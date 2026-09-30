package org.jsoup.helper;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Locale;

public class DataUtilTest {

    private ByteBuffer bufferFromString(String str, String charset) throws Exception {
        return ByteBuffer.wrap(str.getBytes(charset));
    }

    // Test parseByteData with null charset and meta charset found, should re-encode
    @Test
    public void testParseByteData_nullCharsetMetaFound_shouldReencode() throws Exception {
        String html = "<html><head><meta charset=\"ISO-8859-1\"><title>Test</title></head><body>Hello</body></html>";
        ByteBuffer buffer = bufferFromString(html, "UTF-8");
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("Test", doc.title());
    }

    // Test parseByteData with null charset and no meta charset, uses default UTF-8
    @Test
    public void testParseByteData_nullCharsetNoMeta_usesDefaultCharset() throws Exception {
        String html = "<html><head><title>Test</title></head><body>Hello</body></html>";
        ByteBuffer buffer = bufferFromString(html, "UTF-8");
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
        assertEquals("Test", doc.title());
    }

    // Test parseByteData with null charset and meta charset same as default, does not re-encode
    @Test
    public void testParseByteData_nullCharsetMetaFoundSameCharset_doesNotReencode() throws Exception {
        String html = "<html><head><meta charset=\"UTF-8\"><title>Test</title></head><body>Hello</body></html>";
        ByteBuffer buffer = bufferFromString(html, "UTF-8");
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
        assertEquals("Test", doc.title());
    }

    // Test parseByteData with specified charset, uses that charset
    @Test
    public void testParseByteData_specifiedCharset_usesThatCharset() throws Exception {
        String html = "<html><head><title>Test</title></head><body>Hello</body></html>";
        ByteBuffer buffer = bufferFromString(html, "ISO-8859-1");
        Document doc = DataUtil.parseByteData(buffer, "ISO-8859-1", "http://example.com", Parser.htmlParser());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("Test", doc.title());
    }

    // Test parseByteData with empty charset throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseByteData_specifiedCharsetEmpty_throwsException() throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[0]);
        DataUtil.parseByteData(buffer, "", "http://example.com", Parser.htmlParser());
    }

    // Test parseByteData with BOM character (UTF-8 BOM) strips it
    @Test
    public void testParseByteData_bomCharacter_stripsBOM() throws Exception {
        byte[] bom = {(byte)0xEF, (byte)0xBB, (byte)0xBF};
        String content = "<html><head><title>Test</title></head><body>Hello</body></html>";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(bom);
        baos.write(content.getBytes("UTF-8"));
        ByteBuffer buffer = ByteBuffer.wrap(baos.toByteArray());
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertEquals("Test", doc.title());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    // Test parseByteData with null charset and meta http-equiv, should re-encode
    @Test
    public void testParseByteData_nullCharsetMetaHttpEquiv_shouldReencode() throws Exception {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"><title>Test</title></head><body>Hello</body></html>";
        ByteBuffer buffer = bufferFromString(html, "UTF-8");
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("Test", doc.title());
    }

    // Test readToByteBuffer with maxSize=0 (unlimited) reads all data
    @Test
    public void testReadToByteBuffer_unlimited_readsAllData() throws Exception {
        byte[] data = "HelloWorld".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 0);
        assertEquals(data.length, buf.capacity());
        byte[] out = new byte[buf.remaining()];
        buf.get(out);
        assertArrayEquals(data, out);
    }

    // Test readToByteBuffer with capped maxSize equal to stream length
    @Test
    public void testReadToByteBuffer_cappedExactSize_readsExact() throws Exception {
        byte[] data = new byte[100];
        for (int i = 0; i < 100; i++) data[i] = (byte)i;
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 100);
        assertEquals(100, buf.capacity());
        byte[] out = new byte[buf.remaining()];
        buf.get(out);
        assertArrayEquals(data, out);
    }

    // Test readToByteBuffer with capped maxSize less than stream length, reads up to cap
    @Test
    public void testReadToByteBuffer_cappedLessThanStream_readsUpToCap() throws Exception {
        byte[] data = new byte[200];
        for (int i = 0; i < 200; i++) data[i] = (byte)(i % 256);
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 50);
        assertEquals(50, buf.capacity());
        byte[] expected = new byte[50];
        System.arraycopy(data, 0, expected, 0, 50);
        byte[] out = new byte[buf.remaining()];
        buf.get(out);
        assertArrayEquals(expected, out);
    }

    // Test readToByteBuffer with capped maxSize greater than stream length, reads all
    @Test
    public void testReadToByteBuffer_cappedMoreThanStream_readsAll() throws Exception {
        byte[] data = new byte[50];
        for (int i = 0; i < 50; i++) data[i] = (byte)i;
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buf = DataUtil.readToByteBuffer(in, 200);
        assertEquals(50, buf.capacity());
        byte[] out = new byte[buf.remaining()];
        buf.get(out);
        assertArrayEquals(data, out);
    }

    // Test readToByteBuffer with negative maxSize throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBuffer_negativeMaxSize_throwsException() throws Exception {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        DataUtil.readToByteBuffer(in, -1);
    }

    // Test getCharsetFromContentType with null input returns null
    @Test
    public void testGetCharsetFromContentType_nullInput_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    // Test getCharsetFromContentType with no charset in content type returns null
    @Test
    public void testGetCharsetFromContentType_noCharset_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
    }

    // Test getCharsetFromContentType with valid charset returns that charset
    @Test
    public void testGetCharsetFromContentType_validCharset_returnsCharset() {
        String result = DataUtil.getCharsetFromContentType("text/html; charset=UTF-8");
        assertEquals("UTF-8", result);
    }

    // Test getCharsetFromContentType with lowercase supported charset returns original case
    @Test
    public void testGetCharsetFromContentType_lowercaseCharset_returnsOriginalCase() {
        String result = DataUtil.getCharsetFromContentType("text/html; charset=iso-8859-1");
        assertNotNull(result);
        // Depending on JVM, Charset.isSupported("iso-8859-1") returns true, method returns "iso-8859-1"
        assertEquals("iso-8859-1", result);
    }

    // Test getCharsetFromContentType with unsupported charset returns null
    @Test
    public void testGetCharsetFromContentType_unsupportedCharset_returnsNull() {
        String result = DataUtil.getCharsetFromContentType("text/html; charset=dummy-unknown");
        assertNull(result);
    }
}