package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;
import java.util.logging.Level;

import static org.junit.Assert.assertEquals;

public class PeepholeFoldConstantsTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    Compiler.setLoggingLevel(Level.OFF);
  }

  private Node fold(String js) {
    Node root = compiler.parseTestCode(js);
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(
        compiler, new PeepholeFoldConstants());
    pass.process(null, root);
    return root;
  }

  private void test(String js, String expectedJs) {
    Node root = fold(js);
    Node expectedRoot = compiler.parseTestCode(expectedJs);
    String actual = compiler.toSource(root).trim();
    String expected = compiler.toSource(expectedRoot).trim();
    assertEquals(expected, actual);
  }

  // Tests unsigned right shift of negative number (Defects4J 97b regression)
  @Test
  public void testTryFoldShift_unsignedRightShiftNegative_foldsToPositiveUnsigned() {
    test("x = -1 >>> 0", "x = 4294967295");
    test("x = -1 >>> 1", "x = 2147483647");
  }

  // Tests standard shift operators (LSH, RSH, URSH)
  @Test
  public void testTryFoldShift_standardShifts_foldsCorrectly() {
    test("x = 1 << 2", "x = 4");
    test("x = 8 >> 1", "x = 4");
    test("x = 8 >>> 1", "x = 4");
  }

  // Tests shift amount out of bounds
  @Test
  public void testTryFoldShift_amountOutOfBounds_doesNotFold() {
    test("x = 1 << 32", "x = 1 << 32");
    test("x = 1 << -1", "x = 1 << -1");
  }

  // Tests typeof operator folding for various literals
  @Test
  public void testTryFoldTypeof_literalTypes_foldsToString() {
    test("x = typeof 'abc'", "x = 'string'");
    test("x = typeof 123", "x = 'number'");
    test("x = typeof true", "x = 'boolean'");
    test("x = typeof false", "x = 'boolean'");
    test("x = typeof null", "x = 'object'");
    test("x = typeof undefined", "x = 'undefined'");
    test("x = typeof void 0", "x = 'undefined'");
    test("x = typeof {}", "x = 'object'");
    test("x = typeof []", "x = 'object'");
  }

  // Tests unary NOT, NEG, and BITNOT operations
  @Test
  public void testTryFoldUnaryOperator_validOperands_foldsCorrectly() {
    test("x = !true", "x = false");
    test("x = !false", "x = true");
    test("x = - -5", "x = 5");
    test("x = ~0", "x = -1");
    test("x = ~-1", "x = 0");
  }

  // Tests basic arithmetic operations (+, -, *, /)
  @Test
  public void testTryFoldArithmetic_standardOperations_foldsResults() {
    test("x = 10 + 20", "x = 30");
    test("x = 20 - 5", "x = 15");
    test("x = 4 * 5", "x = 20");
    test("x = 10 / 2", "x = 5");
  }

  // Tests divide by zero edge case
  @Test
  public void testTryFoldArithmetic_divideByZero_doesNotFold() {
    test("x = 10 / 0", "x = 10 / 0");
  }

  // Tests string concatenation with ADD
  @Test
  public void testTryFoldAdd_stringConcatenation_foldsString() {
    test("x = 'hello ' + 'world'", "x = 'hello world'");
    test("x = 'a' + 1", "x = 'a1'");
    test("x = 1 + 'a'", "x = '1a'");
    test("x = y + 'a' + 'b'", "x = y + 'ab'");
  }

  // Tests bitwise AND and OR operations
  @Test
  public void testTryFoldBitAndOr_numbers_foldsBits() {
    test("x = 1 & 3", "x = 1");
    test("x = 1 | 2", "x = 3");
  }

  // Tests comparison operators (==, ===, !=, !==, <, >)
  @Test
  public void testTryFoldComparison_literals_foldsToBoolean() {
    test("x = 1 === 1", "x = true");
    test("x = 1 === 2", "x = false");
    test("x = 'a' == 'a'", "x = true");
    test("x = 'a' == 'b'", "x = false");
    test("x = 5 > 3", "x = true");
    test("x = 5 < 3", "x = false");
    test("x = null == undefined", "x = true");
    test("x = null === undefined", "x = false");
  }

  // Tests String.indexOf and lastIndexOf folding
  @Test
  public void testTryFoldStringIndexOf_stringLiterals_foldsIndex() {
    test("x = 'abcdef'.indexOf('cd')", "x = 2");
    test("x = 'abcdef'.indexOf('z')", "x = -1");
    test("x = 'abcdefbc'.lastIndexOf('bc')", "x = 6");
  }

  // Tests Array.join folding
  @Test
  public void testTryFoldStringJoin_arrayLiteral_foldsJoinedString() {
    test("x = ['a', 'b', 'c'].join(',')", "x = 'a,b,c'");
    test("x = [].join(',')", "x = ''");
  }

  // Tests array element indexing
  @Test
  public void testTryFoldGetElem_arrayLiteral_foldsElement() {
    test("x = [1, 2, 3][0]", "x = 1");
    test("x = [1, 2, 3][2]", "x = 3");
    test("x = [1, 2, 3][5]", "x = [1, 2, 3][5]");
  }

  // Tests array and string length property access
  @Test
  public void testTryFoldGetProp_length_foldsToNumber() {
    test("x = [1, 2, 3].length", "x = 3");
    test("x = 'hello'.length", "x = 5");
  }

  // Tests logical AND / OR folding
  @Test
  public void testTryFoldAndOr_booleanOperands_foldsExpression() {
    test("x = true && 1", "x = 1");
    test("x = false && 1", "x = false");
    test("x = true || 1", "x = true");
    test("x = false || 1", "x = 1");
  }

  // Tests instanceof operator with immutable left operand
  @Test
  public void testTryFoldInstanceof_immutableOperands_foldsFalse() {
    test("x = 1 instanceof Object", "x = false");
    test("x = 'test' instanceof Object", "x = false");
  }

  // Tests modulo operation (%)
  @Test
  public void testTryFoldArithmetic_modulo_foldsRemainder() {
    test("x = 10 % 3", "x = 1");
    test("x = 10 % 0", "x = 10 % 0");
  }

  // Tests bitwise XOR operation (^)
  @Test
  public void testTryFoldBitXor_numbers_foldsBits() {
    test("x = 1 ^ 3", "x = 2");
    test("x = 7 ^ 7", "x = 0");
  }

  // Tests ternary/hook operator (?:)
  @Test
  public void testTryFoldHook_conditionKnown_foldsBranch() {
    test("x = true ? 1 : 2", "x = 1");
    test("x = false ? 1 : 2", "x = 2");
    test("x = 'truthy' ? 1 : 2", "x = 1");
    test("x = 0 ? 1 : 2", "x = 2");
  }

  // Tests string substr, substring, charAt, and charCodeAt folding
  @Test
  public void testTryFoldStringMethods_stringLiterals_foldsResult() {
    test("x = 'abcdef'.substr(1, 3)", "x = 'bcd'");
    test("x = 'abcdef'.substring(2, 5)", "x = 'cde'");
    test("x = 'abc'.charAt(1)", "x = 'b'");
    test("x = 'abc'.charCodeAt(0)", "x = 97");
  }

  // Tests addition associativity and precedence with strings and numbers
  @Test
  public void testTryFoldAdd_mixedAssociativity_foldsCorrectly() {
    test("x = 1 + 2 + 'a'", "x = '3a'");
    test("x = 'a' + 1 + 2", "x = 'a12'");
    test("x = 1 + 'a' + 2", "x = '1a2'");
  }

  // Tests unary NOT with various truthy and falsy values
  @Test
  public void testTryFoldUnaryOperator_truthyAndFalsyValues_foldsToBoolean() {
    test("x = !0", "x = true");
    test("x = !1", "x = false");
    test("x = !''", "x = true");
    test("x = !'abc'", "x = false");
    test("x = !null", "x = true");
    test("x = !undefined", "x = true");
    test("x = !{}", "x = false");
    test("x = ![]", "x = false");
  }

  // Tests relational comparison operators (<=, >=, !=, !==)
  @Test
  public void testTryFoldComparison_relationalAndInequality_foldsToBoolean() {
    test("x = 5 <= 5", "x = true");
    test("x = 5 >= 6", "x = false");
    test("x = 'a' < 'b'", "x = true");
    test("x = 'b' <= 'a'", "x = false");
    test("x = 1 != 2", "x = true");
    test("x = 1 !== 1", "x = false");
  }

  // Tests constructor folding (new Array(), new Object())
  @Test
  public void testTryFoldCtorCall_standardConstructors_foldsLiterals() {
    test("x = new Array()", "x = []");
    test("x = new Array(1, 2, 3)", "x = [1, 2, 3]");
    test("x = new Object()", "x = {}");
  }

  // Tests object literal property access folding
  @Test
  public void testTryFoldGetElem_objectLiteral_foldsProperty() {
    test("x = ({a: 1})['a']", "x = 1");
    test("x = ({'foo': 'bar'})['foo']", "x = 'bar'");
  }

  // Tests void operator folding
  @Test
  public void testTryFoldVoid_constantExpressions_foldsUndefined() {
    test("x = void 0", "x = void 0");
    test("x = void 'hello'", "x = void 0");
    test("x = void 123", "x = void 0");
  }
}