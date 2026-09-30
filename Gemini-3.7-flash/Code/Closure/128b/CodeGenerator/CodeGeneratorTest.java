package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import static org.junit.Assert.*;

public class CodeGeneratorTest {

  // Tests single digit zero (Defects4J Closure-128 defect)
  @Test
  public void testIsSimpleNumber_zero_returnsTrue() {
    assertTrue(CodeGenerator.isSimpleNumber("0"));
  }

  // Tests single non-zero digit
  @Test
  public void testIsSimpleNumber_singleDigit_returnsTrue() {
    assertTrue(CodeGenerator.isSimpleNumber("7"));
  }

  // Tests multiple valid digits
  @Test
  public void testIsSimpleNumber_multiDigit_returnsTrue() {
    assertTrue(CodeGenerator.isSimpleNumber("123456789"));
  }

  // Tests string with leading zero and multiple digits
  @Test
  public void testIsSimpleNumber_leadingZero_returnsFalse() {
    assertFalse(CodeGenerator.isSimpleNumber("0123"));
  }

  // Tests double zero
  @Test
  public void testIsSimpleNumber_doubleZero_returnsFalse() {
    assertFalse(CodeGenerator.isSimpleNumber("00"));
  }

  // Tests empty string boundary
  @Test
  public void testIsSimpleNumber_emptyString_returnsFalse() {
    assertFalse(CodeGenerator.isSimpleNumber(""));
  }

  // Tests string containing non-digit characters
  @Test
  public void testIsSimpleNumber_nonDigits_returnsFalse() {
    assertFalse(CodeGenerator.isSimpleNumber("123a"));
    assertFalse(CodeGenerator.isSimpleNumber("-5"));
    assertFalse(CodeGenerator.isSimpleNumber("1.5"));
  }

  // Tests getSimpleNumber with zero (Defects4J Closure-128 defect)
  @Test
  public void testGetSimpleNumber_zero_returnsZero() {
    assertEquals(0.0, CodeGenerator.getSimpleNumber("0"), 0.0);
  }

  // Tests getSimpleNumber with valid positive integer
  @Test
  public void testGetSimpleNumber_validInteger_returnsParsedValue() {
    assertEquals(123.0, CodeGenerator.getSimpleNumber("123"), 0.0);
  }

