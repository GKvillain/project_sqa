package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

public class InlineVariablesTest extends CompilerTestCase {

  private InlineVariables.Mode mode = InlineVariables.Mode.ALL;
  private boolean inlineAllStrings = false;

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new InlineVariables(compiler, mode, inlineAllStrings);
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    mode = InlineVariables.Mode.ALL;
    inlineAllStrings = false;
    enableNormalize();
  }

  // Tests single-use variable inlining in simple local scope
  @Test
  public void testInlineSingleUseVariable_simpleValue_inlinesCorrectly() {
    test("function f() { var x = 1; return x; }",
         "function f() { return 1; }");
  }

  // Tests immutable variable used multiple times
  @Test
  public void testInlineImmutableVariable_multipleReads_inlinesAll() {
    test("function f() { var x = 1; return x + x; }",
         "function f() { return 1 + 1; }");
  }

  // Tests that variable assigned multiple times is not inlined
  @Test
  public void testInlineVariable_reassigned_doesNotInline() {
    testSame("function f() { var x = 1; x = 2; return x; }");
  }

  // Tests MODE = CONSTANTS_ONLY inlines only constant variables
  @Test
  public void testInlineConstantsOnlyMode_constantAndNonConstant_inlinesOnlyConstant() {
    mode = InlineVariables.Mode.CONSTANTS_ONLY;
    test("var CONST_A = 1; var b = 2; return CONST_A + b;",
         "var b = 2; return 1 + b;");
  }

  // Tests MODE = LOCALS_ONLY does not inline global variables
  @Test
  public void testInlineLocalsOnlyMode_globalsAndLocals_inlinesOnlyLocals() {
    mode = InlineVariables.Mode.LOCALS_ONLY;
    test("var x = 1; var y = x; function f() { var a = 2; return a; }",
         "var x = 1; var y = x; function f() { return 2; }");
  }

  // Tests string inlining heuristic when inlineAllStrings is false
  @Test
  public void testInlineString_shortVersusLongString_respectsSizeHeuristic() {
    inlineAllStrings = false;
    test("function f() { var x = 'short'; return x; }",
         "function f() { return 'short'; }");
  }

  // Tests inlineAllStrings flag enabled
  @Test
  public void testInlineAllStrings_longString_inlinesRegardlessOfSize() {
    inlineAllStrings = true;
    test("var x = 'a very long string that would not normally inline'; return x + x;",
         "return 'a very long string that would not normally inline' + " +
         "'a very long string that would not normally inline';");
  }

  // Tests that property method call context is preserved and not inlined as callee
  @Test
  public void testInlineMethodCallContext_getPropAsCallee_doesNotInlineToPreserveThis() {
    testSame("var a = b.c; a();");
    test("var a = b.c; f(a);", "f(b.c);");
  }

  // Tests variable aliasing where candidate alias is inlined
  @Test
  public void testInlineAlias_singleAliasOfVariable_inlinesAlias() {
    test("function f() { var x = extern(); var y = x; return y; }",
         "function f() { var x = extern(); return x; }");
  }

  // Tests variable aliasing with multiple aliases
  @Test
  public void testInlineAlias_multipleAliases_inlinesCorrectly() {
    test("function f() { var a = 1; var b = a; var c = b; return c; }",
         "function f() { return 1; }");
  }

  // Tests variable separated declaration and assignment
  @Test
  public void testInlineVariable_declarationSeparateFromInit_inlinesCorrectly() {
    test("function f() { var x; x = 10; return x; }",
         "function f() { return 10; }");
  }

  // Tests uninitialized variable inlining as undefined
  @Test
  public void testInlineVariable_uninitialized_inlinesUndefined() {
    test("function f() { var x; return x; }",
         "function f() { return void 0; }");
  }

  // Tests escaping arguments object prevents inlining
  @Test
  public void testInlineVariable_escapedArguments_doesNotInline() {
    testSame("function f() { var a = arguments; g(arguments); return a[0]; }");
  }

  // Tests unescaped arguments property read allows inlining
  @Test
  public void testInlineVariable_unescapedArgumentsRead_inlinesCorrectly() {
    test("function f() { var a = arguments[0]; return a; }",
         "function f() { return arguments[0]; }");
  }

  // Tests this alias inlining inside function
  @Test
  public void testInlineThisAlias_notEscaped_inlinesThis() {
    test("function f() { var self = this; return self.foo(); }",
         "function f() { return this.foo(); }");
  }

  // Tests that side effect between initialization and reference prevents moderate movement
  @Test
  public void testInlineVariable_sideEffectIntervening_doesNotInlining() {
    testSame("function f() { var x = g(); sideEffect(); return x; }");
  }

  // Tests function expression inlining
  @Test
  public void testInlineFunction_functionExpression_inlinesCall() {
    test("function f() { var g = function(a) { return a; }; return g(1); }",
         "function f() { return function(a) { return a; }(1); }");
  }

  // Tests that variable mutated with unary increment/decrement is not inlined
  @Test
  public void testInlineVariable_incrementDecrement_doesNotInline() {
    testSame("function f() { var x = 1; x++; return x; }");
    testSame("function f() { var x = 1; ++x; return x; }");
    testSame("function f() { var x = 1; x--; return x; }");
  }

  // Tests that variable mutated with compound assignment is not inlined
  @Test
  public void testInlineVariable_compoundAssignment_doesNotInline() {
    testSame("function f() { var x = 1; x += 2; return x; }");
  }

  // Tests inlining boolean and null literals
  @Test
  public void testInlineLiterals_booleanAndNull_inlinesMultipleReads() {
    test("function f() { var a = true; var b = false; var c = null; return a && !b && c; }",
         "function f() { return true && !false && null; }");
  }

  // Tests inlining inside loop: mutable variable initialized outside loop should not be inlined into loop
  @Test
  public void testInlineVariable_initializedOutsideModifiedInLoop_doesNotInline() {
    testSame("function f() { var x = 0; while (g()) { x = x + 1; } return x; }");
  }

  // Tests inlining across conditional branching if side effects are present
  @Test
  public void testInlineVariable_sideEffectInsideBranch_doesNotInlineAcrossBranch() {
    testSame("function f(cond) { var x = g(); if (cond) { return 0; } return x; }");
  }

  // Tests inlining inside catch block
  @Test
  public void testInlineVariable_catchBlockParameterShadow_doesNotInlineParameter() {
    test("function f() { var x = 1; try { throw 2; } catch (x) { return x; } }",
         "function f() { try { throw 2; } catch (x) { return x; } }");
  }

  // Tests that variable modified inside closure is not inlined
  @Test
  public void testInlineVariable_modifiedInsideClosure_doesNotInline() {
    testSame("function f() { var x = 1; function inner() { x = 2; } inner(); return x; }");
  }

  // Tests inlining of regex literal
  @Test
  public void testInlineRegexLiteral_singleUse_inlinesCorrectly() {
    test("function f(s) { var re = /abc/g; return re.test(s); }",
         "function f(s) { return /abc/g.test(s); }");
  }

  // Tests inlining of object and array literals with single read
  @Test
  public void testInlineComplexLiteral_singleRead_inlinesCorrectly() {
    test("function f() { var obj = {a: 1, b: 2}; return obj.a; }",
         "function f() { return {a: 1, b: 2}.a; }");
    test("function f() { var arr = [1, 2, 3]; return arr[0]; }",
         "function f() { return [1, 2, 3][0]; }");
  }

  // Tests that multiple reads of mutable object/array literals are not duplicated
  @Test
  public void testInlineComplexLiteral_multipleReads_doesNotInlineDuplicate() {
    testSame("function f() { var obj = {a: 1}; return obj.a + obj.a; }");
    testSame("function f() { var arr = [1, 2]; return arr[0] + arr[1]; }");
  }

  // Tests self-referential / recursive variable definitions
  @Test
  public void testInlineVariable_recursiveSelfReference_doesNotInline() {
    testSame("function f() { var f = function() { return f(); }; return f; }");
  }

  // Tests inlining in conditional operator (ternary)
  @Test
  public void testInlineVariable_inTernary_inlinesCorrectly() {
    test("function f(cond) { var a = 1; var b = 2; return cond ? a : b; }",
         "function f(cond) { return cond ? 1 : 2; }");
  }
}