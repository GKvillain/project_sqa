package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

public class PeepholeSubstituteAlternateSyntaxTest extends CompilerTestCase {

  private boolean late = true;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    enableNormalize();
    late = true;
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new PeepholeOptimizationsPass(compiler,
        new PeepholeSubstituteAlternateSyntax(late));
  }

  // Tests folding if-else with return statements into a ternary return
  @Test
  public void testMinimizeIf_returnHook_replacesIfWithHook() {
    test("function f(x) { if (x) return 1; else return 2; }",
         "function f(x) { return x ? 1 : 2; }");
  }

  // Tests folding if-else with matching assignment into assignment with ternary hook
  @Test
  public void testMinimizeIf_assignHook_replacesIfWithAssignHook() {
    test("function f(x) { var a; if (x) a = 1; else a = 2; }",
         "function f(x) { var a; a = x ? 1 : 2; }");
  }

  // Tests folding if-else where then branch is var and else branch is assign
  @Test
  public void testMinimizeIf_varThenAssignElse_foldsToVarHook() {
    test("function f(x) { if (x) var a = 1; else a = 2; }",
         "function f(x) { var a = x ? 1 : 2; }");
  }

  // Tests folding if-else where then branch is assign and else branch is var
  @Test
  public void testMinimizeIf_assignThenVarElse_foldsToVarHook() {
    test("function f(x) { if (x) a = 1; else var a = 2; }",
         "function f(x) { var a = x ? 1 : 2; }");
  }

  // Tests inverting negated condition in if-else
  @Test
  public void testMinimizeIf_notConditionWithElse_invertsBranches() {
    test("function f(x) { if (!x) bar(); else foo(); }",
         "function f(x) { if (x) foo(); else bar(); }");
  }

  // Tests folding single-branch if into logical AND expression
  @Test
  public void testMinimizeIf_singleBranch_foldsToAnd() {
    test("function f(x) { if (x) foo(); }",
         "function f(x) { x && foo(); }");
  }

  // Tests folding single-branch if with negated condition into logical OR expression
  @Test
  public void testMinimizeIf_notConditionSingleBranch_foldsToOr() {
    test("function f(x) { if (!x) foo(); }",
         "function f(x) { x || foo(); }");
  }

  // Tests combining nested if statements into a single if with AND condition
  @Test
  public void testMinimizeIf_combineNestedIf_foldsToAndCondition() {
    test("function f(x, y) { if (x) { if (y) { foo(); } } }",
         "function f(x, y) { if (x && y) { foo(); } }");
  }

  // Tests reduction of boolean literals true and false to !0 and !1 when late is true
  @Test
  public void testReduceTrueFalse_lateOptimization_reducesTrueFalse() {
    late = true;
    test("var x = true; var y = false;",
         "var x = !0; var y = !1;");
  }

  // Tests folding Object constructor to object literal
  @Test
  public void testFoldLiteralConstructor_newObject_foldsToObjectLit() {
    test("var x = new Object();",
         "var x = {};");
    test("var x = Object();",
         "var x = {};");
  }

  // Tests folding Array constructor to array literal
  @Test
  public void testFoldLiteralConstructor_newArray_foldsToArrayLit() {
    test("var x = new Array();",
         "var x = [];");
    test("var x = new Array(1, 2, 3);",
         "var x = [1, 2, 3];");
    test("var x = Array();",
         "var x = [];");
    test("var x = Array(1, 2);",
         "var x = [1, 2];");
    test("var x = new Array('abc');",
         "var x = ['abc'];");
  }

  // Tests folding RegExp constructor to regular expression literal
  @Test
  public void testFoldLiteralConstructor_regExp_foldsToRegExpLit() {
    test("var x = new RegExp('abc', 'i');",
         "var x = /abc/i;");
    test("var x = new RegExp('hello');",
         "var x = /hello/;");
    test("var x = RegExp('hello');",
         "var x = /hello/;");
    test("var x = new RegExp('', '');",
         "var x = /(?:)/;");
  }

  // Tests simplifying return statements returning undefined or void 0
  @Test
  public void testTryReduceReturn_undefinedOrVoid_reducesToEmptyReturn() {
    test("function f() { return undefined; }",
         "function f() { return; }");
    test("function f() { return void 0; }",
         "function f() { return; }");
  }

  // Tests removing redundant return statement at the end of a function
  @Test
  public void testTryReduceReturn_endOfFunction_removesReturn() {
    test("function f() { a(); return; }",
         "function f() { a(); }");
    test("function f() { return; }",
         "function f() {}");
  }

  // Tests minimizing double NOT condition
  @Test
  public void testTryMinimizeCondition_doubleNot_removesDoubleNot() {
    test("if (!!x) { foo(); }",
         "if (x) { foo(); }");
  }

  // Tests De Morgan law optimization on NOT condition
  @Test
  public void testTryMinimizeCondition_deMorgan_foldsNotAnd() {
    test("if (!(x || y)) { foo(); }",
         "if (!x && !y) { foo(); }");
    test("if (!(x && y)) { foo(); }",
         "if (!x || !y) { foo(); }");
  }

  // Tests minimizing NOT with comparison operators
  @Test
  public void testTryMinimizeNot_equalityOperators_invertsOperator() {
    test("var r = !(x == y);",
         "var r = x != y;");
    test("var r = !(x === y);",
         "var r = x !== y;");
    test("var r = !(x != y);",
         "var r = x == y;");
    test("var r = !(x !== y);",
         "var r = x === y;");
    test("var r = !(x < y);",
         "var r = x >= y;");
    test("var r = !(x <= y);",
         "var r = x > y;");
    test("var r = !(x > y);",
         "var r = x <= y;");
    test("var r = !(x >= y);",
         "var r = x < y;");
  }

  // Tests splitting comma expressions when late optimization is false
  @Test
  public void testTrySplitComma_notLate_splitsComma() {
    late = false;
    test("x = 1, y = 2;",
         "x = 1; y = 2;");
  }

  // Tests replacing undefined name with void 0 in normalized AST
  @Test
  public void testTryReplaceUndefined_normalizedAst_replacesWithVoidZero() {
    test("var x = undefined;",
         "var x = void 0;");
  }

  // Tests folding Boolean() and Number() and String() calls
  @Test
  public void testTryFoldSimpleFunctionCall_string_foldsToConcat() {
    test("var x = String('abc');",
         "var x = '' + 'abc';");
    test("var x = String(y);",
         "var x = '' + y;");
    test("var x = Boolean(y);",
         "var x = !!y;");
    test("var x = Number(y);",
         "var x = +y;");
  }

  // Tests folding immediate call to bound function into .call invocation
  @Test
  public void testTryFoldImmediateCallToBoundFunction_bindCall_foldsToCall() {
    test("function f() {} var x = (f.bind(obj, 1, 2))();",
         "function f() {} var x = f.call(obj, 1, 2);");
    test("function f() {} var x = (f.bind(obj))(1, 2);",
         "function f() {} var x = f.call(obj, 1, 2);");
  }

  // Tests optimizing conditional (hook / ternary) expressions with boolean results
  @Test
  public void testFoldHook_booleanLiterals_simplifiesHook() {
    test("var r = x ? true : false;",
         "var r = !!x;");
    test("var r = x ? false : true;",
         "var r = !x;");
    test("var r = x ? true : y;",
         "var r = !!x || y;");
    test("var r = x ? false : y;",
         "var r = !x && y;");
    test("var r = x ? y : true;",
         "var r = !x || y;");
    test("var r = x ? y : false;",
         "var r = !!x && y;");
  }

  // Tests minimizing if statements with return or throw in both branches
  @Test
  public void testMinimizeIf_followReturnOrThrow_foldsHook() {
    test("function f(x) { if (x) return 1; return 2; }",
         "function f(x) { return x ? 1 : 2; }");
  }

  // Tests folding while(true) to for(;;)
  @Test
  public void testFoldWhile_trueCondition_foldsToFor() {
    test("while (true) { foo(); }",
         "for (;;) { foo(); }");
  }
}