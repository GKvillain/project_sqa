package org.mockito.internal.matchers;

import org.junit.Test;
import static org.junit.Assert.*;

public class EqualityTest {

    // Helper class with throwing equals to test identity short-circuit
    private static class BadEquals {
        @Override
        public boolean equals(Object obj) {
            throw new RuntimeException("equals should not be called on same instance");
        }
    }

    // Tests constructor invocation
    @Test
    public void testEquality_constructor_canBeInstantiated() {
        Equality equality = new Equality();
        assertNotNull(equality);
    }

    // Tests both null arguments
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

    // Tests identical object references that throw exception on equals
    @Test
    public void testAreEqual_sameInstanceWithThrowingEquals_returnsTrue() {
        BadEquals bad = new BadEquals();
        assertTrue(Equality.areEqual(bad, bad));
    }

    // Tests equal standard objects
    @Test
    public void testAreEqual_equalStrings_returnsTrue() {
        assertTrue(Equality.areEqual(new String("hello"), new String("hello")));
    }

    // Tests non-equal standard objects
    @Test
    public void testAreEqual_differentStrings_returnsFalse() {
        assertFalse(Equality.areEqual("hello", "world"));
    }

    // Tests equal primitive int arrays
    @Test
    public void testAreEqual_equalPrimitiveIntArrays_returnsTrue() {
        int[] arr1 = new int[]{1, 2, 3};
        int[] arr2 = new int[]{1, 2, 3};
        assertTrue(Equality.areEqual(arr1, arr2));
    }

    // Tests primitive arrays with different lengths
    @Test
    public void testAreEqual_differentLengthPrimitiveArrays_returnsFalse() {
        int[] arr1 = new int[]{1, 2};
        int[] arr2 = new int[]{1, 2, 3};
        assertFalse(Equality.areEqual(arr1, arr2));
    }

    // Tests primitive arrays with same length but different elements
    @Test
    public void testAreEqual_differentElementsPrimitiveArrays_returnsFalse() {
        int[] arr1 = new int[]{1, 2, 3};
        int[] arr2 = new int[]{1, 2, 4};
        assertFalse(Equality.areEqual(arr1, arr2));
    }

    // Tests first is array and second is non-array
    @Test
    public void testAreEqual_firstArraySecondNonArray_returnsFalse() {
        assertFalse(Equality.areEqual(new int[]{1}, "not an array"));
    }

    // Tests first is non-array and second is array
    @Test
    public void testAreEqual_firstNonArraySecondArray_returnsFalse() {
        assertFalse(Equality.areEqual("not an array", new int[]{1}));
    }

    // Tests equal Object arrays
    @Test
    public void testAreEqual_equalObjectArrays_returnsTrue() {
        Object[] arr1 = new Object[]{"a", "b"};
        Object[] arr2 = new Object[]{"a", "b"};
        assertTrue(Equality.areEqual(arr1, arr2));
    }

    // Tests nested multi-dimensional arrays
    @Test
    public void testAreEqual_nestedMultiDimensionalArrays_returnsTrue() {
        int[][] arr1 = new int[][]{{1, 2}, {3, 4}};
        int[][] arr2 = new int[][]{{1, 2}, {3, 4}};
        assertTrue(Equality.areEqual(arr1, arr2));
    }

    // Tests empty arrays of same type
    @Test
    public void testAreEqual_emptyArrays_returnsTrue() {
        assertTrue(Equality.areEqual(new int[0], new int[0]));
    }

    // Tests isArray method directly for array and non-array
    @Test
    public void testIsArray_arrayAndNonArray_returnsExpected() {
        assertTrue(Equality.isArray(new int[]{1}));
        assertFalse(Equality.isArray("string"));
    }
}