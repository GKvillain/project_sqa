package org.joda.time.field;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.joda.time.Chronology;
import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.junit.Test;

import static org.junit.Assert.*;

public class UnsupportedDurationFieldTest {

    // Tests getInstance caching and basic properties
    @Test
    public void testGetInstance_sameType_returnsCachedInstance() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        assertSame(field1, field2);
        assertEquals(DurationFieldType.hours(), field1.getType());
        assertEquals("hours", field1.getName());
    }

    // Tests isSupported returns false always
    @Test
    public void testIsSupported_always_returnsFalse() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.days());
        assertFalse(field.isSupported());
    }

    // Tests isPrecise returns true always
    @Test
    public void testIsPrecise_always_returnsTrue() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.days());
        assertTrue(field.isPrecise());
    }

    // Tests getUnitMillis returns zero
    @Test
    public void testGetUnitMillis_always_returnsZero() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.minutes());
        assertEquals(0L, field.getUnitMillis());
    }

    // Tests getValue throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testGetValue_longDuration_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.seconds());
        field.getValue(1000L);
    }

    // Tests getValueAsLong throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueAsLong_longDuration_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.seconds());
        field.getValueAsLong(1000L);
    }

    // Tests getValue with instant throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testGetValue_durationAndInstant_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.seconds());
        field.getValue(1000L, 500L);
    }

    // Tests getValueAsLong with instant throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueAsLong_durationAndInstant_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.seconds());
        field.getValueAsLong(1000L, 500L);
    }

    // Tests getMillis with int throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillis_intValue_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        field.getMillis(5);
    }

    // Tests getMillis with long throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillis_longValue_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        field.getMillis(5L);
    }

    // Tests getMillis with int and instant throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillis_intValueAndInstant_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        field.getMillis(5, 100L);
    }

    // Tests getMillis with long and instant throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillis_longValueAndInstant_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        field.getMillis(5L, 100L);
    }

    // Tests add with int value throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testAdd_instantAndIntValue_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.days());
        field.add(1000L, 1);
    }

    // Tests add with long value throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testAdd_instantAndLongValue_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.days());
        field.add(1000L, 1L);
    }

    // Tests getDifference throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testGetDifference_twoInstants_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.months());
        field.getDifference(2000L, 1000L);
    }

    // Tests getDifferenceAsLong throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testGetDifferenceAsLong_twoInstants_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.months());
        field.getDifferenceAsLong(2000L, 1000L);
    }

    // Tests compareTo against supported and unsupported duration fields
    @Test
    public void testCompareTo_differentFields_returnsExpectedValues() {
        UnsupportedDurationField unsupportedField1 = UnsupportedDurationField.getInstance(DurationFieldType.years());
        UnsupportedDurationField unsupportedField2 = UnsupportedDurationField.getInstance(DurationFieldType.months());
        DurationField supportedField = MillisDurationField.INSTANCE;

        assertEquals(0, unsupportedField1.compareTo(unsupportedField2));
        assertEquals(1, unsupportedField1.compareTo(supportedField));
    }

    // Tests equals method with various objects
    @Test
    public void testEquals_variousObjects_returnsExpectedBooleans() {
        UnsupportedDurationField fieldHours1 = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        UnsupportedDurationField fieldHours2 = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        UnsupportedDurationField fieldDays = UnsupportedDurationField.getInstance(DurationFieldType.days());

        assertTrue(fieldHours1.equals(fieldHours1));
        assertTrue(fieldHours1.equals(fieldHours2));
        assertFalse(fieldHours1.equals(fieldDays));
        assertFalse(fieldHours1.equals(null));
        assertFalse(fieldHours1.equals("hours"));
    }

    // Tests equals method when name is null
    @Test
    public void testEquals_nullName_handlesNullProperly() {
        DurationFieldType nullNameType1 = new DurationFieldType(null) {
            private static final long serialVersionUID = 1L;
            public DurationField getField(Chronology chronology) {
                return null;
            }
        };
        DurationFieldType nullNameType2 = new DurationFieldType(null) {
            private static final long serialVersionUID = 1L;
            public DurationField getField(Chronology chronology) {
                return null;
            }
        };

        UnsupportedDurationField fieldNull1 = UnsupportedDurationField.getInstance(nullNameType1);
        UnsupportedDurationField fieldNull2 = UnsupportedDurationField.getInstance(nullNameType2);
        UnsupportedDurationField fieldHours = UnsupportedDurationField.getInstance(DurationFieldType.hours());

        assertTrue(fieldNull1.equals(fieldNull2));
        assertFalse(fieldHours.equals(fieldNull1));
        assertFalse(fieldNull1.equals(fieldHours));
    }

    // Tests hashCode consistency
    @Test
    public void testHashCode_sameType_returnsSameHashCode() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(DurationFieldType.minutes());
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(DurationFieldType.minutes());
        assertEquals(field1.hashCode(), field2.hashCode());
        assertEquals("minutes".hashCode(), field1.hashCode());
    }

    // Tests toString format
    @Test
    public void testToString_validInstance_returnsFormattedString() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.seconds());
        assertEquals("UnsupportedDurationField[seconds]", field.toString());
    }

    // Tests serialization and singleton deserialization
    @Test
    public void testSerialization_singletonPreserved_returnsSameInstance() throws Exception {
        UnsupportedDurationField original = UnsupportedDurationField.getInstance(DurationFieldType.days());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        UnsupportedDurationField result = (UnsupportedDurationField) ois.readObject();
        ois.close();

        assertSame(original, result);
    }
}