  // Tests getSimpleNumber with leading zero returning NaN
  @Test
  public void testGetSimpleNumber_leadingZero_returnsNaN() {
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("05")));
  }

  // Tests getSimpleNumber with numeric overflow
  @Test
  public void testGetSimpleNumber_overflowNumber_returnsNaN() {
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("9999999999999999999999999999999999999999")));
  }

  // Tests getSimpleNumber with non-numeric string
  @Test
  public void testGetSimpleNumber_nonNumeric_returnsNaN() {
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("abc")));
  }

  // Tests identifierEscape with valid Latin string
  @Test
  public void testIdentifierEscape_latinIdentifier_returnsSameString() {
    assertEquals("validVar_123", CodeGenerator.identifierEscape("validVar_123"));
  }

  // Tests identifierEscape with non-Latin Unicode characters
  @Test
  public void testIdentifierEscape_unicodeCharacter_returnsHexEscaped() {
    assertEquals("\\u00e9", CodeGenerator.identifierEscape("\u00e9"));
  }

  // Tests escaping double-quoted JavaScript strings with quotes and newlines
  @Test
  public void testEscapeToDoubleQuotedJsString_quotesAndControlChars_escapesProperly() {
    CodeGenerator cg = CodeGenerator.forCostEstimation(null);
    assertEquals("\"hello\\\"\\nworld\"", cg.escapeToDoubleQuotedJsString("hello\"\nworld"));
    assertEquals("\"\\t\\r\\b\\f\\\\\"", cg.escapeToDoubleQuotedJsString("\t\r\b\f\\"));
  }

  // Tests escaping script closing tags and HTML comments in JS strings
  @Test
  public void testEscapeToDoubleQuotedJsString_htmlTagsAndLineTerminators_escapesProperly() {
    CodeGenerator cg = CodeGenerator.forCostEstimation(null);
    assertEquals("\"\\x3c/script>\"", cg.escapeToDoubleQuotedJsString("</script>"));
    assertEquals("\"\\x3c!--\\u2028\\u2029\"", cg.escapeToDoubleQuotedJsString("<!--\u2028\u2029"));
  }

  // Tests RegExp escaping with forward slashes and control characters
  @Test
  public void testRegexpEscape_regexString_escapesProperly() {
    CodeGenerator cg = CodeGenerator.forCostEstimation(null);
    assertEquals("/a\\/b/", cg.regexpEscape("a/b"));
    assertEquals("/\\n\\t/", cg.regexpEscape("\n\t"));
  }

  // Tests isRepresentableAsInt with various integer and floating point values
  @Test
  public void testIsRepresentableAsInt() {
    assertTrue(CodeGenerator.isRepresentableAsInt(0.0));
    assertTrue(CodeGenerator.isRepresentableAsInt(1.0));
    assertTrue(CodeGenerator.isRepresentableAsInt(-100.0));
    assertTrue(CodeGenerator.isRepresentableAsInt(Integer.MAX_VALUE));
    assertTrue(CodeGenerator.isRepresentableAsInt(Integer.MIN_VALUE));

    assertFalse(CodeGenerator.isRepresentableAsInt(-0.0));
    assertFalse(CodeGenerator.isRepresentableAsInt(1.5));
    assertFalse(CodeGenerator.isRepresentableAsInt(Double.NaN));
    assertFalse(CodeGenerator.isRepresentableAsInt(Double.POSITIVE_INFINITY));
    assertFalse(CodeGenerator.isRepresentableAsInt(Double.NEGATIVE_INFINITY));
    assertFalse(CodeGenerator.isRepresentableAsInt((double) Integer.MAX_VALUE + 1.0));
    assertFalse(CodeGenerator.isRepresentableAsInt((double) Integer.MIN_VALUE - 1.0));
  }

  // Tests strEscape with single quote preference and CDATA / comment sequences
  @Test
  public void testStrEscape_variousDelimitersAndCdata() {
    assertEquals("'foo\\\'bar'", CodeGenerator.strEscape("foo'bar", '\'', "\\\"", "\\\'", "\\\\", null));
    assertEquals("\"foo\\\"bar\"", CodeGenerator.strEscape("foo\"bar", '"', "\\\"", "\\\'", "\\\\", null));
    assertEquals("\"foo]]\\>bar\"", CodeGenerator.strEscape("foo]]>bar", '"', "\\\"", "\\\'", "\\\\", null));
    assertEquals("\"foo--\\>bar\"", CodeGenerator.strEscape("foo-->bar", '"', "\\\"", "\\\'", "\\\\", null));
    assertEquals("\"\\x00123\"", CodeGenerator.strEscape("\0" + "123", '"', "\\\"", "\\\'", "\\\\", null));
    assertEquals("\"\\0\"", CodeGenerator.strEscape("\0", '"', "\\\"", "\\\'", "\\\\", null));
  }

  // Tests strEscape with CharsetEncoder for non-ASCII characters
  @Test
  public void testStrEscape_withCharsetEncoder() {
    CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
    assertEquals("\"\\u00e9\"", CodeGenerator.strEscape("\u00e9", '"', "\\\"", "\\\'", "\\\\", asciiEncoder));

    CharsetEncoder utf8Encoder = Charset.forName("UTF-8").newEncoder();
    assertEquals("\"\u00e9\"", CodeGenerator.strEscape("\u00e9", '"', "\\\"", "\\\'", "\\\\", utf8Encoder));
  }

  // Tests regexpEscape with CharsetEncoder, character classes, and special sequences
  @Test
  public void testRegexpEscape_characterClassesAndCharsetEncoder() {
    CodeGenerator cg = CodeGenerator.forCostEstimation(null);
    CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
    assertEquals("/[\\/]/", cg.regexpEscape("[/]", asciiEncoder));
    assertEquals("/[/]/", cg.regexpEscape("[/]"));
    assertEquals("/\\u00e9/", cg.regexpEscape("\u00e9", asciiEncoder));
    assertEquals("/\\x3c/script>/", cg.regexpEscape("</script>", asciiEncoder));
    assertEquals("/\\x3c!--/", cg.regexpEscape("<!--", asciiEncoder));
    assertEquals("/--\\>/", cg.regexpEscape("-->", asciiEncoder));
    assertEquals("/]]\\>/", cg.regexpEscape("]]>", asciiEncoder));
    assertEquals("/\\u2028\\u2029/", cg.regexpEscape("\u2028\u2029", asciiEncoder));
  }

  // Tests identifierEscape with high Unicode codepoints
  @Test
  public void testIdentifierEscape_highUnicodeCharacters() {
    assertEquals("\\u1234", CodeGenerator.identifierEscape("\u1234"));
    assertEquals("\\uffff", CodeGenerator.identifierEscape("\uffff"));
  }

  // Tests AST node code generation via cost estimation generator
  @Test
  public void testAdd_variousAstNodes() {
    CodeGenerator cg = CodeGenerator.forCostEstimation(null);

    cg.add(new Node(Token.EMPTY));
    cg.add(Node.newNumber(0.0));
    cg.add(Node.newNumber(-0.0));
    cg.add(Node.newNumber(42.5));
    cg.add(Node.newString("simpleString"));
    cg.add(new Node(Token.NULL));
    cg.add(new Node(Token.TRUE));
    cg.add(new Node(Token.FALSE));
    cg.add(new Node(Token.THIS));
    cg.add(Node.newString(Token.NAME, "x"));

    // Unary and Binary operations
    cg.add(new Node(Token.NOT, Node.newString(Token.NAME, "a")));
    cg.add(new Node(Token.NEG, Node.newNumber(5)));
    cg.add(new Node(Token.POS, Node.newNumber(5)));
    cg.add(new Node(Token.VOID, Node.newNumber(0)));
    cg.add(new Node(Token.TYPEOF, Node.newString(Token.NAME, "a")));
    cg.add(new Node(Token.INC, Node.newString(Token.NAME, "a")));
    cg.add(new Node(Token.DEC, Node.newString(Token.NAME, "a")));
    cg.add(new Node(Token.ADD, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b")));
    cg.add(new Node(Token.SUB, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b")));
    cg.add(new Node(Token.MUL, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b")));
    cg.add(new Node(Token.DIV, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b")));
    cg.add(new Node(Token.AND, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b")));
    cg.add(new Node(Token.OR, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b")));
    cg.add(new Node(Token.HOOK, Node.newString(Token.NAME, "c"), Node.newNumber(1), Node.newNumber(2)));

    // Arrays, Objects, Calls, and Declarations
    cg.add(new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newNumber(2)));
    Node key = Node.newString(Token.STRING_KEY, "k");
    key.addChildToBack(Node.newNumber(1));
    cg.add(new Node(Token.OBJECTLIT, key));
    cg.add(new Node(Token.CALL, Node.newString(Token.NAME, "fn"), Node.newNumber(1)));
    cg.add(new Node(Token.NEW, Node.newString(Token.NAME, "Cls")));
    cg.add(new Node(Token.VAR, Node.newString(Token.NAME, "v")));

    // Control flow and statements
    Node block = new Node(Token.BLOCK);
    block.addChildToBack(new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "x")));
    block.addChildToBack(new Node(Token.RETURN, Node.newNumber(1)));
    cg.add(block);

    cg.add(new Node(Token.IF, Node.newString(Token.NAME, "cond"), new Node(Token.BLOCK)));
    cg.add(new Node(Token.WHILE, Node.newString(Token.NAME, "cond"), new Node(Token.BLOCK)));
    cg.add(new Node(Token.DO, new Node(Token.BLOCK), Node.newString(Token.NAME, "cond")));
    cg.add(new Node(Token.FOR, new Node(Token.EMPTY), new Node(Token.EMPTY), new Node(Token.EMPTY), new Node(Token.BLOCK)));
    cg.add(new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"), new Node(Token.PARAM_LIST), new Node(Token.BLOCK)));
    cg.add(new Node(Token.BREAK));
    cg.add(new Node(Token.CONTINUE));
  }
}