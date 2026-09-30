package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableSet;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.TernaryValue;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NodeUtilTest {

  // Tests Math method calls have no side effects (targets Defect 61)
  @Test
  public void testFunctionCallHasSideEffects_mathNamespace_returnsFalse() {
    Node mathName = Node.newString(Token.NAME, "Math");
    Node sinProp = Node.newString(Token.STRING, "sin");
    Node getprop = new Node(Token.GETPROP, mathName, sinProp);
    Node call = new Node(Token.CALL, getprop, Node.newNumber(0.0));

    assertFalse(NodeUtil.functionCallHasSideEffects(call));
  }

  // Tests built-in function calls without side effects
  @Test
  public void testFunctionCallHasSideEffects_builtinFunctions_returnsFalse() {
    Node stringCall = new Node(Token.CALL, Node.newString(Token.NAME, "String"), Node.newNumber(1));
    Node numberCall = new Node(Token.CALL, Node.newString(Token.NAME, "Number"), Node.newString(Token.STRING, "2"));
    Node mathCall = new Node(Token.CALL, Node.newString(Token.NAME, "Math"));

    assertFalse(NodeUtil.functionCallHasSideEffects(stringCall));
    assertFalse(NodeUtil.functionCallHasSideEffects(numberCall));
    assertTrue(NodeUtil.functionCallHasSideEffects(mathCall));
  }

  // Tests object method calls without side effects such as toString()
  @Test
  public void testFunctionCallHasSideEffects_objectToString_returnsFalse() {
    Node obj = new Node(Token.OBJECTLIT);
    Node toStringProp = Node.newString(Token.STRING, "toString");
    Node getprop = new Node(Token.GETPROP, obj, toStringProp);
    Node call = new Node(Token.CALL, getprop);

    assertFalse(NodeUtil.functionCallHasSideEffects(call));
  }

  // Tests constructor calls without side effects
  @Test
  public void testConstructorCallHasSideEffects_knownConstructors() {
    Node arrayNew = new Node(Token.NEW, Node.newString(Token.NAME, "Array"));
    Node regExpNew = new Node(Token.NEW, Node.newString(Token.NAME, "RegExp"));
    Node customNew = new Node(Token.NEW, Node.newString(Token.NAME, "CustomClass"));

    assertFalse(NodeUtil.constructorCallHasSideEffects(arrayNew));
    assertFalse(NodeUtil.constructorCallHasSideEffects(regExpNew));
    assertTrue(NodeUtil.constructorCallHasSideEffects(customNew));
  }

  // Tests side effects check on non-CALL node throwing exception
  @Test(expected = IllegalStateException.class)
  public void testFunctionCallHasSideEffects_nonCallNode_throwsException() {
    Node notCall = new Node(Token.VAR);
    NodeUtil.functionCallHasSideEffects(notCall);
  }

  // Tests side effects check on non-NEW node throwing exception
  @Test(expected = IllegalStateException.class)
  public void testConstructorCallHasSideEffects_nonNewNode_throwsException() {
    Node notNew = new Node(Token.CALL);
    NodeUtil.constructorCallHasSideEffects(notNew);
  }

  // Tests getPureBooleanValue for various literals
  @Test
  public void testGetPureBooleanValue_variousLiterals() {
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(Node.newString(Token.STRING, "hello")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newString(Token.STRING, "")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(Node.newNumber(1.0)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newNumber(0.0)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(new Node(Token.NULL)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(new Node(Token.FALSE)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(new Node(Token.TRUE)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newString(Token.NAME, "undefined")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newString(Token.NAME, "NaN")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(Node.newString(Token.NAME, "Infinity")));
  }

  // Tests getImpureBooleanValue for compound expressions
  @Test
  public void testGetImpureBooleanValue_compoundExpressions() {
    Node comma = new Node(Token.COMMA, Node.newNumber(0), Node.newNumber(1));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(comma));

    Node and = new Node(Token.AND, new Node(Token.TRUE), new Node(Token.FALSE));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(and));

    Node or = new Node(Token.OR, new Node(Token.FALSE), new Node(Token.TRUE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(or));

    Node not = new Node(Token.NOT, new Node(Token.FALSE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(not));
  }

  // Tests getStringValue for different node types
  @Test
  public void testGetStringValue_variousNodes() {
    assertEquals("test", NodeUtil.getStringValue(Node.newString(Token.STRING, "test")));
    assertEquals("42", NodeUtil.getStringValue(Node.newNumber(42.0)));
    assertEquals("3.14", NodeUtil.getStringValue(Node.newNumber(3.14)));
    assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
    assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID, Node.newNumber(0))));
    assertEquals("[object Object]", NodeUtil.getStringValue(new Node(Token.OBJECTLIT)));

    Node arrayLit = new Node(Token.ARRAYLIT, Node.newString(Token.STRING, "a"), Node.newString(Token.STRING, "b"));
    assertEquals("a,b", NodeUtil.getStringValue(arrayLit));
  }

  // Tests getNumberValue for various node types
  @Test
  public void testGetNumberValue_variousNodes() {
    assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(new Node(Token.TRUE)));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.FALSE)));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.NULL)));
    assertEquals(Double.valueOf(123.0), NodeUtil.getNumberValue(Node.newNumber(123.0)));
    assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), NodeUtil.getNumberValue(Node.newString(Token.NAME, "Infinity")));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "NaN"))));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "undefined"))));
  }

  // Tests getStringNumberValue parsing strings
  @Test
  public void testGetStringNumberValue_validAndInvalidStrings() {
    assertEquals(Double.valueOf(0.0), NodeUtil.getStringNumberValue(""));
    assertEquals(Double.valueOf(10.0), NodeUtil.getStringNumberValue("  10  "));
    assertEquals(Double.valueOf(255.0), NodeUtil.getStringNumberValue("0xFF"));
    assertNull(NodeUtil.getStringNumberValue("infinity"));
    assertNull(NodeUtil.getStringNumberValue("+0x10"));
    assertNull(NodeUtil.getStringNumberValue("val\u000bue"));
  }

  // Tests isImmutableValue for literals and expressions
  @Test
  public void testIsImmutableValue_variousNodes() {
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.STRING, "str")));
    assertTrue(NodeUtil.isImmutableValue(Node.newNumber(100)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "undefined")));
    assertFalse(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "foo")));
    assertFalse(NodeUtil.isImmutableValue(new Node(Token.ARRAYLIT)));
  }

  // Tests isLiteralValue with and without functions
  @Test
  public void testIsLiteralValue_nestedStructures() {
    Node arrayLit = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newString(Token.STRING, "a"));
    assertTrue(NodeUtil.isLiteralValue(arrayLit, false));

    Node objLit = new Node(Token.OBJECTLIT);
    Node key = Node.newString(Token.STRING, "k");
    key.addChildToBack(Node.newNumber(2));
    objLit.addChildToBack(key);
    assertTrue(NodeUtil.isLiteralValue(objLit, false));

    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    assertTrue(NodeUtil.isLiteralValue(fn, true));
    assertFalse(NodeUtil.isLiteralValue(fn, false));
  }

  // Tests isValidDefineValue for valid and invalid expressions
  @Test
  public void testIsValidDefineValue_operatorsAndLiterals() {
    Set<String> defines = ImmutableSet.of("DEF_A", "DEF_B");

    assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(1), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newString(Token.STRING, "val"), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "DEF_A"), defines));
    assertFalse(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "UNKNOWN_VAR"), defines));

    Node add = new Node(Token.ADD, Node.newNumber(1), Node.newString(Token.NAME, "DEF_B"));
    assertTrue(NodeUtil.isValidDefineValue(add, defines));

    Node invalidAdd = new Node(Token.ADD, Node.newNumber(1), Node.newString(Token.NAME, "UNKNOWN_VAR"));
    assertFalse(NodeUtil.isValidDefineValue(invalidAdd, defines));
  }

  // Tests opToStr and opToStrNoFail
  @Test
  public void testOpToStr_validOperators() {
    assertEquals("+", NodeUtil.opToStr(Token.ADD));
    assertEquals("-", NodeUtil.opToStr(Token.SUB));
    assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    assertEquals("void", NodeUtil.opToStr(Token.VOID));
    assertEquals("instanceof", NodeUtil.opToStr(Token.INSTANCEOF));
    assertNull(NodeUtil.opToStr(Token.FUNCTION));
    assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));
  }

  // Tests opToStrNoFail throwing Error on invalid operator
  @Test(expected = Error.class)
  public void testOpToStrNoFail_invalidOperator_throwsError() {
    NodeUtil.opToStrNoFail(Token.FUNCTION);
  }

  // Tests isAssociative and isCommutative
  @Test
  public void testIsAssociativeAndIsCommutative() {
    assertTrue(NodeUtil.isAssociative(Token.MUL));
    assertTrue(NodeUtil.isAssociative(Token.AND));
    assertFalse(NodeUtil.isAssociative(Token.ADD));
    assertFalse(NodeUtil.isAssociative(Token.SUB));

    assertTrue(NodeUtil.isCommutative(Token.MUL));
    assertTrue(NodeUtil.isCommutative(Token.BITOR));
    assertFalse(NodeUtil.isCommutative(Token.ADD));
    assertFalse(NodeUtil.isCommutative(Token.DIV));
  }

  // Tests evaluatesToLocalValue
  @Test
  public void testEvaluatesToLocalValue_literalsAndOps() {
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newNumber(42)));
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newString(Token.STRING, "str")));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ARRAYLIT)));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.OBJECTLIT)));
    assertFalse(NodeUtil.evaluatesToLocalValue(Node.newString(Token.NAME, "globalVar")));
  }

  // Tests newQualifiedNameNode creation
  @Test
  public void testNewQualifiedNameNode_simpleAndCompoundNames() {
    CodingConvention convention = new DefaultCodingConvention();

    Node simple = NodeUtil.newQualifiedNameNode(convention, "foo", 1, 0);
    assertTrue(simple.isName());
    assertEquals("foo", simple.getString());

    Node qualified = NodeUtil.newQualifiedNameNode(convention, "foo.bar.baz", 1, 0);
    assertTrue(qualified.isGetProp());
    assertEquals("baz", qualified.getLastChild().getString());
    assertEquals("bar", qualified.getFirstChild().getLastChild().getString());
    assertEquals("foo", qualified.getFirstChild().getFirstChild().getString());
  }

  // Tests isSimpleOperator
  @Test
  public void testIsSimpleOperator_variousNodeTypes() {
    assertTrue(NodeUtil.isSimpleOperator(new Node(Token.ADD)));
    assertTrue(NodeUtil.isSimpleOperator(new Node(Token.SUB)));
    assertTrue(NodeUtil.isSimpleOperator(new Node(Token.TYPEOF)));
    assertFalse(NodeUtil.isSimpleOperator(new Node(Token.ASSIGN)));
    assertFalse(NodeUtil.isSimpleOperator(new Node(Token.CALL)));
  }

  // Tests isPrototypeProperty and getPrototypeClassName
  @Test
  public void testPrototypeHelpers() {
    CodingConvention convention = new DefaultCodingConvention();
    Node qname = NodeUtil.newQualifiedNameNode(convention, "MyClass.prototype.myMethod", 1, 0);

    assertTrue(NodeUtil.isPrototypeProperty(qname));
    assertEquals("myMethod", NodeUtil.getPrototypePropertyName(qname));

    Node classNameNode = NodeUtil.getPrototypeClassName(qname);
    assertNotNull(classNameNode);
    assertEquals("MyClass", classNameNode.getString());
  }
}