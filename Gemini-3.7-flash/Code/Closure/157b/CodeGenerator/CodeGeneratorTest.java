package com.google.javascript.jscomp;

import com.google.common.base.Charsets;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CodeGeneratorTest {

  // Tests standard ASCII string escaping with double quotes
  @Test
  public void testJsString_standardAscii_returnsDoubleQuotedString() {
    String result = CodeGenerator.jsString("hello world", null);
    assertEquals("\"hello world\"", result);
  }

  // Tests string with more double quotes chooses single quote delimiter
  @Test
  public void testJsString_moreDoubleQuotes_usesSingleQuotes() {
    String result = CodeGenerator.jsString("a \"b\" \"c\"", null);
    assertEquals("'a \"b\" \"c\"'", result);
  }

  // Tests string with more single quotes chooses double quote delimiter
  @Test
  public void testJsString_moreSingleQuotes_usesDoubleQuotes() {
    String result = CodeGenerator.jsString("a 'b' 'c'", null);
    assertEquals("\"a 'b' 'c'\"", result);
  }

  // Tests string escaping when single and double quote counts are equal
  @Test
  public void testJsString_equalQuoteCounts_usesDoubleQuotes() {
    String result = CodeGenerator.jsString("'test\"", null);
    assertEquals("\"'test\\\"\"", result);
  }

  // Tests escaping of script tag closure to prevent XSS / parser breakout
  @Test
  public void testJsString_withScriptTag_escapesScriptClosing() {
    String result = CodeGenerator.jsString("</script>", null);
    assertEquals("\"<\\/script>\"", result);
  }

  // Tests case-insensitive script tag closure escaping
  @Test
  public void testJsString_withCaseInsensitiveScriptTag_escapesScriptClosing() {
    String result = CodeGenerator.jsString("</sCrIpT>", null);
    assertEquals("\"<\\/sCrIpT>\"", result);
  }

  // Tests escaping of HTML comment opening sequence
  @Test
  public void testJsString_withHtmlCommentStart_escapesComment() {
    String result = CodeGenerator.jsString("<!-- comment", null);
    assertEquals("\"<\\!-- comment\"", result);
  }

  // Tests escaping of HTML comment closing sequence
  @Test
  public void testJsString_withHtmlCommentEnd_escapesGreaterThan() {
    String result = CodeGenerator.jsString("-->", null);
    assertEquals("\"--\\>\"", result);
  }

  // Tests escaping of CDATA end sequence
  @Test
  public void testJsString_withCDataEnd_escapesGreaterThan() {
    String result = CodeGenerator.jsString("]]>", null);
    assertEquals("\"]]\\>\"", result);
  }

  // Tests escaping of standard control characters: newline, carriage return, tab, null, backslash
  @Test
  public void testJsString_withControlCharacters_escapesControlChars() {
    String result = CodeGenerator.jsString("\0\n\r\t\\", null);
    assertEquals("\"\\0\\n\\r\\t\\\\\"", result);
  }

  // Tests non-ASCII characters escaped to unicode when no charset encoder provided
  @Test
  public void testJsString_withNonAsciiWithoutEncoder_escapesToUnicode() {
    String result = CodeGenerator.jsString("\u0080\u00FF", null);
    assertEquals("\"\\u0080\\u00ff\"", result);
  }

  // Tests non-ASCII characters preserved when supported by charset encoder
  @Test
  public void testJsString_withUtf8CharsetEncoder_preservesEncodableChars() {
    CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
    String result = CodeGenerator.jsString("\u00E9", encoder);
    assertEquals("\"\u00E9\"", result);
  }

  // Tests regex escaping with standard pattern
  @Test
  public void testRegexpEscape_basicPattern_escapesWithSlashes() {
    String result = CodeGenerator.regexpEscape("abc");
    assertEquals("/abc/", result);
  }

  // Tests regex escaping containing forward slashes and special script tags
  @Test
  public void testRegexpEscape_withScriptTag_escapesScriptTag() {
    String result = CodeGenerator.regexpEscape("</script>");
    assertEquals("/<\\/script>/", result);
  }

  // Tests regex escaping with charset encoder
  @Test
  public void testRegexpEscape_withCharsetEncoder_escapesProperly() {
    CharsetEncoder encoder = Charset.forName("US-ASCII").newEncoder();
    String result = CodeGenerator.regexpEscape("\u00E9", encoder);
    assertEquals("/\\u00e9/", result);
  }

  // Tests escapeToDoubleQuotedJsString helper
  @Test
  public void testEscapeToDoubleQuotedJsString_variousInputs_escapesDoubleQuotes() {
    String result = CodeGenerator.escapeToDoubleQuotedJsString("hello \"world\"");
    assertEquals("\"hello \\\"world\\\"\"", result);
  }

  // Tests identifier escaping for valid latin identifier
  @Test
  public void testIdentifierEscape_latinIdentifier_returnsUnchanged() {
    String result = CodeGenerator.identifierEscape("myVar123");
    assertEquals("myVar123", result);
  }

  // Tests identifier escaping for non-latin characters
  @Test
  public void testIdentifierEscape_nonLatinChars_escapesToUnicodeHex() {
    String result = CodeGenerator.identifierEscape("var_\u00E9");
    assertEquals("var_\\u00e9", result);
  }

  // Tests identifier escaping with boundary non-printable characters
  @Test
  public void testIdentifierEscape_controlChars_escapesToUnicodeHex() {
    String result = CodeGenerator.identifierEscape("a\u001Fb");
    assertEquals("a\\u001fb", result);
  }

  // Tests empty string inputs across escape methods
  @Test
  public void testJsString_emptyString_returnsEmptyQuotes() {
    String result = CodeGenerator.jsString("", null);
    assertEquals("\"\"", result);
    assertEquals("//", CodeGenerator.regexpEscape(""));
    assertEquals("", CodeGenerator.identifierEscape(""));
  }

  // Tests escaping of JavaScript line terminators \u2028 and \u2029 in strings
  @Test
  public void testJsString_lineAndParagraphSeparators_escapedToUnicode() {
    String result = CodeGenerator.jsString("\u2028\u2029", null);
    assertEquals("\"\\u2028\\u2029\"", result);

    CharsetEncoder utf8Encoder = Charsets.UTF_8.newEncoder();
    String resultWithEncoder = CodeGenerator.jsString("\u2028\u2029", utf8Encoder);
    assertEquals("\"\\u2028\\u2029\"", resultWithEncoder);
  }

  // Tests regexpEscape with forward slashes, line breaks, HTML comment, and CDATA end markers
  @Test
  public void testRegexpEscape_specialSequences_escapesProperly() {
    assertEquals("/a\\/b/", CodeGenerator.regexpEscape("a/b"));
    assertEquals("/\\n\\r\\u2028\\u2029/", CodeGenerator.regexpEscape("\n\r\u2028\u2029"));
    assertEquals("/<\\!--/", CodeGenerator.regexpEscape("<!--"));
    assertEquals("/--\\>/", CodeGenerator.regexpEscape("-->"));
    assertEquals("/]]\\>/", CodeGenerator.regexpEscape("]]>"));
    assertEquals("/<\\/script>/", CodeGenerator.regexpEscape("</SCRIPT>"));
  }

  // Tests isSimpleNumber utility method
  @Test
  public void testIsSimpleNumber() {
    assertTrue(CodeGenerator.isSimpleNumber("0"));
    assertTrue(CodeGenerator.isSimpleNumber("123"));
    assertTrue(CodeGenerator.isSimpleNumber("9"));

    assertFalse(CodeGenerator.isSimpleNumber(""));
    assertFalse(CodeGenerator.isSimpleNumber("01"));
    assertFalse(CodeGenerator.isSimpleNumber("00"));
    assertFalse(CodeGenerator.isSimpleNumber("-1"));
    assertFalse(CodeGenerator.isSimpleNumber("1.5"));
    assertFalse(CodeGenerator.isSimpleNumber("1e5"));
    assertFalse(CodeGenerator.isSimpleNumber("abc"));
  }

  // Tests isNegativeZero utility method
  @Test
  public void testIsNegativeZero() {
    assertTrue(CodeGenerator.isNegativeZero(-0.0));
    assertFalse(CodeGenerator.isNegativeZero(0.0));
    assertFalse(CodeGenerator.isNegativeZero(-1.0));
    assertFalse(CodeGenerator.isNegativeZero(1.0));
    assertFalse(CodeGenerator.isNegativeZero(Double.NaN));
    assertFalse(CodeGenerator.isNegativeZero(Double.NEGATIVE_INFINITY));
    assertFalse(CodeGenerator.isNegativeZero(Double.POSITIVE_INFINITY));
  }

  // Tests surrogate pair handling in strEscape
  @Test
  public void testJsString_surrogatePair_escapedCorrectly() {
    String supplementaryChar = new String(Character.toChars(0x10400));
    String resultNoEncoder = CodeGenerator.jsString(supplementaryChar, null);
    assertEquals("\"\\ud801\\udc00\"", resultNoEncoder);

    CharsetEncoder asciiEncoder = Charsets.US_ASCII.newEncoder();
    String resultAscii = CodeGenerator.jsString(supplementaryChar, asciiEncoder);
    assertEquals("\"\\ud801\\udc00\"", resultAscii);

    CharsetEncoder utf8Encoder = Charsets.UTF_8.newEncoder();
    String resultUtf8 = CodeGenerator.jsString(supplementaryChar, utf8Encoder);
    assertEquals("\"" + supplementaryChar + "\"", resultUtf8);
  }
}