package com.google.javascript.jscomp;

import com.google.common.collect.Lists;
import com.google.javascript.rhino.InputId;
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

  // Tests normal compilation of valid JavaScript source code
  @Test
  public void testCompile_validCode_successAndGeneratesSource() {
    SourceFile extern = SourceFile.fromCode("externs.js", "function alert(x) {}");
    SourceFile input = SourceFile.fromCode("input.js", "var x = 1 + 2; alert(x);");

    Result result = compiler.compile(extern, input, options);

    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());
    String source = compiler.toSource();
    assertNotNull(source);
    assertTrue(source.contains("alert"));
  }

  // Tests compilation error reporting when given invalid syntax
  @Test
  public void testCompile_syntaxError_reportsError() {
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("input.js", "var x = ;");

    Result result = compiler.compile(extern, input, options);

    assertFalse(result.success);
    assertTrue(compiler.getErrorCount() > 0);
  }

  // Tests dependency management sorting behavior with manageDependencies
  @Test
  public void testCompile_dependencyManagement_ordersInputsCorrectly() {
    options.setClosurePass(true);
    options.setManageClosureDependencies(true);

    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile f1 = SourceFile.fromCode("f1.js", "goog.provide('app'); goog.require('lib'); var a = lib;");
    SourceFile f2 = SourceFile.fromCode("f2.js", "goog.provide('lib'); var lib = 42;");
    SourceFile f3 = SourceFile.fromCode("f3.js", "goog.provide('entry'); goog.require('app');");

    options.dependencyOptions.setEntryPoints(Lists.newArrayList("entry"));

    Result result = compiler.compile(Lists.newArrayList(extern), Lists.newArrayList(f1, f2, f3), options);

    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());
    List<CompilerInput> orderedInputs = compiler.getInputsInOrder();
    assertEquals("lib", orderedInputs.get(0).getProvides().iterator().next());
    assertEquals("app", orderedInputs.get(1).getProvides().iterator().next());
    assertEquals("entry", orderedInputs.get(2).getProvides().iterator().next());
  }

  // Tests missing entry point error during dependency management
  @Test
  public void testCompile_missingEntryPoint_reportsError() {
    options.setClosurePass(true);
    options.setManageClosureDependencies(true);
    options.dependencyOptions.setEntryPoints(Lists.newArrayList("nonExistingEntry"));

    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("input.js", "goog.provide('existing');");

    Result result = compiler.compile(Lists.newArrayList(extern), Lists.newArrayList(input), options);

    assertFalse(result.success);
    assertTrue(compiler.getErrorCount() > 0);
    assertEquals("JSC_MISSING_ENTRY_ERROR", compiler.getErrors()[0].getType().key);
  }

  // Tests progress boundary clamping (min, max, intermediate)
  @Test
  public void testSetProgress_boundaryValues_clampedCorrectly() {
    compiler.setProgress(-0.5);
    assertEquals(0.0, compiler.getProgress(), 0.0001);

    compiler.setProgress(0.5);
    assertEquals(0.5, compiler.getProgress(), 0.0001);

    compiler.setProgress(1.5);
    assertEquals(1.0, compiler.getProgress(), 0.0001);
  }

  // Tests compiling with threads disabled
  @Test
  public void testCompile_disableThreads_compilesSuccessfully() {
    compiler.disableThreads();
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("input.js", "var y = 10;");

    Result result = compiler.compile(extern, input, options);

    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests CodeBuilder append, line counting, column tracking, and resets
  @Test
  public void testCodeBuilder_appendAndMetrics_tracksLinesAndColumns() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    cb.append("var a = 1;\nvar b = 2;\n");

    assertEquals(2, cb.getLineIndex());
    assertEquals(0, cb.getColumnIndex());
    assertTrue(cb.endsWith("\n"));
    assertFalse(cb.endsWith("var"));

    cb.append("var c = 3;");
    assertEquals(2, cb.getLineIndex());
    assertEquals(10, cb.getColumnIndex());
    assertTrue(cb.endsWith(";"));

    cb.reset();
    assertEquals(0, cb.getLength());
    assertEquals(2, cb.getLineIndex());
  }

  // Tests areNodesEqualForInlining equivalence check on Node AST
  @Test
  public void testAreNodesEqualForInlining_plainNodes_returnsCorrectResult() {
    compiler.initCompilerOptionsIfTesting();
    Node n1 = Node.newString("foo");
    Node n2 = Node.newString("foo");
    Node n3 = Node.newString("bar");

    assertTrue(compiler.areNodesEqualForInlining(n1, n2));
    assertFalse(compiler.areNodesEqualForInlining(n1, n3));
  }

  // Tests adding and removing synthetic extern input
  @Test
  public void testNewExternInput_andRemoveExternInput_modifiesInputs() {
    SourceFile extern = SourceFile.fromCode("externs.js", "var x;");
    SourceFile input = SourceFile.fromCode("input.js", "x = 1;");
    compiler.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    compiler.parseInputs();

    CompilerInput customExtern = compiler.newExternInput("custom_extern.js");
    assertNotNull(customExtern);
    assertNotNull(compiler.getInput(customExtern.getInputId()));

    compiler.removeExternInput(customExtern.getInputId());
    assertNull(compiler.getInput(customExtern.getInputId()));
  }

  // Tests save and restore intermediate compiler state
  @Test
  public void testGetStateAndSetState_restoresStateCorrectly() {
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("input.js", "var z = 3;");
    compiler.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    compiler.parseInputs();

    Compiler.IntermediateState state = compiler.getState();
    assertNotNull(state);

    Compiler compiler2 = new Compiler();
    compiler2.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    compiler2.setState(state);

    assertNotNull(compiler2.getRoot());
  }

  // Tests error reporting when duplicate input files are supplied
  @Test
  public void testInit_duplicateInputs_reportsDuplicateInputError() {
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input1 = SourceFile.fromCode("same_name.js", "var a = 1;");
    SourceFile input2 = SourceFile.fromCode("same_name.js", "var b = 2;");

    compiler.init(Lists.newArrayList(extern), Lists.newArrayList(input1, input2), options);

    assertTrue(compiler.getErrorCount() > 0);
    assertEquals("JSC_DUPLICATE_INPUT", compiler.getErrors()[0].getType().key);
  }

  // Tests initializing with empty module list reporting error
  @Test
  public void testInitModules_emptyModuleList_reportsError() {
    List<SourceFile> externs = Lists.newArrayList(SourceFile.fromCode("externs.js", ""));
    List<JSModule> modules = Lists.newArrayList();

    compiler.initModules(externs, modules, options);

    assertTrue(compiler.getErrorCount() > 0);
    assertEquals("JSC_EMPTY_MODULE_LIST_ERROR", compiler.getErrors()[0].getType().key);
  }

  // Tests parseTestCode helper generates valid Node
  @Test
  public void testParseTestCode_validJs_returnsValidAstRoot() {
    Node root = compiler.parseTestCode("var a = 123;");
    assertNotNull(root);
    assertEquals(Token.SCRIPT, root.getType());
    assertTrue(root.hasChildren());
  }

  // Tests toSourceArray converts multiple inputs to individual source strings
  @Test
  public void testToSourceArray_multipleInputs_returnsSourceForEachInput() {
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input1 = SourceFile.fromCode("in1.js", "var a = 1;");
    SourceFile input2 = SourceFile.fromCode("in2.js", "var b = 2;");

    compiler.compile(Lists.newArrayList(extern), Lists.newArrayList(input1, input2), options);

    String[] sources = compiler.toSourceArray();
    assertEquals(2, sources.length);
    assertTrue(sources[0].contains("var a = 1"));
    assertTrue(sources[1].contains("var b = 2"));
  }

  // Tests compiler construction with custom PrintStream
  @Test
  public void testConstructor_withPrintStream_initializesProperly() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    Compiler customCompiler = new Compiler(ps);

    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("input.js", "var hello = 'world';");

    Result result = customCompiler.compile(extern, input, options);
    assertTrue(result.success);
    assertEquals(0, customCompiler.getErrorCount());
  }

  // Tests replaceIncrementalSourceAst replaces AST of an existing input
  @Test
  public void testReplaceScript_existingInput_replacesAstSuccessfully() {
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("mod.js", "var first = 1;");
    compiler.compile(Lists.newArrayList(extern), Lists.newArrayList(input), options);

    JsAst newAst = new JsAst(SourceFile.fromCode("mod.js", "var second = 2;"));
    compiler.replaceScript(newAst);

    CompilerInput updatedInput = compiler.getInput(newAst.getInputId());
    assertNotNull(updatedInput);
    String source = compiler.toSource();
    assertTrue(source.contains("second"));
  }
}