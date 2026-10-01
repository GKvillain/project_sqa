package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class DataUtilTest {

    // Tests getCharsetFromContentType with null content type
    @Test
    public void testGetCharsetFromContentType_nullInput_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    // Tests getCharsetFromContentType when no charset is present in content type
    @Test
    public void testGetCharsetFromContentType_noCharset_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
        assertNull(DataUtil.getCharsetFromContentType("text/html;"));
    }

    // Tests getCharsetFromContentType with valid standard charset
    @Test
    public void testGetCharsetFromContentType_validCharset_returnsCharset() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8"));
        assertEquals("iso-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=iso-8859-1"));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=utf-8"));
    }

    // Tests getCharsetFromContentType with quoted charset attribute
    @Test
    public void testGetCharsetFromContentType_quotedCharset_returnsCharset() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=\"UTF-8\""));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset= \"UTF-8\""));
    }

    // Tests getCharsetFromContentType with unsupported charset
    @Test
    public void testGetCharsetFromContentType_unsupportedCharset_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unsupported-charset-name"));
    }

    // Tests getCharsetFromContentType with illegal charset characters (Defects4J Bug 36)
    @Test
    public void testGetCharsetFromContentType_illegalCharset_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=ISO-8859-1; charset=UTF-8"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=\""));
    }

    // Tests readToByteBuffer with unlimited size (maxSize = 0)
    @Test
    public void testReadToByteBuffer_unlimitedSize_readsFullStream() throws IOException {
        byte[] data = "Sample test string for ByteBuffer reading".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buffer = DataUtil.readToByteBuffer(in, 0);

        assertEquals(data.length, buffer.remaining());
        assertEquals("Sample test string for ByteBuffer reading", new String(buffer.array(), 0, buffer.remaining(), "UTF-8"));
    }

    // Tests readToByteBuffer with capped size (maxSize > 0)
    @Test
    public void testReadToByteBuffer_cappedSize_readsUpToLimit() throws IOException {
        byte[] data = "1234567890".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buffer = DataUtil.readToByteBuffer(in, 5);

        assertEquals(5, buffer.remaining());
        assertEquals("12345", new String(buffer.array(), 0, buffer.remaining(), "UTF-8"));
    }

    // Tests readToByteBuffer default overload (unlimited)
    @Test
    public void testReadToByteBuffer_defaultOverload_readsFullStream() throws IOException {
        byte[] data = "Hello World".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buffer = DataUtil.readToByteBuffer(in);

        assertEquals(data.length, buffer.remaining());
    }

    // Tests readToByteBuffer with negative maxSize throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBuffer_negativeMaxSize_throwsException() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        DataUtil.readToByteBuffer(in, -1);
    }

    // Tests parseByteData with explicitly provided charset
    @Test
    public void testParseByteData_explicitCharset_parsesCorrectly() {
        String html = "<html><head><title>Test</title></head><body><p>Hello</p></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(byteData, "UTF-8", "http://example.com", Parser.htmlParser());

        assertEquals("Test", doc.title());
        assertEquals("Hello", doc.select("p").text());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // Tests parseByteData with empty charset string throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseByteData_emptyCharset_throwsException() {
        ByteBuffer byteData = ByteBuffer.wrap("<p>Hello</p>".getBytes(Charset.forName("UTF-8")));
        DataUtil.parseByteData(byteData, "", "http://example.com", Parser.htmlParser());
    }

    // Tests parseByteData detecting charset from meta http-equiv tag and re-decoding
    @Test
    public void testParseByteData_metaHttpEquivTag_detectsCharsetAndDecodes() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\"><title>Na\u00EFve</title></head><body></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());

        assertEquals("Na\u00EFve", doc.title());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // Tests parseByteData detecting charset from HTML5 meta charset tag
    @Test
    public void testParseByteData_metaCharsetTag_detectsCharset() {
        String html = "<html><head><meta charset=\"iso-8859-1\"><title>Caf\u00E9</title></head><body></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());

        assertEquals("Caf\u00E9", doc.title());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // Tests parseByteData stripping Byte Order Mark (BOM)
    @Test
    public void testParseByteData_withBom_stripsBomCorrectly() {
        String html = "\uFEFF<html><head><meta charset=\"iso-8859-1\"><title>BOM Test</title></head><body></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());

        assertEquals("BOM Test", doc.title());
    }

    // Tests load from InputStream with default parser
    @Test
    public void testLoad_inputStream_parsesDocument() throws IOException {
        String html = "<html><head><title>Stream Test</title></head><body>Content</body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");

        assertEquals("Stream Test", doc.title());
        assertEquals("Content", doc.body().text());
    }

    // Tests load from InputStream with XML parser
    @Test
    public void testLoad_inputStreamWithXmlParser_parsesXmlDocument() throws IOException {
        String xml = "<root><child id=\"1\">Value</child></root>";
        InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());

        assertEquals("Value", doc.select("child").text());
    }

    // Tests load from File
    @Test
    public void testLoad_file_parsesFileDocument() throws IOException {
        File tempFile = File.createTempFile("jsoup_test", ".html");
        tempFile.deleteOnExit();
        try {
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write("<html><head><title>File Test</title></head><body>File Content</body></html>".getBytes("UTF-8"));
            fos.close();

            Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");
            assertEquals("File Test", doc.title());
            assertEquals("File Content", doc.body().text());
        } finally {
            tempFile.delete();
        }
    }
}