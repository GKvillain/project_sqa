package org.apache.commons.math3.dfp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class DfpTest {

    private DfpField factory;

    @Before
    public void setUp() {
        factory = new DfpField(6);
    }

    // Tests multiply by int when result is NaN, Infinite, or valid single digit (Bug Math-17: multiply(int) vs multiplyFast(int) with larger int or edge cases)
    @Test
    public void testMultiply_intValues_returnsCorrectProduct() {
        Dfp a = factory.newDfp("12.34");
        Dfp res = a.multiply(10000);
        assertEquals(factory.newDfp("123400"), res);

        Dfp b = factory.newDfp("12.34");
        Dfp res2 = b.multiply(0);
        assertEquals(factory.getZero(), res2);

        Dfp c = factory.newDfp("12.34");
        Dfp res3 = c.multiply(-1);
        assertEquals(factory.newDfp("-12.34"), res3);

        Dfp d = factory.newDfp("12.34");
        Dfp res4 = d.multiply(123456);
        assertEquals(factory.newDfp("1523447.04"), res4);
    }

    // Tests multiply special cases like Infinity * 0 and NaN
    @Test
    public void testMultiply_specialCases_returnsExpected() {
        Dfp inf = factory.newDfp((byte) 1, Dfp.INFINITE);
        Dfp zero = factory.getZero();
        Dfp nan = factory.newDfp((byte) 1, Dfp.QNAN);

        assertTrue(inf.multiply(zero).isNaN());
        assertTrue(nan.multiply(factory.getOne()).isNaN());
        assertTrue(inf.multiply(2).isInfinite());
        assertTrue(inf.multiply(0).isNaN());
    }

    // Tests division by Dfp and int including divide by zero
    @Test
    public void testDivide_validAndDivideByZero_returnsExpected() {
        Dfp a = factory.newDfp("100");
        Dfp b = factory.newDfp("2");
        assertEquals(factory.newDfp("50"), a.divide(b));
        assertEquals(factory.newDfp("50"), a.divide(2));

        Dfp zero = factory.getZero();
        assertTrue(a.divide(zero).isInfinite());
        assertTrue(a.divide(0).isInfinite());

        Dfp inf = factory.newDfp((byte) 1, Dfp.INFINITE);
        assertTrue(a.divide(inf).equals(zero));
        assertTrue(inf.divide(inf).isNaN());
    }

    // Tests add and subtract with positive, negative, and infinite values
    @Test
    public void testAddAndSubtract_variousInputs_returnsCorrectResult() {
        Dfp a = factory.newDfp("12.34");
        Dfp b = factory.newDfp("56.78");

        assertEquals(factory.newDfp("69.12"), a.add(b));
        assertEquals(factory.newDfp("-44.44"), a.subtract(b));

        Dfp inf = factory.newDfp((byte) 1, Dfp.INFINITE);
        Dfp ninf = factory.newDfp((byte) -1, Dfp.INFINITE);
        assertTrue(inf.add(ninf).isNaN());
        assertTrue(inf.add(a).isInfinite());
    }

    // Tests comparison methods: lessThan, greaterThan, equals, unequal
    @Test
    public void testComparisons_variousConditions_returnsCorrectBooleans() {
        Dfp a = factory.newDfp("12.34");
        Dfp b = factory.newDfp("56.78");
        Dfp nan = factory.newDfp((byte) 1, Dfp.QNAN);

        assertTrue(a.lessThan(b));
        assertFalse(b.lessThan(a));
        assertTrue(b.greaterThan(a));
        assertFalse(a.greaterThan(b));

        assertFalse(a.lessThan(nan));
        assertFalse(a.greaterThan(nan));
        assertFalse(a.equals(nan));
        assertFalse(nan.equals(nan));

        assertTrue(a.unequal(b));
        assertFalse(a.unequal(factory.newDfp("12.34")));
        assertFalse(a.unequal(nan));
    }

    // Tests sign checks: positiveOrNull, strictlyPositive, negativeOrNull, strictlyNegative, isZero
    @Test
    public void testSignChecks_variousInputs_returnsCorrectBooleans() {
        Dfp pos = factory.newDfp("1.0");
        Dfp neg = factory.newDfp("-1.0");
        Dfp zero = factory.getZero();
        Dfp nan = factory.newDfp((byte) 1, Dfp.QNAN);

        assertTrue(pos.strictlyPositive());
        assertTrue(pos.positiveOrNull());
        assertFalse(pos.strictlyNegative());
        assertFalse(pos.negativeOrNull());
        assertFalse(pos.isZero());

        assertTrue(neg.strictlyNegative());
        assertTrue(neg.negativeOrNull());
        assertFalse(neg.strictlyPositive());
        assertFalse(neg.positiveOrNull());

        assertTrue(zero.positiveOrNull());
        assertTrue(zero.negativeOrNull());
        assertFalse(zero.strictlyPositive());
        assertFalse(zero.strictlyNegative());
        assertTrue(zero.isZero());

        assertFalse(nan.isZero());
        assertFalse(nan.strictlyPositive());
        assertFalse(nan.strictlyNegative());
        assertFalse(nan.positiveOrNull());
        assertFalse(nan.negativeOrNull());
    }

    // Tests sqrt method for normal numbers, zero, negative numbers, and special values
    @Test
    public void testSqrt_variousInputs_returnsExpectedRoots() {
        Dfp a = factory.newDfp("4.0");
        assertEquals(factory.newDfp("2.0"), a.sqrt());

        Dfp zero = factory.getZero();
        assertEquals(zero, zero.sqrt());

        Dfp neg = factory.newDfp("-4.0");
        assertTrue(neg.sqrt().isNaN());

        Dfp inf = factory.newDfp((byte) 1, Dfp.INFINITE);
        assertTrue(inf.sqrt().isInfinite());
    }

    // Tests rint, floor, ceil, and remainder
    @Test
    public void testRoundingAndRemainder_variousInputs_returnsExpectedResults() {
        Dfp num = factory.newDfp("1.5");
        assertEquals(factory.newDfp("2.0"), num.rint());
        assertEquals(factory.newDfp("1.0"), num.floor());
        assertEquals(factory.newDfp("2.0"), num.ceil());

        Dfp num2 = factory.newDfp("-1.5");
        assertEquals(factory.newDfp("-2.0"), num2.rint());
        assertEquals(factory.newDfp("-2.0"), num2.floor());
        assertEquals(factory.newDfp("-1.0"), num2.ceil());

        Dfp dividend = factory.newDfp("5.0");
        Dfp divisor = factory.newDfp("2.0");
        assertEquals(factory.newDfp("1.0"), dividend.remainder(divisor));
    }

    // Tests string conversions: toString, scientific notation, normal string
    @Test
    public void testToString_variousRepresentations_returnsFormattedStrings() {
        Dfp a = factory.newDfp("12.34");
        assertEquals("12.34", a.toString());

        Dfp b = factory.newDfp("1e30");
        assertTrue(b.toString().contains("e"));

        Dfp inf = factory.newDfp((byte) 1, Dfp.INFINITE);
        assertEquals("Infinity", inf.toString());

        Dfp ninf = factory.newDfp((byte) -1, Dfp.INFINITE);
        assertEquals("-Infinity", ninf.toString());

        Dfp nan = factory.newDfp((byte) 1, Dfp.QNAN);
        assertEquals("NaN", nan.toString());
    }

    // Tests conversions to double, int, split double, and power operations
    @Test
    public void testConversionsAndPowers_variousInputs_returnsCorrectValues() {
        Dfp a = factory.newDfp("123.456");
        assertEquals(123.456, a.toDouble(), 1e-9);
        assertEquals(123, a.intValue());

        double[] split = a.toSplitDouble();
        assertEquals(123.456, split[0] + split[1], 1e-9);

        Dfp p10 = a.power10(2);
        assertEquals(factory.newDfp("100"), p10);

        Dfp p10k = a.power10K(1);
        assertEquals(factory.newDfp("10000"), p10k);
    }

    // Tests copysign and abs
    @Test
    public void testCopysignAndAbs_variousSigns_returnsCorrectSignedDfp() {
        Dfp pos = factory.newDfp("12.34");
        Dfp neg = factory.newDfp("-56.78");

        assertEquals(pos, neg.abs());
        assertEquals(factory.newDfp("-12.34"), Dfp.copysign(pos, neg));
        assertEquals(factory.newDfp("56.78"), Dfp.copysign(neg, pos));
    }

    // Tests nextAfter method towards positive/negative directions
    @Test
    public void testNextAfter_variousDirections_returnsNextRepresentableNumber() {
        Dfp a = factory.newDfp("1.0");
        Dfp targetGreater = factory.newDfp("2.0");
        Dfp targetLess = factory.newDfp("0.0");

        Dfp nextUp = a.nextAfter(targetGreater);
        assertTrue(nextUp.greaterThan(a));

        Dfp nextDown = a.nextAfter(targetLess);
        assertTrue(nextDown.lessThan(a));

        assertEquals(a, a.nextAfter(a));
    }

    // Tests constructors with byte, int, long, double, and Long.MIN_VALUE edge cases
    @Test
    public void testConstructors_primitiveTypes_constructsCorrectly() {
        Dfp fromByte = factory.newDfp((byte) 12);
        assertEquals(factory.newDfp("12"), fromByte);

        Dfp fromInt = factory.newDfp(12345);
        assertEquals(factory.newDfp("12345"), fromInt);

        Dfp fromLong = factory.newDfp(Long.MIN_VALUE);
        assertEquals(factory.newDfp("-9223372036854775808"), fromLong);

        Dfp fromDouble = factory.newDfp(1.25);
        assertEquals(factory.newDfp("1.25"), fromDouble);

        Dfp fromDoubleInf = factory.newDfp(Double.POSITIVE_INFINITY);
        assertTrue(fromDoubleInf.isInfinite());

        Dfp fromDoubleNaN = factory.newDfp(Double.NaN);
        assertTrue(fromDoubleNaN.isNaN());
    }

    // Tests newInstance with mismatched field precision triggers trap/invalid flag
    @Test
    public void testMismatchedFieldPrecision_operations_returnsNaN() {
        DfpField otherField = new DfpField(12);
        Dfp a = factory.newDfp("1.0");
        Dfp b = otherField.newDfp("1.0");

        Dfp res = a.add(b);
        assertTrue(res.isNaN());
        assertFalse(a.lessThan(b));
        assertFalse(a.greaterThan(b));
    }
}