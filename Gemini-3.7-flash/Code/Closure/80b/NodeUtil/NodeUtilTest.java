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

  // Tests isBooleanResult for comparison and DELPROP expressions
  @Test
  public void testIsBooleanResult_delpropAndComparison_returnsTrue() {
    Node delprop = new Node(Token.DELPROP, Node.newString(Token.NAME, "prop"));
    assertTrue(NodeUtil.isBooleanResult(delprop));

    Node eq = new Node(Token.EQ, Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.isBooleanResult(eq));

    Node not = new Node(Token.NOT, Node.newString(Token.NAME, "a"));
    assertTrue(NodeUtil.isBooleanResult(not));

    Node num = Node.newNumber(1);
    assertFalse(NodeUtil.isBooleanResult(num));
  }

  // Tests isNumericResult for arithmetic operators and literals
  @Test
  public void testIsNumericResult_arithmeticAndNumbers_returnsTrue() {
    Node add = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.isNumericResult(add));

    Node sub = new Node(Token.SUB, Node.newNumber(5), Node.newNumber(3));
    assertTrue(NodeUtil.isNumericResult(sub));

    Node nan = Node.newString(Token.NAME, "NaN");
    assertTrue(NodeUtil.isNumericResult(nan));

    Node str = Node.newString("hello");
    assertFalse(NodeUtil.isNumericResult(str));
  }

  // Tests mayBeString evaluation
  @Test
  public void testMayBeString_variousNodes_identifiesCorrectly() {
    Node str = Node.newString("test");
    assertTrue(NodeUtil.mayBeString(str));

    Node num = Node.newNumber(123);
    assertFalse(NodeUtil.mayBeString(num));

    Node bool = new Node(Token.TRUE);
    assertFalse(NodeUtil.mayBeString(bool));

    Node nullNode = new Node(Token.NULL);
    assertFalse(NodeUtil.mayBeString(nullNode));

    Node voidNode = new Node(Token.VOID, Node.newNumber(0));
    assertFalse(NodeUtil.mayBeString(voidNode));
  }

  // Tests getBooleanValue for literal and special name nodes
  @Test
  public void testGetBooleanValue_literalsAndNames_returnsCorrectTernaryValue() {
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.TRUE)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.FALSE)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.NULL)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.VOID, Node.newNumber(0))));

    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newString("foo")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString("")));

    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newNumber(1.0)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newNumber(0.0)));

    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "undefined")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "NaN")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "Infinity")));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "customVar")));
  }

  // Tests getExpressionBooleanValue for logical and ternary operators
  @Test
  public void testGetExpressionBooleanValue_logicalAndHookOps_returnsExpected() {
    Node and = new Node(Token.AND, new Node(Token.TRUE), Node.newNumber(1));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(and));

    Node or = new Node(Token.OR, new Node(Token.FALSE), Node.newString(""));
    assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(or));

    Node not = new Node(Token.NOT, new Node(Token.TRUE));
    assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(not));

    Node hookSame = new Node(Token.HOOK, Node.newString(Token.NAME, "cond"), new Node(Token.TRUE), new Node(Token.TRUE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(hookSame));

    Node hookDiff = new Node(Token.HOOK, Node.newString(Token.NAME, "cond"), new Node(Token.TRUE), new Node(Token.FALSE));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getExpressionBooleanValue(hookDiff));
  }

  // Tests getStringValue conversion
  @Test
  public void testGetStringValue_variousNodeTypes_convertsCorrectly() {
    assertEquals("test", NodeUtil.getStringValue(Node.newString("test")));
    assertEquals("123", NodeUtil.getStringValue(Node.newNumber(123.0)));
    assertEquals("123.5", NodeUtil.getStringValue(Node.newNumber(123.5)));
    assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
    assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID, Node.newNumber(0))));
    assertEquals("Infinity", NodeUtil.getStringValue(Node.newString(Token.NAME, "Infinity")));
    assertEquals("NaN", NodeUtil.getStringValue(Node.newString(Token.NAME, "NaN")));
    assertEquals("[object Object]", NodeUtil.getStringValue(new Node(Token.OBJECTLIT)));
  }

  // Tests getNumberValue conversion
  @Test
  public void testGetNumberValue_primitivesAndNames_convertsCorrectly() {
    assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(new Node(Token.TRUE)));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.FALSE)));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.NULL)));
    assertEquals(Double.valueOf(42.5), NodeUtil.getNumberValue(Node.newNumber(42.5)));
    assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), NodeUtil.getNumberValue(Node.newString(Token.NAME, "Infinity")));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "NaN"))));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "undefined"))));
  }

  // Tests getStringNumberValue with hex, empty and whitespace values
  @Test
  public void testGetStringNumberValue_variousRepresentations_parsesCorrectly() {
    assertEquals(Double.valueOf(0.0), NodeUtil.getStringNumberValue(""));
    assertEquals(Double.valueOf(0.0), NodeUtil.getStringNumberValue("   "));
    assertEquals(Double.valueOf(255.0), NodeUtil.getStringNumberValue("0xFF"));
    assertEquals(Double.valueOf(16.0), NodeUtil.getStringNumberValue("0x10"));
    assertNull(NodeUtil.getStringNumberValue("+0xFF"));
    assertNull(NodeUtil.getStringNumberValue("infinity"));
    assertEquals(Double.valueOf(123.45), NodeUtil.getStringNumberValue("  123.45 \n\t"));
    assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("invalidNumber")));
  }

  // Tests isImmutableValue and isLiteralValue
  @Test
  public void testIsImmutableValue_and_isLiteralValue() {
    Node num = Node.newNumber(10);
    Node str = Node.newString("val");
    Node nullNode = new Node(Token.NULL);
    Node nameUndef = Node.newString(Token.NAME, "undefined");
    Node normalName = Node.newString(Token.NAME, "myVar");

    assertTrue(NodeUtil.isImmutableValue(num));
    assertTrue(NodeUtil.isImmutableValue(str));
    assertTrue(NodeUtil.isImmutableValue(nullNode));
    assertTrue(NodeUtil.isImmutableValue(nameUndef));
    assertFalse(NodeUtil.isImmutableValue(normalName));

    Node arrayLit = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newString("a"));
    assertTrue(NodeUtil.isLiteralValue(arrayLit, false));

    Node arrayWithVar = new Node(Token.ARRAYLIT, normalName);
    assertFalse(NodeUtil.isLiteralValue(arrayWithVar, false));
  }

  // Tests isValidDefineValue with valid and invalid define expressions
  @Test
  public void testIsValidDefineValue_constantsAndExpressions() {
    Set<String> defines = new HashSet<String>();
    defines.add("DEF_A");

    assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(1), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newString("text"), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "DEF_A"), defines));
    assertFalse(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "UNDEFINED_DEF"), defines));

    Node add = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.isValidDefineValue(add, defines));

    Node not = new Node(Token.NOT, new Node(Token.TRUE));
    assertTrue(NodeUtil.isValidDefineValue(not, defines));
  }

  // Tests isEmptyBlock
  @Test
  public void testIsEmptyBlock_emptyAndNonEmptyBlocks() {
    Node emptyBlock = new Node(Token.BLOCK);
    assertTrue(NodeUtil.isEmptyBlock(emptyBlock));

    Node blockWithEmpty = new Node(Token.BLOCK, new Node(Token.EMPTY));
    assertTrue(NodeUtil.isEmptyBlock(blockWithEmpty));

    Node blockWithExpr = new Node(Token.BLOCK, NodeUtil.newExpr(Node.newNumber(1)));
    assertFalse(NodeUtil.isEmptyBlock(blockWithExpr));

    Node nonBlock = new Node(Token.EXPR_RESULT);
    assertFalse(NodeUtil.isEmptyBlock(nonBlock));
  }

  // Tests isSimpleOperator and isSimpleOperatorType
  @Test
  public void testIsSimpleOperator_variousTypes() {
    assertTrue(NodeUtil.isSimpleOperatorType(Token.ADD));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.SUB));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.EQ));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.TYPEOF));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.ASSIGN));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.CALL));

    Node addNode = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.isSimpleOperator(addNode));
  }

  // Tests isAssociative and isCommutative
  @Test
  public void testIsAssociative_and_isCommutative() {
    assertTrue(NodeUtil.isAssociative(Token.MUL));
    assertTrue(NodeUtil.isAssociative(Token.AND));
    assertTrue(NodeUtil.isAssociative(Token.OR));
    assertFalse(NodeUtil.isAssociative(Token.ADD));
    assertFalse(NodeUtil.isAssociative(Token.SUB));

    assertTrue(NodeUtil.isCommutative(Token.MUL));
    assertTrue(NodeUtil.isCommutative(Token.BITOR));
    assertFalse(NodeUtil.isCommutative(Token.ADD));
    assertFalse(NodeUtil.isCommutative(Token.SUB));
  }

  // Tests isAssignmentOp and getOpFromAssignmentOp
  @Test
  public void testAssignmentOps_recognitionAndOpExtraction() {
    Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertTrue(NodeUtil.isAssignmentOp(assign));

    Node assignAdd = new Node(Token.ASSIGN_ADD, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertTrue(NodeUtil.isAssignmentOp(assignAdd));
    assertEquals(Token.ADD, NodeUtil.getOpFromAssignmentOp(assignAdd));

    Node assignSub = new Node(Token.ASSIGN_SUB, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertEquals(Token.SUB, NodeUtil.getOpFromAssignmentOp(assignSub));

    Node add = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    assertFalse(NodeUtil.isAssignmentOp(add));
  }

  // Tests exception path of getOpFromAssignmentOp with non-assignment node
  @Test(expected = IllegalArgumentException.class)
  public void testGetOpFromAssignmentOp_nonAssignOp_throwsException() {
    Node notAssign = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    NodeUtil.getOpFromAssignmentOp(notAssign);
  }

  // Tests opToStr and opToStrNoFail
  @Test
  public void testOpToStr_and_opToStrNoFail() {
    assertEquals("+", NodeUtil.opToStr(Token.ADD));
    assertEquals("==", NodeUtil.opToStr(Token.EQ));
    assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    assertEquals("typeof", NodeUtil.opToStr(Token.TYPEOF));
    assertNull(NodeUtil.opToStr(Token.BLOCK));

    assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));
  }

  // Tests exception path of opToStrNoFail with invalid operator
  @Test(expected = Error.class)
  public void testOpToStrNoFail_invalidOp_throwsError() {
    NodeUtil.opToStrNoFail(Token.BLOCK);
  }

  // Tests precedence lookup
  @Test
  public void testPrecedence_validTypes_returnsCorrectRank() {
    assertEquals(0, NodeUtil.precedence(Token.COMMA));
    assertEquals(1, NodeUtil.precedence(Token.ASSIGN));
    assertEquals(2, NodeUtil.precedence(Token.HOOK));
    assertEquals(3, NodeUtil.precedence(Token.OR));
    assertEquals(4, NodeUtil.precedence(Token.AND));
    assertEquals(8, NodeUtil.precedence(Token.EQ));
    assertEquals(11, NodeUtil.precedence(Token.ADD));
    assertEquals(15, NodeUtil.precedence(Token.NUMBER));
  }

  // Tests isControlStructure and getConditionExpression
  @Test
  public void testControlStructures_and_conditionExpression() {
    Node cond = new Node(Token.TRUE);
    Node body = new Node(Token.BLOCK);
    Node ifNode = new Node(Token.IF, cond, body);

    assertTrue(NodeUtil.isControlStructure(ifNode));
    assertEquals(cond, NodeUtil.getConditionExpression(ifNode));

    Node whileCond = new Node(Token.FALSE);
    Node whileNode = new Node(Token.WHILE, whileCond, new Node(Token.BLOCK));
    assertTrue(NodeUtil.isControlStructure(whileNode));
    assertEquals(whileCond, NodeUtil.getConditionExpression(whileNode));

    Node block = new Node(Token.BLOCK);
    assertFalse(NodeUtil.isControlStructure(block));
  }

  // Tests evaluatesToLocalValue for literals, immutable values and compound expressions
  @Test
  public void testEvaluatesToLocalValue_literalsAndExpressions() {
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newNumber(1)));
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newString("str")));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ARRAYLIT)));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.OBJECTLIT)));

    Node add = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.evaluatesToLocalValue(add));

    Node hook = new Node(Token.HOOK, new Node(Token.TRUE), Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.evaluatesToLocalValue(hook));

    Node comma = new Node(Token.COMMA, Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.evaluatesToLocalValue(comma));
  }

  // Tests helper utility methods: isLatin, isValidPropertyName, newUndefinedNode, newVarNode
  @Test
  public void testUtilityHelperMethods() {
    assertTrue(NodeUtil.isLatin("asciiOnly"));
    assertFalse(NodeUtil.isLatin("ภาษาไทย"));

    assertTrue(NodeUtil.isValidPropertyName("validProp"));
    assertFalse(NodeUtil.isValidPropertyName("default"));
    assertFalse(NodeUtil.isValidPropertyName("123invalid"));

    Node undef = NodeUtil.newUndefinedNode(null);
    assertEquals(Token.VOID, undef.getType());

    Node varNode = NodeUtil.newVarNode("x", Node.newNumber(42));
    assertEquals(Token.VAR, varNode.getType());
    assertEquals("x", varNode.getFirstChild().getString());
    assertEquals(42.0, varNode.getFirstChild().getFirstChild().getDouble(), 0.0);
  }
}