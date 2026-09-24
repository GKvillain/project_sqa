package org.apache.commons.compress.changes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChangeSetPerformerTest {

    private ChangeSet changeSet;

    @Before
    public void setUp() {
        changeSet = new ChangeSet();
    }

    // Tests performing an empty changeset on an empty archive stream
    @Test
    public void testPerform_emptyChangeSetAndEmptyStream_returnsEmptyResults() throws IOException {
        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        TestArchiveInputStream in = new TestArchiveInputStream(Collections.<ArchiveEntry>emptyList(), null);
        TestArchiveOutputStream out = new TestArchiveOutputStream();

        ChangeSetResults results = performer.perform(in, out);

        assertTrue(results.getAddedFromChangeSet().isEmpty());
        assertTrue(results.getAddedFromStream().isEmpty());
        assertTrue(results.getDeleted().isEmpty());
        assertTrue(out.writtenEntries.isEmpty());
    }

    // Tests stream copy when there are no changes in ChangeSet
    @Test
    public void testPerform_noChanges_copiesAllEntriesFromStream() throws IOException {
        List<ArchiveEntry> entries = new ArrayList<ArchiveEntry>();
        entries.add(new SimpleArchiveEntry("file1.txt"));
        entries.add(new SimpleArchiveEntry("file2.txt"));

        TestArchiveInputStream in = new TestArchiveInputStream(entries, null);
        TestArchiveOutputStream out = new TestArchiveOutputStream();

        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        ChangeSetResults results = performer.perform(in, out);

        assertEquals(2, results.getAddedFromStream().size());
        assertTrue(results.hasBeenAdded("file1.txt"));
        assertTrue(results.hasBeenAdded("file2.txt"));
        assertEquals(2, out.writtenEntries.size());
    }

    // Tests adding an entry with replace mode enabled
    @Test
    public void testPerform_addWithReplaceMode_addsFromChangeSetAndReplacesStream() throws IOException {
        changeSet.add(new SimpleArchiveEntry("file1.txt"), new ByteArrayInputStream("new".getBytes()), true);

        List<ArchiveEntry> entries = new ArrayList<ArchiveEntry>();
        entries.add(new SimpleArchiveEntry("file1.txt"));
        entries.add(new SimpleArchiveEntry("file2.txt"));

        TestArchiveInputStream in = new TestArchiveInputStream(entries, null);
        TestArchiveOutputStream out = new TestArchiveOutputStream();

        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        ChangeSetResults results = performer.perform(in, out);

        assertTrue(results.getAddedFromChangeSet().contains("file1.txt"));
        assertTrue(results.getAddedFromStream().contains("file2.txt"));
        assertFalse(results.getAddedFromStream().contains("file1.txt"));
        assertEquals(2, out.writtenEntries.size());
    }

    // Tests adding an entry without replace mode when entry is not in stream
    @Test
    public void testPerform_addWithoutReplaceModeNotInStream_addsFromChangeSet() throws IOException {
        changeSet.add(new SimpleArchiveEntry("new_file.txt"), new ByteArrayInputStream("data".getBytes()), false);

        List<ArchiveEntry> entries = new ArrayList<ArchiveEntry>();
        entries.add(new SimpleArchiveEntry("stream_file.txt"));

        TestArchiveInputStream in = new TestArchiveInputStream(entries, null);
        TestArchiveOutputStream out = new TestArchiveOutputStream();

        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        ChangeSetResults results = performer.perform(in, out);

        assertTrue(results.getAddedFromChangeSet().contains("new_file.txt"));
        assertTrue(results.getAddedFromStream().contains("stream_file.txt"));
        assertEquals(2, out.writtenEntries.size());
    }

    // Tests adding an entry without replace mode when entry is already present in stream
    @Test
    public void testPerform_addWithoutReplaceModeAlreadyInStream_keepsStreamVersion() throws IOException {
        changeSet.add(new SimpleArchiveEntry("file1.txt"), new ByteArrayInputStream("new_data".getBytes()), false);

        List<ArchiveEntry> entries = new ArrayList<ArchiveEntry>();
        entries.add(new SimpleArchiveEntry("file1.txt"));

        TestArchiveInputStream in = new TestArchiveInputStream(entries, null);
        TestArchiveOutputStream out = new TestArchiveOutputStream();

        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        ChangeSetResults results = performer.perform(in, out);

        assertTrue(results.getAddedFromStream().contains("file1.txt"));
        assertFalse(results.getAddedFromChangeSet().contains("file1.txt"));
        assertEquals(1, out.writtenEntries.size());
    }

    // Tests deleting a single file by filename
    @Test
    public void testPerform_deleteFile_deletesTargetFileFromStream() throws IOException {
        changeSet.delete("to_delete.txt");

        List<ArchiveEntry> entries = new ArrayList<ArchiveEntry>();
        entries.add(new SimpleArchiveEntry("to_delete.txt"));
        entries.add(new SimpleArchiveEntry("to_keep.txt"));

        TestArchiveInputStream in = new TestArchiveInputStream(entries, null);
        TestArchiveOutputStream out = new TestArchiveOutputStream();

        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        ChangeSetResults results = performer.perform(in, out);

        assertTrue(results.getDeleted().contains("to_delete.txt"));
        assertFalse(results.getDeleted().contains("to_keep.txt"));
        assertTrue(results.getAddedFromStream().contains("to_keep.txt"));
        assertEquals(1, out.writtenEntries.size());
        assertEquals("to_keep.txt", out.writtenEntries.get(0).getName());
    }

    // Tests deleting a directory and its nested contents
    @Test
    public void testPerform_deleteDir_deletesDirectoryAndContainedFiles() throws IOException {
        changeSet.deleteDir("folder");

        List<ArchiveEntry> entries = new ArrayList<ArchiveEntry>();
        entries.add(new SimpleArchiveEntry("folder/file1.txt"));
        entries.add(new SimpleArchiveEntry("folder/sub/file2.txt"));
        entries.add(new SimpleArchiveEntry("folder_other/file3.txt"));
        entries.add(new SimpleArchiveEntry("root.txt"));

        TestArchiveInputStream in = new TestArchiveInputStream(entries, null);
        TestArchiveOutputStream out = new TestArchiveOutputStream();

        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        ChangeSetResults results = performer.perform(in, out);

        assertTrue(results.getDeleted().contains("folder/file1.txt"));
        assertTrue(results.getDeleted().contains("folder/sub/file2.txt"));
        assertTrue(results.getAddedFromStream().contains("folder_other/file3.txt"));
        assertTrue(results.getAddedFromStream().contains("root.txt"));
        assertEquals(2, out.writtenEntries.size());
    }

    // Tests isDeletedLater logic when a file delete change occurs later in working set
    @Test
    public void testPerform_deletedLaterByFile_doesNotAddToOutputStream() throws IOException {
        changeSet.add(new SimpleArchiveEntry("file1.txt"), new ByteArrayInputStream("data".getBytes()), false);
        changeSet.delete("file1.txt");

        List<ArchiveEntry> entries = new ArrayList<ArchiveEntry>();
        entries.add(new SimpleArchiveEntry("file1.txt"));

        TestArchiveInputStream in = new TestArchiveInputStream(entries, null);
        TestArchiveOutputStream out = new TestArchiveOutputStream();

        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        ChangeSetResults results = performer.perform(in, out);

        assertTrue(results.getDeleted().contains("file1.txt"));
        assertFalse(results.hasBeenAdded("file1.txt"));
        assertEquals(0, out.writtenEntries.size());
    }

    // Tests isDeletedLater logic when a directory delete change occurs later in working set
    @Test
    public void testPerform_deletedLaterByDir_doesNotAddToOutputStream() throws IOException {
        changeSet.add(new SimpleArchiveEntry("dir/file1.txt"), new ByteArrayInputStream("data".getBytes()), false);
        changeSet.deleteDir("dir");

        List<ArchiveEntry> entries = new ArrayList<ArchiveEntry>();
        entries.add(new SimpleArchiveEntry("dir/file1.txt"));

        TestArchiveInputStream in = new TestArchiveInputStream(entries, null);
        TestArchiveOutputStream out = new TestArchiveOutputStream();

        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        ChangeSetResults results = performer.perform(in, out);

        assertTrue(results.getDeleted().contains("dir/file1.txt"));
        assertFalse(results.hasBeenAdded("dir/file1.txt"));
        assertEquals(0, out.writtenEntries.size());
    }

    // Tests handling of entry with null name in stream
    @Test
    public void testPerform_nullEntryNameInStream_copiesEntryWithoutDeletionCheckFailure() throws IOException {
        changeSet.delete("file1.txt");
        changeSet.deleteDir("dir");

        List<ArchiveEntry> entries = new ArrayList<ArchiveEntry>();
        entries.add(new SimpleArchiveEntry(null));

        TestArchiveInputStream in = new TestArchiveInputStream(entries, null);
        TestArchiveOutputStream out = new TestArchiveOutputStream();

        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        ChangeSetResults results = performer.perform(in, out);

        assertEquals(1, out.writtenEntries.size());
        assertTrue(results.getDeleted().isEmpty());
    }

    // Tests multiple calls on the same ChangeSetPerformer instance
    @Test
    public void testPerform_multipleCalls_operatesIndependently() throws IOException {
        changeSet.delete("to_delete.txt");
        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);

        List<ArchiveEntry> entries1 = new ArrayList<ArchiveEntry>();
        entries1.add(new SimpleArchiveEntry("to_delete.txt"));
        entries1.add(new SimpleArchiveEntry("keep1.txt"));

        TestArchiveInputStream in1 = new TestArchiveInputStream(entries1, null);
        TestArchiveOutputStream out1 = new TestArchiveOutputStream();
        ChangeSetResults results1 = performer.perform(in1, out1);

        assertEquals(1, results1.getDeleted().size());
        assertEquals(1, results1.getAddedFromStream().size());

        List<ArchiveEntry> entries2 = new ArrayList<ArchiveEntry>();
        entries2.add(new SimpleArchiveEntry("to_delete.txt"));
        entries2.add(new SimpleArchiveEntry("keep2.txt"));

        TestArchiveInputStream in2 = new TestArchiveInputStream(entries2, null);
        TestArchiveOutputStream out2 = new TestArchiveOutputStream();
        ChangeSetResults results2 = performer.perform(in2, out2);

        assertEquals(1, results2.getDeleted().size());
        assertEquals(1, results2.getAddedFromStream().size());
    }

    // Tests exception propagation during stream read
    @Test(expected = IOException.class)
    public void testPerform_streamThrowsIOException_propagatesException() throws IOException {
        ArchiveInputStream errorStream = new ArchiveInputStream() {
            @Override
            public ArchiveEntry getNextEntry() throws IOException {
                throw new IOException("Simulated read error");
            }

            @Override
            public int read() throws IOException {
                return -1;
            }
        };

        TestArchiveOutputStream out = new TestArchiveOutputStream();
        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        performer.perform(errorStream, out);
    }

    // Helper ArchiveEntry implementation for tests
    private static class SimpleArchiveEntry implements ArchiveEntry {
        private final String name;

        public SimpleArchiveEntry(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public long getSize() {
            return 0;
        }

        @Override
        public boolean isDirectory() {
            return name != null && name.endsWith("/");
        }

        @Override
        public Date getLastModifiedDate() {
            return new Date();
        }
    }

    // Helper ArchiveInputStream implementation for tests
    private static class TestArchiveInputStream extends ArchiveInputStream {
        private final List<ArchiveEntry> entries;
        private final List<byte[]> contents;
        private int index = 0;
        private ByteArrayInputStream currentStream = null;

        public TestArchiveInputStream(List<ArchiveEntry> entries, List<byte[]> contents) {
            this.entries = entries;
            this.contents = contents;
        }

        @Override
        public ArchiveEntry getNextEntry() throws IOException {
            if (index < entries.size()) {
                ArchiveEntry entry = entries.get(index);
                byte[] data = (contents != null && index < contents.size()) ? contents.get(index) : new byte[0];
                currentStream = new ByteArrayInputStream(data);
                index++;
                return entry;
            }
            return null;
        }

        @Override
        public int read() throws IOException {
            return (currentStream != null) ? currentStream.read() : -1;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return (currentStream != null) ? currentStream.read(b, off, len) : -1;
        }
    }

    // Helper ArchiveOutputStream implementation for tests
    private static class TestArchiveOutputStream extends ArchiveOutputStream {
        final List<ArchiveEntry> writtenEntries = new ArrayList<ArchiveEntry>();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        @Override
        public void putArchiveEntry(ArchiveEntry entry) throws IOException {
            writtenEntries.add(entry);
        }

        @Override
        public void closeArchiveEntry() throws IOException {
        }

        @Override
        public void finish() throws IOException {
        }

        @Override
        public ArchiveEntry createArchiveEntry(File inputFile, String entryName) throws IOException {
            return new SimpleArchiveEntry(entryName);
        }

        @Override
        public void close() throws IOException {
            out.close();
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
        }
    }
}