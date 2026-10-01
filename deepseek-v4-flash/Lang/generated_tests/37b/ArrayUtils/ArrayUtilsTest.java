package org.apache.commons.lang3;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.Map;

public class ArrayUtilsTest {

    // Tests toMap with null input
    @Test
    public void testToMap_nullInput_returnsNull() {
        assertNull(ArrayUtils.toMap(null));
    }

    // Tests toMap with valid array of arrays
    @Test
    public void testToMap_validArrayOfArrays_returnsMap() {
        String[][] input = {{"key1", "value1"}, {"key2", "value2"}};
        Map<Object, Object> result = ArrayUtils.toMap(input);
        assertNotNull(result);
        assertEquals("value1", result.get("key1"));
        assertEquals("value2", result.get("key2"));
        assertEquals(2, result.size());
    }

    // Tests toMap with array containing Map.Entry
    @Test
    public void testToMap_arrayWithEntry_returnsMap() {
        Map.Entry<Object, Object> entry = new java.util.AbstractMap.SimpleEntry<>("key", "value");
        Object[] input = new Object[]{entry};
        Map<Object, Object> result = ArrayUtils.toMap(input);
        assertNotNull(result);
        assertEquals("value", result.get("key"));
        assertEquals(1, result.size());
    }

    // Tests toMap with array element of invalid type
    @Test(expected = IllegalArgumentException.class)
    public void testToMap_invalidElementType_throwsIllegalArgumentException() {
        Object[] input = {new Object()};
        ArrayUtils.toMap(input);
    }

    // Tests toMap with array element being an array with length < 2
    @Test(expected = IllegalArgumentException.class)
    public void testToMap_arrayElementTooShort_throwsIllegalArgumentException() {
        Object[] input = {new String[]{"only"}};
        ArrayUtils.toMap(input);
    }

    // Tests clone with null T[] input
    @Test
    public void testClone_TArray_nullInput_returnsNull() {
        assertNull(ArrayUtils.clone((String[]) null));
    }

    // Tests clone with non-null T[] input
    @Test
    public void testClone_TArray_validInput_returnsClonedArray() {
        String[] original = {"a", "b"};
        String[] cloned = ArrayUtils.clone(original);
        assertNotNull(cloned);
        assertNotSame(original, cloned);
        assertArrayEquals(original, cloned);
    }

    // Tests subarray with null T[] input
    @Test
    public void testSubarray_TArray_nullInput_returnsNull() {
        assertNull(ArrayUtils.subarray((String[]) null, 0, 1));
    }

    // Tests subarray with valid parameters
    @Test
    public void testSubarray_TArray_validInput_returnsSubarray() {
        String[] input = {"a", "b", "c", "d"};
        String[] result = ArrayUtils.subarray(input, 1, 3);
        assertArrayEquals(new String[]{"b", "c"}, result);
    }

    // Tests subarray with start index less than 0
    @Test
    public void testSubarray_TArray_startIndexNegative_returnsSubarrayFromZero() {
        String[] input = {"a", "b", "c"};
        String[] result = ArrayUtils.subarray(input, -1, 2);
        assertArrayEquals(new String[]{"a", "b"}, result);
    }

    // Tests subarray with end index greater than array length
    @Test
    public void testSubarray_TArray_endIndexExceedsLength_returnsSubarrayToEnd() {
        String[] input = {"a", "b"};
        String[] result = ArrayUtils.subarray(input, 1, 10);
        assertArrayEquals(new String[]{"b"}, result);
    }

