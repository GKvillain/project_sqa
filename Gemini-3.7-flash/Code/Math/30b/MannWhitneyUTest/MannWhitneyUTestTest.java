package org.apache.commons.math3.stat.inference;

import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.stat.ranking.NaNStrategy;
import org.apache.commons.math3.stat.ranking.TiesStrategy;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MannWhitneyUTestTest {

    // Tests Mann-Whitney U calculation with valid standard samples
    @Test
    public void testMannWhitneyU_standardSamples_returnsCorrectU() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        double[] x = new double[]{1.0, 2.0, 3.0};
        double[] y = new double[]{4.0, 5.0, 6.0};
        double u = test.mannWhitneyU(x, y);
        assertEquals(9.0, u, 1e-6);
    }

    // Tests Mann-Whitney U calculation with reversed samples
    @Test
    public void testMannWhitneyU_reversedSamples_returnsCorrectU() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        double[] x = new double[]{4.0, 5.0, 6.0};
        double[] y = new double[]{1.0, 2.0, 3.0};
        double u = test.mannWhitneyU(x, y);
        assertEquals(9.0, u, 1e-6);
    }

    // Tests Mann-Whitney U calculation with interleaved samples
    @Test
    public void testMannWhitneyU_interleavedSamples_returnsCorrectU() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        double[] x = new double[]{1.0, 3.0, 5.0};
        double[] y = new double[]{2.0, 4.0, 6.0};
        double u = test.mannWhitneyU(x, y);
        assertEquals(5.0, u, 1e-6);
    }

    // Tests custom NaNStrategy and TiesStrategy constructor
    @Test
    public void testMannWhitneyU_customStrategies_computesCorrectly() {
        MannWhitneyUTest test = new MannWhitneyUTest(NaNStrategy.MAXIMAL, TiesStrategy.SEQUENTIAL);
        double[] x = new double[]{1.0, 2.0, 3.0};
        double[] y = new double[]{2.0, 3.0, 4.0};
        double u = test.mannWhitneyU(x, y);
        assertTrue(u >= 0.0);
    }

    // Tests asymptotic p-value computation for standard samples
    @Test
    public void testMannWhitneyUTest_standardSamples_returnsPValue() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        double[] x = new double[]{1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = new double[]{6.0, 7.0, 8.0, 9.0, 10.0};
        double p = test.mannWhitneyUTest(x, y);
        assertTrue(p >= 0.0 && p <= 1.0);
        assertEquals(0.007963, p, 1e-3);
    }

    // Tests asymptotic p-value with identical distributions
    @Test
    public void testMannWhitneyUTest_identicalSamples_returnsHighPValue() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        double[] x = new double[]{1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = new double[]{1.0, 2.0, 3.0, 4.0, 5.0};
        double p = test.mannWhitneyUTest(x, y);
        assertTrue(p > 0.9);
    }

    // Tests large sample sizes to prevent integer overflow in variance calculation (Defects4J Math-30)
    @Test
    public void testMannWhitneyUTest_largeSamples_calculatesPValueCorrectly() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        int n1 = 1500;
        int n2 = 1500;
        double[] x = new double[n1];
        double[] y = new double[n2];
        for (int i = 0; i < n1; ++i) {
            x[i] = i;
        }
        for (int i = 0; i < n2; ++i) {
            y[i] = i + n1;
        }
        double pValue = test.mannWhitneyUTest(x, y);
        assertTrue("p-value should be non-negative and finite", !Double.isNaN(pValue) && pValue >= 0.0 && pValue <= 1.0);
        assertEquals(0.0, pValue, 1e-10);
    }

    // Tests null first sample for mannWhitneyU throws NullArgumentException
    @Test(expected = NullArgumentException.class)
    public void testMannWhitneyU_nullFirstSample_throwsNullArgumentException() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyU(null, new double[]{1.0});
    }

    // Tests null second sample for mannWhitneyU throws NullArgumentException
    @Test(expected = NullArgumentException.class)
    public void testMannWhitneyU_nullSecondSample_throwsNullArgumentException() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyU(new double[]{1.0}, null);
    }

    // Tests empty first sample for mannWhitneyU throws NoDataException
    @Test(expected = NoDataException.class)
    public void testMannWhitneyU_emptyFirstSample_throwsNoDataException() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyU(new double[0], new double[]{1.0});
    }

    // Tests empty second sample for mannWhitneyU throws NoDataException
    @Test(expected = NoDataException.class)
    public void testMannWhitneyU_emptySecondSample_throwsNoDataException() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyU(new double[]{1.0}, new double[0]);
    }

    // Tests null input for mannWhitneyUTest throws NullArgumentException
    @Test(expected = NullArgumentException.class)
    public void testMannWhitneyUTest_nullSample_throwsNullArgumentException() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyUTest(null, new double[]{1.0});
    }

    // Tests empty input for mannWhitneyUTest throws NoDataException
    @Test(expected = NoDataException.class)
    public void testMannWhitneyUTest_emptySample_throwsNoDataException() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyUTest(new double[]{1.0}, new double[0]);
    }
}