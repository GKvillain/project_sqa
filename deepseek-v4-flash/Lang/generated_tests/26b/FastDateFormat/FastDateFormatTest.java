package org.apache.commons.lang3.time;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

/**
 * JUnit 4 test class for FastDateFormat, targeting Defects4J bug Lang-26b.
 * Tests include normal formatting, time zone overrides, boundary cases, and exception paths.
 */
public class FastDateFormatTest {

    // --------------------------------------------------------------
    // Helper: create a SimpleDateFormat with the same pattern/timezone/locale for expected results
    private String expectedFormat(String pattern, Date date, TimeZone tz, Locale locale) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, locale);
        sdf.setTimeZone(tz);
        return sdf.format(date);
    }

    // --------------------------------------------------------------
    // Test getInstance() with default pattern
    @Test
    public void testGetInstance_defaultPattern_returnsFormatter() {
        FastDateFormat fdf = FastDateFormat.getInstance();
        assertNotNull("getInstance() should not return null", fdf);
        assertFalse(fdf.getPattern().isEmpty());
    }

    // Test getInstance(String) with valid pattern
    @Test
    public void testGetInstance_validPattern_returnsFormatter() {
        String pattern = "yyyy-MM-dd";
        FastDateFormat fdf = FastDateFormat.getInstance(pattern);
        assertEquals(pattern, fdf.getPattern());
    }

    // Test getInstance with null pattern throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetInstance_nullPattern_throwsException() {
        FastDateFormat.getInstance(null);
    }

    // Test getDateInstance normal case
    @Test
    public void testGetDateInstance_styleMedium_returnsFormatter() {
        FastDateFormat fdf = FastDateFormat.getDateInstance(FastDateFormat.MEDIUM, TimeZone.getTimeZone("GMT"), Locale.US);
        assertNotNull(fdf);
        Date now = new Date();
        String expected = expectedFormat("MMM d, yyyy", now, TimeZone.getTimeZone("GMT"), Locale.US);
        assertEquals(expected, fdf.format(now));
    }

    // Test getTimeInstance normal case
    @Test
    public void testGetTimeInstance_styleShort_returnsFormatter() {
        FastDateFormat fdf = FastDateFormat.getTimeInstance(FastDateFormat.SHORT, Locale.UK);
        assertNotNull(fdf);
        Date now = new Date();
        // Use default time zone of formatter (system default, which is not forced)
        String expected = new SimpleDateFormat("HH:mm", Locale.UK).format(now);
        assertEquals(expected, fdf.format(now));
    }

    // Test getDateTimeInstance normal case
    @Test
    public void testGetDateTimeInstance_styleMediumLong_returnsFormatter() {
        FastDateFormat fdf = FastDateFormat.getDateTimeInstance(FastDateFormat.MEDIUM, FastDateFormat.LONG,
                TimeZone.getTimeZone("America/New_York"), Locale.US);
        assertNotNull(fdf);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"), Locale.US);
        cal.set(2024, Calendar.JANUARY, 15, 14, 30, 0);
        cal.set(Calendar.MILLISECOND, 0);
        String expected = new SimpleDateFormat("MMM d, yyyy 'at' h:mm:ss a z", Locale.US).format(cal.getTime());
        assertEquals(expected, fdf.format(cal));
    }

    // --------------------------------------------------------------
    // Test format(Date) basic
    @Test
    public void testFormatDate_basicPattern_returnsCorrectString() {
        String pattern = "yyyy/MM/dd HH:mm:ss";
        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDateFormat fdf = FastDateFormat.getInstance(pattern, tz, Locale.US);
        Calendar cal = Calendar.getInstance(tz, Locale.US);
        cal.set(1970, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        assertEquals("1970/01/01 00:00:00", fdf.format(date));
    }

    // Test format(long)
    @Test
    public void testFormatLong_zeroMillis_returnsEpoch() {
        String pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDateFormat fdf = FastDateFormat.getInstance(pattern, tz, Locale.US);
        assertEquals("1970-01-01T00:00:00Z", fdf.format(0L));
    }

    // --------------------------------------------------------------
    // Test format(Calendar) when timeZoneForced = true (formatter has explicit time zone)
    @Test
    public void testFormatCalendar_timeZoneForced_calendarClonedAndTzChanged() {
        TimeZone formatterTz = TimeZone.getTimeZone("GMT+5");
        TimeZone calendarTz = TimeZone.getTimeZone("GMT-3");
        Locale locale = Locale.US;
        String pattern = "yyyy-MM-dd HH:mm z";
        FastDateFormat fdf = FastDateFormat.getInstance(pattern, formatterTz, locale);

        Calendar cal = Calendar.getInstance(calendarTz, locale);
        cal.set(2020, Calendar.JUNE, 15, 10, 30, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // Expected: formatter forces GMT+5, so output time should be converted to +05:00
        Calendar expectedCal = (Calendar) cal.clone();
        expectedCal.setTimeZone(formatterTz);
        String expected = new SimpleDateFormat(pattern, locale).format(expectedCal.getTime());

        String actual = fdf.format(cal);
        assertEquals("Calendar with forced time zone should be converted", expected, actual);
    }

    // Test format(Calendar) when timeZoneForced = false (formatter has no explicit time zone)
    @Test
    public void testFormatCalendar_timeZoneNotForced_usesCalendarTz() {
        // No time zone passed to getInstance -> mTimeZoneForced = false
        // The formatter will use default system time zone, but format(Calendar) should keep calendar's time zone
        TimeZone calendarTz = TimeZone.getTimeZone("America/Chicago");
        Locale locale = Locale.US;
        String pattern = "yyyy-MM-dd HH:mm z";
        FastDateFormat fdf = FastDateFormat.getInstance(pattern, (TimeZone) null, locale); // timeZoneForced = false

        Calendar cal = Calendar.getInstance(calendarTz, locale);
        cal.set(2020, Calendar.DECEMBER, 25, 8, 45, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // Expected: calendar's time zone should be used (not formatter's default)
        String expected = new SimpleDateFormat(pattern, locale).format(cal.getTime());

        String actual = fdf.format(cal);
        assertEquals("Calendar's time zone should be preserved when not forced", expected, actual);
    }

    // --------------------------------------------------------------
    // Test format(Calendar) with DST transition (covering TimeZoneNameRule branch)
    @Test
    public void testFormatCalendar_dstTransition_usesDaylightName() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        Locale locale = Locale.US;
        String pattern = "yyyy-MM-dd HH:mm:ss z";
        // Force time zone so we test TimeZoneNameRule with mTimeZoneForced = true
        FastDateFormat fdf = FastDateFormat.getInstance(pattern, tz, locale);

        // Date in DST (July)
        Calendar cal = Calendar.getInstance(tz, locale);
        cal.set(2023, Calendar.JULY, 4, 12, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);

        String expected = new SimpleDateFormat(pattern, locale).format(cal.getTime());
        String actual = fdf.format(cal);
        assertEquals("DST time zone name should appear", expected, actual);
    }

    // --------------------------------------------------------------
    // Test format(Object) with unknown type
    @Test(expected = IllegalArgumentException.class)
    public void testFormatObject_unknownType_throwsException() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy");
        fdf.format(new Object(), new StringBuffer(), null);
    }

    // Test format(Object) with null object
    @Test(expected = IllegalArgumentException.class)
    public void testFormatObject_nullObject_throwsException() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy");
        fdf.format(null, new StringBuffer(), null);
    }

    // Test parseObject always returns null
    @Test
    public void testParseObject_anyInput_returnsNull() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy");
        java.text.ParsePosition pos = new java.text.ParsePosition(0);
        assertNull("parseObject should always return null", fdf.parseObject("test", pos));
        assertEquals("ParsePosition index should be 0", 0, pos.getIndex());
        assertEquals("ParsePosition errorIndex should be 0", 0, pos.getErrorIndex());
    }

    // --------------------------------------------------------------
    // Test pattern with various tokens (edge cases)
    @Test
    public void testFormat_variousTokens_producesCorrectOutput() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        Locale locale = Locale.US;
        // Pattern using many tokens: year (4-digit), month (text long), day (2-digit), hour (24), minute, second,
        // millisecond, AM/PM, day of week, timezone offset (RFC822), literal
        String pattern = "yyyy-MMMM-dd HH:mm:ss.SSS a E z";
        FastDateFormat fdf = FastDateFormat.getInstance(pattern, tz, locale);

        Calendar cal = Calendar.getInstance(tz, locale);
        // Set to a specific date/time where all fields are non-zero
        cal.set(1999, Calendar.DECEMBER, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);

        String expected = new SimpleDateFormat(pattern, locale).format(cal.getTime());
        assertEquals("Complex pattern should match SimpleDateFormat", expected, fdf.format(cal));
    }

    // Test pattern with 'ZZ' (ISO8601 timezone with colon) – ensure it produces +00:00 instead of +0000
    @Test
    public void testFormat_ISO8601TimeZone_usesColon() {
        // 'ZZ' is non-standard, but FastDateFormat uses TimeZoneNumberRule.INSTANCE_COLON
        // Expected output for UTC: +00:00
        String pattern = "yyyy-MM-dd'T'HH:mm:ssZZ";
        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDateFormat fdf = FastDateFormat.getInstance(pattern, tz, Locale.US);
        Date date = new Date(0);
        assertEquals("1970-01-01T00:00:00+00:00", fdf.format(date));
    }

    // Test pattern with 'Z' (RFC822 timezone) – should produce +0000
    @Test
    public void testFormat_RFC822TimeZone_noColon() {
        String pattern = "yyyy-MM-dd'T'HH:mm:ssZ";
        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDateFormat fdf = FastDateFormat.getInstance(pattern, tz, Locale.US);
        Date date = new Date(0);
        assertEquals("1970-01-01T00:00:00+0000", fdf.format(date));
    }

    // --------------------------------------------------------------
    // Test boundary: two-digit year pattern 'yy'
    @Test
    public void testFormat_twoDigitYear_returnsLastTwoDigits() {
        String pattern = "yyMMdd";
        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDateFormat fdf = FastDateFormat.getInstance(pattern, tz, Locale.US);
        Calendar cal = Calendar.getInstance(tz, Locale.US);
        cal.set(2001, Calendar.JANUARY, 1);
        assertEquals("010101", fdf.format(cal.getTime()));
    }

    // Test boundary: unpadded month (pattern 'M') – month should be single digit when < 10
    @Test
    public void testFormat_unpaddedMonth_returnsSingleDigit() {
        FastDateFormat fdf = FastDateFormat.getInstance("M/d/yyyy", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.MARCH, 5); // March = 3
        assertEquals("3/5/2023", fdf.format(cal.getTime()));
    }

    // Test boundary: padded month (pattern 'MM') – always two digits
    @Test
    public void testFormat_twoDigitMonth_alwaysTwoDigits() {
        FastDateFormat fdf = FastDateFormat.getInstance("MM/dd/yyyy", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.NOVEMBER, 15); // November = 10+1=11
        assertEquals("11/15/2023", fdf.format(cal.getTime()));
    }

    // --------------------------------------------------------------
    // Test equals and hashCode consistency (regression)
    @Test
    public void testEquals_samePatternTimezoneLocale_areEqual() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat fdf2 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        assertTrue("Same parameters should be equal", fdf1.equals(fdf2));
        assertEquals("HashCode must be equal", fdf1.hashCode(), fdf2.hashCode());
    }

    // Test equals with different pattern
    @Test
    public void testEquals_differentPattern_notEqual() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("yyyy", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat fdf2 = FastDateFormat.getInstance("yy", TimeZone.getTimeZone("GMT"), Locale.US);
        assertFalse("Different patterns should not be equal", fdf1.equals(fdf2));
    }

    // Test toString returns pattern in brackets
    @Test
    public void testToString_returnsPatternInBrackets() {
        String pattern = "yyyyMMdd";
        FastDateFormat fdf = FastDateFormat.getInstance(pattern);
        assertEquals("FastDateFormat[" + pattern + "]", fdf.toString());
    }

    // --------------------------------------------------------------
    // Test format with twenty-four hour field (pattern 'k') – 1..24
    @Test
    public void testFormat_twentyFourHourField_midnightReturns24() {
        // Midnight (0) should become 24
        FastDateFormat fdf = FastDateFormat.getInstance("kk:mm", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 1, 0, 0);
        assertEquals("24:00", fdf.format(cal.getTime()));
    }

    // Test format with twelve hour field (pattern 'h') – 1..12, midnight becomes 12
    @Test
    public void testFormat_twelveHourField_midnightReturns12() {
        FastDateFormat fdf = FastDateFormat.getInstance("hh:mm a", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 1, 0, 0);
        assertEquals("12:00 AM", fdf.format(cal.getTime()));
    }
}