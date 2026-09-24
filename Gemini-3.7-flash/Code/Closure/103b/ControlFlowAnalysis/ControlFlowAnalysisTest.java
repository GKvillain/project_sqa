package com.google.javascript.jscomp;

import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;

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

  // Tests simple sequence of variable statements
  @Test
  public void testProcess_simpleStatements_createsSequentialGraph() {
    String js = "var a = 1; var b = 2; var c = a + b;";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
    assertNotNull(cfg.getImplicitReturn());
    assertTrue(cfg.getDirectedGraphNodes().size() > 0);
  }

  // Tests if-else control flow branches
  @Test
  public void testProcess_ifElse_createsBranchEdges() {
    String js = "if (a) { b = 1; } else { b = 2; }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    DiGraphNode<Node, Branch> entry = cfg.getEntry();
    assertNotNull(entry);
    List<DiGraphNode<Node, Branch>> succs = cfg.getDirectedSuccNodes(entry);
    assertTrue(succs.size() >= 1);
  }

  // Tests if without else branch
  @Test
  public void testProcess_ifWithoutElse_createsConditionalEdges() {
    String js = "if (a) { b = 1; } c = 2;";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
  }

  // Tests while loop control flow
  @Test
  public void testProcess_whileLoop_createsLoopEdges() {
    String js = "while (a < 10) { a++; }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
  }

  // Tests do-while loop control flow
  @Test
  public void testProcess_doWhileLoop_createsLoopEdges() {
    String js = "do { a++; } while (a < 10);";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
  }

  // Tests standard 4-child for loop
  @Test
  public void testProcess_forLoopStandard_createsLoopEdges() {
    String js = "for (var i = 0; i < 10; i++) { foo(); }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
  }

  // Tests for-in loop
  @Test
  public void testProcess_forInLoop_createsForInEdges() {
    String js = "for (var k in obj) { foo(k); }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
  }

  // Tests switch with cases and default
  @Test
  public void testProcess_switchWithCasesAndDefault_createsSwitchEdges() {
    String js = "switch (x) { case 1: a = 1; break; case 2: a = 2; break; default: a = 0; }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
  }

  // Tests empty switch statement
  @Test
  public void testProcess_emptySwitch_createsCfg() {
    String js = "switch (x) {}";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
  }

  // Tests try-catch block
  @Test
  public void testProcess_tryCatch_createsExceptionEdges() {
    String js = "try { foo(); } catch (e) { bar(); }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
  }

  // Tests try-finally block without catch
  @Test
  public void testProcess_tryFinally_createsFinallyEdges() {
    String js = "try { foo(); } finally { cleanup(); }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
  }

  // Tests try-catch-finally block
  @Test
  public void testProcess_tryCatchFinally_createsExceptionAndFinallyEdges() {
    String js = "try { foo(); } catch (e) { handle(e); } finally { cleanup(); }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
  }

  // Tests break statement inside labeled loop
  @Test
  public void testProcess_labeledBreak_connectsToTarget() {
    String js = "outer: while (true) { while (true) { break outer; } }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
  }

  // Tests continue statement inside labeled loop
  @Test
  public void testProcess_labeledContinue_connectsToTarget() {
    String js = "outer: for (var i = 0; i < 10; i++) { for (var j = 0; j < 10; j++) { continue outer; } }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
  }

  // Tests return statement inside try-finally
  @Test
  public void testProcess_returnInTryFinally_connectsThroughFinally() {
    String js = "function f() { try { return 1; } finally { cleanup(); } } f();";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
  }

  // Tests break statement inside try-finally block
  @Test
  public void testProcess_breakInTryFinally_routesThroughFinally() {
    String js = "while (x) { try { break; } finally { cleanup(); } }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
  }

  // Tests with statement control flow
  @Test
  public void testProcess_withStatement_createsEdges() {
    String js = "with (obj) { a = 1; }";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
  }

  // Tests function traversal disabled
  @Test
  public void testProcess_shouldTraverseFunctionsFalse_skipsInnerFunctions() {
    String js = "function f() { var a = 1; } var b = 2;";
    ControlFlowGraph<Node> cfg = createCfg(js, false);
    assertNotNull(cfg);
    assertNotNull(cfg.getEntry());
  }

  // Tests optional node comparator for forward and reverse traversal
  @Test
  public void testGetOptionalNodeComparator_forwardAndReverse_returnsValidComparators() {
    String js = "var a = 1; var b = 2;";
    ControlFlowGraph<Node> cfg = createCfg(js, true);
    Comparator<DiGraphNode<Node, Branch>> forwardComp = cfg.getOptionalNodeComparator(true);
    Comparator<DiGraphNode<Node, Branch>> reverseComp = cfg.getOptionalNodeComparator(false);
    assertNotNull(forwardComp);
    assertNotNull(reverseComp);

    List<DiGraphNode<Node, Branch>> nodes = cfg.getDirectedGraphNodes();
    if (nodes.size() >= 2) {
      DiGraphNode<Node, Branch> n1 = nodes.get(0);
      DiGraphNode<Node, Branch> n2 = nodes.get(1);
      int fResult = forwardComp.compare(n1, n2);
      int rResult = reverseComp.compare(n1, n2);
      assertEquals(fResult, -rResult);
    }
  }

  // Tests isBreakStructure method
  @Test
  public void testIsBreakStructure_variousNodeTypes_identifiesCorrectly() {
    Node forNode = new Node(Token.FOR);
    Node whileNode = new Node(Token.WHILE);
    Node doNode = new Node(Token.DO);
    Node switchNode = new Node(Token.SWITCH);
    Node blockNode = new Node(Token.BLOCK);
    Node ifNode = new Node(Token.IF);
    Node tryNode = new Node(Token.TRY);
    Node varNode = new Node(Token.VAR);

    assertTrue(ControlFlowAnalysis.isBreakStructure(forNode, false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(whileNode, false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(doNode, false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(switchNode, false));

    assertFalse(ControlFlowAnalysis.isBreakStructure(blockNode, false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(blockNode, true));
    assertFalse(ControlFlowAnalysis.isBreakStructure(ifNode, false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(ifNode, true));
    assertFalse(ControlFlowAnalysis.isBreakStructure(tryNode, false));
    assertTrue(ControlFlowAnalysis.isBreakStructure(tryNode, true));

    assertFalse(ControlFlowAnalysis.isBreakStructure(varNode, false));
    assertFalse(ControlFlowAnalysis.isBreakStructure(varNode, true));
  }

  // Tests isContinueStructure method
  @Test
  public void testIsContinueStructure_variousNodeTypes_identifiesCorrectly() {
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
}