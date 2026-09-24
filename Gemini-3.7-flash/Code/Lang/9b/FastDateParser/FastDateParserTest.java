package org.apache.commons.lang3.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class FastDateParserTest {

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final Locale US = Locale.US;

    // Tests standard date and time parsing with literal numbers
    @Test
    public void testParse_standardDatePattern_returnsCorrectDate() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd HH:mm:ss", GMT, US);
        Date parsed = parser.parse("2023-05-15 14:30:45");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.clear();
        cal.set(2023, Calendar.MAY, 15, 14, 30, 45);

        assertEquals(cal.getTime(), parsed);
    }

    // Tests 2-digit abbreviated year adjustment logic
    @Test
    public void testParse_abbreviatedYear_adjustsCorrectly() throws ParseException {
        FastDateParser parser = new FastDateParser("yy-MM-dd", GMT, US);
        Date parsed = parser.parse("23-05-15");

        Calendar cal = Calendar.getInstance(GMT, US);
        int currentYear = cal.get(Calendar.YEAR);
        int expectedCentury = currentYear - currentYear % 100;
        int expectedYear = 23 + expectedCentury;
        if (expectedYear >= currentYear + 20) {
            expectedYear -= 100;
        }

        cal.clear();
        cal.set(expectedYear, Calendar.MAY, 15);
        assertEquals(cal.getTime(), parsed);
    }

    // Tests text month, day of week, and timezone name strategy
    @Test
    public void testParse_textMonthAndDayOfWeek_returnsCorrectDate() throws ParseException {
        FastDateParser parser = new FastDateParser("EEE, dd MMM yyyy HH:mm:ss z", GMT, US);
        Date parsed = parser.parse("Mon, 23 Jan 2012 14:30:00 GMT");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.clear();
        cal.set(2012, Calendar.JANUARY, 23, 14, 30, 0);

        assertEquals(cal.getTime(), parsed);
    }

    // Tests quoted literal text and escaped single quotes
    @Test
    public void testParse_quotedLiteralsAndEscapedQuotes_matchesLiteralText() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy''MM''dd 'at' HH 'o''clock'", GMT, US);
        Date parsed = parser.parse("2023'05'15 at 10 o'clock");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.clear();
        cal.set(2023, Calendar.MAY, 15, 10, 0, 0);

        assertEquals(cal.getTime(), parsed);
    }

    // Tests regular expression special characters in the pattern string
    @Test
    public void testParse_specialRegexCharactersInPattern_escapesProperly() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy.MM.dd [HH:mm:ss] (z) {?+*^$|\\}", GMT, US);
        Date parsed = parser.parse("2023.05.15 [14:30:45] (GMT) {?+*^$|\\}");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.clear();
        cal.set(2023, Calendar.MAY, 15, 14, 30, 45);

        assertEquals(cal.getTime(), parsed);
    }

    // Tests parsing with ParsePosition advancing index and parsing partial strings
    @Test
    public void testParse_withParsePosition_advancesIndex() {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        ParsePosition pos = new ParsePosition(5);
        Date parsed = parser.parse("Date:2023-05-15 extra text", pos);

        assertNotNull(parsed);
        assertEquals(15, pos.getIndex());

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.clear();
        cal.set(2023, Calendar.MAY, 15);
        assertEquals(cal.getTime(), parsed);
    }

    // Tests parse returning null when string does not match at offset
    @Test
    public void testParse_nonMatchingInputWithParsePosition_returnsNull() {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date parsed = parser.parse("InvalidDate", pos);

        assertNull(parsed);
        assertEquals(0, pos.getIndex());
    }

    // Tests exception thrown on unparseable date input
    @Test(expected = ParseException.class)
    public void testParse_unparseableDate_throwsParseException() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        parser.parse("invalid-date-string");
    }

    // Tests invalid format pattern throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_emptyPattern_throwsIllegalArgumentException() {
        new FastDateParser("", GMT, US);
    }

    // Tests Japanese Imperial locale specific error message on parse failure
    @Test
    public void testParse_japaneseImperialLocaleFailure_containsCustomMessage() {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, FastDateParser.JAPANESE_IMPERIAL);
        try {
            parser.parse("invalid");
            fail("Expected ParseException");
        } catch (ParseException e) {
            assertTrue(e.getMessage().contains("The ja_JP_JP locale does not support dates before 1868 AD"));
        }
    }

    // Tests TimeZone offset strategy with numeric offset formats (+HH:mm, -HHmm)
    @Test
    public void testParse_numericTimeZoneOffsets_parsesCorrectly() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd HH:mm:ss Z", GMT, US);
        Date parsed = parser.parse("2023-05-15 14:30:00 +0200");

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+02:00"), US);
        cal.clear();
        cal.set(2023, Calendar.MAY, 15, 14, 30, 0);

        assertEquals(cal.getTime(), parsed);
    }

    // Tests AM/PM and 12-hour clock strategy
    @Test
    public void testParse_amPmAndModuloHour_parsesCorrectly() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd K:mm a", GMT, US);
        Date parsed = parser.parse("2023-05-15 11:30 PM");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.clear();
        cal.set(2023, Calendar.MAY, 15, 23, 30, 0);

        assertEquals(cal.getTime(), parsed);
    }

    // Tests Era (G), Day of Year (D), Millisecond (S), Week of Year (w) strategies
    @Test
    public void testParse_eraDayOfYearAndMilliseconds_parsesCorrectly() throws ParseException {
        FastDateParser parser = new FastDateParser("G yyyy D SSS", GMT, US);
        Date parsed = parser.parse("AD 2023 135 500");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.clear();
        cal.set(Calendar.ERA, GregorianCalendar.AD);
        cal.set(Calendar.YEAR, 2023);
        cal.set(Calendar.DAY_OF_YEAR, 135);
        cal.set(Calendar.MILLISECOND, 500);

        assertEquals(cal.getTime(), parsed);
    }

    // Tests parseObject method delegating to parse
    @Test
    public void testParseObject_validDate_returnsDateObject() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        Object obj1 = parser.parseObject("2023-05-15");
        assertTrue(obj1 instanceof Date);

        ParsePosition pos = new ParsePosition(0);
        Object obj2 = parser.parseObject("2023-05-15", pos);
        assertEquals(obj1, obj2);
        assertEquals(10, pos.getIndex());
    }

    // Tests equality, hashCode, toString, and accessors
    @Test
    public void testEqualsHashCodeToStringAndAccessors_validObjects_returnsExpected() {
        FastDateParser parser1 = new FastDateParser("yyyy-MM-dd", GMT, US);
        FastDateParser parser2 = new FastDateParser("yyyy-MM-dd", GMT, US);
        FastDateParser parser3 = new FastDateParser("yyyy/MM/dd", GMT, US);

        assertEquals(parser1, parser1);
        assertEquals(parser1, parser2);
        assertNotEquals(parser1, parser3);
        assertNotEquals(parser1, null);
        assertNotEquals(parser1, "Different Type");

        assertEquals(parser1.hashCode(), parser2.hashCode());
        assertEquals("yyyy-MM-dd", parser1.getPattern());
        assertEquals(GMT, parser1.getTimeZone());
        assertEquals(US, parser1.getLocale());
        assertNotNull(parser1.getParsePattern());
        assertEquals("FastDateParser[yyyy-MM-dd,en_US,GMT]", parser1.toString());
    }

    // Tests serialization and deserialization reconstructing strategies properly
    @Test
    public void testSerialization_roundTrip_maintainsFunctionality() throws Exception {
        FastDateParser original = new FastDateParser("yyyy-MM-dd HH:mm:ss", GMT, US);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FastDateParser deserialized = (FastDateParser) ois.readObject();
        ois.close();

        assertEquals(original, deserialized);

        Date originalDate = original.parse("2023-05-15 14:30:45");
        Date deserializedDate = deserialized.parse("2023-05-15 14:30:45");
        assertEquals(originalDate, deserializedDate);
    }

    // Tests additional date field strategies: Day of Week in Month (F), Week of Month (W), 1-based Hour (k, h)
    @Test
    public void testParse_additionalFieldStrategies_parsesCorrectly() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM W F k:h:s", GMT, US);
        Date parsed = parser.parse("2023-05 3 2 24:12:30");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.clear();
        cal.set(Calendar.YEAR, 2023);
        cal.set(Calendar.MONTH, Calendar.MAY);
        cal.set(Calendar.WEEK_OF_MONTH, 3);
        cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, 2);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.SECOND, 30);

        assertEquals(cal.getTime(), parsed);
    }
}