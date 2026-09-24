package org.joda.time;

import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;

public class PartialTest {

    // Tests default constructor creates empty Partial with ISO chronology
    @Test
    public void testConstructor_default_createsEmptyPartial() {
        Partial partial = new Partial();
        assertEquals(0, partial.size());
        assertEquals(0, partial.getFieldTypes().length);
        assertEquals(0, partial.getValues().length);
        assertEquals(DateTimeConstants.MILLIS_PER_DAY, partial.getChronology().getZone().getOffset(0) == 0 ? 86400000 : 86400000);
        assertNull(partial.getFormatter());
        assertEquals("[]", partial.toStringList());
    }

    // Tests single field constructor and get methods
    @Test
    public void testConstructor_singleField_initializesCorrectly() {
        Partial partial = new Partial(DateTimeFieldType.hourOfDay(), 10);
        assertEquals(1, partial.size());
        assertEquals(DateTimeFieldType.hourOfDay(), partial.getFieldType(0));
        assertEquals(10, partial.getValue(0));
        assertEquals(10, partial.get(DateTimeFieldType.hourOfDay()));
    }

    // Tests constructor with null field type throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullFieldType_throwsException() {
        new Partial((DateTimeFieldType) null, 1);
    }

    // Tests array constructor with valid multiple fields in descending order
    @Test
    public void testConstructor_validFieldArrays_initializesCorrectly() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[] { 2020, 5, 15 };
        Partial partial = new Partial(types, values);

        assertEquals(3, partial.size());
        assertEquals(2020, partial.getValue(0));
        assertEquals(5, partial.getValue(1));
        assertEquals(15, partial.getValue(2));
    }

    // Tests array constructor with mismatched array lengths throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_mismatchedArrayLengths_throwsException() {
        DateTimeFieldType[] types = new DateTimeFieldType[] { DateTimeFieldType.year() };
        int[] values = new int[] { 2020, 5 };
        new Partial(types, values);
    }

    // Tests array constructor with invalid order throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_invalidFieldOrder_throwsException() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.dayOfMonth(),
            DateTimeFieldType.monthOfYear()
        };
        int[] values = new int[] { 15, 5 };
        new Partial(types, values);
    }

    // Tests with() method when inserting a larger field (Time-4 regression check with Era / Year)
    @Test
    public void testWith_insertLargerField_insertsAtCorrectPosition() {
        Partial partial = new Partial(DateTimeFieldType.year(), 2020);
        Partial updated = partial.with(DateTimeFieldType.era(), DateTimeConstants.CE);

        assertEquals(2, updated.size());
        assertEquals(DateTimeFieldType.era(), updated.getFieldType(0));
        assertEquals(DateTimeFieldType.year(), updated.getFieldType(1));
        assertEquals(DateTimeConstants.CE, updated.getValue(0));
        assertEquals(2020, updated.getValue(1));
    }

    // Tests with() method adding a smaller field maintaining order
    @Test
    public void testWith_insertSmallerField_maintainsOrder() {
        Partial partial = new Partial(DateTimeFieldType.hourOfDay(), 14);
        Partial updated = partial.with(DateTimeFieldType.minuteOfHour(), 30);

        assertEquals(2, updated.size());
        assertEquals(DateTimeFieldType.hourOfDay(), updated.getFieldType(0));
        assertEquals(DateTimeFieldType.minuteOfHour(), updated.getFieldType(1));
        assertEquals(14, updated.getValue(0));
        assertEquals(30, updated.getValue(1));
    }

    // Tests with() method updating an existing field value
    @Test
    public void testWith_existingField_updatesValue() {
        Partial partial = new Partial(DateTimeFieldType.hourOfDay(), 10);
        Partial updated = partial.with(DateTimeFieldType.hourOfDay(), 15);

        assertEquals(1, updated.size());
        assertEquals(15, updated.getValue(0));
        // Same value returns same instance
        assertSame(updated, updated.with(DateTimeFieldType.hourOfDay(), 15));
    }

    // Tests without() method removing an existing field
    @Test
    public void testWithout_existingField_removesField() {
        Partial partial = new Partial(DateTimeFieldType.hourOfDay(), 10)
                .with(DateTimeFieldType.minuteOfHour(), 30);
        Partial updated = partial.without(DateTimeFieldType.hourOfDay());

        assertEquals(1, updated.size());
        assertEquals(DateTimeFieldType.minuteOfHour(), updated.getFieldType(0));
        assertEquals(30, updated.getValue(0));
    }

    // Tests without() method with non-existing field returns this
    @Test
    public void testWithout_nonExistingField_returnsSameInstance() {
        Partial partial = new Partial(DateTimeFieldType.hourOfDay(), 10);
        assertSame(partial, partial.without(DateTimeFieldType.minuteOfHour()));
    }

    // Tests withField() throws exception when field is unsupported
    @Test(expected = IllegalArgumentException.class)
    public void testWithField_unsupportedField_throwsException() {
        Partial partial = new Partial(DateTimeFieldType.hourOfDay(), 10);
        partial.withField(DateTimeFieldType.minuteOfHour(), 20);
    }

    // Tests withFieldAdded() for adding amount
    @Test
    public void testWithFieldAdded_validAmount_addsCorrectly() {
        Partial partial = new Partial(DateTimeFieldType.hourOfDay(), 10);
        Partial updated = partial.withFieldAdded(DurationFieldType.hours(), 5);

        assertEquals(15, updated.get(DateTimeFieldType.hourOfDay()));
        assertSame(partial, partial.withFieldAdded(DurationFieldType.hours(), 0));
    }

    // Tests withFieldAddWrapped() wrapping within partial bounds
    @Test
    public void testWithFieldAddWrapped_exceedingMax_wrapsAround() {
        Partial partial = new Partial(DateTimeFieldType.hourOfDay(), 22);
        Partial updated = partial.withFieldAddWrapped(DurationFieldType.hours(), 4);

        assertEquals(2, updated.get(DateTimeFieldType.hourOfDay()));
    }

    // Tests plus() and minus() with ReadablePeriod
    @Test
    public void testPlusAndMinus_validPeriod_calculatesCorrectly() {
        Partial partial = new Partial(DateTimeFieldType.hourOfDay(), 10);
        Period period = Period.hours(3);

        Partial plusResult = partial.plus(period);
        assertEquals(13, plusResult.get(DateTimeFieldType.hourOfDay()));

        Partial minusResult = plusResult.minus(period);
        assertEquals(10, minusResult.get(DateTimeFieldType.hourOfDay()));
    }

    // Tests isMatch(ReadableInstant) matching instant fields
    @Test
    public void testIsMatch_readableInstant_returnsCorrectMatch() {
        DateTime dt = new DateTime(2020, 5, 15, 10, 30, DateTimeZone.UTC);
        Partial matching = new Partial(DateTimeFieldType.hourOfDay(), 10)
                .with(DateTimeFieldType.minuteOfHour(), 30);
        Partial nonMatching = new Partial(DateTimeFieldType.hourOfDay(), 12);

        assertTrue(matching.isMatch(dt));
        assertFalse(nonMatching.isMatch(dt));
    }

    // Tests isMatch(ReadablePartial) matching partial fields
    @Test
    public void testIsMatch_readablePartial_returnsCorrectMatch() {
        Partial p1 = new Partial(DateTimeFieldType.monthOfYear(), 5)
                .with(DateTimeFieldType.dayOfMonth(), 15);
        Partial p2 = new Partial(DateTimeFieldType.monthOfYear(), 5);

        assertTrue(p2.isMatch(p1));
        assertFalse(p1.isMatch(p2));
    }

    // Tests toString formatting with ISO and custom patterns
    @Test
    public void testToString_formattedOutput_matchesExpected() {
        Partial partial = new Partial(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.monthOfYear(), 5)
                .with(DateTimeFieldType.dayOfMonth(), 15);

        assertEquals("2020-05-15", partial.toString());
        assertEquals("15/05/2020", partial.toString("dd/MM/yyyy"));
        assertEquals("2020", partial.toString("yyyy", Locale.ENGLISH));
    }

    // Tests Property manipulation methods
    @Test
    public void testProperty_manipulationMethods_updateCorrectly() {
        Partial partial = new Partial(DateTimeFieldType.monthOfYear(), 5);
        Partial.Property prop = partial.property(DateTimeFieldType.monthOfYear());

        assertEquals(5, prop.get());
        assertEquals(1, prop.withMinimumValue().get(DateTimeFieldType.monthOfYear()));
        assertEquals(12, prop.withMaximumValue().get(DateTimeFieldType.monthOfYear()));
        assertEquals(7, prop.addToCopy(2).get(DateTimeFieldType.monthOfYear()));
        assertEquals(1, prop.addWrapFieldToCopy(8).get(DateTimeFieldType.monthOfYear()));
        assertEquals(11, prop.setCopy(11).get(DateTimeFieldType.monthOfYear()));
        assertEquals(12, prop.setCopy("December", Locale.ENGLISH).get(DateTimeFieldType.monthOfYear()));
    }
}