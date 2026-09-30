package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class DataUtilTest {

    // Tests getCharsetFromContentType with standard content type header
    @Test
    public void testGetCharsetFromContentType_validContentType_returnsCharset() {
        String charset = DataUtil.getCharsetFromContentType("text/html; charset=utf-8");
        assertEquals("utf-8", charset);
    }

    // Tests getCharsetFromContentType with quotes surrounding charset
    @Test
    public void testGetCharsetFromContentType_quotedCharset_returnsCharset() {
        String charset = DataUtil.getCharsetFromContentType("text/html; charset=\"ISO-8859-1\"");
        assertEquals("ISO-8859-1", charset);
    }

    // Tests getCharsetFromContentType with null input
    @Test
    public void testGetCharsetFromContentType_nullInput_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    // Tests getCharsetFromContentType with no charset parameter
    @Test
    public void testGetCharsetFromContentType_noCharset_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
    }

    // Tests getCharsetFromContentType with empty charset parameter
    @Test
    public void testGetCharsetFromContentType_emptyCharset_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset="));
    }

    // Tests getCharsetFromContentType with unsupported charset
    @Test
    public void testGetCharsetFromContentType_unsupportedCharset_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=invalid-charset-xyz"));
    }

    // Tests emptyByteBuffer returns empty byte buffer
    @Test
    public void testEmptyByteBuffer_returnsZeroLengthBuffer() {
        ByteBuffer buffer = DataUtil.emptyByteBuffer();
        assertEquals(0, buffer.remaining());
        assertEquals(0, buffer.capacity());
    }

    // Tests mimeBoundary produces boundary of configured length
    @Test
    public void testMimeBoundary_generatesCorrectLengthString() {
        String boundary = DataUtil.mimeBoundary();
        assertNotNull(boundary);
        assertEquals(DataUtil.boundaryLength, boundary.length());
    }

    // Tests crossStreams copies all bytes from input to output stream
    @Test
    public void testCrossStreams_copiesDataSuccessfully() throws IOException {
        byte[] inputData = "Hello World across streams".getBytes("UTF-8");
        ByteArrayInputStream in = new ByteArrayInputStream(inputData);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DataUtil.crossStreams(in, out);
        assertArrayEquals(inputData, out.toByteArray());
    }

    // Tests readToByteBuffer with unlimited size (maxSize = 0)
    @Test
    public void testReadToByteBuffer_unlimitedSize_readsEntireStream() throws IOException {
        byte[] data = "Test data stream reading".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);

        ByteBuffer buffer = DataUtil.readToByteBuffer(in, 0);
        assertEquals(data.length, buffer.remaining());
        assertEquals("Test data stream reading", new String(buffer.array(), 0, buffer.limit(), "UTF-8"));
    }

    // Tests readToByteBuffer single argument overload
    @Test
    public void testReadToByteBuffer_defaultMethod_readsEntireStream() throws IOException {
        byte[] data = "Short string".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);

        ByteBuffer buffer = DataUtil.readToByteBuffer(in);
        assertEquals(data.length, buffer.remaining());
    }

    // Tests readToByteBuffer with capped max size
    @Test
    public void testReadToByteBuffer_cappedSize_readsUpToMaxSize() throws IOException {
        byte[] data = "1234567890".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);

        ByteBuffer buffer = DataUtil.readToByteBuffer(in, 5);
        assertEquals(5, buffer.remaining());
        assertEquals("12345", new String(buffer.array(), 0, buffer.limit(), "UTF-8"));
    }

    // Tests readToByteBuffer throws IllegalArgumentException on negative maxSize
    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBuffer_negativeMaxSize_throwsException() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        DataUtil.readToByteBuffer(in, -1);
    }

    // Tests load from InputStream with specified charset
    @Test
    public void testLoad_inputStreamWithSpecifiedCharset_parsesDocument() throws IOException {
        String html = "<html><head><title>Test</title></head><body><p>Hello</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));

        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        assertEquals("Test", doc.title());
        assertEquals("Hello", doc.select("p").text());
    }

    // Tests load from InputStream with custom Parser
    @Test
    public void testLoad_inputStreamWithXmlParser_parsesDocument() throws IOException {
        String xml = "<xml><element>TestXml</element></xml>";
        InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));

        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());
        assertEquals("TestXml", doc.select("element").text());
    }

    // Tests load from File
    @Test
    public void testLoad_fileWithSpecifiedCharset_parsesDocument() throws IOException {
        File tempFile = File.createTempFile("jsoup-datautil-test", ".html");
        tempFile.deleteOnExit();

        String html = "<html><head><title>File Test</title></head><body><p>File content</p></body></html>";
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(html.getBytes("UTF-8"));
        fos.close();

        Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");
        assertEquals("File Test", doc.title());
        assertEquals("File content", doc.select("p").text());
    }

    // Tests parseByteData with null charset detecting charset from meta http-equiv tag
    @Test
    public void testParseByteData_metaHttpEquivCharset_reDecodesWithDetectedCharset() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head><body>Hello</body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));

        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // Tests parseByteData with null charset detecting charset from HTML5 meta charset tag
    @Test
    public void testParseByteData_metaCharsetTag_reDecodesWithDetectedCharset() {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body>Hello</body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));

        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // Tests parseByteData with null charset and invalid meta charset defaults to UTF-8
    @Test
    public void testParseByteData_invalidMetaCharset_defaultsToUtf8() {
        String html = "<html><head><meta charset=\"invalid-charset-123\"></head><body>Hello</body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));

        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // Tests parseByteData with UTF-8 BOM
    @Test
    public void testParseByteData_utf8Bom_stripsBom() {
        String html = "\uFEFF<html><head><title>BOM</title></head><body>Test</body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));

        Document doc = DataUtil.parseByteData(byteData, "UTF-8", "http://example.com", Parser.htmlParser());
        assertEquals("BOM", doc.title());
    }

    // Tests parseByteData with empty string charset throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testParseByteData_emptyCharset_throwsException() {
        ByteBuffer byteData = ByteBuffer.wrap("test".getBytes(Charset.forName("UTF-8")));
        DataUtil.parseByteData(byteData, "", "http://example.com", Parser.htmlParser());
    }
}