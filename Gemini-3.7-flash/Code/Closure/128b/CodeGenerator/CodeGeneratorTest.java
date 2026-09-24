package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CodeGeneratorTest {

  private CodeGenerator codeGenerator;

  @Before
  public void setUp() {
    codeGenerator = CodeGenerator.forCostEstimation(null);
  }

  // Tests isSimpleNumber with single zero digit (Defects4J Bug 128)
  @Test
  public void testIsSimpleNumber_zero_returnsTrue() {
    assertTrue(CodeGenerator.isSimpleNumber("0"));
  }

  // Tests isSimpleNumber with positive integer strings
  @Test
  public void testIsSimpleNumber_positiveIntegers_returnsTrue() {
    assertTrue(CodeGenerator.isSimpleNumber("1"));
    assertTrue(CodeGenerator.isSimpleNumber("10"));
    assertTrue(CodeGenerator.isSimpleNumber("123456789"));
  }

  // Tests isSimpleNumber with leading zero numbers
  @Test
  public void testIsSimpleNumber_leadingZero_returnsFalse() {
    assertFalse(CodeGenerator.isSimpleNumber("00"));
    assertFalse(CodeGenerator.isSimpleNumber("01"));
    assertFalse(CodeGenerator.isSimpleNumber("0123"));
  }

  // Tests isSimpleNumber with empty string and non-digit characters
  @Test
  public void testIsSimpleNumber_invalidStrings_returnsFalse() {
    assertFalse(CodeGenerator.isSimpleNumber(""));
    assertFalse(CodeGenerator.isSimpleNumber("-1"));
    assertFalse(CodeGenerator.isSimpleNumber("1.5"));
    assertFalse(CodeGenerator.isSimpleNumber("abc"));
    assertFalse(CodeGenerator.isSimpleNumber("12a3"));
  }

  // Tests getSimpleNumber with "0" (Defects4J Bug 128)
  @Test
  public void testGetSimpleNumber_zero_returnsZero() {
    assertEquals(0.0, CodeGenerator.getSimpleNumber("0"), 0.0);
  }

  // Tests getSimpleNumber with valid numbers
  @Test
  public void testGetSimpleNumber_validNumbers_returnsDoubleValue() {
    assertEquals(1.0, CodeGenerator.getSimpleNumber("1"), 0.0);
    assertEquals(123.0, CodeGenerator.getSimpleNumber("123"), 0.0);
  }

  // Tests getSimpleNumber with invalid formats
  @Test
  public void testGetSimpleNumber_invalidFormats_returnsNaN() {
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("012")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("abc")));
  }

  // Tests getSimpleNumber with numbers exceeding max safe integer or long range
  @Test
  public void testGetSimpleNumber_overflowValues_returnsNaN() {
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("9007199254740993")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("999999999999999999999999999999")));
  }

  // Tests identifierEscape with Latin characters
  @Test
  public void testIdentifierEscape_latinIdentifier_returnsUnchanged() {
    assertEquals("variableName", CodeGenerator.identifierEscape("variableName"));
    assertEquals("_valid$123", CodeGenerator.identifierEscape("_valid$123"));
  }

  // Tests identifierEscape with non-Latin Unicode characters
  @Test
  public void testIdentifierEscape_nonLatinCharacters_escapesToUnicode() {
    assertEquals("\\u0100", CodeGenerator.identifierEscape("\u0100"));
    assertEquals("var_\\u03c0", CodeGenerator.identifierEscape("var_\u03c0"));
  }

  // Tests escapeToDoubleQuotedJsString with standard strings
  @Test
  public void testEscapeToDoubleQuotedJsString_plainText_returnsQuoted() {
    assertEquals("\"hello world\"", codeGenerator.escapeToDoubleQuotedJsString("hello world"));
  }

  // Tests escapeToDoubleQuotedJsString with escaped control characters
  @Test
  public void testEscapeToDoubleQuotedJsString_controlCharacters_escapesProperly() {
    assertEquals("\"\\x00\"", codeGenerator.escapeToDoubleQuotedJsString("\0"));
    assertEquals("\"\\b\\f\\n\\r\\t\"", codeGenerator.escapeToDoubleQuotedJsString("\b\f\n\r\t"));
    assertEquals("\"\\\\\"", codeGenerator.escapeToDoubleQuotedJsString("\\"));
    assertEquals("\"\\\"\"", codeGenerator.escapeToDoubleQuotedJsString("\""));
  }

  // Tests escapeToDoubleQuotedJsString with Unicode line terminators
  @Test
  public void testEscapeToDoubleQuotedJsString_lineTerminators_escapesToUnicode() {
    assertEquals("\"\\u2028\"", codeGenerator.escapeToDoubleQuotedJsString("\u2028"));
    assertEquals("\"\\u2029\"", codeGenerator.escapeToDoubleQuotedJsString("\u2029"));
  }

  // Tests escapeToDoubleQuotedJsString preventing script and comment tags injection
  @Test
  public void testEscapeToDoubleQuotedJsString_scriptAndCommentTags_escapesAngleBrackets() {
    assertEquals("\"\\x3c/script\\x3e\"", codeGenerator.escapeToDoubleQuotedJsString("</script>"));
    assertEquals("\"\\x3c!--\"", codeGenerator.escapeToDoubleQuotedJsString("<!--"));
    assertEquals("\"--\\x3e\"", codeGenerator.escapeToDoubleQuotedJsString("-->"));
    assertEquals("\"]]\\x3e\"", codeGenerator.escapeToDoubleQuotedJsString("]]>"));
  }

  // Tests regexpEscape with basic regex pattern
  @Test
  public void testRegexpEscape_simplePattern_wrapsInSlashes() {
    assertEquals("/abc/", codeGenerator.regexpEscape("abc"));
    assertEquals("/a=b&c/", codeGenerator.regexpEscape("a=b&c"));
  }

  // Tests regexpEscape with special escaped characters
  @Test
  public void testRegexpEscape_controlCharacters_escapesCorrectly() {
    assertEquals("/\\n\\r/", codeGenerator.regexpEscape("\n\r"));
  }
}