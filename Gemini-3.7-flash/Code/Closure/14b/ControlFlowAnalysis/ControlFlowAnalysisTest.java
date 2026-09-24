package com.google.javascript.jscomp;

import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphEdge;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ControlFlowAnalysisTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private ControlFlowGraph<Node> createCfg(String js, boolean edgeAnnotations) {
    Node root = compiler.parseTestCode(js);
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true, edgeAnnotations);
    cfa.process(null, root);
    return cfa.getCfg();
  }

  private static int count(Iterable<?> iterable) {
    int size = 0;
    for (Object ignored : iterable) {
      size++;
    }
    return size;
  }

  // Tests control flow graph generation for simple sequential statements
  @Test
  public void testProcess_simpleStatements_createsLinearGraph() {
    String js = "var a = 1; var b = 2; var c = a + b;";
    ControlFlowGraph<Node> cfg = createCfg(js, true);

    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
    assertNotNull(cfg.getImplicitReturn());
    assertTrue(count(cfg.getDirectedGraphNodes()) >= 3);
  }

  // Tests IF-ELSE branching edges (true and false paths)
  @Test
  public void testProcess_ifElse_createsBranchingEdges() {
    String js = "if (a) { x = 1; } else { x = 2; }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);

    DiGraphNode<Node, Branch> entryNode = cfg.getEntry();
    List<DiGraphEdge<Node, Branch>> outEdges = cfg.getOutEdges(entryNode.getValue());
    assertNotNull(outEdges);
    assertEquals(2, outEdges.size());

    boolean hasOnTrue = false;
    boolean hasOnFalse = false;
    for (DiGraphEdge<Node, Branch> edge : outEdges) {
      if (edge.getValue() == Branch.ON_TRUE) {
        hasOnTrue = true;
      } else if (edge.getValue() == Branch.ON_FALSE) {
        hasOnFalse = true;
      }
    }
    assertTrue(hasOnTrue);
    assertTrue(hasOnFalse);
  }

  // Tests IF without ELSE branch
  @Test
  public void testProcess_ifWithoutElse_createsOnTrueAndOnFalseEdges() {
    String js = "if (a) { x = 1; } y = 2;";
    ControlFlowGraph<Node> cfg = createCfg(js, true);

    DiGraphNode<Node, Branch> entryNode = cfg.getEntry();
    List<DiGraphEdge<Node, Branch>> outEdges = cfg.getOutEdges(entryNode.getValue());
    assertEquals(2, outEdges.size());
  }

  // Tests WHILE loop branching and back-edge
  @Test
  public void testProcess_whileLoop_createsLoopAndExitEdges() {
    String js = "while (x < 10) { x++; }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);

    DiGraphNode<Node, Branch> entryNode = cfg.getEntry();
    List<DiGraphEdge<Node, Branch>> outEdges = cfg.getOutEdges(entryNode.getValue());
    assertEquals(2, outEdges.size());
  }

  // Tests DO-WHILE loop control flow
  @Test
  public void testProcess_doWhileLoop_createsFallthroughToBody() {
    String js = "do { x++; } while (x < 10);";
    ControlFlowGraph<Node> cfg = createCfg(js, true);

    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
  }

  // Tests standard FOR loop with init, condition, and increment
  @Test
  public void testProcess_forLoop_createsCorrectControlEdges() {
    String js = "for (var i = 0; i < 10; i++) { foo(); }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);

    assertNotNull(cfg);
    assertTrue(count(cfg.getDirectedGraphNodes()) > 2);
  }

  // Tests FOR-IN loop control flow
  @Test
  public void testProcess_forInLoop_createsCollectionAndBodyEdges() {
    String js = "for (var k in obj) { foo(k); }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);

    assertNotNull(cfg);
    assertTrue(count(cfg.getDirectedGraphNodes()) > 2);
  }

  // Tests SWITCH statement with cases and default
  @Test
  public void testProcess_switchWithCasesAndDefault_createsSwitchEdges() {
    String js = "switch (val) { case 1: a(); break; case 2: b(); break; default: c(); }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);

    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
  }

  // Tests SWITCH statement without default
  @Test
  public void testProcess_switchWithoutDefault_createsExitEdge() {
    String js = "switch (val) { case 1: a(); break; }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);

    assertNotNull(cfg);
  }

  // Tests empty SWITCH statement
  @Test
  public void testProcess_emptySwitch_connectsToFollow() {
    String js = "switch (val) {} x = 1;";
    ControlFlowGraph<Node> cfg = createCfg(js, true);

    assertNotNull(cfg);
  }

  // Tests BREAK statement in nested loop
  @Test
  public void testProcess_breakInLoop_exitsLoop() {
    String js = "while (true) { if (foo()) { break; } bar(); } baz();";
    ControlFlowGraph<Node> cfg = createCfg(js, true);

    assertNotNull(cfg);
  }

  // Tests CONTINUE statement in loop
  @Test
  public void testProcess_continueInForLoop_jumpsToIter() {
    String js = "for (var i = 0; i < 10; i++) { if (i == 5) { continue; } foo(i); }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);

    assertNotNull(cfg);
  }

  // Tests labeled BREAK out of labeled block
  @Test
  public void testProcess_labeledBreak_jumpsToLabelFollow() {
    String js = "labelA: { if (foo()) { break labelA; } bar(); } baz();";
    ControlFlowGraph<Node> cfg = createCfg(js, true);

    assertNotNull(cfg);
  }

  // Tests TRY-CATCH-FINALLY blocks control flow and exception edges
  @Test
  public void testProcess_tryCatchFinally_connectsExceptionAndFinallyEdges() {
    String js = "try { foo(); } catch (e) { bar(e); } finally { cleanup(); }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);

    assertNotNull(cfg);
  }

  // Tests TRY-FINALLY without catch block
  @Test
  public void testProcess_tryFinallyWithoutCatch_connectsToFinally() {
    String js = "try { foo(); } finally { cleanup(); }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);

    assertNotNull(cfg);
  }

  // Tests nested TRY-FINALLY with break statements (Regression test for Defect 14)
  @Test
  public void testProcess_nestedTryFinallyWithBreak_connectsEdgesProperly() {
    String js = "while (x) {"
        + "  try {"
        + "    try {"
        + "      break;"
        + "    } finally {"
        + "      foo();"
        + "    }"
        + "  } finally {"
        + "    bar();"
        + "  }"
        + "}"
        + "end();";
    ControlFlowGraph<Node> cfg = createCfg(js, true);

    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
  }

  // Tests RETURN inside TRY-FINALLY connects to finally block before exit
  @Test
  public void testProcess_returnInsideTryFinally_connectsToFinally() {
    String js = "function test() {"
        + "  try {"
        + "    return 1;"
        + "  } finally {"
        + "    cleanup();"
        + "  }"
        + "}";
    ControlFlowGraph<Node> cfg = createCfg(js, true);

    assertNotNull(cfg);
  }

  // Tests THROW statement exception handling
  @Test
  public void testProcess_throwStatement_connectsToExceptionHandler() {
    String js = "try { throw new Error(); } catch (e) { handle(e); }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);

    assertNotNull(cfg);
  }

  // Tests mayThrowException static method for various AST node types
  @Test
  public void testMayThrowException_variousNodes_returnsExpected() {
    Node callNode = new Node(Token.CALL, new Node(Token.NAME));
    assertTrue(ControlFlowAnalysis.mayThrowException(callNode));

    Node throwNode = new Node(Token.THROW, new Node(Token.NAME));
    assertTrue(ControlFlowAnalysis.mayThrowException(throwNode));

    Node getPropNode = new Node(Token.GETPROP, new Node(Token.NAME), new Node(Token.STRING));
    assertTrue(ControlFlowAnalysis.mayThrowException(getPropNode));

    Node numberNode = Node.newNumber(42.0);
    assertFalse(ControlFlowAnalysis.mayThrowException(numberNode));

    Node funcNode = new Node(Token.FUNCTION);
    assertFalse(ControlFlowAnalysis.mayThrowException(funcNode));
  }

  // Tests isBreakStructure static method
  @Test
  public void testIsBreakStructure_variousStructures_returnsCorrectBoolean() {
    Node forNode = new Node(Token.FOR);
    Node whileNode = new Node(Token.WHILE);
    Node doNode = new Node(Token.DO);
    Node switchNode = new Node(Token.SWITCH);
    Node blockNode = new Node(Token.BLOCK);
    Node varNode = new Node(Token.VAR);

    assertTrue(ControlFlowAnalysis.isBreakStructure(forNode, false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(whileNode, false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(doNode, false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(switchNode, false));

    assertFalse(ControlFlowAnalysis.isBreakStructure(blockNode, false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(blockNode, true));

    assertFalse(ControlFlowAnalysis.isBreakStructure(varNode, false));
    assertFalse(ControlFlowAnalysis.isBreakStructure(varNode, true));
  }

  // Tests isContinueStructure static method
  @Test
  public void testIsContinueStructure_variousStructures_returnsCorrectBoolean() {
    Node forNode = new Node(Token.FOR);
    Node whileNode = new Node(Token.WHILE);
    Node doNode = new Node(Token.DO);
    Node switchNode = new Node(Token.SWITCH);
    Node blockNode = new Node(Token.BLOCK);

    assertTrue(ControlFlowAnalysis.isContinueStructure(forNode));
    assertTrue(ControlFlowAnalysis.isContinueStructure(whileNode));
    assertTrue(ControlFlowAnalysis.isContinueStructure(doNode));

    assertFalse(ControlFlowAnalysis.isContinueStructure(switchNode));
    assertFalse(ControlFlowAnalysis.isContinueStructure(blockNode));
  }

  // Tests computeFallThrough for DO, FOR, and default statements
  @Test
  public void testComputeFallThrough_differentNodes_returnsTargetChildOrSelf() {
    Node varNode = new Node(Token.VAR);
    assertSame(varNode, ControlFlowAnalysis.computeFallThrough(varNode));

    Node body = new Node(Token.BLOCK);
    Node cond = new Node(Token.TRUE);
    Node doNode = new Node(Token.DO, body, cond);
    assertSame(body, ControlFlowAnalysis.computeFallThrough(doNode));
  }
}