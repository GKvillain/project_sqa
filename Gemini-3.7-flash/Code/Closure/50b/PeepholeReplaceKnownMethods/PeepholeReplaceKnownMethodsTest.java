package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

public class PeepholeReplaceKnownMethodsTest extends CompilerTestCase {

  @Override
  protected CompilerPass createProcessor(final Compiler compiler) {
    return new PeepholeOptimizationsPass(compiler,
        new PeepholeReplaceKnownMethods());
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    enableNormalize();
  }

  // Tests string toLowerCase folding
  @Test
  public void testStringToLowerCase_constantString_returnsLoweredString() {
    test("x = 'ABC'.toLowerCase()", "x = 'abc'");
    test("x = 'Hello World!'.toLowerCase()", "x = 'hello world!'");
    testSame("x = y.toLowerCase()");
  }

  // Tests string toUpperCase folding
  @Test
  public void testStringToUpperCase_constantString_returnsUpperedString() {
    test("x = 'abc'.toUpperCase()", "x = 'ABC'");
    test("x = 'Hello World!'.toUpperCase()", "x = 'HELLO WORLD!'");
    testSame("x = y.toUpperCase()");
  }

  // Tests string indexOf folding
  @Test
  public void testStringIndexOf_validPatterns_returnsIndex() {
    test("x = 'abcdef'.indexOf('c')", "x = 2");
    test("x = 'abcdef'.indexOf('z')", "x = -1");
    test("x = 'banana'.indexOf('a', 2)", "x = 3");
    testSame("x = 'abcdef'.indexOf(y)");
  }

  // Tests string lastIndexOf folding
  @Test
  public void testStringLastIndexOf_validPatterns_returnsIndex() {
    test("x = 'banana'.lastIndexOf('a')", "x = 5");
    test("x = 'banana'.lastIndexOf('a', 4)", "x = 3");
    test("x = 'abcdef'.lastIndexOf('z')", "x = -1");
    testSame("x = 'banana'.lastIndexOf(y)");
  }

  // Tests string substring folding
  @Test
  public void testStringSubstring_validRanges_returnsSubstring() {
    test("x = 'abcdef'.substring(1, 4)", "x = 'bcd'");
    test("x = 'abcdef'.substring(2)", "x = 'cdef'");
    testSame("x = 'abcdef'.substring(-1, 3)");
    testSame("x = 'abcdef'.substring(1, 10)");
  }

  // Tests string substr folding
  @Test
  public void testStringSubstr_validRanges_returnsSubstr() {
    test("x = 'abcdef'.substr(1, 3)", "x = 'bcd'");
    test("x = 'abcdef'.substr(2)", "x = 'cdef'");
    testSame("x = 'abcdef'.substr(-1, 3)");
    testSame("x = 'abcdef'.substr(2, 10)");
  }

  // Tests string charAt folding
  @Test
  public void testStringCharAt_validAndInvalidIndex_foldsWhenInBounds() {
    test("x = 'abc'.charAt(0)", "x = 'a'");
    test("x = 'abc'.charAt(2)", "x = 'c'");
    testSame("x = 'abc'.charAt(-1)");
    testSame("x = 'abc'.charAt(3)");
  }

  // Tests string charCodeAt folding
  @Test
  public void testStringCharCodeAt_validAndInvalidIndex_foldsWhenInBounds() {
    test("x = 'abc'.charCodeAt(0)", "x = 97");
    test("x = 'abc'.charCodeAt(1)", "x = 98");
    testSame("x = 'abc'.charCodeAt(-1)");
    testSame("x = 'abc'.charCodeAt(3)");
  }

  // Tests parseInt folding
  @Test
  public void testParseInt_validInputs_returnsParsedInteger() {
    test("x = parseInt('123')", "x = 123");
    test("x = parseInt('123', 10)", "x = 123");
    test("x = parseInt('0xa', 16)", "x = 10");
    test("x = parseInt('010', 10)", "x = 10");
    test("x = parseInt(123)", "x = 123");
    testSame("x = parseInt('invalid')");
    testSame("x = parseInt('123', 37)");
  }

  // Tests parseFloat folding
  @Test
  public void testParseFloat_validInputs_returnsParsedFloat() {
    test("x = parseFloat('1.25')", "x = 1.25");
    test("x = parseFloat('123')", "x = 123");
    test("x = parseFloat(1.25)", "x = 1.25");
    testSame("x = parseFloat('invalid')");
  }

  // Tests array join with string constants
  @Test
  public void testArrayJoin_stringLiterals_foldsToString() {
    test("x = ['a', 'b', 'c'].join('')", "x = 'abc'");
    test("x = ['a', 'b', 'c'].join(',')", "x = 'a,b,c'");
    test("x = ['a', 'b', 'c'].join()", "x = 'a,b,c'");
    test("x = [].join()", "x = ''");
  }

