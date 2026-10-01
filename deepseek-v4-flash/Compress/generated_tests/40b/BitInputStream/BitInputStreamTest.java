package org.apache.commons.compress.utils;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

public class BitInputStreamTest {

    private InputStream createInputStream(int... bytes) {
        byte[] data = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            data[i] = (byte) bytes[i];
        }
        return new ByteArrayInputStream(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadBits_negativeCount_throwsException() throws IOException {
        BitInputStream bis = new BitInputStream(new ByteArrayInputStream(new byte[0]), ByteOrder.BIG_ENDIAN);
        bis.readBits(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadBits_countGreaterThan63_throwsException() throws IOException {
        BitInputStream bis = new BitInputStream(new ByteArrayInputStream(new byte[0]), ByteOrder.LITTLE_ENDIAN);
        bis.readBits(64);
    }

    @Test
    public void testReadBits_count1_BigEndian_returnsCorrectBits() throws IOException {
        InputStream in = createInputStream(0xAB, 0xCD);
        BitInputStream bis = new BitInputStream(in, ByteOrder.BIG_ENDIAN);
        assertEquals(1, bis.readBits(1));
    }

    @Test
    public void testReadBits_count1_LittleEndian_returnsCorrectBits() throws IOException {
        InputStream in = createInputStream(0xAB, 0xCD);
        BitInputStream bis = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);
        assertEquals(1, bis.readBits(1));
    }

    @Test
    public void testReadBits_multipleBitsBigEndian() throws IOException {
        InputStream in = createInputStream(0xAB, 0xCD);
        BitInputStream bis = new BitInputStream(in, ByteOrder.BIG_ENDIAN);
        assertEquals(21, bis.readBits(5));
        assertEquals(973, bis.readBits(11));
        assertEquals(-1, bis.readBits(1));
    }

    @Test
    public void testReadBits_multipleBitsLittleEndian() throws IOException {
        InputStream in = createInputStream(0xAB, 0xCD);
        BitInputStream bis = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);
        assertEquals(11, bis.readBits(5));
        assertEquals(1485, bis.readBits(11));
        assertEquals(-1, bis.readBits(1));
    }

    @Test
    public void testReadBits_count63_LittleEndian_returnsCorrectBits() throws IOException {
        InputStream in = createInputStream(0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF);
        BitInputStream bis = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);
        assertEquals(0x7FFFFFFFFFFFFFFFL, bis.readBits(63));
        assertEquals(1, bis.readBits(1));
        assertEquals(-1, bis.readBits(1));
    }

    @Test
    public void testReadBits_count63_BigEndian_returnsCorrectBits() throws IOException {
        InputStream in = createInputStream(0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF);
        BitInputStream bis = new BitInputStream(in, ByteOrder.BIG_ENDIAN);
        assertEquals(0x7FFFFFFFFFFFFFFFL, bis.readBits(63));
        assertEquals(1, bis.readBits(1));
        assertEquals(-1, bis.readBits(1));
    }

    @Test
    public void testReadBits_count58_LittleEndian_returnsCorrectBits() throws IOException {
        InputStream in = createInputStream(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08);
        BitInputStream bis = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);
        long expected = 0x0807060504030201L & 0x3FFFFFFFFFFFFFFL;
        assertEquals(expected, bis.readBits(58));
        assertEquals(0, bis.readBits(6));
    }

    @Test
    public void testReadBits_count58_BigEndian_returnsCorrectBits() throws IOException {
        InputStream in = createInputStream(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08);
        BitInputStream bis = new BitInputStream(in, ByteOrder.BIG_ENDIAN);
        long expected = (0x0102030405060708L >> 6) & 0x3FFFFFFFFFFFFFFL;
        assertEquals(expected, bis.readBits(58));
        assertEquals(0, bis.readBits(6));
    }

    @Test
    public void testReadBits_count62_BigEndian_returnsCorrectBits() throws IOException {
        InputStream in = createInputStream(0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF);
        BitInputStream bis = new BitInputStream(in, ByteOrder.BIG_ENDIAN);
        assertEquals(0x3FFFFFFFFFFFFFFFL, bis.readBits(62));
        assertEquals(3, bis.readBits(2));
        assertEquals(-1, bis.readBits(1));
    }

    @Test
    public void testReadBits_insufficientData_returnsMinusOne() throws IOException {
        InputStream in = createInputStream(0x01, 0x02);
        BitInputStream bis = new BitInputStream(in, ByteOrder.BIG_ENDIAN);
        assertEquals(0x0102, bis.readBits(16));
        assertEquals(-1, bis.readBits(1));
    }

    @Test
    public void testReadBits_partialStream_returnsMinusOne() throws IOException {
        InputStream in = createInputStream(0xFF);
        BitInputStream bis = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);
        assertEquals(-1, bis.readBits(9));
    }

    @Test
    public void testClearBitCache_afterReading_resetsCache() throws IOException {
        InputStream in = createInputStream(0xAB);
        BitInputStream bis = new BitInputStream(in, ByteOrder.BIG_ENDIAN);
        bis.readBits(4);
        bis.clearBitCache();
        assertEquals(-1, bis.readBits(8));
    }

    @Test
    public void testClearBitCache_afterReadingPartial_resetsCacheAndStreamPosition() throws IOException {
        InputStream in = createInputStream(0xAA, 0xBB);
        BitInputStream bis = new BitInputStream(in, ByteOrder.BIG_ENDIAN);
        bis.readBits(4);
        bis.clearBitCache();
        assertEquals(0xBB, bis.readBits(8));
    }

    @Test
    public void testReadBits_sequentialBigEndian_afterLargeRead_returnsCorrectBits() throws IOException {
        InputStream in = createInputStream(0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x00, 0x00);
        BitInputStream bis = new BitInputStream(in, ByteOrder.BIG_ENDIAN);
        assertEquals(0x7FFFFFFFFFFFFFFFL, bis.readBits(63));
        assertEquals(256, bis.readBits(9));
        assertEquals(-1, bis.readBits(1));
    }

    @Test
    public void testReadBits_sequentialLittleEndian_afterLargeRead_returnsCorrectBits() throws IOException {
        InputStream in = createInputStream(0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x00, 0x00);
        BitInputStream bis = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);
        assertEquals(0x7FFFFFFFFFFFFFFFL, bis.readBits(63));
        assertEquals(1, bis.readBits(9));
        assertEquals(-1, bis.readBits(1));
    }

    @Test
    public void testClose_noException() throws IOException {
        BitInputStream bis = new BitInputStream(new ByteArrayInputStream(new byte[0]), ByteOrder.BIG_ENDIAN);
        bis.close();
    }

    @Test(expected = NullPointerException.class)
    public void testReadBits_nullInputStream_throwsNullPointerException() throws IOException {
        BitInputStream bis = new BitInputStream(null, ByteOrder.BIG_ENDIAN);
        bis.readBits(1);
    }
}