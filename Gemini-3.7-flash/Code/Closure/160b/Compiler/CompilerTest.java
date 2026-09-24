package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CompilerTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.disableThreads();
    options = new CompilerOptions();
  }

  // Tests initialization of options with default settings
  @Test
  public void testInitOptions_defaultOptions_initializesErrorManagerAndGuards() {
    compiler.initOptions(options);
    assertNotNull(compiler.getErrorManager());
    assertNotNull(compiler.getOptions());
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests checkTypes flag enabled via DiagnosticGroups
  @Test
  public void testInitOptions_checkTypesDiagnosticGroup_enablesCheckTypes() {
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.ERROR);
    compiler.initOptions(options);
    assertTrue(compiler.isTypeCheckingEnabled());
    assertTrue(options.checkTypes);
  }

  // Tests checkGlobalThisLevel configuration in initOptions
  @Test
  public void testInitOptions_checkGlobalThisLevel_setsWarningGuard() {
    options.checkGlobalThisLevel = CheckLevel.ERROR;
    compiler.initOptions(options);
    JSError error = JSError.make("test.js", 1, 0, CheckGlobalThis.GLOBAL_THIS);
    assertEquals(CheckLevel.ERROR, compiler.getErrorLevel(error));
  }

  // Tests checkSymbols disabled behavior in warnings guard
  @Test
  public void testInitOptions_checkSymbolsDisabled_turnsOffCheckVariables() {
    options.checkSymbols = false;
    compiler.initOptions(options);
    JSError error = JSError.make("test.js", 1, 0, VarCheck.UNDEFINED_VAR_ERROR, "x");
    assertEquals(CheckLevel.OFF, compiler.getErrorLevel(error));
  }

  // Tests normal compilation of valid JavaScript code
  @Test
  public void testCompile_validCode_success() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1; function test() { return a; }");
    Result result = compiler.compile(extern, input, options);

    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());
    assertNotNull(compiler.getRoot());
    assertNotNull(compiler.toSource());
  }

  // Tests compilation with a syntax error
  @Test
  public void testCompile_syntaxError_reportsErrors() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = ;");
    Result result = compiler.compile(extern, input, options);

    assertFalse(result.success);
    assertTrue(compiler.getErrorCount() > 0);
    assertTrue(compiler.hasErrors());
  }

  // Tests duplicate input file names
  @Test
  public void testCompile_duplicateInput_reportsDuplicateInputError() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input1 = JSSourceFile.fromCode("input.js", "var a = 1;");
    JSSourceFile input2 = JSSourceFile.fromCode("input.js", "var b = 2;");
    Result result = compiler.compile(new JSSourceFile[] { extern }, new JSSourceFile[] { input1, input2 }, options);

    assertFalse(result.success);
    assertTrue(compiler.getErrorCount() > 0);
  }

  // Tests empty module list validation
  @Test
  public void testInitModules_emptyModuleList_reportsError() {
    List<JSSourceFile> externs = new ArrayList<JSSourceFile>();
    List<JSModule> modules = new ArrayList<JSModule>();
    compiler.initModules(externs, modules, options);

    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
  }

  // Tests multiple modules where the root module has no inputs
  @Test
  public void testInitModules_emptyRootModuleWithMultipleModules_reportsError() {
    List<JSSourceFile> externs = new ArrayList<JSSourceFile>();
    List<JSModule> modules = new ArrayList<JSModule>();
    JSModule rootModule = new JSModule("root");
    JSModule childModule = new JSModule("child");
    childModule.add(JSSourceFile.fromCode("child.js", "var c = 1;"));
    childModule.addDependency(rootModule);
    modules.add(rootModule);
    modules.add(childModule);

    compiler.initModules(externs, modules, options);

    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
  }

  // Tests null error manager throws exception
  @Test(expected = NullPointerException.class)
  public void testSetErrorManager_nullManager_throwsException() {
    compiler.setErrorManager(null);
  }

  // Tests null PassConfig throws exception
  @Test(expected = NullPointerException.class)
  public void testSetPassConfig_nullPassConfig_throwsException() {
    compiler.setPassConfig(null);
  }

  // Tests assigning PassConfig twice throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testSetPassConfig_alreadyAssigned_throwsException() {
    PassConfig passConfig = compiler.createPassConfigInternal();
    compiler.setPassConfig(passConfig);
    compiler.setPassConfig(passConfig);
  }

  // Tests parsing test code string
  @Test
  public void testParseTestCode_validJs_returnsAstRoot() {
    Node root = compiler.parseTestCode("var x = 10;");
    assertNotNull(root);
    assertEquals(Token.SCRIPT, root.getType());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests parsing synthetic code string
  @Test
  public void testParseSyntheticCode_validJs_returnsAstRoot() {
    Node root = compiler.parseSyntheticCode("synthetic.js", "var y = 20;");
    assertNotNull(root);
    assertEquals(Token.SCRIPT, root.getType());
  }

  // Tests CodeBuilder line, column tracking, and append behavior
  @Test
  public void testCodeBuilder_appendAndMetrics_tracksLineAndColumn() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    cb.append("first line\n");
    cb.append("second line");

    assertEquals(1, cb.getLineIndex());
    assertEquals(11, cb.getColumnIndex());
    assertEquals(22, cb.getLength());
    assertTrue(cb.endsWith("line"));
    assertFalse(cb.endsWith("first"));

    cb.reset();
    assertEquals(0, cb.getLength());
    assertEquals(1, cb.getLineIndex());
  }

  // Tests CodeBuilder endsWith boundary cases
  @Test
  public void testCodeBuilder_endsWith_boundaryConditions() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    assertFalse(cb.endsWith("any"));
    cb.append("abc");
    assertTrue(cb.endsWith("c"));
    assertTrue(cb.endsWith("abc"));
    assertFalse(cb.endsWith("abcd"));
  }

  // Tests intermediate state saving and restoring
  @Test
  public void testGetStateAndSetState_validState_restoresStateSuccessfully() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");
    compiler.init(new JSSourceFile[] { extern }, new JSSourceFile[] { input }, options);
    compiler.parseInputs();

    Compiler.IntermediateState state = compiler.getState();
    assertNotNull(state);

    Compiler freshCompiler = new Compiler();
    freshCompiler.disableThreads();
    freshCompiler.init(new JSSourceFile[] { extern }, new JSSourceFile[] { input }, options);
    freshCompiler.setState(state);

    assertEquals(compiler.externsRoot, freshCompiler.externsRoot);
    assertEquals(compiler.jsRoot, freshCompiler.jsRoot);
  }

  // Tests language mode checking for ECMASCRIPT5
  @Test
  public void testAcceptEcmaScript5_variousLanguageModes_returnsExpected() {
    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT3);
    compiler.initOptions(options);
    assertFalse(compiler.acceptEcmaScript5());

    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
    compiler.initOptions(options);
    assertTrue(compiler.acceptEcmaScript5());

    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5_STRICT);
    compiler.initOptions(options);
    assertTrue(compiler.acceptEcmaScript5());
  }
}