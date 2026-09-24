package org.joda.time.chrono;

import java.util.Locale;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.joda.time.Chronology;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationField;
import org.joda.time.IllegalFieldValueException;

public class ZonedChronologyTest {

    private DateTimeZone originalZone;
    private DateTimeZone zoneLondon;
    private DateTimeZone zoneParis;
    private DateTimeZone zoneNewYork;
    private Chronology isoUtc;
    private ZonedChronology zonedLondon;

    @Before
    public void setUp() {
        originalZone = DateTimeZone.getDefault();
        DateTimeZone.setDefault(DateTimeZone.UTC);

        zoneLondon = DateTimeZone.forID("Europe/London");
        zoneParis = DateTimeZone.forID("Europe/Paris");
        zoneNewYork = DateTimeZone.forID("America/New_York");
        isoUtc = ISOChronology.getInstanceUTC();
        zonedLondon = ZonedChronology.getInstance(isoUtc, zoneLondon);
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(originalZone);
    }

    // Tests getInstance with null chronology throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetInstance_nullChronology_throwsException() {
        ZonedChronology.getInstance(null, zoneLondon);
    }

    // Tests getInstance with null zone throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetInstance_nullZone_throwsException() {
        ZonedChronology.getInstance(isoUtc, null);
    }

    // Tests valid getInstance returns configured instance
    @Test
    public void testGetInstance_validInputs_returnsZonedChronology() {
        ZonedChronology chrono = ZonedChronology.getInstance(isoUtc, zoneParis);
        assertNotNull(chrono);
        assertEquals(zoneParis, chrono.getZone());
        assertEquals(isoUtc, chrono.withUTC());
    }

    // Tests withZone method branches
    @Test
    public void testWithZone_variousInputs_returnsExpectedChronology() {
        // Same zone
        assertSame(zonedLondon, zonedLondon.withZone(zoneLondon));

        // UTC zone returns base
        assertSame(isoUtc, zonedLondon.withZone(DateTimeZone.UTC));

        // Different zone
        Chronology parisChrono = zonedLondon.withZone(zoneParis);
        assertEquals(zoneParis, parisChrono.getZone());

        // Null zone defaults to default zone (UTC here)
        Chronology defaultZoned = zonedLondon.withZone(null);
        assertEquals(DateTimeZone.UTC, defaultZoned.getZone());
    }

    // Tests getDateTimeMillis with 4 parameters (date + millisOfDay)
    @Test
    public void testGetDateTimeMillis_fourArgs_returnsCorrectMillis() {
        long millis = zonedLondon.getDateTimeMillis(2007, 6, 15, 12 * DateTimeConstants.MILLIS_PER_HOUR);
        // London is UTC+1 in summer (BST), so 12:00 local is 11:00 UTC
        long expected = isoUtc.getDateTimeMillis(2007, 6, 15, 11, 0, 0, 0);
        assertEquals(expected, millis);
    }

    // Tests getDateTimeMillis with 7 parameters (date + time)
    @Test
    public void testGetDateTimeMillis_sevenArgs_returnsCorrectMillis() {
        long millis = zonedLondon.getDateTimeMillis(2007, 1, 15, 12, 30, 45, 500);
        // London is UTC+0 in winter (GMT)
        long expected = isoUtc.getDateTimeMillis(2007, 1, 15, 12, 30, 45, 500);
        assertEquals(expected, millis);
    }

    // Tests getDateTimeMillis with 5 parameters (instant base + time)
    @Test
    public void testGetDateTimeMillis_fiveArgs_returnsCorrectMillis() {
        long baseInstant = isoUtc.getDateTimeMillis(2007, 6, 15, 0, 0, 0, 0);
        long millis = zonedLondon.getDateTimeMillis(baseInstant, 14, 0, 0, 0);
        // 14:00 BST -> 13:00 UTC
        long expected = isoUtc.getDateTimeMillis(2007, 6, 15, 13, 0, 0, 0);
        assertEquals(expected, millis);
    }

    // Tests getDateTimeMillis throwing exception on DST gap transition
    @Test(expected = IllegalArgumentException.class)
    public void testGetDateTimeMillis_duringGapTransition_throwsException() {
        // Gap in Europe/London: 2007-03-25 01:00:00 skipped to 02:00:00
        zonedLondon.getDateTimeMillis(2007, 3, 25, 1, 30, 0, 0);
    }

    // Tests equals, hashCode, and toString
    @Test
    public void testEqualsAndHashCodeAndToString() {
        ZonedChronology same = ZonedChronology.getInstance(isoUtc, zoneLondon);
        ZonedChronology differentZone = ZonedChronology.getInstance(isoUtc, zoneParis);

        assertTrue(zonedLondon.equals(zonedLondon));
        assertTrue(zonedLondon.equals(same));
        assertFalse(zonedLondon.equals(differentZone));
        assertFalse(zonedLondon.equals(null));
        assertFalse(zonedLondon.equals("string"));

        assertEquals(zonedLondon.hashCode(), same.hashCode());
        assertEquals("ZonedChronology[ISOChronology[UTC], Europe/London]", zonedLondon.toString());
    }

    // Tests useTimeArithmetic helper logic
    @Test
    public void testUseTimeArithmetic_logic() {
        DurationField hours = zonedLondon.hours();
        DurationField days = zonedLondon.days();
        assertTrue(ZonedChronology.useTimeArithmetic(hours));
        assertFalse(ZonedChronology.useTimeArithmetic(days));
        assertFalse(ZonedChronology.useTimeArithmetic(null));
    }

    // Tests ZonedDateTimeField get and getAsText methods
    @Test
    public void testZonedDateTimeField_getAndText() {
        // 2007-06-15 12:00:00 UTC -> 13:00:00 BST
        long instant = isoUtc.getDateTimeMillis(2007, 6, 15, 12, 0, 0, 0);
        DateTimeField hourField = zonedLondon.hourOfDay();

        assertEquals(13, hourField.get(instant));
        assertEquals("13", hourField.getAsText(instant, Locale.ENGLISH));
        assertEquals("13", hourField.getAsShortText(instant, Locale.ENGLISH));
        assertEquals("13", hourField.getAsText(13, Locale.ENGLISH));
        assertEquals("13", hourField.getAsShortText(13, Locale.ENGLISH));
        assertNotNull(hourField.getDurationField());
        assertNotNull(hourField.getRangeDurationField());
        assertFalse(hourField.isLenient());
    }

    // Tests ZonedDateTimeField add and addWrapField for time field (uses time arithmetic)
    @Test
    public void testZonedDateTimeField_addAndTimeArithmetic() {
        DateTimeField hourField = zonedLondon.hourOfDay();
        long instant = isoUtc.getDateTimeMillis(2007, 6, 15, 12, 0, 0, 0); // 13:00 local

        long addedInt = hourField.add(instant, 2);
        assertEquals(15, hourField.get(addedInt));

        long addedLong = hourField.add(instant, 5L);
        assertEquals(18, hourField.get(addedLong));

        long wrapped = hourField.addWrapField(instant, 15);
        assertEquals(4, hourField.get(wrapped));
    }

    // Tests ZonedDateTimeField add and addWrapField for date field (uses date arithmetic)
    @Test
    public void testZonedDateTimeField_addDateArithmetic() {
        DateTimeField dayField = zonedLondon.dayOfMonth();
        long instant = isoUtc.getDateTimeMillis(2007, 6, 15, 12, 0, 0, 0);

        long addedInt = dayField.add(instant, 3);
        assertEquals(18, dayField.get(addedInt));

        long addedLong = dayField.add(instant, 10L);
        assertEquals(25, dayField.get(addedLong));

        long wrapped = dayField.addWrapField(instant, 20);
        assertEquals(5, dayField.get(wrapped));
    }

    // Tests ZonedDateTimeField set method and transition check
    @Test
    public void testZonedDateTimeField_set() {
        DateTimeField monthField = zonedLondon.monthOfYear();
        long instant = isoUtc.getDateTimeMillis(2007, 6, 15, 12, 0, 0, 0);

        long setVal = monthField.set(instant, 11);
        assertEquals(11, monthField.get(setVal));

        long setText = monthField.set(instant, "December", Locale.ENGLISH);
        assertEquals(12, monthField.get(setText));
    }

    // Tests ZonedDateTimeField set throwing IllegalFieldValueException when setting into DST gap
    @Test(expected = IllegalFieldValueException.class)
    public void testZonedDateTimeField_setIntoDstGap_throwsException() {
        // America/New_York DST gap on 2007-03-11: 02:00 -> 03:00
        ZonedChronology nyChrono = ZonedChronology.getInstance(isoUtc, zoneNewYork);
        long instant = nyChrono.getDateTimeMillis(2007, 3, 11, 1, 30, 0, 0);
        DateTimeField hourField = nyChrono.hourOfDay();
        hourField.set(instant, 2);
    }

    // Tests ZonedDateTimeField rounding operations (floor, ceiling, remainder)
    @Test
    public void testZonedDateTimeField_rounding() {
        DateTimeField hourField = zonedLondon.hourOfDay();
        long instant = isoUtc.getDateTimeMillis(2007, 6, 15, 12, 35, 45, 500);

        long floor = hourField.roundFloor(instant);
        assertEquals(isoUtc.getDateTimeMillis(2007, 6, 15, 12, 0, 0, 0), floor);

        long ceiling = hourField.roundCeiling(instant);
        assertEquals(isoUtc.getDateTimeMillis(2007, 6, 15, 13, 0, 0, 0), ceiling);

        long remainder = hourField.remainder(instant);
        assertEquals(35 * 60 * 1000 + 45 * 1000 + 500, remainder);

        DateTimeField dayField = zonedLondon.dayOfMonth();
        long dayFloor = dayField.roundFloor(instant);
        // 2007-06-15 00:00:00 BST -> 2007-06-14 23:00:00 UTC
        assertEquals(isoUtc.getDateTimeMillis(2007, 6, 14, 23, 0, 0, 0), dayFloor);

        long dayCeil = dayField.roundCeiling(instant);
        assertEquals(isoUtc.getDateTimeMillis(2007, 6, 15, 23, 0, 0, 0), dayCeil);
    }

    // Tests ZonedDateTimeField difference calculations
    @Test
    public void testZonedDateTimeField_difference() {
        DateTimeField hourField = zonedLondon.hourOfDay();
        long start = isoUtc.getDateTimeMillis(2007, 6, 15, 10, 0, 0, 0);
        long end = isoUtc.getDateTimeMillis(2007, 6, 15, 15, 0, 0, 0);

        assertEquals(5, hourField.getDifference(end, start));
        assertEquals(5L, hourField.getDifferenceAsLong(end, start));

        DateTimeField dayField = zonedLondon.dayOfMonth();
        long endDay = isoUtc.getDateTimeMillis(2007, 6, 20, 10, 0, 0, 0);
        assertEquals(5, dayField.getDifference(endDay, start));
        assertEquals(5L, dayField.getDifferenceAsLong(endDay, start));
    }

    // Tests ZonedDateTimeField min/max and leap methods
    @Test
    public void testZonedDateTimeField_minMaxAndLeap() {
        DateTimeField dayField = zonedLondon.dayOfMonth();
        long instant = isoUtc.getDateTimeMillis(2007, 2, 15, 12, 0, 0, 0);

        assertEquals(1, dayField.getMinimumValue());
        assertEquals(1, dayField.getMinimumValue(instant));
        assertEquals(31, dayField.getMaximumValue());
        assertEquals(28, dayField.getMaximumValue(instant));

        DateTimeField yearField = zonedLondon.year();
        assertFalse(yearField.isLeap(instant));
        assertEquals(0, yearField.getLeapAmount(instant));
        assertNotNull(yearField.getLeapDurationField());

        long leapInstant = isoUtc.getDateTimeMillis(2008, 2, 15, 12, 0, 0, 0);
        assertTrue(yearField.isLeap(leapInstant));
        assertEquals(1, yearField.getLeapAmount(leapInstant));

        assertEquals(2, dayField.getMaximumTextLength(Locale.ENGLISH));
        assertEquals(2, dayField.getMaximumShortTextLength(Locale.ENGLISH));
    }

    // Tests ZonedDurationField methods (isPrecise, getUnitMillis, getValue, getMillis, add, difference)
    @Test
    public void testZonedDurationField_operations() {
        DurationField hours = zonedLondon.hours();
        assertTrue(hours.isPrecise());
        assertEquals(DateTimeConstants.MILLIS_PER_HOUR, hours.getUnitMillis());

        long instant = isoUtc.getDateTimeMillis(2007, 6, 15, 12, 0, 0, 0);
        assertEquals(2, hours.getValue(2 * DateTimeConstants.MILLIS_PER_HOUR, instant));
        assertEquals(2L, hours.getValueAsLong(2 * DateTimeConstants.MILLIS_PER_HOUR, instant));
        assertEquals(2 * DateTimeConstants.MILLIS_PER_HOUR, hours.getMillis(2, instant));
        assertEquals(2 * DateTimeConstants.MILLIS_PER_HOUR, hours.getMillis(2L, instant));

        long added = hours.add(instant, 3);
        assertEquals(instant + 3 * DateTimeConstants.MILLIS_PER_HOUR, added);

        long addedLong = hours.add(instant, 4L);
        assertEquals(instant + 4 * DateTimeConstants.MILLIS_PER_HOUR, addedLong);

        assertEquals(3, hours.getDifference(added, instant));
        assertEquals(4L, hours.getDifferenceAsLong(addedLong, instant));

        // Duration field for days (non-time arithmetic)
        DurationField days = zonedLondon.days();
        assertFalse(days.isPrecise());
        long addedDays = days.add(instant, 2);
        assertEquals(isoUtc.getDateTimeMillis(2007, 6, 17, 12, 0, 0, 0), addedDays);
        assertEquals(2, days.getDifference(addedDays, instant));
        assertEquals(2L, days.getDifferenceAsLong(addedDays, instant));
    }
}