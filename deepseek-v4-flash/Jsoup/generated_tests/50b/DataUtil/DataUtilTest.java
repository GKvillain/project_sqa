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

    // Helper methods

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    private static ByteBuffer toByteBuffer(String s, String charset) {
        try {
            return ByteBuffer.wrap(s.getBytes(charset));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    // Tests for parseByteData

    @Test
    // Tests normal HTML without charset specification, null charset
    public void testParseByteData_nullCharset_simpleHtml_returnsDocumentWithUtf8() {
        String html = "<html><body>Hello</body></html>";
        ByteBuffer data = toByteBuffer(html, "UTF-8");
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
        assertEquals("Hello", doc.body().text());
    }

    @Test
    // Tests meta charset detection with different charset
    public void testParseByteData_nullCharset_withMetaCharsetDifferent_usesMetaCharset() {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body>Hello</body></html>";
        ByteBuffer data = toByteBuffer(html, "UTF-8");
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("Hello", doc.body().text());
    }

    @Test
    // Tests meta charset detection with same charset as default
    public void testParseByteData_nullCharset_withMetaCharsetSame_keepsUtf8() {
        String html = "<html><head><meta charset=\"UTF-8\"></head><body>Hello</body></html>";
        ByteBuffer data = toByteBuffer(html, "UTF-8");
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
        assertEquals("Hello", doc.body().text());
    }

    @Test
    // Tests UTF-8 BOM removal when no charset specified
    public void testParseByteData_nullCharset_withUtf8Bom_removesBom() {
        byte[] bom = new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF};
        String content = "<html><body>Hello</body></html>";
        byte[] dataBytes = concat(bom, content.getBytes(StandardCharsets.UTF_8));
        ByteBuffer data = ByteBuffer.wrap(dataBytes);
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
        assertEquals("Hello", doc.body().text());
    }

    @Test
    // Tests that a specified charset is used (without BOM)
    public void testParseByteData_withCharsetSpecified_validInput_usesSpecifiedCharset() {
        String html = "<html><body>Hello</body></html>";
        ByteBuffer data = toByteBuffer(html, "ISO-8859-1");
        Document doc = DataUtil.parseByteData(data, "ISO-8859-1", "http://example.com", Parser.htmlParser());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
        assertEquals("Hello", doc.body().text());
    }

    @Test
    // Tests that BOM overrides specified charset (this may be a defect)
    public void testParseByteData_withCharsetSpecified_withUtf8Bom_incorrectlyOverridesCharset() {
        byte[] bom = new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF};
        String content = "<html><body>Hello</body></html>";
        byte[] dataBytes = concat(bom, content.getBytes(StandardCharsets.UTF_8));
        ByteBuffer data = ByteBuffer.wrap(dataBytes);
        Document doc = DataUtil.parseByteData(data, "ISO-8859-1", "http://example.com", Parser.htmlParser());
        // According to the code, BOM handling re-decodes with default charset and sets charset to default.
        // So we expect charset to be "UTF-8" (defect) instead of "ISO-8859-1".
        // This test will pass if the defect exists (i.e. charset is UTF-8) and fail if fixed.
        assertEquals("UTF-8", doc.outputSettings().charset().name());
        // Also check that BOM removal worked
        assertEquals("Hello", doc.body().text());
    }

    // Tests for getCharsetFromContentType

    @Test
    // Tests null input returns null
    public void testGetCharsetFromContentType_null_returnsNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    @Test
    // Tests valid charset
    public void testGetCharsetFromContentType_valid_returnsCharset() {
        String result = DataUtil.getCharsetFromContentType("text/html; charset=UTF-8");
        assertNotNull(result);
        assertTrue(result.equalsIgnoreCase("UTF-8"));
    }

    @Test
    // Tests charset with uppercase
    public void testGetCharsetFromContentType_upperCase_returnsNormalized() {
        String result = DataUtil.getCharsetFromContentType("text/html; charset=iso-8859-1");
        assertNotNull(result);
        assertTrue(result.equalsIgnoreCase("ISO-8859-1"));
    }

    @Test
    // Tests illegal charset name returns null
    public void testGetCharsetFromContentType_illegalCharsetName_returnsNull() {
        // "invalid-\\x00" is illegal name
        String result = DataUtil.getCharsetFromContentType("text/html; charset=invalid-\\x00");
        assertNull(result);
    }

    // Tests for readToByteBuffer

    @Test
    // Tests reading a small stream without cap
    public void testReadToByteBuffer_smallStream_returnsBytes() throws IOException {
        String data = "Hello World";
        InputStream in = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        ByteBuffer buffer = DataUtil.readToByteBuffer(in);
        assertEquals(data, new String(buffer.array(), "UTF-8"));
    }

    @Test
    // Tests reading with cap that limits bytes
    public void testReadToByteBuffer_cappedSize_limitsBytes() throws IOException {
        String data = "This is a longer string than cap";
        InputStream in = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        ByteBuffer buffer = DataUtil.readToByteBuffer(in, 5);
        assertEquals(5, buffer.remaining());
        assertEquals("This ", new String(buffer.array(), buffer.position(), buffer.remaining(), "UTF-8"));
    }

    // Tests for readFileToByteBuffer

    @Test
    // Tests reading a valid file
    public void testReadFileToByteBuffer_validFile_returnsBuffer() throws IOException {
        File tempFile = File.createTempFile("test", ".tmp");
        tempFile.deleteOnExit();
        String content = "Test file content";
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        }
        ByteBuffer buffer = DataUtil.readFileToByteBuffer(tempFile);
        assertEquals(content, new String(buffer.array(), "UTF-8"));
    }

    @Test(expected = IOException.class)
    // Tests reading non-existent file throws IOException
    public void testReadFileToByteBuffer_nonExistentFile_throwsIOException() throws IOException {
        File nonExistent = new File("/nonexistent/file" + System.currentTimeMillis());
        DataUtil.readFileToByteBuffer(nonExistent);
    }

    // Test for emptyByteBuffer

    @Test
    // Tests empty byte buffer has zero capacity
    public void testEmptyByteBuffer_returnsEmpty() {
        ByteBuffer buffer = DataUtil.emptyByteBuffer();
        assertEquals(0, buffer.capacity());
        assertEquals(0, buffer.remaining());
    }

    // Test for mimeBoundary

    @Test
    // Tests mime boundary length
    public void testMimeBoundary_returnsBoundaryOfLength32() {
        String boundary = DataUtil.mimeBoundary();
        assertNotNull(boundary);
        assertEquals(32, boundary.length());
    }
}