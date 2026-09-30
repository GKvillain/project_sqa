package org.apache.commons.lang3.time;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FastDateParserTest {

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final TimeZone EST = TimeZone.getTimeZone("EST");
    private static final Locale US = Locale.US;

    // Tests normal parsing with standard date-time pattern
    @Test
    public void testParse_standardDateTimePattern_returnsCorrectDate() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd HH:mm:ss", GMT, US);
        Date parsed = parser.parse("2023-10-15 14:30:45");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.clear();
        cal.set(2023, Calendar.OCTOBER, 15, 14, 30, 45);

        assertEquals(cal.getTime(), parsed);
    }

    // Tests 2-digit year parsing and 80-20 year adjustment window
    @Test
    public void testParse_twoDigitYear_adjustsCorrectly() throws ParseException {
        FastDateParser parser = new FastDateParser("yy-MM-dd", GMT, US);
        Date parsed = parser.parse("21-05-10");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.clear();
        int expectedYear = parser.adjustYear(21);
        cal.set(expectedYear, Calendar.MAY, 10);

        assertEquals(cal.getTime(), parsed);
    }

    // Tests 4-digit literal year parsing
    @Test
    public void testParse_fourDigitYear_setsExactYear() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy/MM/dd", GMT, US);
        Date parsed = parser.parse("1995/12/31");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.clear();
        cal.set(1995, Calendar.DECEMBER, 31);

        assertEquals(cal.getTime(), parsed);
    }

    // Tests text month parsing (both short and long month names)
    @Test
    public void testParse_textMonth_parsesLongAndShortNames() throws ParseException {
        FastDateParser parserLong = new FastDateParser("MMMM dd, yyyy", GMT, US);
        Date parsedLong = parserLong.parse("January 15, 2020");

        FastDateParser parserShort = new FastDateParser("MMM dd, yyyy", GMT, US);
        Date parsedShort = parserShort.parse("Jan 15, 2020");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.clear();
        cal.set(2020, Calendar.JANUARY, 15);

        assertEquals(cal.getTime(), parsedLong);
        assertEquals(cal.getTime(), parsedShort);
    }

    // Tests AM/PM and 12-hour format strategies
    @Test
    public void testParse_amPmAnd12Hour_returnsCorrectTime() throws ParseException {
        FastDateParser parser = new FastDateParser("h:mm:ss a", GMT, US);
        Date parsedPm = parser.parse("1:30:15 PM");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.clear();
        cal.set(Calendar.HOUR, 1);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 15);
        cal.set(Calendar.AM_PM, Calendar.PM);

        assertEquals(cal.getTime(), parsedPm);
    }

    // Tests day of week, day of year, week of year and week of month strategies
    @Test
    public void testParse_calendarFieldsStrategies_setsValuesCorrectly() throws ParseException {
        FastDateParser parser = new FastDateParser("E D F w W K k H s S", GMT, US);
        Date parsed = parser.parse("Mon 200 3 29 3 10 14 14 30 500");
        assertNotNull(parsed);
    }

    // Tests parsing with TimeZone strategy (GMT offset and named zone)
    @Test
    public void testParse_timeZoneFormats_parsesZoneOffsets() throws ParseException {
        FastDateParser parserZ = new FastDateParser("yyyy-MM-dd z", GMT, US);
        Date parsedGmt = parserZ.parse("2021-08-01 GMT+02:00");
        assertNotNull(parsedGmt);

        Date parsedNamed = parserZ.parse("2021-08-01 EST");
        assertNotNull(parsedNamed);
    }

    // Tests quoted literal text and escaped single quotes in pattern
    @Test
    public void testParse_quotedLiteralsAndQuotes_matchesExactLiteral() throws ParseException {
        FastDateParser parser = new FastDateParser("''yyyy'' 'at' HH'h'mm", GMT, US);
        Date parsed = parser.parse("'2022' at 10h45");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.clear();
        cal.set(2022, Calendar.JANUARY, 1, 10, 45, 0);

        assertEquals(cal.getTime(), parsed);
    }

    // Tests regex special characters in literal pattern (escaped properly)
    @Test
    public void testParse_regexSpecialCharactersInPattern_parsesSuccessfully() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd [HH?mm.ss]*", GMT, US);
        Date parsed = parser.parse("2023-04-10 [12?30.15]*");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.clear();
        cal.set(2023, Calendar.APRIL, 10, 12, 30, 15);

        assertEquals(cal.getTime(), parsed);
    }

    // Tests parse using ParsePosition offset and index update
    @Test
    public void testParse_withParsePosition_advancesIndex() {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        String text = "Prefix 2021-07-20 Suffix";
        ParsePosition pos = new ParsePosition(7);

        Date parsed = parser.parse(text, pos);
        assertNotNull(parsed);
        assertEquals(17, pos.getIndex());
    }

    // Tests parse with invalid input returning null for ParsePosition and throwing ParseException for parse(String)
    @Test
    public void testParse_unparseableInput_returnsNullOrThrowsException() {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date result = parser.parse("invalid-date", pos);
        assertNull(result);
        assertEquals(0, pos.getIndex());
    }

    // Tests ParseException thrown when parsing unparseable string
    @Test(expected = ParseException.class)
    public void testParse_invalidDateString_throwsParseException() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        parser.parse("not-a-date");
    }

    // Tests invalid pattern syntax throwing IllegalArgumentException during initialization
    @Test(expected = IllegalArgumentException.class)
    public void testInit_invalidPattern_throwsIllegalArgumentException() {
        new FastDateParser("", GMT, US);
    }

    // Tests parseObject method delegations
    @Test
    public void testParseObject_delegatesToParse() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        Object obj1 = parser.parseObject("2020-01-01");
        assertTrue(obj1 instanceof Date);

        ParsePosition pos = new ParsePosition(0);
        Object obj2 = parser.parseObject("2020-01-01", pos);
        assertEquals(obj1, obj2);
    }

    // Tests accessors getPattern, getTimeZone, getLocale
    @Test
    public void testAccessors_getPatternTimeZoneLocale_returnConfiguredValues() {
        FastDateParser parser = new FastDateParser("yyyy/MM/dd", EST, Locale.GERMANY);
        assertEquals("yyyy/MM/dd", parser.getPattern());
        assertEquals(EST, parser.getTimeZone());
        assertEquals(Locale.GERMANY, parser.getLocale());
        assertNotNull(parser.getParsePattern());
    }

    // Tests equals, hashCode and toString consistency
    @Test
    public void testEqualsAndHashCode_sameAndDifferentInstances_behaveCorrectly() {
        FastDateParser parser1 = new FastDateParser("yyyy-MM-dd", GMT, US);
        FastDateParser parser2 = new FastDateParser("yyyy-MM-dd", GMT, US);
        FastDateParser parserDiffPattern = new FastDateParser("yyyy/MM/dd", GMT, US);
        FastDateParser parserDiffZone = new FastDateParser("yyyy-MM-dd", EST, US);
        FastDateParser parserDiffLocale = new FastDateParser("yyyy-MM-dd", GMT, Locale.GERMAN);

        assertTrue(parser1.equals(parser1));
        assertTrue(parser1.equals(parser2));
        assertEquals(parser1.hashCode(), parser2.hashCode());

        assertFalse(parser1.equals(parserDiffPattern));
        assertFalse(parser1.equals(parserDiffZone));
        assertFalse(parser1.equals(parserDiffLocale));
        assertFalse(parser1.equals(null));
        assertFalse(parser1.equals("different-type"));

        assertEquals("FastDateParser[yyyy-MM-dd,en_US,GMT]", parser1.toString());
    }

    // Tests serialization and deserialization reinitializing transient fields
    @Test
    public void testSerialization_roundTrip_preservesBehavior() throws Exception {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd HH:mm:ss", GMT, US);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(parser);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        FastDateParser deserialized = (FastDateParser) ois.readObject();
        ois.close();

        assertEquals(parser, deserialized);
        Date expected = parser.parse("2022-11-25 18:00:00");
        Date actual = deserialized.parse("2022-11-25 18:00:00");
        assertEquals(expected, actual);
    }

    // Tests Japanese Imperial locale support
    @Test
    public void testParse_japaneseImperialLocale_initializesAndParses() {
        FastDateParser parser = new FastDateParser("GGGG yyyy-MM-dd", GMT, FastDateParser.JAPANESE_IMPERIAL);
        assertNotNull(parser.getParsePattern());
    }

    // Tests constructor with custom centuryStart
    @Test
    public void testConstructor_withCenturyStart_setsCenturyWindow() throws ParseException {
        Calendar cal = Calendar.getInstance(GMT, US);
        cal.set(1900, Calendar.JANUARY, 1);
        Date centuryStart = cal.getTime();

        FastDateParser parser = new FastDateParser("yy-MM-dd", GMT, US, centuryStart);
        Date parsed = parser.parse("50-01-01");

        cal.clear();
        cal.set(1950, Calendar.JANUARY, 1);
        assertEquals(cal.getTime(), parsed);
    }

    // Tests parsing with ISO-8601 'Z' TimeZone strategy
    @Test
    public void testParse_isoTimeZoneZ_parsesSuccessfully() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd'T'HH:mm:ssZZ", GMT, US);
        Date parsed = parser.parse("2023-01-01T12:00:00+0000");
        assertNotNull(parsed);

        Date parsedZ = parser.parse("2023-01-01T12:00:00Z");
        assertNotNull(parsedZ);
    }

    // Tests parsing era strategy (AD / BC)
    @Test
    public void testParse_eraStrategy_setsEra() throws ParseException {
        FastDateParser parser = new FastDateParser("G yyyy-MM-dd", GMT, US);
        Date parsedAd = parser.parse("AD 2020-01-01");
        assertNotNull(parsedAd);

        Date parsedBc = parser.parse("BC 0044-03-15");
        assertNotNull(parsedBc);
    }
}