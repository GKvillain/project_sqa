package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class DataUtilTest {

    private static ByteBuffer byteBuffer(String data, String charset) {
        return ByteBuffer.wrap(data.getBytes(Charset.forName(charset)));
    }

    private static ByteBuffer utf8(String data) {
        return byteBuffer(data, "UTF-8");
    }

    // Tests null content type
    @Test
    public void testGetCharsetFromContentType_nullInput_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    // Tests normal quoted charset extraction
    @Test
    public void testGetCharsetFromContentType_quotedCharset_returnsCharset() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=\"UTF-8\""));
    }

    // Tests unsupported charset returning null
    @Test
    public void testGetCharsetFromContentType_unsupportedCharset_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=NoSuchCharset-1"));
    }

    // Tests content type without a charset
    @Test
    public void testGetCharsetFromContentType_noCharset_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
    }

    // Tests reading exactly maxSize bytes
    @Test
    public void testReadToByteBuffer_maxSizeEqualToStream_returnsAllBytes() throws IOException {
        ByteBuffer buffer = DataUtil.readToByteBuffer(new ByteArrayInputStream(new byte[]{1, 2}), 2);
        assertEquals(2, buffer.remaining());
        assertEquals(1, buffer.get(0));
        assertEquals(2, buffer.get(1));
    }

    // Tests truncating when maxSize is less than stream length
    @Test
    public void testReadToByteBuffer_maxSizeLessThanStream_truncates() throws IOException {
        ByteBuffer buffer = DataUtil.readToByteBuffer(new ByteArrayInputStream(new byte[]{1, 2, 3}), 2);
        assertEquals(2, buffer.remaining());
        assertEquals(1, buffer.get(0));
        assertEquals(2, buffer.get(1));
    }

    // Tests maxSize 0 meaning unlimited
    @Test
    public void testReadToByteBuffer_zeroMaxSize_returnsAllBytes() throws IOException {
        ByteBuffer buffer = DataUtil.readToByteBuffer(new ByteArrayInputStream(new byte[]{1, 2, 3}), 0);
        assertEquals(3, buffer.remaining());
    }

    // Tests invalid negative maxSize
    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBuffer_negativeMaxSize_throws() throws IOException {
        DataUtil.readToByteBuffer(new ByteArrayInputStream(new byte[]{1}), -1);
    }

    // Tests UTF-8 parsing when no charset is specified
    @Test
    public void testParseByteData_nullCharsetUtf8_parsesDocument() {
        Document doc = DataUtil.parseByteData(utf8("<html><body><p>Hello</p></body></html>"), null, "http://example.com", Parser.htmlParser());
        assertEquals("Hello", doc.text());
    }

    // Tests a UTF-8 meta charset does not trigger re-decoding
    @Test
    public void testParseByteData_nullCharsetMetaUtf8_doesNotReDecode() {
        String html = "<html><head><meta charset=\"UTF-8\"></head><body><p>Hello</p></body></html>";
        Document doc = DataUtil.parseByteData(utf8(html), null, "http://example.com", Parser.htmlParser());
        assertEquals("Hello", doc.text());
    }

    // Tests HTML5 meta charset detection and re-decode
    @Test
    public void testParseByteData_nullCharsetHtml5Meta_decodesWithFoundCharset() {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body><p>caf\u00e9</p></body></html>";
        Document doc = DataUtil.parseByteData(byteBuffer(html, "ISO-8859-1"), null, "http://example.com", Parser.htmlParser());
        assertEquals("caf\u00e9", doc.text());
    }

    // Tests http-equiv meta content type charset detection
    @Test
    public void testParseByteData_nullCharsetHttpEquiv_decodesWithFoundCharset() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head><body><p>caf\u00e9</p></body></html>";
        Document doc = DataUtil.parseByteData(byteBuffer(html, "ISO-8859-1"), null, "http://example.com", Parser.htmlParser());
        assertEquals("caf\u00e9", doc.text());
    }

    // Tests fallback to charset attribute when content type charset is unsupported
    @Test
    public void testParseByteData_nullCharsetHttpEquivUnsupportedType_charsetAttrFallback() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=NoSuchCharset-1\" charset=\"ISO-8859-1\"></head><body><p>caf\u00e9</p></body></html>";
        Document doc = DataUtil.parseByteData(byteBuffer(html, "ISO-8859-1"), null, "http://example.com", Parser.htmlParser());
        assertEquals("caf\u00e9", doc.text());
    }

    // Tests explicit charset argument parsing
    @Test
    public void testParseByteData_specifiedCharset_decodesWithCharset() {
        String html = "<html><body><p>caf\u00e9</p></body></html>";
        Document doc = DataUtil.parseByteData(byteBuffer(html, "ISO-8859-1"), "ISO-8859-1", "http://example.com", Parser.htmlParser());
        assertEquals("caf\u00e9", doc.text());
    }

    // Tests empty charset argument validation
    @Test(expected = IllegalArgumentException.class)
    public void testParseByteData_emptyCharset_throws() {
        DataUtil.parseByteData(utf8("<html></html>"), "", "http://example.com", Parser.htmlParser());
    }

    // Regression test: UTF-8 BOM must be stripped before parsing so the initial
    // parse result is discarded and the document is re-parsed cleanly.
    @Test
    public void testParseByteData_utf8BomWithDoctype_stripsBomAndReparses() {
        String html = "<!DOCTYPE html><html><body><p>Hello</p></body></html>";
        ByteBuffer data = ByteBuffer.wrap(("\ufeff" + html).getBytes(Charset.forName("UTF-8")));

        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());

        assertFalse("BOM should not remain in parsed document", doc.outerHtml().contains("\ufeff"));
        assertTrue("Document should be re-parsed so the doctype is kept",
                doc.childNodes().get(0).outerHtml().toLowerCase().contains("doctype"));
        assertEquals("Hello", doc.text());
    }

    // Tests load(InputStream, String, String)
    @Test
    public void testLoad_inputStream_parsesDocument() throws IOException {
        Document doc = DataUtil.load(new ByteArrayInputStream(utf8("<html><body><p>Hello</p></body></html>").array()), null, "http://example.com");
        assertEquals("Hello", doc.text());
    }

    // Tests load(InputStream, String, String, Parser)
    @Test
    public void testLoad_inputStreamWithXmlParser_parsesDocument() throws IOException {
        Document doc = DataUtil.load(new ByteArrayInputStream(utf8("<html><body><p>Hello</p></body></html>").array()), "UTF-8", "http://example.com", Parser.xmlParser());
        assertEquals("Hello", doc.text());
    }

    // Tests load(File, String, String)
    @Test
    public void testLoad_file_parsesDocument() throws IOException {
        File file = File.createTempFile("jsoup-datautil-test", ".html");
        try {
            FileOutputStream out = new FileOutputStream(file);
            try {
                out.write(utf8("<html><body><p>Hello</p></body></html>").array());
            } finally {
                out.close();
            }
            Document doc = DataUtil.load(file, "UTF-8", "http://example.com");
            assertEquals("Hello", doc.text());
        } finally {
            file.delete();
        }
    }
}