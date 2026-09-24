package com.google.javascript.jscomp;

import com.google.common.base.Charsets;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CodeGeneratorTest {

  // Tests escaping of null characters in JavaScript strings (Defects4J Closure-65 regression)
  @Test
  public void testEscapeToDoubleQuotedJsString_nullCharacter_escapesCorrectly() {
    String escaped = CodeGenerator.escapeToDoubleQuotedJsString("\0");
    assertEquals("\"\\000\"", escaped);

    String escapedFollowedByDigit = CodeGenerator.escapeToDoubleQuotedJsString("\0" + "7");
    assertEquals("\"\\0007\"", escapedFollowedByDigit);
  }

  // Tests escaping of common escape characters like newline, carriage return, tab, and backslash
  @Test
  public void testEscapeToDoubleQuotedJsString_controlCharacters_escapesSpecialChars() {
    String input = "\n\r\t\\\"'";
    String expected = "\"\\n\\r\\t\\\\\\\"'\"";
    assertEquals(expected, CodeGenerator.escapeToDoubleQuotedJsString(input));
  }

  // Tests escaping script tags to prevent HTML injection in script blocks
  @Test
  public void testEscapeToDoubleQuotedJsString_scriptTagsAndComments_escapesLtAndGt() {
    assertEquals("\"<\\/script>\"", CodeGenerator.escapeToDoubleQuotedJsString("</script>"));
    assertEquals("\"<\\/SCRIPT>\"", CodeGenerator.escapeToDoubleQuotedJsString("</SCRIPT>"));
    assertEquals("\"<\\!--\"", CodeGenerator.escapeToDoubleQuotedJsString("<!--"));
    assertEquals("\"--\\>\"", CodeGenerator.escapeToDoubleQuotedJsString("-->"));
    assertEquals("\"]]\\>\"", CodeGenerator.escapeToDoubleQuotedJsString("]]>"));
  }

  // Tests optimal quote selection when single quotes are more frequent than double quotes
  @Test
  public void testJsString_singleQuotePreference_choosesDoubleQuoteWrapper() {
    CodeGenerator cg = new CodeGenerator(null);
    String input = "say 'hello'";
    assertEquals("\"say 'hello'\"", cg.jsString(input));
  }

  // Tests optimal quote selection when double quotes are more frequent than single quotes
  @Test
  public void testJsString_doubleQuotePreference_choosesSingleQuoteWrapper() {
    CodeGenerator cg = new CodeGenerator(null);
    String input = "say \"hello\"";
    assertEquals("'say \"hello\"'", cg.jsString(input));
  }

  // Tests regexp escape helper with default options
  @Test
  public void testRegexpEscape_standardInput_wrapsWithSlashes() {
    assertEquals("/hello/", CodeGenerator.regexpEscape("hello"));
    assertEquals("/<\\/script>/", CodeGenerator.regexpEscape("</script>"));
  }

  // Tests regexp escape with CharsetEncoder for non-ASCII characters
  @Test
  public void testRegexpEscape_withCharsetEncoder_encodesOrEscapes() {
    CharsetEncoder asciiEncoder = Charsets.US_ASCII.newEncoder();
    String result = CodeGenerator.regexpEscape("héllo", asciiEncoder);
    assertEquals("/h\\u00e9llo/", result);

    CharsetEncoder utf8Encoder = Charsets.UTF_8.newEncoder();
    String utf8Result = CodeGenerator.regexpEscape("héllo", utf8Encoder);
    assertEquals("/héllo/", utf8Result);
  }

  // Tests identifier escaping for Latin characters
  @Test
  public void testIdentifierEscape_latinIdentifier_returnsSameString() {
    assertEquals("myVar_123", CodeGenerator.identifierEscape("myVar_123"));
  }

  // Tests identifier escaping for non-Latin characters
  @Test
  public void testIdentifierEscape_nonLatinIdentifier_escapesUnicode() {
    assertEquals("\\u00e9_var", CodeGenerator.identifierEscape("é_var"));
  }

  // Tests supplementary code point escaping in strings
  @Test
  public void testStrEscape_supplementaryUnicode_escapesSurrogatePair() {
    // Supplementary code point U+10400 (DESERET CAPITAL LETTER LONG I)
    String input = "\uD801\uDC00";
    String escaped = CodeGenerator.escapeToDoubleQuotedJsString(input);
    assertEquals("\"\\ud801\\udc00\"", escaped);
  }

  // Tests isSimpleNumber with valid integer strings
  @Test
  public void testIsSimpleNumber_validNumericStrings_returnsTrue() {
    assertTrue(CodeGenerator.isSimpleNumber("0"));
    assertTrue(CodeGenerator.isSimpleNumber("1234567890"));
  }

  // Tests isSimpleNumber with invalid strings
  @Test
  public void testIsSimpleNumber_invalidStrings_returnsFalse() {
    assertFalse(CodeGenerator.isSimpleNumber(""));
    assertFalse(CodeGenerator.isSimpleNumber("-1"));
    assertFalse(CodeGenerator.isSimpleNumber("12a3"));
    assertFalse(CodeGenerator.isSimpleNumber("1.5"));
  }

  // Tests getSimpleNumber parsing valid numbers
  @Test
  public void testGetSimpleNumber_validInput_returnsParsedDouble() {
    assertEquals(0.0, CodeGenerator.getSimpleNumber("0"), 0.0);
    assertEquals(42.0, CodeGenerator.getSimpleNumber("42"), 0.0);
  }

  // Tests getSimpleNumber with overflow and non-numeric inputs
  @Test
  public void testGetSimpleNumber_invalidOrOverflowInput_returnsNaN() {
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("abc")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("999999999999999999999999999999")));
  }

  // Tests strEscape with null encoder for characters outside Latin printable range
  @Test
  public void testStrEscape_noEncoder_escapesNonLatin() {
    String escaped = CodeGenerator.strEscape(
        "\u001f\u007f\u0100", '"', "\\\"", "'", "\\\\", null);
    assertEquals("\"\\u001f\\u007f\\u0100\"", escaped);
  }

  // Tests constructor with US_ASCII charset setting encoder to null
  @Test
  public void testConstructor_withAsciiCharset_handlesDefaultPath() {
    CodeGenerator cgAscii = new CodeGenerator(null, Charsets.US_ASCII);
    assertEquals("\"\\u00e9\"", cgAscii.jsString("é"));

    CodeGenerator cgUtf8 = new CodeGenerator(null, Charsets.UTF_8);
    assertEquals("\"é\"", cgUtf8.jsString("é"));
  }

  // Tests escaping of backspace, form feed, and unicode line/paragraph separators
  @Test
  public void testEscapeToDoubleQuotedJsString_specialFormattingCharacters() {
    String input = "\b\f\u2028\u2029\u0085\u00ad\ufeff";
    String expected = "\"\\b\\f\\u2028\\u2029\\u0085\\u00ad\\ufeff\"";
    assertEquals(expected, CodeGenerator.escapeToDoubleQuotedJsString(input));
  }

  // Tests isSimpleNumber rejecting numbers with leading zeroes
  @Test
  public void testIsSimpleNumber_leadingZeroes_returnsFalse() {
    assertFalse(CodeGenerator.isSimpleNumber("00"));
    assertFalse(CodeGenerator.isSimpleNumber("01"));
    assertFalse(CodeGenerator.isSimpleNumber("0123"));
  }

  // Tests getSimpleNumber rejecting numbers with leading zeroes
  @Test
  public void testGetSimpleNumber_leadingZeroes_returnsNaN() {
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("00")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("01")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("0123")));
  }

  // Tests quote selection when counts of single and double quotes are equal
  @Test
  public void testJsString_equalQuoteCounts_defaultsToDoubleQuotes() {
    CodeGenerator cg = new CodeGenerator(null);
    String input = "a 'single' and a \"double\"";
    assertEquals("\"a 'single' and a \\\"double\\\"\"", cg.jsString(input));
  }

  // Tests escaping forward slash inside regular expressions
  @Test
  public void testRegexpEscape_forwardSlash_escapesSlash() {
    assertEquals("/a\\/b/", CodeGenerator.regexpEscape("a/b"));
  }

  // Tests surrogate pairs with UTF-8 encoder
  @Test
  public void testStrEscape_surrogatePairWithUtf8Encoder() {
    CharsetEncoder utf8Encoder = Charsets.UTF_8.newEncoder();
    String input = "\uD801\uDC00";
    String escaped = CodeGenerator.strEscape(input, '"', "\\\"", "'", "\\\\", utf8Encoder);
    assertEquals("\"\uD801\uDC00\"", escaped);
  }
}