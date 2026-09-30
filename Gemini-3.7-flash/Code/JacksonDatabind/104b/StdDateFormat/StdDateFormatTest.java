package com.fasterxml.jackson.databind.util;

import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class StdDateFormatTest {

    private StdDateFormat stdDateFormat;

    @Before
    public void setUp() {
        stdDateFormat = new StdDateFormat();
    }

    // Tests default formatting to ISO-8601 format with UTC timezone and no colon in offset
    @Test
    public void testFormat_defaultUtc_formatsIso8601() {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(2020, Calendar.JANUARY, 15, 12, 30, 45);
        cal.set(Calendar.MILLISECOND, 123);
        Date date = cal.getTime();

        String formatted = stdDateFormat.format(date);
        assertEquals("2020-01-15T12:30:45.123+0000", formatted);
    }

    // Tests formatting with colon included in timezone offset (+00:00)
    @Test
    public void testFormat_withColonInTimeZone_formatsWithColon() {
        StdDateFormat format = stdDateFormat.withColonInTimeZone(true);
        assertTrue(format.isColonIncludedInTimeZone());

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(2020, Calendar.JANUARY, 15, 12, 30, 45);
        cal.set(Calendar.MILLISECOND, 123);
        Date date = cal.getTime();

        String formatted = format.format(date);
        assertEquals("2020-01-15T12:30:45.123+00:00", formatted);
    }

    // Tests formatting with non-UTC timezone and offset with colon
    @Test
    public void testFormat_nonUtcTimeZone_formatsCorrectOffset() {
        TimeZone tz = TimeZone.getTimeZone("GMT+05:30");
        StdDateFormat format = stdDateFormat.withTimeZone(tz).withColonInTimeZone(true);

        Calendar cal = new GregorianCalendar(tz, Locale.US);
        cal.clear();
        cal.set(2020, Calendar.JANUARY, 15, 12, 30, 45);
        cal.set(Calendar.MILLISECOND, 50);
        Date date = cal.getTime();

        String formatted = format.format(date);
        assertEquals("2020-01-15T12:30:45.050+05:30", formatted);
    }

    // Tests parsing plain date string (yyyy-MM-dd)
    @Test
    public void testParse_iso8601PlainDate_returnsDate() throws ParseException {
        Date parsed = stdDateFormat.parse("2020-01-15");
        assertNotNull(parsed);

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(parsed);
        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    // Tests parsing ISO-8601 with Z timezone indicator
    @Test
    public void testParse_iso8601FullWithZ_returnsDate() throws ParseException {
        Date parsed = stdDateFormat.parse("2020-01-15T12:30:45.123Z");
        assertNotNull(parsed);

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(parsed);
        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(45, cal.get(Calendar.SECOND));
        assertEquals(123, cal.get(Calendar.MILLISECOND));
    }

    // Tests parsing ISO-8601 with numeric timezone offset and colon
    @Test
    public void testParse_iso8601WithTzOffsetAndColon_returnsDate() throws ParseException {
        Date parsed = stdDateFormat.parse("2020-01-15T12:30:45.123+02:00");
        assertNotNull(parsed);

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(parsed);
        assertEquals(10, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
    }

    // Tests parsing ISO-8601 without seconds part (yyyy-MM-ddTHH:mm)
    @Test
    public void testParse_iso8601WithoutSeconds_returnsDate() throws ParseException {
        Date parsed = stdDateFormat.parse("2020-01-15T12:30Z");
        assertNotNull(parsed);

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(parsed);
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
    }

    // Tests parsing ISO-8601 with various fractional seconds length
    @Test
    public void testParse_iso8601FractionalSeconds_returnsDate() throws ParseException {
        Date d1 = stdDateFormat.parse("2020-01-15T12:30:45.1Z");
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(d1);
        assertEquals(100, cal.get(Calendar.MILLISECOND));

        Date d2 = stdDateFormat.parse("2020-01-15T12:30:45.12Z");
        cal.setTime(d2);
        assertEquals(120, cal.get(Calendar.MILLISECOND));

        Date d6 = stdDateFormat.parse("2020-01-15T12:30:45.123456Z");
        cal.setTime(d6);
        assertEquals(123, cal.get(Calendar.MILLISECOND));
    }

    // Tests parsing ISO-8601 with fractional seconds exceeding 9 digits throws ParseException
    @Test(expected = ParseException.class)
    public void testParse_iso8601FractionsOver9Digits_throwsParseException() throws ParseException {
        stdDateFormat.parse("2020-01-15T12:30:45.1234567890Z");
    }

    // Tests parsing RFC-1123 compliant date string
    @Test
    public void testParse_rfc1123Format_returnsDate() throws ParseException {
        Date parsed = stdDateFormat.parse("Wed, 15 Jan 2020 12:30:45 GMT");
        assertNotNull(parsed);

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(parsed);
        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
    }

    // Tests parsing stringified long timestamp
    @Test
    public void testParse_numericTimestampString_returnsDate() throws ParseException {
        long timestamp = 1579091445000L;
        Date parsed = stdDateFormat.parse(String.valueOf(timestamp));
        assertEquals(timestamp, parsed.getTime());

        Date negativeParsed = stdDateFormat.parse("-1000");
        assertEquals(-1000L, negativeParsed.getTime());
    }

    // Tests parsing invalid date string throws ParseException
    @Test(expected = ParseException.class)
    public void testParse_invalidDateString_throwsParseException() throws ParseException {
        stdDateFormat.parse("invalid-date-string");
    }

    // Tests parse method taking ParsePosition
    @Test
    public void testParse_withParsePosition_parsesCorrectly() {
        ParsePosition pos = new ParsePosition(0);
        Date date = stdDateFormat.parse("2020-01-15T12:30:45.000Z", pos);
        assertNotNull(date);
        assertEquals(0, pos.getErrorIndex());

        ParsePosition invalidPos = new ParsePosition(0);
        Date invalidDate = stdDateFormat.parse("invalid-date", invalidPos);
        assertNull(invalidDate);
    }

    // Tests mutant factory methods for TimeZone, Locale, and Lenient
    @Test
    public void testWithMethods_mutants_returnNewOrSameInstance() {
        TimeZone tz = TimeZone.getTimeZone("PST");
        StdDateFormat dfWithTz = stdDateFormat.withTimeZone(tz);
        assertNotSame(stdDateFormat, dfWithTz);
        assertEquals(tz, dfWithTz.getTimeZone());
        assertSame(dfWithTz, dfWithTz.withTimeZone(tz));
        assertSame(stdDateFormat, stdDateFormat.withTimeZone(null));

        Locale loc = Locale.GERMANY;
        StdDateFormat dfWithLoc = stdDateFormat.withLocale(loc);
        assertNotSame(stdDateFormat, dfWithLoc);
        assertSame(dfWithLoc, dfWithLoc.withLocale(loc));

        StdDateFormat dfLenient = stdDateFormat.withLenient(Boolean.FALSE);
        assertNotSame(stdDateFormat, dfLenient);
        assertFalse(dfLenient.isLenient());
        assertSame(dfLenient, dfLenient.withLenient(Boolean.FALSE));

        StdDateFormat dfColon = stdDateFormat.withColonInTimeZone(true);
        assertNotSame(stdDateFormat, dfColon);
        assertTrue(dfColon.isColonIncludedInTimeZone());
        assertSame(dfColon, dfColon.withColonInTimeZone(true));
    }

    // Tests cloning, equals and hashCode behavior
    @Test
    public void testCloneAndEqualsAndHashCode() {
        StdDateFormat clone = stdDateFormat.clone();
        assertNotSame(stdDateFormat, clone);
        assertFalse(stdDateFormat.equals(clone));
        assertTrue(stdDateFormat.equals(stdDateFormat));
        assertEquals(System.identityHashCode(stdDateFormat), stdDateFormat.hashCode());
    }

    // Tests setLenient, setTimeZone, and state mutation
    @Test
    public void testSetters_mutateState() {
        StdDateFormat format = new StdDateFormat();
        assertTrue(format.isLenient());

        format.setLenient(false);
        assertFalse(format.isLenient());

        TimeZone tz = TimeZone.getTimeZone("GMT+1");
        format.setTimeZone(tz);
        assertEquals(tz, format.getTimeZone());
    }

    // Tests deprecated static factory methods
    @SuppressWarnings("deprecation")
    @Test
    public void testDeprecatedFactoryMethods() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        Locale loc = Locale.US;

        DateFormat isoFormat = StdDateFormat.getISO8601Format(tz, loc);
        assertNotNull(isoFormat);

        DateFormat rfcFormat = StdDateFormat.getRFC1123Format(tz, loc);
        assertNotNull(rfcFormat);

        assertEquals(TimeZone.getTimeZone("UTC"), StdDateFormat.getDefaultTimeZone());
    }

    // Tests toPattern and toString methods
    @Test
    public void testToPatternAndToString() {
        String pattern = stdDateFormat.toPattern();
        assertNotNull(pattern);
        assertTrue(pattern.contains("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        assertTrue(pattern.contains("lenient"));

        StdDateFormat strict = stdDateFormat.withLenient(Boolean.FALSE);
        assertTrue(strict.toPattern().contains("strict"));

        String str = stdDateFormat.toString();
        assertNotNull(str);
        assertTrue(str.contains("DateFormat com.fasterxml.jackson.databind.util.StdDateFormat"));
    }
}