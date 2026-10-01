package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Locale;
import org.joda.time.chrono.CopticChronology;
import org.joda.time.chrono.ISOChronology;

public class PartialTest {

    // Tests empty constructor and basic getters
    @Test
    public void testConstructor_empty_hasZeroSize() {
        Partial p = new Partial();
        assertEquals(0, p.size());
        assertEquals(0, p.getFieldTypes().length);
        assertEquals(0, p.getValues().length);
        assertEquals(ISOChronology.getInstanceUTC(), p.getChronology());
        assertNull(p.getFormatter());
        assertEquals("[]", p.toStringList());
    }

    // Tests single field constructor
    @Test
    public void testConstructor_singleField_initializesCorrectly() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        assertEquals(1, p.size());
        assertEquals(DateTimeFieldType.hourOfDay(), p.getFieldType(0));
        assertEquals(10, p.getValue(0));
        assertEquals(10, p.get(DateTimeFieldType.hourOfDay()));
        assertTrue(p.isSupported(DateTimeFieldType.hourOfDay()));
        assertFalse(p.isSupported(DateTimeFieldType.minuteOfHour()));
    }

    // Tests single field constructor with null type throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullType_throwsException() {
        new Partial((DateTimeFieldType) null, 5);
    }

    // Tests multiple fields constructor in order
    @Test
    public void testConstructor_multipleFields_initializesCorrectly() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[] {2020, 5, 15};
        Partial p = new Partial(types, values);

        assertEquals(3, p.size());
        assertEquals(2020, p.getValue(0));
        assertEquals(5, p.getValue(1));
        assertEquals(15, p.getValue(2));
    }

    // Tests multiple fields constructor with null types array throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullTypesArray_throwsException() {
        new Partial(null, new int[] {1});
    }

    // Tests multiple fields constructor with mismatched lengths throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_mismatchedArrays_throwsException() {
        new Partial(new DateTimeFieldType[] {DateTimeFieldType.hourOfDay()}, new int[] {1, 2});
    }

    // Tests multiple fields constructor with invalid order throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_invalidFieldOrder_throwsException() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.dayOfMonth(),
            DateTimeFieldType.monthOfYear()
        };
        new Partial(types, new int[] {15, 5});
    }

    // Tests multiple fields constructor with duplicate field throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_duplicateField_throwsException() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.hourOfDay(),
            DateTimeFieldType.hourOfDay()
        };
        new Partial(types, new int[] {1, 2});
    }

    // Tests copy constructor from ReadablePartial
    @Test
    public void testConstructor_readablePartial_copiesFieldsAndValues() {
        Partial original = new Partial(DateTimeFieldType.minuteOfHour(), 30);
        Partial copy = new Partial(original);
        assertEquals(original.size(), copy.size());
        assertEquals(original.getValue(0), copy.getValue(0));
        assertEquals(original.getFieldType(0), copy.getFieldType(0));
    }

    // Tests with() method when adding new field in sorted order
    @Test
    public void testWith_newField_maintainsOrder() {
        Partial p = new Partial(DateTimeFieldType.minuteOfHour(), 20);
        Partial result = p.with(DateTimeFieldType.hourOfDay(), 10);

        assertEquals(2, result.size());
        assertEquals(DateTimeFieldType.hourOfDay(), result.getFieldType(0));
        assertEquals(10, result.getValue(0));
        assertEquals(DateTimeFieldType.minuteOfHour(), result.getFieldType(1));
        assertEquals(20, result.getValue(1));
    }

    // Tests with() method when updating existing field value
    @Test
    public void testWith_existingField_updatesValue() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        Partial updated = p.with(DateTimeFieldType.hourOfDay(), 15);

        assertEquals(1, updated.size());
        assertEquals(15, updated.getValue(0));
        
        Partial same = updated.with(DateTimeFieldType.hourOfDay(), 15);
        assertSame(updated, same);
    }

    // Tests with() method with null field type throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testWith_nullFieldType_throwsException() {
        Partial p = new Partial();
        p.with(null, 1);
    }

    // Tests without() method
    @Test
    public void testWithout_existingAndNonExistingField() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10)
                .with(DateTimeFieldType.minuteOfHour(), 30);

        Partial withoutMinute = p.without(DateTimeFieldType.minuteOfHour());
        assertEquals(1, withoutMinute.size());
        assertEquals(DateTimeFieldType.hourOfDay(), withoutMinute.getFieldType(0));

        Partial withoutNonExisting = withoutMinute.without(DateTimeFieldType.secondOfMinute());
        assertSame(withoutMinute, withoutNonExisting);
    }

    // Tests withField() and withFieldAdded()
    @Test
    public void testWithField_and_withFieldAdded() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        
        Partial modified = p.withField(DateTimeFieldType.hourOfDay(), 12);
        assertEquals(12, modified.get(DateTimeFieldType.hourOfDay()));

        Partial added = modified.withFieldAdded(DurationFieldType.hours(), 3);
        assertEquals(15, added.get(DateTimeFieldType.hourOfDay()));

        Partial zeroAdded = added.withFieldAdded(DurationFieldType.hours(), 0);
        assertSame(added, zeroAdded);
    }

    // Tests withFieldAddWrapped()
    @Test
    public void testWithFieldAddWrapped_wrapsValue() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 23);
        Partial wrapped = p.withFieldAddWrapped(DurationFieldType.hours(), 2);
        assertEquals(1, wrapped.get(DateTimeFieldType.hourOfDay()));

        Partial zeroWrapped = wrapped.withFieldAddWrapped(DurationFieldType.hours(), 0);
        assertSame(wrapped, zeroWrapped);
    }

    // Tests plus() and minus() with Period
    @Test
    public void testPlus_and_Minus_period() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        Period period = Period.hours(3);

        Partial plusResult = p.plus(period);
        assertEquals(13, plusResult.get(DateTimeFieldType.hourOfDay()));

        Partial minusResult = plusResult.minus(period);
        assertEquals(10, minusResult.get(DateTimeFieldType.hourOfDay()));

        assertSame(p, p.plus(null));
        assertSame(p, p.minus(null));
    }

    // Tests isMatch() against ReadableInstant and ReadablePartial
    @Test
    public void testIsMatch_instantAndPartial() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10)
                .with(DateTimeFieldType.minuteOfHour(), 30);

        DateTime matchInstant = new DateTime(2020, 1, 1, 10, 30, 0, 0, DateTimeZone.UTC);
        DateTime nonMatchInstant = new DateTime(2020, 1, 1, 11, 30, 0, 0, DateTimeZone.UTC);

        assertTrue(p.isMatch(matchInstant));
        assertFalse(p.isMatch(nonMatchInstant));

        Partial matchingPartial = new Partial(DateTimeFieldType.hourOfDay(), 10)
                .with(DateTimeFieldType.minuteOfHour(), 30);
        Partial nonMatchingPartial = new Partial(DateTimeFieldType.hourOfDay(), 11)
                .with(DateTimeFieldType.minuteOfHour(), 30);

        assertTrue(p.isMatch(matchingPartial));
        assertFalse(p.isMatch(nonMatchingPartial));
    }

    // Tests Property manipulation methods
    @Test
    public void testProperty_operations() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 6);
        Partial.Property prop = p.property(DateTimeFieldType.monthOfYear());

        assertEquals(6, prop.get());
        assertSame(p, prop.getPartial());
        assertEquals(DateTimeFieldType.monthOfYear().getField(p.getChronology()), prop.getField());

        Partial propSet = prop.setCopy(8);
        assertEquals(8, propSet.get(DateTimeFieldType.monthOfYear()));

        Partial propMax = prop.withMaximumValue();
        assertEquals(12, propMax.get(DateTimeFieldType.monthOfYear()));

        Partial propMin = prop.withMinimumValue();
        assertEquals(1, propMin.get(DateTimeFieldType.monthOfYear()));

        Partial propAdd = prop.addToCopy(2);
        assertEquals(8, propAdd.get(DateTimeFieldType.monthOfYear()));

        Partial propWrap = prop.addWrapFieldToCopy(8);
        assertEquals(2, propWrap.get(DateTimeFieldType.monthOfYear()));
    }

    // Tests toString formatting
    @Test
    public void testToString_formatting() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.monthOfYear(), 5)
                .with(DateTimeFieldType.dayOfMonth(), 15);

        assertEquals("2020-05-15", p.toString());
        assertEquals("15/05/2020", p.toString("dd/MM/yyyy"));
        assertEquals("15/05/2020", p.toString("dd/MM/yyyy", Locale.ENGLISH));
        assertEquals("2020-05-15", p.toString((String) null));
        assertEquals("[year=2020, monthOfYear=5, dayOfMonth=15]", p.toStringList());
    }

    // Tests withChronologyRetainFields()
    @Test
    public void testWithChronologyRetainFields_changesChronology() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        assertSame(p, p.withChronologyRetainFields(null));
        assertSame(p, p.withChronologyRetainFields(ISOChronology.getInstance()));

        Partial copticPartial = p.withChronologyRetainFields(org.joda.time.chrono.CopticChronology.getInstance());
        assertEquals(org.joda.time.chrono.CopticChronology.getInstanceUTC(), copticPartial.getChronology());
        assertEquals(10, copticPartial.get(DateTimeFieldType.hourOfDay()));
    }

    // Tests constructor with Chronology parameter
    @Test
    public void testConstructor_withChronology() {
        Partial pNullChrono = new Partial((Chronology) null);
        assertEquals(ISOChronology.getInstanceUTC(), pNullChrono.getChronology());

        Partial pCoptic = new Partial(CopticChronology.getInstanceUTC());
        assertEquals(CopticChronology.getInstanceUTC(), pCoptic.getChronology());

        Partial pSingleWithChrono = new Partial(DateTimeFieldType.hourOfDay(), 8, CopticChronology.getInstanceUTC());
        assertEquals(CopticChronology.getInstanceUTC(), pSingleWithChrono.getChronology());
        assertEquals(8, pSingleWithChrono.get(DateTimeFieldType.hourOfDay()));

        DateTimeFieldType[] types = new DateTimeFieldType[] {DateTimeFieldType.hourOfDay()};
        int[] values = new int[] {9};
        Partial pArrayWithChrono = new Partial(types, values, CopticChronology.getInstanceUTC());
        assertEquals(CopticChronology.getInstanceUTC(), pArrayWithChrono.getChronology());
        assertEquals(9, pArrayWithChrono.get(DateTimeFieldType.hourOfDay()));
    }

    // Tests constructor with null ReadablePartial throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullReadablePartial_throwsException() {
        new Partial((ReadablePartial) null);
    }

    // Tests constructor with null element in types array throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullElementInTypesArray_throwsException() {
        new Partial(new DateTimeFieldType[] {null}, new int[] {1});
    }

    // Tests constructor with null values array throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullValuesArray_throwsException() {
        new Partial(new DateTimeFieldType[] {DateTimeFieldType.hourOfDay()}, null);
    }

    // Tests withField with unsupported field type throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testWithField_unsupportedField_throwsException() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        p.withField(DateTimeFieldType.minuteOfHour(), 30);
    }

    // Tests withField with same value returns same instance
    @Test
    public void testWithField_sameValue_returnsSame() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        assertSame(p, p.withField(DateTimeFieldType.hourOfDay(), 10));
    }

    // Tests withFieldAdded with unsupported duration type throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testWithFieldAdded_unsupportedDuration_throwsException() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        p.withFieldAdded(DurationFieldType.minutes(), 5);
    }

    // Tests withFieldAdded with null duration type throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testWithFieldAdded_nullDuration_throwsException() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        p.withFieldAdded(null, 5);
    }

    // Tests withFieldAddWrapped with unsupported duration type throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testWithFieldAddWrapped_unsupportedDuration_throwsException() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        p.withFieldAddWrapped(DurationFieldType.minutes(), 5);
    }

    // Tests withFieldAddWrapped with null duration type throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testWithFieldAddWrapped_nullDuration_throwsException() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        p.withFieldAddWrapped(null, 5);
    }

    // Tests withPeriodAdded method
    @Test
    public void testWithPeriodAdded() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        Period period = Period.hours(2);

        Partial result = p.withPeriodAdded(period, 2);
        assertEquals(14, result.get(DateTimeFieldType.hourOfDay()));

        assertSame(p, p.withPeriodAdded(period, 0));
        assertSame(p, p.withPeriodAdded(null, 2));
    }

    // Tests without with null parameter returns same instance
    @Test
    public void testWithout_null_returnsSame() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        assertSame(p, p.without(null));
    }

    // Tests isMatch with null parameters
    @Test
    public void testIsMatch_nullInstant_usesCurrentTime() {
        Partial p = new Partial();
        assertTrue(p.isMatch((ReadableInstant) null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsMatch_nullPartial_throwsException() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        p.isMatch((ReadablePartial) null);
    }

    // Tests isMatch with partial having different fields
    @Test
    public void testIsMatch_differentFields() {
        Partial p1 = new Partial(DateTimeFieldType.hourOfDay(), 10);
        Partial p2 = new Partial(DateTimeFieldType.minuteOfHour(), 10);
        assertFalse(p1.isMatch(p2));
    }

    // Tests property method with unsupported or null field
    @Test(expected = IllegalArgumentException.class)
    public void testProperty_unsupportedField_throwsException() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        p.property(DateTimeFieldType.minuteOfHour());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProperty_nullField_throwsException() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        p.property(null);
    }

    // Tests Property text getters and string setters
    @Test
    public void testProperty_textOperations() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 6);
        Partial.Property prop = p.property(DateTimeFieldType.monthOfYear());

        assertEquals(DateTimeFieldType.monthOfYear(), prop.getFieldType());
        assertEquals("6", prop.getAsString());
        assertEquals("June", prop.getAsText(Locale.ENGLISH));
        assertEquals("Jun", prop.getAsShortText(Locale.ENGLISH));
        assertNotNull(prop.getAsText());
        assertNotNull(prop.getAsShortText());

        Partial pFromString = prop.setCopy("7");
        assertEquals(7, pFromString.get(DateTimeFieldType.monthOfYear()));

        Partial pFromText = prop.setCopy("December", Locale.ENGLISH);
        assertEquals(12, pFromText.get(DateTimeFieldType.monthOfYear()));
    }
}