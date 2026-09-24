package com.google.javascript.jscomp;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

public class PeepholeFoldConstantsTest {

  private PeepholeFoldConstants peephole;
  private Compiler compiler;

  @Before
  public void setUp() {
    peephole = new PeepholeFoldConstants();
    compiler = new Compiler();
  }

  private Node fold(Node root) {
    Node parent = new Node(Token.EXPR_RESULT, root);
    Node result = peephole.optimizeSubtree(root);
    return result;
  }

  // Tests array element get with in-bounds constant index (Defects4J 161b regression)
  @Test
  public void testTryFoldArrayAccess_inBoundsIndex_returnsElement() {
    Node elem0 = Node.newString("a");
    Node elem1 = Node.newString("b");
    Node arrayLit = new Node(Token.ARRAYLIT, elem0, elem1);
    Node indexNode = Node.newNumber(1.0);
    Node getElem = new Node(Token.GETELEM, arrayLit, indexNode);

    Node result = fold(getElem);
    assertEquals(Token.STRING, result.getType());
    assertEquals("b", result.getString());
  }

  // Tests array element get with out of bounds index (Defects4J 161b regression)
  @Test
  public void testTryFoldArrayAccess_outOfBoundsIndex_doesNotThrowException() {
    Node elem0 = Node.newString("a");
    Node arrayLit = new Node(Token.ARRAYLIT, elem0);
    Node indexNode = Node.newNumber(2.0);
    Node getElem = new Node(Token.GETELEM, arrayLit, indexNode);

    Node result = fold(getElem);
    assertEquals(Token.GETELEM, result.getType());
  }

  // Tests array element get with negative index
  @Test
  public void testTryFoldArrayAccess_negativeIndex_doesNotFold() {
    Node elem0 = Node.newString("a");
    Node arrayLit = new Node(Token.ARRAYLIT, elem0);
    Node indexNode = Node.newNumber(-1.0);
    Node getElem = new Node(Token.GETELEM, arrayLit, indexNode);

    Node result = fold(getElem);
    assertEquals(Token.GETELEM, result.getType());
  }

  // Tests array element get with non-integer index
  @Test
  public void testTryFoldArrayAccess_fractionalIndex_doesNotFold() {
    Node elem0 = Node.newString("a");
    Node arrayLit = new Node(Token.ARRAYLIT, elem0);
    Node indexNode = Node.newNumber(0.5);
    Node getElem = new Node(Token.GETELEM, arrayLit, indexNode);

    Node result = fold(getElem);
    assertEquals(Token.GETELEM, result.getType());
  }

  // Tests array length property folding
  @Test
  public void testTryFoldGetProp_arrayLength_foldsToCount() {
    Node elem0 = Node.newNumber(1.0);
    Node elem1 = Node.newNumber(2.0);
    Node arrayLit = new Node(Token.ARRAYLIT, elem0, elem1);
    Node propNode = Node.newString("length");
    Node getProp = new Node(Token.GETPROP, arrayLit, propNode);

    Node result = fold(getProp);
    assertEquals(Token.NUMBER, result.getType());
    assertEquals(2.0, result.getDouble(), 0.0);
  }

  // Tests string length property folding
  @Test
  public void testTryFoldGetProp_stringLength_foldsToLength() {
    Node strNode = Node.newString("hello");
    Node propNode = Node.newString("length");
    Node getProp = new Node(Token.GETPROP, strNode, propNode);

    Node result = fold(getProp);
    assertEquals(Token.NUMBER, result.getType());
    assertEquals(5.0, result.getDouble(), 0.0);
  }

  // Tests typeof folding for constant literal
  @Test
  public void testTryFoldTypeof_stringLiteral_foldsToString() {
    Node strNode = Node.newString("hello");
    Node typeofNode = new Node(Token.TYPEOF, strNode);

    Node result = fold(typeofNode);
    assertEquals(Token.STRING, result.getType());
    assertEquals("string", result.getString());
  }

