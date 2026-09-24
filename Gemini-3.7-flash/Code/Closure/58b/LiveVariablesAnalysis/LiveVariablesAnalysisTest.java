package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LiveVariablesAnalysisTest {

  private Node findFirstFunction(Node n) {
    if (n.getType() == Token.FUNCTION) {
      return n;
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      Node fn = findFirstFunction(c);
      if (fn != null) {
        return fn;
      }
    }
    return null;
  }

  private LiveVariablesAnalysis analyze(String src) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(src);
    Node fn = findFirstFunction(root);
    assertNotNull("Function AST node must be found", fn);
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, true);
    cfa.process(null, fn);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    Scope scope = new SyntacticScopeCreator(compiler).createScope(fn, null);
    LiveVariablesAnalysis lva = new LiveVariablesAnalysis(cfg, scope, compiler);
    lva.analyze();
    return lva;
  }

  // Tests for-in loop with property access as LHS (Defects4J Closure-58)
  @Test
  public void testComputeGenKill_forInGetPropLhs_doesNotThrow() {
    LiveVariablesAnalysis lva = analyze("function f(obj) { var a = {}; for (a.x in obj) {} }");
    assertNotNull(lva);
  }

  // Tests for-in loop with array element as LHS (Defects4J Closure-58)
  @Test
  public void testComputeGenKill_forInGetElemLhs_doesNotThrow() {
    LiveVariablesAnalysis lva = analyze("function f(obj) { var a = []; for (a[0] in obj) {} }");
    assertNotNull(lva);
  }

  // Tests for-in loop with variable declaration LHS
  @Test
  public void testComputeGenKill_forInVarDecl_success() {
    LiveVariablesAnalysis lva = analyze("function f(obj) { for (var x in obj) { use(x); } }");
    assertTrue(lva.getVarIndex("x") >= 0);
  }

  // Tests for-in loop with existing local name LHS
  @Test
  public void testComputeGenKill_forInNameLhs_success() {
    LiveVariablesAnalysis lva = analyze("function f(obj) { var x; for (x in obj) { use(x); } }");
    assertTrue(lva.getVarIndex("x") >= 0);
  }

  // Tests standard for loop condition expression and step
  @Test
  public void testComputeGenKill_standardForLoop_success() {
    LiveVariablesAnalysis lva = analyze("function f() { var x = 0; for (; x < 10; x++) {} }");
    assertTrue(lva.getVarIndex("x") >= 0);
  }

  // Tests while and do-while loops condition expression
  @Test
  public void testComputeGenKill_whileAndDoWhile_success() {
    LiveVariablesAnalysis lva = analyze(
        "function f() { var x = 0; while (x < 10) { x++; } do { x--; } while (x > 0); }");
    assertTrue(lva.getVarIndex("x") >= 0);
  }

  // Tests if-statement condition and branching
  @Test
  public void testComputeGenKill_ifCondition_success() {
    LiveVariablesAnalysis lva = analyze("function f(a) { var b = 1; if (a) { b = 2; } return b; }");
    assertTrue(lva.getVarIndex("b") >= 0);
  }

  // Tests logical AND and OR expressions with short-circuiting
  @Test
  public void testComputeGenKill_logicalAndOr_success() {
    LiveVariablesAnalysis lva = analyze(
        "function f(a, b) { var c = a && b; var d = a || b; return c + d; }");
    assertTrue(lva.getVarIndex("c") >= 0);
    assertTrue(lva.getVarIndex("d") >= 0);
  }

  // Tests conditional ternary hook operator
  @Test
  public void testComputeGenKill_hookOperator_success() {
    LiveVariablesAnalysis lva = analyze(
        "function f(a, b, c) { var d = a ? b : c; return d; }");
    assertTrue(lva.getVarIndex("d") >= 0);
  }

  // Tests compound assignment operators reading LHS
  @Test
  public void testComputeGenKill_compoundAssignment_success() {
    LiveVariablesAnalysis lva = analyze("function f() { var a = 1; a += 2; return a; }");
    assertTrue(lva.getVarIndex("a") >= 0);
  }

  // Tests arguments alias causing all parameters to escape
  @Test
  public void testMarkAllParametersEscaped_argumentsAlias_escapesParams() {
    LiveVariablesAnalysis lva = analyze("function f(a, b) { var args = arguments; return args[0]; }");
    Set<Scope.Var> escaped = lva.getEscapedLocals();
    assertEquals(2, escaped.size());
  }

  // Tests exception branch in try-catch block for conditional kills
  @Test
  public void testFlowThrough_tryCatchBlock_conditionalKills() {
    LiveVariablesAnalysis lva = analyze(
        "function f() { var a = 1; try { a = 2; } catch (e) { } return a; }");
    assertTrue(lva.getVarIndex("a") >= 0);
  }

  // Tests inner function closure escaping local variables
  @Test
  public void testGetEscapedLocals_closureVar_escapes() {
    LiveVariablesAnalysis lva = analyze(
        "function f() { var a = 1; function g() { return a; } return g; }");
    Set<Scope.Var> escaped = lva.getEscapedLocals();
    assertEquals(1, escaped.size());
  }

  // Tests LiveVariableLattice equality, hashCode, toString, and liveness check
  @Test
  public void testLiveVariableLattice_operations() {
    LiveVariablesAnalysis lva = analyze("function f() { var a = 1; var b = 2; return a + b; }");
    LiveVariablesAnalysis.LiveVariableLattice entry = lva.createEntryLattice();
    LiveVariablesAnalysis.LiveVariableLattice init = lva.createInitialEstimateLattice();

    assertEquals(entry, init);
    assertEquals(entry.hashCode(), init.hashCode());
    assertNotNull(entry.toString());
    assertFalse(entry.isLive(0));
    assertFalse(lva.isForward());
  }

  // Tests variable index retrieval
  @Test
  public void testGetVarIndex_validVar_returnsIndex() {
    LiveVariablesAnalysis lva = analyze("function f(x, y) { var z = 1; return x + y + z; }");
    int indexX = lva.getVarIndex("x");
    int indexY = lva.getVarIndex("y");
    int indexZ = lva.getVarIndex("z");

    assertTrue(indexX >= 0);
    assertTrue(indexY >= 0);
    assertTrue(indexZ >= 0);
    assertTrue(indexX != indexY);
    assertTrue(indexY != indexZ);
  }
}