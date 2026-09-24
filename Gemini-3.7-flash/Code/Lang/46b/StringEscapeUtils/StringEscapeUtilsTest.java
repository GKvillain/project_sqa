package org.apache.commons.lang;

import java.io.IOException;
import java.io.StringWriter;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.junit.Test;

import static org.junit.Assert.*;

public class StringEscapeUtilsTest {

    // Tests default constructor instantiation
    @Test
    public void testConstructor_default_instanceCreated() {
        assertNotNull(new StringEscapeUtils());
    }

    // Tests escapeJava with null input
    @Test
    public void testEscapeJava_nullInput_returnsNull() {
        assertNull(StringEscapeUtils.escapeJava(null));
    }

    // Tests escapeJava with normal string and special characters without escaping single quote
    @Test
    public void testEscapeJava_quotesAndControls_returnsEscapedString() {
        String input = "He didn't say, \n\t\r\f\b\"Stop!\"\\";
        String expected = "He didn't say, \\n\\t\\r\\f\\b\\\"Stop!\\\"\\\\";
        assertEquals(expected, StringEscapeUtils.escapeJava(input));
    }

    // Tests escapeJava with forward slash which should not be escaped in Java
    @Test
    public void testEscapeJava_forwardSlash_doesNotEscapeSlash() {
        assertEquals("/path/to/resource", StringEscapeUtils.escapeJava("/path/to/resource"));
    }

    // Tests escapeJava with unicode characters across different ranges
    @Test
    public void testEscapeJava_unicodeCharacters_returnsUnicodeEscaped() {
        String input = "\u0001\u0010\u0080\u0100\u1000";
        String expected = "\\u0001\\u0010\\u0080\\u0100\\u1000";
        assertEquals(expected, StringEscapeUtils.escapeJava(input));
    }

    // Tests escapeJava with Writer and null writer argument
    @Test(expected = IllegalArgumentException.class)
    public void testEscapeJava_nullWriter_throwsIllegalArgumentException() throws IOException {
        StringEscapeUtils.escapeJava(null, "test");
    }

    // Tests escapeJavaScript with single quote escaping
    @Test
    public void testEscapeJavaScript_singleQuote_escapesSingleQuote() {
        String input = "He didn't say";
        String expected = "He didn\\'t say";
        assertEquals(expected, StringEscapeUtils.escapeJavaScript(input));
    }

