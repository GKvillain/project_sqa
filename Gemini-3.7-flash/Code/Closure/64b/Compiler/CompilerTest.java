package com.google.javascript.jscomp;

import com.google.javascript.jscomp.CompilerOptions.DevMode;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CompilerTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
  }

  // Tests basic compilation of a simple script
  @Test
  public void testCompile_simpleScript_succeeds() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "function alert(x) {}");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = 1; alert(x);");
    
    Result result = compiler.compile(extern, input, options);
    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());
    assertNotNull(compiler.getRoot());
    assertTrue(compiler.toSource().contains("var x=1;alert(x)"));
  }

  // Tests compilation with multiple inputs and toSource generation
  @Test
  public void testCompile_multipleInputs_concatenatesOutput() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input1 = JSSourceFile.fromCode("input1.js", "var a = 1;");
    JSSourceFile input2 = JSSourceFile.fromCode("input2.js", "var b = 2;");
    
    Result result = compiler.compile(new JSSourceFile[]{extern}, new JSSourceFile[]{input1, input2}, options);
    assertTrue(result.success);
    String source = compiler.toSource();
    assertTrue(source.contains("var a=1;"));
    assertTrue(source.contains("var b=2;"));
  }

  // Tests toSourceArray returning code separated per input
  @Test
  public void testToSourceArray_multipleInputs_returnsCorrectArray() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input1 = JSSourceFile.fromCode("input1.js", "var a = 10;");
    JSSourceFile input2 = JSSourceFile.fromCode("input2.js", "var b = 20;");
    
    compiler.compile(new JSSourceFile[]{extern}, new JSSourceFile[]{input1, input2}, options);
    String[] sources = compiler.toSourceArray();
    
    assertEquals(2, sources.length);
    assertTrue(sources[0].contains("var a=10"));
    assertTrue(sources[1].contains("var b=20"));
  }

  // Tests ECMASCRIPT5_STRICT language mode
  @Test
  public void testCompile_es5Strict_emitsUseStrict() {
    options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);
    
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");
    
    Result result = compiler.compile(extern, input, options);
    assertTrue(result.success);
    String source = compiler.toSource();
    assertTrue(source.contains("var a=1"));
  }

  // Tests compilation with JSModules
  @Test
  public void testCompile_modules_succeeds() {
    JSModule rootModule = new JSModule("root");
    rootModule.add(JSSourceFile.fromCode("root.js", "var m1 = 1;"));

    JSModule depModule = new JSModule("dep");
    depModule.add(JSSourceFile.fromCode("dep.js", "var m2 = 2;"));
    depModule.addDependency(rootModule);

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    Result result = compiler.compile(new JSSourceFile[]{extern}, new JSModule[]{rootModule, depModule}, options);
    
    assertTrue(result.success);
    String rootSource = compiler.toSource(rootModule);
    String depSource = compiler.toSource(depModule);
    assertTrue(rootSource.contains("var m1=1"));
    assertTrue(depSource.contains("var m2=2"));
  }

  // Tests module dependency error when order is invalid
  @Test
  public void testCompile_invalidModuleOrder_reportsError() {
    JSModule modA = new JSModule("modA");
    modA.add(JSSourceFile.fromCode("a.js", "var a = 1;"));

    JSModule modB = new JSModule("modB");
    modB.add(JSSourceFile.fromCode("b.js", "var b = 2;"));

    // modA depends on modB, but modA is listed first
    modA.addDependency(modB);

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    Result result = compiler.compile(new JSSourceFile[]{extern}, new JSModule[]{modA, modB}, options);
    
    assertFalse(result.success);
    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
  }

  // Tests parseTestCode helper
  @Test
  public void testParseTestCode_validCode_returnsScriptNode() {
    Node node = compiler.parseTestCode("var a = 1 + 2;");
    assertNotNull(node);
    assertEquals(Token.SCRIPT, node.getType());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests parseSyntheticCode helper
  @Test
  public void testParseSyntheticCode_validCode_returnsScriptNode() {
    Node node = compiler.parseSyntheticCode("custom.js", "var x = true;");
    assertNotNull(node);
    assertEquals(Token.SCRIPT, node.getType());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests newExternInput and duplicate extern input name error
  @Test(expected = IllegalArgumentException.class)
  public void testNewExternInput_duplicateName_throwsException() {
    compiler.init(new JSSourceFile[]{}, new JSSourceFile[]{JSSourceFile.fromCode("input.js", "var x;")}, options);
    compiler.parseInputs();
    compiler.newExternInput("extern1.js");
    compiler.newExternInput("extern1.js");
  }

  // Tests removeInput successfully detaches node and removes entry
  @Test
  public void testRemoveInput_existingInput_removesSuccessfully() {
    JSSourceFile input1 = JSSourceFile.fromCode("file1.js", "var a = 1;");
    JSSourceFile input2 = JSSourceFile.fromCode("file2.js", "var b = 2;");
    compiler.init(new JSSourceFile[]{}, new JSSourceFile[]{input1, input2}, options);
    compiler.parseInputs();

    assertNotNull(compiler.getInput("file1.js"));
    compiler.removeInput("file1.js");
    assertNull(compiler.getInput("file1.js"));
  }

  // Tests removeInput on non-existent input does not fail
  @Test
  public void testRemoveInput_nonExistentInput_doesNothing() {
    compiler.init(new JSSourceFile[]{}, new JSSourceFile[]{JSSourceFile.fromCode("input.js", "var a = 1;")}, options);
    compiler.parseInputs();
    compiler.removeInput("nonExistent.js");
    assertNotNull(compiler.getInput("input.js"));
  }

  // Tests CodeBuilder append, reset, line and column tracking, endsWith
  @Test
  public void testCodeBuilder_variousOperations_tracksCorrectState() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    assertEquals(0, cb.getLength());
    assertEquals(0, cb.getLineIndex());
    assertEquals(0, cb.getColumnIndex());
    assertEquals("", cb.toString());

    cb.append("var x = 1;\nvar y = 2;");
    assertEquals(21, cb.getLength());
    assertEquals(1, cb.getLineIndex());
    assertEquals(10, cb.getColumnIndex());
    assertTrue(cb.endsWith(";"));
    assertFalse(cb.endsWith("\n"));

    cb.reset();
    assertEquals(0, cb.getLength());
    assertEquals(1, cb.getLineIndex());
    assertEquals("", cb.toString());
  }

  // Tests toSource with input delimiter option enabled
  @Test
  public void testToSource_withInputDelimiter_appendsDelimiter() {
    options.printInputDelimiter = true;
    options.inputDelimiter = "// INPUT: %name% (#%num%)";
    
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("myInput.js", "var z = 42;");
    
    Result result = compiler.compile(extern, input, options);
    assertTrue(result.success);
    String source = compiler.toSource();
    assertTrue(source.contains("// INPUT: myInput.js (#0)"));
    assertTrue(source.contains("var z=42"));
  }

  // Tests uniqueNameIdSupplier generation and reset
  @Test
  public void testGetUniqueNameIdSupplier_andReset_incrementsCorrectly() {
    com.google.common.base.Supplier<String> supplier = compiler.getUniqueNameIdSupplier();
    assertEquals("0", supplier.get());
    assertEquals("1", supplier.get());
    assertEquals("2", supplier.get());

    compiler.resetUniqueNameId();
    assertEquals("0", supplier.get());
  }

  // Tests save and restore state (IntermediateState)
  @Test
  public void testGetState_andSetState_preservesState() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");
    
    compiler.init(new JSSourceFile[]{extern}, new JSSourceFile[]{input}, options);
    compiler.parseInputs();

    Compiler.IntermediateState state = compiler.getState();
    assertNotNull(state);

    Compiler newCompiler = new Compiler();
    newCompiler.init(new JSSourceFile[]{extern}, new JSSourceFile[]{input}, options);
    newCompiler.setState(state);
    assertNotNull(newCompiler.getRoot());
  }

  // Tests error reporting and error counting with custom ErrorManager
  @Test
  public void testSetErrorManager_andReportError_updatesCounts() {
    BasicErrorManager errorManager = new BasicErrorManager() {
      @Override
      public void println(CheckLevel level, JSError error) {}
      @Override
      public void printSummary() {}
    };
    compiler.setErrorManager(errorManager);
    assertSame(errorManager, compiler.getErrorManager());

    JSError error = JSError.make("test.js", 1, 0, CheckLevel.ERROR, Compiler.DUPLICATE_INPUT, "foo.js");
    compiler.report(error);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrors().length);
  }

  // Tests disableThreads setting
  @Test
  public void testDisableThreads_compilesSuccessfully() {
    compiler.disableThreads();
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 100;");
    
    Result result = compiler.compile(extern, input, options);
    assertTrue(result.success);
    assertTrue(compiler.toSource().contains("var a=100"));
  }

  // Tests areNodesEqualForInlining with and without property disambiguation options
  @Test
  public void testAreNodesEqualForInlining_comparesNodes() {
    Node n1 = Node.newString("foo");
    Node n2 = Node.newString("foo");
    Node n3 = Node.newString("bar");

    compiler.initOptions(options);
    assertTrue(compiler.areNodesEqualForInlining(n1, n2));
    assertFalse(compiler.areNodesEqualForInlining(n1, n3));

    options.disambiguateProperties = true;
    assertTrue(compiler.areNodesEqualForInlining(n1, n2));
    assertFalse(compiler.areNodesEqualForInlining(n1, n3));
  }

  // Tests constructor with PrintStream parameter
  @Test
  public void testConstructor_withPrintStream_initializes() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    Compiler customCompiler = new Compiler(ps);

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = 5;");
    Result result = customCompiler.compile(extern, input, new CompilerOptions());
    assertTrue(result.success);
  }

  // Tests getSourceLine and getSourceRegion
  @Test
  public void testGetSourceLineAndRegion_validInput_returnsSourceInfo() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var line1 = 1;\nvar line2 = 2;\nvar line3 = 3;");
    
    compiler.init(new JSSourceFile[]{extern}, new JSSourceFile[]{input}, options);
    compiler.parseInputs();

    assertEquals("var line1 = 1;", compiler.getSourceLine("input.js", 1));
    assertEquals("var line2 = 2;", compiler.getSourceLine("input.js", 2));
    assertNull(compiler.getSourceLine("input.js", 0));
    assertNull(compiler.getSourceLine("nonExistent.js", 1));
  }
}