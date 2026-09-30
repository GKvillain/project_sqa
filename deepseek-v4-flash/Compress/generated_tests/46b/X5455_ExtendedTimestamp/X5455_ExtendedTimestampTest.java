package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

public class X5455_ExtendedTimestampTest {

    // Tests default state after construction
    @Test
    public void testConstructor_defaultState_returnsEmptyExtraField() {
        X5455_ExtendedTimestamp x = new X5455_ExtendedTimestamp();

        assertEquals(0, x.getFlags());
        assertFalse(x.isBit0_modifyTimePresent());
        assertFalse(x.isBit1_accessTimePresent());
        assertFalse(x.isBit2_createTimePresent());
        assertNull(x.getModifyTime());
        assertNull(x.getAccessTime());
        assertNull(x.getCreateTime());
        assertNull(x.getModifyJavaTime());
        assertNull(x.getAccessJavaTime());
        assertNull(x.getCreateJavaTime());
    }

    // Tests header id
    @Test
    public void testGetHeaderId_default_returns5455() {
        X5455_ExtendedTimestamp x = new X5455_ExtendedTimestamp();

        assertEquals(0x5455, x.getHeaderId().getValue());
    }

    // Tests default extra field lengths
    @Test
    public void testGetLengths_default_returnsOne() {
        X5455_ExtendedTimestamp x = new X5455_ExtendedTimestamp();

        assertEquals(1, x.getLocalFileDataLength().getValue());
        assertEquals(1, x.getCentralDirectoryLength().getValue());
    }

    // Tests setting modify time and writing local/central data
    @Test
    public void testSetModifyTime_setsFlagAndWritesModifyTime() {
        X5455_ExtendedTimestamp x = new X5455_ExtendedTimestamp();
        x.setModifyTime(new ZipLong(1000L));

        assertTrue(x.isBit0_modifyTimePresent());
        assertEquals(1000, x.getModifyTime().getIntValue());
        assertEquals(5, x.getLocalFileDataLength().getValue());
        assertEquals(5, x.getCentralDirectoryLength().getValue());

        byte[] local = x.getLocalFileDataData();
        assertEquals(5, local.length);
        assertEquals(1, local[0]);
        assertEquals(1000, new ZipLong(local, 1).getIntValue());

        byte[] central = x.getCentralDirectoryData();
        assertEquals(5, central.length);
        assertEquals(1, central[0]);
        assertEquals(1000, new ZipLong(central, 1).getIntValue());
    }

    // Tests all three timestamps in local data and only modify time in central data
    @Test
    public void testSetAllTimes_localDataContainsAllAndCentralContainsModifyOnly() {
        X5455_ExtendedTimestamp x = new X5455_ExtendedTimestamp();
        x.setModifyTime(new ZipLong(1L));
        x.setAccessTime(new ZipLong(2L));
        x.setCreateTime(new ZipLong(3L));

        assertTrue(x.isBit1_accessTimePresent());
        assertTrue(x.isBit2_createTimePresent());
        assertEquals(13, x.getLocalFileDataLength().getValue());
        assertEquals(5, x.getCentralDirectoryLength().getValue());

        byte[] local = x.getLocalFileDataData();
        assertEquals(13, local.length);
        assertEquals(7, local[0]);
        assertEquals(1, new ZipLong(local, 1).getIntValue());
        assertEquals(2, new ZipLong(local, 5).getIntValue());
        assertEquals(3, new ZipLong(local, 9).getIntValue());

        byte[] central = x.getCentralDirectoryData();
        assertEquals(5, central.length);
        assertEquals(1, new ZipLong(central, 1).getIntValue());
    }

