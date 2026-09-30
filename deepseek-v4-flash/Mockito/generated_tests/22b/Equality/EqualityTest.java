package org.mockito.internal.matchers;

import org.junit.Test;
import static org.junit.Assert.*;

public class EqualityTest {

    @Test
    public void testAreEqual_bothNull_returnsTrue() {
        assertTrue(Equality.areEqual(null, null));
    }

    @Test
    public void testAreEqual_firstNullSecondNonNull_returnsFalse() {
        assertFalse(Equality.areEqual(null, "abc"));
    }

    @Test
    public void testAreEqual_firstNonNullSecondNull_returnsFalse() {
        assertFalse(Equality.areEqual("abc", null));
    }

    @Test
    public void testAreEqual_sameString_returnsTrue() {
        String s = "hello";
        assertTrue(Equality.areEqual(s, s));
    }

    @Test
    public void testAreEqual_differentStrings_returnsFalse() {
        assertFalse(Equality.areEqual("hello", "world"));
    }

    @Test
    public void testAreEqual_stringVsInteger_returnsFalse() {
        assertFalse(Equality.areEqual("1", 1));
    }

    @Test
    public void testAreEqual_arrayVsNonArray_returnsFalse() {
        assertFalse(Equality.areEqual(new int[]{1}, "not array"));
    }

    @Test
    public void testAreEqual_sameLengthSameElements_returnsTrue() {
        String[] a = {"a", "b"};
        String[] b = {"a", "b"};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test
    public void testAreEqual_differentLength_returnsFalse() {
        String[] a = {"a"};
        String[] b = {"a", "b"};
        assertFalse(Equality.areEqual(a, b));
    }

    @Test
    public void testAreEqual_sameLengthDifferentElements_returnsFalse() {
        String[] a = {"a", "b"};
        String[] b = {"a", "c"};
        assertFalse(Equality.areEqual(a, b));
    }

    @Test
    public void testAreEqual_emptyArrays_returnsTrue() {
        String[] a = {};
        String[] b = {};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test
    public void testAreEqual_intPrimitiveArraysEqual_returnsTrue() {
        int[] a = {1, 2, 3};
        int[] b = {1, 2, 3};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test
    public void testAreEqual_intPrimitiveArraysNotEqual_returnsFalse() {
        int[] a = {1, 2, 3};
        int[] b = {1, 2, 4};
        assertFalse(Equality.areEqual(a, b));
    }

    @Test
    public void testAreEqual_nestedArrayEqual_returnsTrue() {
        String[][] a = {{"x"}, {"y"}};
        String[][] b = {{"x"}, {"y"}};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test
    public void testAreEqual_nestedArrayNotEqual_returnsFalse() {
        String[][] a = {{"x"}, {"y"}};
        String[][] b = {{"x"}, {"z"}};
        assertFalse(Equality.areEqual(a, b));
    }

    @Test
    public void testAreEqual_arrayWithNullElements_equalNullElements_returnsTrue() {
        Object[] a = {null, "b"};
        Object[] b = {null, "b"};
        assertTrue(Equality.areEqual(a, b));
    }

    @Test
    public void testAreEqual_arrayWithNullElements_notEqualToNonNullElement_returnsFalse() {
        Object[] a = {null, "b"};
        Object[] b = {"a", "b"};
        assertFalse(Equality.areEqual(a, b));
    }

    @Test
    public void testIsArray_withString_returnsFalse() {
        assertFalse(Equality.isArray("string"));
    }

    @Test
    public void testIsArray_withIntArray_returnsTrue() {
        assertTrue(Equality.isArray(new int[]{1, 2}));
    }

    @Test
    public void testAreArrayLengthsEqual_equalLengths_returnsTrue() {
        int[] a = {1, 2};
        int[] b = {3, 4};
        assertTrue(Equality.areArrayLengthsEqual(a, b));
    }

    @Test
    public void testAreArrayLengthsEqual_differentLengths_returnsFalse() {
        int[] a = {1, 2};
        int[] b = {3};
        assertFalse(Equality.areArrayLengthsEqual(a, b));
    }

    @Test
    public void testAreArrayElementsEqual_equalElements_returnsTrue() {
        String[] a = {"a", "b"};
        String[] b = {"a", "b"};
        assertTrue(Equality.areArrayElementsEqual(a, b));
    }

    @Test
    public void testAreArrayElementsEqual_notEqualElements_returnsFalse() {
        String[] a = {"a", "b"};
        String[] b = {"a", "c"};
        assertFalse(Equality.areArrayElementsEqual(a, b));
    }
}