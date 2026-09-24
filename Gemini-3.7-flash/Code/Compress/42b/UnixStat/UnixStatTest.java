package org.apache.commons.compress.archivers.zip;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UnixStatTest {

    // Tests exact value of PERM_MASK constant
    @Test
    public void testPermMask_constantValue_matchesOctal07777() {
        assertEquals(07777, UnixStat.PERM_MASK);
        assertEquals(4095, UnixStat.PERM_MASK);
    }

    // Tests exact value of LINK_FLAG constant
    @Test
    public void testLinkFlag_constantValue_matchesOctal0120000() {
        assertEquals(0120000, UnixStat.LINK_FLAG);
        assertEquals(40960, UnixStat.LINK_FLAG);
    }

    // Tests exact value of FILE_FLAG constant
    @Test
    public void testFileFlag_constantValue_matchesOctal0100000() {
        assertEquals(0100000, UnixStat.FILE_FLAG);
        assertEquals(32768, UnixStat.FILE_FLAG);
    }

    // Tests exact value of DIR_FLAG constant
    @Test
    public void testDirFlag_constantValue_matchesOctal040000() {
        assertEquals(040000, UnixStat.DIR_FLAG);
        assertEquals(16384, UnixStat.DIR_FLAG);
    }

    // Tests exact value of DEFAULT_LINK_PERM constant
    @Test
    public void testDefaultLinkPerm_constantValue_matchesOctal0777() {
        assertEquals(0777, UnixStat.DEFAULT_LINK_PERM);
        assertEquals(511, UnixStat.DEFAULT_LINK_PERM);
    }

    // Tests exact value of DEFAULT_DIR_PERM constant
    @Test
    public void testDefaultDirPerm_constantValue_matchesOctal0755() {
        assertEquals(0755, UnixStat.DEFAULT_DIR_PERM);
        assertEquals(493, UnixStat.DEFAULT_DIR_PERM);
    }

    // Tests exact value of DEFAULT_FILE_PERM constant
    @Test
    public void testDefaultFilePerm_constantValue_matchesOctal0644() {
        assertEquals(0644, UnixStat.DEFAULT_FILE_PERM);
        assertEquals(420, UnixStat.DEFAULT_FILE_PERM);
    }

    // Tests bitwise AND mask operation with file mode
    @Test
    public void testPermMask_bitwiseAndWithCombinedMode_extractsPermissions() {
        int fullMode = UnixStat.FILE_FLAG | UnixStat.DEFAULT_FILE_PERM;
        int permissions = fullMode & UnixStat.PERM_MASK;
        assertEquals(UnixStat.DEFAULT_FILE_PERM, permissions);
    }

    // Tests bitwise AND mask operation with directory mode
    @Test
    public void testPermMask_bitwiseAndWithDirectoryMode_extractsPermissions() {
        int fullMode = UnixStat.DIR_FLAG | UnixStat.DEFAULT_DIR_PERM;
        int permissions = fullMode & UnixStat.PERM_MASK;
        assertEquals(UnixStat.DEFAULT_DIR_PERM, permissions);
    }

    // Tests bitwise AND mask operation with symbolic link mode
    @Test
    public void testPermMask_bitwiseAndWithLinkMode_extractsPermissions() {
        int fullMode = UnixStat.LINK_FLAG | UnixStat.DEFAULT_LINK_PERM;
        int permissions = fullMode & UnixStat.PERM_MASK;
        assertEquals(UnixStat.DEFAULT_LINK_PERM, permissions);
    }

    // Tests that flags are mutually distinct and non-overlapping
    @Test
    public void testFlags_mutualExclusivity_noOverlappingBits() {
        assertEquals(0, UnixStat.DIR_FLAG & UnixStat.FILE_FLAG);
        assertEquals(UnixStat.FILE_FLAG, UnixStat.LINK_FLAG & UnixStat.FILE_FLAG);
        assertEquals(0, UnixStat.DIR_FLAG & UnixStat.PERM_MASK);
        assertEquals(0, UnixStat.FILE_FLAG & UnixStat.PERM_MASK);
        assertEquals(0, UnixStat.LINK_FLAG & UnixStat.PERM_MASK);
    }

    // Tests that default permissions fit within PERM_MASK
    @Test
    public void testDefaultPermissions_fitWithinPermMask() {
        assertEquals(UnixStat.DEFAULT_LINK_PERM, UnixStat.DEFAULT_LINK_PERM & UnixStat.PERM_MASK);
        assertEquals(UnixStat.DEFAULT_DIR_PERM, UnixStat.DEFAULT_DIR_PERM & UnixStat.PERM_MASK);
        assertEquals(UnixStat.DEFAULT_FILE_PERM, UnixStat.DEFAULT_FILE_PERM & UnixStat.PERM_MASK);
    }

    // Tests implementation of UnixStat interface
    @Test
    public void testInterfaceImplementation() {
        UnixStat stat = new UnixStat() {};
        assertNotNull(stat);
        assertTrue(stat instanceof UnixStat);
    }
}