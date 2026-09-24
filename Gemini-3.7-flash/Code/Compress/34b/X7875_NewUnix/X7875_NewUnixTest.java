package org.apache.commons.compress.archivers.zip;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.zip.ZipException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class X7875_NewUnixTest {

    private X7875_NewUnix xf;

    @Before
    public void setUp() {
        xf = new X7875_NewUnix();
    }

    // Tests default constructor initializes UID and GID to 1000
    @Test
    public void testConstructor_defaultValues_initializedTo1000() {
        assertEquals(1000L, xf.getUID());
        assertEquals(1000L, xf.getGID());
        assertEquals(new ZipShort(0x7875), xf.getHeaderId());
    }

    // Tests getter and setter for standard UID and GID
    @Test
    public void testSetAndGet_standardValues_returnsCorrectValues() {
        xf.setUID(500L);
        xf.setGID(600L);
        assertEquals(500L, xf.getUID());
        assertEquals(600L, xf.getGID());
    }

    // Tests setter and getter for zero UID and GID (boundary condition for root)
    @Test
    public void testSetAndGet_zeroValues_returnsZero() {
        xf.setUID(0L);
        xf.setGID(0L);
        assertEquals(0L, xf.getUID());
        assertEquals(0L, xf.getGID());
    }

    // Tests setter and getter for 32-bit unsigned boundary values
    @Test
    public void testSetAndGet_unsigned32BitMax_returnsCorrectValue() {
        long max32Bit = 0xFFFFFFFFL;
        xf.setUID(max32Bit);
        xf.setGID(max32Bit);
        assertEquals(max32Bit, xf.getUID());
        assertEquals(max32Bit, xf.getGID());
    }

    // Tests getLocalFileDataLength and getLocalFileDataData format for default values (1000 = 0x03E8)
    @Test
    public void testGetLocalFileData_default1000_encodesCorrectly() {
        byte[] data = xf.getLocalFileDataData();
        // version (1) + uidSize (2) + uid (0xE8, 0x03) + gidSize (2) + gid (0xE8, 0x03)
        assertEquals(7, data.length);
        assertEquals(new ZipShort(7), xf.getLocalFileDataLength());

        byte[] expected = new byte[] {
            1,          // version
            2,          // UID size
            (byte) 0xE8, 0x03, // 1000 in little-endian
            2,          // GID size
            (byte) 0xE8, 0x03  // 1000 in little-endian
        };
        assertArrayEquals(expected, data);
    }

    // Tests getLocalFileDataData with 0 UID and GID
    @Test
    public void testGetLocalFileData_zeroUidGid_encodesSingleByte() {
        xf.setUID(0L);
        xf.setGID(0L);
        byte[] data = xf.getLocalFileDataData();
        assertEquals(5, data.length);
        assertEquals(new ZipShort(5), xf.getLocalFileDataLength());

        byte[] expected = new byte[] {
            1, // version
            1, // UID size
            0, // UID
            1, // GID size
            0  // GID
        };
        assertArrayEquals(expected, data);
    }

    // Tests getCentralDirectoryData returns empty array
    @Test
    public void testGetCentralDirectoryData_returnsEmptyArray() {
        byte[] data = xf.getCentralDirectoryData();
        assertNotNull(data);
        assertEquals(0, data.length);
    }

    // Tests getCentralDirectoryLength returns expected ZipShort
    @Test
    public void testGetCentralDirectoryLength_returnsLength() {
        ZipShort len = xf.getCentralDirectoryLength();
        assertNotNull(len);
        assertEquals(xf.getLocalFileDataLength().getValue(), len.getValue());
    }

    // Tests parseFromLocalFileData with valid data
    @Test
    public void testParseFromLocalFileData_validBuffer_parsesUidAndGid() throws ZipException {
        byte[] data = new byte[] {
            1,          // version
            2,          // UID size
            (byte) 0x34, 0x12, // UID = 0x1234 = 4660
            3,          // GID size
            (byte) 0x78, 0x56, 0x34 // GID = 0x345678 = 3429496
        };

        xf.parseFromLocalFileData(data, 0, data.length);
        assertEquals(4660L, xf.getUID());
        assertEquals(3429496L, xf.getGID());
    }

    // Tests parseFromLocalFileData with offset in buffer
    @Test
    public void testParseFromLocalFileData_withOffset_parsesCorrectly() throws ZipException {
        byte[] data = new byte[] {
            0, 0,       // prefix padding
            1,          // version
            1,          // UID size
            42,         // UID = 42
            1,          // GID size
            99          // GID = 99
        };

        xf.parseFromLocalFileData(data, 2, 5);
        assertEquals(42L, xf.getUID());
        assertEquals(99L, xf.getGID());
    }

    // Tests round-trip parse and encode
    @Test
    public void testRoundTrip_encodeAndParse_restoresState() throws ZipException {
        xf.setUID(12345678L);
        xf.setGID(87654321L);

        byte[] data = xf.getLocalFileDataData();
        X7875_NewUnix parsed = new X7875_NewUnix();
        parsed.parseFromLocalFileData(data, 0, data.length);

        assertEquals(xf.getUID(), parsed.getUID());
        assertEquals(xf.getGID(), parsed.getGID());
        assertEquals(xf, parsed);
    }

    // Tests parseFromCentralDirectoryData does not throw exception
    @Test
    public void testParseFromCentralDirectoryData_doesNothing() throws ZipException {
        xf.parseFromCentralDirectoryData(new byte[0], 0, 0);
        assertEquals(1000L, xf.getUID());
        assertEquals(1000L, xf.getGID());
    }

    // Tests trimLeadingZeroesForceMinLength with null input
    @Test
    public void testTrimLeadingZeroesForceMinLength_nullInput_returnsNull() {
        assertNull(X7875_NewUnix.trimLeadingZeroesForceMinLength(null));
    }

    // Tests trimLeadingZeroesForceMinLength with all zeros array enforcing min length 1
    @Test
    public void testTrimLeadingZeroesForceMinLength_allZeroArray_enforcesMinLength() {
        byte[] input = new byte[] { 0, 0, 0, 0 };
        byte[] result = X7875_NewUnix.trimLeadingZeroesForceMinLength(input);
        assertArrayEquals(new byte[] { 0 }, result);
    }

    // Tests trimLeadingZeroesForceMinLength with leading zeros trimmed
    @Test
    public void testTrimLeadingZeroesForceMinLength_leadingZeroes_trimsCorrectly() {
        byte[] input = new byte[] { 0, 0, 1, 2, 3 };
        byte[] result = X7875_NewUnix.trimLeadingZeroesForceMinLength(input);
        assertArrayEquals(new byte[] { 1, 2, 3 }, result);
    }

    // Tests trimLeadingZeroesForceMinLength with no leading zeros
    @Test
    public void testTrimLeadingZeroesForceMinLength_noLeadingZeroes_returnsSameContent() {
        byte[] input = new byte[] { 5, 6, 7 };
        byte[] result = X7875_NewUnix.trimLeadingZeroesForceMinLength(input);
        assertArrayEquals(new byte[] { 5, 6, 7 }, result);
    }

    // Tests equals and hashCode contracts
    @Test
    public void testEqualsAndHashCode_sameValues_equalAndSameHashCode() {
        X7875_NewUnix xf2 = new X7875_NewUnix();
        assertTrue(xf.equals(xf));
        assertTrue(xf.equals(xf2));
        assertEquals(xf.hashCode(), xf2.hashCode());

        xf2.setUID(2000L);
        assertFalse(xf.equals(xf2));
        assertFalse(xf.equals(null));
        assertFalse(xf.equals("other type"));

        xf.setUID(2000L);
        assertTrue(xf.equals(xf2));
        xf2.setGID(3000L);
        assertFalse(xf.equals(xf2));
    }

    // Tests clone method creates an independent equal object
    @Test
    public void testClone_clonedInstance_equalsOriginal() throws CloneNotSupportedException {
        xf.setUID(4321L);
        xf.setGID(8765L);
        X7875_NewUnix cloned = (X7875_NewUnix) xf.clone();
        assertNotNull(cloned);
        assertEquals(xf, cloned);
        assertEquals(xf.getUID(), cloned.getUID());
        assertEquals(xf.getGID(), cloned.getGID());
    }

    // Tests toString contains class information and field values
    @Test
    public void testToString_containsUidAndGid() {
        xf.setUID(123L);
        xf.setGID(456L);
        String str = xf.toString();
        assertTrue(str.contains("0x7875"));
        assertTrue(str.contains("UID=123"));
        assertTrue(str.contains("GID=456"));
    }
}