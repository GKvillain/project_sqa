package org.joda.time.chrono;

import java.util.Locale;
import org.joda.time.Chronology;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.YearMonthDay;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GJChronologyTest {

    private DateTimeZone originalZone;

    @Before
    public void setUp() {
        originalZone = DateTimeZone.getDefault();
        DateTimeZone.setDefault(DateTimeZone.UTC);
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(originalZone);
    }

    // Tests factory method for UTC instance
    @Test
    public void testGetInstanceUTC_returnsUtcChronology() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        assertNotNull(chrono);
        assertEquals(DateTimeZone.UTC, chrono.getZone());
        assertEquals(GJChronology.DEFAULT_CUTOVER, chrono.getGregorianCutover());
        assertEquals(4, chrono.getMinimumDaysInFirstWeek());
    }

    // Tests factory method with default zone
    @Test
    public void testGetInstance_returnsDefaultZoneChronology() {
        GJChronology chrono = GJChronology.getInstance();
        assertNotNull(chrono);
        assertEquals(DateTimeZone.UTC, chrono.getZone());
        assertEquals(GJChronology.DEFAULT_CUTOVER, chrono.getGregorianCutover());
    }

    // Tests factory method with specific zone
    @Test
    public void testGetInstance_withZone_returnsConfiguredZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        GJChronology chrono = GJChronology.getInstance(zone);
        assertEquals(zone, chrono.getZone());
    }

    // Tests factory method with zone, cutover instant, and min days
    @Test
    public void testGetInstance_withCutoverAndMinDays_returnsCorrectInstance() {
        Instant cutover = new Instant(0L);
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC, cutover, 5);
        assertEquals(DateTimeZone.UTC, chrono.getZone());
        assertEquals(cutover, chrono.getGregorianCutover());
        assertEquals(5, chrono.getMinimumDaysInFirstWeek());
    }

    // Tests factory method with long cutover
    @Test
    public void testGetInstance_withLongCutover_returnsCorrectInstance() {
        long cutoverMillis = GJChronology.DEFAULT_CUTOVER.getMillis();
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC, cutoverMillis, 4);
        assertEquals(GJChronology.DEFAULT_CUTOVER, chrono.getGregorianCutover());
    }

    // Tests withUTC and withZone methods
    @Test
    public void testWithZone_variousInputs_returnsExpectedChronology() {
        GJChronology chronoUtc = GJChronology.getInstanceUTC();
        assertSame(chronoUtc, chronoUtc.withUTC());

        DateTimeZone zone = DateTimeZone.forOffsetHours(5);
        Chronology zonedChrono = chronoUtc.withZone(zone);
        assertEquals(zone, zonedChrono.getZone());

        Chronology nullZoneChrono = chronoUtc.withZone(null);
        assertEquals(DateTimeZone.getDefault(), nullZoneChrono.getZone());
    }

    // Tests getDateTimeMillis with 4 parameters in Gregorian era
    @Test
    public void testGetDateTimeMillis_gregorianDate_returnsCorrectMillis() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long millis = chrono.getDateTimeMillis(2020, 5, 10, 3600000);
        assertEquals(2020, chrono.year().get(millis));
        assertEquals(5, chrono.monthOfYear().get(millis));
        assertEquals(10, chrono.dayOfMonth().get(millis));
        assertEquals(3600000, chrono.millisOfDay().get(millis));
    }

    // Tests getDateTimeMillis with 4 parameters in Julian era
    @Test
    public void testGetDateTimeMillis_julianDate_returnsCorrectMillis() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long millis = chrono.getDateTimeMillis(1500, 2, 10, 0);
        assertEquals(1500, chrono.year().get(millis));
        assertEquals(2, chrono.monthOfYear().get(millis));
        assertEquals(10, chrono.dayOfMonth().get(millis));
    }

    // Tests getDateTimeMillis with invalid cutover date in 4-parameter method
    @Test(expected = IllegalArgumentException.class)
    public void testGetDateTimeMillis_cutoverGap4Params_throwsException() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        // 1582-10-05 to 1582-10-14 do not exist in default GJChronology
        chrono.getDateTimeMillis(1582, 10, 5, 0);
    }

    // Tests getDateTimeMillis with 7 parameters in Julian leap day
    @Test
    public void testGetDateTimeMillis_julianFeb29_returnsCorrectMillis() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long millis = chrono.getDateTimeMillis(1500, 2, 29, 12, 30, 15, 500);
        assertEquals(1500, chrono.year().get(millis));
        assertEquals(2, chrono.monthOfYear().get(millis));
        assertEquals(29, chrono.dayOfMonth().get(millis));
        assertEquals(12, chrono.hourOfDay().get(millis));
        assertEquals(30, chrono.minuteOfHour().get(millis));
    }

    // Tests getDateTimeMillis with invalid cutover date in 7-parameter method
    @Test(expected = IllegalArgumentException.class)
    public void testGetDateTimeMillis_cutoverGap7Params_throwsException() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        chrono.getDateTimeMillis(1582, 10, 10, 10, 0, 0, 0);
    }

    // Tests field leap behavior across Julian and Gregorian eras
    @Test
    public void testIsLeap_julianVsGregorianCenturyLeapYears() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long julian1500 = chrono.getDateTimeMillis(1500, 2, 28, 0);
        // In Julian, 1500 is a leap year
        assertTrue(chrono.year().isLeap(julian1500));

        long gregorian1700 = chrono.getDateTimeMillis(1700, 2, 28, 0);
        // In Gregorian, 1700 is not a leap year
        assertFalse(chrono.year().isLeap(gregorian1700));

        long gregorian2000 = chrono.getDateTimeMillis(2000, 2, 28, 0);
        // In Gregorian, 2000 is a leap year
        assertTrue(chrono.year().isLeap(gregorian2000));
    }

    // Tests adding years across cutover
    @Test
    public void testImpreciseCutoverField_addYearsAcrossCutover() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long instant = chrono.getDateTimeMillis(1580, 1, 1, 0);
        long result = chrono.years().add(instant, 10);
        assertEquals(1590, chrono.year().get(result));
        assertEquals(1, chrono.monthOfYear().get(result));
        assertEquals(1, chrono.dayOfMonth().get(result));
    }

    // Tests subtracting years across cutover
    @Test
    public void testImpreciseCutoverField_subtractYearsAcrossCutover() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long instant = chrono.getDateTimeMillis(1590, 1, 1, 0);
        long result = chrono.years().add(instant, -10);
        assertEquals(1580, chrono.year().get(result));
        assertEquals(1, chrono.monthOfYear().get(result));
        assertEquals(1, chrono.dayOfMonth().get(result));
    }

    // Tests adding months across cutover
    @Test
    public void testImpreciseCutoverField_addMonthsAcrossCutover() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long instant = chrono.getDateTimeMillis(1582, 9, 1, 0);
        long result = chrono.months().add(instant, 3);
        assertEquals(1582, chrono.year().get(result));
        assertEquals(12, chrono.monthOfYear().get(result));
        assertEquals(1, chrono.dayOfMonth().get(result));
    }

    // Tests getDifference for year field across cutover
    @Test
    public void testImpreciseCutoverField_getDifference() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long start = chrono.getDateTimeMillis(1580, 5, 1, 0);
        long end = chrono.getDateTimeMillis(1590, 5, 1, 0);
        assertEquals(10, chrono.years().getDifference(end, start));
        assertEquals(-10, chrono.years().getDifference(start, end));
        assertEquals(10L, chrono.years().getDifferenceAsLong(end, start));
    }

    // Tests setting date values across cutover
    @Test
    public void testCutoverField_setYearAcrossCutover() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long instant = chrono.getDateTimeMillis(1580, 10, 20, 0);
        long result = chrono.year().set(instant, 1590);
        assertEquals(1590, chrono.year().get(result));
        assertEquals(10, chrono.monthOfYear().get(result));
        assertEquals(20, chrono.dayOfMonth().get(result));
    }

    // Tests getMinimumValue and getMaximumValue for dayOfMonth around cutover
    @Test
    public void testCutoverField_getMinMaxValues() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long oct1582 = chrono.getDateTimeMillis(1582, 10, 15, 0);
        assertEquals(1, chrono.dayOfMonth().getMinimumValue(oct1582));
        assertEquals(31, chrono.dayOfMonth().getMaximumValue(oct1582));
        assertEquals(1, chrono.monthOfYear().getMinimumValue(oct1582));
        assertEquals(12, chrono.monthOfYear().getMaximumValue(oct1582));
    }

    // Tests text formatting and parsing methods
    @Test
    public void testCutoverField_getAsTextAndShortText() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long instant = chrono.getDateTimeMillis(2020, 5, 1, 0);
        assertEquals("May", chrono.monthOfYear().getAsText(instant, Locale.ENGLISH));
        assertEquals("May", chrono.monthOfYear().getAsShortText(instant, Locale.ENGLISH));
        assertEquals("Friday", chrono.dayOfWeek().getAsText(instant, Locale.ENGLISH));
    }

    // Tests roundFloor and roundCeiling across cutover
    @Test
    public void testCutoverField_roundFloorAndRoundCeiling() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long instant = chrono.getDateTimeMillis(1582, 10, 15, 12, 30, 0, 0);
        long floor = chrono.dayOfMonth().roundFloor(instant);
        assertEquals(chrono.getDateTimeMillis(1582, 10, 15, 0, 0, 0, 0), floor);

        long ceiling = chrono.dayOfMonth().roundCeiling(instant);
        assertEquals(chrono.getDateTimeMillis(1582, 10, 16, 0, 0, 0, 0), ceiling);
    }

    // Tests partial addition on contiguous partial
    @Test
    public void testCutoverField_addReadablePartial() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        YearMonthDay ymd = new YearMonthDay(1582, 10, 4, chrono);
        YearMonthDay result = ymd.plusDays(1);
        // After 1582-10-04 (Julian), next day is 1582-10-15 (Gregorian)
        assertEquals(1582, result.getYear());
        assertEquals(10, result.getMonthOfYear());
        assertEquals(15, result.getDayOfMonth());
    }

    // Tests negative years / BCE operations with LocalDate
    @Test
    public void testCutoverField_negativeYearHandling() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long instant = chrono.getDateTimeMillis(-10, 6, 15, 0);
        assertEquals(-10, chrono.year().get(instant));

        long added = chrono.years().add(instant, 20);
        assertEquals(10, chrono.year().get(added));
    }

    // Tests equals and hashCode contracts
    @Test
    public void testEqualsAndHashCode() {
        GJChronology chrono1 = GJChronology.getInstance(DateTimeZone.UTC, GJChronology.DEFAULT_CUTOVER, 4);
        GJChronology chrono2 = GJChronology.getInstanceUTC();
        GJChronology chrono3 = GJChronology.getInstance(DateTimeZone.UTC, new Instant(0L), 4);
        GJChronology chrono4 = GJChronology.getInstance(DateTimeZone.forOffsetHours(1), GJChronology.DEFAULT_CUTOVER, 4);

        assertEquals(chrono1, chrono2);
        assertEquals(chrono1.hashCode(), chrono2.hashCode());

        assertFalse(chrono1.equals(chrono3));
        assertFalse(chrono1.equals(chrono4));
        assertFalse(chrono1.equals("OtherType"));
        assertFalse(chrono1.equals(null));
    }

    // Tests toString representation
    @Test
    public void testToString_formatsCorrectly() {
        GJChronology defaultChrono = GJChronology.getInstanceUTC();
        assertEquals("GJChronology[UTC]", defaultChrono.toString());

        Instant cutover = new Instant(0L);
        GJChronology customChrono = GJChronology.getInstance(DateTimeZone.UTC, cutover, 3);
        assertTrue(customChrono.toString().contains("cutover="));
        assertTrue(customChrono.toString().contains("mdfw=3"));
    }
}