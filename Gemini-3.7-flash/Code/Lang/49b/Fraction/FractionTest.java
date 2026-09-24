package org.apache.commons.lang.math;

import org.junit.Test;
import static org.junit.Assert.*;

public class FractionTest {

    // Tests Lang-49 bug: reduce with zero numerator
    @Test
    public void testReduce_zeroNumerator_returnsZeroInstance() {
        Fraction f = Fraction.getFraction(0, 100);
        Fraction res = f.reduce();
        assertEquals(0, res.getNumerator());
        assertEquals(1, res.getDenominator());
        assertSame(Fraction.ZERO, res);
    }

    // Tests normal reduction
    @Test
    public void testReduce_simplifiableFraction_returnsReducedInstance() {
        Fraction f = Fraction.getFraction(2, 4);
        Fraction res = f.reduce();
        assertEquals(1, res.getNumerator());
        assertEquals(2, res.getDenominator());

        Fraction irreducible = Fraction.getFraction(1, 3);
        assertSame(irreducible, irreducible.reduce());
    }

    // Tests factory method with 2 integers
    @Test
    public void testGetFraction_twoIntegers_handlesNegativeAndSigns() {
        Fraction f1 = Fraction.getFraction(3, 4);
        assertEquals(3, f1.getNumerator());
        assertEquals(4, f1.getDenominator());

        Fraction f2 = Fraction.getFraction(3, -4);
        assertEquals(-3, f2.getNumerator());
        assertEquals(4, f2.getDenominator());

        Fraction f3 = Fraction.getFraction(-3, -4);
        assertEquals(3, f3.getNumerator());
        assertEquals(4, f3.getDenominator());
    }

    // Tests factory method with zero denominator exception
    @Test(expected = ArithmeticException.class)
    public void testGetFraction_zeroDenominator_throwsArithmeticException() {
        Fraction.getFraction(1, 0);
    }

    // Tests factory method overflow on negation with Integer.MIN_VALUE
    @Test(expected = ArithmeticException.class)
    public void testGetFraction_minIntDenominator_throwsArithmeticException() {
        Fraction.getFraction(1, Integer.MIN_VALUE);
    }

    // Tests factory method with whole, numerator, and denominator
    @Test
    public void testGetFraction_threeIntegers_returnsCorrectFraction() {
        Fraction f1 = Fraction.getFraction(1, 2, 3);
        assertEquals(5, f1.getNumerator());
        assertEquals(3, f1.getDenominator());

        Fraction f2 = Fraction.getFraction(-1, 2, 3);
        assertEquals(-5, f2.getNumerator());
        assertEquals(3, f2.getDenominator());
    }

    // Tests getReducedFraction factory method
    @Test
    public void testGetReducedFraction_variousInputs_returnsReducedFraction() {
        assertSame(Fraction.ZERO, Fraction.getReducedFraction(0, 5));
        assertEquals(Fraction.getFraction(1, 2), Fraction.getReducedFraction(2, 4));
        assertEquals(Fraction.getFraction(-1, 2), Fraction.getReducedFraction(2, -4));
    }

    // Tests getFraction from double
    @Test
    public void testGetFraction_doubleValue_convertsAccurately() {
        Fraction f1 = Fraction.getFraction(0.5);
        assertEquals(1, f1.getNumerator());
        assertEquals(2, f1.getDenominator());

        Fraction f2 = Fraction.getFraction(-0.75);
        assertEquals(-3, f2.getNumerator());
        assertEquals(4, f2.getDenominator());
    }

    // Tests getFraction from String with various formats
    @Test
    public void testGetFraction_stringInput_parsesCorrectly() {
        assertEquals(Fraction.getFraction(1, 2), Fraction.getFraction("0.5"));
        assertEquals(Fraction.getFraction(5, 3), Fraction.getFraction("1 2/3"));
        assertEquals(Fraction.getFraction(3, 4), Fraction.getFraction("3/4"));
        assertEquals(Fraction.getFraction(5, 1), Fraction.getFraction("5"));
    }

    // Tests getFraction with null String
    @Test(expected = IllegalArgumentException.class)
    public void testGetFraction_nullString_throwsException() {
        Fraction.getFraction(null);
    }

    // Tests accessors and proper fractions
    @Test
    public void testAccessors_improperAndNegative_returnsCorrectParts() {
        Fraction f = Fraction.getFraction(7, 4);
        assertEquals(7, f.getNumerator());
        assertEquals(4, f.getDenominator());
        assertEquals(3, f.getProperNumerator());
        assertEquals(1, f.getProperWhole());

        Fraction neg = Fraction.getFraction(-7, 4);
        assertEquals(3, neg.getProperNumerator());
        assertEquals(-1, neg.getProperWhole());
    }

