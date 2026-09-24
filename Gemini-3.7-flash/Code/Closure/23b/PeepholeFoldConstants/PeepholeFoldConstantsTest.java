package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PeepholeFoldConstantsTest {

  private PeepholeFoldConstants foldConstants;
  private PeepholeFoldConstants foldConstantsLate;
  private Compiler compiler;

  @Before
  public void setUp() {
    foldConstants = new PeepholeFoldConstants(false);
    foldConstantsLate = new PeepholeFoldConstants(true);
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
  }

  private Node wrapAndOptimize(PeepholeFoldConstants pass, Node node) {
    Node root = IR.exprResult(node);
    IR.root(root);
    pass.beginTraversal(compiler);
    Node result = pass.optimizeSubtree(node);
    pass.endTraversal(compiler);
    return result;
  }

  // Tests array index access at first element (index 0) - targeted at Defect 23b
  @Test
  public void testOptimizeSubtree_arrayAccessFirstElement_foldsCorrectly() {
    Node elem0 = IR.string("first");
    Node elem1 = IR.string("second");
    Node arrayLit = IR.arraylit(elem0, elem1);
    Node index = IR.number(0);
    Node getElem = IR.getelem(arrayLit, index);

    Node result = wrapAndOptimize(foldConstants, getElem);

    assertEquals(Token.STRING, result.getType());
    assertEquals("first", result.getString());
  }

  // Tests array index access at element > 0
  @Test
  public void testOptimizeSubtree_arrayAccessSubsequentElement_foldsCorrectly() {
    Node elem0 = IR.string("first");
    Node elem1 = IR.string("second");
    Node arrayLit = IR.arraylit(elem0, elem1);
    Node index = IR.number(1);
    Node getElem = IR.getelem(arrayLit, index);

    Node result = wrapAndOptimize(foldConstants, getElem);

    assertEquals(Token.STRING, result.getType());
    assertEquals("second", result.getString());
  }

  // Tests array index out of bounds returns error and does not fold
  @Test
  public void testOptimizeSubtree_arrayAccessOutOfBounds_reportsError() {
    Node elem0 = IR.string("first");
    Node arrayLit = IR.arraylit(elem0);
    Node index = IR.number(2);
    Node getElem = IR.getelem(arrayLit, index);

    Node result = wrapAndOptimize(foldConstants, getElem);

    assertEquals(Token.GETELEM, result.getType());
    assertEquals(1, compiler.getErrorCount());
  }

  // Tests array negative index reports error and does not fold
  @Test
  public void testOptimizeSubtree_arrayAccessNegativeIndex_reportsError() {
    Node elem0 = IR.string("first");
    Node arrayLit = IR.arraylit(elem0);
    Node index = IR.number(-1);
    Node getElem = IR.getelem(arrayLit, index);

    Node result = wrapAndOptimize(foldConstants, getElem);

    assertEquals(Token.GETELEM, result.getType());
    assertEquals(1, compiler.getErrorCount());
  }

  // Tests typeof folding for literal strings
  @Test
  public void testOptimizeSubtree_typeofString_foldsToStringLiteral() {
    Node typeofNode = new Node(Token.TYPEOF, IR.string("hello"));

    Node result = wrapAndOptimize(foldConstants, typeofNode);

    assertEquals(Token.STRING, result.getType());
    assertEquals("string", result.getString());
  }

  // Tests typeof folding for literal numbers
  @Test
  public void testOptimizeSubtree_typeofNumber_foldsToNumberLiteral() {
    Node typeofNode = new Node(Token.TYPEOF, IR.number(42));

    Node result = wrapAndOptimize(foldConstants, typeofNode);

    assertEquals(Token.STRING, result.getType());
    assertEquals("number", result.getString());
  }

  // Tests unary NOT operator on boolean literals
  @Test
  public void testOptimizeSubtree_unaryNotTrue_foldsToFalse() {
    Node notNode = new Node(Token.NOT, IR.trueNode());

    Node result = wrapAndOptimize(foldConstants, notNode);

    assertEquals(Token.FALSE, result.getType());
  }

  // Tests unary NEG operator on numbers
  @Test
  public void testOptimizeSubtree_unaryNegNumber_foldsToNegativeNumber() {
    Node negNode = new Node(Token.NEG, IR.number(5.5));

    Node result = wrapAndOptimize(foldConstants, negNode);

    assertEquals(Token.NUMBER, result.getType());
    assertEquals(-5.5, result.getDouble(), 0.0);
  }

  // Tests unary BITNOT operator on integer
  @Test
  public void testOptimizeSubtree_unaryBitNotInt_foldsToBitwiseInverted() {
    Node bitnotNode = new Node(Token.BITNOT, IR.number(0));

    Node result = wrapAndOptimize(foldConstants, bitnotNode);

    assertEquals(Token.NUMBER, result.getType());
    assertEquals(-1.0, result.getDouble(), 0.0);
  }

  // Tests void operator reduction
  @Test
  public void testOptimizeSubtree_voidNonZero_foldsToVoidZero() {
    Node voidNode = new Node(Token.VOID, IR.number(123));

    Node result = wrapAndOptimize(foldConstants, voidNode);

    assertEquals(Token.VOID, result.getType());
    assertEquals(0.0, result.getFirstChild().getDouble(), 0.0);
  }

  // Tests binary arithmetic ADD folding with numbers
  @Test
  public void testOptimizeSubtree_addNumbers_foldsToSum() {
    Node addNode = IR.add(IR.number(10), IR.number(25));

    Node result = wrapAndOptimize(foldConstants, addNode);

    assertEquals(Token.NUMBER, result.getType());
    assertEquals(35.0, result.getDouble(), 0.0);
  }

  // Tests binary string concatenation folding
  @Test
  public void testOptimizeSubtree_addStrings_foldsToConcatenatedString() {
    Node addNode = IR.add(IR.string("foo"), IR.string("bar"));

    Node result = wrapAndOptimize(foldConstants, addNode);

    assertEquals(Token.STRING, result.getType());
    assertEquals("foobar", result.getString());
  }

  // Tests binary comparison EQ with matching literals
  @Test
  public void testOptimizeSubtree_comparisonEqEqualStrings_foldsToTrue() {
    Node eqNode = new Node(Token.EQ, IR.string("abc"), IR.string("abc"));

    Node result = wrapAndOptimize(foldConstants, eqNode);

    assertEquals(Token.TRUE, result.getType());
  }

  // Tests binary comparison LT with numbers
  @Test
  public void testOptimizeSubtree_comparisonLtNumbers_foldsToTrue() {
    Node ltNode = new Node(Token.LT, IR.number(3), IR.number(5));

    Node result = wrapAndOptimize(foldConstants, ltNode);

    assertEquals(Token.TRUE, result.getType());
  }

  // Tests binary shift operator LSH
  @Test
  public void testOptimizeSubtree_shiftLsh_foldsToShiftedValue() {
    Node lshNode = new Node(Token.LSH, IR.number(1), IR.number(3));

    Node result = wrapAndOptimize(foldConstants, lshNode);

    assertEquals(Token.NUMBER, result.getType());
    assertEquals(8.0, result.getDouble(), 0.0);
  }

  // Tests short-circuit logical AND operator with false left node
  @Test
  public void testOptimizeSubtree_andWithFalseLeft_foldsToFalse() {
    Node andNode = new Node(Token.AND, IR.falseNode(), IR.name("x"));

    Node result = wrapAndOptimize(foldConstants, andNode);

    assertEquals(Token.FALSE, result.getType());
  }

  // Tests short-circuit logical OR operator with true left node
  @Test
  public void testOptimizeSubtree_orWithTrueLeft_foldsToTrue() {
    Node orNode = new Node(Token.OR, IR.trueNode(), IR.name("x"));

    Node result = wrapAndOptimize(foldConstants, orNode);

    assertEquals(Token.TRUE, result.getType());
  }

  // Tests getprop length property on array literal
  @Test
  public void testOptimizeSubtree_arrayLengthProp_foldsToLengthNumber() {
    Node arrayLit = IR.arraylit(IR.number(1), IR.number(2), IR.number(3));
    Node getProp = IR.getprop(arrayLit, IR.string("length"));

    Node result = wrapAndOptimize(foldConstants, getProp);

    assertEquals(Token.NUMBER, result.getType());
    assertEquals(3.0, result.getDouble(), 0.0);
  }

  // Tests object property access folding on object literal
  @Test
  public void testOptimizeSubtree_objectPropAccess_foldsToPropertyValue() {
    Node key = IR.stringKey("a");
    key.addChildToFront(IR.number(100));
    Node objLit = IR.objectlit(key);
    Node getProp = IR.getprop(objLit, IR.string("a"));

    Node result = wrapAndOptimize(foldConstants, getProp);

    assertEquals(Token.NUMBER, result.getType());
    assertEquals(100.0, result.getDouble(), 0.0);
  }

  // Tests unfolding assignment operator when late is false
  @Test
  public void testOptimizeSubtree_unfoldAssignAdd_unfoldsCorrectly() {
    Node assignAdd = new Node(Token.ASSIGN_ADD, IR.name("x"), IR.number(5));

    Node result = wrapAndOptimize(foldConstants, assignAdd);

    assertEquals(Token.ASSIGN, result.getType());
    assertEquals(Token.ADD, result.getLastChild().getType());
  }
}