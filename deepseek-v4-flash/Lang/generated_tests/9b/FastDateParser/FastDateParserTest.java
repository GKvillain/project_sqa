package org.apache.commons.lang3.time;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

/**
 * JUnit 4 tests for FastDateParser, targeting the Defects4J bug #9b
 * which affects parsing of time zone offsets like "GMT+09:30".
 *
 * The test suite focuses on the most relevant paths:
 * - time zone parsing (both named and GMT offsets)
 * - standard date parsing
 * - month/day-of-week text handling
 * - two-digit year adjustment
 * - error handling and parse position semantics
 */
public class FastDateParserTest {

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final Locale US = Locale.US;

    private FastDateParser createParser(String pattern) {
        return new FastDateParser(pattern, GMT, US);
    }

    private Calendar calendarFor(TimeZone tz, int year, int month, int day, int hour, int minute, int second) {
        Calendar cal = Calendar.getInstance(tz, US);
        cal.clear();
        cal.set(year, month, day, hour, minute, second);
        return cal;
    }

    // ----------------------------------------------------------------
    // Time zone parsing – these tests exercise the defect in TimeZoneStrategy
    // ----------------------------------------------------------------

    // Tests parsing a GMT offset with a colon (e.g. "GMT+09:30")
    // This case was broken before the fix: only "GMT+09" was captured.
    @Test
    public void testParse_gmtOffsetWithColon_returnsCorrectTime() throws Exception {
        FastDateParser parser = createParser("yyyy-MM-dd HH:mm:ss z");
        Date date = parser.parse("2015-12-31 23:30:00 GMT+09:30");

        Calendar expected = calendarFor(TimeZone.getTimeZone("GMT+09:30"),
                2015, Calendar.DECEMBER, 31, 23, 30, 0);
        assertEquals(expected.getTimeInMillis(), date.getTime());
    }

    // Tests parsing a GMT offset without a colon (e.g. "GMT+0530")
    // This also triggers the same incomplete capture as the colon case.
    @Test
    public void testParse_gmtOffsetNoColon_returnsCorrectTime() throws Exception {
        FastDateParser parser = createParser("yyyy-MM-dd HH:mm:ss z");
        Date date = parser.parse("2015-12-31 23:30:00 GMT+0530");

        Calendar expected = calendarFor(TimeZone.getTimeZone("GMT+05:30"),
                2015, Calendar.DECEMBER, 31, 23, 30, 0);
        assertEquals(expected.getTimeInMillis(), date.getTime());
    }

    // Tests parsing a named time zone abbreviation (PST)
    @Test
    public void testParse_timeZoneAbbreviation_returnsCorrectDate() throws Exception {
        FastDateParser parser = createParser("yyyy-MM-dd HH:mm:ss z");
        Date date = parser.parse("2015-12-31 12:00:00 PST");

        Calendar expected = calendarFor(TimeZone.getTimeZone("America/Los_Angeles"),
                2015, Calendar.DECEMBER, 31, 12, 0, 0);
        assertEquals(expected.getTimeInMillis(), date.getTime());
    }

    // Tests parsing a time zone offset in the form "+09:30" (without GMT prefix)
    @Test
    public void testParse_plusOffset_returnsCorrectTime() throws Exception {
        FastDateParser parser = createParser("yyyy-MM-dd HH:mm:ss z");
        Date date = parser.parse("2015-12-31 23:30:00 +09:30");

        Calendar expected = calendarFor(TimeZone.getTimeZone("GMT+09:30"),
                2015, Calendar.DECEMBER, 31, 23, 30, 0);
        assertEquals(expected.getTimeInMillis(), date.getTime());
    }

    // ----------------------------------------------------------------
    // Regular date parsing
    // ----------------------------------------------------------------

    // Tests a simple date pattern
    @Test
    public void testParse_simpleDate_returnsCorrectDate() throws Exception {
        FastDateParser parser = createParser("yyyy-MM-dd");
        Date date = parser.parse("2015-12-31");

        Calendar expected = calendarFor(GMT, 2015, Calendar.DECEMBER, 31, 0, 0, 0);
        assertEquals(expected.getTime(), date);
    }

    // Tests month short names, e.g. "Jan"
    @Test
    public void testParse_monthShortName_returnsCorrectMonth() throws Exception {
        FastDateParser parser = createParser("yyyy-MMM");
        Date date = parser.parse("2015-Jan");

        Calendar expected = calendarFor(GMT, 2015, Calendar.JANUARY, 1, 0, 0, 0);
        assertEquals(expected.getTime(), date);
    }

    // Tests day-of-week short names, e.g. "Thu"
    @Test
    public void testParse_dayOfWeekShortName_returnsCorrectDay() throws Exception {
        FastDateParser parser = createParser("yyyy-MM-dd EEE");
        Date date = parser.parse("2015-12-31 Thu");

        Calendar expected = calendarFor(GMT, 2015, Calendar.DECEMBER, 31, 0, 0, 0);
        assertEquals(expected.getTime(), date);
    }

