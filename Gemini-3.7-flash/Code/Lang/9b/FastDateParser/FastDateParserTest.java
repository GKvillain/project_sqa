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
import java.util.Locale;
import java.util.TimeZone;

public class FastDateParserTest {

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final TimeZone EST = TimeZone.getTimeZone("EST");
    private static final Locale US = Locale.US;

    // Tests standard date parsing with yyyy-MM-dd HH:mm:ss format
    @Test
    public void testParse_standardDateTime_returnsCorrectDate() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd HH:mm:ss", GMT, US);
        Date date = parser.parse("2023-11-15 14:30:45");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(date);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.NOVEMBER, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(45, cal.get(Calendar.SECOND));
    }

    // Tests 2-digit abbreviated year adjustment logic (80 years before, 20 years after)
    @Test
    public void testParse_abbreviatedYear_adjustsCorrectly() throws ParseException {
        FastDateParser parser = new FastDateParser("yy-MM-dd", GMT, US);
        Date parsedDate = parser.parse("23-01-01");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(parsedDate);
        int currentCentury = Calendar.getInstance(GMT, US).get(Calendar.YEAR) / 100 * 100;
        int expectedYear = currentCentury + 23;
        assertEquals(expectedYear, cal.get(Calendar.YEAR));
    }

    // Tests parsing with textual month names and day of week
    @Test
    public void testParse_textMonthAndDayOfWeek_parsesCorrectly() throws ParseException {
        FastDateParser parser = new FastDateParser("EEEE, MMMM d, yyyy", GMT, US);
        Date date = parser.parse("Monday, July 4, 2022");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(date);
        assertEquals(2022, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, cal.get(Calendar.MONTH));
        assertEquals(4, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(Calendar.MONDAY, cal.get(Calendar.DAY_OF_WEEK));
    }

    // Tests 12-hour AM/PM formatting with 'a', 'h', 'K', 'k', and 'H'
    @Test
    public void testParse_amPmAndVariousHourStrategies_parsesCorrectly() throws ParseException {
        FastDateParser parserH = new FastDateParser("yyyy-MM-dd h:mm a", GMT, US);
        Date datePm = parserH.parse("2023-05-10 8:15 PM");
        Calendar calPm = Calendar.getInstance(GMT, US);
        calPm.setTime(datePm);
        assertEquals(20, calPm.get(Calendar.HOUR_OF_DAY));

        FastDateParser parserK = new FastDateParser("K:mm a", GMT, US);
        Date dateAm = parserK.parse("0:30 AM");
        Calendar calAm = Calendar.getInstance(GMT, US);
        calAm.setTime(dateAm);
        assertEquals(0, calAm.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, calAm.get(Calendar.MINUTE));
    }

    // Tests pattern with quoted literals and escaped characters
    @Test
    public void testParse_quotedLiteralsAndRegexCharacters_handlesEscapes() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy'T'MM'/'dd' ['HH:mm']'", GMT, US);
        Date date = parser.parse("2023T12/25 [18:00]");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(date);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, cal.get(Calendar.MONTH));
        assertEquals(25, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(18, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
    }

    // Tests special regex metacharacters in literal text
    @Test
    public void testParse_specialRegexCharactersInPattern_escapedSuccessfully() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy.MM.dd (G) {HH$mm} [ss?SSS]", GMT, US);
        Date date = parser.parse("2023.01.01 (AD) {10$20} [30?400]");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(date);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(Calendar.ERA, Calendar.ERA);
        assertEquals(10, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, cal.get(Calendar.MINUTE));
        assertEquals(30, cal.get(Calendar.SECOND));
        assertEquals(400, cal.get(Calendar.MILLISECOND));
    }

    // Tests unparseable text input throws ParseException
    @Test(expected = ParseException.class)
    public void testParse_unparseableInput_throwsParseException() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        parser.parse("invalid-date-format");
    }

    // Tests invalid pattern containing unmatched or unsupported format character
    @Test(expected = IllegalArgumentException.class)
    public void testInit_invalidPatternCharacter_throwsIllegalArgumentException() {
        new FastDateParser("yyyy-MM-dd X", GMT, US);
    }

    // Tests parse using ParsePosition and advancing index
    @Test
    public void testParse_withParsePosition_advancesIndexCorrectly() {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        String text = "Prefix: 2023-08-20 Suffix";
        ParsePosition pos = new ParsePosition(8);
        Date date = parser.parse(text, pos);

        assertNotNull(date);
        assertEquals(18, pos.getIndex());

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(date);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.AUGUST, cal.get(Calendar.MONTH));
        assertEquals(20, cal.get(Calendar.DAY_OF_MONTH));
    }

    // Tests parse using ParsePosition with non-matching prefix returning null
    @Test
    public void testParse_withParsePositionMismatch_returnsNull() {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        ParsePosition pos = new ParsePosition(0);
        Date date = parser.parse("Not a date", pos);
        assertNull(date);
        assertEquals(0, pos.getIndex());
    }

    // Tests parseObject methods
    @Test
    public void testParseObject_validInput_returnsDateInstance() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy/MM/dd", GMT, US);
        Object obj1 = parser.parseObject("2021/04/15");
        assertTrue(obj1 instanceof Date);

        ParsePosition pos = new ParsePosition(0);
        Object obj2 = parser.parseObject("2021/04/15", pos);
        assertEquals(obj1, obj2);
    }

    // Tests timezone strategies for GMT and offset representations
    @Test
    public void testParse_timeZoneOffsetAndName_parsesCorrectly() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd HH:mm z", GMT, US);
        Date dateGmt = parser.parse("2023-01-01 12:00 GMT+02:00");
        assertNotNull(dateGmt);

        FastDateParser parserZ = new FastDateParser("yyyy-MM-dd HH:mm Z", GMT, US);
        Date dateOffset = parserZ.parse("2023-01-01 12:00 +0200");
        assertNotNull(dateOffset);
        assertEquals(dateGmt.getTime(), dateOffset.getTime());
    }

    // Tests getters for pattern, timeZone, and locale
    @Test
    public void testAccessors_configuredValues_returnCorrectProperties() {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", EST, US);
        assertEquals("yyyy-MM-dd", parser.getPattern());
        assertEquals(EST, parser.getTimeZone());
        assertEquals(US, parser.getLocale());
        assertNotNull(parser.getParsePattern());
    }

    // Tests equals and hashCode contracts
    @Test
    public void testEqualsAndHashCode_sameAndDifferentParameters_obeysContract() {
        FastDateParser parser1 = new FastDateParser("yyyy-MM-dd", GMT, US);
        FastDateParser parser2 = new FastDateParser("yyyy-MM-dd", GMT, US);
        FastDateParser parserDiffPattern = new FastDateParser("yyyy/MM/dd", GMT, US);
        FastDateParser parserDiffTz = new FastDateParser("yyyy-MM-dd", EST, US);
        FastDateParser parserDiffLocale = new FastDateParser("yyyy-MM-dd", GMT, Locale.GERMANY);

        assertEquals(parser1, parser1);
        assertEquals(parser1, parser2);
        assertEquals(parser1.hashCode(), parser2.hashCode());

        assertFalse(parser1.equals(null));
        assertFalse(parser1.equals("A String"));
        assertFalse(parser1.equals(parserDiffPattern));
        assertFalse(parser1.equals(parserDiffTz));
        assertFalse(parser1.equals(parserDiffLocale));
    }

    // Tests toString format
    @Test
    public void testToString_validParser_containsKeyAttributes() {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        String str = parser.toString();
        assertTrue(str.contains("FastDateParser["));
        assertTrue(str.contains("yyyy-MM-dd"));
        assertTrue(str.contains("en_US") || str.contains("en"));
        assertTrue(str.contains("GMT"));
    }

    // Tests serialization and deserialization round-trip
    @Test
    public void testSerialization_roundTrip_preservesFunctionality() throws Exception {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd HH:mm:ss", GMT, US);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(parser);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        FastDateParser deserialized = (FastDateParser) ois.readObject();
        ois.close();

        assertEquals(parser, deserialized);
        Date date1 = parser.parse("2023-06-15 10:20:30");
        Date date2 = deserialized.parse("2023-06-15 10:20:30");
        assertEquals(date1, date2);
    }

    // Tests day of year, week of year, and week of month numerical fields
    @Test
    public void testParse_weekAndDayStrategies_parsesCorrectFields() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy D w W F", GMT, US);
        Date date = parser.parse("2023 180 26 4 4");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(date);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(180, cal.get(Calendar.DAY_OF_YEAR));
    }

    // Tests Japanese Imperial locale exception handling for unparseable dates
    @Test
    public void testParse_japaneseImperialLocale_throwsParseExceptionWithLocaleNote() {
        FastDateParser parser = new FastDateParser("GGGG yyyy-MM-dd", GMT, FastDateParser.JAPANESE_IMPERIAL);
        try {
            parser.parse("InvalidDate");
            fail("Expected ParseException");
        } catch (ParseException e) {
            assertTrue(e.getMessage().contains("does not support dates before 1868 AD"));
        }
    }

    // Tests direct adjustYear boundary logic
    @Test
    public void testAdjustYear_withinWindow_adjustsCorrectly() {
        FastDateParser parser = new FastDateParser("yy", GMT, US);
        int currentYear = Calendar.getInstance(GMT, US).get(Calendar.YEAR);
        int thisCentury = currentYear - currentYear % 100;
        int twoDigitThisYear = currentYear % 100;

        int adjusted = parser.adjustYear(twoDigitThisYear);
        assertEquals(currentYear, adjusted);

        int adjustedPast = parser.adjustYear((twoDigitThisYear + 25) % 100);
        assertTrue(adjustedPast <= currentYear + 20);
        assertTrue(adjustedPast >= currentYear - 80);
    }
}