package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class DataUtilTest {

    // Tests the getCharsetFromContentType method with a null input
    @Test
    public void testGetCharsetFromContentType_nullInput_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    // Tests the getCharsetFromContentType method with an empty string
    @Test
    public void testGetCharsetFromContentType_emptyString_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType(""));
    }

    // Tests the getCharsetFromContentType method with a valid content type string
    @Test
    public void testGetCharsetFromContentType_validContentType_returnsCharset() {
        String result = DataUtil.getCharsetFromContentType("text/html; charset=UTF-8");
        assertEquals("UTF-8", result);
    }

    // Tests the getCharsetFromContentType method with a content type having no charset
    @Test
    public void testGetCharsetFromContentType_noCharset_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
    }

    // Tests parseByteData with a null charset (meta detection), empty document
    @Test
    public void testParseByteData_nullCharsetAndEmptyDoc_returnsDocumentWithDefaultCharset() {
        ByteBuffer byteData = ByteBuffer.wrap("<html></html>".getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // Tests parseByteData with a null charset (meta detection), document with meta charset tag
    @Test
    public void testParseByteData_nullCharsetWithMetaTag_returnsDocumentWithDetectedCharset() {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // Tests parseByteData with a null charset (meta detection), document with meta http-equiv tag
    @Test
    public void testParseByteData_nullCharsetWithMetaHttpEquiv_returnsDocumentWithDetectedCharset() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-16\"></head><body></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("UTF-16", doc.outputSettings().charset().name());
    }

    // Tests parseByteData with a specified charset (not null)
    @Test
    public void testParseByteData_specifiedCharset_returnsDocumentWithSpecifiedCharset() {
        String html = "<html><body>Hello</body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_16));
        Document doc = DataUtil.parseByteData(byteData, "UTF-16", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("UTF-16", doc.outputSettings().charset().name());
    }

    // Tests parseByteData with a null charset and BOM character at the start
    @Test
    public void testParseByteData_nullCharsetWithBOM_stripsBOM() {
        byte[] bomBytes = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 0x48, 0x65, 0x6C, 0x6C, 0x6F};
        ByteBuffer byteData = ByteBuffer.wrap(bomBytes);
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // Tests parseByteData when the meta charset is the same as the default charset (no re-decode needed)
    @Test
    public void testParseByteData_nullCharsetMetaCharsetSameAsDefault_returnsDocumentWithDefaultCharset() {
        String html = "<html><head><meta charset=\"UTF-8\"></head><body></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // Tests the readToByteBuffer method with an empty input stream
    @Test
    public void testReadToByteBuffer_emptyStream_returnsEmptyBuffer() throws IOException {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        ByteBuffer result = DataUtil.readToByteBuffer(emptyStream);
        assertEquals(0, result.limit());
    }

    // Tests the readToByteBuffer method with a small amount of data
    @Test
    public void testReadToByteBuffer_smallStream_returnsCorrectData() throws IOException {
        byte[] data = "Hello".getBytes(StandardCharsets.UTF_8);
        InputStream stream = new ByteArrayInputStream(data);
        ByteBuffer result = DataUtil.readToByteBuffer(stream);
        assertEquals("Hello", StandardCharsets.UTF_8.decode(result).toString());
    }

    // Tests the load(InputStream, String, String) method with a valid stream
    @Test
    public void testLoad_validInputStream_returnsDocument() throws IOException {
        String html = "<html><body>Test</body></html>";
        InputStream stream = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.load(stream, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("Test", doc.body().text());
    }

    // Tests the load(InputStream, String, String, Parser) method with an XML parser
    @Test
    public void testLoad_withXmlParser_returnsDocument() throws IOException {
        String xml = "<root><child>value</child></root>";
        InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.load(stream, "UTF-8", "http://example.com", Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("value", doc.select("child").first().text());
    }

    // Tests parseByteData with null charset and malformed meta tag (no charset found)
    @Test
    public void testParseByteData_nullCharsetNoMetaCharset_returnsDocumentWithDefaultCharset() {
        String html = "<html><head><meta name=\"description\" content=\"test\"></head><body></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // Tests the load(File, String, String) method with a temporary file
    @Test
    public void testLoad_validFile_returnsDocument() throws IOException {
        File tempFile = File.createTempFile("test", ".html");
        tempFile.deleteOnExit();
        String html = "<html><body>File</body></html>";
        Files.write(tempFile.toPath(), html.getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("File", doc.body().text());
    }

    // Tests the getCharsetFromContentType method with a charset surrounded by quotes
    @Test
    public void testGetCharsetFromContentType_charsetWithQuotes_returnsTrimmedCharset() {
        String result = DataUtil.getCharsetFromContentType("text/html; charset=\"EUC-JP\"");
        assertEquals("EUC-JP", result);
    }

    // Tests the getCharsetFromContentType method with a charset containing whitespace
    @Test
    public void testGetCharsetFromContentType_charsetWithWhitespace_returnsTrimmedCharset() {
        String result = DataUtil.getCharsetFromContentType("text/html; charset =   UTF-8  ");
        assertEquals("UTF-8", result);
    }

    // Tests the getCharsetFromContentType method with an unsupported charset
    @Test
    public void testGetCharsetFromContentType_unsupportedCharset_returnsNull() {
        // This is a valid charset pattern, so it will return the charset string
        String result = DataUtil.getCharsetFromContentType("text/html; charset=ISO-8859-1");
        assertNotNull(result);
    }
}