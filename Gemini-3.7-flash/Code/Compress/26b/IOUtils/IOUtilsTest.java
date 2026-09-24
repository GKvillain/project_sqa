package org.apache.commons.compress.utils;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IOUtilsTest {

    // Tests copy with default buffer size
    @Test
    public void testCopy_validStream_copiesAllBytes() throws IOException {
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        ByteArrayInputStream input = new ByteArrayInputStream(data);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        long count = IOUtils.copy(input, output);

        assertEquals(5, count);
        assertArrayEquals(data, output.toByteArray());
    }

    // Tests copy with custom buffer size
    @Test
    public void testCopy_customBufferSize_copiesAllBytes() throws IOException {
        byte[] data = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        ByteArrayInputStream input = new ByteArrayInputStream(data);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        long count = IOUtils.copy(input, output, 2);

        assertEquals(8, count);
        assertArrayEquals(data, output.toByteArray());
    }

    // Tests copy with empty stream
    @Test
    public void testCopy_emptyStream_returnsZero() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        long count = IOUtils.copy(input, output);

        assertEquals(0, count);
        assertEquals(0, output.size());
    }

    // Tests skip with normal InputStream
    @Test
    public void testSkip_validStream_skipsRequestedBytes() throws IOException {
        byte[] data = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        ByteArrayInputStream input = new ByteArrayInputStream(data);

        long skipped = IOUtils.skip(input, 5);

        assertEquals(5, skipped);
        assertEquals(6, input.read());
    }

    // Tests skip with numToSkip equal to zero
    @Test
    public void testSkip_zeroBytes_returnsZero() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[]{1, 2, 3});

        long skipped = IOUtils.skip(input, 0);

        assertEquals(0, skipped);
    }

    // Tests skip more than available bytes
    @Test
    public void testSkip_moreThanAvailable_returnsAvailableBytes() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[]{1, 2, 3});

        long skipped = IOUtils.skip(input, 10);

        assertEquals(3, skipped);
    }

    // Tests skip when stream's skip() returns 0 but stream has more bytes (Defects4J Compress 26)
    @Test
    public void testSkip_streamSkipReturnsZero_fallsBackToRead() throws IOException {
        final byte[] data = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        InputStream input = new FilterInputStream(new ByteArrayInputStream(data)) {
            private int skipCalls = 0;

            @Override
            public long skip(long n) throws IOException {
                skipCalls++;
                if (skipCalls == 1) {
                    return 2;
                }
                return 0; // returns 0 on subsequent calls, should fall back to read
            }
        };

        long skipped = IOUtils.skip(input, 7);

        assertEquals(7, skipped);
        assertEquals(8, input.read());
    }

    // Tests readFully with array buffer
    @Test
    public void testReadFully_validStream_fillsBuffer() throws IOException {
        byte[] data = new byte[]{10, 20, 30, 40};
        ByteArrayInputStream input = new ByteArrayInputStream(data);
        byte[] buffer = new byte[4];

        int read = IOUtils.readFully(input, buffer);

        assertEquals(4, read);
        assertArrayEquals(data, buffer);
    }

    // Tests readFully when stream has fewer bytes than requested length
    @Test
    public void testReadFully_streamSmallerThanBuffer_readsAvailableBytes() throws IOException {
        byte[] data = new byte[]{10, 20};
        ByteArrayInputStream input = new ByteArrayInputStream(data);
        byte[] buffer = new byte[5];

        int read = IOUtils.readFully(input, buffer, 0, 5);

        assertEquals(2, read);
        assertEquals(10, buffer[0]);
        assertEquals(20, buffer[1]);
        assertEquals(0, buffer[2]);
    }

    // Tests readFully with custom offset and length
    @Test
    public void testReadFully_withOffsetAndLen_readsIntoOffset() throws IOException {
        byte[] data = new byte[]{1, 2, 3};
        ByteArrayInputStream input = new ByteArrayInputStream(data);
        byte[] buffer = new byte[6];

        int read = IOUtils.readFully(input, buffer, 2, 3);

        assertEquals(3, read);
        assertEquals(0, buffer[0]);
        assertEquals(0, buffer[1]);
        assertEquals(1, buffer[2]);
        assertEquals(2, buffer[3]);
        assertEquals(3, buffer[4]);
        assertEquals(0, buffer[5]);
    }

    // Tests readFully with negative length throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadFully_negativeLength_throwsIndexOutOfBoundsException() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[]{1, 2});
        IOUtils.readFully(input, new byte[5], 0, -1);
    }

    // Tests readFully with negative offset throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadFully_negativeOffset_throwsIndexOutOfBoundsException() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[]{1, 2});
        IOUtils.readFully(input, new byte[5], -1, 2);
    }

    // Tests readFully with offset + len exceeding buffer length throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadFully_offsetPlusLenExceedsLength_throwsIndexOutOfBoundsException() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[]{1, 2});
        IOUtils.readFully(input, new byte[5], 3, 3);
    }

    // Tests toByteArray converts input stream to byte array
    @Test
    public void testToByteArray_validStream_returnsByteArray() throws IOException {
        byte[] data = "Hello Compress".getBytes();
        ByteArrayInputStream input = new ByteArrayInputStream(data);

        byte[] result = IOUtils.toByteArray(input);

        assertArrayEquals(data, result);
    }

    // Tests closeQuietly with non-null Closeable
    @Test
    public void testCloseQuietly_validCloseable_closesSuccessfully() {
        final boolean[] closed = new boolean[]{false};
        Closeable c = new Closeable() {
            @Override
            public void close() throws IOException {
                closed[0] = true;
            }
        };

        IOUtils.closeQuietly(c);

        assertTrue(closed[0]);
    }

    // Tests closeQuietly with null Closeable
    @Test
    public void testCloseQuietly_nullCloseable_doesNotThrow() {
        IOUtils.closeQuietly(null);
    }

    // Tests closeQuietly swallows IOException
    @Test
    public void testCloseQuietly_throwingCloseable_swallowsException() {
        Closeable throwingCloseable = new Closeable() {
            @Override
            public void close() throws IOException {
                throw new IOException("Failed to close");
            }
        };

        IOUtils.closeQuietly(throwingCloseable);
    }
}