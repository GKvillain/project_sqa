package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for the UnixStat interface constants.
 * Verifies the exact numeric values as defined in the production interface.
 */
public class UnixStatTest {

    // Test the permission mask constant (07777 octal)
    @Test
    public void testPERM_MASK_expectedValue() {
        assertEquals(4095, UnixStat.PERM_MASK);
    }

    // Test the symbolic link flag constant (0120000 octal)
    @Test
    public void testLINK_FLAG_expectedValue() {
        assertEquals(40960, UnixStat.LINK_FLAG);
    }

    // Test the regular file flag constant (0100000 octal)
    @Test
    public void testFILE_FLAG_expectedValue() {
        assertEquals(32768, UnixStat.FILE_FLAG);
    }

    // Test the directory flag constant (040000 octal)
    @Test
    public void testDIR_FLAG_expectedValue() {
        assertEquals(16384, UnixStat.DIR_FLAG);
    }

    // Test the default symbolic link permission constant (0777 octal)
    @Test
    public void testDEFAULT_LINK_PERM_expectedValue() {
        assertEquals(511, UnixStat.DEFAULT_LINK_PERM);
    }

    // Test the default directory permission constant (0755 octal)
    @Test
    public void testDEFAULT_DIR_PERM_expectedValue() {
        assertEquals(493, UnixStat.DEFAULT_DIR_PERM);
    }

    // Test the default regular file permission constant (0644 octal)
    @Test
    public void testDEFAULT_FILE_PERM_expectedValue() {
        assertEquals(420, UnixStat.DEFAULT_FILE_PERM);
    }

    // ========== Additional test cases for uncovered parts ==========

    // Tests for ZipShort utility class
    @Test
    public void testZipShortGetValue() {
        byte[] bytes = {0x01, 0x02};
        assertEquals(0x0201, ZipShort.getValue(bytes));
    }

    @Test
    public void testZipShortPutValue() {
        byte[] buf = new byte[2];
        ZipShort.putValue(0x0201, buf, 0);
        assertArrayEquals(new byte[]{0x01, 0x02}, buf);
    }

    // Tests for ZipLong utility class
    @Test
    public void testZipLongGetValue() {
        byte[] bytes = {0x01, 0x02, 0x03, 0x04};
        assertEquals(0x04030201L, ZipLong.getValue(bytes));
    }

    @Test
    public void testZipLongPutValue() {
        byte[] buf = new byte[4];
        ZipLong.putValue(0x04030201L, buf, 0);
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04}, buf);
    }

    // Tests for GeneralPurposeBit class
    @Test
    public void testGeneralPurposeBitDefaults() {
        GeneralPurposeBit gpb = new GeneralPurposeBit();
        assertFalse(gpb.isUTF8());
        assertFalse(gpb.isEncryption());
        assertFalse(gpb.isDataDescriptor());
        assertFalse(gpb.isStrongEncryption());
    }

    @Test
    public void testGeneralPurposeBitSetAndGet() {
        GeneralPurposeBit gpb = new GeneralPurposeBit();
        gpb.setUTF8(true);
        assertTrue(gpb.isUTF8());
        gpb.setDataDescriptor(true);
        assertTrue(gpb.isDataDescriptor());
        gpb.setEncryption(true);
        assertTrue(gpb.isEncryption());
    }

    @Test
    public void testGeneralPurposeBitEncodeDecode() {
        GeneralPurposeBit gpb = new GeneralPurposeBit();
        gpb.setUTF8(true);
        gpb.setDataDescriptor(true);
        byte[] encoded = gpb.encode();
        assertNotNull(encoded);
        assertEquals(2, encoded.length);
        GeneralPurposeBit decoded = GeneralPurposeBit.parse(encoded, 0);
        assertTrue(decoded.isUTF8());
        assertTrue(decoded.isDataDescriptor());
        assertFalse(decoded.isEncryption());
    }
}