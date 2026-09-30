package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

public class PeepholeSubstituteAlternateSyntaxTest extends CompilerTestCase {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    enableLineNumberCheck(true);
    enableNormalize();
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new PeepholeOptimizationsPass(compiler,
        new PeepholeSubstituteAlternateSyntax());
  }

  // Tests folding of simple IF statements to AND / OR expressions
  @Test
  public void testOptimizeSubtree_simpleIf_foldsToAndOr() {
    test("if (x) foo();", "x && foo();");
    test("if (!x) foo();", "x || foo();");
  }

  // Tests that event handlers with potential side effects/implicit this in IE are not folded
  @Test
  public void testOptimizeSubtree_eventHandlerInIf_doesNotFold() {
    testSame("if (f) f.onchange();");
    testSame("if (f) f.onclick();");
  }

  // Tests folding IF statement condition negation with else branch
  @Test
  public void testOptimizeSubtree_negatedConditionWithElse_invertsBranches() {
    test("if (!x) foo(); else bar();", "if (x) bar(); else foo();");
  }

  // Tests folding IF-ELSE branches returning values into a ternary HOOK return
  @Test
  public void testOptimizeSubtree_ifElseReturns_foldsToHookReturn() {
    test("if (x) return 1; else return 2;", "return x ? 1 : 2;");
  }

  // Tests folding IF-ELSE branches assigning to the same variable into a HOOK assignment
  @Test
  public void testOptimizeSubtree_ifElseAssignments_foldsToHookAssign() {
    test("if (x) a = 1; else a = 2;", "a = x ? 1 : 2;");
    test("if (x) var y = 1; else y = 2;", "var y = x ? 1 : 2;");
    test("if (x) y = 1; else var y = 2;", "var y = x ? 1 : 2;");
  }

  // Tests removal of repeated statements in IF-ELSE branches
  @Test
  public void testOptimizeSubtree_repeatedStatementsInBranches_extractsCommonTail() {
    test("if (a) { x = 1; return true; } else { x = 2; return true; }",
         "if (a) x = 1; else x = 2; return true;");
  }

  // Tests NOT operator minimization on equality comparisons
  @Test
  public void testOptimizeSubtree_notOperatorOnComparisons_invertsComparison() {
    test("!(x == y)", "x != y");
    test("!(x != y)", "x == y");
    test("!(x === y)", "x !== y");
    test("!(x !== y)", "x === y");
    testSame("!(x < y)");
  }

  // Tests reducing return statements with undefined or void 0 to empty returns
  @Test
  public void testOptimizeSubtree_returnUndefinedOrVoid_simplifiesToReturn() {
    test("function f() { return undefined; }", "function f() { return; }");
    test("function f() { return void 0; }", "function f() { return; }");
    testSame("function f() { return void foo(); }");
  }

  // Tests boolean condition minimization with HOOK expressions
  @Test
  public void testOptimizeSubtree_conditionWithHook_minimizesBoolean() {
    test("if (x ? true : false) foo();", "if (x) foo();");
    test("if (x ? false : true) foo();", "if (!x) foo();");
    test("if (x ? true : y) foo();", "if (x || y) foo();");
    test("if (x ? y : false) foo();", "if (x && y) foo();");
  }

  // Tests boolean condition minimization with double negation and De Morgan laws
  @Test
  public void testOptimizeSubtree_booleanLogicInCondition_simplifies() {
    test("if (!!x) foo();", "if (x) foo();");
    test("if (!(x || y)) foo();", "if (!x && !y) foo();");
    test("if (!(x && y)) foo();", "if (!x || !y) foo();");
  }

  // Tests folding Object constructor to object literal
  @Test
  public void testOptimizeSubtree_objectConstructor_foldsToLiteral() {
    test("new Object()", "({})");
    test("Object()", "({})");
  }

  // Tests folding Array constructor to array literal
  @Test
  public void testOptimizeSubtree_arrayConstructor_foldsToLiteral() {
    test("new Array()", "[]");
    test("new Array(1, 2, 3)", "[1, 2, 3]");
    test("new Array(0)", "[]");
    test("new Array('a')", "['a']");
    test("new Array([1, 2])", "[[1, 2]]");
    testSame("new Array(5)");
  }

  // Tests folding RegExp constructor to regexp literal
  @Test
  public void testOptimizeSubtree_regExpConstructor_foldsToLiteral() {
    test("new RegExp('abc')", "/abc/");
    test("new RegExp('abc', 'i')", "/abc/i");
    test("new RegExp('a/b')", "/a\\/b/");
    testSame("new RegExp('abc', 'g')");
  }

  // Tests folding while(true) to for(;;)
  @Test
  public void testOptimizeSubtree_whileTrue_foldsToFor() {
    test("while (true) { foo(); }", "for (;;) { foo(); }");
    test("while (1) { foo(); }", "for (;;) { foo(); }");
  }

  // Tests removing redundant trailing return statements in functions
  @Test
  public void testOptimizeSubtree_trailingReturnInFunction_removesReturn() {
    test("function f() { foo(); return; }", "function f() { foo(); }");
    test("function f() { if (x) { foo(); return; } }", "function f() { if (x) { foo(); } }");
  }

  // Tests folding hook expressions where both branches are identical or boolean-like
  @Test
  public void testOptimizeSubtree_hookExpressions_simplifies() {
    test("x ? x : y", "x || y");
    test("x ? y : x", "x && y");
    test("x ? a : a", "x, a");
  }

  // Tests array literal of string elements splitting optimization
  @Test
  public void testOptimizeSubtree_stringArrayLiteral_foldsToSplit() {
    test("['a', 'b', 'c', 'd', 'e']", "\"a b c d e\".split(\" \")");
    test("['hello', 'world', 'foo', 'bar']", "\"hello world foo bar\".split(\" \")");
  }

  // Tests folding RegExp constructor without 'new' keyword
  @Test
  public void testOptimizeSubtree_regExpCall_foldsToLiteral() {
    test("RegExp('hello')", "/hello/");
    test("RegExp('hello', 'm')", "/hello/m");
    testSame("RegExp('foo', 'g')");
  }

  // Tests folding typeof comparisons against undefined
  @Test
  public void testOptimizeSubtree_typeofUndefined_simplifies() {
    test("typeof x === 'undefined'", "x === void 0");
    test("typeof x !== 'undefined'", "x !== void 0");
    test("'undefined' === typeof x", "x === void 0");
    test("'undefined' !== typeof x", "x !== void 0");
  }

  // Tests nested if and return statement merging
  @Test
  public void testOptimizeSubtree_ifReturnElseReturn_simplifies() {
    test("function f() { if (x) { return 1; } return 2; }",
         "function f() { return x ? 1 : 2; }");
    test("function f() { if (x) return 1; if (y) return 2; return 3; }",
         "function f() { return x ? 1 : y ? 2 : 3; }");
  }

  // Tests comma operator expression flattening in return and expressions
  @Test
  public void testOptimizeSubtree_commaExpression_optimizes() {
    test("function f() { a(); return b(); }", "function f() { return a(), b(); }");
    test("if (x) { a(); b(); }", "x && (a(), b());");
  }

  // Tests loop condition simplification
  @Test
  public void testOptimizeSubtree_forLoopCondition_simplifies() {
    test("for (var i = 0; true; i++) { foo(); }", "for (var i = 0; ; i++) { foo(); }");
    test("for (; true; ) { foo(); }", "for (;;) { foo(); }");
  }
}