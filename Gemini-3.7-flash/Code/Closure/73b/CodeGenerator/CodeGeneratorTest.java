package com.google.javascript.jscomp;

import com.google.common.base.Charsets;
import org.junit.Test;

import java.nio.charset.CharsetEncoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CodeGeneratorTest {

  // Tests escaping of DEL character (0x7f) which is boundary ASCII and must be unicode escaped (Defects4J 73)
  @Test
  public void testJsString_delCharacter0x7f_escapedToUnicodeHex() {
    assertEquals("\"\\u007f\"", CodeGenerator.jsString("\u007f", null));
    assertEquals("\"a\\u007fb\"", CodeGenerator.jsString("a\u007fb", null));
    assertEquals("\"\\u007f\"", CodeGenerator.escapeToDoubleQuotedJsString("\u007f"));
    assertEquals("/\\u007f/", CodeGenerator.regexpEscape("\u007f"));
  }

  // Tests standard escape sequences for control characters
  @Test
  public void testJsString_controlCharacters_escapedCorrectly() {
    assertEquals("\"\\0\\n\\r\\t\\\\\"", CodeGenerator.jsString("\0\n\r\t\\", null));
  }

  // Tests boundary ASCII characters between 0x1f and 0x7f
  @Test
  public void testJsString_asciiPrintableBoundaries_preservedWithoutEscaping() {
    assertEquals("\" \"", CodeGenerator.jsString(" ", null)); // 0x20 space
    assertEquals("\"~\"", CodeGenerator.jsString("~", null)); // 0x7e tilde
    assertEquals("\"\\u001f\"", CodeGenerator.jsString("\u001f", null)); // 0x1f unit separator
  }

  // Tests choosing single vs double quote delimiters based on occurrences
  @Test
  public void testJsString_quoteSelection_prefersOptimalQuote() {
    assertEquals("'a\"b'", CodeGenerator.jsString("a\"b", null));
    assertEquals("\"a'b\"", CodeGenerator.jsString("a'b", null));
    assertEquals("\"a\\\"'b\"", CodeGenerator.jsString("a\"'b", null));
    assertEquals("'a\"\"\\\'b'", CodeGenerator.jsString("a\"\"'b", null));
  }

  // Tests escaping closing script tags, html comment openers, and cdata end tags
  @Test
  public void testJsString_htmlScriptAndCommentTags_escaped() {
    assertEquals("\"<\\/script>\"", CodeGenerator.jsString("</script>", null));
    assertEquals("\"<\\/SCRIPT>\"", CodeGenerator.jsString("</SCRIPT>", null));
    assertEquals("\"<\\!-- comment --\\>\"", CodeGenerator.jsString("<!-- comment -->", null));
    assertEquals("\"a]]\\>b\"", CodeGenerator.jsString("a]]>b", null));
    assertEquals("\"a>b\"", CodeGenerator.jsString("a>b", null));
  }

  // Tests unicode escaping for non-Latin characters without encoder
  @Test
  public void testJsString_nonLatinCharacters_escapedWithoutEncoder() {
    assertEquals("\"\\u0080\"", CodeGenerator.jsString("\u0080", null));
    assertEquals("\"\\u0100\"", CodeGenerator.jsString("\u0100", null));
    assertEquals("\"\\u2028\\u2029\"", CodeGenerator.jsString("\u2028\u2029", null));
  }

  // Tests supplementary unicode code points that require surrogate pairs
  @Test
  public void testJsString_supplementaryCodePoints_escapedAsSurrogates() {
    String supplementaryChar = new String(Character.toChars(0x10000));
    assertEquals("\"\\ud800\\udc00\"", CodeGenerator.jsString(supplementaryChar, null));
  }

  // Tests jsString behavior when a custom CharsetEncoder is provided
  @Test
  public void testJsString_withUtf8CharsetEncoder_preservesNonAscii() {
    CharsetEncoder utf8Encoder = Charsets.UTF_8.newEncoder();
    assertEquals("\"\u4e16\u754c\"", CodeGenerator.jsString("\u4e16\u754c", utf8Encoder));
  }

  // Tests regexpEscape method formatting
  @Test
  public void testRegexpEscape_regexPatterns_escapedCorrectly() {
    assertEquals("//", CodeGenerator.regexpEscape(""));
    assertEquals("/foo/test/", CodeGenerator.regexpEscape("foo/test"));
    assertEquals("/<\\/script>/", CodeGenerator.regexpEscape("</script>"));
  }

  // Tests escapeToDoubleQuotedJsString helper method
  @Test
  public void testEscapeToDoubleQuotedJsString_variousInputs_escapedCorrectly() {
    assertEquals("\"hello\"", CodeGenerator.escapeToDoubleQuotedJsString("hello"));
    assertEquals("\"\\\"hello\\\\world\\\"\"", CodeGenerator.escapeToDoubleQuotedJsString("\"hello\\world\""));
  }

  // Tests isSimpleNumber with positive integer strings
  @Test
  public void testIsSimpleNumber_validNumericStrings_returnsTrue() {
    assertTrue(CodeGenerator.isSimpleNumber("0"));
    assertTrue(CodeGenerator.isSimpleNumber("123456789"));
    assertTrue(CodeGenerator.isSimpleNumber("999"));
  }

  // Tests isSimpleNumber with non-numeric or invalid string formats
  @Test
  public void testIsSimpleNumber_invalidNumericStrings_returnsFalse() {
    assertFalse(CodeGenerator.isSimpleNumber(""));
    assertFalse(CodeGenerator.isSimpleNumber("-1"));
    assertFalse(CodeGenerator.isSimpleNumber("+1"));
    assertFalse(CodeGenerator.isSimpleNumber("12a3"));
    assertFalse(CodeGenerator.isSimpleNumber(" 100"));
  }

  // Tests getSimpleNumber converting valid numeric string to double
  @Test
  public void testGetSimpleNumber_validNumericStrings_returnsParsedValue() {
    assertEquals(0.0, CodeGenerator.getSimpleNumber("0"), 0.0);
    assertEquals(42.0, CodeGenerator.getSimpleNumber("42"), 0.0);
    assertEquals(1000.0, CodeGenerator.getSimpleNumber("1000"), 0.0);
  }

  // Tests getSimpleNumber returning NaN for invalid number strings
  @Test
  public void testGetSimpleNumber_invalidStrings_returnsNaN() {
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("abc")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("-5")));
  }

  // Tests identifierEscape with valid Latin identifier characters
  @Test
  public void testIdentifierEscape_latinIdentifier_returnsSameString() {
    assertEquals("myVar_123$", CodeGenerator.identifierEscape("myVar_123$"));
  }

  // Tests identifierEscape with non-Latin unicode characters
  @Test
  public void testIdentifierEscape_nonLatinIdentifier_escapedToUnicode() {
    assertEquals("\\u00e9", CodeGenerator.identifierEscape("\u00e9"));
    assertEquals("var_\\u00e9_test", CodeGenerator.identifierEscape("var_\u00e9_test"));
  }

  // Tests additional control characters \b and \f in jsString
  @Test
  public void testJsString_additionalControlCharacters_escapedCorrectly() {
    assertEquals("\"\\b\\f\"", CodeGenerator.jsString("\b\f", null));
  }

  // Tests HTML comment end delimiter '-->' escaping
  @Test
  public void testJsString_htmlCommentEndTag_escaped() {
    assertEquals("\"--\\>\"", CodeGenerator.jsString("-->", null));
    assertEquals("\"foo--\\>bar\"", CodeGenerator.jsString("foo-->bar", null));
  }

  // Tests leading zeroes in isSimpleNumber and getSimpleNumber
  @Test
  public void testIsSimpleNumber_leadingZeros_returnsFalse() {
    assertFalse(CodeGenerator.isSimpleNumber("01"));
    assertFalse(CodeGenerator.isSimpleNumber("00"));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("01")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("00")));
  }

  // Tests jsString with non-ASCII characters and an ASCII-only CharsetEncoder
  @Test
  public void testJsString_withAsciiOnlyCharsetEncoder_escapesNonAscii() {
    CharsetEncoder asciiEncoder = Charsets.US_ASCII.newEncoder();
    assertEquals("\"\\u00e9\"", CodeGenerator.jsString("\u00e9", asciiEncoder));
    assertEquals("\"\\u4e16\\u754c\"", CodeGenerator.jsString("\u4e16\u754c", asciiEncoder));
  }

  // Tests regexpEscape with CharsetEncoder
  @Test
  public void testRegexpEscape_withCharsetEncoder_escapedCorrectly() {
    CharsetEncoder utf8Encoder = Charsets.UTF_8.newEncoder();
    assertEquals("/\u4e16\u754c/", CodeGenerator.regexpEscape("\u4e16\u754c", utf8Encoder));
    CharsetEncoder asciiEncoder = Charsets.US_ASCII.newEncoder();
    assertEquals("/\\u4e16\\u754c/", CodeGenerator.regexpEscape("\u4e16\u754c", asciiEncoder));
  }

  // Tests identifierEscape with high surrogate / supplementary unicode characters
  @Test
  public void testIdentifierEscape_supplementaryCharacters_escapedCorrectly() {
    String supplementaryChar = new String(Character.toChars(0x10000));
    assertEquals("\\ud800\\udc00", CodeGenerator.identifierEscape(supplementaryChar));
  }
}