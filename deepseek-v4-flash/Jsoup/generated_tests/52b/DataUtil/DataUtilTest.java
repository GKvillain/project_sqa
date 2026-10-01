package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class DataUtilTest {

    // Tests empty input with null charset: should not throw and should return an empty document
    @Test
    public void testLoad_emptyInputNullCharset_returnsDocument() throws Exception {
        Document doc = DataUtil.load(new ByteArrayInputStream(new byte[0]), null, "http://example.com");
        assertNotNull(doc);
        assertEquals("", doc.text());
    }

    // Tests loading UTF-8 HTML from an input stream
    @Test
    public void testLoad_inputStreamWithUtf8Html_parsesDocument() throws Exception {
        String html = "<html><head><title>T</title></head><body><p>Hello</p></body></html>";
        Document doc = DataUtil.load(new ByteArrayInputStream(html.getBytes("UTF-8")), "UTF-8", "http://example.com");
        assertEquals("Hello", doc.body().text());
    }

    // Tests loading XML using an XML parser
    @Test
    public void testLoad_inputStreamWithXmlParser_parsesXml() throws Exception {
        Document doc = DataUtil.load(
                new ByteArrayInputStream("<root>Hello</root>".getBytes("UTF-8")),
                "UTF-8", "http://example.com", Parser.xmlParser());
        assertEquals("Hello", doc.text());
    }

    // Tests loading a UTF-8 file
    @Test
    public void testLoad_fileWithUtf8Html_parsesDocument() throws Exception {
        File file = createTempFile("<html><body><p>File loaded</p></body></html>".getBytes("UTF-8"));
        try {
            Document doc = DataUtil.load(file, "UTF-8", "http://example.com");
            assertEquals("File loaded", doc.body().text());
        } finally {
            file.delete();
        }
    }

    // Tests charset detection from a meta http-equiv content type
    @Test
    public void testParseByteData_htmlMetaCharset_redecodesDocument() throws Exception {
        String html = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=ISO-8859-1\"></head><body>caf\u00e9</body></html>";
        Document doc = DataUtil.parseByteData(
                ByteBuffer.wrap(html.getBytes("ISO-8859-1")), null, "", Parser.htmlParser());
        assertEquals("caf\u00e9", doc.body().text());
    }

    // Tests charset detection from an HTML5 meta charset tag
    @Test
    public void testParseByteData_html5MetaCharset_redecodesDocument() throws Exception {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body>caf\u00e9</body></html>";
        Document doc = DataUtil.parseByteData(
                ByteBuffer.wrap(html.getBytes("ISO-8859-1")), null, "", Parser.htmlParser());
        assertEquals("caf\u00e9", doc.body().text());
    }

    // Tests charset detection from an XML declaration
    @Test
    public void testParseByteData_xmlDeclarationCharset_redecodesDocument() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><root>caf\u00e9</root>";
        Document doc = DataUtil.parseByteData(
                ByteBuffer.wrap(xml.getBytes("ISO-8859-1")), null, "", Parser.xmlParser());
        assertEquals("caf\u00e9", doc.text());
    }

    // Tests UTF-8 BOM handling
    @Test
    public void testParseByteData_utf8Bom_parsesDocument() throws Exception {
        byte[] text = "Hello".getBytes("UTF-8");
        byte[] data = new byte[text.length + 3];
        data[0] = (byte) 0xEF;
        data[1] = (byte) 0xBB;
        data[2] = (byte) 0xBF;
        System.arraycopy(text, 0, data, 3, text.length);

        Document doc = DataUtil.parseByteData(ByteBuffer.wrap(data), null, "", Parser.htmlParser());
        assertEquals("Hello", doc.body().text());
    }

    // Tests maxSize truncation when the stream contains more data than the limit
    @Test
    public void testReadToByteBuffer_maxSizeLessThanInput_truncatesBuffer() throws Exception {
        byte[] input = "0123456789".getBytes("UTF-8");
        ByteBuffer buffer = DataUtil.readToByteBuffer(new ByteArrayInputStream(input), 4);
        assertArrayEquals("0123".getBytes("UTF-8"), toByteArray(buffer));
    }

    // Tests exact maxSize: no truncation and no extra bytes
    @Test
    public void testReadToByteBuffer_maxSizeExactlyInput_returnsAllBytes() throws Exception {
        byte[] input = "0123456789".getBytes("UTF-8");
        ByteBuffer buffer = DataUtil.readToByteBuffer(new ByteArrayInputStream(input), 10);
        assertArrayEquals(input, toByteArray(buffer));
    }

    // Tests maxSize 0 means unlimited
    @Test
    public void testReadToByteBuffer_maxSizeZero_readsAllBytes() throws Exception {
        byte[] input = "0123456789".getBytes("UTF-8");
        ByteBuffer buffer = DataUtil.readToByteBuffer(new ByteArrayInputStream(input), 0);
        assertArrayEquals(input, toByteArray(buffer));
    }

    // Tests negative maxSize rejected
    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBuffer_negativeMaxSize_throwsIllegalArgumentException() throws Exception {
        DataUtil.readToByteBuffer(new ByteArrayInputStream("x".getBytes("UTF-8")), -1);
    }

    // Tests copying an input stream to an output stream
    @Test
    public void testCrossStreams_copiesAllBytes() throws Exception {
        byte[] data = "stream data".getBytes("UTF-8");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataUtil.crossStreams(new ByteArrayInputStream(data), out);
        assertArrayEquals(data, out.toByteArray());
    }

    // Tests reading a file into a ByteBuffer
    @Test
    public void testReadFileToByteBuffer_returnsFileContents() throws Exception {
        byte[] data = "file contents".getBytes("UTF-8");
        File file = createTempFile(data);
        try {
            ByteBuffer buffer = DataUtil.readFileToByteBuffer(file);
            assertArrayEquals(data, toByteArray(buffer));
        } finally {
            file.delete();
        }
    }

    // Tests valid charset parsing from a content type header
    @Test
    public void testGetCharsetFromContentType_validCharset_returnsCharset() {
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=ISO-8859-1"));
    }

    // Tests quoted charset values
    @Test
    public void testGetCharsetFromContentType_quotedCharset_returnsCharset() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=\"UTF-8\""));
    }

    // Tests invalid content types return null
    @Test
    public void testGetCharsetFromContentType_invalidContentType_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=not-a-charset"));
        assertNull(DataUtil.getCharsetFromContentType("text/plain"));
    }

    // Tests mime boundary generation
    @Test
    public void testMimeBoundary_returns32CharsWithAllowedChars() {
        String boundary = DataUtil.mimeBoundary();
        assertEquals(DataUtil.boundaryLength, boundary.length());
        assertTrue(boundary.matches("[-_0-9a-zA-Z]{32}"));
    }

    // Tests empty ByteBuffer factory
    @Test
    public void testEmptyByteBuffer_returnsEmptyBuffer() {
        ByteBuffer buffer = DataUtil.emptyByteBuffer();
        assertNotNull(buffer);
        assertEquals(0, buffer.remaining());
    }

    private File createTempFile(byte[] data) throws Exception {
        File file = File.createTempFile("jsoup-datautil", ".html");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(data);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return file;
    }

    private byte[] toByteArray(ByteBuffer buffer) {
        ByteBuffer copy = buffer.duplicate();
        byte[] data = new byte[copy.remaining()];
        copy.get(data);
        return data;
    }
}