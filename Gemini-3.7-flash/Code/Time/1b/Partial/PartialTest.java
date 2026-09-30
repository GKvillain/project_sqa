package org.joda.time;

import java.util.Locale;

import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.ISOChronology;
import org.junit.Test;
import static org.junit.Assert.*;

public class PartialTest {

    // Tests default constructor initializes empty partial with ISO chronology
    @Test
    public void testConstructor_default_createsEmptyPartial() {
        Partial partial = new Partial();
        assertEquals(0, partial.size());
        assertEquals(0, partial.getFieldTypes().length);
        assertEquals(0, partial.getValues().length);
        assertNotNull(partial.getChronology());
    }

    // Tests constructor with null chronology defaults to ISO chronology
    @Test
    public void testConstructor_nullChronology_usesISOChronology() {
        Partial partial = new Partial((Chronology) null);
        assertEquals(0, partial.size());
        assertEquals(DateTimeZone.UTC, partial.getChronology().getZone());
    }

    // Tests single field constructor with valid inputs
    @Test
    public void testConstructor_singleField_createsPartial() {
        Partial partial = new Partial(DateTimeFieldType.hourOfDay(), 10);
        assertEquals(1, partial.size());
        assertEquals(DateTimeFieldType.hourOfDay(), partial.getFieldType(0));
        assertEquals(10, partial.getValue(0));
    }

