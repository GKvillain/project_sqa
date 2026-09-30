package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Locale;
import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.CopticChronology;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.chrono.GregorianChronology;

public class PartialTest {

    // Tests default constructor and size
    @Test
    public void testConstructor_default_createsEmptyPartial() {
        Partial p = new Partial();
        assertEquals(0, p.size());
        assertEquals(0, p.getFieldTypes().length);
        assertEquals(0, p.getValues().length);
        assertEquals(ISOChronology.getInstanceUTC(), p.getChronology());
        assertNull(p.getFormatter());
        assertEquals("[]", p.toStringList());
    }

    // Tests single field constructor and accessors
    @Test
    public void testConstructor_singleField_initializesCorrectly() {
        Partial p = new Partial(DateTimeFieldType.year(), 2023);
        assertEquals(1, p.size());
        assertEquals(DateTimeFieldType.year(), p.getFieldType(0));
        assertEquals(2023, p.getValue(0));
        assertEquals(2023, p.get(DateTimeFieldType.year()));
        assertTrue(p.isSupported(DateTimeFieldType.year()));
        assertFalse(p.isSupported(DateTimeFieldType.monthOfYear()));
    }

    // Tests constructor with null type throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullType_throwsIllegalArgumentException() {
        new Partial((DateTimeFieldType) null, 1);
    }

    // Tests array constructor with valid multiple fields in descending order
    @Test
    public void testConstructor_arrayTypesAndValues_validInput() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[] { 2023, 5, 20 };
        Partial p = new Partial(types, values, ISOChronology.getInstanceUTC());

        assertEquals(3, p.size());
        assertEquals(2023, p.getValue(0));
        assertEquals(5, p.getValue(1));
        assertEquals(20, p.getValue(2));
    }

    // Tests array constructor with null array throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullTypesArray_throwsException() {
        new Partial((DateTimeFieldType[]) null, new int[] { 1 });
    }

    // Tests array constructor with null values array throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullValuesArray_throwsException() {
        new Partial(new DateTimeFieldType[] { DateTimeFieldType.year() }, null);
    }

    // Tests array constructor with mismatched array length
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_mismatchedArrayLengths_throwsException() {
        DateTimeFieldType[] types = new DateTimeFieldType[] { DateTimeFieldType.year() };
        int[] values = new int[] { 2023, 5 };
        new Partial(types, values);
    }

    // Tests array constructor with null element in types array
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullElementInTypes_throwsException() {
        DateTimeFieldType[] types = new DateTimeFieldType[] { DateTimeFieldType.year(), null };
        int[] values = new int[] { 2023, 5 };
        new Partial(types, values);
    }

    // Tests array constructor with invalid order (smaller before larger)
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_invalidOrder_throwsException() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.dayOfMonth(),
            DateTimeFieldType.monthOfYear()
        };
        int[] values = new int[] { 20, 5 };
        new Partial(types, values);
    }

    // Tests array constructor with duplicate field types
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_duplicateTypes_throwsException() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.dayOfMonth(),
            DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[] { 1, 2 };
        new Partial(types, values);
    }

    // Tests constructor copying from another ReadablePartial
    @Test
    public void testConstructor_readablePartial_copiesValues() {
        Partial source = new Partial(DateTimeFieldType.hourOfDay(), 10);
        Partial copy = new Partial(source);
        assertEquals(1, copy.size());
        assertEquals(10, copy.getValue(0));
    }

    // Tests constructor with null ReadablePartial
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullReadablePartial_throwsException() {
        new Partial((ReadablePartial) null);
    }

    // Tests withChronologyRetainFields
    @Test
    public void testWithChronologyRetainFields_changesChronology() {
        Partial p = new Partial(DateTimeFieldType.dayOfMonth(), 15, ISOChronology.getInstanceUTC());
        Partial buddhist = p.withChronologyRetainFields(BuddhistChronology.getInstanceUTC());
        assertEquals(BuddhistChronology.getInstanceUTC(), buddhist.getChronology());
        assertEquals(15, buddhist.getValue(0));
        assertSame(p, p.withChronologyRetainFields(ISOChronology.getInstanceUTC()));
    }

    // Tests with() method when field is already present
    @Test
    public void testWith_existingField_updatesValue() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        assertSame(p, p.with(DateTimeFieldType.year(), 2020));

        Partial updated = p.with(DateTimeFieldType.year(), 2025);
        assertEquals(2025, updated.get(DateTimeFieldType.year()));
        assertEquals(2020, p.get(DateTimeFieldType.year()));
    }

    // Tests with() method inserting new fields in correct sorted order
    @Test
    public void testWith_newField_maintainsOrder() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 12);
        Partial p2 = p.with(DateTimeFieldType.minuteOfHour(), 30);
        Partial p3 = p2.with(DateTimeFieldType.dayOfMonth(), 15);

        assertEquals(3, p3.size());
        assertEquals(DateTimeFieldType.dayOfMonth(), p3.getFieldType(0));
        assertEquals(DateTimeFieldType.hourOfDay(), p3.getFieldType(1));
        assertEquals(DateTimeFieldType.minuteOfHour(), p3.getFieldType(2));
        assertEquals(15, p3.getValue(0));
        assertEquals(12, p3.getValue(1));
        assertEquals(30, p3.getValue(2));
    }

    // Tests with() method when adding field with same unit duration but different range (Defects4J Time-2 regression)
    @Test
    public void testWith_sameUnitDurationDifferentRange_insertsCorrectly() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        Partial p2 = p.with(DateTimeFieldType.era(), 1);
        assertEquals(2, p2.size());
        assertEquals(DateTimeFieldType.era(), p2.getFieldType(0));
        assertEquals(DateTimeFieldType.year(), p2.getFieldType(1));
    }

    // Tests without() method
    @Test
    public void testWithout_removesFieldCorrectly() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth()
        };
        Partial p = new Partial(types, new int[] { 2023, 5, 20 });

        Partial withoutMonth = p.without(DateTimeFieldType.monthOfYear());
        assertEquals(2, withoutMonth.size());
        assertEquals(DateTimeFieldType.year(), withoutMonth.getFieldType(0));
        assertEquals(DateTimeFieldType.dayOfMonth(), withoutMonth.getFieldType(1));

        assertSame(withoutMonth, withoutMonth.without(DateTimeFieldType.minuteOfHour()));
    }

    // Tests withField(), withFieldAdded(), and withFieldAddWrapped()
    @Test
    public void testWithFieldAndAddOperations() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 6);
        assertSame(p, p.withField(DateTimeFieldType.monthOfYear(), 6));

        Partial modified = p.withField(DateTimeFieldType.monthOfYear(), 8);
        assertEquals(8, modified.getValue(0));

        assertSame(modified, modified.withFieldAdded(DurationFieldType.months(), 0));
        Partial added = modified.withFieldAdded(DurationFieldType.months(), 3);
        assertEquals(11, added.getValue(0));

        assertSame(added, added.withFieldAddWrapped(DurationFieldType.months(), 0));
        Partial wrapped = added.withFieldAddWrapped(DurationFieldType.months(), 3);
        assertEquals(2, wrapped.getValue(0));
    }

    // Tests plus() and minus() period methods
    @Test
    public void testPlusAndMinusPeriod() {
        Partial p = new Partial(DateTimeFieldType.dayOfMonth(), 10);
        assertSame(p, p.plus(null));

        Period period = Period.days(5);
        Partial plusResult = p.plus(period);
        assertEquals(15, plusResult.getValue(0));

        Partial minusResult = plusResult.minus(period);
        assertEquals(10, minusResult.getValue(0));
        assertSame(p, p.minus(null));
    }

    // Tests isMatch() against ReadableInstant and ReadablePartial
    @Test
    public void testIsMatch_instantAndPartial() {
        DateTime dt = new DateTime(2023, 5, 20, 10, 30, ISOChronology.getInstanceUTC());
        Partial matchingPartial = new Partial(DateTimeFieldType.monthOfYear(), 5)
                .with(DateTimeFieldType.dayOfMonth(), 20);
        Partial nonMatchingPartial = new Partial(DateTimeFieldType.monthOfYear(), 6);

        assertTrue(matchingPartial.isMatch(dt));
        assertFalse(nonMatchingPartial.isMatch(dt));

        Partial otherMatch = new Partial(DateTimeFieldType.monthOfYear(), 5)
                .with(DateTimeFieldType.dayOfMonth(), 20)
                .with(DateTimeFieldType.year(), 2023);
        assertTrue(matchingPartial.isMatch(otherMatch));
        assertFalse(nonMatchingPartial.isMatch(otherMatch));
    }

    // Tests isMatch(ReadablePartial) with null throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testIsMatch_nullPartial_throwsException() {
        Partial p = new Partial(DateTimeFieldType.year(), 2023);
        p.isMatch((ReadablePartial) null);
    }

    // Tests toString formatting methods
    @Test
    public void testToString_formatsProperly() {
        Partial p = new Partial(DateTimeFieldType.year(), 2023)
                .with(DateTimeFieldType.monthOfYear(), 5);

        assertNotNull(p.toString());
        assertEquals("2023-05", p.toString("yyyy-MM"));
        assertEquals("2023-05", p.toString("yyyy-MM", Locale.US));
        assertEquals(p.toString(), p.toString(null));
        assertEquals(p.toString(), p.toString(null, Locale.US));
        assertEquals("[year=2023, monthOfYear=5]", p.toStringList());
    }

    // Tests Property inner class operations
    @Test
    public void testProperty_operations() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 5);
        Partial.Property prop = p.property(DateTimeFieldType.monthOfYear());

        assertEquals(5, prop.get());
        assertEquals(DateTimeFieldType.monthOfYear(), prop.getFieldType());
        assertSame(p, prop.getPartial());
        assertSame(p, prop.getReadablePartial());
        assertNotNull(prop.getField());

        Partial propSet = prop.setCopy(10);
        assertEquals(10, propSet.get(DateTimeFieldType.monthOfYear()));

        Partial propSetText = prop.setCopy("December", Locale.ENGLISH);
        assertEquals(12, propSetText.get(DateTimeFieldType.monthOfYear()));

        Partial propSetTextNoLocale = prop.setCopy("6");
        assertEquals(6, propSetTextNoLocale.get(DateTimeFieldType.monthOfYear()));

        Partial propAdd = prop.addToCopy(2);
        assertEquals(7, propAdd.get(DateTimeFieldType.monthOfYear()));

        Partial propAddWrap = prop.addWrapFieldToCopy(10);
        assertEquals(3, propAddWrap.get(DateTimeFieldType.monthOfYear()));

        Partial propMin = prop.withMinimumValue();
        assertEquals(1, propMin.get(DateTimeFieldType.monthOfYear()));

        Partial propMax = prop.withMaximumValue();
        assertEquals(12, propMax.get(DateTimeFieldType.monthOfYear()));
    }

    // Tests Property with unsupported field throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testProperty_unsupportedField_throwsException() {
        Partial p = new Partial(DateTimeFieldType.year(), 2023);
        p.property(DateTimeFieldType.secondOfDay());
    }

    // Tests constructor with Chronology parameter
    @Test
    public void testConstructor_chronology() {
        Partial p = new Partial(CopticChronology.getInstanceUTC());
        assertEquals(CopticChronology.getInstanceUTC(), p.getChronology());
        assertEquals(0, p.size());

        Partial pNull = new Partial((Chronology) null);
        assertEquals(ISOChronology.getInstanceUTC(), pNull.getChronology());
    }

    // Tests constructor with DateTimeFieldType, value and Chronology
    @Test
    public void testConstructor_typeValueChronology() {
        Partial p = new Partial(DateTimeFieldType.year(), 2024, CopticChronology.getInstanceUTC());
        assertEquals(CopticChronology.getInstanceUTC(), p.getChronology());
        assertEquals(2024, p.get(DateTimeFieldType.year()));

        Partial pNullChrono = new Partial(DateTimeFieldType.year(), 2024, null);
        assertEquals(ISOChronology.getInstanceUTC(), pNullChrono.getChronology());
    }

    // Tests indexOf methods
    @Test
    public void testIndexOf() {
        Partial p = new Partial(DateTimeFieldType.year(), 2023)
                .with(DateTimeFieldType.monthOfYear(), 5)
                .with(DateTimeFieldType.dayOfMonth(), 20);

        assertEquals(0, p.indexOf(DateTimeFieldType.year()));
        assertEquals(1, p.indexOf(DateTimeFieldType.monthOfYear()));
        assertEquals(2, p.indexOf(DateTimeFieldType.dayOfMonth()));
        assertEquals(-1, p.indexOf(DateTimeFieldType.hourOfDay()));

        assertEquals(0, p.indexOf(DurationFieldType.years()));
        assertEquals(1, p.indexOf(DurationFieldType.months()));
        assertEquals(2, p.indexOf(DurationFieldType.days()));
        assertEquals(-1, p.indexOf(DurationFieldType.hours()));
    }

    // Tests getField and getFields
    @Test
    public void testGetFields() {
        Partial p = new Partial(DateTimeFieldType.year(), 2023)
                .with(DateTimeFieldType.monthOfYear(), 5);
        DateTimeField[] fields = p.getFields();
        assertEquals(2, fields.length);
        assertEquals(fields[0], p.getField(0, ISOChronology.getInstanceUTC()));
        assertEquals(fields[1], p.getField(1, ISOChronology.getInstanceUTC()));
    }

    // Tests with() null fieldType throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWith_nullFieldType_throwsException() {
        Partial p = new Partial(DateTimeFieldType.year(), 2023);
        p.with(null, 5);
    }

    // Tests without() null fieldType returns same instance
    @Test
    public void testWithout_nullFieldType() {
        Partial p = new Partial(DateTimeFieldType.year(), 2023);
        assertSame(p, p.without(null));
    }

    // Tests withField() throwing IllegalArgumentException when field unsupported
    @Test(expected = IllegalArgumentException.class)
    public void testWithField_unsupportedField_throwsException() {
        Partial p = new Partial(DateTimeFieldType.year(), 2023);
        p.withField(DateTimeFieldType.monthOfYear(), 5);
    }

    // Tests withFieldAdded() throwing IllegalArgumentException when field unsupported
    @Test(expected = IllegalArgumentException.class)
    public void testWithFieldAdded_unsupportedField_throwsException() {
        Partial p = new Partial(DateTimeFieldType.year(), 2023);
        p.withFieldAdded(DurationFieldType.months(), 1);
    }

    // Tests withFieldAddWrapped() throwing IllegalArgumentException when field unsupported
    @Test(expected = IllegalArgumentException.class)
    public void testWithFieldAddWrapped_unsupportedField_throwsException() {
        Partial p = new Partial(DateTimeFieldType.year(), 2023);
        p.withFieldAddWrapped(DurationFieldType.months(), 1);
    }

    // Tests withPeriodAdded with zero and non-zero scalar
    @Test
    public void testWithPeriodAdded() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 5);
        assertSame(p, p.withPeriodAdded(null, 1));
        assertSame(p, p.withPeriodAdded(Period.months(2), 0));

        Partial result = p.withPeriodAdded(Period.months(2), 2);
        assertEquals(9, result.get(DateTimeFieldType.monthOfYear()));
    }

    // Tests isMatch with null instant (uses current time without exception)
    @Test
    public void testIsMatch_nullInstant() {
        Partial p = new Partial();
        assertTrue(p.isMatch((ReadableInstant) null));
    }

    // Tests toString with various field combinations and caching formatter
    @Test
    public void testToString_variousCombinations() {
        Partial timeOnly = new Partial(DateTimeFieldType.hourOfDay(), 10)
                .with(DateTimeFieldType.minuteOfHour(), 30);
        assertNotNull(timeOnly.toString());

        Partial dateOnly = new Partial(DateTimeFieldType.year(), 2023)
                .with(DateTimeFieldType.monthOfYear(), 5)
                .with(DateTimeFieldType.dayOfMonth(), 20);
        assertEquals("2023-05-20", dateOnly.toString());

        // Test formatter caching branch
        assertNotNull(dateOnly.getFormatter());
        assertEquals("2023-05-20", dateOnly.toString());
    }

    // Tests withChronologyRetainFields with null chronology defaulting to ISO
    @Test
    public void testWithChronologyRetainFields_nullDefaultsToISO() {
        Partial p = new Partial(DateTimeFieldType.year(), 2023, GregorianChronology.getInstanceUTC());
        Partial res = p.withChronologyRetainFields(null);
        assertEquals(ISOChronology.getInstanceUTC(), res.getChronology());
    }
}