package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import org.apache.commons.lang.exception.NestableRuntimeException;

public class StringEscapeUtilsTest {

    @Test
    public void testNullInput_ReturnsNull() {
        assertNull(StringEscapeUtils.escapeJava(null));
        assertNull(StringEscapeUtils.escapeJavaScript(null));
        assertNull(StringEscapeUtils.escapeHtml(null));
        assertNull(StringEscapeUtils.escapeXml(null));
        assertNull(StringEscapeUtils.escapeSql(null));
        assertNull(StringEscapeUtils.unescapeJava(null));
        assertNull(StringEscapeUtils.unescapeJavaScript(null));
        assertNull(StringEscapeUtils.unescapeHtml(null));
        assertNull(StringEscapeUtils.unescapeXml(null));
    }

    @Test
    public void testEmptyString_ReturnsEmpty() {
        assertEquals("", StringEscapeUtils.escapeJava(""));
        assertEquals("", StringEscapeUtils.escapeJavaScript(""));
        assertEquals("", StringEscapeUtils.escapeHtml(""));
        assertEquals("", StringEscapeUtils.escapeXml(""));
        assertEquals("", StringEscapeUtils.escapeSql(""));
        assertEquals("", StringEscapeUtils.unescapeJava(""));
        assertEquals("", StringEscapeUtils.unescapeJavaScript(""));
        assertEquals("", StringEscapeUtils.unescapeHtml(""));
        assertEquals("", StringEscapeUtils.unescapeXml(""));
    }

    @Test
    public void testEscapeJava_MixedCharacters_ReturnsEscapedString() {
        // Covers normal characters, control chars \n, \b, \t, \r, \f,
        // backslash, double quote, single quote (no escape),
        // unicode >0x7f (>127), >0xff (>255), >0xfff (>4095),
        // low control default branches (ch > 0xf and ch <= 0xf).
        String input = "Hi\n\b\t\r\f\\\"'\u0080\u0100\u1000\u0000\u0010\u000b";
        String expected = "Hi\\n\\b\\t\\r\\f\\\\\\\"'\\u0080\\u0100\\u1000\\u0000\\u0010\\u000b";
        assertEquals(expected, StringEscapeUtils.escapeJava(input));
    }

    @Test
    public void testEscapeJavaScript_SpecialChars_ReturnsEscapedString() {
        // Covers escapeSingleQuote true (single quote escaped),
        // double quote, backslash, and newline.
        assertEquals("\\'", StringEscapeUtils.escapeJavaScript("'"));
        assertEquals("\\\\", StringEscapeUtils.escapeJavaScript("\\"));
        assertEquals("\\\"", StringEscapeUtils.escapeJavaScript("\""));
        assertEquals("Hello\\'World", StringEscapeUtils.escapeJavaScript("Hello'World"));
        assertEquals("Hi\\n", StringEscapeUtils.escapeJavaScript("Hi\n"));
    }

    @Test
    public void testUnescapeJava_EscapeSequences_ReturnsUnescaped() {
        // Covers all escape sequences: \n, \b, \t, \r, \f, \\, \', \", and default \x.
        String input = "Hi\\n\\b\\t\\r\\f\\\\\\'\\\"\\x";
        String expected = "Hi\n\b\t\r\f\\'\"x";
        assertEquals(expected, StringEscapeUtils.unescapeJava(input));
    }

    @Test
    public void testUnescapeJava_BackslashAtEnd_ReturnsBackslash() {
        // Covers final hadSlash case.
        assertEquals("abc\\", StringEscapeUtils.unescapeJava("abc\\\\"));
    }

    @Test
    public void testUnescapeJava_ValidUnicode_ReturnsCharacter() {
        assertEquals("A", StringEscapeUtils.unescapeJava("\\u0041"));
        assertEquals("\u00FC", StringEscapeUtils.unescapeJava("\\u00FC"));
        assertEquals("\uFFFF", StringEscapeUtils.unescapeJava("\\uFFFF"));
        assertEquals("ABC", StringEscapeUtils.unescapeJava("\\u0041\\u0042\\u0043"));
    }

