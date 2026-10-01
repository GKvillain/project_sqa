package com.google.javascript.jscomp;

import org.junit.Test;

public class PeepholeFoldConstantsTest extends CompilerTestCase {

  private boolean late = true;

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new PeepholeOptimizationsPass(compiler, new PeepholeFoldConstants(late));
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  // Tests array access as an assignment target should not fold (Regression test for Defect 161)
  @Test
  public void testOptimizeSubtree_arrayAccessAssignmentTarget_doesNotFold() {
    testSame("[][1] = 1;");
    testSame("[a, b][0] = 1;");
    testSame("[a, b][1] = 1;");
  }

  // Tests folding array element access
  @Test
  public void testOptimizeSubtree_arrayAccess_foldsElement() {
    test("x = [1, 2, 3][0]", "x = 1");
    test("x = [1, 2, 3][1]", "x = 2");
    test("x = [1, 2, 3][2]", "x = 3");
  }

  // Tests typeof operator folding on various literal types
  @Test
  public void testOptimizeSubtree_typeofLiterals_foldsToTypeName() {
    test("x = typeof 1", "x = \"number\"");
    test("x = typeof 'foo'", "x = \"string\"");
    test("x = typeof true", "x = \"boolean\"");
    test("x = typeof null", "x = \"object\"");
    test("x = typeof undefined", "x = \"undefined\"");
  }

  // Tests unary operators POS, NEG, NOT, and BITNOT
  @Test
  public void testOptimizeSubtree_unaryOps_foldsConstant() {
    test("x = - -3", "x = 3");
    test("x = ~0", "x = -1");
    test("x = !true", "x = false");
    test("x = !false", "x = true");
    test("x = +5", "x = 5");
  }

  // Tests binary arithmetic operators (+, -, *, /, %)
  @Test
  public void testOptimizeSubtree_binaryArithmeticOps_foldsNumbers() {
    test("x = 2 + 3", "x = 5");
    test("x = 10 - 4", "x = 6");
    test("x = 3 * 4", "x = 12");
    test("x = 10 / 2", "x = 5");
    test("x = 7 % 3", "x = 1");
  }

  // Tests bitwise shift operators (<<, >>, >>>)
  @Test
  public void testOptimizeSubtree_shiftOps_foldsCorrectResult() {
    test("x = 1 << 2", "x = 4");
    test("x = 8 >> 1", "x = 4");
    test("x = -1 >>> 0", "x = 4294967295");
  }

  // Tests string concatenation folding
  @Test
  public void testOptimizeSubtree_stringConcat_foldsStrings() {
    test("x = 'a' + 'b'", "x = \"ab\"");
    test("x = 'a' + 'b' + 'c'", "x = \"abc\"");
  }

  // Tests comparison operators (==, ===, !=, !==, <, <=, >, >=)
  @Test
  public void testOptimizeSubtree_comparisonOps_foldsToBoolean() {
    test("x = 1 < 2", "x = true");
    test("x = 2 <= 2", "x = true");
    test("x = 3 > 5", "x = false");
    test("x = 'a' === 'a'", "x = true");
    test("x = 'a' !== 'b'", "x = true");
    test("x = null === undefined", "x = false");
    test("x = null == undefined", "x = true");
  }

  // Tests logical AND/OR short-circuit folding
  @Test
  public void testOptimizeSubtree_logicalAndOr_foldsShortCircuit() {
    test("x = true && 1", "x = 1");
    test("x = false && 1", "x = false");
    test("x = true || 1", "x = true");
    test("x = false || 1", "x = 1");
  }

  // Tests instanceof operator folding for immutable literals and Object
  @Test
  public void testOptimizeSubtree_instanceof_foldsLiteral() {
    test("x = 'hello' instanceof Object", "x = false");
    test("x = 123 instanceof Object", "x = false");
    test("x = ({}) instanceof Object", "x = true");
  }

  // Tests folding of .length property on arrays and strings
  @Test
  public void testOptimizeSubtree_lengthProperty_foldsLength() {
    test("x = [1, 2, 3].length", "x = 3");
    test("x = 'hello'.length", "x = 5");
  }

  // Tests object property access folding
  @Test
  public void testOptimizeSubtree_objectPropAccess_foldsPropertyValue() {
    test("x = ({a: 1, b: 2}).a", "x = 1");
    test("x = ({a: 1, b: 2})['b']", "x = 2");
  }

  // Tests converting x = x + y to compound assignment x += y
  @Test
  public void testOptimizeSubtree_assignOp_convertsToCompoundAssign() {
    test("x = x + 1", "x += 1");
    test("x = x * 2", "x *= 2");
    test("x = x - 3", "x -= 3");
  }

  // Tests reducing void operator operands to 0
  @Test
  public void testOptimizeSubtree_voidOp_reducesToVoidZero() {
    test("x = void 1", "x = void 0");
    test("x = void 'hello'", "x = void 0");
  }

  // Tests bitwise operators (AND, OR, XOR)
  @Test
  public void testOptimizeSubtree_bitwiseOps_foldsBitwise() {
    test("x = 5 & 3", "x = 1");
    test("x = 5 | 3", "x = 7");
    test("x = 5 ^ 3", "x = 6");
  }

  // Tests ternary (hook) operator folding on constant conditions
  @Test
  public void testOptimizeSubtree_hook_foldsTernary() {
    test("x = true ? 1 : 2", "x = 1");
    test("x = false ? 1 : 2", "x = 2");
    test("x = 1 ? 'a' : 'b'", "x = \"a\"");
    test("x = 0 ? 'a' : 'b'", "x = \"b\"");
  }

  // Tests associative string and number concatenation
  @Test
  public void testOptimizeSubtree_associativeConcat_foldsPartialConstants() {
    test("x = 'a' + 'b' + y", "x = \"ab\" + y");
    test("x = 1 + 2 + y", "x = 3 + y");
    test("x = 'a' + 1", "x = \"a1\"");
    test("x = 1 + 'a'", "x = \"1a\"");
  }

  // Tests empty string and array length folding
  @Test
  public void testOptimizeSubtree_emptyLength_foldsToZero() {
    test("x = ''.length", "x = 0");
    test("x = [].length", "x = 0");
  }

  // Tests out of bounds array access should not fold
  @Test
  public void testOptimizeSubtree_arrayAccessOutOfBounds_doesNotFold() {
    testSame("x = [1, 2][-1];");
    testSame("x = [1, 2][5];");
  }
}