package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.TernaryValue;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class NodeUtilTest {

  // Tests getPureBooleanValue for primitive and literal nodes
  @Test
  public void testGetPureBooleanValue_primitives_returnsExpectedTernaryValue() {
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(new Node(Token.TRUE)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(new Node(Token.FALSE)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(new Node(Token.NULL)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(Node.newString("hello")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newString("")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(Node.newNumber(1.0)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newNumber(0.0)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newString(Token.NAME, "undefined")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newString(Token.NAME, "NaN")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(Node.newString(Token.NAME, "Infinity")));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getPureBooleanValue(Node.newString(Token.NAME, "foo")));
  }

  // Tests getPureBooleanValue for VOID and NOT nodes
  @Test
  public void testGetPureBooleanValue_voidAndNot_returnsCorrectTernaryValue() {
    Node voidZero = new Node(Token.VOID, Node.newNumber(0));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(voidZero));

    Node notTrue = new Node(Token.NOT, new Node(Token.TRUE));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(notTrue));

    Node notFalse = new Node(Token.NOT, new Node(Token.FALSE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(notFalse));
  }

  // Tests getImpureBooleanValue for logical operators and expressions
  @Test
  public void testGetImpureBooleanValue_logicalAndHookOps_returnsCombinedValue() {
    Node andNode = new Node(Token.AND, new Node(Token.TRUE), new Node(Token.FALSE));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(andNode));

    Node orNode = new Node(Token.OR, new Node(Token.FALSE), new Node(Token.TRUE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(orNode));

    Node hookNode = new Node(Token.HOOK,
        new Node(Token.TRUE),
        new Node(Token.TRUE),
        new Node(Token.TRUE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(hookNode));

    Node hookDiff = new Node(Token.HOOK,
        new Node(Token.TRUE),
        new Node(Token.TRUE),
        new Node(Token.FALSE));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getImpureBooleanValue(hookDiff));

    Node commaNode = new Node(Token.COMMA, new Node(Token.FALSE), new Node(Token.TRUE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(commaNode));
  }

  // Tests getStringValue conversions for literals and numbers
  @Test
  public void testGetStringValue_variousNodeTypes_returnsCorrectString() {
    assertEquals("test", NodeUtil.getStringValue(Node.newString("test")));
    assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
    assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID, Node.newNumber(0))));
    assertEquals("42", NodeUtil.getStringValue(Node.newNumber(42.0)));
    assertEquals("3.14", NodeUtil.getStringValue(Node.newNumber(3.14)));
    assertEquals("undefined", NodeUtil.getStringValue(Node.newString(Token.NAME, "undefined")));
    assertEquals("Infinity", NodeUtil.getStringValue(Node.newString(Token.NAME, "Infinity")));
    assertEquals("NaN", NodeUtil.getStringValue(Node.newString(Token.NAME, "NaN")));
  }

  // Tests getStringValue for Array and Object literals
  @Test
  public void testGetStringValue_arrayAndObjectLiterals_returnsFormattedString() {
    Node array = new Node(Token.ARRAYLIT, Node.newString("a"), Node.newString("b"));
    assertEquals("a,b", NodeUtil.getStringValue(array));

    Node emptyArray = new Node(Token.ARRAYLIT);
    assertEquals("", NodeUtil.getStringValue(emptyArray));

    Node objLit = new Node(Token.OBJECTLIT);
    assertEquals("[object Object]", NodeUtil.getStringValue(objLit));
  }

  // Tests getNumberValue for various node types
  @Test
  public void testGetNumberValue_variousNodeTypes_returnsExpectedDouble() {
    assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(new Node(Token.TRUE)));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.FALSE)));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.NULL)));
    assertEquals(Double.valueOf(123.0), NodeUtil.getNumberValue(Node.newNumber(123.0)));
    assertEquals(Double.valueOf(Double.NaN), NodeUtil.getNumberValue(new Node(Token.VOID, Node.newNumber(0))));
    assertEquals(Double.valueOf(Double.NaN), NodeUtil.getNumberValue(Node.newString(Token.NAME, "NaN")));
    assertEquals(Double.valueOf(Double.NaN), NodeUtil.getNumberValue(Node.newString(Token.NAME, "undefined")));
    assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), NodeUtil.getNumberValue(Node.newString(Token.NAME, "Infinity")));

    Node negInfinity = new Node(Token.NEG, Node.newString(Token.NAME, "Infinity"));
    assertEquals(Double.valueOf(Double.NEGATIVE_INFINITY), NodeUtil.getNumberValue(negInfinity));

    assertEquals(Double.valueOf(100.0), NodeUtil.getNumberValue(Node.newString("100")));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(Node.newString("")));
  }

  // Tests getStringNumberValue parsing with hex, whitespace and invalid numbers
  @Test
  public void testGetStringNumberValue_specialStrings_returnsParsedDouble() {
    assertEquals(Double.valueOf(16.0), NodeUtil.getStringNumberValue("0x10"));
    assertEquals(Double.valueOf(255.0), NodeUtil.getStringNumberValue(" 0xFF "));
    assertNull(NodeUtil.getStringNumberValue("+0x10"));
    assertNull(NodeUtil.getStringNumberValue("infinity"));
    assertNull(NodeUtil.getStringNumberValue("hello\u000bworld"));
    assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("notANumber")));
  }

  // Tests isImmutableValue for immutable and mutable AST nodes
  @Test
  public void testIsImmutableValue_variousNodes_returnsExpectedBoolean() {
    assertTrue(NodeUtil.isImmutableValue(Node.newString("str")));
    assertTrue(NodeUtil.isImmutableValue(Node.newNumber(123)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.VOID, Node.newNumber(0))));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "undefined")));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "NaN")));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "Infinity")));

    assertFalse(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "x")));
    assertFalse(NodeUtil.isImmutableValue(new Node(Token.ARRAYLIT)));
  }

  // Tests isLiteralValue for Array, Object and Function nodes
  @Test
  public void testIsLiteralValue_complexLiterals_evaluatesCorrectly() {
    Node arrayLit = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newString("a"));
    assertTrue(NodeUtil.isLiteralValue(arrayLit, false));

    Node arrayWithVar = new Node(Token.ARRAYLIT, Node.newString(Token.NAME, "variable"));
    assertFalse(NodeUtil.isLiteralValue(arrayWithVar, false));

    Node objLit = new Node(Token.OBJECTLIT);
    Node key = Node.newString(Token.STRING, "k");
    key.addChildToBack(Node.newNumber(1));
    objLit.addChildToBack(key);
    assertTrue(NodeUtil.isLiteralValue(objLit, false));
  }

  // Tests isValidDefineValue for valid and invalid define expressions
  @Test
  public void testIsValidDefineValue_expressions_checksValidConstants() {
    Set<String> defines = new HashSet<String>();
    defines.add("DEF_A");

    assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(1), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newString("val"), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "DEF_A"), defines));
    assertFalse(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "UNKNOWN_DEF"), defines));

    Node addNode = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.isValidDefineValue(addNode, defines));

    Node notNode = new Node(Token.NOT, new Node(Token.TRUE));
    assertTrue(NodeUtil.isValidDefineValue(notNode, defines));
  }

  // Tests precedence method for operator tokens
  @Test
  public void testPrecedence_variousTokens_returnsCorrectPrecedence() {
    assertEquals(0, NodeUtil.precedence(Token.COMMA));
    assertEquals(1, NodeUtil.precedence(Token.ASSIGN));
    assertEquals(2, NodeUtil.precedence(Token.HOOK));
    assertEquals(3, NodeUtil.precedence(Token.OR));
    assertEquals(4, NodeUtil.precedence(Token.AND));
    assertEquals(8, NodeUtil.precedence(Token.EQ));
    assertEquals(11, NodeUtil.precedence(Token.ADD));
    assertEquals(12, NodeUtil.precedence(Token.MUL));
    assertEquals(13, NodeUtil.precedence(Token.NOT));
    assertEquals(15, NodeUtil.precedence(Token.NAME));
  }

  // Tests opToStr and opToStrNoFail mappings
  @Test
  public void testOpToStr_validAndInvalidOperators_returnsRepresentation() {
    assertEquals("+", NodeUtil.opToStr(Token.ADD));
    assertEquals("-", NodeUtil.opToStr(Token.SUB));
    assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    assertEquals("!", NodeUtil.opToStr(Token.NOT));
    assertEquals("=", NodeUtil.opToStr(Token.ASSIGN));
    assertNull(NodeUtil.opToStr(Token.FUNCTION));

    assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));
  }

  // Tests opToStrNoFail throwing Error on invalid operator token
  @Test(expected = Error.class)
  public void testOpToStrNoFail_invalidOperator_throwsError() {
    NodeUtil.opToStrNoFail(Token.FUNCTION);
  }

  // Tests mayHaveSideEffects for pure expressions vs side-effecting operations
  @Test
  public void testMayHaveSideEffects_variousNodes_returnsExpectedState() {
    assertFalse(NodeUtil.mayHaveSideEffects(Node.newNumber(1)));
    assertFalse(NodeUtil.mayHaveSideEffects(Node.newString("str")));
    assertFalse(NodeUtil.mayHaveSideEffects(new Node(Token.EMPTY)));
    assertFalse(NodeUtil.mayHaveSideEffects(new Node(Token.BLOCK)));

    assertTrue(NodeUtil.mayHaveSideEffects(new Node(Token.THROW, Node.newString("err"))));

    Node assign = new Node(Token.ASSIGN,
        Node.newString(Token.NAME, "x"),
        Node.newNumber(1));
    assertTrue(NodeUtil.mayHaveSideEffects(assign));
  }

  // Tests evaluatesToLocalValue for various expression types
  @Test
  public void testEvaluatesToLocalValue_variousNodes_returnsLocalStatus() {
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newNumber(1)));
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newString("abc")));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ARRAYLIT)));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.OBJECTLIT)));

    Node addNode = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.evaluatesToLocalValue(addNode));

    Node commaNode = new Node(Token.COMMA, Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.evaluatesToLocalValue(commaNode));
  }

  // Tests isNumericResult, isBooleanResult, and mayBeString helpers
  @Test
  public void testTypePredicates_nodes_identifiesTypesCorrectly() {
    Node numAdd = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    Node strAdd = new Node(Token.ADD, Node.newString("a"), Node.newString("b"));
    Node eq = new Node(Token.EQ, Node.newNumber(1), Node.newNumber(2));

    assertTrue(NodeUtil.isNumericResult(numAdd));
    assertTrue(NodeUtil.isBooleanResult(eq));
    assertFalse(NodeUtil.isBooleanResult(numAdd));
    assertTrue(NodeUtil.mayBeString(strAdd));
    assertFalse(NodeUtil.mayBeString(numAdd));
  }

  // Tests isAssociative and isCommutative for operators
  @Test
  public void testIsAssociativeAndCommutative_operatorTokens_returnsCorrectBooleans() {
    assertTrue(NodeUtil.isAssociative(Token.MUL));
    assertTrue(NodeUtil.isAssociative(Token.AND));
    assertTrue(NodeUtil.isAssociative(Token.OR));
    assertFalse(NodeUtil.isAssociative(Token.ADD));
    assertFalse(NodeUtil.isAssociative(Token.SUB));

    assertTrue(NodeUtil.isCommutative(Token.MUL));
    assertTrue(NodeUtil.isCommutative(Token.BITOR));
    assertFalse(NodeUtil.isCommutative(Token.ADD));
    assertFalse(NodeUtil.isCommutative(Token.DIV));
  }

  // Tests removeChild safely removing statement from BLOCK
  @Test
  public void testRemoveChild_statementInBlock_removesChildSuccessfully() {
    Node block = new Node(Token.BLOCK);
    Node expr1 = NodeUtil.newExpr(Node.newNumber(1));
    Node expr2 = NodeUtil.newExpr(Node.newNumber(2));
    block.addChildToBack(expr1);
    block.addChildToBack(expr2);

    NodeUtil.removeChild(block, expr1);
    assertEquals(1, block.getChildCount());
    assertSame(expr2, block.getFirstChild());
  }

  // Tests isEmptyBlock and tryMergeBlock
  @Test
  public void testTryMergeBlock_blockInParent_mergesChildren() {
    Node parentBlock = new Node(Token.BLOCK);
    Node innerBlock = new Node(Token.BLOCK);
    Node expr = NodeUtil.newExpr(Node.newNumber(10));
    innerBlock.addChildToBack(expr);
    parentBlock.addChildToBack(innerBlock);

    assertTrue(NodeUtil.tryMergeBlock(innerBlock));
    assertEquals(1, parentBlock.getChildCount());
    assertSame(expr, parentBlock.getFirstChild());
  }

  // Tests getFunctionName and isFunctionDeclaration
  @Test
  public void testFunctionHelpers_functionNodes_returnsNameAndDeclarationStatus() {
    Node body = new Node(Token.BLOCK);
    Node params = new Node(Token.LP);
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "myFunc"), params, body);

    Node block = new Node(Token.BLOCK, fn);
    assertTrue(NodeUtil.isFunctionDeclaration(fn));
    assertFalse(NodeUtil.isFunctionExpression(fn));
    assertEquals("myFunc", NodeUtil.getFunctionName(fn));
  }

  // Tests isStatement and isStatementBlock
  @Test
  public void testStatementPredicates_statementAndExpressionNodes_returnsCorrectBooleans() {
    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
    Node exprResult = NodeUtil.newExpr(Node.newNumber(1));
    Node block = new Node(Token.BLOCK);
    Node ifNode = new Node(Token.IF, new Node(Token.TRUE), new Node(Token.BLOCK));

    assertTrue(NodeUtil.isStatement(varNode));
    assertTrue(NodeUtil.isStatement(exprResult));
    assertTrue(NodeUtil.isStatement(ifNode));
    assertFalse(NodeUtil.isStatement(Node.newNumber(1)));

    assertTrue(NodeUtil.isStatementBlock(block));
    assertFalse(NodeUtil.isStatementBlock(varNode));
  }

  // Tests isLoopStructure and getConditionExpression for loop/conditional nodes
  @Test
  public void testLoopAndConditionHelpers_controlStructures_extractsConditionCorrectly() {
    Node whileCond = new Node(Token.TRUE);
    Node whileLoop = new Node(Token.WHILE, whileCond, new Node(Token.BLOCK));
    assertTrue(NodeUtil.isLoopStructure(whileLoop));
    assertSame(whileCond, NodeUtil.getConditionExpression(whileLoop));

    Node ifCond = new Node(Token.FALSE);
    Node ifNode = new Node(Token.IF, ifCond, new Node(Token.BLOCK));
    assertFalse(NodeUtil.isLoopStructure(ifNode));
    assertSame(ifCond, NodeUtil.getConditionExpression(ifNode));

    Node hookCond = Node.newString(Token.NAME, "cond");
    Node hookNode = new Node(Token.HOOK, hookCond, Node.newNumber(1), Node.newNumber(2));
    assertSame(hookCond, NodeUtil.getConditionExpression(hookNode));

    Node forCond = Node.newString(Token.NAME, "i");
    Node forLoop = new Node(Token.FOR, new Node(Token.EMPTY), forCond, new Node(Token.EMPTY), new Node(Token.BLOCK));
    assertTrue(NodeUtil.isLoopStructure(forLoop));
    assertSame(forCond, NodeUtil.getConditionExpression(forLoop));
  }

  // Tests AST node factory helpers (newVarNode, newQualifiedNameNode, getRootOfQualifiedName)
  @Test
  public void testAstFactoryAndQualifiedName_constructedNodes_matchesStructure() {
    Node varDecl = NodeUtil.newVarNode("myVar", Node.newNumber(10));
    assertEquals(Token.VAR, varDecl.getType());
    assertEquals(1, varDecl.getChildCount());
    Node nameNode = varDecl.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertEquals("myVar", nameNode.getString());
    assertEquals(Token.NUMBER, nameNode.getFirstChild().getType());

    Node qname = NodeUtil.newQualifiedNameNode(null, "a.b.c");
    assertEquals(Token.GETPROP, qname.getType());
    Node root = NodeUtil.getRootOfQualifiedName(qname);
    assertEquals(Token.NAME, root.getType());
    assertEquals("a", root.getString());
  }

  // Tests isSimpleOperator, isUnaryOperator, and isBinaryOperator
  @Test
  public void testOperatorTypePredicates_variousOperatorNodes_identifiesCategories() {
    Node notNode = new Node(Token.NOT, new Node(Token.TRUE));
    Node addNode = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    Node varNode = new Node(Token.VAR);

    assertTrue(NodeUtil.isUnaryOperator(notNode));
    assertFalse(NodeUtil.isBinaryOperator(notNode));
    assertTrue(NodeUtil.isSimpleOperator(notNode));

    assertFalse(NodeUtil.isUnaryOperator(addNode));
    assertTrue(NodeUtil.isBinaryOperator(addNode));
    assertTrue(NodeUtil.isSimpleOperator(addNode));

    assertFalse(NodeUtil.isSimpleOperator(varNode));
  }

  // Tests isLhs helper for assignments and name declarations
  @Test
  public void testIsLhs_targetExpressions_distinguishesLeftAndRightHandSides() {
    Node lhsName = Node.newString(Token.NAME, "x");
    Node rhsVal = Node.newNumber(42);
    Node assignNode = new Node(Token.ASSIGN, lhsName, rhsVal);

    assertTrue(NodeUtil.isLhs(lhsName, assignNode));
    assertFalse(NodeUtil.isLhs(rhsVal, assignNode));

    Node varName = Node.newString(Token.NAME, "y");
    Node varNode = new Node(Token.VAR, varName);
    assertTrue(NodeUtil.isVarDeclaration(varName));
    assertTrue(NodeUtil.isNameDeclaration(varNode));
  }

  // Tests hasCatchHandler and hasFinally for TRY nodes
  @Test
  public void testTryCatchFinallyHelpers_tryNodes_detectsHandlersCorrectly() {
    Node tryBlock = new Node(Token.BLOCK);
    Node catchBlock = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH, Node.newString(Token.NAME, "e"), catchBlock);
    Node blockWithCatch = new Node(Token.BLOCK, catchNode);
    Node finallyBlock = new Node(Token.BLOCK);

    Node fullTry = new Node(Token.TRY, tryBlock, blockWithCatch, finallyBlock);
    assertTrue(NodeUtil.hasCatchHandler(blockWithCatch));
    assertTrue(NodeUtil.hasFinally(fullTry));

    Node emptyCatchBlock = new Node(Token.BLOCK);
    Node tryWithoutFinally = new Node(Token.TRY, tryBlock, emptyCatchBlock);
    assertFalse(NodeUtil.hasCatchHandler(emptyCatchBlock));
    assertFalse(NodeUtil.hasFinally(tryWithoutFinally));
  }
}