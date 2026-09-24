package com.fasterxml.jackson.core.sym;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonFactory;

public class ByteQuadsCanonicalizerTest {

    private ByteQuadsCanonicalizer root;

    @Before
    public void setUp() {
        root = ByteQuadsCanonicalizer.createRoot(123);
    }

    // Tests initialization of root canonicalizer
    @Test
    public void testCreateRoot_initialState_correctDefaults() {
        assertNotNull(root);
        assertEquals(0, root.size());
        assertEquals(123, root.hashSeed());
        assertEquals(64, root.bucketCount());
        assertFalse(root.maybeDirty());
        assertEquals(0, root.primaryCount());
        assertEquals(0, root.secondaryCount());
        assertEquals(0, root.tertiaryCount());
        assertEquals(0, root.spilloverCount());
        assertEquals(0, root.totalCount());
    }

    // Tests random seed root factory method
    @Test
    public void testCreateRoot_randomSeed_nonZeroSeed() {
        ByteQuadsCanonicalizer r = ByteQuadsCanonicalizer.createRoot();
        assertNotNull(r);
        assertTrue((r.hashSeed() & 1) != 0);
    }

    // Tests adding and finding 1-quad name in child
    @Test
    public void testAddAndFindName_oneQuad_returnsCorrectSymbol() {
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.collectDefaults());
        int q1 = 0x12345678;
        String name = "field1";

        assertNull(child.findName(q1));
        String added = child.addName(name, q1);
        assertEquals(name, added);

