package com.google.javascript.jscomp;

import org.junit.Test;

public class UnreachableCodeEliminationTest extends CompilerTestCase {
  private boolean removeNoOpStatements = true;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    removeNoOpStatements = true;
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new UnreachableCodeElimination(compiler, removeNoOpStatements);
  }

  @Override
  protected int getNumRepetitions() {
    return 2;
  }

  // Tests that empty function body is unchanged
  @Test
  public void testProcess_emptyFunction_noChange() {
    testSame("function f() {}");
  }

  // Tests removal of dead code following return statement
  @Test
  public void testProcess_codeAfterReturn_removesDeadCode() {
    test("function f() { return; var x = 1; }", "function f() { return; }");
  }

  // Tests removal of unreachable branch in if statement
  @Test
  public void testProcess_unreachableIfBranch_removesDeadCode() {
    test("function f() { if (false) { var x = 1; } }", "function f() {}");
  }

  // Tests removal of useless return at the end of function
  @Test
  public void testProcess_uselessReturnAtEndOfFunction_removesReturn() {
    test("function f() { a(); return; }", "function f() { a(); }");
  }

  // Tests preserving return with return value at end of function
  @Test
  public void testProcess_returnWithValue_preservesReturn() {
    testSame("function f() { return 1; }");
  }

  // Tests removal of useless continue at end of loop body
  @Test
  public void testProcess_uselessContinueInLoop_removesContinue() {
    test("while (x) { a(); continue; }", "while (x) { a(); }");
  }

  // Tests preserving necessary break in switch statement
  @Test
  public void testProcess_breakInSwitch_preservesBreak() {
    testSame("switch (x) { case 1: a(); break; default: b(); }");
  }

  // Tests removal of no-op statements without side effects
  @Test
  public void testProcess_noOpStatements_removesStatements() {
    test("function f() { 1 + 1; true; }", "function f() {}");
  }

  // Tests preserving statements with side effects
  @Test
  public void testProcess_sideEffectStatements_preservesStatements() {
    testSame("function f() { a(); b = 1; }");
  }

  // Tests break inside finally block in loop (regression test for Issue 127)
  @Test
  public void testProcess_breakInTryFinallyInLoop_doesNotThrowException() {
    testSame("while (x) { try { a(); } finally { break; } }");
  }

  // Tests return in try and return in finally (regression test for Issue 127)
  @Test
  public void testProcess_returnInTryFinally_doesNotThrowException() {
    testSame("function f() { try { return 1; } finally { return 2; } }");
  }

  // Tests return in try with side effect in finally
  @Test
  public void testProcess_tryFinallyWithSideEffect_preservesFinally() {
    testSame("function f() { try { return; } finally { a(); } }");
  }

  // Tests try catch block preservation
  @Test
  public void testProcess_tryCatchBlock_preservesCatch() {
    testSame("try { a(); } catch (e) { b(); }");
  }

  // Tests unreachable do-while loop preservation
  @Test
  public void testProcess_unreachableDoWhile_preservesDoLoop() {
    testSame("function f() { return; do { a(); } while (true); }");
  }

  // Tests dead var declaration without initial values
  @Test
  public void testProcess_deadVarWithoutInit_ignoresDeadVar() {
    testSame("function f() { return; var x; }");
  }

  // Tests expressions inside for-in loop header
  @Test
  public void testProcess_forInHeader_preservesLoop() {
    testSame("for (var x in y) { a(); }");
  }

  // Tests removeNoOpStatements disabled flag
  @Test
  public void testProcess_removeNoOpDisabled_preservesNoOp() {
    removeNoOpStatements = false;
    testSame("function f() { 1 + 1; }");
  }

  // Tests removal of dead code following throw statement
  @Test
  public void testProcess_codeAfterThrow_removesDeadCode() {
    test("function f() { throw 'error'; var x = 1; a(); }", "function f() { throw 'error'; }");
  }

  // Tests removal of dead code following infinite loop
  @Test
  public void testProcess_codeAfterInfiniteLoop_removesDeadCode() {
    test("function f() { while (true) { a(); } var x = 1; }", "function f() { while (true) { a(); } }");
  }

  // Tests removal of empty block statements
  @Test
  public void testProcess_emptyBlocks_removesEmptyBlocks() {
    test("function f() { {} a(); {} }", "function f() { a(); }");
  }

  // Tests removal of redundant empty statements (semicolons)
  @Test
  public void testProcess_emptyStatements_removesSemicolons() {
    test("function f() { ; a(); ; }", "function f() { a(); }");
  }

  // Tests removal of labeled blocks without breaks
  @Test
  public void testProcess_unusedLabel_removesLabel() {
    test("function f() { L: { a(); } }", "function f() { a(); }");
  }

  // Tests labeled break statement
  @Test
  public void testProcess_labeledBreak_eliminatesDeadCodeAfterBreak() {
    test("function f() { L: { a(); break L; b(); } }", "function f() { a(); }");
  }

  // Tests dead code following break in while loop
  @Test
  public void testProcess_codeAfterBreakInLoop_removesDeadCode() {
    test("while (true) { break; a(); }", "while (true) { break; }");
  }

  // Tests dead code following continue in loop
  @Test
  public void testProcess_codeAfterContinueInLoop_removesDeadCode() {
    test("while (x) { continue; a(); }", "while (x) { }");
  }

  // Tests dead code in switch cases after unconditional return
  @Test
  public void testProcess_switchCaseWithDeadCode_removesDeadCode() {
    test("switch (x) { case 1: return; a(); case 2: b(); }", "switch (x) { case 1: return; case 2: b(); }");
  }

  // Tests removal of if-else when both branches return and following code is dead
  @Test
  public void testProcess_bothBranchesReturn_removesSubsequentCode() {
    test("function f() { if (x) { return 1; } else { return 2; } a(); }",
         "function f() { if (x) { return 1; } else { return 2; } }");
  }

  // Tests removal of empty try block with catch and finally
  @Test
  public void testProcess_emptyTryCatchFinally_removesNoOp() {
    test("function f() { try {} catch (e) {} finally {} }", "function f() {}");
  }

  // Tests dead code inside for-loop update statement when unreachable
  @Test
  public void testProcess_forLoopWithUnreachableBody_removesDeadCode() {
    test("for (var i = 0; false; i++) { a(); }", "var i = 0;");
  }
}