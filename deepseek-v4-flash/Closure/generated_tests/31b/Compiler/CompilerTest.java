package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.common.collect.Lists;
import com.google.javascript.jscomp.Compiler.CodeBuilder;
import com.google.javascript.jscomp.Compiler.IntermediateState;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;

import org.junit.Before;
import org.junit.Test;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * JUnit 4 test class for Compiler, targeting Defects4J bug 31b.
 * Focuses on branch coverage and defect detection.
 */
public class CompilerTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
  }

  // Tests that a simple compilation with valid externs and source succeeds
  @Test
  public void testCompile_simpleValidInput_returnsSuccess() {
    SourceFile extern = SourceFile.fromCode("externs.js", "var window;");
    SourceFile input = SourceFile.fromCode("input.js", "var x = 1;");
    Result result = compiler.compile(extern, input, options);
    assertTrue("Compilation should succeed", result.success);
    assertEquals("Should have no errors", 0, compiler.getErrorCount());
  }

  // Tests that compilation with an empty module list reports an error
  @Test
  public void testInit_emptyModuleList_reportsError() {
    List<JSSourceFile> externs = Lists.newArrayList();
    List<JSModule> modules = new ArrayList<JSModule>();
    compiler.initModules(externs, modules, options);
    assertTrue("Should have errors", compiler.getErrorCount() > 0);
  }

  // Tests that the root module containing zero inputs with multiple modules reports an error
  @Test
  public void testInit_emptyRootModuleWithMultipleModules_reportsError() {
    List<JSSourceFile> externs = Lists.newArrayList();
    JSModule rootModule = new JSModule("root");
    JSModule otherModule = new JSModule("other");
    otherModule.add(SourceFile.fromCode("other.js", "var a = 1;"));
    List<JSModule> modules = Lists.newArrayList(rootModule, otherModule);
    compiler.initModules(externs, modules, options);
    assertTrue("Should have errors for empty root module", compiler.getErrorCount() > 0);
  }

  // Tests that initOptions with checkTypes enabled via DiagnosticGroups sets checkTypes to true
  @Test
  public void testInitOptions_checkTypesEnabledViaDiagnosticGroups_setsCheckTypesTrue() {
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.ERROR);
    compiler.initOptions(options);
    assertTrue("checkTypes should be true", options.checkTypes);
  }

  // Tests the createFillFileName method with a valid module name
  @Test
  public void testCreateFillFileName_validModuleName_returnsBracketedName() {
    String result = Compiler.createFillFileName("testModule");
    assertEquals("Should be [testModule]", "[testModule]", result);
  }

  // Tests that fillEmptyModules creates a fill file for an empty module
  @Test
  public void testFillEmptyModules_emptyModule_createsFillFile() {
    List<JSModule> modules = new ArrayList<JSModule>();
    JSModule module = new JSModule("emptyModule");
    modules.add(module);
    Compiler.fillEmptyModules(modules);
    assertEquals("Should have 1 input after fill", 1, module.getInputs().size());
  }

  // Tests that initInputsByIdMap detects duplicate extern inputs
  @Test
  public void testInitInputsByIdMap_duplicateExtern_reportsError() {
    List<SourceFile> externs = Lists.newArrayList(
        SourceFile.fromCode("duplicate.js", "var a;"),
        SourceFile.fromCode("duplicate.js", "var b;")
    );
    List<JSModule> modules = Lists.newArrayList(new JSModule("m"));
    modules.get(0).add(SourceFile.fromCode("input.js", "var c;"));
    compiler.initModules(externs, modules, options);
    assertTrue("Should report duplicate extern error", compiler.getErrorCount() > 0);
  }

  // Tests that compile with hasErrors after init returns getResult without calling compile
  @Test
  public void testCompile_hasErrorsAfterInit_returnsResultWithoutCompile() {
    List<JSSourceFile> externs = Lists.newArrayList();
    List<JSModule> modules = new ArrayList<JSModule>();
    Result result = compiler.compileModules(externs, modules, options);
    assertFalse("Compilation should not succeed", result.success);
  }

  // Tests the CodeBuilder append method with a simple string
  @Test
  public void testCodeBuilderAppend_simpleString_updatesCorrectly() {
    CodeBuilder cb = new CodeBuilder();
    cb.append("hello");
    assertEquals("Should be 'hello'", "hello", cb.toString());
    assertEquals("Line index should be 0", 0, cb.getLineIndex());
    assertEquals("Column index should be 5", 5, cb.getColumnIndex());
  }

  // Tests CodeBuilder append with newlines
  @Test
  public void testCodeBuilderAppend_stringWithNewline_updatesLineCount() {
    CodeBuilder cb = new CodeBuilder();
    cb.append("line1\nline2");
    assertEquals("Line index should be 1", 1, cb.getLineIndex());
    assertEquals("Column index should be 5", 5, cb.getColumnIndex());
  }

  // Tests CodeBuilder endsWith method
  @Test
  public void testCodeBuilderEndsWith_validSuffix_returnsTrue() {
    CodeBuilder cb = new CodeBuilder();
    cb.append("test");
    assertTrue("Should end with 'st'", cb.endsWith("st"));
  }

  // Tests CodeBuilder reset method
  @Test
  public void testCodeBuilderReset_afterAppend_clearsBuffer() {
    CodeBuilder cb = new CodeBuilder();
    cb.append("some content");
    cb.reset();
    assertEquals("Should be empty after reset", "", cb.toString());
  }

  // Tests setProgress with valid values within 0.0 to 1.0
  @Test
  public void testSetProgress_validValue_setsProgress() {
    compiler.setProgress(0.5);
    assertEquals("Progress should be 0.5", 0.5, compiler.getProgress(), 0.0001);
  }

  // Tests setProgress with value greater than 1.0 clamps to 1.0
  @Test
  public void testSetProgress_valueAboveOne_clampsToOne() {
    compiler.setProgress(2.0);
    assertEquals("Progress should be 1.0", 1.0, compiler.getProgress(), 0.0001);
  }

  // Tests setProgress with negative value clamps to 0.0
  @Test
  public void testSetProgress_negativeValue_clampsToZero() {
    compiler.setProgress(-1.0);
    assertEquals("Progress should be 0.0", 0.0, compiler.getProgress(), 0.0001);
  }

  // Tests that hasRegExpGlobalReferences returns true by default
  @Test
  public void testHasRegExpGlobalReferences_default_returnsTrue() {
    assertTrue("Default should be true", compiler.hasRegExpGlobalReferences());
  }

  // Tests setHasRegExpGlobalReferences toggles correctly
  @Test
  public void testSetHasRegExpGlobalReferences_false_setsToFalse() {
    compiler.setHasRegExpGlobalReferences(false);
    assertFalse("Should be false after set", compiler.hasRegExpGlobalReferences());
  }

  // Tests getErrors returns non-null array even when no errors
  @Test
  public void testGetErrors_noErrors_returnsEmptyArray() {
    assertNotNull("Should not be null", compiler.getErrors());
    assertEquals("Should be empty", 0, compiler.getErrors().length);
  }

  // Tests getWarnings returns non-null array even when no warnings
  @Test
  public void testGetWarnings_noWarnings_returnsEmptyArray() {
    assertNotNull("Should not be null", compiler.getWarnings());
    assertEquals("Should be empty", 0, compiler.getWarnings().length);
  }

  // Tests parse method with valid source file returns non-null node
  @Test
  public void testParse_validSourceFile_returnsNode() {
    Node node = compiler.parse(SourceFile.fromCode("test.js", "var x = 1;"));
    assertNotNull("Parsed node should not be null", node);
    assertTrue("Root should be a script", node.isScript());
  }

  // Tests disableThreads sets useThreads to false (indirectly via runInCompilerThread)
  @Test
  public void testDisableThreads_afterCall_usesCurrentThread() {
    compiler.disableThreads();
    // This test verifies the method can be called without exception and
    // subsequent compile uses current thread (no thread creation)
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("input.js", "var a = 1;");
    Result result = compiler.compile(extern, input, options);
    assertNotNull("Result should not be null", result);
  }

  // Tests getOptions returns non-null after initOptions
  @Test
  public void testGetOptions_afterInitOptions_returnsOptions() {
    compiler.initOptions(options);
    assertNotNull("Options should not be null", compiler.getOptions());
  }

  // Tests toSource returns empty string when jsRoot is null
  @Test
  public void testToSource_noJsRoot_returnsEmptyString() {
    // Compiler initialized but not parsed yet
    String source = compiler.toSource();
    assertEquals("Should be empty string", "", source);
  }
}