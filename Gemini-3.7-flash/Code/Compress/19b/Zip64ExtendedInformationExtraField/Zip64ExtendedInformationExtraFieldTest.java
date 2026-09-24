package org.apache.commons.compress.archivers.zip;

import org.junit.Test;
import java.util.zip.ZipException;
import static org.junit.Assert.*;

public class Zip64ExtendedInformationExtraFieldTest {

    // Tests header ID constant value
    @Test
    public void testGetHeaderId_default_returnsZip64HeaderId() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        assertEquals(new ZipShort(0x0001), field.getHeaderId());
    }

    // Tests default constructor and getters/setters
    @Test
    public void testGettersAndSetters_validValues_returnsCorrectValues() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());

        ZipEightByteInteger size = new ZipEightByteInteger(100L);
        ZipEightByteInteger compSize = new ZipEightByteInteger(50L);
        ZipEightByteInteger offset = new ZipEightByteInteger(1000L);
        ZipLong disk = new ZipLong(1L);

        field.setSize(size);
        field.setCompressedSize(compSize);
        field.setRelativeHeaderOffset(offset);
        field.setDiskStartNumber(disk);

        assertEquals(size, field.getSize());
        assertEquals(compSize, field.getCompressedSize());
        assertEquals(offset, field.getRelativeHeaderOffset());
        assertEquals(disk, field.getDiskStartNumber());
    }

    // Tests two-arg constructor initialization
    @Test
    public void testConstructor_twoArgs_initializesSizes() {
        ZipEightByteInteger size = new ZipEightByteInteger(200L);
        ZipEightByteInteger compSize = new ZipEightByteInteger(150L);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(size, compSize);

        assertEquals(size, field.getSize());
        assertEquals(compSize, field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    // Tests four-arg constructor initialization
    @Test
    public void testConstructor_fourArgs_initializesAllFields() {
        ZipEightByteInteger size = new ZipEightByteInteger(300L);
        ZipEightByteInteger compSize = new ZipEightByteInteger(250L);
        ZipEightByteInteger offset = new ZipEightByteInteger(2000L);
        ZipLong disk = new ZipLong(2L);

        Zip64ExtendedInformationExtraField field =
            new Zip64ExtendedInformationExtraField(size, compSize, offset, disk);

        assertEquals(size, field.getSize());
        assertEquals(compSize, field.getCompressedSize());
        assertEquals(offset, field.getRelativeHeaderOffset());
        assertEquals(disk, field.getDiskStartNumber());
    }

    // Tests local file data when no fields are populated
    @Test
    public void testGetLocalFileDataData_noFields_returnsEmptyByteArray() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        assertEquals(new ZipShort(0), field.getLocalFileDataLength());
        assertArrayEquals(new byte[0], field.getLocalFileDataData());
    }

    // Tests local file data generation when both sizes are present
    @Test
    public void testGetLocalFileDataData_bothSizesPresent_returns16Bytes() {
        ZipEightByteInteger size = new ZipEightByteInteger(1024L);
        ZipEightByteInteger compSize = new ZipEightByteInteger(512L);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(size, compSize);

        assertEquals(new ZipShort(16), field.getLocalFileDataLength());
        byte[] data = field.getLocalFileDataData();
        assertEquals(16, data.length);

        ZipEightByteInteger readSize = new ZipEightByteInteger(data, 0);
        ZipEightByteInteger readCompSize = new ZipEightByteInteger(data, 8);
        assertEquals(size, readSize);
        assertEquals(compSize, readCompSize);
    }

    // Tests exception when only size is set for local file data
    @Test(expected = IllegalArgumentException.class)
    public void testGetLocalFileDataData_onlySizeSet_throwsIllegalArgumentException() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.setSize(new ZipEightByteInteger(100L));
        field.getLocalFileDataData();
    }

    // Tests exception when only compressed size is set for local file data
    @Test(expected = IllegalArgumentException.class)
    public void testGetLocalFileDataData_onlyCompressedSizeSet_throwsIllegalArgumentException() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.setCompressedSize(new ZipEightByteInteger(100L));
        field.getLocalFileDataData();
    }

    // Tests central directory data generation with all four fields
    @Test
    public void testGetCentralDirectoryData_allFieldsPresent_returns28Bytes() {
        ZipEightByteInteger size = new ZipEightByteInteger(1000L);
        ZipEightByteInteger compSize = new ZipEightByteInteger(800L);
        ZipEightByteInteger offset = new ZipEightByteInteger(50000L);
        ZipLong disk = new ZipLong(3L);

        Zip64ExtendedInformationExtraField field =
            new Zip64ExtendedInformationExtraField(size, compSize, offset, disk);

        assertEquals(new ZipShort(28), field.getCentralDirectoryLength());
        byte[] data = field.getCentralDirectoryData();
        assertEquals(28, data.length);

        assertEquals(size, new ZipEightByteInteger(data, 0));
        assertEquals(compSize, new ZipEightByteInteger(data, 8));
        assertEquals(offset, new ZipEightByteInteger(data, 16));
        assertEquals(disk, new ZipLong(data, 24));
    }

    // Tests central directory length with partially populated fields
    @Test
    public void testGetCentralDirectoryLength_partialFields_calculatesCorrectLength() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.setRelativeHeaderOffset(new ZipEightByteInteger(1234L));
        field.setDiskStartNumber(new ZipLong(1L));

        assertEquals(new ZipShort(12), field.getCentralDirectoryLength());
    }

    // Tests parsing empty local file data does nothing
    @Test
    public void testParseFromLocalFileData_lengthZero_doesNotSetFields() throws Exception {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        byte[] buffer = new byte[10];
        field.parseFromLocalFileData(buffer, 0, 0);

        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
    }

    // Tests exception when local file data length is less than 16 bytes
    @Test(expected = ZipException.class)
    public void testParseFromLocalFileData_lengthLessThan16_throwsZipException() throws Exception {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        byte[] buffer = new byte[8];
        field.parseFromLocalFileData(buffer, 0, 8);
    }

    // Tests parsing local file data with all four fields (28 bytes)
    @Test
    public void testParseFromLocalFileData_28Bytes_parsesAllFields() throws Exception {
        byte[] buffer = new byte[28];
        System.arraycopy(new ZipEightByteInteger(100L).getBytes(), 0, buffer, 0, 8);
        System.arraycopy(new ZipEightByteInteger(50L).getBytes(), 0, buffer, 8, 8);
        System.arraycopy(new ZipEightByteInteger(500L).getBytes(), 0, buffer, 16, 8);
        System.arraycopy(new ZipLong(1L).getBytes(), 0, buffer, 24, 4);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(buffer, 0, 28);

        assertEquals(new ZipEightByteInteger(100L), field.getSize());
        assertEquals(new ZipEightByteInteger(50L), field.getCompressedSize());
        assertEquals(new ZipEightByteInteger(500L), field.getRelativeHeaderOffset());
        assertEquals(new ZipLong(1L), field.getDiskStartNumber());
    }

    // Tests parsing central directory data with length 24 (both sizes and offset)
    @Test
    public void testParseFromCentralDirectoryData_24Bytes_parsesThreeDWORDs() throws Exception {
        byte[] buffer = new byte[24];
        System.arraycopy(new ZipEightByteInteger(10L).getBytes(), 0, buffer, 0, 8);
        System.arraycopy(new ZipEightByteInteger(5L).getBytes(), 0, buffer, 8, 8);
        System.arraycopy(new ZipEightByteInteger(100L).getBytes(), 0, buffer, 16, 8);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, 24);

        assertEquals(new ZipEightByteInteger(10L), field.getSize());
        assertEquals(new ZipEightByteInteger(5L), field.getCompressedSize());
        assertEquals(new ZipEightByteInteger(100L), field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    // Tests parsing central directory data where length % 8 == 4
    @Test
    public void testParseFromCentralDirectoryData_lengthModEightIsFour_parsesDiskStart() throws Exception {
        byte[] buffer = new byte[4];
        System.arraycopy(new ZipLong(7L).getBytes(), 0, buffer, 0, 4);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, 4);

        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertEquals(new ZipLong(7L), field.getDiskStartNumber());
    }

    // Tests reparseCentralDirectoryData with null raw data does nothing
    @Test
    public void testReparseCentralDirectoryData_nullRawData_doesNothing() throws Exception {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.reparseCentralDirectoryData(true, true, true, true);
        assertNull(field.getSize());
    }

    // Tests reparseCentralDirectoryData successfully unpacking selective fields
    @Test
    public void testReparseCentralDirectoryData_validRawData_unpacksRequestedFields() throws Exception {
        byte[] buffer = new byte[16];
        System.arraycopy(new ZipEightByteInteger(42L).getBytes(), 0, buffer, 0, 8);
        System.arraycopy(new ZipEightByteInteger(24L).getBytes(), 0, buffer, 8, 8);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, 16);

        field.reparseCentralDirectoryData(true, true, false, false);
        assertEquals(new ZipEightByteInteger(42L), field.getSize());
        assertEquals(new ZipEightByteInteger(24L), field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    // Tests reparseCentralDirectoryData throws ZipException when length does not match expected
    @Test(expected = ZipException.class)
    public void testReparseCentralDirectoryData_lengthMismatch_throwsZipException() throws Exception {
        byte[] buffer = new byte[8];
        System.arraycopy(new ZipEightByteInteger(42L).getBytes(), 0, buffer, 0, 8);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, 8);

        field.reparseCentralDirectoryData(true, true, false, false);
    }
}