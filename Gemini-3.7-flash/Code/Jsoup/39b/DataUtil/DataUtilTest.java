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

    // Tests loading HTML document from a File
    @Test
    public void testLoad_fileInput_returnsDocument() throws IOException {
        File file = File.createTempFile("datautil-test", ".html");
        file.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write("<title>Test File</title><p>Hello</p>".getBytes("UTF-8"));
        fos.close();

        Document doc = DataUtil.load(file, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("Test File", doc.title());
        assertEquals("Hello", doc.select("p").text());
    }

    // Tests loading HTML document from an InputStream with default parser
    @Test
    public void testLoad_inputStreamWithCharset_returnsDocument() throws IOException {
        InputStream in = new ByteArrayInputStream("<div>Sample</div>".getBytes("UTF-8"));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("Sample", doc.select("div").text());
    }

    // Tests loading document from an InputStream with custom XML Parser
    @Test
    public void testLoad_inputStreamWithXmlParser_returnsDocument() throws IOException {
        InputStream in = new ByteArrayInputStream("<xml><child>Content</child></xml>".getBytes("UTF-8"));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("Content", doc.select("child").text());
    }

    // Tests reading unlimited size from stream into ByteBuffer
    @Test
    public void testReadToByteBuffer_unlimitedSize_readsEntireStream() throws IOException {
        byte[] expectedData = "Some arbitrary data stream content".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(expectedData);
        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in, 0);

        byte[] actualData = new byte[byteBuffer.remaining()];
        byteBuffer.get(actualData);
        assertArrayEquals(expectedData, actualData);
    }

    // Tests reading capped size from stream into ByteBuffer
    @Test
    public void testReadToByteBuffer_cappedSize_readsOnlyUpToMaxSize() throws IOException {
        byte[] originalData = "0123456789ABCDEF".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(originalData);
        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in, 5);

        assertEquals(5, byteBuffer.remaining());
        byte[] actualData = new byte[5];
        byteBuffer.get(actualData);
        assertEquals("01234", new String(actualData, "UTF-8"));
    }

    // Tests readToByteBuffer default overload without maxSize
    @Test
    public void testReadToByteBuffer_defaultOverload_readsAllBytes() throws IOException {
        byte[] originalData = "test stream data".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(originalData);
        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in);

        assertEquals(originalData.length, byteBuffer.remaining());
    }

    // Tests exception on negative maxSize argument
    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBuffer_negativeMaxSize_throwsException() throws IOException {
        InputStream in = new ByteArrayInputStream("data".getBytes("UTF-8"));
        DataUtil.readToByteBuffer(in, -1);
    }

    // Tests extracting valid charset from content-type string
    @Test
    public void testGetCharsetFromContentType_validHeader_returnsCharset() {
        String charset = DataUtil.getCharsetFromContentType("text/html; charset=ISO-8859-1");
        assertEquals("ISO-8859-1", charset);
    }

    // Tests extracting quoted charset from content-type string
    @Test
    public void testGetCharsetFromContentType_quotedCharset_returnsTrimmedCharset() {
        String charset = DataUtil.getCharsetFromContentType("text/html; charset=\"UTF-8\"");
        assertEquals("UTF-8", charset);
    }

    // Tests extracting charset when input is null or missing charset attribute
    @Test
    public void testGetCharsetFromContentType_nullOrMissing_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset="));
    }

    // Tests extracting unsupported or invalid charset returns null
    @Test
    public void testGetCharsetFromContentType_invalidCharset_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unsupported_charset_xyz"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=@#$%^&*"));
    }

    // Tests parseByteData detecting charset from meta http-equiv tag
    @Test
    public void testParseByteData_metaHttpEquiv_redecodesWithFoundCharset() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head>"
                + "<body><p>Hello World</p></body></html>";
        byte[] bytes = html.getBytes(Charset.forName("ISO-8859-1"));
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("Hello World", doc.select("p").text());
    }

    // Tests parseByteData detecting charset from HTML5 meta charset tag
    @Test
    public void testParseByteData_metaCharsetHtml5_redecodesWithFoundCharset() {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body><p>Test</p></body></html>";
        byte[] bytes = html.getBytes(Charset.forName("ISO-8859-1"));
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("Test", doc.select("p").text());
    }

    // Tests parseByteData fallback when meta has invalid charset name
    @Test
    public void testParseByteData_metaInvalidCharset_fallsBackToDefault() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html\" charset=\"invalid-charset-name\"></head>"
                + "<body><p>Content</p></body></html>";
        byte[] bytes = html.getBytes(Charset.forName("UTF-8"));
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
        assertEquals("Content", doc.select("p").text());
    }

    // Tests parseByteData with empty charset string throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testParseByteData_emptyCharset_throwsException() {
        ByteBuffer buffer = ByteBuffer.wrap("<p>data</p>".getBytes());
        DataUtil.parseByteData(buffer, "", "http://example.com", Parser.htmlParser());
    }

    // Tests UTF-8 BOM handling when charset is null (defect Jsoup-39)
    @Test
    public void testParseByteData_utf8BomNullCharset_discardsBomAndParsesCorrectly() {
        byte[] bomAndHtml = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, '<', 'p', '>', 'H', 'e', 'l', 'l', 'o', '<', '/', 'p', '>'};
        ByteBuffer buffer = ByteBuffer.wrap(bomAndHtml);

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
        assertEquals("Hello", doc.select("p").text());
        assertEquals("Hello", doc.body().text());
    }

    // Tests UTF-8 BOM handling when charset is explicitly specified
    @Test
    public void testParseByteData_utf8BomExplicitCharset_discardsBom() {
        byte[] bomAndHtml = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, '<', 'p', '>', 'W', 'o', 'r', 'l', 'd', '<', '/', 'p', '>'};
        ByteBuffer buffer = ByteBuffer.wrap(bomAndHtml);

        Document doc = DataUtil.parseByteData(buffer, "UTF-8", "http://example.com", Parser.htmlParser());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
        assertEquals("World", doc.select("p").text());
        assertEquals("World", doc.body().text());
    }
}