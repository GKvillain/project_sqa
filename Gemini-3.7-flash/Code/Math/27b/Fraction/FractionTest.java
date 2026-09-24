package org.apache.commons.math3.fraction;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FractionTest {

    // Tests percentageValue with large numerator to detect integer overflow (Defects4J Math-27)
    @Test
    public void testPercentageValue_largeNumerator_computesCorrectlyWithoutOverflow() {
        Fraction f = new Fraction(Integer.MAX_VALUE / 2, 1);
        double expected = 100.0 * ((double) (Integer.MAX_VALUE / 2));
        assertEquals(expected, f.percentageValue(), 1.0e-5);
    }

    // Tests standard constructor with numerator and denominator
    @Test
    public void testConstructor_standard_reducesProperly() {
        Fraction f = new Fraction(2, 4);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    // Tests constructor with negative denominator
    @Test
    public void testConstructor_negativeDenominator_normalizesSign() {
        Fraction f = new Fraction(1, -2);
        assertEquals(-1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    // Tests constructor with zero denominator throws exception
    @Test(expected = MathArithmeticException.class)
    public void testConstructor_zeroDenominator_throwsException() {
        new Fraction(1, 0);
    }

    // Tests constructor with Integer.MIN_VALUE overflow
    @Test(expected = MathArithmeticException.class)
    public void testConstructor_minValueDenominator_throwsException() {
        new Fraction(1, Integer.MIN_VALUE);
    }

    // Tests double conversion constructor
    @Test
    public void testConstructor_doubleValue_convertsAccurately() {
        Fraction f = new Fraction(0.75);
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());
        assertEquals(0.75, f.doubleValue(), 1.0e-5);
        assertEquals(0.75f, f.floatValue(), 1.0e-5f);
        assertEquals(0, f.intValue());
        assertEquals(0L, f.longValue());
    }

    // Tests abs method for negative and positive fractions
    @Test
    public void testAbs_negativeAndPositive_returnsAbsoluteValue() {
        Fraction neg = new Fraction(-3, 4);
        Fraction pos = new Fraction(3, 4);
        assertEquals(pos, neg.abs());
        assertEquals(pos, pos.abs());
    }

    // Tests negate method and its overflow edge case
    @Test
    public void testNegate_validFraction_returnsNegated() {
        Fraction f = new Fraction(3, 4);
        assertEquals(new Fraction(-3, 4), f.negate());
    }

    // Tests reciprocal method
    @Test
    public void testReciprocal_validFraction_returnsReciprocal() {
        Fraction f = new Fraction(3, 4);
        assertEquals(new Fraction(4, 3), f.reciprocal());
    }

    // Tests addition with Fraction and integer
    @Test
    public void testAdd_fractionAndInteger_returnsCorrectSum() {
        Fraction f1 = new Fraction(1, 3);
        Fraction f2 = new Fraction(1, 6);
        assertEquals(new Fraction(1, 2), f1.add(f2));
        assertEquals(new Fraction(4, 3), f1.add(1));
    }

    // Tests subtraction with Fraction and integer
    @Test
    public void testSubtract_fractionAndInteger_returnsCorrectDifference() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 6);
        assertEquals(new Fraction(1, 3), f1.subtract(f2));
        assertEquals(new Fraction(-1, 2), f1.subtract(1));
    }

    // Tests multiplication with Fraction and integer
    @Test
    public void testMultiply_fractionAndInteger_returnsCorrectProduct() {
        Fraction f1 = new Fraction(2, 3);
        Fraction f2 = new Fraction(3, 4);
        assertEquals(new Fraction(1, 2), f1.multiply(f2));
        assertEquals(new Fraction(4, 3), f1.multiply(2));
        assertEquals(Fraction.ZERO, f1.multiply(Fraction.ZERO));
    }

    // Tests division with Fraction and integer
    @Test
    public void testDivide_fractionAndInteger_returnsCorrectQuotient() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(3, 4);
        assertEquals(new Fraction(2, 3), f1.divide(f2));
        assertEquals(new Fraction(1, 4), f1.divide(2));
    }

    // Tests division by zero fraction throws exception
    @Test(expected = MathArithmeticException.class)
    public void testDivide_zeroFraction_throwsException() {
        Fraction f = new Fraction(1, 2);
        f.divide(Fraction.ZERO);
    }

    // Tests null arguments throw NullArgumentException
    @Test(expected = NullArgumentException.class)
    public void testAdd_nullArgument_throwsException() {
        Fraction.ONE.add(null);
    }

    // Tests compareTo method
    @Test
    public void testCompareTo_differentFractions_returnsExpectedComparison() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(3, 4);
        Fraction f3 = new Fraction(2, 4);

        assertTrue(f1.compareTo(f2) < 0);
        assertTrue(f2.compareTo(f1) > 0);
        assertEquals(0, f1.compareTo(f3));
    }

    // Tests equals and hashCode contract
    @Test
    public void testEqualsAndHashCode_variousObjects_returnsConsistentResults() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(2, 4);
        Fraction f3 = new Fraction(1, 3);

        assertTrue(f1.equals(f1));
        assertTrue(f1.equals(f2));
        assertFalse(f1.equals(f3));
        assertFalse(f1.equals(null));
        assertFalse(f1.equals("1/2"));

        assertEquals(f1.hashCode(), f2.hashCode());
    }

    // Tests toString format
    @Test
    public void testToString_variousFractions_returnsFormattedString() {
        assertEquals("3 / 4", new Fraction(3, 4).toString());
        assertEquals("2", new Fraction(2, 1).toString());
        assertEquals("0", Fraction.ZERO.toString());
    }

    // Tests getReducedFraction static factory method
    @Test
    public void testGetReducedFraction_validInputs_returnsReducedFraction() {
        Fraction f = Fraction.getReducedFraction(6, 8);
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());

        Fraction zero = Fraction.getReducedFraction(0, 5);
        assertEquals(Fraction.ZERO, zero);
    }

    // Tests getField returns non-null FractionField instance
    @Test
    public void testGetField_standardCall_returnsInstance() {
        assertNotNull(Fraction.ONE.getField());
    }
}