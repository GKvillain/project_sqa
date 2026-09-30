package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.zip.ZipException;

public class Zip64ExtendedInformationExtraFieldTest {

    // Test constructor with null size and compressedSize
    @Test
    public void testConstructor_nullSizeAndCompressedSize_createsEmptyField() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(null, null);
        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    // Test constructor with valid size and compressedSize
    @Test
    public void testConstructor_validSizeAndCompressedSize_setsFields() {
        ZipEightByteInteger size = new ZipEightByteInteger(1000L);
        ZipEightByteInteger compressedSize = new ZipEightByteInteger(500L);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(size, compressedSize);
        assertEquals(size, field.getSize());
        assertEquals(compressedSize, field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    // Test constructor with all four parameters
    @Test
    public void testConstructor_allParameters_setsAllFields() {
        ZipEightByteInteger size = new ZipEightByteInteger(1000L);
        ZipEightByteInteger compressedSize = new ZipEightByteInteger(500L);
        ZipEightByteInteger offset = new ZipEightByteInteger(200L);
        ZipLong diskStart = new ZipLong(1L);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(size, compressedSize, offset, diskStart);
        assertEquals(size, field.getSize());
        assertEquals(compressedSize, field.getCompressedSize());
        assertEquals(offset, field.getRelativeHeaderOffset());
        assertEquals(diskStart, field.getDiskStartNumber());
    }

    // Test getLocalFileDataLength when size is null
    @Test
    public void testGetLocalFileDataLength_sizeNull_returnsZero() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        assertEquals(0, field.getLocalFileDataLength().getValue());
    }

    // Test getLocalFileDataLength when size is not null
    @Test
    public void testGetLocalFileDataLength_sizeNotNull_returnsTwoDword() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(
            new ZipEightByteInteger(1000L), new ZipEightByteInteger(500L));
        assertEquals(2 * ZipConstants.DWORD, field.getLocalFileDataLength().getValue());
    }

