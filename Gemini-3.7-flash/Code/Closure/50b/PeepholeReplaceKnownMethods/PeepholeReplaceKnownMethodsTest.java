package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link PeepholeReplaceKnownMethods}.
 */
public class PeepholeReplaceKnownMethodsTest extends CompilerTestCase {

  public PeepholeReplaceKnownMethodsTest() {
    super();
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    enableNormalize();
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new PeepholeOptimizationsPass(compiler, new PeepholeReplaceKnownMethods());
  }

  // Tests folding of empty and single-element array join
  @Test
  public void testFoldArrayJoin_emptyAndSingleElement_foldsCorrectly() {
    test("x = [].join()", "x = \"\"");
    test("x = [].join(\",\")", "x = \"\"");
    test("x = [].join(\"\")", "x = \"\"");
    test("x = [\"a\"].join()", "x = \"a\"");
    test("x = [\"a\"].join(\",\")", "x = \"a\"");
    test("x = [\"a\"].join(\"\")", "x = \"a\"");
  }

  // Tests folding of multi-element array join with default and custom separators
  @Test
  public void testFoldArrayJoin_multipleConstants_foldsToString() {
    test("x = [\"a\", \"b\", \"c\"].join()", "x = \"a,b,c\"");
    test("x = [\"a\", \"b\", \"c\"].join(\"\")", "x = \"abc\"");
    test("x = [\"a\", \"b\", \"c\"].join(\",\")", "x = \"a,b,c\"");
    test("x = [\"a\", \"b\", \"c\"].join(\"-\")", "x = \"a-b-c\"");
    test("x = [1, 2, 3].join(\"\")", "x = \"123\"");
    test("x = [1, 2, 3].join(\",\")", "x = \"1,2,3\"");
  }

  // Tests array join with non-literal or dynamic elements
  @Test
  public void testFoldArrayJoin_dynamicElements_preservesOrPartiallyFolds() {
    test("x = [a, \"b\", \"c\"].join(\"\")", "x = [a, \"bc\"].join(\"\")");
    test("x = [\"a\", \"b\", a].join(\"\")", "x = [\"ab\", a].join(\"\")");
    testSame("x = [a, b, c].join(\"\")");
    testSame("x = [\"a\", \"b\"].join(sep)");
  }

  // Tests String.prototype.indexOf and lastIndexOf folding
  @Test
  public void testFoldStringIndexOf_constants_foldsToInteger() {
    test("x = \"abcdef\".indexOf(\"c\")", "x = 2");
    test("x = \"abcdef\".indexOf(\"z\")", "x = -1");
    test("x = \"abcdef\".indexOf(\"c\", 1)", "x = 2");
    test("x = \"abcdef\".indexOf(\"c\", 3)", "x = -1");
    test("x = \"abcdefbc\".lastIndexOf(\"bc\")", "x = 6");
    test("x = \"abcdefbc\".lastIndexOf(\"bc\", 3)", "x = 1");
  }

  // Tests String.prototype.substr folding
  @Test
  public void testFoldStringSubstr_validIndices_foldsToString() {
    test("x = \"abcdef\".substr(2)", "x = \"cdef\"");
    test("x = \"abcdef\".substr(2, 3)", "x = \"cde\"");
    test("x = \"abcdef\".substr(0, 6)", "x = \"abcdef\"");
    testSame("x = \"abcdef\".substr(-1)");
    testSame("x = \"abcdef\".substr(2, -1)");
    testSame("x = \"abcdef\".substr(2, 10)");
  }

  // Tests String.prototype.substring folding
  @Test
  public void testFoldStringSubstring_validIndices_foldsToString() {
    test("x = \"abcdef\".substring(2)", "x = \"cdef\"");
    test("x = \"abcdef\".substring(2, 4)", "x = \"cd\"");
    test("x = \"abcdef\".substring(0, 6)", "x = \"abcdef\"");
    testSame("x = \"abcdef\".substring(-1)");
    testSame("x = \"abcdef\".substring(4, 2)");
    testSame("x = \"abcdef\".substring(2, 10)");
  }

  // Tests String.prototype.charAt and charCodeAt folding
  @Test
  public void testFoldStringCharAtAndCharCodeAt_validIndex_foldsToResult() {
    test("x = \"abcdef\".charAt(0)", "x = \"a\"");
    test("x = \"abcdef\".charAt(2)", "x = \"c\"");
    testSame("x = \"abcdef\".charAt(-1)");
    testSame("x = \"abcdef\".charAt(6)");
    test("x = \"abc\".charCodeAt(0)", "x = 97");
    test("x = \"abc\".charCodeAt(1)", "x = 98");
    testSame("x = \"abc\".charCodeAt(-1)");
    testSame("x = \"abc\".charCodeAt(3)");
  }

  // Tests String.prototype.toLowerCase and toUpperCase folding
  @Test
  public void testFoldStringToLowerCaseAndUpperCase_stringLiterals_foldsCases() {
    test("x = \"aBcDeF\".toLowerCase()", "x = \"abcdef\"");
    test("x = \"aBcDeF\".toUpperCase()", "x = \"ABCDEF\"");
    test("x = \"123$%^\".toLowerCase()", "x = \"123$%^\"");
    test("x = \"\".toLowerCase()", "x = \"\"");
    test("x = \"\".toUpperCase()", "x = \"\"");
  }

  // Tests parseInt folding with valid numeric string and radix
  @Test
  public void testFoldParseInt_validRadixAndStrings_foldsToInteger() {
    test("x = parseInt(\"123\")", "x = 123");
    test("x = parseInt(\"123\", 10)", "x = 123");
    test("x = parseInt(\"  123  \")", "x = 123");
    test("x = parseInt(\"0xA\", 16)", "x = 10");
    test("x = parseInt(\"0xa\")", "x = 10");
    test("x = parseInt(123)", "x = 123");
    test("x = parseInt(123, 10)", "x = 123");
    testSame("x = parseInt(\"123\", 37)");
    testSame("x = parseInt(\"123\", 1)");
    testSame("x = parseInt(\"abc\")");
  }

  // Tests parseFloat folding with floating point string literals
  @Test
  public void testFoldParseFloat_validFloatingStrings_foldsToNumber() {
    test("x = parseFloat(\"1.25\")", "x = 1.25");
    test("x = parseFloat(\"0.5\")", "x = 0.5");
    test("x = parseFloat(\"123\")", "x = 123");
    test("x = parseFloat(1.25)", "x = 1.25");
    testSame("x = parseFloat(\"abc\")");
    testSame("x = parseFloat(\"1.25foo\")");
  }
}