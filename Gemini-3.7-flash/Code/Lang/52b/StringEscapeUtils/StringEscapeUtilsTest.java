package org.apache.commons.lang;

import java.io.IOException;
import java.io.StringWriter;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.junit.Test;
import static org.junit.Assert.*;

public class StringEscapeUtilsTest {

    // Tests public constructor instantiation
    @Test
    public void testConstructor_default_instanceNotNull() {
        StringEscapeUtils instance = new StringEscapeUtils();
        assertNotNull(instance);
    }

    // Tests escapeJava with null input
    @Test
    public void testEscapeJava_nullInput_returnsNull() {
        assertNull(StringEscapeUtils.escapeJava(null));
    }

    // Tests escapeJava with basic and boundary characters
    @Test
    public void testEscapeJava_quotesAndBackslash_returnsEscapedString() {
        assertEquals("He didn't say, \\\"Stop!\\\"", StringEscapeUtils.escapeJava("He didn't say, \"Stop!\""));
        assertEquals("backslash\\\\quote\\\"", StringEscapeUtils.escapeJava("backslash\\quote\""));
        assertEquals("", StringEscapeUtils.escapeJava(""));
    }

    // Tests escapeJava with control characters and unicode boundaries
    @Test
    public void testEscapeJava_controlAndUnicodeChars_returnsEscapedUnicode() {
        String input = "\b\t\n\f\r\u0001\u001F\u0080\u0100\u1000";
        String expected = "\\b\\t\\n\\f\\r\\u0001\\u001F\\u0080\\u0100\\u1000";
        assertEquals(expected, StringEscapeUtils.escapeJava(input));
    }

    // Tests escapeJava using Writer and null Writer exception
    @Test
    public void testEscapeJava_writerTarget_writesCorrectly() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeJava(writer, "a\nb");
        assertEquals("a\\nb", writer.toString());

