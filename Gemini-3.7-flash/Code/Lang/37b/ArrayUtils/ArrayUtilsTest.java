package org.apache.commons.lang3;

import org.junit.Test;
import java.util.Map;
import static org.junit.Assert.*;

public class ArrayUtilsTest {

    // Tests constructor instantiation
    @Test
    public void testConstructor_default_newInstanceNotNull() {
        ArrayUtils utils = new ArrayUtils();
        assertNotNull(utils);
    }

    // Tests toString method with null and valid array inputs
    @Test
    public void testToString_validAndNullInput_returnsFormattedString() {
        assertEquals("{}", ArrayUtils.toString(null));
        assertEquals("custom", ArrayUtils.toString(null, "custom"));
        assertEquals("{1,2}", ArrayUtils.toString(new int[] { 1, 2 }));
    }

    // Tests isEquals method with identical and different arrays
    @Test
    public void testIsEquals_arrayComparison_returnsCorrectBoolean() {
        assertTrue(ArrayUtils.isEquals(new int[] { 1, 2 }, new int[] { 1, 2 }));
        assertFalse(ArrayUtils.isEquals(new int[] { 1, 2 }, new int[] { 2, 1 }));
        assertFalse(ArrayUtils.isEquals(new int[] { 1 }, null));
    }

    // Tests toMap with valid nested key-value pairs
    @Test
    public void testToMap_validArrayInput_returnsPopulatedMap() {
        String[][] array = new String[][] {
            {"RED", "#FF0000"},
            {"GREEN", "#00FF00"}
        };
        Map<Object, Object> map = ArrayUtils.toMap(array);
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals("#FF0000", map.get("RED"));
        assertEquals("#00FF00", map.get("GREEN"));
        assertNull(ArrayUtils.toMap(null));
    }

