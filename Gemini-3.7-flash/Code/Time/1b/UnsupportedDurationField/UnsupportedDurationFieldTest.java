package org.joda.time.field;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class UnsupportedDurationFieldTest {

    private DurationFieldType hoursType;
    private DurationFieldType daysType;
    private UnsupportedDurationField hoursField;
    private UnsupportedDurationField daysField;

    @Before
    public void setUp() {
        hoursType = DurationFieldType.hours();
        daysType = DurationFieldType.days();
        hoursField = UnsupportedDurationField.getInstance(hoursType);
        daysField = UnsupportedDurationField.getInstance(daysType);
    }

    // Tests caching and singleton behavior of getInstance
    @Test
    public void testGetInstance_sameType_returnsCachedInstance() {
        UnsupportedDurationField instance1 = UnsupportedDurationField.getInstance(hoursType);
        UnsupportedDurationField instance2 = UnsupportedDurationField.getInstance(hoursType);
        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    // Tests simple accessors getType and getName
    @Test
    public void testGetTypeAndName_validField_returnsCorrectValues() {
        assertEquals(hoursType, hoursField.getType());
        assertEquals("hours", hoursField.getName());
    }

    // Tests isSupported returns false always
    @Test
    public void testIsSupported_always_returnsFalse() {
        assertFalse(hoursField.isSupported());
    }

    // Tests isPrecise returns true always
    @Test
    public void testIsPrecise_always_returnsTrue() {
        assertTrue(hoursField.isPrecise());
    }

    // Tests getUnitMillis returns zero always
    @Test
    public void testGetUnitMillis_always_returnsZero() {
        assertEquals(0L, hoursField.getUnitMillis());
    }

    // Tests getValue throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testGetValue_longDuration_throwsException() {
        hoursField.getValue(1000L);
    }

    // Tests getValueAsLong throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueAsLong_longDuration_throwsException() {
        hoursField.getValueAsLong(1000L);
    }

    // Tests getValue with instant throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testGetValue_longDurationAndInstant_throwsException() {
        hoursField.getValue(1000L, 500L);
    }

    // Tests getValueAsLong with instant throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueAsLong_longDurationAndInstant_throwsException() {
        hoursField.getValueAsLong(1000L, 500L);
    }

    // Tests getMillis with int throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillis_intValue_throwsException() {
        hoursField.getMillis(5);
    }

    // Tests getMillis with long throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillis_longValue_throwsException() {
        hoursField.getMillis(5L);
    }

    // Tests getMillis with int and instant throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillis_intValueAndInstant_throwsException() {
        hoursField.getMillis(5, 1000L);
    }

    // Tests getMillis with long and instant throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillis_longValueAndInstant_throwsException() {
        hoursField.getMillis(5L, 1000L);
    }

    // Tests add with int value throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testAdd_instantAndIntValue_throwsException() {
        hoursField.add(1000L, 5);
    }

    // Tests add with long value throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testAdd_instantAndLongValue_throwsException() {
        hoursField.add(1000L, 5L);
    }

    // Tests getDifference throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testGetDifference_twoInstants_throwsException() {
        hoursField.getDifference(2000L, 1000L);
    }

    // Tests getDifferenceAsLong throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testGetDifferenceAsLong_twoInstants_throwsException() {
        hoursField.getDifferenceAsLong(2000L, 1000L);
    }

    // Tests compareTo with supported duration field
    @Test
    public void testCompareTo_supportedDurationField_returnsOne() {
        DurationField supportedField = MillisDurationField.INSTANCE;
        assertEquals(1, hoursField.compareTo(supportedField));
    }

    // Tests compareTo with unsupported duration field
    @Test
    public void testCompareTo_unsupportedDurationField_returnsZero() {
        assertEquals(0, hoursField.compareTo(daysField));
    }

    // Tests equals for identical, equivalent, different, and null objects
    @Test
    public void testEquals_variousObjects_returnsExpectedResult() {
        assertTrue(hoursField.equals(hoursField));
        assertTrue(hoursField.equals(UnsupportedDurationField.getInstance(hoursType)));
        assertFalse(hoursField.equals(daysField));
        assertFalse(hoursField.equals("hours"));
        assertFalse(hoursField.equals(null));
    }

    // Tests hashCode consistency with equals
    @Test
    public void testHashCode_equalObjects_haveSameHashCode() {
        UnsupportedDurationField anotherHoursField = UnsupportedDurationField.getInstance(hoursType);
        assertEquals(hoursField.hashCode(), anotherHoursField.hashCode());
    }

    // Tests toString formatting
    @Test
    public void testToString_validField_returnsFormattedString() {
        assertEquals("UnsupportedDurationField[hours]", hoursField.toString());
    }

    // Tests serialization and deserialization singleton resolution
    @Test
    public void testSerialization_readResolve_returnsSingletonInstance() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(hoursField);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertSame(hoursField, deserialized);
    }
}