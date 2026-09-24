package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

public class UnreachableCodeEliminationTest extends CompilerTestCase {
  private boolean removeNoOpStatements = true;

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new UnreachableCodeElimination(compiler, removeNoOpStatements);
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    removeNoOpStatements = true;
  }

  // Tests removal of code following a return statement
  @Test
  public void testProcess_unreachableAfterReturn_removesDeadCode() {
    test("function foo() { return 1; var x = 2; }",
         "function foo() { return 1; var x; }");
  }

  // Tests removal of side-effect-free expression statements when enabled
  @Test
  public void testProcess_noOpStatementsEnabled_removesStatements() {
    test("var x = 1; x; true; 1 + 1;", "var x = 1;");
  }

  // Tests preservation of side-effect-free expression statements when disabled
  @Test
  public void testProcess_noOpStatementsDisabled_keepsStatements() {
    removeNoOpStatements = false;
    testSame("var x = 1; x; true; 1 + 1;");
  }

  // Tests removal of useless return without value at the end of a function
  @Test
  public void testProcess_uselessReturnAtEndOfFunction_removesReturn() {
    test("function foo() { var x = 1; return; }",
         "function foo() { var x = 1; }");
  }

  // Tests that return with a return value is not removed
  @Test
  public void testProcess_returnWithValue_keepsReturn() {
    testSame("function foo() { return 1; }");
  }

  // Tests that break inside try-finally is preserved to maintain control flow
  @Test
  public void testProcess_breakInsideTryFinally_keepsBreak() {
    testSame("while (x) { try { break; } finally { y(); } }");
  }

  // Tests that return inside try-finally is preserved
  @Test
  public void testProcess_returnInsideTryFinally_keepsReturn() {
    testSame("function foo() { try { return; } finally { bar(); } }");
  }

  // Tests that continue inside try-finally is preserved
  @Test
  public void testProcess_continueInsideTryFinally_keepsContinue() {
    testSame("while (x) { try { continue; } finally { y(); } }");
  }

  // Tests removal of useless break statement targeting the end of a loop
  @Test
  public void testProcess_uselessBreak_removesBreak() {
    test("while (x) { a(); break; }",
         "while (x) { a(); }");
  }

  // Tests removal of useless continue statement at the end of a loop body
  @Test
  public void testProcess_uselessContinue_removesContinue() {
    test("while (x) { a(); continue; }",
         "while (x) { a(); }");
  }

  // Tests that do-while unreachable node is safely preserved
  @Test
  public void testProcess_unreachableDoWhile_preservesDoStructure() {
    testSame("function foo() { return; do { x(); } while (true); }");
  }

  // Tests that for-in headers are preserved even if expression appears side-effect free
  @Test
  public void testProcess_forInHeader_preservesHeader() {
    testSame("for (var prop in obj) {}");
  }

  // Tests removal of unreachable catch block while preserving try structure
  @Test
  public void testProcess_unreachableCatch_handlesFinally() {
    test("try { return 1; } catch (e) { alert(e); }",
         "try { return 1; } finally {}");
  }

  // Tests that dead variable declarations without initializers are safely ignored
  @Test
  public void testProcess_deadVarDeclarationWithoutInit_keepsDeclaration() {
    testSame("function foo() { return; var x; }");
  }

  // Tests cascaded useless branching removal
  @Test
  public void testProcess_cascadedBranches_removesAllUselessBranches() {
    test("switch (x) { case 1: break; default: break; }",
         "switch (x) { case 1: default: }");
  }

  // Tests empty block and statement handling
  @Test
  public void testProcess_emptyBlock_preservesBlock() {
    testSame("function foo() { ; {} }");
  }
}