package org.apache.commons.lang3;

import static org.junit.Assert.*;
import java.util.Map;
import org.junit.Test;

public class ArrayUtilsTest {

    // Tests null input for generic subarray
    @Test
    public void testSubarray_nullInput_returnsNull() {
        assertNull(ArrayUtils.subarray((Object[]) null, 0, 1));
    }

    // Tests normal subarray for int array
    @Test
    public void testSubarray_intArray_validRange_returnsSubarray() {
        int[] array = {1, 2, 3, 4, 5};
        int[] result = ArrayUtils.subarray(array, 1, 4);
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(2, result[0]);
        assertEquals(3, result[1]);
        assertEquals(4, result[2]);
    }

    // Tests subarray when endIndex exceeds array length
    @Test
    public void testSubarray_doubleArray_endIndexOvershoot_returnsToEnd() {
        double[] array = {1.1, 2.2, 3.3};
        double[] result = ArrayUtils.subarray(array, 1, 10);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals(2.2, result[0], 1e-9);
        assertEquals(3.3, result[1], 1e-9);
    }

    // Tests subarray with negative startIndex
    @Test
    public void testSubarray_booleanArray_negativeStartIndex_returnsFromZero() {
        boolean[] array = {true, false, true};
        boolean[] result = ArrayUtils.subarray(array, -1, 2);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertTrue(result[0]);
        assertFalse(result[1]);
    }

    // Tests subarray when startIndex >= endIndex producing empty array
    @Test
    public void testSubarray_longArray_startAfterEnd_returnsEmpty() {
        long[] array = {10L, 20L};
        long[] result = ArrayUtils.subarray(array, 2, 1);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    // Tests add with null array and non-null element
    @Test
    public void testAdd_nullArrayWithNonNullElement_returnsArrayWithElement() {
        String[] result = ArrayUtils.add((String[]) null, "test");
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("test", result[0]);
    }

    // Tests add with null array and null element
    @Test
    public void testAdd_nullArrayWithNullElement_returnsArrayWithNull() {
        String[] result = ArrayUtils.add((String[]) null, (String) null);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertNull(result[0]);
    }

    // Tests addAll of two int arrays
    @Test
    public void testAddAll_twoIntArrays_returnsCombinedArray() {
        int[] a = {1, 2};
        int[] b = {3, 4, 5};
        int[] result = ArrayUtils.addAll(a, b);
        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, result);
    }

    // Tests remove at valid index
    @Test
    public void testRemove_validIndex_removesElement() {
        String[] array = {"a", "b", "c"};
        String[] result = ArrayUtils.remove(array, 1);
        assertArrayEquals(new String[]{"a", "c"}, result);
    }

    // Tests remove with invalid index expecting exception
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemove_invalidIndex_throwsException() {
        ArrayUtils.remove(new int[]{1, 2}, 5);
    }

    // Tests removeElement when element is found (first occurrence)
    @Test
    public void testRemoveElement_elementPresent_removesFirst() {
        String[] array = {"a", "b", "a", "c"};
        String[] result = ArrayUtils.removeElement(array, "a");
        assertArrayEquals(new String[]{"b", "a", "c"}, result);
    }

    // Tests indexOf for null element in Object array
    @Test
    public void testIndexOf_objectArrayFindNull_returnsIndex() {
        String[] array = {"a", null, "b"};
        assertEquals(1, ArrayUtils.indexOf(array, null));
    }

    // Tests indexOf for non-null element in Object array
    @Test
    public void testIndexOf_objectArrayFindObject_returnsIndex() {
        String[] array = {"x", "y", "z"};
        assertEquals(2, ArrayUtils.indexOf(array, "z"));
    }

    // Tests lastIndexOf for null element
    @Test
    public void testLastIndexOf_objectArrayFindNull_returnsLastIndex() {
        String[] array = {null, "a", null};
        assertEquals(2, ArrayUtils.lastIndexOf(array, null));
    }

    // Tests isEmpty for null and empty arrays
    @Test
    public void testIsEmpty_nullAndEmpty_returnsTrue() {
        assertTrue(ArrayUtils.isEmpty((Object[]) null));
        assertTrue(ArrayUtils.isEmpty(new int[0]));
    }

