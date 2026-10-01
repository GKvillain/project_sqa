package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PeepholeFoldConstantsTest {

  private PeepholeFoldConstants folder;
  private Compiler compiler;

  @Before
  public void setUp() {
    folder = new PeepholeFoldConstants();
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
  }

  private Node fold(Node n) {
    Node parent = new Node(Token.EXPR_RESULT, n);
    folder.beginTraversal(new NodeTraversal(compiler, null));
    return folder.optimizeSubtree(n);
  }

  // Tests folding of division by zero (Defects4J Closure 78 bug trigger)
  @Test
  public void testOptimizeSubtree_divideByZero_returnsOriginalAndReportsError() {
    Node left = Node.newNumber(1.0);
    Node right = Node.newNumber(0.0);
    Node div = new Node(Token.DIV, left, right);
    Node result = fold(div);

    assertEquals(Token.DIV, result.getType());
    assertEquals(1, compiler.getErrorCount());
  }

  // Tests folding of modulo by zero (Defects4J Closure 78 bug trigger)
  @Test
  public void testOptimizeSubtree_moduloByZero_returnsOriginalAndReportsError() {
    Node left = Node.newNumber(10.0);
    Node right = Node.newNumber(0.0);
    Node mod = new Node(Token.MOD, left, right);
    Node result = fold(mod);

    assertEquals(Token.MOD, result.getType());
    assertEquals(1, compiler.getErrorCount());
  }

  // Tests normal arithmetic folding
  @Test
  public void testOptimizeSubtree_arithmeticAdd_foldsToSum() {
    Node left = Node.newNumber(2.5);
    Node right = Node.newNumber(3.5);
    Node add = new Node(Token.ADD, left, right);
    Node result = fold(add);

    assertEquals(Token.NUMBER, result.getType());
    assertEquals(6.0, result.getDouble(), 0.0);
  }

  // Tests string concatenation folding
  @Test
  public void testOptimizeSubtree_stringConcat_foldsToString() {
    Node left = Node.newString("hello ");
    Node right = Node.newString("world");
    Node add = new Node(Token.ADD, left, right);
    Node result = fold(add);

    assertEquals(Token.STRING, result.getType());
    assertEquals("hello world", result.getString());
  }

  // Tests unary NOT folding
  @Test
  public void testOptimizeSubtree_unaryNot_foldsToOppositeBoolean() {
    Node child = new Node(Token.TRUE);
    Node not = new Node(Token.NOT, child);
    Node result = fold(not);

    assertEquals(Token.FALSE, result.getType());
  }

  // Tests unary NOT preserving !0 and !1
  @Test
  public void testOptimizeSubtree_unaryNotZero_doesNotFold() {
    Node child = Node.newNumber(0.0);
    Node not = new Node(Token.NOT, child);
    Node result = fold(not);

    assertEquals(Token.NOT, result.getType());
  }

  // Tests unary NEG folding
  @Test
  public void testOptimizeSubtree_unaryNeg_foldsToNegativeNumber() {
    Node child = Node.newNumber(5.0);
    Node neg = new Node(Token.NEG, child);
    Node result = fold(neg);

    assertEquals(Token.NUMBER, result.getType());
    assertEquals(-5.0, result.getDouble(), 0.0);
  }

  // Tests unary BITNOT folding
  @Test
  public void testOptimizeSubtree_unaryBitNot_foldsToBitwiseComplement() {
    Node child = Node.newNumber(0.0);
    Node bitNot = new Node(Token.BITNOT, child);
    Node result = fold(bitNot);

    assertEquals(Token.NUMBER, result.getDouble() == -1.0 ? Token.NUMBER : result.getType());
    assertEquals(-1.0, result.getDouble(), 0.0);
  }

  // Tests typeof folding for literals
  @Test
  public void testOptimizeSubtree_typeofString_foldsToStringLiteral() {
    Node child = Node.newString("abc");
    Node typeOf = new Node(Token.TYPEOF, child);
    Node result = fold(typeOf);

    assertEquals(Token.STRING, result.getType());
    assertEquals("string", result.getString());
  }

  // Tests typeof folding for numbers
  @Test
  public void testOptimizeSubtree_typeofNumber_foldsToNumberLiteral() {
    Node child = Node.newNumber(123.0);
    Node typeOf = new Node(Token.TYPEOF, child);
    Node result = fold(typeOf);

    assertEquals(Token.STRING, result.getType());
    assertEquals("number", result.getString());
  }

  // Tests shift operator folding
  @Test
  public void testOptimizeSubtree_leftShift_foldsToShiftedValue() {
    Node left = Node.newNumber(1.0);
    Node right = Node.newNumber(2.0);
    Node lsh = new Node(Token.LSH, left, right);
    Node result = fold(lsh);

    assertEquals(Token.NUMBER, result.getType());
    assertEquals(4.0, result.getDouble(), 0.0);
  }

  // Tests comparison folding
  @Test
  public void testOptimizeSubtree_comparisonLessThan_foldsToTrue() {
    Node left = Node.newNumber(1.0);
    Node right = Node.newNumber(2.0);
    Node lt = new Node(Token.LT, left, right);
    Node result = fold(lt);

    assertEquals(Token.TRUE, result.getType());
  }

  // Tests comparison equality folding for strings
  @Test
  public void testOptimizeSubtree_stringEquality_foldsToFalse() {
    Node left = Node.newString("a");
    Node right = Node.newString("b");
    Node eq = new Node(Token.SHEQ, left, right);
    Node result = fold(eq);

    assertEquals(Token.FALSE, result.getType());
  }

  // Tests AND/OR logical operator folding
  @Test
  public void testOptimizeSubtree_logicalAndWithTrue_foldsToRight() {
    Node left = new Node(Token.TRUE);
    Node right = Node.newString("foo");
    Node and = new Node(Token.AND, left, right);
    Node result = fold(and);

    assertEquals(Token.STRING, result.getType());
    assertEquals("foo", result.getString());
  }

  // Tests array length property access folding
  @Test
  public void testOptimizeSubtree_arrayLiteralLength_foldsToLengthNumber() {
    Node array = new Node(Token.ARRAYLIT, Node.newNumber(1.0), Node.newNumber(2.0));
    Node prop = Node.newString("length");
    Node getProp = new Node(Token.GETPROP, array, prop);
    Node result = fold(getProp);

    assertEquals(Token.NUMBER, result.getType());
    assertEquals(2.0, result.getDouble(), 0.0);
  }

  // Tests string toLowerCase folding
  @Test
  public void testOptimizeSubtree_stringToLowerCase_foldsToLowercaseString() {
    Node target = Node.newString("HELLO");
    Node method = Node.newString("toLowerCase");
    Node getProp = new Node(Token.GETPROP, target, method);
    Node call = new Node(Token.CALL, getProp);
    Node result = fold(call);

    assertEquals(Token.STRING, result.getType());
    assertEquals("hello", result.getString());
  }

  // Tests array join folding
  @Test
  public void testOptimizeSubtree_arrayJoin_foldsToJoinedString() {
    Node array = new Node(Token.ARRAYLIT, Node.newString("a"), Node.newString("b"));
    Node method = Node.newString("join");
    Node getProp = new Node(Token.GETPROP, array, method);
    Node call = new Node(Token.CALL, getProp, Node.newString(","));
    Node result = fold(call);

    assertEquals(Token.STRING, result.getType());
    assertEquals("a,b", result.getString());
  }

  // Tests array get element folding
  @Test
  public void testOptimizeSubtree_arrayGetElem_foldsToElement() {
    Node array = new Node(Token.ARRAYLIT, Node.newString("first"), Node.newString("second"));
    Node index = Node.newNumber(1.0);
    Node getElem = new Node(Token.GETELEM, array, index);
    Node result = fold(getElem);

    assertEquals(Token.STRING, result.getType());
    assertEquals("second", result.getString());
  }

  // Tests subtraction folding
  @Test
  public void testOptimizeSubtree_subtraction_foldsToDifference() {
    Node left = Node.newNumber(10.0);
    Node right = Node.newNumber(4.0);
    Node sub = new Node(Token.SUB, left, right);
    Node result = fold(sub);

    assertEquals(Token.NUMBER, result.getType());
    assertEquals(6.0, result.getDouble(), 0.0);
  }

  // Tests multiplication folding
  @Test
  public void testOptimizeSubtree_multiplication_foldsToProduct() {
    Node left = Node.newNumber(3.0);
    Node right = Node.newNumber(4.0);
    Node mul = new Node(Token.MUL, left, right);
    Node result = fold(mul);

    assertEquals(Token.NUMBER, result.getType());
    assertEquals(12.0, result.getDouble(), 0.0);
  }

  // Tests bitwise AND, OR, XOR folding
  @Test
  public void testOptimizeSubtree_bitwiseOperations_foldsCorrectly() {
    Node left = Node.newNumber(6.0); // 110
    Node right = Node.newNumber(3.0); // 011

    Node bitAnd = new Node(Token.BITAND, left.cloneTree(), right.cloneTree());
    Node resultAnd = fold(bitAnd);
    assertEquals(Token.NUMBER, resultAnd.getType());
    assertEquals(2.0, resultAnd.getDouble(), 0.0);

    Node bitOr = new Node(Token.BITOR, left.cloneTree(), right.cloneTree());
    Node resultOr = fold(bitOr);
    assertEquals(Token.NUMBER, resultOr.getType());
    assertEquals(7.0, resultOr.getDouble(), 0.0);

    Node bitXor = new Node(Token.BITXOR, left.cloneTree(), right.cloneTree());
    Node resultXor = fold(bitXor);
    assertEquals(Token.NUMBER, resultXor.getType());
    assertEquals(5.0, resultXor.getDouble(), 0.0);
  }

  // Tests right shift and unsigned right shift folding
  @Test
  public void testOptimizeSubtree_rightShift_foldsCorrectly() {
    Node left = Node.newNumber(-8.0);
    Node right = Node.newNumber(2.0);

    Node rsh = new Node(Token.RSH, left.cloneTree(), right.cloneTree());
    Node resultRsh = fold(rsh);
    assertEquals(Token.NUMBER, resultRsh.getType());
    assertEquals(-2.0, resultRsh.getDouble(), 0.0);

    Node ursh = new Node(Token.URSH, Node.newNumber(8.0), Node.newNumber(2.0));
    Node resultUrsh = fold(ursh);
    assertEquals(Token.NUMBER, resultUrsh.getType());
    assertEquals(2.0, resultUrsh.getDouble(), 0.0);
  }

  // Tests comparison operators (GT, LE, GE, EQ, NE, SHNE)
  @Test
  public void testOptimizeSubtree_comparisonOperators_foldsCorrectly() {
    Node gt = new Node(Token.GT, Node.newNumber(5.0), Node.newNumber(2.0));
    assertEquals(Token.TRUE, fold(gt).getType());

    Node le = new Node(Token.LE, Node.newNumber(5.0), Node.newNumber(2.0));
    assertEquals(Token.FALSE, fold(le).getType());

    Node ge = new Node(Token.GE, Node.newNumber(5.0), Node.newNumber(5.0));
    assertEquals(Token.TRUE, fold(ge).getType());

    Node eq = new Node(Token.EQ, Node.newNumber(5.0), Node.newNumber(5.0));
    assertEquals(Token.TRUE, fold(eq).getType());

    Node ne = new Node(Token.NE, Node.newNumber(5.0), Node.newNumber(3.0));
    assertEquals(Token.TRUE, fold(ne).getType());

    Node shne = new Node(Token.SHNE, Node.newString("a"), Node.newString("a"));
    assertEquals(Token.FALSE, fold(shne).getType());
  }

  // Tests logical OR operator folding
  @Test
  public void testOptimizeSubtree_logicalOrWithFalse_foldsToRight() {
    Node left = new Node(Token.FALSE);
    Node right = Node.newString("bar");
    Node or = new Node(Token.OR, left, right);
    Node result = fold(or);

    assertEquals(Token.STRING, result.getType());
    assertEquals("bar", result.getString());
  }

  // Tests ternary hook operator folding
  @Test
  public void testOptimizeSubtree_hookTrueCondition_foldsToThenClause() {
    Node cond = new Node(Token.TRUE);
    Node thenBranch = Node.newString("yes");
    Node elseBranch = Node.newString("no");
    Node hook = new Node(Token.HOOK, cond, thenBranch, elseBranch);
    Node result = fold(hook);

    assertEquals(Token.STRING, result.getType());
    assertEquals("yes", result.getString());
  }

  @Test
  public void testOptimizeSubtree_hookFalseCondition_foldsToElseClause() {
    Node cond = new Node(Token.FALSE);
    Node thenBranch = Node.newString("yes");
    Node elseBranch = Node.newString("no");
    Node hook = new Node(Token.HOOK, cond, thenBranch, elseBranch);
    Node result = fold(hook);

    assertEquals(Token.STRING, result.getType());
    assertEquals("no", result.getString());
  }

  // Tests string methods: indexOf, charAt, substring, toUpperCase
  @Test
  public void testOptimizeSubtree_stringMethods_foldsCorrectly() {
    // indexOf
    Node target = Node.newString("abcdef");
    Node indexOfCall = new Node(Token.CALL, new Node(Token.GETPROP, target.cloneTree(), Node.newString("indexOf")), Node.newString("cd"));
    Node resultIndexOf = fold(indexOfCall);
    assertEquals(Token.NUMBER, resultIndexOf.getType());
    assertEquals(2.0, resultIndexOf.getDouble(), 0.0);

    // charAt
    Node charAtCall = new Node(Token.CALL, new Node(Token.GETPROP, target.cloneTree(), Node.newString("charAt")), Node.newNumber(1.0));
    Node resultCharAt = fold(charAtCall);
    assertEquals(Token.STRING, resultCharAt.getType());
    assertEquals("b", resultCharAt.getString());

    // substring
    Node substringCall = new Node(Token.CALL, new Node(Token.GETPROP, target.cloneTree(), Node.newString("substring")), Node.newNumber(1.0), Node.newNumber(3.0));
    Node resultSubstring = fold(substringCall);
    assertEquals(Token.STRING, resultSubstring.getType());
    assertEquals("bc", resultSubstring.getString());

    // toUpperCase
    Node toUpperCaseCall = new Node(Token.CALL, new Node(Token.GETPROP, Node.newString("world"), Node.newString("toUpperCase")));
    Node resultToUpperCase = fold(toUpperCaseCall);
    assertEquals(Token.STRING, resultToUpperCase.getType());
    assertEquals("WORLD", resultToUpperCase.getString());
  }

  // Tests string length property access folding
  @Test
  public void testOptimizeSubtree_stringLength_foldsToLengthNumber() {
    Node str = Node.newString("hello");
    Node prop = Node.newString("length");
    Node getProp = new Node(Token.GETPROP, str, prop);
    Node result = fold(getProp);

    assertEquals(Token.NUMBER, result.getType());
    assertEquals(5.0, result.getDouble(), 0.0);
  }

  // Tests typeof folding for boolean, object, undefined
  @Test
  public void testOptimizeSubtree_typeofOtherTypes_foldsCorrectly() {
    Node typeofBool = new Node(Token.TYPEOF, new Node(Token.TRUE));
    assertEquals("boolean", fold(typeofBool).getString());

    Node typeofNull = new Node(Token.TYPEOF, new Node(Token.NULL));
    assertEquals("object", fold(typeofNull).getString());

    Node typeofVoid = new Node(Token.TYPEOF, new Node(Token.VOID, Node.newNumber(0.0)));
    assertEquals("undefined", fold(typeofVoid).getString());
  }

  // Tests comma operator folding
  @Test
  public void testOptimizeSubtree_commaWithNoSideEffects_foldsToSecond() {
    Node left = Node.newNumber(1.0);
    Node right = Node.newString("done");
    Node comma = new Node(Token.COMMA, left, right);
    Node result = fold(comma);

    assertEquals(Token.STRING, result.getType());
    assertEquals("done", result.getString());
  }
}