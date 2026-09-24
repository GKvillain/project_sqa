package org.jfree.data.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import org.junit.Before;
import org.junit.Test;

public class TimePeriodValuesTest {

    private TimePeriodValues series;

    @Before
    public void setUp() {
        this.series = new TimePeriodValues("Test Series");
    }

    // Tests constructor initialization and default values
    @Test
    public void testConstructor_defaultValues_initializedCorrectly() {
        assertEquals("Test Series", this.series.getKey());
        assertEquals("Time", this.series.getDomainDescription());
        assertEquals("Value", this.series.getRangeDescription());
        assertEquals(0, this.series.getItemCount());
        assertEquals(-1, this.series.getMinStartIndex());
        assertEquals(-1, this.series.getMaxStartIndex());
        assertEquals(-1, this.series.getMinMiddleIndex());
        assertEquals(-1, this.series.getMaxMiddleIndex());
        assertEquals(-1, this.series.getMinEndIndex());
        assertEquals(-1, this.series.getMaxEndIndex());
    }

    // Tests custom constructor with domain and range descriptions
    @Test
    public void testConstructor_customDomainAndRange_initializedCorrectly() {
        TimePeriodValues customSeries = new TimePeriodValues("Series 2", "Custom Domain", "Custom Range");
        assertEquals("Series 2", customSeries.getKey());
        assertEquals("Custom Domain", customSeries.getDomainDescription());
        assertEquals("Custom Range", customSeries.getRangeDescription());
    }

    // Tests setting domain and range descriptions
    @Test
    public void testSetDomainAndRangeDescription_validStrings_updatesValues() {
        this.series.setDomainDescription("New Domain");
        assertEquals("New Domain", this.series.getDomainDescription());

        this.series.setRangeDescription("New Range");
        assertEquals("New Range", this.series.getRangeDescription());

        this.series.setDomainDescription(null);
        assertNull(this.series.getDomainDescription());

        this.series.setRangeDescription(null);
        assertNull(this.series.getRangeDescription());
    }

