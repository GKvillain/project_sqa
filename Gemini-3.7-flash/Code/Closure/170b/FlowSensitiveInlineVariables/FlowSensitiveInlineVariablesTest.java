package com.google.javascript.jscomp;

import org.junit.Test;

public class FlowSensitiveInlineVariablesTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new FlowSensitiveInlineVariables(compiler);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests basic variable declaration inlining
  @Test
  public void testInlineVariable_simpleVarDeclaration_inlinesValue() {
    test("function f() { var x = 1; return x; }",
         "function f() { return 1; }");
  }

  // Tests assignment inlining to simple return
  @Test
  public void testInlineVariable_simpleAssignment_inlinesValue() {
    test("function f() { var x; x = 1; return x; }",
         "function f() { var x; return 1; }");
  }

  // Tests multiple sequential assignments where earlier definition reaches use
  @Test
  public void testInlineVariable_multipleAssignments_inlinesLastReachingDef() {
    test("function f() { var x = 1; x = 2; return x; }",
         "function f() { var x = 1; return 2; }");
  }

  // Tests global scope handling (global variables must not be inlined)
  @Test
  public void testEnterScope_globalScope_doesNotInline() {
    testSame("var x = 1; function f() { return x; }");
  }

  // Tests candidate within loop is not inlined
  @Test
  public void testInlineVariable_useWithinLoop_doesNotInline() {
    testSame("function f() { var x = 1; while (true) { alert(x); } }");
  }

  // Tests expression with GETPROP is not inlined
  @Test
  public void testInlineVariable_rhsHasGetProp_doesNotInline() {
    testSame("function f(a) { var x = a.b; a.b = 2; return x; }");
  }

  // Tests expression with GETELEM is not inlined
  @Test
  public void testInlineVariable_rhsHasGetElem_doesNotInline() {
    testSame("function f(a, i) { var x = a[i]; a[i] = 2; return x; }");
  }

  // Tests object literal creation is not inlined
  @Test
  public void testInlineVariable_rhsIsObjectLit_doesNotInline() {
    testSame("function f() { var x = {}; return x; }");
  }

  // Tests array literal creation is not inlined
  @Test
  public void testInlineVariable_rhsIsArrayLit_doesNotInline() {
    testSame("function f() { var x = [1, 2]; return x; }");
  }

  // Tests catch variable reference is not inlined
  @Test
  public void testInlineVariable_referenceInCatch_doesNotInline() {
    testSame("function f() { try {} catch (e) { var x = e; return x; } }");
  }

  // Tests expression with side effect between definition and use
  @Test
  public void testInlineVariable_sideEffectBetweenNodes_doesNotInline() {
    testSame("function f(b) { var x = b; modify(b); return x; }");
  }

  // Tests variable with multiple uses in same CFG node
  @Test
  public void testInlineVariable_multipleUsesInCfgNode_doesNotInline() {
    testSame("function f() { var x = 1; return x + x; }");
  }

  // Tests variable used in parameter list is not candidate
  @Test
  public void testInlineVariable_paramListUsage_doesNotInline() {
    testSame("function f(x) { return x; }");
  }

  // Tests bug 170: conditional expression with assignment in branch
  @Test
  public void testInlineVariable_conditionalWithAssignment_doesNotInlineIncorrectly() {
    testSame("function f(a) { var x; x = 1; return (a == 2) ? (x = 2) : x; }");
  }

  // Tests chained assignment special case
  @Test
  public void testInlineVariable_chainedAssignment_inlinesCorrectly() {
    test("function f() { var x = 1; var y = x; return y; }",
         "function f() { var y = 1; return y; }");
  }

  // Tests labeled statement with assignment
  @Test
  public void testInlineVariable_labeledAssignment_inlinesValue() {
    test("function f() { var x; label: x = 1; return x; }",
         "function f() { var x; return 1; }");
  }

  // Tests increment operator does not inline
  @Test
  public void testInlineVariable_incOperator_doesNotInline() {
    testSame("function f() { var x = 1; x++; return x; }");
  }

  // Tests decrement operator does not inline
  @Test
  public void testInlineVariable_decOperator_doesNotInline() {
    testSame("function f() { var x = 1; x--; return x; }");
  }
}