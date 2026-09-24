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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class FastDateFormatTest {

    // Tests Lang-26 bug: Date formatting with specific Locale where week-of-year calculation differs
    @Test
    public void testFormat_dateWithLocaleWeekCalculation_matchesCalendarFormat() {
        Locale svSe = new Locale("sv", "SE");
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"), svSe);
        cal.clear();
        cal.set(2010, Calendar.JANUARY, 1);
        Date date = cal.getTime();

        FastDateFormat fdf = FastDateFormat.getInstance("w", TimeZone.getTimeZone("GMT"), svSe);
        assertEquals("53", fdf.format(cal));
        assertEquals("53", fdf.format(date));
    }

    // Tests getInstance factory methods and caching
    @Test
    public void testGetInstance_variousArguments_returnsCachedInstances() {
        FastDateFormat fdf1 = FastDateFormat.getInstance();
        FastDateFormat fdf2 = FastDateFormat.getInstance();
        assertSame(fdf1, fdf2);

        FastDateFormat fdfPattern = FastDateFormat.getInstance("yyyy-MM-dd");
        FastDateFormat fdfPattern2 = FastDateFormat.getInstance("yyyy-MM-dd");
        assertSame(fdfPattern, fdfPattern2);

        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fdfTz = FastDateFormat.getInstance("yyyy-MM-dd", tz);
        assertEquals(tz, fdfTz.getTimeZone());

        Locale loc = Locale.GERMANY;
        FastDateFormat fdfLoc = FastDateFormat.getInstance("yyyy-MM-dd", loc);
        assertEquals(loc, fdfLoc.getLocale());

        FastDateFormat fdfAll = FastDateFormat.getInstance("yyyy-MM-dd", tz, loc);
        assertEquals(tz, fdfAll.getTimeZone());
        assertEquals(loc, fdfAll.getLocale());
    }

    // Tests null pattern throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetInstance_nullPattern_throwsIllegalArgumentException() {
        FastDateFormat.getInstance(null);
    }

    // Tests invalid pattern component throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetInstance_illegalPattern_throwsIllegalArgumentException() {
        FastDateFormat.getInstance("yyyy-MM-dd X");
    }

    // Tests getDateInstance factories
    @Test
    public void testGetDateInstance_validStyles_returnsFormattedOutput() {
        FastDateFormat fdfDefault = FastDateFormat.getDateInstance(FastDateFormat.SHORT);
        assertNotNull(fdfDefault);

        FastDateFormat fdfLocale = FastDateFormat.getDateInstance(FastDateFormat.MEDIUM, Locale.US);
        assertNotNull(fdfLocale);

        FastDateFormat fdfTz = FastDateFormat.getDateInstance(FastDateFormat.LONG, TimeZone.getTimeZone("UTC"));
        assertNotNull(fdfTz);

        FastDateFormat fdfAll = FastDateFormat.getDateInstance(FastDateFormat.FULL, TimeZone.getTimeZone("UTC"), Locale.US);
        assertNotNull(fdfAll);
    }

    // Tests getTimeInstance factories
    @Test
    public void testGetTimeInstance_validStyles_returnsFormattedOutput() {
        FastDateFormat fdfDefault = FastDateFormat.getTimeInstance(FastDateFormat.SHORT);
        assertNotNull(fdfDefault);

        FastDateFormat fdfLocale = FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM, Locale.US);
        assertNotNull(fdfLocale);

        FastDateFormat fdfTz = FastDateFormat.getTimeInstance(FastDateFormat.LONG, TimeZone.getTimeZone("UTC"));
        assertNotNull(fdfTz);

        FastDateFormat fdfAll = FastDateFormat.getTimeInstance(FastDateFormat.FULL, TimeZone.getTimeZone("UTC"), Locale.US);
        assertNotNull(fdfAll);
    }

    // Tests getDateTimeInstance factories
    @Test
    public void testGetDateTimeInstance_validStyles_returnsFormattedOutput() {
        FastDateFormat fdfDefault = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT);
        assertNotNull(fdfDefault);

        FastDateFormat fdfLocale = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT, Locale.US);
        assertNotNull(fdfLocale);

        FastDateFormat fdfTz = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT, TimeZone.getTimeZone("UTC"));
        assertNotNull(fdfTz);

        FastDateFormat fdfAll = FastDateFormat.getDateTimeInstance(FastDateFormat.MEDIUM, FastDateFormat.MEDIUM, TimeZone.getTimeZone("UTC"), Locale.US);
        assertNotNull(fdfAll);
    }

    // Tests standard date and time pattern symbols
    @Test
    public void testFormat_standardPatterns_formatsExpectedValues() {
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        Locale us = Locale.US;

        Calendar cal = new GregorianCalendar(gmt, us);
        cal.clear();
        cal.set(2023, Calendar.MARCH, 8, 14, 5, 9);
        cal.set(Calendar.MILLISECOND, 42);

        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS", gmt, us);
        assertEquals("2023-03-08 14:05:09.042", fdf.format(cal));

        FastDateFormat fdf2Digit = FastDateFormat.getInstance("yy-M-d H:m:s.S", gmt, us);
        assertEquals("23-3-8 14:5:9.42", fdf2Digit.format(cal));

        FastDateFormat fdfText = FastDateFormat.getInstance("G EEEE MMMM 'o''clock' a", gmt, us);
        assertEquals("AD Wednesday March o'clock PM", fdfText.format(cal));
    }

    // Tests 12-hour and 24-hour boundary values (hour 0 / midnight, hour 12 / noon)
    @Test
    public void testFormat_hourVariations_formatsCorrectly() {
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        Locale us = Locale.US;

        Calendar midnight = new GregorianCalendar(gmt, us);
        midnight.clear();
        midnight.set(2023, Calendar.JANUARY, 1, 0, 0, 0);

        FastDateFormat fdf = FastDateFormat.getInstance("h:H:k:K a", gmt, us);
        assertEquals("12:0:24:0 AM", fdf.format(midnight));

        Calendar noon = new GregorianCalendar(gmt, us);
        noon.clear();
        noon.set(2023, Calendar.JANUARY, 1, 12, 0, 0);
        assertEquals("12:12:12:0 PM", fdf.format(noon));
    }

    // Tests day/week pattern letters: D, F, w, W
    @Test
    public void testFormat_dayWeekFields_formatsCorrectly() {
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        Locale us = Locale.US;

        Calendar cal = new GregorianCalendar(gmt, us);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 15);

        FastDateFormat fdf = FastDateFormat.getInstance("D F w W", gmt, us);
        assertEquals("15 3 3 3", fdf.format(cal));
    }

    // Tests timezone format patterns: z, zzzz, Z, ZZ
    @Test
    public void testFormat_timezonePatterns_formatsCorrectly() {
        TimeZone tz = TimeZone.getTimeZone("GMT+02:00");
        Locale us = Locale.US;

        Calendar cal = new GregorianCalendar(tz, us);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1);

        FastDateFormat fdf = FastDateFormat.getInstance("z zzzz Z ZZ", tz, us);
        assertEquals("GMT+02:00 GMT+02:00 +0200 +02:00", fdf.format(cal));
    }

    // Tests negative timezone offset formatting
    @Test
    public void testFormat_negativeTimezoneOffset_formatsWithMinus() {
        TimeZone tz = TimeZone.getTimeZone("GMT-05:00");
        Locale us = Locale.US;

        Calendar cal = new GregorianCalendar(tz, us);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1);

        FastDateFormat fdf = FastDateFormat.getInstance("Z ZZ", tz, us);
        assertEquals("-0500 -05:00", fdf.format(cal));
    }

    // Tests format overloads: long, Date, Calendar, StringBuffer, Format.format(Object, StringBuffer, FieldPosition)
    @Test
    public void testFormat_variousInputTypes_formatsCorrectly() {
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        Locale us = Locale.US;
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd", gmt, us);

        Calendar cal = new GregorianCalendar(gmt, us);
        cal.clear();
        cal.set(2023, Calendar.MAY, 4);
        long millis = cal.getTimeInMillis();
        Date date = cal.getTime();

        assertEquals("2023-05-04", fdf.format(millis));
        assertEquals("2023-05-04", fdf.format(date));
        assertEquals("2023-05-04", fdf.format(cal));

        StringBuffer sb = new StringBuffer("Result: ");
        assertEquals("Result: 2023-05-04", fdf.format(millis, sb).toString());

        StringBuffer sbDate = new StringBuffer();
        assertEquals("2023-05-04", fdf.format(date, sbDate).toString());

        StringBuffer sbObject = new StringBuffer();
        assertEquals("2023-05-04", fdf.format((Object) date, sbObject, new FieldPosition(0)).toString());
        assertEquals("2023-05-04", fdf.format((Object) cal, new StringBuffer(), new FieldPosition(0)).toString());
        assertEquals("2023-05-04", fdf.format((Object) Long.valueOf(millis), new StringBuffer(), new FieldPosition(0)).toString());
    }

    // Tests invalid object passed to format(Object, StringBuffer, FieldPosition)
    @Test(expected = IllegalArgumentException.class)
    public void testFormat_invalidObjectType_throwsIllegalArgumentException() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        fdf.format("2023-05-04", new StringBuffer(), new FieldPosition(0));
    }

    // Tests parseObject returns null as parsing is not supported
    @Test
    public void testParseObject_always_returnsNull() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        assertNull(fdf.parseObject("2023-05-04", pos));
        assertEquals(0, pos.getIndex());
        assertEquals(0, pos.getErrorIndex());
    }

    // Tests equals, hashCode, and toString
    @Test
    public void testEqualsAndHashCodeAndToString() {
        TimeZone tz1 = TimeZone.getTimeZone("GMT");
        TimeZone tz2 = TimeZone.getTimeZone("UTC");
        Locale loc = Locale.US;

        FastDateFormat fdf1 = FastDateFormat.getInstance("yyyy-MM-dd", tz1, loc);
        FastDateFormat fdf2 = FastDateFormat.getInstance("yyyy-MM-dd", tz1, loc);
        FastDateFormat fdfDiffPattern = FastDateFormat.getInstance("yyyy/MM/dd", tz1, loc);
        FastDateFormat fdfDiffTz = FastDateFormat.getInstance("yyyy-MM-dd", tz2, loc);

        assertTrue(fdf1.equals(fdf1));
        assertTrue(fdf1.equals(fdf2));
        assertEquals(fdf1.hashCode(), fdf2.hashCode());

        assertFalse(fdf1.equals(null));
        assertFalse(fdf1.equals("string"));
        assertFalse(fdf1.equals(fdfDiffPattern));
        assertFalse(fdf1.equals(fdfDiffTz));

        assertEquals("FastDateFormat[yyyy-MM-dd]", fdf1.toString());
        assertEquals("yyyy-MM-dd", fdf1.getPattern());
        assertTrue(fdf1.getMaxLengthEstimate() > 0);
        assertTrue(fdf1.getTimeZoneOverridesCalendar());
    }
}