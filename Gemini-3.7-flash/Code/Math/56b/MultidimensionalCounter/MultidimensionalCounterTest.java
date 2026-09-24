package org.apache.commons.math.util;

import org.apache.commons.math.exception.DimensionMismatchException;
import org.apache.commons.math.exception.NotStrictlyPositiveException;
import org.apache.commons.math.exception.OutOfRangeException;
import org.junit.Test;
import static org.junit.Assert.*;

public class MultidimensionalCounterTest {

    // Tests normal initialization and basic getter properties
    @Test
    public void testConstructor_validDimensions_setsPropertiesCorrectly() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3, 4);
        assertEquals(3, counter.getDimension());
        assertEquals(24, counter.getSize());
        assertArrayEquals(new int[]{2, 3, 4}, counter.getSizes());
    }

    // Tests constructor throwing NotStrictlyPositiveException for zero or negative sizes
    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructor_zeroOrNegativeSize_throwsNotStrictlyPositiveException() {
        new MultidimensionalCounter(2, 0, 4);
    }

    // Tests 1D multidimensional counter conversion
    @Test
    public void testGetCounts_oneDimension_returnsExpectedIndices() {
        MultidimensionalCounter counter = new MultidimensionalCounter(5);
        assertEquals(1, counter.getDimension());
        assertEquals(5, counter.getSize());
        for (int i = 0; i < 5; i++) {
            assertArrayEquals(new int[]{i}, counter.getCounts(i));
            assertEquals(i, counter.getCount(i));
        }
    }

    // Tests multidimensional to unidimensional conversion (getCount)
    @Test
    public void testGetCount_validCoordinates_returnsCorrectIndex() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 4, 3);
        assertEquals(0, counter.getCount(0, 0, 0));
        assertEquals(1, counter.getCount(0, 0, 1));
        assertEquals(2, counter.getCount(0, 0, 2));
        assertEquals(3, counter.getCount(0, 1, 0));
        assertEquals(12, counter.getCount(1, 0, 0));
        assertEquals(23, counter.getCount(1, 3, 2));
    }

    // Tests unidimensional to multidimensional conversion (getCounts) - defect detection Math-56
    @Test
    public void testGetCounts_validIndex_returnsCorrectCoordinates() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3, 4);
        int total = counter.getSize();
        for (int i = 0; i < total; i++) {
            int[] multidimensional = counter.getCounts(i);
            int unidimensional = counter.getCount(multidimensional);
            assertEquals(i, unidimensional);
        }
    }

    // Tests specific coordinate conversion for last dimension index
    @Test
    public void testGetCounts_multiDimensionLastIndex_returnsCorrectCoordinates() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 4, 5);
        assertArrayEquals(new int[]{0, 0, 3}, counter.getCounts(3));
        assertArrayEquals(new int[]{0, 0, 4}, counter.getCounts(4));
        assertArrayEquals(new int[]{0, 1, 0}, counter.getCounts(5));
        assertArrayEquals(new int[]{0, 1, 4}, counter.getCounts(9));
        assertArrayEquals(new int[]{1, 3, 4}, counter.getCounts(39));
    }

    // Tests getCounts with index out of lower bound
    @Test(expected = OutOfRangeException.class)
    public void testGetCounts_negativeIndex_throwsOutOfRangeException() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        counter.getCounts(-1);
    }

    // Tests getCounts with index out of upper bound
    @Test(expected = OutOfRangeException.class)
    public void testGetCounts_indexTooLarge_throwsOutOfRangeException() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        counter.getCounts(6);
    }

    // Tests getCount with dimension mismatch
    @Test(expected = DimensionMismatchException.class)
    public void testGetCount_dimensionMismatch_throwsDimensionMismatchException() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        counter.getCount(0, 1, 2);
    }

    // Tests getCount with coordinate below zero
    @Test(expected = OutOfRangeException.class)
    public void testGetCount_coordinateBelowZero_throwsOutOfRangeException() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        counter.getCount(0, -1);
    }

    // Tests getCount with coordinate exceeding dimension size
    @Test(expected = OutOfRangeException.class)
    public void testGetCount_coordinateExceedsSize_throwsOutOfRangeException() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        counter.getCount(0, 3);
    }

    // Tests iterator traversal and consistency with getCounts and getCount
    @Test
    public void testIterator_standardIteration_iteratesAllElementsCorrectly() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3, 2);
        MultidimensionalCounter.Iterator iter = counter.iterator();

        int count = 0;
        while (iter.hasNext()) {
            Integer nextVal = iter.next();
            assertEquals(Integer.valueOf(count), nextVal);
            assertEquals(count, iter.getCount());
            int[] expectedCounts = counter.getCounts(count);
            assertArrayEquals(expectedCounts, iter.getCounts());
            for (int d = 0; d < counter.getDimension(); d++) {
                assertEquals(expectedCounts[d], iter.getCount(d));
            }
            count++;
        }
        assertEquals(counter.getSize(), count);
        assertFalse(iter.hasNext());
    }

    // Tests iterator remove operation throwing UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testIteratorRemove_invoked_throwsUnsupportedOperationException() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 2);
        MultidimensionalCounter.Iterator iter = counter.iterator();
        iter.remove();
    }

    // Tests toString representation of counter
    @Test
    public void testToString_validCounter_returnsFormattedString() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3);
        String str = counter.toString();
        assertNotNull(str);
        assertTrue(str.contains("[") && str.contains("]"));
    }
}