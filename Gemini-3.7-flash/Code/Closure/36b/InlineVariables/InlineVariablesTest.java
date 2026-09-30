package com.google.javascript.jscomp;

public class InlineVariablesTest extends CompilerTestCase {

  private InlineVariables.Mode mode = InlineVariables.Mode.ALL;
  private boolean inlineAllStrings = true;

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
    inlineAllStrings = true;
  }

  // Tests basic inlining of a variable used only once
  public void testInline_singleUseVariable_inlinesValue() {
    test("var x = 1; var y = x + 2;", "var y = 1 + 2;");
  }

  // Tests immutable variable referenced multiple times
  public void testInline_multipleReferencesToImmutable_inlinesAll() {
    test("var x = 1; var y = x; var z = x;", "var y = 1; var z = 1;");
  }

  // Tests variable declared but never initialized inlined as undefined
  public void testInline_uninitializedVariable_inlinesUndefined() {
    test("var x; var y = x; var z = x;", "var y = void 0; var z = void 0;");
  }

  // Tests constants-only mode where non-constants are ignored
  public void testInline_constantsOnlyMode_inlinesOnlyConstants() {
    mode = InlineVariables.Mode.CONSTANTS_ONLY;
    test("var CONST = 5; var nonConst = 6; var a = CONST; var b = nonConst;",
         "var nonConst = 6; var a = 5; var b = nonConst;");
  }

  // Tests locals-only mode where global variables are not inlined
  public void testInline_localsOnlyMode_doesNotInlineGlobals() {
    mode = InlineVariables.Mode.LOCALS_ONLY;
    test("var g = 1; var a = g; function f() { var local = 2; var b = local; }",
         "var g = 1; var a = g; function f() { var b = 2; }");
  }

  // Tests inlining of well-defined alias candidates
  public void testInline_aliasCandidate_inlinesAlias() {
    test("var a = 1; var b = a; var c = b + 1; var d = b + 2;",
         "var c = 1 + 1; var d = 1 + 2;");
  }

  // Tests that variables across different basic blocks/control structures are not inlined
  public void testInline_variableAcrossControlStructures_doesNotInlining() {
    testSame("var x = 1; if (true) { var y = x; }");
  }

  // Tests property access method call context is preserved and not inlined
  public void testInline_getPropCallContext_doesNotInlining() {
    testSame("var a = b.c; a();");
  }

  // Tests property access as argument is inlinable
  public void testInline_getPropAsArgument_inlinesValue() {
    test("var a = b.c; f(a);", "f(b.c);");
  }

  // Tests that modifying arguments disables inlining inside function
  public void testInline_argumentsModified_doesNotInline() {
    testSame("function f(x) { arguments[0] = 2; var a = x; return a; }");
  }

  // Tests function declarations inlining into call
  public void testInline_functionDeclaration_inlinesCorrectly() {
    test("function f() { return 1; } var a = f();", "var a = (function() { return 1; })();");
  }

  // Tests subclass relationship call does not inline class definitions
  public void testInline_subclassRelationshipCall_doesNotInline() {
    testSame("function Super() {} function Sub() {} goog.inherits(Sub, Super);");
  }

  // Tests singleton getter call does not inline constructor definition (Defect 36)
  public void testInline_singletonGetterCall_doesNotInline() {
    testSame("function Foo() {} goog.addSingletonGetter(Foo);");
  }

  // Tests string not worth inlining when inlineAllStrings is false
  public void testInline_longStringNotWorthInlining_doesNotInline() {
    inlineAllStrings = false;
    testSame("var longStr = 'this_is_a_very_long_string_constant'; var a = longStr; var b = longStr; var c = longStr;");
  }

  // Tests declaration and separate assignment inlining
  public void testInline_separateDeclarationAndAssign_inlinesCorrectly() {
    test("var x; x = 1; var y = x;", "var y = 1;");
  }

  // Tests short string is inlined even when inlineAllStrings is false
  public void testInline_shortStringWhenInlineAllStringsFalse_inlines() {
    inlineAllStrings = false;
    test("var s = 'a'; var x = s; var y = s;", "var x = 'a'; var y = 'a';");
  }

  // Tests variables modified after initialization are not inlined
  public void testNoInline_mutatedVariable_doesNotInline() {
    testSame("var x = 1; x++; var y = x;");
    testSame("var x = 1; x = 2; var y = x;");
  }

  // Tests self-referential variables are not incorrectly inlined
  public void testNoInline_selfReference_doesNotInline() {
    testSame("var x = x;");
    testSame("var x = x + 1;");
  }

  // Tests inlining variables within local function scope
  public void testInline_insideFunction_inlinesLocal() {
    test("function f() { var a = 10; var b = a + 5; return b; }",
         "function f() { var b = 10 + 5; return b; }");
  }

  // Tests variables used inside loops are not inlined if modified in loop
  public void testNoInline_variableModifiedInLoop_doesNotInline() {
    testSame("var x = 0; while (foo()) { var y = x; x++; }");
    testSame("for (var i = 0; i < 10; i++) { var x = i; }");
  }

  // Tests catch clause variables are not inlined incorrectly
  public void testNoInline_catchClause_doesNotInline() {
    testSame("try { foo(); } catch (e) { var x = e; bar(x); }");
  }

  // Tests chaining multiple assignments
  public void testInline_chainedAssignments_inlines() {
    test("var a = 1; var b = 2; var c = a + b;", "var c = 1 + 2;");
  }

  // Tests inlining inside switch cases
  public void testInline_switchCase_inlinesInsideCase() {
    test("switch (foo) { case 1: var x = 5; var y = x; break; }",
         "switch (foo) { case 1: var y = 5; break; }");
  }
}