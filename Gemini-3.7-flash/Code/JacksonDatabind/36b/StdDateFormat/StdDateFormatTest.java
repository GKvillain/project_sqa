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

    // Tests default instance and static getters
    @Test
    public void testGetDefaultTimeZone_returnsUTC() {
        TimeZone tz = StdDateFormat.getDefaultTimeZone();
        assertEquals("UTC", tz.getID());
    }

    // Tests cloning and configuration methods
    @Test
    public void testWithTimeZone_validTimeZone_returnsNewInstanceWithTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("GMT+2");
        StdDateFormat custom = stdDateFormat.withTimeZone(tz);
        assertNotSame(stdDateFormat, custom);
        assertEquals(tz, custom.getTimeZone());

        // with null timezone defaults to UTC
        StdDateFormat defaultTz = custom.withTimeZone(null);
        assertEquals(StdDateFormat.getDefaultTimeZone(), defaultTz.getTimeZone());

        // same timezone returns same instance
        assertSame(custom, custom.withTimeZone(tz));
    }

    // Tests withLocale
    @Test
    public void testWithLocale_differentLocale_returnsNewInstance() {
        StdDateFormat custom = stdDateFormat.withLocale(Locale.GERMANY);
        assertNotSame(stdDateFormat, custom);

        // same locale returns this
        assertSame(stdDateFormat, stdDateFormat.withLocale(Locale.US));
    }

    // Tests clone method
    @Test
    public void testClone_createsIndependentCopy() {
        StdDateFormat cloned = stdDateFormat.clone();
        assertNotSame(stdDateFormat, cloned);
        assertEquals(stdDateFormat.getTimeZone(), cloned.getTimeZone());
        assertEquals(stdDateFormat.isLenient(), cloned.isLenient());
    }

    // Tests parsing ISO8601 with Zulu time ('Z')
    @Test
    public void testParse_iso8601WithZulu_returnsCorrectDate() throws Exception {
        Date d = stdDateFormat.parse("2020-01-01T12:00:00.000Z");
        assertNotNull(d);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(d);
        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
    }

    // Tests parsing ISO8601 with missing milliseconds and 'Z'
    @Test
    public void testParse_iso8601WithZuluMissingMillis_returnsCorrectDate() throws Exception {
        Date d = stdDateFormat.parse("2020-01-01T12:00:00Z");
        assertNotNull(d);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(d);
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
    }

    // Tests parsing plain date without time
    @Test
    public void testParse_plainDate_returnsCorrectDate() throws Exception {
        Date d = stdDateFormat.parse("2020-05-15");
        assertNotNull(d);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(d);
        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
    }

    // Tests parsing ISO8601 with timezone offset
    @Test
    public void testParse_iso8601WithOffset_returnsCorrectDate() throws Exception {
        Date d = stdDateFormat.parse("2020-01-01T12:00:00.000+02:00");
        assertNotNull(d);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(d);
        assertEquals(10, cal.get(Calendar.HOUR_OF_DAY)); // 12:00 +02:00 is 10:00 UTC
    }

    // Tests parsing ISO8601 with offset without colon
    @Test
    public void testParse_iso8601WithOffsetNoColon_returnsCorrectDate() throws Exception {
        Date d = stdDateFormat.parse("2020-01-01T12:00:00.000+0200");
        assertNotNull(d);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(d);
        assertEquals(10, cal.get(Calendar.HOUR_OF_DAY));
    }

    // Tests parsing ISO8601 with partial milliseconds
    @Test
    public void testParse_iso8601WithPartialMillis_returnsCorrectDate() throws Exception {
        Date d1 = stdDateFormat.parse("2020-01-01T12:00:00.1+0000");
        assertNotNull(d1);

        Date d2 = stdDateFormat.parse("2020-01-01T12:00:00.12+0000");
        assertNotNull(d2);

        Date d3 = stdDateFormat.parse("2020-01-01T12:00:00+0000");
        assertNotNull(d3);
    }

    // Tests parsing ISO8601 without timezone indicator (defaults to UTC/Z)
    @Test
    public void testParse_iso8601NoTimezone_returnsCorrectDate() throws Exception {
        Date d = stdDateFormat.parse("2020-01-01T12:00:00.000");
        assertNotNull(d);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(d);
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
    }

    // Tests parsing RFC1123 format
    @Test
    public void testParse_rfc1123Format_returnsCorrectDate() throws Exception {
        Date d = stdDateFormat.parse("Wed, 01 Jan 2020 12:00:00 GMT");
        assertNotNull(d);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(d);
        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
    }

    // Tests parsing numeric timestamp (positive and negative)
    @Test
    public void testParse_numericTimestamp_returnsCorrectDate() throws Exception {
        Date d1 = stdDateFormat.parse("0");
        assertEquals(0L, d1.getTime());

        Date d2 = stdDateFormat.parse("1577836800000");
        assertEquals(1577836800000L, d2.getTime());

        Date d3 = stdDateFormat.parse("-1000");
        assertEquals(-1000L, d3.getTime());
    }

    // Tests parse with ParsePosition for ISO8601
    @Test
    public void testParse_withParsePosition_returnsDate() {
        ParsePosition pos = new ParsePosition(0);
        Date d = stdDateFormat.parse("2020-01-01T12:00:00.000Z", pos);
        assertNotNull(d);
        assertEquals(0, pos.getErrorIndex());

        ParsePosition posNumeric = new ParsePosition(0);
        Date dNumeric = stdDateFormat.parse("123456", posNumeric);
        assertNotNull(dNumeric);
        assertEquals(123456L, dNumeric.getTime());
    }

    // Tests parsing invalid date string throws ParseException
    @Test(expected = ParseException.class)
    public void testParse_invalidFormat_throwsParseException() throws Exception {
        stdDateFormat.parse("invalid-date-string");
    }

    // Tests formatting a date to ISO8601
    @Test
    public void testFormat_validDate_returnsFormattedIsoString() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1, 12, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date d = cal.getTime();

        StringBuffer sb = new StringBuffer();
        StringBuffer result = stdDateFormat.format(d, sb, new FieldPosition(0));
        assertEquals("2020-01-01T12:00:00.000+0000", result.toString());
    }

    // Tests setTimeZone clears formats and updates timezone
    @Test
    public void testSetTimeZone_updatesTimeZoneAndClearsFormats() {
        TimeZone tz = TimeZone.getTimeZone("PST");
        stdDateFormat.setTimeZone(tz);
        assertEquals(tz, stdDateFormat.getTimeZone());

        // Calling format uses new timezone
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1, 12, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date d = cal.getTime();

        StringBuffer sb = new StringBuffer();
        StringBuffer result = stdDateFormat.format(d, sb, new FieldPosition(0));
        assertTrue(result.toString().contains("-0800"));
    }

    // Tests static factory methods for format blueprints
    @Test
    public void testStaticGetFormatMethods_returnConfiguredFormats() {
        TimeZone tz = TimeZone.getTimeZone("GMT+1");
        Locale loc = Locale.FRENCH;

        DateFormat isoFormat = StdDateFormat.getISO8601Format(tz, loc);
        assertNotNull(isoFormat);
        assertEquals(tz, isoFormat.getTimeZone());

        DateFormat rfcFormat = StdDateFormat.getRFC1123Format(tz, loc);
        assertNotNull(rfcFormat);
        assertEquals(tz, rfcFormat.getTimeZone());

        DateFormat isoDeprecated = StdDateFormat.getISO8601Format(tz);
        assertNotNull(isoDeprecated);

        DateFormat rfcDeprecated = StdDateFormat.getRFC1123Format(tz);
        assertNotNull(rfcDeprecated);
    }

    // Tests isLenient and toString representation
    @Test
    public void testIsLenientAndToString() {
        assertTrue(stdDateFormat.isLenient());

        StdDateFormat nonLenient = new StdDateFormat(TimeZone.getTimeZone("UTC"), Locale.US, Boolean.FALSE);
        assertFalse(nonLenient.isLenient());

        String str = stdDateFormat.toString();
        assertTrue(str.contains("DateFormat"));
        assertTrue(str.contains("locale: en_US"));
    }
}