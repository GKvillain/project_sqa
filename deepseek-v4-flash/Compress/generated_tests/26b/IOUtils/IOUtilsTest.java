package org.apache.commons.compress.utils;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;

public class IOUtilsTest {

    // Tests normal copy with small data
    @Test
    public void testCopy_normalInput_returnsCount() throws IOException {
        byte[] data = "Hello, World!".getBytes("UTF-8");
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long copied = IOUtils.copy(in, out);
        assertEquals(data.length, copied);
        assertArrayEquals(data, out.toByteArray());
    }

    // Tests copy with a larger buffer size
    @Test
    public void testCopy_largeBuffer_works() throws IOException {
        byte[] data = new byte[20000];
        for (int i = 0; i < 20000; i++) {
            data[i] = (byte) (i % 256);
        }
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long copied = IOUtils.copy(in, out, 4096);
        assertEquals(data.length, copied);
        assertArrayEquals(data, out.toByteArray());
    }

    // Tests copy with an empty input stream
    @Test
    public void testCopy_emptyStream_returnsZero() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertEquals(0, IOUtils.copy(in, out));
        assertEquals(0, out.size());
    }

    // Tests skip with normal available bytes
    @Test
    public void testSkip_normal_returnsSkipped() throws IOException {
        byte[] data = new byte[100];
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        long skipped = IOUtils.skip(in, 50);
        assertEquals(50, skipped);
        assertEquals(50, in.available());
    }

    // Tests skip when requested more than available
    @Test
    public void testSkip_moreThanAvailable_returnsAvailable() throws IOException {
        byte[] data = new byte[30];
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        long skipped = IOUtils.skip(in, 100);
        assertEquals(30, skipped);
        assertEquals(0, in.available());
    }

    // Tests skip with zero count
    @Test
    public void testSkip_zero_returnsZero() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[10]);
        assertEquals(0, IOUtils.skip(in, 0));
    }

    // Tests skip when underlying skip returns 0 (defect path)
    @Test
    public void testSkip_skipReturnsZero_shouldFallbackToRead() throws IOException {
        final byte[] data = new byte[100];
        for (int i = 0; i < 100; i++) {
            data[i] = (byte) i;
        }
        InputStream in = new ByteArrayInputStream(data) {
            @Override
            public long skip(long n) {
                return 0;
            }
        };
        long skipped = IOUtils.skip(in, 100);
        assertEquals(100, skipped); // Expected correct behavior, but bug causes 0
    }

    // Tests readFully normal case
    @Test
    public void testReadFully_normal_returnsCount() throws IOException {
        byte[] data = "Apache Commons".getBytes("UTF-8");
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        byte[] buf = new byte[data.length];
        int read = IOUtils.readFully(in, buf);
        assertEquals(data.length, read);
        assertArrayEquals(data, buf);
    }

    // Tests readFully when buffer is larger than available data
    @Test
    public void testReadFully_partialBuffer_returnsLess() throws IOException {
        byte[] data = "Short".getBytes("UTF-8");
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        byte[] buf = new byte[100];
        int read = IOUtils.readFully(in, buf);
        assertEquals(data.length, read);
        for (int i = 0; i < data.length; i++) {
            assertEquals(data[i], buf[i]);
        }
    }

    // Tests readFully with offset and length parameters
    @Test
    public void testReadFully_offsetLen_returnsLen() throws IOException {
        byte[] data = "0123456789".getBytes("UTF-8");
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        byte[] buf = new byte[20];
        int read = IOUtils.readFully(in, buf, 5, 4);
        assertEquals(4, read);
        assertEquals('0', buf[5]);
        assertEquals('1', buf[6]);
        assertEquals('2', buf[7]);
        assertEquals('3', buf[8]);
    }

    // Tests readFully with negative offset (throws exception)
    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadFully_invalidOffset_throwsException() throws IOException {
        byte[] data = "test".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        IOUtils.readFully(in, new byte[10], -1, 5);
    }

    // Tests readFully with negative length (throws exception)
    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadFully_invalidLen_throwsException() throws IOException {
        byte[] data = "test".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        IOUtils.readFully(in, new byte[10], 0, -1);
    }

    // Tests readFully when offset+len exceeds buffer length (throws exception)
    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadFully_offsetLenExceedsLength_throwsException() throws IOException {
        byte[] data = "test".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        IOUtils.readFully(in, new byte[5], 3, 3);
    }

    // Tests toByteArray normal content
    @Test
    public void testToByteArray_normal_returnsBytes() throws IOException {
        byte[] data = "Hello".getBytes("UTF-8");
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        byte[] result = IOUtils.toByteArray(in);
        assertArrayEquals(data, result);
    }

    // Tests toByteArray with empty input
    @Test
    public void testToByteArray_empty_returnsEmptyArray() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        byte[] result = IOUtils.toByteArray(in);
        assertEquals(0, result.length);
    }

    // Tests closeQuietly with null
    @Test
    public void testCloseQuietly_null_doesNothing() {
        IOUtils.closeQuietly(null);
        // no exception expected
    }

    // Tests closeQuietly with a plain closeable
    @Test
    public void testCloseQuietly_closeable_closes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.closeQuietly(baos);
        // no exception, just ensure it ran
    }

    // Tests closeQuietly swallows IOException
    @Test
    public void testCloseQuietly_closeableWithIOException_swallowed() {
        Closeable c = new Closeable() {
            @Override
            public void close() throws IOException {
                throw new IOException("test");
            }
        };
        IOUtils.closeQuietly(c);
        // no exception should propagate
    }
}