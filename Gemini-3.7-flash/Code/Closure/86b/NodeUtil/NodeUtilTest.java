package com.google.javascript.jscomp;

import com.google.common.collect.Sets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.TernaryValue;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.*;

public class NodeUtilTest {

  // Tests boolean value calculation for literal tokens
  @Test
  public void testGetBooleanValue_literals_returnsExpectedTernaryValue() {
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.TRUE)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.FALSE)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.NULL)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.VOID, Node.newNumber(0))));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newString("hello")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString("")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newNumber(1.0)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newNumber(0.0)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "undefined")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "NaN")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "Infinity")));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "other")));
  }

  // Tests boolean evaluation for expressions (NOT, AND, OR, HOOK)
  @Test
  public void testGetExpressionBooleanValue_logicalExpressions_returnsCorrectValue() {
    Node notNode = new Node(Token.NOT, new Node(Token.TRUE));
    assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(notNode));

    Node andNode = new Node(Token.AND, new Node(Token.TRUE), new Node(Token.FALSE));
    assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(andNode));

    Node orNode = new Node(Token.OR, new Node(Token.TRUE), new Node(Token.FALSE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(orNode));

    Node hookNodeSame = new Node(Token.HOOK, new Node(Token.TRUE), new Node(Token.TRUE), new Node(Token.TRUE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(hookNodeSame));

    Node hookNodeDiff = new Node(Token.HOOK, new Node(Token.TRUE), new Node(Token.TRUE), new Node(Token.FALSE));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getExpressionBooleanValue(hookNodeDiff));
  }

  // Tests string value conversion for various node types
  @Test
  public void testGetStringValue_variousNodes_returnsExpectedString() {
    assertEquals("foo", NodeUtil.getStringValue(Node.newString("foo")));
    assertEquals("undefined", NodeUtil.getStringValue(Node.newString(Token.NAME, "undefined")));
    assertEquals("NaN", NodeUtil.getStringValue(Node.newString(Token.NAME, "NaN")));
    assertEquals("Infinity", NodeUtil.getStringValue(Node.newString(Token.NAME, "Infinity")));
    assertEquals("100", NodeUtil.getStringValue(Node.newNumber(100.0)));
    assertEquals("100.5", NodeUtil.getStringValue(Node.newNumber(100.5)));
    assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
    assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID, Node.newNumber(0))));
    assertNull(NodeUtil.getStringValue(new Node(Token.ARRAYLIT)));
  }

  // Tests number value conversion for literal and identifier nodes
  @Test
  public void testGetNumberValue_variousNodes_returnsExpectedDouble() {
    assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(new Node(Token.TRUE)));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.FALSE)));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.NULL)));
    assertEquals(Double.valueOf(42.5), NodeUtil.getNumberValue(Node.newNumber(42.5)));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(new Node(Token.VOID))));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "undefined"))));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "NaN"))));
    assertEquals(Double.valueOf(Double.POSITIVE_INFINITY),
        NodeUtil.getNumberValue(Node.newString(Token.NAME, "Infinity")));
    assertNull(NodeUtil.getNumberValue(Node.newString(Token.NAME, "customVar")));
  }

  // Tests extracting function name from function declarations and expressions
  @Test
  public void testGetFunctionName_variousParentStructures_returnsCorrectName() {
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "funcName"), new Node(Token.LP), new Node(Token.BLOCK));
    Node script = new Node(Token.SCRIPT, fn);
    assertEquals("funcName", NodeUtil.getFunctionName(fn));

    Node anonFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "varName"));
    varNode.getFirstChild().addChildToBack(anonFn);
    assertEquals("varName", NodeUtil.getFunctionName(anonFn));

    Node getProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "obj"), Node.newString(Token.STRING, "method"));
    Node assignNode = new Node(Token.ASSIGN, getProp, anonFn);
    assertEquals("obj.method", NodeUtil.getFunctionName(anonFn));
  }

  // Tests immutable value identification
  @Test
  public void testIsImmutableValue_validAndInvalidNodes_returnsExpectedBoolean() {
    assertTrue(NodeUtil.isImmutableValue(Node.newString("str")));
    assertTrue(NodeUtil.isImmutableValue(Node.newNumber(123)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "undefined")));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "Infinity")));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "NaN")));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NEG, Node.newNumber(5))));

    assertFalse(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "x")));
    assertFalse(NodeUtil.isImmutableValue(new Node(Token.OBJECTLIT)));
  }

  // Tests literal value identification for array and object literals
  @Test
  public void testIsLiteralValue_compositeLiterals_evaluatesCorrectly() {
    Node arrayLit = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newString("a"));
    assertTrue(NodeUtil.isLiteralValue(arrayLit, false));

    Node nonConstArray = new Node(Token.ARRAYLIT, Node.newString(Token.NAME, "x"));
    assertFalse(NodeUtil.isLiteralValue(nonConstArray, false));

    Node objLit = new Node(Token.OBJECTLIT);
    Node key = Node.newString(Token.STRING, "k");
    key.addChildToBack(Node.newNumber(1));
    objLit.addChildToBack(key);
    assertTrue(NodeUtil.isLiteralValue(objLit, false));
  }

  // Tests define value validity
  @Test
  public void testIsValidDefineValue_operatorsAndOperands_returnsExpected() {
    Set<String> defines = Sets.newHashSet("DEF_A", "DEF_B");
    assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(1), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newString("val"), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "DEF_A"), defines));
    assertFalse(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "OTHER"), defines));

    Node add = new Node(Token.ADD, Node.newNumber(1), Node.newString(Token.NAME, "DEF_B"));
    assertTrue(NodeUtil.isValidDefineValue(add, defines));

    Node not = new Node(Token.NOT, new Node(Token.TRUE));
    assertTrue(NodeUtil.isValidDefineValue(not, defines));
  }

  // Tests simple operator classification
  @Test
  public void testIsSimpleOperatorType_variousTokens_identifiesCorrectly() {
    assertTrue(NodeUtil.isSimpleOperatorType(Token.ADD));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.SUB));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.MUL));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.DIV));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.EQ));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.TYPEOF));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.ASSIGN));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.HOOK));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.CALL));
  }

  // Tests side effect detection for constructor calls
  @Test
  public void testConstructorCallHasSideEffects_builtinsAndCustom_evaluated() {
    Node builtinNew = new Node(Token.NEW, Node.newString(Token.NAME, "Array"));
    assertFalse(NodeUtil.constructorCallHasSideEffects(builtinNew));

    Node customNew = new Node(Token.NEW, Node.newString(Token.NAME, "CustomClass"));
    assertTrue(NodeUtil.constructorCallHasSideEffects(customNew));
  }

  // Tests side effect detection for constructor calls throws exception on invalid node
  @Test(expected = IllegalStateException.class)
  public void testConstructorCallHasSideEffects_nonNewNode_throwsException() {
    Node callNode = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    NodeUtil.constructorCallHasSideEffects(callNode);
  }

  // Tests side effect detection for function calls
  @Test
  public void testFunctionCallHasSideEffects_builtinsAndMath_evaluated() {
    Node builtinCall = new Node(Token.CALL, Node.newString(Token.NAME, "String"));
    assertFalse(NodeUtil.functionCallHasSideEffects(builtinCall));

    Node mathCall = new Node(Token.CALL,
        new Node(Token.GETPROP, Node.newString(Token.NAME, "Math"), Node.newString(Token.STRING, "sin")));
    assertFalse(NodeUtil.functionCallHasSideEffects(mathCall));

    Node customCall = new Node(Token.CALL, Node.newString(Token.NAME, "myFunc"));
    assertTrue(NodeUtil.functionCallHasSideEffects(customCall));
  }

  // Tests evaluatesToLocalValue for literals, expressions, assignments, and new objects
  @Test
  public void testEvaluatesToLocalValue_variousNodes_returnsExpectedLocality() {
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newNumber(1)));
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newString("test")));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ARRAYLIT)));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.OBJECTLIT)));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2))));

    Node newExpr = new Node(Token.NEW, Node.newString(Token.NAME, "Object"));
    assertTrue(NodeUtil.evaluatesToLocalValue(newExpr));

    Node nameNode = Node.newString(Token.NAME, "globalVar");
    assertFalse(NodeUtil.evaluatesToLocalValue(nameNode));
  }

  // Tests operator string conversions
  @Test
  public void testOpToStr_validAndInvalidOperators_returnsStringOrNull() {
    assertEquals("+", NodeUtil.opToStr(Token.ADD));
    assertEquals("-", NodeUtil.opToStr(Token.SUB));
    assertEquals("==", NodeUtil.opToStr(Token.EQ));
    assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    assertEquals("!", NodeUtil.opToStr(Token.NOT));
    assertEquals("typeof", NodeUtil.opToStr(Token.TYPEOF));
    assertEquals("instanceof", NodeUtil.opToStr(Token.INSTANCEOF));
    assertNull(NodeUtil.opToStr(Token.FUNCTION));
  }

  // Tests operator string conversion throws error on unknown operator
  @Test(expected = Error.class)
  public void testOpToStrNoFail_invalidOperator_throwsError() {
    NodeUtil.opToStrNoFail(Token.FUNCTION);
  }

  // Tests precedence determination for various token types
  @Test
  public void testPrecedence_variousTokens_returnsCorrectPrecedenceOrder() {
    assertTrue(NodeUtil.precedence(Token.COMMA) < NodeUtil.precedence(Token.ASSIGN));
    assertTrue(NodeUtil.precedence(Token.ASSIGN) < NodeUtil.precedence(Token.HOOK));
    assertTrue(NodeUtil.precedence(Token.HOOK) < NodeUtil.precedence(Token.OR));
    assertTrue(NodeUtil.precedence(Token.OR) < NodeUtil.precedence(Token.AND));
    assertTrue(NodeUtil.precedence(Token.ADD) < NodeUtil.precedence(Token.MUL));
    assertTrue(NodeUtil.precedence(Token.MUL) < NodeUtil.precedence(Token.NOT));
  }

  // Tests associativity and commutativity of operators
  @Test
  public void testIsAssociativeAndCommutative_variousOperators_returnsExpected() {
    assertTrue(NodeUtil.isAssociative(Token.MUL));
    assertTrue(NodeUtil.isAssociative(Token.AND));
    assertTrue(NodeUtil.isAssociative(Token.OR));
    assertFalse(NodeUtil.isAssociative(Token.ADD));
    assertFalse(NodeUtil.isAssociative(Token.SUB));

    assertTrue(NodeUtil.isCommutative(Token.MUL));
    assertTrue(NodeUtil.isCommutative(Token.BITOR));
    assertTrue(NodeUtil.isCommutative(Token.BITAND));
    assertFalse(NodeUtil.isCommutative(Token.ADD));
    assertFalse(NodeUtil.isCommutative(Token.DIV));
  }

  // Tests property name validation and ASCII checks
  @Test
  public void testIsValidPropertyNameAndIsLatin_variousStrings_validated() {
    assertTrue(NodeUtil.isLatin("asciiOnly"));
    assertFalse(NodeUtil.isLatin("nonAscii\u00e9"));

    assertTrue(NodeUtil.isValidPropertyName("propName"));
    assertTrue(NodeUtil.isValidPropertyName("_valid123"));
    assertFalse(NodeUtil.isValidPropertyName("class"));
    assertFalse(NodeUtil.isValidPropertyName("123invalid"));
    assertFalse(NodeUtil.isValidPropertyName("prop\u00e9"));
  }

  // Tests node removal from block and var statements
  @Test
  public void testRemoveChild_fromBlockAndVar_removesProperly() {
    Node block = new Node(Token.BLOCK);
    Node expr = NodeUtil.newExpr(Node.newNumber(1));
    block.addChildToBack(expr);
    assertEquals(1, block.getChildCount());
    NodeUtil.removeChild(block, expr);
    assertEquals(0, block.getChildCount());

    Node script = new Node(Token.SCRIPT);
    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "a"));
    script.addChildToBack(varNode);
    NodeUtil.removeChild(varNode, varNode.getFirstChild());
    assertEquals(0, script.getChildCount());
  }

  // Tests node removal failure path on invalid parent-child relation
  @Test(expected = IllegalStateException.class)
  public void testRemoveChild_invalidStructure_throwsException() {
    Node add = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    NodeUtil.removeChild(add, add.getFirstChild());
  }

  // Tests loop structure inspection and block extraction
  @Test
  public void testLoopStructureAndBlocks_variousLoops_identifiedCorrectly() {
    Node forBody = new Node(Token.BLOCK);
    Node forNode = new Node(Token.FOR, Node.newString(Token.NAME, "i"), Node.newNumber(0), Node.newNumber(10), forBody);
    assertTrue(NodeUtil.isLoopStructure(forNode));
    assertEquals(forBody, NodeUtil.getLoopCodeBlock(forNode));

    Node doBody = new Node(Token.BLOCK);
    Node doNode = new Node(Token.DO, doBody, new Node(Token.TRUE));
    assertTrue(NodeUtil.isLoopStructure(doNode));
    assertEquals(doBody, NodeUtil.getLoopCodeBlock(doNode));

    Node ifNode = new Node(Token.IF, new Node(Token.TRUE), new Node(Token.BLOCK));
    assertFalse(NodeUtil.isLoopStructure(ifNode));
    assertNull(NodeUtil.getLoopCodeBlock(ifNode));
  }
}