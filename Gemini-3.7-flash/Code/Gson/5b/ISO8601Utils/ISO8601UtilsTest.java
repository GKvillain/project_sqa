package com.google.gson.internal.bind.util;

import org.junit.Test;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ISO8601UtilsTest {

    // Tests formatting a date with default settings (UTC, without milliseconds)
    @Test
    public void testFormat_dateOnlyDefaultUtc_returnsFormattedUtcString() {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.JUNE, 15, 12, 30, 45);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();

        String formatted = ISO8601Utils.format(date);
        assertEquals("2023-06-15T12:30:45Z", formatted);
    }

    // Tests formatting a date including milliseconds
    @Test
    public void testFormat_withMillisTrue_returnsFormattedWithMillis() {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.JUNE, 15, 12, 30, 45);
        cal.set(Calendar.MILLISECOND, 123);
        Date date = cal.getTime();

        String formatted = ISO8601Utils.format(date, true);
        assertEquals("2023-06-15T12:30:45.123Z", formatted);
    }

    // Tests formatting a date with positive timezone offset
    @Test
    public void testFormat_withCustomPositiveTimezone_returnsFormattedWithOffset() {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.JUNE, 15, 12, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();

        TimeZone tz = TimeZone.getTimeZone("GMT+02:00");
        String formatted = ISO8601Utils.format(date, false, tz);
        assertEquals("2023-06-15T14:00:00+02:00", formatted);
    }

    // Tests formatting a date with negative timezone offset
    @Test
    public void testFormat_withCustomNegativeTimezone_returnsFormattedWithNegativeOffset() {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.JUNE, 15, 12, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();

        TimeZone tz = TimeZone.getTimeZone("GMT-05:00");
        String formatted = ISO8601Utils.format(date, false, tz);
        assertEquals("2023-06-15T07:00:00-05:00", formatted);
    }

    // Tests parsing date-only string without time component
    @Test
    public void testParse_dateOnlyWithoutHyphens_returnsParsedDate() throws ParseException {
        String dateStr = "20230615";
        ParsePosition pos = new ParsePosition(0);
        Date date = ISO8601Utils.parse(dateStr, pos);

        assertNotNull(date);
        assertEquals(8, pos.getIndex());

        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JUNE, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
    }

    // Tests parsing ISO8601 with Zulu (UTC) timezone
    @Test
    public void testParse_dateTimeUtcZ_returnsCorrectUtcDate() throws ParseException {
        String dateStr = "2023-06-15T12:30:45Z";
        ParsePosition pos = new ParsePosition(0);
        Date date = ISO8601Utils.parse(dateStr, pos);

        assertNotNull(date);
        assertEquals(dateStr.length(), pos.getIndex());

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTime(date);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JUNE, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(45, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    // Tests parsing ISO8601 with positive timezone offset with colon
    @Test
    public void testParse_positiveTimezoneOffsetWithColon_returnsCorrectDate() throws ParseException {
        String dateStr = "2023-06-15T14:30:45+02:00";
        ParsePosition pos = new ParsePosition(0);
        Date date = ISO8601Utils.parse(dateStr, pos);

        assertNotNull(date);
        assertEquals(dateStr.length(), pos.getIndex());

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTime(date);
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
    }

    // Tests parsing ISO8601 with positive timezone offset without colon
    @Test
    public void testParse_positiveTimezoneOffsetWithoutColon_returnsCorrectDate() throws ParseException {
        String dateStr = "2023-06-15T14:30:45+0200";
        ParsePosition pos = new ParsePosition(0);
        Date date = ISO8601Utils.parse(dateStr, pos);

        assertNotNull(date);
        assertEquals(dateStr.length(), pos.getIndex());

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTime(date);
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
    }

    // Tests parsing ISO8601 with special zero offsets (+00:00 and +0000)
    @Test
    public void testParse_zeroTimezoneOffsets_returnsCorrectUtcDate() throws ParseException {
        String dateStr1 = "2023-06-15T12:30:45+00:00";
        ParsePosition pos1 = new ParsePosition(0);
        Date date1 = ISO8601Utils.parse(dateStr1, pos1);

        String dateStr2 = "2023-06-15T12:30:45+0000";
        ParsePosition pos2 = new ParsePosition(0);
        Date date2 = ISO8601Utils.parse(dateStr2, pos2);

        assertEquals(date1.getTime(), date2.getTime());
    }

    // Tests parsing milliseconds with 1 digit fraction
    @Test
    public void testParse_fractionalSecondsOneDigit_compensatesToHundredMillis() throws ParseException {
        String dateStr = "2023-06-15T12:30:45.5Z";
        ParsePosition pos = new ParsePosition(0);
        Date date = ISO8601Utils.parse(dateStr, pos);

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTime(date);
        assertEquals(500, cal.get(Calendar.MILLISECOND));
    }

    // Tests parsing milliseconds with 2 digits fraction
    @Test
    public void testParse_fractionalSecondsTwoDigits_compensatesToTenMillis() throws ParseException {
        String dateStr = "2023-06-15T12:30:45.55Z";
        ParsePosition pos = new ParsePosition(0);
        Date date = ISO8601Utils.parse(dateStr, pos);

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTime(date);
        assertEquals(550, cal.get(Calendar.MILLISECOND));
    }

    // Tests parsing milliseconds with 3 digits fraction
    @Test
    public void testParse_fractionalSecondsThreeDigits_parsesExactMillis() throws ParseException {
        String dateStr = "2023-06-15T12:30:45.123Z";
        ParsePosition pos = new ParsePosition(0);
        Date date = ISO8601Utils.parse(dateStr, pos);

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTime(date);
        assertEquals(123, cal.get(Calendar.MILLISECOND));
    }

    // Tests leap second handling (seconds between 60 and 62 truncated to 59)
    @Test
    public void testParse_leapSeconds_truncatesToFiftyNineSeconds() throws ParseException {
        String dateStr = "2023-06-15T23:59:60Z";
        ParsePosition pos = new ParsePosition(0);
        Date date = ISO8601Utils.parse(dateStr, pos);

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTime(date);
        assertEquals(59, cal.get(Calendar.SECOND));
    }

    // Tests exception when time component is present but time zone is missing
    @Test(expected = ParseException.class)
    public void testParse_missingTimezoneIndicator_throwsParseException() throws ParseException {
        String dateStr = "2023-06-15T12:30:45";
        ParsePosition pos = new ParsePosition(0);
        ISO8601Utils.parse(dateStr, pos);
    }

    // Tests exception when invalid time zone indicator character is provided
    @Test(expected = ParseException.class)
    public void testParse_invalidTimezoneIndicator_throwsParseException() throws ParseException {
        String dateStr = "2023-06-15T12:30:45X";
        ParsePosition pos = new ParsePosition(0);
        ISO8601Utils.parse(dateStr, pos);
    }

    // Tests exception when non-numeric characters are in date field
    @Test(expected = ParseException.class)
    public void testParse_nonNumericCharacters_throwsParseException() throws ParseException {
        String dateStr = "202A-06-15T12:30:45Z";
        ParsePosition pos = new ParsePosition(0);
        ISO8601Utils.parse(dateStr, pos);
    }

    // Tests exception when input is null
    @Test(expected = ParseException.class)
    public void testParse_nullInput_throwsParseException() throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        ISO8601Utils.parse(null, pos);
    }
}