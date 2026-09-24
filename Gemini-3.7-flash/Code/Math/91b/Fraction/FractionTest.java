package org.apache.commons.math.fraction;

import org.junit.Test;
import static org.junit.Assert.*;

public class FractionTest {

    // Tests compareTo with close fractions (Defects4J Math-91 regression)
    @Test
    public void testCompareTo_closeFractions_returnsCorrectComparison() {
        Fraction pi1 = new Fraction(105115, 33457);
        Fraction pi2 = new Fraction(105116, 33457);
        assertTrue(pi1.compareTo(pi2) < 0);
        assertTrue(pi2.compareTo(pi1) > 0);
        assertEquals(0, pi1.compareTo(pi1));
    }

    // Tests compareTo basic behavior
    @Test
    public void testCompareTo_standardFractions_returnsExpectedSigns() {
        Fraction half = new Fraction(1, 2);
        Fraction third = new Fraction(1, 3);
        Fraction minusHalf = new Fraction(-1, 2);

        assertTrue(half.compareTo(third) > 0);
        assertTrue(third.compareTo(half) < 0);
        assertTrue(minusHalf.compareTo(half) < 0);
        assertEquals(0, half.compareTo(new Fraction(2, 4)));
    }

    // Tests constructor with double value conversion
    @Test
    public void testConstructorDouble_validValues_convertsProperly() throws Exception {
        Fraction f1 = new Fraction(0.5);
        assertEquals(1, f1.getNumerator());
        assertEquals(2, f1.getDenominator());

        Fraction f2 = new Fraction(2.0);
        assertEquals(2, f2.getNumerator());
        assertEquals(1, f2.getDenominator());
    }

    // Tests constructor double with max denominator
    @Test
    public void testConstructorDouble_withMaxDenominator_convertsProperly() throws Exception {
        Fraction f = new Fraction(0.333333, 10);
        assertEquals(1, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    // Tests constructor with double overflow exception
    @Test(expected = FractionConversionException.class)
    public void testConstructorDouble_overflow_throwsException() throws Exception {
        new Fraction((double) Integer.MAX_VALUE + 1000.0, 1.0e-5, 100);
    }

    // Tests integer constructor reduction and negative sign normalization
    @Test
    public void testConstructorIntInt_normalAndReducedValues_success() {
        Fraction f = new Fraction(6, -8);
        assertEquals(-3, f.getNumerator());
        assertEquals(4, f.getDenominator());

        Fraction f2 = new Fraction(-6, -8);
        assertEquals(3, f2.getNumerator());
        assertEquals(4, f2.getDenominator());
    }

    // Tests integer constructor with zero denominator
    @Test(expected = ArithmeticException.class)
    public void testConstructorIntInt_zeroDenominator_throwsArithmeticException() {
        new Fraction(1, 0);
    }

    // Tests integer constructor with Integer.MIN_VALUE denominator overflow
    @Test(expected = ArithmeticException.class)
    public void testConstructorIntInt_minIntDenominator_throwsArithmeticException() {
        new Fraction(1, Integer.MIN_VALUE);
    }

    // Tests abs method
    @Test
    public void testAbs_positiveAndNegative_returnsAbsoluteFraction() {
        Fraction f1 = new Fraction(-3, 4).abs();
        assertEquals(3, f1.getNumerator());
        assertEquals(4, f1.getDenominator());

        Fraction f2 = new Fraction(3, 4).abs();
        assertSame(f2, f2.abs());
    }

    // Tests negate method and overflow
    @Test
    public void testNegate_validFraction_returnsNegated() {
        Fraction f = new Fraction(3, 4).negate();
        assertEquals(-3, f.getNumerator());
        assertEquals(4, f.getDenominator());
    }

    // Tests negate method integer overflow
    @Test(expected = ArithmeticException.class)
    public void testNegate_minIntNumerator_throwsArithmeticException() {
        new Fraction(Integer.MIN_VALUE, 1).negate();
    }

    // Tests reciprocal method
    @Test
    public void testReciprocal_validFraction_returnsReciprocal() {
        Fraction f = new Fraction(3, 4).reciprocal();
        assertEquals(4, f.getNumerator());
        assertEquals(3, f.getDenominator());
    }

    // Tests add method
    @Test
    public void testAdd_validFractions_returnsSum() {
        Fraction f1 = new Fraction(1, 3);
        Fraction f2 = new Fraction(1, 6);
        Fraction sum = f1.add(f2);
        assertEquals(1, sum.getNumerator());
        assertEquals(2, sum.getDenominator());

        // Addition with zero
        assertEquals(f1, f1.add(Fraction.ZERO));
        assertEquals(f1, Fraction.ZERO.add(f1));
    }

    // Tests add method with null parameter
    @Test(expected = IllegalArgumentException.class)
    public void testAdd_nullFraction_throwsIllegalArgumentException() {
        Fraction.ONE.add(null);
    }

    // Tests subtract method
    @Test
    public void testSubtract_validFractions_returnsDifference() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(1, 3);
        Fraction diff = f1.subtract(f2);
        assertEquals(1, diff.getNumerator());
        assertEquals(6, diff.getDenominator());

        assertEquals(f1, f1.subtract(Fraction.ZERO));
        assertEquals(f1.negate(), Fraction.ZERO.subtract(f1));
    }

    // Tests multiply method
    @Test
    public void testMultiply_validFractions_returnsProduct() {
        Fraction f1 = new Fraction(2, 3);
        Fraction f2 = new Fraction(3, 4);
        Fraction product = f1.multiply(f2);
        assertEquals(1, product.getNumerator());
        assertEquals(2, product.getDenominator());

        assertEquals(Fraction.ZERO, f1.multiply(Fraction.ZERO));
    }

    // Tests multiply method with null parameter
    @Test(expected = IllegalArgumentException.class)
    public void testMultiply_nullFraction_throwsIllegalArgumentException() {
        Fraction.ONE.multiply(null);
    }

    // Tests divide method
    @Test
    public void testDivide_validFractions_returnsQuotient() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(3, 4);
        Fraction quotient = f1.divide(f2);
        assertEquals(2, quotient.getNumerator());
        assertEquals(3, quotient.getDenominator());
    }

