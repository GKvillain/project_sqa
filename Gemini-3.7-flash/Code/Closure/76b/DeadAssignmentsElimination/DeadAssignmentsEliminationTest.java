package com.google.javascript.jscomp;

import org.junit.Test;

public class DeadAssignmentsEliminationTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new DeadAssignmentsElimination(compiler);
  }

  // Tests basic dead assignment elimination in a local scope
  @Test
  public void testDeadAssignment_unusedVariable_eliminatesAssignment() {
    test("function f() { var a; a = 1; }",
         "function f() { var a; 1; }");
  }

  // Tests sequential assignments where the first assignment is overwritten
  @Test
  public void testDeadAssignment_reassignedBeforeRead_eliminatesFirstAssignment() {
    test("function f() { var a; a = 1; a = 2; return a; }",
         "function f() { var a; 1; a = 2; return a; }");
  }

  // Tests identity assignment removal (e.g., a = a)
  @Test
  public void testIdentityAssignment_sameVariable_replacesWithRhs() {
    test("function f() { var a; a = a; }",
         "function f() { var a; a; }");
  }

  // Tests increment operator removal when the result is unused
  @Test
  public void testIncrement_unused_replacesWithVoidZero() {
    test("function f() { var a = 0; a++; }",
         "function f() { var a = 0; void 0; }");
  }

  // Tests decrement operator removal when the result is unused
  @Test
  public void testDecrement_unused_replacesWithVoidZero() {
    test("function f() { var a = 0; a--; }",
         "function f() { var a = 0; void 0; }");
  }

  // Tests compound assignment operators (e.g., +=)
  @Test
  public void testCompoundAssignment_unused_replacesWithBinaryOp() {
    test("function f() { var a = 0; a += 1; }",
         "function f() { var a = 0; a + 1; }");
  }

  // Tests global scope assignments are not eliminated
  @Test
  public void testGlobalScope_assignmentsNotEliminated() {
    testSame("var a; a = 1;");
  }

  // Tests that local variables escaping to inner functions are preserved
  @Test
  public void testEscapedLocals_innerFunction_preservesAssignment() {
    testSame("function f() { var a; function g() { return a; } a = 1; }");
  }

  // Tests logical OR expression where LHS assignment may be read on RHS or later (Defects4J Closure 76)
  @Test
  public void testLogicalOr_assignInLhs_variableReadLater_preservesAssign() {
    testSame("function f(x) { var a; (a = x) || (a = 1); return a; }");
  }

  // Tests logical AND expression where LHS assignment may be read on RHS or later (Defects4J Closure 76)
  @Test
  public void testLogicalAnd_assignInLhs_variableReadLater_preservesAssign() {
    testSame("function f(x) { var a; (a = x) && (a = 1); return a; }");
  }

  // Tests hook (ternary) expression where LHS assignment is read in condition/branches (Defects4J Closure 76)
  @Test
  public void testHook_assignInCondition_variableReadLater_preservesAssign() {
    testSame("function f(x, y) { var a; (a = x) ? (a = y) : 0; return a; }");
  }

  // Tests return statement with logical OR condition (Defects4J Closure 76)
  @Test
  public void testReturn_logicalOr_preservesAssign() {
    testSame("function f(x) { var a; return (a = x) || (a = 1); }");
  }

  // Tests return statement with logical AND condition (Defects4J Closure 76)
  @Test
  public void testReturn_logicalAnd_preservesAssign() {
    testSame("function f(x) { var a; return (a = x) && (a = 1); }");
  }

  // Tests dead assignment inside hook expression condition
  @Test
  public void testHook_deadAssignmentInCondition_eliminatesAssignment() {
    test("function f() { var a; (a = 1) ? 0 : 0; }",
         "function f() { var a; 1 ? 0 : 0; }");
  }

  // Tests assignments in loop conditions
  @Test
  public void testWhileLoop_assignmentInCondition_preservesAssign() {
    testSame("function f() { var a; while (a = 1) { return a; } }");
  }

  // Tests assignments in switch statement condition
  @Test
  public void testSwitch_assignmentInExpression_preservesAssign() {
    testSame("function f() { var a; switch (a = 1) { case 1: return a; } }");
  }

  // Tests unused parameter assignments
  @Test
  public void testParameterAssignment_unused_eliminatesAssignment() {
    test("function f(x) { x = 1; }",
         "function f(x) { 1; }");
  }

  // Tests assignment with side-effect RHS maintains the expression evaluation
  @Test
  public void testDeadAssignment_withSideEffects_preservesCall() {
    test("function f() { var a; a = g(); }",
         "function f() { var a; g(); }");
  }

  // Tests comma operator dead assignments
  @Test
  public void testCommaOperator_deadAssignment_eliminatesAssignment() {
    test("function f() { var a; a = 1, a = 2; return a; }",
         "function f() { var a; 1, a = 2; return a; }");
  }

  // Tests for-loop initializer and condition dead assignments
  @Test
  public void testForLoop_deadAssignmentInInit_eliminatesAssignment() {
    test("function f() { var a; for (a = 1; false; ) {} }",
         "function f() { var a; for (1; false; ) {} }");
  }

  // Tests do-while loop assignment preservation
  @Test
  public void testDoWhileLoop_assignmentInBodyReadInCondition() {
    testSame("function f() { var a; do { a = 1; } while (a); }");
  }

  // Tests try-catch-finally block dead assignments
  @Test
  public void testTryCatchFinally_deadAssignmentInTry() {
    test("function f() { var a; try { a = 1; } catch (e) { a = 2; } a = 3; return a; }",
         "function f() { var a; try { 1; } catch (e) { 2; } a = 3; return a; }");
  }

  // Tests prefix increment/decrement removal when unused
  @Test
  public void testPrefixIncrementDecrement_unused_replacesWithVoidZero() {
    test("function f() { var a = 0; ++a; --a; }",
         "function f() { var a = 0; void 0; void 0; }");
  }

  // Tests conditional branches where only one path reassigns
  @Test
  public void testIfElse_assignmentUsedInOneBranch() {
    testSame("function f(cond) { var a = 1; if (cond) { return a; } }");
  }
}