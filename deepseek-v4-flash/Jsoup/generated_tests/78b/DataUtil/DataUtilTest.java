package org.jsoup.helper;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

public class DataUtilTest {

    // Tests null input returns null
    @Test
    public void testGetCharsetFromContentType_null_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    // Tests valid charset extraction
    @Test
    public void testGetCharsetFromContentType_validCharset_returnsCharset() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8"));
    }

    // Tests unsupported charset returns null
    @Test
    public void testGetCharsetFromContentType_unsupportedCharset_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("charset=INVALID_CHARSET_NAME"));
    }

    // Tests charset with quotes is returned unquoted
    @Test
    public void testGetCharsetFromContentType_charsetWithQuotes_returnsUnquoted() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=\"UTF-8\""));
    }

    // Tests readToByteBuffer with maxSize limit
    @Test
    public void testReadToByteBuffer_maxSizeLimited_returnsLimitedBytes() throws IOException {
        byte[] data = new byte[100];
        for (int i = 0; i < 100; i++) data[i] = (byte) i;
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 50);
        assertEquals(50, buf.remaining());
        byte[] out = new byte[buf.remaining()];
        buf.get(out);
        for (int i = 0; i < 50; i++) {
            assertEquals((byte) i, out[i]);
        }
    }

    // Tests negative maxSize throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBuffer_negativeMaxSize_throwsException() throws IOException {
        DataUtil.readToByteBuffer(new ByteArrayInputStream(new byte[10]), -1);
    }

    // Tests maxSize 0 reads all bytes
    @Test
    public void testReadToByteBuffer_maxSizeZero_readsAll() throws IOException {
        byte[] data = {1, 2, 3};
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 0);
        assertEquals(3, buf.remaining());
    }

    // Tests null input returns new Document with baseUri
    @Test
    public void testParseInputStream_nullInput_returnsDocument() throws IOException {
        Document doc = DataUtil.parseInputStream(null, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("http://example.com", doc.baseUri());
    }

    // Tests UTF-8 BOM detection and correct parsing
    @Test
    public void testParseInputStream_withBomUtf8_detectsUtf8() throws IOException {
        String html = "<html><head><title>Test</title></head><body>Hello</body></html>";
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] content = html.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, bytes, 0, bom.length);
        System.arraycopy(content, 0, bytes, bom.length, content.length);
        InputStream in = new ByteArrayInputStream(bytes);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.title());
    }

    // Tests UTF-16LE BOM detection and correct parsing
    @Test
    public void testParseInputStream_withBomUtf16_detectsUtf16() throws IOException {
        String html = "<html><head><title>Tëst</title></head><body>Hello</body></html>";
        byte[] bom = {(byte) 0xFF, (byte) 0xFE};
        byte[] content = html.getBytes(StandardCharsets.UTF_16LE);
        byte[] bytes = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, bytes, 0, bom.length);
        System.arraycopy(content, 0, bytes, bom.length, content.length);
        InputStream in = new ByteArrayInputStream(bytes);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Tëst", doc.title());
    }

    // Tests specified charset is used for decoding
    @Test
    public void testParseInputStream_charsetSpecified_usesSpecifiedCharset() throws IOException {
        String text = "é";
        String html = "<html><head><title>" + text + "</title></head><body></body></html>";
        byte[] content = html.getBytes(StandardCharsets.ISO_8859_1);
        InputStream in = new ByteArrayInputStream(content);
        Document doc = DataUtil.parseInputStream(in, "ISO-8859-1", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals(text, doc.title());
    }

    // Tests charset detection from meta element triggers re-parse
    @Test
    public void testParseInputStream_charsetFromMeta_redetect() throws IOException {
        String meta = "<meta charset=\"Shift_JIS\">";
        String body = "テスト";
        String html = "<html><head>" + meta + "</head><body>" + body + "</body></html>";
        Charset shiftJis = Charset.forName("Shift_JIS");
        byte[] content = html.getBytes(shiftJis);
        InputStream in = new ByteArrayInputStream(content);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals(body, doc.body().text());
    }

    // Tests charset detection from XML declaration triggers re-parse
    @Test
    public void testParseInputStream_charsetFromXmlDeclaration_redetect() throws IOException {
        String html = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><html><head><title>é</title></head><body></body></html>";
        byte[] content = html.getBytes(StandardCharsets.ISO_8859_1);
        InputStream in = new ByteArrayInputStream(content);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("é", doc.title());
    }

    // Tests that fullyRead true and no charset override does not re-parse
    @Test
    public void testParseInputStream_fullyReadTrue_noRedetectIfDefaultCharset() throws IOException {
        String html = "<html><head><title>Test</title></head><body>Small</body></html>";
        byte[] content = html.getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(content);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // Tests that fullyRead false triggers re-parse with default charset
    @Test
    public void testParseInputStream_fullyReadFalse_reparsedWithDefaultCharset() throws IOException {
        String title = "Large";
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>").append(title).append("</title></head><body>");
        for (int i = 0; i < 6000; i++) {
            sb.append('x');
        }
        sb.append("</body></html>");
        String html = sb.toString();
        byte[] content = html.getBytes(StandardCharsets.UTF_8);
        assertTrue("Content must be larger than firstReadBufferSize", content.length > 5119);
        InputStream in = new ByteArrayInputStream(content);
        Document doc = DataUtil.parseInputStream(in, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals(title, doc.title());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // Tests empty charset name throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testParseInputStream_charsetNameEmpty_throwsException() throws IOException {
        String html = "<html></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes());
        DataUtil.parseInputStream(in, "", "http://example.com", Parser.htmlParser());
    }

    // Tests crossStreams copies data correctly
    @Test
    public void testCrossStreams_transfersData() throws IOException {
        byte[] data = {10, 20, 30};
        InputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataUtil.crossStreams(in, out);
        assertArrayEquals(data, out.toByteArray());
    }

    // Tests emptyByteBuffer returns zero-capacity buffer
    @Test
    public void testEmptyByteBuffer_returnsEmpty() {
        ByteBuffer buf = DataUtil.emptyByteBuffer();
        assertEquals(0, buf.capacity());
    }

    // Tests mimeBoundary returns string of length 32
    @Test
    public void testMimeBoundary_returnsLength32() {
        String boundary = DataUtil.mimeBoundary();
        assertEquals(32, boundary.length());
    }

    // Tests readFileToByteBuffer returns correct bytes
    @Test
    public void testReadFileToByteBuffer_returnsBytes() throws IOException {
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();
        byte[] expected = {0, 1, 2, 3, 4, 5};
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(expected);
        }
        ByteBuffer buf = DataUtil.readFileToByteBuffer(tempFile);
        byte[] actual = new byte[buf.remaining()];
        buf.get(actual);
        assertArrayEquals(expected, actual);
    }
}