package org.apache.commons.lang3;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ArrayUtils}.
 */
public class ArrayUtilsTest {

    // Tests adding elements to an Object array and type retention
    @Test
    public void testAdd_objectArrayAndElement_returnsNewArray() {
        String[] array = new String[]{"a", "b"};
        String[] result = ArrayUtils.add(array, "c");
        assertArrayEquals(new String[]{"a", "b", "c"}, result);
        assertEquals(String.class, result.getClass().getComponentType());
    }

    // Tests adding an element to a null Object array
    @Test
    public void testAdd_nullArrayValidElement_returnsSingleElementArray() {
        String[] result = ArrayUtils.add((String[]) null, "a");
        assertArrayEquals(new String[]{"a"}, result);
        assertEquals(String.class, result.getClass().getComponentType());
    }

    // Tests adding null element to a null array throws IllegalArgumentException (Defects4J Lang-35)
    @Test(expected = IllegalArgumentException.class)
    public void testAdd_nullArrayAndNullElement_throwsIllegalArgumentException() {
        ArrayUtils.add((String[]) null, (String) null);
    }

    // Tests inserting an element at a specific index in an Object array
    @Test
    public void testAddAtIndex_objectArray_insertsAtCorrectPosition() {
        String[] array = new String[]{"a", "c"};
        String[] result = ArrayUtils.add(array, 1, "b");
        assertArrayEquals(new String[]{"a", "b", "c"}, result);
    }

    // Tests inserting null element at index 0 into a null array throws IllegalArgumentException (Defects4J Lang-35)
    @Test(expected = IllegalArgumentException.class)
    public void testAddAtIndex_nullArrayAndNullElement_throwsIllegalArgumentException() {
        ArrayUtils.add((String[]) null, 0, (String) null);
    }