    // Tests divide method by zero fraction
    @Test(expected = ArithmeticException.class)
    public void testDivide_zeroFraction_throwsArithmeticException() {
        Fraction.ONE.divide(Fraction.ZERO);
    }

    // Tests getReducedFraction utility method
    @Test
    public void testGetReducedFraction_variousInputs_returnsReduced() {
        Fraction f1 = Fraction.getReducedFraction(0, 5);
        assertEquals(0, f1.getNumerator());
        assertEquals(1, f1.getDenominator());

        Fraction f2 = Fraction.getReducedFraction(2, 4);
        assertEquals(1, f2.getNumerator());
        assertEquals(2, f2.getDenominator());

        Fraction f3 = Fraction.getReducedFraction(2, -4);
        assertEquals(-1, f3.getNumerator());
        assertEquals(2, f3.getDenominator());

        Fraction f4 = Fraction.getReducedFraction(Integer.MIN_VALUE, 2);
        assertEquals(Integer.MIN_VALUE / 2, f4.getNumerator());
        assertEquals(1, f4.getDenominator());
    }

    // Tests getReducedFraction divide by zero
    @Test(expected = ArithmeticException.class)
    public void testGetReducedFraction_zeroDenominator_throwsArithmeticException() {
        Fraction.getReducedFraction(1, 0);
    }

    // Tests equality and hash code
    @Test
    public void testEqualsAndHashCode_variousCases_returnsExpected() {
        Fraction f1 = new Fraction(1, 2);
        Fraction f2 = new Fraction(2, 4);
        Fraction f3 = new Fraction(1, 3);

        assertTrue(f1.equals(f1));
        assertTrue(f1.equals(f2));
        assertFalse(f1.equals(f3));
        assertFalse(f1.equals(null));
        assertFalse(f1.equals("not a fraction"));

        assertEquals(f1.hashCode(), f2.hashCode());
    }

    // Tests primitive numeric conversions
    @Test
    public void testConversions_validFraction_returnsCorrectPrimitives() {
        Fraction f = new Fraction(3, 2);
        assertEquals(1.5, f.doubleValue(), 1e-9);
        assertEquals(1.5f, f.floatValue(), 1e-9f);
        assertEquals(1, f.intValue());
        assertEquals(1L, f.longValue());
    }
}