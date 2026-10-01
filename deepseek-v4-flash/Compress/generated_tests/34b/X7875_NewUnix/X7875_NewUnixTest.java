package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.zip.ZipException;

public class X7875_NewUnixTest {

    // --- Helper: create byte array from hex string
    private static byte[] hexBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    // Test default constructor values
    @Test
    public void testDefaultConstructor_values() {
        X7875_NewUnix xf = new X7875_NewUnix();
        assertEquals(1000L, xf.getUID());
        assertEquals(1000L, xf.getGID());
        // version is package-private but accessible? Not directly; we can infer from getLocalFileDataData
        // The version is stored as first byte, so we check indirectly via parsing
        byte[] data = xf.getLocalFileDataData();
        assertEquals(1, data[0] & 0xFF); // version = 1
    }

    // Test header ID
    @Test
    public void testGetHeaderId_returns7875() {
        X7875_NewUnix xf = new X7875_NewUnix();
        ZipShort header = xf.getHeaderId();
        assertEquals(0x7875, header.getValue());
    }

    // Normal set/get UID and GID
    @Test
    public void testSetGetUID_GID_normalValues() {
        X7875_NewUnix xf = new X7875_NewUnix();
        xf.setUID(42L);
        xf.setGID(12345L);
        assertEquals(42L, xf.getUID());
        assertEquals(12345L, xf.getGID());
    }

    // UID/GID zero
    @Test
    public void testSetGetUID_GID_zeroValues() {
        X7875_NewUnix xf = new X7875_NewUnix();
        xf.setUID(0L);
        xf.setGID(0L);
        assertEquals(0L, xf.getUID());
        assertEquals(0L, xf.getGID());
    }

    // UID/GID maximum unsigned 32-bit (2^32 - 1)
    @Test
    public void testSetGetUID_GID_maxUnsigned32() {
        X7875_NewUnix xf = new X7875_NewUnix();
        long max32 = 0xFFFFFFFFL; // 4294967295
        xf.setUID(max32);
        xf.setGID(max32);
        assertEquals(max32, xf.getUID());
        assertEquals(max32, xf.getGID());
    }

    // UID/GID with value that has leading zero in big-endian (e.g., 256) – potential bug
    @Test
    public void testSetGetUID_GID_value256_roundTrip() {
        X7875_NewUnix xf = new X7875_NewUnix();
        long value = 256L;
        xf.setUID(value);
        xf.setGID(value);
        byte[] data = xf.getLocalFileDataData();
        // parse back
        X7875_NewUnix xf2 = new X7875_NewUnix();
        try {
            xf2.parseFromLocalFileData(data, 0, data.length);
        } catch (ZipException e) {
            fail("Unexpected ZipException: " + e.getMessage());
        }
        assertEquals("UID should round-trip", value, xf2.getUID());
        assertEquals("GID should round-trip", value, xf2.getGID());
    }

    // Another value with more bytes: 65536 (0x10000)
    @Test
    public void testSetGetUID_GID_value65536_roundTrip() {
        X7875_NewUnix xf = new X7875_NewUnix();
        long value = 65536L;
        xf.setUID(value);
        xf.setGID(value);
        byte[] data = xf.getLocalFileDataData();
        X7875_NewUnix xf2 = new X7875_NewUnix();
        try {
            xf2.parseFromLocalFileData(data, 0, data.length);
        } catch (ZipException e) {
            fail("Unexpected ZipException: " + e.getMessage());
        }
        assertEquals(value, xf2.getUID());
        assertEquals(value, xf2.getGID());
    }

    // Test getLocalFileDataLength after setting known values
    @Test
    public void testGetLocalFileDataLength_knownValues() {
        X7875_NewUnix xf = new X7875_NewUnix();
        // Default: uid=1000, gid=1000 -> each takes 2 bytes (0x03E8) after reverse & trim
        // Version=1, UIDSize=2, UID=2 bytes, GIDSize=2, GID=2 bytes => total = 3+2+2 = 7
        assertEquals(7, xf.getLocalFileDataLength().getValue());

        // Set uid to 0 and gid to 0 => each takes 1 byte (trimmed to {0}) => total = 3+1+1 = 5
        xf.setUID(0);
        xf.setGID(0);
        assertEquals(5, xf.getLocalFileDataLength().getValue());

        // Set to large values (255) => 1 byte each => total = 3+1+1 = 5
        xf.setUID(255);
        xf.setGID(255);
        assertEquals(5, xf.getLocalFileDataLength().getValue());
    }

    // Test getCentralDirectoryLength delegates to local
    @Test
    public void testGetCentralDirectoryLength_equalsLocal() {
        X7875_NewUnix xf = new X7875_NewUnix();
        xf.setUID(123456L);
        xf.setGID(789012L);
        assertEquals(xf.getLocalFileDataLength().getValue(), xf.getCentralDirectoryLength().getValue());
    }

    // Test getCentralDirectoryData returns empty array
    @Test
    public void testGetCentralDirectoryData_empty() {
        X7875_NewUnix xf = new X7875_NewUnix();
        assertArrayEquals(new byte[0], xf.getCentralDirectoryData());
    }

    // Test parseFromLocalFileData with valid data
    @Test
    public void testParseFromLocalFileData_normal() throws ZipException {
        // Build data for uid=1000, gid=1000
        // version=1, uidSize=2, uid={0xE8,0x03} (little-endian for 1000), gidSize=2, gid={0xE8,0x03}
        byte[] data = new byte[] {1, 2, (byte)0xE8, 0x03, 2, (byte)0xE8, 0x03};
        X7875_NewUnix xf = new X7875_NewUnix();
        xf.parseFromLocalFileData(data, 0, data.length);
        assertEquals(1000L, xf.getUID());
        assertEquals(1000L, xf.getGID());
    }

