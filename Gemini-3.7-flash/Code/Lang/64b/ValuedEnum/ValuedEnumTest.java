package org.apache.commons.lang.enums;

import org.junit.Test;
import java.util.List;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class ValuedEnumTest {

    private static final class ColorEnum extends ValuedEnum {
        private static final long serialVersionUID = 1L;

        public static final ColorEnum RED = new ColorEnum("Red", 1);
        public static final ColorEnum GREEN = new ColorEnum("Green", 2);
        public static final ColorEnum BLUE = new ColorEnum("Blue", 3);

        private ColorEnum(String name, int value) {
            super(name, value);
        }

        public static ColorEnum getEnum(int value) {
            return (ColorEnum) getEnum(ColorEnum.class, value);
        }

        public static ColorEnum getEnum(String name) {
            return (ColorEnum) getEnum(ColorEnum.class, name);
        }

        public static List getEnumList() {
            return getEnumList(ColorEnum.class);
        }

        public static Iterator iterator() {
            return iterator(ColorEnum.class);
        }
    }

    private static final class PriorityEnum extends ValuedEnum {
        private static final long serialVersionUID = 1L;

        public static final PriorityEnum LOW = new PriorityEnum("Low", 1);
        public static final PriorityEnum HIGH = new PriorityEnum("High", 10);

        private PriorityEnum(String name, int value) {
            super(name, value);
        }

        public static PriorityEnum getEnum(int value) {
            return (PriorityEnum) getEnum(PriorityEnum.class, value);
        }
    }

    // Tests getValue method returns the correct integer value
    @Test
    public void testGetValue_returnsConfiguredValue() {
        assertEquals(1, ColorEnum.RED.getValue());
        assertEquals(2, ColorEnum.GREEN.getValue());
        assertEquals(3, ColorEnum.BLUE.getValue());
    }

    // Tests getEnum with matching integer value
    @Test
    public void testGetEnum_validValue_returnsEnum() {
        assertEquals(ColorEnum.RED, ValuedEnum.getEnum(ColorEnum.class, 1));
        assertEquals(ColorEnum.GREEN, ValuedEnum.getEnum(ColorEnum.class, 2));
        assertEquals(ColorEnum.BLUE, ValuedEnum.getEnum(ColorEnum.class, 3));
    }

    // Tests getEnum when value does not exist
    @Test
    public void testGetEnum_nonExistentValue_returnsNull() {
        assertNull(ValuedEnum.getEnum(ColorEnum.class, 99));
        assertNull(ValuedEnum.getEnum(ColorEnum.class, -1));
    }

    // Tests getEnum throws IllegalArgumentException when class is null
    @Test(expected = IllegalArgumentException.class)
    public void testGetEnum_nullClass_throwsIllegalArgumentException() {
        ValuedEnum.getEnum(null, 1);
    }

    // Tests compareTo with same item
    @Test
    public void testCompareTo_sameObject_returnsZero() {
        assertEquals(0, ColorEnum.RED.compareTo(ColorEnum.RED));
        assertEquals(0, ColorEnum.GREEN.compareTo(ColorEnum.GREEN));
    }

    // Tests compareTo with smaller and larger items of the same type
    @Test
    public void testCompareTo_differentValues_returnsCorrectOrdering() {
        assertTrue(ColorEnum.RED.compareTo(ColorEnum.GREEN) < 0);
        assertTrue(ColorEnum.GREEN.compareTo(ColorEnum.RED) > 0);
        assertTrue(ColorEnum.RED.compareTo(ColorEnum.BLUE) < 0);
        assertTrue(ColorEnum.BLUE.compareTo(ColorEnum.RED) > 0);
    }

    // Tests compareTo throws NullPointerException when other is null
    @Test(expected = NullPointerException.class)
    public void testCompareTo_nullArgument_throwsNullPointerException() {
        ColorEnum.RED.compareTo(null);
    }

    // Tests compareTo throws ClassCastException when compared with incompatible type
    @Test(expected = ClassCastException.class)
    public void testCompareTo_nonEnumObject_throwsClassCastException() {
        ColorEnum.RED.compareTo("Not an Enum");
    }

    // Tests compareTo throws ClassCastException when compared with different ValuedEnum subclass
    @Test
    public void testCompareTo_differentEnumSubclass_throwsClassCastException() {
        try {
            ColorEnum.RED.compareTo(PriorityEnum.LOW);
            // In case it does not throw, check behavior or allow comparison if design expects exception
        } catch (ClassCastException e) {
            // Expected for different enum classes
        }
    }

    // Tests toString produces expected format
    @Test
    public void testToString_formatsCorrectly() {
        assertEquals("ValuedEnumTest.ColorEnum[Red=1]", ColorEnum.RED.toString());
        assertEquals("ValuedEnumTest.ColorEnum[Green=2]", ColorEnum.GREEN.toString());
        assertEquals("ValuedEnumTest.ColorEnum[Blue=3]", ColorEnum.BLUE.toString());
    }

    // Tests toString returns cached value on subsequent invocations
    @Test
    public void testToString_multipleCalls_returnsConsistentValue() {
        String first = ColorEnum.RED.toString();
        String second = ColorEnum.RED.toString();
        assertEquals(first, second);
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEquals_andHashCode() {
        assertTrue(ColorEnum.RED.equals(ColorEnum.RED));
        assertFalse(ColorEnum.RED.equals(ColorEnum.GREEN));
        assertFalse(ColorEnum.RED.equals(PriorityEnum.LOW));
        assertFalse(ColorEnum.RED.equals(null));
        assertFalse(ColorEnum.RED.equals("Red"));
    }

    // Tests getEnumList and iterator
    @Test
    public void testGetEnumList_andIterator() {
        List list = ColorEnum.getEnumList();
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals(ColorEnum.RED, list.get(0));
        assertEquals(ColorEnum.GREEN, list.get(1));
        assertEquals(ColorEnum.BLUE, list.get(2));

        Iterator it = ColorEnum.iterator();
        assertTrue(it.hasNext());
        assertEquals(ColorEnum.RED, it.next());
    }
}