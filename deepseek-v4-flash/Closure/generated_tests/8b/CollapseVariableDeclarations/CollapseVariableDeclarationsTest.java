package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

public class CollapseVariableDeclarationsTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new CollapseVariableDeclarations(compiler);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    // CollapseVariableDeclarations requires non-normalized AST
    disableNormalize();
  }

  // Normal cases: simple var collapse
  @Test
  public void testCollapseTwoVars() {
    test("var a; var b;", "var a, b;");
  }

  @Test
  public void testCollapseVarWithValueAndVar() {
    test("var a = 1; var b = 2;", "var a = 1, b = 2;");
  }

  @Test
  public void testCollapseMixedStubAndValue() {
    test("var a; var b = 1;", "var a, b = 1;");
  }

  @Test
  public void testCollapseValueThenStub() {
    test("var a = 1; var b;", "var a = 1, b;");
  }

  // Multiple variable declarations collapse
  @Test
  public void testCollapseMultipleVars() {
    test("var a; var b; var c;", "var a, b, c;");
  }

  @Test
  public void testCollapseMultipleVarsWithValues() {
    test("var a = 1; var b = 2; var c = 3;", "var a = 1, b = 2, c = 3;");
  }

  // Edge cases: single var, no collapse
  @Test
  public void testNoCollapseSingleVar() {
    testSame("var a;");
  }

  @Test
  public void testNoCollapseSingleVarWithValue() {
    testSame("var a = 1;");
  }

  // Edge case: var inside if parent (should not collapse)
  @Test
  public void testNoCollapseIfParentIsIf() {
    testSame("if (x) var a; var b;");
  }

  // No collapse if next node is not var or assign
  @Test
  public void testNoCollapseNonVarNext() {
    testSame("var a; foo();");
  }

  // Collapse var stub followed by var with init (regression for Defects4J bug 8b)
  @Test
  public void testCollapseVarStubThenVarWithInit() {
    test("var a; var a = 1;", "var a = 1;");
  }

  // Blacklisted assign blocks collapse of following var
  @Test
  public void testBlacklistedAssignBlocksCollapse() {
    testSame("var a; a = 1; var b;");
  }

  // Assignments followed by var – should collapse
  @Test
  public void testCollapseAssignThenVar() {
    test("a = 1; var b = 2;", "var a = 1, b = 2;");
  }

  // Multiple assignments then var
  @Test
  public void testCollapseMultipleAssignsThenVar() {
    test("a = 1; b = 2; var c = 3;", "var a = 1, b = 2, c = 3;");
  }

  // Assignments alone without var – should not collapse (hasVar must be true)
  @Test
  public void testNoCollapseAssignsOnly() {
    testSame("a = 1; b = 2;");
  }

  // Empty input
  @Test
  public void testEmptyInput() {
    testSame("");
  }
}