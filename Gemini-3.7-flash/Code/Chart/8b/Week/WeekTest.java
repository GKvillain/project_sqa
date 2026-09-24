package org.jfree.data.time;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class WeekTest {

    // Tests default constructor creating non-null instance
    @Test
    public void testConstructor_default_createsValidWeek() {
        Week week = new Week();
        assertNotNull(week);
        assertTrue(week.getWeek() >= 1 && week.getWeek() <= 53);
    }

    // Tests week and year int constructor with valid values
    @Test
    public void testConstructor_intWeekAndYear_setsCorrectValues() {
        Week week = new Week(25, 2023);
        assertEquals(25, week.getWeek());
        assertEquals(2023, week.getYearValue());
        assertEquals(new Year(2023), week.getYear());
    }

    // Tests week and Year object constructor
    @Test
    public void testConstructor_weekAndYearObject_setsCorrectValues() {
        Year year = new Year(2020);
        Week week = new Week(10, year);
        assertEquals(10, week.getWeek());
        assertEquals(2020, week.getYearValue());
    }

    // Tests constructor with Date, TimeZone and Locale
    @Test
    public void testConstructor_dateTimeZoneLocale_calculatesWeekCorrectly() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 10);
        Date time = cal.getTime();

        Week week = new Week(time, TimeZone.getTimeZone("UTC"), Locale.UK);
        assertEquals(2023, week.getYearValue());
        assertEquals(2, week.getWeek());
    }

    // Tests deprecated Date and TimeZone constructor
    @Test
    public void testConstructor_dateTimeZone_createsWeek() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.clear();
        cal.set(2022, Calendar.JUNE, 15);
        Date time = cal.getTime();

        Week week = new Week(time, TimeZone.getTimeZone("GMT"));
        assertEquals(2022, week.getYearValue());
    }

    // Tests constructor exception on null Date
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullDate_throwsException() {
        new Week(null, TimeZone.getDefault(), Locale.getDefault());
    }

    // Tests constructor exception on null TimeZone
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullZone_throwsException() {
        new Week(new Date(), null, Locale.getDefault());
    }

    // Tests constructor exception on null Locale
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullLocale_throwsException() {
        new Week(new Date(), TimeZone.getDefault(), null);
    }

    // Tests week boundary at end of year mapping to week 1 of next year
    @Test
    public void testConstructor_endOfYearWeek1_mapsToNextYear() {
        Calendar cal = Calendar.getInstance(Locale.UK);
        cal.clear();
        cal.set(Calendar.YEAR, 2014);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 30);
        Date time = cal.getTime();

        Week week = new Week(time, TimeZone.getDefault(), Locale.UK);
        assertEquals(1, week.getWeek());
        assertEquals(2015, week.getYearValue());
    }

    // Tests getSerialIndex calculation
    @Test
    public void testGetSerialIndex_standardValues_returnsCorrectIndex() {
        Week week = new Week(5, 2020);
        long expectedIndex = 2020 * 53L + 5;
        assertEquals(expectedIndex, week.getSerialIndex());
    }

    // Tests previous() in middle of year
    @Test
    public void testPrevious_midYear_returnsPreviousWeek() {
        Week week = new Week(20, 2023);
        RegularTimePeriod prev = week.previous();
        assertNotNull(prev);
        assertEquals(new Week(19, 2023), prev);
    }

    // Tests previous() at first week of year
    @Test
    public void testPrevious_firstWeekOfYear_returnsLastWeekOfPrevYear() {
        Week week = new Week(1, 2023);
        RegularTimePeriod prev = week.previous();
        assertNotNull(prev);
        assertEquals(2022, ((Week) prev).getYearValue());
    }

    // Tests previous() at lower limit 1900
    @Test
    public void testPrevious_lowerLimit1900_returnsNull() {
        Week week = new Week(1, 1900);
        assertNull(week.previous());
    }

    // Tests next() in middle of year
    @Test
    public void testNext_midYear_returnsNextWeek() {
        Week week = new Week(20, 2023);
        RegularTimePeriod next = week.next();
        assertNotNull(next);
        assertEquals(new Week(21, 2023), next);
    }

    // Tests next() at upper boundary year 9999
    @Test
    public void testNext_upperBoundary9999_returnsNull() {
        Week week = new Week(53, 9999);
        assertNull(week.next());
    }

    // Tests getFirstMillisecond and getLastMillisecond with Calendar
    @Test
    public void testGetFirstAndLastMillisecond_withCalendar_returnsValidRange() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
        Week week = new Week(15, 2023);
        long start = week.getFirstMillisecond(cal);
        long end = week.getLastMillisecond(cal);
        assertTrue(start < end);
        assertEquals(end - start, (7L * 24 * 60 * 60 * 1000) - 1);
    }

    // Tests peg method updating cached milliseconds
    @Test
    public void testPeg_updatesFirstAndLastMilliseconds() {
        Week week = new Week(1, 2023);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.UK);
        week.peg(cal);
        assertEquals(week.getFirstMillisecond(cal), week.getFirstMillisecond());
        assertEquals(week.getLastMillisecond(cal), week.getLastMillisecond());
    }

    // Tests toString format
    @Test
    public void testToString_validWeek_returnsExpectedString() {
        Week week = new Week(9, 2002);
        assertEquals("Week 9, 2002", week.toString());
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_sameValues_returnsTrueAndConsistentHashCode() {
        Week w1 = new Week(12, 2021);
        Week w2 = new Week(12, 2021);
        Week w3 = new Week(13, 2021);
        Week w4 = new Week(12, 2022);

        assertTrue(w1.equals(w1));
        assertTrue(w1.equals(w2));
        assertFalse(w1.equals(w3));
        assertFalse(w1.equals(w4));
        assertFalse(w1.equals(null));
        assertFalse(w1.equals("Other"));
        assertEquals(w1.hashCode(), w2.hashCode());
    }

    // Tests compareTo ordering
    @Test
    public void testCompareTo_variousWeeks_returnsCorrectOrder() {
        Week w1 = new Week(10, 2020);
        Week w2 = new Week(10, 2020);
        Week w3 = new Week(15, 2020);
        Week w4 = new Week(5, 2021);

        assertEquals(0, w1.compareTo(w2));
        assertTrue(w1.compareTo(w3) < 0);
        assertTrue(w3.compareTo(w1) > 0);
        assertTrue(w1.compareTo(w4) < 0);
        assertTrue(w4.compareTo(w1) > 0);
        assertTrue(w1.compareTo(new Object()) > 0);
    }

    // Tests parseWeek with valid string formats
    @Test
    public void testParseWeek_validFormats_returnsParsedWeek() {
        Week parsed1 = Week.parseWeek("2023-W05");
        assertNotNull(parsed1);
        assertEquals(5, parsed1.getWeek());
        assertEquals(2023, parsed1.getYearValue());

        Week parsed2 = Week.parseWeek("W12-2021");
        assertNotNull(parsed2);
        assertEquals(12, parsed2.getWeek());
        assertEquals(2021, parsed2.getYearValue());

        assertNull(Week.parseWeek(null));
    }

    // Tests parseWeek invalid string without separator
    @Test(expected = TimePeriodFormatException.class)
    public void testParseWeek_noSeparator_throwsException() {
        Week.parseWeek("202305");
    }

    // Tests parseWeek invalid string with invalid week
    @Test(expected = TimePeriodFormatException.class)
    public void testParseWeek_invalidWeek_throwsException() {
        Week.parseWeek("2023-W99");
    }
}