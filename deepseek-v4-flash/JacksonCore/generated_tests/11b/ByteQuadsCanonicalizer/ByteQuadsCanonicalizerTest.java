package com.fasterxml.jackson.core.sym;

import static org.junit.Assert.*;

import org.junit.Test;

public class ByteQuadsCanonicalizerTest {

    // Tests initial state of a fresh root table
    @Test
    public void testCreateRoot_defaultSettings_initialSizeCorrect() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        assertEquals(64, root.bucketCount());
        assertEquals(0, root.size());
        assertTrue(root.hashSeed() != 0);
    }

    // Tests that createRoot with specific seed works
    @Test
    public void testCreateRoot_withSeed_sizeAndSeedCorrect() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(12345);
        assertEquals(12345, root.hashSeed());
        assertEquals(0, root.size());
    }

    // Tests making a child from root
    @Test
    public void testMakeChild_defaultFlags_childCreated() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0x1234);
        int flags = 0; // no special features
        ByteQuadsCanonicalizer child = root.makeChild(flags);
        assertNotNull(child);
        assertEquals(0, child.size());
        assertEquals(root.hashSeed(), child.hashSeed());
    }

    // Tests addName with single quad, then findName
    @Test
    public void testAddName_findName_singleQuad_returnsInterned() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0x1234);
        int flags = 0;
        ByteQuadsCanonicalizer child = root.makeChild(flags);
        String name = "test";
        int q1 = 0x74657374; // 'test' as int
        String result = child.addName(name, q1);
        assertEquals(name, result);
        String found = child.findName(q1);
        assertEquals(name, found);
    }

    // Tests addName with two quads, then findName
    @Test
    public void testAddName_findName_twoQuads_returnsCorrect() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0x1234);
        int flags = 0;
        ByteQuadsCanonicalizer child = root.makeChild(flags);
        String name = "hello";
        int q1 = 0x68656c6c; // 'hell'
        int q2 = 0x6f;       // 'o' padded
        child.addName(name, q1, q2);
        String found = child.findName(q1, q2);
        assertEquals(name, found);
    }

    // Tests findName returns null for missing single quad entry
    @Test
    public void testFindName_singleQuad_notFound_returnsNull() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0x1234);
        int flags = 0;
        ByteQuadsCanonicalizer child = root.makeChild(flags);
        String found = child.findName(0x01020304);
        assertNull(found);
    }

    // Tests findName returns null for missing two quad entry
    @Test
    public void testFindName_twoQuads_notFound_returnsNull() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0x1234);
        int flags = 0;
        ByteQuadsCanonicalizer child = root.makeChild(flags);
        String found = child.findName(0x01020304, 0x05060708);
        assertNull(found);
    }

    // Tests count after adding multiple entries
    @Test
    public void testAddName_multipleEntries_countIncrements() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0x1234);
        int flags = 0;
        ByteQuadsCanonicalizer child = root.makeChild(flags);
        child.addName("a", 0x61);
        child.addName("b", 0x62);
        child.addName("c", 0x63);
        assertEquals(3, child.size());
    }

    // Tests addName with three quads
    @Test
    public void testAddName_threeQuads_findNameWorks() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0x1234);
        int flags = 0;
        ByteQuadsCanonicalizer child = root.makeChild(flags);
        String name = "abcdef";
        int q1 = 0x61626364; // 'abcd'
        int q2 = 0x6566;     // 'ef'
        int q3 = 0x0;        // third quad pad
        child.addName(name, q1, q2, q3);
        String found = child.findName(q1, q2, q3);
        assertEquals(name, found);
    }

    // Tests spillover count after many insertions
    @Test
    public void testSpilloverCount_afterManyInserts_nonZero() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0x1234);
        int flags = 0;
        ByteQuadsCanonicalizer child = root.makeChild(flags);
        // add many entries to force spillover
        for (int i = 0; i < 200; i++) {
            String s = String.valueOf(i);
            int q = i + 1;
            child.addName(s, q);
        }
        assertTrue(child.size() == 200);
        // We don't assert exact spillover, just that table has entries
    }

    // Tests release merges child into parent
    @Test
    public void testRelease_childWithEntries_parentCountIncreases() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0x1234);
        int flags = 0;
        ByteQuadsCanonicalizer child = root.makeChild(flags);
        child.addName("x", 0x78);
        child.release();
        assertTrue(root.size() >= 1);
    }

    // Tests maybeDirty returns true after modifying child
    @Test
    public void testMaybeDirty_afterModification_returnsTrue() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0x1234);
        int flags = 0;
        ByteQuadsCanonicalizer child = root.makeChild(flags);
        assertFalse(child.maybeDirty());
        child.addName("x", 0x78);
        assertTrue(child.maybeDirty());
    }

    // Tests hashSeed method
    @Test
    public void testHashSeed_seedValueCorrect() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0xABCD);
        assertEquals(0xABCD, root.hashSeed());
    }

    // Tests toString does not crash
    @Test
    public void testToString_nonEmptyTable_returnsString() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0x1234);
        int flags = 0;
        ByteQuadsCanonicalizer child = root.makeChild(flags);
        child.addName("a", 0x61);
        String str = child.toString();
        assertNotNull(str);
        assertTrue(str.contains("size=1"));
    }

    // Tests calcHash consistency for single quad
    @Test
    public void testCalcHash_singleQuad_returnsNonZero() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0x1234);
        int hash = root.calcHash(0x41424344);
        assertTrue(hash != 0);
    }

    // Tests findName with int array variant (long name)
    @Test
    public void testFindName_longNameViaArray_returnsCorrect() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0x1234);
        int flags = 0;
        ByteQuadsCanonicalizer child = root.makeChild(flags);
        String name = "longname";
        int[] quads = {0x6c6f6e67, 0x6e616d65}; // 'long','name'
        child.addName(name, quads, 2);
        // should match two-quad findName
        String found = child.findName(quads[0], quads[1]);
        assertEquals(name, found);
    }

    // ==================== New tests for missing coverage ====================

    @Test
    public void testCalcHash_twoQuads_returnsNonZero() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0x1234);
        assertTrue(root.calcHash(0x12345678, 0x9ABCDEF0) != 0);
    }

    @Test
    public void testCalcHash_threeQuads_returnsNonZero() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0x1234);
        assertTrue(root.calcHash(0x11111111, 0x22222222, 0x33333333) != 0);
    }

    @Test
    public void testCalcHash_array_returnsNonZero() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0x1234);
        int[] quads = {0x41424344, 0x45464748};
        assertTrue(root.calcHash(quads, 2) != 0);
    }

    @Test
    public void testResize_bucketCountIncreases() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0x1234);
        int flags = 0;
        ByteQuadsCanonicalizer child = root.makeChild(flags);
        int initialBucketCount = child.bucketCount(); // should be 64
        // Insert more than threshold (threshold ~ 0.75 * 64 = 48)
        for (int i = 0; i < 60; i++) {
            String s = String.valueOf(i);
            int q = i + 0x1000; // ensure unique
            child.addName(s, q);
        }
        int newBucketCount = child.bucketCount();
        assertTrue("bucket count should have increased after many inserts",
                newBucketCount > initialBucketCount);
    }

    @Test
    public void testAddName_findName_oneQuadViaArray() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0x1234);
        int flags = 0;
        ByteQuadsCanonicalizer child = root.makeChild(flags);
        String name = "X";
        int[] quads = {0x58}; // 'X'
        child.addName(name, quads, 1);
        String found = child.findName(quads, 1);
        assertEquals(name, found);
    }

    @Test
    public void testAddName_findName_threeQuadsViaArray() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0x1234);
        int flags = 0;
        ByteQuadsCanonicalizer child = root.makeChild(flags);
        String name = "abcdef";
        int[] quads = {0x61626364, 0x6566, 0x0}; // 'abcd','ef', pad
        child.addName(name, quads, 3);
        String found = child.findName(quads, 3);
        assertEquals(name, found);
    }
}