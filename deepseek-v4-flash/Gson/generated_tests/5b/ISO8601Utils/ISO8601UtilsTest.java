package com.google.gson.internal.bind.util;

import org.junit.Test;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import static org.junit.Assert.*;

public class ISO8601UtilsTest {

    private Date createDate(int year, int month, int day, int hour, int minute, int second, int millis, TimeZone tz) {
        Calendar cal = Calendar.getInstance(tz);
        cal.setLenient(false);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, millis);
        return cal.getTime();
    }

    // Tests basic format (no millis, UTC timezone)
    @Test
    public void testFormat_date_returnsCorrectString() {
        Date date = createDate(2012, 4, 23, 18, 25, 43, 0, TimeZone.getTimeZone("UTC"));
        String result = ISO8601Utils.format(date);
        assertEquals("2012-04-23T18:25:43Z", result);
    }

    // Tests format with milliseconds
    @Test
    public void testFormat_dateWithMillis_returnsCorrectString() {
        Date date = createDate(2012, 4, 23, 18, 25, 43, 511, TimeZone.getTimeZone("UTC"));
        String result = ISO8601Utils.format(date, true);
        assertEquals("2012-04-23T18:25:43.511Z", result);
    }

    // Tests format with non-UTC timezone
    @Test
    public void testFormat_dateWithTimeZone_returnsOffsetString() {
        TimeZone tz = TimeZone.getTimeZone("GMT+01:00");
        Date date = createDate(2012, 4, 23, 19, 25, 43, 0, tz);
        String result = ISO8601Utils.format(date, false, tz);
        assertEquals("2012-04-23T19:25:43+01:00", result);
    }

    // Tests parse with date only (no time)
    @Test
    public void testParse_dateOnly_returnsDate() throws Exception {
        Date expected = createDate(2012, 4, 23, 0, 0, 0, 0, TimeZone.getTimeZone("UTC"));
        Date result = ISO8601Utils.parse("2012-04-23", new ParsePosition(0));
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests parse with date and time (no millis, no timezone)
    @Test
    public void testParse_dateWithT_returnsDate() throws Exception {
        Date expected = createDate(2012, 4, 23, 18, 25, 43, 0, TimeZone.getTimeZone("UTC"));
        Date result = ISO8601Utils.parse("2012-04-23T18:25:43", new ParsePosition(0));
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests parse with milliseconds
    @Test
    public void testParse_dateWithMillis_returnsDate() throws Exception {
        Date expected = createDate(2012, 4, 23, 18, 25, 43, 511, TimeZone.getTimeZone("UTC"));
        Date result = ISO8601Utils.parse("2012-04-23T18:25:43.511", new ParsePosition(0));
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests parse with Zulu timezone
    @Test
    public void testParse_dateWithZulu_returnsUTC() throws Exception {
        Date expected = createDate(2012, 4, 23, 18, 25, 43, 0, TimeZone.getTimeZone("UTC"));
        Date result = ISO8601Utils.parse("2012-04-23T18:25:43Z", new ParsePosition(0));
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests parse with positive offset (with colon)
    @Test
    public void testParse_dateWithPositiveOffset_returnsDate() throws Exception {
        TimeZone tz = TimeZone.getTimeZone("GMT+01:00");
        Date expected = createDate(2012, 4, 23, 18, 25, 43, 0, tz);
        Date result = ISO8601Utils.parse("2012-04-23T18:25:43+01:00", new ParsePosition(0));
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests parse with negative offset
    @Test
    public void testParse_dateWithNegativeOffset_returnsDate() throws Exception {
        TimeZone tz = TimeZone.getTimeZone("GMT-05:30");
        Date expected = createDate(2012, 4, 23, 18, 25, 43, 0, tz);
        Date result = ISO8601Utils.parse("2012-04-23T18:25:43-05:30", new ParsePosition(0));
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests parse with offset without colon
    @Test
    public void testParse_dateWithOffsetNoColon_returnsDate() throws Exception {
        TimeZone tz = TimeZone.getTimeZone("GMT+0100");
        Date expected = createDate(2012, 4, 23, 18, 25, 43, 0, tz);
        Date result = ISO8601Utils.parse("2012-04-23T18:25:43+0100", new ParsePosition(0));
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests parse with offset with only hours (no minutes) - this detects Defects4J bug 5b
    @Test
    public void testParse_dateWithOffsetOnlyHours_returnsDate() throws Exception {
        TimeZone tz = TimeZone.getTimeZone("GMT+05");
        Date expected = createDate(2012, 4, 23, 18, 25, 43, 0, tz);
        Date result = ISO8601Utils.parse("2012-04-23T18:25:43+05", new ParsePosition(0));
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests parse with "+0000" offset -> UTC
    @Test
    public void testParse_dateWithUTCOffsetZero_returnsUTC() throws Exception {
        Date expected = createDate(2012, 4, 23, 18, 25, 43, 0, TimeZone.getTimeZone("UTC"));
        Date result = ISO8601Utils.parse("2012-04-23T18:25:43+0000", new ParsePosition(0));
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests leap second truncation (seconds = 60 -> truncated to 59)
    @Test
    public void testParse_dateWithLeapSecond_secondsTruncated() throws Exception {
        Date expected = createDate(2012, 6, 30, 23, 59, 59, 0, TimeZone.getTimeZone("UTC"));
        Date result = ISO8601Utils.parse("2012-06-30T23:59:60Z", new ParsePosition(0));
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests fractional seconds with one digit
    @Test
    public void testParse_dateWithFractionalSecondsOneDigit() throws Exception {
        Date expected = createDate(2012, 4, 23, 18, 25, 43, 100, TimeZone.getTimeZone("UTC"));
        Date result = ISO8601Utils.parse("2012-04-23T18:25:43.1Z", new ParsePosition(0));
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests fractional seconds with two digits
    @Test
    public void testParse_dateWithFractionalSecondsTwoDigits() throws Exception {
        Date expected = createDate(2012, 4, 23, 18, 25, 43, 120, TimeZone.getTimeZone("UTC"));
        Date result = ISO8601Utils.parse("2012-04-23T18:25:43.12Z", new ParsePosition(0));
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests fractional seconds with three digits
    @Test
    public void testParse_dateWithFractionalSecondsThreeDigits() throws Exception {
        Date expected = createDate(2012, 4, 23, 18, 25, 43, 123, TimeZone.getTimeZone("UTC"));
        Date result = ISO8601Utils.parse("2012-04-23T18:25:43.123Z", new ParsePosition(0));
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests invalid year (empty string)
    @Test(expected = ParseException.class)
    public void testParse_emptyString_throwsParseException() throws Exception {
        ISO8601Utils.parse("", new ParsePosition(0));
    }

    // Tests missing time zone indicator
    @Test(expected = ParseException.class)
    public void testParse_missingTimeZoneIndicator_throwsParseException() throws Exception {
        ISO8601Utils.parse("2012-04-23T18:25:43", new ParsePosition(0));
    }

    // Tests invalid time zone indicator character
    @Test(expected = ParseException.class)
    public void testParse_invalidTimeZoneIndicator_throwsParseException() throws Exception {
        ISO8601Utils.parse("2012-04-23T18:25:43X", new ParsePosition(0));
    }

    // Tests null date input for format method
    @Test(expected = NullPointerException.class)
    public void testFormat_nullDate_throwsNullPointerException() {
        ISO8601Utils.format(null);
    }
}