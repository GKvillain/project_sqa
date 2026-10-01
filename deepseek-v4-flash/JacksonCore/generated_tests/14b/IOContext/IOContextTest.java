package com.fasterxml.jackson.core.io;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.util.BufferRecycler;

public class IOContextTest {

    // Test constructor sets fields correctly
    @Test
    public void testConstructor_setsFieldsCorrectly() {
        BufferRecycler br = new BufferRecycler();
        Object sourceRef = new Object();
        IOContext context = new IOContext(br, sourceRef, true);
        assertSame(sourceRef, context.getSourceReference());
        assertTrue(context.isResourceManaged());
        assertNull(context.getEncoding());
    }

    // Test constructor with managedResource = false
    @Test
    public void testConstructor_managedResourceFalse() {
        IOContext context = new IOContext(new BufferRecycler(), "test", false);
        assertFalse(context.isResourceManaged());
    }

    // Test setEncoding sets the encoding
    @Test
    public void testSetEncoding_setsEncoding() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        context.setEncoding(JsonEncoding.UTF8);
        assertEquals(JsonEncoding.UTF8, context.getEncoding());
    }

    // Test withEncoding returns self and sets encoding
    @Test
    public void testWithEncoding_returnsSelfAndSetsEncoding() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        IOContext result = context.withEncoding(JsonEncoding.UTF16_BE);
        assertSame(context, result);
        assertEquals(JsonEncoding.UTF16_BE, context.getEncoding());
    }

    // Test allocReadIOBuffer allocates a non-null buffer
    @Test
    public void testAllocReadIOBuffer_allocatesBuffer() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        byte[] buffer = context.allocReadIOBuffer();
        assertNotNull(buffer);
    }

    // Test allocReadIOBuffer throws exception on second call (same method, same buffer slot)
    @Test(expected = IllegalStateException.class)
    public void testAllocReadIOBuffer_secondCallThrowsException() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        context.allocReadIOBuffer();
        context.allocReadIOBuffer(); // Should throw IllegalStateException
    }

    // Test allocWriteEncodingBuffer allocates a non-null buffer
    @Test
    public void testAllocWriteEncodingBuffer_allocatesBuffer() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        byte[] buffer = context.allocWriteEncodingBuffer();
        assertNotNull(buffer);
    }

    // Test allocWriteEncodingBuffer throws exception on second call
    @Test(expected = IllegalStateException.class)
    public void testAllocWriteEncodingBuffer_secondCallThrowsException() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        context.allocWriteEncodingBuffer();
        context.allocWriteEncodingBuffer();
    }

    // Test allocBase64Buffer allocates a non-null buffer
    @Test
    public void testAllocBase64Buffer_allocatesBuffer() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        byte[] buffer = context.allocBase64Buffer();
        assertNotNull(buffer);
    }

    // Test allocBase64Buffer throws exception on second call
    @Test(expected = IllegalStateException.class)
    public void testAllocBase64Buffer_secondCallThrowsException() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        context.allocBase64Buffer();
        context.allocBase64Buffer();
    }

    // Test allocTokenBuffer allocates a non-null buffer
    @Test
    public void testAllocTokenBuffer_allocatesBuffer() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        char[] buffer = context.allocTokenBuffer();
        assertNotNull(buffer);
    }

    // Test allocTokenBuffer throws exception on second call
    @Test(expected = IllegalStateException.class)
    public void testAllocTokenBuffer_secondCallThrowsException() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        context.allocTokenBuffer();
        context.allocTokenBuffer();
    }

    // Test allocConcatBuffer allocates a non-null buffer
    @Test
    public void testAllocConcatBuffer_allocatesBuffer() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        char[] buffer = context.allocConcatBuffer();
        assertNotNull(buffer);
    }

    // Test allocNameCopyBuffer allocates a non-null buffer
    @Test
    public void testAllocNameCopyBuffer_allocatesBuffer() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        char[] buffer = context.allocNameCopyBuffer(256);
        assertNotNull(buffer);
    }

    // Test allocNameCopyBuffer throws exception on second call
    @Test(expected = IllegalStateException.class)
    public void testAllocNameCopyBuffer_secondCallThrowsException() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        context.allocNameCopyBuffer(256);
        context.allocNameCopyBuffer(256);
    }

    // Test releaseReadIOBuffer releases and does not throw when buffer is the same one allocated
    @Test
    public void testReleaseReadIOBuffer_releasesBuffer() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        byte[] buffer = context.allocReadIOBuffer();
        context.releaseReadIOBuffer(buffer);
        // After release, can allocate again (since _readIOBuffer is set to null)
        byte[] buffer2 = context.allocReadIOBuffer();
        assertNotNull(buffer2);
    }

    // Test releaseReadIOBuffer with null input does nothing
    @Test
    public void testReleaseReadIOBuffer_nullInput_doesNothing() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        // Should not throw
        context.releaseReadIOBuffer(null);
    }

    // Test releaseReadIOBuffer with wrong buffer throws IllegalArgumentException (branch toRelease != src and toRelease.length <= src.length)
    @Test(expected = IllegalArgumentException.class)
    public void testReleaseReadIOBuffer_wrongBufferSmaller_throwsException() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        byte[] allocated = context.allocReadIOBuffer();
        byte[] wrongBuffer = new byte[allocated.length / 2]; // smaller buffer
        context.releaseReadIOBuffer(wrongBuffer);
    }

    // Test releaseReadIOBuffer with larger buffer than allocated - should succeed (allowed upgrade)
    @Test
    public void testReleaseReadIOBuffer_largerBuffer_success() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        byte[] allocated = context.allocReadIOBuffer();
        byte[] largeBuffer = new byte[allocated.length + 100]; // larger buffer
        context.releaseReadIOBuffer(largeBuffer);
    }

    // Test releaseWriteEncodingBuffer with null input does nothing
    @Test
    public void testReleaseWriteEncodingBuffer_nullInput_doesNothing() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        context.releaseWriteEncodingBuffer(null);
    }

    // Test releaseWriteEncodingBuffer with wrong buffer throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testReleaseWriteEncodingBuffer_wrongBufferSmaller_throwsException() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        byte[] allocated = context.allocWriteEncodingBuffer();
        byte[] wrongBuffer = new byte[allocated.length / 2];
        context.releaseWriteEncodingBuffer(wrongBuffer);
    }

    // Test releaseBase64Buffer with null input does nothing
    @Test
    public void testReleaseBase64Buffer_nullInput_doesNothing() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        context.releaseBase64Buffer(null);
    }

    // Test releaseTokenBuffer with null input does nothing
    @Test
    public void testReleaseTokenBuffer_nullInput_doesNothing() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        context.releaseTokenBuffer(null);
    }

    // Test releaseConcatBuffer with null input does nothing
    @Test
    public void testReleaseConcatBuffer_nullInput_doesNothing() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        context.releaseConcatBuffer(null);
    }

    // Test releaseNameCopyBuffer with null input does nothing
    @Test
    public void testReleaseNameCopyBuffer_nullInput_doesNothing() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        context.releaseNameCopyBuffer(null);
    }

    // Test constructTextBuffer returns a non-null TextBuffer
    @Test
    public void testConstructTextBuffer_returnsNonNull() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        assertNotNull(context.constructTextBuffer());
    }

    // New tests for uncovered parts

    @Test
    public void testReleaseWriteEncodingBuffer_releasesBuffer() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        byte[] buffer = context.allocWriteEncodingBuffer();
        context.releaseWriteEncodingBuffer(buffer);
        byte[] buffer2 = context.allocWriteEncodingBuffer();
        assertNotNull(buffer2);
    }

    @Test
    public void testReleaseWriteEncodingBuffer_largerBuffer_success() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        byte[] allocated = context.allocWriteEncodingBuffer();
        byte[] largeBuffer = new byte[allocated.length + 100];
        context.releaseWriteEncodingBuffer(largeBuffer);
    }

    @Test
    public void testReleaseBase64Buffer_releasesBuffer() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        byte[] buffer = context.allocBase64Buffer();
        context.releaseBase64Buffer(buffer);
        byte[] buffer2 = context.allocBase64Buffer();
        assertNotNull(buffer2);
    }

    @Test
    public void testReleaseTokenBuffer_releasesBuffer() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        char[] buffer = context.allocTokenBuffer();
        context.releaseTokenBuffer(buffer);
        char[] buffer2 = context.allocTokenBuffer();
        assertNotNull(buffer2);
    }

    @Test
    public void testReleaseConcatBuffer_releasesBuffer() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        char[] buffer = context.allocConcatBuffer();
        context.releaseConcatBuffer(buffer);
        char[] buffer2 = context.allocConcatBuffer();
        assertNotNull(buffer2);
    }

    @Test
    public void testReleaseNameCopyBuffer_releasesBuffer() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        char[] buffer = context.allocNameCopyBuffer(256);
        context.releaseNameCopyBuffer(buffer);
        char[] buffer2 = context.allocNameCopyBuffer(256);
        assertNotNull(buffer2);
    }

    @Test
    public void testAllocConcatBuffer_secondCallThrowsException() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        context.allocConcatBuffer();
        try {
            context.allocConcatBuffer();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void testReleaseBase64Buffer_wrongBufferSmaller_throwsException() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        byte[] allocated = context.allocBase64Buffer();
        byte[] wrongBuffer = new byte[allocated.length / 2];
        try {
            context.releaseBase64Buffer(wrongBuffer);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testReleaseBase64Buffer_largerBuffer_success() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        byte[] allocated = context.allocBase64Buffer();
        byte[] largeBuffer = new byte[allocated.length + 100];
        context.releaseBase64Buffer(largeBuffer);
    }

    @Test
    public void testReleaseTokenBuffer_wrongBufferSmaller_throwsException() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        char[] allocated = context.allocTokenBuffer();
        char[] wrongBuffer = new char[allocated.length / 2];
        try {
            context.releaseTokenBuffer(wrongBuffer);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testReleaseTokenBuffer_largerBuffer_success() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        char[] allocated = context.allocTokenBuffer();
        char[] largeBuffer = new char[allocated.length + 100];
        context.releaseTokenBuffer(largeBuffer);
    }

    @Test
    public void testReleaseConcatBuffer_wrongBufferSmaller_throwsException() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        char[] allocated = context.allocConcatBuffer();
        char[] wrongBuffer = new char[allocated.length / 2];
        try {
            context.releaseConcatBuffer(wrongBuffer);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testReleaseConcatBuffer_largerBuffer_success() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        char[] allocated = context.allocConcatBuffer();
        char[] largeBuffer = new char[allocated.length + 100];
        context.releaseConcatBuffer(largeBuffer);
    }

    @Test
    public void testReleaseNameCopyBuffer_wrongBufferSmaller_throwsException() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        char[] allocated = context.allocNameCopyBuffer(256);
        char[] wrongBuffer = new char[allocated.length / 2];
        try {
            context.releaseNameCopyBuffer(wrongBuffer);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testReleaseNameCopyBuffer_largerBuffer_success() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        char[] allocated = context.allocNameCopyBuffer(256);
        char[] largeBuffer = new char[allocated.length + 100];
        context.releaseNameCopyBuffer(largeBuffer);
    }

    @Test
    public void testAllocNameCopyBuffer_negativeSize_throwsException() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        try {
            context.allocNameCopyBuffer(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testAllocNameCopyBuffer_zeroSize_allocates() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        char[] buffer = context.allocNameCopyBuffer(0);
        assertNotNull(buffer);
        assertEquals(0, buffer.length);
    }

    @Test
    public void testSetNullEncoding() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        context.setEncoding(null);
        assertNull(context.getEncoding());
    }

    @Test
    public void testWithEncoding_nullEncoding() {
        IOContext context = new IOContext(new BufferRecycler(), null, false);
        IOContext result = context.withEncoding(null);
        assertSame(context, result);
        assertNull(context.getEncoding());
    }
}