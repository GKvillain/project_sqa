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
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DataUtilTest {

    // Tests loading with null input stream returning an empty Document
    @Test
    public void testLoad_nullInputStream_returnsEmptyDocument() throws IOException {
        Document doc = DataUtil.load((InputStream) null, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("http://example.com", doc.baseUri());
        assertEquals(0, doc.children().size());
    }

    // Tests normal document parsing from input stream with default UTF-8
    @Test
    public void testLoad_validInputStream_parsesCorrectly() throws IOException {
        InputStream in = new ByteArrayInputStream("<p>Hello World</p>".getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("Hello World", doc.select("p").text());
    }

    // Tests document loading using an explicit custom parser
    @Test
    public void testLoad_withCustomParser_parsesCorrectly() throws IOException {
        InputStream in = new ByteArrayInputStream("<item>Value</item>".getBytes(StandardCharsets.UTF_8));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com", Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("Value", doc.select("item").text());
    }

    // Tests loading html document from File
    @Test
    public void testLoad_file_parsesCorrectly() throws IOException {
        File tempFile = File.createTempFile("jsoup-test", ".html");
        tempFile.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            out.write("<div>File Content</div>".getBytes(StandardCharsets.UTF_8));
        }

        Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("File Content", doc.select("div").text());
    }

    // Tests exception path when charset argument is empty string
    @Test(expected = IllegalArgumentException.class)
    public void testLoad_emptyCharset_throwsIllegalArgumentException() throws IOException {
        InputStream in = new ByteArrayInputStream("<p>Test</p>".getBytes(StandardCharsets.UTF_8));
        DataUtil.load(in, "", "http://example.com");
    }

    // Tests stream copy functionality in crossStreams
    @Test
    public void testCrossStreams_validInput_copiesAllBytes() throws IOException {
        byte[] expected = "Cross stream data test".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(expected);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DataUtil.crossStreams(in, out);
        assertArrayEquals(expected, out.toByteArray());
    }

    // Tests reading byte buffer with a max size limit
    @Test
    public void testReadToByteBuffer_maxSizeLimit_readsUpToLimit() throws IOException {
        byte[] data = new byte[]{10, 20, 30, 40, 50, 60, 70};
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in, 3);
        assertEquals(3, byteBuffer.remaining());
        assertEquals(10, byteBuffer.get());
        assertEquals(20, byteBuffer.get());
        assertEquals(30, byteBuffer.get());
    }

    // Tests reading byte buffer with unlimited size (0)
    @Test
    public void testReadToByteBuffer_zeroMaxSize_readsAll() throws IOException {
        byte[] data = new byte[]{1, 2, 3, 4};
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in, 0);
        assertEquals(4, byteBuffer.remaining());
    }

    // Tests exception path for negative maxSize in readToByteBuffer
    @Test(expected = IllegalArgumentException.class)
    public void testReadToByteBuffer_negativeMaxSize_throwsIllegalArgumentException() throws IOException {
        DataUtil.readToByteBuffer(new ByteArrayInputStream(new byte[0]), -1);
    }

    // Tests empty byte buffer helper
    @Test
    public void testEmptyByteBuffer_returnsZeroCapacityBuffer() {
        ByteBuffer buffer = DataUtil.emptyByteBuffer();
        assertNotNull(buffer);
        assertEquals(0, buffer.capacity());
        assertEquals(0, buffer.remaining());
    }

    // Tests content type parsing with valid, quoted, and unsupported charsets
    @Test
    public void testGetCharsetFromContentType_variousHeaders_extractsExpectedCharset() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=utf-8"));
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=\"ISO-8859-1\""));
        assertEquals("GB2312", DataUtil.getCharsetFromContentType("text/html; charset='gb2312'"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=unsupported_charset_xyz"));
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
        assertNull(DataUtil.getCharsetFromContentType(null));
        assertNull(DataUtil.getCharsetFromContentType(""));
    }

    // Tests random mime boundary generation length and format
    @Test
    public void testMimeBoundary_generatesExpectedLengthString() {
        String boundary = DataUtil.mimeBoundary();
        assertNotNull(boundary);
        assertEquals(DataUtil.boundaryLength, boundary.length());
    }

    // Tests UTF-8 BOM detection and decoding
    @Test
    public void testParseInputStream_bomUtf8_detectsAndDecodesCorrectly() throws IOException {
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] text = "<p>BOM UTF-8</p>".getBytes(StandardCharsets.UTF_8);
        byte[] combined = new byte[bom.length + text.length];
        System.arraycopy(bom, 0, combined, 0, bom.length);
        System.arraycopy(text, 0, combined, bom.length, text.length);

        Document doc = DataUtil.load(new ByteArrayInputStream(combined), null, "http://example.com");
        assertEquals("BOM UTF-8", doc.select("p").text());
        assertEquals(StandardCharsets.UTF_8, doc.outputSettings().charset());
    }

    // Tests UTF-16BE BOM detection and decoding
    @Test
    public void testParseInputStream_bomUtf16Be_detectsAndDecodesCorrectly() throws IOException {
        byte[] bom = new byte[]{(byte) 0xFE, (byte) 0xFF};
        byte[] text = "<p>BOM UTF-16BE</p>".getBytes(StandardCharsets.UTF_16BE);
        byte[] combined = new byte[bom.length + text.length];
        System.arraycopy(bom, 0, combined, 0, bom.length);
        System.arraycopy(text, 0, combined, bom.length, text.length);

        Document doc = DataUtil.load(new ByteArrayInputStream(combined), null, "http://example.com");
        assertEquals("BOM UTF-16BE", doc.select("p").text());
    }

    // Tests UTF-16LE BOM detection and decoding
    @Test
    public void testParseInputStream_bomUtf16Le_detectsAndDecodesCorrectly() throws IOException {
        byte[] bom = new byte[]{(byte) 0xFF, (byte) 0xFE};
        byte[] text = "<p>BOM UTF-16LE</p>".getBytes(StandardCharsets.UTF_16LE);
        byte[] combined = new byte[bom.length + text.length];
        System.arraycopy(bom, 0, combined, 0, bom.length);
        System.arraycopy(text, 0, combined, bom.length, text.length);

        Document doc = DataUtil.load(new ByteArrayInputStream(combined), null, "http://example.com");
        assertEquals("BOM UTF-16LE", doc.select("p").text());
    }

    // Tests HTML5 meta charset detection and re-decoding
    @Test
    public void testParseInputStream_metaCharsetHtml5_detectsAndAppliesCharset() throws IOException {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body><p>Meta Charset</p></body></html>";
        Document doc = DataUtil.load(new ByteArrayInputStream(html.getBytes(StandardCharsets.ISO_8859_1)), null, "http://example.com");
        assertEquals("Meta Charset", doc.select("p").text());
        assertEquals(StandardCharsets.ISO_8859_1, doc.outputSettings().charset());
    }

    // Tests meta http-equiv content-type charset detection
    @Test
    public void testParseInputStream_metaHttpEquiv_detectsAndAppliesCharset() throws IOException {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head><body><p>Equiv</p></body></html>";
        Document doc = DataUtil.load(new ByteArrayInputStream(html.getBytes(StandardCharsets.ISO_8859_1)), null, "http://example.com");
        assertEquals("Equiv", doc.select("p").text());
        assertEquals(StandardCharsets.ISO_8859_1, doc.outputSettings().charset());
    }

    // Tests XML declaration encoding attribute detection
    @Test
    public void testParseInputStream_xmlDeclarationEncoding_detectsAndAppliesCharset() throws IOException {
        String xml = "<?xml encoding=\"ISO-8859-1\"?><root><item>XML Item</item></root>";
        Document doc = DataUtil.load(new ByteArrayInputStream(xml.getBytes(StandardCharsets.ISO_8859_1)), null, "http://example.com", Parser.xmlParser());
        assertEquals("XML Item", doc.select("item").text());
        assertEquals(StandardCharsets.ISO_8859_1, doc.outputSettings().charset());
    }

    // Tests XML declaration in comment format detection
    @Test
    public void testParseInputStream_xmlDeclarationComment_detectsAndAppliesCharset() throws IOException {
        String html = "<!--?xml encoding=\"ISO-8859-1\"?><html><body><p>Comment XML</p></body></html>";
        Document doc = DataUtil.load(new ByteArrayInputStream(html.getBytes(StandardCharsets.ISO_8859_1)), null, "http://example.com");
        assertEquals("Comment XML", doc.select("p").text());
        assertEquals(StandardCharsets.ISO_8859_1, doc.outputSettings().charset());
    }

    // Tests stream requiring re-decode when first read does not fully consume stream
    @Test
    public void testParseInputStream_largeDocumentWithMetaCharset_redecodesFully() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta charset=\"ISO-8859-1\"></head><body>");
        for (int i = 0; i < 2000; i++) {
            sb.append("<p>Paragraph number ").append(i).append("</p>");
        }
        sb.append("</body></html>");

        byte[] bytes = sb.toString().getBytes(StandardCharsets.ISO_8859_1);
        Document doc = DataUtil.load(new ByteArrayInputStream(bytes), null, "http://example.com");
        assertEquals(2000, doc.select("p").size());
        assertEquals(StandardCharsets.ISO_8859_1, doc.outputSettings().charset());
    }
}