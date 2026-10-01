package org.joda.time.chrono;

import java.util.Locale;
import org.joda.time.Chronology;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationField;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.Instant;
import org.joda.time.YearMonthDay;
import org.junit.Test;

import static org.junit.Assert.*;

public class GJChronologyTest {

    // Tests Julian leap year before Gregorian cutover (e.g., year 1500 day 29 of Feb)
    @Test
    public void testGetDateTimeMillis_julianLeapYear_returnsCorrectMillis() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long millis = chrono.getDateTimeMillis(1500, 2, 29, 0);
        assertEquals(1500, chrono.year().get(millis));
        assertEquals(2, chrono.monthOfYear().get(millis));
        assertEquals(29, chrono.dayOfMonth().get(millis));
    }

    // Tests Julian leap year with full time fields
    @Test
    public void testGetDateTimeMillis_julianLeapYearFullTime_returnsCorrectMillis() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long millis = chrono.getDateTimeMillis(1500, 2, 29, 12, 30, 40, 500);
        assertEquals(1500, chrono.year().get(millis));
        assertEquals(2, chrono.monthOfYear().get(millis));
        assertEquals(29, chrono.dayOfMonth().get(millis));
        assertEquals(12, chrono.hourOfDay().get(millis));
        assertEquals(30, chrono.minuteOfHour().get(millis));
        assertEquals(40, chrono.secondOfMinute().get(millis));
        assertEquals(500, chrono.millisOfSecond().get(millis));
    }

    // Tests illegal date in the cutover gap (Oct 5-14, 1582 in default cutover)
    @Test(expected = IllegalArgumentException.class)
    public void testGetDateTimeMillis_cutoverGapDate_throwsException() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        chrono.getDateTimeMillis(1582, 10, 6, 0);
    }

    // Tests illegal cutover gap with full time fields
    @Test(expected = IllegalArgumentException.class)
    public void testGetDateTimeMillis_cutoverGapDateFullTime_throwsException() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        chrono.getDateTimeMillis(1582, 10, 6, 10, 0, 0, 0);
    }

    // Tests factory method getInstanceUTC and basic properties
    @Test
    public void testGetInstanceUTC_returnsCorrectInstance() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        assertEquals(DateTimeZone.UTC, chrono.getZone());
        assertEquals(GJChronology.DEFAULT_CUTOVER, chrono.getGregorianCutover());
        assertEquals(4, chrono.getMinimumDaysInFirstWeek());
    }

    // Tests factory method getInstance with default timezone
    @Test
    public void testGetInstance_defaultZone_returnsNonNull() {
        GJChronology chrono = GJChronology.getInstance();
        assertNotNull(chrono);
        assertEquals(DateTimeZone.getDefault(), chrono.getZone());
    }

    // Tests factory method getInstance with specific timezone
    @Test
    public void testGetInstance_specificZone_returnsCorrectZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        GJChronology chrono = GJChronology.getInstance(zone);
        assertEquals(zone, chrono.getZone());
    }

    // Tests factory method with custom cutover instant and minDaysInFirstWeek
    @Test
    public void testGetInstance_customCutoverAndMinDays_returnsCorrectInstance() {
        Instant cutover = new Instant(0L);
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC, cutover, 5);
        assertEquals(cutover, chrono.getGregorianCutover());
        assertEquals(5, chrono.getMinimumDaysInFirstWeek());
    }

    // Tests factory method with long cutover and caching
    @Test
    public void testGetInstance_longCutover_returnsCachedInstance() {
        long cutover = GJChronology.DEFAULT_CUTOVER.getMillis();
        GJChronology chrono1 = GJChronology.getInstance(DateTimeZone.UTC, cutover, 4);
        GJChronology chrono2 = GJChronology.getInstance(DateTimeZone.UTC, (Instant) null, 4);
        assertSame(chrono1, chrono2);
    }

    // Tests withZone and withUTC conversions
    @Test
    public void testWithZoneAndWithUTC_validTransitions() {
        GJChronology chronoUTC = GJChronology.getInstanceUTC();
        assertSame(chronoUTC, chronoUTC.withZone(DateTimeZone.UTC));
        assertSame(chronoUTC, chronoUTC.withUTC());

        DateTimeZone zone = DateTimeZone.forOffsetHours(-5);
        Chronology zonedChrono = chronoUTC.withZone(zone);
        assertEquals(zone, zonedChrono.getZone());
        assertSame(chronoUTC, zonedChrono.withUTC());
    }

    // Tests equality and hashCode contracts
    @Test
    public void testEqualsAndHashCode_sameAndDifferentParameters() {
        GJChronology chrono1 = GJChronology.getInstanceUTC();
        GJChronology chrono2 = GJChronology.getInstance(DateTimeZone.UTC, GJChronology.DEFAULT_CUTOVER, 4);
        GJChronology chrono3 = GJChronology.getInstance(DateTimeZone.forOffsetHours(1));

        assertEquals(chrono1, chrono2);
        assertEquals(chrono1.hashCode(), chrono2.hashCode());
        assertFalse(chrono1.equals(chrono3));
        assertFalse(chrono1.equals(null));
        assertFalse(chrono1.equals("NotAChronology"));
    }

    // Tests toString formatting with default and non-default cutover
    @Test
    public void testToString_formatsCorrectly() {
        GJChronology chronoDefault = GJChronology.getInstanceUTC();
        assertEquals("GJChronology[UTC]", chronoDefault.toString());

        Instant cutover = new Instant(0L);
        GJChronology chronoCustom = GJChronology.getInstance(DateTimeZone.UTC, cutover, 3);
        assertTrue(chronoCustom.toString().contains("cutover="));
        assertTrue(chronoCustom.toString().contains("mdfw=3"));
    }

    // Tests date addition and field calculations crossing cutover
    @Test
    public void testAddMonths_acrossCutover_calculatesCorrectly() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long start = chrono.getDateTimeMillis(1582, 10, 4, 0); // Julian date before cutover
        long result = chrono.monthOfYear().add(start, 1);
        assertEquals(1582, chrono.year().get(result));
        assertEquals(11, chrono.monthOfYear().get(result));
        assertEquals(4, chrono.dayOfMonth().get(result));
    }

    // Tests year field get, set, and difference across cutover
    @Test
    public void testYearField_getDifference_acrossCutover() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long instantJulian = chrono.getDateTimeMillis(1580, 1, 1, 0);
        long instantGregorian = chrono.getDateTimeMillis(1590, 1, 1, 0);

        int diff = chrono.year().getDifference(instantGregorian, instantJulian);
        assertEquals(10, diff);
        long diffLong = chrono.year().getDifferenceAsLong(instantJulian, instantGregorian);
        assertEquals(-10L, diffLong);
    }

    // Tests CutoverField getMinimumValue and getMaximumValue
    @Test
    public void testFieldMinMaxValues_aroundCutover() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long cutoverMonth = chrono.getDateTimeMillis(1582, 10, 15, 0);

        int maxDay = chrono.dayOfMonth().getMaximumValue(cutoverMonth);
        assertEquals(31, maxDay);

        int minDay = chrono.dayOfMonth().getMinimumValue(cutoverMonth);
        assertEquals(1, minDay);
    }

    // Tests partial date addition using YearMonthDay
    @Test
    public void testPartialAdd_contiguousFields_addsProperly() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        YearMonthDay ymd = new YearMonthDay(1582, 9, 30, chrono);
        YearMonthDay added = ymd.plusDays(10);
        assertEquals(1582, added.getYear());
        assertEquals(10, added.getMonthOfYear());
        // Julian Oct 4 followed by Gregorian Oct 15 -> 4 days in Sep + 6 days = Oct 20
        assertEquals(20, added.getDayOfMonth());
    }

    // Tests text representation methods of CutoverField
    @Test
    public void testFieldText_returnsValidText() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long instant = chrono.getDateTimeMillis(2000, 1, 1, 0);
        assertEquals("January", chrono.monthOfYear().getAsText(instant, Locale.ENGLISH));
        assertEquals("Jan", chrono.monthOfYear().getAsShortText(instant, Locale.ENGLISH));
        assertTrue(chrono.monthOfYear().getMaximumTextLength(Locale.ENGLISH) > 0);
        assertTrue(chrono.monthOfYear().getMaximumShortTextLength(Locale.ENGLISH) > 0);
    }

    // Tests leap year checks before and after cutover
    @Test
    public void testIsLeap_julianAndGregorianYears() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long julian1500 = chrono.getDateTimeMillis(1500, 1, 1, 0);
        long gregorian1900 = chrono.getDateTimeMillis(1900, 1, 1, 0);
        long gregorian2000 = chrono.getDateTimeMillis(2000, 1, 1, 0);

        assertTrue(chrono.year().isLeap(julian1500));
        assertFalse(chrono.year().isLeap(gregorian1900));
        assertTrue(chrono.year().isLeap(gregorian2000));
    }
}