    // Tests subarray when newSize <= 0, returns empty array
    @Test
    public void testSubarray_TArray_newSizeZero_returnsEmptyArray() {
        String[] input = {"a", "b", "c"};
        String[] result = ArrayUtils.subarray(input, 2, 2);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    // Tests isSameLength with both null
    @Test
    public void testIsSameLength_ObjectArray_bothNull_returnsTrue() {
        assertTrue(ArrayUtils.isSameLength((Object[]) null, (Object[]) null));
    }

    // Tests isSameLength with one null and other non-null empty
    @Test
    public void testIsSameLength_ObjectArray_oneNullOtherEmpty_returnsTrue() {
        assertTrue(ArrayUtils.isSameLength(null, new Object[0]));
    }

    // Tests isSameLength with different lengths
    @Test
    public void testIsSameLength_ObjectArray_differentLengths_returnsFalse() {
        assertFalse(ArrayUtils.isSameLength(new Object[1], new Object[2]));
    }

    // Tests isEmpty with null array
    @Test
    public void testIsEmpty_TArray_nullInput_returnsTrue() {
        assertTrue(ArrayUtils.isEmpty((String[]) null));
    }

    // Tests isEmpty with empty array
    @Test
    public void testIsEmpty_TArray_emptyArray_returnsTrue() {
        assertTrue(ArrayUtils.isEmpty(new String[0]));
    }

    // Tests isEmpty with non-empty array
    @Test
    public void testIsEmpty_TArray_nonEmptyArray_returnsFalse() {
        assertFalse(ArrayUtils.isEmpty(new String[]{"a"}));
    }

    // Tests addAll with two non-null arrays
    @Test
    public void testAddAll_TArray_bothNonNull_returnsJoinedArray() {
        String[] array1 = {"a", "b"};
        String[] array2 = {"c", "d"};
        String[] result = ArrayUtils.addAll(array1, array2);
        assertArrayEquals(new String[]{"a", "b", "c", "d"}, result);
    }

    // Tests addAll with first array null
    @Test
    public void testAddAll_TArray_firstNull_returnsCloneOfSecond() {
        String[] array2 = {"x", "y"};
        String[] result = ArrayUtils.addAll(null, array2);
        assertArrayEquals(array2, result);
        assertNotSame(array2, result);
    }

    // Tests addAll with second array null
    @Test
    public void testAddAll_TArray_secondNull_returnsCloneOfFirst() {
        String[] array1 = {"p", "q"};
        String[] result = ArrayUtils.addAll(array1, (String[]) null);
        assertArrayEquals(array1, result);
        assertNotSame(array1, result);
    }

    // Tests add with element to non-null array
    @Test
    public void testAdd_TArray_validInput_returnsArrayWithElementAdded() {
        String[] input = {"a"};
        String[] result = ArrayUtils.add(input, "b");
        assertArrayEquals(new String[]{"a", "b"}, result);
    }

    // Tests add with null array
    @Test
    public void testAdd_TArray_nullArray_returnsSingleElementArray() {
        String[] result = ArrayUtils.add(null, "test");
        assertArrayEquals(new String[]{"test"}, result);
    }

    // Tests remove by valid index
    @Test
    public void testRemove_TArray_validIndex_returnsArrayWithoutElement() {
        String[] input = {"a", "b", "c"};
        String[] result = ArrayUtils.remove(input, 1);
        assertArrayEquals(new String[]{"a", "c"}, result);
    }

    // Tests remove by index out of bounds
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemove_TArray_indexOutOfBounds_throwsException() {
        ArrayUtils.remove(new String[]{"a"}, 5);
    }

    // Tests removeElement with element present
    @Test
    public void testRemoveElement_TArray_elementPresent_returnsArrayWithoutFirstOccurrence() {
        String[] input = {"a", "b", "a"};
        String[] result = ArrayUtils.removeElement(input, "a");
        assertArrayEquals(new String[]{"b", "a"}, result);
    }

    // Tests removeElement with element absent
    @Test
    public void testRemoveElement_TArray_elementAbsent_returnsClonedArray() {
        String[] input = {"a", "b"};
        String[] result = ArrayUtils.removeElement(input, "c");
        assertArrayEquals(input, result);
        assertNotSame(input, result);
    }

    // ====================== NEW TEST CASES ======================

    // isEmpty for primitive arrays

    // int[]
    @Test
    public void testIsEmpty_intArray_null() {
        assertTrue(ArrayUtils.isEmpty((int[]) null));
    }

    @Test
    public void testIsEmpty_intArray_empty() {
        assertTrue(ArrayUtils.isEmpty(new int[0]));
    }

    @Test
    public void testIsEmpty_intArray_nonEmpty() {
        assertFalse(ArrayUtils.isEmpty(new int[]{1, 2}));
    }

    // long[]
    @Test
    public void testIsEmpty_longArray_null() {
        assertTrue(ArrayUtils.isEmpty((long[]) null));
    }

    @Test
    public void testIsEmpty_longArray_empty() {
        assertTrue(ArrayUtils.isEmpty(new long[0]));
    }

    @Test
    public void testIsEmpty_longArray_nonEmpty() {
        assertFalse(ArrayUtils.isEmpty(new long[]{1L, 2L}));
    }

    // boolean[]
    @Test
    public void testIsEmpty_booleanArray_null() {
        assertTrue(ArrayUtils.isEmpty((boolean[]) null));
    }

    @Test
    public void testIsEmpty_booleanArray_empty() {
        assertTrue(ArrayUtils.isEmpty(new boolean[0]));
    }

    @Test
    public void testIsEmpty_booleanArray_nonEmpty() {
        assertFalse(ArrayUtils.isEmpty(new boolean[]{true}));
    }

    // byte[]
    @Test
    public void testIsEmpty_byteArray_null() {
        assertTrue(ArrayUtils.isEmpty((byte[]) null));
    }

    @Test
    public void testIsEmpty_byteArray_empty() {
        assertTrue(ArrayUtils.isEmpty(new byte[0]));
    }

    @Test
    public void testIsEmpty_byteArray_nonEmpty() {
        assertFalse(ArrayUtils.isEmpty(new byte[]{1}));
    }

    // char[]
    @Test
    public void testIsEmpty_charArray_null() {
        assertTrue(ArrayUtils.isEmpty((char[]) null));
    }

    @Test
    public void testIsEmpty_charArray_empty() {
        assertTrue(ArrayUtils.isEmpty(new char[0]));
    }

    @Test
    public void testIsEmpty_charArray_nonEmpty() {
        assertFalse(ArrayUtils.isEmpty(new char[]{'A'}));
    }

    // short[]
    @Test
    public void testIsEmpty_shortArray_null() {
        assertTrue(ArrayUtils.isEmpty((short[]) null));
    }

    @Test
    public void testIsEmpty_shortArray_empty() {
        assertTrue(ArrayUtils.isEmpty(new short[0]));
    }

    @Test
    public void testIsEmpty_shortArray_nonEmpty() {
        assertFalse(ArrayUtils.isEmpty(new short[]{1}));
    }

    // float[]
    @Test
    public void testIsEmpty_floatArray_null() {
        assertTrue(ArrayUtils.isEmpty((float[]) null));
    }

    @Test
    public void testIsEmpty_floatArray_empty() {
        assertTrue(ArrayUtils.isEmpty(new float[0]));
    }

    @Test
    public void testIsEmpty_floatArray_nonEmpty() {
        assertFalse(ArrayUtils.isEmpty(new float[]{1.0f}));
    }

    // double[]
    @Test
    public void testIsEmpty_doubleArray_null() {
        assertTrue(ArrayUtils.isEmpty((double[]) null));
    }

    @Test
    public void testIsEmpty_doubleArray_empty() {
        assertTrue(ArrayUtils.isEmpty(new double[0]));
    }

    @Test
    public void testIsEmpty_doubleArray_nonEmpty() {
        assertFalse(ArrayUtils.isEmpty(new double[]{1.0}));
    }

    // isSameLength for primitive arrays

    // int[]
    @Test
    public void testIsSameLength_intArray_bothNull() {
        assertTrue(ArrayUtils.isSameLength((int[]) null, (int[]) null));
    }

    @Test
    public void testIsSameLength_intArray_oneNullOtherEmpty() {
        assertTrue(ArrayUtils.isSameLength(null, new int[0]));
    }

    @Test
    public void testIsSameLength_intArray_differentLengths() {
        assertFalse(ArrayUtils.isSameLength(new int[1], new int[2]));
    }

    @Test
    public void testIsSameLength_intArray_sameLengths() {
        assertTrue(ArrayUtils.isSameLength(new int[3], new int[3]));
    }

    // long[]
    @Test
    public void testIsSameLength_longArray_bothNull() {
        assertTrue(ArrayUtils.isSameLength((long[]) null, (long[]) null));
    }

    @Test
    public void testIsSameLength_longArray_oneNullOtherEmpty() {
        assertTrue(ArrayUtils.isSameLength(null, new long[0]));
    }

    @Test
    public void testIsSameLength_longArray_differentLengths() {
        assertFalse(ArrayUtils.isSameLength(new long[1], new long[2]));
    }

    // boolean[]
    @Test
    public void testIsSameLength_booleanArray_bothNull() {
        assertTrue(ArrayUtils.isSameLength((boolean[]) null, (boolean[]) null));
    }

    @Test
    public void testIsSameLength_booleanArray_oneNullOtherEmpty() {
        assertTrue(ArrayUtils.isSameLength(null, new boolean[0]));
    }

    @Test
    public void testIsSameLength_booleanArray_differentLengths() {
        assertFalse(ArrayUtils.isSameLength(new boolean[1], new boolean[2]));
    }

    // clone for primitive arrays

    @Test
    public void testClone_intArray_null() {
        assertNull(ArrayUtils.clone((int[]) null));
    }

    @Test
    public void testClone_intArray_valid() {
        int[] original = {1, 2, 3};
        int[] cloned = ArrayUtils.clone(original);
        assertNotNull(cloned);
        assertNotSame(original, cloned);
        assertArrayEquals(original, cloned);
    }

    @Test
    public void testClone_longArray_null() {
        assertNull(ArrayUtils.clone((long[]) null));
    }

    @Test
    public void testClone_longArray_valid() {
        long[] original = {1L, 2L};
        long[] cloned = ArrayUtils.clone(original);
        assertNotNull(cloned);
        assertNotSame(original, cloned);
        assertArrayEquals(original, cloned);
    }

    @Test
    public void testClone_booleanArray_null() {
        assertNull(ArrayUtils.clone((boolean[]) null));
    }

    @Test
    public void testClone_booleanArray_valid() {
        boolean[] original = {true, false};
        boolean[] cloned = ArrayUtils.clone(original);
        assertNotNull(cloned);
        assertNotSame(original, cloned);
        assertArrayEquals(new boolean[]{true, false}, cloned);
    }

    // subarray for int[]
    @Test
    public void testSubarray_intArray_null() {
        assertNull(ArrayUtils.subarray((int[]) null, 0, 1));
    }

    @Test
    public void testSubarray_intArray_valid() {
        int[] input = {1, 2, 3, 4};
        int[] result = ArrayUtils.subarray(input, 1, 3);
        assertArrayEquals(new int[]{2, 3}, result);
    }

    @Test
    public void testSubarray_intArray_startNegative() {
        int[] input = {1, 2, 3};
        int[] result = ArrayUtils.subarray(input, -1, 2);
        assertArrayEquals(new int[]{1, 2}, result);
    }

    @Test
    public void testSubarray_intArray_endExceedsLength() {
        int[] input = {1, 2};
        int[] result = ArrayUtils.subarray(input, 1, 10);
        assertArrayEquals(new int[]{2}, result);
    }

    @Test
    public void testSubarray_intArray_newSizeZero() {
        int[] input = {1, 2, 3};
        int[] result = ArrayUtils.subarray(input, 2, 2);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    // add for int[]
    @Test
    public void testAdd_intArray_null() {
        int[] result = ArrayUtils.add((int[]) null, 5);
        assertArrayEquals(new int[]{5}, result);
    }

    @Test
    public void testAdd_intArray_valid() {
        int[] input = {1, 2};
        int[] result = ArrayUtils.add(input, 3);
        assertArrayEquals(new int[]{1, 2, 3}, result);
    }

    // remove for int[]
    @Test
    public void testRemove_intArray_validIndex() {
        int[] input = {1, 2, 3};
        int[] result = ArrayUtils.remove(input, 1);
        assertArrayEquals(new int[]{1, 3}, result);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemove_intArray_indexOutOfBounds() {
        ArrayUtils.remove(new int[]{1}, 5);
    }

    // removeElement for int[]
    @Test
    public void testRemoveElement_intArray_elementPresent() {
        int[] input = {1, 2, 1};
        int[] result = ArrayUtils.removeElement(input, 1);
        assertArrayEquals(new int[]{2, 1}, result);
    }

    @Test
    public void testRemoveElement_intArray_elementAbsent() {
        int[] input = {1, 2};
        int[] result = ArrayUtils.removeElement(input, 3);
        assertArrayEquals(input, result);
        assertNotSame(input, result);
    }

    // indexOf for int[]
    @Test
    public void testIndexOf_intArray_elementFound() {
        assertEquals(1, ArrayUtils.indexOf(new int[]{1, 2, 3}, 2));
    }

    @Test
    public void testIndexOf_intArray_elementNotFound() {
        assertEquals(-1, ArrayUtils.indexOf(new int[]{1, 2, 3}, 5));
    }

    @Test
    public void testIndexOf_intArray_startIndex() {
        assertEquals(2, ArrayUtils.indexOf(new int[]{1, 2, 1}, 1, 2));
    }

    // lastIndexOf for int[]
    @Test
    public void testLastIndexOf_intArray_elementFound() {
        assertEquals(2, ArrayUtils.lastIndexOf(new int[]{1, 2, 1}, 1));
    }

    @Test
    public void testLastIndexOf_intArray_elementNotFound() {
        assertEquals(-1, ArrayUtils.lastIndexOf(new int[]{1, 2, 3}, 5));
    }

    // contains for int[]
    @Test
    public void testContains_intArray_elementFound() {
        assertTrue(ArrayUtils.contains(new int[]{1, 2, 3}, 2));
    }

    @Test
    public void testContains_intArray_elementNotFound() {
        assertFalse(ArrayUtils.contains(new int[]{1, 2, 3}, 5));
    }

    // reverse for int[]
    @Test
    public void testReverse_intArray_valid() {
        int[] input = {1, 2, 3, 4};
        int[] result = ArrayUtils.reverse(input);
        assertArrayEquals(new int[]{4, 3, 2, 1}, result);
        assertSame(input, result);
    }

    // toString for int[]
    @Test
    public void testToString_intArray_null() {
        assertNull(ArrayUtils.toString((int[]) null));
    }

    @Test
    public void testToString_intArray_valid() {
        int[] input = {1, 2, 3};
        String result = ArrayUtils.toString(input);
        assertEquals("{1,2,3}", result);
    }

    // toObject for int[]
    @Test
    public void testToObject_intArray_valid() {
        int[] input = {1, 2, 3};
        Integer[] result = ArrayUtils.toObject(input);
        assertArrayEquals(new Integer[]{1, 2, 3}, result);
    }

    // toPrimitive for Integer[]
    @Test
    public void testToPrimitive_IntegerArray_valid() {
        Integer[] input = {1, 2, 3};
        int[] result = ArrayUtils.toPrimitive(input);
        assertArrayEquals(new int[]{1, 2, 3}, result);
    }

    // isSorted for int[]
    @Test
    public void testIsSorted_intArray_sorted() {
        assertTrue(ArrayUtils.isSorted(new int[]{1, 2, 3, 4}));
    }

    @Test
    public void testIsSorted_intArray_unsorted() {
        assertFalse(ArrayUtils.isSorted(new int[]{1, 3, 2}));
    }

    // nullToEmpty for int[]
    @Test
    public void testNullToEmpty_intArray_null() {
        int[] result = ArrayUtils.nullToEmpty((int[]) null);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testNullToEmpty_intArray_nonNull() {
        int[] input = {1, 2};
        int[] result = ArrayUtils.nullToEmpty(input);
        assertSame(input, result);
    }

    // getLength
    @Test
    public void testGetLength_null() {
        assertEquals(0, ArrayUtils.getLength(null));
    }

    @Test
    public void testGetLength_intArray() {
        assertEquals(3, ArrayUtils.getLength(new int[3]));
    }

    @Test
    public void testGetLength_StringArray() {
        assertEquals(2, ArrayUtils.getLength(new String[2]));
    }

    // isSameType
    @Test
    public void testIsSameType_sameType() {
        assertTrue(ArrayUtils.isSameType(new int[0], new int[0]));
    }

    @Test
    public void testIsSameType_differentType() {
        assertFalse(ArrayUtils.isSameType(new int[0], new long[0]));
    }

    @Test
    public void testIsSameType_withObjectArrays() {
        // String[] and Integer[] have different component types -> false
        assertFalse(ArrayUtils.isSameType(new String[0], new Integer[0]));
    }

    // isArrayIndexValid
    @Test
    public void testIsArrayIndexValid_validIndex() {
        assertTrue(ArrayUtils.isArrayIndexValid(new int[3], 0));
        assertTrue(ArrayUtils.isArrayIndexValid(new int[3], 2));
    }

    @Test
    public void testIsArrayIndexValid_negativeIndex() {
        assertFalse(ArrayUtils.isArrayIndexValid(new int[3], -1));
    }

    @Test
    public void testIsArrayIndexValid_outOfBounds() {
        assertFalse(ArrayUtils.isArrayIndexValid(new int[3], 3));
    }

    @Test
    public void testIsArrayIndexValid_nullArray() {
        assertFalse(ArrayUtils.isArrayIndexValid(null, 0));
    }

    // toMap additional tests
    @Test(expected = IllegalArgumentException.class)
    public void testToMap_nullElement_throws() {
        Object[] input = {null};
        ArrayUtils.toMap(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToMap_arrayElementLengthZero_throws() {
        Object[] input = {new Object[0]};
        ArrayUtils.toMap(input);
    }

    @Test
    public void testToMap_arrayElementLengthMoreThanTwo_returnsMap() {
        Object[] input = {new Object[]{"key", "value", "extra"}};
        Map<Object, Object> result = ArrayUtils.toMap(input);
        assertEquals(1, result.size());
        assertEquals("value", result.get("key"));
    }

    // addAll for int[]
    @Test
    public void testAddAll_intArray_bothNonNull() {
        int[] array1 = {1, 2};
        int[] array2 = {3, 4};
        int[] result = ArrayUtils.addAll(array1, array2);
        assertArrayEquals(new int[]{1, 2, 3, 4}, result);
    }

    @Test
    public void testAddAll_intArray_firstNull() {
        int[] array2 = {1, 2};
        int[] result = ArrayUtils.addAll((int[]) null, array2);
        assertArrayEquals(new int[]{1, 2}, result);
        assertNotSame(array2, result);
    }

    @Test
    public void testAddAll_intArray_secondNull() {
        int[] array1 = {1, 2};
        int[] result = ArrayUtils.addAll(array1, (int[]) null);
        assertArrayEquals(new int[]{1, 2}, result);
        assertNotSame(array1, result);
    }

    // swap for int[]
    @Test
    public void testSwap_intArray_valid() {
        int[] input = {1, 2, 3};
        ArrayUtils.swap(input, 0, 2);
        assertArrayEquals(new int[]{3, 2, 1}, input);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSwap_intArray_invalidIndex() {
        ArrayUtils.swap(new int[]{1}, 0, 5);
    }

}