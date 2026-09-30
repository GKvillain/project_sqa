package org.joda.time.field;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.junit.Test;
import static org.junit.Assert.*;

public class UnsupportedDurationFieldTest {

    // Tests getInstance caching and singleton behavior
    @Test
    public void testGetInstance_sameType_returnsCachedInstance() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(DurationFieldType.years());
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(DurationFieldType.years());
        assertNotNull(field1);
        assertSame(field1, field2);
    }

    // Tests basic accessors: getType, getName, isSupported, isPrecise, getUnitMillis
    @Test
    public void testBasicProperties_validType_returnsExpectedValues() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.months());
        assertEquals(DurationFieldType.months(), field.getType());
        assertEquals("months", field.getName());
        assertFalse(field.isSupported());
        assertTrue(field.isPrecise());
        assertEquals(0L, field.getUnitMillis());
    }

    // Tests compareTo method returning zero
    @Test
    public void testCompareTo_anyField_returnsZero() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(DurationFieldType.days());
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        assertEquals(0, field1.compareTo(field2));
        assertEquals(0, field1.compareTo(field1));
    }

    // Tests equals and hashCode methods
    @Test
    public void testEqualsAndHashCode_sameAndDifferentObjects_behavesCorrectly() {
        UnsupportedDurationField fieldYears1 = UnsupportedDurationField.getInstance(DurationFieldType.years());
        UnsupportedDurationField fieldYears2 = UnsupportedDurationField.getInstance(DurationFieldType.years());
        UnsupportedDurationField fieldDays = UnsupportedDurationField.getInstance(DurationFieldType.days());

        assertTrue(fieldYears1.equals(fieldYears1));
        assertTrue(fieldYears1.equals(fieldYears2));
        assertFalse(fieldYears1.equals(fieldDays));
        assertFalse(fieldYears1.equals("NotADurationField"));
        assertFalse(fieldYears1.equals(null));

        assertEquals(fieldYears1.hashCode(), fieldYears2.hashCode());
    }

    // Tests toString format
    @Test
    public void testToString_validField_returnsFormattedString() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.seconds());
        assertEquals("UnsupportedDurationField[seconds]", field.toString());
    }

    // Tests serialization and readResolve mechanism
    @Test
    public void testSerialization_singletonInstance_deserializesToSameInstance() throws Exception {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.minutes());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(field);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        UnsupportedDurationField deserialized = (UnsupportedDurationField) ois.readObject();
        ois.close();

        assertSame(field, deserialized);
    }

    // Tests exception path for getValue(long)
    @Test(expected = UnsupportedOperationException.class)
    public void testGetValue_longDuration_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        field.getValue(1000L);
    }

    // Tests exception path for getValueAsLong(long)
    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueAsLong_longDuration_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        field.getValueAsLong(1000L);
    }

    // Tests exception path for getValue(long, long)
    @Test(expected = UnsupportedOperationException.class)
    public void testGetValue_longDurationAndInstant_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        field.getValue(1000L, 500L);
    }

    // Tests exception path for getValueAsLong(long, long)
    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueAsLong_longDurationAndInstant_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        field.getValueAsLong(1000L, 500L);
    }

    // Tests exception path for getMillis(int)
    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillis_intValue_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        field.getMillis(10);
    }

    // Tests exception path for getMillis(long)
    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillis_longValue_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        field.getMillis(10L);
    }

    // Tests exception path for getMillis(int, long)
    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillis_intValueAndInstant_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        field.getMillis(10, 500L);
    }

    // Tests exception path for getMillis(long, long)
    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillis_longValueAndInstant_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        field.getMillis(10L, 500L);
    }

    // Tests exception path for add(long, int)
    @Test(expected = UnsupportedOperationException.class)
    public void testAdd_instantAndIntValue_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        field.add(1000L, 5);
    }

    // Tests exception path for add(long, long)
    @Test(expected = UnsupportedOperationException.class)
    public void testAdd_instantAndLongValue_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        field.add(1000L, 5L);
    }

    // Tests exception path for getDifference(long, long)
    @Test(expected = UnsupportedOperationException.class)
    public void testGetDifference_twoInstants_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        field.getDifference(2000L, 1000L);
    }

    // Tests exception path for getDifferenceAsLong(long, long)
    @Test(expected = UnsupportedOperationException.class)
    public void testGetDifferenceAsLong_twoInstants_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        field.getDifferenceAsLong(2000L, 1000L);
    }

    // Tests exception path for subtract(long, int)
    @Test(expected = UnsupportedOperationException.class)
    public void testSubtract_instantAndIntValue_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        field.subtract(1000L, 5);
    }

    // Tests exception path for subtract(long, long)
    @Test(expected = UnsupportedOperationException.class)
    public void testSubtract_instantAndLongValue_throwsException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        field.subtract(1000L, 5L);
    }

    // Tests the exception message produced by unsupported methods
    @Test
    public void testUnsupportedExceptionMessage() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.halfdays());
        try {
            field.getValue(100L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            assertEquals("halfdays field is unsupported", ex.getMessage());
        }
    }
}