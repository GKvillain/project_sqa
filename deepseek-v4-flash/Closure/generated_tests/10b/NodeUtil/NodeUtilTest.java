package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.TernaryValue;

import org.junit.Test;
import org.junit.Before;

public class NodeUtilTest {

  // Tests for getPureBooleanValue

  // Tests string literal non-empty returns TRUE
  @Test
  public void testGetPureBooleanValue_stringNonEmpty_returnsTrue() {
    Node stringNode = IR.string("hello");
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(stringNode));
  }

  // Tests string literal empty returns FALSE
  @Test
  public void testGetPureBooleanValue_stringEmpty_returnsFalse() {
    Node stringNode = IR.string("");
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(stringNode));
  }

  // Tests number zero returns FALSE
  @Test
  public void testGetPureBooleanValue_numberZero_returnsFalse() {
    Node numberNode = IR.number(0);
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(numberNode));
  }

  // Tests number non-zero returns TRUE
  @Test
  public void testGetPureBooleanValue_numberNonZero_returnsTrue() {
    Node numberNode = IR.number(1);
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(numberNode));
  }

  // Tests null node returns FALSE
  @Test
  public void testGetPureBooleanValue_null_returnsFalse() {
    Node nullNode = IR.nullNode();
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(nullNode));
  }

  // Tests undefined name returns FALSE
  @Test
  public void testGetPureBooleanValue_undefinedName_returnsFalse() {
    Node nameNode = IR.name("undefined");
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(nameNode));
  }

  // Tests NaN name returns FALSE
  @Test
  public void testGetPureBooleanValue_nanName_returnsFalse() {
    Node nameNode = IR.name("NaN");
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(nameNode));
  }

  // Tests Infinity name returns TRUE
  @Test
  public void testGetPureBooleanValue_infinityName_returnsTrue() {
    Node nameNode = IR.name("Infinity");
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(nameNode));
  }

  // Tests NOT node with TRUE child returns FALSE
  @Test
  public void testGetPureBooleanValue_notTrue_returnsFalse() {
    Node notNode = IR.not(IR.trueNode());
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(notNode));
  }

  // Tests NOT node with FALSE child returns TRUE
  @Test
  public void testGetPureBooleanValue_notFalse_returnsTrue() {
    Node notNode = IR.not(IR.falseNode());
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(notNode));
  }

  // Tests TRUE node returns TRUE
  @Test
  public void testGetPureBooleanValue_true_returnsTrue() {
    Node trueNode = IR.trueNode();
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(trueNode));
  }

  // Tests FALSE node returns FALSE
  @Test
  public void testGetPureBooleanValue_false_returnsFalse() {
    Node falseNode = IR.falseNode();
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(falseNode));
  }

  // Tests VOID node without side effects returns FALSE
  @Test
  public void testGetPureBooleanValue_voidNoSideEffects_returnsFalse() {
    // void 0 has no side effects
    Node voidNode = IR.voidNode(IR.number(0));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(voidNode));
  }

  // Tests array literal without side effects returns TRUE
  @Test
  public void testGetPureBooleanValue_arrayLitNoSideEffects_returnsTrue() {
    Node arrayLit = IR.arraylit(IR.number(1));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(arrayLit));
  }

  // Tests object literal without side effects returns TRUE
  @Test
  public void testGetPureBooleanValue_objectLitNoSideEffects_returnsTrue() {
    Node objectLit = IR.objectlit();
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(objectLit));
  }

  // Tests for getStringValue

  // Tests string node returns its content
  @Test
  public void testGetStringValue_stringNode_returnsString() {
    Node stringNode = IR.string("test");
    assertEquals("test", NodeUtil.getStringValue(stringNode));
  }

  // Tests number node returns its string representation
  @Test
  public void testGetStringValue_numberNode_returnsString() {
    Node numberNode = IR.number(42);
    assertEquals("42", NodeUtil.getStringValue(numberNode));
  }

  // Tests false node returns "false"
  @Test
  public void testGetStringValue_falseNode_returnsFalseString() {
    Node falseNode = IR.falseNode();
    assertEquals("false", NodeUtil.getStringValue(falseNode));
  }

  // Tests true node returns "true"
  @Test
  public void testGetStringValue_trueNode_returnsTrueString() {
    Node trueNode = IR.trueNode();
    assertEquals("true", NodeUtil.getStringValue(trueNode));
  }

  // Tests null node returns "null"
  @Test
  public void testGetStringValue_nullNode_returnsNullString() {
    Node nullNode = IR.nullNode();
    assertEquals("null", NodeUtil.getStringValue(nullNode));
  }

  // Tests void node returns "undefined"
  @Test
  public void testGetStringValue_voidNode_returnsUndefinedString() {
    Node voidNode = IR.voidNode(IR.number(0));
    assertEquals("undefined", NodeUtil.getStringValue(voidNode));
  }

  // Tests NOT node with TRUE child returns "false" (because NOT is reversed)
  @Test
  public void testGetStringValue_notTrue_returnsFalse() {
    Node notNode = IR.not(IR.trueNode());
    assertEquals("false", NodeUtil.getStringValue(notNode));
  }

  // Tests NOT node with FALSE child returns "true"
  @Test
  public void testGetStringValue_notFalse_returnsTrue() {
    Node notNode = IR.not(IR.falseNode());
    assertEquals("true", NodeUtil.getStringValue(notNode));
  }

  // Tests for isImmutableValue

  // Tests string is immutable
  @Test
  public void testIsImmutableValue_string_returnsTrue() {
    assertTrue(NodeUtil.isImmutableValue(IR.string("hello")));
  }

  // Tests number is immutable
  @Test
  public void testIsImmutableValue_number_returnsTrue() {
    assertTrue(NodeUtil.isImmutableValue(IR.number(5)));
  }

  // Tests null is immutable
  @Test
  public void testIsImmutableValue_null_returnsTrue() {
    assertTrue(NodeUtil.isImmutableValue(IR.nullNode()));
  }

  // Tests true is immutable
  @Test
  public void testIsImmutableValue_true_returnsTrue() {
    assertTrue(NodeUtil.isImmutableValue(IR.trueNode()));
  }

  // Tests false is immutable
  @Test
  public void testIsImmutableValue_false_returnsTrue() {
    assertTrue(NodeUtil.isImmutableValue(IR.falseNode()));
  }

  // Tests undefined name is immutable
  @Test
  public void testIsImmutableValue_undefinedName_returnsTrue() {
    assertTrue(NodeUtil.isImmutableValue(IR.name("undefined")));
  }

  // Tests NaN name is immutable
  @Test
  public void testIsImmutableValue_nanName_returnsTrue() {
    assertTrue(NodeUtil.isImmutableValue(IR.name("NaN")));
  }

  // Tests Infinity name is immutable
  @Test
  public void testIsImmutableValue_infinityName_returnsTrue() {
    assertTrue(NodeUtil.isImmutableValue(IR.name("Infinity")));
  }

  // Tests NOT of immutable value is immutable
  @Test
  public void testIsImmutableValue_notImmutable_returnsTrue() {
    assertTrue(NodeUtil.isImmutableValue(IR.not(IR.trueNode())));
  }

  // Tests for isSimpleOperatorType

  // Tests ADD is simple operator
  @Test
  public void testIsSimpleOperatorType_add_returnsTrue() {
    assertTrue(NodeUtil.isSimpleOperatorType(Token.ADD));
  }

  // Tests SUB is simple operator
  @Test
  public void testIsSimpleOperatorType_sub_returnsTrue() {
    assertTrue(NodeUtil.isSimpleOperatorType(Token.SUB));
  }

  // Tests ASSIGN is not simple operator
  @Test
  public void testIsSimpleOperatorType_assign_returnsFalse() {
    assertFalse(NodeUtil.isSimpleOperatorType(Token.ASSIGN));
  }

  // Tests for isLiteralValue

  // Tests array literal with literal children returns true
  @Test
  public void testIsLiteralValue_arrayLitWithLiteralChildren_returnsTrue() {
    Node arrayLit = IR.arraylit(IR.number(1), IR.string("hello"));
    assertTrue(NodeUtil.isLiteralValue(arrayLit, false));
  }

  // Tests object literal with literal values returns true
  @Test
  public void testIsLiteralValue_objectLitWithLiteralValues_returnsTrue() {
    Node key = IR.stringKey("a");
    key.addChildToBack(IR.number(1));
    Node objectLit = IR.objectlit(key);
    assertTrue(NodeUtil.isLiteralValue(objectLit, false));
  }

  // Tests for getNumberValue

  // Tests true returns 1.0
  @Test
  public void testGetNumberValue_true_returnsOne() {
    assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(IR.trueNode()));
  }

  // Tests false returns 0.0
  @Test
  public void testGetNumberValue_false_returnsZero() {
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(IR.falseNode()));
  }

  // Tests null returns 0.0
  @Test
  public void testGetNumberValue_null_returnsZero() {
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(IR.nullNode()));
  }

  // Tests number node returns its value
  @Test
  public void testGetNumberValue_number_returnsItself() {
    assertEquals(Double.valueOf(3.14), NodeUtil.getNumberValue(IR.number(3.14)));
  }

  // Tests void node with no side effects returns NaN
  @Test
  public void testGetNumberValue_voidNoSideEffects_returnsNaN() {
    Double result = NodeUtil.getNumberValue(IR.voidNode(IR.number(0)));
    assertNotNull(result);
    assertTrue(Double.isNaN(result));
  }

  // Tests undefined name returns NaN
  @Test
  public void testGetNumberValue_undefinedName_returnsNaN() {
    Double result = NodeUtil.getNumberValue(IR.name("undefined"));
    assertNotNull(result);
    assertTrue(Double.isNaN(result));
  }

  // Tests NaN name returns NaN
  @Test
  public void testGetNumberValue_nanName_returnsNaN() {
    Double result = NodeUtil.getNumberValue(IR.name("NaN"));
    assertNotNull(result);
    assertTrue(Double.isNaN(result));
  }

  // Tests Infinity name returns POSITIVE_INFINITY
  @Test
  public void testGetNumberValue_infinityName_returnsPositiveInfinity() {
    assertEquals(Double.POSITIVE_INFINITY,
        NodeUtil.getNumberValue(IR.name("Infinity")), 0.0);
  }

  // Tests string "123" returns 123.0
  @Test
  public void testGetNumberValue_stringNumber_returnsDouble() {
    assertEquals(Double.valueOf(123.0), NodeUtil.getNumberValue(IR.string("123")));
  }

  // Tests for getImpureBooleanValue

  // Tests ASSIGN returns value of RHS
  @Test
  public void testGetImpureBooleanValue_assignWithTrueRHS_returnsTrue() {
    Node assign = IR.assign(IR.name("x"), IR.trueNode());
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(assign));
  }

  // Tests NOT returns inverse
  @Test
  public void testGetImpureBooleanValue_notTrue_returnsFalse() {
    Node notNode = IR.not(IR.trueNode());
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(notNode));
  }

  // Tests arraylit returns TRUE
  @Test
  public void testGetImpureBooleanValue_arraylit_returnsTrue() {
    Node arrayLit = IR.arraylit(IR.number(1));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(arrayLit));
  }

  // Tests objectlit returns TRUE
  @Test
  public void testGetImpureBooleanValue_objectlit_returnsTrue() {
    Node objectLit = IR.objectlit();
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(objectLit));
  }

  // Tests void returns FALSE
  @Test
  public void testGetImpureBooleanValue_void_returnsFalse() {
    Node voidNode = IR.voidNode(IR.number(0));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(voidNode));
  }

  // Tests for isFunctionDeclaration

  // Tests function as statement returns true
  @Test
  public void testIsFunctionDeclaration_functionAsStatement_returnsTrue() {
    Node function = IR.function(IR.name("f"), IR.paramList(), IR.block());
    // A function directly inside a script/block is a statement
    IR.script(function);
    assertTrue(NodeUtil.isFunctionDeclaration(function));
  }

  // Tests function expression returns false
  @Test
  public void testIsFunctionDeclaration_functionExpression_returnsFalse() {
    Node function = IR.function(IR.name(""), IR.paramList(), IR.block());
    // A function as an expression (e.g., var x = function() {})
    IR.name("x").addChildToFront(function);
    assertFalse(NodeUtil.isFunctionDeclaration(function));
  }

  // Tests for isAssignmentOp

  // Tests ASSIGN is assignment op
  @Test
  public void testIsAssignmentOp_assign_returnsTrue() {
    assertTrue(NodeUtil.isAssignmentOp(IR.assign(IR.name("a"), IR.number(1))));
  }

  // Tests ADD is not assignment op
  @Test
  public void testIsAssignmentOp_add_returnsFalse() {
    // ADD is a binary operator, not an assignment operator
    assertFalse(NodeUtil.isAssignmentOp(IR.add(IR.number(1), IR.number(2))));
  }
}