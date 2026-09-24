package com.google.javascript.jscomp;

import org.junit.Test;

/**
 * Unit tests for {@link FlowSensitiveInlineVariables}.
 */
public class FlowSensitiveInlineVariablesTest extends CompilerTestCase {

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new FlowSensitiveInlineVariables(compiler);
  }

  // Tests simple variable declaration and use inlining
  @Test
  public void testProcess_simpleVarDeclaration_inlinesVariable() {
    test("function f() { var x = 1; return x; }",
         "function f() { return 1; }");
  }

  // Tests assignment definition inlining
  @Test
  public void testProcess_assignExpression_inlinesVariable() {
    test("function f() { var x; x = 1; return x; }",
         "function f() { var x; return 1; }");
  }

  // Tests multiple uses of a variable in use CFG node prevents inlining
  @Test
  public void testProcess_multipleUsesInSingleNode_doesNotInlining() {
    testSame("function f() { var x = 1; return x + x; }");
  }

  // Tests multiple uses across different nodes prevents inlining
  @Test
  public void testProcess_multipleUsesAcrossNodes_doesNotInlining() {
    testSame("function f() { var x = 1; print(x); return x; }");
  }

  // Tests that global scope variables are ignored
  @Test
  public void testProcess_globalScopeVariable_doesNotInlining() {
    testSame("var x = 1; print(x);");
  }

  // Tests that variable used within a loop is not inlined
  @Test
  public void testProcess_useWithinLoop_doesNotInlining() {
    testSame("function f() { var x = 1; while (true) { print(x); } }");
  }

  // Tests that function parameters are not inlined as definition
  @Test
  public void testProcess_parameterDefinition_doesNotInlining() {
    testSame("function f(x) { return x; }");
  }

  // Tests that definitions with GETPROP (property access) are not inlined
  @Test
  public void testProcess_rhsGetProp_doesNotInlining() {
    testSame("function f() { var x = a.b; return x; }");
  }

  // Tests that definitions with GETELEM (array index) are not inlined
  @Test
  public void testProcess_rhsGetElem_doesNotInlining() {
    testSame("function f() { var x = a[0]; return x; }");
  }

  // Tests that definitions creating Array literal are not inlined
  @Test
  public void testProcess_rhsArrayLiteral_doesNotInlining() {
    testSame("function f() { var x = [1, 2]; return x; }");
  }

  // Tests that definitions creating Object literal are not inlined
  @Test
  public void testProcess_rhsObjectLiteral_doesNotInlining() {
    testSame("function f() { var x = {a: 1}; return x; }");
  }

  // Tests that definitions with NEW expression are not inlined
  @Test
  public void testProcess_rhsNewObject_doesNotInlining() {
    testSame("function f() { var x = new Object(); return x; }");
  }

  // Tests that definitions with REGEXP literal are not inlined
  @Test
  public void testProcess_rhsRegExp_doesNotInlining() {
    testSame("function f() { var x = /abc/; return x; }");
  }

  // Tests that assignment used as R-Value (e.g. in condition) is not inlined
  @Test
  public void testProcess_assignmentAsRValue_doesNotInlining() {
    testSame("function f() { var x; if (x = 1) { return x; } }");
  }

  // Tests that RHS with side effects is not inlined
  @Test
  public void testProcess_rhsWithSideEffects_doesNotInlining() {
    testSame("function f() { var x = g(); return x; }");
  }

  // Tests inlining when assignment is within a labeled statement
  @Test
  public void testProcess_labeledAssignment_inlinesVariable() {
    test("function f() { var x; L: x = 1; return x; }",
         "function f() { var x; return 1; }");
  }

  // Tests side effect along path between non-adjacent statements
  @Test
  public void testProcess_sideEffectBetweenDefAndUse_doesNotInlining() {
    testSame("function f() { var x = a; g(); return x; }");
  }

  // Tests inner function accessing outer scope variable (Issue 157 / regression)
  @Test
  public void testProcess_outerScopeVarDependency_inlinesSafely() {
    test("function f() { " +
         "  var a = 1; " +
         "  return function() { " +
         "    var b = a; " +
         "    return b; " +
         "  }; " +
         "}",
         "function f() { " +
         "  var a = 1; " +
         "  return function() { " +
         "    return a; " +
         "  }; " +
         "}");
  }

  // Tests variable read in assignment target is not treated as pure read
  @Test
  public void testProcess_varUsedAsLhsOfCompoundAssign_doesNotInlining() {
    testSame("function f() { var x = 1; x += 2; return x; }");
  }

  // Tests exported name is not inlined
  @Test
  public void testProcess_exportedName_doesNotInlining() {
    testSame("function f() { var _x = 1; return _x; }");
  }
}