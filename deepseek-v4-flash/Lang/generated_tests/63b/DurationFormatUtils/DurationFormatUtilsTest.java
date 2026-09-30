package org.apache.commons.lang.time;

import org.junit.Test;
import java.util.Calendar;
import java.util.TimeZone;
import static org.junit.Assert.*;

public class DurationFormatUtilsTest {

    @Test
    public void testFormatDuration_normal() {
        // 1 day, 2 hours, 3 minutes, 4 seconds, 500 millis
        long millis = 1 * 86400000L + 2 * 3600000L + 3 * 60000L + 4 * 1000L + 500L;
        String result = DurationFormatUtils.formatDuration(millis, "d 'days' H 'hours' m 'minutes' s 'seconds' S 'ms'");
        assertEquals("1 days 2 hours 3 minutes 4 seconds 500 ms", result);
    }

    @Test
    public void testFormatDuration_zero() {
        String result = DurationFormatUtils.formatDuration(0, "H:mm:ss.SSS");
        assertEquals("0:00:00.000", result);
    }

    @Test
    public void testFormatDuration_negative() {
        // Negative duration does not throw and produces expected negative value
        String result = DurationFormatUtils.formatDuration(-1000, "s");
        assertEquals("-1", result);
    }

    @Test
    public void testFormatDurationHMS() {
        // 1 hour, 2 minutes, 3.456 seconds
        long millis = 3723456;
        String result = DurationFormatUtils.formatDurationHMS(millis);
        assertEquals("1:02:03.456", result);
    }

    @Test
    public void testFormatDurationISO() {
        // 16 minutes 40 seconds
        long millis = 1000000;
        String result = DurationFormatUtils.formatDurationISO(millis);
        assertEquals("P0000Y0M0DT0H16M40.000S", result);
    }

    @Test
    public void testFormatDurationWords_suppressLeading() {
        String result = DurationFormatUtils.formatDurationWords(60000, true, false);
        assertEquals("1 minute", result);
    }

    @Test
    public void testFormatDurationWords_suppressTrailing() {
        // suppress trailing zero seconds
        String result = DurationFormatUtils.formatDurationWords(60000, false, true);
        assertEquals("0 days 0 hours 1 minute", result);
    }

    @Test
    public void testFormatDurationWords_leadingAndTrailing() {
        // 1 day 1 hour 1 minute 1 second
        long millis = 90061000;
        String result = DurationFormatUtils.formatDurationWords(millis, true, true);
        assertEquals("1 day 1 hour 1 minute 1 second", result);
    }

    @Test
    public void testFormatPeriodISO_crossMonthBoundary() {
        // Defect: bug 63b – should be 2 months, 0 days, not 1 month 31 days
        TimeZone tz = TimeZone.getDefault();
        Calendar start = Calendar.getInstance(tz);
        start.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
        start.set(Calendar.MILLISECOND, 0);
        Calendar end = Calendar.getInstance(tz);
        end.set(2000, Calendar.MARCH, 1, 0, 0, 0);
        end.set(Calendar.MILLISECOND, 0);
        long startMillis = start.getTimeInMillis();
        long endMillis = end.getTimeInMillis();
        String result = DurationFormatUtils.formatPeriodISO(startMillis, endMillis);
        assertEquals("P0000Y2M0DT0H0M0.000S", result);
    }

    @Test
    public void testFormatPeriod_shortDuration() {
        // Less than 28 days, uses formatDuration path
        long start = 0;
        long end = 100000;
        String result = DurationFormatUtils.formatPeriod(start, end, "s");
        assertEquals("100", result);
    }

