package com.google.javascript.jscomp;

import org.junit.Test;

/**
 * Tests for {@link UnreachableCodeElimination}.
 */
public class UnreachableCodeEliminationTest extends CompilerTestCase {
  private boolean removeNoOpStatements = true;

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new UnreachableCodeElimination(compiler, removeNoOpStatements);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    removeNoOpStatements = true;
  }

  // Tests removing dead code after an unconditional return statement
  @Test
  public void testRemoveDeadCode_afterReturn_removesDeadCode() {
    test("function f() { return; alert(1); }", "function f() { return; }");
  }

  // Tests removing dead code after an unconditional throw statement
  @Test
  public void testRemoveDeadCode_afterThrow_removesDeadCode() {
    test("function f() { throw 'error'; alert(1); }", "function f() { throw 'error'; }");
  }

  // Tests removing statements without side effects when removeNoOpStatements is true
  @Test
  public void testRemoveNoOp_withoutSideEffects_removesStatement() {
    test("function f() { 1; true; 'hello'; }", "function f() {}");
  }

  // Tests keeping statements with potential side effects
  @Test
  public void testRemoveNoOp_withSideEffects_keepsStatement() {
    testSame("function f() { foo(); x = 1; }");
  }

  // Tests that no-op statements are preserved when removeNoOpStatements is false
  @Test
  public void testRemoveNoOp_whenDisabled_keepsNoOpStatement() {
    removeNoOpStatements = false;
    testSame("function f() { 1; }");
  }

  // Tests removing useless return statement at the end of a function
  @Test
  public void testTryRemoveUnconditionalBranching_uselessReturn_removesReturn() {
    test("function f() { alert(1); return; }", "function f() { alert(1); }");
  }

  // Tests keeping return statement that returns a value
  @Test
  public void testTryRemoveUnconditionalBranching_returnWithValue_keepsReturn() {
    testSame("function f() { return 1; }");
  }

  // Tests removing useless break statement in a loop
  @Test
  public void testTryRemoveUnconditionalBranching_uselessBreak_removesBreak() {
    test("while (true) { alert(1); break; }", "while (true) { alert(1); break; }");
    test("switch (x) { case 1: alert(1); break; }", "switch (x) { case 1: alert(1); }");
  }

  // Tests removing useless continue statement at the end of a loop
  @Test
  public void testTryRemoveUnconditionalBranching_uselessContinue_removesContinue() {
    test("for (var i = 0; i < 10; i++) { alert(i); continue; }",
         "for (var i = 0; i < 10; i++) { alert(i); }");
  }

  // Tests cascading removal of branches
  @Test
  public void testTryRemoveUnconditionalBranching_cascadedBranches_removesUselessJumps() {
    test("function f() { if (x) { return; } else { return; } }",
         "function f() { if (x) {} else {} }");
  }

  // Tests unreachable do-while loop is not removed directly to avoid messiness
  @Test
  public void testRemoveDeadCode_doWhileLoop_preservesStructure() {
    test("function f() { return; do { alert(1); } while (false); }",
         "function f() { return; }");
  }

  // Tests unreachable code inside try-catch-finally blocks
  @Test
  public void testRemoveDeadCode_tryCatchFinally_handlesSafely() {
    test("try { alert(1); } catch (e) { alert(2); } finally { alert(3); }",
         "try { alert(1); } catch (e) { alert(2); } finally { alert(3); }");
  }

  // Tests unreachable catch block modification
  @Test
  public void testRemoveDeadCode_unreachableTryBranch_preservesFinally() {
    test("function f() { return; try { alert(1); } catch (e) { alert(2); } }",
         "function f() { return; }");
  }

  // Tests variable declarations inside dead branches are preserved for hoisting
  @Test
  public void testRemoveDeadCode_varDeclarationsInsideDeadCode_redecaresVars() {
    test("function f() { return; var x = 1; }",
         "function f() { return; var x; }");
  }

  // Tests empty block and token handling
  @Test
  public void testRemoveDeadCode_emptyBlock_handlesSafely() {
    test("function f() { ;;; }", "function f() {}");
  }

  // Tests nested functions are preserved and traversed
  @Test
  public void testRemoveDeadCode_nestedFunctions_traversesCorrectly() {
    test("function f() { function g() { return; alert(1); } g(); }",
         "function f() { function g() { return; } g(); }");
  }

  // Tests multiple returns and branching control flow
  @Test
  public void testTryRemoveUnconditionalBranching_multipleBranches_simplifies() {
    test("function f(x) { if (x) { return 1; } else { return 2; } return; }",
         "function f(x) { if (x) { return 1; } else { return 2; } }");
  }
}