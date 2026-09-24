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

    // Tests parsing valid plain date (yyyy-MM-dd)
    @Test
    public void testParse_plainDateFormat_returnsCorrectDate() throws Exception {
        Date date = stdDateFormat.parse("2020-01-01");
        assertNotNull(date);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.setTime(date);
        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
    }

    // Tests parsing ISO8601 date with trailing 'Z' and without milliseconds
    @Test
    public void testParse_iso8601WithZuluNoMillis_returnsCorrectDate() throws Exception {
        Date date = stdDateFormat.parse("2020-01-01T12:00:00Z");
        assertNotNull(date);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.setTime(date);
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    // Tests parsing ISO8601 date with trailing 'Z' and with milliseconds
    @Test
    public void testParse_iso8601WithZuluAndMillis_returnsCorrectDate() throws Exception {
        Date date = stdDateFormat.parse("2020-01-01T12:00:00.123Z");
        assertNotNull(date);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.setTime(date);
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(123, cal.get(Calendar.MILLISECOND));
    }

    // Tests parsing ISO8601 date with timezone offset containing colon (+HH:mm)
    @Test
    public void testParse_iso8601WithColonTimezone_returnsCorrectDate() throws Exception {
        Date date = stdDateFormat.parse("2020-01-01T12:00:00+02:00");
        assertNotNull(date);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.setTime(date);
        assertEquals(10, cal.get(Calendar.HOUR_OF_DAY));
    }

    // Tests parsing ISO8601 date with timezone offset without colon (+HHmm)
    @Test
    public void testParse_iso8601WithNonColonTimezone_returnsCorrectDate() throws Exception {
        Date date = stdDateFormat.parse("2020-01-01T12:00:00-0500");
        assertNotNull(date);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.setTime(date);
        assertEquals(17, cal.get(Calendar.HOUR_OF_DAY));
    }

    // Tests parsing ISO8601 date with 2-digit timezone offset (e.g. +HH)
    @Test
    public void testParse_iso8601With2DigitTimezone_returnsCorrectDate() throws Exception {
        Date date = stdDateFormat.parse("2020-01-01T12:00:00+02");
        assertNotNull(date);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.setTime(date);
        assertEquals(10, cal.get(Calendar.HOUR_OF_DAY));
    }

    // Tests parsing ISO8601 date without timezone indicator
    @Test
    public void testParse_iso8601WithoutTimezone_returnsCorrectDate() throws Exception {
        Date date = stdDateFormat.parse("2020-01-01T12:00:00");
        assertNotNull(date);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.setTime(date);
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
    }

    // Tests parsing valid RFC1123 date string
    @Test
    public void testParse_rfc1123Format_returnsCorrectDate() throws Exception {
        Date date = stdDateFormat.parse("Wed, 01 Jan 2020 12:00:00 GMT");
        assertNotNull(date);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.setTime(date);
        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
    }

    // Tests parsing stringified positive numeric timestamp
    @Test
    public void testParse_numericTimestampPositive_returnsCorrectDate() throws Exception {
        long timestamp = 1577880000000L;
        Date date = stdDateFormat.parse(String.valueOf(timestamp));
        assertNotNull(date);
        assertEquals(timestamp, date.getTime());
    }

    // Tests parsing stringified negative numeric timestamp
    @Test
    public void testParse_numericTimestampNegative_returnsCorrectDate() throws Exception {
        long timestamp = -1000000L;
        Date date = stdDateFormat.parse(String.valueOf(timestamp));
        assertNotNull(date);
        assertEquals(timestamp, date.getTime());
    }

    // Tests parsing invalid date string expecting ParseException
    @Test(expected = ParseException.class)
    public void testParse_invalidDateString_throwsParseException() throws Exception {
        stdDateFormat.parse("invalid-date-format");
    }

    // Tests parse method with ParsePosition parameter on error
    @Test
    public void testParse_invalidDateWithParsePosition_returnsNull() {
        ParsePosition pos = new ParsePosition(0);
        Date result = stdDateFormat.parse("not-a-date", pos);
        assertNull(result);
    }

    // Tests date formatting to ISO8601 standard format
    @Test
    public void testFormat_validDate_formatsToISO8601() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.clear();
        cal.set(2020, Calendar.JANUARY, 1, 12, 0, 0);
        Date date = cal.getTime();

        StringBuffer sb = new StringBuffer();
        stdDateFormat.format(date, sb, new FieldPosition(0));
        assertEquals("2020-01-01T12:00:00.000+0000", sb.toString());
    }

    // Tests withTimeZone and clone methods
    @Test
    public void testWithTimeZone_newTimeZone_returnsNewInstance() {
        TimeZone tz = TimeZone.getTimeZone("PST");
        StdDateFormat cloned = stdDateFormat.withTimeZone(tz);
        assertNotSame(stdDateFormat, cloned);

        StdDateFormat sameTz = cloned.withTimeZone(tz);
        assertSame(cloned, sameTz);

        StdDateFormat defaultTz = cloned.withTimeZone(null);
        assertNotSame(cloned, defaultTz);
    }

    // Tests withLocale method
    @Test
    public void testWithLocale_differentLocale_returnsNewInstance() {
        StdDateFormat withLoc = stdDateFormat.withLocale(Locale.FRANCE);
        assertNotSame(stdDateFormat, withLoc);

        StdDateFormat sameLoc = withLoc.withLocale(Locale.FRANCE);
        assertSame(withLoc, sameLoc);
    }

    // Tests setTimeZone resetting cached formats
    @Test
    public void testSetTimeZone_updatesTimeZoneAndResetsFormats() throws Exception {
        TimeZone tz1 = TimeZone.getTimeZone("GMT");
        TimeZone tz2 = TimeZone.getTimeZone("EST");
        stdDateFormat.setTimeZone(tz1);
        stdDateFormat.parse("2020-01-01T12:00:00Z");
        stdDateFormat.setTimeZone(tz2);
        assertTrue(stdDateFormat.toString().contains("EST"));
    }

    // Tests static factory / blueprint helper methods
    @Test
    public void testStaticFactoriesAndBlueprints() {
        assertNotNull(StdDateFormat.getDefaultTimeZone());
        assertNotNull(StdDateFormat.getBlueprintISO8601Format());
        assertNotNull(StdDateFormat.getBlueprintRFC1123Format());

        TimeZone tz = TimeZone.getTimeZone("GMT");
        DateFormat iso8601 = StdDateFormat.getISO8601Format(tz, Locale.GERMANY);
        assertNotNull(iso8601);
        assertEquals(tz, iso8601.getTimeZone());

        DateFormat rfc1123 = StdDateFormat.getRFC1123Format(tz, Locale.GERMANY);
        assertNotNull(rfc1123);
        assertEquals(tz, rfc1123.getTimeZone());
    }

    // Tests clone method creates independent copy
    @Test
    public void testClone_createsIndependentCopy() {
        StdDateFormat cloned = stdDateFormat.clone();
        assertNotNull(cloned);
        assertNotSame(stdDateFormat, cloned);
        assertEquals(stdDateFormat.toString(), cloned.toString());
    }
}