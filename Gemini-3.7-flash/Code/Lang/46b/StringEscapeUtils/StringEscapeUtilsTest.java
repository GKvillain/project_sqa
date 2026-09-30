package org.apache.commons.lang;

import java.io.IOException;
import java.io.StringWriter;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for {@link StringEscapeUtils}.
 */
public class StringEscapeUtilsTest {

    // Tests constructor
    @Test
    public void testConstructor_default_instanceCreated() {
        assertNotNull(new StringEscapeUtils());
    }

    // Tests Java escaping with slashes, quotes, and control chars
    @Test
    public void testEscapeJava_slashAndQuotes_escapedProperly() {
        assertNull(StringEscapeUtils.escapeJava(null));
        assertEquals("", StringEscapeUtils.escapeJava(""));
        assertEquals("He didn't say, \\\"Stop!\\\"", StringEscapeUtils.escapeJava("He didn't say, \"Stop!\""));
        assertEquals("tab:\\t cr:\\r lf:\\n ff:\\f bs:\\b backslash:\\\\", 
                StringEscapeUtils.escapeJava("tab:\t cr:\r lf:\n ff:\f bs:\b backslash:\\"));
        assertEquals("test/slash", StringEscapeUtils.escapeJava("test/slash"));
    }

    // Tests Java escaping with unicode characters
    @Test
    public void testEscapeJava_unicodeCharacters_escapedProperly() {
        assertEquals("\\u0001", StringEscapeUtils.escapeJava("\u0001"));
        assertEquals("\\u001F", StringEscapeUtils.escapeJava("\u001f"));
        assertEquals("\\u007F", StringEscapeUtils.escapeJava("\u007f"));
        assertEquals("\\u00A0", StringEscapeUtils.escapeJava("\u00a0"));
        assertEquals("\\u1234", StringEscapeUtils.escapeJava("\u1234"));
    }

    // Tests Java escaping to Writer
    @Test
    public void testEscapeJava_writer_writesEscapedString() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeJava(writer, "test\n");
        assertEquals("test\\n", writer.toString());