    // Tests ERA handling (AD/BC)
    @Test
    public void testParse_eraAD_returnsCorrectYear() throws Exception {
        FastDateParser parser = createParser("G yyyy");
        Date date = parser.parse("AD 2015");

        Calendar expected = Calendar.getInstance(GMT, US);
        expected.clear();
        expected.set(Calendar.ERA, GregorianCalendar.AD);
        expected.set(2015, Calendar.JANUARY, 1, 0, 0, 0);
        assertEquals(expected.getTime(), date);
    }

    // Tests AM/PM handling
    @Test
    public void testParse_amPm_returnsCorrectHour() throws Exception {
        FastDateParser parser = createParser("hh a");
        Date date = parser.parse("10 PM");

        Calendar expected = Calendar.getInstance(GMT, US);
        expected.clear();
        expected.set(Calendar.HOUR, 10);
        expected.set(Calendar.AM_PM, Calendar.PM);
        expected.set(Calendar.MINUTE, 0);
        expected.set(Calendar.SECOND, 0);
        expected.set(Calendar.MILLISECOND, 0);
        assertEquals(expected.getTime(), date);
    }

    // Tests two-digit year parsing takes adjustYear() into account
    @Test
    public void testParse_twoDigitYear_usesAdjustedYear() throws Exception {
        FastDateParser parser = createParser("yy");
        Date date = parser.parse("15");

        int expectedYear = parser.adjustYear(15);
        Calendar expected = calendarFor(GMT, expectedYear, Calendar.JANUARY, 1, 0, 0, 0);
        assertEquals(expected.getTime(), date);
    }

    // ----------------------------------------------------------------
    // adjustYear() boundary tests
    // ----------------------------------------------------------------

    // Tests year that should stay in the forward window (less than +20)
    @Test
    public void testAdjustYear_beforeYearPlus20_returnsAdjustedYear() {
        FastDateParser parser = createParser("yy");
        int thisYear = Calendar.getInstance(GMT, US).get(Calendar.YEAR);
        int futureBound = thisYear + 20;
        int candidate = 43;
        int trial = candidate + thisYear - thisYear % 100;
        int expected = trial < futureBound ? trial : trial - 100;
        assertEquals(expected, parser.adjustYear(candidate));
    }

    // Tests year that falls exactly at the +20 boundary (must be pushed back 100)
    @Test
    public void testAdjustYear_atYearPlus20_returnsPreviousCentury() {
        FastDateParser parser = createParser("yy");
        int thisYear = Calendar.getInstance(GMT, US).get(Calendar.YEAR);
        int futureBound = thisYear + 20;
        int candidate = 44;
        int trial = candidate + thisYear - thisYear % 100;
        int expected = trial < futureBound ? trial : trial - 100;
        assertEquals(expected, parser.adjustYear(candidate));
    }

    // ----------------------------------------------------------------
    // ParsePosition handling
    // ----------------------------------------------------------------

    // Tests parse(String, ParsePosition) updates the index
    @Test
    public void testParse_parsePosition_updatesIndex() throws Exception {
        FastDateParser parser = createParser("yyyy");
        ParsePosition pos = new ParsePosition(0);
        Date date = parser.parse("2015-extra", pos);

        assertNotNull(date);
        assertEquals(4, pos.getIndex());
        Calendar expected = calendarFor(GMT, 2015, Calendar.JANUARY, 1, 0, 0, 0);
        assertEquals(expected.getTime(), date);
    }

    // Tests parse(String, ParsePosition) returns null and leaves index unchanged on failure
    @Test
    public void testParse_parsePosition_noMatch_returnsNull() {
        FastDateParser parser = createParser("yyyy");
        ParsePosition pos = new ParsePosition(0);
        Date date = parser.parse("not-a-year", pos);

        assertNull(date);
        assertEquals(0, pos.getIndex());
    }

    // ----------------------------------------------------------------
    // Error handling
    // ----------------------------------------------------------------

    // Tests invalid pattern is rejected at construction time
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_invalidPattern_throwsException() {
        new FastDateParser("abc", GMT, US);
    }

    // Tests parse throws ParseException for a non-matching input
    @Test(expected = ParseException.class)
    public void testParse_invalidInput_throwsParseException() throws Exception {
        createParser("yyyy-MM-dd").parse("not-a-date");
    }

    // Tests parse throws ParseException for an empty string
    @Test(expected = ParseException.class)
    public void testParse_emptyString_throwsParseException() throws Exception {
        createParser("yyyy").parse("");
    }

    // ----------------------------------------------------------------
    // Object identity and string representation
    // ----------------------------------------------------------------

    // Tests equals returns true for identical parser fields
    @Test
    public void testEquals_sameFields_returnsTrue() {
        FastDateParser p1 = createParser("yyyy-MM-dd");
        FastDateParser p2 = createParser("yyyy-MM-dd");
        assertTrue(p1.equals(p2));
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    // Tests toString contains the pattern, locale, and time zone ID
    @Test
    public void testToString_containsPatternInfo() {
        FastDateParser parser = createParser("yyyy-MM-dd");
        String s = parser.toString();
        assertTrue(s.contains("yyyy-MM-dd"));
        assertTrue(s.contains("GMT"));
        assertTrue(s.contains(US.toString()));
    }
}