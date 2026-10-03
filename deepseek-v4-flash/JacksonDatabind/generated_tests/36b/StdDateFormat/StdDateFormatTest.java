package com.fasterxml.jackson.databind.util;

import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test class for StdDateFormat, targeting Defects4J bug 36b.
 * Covers parsing, formatting, configuration, and edge cases.
 */
public class StdDateFormatTest {

    private final StdDateFormat fmt = new StdDateFormat();
    private final StringBuffer sb = new StringBuffer();
    private final FieldPosition fp = new FieldPosition(0);
    private static final long TIMESTAMP_2019_01_01 = 1546300800000L; // 2019-01-01T00:00:00.000Z

    // ==================== Normal parsing ====================

    @Test
    // Tests standard ISO-8601 date with 'Z'
    public void testParse_iso8601Z_validDate_returnsDate() throws Exception {
        Date dt = fmt.parse("2019-01-01T00:00:00.000Z");
        assertEquals(TIMESTAMP_2019_01_01, dt.getTime());
    }

    @Test
    // Tests ISO-8601 date with explicit "+0000" offset
    public void testParse_iso8601Offset_validDate_returnsDate() throws Exception {
        Date dt = fmt.parse("2019-01-01T00:00:00.000+0000");
        assertEquals(TIMESTAMP_2019_01_01, dt.getTime());
    }

    @Test
    // Tests ISO-8601 date with colon in offset "+00:00"
    public void testParse_iso8601ColonOffset_validDate_returnsDate() throws Exception {
        Date dt = fmt.parse("2019-01-01T00:00:00.000+00:00");
        assertEquals(TIMESTAMP_2019_01_01, dt.getTime());
    }

    @Test
    // Tests plain date (yyyy-MM-dd)
    public void testParse_plainDate_validDate_returnsDate() throws Exception {
        Date dt = fmt.parse("2019-01-01");
        assertEquals(TIMESTAMP_2019_01_01, dt.getTime());
    }

    @Test
    // Tests RFC-1123 date
    public void testParse_rfc1123_validDate_returnsDate() throws Exception {
        Date dt = fmt.parse("Tue, 01 Jan 2019 00:00:00 UTC");
        assertEquals(TIMESTAMP_2019_01_01, dt.getTime());
    }

    @Test
    // Tests numeric timestamp (long)
    public void testParse_timestampPositive_returnsDate() throws Exception {
        Date dt = fmt.parse("1546300800000");
        assertEquals(TIMESTAMP_2019_01_01, dt.getTime());
    }

    @Test
    // Tests negative timestamp
    public void testParse_negativeTimestamp_returnsDate() throws Exception {
        Date dt = fmt.parse("-1000000");
        assertEquals(-1000000L, dt.getTime());
    }

    // ==================== Edge cases (missing parts) ====================

    @Test
    // Tests ISO-8601 date without milliseconds (only 'Z')
    public void testParse_iso8601NoMillis_returnsDate() throws Exception {
        Date dt = fmt.parse("2019-01-01T00:00:00Z");
        assertEquals(TIMESTAMP_2019_01_01, dt.getTime());
    }

    @Test
    // Tests ISO-8601 date without seconds (only 'Z')
    public void testParse_iso8601NoSeconds_returnsDate() throws Exception {
        Date dt = fmt.parse("2019-01-01T00:00Z");
        assertEquals(TIMESTAMP_2019_01_01, dt.getTime());
    }

    @Test
    // Tests ISO-8601 date with only hour (no time zone), adds .000Z
    public void testParse_iso8601OnlyHour_addsZ_returnsDate() throws Exception {
        Date dt = fmt.parse("2019-01-01T00");
        assertEquals(TIMESTAMP_2019_01_01, dt.getTime());
    }

    // ==================== Exception paths ====================

    @Test(expected = ParseException.class)
    // Tests invalid string that cannot be parsed
    public void testParse_invalidString_throwsParseException() throws Exception {
        fmt.parse("not-a-date");
    }

    @Test(expected = ParseException.class)
    // Tests empty string
    public void testParse_emptyString_throwsParseException() throws Exception {
        fmt.parse("");
    }

    @Test(expected = NullPointerException.class)
    // Tests null input (not handled, throws NPE)
    public void testParse_nullString_throwsNullPointerException() throws Exception {
        fmt.parse(null);
    }

