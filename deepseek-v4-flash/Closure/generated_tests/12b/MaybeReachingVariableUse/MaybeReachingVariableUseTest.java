package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableList;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.NodeUtil;
import com.google.javascript.jscomp.ControlFlowGraph;
import com.google.javascript.jscomp.Scope;
import com.google.javascript.jscomp.graph.GraphNode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.Collection;

public class MaybeReachingVariableUseTest {

  private Compiler compiler;
  private Scope scope;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
  }

  // Helper method to parse JavaScript and create CFG
  private ControlFlowGraph<Node> parseAndBuildCfg(String js) {
    Node root = compiler.parseSyntheticCode(js);
    // Build scope
    scope = new SyntacticScopeCreator(compiler).createScope(root.getFirstChild(), null);
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true, false);
    cfa.analyze(root.getFirstChild());
    return cfa.getCfg();
  }

  // Tests basic variable usage in function
  @Test
  public void testSimpleVariableUse() {
    String js = "function f() { var x = 1; var y = x; }";
    ControlFlowGraph<Node> cfg = parseAndBuildCfg(js);
    MaybeReachingVariableUse analysis = new MaybeReachingVariableUse(cfg, scope, compiler);
    analysis.analyze();
    // Get the node for 'x' assignment
    Node defNode = findNameNode(cfg, "x");
    assertNotNull("Definition node for x should be found", defNode);
    Collection<Node> uses = analysis.getUses("x", defNode);
    // Should have at least one use (in y = x)
    assertNotNull("Uses should not be null", uses);
    assertFalse("Should have at least one use", uses.isEmpty());
  }

  // Tests variable use after assignment in a branch
  @Test
  public void testVariableUseInBranch() {
    String js = "function f() { var x = 1; if (true) { var y = x; } }";
    ControlFlowGraph<Node> cfg = parseAndBuildCfg(js);
    MaybeReachingVariableUse analysis = new MaybeReachingVariableUse(cfg, scope, compiler);
    analysis.analyze();
    Node defNode = findNameNode(cfg, "x");
    assertNotNull("Definition node for x should be found", defNode);
    Collection<Node> uses = analysis.getUses("x", defNode);
    assertNotNull("Uses should not be null", uses);
  }

  // Tests variable overwritten - old definition should not be used
  @Test
  public void testVariableOverwritten() {
    String js = "function f() { var x = 1; x = 2; var y = x; }";
    ControlFlowGraph<Node> cfg = parseAndBuildCfg(js);
    MaybeReachingVariableUse analysis = new MaybeReachingVariableUse(cfg, scope, compiler);
    analysis.analyze();
    // Get the first assignment
    Node firstDef = findNameNode(cfg, "x");
    assertNotNull("Definition node for x should be found", firstDef);
    Collection<Node> uses = analysis.getUses("x", firstDef);
    // The first assignment to x should not have uses because it was overwritten
    assertTrue("First definition should have no uses", uses == null || uses.isEmpty());
  }

  // Tests variable in loop - use after definition
  @Test
  public void testVariableInLoop() {
    String js = "function f() { var x = 1; for (var i = 0; i < 10; i++) { var y = x; } }";
    ControlFlowGraph<Node> cfg = parseAndBuildCfg(js);
    MaybeReachingVariableUse analysis = new MaybeReachingVariableUse(cfg, scope, compiler);
    analysis.analyze();
    Node defNode = findNameNode(cfg, "x");
    assertNotNull("Definition node for x should be found", defNode);
    Collection<Node> uses = analysis.getUses("x", defNode);
    assertNotNull("Uses should not be null", uses);
    assertFalse("Should have at least one use", uses.isEmpty());
  }

  // Tests variable in if-else
  @Test
  public void testVariableInIfElse() {
    String js = "function f(a) { var x = 1; if (a) { var y = x; } else { var z = x; } }";
    ControlFlowGraph<Node> cfg = parseAndBuildCfg(js);
    MaybeReachingVariableUse analysis = new MaybeReachingVariableUse(cfg, scope, compiler);
    analysis.analyze();
    Node defNode = findNameNode(cfg, "x");
    assertNotNull("Definition node for x should be found", defNode);
    Collection<Node> uses = analysis.getUses("x", defNode);
    assertNotNull("Uses should not be null", uses);
    assertFalse("Should have at least one use", uses.isEmpty());
  }

  // Tests variable not used
  @Test
  public void testVariableNotUsed() {
    String js = "function f() { var x = 1; var y = 2; }";
    ControlFlowGraph<Node> cfg = parseAndBuildCfg(js);
    MaybeReachingVariableUse analysis = new MaybeReachingVariableUse(cfg, scope, compiler);
    analysis.analyze();
    Node defNode = findNameNode(cfg, "x");
    assertNotNull("Definition node for x should be found", defNode);
    Collection<Node> uses = analysis.getUses("x", defNode);
    assertTrue("Should have no uses", uses == null || uses.isEmpty());
  }

  // Tests variable in compound assignment
  @Test
  public void testCompoundAssignment() {
    String js = "function f() { var x = 1; x += 2; var y = x; }";
    ControlFlowGraph<Node> cfg = parseAndBuildCfg(js);
    MaybeReachingVariableUse analysis = new MaybeReachingVariableUse(cfg, scope, compiler);
    analysis.analyze();
    Node defNode = findNameNode(cfg, "x");
    assertNotNull("Definition node for x should be found", defNode);
    Collection<Node> uses = analysis.getUses("x", defNode);
    // The initial definition should have uses (in the compound assignment and in y = x)
    assertNotNull("Uses should not be null", uses);
  }

  // Tests for-in loop
  @Test
  public void testForInLoop() {
    String js = "function f(obj) { for (var x in obj) { var y = x; } }";
    ControlFlowGraph<Node> cfg = parseAndBuildCfg(js);
    MaybeReachingVariableUse analysis = new MaybeReachingVariableUse(cfg, scope, compiler);
    analysis.analyze();
    Node defNode = findNameNode(cfg, "x");
    assertNotNull("Definition node for x should be found", defNode);
    Collection<Node> uses = analysis.getUses("x", defNode);
    // In for-in, the variable is defined (implicitly) and used, but may have different behavior
    assertNotNull("Uses should not be null", uses);
  }

  // Tests conditional (ternary) operator
  @Test
  public void testConditionalOperator() {
    String js = "function f(a) { var x = 1; var y = a ? x : 2; }";
    ControlFlowGraph<Node> cfg = parseAndBuildCfg(js);
    MaybeReachingVariableUse analysis = new MaybeReachingVariableUse(cfg, scope, compiler);
    analysis.analyze();
    Node defNode = findNameNode(cfg, "x");
    assertNotNull("Definition node for x should be found", defNode);
    Collection<Node> uses = analysis.getUses("x", defNode);
    assertNotNull("Uses should not be null", uses);
    assertFalse("Should have at least one use", uses.isEmpty());
  }

  // Tests logical operators
  @Test
  public void testLogicalOperator() {
    String js = "function f(a) { var x = 1; var y = a && x; }";
    ControlFlowGraph<Node> cfg = parseAndBuildCfg(js);
    MaybeReachingVariableUse analysis = new MaybeReachingVariableUse(cfg, scope, compiler);
    analysis.analyze();
    Node defNode = findNameNode(cfg, "x");
    assertNotNull("Definition node for x should be found", defNode);
    Collection<Node> uses = analysis.getUses("x", defNode);
    assertNotNull("Uses should not be null", uses);
    assertFalse("Should have at least one use", uses.isEmpty());
  }

  // Tests null input to getUses (invalid node)
  @Test(expected = NullPointerException.class)
  public void testGetUsesWithNull() {
    String js = "function f() { var x = 1; }";
    ControlFlowGraph<Node> cfg = parseAndBuildCfg(js);
    MaybeReachingVariableUse analysis = new MaybeReachingVariableUse(cfg, scope, compiler);
    analysis.analyze();
    analysis.getUses("x", null);
  }

  // Helper method to find a definition node in the CFG
  private Node findNameNode(ControlFlowGraph<Node> cfg, String name) {
    for (GraphNode<Node, ControlFlowGraph.Branch> node : cfg.getNodes()) {
      Node n = node.getValue();
      if (n == null) continue;

      // var x = ...;
      if (n.getType() == Token.VAR) {
        Node varName = n.getFirstChild();
        if (varName != null && varName.getType() == Token.NAME
            && name.equals(varName.getString())) {
          return n;
        }
      }

      // x = ...; or x += ...;
      if (NodeUtil.isAssignmentOp(n)) {
        Node lhs = n.getFirstChild();
        if (lhs != null && lhs.getType() == Token.NAME
            && name.equals(lhs.getString())) {
          return n;
        }
      }

      // EXPR_RESULT wrapping an assignment.
      if (n.getType() == Token.EXPR_RESULT) {
        Node expr = n.getFirstChild();
        if (expr != null && NodeUtil.isAssignmentOp(expr)) {
          Node lhs = expr.getFirstChild();
          if (lhs != null && lhs.getType() == Token.NAME
              && name.equals(lhs.getString())) {
            return n;
          }
        }
      }

      // for (var x in ...)
      if (NodeUtil.isForIn(n)) {
        Node lhs = n.getFirstChild();
        if (lhs != null && lhs.getType() == Token.VAR) {
          lhs = lhs.getLastChild();
        }
        if (lhs != null && lhs.getType() == Token.NAME
            && name.equals(lhs.getString())) {
          return n;
        }
      }
    }
    return null;
  }
}