    // Tests toMap with invalid array element length throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testToMap_invalidSubArrayLength_throwsIllegalArgumentException() {
        Object[] array = new Object[] {
            new String[] { "key" }
        };
        ArrayUtils.toMap(array);
    }

    // Tests clone operations for Object and primitive arrays
    @Test
    public void testClone_validAndNullArrays_returnsClonedArray() {
        assertNull(ArrayUtils.clone((Object[]) null));
        String[] strArray = new String[] { "a", "b" };
        String[] clonedStr = ArrayUtils.clone(strArray);
        assertArrayEquals(strArray, clonedStr);
        assertNotSame(strArray, clonedStr);

        int[] intArray = new int[] { 1, 2, 3 };
        int[] clonedInt = ArrayUtils.clone(intArray);
        assertArrayEquals(intArray, clonedInt);
        assertNotSame(intArray, clonedInt);
    }

    // Tests subarray extraction with normal and boundary index values
    @Test
    public void testSubarray_validAndBoundaryIndices_returnsCorrectSubarray() {
        String[] array = new String[] { "a", "b", "c", "d" };
        assertArrayEquals(new String[] { "b", "c" }, ArrayUtils.subarray(array, 1, 3));
        assertArrayEquals(new String[] { "a", "b" }, ArrayUtils.subarray(array, -1, 2));
        assertArrayEquals(new String[] { "c", "d" }, ArrayUtils.subarray(array, 2, 10));
        assertArrayEquals(new String[0], ArrayUtils.subarray(array, 3, 2));
        assertNull(ArrayUtils.subarray((Object[]) null, 0, 1));

        int[] intArray = new int[] { 10, 20, 30, 40 };
        assertArrayEquals(new int[] { 20, 30 }, ArrayUtils.subarray(intArray, 1, 3));
    }

    // Tests isSameLength method across null, empty, matching and non-matching lengths
    @Test
    public void testIsSameLength_variousArrays_returnsCorrectBoolean() {
        assertTrue(ArrayUtils.isSameLength((Object[]) null, (Object[]) null));
        assertTrue(ArrayUtils.isSameLength(new String[0], (String[]) null));
        assertFalse(ArrayUtils.isSameLength(new String[] { "a" }, (String[]) null));
        assertTrue(ArrayUtils.isSameLength(new int[] { 1, 2 }, new int[] { 3, 4 }));
        assertFalse(ArrayUtils.isSameLength(new int[] { 1 }, new int[] { 1, 2 }));
    }

    // Tests isSameType method checking type equivalence and exception handling
    @Test
    public void testIsSameType_matchingAndMismatchedTypes_returnsCorrectResult() {
        assertTrue(ArrayUtils.isSameType(new String[0], new String[1]));
        assertFalse(ArrayUtils.isSameType(new String[0], new Object[0]));
    }

    // Tests isSameType with null argument throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testIsSameType_nullArray_throwsIllegalArgumentException() {
        ArrayUtils.isSameType(null, new Object[0]);
    }

    // Tests reverse method modifying array in-place
    @Test
    public void testReverse_validArrays_reversesInPlace() {
        String[] strArray = new String[] { "a", "b", "c" };
        ArrayUtils.reverse(strArray);
        assertArrayEquals(new String[] { "c", "b", "a" }, strArray);

        int[] intArray = new int[] { 1, 2, 3, 4 };
        ArrayUtils.reverse(intArray);
        assertArrayEquals(new int[] { 4, 3, 2, 1 }, intArray);

        ArrayUtils.reverse((Object[]) null);
    }

    // Tests indexOf and lastIndexOf on Object arrays
    @Test
    public void testIndexOfAndLastIndexOf_objectArray_returnsCorrectIndex() {
        String[] array = new String[] { "a", "b", "c", "b", null };
        assertEquals(1, ArrayUtils.indexOf(array, "b"));
        assertEquals(3, ArrayUtils.lastIndexOf(array, "b"));
        assertEquals(4, ArrayUtils.indexOf(array, null));
        assertEquals(4, ArrayUtils.lastIndexOf(array, null));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(array, "z"));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(null, "a"));
    }

    // Tests indexOf, lastIndexOf and contains on primitive arrays
    @Test
    public void testIndexOfAndContains_primitiveArrays_returnsExpected() {
        int[] intArray = new int[] { 1, 2, 3, 2, 1 };
        assertEquals(1, ArrayUtils.indexOf(intArray, 2));
        assertEquals(3, ArrayUtils.lastIndexOf(intArray, 2));
        assertTrue(ArrayUtils.contains(intArray, 3));
        assertFalse(ArrayUtils.contains(intArray, 99));

        double[] doubleArray = new double[] { 1.0, 2.0, 3.0 };
        assertEquals(1, ArrayUtils.indexOf(doubleArray, 2.05, 0.1));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(doubleArray, 2.5, 0.1));
        assertTrue(ArrayUtils.contains(doubleArray, 2.05, 0.1));
    }

    // Tests toPrimitive and toObject conversion methods
    @Test
    public void testToPrimitiveAndToObject_conversions_returnsConvertedArray() {
        Integer[] objInts = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), null };
        int[] primInts = ArrayUtils.toPrimitive(objInts, -1);
        assertArrayEquals(new int[] { 1, 2, -1 }, primInts);

        Integer[] convertedBack = ArrayUtils.toObject(new int[] { 1, 2 });
        assertArrayEquals(new Integer[] { Integer.valueOf(1), Integer.valueOf(2) }, convertedBack);

        assertNull(ArrayUtils.toPrimitive((Integer[]) null));
        assertNull(ArrayUtils.toObject((int[]) null));
        assertArrayEquals(ArrayUtils.EMPTY_INT_ARRAY, ArrayUtils.toPrimitive(new Integer[0]));
    }

    // Tests isEmpty method across various array types
    @Test
    public void testIsEmpty_nullAndEmptyAndPopulated_returnsCorrectBoolean() {
        assertTrue(ArrayUtils.isEmpty((Object[]) null));
        assertTrue(ArrayUtils.isEmpty(new Object[0]));
        assertFalse(ArrayUtils.isEmpty(new String[] { "test" }));

        assertTrue(ArrayUtils.isEmpty((int[]) null));
        assertTrue(ArrayUtils.isEmpty(new int[0]));
        assertFalse(ArrayUtils.isEmpty(new int[] { 1 }));
    }

    // Tests addAll combining compatible arrays
    @Test
    public void testAddAll_compatibleArrays_returnsCombinedArray() {
        String[] array1 = new String[] { "a", "b" };
        String[] array2 = new String[] { "c", "d" };
        String[] combined = ArrayUtils.addAll(array1, array2);
        assertArrayEquals(new String[] { "a", "b", "c", "d" }, combined);

        assertArrayEquals(array1, ArrayUtils.addAll(array1, (String[]) null));
        assertArrayEquals(array2, ArrayUtils.addAll((String[]) null, array2));
        assertNull(ArrayUtils.addAll((Object[]) null, (Object[]) null));

        int[] intCombined = ArrayUtils.addAll(new int[] { 1, 2 }, new int[] { 3, 4 });
        assertArrayEquals(new int[] { 1, 2, 3, 4 }, intCombined);
    }

    // Tests addAll with incompatible array element types (defect Lang-37)
    @Test(expected = IllegalArgumentException.class)
    public void testAddAll_incompatibleTypes_throwsIllegalArgumentException() {
        Object[] array1 = new Integer[] { Integer.valueOf(1) };
        Object[] array2 = new String[] { "hello" };
        ArrayUtils.addAll(array1, array2);
    }

    // Tests add element at the end and at a specific index
    @Test
    public void testAdd_elementsAndIndices_returnsExpandedArray() {
        String[] array = new String[] { "a", "b" };
        assertArrayEquals(new String[] { "a", "b", "c" }, ArrayUtils.add(array, "c"));
        assertArrayEquals(new String[] { "start", "a", "b" }, ArrayUtils.add(array, 0, "start"));
        assertArrayEquals(new String[] { "a", "mid", "b" }, ArrayUtils.add(array, 1, "mid"));

        assertArrayEquals(new String[] { "first" }, ArrayUtils.add((String[]) null, "first"));
        assertArrayEquals(new int[] { 1, 2, 3 }, ArrayUtils.add(new int[] { 1, 2 }, 3));
        assertArrayEquals(new int[] { 99, 1, 2 }, ArrayUtils.add(new int[] { 1, 2 }, 0, 99));
    }

    // Tests remove and removeElement methods
    @Test
    public void testRemoveAndRemoveElement_validInputs_returnsShrunkArray() {
        String[] array = new String[] { "a", "b", "c", "b" };
        assertArrayEquals(new String[] { "a", "c", "b" }, ArrayUtils.remove(array, 1));
        assertArrayEquals(new String[] { "a", "c", "b" }, ArrayUtils.removeElement(array, "b"));
        assertArrayEquals(array, ArrayUtils.removeElement(array, "nonexistent"));
        assertNull(ArrayUtils.removeElement((String[]) null, "a"));

        int[] intArray = new int[] { 10, 20, 30 };
        assertArrayEquals(new int[] { 10, 30 }, ArrayUtils.remove(intArray, 1));
        assertArrayEquals(new int[] { 10, 30 }, ArrayUtils.removeElement(intArray, 20));
    }

    // Tests remove method with out-of-bounds index throwing exception
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemove_indexOutOfBounds_throwsIndexOutOfBoundsException() {
        ArrayUtils.remove(new String[] { "a" }, 2);
    }
}