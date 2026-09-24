package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.Level;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CompilerTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
  }

  // Tests basic CodeBuilder append, lineCount, and endsWith functionality
  @Test
  public void testCodeBuilder_appendAndEndsWith_returnsExpectedResults() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    cb.append("var x = 1;\nvar y = 2;");

    assertEquals("var x = 1;\nvar y = 2;", cb.toString());
    assertEquals(21, cb.getLength());
    assertEquals(1, cb.getLineIndex());
    assertEquals(10, cb.getColumnIndex());
    assertTrue(cb.endsWith(";"));
    assertTrue(cb.endsWith("var y = 2;"));
    assertFalse(cb.endsWith("x = 1;"));
  }

  // Tests CodeBuilder reset clearing string buffer but preserving line index
  @Test
  public void testCodeBuilder_reset_clearsBuffer() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    cb.append("line 1\nline 2\n");
    assertEquals(2, cb.getLineIndex());

    cb.reset();
    assertEquals("", cb.toString());
    assertEquals(0, cb.getLength());
    assertEquals(2, cb.getLineIndex());
  }

  // Tests parsing test code into an AST node
  @Test
  public void testParseTestCode_validCode_returnsScriptNode() {
    Node node = compiler.parseTestCode("var a = 10;");
    assertNotNull(node);
    assertEquals(Token.SCRIPT, node.getType());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests parsing synthetic code
  @Test
  public void testParseSyntheticCode_validCode_returnsAstNode() {
    Node node = compiler.parseSyntheticCode("synthetic.js", "function foo() { return 1; }");
    assertNotNull(node);
    assertEquals(Token.SCRIPT, node.getType());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests compilation with standard single source input
  @Test
  public void testCompile_singleFile_successResult() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "function alert(x) {}");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = 1; alert(x);");

    Result result = compiler.compile(extern, input, options);
    assertTrue(result.success);
    assertEquals(0, result.errors.length);
    assertNotNull(compiler.getRoot());
  }

  // Tests compilation reporting syntax errors properly
  @Test
  public void testCompile_syntaxError_reportsErrors() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = ;");

    Result result = compiler.compile(extern, input, options);
    assertFalse(result.success);
    assertTrue(result.errors.length > 0);
    assertTrue(compiler.hasErrors());
  }

  // Tests duplicate JS source inputs detection during initialization
  @Test
  public void testInit_duplicateInputs_reportsDuplicateInputError() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input1 = JSSourceFile.fromCode("test.js", "var a = 1;");
    JSSourceFile input2 = JSSourceFile.fromCode("test.js", "var b = 2;");

    compiler.init(new JSSourceFile[]{extern}, new JSSourceFile[]{input1, input2}, options);
    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
    assertEquals(Compiler.DUPLICATE_INPUT, compiler.getErrors()[0].getType());
  }

  // Tests duplicate externs inputs detection during initialization
  @Test
  public void testInit_duplicateExterns_reportsDuplicateExternError() {
    JSSourceFile extern1 = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile extern2 = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("test.js", "var a = 1;");

    compiler.init(new JSSourceFile[]{extern1, extern2}, new JSSourceFile[]{input}, options);
    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
    assertEquals(Compiler.DUPLICATE_EXTERN_INPUT, compiler.getErrors()[0].getType());
  }

  // Tests modules initialization with an empty module list
  @Test
  public void testInit_emptyModules_reportsEmptyModuleListError() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSModule[] modules = new JSModule[0];

    compiler.init(new JSSourceFile[]{extern}, modules, options);
    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
    assertEquals("EMPTY_MODULE_LIST_ERROR", compiler.getErrors()[0].getType().key);
  }

  // Tests modules initialization when root module contains no inputs
  @Test
  public void testInit_emptyRootModule_reportsEmptyRootModuleError() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSModule root = new JSModule("root");
    JSModule[] modules = new JSModule[]{root};

    compiler.init(new JSSourceFile[]{extern}, modules, options);
    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
    assertEquals("EMPTY_ROOT_MODULE_ERROR", compiler.getErrors()[0].getType().key);
  }

  // Tests modules initialization with duplicate inputs across modules
  @Test
  public void testInit_duplicateInputAcrossModules_reportsError() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile sharedInput = JSSourceFile.fromCode("shared.js", "var x = 1;");

    JSModule mod1 = new JSModule("mod1");
    mod1.add(sharedInput);
    JSModule mod2 = new JSModule("mod2");
    mod2.add(sharedInput);

    compiler.init(new JSSourceFile[]{extern}, new JSModule[]{mod1, mod2}, options);
    assertTrue(compiler.hasErrors());
    assertEquals(Compiler.DUPLICATE_INPUT_IN_MODULES, compiler.getErrors()[0].getType());
  }

  // Tests disableThreads execution path
  @Test
  public void testDisableThreads_compileRunsCorrectly() {
    compiler.disableThreads();
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var z = 3;");

    Result result = compiler.compile(extern, input, options);
    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests newExternInput functionality and duplicate extern check
  @Test
  public void testNewExternInput_validName_addsExternSuccessfully() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");
    compiler.init(new JSSourceFile[]{extern}, new JSSourceFile[]{input}, options);
    compiler.parseInputs();

    CompilerInput newExtern = compiler.newExternInput("custom_extern.js");
    assertNotNull(newExtern);
    assertEquals("custom_extern.js", newExtern.getName());
    assertNotNull(compiler.getInput("custom_extern.js"));
  }

  // Tests newExternInput with conflicting extern name throwing IllegalArgumentException
  @Test(expected = IllegalArgumentException.class)
  public void testNewExternInput_duplicateName_throwsIllegalArgumentException() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");
    compiler.init(new JSSourceFile[]{extern}, new JSSourceFile[]{input}, options);
    compiler.parseInputs();

    compiler.newExternInput("externs.js");
  }

  // Tests getNodeForCodeInsertion with null module
  @Test
  public void testGetNodeForCodeInsertion_nullModule_returnsFirstInputRoot() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");
    compiler.init(new JSSourceFile[]{extern}, new JSSourceFile[]{input}, options);
    compiler.parseInputs();

    Node insertionNode = compiler.getNodeForCodeInsertion(null);
    assertNotNull(insertionNode);
    assertEquals(Token.SCRIPT, insertionNode.getType());
  }

  // Tests getNodeForCodeInsertion without inputs throwing IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testGetNodeForCodeInsertion_noInputs_throwsIllegalStateException() {
    compiler.init(new JSSourceFile[]{}, new JSSourceFile[]{}, options);
    compiler.getNodeForCodeInsertion(null);
  }

  // Tests getSourceLine and getSourceRegion retrieval
  @Test
  public void testGetSourceLineAndRegion_validInput_returnsSourceInfo() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;\nvar b = 2;\nvar c = 3;");
    compiler.init(new JSSourceFile[]{extern}, new JSSourceFile[]{input}, options);

    assertEquals("var b = 2;", compiler.getSourceLine("input.js", 2));
    assertNull(compiler.getSourceLine("input.js", 0));
    assertNull(compiler.getSourceLine("nonexistent.js", 1));

    Region region = compiler.getSourceRegion("input.js", 2);
    assertNotNull(region);
    assertEquals("var b = 2;", region.getSourceExcerpt());
  }

  // Tests getState and setState for IntermediateState saving and restoring
  @Test
  public void testGetStateAndSetState_restoresCompilerState() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");
    compiler.init(new JSSourceFile[]{extern}, new JSSourceFile[]{input}, options);
    compiler.parseInputs();

    Compiler.IntermediateState state = compiler.getState();
    assertNotNull(state);

    Compiler newCompiler = new Compiler();
    newCompiler.init(new JSSourceFile[]{extern}, new JSSourceFile[]{input}, options);
    newCompiler.setState(state);

    assertEquals(compiler.getRoot(), newCompiler.getRoot());
  }

  // Tests areNodesEqualForInlining equality check
  @Test
  public void testAreNodesEqualForInlining_identicalNodes_returnsTrue() {
    compiler.initCompilerOptionsIfTesting();
    Node n1 = Node.newString("foo");
    Node n2 = Node.newString("foo");
    Node n3 = Node.newString("bar");

    assertTrue(compiler.areNodesEqualForInlining(n1, n2));
    assertFalse(compiler.areNodesEqualForInlining(n1, n3));
  }

  // Tests Compiler with custom PrintStream
  @Test
  public void testCompiler_withPrintStream_initializesProperly() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    Compiler customCompiler = new Compiler(ps);

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");
    Result result = customCompiler.compile(extern, input, options);

    assertTrue(result.success);
    assertNotNull(customCompiler.getErrorManager());
  }

  // Tests setLoggingLevel
  @Test
  public void testSetLoggingLevel_executesWithoutError() {
    Compiler.setLoggingLevel(Level.WARNING);
    Compiler.setLoggingLevel(Level.INFO);
  }
}