package com.google.javascript.jscomp;

import com.google.common.collect.Sets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MaybeReachingVariableUseTest {

  private Compiler compiler;
  private Scope scope;
  private ControlFlowGraph<Node> cfg;
  private Node root;
  private Node defNode;
  private Set<Node> useNodes;

  @Before
  public void setUp() {
    compiler = new Compiler();
    useNodes = Sets.newHashSet();
  }

  private MaybeReachingVariableUse computeUse(String src) {
    CompilerOptions options = new CompilerOptions();
    compiler.init(
        Collections.<SourceFile>emptyList(),
        Collections.singletonList(SourceFile.fromCode("testcode", src)),
        options);
    root = compiler.parseInputs();
    assertNotNull(root);

    Node script = root.getFirstChild();
    SyntacticScopeCreator scopeCreator = new SyntacticScopeCreator(compiler);
    scope = scopeCreator.createScope(script, null);

    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true, true);
    cfa.process(null, script);
    cfg = cfa.getCfg();

    findDefAndUseNodes(script);

    MaybeReachingVariableUse analysis = new MaybeReachingVariableUse(cfg, scope, compiler);
    analysis.analyze();
    return analysis;
  }

  private void findDefAndUseNodes(Node n) {
    if (n.isName() && "D".equals(n.getString())) {
      defNode = findEnclosingCfgNode(n);
    }
    if (n.isName() && "U".equals(n.getString())) {
      useNodes.add(findEnclosingCfgNode(n));
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      findDefAndUseNodes(c);
    }
  }

  private Node findEnclosingCfgNode(Node n) {
    while (n != null) {
      if (cfg.hasNode(n)) {
        return n;
      }
      n = n.getParent();
    }
    return null;
  }

  private void assertMatch(String src, String varName, int expectedUses) {
    MaybeReachingVariableUse analysis = computeUse(src);
    assertNotNull("Definition node should be found", defNode);
    Collection<Node> uses = analysis.getUses(varName, defNode);
    assertEquals(expectedUses, uses.size());
    for (Node u : useNodes) {
      assertTrue(uses.contains(u));
    }
  }

  // Tests simple reaching use with VAR declaration
  @Test
  public void testGetUses_simpleVarAssignment_reachesUse() {
    assertMatch("var x = 1; var D = x; var U = x;", "x", 1);
  }

  // Tests that subsequent definition overrides the reaching use
  @Test
  public void testGetUses_redefinition_overridesPreviousUse() {
    assertMatch("var x = 1; var D = x; x = 2; var U = x;", "x", 0);
  }

  // Tests branching with IF condition where use may reach from branch
  @Test
  public void testGetUses_ifBranch_reachesUse() {
    assertMatch("var x = 1; var D = x; if (condition) { var U = x; }", "x", 1);
  }

  // Tests loop construct where variable is used inside WHILE body
  @Test
  public void testGetUses_whileLoop_reachesUse() {
    assertMatch("var x = 1; var D = x; while (true) { var U = x; }", "x", 1);
  }

  // Tests DO WHILE loop condition and body
  @Test
  public void testGetUses_doWhileLoop_reachesUse() {
    assertMatch("var x = 1; var D = x; do { var U = x; } while (x < 10);", "x", 1);
  }

  // Tests FOR loop with condition and increment
  @Test
  public void testGetUses_forLoop_reachesUse() {
    assertMatch("var x = 1; var D = x; for (var i = 0; i < 10; i++) { var U = x; }", "x", 1);
  }

  // Tests FOR-IN loop syntax
  @Test
  public void testGetUses_forInLoop_reachesUse() {
    assertMatch("var x = 1; var y = {}; var D = x; for (var k in y) { var U = x; }", "x", 1);
  }

  // Tests FOR-IN loop defining iterating variable without var
  @Test
  public void testGetUses_forInLoopRedefineVar_reachesUse() {
    assertMatch("var x = 1; var y = {}; var D = x; for (x in y) { var U = x; }", "x", 1);
  }

  // Tests HOOK (ternary) operator condition and branches
  @Test
  public void testGetUses_hookOperator_reachesUse() {
    assertMatch("var x = 1; var D = x; var res = condition ? (var U = x) : 0;", "x", 1);
  }

  // Tests logical AND operator short-circuit behavior
  @Test
  public void testGetUses_andOperator_reachesUse() {
    assertMatch("var x = 1; var D = x; condition && (var U = x);", "x", 1);
  }

  // Tests logical OR operator short-circuit behavior
  @Test
  public void testGetUses_orOperator_reachesUse() {
    assertMatch("var x = 1; var D = x; condition || (var U = x);", "x", 1);
  }

  // Tests compound assignment operator reading the variable (e.g. +=)
  @Test
  public void testGetUses_compoundAssignment_readsVariable() {
    assertMatch("var x = 1; var D = x; x += (var U = 2);", "x", 0);
  }

  // Tests exception path checking in flowThrough (Bug 12)
  @Test
  public void testGetUses_tryCatchFinally_variableReachesCatch() {
    assertMatch("var x = 1; var D = x; try { x = 2; throw 'err'; } catch (e) { var U = x; }", "x", 1);
  }

  // Tests ReachingUses lattice equals and hashCode
  @Test
  public void testReachingUses_equalsAndHashCode_success() {
    MaybeReachingVariableUse.ReachingUses u1 = new MaybeReachingVariableUse.ReachingUses();
    MaybeReachingVariableUse.ReachingUses u2 = new MaybeReachingVariableUse.ReachingUses(u1);

    assertEquals(u1, u2);
    assertEquals(u1.hashCode(), u2.hashCode());
    assertFalse(u1.equals(new Object()));
  }

  // Tests flow direction is backward analysis
  @Test
  public void testIsForward_direction_isFalse() {
    computeUse("var x = 1;");
    MaybeReachingVariableUse analysis = new MaybeReachingVariableUse(cfg, scope, compiler);
    assertFalse(analysis.isForward());
  }

  // Tests increment operator (INC)
  @Test
  public void testGetUses_postIncrement_readsAndDefinesVariable() {
    assertMatch("var x = 1; var D = x; x++; var U = x;", "x", 0);
  }

  // Tests decrement operator (DEC)
  @Test
  public void testGetUses_preDecrement_readsAndDefinesVariable() {
    assertMatch("var x = 1; var D = x; --x; var U = x;", "x", 0);
  }

  // Tests switch statement cases
  @Test
  public void testGetUses_switchStatement_reachesUsesInBranches() {
    assertMatch(
        "var x = 1; var D = x; switch (condition) { case 1: var U = x; break; default: var U = x; }",
        "x",
        2);
  }

  // Tests multiple uses reaching definition from both IF and ELSE branches
  @Test
  public void testGetUses_ifElseBranches_bothReachDefinition() {
    assertMatch(
        "var x = 1; var D = x; if (condition) { var U = x; } else { var U = x; }",
        "x",
        2);
  }

  // Tests function call with variable as argument
  @Test
  public void testGetUses_functionCallArgument_reachesUse() {
    assertMatch("function foo(arg) {} var x = 1; var D = x; foo(var U = x);", "x", 1);
  }

  // Tests query for variable not defined in scope
  @Test
  public void testGetUses_nonExistentVariable_returnsEmptyUses() {
    computeUse("var x = 1; var D = x;");
    Collection<Node> uses = new MaybeReachingVariableUse(cfg, scope, compiler).getUses("nonExistent", defNode);
    assertTrue(uses.isEmpty());
  }

  // Tests join of ReachingUses with different states
  @Test
  public void testJoin_branchesWithDifferentUses_combinesUses() {
    MaybeReachingVariableUse analysis = computeUse(
        "var x = 1; var D = x; if (cond) { var U = x; } else { x = 2; var U = x; }");
    Collection<Node> uses = analysis.getUses("x", defNode);
    assertEquals(1, uses.size());
  }

  // Tests inner function accessing variable (escaped variable)
  @Test
  public void testGetUses_innerFunctionReference_variableEscapes() {
    assertMatch("var x = 1; var D = x; function inner() { var U = x; }", "x", 1);
  }
}