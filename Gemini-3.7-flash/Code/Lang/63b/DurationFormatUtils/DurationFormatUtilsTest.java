package org.apache.commons.lang.time;

import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class DurationFormatUtilsTest {

    // Tests constructor instantiation
    @Test
    public void testConstructor_default_instantiatesCorrectly() {
        DurationFormatUtils utils = new DurationFormatUtils();
        assertNotNull(utils);
    }

    // Tests formatDurationHMS with standard duration
    @Test
    public void testFormatDurationHMS_standardDuration_returnsFormattedString() {
        long duration = (2 * 3600 + 30 * 60 + 15) * 1000L + 456;
        assertEquals("2:30:15.456", DurationFormatUtils.formatDurationHMS(duration));
    }

    // Tests formatDurationISO
    @Test
    public void testFormatDurationISO_standardDuration_returnsCorrectISOString() {
        long duration = (7 * 86400 + 6 * 3600 + 5 * 60 + 4) * 1000L + 321;
        assertEquals("P0Y0M7DT6H5M4.321S", DurationFormatUtils.formatDurationISO(duration));
    }

    // Tests formatDuration with padWithZeros true and false
    @Test
    public void testFormatDuration_paddingOptions_returnsExpectedPadding() {
        long duration = (5 * 60 + 9) * 1000L + 8;
        assertEquals("05:09.008", DurationFormatUtils.formatDuration(duration, "mm:ss.SSS", true));
        assertEquals("5:9.8", DurationFormatUtils.formatDuration(duration, "m:s.S", false));
    }

    // Tests formatDuration with custom literal tokens in format
    @Test
    public void testFormatDuration_literalQuotes_preservesLiterals() {
        long duration = 3600 * 1000L + 120 * 1000L;
        String formatted = DurationFormatUtils.formatDuration(duration, "'Duration: 'H' hours and 'm' minutes'");
        assertEquals("Duration: 1 hours and 2 minutes", formatted);
    }

    // Tests formatDurationWords with suppressed leading/trailing zeros and plurals
    @Test
    public void testFormatDurationWords_suppressLeadingAndTrailing_handlesCorrectly() {
        long oneDayOneHour = (24 + 1) * 3600 * 1000L;
        String words = DurationFormatUtils.formatDurationWords(oneDayOneHour, true, true);
        assertEquals("1 day 1 hour", words);

        long multiDays = (2 * 24 + 3) * 3600 * 1000L + 4 * 60 * 1000L + 5000L;
        String wordsMulti = DurationFormatUtils.formatDurationWords(multiDays, false, false);
        assertEquals("2 days 3 hours 4 minutes 5 seconds", wordsMulti);

        String zeroDuration = DurationFormatUtils.formatDurationWords(0, true, true);
        assertEquals("", zeroDuration);

        String zeroSuppressLeadingOnly = DurationFormatUtils.formatDurationWords(0, true, false);
        assertEquals("", zeroSuppressLeadingOnly);

        String zeroSuppressTrailingOnly = DurationFormatUtils.formatDurationWords(0, false, true);
        assertEquals("", zeroSuppressTrailingOnly);
    }

    // Tests formatPeriodISO
    @Test
    public void testFormatPeriodISO_validRange_returnsISOPeriod() {
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        start.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        end.set(2021, Calendar.FEBRUARY, 15, 10, 20, 30);
        end.set(Calendar.MILLISECOND, 400);

        String iso = DurationFormatUtils.formatPeriodISO(start.getTimeInMillis(), end.getTimeInMillis());
        assertEquals("P1Y1M14DT10H20M30.400S", iso);
    }

    // Tests formatPeriod with duration less than 28 days (short-circuit branch)
    @Test
    public void testFormatPeriod_durationLessThan28Days_formatsAsDuration() {
        long start = 10000L;
        long end = start + (5 * 86400 * 1000L);
        assertEquals("5", DurationFormatUtils.formatPeriod(start, end, "d"));
    }

    // Tests formatPeriod with month boundary difference (Lang-63 regression)
    @Test
    public void testFormatPeriod_monthDifference_calculatesAccurately() {
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        start.set(2005, Calendar.NOVEMBER, 1, 0, 0, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        end.set(2005, Calendar.DECEMBER, 1, 0, 0, 0);
        end.set(Calendar.MILLISECOND, 0);

        assertEquals("1", DurationFormatUtils.formatPeriod(start.getTimeInMillis(), end.getTimeInMillis(), "M", false, TimeZone.getTimeZone("GMT")));
        assertEquals("0", DurationFormatUtils.formatPeriod(start.getTimeInMillis(), end.getTimeInMillis(), "y", false, TimeZone.getTimeZone("GMT")));
    }

    // Tests formatPeriod when format excludes higher-order fields (years, months, days, hours, minutes)
    @Test
    public void testFormatPeriod_omittedFields_accumulatesToRemainingFields() {
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        start.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        end.set(2021, Calendar.JANUARY, 1, 0, 0, 0);
        end.set(Calendar.MILLISECOND, 0);

        // Omit years, request months
        assertEquals("12", DurationFormatUtils.formatPeriod(start.getTimeInMillis(), end.getTimeInMillis(), "M", false, TimeZone.getTimeZone("GMT")));

        // Omit years and months, request days
        assertEquals("366", DurationFormatUtils.formatPeriod(start.getTimeInMillis(), end.getTimeInMillis(), "d", false, TimeZone.getTimeZone("GMT")));

        // Omit years, months, days, request hours
        assertEquals("8784", DurationFormatUtils.formatPeriod(start.getTimeInMillis(), end.getTimeInMillis(), "H", false, TimeZone.getTimeZone("GMT")));
    }

    // Tests formatPeriod with negative field adjustments in loop (seconds, minutes, hours, days, months)
    @Test
    public void testFormatPeriod_negativeFieldDifferences_adjustsCorrectly() {
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        start.set(2020, Calendar.MAY, 25, 23, 55, 50);
        start.set(Calendar.MILLISECOND, 900);

        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        end.set(2020, Calendar.JULY, 10, 1, 5, 10);
        end.set(Calendar.MILLISECOND, 100);

        String result = DurationFormatUtils.formatPeriod(
                start.getTimeInMillis(),
                end.getTimeInMillis(),
                "M'm 'd'd 'H'h 'm'm 's's 'S'ms'",
                false,
                TimeZone.getTimeZone("GMT")
        );
        assertEquals("1m 15d 1h 9m 19s 200ms", result);
    }

    // Tests Token inner class equals, hashCode, and toString
    @Test
    public void testToken_equalsAndHashCodeAndToString_behavesCorrectly() {
        DurationFormatUtils.Token token1 = new DurationFormatUtils.Token("y", 2);
        DurationFormatUtils.Token token2 = new DurationFormatUtils.Token("y", 2);
        DurationFormatUtils.Token token3 = new DurationFormatUtils.Token("y", 1);
        DurationFormatUtils.Token token4 = new DurationFormatUtils.Token("M", 2);
        DurationFormatUtils.Token tokenBuffer1 = new DurationFormatUtils.Token(new StringBuffer("abc"));
        DurationFormatUtils.Token tokenBuffer2 = new DurationFormatUtils.Token(new StringBuffer("abc"));
        DurationFormatUtils.Token tokenNumber1 = new DurationFormatUtils.Token(Integer.valueOf(10));
        DurationFormatUtils.Token tokenNumber2 = new DurationFormatUtils.Token(Integer.valueOf(10));

        assertEquals(token1, token2);
        assertEquals(token1.hashCode(), token2.hashCode());
        assertFalse(token1.equals(token3));
        assertFalse(token1.equals(token4));
        assertFalse(token1.equals("notAToken"));
        assertFalse(token1.equals(null));

        assertEquals(tokenBuffer1, tokenBuffer2);
        assertEquals(tokenNumber1, tokenNumber2);

        token3.increment();
        assertEquals(2, token3.getCount());
        assertEquals("yy", token1.toString());
    }

    // Tests Token.containsTokenWithValue
    @Test
    public void testToken_containsTokenWithValue_returnsCorrectBoolean() {
        DurationFormatUtils.Token[] tokens = DurationFormatUtils.lexx("yyyy-MM-dd");
        assertTrue(DurationFormatUtils.Token.containsTokenWithValue(tokens, "y"));
        assertTrue(DurationFormatUtils.Token.containsTokenWithValue(tokens, "M"));
        assertTrue(DurationFormatUtils.Token.containsTokenWithValue(tokens, "d"));
        assertFalse(DurationFormatUtils.Token.containsTokenWithValue(tokens, "H"));
    }
}