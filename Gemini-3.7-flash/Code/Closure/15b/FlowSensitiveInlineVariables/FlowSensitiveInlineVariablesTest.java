package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

public class FlowSensitiveInlineVariablesTest extends CompilerTestCase {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new FlowSensitiveInlineVariables(compiler);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests simple variable declaration and inlining into return
  @Test
  public void testInlineVariable_simpleVarDeclaration_inlinesValue() {
    test("function f(x) { var a = x; return a; }",
         "function f(x) { return x; }");
  }

  // Tests simple assignment inlining
  @Test
  public void testInlineVariable_simpleAssign_inlinesValue() {
    test("function f(x) { var a; a = x; return a; }",
         "function f(x) { var a; return x; }");
  }

  // Tests that global scope variables are not inlined
  @Test
  public void testInlineVariable_globalScope_doesNotInlining() {
    testSame("var a = 1; var b = a;");
  }

  // Tests that multiple uses prevent inlining
  @Test
  public void testInlineVariable_multipleUses_doesNotInlining() {
    testSame("function f(x) { var a = x; return a + a; }");
  }

  // Tests that RHS with side effects is not inlined
  @Test
  public void testInlineVariable_rhsHasSideEffects_doesNotInlining() {
    testSame("function f(x) { var a = g(); return a; }");
  }

  // Tests side effect occurring along the path between def and use
  @Test
  public void testInlineVariable_sideEffectAlongPath_doesNotInlining() {
    testSame("function f(x) { var a = x; g(); return a; }");
  }

  // Tests delete property operator between def and use
  @Test
  public void testInlineVariable_deletePropAlongPath_doesNotInlining() {
    testSame("function f(x, y) { var a = x; delete y.prop; return a; }");
  }

  // Tests for-in loop along the path between def and use
  @Test
  public void testInlineVariable_forInAlongPath_doesNotInlining() {
    testSame("function f(x, y) { var a = x; for (var k in y) {} return a; }");
  }

  // Tests use within a loop is not inlined
  @Test
  public void testInlineVariable_useWithinLoop_doesNotInlining() {
    testSame("function f(x) { var a = x; while (true) { return a; } }");
  }

  // Tests array literal definition is not inlined
  @Test
  public void testInlineVariable_arrayLiteral_doesNotInlining() {
    testSame("function f(x) { var a = [x]; return a; }");
  }

  // Tests object literal definition is not inlined
  @Test
  public void testInlineVariable_objectLiteral_doesNotInlining() {
    testSame("function f(x) { var a = {prop: x}; return a; }");
  }

  // Tests new expression definition is not inlined
  @Test
  public void testInlineVariable_newExpression_doesNotInlining() {
    testSame("function f(x) { var a = new Object(); return a; }");
  }

  // Tests property access RHS is not inlined
  @Test
  public void testInlineVariable_getPropExpression_doesNotInlining() {
    testSame("function f(x) { var a = x.prop; return a; }");
  }

  // Tests element access RHS is not inlined
  @Test
  public void testInlineVariable_getElemExpression_doesNotInlining() {
    testSame("function f(x) { var a = x[0]; return a; }");
  }

  // Tests side effect occurring on the left of the use
  @Test
  public void testInlineVariable_sideEffectLeftOfUse_doesNotInlining() {
    testSame("function f(x) { var a = x; return g(), a; }");
  }

  // Tests side effect occurring on the right of the definition
  @Test
  public void testInlineVariable_sideEffectRightOfDef_doesNotInlining() {
    testSame("function f(x) { var a = (g(), x); return a; }");
  }

  // Tests assignment used as R-Value is not inlined
  @Test
  public void testInlineVariable_assignmentAsRValue_doesNotInlining() {
    testSame("function f(x) { var a; var b = (a = x); return a; }");
  }

  // Tests function parameter cannot be inlined
  @Test
  public void testInlineVariable_functionParam_doesNotInlining() {
    testSame("function f(a) { return a; }");
  }

