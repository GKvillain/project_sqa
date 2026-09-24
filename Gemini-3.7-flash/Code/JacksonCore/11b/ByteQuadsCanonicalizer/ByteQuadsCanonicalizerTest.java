package com.fasterxml.jackson.core.sym;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ByteQuadsCanonicalizerTest {

    private ByteQuadsCanonicalizer root;

    @Before
    public void setUp() {
        root = ByteQuadsCanonicalizer.createRoot(12345);
    }

    // Tests initialization state of root canonicalizer
    @Test
    public void testCreateRoot_initialState_correctDefaults() {
        assertNotNull(root);
        assertEquals(0, root.size());
        assertEquals(64, root.bucketCount());
        assertEquals(12345, root.hashSeed());
        assertFalse(root.maybeDirty());
        assertEquals(0, root.primaryCount());
        assertEquals(0, root.secondaryCount());
        assertEquals(0, root.tertiaryCount());
        assertEquals(0, root.spilloverCount());
        assertEquals(0, root.totalCount());
    }

    // Tests adding and finding 1-quad symbols
    @Test
    public void testAddAndFindName_singleQuad_success() {
        ByteQuadsCanonicalizer child = root.makeChild(0);
        int q1 = 0x12345678;
        String name = "symbol1";

        String added = child.addName(name, q1);
        assertEquals(name, added);
        assertEquals(1, child.size());
        assertTrue(child.maybeDirty());

        String found = child.findName(q1);
        assertEquals(name, found);

        assertNull(child.findName(0x99999999));
    }

    // Tests adding and finding 2-quad symbols
    @Test
    public void testAddAndFindName_twoQuads_success() {
        ByteQuadsCanonicalizer child = root.makeChild(0);
        int q1 = 0x11111111;
        int q2 = 0x22222222;
        String name = "symbol2";

        String added = child.addName(name, q1, q2);
        assertEquals(name, added);
        assertEquals(1, child.size());

        String found = child.findName(q1, q2);
        assertEquals(name, found);

        assertNull(child.findName(q1, 0x33333333));
    }

    // Tests adding and finding 3-quad symbols
    @Test
    public void testAddAndFindName_threeQuads_success() {
        ByteQuadsCanonicalizer child = root.makeChild(0);
        int q1 = 0x11111111;
        int q2 = 0x22222222;
        int q3 = 0x33333333;
        String name = "symbol3";

        String added = child.addName(name, q1, q2, q3);
        assertEquals(name, added);
        assertEquals(1, child.size());

        String found = child.findName(q1, q2, q3);
        assertEquals(name, found);

        assertNull(child.findName(q1, q2, 0x44444444));
    }

    // Tests adding and finding symbols with 4 or more quads (long names)
    @Test
    public void testAddAndFindName_fourAndMoreQuads_success() {
        ByteQuadsCanonicalizer child = root.makeChild(0);
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

        int[] nonExistent = new int[]{1, 2, 3, 999};
        assertNull(child.findName(nonExistent, 4));
    }

    // Tests adding names using int array for lengths 1, 2, and 3
    @Test
    public void testAddName_arrayShortLengths_routedCorrectly() {
        ByteQuadsCanonicalizer child = root.makeChild(0);
        int[] quads = new int[]{10, 20, 30};

        child.addName("one", quads, 1);
        child.addName("two", quads, 2);
        child.addName("three", quads, 3);

        assertEquals("one", child.findName(10));
        assertEquals("two", child.findName(10, 20));
        assertEquals("three", child.findName(10, 20, 30));
        assertEquals(3, child.size());
    }

    // Tests child merging changes back to parent upon release
    @Test
    public void testRelease_dirtyChild_mergesStateToParent() {
        ByteQuadsCanonicalizer child = root.makeChild(0);
        child.addName("sym1", 100);
        child.addName("sym2", 200, 300);

        assertEquals(2, child.size());
        assertEquals(0, root.size());

        child.release();

        assertEquals(2, root.size());

        // A new child from root should now inherit merged symbols
        ByteQuadsCanonicalizer child2 = root.makeChild(0);
        assertEquals(2, child2.size());
        assertEquals("sym1", child2.findName(100));
        assertEquals("sym2", child2.findName(200, 300));
    }

    // Tests release without modification does not dirty or alter parent
    @Test
    public void testRelease_unmodifiedChild_doesNotChangeParent() {
        ByteQuadsCanonicalizer child = root.makeChild(0);
        assertFalse(child.maybeDirty());
        child.release();
        assertEquals(0, root.size());
    }

    // Tests symbol table rehashing and expansion under load
    @Test
    public void testRehash_addingManySymbols_expandsAndPreservesSymbols() {
        ByteQuadsCanonicalizer child = root.makeChild(0);
        int count = 120;

        for (int i = 0; i < count; ++i) {
            child.addName("name" + i, i + 1, i + 2);
        }

        assertEquals(count, child.size());
        assertTrue(child.bucketCount() > 64);

        for (int i = 0; i < count; ++i) {
            assertEquals("name" + i, child.findName(i + 1, i + 2));
        }

        child.release();
        assertEquals(count, root.size());
    }

    // Tests calcHash methods for 1, 2, 3, and array quads
    @Test
    public void testCalcHash_variousLengths_producesConsistentHash() {
        int h1 = root.calcHash(42);
        int h2 = root.calcHash(42, 84);
        int h3 = root.calcHash(42, 84, 126);
        int h4 = root.calcHash(new int[]{42, 84, 126, 168}, 4);

        assertEquals(h1, root.calcHash(42));
        assertEquals(h2, root.calcHash(42, 84));
        assertEquals(h3, root.calcHash(42, 84, 126));
        assertEquals(h4, root.calcHash(new int[]{42, 84, 126, 168}, 4));
    }

    // Tests exception when calcHash with array receives invalid length (< 4)
    @Test(expected = IllegalArgumentException.class)
    public void testCalcHash_invalidLengthLessThanFour_throwsIllegalArgumentException() {
        root.calcHash(new int[]{1, 2, 3}, 3);
    }

    // Tests tertiary shift calculation logic
    @Test
    public void testCalcTertiaryShift_variousSizes_returnsExpectedShift() {
        assertEquals(4, ByteQuadsCanonicalizer._calcTertiaryShift(64));
        assertEquals(4, ByteQuadsCanonicalizer._calcTertiaryShift(128));
        assertEquals(5, ByteQuadsCanonicalizer._calcTertiaryShift(512));
        assertEquals(6, ByteQuadsCanonicalizer._calcTertiaryShift(2048));
        assertEquals(7, ByteQuadsCanonicalizer._calcTertiaryShift(8192));
    }

    // Tests toString method contains class name and size info
    @Test
    public void testToString_validInstance_returnsInformativeString() {
        ByteQuadsCanonicalizer child = root.makeChild(0);
        child.addName("test", 123);
        String desc = child.toString();

        assertNotNull(desc);
        assertTrue(desc.contains("ByteQuadsCanonicalizer"));
        assertTrue(desc.contains("size=1"));
    }

    // Tests createRoot without seed generates a valid instance
    @Test
    public void testCreateRoot_randomSeed_createsValidInstance() {
        ByteQuadsCanonicalizer randomRoot = ByteQuadsCanonicalizer.createRoot();
        assertNotNull(randomRoot);
        assertTrue(randomRoot.hashSeed() != 0);
        assertEquals(0, randomRoot.size());
    }
}