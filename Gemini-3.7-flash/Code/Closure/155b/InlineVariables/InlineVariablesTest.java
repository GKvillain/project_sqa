package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class InlineVariablesTest extends CompilerTestCase {

  private InlineVariables.Mode mode = InlineVariables.Mode.ALL;
  private boolean inlineAllStrings = false;

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new InlineVariables(compiler, mode, inlineAllStrings);
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    mode = InlineVariables.Mode.ALL;
    inlineAllStrings = false;
  }

  // Tests inlining of a simple local variable used once
  @Test
  public void testProcess_simpleLocalVariable_inlinesValue() {
    test("function f() { var x = 1; return x; }",
         "function f() { return 1; }");
  }

  // Tests constants-only mode does not inline non-constant variables
  @Test
  public void testProcess_constantsOnlyModeNonConstant_doesNotInlining() {
    mode = InlineVariables.Mode.CONSTANTS_ONLY;
    testSame("var x = 1; var y = x;");
  }

  // Tests constants-only mode inlines declared constants
  @Test
  public void testProcess_constantsOnlyModeConstant_inlinesConstant() {
    mode = InlineVariables.Mode.CONSTANTS_ONLY;
    test("var CONST_A = 1; var b = CONST_A;",
         "var b = 1;");
  }

  // Tests locals-only mode does not inline global variables
  @Test
  public void testProcess_localsOnlyModeGlobalVar_doesNotInlining() {
    mode = InlineVariables.Mode.LOCALS_ONLY;
    testSame("var x = 1; var y = x;");
  }

  // Tests locals-only mode inlines local variables
  @Test
  public void testProcess_localsOnlyModeLocalVar_inlinesLocalVar() {
    mode = InlineVariables.Mode.LOCALS_ONLY;
    test("function f() { var x = 1; return x; }",
         "function f() { return 1; }");
  }

  // Tests uninitialized local variable is inlined to undefined
  @Test
  public void testProcess_uninitializedVariable_inlinesUndefined() {
    test("function f() { var x; return x; }",
         "function f() { return void 0; }");
  }

  // Tests immutable variable referenced multiple times is inlined to all references
  @Test
  public void testProcess_immutableVariableMultipleRefs_inlinesAll() {
    test("function f() { var x = 1; var y = x; var z = x; }",
         "function f() { var y = 1; var z = 1; }");
  }

  // Tests string not worth inlining when inlineAllStrings is false
  @Test
  public void testProcess_inlineAllStringsFalse_doesNotInlineLongString() {
    inlineAllStrings = false;
    testSame("var a = 'abcdefghijklmnopqrstuvwxyz'; var b = a; var c = a; var d = a;");
  }

  // Tests inlineAllStrings true forces inlining of string variables
  @Test
  public void testProcess_inlineAllStringsTrue_inlinesString() {
    inlineAllStrings = true;
    test("function f() { var a = 'abcdefghijklmnopqrstuvwxyz'; var b = a; }",
         "function f() { var b = 'abcdefghijklmnopqrstuvwxyz'; }");
  }

  // Tests GETPROP variable called as function is not inlined to preserve context
  @Test
  public void testProcess_getPropCallContext_doesNotInlining() {
    testSame("function f(b) { var a = b.c; a(); }");
  }

  // Tests GETPROP variable passed as argument can safely be inlined
  @Test
  public void testProcess_getPropPassedAsArgument_inlinesGetProp() {
    test("function f(b, g) { var a = b.c; g(a); }",
         "function f(b, g) { g(b.c); }");
  }

  // Tests separate variable declaration and assignment
  @Test
  public void testProcess_separateDeclarationAndInit_inlinesValue() {
    test("function f() { var x; x = 1; return x; }",
         "function f() { return 1; }");
  }

  // Tests alias candidate inlining for function parameters
  @Test
  public void testProcess_aliasCandidate_inlinesAlias() {
    test("function f(x) { var y = x; return y; }",
         "function f(x) { return x; }");
  }

  // Tests this alias inlining when this is not escaped
  @Test
  public void testProcess_thisAlias_inlinesThis() {
    test("function f() { var self = this; return self.foo(); }",
         "function f() { return this.foo(); }");
  }

  // Tests variable referenced inside loop across basic blocks is not inlined
  @Test
  public void testProcess_variableUsedInsideLoop_doesNotInlining() {
    testSame("function f() { var x = 1; while (true) { alert(x); } }");
  }

  // Tests subclass defining call is not inlined
  @Test
  public void testProcess_subclassDefinitionCall_doesNotInlining() {
    testSame("function f() { var x = function() {}; goog.inherits(x, Object); }");
  }
}