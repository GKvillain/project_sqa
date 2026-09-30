package com.google.javascript.jscomp;

public class RemoveUnusedVarsTest extends CompilerTestCase {

  private boolean removeGlobals = true;
  private boolean preserveFunctionExpressionNames = false;
  private boolean modifyCallSites = false;

  public RemoveUnusedVarsTest() {
    super();
  }

  @Override
  protected void setUp() throws Exception {
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

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests removal of simple unreferenced variable declaration
  public void testProcess_unusedVariable_removesDeclaration() {
    test("var a = 1;", "");
  }

  // Tests that unreferenced variable with side-effect initial value leaves side effect expression
  public void testProcess_unusedVariableWithSideEffects_retainsSideEffectOnly() {
    test("var a = foo();", "foo();");
  }

  // Tests that referenced variable is not removed
  public void testProcess_referencedVariable_retainsDeclaration() {
    testSame("var a = 1; var b = a; alert(b);");
  }

  // Tests removal of unreferenced function declaration
  public void testProcess_unusedFunctionDeclaration_removesFunction() {
    test("function f() { var a = 1; }", "");
  }

  // Tests that referenced function declaration is retained
  public void testProcess_referencedFunctionDeclaration_retainsFunction() {
    testSame("function f() { return 1; } alert(f());");
  }

  // Tests removal of property assignments on unreferenced object literals
  public void testProcess_propertyAssignOnObjectLiteral_removesAssign() {
    test("var a = {}; a.b = 1;", "");
  }

  // Tests that property assignments on unknown/returned values are retained
  public void testProcess_propertyAssignOnUnknownValue_retainsAssign() {
    testSame("var a = foo(); a.b = 1;");
  }

  // Tests removal of unreferenced array and unreferenced variable assigned into it (Closure-45 regression)
  public void testProcess_unusedArrayAndUnusedAssignedVar_removesBoth() {
    test("var a = []; var b = 1; a[0] = b;", "");
  }

  // Tests that multiple property assignments to unreferenced array are fully removed
  public void testProcess_multipleAssignsToUnusedArray_removesAll() {
    test("var a = []; var b = 1; a[0] = b; var c = 2; a[1] = c;", "");
  }

  // Tests removal of only unreferenced names in a multi-variable declaration
  public void testProcess_multipleVarsInDeclaration_removesOnlyUnreferenced() {
    test("var a = 1, b = 2; alert(a);", "var a = 1; alert(a);");
  }

  // Tests removal of unused trailing parameters from function declaration
  public void testProcess_unusedTrailingFunctionArgs_removesTrailingArgs() {
    test("function f(a, b) { return a; } alert(f(1, 2));",
         "function f(a) { return a; } alert(f(1, 2));");
  }

  // Tests that escaping 'arguments' keyword prevents parameter removal
  public void testProcess_escapedArguments_preservesParameters() {
    testSame("function f(a, b) { return arguments; } alert(f(1, 2));");
  }

  // Tests that for-in loop variable declaration is preserved
  public void testProcess_forInLoopVariable_preservesVariable() {
    testSame("for (var a in b) {}");
  }

  // Tests that global variables are preserved when removeGlobals is false
  public void testProcess_removeGlobalsFalse_preservesGlobalVariables() {
    removeGlobals = false;
    testSame("var a = 1;");
  }

  // Tests that unused local variables are removed even when removeGlobals is false
  public void testProcess_removeGlobalsFalse_removesLocalUnusedVars() {
    removeGlobals = false;
    test("function f() { var a = 1; } alert(f());",
         "function f() {} alert(f());");
  }

  // Tests call site optimization to remove unused parameters from caller
  public void testProcess_modifyCallSites_removesUnusedArgumentAtCallSite() {
    modifyCallSites = true;
    test("function f(a, b) { return a; } f(1, 2);",
         "function f(a) { return a; } f(1);");
  }

  // Tests that referenced array retains assigned variable (Closure-45 fix verification)
  public void testProcess_referencedArrayWithAssignedVar_retainsBoth() {
    testSame("var a = []; var b = 1; a[0] = b; alert(a);");
  }

  // Tests that preserveFunctionExpressionNames preserves named function expression names
  public void testProcess_preserveFunctionExpressionNames_preservesName() {
    preserveFunctionExpressionNames = true;
    testSame("var f = function foo() { return 1; }; alert(f());");
  }

  // Tests that preserveFunctionExpressionNames false removes unused named function expression name
  public void testProcess_preserveFunctionExpressionNamesFalse_removesName() {
    preserveFunctionExpressionNames = false;
    test("var f = function foo() { return 1; }; alert(f());",
         "var f = function() { return 1; }; alert(f());");
  }

  // Tests mutual recursion between unused functions
  public void testProcess_mutualRecursiveUnusedFunctions_removesBoth() {
    test("function f() { g(); } function g() { f(); }", "");
  }

  // Tests unused variable in catch block
  public void testProcess_unusedCatchVariable_preservesCatchBlock() {
    testSame("try { foo(); } catch (e) {}");
  }

  // Tests prefix and postfix increment on unused variable
  public void testProcess_unusedVarWithIncDec_removesIncDec() {
    test("var a = 1; a++;", "");
    test("var a = 1; ++a;", "");
  }

  // Tests unused variable reassignment
  public void testProcess_unusedVarReassignment_removesAllAssignments() {
    test("var a = 1; a = 2; a = 3;", "");
  }

  // Tests that chained assignment to used and unused variable keeps used assignment
  public void testProcess_chainedAssignmentWithUsedVar_keepsUsedAssign() {
    test("var a; var b = a = 1; alert(b);",
         "var b = 1; alert(b);");
  }
}