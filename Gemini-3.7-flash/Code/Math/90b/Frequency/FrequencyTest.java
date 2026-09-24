package org.apache.commons.math.stat;

import java.util.Comparator;
import java.util.Iterator;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class FrequencyTest {

    private Frequency f;

    @Before
    public void setUp() {
        f = new Frequency();
    }

    // Tests empty frequency statistics
    @Test
    public void testEmpty_returnsInitialStatistics() {
        assertEquals(0L, f.getSumFreq());
        assertEquals(0L, f.getCount(1));
        assertEquals(0L, f.getCount('a'));
        assertEquals(0L, f.getCount("test"));
        assertTrue(Double.isNaN(f.getPct(1)));
        assertTrue(Double.isNaN(f.getPct('a')));
        assertTrue(Double.isNaN(f.getPct("test")));
        assertEquals(0L, f.getCumFreq(1));
        assertEquals(0L, f.getCumFreq('a'));
        assertEquals(0L, f.getCumFreq("test"));
        assertTrue(Double.isNaN(f.getCumPct(1)));
        assertTrue(Double.isNaN(f.getCumPct('a')));
        assertTrue(Double.isNaN(f.getCumPct("test")));
        assertNotNull(f.toString());
        assertFalse(f.valuesIterator().hasNext());
    }

    // Tests integral values equivalence (int, long, Integer, Long)
    @Test
    public void testAddValue_integralTypes_treatedAsLong() {
        f.addValue(1);
        f.addValue(Integer.valueOf(1));
        f.addValue(1L);
        f.addValue(Long.valueOf(1));
        f.addValue(2);

        assertEquals(5L, f.getSumFreq());
        assertEquals(4L, f.getCount(1));
        assertEquals(4L, f.getCount(1L));
        assertEquals(4L, f.getCount(Integer.valueOf(1)));
        assertEquals(4L, f.getCount(Long.valueOf(1)));
        assertEquals(1L, f.getCount(2));
        assertEquals(0L, f.getCount(3));

        assertEquals(0.8, f.getPct(1), 1e-6);
        assertEquals(0.8, f.getPct(1L), 1e-6);
        assertEquals(0.8, f.getPct(Integer.valueOf(1)), 1e-6);
        assertEquals(0.2, f.getPct(2), 1e-6);
        assertEquals(0.0, f.getPct(3), 1e-6);
    }

    // Tests cumulative frequency and percentage for integral values
    @Test
    public void testCumulativeMethods_integralValues_returnsExpectedResults() {
        f.addValue(10);
        f.addValue(20);
        f.addValue(30);
        f.addValue(20);

        assertEquals(4L, f.getSumFreq());

        // Below lowest value
        assertEquals(0L, f.getCumFreq(5));
        assertEquals(0.0, f.getCumPct(5), 1e-6);

        // At boundaries and between values
        assertEquals(1L, f.getCumFreq(10));
        assertEquals(0.25, f.getCumPct(10), 1e-6);

        assertEquals(1L, f.getCumFreq(15));
        assertEquals(0.25, f.getCumPct(15), 1e-6);

        assertEquals(3L, f.getCumFreq(20));
        assertEquals(0.75, f.getCumPct(20), 1e-6);

        assertEquals(3L, f.getCumFreq(25));
        assertEquals(0.75, f.getCumPct(25), 1e-6);

        // At and above highest value
        assertEquals(4L, f.getCumFreq(30));
        assertEquals(1.0, f.getCumPct(30), 1e-6);

        assertEquals(4L, f.getCumFreq(40));
        assertEquals(1.0, f.getCumPct(40), 1e-6);
    }

    // Tests char type handling
    @Test
    public void testAddValue_charType_tracksFrequenciesCorrectly() {
        f.addValue('a');
        f.addValue('b');
        f.addValue('b');
        f.addValue('c');

        assertEquals(4L, f.getSumFreq());
        assertEquals(1L, f.getCount('a'));
        assertEquals(2L, f.getCount('b'));
        assertEquals(1L, f.getCount('c'));
        assertEquals(0L, f.getCount('d'));

        assertEquals(0.25, f.getPct('a'), 1e-6);
        assertEquals(0.50, f.getPct('b'), 1e-6);

        assertEquals(0L, f.getCumFreq('0'));
        assertEquals(1L, f.getCumFreq('a'));
        assertEquals(3L, f.getCumFreq('b'));
        assertEquals(4L, f.getCumFreq('c'));
        assertEquals(4L, f.getCumFreq('z'));

        assertEquals(0.75, f.getCumPct('b'), 1e-6);
    }

    // Tests String objects and iteration
    @Test
    public void testAddValue_stringObjects_iteratesInNaturalOrder() {
        f.addValue("banana");
        f.addValue("apple");
        f.addValue("apple");
        f.addValue("cherry");

        assertEquals(4L, f.getSumFreq());
        assertEquals(2L, f.getCount("apple"));
        assertEquals(1L, f.getCount("banana"));
        assertEquals(1L, f.getCount("cherry"));

        Iterator iter = f.valuesIterator();
        assertTrue(iter.hasNext());
        assertEquals("apple", iter.next());
        assertTrue(iter.hasNext());
        assertEquals("banana", iter.next());
        assertTrue(iter.hasNext());
        assertEquals("cherry", iter.next());
        assertFalse(iter.hasNext());
    }

    // Tests clear functionality
    @Test
    public void testClear_afterAddingValues_resetsFrequency() {
        f.addValue(10);
        f.addValue(20);
        assertEquals(2L, f.getSumFreq());

        f.clear();
        assertEquals(0L, f.getSumFreq());
        assertEquals(0L, f.getCount(10));
        assertTrue(Double.isNaN(f.getPct(10)));
        assertFalse(f.valuesIterator().hasNext());
    }

    // Tests custom comparator constructor
    @Test
    public void testConstructor_withCustomComparator_ordersProperly() {
        Frequency caseInsensitiveFreq = new Frequency(String.CASE_INSENSITIVE_ORDER);
        caseInsensitiveFreq.addValue("a");
        caseInsensitiveFreq.addValue("A");
        caseInsensitiveFreq.addValue("B");

        assertEquals(3L, caseInsensitiveFreq.getSumFreq());
        assertEquals(2L, caseInsensitiveFreq.getCount("a"));
        assertEquals(2L, caseInsensitiveFreq.getCount("A"));
        assertEquals(1L, caseInsensitiveFreq.getCount("b"));
        assertEquals(1L, caseInsensitiveFreq.getCount("B"));
    }

    // Tests toString formatting with data
    @Test
    public void testToString_withValues_containsHeaderAndData() {
        f.addValue("one");
        f.addValue("two");
        String output = f.toString();

        assertNotNull(output);
        assertTrue(output.contains("Value \t Freq. \t Pct. \t Cum Pct. \n"));
        assertTrue(output.contains("one"));
        assertTrue(output.contains("two"));
    }

    // Tests getCount and getCumFreq with non-comparable type against existing entries
    @Test
    public void testGetCountAndCumFreq_incompatibleType_returnsZero() {
        f.addValue("test");
        assertEquals(0L, f.getCount(100));
        assertEquals(0L, f.getCumFreq(100));
        assertEquals(0.0, f.getCumPct(100), 1e-6);
    }

    // Tests adding incompatible type throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAddValue_incompatibleTypes_throwsIllegalArgumentException() {
        f.addValue("string");
        f.addValue(123);
    }

    // Tests adding non-comparable object throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAddValue_nonComparableObject_throwsIllegalArgumentException() {
        f.addValue(new Object());
    }
}