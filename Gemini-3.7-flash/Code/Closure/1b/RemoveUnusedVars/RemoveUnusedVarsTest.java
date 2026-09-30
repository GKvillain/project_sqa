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
  @Before
  public void setUp() throws Exception {
    super.setUp();
    removeGlobals = true;
    preserveFunctionExpressionNames = false;
    modifyCallSites = false;
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new RemoveUnusedVars(
        compiler, removeGlobals, preserveFunctionExpressionNames, modifyCallSites);
  }

  // Tests removal of simple unused global variable
  @Test
  public void testProcess_unusedGlobalVar_removesVariable() {
    test("var a = 1;", "");
  }

  // Tests referenced variable is kept
  @Test
  public void testProcess_referencedGlobalVar_keepsVariable() {
    test("var a = 1; alert(a);", "var a = 1; alert(a);");
  }

  // Tests when removeGlobals is false, unused global var is preserved
  @Test
  public void testProcess_removeGlobalsFalse_preservesGlobalVar() {
    removeGlobals = false;
    test("var a = 1;", "var a = 1;");
  }

  // Tests unused function declaration removal
  @Test
  public void testProcess_unusedFunctionDeclaration_removesFunction() {
    test("function foo() { return 1; }", "");
  }

  // Tests referenced function declaration is kept
  @Test
  public void testProcess_referencedFunctionDeclaration_keepsFunction() {
    test("function foo() { return 1; } foo();", "function foo() { return 1; } foo();");
  }

  // Tests unused local variable inside a function is removed
  @Test
  public void testProcess_unusedLocalVar_removesLocalVar() {
    test("function foo() { var a = 1; return 2; } foo();",
         "function foo() { return 2; } foo();");
  }

  // Tests variable with side effects in initializer preserves side effect
  @Test
  public void testProcess_unusedVarWithSideEffect_preservesSideEffect() {
    test("var a = alert();", "alert();");
  }

  // Tests multiple variables in single var statement with one unused
  @Test
  public void testProcess_multipleVarsOneUnused_removesOnlyUnused() {
    test("var a = 1, b = 2; alert(b);", "var b = 2; alert(b);");
  }

  // Tests unused function argument removal without call site modification
  @Test
  public void testProcess_unusedFunctionArg_removesTrailingUnusedArg() {
    test("function foo(a, b) { alert(a); } foo(1, 2);",
         "function foo(a) { alert(a); } foo(1, 2);");
  }

  // Tests unused function argument when removeGlobals is false
  @Test
  public void testProcess_unusedFunctionArgWithRemoveGlobalsFalse_removesArg() {
    removeGlobals = false;
    test("function foo(a, b) { alert(a); } foo(1, 2);",
         "function foo(a) { alert(a); } foo(1, 2);");
  }

  // Tests unused function expression name removal
  @Test
  public void testProcess_unusedFunctionExpressionName_removesName() {
    preserveFunctionExpressionNames = false;
    test("var f = function foo() {}; f();",
         "var f = function() {}; f();");
  }

  // Tests preserveFunctionExpressionNames keeps function expression name
  @Test
  public void testProcess_preserveFunctionExpressionNamesTrue_keepsName() {
    preserveFunctionExpressionNames = true;
    test("var f = function foo() {}; f();",
         "var f = function foo() {}; f();");
  }

  // Tests arguments reference in function scope marks all parameters as referenced
  @Test
  public void testProcess_argumentsEscaped_preservesAllParameters() {
    test("function foo(a, b) { return arguments[0]; } foo(1, 2);",
         "function foo(a, b) { return arguments[0]; } foo(1, 2);");
  }

  // Tests unused property assignment on unreferenced object is removed
  @Test
  public void testProcess_unusedPropertyAssign_removesAssign() {
    test("var a = {}; a.b = 1;", "");
  }

  // Tests modifyCallSites removes unused argument across call sites
  @Test
  public void testProcess_modifyCallSites_removesArgFromCalls() {
    modifyCallSites = true;
    test("function foo(a) {} foo(1);",
         "function foo() {} foo();");
  }

  // Tests modifyCallSites replaces unused non-trailing argument with zero
  @Test
  public void testProcess_modifyCallSitesNonTrailingUnused_replacesWithZero() {
    modifyCallSites = true;
    test("function foo(a, b) { alert(b); } foo(1, 2);",
         "function foo(b) { alert(b); } foo(2);");
  }

  // Tests for-in variable is handled correctly
  @Test
  public void testProcess_forInVar_preservesLoopVar() {
    test("var obj = {a: 1}; for (var k in obj) { alert(k); }",
         "var obj = {a: 1}; for (var k in obj) { alert(k); }");
  }

  // Tests chained assignments to unused variables are removed
  @Test
  public void testProcess_chainedAssignsUnused_removesBoth() {
    test("var a; var b; a = b = 1;", "");
  }

  // Tests increment/decrement on unused variable is removed
  @Test
  public void testProcess_unusedIncDec_removesOperation() {
    test("var a = 1; a++; ++a;", "");
  }

  // Tests compound assignment on unused variable preserves side effects of RHS
  @Test
  public void testProcess_compoundAssignmentWithSideEffect_preservesRhsSideEffect() {
    test("var a = 1; a += alert();", "alert();");
  }

  // Tests cascaded unused variable references are all removed
  @Test
  public void testProcess_cascadedUnusedVars_removesAll() {
    test("var a = 1; var b = a; var c = b;", "");
  }

  // Tests assignment to unused variable inside used expression removes assignment target
  @Test
  public void testProcess_unusedVarAssignedInExpression_preservesAssignedValue() {
    test("var a; var b = (a = alert()) + 1; alert(b);",
         "var b = alert() + 1; alert(b);");
  }

  // Tests modifyCallSites extracts side-effecting argument passed to unused parameter
  @Test
  public void testProcess_modifyCallSitesWithSideEffectArg_extractsSideEffect() {
    modifyCallSites = true;
    test("function foo(a) {} foo(alert());",
         "function foo() {} alert(); foo();");
  }

  // Tests modifyCallSites does not modify call sites when function escapes
  @Test
  public void testProcess_modifyCallSitesEscapedFunction_doesNotModifyCallSites() {
    modifyCallSites = true;
    test("function foo(a) {} function bar(fn) { fn(1); } bar(foo);",
         "function foo(a) {} function bar(fn) { fn(1); } bar(foo);");
  }

  // Tests catch block variable is preserved
  @Test
  public void testProcess_catchBlockVariable_preservesCatchVar() {
    test("try { alert(); } catch (e) {}",
         "try { alert(); } catch (e) {}");
  }

  // Tests unused prototype properties and methods are removed
  @Test
  public void testProcess_unusedPrototypeAssignment_removesAssignment() {
    test("function Foo() {} Foo.prototype.bar = function() {};", "");
  }

  // Tests recursive function expression self-reference preserves its name
  @Test
  public void testProcess_recursiveFunctionExpression_preservesName() {
    test("var f = function foo(n) { if (n > 0) foo(n - 1); }; f(5);",
         "var f = function foo(n) { if (n > 0) foo(n - 1); }; f(5);");
  }

  // Tests anonymous function expression removes unused trailing parameters
  @Test
  public void testProcess_anonymousFunctionUnusedArgs_removesTrailingArgs() {
    test("var f = function(a, b) { alert(a); }; f(1, 2);",
         "var f = function(a) { alert(a); }; f(1, 2);");
  }
}