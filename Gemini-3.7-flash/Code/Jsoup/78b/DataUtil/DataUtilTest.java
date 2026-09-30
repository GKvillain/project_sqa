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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DataUtilTest {

    // Tests null input stream returns empty Document
    @Test
    public void testParseInputStream_nullInput_returnsEmptyDocument() throws IOException {
        Document doc = DataUtil.parseInputStream(null, "UTF-8", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("http://example.com", doc.baseUri());
        assertEquals(0, doc.children().size());
    }

    // Tests parsing empty input stream
    @Test
    public void testParseInputStream_emptyStream_returnsDocument() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // Tests parsing UTF-8 stream with UTF-8 BOM
    @Test
    public void testParseInputStream_utf8Bom_detectsAndStripsBom() throws IOException {
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] content = "<p>Hello BOM</p>".getBytes("UTF-8");
        byte[] all = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, all, 0, bom.length);
        System.arraycopy(content, 0, all, bom.length, content.length);

        InputStream in = new ByteArrayInputStream(all);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertEquals("Hello BOM", doc.select("p").text());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // Tests parsing UTF-16 BE with BOM
    @Test
    public void testParseInputStream_utf16BeBom_detectsCharset() throws IOException {
        byte[] bom = new byte[]{(byte) 0xFE, (byte) 0xFF};
        byte[] content = "<title>UTF16BE</title>".getBytes("UTF-16BE");
        byte[] all = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, all, 0, bom.length);
        System.arraycopy(content, 0, all, bom.length, content.length);

        InputStream in = new ByteArrayInputStream(all);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertEquals("UTF16BE", doc.title());
        assertEquals("UTF-16", doc.outputSettings().charset().name());
    }

    // Tests parsing UTF-16 LE with BOM
    @Test
    public void testParseInputStream_utf16LeBom_detectsCharset() throws IOException {
        byte[] bom = new byte[]{(byte) 0xFF, (byte) 0xFE};
        byte[] content = "<title>UTF16LE</title>".getBytes("UTF-16LE");
        byte[] all = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, all, 0, bom.length);
        System.arraycopy(content, 0, all, bom.length, content.length);

        InputStream in = new ByteArrayInputStream(all);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertEquals("UTF16LE", doc.title());
        assertEquals("UTF-16", doc.outputSettings().charset().name());
    }

    // Tests parsing UTF-32 BE with BOM
    @Test
    public void testParseInputStream_utf32BeBom_detectsCharset() throws IOException {
        byte[] bom = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF};
        if (Charset.isSupported("UTF-32")) {
            byte[] content = "<title>UTF32BE</title>".getBytes("UTF-32BE");
            byte[] all = new byte[bom.length + content.length];
            System.arraycopy(bom, 0, all, 0, bom.length);
            System.arraycopy(content, 0, all, bom.length, content.length);

            InputStream in = new ByteArrayInputStream(all);
            Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
            assertEquals("UTF-32", doc.outputSettings().charset().name());
        }
    }

    // Tests detection of HTML5 meta charset
    @Test
    public void testParseInputStream_metaCharset_switchesCharset() throws IOException {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body><p>Hello</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("ISO-8859-1"));
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("Hello", doc.select("p").text());
    }

    // Tests detection of http-equiv Content-Type meta tag
    @Test
    public void testParseInputStream_metaHttpEquiv_switchesCharset() throws IOException {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head><body><p>Test</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("ISO-8859-1"));
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("Test", doc.select("p").text());
    }

    // Tests detection of XML prolog encoding
    @Test
    public void testParseInputStream_xmlDeclarationEncoding_switchesCharset() throws IOException {
        String xml = "<?xml encoding='ISO-8859-1'?><root><val>Data</val></root>";
        InputStream in = new ByteArrayInputStream(xml.getBytes("ISO-8859-1"));
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.xmlParser());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("Data", doc.select("val").text());
    }

    // Tests parsing with explicit charset provided
    @Test
    public void testParseInputStream_explicitCharset_usesProvidedCharset() throws IOException {
        String html = "<p>Explicit</p>";
        InputStream in = new ByteArrayInputStream(html.getBytes("ISO-8859-1"));
        Document doc = DataUtil.parseInputStream(in, "ISO-8859-1", "http://example.com", Parser.htmlParser());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("Explicit", doc.select("p").text());
    }

    // Tests empty charset argument throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseInputStream_emptyCharset_throwsException() throws IOException {
        InputStream in = new ByteArrayInputStream("<p>Test</p>".getBytes("UTF-8"));
        DataUtil.parseInputStream(in, "", "http://example.com", Parser.htmlParser());
    }

    // Tests load method with InputStream
    @Test
    public void testLoad_inputStream_loadsDocument() throws IOException {
        InputStream in = new ByteArrayInputStream("<title>Jsoup Load</title>".getBytes("UTF-8"));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        assertEquals("Jsoup Load", doc.title());
    }

    // Tests load method with File
    @Test
    public void testLoad_file_loadsDocument() throws IOException {
        File temp = File.createTempFile("datautil_test", ".html");
        temp.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(temp);
        fos.write("<title>File Test</title>".getBytes("UTF-8"));
        fos.close();

        Document doc = DataUtil.load(temp, "UTF-8", "http://example.com");
        assertEquals("File Test", doc.title());
    }

    // Tests crossStreams copies all bytes from in to out
    @Test
    public void testCrossStreams_validStreams_copiesData() throws IOException {
        byte[] inputData = "Sample data for stream copying".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(inputData);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DataUtil.crossStreams(in, out);
        assertEquals("Sample data for stream copying", out.toString("UTF-8"));
    }

    // Tests readToByteBuffer with negative maxSize throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBuffer_negativeMaxSize_throwsException() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3});
        DataUtil.readToByteBuffer(in, -1);
    }

    // Tests readToByteBuffer with unlimited maxSize (0)
    @Test
    public void testReadToByteBuffer_zeroMaxSize_readsAll() throws IOException {
        byte[] data = "Hello Buffer".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buffer = DataUtil.readToByteBuffer(in, 0);
        assertEquals(data.length, buffer.remaining());
    }

    // Tests readToByteBuffer with maxSize limit
    @Test
    public void testReadToByteBuffer_maxSizeLimit_readsUpToLimit() throws IOException {
        byte[] data = "1234567890".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buffer = DataUtil.readToByteBuffer(in, 5);
        assertEquals(5, buffer.remaining());
    }

    // Tests readToByteBuffer with single stream argument
    @Test
    public void testReadToByteBuffer_streamOnly_readsAll() throws IOException {
        byte[] data = "Stream Data".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buffer = DataUtil.readToByteBuffer(in);
        assertEquals(data.length, buffer.remaining());
    }

    // Tests readFileToByteBuffer reads whole file
    @Test
    public void testReadFileToByteBuffer_validFile_returnsByteBuffer() throws IOException {
        File temp = File.createTempFile("datautil_read_test", ".txt");
        temp.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(temp);
        fos.write("File buffer content".getBytes("UTF-8"));
        fos.close();

        ByteBuffer buffer = DataUtil.readFileToByteBuffer(temp);
        assertEquals("File buffer content".getBytes("UTF-8").length, buffer.remaining());
    }

    // Tests emptyByteBuffer returns empty buffer
    @Test
    public void testEmptyByteBuffer_returnsZeroCapacityBuffer() {
        ByteBuffer buffer = DataUtil.emptyByteBuffer();
        assertNotNull(buffer);
        assertEquals(0, buffer.capacity());
        assertEquals(0, buffer.remaining());
    }

    // Tests getCharsetFromContentType with valid content-type
    @Test
    public void testGetCharsetFromContentType_validContentType_returnsCharset() {
        String charset = DataUtil.getCharsetFromContentType("text/html; charset=UTF-8");
        assertEquals("UTF-8", charset);

        charset = DataUtil.getCharsetFromContentType("text/html; charset=\"ISO-8859-1\"");
        assertEquals("ISO-8859-1", charset);

        charset = DataUtil.getCharsetFromContentType("text/html; charset='US-ASCII'");
        assertEquals("US-ASCII", charset);
    }

    // Tests getCharsetFromContentType with null and invalid content-type
    @Test
    public void testGetCharsetFromContentType_nullOrInvalid_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset="));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=UNSUPPORTED_CHARSET_XYZ"));
    }

    // Tests mimeBoundary creates string of boundaryLength
    @Test
    public void testMimeBoundary_defaultLength_returnsValidBoundary() {
        String boundary = DataUtil.mimeBoundary();
        assertNotNull(boundary);
        assertEquals(DataUtil.boundaryLength, boundary.length());
        assertTrue(boundary.matches("[-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ]+"));
    }
}