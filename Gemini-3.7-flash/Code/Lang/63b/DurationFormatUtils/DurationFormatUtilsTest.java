package org.apache.commons.lang.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.TimeZone;

public class DurationFormatUtilsTest {

    // Tests constructor
    @Test
    public void testConstructor_defaultInstance_createdSuccessfully() {
        DurationFormatUtils utils = new DurationFormatUtils();
        assertNotNull(utils);
    }

    // Tests formatDurationHMS method
    @Test
    public void testFormatDurationHMS_standardDuration_formatsCorrectly() {
        long duration = (2 * 3600 + 30 * 60 + 15) * 1000L + 123;
        String result = DurationFormatUtils.formatDurationHMS(duration);
        assertEquals("2:30:15.123", result);
    }

    // Tests formatDurationISO method
    @Test
    public void testFormatDurationISO_standardDuration_formatsCorrectly() {
        long duration = (7 * 86400 + 6 * 3600 + 5 * 60 + 4) * 1000L + 321;
        String result = DurationFormatUtils.formatDurationISO(duration);
        assertEquals("P0Y0M7DT6H5M4.321S", result);
    }

    // Tests formatDuration with padWithZeros true and false
    @Test
    public void testFormatDuration_customFormat_formatsWithAndWithoutPadding() {
        long duration = (1 * 3600 + 2 * 60 + 3) * 1000L + 4;
        String padded = DurationFormatUtils.formatDuration(duration, "HH:mm:ss.SSS", true);
        assertEquals("01:02:03.004", padded);

        String unpadded = DurationFormatUtils.formatDuration(duration, "H:m:s.S", false);
        assertEquals("1:2:3.4", unpadded);
    }

    // Tests formatDuration with literals in format
    @Test
    public void testFormatDuration_literalQuotes_retainsLiterals() {
        long duration = 65000L;
        String result = DurationFormatUtils.formatDuration(duration, "m' minutes, 's' seconds'");
        assertEquals("1 minutes, 5 seconds", result);
    }

    // Tests formatDurationWords with all combinations of suppressing zero elements
    @Test
    public void testFormatDurationWords_variousSuppressFlags_formatsAppropriately() {
        long durationZero = 0L;
        assertEquals("", DurationFormatUtils.formatDurationWords(durationZero, true, true));
        assertEquals("0 days 0 hours 0 minutes 0 seconds", DurationFormatUtils.formatDurationWords(durationZero, false, false));

        long durationOne = (1 * 86400 + 1 * 3600 + 1 * 60 + 1) * 1000L;
        assertEquals("1 day 1 hour 1 minute 1 second", DurationFormatUtils.formatDurationWords(durationOne, true, true));

        long durationLeadingZero = (2 * 60 + 3) * 1000L;
        assertEquals("2 minutes 3 seconds", DurationFormatUtils.formatDurationWords(durationLeadingZero, true, false));

        long durationTrailingZero = (2 * 3600) * 1000L;
        assertEquals("2 hours", DurationFormatUtils.formatDurationWords(durationTrailingZero, false, true));
    }

    // Tests formatPeriodISO method
    @Test
    public void testFormatPeriodISO_standardPeriod_formatsCorrectly() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar cal1 = Calendar.getInstance(tz);
        cal1.set(2005, Calendar.JANUARY, 1, 0, 0, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance(tz);
        cal2.set(2006, Calendar.FEBRUARY, 2, 1, 2, 3);
        cal2.set(Calendar.MILLISECOND, 4);

        String result = DurationFormatUtils.formatPeriodISO(cal1.getTimeInMillis(), cal2.getTimeInMillis());
        assertNotNull(result);
        assertTrue(result.startsWith("P"));
    }

    // Tests formatPeriod for duration less than 28 days
    @Test
    public void testFormatPeriod_shortDurationLessThan28Days_delegatesToFormatDuration() {
        long start = 0L;
        long end = 10 * DateUtils.MILLIS_PER_DAY + 5 * DateUtils.MILLIS_PER_HOUR;
        String result = DurationFormatUtils.formatPeriod(start, end, "d' days 'H' hours'");
        assertEquals("10 days 5 hours", result);
    }

