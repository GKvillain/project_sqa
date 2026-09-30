package org.apache.commons.lang.math;

import org.junit.Test;
import static org.junit.Assert.*;

public class FractionTest {

    // Tests normal case for getFraction(int, int)
    @Test
    public void testGetFraction_normalInputs_returnsCorrectFraction() {
        Fraction f = Fraction.getFraction(1, 2);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    // Tests denominator zero -> ArithmeticException
    @Test(expected = ArithmeticException.class)
    public void testGetFraction_denominatorZero_throwsArithmeticException() {
        Fraction.getFraction(1, 0);
    }

    // Tests negative denominator sign resolution
    @Test
    public void testGetFraction_negativeDenominator_resolvesSign() {
        Fraction f = Fraction.getFraction(1, -2);
        assertEquals(-1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    // Tests overflow when numerator = Integer.MIN_VALUE and denominator negative
    @Test(expected = ArithmeticException.class)
    public void testGetFraction_numeratorMINVALUE_denominatorNegative_throws() {
        Fraction.getFraction(Integer.MIN_VALUE, -1);
    }

    // Tests denominator = Integer.MIN_VALUE cannot be negated
    @Test(expected = ArithmeticException.class)
    public void testGetFraction_denominatorMINVALUE_throws() {
        Fraction.getFraction(1, Integer.MIN_VALUE);
    }

    // Tests getReducedFraction normal reduction
    @Test
    public void testGetReducedFraction_normal_returnsReduced() {
        Fraction f = Fraction.getReducedFraction(2, 4);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    // Tests getReducedFraction with zero numerator -> ZERO constant
    @Test
    public void testGetReducedFraction_zeroNumerator_returnsZERO() {
        assertSame(Fraction.ZERO, Fraction.getReducedFraction(0, 5));
    }

    // Tests getReducedFraction with denominator = Integer.MIN_VALUE and even numerator (handled)
    @Test
    public void testGetReducedFraction_denominatorMINVALUE_numeratorEven_reduces() {
        Fraction f = Fraction.getReducedFraction(2, Integer.MIN_VALUE);
        assertEquals(-1, f.getNumerator());
        assertEquals(1073741824, f.getDenominator());
    }

    // Tests getReducedFraction with denominator = Integer.MIN_VALUE and odd numerator (throws in current code)
    @Test(expected = ArithmeticException.class)
    public void testGetReducedFraction_denominatorMINVALUE_numeratorOdd_throws() {
        Fraction.getReducedFraction(1, Integer.MIN_VALUE);
    }

    // Tests getFraction(double) with a simple value
    @Test
    public void testGetFraction_doubleNormal_returnsApproximation() {
        Fraction f = Fraction.getFraction(0.5);
        assertEquals(1, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    // Tests getFraction(double) with NaN
    @Test(expected = ArithmeticException.class)
    public void testGetFraction_doubleNaN_throws() {
        Fraction.getFraction(Double.NaN);
    }

    // Tests getFraction(String) with a simple integer string
    @Test
    public void testGetFraction_stringInteger_returnsFraction() {
        Fraction f = Fraction.getFraction("5");
        assertEquals(5, f.getNumerator());
        assertEquals(1, f.getDenominator());
    }

    // Tests getFraction(String) with "X Y/Z" format
    @Test
    public void testGetFraction_stringMixed_returnsFraction() {
        Fraction f = Fraction.getFraction("1 1/2");
        assertEquals(3, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    // Tests getFraction(String) null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetFraction_stringNull_throws() {
        Fraction.getFraction(null);
    }

    // Tests add normal case
    @Test
    public void testAdd_normal_returnsSum() {
        Fraction a = Fraction.getFraction(1, 2);
        Fraction b = Fraction.getFraction(1, 3);
        Fraction sum = a.add(b);
        assertEquals(5, sum.getNumerator());
        assertEquals(6, sum.getDenominator());
    }

    // Tests subtract normal case
    @Test
    public void testSubtract_normal_returnsDifference() {
        Fraction a = Fraction.getFraction(3, 4);
        Fraction b = Fraction.getFraction(1, 4);
        Fraction diff = a.subtract(b);
        assertEquals(1, diff.getNumerator());
        assertEquals(2, diff.getDenominator());
    }

    // Tests multiplyBy normal case
    @Test
    public void testMultiplyBy_normal_returnsProduct() {
        Fraction a = Fraction.getFraction(2, 3);
        Fraction b = Fraction.getFraction(3, 4);
        Fraction prod = a.multiplyBy(b);
        assertEquals(1, prod.getNumerator());
        assertEquals(2, prod.getDenominator());
    }

    // Tests divideBy zero fraction -> ArithmeticException
    @Test(expected = ArithmeticException.class)
    public void testDivideBy_zeroDenominator_throws() {
        Fraction a = Fraction.getFraction(1, 2);
        a.divideBy(Fraction.ZERO);
    }

    // Tests invert of zero -> ArithmeticException
    @Test(expected = ArithmeticException.class)
    public void testInvert_zero_throws() {
        Fraction.ZERO.invert();
    }

    // Tests invert with numerator = Integer.MIN_VALUE -> overflow
    @Test(expected = ArithmeticException.class)
    public void testInvert_numeratorMINVALUE_throws() {
        Fraction f = Fraction.getFraction(Integer.MIN_VALUE, 1);
        f.invert();
    }

    // Tests negate with numerator = Integer.MIN_VALUE -> overflow
    @Test(expected = ArithmeticException.class)
    public void testNegate_numeratorMINVALUE_throws() {
        Fraction f = Fraction.getFraction(Integer.MIN_VALUE, 1);
        f.negate();
    }

    // Tests abs for positive fraction returns same instance
    @Test
    public void testAbs_positive_returnsSame() {
        Fraction f = Fraction.getFraction(3, 4);
        assertSame(f, f.abs());
    }

    // Tests abs for negative fraction returns positive
    @Test
    public void testAbs_negative_returnsPositive() {
        Fraction f = Fraction.getFraction(-3, 4);
        Fraction abs = f.abs();
        assertEquals(3, abs.getNumerator());
        assertEquals(4, abs.getDenominator());
    }

    // Tests compareTo equal fractions
    @Test
    public void testCompareTo_equal_returnsZero() {
        Fraction a = Fraction.getFraction(1, 2);
        Fraction b = Fraction.getFraction(1, 2);
        assertEquals(0, a.compareTo(b));
    }

    // Tests compareTo different fractions
    @Test
    public void testCompareTo_less_returnsNegative() {
        Fraction a = Fraction.getFraction(1, 3);
        Fraction b = Fraction.getFraction(1, 2);
        assertTrue(a.compareTo(b) < 0);
    }

    // Tests equals when fractions have same numerator and denominator
    @Test
    public void testEquals_sameValues_returnsTrue() {
        Fraction a = Fraction.getFraction(1, 2);
        Fraction b = Fraction.getFraction(1, 2);
        assertTrue(a.equals(b));
    }

    // Tests equals when fractions are not reduced
    @Test
    public void testEquals_differentRepresentations_returnsFalse() {
        Fraction a = Fraction.getFraction(1, 2);
        Fraction b = Fraction.getFraction(2, 4);
        assertFalse(a.equals(b));
    }

    // Tests toString format
    @Test
    public void testToString_normal_returnsSlashFormat() {
        Fraction f = Fraction.getFraction(3, 7);
        assertEquals("3/7", f.toString());
    }

    // Tests toProperString for improper fraction
    @Test
    public void testToProperString_improper_returnsMixedFormat() {
        Fraction f = Fraction.getFraction(7, 4);
        assertEquals("1 3/4", f.toProperString());
    }

    // Tests pow with power zero returns ONE
    @Test
    public void testPow_zero_returnsONE() {
        Fraction f = Fraction.getFraction(2, 3);
        assertSame(Fraction.ONE, f.pow(0));
    }

    // Tests pow with negative power (inversion)
    @Test
    public void testPow_negativeOne_returnsInverse() {
        Fraction f = Fraction.getFraction(2, 3);
        Fraction inv = f.pow(-1);
        assertEquals(3, inv.getNumerator());
        assertEquals(2, inv.getDenominator());
    }

    // Tests getFraction(int, int, int) normal case
    @Test
    public void testGetFraction_wholeNumber_returnsFraction() {
        Fraction f = Fraction.getFraction(1, 1, 2);
        assertEquals(3, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    // Tests getFraction(int, int, int) overflow
    @Test(expected = ArithmeticException.class)
    public void testGetFraction_whole_overflow_throws() {
        Fraction.getFraction(Integer.MAX_VALUE, 1, 1);
    }

    // Tests getFraction(int, int, int) negative whole number
    @Test
    public void testGetFraction_whole_negativeWhole_returnsCorrect() {
        Fraction f = Fraction.getFraction(-1, 1, 2);
        assertEquals(-3, f.getNumerator());
        assertEquals(2, f.getDenominator());
    }

    // Tests getFraction(String) with invalid format
    @Test(expected = NumberFormatException.class)
    public void testGetFraction_stringInvalid_throws() {
        Fraction.getFraction("abc");
    }
}