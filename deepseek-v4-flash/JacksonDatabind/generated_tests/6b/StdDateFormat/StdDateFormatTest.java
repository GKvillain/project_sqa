package com.fasterxml.jackson.databind.util;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

public class StdDateFormatTest {

    // Helper: create a known date in GMT
    private Date date(long millis) {
        return new Date(millis);
    }

    // Test parse of a standard ISO8601 date with explicit milliseconds and 'Z'
    @Test
    public void testParse_validISO8601WithZ_returnsDate() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        Date result = fmt.parse("2014-10-01T12:00:00.000Z");
        assertNotNull(result);
        // Expected: 2014-10-01 12:00:00 GMT = 1412164800000
        assertEquals(1412164800000L, result.getTime());
    }

    // Test parse of ISO8601 with timezone offset (no colon)
    @Test
    public void testParse_validISO8601WithOffset_returnsDate() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        Date result = fmt.parse("2014-10-01T12:00:00.000+0100");
        assertNotNull(result);
        // 2014-10-01 12:00:00 +0100 = 11:00 GMT = 1412161200000
        assertEquals(1412161200000L, result.getTime());
    }

    // Test parse of ISO8601 with timezone offset containing colon (should be handled)
    @Test
    public void testParse_validISO8601WithOffsetColon_returnsDate() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        Date result = fmt.parse("2014-10-01T12:00:00.000+01:00");
        assertNotNull(result);
        assertEquals(1412161200000L, result.getTime());
    }

    // Test parse of ISO8601 missing milliseconds with 'Z' (should add .000)
    @Test
    public void testParse_ISO8601MissingMillisWithZ_returnsDate() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        Date result = fmt.parse("2014-10-01T12:00:00Z");
        assertNotNull(result);
        assertEquals(1412164800000L, result.getTime());
    }

    // Test parse of ISO8601 missing milliseconds with offset (should add .000)
    @Test
    public void testParse_ISO8601MissingMillisWithOffset_returnsDate() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        Date result = fmt.parse("2014-10-01T12:00:00+0100");
        assertNotNull(result);
        assertEquals(1412161200000L, result.getTime());
    }

    // Test parse of plain date (yyyy-MM-dd)
    @Test
    public void testParse_validPlainDate_returnsDate() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        Date result = fmt.parse("2014-10-01");
        assertNotNull(result);
        // Plain date implies time 00:00:00.000 GMT
        assertEquals(1412121600000L, result.getTime());
    }

    // Test parse of RFC1123 date
    @Test
    public void testParse_validRFC1123_returnsDate() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        Date result = fmt.parse("Wed, 01 Oct 2014 12:00:00 GMT");
        assertNotNull(result);
        assertEquals(1412164800000L, result.getTime());
    }

    // Test parse of a numeric timestamp (positive)
    @Test
    public void testParse_validPositiveTimestamp_returnsDate() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        Date result = fmt.parse("1412164800000");
        assertNotNull(result);
        assertEquals(1412164800000L, result.getTime());
    }

    // Test parse of a negative timestamp
    @Test
    public void testParse_validNegativeTimestamp_returnsDate() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        Date result = fmt.parse("-1234567890");
        assertNotNull(result);
        assertEquals(-1234567890L, result.getTime());
    }

    // Test parse of an invalid string that looks like ISO8601 but is not
    @Test(expected = ParseException.class)
    public void testParse_invalidISO8601Like_throwsParseException() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        fmt.parse("1234-56-78");
    }

    // Test parse of a completely invalid string
    @Test(expected = ParseException.class)
    public void testParse_invalidString_throwsParseException() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        fmt.parse("not a date");
    }

    // Test parse of empty string (trims to empty)
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testParse_emptyString_throwsIndexOutOfBounds() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        fmt.parse("   ");
    }

    // Test parse of null input (calls parse(String))
    @Test(expected = NullPointerException.class)
    public void testParse_nullString_throwsNullPointer() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        fmt.parse((String) null);
    }

    // Test format of a known date – should produce ISO8601 string in GMT
    @Test
    public void testFormat_validDate_returnsISO8601String() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        Date date = date(1412164800000L); // 2014-10-01 12:00:00 GMT
        String result = fmt.format(date);
        // The exact string depends on JDK generation, but should start with "2014-10-01T12:00:00.000+0000" or "Z"? 
        // Actually format uses _formatISO8601 which is "yyyy-MM-dd'T'HH:mm:ss.SSSZ" producing "+0000"
        assertTrue(result.startsWith("2014-10-01T12:00:00.000"));
        assertTrue(result.endsWith("+0000") || result.endsWith("Z"));
    }

    // Test clone creates a new instance
    @Test
    public void testClone_returnsNewInstance() {
        StdDateFormat fmt = new StdDateFormat();
        StdDateFormat clone = fmt.clone();
        assertNotNull(clone);
        assertNotSame(fmt, clone);
    }

    // Test withTimeZone with same timezone returns same instance
    @Test
    public void testWithTimeZone_sameTimeZone_returnsThis() {
        StdDateFormat fmt = new StdDateFormat(TimeZone.getTimeZone("GMT"), Locale.US);
        StdDateFormat result = fmt.withTimeZone(TimeZone.getTimeZone("GMT"));
        assertSame(fmt, result);
    }

    // Test withTimeZone with different timezone returns new instance
    @Test
    public void testWithTimeZone_differentTimeZone_returnsNew() {
        StdDateFormat fmt = new StdDateFormat(TimeZone.getTimeZone("GMT"), Locale.US);
        StdDateFormat result = fmt.withTimeZone(TimeZone.getTimeZone("America/New_York"));
        assertNotNull(result);
        assertNotSame(fmt, result);
    }

    // Test setTimeZone resets internal format instances
    @Test
    public void testSetTimeZone_resetsFormats() {
        StdDateFormat fmt = new StdDateFormat();
        // Access format by calling format (creates internal _formatISO8601)
        fmt.format(new Date());
        // Verify that after setTimeZone, next format uses new timezone
        fmt.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        // Calling format again should produce different timezone
        String result = fmt.format(date(1412164800000L));
        // Should now be -0500 or -0400 depending on DST
        assertTrue(result.contains("-04") || result.contains("-05"));
    }

    // Test parse of ISO8601 with timezone offset but missing minutes (e.g., +01)
    @Test
    public void testParse_ISO8601OffsetMissingMinutes_returnsDate() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        Date result = fmt.parse("2014-10-01T12:00:00.000+01");
        assertNotNull(result);
        // +01 means +0100, so 11:00 GMT
        assertEquals(1412161200000L, result.getTime());
    }

    // Test parse of ISO8601 without timezone (should append Z)
    @Test
    public void testParse_ISO8601NoTimeZone_returnsDate() throws Exception {
        StdDateFormat fmt = new StdDateFormat();
        Date result = fmt.parse("2014-10-01T12:00:00");
        assertNotNull(result);
        // Appended Z => 12:00 GMT
        assertEquals(1412164800000L, result.getTime());
    }
}