package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;

public class FractionTest {

    // Tests creation with valid numerator and denominator
    @Test
    public void testGetFraction_validNumeratorAndDenominator_createsFraction() {
        Fraction f = Fraction.getFraction(3, 4);
        assertEquals(3, f.getNumerator());
        assertEquals(4, f.getDenominator());

        Fraction negativeDenom = Fraction.getFraction(3, -4);
        assertEquals(-3, negativeDenom.getNumerator());
        assertEquals(4, negativeDenom.getDenominator());
    }

    // Tests zero denominator exception
    @Test(expected = ArithmeticException.class)
    public void testGetFraction_zeroDenominator_throwsArithmeticException() {
        Fraction.getFraction(1, 0);
    }

    // Tests integer min value overflow in denominator
    @Test(expected = ArithmeticException.class)
    public void testGetFraction_minValDenominator_throwsArithmeticException() {
        Fraction.getFraction(1, Integer.MIN_VALUE);
    }

    // Tests 3-part fraction creation (whole, numerator, denominator)
    @Test
    public void testGetFraction_threeArgs_createsCorrectFraction() {
        Fraction f1 = Fraction.getFraction(1, 1, 2);
        assertEquals(3, f1.getNumerator());
        assertEquals(2, f1.getDenominator());

        Fraction f2 = Fraction.getFraction(-1, 1, 2);
        assertEquals(-3, f2.getNumerator());
        assertEquals(2, f2.getDenominator());
    }

    // Tests 3-part fraction with negative denominator exception
    @Test(expected = ArithmeticException.class)
    public void testGetFraction_threeArgsNegativeDenominator_throwsArithmeticException() {
        Fraction.getFraction(1, 1, -2);
    }

    // Tests reduced fraction factory method including GCD with Integer.MIN_VALUE
    @Test
    public void testGetReducedFraction_standardAndBoundaryValues_reducesProperly() {
        Fraction f1 = Fraction.getReducedFraction(0, 5);
        assertEquals(Fraction.ZERO, f1);

        Fraction f2 = Fraction.getReducedFraction(2, 4);
        assertEquals(1, f2.getNumerator());
        assertEquals(2, f2.getDenominator());

        Fraction f3 = Fraction.getReducedFraction(Integer.MIN_VALUE, 2);
        assertEquals(Integer.MIN_VALUE / 2, f3.getNumerator());
        assertEquals(1, f3.getDenominator());

        Fraction f4 = Fraction.getReducedFraction(Integer.MIN_VALUE, 4);
        assertEquals(Integer.MIN_VALUE / 4, f4.getNumerator());
        assertEquals(1, f4.getDenominator());
    }

    // Tests double conversion to Fraction
    @Test
    public void testGetFraction_doubleValue_convertsProperly() {
        Fraction f = Fraction.getFraction(0.5);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());

