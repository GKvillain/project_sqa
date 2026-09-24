package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;

public class FractionTest {

    // Tests normal creation and accessors
    @Test
    public void testGetFraction_twoInts_returnsCorrectFraction() {
        Fraction f = Fraction.getFraction(3, 4);
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());
        assertEquals(0, f.getProperWhole());
        assertEquals(3, f.getProperNumerator());
    }

    // Tests negative denominator resolution
    @Test
    public void testGetFraction_negativeDenominator_movesSignToNumerator() {
        Fraction f = Fraction.getFraction(3, -4);
        assertEquals(-3, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    // Tests denominator zero throws ArithmeticException
    @Test(expected = ArithmeticException.class)
    public void testGetFraction_zeroDenominator_throwsException() {
        Fraction.getFraction(1, 0);
    }

    // Tests 3-parameter factory method with proper/improper conversion
    @Test
    public void testGetFraction_threeInts_returnsCorrectImproperFraction() {
        Fraction f = Fraction.getFraction(1, 1, 2);
        assertEquals(3, f.getNumerator());
        assertEquals(2, f.getDenominator());

        Fraction negative = Fraction.getFraction(-1, 1, 2);
        assertEquals(-3, negative.getNumerator());
        assertEquals(2, negative.getDenominator());
    }

    // Tests 3-parameter factory with invalid negative numerator/denominator
    @Test(expected = ArithmeticException.class)
    public void testGetFraction_negativeNumeratorInWholePart_throwsException() {
        Fraction.getFraction(1, -1, 2);
    }

    // Tests double conversion to Fraction
    @Test
    public void testGetFraction_doubleValue_convertsAccurately() {
        Fraction f = Fraction.getFraction(0.75);
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    // Tests string parsing for various formats
    @Test
    public void testGetFraction_stringFormats_parsesCorrectly() {
        assertEquals(Fraction.getFraction(1, 2), Fraction.getFraction("0.5"));
        assertEquals(Fraction.getFraction(3, 4), Fraction.getFraction("3/4"));
        assertEquals(Fraction.getFraction(7, 4), Fraction.getFraction("1 3/4"));
        assertEquals(Fraction.getFraction(5, 1), Fraction.getFraction("5"));
    }

    // Tests string parsing with null input
    @Test(expected = IllegalArgumentException.class)
    public void testGetFraction_nullString_throwsException() {
        Fraction.getFraction((String) null);
    }

    // Tests getReducedFraction with Integer.MIN_VALUE (Defects4J Lang-22 bug detection)
    @Test
    public void testGetReducedFraction_integerMinValue_reducesCorrectly() {
        Fraction f1 = Fraction.getReducedFraction(Integer.MIN_VALUE, 2);
        assertEquals(Integer.MIN_VALUE / 2, f1.getNumerator());
        assertEquals(1, f1.getDenominator());

        Fraction f2 = Fraction.getReducedFraction(2, Integer.MIN_VALUE);
        assertEquals(-1, f2.getNumerator());
        assertEquals(-(Integer.MIN_VALUE / 2), f2.getDenominator());
    }

    // Tests getReducedFraction with zero and normal cases
    @Test
    public void testGetReducedFraction_zeroAndNormalValues_returnsReducedFraction() {
        assertSame(Fraction.ZERO, Fraction.getReducedFraction(0, 5));
        Fraction f = Fraction.getReducedFraction(2, 4);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    // Tests Number interface conversion methods
    @Test
    public void testNumberMethods_conversions_returnCorrectValues() {
        Fraction f = Fraction.getFraction(5, 2);
        assertEquals(2, f.intValue());
        assertEquals(2L, f.longValue());
        assertEquals(2.5f, f.floatValue(), 0.00001f);
        assertEquals(2.5d, f.doubleValue(), 0.00001d);
    }

    // Tests arithmetic: add and subtract
    @Test
    public void testAddAndSubtract_validFractions_returnsReducedResult() {
        Fraction f1 = Fraction.getFraction(1, 3);
        Fraction f2 = Fraction.getFraction(1, 6);

        Fraction sum = f1.add(f2);
        assertEquals(1, sum.getNumerator());
        assertEquals(2, sum.getDenominator());

        Fraction diff = f1.subtract(f2);
        assertEquals(1, diff.getNumerator());
        assertEquals(6, diff.getDenominator());
    }

    // Tests arithmetic: multiplyBy and divideBy
    @Test
    public void testMultiplyAndDivide_validFractions_returnsCorrectResult() {
        Fraction f1 = Fraction.getFraction(2, 3);
        Fraction f2 = Fraction.getFraction(3, 4);

        Fraction product = f1.multiplyBy(f2);
        assertEquals(1, product.getNumerator());
        assertEquals(2, product.getDenominator());

        Fraction quotient = f1.divideBy(f2);
        assertEquals(8, quotient.getNumerator());
        assertEquals(9, quotient.getDenominator());
    }

    // Tests divideBy zero throws ArithmeticException
    @Test(expected = ArithmeticException.class)
    public void testDivideBy_zeroFraction_throwsException() {
        Fraction f = Fraction.getFraction(1, 2);
        f.divideBy(Fraction.ZERO);
    }

    // Tests invert, negate, and abs transformations
    @Test
    public void testTransformations_invertNegateAbs_returnsExpectedFractions() {
        Fraction f = Fraction.getFraction(-2, 3);
        assertEquals(Fraction.getFraction(-3, 2), f.invert());
        assertEquals(Fraction.getFraction(2, 3), f.negate());
        assertEquals(Fraction.getFraction(2, 3), f.abs());
    }

    // Tests pow operation with positive, negative, and zero exponents
    @Test
    public void testPow_variousExponents_computesCorrectPower() {
        Fraction f = Fraction.getFraction(2, 3);
        assertEquals(Fraction.ONE, f.pow(0));
        assertEquals(f, f.pow(1));
        assertEquals(Fraction.getFraction(4, 9), f.pow(2));
        assertEquals(Fraction.getFraction(9, 4), f.pow(-2));
    }

    // Tests equals, hashCode, and compareTo
    @Test
    public void testEqualsAndCompareTo_consistentComparisons() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(1, 2);
        Fraction f3 = Fraction.getFraction(2, 4);
        Fraction f4 = Fraction.getFraction(3, 4);

        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
        assertFalse(f1.equals(f3));
        assertFalse(f1.equals("1/2"));

        assertEquals(0, f1.compareTo(f2));
        assertEquals(0, f1.compareTo(f3));
        assertTrue(f1.compareTo(f4) < 0);
        assertTrue(f4.compareTo(f1) > 0);
    }

    // Tests toString and toProperString formatting
    @Test
    public void testToStringAndToProperString_formatsCorrectly() {
        Fraction f1 = Fraction.getFraction(7, 4);
        assertEquals("7/4", f1.toString());
        assertEquals("1 3/4", f1.toProperString());

        Fraction f2 = Fraction.getFraction(-7, 4);
        assertEquals("-7/4", f2.toString());
        assertEquals("-1 3/4", f2.toProperString());

        Fraction zero = Fraction.ZERO;
        assertEquals("0", zero.toProperString());

        Fraction whole = Fraction.getFraction(4, 2);
        assertEquals("2", whole.toProperString());
    }
}