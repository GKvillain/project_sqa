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

    // Tests extracting charset from standard Content-Type header
    @Test
    public void testGetCharsetFromContentType_validCharset_returnsCharset() {
        String charset = DataUtil.getCharsetFromContentType("text/html; charset=UTF-8");
        assertEquals("UTF-8", charset);
    }

    // Tests extracting charset enclosed in double quotes
    @Test
    public void testGetCharsetFromContentType_quotedCharset_returnsCleanCharset() {
        String charset = DataUtil.getCharsetFromContentType("text/html; charset=\"gb2312\"");
        assertEquals("gb2312", charset);
    }

    // Tests extracting charset enclosed in single quotes
    @Test
    public void testGetCharsetFromContentType_singleQuotedCharset_returnsCleanCharset() {
        String charset = DataUtil.getCharsetFromContentType("text/html; charset='ISO-8859-1'");
        assertEquals("ISO-8859-1", charset);
    }

    // Tests null input for Content-Type header
    @Test
    public void testGetCharsetFromContentType_nullInput_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    // Tests Content-Type header with no charset defined
    @Test
    public void testGetCharsetFromContentType_noCharset_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html; text/plain"));
    }

    // Tests Content-Type header with unsupported charset name
    @Test
    public void testGetCharsetFromContentType_unsupportedCharset_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unsupported-invalid-charset-123"));
    }

    // Tests mime boundary generator length and content
    @Test
    public void testMimeBoundary_generatesRandomBoundary_correctLength() {
        String boundary = DataUtil.mimeBoundary();
        assertNotNull(boundary);
        assertEquals(DataUtil.boundaryLength, boundary.length());
    }

    // Tests empty byte buffer creation
    @Test
    public void testEmptyByteBuffer_returnsZeroCapacityBuffer() {
        ByteBuffer buffer = DataUtil.emptyByteBuffer();
        assertNotNull(buffer);
        assertEquals(0, buffer.remaining());
    }

    // Tests crossStreams copies all bytes from input to output
    @Test
    public void testCrossStreams_copiesStreamData_matchesInput() throws IOException {
        byte[] expected = "Testing Stream Copying".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(expected);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DataUtil.crossStreams(in, out);

        assertArrayEquals(expected, out.toByteArray());
    }

    private static void assertArrayEquals(byte[] expected, byte[] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i]);
        }
    }

    // Tests reading input stream with unlimited size (maxSize = 0)
    @Test
    public void testReadToByteBuffer_unlimitedSize_readsEntireStream() throws IOException {
        byte[] data = "Sample unlimited stream content".getBytes("UTF-8");
        ByteBuffer buffer = DataUtil.readToByteBuffer(new ByteArrayInputStream(data), 0);
        assertEquals(data.length, buffer.remaining());
    }

    // Tests reading input stream with capped max size
    @Test
    public void testReadToByteBuffer_cappedSize_readsUpToMaxSize() throws IOException {
        byte[] data = "1234567890".getBytes("UTF-8");
        ByteBuffer buffer = DataUtil.readToByteBuffer(new ByteArrayInputStream(data), 5);
        assertEquals(5, buffer.remaining());
    }

    // Tests reading input stream with negative max size throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBuffer_negativeMaxSize_throwsException() throws IOException {
        DataUtil.readToByteBuffer(new ByteArrayInputStream(new byte[0]), -1);
    }

    // Tests parsing document with explicit charset
    @Test
    public void testParseByteData_explicitCharset_parsesCorrectly() {
        String html = "<p>Test Paragraph</p>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(buffer, "UTF-8", "http://example.com", Parser.htmlParser());

        assertEquals("Test Paragraph", doc.select("p").text());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // Tests parsing document detecting charset from meta http-equiv
    @Test
    public void testParseByteData_metaHttpEquivCharset_redecodesCorrectly() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head><body><p>Hello</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());

        assertEquals("Hello", doc.select("p").text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // Tests parsing document detecting charset from HTML5 meta charset
    @Test
    public void testParseByteData_metaCharset_redecodesCorrectly() {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body><p>World</p></body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());

        assertEquals("World", doc.select("p").text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // Tests parsing XML document detecting encoding from XML declaration
    @Test
    public void testParseByteData_xmlDeclarationEncoding_detectsEncoding() {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><root><item>test</item></root>";
        ByteBuffer buffer = ByteBuffer.wrap(xml.getBytes(Charset.forName("ISO-8859-1")));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.xmlParser());

        assertEquals("test", doc.select("item").text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // Tests parsing empty input stream using xml parser
    @Test
    public void testParseByteData_emptyXml_parsesWithoutException() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[0]);
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.xmlParser());
        assertNotNull(doc);
    }

    // Tests detecting UTF-8 charset from BOM
    @Test
    public void testParseByteData_utf8Bom_detectsAndConsumesBom() {
        byte[] htmlBytes = "<p>BOM Test</p>".getBytes(Charset.forName("UTF-8"));
        byte[] bomBytes = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] combined = new byte[bomBytes.length + htmlBytes.length];
        System.arraycopy(bomBytes, 0, combined, 0, bomBytes.length);
        System.arraycopy(htmlBytes, 0, combined, bomBytes.length, htmlBytes.length);

        ByteBuffer buffer = ByteBuffer.wrap(combined);
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());

        assertEquals("BOM Test", doc.select("p").text());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // Tests loading document from file
    @Test
    public void testLoad_fileInput_parsesDocument() throws IOException {
        File tempFile = File.createTempFile("dataUtilTest", ".html");
        tempFile.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write("<title>File Test</title>".getBytes("UTF-8"));
        fos.close();

        Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");
        assertEquals("File Test", doc.title());
    }

    // Tests loading document from input stream with xml parser
    @Test
    public void testLoad_streamWithXmlParser_parsesCorrectly() throws IOException {
        InputStream in = new ByteArrayInputStream("<xml><node>val</node></xml>".getBytes("UTF-8"));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());

        assertEquals("val", doc.select("node").text());
    }
}