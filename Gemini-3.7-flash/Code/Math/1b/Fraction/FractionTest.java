package org.apache.commons.math3.fraction;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FractionTest {

    // Tests creation from int numerator and denominator with reduction
    @Test
    public void testConstructor_twoInts_createsReducedFraction() {
        Fraction f = new Fraction(2, 4);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    // Tests negative signs normalization to numerator
    @Test
    public void testConstructor_negativeDenominator_normalizesSign() {
        Fraction f = new Fraction(1, -2);
        assertEquals(-1, f.getNumerator());
        assertEquals(2, f.getDenominator());

        Fraction f2 = new Fraction(-1, -2);
        assertEquals(1, f2.getNumerator());
        assertEquals(2, f2.getDenominator());
    }

    // Tests exception on zero denominator
    @Test(expected = MathArithmeticException.class)
    public void testConstructor_zeroDenominator_throwsMathArithmeticException() {
        new Fraction(1, 0);
    }

    // Tests overflow on Integer.MIN_VALUE denominator
    @Test(expected = MathArithmeticException.class)
    public void testConstructor_minIntDenominator_throwsMathArithmeticException() {
        new Fraction(1, Integer.MIN_VALUE);
    }

    // Tests constructor with single integer
    @Test
    public void testConstructor_singleInt_createsWholeFraction() {
        Fraction f = new Fraction(5);
        assertEquals(5, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    // Tests conversion of double values
    @Test
    public void testConstructor_doubleValue_convertsAccurately() {
        Fraction f1 = new Fraction(0.5);
        assertEquals(1, f1.getNumerator());
        assertEquals(2, f1.getDenominator());

        Fraction f2 = new Fraction(1.0 / 3.0);
        assertEquals(1, f2.getNumerator());
        assertEquals(3, f2.getDenominator());

        Fraction f3 = new Fraction(0.0);
        assertEquals(0, f3.getNumerator());
        assertEquals(1, f3.getDenominator());
    }

    // Tests double conversion with maxDenominator
    @Test
    public void testConstructor_doubleWithMaxDenominator_convergesProperly() {
        Fraction f = new Fraction(0.666666, 10);
        assertEquals(2, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    // Tests conversion exception when double fails to converge or exceeds max iterations
    @Test(expected = FractionConversionException.class)
    public void testConstructor_doubleMaxIterationsExceeded_throwsException() {
        new Fraction(0.123456789, 1e-15, 2);
    }

    // Tests absolute value
    @Test
    public void testAbs_negativeAndPositiveFractions_returnsPositive() {
        Fraction f1 = new Fraction(-3, 4);
        assertEquals(new Fraction(3, 4), f1.abs());

        Fraction f2 = new Fraction(3, 4);
        assertEquals(f2, f2.abs());
    }

    // Tests negation and negation overflow
    @Test
    public void testNegate_validFraction_returnsNegated() {
        Fraction f = new Fraction(3, 4);
        assertEquals(new Fraction(-3, 4), f.negate());
    }

    // Tests negate exception when numerator is Integer.MIN_VALUE
    @Test(expected = MathArithmeticException.class)
    public void testNegate_minIntNumerator_throwsMathArithmeticException() {
        Fraction f = new Fraction(Integer.MIN_VALUE, 1);
        f.negate();
    }

    // Tests reciprocal
    @Test
    public void testReciprocal_validFraction_returnsInverted() {
        Fraction f = new Fraction(3, 4);
        assertEquals(new Fraction(4, 3), f.reciprocal());
    }

    // Tests addition of fractions and integer
    @Test
    public void testAdd_fractionAndInteger_returnsCorrectSum() {
        Fraction f1 = new Fraction(1, 3);
        Fraction f2 = new Fraction(1, 6);
        assertEquals(new Fraction(1, 2), f1.add(f2));
        assertEquals(f1, f1.add(Fraction.ZERO));
        assertEquals(f2, Fraction.ZERO.add(f2));

        Fraction f3 = new Fraction(1, 2);
        assertEquals(new Fraction(3, 2), f3.add(1));
    }

    // Tests addition null argument
    @Test(expected = NullArgumentException.class)
    public void testAdd_nullFraction_throwsNullArgumentException() {
        new Fraction(1, 2).add(null);
    }

    // Tests subtraction of fractions and integer
    @Test
    public void testSubtract_fractionAndInteger_returnsCorrectDifference() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 6);
        assertEquals(new Fraction(1, 3), f1.subtract(f2));
        assertEquals(new Fraction(-1, 2), Fraction.ZERO.subtract(f1));
        assertEquals(f1, f1.subtract(Fraction.ZERO));

        assertEquals(new Fraction(-1, 2), f1.subtract(1));
    }

    // Tests multiplication of fractions and integer
    @Test
    public void testMultiply_fractionAndInteger_returnsCorrectProduct() {
        Fraction f1 = new Fraction(2, 3);
        Fraction f2 = new Fraction(3, 4);
        assertEquals(new Fraction(1, 2), f1.multiply(f2));
        assertEquals(Fraction.ZERO, f1.multiply(Fraction.ZERO));
        assertEquals(new Fraction(4, 3), f1.multiply(2));
    }

    // Tests division of fractions and integer
    @Test
    public void testDivide_fractionAndInteger_returnsCorrectQuotient() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 4);
        assertEquals(new Fraction(2, 1), f1.divide(f2));
        assertEquals(new Fraction(1, 4), f1.divide(2));
    }

    // Tests division by zero fraction
    @Test(expected = MathArithmeticException.class)
    public void testDivide_zeroFraction_throwsMathArithmeticException() {
        new Fraction(1, 2).divide(Fraction.ZERO);
    }

    // Tests compareTo method
    @Test
    public void testCompareTo_variousFractions_returnsExpectedOrdering() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(3, 4);
        Fraction f3 = new Fraction(2, 4);

        assertTrue(f1.compareTo(f2) < 0);
        assertTrue(f2.compareTo(f1) > 0);
        assertEquals(0, f1.compareTo(f3));
    }

    // Tests equals and hashCode methods
    @Test
    public void testEqualsAndHashCode_variousObjects_satisfiesContract() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 2);
        Fraction f3 = new Fraction(2, 3);

        assertTrue(f1.equals(f1));
        assertTrue(f1.equals(f2));
        assertFalse(f1.equals(f3));
        assertFalse(f1.equals(null));
        assertFalse(f1.equals("1/2"));
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    // Tests primitive numeric conversions and percentageValue
    @Test
    public void testConversions_primitiveValues_returnsAccurateValues() {
        Fraction f = new Fraction(3, 2);
        assertEquals(1.5, f.doubleValue(), 1e-10);
        assertEquals(1.5f, f.floatValue(), 1e-10f);
        assertEquals(1, f.intValue());
        assertEquals(1L, f.longValue());
        assertEquals(150.0, f.percentageValue(), 1e-10);
    }

    // Tests toString formatting
    @Test
    public void testToString_variousFractions_returnsFormattedString() {
        assertEquals("3 / 4", new Fraction(3, 4).toString());
        assertEquals("2", new Fraction(2, 1).toString());
        assertEquals("0", new Fraction(0, 5).toString());
    }

    // Tests getReducedFraction utility
    @Test
    public void testGetReducedFraction_variousInputs_returnsReducedInstance() {
        assertEquals(Fraction.ZERO, Fraction.getReducedFraction(0, 5));
        assertEquals(new Fraction(1, 2), Fraction.getReducedFraction(2, 4));
        assertEquals(new Fraction(-1, 2), Fraction.getReducedFraction(1, -2));
        assertEquals(new Fraction(1, 2), Fraction.getReducedFraction(-1, -2));
        assertEquals(new Fraction(1, 1073741824), Fraction.getReducedFraction(2, Integer.MIN_VALUE));
    }

    // Tests getField returns non-null instance
    @Test
    public void testGetField_returnsNonNullInstance() {
        assertNotNull(Fraction.ONE.getField());
    }
}