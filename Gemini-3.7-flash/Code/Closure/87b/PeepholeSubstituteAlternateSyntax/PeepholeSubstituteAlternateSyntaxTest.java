package com.google.javascript.jscomp;

import org.junit.Test;

public class PeepholeSubstituteAlternateSyntaxTest extends CompilerTestCase {

  public PeepholeSubstituteAlternateSyntaxTest() {
    super();
  }

  @Override
  protected CompilerPass createProcessor(final Compiler compiler) {
    return new PeepholeOptimizationsPass(compiler, new PeepholeSubstituteAlternateSyntax());
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    enableLineNumberCheck(false);
    enableNormalize();
  }

  // Tests reducing return undefined to simple return
  @Test
  public void testOptimizeSubtree_returnUndefined_reducedToReturn() {
    test("function f() { return undefined; }", "function f() { return; }");
  }

  // Tests reducing return void 0 to simple return
  @Test
  public void testOptimizeSubtree_returnVoidZero_reducedToReturn() {
    test("function f() { return void 0; }", "function f() { return; }");
  }

  // Tests minimizing NOT equals to not-equal operator
  @Test
  public void testOptimizeSubtree_notEquals_foldsToNotEqual() {
    test("if (!(x == y)) foo();", "if (x != y) foo();");
  }

  // Tests minimizing NOT strict equals to strict not-equal operator
  @Test
  public void testOptimizeSubtree_notStrictEquals_foldsToStrictNotEqual() {
    test("if (!(x === y)) foo();", "if (x !== y) foo();");
  }

  // Tests folding if-else return statements into return hook
  @Test
  public void testOptimizeSubtree_ifElseReturns_foldsToHookReturn() {
    test("function f() { if (x) return 1; else return 2; }",
         "function f() { return x ? 1 : 2; }");
  }

  // Tests folding if-else assignments into assignment hook
  @Test
  public void testOptimizeSubtree_ifElseAssignments_foldsToHookAssignment() {
    test("if (x) a = 1; else a = 2;", "a = x ? 1 : 2;");
  }

  // Tests folding if-else calls into call hook
  @Test
  public void testOptimizeSubtree_ifElseCalls_foldsToHookCall() {
    test("if (x) foo(); else bar();", "x ? foo() : bar();");
  }

  // Tests folding if statement without else into AND expression
  @Test
  public void testOptimizeSubtree_ifWithoutElse_foldsToAndExpression() {
    test("if (x) foo();", "x && foo();");
  }

  // Tests folding if NOT statement without else into OR expression
  @Test
  public void testOptimizeSubtree_ifNotWithoutElse_foldsToOrExpression() {
    test("if (!x) foo();", "x || foo();");
  }

  // Tests inverting condition on if-else with NOT
  @Test
  public void testOptimizeSubtree_ifNotWithElse_invertsConditionAndSwapsBranches() {
    test("if (!x) foo(); else bar();", "if (x) bar(); else foo();");
  }

  // Tests folding if with var and else with assign to var hook
  @Test
  public void testOptimizeSubtree_ifVarElseAssign_foldsToVarHook() {
    test("if (x) var y = 1; else y = 2;", "var y = x ? 1 : 2;");
  }

  // Tests folding if with assign and else with var to var hook
  @Test
  public void testOptimizeSubtree_ifAssignElseVar_foldsToVarHook() {
    test("if (x) y = 1; else var y = 2;", "var y = x ? 1 : 2;");
  }

  // Tests minimizing double NOT in conditions
  @Test
  public void testOptimizeSubtree_conditionDoubleNot_removesDoubleNot() {
    test("if (!!x) foo();", "if (x) foo();");
  }

  // Tests De Morgan simplification in conditions
  @Test
  public void testOptimizeSubtree_conditionDeMorgan_simplifiesCondition() {
    test("if (!(!x || !y)) foo();", "if (x && y) foo();");
  }

  // Tests simplifying hook conditions where true case returns true
  @Test
  public void testOptimizeSubtree_conditionHookTrue_simplifiesToOr() {
    test("if (x ? true : y) foo();", "if (x || y) foo();");
  }

  // Tests simplifying hook conditions where false case returns false
  @Test
  public void testOptimizeSubtree_conditionHookFalse_simplifiesToAnd() {
    test("if (x ? y : false) foo();", "if (x && y) foo();");
  }

  // Tests folding new Object() to object literal
  @Test
  public void testOptimizeSubtree_newObject_foldsToObjectLiteral() {
    test("var x = new Object();", "var x = {};");
  }

  // Tests folding new Array() with arguments to array literal
  @Test
  public void testOptimizeSubtree_newArrayWithArgs_foldsToArrayLiteral() {
    test("var x = new Array(1, 2);", "var x = [1, 2];");
  }

  // Tests folding new RegExp() with safe flags to regex literal
  @Test
  public void testOptimizeSubtree_newRegExp_foldsToRegExpLiteral() {
    test("var x = new RegExp('abc', 'i');", "var x = /abc/i;");
  }

  // Tests regression for Issue 291: event handler call in expression block
  @Test
  public void testOptimizeSubtree_eventHandlerCall_doesNotFoldToAnd() {
    testSame("if (x) { onchange(); }");
  }
}