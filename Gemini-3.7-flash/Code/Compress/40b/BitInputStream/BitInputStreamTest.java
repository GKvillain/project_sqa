package org.apache.commons.compress.utils;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BitInputStreamTest {

    // Tests reading single bit in Little Endian order
    @Test
    public void testReadBits_singleBitLittleEndian_returnsCorrectBit() throws IOException {
        final byte[] bytes = new byte[] { 0x01 };
        final BitInputStream bis = new BitInputStream(new ByteArrayInputStream(bytes), ByteOrder.LITTLE_ENDIAN);
        assertEquals(1L, bis.readBits(1));
        bis.close();
    }

    // Tests reading single bit in Big Endian order
    @Test
    public void testReadBits_singleBitBigEndian_returnsCorrectBit() throws IOException {
        final byte[] bytes = new byte[] { (byte) 0x80 };
        final BitInputStream bis = new BitInputStream(new ByteArrayInputStream(bytes), ByteOrder.BIG_ENDIAN);
        assertEquals(1L, bis.readBits(1));
        bis.close();
    }

    // Tests reading 0 bits returns 0
    @Test
    public void testReadBits_zeroCount_returnsZero() throws IOException {
        final byte[] bytes = new byte[] { (byte) 0xFF };
        final BitInputStream bis = new BitInputStream(new ByteArrayInputStream(bytes), ByteOrder.LITTLE_ENDIAN);
        assertEquals(0L, bis.readBits(0));
        bis.close();
    }

    // Tests negative bit count throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testReadBits_negativeCount_throwsIllegalArgumentException() throws IOException {
        final byte[] bytes = new byte[] { 0x01 };
        final BitInputStream bis = new BitInputStream(new ByteArrayInputStream(bytes), ByteOrder.LITTLE_ENDIAN);
        try {
            bis.readBits(-1);
        } finally {
            bis.close();
        }
    }

    // Tests bit count > 63 throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testReadBits_countGreaterThan63_throwsIllegalArgumentException() throws IOException {
        final byte[] bytes = new byte[] { 0x01 };
        final BitInputStream bis = new BitInputStream(new ByteArrayInputStream(bytes), ByteOrder.LITTLE_ENDIAN);
        try {
            bis.readBits(64);
        } finally {
            bis.close();
        }
    }

    // Tests reading reaching EOF returns -1
    @Test
    public void testReadBits_eof_returnsNegativeOne() throws IOException {
        final byte[] bytes = new byte[0];
        final BitInputStream bis = new BitInputStream(new ByteArrayInputStream(bytes), ByteOrder.LITTLE_ENDIAN);
        assertEquals(-1L, bis.readBits(1));
        bis.close();
    }

    // Tests reading multiple bits across byte boundaries in Little Endian
    @Test
    public void testReadBits_acrossByteBoundariesLittleEndian_returnsCorrectResult() throws IOException {
        final byte[] bytes = new byte[] { 0x23, 0x45 };
        final BitInputStream bis = new BitInputStream(new ByteArrayInputStream(bytes), ByteOrder.LITTLE_ENDIAN);
        assertEquals(0x03L, bis.readBits(4));
        assertEquals(0x52L, bis.readBits(8));
        assertEquals(0x04L, bis.readBits(4));
        bis.close();
    }

    // Tests reading multiple bits across byte boundaries in Big Endian
    @Test
    public void testReadBits_acrossByteBoundariesBigEndian_returnsCorrectResult() throws IOException {
        final byte[] bytes = new byte[] { 0x23, 0x45 };
        final BitInputStream bis = new BitInputStream(new ByteArrayInputStream(bytes), ByteOrder.BIG_ENDIAN);
        assertEquals(0x02L, bis.readBits(4));
        assertEquals(0x34L, bis.readBits(8));
        assertEquals(0x05L, bis.readBits(4));
        bis.close();
    }

    // Tests reading maximum allowed 63 bits in Little Endian
    @Test
    public void testReadBits_maxCount63LittleEndian_returnsCorrectResult() throws IOException {
        final byte[] bytes = new byte[] {
            (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD,
            (byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44
        };
        final BitInputStream bis = new BitInputStream(new ByteArrayInputStream(bytes), ByteOrder.LITTLE_ENDIAN);
        final long expected = (0x44332211DDCCBBAAL >>> 1) & 0x7FFFFFFFFFFFFFFFL;
        final long result = bis.readBits(63);
        assertEquals(0x44332211DDCCBBAAL & 0x7FFFFFFFFFFFFFFFL, result);
        bis.close();
    }

    // Tests reading maximum allowed 63 bits in Big Endian
    @Test
    public void testReadBits_maxCount63BigEndian_returnsCorrectResult() throws IOException {
        final byte[] bytes = new byte[] {
            (byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44,
            (byte) 0x55, (byte) 0x66, (byte) 0x77, (byte) 0x88
        };
        final BitInputStream bis = new BitInputStream(new ByteArrayInputStream(bytes), ByteOrder.BIG_ENDIAN);
        final long result = bis.readBits(63);
        final long expected = 0x1122334455667788L >>> 1;
        assertEquals(expected, result);
        bis.close();
    }

    // Tests clearing bit cache
    @Test
    public void testClearBitCache_clearsCachedBits() throws IOException {
        final byte[] bytes = new byte[] { 0x23, 0x45 };
        final BitInputStream bis = new BitInputStream(new ByteArrayInputStream(bytes), ByteOrder.LITTLE_ENDIAN);
        assertEquals(0x03L, bis.readBits(4));
        bis.clearBitCache();
        assertEquals(0x45L, bis.readBits(8));
        bis.close();
    }

    // Tests close closes underlying input stream
    @Test
    public void testClose_closesUnderlyingStream() throws IOException {
        final boolean[] closed = new boolean[] { false };
        final InputStream in = new ByteArrayInputStream(new byte[] { 0x01 }) {
            @Override
            public void close() throws IOException {
                closed[0] = true;
                super.close();
            }
        };
        final BitInputStream bis = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);
        bis.close();
        assertTrue(closed[0]);
    }
}