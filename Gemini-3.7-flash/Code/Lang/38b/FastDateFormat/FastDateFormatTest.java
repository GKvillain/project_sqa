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

    // Tests defect Lang-38: formatting Calendar with forced TimeZone
    @Test
    public void testFormat_calendarWithForcedTimeZone_adjustsTimeCorrectly() {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.set(2009, Calendar.OCTOBER, 16, 8, 42, 16);
        cal.set(Calendar.MILLISECOND, 0);

        FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZZ", TimeZone.getTimeZone("GMT-8"));
        assertEquals("2009-10-16T00:42:16.000-08:00", format.format(cal));
    }

    // Tests standard formatting with Date, Calendar, and long millis
    @Test
    public void testFormat_dateCalendarAndMillis_produceConsistentOutput() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        Locale locale = Locale.US;
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS", tz, locale);

        Calendar cal = new GregorianCalendar(tz, locale);
        cal.set(2023, Calendar.JANUARY, 15, 13, 45, 30);
        cal.set(Calendar.MILLISECOND, 500);

        Date date = cal.getTime();
        long millis = date.getTime();

        String expected = "2023-01-15 13:45:30.500";
        assertEquals(expected, fdf.format(date));
        assertEquals(expected, fdf.format(cal));
        assertEquals(expected, fdf.format(millis));

        StringBuffer buf = new StringBuffer("Result: ");
        assertEquals("Result: " + expected, fdf.format(date, buf).toString());
    }

    // Tests Format.format(Object, StringBuffer, FieldPosition) polymorphic dispatch
    @Test
    public void testFormat_objectTypes_formatsSuccessfully() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd", tz, Locale.US);

        Calendar cal = new GregorianCalendar(tz, Locale.US);
        cal.set(2022, Calendar.MAY, 10, 0, 0, 0);

        StringBuffer buf = new StringBuffer();
        fdf.format((Object) cal.getTime(), buf, new FieldPosition(0));
        assertEquals("2022-05-10", buf.toString());

        buf = new StringBuffer();
        fdf.format((Object) cal, buf, new FieldPosition(0));
        assertEquals("2022-05-10", buf.toString());

        buf = new StringBuffer();
        fdf.format((Object) Long.valueOf(cal.getTimeInMillis()), buf, new FieldPosition(0));
        assertEquals("2022-05-10", buf.toString());
    }

    // Tests unsupported object type in format(Object, StringBuffer, FieldPosition)
    @Test(expected = IllegalArgumentException.class)
    public void testFormat_invalidObjectType_throwsException() {
        FastDateFormat fdf = FastDateFormat.getInstance();
        fdf.format("invalid date object", new StringBuffer(), new FieldPosition(0));
    }

    // Tests various pattern tokens: year, month, day, hours, literals, time zones
    @Test
    public void testParsePattern_variousTokens_formatsCorrectly() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        Locale locale = Locale.US;

        String pattern = "G yyyy yy MMMM MMM MM M d h H m s S EEEE E D F w W a k K z zzzz Z ZZ '' 'literal' ''";
        FastDateFormat fdf = FastDateFormat.getInstance(pattern, tz, locale);

        Calendar cal = new GregorianCalendar(tz, locale);
        cal.set(2023, Calendar.DECEMBER, 5, 0, 30, 15);
        cal.set(Calendar.MILLISECOND, 25);

        String result = fdf.format(cal);
        assertNotNull(result);
        assertTrue(result.contains("AD"));
        assertTrue(result.contains("2023"));
        assertTrue(result.contains("23"));
        assertTrue(result.contains("December"));
        assertTrue(result.contains("Dec"));
        assertTrue(result.contains("12"));
        assertTrue(result.contains("literal"));
        assertTrue(result.contains("'"));
    }

    // Tests TwelveHourField and TwentyFourHourField hour-zero boundary adjustments
    @Test
    public void testFormat_midnightAndNoonBoundaries_handles12And24Hours() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDateFormat fdf12 = FastDateFormat.getInstance("h K a", tz, Locale.US);
        FastDateFormat fdf24 = FastDateFormat.getInstance("H k", tz, Locale.US);

        Calendar cal = new GregorianCalendar(tz, Locale.US);
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0); // Midnight

        assertEquals("12 0 AM", fdf12.format(cal));
        assertEquals("0 24", fdf24.format(cal));

        cal.set(Calendar.HOUR_OF_DAY, 12); // Noon
        assertEquals("12 0 PM", fdf12.format(cal));
        assertEquals("12 12", fdf24.format(cal));
    }

    // Tests PaddedNumberField with values >= 100 and >= 1000
    @Test
    public void testFormat_paddedNumberField_padsCorrectly() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-SSS-DDD", tz, Locale.US);

        Calendar cal = new GregorianCalendar(tz, Locale.US);
        cal.set(2023, Calendar.JANUARY, 5, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 7);

        assertEquals("2023-007-005", fdf.format(cal));

        cal.set(Calendar.DAY_OF_YEAR, 123);
        cal.set(Calendar.MILLISECOND, 456);
        assertEquals("2023-456-123", fdf.format(cal));
    }

    // Tests factory methods for standard date, time, and dateTime instances
    @Test
    public void testFactoryMethods_standardInstances_returnsCachedInstances() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Locale locale = Locale.UK;

        FastDateFormat df1 = FastDateFormat.getDateInstance(FastDateFormat.SHORT, tz, locale);
        FastDateFormat df2 = FastDateFormat.getDateInstance(FastDateFormat.SHORT, tz, locale);
        assertTrue(df1 == df2);

        FastDateFormat tf1 = FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM, tz, locale);
        FastDateFormat tf2 = FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM, tz, locale);
        assertTrue(tf1 == tf2);

        FastDateFormat dtf1 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT, tz, locale);
        FastDateFormat dtf2 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT, tz, locale);
        assertTrue(dtf1 == dtf2);

        assertNotNull(FastDateFormat.getDateInstance(FastDateFormat.FULL));
        assertNotNull(FastDateFormat.getDateInstance(FastDateFormat.LONG, locale));
        assertNotNull(FastDateFormat.getDateInstance(FastDateFormat.MEDIUM, tz));
        assertNotNull(FastDateFormat.getTimeInstance(FastDateFormat.SHORT));
        assertNotNull(FastDateFormat.getTimeInstance(FastDateFormat.LONG, locale));
        assertNotNull(FastDateFormat.getTimeInstance(FastDateFormat.FULL, tz));
        assertNotNull(FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL));
        assertNotNull(FastDateFormat.getDateTimeInstance(FastDateFormat.LONG, FastDateFormat.LONG, locale));
        assertNotNull(FastDateFormat.getDateTimeInstance(FastDateFormat.MEDIUM, FastDateFormat.MEDIUM, tz));
    }

    // Tests factory getInstance overloads and instance caching
    @Test
    public void testGetInstance_overloadsAndCaching_returnsSameInstance() {
        FastDateFormat defaultFormat = FastDateFormat.getInstance();
        assertNotNull(defaultFormat);

        FastDateFormat patternFormat = FastDateFormat.getInstance("yyyy-MM-dd");
        FastDateFormat patternFormatSame = FastDateFormat.getInstance("yyyy-MM-dd");
        assertTrue(patternFormat == patternFormatSame);

        FastDateFormat tzFormat = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"));
        assertNotNull(tzFormat);

        FastDateFormat localeFormat = FastDateFormat.getInstance("yyyy-MM-dd", Locale.FRANCE);
        assertNotNull(localeFormat);
    }

    // Tests illegal pattern component handling
    @Test(expected = IllegalArgumentException.class)
    public void testGetInstance_invalidPatternToken_throwsException() {
        FastDateFormat.getInstance("yyyy-MM-dd X");
    }

    // Tests null pattern handling
    @Test(expected = IllegalArgumentException.class)
    public void testGetInstance_nullPattern_throwsException() {
        FastDateFormat.getInstance(null);
    }

    // Tests parseObject returning null as parsing is unsupported
    @Test
    public void testParseObject_unsupportedOperation_returnsNullAndResetsPosition() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(5);
        Object result = fdf.parseObject("2023-01-01", pos);
        assertNull(result);
        assertEquals(0, pos.getIndex());
        assertEquals(0, pos.getErrorIndex());
    }

    // Tests equals, hashCode, toString, and accessors
    @Test
    public void testEqualsAndHashCodeAndAccessors_contractCompliance() {
        TimeZone tz1 = TimeZone.getTimeZone("GMT");
        TimeZone tz2 = TimeZone.getTimeZone("EST");
        Locale loc1 = Locale.US;
        Locale loc2 = Locale.GERMANY;

        FastDateFormat fdf1 = FastDateFormat.getInstance("yyyy-MM-dd", tz1, loc1);
        FastDateFormat fdf2 = FastDateFormat.getInstance("yyyy-MM-dd", tz1, loc1);
        FastDateFormat fdfDiffPattern = FastDateFormat.getInstance("yyyy/MM/dd", tz1, loc1);
        FastDateFormat fdfDiffTz = FastDateFormat.getInstance("yyyy-MM-dd", tz2, loc1);
        FastDateFormat fdfDiffLoc = FastDateFormat.getInstance("yyyy-MM-dd", tz1, loc2);
        FastDateFormat fdfDefaultTz = FastDateFormat.getInstance("yyyy-MM-dd", null, loc1);

        assertTrue(fdf1.equals(fdf1));
        assertTrue(fdf1.equals(fdf2));
        assertEquals(fdf1.hashCode(), fdf2.hashCode());

        assertFalse(fdf1.equals(null));
        assertFalse(fdf1.equals("other type"));
        assertFalse(fdf1.equals(fdfDiffPattern));
        assertFalse(fdf1.equals(fdfDiffTz));
        assertFalse(fdf1.equals(fdfDiffLoc));
        assertFalse(fdf1.equals(fdfDefaultTz));

        assertEquals("yyyy-MM-dd", fdf1.getPattern());
        assertEquals(tz1, fdf1.getTimeZone());
        assertEquals(loc1, fdf1.getLocale());
        assertTrue(fdf1.getTimeZoneOverridesCalendar());
        assertFalse(fdfDefaultTz.getTimeZoneOverridesCalendar());
        assertTrue(fdf1.getMaxLengthEstimate() > 0);
        assertEquals("FastDateFormat[yyyy-MM-dd]", fdf1.toString());
    }

    // Tests TimeZone display caching and negative offsets
    @Test
    public void testFormat_timeZoneOffsetsAndDisplays_formatsCorrectly() {
        TimeZone tzMinus = TimeZone.getTimeZone("GMT-05:00");
        FastDateFormat fdfMinus = FastDateFormat.getInstance("Z ZZ z zzzz", tzMinus, Locale.US);

        Calendar cal = new GregorianCalendar(tzMinus, Locale.US);
        cal.set(2023, Calendar.JANUARY, 1, 12, 0, 0);

        String formatted = fdfMinus.format(cal);
        assertTrue(formatted.contains("-0500"));
        assertTrue(formatted.contains("-05:00"));

        TimeZone tzPlus = TimeZone.getTimeZone("GMT+08:00");
        FastDateFormat fdfPlus = FastDateFormat.getInstance("Z ZZ", tzPlus, Locale.US);
        Calendar calPlus = new GregorianCalendar(tzPlus, Locale.US);
        calPlus.set(2023, Calendar.JANUARY, 1, 12, 0, 0);

        String formattedPlus = fdfPlus.format(calPlus);
        assertTrue(formattedPlus.contains("+0800"));
        assertTrue(formattedPlus.contains("+08:00"));
    }
}