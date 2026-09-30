package org.apache.commons.compress.changes;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.*;
import java.util.*;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.*;

public class ChangeSetPerformerTest {

    private byte[] createZip(String... entryNames) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(bos)) {
            for (String name : entryNames) {
                ZipArchiveEntry entry = new ZipArchiveEntry(name);
                zos.putArchiveEntry(entry);
                if (!name.endsWith("/")) {
                    zos.write("content".getBytes("UTF-8"));
                }
                zos.closeArchiveEntry();
            }
        }
        return bos.toByteArray();
    }

    private List<String> getEntryNames(byte[] zipBytes) throws IOException {
        List<String> names = new ArrayList<>();
        try (ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes))) {
            ArchiveEntry e;
            while ((e = zin.getNextEntry()) != null) {
                names.add(e.getName());
            }
        }
        return names;
    }

    // Test 1: No changes should copy all entries
    @Test
    public void testPerform_noChanges_copiesAllEntries() throws Exception {
        byte[] inputZip = createZip("a.txt", "b.txt", "c/", "c/d.txt");
        ByteArrayOutputStream outputBos = new ByteArrayOutputStream();
        try (ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(inputZip));
             ZipArchiveOutputStream zout = new ZipArchiveOutputStream(outputBos)) {
            ChangeSet changeSet = new ChangeSet();
            ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
            performer.perform(zin, zout);
        }
        List<String> outputNames = getEntryNames(outputBos.toByteArray());
        assertEquals(4, outputNames.size());
        assertTrue(outputNames.contains("a.txt"));
        assertTrue(outputNames.contains("b.txt"));
        assertTrue(outputNames.contains("c/"));
        assertTrue(outputNames.contains("c/d.txt"));
    }

    // Test 2: Delete exact entry
    @Test
    public void testPerform_deleteExactEntry_removesIt() throws Exception {
        byte[] inputZip = createZip("a.txt", "b.txt");
        ByteArrayOutputStream outputBos = new ByteArrayOutputStream();
        try (ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(inputZip));
             ZipArchiveOutputStream zout = new ZipArchiveOutputStream(outputBos)) {
            ChangeSet changeSet = new ChangeSet();
            changeSet.delete("a.txt");
            ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
            performer.perform(zin, zout);
        }
        List<String> outputNames = getEntryNames(outputBos.toByteArray());
        assertEquals(1, outputNames.size());
        assertEquals("b.txt", outputNames.get(0));
    }

    // Test 3: Delete directory without trailing slash
    @Test
    public void testPerform_deleteDirectory_removesItselfAndChildren() throws Exception {
        byte[] inputZip = createZip("dir/", "dir/f1.txt", "dir/f2.txt", "other.txt");
        ByteArrayOutputStream outputBos = new ByteArrayOutputStream();
        try (ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(inputZip));
             ZipArchiveOutputStream zout = new ZipArchiveOutputStream(outputBos)) {
            ChangeSet changeSet = new ChangeSet();
            changeSet.deleteDir("dir");
            ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
            performer.perform(zin, zout);
        }
        List<String> outputNames = getEntryNames(outputBos.toByteArray());
        assertEquals(1, outputNames.size());
        assertEquals("other.txt", outputNames.get(0));
    }

    // Test 4: Delete directory with trailing slash (potential bug area)
    @Test
    public void testPerform_deleteDirectoryWithTrailingSlash_removesItselfAndChildren() throws Exception {
        byte[] inputZip = createZip("dir/", "dir/f1.txt", "other.txt");
        ByteArrayOutputStream outputBos = new ByteArrayOutputStream();
        try (ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(inputZip));
             ZipArchiveOutputStream zout = new ZipArchiveOutputStream(outputBos)) {
            ChangeSet changeSet = new ChangeSet();
            changeSet.deleteDir("dir/");
            ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
            performer.perform(zin, zout);
        }
        List<String> outputNames = getEntryNames(outputBos.toByteArray());
        assertFalse("Directory entry 'dir/' should be deleted", outputNames.contains("dir/"));
        assertFalse("File 'dir/f1.txt' should be deleted", outputNames.contains("dir/f1.txt"));
        assertEquals(1, outputNames.size());
        assertEquals("other.txt", outputNames.get(0));
    }

    // Test 5: Add entry with replace mode (default) should place it first
    @Test
    public void testPerform_addWithReplaceMode_addsBeforeOriginal() throws Exception {
        byte[] inputZip = createZip("original.txt");
        ByteArrayOutputStream outputBos = new ByteArrayOutputStream();
        try (ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(inputZip));
             ZipArchiveOutputStream zout = new ZipArchiveOutputStream(outputBos)) {
            ChangeSet changeSet = new ChangeSet();
            ZipArchiveEntry addEntry = new ZipArchiveEntry("new.txt");
            changeSet.add(addEntry, new ByteArrayInputStream("new content".getBytes("UTF-8")));
            ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
            performer.perform(zin, zout);
        }
        List<String> outputNames = getEntryNames(outputBos.toByteArray());
        assertEquals(2, outputNames.size());
        assertEquals("new.txt", outputNames.get(0));
        assertEquals("original.txt", outputNames.get(1));
    }

    // Test 6: Add after delete (same name) results in one entry from changeset
    @Test
    public void testPerform_addAfterDelete_entryAddedFromChangeSet() throws Exception {
        byte[] inputZip = createZip("a.txt");
        ByteArrayOutputStream outputBos = new ByteArrayOutputStream();
        try (ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(inputZip));
             ZipArchiveOutputStream zout = new ZipArchiveOutputStream(outputBos)) {
            ChangeSet changeSet = new ChangeSet();
            changeSet.delete("a.txt");
            ZipArchiveEntry addEntry = new ZipArchiveEntry("a.txt");
            changeSet.add(addEntry, new ByteArrayInputStream("new content".getBytes("UTF-8")));
            ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
            performer.perform(zin, zout);
        }
        List<String> outputNames = getEntryNames(outputBos.toByteArray());
        assertEquals(1, outputNames.size());
        assertEquals("a.txt", outputNames.get(0));
    }

    // Test 7: Delete directory should not remove entries that start with same prefix but are not under the directory
    @Test
    public void testPerform_deleteDirectory_matchesPrefixOnly_notRemoveSimilar() throws Exception {
        byte[] inputZip = createZip("dir/", "dir/file.txt", "dir2.txt", "dir2/", "dir2/x.txt");
        ByteArrayOutputStream outputBos = new ByteArrayOutputStream();
        try (ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(inputZip));
             ZipArchiveOutputStream zout = new ZipArchiveOutputStream(outputBos)) {
            ChangeSet changeSet = new ChangeSet();
            changeSet.deleteDir("dir");
            ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
            performer.perform(zin, zout);
        }
        List<String> outputNames = getEntryNames(outputBos.toByteArray());
        assertFalse(outputNames.contains("dir/"));
        assertFalse(outputNames.contains("dir/file.txt"));
        assertTrue(outputNames.contains("dir2.txt"));
        assertTrue(outputNames.contains("dir2/"));
        assertTrue(outputNames.contains("dir2/x.txt"));
        assertEquals(3, outputNames.size());
    }

    // Test 8: Add replace mode should not duplicate if same entry exists in input stream
    @Test
    public void testPerform_addReplaceMode_avoidsDuplicatingInputStreamEntry() throws Exception {
        byte[] inputZip = createZip("a.txt");
        ByteArrayOutputStream outputBos = new ByteArrayOutputStream();
        try (ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(inputZip));
             ZipArchiveOutputStream zout = new ZipArchiveOutputStream(outputBos)) {
            ChangeSet changeSet = new ChangeSet();
            ZipArchiveEntry addEntry = new ZipArchiveEntry("a.txt");
            changeSet.add(addEntry, new ByteArrayInputStream("new content".getBytes("UTF-8")));
            ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
            performer.perform(zin, zout);
        }
        List<String> outputNames = getEntryNames(outputBos.toByteArray());
        assertEquals(1, outputNames.size());
        assertEquals("a.txt", outputNames.get(0));
    }

    // Test 9: Empty input stream with add
    @Test
    public void testPerform_emptyInputStream_addEntry() throws Exception {
        byte[] inputZip = createZip();
        ByteArrayOutputStream outputBos = new ByteArrayOutputStream();
        try (ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(inputZip));
             ZipArchiveOutputStream zout = new ZipArchiveOutputStream(outputBos)) {
            ChangeSet changeSet = new ChangeSet();
            ZipArchiveEntry addEntry = new ZipArchiveEntry("new.txt");
            changeSet.add(addEntry, new ByteArrayInputStream("content".getBytes("UTF-8")));
            ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
            performer.perform(zin, zout);
        }
        List<String> outputNames = getEntryNames(outputBos.toByteArray());
        assertEquals(1, outputNames.size());
        assertEquals("new.txt", outputNames.get(0));
    }

    // Test 10: Multiple changes (delete, deleteDir, add)
    @Test
    public void testPerform_multipleChanges_correctOutput() throws Exception {
        byte[] inputZip = createZip("a.txt", "b.txt", "dir/", "dir/x.txt");
        ByteArrayOutputStream outputBos = new ByteArrayOutputStream();
        try (ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(inputZip));
             ZipArchiveOutputStream zout = new ZipArchiveOutputStream(outputBos)) {
            ChangeSet changeSet = new ChangeSet();
            changeSet.delete("a.txt");
            changeSet.deleteDir("dir");
            ZipArchiveEntry addEntry = new ZipArchiveEntry("c.txt");
            changeSet.add(addEntry, new ByteArrayInputStream("c content".getBytes("UTF-8")));
            ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
            performer.perform(zin, zout);
        }
        List<String> outputNames = getEntryNames(outputBos.toByteArray());
        assertEquals(2, outputNames.size());
        assertEquals("c.txt", outputNames.get(0));
        assertEquals("b.txt", outputNames.get(1));
    }

    // Test 11: Delete directory entry with no children
    @Test
    public void testPerform_deleteDirOnDirectoryEntryOnly_removesIt() throws Exception {
        byte[] inputZip = createZip("mydir/");
        ByteArrayOutputStream outputBos = new ByteArrayOutputStream();
        try (ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(inputZip));
             ZipArchiveOutputStream zout = new ZipArchiveOutputStream(outputBos)) {
            ChangeSet changeSet = new ChangeSet();
            changeSet.deleteDir("mydir");
            ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
            performer.perform(zin, zout);
        }
        List<String> outputNames = getEntryNames(outputBos.toByteArray());
        assertTrue(outputNames.isEmpty());
    }

    // Test 12: Replace add before delete directory keeps the added entry
    @Test
    public void testPerform_addReplaceBeforeDeleteDir_retainsAddedEntry() throws Exception {
        byte[] inputZip = createZip("dir/", "dir/f.txt");
        ByteArrayOutputStream outputBos = new ByteArrayOutputStream();
        try (ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(inputZip));
             ZipArchiveOutputStream zout = new ZipArchiveOutputStream(outputBos)) {
            ChangeSet changeSet = new ChangeSet();
            changeSet.deleteDir("dir");
            ZipArchiveEntry addEntry = new ZipArchiveEntry("dir/f.txt");
            changeSet.add(addEntry, new ByteArrayInputStream("new f content".getBytes("UTF-8")));
            ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
            performer.perform(zin, zout);
        }
        List<String> outputNames = getEntryNames(outputBos.toByteArray());
        assertEquals(1, outputNames.size());
        assertEquals("dir/f.txt", outputNames.get(0));
    }

    // --- New test cases targeting uncovered areas ---

    // Test 13: Delete a file that does not exist -> archive unchanged
    @Test
    public void testPerform_deleteNonExistentFile_doesNotChangeArchive() throws Exception {
        byte[] inputZip = createZip("a.txt");
        ByteArrayOutputStream outputBos = new ByteArrayOutputStream();
        try (ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(inputZip));
             ZipArchiveOutputStream zout = new ZipArchiveOutputStream(outputBos)) {
            ChangeSet changeSet = new ChangeSet();
            changeSet.delete("nonexistent.txt");
            ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
            performer.perform(zin, zout);
        }
        List<String> outputNames = getEntryNames(outputBos.toByteArray());
        assertEquals(1, outputNames.size());
        assertTrue(outputNames.contains("a.txt"));
    }

    // Test 14: Delete a directory that does not exist -> archive unchanged
    @Test
    public void testPerform_deleteNonExistentDirectory_doesNotChangeArchive() throws Exception {
        byte[] inputZip = createZip("dir/", "dir/file.txt");
        ByteArrayOutputStream outputBos = new ByteArrayOutputStream();
        try (ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(inputZip));
             ZipArchiveOutputStream zout = new ZipArchiveOutputStream(outputBos)) {
            ChangeSet changeSet = new ChangeSet();
            changeSet.deleteDir("missingDir");
            ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
            performer.perform(zin, zout);
        }
        List<String> outputNames = getEntryNames(outputBos.toByteArray());
        assertEquals(2, outputNames.size());
        assertTrue(outputNames.contains("dir/"));
        assertTrue(outputNames.contains("dir/file.txt"));
    }

    // Test 15: Add with replace=false should duplicate an existing entry
    @Test
    public void testPerform_addWithReplaceFalse_duplicatesExistingEntry() throws Exception {
        byte[] inputZip = createZip("a.txt");
        ByteArrayOutputStream outputBos = new ByteArrayOutputStream();
        try (ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(inputZip));
             ZipArchiveOutputStream zout = new ZipArchiveOutputStream(outputBos)) {
            ChangeSet changeSet = new ChangeSet();
            ZipArchiveEntry addEntry = new ZipArchiveEntry("a.txt");
            // replace = false → keep original and add new one
            changeSet.add(addEntry, new ByteArrayInputStream("new content".getBytes("UTF-8")), false);
            ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
            performer.perform(zin, zout);
        }
        List<String> outputNames = getEntryNames(outputBos.toByteArray());
        assertEquals(2, outputNames.size());
        assertEquals("a.txt", outputNames.get(0)); // added entry appears first
        assertEquals("a.txt", outputNames.get(1)); // original entry remains
    }

    // Test 16: Add a directory entry (name ending with '/')
    @Test
    public void testPerform_addDirectoryEntry_addsItToArchive() throws Exception {
        byte[] inputZip = createZip("existing.txt");
        ByteArrayOutputStream outputBos = new ByteArrayOutputStream();
        try (ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(inputZip));
             ZipArchiveOutputStream zout = new ZipArchiveOutputStream(outputBos)) {
            ChangeSet changeSet = new ChangeSet();
            ZipArchiveEntry dirEntry = new ZipArchiveEntry("newdir/");
            changeSet.add(dirEntry, new ByteArrayInputStream(new byte[0]));
            ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
            performer.perform(zin, zout);
        }
        List<String> outputNames = getEntryNames(outputBos.toByteArray());
        assertEquals(2, outputNames.size());
        assertTrue(outputNames.contains("existing.txt"));
        assertTrue(outputNames.contains("newdir/"));
    }

    // Test 17: Add then delete the same entry -> no entry in output
    @Test
    public void testPerform_addThenDeleteSameEntry_removesIt() throws Exception {
        byte[] inputZip = createZip("original.txt");
        ByteArrayOutputStream outputBos = new ByteArrayOutputStream();
        try (ZipArchiveInputStream zin = new ZipArchiveInputStream(new ByteArrayInputStream(inputZip));
             ZipArchiveOutputStream zout = new ZipArchiveOutputStream(outputBos)) {
            ChangeSet changeSet = new ChangeSet();
            ZipArchiveEntry addEntry = new ZipArchiveEntry("temp.txt");
            changeSet.add(addEntry, new ByteArrayInputStream("temp content".getBytes("UTF-8")));
            changeSet.delete("temp.txt");
            ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
            performer.perform(zin, zout);
        }
        List<String> outputNames = getEntryNames(outputBos.toByteArray());
        assertEquals(1, outputNames.size());
        assertTrue(outputNames.contains("original.txt"));
    }
}