    // Test getCentralDirectoryLength with all fields null
    @Test
    public void testGetCentralDirectoryLength_allNull_returnsZero() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        assertEquals(0, field.getCentralDirectoryLength().getValue());
    }

    // Test getCentralDirectoryLength with all fields set
    @Test
    public void testGetCentralDirectoryLength_allSet_returnsCorrectLength() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(
            new ZipEightByteInteger(1000L), new ZipEightByteInteger(500L),
            new ZipEightByteInteger(200L), new ZipLong(1L));
        assertEquals(3 * ZipConstants.DWORD + ZipConstants.WORD, field.getCentralDirectoryLength().getValue());
    }

    // Test getLocalFileDataData returns empty array when both size and compressedSize are null
    @Test
    public void testGetLocalFileDataData_bothNull_returnsEmpty() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        byte[] data = field.getLocalFileDataData();
        assertEquals(0, data.length);
    }

    // Test getLocalFileDataData throws exception when one field is null
    @Test(expected = IllegalArgumentException.class)
    public void testGetLocalFileDataData_oneNull_throwsException() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.setSize(new ZipEightByteInteger(1000L));
        field.getLocalFileDataData();
    }

    // Test getLocalFileDataData returns correct data when both fields are set
    @Test
    public void testGetLocalFileDataData_bothSet_returnsCorrectData() {
        ZipEightByteInteger size = new ZipEightByteInteger(1000L);
        ZipEightByteInteger compressedSize = new ZipEightByteInteger(500L);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(size, compressedSize);
        byte[] data = field.getLocalFileDataData();
        assertEquals(2 * ZipConstants.DWORD, data.length);
        assertArrayEquals(size.getBytes(), java.util.Arrays.copyOfRange(data, 0, ZipConstants.DWORD));
        assertArrayEquals(compressedSize.getBytes(), java.util.Arrays.copyOfRange(data, ZipConstants.DWORD, 2 * ZipConstants.DWORD));
    }

    // Test getCentralDirectoryData with all fields set
    @Test
    public void testGetCentralDirectoryData_allSet_returnsCorrectData() {
        ZipEightByteInteger size = new ZipEightByteInteger(1000L);
        ZipEightByteInteger compressedSize = new ZipEightByteInteger(500L);
        ZipEightByteInteger offset = new ZipEightByteInteger(200L);
        ZipLong diskStart = new ZipLong(1L);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(size, compressedSize, offset, diskStart);
        byte[] data = field.getCentralDirectoryData();
        assertEquals(3 * ZipConstants.DWORD + ZipConstants.WORD, data.length);
        assertArrayEquals(size.getBytes(), java.util.Arrays.copyOfRange(data, 0, ZipConstants.DWORD));
        assertArrayEquals(compressedSize.getBytes(), java.util.Arrays.copyOfRange(data, ZipConstants.DWORD, 2 * ZipConstants.DWORD));
        assertArrayEquals(offset.getBytes(), java.util.Arrays.copyOfRange(data, 2 * ZipConstants.DWORD, 3 * ZipConstants.DWORD));
        assertArrayEquals(diskStart.getBytes(), java.util.Arrays.copyOfRange(data, 3 * ZipConstants.DWORD, 3 * ZipConstants.DWORD + ZipConstants.WORD));
    }

    // Tests parseFromLocalFileData with zero length
    @Test
    public void testParseFromLocalFileData_zeroLength_setsNothing() throws ZipException {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(new byte[0], 0, 0);
        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    // Tests parseFromLocalFileData throws exception when length is less than 2 * DWORD
    @Test(expected = ZipException.class)
    public void testParseFromLocalFileData_tooShort_throwsException() throws ZipException {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(new byte[ZipConstants.DWORD], 0, ZipConstants.DWORD);
    }

    // Tests parseFromLocalFileData with exactly 2 * DWORD length
    @Test
    public void testParseFromLocalFileData_exactSizes_setsSizes() throws ZipException {
        byte[] buffer = new byte[2 * ZipConstants.DWORD];
        long sizeVal = 1000L;
        long compressedSizeVal = 500L;
        System.arraycopy(ZipEightByteInteger.getBytes(sizeVal), 0, buffer, 0, ZipConstants.DWORD);
        System.arraycopy(ZipEightByteInteger.getBytes(compressedSizeVal), 0, buffer, ZipConstants.DWORD, ZipConstants.DWORD);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(buffer, 0, buffer.length);
        assertEquals(sizeVal, field.getSize().getLongValue());
        assertEquals(compressedSizeVal, field.getCompressedSize().getLongValue());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    // Tests parseFromLocalFileData with length containing offset and diskStart
    @Test
    public void testParseFromLocalFileData_fullData_setsAll() throws ZipException {
        byte[] buffer = new byte[3 * ZipConstants.DWORD + ZipConstants.WORD];
        long sizeVal = 1000L;
        long compressedSizeVal = 500L;
        long offsetVal = 200L;
        long diskStartVal = 1L;
        System.arraycopy(ZipEightByteInteger.getBytes(sizeVal), 0, buffer, 0, ZipConstants.DWORD);
        System.arraycopy(ZipEightByteInteger.getBytes(compressedSizeVal), 0, buffer, ZipConstants.DWORD, ZipConstants.DWORD);
        System.arraycopy(ZipEightByteInteger.getBytes(offsetVal), 0, buffer, 2 * ZipConstants.DWORD, ZipConstants.DWORD);
        System.arraycopy(ZipLong.getBytes(diskStartVal), 0, buffer, 3 * ZipConstants.DWORD, ZipConstants.WORD);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(buffer, 0, buffer.length);
        assertEquals(sizeVal, field.getSize().getLongValue());
        assertEquals(compressedSizeVal, field.getCompressedSize().getLongValue());
        assertEquals(offsetVal, field.getRelativeHeaderOffset().getLongValue());
        assertEquals(diskStartVal, field.getDiskStartNumber().getValue());
    }

    // Tests parseFromCentralDirectoryData with length >= 3 * DWORD + WORD
    @Test
    public void testParseFromCentralDirectoryData_fullLength_parsesAll() throws ZipException {
        byte[] buffer = new byte[3 * ZipConstants.DWORD + ZipConstants.WORD];
        long sizeVal = 1000L;
        long compressedSizeVal = 500L;
        long offsetVal = 200L;
        long diskStartVal = 1L;
        System.arraycopy(ZipEightByteInteger.getBytes(sizeVal), 0, buffer, 0, ZipConstants.DWORD);
        System.arraycopy(ZipEightByteInteger.getBytes(compressedSizeVal), 0, buffer, ZipConstants.DWORD, ZipConstants.DWORD);
        System.arraycopy(ZipEightByteInteger.getBytes(offsetVal), 0, buffer, 2 * ZipConstants.DWORD, ZipConstants.DWORD);
        System.arraycopy(ZipLong.getBytes(diskStartVal), 0, buffer, 3 * ZipConstants.DWORD, ZipConstants.WORD);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, buffer.length);
        assertEquals(sizeVal, field.getSize().getLongValue());
        assertEquals(compressedSizeVal, field.getCompressedSize().getLongValue());
        assertEquals(offsetVal, field.getRelativeHeaderOffset().getLongValue());
        assertEquals(diskStartVal, field.getDiskStartNumber().getValue());
    }

    // Tests parseFromCentralDirectoryData with length == 3 * DWORD
    @Test
    public void testParseFromCentralDirectoryData_lengthThreeDword_parsesSizesAndOffset() throws ZipException {
        byte[] buffer = new byte[3 * ZipConstants.DWORD];
        long sizeVal = 1000L;
        long compressedSizeVal = 500L;
        long offsetVal = 200L;
        System.arraycopy(ZipEightByteInteger.getBytes(sizeVal), 0, buffer, 0, ZipConstants.DWORD);
        System.arraycopy(ZipEightByteInteger.getBytes(compressedSizeVal), 0, buffer, ZipConstants.DWORD, ZipConstants.DWORD);
        System.arraycopy(ZipEightByteInteger.getBytes(offsetVal), 0, buffer, 2 * ZipConstants.DWORD, ZipConstants.DWORD);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, buffer.length);
        assertEquals(sizeVal, field.getSize().getLongValue());
        assertEquals(compressedSizeVal, field.getCompressedSize().getLongValue());
        assertEquals(offsetVal, field.getRelativeHeaderOffset().getLongValue());
        assertNull(field.getDiskStartNumber());
    }

    // Tests parseFromCentralDirectoryData with length % DWORD == WORD sets diskStart
    @Test
    public void testParseFromCentralDirectoryData_modWord_parsesDiskStart() throws ZipException {
        byte[] buffer = new byte[ZipConstants.WORD];
        long diskStartVal = 1L;
        System.arraycopy(ZipLong.getBytes(diskStartVal), 0, buffer, 0, ZipConstants.WORD);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, buffer.length);
        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertEquals(diskStartVal, field.getDiskStartNumber().getValue());
    }

    // Tests reparseCentralDirectoryData with valid data and all fields present
    @Test
    public void testReparseCentralDirectoryData_validData_parsesCorrectly() throws ZipException {
        byte[] buffer = new byte[3 * ZipConstants.DWORD + ZipConstants.WORD];
        long sizeVal = 1000L;
        long compressedSizeVal = 500L;
        long offsetVal = 200L;
        long diskStartVal = 1L;
        System.arraycopy(ZipEightByteInteger.getBytes(sizeVal), 0, buffer, 0, ZipConstants.DWORD);
        System.arraycopy(ZipEightByteInteger.getBytes(compressedSizeVal), 0, buffer, ZipConstants.DWORD, ZipConstants.DWORD);
        System.arraycopy(ZipEightByteInteger.getBytes(offsetVal), 0, buffer, 2 * ZipConstants.DWORD, ZipConstants.DWORD);
        System.arraycopy(ZipLong.getBytes(diskStartVal), 0, buffer, 3 * ZipConstants.DWORD, ZipConstants.WORD);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, buffer.length);
        field.reparseCentralDirectoryData(true, true, true, true);
        assertEquals(sizeVal, field.getSize().getLongValue());
        assertEquals(compressedSizeVal, field.getCompressedSize().getLongValue());
        assertEquals(offsetVal, field.getRelativeHeaderOffset().getLongValue());
        assertEquals(diskStartVal, field.getDiskStartNumber().getValue());
    }

    // Tests reparseCentralDirectoryData throws exception when raw data length doesn't match expected length
    @Test(expected = ZipException.class)
    public void testReparseCentralDirectoryData_lengthMismatch_throwsException() throws ZipException {
        byte[] buffer = new byte[2 * ZipConstants.DWORD];
        long sizeVal = 1000L;
        long compressedSizeVal = 500L;
        System.arraycopy(ZipEightByteInteger.getBytes(sizeVal), 0, buffer, 0, ZipConstants.DWORD);
        System.arraycopy(ZipEightByteInteger.getBytes(compressedSizeVal), 0, buffer, ZipConstants.DWORD, ZipConstants.DWORD);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, buffer.length);
        // Expected length should be 3 * DWORD + WORD, but raw data is only 2 * DWORD
        field.reparseCentralDirectoryData(true, true, true, true);
    }
}