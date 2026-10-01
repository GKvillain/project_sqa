package org.apache.commons.math3.fraction;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NullArgumentException;

public class FractionTest {

    @Test(expected = MathArithmeticException.class)
    public void testConstructor_int_int_zeroDenominator_throwsMathArithmeticException() {
        new Fraction(1, 0);
    }

    @Test
    public void testConstructor_int_int_negativeDenominator_normal() {
        Fraction f = new Fraction(1, -2);
        assertEquals(-1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testConstructor_int_int_overflowNegativeDenominator_throwsMathArithmeticException() {
        new Fraction(Integer.MIN_VALUE, -1);
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructor_doubleValue_tooLarge_throwsFractionConversionException() {
        new Fraction(3e9);
    }

    @Test
    public void testConstructor_doubleValue_minValueWithMaxDenominator_doesNotThrowFractionConversionException() {
        Fraction f = new Fraction(Double.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(f);
        assertEquals(0, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructor_doubleValue_smallMaxIterations_throwsFractionConversionException() {
        new Fraction(Math.PI, 1e-5, 1);
    }

    @Test
    public void testAbs_negativeValue_returnsPositive() {
        Fraction f = new Fraction(-1, 3);
        Fraction abs = f.abs();
        assertEquals(1, abs.getNumerator());
        assertEquals(3, abs.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testNegate_overflow_throwsMathArithmeticException() {
        new Fraction(Integer.MIN_VALUE, 1).negate();
    }

    @Test(expected = MathArithmeticException.class)
    public void testReciprocal_ofZero_throwsMathArithmeticException() {
        Fraction.ZERO.reciprocal();
    }

    @Test
    public void testEquals_multipleConditions() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(2, 4);
        Fraction c = new Fraction(3, 4);
        assertTrue(a.equals(a));
        assertTrue(a.equals(b));
        assertFalse(a.equals(c));
        assertFalse(a.equals(null));
        assertFalse(a.equals("string"));
    }

    @Test
    public void testCompareTo_equal_returnsZero() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(2, 4);
        assertEquals(0, a.compareTo(b));
    }

    @Test(expected = NullArgumentException.class)
    public void testAdd_null_throwsNullArgumentException() {
        Fraction.ONE.add(null);
    }

    @Test(expected = NullArgumentException.class)
    public void testMultiply_null_throwsNullArgumentException() {
        Fraction.ONE.multiply(null);
    }

    @Test(expected = MathArithmeticException.class)
    public void testAdd_subtract_overflow_throwsMathArithmeticException() {
        Fraction f1 = new Fraction(1, Integer.MAX_VALUE);
        Fraction f2 = new Fraction(1, Integer.MAX_VALUE - 1);
        f1.add(f2);
    }

    @Test(expected = MathArithmeticException.class)
    public void testDivide_byZero_throwsMathArithmeticException() {
        Fraction.ONE.divide(Fraction.ZERO);
    }

    @Test(expected = MathArithmeticException.class)
    public void testGetReducedFraction_zeroDenominator_throwsMathArithmeticException() {
        Fraction.getReducedFraction(1, 0);
    }

    @Test
    public void testGetReducedFraction_denominatorMinValue_evenNumerator() {
        Fraction f = Fraction.getReducedFraction(2, Integer.MIN_VALUE);
        assertEquals(-1, f.getNumerator());
        assertEquals(1073741824, f.getDenominator());
    }

    @Test
    public void testToString_multipleConditions() {
        assertEquals("0", Fraction.ZERO.toString());
        assertEquals("5", new Fraction(5).toString());
        assertEquals("1 / 2", new Fraction(1, 2).toString());
    }

    @Test
    public void testMultiply_normal_returnsReducedFraction() {
        Fraction a = new Fraction(2, 3);
        Fraction b = new Fraction(3, 4);
        Fraction result = a.multiply(b);
        assertEquals(1, result.getNumerator());
        assertEquals(2, result.getDenominator());
    }

    @Test
    public void testAdd_normal_returnsReducedFraction() {
        Fraction a = new Fraction(1, 3);
        Fraction b = new Fraction(1, 6);
        Fraction result = a.add(b);
        assertEquals(1, result.getNumerator());
        assertEquals(2, result.getDenominator());
    }

    @Test
    public void testAdd_zeroNumeratorBehavior() {
        assertEquals(new Fraction(1, 2), Fraction.ZERO.add(new Fraction(1, 2)));
        assertEquals(new Fraction(-1, 2), Fraction.ZERO.subtract(new Fraction(1, 2)));
        assertEquals(Fraction.ONE, Fraction.ONE.add(Fraction.ZERO));
    }

    // New tests for uncovered branches
    
    @Test(expected = NullArgumentException.class)
    public void testSubtract_null_throwsNullArgumentException() {
        Fraction.ONE.subtract(null);
    }

    @Test(expected = NullArgumentException.class)
    public void testDivide_null_throwsNullArgumentException() {
        Fraction.ONE.divide(null);
    }

    @Test
    public void testAbs_zero_returnsZero() {
        assertEquals(Fraction.ZERO, Fraction.ZERO.abs());
    }

    @Test
    public void testAbs_positive_returnsSame() {
        Fraction f = new Fraction(3, 4);
        assertSame(f, f.abs());
    }

    @Test
    public void testNegate_positive_returnsNegative() {
        Fraction f = new Fraction(3, 4);
        Fraction neg = f.negate();
        assertEquals(-3, neg.getNumerator());
        assertEquals(4, neg.getDenominator());
    }

    @Test
    public void testNegate_negative_returnsPositive() {
        Fraction f = new Fraction(-3, 4);
        Fraction neg = f.negate();
        assertEquals(3, neg.getNumerator());
        assertEquals(4, neg.getDenominator());
    }

    @Test
    public void testReciprocal_normal() {
        Fraction f = new Fraction(2, 3);
        Fraction rec = f.reciprocal();
        assertEquals(3, rec.getNumerator());
        assertEquals(2, rec.getDenominator());
    }

    @Test(expected = FractionConversionException.class)
    public void testConstructor_doubleValue_negativeToLarge_throwsFractionConversionException() {
        new Fraction(-3e9);
    }

    @Test
    public void testConstructor_doubleValue_zero_returnsZero() {
        Fraction f = new Fraction(0.0);
        assertEquals(0, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test
    public void testConstructor_doubleValue_one_returnsOne() {
        Fraction f = new Fraction(1.0);
        assertEquals(1, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test
    public void testCompareTo_lessThan_returnsNegative() {
        Fraction a = new Fraction(1, 3);
        Fraction b = new Fraction(1, 2);
        assertTrue(a.compareTo(b) < 0);
    }

    @Test
    public void testCompareTo_greaterThan_returnsPositive() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(1, 3);
        assertTrue(a.compareTo(b) > 0);
    }

    @Test(expected = NullArgumentException.class)
    public void testCompareTo_null_throwsNullArgumentException() {
        new Fraction(1, 2).compareTo(null);
    }

    @Test
    public void testDoubleValue() {
        Fraction f = new Fraction(1, 4);
        assertEquals(0.25, f.doubleValue(), 1e-9);
    }

    @Test
    public void testFloatValue() {
        Fraction f = new Fraction(1, 4);
        assertEquals(0.25f, f.floatValue(), 1e-6f);
    }

    @Test
    public void testIntValue() {
        Fraction f = new Fraction(5, 2);
        assertEquals(2, f.intValue());
    }

    @Test
    public void testLongValue() {
        Fraction f = new Fraction(5, 2);
        assertEquals(2L, f.longValue());
    }

    @Test
    public void testHashCode() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(2, 4);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testGetReducedFraction_normal() {
        Fraction f = Fraction.getReducedFraction(2, 4);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    @Test
    public void testGetReducedFraction_zeroNumerator_zeroDenominator() {
        Fraction f = Fraction.getReducedFraction(0, 5);
        assertEquals(0, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    @Test
    public void testAdd_differentDenominators_correctReduction() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(1, 3);
        Fraction result = a.add(b);
        assertEquals(5, result.getNumerator());
        assertEquals(6, result.getDenominator());
    }

    @Test
    public void testSubtract_normal() {
        Fraction a = new Fraction(2, 3);
        Fraction b = new Fraction(1, 6);
        Fraction result = a.subtract(b);
        assertEquals(1, result.getNumerator());
        assertEquals(2, result.getDenominator());
    }

    @Test
    public void testMultiply_zero_returnsZero() {
        Fraction result = Fraction.ZERO.multiply(new Fraction(2, 3));
        assertEquals(Fraction.ZERO, result);
    }

    @Test
    public void testDivide_normal() {
        Fraction a = new Fraction(2, 3);
        Fraction b = new Fraction(3, 4);
        Fraction result = a.divide(b);
        assertEquals(8, result.getNumerator());
        assertEquals(9, result.getDenominator());
    }

    @Test(expected = MathArithmeticException.class)
    public void testConstructor_int_int_overflowMultiplication_throwsMathArithmeticException() {
        new Fraction(Integer.MAX_VALUE, 2);
    }

    @Test(expected = MathArithmeticException.class)
    public void testConstructor_doubleValue_maxDenominator1_throwsFractionConversionException() {
        new Fraction(0.5, 1);
    }

    @Test
    public void testConstructor_doubleValue_exactFraction_maxDenominator() {
        Fraction f = new Fraction(0.5, 100);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }
}