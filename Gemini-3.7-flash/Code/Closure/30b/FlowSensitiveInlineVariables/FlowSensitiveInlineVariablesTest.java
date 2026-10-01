package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link FlowSensitiveInlineVariables}.
 */
public class FlowSensitiveInlineVariablesTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new FlowSensitiveInlineVariables(compiler);
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests simple variable declaration inlining to a single read use
  @Test
  public void testInline_simpleVarDeclaration_inlinesValue() {
    test("function f() { var x = 1; return x; }",
         "function f() { return 1; }");
  }

  // Tests simple assignment statement inlining
  @Test
  public void testInline_simpleAssignment_inlinesValue() {
    test("function f() { var x; x = 1; return x; }",
         "function f() { var x; return 1; }");
  }

  // Tests that multiple uses within the same CFG node prevent inlining
  @Test
  public void testInline_multipleUsesInSameCfgNode_doesNotInline() {
    testSame("function f() { var x = 1; return x + x; }");
  }

  // Tests that variable use inside a loop is not inlined
  @Test
  public void testInline_useInsideLoop_doesNotInline() {
    testSame("function f() { var x = 1; while (true) { return x; } }");
  }

  // Tests that GETPROP on RHS prevents inlining
  @Test
  public void testInline_rhsHasGetProp_doesNotInline() {
    testSame("function f(a) { var x = a.b; return x; }");
  }

  // Tests that GETELEM on RHS prevents inlining
  @Test
  public void testInline_rhsHasGetElem_doesNotInline() {
    testSame("function f(a) { var x = a[0]; return x; }");
  }

  // Tests that Object literal on RHS prevents inlining
  @Test
  public void testInline_rhsHasObjectLiteral_doesNotInline() {
    testSame("function f() { var x = {a: 1}; return x; }");
  }

  // Tests that Array literal on RHS prevents inlining
  @Test
  public void testInline_rhsHasArrayLiteral_doesNotInline() {
    testSame("function f() { var x = [1, 2]; return x; }");
  }

  // Tests that RegExp literal on RHS prevents inlining
  @Test
  public void testInline_rhsHasRegExp_doesNotInline() {
    testSame("function f() { var x = /abc/; return x; }");
  }

  // Tests that 'new' expression on RHS prevents inlining
  @Test
  public void testInline_rhsHasNew_doesNotInline() {
    testSame("function f() { var x = new Object(); return x; }");
  }

  // Tests that variables in global scope are not inlined
  @Test
  public void testInline_globalScope_doesNotInline() {
    testSame("var x = 1; var y = x;");
  }

  // Tests that function parameters are not inlined
  @Test
  public void testInline_parameterDefinition_doesNotInline() {
    testSame("function f(x) { return x; }");
  }

  // Tests that assignment used as an R-value is not inlined
  @Test
  public void testInline_assignmentUsedAsRValue_doesNotInline() {
    testSame("function f() { var x; var y = (x = 1); return x; }");
  }

  // Tests that side effect between definition and use prevents inlining
  @Test
  public void testInline_sideEffectBetweenDefAndUse_doesNotInline() {
    testSame("function f() { var x = 1; modify(); return x; }");
  }

  // Tests that side-effecting RHS prevents inlining
  @Test
  public void testInline_sideEffectInDefRhs_doesNotInline() {
    testSame("function f() { var x = foo(); return x; }");
  }

  // Tests outer scope dependency followed by mutation of outer scope variable (Defects4J 30b)
  @Test
  public void testInline_outerScopeVariableDependency_doesNotInline() {
    testSame("var x = 1; function f() { var a = x; x = 2; return a; }");
  }

  // Tests outer scope dependency with function call potentially modifying outer scope
  @Test
  public void testInline_outerScopeVariableModifiedInFunctionCall_doesNotInline() {
    testSame("var x = 1; function f() { var a = x; g(); return a; } function g() { x = 2; }");
  }

  // Tests multiple reaching definitions preventing inlining
  @Test
  public void testInline_multipleReachingDefinitions_doesNotInline() {
    testSame("function f(b) { var x = 1; if (b) { x = 2; } return x; }");
  }

  // Tests that increment/decrement uses are not considered pure reads
  @Test
  public void testInline_incrementOperation_doesNotInline() {
    testSame("function f() { var x = 1; x++; return x; }");
  }

  // Tests inlining within nested function scope
  @Test
  public void testInline_nestedFunctionScope_inlinesInnerVar() {
    test("function f() { function g() { var x = 1; return x; } return g(); }",
         "function f() { function g() { return 1; } return g(); }");
  }

  // Tests boolean, string, and null literal inlining
  @Test
  public void testInline_literals_inlinesValues() {
    test("function f() { var x = true; return x; }",
         "function f() { return true; }");
    test("function f() { var x = 'hello'; return x; }",
         "function f() { return 'hello'; }");
    test("function f() { var x = null; return x; }",
         "function f() { return null; }");
  }

  // Tests inlining into function call argument
  @Test
  public void testInline_intoCallArgument_inlinesValue() {
    test("function f() { var x = 1; g(x); }",
         "function f() { g(1); }");
  }

  // Tests inlining into IF condition
  @Test
  public void testInline_intoIfCondition_inlinesValue() {
    test("function f() { var x = true; if (x) { return 1; } return 0; }",
         "function f() { if (true) { return 1; } return 0; }");
  }

  // Tests inlining into expression with binary operator
  @Test
  public void testInline_intoBinaryExpression_inlinesValue() {
    test("function f(a) { var x = 1; return a + x; }",
         "function f(a) { return a + 1; }");
  }

  // Tests multi-variable VAR declaration where one variable is inlined and removed
  @Test
  public void testInline_multiVarDeclaration_inlinesTargetAndKeepsOthers() {
    test("function f() { var x = 1, y = 2; return x; }",
         "function f() { var y = 2; return 1; }");
  }

  // Tests unary operator RHS inlining
  @Test
  public void testInline_unaryExpressionRhs_inlinesValue() {
    test("function f(a) { var x = !a; return x; }",
         "function f(a) { return !a; }");
    test("function f(a) { var x = -a; return x; }",
         "function f(a) { return -a; }");
    test("function f(a) { var x = typeof a; return x; }",
         "function f(a) { return typeof a; }");
  }

  // Tests binary expression RHS inlining when operands are immutable/safe
  @Test
  public void testInline_binaryExpressionRhs_inlinesValue() {
    test("function f(a, b) { var x = a + b; return x; }",
         "function f(a, b) { return a + b; }");
  }

  // Tests inlining across intervening pure statements
  @Test
  public void testInline_acrossPureStatements_inlinesValue() {
    test("function f() { var x = 1; var y = 2; return x; }",
         "function f() { var y = 2; return 1; }");
  }

  // Tests that local variable modified between RHS evaluation and use prevents inlining
  @Test
  public void testInline_localVariableModifiedBetweenDefAndUse_doesNotInline() {
    testSame("function f() { var y = 1; var x = y; y = 2; return x; }");
  }

  // Tests that presence of eval in scope prevents inlining
  @Test
  public void testInline_evalInScope_doesNotInline() {
    testSame("function f() { var x = 1; eval('x = 2'); return x; }");
  }

  // Tests inlining into switch expression
  @Test
  public void testInline_intoSwitchExpression_inlinesValue() {
    test("function f() { var x = 1; switch (x) { case 1: return 2; } }",
         "function f() { switch (1) { case 1: return 2; } }");
  }

  // Tests inlining into ternary (hook) operator condition
  @Test
  public void testInline_intoTernary_inlinesValue() {
    test("function f(a) { var x = 1; return a ? x : 0; }",
         "function f(a) { return a ? 1 : 0; }");
  }

  // Tests that multiple reads across different statements prevent inlining
  @Test
  public void testInline_multipleReadsAcrossStatements_doesNotInline() {
    testSame("function f() { var x = 1; g(x); return x; }");
  }

  // Tests that definition without initialization is not inlined
  @Test
  public void testInline_varWithoutInitialization_doesNotInline() {
    testSame("function f() { var x; return x; }");
  }

  // Tests reassignment before use does not inline the first definition
  @Test
  public void testInline_reassignmentBeforeUse_doesNotInlinedFirstDef() {
    test("function f() { var x = 1; x = 2; return x; }",
         "function f() { var x = 1; return 2; }");
  }

  // Tests inlining into throw statement
  @Test
  public void testInline_intoThrowStatement_inlinesValue() {
    test("function f() { var x = 'error'; throw x; }",
         "function f() { throw 'error'; }");
  }
}