    // Tests parsing all three fields from local file data
    @Test
    public void testParseFromLocalFileData_allFields() throws Exception {
        byte[] data = new byte[13];
        data[0] = 7;
        System.arraycopy(new ZipLong(1L).getBytes(), 0, data, 1, 4);
        System.arraycopy(new ZipLong(2L).getBytes(), 0, data, 5, 4);
        System.arraycopy(new ZipLong(3L).getBytes(), 0, data, 9, 4);

        X5455_ExtendedTimestamp x = new X5455_ExtendedTimestamp();
        x.parseFromLocalFileData(data, 0, data.length);

        assertEquals(7, x.getFlags());
        assertEquals(1, x.getModifyTime().getIntValue());
        assertEquals(2, x.getAccessTime().getIntValue());
        assertEquals(3, x.getCreateTime().getIntValue());
    }

    // Tests parsing flags-only local data
    @Test
    public void testParseFromLocalFileData_flagsOnly_noTimes() throws Exception {
        X5455_ExtendedTimestamp x = new X5455_ExtendedTimestamp();
        x.parseFromLocalFileData(new byte[] {0}, 0, 1);

        assertEquals(0, x.getFlags());
        assertNull(x.getModifyTime());
        assertNull(x.getAccessTime());
        assertNull(x.getCreateTime());
    }

    // Tests central directory parse with access/create flags but no access/create data
    @Test
    public void testParseFromCentralDirectoryData_missingAccessCreateTimesLeavesNull() throws Exception {
        byte[] data = new byte[5];
        data[0] = 7;
        System.arraycopy(new ZipLong(42L).getBytes(), 0, data, 1, 4);

        X5455_ExtendedTimestamp x = new X5455_ExtendedTimestamp();
        x.parseFromCentralDirectoryData(data, 0, data.length);

        assertEquals(7, x.getFlags());
        assertEquals(42, x.getModifyTime().getIntValue());
        assertNull(x.getAccessTime());
        assertNull(x.getCreateTime());
        assertEquals(5, x.getLocalFileDataLength().getValue());

        byte[] rewritten = x.getLocalFileDataData();
        assertEquals(1, rewritten[0]);
    }

    // Tests Java Date setter truncates milliseconds
    @Test
    public void testSetModifyJavaTime_roundsMillisDownAndReturnsSameTime() {
        X5455_ExtendedTimestamp x = new X5455_ExtendedTimestamp();
        x.setModifyJavaTime(new Date(1234567890123L));

        assertEquals(1234567890L * 1000L, x.getModifyJavaTime().getTime());
        assertTrue(x.isBit0_modifyTimePresent());
    }

    // Tests access/create Java Date setters
    @Test
    public void testSetAccessAndCreateJavaTime_storesSeconds() {
        X5455_ExtendedTimestamp x = new X5455_ExtendedTimestamp();
        x.setAccessJavaTime(new Date(2000L));
        x.setCreateJavaTime(new Date(3000L));

        assertEquals(2, x.getAccessTime().getIntValue());
        assertEquals(3, x.getCreateTime().getIntValue());
        assertEquals(2000L, x.getAccessJavaTime().getTime());
        assertEquals(3000L, x.getCreateJavaTime().getTime());
        assertTrue(x.isBit1_accessTimePresent());
        assertTrue(x.isBit2_createTimePresent());
    }

    // Tests negative seconds before Unix epoch
    @Test
    public void testSetModifyJavaTime_beforeEpoch_usesNegativeSeconds() {
        X5455_ExtendedTimestamp x = new X5455_ExtendedTimestamp();
        x.setModifyJavaTime(new Date(-1000L));

        assertEquals(-1, x.getModifyTime().getIntValue());
        assertEquals(-1000L, x.getModifyJavaTime().getTime());
    }

    // Tests maximum signed 32-bit boundary
    @Test
    public void testSetModifyJavaTime_atSignedMax_succeeds() {
        X5455_ExtendedTimestamp x = new X5455_ExtendedTimestamp();
        long seconds = 2147483647L;
        x.setModifyJavaTime(new Date(seconds * 1000L));

        assertEquals(seconds * 1000L, x.getModifyJavaTime().getTime());
        assertEquals(2147483647, x.getModifyTime().getIntValue());
    }

