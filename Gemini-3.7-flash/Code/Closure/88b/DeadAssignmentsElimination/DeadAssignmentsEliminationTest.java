package com.google.javascript.jscomp;

import org.junit.Test;

/**
 * Unit tests for {@link DeadAssignmentsElimination}.
 */
public class DeadAssignmentsEliminationTest extends CompilerTestCase {

  public DeadAssignmentsEliminationTest() {
    super();
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new DeadAssignmentsElimination(compiler);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests regression defect where assignment inside short-circuit expression is incorrectly eliminated
  @Test
  public void testIssue297_logicalOrAssignment_notEliminated() {
    testSame("function f(x){ var a; return (a = x) || (a = 0); }");
  }

  // Tests regression defect where assignment in hook conditional is read in true branch
  @Test
  public void testIssue297_hookConditionAssignment_notEliminated() {
    testSame("function f(x){ var a; return (a = x) ? a : 0; }");
  }

  // Tests regression defect where assignment is conditionally overwritten in branches
  @Test
  public void testIssue297_hookBranchAssignment_notEliminated() {
    testSame("function f(x){ var a; return (a = x) ? (a = 0) : 0; }");
  }

  // Tests eliminating a standard overwritten local variable assignment
  @Test
  public void testEliminateDeadAssignment_simpleOverwrittenVar_eliminatesFirstAssign() {
    test("function f() { var x; x = 1; x = 2; return x; }",
         "function f() { var x; 1; x = 2; return x; }");
  }

  // Tests self assignment elimination (e.g. a = a)
  @Test
  public void testEliminateDeadAssignment_selfAssignment_replacesWithRhs() {
    test("function f() { var x = 1; x = x; return x; }",
         "function f() { var x = 1; x; return x; }");
  }

  // Tests unused increment operator elimination
  @Test
  public void testEliminateDeadAssignment_deadIncrement_replacesWithVoidZero() {
    test("function f() { var x = 1; x++; return 1; }",
         "function f() { var x = 1; void 0; return 1; }");
  }

  // Tests unused decrement operator elimination
  @Test
  public void testEliminateDeadAssignment_deadDecrement_replacesWithVoidZero() {
    test("function f() { var x = 1; x--; return 1; }",
         "function f() { var x = 1; void 0; return 1; }");
  }

  // Tests compound assignment elimination (e.g. x += 2 converted to x + 2)
  @Test
  public void testEliminateDeadAssignment_deadCompoundAssign_replacesWithBinaryOp() {
    test("function f() { var x = 1; x += 2; return 1; }",
         "function f() { var x = 1; x + 2; return 1; }");
  }

  // Tests that global scope assignments are not eliminated
  @Test
  public void testGlobalScope_assignmentsNotEliminated() {
    testSame("var x = 1; x = 2;");
  }

  // Tests that variables in functions containing inner closures are not eliminated
  @Test
  public void testInnerClosure_escapedVariablesNotEliminated() {
    testSame("function f() { var x = 1; function g() { return x; } x = 2; return g(); }");
  }

  // Tests dead assignment in for-loop condition / increment
  @Test
  public void testEliminateDeadAssignment_inForLoop_eliminatesDeadAssign() {
    test("function f() { var x; for (x = 1; false; x = 2) {} return 1; }",
         "function f() { var x; for (1; false; 2) {} return 1; }");
  }

  // Tests assignment inside switch statement
  @Test
  public void testEliminateDeadAssignment_inSwitchStatement_eliminatesDeadAssign() {
    test("function f(x) { var a; switch (a = x) { case 1: a = 2; } return a; }",
         "function f(x) { var a; switch (a = x) { case 1: 2; } return a; }");
  }

  // Tests assignment inside while loop condition
  @Test
  public void testEliminateDeadAssignment_inWhileCondition_eliminatesDeadAssign() {
    test("function f() { var x = 0; while (x = 1) { break; } return 1; }",
         "function f() { var x = 0; while (1) { break; } return 1; }");
  }

  // Tests chained dead assignments
  @Test
  public void testEliminateDeadAssignment_chainedAssignments_eliminatesAll() {
    test("function f() { var x, y; x = y = 1; return 1; }",
         "function f() { var x, y; 1; return 1; }");
  }

  // Tests that live variable read across control flow branches is preserved
  @Test
  public void testLiveVariable_usedInReturn_preserved() {
    testSame("function f(x) { var a = 1; if (x) { a = 2; } return a; }");
  }

  // Tests prefix increment and decrement dead assignment elimination
  @Test
  public void testEliminateDeadAssignment_prefixIncrementDecrement() {
    test("function f() { var x = 1; ++x; return 1; }",
         "function f() { var x = 1; void 0; return 1; }");
    test("function f() { var x = 1; --x; return 1; }",
         "function f() { var x = 1; void 0; return 1; }");
  }

  // Tests function parameter dead assignment elimination
  @Test
  public void testEliminateDeadAssignment_functionParameter() {
    test("function f(param) { param = 1; return 2; }",
         "function f(param) { 1; return 2; }");
  }

  // Tests do-while loop dead assignment elimination
  @Test
  public void testEliminateDeadAssignment_inDoWhileLoop() {
    test("function f() { var x; do { x = 1; } while (false); return 2; }",
         "function f() { var x; do { 1; } while (false); return 2; }");
  }

  // Tests various compound assignment operators elimination
  @Test
  public void testEliminateDeadAssignment_variousCompoundOps() {
    test("function f() { var x = 1; x -= 2; return 1; }",
         "function f() { var x = 1; x - 2; return 1; }");
    test("function f() { var x = 1; x *= 2; return 1; }",
         "function f() { var x = 1; x * 2; return 1; }");
    test("function f() { var x = 1; x /= 2; return 1; }",
         "function f() { var x = 1; x / 2; return 1; }");
    test("function f() { var x = 1; x %= 2; return 1; }",
         "function f() { var x = 1; x % 2; return 1; }");
    test("function f() { var x = 1; x &= 2; return 1; }",
         "function f() { var x = 1; x & 2; return 1; }");
    test("function f() { var x = 1; x |= 2; return 1; }",
         "function f() { var x = 1; x | 2; return 1; }");
    test("function f() { var x = 1; x ^= 2; return 1; }",
         "function f() { var x = 1; x ^ 2; return 1; }");
    test("function f() { var x = 1; x <<= 2; return 1; }",
         "function f() { var x = 1; x << 2; return 1; }");
    test("function f() { var x = 1; x >>= 2; return 1; }",
         "function f() { var x = 1; x >> 2; return 1; }");
    test("function f() { var x = 1; x >>>= 2; return 1; }",
         "function f() { var x = 1; x >>> 2; return 1; }");
  }

  // Tests try-catch-finally control flow
  @Test
  public void testEliminateDeadAssignment_inTryCatchFinally() {
    test("function f() { var x; try { x = 1; } catch (e) { x = 2; } return 3; }",
         "function f() { var x; try { 1; } catch (e) { 2; } return 3; }");
  }

  // Tests comma operator dead assignments
  @Test
  public void testEliminateDeadAssignment_inCommaExpression() {
    test("function f() { var x; (x = 1), (x = 2); return x; }",
         "function f() { var x; 1, (x = 2); return x; }");
  }

  // Tests anonymous function expressions
  @Test
  public void testEliminateDeadAssignment_inFunctionExpression() {
    test("var g = function() { var x = 1; x = 2; return x; };",
         "var g = function() { var x = 1; 1; x = 2; return x; };");
  }
}