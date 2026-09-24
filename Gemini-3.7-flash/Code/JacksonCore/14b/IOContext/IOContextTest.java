package com.fasterxml.jackson.core.io;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.TextBuffer;

public class IOContextTest {

    private BufferRecycler _recycler;
    private Object _sourceRef;
    private IOContext _context;

    @Before
    public void setUp() {
        _recycler = new BufferRecycler();
        _sourceRef = "test-source-reference";
        _context = new IOContext(_recycler, _sourceRef, true);
    }

    // Tests initialization and getter methods for configuration
    @Test
    public void testConstructAndGetters_validInputs_returnsCorrectValues() {
        assertSame(_sourceRef, _context.getSourceReference());
        assertTrue(_context.isResourceManaged());
        assertNull(_context.getEncoding());

        IOContext unmanagedContext = new IOContext(_recycler, null, false);
        assertNull(unmanagedContext.getSourceReference());
        assertFalse(unmanagedContext.isResourceManaged());
    }

    // Tests setting and chaining encoding configuration
    @Test
    public void testSetAndWithEncoding_validEncoding_updatesEncoding() {
        _context.setEncoding(JsonEncoding.UTF8);
        assertSame(JsonEncoding.UTF8, _context.getEncoding());

        IOContext chained = _context.withEncoding(JsonEncoding.UTF16_BE);
        assertSame(_context, chained);
        assertSame(JsonEncoding.UTF16_BE, _context.getEncoding());
    }

    // Tests construction of TextBuffer using context's buffer recycler
    @Test
    public void testConstructTextBuffer_validContext_returnsNonNullTextBuffer() {
        TextBuffer textBuffer = _context.constructTextBuffer();
        assertNotNull(textBuffer);
    }

    // Tests allocation and release of read IO byte buffer
    @Test
    public void testAllocAndReleaseReadIOBuffer_validAllocation_allocatesAndReleasesSuccessfully() {
        byte[] buf = _context.allocReadIOBuffer();
        assertNotNull(buf);
        assertTrue(buf.length > 0);

        _context.releaseReadIOBuffer(buf);
        // After release, should allow re-allocation
        byte[] buf2 = _context.allocReadIOBuffer(500);
        assertNotNull(buf2);
        assertTrue(buf2.length >= 500);
        _context.releaseReadIOBuffer(buf2);
    }

    // Tests allocation and release of write encoding byte buffer
    @Test
    public void testAllocAndReleaseWriteEncodingBuffer_validAllocation_allocatesAndReleasesSuccessfully() {
        byte[] buf = _context.allocWriteEncodingBuffer();
        assertNotNull(buf);
        assertTrue(buf.length > 0);

        _context.releaseWriteEncodingBuffer(buf);
        byte[] buf2 = _context.allocWriteEncodingBuffer(1000);
        assertNotNull(buf2);
        assertTrue(buf2.length >= 1000);
        _context.releaseWriteEncodingBuffer(buf2);
    }

    // Tests allocation and release of base64 byte buffer
    @Test
    public void testAllocAndReleaseBase64Buffer_validAllocation_allocatesAndReleasesSuccessfully() {
        byte[] buf = _context.allocBase64Buffer();
        assertNotNull(buf);
        assertTrue(buf.length > 0);

        _context.releaseBase64Buffer(buf);
    }

    // Tests allocation and release of token char buffer
    @Test
    public void testAllocAndReleaseTokenBuffer_validAllocation_allocatesAndReleasesSuccessfully() {
        char[] buf = _context.allocTokenBuffer();
        assertNotNull(buf);
        assertTrue(buf.length > 0);

        _context.releaseTokenBuffer(buf);
        char[] buf2 = _context.allocTokenBuffer(200);
        assertNotNull(buf2);
        assertTrue(buf2.length >= 200);
        _context.releaseTokenBuffer(buf2);
    }

    // Tests allocation and release of concat char buffer
    @Test
    public void testAllocAndReleaseConcatBuffer_validAllocation_allocatesAndReleasesSuccessfully() {
        char[] buf = _context.allocConcatBuffer();
        assertNotNull(buf);
        assertTrue(buf.length > 0);

        _context.releaseConcatBuffer(buf);
    }

    // Tests allocation and release of name copy char buffer
    @Test
    public void testAllocAndReleaseNameCopyBuffer_validAllocation_allocatesAndReleasesSuccessfully() {
        char[] buf = _context.allocNameCopyBuffer(128);
        assertNotNull(buf);
        assertTrue(buf.length >= 128);

        _context.releaseNameCopyBuffer(buf);
    }

    // Tests exception path when allocating read IO buffer twice without release
    @Test(expected = IllegalStateException.class)
    public void testAllocReadIOBuffer_doubleAlloc_throwsIllegalStateException() {
        _context.allocReadIOBuffer();
        _context.allocReadIOBuffer();
    }

    // Tests exception path when allocating write encoding buffer twice without release
    @Test(expected = IllegalStateException.class)
    public void testAllocWriteEncodingBuffer_doubleAlloc_throwsIllegalStateException() {
        _context.allocWriteEncodingBuffer();
        _context.allocWriteEncodingBuffer();
    }

    // Tests exception path when allocating token buffer twice without release
    @Test(expected = IllegalStateException.class)
    public void testAllocTokenBuffer_doubleAlloc_throwsIllegalStateException() {
        _context.allocTokenBuffer();
        _context.allocTokenBuffer();
    }

    // Tests releasing null buffers safely without exception
    @Test
    public void testReleaseBuffers_nullBuffer_doesNothing() {
        _context.releaseReadIOBuffer(null);
        _context.releaseWriteEncodingBuffer(null);
        _context.releaseBase64Buffer(null);
        _context.releaseTokenBuffer(null);
        _context.releaseConcatBuffer(null);
        _context.releaseNameCopyBuffer(null);
    }

    // Tests releasing a different byte buffer with same length (Defects4J JacksonCore-14)
    @Test
    public void testReleaseReadIOBuffer_sameLengthDifferentBuffer_releasesSuccessfully() {
        byte[] orig = _context.allocReadIOBuffer();
        byte[] replacement = new byte[orig.length];
        _context.releaseReadIOBuffer(replacement);
    }

    // Tests releasing a different char buffer with same length (Defects4J JacksonCore-14)
    @Test
    public void testReleaseTokenBuffer_sameLengthDifferentBuffer_releasesSuccessfully() {
        char[] orig = _context.allocTokenBuffer();
        char[] replacement = new char[orig.length];
        _context.releaseTokenBuffer(replacement);
    }

    // Tests releasing a different char buffer with larger length
    @Test
    public void testReleaseConcatBuffer_largerBuffer_releasesSuccessfully() {
        char[] orig = _context.allocConcatBuffer();
        char[] replacement = new char[orig.length + 100];
        _context.releaseConcatBuffer(replacement);
    }

    // Tests exception when releasing a byte buffer smaller than allocated
    @Test(expected = IllegalArgumentException.class)
    public void testReleaseReadIOBuffer_smallerBuffer_throwsIllegalArgumentException() {
        byte[] orig = _context.allocReadIOBuffer();
        byte[] smaller = new byte[orig.length - 1];
        _context.releaseReadIOBuffer(smaller);
    }

    // Tests exception when releasing a char buffer smaller than allocated
    @Test(expected = IllegalArgumentException.class)
    public void testReleaseTokenBuffer_smallerBuffer_throwsIllegalArgumentException() {
        char[] orig = _context.allocTokenBuffer();
        char[] smaller = new char[orig.length - 1];
        _context.releaseTokenBuffer(smaller);
    }
}