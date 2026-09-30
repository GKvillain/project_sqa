package org.apache.commons.lang3.time;

import static org.junit.Assert.*;

import java.text.FieldPosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

public class FastDatePrinterTest {

    // Tests format(Date) with a simple pattern
    @Test
    public void testFormatDate_standardPattern_returnsFormattedString() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 15);
        String result = printer.format(cal.getTime());
        assertEquals("2023-01-15", result);
    }

    // Tests format(long) with milliseconds
    @Test
    public void testFormatMillis_standardPattern_returnsFormattedString() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 15);
        String result = printer.format(cal.getTimeInMillis());
        assertEquals("2023-01-15", result);
    }

    // Tests format(Calendar) with a simple pattern
    @Test
    public void testFormatCalendar_standardPattern_returnsFormattedString() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 15);
        String result = printer.format(cal);
        assertEquals("2023-01-15", result);
    }

    // Tests formatting with two-digit year pattern
    @Test
    public void testFormatDate_twoDigitYear_returnsTwoDigitYear() {
        FastDatePrinter printer = new FastDatePrinter("yy", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 1);
        String result = printer.format(cal.getTime());
        assertEquals("23", result);
    }

    // Tests formatting with month as text (full name)
    @Test
    public void testFormatDate_monthTextFull_returnsFullMonthName() {
        FastDatePrinter printer = new FastDatePrinter("MMMM", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 1);
        String result = printer.format(cal.getTime());
        assertEquals("January", result);
    }

    // Tests formatting with month as short text
    @Test
    public void testFormatDate_monthTextShort_returnsShortMonthName() {
        FastDatePrinter printer = new FastDatePrinter("MMM", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 1);
        String result = printer.format(cal.getTime());
        assertEquals("Jan", result);
    }

    // Tests formatting with two-digit month
    @Test
    public void testFormatDate_twoDigitMonth_returnsTwoDigitMonth() {
        FastDatePrinter printer = new FastDatePrinter("MM", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 1);
        String result = printer.format(cal.getTime());
        assertEquals("01", result);
    }

    // Tests formatting with single-digit month (no padding)
    @Test
    public void testFormatDate_unpaddedMonth_returnsMonthWithoutPadding() {
        FastDatePrinter printer = new FastDatePrinter("M", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.SEPTEMBER, 1);
        String result = printer.format(cal.getTime());
        assertEquals("9", result);
    }

    // Tests format(Date) with time zone offset pattern
    @Test
    public void testFormatDate_timeZoneOffset_returnsOffsetString() {
        FastDatePrinter printer = new FastDatePrinter("Z", TimeZone.getTimeZone("GMT+05:30"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+05:30"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 1);
        String result = printer.format(cal.getTime());
        // Expected offset for GMT+05:30 is +0530
        assertEquals("+0530", result);
    }

    // Tests format(Date) with time zone offset with colon pattern
    @Test
    public void testFormatDate_timeZoneOffsetWithColon_returnsOffsetWithColon() {
        FastDatePrinter printer = new FastDatePrinter("ZZ", TimeZone.getTimeZone("GMT+05:30"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+05:30"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 1);
        String result = printer.format(cal.getTime());
        // Expected offset for GMT+05:30 is +05:30
        assertEquals("+05:30", result);
    }

    // Tests format with hour in am/pm (1-12) - twelve hour field
    @Test
    public void testFormatDate_twelveHourField_midnightReturns12() {
        FastDatePrinter printer = new FastDatePrinter("h:mm a", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
        String result = printer.format(cal.getTime());
        assertEquals("12:00 AM", result);
    }

    // Tests format with hour in day (1-24) - twenty four hour field
    @Test
    public void testFormatDate_twentyFourHourField_midnightReturns24() {
        FastDatePrinter printer = new FastDatePrinter("k:mm", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
        String result = printer.format(cal.getTime());
        assertEquals("24:00", result);
    }

    // Tests format with AM/PM marker
    @Test
    public void testFormatDate_amPmMarker_returnsCorrectMarker() {
        FastDatePrinter printer = new FastDatePrinter("a", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 1, 14, 0, 0);
        String result = printer.format(cal.getTime());
        assertEquals("PM", result);
    }

    // Tests format with day of week as text (short)
    @Test
    public void testFormatDate_dayOfWeekShort_returnsShortDayName() {
        FastDatePrinter printer = new FastDatePrinter("E", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 1); // Sunday
        String result = printer.format(cal.getTime());
        assertEquals("Sun", result);
    }

    // Tests format with day of week as text (full)
    @Test
    public void testFormatDate_dayOfWeekFull_returnsFullDayName() {
        FastDatePrinter printer = new FastDatePrinter("EEEE", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 1); // Sunday
        String result = printer.format(cal.getTime());
        assertEquals("Sunday", result);
    }

    // Tests format with era designator
    @Test
    public void testFormatDate_eraDesignator_returnsEraString() {
        FastDatePrinter printer = new FastDatePrinter("G", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 1);
        String result = printer.format(cal.getTime());
        assertEquals("AD", result);
    }

    // Tests format with literal text in pattern
    @Test
    public void testFormatDate_literalText_returnsLiteralText() {
        FastDatePrinter printer = new FastDatePrinter("'Today is' yyyy", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 1);
        String result = printer.format(cal.getTime());
        assertEquals("Today is 2023", result);
    }

    // Tests format(Object) with Date object
    @Test
    public void testFormatObject_date_returnsFormattedString() {
        FastDatePrinter printer = new FastDatePrinter("yyyy", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 1);
        StringBuffer buf = new StringBuffer();
        printer.format((Object) cal.getTime(), buf, new FieldPosition(0));
        assertEquals("2023", buf.toString());
    }

    // Tests format(Object) with Calendar object
    @Test
    public void testFormatObject_calendar_returnsFormattedString() {
        FastDatePrinter printer = new FastDatePrinter("yyyy", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 1);
        StringBuffer buf = new StringBuffer();
        printer.format((Object) cal, buf, new FieldPosition(0));
        assertEquals("2023", buf.toString());
    }

    // Tests format(Object) with Long object
    @Test
    public void testFormatObject_long_returnsFormattedString() {
        FastDatePrinter printer = new FastDatePrinter("yyyy", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 1);
        StringBuffer buf = new StringBuffer();
        printer.format((Object) cal.getTimeInMillis(), buf, new FieldPosition(0));
        assertEquals("2023", buf.toString());
    }

    // Tests format(Object) with invalid type throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testFormatObject_invalidType_throwsIllegalArgumentException() {
        FastDatePrinter printer = new FastDatePrinter("yyyy", TimeZone.getTimeZone("UTC"), Locale.US);
        printer.format((Object) "invalid", new StringBuffer(), new FieldPosition(0));
    }

    // Tests that getPattern returns the correct pattern
    @Test
    public void testGetPattern_returnsPattern() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals("yyyy-MM-dd", printer.getPattern());
    }

    // Tests that getTimeZone returns the correct time zone
    @Test
    public void testGetTimeZone_returnsTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDatePrinter printer = new FastDatePrinter("yyyy", tz, Locale.US);
        assertEquals(tz, printer.getTimeZone());
    }

    // Tests that getLocale returns the correct locale
    @Test
    public void testGetLocale_returnsLocale() {
        Locale locale = Locale.US;
        FastDatePrinter printer = new FastDatePrinter("yyyy", TimeZone.getTimeZone("UTC"), locale);
        assertEquals(locale, printer.getLocale());
    }

    // Tests equals method - same objects should be equal
    @Test
    public void testEquals_samePatternTimeZoneLocale_returnsTrue() {
        FastDatePrinter p1 = new FastDatePrinter("yyyy", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDatePrinter p2 = new FastDatePrinter("yyyy", TimeZone.getTimeZone("UTC"), Locale.US);
        assertTrue(p1.equals(p2));
    }

    // Tests equals method - different objects should not be equal
    @Test
    public void testEquals_differentPattern_returnsFalse() {
        FastDatePrinter p1 = new FastDatePrinter("yyyy", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDatePrinter p2 = new FastDatePrinter("yy", TimeZone.getTimeZone("UTC"), Locale.US);
        assertFalse(p1.equals(p2));
    }

    // Tests that hashCode is consistent with equals
    @Test
    public void testHashCode_equalObjects_haveSameHashCode() {
        FastDatePrinter p1 = new FastDatePrinter("yyyy", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDatePrinter p2 = new FastDatePrinter("yyyy", TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    // Tests toString returns expected format
    @Test
    public void testToString_returnsDebugString() {
        FastDatePrinter printer = new FastDatePrinter("yyyy", TimeZone.getTimeZone("UTC"), Locale.US);
        String result = printer.toString();
        assertTrue(result.contains("FastDatePrinter"));
        assertTrue(result.contains("yyyy"));
        assertTrue(result.contains("en_US"));
        assertTrue(result.contains("UTC"));
    }

    // Tests that illegal pattern throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_invalidPattern_throwsIllegalArgumentException() {
        new FastDatePrinter("X", TimeZone.getTimeZone("UTC"), Locale.US);
    }

    // Tests constructor with null pattern throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testConstructor_nullPattern_throwsNullPointerException() {
        new FastDatePrinter(null, TimeZone.getTimeZone("UTC"), Locale.US);
    }

    // Tests constructor with null timezone throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testConstructor_nullTimeZone_throwsNullPointerException() {
        new FastDatePrinter("yyyy", null, Locale.US);
    }

    // Tests constructor with null locale throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testConstructor_nullLocale_throwsNullPointerException() {
        new FastDatePrinter("yyyy", TimeZone.getTimeZone("UTC"), null);
    }
}