  // Tests array join with numeric and mixed literals
  @Test
  public void testArrayJoin_mixedLiterals_foldsToString() {
    test("x = [1, 2, 3].join('')", "x = '123'");
    test("x = [1, 2, 3].join(',')", "x = '1,2,3'");
    test("x = [1, 'a', 2].join('-')", "x = '1-a-2'");
  }

  // Tests array join with variables and partial folding (Defects4J Closure-50 regression target)
  @Test
  public void testArrayJoin_withNonLiterals_foldsAdjacentLiterals() {
    test("x = [a, 'b', 'c'].join('')", "x = [a, 'bc'].join('')");
    test("x = [a, 'b', 'c'].join(',')", "x = [a, 'b,c'].join(',')");
    test("x = ['a', 'b', a].join('')", "x = ['ab', a].join('')");
    test("x = [a, 'b', 'c', d].join('')", "x = [a, 'bc', d].join('')");
    testSame("x = [a, b].join('')");
  }

  // Tests array join when single non-string element needs string coercion
  @Test
  public void testArrayJoin_singleNonStringElement_coercesToString() {
    test("x = [a].join('')", "x = '' + a");
    test("x = [1].join('')", "x = '1'");
  }

  // Tests string split folding
  @Test
  public void testStringSplit_constantStrings_foldsToArray() {
    test("x = 'a,b,c'.split(',')", "x = ['a', 'b', 'c']");
    test("x = 'hello'.split('')", "x = ['h', 'e', 'l', 'l', 'o']");
    test("x = 'a,b,c'.split(',', 2)", "x = ['a', 'b']");
    test("x = 'abc'.split()", "x = ['abc']");
    testSame("x = y.split(',')");
    testSame("x = 'a,b,c'.split(y)");
  }

  // Tests string substring with swapped start/end indices
  @Test
  public void testStringSubstring_swappedIndices_normalizesAndFolds() {
    test("x = 'abcdef'.substring(4, 1)", "x = 'bcd'");
    test("x = 'abcdef'.substring(0, 0)", "x = ''");
    test("x = 'abcdef'.substring(2, 2)", "x = ''");
  }

  // Tests string substr with 0 length or single argument
  @Test
  public void testStringSubstr_zeroLengthAndSingleArg_foldsProperly() {
    test("x = 'abcdef'.substr(2, 0)", "x = ''");
    test("x = 'abcdef'.substr(0, 3)", "x = 'abc'");
  }

  // Tests charAt and charCodeAt default index (index 0 when omitted)
  @Test
  public void testStringCharAtAndCharCodeAt_noArguments_defaultsToZero() {
    test("x = 'abc'.charAt()", "x = 'a'");
    test("x = 'abc'.charCodeAt()", "x = 97");
  }

  // Tests string indexOf and lastIndexOf with empty search string and edge cases
  @Test
  public void testStringIndexOfAndLastIndexOf_edgeCases_foldsCorrectly() {
    test("x = 'abcdef'.indexOf('')", "x = 0");
    test("x = 'abcdef'.indexOf('', 2)", "x = 2");
    test("x = 'abcdef'.lastIndexOf('')", "x = 6");
    test("x = 'abcdef'.indexOf('cd')", "x = 2");
    test("x = 'abcdef'.lastIndexOf('cd')", "x = 2");
  }

  // Tests parseInt with various radices, prefixes, and whitespace
  @Test
  public void testParseInt_radixAndFormatVariations_foldsCorrectly() {
    test("x = parseInt(' 123 ')", "x = 123");
    test("x = parseInt('1010', 2)", "x = 10");
    test("x = parseInt('12', 8)", "x = 10");
    test("x = parseInt('z', 36)", "x = 35");
    test("x = parseInt('123', 0)", "x = 123");
    test("x = parseInt('0x1f')", "x = 31");
    test("x = parseInt('123abc')", "x = 123");
  }

  // Tests parseFloat with leading/trailing whitespace and scientific notation
  @Test
  public void testParseFloat_variousFormats_foldsCorrectly() {
    test("x = parseFloat('  1.5  ')", "x = 1.5");
    test("x = parseFloat('.5')", "x = 0.5");
    test("x = parseFloat('1e2')", "x = 100");
    test("x = parseFloat('1.25abc')", "x = 1.25");
  }

  // Tests array join with null, undefined, booleans, and nested literals
  @Test
  public void testArrayJoin_nullUndefinedAndBooleans_foldsCorrectly() {
    test("x = [null, undefined].join('')", "x = ''");
    test("x = [null, 'a', undefined].join(',')", "x = ',a,'");
    test("x = [true, false].join('')", "x = 'truefalse'");
    test("x = [true, false].join(',')", "x = 'true,false'");
    test("x = [].join('-')", "x = ''");
  }

  // Tests array join with call expression side effects
  @Test
  public void testArrayJoin_withSideEffects_preservesSideEffectsOrder() {
    test("x = [foo(), 'a', 'b'].join('')", "x = [foo(), 'ab'].join('')");
    test("x = ['a', 'b', foo()].join('')", "x = ['ab', foo()].join('')");
  }
}