    // Tests formatPeriod across months (Regression test for Defect 63)
    @Test
    public void testFormatPeriod_acrossMonths_calculatesDifferenceAccurately() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar cal1 = Calendar.getInstance(tz);
        cal1.set(2005, Calendar.NOVEMBER, 15, 0, 0, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance(tz);
        cal2.set(2005, Calendar.DECEMBER, 15, 0, 0, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        String result = DurationFormatUtils.formatPeriod(cal1.getTimeInMillis(), cal2.getTimeInMillis(), "M", false, tz);
        assertEquals("1", result);

        String resultDays = DurationFormatUtils.formatPeriod(cal1.getTimeInMillis(), cal2.getTimeInMillis(), "d", false, tz);
        assertEquals("30", resultDays);
    }

    // Tests formatPeriod across month boundary with different day counts
    @Test
    public void testFormatPeriod_monthBoundary_calculatesCorrectDifference() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar cal1 = Calendar.getInstance(tz);
        cal1.set(2005, Calendar.JANUARY, 15, 0, 0, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance(tz);
        cal2.set(2005, Calendar.FEBRUARY, 15, 0, 0, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        String resultMonth = DurationFormatUtils.formatPeriod(cal1.getTimeInMillis(), cal2.getTimeInMillis(), "M", false, tz);
        assertEquals("1", resultMonth);

        String resultDays = DurationFormatUtils.formatPeriod(cal1.getTimeInMillis(), cal2.getTimeInMillis(), "d", false, tz);
        assertEquals("31", resultDays);
    }

    // Tests formatPeriod across years
    @Test
    public void testFormatPeriod_acrossYears_calculatesYearAndMonthCorrectly() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar cal1 = Calendar.getInstance(tz);
        cal1.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance(tz);
        cal2.set(2003, Calendar.MARCH, 1, 0, 0, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        String result = DurationFormatUtils.formatPeriod(cal1.getTimeInMillis(), cal2.getTimeInMillis(), "y'y 'M'm 'd'd'", false, tz);
        assertEquals("3y 2m 0d", result);
    }

    // Tests formatPeriod when only minutes/seconds/millis are requested
    @Test
    public void testFormatPeriod_subDayUnitsOnly_rollsUpValues() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar cal1 = Calendar.getInstance(tz);
        cal1.set(2005, Calendar.JANUARY, 1, 0, 0, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance(tz);
        cal2.set(2005, Calendar.FEBRUARY, 1, 2, 3, 4);
        cal2.set(Calendar.MILLISECOND, 500);

        String resultHours = DurationFormatUtils.formatPeriod(cal1.getTimeInMillis(), cal2.getTimeInMillis(), "H", false, tz);
        int expectedHours = 31 * 24 + 2;
        assertEquals(Integer.toString(expectedHours), resultHours);

        String resultMinutes = DurationFormatUtils.formatPeriod(cal1.getTimeInMillis(), cal2.getTimeInMillis(), "m", false, tz);
        int expectedMinutes = expectedHours * 60 + 3;
        assertEquals(Integer.toString(expectedMinutes), resultMinutes);
    }

    // Tests formatPeriod with default timezone and default format
    @Test
    public void testFormatPeriod_defaultTimezone_formatsCorrectly() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2005, Calendar.JANUARY, 1, 0, 0, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.set(2005, Calendar.FEBRUARY, 15, 0, 0, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        String result = DurationFormatUtils.formatPeriod(cal1.getTimeInMillis(), cal2.getTimeInMillis(), "M'm 'd'd'");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    // Tests Token equality and hashCode methods
    @Test
    public void testToken_equalsAndHashCode_evaluatesCorrectly() {
        DurationFormatUtils.Token token1 = new DurationFormatUtils.Token("y", 1);
        DurationFormatUtils.Token token2 = new DurationFormatUtils.Token("y", 1);
        DurationFormatUtils.Token token3 = new DurationFormatUtils.Token("y", 2);
        DurationFormatUtils.Token token4 = new DurationFormatUtils.Token("M", 1);
        DurationFormatUtils.Token bufferToken1 = new DurationFormatUtils.Token(new StringBuffer("abc"));
        DurationFormatUtils.Token bufferToken2 = new DurationFormatUtils.Token(new StringBuffer("abc"));
        DurationFormatUtils.Token numberToken1 = new DurationFormatUtils.Token(Integer.valueOf(10));
        DurationFormatUtils.Token numberToken2 = new DurationFormatUtils.Token(Integer.valueOf(10));

        assertEquals(token1, token2);
        assertEquals(token1.hashCode(), token2.hashCode());
        assertNotEquals(token1, token3);
        assertNotEquals(token1, token4);
        assertNotEquals(token1, null);
        assertNotEquals(token1, "y");

        assertEquals(bufferToken1, bufferToken2);
        assertEquals(numberToken1, numberToken2);
        assertEquals("yy", token3.toString());
    }

    // Tests lexx tokenizer
    @Test
    public void testLexx_variousTokens_parsesSuccessfully() {
        DurationFormatUtils.Token[] tokens = DurationFormatUtils.lexx("yyyy-MM-dd'T'HH:mm:ss.SSS");
        assertNotNull(tokens);
        assertTrue(tokens.length > 0);
        assertTrue(DurationFormatUtils.Token.containsTokenWithValue(tokens, "y"));
        assertTrue(DurationFormatUtils.Token.containsTokenWithValue(tokens, "M"));
        assertTrue(DurationFormatUtils.Token.containsTokenWithValue(tokens, "d"));
        assertTrue(DurationFormatUtils.Token.containsTokenWithValue(tokens, "H"));
        assertTrue(DurationFormatUtils.Token.containsTokenWithValue(tokens, "m"));
        assertTrue(DurationFormatUtils.Token.containsTokenWithValue(tokens, "s"));
        assertTrue(DurationFormatUtils.Token.containsTokenWithValue(tokens, "S"));
    }
}