        assertEquals(1, child.size());
        assertTrue(child.maybeDirty());
        assertEquals(name, child.findName(q1));
        assertNull(child.findName(0x99999999));
    }

    // Tests adding and finding 2-quads name in child
    @Test
    public void testAddAndFindName_twoQuads_returnsCorrectSymbol() {
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.collectDefaults());
        int q1 = 0x12345678;
        int q2 = 0x23456789;
        String name = "field12";

        assertNull(child.findName(q1, q2));
        String added = child.addName(name, q1, q2);
        assertEquals(name, added);

        assertEquals(1, child.size());
        assertEquals(name, child.findName(q1, q2));
        assertNull(child.findName(q1, 0x0));
        assertNull(child.findName(0x11111111, q2));
    }

    // Tests adding 2-quads name when second quad is zero
    @Test
    public void testAddName_twoQuadsWithSecondZero_handledAsOneQuadHash() {
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.collectDefaults());
        int q1 = 0x12345678;
        String name = "singleViaTwo";

        String added = child.addName(name, q1, 0);
        assertEquals(name, added);
        assertEquals(name, child.findName(q1, 0));
    }

    // Tests adding and finding 3-quads name in child
    @Test
    public void testAddAndFindName_threeQuads_returnsCorrectSymbol() {
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.collectDefaults());
        int q1 = 0x12345678;
        int q2 = 0x23456789;
        int q3 = 0x3456789A;
        String name = "field123";

        assertNull(child.findName(q1, q2, q3));
        String added = child.addName(name, q1, q2, q3);
        assertEquals(name, added);

        assertEquals(1, child.size());
        assertEquals(name, child.findName(q1, q2, q3));
        assertNull(child.findName(q1, q2, 0x11111111));
    }

    // Tests adding and finding long names using quad array with lengths 4, 5, 8, 9
    @Test
    public void testAddAndFindName_multiQuadsArray_returnsCorrectSymbol() {
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.collectDefaults());

        int[] q4 = new int[]{1, 2, 3, 4};
        int[] q5 = new int[]{1, 2, 3, 4, 5};
        int[] q8 = new int[]{1, 2, 3, 4, 5, 6, 7, 8};
        int[] q9 = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};

        child.addName("name4", q4, 4);
        child.addName("name5", q5, 5);
        child.addName("name8", q8, 8);
        child.addName("name9", q9, 9);

        assertEquals("name4", child.findName(q4, 4));
        assertEquals("name5", child.findName(q5, 5));
        assertEquals("name8", child.findName(q8, 8));
        assertEquals("name9", child.findName(q9, 9));

        int[] mismatch = new int[]{1, 2, 3, 99};
        assertNull(child.findName(mismatch, 4));
    }

    // Tests adding array of length 1, 2, 3 delegated to respective logic
    @Test
    public void testAddAndFindName_arrayWithShortLength_delegatesCorrectly() {
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.collectDefaults());
        int[] q = new int[]{10, 20, 30};

        child.addName("s1", q, 1);
        child.addName("s2", q, 2);
        child.addName("s3", q, 3);

        assertEquals("s1", child.findName(q, 1));
        assertEquals("s2", child.findName(q, 2));
        assertEquals("s3", child.findName(q, 3));
    }

    // Tests calcHash with invalid qlen (< 4) throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCalcHash_arrayLengthLessThanFour_throwsException() {
        root.calcHash(new int[]{1, 2, 3}, 3);
    }

    // Tests release and merge back into root
    @Test
    public void testRelease_dirtyChild_mergesIntoRoot() {
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.collectDefaults());
        child.addName("key1", 100);
        child.addName("key2", 200, 300);
        assertEquals(2, child.size());
        assertEquals(0, root.size());

        child.release();

        assertEquals(2, root.size());
        assertFalse(child.maybeDirty());

        // A new child should inherit the merged entries
        ByteQuadsCanonicalizer child2 = root.makeChild(JsonFactory.Feature.collectDefaults());
        assertEquals(2, child2.size());
        assertEquals("key1", child2.findName(100));
        assertEquals("key2", child2.findName(200, 300));
    }

    // Tests release when child has no new entries does not change root
    @Test
    public void testRelease_unmodifiedChild_doesNotAffectRoot() {
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.collectDefaults());
        child.release();
        assertEquals(0, root.size());
    }

    // Tests table expansion and rehashing when filling past capacity
    @Test
    public void testRehash_addManyEntries_expandsAndPreservesAll() {
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.collectDefaults());
        int initialBuckets = child.bucketCount();

        int count = 100;
        for (int i = 0; i < count; ++i) {
            child.addName("name" + i, i * 7 + 1, i * 13 + 2);
        }

        assertEquals(count, child.size());
        assertTrue(child.bucketCount() > initialBuckets);

        for (int i = 0; i < count; ++i) {
            assertEquals("name" + i, child.findName(i * 7 + 1, i * 13 + 2));
        }

        child.release();
        assertEquals(count, root.size());
    }

    // Tests calcTertiaryShift boundaries
    @Test
    public void testCalcTertiaryShift_variousSlotSizes_correctShift() {
        assertEquals(4, ByteQuadsCanonicalizer._calcTertiaryShift(64));
        assertEquals(4, ByteQuadsCanonicalizer._calcTertiaryShift(128));
        assertEquals(5, ByteQuadsCanonicalizer._calcTertiaryShift(512));
        assertEquals(6, ByteQuadsCanonicalizer._calcTertiaryShift(2048));
        assertEquals(7, ByteQuadsCanonicalizer._calcTertiaryShift(8192));
    }

    // Tests toString format contains size and count info
    @Test
    public void testToString_returnsInformativeString() {
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.collectDefaults());
        child.addName("foo", 123);
        String desc = child.toString();
        assertNotNull(desc);
        assertTrue(desc.contains("size=1"));
        assertTrue(desc.contains("pri/sec/ter/spill"));
    }

    // Tests secondary and tertiary collision handling
    @Test
    public void testCollisionHandling_multipleCollidingKeys_allFound() {
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.collectDefaults());

        // Add multiple names that map to same primary offset
        for (int i = 0; i < 20; i++) {
            child.addName("col" + i, i + 1);
        }

        for (int i = 0; i < 20; i++) {
            assertEquals("col" + i, child.findName(i + 1));
        }
        assertTrue(child.totalCount() >= 20);
    }
}