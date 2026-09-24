package com.google.javascript.jscomp;

import org.junit.Test;

/**
 * Unit tests for {@link FoldConstants}.
 */
public class FoldConstantsTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new FoldConstants(compiler);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // Tests string join with constant string elements
  @Test
  public void testStringJoin_constantElements_foldsToString() {
    test("x = ['a', 'b', 'c'].join('')", "x = 'abc'");
    test("x = ['a', 'b', 'c'].join(',')", "x = 'a,b,c'");
  }

  // Tests string join with non-constant elements
  @Test
  public void testStringJoin_mixedElements_foldsPartially() {
    test("x = [a, 'b', 'c'].join('')", "x = [a, 'bc'].join('')");
    test("x = ['a', 'b', a].join('')", "x = ['ab', a].join('')");
  }

  // Tests string join with empty array and single element
  @Test
  public void testStringJoin_boundaryArrays_foldsCorrectly() {
    test("x = [].join('')", "x = ''");
    test("x = ['a'].join('')", "x = 'a'");
    test("x = [a].join('')", "x = '' + a");
  }

  // Tests basic arithmetic operations folding
  @Test
  public void testArithmetic_basicOperations_folded() {
    test("x = 1 + 2", "x = 3");
    test("x = 5 - 2", "x = 3");
    test("x = 2 * 3", "x = 6");
    test("x = 6 / 2", "x = 3");
    test("x = 'a' + 'b'", "x = 'ab'");
  }

  // Tests divide by zero diagnostic error
  @Test
  public void testArithmetic_divideByZero_reportsError() {
    test("x = 1 / 0", "x = 1 / 0", null, FoldConstants.DIVIDE_BY_0_ERROR);
  }

  // Tests bitwise AND, OR, NOT, and shift operations
  @Test
  public void testBitwise_operations_folded() {
    test("x = 1 & 3", "x = 1");
    test("x = 1 | 2", "x = 3");
    test("x = ~1", "x = -2");
    test("x = 1 << 2", "x = 4");
    test("x = 8 >> 1", "x = 4");
    test("x = 8 >>> 1", "x = 4");
  }

  // Tests comparison operations for various literals
  @Test
  public void testComparison_variousLiterals_folded() {
    test("x = ('a' == 'a')", "x = true");
    test("x = ('a' == 'b')", "x = false");
    test("x = (1 < 2)", "x = true");
    test("x = (2 >= 3)", "x = false");
    test("x = (null == undefined)", "x = true");
    test("x = (null === undefined)", "x = false");
  }

  // Tests logical AND / OR folding with literal operands
  @Test
  public void testLogical_andOrFolding_folded() {
    test("x = true && foo()", "x = foo()");
    test("x = false && foo()", "x = false");
    test("x = true || foo()", "x = true");
    test("x = false || foo()", "x = foo()");
  }

  // Tests IF statements and hook expressions with literal conditions
  @Test
  public void testHookIf_constantCondition_folded() {
    test("if (true) { x = 1; }", "x = 1;");
    test("if (false) { x = 1; }", "");
    test("x = true ? 1 : 2", "x = 1");
    test("x = false ? 1 : 2", "x = 2");
  }

  // Tests typeof operator folding on literals
  @Test
  public void testTypeof_literals_folded() {
    test("x = typeof 'hello'", "x = 'string'");
    test("x = typeof 123", "x = 'number'");
    test("x = typeof true", "x = 'boolean'");
    test("x = typeof {}", "x = 'object'");
    test("x = typeof []", "x = 'object'");
    test("x = typeof undefined", "x = 'undefined'");
  }

  // Tests array element access with constant index
  @Test
  public void testGetElem_constantArrayIndex_folded() {
    test("x = [10, 20, 30][1]", "x = 20");
    test("x = ['a', 'b', 'c'][0]", "x = 'a'");
  }

  // Tests array and string length property access
  @Test
  public void testGetProp_lengthProperty_folded() {
    test("x = [1, 2, 3].length", "x = 3");
    test("x = 'hello'.length", "x = 5");
  }

  // Tests String.indexOf and String.lastIndexOf evaluation
  @Test
  public void testStringIndexOf_constantStrings_folded() {
    test("x = 'abcdef'.indexOf('cd')", "x = 2");
    test("x = 'abcdef'.indexOf('z')", "x = -1");
    test("x = 'abcdefbc'.indexOf('bc', 3)", "x = 6");
    test("x = 'abcdefbc'.lastIndexOf('bc')", "x = 6");
  }

  // Tests RegExp constructor folding to regex literal
  @Test
  public void testRegExp_constructorFolding_folded() {
    test("x = new RegExp('abc')", "x = /abc/");
    test("x = new RegExp('abc', 'i')", "x = /abc/i");
  }

  // Tests unary NOT minimization and literal negation
  @Test
  public void testUnary_notAndNeg_folded() {
    test("x = !true", "x = false");
    test("x = !false", "x = true");
    test("x = -5", "x = -5");
    test("x = -(-5)", "x = 5");
    test("x = !(a == b)", "x = a != b");
  }

  // Tests folding of loops with false conditions
  @Test
  public void testLoops_falseCondition_removed() {
    test("while (false) { x = 1; }", "");
    test("for (;false;) { x = 1; }", "");
  }

  // Tests left child string concatenation folding
  @Test
  public void testLeftChildAdd_stringConcatenation_folded() {
    test("x = foo() + 'a' + 'b'", "x = foo() + 'ab'");
  }
}