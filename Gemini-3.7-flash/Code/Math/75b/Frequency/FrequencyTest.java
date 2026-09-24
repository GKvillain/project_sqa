package org.apache.commons.math.stat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;
import java.util.Iterator;

import org.apache.commons.math.MathRuntimeException;
import org.junit.Before;
import org.junit.Test;

public class FrequencyTest {

    private Frequency f;

    @Before
    public void setUp() {
        f = new Frequency();
    }

    // Tests defect Math-75: getPct(Object) should return individual pct, not cumulative pct
    @Test
    public void testGetPct_objectArgument_returnsPercentageNotCumulative() {
        f.addValue(1);
        f.addValue(2);
        f.addValue(3);
        // Total sum = 3, count for 3 is 1 -> pct is 1/3 (0.333...), cumPct is 1.0
        Object target = Integer.valueOf(3);
        assertEquals(1.0 / 3.0, f.getPct(target), 1e-6);
    }

    // Tests basic addValue and count retrievals for integral types
    @Test
    public void testAddValue_integralTypes_countsMergedCorrectly() {
        f.addValue(1);
        f.addValue(1L);
        f.addValue(Integer.valueOf(1));
        f.addValue(Long.valueOf(1L));
        f.addValue(2);

        assertEquals(5, f.getSumFreq());
        assertEquals(4, f.getCount(1));
        assertEquals(4, f.getCount(1L));
        assertEquals(4, f.getCount(Integer.valueOf(1)));
        assertEquals(4, f.getCount(Long.valueOf(1L)));
        assertEquals(1, f.getCount(2));
        assertEquals(0, f.getCount(3));
    }

    // Tests adding char values
    @Test
    public void testAddValue_charValues_countsCorrectly() {
        f.addValue('a');
        f.addValue('b');
        f.addValue('a');

        assertEquals(3, f.getSumFreq());
        assertEquals(2, f.getCount('a'));
        assertEquals(1, f.getCount('b'));
        assertEquals(0, f.getCount('c'));
    }

    // Tests getPct for various types and boundary when empty
    @Test
    public void testGetPct_emptyAndPopulated_returnsExpectedValues() {
        assertTrue(Double.isNaN(f.getPct(1)));
        assertTrue(Double.isNaN(f.getPct(1L)));
        assertTrue(Double.isNaN(f.getPct('a')));

        f.addValue(1);
        f.addValue(2);

        assertEquals(0.5, f.getPct(1), 1e-6);
        assertEquals(0.5, f.getPct(2L), 1e-6);
        assertEquals(0.0, f.getPct(3), 1e-6);
    }

    // Tests cumulative frequency calculations
    @Test
    public void testGetCumFreq_variousValues_returnsCumulativeCounts() {
        assertEquals(0, f.getCumFreq(1));

        f.addValue(10);
        f.addValue(20);
        f.addValue(30);
        f.addValue(20);

        // Sum = 4; values: 10 (1), 20 (2), 30 (1)
        assertEquals(0, f.getCumFreq(5));   // Less than lowest
        assertEquals(1, f.getCumFreq(10));  // Equal to lowest
        assertEquals(3, f.getCumFreq(20));  // Intermediate
        assertEquals(3, f.getCumFreq(25));  // Between existing keys
        assertEquals(4, f.getCumFreq(30));  // Equal to highest
        assertEquals(4, f.getCumFreq(35));  // Greater than highest
    }

    // Tests cumulative percentage calculations
    @Test
    public void testGetCumPct_emptyAndPopulated_returnsExpectedProportions() {
        assertTrue(Double.isNaN(f.getCumPct(1)));

        f.addValue(10);
        f.addValue(20);
        f.addValue(30);
        f.addValue(20);

        assertEquals(0.0, f.getCumPct(5), 1e-6);
        assertEquals(0.25, f.getCumPct(10), 1e-6);
        assertEquals(0.75, f.getCumPct(20), 1e-6);
        assertEquals(0.75, f.getCumPct(25), 1e-6);
        assertEquals(1.0, f.getCumPct(30), 1e-6);
        assertEquals(1.0, f.getCumPct(35), 1e-6);
        assertEquals(0.75, f.getCumPct((Object) Long.valueOf(20)), 1e-6);
    }

    // Tests custom comparator constructor
    @Test
    public void testConstructor_customComparator_ordersCorrectly() {
        Frequency customFreq = new Frequency(String.CASE_INSENSITIVE_ORDER);
        customFreq.addValue("a");
        customFreq.addValue("A");
        customFreq.addValue("b");

        assertEquals(3, customFreq.getSumFreq());
        assertEquals(2, customFreq.getCount("a"));
        assertEquals(2, customFreq.getCount("A"));
        assertEquals(1, customFreq.getCount("b"));
    }

    // Tests adding non-comparable object throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAddValue_nonComparableObject_throwsException() {
        Object nonComparable = new Object();
        f.addValue(nonComparable);
    }

    // Tests adding incompatible comparable types throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAddValue_incompatibleComparableTypes_throwsException() {
        f.addValue("string");
        f.addValue(10L);
    }

    // Tests getCount and getCumFreq when querying incompatible type
    @Test
    public void testGetCountAndCumFreq_incompatibleType_returnsZero() {
        f.addValue("abc");
        assertEquals(0, f.getCount(10L));
        assertEquals(0, f.getCumFreq(10L));
    }

    // Tests clear method
    @Test
    public void testClear_populatedTable_resetsAll() {
        f.addValue(1);
        f.addValue(2);
        f.clear();

        assertEquals(0, f.getSumFreq());
        assertEquals(0, f.getCount(1));
        assertTrue(Double.isNaN(f.getPct(1)));
    }

    // Tests valuesIterator method
    @Test
    public void testValuesIterator_returnsAllKeysInOrder() {
        f.addValue(30);
        f.addValue(10);
        f.addValue(20);

        Iterator<Comparable<?>> it = f.valuesIterator();
        assertNotNull(it);
        assertTrue(it.hasNext());
        assertEquals(Long.valueOf(10), it.next());
        assertEquals(Long.valueOf(20), it.next());
        assertEquals(Long.valueOf(30), it.next());
        assertFalse(it.hasNext());
    }

    // Tests toString method output
    @Test
    public void testToString_populatedTable_returnsFormattedString() {
        f.addValue("one");
        String str = f.toString();
        assertNotNull(str);
        assertTrue(str.contains("Value \t Freq. \t Pct. \t Cum Pct."));
        assertTrue(str.contains("one"));
    }

    // Tests equals and hashCode contracts
    @Test
    public void testEqualsAndHashCode_sameAndDifferentInstances() {
        Frequency f2 = new Frequency();
        assertTrue(f.equals(f));
        assertTrue(f.equals(f2));
        assertEquals(f.hashCode(), f2.hashCode());

        f.addValue(1);
        assertFalse(f.equals(f2));
        assertFalse(f.equals(null));
        assertFalse(f.equals("other type"));

        f2.addValue(1);
        assertTrue(f.equals(f2));
        assertEquals(f.hashCode(), f2.hashCode());
    }
}