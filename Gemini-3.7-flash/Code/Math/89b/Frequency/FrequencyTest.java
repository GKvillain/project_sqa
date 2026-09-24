package org.apache.commons.math.stat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for {@link Frequency}.
 */
public class FrequencyTest {

    private Frequency f;

    @Before
    public void setUp() {
        f = new Frequency();
    }

    // Tests defect Math-89: addValue(Object) with non-Comparable object should throw IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAddValue_nonComparableObject_throwsIllegalArgumentException() {
        Object nonComparable = new Object();
        f.addValue(nonComparable);
    }

    // Tests addValue and count/pct methods with int and Integer values
    @Test
    public void testAddValue_intValues_returnsCorrectCountsAndPercentages() {
        f.addValue(1);
        f.addValue(Integer.valueOf(1));
        f.addValue(2);
        f.addValue(3);

        assertEquals(4, f.getSumFreq());
        assertEquals(2, f.getCount(1));
        assertEquals(2, f.getCount(Integer.valueOf(1)));
        assertEquals(1, f.getCount(2));
        assertEquals(1, f.getCount(3));
        assertEquals(0, f.getCount(4));

        assertEquals(0.5, f.getPct(1), 1e-6);
        assertEquals(0.25, f.getPct(2), 1e-6);
        assertEquals(0.0, f.getPct(4), 1e-6);
    }

    // Tests addValue and getCount/getPct/getCumFreq with long values
    @Test
    public void testAddValue_longValues_returnsCorrectCountsAndPercentages() {
        f.addValue(10L);
        f.addValue(20L);
        f.addValue(20L);

        assertEquals(3, f.getSumFreq());
        assertEquals(1, f.getCount(10L));
        assertEquals(2, f.getCount(20L));
        assertEquals(0, f.getCount(30L));

        assertEquals(1.0 / 3.0, f.getPct(10L), 1e-6);
        assertEquals(2.0 / 3.0, f.getPct(20L), 1e-6);
    }

    // Tests addValue and getCount/getPct with char values
    @Test
    public void testAddValue_charValues_returnsCorrectCountsAndPercentages() {
        f.addValue('a');
        f.addValue('b');
        f.addValue('a');

        assertEquals(3, f.getSumFreq());
        assertEquals(2, f.getCount('a'));
        assertEquals(1, f.getCount('b'));
        assertEquals(0, f.getCount('c'));

        assertEquals(2.0 / 3.0, f.getPct('a'), 1e-6);
        assertEquals(1.0 / 3.0, f.getPct('b'), 1e-6);
    }

    // Tests mixing incompatible types throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAddValue_incompatibleTypes_throwsIllegalArgumentException() {
        f.addValue(1);
        f.addValue("string");
    }

    // Tests cumulative frequency and percentage methods
    @Test
    public void testGetCumFreqAndCumPct_variousValues_returnsCorrectCumulativeStatistics() {
        f.addValue(10);
        f.addValue(20);
        f.addValue(30);

        // Value less than lowest entry
        assertEquals(0, f.getCumFreq(5));
        assertEquals(0.0, f.getCumPct(5), 1e-6);

        // Value equal to first entry
        assertEquals(1, f.getCumFreq(10));
        assertEquals(1.0 / 3.0, f.getCumPct(10), 1e-6);

        // Value between entries
        assertEquals(1, f.getCumFreq(15));
        assertEquals(1.0 / 3.0, f.getCumPct(15), 1e-6);

        // Value equal to middle entry
        assertEquals(2, f.getCumFreq(20));
        assertEquals(2.0 / 3.0, f.getCumPct(20), 1e-6);

        // Value equal to last entry
        assertEquals(3, f.getCumFreq(30));
        assertEquals(1.0, f.getCumPct(30), 1e-6);

        // Value greater than last entry
        assertEquals(3, f.getCumFreq(40));
        assertEquals(1.0, f.getCumPct(40), 1e-6);
    }

    // Tests getCumFreq for char values
    @Test
    public void testGetCumFreq_charValues_returnsCorrectResult() {
        f.addValue('b');
        f.addValue('d');

        assertEquals(0, f.getCumFreq('a'));
        assertEquals(1, f.getCumFreq('b'));
        assertEquals(1, f.getCumFreq('c'));
        assertEquals(2, f.getCumFreq('d'));
        assertEquals(2, f.getCumFreq('e'));

        assertEquals(0.0, f.getCumPct('a'), 1e-6);
        assertEquals(0.5, f.getCumPct('b'), 1e-6);
        assertEquals(1.0, f.getCumPct('d'), 1e-6);
    }

    // Tests empty frequency table behavior
    @Test
    public void testEmpty_returnsExpectedEmptyValues() {
        assertEquals(0, f.getSumFreq());
        assertEquals(0, f.getCount(1));
        assertEquals(0, f.getCount('a'));
        assertEquals(0, f.getCumFreq(1));
        assertTrue(Double.isNaN(f.getPct(1)));
        assertTrue(Double.isNaN(f.getPct('a')));
        assertTrue(Double.isNaN(f.getCumPct(1)));
        assertTrue(Double.isNaN(f.getCumPct('a')));
    }

    // Tests clear method
    @Test
    public void testClear_populatedTable_resetsFrequencyTable() {
        f.addValue(1);
        f.addValue(2);
        assertEquals(2, f.getSumFreq());

        f.clear();

        assertEquals(0, f.getSumFreq());
        assertEquals(0, f.getCount(1));
    }

    // Tests custom comparator constructor
    @Test
    public void testConstructor_customComparator_ordersValuesCorrectly() {
        Comparator<String> caseInsensitive = String.CASE_INSENSITIVE_ORDER;
        Frequency customFreq = new Frequency(caseInsensitive);
        customFreq.addValue("abc");
        customFreq.addValue("ABC");

        assertEquals(2, customFreq.getSumFreq());
        assertEquals(2, customFreq.getCount("abc"));
        assertEquals(2, customFreq.getCount("ABC"));
        assertEquals(2, customFreq.getCumFreq("abc"));
    }

    // Tests valuesIterator returns elements in order
    @Test
    public void testValuesIterator_returnsAddedValues() {
        f.addValue(2);
        f.addValue(1);
        f.addValue(3);

        Iterator<?> it = f.valuesIterator();
        assertTrue(it.hasNext());
        assertEquals(1L, it.next());
        assertTrue(it.hasNext());
        assertEquals(2L, it.next());
        assertTrue(it.hasNext());
        assertEquals(3L, it.next());
        assertFalse(it.hasNext());
    }

    // Tests toString method contains formatted output
    @Test
    public void testToString_populatedTable_returnsNonEmptyString() {
        f.addValue(1);
        f.addValue(2);
        String str = f.toString();

        assertTrue(str.startsWith("Value \t Freq. \t Pct. \t Cum Pct. \n"));
        assertTrue(str.contains("1\t1\t"));
        assertTrue(str.contains("2\t1\t"));
    }

    // Tests getCount and getCumFreq with non-comparable query object
    @Test
    public void testGetCountAndGetCumFreq_incompatibleType_returnsZero() {
        f.addValue(1);
        f.addValue(2);

        assertEquals(0, f.getCount("string"));
        assertEquals(0, f.getCumFreq("string"));
        assertEquals(0.0, f.getCumPct("string"), 1e-6);
    }
}