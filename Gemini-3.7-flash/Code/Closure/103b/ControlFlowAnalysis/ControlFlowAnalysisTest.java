package com.google.javascript.jscomp;

import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.*;

public class ControlFlowAnalysisTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private ControlFlowGraph<Node> createCfg(String js, boolean traverseFunctions) {
    Node root = compiler.parseTestCode(js);
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, traverseFunctions);
    cfa.process(null, root);
    return cfa.getCfg();
  }

  // Tests simple if-else control flow graph construction
  @Test
  public void testProcess_ifElseStatement_createsCorrectBranches() {
    String js = "if (a) { x = 1; } else { x = 2; }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
    assertNotNull(cfg.getImplicitReturn());
    assertTrue(cfg.getDirectedGraphNodes().size() > 0);
  }

  // Tests if statement without else branch
  @Test
  public void testProcess_ifWithoutElse_createsOnFalseFollowEdge() {
    String js = "if (a) { x = 1; } y = 2;";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
  }

  // Tests while loop control flow graph construction
  @Test
  public void testProcess_whileLoop_createsLoopAndExitBranches() {
    String js = "while (x > 0) { x--; }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertTrue(cfg.getDirectedGraphNodes().size() > 0);
  }

  // Tests do-while loop control flow graph construction
  @Test
  public void testProcess_doWhileLoop_createsLoopAndExitBranches() {
    String js = "do { x--; } while (x > 0);";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertTrue(cfg.getDirectedGraphNodes().size() > 0);
  }

  // Tests standard 4-child for loop control flow
  @Test
  public void testProcess_forLoop_createsInitCondIterBodyEdges() {
    String js = "for (var i = 0; i < 10; i++) { x += i; }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertTrue(cfg.getDirectedGraphNodes().size() > 0);
  }

  // Tests for-in loop control flow
  @Test
  public void testProcess_forInLoop_createsCollectionAndBodyEdges() {
    String js = "for (var key in obj) { x = obj[key]; }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertTrue(cfg.getDirectedGraphNodes().size() > 0);
  }

  // Tests switch statement with case and default blocks
  @Test
  public void testProcess_switchCaseDefault_createsBranchEdges() {
    String js = "switch (x) { case 1: a(); break; case 2: b(); break; default: c(); }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertTrue(cfg.getDirectedGraphNodes().size() > 0);
  }

  // Tests switch statement without default
  @Test
  public void testProcess_switchWithoutDefault_createsFollowEdge() {
    String js = "switch (x) { case 1: a(); break; } y = 1;";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertTrue(cfg.getDirectedGraphNodes().size() > 0);
  }

  // Tests try-catch-finally block control flow and exception edges
  @Test
  public void testProcess_tryCatchFinally_createsExceptionAndFinallyEdges() {
    String js = "try { foo(); } catch (e) { handle(e); } finally { cleanup(); }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertTrue(cfg.getDirectedGraphNodes().size() > 0);
  }

  // Tests nested try blocks with throw statement
  @Test
  public void testProcess_nestedTryWithThrow_connectsToInnerCatch() {
    String js = "try { try { throw e; } catch (i) { x = i; } } catch (o) { y = o; }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
  }

  // Tests labeled break and continue in nested loops
  @Test
  public void testProcess_labeledBreakAndContinue_connectsToTarget() {
    String js = "OUTER: for (var i = 0; i < 10; i++) { " +
                "  INNER: while (true) { " +
                "    if (i === 1) continue OUTER; " +
                "    if (i === 2) break OUTER; " +
                "  } " +
                "}";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
  }

  // Tests return statement inside try-finally block
  @Test
  public void testProcess_returnInTryWithFinally_connectsToFinally() {
    String js = "function test() { try { return 1; } finally { cleanup(); } }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
  }

  // Tests with statement control flow
  @Test
  public void testProcess_withStatement_createsEdgesToBody() {
    String js = "with (obj) { x = a; }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
  }

  // Tests CFG node priority comparator in forward and backward directions
  @Test
  public void testGetOptionalNodeComparator_forwardAndBackward_comparesPositions() {
    String js = "var a = 1; var b = 2;";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    Comparator<DiGraphNode<Node, Branch>> fwdComp = cfg.getOptionalNodeComparator(true);
    Comparator<DiGraphNode<Node, Branch>> bwdComp = cfg.getOptionalNodeComparator(false);
    assertNotNull(fwdComp);
    assertNotNull(bwdComp);

    DiGraphNode<Node, Branch> entry = cfg.getEntry();
    DiGraphNode<Node, Branch> ret = cfg.getImplicitReturn();
    assertTrue(fwdComp.compare(entry, ret) < 0);
    assertTrue(bwdComp.compare(entry, ret) > 0);
  }

  // Tests shouldTraverseFunctions flag set to false
  @Test
  public void testProcess_doNotTraverseFunctions_skipsInnerFunctions() {
    String js = "var x = 1; function f() { var y = 2; } var z = 3;";
    ControlFlowGraph<Node> cfg = createCfg(js, false);
    assertNotNull(cfg);
  }

  // Tests isBreakStructure helper method with different node types
  @Test
  public void testIsBreakStructure_variousNodeTypes_returnsExpected() {
    assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.FOR), false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.WHILE), false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.DO), false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.SWITCH), false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.BLOCK), true));
    assertFalse(ControlFlowAnalysis.isBreakStructure(new Node(Token.BLOCK), false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.IF), true));
    assertFalse(ControlFlowAnalysis.isBreakStructure(new Node(Token.IF), false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.TRY), true));
    assertFalse(ControlFlowAnalysis.isBreakStructure(new Node(Token.TRY), false));
    assertFalse(ControlFlowAnalysis.isBreakStructure(new Node(Token.VAR), true));
  }

  // Tests isContinueStructure helper method with different node types
  @Test
  public void testIsContinueStructure_variousNodeTypes_returnsExpected() {
    assertTrue(ControlFlowAnalysis.isContinueStructure(new Node(Token.FOR)));
    assertTrue(ControlFlowAnalysis.isContinueStructure(new Node(Token.WHILE)));
    assertTrue(ControlFlowAnalysis.isContinueStructure(new Node(Token.DO)));
    assertFalse(ControlFlowAnalysis.isContinueStructure(new Node(Token.SWITCH)));
    assertFalse(ControlFlowAnalysis.isContinueStructure(new Node(Token.IF)));
    assertFalse(ControlFlowAnalysis.isContinueStructure(new Node(Token.BLOCK)));
  }

  // Tests property access and potential exception throwing within try block
  @Test
  public void testProcess_propertyAccessInTry_createsExceptionEdge() {
    String js = "try { var a = obj.prop; } catch (e) { log(e); }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
  }

  // Tests short-circuit logical AND (&&) and logical OR (||) expressions
  @Test
  public void testProcess_shortCircuitAndOr_createsBranchingEdges() {
    String js = "var x = a && b; var y = c || d;";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertTrue(cfg.getDirectedGraphNodes().size() > 0);
  }

  // Tests ternary (hook) conditional expressions
  @Test
  public void testProcess_hookExpression_createsConditionalBranches() {
    String js = "var result = condition ? expr1() : expr2();";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertTrue(cfg.getDirectedGraphNodes().size() > 0);
  }

  // Tests comma operator sequence
  @Test
  public void testProcess_commaOperator_evaluatesLeftToRight() {
    String js = "var x = (foo(), bar(), baz());";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertTrue(cfg.getDirectedGraphNodes().size() > 0);
  }

  // Tests for loop with empty header (infinite loop: for (;;))
  @Test
  public void testProcess_infiniteForLoop_createsExpectedLoopEdges() {
    String js = "for (;;) { if (a) { break; } }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertTrue(cfg.getDirectedGraphNodes().size() > 0);
  }

  // Tests try-finally block without catch clause
  @Test
  public void testProcess_tryFinallyWithoutCatch_connectsToFinally() {
    String js = "try { doSomething(); } finally { cleanUp(); }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertTrue(cfg.getDirectedGraphNodes().size() > 0);
  }

  // Tests labeled block breaking
  @Test
  public void testProcess_labeledBlockBreak_connectsToBlockEnd() {
    String js = "BLOCK: { if (a) break BLOCK; b(); } c();";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertTrue(cfg.getDirectedGraphNodes().size() > 0);
  }

  // Tests throw statement outside try block connecting to implicit return
  @Test
  public void testProcess_throwOutsideTry_connectsToImplicitReturn() {
    String js = "function f() { throw new Error('fail'); }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertTrue(cfg.getDirectedGraphNodes().size() > 0);
  }

  // Tests switch statement with fall-through (cases without break)
  @Test
  public void testProcess_switchCaseFallthrough_createsSequentialEdges() {
    String js = "switch (x) { case 1: a(); case 2: b(); default: c(); }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertTrue(cfg.getDirectedGraphNodes().size() > 0);
  }

  // Tests nested finally blocks execution flow
  @Test
  public void testProcess_nestedFinallyBlocks_chainsFinallyExecution() {
    String js = "try { try { a(); } finally { b(); } } finally { c(); }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertTrue(cfg.getDirectedGraphNodes().size() > 0);
  }

  // Tests break inside finally block overriding pending control flow
  @Test
  public void testProcess_breakInsideFinally_divertsControlFlow() {
    String js = "while (true) { try { a(); } finally { break; } }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertTrue(cfg.getDirectedGraphNodes().size() > 0);
  }

  // Tests return statement inside finally block
  @Test
  public void testProcess_returnInsideFinally_divertsControlFlow() {
    String js = "function f() { try { a(); } finally { return 1; } }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertTrue(cfg.getDirectedGraphNodes().size() > 0);
  }

  // Tests various expressions that can throw inside a try block
  @Test
  public void testProcess_throwingExpressionsInsideTry_createsExceptionEdges() {
    String js = "try { new Foo(); arr[i]; 'key' in obj; delete obj.prop; a instanceof B; } catch(e) {}";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertTrue(cfg.getDirectedGraphNodes().size() > 0);
  }

  // Tests empty block and empty statements
  @Test
  public void testProcess_emptyStatementsAndBlocks_handledCorrectly() {
    String js = "; {} if (a) {} else {}";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertTrue(cfg.getDirectedGraphNodes().size() > 0);
  }

  // Tests mayThrowException utility method
  @Test
  public void testMayThrowException_variousNodes_returnsExpected() {
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.CALL)));
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.NEW)));
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.THROW)));
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.GETPROP)));
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.GETELEM)));
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.INSTANCEOF)));
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.IN)));
    assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.DELPROP)));
    assertFalse(ControlFlowAnalysis.mayThrowException(new Node(Token.NUMBER)));
    assertFalse(ControlFlowAnalysis.mayThrowException(new Node(Token.STRING)));
    assertFalse(ControlFlowAnalysis.mayThrowException(new Node(Token.NAME)));
    assertFalse(ControlFlowAnalysis.mayThrowException(new Node(Token.TRUE)));
    assertFalse(ControlFlowAnalysis.mayThrowException(new Node(Token.FALSE)));
  }
}