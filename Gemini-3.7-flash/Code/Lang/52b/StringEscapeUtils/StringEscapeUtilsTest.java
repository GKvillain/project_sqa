package org.apache.commons.lang;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.junit.Test;
import static org.junit.Assert.*;

public class StringEscapeUtilsTest {

    // Tests constructor creation
    @Test
    public void testConstructor_defaultInstantiation_notNull() {
        assertNotNull(new StringEscapeUtils());
    }

    // Tests null and empty inputs for escapeJava
    @Test
    public void testEscapeJava_nullAndEmpty_returnsSame() {
        assertNull(StringEscapeUtils.escapeJava(null));
        assertEquals("", StringEscapeUtils.escapeJava(""));
    }

    // Tests control characters, quotes, and backslashes in escapeJava
    @Test
    public void testEscapeJava_controlCharactersAndQuotes_escapesCorrectly() {
        assertEquals("\\b\\t\\n\\f\\r\\\"\\\\", StringEscapeUtils.escapeJava("\b\t\n\f\r\"\\"));
        assertEquals("He didn't say, \\\"Stop!\\\"", StringEscapeUtils.escapeJava("He didn't say, \"Stop!\""));
    }

    // Tests various unicode boundary branches in escapeJava
    @Test
    public void testEscapeJava_unicodeRanges_escapesHex() {
        assertEquals("\\u0001", StringEscapeUtils.escapeJava("\u0001"));
        assertEquals("\\u0010", StringEscapeUtils.escapeJava("\u0010"));
        assertEquals("\\u0080", StringEscapeUtils.escapeJava("\u0080"));
        assertEquals("\\u0100", StringEscapeUtils.escapeJava("\u0100"));
        assertEquals("\\u1000", StringEscapeUtils.escapeJava("\u1000"));
    }

    // Tests escapeJava with Writer and null handling
    @Test
    public void testEscapeJava_writerNormalAndNullInput_writesExpected() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeJava(writer, "test\n");
        assertEquals("test\\n", writer.toString());