    // Tests inserting at an invalid negative index throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAtIndex_negativeIndex_throwsIndexOutOfBoundsException() {
        ArrayUtils.add(new int[]{1, 2}, -1, 3);
    }

    // Tests adding primitive elements to primitive arrays
    @Test
    public void testAdd_primitiveIntArray_returnsExtendedArray() {
        int[] array = new int[]{1, 2};
        int[] result = ArrayUtils.add(array, 3);
        assertArrayEquals(new int[]{1, 2, 3}, result);

        int[] nullResult = ArrayUtils.add((int[]) null, 5);
        assertArrayEquals(new int[]{5}, nullResult);
    }

    // Tests combining two arrays using addAll
    @Test
    public void testAddAll_objectArrays_returnsMergedArray() {
        String[] array1 = new String[]{"a", "b"};
        String[] array2 = new String[]{"c", "d"};
        String[] result = ArrayUtils.addAll(array1, array2);
        assertArrayEquals(new String[]{"a", "b", "c", "d"}, result);

        assertArrayEquals(array1, ArrayUtils.addAll(array1, (String[]) null));
        assertArrayEquals(array2, ArrayUtils.addAll((String[]) null, array2));
        assertNull(ArrayUtils.addAll((String[]) null, (String[]) null));
    }

    // Tests addAll with incompatible array types throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAddAll_incompatibleTypes_throwsIllegalArgumentException() {
        String[] array1 = new String[]{"a"};
        Integer[] array2 = new Integer[]{1};
        ArrayUtils.addAll(array1, (String[]) (Object) array2);
    }

    // Tests removing an element by index from an Object array
    @Test
    public void testRemove_validIndex_returnsArrayWithoutElement() {
        String[] array = new String[]{"a", "b", "c"};
        String[] result = ArrayUtils.remove(array, 1);
        assertArrayEquals(new String[]{"a", "c"}, result);
    }

    // Tests removing an element at an invalid index throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemove_indexOutOfBounds_throwsIndexOutOfBoundsException() {
        ArrayUtils.remove(new String[]{"a", "b"}, 2);
    }

    // Tests removing an element from a null array throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemove_nullArray_throwsIndexOutOfBoundsException() {
        ArrayUtils.remove((Object[]) null, 0);
    }

    // Tests removing the first occurrence of an element
    @Test
    public void testRemoveElement_existingAndNonExistingElement_returnsExpectedArray() {
        String[] array = new String[]{"a", "b", "a"};
        String[] result = ArrayUtils.removeElement(array, "a");
        assertArrayEquals(new String[]{"b", "a"}, result);

        String[] notFoundResult = ArrayUtils.removeElement(array, "nonexistent");
        assertArrayEquals(array, notFoundResult);

        assertNull(ArrayUtils.removeElement((String[]) null, "a"));
    }

    // Tests indexOf and lastIndexOf on Object arrays
    @Test
    public void testIndexOfAndLastIndexOf_objectArray_returnsCorrectIndices() {
        String[] array = new String[]{"a", "b", "a", null};
        assertEquals(0, ArrayUtils.indexOf(array, "a"));
        assertEquals(2, ArrayUtils.lastIndexOf(array, "a"));
        assertEquals(3, ArrayUtils.indexOf(array, null));
        assertEquals(3, ArrayUtils.lastIndexOf(array, null));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(array, "c"));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf((String[]) null, "a"));
        assertTrue(ArrayUtils.contains(array, "b"));
        assertFalse(ArrayUtils.contains(array, "z"));
    }

    // Tests double indexOf with tolerance boundary cases
    @Test
    public void testIndexOf_doubleArrayWithTolerance_returnsCorrectIndex() {
        double[] array = new double[]{1.0, 2.0, 3.0};
        assertEquals(1, ArrayUtils.indexOf(array, 2.05, 0.1));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(array, 2.5, 0.1));
        assertEquals(1, ArrayUtils.lastIndexOf(array, 2.05, 0.1));
        assertTrue(ArrayUtils.contains(array, 2.05, 0.1));
        assertFalse(ArrayUtils.contains(array, 5.0, 0.1));
    }

    // Tests extracting a subarray with various valid and boundary indices
    @Test
    public void testSubarray_variousIndices_returnsExpectedSubarray() {
        Integer[] array = new Integer[]{10, 20, 30, 40};
        assertArrayEquals(new Integer[]{20, 30}, ArrayUtils.subarray(array, 1, 3));
        assertArrayEquals(new Integer[]{10, 20}, ArrayUtils.subarray(array, -2, 2));
        assertArrayEquals(new Integer[]{30, 40}, ArrayUtils.subarray(array, 2, 10));
        assertArrayEquals(new Integer[0], ArrayUtils.subarray(array, 3, 2));
        assertNull(ArrayUtils.subarray((Integer[]) null, 0, 1));
    }

    // Tests converting a multi-dimensional array into a Map
    @Test
    public void testToMap_valid2DArray_returnsPopulatedMap() {
        String[][] array = new String[][]{{"key1", "val1"}, {"key2", "val2"}};
        Map<Object, Object> map = ArrayUtils.toMap(array);
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals("val1", map.get("key1"));
        assertEquals("val2", map.get("key2"));
        assertNull(ArrayUtils.toMap(null));
    }

    // Tests toMap with invalid element types throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testToMap_invalidEntryType_throwsIllegalArgumentException() {
        Object[] invalidArray = new Object[]{"singleString"};
        ArrayUtils.toMap(invalidArray);
    }

    // Tests converting primitive arrays to object arrays and vice-versa
    @Test
    public void testToPrimitiveAndToObject_intArrays_convertsProperly() {
        int[] primitives = new int[]{1, 2, 3};
        Integer[] objects = ArrayUtils.toObject(primitives);
        assertArrayEquals(new Integer[]{1, 2, 3}, objects);

        int[] backToPrimitives = ArrayUtils.toPrimitive(objects);
        assertArrayEquals(primitives, backToPrimitives);

        Integer[] withNull = new Integer[]{1, null, 3};
        int[] defaulted = ArrayUtils.toPrimitive(withNull, -1);
        assertArrayEquals(new int[]{1, -1, 3}, defaulted);

        assertNull(ArrayUtils.toPrimitive((Integer[]) null));
        assertNull(ArrayUtils.toObject((int[]) null));
    }

    // Tests reverse, isSameLength, isSameType, isEmpty, and getLength utility methods
    @Test
    public void testUtilityMethods_validInputs_returnsExpectedResults() {
        int[] array = new int[]{1, 2, 3};
        ArrayUtils.reverse(array);
        assertArrayEquals(new int[]{3, 2, 1}, array);
        ArrayUtils.reverse((int[]) null);

        assertTrue(ArrayUtils.isSameLength(new int[]{1, 2}, new int[]{3, 4}));
        assertFalse(ArrayUtils.isSameLength(new int[]{1}, new int[]{1, 2}));
        assertTrue(ArrayUtils.isSameLength((int[]) null, (int[]) null));

        assertTrue(ArrayUtils.isSameType(new String[0], new String[1]));
        assertFalse(ArrayUtils.isSameType(new String[0], new Integer[0]));

        assertTrue(ArrayUtils.isEmpty((Object[]) null));
        assertTrue(ArrayUtils.isEmpty(new Object[0]));
        assertFalse(ArrayUtils.isEmpty(new String[]{"a"}));

        assertEquals(0, ArrayUtils.getLength(null));
        assertEquals(3, ArrayUtils.getLength(new int[]{1, 2, 3}));
    }
}