    // Tests single field constructor with null type throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullSingleFieldType_throwsException() {
        new Partial((DateTimeFieldType) null, 10);
    }

    // Tests array constructor with valid ordered fields
    @Test
    public void testConstructor_validArrayTypesAndValues_createsPartial() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[] { 2023, 5, 20 };
        Partial partial = new Partial(types, values);

        assertEquals(3, partial.size());
        assertEquals(2023, partial.getValue(0));
        assertEquals(5, partial.getValue(1));
        assertEquals(20, partial.getValue(2));
    }

    // Tests array constructor with null types throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullTypesArray_throwsException() {
        new Partial((DateTimeFieldType[]) null, new int[] { 1 });
    }

    // Tests array constructor with null values throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullValuesArray_throwsException() {
        new Partial(new DateTimeFieldType[] { DateTimeFieldType.dayOfMonth() }, null);
    }

    // Tests array constructor with mismatched array lengths throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_mismatchedArrayLengths_throwsException() {
        new Partial(new DateTimeFieldType[] { DateTimeFieldType.dayOfMonth() }, new int[] { 1, 2 });
    }

    // Tests array constructor containing null element throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullElementInTypes_throwsException() {
        DateTimeFieldType[] types = new DateTimeFieldType[] { DateTimeFieldType.year(), null };
        new Partial(types, new int[] { 2023, 5 });
    }

    // Tests array constructor with incorrect order throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_invalidFieldOrder_throwsException() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.minuteOfHour(),
            DateTimeFieldType.hourOfDay()
        };
        new Partial(types, new int[] { 30, 10 });
    }

    // Tests array constructor with duplicate field types throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_duplicateFieldTypes_throwsException() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.dayOfMonth(),
            DateTimeFieldType.dayOfMonth()
        };
        new Partial(types, new int[] { 1, 2 });
    }

    // Tests constructor copying from ReadablePartial
    @Test
    public void testConstructor_copyReadablePartial_copiesCorrectly() {
        Partial original = new Partial(DateTimeFieldType.dayOfMonth(), 15);
        Partial copy = new Partial(original);

        assertEquals(original.size(), copy.size());
        assertEquals(original.getFieldType(0), copy.getFieldType(0));
        assertEquals(original.getValue(0), copy.getValue(0));
    }

    // Tests constructor with null ReadablePartial throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullReadablePartial_throwsException() {
        new Partial((ReadablePartial) null);
    }

    // Tests with() inserting field maintaining order, and handling range duration types
    @Test
    public void testWith_newFieldsInOrder_insertsCorrectly() {
        Partial partial = new Partial();
        partial = partial.with(DateTimeFieldType.hourOfDay(), 10);
        partial = partial.with(DateTimeFieldType.minuteOfHour(), 30);
        partial = partial.with(DateTimeFieldType.year(), 2020);

        assertEquals(3, partial.size());
        assertEquals(DateTimeFieldType.year(), partial.getFieldType(0));
        assertEquals(DateTimeFieldType.hourOfDay(), partial.getFieldType(1));
        assertEquals(DateTimeFieldType.minuteOfHour(), partial.getFieldType(2));
        assertEquals(2020, partial.getValue(0));
        assertEquals(10, partial.getValue(1));
        assertEquals(30, partial.getValue(2));
    }

    // Tests with() when adding a field with null range duration alongside non-null range duration field
    @Test
    public void testWith_fieldWithNullRangeDuration_insertsCorrectly() {
        Partial partial = new Partial(DateTimeFieldType.year(), 2020);
        partial = partial.with(DateTimeFieldType.era(), 1);

        assertEquals(2, partial.size());
        assertEquals(DateTimeFieldType.era(), partial.getFieldType(0));
        assertEquals(DateTimeFieldType.year(), partial.getFieldType(1));
    }

    // Tests with() when adding existing field with same value returns this
    @Test
    public void testWith_sameValue_returnsSameInstance() {
        Partial partial = new Partial(DateTimeFieldType.hourOfDay(), 10);
        Partial result = partial.with(DateTimeFieldType.hourOfDay(), 10);
        assertSame(partial, result);
    }

    // Tests with() when fieldType is null throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testWith_nullFieldType_throwsException() {
        Partial partial = new Partial(DateTimeFieldType.hourOfDay(), 10);
        partial.with(null, 5);
    }

    // Tests without() removing supported field and unsupported field
    @Test
    public void testWithout_existingAndNonExistingField_removesOrReturnsSame() {
        Partial partial = new Partial(DateTimeFieldType.hourOfDay(), 10)
                .with(DateTimeFieldType.minuteOfHour(), 20);

        Partial removed = partial.without(DateTimeFieldType.hourOfDay());
        assertEquals(1, removed.size());
        assertEquals(DateTimeFieldType.minuteOfHour(), removed.getFieldType(0));
        assertEquals(20, removed.getValue(0));

        Partial notPresent = removed.without(DateTimeFieldType.year());
        assertSame(removed, notPresent);
    }

    // Tests withField() updating existing field value
    @Test
    public void testWithField_supportedField_updatesValue() {
        Partial partial = new Partial(DateTimeFieldType.hourOfDay(), 10);
        Partial updated = partial.withField(DateTimeFieldType.hourOfDay(), 15);

        assertEquals(15, updated.getValue(0));
        assertSame(partial, partial.withField(DateTimeFieldType.hourOfDay(), 10));
    }

    // Tests withField() with unsupported field throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testWithField_unsupportedField_throwsException() {
        Partial partial = new Partial(DateTimeFieldType.hourOfDay(), 10);
        partial.withField(DateTimeFieldType.minuteOfHour(), 15);
    }

    // Tests withFieldAdded() and withFieldAddWrapped()
    @Test
    public void testWithFieldAdded_validAmount_addsCorrectly() {
        Partial partial = new Partial(DateTimeFieldType.minuteOfHour(), 45);

        Partial added = partial.withFieldAdded(DurationFieldType.minutes(), 10);
        assertEquals(55, added.getValue(0));

        assertSame(partial, partial.withFieldAdded(DurationFieldType.minutes(), 0));

        Partial wrapped = partial.withFieldAddWrapped(DurationFieldType.minutes(), 20);
        assertEquals(5, wrapped.getValue(0));

        assertSame(partial, partial.withFieldAddWrapped(DurationFieldType.minutes(), 0));
    }

    // Tests plus() and minus() with ReadablePeriod
    @Test
    public void testPlusMinus_period_addsAndSubtracts() {
        Partial partial = new Partial(DateTimeFieldType.hourOfDay(), 10)
                .with(DateTimeFieldType.minuteOfHour(), 30);
        Period period = Period.hours(2).withMinutes(15);

        Partial plusResult = partial.plus(period);
        assertEquals(12, plusResult.getValue(0));
        assertEquals(45, plusResult.getValue(1));

        Partial minusResult = plusResult.minus(period);
        assertEquals(10, minusResult.getValue(0));
        assertEquals(30, minusResult.getValue(1));

        assertSame(partial, partial.plus(null));
        assertSame(partial, partial.minus(null));
    }

    // Tests isMatch() against ReadableInstant and ReadablePartial
    @Test
    public void testIsMatch_instantAndPartial_matchesCorrectly() {
        DateTime dt = new DateTime(2023, 5, 20, 10, 30, 0, 0, DateTimeZone.UTC);
        Partial partial = new Partial(DateTimeFieldType.year(), 2023)
                .with(DateTimeFieldType.monthOfYear(), 5);

        assertTrue(partial.isMatch(dt));

        DateTime nonMatchingDt = new DateTime(2022, 5, 20, 10, 30, 0, 0, DateTimeZone.UTC);
        assertFalse(partial.isMatch(nonMatchingDt));

        Partial matchingPartial = new Partial(DateTimeFieldType.year(), 2023)
                .with(DateTimeFieldType.monthOfYear(), 5)
                .with(DateTimeFieldType.dayOfMonth(), 20);
        assertTrue(partial.isMatch(matchingPartial));

        Partial nonMatchingPartial = new Partial(DateTimeFieldType.year(), 2022)
                .with(DateTimeFieldType.monthOfYear(), 5);
        assertFalse(partial.isMatch(nonMatchingPartial));
    }

    // Tests isMatch() with null ReadablePartial throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testIsMatch_nullPartial_throwsException() {
        Partial partial = new Partial(DateTimeFieldType.dayOfMonth(), 1);
        partial.isMatch((ReadablePartial) null);
    }

    // Tests Property manipulation methods
    @Test
    public void testProperty_methods_manipulateAndReturnCorrectly() {
        Partial partial = new Partial(DateTimeFieldType.monthOfYear(), 6)
                .with(DateTimeFieldType.dayOfMonth(), 15);
        Partial.Property prop = partial.property(DateTimeFieldType.monthOfYear());

        assertEquals(6, prop.get());
        assertEquals(DateTimeFieldType.monthOfYear(), prop.getFieldType());
        assertSame(partial, prop.getPartial());

        Partial updated = prop.addToCopy(2);
        assertEquals(8, updated.getValue(0));

        Partial wrapped = prop.addWrapFieldToCopy(8);
        assertEquals(2, wrapped.getValue(0));

        Partial setVal = prop.setCopy(12);
        assertEquals(12, setVal.getValue(0));

        Partial setMax = prop.withMaximumValue();
        assertEquals(12, setMax.getValue(0));

        Partial setMin = prop.withMinimumValue();
        assertEquals(1, setMin.getValue(0));

        Partial setText = prop.setCopy("December", Locale.ENGLISH);
        assertEquals(12, setText.getValue(0));

        Partial setTextDefaultLocale = prop.setCopy("11");
        assertEquals(11, setTextDefaultLocale.getValue(0));
    }

    // Tests toString(), toStringList() and formatted output
    @Test
    public void testToString_variousFormats_returnsExpectedStrings() {
        Partial emptyPartial = new Partial();
        assertEquals("[]", emptyPartial.toString());

        Partial partial = new Partial(DateTimeFieldType.year(), 2023)
                .with(DateTimeFieldType.monthOfYear(), 5);

        assertEquals("2023-05", partial.toString());
        assertEquals("[year=2023, monthOfYear=5]", partial.toStringList());
        assertEquals("05/2023", partial.toString("MM/yyyy"));
        assertEquals("May/2023", partial.toString("MMM/yyyy", Locale.ENGLISH));
        assertEquals("2023-05", partial.toString(null));
        assertEquals("2023-05", partial.toString(null, Locale.ENGLISH));
    }

    // Tests withChronologyRetainFields()
    @Test
    public void testWithChronologyRetainFields_changesChronology() {
        Partial partial = new Partial(DateTimeFieldType.year(), 2023);
        assertSame(partial, partial.withChronologyRetainFields(null));
        assertSame(partial, partial.withChronologyRetainFields(partial.getChronology()));
    }

    // Tests constructor with single field and chronology
    @Test
    public void testConstructor_singleFieldAndChronology() {
        Chronology chrono = BuddhistChronology.getInstanceUTC();
        Partial partial = new Partial(DateTimeFieldType.year(), 2566, chrono);
        assertEquals(1, partial.size());
        assertEquals(DateTimeFieldType.year(), partial.getFieldType(0));
        assertEquals(2566, partial.getValue(0));
        assertEquals(chrono, partial.getChronology());
    }

    // Tests constructor with empty arrays and explicit chronology
    @Test
    public void testConstructor_emptyArraysAndChronology() {
        Chronology chrono = BuddhistChronology.getInstanceUTC();
        Partial partial = new Partial(new DateTimeFieldType[0], new int[0], chrono);
        assertEquals(0, partial.size());
        assertEquals(chrono, partial.getChronology());
    }

    // Tests withChronologyRetainFields when changing to a different chronology
    @Test
    public void testWithChronologyRetainFields_differentChronology() {
        Partial partial = new Partial(DateTimeFieldType.year(), 2023, ISOChronology.getInstanceUTC());
        Chronology buddhist = BuddhistChronology.getInstanceUTC();
        Partial result = partial.withChronologyRetainFields(buddhist);
        assertNotSame(partial, result);
        assertEquals(buddhist, result.getChronology());
        assertEquals(2023, result.getValue(0));
    }

    // Tests with() updating an existing field with a new value
    @Test
    public void testWith_existingFieldDifferentValue_updatesValue() {
        Partial partial = new Partial(DateTimeFieldType.hourOfDay(), 10);
        Partial updated = partial.with(DateTimeFieldType.hourOfDay(), 15);
        assertEquals(1, updated.size());
        assertEquals(15, updated.getValue(0));
    }

    // Tests withPeriodAdded() with non-zero scalar and null period
    @Test
    public void testWithPeriodAdded() {
        Partial partial = new Partial(DateTimeFieldType.hourOfDay(), 10);
        Period period = Period.hours(2);
        Partial result = partial.withPeriodAdded(period, 3);
        assertEquals(16, result.getValue(0));

        assertSame(partial, partial.withPeriodAdded(null, 1));
        assertSame(partial, partial.withPeriodAdded(period, 0));
    }

    // Tests withFieldAdded and withFieldAddWrapped throwing on unsupported DurationFieldType
    @Test(expected = IllegalArgumentException.class)
    public void testWithFieldAdded_unsupportedType_throwsException() {
        Partial partial = new Partial(DateTimeFieldType.hourOfDay(), 10);
        partial.withFieldAdded(DurationFieldType.days(), 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithFieldAddWrapped_unsupportedType_throwsException() {
        Partial partial = new Partial(DateTimeFieldType.hourOfDay(), 10);
        partial.withFieldAddWrapped(DurationFieldType.days(), 1);
    }

    // Tests property() with unsupported field type throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testProperty_unsupportedFieldType_throwsException() {
        Partial partial = new Partial(DateTimeFieldType.hourOfDay(), 10);
        partial.property(DateTimeFieldType.dayOfMonth());
    }

    // Tests Property helper accessors
    @Test
    public void testProperty_accessors() {
        Partial partial = new Partial(DateTimeFieldType.dayOfMonth(), 15);
        Partial.Property prop = partial.property(DateTimeFieldType.dayOfMonth());
        assertNotNull(prop.getField());
        assertSame(partial, prop.getReadablePartial());
    }

    // Tests getField(int, Chronology)
    @Test
    public void testGetField_withChronology() {
        Partial partial = new Partial(DateTimeFieldType.year(), 2023);
        DateTimeField field = partial.getField(0, ISOChronology.getInstanceUTC());
        assertEquals("year", field.getName());
    }

    // Tests isMatch(ReadableInstant) with null matching against current time without error
    @Test
    public void testIsMatch_nullInstant() {
        Partial partial = new Partial(DateTimeFieldType.year(), 1900);
        assertFalse(partial.isMatch((ReadableInstant) null));
    }

    // Tests toString() with unparseable pattern fallback
    @Test
    public void testToString_unsupportedPattern() {
        Partial partial = new Partial(DateTimeFieldType.minuteOfHour(), 30);
        assertNotNull(partial.toString());
    }
}