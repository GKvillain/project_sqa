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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DataUtilTest {

    // Tests extracting valid charset from content type header
    @Test
    public void testGetCharsetFromContentType_validCharset_returnsCharset() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=utf-8"));
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=ISO-8859-1"));
        assertEquals("US-ASCII", DataUtil.getCharsetFromContentType("text/html; charset=\"US-ASCII\""));
    }

    // Tests null or missing charset in content type
    @Test
    public void testGetCharsetFromContentType_nullOrMissing_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset="));
    }

    // Tests unsupported charset name in content type header
    @Test
    public void testGetCharsetFromContentType_unsupportedCharset_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unsupported_12345_charset"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=??invalid??"));
    }

    // Tests reading input stream fully into byte buffer without max size limit
    @Test
    public void testReadToByteBuffer_unlimitedSize_readsAllBytes() throws IOException {
        byte[] inputData = "Hello World Jsoup Test".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(inputData);
        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in, 0);

        assertEquals(inputData.length, byteBuffer.remaining());
        assertEquals("Hello World Jsoup Test", new String(byteBuffer.array(), "UTF-8"));
    }

    // Tests reading input stream with a max size cap
    @Test
    public void testReadToByteBuffer_cappedSize_readsCappedBytes() throws IOException {
        byte[] inputData = "Hello World Jsoup Test".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(inputData);
        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in, 5);

        assertEquals(5, byteBuffer.remaining());
        assertEquals("Hello", new String(byteBuffer.array(), "UTF-8"));
    }

    // Tests readToByteBuffer default overload without maxSize
    @Test
    public void testReadToByteBuffer_defaultOverload_readsAllBytes() throws IOException {
        byte[] inputData = "Overload Test".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(inputData);
        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in);

        assertEquals(inputData.length, byteBuffer.remaining());
    }

    // Tests readToByteBuffer with invalid negative max size
    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBuffer_negativeMaxSize_throwsException() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        DataUtil.readToByteBuffer(in, -1);
    }

    // Tests UTF-8 BOM handling when charset is null (detect from stream)
    @Test
    public void testParseByteData_utf8BomWithNullCharset_discardsBom() {
        byte[] bomData = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, '<', 'h', 't', 'm', 'l', '>', '<', 'h', 'e', 'a', 'd', '>', '<', 't', 'i', 't', 'l', 'e', '>', 'B', 'O', 'M', '<', '/', 't', 'i', 't', 'l', 'e', '>', '<', '/', 'h', 'e', 'a', 'd', '>', '<', 'b', 'o', 'd', 'y', '>', 'H', 'e', 'l', 'l', 'o', '<', '/', 'b', 'o', 'd', 'y', '>', '<', '/', 'h', 't', 'm', 'l', '>'};
        ByteBuffer buffer = ByteBuffer.wrap(bomData);

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("BOM", doc.title());
        assertEquals("Hello", doc.body().text());
        assertFalse(doc.head().outerHtml().contains("\uFEFF"));
    }

    // Tests UTF-8 BOM handling when charset is explicitly specified as UTF-8
    @Test
    public void testParseByteData_utf8BomWithExplicitUtf8Charset_discardsBom() {
        byte[] bomData = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, '<', 'p', '>', 'T', 'e', 's', 't', '<', '/', 'p', '>'};
        ByteBuffer buffer = ByteBuffer.wrap(bomData);

        Document doc = DataUtil.parseByteData(buffer, "UTF-8", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.select("p").text());
        assertFalse(doc.outerHtml().contains("\uFEFF"));
    }

    // Tests parsing byte data with meta charset tag switching charset from UTF-8 to ISO-8859-1
    @Test
    public void testParseByteData_metaCharsetSwitch_reDecodesCorrectly() {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body><p>Caf\u00e9</p></body></html>";
        byte[] bytes = html.getBytes(Charset.forName("ISO-8859-1"));
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Caf\u00e9", doc.select("p").text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // Tests parsing byte data with meta http-equiv content-type tag
    @Test
    public void testParseByteData_metaHttpEquiv_reDecodesCorrectly() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head><body><p>Caf\u00e9</p></body></html>";
        byte[] bytes = html.getBytes(Charset.forName("ISO-8859-1"));
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Caf\u00e9", doc.select("p").text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    // Tests parseByteData with empty charset string throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testParseByteData_emptyCharset_throwsException() {
        ByteBuffer buffer = ByteBuffer.wrap("<p>test</p>".getBytes());
        DataUtil.parseByteData(buffer, "", "http://example.com", Parser.htmlParser());
    }

    // Tests load from InputStream with charset and custom parser
    @Test
    public void testLoad_inputStreamWithParser_loadsDocument() throws IOException {
        InputStream in = new ByteArrayInputStream("<xml><tag>value</tag></xml>".getBytes("UTF-8"));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());

        assertNotNull(doc);
        assertEquals("value", doc.select("tag").text());
    }

    // Tests load from InputStream with default HTML parser
    @Test
    public void testLoad_inputStreamDefaultParser_loadsDocument() throws IOException {
        InputStream in = new ByteArrayInputStream("<div><span>Content</span></div>".getBytes("UTF-8"));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");

        assertNotNull(doc);
        assertEquals("Content", doc.select("span").text());
    }

    // Tests load from File
    @Test
    public void testLoad_fileInput_loadsDocument() throws IOException {
        File tempFile = File.createTempFile("jsoup_data_util_test", ".html");
        tempFile.deleteOnExit();

        FileOutputStream out = new FileOutputStream(tempFile);
        out.write("<html><head><title>File Test</title></head><body><p>Hello File</p></body></html>".getBytes("UTF-8"));
        out.close();

        Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("File Test", doc.title());
        assertEquals("Hello File", doc.select("p").text());
    }
}