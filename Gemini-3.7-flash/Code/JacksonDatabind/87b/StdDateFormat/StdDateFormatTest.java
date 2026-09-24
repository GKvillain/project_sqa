package com.fasterxml.jackson.databind.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
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

    // Tests default instance state, timezone, and leniency
    @Test
    public void testDefaultState_initialValues_expectedDefaults() {
        assertNotNull(StdDateFormat.instance);
        assertEquals(TimeZone.getTimeZone("UTC"), StdDateFormat.getDefaultTimeZone());
        assertTrue(stdDateFormat.isLenient());
        assertNull(stdDateFormat.getTimeZone());
    }

    // Tests withTimeZone behavior when timezone is null, identical, or different
    @Test
    public void testWithTimeZone_variousInputs_returnsExpectedInstance() {
        TimeZone tzPst = TimeZone.getTimeZone("PST");
        StdDateFormat dfWithTz = stdDateFormat.withTimeZone(tzPst);
        assertNotSame(stdDateFormat, dfWithTz);
        assertEquals(tzPst, dfWithTz.getTimeZone());

        StdDateFormat sameDf = dfWithTz.withTimeZone(tzPst);
        assertSame(dfWithTz, sameDf);

        StdDateFormat defaultTzDf = dfWithTz.withTimeZone(null);
        assertEquals(StdDateFormat.getDefaultTimeZone(), defaultTzDf.getTimeZone());
    }

    // Tests withLocale behavior when locale is identical or different
    @Test
    public void testWithLocale_variousLocales_returnsExpectedInstance() {
        StdDateFormat dfGermany = stdDateFormat.withLocale(Locale.GERMANY);
        assertNotSame(stdDateFormat, dfGermany);

        StdDateFormat sameDf = dfGermany.withLocale(Locale.GERMANY);
        assertSame(dfGermany, sameDf);
    }

    // Tests clone method
    @Test
    public void testClone_createsIndependentInstance() {
        stdDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        stdDateFormat.setLenient(false);

        StdDateFormat cloned = stdDateFormat.clone();
        assertNotSame(stdDateFormat, cloned);
        assertEquals(stdDateFormat.getTimeZone(), cloned.getTimeZone());
        assertFalse(cloned.isLenient());
    }

    // Tests setTimeZone and setLenient clearing cached formats
    @Test
    public void testSetTimeZoneAndSetLenient_modifiesState() {
        TimeZone tzEst = TimeZone.getTimeZone("EST");
        stdDateFormat.setTimeZone(tzEst);
        assertEquals(tzEst, stdDateFormat.getTimeZone());

        stdDateFormat.setLenient(false);
        assertFalse(stdDateFormat.isLenient());

        stdDateFormat.setLenient(true);
        assertTrue(stdDateFormat.isLenient());
    }

    // Tests formatting date to standard ISO-8601 string
    @Test
    public void testFormat_validDate_formatsToISO8601() {
        Date date = new Date(0L);
        StringBuffer sb = new StringBuffer();
        stdDateFormat.format(date, sb, new FieldPosition(0));
        assertEquals("1970-01-01T00:00:00.000+0000", sb.toString());
    }

    // Tests parsing plain date without time (yyyy-MM-dd)
    @Test
    public void testParse_plainDate_parsedCorrectly() throws ParseException {
        Date parsed = stdDateFormat.parse("1970-01-01");
        assertNotNull(parsed);
        assertEquals(0L, parsed.getTime());
    }

    // Tests parsing ISO-8601 Zulu format with and without milliseconds
    @Test
    public void testParse_iso8601Zulu_parsedCorrectly() throws ParseException {
        Date parsedWithMillis = stdDateFormat.parse("1970-01-01T00:00:00.000Z");
        assertNotNull(parsedWithMillis);
        assertEquals(0L, parsedWithMillis.getTime());

        Date parsedWithoutMillis = stdDateFormat.parse("1970-01-01T00:00:00Z");
        assertNotNull(parsedWithoutMillis);
        assertEquals(0L, parsedWithoutMillis.getTime());
    }

    // Tests parsing ISO-8601 with timezone offset containing colon (e.g., +00:00)
    @Test
    public void testParse_iso8601WithColonTimezone_parsedCorrectly() throws ParseException {
        Date parsed = stdDateFormat.parse("1970-01-01T00:00:00.000+00:00");
        assertNotNull(parsed);
        assertEquals(0L, parsed.getTime());

        Date parsedOffset = stdDateFormat.parse("1970-01-01T01:00:00.000+01:00");
        assertNotNull(parsedOffset);
        assertEquals(0L, parsedOffset.getTime());
    }

    // Tests parsing ISO-8601 with 2-digit timezone offset (e.g., +00, -00)
    @Test
    public void testParse_iso8601WithShortTimezone_parsedCorrectly() throws ParseException {
        Date parsed = stdDateFormat.parse("1970-01-01T00:00:00.000+00");
        assertNotNull(parsed);
        assertEquals(0L, parsed.getTime());
    }

    // Tests parsing ISO-8601 without explicit timezone
    @Test
    public void testParse_iso8601NoTimezone_parsedAsUtc() throws ParseException {
        Date parsed = stdDateFormat.parse("1970-01-01T00:00:00.000");
        assertNotNull(parsed);
        assertEquals(0L, parsed.getTime());

        Date parsedNoMillis = stdDateFormat.parse("1970-01-01T00:00:00");
        assertNotNull(parsedNoMillis);
        assertEquals(0L, parsedNoMillis.getTime());
    }

    // Tests parsing numeric timestamp strings (positive and negative)
    @Test
    public void testParse_numericTimestampStrings_parsedCorrectly() throws ParseException {
        Date positive = stdDateFormat.parse("1000");
        assertEquals(1000L, positive.getTime());

        Date negative = stdDateFormat.parse("-1000");
        assertEquals(-1000L, negative.getTime());
    }

    // Tests parsing RFC-1123 compliant date string
    @Test
    public void testParse_rfc1123Date_parsedCorrectly() throws ParseException {
        Date parsed = stdDateFormat.parse("Thu, 01 Jan 1970 00:00:00 GMT");
        assertNotNull(parsed);
        assertEquals(0L, parsed.getTime());
    }

    // Tests parsing invalid date string expecting ParseException
    @Test(expected = ParseException.class)
    public void testParse_invalidFormat_throwsParseException() throws ParseException {
        stdDateFormat.parse("not-a-valid-date");
    }

    // Tests parse method taking ParsePosition directly
    @Test
    public void testParse_withParsePosition_returnsDateOrNull() {
        ParsePosition pos = new ParsePosition(0);
        Date parsed = stdDateFormat.parse("1970-01-01T00:00:00.000Z", pos);
        assertNotNull(parsed);
        assertEquals(0L, parsed.getTime());

        ParsePosition invalidPos = new ParsePosition(0);
        Date invalidResult = stdDateFormat.parse("invalid-date-string", invalidPos);
        assertNull(invalidResult);
    }

    // Tests deprecated static factory methods for ISO-8601 and RFC-1123 formats
    @SuppressWarnings("deprecation")
    @Test
    public void testStaticGetFormatMethods_returnsConfiguredDateFormat() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        DateFormat isoFormat = StdDateFormat.getISO8601Format(tz);
        assertNotNull(isoFormat);

        DateFormat isoFormatWithLocale = StdDateFormat.getISO8601Format(tz, Locale.GERMANY);
        assertNotNull(isoFormatWithLocale);

        DateFormat rfcFormat = StdDateFormat.getRFC1123Format(tz);
        assertNotNull(rfcFormat);

        DateFormat rfcFormatWithLocale = StdDateFormat.getRFC1123Format(tz, Locale.GERMANY);
        assertNotNull(rfcFormatWithLocale);
    }

    // Tests toString, equals, and hashCode methods
    @Test
    public void testObjectOverrides_toStringEqualsHashCode() {
        String str = stdDateFormat.toString();
        assertNotNull(str);
        assertTrue(str.contains("StdDateFormat"));

        assertTrue(stdDateFormat.equals(stdDateFormat));
        assertFalse(stdDateFormat.equals(new StdDateFormat()));
        assertFalse(stdDateFormat.equals(null));
        assertFalse(stdDateFormat.equals("string"));

        assertEquals(System.identityHashCode(stdDateFormat), stdDateFormat.hashCode());
    }
}