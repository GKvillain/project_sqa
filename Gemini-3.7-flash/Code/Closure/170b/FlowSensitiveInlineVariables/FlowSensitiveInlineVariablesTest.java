package com.google.javascript.jscomp;

import org.junit.Test;

public class FlowSensitiveInlineVariablesTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new FlowSensitiveInlineVariables(compiler);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  private void inline(String js, String expected) {
    test(js, expected);
  }

  // Tests simple variable declaration and return inlining
  @Test
  public void testSimpleVarInline_validCandidate_inlinesValue() {
    inline("function f() { var x = 1; return x; }",
           "function f() { return 1; }");
  }

  // Tests simple assignment to var and return inlining
  @Test
  public void testSimpleAssignInline_validCandidate_inlinesValue() {
    inline("function f() { var x; x = 1; return x; }",
           "function f() { var x; return 1; }");
  }

  // Tests that variables with multiple uses in the CFG are not inlined
  @Test
  public void testMultipleUses_multipleReferences_doesNotInline() {
    testSame("function f() { var x = 1; return x + x; }");
  }

  // Tests that global scope variables are not inlined
  @Test
  public void testGlobalScope_globalVariable_doesNotInline() {
    testSame("var x = 1; function f() { return x; }");
  }

  // Tests that function parameters are not inlined
  @Test
  public void testParameter_functionParamDef_doesNotInline() {
    testSame("function f(x) { return x; }");
  }

  // Tests that variable inside a loop is not inlined
  @Test
  public void testWithinLoop_useInLoop_doesNotInline() {
    testSame("function f() { var x = 1; while (true) { print(x); } }");
  }

  // Tests that side effect between definition and use prevents inlining
  @Test
  public void testSideEffectPath_interveningCall_doesNotInline() {
    testSame("function f() { var x = read(); modify(); return x; }");
  }

  // Tests that definitions containing object literals are not inlined
  @Test
  public void testObjectLiteral_newObject_doesNotInline() {
    testSame("function f() { var x = {}; return x; }");
  }

  // Tests that definitions containing array literals are not inlined
  @Test
  public void testArrayLiteral_newArray_doesNotInline() {
    testSame("function f() { var x = [1, 2]; return x; }");
  }

  // Tests that definitions containing property access are not inlined
  @Test
  public void testGetProp_propertyAccess_doesNotInline() {
    testSame("function f(a) { var x = a.b; return x; }");
  }

  // Tests definitions containing element access are not inlined
  @Test
  public void testGetElem_elementAccess_doesNotInline() {
    testSame("function f(a) { var x = a[0]; return x; }");
  }

  // Tests that definitions containing regexp literals are not inlined
  @Test
  public void testRegExp_regexpLiteral_doesNotInline() {
    testSame("function f() { var x = /abc/; return x; }");
  }

  // Tests that references to catch parameters are not inlined
  @Test
  public void testCatchClause_catchVariable_doesNotInline() {
    testSame("function f() { try {} catch (e) { var x = e; return x; } }");
  }

  // Tests regression where variable assignment occurs conditionally in hook branch (Defects4J 170)
  @Test
  public void testConditionalHook_assignInHookBranch_doesNotInline() {
    testSame("function f(a) { var x; x = 1; print(a ? (x = 2) : 0); return x; }");
  }

  // Tests regression where variable assignment occurs conditionally in hook false branch
  @Test
  public void testConditionalHook_assignInHookFalseBranch_doesNotInline() {
    testSame("function f(a) { var x; x = 1; print(a ? 0 : (x = 2)); return x; }");
  }

  // Tests regression with logical OR containing assignment
  @Test
  public void testLogicalOr_assignInRightBranch_doesNotInline() {
    testSame("function f(a) { var x; x = 1; print(a || (x = 2)); return x; }");
  }

  // Tests regression with logical AND containing assignment
  @Test
  public void testLogicalAnd_assignInRightBranch_doesNotInline() {
    testSame("function f(a) { var x; x = 1; print(a && (x = 2)); return x; }");
  }

  // Tests chained variable dependencies inlining correctly
  @Test
  public void testDependentVariables_chainedDefinitions_inlinesSafely() {
    inline("function f() { var a = 1; var b = a; return b; }",
           "function f() { var a = 1; return a; }");
  }

  // Tests labeled statement assignment removal when inlining
  @Test
  public void testLabeledAssign_labeledStatement_inlinesAndCleansUp() {
    inline("function f() { var x; label: x = 1; return x; }",
           "function f() { var x; return 1; }");
  }

  // --- New Tests ---

  // Tests inlining an expression into a function call argument
  @Test
  public void testInlineIntoCallArgument() {
    inline("function f() { var x = 1 + 2; g(x); }",
           "function f() { g(1 + 2); }");
  }

  // Tests inlining into binary operations
  @Test
  public void testInlineIntoBinaryExpression() {
    inline("function f(a) { var x = 1; return a + x; }",
           "function f(a) { return a + 1; }");
  }

  // Tests inlining with multiple variable declarations in the same var statement
  @Test
  public void testMultiVarDeclaration() {
    inline("function f() { var x = 1, y = 2; return x; }",
           "function f() { var y = 2; return 1; }");
  }

  // Tests that self-reassignment prevents inlining
  @Test
  public void testSelfReassignment_doesNotInline() {
    testSame("function f(x) { x = x + 1; return x; }");
  }

  // Tests that modifying a variable used in a definition before use prevents inlining
  @Test
  public void testVariableModifiedBetweenDefAndUse_doesNotInline() {
    testSame("function f() { var a = 1; var x = a; a = 2; return x; }");
  }

  // Tests that conditional definition prevents inlining
  @Test
  public void testConditionalDefinition_doesNotInline() {
    testSame("function f(cond) { var x; if (cond) { x = 1; } return x; }");
  }

  // Tests inlining inside an if branch
  @Test
  public void testInlineInsideBranch() {
    inline("function f(cond) { if (cond) { var x = 1; return x; } return 0; }",
           "function f(cond) { if (cond) { return 1; } return 0; }");
  }

  // Tests that definitions containing function expressions are not inlined
  @Test
  public void testFunctionExpression_doesNotInline() {
    testSame("function f() { var x = function() {}; return x; }");
  }

  // Tests that definitions containing 'this' are not inlined
  @Test
  public void testThisExpression_doesNotInline() {
    testSame("function f() { var x = this.foo; return x; }");
  }

  // Tests that variables modified via pre/post increment are not inlined
  @Test
  public void testIncrementSideEffect_doesNotInline() {
    testSame("function f() { var x = 1; x++; return x; }");
    testSame("function f() { var x = 1; ++x; return x; }");
  }

  // Tests inlining within a switch statement
  @Test
  public void testInlineInsideSwitchCase() {
    inline("function f(e) { switch (e) { case 1: var x = 10; return x; } }",
           "function f(e) { switch (e) { case 1: return 10; } }");
  }

  // Tests inlining into throw statement
  @Test
  public void testInlineIntoThrow() {
    inline("function f() { var e = 'error'; throw e; }",
           "function f() { throw 'error'; }");
  }

  // Tests that variables used in do-while loops are not inlined
  @Test
  public void testDoWhileLoop_doesNotInline() {
    testSame("function f() { var x = 1; do { print(x); } while (false); }");
  }

  // Tests that variables in for-in loops are not inlined
  @Test
  public void testForInLoop_doesNotInline() {
    testSame("function f(obj) { var x = 1; for (var k in obj) { print(x); } }");
  }

  // Tests nested function scope variable shadowing does not affect outer inlining
  @Test
  public void testShadowedVariableInInnerScope() {
    inline("function f() { var x = 1; function g() { var x = 2; return x; } return x; }",
           "function f() { function g() { return 2; } return 1; }");
  }

  // Tests that assignments as expression values are handled properly
  @Test
  public void testAssignmentExpressionValue_doesNotInline() {
    testSame("function f() { var x; var y = (x = 1); return x; }");
  }

  // Tests comma operator expression inlining
  @Test
  public void testCommaOperatorInlining() {
    inline("function f() { var x = (1, 2); return x; }",
           "function f() { return (1, 2); }");
  }

  // Tests unary expressions inlining
  @Test
  public void testUnaryExpressionInlining() {
    inline("function f() { var x = !0; return x; }",
           "function f() { return !0; }");
    inline("function f() { var x = typeof 1; return x; }",
           "function f() { return typeof 1; }");
  }
}