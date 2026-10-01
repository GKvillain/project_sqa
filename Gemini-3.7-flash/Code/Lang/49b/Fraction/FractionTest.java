package org.apache.commons.lang.math;

import org.junit.Test;
import static org.junit.Assert.*;

public class FractionTest {

    // Tests reducing a fraction with zero numerator to Fraction.ZERO
    @Test
    public void testReduce_zeroNumerator_returnsZero() {
        Fraction f = Fraction.getFraction(0, 5);
        Fraction result = f.reduce();
        assertEquals(Fraction.ZERO, result);
        assertEquals(0, result.getNumerator());
        assertEquals(1, result.getDenominator());
    }

    // Tests reducing a normal fraction to its simplest form
    @Test
    public void testReduce_validFraction_returnsReducedFraction() {
        Fraction f = Fraction.getFraction(2, 4);
        Fraction result = f.reduce();
        assertEquals(1, result.getNumerator());
        assertEquals(2, result.getDenominator());

        Fraction alreadyReduced = Fraction.getFraction(3, 7);
        assertSame(alreadyReduced, alreadyReduced.reduce());
    }

    // Tests getFraction with standard numerator and denominator
    @Test
    public void testGetFraction_twoInts_returnsFraction() {
        Fraction f1 = Fraction.getFraction(3, 4);
        assertEquals(3, f1.getNumerator());
        assertEquals(4, f1.getDenominator());

        Fraction f2 = Fraction.getFraction(3, -4);
        assertEquals(-3, f2.getNumerator());
        assertEquals(4, f2.getDenominator());
    }

    // Tests getFraction with zero denominator throws ArithmeticException
    @Test(expected = ArithmeticException.class)
    public void testGetFraction_zeroDenominator_throwsArithmeticException() {
        Fraction.getFraction(1, 0);
    }

    // Tests getFraction with whole number and proper parts
    @Test
    public void testGetFraction_threeInts_returnsFraction() {
        Fraction f1 = Fraction.getFraction(1, 1, 2);
        assertEquals(3, f1.getNumerator());
        assertEquals(2, f1.getDenominator());

        Fraction f2 = Fraction.getFraction(-1, 1, 2);
        assertEquals(-3, f2.getNumerator());
        assertEquals(2, f2.getDenominator());
    }

    // Tests getReducedFraction factory method
    @Test
    public void testGetReducedFraction_variousInputs_returnsReducedFraction() {
        Fraction f1 = Fraction.getReducedFraction(0, 10);
        assertSame(Fraction.ZERO, f1);

        Fraction f2 = Fraction.getReducedFraction(2, 4);
        assertEquals(1, f2.getNumerator());
        assertEquals(2, f2.getDenominator());

        Fraction f3 = Fraction.getReducedFraction(2, -4);
        assertEquals(-1, f3.getNumerator());
        assertEquals(2, f3.getDenominator());
    }

    // Tests getFraction from double value
    @Test
    public void testGetFraction_doubleValue_returnsAccurateFraction() {
        Fraction f1 = Fraction.getFraction(0.5);
        assertEquals(1, f1.getNumerator());
        assertEquals(2, f1.getDenominator());

        Fraction f2 = Fraction.getFraction(-0.75);
        assertEquals(-3, f2.getNumerator());
        assertEquals(4, f2.getDenominator());
    }

    // Tests getFraction from String formats
    @Test
    public void testGetFraction_stringFormats_returnsParsedFraction() {
        assertEquals(Fraction.getFraction(1, 2), Fraction.getFraction("1/2"));
        assertEquals(Fraction.getFraction(1, 1, 2), Fraction.getFraction("1 1/2"));
        assertEquals(Fraction.getFraction(5, 1), Fraction.getFraction("5"));
        assertEquals(Fraction.getFraction(1, 2), Fraction.getFraction("0.5"));
    }