    // Tests overflow beyond signed 32-bit range
    @Test(expected = IllegalArgumentException.class)
    public void testSetModifyJavaTime_overSignedMax_throws() {
        X5455_ExtendedTimestamp x = new X5455_ExtendedTimestamp();
        x.setModifyJavaTime(new Date(2147483648L * 1000L));
    }

    // Tests null setters clear flags and stored times
    @Test
    public void testSetTime_nullClearsFlagAndTime() {
        X5455_ExtendedTimestamp x = new X5455_ExtendedTimestamp();
        x.setModifyTime(new ZipLong(1L));
        x.setAccessTime(new ZipLong(2L));
        x.setCreateTime(new ZipLong(3L));

        x.setModifyTime(null);
        x.setAccessTime(null);
        x.setCreateTime(null);

        assertEquals(0, x.getFlags());
        assertNull(x.getModifyTime());
        assertNull(x.getAccessTime());
        assertNull(x.getCreateTime());
        assertNull(x.getModifyJavaTime());
        assertNull(x.getAccessJavaTime());
        assertNull(x.getCreateJavaTime());
    }

    // Tests flag set without corresponding time does not write that time
    @Test
    public void testGetLocalFileDataData_flagSetWithoutTimeDoesNotWriteTime() {
        X5455_ExtendedTimestamp x = new X5455_ExtendedTimestamp();
        x.setFlags(ACCESS_TIME_BIT);

        assertEquals(1, x.getLocalFileDataLength().getValue());
        assertEquals(1, x.getLocalFileDataData().length);
        assertEquals(0, x.getLocalFileDataData()[0]);
        assertEquals(2, x.getFlags());
    }

    // Tests flags byte handling and ignored bits mask
    @Test
    public void testSetFlags_onlyFirstThreeBitsControlPresence() {
        X5455_ExtendedTimestamp x = new X5455_ExtendedTimestamp();
        x.setFlags((byte) (MODIFY_TIME_BIT | ACCESS_TIME_BIT | CREATE_TIME_BIT | 0x08));

        assertTrue(x.isBit0_modifyTimePresent());
        assertTrue(x.isBit1_accessTimePresent());
        assertTrue(x.isBit2_createTimePresent());
        assertEquals(0x0F, x.getFlags());
        assertEquals(7, x.getFlags() & 0x07);
    }

    // Tests equals and hashCode for equal objects
    @Test
    public void testEquals_sameFields_returnsTrueAndSameHash() {
        X5455_ExtendedTimestamp a = new X5455_ExtendedTimestamp();
        a.setModifyTime(new ZipLong(123L));
        a.setAccessTime(new ZipLong(456L));
        a.setCreateTime(new ZipLong(789L));

        X5455_ExtendedTimestamp b = new X5455_ExtendedTimestamp();
        b.setModifyTime(new ZipLong(123L));
        b.setAccessTime(new ZipLong(456L));
        b.setCreateTime(new ZipLong(789L));

        assertTrue(a.equals(a));
        assertTrue(a.equals(b));
        assertFalse(a.equals(null));
        assertEquals(a.hashCode(), b.hashCode());

        b.setCreateTime(new ZipLong(790L));
        assertFalse(a.equals(b));
    }

    // Tests clone returns a distinct equal copy
    @Test
    public void testClone_returnsEqualCopy() throws Exception {
        X5455_ExtendedTimestamp x = new X5455_ExtendedTimestamp();
        x.setModifyTime(new ZipLong(111L));

        X5455_ExtendedTimestamp copy = (X5455_ExtendedTimestamp) x.clone();

        assertTrue(x != copy);
        assertEquals(x, copy);
        assertEquals(111, copy.getModifyTime().getIntValue());
    }

    // Tests toString contains modify time when present
    @Test
    public void testToString_containsTimeWhenPresent() {
        X5455_ExtendedTimestamp x = new X5455_ExtendedTimestamp();

        assertFalse(x.toString().contains("Modify:"));

        x.setModifyTime(new ZipLong(1000L));
        String s = x.toString();
        assertTrue(s.contains("0x5455"));
        assertTrue(s.contains("Modify:"));
    }
}