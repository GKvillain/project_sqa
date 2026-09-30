package org.apache.commons.lang.time;

import static org.junit.Assert.*;

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

public class FastDateFormatTest {

    // Tests basic date formatting with default pattern
    @Test
    public void testGetInstance_defaultInstance_returnsNonNull() {
        FastDateFormat format = FastDateFormat.getInstance();
        assertNotNull(format);
    }

    // Tests formatting a date with a standard pattern
    @Test
    public void testFormat_dateWithPattern_returnsFormattedString() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd");
        Date date = new Date(1000000000000L);
        assertEquals("2001-09-09", format.format(date));
    }

    // Tests formatting with a Calendar
    @Test
    public void testFormat_calendarWithPattern_returnsFormattedString() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd");
        Calendar calendar = new GregorianCalendar(2020, Calendar.JANUARY, 15);
        assertEquals("2020-01-15", format.format(calendar));
    }

    // Tests formatting with a long millisecond value
    @Test
    public void testFormat_longValueWithPattern_returnsFormattedString() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy");
        assertEquals("2001", format.format(1000000000000L));
    }

    // Tests parsing returns null (unsupported operation)
    @Test
    public void testParseObject_validInput_returnsNull() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy");
        ParsePosition pos = new ParsePosition(0);
        assertNull(format.parseObject("2020", pos));
        assertEquals(0, pos.getIndex());
        assertEquals(0, pos.getErrorIndex());
    }

    // Tests getPattern returns the pattern used
    @Test
    public void testGetPattern_afterCreation_returnsPattern() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy/MM/dd");
        assertEquals("yyyy/MM/dd", format.getPattern());
    }

    // Tests getTimeZone returns the time zone
    @Test
    public void testGetTimeZone_createdWithTimeZone_returnsTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("GMT+08:00");
        FastDateFormat format = FastDateFormat.getInstance("yyyy", tz);
        assertEquals(tz, format.getTimeZone());
    }

    // Tests getLocale returns the locale
    @Test
    public void testGetLocale_createdWithLocale_returnsLocale() {
        Locale locale = Locale.US;
        FastDateFormat format = FastDateFormat.getInstance("yyyy", null, locale);
        assertEquals(locale, format.getLocale());
    }

    // Tests getMaxLengthEstimate returns a positive value
    @Test
    public void testGetMaxLengthEstimate_simplePattern_returnsPositiveValue() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy");
        assertTrue(format.getMaxLengthEstimate() > 0);
    }

    // Tests equals with the same attributes
    @Test
    public void testEquals_samePatternTimezoneLocale_returnsTrue() {
        FastDateFormat format1 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getDefault(), Locale.getDefault());
        FastDateFormat format2 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getDefault(), Locale.getDefault());
        assertEquals(format1, format2);
    }

    // Tests equals with different pattern
    @Test
    public void testEquals_differentPattern_returnsFalse() {
        FastDateFormat format1 = FastDateFormat.getInstance("yyyy");
        FastDateFormat format2 = FastDateFormat.getInstance("MM");
        assertNotEquals(format1, format2);
    }

    // Tests equals with non-FastDateFormat object
    @Test
    public void testEquals_nonFastDateFormat_returnsFalse() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy");
        assertFalse(format.equals("some string"));
    }

    // Tests hashCode is consistent with equals
    @Test
    public void testHashCode_sameAttributes_returnsSameHashCode() {
        FastDateFormat format1 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getDefault(), Locale.getDefault());
        FastDateFormat format2 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getDefault(), Locale.getDefault());
        assertEquals(format1.hashCode(), format2.hashCode());
    }

    // Tests toString returns a string containing the pattern
    @Test
    public void testToString_containsPattern() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy");
        String toStringValue = format.toString();
        assertTrue(toStringValue.contains("yyyy"));
    }

    // Tests getDateInstance with style
    @Test
    public void testGetDateInstance_mediumStyle_returnsNonNull() {
        FastDateFormat format = FastDateFormat.getDateInstance(FastDateFormat.MEDIUM);
        assertNotNull(format);
    }

    // Tests getTimeInstance with style
    @Test
    public void testGetTimeInstance_shortStyle_returnsNonNull() {
        FastDateFormat format = FastDateFormat.getTimeInstance(FastDateFormat.SHORT);
        assertNotNull(format);
    }

    // Tests getDateTimeInstance with styles
    @Test
    public void testGetDateTimeInstance_bothStyles_returnsNonNull() {
        FastDateFormat format = FastDateFormat.getDateTimeInstance(FastDateFormat.LONG, FastDateFormat.MEDIUM);
        assertNotNull(format);
    }

    // Tests formatting with a literal text pattern
    @Test
    public void testFormat_literalTextPattern_returnsLiteralText() {
        FastDateFormat format = FastDateFormat.getInstance("'Year:' yyyy");
        assertEquals("Year: 2001", format.format(1000000000000L));
    }

    // Tests formatting with hour fields to cover TwelveHourField and TwentyFourHourField
    @Test
    public void testFormat_hourPatterns_returnsFormattedHour() {
        FastDateFormat format12 = FastDateFormat.getInstance("h");
        FastDateFormat format24 = FastDateFormat.getInstance("k");
        Calendar cal = new GregorianCalendar(2020, Calendar.JANUARY, 15, 0, 0, 0);
        assertEquals("12", format12.format(cal.getTime()));
        assertEquals("24", format24.format(cal.getTime()));
    }

    // Tests format with time zone number rule
    @Test
    public void testFormat_timeZoneNumberRule_returnsFormattedTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("GMT+05:30");
        FastDateFormat format = FastDateFormat.getInstance("Z", tz);
        Calendar cal = new GregorianCalendar(tz);
        cal.set(2020, Calendar.JANUARY, 15, 10, 30, 0);
        assertEquals("+0530", format.format(cal));
    }

    // Tests format with time zone number rule with colon
    @Test
    public void testFormat_timeZoneNumberRuleWithColon_returnsFormattedTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("GMT+05:30");
        FastDateFormat format = FastDateFormat.getInstance("ZZ", tz);
        Calendar cal = new GregorianCalendar(tz);
        cal.set(2020, Calendar.JANUARY, 15, 10, 30, 0);
        assertEquals("+05:30", format.format(cal));
    }

    // Tests format with null object throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFormat_nullObject_throwsIllegalArgumentException() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy");
        format.format((Object) null, new StringBuffer(), new FieldPosition(0));
    }

    // Tests format with invalid object type throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFormat_invalidObjectType_throwsIllegalArgumentException() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy");
        format.format(new Object(), new StringBuffer(), new FieldPosition(0));
    }

    // Tests constructor with null pattern throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetInstance_nullPattern_throwsIllegalArgumentException() {
        FastDateFormat.getInstance(null);
    }

    // Tests formatting with time zone override on calendar
    @Test
    public void testFormat_calendarWithTimeZoneOverride_usesFormattedTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("GMT+02:00");
        FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", tz);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+02:00"));
        cal.set(2020, Calendar.JANUARY, 15, 10, 30, 0);
        assertTrue(format.getTimeZoneOverridesCalendar());
        assertEquals("2020-01-15 10:30:00", format.format(cal));
    }

    // Tests getTimeZoneOverridesCalendar returns true when time zone is forced
    @Test
    public void testGetTimeZoneOverridesCalendar_withTimeZone_returnsTrue() {
        TimeZone tz = TimeZone.getTimeZone("GMT+08:00");
        FastDateFormat format = FastDateFormat.getInstance("yyyy", tz);
        assertTrue(format.getTimeZoneOverridesCalendar());
    }

    // Tests getTimeZoneOverridesCalendar returns false when no time zone is forced
    @Test
    public void testGetTimeZoneOverridesCalendar_withoutTimeZone_returnsFalse() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy");
        assertFalse(format.getTimeZoneOverridesCalendar());
    }

    // Tests format with a Calendar when time zone is not forced, uses calendar's time zone
    @Test
    public void testFormat_calendarWithoutTimeZoneForced_usesCalendarTimeZone() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd");
        TimeZone tz = TimeZone.getTimeZone("GMT+00:00");
        Calendar cal = new GregorianCalendar(tz);
        cal.set(2020, Calendar.JANUARY, 15);
        assertEquals("2020-01-15", format.format(cal));
    }

    // ========== New test cases for uncovered coverage ==========

    // Tests time zone name rule (z pattern)
    @Test
    public void testFormat_timeZoneNameRule_returnsFormattedTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("GMT+05:30");
        FastDateFormat format = FastDateFormat.getInstance("z", tz);
        Calendar cal = new GregorianCalendar(tz);
        cal.set(2020, Calendar.JANUARY, 15, 10, 30, 0);
        String result = format.format(cal);
        assertNotNull(result);
        // Should contain something like "IST" or "GMT+05:30" depending on locale
        assertTrue(result.contains("GMT") || result.contains("IST") || result.contains("+05:30"));
    }

    // Tests ISO time zone (X pattern) - single X without colon
    @Test
    public void testFormat_isoTimeZone_returnsFormattedTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("GMT+05:30");
        FastDateFormat format = FastDateFormat.getInstance("X", tz);
        Calendar cal = new GregorianCalendar(tz);
        cal.set(2020, Calendar.JANUARY, 15, 10, 30, 0);
        assertEquals("+0530", format.format(cal));
    }

    // Tests ISO time zone with colon (XX pattern)
    @Test
    public void testFormat_isoTimeZoneWithColon_returnsFormattedTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("GMT+05:30");
        FastDateFormat format = FastDateFormat.getInstance("XX", tz);
        Calendar cal = new GregorianCalendar(tz);
        cal.set(2020, Calendar.JANUARY, 15, 10, 30, 0);
        assertEquals("+05:30", format.format(cal));
    }

    // Tests era (G) pattern
    @Test
    public void testFormat_era_returnsEra() {
        FastDateFormat format = FastDateFormat.getInstance("G yyyy");
        Calendar cal = new GregorianCalendar(2020, Calendar.JANUARY, 1);
        assertEquals("AD 2020", format.format(cal));
    }

    // Tests AM/PM (a) pattern
    @Test
    public void testFormat_amPm_returnsAmPm() {
        FastDateFormat format = FastDateFormat.getInstance("hh:mm a");
        Calendar cal = new GregorianCalendar(2020, Calendar.JANUARY, 15, 0, 0, 0);
        assertEquals("12:00 AM", format.format(cal));
        cal.set(Calendar.HOUR_OF_DAY, 12);
        assertEquals("12:00 PM", format.format(cal));
    }

    // Tests day of year (D) pattern
    @Test
    public void testFormat_dayOfYear_returnsDayOfYear() {
        FastDateFormat format = FastDateFormat.getInstance("D yyyy");
        Calendar cal = new GregorianCalendar(2020, Calendar.JANUARY, 15);
        String result = format.format(cal);
        assertTrue(result.startsWith("15"));
        assertEquals(7, result.length()); // "15 2020"
    }

    // Tests week of year (w) pattern
    @Test
    public void testFormat_weekOfYear_returnsWeekOfYear() {
        FastDateFormat format = FastDateFormat.getInstance("w yyyy");
        Calendar cal = new GregorianCalendar(2020, Calendar.JANUARY, 15);
        String result = format.format(cal);
        // January 15, 2020 is in week 3 of ISO week? At least it should be a number
        assertTrue(result.matches("\\d+ 2020"));
    }

    // Tests day of week in month (F) pattern
    @Test
    public void testFormat_dayOfWeekInMonth_returnsDayOfWeekInMonth() {
        FastDateFormat format = FastDateFormat.getInstance("F yyyy-MM");
        Calendar cal = new GregorianCalendar(2020, Calendar.JANUARY, 15);
        String result = format.format(cal);
        // January 15, 2020 is the 3rd Wednesday of the month -> "3"
        assertTrue(result.startsWith("3"));
    }

    // Tests literal with escaped single quote
    @Test
    public void testFormat_literalWithSingleQuote_returnsLiteralWithQuote() {
        FastDateFormat format = FastDateFormat.getInstance("'It''s' yyyy");
        assertEquals("It's 2001", format.format(1000000000000L));
    }

    // Tests month name with locale
    @Test
    public void testFormat_monthNameLocale_returnsMonthName() {
        FastDateFormat format = FastDateFormat.getInstance("MMMM yyyy", Locale.US);
        Calendar cal = new GregorianCalendar(2020, Calendar.JANUARY, 15);
        assertEquals("January 2020", format.format(cal));

        FastDateFormat formatFR = FastDateFormat.getInstance("MMMM yyyy", Locale.FRENCH);
        assertEquals("janvier 2020", formatFR.format(cal));
    }

    // Tests getMaxLengthEstimate with a longer pattern
    @Test
    public void testGetMaxLengthEstimate_longPattern_returnsLargerValue() {
        FastDateFormat shortFormat = FastDateFormat.getInstance("yyyy");
        FastDateFormat longFormat = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        assertTrue(longFormat.getMaxLengthEstimate() > shortFormat.getMaxLengthEstimate());
    }

    // Tests equals with null object
    @Test
    public void testEquals_nullObject_returnsFalse() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy");
        assertFalse(format.equals(null));
    }

    // Tests hashCode consistency when timezone and locale are null
    @Test
    public void testHashCode_nullTimezoneAndLocale_returnsConsistent() {
        FastDateFormat format1 = FastDateFormat.getInstance("yyyy", null, null);
        FastDateFormat format2 = FastDateFormat.getInstance("yyyy", null, null);
        assertEquals(format1.hashCode(), format2.hashCode());
    }

    // Tests format with null Date argument
    @Test(expected = NullPointerException.class)
    public void testFormat_nullDate_throwsNullPointerException() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy");
        format.format((Date) null);
    }

    // Tests format with null Calendar argument
    @Test(expected = NullPointerException.class)
    public void testFormat_nullCalendar_throwsNullPointerException() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy");
        format.format((Calendar) null);
    }

    // Tests format with null StringBuffer
    @Test(expected = NullPointerException.class)
    public void testFormat_nullStringBuffer_throwsNullPointerException() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy");
        format.format(new Date(), null, new FieldPosition(0));
    }

    // Tests format with null FieldPosition
    @Test(expected = NullPointerException.class)
    public void testFormat_nullFieldPosition_throwsNullPointerException() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy");
        format.format(new Date(), new StringBuffer(), null);
    }

    // Tests format with Calendar using a different time zone that overrides the calendar's zone
    @Test
    public void testFormat_calendarWithDifferentTimeZone_usesOverrideTimeZone() {
        TimeZone tzFormat = TimeZone.getTimeZone("GMT+02:00");
        TimeZone tzCal = TimeZone.getTimeZone("GMT-05:00");
        FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", tzFormat);
        Calendar cal = new GregorianCalendar(tzCal);
        cal.set(2020, Calendar.JANUARY, 15, 10, 30, 0); // 10:30 EST
        // Expected: 10:30 EST = 15:30 UTC = 17:30 GMT+2
        assertEquals("2020-01-15 17:30:00", format.format(cal));
    }

    // Tests format with Calendar without time zone forced, but calendar has a non-default zone
    @Test
    public void testFormat_calendarWithoutTimeZoneForced_differentTimeZone_usesCalendarTimeZone() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
        TimeZone tzCal = TimeZone.getTimeZone("GMT-05:00");
        Calendar cal = new GregorianCalendar(tzCal);
        cal.set(2020, Calendar.JANUARY, 15, 10, 30, 0);
        // Since no time zone override, calendar's zone (GMT-5) is used
        assertEquals("2020-01-15 10:30:00", format.format(cal));
    }

    // Tests format(Calendar, StringBuffer, FieldPosition) method
    @Test
    public void testFormat_calendarWithBufferAndFieldPosition_returnsFormatted() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd");
        Calendar cal = new GregorianCalendar(2020, Calendar.JANUARY, 15);
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        StringBuffer result = format.format(cal, buf, pos);
        assertEquals("2020-01-15", result.toString());
        assertSame(buf, result);
    }

    // Tests getDateInstance with FULL style and locale
    @Test
    public void testGetDateInstance_fullStyleLocale_returnsNonNull() {
        FastDateFormat format = FastDateFormat.getDateInstance(FastDateFormat.FULL, Locale.US);
        assertNotNull(format);
        String pattern = format.getPattern();
        assertTrue(pattern.contains("MMMM") || pattern.contains("yyyy"));
    }

    // Tests getTimeInstance with style and time zone
    @Test
    public void testGetTimeInstance_shortStyleUTC_returnsNonNull() {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        FastDateFormat format = FastDateFormat.getTimeInstance(FastDateFormat.SHORT, utc);
        assertNotNull(format);
        String formatted = format.format(new Date());
        assertNotNull(formatted);
    }

    // Tests getDateTimeInstance with styles, time zone and locale
    @Test
    public void testGetDateTimeInstance_stylesTimezoneLocale() {
        FastDateFormat format = FastDateFormat.getDateTimeInstance(
                FastDateFormat.LONG, FastDateFormat.SHORT,
                TimeZone.getDefault(), Locale.UK);
        assertNotNull(format);
        String formatted = format.format(new Date());
        assertNotNull(formatted);
    }
}