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

  // Tests isValidDefineValue for valid literal types
  @Test
  public void testIsValidDefineValue_literals_returnsTrue() {
    Set<String> defines = Collections.emptySet();
    assertTrue(NodeUtil.isValidDefineValue(Node.newString("hello"), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(42), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.FALSE), defines));
  }

  // Tests isValidDefineValue for unary operators
  @Test
  public void testIsValidDefineValue_unaryOperators_validatesChild() {
    Set<String> defines = Collections.emptySet();
    Node validNot = new Node(Token.NOT, new Node(Token.TRUE));
    Node validNeg = new Node(Token.NEG, Node.newNumber(5));
    Node invalidNot = new Node(Token.NOT, new Node(Token.NULL));

    assertTrue(NodeUtil.isValidDefineValue(validNot, defines));
    assertTrue(NodeUtil.isValidDefineValue(validNeg, defines));
    assertFalse(NodeUtil.isValidDefineValue(invalidNot, defines));
  }

  // Tests isValidDefineValue for binary bitwise operators with valid children
  @Test
  public void testIsValidDefineValue_binaryBitwiseBothValid_returnsTrue() {
    Set<String> defines = Collections.emptySet();
    Node validBitAnd = new Node(Token.BITAND, Node.newNumber(1), Node.newNumber(2));
    Node validBitOr = new Node(Token.BITOR, Node.newNumber(1), Node.newNumber(2));
    Node validBitXor = new Node(Token.BITXOR, Node.newNumber(1), Node.newNumber(2));

    assertTrue(NodeUtil.isValidDefineValue(validBitAnd, defines));
    assertTrue(NodeUtil.isValidDefineValue(validBitOr, defines));
    assertTrue(NodeUtil.isValidDefineValue(validBitXor, defines));
  }

  // Tests isValidDefineValue when second child of binary operator is invalid
  @Test
  public void testIsValidDefineValue_binaryBitwiseSecondChildInvalid_returnsFalse() {
    Set<String> defines = Collections.emptySet();
    Node invalidBitAnd = new Node(Token.BITAND, Node.newNumber(1), Node.newString(Token.NAME, "UNKNOWN"));
    Node invalidBitOr = new Node(Token.BITOR, Node.newNumber(1), new Node(Token.NULL));
    Node invalidBitXor = new Node(Token.BITXOR, Node.newNumber(1), Node.newString(Token.NAME, "UNKNOWN"));

    assertFalse(NodeUtil.isValidDefineValue(invalidBitAnd, defines));
    assertFalse(NodeUtil.isValidDefineValue(invalidBitOr, defines));
    assertFalse(NodeUtil.isValidDefineValue(invalidBitXor, defines));
  }

  // Tests isValidDefineValue with defined and undefined names / getprops
  @Test
  public void testIsValidDefineValue_nameAndGetProp_checksDefinesSet() {
    Set<String> defines = new HashSet<String>();
    defines.add("DEF_NAME");
    defines.add("a.b.c");

    Node defName = Node.newString(Token.NAME, "DEF_NAME");
    Node undefName = Node.newString(Token.NAME, "OTHER_NAME");
    Node defProp = NodeUtil.newQualifiedNameNode("a.b.c", -1, -1);
    Node undefProp = NodeUtil.newQualifiedNameNode("x.y.z", -1, -1);

    assertTrue(NodeUtil.isValidDefineValue(defName, defines));
    assertFalse(NodeUtil.isValidDefineValue(undefName, defines));
    assertTrue(NodeUtil.isValidDefineValue(defProp, defines));
    assertFalse(NodeUtil.isValidDefineValue(undefProp, defines));
  }

  // Tests isValidDefineValue for unsupported node types
  @Test
  public void testIsValidDefineValue_unsupportedNodes_returnsFalse() {
    Set<String> defines = Collections.emptySet();
    assertFalse(NodeUtil.isValidDefineValue(new Node(Token.NULL), defines));
    assertFalse(NodeUtil.isValidDefineValue(new Node(Token.ARRAYLIT), defines));
    assertFalse(NodeUtil.isValidDefineValue(new Node(Token.OBJECTLIT), defines));
  }

  // Tests getBooleanValue for various literals
  @Test
  public void testGetBooleanValue_literals_returnsExpectedTernaryValue() {
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newString("non-empty")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString("")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newNumber(1.0)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newNumber(0.0)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.TRUE)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.FALSE)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.NULL)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.VOID)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "undefined")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "NaN")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "Infinity")));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "otherVar")));
  }

  // Tests getExpressionBooleanValue for logical operations
  @Test
  public void testGetExpressionBooleanValue_logicalOperators_evaluatesCorrectly() {
    Node notNode = new Node(Token.NOT, new Node(Token.TRUE));
    assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(notNode));

    Node andNode = new Node(Token.AND, new Node(Token.TRUE), new Node(Token.FALSE));
    assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(andNode));

    Node orNode = new Node(Token.OR, new Node(Token.TRUE), new Node(Token.FALSE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(orNode));

    Node hookSame = new Node(Token.HOOK, new Node(Token.TRUE), new Node(Token.TRUE), new Node(Token.TRUE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(hookSame));

    Node hookDiff = new Node(Token.HOOK, new Node(Token.TRUE), new Node(Token.TRUE), new Node(Token.FALSE));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getExpressionBooleanValue(hookDiff));
  }

  // Tests getStringValue for various nodes
  @Test
  public void testGetStringValue_variousNodes_returnsExpectedString() {
    assertEquals("test", NodeUtil.getStringValue(Node.newString("test")));
    assertEquals("foo", NodeUtil.getStringValue(Node.newString(Token.NAME, "foo")));
    assertEquals("42", NodeUtil.getStringValue(Node.newNumber(42.0)));
    assertEquals("42.5", NodeUtil.getStringValue(Node.newNumber(42.5)));
    assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
    assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID)));
    assertNull(NodeUtil.getStringValue(new Node(Token.ARRAYLIT)));
  }

  // Tests isImmutableValue for literals and special names
  @Test
  public void testIsImmutableValue_variousNodes_identifiesCorrectly() {
    assertTrue(NodeUtil.isImmutableValue(Node.newString("str")));
    assertTrue(NodeUtil.isImmutableValue(Node.newNumber(1)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "undefined")));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "Infinity")));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "NaN")));
    assertFalse(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "customVar")));
  }

  // Tests isLiteralValue with and without functions
  @Test
  public void testIsLiteralValue_arrayAndFunction_handlesIncludeFunctionsFlag() {
    Node arrayLit = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newString("a"));
    assertTrue(NodeUtil.isLiteralValue(arrayLit, false));

    Node fnExpr = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    Node parentExpr = new Node(Token.EXPR_RESULT, fnExpr);
    assertFalse(NodeUtil.isLiteralValue(fnExpr, false));
    assertTrue(NodeUtil.isLiteralValue(fnExpr, true));
  }

  // Tests isEmptyBlock
  @Test
  public void testIsEmptyBlock_emptyAndNonEmptyBlocks_identifiesCorrectly() {
    Node emptyBlock = new Node(Token.BLOCK);
    assertTrue(NodeUtil.isEmptyBlock(emptyBlock));

    Node blockWithEmpty = new Node(Token.BLOCK, new Node(Token.EMPTY));
    assertTrue(NodeUtil.isEmptyBlock(blockWithEmpty));

    Node blockWithStatement = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1)));
    assertFalse(NodeUtil.isEmptyBlock(blockWithStatement));

    Node notBlock = new Node(Token.EXPR_RESULT);
    assertFalse(NodeUtil.isEmptyBlock(notBlock));
  }

  // Tests isSimpleOperator and isSimpleOperatorType
  @Test
  public void testIsSimpleOperatorType_operatorTypes_identifiesCorrectly() {
    assertTrue(NodeUtil.isSimpleOperatorType(Token.ADD));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.SUB));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.MUL));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.BITAND));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.HOOK));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.CALL));
  }

  // Tests isLatin
  @Test
  public void testIsLatin_asciiAndNonAscii_returnsExpected() {
    assertTrue(NodeUtil.isLatin("helloWorld_123$"));
    assertTrue(NodeUtil.isLatin(""));
    assertFalse(NodeUtil.isLatin("hello\u0100world"));
  }

  // Tests isValidPropertyName
  @Test
  public void testIsValidPropertyName_validAndInvalidNames_identifiesCorrectly() {
    assertTrue(NodeUtil.isValidPropertyName("validPropName"));
    assertTrue(NodeUtil.isValidPropertyName("$foo_123"));
    assertFalse(NodeUtil.isValidPropertyName("class")); // keyword
    assertFalse(NodeUtil.isValidPropertyName("123abc")); // not identifier
    assertFalse(NodeUtil.isValidPropertyName("prop\u00e9")); // non-latin
  }

  // Tests precedence
  @Test
  public void testPrecedence_operators_returnsCorrectPrecedence() {
    assertEquals(0, NodeUtil.precedence(Token.COMMA));
    assertEquals(1, NodeUtil.precedence(Token.ASSIGN));
    assertEquals(2, NodeUtil.precedence(Token.HOOK));
    assertEquals(3, NodeUtil.precedence(Token.OR));
    assertEquals(4, NodeUtil.precedence(Token.AND));
    assertEquals(11, NodeUtil.precedence(Token.ADD));
    assertEquals(15, NodeUtil.precedence(Token.NAME));
  }

  // Tests precedence for unknown token throwing Error
  @Test(expected = Error.class)
  public void testPrecedence_unknownToken_throwsError() {
    NodeUtil.precedence(-999);
  }

  // Tests opToStr and opToStrNoFail
  @Test
  public void testOpToStr_validAndInvalid_returnsStringOrThrows() {
    assertEquals("+", NodeUtil.opToStr(Token.ADD));
    assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    assertEquals("&&", NodeUtil.opToStr(Token.AND));
    assertNull(NodeUtil.opToStr(Token.FUNCTION));
    assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));
  }

  // Tests opToStrNoFail exception path
  @Test(expected = Error.class)
  public void testOpToStrNoFail_invalidOp_throwsError() {
    NodeUtil.opToStrNoFail(Token.FUNCTION);
  }

  // Tests newQualifiedNameNode
  @Test
  public void testNewQualifiedNameNode_singleAndMultiPart_createsProperTree() {
    Node simple = NodeUtil.newQualifiedNameNode("foo", 1, 2);
    assertEquals(Token.NAME, simple.getType());
    assertEquals("foo", simple.getString());

    Node qualified = NodeUtil.newQualifiedNameNode("a.b.c", 1, 2);
    assertEquals(Token.GETPROP, qualified.getType());
    assertEquals("a.b.c", qualified.getQualifiedName());
    assertEquals("a", NodeUtil.getRootOfQualifiedName(qualified).getString());
  }

  // Tests removeChild for statement block
  @Test
  public void testRemoveChild_statementInBlock_removesSuccessfully() {
    Node block = new Node(Token.BLOCK);
    Node expr1 = new Node(Token.EXPR_RESULT, Node.newNumber(1));
    Node expr2 = new Node(Token.EXPR_RESULT, Node.newNumber(2));
    block.addChildToBack(expr1);
    block.addChildToBack(expr2);

    NodeUtil.removeChild(block, expr1);
    assertEquals(1, block.getChildCount());
    assertSame(expr2, block.getFirstChild());
  }

  // Tests evaluatesToLocalValue
  @Test
  public void testEvaluatesToLocalValue_literalsAndImmutable_returnsTrue() {
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newNumber(123)));
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newString("str")));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ARRAYLIT)));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.OBJECTLIT)));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.NEW, Node.newString(Token.NAME, "Object"))));
  }
}