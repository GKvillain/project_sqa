package org.apache.commons.compress.compressors.bzip2;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit 4 test class for BZip2CompressorInputStream.
 * Focus: defect detection, branch coverage, line coverage.
 */
public class BZip2CompressorInputStreamTest {

    private static final String HELLO = "Hello, BZip2!";
    private static final String WORLD = "World";

    private byte[] validBz2Single;
    private byte[] validBz2Concatenated;
    private byte[] validBz2SingleHello;

    @Before
    public void setUp() throws IOException {
        // Create valid single stream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (BZip2CompressorOutputStream bzOut = new BZip2CompressorOutputStream(baos)) {
            bzOut.write(HELLO.getBytes("UTF-8"));
        }
        validBz2SingleHello = baos.toByteArray();

        // Create two concatenated streams
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        BZip2CompressorOutputStream bzOut1 = new BZip2CompressorOutputStream(baos2);
        bzOut1.write(HELLO.getBytes("UTF-8"));
        bzOut1.close();
        BZip2CompressorOutputStream bzOut2 = new BZip2CompressorOutputStream(baos2);
        bzOut2.write(WORLD.getBytes("UTF-8"));
        bzOut2.close();
        validBz2Concatenated = baos2.toByteArray();

        // Single non-concatenated (same as validBz2SingleHello)
        validBz2Single = validBz2SingleHello;
    }

    @After
    public void tearDown() {
        // no resources to clean
    }

    // ---- matches tests ----

    @Test
    public void testMatches_validSignature_returnsTrue() {
        byte[] sig = new byte[] {'B', 'Z', 'h'};
        assertTrue(BZip2CompressorInputStream.matches(sig, 3));
    }

    @Test
    public void testMatches_shortLength_returnsFalse() {
        byte[] sig = new byte[] {'B', 'Z'};
        assertFalse(BZip2CompressorInputStream.matches(sig, 2));
    }

    @Test
    public void testMatches_wrongFirstByte_returnsFalse() {
        byte[] sig = new byte[] {'X', 'Z', 'h'};
        assertFalse(BZip2CompressorInputStream.matches(sig, 3));
    }

    @Test
    public void testMatches_wrongSecondByte_returnsFalse() {
        byte[] sig = new byte[] {'B', 'X', 'h'};
        assertFalse(BZip2CompressorInputStream.matches(sig, 3));
    }

    @Test
    public void testMatches_wrongThirdByte_returnsFalse() {
        byte[] sig = new byte[] {'B', 'Z', 'x'};
        assertFalse(BZip2CompressorInputStream.matches(sig, 3));
    }

    // ---- constructor tests ----

    @Test(expected = NullPointerException.class)
    public void testConstructor_nullInputStream_throwsNullPointerException() throws IOException {
        new BZip2CompressorInputStream(null);
    }

    @Test(expected = IOException.class)
    public void testConstructor_invalidMagic_throwsIOException() throws IOException {
        byte[] invalid = new byte[] {0, 1, 2, 3};
        new BZip2CompressorInputStream(new ByteArrayInputStream(invalid));
    }

    @Test(expected = IOException.class)
    public void testConstructor_invalidBlockSize_throwsIOException() throws IOException {
        byte[] magic = new byte[] {'B', 'Z', 'h', '0'};
        new BZip2CompressorInputStream(new ByteArrayInputStream(magic));
    }

    // ---- read() tests ----

    @Test
    public void testRead_normalData_returnsCorrectContent() throws IOException {
        try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(validBz2SingleHello))) {
            byte[] buf = new byte[256];
            int len = bzIn.read(buf);
            assertTrue("should read something", len > 0);
            String result = new String(buf, 0, len, "UTF-8");
            assertEquals(HELLO, result);
        }
    }

    @Test
    public void testRead_concatenatedStreams_readsBoth() throws IOException {
        try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(validBz2Concatenated), true)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[256];
            int len;
            while ((len = bzIn.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            String result = baos.toString("UTF-8");
            assertEquals(HELLO + WORLD, result);
        }
    }

    @Test
    public void testRead_nonConcatenatedStreams_stopsAfterFirst() throws IOException {
        try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(validBz2Concatenated), false)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[256];
            int len;
            while ((len = bzIn.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            String result = baos.toString("UTF-8");
            assertEquals(HELLO, result);
        }
    }

    @Test
    public void testRead_readZeroLengthBuffer_returnsZero() throws IOException {
        try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(validBz2SingleHello))) {
            int r = bzIn.read(new byte[0], 0, 0);
            assertEquals(0, r);
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_negativeOffset_throwsException() throws IOException {
        try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(validBz2SingleHello))) {
            bzIn.read(new byte[10], -1, 5);
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_negativeLength_throwsException() throws IOException {
        try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(validBz2SingleHello))) {
            bzIn.read(new byte[10], 0, -1);
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_offsLengthExceedLength_throwsException() throws IOException {
        try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(validBz2SingleHello))) {
            bzIn.read(new byte[10], 8, 5);
        }
    }

    @Test
    public void testRead_readSingleByte_returnsCorrectValue() throws IOException {
        try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(validBz2SingleHello))) {
            int first = bzIn.read();
            assertTrue("should be positive", first >= 0);
            byte[] expected = HELLO.getBytes("UTF-8");
            assertEquals(expected[0] & 0xFF, first);
        }
    }

    @Test(expected = IOException.class)
    public void testRead_emptyStream_throwsIOException() throws IOException {
        new BZip2CompressorInputStream(new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = IOException.class)
    public void testRead_truncatedStream_throwsIOException() throws IOException {
        // truncate valid stream by removing last few bytes
        byte[] truncated = Arrays.copyOf(validBz2SingleHello, validBz2SingleHello.length - 10);
        try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(truncated))) {
            while (bzIn.read() != -1) {
                // consume
            }
        }
    }

    // ---- close() tests ----

    @Test(expected = IOException.class)
    public void testRead_afterClose_throwsIOException() throws IOException {
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(validBz2SingleHello));
        bzIn.close();
        bzIn.read(); // should throw IOException
    }

    @Test
    public void testClose_multipleClose_doesNotThrow() throws IOException {
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(
                new ByteArrayInputStream(validBz2SingleHello));
        bzIn.close();
        bzIn.close(); // should not throw
    }
}