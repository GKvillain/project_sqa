package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    // Tests getCharsetFromContentType with standard UTF-8 charset
    @Test
    public void testGetCharsetFromContentType_standardUtf8_returnsUppercase() {
        String contentType = "text/html; charset=utf-8";
        String charset = DataUtil.getCharsetFromContentType(contentType);
        assertEquals("UTF-8", charset);
    }

    // Tests getCharsetFromContentType with quoted charset string
    @Test
    public void testGetCharsetFromContentType_quotedCharset_returnsUppercaseTrimmed() {
        String contentType = "text/html; charset=\"UTF-8\"";
        String charset = DataUtil.getCharsetFromContentType(contentType);
        assertEquals("UTF-8", charset);
    }

    // Tests getCharsetFromContentType with different charset (ISO-8859-1)
    @Test
    public void testGetCharsetFromContentType_isoCharset_returnsIso() {
        String contentType = "text/html; charset=iso-8859-1";
        String charset = DataUtil.getCharsetFromContentType(contentType);
        assertEquals("ISO-8859-1", charset);
    }

    // Tests getCharsetFromContentType with unsupported charset returns null or unsupported
    @Test
    public void testGetCharsetFromContentType_unsupportedCharset_handlesGracefully() {
        String contentType = "text/html; charset=unsupported-charset-name";
        String charset = DataUtil.getCharsetFromContentType(contentType);
        if (charset != null) {
            assertEquals("UNSUPPORTED-CHARSET-NAME", charset);
        } else {
            assertNull(charset);
        }
    }

    // Tests getCharsetFromContentType with null input
    @Test
    public void testGetCharsetFromContentType_nullInput_returnsNull() {
        String charset = DataUtil.getCharsetFromContentType(null);
        assertNull(charset);
    }

    // Tests getCharsetFromContentType when no charset attribute is present
    @Test
    public void testGetCharsetFromContentType_noCharset_returnsNull() {
        String contentType = "text/html; text/plain";
        String charset = DataUtil.getCharsetFromContentType(contentType);
        assertNull(charset);
    }

    // Tests getCharsetFromContentType with empty charset value
    @Test
    public void testGetCharsetFromContentType_emptyCharset_returnsEmptyOrNull() {
        String contentType = "text/html; charset=";
        String charset = DataUtil.getCharsetFromContentType(contentType);
        assertTrue(charset == null || charset.length() == 0);
    }

    // Tests load from InputStream with null charsetName default to UTF-8
    @Test
    public void testLoadInputStream_nullCharset_parsesAsUtf8() throws IOException {
        String html = "<html><head><title>Test</title></head><body><p>Hello</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        Document doc = DataUtil.load(in, null, "http://example.com");
        assertEquals("Test", doc.title());
        assertEquals("Hello", doc.select("p").text());
    }

    // Tests load from InputStream with custom Parser
    @Test
    public void testLoadInputStream_xmlParser_parsesCorrectly() throws IOException {
        String xml = "<root><child>value</child></root>";
        InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());
        assertEquals("value", doc.select("child").text());
    }

    // Tests load from File
    @Test
    public void testLoadFile_validFile_parsesDocument() throws IOException {
        File file = temporaryFolder.newFile("test.html");
        FileOutputStream out = new FileOutputStream(file);
        out.write("<html><head><title>File Test</title></head><body>Content</body></html>".getBytes("UTF-8"));
        out.close();

        Document doc = DataUtil.load(file, "UTF-8", "http://example.com");
        assertEquals("File Test", doc.title());
    }

    // Tests parseByteData detecting meta http-equiv charset
    @Test
    public void testParseByteData_metaHttpEquivCharset_redecodesCorrectly() {
        String html = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=ISO-8859-1\"><title>Café</title></head><body></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Café", doc.title());
    }

    // Tests parseByteData detecting HTML5 meta charset
    @Test
    public void testParseByteData_html5MetaCharset_redecodesCorrectly() {
        String html = "<html><head><meta charset=\"ISO-8859-1\"><title>Café</title></head><body></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));
        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Café", doc.title());
    }

    // Tests parseByteData with leading Byte Order Mark (BOM)
    @Test
    public void testParseByteData_leadingBom_stripsBom() {
        String html = "\uFEFF<html><head><title>BOM Test</title></head><body></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(byteData, "UTF-8", "http://example.com", Parser.htmlParser());
        assertEquals("BOM Test", doc.title());
    }

    // Tests parseByteData with empty charset throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseByteData_emptyCharset_throwsException() {
        String html = "<html><body></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        DataUtil.parseByteData(byteData, "", "http://example.com", Parser.htmlParser());
    }

    // Tests readToByteBuffer reads all stream bytes into ByteBuffer
    @Test
    public void testReadToByteBuffer_validStream_returnsPopulatedByteBuffer() throws IOException {
        byte[] expectedBytes = "Sample stream content for ByteBuffer".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(expectedBytes);
        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in);
        assertEquals(expectedBytes.length, byteBuffer.remaining());
    }
}