    @Test
    public void testFormatPeriod_withNegativeAdjust() {
        // Cross midnight, hours become negative then adjusted
        TimeZone tz = TimeZone.getDefault();
        Calendar start = Calendar.getInstance(tz);
        start.set(2000, Calendar.JANUARY, 1, 23, 30, 0);
        start.set(Calendar.MILLISECOND, 0);
        Calendar end = Calendar.getInstance(tz);
        end.set(2000, Calendar.JANUARY, 2, 1, 0, 0);
        end.set(Calendar.MILLISECOND, 0);
        long startMillis = start.getTimeInMillis();
        long endMillis = end.getTimeInMillis();
        String result = DurationFormatUtils.formatPeriod(startMillis, endMillis, "H:m:s");
        assertEquals("1:30:0", result);
    }

    @Test
    public void testFormatPeriod_crossYearBoundary() {
        // Crossing year boundary, should be 1 day difference
        TimeZone tz = TimeZone.getDefault();
        Calendar start = Calendar.getInstance(tz);
        start.set(2000, Calendar.DECEMBER, 31, 0, 0, 0);
        start.set(Calendar.MILLISECOND, 0);
        Calendar end = Calendar.getInstance(tz);
        end.set(2001, Calendar.JANUARY, 1, 0, 0, 0);
        end.set(Calendar.MILLISECOND, 0);
        long startMillis = start.getTimeInMillis();
        long endMillis = end.getTimeInMillis();
        String result = DurationFormatUtils.formatPeriod(startMillis, endMillis, "d 'days'");
        assertEquals("1 days", result);
    }

    @Test
    public void testFormatPeriod_withTimeZone() {
        // Explicit timezone parameter
        TimeZone tz = TimeZone.getTimeZone("GMT+5");
        Calendar start = Calendar.getInstance(tz);
        start.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
        start.set(Calendar.MILLISECOND, 0);
        Calendar end = Calendar.getInstance(tz);
        end.set(2000, Calendar.JANUARY, 2, 0, 0, 0);
        end.set(Calendar.MILLISECOND, 0);
        long startMillis = start.getTimeInMillis();
        long endMillis = end.getTimeInMillis();
        String result = DurationFormatUtils.formatPeriod(startMillis, endMillis, "d 'days'", true, tz);
        assertEquals("1 days", result);
    }

    @Test
    public void testFormatDuration_withoutDays() {
        // No 'd' token, hours should accumulate
        long millis = 90061000; // 1 day 1h 1m 1s
        String result = DurationFormatUtils.formatDuration(millis, "H 'hours' m 'minutes'");
        assertEquals("25 hours 1 minutes", result);
    }

    @Test
    public void testFormatDuration_withoutHours() {
        // No 'H' token, minutes should accumulate
        long millis = 3600000; // 1 hour
        String result = DurationFormatUtils.formatDuration(millis, "m 'minutes'");
        assertEquals("60 minutes", result);
    }

    @Test
    public void testFormatDuration_padWithZerosFalse() {
        // Disable zero padding
        long millis = 3661000; // 1h 1m 1s
        String result = DurationFormatUtils.formatDuration(millis, "H:mm:ss", false);
        assertEquals("1:1:1", result);
    }

    @Test
    public void testFormatDuration_withYearToken() {
        // 'y' token present, but not used for duration computation, output 0 years
        long millis = 86400000;
        String result = DurationFormatUtils.formatDuration(millis, "y 'years' d 'days'");
        assertEquals("0 years 1 days", result);
    }

    @Test
    public void testFormatDuration_secondsAndMillis() {
        // Combined seconds and millis
        long millis = 1500;
        String result = DurationFormatUtils.formatDuration(millis, "s.S");
        assertEquals("1.5", result);
    }

    @Test
    public void testFormatDuration_zeroMillisWithSecond() {
        // 1 second exactly
        long millis = 1000;
        String result = DurationFormatUtils.formatDuration(millis, "s.SSS");
        assertEquals("1.000", result);
    }

    @Test
    public void testFormatDuration_withLiteral() {
        // Literal apostrophe inside pattern
        long millis = 3661000;
        String result = DurationFormatUtils.formatDuration(millis, "H 'o''clock' m");
        assertEquals("1 o'clock 1", result);
    }
}