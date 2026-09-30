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
  public void testRemoveCodeAfterReturn_unreachableCode_removed() {
    test("function f() { return; var x = 1; }", "function f() { var x; }");
  }

  // Tests removal of statements without side effects when removeNoOpStatements is true
  @Test
  public void testRemoveNoOpStatements_enabled_removesPureStatements() {
    removeNoOpStatements = true;
    test("var x = 1; true; false; x;", "var x = 1;");
  }

  // Tests preservation of statements without side effects when removeNoOpStatements is false
  @Test
  public void testRemoveNoOpStatements_disabled_retainsPureStatements() {
    removeNoOpStatements = false;
    testSame("var x = 1; true; false; x;");
  }

  // Tests removal of a useless return at the end of a function
  @Test
  public void testTryRemoveUnconditionalBranching_uselessReturn_removed() {
    test("function f() { var x = 1; return; }", "function f() { var x = 1; }");
  }

  // Tests that a return with a return value expression is not removed
  @Test
  public void testTryRemoveUnconditionalBranching_returnWithValue_retained() {
    testSame("function f() { return 1; }");
  }

  // Tests removal of useless break inside a loop
  @Test
  public void testTryRemoveUnconditionalBranching_uselessBreak_removed() {
    test("while (true) { var x = 1; break; }", "while (true) { var x = 1; break; }");
  }

  // Tests removal of useless continue inside a loop
  @Test
  public void testTryRemoveUnconditionalBranching_uselessContinue_removed() {
    test("while (true) { var x = 1; continue; }", "while (true) { var x = 1; }");
  }

  // Tests cascading removal of useless branches
  @Test
  public void testTryRemoveUnconditionalBranching_cascadedBranches_removed() {
    test("function f() { if (x) { return; } return; }", "function f() { if (x) {} }");
  }

  // Tests that unreachable DO loop is preserved due to initial iteration execution
  @Test
  public void testRemoveDeadExprStatementSafely_unreachableDoLoop_retained() {
    testSame("function f() { return; do { var x = 1; } while (false); }");
  }

  // Tests that empty blocks are handled safely without errors
  @Test
  public void testRemoveDeadExprStatementSafely_emptyBlock_handledSafely() {
    testSame("function f() { {} }");
  }

  // Tests try-catch block preservation and finally adjustment
  @Test
  public void testRemoveDeadExprStatementSafely_tryCatchBlock_handledSafely() {
    testSame("try { var x = 1; } catch (e) { var y = 2; }");
  }

  // Tests try-finally block where catch is absent
  @Test
  public void testRemoveDeadExprStatementSafely_tryFinallyBlock_handledSafely() {
    testSame("try { var x = 1; } finally { var y = 2; }");
  }

  // Tests that function definitions inside dead code are handled
  @Test
  public void testVisit_functionInDeadCode_handledSafely() {
    test("function f() { return; function g() {} }", "function f() { function g() {} }");
  }

  // Tests variable redeclaration hoisting inside removed branches
  @Test
  public void testRemoveDeadExprStatementSafely_varDeclarationInDeadBranch_hoisted() {
    test("if (false) { var a = 1; }", "var a;");
  }

  // Tests labeled break removal when branching to following statement
  @Test
  public void testTryRemoveUnconditionalBranching_labeledBreak_removed() {
    test("LABEL: { var x = 1; break LABEL; }", "LABEL: { var x = 1; }");
  }

  // Tests switch statement break removal when safe
  @Test
  public void testTryRemoveUnconditionalBranching_switchBreak_retainedWhenNecessary() {
    testSame("switch (x) { case 1: var a = 1; break; case 2: var b = 2; }");
  }

  // Tests nested if-else blocks with returns
  @Test
  public void testTryRemoveUnconditionalBranching_nestedIfReturns_simplified() {
    test("function f() { if (a) { if (b) { return; } } return; }",
         "function f() { if (a) { if (b) {} } }");
  }

  // Tests removal of unreachable code following a throw statement
  @Test
  public void testRemoveCodeAfterThrow_unreachableCode_removed() {
    test("function f() { throw 'error'; var x = 1; }", "function f() { throw 'error'; var x; }");
  }

  // Tests removal of useless break in the last case of a switch statement
  @Test
  public void testTryRemoveUnconditionalBranching_lastSwitchCaseBreak_removed() {
    test("switch (x) { case 1: var a = 1; break; }", "switch (x) { case 1: var a = 1; }");
  }

  // Tests removal of useless continue targeting labeled loop
  @Test
  public void testTryRemoveUnconditionalBranching_labeledContinue_removed() {
    test("LOOP: while (true) { continue LOOP; }", "LOOP: while (true) {}");
  }

  // Tests removal of code following a break inside a loop
  @Test
  public void testRemoveCodeAfterBreak_unreachableCode_removed() {
    test("while (true) { break; var x = 1; }", "while (true) { break; var x; }");
  }

  // Tests removal of code following a continue inside a loop
  @Test
  public void testRemoveCodeAfterContinue_unreachableCode_removed() {
    test("while (true) { continue; var x = 1; }", "while (true) { continue; var x; }");
  }

  // Tests removal of let and const declarations in unreachable code (not hoisted)
  @Test
  public void testRemoveDeadCode_blockScopedDeclarations_completelyRemoved() {
    test("function f() { return; let x = 1; const y = 2; }", "function f() {}");
  }

  // Tests removal of unreachable class declaration in dead code
  @Test
  public void testRemoveDeadCode_classDeclaration_completelyRemoved() {
    test("function f() { return; class C {} }", "function f() {}");
  }

  // Tests unreachable code after if-else branch where both branches return
  @Test
  public void testRemoveCodeAfterIfElseBothReturn_unreachableCode_removed() {
    test("function f() { if (x) return; else return; var a = 1; }",
         "function f() { if (x) return; else return; var a; }");
  }

  // Tests dead code following an infinite loop
  @Test
  public void testRemoveCodeAfterInfiniteLoop_unreachableCode_removed() {
    test("for (;;) {} var x = 1;", "for (;;) {} var x;");
  }

  // Tests handling of unreachable try-catch block
  @Test
  public void testRemoveDeadCode_unreachableTryCatch_varsHoisted() {
    test("function f() { return; try { var x = 1; } catch (e) { var y = 2; } }",
         "function f() { var x; var y; }");
  }
}