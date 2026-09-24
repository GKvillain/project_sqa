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

    // Tests getInstance caching and identity
    @Test
    public void testGetInstance_sameType_returnsCachedInstance() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(DurationFieldType.days());
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(DurationFieldType.days());
        assertSame(field1, field2);
    }

    // Tests simple accessors and state queries
    @Test
    public void testAccessors_validType_returnsExpectedValues() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        assertEquals(DurationFieldType.years(), field.getType());
        assertEquals("years", field.getName());
        assertFalse(field.isSupported());
        assertTrue(field.isPrecise());
        assertEquals(0L, field.getUnitMillis());
    }

    // Tests compareTo behavior
    @Test
    public void testCompareTo_anyField_returnsZero() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(DurationFieldType.minutes());
        assertEquals(0, field1.compareTo(field2));
    }

    // Tests equals method with self, same type, different type, and non-instance
    @Test
    public void testEquals_variousObjects_returnsCorrectBoolean() {
        UnsupportedDurationField hours1 = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        UnsupportedDurationField hours2 = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        UnsupportedDurationField minutes = UnsupportedDurationField.getInstance(DurationFieldType.minutes());

        assertTrue(hours1.equals(hours1));
        assertTrue(hours1.equals(hours2));
        assertFalse(hours1.equals(minutes));
        assertFalse(hours1.equals("hours"));
        assertFalse(hours1.equals(null));
    }

    // Tests hashCode consistency
    @Test
    public void testHashCode_sameType_returnsSameHashCode() {
        UnsupportedDurationField hours1 = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        UnsupportedDurationField hours2 = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        assertEquals(hours1.hashCode(), hours2.hashCode());
        assertEquals("hours".hashCode(), hours1.hashCode());
    }

    // Tests toString format
    @Test
    public void testToString_validType_returnsFormattedString() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.seconds());
        assertEquals("UnsupportedDurationField[seconds]", field.toString());
    }

    // Tests serialization and singleton resolution
    @Test
    public void testSerialization_validInstance_maintainsSingleton() throws Exception {
        UnsupportedDurationField original = UnsupportedDurationField.getInstance(DurationFieldType.millis());
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

    // Tests getValue(long) throws exception
    @Test(expected = UnsupportedOperationException.class)
    public void testGetValue_long_throwsException() {
        UnsupportedDurationField.getInstance(DurationFieldType.months()).getValue(100L);
    }

    // Tests getValueAsLong(long) throws exception
    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueAsLong_long_throwsException() {
        UnsupportedDurationField.getInstance(DurationFieldType.months()).getValueAsLong(100L);
    }

    // Tests getValue(long, long) throws exception
    @Test(expected = UnsupportedOperationException.class)
    public void testGetValue_longAndInstant_throwsException() {
        UnsupportedDurationField.getInstance(DurationFieldType.months()).getValue(100L, 200L);
    }

    // Tests getValueAsLong(long, long) throws exception
    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueAsLong_longAndInstant_throwsException() {
        UnsupportedDurationField.getInstance(DurationFieldType.months()).getValueAsLong(100L, 200L);
    }

    // Tests getMillis(int) throws exception
    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillis_int_throwsException() {
        UnsupportedDurationField.getInstance(DurationFieldType.months()).getMillis(10);
    }

    // Tests getMillis(long) throws exception
    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillis_long_throwsException() {
        UnsupportedDurationField.getInstance(DurationFieldType.months()).getMillis(10L);
    }

    // Tests getMillis(int, long) throws exception
    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillis_intAndInstant_throwsException() {
        UnsupportedDurationField.getInstance(DurationFieldType.months()).getMillis(10, 100L);
    }

    // Tests getMillis(long, long) throws exception
    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillis_longAndInstant_throwsException() {
        UnsupportedDurationField.getInstance(DurationFieldType.months()).getMillis(10L, 100L);
    }

    // Tests add(long, int) throws exception
    @Test(expected = UnsupportedOperationException.class)
    public void testAdd_longAndInt_throwsException() {
        UnsupportedDurationField.getInstance(DurationFieldType.months()).add(100L, 10);
    }

    // Tests add(long, long) throws exception
    @Test(expected = UnsupportedOperationException.class)
    public void testAdd_longAndLong_throwsException() {
        UnsupportedDurationField.getInstance(DurationFieldType.months()).add(100L, 10L);
    }

    // Tests getDifference(long, long) throws exception
    @Test(expected = UnsupportedOperationException.class)
    public void testGetDifference_twoInstants_throwsException() {
        UnsupportedDurationField.getInstance(DurationFieldType.months()).getDifference(200L, 100L);
    }

    // Tests getDifferenceAsLong(long, long) throws exception
    @Test(expected = UnsupportedOperationException.class)
    public void testGetDifferenceAsLong_twoInstants_throwsException() {
        UnsupportedDurationField.getInstance(DurationFieldType.months()).getDifferenceAsLong(200L, 100L);
    }
}