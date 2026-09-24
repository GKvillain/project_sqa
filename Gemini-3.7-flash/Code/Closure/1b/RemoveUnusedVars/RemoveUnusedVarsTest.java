package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

public class RemoveUnusedVarsTest extends CompilerTestCase {

  private boolean removeGlobals = true;
  private boolean preserveFunctionExpressionNames = false;
  private boolean modifyCallSites = false;

  public RemoveUnusedVarsTest() {
    super();
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new RemoveUnusedVars(
        compiler, removeGlobals, preserveFunctionExpressionNames,
        modifyCallSites);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    removeGlobals = true;
    preserveFunctionExpressionNames = false;
    modifyCallSites = false;
  }

  // Tests defect 1b: Unreferenced function args should not be removed when removeGlobals is false
  @Test
  public void testRemoveUnreferencedFunctionArgs_removeGlobalsFalse_preservesArgs() {
    removeGlobals = false;
    testSame("function f(a, b) {}");
  }

  // Tests removal of trailing unreferenced function args when removeGlobals is true
  @Test
  public void testRemoveUnreferencedFunctionArgs_removeGlobalsTrue_removesTrailingUnusedArgs() {
    removeGlobals = true;
    test("function f(a, b) { alert(a); } f(1);", "function f(a) { alert(a); } f(1);");
  }

  // Tests removal of unreferenced global variables when removeGlobals is true
  @Test
  public void testProcess_unusedGlobalVar_removed() {
    removeGlobals = true;
    test("var x = 1;", "");
  }

  // Tests preservation of unreferenced global variables when removeGlobals is false
  @Test
  public void testProcess_unusedGlobalVar_preservedWhenRemoveGlobalsFalse() {
    removeGlobals = false;
    testSame("var x = 1;");
  }

  // Tests removal of unreferenced local variables inside a function
  @Test
  public void testProcess_unusedLocalVar_removed() {
    test("function f() { var x = 1; } f();", "function f() {} f();");
  }

  // Tests removal of unreferenced global function declaration
  @Test
  public void testProcess_unusedGlobalFunction_removed() {
    removeGlobals = true;
    test("function unused() {}", "");
  }

  // Tests side-effect initializers retain the side-effect expression
  @Test
  public void testProcess_sideEffectInitializer_leavesCall() {
    test("var x = foo();", "foo();");
  }

  // Tests multi-var declarations remove only the unused name
  @Test
  public void testProcess_multiVarDeclaration_removesOnlyUnused() {
    test("var a = 1, b = 2; alert(b);", "var b = 2; alert(b);");
  }

  // Tests preservation of function expression name when preserveFunctionExpressionNames is true
  @Test
  public void testProcess_preserveFunctionExpressionNames_true() {
    preserveFunctionExpressionNames = true;
    testSame("var f = function g() {}; f();");
  }

  // Tests removal of function expression name when preserveFunctionExpressionNames is false
  @Test
  public void testProcess_preserveFunctionExpressionNames_false() {
    preserveFunctionExpressionNames = false;
    test("var f = function g() {}; f();", "var f = function() {}; f();");
  }

  // Tests that accessing the 'arguments' object marks all parameters as referenced
  @Test
  public void testProcess_argumentsEscaped_preservesAllParameters() {
    testSame("function f(a, b) { return arguments; } f();");
  }

  // Tests removal of unreferenced variable with property assignments
  @Test
  public void testProcess_propertyAssignToUnusedVar_removed() {
    test("var x = {}; x.a = 1; x.b = 2;", "");
  }

  // Tests that property assignments to unknown/aliased values are preserved
  @Test
  public void testProcess_propertyAssignToAliasedVar_preserved() {
    testSame("var x = foo(); x.a = 1;");
  }

  // Tests for-in variable declarations are preserved
  @Test
  public void testProcess_forInVar_preserved() {
    testSame("for (var prop in obj) {}");
  }

  // Tests call-site parameter optimization when modifyCallSites is true
  @Test
  public void testModifyCallSites_unusedParameter_removedFromCallSite() {
    modifyCallSites = true;
    test("function f(a, b) { return a; } f(1, 2);",
         "function f(a) { return a; } f(1);");
  }

  // Tests inheritance defining call (goog.inherits) removal for unused subclass
  @Test
  public void testProcess_classDefiningCalls_removedForUnusedSubclass() {
    removeGlobals = true;
    test("function Super() {} function Sub() {} goog.inherits(Sub, Super);", "");
  }
}