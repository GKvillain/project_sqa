package org.jsoup.helper;

import org.junit.Test;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.junit.Assert.*;

/**
 * JUnit 4 test class for DataUtil, targeting Defects4J bug 20b.
 * Tests cover normal, boundary, edge, branch, and regression scenarios.
 */
public class DataUtilTest {

    // Tests getCharsetFromContentType with valid content type containing charset
    @Test
    public void testGetCharsetFromContentType_validContentType_returnsCharset() {
        String result = DataUtil.getCharsetFromContentType("text/html; charset=EUC-JP");
        assertEquals("EUC-JP", result);
    }

    // Tests getCharsetFromContentType with charset in different case
    @Test
    public void testGetCharsetFromContentType_mixedCaseCharset_returnsUpperCase() {
        String result = DataUtil.getCharsetFromContentType("text/html; charset=utf-8");
        assertEquals("UTF-8", result);
    }

    // Tests getCharsetFromContentType with null content type
    @Test
    public void testGetCharsetFromContentType_nullInput_returnsNull() {
        String result = DataUtil.getCharsetFromContentType(null);
        assertNull(result);
    }

    // Tests getCharsetFromContentType with empty string
    @Test
    public void testGetCharsetFromContentType_emptyString_returnsNull() {
        String result = DataUtil.getCharsetFromContentType("");
        assertNull(result);
    }

    // Tests getCharsetFromContentType with no charset in content type
    @Test
    public void testGetCharsetFromContentType_noCharset_returnsNull() {
        String result = DataUtil.getCharsetFromContentType("text/html");
        assertNull(result);
    }

    // Tests parseByteData with null charsetName and no meta charset (default UTF-8)
    @Test
    public void testParseByteData_nullCharsetNoMeta_usesDefaultCharset() {
        String html = "<html><head></head><body>Hello</body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
    }

    // Tests parseByteData with null charsetName but meta charset found (should re-decode)
    @Test
    public void testParseByteData_nullCharsetWithMeta_usesMetaCharset() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head><body>Hello</body></html>";
        // Encode as ISO-8859-1 to test re-decode path
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
    }

    // Tests parseByteData with specified charsetName (from header)
    @Test
    public void testParseByteData_specifiedCharset_usesGivenCharset() {
        String html = "<html><body>Data</body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(buffer, "UTF-8", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Data", doc.body().text());
    }

    // Tests parseByteData with BOM (byte-order-mark) stripping
    @Test
    public void testParseByteData_withBOM_stripsBOM() {
        // UTF-8 BOM: 0xEF, 0xBB, 0xBF
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        String html = "<html><body>BOM test</body></html>";
        byte[] combined = new byte[bom.length + html.getBytes(Charset.forName("UTF-8")).length];
        System.arraycopy(bom, 0, combined, 0, bom.length);
        System.arraycopy(html.getBytes(Charset.forName("UTF-8")), 0, combined, bom.length, html.getBytes(Charset.forName("UTF-8")).length);
        ByteBuffer buffer = ByteBuffer.wrap(combined);
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("BOM test", doc.body().text());
    }

    // Tests readToByteBuffer with valid input stream
    @Test
    public void testReadToByteBuffer_validInputStream_returnsByteBuffer() throws IOException {
        String data = "test data";
        InputStream in = new ByteArrayInputStream(data.getBytes(Charset.forName("UTF-8")));
        ByteBuffer buffer = DataUtil.readToByteBuffer(in);
        assertNotNull(buffer);
        String result = new String(buffer.array(), Charset.forName("UTF-8"));
        assertEquals("test data", result);
    }

    // Tests readToByteBuffer with empty input stream
    @Test
    public void testReadToByteBuffer_emptyInputStream_returnsEmptyBuffer() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ByteBuffer buffer = DataUtil.readToByteBuffer(in);
        assertNotNull(buffer);
        assertEquals(0, buffer.remaining());
    }

    // Tests load(InputStream, String, String) with valid input
    @Test
    public void testLoadInputStream_validInput_returnsDocument() throws IOException {
        String html = "<html><body>Hello</body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
    }

    // Tests load(InputStream, String, String, Parser) with XML parser
    @Test
    public void testLoadInputStreamWithParser_validInput_returnsDocument() throws IOException {
        String html = "<root><item>data</item></root>";
        InputStream in = new ByteArrayInputStream(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("data", doc.text());
    }

    // Tests parseByteData with empty charset name in meta (should not re-decode)
    @Test
    public void testParseByteData_nullCharsetEmptyMeta_keepsDefaultCharset() {
        String html = "<html><head><meta charset=\"\"></head><body>Hi</body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hi", doc.body().text());
    }

    // Tests getCharsetFromContentType with quoted charset
    @Test
    public void testGetCharsetFromContentType_quotedCharset_returnsCharset() {
        String result = DataUtil.getCharsetFromContentType("text/html; charset=\"UTF-8\"");
        assertEquals("UTF-8", result);
    }

    // Tests parseByteData with charsetName same as default (should not re-decode)
    @Test
    public void testParseByteData_nullCharsetMetaSameAsDefault_keepsDefault() {
        String html = "<html><head><meta charset=\"UTF-8\"></head><body>Same</body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Same", doc.body().text());
    }
}