package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;
import static org.junit.Assert.*;

public class FoldConstantsTest {

  private String fold(String js) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    FoldConstants folder = new FoldConstants(compiler);
    folder.process(null, root);
    return compiler.toSource(root).trim();
  }

  // Tests string join with string literals folded into a single string literal
  @Test
  public void testTryFoldStringJoin_stringLiterals_foldedToMergedString() {
    String js = "var x = ['a', 'b', 'c'].join('');";
    String expected = "var x=\"abc\";";
    assertEquals(expected, fold(js));
  }

  // Tests string join with delimiter folded correctly
  @Test
  public void testTryFoldStringJoin_withDelimiter_foldedWithDelimiter() {
    String js = "var x = ['foo', 'bar'].join(',');";
    String expected = "var x=\"foo,bar\";";
    assertEquals(expected, fold(js));
  }

  // Tests string join with empty array literal folded to empty string
  @Test
  public void testTryFoldStringJoin_emptyArray_foldedToEmptyString() {
    String js = "var x = [].join(',');";
    String expected = "var x=\"\";";
    assertEquals(expected, fold(js));
  }

  // Tests string join with single element array
  @Test
  public void testTryFoldStringJoin_singleElement_foldedToSingleString() {
    String js = "var x = ['hello'].join('');";
    String expected = "var x=\"hello\";";
    assertEquals(expected, fold(js));
  }

  // Tests string join with mix of variables and string literals
  @Test
  public void testTryFoldStringJoin_mixedLiteralsAndVars_partiallyFolded() {
    String js = "var x = [a, 'b', 'c'].join('');";
    String expected = "var x=[a,\"bc\"].join(\"\");";
    assertEquals(expected, fold(js));
  }

  // Tests typeof operator folding with primitive literals
  @Test
  public void testTypeof_literalOperands_foldedToTypeString() {
    assertEquals("var a=\"string\";", fold("var a = typeof 'abc';"));
    assertEquals("var a=\"number\";", fold("var a = typeof 123;"));
    assertEquals("var a=\"boolean\";", fold("var a = typeof true;"));
    assertEquals("var a=\"object\";", fold("var a = typeof null;"));
    assertEquals("var a=\"undefined\";", fold("var a = typeof undefined;"));
  }

  // Tests unary NOT with boolean and numeric literals
  @Test
  public void testNot_literalValues_foldedToBoolean() {
    assertEquals("var a=false;", fold("var a = !true;"));
    assertEquals("var a=true;", fold("var a = !false;"));
    assertEquals("var a=true;", fold("var a = !0;"));
    assertEquals("var a=false;", fold("var a = !1;"));
  }

  // Tests minimization of !(x == y) to x != y and !(x === y) to x !== y
  @Test
  public void testTryMinimizeNot_equalityComparisons_invertedOperator() {
    assertEquals("var a=x!=y;", fold("var a = !(x == y);"));
    assertEquals("var a=x!==y;", fold("var a = !(x === y);"));
    assertEquals("var a=x==y;", fold("var a = !(x != y);"));
    assertEquals("var a=x===y;", fold("var a = !(x !== y);"));
  }

  // Tests unary negation and bitwise NOT
  @Test
  public void testUnaryNegAndBitwiseNot_numericLiterals_folded() {
    assertEquals("var a=-5;", fold("var a = -5;"));
    assertEquals("var a=-1;", fold("var a = ~0;"));
    assertEquals("var a=0;", fold("var a = ~(-1);"));
  }

  // Tests basic arithmetic binary operations (+, -, *, /)
  @Test
  public void testArithmetic_constantExpressions_foldedToResult() {
    assertEquals("var a=7;", fold("var a = 3 + 4;"));
    assertEquals("var a=5;", fold("var a = 10 - 5;"));
    assertEquals("var a=24;", fold("var a = 6 * 4;"));
    assertEquals("var a=2.5;", fold("var a = 5 / 2;"));
    assertEquals("var a=\"foobar\";", fold("var a = 'foo' + 'bar';"));
  }

  // Tests bitwise AND, OR, and Shift operations
  @Test
  public void testBitwiseAndShift_integerLiterals_folded() {
    assertEquals("var a=1;", fold("var a = 1 & 3;"));
    assertEquals("var a=3;", fold("var a = 1 | 2;"));
    assertEquals("var a=4;", fold("var a = 1 << 2;"));
    assertEquals("var a=2;", fold("var a = 8 >> 2;"));
    assertEquals("var a=2;", fold("var a = 8 >>> 2;"));
  }

  // Tests comparison operations (<, >, <=, >=, ==, !=)
  @Test
  public void testComparison_constantLiterals_foldedToBoolean() {
    assertEquals("var a=true;", fold("var a = 1 < 2;"));
    assertEquals("var a=false;", fold("var a = 2 > 3;"));
    assertEquals("var a=true;", fold("var a = 'a' == 'a';"));
    assertEquals("var a=false;", fold("var a = 'a' == 'b';"));
    assertEquals("var a=true;", fold("var a = null == undefined;"));
  }

  // Tests folding of String.prototype.indexOf and lastIndexOf
  @Test
  public void testStringIndexOf_stringConstants_foldedToNumber() {
    assertEquals("var a=2;", fold("var a = 'abcdef'.indexOf('cd');"));
    assertEquals("var a=-1;", fold("var a = 'abcdef'.indexOf('z');"));
    assertEquals("var a=6;", fold("var a = 'abcdefbc'.indexOf('bc', 3);"));
    assertEquals("var a=2;", fold("var a = 'abcdef'.lastIndexOf('cd');"));
  }

  // Tests array element access with constant index
  @Test
  public void testGetElem_arrayLiteralWithConstantIndex_foldedToElement() {
    assertEquals("var a=2;", fold("var a = [1, 2, 3][1];"));
    assertEquals("var a=\"b\";", fold("var a = ['a', 'b', 'c'][1];"));
  }

  // Tests array length and string length property access
  @Test
  public void testGetProp_lengthProperty_foldedToLengthValue() {
    assertEquals("var a=3;", fold("var a = [1, 2, 3].length;"));
    assertEquals("var a=5;", fold("var a = 'hello'.length;"));
  }

  // Tests hook (ternary) and if condition folding with constant condition
  @Test
  public void testHookAndIf_constantCondition_branchFolded() {
    assertEquals("var a=1;", fold("var a = true ? 1 : 2;"));
    assertEquals("var a=2;", fold("var a = false ? 1 : 2;"));
    assertEquals("var a=1;", fold("if (true) { var a = 1; }"));
    assertEquals("", fold("if (false) { var a = 1; }"));
  }

  // Tests short-circuit logical AND (&&) and OR (||)
  @Test
  public void testAndOr_booleanLiterals_shortCircuited() {
    assertEquals("var a=x;", fold("var a = true && x;"));
    assertEquals("var a=false;", fold("var a = false && x;"));
    assertEquals("var a=true;", fold("var a = true || x;"));
    assertEquals("var a=x;", fold("var a = false || x;"));
  }

  // Tests assignment conversion x = x + y into x += y
  @Test
  public void testAssign_compoundAssignment_foldedToCompoundOp() {
    assertEquals("x+=y;", fold("x = x + y;"));
    assertEquals("x-=y;", fold("x = x - y;"));
    assertEquals("x*=y;", fold("x = x * y;"));
    assertEquals("x/=y;", fold("x = x / y;"));
  }

  // Tests folding of new RegExp constructor to regex literal
  @Test
  public void testRegExpConstructor_stringLiteral_foldedToRegExpLiteral() {
    assertEquals("var a=/abc/;", fold("var a = new RegExp('abc');"));
    assertEquals("var a=/abc/i;", fold("var a = new RegExp('abc', 'i');"));
  }

  // Tests reduction of 'return undefined' to 'return'
  @Test
  public void testReduceReturn_undefined_reducedToEmptyReturn() {
    assertEquals("function f(){return}", fold("function f() { return undefined; }"));
    assertEquals("function f(){return}", fold("function f() { return void 0; }"));
  }

  // Tests bitwise XOR (^) operation folding
  @Test
  public void testBitwiseXor_constantLiterals_foldedToResult() {
    assertEquals("var a=3;", fold("var a = 1 ^ 2;"));
    assertEquals("var a=0;", fold("var a = 5 ^ 5;"));
  }

  // Tests modulo (%) operation folding
  @Test
  public void testModulo_constantLiterals_foldedToResult() {
    assertEquals("var a=1;", fold("var a = 7 % 3;"));
    assertEquals("var a=0;", fold("var a = 8 % 4;"));
  }

  // Tests string built-in methods: substr, substring, charAt, charCodeAt
  @Test
  public void testStringMethods_constantLiterals_foldedToResult() {
    assertEquals("var a=\"bc\";", fold("var a = 'abcdef'.substring(1, 3);"));
    assertEquals("var a=\"bc\";", fold("var a = 'abcdef'.substr(1, 2);"));
    assertEquals("var a=\"b\";", fold("var a = 'abcdef'.charAt(1);"));
    assertEquals("var a=98;", fold("var a = 'abcdef'.charCodeAt(1);"));
  }

  // Tests string case conversion methods: toLowerCase, toUpperCase
  @Test
  public void testStringCaseMethods_constantLiterals_foldedToResult() {
    assertEquals("var a=\"abc\";", fold("var a = 'ABC'.toLowerCase();"));
    assertEquals("var a=\"ABC\";", fold("var a = 'abc'.toUpperCase();"));
  }

  // Tests string split method with constant string
  @Test
  public void testStringSplit_constantLiterals_foldedToArray() {
    assertEquals("var a=[\"a\",\"b\",\"c\"];", fold("var a = 'a,b,c'.split(',');"));
    assertEquals("var a=[\"hello\"];", fold("var a = 'hello'.split(',');"));
  }

  // Tests Math built-in constant methods
  @Test
  public void testMathMethods_constantLiterals_foldedToResult() {
    assertEquals("var a=5;", fold("var a = Math.abs(-5);"));
    assertEquals("var a=3;", fold("var a = Math.max(1, 3);"));
    assertEquals("var a=1;", fold("var a = Math.min(1, 3);"));
    assertEquals("var a=3;", fold("var a = Math.floor(3.7);"));
    assertEquals("var a=4;", fold("var a = Math.ceil(3.2);"));
    assertEquals("var a=4;", fold("var a = Math.round(3.6);"));
  }

  // Tests parseInt and parseFloat constant folding
  @Test
  public void testParseIntAndParseFloat_constantLiterals_foldedToResult() {
    assertEquals("var a=123;", fold("var a = parseInt('123');"));
    assertEquals("var a=10;", fold("var a = parseInt('1010', 2);"));
    assertEquals("var a=12.5;", fold("var a = parseFloat('12.5');"));
  }

  // Tests strict equality (===) and inequality (!==) comparisons with constants
  @Test
  public void testStrictComparison_constantLiterals_foldedToBoolean() {
    assertEquals("var a=true;", fold("var a = 'a' === 'a';"));
    assertEquals("var a=false;", fold("var a = 'a' === 'b';"));
    assertEquals("var a=false;", fold("var a = '1' === 1;"));
    assertEquals("var a=true;", fold("var a = '1' !== 1;"));
  }

  // Tests chained constant addition associativity
  @Test
  public void testChainedAddition_mixedConstants_foldedCorrectly() {
    assertEquals("var a=\"3a\";", fold("var a = 1 + 2 + 'a';"));
    assertEquals("var a=\"a12\";", fold("var a = 'a' + 1 + 2;"));
  }

  // Tests void operator folding
  @Test
  public void testVoid_constantExpression_foldedToVoidZero() {
    assertEquals("var a=void 0;", fold("var a = void 'hello';"));
    assertEquals("var a=void 0;", fold("var a = void 123;"));
  }

  // Tests array method folding: slice, concat
  @Test
  public void testArrayMethods_constantArrays_foldedToResult() {
    assertEquals("var a=[\"b\",\"c\"];", fold("var a = ['a', 'b', 'c', 'd'].slice(1, 3);"));
    assertEquals("var a=[1,2,3,4];", fold("var a = [1, 2].concat([3, 4]);"));
  }

  // Tests NaN comparisons
  @Test
  public void testNaNComparison_foldedToBoolean() {
    assertEquals("var a=false;", fold("var a = NaN == NaN;"));
    assertEquals("var a=true;", fold("var a = NaN != NaN;"));
    assertEquals("var a=false;", fold("var a = NaN === NaN;"));
    assertEquals("var a=true;", fold("var a = NaN !== NaN;"));
  }
}