        StringEscapeUtils.escapeJava(writer, null);
        assertEquals("a\\nb", writer.toString());
    }

    // Tests escapeJava with null writer throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testEscapeJava_nullWriter_throwsIllegalArgumentException() throws IOException {
        StringEscapeUtils.escapeJava(null, "text");
    }

    // Tests escapeJavaScript escaping single quotes and forward slashes
    @Test
    public void testEscapeJavaScript_singleQuoteAndSlash_returnsEscaped() {
        assertNull(StringEscapeUtils.escapeJavaScript(null));
        assertEquals("He didn\\'t say, \\\"Stop!\\\"", StringEscapeUtils.escapeJavaScript("He didn't say, \"Stop!\""));
    }

    // Tests escapeJavaScript using Writer and null Writer exception
    @Test
    public void testEscapeJavaScript_writerTarget_writesCorrectly() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeJavaScript(writer, "It's 'ok'");
        assertEquals("It\\'s \\'ok\\'", writer.toString());
    }

    // Tests escapeJavaScript with null writer throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testEscapeJavaScript_nullWriter_throwsIllegalArgumentException() throws IOException {
        StringEscapeUtils.escapeJavaScript(null, "text");
    }

    // Tests unescapeJava with null input
    @Test
    public void testUnescapeJava_nullInput_returnsNull() {
        assertNull(StringEscapeUtils.unescapeJava(null));
    }

    // Tests unescapeJava with standard escape sequences
    @Test
    public void testUnescapeJava_escapedSequences_returnsUnescapedString() {
        String input = "\\b\\t\\n\\f\\r\\\'\\\"\\\\\\a";
        String expected = "\b\t\n\f\r\'\"\\a";
        assertEquals(expected, StringEscapeUtils.unescapeJava(input));
        assertEquals("trailing slash\\", StringEscapeUtils.unescapeJava("trailing slash\\"));
    }

    // Tests unescapeJava with Unicode values
    @Test
    public void testUnescapeJava_unicodeSequence_returnsUnicodeCharacter() {
        assertEquals("A\u0041\u00AE", StringEscapeUtils.unescapeJava("A\\u0041\\u00AE"));
    }

    // Tests unescapeJava with invalid Unicode sequence throwing NestableRuntimeException
    @Test(expected = NestableRuntimeException.class)
    public void testUnescapeJava_invalidUnicode_throwsNestableRuntimeException() {
        StringEscapeUtils.unescapeJava("\\u00ZZ");
    }

    // Tests unescapeJava with null Writer
    @Test(expected = IllegalArgumentException.class)
    public void testUnescapeJava_nullWriter_throwsIllegalArgumentException() throws IOException {
        StringEscapeUtils.unescapeJava(null, "test");
    }

    // Tests unescapeJavaScript wrapper method
    @Test
    public void testUnescapeJavaScript_escapedString_returnsUnescapedString() throws IOException {
        assertNull(StringEscapeUtils.unescapeJavaScript(null));
        assertEquals("tab\tquote'double\"", StringEscapeUtils.unescapeJavaScript("tab\\tquote\\\'double\\\""));

        StringWriter writer = new StringWriter();
        StringEscapeUtils.unescapeJavaScript(writer, "a\\nb");
        assertEquals("a\nb", writer.toString());
    }

    // Tests escapeHtml and unescapeHtml
    @Test
    public void testEscapeAndUnescapeHtml_validEntities_transformsCorrectly() throws IOException {
        assertNull(StringEscapeUtils.escapeHtml(null));
        assertNull(StringEscapeUtils.unescapeHtml(null));

        String input = "\"bread\" & 'butter' < \u00A9 >";
        String escaped = StringEscapeUtils.escapeHtml(input);
        assertEquals("&quot;bread&quot; &amp; 'butter' &lt; &copy; &gt;", escaped);
        assertEquals(input, StringEscapeUtils.unescapeHtml(escaped));

        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeHtml(writer, "<Fran\u00E7ais>");
        assertEquals("&lt;Fran&ccedil;ais&gt;", writer.toString());

        StringWriter unescapeWriter = new StringWriter();
        StringEscapeUtils.unescapeHtml(unescapeWriter, "&lt;Fran&ccedil;ais&gt;");
        assertEquals("<Fran\u00E7ais>", unescapeWriter.toString());
    }

    // Tests escapeHtml and unescapeHtml with null writer
    @Test(expected = IllegalArgumentException.class)
    public void testEscapeHtml_nullWriter_throwsIllegalArgumentException() throws IOException {
        StringEscapeUtils.escapeHtml(null, "text");
    }

    // Tests escapeXml and unescapeXml
    @Test
    public void testEscapeAndUnescapeXml_validEntities_transformsCorrectly() throws IOException {
        assertNull(StringEscapeUtils.escapeXml(null));
        assertNull(StringEscapeUtils.unescapeXml(null));

        String input = "<tag attr='value & \"other\"'>\u00A9</tag>";
        String escaped = StringEscapeUtils.escapeXml(input);
        assertEquals("&lt;tag attr=&apos;value &amp; &quot;other&quot;&gt;&#169;&lt;/tag&gt;", escaped);
        assertEquals(input, StringEscapeUtils.unescapeXml(escaped));

        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeXml(writer, "<abc>");
        assertEquals("&lt;abc&gt;", writer.toString());

        StringWriter unescapeWriter = new StringWriter();
        StringEscapeUtils.unescapeXml(unescapeWriter, "&lt;abc&gt;");
        assertEquals("<abc>", unescapeWriter.toString());
    }

    // Tests escapeXml and unescapeXml with null writer
    @Test(expected = IllegalArgumentException.class)
    public void testEscapeXml_nullWriter_throwsIllegalArgumentException() throws IOException {
        StringEscapeUtils.escapeXml(null, "text");
    }

    // Tests escapeSql with null and single quote replacement
    @Test
    public void testEscapeSql_singleQuotes_escapesCorrectly() {
        assertNull(StringEscapeUtils.escapeSql(null));
        assertEquals("", StringEscapeUtils.escapeSql(""));
        assertEquals("McHale''s Navy", StringEscapeUtils.escapeSql("McHale's Navy"));
        assertEquals("NoQuotes", StringEscapeUtils.escapeSql("NoQuotes"));
        assertEquals("''''", StringEscapeUtils.escapeSql("''"));
    }
}