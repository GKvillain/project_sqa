package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link FlowSensitiveInlineVariables}.
 */
public class FlowSensitiveInlineVariablesTest extends CompilerTestCase {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new FlowSensitiveInlineVariables(compiler);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests simple var declaration inlining
  @Test
  public void testInline_simpleVar_inlinesValue() {
    test("function f() { var x = 1; return x; }",
         "function f() { return 1; }");
  }

  // Tests simple assignment inlining
  @Test
  public void testInline_simpleAssign_inlinesValue() {
    test("function f() { var x; x = 1; return x; }",
         "function f() { var x; return 1; }");
  }

  // Tests that variables in global scope are not inlined
  @Test
  public void testInline_globalScope_doesNotInline() {
    testSame("var x = 1; var y = x;");
  }

  // Tests that multiple uses prevent inlining
  @Test
  public void testInline_multipleUses_doesNotInline() {
    testSame("function f() { var x = 1; return x + x; }");
  }

  // Tests that uses within loops are not inlined
  @Test
  public void testInline_withinLoop_doesNotInline() {
    testSame("function f() { var x = 1; while (true) { alert(x); } }");
    testSame("function f() { var x = 1; for (var i = 0; i < 10; i++) { alert(x); } }");
    testSame("function f() { var x = 1; do { alert(x); } while (true); }");
  }

  // Tests that catch expressions are not inlined
  @Test
  public void testInline_catchExpression_doesNotInline() {
    testSame("function f(e) { var a = e; try { throw x; } catch (e) {} return a; }");
    testSame("var a = e; try { throw x; } catch (e) { alert(e); } return a;");
  }

  // Tests that object and array literals are not inlined
  @Test
  public void testInline_objectOrArrayLiteral_doesNotInline() {
    testSame("function f() { var x = {}; return x; }");
    testSame("function f() { var x = []; return x; }");
  }

  // Tests that property access and element access are not inlined
  @Test
  public void testInline_getPropOrGetElem_doesNotInline() {
    testSame("function f(a) { var x = a.b; return x; }");
    testSame("function f(a, b) { var x = a[b]; return x; }");
  }

  // Tests that 'new' expressions and RegExps are not inlined
  @Test
  public void testInline_newOrRegExp_doesNotInline() {
    testSame("function f() { var x = new Object(); return x; }");
    testSame("function f() { var x = /abc/; return x; }");
  }

  // Tests that RHS with potential side effects is not inlined
  @Test
  public void testInline_sideEffectRhs_doesNotInline() {
    testSame("function f() { var x = g(); return x; }");
  }

  // Tests that assignment used as R-Value is not inlined
  @Test
  public void testInline_assignAsRValue_doesNotInline() {
    testSame("function f() { var x; var y = (x = 1); return x; }");
  }

  // Tests side effects between definition and use preventing inlining
  @Test
  public void testInline_sideEffectsBetweenDefAndUse_doesNotInline() {
    testSame("function f(a) { var x = a; g(); return x; }");
  }

  // Tests that expression with side effects on the right of def prevents inlining
  @Test
  public void testInline_sideEffectsRightOfDef_doesNotInline() {
    testSame("function f(b) { var x = (b, g()); return x; }");
  }

  // Tests function parameters are not inlined
  @Test
  public void testInline_functionParameter_doesNotInline() {
    testSame("function f(x) { return x; }");
  }

  // Tests multiple sequential inlines in a function
  @Test
  public void testInline_sequentialVars_inlinesBoth() {
    test("function f() { var x = 1; var y = 2; return x + y; }",
         "function f() { var x = 1; return 1 + 2; }");
  }

  // Tests labeled assignment inlining
  @Test
  public void testInline_labeledAssign_inlinesValue() {
    test("function f() { var x; label: x = 1; return x; }",
         "function f() { var x; label: ; return 1; }");
  }

  // Tests dependent variable inlining interaction
  @Test
  public void testInline_dependentVariables_inlinesCorrectly() {
    test("function f() { var x = 1; var y = x + 1; return y; }",
         "function f() { var x = 1; return 1 + 1; }");
  }

  // Tests that increment/decrement targets are not treated as pure reads
  @Test
  public void testInline_incDecOperation_doesNotInline() {
    testSame("function f() { var x = 1; x++; return x; }");
    testSame("function f() { var x = 1; x--; return x; }");
  }
}