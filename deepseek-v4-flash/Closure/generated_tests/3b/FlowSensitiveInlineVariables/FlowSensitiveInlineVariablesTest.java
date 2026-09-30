package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * JUnit 4 test class for FlowSensitiveInlineVariables, targeting
 * normal, boundary, and defect-revealing cases.
 */
public class FlowSensitiveInlineVariablesTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new FlowSensitiveInlineVariables(compiler);
  }

  // Tests normal simple inlining of a variable defined with assignment.
  @Test
  public void testInlineSimple() {
    test("function f() { var x = 1; return x; }",
         "function f() { return 1; }");
  }

  // Tests inlining a variable defined via expression assignment.
  @Test
  public void testInlineExprAssign() {
    test("function f() { var x; x = 2; return x; }",
         "function f() { return 2; }");
  }

  // Tests that side effect between definition and use prevents inlining.
  @Test
  public void testSideEffectBetween() {
    testSame("function f() { var x = 1; alert(''); return x; }");
  }

  // Tests that multiple uses of variable prevent inlining.
  @Test
  public void testMultipleUses() {
    testSame("function f() { var x = 1; f(x); return x; }");
  }

  // Tests that a use inside a loop prevents inlining.
  @Test
  public void testUseInLoop() {
    testSame("function f() { var x = 1; while(true) { return x; } }");
  }

  // Tests that a parameter is never inlined.
  @Test
  public void testParameter() {
    testSame("function f(x) { return x; }");
  }

  // Tests that an exported variable is not inlined.
  @Test
  public void testExported() {
    testSame("function f() { var x = 1; this.x = x; return x; }");
  }

  // Tests that a definition with a function call (side effect) is not inlined.
  @Test
  public void testDefHasSideEffect() {
    testSame("function f() { var x = foo(); return x; }");
  }

  // Tests that a definition of a new object is not inlined.
  @Test
  public void testDefNewObject() {
    testSame("function f() { var x = new Object(); return x; }");
  }

  // Tests that a definition with GETELEM is not inlined.
  @Test
  public void testDefGetElem() {
    testSame("function f() { var x = a[0]; return x; }");
  }

  // Tests that a definition with GETPROP is not inlined.
  @Test
  public void testDefGetProp() {
    testSame("function f() { var x = a.b; return x; }");
  }

  // Tests that a definition with ARRAYLIT is not inlined.
  @Test
  public void testDefArrayLit() {
    testSame("function f() { var x = [1,2]; return x; }");
  }

  // Tests that a definition with OBJECTLIT is not inlined.
  @Test
  public void testDefObjectLit() {
    testSame("function f() { var x = {a:1}; return x; }");
  }

  // Tests that a definition with REGEXP is not inlined.
  @Test
  public void testDefRegExp() {
    testSame("function f() { var x = /abc/; return x; }");
  }

  // Tests that a definition with NEW (constructor call) is not inlined.
  @Test
  public void testDefNew() {
    testSame("function f() { var x = new Foo(); return x; }");
  }

  // Tests that a non-expression assignment (e.g., inside an if condition) is not inlined.
  @Test
  public void testDefNotExprAssign() {
    testSame("function f() { var x; if (x = 1) { return x; } }");
  }

  // Tests that multiple uses of variable in the same CFG node (e.g., return x + x) prevents inlining.
  @Test
  public void testMultipleUseInSameCFGNode() {
    testSame("function f() { var x = 1; return x + x; }");
  }

  // Tests that a variable used as LHS of assignment is not considered a read and thus not inlined.
  @Test
  public void testVarUsedAsLHS() {
    testSame("function f() { var x = 1; x = 2; return x; }");
  }

  // Tests that side effect on a branch that does not reach the use still prevents inlining.
  @Test
  public void testSideEffectOnOtherBranch() {
    testSame("function f() { var x = 1; if (a) { alert(1); } return x; }");
  }

  // Tests that global scope variables are not processed.
  @Test
  public void testGlobalScope() {
    testSame("var x = 1; return x;");
  }

  // Tests that a definition whose RHS may have side effects (assignment to property) prevents inlining.
  @Test
  public void testDefRHSMayHaveSideEffects() {
    testSame("function f() { var x = a.b = 1; return x; }");
  }
}