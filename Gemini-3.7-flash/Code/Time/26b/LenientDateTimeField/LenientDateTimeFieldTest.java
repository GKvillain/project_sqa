package org.joda.time.field;

import org.joda.time.Chronology;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.ISOChronology;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class LenientDateTimeFieldTest {

    private Chronology isoUtc;
    private Chronology isoParis;
    private DateTimeZone parisZone;

    @Before
    public void setUp() {
        parisZone = DateTimeZone.forID("Europe/Paris");
        isoUtc = ISOChronology.getInstanceUTC();
        isoParis = ISOChronology.getInstance(parisZone);
    }

    // Tests null field input returns null
    @Test
    public void testGetInstance_nullField_returnsNull() {
        DateTimeField result = LenientDateTimeField.getInstance(null, isoUtc);
        assertNull(result);
    }

    // Tests field that is already lenient returns same instance
    @Test
    public void testGetInstance_alreadyLenientField_returnsSame() {
        DateTimeField rawField = isoUtc.hourOfDay();
        DateTimeField lenient = LenientDateTimeField.getInstance(rawField, isoUtc);
        DateTimeField result = LenientDateTimeField.getInstance(lenient, isoUtc);
        assertSame(lenient, result);
    }

    // Tests unwrapping StrictDateTimeField before creating LenientDateTimeField
    @Test
    public void testGetInstance_strictDateTimeField_unwrapsAndCreatesLenient() {
        DateTimeField rawField = isoUtc.dayOfMonth();
        DateTimeField strict = StrictDateTimeField.getInstance(rawField);
        DateTimeField lenient = LenientDateTimeField.getInstance(strict, isoUtc);

        assertNotNull(lenient);
        assertTrue(lenient.isLenient());
        assertTrue(lenient instanceof LenientDateTimeField);
    }

    // Tests normal non-lenient field returns new LenientDateTimeField instance
    @Test
    public void testGetInstance_normalField_returnsLenientDateTimeField() {
        DateTimeField rawField = isoUtc.monthOfYear();
        DateTimeField lenient = LenientDateTimeField.getInstance(rawField, isoUtc);

        assertNotNull(lenient);
        assertTrue(lenient.isLenient());
        assertEquals(rawField.getType(), lenient.getType());
    }

    // Tests isLenient always returns true
    @Test
    public void testIsLenient_always_returnsTrue() {
        DateTimeField lenient = LenientDateTimeField.getInstance(isoUtc.secondOfMinute(), isoUtc);
        assertTrue(lenient.isLenient());
    }

    // Tests set with valid standard within-bounds value
    @Test
    public void testSet_validWithinBoundsValue_setsCorrectly() {
        DateTimeField lenient = LenientDateTimeField.getInstance(isoUtc.hourOfDay(), isoUtc);
        long instant = 0L; // 1970-01-01T00:00:00.000Z
        long result = lenient.set(instant, 5);
        assertEquals(5 * 3600000L, result);
        assertEquals(5, lenient.get(result));
    }

    // Tests set with same value returns unchanged instant
    @Test
    public void testSet_sameCurrentValue_returnsUnchangedInstant() {
        DateTimeField lenient = LenientDateTimeField.getInstance(isoUtc.minuteOfHour(), isoUtc);
        long instant = 123456789L;
        int currentValue = lenient.get(instant);
        long result = lenient.set(instant, currentValue);
        assertEquals(instant, result);
    }

    // Tests set with out-of-bounds positive value in UTC
    @Test
    public void testSet_outOfBoundsPositiveValue_addsDifferenceCorrectly() {
        DateTimeField lenient = LenientDateTimeField.getInstance(isoUtc.dayOfMonth(), isoUtc);
        // 1970-01-01 -> day 32 should become 1970-02-01
        long instant = 0L;
        long result = lenient.set(instant, 32);
        long expected = 31L * 86400000L;
        assertEquals(expected, result);
        assertEquals(1, isoUtc.dayOfMonth().get(result));
        assertEquals(2, isoUtc.monthOfYear().get(result));
    }

    // Tests set with zero or negative value
    @Test
    public void testSet_outOfBoundsNegativeValue_subtractsDifferenceCorrectly() {
        DateTimeField lenient = LenientDateTimeField.getInstance(isoUtc.dayOfMonth(), isoUtc);
        // 1970-01-01 -> day 0 should become 1969-12-31
        long instant = 0L;
        long result = lenient.set(instant, 0);
        long expected = -1L * 86400000L;
        assertEquals(expected, result);
        assertEquals(31, isoUtc.dayOfMonth().get(result));
        assertEquals(12, isoUtc.monthOfYear().get(result));
        assertEquals(1969, isoUtc.year().get(result));
    }

    // Tests set using time zone chronology with local offset
    @Test
    public void testSet_withNonUtcTimeZoneChronology_calculatesCorrectInstant() {
        DateTimeField lenient = LenientDateTimeField.getInstance(isoParis.hourOfDay(), isoParis);
        long instant = 0L; // 1970-01-01T01:00:00.000+01:00 in Paris
        long result = lenient.set(instant, 25);
        long expected = 24 * 3600000L;
        assertEquals(expected, result);
        assertEquals(1, lenient.get(result));
    }

    // Tests set with month field exceeding 12 months
    @Test
    public void testSet_monthExceedingYearBoundary_rollsOverYear() {
        DateTimeField lenient = LenientDateTimeField.getInstance(isoUtc.monthOfYear(), isoUtc);
        long instant = 0L; // 1970-01-01
        long result = lenient.set(instant, 14); // month 14 -> 1971-02-01
        assertEquals(1971, isoUtc.year().get(result));
        assertEquals(2, isoUtc.monthOfYear().get(result));
        assertEquals(1, isoUtc.dayOfMonth().get(result));
    }

    // Tests set with non-ISO chronology
    @Test
    public void testSet_buddhistChronology_calculatesCorrectly() {
        Chronology buddhist = BuddhistChronology.getInstanceUTC();
        DateTimeField lenient = LenientDateTimeField.getInstance(buddhist.hourOfDay(), buddhist);
        long instant = 0L;
        long result = lenient.set(instant, 30);
        assertEquals(30 * 3600000L, result);
        assertEquals(6, lenient.get(result));
    }

    // Tests getType delegates correctly
    @Test
    public void testGetType_returnsUnderlyingFieldType() {
        DateTimeField lenient = LenientDateTimeField.getInstance(isoUtc.secondOfMinute(), isoUtc);
        assertEquals(DateTimeFieldType.secondOfMinute(), lenient.getType());
    }

    // Tests set into daylight saving time spring-forward gap
    @Test
    public void testSet_dstGap_resolvesCorrectly() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        Chronology chrono = ISOChronology.getInstance(zone);
        DateTimeField lenient = LenientDateTimeField.getInstance(chrono.hourOfDay(), chrono);

        // 2007-03-11T01:30:00.000-05:00 (EST)
        // 2:00 AM -> 3:00 AM is the spring-forward gap in America/New_York
        long instant = chrono.getDateTimeMillis(2007, 3, 11, 1, 30, 0, 0);
        long result = lenient.set(instant, 2); // setting to non-existent 2:30 AM
        assertNotNull(result);
        assertEquals(3, chrono.hourOfDay().get(result));
        assertEquals(30, chrono.minuteOfHour().get(result));
    }

    // Tests set into daylight saving time autumn-fallback overlap
    @Test
    public void testSet_dstOverlap_resolvesCorrectly() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        Chronology chrono = ISOChronology.getInstance(zone);
        DateTimeField lenient = LenientDateTimeField.getInstance(chrono.hourOfDay(), chrono);

        // 2007-11-04T00:30:00.000-04:00 (EDT)
        // 1:00 AM - 2:00 AM occurs twice
        long instant = chrono.getDateTimeMillis(2007, 11, 4, 0, 30, 0, 0);
        long result = lenient.set(instant, 1);
        assertEquals(1, chrono.hourOfDay().get(result));
        assertEquals(30, chrono.minuteOfHour().get(result));
    }

    // Tests set with year field and large offsets
    @Test
    public void testSet_yearField_largeOffset() {
        DateTimeField lenient = LenientDateTimeField.getInstance(isoUtc.year(), isoUtc);
        long instant = 0L; // 1970-01-01
        long result = lenient.set(instant, 2020);
        assertEquals(2020, isoUtc.year().get(result));
        assertEquals(1, isoUtc.monthOfYear().get(result));
        assertEquals(1, isoUtc.dayOfMonth().get(result));
    }

    // Tests set with millisOfSecond field
    @Test
    public void testSet_millisOfSecond_exceedingSecondBoundary() {
        DateTimeField lenient = LenientDateTimeField.getInstance(isoUtc.millisOfSecond(), isoUtc);
        long instant = 0L;
        long result = lenient.set(instant, 1500);
        assertEquals(1500L, result);
        assertEquals(1, isoUtc.secondOfMinute().get(result));
        assertEquals(500, isoUtc.millisOfSecond().get(result));
    }
}