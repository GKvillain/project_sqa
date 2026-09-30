package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import org.junit.Test;

/**
 * JUnit 4 tests for RemoveUnusedVars.
 * Focuses on removing unused globals/locals, function arguments,
 * and handling of property assignments and inheritance calls.
 */
public class RemoveUnusedVarsTest {

  /** Helper to remove whitespace for relaxed comparison. */
  private static String normalize(String s) {
    return s.replaceAll("\\s+", "");
  }

  private static void assertResult(String expected, String actual) {
    assertEquals(normalize(expected), normalize(actual));
  }

  /** Runs RemoveUnusedVars on the given code with the given configuration. */
  private String process(
      String code,
      boolean removeGlobals,
      boolean preserveFunctionExpressionNames,
      boolean modifyCallSites) {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    Node externs = compiler.parseSyntheticCode("externs", "");
    Node root = compiler.parseSyntheticCode("test", code);
    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);
    RemoveUnusedVars pass =
        new RemoveUnusedVars(
            compiler, removeGlobals, preserveFunctionExpressionNames, modifyCallSites);
    pass.process(externs, root);
    return compiler.toSource(root);
  }

  // Tests removing an unused global variable when removeGlobals is true.
  @Test
  public void testRemoveGlobalVar_unreferenced_removesVar() {
    String result = process("var unused = 1;", true, false, false);
    assertTrue(result.trim().isEmpty());
  }

  // Tests that a global variable is kept when removeGlobals is false.
  @Test
  public void testKeepGlobalVar_whenRemoveGlobalsFalse_keepsVar() {
    String result = process("var unused = 1;", false, false, false);
    assertResult("var unused=1;", result);
  }

  // Tests that an unused local variable inside a referenced function is removed.
  @Test
  public void testRemoveLocalVar_unreferenced_removesVar() {
    String result = process("function f() { var x = 1; } f();", true, false, false);
    assertResult("function f(){} f();", result);
  }

  // Tests that a referenced local variable is not removed.
  @Test
  public void testKeepReferencedLocalVar_keepsIt() {
    String result = process("function f() { var x = 1; return x; } f();", true, false, false);
    assertResult("function f(){var x=1;return x;} f();", result);
  }

  // Tests that the last unused function argument is removed when call sites are not modified.
  @Test
  public void testRemoveUnusedFunctionArg_lastArg_removedFromFunctionDefinition() {
    String result = process("function f(a,b){ return a; } f(1,2);", true, false, false);
    assertResult("function f(a){ return a;} f(1,2);", result);
  }

  // Tests that unused arguments are removed from call sites when modifyCallSites is true.
  @Test
  public void testRemoveUnusedFunctionArg_withModifyCallSites_removesArgFromCallSites() {
    String result = process("function f(a,b){ return a; } f(1,2);", true, false, true);
    assertResult("function f(a){ return a;} f(1);", result);
  }

  // Tests that used function arguments are kept.
  @Test
  public void testKeepUsedFunctionArg_notRemoved() {
    String result = process("function f(a,b){ return a+b; } f(1,2);", true, false, false);
    assertResult("function f(a,b){ return a+b;} f(1,2);", result);
  }

  // Tests that an unused function declaration is removed.
  @Test
  public void testRemoveUnusedFunctionDeclaration_removesFunction() {
    String result = process("function f(){}", true, false, false);
    assertTrue(result.trim().isEmpty());
  }

  // Tests that a simple assignment to an unused variable is completely removed.
  @Test
  public void testRemoveAssignToUnusedVar_removesVarAndAssign() {
    String result = process("var a=1; a=2;", true, false, false);
    assertTrue(result.trim().isEmpty());
  }

  // Tests that an assignment with side effects is replaced by the RHS expression.
  @Test
  public void testAssignWithSideEffect_replacedWithSideEffectExpression() {
    String result = process("function foo(){} var a=1; a = foo();", true, false, false);
    assertResult("function foo(){} foo();", result);
  }

  // Tests that a property assignment to an unknown value keeps the variable alive.
  @Test
  public void testPropertyAssignToUnknownValue_keepsVariable() {
    String code = "function foo() { return {}; } var x = foo(); x.bar = 1;";
    String result = process(code, true, false, false);
    assertResult("function foo(){ return {}; } var x=foo(); x.bar=1;", result);
  }

  // Tests that a property assignment to a literal value is removed safely.
  @Test
  public void testPropertyAssignToLiteralValue_removesVariableAndAssign() {
    String code = "var x = {}; x.foo = 3;";
    String result = process(code, true, false, false);
    assertTrue(result.trim().isEmpty());
  }

  // Tests that a goog.inherits call is removed when the subclass is unused.
  @Test
  public void testGoogInherits_whenSubclassUnused_removesCallAndClasses() {
    String code = "var A = function(){}; var B = function(){}; goog.inherits(A, B);";
    String result = process(code, true, false, false);
    assertTrue(result.trim().isEmpty());
  }

  // Tests that a call through .apply is not modified when modifyCallSites is true.
  @Test
  public void testFunctionApply_doesNotModifyCallSites() {
    String code = "function f(a,b){ return a; } f.apply(null, [1,2]);";
    String result = process(code, true, false, true);
    assertResult("function f(a,b){return a;} f.apply(null,[1,2]);", result);
  }
}