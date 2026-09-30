package org.mockito.internal.matchers;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EqualityTest {

    private static class BadEquals {
        @Override
        public boolean equals(Object obj) {
            throw new RuntimeException("Bad equals method");
        }
    }

    // Tests both arguments are null
    @Test
    public void testAreEqual_bothNull_returnsTrue() {
        assertTrue(Equality.areEqual(null, null));
    }

    // Tests first argument null and second non-null
    @Test
    public void testAreEqual_firstNullSecondNonNull_returnsFalse() {
        assertFalse(Equality.areEqual(null, "test"));
    }

    // Tests first argument non-null and second null
    @Test
    public void testAreEqual_firstNonNullSecondNull_returnsFalse() {
        assertFalse(Equality.areEqual("test", null));
    }

    // Tests two equal non-array objects
    @Test
    public void testAreEqual_equalObjects_returnsTrue() {
        assertTrue(Equality.areEqual("hello", "hello"));
    }

    // Tests two different non-array objects
    @Test
    public void testAreEqual_differentObjects_returnsFalse() {
        assertFalse(Equality.areEqual("hello", "world"));
    }

    // Tests same instance with an equals method that throws exception
    @Test
    public void testAreEqual_sameInstanceThrowingEquals_returnsTrue() {
        BadEquals bad = new BadEquals();
        assertTrue(Equality.areEqual(bad, bad));
    }

    // Tests equal primitive arrays
    @Test
    public void testAreEqual_equalPrimitiveArrays_returnsTrue() {
        assertTrue(Equality.areEqual(new int[]{1, 2, 3}, new int[]{1, 2, 3}));
    }

    // Tests primitive arrays with different lengths
    @Test
    public void testAreEqual_differentLengthPrimitiveArrays_returnsFalse() {
        assertFalse(Equality.areEqual(new int[]{1, 2}, new int[]{1, 2, 3}));
    }

    // Tests primitive arrays with same length but different elements
    @Test
    public void testAreEqual_differentContentPrimitiveArrays_returnsFalse() {
        assertFalse(Equality.areEqual(new int[]{1, 2, 3}, new int[]{1, 2, 4}));
    }

    // Tests equal object arrays
    @Test
    public void testAreEqual_equalObjectArrays_returnsTrue() {
        assertTrue(Equality.areEqual(new String[]{"a", "b"}, new String[]{"a", "b"}));
    }

    // Tests object arrays with different elements
    @Test
    public void testAreEqual_differentContentObjectArrays_returnsFalse() {
        assertFalse(Equality.areEqual(new String[]{"a", "b"}, new String[]{"a", "c"}));
    }

    // Tests equal multidimensional arrays
    @Test
    public void testAreEqual_equalMultidimensionalArrays_returnsTrue() {
        assertTrue(Equality.areEqual(new int[][]{{1, 2}, {3, 4}}, new int[][]{{1, 2}, {3, 4}}));
    }

    // Tests different multidimensional arrays
    @Test
    public void testAreEqual_differentMultidimensionalArrays_returnsFalse() {
        assertFalse(Equality.areEqual(new int[][]{{1, 2}}, new int[][]{{1, 3}}));
    }

    // Tests first argument array and second not array
    @Test
    public void testAreEqual_firstArraySecondNotArray_returnsFalse() {
        assertFalse(Equality.areEqual(new int[]{1, 2}, "not an array"));
    }

    // Tests first argument not array and second array
    @Test
    public void testAreEqual_firstNotArraySecondArray_returnsFalse() {
        assertFalse(Equality.areEqual("not an array", new int[]{1, 2}));
    }

    // Tests both empty arrays
    @Test
    public void testAreEqual_bothEmptyArrays_returnsTrue() {
        assertTrue(Equality.areEqual(new int[]{}, new int[]{}));
    }
}