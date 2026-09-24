package org.apache.commons.lang3.time;

import org.junit.Before;
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

    private Calendar cal;

    @Before
    public void setUp() {
        cal = Calendar.getInstance(GMT, US);
        cal.clear();
    }

    // Tests normal parsing with standard numeric date and time pattern
    @Test
    public void testParse_standardNumericPattern_returnsCorrectDate() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd HH:mm:ss.SSS", GMT, US);
        Date parsed = parser.parse("2023-10-15 14:30:45.123");

        cal.set(2023, Calendar.OCTOBER, 15, 14, 30, 45);
        cal.set(Calendar.MILLISECOND, 123);
        assertEquals(cal.getTime(), parsed);
    }

    // Tests abbreviated 2-digit year logic within century boundary
    @Test
    public void testParse_abbreviatedYear_adjustsCorrectCentury() throws ParseException {
        FastDateParser parser = new FastDateParser("yy-MM-dd", GMT, US);
        Date parsed = parser.parse("23-05-10");

        cal.set(2023, Calendar.MAY, 10, 0, 0, 0);
        assertEquals(cal.getTime(), parsed);
    }

    // Tests text strategy for textual months and days of week
    @Test
    public void testParse_textualMonthAndDayOfWeek_returnsCorrectDate() throws ParseException {
        FastDateParser parser = new FastDateParser("EEEE, MMMM d, yyyy", GMT, US);
        Date parsed = parser.parse("Sunday, October 15, 2023");

        cal.set(2023, Calendar.OCTOBER, 15, 0, 0, 0);
        assertEquals(cal.getTime(), parsed);

        FastDateParser shortParser = new FastDateParser("EEE, MMM d, yyyy", GMT, US);
        Date shortParsed = shortParser.parse("Sun, Oct 15, 2023");
        assertEquals(cal.getTime(), shortParsed);
    }

    // Tests AM/PM and 12-hour clock strategies (h, K, a)
    @Test
    public void testParse_amPmAnd12HourClock_returnsCorrectDate() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd K:mm:ss a", GMT, US);
        Date parsed = parser.parse("2023-10-15 2:30:00 PM");

        cal.set(2023, Calendar.OCTOBER, 15, 14, 30, 0);
        assertEquals(cal.getTime(), parsed);

        FastDateParser parserH = new FastDateParser("yyyy-MM-dd h:mm a", GMT, US);
        Date parsedH = parserH.parse("2023-10-15 12:30 PM");
        cal.set(2023, Calendar.OCTOBER, 15, 12, 30, 0);
        assertEquals(cal.getTime(), parsedH);
    }

    // Tests era and week-based strategies (G, w, W, D, F, k)
    @Test
    public void testParse_eraAndWeekStrategies_returnsCorrectDate() throws ParseException {
        FastDateParser parser = new FastDateParser("G yyyy D k", GMT, US);
        Date parsed = parser.parse("AD 2023 100 24");

        cal.set(Calendar.ERA, GregorianCalendarUtils(2023).get(Calendar.ERA));
        cal.set(Calendar.YEAR, 2023);
        cal.set(Calendar.DAY_OF_YEAR, 100);
        cal.set(Calendar.HOUR_OF_DAY, 24);
        assertEquals(cal.getTime(), parsed);
    }

    private Calendar GregorianCalendarUtils(int year) {
        Calendar c = Calendar.getInstance(GMT, US);
        c.clear();
        c.set(Calendar.YEAR, year);
        return c;
    }

    // Tests timezone strategy parsing offset and named timezones
    @Test
    public void testParse_timezoneStrategy_parsesTimeZoneCorrectly() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd HH:mm:ss z", GMT, US);
        Date parsedGmt = parser.parse("2023-10-15 14:30:00 GMT");

        cal.setTimeZone(GMT);
        cal.set(2023, Calendar.OCTOBER, 15, 14, 30, 0);
        assertEquals(cal.getTime(), parsedGmt);

        FastDateParser parserZ = new FastDateParser("yyyy-MM-dd HH:mm:ss Z", GMT, US);
        Date parsedOffset = parserZ.parse("2023-10-15 14:30:00 -0400");
        Calendar calEst = Calendar.getInstance(TimeZone.getTimeZone("GMT-0400"), US);
        calEst.clear();
        calEst.set(2023, Calendar.OCTOBER, 15, 14, 30, 0);
        assertEquals(calEst.getTime(), parsedOffset);
    }

    // Tests quoted literal text and escaped quote handling
    @Test
    public void testParse_quotedLiteralAndSpecialChars_parsesSuccessfully() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy 'year' ''MM'' dd", GMT, US);
        Date parsed = parser.parse("2023 year '10' 15");

        cal.set(2023, Calendar.OCTOBER, 15, 0, 0, 0);
        assertEquals(cal.getTime(), parsed);
    }

    // Tests parse with ParsePosition and offset
    @Test
    public void testParse_withParsePosition_updatesIndexAndParses() {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        String text = "Prefix 2023-10-15 Suffix";
        ParsePosition pos = new ParsePosition(7);
        Date parsed = parser.parse(text, pos);

        assertNotNull(parsed);
        assertEquals(17, pos.getIndex());
        cal.set(2023, Calendar.OCTOBER, 15, 0, 0, 0);
        assertEquals(cal.getTime(), parsed);
    }

    // Tests parse with ParsePosition returning null on unmatched input
    @Test
    public void testParse_withInvalidPosition_returnsNull() {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date parsed = parser.parse("invalid-date", pos);

        assertNull(parsed);
        assertEquals(0, pos.getIndex());
    }

    // Tests parseObject method delegations
    @Test
    public void testParseObject_validStringAndPosition_returnsDate() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        Object obj1 = parser.parseObject("2023-10-15");
        assertNotNull(obj1);

        ParsePosition pos = new ParsePosition(0);
        Object obj2 = parser.parseObject("2023-10-15", pos);
        assertEquals(obj1, obj2);
        assertEquals(10, pos.getIndex());
    }

    // Tests exception path for unparseable input
    @Test(expected = ParseException.class)
    public void testParse_unparseableString_throwsParseException() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        parser.parse("not-a-date");
    }

    // Tests exception path for invalid format pattern
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_invalidPattern_throwsIllegalArgumentException() {
        new FastDateParser("", GMT, US);
    }

    // Tests Japanese Imperial locale exception note
    @Test
    public void testParse_japaneseImperialLocale_throwsExceptionWithSpecificMessage() {
        Locale jaJpJp = new Locale("ja", "JP", "JP");
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, jaJpJp);
        try {
            parser.parse("invalid");
            org.junit.Assert.fail("Expected ParseException");
        } catch (ParseException e) {
            assertTrue(e.getMessage().contains("The ja_JP_JP locale does not support dates before 1868 AD"));
        }
    }

    // Tests getters for pattern, timeZone, locale, and parsePattern
    @Test
    public void testGetters_returnCorrectConfiguredValues() {
        String pattern = "yyyy-MM-dd HH:mm";
        FastDateParser parser = new FastDateParser(pattern, EST, Locale.GERMANY);

        assertEquals(pattern, parser.getPattern());
        assertEquals(EST, parser.getTimeZone());
        assertEquals(Locale.GERMANY, parser.getLocale());
        assertNotNull(parser.getParsePattern());
    }

    // Tests equals, hashCode, and toString contracts
    @Test
    public void testEqualsAndHashCodeAndToString() {
        FastDateParser parser1 = new FastDateParser("yyyy-MM-dd", GMT, US);
        FastDateParser parser2 = new FastDateParser("yyyy-MM-dd", GMT, US);
        FastDateParser parserDiffPattern = new FastDateParser("yyyy/MM/dd", GMT, US);
        FastDateParser parserDiffTz = new FastDateParser("yyyy-MM-dd", EST, US);
        FastDateParser parserDiffLocale = new FastDateParser("yyyy-MM-dd", GMT, Locale.FRANCE);

        assertEquals(parser1, parser1);
        assertEquals(parser1, parser2);
        assertEquals(parser1.hashCode(), parser2.hashCode());

        assertFalse(parser1.equals(null));
        assertFalse(parser1.equals("different type"));
        assertFalse(parser1.equals(parserDiffPattern));
        assertFalse(parser1.equals(parserDiffTz));
        assertFalse(parser1.equals(parserDiffLocale));

        String str = parser1.toString();
        assertTrue(str.contains("yyyy-MM-dd"));
        assertTrue(str.contains("GMT"));
    }

    // Tests serialization and deserialization reconstructing parser correctly
    @Test
    public void testSerialization_roundTrip_preservesParsingBehavior() throws Exception {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd HH:mm:ss", GMT, US);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(parser);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FastDateParser deserialized = (FastDateParser) ois.readObject();

        assertEquals(parser, deserialized);

        Date originalParsed = parser.parse("2023-10-15 12:00:00");
        Date deserializedParsed = deserialized.parse("2023-10-15 12:00:00");
        assertEquals(originalParsed, deserializedParsed);
    }
}