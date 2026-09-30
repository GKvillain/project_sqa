package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;
import static org.junit.Assert.*;

public class LiveVariablesAnalysisTest {

  private LiveVariablesAnalysis computeLiveness(String src) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(src);
    assertEquals(0, compiler.getErrorCount());
    Node function = root.getFirstChild();
    Scope scope = new SyntacticScopeCreator(compiler).createScope(function, null);
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, true);
    cfa.process(null, function.getLastChild());
    LiveVariablesAnalysis analysis = new LiveVariablesAnalysis(
        cfa.getCfg(), scope, compiler);
    analysis.analyze();
    return analysis;
  }

  // Tests for-in loop with property access as LHS (Defects4J Closure-58 bug scenario)
  @Test
  public void testForIn_propertyAccessLhs_doesNotThrowException() {
    LiveVariablesAnalysis analysis = computeLiveness(
        "function foo(obj, items) { for (obj.prop in items) {} }");
    assertNotNull(analysis);
  }

  // Tests for-in loop with element access as LHS (Defects4J Closure-58 bug scenario)
  @Test
  public void testForIn_elementAccessLhs_doesNotThrowException() {
    LiveVariablesAnalysis analysis = computeLiveness(
        "function foo(obj, key, items) { for (obj[key] in items) {} }");
    assertNotNull(analysis);
  }

  // Tests for-in loop with var declaration
  @Test
  public void testForIn_varDeclaration_computesLiveness() {
    LiveVariablesAnalysis analysis = computeLiveness(
        "function foo(items) { var sum = 0; for (var x in items) { sum = sum + x; } return sum; }");
    assertNotNull(analysis);
    assertTrue(analysis.getVarIndex("sum") >= 0);
    assertTrue(analysis.getVarIndex("x") >= 0);
  }

  // Tests for-in loop with simple name LHS
  @Test
  public void testForIn_simpleNameLhs_computesLiveness() {
    LiveVariablesAnalysis analysis = computeLiveness(
        "function foo(items) { var x; for (x in items) {} }");
    assertNotNull(analysis);
    assertTrue(analysis.getVarIndex("x") >= 0);
  }

  // Tests standard for loop
  @Test
  public void testStandardForLoop_withCondition_computesLiveness() {
    LiveVariablesAnalysis analysis = computeLiveness(
        "function foo(n) { var res = 0; for (var i = 0; i < n; i = i + 1) { res = res + i; } return res; }");
    assertNotNull(analysis);
    assertTrue(analysis.getVarIndex("res") >= 0);
    assertTrue(analysis.getVarIndex("i") >= 0);
  }

  // Tests while loop and condition evaluation
  @Test
  public void testWhileLoop_computesLiveness() {
    LiveVariablesAnalysis analysis = computeLiveness(
        "function foo(x) { while (x > 0) { x = x - 1; } return x; }");
    assertNotNull(analysis);
    assertTrue(analysis.getVarIndex("x") >= 0);
  }

  // Tests do-while loop and condition evaluation
  @Test
  public void testDoWhileLoop_computesLiveness() {
    LiveVariablesAnalysis analysis = computeLiveness(
        "function foo(x) { do { x = x - 1; } while (x > 0); return x; }");
    assertNotNull(analysis);
    assertTrue(analysis.getVarIndex("x") >= 0);
  }

  // Tests if-else conditional branches
  @Test
  public void testIfElse_computesLiveness() {
    LiveVariablesAnalysis analysis = computeLiveness(
        "function foo(cond, a, b) { var x; if (cond) { x = a; } else { x = b; } return x; }");
    assertNotNull(analysis);
    assertTrue(analysis.getVarIndex("x") >= 0);
  }

  // Tests logical AND and OR short-circuit expressions
  @Test
  public void testLogicalAndOr_computesLiveness() {
    LiveVariablesAnalysis analysis = computeLiveness(
        "function foo(a, b, c) { var res = (a && b) || c; return res; }");
    assertNotNull(analysis);
    assertTrue(analysis.getVarIndex("res") >= 0);
  }

  // Tests conditional HOOK (ternary operator)
  @Test
  public void testHookOperator_computesLiveness() {
    LiveVariablesAnalysis analysis = computeLiveness(
        "function foo(cond, a, b) { var res = cond ? a : b; return res; }");
    assertNotNull(analysis);
    assertTrue(analysis.getVarIndex("res") >= 0);
  }

  // Tests compound assignment operators
  @Test
  public void testCompoundAssignment_computesLiveness() {
    LiveVariablesAnalysis analysis = computeLiveness(
        "function foo(a) { var b = 10; b += a; return b; }");
    assertNotNull(analysis);
    assertTrue(analysis.getVarIndex("b") >= 0);
  }

  // Tests argument array alias escaping parameters
  @Test
  public void testArgumentsAlias_escapesParameters() {
    LiveVariablesAnalysis analysis = computeLiveness(
        "function foo(a, b) { return arguments[0] + arguments[1]; }");
    assertNotNull(analysis);
    assertFalse(analysis.getEscapedLocals().isEmpty());
  }

  // Tests inner function closure escaping local variables
  @Test
  public void testInnerFunction_escapesLocalVariables() {
    LiveVariablesAnalysis analysis = computeLiveness(
        "function foo(a) { var b = a; function inner() { return b; } return inner(); }");
    assertNotNull(analysis);
    assertFalse(analysis.getEscapedLocals().isEmpty());
  }

  // Tests VAR declaration without initial value
  @Test
  public void testVarDeclarationWithoutAssignment() {
    LiveVariablesAnalysis analysis = computeLiveness(
        "function foo() { var a; var b = 1; return b; }");
    assertNotNull(analysis);
    assertTrue(analysis.getVarIndex("a") >= 0);
    assertTrue(analysis.getVarIndex("b") >= 0);
  }

  // Tests isForward method returning false
  @Test
  public void testIsForward_returnsFalse() {
    LiveVariablesAnalysis analysis = computeLiveness("function foo(x) { return x; }");
    assertFalse(analysis.isForward());
  }

  // Tests lattice createEntryLattice and createInitialEstimateLattice
  @Test
  public void testCreateLattices_notNull() {
    LiveVariablesAnalysis analysis = computeLiveness("function foo(x, y) { return x + y; }");
    LiveVariablesAnalysis.LiveVariableLattice entry = analysis.createEntryLattice();
    LiveVariablesAnalysis.LiveVariableLattice initial = analysis.createInitialEstimateLattice();
    assertNotNull(entry);
    assertNotNull(initial);
    assertEquals(entry, initial);
    assertEquals(entry.hashCode(), initial.hashCode());
    assertFalse(entry.isLive(0));
    assertNotNull(entry.toString());
  }
}