    // Tests escapeJavaScript with Writer writing null string
    @Test
    public void testEscapeJavaScript_nullStringWithWriter_doesNotWrite() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeJavaScript(writer, null);
        assertEquals("", writer.toString());
    }

    // Tests unescapeJava with null input
    @Test
    public void testUnescapeJava_nullInput_returnsNull() {
        assertNull(StringEscapeUtils.unescapeJava(null));
    }

    // Tests unescapeJava with valid escapes and unicode
    @Test
    public void testUnescapeJava_validEscapes_returnsUnescapedString() {
        String input = "\\b\\t\\n\\f\\r\\\"\\\'\\\\\\u0041\\u0020A";
        String expected = "\b\t\n\f\r\"\'\\A A";
        assertEquals(expected, StringEscapeUtils.unescapeJava(input));
    }

    // Tests unescapeJava with trailing backslash and regular escaped characters
    @Test
    public void testUnescapeJava_trailingSlash_returnsTrailingSlash() {
        assertEquals("test\\", StringEscapeUtils.unescapeJava("test\\"));
        assertEquals("test\\k", StringEscapeUtils.unescapeJava("test\\k"));
    }

    // Tests unescapeJava with invalid unicode hex sequence
    @Test(expected = NestableRuntimeException.class)
    public void testUnescapeJava_invalidUnicode_throwsException() {
        StringEscapeUtils.unescapeJava("\\uZZZZ");
    }

    // Tests unescapeJavaScript wrapper method
    @Test
    public void testUnescapeJavaScript_escapedString_returnsUnescapedString() throws IOException {
        assertEquals("a\nb", StringEscapeUtils.unescapeJavaScript("a\\nb"));
        StringWriter writer = new StringWriter();
        StringEscapeUtils.unescapeJavaScript(writer, "a\\nb");
        assertEquals("a\nb", writer.toString());
    }

    // Tests escapeHtml and unescapeHtml methods
    @Test
    public void testEscapeHtmlAndUnescapeHtml_htmlEntities_escapesAndUnescapesCorrectly() {
        assertNull(StringEscapeUtils.escapeHtml(null));
        assertNull(StringEscapeUtils.unescapeHtml(null));

        String input = "\"bread\" & <butter> \u00E0";
        String escaped = StringEscapeUtils.escapeHtml(input);
        assertTrue(escaped.contains("&quot;bread&quot;"));
        assertTrue(escaped.contains("&amp;"));
        assertTrue(escaped.contains("&lt;butter&gt;"));

        assertEquals(input, StringEscapeUtils.unescapeHtml(escaped));
    }

    // Tests escapeHtml with null Writer throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testEscapeHtml_nullWriter_throwsIllegalArgumentException() throws IOException {
        StringEscapeUtils.escapeHtml(null, "test");
    }

    // Tests escapeXml and unescapeXml with basic entities and null handling
    @Test
    public void testEscapeXmlAndUnescapeXml_xmlEntities_escapesAndUnescapesCorrectly() {
        assertNull(StringEscapeUtils.escapeXml(null));
        assertNull(StringEscapeUtils.unescapeXml(null));

        String input = "<tag attr=\"value\" & 'test'>";
        String escaped = StringEscapeUtils.escapeXml(input);
        assertEquals("&lt;tag attr=&quot;value&quot; &amp; &apos;test&apos;&gt;", escaped);
        assertEquals(input, StringEscapeUtils.unescapeXml(escaped));
    }

    // Tests escapeXml with Writer and null handling
    @Test(expected = IllegalArgumentException.class)
    public void testEscapeXml_nullWriter_throwsIllegalArgumentException() throws IOException {
        StringEscapeUtils.escapeXml(null, "test");
    }

    // Tests escapeSql with single quotes
    @Test
    public void testEscapeSql_singleQuotes_doublesSingleQuotes() {
        assertNull(StringEscapeUtils.escapeSql(null));
        assertEquals("McHale''s Navy", StringEscapeUtils.escapeSql("McHale's Navy"));
        assertEquals("no quotes", StringEscapeUtils.escapeSql("no quotes"));
    }

    // Tests escapeCsv with various characters requiring quotes
    @Test
    public void testEscapeCsv_variousInputs_escapesProperly() {
        assertNull(StringEscapeUtils.escapeCsv(null));
        assertEquals("simple", StringEscapeUtils.escapeCsv("simple"));
        assertEquals("\"hello, world\"", StringEscapeUtils.escapeCsv("hello, world"));
        assertEquals("\"hello\nworld\"", StringEscapeUtils.escapeCsv("hello\nworld"));
        assertEquals("\"hello\"\"world\"", StringEscapeUtils.escapeCsv("hello\"world"));
    }

    // Tests unescapeCsv with quoted and unquoted strings
    @Test
    public void testUnescapeCsv_quotedAndUnquoted_unescapesProperly() {
        assertNull(StringEscapeUtils.unescapeCsv(null));
        assertEquals("", StringEscapeUtils.unescapeCsv(""));
        assertEquals("a", StringEscapeUtils.unescapeCsv("a"));
        assertEquals("hello, world", StringEscapeUtils.unescapeCsv("\"hello, world\""));
        assertEquals("hello\"world", StringEscapeUtils.unescapeCsv("\"hello\"\"world\""));
        assertEquals("unquoted,value", StringEscapeUtils.unescapeCsv("unquoted,value"));
    }
}