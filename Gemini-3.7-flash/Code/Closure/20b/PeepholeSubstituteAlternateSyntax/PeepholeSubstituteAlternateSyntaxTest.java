package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

public class PeepholeSubstituteAlternateSyntaxTest extends CompilerTestCase {

  private boolean late = true;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    late = true;
    enableLineNumberCheck(true);
    disableNormalize();
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new PeepholeOptimizationsPass(compiler,
        new PeepholeSubstituteAlternateSyntax(late));
  }

  // Tests folding String(x) with a single argument into '' + x
  @Test
  public void testFoldSimpleFunctionCall_singleArg_foldsToConcat() {
    test("var x = String('hello');", "var x = '' + 'hello';");
  }

  // Tests that String(a, b) with multiple arguments is not folded (Defects4J Closure-20 regression test)
  @Test
  public void testFoldSimpleFunctionCall_multipleArgs_doesNotFold() {
    testSame("var x = String('hello', 'world');");
  }

  // Tests that String() with no arguments is not folded
  @Test
  public void testFoldSimpleFunctionCall_noArgs_doesNotFold() {
    testSame("var x = String();");
  }

  // Tests that calls to functions other than String are not folded
  @Test
  public void testFoldSimpleFunctionCall_nonStringName_doesNotFold() {
    testSame("var x = OtherFunction('hello');");
  }

  // Tests minimizing !(x == y) to x != y
  @Test
  public void testMinimizeNot_equalsOperator_replacesWithNotEquals() {
    test("if (!(x == y)) { foo(); }", "if (x != y) { foo(); }");
  }

  // Tests minimizing !(x === y) to x !== y
  @Test
  public void testMinimizeNot_strictEqualsOperator_replacesWithStrictNotEquals() {
    test("if (!(x === y)) { foo(); }", "if (x !== y) { foo(); }");
  }

  // Tests replacing if-else returning values with a conditional hook return
  @Test
  public void testMinimizeIf_thenAndElseReturns_foldsToHook() {
    test("function f() { if (x) return 1; else return 2; }",
         "function f() { return x ? 1 : 2; }");
  }

  // Tests replacing if-else assignments to the same variable with a conditional hook
  @Test
  public void testMinimizeIf_thenAndElseAssignments_foldsToHook() {
    test("if (x) a = 1; else a = 2;", "a = x ? 1 : 2;");
  }

  // Tests replacing simple if statement without else into an AND expression
  @Test
  public void testMinimizeIf_noElseBranch_foldsToAnd() {
    test("if (x) foo();", "x && foo();");
  }

  // Tests replacing if(!x) foo() without else into an OR expression
  @Test
  public void testMinimizeIf_notConditionNoElseBranch_foldsToOr() {
    test("if (!x) foo();", "x || foo();");
  }

  // Tests reducing return undefined to return
  @Test
  public void testReduceReturn_returnUndefined_removesUndefined() {
    test("function f() { return undefined; }", "function f() { return; }");
  }

  // Tests reducing return void 0 to return
  @Test
  public void testReduceReturn_returnVoidZero_removesVoid() {
    test("function f() { return void 0; }", "function f() { return; }");
  }

  // Tests reducing true and false literals to !0 and !1 when late is true
  @Test
  public void testReduceTrueFalse_lateMode_replacesWithNotNumber() {
    late = true;
    test("var x = true;", "var x = !0;");
    test("var x = false;", "var x = !1;");
  }

  // Tests folding new RegExp(...) to regex literal
  @Test
  public void testFoldLiteralConstructor_validRegExp_foldsToRegExpLiteral() {
    enableNormalize();
    test("var x = new RegExp('foo', 'i');", "var x = /foo/i;");
  }

  // Tests reporting an error on invalid regular expression flags
  @Test
  public void testFoldLiteralConstructor_invalidRegExpFlags_emitsDiagnostic() {
    enableNormalize();
    test("var x = new RegExp('foo', 'bad');", "var x = new RegExp('foo', 'bad');",
        PeepholeSubstituteAlternateSyntax.INVALID_REGULAR_EXPRESSION_FLAGS);
  }

  // Tests folding new Array(...) with arguments to array literal
  @Test
  public void testFoldLiteralConstructor_arrayWithArgs_foldsToArrayLiteral() {
    enableNormalize();
    test("var x = new Array('a', 'b');", "var x = ['a', 'b'];");
  }

  // Tests folding new Object() without arguments to object literal
  @Test
  public void testFoldLiteralConstructor_emptyObject_foldsToObjectLiteral() {
    enableNormalize();
    test("var x = new Object();", "var x = {};");
  }

  // Tests folding immediate invocation of bound function to .call(...)
  @Test
  public void testFoldImmediateCallToBoundFunction_withThisValue_foldsToCallMethod() {
    test("(fn.bind(obj, 1))(2);", "fn.call(obj, 1, 2);");
  }

  // Tests splitting comma expressions into separate statements when late is false
  @Test
  public void testSplitComma_notLate_splitsCommaExpression() {
    late = false;
    test("x, y;", "x; y;");
  }

  // Tests minimizing !(x != y) to x == y
  @Test
  public void testMinimizeNot_notEqualsOperator_replacesWithEquals() {
    test("if (!(x != y)) { foo(); }", "if (x == y) { foo(); }");
  }

  // Tests minimizing !(x !== y) to x === y
  @Test
  public void testMinimizeNot_strictNotEqualsOperator_replacesWithStrictEquals() {
    test("if (!(x !== y)) { foo(); }", "if (x === y) { foo(); }");
  }

  // Tests minimizing relational operators under NOT
  @Test
  public void testMinimizeNot_relationalOperators() {
    test("if (!(a < b)) { foo(); }", "if (a >= b) { foo(); }");
    test("if (!(a <= b)) { foo(); }", "if (a > b) { foo(); }");
    test("if (!(a > b)) { foo(); }", "if (a <= b) { foo(); }");
    test("if (!(a >= b)) { foo(); }", "if (a < b) { foo(); }");
  }

  // Tests replacing undefined with void 0 when late is true
  @Test
  public void testReplaceUndefined_lateMode_replacesWithVoidZero() {
    late = true;
    test("var x = undefined;", "var x = void 0;");
  }

  // Tests while(true) folding to for(;;)
  @Test
  public void testFoldLoop_whileTrue_foldsToForInfinite() {
    test("while (true) { foo(); }", "for (;;) { foo(); }");
  }

  // Tests new Array() without args folds to []
  @Test
  public void testFoldLiteralConstructor_emptyArray_foldsToArrayLiteral() {
    enableNormalize();
    test("var x = new Array();", "var x = [];");
  }

  // Tests new Array(1) single numeric argument is not folded to [1]
  @Test
  public void testFoldLiteralConstructor_singleNumberArray_doesNotFold() {
    enableNormalize();
    testSame("var x = new Array(5);");
  }

  // Tests folding RegExp without flags to regex literal
  @Test
  public void testFoldLiteralConstructor_regExpWithoutFlags_foldsToRegExpLiteral() {
    enableNormalize();
    test("var x = new RegExp('abc');", "var x = /abc/;");
  }

  // Tests folding Boolean(x) into !x or !!x
  @Test
  public void testFoldSimpleFunctionCall_boolean_foldsToNot() {
    late = true;
    test("var x = Boolean(y);", "var x = !!y;");
  }

  // Tests folding immediate invocation of bound function with no bound arguments
  @Test
  public void testFoldImmediateCallToBoundFunction_noBoundArgs_foldsToCall() {
    test("(fn.bind(obj))(1, 2);", "fn.call(obj, 1, 2);");
  }
}