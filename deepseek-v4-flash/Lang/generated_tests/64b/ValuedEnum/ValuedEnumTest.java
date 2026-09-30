package org.apache.commons.lang.enums;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

/**
 * JUnit 4 test class for ValuedEnum.
 * Targets defect 64b in Defects4J Lang project.
 */
public class ValuedEnumTest {

    // Test enum subclass for testing
    private static final class TestValuedEnum extends ValuedEnum {
        private static final long serialVersionUID = 1L;
        public static final TestValuedEnum ONE = new TestValuedEnum("ONE", 1);
        public static final TestValuedEnum TWO = new TestValuedEnum("TWO", 2);
        public static final TestValuedEnum THREE = new TestValuedEnum("THREE", 3);
        public static final TestValuedEnum NEGATIVE = new TestValuedEnum("NEGATIVE", -1);
        public static final TestValuedEnum ZERO = new TestValuedEnum("ZERO", 0);
        public static final TestValuedEnum LARGE = new TestValuedEnum("LARGE", Integer.MAX_VALUE);

        private TestValuedEnum(String name, int value) {
            super(name, value);
        }

        public static TestValuedEnum getEnum(String name) {
            return (TestValuedEnum) getEnum(TestValuedEnum.class, name);
        }

        public static TestValuedEnum getEnum(int value) {
            return (TestValuedEnum) getEnum(TestValuedEnum.class, value);
        }

        public static List getEnumList() {
            return getEnumList(TestValuedEnum.class);
        }

        public static Iterator iterator() {
            return iterator(TestValuedEnum.class);
        }
    }

    // --- Test getValue() ---

    // Tests normal case: getValue returns correct integer value
    @Test
    public void testGetValue_normalEnum_returnsCorrectValue() {
        assertEquals(1, TestValuedEnum.ONE.getValue());
        assertEquals(2, TestValuedEnum.TWO.getValue());
        assertEquals(3, TestValuedEnum.THREE.getValue());
    }

    // Tests boundary: getValue returns zero
    @Test
    public void testGetValue_zeroValue_returnsZero() {
        assertEquals(0, TestValuedEnum.ZERO.getValue());
    }

    // Tests boundary: getValue returns negative value
    @Test
    public void testGetValue_negativeValue_returnsNegative() {
        assertEquals(-1, TestValuedEnum.NEGATIVE.getValue());
    }

    // Tests boundary: getValue returns Integer.MAX_VALUE
    @Test
    public void testGetValue_maxIntegerValue_returnsMax() {
        assertEquals(Integer.MAX_VALUE, TestValuedEnum.LARGE.getValue());
    }

    // --- Test getEnum(Class, int) ---

    // Tests normal case: getEnum by value returns correct enum
    @Test
    public void testGetEnum_byValue_validValue_returnsEnum() {
        assertSame(TestValuedEnum.ONE, TestValuedEnum.getEnum(1));
        assertSame(TestValuedEnum.TWO, TestValuedEnum.getEnum(2));
        assertSame(TestValuedEnum.THREE, TestValuedEnum.getEnum(3));
    }

    // Tests edge: getEnum by value with value 0
    @Test
    public void testGetEnum_byValue_zeroValue_returnsEnum() {
        assertSame(TestValuedEnum.ZERO, TestValuedEnum.getEnum(0));
    }

    // Tests edge: getEnum by value with negative value
    @Test
    public void testGetEnum_byValue_negativeValue_returnsEnum() {
        assertSame(TestValuedEnum.NEGATIVE, TestValuedEnum.getEnum(-1));
    }

    // Tests edge: getEnum by value with Integer.MAX_VALUE
    @Test
    public void testGetEnum_byValue_maxIntegerValue_returnsEnum() {
        assertSame(TestValuedEnum.LARGE, TestValuedEnum.getEnum(Integer.MAX_VALUE));
    }

    // Tests invalid: getEnum by value with nonexistent value returns null (branch: loop exhausts)
    @Test
    public void testGetEnum_byValue_nonexistentValue_returnsNull() {
        assertNull(TestValuedEnum.getEnum(999));
    }

    // Tests edge: getEnum by value with null class throws IllegalArgumentException (branch: if condition)
    @Test(expected = IllegalArgumentException.class)
    public void testGetEnum_byValue_nullClass_throwsIllegalArgumentException() {
        ValuedEnum.getEnum(null, 1);
    }

    // Tests edge: getEnum by value with null class and zero value (branch: if condition, border)
    @Test(expected = IllegalArgumentException.class)
    public void testGetEnum_byValue_nullClassAndZero_throwsIllegalArgumentException() {
        ValuedEnum.getEnum(null, 0);
    }

    // Tests invalid: getEnum by value with value not matching any enum returns null (coverage of loop)
    @Test
    public void testGetEnum_byValue_nonMatchingNegative_returnsNull() {
        assertNull(TestValuedEnum.getEnum(-999));
    }

    // --- Test compareTo() ---

    // Tests normal case: compareTo returns positive when this value is greater
    @Test
    public void testCompareTo_greaterValue_returnsPositive() {
        assertTrue(TestValuedEnum.TWO.compareTo(TestValuedEnum.ONE) > 0);
    }

    // Tests normal case: compareTo returns negative when this value is smaller
    @Test
    public void testCompareTo_smallerValue_returnsNegative() {
        assertTrue(TestValuedEnum.ONE.compareTo(TestValuedEnum.TWO) < 0);
    }

    // Tests boundary: compareTo returns zero when values equal
    @Test
    public void testCompareTo_equalValue_returnsZero() {
        assertEquals(0, TestValuedEnum.ONE.compareTo(TestValuedEnum.ONE));
    }

    // Tests edge: compareTo with negative and positive values
    @Test
    public void testCompareTo_negativeVsPositive_returnsNegative() {
        assertTrue(TestValuedEnum.NEGATIVE.compareTo(TestValuedEnum.ONE) < 0);
    }

    // Tests edge: compareTo with zero
    @Test
    public void testCompareTo_zeroVsPositive_returnsNegative() {
        assertTrue(TestValuedEnum.ZERO.compareTo(TestValuedEnum.ONE) < 0);
    }

    // Tests edge: compareTo with max integer
    @Test
    public void testCompareTo_largeVsSmall_returnsPositive() {
        assertTrue(TestValuedEnum.LARGE.compareTo(TestValuedEnum.ONE) > 0);
    }

    // --- Test toString() ---

    // Tests normal case: toString returns formatted string
    @Test
    public void testToString_validEnum_returnsFormattedString() {
        String result = TestValuedEnum.ONE.toString();
        assertNotNull(result);
        assertTrue(result.contains("ONE"));
        assertTrue(result.contains("=1"));
    }

    // Tests edge: toString for enum with negative value
    @Test
    public void testToString_negativeValue_returnsFormattedString() {
        String result = TestValuedEnum.NEGATIVE.toString();
        assertNotNull(result);
        assertTrue(result.contains("NEGATIVE"));
        assertTrue(result.contains("=-1"));
    }

    // Tests edge: toString for enum with zero value
    @Test
    public void testToString_zeroValue_returnsFormattedString() {
        String result = TestValuedEnum.ZERO.toString();
        assertNotNull(result);
        assertTrue(result.contains("ZERO"));
        assertTrue(result.contains("=0"));
    }

    // Tests edge: toString for enum with large value
    @Test
    public void testToString_maxIntegerValue_returnsFormattedString() {
        String result = TestValuedEnum.LARGE.toString();
        assertNotNull(result);
        assertTrue(result.contains("LARGE"));
        assertTrue(result.contains("=" + Integer.MAX_VALUE));
    }
}