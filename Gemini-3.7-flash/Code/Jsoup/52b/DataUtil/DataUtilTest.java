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

    // Tests getCharsetFromContentType with standard charset declaration
    @Test
    public void testGetCharsetFromContentType_standardCharset_returnsCharset() {
        String charset = DataUtil.getCharsetFromContentType("text/html; charset=UTF-8");
        assertEquals("UTF-8", charset);
    }

    // Tests getCharsetFromContentType with double-quoted charset
    @Test
    public void testGetCharsetFromContentType_quotedCharset_returnsTrimmedCharset() {
        String charset = DataUtil.getCharsetFromContentType("text/html; charset=\"ISO-8859-1\"");
        assertEquals("ISO-8859-1", charset);
    }

    // Tests getCharsetFromContentType with single-quoted charset
    @Test
    public void testGetCharsetFromContentType_singleQuotedCharset_returnsTrimmedCharset() {
        String charset = DataUtil.getCharsetFromContentType("text/html; charset='US-ASCII'");
        assertEquals("US-ASCII", charset);
    }

    // Tests getCharsetFromContentType with null input
    @Test
    public void testGetCharsetFromContentType_nullInput_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    // Tests getCharsetFromContentType with unsupported charset name
    @Test
    public void testGetCharsetFromContentType_unsupportedCharset_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=invalid_charset_name_xyz"));
    }

    // Tests getCharsetFromContentType without charset parameter
    @Test
    public void testGetCharsetFromContentType_noCharset_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html; text/plain"));
    }

    // Tests emptyByteBuffer returns buffer with zero capacity
    @Test
    public void testEmptyByteBuffer_returnsZeroCapacityBuffer() {
        ByteBuffer buffer = DataUtil.emptyByteBuffer();
        assertNotNull(buffer);
        assertEquals(0, buffer.capacity());
        assertEquals(0, buffer.remaining());
    }

    // Tests mimeBoundary length and content
    @Test
    public void testMimeBoundary_generatesValidBoundary() {
        String boundary = DataUtil.mimeBoundary();
        assertNotNull(boundary);
        assertEquals(DataUtil.boundaryLength, boundary.length());
    }

    // Tests crossStreams copies all bytes from input to output
    @Test
    public void testCrossStreams_validInput_copiesAllBytes() throws IOException {
        byte[] inputData = "Hello, CrossStreams!".getBytes(Charset.forName("UTF-8"));
        ByteArrayInputStream in = new ByteArrayInputStream(inputData);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DataUtil.crossStreams(in, out);
        assertEquals(new String(inputData, "UTF-8"), out.toString("UTF-8"));
    }

    // Tests readToByteBuffer with unlimited size (maxSize = 0)
    @Test
    public void testReadToByteBuffer_unlimitedSize_readsEntireStream() throws IOException {
        byte[] inputData = "Test stream content for unlimited read".getBytes(Charset.forName("UTF-8"));
        ByteArrayInputStream in = new ByteArrayInputStream(inputData);

        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in, 0);
        assertEquals(inputData.length, byteBuffer.remaining());
    }

    // Tests readToByteBuffer with capped maximum size
    @Test
    public void testReadToByteBuffer_cappedSize_readsUpToMaxSize() throws IOException {
        byte[] inputData = "1234567890abcdefghij".getBytes(Charset.forName("UTF-8"));
        ByteArrayInputStream in = new ByteArrayInputStream(inputData);

        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in, 5);
        assertEquals(5, byteBuffer.remaining());
    }

    // Tests readToByteBuffer with default maxSize overload
    @Test
    public void testReadToByteBuffer_defaultOverload_readsEntireStream() throws IOException {
        byte[] inputData = "Short test data".getBytes(Charset.forName("UTF-8"));
        ByteArrayInputStream in = new ByteArrayInputStream(inputData);

        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in);
        assertEquals(inputData.length, byteBuffer.remaining());
    }

    // Tests readToByteBuffer with negative maxSize throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBuffer_negativeMaxSize_throwsException() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3});
        DataUtil.readToByteBuffer(in, -1);
    }

    // Tests load from InputStream with specified charset
    @Test
    public void testLoad_specifiedCharset_parsesCorrectly() throws IOException {
        String html = "<html><head><title>Test</title></head><body><p>Hello</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));

        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        assertEquals("Test", doc.title());
        assertEquals("Hello", doc.select("p").text());
    }

    // Tests load from InputStream detecting meta http-equiv charset
    @Test
    public void testLoad_detectMetaHttpEquivCharset_redecodesCorrectly() throws IOException {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head>"
                + "<body><p>Caf\u00E9</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("ISO-8859-1"));

        Document doc = DataUtil.load(in, null, "http://example.com");
        assertEquals("Caf\u00E9", doc.select("p").text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // Tests load from InputStream detecting HTML5 meta charset
    @Test
    public void testLoad_detectMetaCharset_parsesCorrectly() throws IOException {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body><p>Testing</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("ISO-8859-1"));

        Document doc = DataUtil.load(in, null, "http://example.com");
        assertEquals("Testing", doc.select("p").text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // Tests load with XML declaration charset detection using XML parser
    @Test
    public void testLoad_xmlDeclarationEncoding_detectsCharset() throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><root><msg>Caf\u00E9</msg></root>";
        InputStream in = new ByteArrayInputStream(xml.getBytes("ISO-8859-1"));

        Document doc = DataUtil.load(in, null, "http://example.com", Parser.xmlParser());
        assertEquals("Caf\u00E9", doc.select("msg").text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // Tests load with UTF-8 BOM
    @Test
    public void testLoad_utf8Bom_detectsAndConsumesBom() throws IOException {
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] htmlBytes = "<html><body><p>BOM Test</p></body></html>".getBytes("UTF-8");
        byte[] combined = new byte[bom.length + htmlBytes.length];
        System.arraycopy(bom, 0, combined, 0, bom.length);
        System.arraycopy(htmlBytes, 0, combined, bom.length, htmlBytes.length);

        InputStream in = new ByteArrayInputStream(combined);
        Document doc = DataUtil.load(in, null, "http://example.com");
        assertEquals("BOM Test", doc.select("p").text());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // Tests load from File
    @Test
    public void testLoad_fromFile_parsesDocument() throws IOException {
        File tempFile = File.createTempFile("datautil_test", ".html");
        try {
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write("<html><head><title>File Test</title></head><body>Content</body></html>".getBytes("UTF-8"));
            fos.close();

            Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");
            assertEquals("File Test", doc.title());
        } finally {
            assertTrue(tempFile.delete());
        }
    }

    // Tests load with empty InputStream
    @Test
    public void testLoad_emptyStream_returnsEmptyDoc() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        Document doc = DataUtil.load(in, null, "http://example.com");
        assertNotNull(doc);
    }
}