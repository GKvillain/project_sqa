package com.google.javascript.jscomp;

import com.google.common.collect.Lists;
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

  // Tests default initialization of compiler options
  @Test
  public void testInitOptions_defaultOptions_initializesGuardsAndPassConfig() {
    compiler.initOptions(options);
    assertNotNull(compiler.getErrorManager());
    assertNotNull(compiler.getOptions());
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests initOptions when checkTypes is explicitly enabled via DiagnosticGroups
  @Test
  public void testInitOptions_enableCheckTypesDiagnosticGroup_enablesCheckTypes() {
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.ERROR);
    compiler.initOptions(options);
    assertTrue(options.checkTypes);
  }

  // Tests initOptions when checkGlobalThis is configured
  @Test
  public void testInitOptions_checkGlobalThisLevelOn_setsWarningLevel() {
    options.checkGlobalThisLevel = CheckLevel.WARNING;
    compiler.initOptions(options);
    JSError error = JSError.make("test.js", 1, 1, CheckGlobalThis.GLOBAL_THIS);
    assertEquals(CheckLevel.WARNING, compiler.getErrorLevel(error));
  }

  // Tests initOptions with ECMASCRIPT5_STRICT mode
  @Test
  public void testInitOptions_es5Strict_setsStrictWarningLevel() {
    options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);
    compiler.initOptions(options);
    JSError error = JSError.make("test.js", 1, 1, DiagnosticGroups.ES5_STRICT.getTypes().iterator().next());
    assertEquals(CheckLevel.ERROR, compiler.getErrorLevel(error));
  }

  // Tests basic compile with source strings
  @Test
  public void testCompile_simpleSource_returnsSuccessResult() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "var window;");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = 1;");
    
    Result result = compiler.compile(extern, input, options);
    assertTrue(result.success);
    assertEquals(0, result.errors.length);
    assertNotNull(compiler.getRoot());
  }

  // Tests compilation with threads disabled
  @Test
  public void testCompile_threadsDisabled_compilesSuccessfully() {
    compiler.disableThreads();
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "function foo() { return 42; }");
    
    Result result = compiler.compile(extern, input, options);
    assertTrue(result.success);
    assertEquals("function foo(){return 42}", compiler.toSource().trim());
  }

  // Tests parseTestCode returns valid AST root node
  @Test
  public void testParseTestCode_validJs_returnsScriptNode() {
    Node node = compiler.parseTestCode("var a = 1 + 2;");
    assertNotNull(node);
    assertEquals(Token.SCRIPT, node.getType());
    assertNotNull(compiler.getInput(" [testcode] "));
  }

  // Tests parseSyntheticCode with custom name
  @Test
  public void testParseSyntheticCode_customName_registersInput() {
    Node node = compiler.parseSyntheticCode("synth.js", "var y = 2;");
    assertNotNull(node);
    assertNotNull(compiler.getInput("synth.js"));
  }

  // Tests toSource serialization with CodeBuilder
  @Test
  public void testCodeBuilder_appendAndCount_maintainsCorrectIndices() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    cb.append("var x = 1;\nvar y = 2;\n");
    assertEquals(2, cb.getLineIndex());
    assertEquals(0, cb.getColumnIndex());
    assertTrue(cb.endsWith("\n"));
    assertTrue(cb.endsWith("y = 2;\n"));
    assertFalse(cb.endsWith("x = 1;\n"));
    
    cb.reset();
    assertEquals(0, cb.getLength());
    assertEquals(2, cb.getLineIndex()); // reset keeps line count intact
  }

  // Tests toSourceArray with multiple inputs
  @Test
  public void testToSourceArray_multipleInputs_returnsIndividualSources() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile[] inputs = new JSSourceFile[] {
        JSSourceFile.fromCode("a.js", "var a = 10;"),
        JSSourceFile.fromCode("b.js", "var b = 20;")
    };
    compiler.compile(new JSSourceFile[] { extern }, inputs, options);
    String[] sources = compiler.toSourceArray();
    assertNotNull(sources);
    assertEquals(2, sources.length);
    assertTrue(sources[0].contains("a"));
    assertTrue(sources[1].contains("b"));
  }

  // Tests error reporting for empty module list
  @Test
  public void testInitModules_emptyModuleList_reportsError() {
    List<JSSourceFile> externs = Lists.newArrayList();
    List<JSModule> modules = Lists.newArrayList();
    compiler.initModules(externs, modules, options);
    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
  }

  // Tests error reporting for empty root module when multiple modules exist
  @Test
  public void testInitModules_emptyRootModuleInMultiModule_reportsError() {
    List<JSSourceFile> externs = Lists.newArrayList();
    JSModule mod1 = new JSModule("mod1");
    JSModule mod2 = new JSModule("mod2");
    mod2.add(JSSourceFile.fromCode("input2.js", "var x = 1;"));
    
    List<JSModule> modules = Lists.newArrayList(mod1, mod2);
    compiler.initModules(externs, modules, options);
    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
  }

  // Tests newExternInput and removeExternInput lifecycle
  @Test
  public void testNewAndRemoveExternInput_validExtern_addsAndRemovesSuccessfully() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a;");
    compiler.init(new JSSourceFile[] { extern }, new JSSourceFile[] { input }, options);
    compiler.parseInputs();

    CompilerInput newExt = compiler.newExternInput("custom_extern.js");
    assertNotNull(newExt);
    assertTrue(newExt.isExtern());
    assertNotNull(compiler.getInput("custom_extern.js"));

    compiler.removeExternInput("custom_extern.js");
    assertNull(compiler.getInput("custom_extern.js"));
  }

  // Tests newExternInput with conflicting name throws exception
  @Test(expected = IllegalArgumentException.class)
  public void testNewExternInput_duplicateName_throwsException() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a;");
    compiler.init(new JSSourceFile[] { extern }, new JSSourceFile[] { input }, options);
    compiler.parseInputs();

    compiler.newExternInput("externs.js");
  }

  // Tests save and restore IntermediateState
  @Test
  public void testGetAndSetState_validCompilation_restoresState() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");
    compiler.init(new JSSourceFile[] { extern }, new JSSourceFile[] { input }, options);
    compiler.parseInputs();

    Compiler.IntermediateState state = compiler.getState();
    assertNotNull(state);

    Compiler newCompiler = new Compiler();
    newCompiler.init(new JSSourceFile[] { extern }, new JSSourceFile[] { input }, options);
    newCompiler.setState(state);
    assertNotNull(newCompiler.getRoot());
  }

  // Tests node equality check for inlining with and without property ambiguation
  @Test
  public void testAreNodesEqualForInlining_nodes_returnsTrueForEquivalent() {
    compiler.initOptions(options);
    Node n1 = new Node(Token.NAME);
    n1.setString("foo");
    Node n2 = new Node(Token.NAME);
    n2.setString("foo");

    assertTrue(compiler.areNodesEqualForInlining(n1, n2));

    options.ambiguateProperties = true;
    assertTrue(compiler.areNodesEqualForInlining(n1, n2));
  }

  // Tests constructor with PrintStream error manager
  @Test
  public void testConstructor_withPrintStream_createsStreamErrorManager() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    Compiler customCompiler = new Compiler(ps);
    customCompiler.initOptions(options);
    
    assertNotNull(customCompiler.getErrorManager());
  }

  // Tests getSourceLine and getSourceRegion
  @Test
  public void testGetSourceLineAndRegion_validFile_returnsContent() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "line1\nline2\nline3");
    compiler.init(new JSSourceFile[] { extern }, new JSSourceFile[] { input }, options);

    assertEquals("line1", compiler.getSourceLine("input.js", 1));
    assertEquals("line2", compiler.getSourceLine("input.js", 2));
    assertNull(compiler.getSourceLine("input.js", 0));
    assertNull(compiler.getSourceLine("nonexistent.js", 1));

    Region region = compiler.getSourceRegion("input.js", 2);
    assertNotNull(region);
  }

  // Tests isTypeCheckingEnabled and getTypeRegistry
  @Test
  public void testGetTypeRegistry_looseTypes_createsRegistry() {
    options.checkTypes = true;
    compiler.initOptions(options);
    assertTrue(compiler.isTypeCheckingEnabled());
    assertNotNull(compiler.getTypeRegistry());
    assertNotNull(compiler.getReverseAbstractInterpreter());
  }

  // Tests language mode support methods
  @Test
  public void testAcceptEcmaScript5_differentModes_returnsExpectedBoolean() {
    options.setLanguageIn(LanguageMode.ECMASCRIPT3);
    compiler.initOptions(options);
    assertFalse(compiler.acceptEcmaScript5());

    options.setLanguageIn(LanguageMode.ECMASCRIPT5);
    compiler.initOptions(options);
    assertTrue(compiler.acceptEcmaScript5());

    options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);
    compiler.initOptions(options);
    assertTrue(compiler.acceptEcmaScript5());
  }
}