    @Test
    public void testUnescapeJava_IncompleteUnicode_ReturnsEmpty() {
        // Incomplete unicode sequence is dropped.
        assertEquals("", StringEscapeUtils.unescapeJava("\\u00"));
        assertEquals("", StringEscapeUtils.unescapeJava("\\u"));
    }

    @Test(expected = NestableRuntimeException.class)
    public void testUnescapeJava_InvalidUnicode_ThrowsNestableRuntimeException() {
        StringEscapeUtils.unescapeJava("\\ughij");
    }

    @Test
    public void testEscapeHtml_Basic_ReturnsEscaped() {
        assertEquals("&lt;", StringEscapeUtils.escapeHtml("<"));
        assertEquals("&gt;", StringEscapeUtils.escapeHtml(">"));
        assertEquals("&amp;", StringEscapeUtils.escapeHtml("&"));
        assertEquals("&quot;", StringEscapeUtils.escapeHtml("\""));
        assertEquals("'", StringEscapeUtils.escapeHtml("'"));
    }

    @Test
    public void testUnescapeHtml_Basic_ReturnsUnescaped() {
        assertEquals("<", StringEscapeUtils.unescapeHtml("&lt;"));
        assertEquals(">", StringEscapeUtils.unescapeHtml("&gt;"));
        assertEquals("&", StringEscapeUtils.unescapeHtml("&amp;"));
        assertEquals("\"", StringEscapeUtils.unescapeHtml("&quot;"));
    }

    @Test
    public void testEscapeXml_Basic_ReturnsEscaped() {
        assertEquals("&lt;", StringEscapeUtils.escapeXml("<"));
        assertEquals("&gt;", StringEscapeUtils.escapeXml(">"));
        assertEquals("&amp;", StringEscapeUtils.escapeXml("&"));
        assertEquals("&quot;", StringEscapeUtils.escapeXml("\""));
        assertEquals("&apos;", StringEscapeUtils.escapeXml("'"));
    }

    @Test
    public void testUnescapeXml_Basic_ReturnsUnescaped() {
        assertEquals("<", StringEscapeUtils.unescapeXml("&lt;"));
        assertEquals(">", StringEscapeUtils.unescapeXml("&gt;"));
        assertEquals("&", StringEscapeUtils.unescapeXml("&amp;"));
        assertEquals("\"", StringEscapeUtils.unescapeXml("&quot;"));
        assertEquals("'", StringEscapeUtils.unescapeXml("&apos;"));
    }

    @Test
    public void testEscapeSql_SingleQuote_ReturnsEscaped() {
        assertEquals("''", StringEscapeUtils.escapeSql("'"));
        assertEquals("Hello ''World''", StringEscapeUtils.escapeSql("Hello 'World'"));
    }

    @Test
    public void testWriterNull_AllMethods_ThrowsIllegalArgumentException() throws IOException {
        try {
            StringEscapeUtils.escapeJava(null, "test");
            fail("escapeJava");
        } catch (IllegalArgumentException e) { }
        try {
            StringEscapeUtils.escapeJavaScript(null, "test");
            fail("escapeJavaScript");
        } catch (IllegalArgumentException e) { }
        try {
            StringEscapeUtils.unescapeJava(null, "test");
            fail("unescapeJava");
        } catch (IllegalArgumentException e) { }
        try {
            StringEscapeUtils.unescapeJavaScript(null, "test");
            fail("unescapeJavaScript");
        } catch (IllegalArgumentException e) { }
        try {
            StringEscapeUtils.escapeHtml(null, "test");
            fail("escapeHtml");
        } catch (IllegalArgumentException e) { }
        try {
            StringEscapeUtils.unescapeHtml(null, "test");
            fail("unescapeHtml");
        } catch (IllegalArgumentException e) { }
        try {
            StringEscapeUtils.escapeXml(null, "test");
            fail("escapeXml");
        } catch (IllegalArgumentException e) { }
        try {
            StringEscapeUtils.unescapeXml(null, "test");
            fail("unescapeXml");
        } catch (IllegalArgumentException e) { }
    }
}