package com.google.javascript.jscomp;

import static org.junit.Assert.*;
import org.junit.Test;
import com.google.javascript.rhino.Node;

public class FlowSensitiveInlineVariablesTest {

  private String runInlinePass(String code) {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    Node root = compiler.parseTestCode(code);
    new FlowSensitiveInlineVariables(compiler).process(null, root);
    return compiler.toSource();
  }

  private void assertInlined(String code, String fragment) {
    String result = runInlinePass(code);
    assertFalse("Expected variable removed, but was: " + result,
        result.contains("var x"));
    assertTrue("Expected output to contain: " + fragment + ", but was: " + result,
        result.contains(fragment));
  }

  private void assertNotInlined(String code, String fragment) {
    String result = runInlinePass(code);
    assertTrue("Expected variable to remain, but was: " + result,
        result.contains("var x"));
    assertTrue("Expected output to contain: " + fragment + ", but was: " + result,
        result.contains(fragment));
  }

  // Tests normal inlining of a simple local variable.
  @Test
  public void testSimpleVariable_shouldBeInlined() {
    assertInlined("function f() { var x = 1; print(x); }", "print(1)");
  }

  // Tests inlining when the use is inside an if branch.
  @Test
  public void testUseInsideIf_shouldBeInlined() {
    assertInlined("function f() { var x = 1; if (a) { print(x); } }", "print(1)");
  }

  // Tests that a function parameter is never inlined.
  @Test
  public void testParameter_shouldNotBeInlined() {
    String result = runInlinePass("function f(x) { print(x); }");
    assertFalse("Parameter should not be removed as var", result.contains("var x"));
    assertTrue("Expected parameter use to remain", result.contains("print(x)"));
  }

  // Tests that a variable with multiple uses is not inlined.
  @Test
  public void testMultipleUses_shouldNotBeInlined() {
    assertNotInlined("function f() { var x = 1; print(x); print(x); }", "print(x)");
  }

  // Tests that a variable used inside a loop is not inlined.
  @Test
  public void testUseInLoop_shouldNotBeInlined() {
    assertNotInlined("function f() { var x = 1; while (g()) { print(x); } }", "print(x)");
  }

  // Tests that a side-effect call between definition and use prevents inlining.
  @Test
  public void testSideEffectBetweenDefAndUse_shouldNotBeInlined() {
    assertNotInlined("function f() { var x = 1; g(); print(x); }", "print(x)");
  }

  // Tests that a side-effectful right-hand side prevents inlining.
  @Test
  public void testRhsWithSideEffect_shouldNotBeInlined() {
    assertNotInlined("function f() { var x = g(); print(x); }", "print(x)");
  }

  // Tests that a GETPROP expression on the right-hand side prevents inlining.
  @Test
  public void testRhsWithGetProp_shouldNotBeInlined() {
    assertNotInlined("function f() { var x = a.b; print(x); }", "print(x)");
  }

  // Tests that a NEW expression on the right-hand side prevents inlining.
  @Test
  public void testRhsWithNew_shouldNotBeInlined() {
    assertNotInlined("function f() { var x = new Foo(); print(x); }", "print(x)");
  }

  // Tests that an assignment used as an R-value prevents inlining.
  @Test
  public void testAssignmentAsRValue_shouldNotBeInlined() {
    assertNotInlined("function f() { var x, y; y = x = 1; print(x); }", "print(x)");
  }

  // Tests that an exported-looking variable name is not inlined.
  @Test
  public void testExportedName_shouldNotBeInlined() {
    String result = runInlinePass("function f() { var _x = 1; print(_x); }");
    assertTrue("Expected exported variable to remain", result.contains("var _x"));
    assertTrue("Expected exported use to remain", result.contains("print(_x)"));
  }

  // Tests that a side-effect in the condition path prevents inlining.
  @Test
  public void testSideEffectInCondition_shouldNotBeInlined() {
    assertNotInlined("function f() { var x = 1; if (g()) { print(x); } }", "print(x)");
  }
}