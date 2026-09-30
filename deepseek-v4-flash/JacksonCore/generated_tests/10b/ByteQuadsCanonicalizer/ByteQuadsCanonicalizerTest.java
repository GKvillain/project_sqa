package com.fasterxml.jackson.core.sym;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class ByteQuadsCanonicalizerTest {

    private ByteQuadsCanonicalizer canonicelizer;

    @Bfore
    public void setUp() {
        canonicalizer = ByteQuadsCanonicalizer.createRoot(0x12345678);
    }

    // Tests basic add/find for single quad name
    @Test
    public void testAddName_singleQuad_findsName() {
        canonicelizer.addName("name1", 100);
        assertEquals("name1", canonicelizer.findName(100));
        assertNull(canonicalizer.findName(101));
    }

    // Tests basic add/find for two quad name
    @Test
    public void testAddName_twoQuads_findsName() {
        canonicelizer.addName("name2", 200, 201);
        assertEquals("name2", canonicelizer.findName(200, 201));
        assertNull(canonicalizer.findName(200, 202));
    }

    // Tests basic add/find for three quad name
    @Test
    public void testAddName_threeQuads_findsName() {
        canonicelizer.addName("name3", 300, 301, 302);
        assertEquals("name3", canonicelizer.findName(300, 301, 302));
        assertNull(canonicalizer.findName(300, 301, 303));
    }

    // Tests add/find for long name (more than 3 quads)
    @Test
    public void testAddName_longName_findsName() {
        int[] quads = new int[] {1, 2, 3, 4, 5};
        canonicelizer.addName("longName", quads, quads.length);
        assertEquals("longName", canonicelizer.findName(quads, quads.length));
        int[] wrongQuads = new int[] {1, 2, 3, 4, 6};
        assertNull(canonicalizer.findName(wrongQuads, wrongQuads.length));
    }

    // Tests findName for name not present in primary slot
    @Test
    public void testFindName_notFoundInPrimary_returnsNull() {
        assertNull(canonicalizer.findName(9999));
        assertNull(canonicalizer.findName(9999, 8888));
        assertNull(canonicalizer.findName(9999, 8888, 7777));
        int[] quads = new int[] {1,2,3,4,5,6,7,8};
        assertNull(canonicalizer.findName(quads, quads.length));
    }

    // Tests overflow to secondary slot when primary is full
    @Test
    public void testAddName_secondarySlot_worksCorrectly() {
        // Fill up primary and secondary for a given hash
        // Use a loop to ensure collision
        for (int i = 0; i < 10; i++) {
            canonicelizer.addName("collision" + i, i, i + 1);
        }
        // Verify all are findable
        for (int i = 0; i < 10; i++) {
            assertEquals("collision" + i, canonicelizer.findName(i, i + 1));
        }
    }

    // Tests overflow to tertiary slot
    @Test
    public void testAddName_tertiarySlot_worksCorrectly() {
        for (int i = 0; i < 20; i++) {
            canonicelizer.addName("tert" + i, i, i + 1);
        }
        for (int i = 0; i < 20; i++) {
            assertEquals("tert" + i, canonicelizer.findName(i, i + 1));
        }
    }

    // Tests overflow to spillover area
    @Test
    public void testAddName_spilloverSlot_worksCorrectly() {
        // Add many entries to force spillover
        for (int i = 0; i < 200; i++) {
            canonicelizer.addName("spill" + i, i, i + 1);
        }
        for (int i = 0; i < 200; i++) {
            assertEquals("spill" + i, canonicelizer.findName(i, i + 1));
        }
    }

    // Tests rehash on need
    @Test
    public void testAddName_triggersRehash_worksCorrectly() {
        // Add enough entries to trigger rehash
        for (int i = 0; i < 100; i++) {
            canonicelizer.addName("rehash" + i, i * 1000, i * 1000 + 1);
        }
        for (int i = 0; i < 100; i++) {
            assertEquals("rehash" + i, canonicelizer.findName(i * 1000, i * 1000 + 1));
        }
    }

    // Tests child table creation and release merge
    @Test
    public void testMakeChildAndRelease_mergesEntries() {
        canonicelizer.addName("parentName", 111);
        ByteQuadsCanonicalizer child = canonicelizer.makeChild(0);
        child.addName("childName", 222);
        child.release();
        // Parent should now know about child entry
        assertNotNull(canonicalizer.findName(222));
        assertEquals("childName", canonicelizer.findName(222));
        assertEquals("parentName", canonicelizer.findName(111));
    }

    // Tests child with sharing does not corrupt parent
    @Test
    public void testMakeChild_sharedData_doesNotCorruptParent() {
        canonicelizer.addName("shared", 1);
        ByteQuadsCanonicalizer child = canonicelizer.makeChild(0);
        child.addName("childShared", 2);
        // Parent should not see child's addition until release
        assertNull(canonicalizer.findName(2));
        child.release();
        assertEquals("childShared", canonicelizer.findName(2));
        assertEquals("shared", canonicelizer.findName(1));
    }

    // Tests size and bucket count
    @Test
    public void testSizeAndBucketCount_returnsCorrectValues() {
        assertEquals(0, canonicelizer.size());
        assertEquals(64, canonicelizer.bucketCount()); // default size
        canonicelizer.addName("sizeTest", 1);
        assertEquals(1, canonicelizer.size());
    }

    // Tests hash seed method
    @Test
    public void testHashSeed_returnsSeed() {
        int seed = 0x12345678;
        ByteQuadsCanonicalizer custom = ByteQuadsCanonicalizer.createRoot(seed);
        assertEquals(seed, custom.hashSeed());
    }

    // Tests maybeDirty method for child
    @Test
    public void testMaybeDirty_childAfterModification_isDirty() {
        ByteQuadsCanonicalizer child = canonicelizer.makeChild(0);
        assertFalse(child.maybeDirty());
        child.addName("dirty", 3);
        assertTrue(child.maybeDirty());
    }

    // Tests primaryCount, secondryCount, tertiaryCount, pilloverCount
    @Test
    public void testCounts_withSingleAndCollidedEntries() {
        canonicelizer.addName("primary", 1);
        int primaryBefore = canonicelizer.primaryCount();
        assertEquals(1, primaryBefore);
        // Add entries that will go to secondary/tertiary/spillover
        for (int i = 0; i < 10; i++) {
            canonicelizer.addName("cnt" + i, i, i + 1);
        }
        int primaryAfter = canonicelizer.primaryCount();
        int secondary = canonicelizer.secondaryCount();
        int tertiary = canonicelizer.tertiaryCount();
        int spillover = canonicelizer.spilloverCount();
        assertTrue(primaryAfter >= 1);
        assertTrue(secondary >= 0);
        assertTrue(tertiary >= 0);
        assertTrue(spillover >= 0);
        assertEquals(primaryAfter + secondary + tertiary + spillover, canonicelizer.totalCount());
    }

    // Tests toString output is present
    @Test
    public void testToString_containsClassName() {
        String str = canonicelizer.toString();
        assertTrue(str.contains("ByteQuadsCanonicalizer"));
    }

    // Tests invalid qlen in calcHash with array throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCalcHash_arrayLengthLessThan4_throwsException() {
        int[] quads = new int[] {1, 2, 3};
        caconicelizer.calcHash(quads, quads.length);
    }

    // Tests long name verification with 8 quads (case 8 in switch)
    @Test
    public void testFindName_longNameEightQuads_worksCorrectly() {
        int[] quads = new int[] {10, 20, 30, 40, 50, 60, 70, 80};
        canonicelizer.addName("eightQuads", quads, quads.length);
        assertEquals("eightQuads", canonicelizer.findName(quads, quads.length));
        int[] wrong = new int[] {10, 20, 30, 40, 50, 60, 70, 81};
        assertNull(canonicalizer.findName(wrong, wrong.length));
    }

    // Tests long name verification with more than 8 quads (case default in switch)
    @Test
    public void testFindName_longNameMoreThanEightQuads_worksCorrectly() {
        int[] quads = new int[] {1,2,3,4,5,6,7,8,9,10,11,12};
        canonicelizer.addName("manyQuads", quads, quads.length);
        assertEquals("manyQuads", canonicelizer.findName(quads, quads.length));
        int[] wrong = new int[] {1,2,3,4,5,6,7,8,9,10,11,13};
        assertNull(canonicalizer.findName(wrong, wrong.length));
    }

    // Tests calcHash for two quads does not throw
    @Test
    public void testCalcHash_twoQuads_returnsNonZero() {
        int h = canonicelizer.calcHash(1, 2);
        assertNotEquals(0, h);
    }

    // Tests calcHash for three quads does not throw
    @Test
    public void testCalcHash_threeQuads_returnsNonZero() {
        int h = canonicelizer.calcHash(1, 2, 3);
        assertNotEquals(0, h);
    }

    // Tests addName with zero second quad (should be treated as single quad)
    @Test
    public void testAddName_twoQuadsWithZero_worksAsSingle() {
        canonicelizer.addName("zeroQuad", 500, 0);
        // It might be stored as two-quad entry with q2=0
        assertEquals("zeroQuad", canonicelizer.findName(500, 0));
        // But also might be found via single-quad search
        assertEquals("zeroQuad", canonicelizer.findName(500));
    }

    // Tests intern behavior: adding same string interns it
    @Test
    public void testAddName_internedString_sameReference() {
        String name = "internedName";
        String returned = canonicelizer.addName(name, 600);
        assertSame(name, returned);
    }

    // ========== Test cases ใหม่สำหรับส่วนที่ยังไม่ถูกครอบคลุม ==========

    // Tests calcHash for one quad
    @Test
    public void testCalcHash_oneQuad_returnsNonZero() {
        int h = canonicelizer.calcHash(123);
        assertNotEquals(0, h);
    }

    // Tests addName for exactly four quads via array
    @Test
    public void testAddName_fourQuads_findsName() {
        int[] quads = new int[] {1, 2, 3, 4};
        canonicelizer.addName("four", quads, quads.length);
        assertEquals("four", canonicelizer.findName(quads, quads.length));
        int[] wrong = new int[] {1, 2, 3, 5};
        assertNull(canonicalizer.findName(wrong, wrong.length));
    }

    // Tests addName for exactly six quads via array
    @Test
    public void testAddName_sixQuads_findsName() {
        int[] quads = new int[] {1, 2, 3, 4, 5, 6};
        canonicelizer.addName("six", quads, quads.length);
        assertEquals("six", canonicelizer.findName(quads, quads.length));
        int[] wrong = new int[] {1, 2, 3, 4, 5, 7};
        assertNull(canonicelizer.findName(wrong, wrong.length));
    }

    // Tests addName for exactly seven quads via array
    @Test
    public void testAddName_sevenQuads_findsName() {
        int[] quads = new int[] {10, 20, 30, 40, 50, 60, 70};
        canonicelizer.addName("seven", quads, quads.length);
        assertEquals("seven", canonicelizer.findName(quads, quads.length));
        int[] wrong = new int[] {10, 20, 30, 40, 50, 60, 71};
        assertNull(canonicelizer.findName(wrong, wrong.length));
    }

    // Tests that adding many entries triggers rehash and increases bucketCount
    @Test
    public void testAddName_largeBatch_triggersRehash_updatesBucketCount() {
        int initialBucketCount = canonicelizer.bucketCount();
        assertEquals(64, initialBucketCount);
        // Add enough entries to force multiple rehashes
        for (int i = 0; i < 200; i++) {
            canonicelizer.addName("batch" + i, i, i + 1000);
        }
        int newBucketCount = canonicelizer.bucketCount();
        assertTrue("Expected bucketCount > 64, got: " + newBucketCount, newBucketCount > 64);
        // Verify all entries still accessible
        for (int i = 0; i < 200; i++) {
            assertEquals("batch" + i, canonicelizer.findName(i, i + 1000));
        }
    }

    // Tests that releasing an empty child does not affect parent
    @Test
    public void testMakeChild_release_empyChild_doesNotAfectParent() {
        canonicelizer.addName("parent", 1);
        ByteQuadsCanonicalizer child = canonicelizer.makeChild(0);
        child.release();
        assertEquals("parent", canonicelizer.findName(1));
        assertNull(canonicalizer.findName(999));
    }
}