package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test class for MustBeReachingVariableDef.
 * Designed to detect the defect in Defects4J bug 30b (missing ON_EX edges).
 */
public class MustBeReachingVariableDefTest {

  // Helper: parse JS code and return the function node.
  private Node parseFunction(String code) {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    Node script = compiler.parseSyntheticCode(code);
    // Assume code is a function expression or statement.
    return script.getFirstChild(); // FUNCTION node
  }

  // Helper: create a MustBeReachingVariableDef for a given function.
  private MustBeReachingVariableDef analyzeFunction(Node function) {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, true);
    ControlFlowGraph<Node> cfg = cfa.analyze(function);

    // Obtain the scope for the function.
    Scope scope = new SyntacticScopeCreator(compiler).createScope(function, null);
    MustBeReachingVariableDef analysis =
        new MustBeReachingVariableDef(cfg, scope, compiler);
    analysis.analyze();
    return analysis;
  }

  // Test 1: Basic definition – variable assigned and then used.
  // Expected: definition reaches the use.
  @Test
  public void testSimpleAssignment_useAfterDef_returnsDefinition() {
    Node function = parseFunction("function f() { var x = 1; return x; }");
    MustBeReachingVariableDef analysis = analyzeFunction(function);

    // Find the return node
    Node returnNode = function.getLastChild().getLastChild(); // BLOCK -> RETURN
    Node def = analysis.getDef("x", returnNode);
    assertNotNull("Definition should reach the return", def);
  }

  // Test 2: Definition inside an if block – conditional definition.
  // Expected: definition does NOT reach after the if (must-reach false).
  @Test
  public void testConditionalDefinition_insideIf_returnsNull() {
    Node function = parseFunction("function f(a) { if (a) { var x = 1; } return x; }");
    MustBeReachingVariableDef analysis = analyzeFunction(function);

    Node returnNode = function.getLastChild().getLastChild(); // RETURN
    Node def = analysis.getDef("x", returnNode);
    assertNull("Conditional definition should not be must-reach", def);
  }

  // Test 3: Definition inside a try block (no exception path considered).
  // This test exposes the known defect: the analysis does not account for
  // exceptional control flow, so it incorrectly reports a must-reach definition.
  // On a fixed version, the definition should NOT be must-reach.
  @Test
  public void testTryCatch_definitionInTry_returnsNullWhenFixed() {
    Node function = parseFunction("function f() { var x = 1; try { x = 2; } catch(e) {} return x; }");
    MustBeReachingVariableDef analysis = analyzeFunction(function);

    Node returnNode = function.getLastChild().getLastChild(); // RETURN
    Node def = analysis.getDef("x", returnNode);
    // Buggy version returns non-null (definition of x=2).
    // Fixed version should return null because exception path skips the definition.
    assertNull("Definition in try should not be must-reach due to exception path", def);
  }

  // Test 4: Definition inside a catch block – conditional.
  // Expected: definition does not reach after the catch.
  @Test
  public void testCatchBlock_definitionInCatch_returnsNull() {
    Node function = parseFunction("function f() { try { var x = 1; } catch(e) { x = 2; } return x; }");
    MustBeReachingVariableDef analysis = analyzeFunction(function);

    Node returnNode = function.getLastChild().getLastChild(); // RETURN
    Node def = analysis.getDef("x", returnNode);
    assertNull("Definition in catch should not be must-reach", def);
  }

  // Test 5: Variable defined in a for-in loop.
  // Expected: the loop variable definition reaches inside the loop body.
  @Test
  public void testForIn_loopVariable_returnsDefinition() {
    Node function = parseFunction("function f(obj) { for (var x in obj) { alert(x); } }");
    MustBeReachingVariableDef analysis = analyzeFunction(function);

    // Find the alert node (first statement after for)
    Node forNode = function.getLastChild().getFirstChild(); // BLOCK -> FOR
    Node body = forNode.getLastChild(); // FOR body
    Node alertNode = body.getFirstChild();
    Node def = analysis.getDef("x", alertNode);
    assertNotNull("Loop variable should be defined inside the loop", def);
  }

  // Test 6: Use of arguments escapes parameters.
  // Expected: parameters become BOTTOM.
  @Test
  public void testArguments_useEscapesParameters_parametersAreBottom() {
    Node function = parseFunction("function f(a) { arguments; return a; }");
    MustBeReachingVariableDef analysis = analyzeFunction(function);

    Node returnNode = function.getLastChild().getLastChild(); // RETURN
    Node def = analysis.getDef("a", returnNode);
    assertNull("Parameter should be BOTTOM after arguments usage", def);
  }

  // Test 7: Variable that depends on an escaped parameter is also set to BOTTOM.
  @Test
  public void testDependentVariable_parameterEscaped_bottom() {
    Node function = parseFunction("function f(a) { var x = a; arguments; return x; }");
    MustBeReachingVariableDef analysis = analyzeFunction(function);

    Node returnNode = function.getLastChild().getLastChild(); // RETURN
    Node def = analysis.getDef("x", returnNode);
    assertNull("Variable depending on escaped parameter becomes BOTTOM", def);
  }

  // Test 8: Definition in a for loop – condition expression only.
  @Test
  public void testForLoop_conditionOnly_doesNotDefine() {
    Node function = parseFunction("function f() { for (var i = 0; i < 10; i++) { var x = 1; } return x; }");
    MustBeReachingVariableDef analysis = analyzeFunction(function);

    Node returnNode = function.getLastChild().getLastChild(); // RETURN
    Node def = analysis.getDef("x", returnNode);
    assertNull("x defined inside loop should not reach after loop", def);
  }

  // Test 9: Variable defined in while loop – conditional.
  @Test
  public void testWhileLoop_definitionInside_returnsNull() {
    Node function = parseFunction("function f() { while (true) { var x = 1; } return x; }");
    MustBeReachingVariableDef analysis = analyzeFunction(function);

    Node returnNode = function.getLastChild().getLastChild(); // RETURN
    Node def = analysis.getDef("x", returnNode);
    assertNull("Definition inside while should not be must-reach", def);
  }

  // Test 10: Local variable that is not escaped – must reach.
  @Test
  public void testNonEscapedVariable_simpleUse_returnsDefinition() {
    Node function = parseFunction("function f() { var x = 1; var y = x; return y; }");
    MustBeReachingVariableDef analysis = analyzeFunction(function);

    Node returnNode = function.getLastChild().getLastChild(); // RETURN
    Node def = analysis.getDef("y", returnNode);
    assertNotNull("Definition of y should reach the return", def);
  }

  // Test 11: Variable defined and then reassigned – last definition wins.
  @Test
  public void testMultipleAssignments_lastDefinitionReaches() {
    Node function = parseFunction("function f() { var x = 1; x = 2; return x; }");
    MustBeReachingVariableDef analysis = analyzeFunction(function);

    Node returnNode = function.getLastChild().getLastChild(); // RETURN
    Node def = analysis.getDef("x", returnNode);
    assertNotNull("Last definition should reach", def);
  }

  // Test 12: Variable used before definition – should not reach.
  @Test
  public void testUseBeforeDefinition_returnsNull() {
    Node function = parseFunction("function f() { return x; var x = 1; }");
    MustBeReachingVariableDef analysis = analyzeFunction(function);

    Node returnNode = function.getLastChild().getFirstChild(); // RETURN is first child of BLOCK
    Node def = analysis.getDef("x", returnNode);
    assertNull("Use before definition should not have reaching definition", def);
  }

  // Test 13: Function parameter is always defined at entry.
  @Test
  public void testParameter_useAtReturn_returnsEntryNode() {
    Node function = parseFunction("function f(a) { return a; }");
    MustBeReachingVariableDef analysis = analyzeFunction(function);

    Node returnNode = function.getLastChild().getLastChild(); // RETURN
    Node def = analysis.getDef("a", returnNode);
    assertNotNull("Parameter should have reaching definition at entry", def);
  }
}