    // Test parseFromLocalFileData with uidSize=0 should throw NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testParseFromLocalFileData_zeroUidSize() throws ZipException {
        // version=1, uidSize=0, then gidSize=2, gid=2 bytes (just to fill)
        byte[] data = new byte[] {1, 0, 2, (byte)0xE8, 0x03}; // total 5 bytes
        X7875_NewUnix xf = new X7875_NewUnix();
        xf.parseFromLocalFileData(data, 0, data.length);
    }

    // Test parseFromLocalFileData with gidSize=0 should throw NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testParseFromLocalFileData_zeroGidSize() throws ZipException {
        // version=1, uidSize=2, uid=2 bytes, gidSize=0
        byte[] data = new byte[] {1, 2, (byte)0xE8, 0x03, 0}; // total 5 bytes
        X7875_NewUnix xf = new X7875_NewUnix();
        xf.parseFromLocalFileData(data, 0, data.length);
    }

    // Test parseFromLocalFileData with data too short causes ArrayIndexOutOfBoundsException
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testParseFromLocalFileData_shortData() throws ZipException {
        // version=1, uidSize=2, but only 1 byte of uid provided
        byte[] data = new byte[] {1, 2, (byte)0xE8}; // missing second uid byte
        X7875_NewUnix xf = new X7875_NewUnix();
        xf.parseFromLocalFileData(data, 0, data.length);
    }

    // Test trimLeadingZeroesForceMinLength with null
    @Test
    public void testTrimLeadingZeroesForceMinLength_null_returnsNull() {
        assertNull(X7875_NewUnix.trimLeadingZeroesForceMinLength(null));
    }

    // Test trimLeadingZeroesForceMinLength with all zero bytes
    @Test
    public void testTrimLeadingZeroesForceMinLength_allZeroes() {
        byte[] input = {0, 0, 0};
        byte[] result = X7875_NewUnix.trimLeadingZeroesForceMinLength(input);
        byte[] expected = {0}; // MIN_LENGTH=1, after trimming 3 zeros, length=0 -> forced to 1, copy 0 bytes, result[0]=0
        assertArrayEquals(expected, result);
    }

    // Test trimLeadingZeroesForceMinLength with leading zeroes and trailing data
    @Test
    public void testTrimLeadingZeroesForceMinLength_leadingZeroes() {
        byte[] input = {0, 0, 5, 6};
        byte[] result = X7875_NewUnix.trimLeadingZeroesForceMinLength(input);
        byte[] expected = {5, 6};
        assertArrayEquals(expected, result);
    }

    // Test trimLeadingZeroesForceMinLength with no leading zeroes
    @Test
    public void testTrimLeadingZeroesForceMinLength_noLeadingZeroes() {
        byte[] input = {1, 2, 3};
        byte[] result = X7875_NewUnix.trimLeadingZeroesForceMinLength(input);
        assertArrayEquals(input, result);
    }

    // Test trimLeadingZeroesForceMinLength with single zero element (MIN_LENGTH=1)
    @Test
    public void testTrimLeadingZeroesForceMinLength_singleZero_keepsOne() {
        byte[] input = {0};
        byte[] result = X7875_NewUnix.trimLeadingZeroesForceMinLength(input);
        byte[] expected = {0};
        assertArrayEquals(expected, result);
    }

    // Test toString
    @Test
    public void testToString_notNull() {
        X7875_NewUnix xf = new X7875_NewUnix();
        assertNotNull(xf.toString());
        assertTrue(xf.toString().contains("UID="));
        assertTrue(xf.toString().contains("GID="));
    }

    // Test equals: same object, symmetric, null, different type
    @Test
    public void testEquals_symmetric() {
        X7875_NewUnix xf1 = new X7875_NewUnix();
        X7875_NewUnix xf2 = new X7875_NewUnix();
        assertEquals(xf1, xf2);
        assertEquals(xf2, xf1);
    }

    @Test
    public void testEquals_differentUID() {
        X7875_NewUnix xf1 = new X7875_NewUnix();
        X7875_NewUnix xf2 = new X7875_NewUnix();
        xf2.setUID(999L);
        assertNotEquals(xf1, xf2);
    }

    @Test
    public void testEquals_nullObject() {
        X7875_NewUnix xf = new X7875_NewUnix();
        assertFalse(xf.equals(null));
    }

    @Test
    public void testEquals_differentClass() {
        X7875_NewUnix xf = new X7875_NewUnix();
        assertFalse(xf.equals("string"));
    }

    // Test hashCode consistency
    @Test
    public void testHashCode_consistent() {
        X7875_NewUnix xf = new X7875_NewUnix();
        int hc1 = xf.hashCode();
        int hc2 = xf.hashCode();
        assertEquals(hc1, hc2);
    }

    // Test clone
    @Test
    public void testClone_equalsOriginal() throws CloneNotSupportedException {
        X7875_NewUnix xf = new X7875_NewUnix();
        xf.setUID(777L);
        xf.setGID(888L);
        X7875_NewUnix clone = (X7875_NewUnix) xf.clone();
        assertNotSame(xf, clone);
        assertEquals(xf, clone);
        assertEquals(xf.hashCode(), clone.hashCode());
    }

    // Test reset via clone? Not directly; but we can test that default constructor gives 1000.
}