        StringWriter nullInputWriter = new StringWriter();
        StringEscapeUtils.escapeJava(nullInputWriter, null);
        assertEquals("", nullInputWriter.toString());
    }

    // Tests escapeJava with null Writer throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testEscapeJava_nullWriter_throwsIllegalArgumentException() throws IOException {
        StringEscapeUtils.escapeJava((Writer) null, "test");
    }

    // Tests escapeJavaScript escaping single quotes and forward slashes
    @Test
    public void testEscapeJavaScript_singleQuoteAndSlashes_escapesCorrectly() {
        assertNull(StringEscapeUtils.escapeJavaScript(null));
        assertEquals("He didn\\'t say, \\\"Stop!\\\"", StringEscapeUtils.escapeJavaScript("He didn't say, \"Stop!\""));
        assertEquals("document.getElementById(\\'test\\')", StringEscapeUtils.escapeJavaScript("document.getElementById('test')"));
    }

    // Tests escapeJavaScript with Writer
    @Test
    public void testEscapeJavaScript_writerValidAndNull_behavesCorrectly() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeJavaScript(writer, "'hello'");
        assertEquals("\\'hello\\'", writer.toString());

        StringWriter nullStrWriter = new StringWriter();
        StringEscapeUtils.escapeJavaScript(nullStrWriter, null);
        assertEquals("", nullStrWriter.toString());
    }

    // Tests escapeJavaScript with null Writer throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testEscapeJavaScript_nullWriter_throwsIllegalArgumentException() throws IOException {
        StringEscapeUtils.escapeJavaScript((Writer) null, "test");
    }

    // Tests unescapeJava with null and normal escaped characters
    @Test
    public void testUnescapeJava_escapedCharacters_unescapesCorrectly() {
        assertNull(StringEscapeUtils.unescapeJava(null));
        assertEquals("", StringEscapeUtils.unescapeJava(""));
        assertEquals("\b\t\n\f\r'\"\\", StringEscapeUtils.unescapeJava("\\b\\t\\n\\f\\r\\'\\\"\\\\"));
        assertEquals("abc", StringEscapeUtils.unescapeJava("abc"));
        assertEquals("a", StringEscapeUtils.unescapeJava("\\a"));
    }

    // Tests unescapeJava unicode parsing and trailing slash branch
    @Test
    public void testUnescapeJava_unicodeAndTrailingSlash_unescapesCorrectly() {
        assertEquals("\u0041\u1234", StringEscapeUtils.unescapeJava("\\u0041\\u1234"));
        assertEquals("trailing\\", StringEscapeUtils.unescapeJava("trailing\\"));
    }

    // Tests unescapeJava invalid unicode handling throwing NestableRuntimeException
    @Test(expected = NestableRuntimeException.class)
    public void testUnescapeJava_invalidUnicode_throwsNestableRuntimeException() {
        StringEscapeUtils.unescapeJava("\\u00ZZ");
    }

    // Tests unescapeJava with Writer and null handling
    @Test
    public void testUnescapeJava_writerValidAndNullInput_writesExpected() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.unescapeJava(writer, "\\n");
        assertEquals("\n", writer.toString());

        StringWriter nullStrWriter = new StringWriter();
        StringEscapeUtils.unescapeJava(nullStrWriter, null);
        assertEquals("", nullStrWriter.toString());
    }

    // Tests unescapeJava with null Writer throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testUnescapeJava_nullWriter_throwsIllegalArgumentException() throws IOException {
        StringEscapeUtils.unescapeJava((Writer) null, "test");
    }

    // Tests unescapeJavaScript delegations
    @Test
    public void testUnescapeJavaScript_escapedLiterals_unescapesCorrectly() throws IOException {
        assertNull(StringEscapeUtils.unescapeJavaScript(null));
        assertEquals("He didn't say, \"Stop!\"", StringEscapeUtils.unescapeJavaScript("He didn\\'t say, \\\"Stop!\\\""));

        StringWriter writer = new StringWriter();
        StringEscapeUtils.unescapeJavaScript(writer, "\\'test\\'");
        assertEquals("'test'", writer.toString());

        StringWriter nullStrWriter = new StringWriter();
        StringEscapeUtils.unescapeJavaScript(nullStrWriter, null);
        assertEquals("", nullStrWriter.toString());
    }

    // Tests unescapeJavaScript with null Writer throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testUnescapeJavaScript_nullWriter_throwsIllegalArgumentException() throws IOException {
        StringEscapeUtils.unescapeJavaScript((Writer) null, "test");
    }

    // Tests escapeHtml and unescapeHtml methods
    @Test
    public void testEscapeAndUnescapeHtml_validAndNull_convertsProperly() throws IOException {
        assertNull(StringEscapeUtils.escapeHtml(null));
        assertNull(StringEscapeUtils.unescapeHtml(null));

        String raw = "\"bread\" & 'butter' <tag>";
        String escaped = StringEscapeUtils.escapeHtml(raw);
        assertEquals("&quot;bread&quot; &amp; 'butter' &lt;tag&gt;", escaped);
        assertEquals(raw, StringEscapeUtils.unescapeHtml(escaped));

        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeHtml(writer, "<b>");
        assertEquals("&lt;b&gt;", writer.toString());

        StringWriter unescapeWriter = new StringWriter();
        StringEscapeUtils.unescapeHtml(unescapeWriter, "&lt;b&gt;");
        assertEquals("<b>", unescapeWriter.toString());

        StringWriter nullWriter = new StringWriter();
        StringEscapeUtils.escapeHtml(nullWriter, null);
        StringEscapeUtils.unescapeHtml(nullWriter, null);
        assertEquals("", nullWriter.toString());
    }

    // Tests escapeHtml and unescapeHtml null Writer exceptions
    @Test(expected = IllegalArgumentException.class)
    public void testEscapeHtml_nullWriter_throwsIllegalArgumentException() throws IOException {
        StringEscapeUtils.escapeHtml(null, "test");
    }

    // Tests unescapeHtml null Writer exception
    @Test(expected = IllegalArgumentException.class)
    public void testUnescapeHtml_nullWriter_throwsIllegalArgumentException() throws IOException {
        StringEscapeUtils.unescapeHtml(null, "test");
    }

    // Tests escapeXml and unescapeXml methods
    @Test
    public void testEscapeAndUnescapeXml_validAndNull_convertsProperly() throws IOException {
        assertNull(StringEscapeUtils.escapeXml(null));
        assertNull(StringEscapeUtils.unescapeXml(null));

        String raw = "<abc>&\"'</abc>";
        String escaped = StringEscapeUtils.escapeXml(raw);
        assertEquals("&lt;abc&gt;&amp;&quot;&apos;&lt;/abc&gt;", escaped);
        assertEquals(raw, StringEscapeUtils.unescapeXml(escaped));

        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeXml(writer, "<foo>");
        assertEquals("&lt;foo&gt;", writer.toString());

        StringWriter unescapeWriter = new StringWriter();
        StringEscapeUtils.unescapeXml(unescapeWriter, "&lt;foo&gt;");
        assertEquals("<foo>", unescapeWriter.toString());

        StringWriter nullWriter = new StringWriter();
        StringEscapeUtils.escapeXml(nullWriter, null);
        StringEscapeUtils.unescapeXml(nullWriter, null);
        assertEquals("", nullWriter.toString());
    }

    // Tests escapeXml null Writer exception
    @Test(expected = IllegalArgumentException.class)
    public void testEscapeXml_nullWriter_throwsIllegalArgumentException() throws IOException {
        StringEscapeUtils.escapeXml(null, "test");
    }

    // Tests unescapeXml null Writer exception
    @Test(expected = IllegalArgumentException.class)
    public void testUnescapeXml_nullWriter_throwsIllegalArgumentException() throws IOException {
        StringEscapeUtils.unescapeXml(null, "test");
    }

    // Tests escapeSql method
    @Test
    public void testEscapeSql_singleQuotesAndNull_escapesCorrectly() {
        assertNull(StringEscapeUtils.escapeSql(null));
        assertEquals("", StringEscapeUtils.escapeSql(""));
        assertEquals("McHale''s Navy", StringEscapeUtils.escapeSql("McHale's Navy"));
        assertEquals("NoQuotes", StringEscapeUtils.escapeSql("NoQuotes"));
        assertEquals("''''", StringEscapeUtils.escapeSql("''"));
    }
}