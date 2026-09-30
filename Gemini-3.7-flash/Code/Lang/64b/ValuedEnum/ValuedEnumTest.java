package org.apache.commons.lang.enums;

import org.junit.Test;
import static org.junit.Assert.*;

public class ValuedEnumTest {

    private static final class DummyValuedEnum extends ValuedEnum {
        private static final long serialVersionUID = 1L;

        public static final DummyValuedEnum ITEM_1 = new DummyValuedEnum("Item 1", 1);
        public static final DummyValuedEnum ITEM_2 = new DummyValuedEnum("Item 2", 2);
        public static final DummyValuedEnum ITEM_3 = new DummyValuedEnum("Item 3", 3);

        private DummyValuedEnum(String name, int value) {
            super(name, value);
        }

        public static DummyValuedEnum getEnum(int value) {
            return (DummyValuedEnum) getEnum(DummyValuedEnum.class, value);
        }
    }

    private static final class AnotherValuedEnum extends ValuedEnum {
        private static final long serialVersionUID = 1L;

        public static final AnotherValuedEnum OTHER_1 = new AnotherValuedEnum("Other 1", 1);

        private AnotherValuedEnum(String name, int value) {
            super(name, value);
        }
    }

    // Tests getValue returns the correct int value
    @Test
    public void testGetValue_validEnum_returnsCorrectValue() {
        assertEquals(1, DummyValuedEnum.ITEM_1.getValue());
        assertEquals(2, DummyValuedEnum.ITEM_2.getValue());
        assertEquals(3, DummyValuedEnum.ITEM_3.getValue());
    }

    // Tests getEnum with matching value returns the expected enum instance
    @Test
    public void testGetEnum_existingValue_returnsEnum() {
        assertSame(DummyValuedEnum.ITEM_1, DummyValuedEnum.getEnum(1));
        assertSame(DummyValuedEnum.ITEM_2, DummyValuedEnum.getEnum(2));
        assertSame(DummyValuedEnum.ITEM_3, ValuedEnum.getEnum(DummyValuedEnum.class, 3));
    }

    // Tests getEnum with non-existing value returns null
    @Test
    public void testGetEnum_nonExistingValue_returnsNull() {
        assertNull(DummyValuedEnum.getEnum(999));
        assertNull(ValuedEnum.getEnum(DummyValuedEnum.class, -1));
    }

    // Tests getEnum with null class throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetEnum_nullClass_throwsIllegalArgumentException() {
        ValuedEnum.getEnum(null, 1);
    }

    // Tests compareTo with equal enum returns zero
    @Test
    public void testCompareTo_equalEnum_returnsZero() {
        assertEquals(0, DummyValuedEnum.ITEM_1.compareTo(DummyValuedEnum.ITEM_1));
    }

    // Tests compareTo when this value is less than other value
    @Test
    public void testCompareTo_smallerValue_returnsNegative() {
        assertTrue(DummyValuedEnum.ITEM_1.compareTo(DummyValuedEnum.ITEM_2) < 0);
        assertTrue(DummyValuedEnum.ITEM_1.compareTo(DummyValuedEnum.ITEM_3) < 0);
    }

    // Tests compareTo when this value is greater than other value
    @Test
    public void testCompareTo_greaterValue_returnsPositive() {
        assertTrue(DummyValuedEnum.ITEM_2.compareTo(DummyValuedEnum.ITEM_1) > 0);
        assertTrue(DummyValuedEnum.ITEM_3.compareTo(DummyValuedEnum.ITEM_1) > 0);
    }

    // Tests compareTo with null object throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testCompareTo_nullObject_throwsNullPointerException() {
        DummyValuedEnum.ITEM_1.compareTo(null);
    }

    // Tests compareTo with incompatible object throws ClassCastException
    @Test(expected = ClassCastException.class)
    public void testCompareTo_incompatibleObject_throwsClassCastException() {
        DummyValuedEnum.ITEM_1.compareTo("Not a ValuedEnum");
    }

    // Tests toString returns properly formatted string
    @Test
    public void testToString_validEnum_returnsFormattedString() {
        assertEquals("ValuedEnumTest.DummyValuedEnum[Item 1=1]", DummyValuedEnum.ITEM_1.toString());
        assertEquals("ValuedEnumTest.DummyValuedEnum[Item 2=2]", DummyValuedEnum.ITEM_2.toString());
    }

    // Tests toString cache branch by calling toString multiple times
    @Test
    public void testToString_cachedResult_returnsSameString() {
        String firstCall = DummyValuedEnum.ITEM_3.toString();
        String secondCall = DummyValuedEnum.ITEM_3.toString();
        assertSame(firstCall, secondCall);
    }

    // Tests compareTo between different ValuedEnum subclasses
    @Test
    public void testCompareTo_differentValuedEnumClass_comparesValues() {
        assertEquals(0, DummyValuedEnum.ITEM_1.compareTo(AnotherValuedEnum.OTHER_1));
        assertTrue(DummyValuedEnum.ITEM_2.compareTo(AnotherValuedEnum.OTHER_1) > 0);
    }
}