        StringEscapeUtils.escapeJava(writer, (String) null);
        assertEquals("test\\n", writer.toString());
    }

    // Tests Java escaping to null Writer throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testEscapeJava_nullWriter_throwsException() throws IOException {
        StringEscapeUtils.escapeJava(null, "test");
    }

    // Tests JavaScript escaping including single quote and forward slash
    @Test
    public void testEscapeJavaScript_specialChars_escapedProperly() throws IOException {
        assertNull(StringEscapeUtils.escapeJavaScript(null));
        assertEquals("He didn\\'t say, \\\"Stop!\\\"", StringEscapeUtils.escapeJavaScript("He didn't say, \"Stop!\""));
        assertEquals("test\\/slash", StringEscapeUtils.escapeJavaScript("test/slash"));

        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeJavaScript(writer, "a'b");
        assertEquals("a\\'b", writer.toString());
    }

    // Tests Java unescaping standard escape sequences and trailing slash
    @Test
    public void testUnescapeJava_validEscapes_returnsUnescapedString() {
        assertNull(StringEscapeUtils.unescapeJava(null));
        assertEquals("", StringEscapeUtils.unescapeJava(""));
        assertEquals("tab:\t cr:\r lf:\n ff:\f bs:\b quote:\" apos:' bs:\\", 
                StringEscapeUtils.unescapeJava("tab:\\t cr:\\r lf:\\n ff:\\f bs:\\b quote:\\\" apos:\\' bs:\\\\"));
        assertEquals("abc\\", StringEscapeUtils.unescapeJava("abc\\"));
        assertEquals("regular string", StringEscapeUtils.unescapeJava("regular string"));
    }

    // Tests Java unescaping unicode characters
    @Test
    public void testUnescapeJava_unicode_returnsDecodedCharacters() {
        assertEquals("\u0041\u0042\u1234", StringEscapeUtils.unescapeJava("\\u0041\\u0042\\u1234"));
    }

    // Tests Java unescaping invalid unicode throws exception
    @Test(expected = NestableRuntimeException.class)
    public void testUnescapeJava_invalidUnicode_throwsException() {
        StringEscapeUtils.unescapeJava("\\u00ZZ");
    }

    // Tests Java unescaping to Writer and null Writer exception
    @Test
    public void testUnescapeJava_writer_writesUnescapedString() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.unescapeJava(writer, "line1\\nline2");
        assertEquals("line1\nline2", writer.toString());

        StringEscapeUtils.unescapeJava(writer, (String) null);
        assertEquals("line1\nline2", writer.toString());
    }

    // Tests unescapeJava with null Writer throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testUnescapeJava_nullWriter_throwsException() throws IOException {
        StringEscapeUtils.unescapeJava(null, "test");
    }

    // Tests JavaScript unescaping
    @Test
    public void testUnescapeJavaScript_escapedString_unescapedCorrectly() throws IOException {
        assertNull(StringEscapeUtils.unescapeJavaScript(null));
        assertEquals("test\n", StringEscapeUtils.unescapeJavaScript("test\\n"));

        StringWriter writer = new StringWriter();
        StringEscapeUtils.unescapeJavaScript(writer, "test\\t");
        assertEquals("test\t", writer.toString());
    }

    // Tests HTML escaping and unescaping
    @Test
    public void testEscapeAndUnescapeHtml_entities_convertedCorrectly() throws IOException {
        assertNull(StringEscapeUtils.escapeHtml(null));
        assertEquals("&quot;bread&quot; &amp; &quot;butter&quot;", StringEscapeUtils.escapeHtml("\"bread\" & \"butter\""));
        assertEquals("&lt;Fran&ccedil;ais&gt;", StringEscapeUtils.escapeHtml("<Fran\u00e7ais>"));

        assertNull(StringEscapeUtils.unescapeHtml(null));
        assertEquals("\"bread\" & \"butter\"", StringEscapeUtils.unescapeHtml("&quot;bread&quot; &amp; &quot;butter&quot;"));
        assertEquals("<Fran\u00e7ais>", StringEscapeUtils.unescapeHtml("&lt;Fran&ccedil;ais&gt;"));

        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeHtml(writer, "<a>");
        assertEquals("&lt;a&gt;", writer.toString());

        StringWriter unescapeWriter = new StringWriter();
        StringEscapeUtils.unescapeHtml(unescapeWriter, "&lt;a&gt;");
        assertEquals("<a>", unescapeWriter.toString());

        StringEscapeUtils.escapeHtml(writer, (String) null);
        StringEscapeUtils.unescapeHtml(unescapeWriter, (String) null);
    }

    // Tests HTML escape with null Writer throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testEscapeHtml_nullWriter_throwsException() throws IOException {
        StringEscapeUtils.escapeHtml(null, "test");
    }

    // Tests HTML unescape with null Writer throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testUnescapeHtml_nullWriter_throwsException() throws IOException {
        StringEscapeUtils.unescapeHtml(null, "&amp;");
    }

    // Tests XML escaping and unescaping
    @Test
    public void testEscapeAndUnescapeXml_entities_convertedCorrectly() throws IOException {
        assertNull(StringEscapeUtils.escapeXml(null));
        assertEquals("&quot;bread&quot; &amp; &apos;butter&apos; &lt;&gt;", 
                StringEscapeUtils.escapeXml("\"bread\" & 'butter' <>"));

        assertNull(StringEscapeUtils.unescapeXml(null));
        assertEquals("\"bread\" & 'butter' <>", 
                StringEscapeUtils.unescapeXml("&quot;bread&quot; &amp; &apos;butter&apos; &lt;&gt;"));

        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeXml(writer, "<foo>");
        assertEquals("&lt;foo&gt;", writer.toString());

        StringWriter unescapeWriter = new StringWriter();
        StringEscapeUtils.unescapeXml(unescapeWriter, "&lt;foo&gt;");
        assertEquals("<foo>", unescapeWriter.toString());

        StringEscapeUtils.escapeXml(writer, (String) null);
        StringEscapeUtils.unescapeXml(unescapeWriter, (String) null);
    }

    // Tests XML escape with null Writer throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testEscapeXml_nullWriter_throwsException() throws IOException {
        StringEscapeUtils.escapeXml(null, "test");
    }

    // Tests XML unescape with null Writer throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testUnescapeXml_nullWriter_throwsException() throws IOException {
        StringEscapeUtils.unescapeXml(null, "&lt;");
    }

    // Tests SQL escaping
    @Test
    public void testEscapeSql_singleQuotes_escapedCorrectly() {
        assertNull(StringEscapeUtils.escapeSql(null));
        assertEquals("plain text", StringEscapeUtils.escapeSql("plain text"));
        assertEquals("McHale''s Navy", StringEscapeUtils.escapeSql("McHale's Navy"));
        assertEquals("''test''", StringEscapeUtils.escapeSql("'test'"));
    }

    // Tests CSV escaping
    @Test
    public void testEscapeCsv_variousInputs_escapedCorrectly() throws IOException {
        assertNull(StringEscapeUtils.escapeCsv(null));
        assertEquals("simple", StringEscapeUtils.escapeCsv("simple"));
        assertEquals("\"a,b\"", StringEscapeUtils.escapeCsv("a,b"));
        assertEquals("\"a\nb\"", StringEscapeUtils.escapeCsv("a\nb"));
        assertEquals("\"a\rb\"", StringEscapeUtils.escapeCsv("a\rb"));
        assertEquals("\"a\"\"b\"", StringEscapeUtils.escapeCsv("a\"b"));

        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeCsv(writer, "a,b");
        assertEquals("\"a,b\"", writer.toString());

        StringWriter plainWriter = new StringWriter();
        StringEscapeUtils.escapeCsv(plainWriter, "plain");
        assertEquals("plain", plainWriter.toString());

        StringEscapeUtils.escapeCsv(plainWriter, (String) null);
    }

    // Tests CSV unescaping
    @Test
    public void testUnescapeCsv_variousInputs_unescapedCorrectly() throws IOException {
        assertNull(StringEscapeUtils.unescapeCsv(null));
        assertEquals("", StringEscapeUtils.unescapeCsv(""));
        assertEquals("a", StringEscapeUtils.unescapeCsv("a"));
        assertEquals("plain", StringEscapeUtils.unescapeCsv("plain"));
        assertEquals("a,b", StringEscapeUtils.unescapeCsv("\"a,b\""));
        assertEquals("a\"b", StringEscapeUtils.unescapeCsv("\"a\"\"b\""));
        assertEquals("a\nb", StringEscapeUtils.unescapeCsv("\"a\nb\""));
        assertEquals("\"not csv content\"", StringEscapeUtils.unescapeCsv("\"not csv content\""));

        StringWriter writer = new StringWriter();
        StringEscapeUtils.unescapeCsv(writer, "\"a,b\"");
        assertEquals("a,b", writer.toString());

        StringWriter shortWriter = new StringWriter();
        StringEscapeUtils.unescapeCsv(shortWriter, "x");
        assertEquals("x", shortWriter.toString());

        StringEscapeUtils.unescapeCsv(writer, (String) null);
    }
}