    // Tests adding null item throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAdd_nullItem_throwsException() {
        this.series.add((TimePeriodValue) null);
    }

    // Tests adding items and accessing data items, time periods, and values
    @Test
    public void testAdd_validItems_retrievesCorrectData() {
        TimePeriod p1 = new SimpleTimePeriod(new Date(100L), new Date(200L));
        TimePeriod p2 = new SimpleTimePeriod(new Date(300L), new Date(400L));

        this.series.add(p1, 10.5);
        this.series.add(p2, Double.valueOf(20.5));

        assertEquals(2, this.series.getItemCount());
        assertEquals(p1, this.series.getTimePeriod(0));
        assertEquals(Double.valueOf(10.5), this.series.getValue(0));
        assertEquals(p2, this.series.getTimePeriod(1));
        assertEquals(Double.valueOf(20.5), this.series.getValue(1));

        TimePeriodValue tpv = this.series.getDataItem(0);
        assertEquals(p1, tpv.getPeriod());
        assertEquals(Double.valueOf(10.5), tpv.getValue());
    }

    // Tests update method modifies existing item value
    @Test
    public void testUpdate_existingIndex_updatesValue() {
        TimePeriod p1 = new SimpleTimePeriod(new Date(100L), new Date(200L));
        this.series.add(p1, 10.0);
        this.series.update(0, Double.valueOf(25.0));

        assertEquals(Double.valueOf(25.0), this.series.getValue(0));
    }

    // Tests bound calculation for maxMiddleIndex and other bounds (Bug Chart-7)
    @Test
    public void testGetMaxMiddleIndex_multiplePeriods_returnsCorrectIndex() {
        // Period 1: start 100, end 500 -> middle = 300
        TimePeriod p1 = new SimpleTimePeriod(new Date(100L), new Date(500L));
        // Period 2: start 0, end 200 -> middle = 100
        TimePeriod p2 = new SimpleTimePeriod(new Date(0L), new Date(200L));
        // Period 3: start 200, end 700 -> middle = 450
        TimePeriod p3 = new SimpleTimePeriod(new Date(200L), new Date(700L));

        this.series.add(p1, 1.0);
        this.series.add(p2, 2.0);
        this.series.add(p3, 3.0);

        assertEquals(1, this.series.getMinStartIndex());
        assertEquals(2, this.series.getMaxStartIndex());
        assertEquals(1, this.series.getMinMiddleIndex());
        assertEquals(2, this.series.getMaxMiddleIndex());
        assertEquals(1, this.series.getMinEndIndex());
        assertEquals(2, this.series.getMaxEndIndex());
    }

    // Tests bounds recalculation when bounds change across sequence of additions
    @Test
    public void testBounds_intermediateMiddleValues_trackedCorrectly() {
        TimePeriod p1 = new SimpleTimePeriod(new Date(100L), new Date(200L)); // mid 150
        TimePeriod p2 = new SimpleTimePeriod(new Date(50L), new Date(100L));   // mid 75
        TimePeriod p3 = new SimpleTimePeriod(new Date(120L), new Date(180L)); // mid 150 (not greater)

        this.series.add(p1, 1.0);
        this.series.add(p2, 2.0);
        this.series.add(p3, 3.0);

        assertEquals(1, this.series.getMinStartIndex());
        assertEquals(2, this.series.getMaxStartIndex());
        assertEquals(1, this.series.getMinMiddleIndex());
        assertEquals(0, this.series.getMaxMiddleIndex());
        assertEquals(1, this.series.getMinEndIndex());
        assertEquals(0, this.series.getMaxEndIndex());
    }

    // Tests delete method removes items and recalculates bounds
    @Test
    public void testDelete_subsetOfItems_updatesDataAndBounds() {
        TimePeriod p1 = new SimpleTimePeriod(new Date(100L), new Date(200L));
        TimePeriod p2 = new SimpleTimePeriod(new Date(300L), new Date(400L));
        TimePeriod p3 = new SimpleTimePeriod(new Date(500L), new Date(600L));

        this.series.add(p1, 1.0);
        this.series.add(p2, 2.0);
        this.series.add(p3, 3.0);

        this.series.delete(0, 1);

        assertEquals(1, this.series.getItemCount());
        assertEquals(p3, this.series.getTimePeriod(0));
        assertEquals(0, this.series.getMinStartIndex());
        assertEquals(0, this.series.getMaxStartIndex());
        assertEquals(0, this.series.getMinMiddleIndex());
        assertEquals(0, this.series.getMaxMiddleIndex());
        assertEquals(0, this.series.getMinEndIndex());
        assertEquals(0, this.series.getMaxEndIndex());
    }

    // Tests equals and hashCode methods
    @Test
    public void testEqualsAndHashCode_sameAndDifferentObjects_worksCorrectly() {
        TimePeriodValues s1 = new TimePeriodValues("Series", "D", "R");
        TimePeriodValues s2 = new TimePeriodValues("Series", "D", "R");

        assertTrue(s1.equals(s1));
        assertTrue(s1.equals(s2));
        assertEquals(s1.hashCode(), s2.hashCode());

        assertFalse(s1.equals(null));
        assertFalse(s1.equals("Not a TimePeriodValues"));

        s2.setDomainDescription("Other D");
        assertFalse(s1.equals(s2));

        s2.setDomainDescription("D");
        s2.setRangeDescription("Other R");
        assertFalse(s1.equals(s2));

        s2.setRangeDescription("R");
        TimePeriod p1 = new SimpleTimePeriod(new Date(10L), new Date(20L));
        s1.add(p1, 100.0);
        assertFalse(s1.equals(s2));

        s2.add(p1, 100.0);
        assertTrue(s1.equals(s2));
        assertEquals(s1.hashCode(), s2.hashCode());

        TimePeriod p2 = new SimpleTimePeriod(new Date(30L), new Date(40L));
        s2.add(p2, 200.0);
        assertFalse(s1.equals(s2));
    }

    // Tests clone and createCopy methods
    @Test
    public void testClone_populatedSeries_createsIndependentCopy() throws CloneNotSupportedException {
        TimePeriod p1 = new SimpleTimePeriod(new Date(100L), new Date(200L));
        TimePeriod p2 = new SimpleTimePeriod(new Date(300L), new Date(400L));

        this.series.add(p1, 1.0);
        this.series.add(p2, 2.0);

        TimePeriodValues clone = (TimePeriodValues) this.series.clone();
        assertTrue(this.series.equals(clone));
        assertEquals(this.series.getItemCount(), clone.getItemCount());

        clone.update(0, Double.valueOf(99.0));
        assertFalse(this.series.equals(clone));
    }

    // Tests createCopy with empty series
    @Test
    public void testCreateCopy_emptySeries_returnsEmptyCopy() throws CloneNotSupportedException {
        TimePeriodValues copy = this.series.createCopy(0, -1);
        assertNotNull(copy);
        assertEquals(0, copy.getItemCount());
    }

    // Tests createCopy with valid subset range
    @Test
    public void testCreateCopy_subsetRange_returnsSubset() throws CloneNotSupportedException {
        TimePeriod p1 = new SimpleTimePeriod(new Date(100L), new Date(200L));
        TimePeriod p2 = new SimpleTimePeriod(new Date(300L), new Date(400L));
        TimePeriod p3 = new SimpleTimePeriod(new Date(500L), new Date(600L));

        this.series.add(p1, 10.0);
        this.series.add(p2, 20.0);
        this.series.add(p3, 30.0);

        TimePeriodValues subset = this.series.createCopy(1, 2);
        assertEquals(2, subset.getItemCount());
        assertEquals(p2, subset.getTimePeriod(0));
        assertEquals(p3, subset.getTimePeriod(1));
    }
}