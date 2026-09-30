package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

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
    options.initConverter();
  }

  // Tests basic compilation of a simple script
  @Test
  public void testCompile_simpleInput_success() {
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("input.js", "var a = 1;");
    Result result = compiler.compile(extern, input, options);

    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests dependency management and dependency sorting with closure pass (Bug 18 target)
  @Test
  public void testCompile_dependencySorting_reordersInputs() {
    options.setClosurePass(true);
    options.setManageDependencies(true);

    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile fileB = SourceFile.fromCode("b.js", "goog.provide('b'); var b = 1;");
    SourceFile fileA = SourceFile.fromCode("a.js", "goog.require('b'); var a = b;");

    List<SourceFile> inputs = Lists.newArrayList(fileA, fileB);
    Result result = compiler.compile(Lists.newArrayList(extern), inputs, options);

    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());
    List<CompilerInput> order = compiler.getInputsInOrder();
    assertEquals(2, order.size());
    assertEquals("b.js", order.get(0).getName());
    assertEquals("a.js", order.get(1).getName());
  }

  // Tests dependency management reporting missing entry point error
  @Test
  public void testCompile_missingEntryPoint_reportsError() {
    options.setClosurePass(true);
    options.setManageDependencies(ImmutableList.of("non.existent.entry"));

    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("input.js", "goog.provide('foo');");

    Result result = compiler.compile(extern, input, options);
    assertFalse(result.success);
    assertTrue(compiler.getErrorCount() > 0);
  }

  // Tests circular dependency error reporting
  @Test
  public void testCompile_circularDependency_reportsError() {
    options.setClosurePass(true);
    options.setManageDependencies(true);

    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile fileA = SourceFile.fromCode("a.js", "goog.provide('a'); goog.require('b');");
    SourceFile fileB = SourceFile.fromCode("b.js", "goog.provide('b'); goog.require('a');");

    Result result = compiler.compile(
        Lists.newArrayList(extern),
        Lists.newArrayList(fileA, fileB),
        options);

    assertFalse(result.success);
    assertTrue(compiler.getErrorCount() > 0);
  }

  // Tests duplicate input error detection in initInputsByIdMap
  @Test
  public void testInit_duplicateInputs_reportsError() {
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input1 = SourceFile.fromCode("input.js", "var a = 1;");
    SourceFile input2 = SourceFile.fromCode("input.js", "var b = 2;");

    compiler.init(
        Lists.newArrayList(extern),
        Lists.newArrayList(input1, input2),
        options);

    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
  }

  // Tests hoisting inputs with @externs annotation into externs list
  @Test
  public void testParseInputs_hoistExterns_movesToExterns() {
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile annotatedExtern = SourceFile.fromCode("ext.js", "/** @externs */ var extVar;");
    SourceFile input = SourceFile.fromCode("input.js", "var a = extVar;");

    compiler.init(
        Lists.newArrayList(extern),
        Lists.newArrayList(annotatedExtern, input),
        options);
    compiler.parseInputs();

    assertEquals(2, compiler.getExternsInOrder().size());
    assertEquals(1, compiler.getInputsInOrder().size());
    assertEquals("input.js", compiler.getInputsInOrder().get(0).getName());
  }

  // Tests hoisting inputs with @nocompile annotation out of input list
  @Test
  public void testParseInputs_hoistNoCompile_removesInput() {
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile noCompile = SourceFile.fromCode("ignore.js", "/** @nocompile */ var ignore = 1;");
    SourceFile input = SourceFile.fromCode("input.js", "var a = 1;");

    compiler.init(
        Lists.newArrayList(extern),
        Lists.newArrayList(noCompile, input),
        options);
    compiler.parseInputs();

    assertEquals(1, compiler.getInputsInOrder().size());
    assertEquals("input.js", compiler.getInputsInOrder().get(0).getName());
  }

  // Tests progress boundary clamping
  @Test
  public void testSetProgress_boundaryValues_clampsCorrectly() {
    compiler.setProgress(-0.5);
    assertEquals(0.0, compiler.getProgress(), 0.0001);

    compiler.setProgress(1.5);
    assertEquals(1.0, compiler.getProgress(), 0.0001);

    compiler.setProgress(0.42);
    assertEquals(0.42, compiler.getProgress(), 0.0001);
  }

  // Tests adding and getting new extern input dynamically
  @Test
  public void testNewExternInput_validName_addsSuccessfully() {
    compiler.init(
        Lists.newArrayList(SourceFile.fromCode("externs.js", "")),
        Lists.newArrayList(SourceFile.fromCode("input.js", "var a = 1;")),
        options);
    compiler.parseInputs();

    CompilerInput created = compiler.newExternInput("custom_extern.js");
    assertNotNull(created);
    assertNotNull(compiler.getInput(new InputId("custom_extern.js")));
  }

  // Tests adding duplicate extern input throws IllegalArgumentException
  @Test(expected = IllegalArgumentException.class)
  public void testNewExternInput_duplicateName_throwsException() {
    compiler.init(
        Lists.newArrayList(SourceFile.fromCode("externs.js", "")),
        Lists.newArrayList(SourceFile.fromCode("input.js", "var a = 1;")),
        options);
    compiler.parseInputs();

    compiler.newExternInput("custom.js");
    compiler.newExternInput("custom.js");
  }

  // Tests adding new source ast incrementally
  @Test
  public void testAddNewSourceAst_newAst_returnsTrue() {
    compiler.init(
        Lists.newArrayList(SourceFile.fromCode("externs.js", "")),
        Lists.newArrayList(SourceFile.fromCode("input.js", "var a = 1;")),
        options);
    compiler.parseInputs();

    JsAst newAst = new JsAst(SourceFile.fromCode("added.js", "var b = 2;"));
    boolean added = compiler.addNewSourceAst(newAst);
    assertTrue(added);
    assertNotNull(compiler.getInput(new InputId("added.js")));
  }

  // Tests adding duplicate source ast throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testAddNewSourceAst_duplicateInput_throwsException() {
    compiler.init(
        Lists.newArrayList(SourceFile.fromCode("externs.js", "")),
        Lists.newArrayList(SourceFile.fromCode("input.js", "var a = 1;")),
        options);
    compiler.parseInputs();

    JsAst duplicateAst = new JsAst(SourceFile.fromCode("input.js", "var a = 2;"));
    compiler.addNewSourceAst(duplicateAst);
  }

  // Tests parsing test code string directly
  @Test
  public void testParseTestCode_validString_returnsNode() {
    Node root = compiler.parseTestCode("function test() { return 10; }");
    assertNotNull(root);
    assertTrue(root.isScript());
  }

  // Tests parseSyntheticCode with file name
  @Test
  public void testParseSyntheticCode_withFileName_returnsNode() {
    Node root = compiler.parseSyntheticCode("synthetic.js", "var x = 123;");
    assertNotNull(root);
    assertEquals("synthetic.js", root.getSourceFileName());
  }

  // Tests toSource serialization with CodeBuilder
  @Test
  public void testToSource_compiledCode_outputsJsString() {
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("input.js", "var x = 10;");
    compiler.compile(extern, input, options);

    String source = compiler.toSource();
    assertNotNull(source);
    assertTrue(source.contains("var x=10"));
  }

  // Tests module source output toSource(JSModule)
  @Test
  public void testToSource_module_returnsCombinedSource() {
    JSModule module = new JSModule("mod1");
    module.add(SourceFile.fromCode("m1.js", "var x = 1;"));
    module.add(SourceFile.fromCode("m2.js", "var y = 2;"));

    SourceFile extern = SourceFile.fromCode("externs.js", "");
    compiler.compileModules(
        Lists.newArrayList(extern),
        Lists.newArrayList(module),
        options);

    String source = compiler.toSource(module);
    assertNotNull(source);
    assertTrue(source.contains("var x=1"));
    assertTrue(source.contains("var y=2"));
  }

  // Tests disableThreads setting
  @Test
  public void testDisableThreads_runsExecutionWithoutThread() {
    compiler.disableThreads();
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("input.js", "var z = 3;");
    Result result = compiler.compile(extern, input, options);

    assertTrue(result.success);
    assertEquals("var z=3;", compiler.toSource().trim());
  }

  // Tests saving and restoring intermediate compiler state
  @Test
  public void testGetAndSetState_restoresState() {
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("input.js", "var a = 1;");
    compiler.init(
        Lists.newArrayList(extern),
        Lists.newArrayList(input),
        options);
    compiler.parseInputs();

    Compiler.IntermediateState state = compiler.getState();
    assertNotNull(state);

    compiler.setState(state);
    assertEquals(1, compiler.getInputsInOrder().size());
  }

  // Tests getReleaseVersion and getReleaseDate return non-null values
  @Test
  public void testGetReleaseVersionAndDate_returnsValue() {
    String version = Compiler.getReleaseVersion();
    String date = Compiler.getReleaseDate();
    assertNotNull(version);
    assertNotNull(date);
  }
}