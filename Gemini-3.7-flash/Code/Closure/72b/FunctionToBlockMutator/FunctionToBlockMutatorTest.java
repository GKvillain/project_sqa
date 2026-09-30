package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FunctionToBlockMutatorTest {

  private Compiler compiler;

  private static class TestSupplier implements Supplier<String> {
    private int id = 0;

    @Override
    public String get() {
      return String.valueOf(id++);
    }
  }

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private Node parseFunction(String js) {
    Node root = compiler.parseTestCode(js);
    Node first = root.getFirstChild();
    if (first.isFunction()) {
      return first;
    }
    if (first.isExprResult() && first.getFirstChild().isFunction()) {
      return first.getFirstChild();
    }
    if (first.isVar() && first.getFirstChild().hasChildren()
        && first.getFirstChild().getFirstChild().isFunction()) {
      return first.getFirstChild().getFirstChild();
    }
    return first;
  }

  private Node parseCall(String js) {
    Node root = compiler.parseTestCode(js);
    return root.getFirstChild().getFirstChild();
  }

  // Tests LabelNameSupplier generating expected label names
  @Test
  public void testLabelNameSupplier_validIdSupplier_returnsFormattedLabel() {
    Supplier<String> idSupplier = new TestSupplier();
    FunctionToBlockMutator.LabelNameSupplier supplier =
        new FunctionToBlockMutator.LabelNameSupplier(idSupplier);

    assertEquals("JSCompiler_inline_label_0", supplier.get());
    assertEquals("JSCompiler_inline_label_1", supplier.get());
  }

  // Tests basic mutation of a simple function with single return and resultName
  @Test
  public void testMutate_simpleReturnWithResultName_replacesReturnWithAssignment() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, new TestSupplier());

    Node fnNode = parseFunction("function foo() { return 1; }");
    Node callNode = parseCall("foo()");

    Node result = mutator.mutate("foo", fnNode, callNode, "result", false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    assertEquals(1, result.getChildCount());
    Node expr = result.getFirstChild();
    assertEquals(Token.EXPR_RESULT, expr.getType());
    Node assign = expr.getFirstChild();
    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals("result", assign.getFirstChild().getString());
  }

  // Tests mutation without return statement and needsDefaultResult is true
  @Test
  public void testMutate_noReturnNeedsDefaultResult_addsDummyAssignment() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, new TestSupplier());

    Node fnNode = parseFunction("function foo() { var a = 1; }");
    Node callNode = parseCall("foo()");

    Node result = mutator.mutate("foo", fnNode, callNode, "res", true, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    Node lastStmt = result.getLastChild();
    assertEquals(Token.EXPR_RESULT, lastStmt.getType());
    Node assign = lastStmt.getFirstChild();
    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals("res", assign.getFirstChild().getString());
  }

  // Tests mutation with null function name (anonymous function)
  @Test
  public void testMutate_nullFunctionName_usesAnonLabelPrefix() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, new TestSupplier());

    Node fnNode = parseFunction("function () { if (true) { return 1; } return 2; }");
    Node callNode = parseCall("foo()");

    Node result = mutator.mutate(null, fnNode, callNode, "res", false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    Node labelNode = result.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    Node labelNameNode = labelNode.getFirstChild();
    assertTrue(labelNameNode.getString().startsWith("JSCompiler_inline_label_anon_"));
  }

  // Tests mutation with empty function name
  @Test
  public void testMutate_emptyFunctionName_usesAnonLabelPrefix() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, new TestSupplier());

    Node fnNode = parseFunction("function () { if (true) { return 1; } return 2; }");
    Node callNode = parseCall("foo()");

    Node result = mutator.mutate("", fnNode, callNode, "res", false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    Node labelNode = result.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    Node labelNameNode = labelNode.getFirstChild();
    assertTrue(labelNameNode.getString().startsWith("JSCompiler_inline_label_anon_"));
  }

  // Tests multiple return statements requiring label and break statements
  @Test
  public void testMutate_multipleReturns_generatesLabelAndBreak() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, new TestSupplier());

    Node fnNode = parseFunction("function foo(x) { if (x) { return 1; } return 2; }");
    Node callNode = parseCall("foo(true)");

    Node result = mutator.mutate("foo", fnNode, callNode, "res", false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    Node label = result.getFirstChild();
    assertEquals(Token.LABEL, label.getType());
  }

  // Tests call in loop initializing uninitialized var declarations
  @Test
  public void testMutate_isCallInLoopTrue_initializesUninitializedVars() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, new TestSupplier());

    Node fnNode = parseFunction("function foo() { var a; return 1; }");
    Node callNode = parseCall("foo()");

    Node result = mutator.mutate("foo", fnNode, callNode, null, false, true);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    Node varNode = result.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    Node nameNode = varNode.getFirstChild();
    assertTrue(nameNode.hasChildren());
    assertEquals(Token.VOID, nameNode.getFirstChild().getType());
  }

  // Tests call in loop with loop structure not modified inside the function body
  @Test
  public void testMutate_isCallInLoopWithInnerLoop_preservesLoopStructure() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, new TestSupplier());

    Node fnNode = parseFunction("function foo() { for (var k in obj) {} return 1; }");
    Node callNode = parseCall("foo()");

    Node result = mutator.mutate("foo", fnNode, callNode, null, false, true);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
  }

  // Tests inlining arguments with modified parameter requiring aliasing
  @Test
  public void testMutate_modifiedParameters_createsAliases() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, new TestSupplier());

    Node fnNode = parseFunction("function foo(x) { x = x + 1; return x; }");
    Node callNode = parseCall("foo(5)");

    Node result = mutator.mutate("foo", fnNode, callNode, "res", false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    assertEquals(Token.VAR, result.getFirstChild().getType());
  }

  // Tests empty return statement with no resultName (return removed)
  @Test
  public void testMutate_emptyReturnWithoutResultName_removesReturn() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, new TestSupplier());

    Node fnNode = parseFunction("function foo() { return; }");
    Node callNode = parseCall("foo()");

    Node result = mutator.mutate("foo", fnNode, callNode, null, false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    assertEquals(0, result.getChildCount());
  }

  // Tests empty return statement with resultName (assigns undefined)
  @Test
  public void testMutate_emptyReturnWithResultName_assignsUndefined() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, new TestSupplier());

    Node fnNode = parseFunction("function foo() { return; }");
    Node callNode = parseCall("foo()");

    Node result = mutator.mutate("foo", fnNode, callNode, "res", false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    assertEquals(1, result.getChildCount());
    Node expr = result.getFirstChild();
    assertEquals(Token.EXPR_RESULT, expr.getType());
    Node assign = expr.getFirstChild();
    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals(Token.VOID, assign.getLastChild().getType());
  }

  // Tests return value without resultName (becomes expression statement)
  @Test
  public void testMutate_returnWithoutResultName_becomesExpressionStatement() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, new TestSupplier());

    Node fnNode = parseFunction("function foo() { return bar(); }");
    Node callNode = parseCall("foo()");

    Node result = mutator.mutate("foo", fnNode, callNode, null, false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    assertEquals(1, result.getChildCount());
    Node expr = result.getFirstChild();
    assertEquals(Token.EXPR_RESULT, expr.getType());
    assertEquals(Token.CALL, expr.getFirstChild().getType());
  }

  // Tests call with side-effect arguments that are unused by parameters
  @Test
  public void testMutate_unusedSideEffectArguments_preservesSideEffects() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, new TestSupplier());

    Node fnNode = parseFunction("function foo() { return 1; }");
    Node callNode = parseCall("foo(bar())");

    Node result = mutator.mutate("foo", fnNode, callNode, "res", false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    Node firstStmt = result.getFirstChild();
    assertEquals(Token.EXPR_RESULT, firstStmt.getType());
    assertEquals(Token.CALL, firstStmt.getFirstChild().getType());
  }

  // Tests multiple returns with empty return inside conditional
  @Test
  public void testMutate_multipleReturnsWithEmptyReturn_assignsUndefinedAndBreaks() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, new TestSupplier());

    Node fnNode = parseFunction("function foo(x) { if (x) { return; } return 1; }");
    Node callNode = parseCall("foo(true)");

    Node result = mutator.mutate("foo", fnNode, callNode, "res", false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    Node labelNode = result.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    Node labelBlock = labelNode.getLastChild();
    Node ifNode = labelBlock.getFirstChild();
    assertEquals(Token.IF, ifNode.getType());
    Node ifBlock = ifNode.getChildAtIndex(1);
    Node assignExpr = ifBlock.getFirstChild();
    assertEquals(Token.EXPR_RESULT, assignExpr.getType());
    assertEquals(Token.ASSIGN, assignExpr.getFirstChild().getType());
    assertEquals(Token.VOID, assignExpr.getFirstChild().getLastChild().getType());
    Node breakNode = ifBlock.getLastChild();
    assertEquals(Token.BREAK, breakNode.getType());
  }

  // Tests multiple returns without resultName creating simple break statements
  @Test
  public void testMutate_multipleReturnsWithoutResultName_createsBreakWithoutAssign() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, new TestSupplier());

    Node fnNode = parseFunction("function foo(x) { if (x) { return; } doSomething(); }");
    Node callNode = parseCall("foo(true)");

    Node result = mutator.mutate("foo", fnNode, callNode, null, false, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    Node labelNode = result.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    Node labelBlock = labelNode.getLastChild();
    Node ifNode = labelBlock.getFirstChild();
    Node ifBlock = ifNode.getChildAtIndex(1);
    assertEquals(Token.BREAK, ifBlock.getFirstChild().getType());
  }

  // Tests call in loop with already initialized var declarations
  @Test
  public void testMutate_isCallInLoopTrue_initializedVarNotOverridden() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, new TestSupplier());

    Node fnNode = parseFunction("function foo() { var a = 42; return a; }");
    Node callNode = parseCall("foo()");

    Node result = mutator.mutate("foo", fnNode, callNode, null, false, true);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    Node varNode = result.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    Node nameNode = varNode.getFirstChild();
    assertTrue(nameNode.hasChildren());
    assertEquals(Token.NUMBER, nameNode.getFirstChild().getType());
  }

  // Tests nested function within inlined function does not modify nested function var declarations
  @Test
  public void testMutate_isCallInLoopWithNestedFunction_doesNotModifyNestedVar() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, new TestSupplier());

    Node fnNode = parseFunction("function foo() { function bar() { var inner; } return 1; }");
    Node callNode = parseCall("foo()");

    Node result = mutator.mutate("foo", fnNode, callNode, null, false, true);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    Node fnChild = result.getFirstChild();
    assertEquals(Token.FUNCTION, fnChild.getType());
    Node innerBlock = fnChild.getLastChild();
    Node innerVar = innerBlock.getFirstChild();
    assertEquals(Token.VAR, innerVar.getType());
    assertFalse(innerVar.getFirstChild().hasChildren());
  }

  // Tests needsDefaultResult is true with multiple return statements
  @Test
  public void testMutate_multipleReturnsWithNeedsDefaultResult_appendsDefaultAssignment() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, new TestSupplier());

    Node fnNode = parseFunction("function foo(x) { if (x) { return 1; } }");
    Node callNode = parseCall("foo(true)");

    Node result = mutator.mutate("foo", fnNode, callNode, "res", true, false);

    assertNotNull(result);
    assertEquals(Token.BLOCK, result.getType());
    Node lastStmt = result.getLastChild();
    assertEquals(Token.EXPR_RESULT, lastStmt.getType());
    Node assign = lastStmt.getFirstChild();
    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals("res", assign.getFirstChild().getString());
    assertEquals(Token.VOID, assign.getLastChild().getType());
  }
}