    // Tests Number methods
    @Test
    public void testNumberConversions_fractionValues_returnsCorrectPrimitves() {
        Fraction f = Fraction.getFraction(3, 2);
        assertEquals(1, f.intValue());
        assertEquals(1L, f.longValue());
        assertEquals(1.5f, f.floatValue(), 0.0001f);
        assertEquals(1.5d, f.doubleValue(), 0.0001d);
    }

    // Tests inversion, negation, and absolute value
    @Test
    public void testInvertNegateAbs_variousCases_returnsExpectedResults() {
        Fraction f = Fraction.getFraction(-2, 3);
        assertEquals(Fraction.getFraction(-3, 2), f.invert());
        assertEquals(Fraction.getFraction(2, 3), f.negate());
        assertEquals(Fraction.getFraction(2, 3), f.abs());

        Fraction pos = Fraction.getFraction(2, 3);
        assertSame(pos, pos.abs());
    }

    // Tests invert of zero
    @Test(expected = ArithmeticException.class)
    public void testInvert_zeroNumerator_throwsArithmeticException() {
        Fraction.ZERO.invert();
    }

    // Tests power calculations
    @Test
    public void testPow_variousPowers_calculatesCorrectly() {
        Fraction f = Fraction.getFraction(2, 3);
        assertSame(f, f.pow(1));
        assertEquals(Fraction.ONE, f.pow(0));
        assertEquals(Fraction.getFraction(4, 9), f.pow(2));
        assertEquals(Fraction.getFraction(8, 27), f.pow(3));
        assertEquals(Fraction.getFraction(9, 4), f.pow(-2));
    }

    // Tests addition and subtraction
    @Test
    public void testAddAndSubtract_validFractions_computesCorrectResult() {
        Fraction f1 = Fraction.getFraction(1, 3);
        Fraction f2 = Fraction.getFraction(1, 6);

        assertEquals(Fraction.getFraction(1, 2), f1.add(f2));
        assertEquals(Fraction.getFraction(1, 6), f1.subtract(f2));

        assertEquals(f1, f1.add(Fraction.ZERO));
        assertEquals(f1, Fraction.ZERO.add(f1));
        assertEquals(f1.negate(), Fraction.ZERO.subtract(f1));
    }

    // Tests multiplication and division
    @Test
    public void testMultiplyAndDivide_validFractions_computesCorrectResult() {
        Fraction f1 = Fraction.getFraction(2, 3);
        Fraction f2 = Fraction.getFraction(3, 4);

        assertEquals(Fraction.getFraction(1, 2), f1.multiplyBy(f2));
        assertEquals(Fraction.getFraction(8, 9), f1.divideBy(f2));

        assertSame(Fraction.ZERO, f1.multiplyBy(Fraction.ZERO));
    }

    // Tests division by zero fraction
    @Test(expected = ArithmeticException.class)
    public void testDivideBy_zeroFraction_throwsArithmeticException() {
        Fraction.getFraction(1, 2).divideBy(Fraction.ZERO);
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_variousObjects_returnsExpected() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(1, 2);
        Fraction f3 = Fraction.getFraction(2, 4);

        assertEquals(f1, f1);
        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
        assertFalse(f1.equals(f3));
        assertFalse(f1.equals(null));
        assertFalse(f1.equals("1/2"));
    }

    // Tests compareTo
    @Test
    public void testCompareTo_differentFractions_ordersCorrectly() {
        Fraction f1 = Fraction.getFraction(1, 2);
        Fraction f2 = Fraction.getFraction(2, 4);
        Fraction f3 = Fraction.getFraction(3, 4);

        assertEquals(0, f1.compareTo(f2));
        assertTrue(f1.compareTo(f3) < 0);
        assertTrue(f3.compareTo(f1) > 0);
    }

    // Tests toString and toProperString
    @Test
    public void testToStringAndToProperString_formatsCorrectly() {
        Fraction f1 = Fraction.getFraction(7, 4);
        assertEquals("7/4", f1.toString());
        assertEquals("1 3/4", f1.toProperString());

        Fraction f2 = Fraction.getFraction(-7, 4);
        assertEquals("-7/4", f2.toString());
        assertEquals("-1 3/4", f2.toProperString());

        Fraction f3 = Fraction.getFraction(0, 1);
        assertEquals("0", f3.toProperString());

        Fraction f4 = Fraction.getFraction(4, 4);
        assertEquals("1", f4.toProperString());

        Fraction f5 = Fraction.getFraction(-4, 4);
        assertEquals("-1", f5.toProperString());
    }
}