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

  // Tests basic compilation with single extern and input file
  @Test
  public void testCompile_simpleInput_success() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "function alert(msg) {}");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = 10; alert(x);");

    Result result = compiler.compile(extern, input, options);

    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());
    assertNotNull(compiler.getRoot());
    assertTrue(compiler.toSource().contains("var x=10"));
  }

  // Tests compilation with multiple input source files
  @Test
  public void testCompile_multipleInputs_generatesCorrectSource() {
    JSSourceFile[] externs = new JSSourceFile[] {
        JSSourceFile.fromCode("externs.js", "")
    };
    JSSourceFile[] inputs = new JSSourceFile[] {
        JSSourceFile.fromCode("f1.js", "var a = 1;"),
        JSSourceFile.fromCode("f2.js", "var b = 2;")
    };

    Result result = compiler.compile(externs, inputs, options);

    assertTrue(result.success);
    String source = compiler.toSource();
    assertTrue(source.contains("var a=1"));
    assertTrue(source.contains("var b=2"));

    String[] sourceArray = compiler.toSourceArray();
    assertEquals(2, sourceArray.length);
    assertTrue(sourceArray[0].contains("var a=1"));
    assertTrue(sourceArray[1].contains("var b=2"));
  }

  // Tests compilation error when JS contains syntax errors
  @Test
  public void testCompile_syntaxError_reportsError() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = ;");

    Result result = compiler.compile(extern, input, options);

    assertFalse(result.success);
    assertTrue(compiler.getErrorCount() > 0);
    assertTrue(compiler.hasErrors());
  }

  // Tests module compilation with module dependencies
  @Test
  public void testCompileModules_dependentModules_success() {
    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1.js", "var x = 1;"));

    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2.js", "var y = x + 1;"));
    m2.addDependency(m1);

    List<JSSourceFile> externs = new ArrayList<JSSourceFile>();
    externs.add(JSSourceFile.fromCode("externs.js", ""));

    List<JSModule> modules = new ArrayList<JSModule>();
    modules.add(m1);
    modules.add(m2);

    Result result = compiler.compileModules(externs, modules, options);

    assertTrue(result.success);
    assertNotNull(compiler.getModuleGraph());
    assertEquals("var x=1;", compiler.toSource(m1).trim());
    assertEquals("var y=x+1;", compiler.toSource(m2).trim());
  }

  // Tests module dependency error when modules are in invalid order
  @Test
  public void testCompileModules_badDependencyOrder_reportsModuleDependencyError() {
    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1.js", "var x = 1;"));

    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2.js", "var y = 2;"));

    // m1 depends on m2, but m1 is listed first
    m1.addDependency(m2);

    List<JSSourceFile> externs = new ArrayList<JSSourceFile>();
    externs.add(JSSourceFile.fromCode("externs.js", ""));

    List<JSModule> modules = new ArrayList<JSModule>();
    modules.add(m1);
    modules.add(m2);

    Result result = compiler.compileModules(externs, modules, options);

    assertFalse(result.success);
    assertTrue(compiler.getErrorCount() > 0);
    assertEquals("JSC_MODULE_DEPENDENCY_ERROR", compiler.getErrors()[0].getType().key);
  }

  // Tests compiling empty module list reports error
  @Test
  public void testInitModules_emptyModuleList_reportsError() {
    List<JSSourceFile> externs = new ArrayList<JSSourceFile>();
    List<JSModule> modules = new ArrayList<JSModule>();

    compiler.initModules(externs, modules, options);

    assertTrue(compiler.hasErrors());
    assertEquals("JSC_EMPTY_MODULE_LIST_ERROR", compiler.getErrors()[0].getType().key);
  }

  // Tests duplicate input error reporting
  @Test
  public void testInitInputsByNameMap_duplicateInput_reportsDuplicateError() {
    List<JSSourceFile> externs = new ArrayList<JSSourceFile>();
    List<JSSourceFile> inputs = new ArrayList<JSSourceFile>();
    inputs.add(JSSourceFile.fromCode("duplicate.js", "var a = 1;"));
    inputs.add(JSSourceFile.fromCode("duplicate.js", "var b = 2;"));

    compiler.init(externs, inputs, options);

    assertTrue(compiler.hasErrors());
    assertEquals("JSC_DUPLICATE_INPUT", compiler.getErrors()[0].getType().key);
  }

  // Tests parseTestCode returns valid AST and registers input
  @Test
  public void testParseTestCode_validJs_returnsNode() {
    Node node = compiler.parseTestCode("var a = 42;");

    assertNotNull(node);
    assertEquals(Token.SCRIPT, node.getType());
    assertNotNull(compiler.getInput(" [testcode] "));
  }

  // Tests parseSyntheticCode with custom name
  @Test
  public void testParseSyntheticCode_customName_returnsNode() {
    Node node = compiler.parseSyntheticCode("synth.js", "function test() { return 1; }");

    assertNotNull(node);
    assertNotNull(compiler.getInput("synth.js"));
  }

  // Tests newExternInput adds extern properly
  @Test
  public void testNewExternInput_validName_addsExtern() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = 1;");
    compiler.compile(extern, input, options);

    CompilerInput newExtern = compiler.newExternInput("dynamicExtern.js");

    assertNotNull(newExtern);
    assertTrue(newExtern.isExtern());
    assertEquals(newExtern, compiler.getInput("dynamicExtern.js"));
  }

  // Tests newExternInput with conflicting name throws exception
  @Test(expected = IllegalArgumentException.class)
  public void testNewExternInput_conflictingName_throwsException() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = 1;");
    compiler.compile(extern, input, options);

    compiler.newExternInput("externs.js");
  }

  // Tests removeInput successfully removes input from AST
  @Test
  public void testRemoveInput_existingInput_removesSuccessfully() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = 1;");
    compiler.compile(extern, input, options);

    assertNotNull(compiler.getInput("input.js"));
    compiler.removeInput("input.js");
    assertNull(compiler.getInput("input.js"));
  }

  // Tests languageMode and acceptEcmaScript5 branch coverage
  @Test
  public void testLanguageMode_ecmascript5Modes_returnsExpected() {
    options.setLanguageIn(LanguageMode.ECMASCRIPT5);
    compiler.initOptions(options);
    assertTrue(compiler.acceptEcmaScript5());
    assertEquals(LanguageMode.ECMASCRIPT5, compiler.languageMode());

    options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);
    compiler.initOptions(options);
    assertTrue(compiler.acceptEcmaScript5());
    assertEquals(LanguageMode.ECMASCRIPT5_STRICT, compiler.languageMode());

    options.setLanguageIn(LanguageMode.ECMASCRIPT3);
    compiler.initOptions(options);
    assertFalse(compiler.acceptEcmaScript5());
    assertEquals(LanguageMode.ECMASCRIPT3, compiler.languageMode());
  }

  // Tests CodeBuilder append, reset, line/col tracking and endsWith
  @Test
  public void testCodeBuilder_trackingAndEndsWith() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    cb.append("hello\nworld");

    assertEquals("hello\nworld", cb.toString());
    assertEquals(11, cb.getLength());
    assertEquals(1, cb.getLineIndex());
    assertEquals(5, cb.getColumnIndex());
    assertTrue(cb.endsWith("world"));
    assertFalse(cb.endsWith("hello"));

    cb.reset();
    assertEquals(0, cb.getLength());
    assertEquals(1, cb.getLineIndex());
  }

  // Tests getState and setState for compiler intermediate state
  @Test
  public void testGetAndSetState_preservesState() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");
    compiler.compile(extern, input, options);

    Compiler.IntermediateState state = compiler.getState();
    assertNotNull(state);

    Compiler newCompiler = new Compiler();
    newCompiler.initOptions(options);
    newCompiler.setState(state);

    assertEquals(state.externsRoot, newCompiler.externsRoot);
    assertEquals(state.jsRoot, newCompiler.jsRoot);
  }

  // Tests custom print stream constructor and error output
  @Test
  public void testCompiler_printStreamConstructor_writesToStream() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    Compiler customCompiler = new Compiler(ps);

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var = ;");

    customCompiler.compile(extern, input, options);

    assertTrue(customCompiler.hasErrors());
    assertTrue(baos.toString().length() > 0);
  }

  // Tests disableThreads flag execution path
  @Test
  public void testDisableThreads_compilesSuccessfully() {
    compiler.disableThreads();
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = 42;");

    Result result = compiler.compile(extern, input, options);

    assertTrue(result.success);
    assertEquals("var x=42;", compiler.toSource().trim());
  }

  // Tests getSourceLine and getSourceRegion
  @Test
  public void testGetSourceLineAndRegion_validInput_returnsLineContent() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("test.js", "line1;\nline2;\nline3;");
    compiler.compile(extern, input, options);

    assertEquals("line2;", compiler.getSourceLine("test.js", 2));
    assertNull(compiler.getSourceLine("test.js", 0));
    assertNull(compiler.getSourceLine("nonexistent.js", 1));

    assertNotNull(compiler.getSourceRegion("test.js", 2));
    assertNull(compiler.getSourceRegion("test.js", -1));
  }

  // Tests toSource formatting with input delimiter option
  @Test
  public void testToSource_withInputDelimiter_appendsDelimiter() {
    options.printInputDelimiter = true;
    options.inputDelimiter = "// [%name%]";
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("foo.js", "var x = 1;");

    Result result = compiler.compile(extern, input, options);

    assertTrue(result.success);
    String source = compiler.toSource();
    assertTrue(source.contains("// [foo.js]"));
  }
}