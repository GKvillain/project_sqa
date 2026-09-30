package com.fasterxml.jackson.databind.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class StdDateFormatTest {

    private StdDateFormat stdDateFormat;

    @Before
    public void setUp() {
        stdDateFormat = new StdDateFormat();
    }

    // Tests default timezone configuration
    @Test
    public void testGetDefaultTimeZone_default_returnsUtc() {
        TimeZone tz = StdDateFormat.getDefaultTimeZone();
        assertEquals("UTC", tz.getID());
    }

    // Tests parsing ISO8601 date string ending with 'Z'
    @Test
    public void testParse_iso8601WithZulu_returnsCorrectDate() throws ParseException {
        Date result = stdDateFormat.parse("2020-01-01T00:00:00.000Z");
        assertNotNull(result);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(result);
        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
    }

    // Tests parsing ISO8601 date string ending with 'Z' without milliseconds
    @Test
    public void testParse_iso8601WithZuluNoMillis_returnsCorrectDate() throws ParseException {
        Date result = stdDateFormat.parse("2020-01-01T12:30:45Z");
        assertNotNull(result);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(result);
        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(45, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    // Tests parsing ISO8601 date string with colon timezone offset
    @Test
    public void testParse_iso8601WithColonTimezone_returnsCorrectDate() throws ParseException {
        Date result = stdDateFormat.parse("2020-01-01T00:00:00.000+02:00");
        assertNotNull(result);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(result);
        // UTC time should be 2019-12-31 22:00:00
        assertEquals(2019, cal.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, cal.get(Calendar.MONTH));
        assertEquals(31, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(22, cal.get(Calendar.HOUR_OF_DAY));
    }

    // Tests parsing ISO8601 date string with 2-digit hour-only timezone offset
    @Test
    public void testParse_iso8601WithTwoDigitTimezone_returnsCorrectDate() throws ParseException {
        Date result = stdDateFormat.parse("2020-01-01T00:00:00.000+02");
        assertNotNull(result);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(result);
        assertEquals(2019, cal.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, cal.get(Calendar.MONTH));
        assertEquals(31, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(22, cal.get(Calendar.HOUR_OF_DAY));
    }

    // Tests parsing ISO8601 date string without timezone indicator
    @Test
    public void testParse_iso8601WithoutTimezone_defaultsToUtc() throws ParseException {
        Date result = stdDateFormat.parse("2020-01-01T10:15:30");
        assertNotNull(result);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(result);
        assertEquals(10, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(15, cal.get(Calendar.MINUTE));
        assertEquals(30, cal.get(Calendar.SECOND));
    }

    // Tests parsing plain date without time part
    @Test
    public void testParse_plainDate_returnsCorrectDate() throws ParseException {
        Date result = stdDateFormat.parse("2020-05-15");
        assertNotNull(result);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(result);
        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
    }

    // Tests parsing stringified timestamp number
    @Test
    public void testParse_timestampString_returnsCorrectDate() throws ParseException {
        long timestamp = 1577836800000L;
        Date result = stdDateFormat.parse(String.valueOf(timestamp));
        assertEquals(timestamp, result.getTime());
    }

    // Tests parsing negative stringified timestamp number
    @Test
    public void testParse_negativeTimestampString_returnsCorrectDate() throws ParseException {
        long timestamp = -1000000L;
        Date result = stdDateFormat.parse(String.valueOf(timestamp));
        assertEquals(timestamp, result.getTime());
    }

    // Tests parsing RFC1123 compliant date string
    @Test
    public void testParse_rfc1123String_returnsCorrectDate() throws ParseException {
        Date result = stdDateFormat.parse("Wed, 01 Jan 2020 00:00:00 GMT");
        assertNotNull(result);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(result);
        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
    }

    // Tests parse with invalid date format throwing ParseException
    @Test(expected = ParseException.class)
    public void testParse_invalidFormat_throwsParseException() throws ParseException {
        stdDateFormat.parse("invalid-date-string");
    }

    // Tests parse method with ParsePosition
    @Test
    public void testParseWithPosition_validPlainDate_returnsDate() {
        ParsePosition pos = new ParsePosition(0);
        Date result = stdDateFormat.parse("2020-01-01", pos);
        assertNotNull(result);
        assertEquals(10, pos.getIndex());
    }

    // Tests parse method with ParsePosition on invalid string
    @Test
    public void testParseWithPosition_invalidString_returnsNull() {
        ParsePosition pos = new ParsePosition(0);
        Date result = stdDateFormat.parse("not-a-date", pos);
        assertNull(result);
    }

    // Tests formatting Date object to ISO8601 string
    @Test
    public void testFormat_validDate_returnsFormattedIso8601String() {
        Date date = new Date(0L); // 1970-01-01T00:00:00.000Z in UTC
        String formatted = stdDateFormat.format(date);
        assertEquals("1970-01-01T00:00:00.000+0000", formatted);
    }

    // Tests withTimeZone immutability and behavior
    @Test
    public void testWithTimeZone_customTimeZone_returnsNewInstanceWithTargetTz() {
        TimeZone tzGmtPlus2 = TimeZone.getTimeZone("GMT+2");
        StdDateFormat customTzFormat = stdDateFormat.withTimeZone(tzGmtPlus2);

        assertNotSame(stdDateFormat, customTzFormat);
        assertEquals(tzGmtPlus2, customTzFormat.getTimeZone());

        StdDateFormat sameTzFormat = customTzFormat.withTimeZone(tzGmtPlus2);
        assertSame(customTzFormat, sameTzFormat);

        StdDateFormat resetTzFormat = customTzFormat.withTimeZone(null);
        assertEquals(StdDateFormat.getDefaultTimeZone(), resetTzFormat.getTimeZone());
    }

    // Tests withLocale immutability and behavior
    @Test
    public void testWithLocale_customLocale_returnsNewInstanceWithTargetLocale() {
        StdDateFormat customLocaleFormat = stdDateFormat.withLocale(Locale.GERMANY);
        assertNotSame(stdDateFormat, customLocaleFormat);

        StdDateFormat sameLocaleFormat = customLocaleFormat.withLocale(Locale.GERMANY);
        assertSame(customLocaleFormat, sameLocaleFormat);
    }

    // Tests clone method produces independent instance
    @Test
    public void testClone_createsIndependentInstance() {
        StdDateFormat cloned = stdDateFormat.clone();
        assertNotNull(cloned);
        assertNotSame(stdDateFormat, cloned);
    }

    // Tests setLenient and isLenient
    @Test
    public void testSetLenient_and_isLenient_togglesCorrectly() {
        assertTrue(stdDateFormat.isLenient());

        stdDateFormat.setLenient(false);
        assertFalse(stdDateFormat.isLenient());

        stdDateFormat.setLenient(true);
        assertTrue(stdDateFormat.isLenient());
    }

    // Tests setTimeZone updates timezone state
    @Test
    public void testSetTimeZone_updatesTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("PST");
        stdDateFormat.setTimeZone(tz);
        assertEquals(tz, stdDateFormat.getTimeZone());
    }

    // Tests static factory methods for format instances
    @Test
    public void testGetISO8601AndRFC1123Format_returnsNonNullInstances() {
        DateFormat isoFormat = StdDateFormat.getISO8601Format(TimeZone.getTimeZone("UTC"), Locale.US);
        assertNotNull(isoFormat);

        DateFormat rfcFormat = StdDateFormat.getRFC1123Format(TimeZone.getTimeZone("UTC"), Locale.US);
        assertNotNull(rfcFormat);

        DateFormat isoDep = StdDateFormat.getISO8601Format(TimeZone.getTimeZone("UTC"));
        assertNotNull(isoDep);

        DateFormat rfcDep = StdDateFormat.getRFC1123Format(TimeZone.getTimeZone("UTC"));
        assertNotNull(rfcDep);
    }

    // Tests equals, hashCode and toString
    @Test
    public void testEqualsAndHashCodeAndToString() {
        assertTrue(stdDateFormat.equals(stdDateFormat));
        assertFalse(stdDateFormat.equals(new Object()));
        assertEquals(System.identityHashCode(stdDateFormat), stdDateFormat.hashCode());

        String str = stdDateFormat.toString();
        assertNotNull(str);
        assertTrue(str.contains("DateFormat"));
    }
}