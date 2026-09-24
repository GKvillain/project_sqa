package org.apache.commons.math.stat.descriptive;

import org.apache.commons.math.exception.MathIllegalStateException;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.stat.descriptive.moment.GeometricMean;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.Variance;
import org.apache.commons.math.stat.descriptive.rank.Max;
import org.apache.commons.math.stat.descriptive.rank.Min;
import org.apache.commons.math.stat.descriptive.summary.Sum;
import org.apache.commons.math.stat.descriptive.summary.SumOfLogs;
import org.apache.commons.math.stat.descriptive.summary.SumOfSquares;
import org.apache.commons.math.util.FastMath;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SummaryStatisticsTest {

    private static final double TOLERANCE = 1E-12;

    // Tests defect where custom Mean implementation instance is not incremented
    @Test
    public void testSetMeanImpl_customMeanInstance_computesCorrectly() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.setMeanImpl(new Mean());
        stats.addValue(1.0);
        stats.addValue(2.0);
        stats.addValue(3.0);
        stats.addValue(4.0);

        assertEquals(2.5, stats.getMean(), TOLERANCE);
    }

    // Tests defect where custom Variance implementation instance is not incremented
    @Test
    public void testSetVarianceImpl_customVarianceInstance_computesCorrectly() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.setVarianceImpl(new Variance());
        stats.addValue(1.0);
        stats.addValue(2.0);
        stats.addValue(3.0);
        stats.addValue(4.0);

        assertEquals(1.6666666666666667, stats.getVariance(), TOLERANCE);
    }

    // Tests defect where custom GeometricMean implementation instance is not incremented
    @Test
    public void testSetGeoMeanImpl_customGeometricMeanInstance_computesCorrectly() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.setGeoMeanImpl(new GeometricMean());
        stats.addValue(1.0);
        stats.addValue(2.0);
        stats.addValue(4.0);
        stats.addValue(8.0);

        assertEquals(FastMath.pow(64.0, 0.25), stats.getGeometricMean(), TOLERANCE);
    }

    // Tests basic statistics calculation with default implementations
    @Test
    public void testAddValue_defaultImplementations_returnsExpectedValues() {
        SummaryStatistics stats = new SummaryStatistics();
        assertTrue(Double.isNaN(stats.getMean()));
        assertTrue(Double.isNaN(stats.getVariance()));
        assertTrue(Double.isNaN(stats.getStandardDeviation()));
        assertTrue(Double.isNaN(stats.getMin()));
        assertTrue(Double.isNaN(stats.getMax()));
        assertTrue(Double.isNaN(stats.getSum()));
        assertTrue(Double.isNaN(stats.getSumsq()));
        assertTrue(Double.isNaN(stats.getSumOfLogs()));
        assertTrue(Double.isNaN(stats.getGeometricMean()));
        assertTrue(Double.isNaN(stats.getSecondMoment()));
        assertEquals(0, stats.getN());

        stats.addValue(1.0);
        stats.addValue(2.0);
        stats.addValue(3.0);
        stats.addValue(4.0);
        stats.addValue(5.0);

        assertEquals(5, stats.getN());
        assertEquals(1.0, stats.getMin(), TOLERANCE);
        assertEquals(5.0, stats.getMax(), TOLERANCE);
        assertEquals(15.0, stats.getSum(), TOLERANCE);
        assertEquals(55.0, stats.getSumsq(), TOLERANCE);
        assertEquals(3.0, stats.getMean(), TOLERANCE);
        assertEquals(2.5, stats.getVariance(), TOLERANCE);
        assertEquals(2.0, stats.getPopulationVariance(), TOLERANCE);
        assertEquals(FastMath.sqrt(2.5), stats.getStandardDeviation(), TOLERANCE);
        assertEquals(10.0, stats.getSecondMoment(), TOLERANCE);
    }

    // Tests standard deviation boundary condition when N=0 and N=1
    @Test
    public void testStandardDeviation_singleValue_returnsZero() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(10.0);

        assertEquals(1, stats.getN());
        assertEquals(0.0, stats.getStandardDeviation(), TOLERANCE);
        assertEquals(0.0, stats.getSecondMoment(), TOLERANCE);
    }

    // Tests clearing statistics and resetting internal state
    @Test
    public void testClear_afterAddingValues_resetsAllStatistics() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.setMeanImpl(new Mean());
        stats.setVarianceImpl(new Variance());
        stats.addValue(10.0);
        stats.addValue(20.0);

        stats.clear();

        assertEquals(0, stats.getN());
        assertTrue(Double.isNaN(stats.getMean()));
        assertTrue(Double.isNaN(stats.getVariance()));
        assertTrue(Double.isNaN(stats.getSum()));
    }

    // Tests exception thrown when setting statistic implementation after values are added
    @Test(expected = MathIllegalStateException.class)
    public void testSetMeanImpl_afterValuesAdded_throwsMathIllegalStateException() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(1.0);
        stats.setMeanImpl(new Mean());
    }

    // Tests exception thrown when setting variance implementation after values are added
    @Test(expected = MathIllegalStateException.class)
    public void testSetVarianceImpl_afterValuesAdded_throwsMathIllegalStateException() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(1.0);
        stats.setVarianceImpl(new Variance());
    }

    // Tests exception thrown when setting geoMean implementation after values are added
    @Test(expected = MathIllegalStateException.class)
    public void testSetGeoMeanImpl_afterValuesAdded_throwsMathIllegalStateException() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(1.0);
        stats.setGeoMeanImpl(new GeometricMean());
    }

    // Tests exception thrown when setting sum implementation after values are added
    @Test(expected = MathIllegalStateException.class)
    public void testSetSumImpl_afterValuesAdded_throwsMathIllegalStateException() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(1.0);
        stats.setSumImpl(new Sum());
    }

    // Tests exception thrown when setting sumsq implementation after values are added
    @Test(expected = MathIllegalStateException.class)
    public void testSetSumsqImpl_afterValuesAdded_throwsMathIllegalStateException() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(1.0);
        stats.setSumsqImpl(new SumOfSquares());
    }

    // Tests exception thrown when setting min implementation after values are added
    @Test(expected = MathIllegalStateException.class)
    public void testSetMinImpl_afterValuesAdded_throwsMathIllegalStateException() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(1.0);
        stats.setMinImpl(new Min());
    }

    // Tests exception thrown when setting max implementation after values are added
    @Test(expected = MathIllegalStateException.class)
    public void testSetMaxImpl_afterValuesAdded_throwsMathIllegalStateException() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(1.0);
        stats.setMaxImpl(new Max());
    }

    // Tests setter for sumLog implementation and its effect on geoMean
    @Test
    public void testSetSumLogImpl_validImpl_updatesSumLog() {
        SummaryStatistics stats = new SummaryStatistics();
        SumOfLogs sumLog = new SumOfLogs();
        stats.setSumLogImpl(sumLog);
        assertEquals(sumLog, stats.getSumLogImpl());

        stats.addValue(2.0);
        stats.addValue(8.0);
        assertEquals(4.0, stats.getGeometricMean(), TOLERANCE);
    }

    // Tests getters for implementations
    @Test
    public void testGetImpl_defaultImplementations_returnsNotNull() {
        SummaryStatistics stats = new SummaryStatistics();
        assertNotNull(stats.getSumImpl());
        assertNotNull(stats.getSumsqImpl());
        assertNotNull(stats.getMinImpl());
        assertNotNull(stats.getMaxImpl());
        assertNotNull(stats.getSumLogImpl());
        assertNotNull(stats.getGeoMeanImpl());
        assertNotNull(stats.getMeanImpl());
        assertNotNull(stats.getVarianceImpl());
    }

    // Tests getSummary method returning StatisticalSummaryValues
    @Test
    public void testGetSummary_populatedStats_returnsMatchingStatisticalSummary() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(2.0);
        stats.addValue(4.0);

        StatisticalSummary summary = stats.getSummary();
        assertEquals(stats.getMean(), summary.getMean(), TOLERANCE);
        assertEquals(stats.getVariance(), summary.getVariance(), TOLERANCE);
        assertEquals(stats.getN(), summary.getN());
        assertEquals(stats.getMax(), summary.getMax(), TOLERANCE);
        assertEquals(stats.getMin(), summary.getMin(), TOLERANCE);
        assertEquals(stats.getSum(), summary.getSum(), TOLERANCE);
    }

    // Tests copy constructor and copy method
    @Test
    public void testCopy_validInstance_createsEqualDeepCopy() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(1.0);
        stats.addValue(2.0);
        stats.addValue(3.0);

        SummaryStatistics copy = stats.copy();
        assertEquals(stats, copy);
        assertEquals(stats.hashCode(), copy.hashCode());

        SummaryStatistics copyFromConstructor = new SummaryStatistics(stats);
        assertEquals(stats, copyFromConstructor);
    }

    // Tests copy with null source or destination throws NullArgumentException
    @Test(expected = NullArgumentException.class)
    public void testCopy_nullSource_throwsNullArgumentException() {
        SummaryStatistics.copy(null, new SummaryStatistics());
    }

    // Tests copy with null destination throws NullArgumentException
    @Test(expected = NullArgumentException.class)
    public void testCopy_nullDestination_throwsNullArgumentException() {
        SummaryStatistics.copy(new SummaryStatistics(), null);
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_variousCases_returnsExpectedResults() {
        SummaryStatistics stats1 = new SummaryStatistics();
        SummaryStatistics stats2 = new SummaryStatistics();

        assertTrue(stats1.equals(stats1));
        assertFalse(stats1.equals(null));
        assertFalse(stats1.equals("Not a SummaryStatistics"));
        assertTrue(stats1.equals(stats2));
        assertEquals(stats1.hashCode(), stats2.hashCode());

        stats1.addValue(1.0);
        assertFalse(stats1.equals(stats2));

        stats2.addValue(1.0);
        assertTrue(stats1.equals(stats2));
        assertEquals(stats1.hashCode(), stats2.hashCode());
    }

    // Tests toString method generating report string
    @Test
    public void testToString_populatedStats_containsExpectedReport() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(1.0);
        stats.addValue(2.0);

        String str = stats.toString();
        assertNotNull(str);
        assertTrue(str.contains("SummaryStatistics:"));
        assertTrue(str.contains("n: 2"));
    }
}