    // Tests isSameLength for both null
    @Test
    public void testIsSameLength_bothNull_returnsTrue() {
        assertTrue(ArrayUtils.isSameLength((Object[]) null, (Object[]) null));
    }

    // Tests isSameLength for null and non-empty array
    @Test
    public void testIsSameLength_nullAndNonEmpty_returnsFalse() {
        assertFalse(ArrayUtils.isSameLength((Object[]) null, new String[]{"a"}));
    }

    // Tests toMap with valid two‑dimensional array
    @Test
    public void testToMap_validInput_returnsMap() {
        String[][] input = {{"key1", "value1"}, {"key2", "value2"}};
        Map<Object, Object> map = ArrayUtils.toMap(input);
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));
    }

    // Tests toMap with element whose length is less than 2 (exception expected)
    @Test(expected = IllegalArgumentException.class)
    public void testToMap_elementWithLength1_throwsException() {
        ArrayUtils.toMap(new Object[]{new Object[]{"onlyKey"}});
    }

    // Tests clone for null array
    @Test
    public void testClone_nullArray_returnsNull() {
        assertNull(ArrayUtils.clone((Object[]) null));
        assertNull(ArrayUtils.clone((int[]) null));
    }

    // Tests clone for non‑empty array returns a new array with same content
    @Test
    public void testClone_nonEmptyArray_returnsClone() {
        int[] original = {1, 2, 3};
        int[] cloned = ArrayUtils.clone(original);
        assertNotSame(original, cloned);
        assertArrayEquals(original, cloned);
    }

    // ========== Additional tests for uncovered parts ==========

    // --- subarray for other primitive types and Object[] normal ---
    @Test
    public void testSubarray_byteArray_normal() {
        byte[] array = {1, 2, 3, 4};
        byte[] result = ArrayUtils.subarray(array, 1, 3);
        assertArrayEquals(new byte[]{2, 3}, result);
    }

    @Test
    public void testSubarray_charArray_normal() {
        char[] array = {'a', 'b', 'c', 'd'};
        char[] result = ArrayUtils.subarray(array, 0, 2);
        assertArrayEquals(new char[]{'a', 'b'}, result);
    }

    @Test
    public void testSubarray_floatArray_normal() {
        float[] array = {1.0f, 2.0f, 3.0f};
        float[] result = ArrayUtils.subarray(array, 0, 2);
        assertArrayEquals(new float[]{1.0f, 2.0f}, result, 1e-9f);
    }

    @Test
    public void testSubarray_shortArray_normal() {
        short[] array = {10, 20, 30};
        short[] result = ArrayUtils.subarray(array, 1, 3);
        assertArrayEquals(new short[]{20, 30}, result);
    }

    @Test
    public void testSubarray_objectArray_normal() {
        String[] array = {"a", "b", "c"};
        String[] result = ArrayUtils.subarray(array, 0, 2);
        assertArrayEquals(new String[]{"a", "b"}, result);
    }

    @Test
    public void testSubarray_intArray_emptyRange() {
        int[] array = {1, 2, 3};
        int[] result = ArrayUtils.subarray(array, 0, 0);
        assertEquals(0, result.length);
    }

    // --- reverse for all primitive types and Object ---
    @Test
    public void testReverse_intArray() {
        int[] array = {1, 2, 3, 4};
        ArrayUtils.reverse(array);
        assertArrayEquals(new int[]{4, 3, 2, 1}, array);
    }

    @Test
    public void testReverse_booleanArray() {
        boolean[] array = {true, false, true};
        ArrayUtils.reverse(array);
        assertArrayEquals(new boolean[]{true, false, true}, array);
    }

    @Test
    public void testReverse_byteArray() {
        byte[] array = {1, 2, 3};
        ArrayUtils.reverse(array);
        assertArrayEquals(new byte[]{3, 2, 1}, array);
    }

    @Test
    public void testReverse_charArray() {
        char[] array = {'a', 'b', 'c'};
        ArrayUtils.reverse(array);
        assertArrayEquals(new char[]{'c', 'b', 'a'}, array);
    }

    @Test
    public void testReverse_doubleArray() {
        double[] array = {1.1, 2.2, 3.3};
        ArrayUtils.reverse(array);
        assertArrayEquals(new double[]{3.3, 2.2, 1.1}, array, 1e-9);
    }

    @Test
    public void testReverse_floatArray() {
        float[] array = {1.0f, 2.0f, 3.0f};
        ArrayUtils.reverse(array);
        assertArrayEquals(new float[]{3.0f, 2.0f, 1.0f}, array, 1e-9f);
    }

    @Test
    public void testReverse_longArray() {
        long[] array = {10L, 20L, 30L};
        ArrayUtils.reverse(array);
        assertArrayEquals(new long[]{30L, 20L, 10L}, array);
    }

    @Test
    public void testReverse_shortArray() {
        short[] array = {1, 2, 3};
        ArrayUtils.reverse(array);
        assertArrayEquals(new short[]{3, 2, 1}, array);
    }

    @Test
    public void testReverse_objectArray() {
        String[] array = {"a", "b", "c"};
        ArrayUtils.reverse(array);
        assertArrayEquals(new String[]{"c", "b", "a"}, array);
    }

    // --- swap for different types ---
    @Test
    public void testSwap_intArray() {
        int[] array = {1, 2, 3, 4};
        ArrayUtils.swap(array, 0, 2);
        assertArrayEquals(new int[]{3, 2, 1, 4}, array);
    }

    @Test
    public void testSwap_longArray() {
        long[] array = {10L, 20L, 30L};
        ArrayUtils.swap(array, 1, 2);
        assertArrayEquals(new long[]{10L, 30L, 20L}, array);
    }

    @Test
    public void testSwap_objectArray() {
        String[] array = {"a", "b", "c"};
        ArrayUtils.swap(array, 0, 2);
        assertArrayEquals(new String[]{"c", "b", "a"}, array);
    }

    @Test
    public void testSwap_sameIndex_noChange() {
        int[] array = {1, 2, 3};
        ArrayUtils.swap(array, 1, 1);
        assertArrayEquals(new int[]{1, 2, 3}, array);
    }

    // --- shift for int array ---
    @Test
    public void testShift_intArray_left() {
        int[] array = {1, 2, 3, 4, 5};
        ArrayUtils.shift(array, 2);
        assertArrayEquals(new int[]{3, 4, 5, 1, 2}, array);
    }

    @Test
    public void testShift_intArray_right() {
        int[] array = {1, 2, 3, 4, 5};
        ArrayUtils.shift(array, -2);
        assertArrayEquals(new int[]{4, 5, 1, 2, 3}, array);
    }

    @Test
    public void testShift_intArray_negativeShift() {
        int[] array = {1, 2, 3};
        ArrayUtils.shift(array, -1);
        assertArrayEquals(new int[]{3, 1, 2}, array);
    }

    // --- insert for int and Object arrays ---
    @Test
    public void testInsert_intArray() {
        int[] array = {1, 2, 3};
        int[] result = ArrayUtils.insert(1, array, 99);
        assertArrayEquals(new int[]{1, 99, 2, 3}, result);
    }

    @Test
    public void testInsert_objectArray() {
        String[] array = {"a", "b"};
        String[] result = ArrayUtils.insert(0, array, "x");
        assertArrayEquals(new String[]{"x", "a", "b"}, result);
    }

    @Test
    public void testInsert_atEnd() {
        int[] array = {1, 2};
        int[] result = ArrayUtils.insert(2, array, 3);
        assertArrayEquals(new int[]{1, 2, 3}, result);
    }

    // --- contains for int and Object arrays ---
    @Test
    public void testContains_intArray_found() {
        assertTrue(ArrayUtils.contains(new int[]{1, 2, 3}, 2));
    }

    @Test
    public void testContains_intArray_notFound() {
        assertFalse(ArrayUtils.contains(new int[]{1, 2, 3}, 4));
    }

    @Test
    public void testContains_objectArray_found() {
        assertTrue(ArrayUtils.contains(new String[]{"a", "b"}, "a"));
    }

    @Test
    public void testContains_objectArray_nullFound() {
        assertTrue(ArrayUtils.contains(new String[]{"a", null, "b"}, (String) null));
    }

    // --- isEmpty for all remaining primitive types ---
    @Test
    public void testIsEmpty_booleanArray_null() {
        assertTrue(ArrayUtils.isEmpty((boolean[]) null));
    }

    @Test
    public void testIsEmpty_booleanArray_empty() {
        assertTrue(ArrayUtils.isEmpty(new boolean[0]));
    }

    @Test
    public void testIsEmpty_byteArray_null() {
        assertTrue(ArrayUtils.isEmpty((byte[]) null));
    }

    @Test
    public void testIsEmpty_byteArray_empty() {
        assertTrue(ArrayUtils.isEmpty(new byte[0]));
    }

    @Test
    public void testIsEmpty_charArray_null() {
        assertTrue(ArrayUtils.isEmpty((char[]) null));
    }

    @Test
    public void testIsEmpty_charArray_empty() {
        assertTrue(ArrayUtils.isEmpty(new char[0]));
    }

    @Test
    public void testIsEmpty_doubleArray_null() {
        assertTrue(ArrayUtils.isEmpty((double[]) null));
    }

    @Test
    public void testIsEmpty_doubleArray_empty() {
        assertTrue(ArrayUtils.isEmpty(new double[0]));
    }

    @Test
    public void testIsEmpty_floatArray_null() {
        assertTrue(ArrayUtils.isEmpty((float[]) null));
    }

    @Test
    public void testIsEmpty_floatArray_empty() {
        assertTrue(ArrayUtils.isEmpty(new float[0]));
    }

    @Test
    public void testIsEmpty_longArray_null() {
        assertTrue(ArrayUtils.isEmpty((long[]) null));
    }

    @Test
    public void testIsEmpty_longArray_empty() {
        assertTrue(ArrayUtils.isEmpty(new long[0]));
    }

    @Test
    public void testIsEmpty_shortArray_null() {
        assertTrue(ArrayUtils.isEmpty((short[]) null));
    }

    @Test
    public void testIsEmpty_shortArray_empty() {
        assertTrue(ArrayUtils.isEmpty(new short[0]));
    }

    // --- isSameLength for various primitive types ---
    @Test
    public void testIsSameLength_intArray_same() {
        assertTrue(ArrayUtils.isSameLength(new int[3], new int[3]));
    }

    @Test
    public void testIsSameLength_intArray_different() {
        assertFalse(ArrayUtils.isSameLength(new int[3], new int[4]));
    }

    @Test
    public void testIsSameLength_booleanArray_same() {
        assertTrue(ArrayUtils.isSameLength(new boolean[2], new boolean[2]));
    }

    @Test
    public void testIsSameLength_booleanArray_different() {
        assertFalse(ArrayUtils.isSameLength(new boolean[2], new boolean[3]));
    }

    @Test
    public void testIsSameLength_byteArray_same() {
        assertTrue(ArrayUtils.isSameLength(new byte[1], new byte[1]));
    }

    @Test
    public void testIsSameLength_charArray_same() {
        assertTrue(ArrayUtils.isSameLength(new char[5], new char[5]));
    }

    @Test
    public void testIsSameLength_doubleArray_same() {
        assertTrue(ArrayUtils.isSameLength(new double[0], new double[0]));
    }

    @Test
    public void testIsSameLength_floatArray_same() {
        assertTrue(ArrayUtils.isSameLength(new float[2], new float[2]));
    }

    @Test
    public void testIsSameLength_longArray_same() {
        assertTrue(ArrayUtils.isSameLength(new long[3], new long[3]));
    }

    @Test
    public void testIsSameLength_shortArray_same() {
        assertTrue(ArrayUtils.isSameLength(new short[4], new short[4]));
    }

    @Test
    public void testIsSameLength_nullAndNonNull_int() {
        assertFalse(ArrayUtils.isSameLength((int[]) null, new int[1]));
    }

    // --- toMap additional cases ---
    @Test
    public void testToMap_duplicateKey_lastWins() {
        Object[][] input = {{"key", "value1"}, {"key", "value2"}};
        Map<Object, Object> map = ArrayUtils.toMap(input);
        assertEquals("value2", map.get("key"));
    }

    @Test
    public void testToMap_nullKey() {
        Object[][] input = {{null, "value"}};
        Map<Object, Object> map = ArrayUtils.toMap(input);
        assertTrue(map.containsKey(null));
        assertEquals("value", map.get(null));
    }

    @Test
    public void testToMap_nullValue() {
        Object[][] input = {{"key", null}};
        Map<Object, Object> map = ArrayUtils.toMap(input);
        assertTrue(map.containsKey("key"));
        assertNull(map.get("key"));
    }

    // --- clone for remaining primitive types ---
    @Test
    public void testClone_booleanArray() {
        boolean[] original = {true, false};
        boolean[] cloned = ArrayUtils.clone(original);
        assertNotSame(original, cloned);
        assertArrayEquals(original, cloned);
    }

    @Test
    public void testClone_byteArray() {
        byte[] original = {1, 2};
        byte[] cloned = ArrayUtils.clone(original);
        assertNotSame(original, cloned);
        assertArrayEquals(original, cloned);
    }

    @Test
    public void testClone_charArray() {
        char[] original = {'a', 'b'};
        char[] cloned = ArrayUtils.clone(original);
        assertNotSame(original, cloned);
        assertArrayEquals(original, cloned);
    }

    @Test
    public void testClone_doubleArray() {
        double[] original = {1.1, 2.2};
        double[] cloned = ArrayUtils.clone(original);
        assertNotSame(original, cloned);
        assertArrayEquals(original, cloned, 1e-9);
    }

    @Test
    public void testClone_floatArray() {
        float[] original = {1.0f, 2.0f};
        float[] cloned = ArrayUtils.clone(original);
        assertNotSame(original, cloned);
        assertArrayEquals(original, cloned, 1e-9f);
    }

    @Test
    public void testClone_longArray() {
        long[] original = {10L, 20L};
        long[] cloned = ArrayUtils.clone(original);
        assertNotSame(original, cloned);
        assertArrayEquals(original, cloned);
    }

    @Test
    public void testClone_shortArray() {
        short[] original = {1, 2};
        short[] cloned = ArrayUtils.clone(original);
        assertNotSame(original, cloned);
        assertArrayEquals(original, cloned);
    }

    // --- add for primitive arrays and add with index ---
    @Test
    public void testAdd_intArray() {
        int[] array = {1, 2};
        int[] result = ArrayUtils.add(array, 3);
        assertArrayEquals(new int[]{1, 2, 3}, result);
    }

    @Test
    public void testAddAtIndex_intArray() {
        int[] array = {1, 2, 3};
        int[] result = ArrayUtils.add(array, 1, 99);
        assertArrayEquals(new int[]{1, 99, 2, 3}, result);
    }

    @Test
    public void testAddAtIndex_objectArray() {
        String[] array = {"a", "b"};
        String[] result = ArrayUtils.add(array, 0, "x");
        assertArrayEquals(new String[]{"x", "a", "b"}, result);
    }

    @Test
    public void testAdd_booleanArray() {
        boolean[] array = {true};
        boolean[] result = ArrayUtils.add(array, false);
        assertArrayEquals(new boolean[]{true, false}, result);
    }

    // --- addAll for other types ---
    @Test
    public void testAddAll_booleanArray() {
        boolean[] a = {true, false};
        boolean[] b = {true};
        boolean[] result = ArrayUtils.addAll(a, b);
        assertArrayEquals(new boolean[]{true, false, true}, result);
    }

    @Test
    public void testAddAll_objectArray() {
        String[] a = {"a", "b"};
        String[] b = {"c"};
        String[] result = ArrayUtils.addAll(a, b);
        assertArrayEquals(new String[]{"a", "b", "c"}, result);
    }

    @Test
    public void testAddAll_emptyFirst() {
        int[] a = {};
        int[] b = {1, 2};
        int[] result = ArrayUtils.addAll(a, b);
        assertArrayEquals(new int[]{1, 2}, result);
    }

    // --- remove for primitive types ---
    @Test
    public void testRemove_charArray() {
        char[] array = {'a', 'b', 'c'};
        char[] result = ArrayUtils.remove(array, 1);
        assertArrayEquals(new char[]{'a', 'c'}, result);
    }

    @Test
    public void testRemove_booleanArray() {
        boolean[] array = {true, false, true};
        boolean[] result = ArrayUtils.remove(array, 0);
        assertArrayEquals(new boolean[]{false, true}, result);
    }

    @Test
    public void testRemove_longArray() {
        long[] array = {10L, 20L, 30L};
        long[] result = ArrayUtils.remove(array, 2);
        assertArrayEquals(new long[]{10L, 20L}, result);
    }

    // --- removeElement for primitive types ---
    @Test
    public void testRemoveElement_intArray() {
        int[] array = {1, 2, 1, 3};
        int[] result = ArrayUtils.removeElement(array, 1);
        assertArrayEquals(new int[]{2, 1, 3}, result);
    }

    @Test
    public void testRemoveElement_booleanArray() {
        boolean[] array = {true, false, true};
        boolean[] result = ArrayUtils.removeElement(array, true);
        assertArrayEquals(new boolean[]{false, true}, result);
    }

    // --- indexOf and lastIndexOf for primitive types ---
    @Test
    public void testIndexOf_intArray_found() {
        assertEquals(1, ArrayUtils.indexOf(new int[]{1, 2, 3}, 2));
    }

    @Test
    public void testIndexOf_intArray_notFound() {
        assertEquals(-1, ArrayUtils.indexOf(new int[]{1, 2, 3}, 4));
    }

    @Test
    public void testIndexOf_byteArray() {
        assertEquals(2, ArrayUtils.indexOf(new byte[]{1, 2, 3}, (byte) 3));
    }

    @Test
    public void testIndexOf_charArray() {
        assertEquals(0, ArrayUtils.indexOf(new char[]{'a', 'b'}, 'a'));
    }

    @Test
    public void testIndexOf_doubleArray() {
        assertEquals(1, ArrayUtils.indexOf(new double[]{1.1, 2.2, 3.3}, 2.2, 1e-9));
    }

    @Test
    public void testLastIndexOf_intArray_found() {
        assertEquals(2, ArrayUtils.lastIndexOf(new int[]{1, 2, 1}, 1));
    }

    @Test
    public void testLastIndexOf_intArray_notFound() {
        assertEquals(-1, ArrayUtils.lastIndexOf(new int[]{1, 2, 3}, 4));
    }

    @Test
    public void testLastIndexOf_booleanArray() {
        assertEquals(1, ArrayUtils.lastIndexOf(new boolean[]{true, false, true}, true));
    }

    // --- nullToEmpty ---
    @Test
    public void testNullToEmpty_objectArray_null() {
        String[] result = ArrayUtils.nullToEmpty((String[]) null);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testNullToEmpty_objectArray_nonNull() {
        String[] input = {"a", "b"};
        assertSame(input, ArrayUtils.nullToEmpty(input));
    }

    @Test
    public void testNullToEmpty_intArray_null() {
        int[] result = ArrayUtils.nullToEmpty((int[]) null);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testNullToEmpty_booleanArray_null() {
        boolean[] result = ArrayUtils.nullToEmpty((boolean[]) null);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    // --- isNotEmpty ---
    @Test
    public void testIsNotEmpty_null() {
        assertFalse(ArrayUtils.isNotEmpty((Object[]) null));
    }

    @Test
    public void testIsNotEmpty_empty() {
        assertFalse(ArrayUtils.isNotEmpty(new Object[0]));
    }

    @Test
    public void testIsNotEmpty_nonEmpty() {
        assertTrue(ArrayUtils.isNotEmpty(new String[]{"a"}));
    }

    @Test
    public void testIsNotEmpty_intArray_nonEmpty() {
        assertTrue(ArrayUtils.isNotEmpty(new int[]{1}));
    }
}