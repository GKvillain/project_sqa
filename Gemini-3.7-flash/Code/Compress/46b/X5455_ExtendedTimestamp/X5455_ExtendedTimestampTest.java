package org.apache.commons.compress.archivers.zip;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.zip.ZipException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class X5455_ExtendedTimestampTest {

    private X5455_ExtendedTimestamp xf;

    @Before
    public void setUp() {
        xf = new X5455_ExtendedTimestamp();
    }

    // Tests getHeaderId returns 0x5455
    @Test
    public void testGetHeaderId_default_returns0x5455() {
        assertEquals(new ZipShort(0x5455), xf.getHeaderId());
    }

    // Tests default length when no timestamps are set
    @Test
    public void testGetLocalFileDataLength_default_returnsOneByte() {
        assertEquals(new ZipShort(1), xf.getLocalFileDataLength());
        assertEquals(new ZipShort(1), xf.getCentralDirectoryLength());
    }

    // Tests setting modify, access, and create times using ZipLong and checking data length
    @Test
    public void testSetTimes_allThreeSet_returnsCorrectLengthsAndData() {
        final ZipLong modTime = new ZipLong(1000);
        final ZipLong accTime = new ZipLong(2000);
        final ZipLong creTime = new ZipLong(3000);

        xf.setModifyTime(modTime);
        xf.setAccessTime(accTime);
        xf.setCreateTime(creTime);

        assertTrue(xf.isBit0_modifyTimePresent());
        assertTrue(xf.isBit1_accessTimePresent());
        assertTrue(xf.isBit2_createTimePresent());
        assertEquals((byte) 7, xf.getFlags());

        assertEquals(new ZipShort(1 + 4 + 4 + 4), xf.getLocalFileDataLength());
        assertEquals(new ZipShort(1 + 4), xf.getCentralDirectoryLength());

        final byte[] localData = xf.getLocalFileDataData();
        assertEquals(13, localData.length);
        assertEquals(7, localData[0]);

        final byte[] centralData = xf.getCentralDirectoryData();
        assertEquals(5, centralData.length);
        assertEquals(7, centralData[0]);
    }

    // Tests setting times using java.util.Date
    @Test
    public void testSetJavaTimes_validDates_returnsTruncatedToSeconds() {
        final Date modDate = new Date(1400000000123L);
        final Date accDate = new Date(1500000000456L);
        final Date creDate = new Date(1600000000789L);

        xf.setModifyJavaTime(modDate);
        xf.setAccessJavaTime(accDate);
        xf.setCreateJavaTime(creDate);

        assertEquals(new Date(1400000000000L), xf.getModifyJavaTime());
        assertEquals(new Date(1500000000000L), xf.getAccessJavaTime());
        assertEquals(new Date(1600000000000L), xf.getCreateJavaTime());
    }

    // Tests setting null Java Date values resets flags and timestamps
    @Test
    public void testSetJavaTimes_nullInput_clearsFlagsAndValues() {
        xf.setModifyJavaTime(new Date(1000000000000L));
        assertTrue(xf.isBit0_modifyTimePresent());

        xf.setModifyJavaTime(null);
        xf.setAccessJavaTime(null);
        xf.setCreateJavaTime(null);

        assertFalse(xf.isBit0_modifyTimePresent());
        assertFalse(xf.isBit1_accessTimePresent());
        assertFalse(xf.isBit2_createTimePresent());
        assertNull(xf.getModifyTime());
        assertNull(xf.getAccessTime());
        assertNull(xf.getCreateTime());
        assertNull(xf.getModifyJavaTime());
        assertNull(xf.getAccessJavaTime());
        assertNull(xf.getCreateJavaTime());
    }

    // Tests parsing valid local file data with all three timestamps
    @Test
    public void testParseFromLocalFileData_allThreeTimes_populatesCorrectly() throws ZipException {
        final byte[] data = new byte[]{
                7,
                10, 0, 0, 0,
                20, 0, 0, 0,
                30, 0, 0, 0
        };

        xf.parseFromLocalFileData(data, 0, data.length);

        assertTrue(xf.isBit0_modifyTimePresent());
        assertTrue(xf.isBit1_accessTimePresent());
        assertTrue(xf.isBit2_createTimePresent());
        assertEquals(new ZipLong(10), xf.getModifyTime());
        assertEquals(new ZipLong(20), xf.getAccessTime());
        assertEquals(new ZipLong(30), xf.getCreateTime());
    }

    // Tests parsing central directory data where only modify time is present
    @Test
    public void testParseFromCentralDirectoryData_modifyOnly_populatesCorrectly() throws ZipException {
        final byte[] centralData = new byte[]{
                7, // flags indicate mod, acc, cre in local header
                10, 0, 0, 0
        };

        xf.parseFromCentralDirectoryData(centralData, 0, centralData.length);

        assertTrue(xf.isBit0_modifyTimePresent());
        assertTrue(xf.isBit1_accessTimePresent());
        assertTrue(xf.isBit2_createTimePresent());
        assertEquals(new ZipLong(10), xf.getModifyTime());
        assertNull(xf.getAccessTime());
        assertNull(xf.getCreateTime());
    }

    // Tests flags setter and individual bit getters
    @Test
    public void testSetFlags_individualBits_returnsExpectedBooleans() {
        xf.setFlags((byte) 0);
        assertFalse(xf.isBit0_modifyTimePresent());
        assertFalse(xf.isBit1_accessTimePresent());
        assertFalse(xf.isBit2_createTimePresent());

        xf.setFlags(X5455_ExtendedTimestamp.MODIFY_TIME_BIT);
        assertTrue(xf.isBit0_modifyTimePresent());
        assertFalse(xf.isBit1_accessTimePresent());
        assertFalse(xf.isBit2_createTimePresent());

        xf.setFlags(X5455_ExtendedTimestamp.ACCESS_TIME_BIT);
        assertFalse(xf.isBit0_modifyTimePresent());
        assertTrue(xf.isBit1_accessTimePresent());
        assertFalse(xf.isBit2_createTimePresent());

        xf.setFlags(X5455_ExtendedTimestamp.CREATE_TIME_BIT);
        assertFalse(xf.isBit0_modifyTimePresent());
        assertFalse(xf.isBit1_accessTimePresent());
        assertTrue(xf.isBit2_createTimePresent());
    }

    // Tests round trip serialization and parsing
    @Test
    public void testRoundTrip_localData_matchesOriginal() throws ZipException {
        xf.setModifyJavaTime(new Date(123456789000L));
        xf.setAccessJavaTime(new Date(987654321000L));

        final byte[] localData = xf.getLocalFileDataData();

        final X5455_ExtendedTimestamp parsed = new X5455_ExtendedTimestamp();
        parsed.parseFromLocalFileData(localData, 0, localData.length);

        assertEquals(xf, parsed);
        assertEquals(xf.getModifyJavaTime(), parsed.getModifyJavaTime());
        assertEquals(xf.getAccessJavaTime(), parsed.getAccessJavaTime());
        assertNull(parsed.getCreateJavaTime());
    }

    // Tests boundary date values for 32-bit signed integer limits
    @Test
    public void testSetModifyJavaTime_maxSigned32BitSeconds_succeeds() {
        final Date maxSignedDate = new Date(0x7FFFFFFFL * 1000L);
        xf.setModifyJavaTime(maxSignedDate);
        assertEquals(maxSignedDate, xf.getModifyJavaTime());
    }

    // Tests date exceeding signed 32-bit integer range throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetModifyJavaTime_dateExceeding32Bits_throwsException() {
        final Date tooLargeDate = new Date(0x100000000L * 1000L);
        xf.setModifyJavaTime(tooLargeDate);
    }

    // Tests date exceeding signed 32-bit maximum (year 2038 overflow) throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetModifyJavaTime_signed32BitOverflow_throwsException() {
        final Date overflowDate = new Date((0x7FFFFFFFL + 1L) * 1000L);
        xf.setModifyJavaTime(overflowDate);
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_sameValues_equalAndSameHash() {
        final X5455_ExtendedTimestamp first = new X5455_ExtendedTimestamp();
        final X5455_ExtendedTimestamp second = new X5455_ExtendedTimestamp();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());

        first.setModifyTime(new ZipLong(100));
        assertFalse(first.equals(second));

        second.setModifyTime(new ZipLong(100));
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());

        assertFalse(first.equals(null));
        assertFalse(first.equals("other type"));
    }

    // Tests clone creates independent copy
    @Test
    public void testClone_clonedInstance_equalsOriginalButNotSame() throws CloneNotSupportedException {
        xf.setModifyTime(new ZipLong(123));
        xf.setAccessTime(new ZipLong(456));

        final X5455_ExtendedTimestamp cloned = (X5455_ExtendedTimestamp) xf.clone();
        assertNotSame(xf, cloned);
        assertEquals(xf, cloned);
        assertEquals(xf.getModifyTime(), cloned.getModifyTime());
        assertEquals(xf.getAccessTime(), cloned.getAccessTime());
    }

    // Tests toString returns formatted debug string
    @Test
    public void testToString_withTimes_containsFieldInfo() {
        xf.setModifyTime(new ZipLong(123456));
        xf.setAccessTime(new ZipLong(234567));
        xf.setCreateTime(new ZipLong(345678));

        final String str = xf.toString();
        assertNotNull(str);
        assertTrue(str.contains("0x5455"));
        assertTrue(str.contains("Modify:"));
        assertTrue(str.contains("Access:"));
        assertTrue(str.contains("Create:"));
    }
}