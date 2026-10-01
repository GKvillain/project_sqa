package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

public class CompilerTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
  }

  // Tests default initialization and initial state
  @Test
  public void testConstructor_default_initializesCorrectly() {
    assertNotNull(compiler.getErrorManager());
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
    assertFalse(compiler.hasErrors());
  }

  // Tests custom PrintStream constructor and stream output
  @Test
  public void testConstructor_withPrintStream_createsStreamErrorManager() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    Compiler customCompiler = new Compiler(ps);
    customCompiler.initOptions(options);

    assertNotNull(customCompiler.getErrorManager());
    assertEquals(0, customCompiler.getErrorCount());
  }

  // Tests custom ErrorManager constructor
  @Test
  public void testConstructor_withErrorManager_setsErrorManager() {
    BasicErrorManager errorManager = new LoggerErrorManager(options.errorFormat.toFormatter(compiler, false), null);
    Compiler customCompiler = new Compiler(errorManager);

    assertSame(errorManager, customCompiler.getErrorManager());
  }

  // Tests setting null ErrorManager throws NullPointerException
  @Test(expected = NullPointerException.class)
  public void testSetErrorManager_null_throwsException() {
    compiler.setErrorManager(null);
  }

  // Tests compiling simple valid JavaScript code
  @Test
  public void testCompile_simpleValidCode_returnsSuccessResult() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "function alert(x) {}");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = 1; alert(x);");

    Result result = compiler.compile(extern, input, options);

    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());
    assertNotNull(compiler.getRoot());
    assertNotNull(compiler.jsRoot);
    assertNotNull(compiler.externsRoot);
  }

  // Tests compiling with syntax error in input
  @Test
  public void testCompile_syntaxError_reportsError() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = ;");

    Result result = compiler.compile(extern, input, options);

    assertFalse(result.success);
    assertTrue(compiler.getErrorCount() > 0);
    assertTrue(compiler.hasErrors());
  }

  // Tests parseTestCode returns valid AST node
  @Test
  public void testParseTestCode_validJs_returnsScriptNode() {
    Node node = compiler.parseTestCode("var a = 10;");

    assertNotNull(node);
    assertEquals(Token.SCRIPT, node.getType());
    assertEquals(1, node.getChildCount());
  }

  // Tests parseSyntheticCode with explicit name
  @Test
  public void testParseSyntheticCode_withName_createsInputAndNode() {
    Node node = compiler.parseSyntheticCode("synthetic.js", "function foo() {}");

    assertNotNull(node);
    assertEquals(Token.SCRIPT, node.getType());
    assertNotNull(compiler.getInput("synthetic.js"));
  }

  // Tests disableThreads compiles on current thread
  @Test
  public void testDisableThreads_compilesCorrectly() {
    compiler.disableThreads();
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");

    Result result = compiler.compile(extern, input, options);

    assertTrue(result.success);
    assertEquals("var a=1", compiler.toSource().trim());
  }

  // Tests compiling modules with proper dependencies
  @Test
  public void testCompile_validModules_succeeds() {
    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1.js", "var a = 1;"));

    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2.js", "var b = a;"));
    m2.addDependency(m1);

    JSSourceFile[] externs = new JSSourceFile[] { JSSourceFile.fromCode("externs.js", "") };
    JSModule[] modules = new JSModule[] { m1, m2 };

    Result result = compiler.compile(externs, modules, options);

    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());
    assertNotNull(compiler.getModuleGraph());
  }

  // Tests module dependency error when modules are out of dependency order
  @Test
  public void testInit_modulesOutOfOrder_reportsModuleDependencyError() {
    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1.js", "var a = 1;"));

    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2.js", "var b = a;"));
    m1.addDependency(m2);

    JSSourceFile[] externs = new JSSourceFile[] { JSSourceFile.fromCode("externs.js", "") };
    JSModule[] modules = new JSModule[] { m1, m2 };

    compiler.init(externs, modules, options);

    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
  }

  // Tests duplicate input files across modules
  @Test
  public void testInit_duplicateInputsInModules_reportsError() {
    JSSourceFile duplicateFile = JSSourceFile.fromCode("dup.js", "var a = 1;");
    JSModule m1 = new JSModule("m1");
    m1.add(duplicateFile);

    JSModule m2 = new JSModule("m2");
    m2.add(duplicateFile);

    JSSourceFile[] externs = new JSSourceFile[] { JSSourceFile.fromCode("externs.js", "") };
    JSModule[] modules = new JSModule[] { m1, m2 };

    compiler.init(externs, modules, options);

    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
  }

  // Tests duplicate input file names in inputs list
  @Test
  public void testInit_duplicateInputNames_reportsError() {
    JSSourceFile[] externs = new JSSourceFile[] { JSSourceFile.fromCode("externs.js", "") };
    JSSourceFile[] inputs = new JSSourceFile[] {
        JSSourceFile.fromCode("test.js", "var a = 1;"),
        JSSourceFile.fromCode("test.js", "var b = 2;")
    };

    compiler.init(externs, inputs, options);

    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
  }

  // Tests getNodeForCodeInsertion with root module
  @Test
  public void testGetNodeForCodeInsertion_nullModule_returnsFirstInputRoot() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = 1;");
    compiler.compile(extern, input, options);

    Node node = compiler.getNodeForCodeInsertion(null);

    assertNotNull(node);
    assertEquals(Token.SCRIPT, node.getType());
  }

  // Tests getNodeForCodeInsertion with dependent module containing inputs
  @Test
  public void testGetNodeForCodeInsertion_moduleWithInput_returnsModuleInputRoot() {
    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1.js", "var a = 1;"));

    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2.js", "var b = 2;"));
    m2.addDependency(m1);

    compiler.compile(new JSSourceFile[] { JSSourceFile.fromCode("externs.js", "") },
        new JSModule[] { m1, m2 }, options);

    Node node = compiler.getNodeForCodeInsertion(m2);

    assertNotNull(node);
    assertEquals(Token.SCRIPT, node.getType());
    assertEquals("m2.js", node.getProp(Node.SOURCENAME_PROP));
  }

  // Tests saving and restoring compiler intermediate state
  @Test
  public void testGetAndSetState_restoresState() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");
    compiler.compile(extern, input, options);

    Compiler.IntermediateState state = compiler.getState();
    assertNotNull(state);

    Compiler newCompiler = new Compiler();
    newCompiler.initOptions(options);
    newCompiler.setState(state);

    assertNotNull(newCompiler.getRoot());
    assertEquals(compiler.getRoot(), newCompiler.getRoot());
  }

  // Tests newExternInput adds synthetic extern input correctly
  @Test
  public void testNewExternInput_addsSyntheticExtern() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");
    compiler.compile(extern, input, options);

    CompilerInput newExtern = compiler.newExternInput("custom_extern.js");

    assertNotNull(newExtern);
    assertTrue(newExtern.isExtern());
    assertNotNull(compiler.getInput("custom_extern.js"));
  }

  // Tests newExternInput with conflicting name throws exception
  @Test(expected = IllegalArgumentException.class)
  public void testNewExternInput_conflictingName_throwsException() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");
    compiler.compile(extern, input, options);

    compiler.newExternInput("externs.js");
  }

  // Tests getSourceLine and getSourceRegion for existing source
  @Test
  public void testGetSourceLineAndRegion_validFile_returnsContent() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var line1 = 1;\nvar line2 = 2;\n");
    compiler.compile(extern, input, options);

    String line = compiler.getSourceLine("input.js", 2);
    Region region = compiler.getSourceRegion("input.js", 2);

    assertEquals("var line2 = 2;", line);
    assertNotNull(region);
  }

  // Tests CodeBuilder helper functionality
  @Test
  public void testCodeBuilder_appendAndCountLines() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    cb.append("foo\nbar\nbaz");

    assertEquals(2, cb.getLineIndex());
    assertEquals(3, cb.getColumnIndex());
    assertTrue(cb.endsWith("baz"));
    assertFalse(cb.endsWith("foo"));

    cb.reset();
    assertEquals(0, cb.getLength());
    assertEquals(2, cb.getLineIndex());
  }

  // Tests code change handler triggers
  @Test
  public void testReportCodeChange_callsRegisteredHandler() {
    final boolean[] changed = new boolean[] { false };
    CodeChangeHandler handler = new CodeChangeHandler() {
      @Override
      public void reportChange() {
        changed[0] = true;
      }
    };

    compiler.addChangeHandler(handler);
    compiler.reportCodeChange();
    assertTrue(changed[0]);

    compiler.removeChangeHandler(handler);
    changed[0] = false;
    compiler.reportCodeChange();
    assertFalse(changed[0]);
  }
}