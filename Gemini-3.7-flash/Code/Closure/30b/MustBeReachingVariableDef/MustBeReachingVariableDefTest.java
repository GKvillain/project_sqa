package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

public class MustBeReachingVariableDefTest {

  private ControlFlowGraph<Node> cfg;
  private Scope jsScope;
  private AbstractCompiler compiler;

  private MustBeReachingVariableDef computeAnalysis(String js) {
    compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    Node script = root.getFirstChild();
    Node targetNode = script;

    SyntacticScopeCreator scopeCreator = new SyntacticScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root, null);

    if (script.hasChildren() && script.getFirstChild().isFunction()) {
      Node function = script.getFirstChild();
      targetNode = function.getLastChild(); // Function body (BLOCK)
      jsScope = scopeCreator.createScope(function, globalScope);
    } else {
      jsScope = globalScope;
    }

    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, true);
    cfa.process(null, targetNode);
    cfg = cfa.getCfg();

    MustBeReachingVariableDef analysis =
        new MustBeReachingVariableDef(cfg, jsScope, compiler);
    analysis.analyze();
    return analysis;
  }

  private Node findNode(Node root, final int tokenType, final String name) {
    if (root.getType() == tokenType) {
      if (name == null || name.equals(root.getString())) {
        return root;
      }
    }
    for (Node c = root.getFirstChild(); c != null; c = c.getNext()) {
      Node res = findNode(c, tokenType, name);
      if (res != null) {
        return res;
      }
    }
    return null;
  }

  private Node findLastNode(Node root, final int tokenType, final String name) {
    Node last = null;
    if (root.getType() == tokenType) {
      if (name == null || name.equals(root.getString())) {
        last = root;
      }
    }
    for (Node c = root.getFirstChild(); c != null; c = c.getNext()) {
      Node res = findLastNode(c, tokenType, name);
      if (res != null) {
        last = res;
      }
    }
    return last;
  }

  // Tests simple linear reaching definition for local variable
  @Test
  public void testGetDef_simpleLinearAssignment_returnsDefinitionNode() {
    String js = "function f() { var x = 1; var y = x; }";
    MustBeReachingVariableDef analysis = computeAnalysis(js);

    Node useNode = findLastNode(compiler.getRoot(), Token.VAR, null);
    assertNotNull(useNode);

    Node defNode = analysis.getDef("x", useNode);
    assertNotNull(defNode);
    assertEquals(Token.VAR, defNode.getType());
  }

  // Tests conditional assignment causing multiple definitions (BOTTOM)
  @Test
  public void testGetDef_conditionalBranch_returnsNullDueToJoin() {
    String js = "function f(cond) { var x = 1; if (cond) { x = 2; } var y = x; }";
    MustBeReachingVariableDef analysis = computeAnalysis(js);

    Node useNode = findLastNode(compiler.getRoot(), Token.VAR, null);
    assertNotNull(useNode);

    Node defNode = analysis.getDef("x", useNode);
    assertNull(defNode);
  }

  // Tests loop-based redefinition invalidating reaching definition
  @Test
  public void testGetDef_whileLoopRedefinition_returnsNull() {
    String js = "function f(cond) { var x = 1; while (cond) { x = 2; } var y = x; }";
    MustBeReachingVariableDef analysis = computeAnalysis(js);

    Node useNode = findLastNode(compiler.getRoot(), Token.VAR, null);
    assertNotNull(useNode);

    Node defNode = analysis.getDef("x", useNode);
    assertNull(defNode);
  }

  // Tests increment operator definition
  @Test
  public void testGetDef_incrementOperator_definesVariable() {
    String js = "function f() { var x = 1; x++; var y = x; }";
    MustBeReachingVariableDef analysis = computeAnalysis(js);

    Node useNode = findLastNode(compiler.getRoot(), Token.VAR, null);
    assertNotNull(useNode);

    Node defNode = analysis.getDef("x", useNode);
    assertNotNull(defNode);
    assertEquals(Token.EXPR_RESULT, defNode.getType());
    assertEquals(Token.INC, defNode.getFirstChild().getType());
  }

  // Tests decrement operator definition
  @Test
  public void testGetDef_decrementOperator_definesVariable() {
    String js = "function f() { var x = 1; x--; var y = x; }";
    MustBeReachingVariableDef analysis = computeAnalysis(js);

    Node useNode = findLastNode(compiler.getRoot(), Token.VAR, null);
    assertNotNull(useNode);

    Node defNode = analysis.getDef("x", useNode);
    assertNotNull(defNode);
    assertEquals(Token.EXPR_RESULT, defNode.getType());
    assertEquals(Token.DEC, defNode.getFirstChild().getType());
  }

  // Tests for-in loop definition
  @Test
  public void testGetDef_forInLoop_definesVariable() {
    String js = "function f(obj) { var x; for (x in obj) { var y = x; } }";
    MustBeReachingVariableDef analysis = computeAnalysis(js);

    Node useNode = findLastNode(compiler.getRoot(), Token.VAR, null);
    assertNotNull(useNode);

    Node defNode = analysis.getDef("x", useNode);
    assertNotNull(defNode);
    assertEquals(Token.FOR, defNode.getType());
  }

  // Tests hook (ternary) conditional expression
  @Test
  public void testGetDef_hookExpression_handlesBranches() {
    String js = "function f(cond) { var x = 0; cond ? (x = 1) : (x = 2); var y = x; }";
    MustBeReachingVariableDef analysis = computeAnalysis(js);

    Node useNode = findLastNode(compiler.getRoot(), Token.VAR, null);
    assertNotNull(useNode);

    Node defNode = analysis.getDef("x", useNode);
    assertNull(defNode);
  }

  // Tests logical AND conditional definition
  @Test
  public void testGetDef_andExpression_treatsRightHandAsConditional() {
    String js = "function f(cond) { var x = 1; cond && (x = 2); var y = x; }";
    MustBeReachingVariableDef analysis = computeAnalysis(js);

    Node useNode = findLastNode(compiler.getRoot(), Token.VAR, null);
    assertNotNull(useNode);

    Node defNode = analysis.getDef("x", useNode);
    assertNull(defNode);
  }

  // Tests arguments object assignment escaping parameters
  @Test
  public void testGetDef_argumentsAssignment_escapesParameters() {
    String js = "function f(x) { arguments[0] = 2; var y = x; }";
    MustBeReachingVariableDef analysis = computeAnalysis(js);

    Node useNode = findLastNode(compiler.getRoot(), Token.VAR, null);
    assertNotNull(useNode);

    Node defNode = analysis.getDef("x", useNode);
    assertNull(defNode);
  }

  // Tests arguments referenced directly
  @Test
  public void testGetDef_argumentsReference_escapesParameters() {
    String js = "function f(x) { var args = arguments; var y = x; }";
    MustBeReachingVariableDef analysis = computeAnalysis(js);

    Node useNode = findLastNode(compiler.getRoot(), Token.VAR, null);
    assertNotNull(useNode);

    Node defNode = analysis.getDef("x", useNode);
    assertNull(defNode);
  }

  // Tests dependency on outer scope variables returning true
  @Test
  public void testDependsOnOuterScopeVars_outerVarDependency_returnsTrue() {
    String js = "var outer = 1; function f() { var x = outer; var y = x; }";
    MustBeReachingVariableDef analysis = computeAnalysis(js);

    Node useNode = findLastNode(compiler.getRoot(), Token.VAR, null);
    assertNotNull(useNode);

    boolean depends = analysis.dependsOnOuterScopeVars("x", useNode);
    assertTrue(depends);
  }

  // Tests dependency on local variables only returning false
  @Test
  public void testDependsOnOuterScopeVars_localOnlyDependency_returnsFalse() {
    String js = "function f() { var a = 1; var x = a; var y = x; }";
    MustBeReachingVariableDef analysis = computeAnalysis(js);

    Node useNode = findLastNode(compiler.getRoot(), Token.VAR, null);
    assertNotNull(useNode);

    boolean depends = analysis.dependsOnOuterScopeVars("x", useNode);
    assertFalse(depends);
  }

  // Tests invalid node passed to getDef throws exception
  @Test(expected = IllegalArgumentException.class)
  public void testGetDef_nodeNotInCfg_throwsException() {
    String js = "function f() { var x = 1; }";
    MustBeReachingVariableDef analysis = computeAnalysis(js);

    Node invalidNode = new Node(Token.EMPTY);
    analysis.getDef("x", invalidNode);
  }

  // Tests invalid node passed to dependsOnOuterScopeVars throws exception
  @Test(expected = IllegalArgumentException.class)
  public void testDependsOnOuterScopeVars_nodeNotInCfg_throwsException() {
    String js = "function f() { var x = 1; }";
    MustBeReachingVariableDef analysis = computeAnalysis(js);

    Node invalidNode = new Node(Token.EMPTY);
    analysis.dependsOnOuterScopeVars("x", invalidNode);
  }

  // Tests MustDef equals contract
  @Test
  public void testMustDef_equalsContract() {
    MustBeReachingVariableDef.MustDef def1 = new MustBeReachingVariableDef.MustDef();
    MustBeReachingVariableDef.MustDef def2 = new MustBeReachingVariableDef.MustDef();
    assertEquals(def1, def2);
    assertFalse(def1.equals("nonMustDefObject"));
  }

  // Tests analysis forward direction property
  @Test
  public void testIsForward_returnsTrue() {
    String js = "function f() { var x = 1; }";
    MustBeReachingVariableDef analysis = computeAnalysis(js);
    assertTrue(analysis.isForward());
  }

  // Tests dependent variable redefined clears dependent reaching definitions
  @Test
  public void testGetDef_dependentVariableRedefined_invalidatesReachingDef() {
    String js = "function f() { var a = 1; var b = a; a = 2; var c = b; }";
    MustBeReachingVariableDef analysis = computeAnalysis(js);

    Node useNode = findLastNode(compiler.getRoot(), Token.VAR, null);
    assertNotNull(useNode);

    Node defNode = analysis.getDef("b", useNode);
    assertNull(defNode);
  }

  // Tests logical OR conditional definition
  @Test
  public void testGetDef_orExpression_treatsRightHandAsConditional() {
    String js = "function f(cond) { var x = 1; cond || (x = 2); var y = x; }";
    MustBeReachingVariableDef analysis = computeAnalysis(js);

    Node useNode = findLastNode(compiler.getRoot(), Token.VAR, null);
    assertNotNull(useNode);

    Node defNode = analysis.getDef("x", useNode);
    assertNull(defNode);
  }

  // Tests simple assignment statement (ASSIGN)
  @Test
  public void testGetDef_simpleAssignStatement_returnsAssignNode() {
    String js = "function f() { var x; x = 1; var y = x; }";
    MustBeReachingVariableDef analysis = computeAnalysis(js);

    Node useNode = findLastNode(compiler.getRoot(), Token.VAR, null);
    assertNotNull(useNode);

    Node defNode = analysis.getDef("x", useNode);
    assertNotNull(defNode);
    assertEquals(Token.EXPR_RESULT, defNode.getType());
    assertEquals(Token.ASSIGN, defNode.getFirstChild().getType());
  }

  // Tests compound assignment (e.g., +=)
  @Test
  public void testGetDef_compoundAssignment_definesVariable() {
    String js = "function f() { var x = 1; x += 2; var y = x; }";
    MustBeReachingVariableDef analysis = computeAnalysis(js);

    Node useNode = findLastNode(compiler.getRoot(), Token.VAR, null);
    assertNotNull(useNode);

    Node defNode = analysis.getDef("x", useNode);
    assertNotNull(defNode);
    assertEquals(Token.EXPR_RESULT, defNode.getType());
    assertEquals(Token.ASSIGN_ADD, defNode.getFirstChild().getType());
  }

  // Tests inner function declaration
  @Test
  public void testGetDef_functionDeclaration_definesName() {
    String js = "function f() { function g() {} var y = g; }";
    MustBeReachingVariableDef analysis = computeAnalysis(js);

    Node useNode = findLastNode(compiler.getRoot(), Token.VAR, null);
    assertNotNull(useNode);

    Node defNode = analysis.getDef("g", useNode);
    assertNotNull(defNode);
    assertEquals(Token.FUNCTION, defNode.getType());
  }

  // Tests unknown variable lookup returns null
  @Test
  public void testGetDef_unknownVariable_returnsNull() {
    String js = "function f() { var x = 1; var y = x; }";
    MustBeReachingVariableDef analysis = computeAnalysis(js);

    Node useNode = findLastNode(compiler.getRoot(), Token.VAR, null);
    assertNotNull(useNode);

    Node defNode = analysis.getDef("unknownVar", useNode);
    assertNull(defNode);
  }

  // Tests function call causing escaping / side effects
  @Test
  public void testGetDef_functionCallEscapes_modifiesReachingDef() {
    String js = "var g = 1; function f() { var x = g; externalFunc(); var y = x; }";
    MustBeReachingVariableDef analysis = computeAnalysis(js);

    Node useNode = findLastNode(compiler.getRoot(), Token.VAR, null);
    assertNotNull(useNode);

    Node defNode = analysis.getDef("x", useNode);
    assertNull(defNode);
  }

  // Tests for-in loop with var declaration in loop header
  @Test
  public void testGetDef_forInWithVarDeclaration_definesVariable() {
    String js = "function f(obj) { for (var x in obj) { var y = x; } }";
    MustBeReachingVariableDef analysis = computeAnalysis(js);

    Node useNode = findLastNode(compiler.getRoot(), Token.VAR, null);
    assertNotNull(useNode);

    Node defNode = analysis.getDef("x", useNode);
    assertNotNull(defNode);
    assertEquals(Token.FOR, defNode.getType());
  }
}