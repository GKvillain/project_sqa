package com.fasterxml.jackson.databind.util;

import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class StdDateFormatTest {

    private StdDateFormat stdDateFormat;

    @Before
    public void setUp() {
        stdDateFormat = new StdDateFormat();
    }

    // Tests default instance and default time zone
    @Test
    public void testGetDefaultTimeZone_returnsUTC() {
        TimeZone tz = StdDateFormat.getDefaultTimeZone();
        assertEquals("UTC", tz.getID());
        assertTrue(stdDateFormat.isLenient());
    }

    // Tests formatting date to ISO8601 string
    @Test
    public void testFormat_validDate_returnsISO8601String() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1, 12, 30, 45);
        cal.set(Calendar.MILLISECOND, 123);
        Date date = cal.getTime();

        StringBuffer sb = stdDateFormat.format(date, new StringBuffer(), new FieldPosition(0));
        assertEquals("2023-01-01T12:30:45.123+0000", sb.toString());
    }

    // Tests parsing ISO8601 date string with Z timezone
    @Test
    public void testParse_iso8601WithZ_success() throws Exception {
        Date dt = stdDateFormat.parse("2023-01-01T12:30:45.123Z");
        assertNotNull(dt);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(dt);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(45, cal.get(Calendar.SECOND));
        assertEquals(123, cal.get(Calendar.MILLISECOND));
    }

    // Tests parsing ISO8601 date string with Z and without milliseconds
    @Test
    public void testParse_iso8601WithZMissingMillis_success() throws Exception {
        Date dt = stdDateFormat.parse("2023-01-01T12:30:45Z");
        assertNotNull(dt);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(dt);
        assertEquals(45, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    // Tests parsing ISO8601 with timezone offset containing colon
    @Test
    public void testParse_iso8601WithColonTimezone_success() throws Exception {
        Date dt = stdDateFormat.parse("2023-01-01T14:30:45.123+02:00");
        assertNotNull(dt);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(dt);
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
    }

    // Tests parsing ISO8601 with partial time offset
    @Test
    public void testParse_iso8601With2DigitTimezoneOffset_success() throws Exception {
        Date dt = stdDateFormat.parse("2023-01-01T14:30:45.123+02");
        assertNotNull(dt);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(dt);
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
    }

    // Tests parsing plain date without time (yyyy-MM-dd)
    @Test
    public void testParse_plainDateFormat_success() throws Exception {
        Date dt = stdDateFormat.parse("2023-01-01");
        assertNotNull(dt);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(dt);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
    }

    // Tests parsing RFC1123 date string
    @Test
    public void testParse_rfc1123Format_success() throws Exception {
        Date dt = stdDateFormat.parse("Sun, 01 Jan 2023 12:30:45 GMT");
        assertNotNull(dt);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(dt);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(45, cal.get(Calendar.SECOND));
    }

    // Tests parsing numeric timestamp string (positive long)
    @Test
    public void testParse_numericTimestamp_success() throws Exception {
        long timestamp = 1672576245123L;
        Date dt = stdDateFormat.parse(String.valueOf(timestamp));
        assertNotNull(dt);
        assertEquals(timestamp, dt.getTime());
    }

    // Tests parsing negative numeric timestamp string
    @Test
    public void testParse_negativeNumericTimestamp_success() throws Exception {
        long timestamp = -1000000L;
        Date dt = stdDateFormat.parse(String.valueOf(timestamp));
        assertNotNull(dt);
        assertEquals(timestamp, dt.getTime());
    }

    // Tests parsing invalid date string throws ParseException
    @Test(expected = ParseException.class)
    public void testParse_invalidFormat_throwsParseException() throws Exception {
        stdDateFormat.parse("not-a-valid-date-string");
    }

    // Tests parse with ParsePosition returning null for invalid format
    @Test
    public void testParse_withParsePositionInvalid_returnsNull() {
        ParsePosition pos = new ParsePosition(0);
        Date dt = stdDateFormat.parse("invalid_date", pos);
        assertNull(dt);
    }

    // Tests parse with ParsePosition for numeric string
    @Test
    public void testParse_withParsePositionNumeric_success() {
        ParsePosition pos = new ParsePosition(0);
        Date dt = stdDateFormat.parse("123456789", pos);
        assertNotNull(dt);
        assertEquals(123456789L, dt.getTime());
    }

    // Tests withTimeZone creating a new instance with different timezone
    @Test
    public void testWithTimeZone_differentTimeZone_returnsNewInstance() {
        TimeZone tz = TimeZone.getTimeZone("GMT+2");
        StdDateFormat custom = stdDateFormat.withTimeZone(tz);
        assertNotSame(stdDateFormat, custom);
        assertEquals(tz, custom.getTimeZone());

        StdDateFormat same = custom.withTimeZone(tz);
        assertSame(custom, same);

        StdDateFormat resetToDefault = custom.withTimeZone(null);
        assertNotSame(custom, resetToDefault);
    }

    // Tests withLocale creating a new instance with different locale
    @Test
    public void testWithLocale_differentLocale_returnsNewInstance() {
        StdDateFormat custom = stdDateFormat.withLocale(Locale.GERMANY);
        assertNotSame(stdDateFormat, custom);

        StdDateFormat same = custom.withLocale(Locale.GERMANY);
        assertSame(custom, same);
    }

    // Tests setTimeZone resetting format instances
    @Test
    public void testSetTimeZone_updatesTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("PST");
        stdDateFormat.setTimeZone(tz);
        assertEquals(tz, stdDateFormat.getTimeZone());
    }

    // Tests clone method preserving configuration
    @Test
    public void testClone_createsIndependentCopy() {
        StdDateFormat clone = stdDateFormat.clone();
        assertNotNull(clone);
        assertNotSame(stdDateFormat, clone);
        assertEquals(stdDateFormat.isLenient(), clone.isLenient());
    }

    // Tests static factory methods for format instances
    @Test
    public void testStaticGetFormatMethods_returnConfiguredFormats() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        DateFormat isoFormat = StdDateFormat.getISO8601Format(tz, Locale.US);
        assertNotNull(isoFormat);
        assertEquals(tz, isoFormat.getTimeZone());

        DateFormat rfcFormat = StdDateFormat.getRFC1123Format(tz, Locale.US);
        assertNotNull(rfcFormat);
        assertEquals(tz, rfcFormat.getTimeZone());

        DateFormat isoFormatDeprecated = StdDateFormat.getISO8601Format(tz);
        assertNotNull(isoFormatDeprecated);

        DateFormat rfcFormatDeprecated = StdDateFormat.getRFC1123Format(tz);
        assertNotNull(rfcFormatDeprecated);
    }

    // Tests toString method contains class name and locale info
    @Test
    public void testToString_containsFormatInformation() {
        String str = stdDateFormat.toString();
        assertTrue(str.contains("DateFormat"));
        assertTrue(str.contains(Locale.US.toString()));

        StdDateFormat tzFormat = stdDateFormat.withTimeZone(TimeZone.getTimeZone("GMT+1"));
        String tzStr = tzFormat.toString();
        assertTrue(tzStr.contains("timezone:"));
    }
}