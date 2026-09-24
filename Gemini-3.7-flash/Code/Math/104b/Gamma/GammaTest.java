package org.apache.commons.math.special;

import org.apache.commons.math.MathException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.junit.Test;
import static org.junit.Assert.*;

public class GammaTest {

    private static final double DELTA = 1e-8;

    // Tests logGamma with NaN input
    @Test
    public void testLogGamma_nanInput_returnsNaN() {
        assertTrue(Double.isNaN(Gamma.logGamma(Double.NaN)));
    }

    // Tests logGamma with non-positive values
    @Test
    public void testLogGamma_nonPositiveInput_returnsNaN() {
        assertTrue(Double.isNaN(Gamma.logGamma(0.0)));
        assertTrue(Double.isNaN(Gamma.logGamma(-1.0)));
        assertTrue(Double.isNaN(Gamma.logGamma(-10.5)));
    }

    // Tests logGamma with positive integer and real values
    @Test
    public void testLogGamma_positiveInput_returnsCorrectValue() {
        assertEquals(0.0, Gamma.logGamma(1.0), DELTA);
        assertEquals(0.0, Gamma.logGamma(2.0), DELTA);
        assertEquals(Math.log(2.0), Gamma.logGamma(3.0), DELTA);
        assertEquals(Math.log(6.0), Gamma.logGamma(4.0), DELTA);
        assertEquals(Math.log(24.0), Gamma.logGamma(5.0), DELTA);
    }

    // Tests regularizedGammaP with invalid inputs (NaN, negative, zero 'a')
    @Test
    public void testRegularizedGammaP_invalidInputs_returnsNaN() throws MathException {
        assertTrue(Double.isNaN(Gamma.regularizedGammaP(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(Gamma.regularizedGammaP(1.0, Double.NaN)));
        assertTrue(Double.isNaN(Gamma.regularizedGammaP(0.0, 1.0)));
        assertTrue(Double.isNaN(Gamma.regularizedGammaP(-1.0, 1.0)));
        assertTrue(Double.isNaN(Gamma.regularizedGammaP(1.0, -1.0)));
    }

    // Tests regularizedGammaP with x = 0
    @Test
    public void testRegularizedGammaP_zeroX_returnsZero() throws MathException {
        assertEquals(0.0, Gamma.regularizedGammaP(1.0, 0.0), DELTA);
        assertEquals(0.0, Gamma.regularizedGammaP(2.5, 0.0), DELTA);
    }

    // Tests regularizedGammaP when a >= 1.0 and x > a (delegates to regularizedGammaQ)
    @Test
    public void testRegularizedGammaP_xGreaterThanA_returnsCorrectValue() throws MathException {
        double expected = 0.8008517265285442;
        assertEquals(expected, Gamma.regularizedGammaP(2.0, 3.0), DELTA);
    }

    // Tests regularizedGammaP when x <= a or a < 1.0 (series expansion path)
    @Test
    public void testRegularizedGammaP_xLessThanA_returnsCorrectValue() throws MathException {
        double expected = 0.32332358381693654;
        assertEquals(expected, Gamma.regularizedGammaP(3.0, 2.0), DELTA);
        
        // a < 1.0 case
        assertEquals(0.8427007929497148, Gamma.regularizedGammaP(0.5, 1.5), DELTA);
    }

    // Tests regularizedGammaP max iterations exceeded exception
    @Test(expected = MaxIterationsExceededException.class)
    public void testRegularizedGammaP_maxIterationsExceeded_throwsException() throws MathException {
        Gamma.regularizedGammaP(3.0, 2.0, 1e-15, 1);
    }

    // Tests regularizedGammaQ with invalid inputs (NaN, negative, zero 'a')
    @Test
    public void testRegularizedGammaQ_invalidInputs_returnsNaN() throws MathException {
        assertTrue(Double.isNaN(Gamma.regularizedGammaQ(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(Gamma.regularizedGammaQ(1.0, Double.NaN)));
        assertTrue(Double.isNaN(Gamma.regularizedGammaQ(0.0, 1.0)));
        assertTrue(Double.isNaN(Gamma.regularizedGammaQ(-1.0, 1.0)));
        assertTrue(Double.isNaN(Gamma.regularizedGammaQ(1.0, -1.0)));
    }

    // Tests regularizedGammaQ with x = 0
    @Test
    public void testRegularizedGammaQ_zeroX_returnsOne() throws MathException {
        assertEquals(1.0, Gamma.regularizedGammaQ(1.0, 0.0), DELTA);
        assertEquals(1.0, Gamma.regularizedGammaQ(2.5, 0.0), DELTA);
    }

    // Tests regularizedGammaQ when x < a or a < 1.0 (delegates to regularizedGammaP)
    @Test
    public void testRegularizedGammaQ_xLessThanA_returnsCorrectValue() throws MathException {
        double expected = 1.0 - 0.32332358381693654;
        assertEquals(expected, Gamma.regularizedGammaQ(3.0, 2.0), DELTA);
        
        // a < 1.0 case
        assertEquals(1.0 - 0.8427007929497148, Gamma.regularizedGammaQ(0.5, 1.5), DELTA);
    }

    // Tests regularizedGammaQ when x >= a and a >= 1.0 (continued fraction path)
    @Test
    public void testRegularizedGammaQ_xGreaterThanOrEqualToA_returnsCorrectValue() throws MathException {
        double expected = 1.0 - 0.8008517265285442;
        assertEquals(expected, Gamma.regularizedGammaQ(2.0, 3.0), DELTA);
    }

    // Tests regularizedGammaQ max iterations exceeded exception
    @Test(expected = MaxIterationsExceededException.class)
    public void testRegularizedGammaQ_maxIterationsExceeded_throwsException() throws MathException {
        Gamma.regularizedGammaQ(2.0, 3.0, 1e-15, 1);
    }

    // Tests mathematical relationship P(a, x) + Q(a, x) == 1
    @Test
    public void testRegularizedGamma_sumPAndQ_equalsOne() throws MathException {
        double a = 5.0;
        double x = 4.0;
        double p = Gamma.regularizedGammaP(a, x);
        double q = Gamma.regularizedGammaQ(a, x);
        assertEquals(1.0, p + q, DELTA);

        a = 2.0;
        x = 5.0;
        p = Gamma.regularizedGammaP(a, x);
        q = Gamma.regularizedGammaQ(a, x);
        assertEquals(1.0, p + q, DELTA);
    }
}