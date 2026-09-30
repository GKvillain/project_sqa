package org.apache.commons.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;

public class StringEscapeUtilsTest {

    // Tests null input for escapeJava
    @Test
    public void testEscapeJava_nullInput_returnsNull() {
        assertNull(StringEscapeUtils.escapeJava(null));
    }

    // Tests empty string for escapeJava
    @Test
    public void testEscapeJava_emptyString_returnsEmpty() {
        assertEquals("", StringEscapeUtils.escapeJava(""));
    }

    // Tests forward slash is not escaped by escapeJava (bug detection)
    @Test
    public void testEscapeJava_forwardSlash_returnsSlash() {
        assertEquals("/", StringEscapeUtils.escapeJava("/"));
    }

    // Tests forward slash is escaped by escapeJavaScript
    @Test
    public void testEscapeJavaScript_forwardSlash_returnsEscapedSlash() {
        assertEquals("\\/", StringEscapeUtils.escapeJavaScript("/"));
    }

    // Tests single quote is not escaped by escapeJava
    @Test
    public void testEscapeJava_singleQuote_notEscaped() {
        assertEquals("'", StringEscapeUtils.escapeJava("'"));
    }

    // Tests single quote is escaped by escapeJavaScript
    @Test
    public void testEscapeJavaScript_singleQuote_escaped() {
        assertEquals("\\'", StringEscapeUtils.escapeJavaScript("'"));
    }

    // Tests double quote is escaped by both
    @Test
    public void testEscapeJava_doubleQuote_escaped() {
        assertEquals("\\\"", StringEscapeUtils.escapeJava("\""));
    }

    // Tests backslash is escaped
    @Test
    public void testEscapeJava_backslash_escaped() {
        assertEquals("\\\\", StringEscapeUtils.escapeJava("\\"));
    }

    // Tests control characters: tab, newline, carriage return, backspace, form feed
    @Test
    public void testEscapeJava_controlChars_escaped() {
        assertEquals("\\t", StringEscapeUtils.escapeJava("\t"));
        assertEquals("\\n", StringEscapeUtils.escapeJava("\n"));
        assertEquals("\\r", StringEscapeUtils.escapeJava("\r"));
        assertEquals("\\b", StringEscapeUtils.escapeJava("\b"));
        assertEquals("\\f", StringEscapeUtils.escapeJava("\f"));
    }

    // Tests high unicode (> 0xfff) escape
    @Test
    public void testEscapeJava_highUnicode_escaped() {
        assertEquals("\\uA000", StringEscapeUtils.escapeJava("\ua000"));
    }

    // Tests unicode > 0xff and <= 0xfff
    @Test
    public void testEscapeJava_mediumUnicode_escaped() {
        assertEquals("\\u0100", StringEscapeUtils.escapeJava("\u0100"));
    }

    // Tests unicode > 0x7f and <= 0xff
    @Test
    public void testEscapeJava_lowUnicode_escaped() {
        assertEquals("\\u0080", StringEscapeUtils.escapeJava("\u0080"));
    }

    // Tests escapeJava with Writer, null Writer throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testEscapeJavaWriter_nullWriter_throwsException() throws IOException {
        StringEscapeUtils.escapeJava(null, "test");
    }

    // Tests escapeJava with Writer and null string does nothing
    @Test
    public void testEscapeJavaWriter_nullString_writesNothing() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeJava(writer, null);
        assertEquals("", writer.toString());
    }

    // Tests unescapeJava basic
    @Test
    public void testUnescapeJava_escapedString_unescaped() {
        assertEquals("\\", StringEscapeUtils.unescapeJava("\\\\"));
        assertEquals("\"", StringEscapeUtils.unescapeJava("\\\""));
        assertEquals("'", StringEscapeUtils.unescapeJava("\\'"));
        assertEquals("\n", StringEscapeUtils.unescapeJava("\\n"));
        assertEquals("\t", StringEscapeUtils.unescapeJava("\\t"));
    }

    // Tests unescapeJava with unicode escape
    @Test
    public void testUnescapeJava_unicodeEscape_returnsChar() {
        assertEquals("A", StringEscapeUtils.unescapeJava("\\u0041"));
    }

    // Tests unescapeJava null input
    @Test
    public void testUnescapeJava_nullInput_returnsNull() {
        assertNull(StringEscapeUtils.unescapeJava(null));
    }

    // Tests escapeCsv with comma
    @Test
    public void testEscapeCsv_containsComma_quoted() {
        assertEquals("\"a,b\"", StringEscapeUtils.escapeCsv("a,b"));
    }

    // Tests escapeCsv with double quote
    @Test
    public void testEscapeCsv_containsQuote_escapedInsideQuotes() {
        assertEquals("\"\"\"\"", StringEscapeUtils.escapeCsv("\""));
    }

    // Tests escapeCsv with newline
    @Test
    public void testEscapeCsv_containsNewline_quoted() {
        assertEquals("\"a\nb\"", StringEscapeUtils.escapeCsv("a\nb"));
    }

    // Tests escapeCsv without special chars returns same
    @Test
    public void testEscapeCsv_noSpecialChars_returnsSame() {
        assertEquals("hello", StringEscapeUtils.escapeCsv("hello"));
    }

    // Tests unescapeCsv with quoted string
    @Test
    public void testUnescapeCsv_quotedString_removesQuotes() {
        assertEquals("a,b", StringEscapeUtils.unescapeCsv("\"a,b\""));
    }

    // Tests unescapeCsv with escaped double quote inside
    @Test
    public void testUnescapeCsv_escapedDoubleQuote_unescaped() {
        assertEquals("\"", StringEscapeUtils.unescapeCsv("\"\"\"\""));
    }

    // Tests unescapeCsv null input
    @Test
    public void testUnescapeCsv_nullInput_returnsNull() {
        assertNull(StringEscapeUtils.unescapeCsv(null));
    }

    // Tests escapeSql replaces single quote with two single quotes
    @Test
    public void testEscapeSql_singleQuote_replaced() {
        assertEquals("McHale''s Navy", StringEscapeUtils.escapeSql("McHale's Navy"));
    }

    // Tests escapeSql null input
    @Test
    public void testEscapeSql_nullInput_returnsNull() {
        assertNull(StringEscapeUtils.escapeSql(null));
    }

    // Tests escapeHtml basic (entity escaping)
    @Test
    public void testEscapeHtml_ampersand_escaped() {
        assertEquals("&amp;", StringEscapeUtils.escapeHtml("&"));
    }

    // Tests unescapeHtml basic
    @Test
    public void testUnescapeHtml_entity_unescaped() {
        assertEquals("&", StringEscapeUtils.unescapeHtml("&amp;"));
    }

    // Tests escapeXml basic
    @Test
    public void testEscapeXml_lessThan_escaped() {
        assertEquals("&lt;", StringEscapeUtils.escapeXml("<"));
    }

    // Tests unescapeXml basic
    @Test
    public void testUnescapeXml_entity_unescaped() {
        assertEquals("<", StringEscapeUtils.unescapeXml("&lt;"));
    }

    // Tests unescapeJavaScript delegates to unescapeJava
    @Test
    public void testUnescapeJavaScript_escapedString_unescaped() {
        assertEquals("\n", StringEscapeUtils.unescapeJavaScript("\\n"));
    }
}