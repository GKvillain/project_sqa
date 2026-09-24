package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DataUtilTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    // Tests extracting charset from Content-Type header with standard format
    @Test
    public void testGetCharsetFromContentType_standardContentType_returnsCharset() {
        String contentType = "text/html; charset=utf-8";
        String charset = DataUtil.getCharsetFromContentType(contentType);
        assertEquals("utf-8", charset);
    }

    // Tests extracting charset with quotes and uppercase
    @Test
    public void testGetCharsetFromContentType_quotedCharset_returnsCleanCharset() {
        String contentType = "text/html; charset=\"ISO-8859-1\"";
        String charset = DataUtil.getCharsetFromContentType(contentType);
        assertEquals("ISO-8859-1", charset);
    }

    // Tests extracting charset when input is null or does not contain charset
    @Test
    public void testGetCharsetFromContentType_nullOrMissingCharset_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset="));
    }

    // Tests extracting unsupported or illegal charset names
    @Test
    public void testGetCharsetFromContentType_unsupportedOrIllegalCharset_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unsupported-charset-name"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=???"));
    }

    // Tests generating mime boundary string format and length
    @Test
    public void testMimeBoundary_defaultCall_generatesExpectedLength() {
        String boundary = DataUtil.mimeBoundary();
        assertNotNull(boundary);
        assertEquals(DataUtil.boundaryLength, boundary.length());
    }

    // Tests empty byte buffer utility method
    @Test
    public void testEmptyByteBuffer_returnsZeroCapacityBuffer() {
        ByteBuffer buffer = DataUtil.emptyByteBuffer();
        assertNotNull(buffer);
        assertEquals(0, buffer.capacity());
    }

    // Tests crossStreams transfers all bytes from input to output stream
    @Test
    public void testCrossStreams_validStream_transfersAllData() throws IOException {
        byte[] expected = "Hello World Transfer Stream Test".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(expected);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DataUtil.crossStreams(in, out);

        assertArrayEquals(expected, out.toByteArray());
    }

    // Tests reading stream to byte buffer with unlimited max size
    @Test
    public void testReadToByteBuffer_unlimitedSize_readsEntireStream() throws IOException {
        byte[] data = "Sample Data for Unlimited Read".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);

        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in);
        assertEquals(data.length, byteBuffer.remaining());
        assertArrayEquals(data, byteBuffer.array());
    }

    // Tests reading stream to byte buffer capped at maxSize
    @Test
    public void testReadToByteBuffer_cappedSize_readsOnlyUpToMaxSize() throws IOException {
        byte[] data = "Sample Data Exceeding Max Size".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);

        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in, 6);
        assertEquals(6, byteBuffer.remaining());
        assertEquals("Sample", new String(byteBuffer.array(), "UTF-8"));
    }

    // Tests readToByteBuffer throws exception on negative maxSize
    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBuffer_negativeMaxSize_throwsException() throws IOException {
        InputStream in = new ByteArrayInputStream("test".getBytes("UTF-8"));
        DataUtil.readToByteBuffer(in, -1);
    }

    // Tests parsing byte data with explicitly specified charset
    @Test
    public void testParseByteData_explicitCharset_parsesCorrectly() {
        String html = "<html><head><title>Explicit</title></head><body><p>Test</p></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));

        Document doc = DataUtil.parseByteData(byteData, "UTF-8", "http://example.com", Parser.htmlParser());
        assertEquals("Explicit", doc.title());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // Tests parsing byte data when empty charset is supplied throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testParseByteData_emptyCharset_throwsException() {
        ByteBuffer byteData = ByteBuffer.wrap("<p>Hello</p>".getBytes());
        DataUtil.parseByteData(byteData, "", "http://example.com", Parser.htmlParser());
    }

    // Tests parsing byte data with null charset defaulting to UTF-8
    @Test
    public void testParseByteData_nullCharsetDefault_parsesAsUtf8() {
        String html = "<html><head><title>Default</title></head><body><p>Hello</p></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));

        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertEquals("Default", doc.title());
    }

    // Tests re-decoding when meta charset attribute is present in HTML
    @Test
    public void testParseByteData_metaCharsetAttribute_redecodesToSpecifiedCharset() throws IOException {
        String html = "<html><head><meta charset=\"ISO-8859-1\"><title>É</title></head><body></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));

        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertEquals("É", doc.title());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // Tests re-decoding when meta http-equiv content-type is present in HTML
    @Test
    public void testParseByteData_metaHttpEquiv_redecodesToSpecifiedCharset() throws IOException {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"><title>Café</title></head><body></body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));

        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertEquals("Café", doc.title());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // Tests parsing HTML with UTF-8 BOM stripped properly
    @Test
    public void testParseByteData_utf8Bom_stripsBomProperly() throws IOException {
        byte[] bomUtf8 = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, '<', 'p', '>', 'B', 'O', 'M', '<', '/', 'p', '>'};
        ByteBuffer byteData = ByteBuffer.wrap(bomUtf8);

        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertEquals("BOM", doc.select("p").text());
    }

    // Tests parsing HTML with UTF-16BE BOM when charset is null (Bug 50 target)
    @Test
    public void testParseByteData_utf16BeBom_parsesCorrectly() throws IOException {
        byte[] bomUtf16Be = new byte[]{
                (byte) 0xFE, (byte) 0xFF,
                0x00, '<', 0x00, 'p', 0x00, '>',
                0x00, 'T', 0x00, 'e', 0x00, 's', 0x00, 't',
                0x00, '<', 0x00, '/', 0x00, 'p', 0x00, '>'
        };
        ByteBuffer byteData = ByteBuffer.wrap(bomUtf16Be);

        Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertEquals("Test", doc.select("p").text());
    }

    // Tests loading HTML directly from an InputStream with custom parser
    @Test
    public void testLoad_inputStreamWithXmlParser_parsesCorrectly() throws IOException {
        String xml = "<xml><data>Value</data></xml>";
        InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));

        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());
        assertEquals("Value", doc.select("data").text());
    }

    // Tests loading HTML from InputStream using default htmlParser overload
    @Test
    public void testLoad_inputStreamDefaultParser_parsesCorrectly() throws IOException {
        String html = "<p>Stream Test</p>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));

        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        assertEquals("Stream Test", doc.select("p").text());
    }

    // Tests loading HTML directly from a File and reading file to byte buffer
    @Test
    public void testLoad_fileInput_parsesDocumentCorrectly() throws IOException {
        File tempFile = temporaryFolder.newFile("test.html");
        OutputStream out = new FileOutputStream(tempFile);
        out.write("<html><head><title>File Test</title></head><body><p>Content</p></body></html>".getBytes("UTF-8"));
        out.close();

        Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");
        assertEquals("File Test", doc.title());
        assertEquals("Content", doc.select("p").text());

        ByteBuffer fileBuffer = DataUtil.readFileToByteBuffer(tempFile);
        assertTrue(fileBuffer.remaining() > 0);
    }
}