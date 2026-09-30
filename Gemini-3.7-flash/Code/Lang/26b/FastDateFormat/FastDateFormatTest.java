package org.apache.commons.lang3.time;

import org.junit.Test;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FastDateFormatTest {

    // Tests week in year formatting with specific locale to expose localization issues (Lang-26)
    @Test
    public void testFormatDate_weekInYearWithLocale_returnsCorrectWeek() {
        Locale locale = new Locale("sv", "SE");
        Calendar cal = Calendar.getInstance(locale);
        cal.clear();
        cal.set(2010, Calendar.JANUARY, 1, 12, 0, 0);
        Date date = cal.getTime();

        FastDateFormat fdf = FastDateFormat.getInstance("EEEE', week 'ww", locale);
        assertEquals("fredag, vecka 53", fdf.format(date));
    }

    // Tests getInstance factory methods and caching
    @Test
    public void testGetInstance_variousSignatures_returnsNonNullInstance() {
        FastDateFormat fdf1 = FastDateFormat.getInstance();
        FastDateFormat fdf2 = FastDateFormat.getInstance("yyyy-MM-dd");
        FastDateFormat fdf3 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"));
        FastDateFormat fdf4 = FastDateFormat.getInstance("yyyy-MM-dd", Locale.US);
        FastDateFormat fdf5 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);

        assertNotNull(fdf1);
        assertEquals("yyyy-MM-dd", fdf2.getPattern());
        assertEquals(TimeZone.getTimeZone("GMT"), fdf3.getTimeZone());
        assertEquals(Locale.US, fdf4.getLocale());
        assertEquals("yyyy-MM-dd", fdf5.getPattern());

        FastDateFormat fdfCached = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        assertEquals(fdf5, fdfCached);
    }

    // Tests date, time, and dateTime instance factory methods
    @Test
    public void testGetDateTimeInstances_validStyles_returnsConfiguredInstances() {
        FastDateFormat dateOnly = FastDateFormat.getDateInstance(FastDateFormat.SHORT, Locale.US);
        FastDateFormat timeOnly = FastDateFormat.getTimeInstance(FastDateFormat.SHORT, Locale.US);
        FastDateFormat dateTime = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT, Locale.US);

        assertNotNull(dateOnly);
        assertNotNull(timeOnly);
        assertNotNull(dateTime);
    }

    // Tests formatting of Date, Calendar, and long millisecond values
    @Test
    public void testFormat_dateCalendarAndMillis_producesExpectedString() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Locale locale = Locale.US;
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", tz, locale);

        Calendar cal = new GregorianCalendar(tz, locale);
        cal.clear();
        cal.set(2023, Calendar.DECEMBER, 25, 15, 30, 45);
        Date date = cal.getTime();
        long millis = date.getTime();

        String expected = "2023-12-25 15:30:45";
        assertEquals(expected, fdf.format(date));
        assertEquals(expected, fdf.format(cal));
        assertEquals(expected, fdf.format(millis));

        StringBuffer buf = new StringBuffer("Result: ");
        assertEquals("Result: " + expected, fdf.format(date, buf).toString());
    }

    // Tests format using Format base class method with FieldPosition
    @Test
    public void testFormat_objectTypes_formatsProperly() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.clear();
        cal.set(2021, Calendar.JANUARY, 1);

        StringBuffer buf1 = new StringBuffer();
        fdf.format(cal.getTime(), buf1, new FieldPosition(0));
        assertEquals("2021-01-01", buf1.toString());

        StringBuffer buf2 = new StringBuffer();
        fdf.format(cal, buf2, new FieldPosition(0));
        assertEquals("2021-01-01", buf2.toString());

        StringBuffer buf3 = new StringBuffer();
        fdf.format(Long.valueOf(cal.getTimeInMillis()), buf3, new FieldPosition(0));
        assertEquals("2021-01-01", buf3.toString());
    }

    // Tests format with invalid object type throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFormat_invalidObject_throwsIllegalArgumentException() {
        FastDateFormat fdf = FastDateFormat.getInstance();
        fdf.format("Not A Date", new StringBuffer(), new FieldPosition(0));
    }

    // Tests constructor with null pattern throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullPattern_throwsIllegalArgumentException() {
        FastDateFormat.getInstance(null);
    }

    // Tests invalid pattern character throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParsePattern_invalidPatternChar_throwsIllegalArgumentException() {
        FastDateFormat.getInstance("yyyy-MM-dd X");
    }

    // Tests various standard pattern tokens (G, y, M, d, h, H, m, s, S, E, D, F, w, W, a, k, K)
    @Test
    public void testParsePattern_allStandardTokens_formatsCorrectly() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Locale locale = Locale.US;
        String pattern = "G y yy yyyy M MM MMM MMMM d h H m s S E EEEE D F w W a k K";
        FastDateFormat fdf = FastDateFormat.getInstance(pattern, tz, locale);

        Calendar cal = new GregorianCalendar(tz, locale);
        cal.clear();
        cal.set(2023, Calendar.JULY, 4, 13, 5, 9);
        cal.set(Calendar.MILLISECOND, 123);

        String result = fdf.format(cal);
        assertNotNull(result);
        assertTrue(result.contains("AD"));
        assertTrue(result.contains("23"));
        assertTrue(result.contains("2023"));
        assertTrue(result.contains("Jul"));
        assertTrue(result.contains("July"));
        assertTrue(result.contains("Tue"));
        assertTrue(result.contains("Tuesday"));
        assertTrue(result.contains("PM"));
    }

    // Tests literal strings and quoted single quotes
    @Test
    public void testParsePattern_literalsAndEscapes_formatsLiteralsCorrectly() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fdf = FastDateFormat.getInstance("''yyyy'' 'hello' ''", tz, Locale.US);

        Calendar cal = new GregorianCalendar(tz, Locale.US);
        cal.clear();
        cal.set(Calendar.YEAR, 2023);

        String result = fdf.format(cal);
        assertEquals("'2023' hello '", result);
    }

    // Tests timezone formatting rules 'z', 'zzzz', 'Z', and 'ZZ'
    @Test
    public void testFormat_timeZoneRules_appliesTimeZoneFormats() {
        TimeZone tz = TimeZone.getTimeZone("GMT-05:00");
        FastDateFormat fdf = FastDateFormat.getInstance("z zzzz Z ZZ", tz, Locale.US);

        Calendar cal = new GregorianCalendar(tz, Locale.US);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1, 12, 0, 0);

        String result = fdf.format(cal);
        assertTrue(result.contains("-0500"));
        assertTrue(result.contains("-05:00"));
    }

    // Tests parseObject returns null as not supported
    @Test
    public void testParseObject_always_returnsNullAndSetsIndexes() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        Object result = fdf.parseObject("2023-01-01", pos);

        assertNull(result);
        assertEquals(0, pos.getIndex());
        assertEquals(0, pos.getErrorIndex());
    }

    // Tests equals, hashCode, and toString methods
    @Test
    public void testEqualsAndHashCode_sameAndDifferentObjects_worksCorrectly() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat fdf2 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat fdf3 = FastDateFormat.getInstance("yyyy/MM/dd", TimeZone.getTimeZone("GMT"), Locale.US);

        assertEquals(fdf1, fdf2);
        assertEquals(fdf1.hashCode(), fdf2.hashCode());
        assertFalse(fdf1.equals(fdf3));
        assertFalse(fdf1.equals(null));
        assertFalse(fdf1.equals("different type"));

        assertTrue(fdf1.toString().contains("yyyy-MM-dd"));
    }

    // Tests accessors and estimate length
    @Test
    public void testAccessors_configuredProperties_returnsExpectedValues() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Locale locale = Locale.GERMANY;
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd", tz, locale);

        assertEquals("yyyy-MM-dd", fdf.getPattern());
        assertEquals(tz, fdf.getTimeZone());
        assertEquals(locale, fdf.getLocale());
        assertTrue(fdf.getTimeZoneOverridesCalendar());
        assertTrue(fdf.getMaxLengthEstimate() > 0);
    }

    // Tests calendar formatting when timezone is forced
    @Test
    public void testFormatCalendar_timeZoneForced_convertsToFormatterTimeZone() {
        TimeZone formatterTz = TimeZone.getTimeZone("GMT+02:00");
        FastDateFormat fdf = FastDateFormat.getInstance("HH:mm", formatterTz, Locale.US);

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1, 10, 0, 0);

        String result = fdf.format(cal);
        assertEquals("12:00", result);
    }
}