        Fraction fNeg = Fraction.getFraction(-0.75);
        assertEquals(-3, fNeg.getNumerator());
        assertEquals(4, fNeg.getDenominator());
    }

    // Tests double NaN exception
    @Test(expected = ArithmeticException.class)
    public void testGetFraction_doubleNaN_throwsArithmeticException() {
        Fraction.getFraction(Double.NaN);
    }

    // Tests parsing string formats
    @Test
    public void testGetFraction_stringFormats_parsesCorrectly() {
        assertEquals(Fraction.getFraction(3, 4), Fraction.getFraction("3/4"));
        assertEquals(Fraction.getFraction(5, 1), Fraction.getFraction("5"));
        assertEquals(Fraction.getFraction(1, 1, 2), Fraction.getFraction("1 1/2"));
        assertEquals(Fraction.getFraction(0.5), Fraction.getFraction("0.5"));
    }

    // Tests parsing null string
    @Test(expected = IllegalArgumentException.class)
    public void testGetFraction_nullString_throwsIllegalArgumentException() {
        Fraction.getFraction((String) null);
    }

    // Tests proper parts and numeric accessors
    @Test
    public void testProperPartsAndConversions() {
        Fraction f = Fraction.getFraction(7, 4);
        assertEquals(3, f.getProperNumerator());
        assertEquals(1, f.getProperWhole());
        assertEquals(1, f.intValue());
        assertEquals(1L, f.longValue());
        assertEquals(1.75f, f.floatValue(), 0.0001f);
        assertEquals(1.75, f.doubleValue(), 0.0001);

        Fraction negF = Fraction.getFraction(-7, 4);
        assertEquals(3, negF.getProperNumerator());
        assertEquals(-1, negF.getProperWhole());
    }

    // Tests reduce, invert, negate, and abs
    @Test
    public void testReduceInvertNegateAbs() {
        Fraction f = Fraction.getFraction(2, 4);
        assertEquals(Fraction.getFraction(1, 2), f.reduce());

        Fraction inv = Fraction.getFraction(3, 4).invert();
        assertEquals(4, inv.getNumerator());
        assertEquals(3, inv.getDenominator());

        Fraction neg = Fraction.getFraction(3, 4).negate();
        assertEquals(-3, neg.getNumerator());
        assertEquals(4, neg.getDenominator());

        Fraction abs = Fraction.getFraction(-3, 4).abs();
        assertEquals(3, abs.getNumerator());
        assertEquals(4, abs.getDenominator());
    }

    // Tests invert zero exception
    @Test(expected = ArithmeticException.class)
    public void testInvert_zero_throwsArithmeticException() {
        Fraction.ZERO.invert();
    }

    // Tests pow operation
    @Test
    public void testPow() {
        Fraction f = Fraction.getFraction(2, 3);
        assertEquals(Fraction.ONE, f.pow(0));
        assertEquals(f, f.pow(1));
        assertEquals(Fraction.getFraction(4, 9), f.pow(2));
        assertEquals(Fraction.getFraction(9, 4), f.pow(-2));
    }

    // Tests add and subtract operations
    @Test
    public void testAddAndSubtract() {
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

    // Tests multiplyBy and divideBy operations
    @Test
    public void testMultiplyAndDivide() {
        Fraction f1 = Fraction.getFraction(2, 3);
        Fraction f2 = Fraction.getFraction(3, 4);

        Fraction prod = f1.multiplyBy(f2);
        assertEquals(1, prod.getNumerator());
        assertEquals(2, prod.getDenominator());

        Fraction quotient = f1.divideBy(f2);
        assertEquals(8, quotient.getNumerator());
        assertEquals(9, quotient.getDenominator());

        assertEquals(Fraction.ZERO, f1.multiplyBy(Fraction.ZERO));
    }

    // Tests divide by zero exception
    @Test(expected = ArithmeticException.class)
    public void testDivideBy_zero_throwsArithmeticException() {
        Fraction.getFraction(1, 2).divideBy(Fraction.ZERO);
    }

    // Tests equals, hashCode, and compareTo
    @Test
    public void testEqualsHashCodeAndCompareTo() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(1, 2);
        Fraction f3 = Fraction.getFraction(2, 4);
        Fraction f4 = Fraction.getFraction(2, 3);

        assertEquals(f1, f2);
        assertNotEquals(f1, f3);
        assertEquals(f1.hashCode(), f2.hashCode());

        assertEquals(0, f1.compareTo(f3));
        assertTrue(f1.compareTo(f4) < 0);
        assertTrue(f4.compareTo(f1) > 0);
    }

    // Tests toString and toProperString formats
    @Test
    public void testToStringAndToProperString() {
        Fraction f1 = Fraction.getFraction(3, 4);
        assertEquals("3/4", f1.toString());
        assertEquals("3/4", f1.toProperString());

        Fraction f2 = Fraction.getFraction(7, 4);
        assertEquals("7/4", f2.toString());
        assertEquals("1 3/4", f2.toProperString());

        Fraction f3 = Fraction.getFraction(0, 1);
        assertEquals("0/1", f3.toString());
        assertEquals("0", f3.toProperString());

        Fraction f4 = Fraction.getFraction(4, 4);
        assertEquals("1", f4.toProperString());

        Fraction f5 = Fraction.getFraction(-4, 4);
        assertEquals("-1", f5.toProperString());
    }
}