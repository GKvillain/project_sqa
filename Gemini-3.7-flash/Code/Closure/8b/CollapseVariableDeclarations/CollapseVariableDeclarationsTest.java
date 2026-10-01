package com.google.javascript.jscomp;

import org.junit.Test;

public class CollapseVariableDeclarationsTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new CollapseVariableDeclarations(compiler);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests collapsing multiple simple var declarations into a single var
  @Test
  public void testProcess_multipleVars_collapsesIntoSingleVar() {
    test("var a; var b; var c;",
         "var a, b, c;");
  }

  // Tests collapsing initialized var declarations
  @Test
  public void testProcess_initializedVars_collapsesIntoSingleVar() {
    test("var a = 1; var b = 2; var c = 3;",
         "var a = 1, b = 2, c = 3;");
  }

  // Tests collapsing mix of initialized and uninitialized vars
  @Test
  public void testProcess_mixedInitializedAndUninitialized_collapsesCorrectly() {
    test("var a; var b = 1; var c = 2;",
         "var a, b = 1, c = 2;");
    test("var a = 1; var b; var c = 2;",
         "var a = 1, b, c = 2;");
  }

  // Tests collapsing multi-declaration var statements
  @Test
  public void testProcess_multiVarStatements_collapsesIntoSingleVar() {
    test("var a = 1, b = 2; var c = 3, d = 4;",
         "var a = 1, b = 2, c = 3, d = 4;");
  }

  // Tests that intervening non-collapsible statement splits collapse chains
  @Test
  public void testProcess_interveningStatement_splitsCollapses() {
    test("var a = 1; var b = 2; foo(); var c = 3; var d = 4;",
         "var a = 1, b = 2; foo(); var c = 3, d = 4;");
  }

  // Tests that single var followed by non-var is unchanged
  @Test
  public void testProcess_singleVarWithInterveningStatement_doesNotChange() {
    testSame("var a = 1; foo(); var b = 2;");
  }

  // Tests that vars directly under an IF node are not collapsed
  @Test
  public void testProcess_varsUnderIf_doesNotCollapse() {
    testSame("if (x) var a = 1; else var b = 2;");
  }

  // Tests collapsing inside function bodies
  @Test
  public void testProcess_varsInsideFunction_collapsesInsideFunction() {
    test("function f() { var a = 1; var b = 2; return a + b; }",
         "function f() { var a = 1, b = 2; return a + b; }");
  }

  // Tests that assignment to a stub var is not redeclared due to stub blacklisting
  @Test
  public void testProcess_assignmentToStubVar_doesNotRedeclare() {
    testSame("var a; a = 1;");
    testSame("var a, b; a = 1; b = 2;");
  }

  // Tests collapsing initialized var followed by assignment to the same var
  @Test
  public void testProcess_varFollowedByReassignment_collapsesCorrectly() {
    test("var a = 1; a = 2;",
         "var a = 1, a = 2;");
  }

  // Tests collapsing initialized var followed by reassignment and another var
  @Test
  public void testProcess_varReassignmentAndFollowedByVar_collapsesAll() {
    test("var a = 1; a = 2; var b = 3;",
         "var a = 1, a = 2, b = 3;");
  }

  // Tests that assignment to variable in outer scope is not redeclared
  @Test
  public void testProcess_assignmentToOuterScopeVar_doesNotRedeclare() {
    testSame("var a = 1; function f() { a = 2; var b = 3; }");
  }

  // Tests that assignment to property or complex LHS is not redeclared
  @Test
  public void testProcess_propertyAssignment_doesNotCollapse() {
    testSame("var a = 1; a.b = 2; var c = 3;");
  }

  // Tests that undeclared variable assignment is not collapsed
  @Test
  public void testProcess_undeclaredVariableAssignment_doesNotCollapse() {
    testSame("var a = 1; x = 2; var b = 3;");
  }

  // Tests single isolated var declaration remains unchanged
  @Test
  public void testProcess_singleVarDeclaration_doesNotChange() {
    testSame("var a = 1;");
    testSame("var a;");
  }

  // Tests collapsing multiple consecutive reassignments following var declaration
  @Test
  public void testProcess_multipleReassignments_collapsesAll() {
    test("var a = 1; a = 2; a = 3;",
         "var a = 1, a = 2, a = 3;");
  }

  // Tests collapsing reassignments of previously declared initialized vars in same chain
  @Test
  public void testProcess_reassignmentOfEarlierDeclaredVar_collapses() {
    test("var a = 1; var b = 2; a = 3; b = 4;",
         "var a = 1, b = 2, a = 3, b = 4;");
  }

  // Tests that compound assignments are not collapsed into var declarations
  @Test
  public void testProcess_compoundAssignment_doesNotCollapse() {
    testSame("var a = 1; a += 2;");
    testSame("var a = 1; a -= 2; var b = 3;");
  }

  // Tests that unary increment or decrement operations are not collapsed
  @Test
  public void testProcess_incrementDecrement_doesNotCollapse() {
    testSame("var a = 1; ++a;");
    testSame("var a = 1; a++; var b = 2;");
  }

  // Tests collapsing var declarations inside a block statement
  @Test
  public void testProcess_varsInBlock_collapsesInsideBlock() {
    test("{ var a = 1; var b = 2; }",
         "{ var a = 1, b = 2; }");
  }

  // Tests collapsing var declarations inside try, catch, and finally blocks
  @Test
  public void testProcess_varsInTryCatchFinally_collapsesInEachBlock() {
    test("try { var a = 1; var b = 2; } catch (e) { var c = 3; var d = 4; } finally { var e = 5; var f = 6; }",
         "try { var a = 1, b = 2; } catch (e) { var c = 3, d = 4; } finally { var e = 5, f = 6; }");
  }

  // Tests collapsing var declarations inside switch case statements
  @Test
  public void testProcess_varsInSwitch_collapsesInCase() {
    test("switch (x) { case 1: var a = 1; var b = 2; break; }",
         "switch (x) { case 1: var a = 1, b = 2; break; }");
  }

  // Tests collapsing var declarations inside loops
  @Test
  public void testProcess_varsInLoops_collapsesInBody() {
    test("while (true) { var a = 1; var b = 2; }",
         "while (true) { var a = 1, b = 2; }");
    test("do { var a = 1; var b = 2; } while (true);",
         "do { var a = 1, b = 2; } while (true);");
    test("for (;;) { var a = 1; var b = 2; }",
         "for (;;) { var a = 1, b = 2; }");
    test("for (var a in obj) { var b = 1; var c = 2; }",
         "for (var a in obj) { var b = 1, c = 2; }");
  }

  // Tests collapsing var declarations initialized with function expressions
  @Test
  public void testProcess_functionExpressions_collapsesCorrectly() {
    test("var a = function() {}; var b = function() {};",
         "var a = function() {}, b = function() {};");
  }
}