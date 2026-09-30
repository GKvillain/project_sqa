package org.apache.commons.lang3.time;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

public class FastDateParserTest {

    // Tests basic parse with yyyy-MM-dd pattern
    @Test
    public void testParse_basicPattern() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", TimeZone.getDefault(), Locale.US);
        Date date = parser.parse("2020-02-20");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(1, cal.get(Calendar.MONTH));
        assertEquals(20, cal.get(Calendar.DAY_OF_MONTH));
    }

    // Tests two-digit year "00" returns year 2000 (based on current year)
    @Test
    public void testParse_abbreviatedYear_returns2000() throws ParseException {
        FastDateParser parser = new FastDateParser("yy", TimeZone.getDefault(), Locale.US);
        Date date = parser.parse("00");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2000, cal.get(Calendar.YEAR));
    }

    // Tests two-digit year "19" returns 2019 (based on current year)
    @Test
    public void testParse_abbreviatedYear19_returns2019() throws ParseException {
        FastDateParser parser = new FastDateParser("yy", TimeZone.getDefault(), Locale.US);
        Date date = parser.parse("19");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2019, cal.get(Calendar.YEAR));
    }

    // Tests adjustYear boundary condition (20)
    @Test
    public void testAdjustYear_boundary() {
        FastDateParser parser = new FastDateParser("yy", TimeZone.getDefault(), Locale.US);
        int thisYear = Calendar.getInstance(TimeZone.getDefault(), Locale.US).get(Calendar.YEAR);
        int trial = 20 + thisYear - thisYear % 100;
        int expected = trial < thisYear + 20 ? trial : trial - 100;
        assertEquals(expected, parser.adjustYear(20));
    }

    // Tests invalid pattern throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testInit_invalidPattern_throwsException() {
        new FastDateParser("Invalid[Pattern", TimeZone.getDefault(), Locale.US);
    }

    // Tests non-matching input throws ParseException
    @Test(expected = ParseException.class)
    public void testParse_invalidDate_throwsParseException() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", TimeZone.getDefault(), Locale.US);
        parser.parse("abc");
    }

    // Tests parse with ParsePosition returns correct Date and index
    @Test
    public void testParse_withParsePosition_ok() {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", TimeZone.getDefault(), Locale.US);
        ParsePosition pos = new ParsePosition(0);
        Date date = parser.parse("2020-02-20", pos);
        assertNotNull(date);
        assertEquals(10, pos.getIndex());
    }

    // Tests parse with ParsePosition returns null on mismatch
    @Test
    public void testParse_withParsePosition_invalid_returnsNull() {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", TimeZone.getDefault(), Locale.US);
        ParsePosition pos = new ParsePosition(0);
        Date date = parser.parse("abc", pos);
        assertNull(date);
    }

    // Tests Japanese Imperial locale throws ParseException with special message
    @Test(expected = ParseException.class)
    public void testParse_japaneseImperial_throwsException() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy", TimeZone.getDefault(), FastDateParser.JAPANESE_IMPERIAL);
        parser.parse("invalid");
    }

    // Tests parse with text month (MMM)
    @Test
    public void testParse_textMonth() throws ParseException {
        FastDateParser parser = new FastDateParser("MMM yyyy", TimeZone.getDefault(), Locale.US);
        Date date = parser.parse("Jan 2020");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(0, cal.get(Calendar.MONTH));
    }

    // Tests parse with am/pm marker
    @Test
    public void testParse_amPm() throws ParseException {
        FastDateParser parser = new FastDateParser("hh:mm a", TimeZone.getDefault(), Locale.US);
        Date date = parser.parse("10:30 AM");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(10, cal.get(Calendar.HOUR));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(Calendar.AM, cal.get(Calendar.AM_PM));
    }

    // Tests parse with timezone abbreviation
    @Test
    public void testParse_timeZone() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd z", TimeZone.getTimeZone("GMT"), Locale.US);
        Date date = parser.parse("2020-02-20 GMT");
        assertNotNull(date);
    }

    // Tests parse with GMT offset (Z pattern)
    @Test
    public void testParse_gmtOffset() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd Z", TimeZone.getDefault(), Locale.US);
        Date date = parser.parse("2020-02-20 +0530");
        assertNotNull(date);
    }

    // Tests getter methods for pattern, timezone, locale
    @Test
    public void testGetters() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        Locale locale = Locale.UK;
        FastDateParser parser = new FastDateParser("dd/MM/yyyy", tz, locale);
        assertEquals("dd/MM/yyyy", parser.getPattern());
        assertEquals(tz, parser.getTimeZone());
        assertEquals(locale, parser.getLocale());
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode() {
        FastDateParser p1 = new FastDateParser("yyyy", TimeZone.getDefault(), Locale.US);
        FastDateParser p2 = new FastDateParser("yyyy", TimeZone.getDefault(), Locale.US);
        assertTrue(p1.equals(p2));
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    // Tests toString contains class name and pattern
    @Test
    public void testToString() {
        FastDateParser parser = new FastDateParser("yy", TimeZone.getDefault(), Locale.US);
        String str = parser.toString();
        assertTrue(str.contains("FastDateParser"));
        assertTrue(str.contains("yy"));
    }

    // Tests getDisplayNames for ERA returns non-null array
    @Test
    public void testGetDisplayNames_era_returnsNonEmpty() {
        FastDateParser parser = new FastDateParser("yyyy", TimeZone.getDefault(), Locale.US);
        Object[] eras = parser.getDisplayNames(Calendar.ERA);
        assertNotNull(eras);
        assertTrue(eras.length > 0);
    }

    // Tests getDisplayNames with invalid field throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetDisplayNames_invalidField_throwsException() {
        FastDateParser parser = new FastDateParser("yyyy", TimeZone.getDefault(), Locale.US);
        parser.getDisplayNames(999);
    }

    // Tests parse with consecutive numeric fields (no separator)
    @Test
    public void testParse_consecutiveNumbers() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyyMMdd", TimeZone.getDefault(), Locale.US);
        Date date = parser.parse("20200220");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(1, cal.get(Calendar.MONTH));
        assertEquals(20, cal.get(Calendar.DAY_OF_MONTH));
    }

    // Tests parse with quoted literal text
    @Test
    public void testParse_quotedText() throws ParseException {
        FastDateParser parser = new FastDateParser("'T'yy/MM/dd", TimeZone.getDefault(), Locale.US);
        Date date = parser.parse("T20/02/20");
        assertNotNull(date);
    }

    // Tests parseObject() returns Date
    @Test
    public void testParseObject() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy", TimeZone.getDefault(), Locale.US);
        Object obj = parser.parseObject("2020");
        assertTrue(obj instanceof Date);
    }

    // Tests parseObject with ParsePosition
    @Test
    public void testParseObject_withParsePosition() {
        FastDateParser parser = new FastDateParser("yyyy", TimeZone.getDefault(), Locale.US);
        ParsePosition pos = new ParsePosition(0);
        Object obj = parser.parseObject("2020", pos);
        assertNotNull(obj);
        assertEquals(4, pos.getIndex());
    }
}