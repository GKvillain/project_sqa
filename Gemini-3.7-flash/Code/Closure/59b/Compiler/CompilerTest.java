package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler.CodeBuilder;
import com.google.javascript.jscomp.CompilerOptions.DevMode;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
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

  // Tests initOptions with default settings and checkGlobalThisLevel
  @Test
  public void testInitOptions_defaultOptions_initializesWarningsGuard() {
    options.checkGlobalThisLevel = CheckLevel.ERROR;
    compiler.initOptions(options);

    assertNotNull(compiler.getErrorManager());
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests initOptions with checkGlobalThisLevel off
  @Test
  public void testInitOptions_globalThisOff_doesNotSetErrorLevel() {
    options.checkGlobalThisLevel = CheckLevel.OFF;
    compiler.initOptions(options);

    assertEquals(0, compiler.getErrorCount());
  }

  // Tests initOptions with DiagnosticGroups.CHECK_TYPES enabled
  @Test
  public void testInitOptions_checkTypesEnabled_setsCheckTypesTrue() {
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.WARNING);
    compiler.initOptions(options);

    assertTrue(options.checkTypes);
  }

  // Tests initOptions with DiagnosticGroups.CHECK_TYPES disabled
  @Test
  public void testInitOptions_checkTypesDisabled_setsCheckTypesFalse() {
    options.checkTypes = true;
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.OFF);
    compiler.initOptions(options);

    assertFalse(options.checkTypes);
  }

  // Tests initOptions with ECMASCRIPT5_STRICT mode
  @Test
  public void testInitOptions_ecmaScript5Strict_setsEs5StrictWarningLevel() {
    options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);
    compiler.initOptions(options);

    assertEquals(LanguageMode.ECMASCRIPT5_STRICT, compiler.languageMode());
    assertTrue(compiler.acceptEcmaScript5());
  }

  // Tests setErrorManager with null argument
  @Test(expected = NullPointerException.class)
  public void testSetErrorManager_nullManager_throwsNullPointerException() {
    compiler.setErrorManager(null);
  }

  // Tests setPassConfig with null argument
  @Test(expected = NullPointerException.class)
  public void testSetPassConfig_nullPassConfig_throwsNullPointerException() {
    compiler.setPassConfig(null);
  }

  // Tests setPassConfig when already initialized
  @Test(expected = IllegalStateException.class)
  public void testSetPassConfig_alreadyAssigned_throwsIllegalStateException() {
    compiler.initOptions(options);
    PassConfig passConfig1 = compiler.getPassConfig();
    compiler.setPassConfig(passConfig1);
  }

  // Tests simple compile with single extern and single input file
  @Test
  public void testCompile_validSingleInput_succeedsWithoutErrors() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "var window;");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");

    Result result = compiler.compile(extern, input, options);

    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
    assertNotNull(compiler.getRoot());
  }

  // Tests compile detecting duplicate input files
  @Test
  public void testCompile_duplicateInput_reportsDuplicateInputError() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input1 = JSSourceFile.fromCode("input.js", "var a = 1;");
    JSSourceFile input2 = JSSourceFile.fromCode("input.js", "var b = 2;");

    Result result = compiler.compile(new JSSourceFile[] { extern },
        new JSSourceFile[] { input1, input2 }, options);

    assertFalse(result.success);
    assertTrue(compiler.getErrorCount() > 0);
  }

  // Tests compile with empty module list
  @Test
  public void testCompileModules_emptyModuleList_reportsError() {
    List<JSSourceFile> externs = new ArrayList<JSSourceFile>();
    List<JSModule> modules = new ArrayList<JSModule>();

    Result result = compiler.compileModules(externs, modules, options);

    assertFalse(result.success);
    assertTrue(compiler.getErrorCount() > 0);
  }

  // Tests compile with dependent JSModules
  @Test
  public void testCompileModules_validModules_compilesSuccessfully() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1.js", "var x = 10;"));

    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2.js", "var y = x + 1;"));
    m2.addDependency(m1);

    Result result = compiler.compile(extern, new JSModule[] { m1, m2 }, options);

    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());
    assertNotNull(compiler.getModuleGraph());
  }

  // Tests parseSyntheticCode and parseTestCode
  @Test
  public void testParseSyntheticCode_validJs_returnsAstNode() {
    Node node = compiler.parseSyntheticCode("test.js", "function foo() { return 42; }");
    assertNotNull(node);
    assertEquals(Token.SCRIPT, node.getType());
  }

  // Tests toSource on CodeBuilder and basic code generation
  @Test
  public void testToSource_simpleCode_generatesExpectedJavaScript() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "function add(a, b) { return a + b; }");

    compiler.compile(extern, input, options);
    String source = compiler.toSource();

    assertNotNull(source);
    assertTrue(source.contains("function add"));
  }

  // Tests CodeBuilder append, getLineIndex, getColumnIndex, endsWith, and reset
  @Test
  public void testCodeBuilder_appendAndReset_tracksLineAndColumnCorrectly() {
    CodeBuilder cb = new CodeBuilder();
    cb.append("var a = 1;\nvar b = 2;");

    assertEquals(1, cb.getLineIndex());
    assertEquals(10, cb.getColumnIndex());
    assertTrue(cb.endsWith(";"));
    assertFalse(cb.endsWith("\n"));
    assertEquals(21, cb.getLength());

    cb.reset();
    assertEquals(0, cb.getLength());
    assertEquals(1, cb.getLineIndex());
  }

  // Tests getSourceLine and getSourceRegion with invalid line boundary
  @Test
  public void testGetSourceLine_invalidBoundary_returnsNull() {
    assertNull(compiler.getSourceLine("unknown.js", 0));
    assertNull(compiler.getSourceLine("unknown.js", -1));
    assertNull(compiler.getSourceRegion("unknown.js", 0));
    assertNull(compiler.getSourceRegion("unknown.js", -5));
  }

  // Tests PrintStream constructor of Compiler
  @Test
  public void testCompilerConstructor_withPrintStream_initializesProperly() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    Compiler customCompiler = new Compiler(ps);

    customCompiler.initOptions(options);
    assertNotNull(customCompiler.getErrorManager());
  }

  // Tests disableThreads, hasRegExpGlobalReferences and resetUniqueNameId
  @Test
  public void testCompilerFlags_andStateReset_functionsAsExpected() {
    compiler.disableThreads();
    assertTrue(compiler.hasRegExpGlobalReferences());

    compiler.setHasRegExpGlobalReferences(false);
    assertFalse(compiler.hasRegExpGlobalReferences());

    compiler.resetUniqueNameId();
    assertEquals("0", compiler.getUniqueNameIdSupplier().get());
    assertEquals("1", compiler.getUniqueNameIdSupplier().get());
  }

  // Tests init with List interface and basic getters
  @Test
  public void testInit_withLists_initializesInputsAndExterns() {
    List<JSSourceFile> externs = Collections.singletonList(
        JSSourceFile.fromCode("externs.js", "var console;"));
    List<JSSourceFile> inputs = Collections.singletonList(
        JSSourceFile.fromCode("input.js", "console.log('hello');"));

    compiler.init(externs, inputs, options);

    assertEquals(1, compiler.getExternsForTesting().size());
    assertEquals(1, compiler.getInputsForTesting().size());
    assertNotNull(compiler.getSourceFileByName("input.js"));
    assertNotNull(compiler.getSourceFileByName("externs.js"));
    assertNull(compiler.getSourceFileByName("nonexistent.js"));
    assertEquals("console.log('hello');", compiler.getSourceLine("input.js", 1));
    assertNotNull(compiler.getSourceRegion("input.js", 1));
  }

  // Tests Compiler constructor with custom ErrorManager
  @Test
  public void testCompilerConstructor_withErrorManager_usesProvidedManager() {
    BasicErrorManager customManager = new BasicErrorManager() {
      @Override
      public void println(CheckLevel level, JSError error) {}
      @Override
      protected void printSummary() {}
    };
    Compiler customCompiler = new Compiler(customManager);
    assertSame(customManager, customCompiler.getErrorManager());
  }

  // Tests parseTestCode and node equality
  @Test
  public void testParseTestCode_andAreNodesEqualForTesting() {
    Node node1 = compiler.parseTestCode("var a = 1;");
    Node node2 = compiler.parseTestCode("var a = 1;");
    Node node3 = compiler.parseTestCode("var b = 2;");

    assertNotNull(node1);
    assertNotNull(node2);
    assertTrue(compiler.areNodesEqualForTesting(node1, node2));
    assertFalse(compiler.areNodesEqualForTesting(node1, node3));
  }

  // Tests toSource variants: toSource(Node), toSource(JSModule), toSourceArray()
  @Test
  public void testToSource_nodeAndModuleVariants_generatesExpectedStrings() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSModule m1 = new JSModule("mod1");
    m1.add(JSSourceFile.fromCode("m1.js", "var foo = 1;"));
    JSModule m2 = new JSModule("mod2");
    m2.add(JSSourceFile.fromCode("m2.js", "var bar = 2;"));

    compiler.compile(new JSSourceFile[] { extern }, new JSModule[] { m1, m2 }, options);

    String[] sourceArray = compiler.toSourceArray();
    assertNotNull(sourceArray);
    assertEquals(2, sourceArray.length);

    String[] m1Array = compiler.toSourceArray(m1);
    assertNotNull(m1Array);
    assertEquals(1, m1Array.length);

    String m1Source = compiler.toSource(m1);
    assertTrue(m1Source.contains("var foo"));

    Node root = compiler.getRoot();
    String rootSource = compiler.toSource(root);
    assertNotNull(rootSource);
  }

  // Tests reportCodeChange, hasHaltingErrors, and lifeCycleStage
  @Test
  public void testLifeCycleAndStateMethods() {
    compiler.initOptions(options);
    assertFalse(compiler.hasHaltingErrors());

    compiler.reportCodeChange();
    assertNotNull(compiler.getLifeCycleStage());
    assertFalse(compiler.isIdeMode());
    assertFalse(compiler.isTypeCheckingEnabled());
  }

  // Tests getTypeRegistry and getDefaultErrorReporter
  @Test
  public void testGetTypeRegistry_andDefaultErrorReporter_notNull() {
    JSTypeRegistry registry = compiler.getTypeRegistry();
    assertNotNull(registry);
    assertNotNull(compiler.getDefaultErrorReporter());
  }

  // Tests Tracer methods and setLoggingLevel
  @Test
  public void testTracer_andLoggingLevel_executesSafely() {
    Compiler.setLoggingLevel(Level.OFF);
    Object tracer = compiler.newTracer("testPass");
    assertNotNull(tracer);
    compiler.stopTracer(tracer, "testPass");
  }

  // Tests ensureLibraryInjected
  @Test
  public void testEnsureLibraryInjected_compilationPipeline() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");
    compiler.compile(extern, input, options);

    Node injected = compiler.ensureLibraryInjected("base");
    assertNotNull(injected);
  }
}