    @Test(expected = ParseException.class)
    // Tests invalid ISO-8601 date with wrong number of digits (len=9)
    public void testParse_invalidPlainDate_throwsParseException() throws Exception {
        fmt.parse("2019-01-1");
    }

    // ==================== Parsing with ParsePosition ====================

    @Test
    // Tests parse(String, ParsePosition) with valid input returns Date
    public void testParseWithPos_validInput_returnsDate() {
        ParsePosition pos = new ParsePosition(0);
        Date dt = fmt.parse("2019-01-01T00:00:00.000Z", pos);
        assertNotNull(dt);
        assertEquals(TIMESTAMP_2019_01_01, dt.getTime());
    }

    @Test
    // Tests parse(String, ParsePosition) with invalid input returns null
    public void testParseWithPos_invalidInput_returnsNull() {
        ParsePosition pos = new ParsePosition(0);
        Date dt = fmt.parse("not-a-date", pos);
        assertNull(dt);
    }

    // ==================== Format ====================

    @Test
    // Tests format produces ISO-8601 string with UTC timezone
    public void testFormat_validDate_returnsISO8601() throws Exception {
        sb.setLength(0);
        StringBuffer result = fmt.format(new Date(TIMESTAMP_2019_01_01), sb, fp);
        String expected = "2019-01-01T00:00:00.000+0000";
        assertEquals(expected, result.toString());
    }

    // ==================== Configuration ====================

    @Test
    // Tests withTimeZone returns new instance with changed timezone
    public void testWithTimeZone_differentTimeZone_returnsNewInstance() {
        TimeZone tz = TimeZone.getTimeZone("GMT+2");
        StdDateFormat fmt2 = fmt.withTimeZone(tz);
        assertNotSame(fmt, fmt2);
        assertEquals(tz, fmt2.getTimeZone());
    }

    @Test
    // Tests clone returns a new instance with same settings
    public void testClone_returnsEqualInstance() {
        StdDateFormat cloned = fmt.clone();
        assertNotSame(fmt, cloned);
        assertEquals(fmt.getTimeZone(), cloned.getTimeZone());
        assertEquals(fmt.isLenient(), cloned.isLenient());
    }

    @Test
    // Tests default leniency is true
    public void testIsLenient_defaultIsTrue() {
        assertTrue(fmt.isLenient());
    }

    @Test(expected = ParseException.class)
    // Tests non-lenient parsing rejects invalid dates like February 29 in non-leap year
    public void testParse_lenientFalse_invalidDate_throwsParseException() throws Exception {
        // create a non-lenient instance via withTimeZone (or constructor)
        StdDateFormat strictFmt = new StdDateFormat(null, Locale.US, Boolean.FALSE);
        strictFmt.parse("2019-02-29");
    }

    @Test
    // Tests setTimeZone changes timezone and clears cached formats
    public void testSetTimeZone_changesTimeZone() {
        TimeZone original = fmt.getTimeZone();
        TimeZone newTz = TimeZone.getTimeZone("GMT-5");
        fmt.setTimeZone(newTz);
        assertEquals(newTz, fmt.getTimeZone());
        // verify format uses new timezone
        sb.setLength(0);
        StringBuffer result = fmt.format(new Date(TIMESTAMP_2019_01_01), sb, fp);
        // UTC offset is "+0000", but with GMT-5 it should be "-0500"
        assertTrue(result.toString().endsWith("-0500"));
        // restore original for other tests (not strictly necessary)
    }

    // ==================== Additional edge cases ====================

    @Test
    // Tests parse with timestamp that has leading zeros (should still parse)
    public void testParse_timestampLeadingZeros_returnsDate() throws Exception {
        Date dt = fmt.parse("0000000000000");
        assertEquals(0L, dt.getTime());
    }

    @Test
    // Tests looksLikeISO8601 false path: non-digit start leads to RFC1123/timestamp fallback
    public void testParse_nonIsoString_usesFallback() throws Exception {
        // A string that is not ISO, not numeric, not RFC1123 -> throws exception
        try {
            fmt.parse("abc");
            fail("Expected ParseException");
        } catch (ParseException e) {
            // expected
        }
    }
}