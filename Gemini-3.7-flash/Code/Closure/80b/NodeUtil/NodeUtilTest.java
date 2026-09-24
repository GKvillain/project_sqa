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

  // Tests boolean result for DELPROP operator and boolean literals/comparisons
  @Test
  public void testIsBooleanResult_variousNodes_returnsExpected() {
    Node trueNode = new Node(Token.TRUE);
    Node falseNode = new Node(Token.FALSE);
    Node eqNode = new Node(Token.EQ, Node.newNumber(1), Node.newNumber(2));
    Node notNode = new Node(Token.NOT, Node.newNumber(1));
    Node delpropNode = new Node(Token.DELPROP, Node.newString(Token.NAME, "a"));
    Node numberNode = Node.newNumber(5);

    assertTrue(NodeUtil.isBooleanResult(trueNode));
    assertTrue(NodeUtil.isBooleanResult(falseNode));
    assertTrue(NodeUtil.isBooleanResult(eqNode));
    assertTrue(NodeUtil.isBooleanResult(notNode));
    assertTrue(NodeUtil.isBooleanResult(delpropNode));
    assertFalse(NodeUtil.isBooleanResult(numberNode));
  }

  // Tests numeric result helper for numbers, unary and binary arithmetic
  @Test
  public void testIsNumericResult_numericNodes_returnsTrue() {
    Node numberNode = Node.newNumber(42);
    Node subNode = new Node(Token.SUB, Node.newNumber(1), Node.newNumber(2));
    Node bitwiseNode = new Node(Token.BITAND, Node.newNumber(1), Node.newNumber(2));
    Node nanNode = Node.newString(Token.NAME, "NaN");
    Node infinityNode = Node.newString(Token.NAME, "Infinity");
    Node stringNode = Node.newString("hello");

    assertTrue(NodeUtil.isNumericResult(numberNode));
    assertTrue(NodeUtil.isNumericResult(subNode));
    assertTrue(NodeUtil.isNumericResult(bitwiseNode));
    assertTrue(NodeUtil.isNumericResult(nanNode));
    assertTrue(NodeUtil.isNumericResult(infinityNode));
    assertFalse(NodeUtil.isNumericResult(stringNode));
  }

  // Tests getStringValue for different AST nodes
  @Test
  public void testGetStringValue_variousNodes_returnsCorrectString() {
    assertEquals("hello", NodeUtil.getStringValue(Node.newString("hello")));
    assertEquals("123", NodeUtil.getStringValue(Node.newNumber(123.0)));
    assertEquals("123.45", NodeUtil.getStringValue(Node.newNumber(123.45)));
    assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
    assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID, Node.newNumber(0))));
    assertEquals("[object Object]", NodeUtil.getStringValue(new Node(Token.OBJECTLIT)));
    assertEquals("NaN", NodeUtil.getStringValue(Node.newString(Token.NAME, "NaN")));
  }

  // Tests getNumberValue for various literal and name nodes
  @Test
  public void testGetNumberValue_variousNodes_returnsCorrectDouble() {
    assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(new Node(Token.TRUE)));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.FALSE)));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.NULL)));
    assertEquals(Double.valueOf(3.14), NodeUtil.getNumberValue(Node.newNumber(3.14)));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "NaN"))));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "undefined"))));
    assertEquals(Double.valueOf(Double.POSITIVE_INFINITY),
        NodeUtil.getNumberValue(Node.newString(Token.NAME, "Infinity")));
    assertEquals(Double.valueOf(0.0),
        NodeUtil.getNumberValue(Node.newString("")));
    assertEquals(Double.valueOf(255.0),
        NodeUtil.getNumberValue(Node.newString("0xff")));
  }

  // Tests getBooleanValue for literal nodes
  @Test
  public void testGetBooleanValue_literals_returnsExpectedTernaryValue() {
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newString("non-empty")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString("")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newNumber(1.0)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newNumber(0.0)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.NULL)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.FALSE)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.TRUE)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "undefined")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "NaN")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "Infinity")));
  }

  // Tests getExpressionBooleanValue for logical operations and hooks
  @Test
  public void testGetExpressionBooleanValue_logicalExpressions_returnsEvaluatedValue() {
    Node andNode = new Node(Token.AND, new Node(Token.TRUE), Node.newNumber(1));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(andNode));

    Node orNode = new Node(Token.OR, new Node(Token.FALSE), new Node(Token.NULL));
    assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(orNode));

    Node notNode = new Node(Token.NOT, new Node(Token.TRUE));
    assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(notNode));

    Node hookNode = new Node(Token.HOOK,
        Node.newString(Token.NAME, "cond"),
        new Node(Token.TRUE),
        new Node(Token.TRUE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(hookNode));
  }

  // Tests isImmutableValue on immutable primitives and mutable constructs
  @Test
  public void testIsImmutableValue_primitivesAndNames_returnsCorrectBoolean() {
    assertTrue(NodeUtil.isImmutableValue(Node.newString("abc")));
    assertTrue(NodeUtil.isImmutableValue(Node.newNumber(10)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "undefined")));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NOT, new Node(Token.TRUE))));
    assertFalse(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "x")));
    assertFalse(NodeUtil.isImmutableValue(new Node(Token.ARRAYLIT)));
  }

  // Tests isLiteralValue for collections and nested literals
  @Test
  public void testIsLiteralValue_arrayAndObjectLiterals_returnsExpected() {
    Node arrayLit = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newString("a"));
    assertTrue(NodeUtil.isLiteralValue(arrayLit, false));

    Node arrayWithVar = new Node(Token.ARRAYLIT, Node.newString(Token.NAME, "variable"));
    assertFalse(NodeUtil.isLiteralValue(arrayWithVar, false));

    Node emptyObj = new Node(Token.OBJECTLIT);
    assertTrue(NodeUtil.isLiteralValue(emptyObj, false));
  }

  // Tests isValidDefineValue with defined constant names and operators
  @Test
  public void testIsValidDefineValue_constantsAndExpressions_returnsExpected() {
    Set<String> defines = new HashSet<String>();
    defines.add("DEF_A");

    assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(10), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newString("str"), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "DEF_A"), defines));
    assertFalse(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "UNKNOWN"), defines));

    Node addNode = new Node(Token.ADD, Node.newNumber(1), Node.newString(Token.NAME, "DEF_A"));
    assertTrue(NodeUtil.isValidDefineValue(addNode, defines));
  }

  // Tests isEmptyBlock on empty and non-empty block nodes
  @Test
  public void testIsEmptyBlock_blocks_returnsExpected() {
    Node emptyBlock = new Node(Token.BLOCK);
    assertTrue(NodeUtil.isEmptyBlock(emptyBlock));

    Node blockWithEmpty = new Node(Token.BLOCK, new Node(Token.EMPTY));
    assertTrue(NodeUtil.isEmptyBlock(blockWithEmpty));

    Node blockWithExpr = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1)));
    assertFalse(NodeUtil.isEmptyBlock(blockWithExpr));

    Node nonBlock = Node.newNumber(1);
    assertFalse(NodeUtil.isEmptyBlock(nonBlock));
  }

  // Tests isSimpleOperator and isSimpleOperatorType
  @Test
  public void testIsSimpleOperator_variousOperators_returnsExpected() {
    assertTrue(NodeUtil.isSimpleOperator(new Node(Token.ADD)));
    assertTrue(NodeUtil.isSimpleOperator(new Node(Token.SUB)));
    assertTrue(NodeUtil.isSimpleOperator(new Node(Token.TYPEOF)));
    assertTrue(NodeUtil.isSimpleOperator(new Node(Token.VOID)));
    assertFalse(NodeUtil.isSimpleOperator(new Node(Token.ASSIGN)));
    assertFalse(NodeUtil.isSimpleOperator(new Node(Token.CALL)));
  }

  // Tests opToStr operator conversion
  @Test
  public void testOpToStr_validAndInvalidOps_returnsCorrectRepresentation() {
    assertEquals("+", NodeUtil.opToStr(Token.ADD));
    assertEquals("-", NodeUtil.opToStr(Token.SUB));
    assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    assertEquals("!==", NodeUtil.opToStr(Token.SHNE));
    assertEquals("instanceof", NodeUtil.opToStr(Token.INSTANCEOF));
    assertNull(NodeUtil.opToStr(Token.CALL));
  }

  // Tests opToStrNoFail exception path
  @Test(expected = Error.class)
  public void testOpToStrNoFail_unknownOp_throwsError() {
    NodeUtil.opToStrNoFail(Token.CALL);
  }

  // Tests isAssociative and isCommutative
  @Test
  public void testIsAssociativeAndCommutative_operatorTokens_returnsExpected() {
    assertTrue(NodeUtil.isAssociative(Token.MUL));
    assertTrue(NodeUtil.isAssociative(Token.AND));
    assertTrue(NodeUtil.isAssociative(Token.BITOR));
    assertFalse(NodeUtil.isAssociative(Token.ADD));
    assertFalse(NodeUtil.isAssociative(Token.SUB));

    assertTrue(NodeUtil.isCommutative(Token.MUL));
    assertTrue(NodeUtil.isCommutative(Token.BITOR));
    assertFalse(NodeUtil.isCommutative(Token.ADD));
    assertFalse(NodeUtil.isCommutative(Token.DIV));
  }

  // Tests isLatin string validation
  @Test
  public void testIsLatin_asciiAndUnicode_returnsExpected() {
    assertTrue(NodeUtil.isLatin("asciiOnly123"));
    assertTrue(NodeUtil.isLatin(""));
    assertFalse(NodeUtil.isLatin("unicode\u0100"));
    assertFalse(NodeUtil.isLatin("สวัสดี"));
  }

  // Tests isValidPropertyName
  @Test
  public void testIsValidPropertyName_validAndInvalidNames_returnsExpected() {
    assertTrue(NodeUtil.isValidPropertyName("validProp"));
    assertTrue(NodeUtil.isValidPropertyName("$foo"));
    assertTrue(NodeUtil.isValidPropertyName("_bar_123"));
    assertFalse(NodeUtil.isValidPropertyName("class"));
    assertFalse(NodeUtil.isValidPropertyName("function"));
    assertFalse(NodeUtil.isValidPropertyName("123prop"));
    assertFalse(NodeUtil.isValidPropertyName("prop\u0100"));
  }

  // Tests precedence method
  @Test
  public void testPrecedence_variousTypes_returnsExpectedValue() {
    assertEquals(0, NodeUtil.precedence(Token.COMMA));
    assertEquals(1, NodeUtil.precedence(Token.ASSIGN));
    assertEquals(2, NodeUtil.precedence(Token.HOOK));
    assertEquals(3, NodeUtil.precedence(Token.OR));
    assertEquals(4, NodeUtil.precedence(Token.AND));
    assertEquals(8, NodeUtil.precedence(Token.EQ));
    assertEquals(11, NodeUtil.precedence(Token.ADD));
    assertEquals(12, NodeUtil.precedence(Token.MUL));
    assertEquals(13, NodeUtil.precedence(Token.NOT));
    assertEquals(15, NodeUtil.precedence(Token.NUMBER));
  }

  // Tests precedence exception on unknown token
  @Test(expected = Error.class)
  public void testPrecedence_unknownToken_throwsError() {
    NodeUtil.precedence(-999);
  }

  // Tests evaluatesToLocalValue
  @Test
  public void testEvaluatesToLocalValue_literalsAndOps_returnsExpected() {
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newNumber(1)));
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newString("str")));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ARRAYLIT)));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.OBJECTLIT)));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2))));
    assertFalse(NodeUtil.evaluatesToLocalValue(Node.newString(Token.NAME, "externalVar")));
  }

  // Tests getFunctionName and getNearestFunctionName
  @Test
  public void testGetFunctionName_namedAndAnonymousFunctions_returnsExpected() {
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "myFunc"), new Node(Token.LP), new Node(Token.BLOCK));
    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "v"));
    varNode.getFirstChild().addChildToBack(fn);

    assertEquals("v", NodeUtil.getFunctionName(fn));
    assertEquals("v", NodeUtil.getNearestFunctionName(fn));

    Node plainFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "plain"), new Node(Token.LP), new Node(Token.BLOCK));
    Node script = new Node(Token.SCRIPT, plainFn);
    assertEquals("plain", NodeUtil.getFunctionName(plainFn));
  }
}