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

  // Tests collapsing multiple consecutive var declarations with initializers
  @Test
  public void testCollapsing_multipleVarsWithInitializers_collapsesIntoSingleVar() {
    test("var a = 1; var b = 2; var c = 3;", "var a = 1, b = 2, c = 3;");
  }

  // Tests collapsing multiple consecutive var declarations without initializers
  @Test
  public void testCollapsing_multipleVarsWithoutInitializers_collapsesIntoSingleVar() {
    test("var a; var b; var c;", "var a, b, c;");
  }

  // Tests collapsing a mix of initialized and uninitialized vars
  @Test
  public void testCollapsing_mixedVarsWithAndWithoutInitializers_collapsesIntoSingleVar() {
    test("var a; var b = 1; var c = 2;", "var a, b = 1, c = 2;");
  }

  // Tests redeclaration of an already initialized variable
  @Test
  public void testCollapsing_redeclarationOfInitializedVar_collapsesIntoSingleVar() {
    test("var a = 1; a = 2;", "var a = 1, a = 2;");
  }

  // Tests collapsing initialized vars followed by reassignments
  @Test
  public void testCollapsing_initializedVarsFollowedByReassignments_collapses() {
    test("var a = 1; var b = 2; a = 3; b = 4;", "var a = 1, b = 2, a = 3, b = 4;");
  }

  // Tests that stub vars without initializers blacklist subsequent redeclarations
  @Test
  public void testCollapsing_stubVarAssignment_doesNotCollapseAssignment() {
    testSame("var a; a = 1;");
  }

  // Tests that stub vars in a multi-var statement blacklist subsequent redeclarations
  @Test
  public void testCollapsing_stubVarInMultiDeclaration_doesNotCollapseSubsequentAssignment() {
    test("var a; var b = 1; a = 2;", "var a, b = 1; a = 2;");
  }

  // Tests that vars in if-then and else branches are not collapsed
  @Test
  public void testCollapsing_varsInIfElseBranches_doesNotCollapse() {
    testSame("if (x) var a; else var b;");
  }

  // Tests that vars inside a block under an if statement are collapsed
  @Test
  public void testCollapsing_varsInsideIfBlock_collapses() {
    test("if (x) { var a; var b; }", "if (x) { var a, b; }");
  }

  // Tests that non-assignment statements interrupt the collapsing chain
  @Test
  public void testCollapsing_separatedStatements_collapsesOnlyAdjacentVars() {
    test("var a = 1; var b = 2; foo(); var c = 3; var d = 4;",
         "var a = 1, b = 2; foo(); var c = 3, d = 4;");
  }

  // Tests that property assignments on objects are not collapsed into var declarations
  @Test
  public void testCollapsing_propertyAssignment_doesNotCollapse() {
    testSame("var a = {}; a.b = 1; var c = 2;");
  }

  // Tests single var declaration remains unchanged
  @Test
  public void testCollapsing_singleVarDeclaration_remainsUnchanged() {
    testSame("var a = 1;");
  }

  // Tests collapsing var declarations inside function scope
  @Test
  public void testCollapsing_insideFunction_collapses() {
    test("function f() { var a = 1; var b = 2; }",
         "function f() { var a = 1, b = 2; }");
  }

  // Tests collapsing across different scopes does not merge outer and inner vars
  @Test
  public void testCollapsing_nestedScopes_collapsesSeparately() {
    test("var a = 1; function f() { var b = 2; var c = 3; } var d = 4;",
         "var a = 1; function f() { var b = 2, c = 3; } var d = 4;");
  }

  // Tests collapsing with complex RHS expressions
  @Test
  public void testCollapsing_complexRhsExpressions_collapses() {
    test("var a = 1 + 2; var b = a * 3;", "var a = 1 + 2, b = a * 3;");
  }

  // Tests collapsing a mix of redeclaration and new var declaration
  @Test
  public void testCollapsing_redeclarationFollowedByVar_collapses() {
    test("var a = 1; a = 2; var b = 3;", "var a = 1, a = 2, b = 3;");
  }
}