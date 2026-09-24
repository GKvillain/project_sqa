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

    // Tests default formatting with UTC timezone
    @Test
    public void testFormat_defaultUtc_formatsCorrectly() {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.MARCH, 15, 10, 30, 45);
        cal.set(Calendar.MILLISECOND, 123);
        Date date = cal.getTime();

        String formatted = stdDateFormat.format(date);
        assertEquals("2023-03-15T10:30:45.123+0000", formatted);
    }

    // Tests formatting with colon included in timezone
    @Test
    public void testFormat_withColonInTimeZone_includesColon() {
        StdDateFormat format = stdDateFormat.withColonInTimeZone(true);
        assertTrue(format.isColonIncludedInTimeZone());

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.MARCH, 15, 10, 30, 45);
        cal.set(Calendar.MILLISECOND, 123);
        Date date = cal.getTime();

        String formatted = format.format(date);
        assertEquals("2023-03-15T10:30:45.123+00:00", formatted);
    }

    // Tests formatting with non-UTC timezone and offset
    @Test
    public void testFormat_nonUtcTimeZone_formatsOffsetCorrectly() {
        TimeZone tz = TimeZone.getTimeZone("GMT+05:30");
        StdDateFormat format = stdDateFormat.withTimeZone(tz).withColonInTimeZone(true);

        Calendar cal = new GregorianCalendar(tz, Locale.US);
        cal.set(2023, Calendar.DECEMBER, 25, 18, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();

        String formatted = format.format(date);
        assertEquals("2023-12-25T18:00:00.000+05:30", formatted);
    }

    // Tests formatting with negative timezone offset
    @Test
    public void testFormat_negativeTimezoneOffset_formatsOffsetCorrectly() {
        TimeZone tz = TimeZone.getTimeZone("GMT-08:00");
        StdDateFormat format = stdDateFormat.withTimeZone(tz);

        Calendar cal = new GregorianCalendar(tz, Locale.US);
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 5);
        Date date = cal.getTime();

        String formatted = format.format(date);
        assertEquals("2023-01-01T00:00:00.005-0800", formatted);
    }

    // Tests parsing valid ISO8601 string with milliseconds and Z
    @Test
    public void testParse_iso8601WithMillisAndZ_returnsCorrectDate() throws ParseException {
        Date date = stdDateFormat.parse("2023-03-15T10:30:45.123Z");
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(date);

        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.MARCH, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(10, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(45, cal.get(Calendar.SECOND));
        assertEquals(123, cal.get(Calendar.MILLISECOND));
    }

    // Tests parsing ISO8601 string with positive offset and colon
    @Test
    public void testParse_iso8601WithPositiveOffset_returnsCorrectDate() throws ParseException {
        Date date = stdDateFormat.parse("2023-03-15T15:30:45.000+05:00");
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(date);

        assertEquals(10, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
    }

    // Tests parsing ISO8601 plain date string without time
    @Test
    public void testParse_plainDate_returnsCorrectDate() throws ParseException {
        Date date = stdDateFormat.parse("2023-03-15");
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(date);

        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.MARCH, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    // Tests parsing ISO8601 date without seconds and timezone
    @Test
    public void testParse_iso8601WithoutSeconds_returnsCorrectDate() throws ParseException {
        Date date = stdDateFormat.parse("2023-03-15T10:30");
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(date);

        assertEquals(10, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
    }

    // Tests parsing ISO8601 with fractional seconds of different lengths (1, 2, 3 digits)
    @Test
    public void testParse_iso8601WithVaryingFractions_returnsCorrectDate() throws ParseException {
        Date d1 = stdDateFormat.parse("2023-03-15T10:30:45.1Z");
        Calendar cal1 = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal1.setTime(d1);
        assertEquals(100, cal1.get(Calendar.MILLISECOND));

        Date d2 = stdDateFormat.parse("2023-03-15T10:30:45.12Z");
        Calendar cal2 = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal2.setTime(d2);
        assertEquals(120, cal2.get(Calendar.MILLISECOND));
    }

    // Tests parsing RFC1123 compliant date string
    @Test
    public void testParse_rfc1123_returnsCorrectDate() throws ParseException {
        Date date = stdDateFormat.parse("Wed, 15 Mar 2023 10:30:45 GMT");
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTime(date);

        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.MARCH, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(10, cal.get(Calendar.HOUR_OF_DAY));
    }

    // Tests parsing timestamp formatted as numeric string
    @Test
    public void testParse_numericTimestampString_returnsCorrectDate() throws ParseException {
        Date date = stdDateFormat.parse("1678876245000");
        assertEquals(1678876245000L, date.getTime());
    }

    // Tests parsing negative numeric timestamp string
    @Test
    public void testParse_negativeTimestampString_returnsCorrectDate() throws ParseException {
        Date date = stdDateFormat.parse("-100000");
        assertEquals(-100000L, date.getTime());
    }

    // Tests parsing invalid date string throws ParseException
    @Test(expected = ParseException.class)
    public void testParse_invalidFormat_throwsParseException() throws ParseException {
        stdDateFormat.parse("not-a-valid-date");
    }

    // Tests parsing date with invalid fractional seconds (>9 digits) throws ParseException
    @Test(expected = ParseException.class)
    public void testParse_fractionalSecondsTooLong_throwsParseException() throws ParseException {
        stdDateFormat.parse("2023-03-15T10:30:45.1234567890Z");
    }

    // Tests mutant factory methods for immutability and property changes
    @Test
    public void testMutantFactories_returnsModifiedInstances() {
        TimeZone tz = TimeZone.getTimeZone("PST");
        StdDateFormat withTz = stdDateFormat.withTimeZone(tz);
        assertNotSame(stdDateFormat, withTz);
        assertEquals(tz, withTz.getTimeZone());

        StdDateFormat sameTz = withTz.withTimeZone(tz);
        assertSame(withTz, sameTz);

        StdDateFormat withNullTz = withTz.withTimeZone(null);
        assertEquals(StdDateFormat.getDefaultTimeZone(), withNullTz.getTimeZone());

        Locale loc = Locale.FRANCE;
        StdDateFormat withLoc = stdDateFormat.withLocale(loc);
        assertNotSame(stdDateFormat, withLoc);

        StdDateFormat sameLoc = withLoc.withLocale(loc);
        assertSame(withLoc, sameLoc);

        StdDateFormat withLenientFalse = stdDateFormat.withLenient(Boolean.FALSE);
        assertNotSame(stdDateFormat, withLenientFalse);
        assertFalse(withLenientFalse.isLenient());

        StdDateFormat sameLenient = withLenientFalse.withLenient(Boolean.FALSE);
        assertSame(withLenientFalse, sameLenient);

        StdDateFormat withColon = stdDateFormat.withColonInTimeZone(true);
        assertTrue(withColon.isColonIncludedInTimeZone());

        StdDateFormat sameColon = withColon.withColonInTimeZone(true);
        assertSame(withColon, sameColon);
    }

    // Tests setTimeZone and setLenient behavior
    @Test
    public void testSetters_mutateConfiguration() {
        StdDateFormat df = (StdDateFormat) stdDateFormat.clone();
        assertTrue(df.isLenient());

        df.setLenient(false);
        assertFalse(df.isLenient());

        TimeZone tz = TimeZone.getTimeZone("GMT+2");
        df.setTimeZone(tz);
        assertEquals(tz, df.getTimeZone());
    }

    // Tests parse with ParsePosition
    @Test
    public void testParse_withParsePosition_parsesCorrectly() {
        ParsePosition pos = new ParsePosition(0);
        Date date = stdDateFormat.parse("2023-03-15T10:30:45.000Z", pos);
        assertNotNull(date);
        assertEquals(-1, pos.getErrorIndex());

        ParsePosition invalidPos = new ParsePosition(0);
        Date invalidDate = stdDateFormat.parse("invalid-date", invalidPos);
        assertNull(invalidDate);
    }

    // Tests clone, equals, hashCode, toPattern, and toString methods
    @Test
    public void testCommonObjectMethods() {
        StdDateFormat clone = stdDateFormat.clone();
        assertNotNull(clone);
        assertNotSame(stdDateFormat, clone);

        assertEquals(stdDateFormat, stdDateFormat);
        assertNotEquals(stdDateFormat, clone);
        assertEquals(System.identityHashCode(stdDateFormat), stdDateFormat.hashCode());

        String pattern = stdDateFormat.toPattern();
        assertTrue(pattern.contains(StdDateFormat.DATE_FORMAT_STR_ISO8601));
        assertTrue(pattern.contains("lenient"));

        StdDateFormat strict = stdDateFormat.withLenient(Boolean.FALSE);
        assertTrue(strict.toPattern().contains("strict"));

        String str = stdDateFormat.toString();
        assertTrue(str.contains(StdDateFormat.class.getName()));
    }

    // Tests deprecated factory methods getISO8601Format and getRFC1123Format
    @SuppressWarnings("deprecation")
    @Test
    public void testDeprecatedFormatFactories() {
        DateFormat isoFormat = StdDateFormat.getISO8601Format(TimeZone.getTimeZone("UTC"), Locale.US);
        assertNotNull(isoFormat);

        DateFormat rfcFormat = StdDateFormat.getRFC1123Format(TimeZone.getTimeZone("UTC"), Locale.GERMANY);
        assertNotNull(rfcFormat);
    }
}