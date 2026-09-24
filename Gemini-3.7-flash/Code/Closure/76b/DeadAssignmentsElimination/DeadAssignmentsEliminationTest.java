package com.google.javascript.jscomp;

import org.junit.Test;

/**
 * Unit tests for DeadAssignmentsElimination.
 */
public class DeadAssignmentsEliminationTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new DeadAssignmentsElimination(compiler);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests null input on process method throwing exception
  @Test(expected = RuntimeException.class)
  public void testProcess_nullExterns_throwsException() {
    DeadAssignmentsElimination dae = new DeadAssignmentsElimination(new Compiler());
    dae.process(null, new com.google.javascript.rhino.Node(0));
  }

  // Tests null root node on process method throwing exception
  @Test(expected = RuntimeException.class)
  public void testProcess_nullRoot_throwsException() {
    DeadAssignmentsElimination dae = new DeadAssignmentsElimination(new Compiler());
    dae.process(new com.google.javascript.rhino.Node(0), null);
  }

  // Tests that dead local assignment before reassignment is removed
  @Test
  public void testProcess_simpleDeadAssign_removesAssign() {
    test("function f() { var x; x = 1; x = 2; return x; }",
         "function f() { var x; 1; x = 2; return x; }");
  }

  // Tests self-assignment elimination
  @Test
  public void testProcess_selfAssignment_removesAssign() {
    test("function f() { var x; x = x; return x; }",
         "function f() { var x; x; return x; }");
  }

  // Tests compound assignment replacement
  @Test
  public void testProcess_compoundAssign_replacesWithBinaryOp() {
    test("function f() { var x = 1; x += 2; }",
         "function f() { var x = 1; x + 2; }");
  }

  // Tests increment replacement when dead
  @Test
  public void testProcess_incrementDead_replacesWithVoidZero() {
    test("function f() { var x = 1; x++; }",
         "function f() { var x = 1; void 0; }");
  }

  // Tests decrement replacement when dead
  @Test
  public void testProcess_decrementDead_replacesWithVoidZero() {
    test("function f() { var x = 1; x--; }",
         "function f() { var x = 1; void 0; }");
  }

  // Tests global variables are not eliminated
  @Test
  public void testProcess_globalScope_doesNotEliminate() {
    testSame("var x = 1; x = 2;");
  }

  // Tests scopes containing inner functions are preserved
  @Test
  public void testProcess_innerFunctionScope_doesNotEliminate() {
    testSame("function f() { var x = 1; function g() { return x; } x = 2; }");
  }

  // Tests if condition dead assignment elimination
  @Test
  public void testProcess_ifConditionDeadAssign_removesAssign() {
    test("function f() { var x; if (x = 1) { return 2; } }",
         "function f() { var x; if (1) { return 2; } }");
  }

  // Tests while condition dead assignment elimination
  @Test
  public void testProcess_whileConditionDeadAssign_removesAssign() {
    test("function f() { var x; while (x = 1) { return 2; } }",
         "function f() { var x; while (1) { return 2; } }");
  }

  // Tests do-while condition dead assignment elimination
  @Test
  public void testProcess_doWhileConditionDeadAssign_removesAssign() {
    test("function f() { var x; do { return 2; } while (x = 1); }",
         "function f() { var x; do { return 2; } while (1); }");
  }

  // Tests for loop condition dead assignment elimination
  @Test
  public void testProcess_forConditionDeadAssign_removesAssign() {
    test("function f() { var x; for (; x = 1;) { return 2; } }",
         "function f() { var x; for (; 1;) { return 2; } }");
  }

  // Tests switch condition dead assignment elimination
  @Test
  public void testProcess_switchConditionDeadAssign_removesAssign() {
    test("function f() { var x; switch (x = 1) { case 1: return 2; } }",
         "function f() { var x; switch (1) { case 1: return 2; } }");
  }

  // Tests logical OR short-circuit assignment preservation
  @Test
  public void testProcess_shortCircuitOr_preservesAssign() {
    testSame("function f(x, y) { var a; (a = x) || (a = y); return a; }");
  }

  // Tests logical AND short-circuit assignment preservation
  @Test
  public void testProcess_shortCircuitAnd_preservesAssign() {
    testSame("function f(x, y) { var a; (a = x) && (a = y); return a; }");
  }

  // Tests ternary hook branch assignment preservation
  @Test
  public void testProcess_hookBranch_preservesAssign() {
    testSame("function f(x, y) { var a; (a = x) ? (a = 1) : (a = 2); return a; }");
  }

  // Tests ternary hook with one branch assignment preservation
  @Test
  public void testProcess_hookOneBranch_preservesAssign() {
    testSame("function f(x) { var a; (a = x) ? 1 : (a = 2); return a; }");
  }

  // Tests chained assignments elimination
  @Test
  public void testProcess_chainedDeadAssigns_removesAssigns() {
    test("function f() { var x, y; x = y = 1; return 2; }",
         "function f() { var x, y; 1; return 2; }");
  }
}