    // Tests getFraction with null string throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetFraction_nullString_throwsIllegalArgumentException() {
        Fraction.getFraction((String) null);
    }

    // Tests accessors and proper string conversions
    @Test
    public void testAccessorsAndProperFormats_validFraction_returnsCorrectValues() {
        Fraction f = Fraction.getFraction(7, 4);
        assertEquals(7, f.getNumerator());
        assertEquals(4, f.getDenominator());
        assertEquals(3, f.getProperNumerator());
        assertEquals(1, f.getProperWhole());
        assertEquals(1, f.intValue());
        assertEquals(1L, f.longValue());
        assertEquals(1.75f, f.floatValue(), 0.0001f);
        assertEquals(1.75d, f.doubleValue(), 0.0001d);
        assertEquals("7/4", f.toString());
        assertEquals("1 3/4", f.toProperString());

        Fraction neg = Fraction.getFraction(-7, 4);
        assertEquals(" -1 3/4".trim(), neg.toProperString());
    }

    // Tests invert, negate, and abs operations
    @Test
    public void testInvertNegateAbs_validFraction_returnsExpectedResults() {
        Fraction f = Fraction.getFraction(2, 3);
        assertEquals(Fraction.getFraction(3, 2), f.invert());
        assertEquals(Fraction.getFraction(-2, 3), f.negate());
        assertEquals(Fraction.getFraction(2, 3), f.abs());

        Fraction neg = Fraction.getFraction(-2, 3);
        assertEquals(Fraction.getFraction(-3, 2), neg.invert());
        assertEquals(Fraction.getFraction(2, 3), neg.negate());
        assertEquals(Fraction.getFraction(2, 3), neg.abs());
    }

    // Tests inverting zero throws ArithmeticException
    @Test(expected = ArithmeticException.class)
    public void testInvert_zeroNumerator_throwsArithmeticException() {
        Fraction.ZERO.invert();
    }

    // Tests pow operation with positive, zero, and negative powers
    @Test
    public void testPow_variousPowers_returnsPoweredFraction() {
        Fraction f = Fraction.getFraction(2, 3);
        assertEquals(Fraction.ONE, f.pow(0));
        assertEquals(f, f.pow(1));
        assertEquals(Fraction.getFraction(4, 9), f.pow(2));
        assertEquals(Fraction.getFraction(8, 27), f.pow(3));
        assertEquals(Fraction.getFraction(9, 4), f.pow(-2));
    }

    // Tests addition and subtraction of fractions
    @Test
    public void testAddAndSubtract_validFractions_returnsCorrectResult() {
        Fraction f1 = Fraction.getFraction(1, 3);
        Fraction f2 = Fraction.getFraction(1, 6);

        Fraction sum = f1.add(f2);
        assertEquals(1, sum.getNumerator());
        assertEquals(2, sum.getDenominator());

        Fraction diff = f1.subtract(f2);
        assertEquals(1, diff.getNumerator());
        assertEquals(6, diff.getDenominator());

        assertEquals(f1, f1.add(Fraction.ZERO));
        assertEquals(f1, f1.subtract(Fraction.ZERO));
    }

    // Tests multiplication and division of fractions
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

        assertEquals(Fraction.ZERO, f1.multiplyBy(Fraction.ZERO));
    }

    // Tests division by zero fraction throws ArithmeticException
    @Test(expected = ArithmeticException.class)
    public void testDivideBy_zeroFraction_throwsArithmeticException() {
        Fraction f = Fraction.getFraction(2, 3);
        f.divideBy(Fraction.ZERO);
    }

    // Tests equals, hashCode, and compareTo methods
    @Test
    public void testEqualsAndCompareTo_variousFractions_returnsExpectedBooleanAndInt() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(1, 2);
        Fraction f3 = Fraction.getFraction(2, 4);
        Fraction f4 = Fraction.getFraction(2, 3);

        assertTrue(f1.equals(f1));
        assertTrue(f1.equals(f2));
        assertFalse(f1.equals(f3)); // equals requires strict numerator & denominator match
        assertFalse(f1.equals(null));
        assertFalse(f1.equals("1/2"));

        assertEquals(f1.hashCode(), f2.hashCode());

        assertEquals(0, f1.compareTo(f2));
        assertEquals(0, f1.compareTo(f3)); // compareTo checks numerical value
        assertTrue(f1.compareTo(f4) < 0);
        assertTrue(f4.compareTo(f1) > 0);
    }
}