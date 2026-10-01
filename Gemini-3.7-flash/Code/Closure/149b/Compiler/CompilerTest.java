package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Level;

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

  // Tests CodeBuilder text appending, line/column tracking, and reset behavior
  @Test
  public void testCodeBuilder_appendAndReset_tracksLineAndColumnCorrectly() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    cb.append("hello\nworld");

    assertEquals("hello\nworld", cb.toString());
    assertEquals(11, cb.getLength());
    assertEquals(1, cb.getLineIndex());
    assertEquals(5, cb.getColumnIndex());
    assertTrue(cb.endsWith("world"));
    assertFalse(cb.endsWith("hello"));

    cb.reset();
    assertEquals("", cb.toString());
    assertEquals(0, cb.getLength());
    assertEquals(1, cb.getLineIndex());
  }

  // Tests getSourceLine with valid and invalid line numbers
  @Test
  public void testGetSourceLine_boundaryLineNumbers_returnsExpected() {
    assertNull(compiler.getSourceLine("nonexistent.js", -1));
    assertNull(compiler.getSourceLine("nonexistent.js", 0));
    assertNull(compiler.getSourceLine("nonexistent.js", 1));
  }

  // Tests getSourceRegion with valid and invalid line numbers
  @Test
  public void testGetSourceRegion_boundaryLineNumbers_returnsExpected() {
    assertNull(compiler.getSourceRegion("nonexistent.js", -1));
    assertNull(compiler.getSourceRegion("nonexistent.js", 0));
    assertNull(compiler.getSourceRegion("nonexistent.js", 1));
  }

  // Tests unique name ID generator and reset mechanism
  @Test
  public void testGetUniqueNameIdSupplier_andReset_generatesSequentialIds() {
    Supplier<String> supplier = compiler.getUniqueNameIdSupplier();
    assertEquals("0", supplier.get());
    assertEquals("1", supplier.get());
    assertEquals("2", supplier.get());

    compiler.resetUniqueNameId();
    assertEquals("0", supplier.get());
  }

  // Tests normalized flag state transitions
  @Test
  public void testNormalizedState_toggle_updatesCorrectly() {
    assertFalse(compiler.isNormalized());

    compiler.setNormalized();
    assertTrue(compiler.isNormalized());

    compiler.setUnnormalized();
    assertFalse(compiler.isNormalized());
  }

  // Tests RegExp global references tracking flag
  @Test
  public void testHasRegExpGlobalReferences_setAndGet_updatesCorrectly() {
    assertTrue(compiler.hasRegExpGlobalReferences());

    compiler.setHasRegExpGlobalReferences(false);
    assertFalse(compiler.hasRegExpGlobalReferences());

    compiler.setHasRegExpGlobalReferences(true);
    assertTrue(compiler.hasRegExpGlobalReferences());
  }

  // Tests setErrorManager with null argument
  @Test(expected = NullPointerException.class)
  public void testSetErrorManager_null_throwsException() {
    compiler.setErrorManager(null);
  }

  // Tests setPassConfig with null argument
  @Test(expected = NullPointerException.class)
  public void testSetPassConfig_null_throwsException() {
    compiler.setPassConfig(null);
  }

  // Tests setPassConfig when already assigned
  @Test(expected = IllegalStateException.class)
  public void testSetPassConfig_alreadyAssigned_throwsException() {
    PassConfig passConfig = compiler.getPassConfig();
    compiler.setPassConfig(passConfig);
  }

  // Tests parseTestCode returns AST root
  @Test
  public void testParseTestCode_validJavaScript_returnsScriptNode() {
    Node root = compiler.parseTestCode("var a = 1; function foo() { return a; }");
    assertNotNull(root);
    assertEquals(Token.SCRIPT, root.getType());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests compile on valid single source input
  @Test
  public void testCompile_singleInput_compilesSuccessfully() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = 10;");

    Result result = compiler.compile(extern, input, options);
    assertTrue(result.success);
    assertEquals(0, result.errors.length);
    assertEquals(0, compiler.getErrorCount());

    String source = compiler.toSource();
    assertTrue(source.contains("var x=10"));
  }

  // Tests compile with multiple inputs and toSourceArray
  @Test
  public void testToSourceArray_multipleInputs_returnsArrayOfSources() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input1 = JSSourceFile.fromCode("input1.js", "var x = 1;");
    JSSourceFile input2 = JSSourceFile.fromCode("input2.js", "var y = 2;");

    compiler.disableThreads();
    Result result = compiler.compile(
        new JSSourceFile[]{extern},
        new JSSourceFile[]{input1, input2},
        options);

    assertTrue(result.success);
    String[] sources = compiler.toSourceArray();
    assertNotNull(sources);
    assertEquals(2, sources.length);
    assertTrue(sources[0].contains("var x=1"));
    assertTrue(sources[1].contains("var y=2"));
  }

  // Tests init with duplicate JS inputs reports error
  @Test
  public void testInit_duplicateInputs_reportsDuplicateInputError() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input1 = JSSourceFile.fromCode("same.js", "var a = 1;");
    JSSourceFile input2 = JSSourceFile.fromCode("same.js", "var b = 2;");

    compiler.init(
        new JSSourceFile[]{extern},
        new JSSourceFile[]{input1, input2},
        options);

    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_DUPLICATE_INPUT", compiler.getErrors()[0].getType().key);
  }

  // Tests init with duplicate extern inputs reports error
  @Test
  public void testInit_duplicateExternInputs_reportsDuplicateExternError() {
    JSSourceFile extern1 = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile extern2 = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");

    compiler.init(
        new JSSourceFile[]{extern1, extern2},
        new JSSourceFile[]{input},
        options);

    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_DUPLICATE_EXTERN_INPUT", compiler.getErrors()[0].getType().key);
  }

  // Tests initModules with empty module list reports error
  @Test
  public void testInitModules_emptyModuleList_reportsEmptyModuleError() {
    List<JSSourceFile> externs = Lists.newArrayList();
    List<JSModule> modules = Lists.newArrayList();

    compiler.initModules(externs, modules, options);
    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_EMPTY_MODULE_LIST_ERROR", compiler.getErrors()[0].getType().key);
  }

  // Tests initModules with single empty module fills with placeholder
  @Test
  public void testInitModules_emptySingleModule_fillsPlaceholder() {
    List<JSSourceFile> externs = Lists.newArrayList();
    JSModule module = new JSModule("mod");
    List<JSModule> modules = Lists.newArrayList(module);

    compiler.initModules(externs, modules, options);
    assertFalse(compiler.hasErrors());
    assertEquals(1, module.getInputs().size());
  }

  // Tests state capture and restoration
  @Test
  public void testGetStateAndSetState_restoresState() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");

    compiler.init(new JSSourceFile[]{extern}, new JSSourceFile[]{input}, options);
    compiler.setNormalized();

    Compiler.IntermediateState state = compiler.getState();
    assertNotNull(state);

    Compiler newCompiler = new Compiler();
    newCompiler.init(new JSSourceFile[]{extern}, new JSSourceFile[]{input}, options);
    newCompiler.setState(state);
    assertTrue(newCompiler.isNormalized());
  }

  // Tests custom PrintStream constructor initializes error manager
  @Test
  public void testConstructor_withPrintStream_initializesPrinter() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    Compiler customCompiler = new Compiler(ps);

    customCompiler.initOptions(options);
    assertNotNull(customCompiler.getErrorManager());
    assertEquals(0, customCompiler.getErrorCount());
  }

  // Tests CodeBuilder endsWith edge cases and breakline helper
  @Test
  public void testCodeBuilder_endsWithAndBreakline() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    cb.append("foo");
    assertTrue(cb.endsWith("foo"));
    assertTrue(cb.endsWith("o"));
    assertFalse(cb.endsWith("foobar"));
    assertFalse(cb.endsWith("bar"));

    cb.breakline();
    assertEquals("foo\n", cb.toString());
  }

  // Tests toSource given an explicit AST Node
  @Test
  public void testToSource_withNode_producesSourceCode() {
    Node script = compiler.parseTestCode("var a = 1;");
    String src = compiler.toSource(script);
    assertNotNull(src);
    assertTrue(src.contains("var a=1"));
  }

  // Tests compile failure on syntax error
  @Test
  public void testCompile_syntaxError_reportsError() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = ;");

    Result result = compiler.compile(extern, input, options);
    assertFalse(result.success);
    assertTrue(compiler.hasErrors());
    assertTrue(compiler.getErrorCount() > 0);
  }

  // Tests getSourceLine on compiled source file lines
  @Test
  public void testGetSourceLine_existingInput_returnsLinesCorrectly() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;\nvar b = 2;\nvar c = 3;");

    compiler.compile(extern, input, options);
    assertEquals("var a = 1;", compiler.getSourceLine("input.js", 1));
    assertEquals("var b = 2;", compiler.getSourceLine("input.js", 2));
    assertEquals("var c = 3;", compiler.getSourceLine("input.js", 3));
    assertNull(compiler.getSourceLine("input.js", 4));
  }

  // Tests getSourceRegion on compiled source file
  @Test
  public void testGetSourceRegion_existingInput_returnsRegion() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;\nvar b = 2;\nvar c = 3;");

    compiler.compile(extern, input, options);
    Region region = compiler.getSourceRegion("input.js", 2);
    assertNotNull(region);
    assertEquals(1, region.getBeginningLineNumber());
    assertEquals(3, region.getEndingLineNumber());
  }

  // Tests type registry and diagnostic groups initialization
  @Test
  public void testGetTypeRegistryAndDiagnosticGroups_notNull() {
    assertNotNull(compiler.getTypeRegistry());
    assertNotNull(compiler.getDiagnosticGroups());
  }

  // Tests setting global logging level
  @Test
  public void testSetLoggingLevel_executesWithoutError() {
    Compiler.setLoggingLevel(Level.FINE);
  }

  // Tests node equality helper for testing
  @Test
  public void testAreNodesEqualForTesting_comparesNodeTrees() {
    Node n1 = compiler.parseTestCode("var a = 1;");
    Node n2 = compiler.parseTestCode("var a = 1;");
    Node n3 = compiler.parseTestCode("var b = 2;");

    assertTrue(compiler.areNodesEqualForTesting(n1, n2));
    assertFalse(compiler.areNodesEqualForTesting(n1, n3));
  }

  // Tests compile with multiple modules and toSource per module
  @Test
  public void testCompile_withModules_succeedsAndGeneratesSourcePerModule() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1.js", "var m1_var = 1;"));
    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2.js", "var m2_var = 2;"));
    m2.addDependency(m1);

    Result result = compiler.compile(
        new JSSourceFile[]{extern},
        new JSModule[]{m1, m2},
        options);

    assertTrue(result.success);
    assertTrue(compiler.toSource(m1).contains("var m1_var=1"));
    assertTrue(compiler.toSource(m2).contains("var m2_var=2"));
  }
}