  // Tests typeof folding for number literal
  @Test
  public void testTryFoldTypeof_numberLiteral_foldsToNumber() {
    Node numNode = Node.newNumber(123.0);
    Node typeofNode = new Node(Token.TYPEOF, numNode);

    Node result = fold(typeofNode);
    assertEquals(Token.STRING, result.getType());
    assertEquals("number", result.getString());
  }

  // Tests unary NOT on boolean true
  @Test
  public void testTryFoldUnaryOperator_notTrue_foldsToFalse() {
    Node trueNode = new Node(Token.TRUE);
    Node notNode = new Node(Token.NOT, trueNode);

    Node result = fold(notNode);
    assertEquals(Token.FALSE, result.getType());
  }

  // Tests unary NEG on number
  @Test
  public void testTryFoldUnaryOperator_negNumber_foldsToNegative() {
    Node numNode = Node.newNumber(5.0);
    Node negNode = new Node(Token.NEG, numNode);

    Node result = fold(negNode);
    assertEquals(Token.NUMBER, result.getType());
    assertEquals(-5.0, result.getDouble(), 0.0);
  }

  // Tests binary arithmetic addition
  @Test
  public void testTryFoldBinaryOperator_addNumbers_foldsToSum() {
    Node left = Node.newNumber(3.0);
    Node right = Node.newNumber(4.0);
    Node addNode = new Node(Token.ADD, left, right);

    Node result = fold(addNode);
    assertEquals(Token.NUMBER, result.getType());
    assertEquals(7.0, result.getDouble(), 0.0);
  }

  // Tests string concatenation folding
  @Test
  public void testTryFoldBinaryOperator_addStrings_foldsToConcatenation() {
    Node left = Node.newString("foo");
    Node right = Node.newString("bar");
    Node addNode = new Node(Token.ADD, left, right);

    Node result = fold(addNode);
    assertEquals(Token.STRING, result.getType());
    assertEquals("foobar", result.getString());
  }

  // Tests binary comparison equal
  @Test
  public void testTryFoldComparison_equalNumbers_foldsToTrue() {
    Node left = Node.newNumber(10.0);
    Node right = Node.newNumber(10.0);
    Node eqNode = new Node(Token.EQ, left, right);

    Node result = fold(eqNode);
    assertEquals(Token.TRUE, result.getType());
  }

  // Tests binary comparison strictly not equal
  @Test
  public void testTryFoldComparison_differentStrings_foldsToTrue() {
    Node left = Node.newString("a");
    Node right = Node.newString("b");
    Node neNode = new Node(Token.NE, left, right);

    Node result = fold(neNode);
    assertEquals(Token.TRUE, result.getType());
  }

  // Tests bitwise shift operation
  @Test
  public void testTryFoldShift_leftShift_foldsCorrectly() {
    Node left = Node.newNumber(2.0);
    Node right = Node.newNumber(3.0);
    Node lshNode = new Node(Token.LSH, left, right);

    Node result = fold(lshNode);
    assertEquals(Token.NUMBER, result.getType());
    assertEquals(16.0, result.getDouble(), 0.0);
  }

  // Tests logical AND operator with constant false left operand
  @Test
  public void testTryFoldAndOr_andWithFalse_foldsToFalse() {
    Node left = new Node(Token.FALSE);
    Node right = Node.newString("unused");
    Node andNode = new Node(Token.AND, left, right);

    Node result = fold(andNode);
    assertEquals(Token.FALSE, result.getType());
  }

  // Tests logical OR operator with constant true left operand
  @Test
  public void testTryFoldAndOr_orWithTrue_foldsToTrue() {
    Node left = new Node(Token.TRUE);
    Node right = Node.newString("unused");
    Node orNode = new Node(Token.OR, left, right);

    Node result = fold(orNode);
    assertEquals(Token.TRUE, result.getType());
  }
}