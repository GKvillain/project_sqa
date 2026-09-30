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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DataUtilTest {

    // Tests parsing an HTML stream with a UTF-8 BOM to ensure BOM is stripped and head is parsed correctly (Defects4J bug 20)
    @Test
    public void testParseByteData_withUtf8Bom_stripsBomCorrectly() {
        ByteBuffer byteData = ByteBuffer.wrap(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, '<', 't', 'i', 't', 'l', 'e', '>', 'B', 'O', 'M', '<', '/', 't', 'i', 't', 'l', 'e', '>'});
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertEquals("BOM", doc.title());
    }

    // Tests loading HTML from InputStream with null charset defaulting to UTF-8
    @Test
    public void testLoadInputStream_nullCharset_defaultsToUtf8() throws IOException {
        InputStream in = new ByteArrayInputStream("<title>Test</title><p>Hello World</p>".getBytes("UTF-8"));
        Document doc = DataUtil.load(in, null, "http://example.com");
        assertEquals("Test", doc.title());
        assertEquals("Hello World", doc.select("p").text());
    }

    // Tests loading HTML from a File
    @Test
    public void testLoadFile_validFile_returnsParsedDocument() throws IOException {
        File tempFile = File.createTempFile("dataUtilTest", ".html");
        tempFile.deleteOnExit();
        FileOutputStream out = new FileOutputStream(tempFile);
        out.write("<p>File Content</p>".getBytes("UTF-8"));
        out.close();

        Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");
        assertEquals("File Content", doc.select("p").text());
    }

    // Tests loading with a custom XML parser
    @Test
    public void testLoadInputStream_withXmlParser_parsesAsXml() throws IOException {
        InputStream in = new ByteArrayInputStream("<xml><node>Value</node></xml>".getBytes("UTF-8"));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());
        assertEquals("Value", doc.select("node").text());
    }

    // Tests charset detection from meta http-equiv content-type tag and re-decoding
    @Test
    public void testParseByteData_metaHttpEquivCharset_redecodesWithFoundCharset() {
        String html = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=ISO-8859-1\"></head><body><p>Test</p></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("Test", doc.select("p").text());
    }

    // Tests charset detection from HTML5 meta charset tag
    @Test
    public void testParseByteData_metaCharsetTag_redecodesWithFoundCharset() {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body><p>HTML5 Meta</p></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("HTML5 Meta", doc.select("p").text());
    }

    // Tests meta charset tag when charset matches default UTF-8 (no re-decoding needed)
    @Test
    public void testParseByteData_metaCharsetUtf8_doesNotRedecode() {
        String html = "<html><head><meta charset=\"UTF-8\"></head><body><p>Same Charset</p></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
        assertEquals("Same Charset", doc.select("p").text());
    }

    // Tests parseByteData with explicitly specified charset
    @Test
    public void testParseByteData_explicitCharset_parsesCorrectly() {
        String html = "<p>Explicit Charset</p>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));
        Document doc = DataUtil.parseByteData(byteData, "ISO-8859-1", "http://example.com", Parser.htmlParser());
        assertEquals("Explicit Charset", doc.select("p").text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // Tests parseByteData when empty charset string is provided (exception path)
    @Test(expected = IllegalArgumentException.class)
    public void testParseByteData_emptyCharset_throwsIllegalArgumentException() {
        ByteBuffer byteData = ByteBuffer.wrap("<p>Empty Charset</p>".getBytes());
        DataUtil.parseByteData(byteData, "", "http://example.com", Parser.htmlParser());
    }

    // Tests readToByteBuffer with empty InputStream
    @Test
    public void testReadToByteBuffer_emptyStream_returnsEmptyByteBuffer() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        ByteBuffer buffer = DataUtil.readToByteBuffer(in);
        assertEquals(0, buffer.remaining());
    }

    // Tests extracting standard charset from content-type header
    @Test
    public void testGetCharsetFromContentType_standardContentType_returnsCharset() {
        String contentType = "text/html; charset=utf-8";
        String charset = DataUtil.getCharsetFromContentType(contentType);
        assertEquals("UTF-8", charset);
    }

    // Tests extracting quoted charset from content-type header
    @Test
    public void testGetCharsetFromContentType_quotedCharset_returnsUnquotedCharset() {
        String contentType = "text/html; charset=\"ISO-8859-1\"";
        String charset = DataUtil.getCharsetFromContentType(contentType);
        assertEquals("ISO-8859-1", charset);
    }

    // Tests extracting charset with whitespace and multiple parameters
    @Test
    public void testGetCharsetFromContentType_extraParamsAndSpaces_returnsTrimmedCharset() {
        String contentType = "text/html; charset=  GB2312 ; boundary=something";
        String charset = DataUtil.getCharsetFromContentType(contentType);
        assertEquals("GB2312", charset);
    }

    // Tests getCharsetFromContentType when input is null
    @Test
    public void testGetCharsetFromContentType_nullInput_returnsNull() {
        String charset = DataUtil.getCharsetFromContentType(null);
        assertNull(charset);
    }

    // Tests getCharsetFromContentType when header has no charset
    @Test
    public void testGetCharsetFromContentType_noCharsetSpecified_returnsNull() {
        String contentType = "text/html; text/plain";
        String charset = DataUtil.getCharsetFromContentType(contentType);
        assertNull(charset);
    }
}