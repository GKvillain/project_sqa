package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

public class FlowSensitiveInlineVariablesTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new FlowSensitiveInlineVariables(compiler);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests inlining of simple variable declaration with literal value
  @Test
  public void testProcess_simpleVarDeclaration_inlinesLiteral() {
    test("function f() { var x = 1; return x; }",
         "function f() { return 1; }");
  }

  // Tests inlining of simple assignment statement with literal value
  @Test
  public void testProcess_simpleAssignment_inlinesLiteral() {
    test("function f() { var x; x = 1; return x; }",
         "function f() { return 1; }");
  }

  // Tests false branch for global scope where inlining should be skipped
  @Test
  public void testProcess_globalScope_doesNotInline() {
    testSame("var x = 1; x;");
  }

  // Tests false branch when a variable has multiple uses
  @Test
  public void testProcess_multipleUses_doesNotInline() {
    testSame("function f() { var x = 1; return x + x; }");
  }

  // Tests false branch when variable use is within a loop
  @Test
  public void testProcess_useWithinLoop_doesNotInline() {
    testSame("function f() { var x = 1; while (true) { return x; } }");
  }

  // Tests false branch when RHS contains property access (GETPROP)
  @Test
  public void testProcess_rhsHasGetProp_doesNotInline() {
    testSame("function f(a) { var x = a.b; return x; }");
  }

  // Tests false branch when RHS contains element access (GETELEM)
  @Test
  public void testProcess_rhsHasGetElem_doesNotInline() {
    testSame("function f(a) { var x = a[0]; return x; }");
  }

  // Tests false branch when RHS contains array literal (ARRAYLIT)
  @Test
  public void testProcess_rhsHasArrayLiteral_doesNotInline() {
    testSame("function f() { var x = [1, 2]; return x; }");
  }

  // Tests false branch when RHS contains object literal (OBJECTLIT)
  @Test
  public void testProcess_rhsHasObjectLiteral_doesNotInline() {
    testSame("function f() { var x = {a: 1}; return x; }");
  }

  // Tests false branch when RHS contains regular expression literal (REGEXP)
  @Test
  public void testProcess_rhsHasRegExpLiteral_doesNotInline() {
    testSame("function f() { var x = /abc/; return x; }");
  }

  // Tests false branch when RHS contains constructor call (NEW)
  @Test
  public void testProcess_rhsHasNewExpression_doesNotInline() {
    testSame("function f() { var x = new Object(); return x; }");
  }

  // Tests false branch when RHS may have side effects
  @Test
  public void testProcess_rhsMayHaveSideEffects_doesNotInline() {
    testSame("function f() { var x = g(); return x; }");
  }

  // Tests false branch when side effect exists to the right of definition
  @Test
  public void testProcess_sideEffectRightOfDef_doesNotInline() {
    testSame("function f() { var x; (x = 1), g(); return x; }");
  }

  // Tests false branch when side effect exists to the left of use
  @Test
  public void testProcess_sideEffectLeftOfUse_doesNotInline() {
    testSame("function f() { var x = 1; g(), x; }");
  }

  // Tests false branch when side effect occurs on path between def and use
  @Test
  public void testProcess_sideEffectBetweenDefAndUse_doesNotInline() {
    testSame("function f() { var x = 1; g(); return x; }");
  }

  // Tests inlining of labeled assignment and removal of label node
  @Test
  public void testProcess_labeledAssignment_inlinesAndRemovesLabel() {
    test("function f() { var x; lab: x = 1; return x; }",
         "function f() { return 1; }");
  }

  // Tests false branch when assignment is used as R-Value
  @Test
  public void testProcess_assignUsedAsRValue_doesNotInline() {
    testSame("function f() { var x, y; y = (x = 1); return x; }");
  }

  // Tests inlining across consecutive statements without intervening side effects
  @Test
  public void testProcess_consecutiveStatements_inlinesVariable() {
    test("function f() { var x = 1; var y = 2; return x + y; }",
         "function f() { var x = 1; return x + 2; }");
  }

  // Tests false branch where definition is a function parameter
  @Test
  public void testProcess_functionParameterDefinition_doesNotInline() {
    testSame("function f(x) { return x; }");
  }
}