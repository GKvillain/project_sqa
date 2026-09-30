package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PeepholeFoldConstantsTest {

  private PeepholeFoldConstants folder;

  @Before
  public void setUp() {
    folder = new PeepholeFoldConstants();
  }

  private Node fold(Node n) {
    Node parent = new Node(Token.SCRIPT, n);
    Node result = folder.optimizeSubtree(n);
    return result != null ? result : parent.getFirstChild();
  }

  // Tests typeof folding for string, number, boolean, null, and undefined
  @Test
  public void testTryFoldTypeof_literals_foldedToTypeNames() {
    Node typeofStr = new Node(Token.TYPEOF, Node.newString("hello"));
    Node resultStr = fold(typeofStr);
    assertEquals(Token.STRING, resultStr.getType());
    assertEquals("string", resultStr.getString());

    Node typeofNum = new Node(Token.TYPEOF, Node.newNumber(42.0));
    Node resultNum = fold(typeofNum);
    assertEquals(Token.STRING, resultNum.getType());
    assertEquals("number", resultNum.getString());

    Node typeofBool = new Node(Token.TYPEOF, new Node(Token.TRUE));
    Node resultBool = fold(typeofBool);
    assertEquals(Token.STRING, resultBool.getType());
    assertEquals("boolean", resultBool.getString());

    Node typeofNull = new Node(Token.TYPEOF, new Node(Token.NULL));
    Node resultNull = fold(typeofNull);
    assertEquals(Token.STRING, resultNull.getType());
    assertEquals("object", resultNull.getString());

    Node typeofUndef = new Node(Token.TYPEOF, Node.newString(Token.NAME, "undefined"));
    Node resultUndef = fold(typeofUndef);
    assertEquals(Token.STRING, resultUndef.getType());
    assertEquals("undefined", resultUndef.getString());
  }

  // Tests unary NOT operator folding
  @Test
  public void testTryFoldUnaryOperator_not_foldsBooleanLiterals() {
    Node notTrue = new Node(Token.NOT, new Node(Token.TRUE));
    Node resultFalse = fold(notTrue);
    assertEquals(Token.FALSE, resultFalse.getType());

    Node notFalse = new Node(Token.NOT, new Node(Token.FALSE));
    Node resultTrue = fold(notFalse);
    assertEquals(Token.TRUE, resultTrue.getType());
  }

  // Tests unary NEG operator folding with number and NaN
  @Test
  public void testTryFoldUnaryOperator_neg_foldsNumericLiterals() {
    Node negNum = new Node(Token.NEG, Node.newNumber(15.0));
    Node resultNum = fold(negNum);
    assertEquals(Token.NUMBER, resultNum.getType());
    assertEquals(-15.0, resultNum.getDouble(), 0.0);

    Node negNan = new Node(Token.NEG, Node.newString(Token.NAME, "NaN"));
    Node resultNan = fold(negNan);
    assertEquals(Token.NAME, resultNan.getType());
    assertEquals("NaN", resultNan.getString());

    Node negInf = new Node(Token.NEG, Node.newString(Token.NAME, "Infinity"));
    Node resultInf = fold(negInf);
    assertEquals(Token.NEG, resultInf.getType());
  }

  // Tests unary BITNOT operator folding
  @Test
  public void testTryFoldUnaryOperator_bitnot_foldsInteger() {
    Node bitnotNode = new Node(Token.BITNOT, Node.newNumber(5.0));
    Node result = fold(bitnotNode);
    assertEquals(Token.NUMBER, result.getType());
    assertEquals(-6.0, result.getDouble(), 0.0);
  }

  // Tests binary arithmetic operations: ADD, SUB, MUL, DIV
  @Test
  public void testTryFoldArithmetic_numericOperations_foldsCorrectly() {
    Node addNode = new Node(Token.ADD, Node.newNumber(10.0), Node.newNumber(5.0));
    Node resultAdd = fold(addNode);
    assertEquals(15.0, resultAdd.getDouble(), 0.0);

    Node subNode = new Node(Token.SUB, Node.newNumber(10.0), Node.newNumber(4.0));
    Node resultSub = fold(subNode);
    assertEquals(6.0, resultSub.getDouble(), 0.0);

    Node mulNode = new Node(Token.MUL, Node.newNumber(3.0), Node.newNumber(7.0));
    Node resultMul = fold(mulNode);
    assertEquals(21.0, resultMul.getDouble(), 0.0);

    Node divNode = new Node(Token.DIV, Node.newNumber(20.0), Node.newNumber(4.0));
    Node resultDiv = fold(divNode);
    assertEquals(5.0, resultDiv.getDouble(), 0.0);
  }

  // Tests string concatenation with ADD
  @Test
  public void testTryFoldAdd_stringLiterals_concatenatesStrings() {
    Node addStr = new Node(Token.ADD, Node.newString("foo"), Node.newString("bar"));
    Node resultStr = fold(addStr);
    assertEquals(Token.STRING, resultStr.getType());
    assertEquals("foobar", resultStr.getString());
  }

  // Tests left-child string concatenation folding: (x + 'a') + 'b' -> x + 'ab'
  @Test
  public void testTryFoldLeftChildAdd_nestedStringAdd_foldsRightChildren() {
    Node innerAdd = new Node(Token.ADD, Node.newString(Token.NAME, "x"), Node.newString("a"));
    Node outerAdd = new Node(Token.ADD, innerAdd, Node.newString("b"));
    Node result = fold(outerAdd);

    assertEquals(Token.ADD, result.getType());
    assertEquals(Token.NAME, result.getFirstChild().getType());
    assertEquals("x", result.getFirstChild().getString());
    assertEquals(Token.STRING, result.getLastChild().getType());
    assertEquals("ab", result.getLastChild().getString());
  }

  // Tests bitwise AND and OR
  @Test
  public void testTryFoldBitAndOr_bitwiseOperations_foldsCorrectly() {
    Node bitAnd = new Node(Token.BITAND, Node.newNumber(6.0), Node.newNumber(3.0));
    Node resultAnd = fold(bitAnd);
    assertEquals(2.0, resultAnd.getDouble(), 0.0);

    Node bitOr = new Node(Token.BITOR, Node.newNumber(6.0), Node.newNumber(3.0));
    Node resultOr = fold(bitOr);
    assertEquals(7.0, resultOr.getDouble(), 0.0);
  }

  // Tests shift operations: LSH, RSH, URSH
  @Test
  public void testTryFoldShift_shiftOperations_foldsCorrectly() {
    Node lsh = new Node(Token.LSH, Node.newNumber(4.0), Node.newNumber(2.0));
    Node resultLsh = fold(lsh);
    assertEquals(16.0, resultLsh.getDouble(), 0.0);

    Node rsh = new Node(Token.RSH, Node.newNumber(16.0), Node.newNumber(2.0));
    Node resultRsh = fold(rsh);
    assertEquals(4.0, resultRsh.getDouble(), 0.0);

    Node ursh = new Node(Token.URSH, Node.newNumber(-1.0), Node.newNumber(0.0));
    Node resultUrsh = fold(ursh);
    assertEquals(4294967295.0, resultUrsh.getDouble(), 0.0);
  }

  // Tests logical AND and OR folding
  @Test
  public void testTryFoldAndOr_logicalExpressions_shortCircuits() {
    Node andNode = new Node(Token.AND, new Node(Token.TRUE), Node.newString(Token.NAME, "x"));
    Node resultAnd = fold(andNode);
    assertEquals(Token.NAME, resultAnd.getType());
    assertEquals("x", resultAnd.getString());

    Node orNode = new Node(Token.OR, new Node(Token.FALSE), Node.newString(Token.NAME, "y"));
    Node resultOr = fold(orNode);
    assertEquals(Token.NAME, resultOr.getType());
    assertEquals("y", resultOr.getString());
  }

  // Tests comparison operations: EQ, NE, LT, LE, GT, GE, SHEQ, SHNE
  @Test
  public void testTryFoldComparison_numericAndStringComparisons_returnsBooleans() {
    Node lt = new Node(Token.LT, Node.newNumber(1.0), Node.newNumber(2.0));
    assertEquals(Token.TRUE, fold(lt).getType());

    Node gt = new Node(Token.GT, Node.newNumber(1.0), Node.newNumber(2.0));
    assertEquals(Token.FALSE, fold(gt).getType());

    Node eq = new Node(Token.EQ, Node.newString("abc"), Node.newString("abc"));
    assertEquals(Token.TRUE, fold(eq).getType());

    Node ne = new Node(Token.NE, Node.newString("abc"), Node.newString("def"));
    assertEquals(Token.TRUE, fold(ne).getType());

    Node sheq = new Node(Token.SHEQ, new Node(Token.NULL), Node.newString(Token.NAME, "undefined"));
    assertEquals(Token.FALSE, fold(sheq).getType());

    Node eqNullUndef = new Node(Token.EQ, new Node(Token.NULL), Node.newString(Token.NAME, "undefined"));
    assertEquals(Token.TRUE, fold(eqNullUndef).getType());
  }

  // Tests instanceof operator folding
  @Test
  public void testTryFoldInstanceof_immutableAndObject_foldsCorrectly() {
    Node inst1 = new Node(Token.INSTANCEOF, Node.newString("str"), Node.newString(Token.NAME, "Object"));
    Node result1 = fold(inst1);
    assertEquals(Token.FALSE, result1.getType());

    Node inst2 = new Node(Token.INSTANCEOF, new Node(Token.ARRAYLIT), Node.newString(Token.NAME, "Object"));
    Node result2 = fold(inst2);
    assertEquals(Token.TRUE, result2.getType());
  }

  // Tests assignment folding: x = x + y -> x += y
  @Test
  public void testTryFoldAssign_addAssignment_foldsToAssignAdd() {
    Node nameLeft = Node.newString(Token.NAME, "x");
    Node nameRight = Node.newString(Token.NAME, "x");
    Node value = Node.newNumber(5.0);
    Node add = new Node(Token.ADD, nameRight, value);
    Node assign = new Node(Token.ASSIGN, nameLeft, add);

    Node result = fold(assign);
    assertEquals(Token.ASSIGN_ADD, result.getType());
    assertEquals("x", result.getFirstChild().getString());
    assertEquals(5.0, result.getLastChild().getDouble(), 0.0);
  }

  // Tests array element retrieval: [1, 2, 3][1] -> 2
  @Test
  public void testTryFoldGetElem_arrayLiteral_returnsIndexedElement() {
    Node array = new Node(Token.ARRAYLIT, Node.newNumber(10.0), Node.newNumber(20.0), Node.newNumber(30.0));
    Node getElem = new Node(Token.GETELEM, array, Node.newNumber(1.0));

    Node result = fold(getElem);
    assertEquals(Token.NUMBER, result.getType());
    assertEquals(20.0, result.getDouble(), 0.0);
  }

  // Tests array and string length property retrieval
  @Test
  public void testTryFoldGetProp_lengthProperty_returnsLengthNumber() {
    Node array = new Node(Token.ARRAYLIT, Node.newNumber(1.0), Node.newNumber(2.0));
    Node arrayLength = new Node(Token.GETPROP, array, Node.newString("length"));
    Node resultArr = fold(arrayLength);
    assertEquals(Token.NUMBER, resultArr.getType());
    assertEquals(2.0, resultArr.getDouble(), 0.0);

    Node str = Node.newString("hello");
    Node strLength = new Node(Token.GETPROP, str, Node.newString("length"));
    Node resultStr = fold(strLength);
    assertEquals(Token.NUMBER, resultStr.getType());
    assertEquals(5.0, resultStr.getDouble(), 0.0);
  }

  // Tests String.prototype.indexOf and lastIndexOf folding
  @Test
  public void testTryFoldStringIndexOf_knownStringMethods_foldsToIndex() {
    Node getPropIdx = new Node(Token.GETPROP, Node.newString("abcdef"), Node.newString("indexOf"));
    Node callIdx = new Node(Token.CALL, getPropIdx, Node.newString("cd"));
    Node resultIdx = fold(callIdx);
    assertEquals(Token.NUMBER, resultIdx.getType());
    assertEquals(2.0, resultIdx.getDouble(), 0.0);

    Node getPropLast = new Node(Token.GETPROP, Node.newString("abcdefcd"), Node.newString("lastIndexOf"));
    Node callLast = new Node(Token.CALL, getPropLast, Node.newString("cd"));
    Node resultLast = fold(callLast);
    assertEquals(Token.NUMBER, resultLast.getType());
    assertEquals(6.0, resultLast.getDouble(), 0.0);
  }

  // Tests Array.prototype.join folding: ['a', 'b', 'c'].join(',') -> 'a,b,c'
  @Test
  public void testTryFoldStringJoin_arrayJoin_foldsToString() {
    Node array = new Node(Token.ARRAYLIT, Node.newString("a"), Node.newString("b"), Node.newString("c"));
    Node getProp = new Node(Token.GETPROP, array, Node.newString("join"));
    Node call = new Node(Token.CALL, getProp, Node.newString(","));

    Node result = fold(call);
    assertEquals(Token.STRING, result.getType());
    assertEquals("a,b,c", result.getString());
  }

  // Tests ternary HOOK operator folding: true ? a : b -> a and false ? a : b -> b
  @Test
  public void testTryFoldHook_conditionalOperator_foldsToSelectedBranch() {
    Node hookTrue = new Node(Token.HOOK, new Node(Token.TRUE), Node.newNumber(1.0), Node.newNumber(2.0));
    Node resultTrue = fold(hookTrue);
    assertEquals(Token.NUMBER, resultTrue.getType());
    assertEquals(1.0, resultTrue.getDouble(), 0.0);

    Node hookFalse = new Node(Token.HOOK, new Node(Token.FALSE), Node.newNumber(1.0), Node.newNumber(2.0));
    Node resultFalse = fold(hookFalse);
    assertEquals(Token.NUMBER, resultFalse.getType());
    assertEquals(2.0, resultFalse.getDouble(), 0.0);
  }

  // Tests unary POS operator folding: +5 -> 5, +"123" -> 123
  @Test
  public void testTryFoldUnaryOperator_pos_foldsNumericConversion() {
    Node posNum = new Node(Token.POS, Node.newNumber(5.0));
    Node resultNum = fold(posNum);
    assertEquals(Token.NUMBER, resultNum.getType());
    assertEquals(5.0, resultNum.getDouble(), 0.0);

    Node posStr = new Node(Token.POS, Node.newString("123"));
    Node resultStr = fold(posStr);
    assertEquals(Token.NUMBER, resultStr.getType());
    assertEquals(123.0, resultStr.getDouble(), 0.0);
  }

  // Tests VOID operator folding: void 0 -> undefined
  @Test
  public void testTryFoldUnaryOperator_void_foldsToUndefined() {
    Node voidNode = new Node(Token.VOID, Node.newNumber(0.0));
    Node result = fold(voidNode);
    assertEquals(Token.NAME, result.getType());
    assertEquals("undefined", result.getString());
  }

  // Tests modulo arithmetic folding: 10 % 3 -> 1
  @Test
  public void testTryFoldArithmetic_mod_foldsRemainder() {
    Node modNode = new Node(Token.MOD, Node.newNumber(10.0), Node.newNumber(3.0));
    Node result = fold(modNode);
    assertEquals(Token.NUMBER, result.getType());
    assertEquals(1.0, result.getDouble(), 0.0);
  }

  // Tests bitwise XOR folding: 5 ^ 3 -> 6
  @Test
  public void testTryFoldBitXor_bitwiseExclusiveOr_foldsCorrectly() {
    Node bitXor = new Node(Token.BITXOR, Node.newNumber(5.0), Node.newNumber(3.0));
    Node result = fold(bitXor);
    assertEquals(Token.NUMBER, result.getType());
    assertEquals(6.0, result.getDouble(), 0.0);
  }

  // Tests string element retrieval via GETELEM: "hello"[1] -> "e"
  @Test
  public void testTryFoldGetElem_stringLiteral_returnsCharacter() {
    Node getElem = new Node(Token.GETELEM, Node.newString("hello"), Node.newNumber(1.0));
    Node result = fold(getElem);
    assertEquals(Token.STRING, result.getType());
    assertEquals("e", result.getString());
  }

  // Tests String.prototype.charAt and charCodeAt folding
  @Test
  public void testTryFoldStringCharAtAndCharCodeAt_knownString_foldsExpectedValues() {
    Node getPropCharAt = new Node(Token.GETPROP, Node.newString("hello"), Node.newString("charAt"));
    Node callCharAt = new Node(Token.CALL, getPropCharAt, Node.newNumber(1.0));
    Node resultCharAt = fold(callCharAt);
    assertEquals(Token.STRING, resultCharAt.getType());
    assertEquals("e", resultCharAt.getString());

    Node getPropCharCodeAt = new Node(Token.GETPROP, Node.newString("hello"), Node.newString("charCodeAt"));
    Node callCharCodeAt = new Node(Token.CALL, getPropCharCodeAt, Node.newNumber(0.0));
    Node resultCharCodeAt = fold(callCharCodeAt);
    assertEquals(Token.NUMBER, resultCharCodeAt.getType());
    assertEquals(104.0, resultCharCodeAt.getDouble(), 0.0);
  }

  // Tests String.prototype.substr and substring folding
  @Test
  public void testTryFoldStringSubstrAndSubstring_knownString_foldsSlicedString() {
    Node getPropSubstr = new Node(Token.GETPROP, Node.newString("abcdef"), Node.newString("substr"));
    Node callSubstr = new Node(Token.CALL, getPropSubstr, Node.newNumber(1.0), Node.newNumber(3.0));
    Node resultSubstr = fold(callSubstr);
    assertEquals(Token.STRING, resultSubstr.getType());
    assertEquals("bcd", resultSubstr.getString());

    Node getPropSubstring = new Node(Token.GETPROP, Node.newString("abcdef"), Node.newString("substring"));
    Node callSubstring = new Node(Token.CALL, getPropSubstring, Node.newNumber(1.0), Node.newNumber(4.0));
    Node resultSubstring = fold(callSubstring);
    assertEquals(Token.STRING, resultSubstring.getType());
    assertEquals("bcd", resultSubstring.getString());
  }

  // Tests String.prototype.split folding: "a,b,c".split(",") -> ["a", "b", "c"]
  @Test
  public void testTryFoldStringSplit_stringSplit_foldsToArrayLiteral() {
    Node getPropSplit = new Node(Token.GETPROP, Node.newString("a,b,c"), Node.newString("split"));
    Node callSplit = new Node(Token.CALL, getPropSplit, Node.newString(","));
    Node resultSplit = fold(callSplit);
    assertEquals(Token.ARRAYLIT, resultSplit.getType());
    assertEquals(3, resultSplit.getChildCount());
    assertEquals("a", resultSplit.getFirstChild().getString());
    assertEquals("c", resultSplit.getLastChild().getString());
  }

  // Tests heterogeneous ADD concatenation: "foo" + 123 -> "foo123"
  @Test
  public void testTryFoldAdd_stringAndNumber_foldsToString() {
    Node addNode = new Node(Token.ADD, Node.newString("foo"), Node.newNumber(123.0));
    Node result = fold(addNode);
    assertEquals(Token.STRING, result.getType());
    assertEquals("foo123", result.getString());
  }

  // Tests assignment folding for subtract: x = x - y -> x -= y
  @Test
  public void testTryFoldAssign_subAssignment_foldsToAssignSub() {
    Node nameLeft = Node.newString(Token.NAME, "x");
    Node nameRight = Node.newString(Token.NAME, "x");
    Node value = Node.newNumber(3.0);
    Node sub = new Node(Token.SUB, nameRight, value);
    Node assign = new Node(Token.ASSIGN, nameLeft, sub);

    Node result = fold(assign);
    assertEquals(Token.ASSIGN_SUB, result.getType());
    assertEquals("x", result.getFirstChild().getString());
    assertEquals(3.0, result.getLastChild().getDouble(), 0.0);
  }
}