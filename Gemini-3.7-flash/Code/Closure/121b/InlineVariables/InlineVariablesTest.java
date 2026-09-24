package com.google.javascript.jscomp;

/**
 * Unit tests for {@link InlineVariables}.
 */
public class InlineVariablesTest extends CompilerTestCase {

  private InlineVariables.Mode mode = InlineVariables.Mode.ALL;
  private boolean inlineAllStrings = false;

  public InlineVariablesTest() {
    enableNormalize();
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new InlineVariables(compiler, mode, inlineAllStrings);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    mode = InlineVariables.Mode.ALL;
    inlineAllStrings = false;
    enableNormalize();
  }

  // Tests inlining a single local variable read once
  public void testInline_singleLocalVariable_inlinesSuccessfully() {
    test("function f() { var x = 1; return x; }",
         "function f() { return 1; }");
  }

  // Tests inlining an immutable variable referenced multiple times
  public void testInline_immutableVariableMultipleRefs_inlinesAll() {
    test("function f() { var x = 1; return x + x; }",
         "function f() { return 1 + 1; }");
  }

  // Tests that uninitialized local variable is inlined as void 0
  public void testInline_uninitializedVariable_inlinesUndefined() {
    test("function f() { var x; return x + x; }",
         "function f() { return void 0 + void 0; }");
  }

  // Tests variable declared and assigned in separate statements
  public void testInline_separateDeclarationAndAssignment_inlinesValue() {
    test("function f() { var x; x = 1; return x; }",
         "function f() { return 1; }");
  }

  // Tests that variable assigned multiple times is not inlined
  public void testInline_variableAssignedMultipleTimes_doesNotInline() {
    testSame("function f() { var x = 1; x = 2; return x; }");
  }

  // Tests that method call context is preserved and not inlined to change this-binding
  public void testInline_methodCallProperty_doesNotInlineToPreserveThis() {
    testSame("function f(o) { var m = o.bar; m(); }");
  }

  // Tests that property access as argument can be inlined safely
  public void testInline_propertyAccessAsArgument_inlinesSafely() {
    test("function f(o, g) { var m = o.bar; g(m); }",
         "function f(o, g) { g(o.bar); }");
  }

  // Tests LOCALS_ONLY mode does not inline global variables
  public void testMode_localsOnly_doesNotInlineGlobals() {
    mode = InlineVariables.Mode.LOCALS_ONLY;
    testSame("var x = 1; function f() { return x; }");
  }

  // Tests LOCALS_ONLY mode inlines local variables
  public void testMode_localsOnly_inlinesLocals() {
    mode = InlineVariables.Mode.LOCALS_ONLY;
    test("function f() { var x = 1; return x; }",
         "function f() { return 1; }");
  }

  // Tests CONSTANTS_ONLY mode does not inline non-constant variables
  public void testMode_constantsOnly_doesNotInlineNonConstants() {
    mode = InlineVariables.Mode.CONSTANTS_ONLY;
    testSame("function f() { var x = 1; return x; }");
  }

  // Tests CONSTANTS_ONLY mode inlines variables marked with @const
  public void testMode_constantsOnly_inlinesDeclaredConstants() {
    mode = InlineVariables.Mode.CONSTANTS_ONLY;
    test("/** @const */ var X = 1; function f() { return X; }",
         "var X = 1; function f() { return 1; }");
  }

  // Tests string inlining heuristic when inlineAllStrings is false
  public void testInline_stringHeuristic_doesNotInlineLargeStringMultipleTimes() {
    inlineAllStrings = false;
    testSame("var X = 'a_very_long_string_constant_value'; " +
             "function f() { return X + X + X + X; }");
  }

  // Tests string inlining when inlineAllStrings is true
  public void testInline_inlineAllStringsTrue_inlinesLargeString() {
    inlineAllStrings = true;
    test("/** @const */ var X = 'a_very_long_string_constant_value'; " +
         "function f() { return X; }",
         "var X = 'a_very_long_string_constant_value'; " +
         "function f() { return 'a_very_long_string_constant_value'; }");
  }

  // Tests aliasing candidate inlining when safe
  public void testInline_aliasCandidate_inlinesCorrectly() {
    test("function f(a) { var b = a; var c = b; return c; }",
         "function f(a) { var b = a; return b; }");
  }

  // Tests that aliasing is not performed when arguments object may be modified
  public void testInline_argumentsModified_doesNotInlineAlias() {
    testSame("function f(x) { arguments[0] = 2; var a = x; var b = a; return b; }");
  }

  // Tests that aliasing across reassignment is handled correctly (Defects4J bug 121)
  public void testInline_aliasCandidateReassigned_doesNotIncorrectlyInline() {
    testSame("function f(a) { var b = a; var c = b; b = 1; return c; }");
  }

  // Tests that aliasing with multiple reads doesn't inline unsafe references
  public void testInline_aliasMultipleReferences_maintainsSemantics() {
    testSame("function f(d) { var e = d.bar; var x = e; var y = function() { return e; }; var z = function() { return x; }; return [y, z]; }");
  }

  // Tests function expression inlining
  public void testInline_functionExpression_inlinesIntoUse() {
    test("function f() { var fn = function(a) { return a; }; return fn(1); }",
         "function f() { return (function(a) { return a; })(1); }");
  }

  // Tests this alias inlining when this is immutable and not escaped
  public void testInline_thisAlias_inlinesSafely() {
    test("function f() { var self = this; return self.foo; }",
         "function f() { return this.foo; }");
  }

  // Tests that variable inside a loop modified across iterations is not inlined
  public void testInline_variableInLoop_doesNotInlineIfModified() {
    testSame("function f() { var x = 0; while (true) { var y = x; x++; if (y) break; } }");
  }

  // Tests inlining with conditional branch
  public void testInline_conditionalBranch_inlinesWhenDeclaredInBranch() {
    test("function f(cond) { if (cond) { var x = 1; return x; } return 0; }",
         "function f(cond) { if (cond) { return 1; } return 0; }");
  }

  // Tests that variable read across try-catch block boundary is handled safely
  public void testInline_tryCatch_preservesExceptionHandling() {
    testSame("function f() { var x = g(); try { h(); } catch (e) { return x; } }");
  }

  // Tests recursive or self-referential variable is not inlined incorrectly
  public void testInline_selfReferentialDeclaration_doesNotInline() {
    testSame("function f() { var x = x + 1; return x; }");
  }
}