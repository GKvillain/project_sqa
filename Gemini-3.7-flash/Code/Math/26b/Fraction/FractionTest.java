package org.apache.commons.math3.fraction;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FractionTest {

    // Tests normal fraction creation from double
    @Test
    public void testConstructor_doubleValue_convertsCorrectly() {
        Fraction f1 = new Fraction(0.5);
        assertEquals(1, f1.getNumerator());
        assertEquals(2, f1.getDenominator());

        Fraction f2 = new Fraction(0.75, 1.0e-5, 100);
        assertEquals(3, f2.getNumerator());
        assertEquals(4, f2.getDenominator());

        Fraction f3 = new Fraction(0.4, 10);
        assertEquals(2, f3.getNumerator());
        assertEquals(5, f3.getDenominator());
    }

    // Tests double constructor with integer boundary values and overflow (Defects4J Math-26)
    @Test(expected = FractionConversionException.class)
    public void testConstructor_doubleOverflowPositive_throwsException() {
        new Fraction((double) Integer.MAX_VALUE + 1.0, 1.0e-5, 100);
    }

    // Tests double constructor with negative overflow
    @Test(expected = FractionConversionException.class)
    public void testConstructor_doubleOverflowNegative_throwsException() {
        new Fraction((double) Integer.MIN_VALUE - 1.0, 1.0e-5, 100);
    }

    // Tests int constructor creating num/1
    @Test
    public void testConstructor_singleInt_createsCorrectFraction() {
        Fraction f = new Fraction(5);
        assertEquals(5, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    // Tests two-int constructor with simplification and sign normalization
    @Test
    public void testConstructor_twoInts_reducedAndSignNormalized() {
        Fraction f1 = new Fraction(6, 8);
        assertEquals(3, f1.getNumerator());
        assertEquals(4, f1.getDenominator());

        Fraction f2 = new Fraction(3, -4);
        assertEquals(-3, f2.getNumerator());
        assertEquals(4, f2.getDenominator());

        Fraction f3 = new Fraction(-3, -4);
        assertEquals(3, f3.getNumerator());
        assertEquals(4, f3.getDenominator());
    }

    // Tests two-int constructor zero denominator exception path
    @Test(expected = MathArithmeticException.class)
    public void testConstructor_zeroDenominator_throwsException() {
        new Fraction(1, 0);
    }

    // Tests two-int constructor overflow with Integer.MIN_VALUE
    @Test(expected = MathArithmeticException.class)
    public void testConstructor_minDenominatorOverflow_throwsException() {
        new Fraction(1, Integer.MIN_VALUE);
    }

    // Tests absolute value method
    @Test
    public void testAbs_negativeAndPositiveFractions_returnsPositive() {
        Fraction negative = new Fraction(-3, 4);
        Fraction positive = new Fraction(3, 4);

        assertEquals(positive, negative.abs());
        assertEquals(positive, positive.abs());
    }

    // Tests compareTo behavior
    @Test
    public void testCompareTo_variousValues_returnsExpectedComparison() {
        Fraction first = new Fraction(1, 2);
        Fraction second = new Fraction(3, 4);
        Fraction third = new Fraction(2, 4);

        assertTrue(first.compareTo(second) < 0);
        assertTrue(second.compareTo(first) > 0);
        assertEquals(0, first.compareTo(third));
    }

    // Tests equals and hashCode methods
    @Test
    public void testEqualsAndHashCode_matchingAndDifferentObjects_behaveCorrectly() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(2, 4);
        Fraction f3 = new Fraction(1, 3);

        assertTrue(f1.equals(f1));
        assertTrue(f1.equals(f2));
        assertFalse(f1.equals(f3));
        assertFalse(f1.equals(null));
        assertFalse(f1.equals("Not a fraction"));

        assertEquals(f1.hashCode(), f2.hashCode());
    }

    // Tests negation and negation overflow
    @Test
    public void testNegate_normalAndMinValue_handledProperly() {
        Fraction f = new Fraction(3, 4);
        Fraction neg = f.negate();
        assertEquals(-3, neg.getNumerator());
        assertEquals(4, neg.getDenominator());
    }

    // Tests negate exception when numerator is Integer.MIN_VALUE
    @Test(expected = MathArithmeticException.class)
    public void testNegate_minValueNumerator_throwsException() {
        Fraction f = new Fraction(Integer.MIN_VALUE, 2);
        f.negate();
    }

    // Tests reciprocal calculation
    @Test
    public void testReciprocal_validFraction_returnsInverted() {
        Fraction f = new Fraction(3, 4);
        Fraction recip = f.reciprocal();
        assertEquals(4, recip.getNumerator());
        assertEquals(3, recip.getDenominator());
    }

    // Tests addition with another fraction and integer
    @Test
    public void testAdd_fractionAndInteger_returnsCorrectSum() {
        Fraction f1 = new Fraction(1, 3);
        Fraction f2 = new Fraction(1, 6);
        Fraction sum = f1.add(f2);
        assertEquals(1, sum.getNumerator());
        assertEquals(2, sum.getDenominator());

        Fraction sumInt = f1.add(2);
        assertEquals(7, sumInt.getNumerator());
        assertEquals(3, sumInt.getDenominator());

        assertEquals(f1, Fraction.ZERO.add(f1));
        assertEquals(f1, f1.add(Fraction.ZERO));
    }

    // Tests addition with null
    @Test(expected = NullArgumentException.class)
    public void testAdd_nullFraction_throwsException() {
        Fraction.ONE.add((Fraction) null);
    }

    // Tests subtraction with another fraction and integer
    @Test
    public void testSubtract_fractionAndInteger_returnsCorrectDifference() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 4);
        Fraction diff = f1.subtract(f2);
        assertEquals(1, diff.getNumerator());
        assertEquals(4, diff.getDenominator());

        Fraction diffInt = f1.subtract(1);
        assertEquals(-1, diffInt.getNumerator());
        assertEquals(2, diffInt.getDenominator());

        assertEquals(f1, f1.subtract(Fraction.ZERO));
        assertEquals(f1.negate(), Fraction.ZERO.subtract(f1));
    }

    // Tests subtraction with null
    @Test(expected = NullArgumentException.class)
    public void testSubtract_nullFraction_throwsException() {
        Fraction.ONE.subtract((Fraction) null);
    }

    // Tests multiplication with fraction and integer
    @Test
    public void testMultiply_fractionAndInteger_returnsCorrectProduct() {
        Fraction f1 = new Fraction(2, 3);
        Fraction f2 = new Fraction(3, 4);
        Fraction prod = f1.multiply(f2);
        assertEquals(1, prod.getNumerator());
        assertEquals(2, prod.getDenominator());

        Fraction prodInt = f1.multiply(3);
        assertEquals(2, prodInt.getNumerator());
        assertEquals(1, prodInt.getDenominator());

        assertEquals(Fraction.ZERO, f1.multiply(Fraction.ZERO));
    }

    // Tests multiplication with null
    @Test(expected = NullArgumentException.class)
    public void testMultiply_nullFraction_throwsException() {
        Fraction.ONE.multiply((Fraction) null);
    }

    // Tests division with fraction and integer
    @Test
    public void testDivide_fractionAndInteger_returnsCorrectQuotient() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 4);
        Fraction quotient = f1.divide(f2);
        assertEquals(2, quotient.getNumerator());
        assertEquals(1, quotient.getDenominator());

        Fraction quotInt = f1.divide(2);
        assertEquals(1, quotInt.getNumerator());
        assertEquals(4, quotInt.getDenominator());
    }

    // Tests division by zero fraction
    @Test(expected = MathArithmeticException.class)
    public void testDivide_byZeroFraction_throwsException() {
        Fraction.ONE.divide(Fraction.ZERO);
    }

    // Tests division with null
    @Test(expected = NullArgumentException.class)
    public void testDivide_nullFraction_throwsException() {
        Fraction.ONE.divide((Fraction) null);
    }

    // Tests getReducedFraction static helper method
    @Test
    public void testGetReducedFraction_variousInputs_returnsReducedInstance() {
        Fraction f1 = Fraction.getReducedFraction(2, 4);
        assertEquals(1, f1.getNumerator());
        assertEquals(2, f1.getDenominator());

        Fraction fZero = Fraction.getReducedFraction(0, 5);
        assertEquals(Fraction.ZERO, fZero);

        Fraction fNeg = Fraction.getReducedFraction(2, -4);
        assertEquals(-1, fNeg.getNumerator());
        assertEquals(2, fNeg.getDenominator());
    }

    // Tests getReducedFraction with zero denominator
    @Test(expected = MathArithmeticException.class)
    public void testGetReducedFraction_zeroDenominator_throwsException() {
        Fraction.getReducedFraction(1, 0);
    }

    // Tests numeric primitive conversions
    @Test
    public void testConversions_primitiveValues_returnsAccurateValues() {
        Fraction f = new Fraction(3, 2);
        assertEquals(1.5, f.doubleValue(), 1e-10);
        assertEquals(1.5f, f.floatValue(), 1e-5f);
        assertEquals(1, f.intValue());
        assertEquals(1L, f.longValue());
        assertEquals(150.0, f.percentageValue(), 1e-10);
        assertNotNull(f.getField());
    }

    // Tests toString method under various forms
    @Test
    public void testToString_variousFractions_formatsCorrectly() {
        assertEquals("0", Fraction.ZERO.toString());
        assertEquals("1", Fraction.ONE.toString());
        assertEquals("3 / 4", new Fraction(3, 4).toString());
    }
}