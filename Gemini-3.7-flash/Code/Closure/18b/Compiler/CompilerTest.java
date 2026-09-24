package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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

  // Tests basic compilation of valid JS source code
  @Test
  public void testCompile_validCode_returnsSuccessResult() {
    SourceFile extern = SourceFile.fromCode("externs.js", "var window;");
    SourceFile input = SourceFile.fromCode("input.js", "var x = 1 + 1;");

    Result result = compiler.compile(extern, input, options);

    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
    assertNotNull(compiler.toSource());
  }

  // Tests compilation with dependency management enabled (bug 18 regression check)
  @Test
  public void testCompile_manageDependenciesWithoutClosurePass_reordersInputsCorrectly() {
    options.setDependencyOptions(new DependencyOptions()
        .setDependencySorting(true)
        .setDependencyPruning(true)
        .setEntryPoints(ImmutableList.of("entry")));

    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile dep = SourceFile.fromCode("dep.js", "goog.provide('dep'); var d = 1;");
    SourceFile entry = SourceFile.fromCode("entry.js", "goog.provide('entry'); goog.require('dep'); var e = d;");

    Result result = compiler.compile(Lists.newArrayList(extern), Lists.newArrayList(entry, dep), options);

    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests compilation with multiple modules
  @Test
  public void testCompile_modules_buildsSourcePerModule() {
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    JSModule m1 = new JSModule("m1");
    m1.add(SourceFile.fromCode("m1.js", "var a = 1;"));

    JSModule m2 = new JSModule("m2");
    m2.add(SourceFile.fromCode("m2.js", "var b = 2;"));
    m2.addDependency(m1);

    Result result = compiler.compileModules(
        Lists.newArrayList(extern),
        Lists.newArrayList(m1, m2),
        options);

    assertTrue(result.success);
    assertEquals(2, compiler.getModuleGraph().getModuleCount());
    assertNotNull(compiler.toSource(m1));
    assertNotNull(compiler.toSource(m2));
  }

  // Tests compilation reporting error for duplicate input names
  @Test
  public void testInitInputsByIdMap_duplicateInputs_reportsError() {
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input1 = SourceFile.fromCode("same.js", "var a = 1;");
    SourceFile input2 = SourceFile.fromCode("same.js", "var b = 2;");

    compiler.init(
        Lists.newArrayList(extern),
        Lists.newArrayList(input1, input2),
        options);

    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
    assertEquals(Compiler.DUPLICATE_INPUT.key, compiler.getErrors()[0].getType().key);
  }

  // Tests compilation with empty root module when multiple modules are provided
  @Test
  public void testInitModules_emptyRootModule_reportsError() {
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    JSModule m1 = new JSModule("root");
    JSModule m2 = new JSModule("child");
    m2.add(SourceFile.fromCode("child.js", "var x = 1;"));
    m2.addDependency(m1);

    compiler.initModules(
        Lists.newArrayList(extern),
        Lists.newArrayList(m1, m2),
        options);

    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
  }

  // Tests parsing test code and synthetic code directly
  @Test
  public void testParseTestCode_validJs_returnsAstNode() {
    Node root = compiler.parseTestCode("var a = 10;");

    assertNotNull(root);
    assertTrue(root.isScript());
    assertNotNull(compiler.getInput(new InputId("[testcode]")));
  }

  // Tests parse synthetic code generates unique synthetic inputs
  @Test
  public void testParseSyntheticCode_validJs_returnsScriptNode() {
    Node node1 = compiler.parseSyntheticCode("var x = 1;");
    Node node2 = compiler.parseSyntheticCode("var y = 2;");

    assertNotNull(node1);
    assertNotNull(node2);
    assertFalse(node1.getInputId().equals(node2.getInputId()));
  }

  // Tests toSource output formatting with single input
  @Test
  public void testToSourceArray_multipleInputs_returnsArrayOfSources() {
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile in1 = SourceFile.fromCode("in1.js", "var a = 1;");
    SourceFile in2 = SourceFile.fromCode("in2.js", "var b = 2;");

    compiler.compile(Lists.newArrayList(extern), Lists.newArrayList(in1, in2), options);
    String[] sources = compiler.toSourceArray();

    assertNotNull(sources);
    assertEquals(2, sources.length);
    assertTrue(sources[0].contains("var a=1"));
    assertTrue(sources[1].contains("var b=2"));
  }

  // Tests CodeBuilder helper functionality
  @Test
  public void testCodeBuilder_appendAndMetrics_tracksLineAndColumn() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    cb.append("var x = 1;\nvar y = 2;\n");

    assertEquals(2, cb.getLineIndex());
    assertEquals(0, cb.getColumnIndex());
    assertTrue(cb.endsWith("\n"));
    assertTrue(cb.getLength() > 0);

    cb.reset();
    assertEquals(0, cb.getLength());
    assertEquals(2, cb.getLineIndex());
  }

  // Tests progress boundary values
  @Test
  public void testSetProgress_valuesWithinAndOutsideRange_clampsCorrectly() {
    compiler.setProgress(0.5);
    assertEquals(0.5, compiler.getProgress(), 0.001);

    compiler.setProgress(1.5);
    assertEquals(1.0, compiler.getProgress(), 0.001);

    compiler.setProgress(-0.5);
    assertEquals(0.0, compiler.getProgress(), 0.001);
  }

  // Tests State saving and restoring for intermediate compile states
  @Test
  public void testGetAndSetState_restoresCompilerState() {
    SourceFile extern = SourceFile.fromCode("externs.js", "var ext;");
    SourceFile input = SourceFile.fromCode("input.js", "var a = 1;");

    compiler.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    compiler.parseInputs();

    Compiler.IntermediateState state = compiler.getState();
    assertNotNull(state);

    Compiler newCompiler = new Compiler();
    newCompiler.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    newCompiler.setState(state);

    assertNotNull(newCompiler.getRoot());
    assertEquals(compiler.getRoot().getChildCount(), newCompiler.getRoot().getChildCount());
  }

  // Tests dynamic creation and removal of extern inputs
  @Test
  public void testNewExternInputAndRemoveExternInput_modifiesExternsList() {
    compiler.init(
        Lists.newArrayList(SourceFile.fromCode("externs.js", "")),
        Lists.newArrayList(SourceFile.fromCode("input.js", "var x = 1;")),
        options);
    compiler.parseInputs();

    int initialExternsCount = compiler.getExternsInOrder().size();
    CompilerInput createdExtern = compiler.newExternInput("custom_extern.js");

    assertNotNull(createdExtern);
    assertEquals(initialExternsCount + 1, compiler.getExternsInOrder().size());
    assertNotNull(compiler.getInput(createdExtern.getInputId()));

    compiler.removeExternInput(createdExtern.getInputId());
    assertEquals(initialExternsCount, compiler.getExternsInOrder().size());
    assertNull(compiler.getInput(createdExtern.getInputId()));
  }

  // Tests custom PrintStream constructor and ErrorManager setup
  @Test
  public void testConstructor_withPrintStream_initializesErrorManager() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(out);
    Compiler customCompiler = new Compiler(ps);
    customCompiler.initOptions(options);

    assertNotNull(customCompiler.getErrorManager());
    assertEquals(0, customCompiler.getErrorCount());
  }

  // Tests unique name ID generation and reset
  @Test
  public void testUniqueNameIdSupplier_generatesSequentialIdsAndResets() {
    com.google.common.base.Supplier<String> supplier = compiler.getUniqueNameIdSupplier();

    assertEquals("0", supplier.get());
    assertEquals("1", supplier.get());
    assertEquals("2", supplier.get());

    compiler.resetUniqueNameId();
    assertEquals("0", supplier.get());
  }

  // Tests compiler release version and release date bundle metadata
  @Test
  public void testGetReleaseVersionAndDate_returnsNonEmptyStrings() {
    String version = Compiler.getReleaseVersion();
    String date = Compiler.getReleaseDate();

    assertNotNull(version);
    assertFalse(version.isEmpty());
    assertNotNull(date);
    assertFalse(date.isEmpty());
  }

  // Tests disableThreads flag
  @Test
  public void testDisableThreads_compilesSuccessfullyInCurrentThread() {
    compiler.disableThreads();
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("input.js", "var x = 42;");

    Result result = compiler.compile(extern, input, options);

    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests source line retrieval for parsed source files
  @Test
  public void testGetSourceLine_validAndInvalidLineNumbers_returnsExpectedLineOrNull() {
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("input.js", "var line1 = 1;\nvar line2 = 2;");

    compiler.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);

    assertEquals("var line1 = 1;", compiler.getSourceLine("input.js", 1));
    assertEquals("var line2 = 2;", compiler.getSourceLine("input.js", 2));
    assertNull(compiler.getSourceLine("input.js", 0));
    assertNull(compiler.getSourceLine("input.js", -1));
    assertNull(compiler.getSourceLine("nonexistent.js", 1));
  }
}