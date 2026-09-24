package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FunctionToBlockMutatorTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private static class SimpleIdSupplier implements Supplier<String> {
    private int id = 0;

    @Override
    public String get() {
      return String.valueOf(id++);
    }
  }

  private FunctionToBlockMutator createMutator() {
    return new FunctionToBlockMutator(compiler, new SimpleIdSupplier());
  }

  private Node parseFunction(String js) {
    Node script = compiler.parseTestCode(js);
    Node first = script.getFirstChild();
    if (first.getType() == Token.FUNCTION) {
      return first;
    }
    if (first.getType() == Token.EXPR_RESULT && first.getFirstChild().getType() == Token.FUNCTION) {
      return first.getFirstChild();
    }
    if (first.getType() == Token.VAR && first.getFirstChild().hasChildren()
        && first.getFirstChild().getFirstChild().getType() == Token.FUNCTION) {
      return first.getFirstChild().getFirstChild();
    }
    throw new IllegalArgumentException("Cannot find function node in: " + js);
  }

  private Node parseCall(String js) {
    Node script = compiler.parseTestCode(js);
    Node first = script.getFirstChild();
    if (first.getType() == Token.EXPR_RESULT && first.getFirstChild().getType() == Token.CALL) {
      return first.getFirstChild();
    }
    if (first.getType() == Token.CALL) {
      return first;
    }
    throw new IllegalArgumentException("Cannot find call node in: " + js);
  }

  // Tests LabelNameSupplier returns expected formatted label
  @Test
  public void testLabelNameSupplier_generatesExpectedLabelFormat() {
    SimpleIdSupplier idSupplier = new SimpleIdSupplier();
    FunctionToBlockMutator.LabelNameSupplier supplier =
        new FunctionToBlockMutator.LabelNameSupplier(idSupplier);
    assertEquals("JSCompiler_inline_label_0", supplier.get());
    assertEquals("JSCompiler_inline_label_1", supplier.get());
  }

  // Tests mutate on a simple function without arguments and no returns
  @Test
  public void testMutate_simpleFunctionNoArgsNoReturn_returnsBlock() {
    FunctionToBlockMutator mutator = createMutator();
    Node fnNode = parseFunction("function foo() { var a = 1; }");
    Node callNode = parseCall("foo()");

    Node result = mutator.mutate("foo", fnNode, callNode, null, false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    assertTrue(result.hasChildren());
  }

  // Tests mutate on function with single return at end and no resultName required
  @Test
  public void testMutate_singleReturnAtEndNoResultName_convertsReturnToExpr() {
    FunctionToBlockMutator mutator = createMutator();
    Node fnNode = parseFunction("function foo() { return 1; }");
    Node callNode = parseCall("foo()");

    Node result = mutator.mutate("foo", fnNode, callNode, null, false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    assertEquals(Token.EXPR_RESULT, result.getFirstChild().getType());
  }

  // Tests mutate on function with return at end and resultName provided
  @Test
  public void testMutate_singleReturnWithResultName_convertsToAssignment() {
    FunctionToBlockMutator mutator = createMutator();
    Node fnNode = parseFunction("function foo() { return 1; }");
    Node callNode = parseCall("foo()");

    Node result = mutator.mutate("foo", fnNode, callNode, "res", false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    Node expr = result.getFirstChild();
    assertEquals(Token.EXPR_RESULT, expr.getType());
    Node assign = expr.getFirstChild();
    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals("res", assign.getFirstChild().getString());
  }

  // Tests mutate on empty return with resultName provided
  @Test
  public void testMutate_emptyReturnWithResultName_assignsUndefined() {
    FunctionToBlockMutator mutator = createMutator();
    Node fnNode = parseFunction("function foo() { return; }");
    Node callNode = parseCall("foo()");

    Node result = mutator.mutate("foo", fnNode, callNode, "res", false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    Node expr = result.getFirstChild();
    assertEquals(Token.EXPR_RESULT, expr.getType());
    Node assign = expr.getFirstChild();
    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals(Token.VOID, assign.getLastChild().getType());
  }

  // Tests mutate on empty return without resultName
  @Test
  public void testMutate_emptyReturnWithoutResultName_removesReturn() {
    FunctionToBlockMutator mutator = createMutator();
    Node fnNode = parseFunction("function foo() { return; }");
    Node callNode = parseCall("foo()");

    Node result = mutator.mutate("foo", fnNode, callNode, null, false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    assertFalse(result.hasChildren());
  }

  // Tests mutate with multiple returns needing labeled block and breaks
  @Test
  public void testMutate_multipleReturns_createsLabeledBlockWithBreaks() {
    FunctionToBlockMutator mutator = createMutator();
    Node fnNode = parseFunction("function foo(x) { if (x) { return 1; } return 2; }");
    Node callNode = parseCall("foo(true)");

    Node result = mutator.mutate("foo", fnNode, callNode, "res", false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    Node labelNode = result.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    Node labelNameNode = labelNode.getFirstChild();
    assertEquals(Token.LABEL_NAME, labelNameNode.getType());
    assertTrue(labelNameNode.getString().startsWith("JSCompiler_inline_label_foo_"));
  }

  // Tests mutate with anonymous function name handling
  @Test
  public void testMutate_anonymousFunctionName_usesAnonInLabel() {
    FunctionToBlockMutator mutator = createMutator();
    Node fnNode = parseFunction("function(x) { if (x) { return 1; } return 2; }");
    Node callNode = parseCall("f(true)");

    Node result = mutator.mutate("", fnNode, callNode, "res", false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    Node labelNode = result.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    Node labelNameNode = labelNode.getFirstChild();
    assertTrue(labelNameNode.getString().startsWith("JSCompiler_inline_label_anon_"));
  }

  // Tests mutate with null function name handling
  @Test
  public void testMutate_nullFunctionName_usesAnonInLabel() {
    FunctionToBlockMutator mutator = createMutator();
    Node fnNode = parseFunction("function(x) { if (x) { return 1; } return 2; }");
    Node callNode = parseCall("f(true)");

    Node result = mutator.mutate(null, fnNode, callNode, "res", false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    Node labelNode = result.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    Node labelNameNode = labelNode.getFirstChild();
    assertTrue(labelNameNode.getString().startsWith("JSCompiler_inline_label_anon_"));
  }

  // Tests mutate when needsDefaultResult is true and function has no exit return
  @Test
  public void testMutate_needsDefaultResultWithoutExitReturn_addsDummyAssignment() {
    FunctionToBlockMutator mutator = createMutator();
    Node fnNode = parseFunction("function foo() { var a = 1; }");
    Node callNode = parseCall("foo()");

    Node result = mutator.mutate("foo", fnNode, callNode, "res", true, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    Node lastChild = result.getLastChild();
    assertEquals(Token.EXPR_RESULT, lastChild.getType());
    Node assign = lastChild.getFirstChild();
    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals("res", assign.getFirstChild().getString());
    assertEquals(Token.VOID, assign.getLastChild().getType());
  }

  // Tests mutate when isCallInLoop is true with uninitialized var declarations
  @Test
  public void testMutate_isCallInLoopWithUninitializedVar_initializesToUndefined() {
    FunctionToBlockMutator mutator = createMutator();
    Node fnNode = parseFunction("function foo() { var a; var b = 1; }");
    Node callNode = parseCall("foo()");

    Node result = mutator.mutate("foo", fnNode, callNode, null, false, true);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    Node firstVar = result.getFirstChild();
    assertEquals(Token.VAR, firstVar.getType());
    Node varName = firstVar.getFirstChild();
    assertTrue(varName.hasChildren());
    assertEquals(Token.VOID, varName.getFirstChild().getType());
  }

  // Tests mutate with loop inside function when isCallInLoop is true
  @Test
  public void testMutate_isCallInLoopWithInnerLoopStructure_skipsLoopVars() {
    FunctionToBlockMutator mutator = createMutator();
    Node fnNode = parseFunction("function foo() { for (var k in obj) {} }");
    Node callNode = parseCall("foo()");

    Node result = mutator.mutate("foo", fnNode, callNode, null, false, true);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
  }

  // Tests mutate with parameters that are modified in function body
  @Test
  public void testMutate_modifiedParameters_createsLocalAliases() {
    FunctionToBlockMutator mutator = createMutator();
    Node fnNode = parseFunction("function foo(x) { x = x + 1; return x; }");
    Node callNode = parseCall("foo(5)");

    Node result = mutator.mutate("foo", fnNode, callNode, "res", false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    Node firstChild = result.getFirstChild();
    assertEquals(Token.VAR, firstChild.getType());
  }

  // Tests mutate with multiple arguments and inlined directly when unmodified
  @Test
  public void testMutate_unmodifiedParameters_inlinedDirectly() {
    FunctionToBlockMutator mutator = createMutator();
    Node fnNode = parseFunction("function foo(a, b) { return a + b; }");
    Node callNode = parseCall("foo(1, 2)");

    Node result = mutator.mutate("foo", fnNode, callNode, "res", false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    assertEquals(Token.EXPR_RESULT, result.getFirstChild().getType());
  }

  // Tests mutate with multiple returns including an empty return statement
  @Test
  public void testMutate_multipleReturnsWithEmptyReturn_assignsUndefinedOnEmptyReturn() {
    FunctionToBlockMutator mutator = createMutator();
    Node fnNode = parseFunction("function foo(x) { if (x) { return; } return 1; }");
    Node callNode = parseCall("foo(true)");

    Node result = mutator.mutate("foo", fnNode, callNode, "res", false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    Node labelNode = result.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
  }

  // Tests mutate with nested function containing returns to ensure inner returns are not modified
  @Test
  public void testMutate_nestedFunctionReturns_leavesInnerFunctionReturnsIntact() {
    FunctionToBlockMutator mutator = createMutator();
    Node fnNode = parseFunction("function foo(x) { function inner() { return 1; } if (x) { return inner(); } return 2; }");
    Node callNode = parseCall("foo(true)");

    Node result = mutator.mutate("foo", fnNode, callNode, "res", false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    Node labelNode = result.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
  }

  // Tests mutate when isCallInLoop is true and multiple vars are declared in a single var statement
  @Test
  public void testMutate_isCallInLoopWithMultipleVarsInSingleVarStatement_initializesUninitializedOnes() {
    FunctionToBlockMutator mutator = createMutator();
    Node fnNode = parseFunction("function foo() { var a, b = 2, c; }");
    Node callNode = parseCall("foo()");

    Node result = mutator.mutate("foo", fnNode, callNode, null, false, true);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    Node varNode = result.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());

    Node aNode = varNode.getFirstChild();
    assertEquals("a", aNode.getString());
    assertTrue(aNode.hasChildren());
    assertEquals(Token.VOID, aNode.getFirstChild().getType());

    Node bNode = aNode.getNext();
    assertEquals("b", bNode.getString());
    assertTrue(bNode.hasChildren());
    assertEquals(Token.NUMBER, bNode.getFirstChild().getType());

    Node cNode = bNode.getNext();
    assertEquals("c", cNode.getString());
    assertTrue(cNode.hasChildren());
    assertEquals(Token.VOID, cNode.getFirstChild().getType());
  }

  // Tests mutate with extra arguments having side effects
  @Test
  public void testMutate_extraArgumentsWithSideEffects_evaluatesSideEffects() {
    FunctionToBlockMutator mutator = createMutator();
    Node fnNode = parseFunction("function foo(a) { return a; }");
    Node callNode = parseCall("foo(1, bar())");

    Node result = mutator.mutate("foo", fnNode, callNode, "res", false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
  }

  // Tests mutate with fewer arguments than parameters
  @Test
  public void testMutate_fewerArgumentsThanParameters_inlinesParametersCorrectly() {
    FunctionToBlockMutator mutator = createMutator();
    Node fnNode = parseFunction("function foo(a, b) { return a; }");
    Node callNode = parseCall("foo(1)");

    Node result = mutator.mutate("foo", fnNode, callNode, "res", false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    assertEquals(Token.EXPR_RESULT, result.getFirstChild().getType());
  }
}