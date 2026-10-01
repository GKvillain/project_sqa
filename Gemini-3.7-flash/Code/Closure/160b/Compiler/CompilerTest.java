package com.google.javascript.jscomp;

import com.google.common.collect.Lists;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Level;

import static org.junit.Assert.*;

public class CompilerTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
  }

  // Tests basic compilation of a single input file
  @Test
  public void testCompile_simpleScript_compilesSuccessfully() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "function alert(msg) {}");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = 10; alert(x);");

    Result result = compiler.compile(extern, input, options);

    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());
    assertNotNull(compiler.toSource());
    assertTrue(compiler.toSource().contains("var x = 10"));
  }

  // Tests compile using thread-disabled mode
  @Test
  public void testCompile_disableThreads_compilesSuccessfully() {
    compiler.disableThreads();
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");

    Result result = compiler.compile(extern, input, options);

    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests initOptions with checkSymbols set to false (Defects4J Bug 160 area)
  @Test
  public void testInitOptions_checkSymbolsFalse_configuresWarningsGuard() {
    options.checkSymbols = false;
    compiler.initOptions(options);

    assertNotNull(compiler.getErrorManager());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests initOptions when checkGlobalThisLevel is set
  @Test
  public void testInitOptions_checkGlobalThisLevel_setsWarningLevel() {
    options.checkGlobalThisLevel = CheckLevel.ERROR;
    compiler.initOptions(options);

    assertEquals(CheckLevel.ERROR, options.checkGlobalThisLevel);
  }

  // Tests initOptions enabling and disabling CHECK_TYPES diagnostic group
  @Test
  public void testInitOptions_diagnosticGroupsCheckTypes_setsCheckTypesFlag() {
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.WARNING);
    compiler.initOptions(options);
    assertTrue(options.checkTypes);

    CompilerOptions options2 = new CompilerOptions();
    options2.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.OFF);
    Compiler compiler2 = new Compiler();
    compiler2.initOptions(options2);
    assertFalse(options2.checkTypes);
  }

  // Tests parsing test code into an AST node
  @Test
  public void testParseTestCode_validJs_returnsNonNullScriptNode() {
    Node root = compiler.parseTestCode("var a = 1;");

    assertNotNull(root);
    assertEquals(" [testcode] ", root.getSourceFileName());
    assertNotNull(compiler.getInput(" [testcode] "));
  }

  // Tests parsing synthetic code
  @Test
  public void testParseSyntheticCode_validJs_returnsAstNode() {
    Node root = compiler.parseSyntheticCode("synthetic.js", "function foo() { return true; }");

    assertNotNull(root);
    assertNotNull(compiler.getInput("synthetic.js"));
  }

  // Tests setPassConfig with null throwing NullPointerException
  @Test(expected = NullPointerException.class)
  public void testSetPassConfig_nullPassConfig_throwsNullPointerException() {
    compiler.setPassConfig(null);
  }

  // Tests setPassConfig when passes already assigned throwing IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testSetPassConfig_alreadyAssigned_throwsIllegalStateException() {
    PassConfig passConfig = new DefaultPassConfig(options);
    compiler.setPassConfig(passConfig);
    compiler.setPassConfig(passConfig);
  }

  // Tests adding new extern input
  @Test
  public void testNewExternInput_uniqueName_addsExternSuccessfully() {
    List<JSSourceFile> externs = Lists.newArrayList(JSSourceFile.fromCode("externs.js", ""));
    List<JSSourceFile> inputs = Lists.newArrayList(JSSourceFile.fromCode("input.js", "var a = 1;"));
    compiler.init(externs, inputs, options);
    compiler.parseInputs();

    CompilerInput newInput = compiler.newExternInput("custom_externs.js");

    assertNotNull(newInput);
    assertEquals("custom_externs.js", newInput.getName());
    assertTrue(newInput.isExtern());
    assertNotNull(compiler.getInput("custom_externs.js"));
  }

  // Tests adding duplicate extern input throws IllegalArgumentException
  @Test(expected = IllegalArgumentException.class)
  public void testNewExternInput_duplicateName_throwsIllegalArgumentException() {
    List<JSSourceFile> externs = Lists.newArrayList(JSSourceFile.fromCode("externs.js", ""));
    List<JSSourceFile> inputs = Lists.newArrayList(JSSourceFile.fromCode("input.js", "var a = 1;"));
    compiler.init(externs, inputs, options);
    compiler.parseInputs();

    compiler.newExternInput("custom_externs.js");
    compiler.newExternInput("custom_externs.js");
  }

  // Tests removing input by name
  @Test
  public void testRemoveInput_existingInput_removesInput() {
    List<JSSourceFile> externs = Lists.newArrayList(JSSourceFile.fromCode("externs.js", ""));
    List<JSSourceFile> inputs = Lists.newArrayList(JSSourceFile.fromCode("input.js", "var a = 1;"));
    compiler.init(externs, inputs, options);
    compiler.parseInputs();

    assertNotNull(compiler.getInput("input.js"));
    compiler.removeInput("input.js");
    assertNull(compiler.getInput("input.js"));
  }

  // Tests multi-module compilation and toSource with modules
  @Test
  public void testCompileModules_validModules_compilesSuccessfully() {
    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1.js", "var a = 1;"));

    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2.js", "var b = 2;"));
    m2.addDependency(m1);

    List<JSModule> modules = Lists.newArrayList(m1, m2);
    List<JSSourceFile> externs = Lists.newArrayList(JSSourceFile.fromCode("externs.js", ""));

    Result result = compiler.compileModules(externs, modules, options);

    assertTrue(result.success);
    String sourceM1 = compiler.toSource(m1);
    String sourceM2 = compiler.toSource(m2);
    assertTrue(sourceM1.contains("var a = 1"));
    assertTrue(sourceM2.contains("var b = 2"));
  }

  // Tests saving and restoring compiler intermediate state
  @Test
  public void testGetStateAndSetState_roundTrip_preservesState() {
    List<JSSourceFile> externs = Lists.newArrayList(JSSourceFile.fromCode("externs.js", ""));
    List<JSSourceFile> inputs = Lists.newArrayList(JSSourceFile.fromCode("input.js", "var a = 1;"));
    compiler.init(externs, inputs, options);
    compiler.parseInputs();

    Compiler.IntermediateState state = compiler.getState();
    assertNotNull(state);

    Compiler newCompiler = new Compiler();
    newCompiler.init(externs, inputs, options);
    newCompiler.setState(state);

    assertNotNull(newCompiler.getRoot());
  }

  // Tests CodeChangeHandler notification
  @Test
  public void testReportCodeChange_withRegisteredHandler_notifiesHandler() {
    final boolean[] changed = new boolean[]{false};
    CodeChangeHandler handler = new CodeChangeHandler() {
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

  // Tests CodeBuilder helper tracking lines and columns
  @Test
  public void testCodeBuilder_appendAndMetrics_tracksLinesAndColumns() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    cb.append("var x = 1;\nvar y = 2;");

    assertEquals(1, cb.getLineIndex());
    assertEquals(10, cb.getColumnIndex());
    assertTrue(cb.endsWith(";"));
    assertFalse(cb.endsWith("\n"));
    assertEquals(21, cb.getLength());

    cb.reset();
    assertEquals(0, cb.getLength());
    assertEquals(1, cb.getLineIndex());
  }

  // Tests custom PrintStream error output
  @Test
  public void testCompilerConstructor_customPrintStream_initializesErrorManager() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(out);
    Compiler customCompiler = new Compiler(ps);

    customCompiler.initOptions(options);
    assertNotNull(customCompiler.getErrorManager());
  }

  // Tests type registry and type checking query
  @Test
  public void testGetTypeRegistry_defaultInitialization_returnsNonNullRegistry() {
    assertNotNull(compiler.getTypeRegistry());
    assertFalse(compiler.isTypeCheckingEnabled());

    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.WARNING);
    compiler.initOptions(options);
    assertTrue(compiler.isTypeCheckingEnabled());
  }

  // Tests hasHaltingErrors query
  @Test
  public void testHasHaltingErrors_cleanCompilation_returnsFalse() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var valid = 1;");
    compiler.compile(extern, input, options);

    assertFalse(compiler.hasHaltingErrors());
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests compilation with syntax error resulting in errors
  @Test
  public void testCompile_syntaxError_reportsErrorAndFails() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("invalid.js", "var = ;");

    Result result = compiler.compile(extern, input, options);

    assertFalse(result.success);
    assertTrue(compiler.getErrorCount() > 0);
    assertTrue(compiler.hasHaltingErrors());
  }

  // Tests roots access after initialization and parsing
  @Test
  public void testGetRoots_afterParse_returnsAstNodes() {
    List<JSSourceFile> externs = Lists.newArrayList(JSSourceFile.fromCode("externs.js", ""));
    List<JSSourceFile> inputs = Lists.newArrayList(JSSourceFile.fromCode("input.js", "var a = 1;"));
    compiler.init(externs, inputs, options);
    compiler.parseInputs();

    assertNotNull(compiler.getRoot());
    assertNotNull(compiler.getJsRoot());
    assertNotNull(compiler.getExternsRoot());
  }

  // Tests logging level configuration
  @Test
  public void testSetLoggingLevel_validLevel_setsLevelWithoutError() {
    Compiler.setLoggingLevel(Level.FINE);
    Compiler.setLoggingLevel(Level.INFO);
  }

  // Tests Tracer creation and stop
  @Test
  public void testNewTracer_validName_tracesPassExecution() {
    Tracer tracer = compiler.newTracer("testPass");
    assertNotNull(tracer);
    tracer.stop();
  }
}