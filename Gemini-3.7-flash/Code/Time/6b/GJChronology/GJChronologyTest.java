package org.joda.time.chrono;

import java.util.Locale;
import org.joda.time.Chronology;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationField;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GJChronologyTest {

    // Tests getInstance factory methods and caching
    @Test
    public void testGetInstance_defaultAndCached() {
        GJChronology chrono1 = GJChronology.getInstance();
        GJChronology chrono2 = GJChronology.getInstance(DateTimeZone.getDefault());
        assertSame(chrono1, chrono2);
        assertEquals(DateTimeZone.getDefault(), chrono1.getZone());
        assertEquals(4, chrono1.getMinimumDaysInFirstWeek());
        assertEquals(GJChronology.DEFAULT_CUTOVER, chrono1.getGregorianCutover());
    }

    // Tests getInstanceUTC factory method
    @Test
    public void testGetInstanceUTC_valid() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        assertEquals(DateTimeZone.UTC, chrono.getZone());
        assertEquals(4, chrono.getMinimumDaysInFirstWeek());
        assertEquals(GJChronology.DEFAULT_CUTOVER, chrono.getGregorianCutover());
    }

    // Tests getInstance with custom cutover and minDaysInFirstWeek
    @Test
    public void testGetInstance_customCutoverAndMinDays() {
        Instant cutover = new Instant(0L);
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC, cutover, 3);
        assertEquals(DateTimeZone.UTC, chrono.getZone());
        assertEquals(3, chrono.getMinimumDaysInFirstWeek());
        assertEquals(cutover, chrono.getGregorianCutover());

        GJChronology chronoFromMillis = GJChronology.getInstance(DateTimeZone.UTC, 0L, 3);
        assertSame(chrono, chronoFromMillis);

        GJChronology defaultCutoverFromMillis = GJChronology.getInstance(
            DateTimeZone.UTC, GJChronology.DEFAULT_CUTOVER.getMillis(), 4);
        assertSame(GJChronology.getInstanceUTC(), defaultCutoverFromMillis);
    }

    // Tests withZone and withUTC conversions
    @Test
    public void testWithZoneAndWithUTC() {
        GJChronology chronoUTC = GJChronology.getInstanceUTC();
        assertSame(chronoUTC, chronoUTC.withUTC());
        assertSame(chronoUTC, chronoUTC.withZone(DateTimeZone.UTC));

        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        Chronology zonedChrono = chronoUTC.withZone(zone);
        assertEquals(zone, zonedChrono.getZone());
        assertSame(chronoUTC, zonedChrono.withUTC());

        Chronology defaultZoneChrono = chronoUTC.withZone(null);
        assertEquals(DateTimeZone.getDefault(), defaultZoneChrono.getZone());
    }

    // Tests equals and hashCode methods
    @Test
    public void testEqualsAndHashCode() {
        GJChronology chrono1 = GJChronology.getInstanceUTC();
        GJChronology chrono2 = GJChronology.getInstance(DateTimeZone.UTC, GJChronology.DEFAULT_CUTOVER, 4);
        GJChronology chrono3 = GJChronology.getInstance(DateTimeZone.UTC, new Instant(0L), 4);
        GJChronology chrono4 = GJChronology.getInstance(DateTimeZone.UTC, GJChronology.DEFAULT_CUTOVER, 3);

        assertTrue(chrono1.equals(chrono1));
        assertTrue(chrono1.equals(chrono2));
        assertEquals(chrono1.hashCode(), chrono2.hashCode());

        assertFalse(chrono1.equals(chrono3));
        assertFalse(chrono1.equals(chrono4));
        assertFalse(chrono1.equals(null));
        assertFalse(chrono1.equals("GJChronology"));
    }

    // Tests toString with default and custom cutovers
    @Test
    public void testToString_formatting() {
        GJChronology chronoUTC = GJChronology.getInstanceUTC();
        assertEquals("GJChronology[UTC]", chronoUTC.toString());

        GJChronology customChrono = GJChronology.getInstance(DateTimeZone.UTC, new Instant(0L), 3);
        assertTrue(customChrono.toString().contains("cutover="));
        assertTrue(customChrono.toString().contains("mdfw=3"));
    }

    // Tests illegal dates within the default cutover gap (1582-10-05 to 1582-10-14)
    @Test(expected = IllegalArgumentException.class)
    public void testGetDateTimeMillis_cutoverGapThrowsException() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        chrono.getDateTimeMillis(1582, 10, 5, 0);
    }

    // Tests valid dates immediately before and after the default cutover gap
    @Test
    public void testGetDateTimeMillis_aroundCutover() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long julianInstant = chrono.getDateTimeMillis(1582, 10, 4, 0, 0, 0, 0);
        long gregorianInstant = chrono.getDateTimeMillis(1582, 10, 15, 0, 0, 0, 0);
        assertEquals(GJChronology.DEFAULT_CUTOVER.getMillis(), gregorianInstant);
        assertEquals(24 * 60 * 60 * 1000L, gregorianInstant - julianInstant);

        long julianInstant4Arg = chrono.getDateTimeMillis(1582, 10, 4, 100);
        assertEquals(julianInstant + 100, julianInstant4Arg);
    }

    // Tests leap day handling in Julian era (1500 was a leap year in Julian, not Gregorian)
    @Test
    public void testGetDateTimeMillis_julianLeapYear() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long millis = chrono.getDateTimeMillis(1500, 2, 29, 12, 0, 0, 0);
        assertEquals(1500, chrono.year().get(millis));
        assertEquals(2, chrono.monthOfYear().get(millis));
        assertEquals(29, chrono.dayOfMonth().get(millis));
    }

    // Tests invalid leap day in non-leap Julian year throws exception
    @Test(expected = IllegalFieldValueException.class)
    public void testGetDateTimeMillis_invalidJulianLeapYear() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        chrono.getDateTimeMillis(1501, 2, 29, 0, 0, 0, 0);
    }

    // Tests addition of years across BCE and negative years (Defects4J Time-6 regression)
    @Test
    public void testYear_addNegativeAndBCEDates() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        LocalDate date = new LocalDate(-100, 1, 1, chrono);
        LocalDate result = date.plusYears(100);
        assertEquals(1, result.getYear());
        assertEquals(1, result.getMonthOfYear());
        assertEquals(1, result.getDayOfMonth());

        LocalDate date2 = new LocalDate(-2003, 5, 29, chrono);
        LocalDate result2 = date2.plusYears(100);
        assertEquals(-1903, result2.getYear());
        assertEquals(5, result2.getMonthOfYear());
        assertEquals(29, result2.getDayOfMonth());
    }

    // Tests cutover field text and duration methods
    @Test
    public void testCutoverFields_getAndText() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long postCutover = chrono.getDateTimeMillis(2020, 1, 15, 10, 30, 0, 0);
        long preCutover = chrono.getDateTimeMillis(1500, 1, 15, 10, 30, 0, 0);

        assertEquals("January", chrono.monthOfYear().getAsText(postCutover, Locale.ENGLISH));
        assertEquals("Jan", chrono.monthOfYear().getAsShortText(postCutover, Locale.ENGLISH));
        assertEquals("January", chrono.monthOfYear().getAsText(preCutover, Locale.ENGLISH));
        assertEquals("Jan", chrono.monthOfYear().getAsShortText(preCutover, Locale.ENGLISH));
        assertEquals("Wednesday", chrono.dayOfWeek().getAsText(postCutover, Locale.ENGLISH));

        assertTrue(chrono.monthOfYear().getMaximumTextLength(Locale.ENGLISH) > 0);
        assertTrue(chrono.monthOfYear().getMaximumShortTextLength(Locale.ENGLISH) > 0);
        assertNotNull(chrono.year().getDurationField());
        assertNotNull(chrono.year().getRangeDurationField());
        assertNotNull(chrono.dayOfMonth().getDurationField());
        assertNotNull(chrono.dayOfMonth().getRangeDurationField());
    }

    // Tests addition of months and weekyears across cutover
    @Test
    public void testImpreciseCutoverField_addMonthsAndWeekyears() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long instant = chrono.getDateTimeMillis(1582, 10, 1, 0, 0, 0, 0);
        long addedMonth = chrono.monthOfYear().add(instant, 1);
        assertEquals(1582, chrono.year().get(addedMonth));
        assertEquals(11, chrono.monthOfYear().get(addedMonth));
        assertEquals(1, chrono.dayOfMonth().get(addedMonth));

        long addedWeekyear = chrono.weekyear().add(instant, 1);
        assertEquals(1583, chrono.weekyear().get(addedWeekyear));
    }

    // Tests difference calculation in ImpreciseCutoverField
    @Test
    public void testImpreciseCutoverField_getDifference() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long start = chrono.getDateTimeMillis(1580, 1, 1, 0, 0, 0, 0);
        long end = chrono.getDateTimeMillis(1585, 1, 1, 0, 0, 0, 0);

        assertEquals(5, chrono.years().getDifference(end, start));
        assertEquals(5L, chrono.years().getDifferenceAsLong(end, start));
        assertEquals(-5, chrono.years().getDifference(start, end));

        assertEquals(60, chrono.months().getDifference(end, start));
        assertEquals(60L, chrono.months().getDifferenceAsLong(end, start));
    }

    // Tests CutoverField minimum and maximum values
    @Test
    public void testCutoverField_minMaxValues() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long cutoverYearInstant = chrono.getDateTimeMillis(1582, 10, 15, 0, 0, 0, 0);

        assertTrue(chrono.dayOfMonth().getMinimumValue(cutoverYearInstant) >= 1);
        assertTrue(chrono.dayOfMonth().getMaximumValue(cutoverYearInstant) <= 31);
        assertEquals(1, chrono.dayOfMonth().getMinimumValue());
        assertEquals(31, chrono.dayOfMonth().getMaximumValue());

        long preCutover = chrono.getDateTimeMillis(1500, 2, 1, 0, 0, 0, 0);
        assertEquals(29, chrono.dayOfMonth().getMaximumValue(preCutover));
    }

    // Tests CutoverField roundFloor and roundCeiling
    @Test
    public void testCutoverField_roundFloorAndCeiling() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long instant = chrono.getDateTimeMillis(1582, 10, 15, 12, 30, 0, 0);

        long floorDay = chrono.dayOfMonth().roundFloor(instant);
        assertEquals(chrono.getDateTimeMillis(1582, 10, 15, 0, 0, 0, 0), floorDay);

        long ceilingDay = chrono.dayOfMonth().roundCeiling(instant);
        assertEquals(chrono.getDateTimeMillis(1582, 10, 16, 0, 0, 0, 0), ceilingDay);

        long preCutoverInstant = chrono.getDateTimeMillis(1582, 10, 4, 12, 30, 0, 0);
        long floorPre = chrono.dayOfMonth().roundFloor(preCutoverInstant);
        assertEquals(chrono.getDateTimeMillis(1582, 10, 4, 0, 0, 0, 0), floorPre);
    }

    // Tests CutoverField set method transitions across cutover
    @Test
    public void testCutoverField_setValues() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long instant = chrono.getDateTimeMillis(1582, 10, 15, 0, 0, 0, 0);

        long newInstant = chrono.year().set(instant, 1580);
        assertEquals(1580, chrono.year().get(newInstant));
        assertEquals(10, chrono.monthOfYear().get(newInstant));
        assertEquals(15, chrono.dayOfMonth().get(newInstant));

        long preCutover = chrono.getDateTimeMillis(1582, 10, 4, 0, 0, 0, 0);
        long postCutoverFromSet = chrono.year().set(preCutover, 1584);
        assertEquals(1584, chrono.year().get(postCutoverFromSet));
    }

    // Tests CutoverField isLeap and getLeapAmount
    @Test
    public void testCutoverField_leapCalculations() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long leapJulian = chrono.getDateTimeMillis(1500, 2, 1, 0, 0, 0, 0);
        assertTrue(chrono.year().isLeap(leapJulian));
        assertEquals(1, chrono.year().getLeapAmount(leapJulian));

        long nonLeapGregorian = chrono.getDateTimeMillis(1700, 2, 1, 0, 0, 0, 0);
        assertFalse(chrono.year().isLeap(nonLeapGregorian));
        assertEquals(0, chrono.year().getLeapAmount(nonLeapGregorian));
        assertNotNull(chrono.year().getLeapDurationField());
    }

    // Tests partial addition on CutoverField
    @Test
    public void testCutoverField_addReadablePartial() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        LocalDate date = new LocalDate(2004, 2, 29, chrono);
        LocalDate addedMonths = date.plusMonths(48);
        assertEquals(2008, addedMonths.getYear());
        assertEquals(2, addedMonths.getMonthOfYear());
        assertEquals(29, addedMonths.getDayOfMonth());
    }
}