  // Tests inlining into a binary operator expression
  @Test
  public void testInlineVariable_intoBinaryExpression_inlinesValue() {
    test("function f(x) { var a = x; return a + 1; }",
         "function f(x) { return x + 1; }");
  }

  // Tests inlining into a function call argument
  @Test
  public void testInlineVariable_intoCallArgument_inlinesValue() {
    test("function f(x) { var a = x; return bar(a); }",
         "function f(x) { return bar(x); }");
  }

  // Tests inlining into an IF condition
  @Test
  public void testInlineVariable_intoIfCondition_inlinesValue() {
    test("function f(x) { var a = x; if (a) { return 1; } return 2; }",
         "function f(x) { if (x) { return 1; } return 2; }");
  }

  // Tests inlining unary expression
  @Test
  public void testInlineVariable_unaryExpression_inlinesValue() {
    test("function f(x) { var a = !x; return a; }",
         "function f(x) { return !x; }");
    test("function f(x) { var a = typeof x; return a; }",
         "function f(x) { return typeof x; }");
    test("function f(x) { var a = -x; return a; }",
         "function f(x) { return -x; }");
  }

  // Tests inlining logical binary expressions
  @Test
  public void testInlineVariable_logicalBinaryExpression_inlinesValue() {
    test("function f(x, y) { var a = x && y; return a; }",
         "function f(x, y) { return x && y; }");
    test("function f(x, y) { var a = x || y; return a; }",
         "function f(x, y) { return x || y; }");
  }

  // Tests inlining ternary conditional expression
  @Test
  public void testInlineVariable_hookExpression_inlinesValue() {
    test("function f(x) { var a = x ? 1 : 2; return a; }",
         "function f(x) { return x ? 1 : 2; }");
  }

  // Tests reassignment before use inlines the latest value
  @Test
  public void testInlineVariable_reassignmentBeforeUse_inlinesLatest() {
    test("function f(x) { var a = 1; a = 2; return a; }",
         "function f(x) { var a = 1; return 2; }");
  }

  // Tests 'this' reference inlining
  @Test
  public void testInlineVariable_thisReference_inlinesValue() {
    test("function f() { var a = this; return a; }",
         "function f() { return this; }");
  }

  // Tests conditional assignment branches do not inline across divergent paths
  @Test
  public void testInlineVariable_conditionalBranches_doesNotInlining() {
    testSame("function f(x) { var a; if (x) { a = 1; } else { a = 2; } return a; }");
  }

  // Tests inner function accessing variable escapes scope and prevents inlining
  @Test
  public void testInlineVariable_innerFunctionEscape_doesNotInlining() {
    testSame("function f(x) { var a = x; function g() { return a; } return g(); }");
  }

  // Tests catch clause scope prevents incorrect inlining
  @Test
  public void testInlineVariable_catchBlock_doesNotInlining() {
    testSame("function f(x) { try { var a = x; } catch (e) { return e; } return a; }");
  }

  // Tests switch statement branches prevent inlining
  @Test
  public void testInlineVariable_switchStatement_doesNotInlining() {
    testSame("function f(x) { var a = 1; switch (x) { case 1: a = 2; break; } return a; }");
  }

  // Tests variable modification inside loop prevents inlining
  @Test
  public void testInlineVariable_modifiedInsideWhileLoop_doesNotInlining() {
    testSame("function f(x) { var a = 1; while (x) { a = 2; } return a; }");
  }

  // Tests variable modification inside do-while loop prevents inlining
  @Test
  public void testInlineVariable_modifiedInsideDoWhileLoop_doesNotInlining() {
    testSame("function f(x) { var a = 1; do { a = 2; } while (x); return a; }");
  }

  // Tests variable modification inside for loop prevents inlining
  @Test
  public void testInlineVariable_modifiedInsideForLoop_doesNotInlining() {
    testSame("function f(x) { var a = 1; for (var i = 0; i < 10; i++) { a = 2; } return a; }");
  }

  // Tests self-referential assignment
  @Test
  public void testInlineVariable_selfReferentialAssignment_doesNotInlining() {
    testSame("function f(x) { var a = 1; a = a + 1; return a; }");
  }
}