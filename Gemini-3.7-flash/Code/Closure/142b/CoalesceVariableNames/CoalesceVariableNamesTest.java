package com.google.javascript.jscomp;

public class CoalesceVariableNamesTest extends CompilerTestCase {

  private boolean usePseudoNames = false;

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new CoalesceVariableNames(compiler, usePseudoNames);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    usePseudoNames = false;
  }

  // Tests basic sequential variable coalescing in function scope
  public void testProcess_sequentialVars_coalescesVariables() {
    test("function f() { var x = 1; x; var y = 2; y; }",
         "function f() { var x = 1; x; x = 2; x; }");
  }

  // Tests that overlapping live ranges prevent coalescing
  public void testProcess_overlappingLiveRanges_doesNotCoalesce() {
    testSame("function f() { var x = 1; var y = 2; x; y; }");
  }

  // Tests global scope variables are untouched
  public void testProcess_globalScope_doesNotCoalesce() {
    testSame("var x = 1; var y = 2; x; y;");
  }

  // Tests coalescing local variable with function parameter
  public void testProcess_parameterAndLocalVar_coalescesIntoParameter() {
    test("function f(x) { x; var y = 1; y; }",
         "function f(x) { x; x = 1; x; }");
  }

  // Tests debug pseudo naming mode
  public void testProcess_usePseudoNames_generatesMergedName() {
    usePseudoNames = true;
    test("function f() { var x = 1; x; var y = 2; y; }",
         "function f() { var x_y = 1; x_y; x_y = 2; x_y; }");
  }

  // Tests multiple sequential variables in separate statements
  public void testProcess_threeSequentialVars_coalescesIntoOne() {
    test("function f() { var x = 1; x; var y = 2; y; var z = 3; z; }",
         "function f() { var x = 1; x; x = 2; x; x = 3; x; }");
  }

  // Tests declaration without initialization
  public void testProcess_uninitializedVar_removesDeclaration() {
    test("function f() { var x = 1; x; var y; y = 2; y; }",
         "function f() { var x = 1; x; x = 2; x; }");
  }

  // Tests coalescing inside for-in loop header
  public void testProcess_forInLoopVar_coalescesProperly() {
    test("function f(obj) { var x = 1; x; for (var y in obj) { y; } }",
         "function f(obj) { var x = 1; x; for (x in obj) { x; } }");
  }

  // Tests coalescing inside standard for loop header
  public void testProcess_forLoopInitVar_coalescesIntoAssign() {
    test("function f() { var x = 1; x; for (var y = 0; y < 10; y++) {} }",
         "function f() { var x = 1; x; for (x = 0; x < 10; x++) {} }");
  }

  // Tests conditional branches with disjoint live ranges
  public void testProcess_disjointBranches_coalescesVariables() {
    test("function f(cond) { if (cond) { var x = 1; x; } else { var y = 2; y; } }",
         "function f(cond) { if (cond) { var x = 1; x; } else { x = 2; x; } }");
  }

  // Tests compound assignment operators recognition
  public void testProcess_compoundAssignment_recognizesDefAndUse() {
    test("function f() { var x = 1; x += 1; var y = 2; y += 2; }",
         "function f() { var x = 1; x += 1; x = 2; x += 2; }");
  }

  // Tests that named function declarations are not coalesced or corrupted
  public void testProcess_namedFunction_doesNotCoalesceFunction() {
    test("function f() { function g() {} var x = 1; x; var y = 2; y; }",
         "function f() { function g() {} var x = 1; x; x = 2; x; }");
  }

  // Tests nested function scopes independently coalesce
  public void testProcess_nestedScope_coalescesInnerIndependently() {
    test("function f() { var x = 1; x; function g() { var a = 2; a; var b = 3; b; } }",
         "function f() { var x = 1; x; function g() { var a = 2; a; a = 3; a; } }");
  }

  // Tests multi-declaration var statement with mixed usage
  public void testProcess_multiVarDeclaration_preservesUnmerged() {
    testSame("function f() { var x = 1, y = 2; x; y; }");
  }

  // Tests two-parameter function with local variable coalescing
  public void testProcess_twoParamsAndLocal_coalescesSafely() {
    test("function f(a, b) { var x = 1; x; var y = 2; y; }",
         "function f(a, b) { var x = 1; x; x = 2; x; }");
  }

  // Tests pseudo-naming when name collision occurs
  public void testProcess_usePseudoNamesWithCollision_appendsSuffix() {
    usePseudoNames = true;
    test("function f() { var x_y = 0; x_y; var x = 1; x; var y = 2; y; }",
         "function f() { var x_y = 0; x_y; var x_y$ = 1; x_y$; x_y$ = 2; x_y$; }");
  }

  // Tests coalescing across while loop constructs
  public void testProcess_whileLoop_coalescesVariables() {
    test("function f() { var x = 0; while (x < 5) { x++; } var y = 1; y; }",
         "function f() { var x = 0; while (x < 5) { x++; } x = 1; x; }");
  }

  // Tests coalescing across do-while loop constructs
  public void testProcess_doWhileLoop_coalescesVariables() {
    test("function f() { var x = 0; do { x++; } while (x < 5); var y = 1; y; }",
         "function f() { var x = 0; do { x++; } while (x < 5); x = 1; x; }");
  }

  // Tests coalescing across try-catch-finally blocks
  public void testProcess_tryCatchFinally_coalescesVariables() {
    test("function f() { try { var x = 1; x; } catch (e) { var y = 2; y; } }",
         "function f() { try { var x = 1; x; } catch (e) { x = 2; x; } }");
  }

  // Tests switch-case statements with disjoint live ranges
  public void testProcess_switchCase_coalescesVariables() {
    test("function f(v) { switch (v) { case 1: var x = 1; x; break; case 2: var y = 2; y; break; } }",
         "function f(v) { switch (v) { case 1: var x = 1; x; break; case 2: x = 2; x; break; } }");
  }

  // Tests coalescing when variable is assigned inside ternary operator
  public void testProcess_ternaryExpression_coalescesVariables() {
    test("function f(cond) { var x = cond ? 1 : 2; x; var y = 3; y; }",
         "function f(cond) { var x = cond ? 1 : 2; x; x = 3; x; }");
  }

  // Tests coalescing with array and object literals
  public void testProcess_complexLiterals_coalescesVariables() {
    test("function f() { var arr = [1, 2, 3]; arr; var obj = {a: 1}; obj; }",
         "function f() { var arr = [1, 2, 3]; arr; arr = {a: 1}; arr; }");
  }

  // Tests coalescing with multiple declarations in single var statement
  public void testProcess_multiVarSequential_coalescesProperly() {
    test("function f() { var a = 1; a; var b = 2, c = 3; b; c; }",
         "function f() { var a = 1; a; a = 2; var c = 3; a; c; }");
  }
}