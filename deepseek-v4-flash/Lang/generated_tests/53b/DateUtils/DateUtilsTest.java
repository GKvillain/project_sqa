package org.apache.commons.lang.time;

import static org.junit.Assert.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

public class DateUtilsTest {

    @Before
    public void setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    // Test isSameDay with null first argument throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDay_nullDate_throwsException() {
        DateUtils.isSameDay((Date) null, new Date());
    }

    // Test isSameDay with same day returns true
    @Test
    public void testIsSameDay_sameDay_returnsTrue() {
        Date d1 = new Date(1000000);
        Date d2 = new Date(d1.getTime());
        assertTrue(DateUtils.isSameDay(d1, d2));
    }

    // Test isSameDay with different days returns false
    @Test
    public void testIsSameDay_differentDay_returnsFalse() {
        Date d1 = new Date(1000000);
        Date d2 = new Date(d1.getTime() + 86400000L);
        assertFalse(DateUtils.isSameDay(d1, d2));
    }

    // Test isSameInstant with null first argument throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testIsSameInstant_nullDate_throwsException() {
        DateUtils.isSameInstant((Date) null, new Date());
    }

    // Test addYears with positive amount
    @Test
    public void testAddYears_positiveAmount_addsCorrectly() {
        Date base = new Date(0L);
        Date result = DateUtils.addYears(base, 1);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(1971, cal.get(Calendar.YEAR));
    }

    // Test addYears with negative amount
    @Test
    public void testAddYears_negativeAmount_subtractsCorrectly() {
        Date base = new Date(0L);
        Date result = DateUtils.addYears(base, -1);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(1969, cal.get(Calendar.YEAR));
    }

    // Test addDays with null input throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testAddDays_nullInput_throwsException() {
        DateUtils.addDays(null, 1);
    }

    // Test addMonths with zero amount returns same date
    @Test
    public void testAddMonths_zeroAmount_returnsSameDate() {
        Date base = new Date(0L);
        Date result = DateUtils.addMonths(base, 0);
        assertEquals(base.getTime(), result.getTime());
    }

    // Test parseDate with null input throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testParseDate_nullInput_throwsException() throws ParseException {
        DateUtils.parseDate(null, new String[]{"yyyy-MM-dd"});
    }

    // Test parseDate with valid pattern returns correct date
    @Test
    public void testParseDate_validPattern_returnsDate() throws ParseException {
        Date d = DateUtils.parseDate("2023-12-25", new String[]{"yyyy-MM-dd"});
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, cal.get(Calendar.MONTH));
        assertEquals(25, cal.get(Calendar.DAY_OF_MONTH));
    }

    // Test parseDate when no pattern matches throws ParseException
    @Test(expected = ParseException.class)
    public void testParseDate_noPatternMatch_throwsParseException() throws ParseException {
        DateUtils.parseDate("abc", new String[]{"yyyy-MM-dd"});
    }

    // Test round to hour in UTC (round down)
    @Test
    public void testRound_hourField_utcRoundDown() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2023, Calendar.JANUARY, 1, 13, 20, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Calendar rounded = DateUtils.round(cal, Calendar.HOUR_OF_DAY);
        assertEquals(13, rounded.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, rounded.get(Calendar.MINUTE));
        assertEquals(0, rounded.get(Calendar.SECOND));
        assertEquals(0, rounded.get(Calendar.MILLISECOND));
    }

    // Test round to hour in UTC (round up)
    @Test
    public void testRound_hourField_utcRoundUp() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2023, Calendar.JANUARY, 1, 13, 40, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Calendar rounded = DateUtils.round(cal, Calendar.HOUR_OF_DAY);
        assertEquals(14, rounded.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, rounded.get(Calendar.MINUTE));
        assertEquals(0, rounded.get(Calendar.SECOND));
        assertEquals(0, rounded.get(Calendar.MILLISECOND));
    }

    // Test round to hour during DST spring forward (edge case for DST transition)
    @Test
    public void testRound_hourField_dstSpringForward_roundsCorrectly() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        // 01:10 EST -> round down to 01:00
        Calendar cal = Calendar.getInstance(tz);
        cal.set(2023, Calendar.MARCH, 12, 1, 10, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Calendar rounded = DateUtils.round(cal, Calendar.HOUR_OF_DAY);
        Calendar expected = Calendar.getInstance(tz);
        expected.set(2023, Calendar.MARCH, 12, 1, 0, 0);
        expected.set(Calendar.MILLISECOND, 0);
        assertEquals(expected.getTime(), rounded.getTime());
        
        // 01:40 EST -> round up to 03:00 EDT (skip 02:00 due to DST)
        cal.set(2023, Calendar.MARCH, 12, 1, 40, 0);
        rounded = DateUtils.round(cal, Calendar.HOUR_OF_DAY);
        expected.set(2023, Calendar.MARCH, 12, 3, 0, 0);
        assertEquals(expected.getTime(), rounded.getTime());
    }

    // Test round to SEMI_MONTH (date = 1, should become 16)
    @Test
    public void testRound_semiMonthField_roundsFirstDay() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2023, Calendar.JANUARY, 1, 10, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Calendar rounded = DateUtils.round(cal, DateUtils.SEMI_MONTH);
        assertEquals(16, rounded.get(Calendar.DAY_OF_MONTH));
        assertEquals(Calendar.JANUARY, rounded.get(Calendar.MONTH));
    }

    // Test truncate to month
    @Test
    public void testTruncate_monthField_truncatesCorrectly() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2023, Calendar.MARCH, 15, 10, 30, 45);
        cal.set(Calendar.MILLISECOND, 123);
        Calendar truncated = DateUtils.truncate(cal, Calendar.MONTH);
        assertEquals(2023, truncated.get(Calendar.YEAR));
        assertEquals(Calendar.MARCH, truncated.get(Calendar.MONTH));
        assertEquals(1, truncated.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, truncated.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, truncated.get(Calendar.MINUTE));
        assertEquals(0, truncated.get(Calendar.SECOND));
        assertEquals(0, truncated.get(Calendar.MILLISECOND));
    }

    // Test truncate with null input throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testTruncate_nullInput_throwsException() {
        DateUtils.truncate((Date) null, Calendar.DAY_OF_MONTH);
    }

    // Test iterator for RANGE_MONTH_SUNDAY returns correct range
    @Test
    public void testIterator_monthRangeSunday_returnsCorrectIterator() {
        Date focus = new Date(0L); // 1970-01-01 00:00 UTC (Thursday)
        Iterator it = DateUtils.iterator(focus, DateUtils.RANGE_MONTH_SUNDAY);
        assertNotNull(it);
        // First date should be Sunday Dec 28, 1969
        Calendar first = (Calendar) it.next();
        Calendar expectedFirst = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expectedFirst.set(1969, Calendar.DECEMBER, 28, 0, 0, 0);
        expectedFirst.set(Calendar.MILLISECOND, 0);
        assertEquals(expectedFirst.getTime(), first.getTime());
        // Last date should be Saturday Jan 31, 1970
        Calendar last = null;
        while (it.hasNext()) {
            last = (Calendar) it.next();
        }
        Calendar expectedLast = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expectedLast.set(1970, Calendar.JANUARY, 31, 0, 0, 0);
        expectedLast.set(Calendar.MILLISECOND, 0);
        assertEquals(expectedLast.getTime(), last.getTime());
    }

    // Test iterator with null focus throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testIterator_nullFocus_throwsException() {
        DateUtils.iterator((Date) null, DateUtils.RANGE_WEEK_SUNDAY);
    }

    // Test isSameLocalTime with null input throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testIsSameLocalTime_nullInput_throwsException() {
        DateUtils.isSameLocalTime((Calendar) null, Calendar.getInstance());
    }

    // ==================== New tests for uncovered areas ====================

    // Test isSameDay with Calendar arguments (same day)
    @Test
    public void testIsSameDay_calendarSameDay_returnsTrue() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        assertTrue(DateUtils.isSameDay(cal1, cal2));
    }

    // Test isSameDay with Calendar arguments (different day)
    @Test
    public void testIsSameDay_calendarDifferentDay_returnsFalse() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_MONTH, 1);
        assertFalse(DateUtils.isSameDay(cal1, cal2));
    }

    // Test isSameDay with null Calendar first argument throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDay_nullCalendar_throwsException() {
        DateUtils.isSameDay((Calendar) null, Calendar.getInstance());
    }

    // Test isSameInstant with Calendar arguments (same instant)
    @Test
    public void testIsSameInstant_calendarSameInstant_returnsTrue() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        assertTrue(DateUtils.isSameInstant(cal1, cal2));
    }

    // Test isSameInstant with Calendar arguments (different instant)
    @Test
    public void testIsSameInstant_calendarDifferentInstant_returnsFalse() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.HOUR, 1);
        assertFalse(DateUtils.isSameInstant(cal1, cal2));
    }

    // Test isSameInstant with null Calendar first argument throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testIsSameInstant_nullCalendar_throwsException() {
        DateUtils.isSameInstant((Calendar) null, Calendar.getInstance());
    }

    // Test isSameLocalTime with same calendar returns true
    @Test
    public void testIsSameLocalTime_sameCalendar_returnsTrue() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        assertTrue(DateUtils.isSameLocalTime(cal1, cal2));
    }

    // Test isSameLocalTime with different time returns false
    @Test
    public void testIsSameLocalTime_differentTime_returnsFalse() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.HOUR, 1);
        assertFalse(DateUtils.isSameLocalTime(cal1, cal2));
    }

    // Test addHours with positive amount
    @Test
    public void testAddHours_positiveAmount_addsCorrectly() {
        Date base = new Date(0L);
        Date result = DateUtils.addHours(base, 2);
        assertEquals(7200000L, result.getTime());
    }

    // Test addHours with negative amount
    @Test
    public void testAddHours_negativeAmount_subtractsCorrectly() {
        Date base = new Date(7200000L);
        Date result = DateUtils.addHours(base, -1);
        assertEquals(3600000L, result.getTime());
    }

    // Test addMinutes with positive amount
    @Test
    public void testAddMinutes_positiveAmount_addsCorrectly() {
        Date base = new Date(0L);
        Date result = DateUtils.addMinutes(base, 30);
        assertEquals(1800000L, result.getTime());
    }

    // Test addMinutes with negative amount
    @Test
    public void testAddMinutes_negativeAmount_subtractsCorrectly() {
        Date base = new Date(3600000L);
        Date result = DateUtils.addMinutes(base, -30);
        assertEquals(1800000L, result.getTime());
    }

    // Test addSeconds with positive amount
    @Test
    public void testAddSeconds_positiveAmount_addsCorrectly() {
        Date base = new Date(0L);
        Date result = DateUtils.addSeconds(base, 45);
        assertEquals(45000L, result.getTime());
    }

    // Test addSeconds with negative amount
    @Test
    public void testAddSeconds_negativeAmount_subtractsCorrectly() {
        Date base = new Date(60000L);
        Date result = DateUtils.addSeconds(base, -15);
        assertEquals(45000L, result.getTime());
    }

    // Test addWeeks with positive amount
    @Test
    public void testAddWeeks_positiveAmount_addsCorrectly() {
        Date base = new Date(0L);
        Date result = DateUtils.addWeeks(base, 1);
        assertEquals(604800000L, result.getTime());
    }

    // Test addWeeks with negative amount
    @Test
    public void testAddWeeks_negativeAmount_subtractsCorrectly() {
        Date base = new Date(604800000L);
        Date result = DateUtils.addWeeks(base, -1);
        assertEquals(0L, result.getTime());
    }

    // Test round to YEAR field
    @Test
    public void testRound_yearField_roundsCorrectly() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2023, Calendar.JULY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Calendar rounded = DateUtils.round(cal, Calendar.YEAR);
        assertEquals(2024, rounded.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, rounded.get(Calendar.MONTH));
        assertEquals(1, rounded.get(Calendar.DAY_OF_MONTH));
    }

    // Test round to MONTH field
    @Test
    public void testRound_monthField_roundsCorrectly() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2023, Calendar.JANUARY, 20, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Calendar rounded = DateUtils.round(cal, Calendar.MONTH);
        assertEquals(2023, rounded.get(Calendar.YEAR));
        assertEquals(Calendar.FEBRUARY, rounded.get(Calendar.MONTH));
        assertEquals(1, rounded.get(Calendar.DAY_OF_MONTH));
    }

    // Test truncate to YEAR field
    @Test
    public void testTruncate_yearField_truncatesCorrectly() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2023, Calendar.JULY, 15, 10, 30, 45);
        cal.set(Calendar.MILLISECOND, 123);
        Calendar truncated = DateUtils.truncate(cal, Calendar.YEAR);
        assertEquals(2023, truncated.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, truncated.get(Calendar.MONTH));
        assertEquals(1, truncated.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, truncated.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, truncated.get(Calendar.MINUTE));
        assertEquals(0, truncated.get(Calendar.SECOND));
        assertEquals(0, truncated.get(Calendar.MILLISECOND));
    }

    // Test truncate to DAY_OF_MONTH field
    @Test
    public void testTruncate_dayField_truncatesCorrectly() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2023, Calendar.JANUARY, 15, 10, 30, 45);
        cal.set(Calendar.MILLISECOND, 123);
        Calendar truncated = DateUtils.truncate(cal, Calendar.DAY_OF_MONTH);
        assertEquals(2023, truncated.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, truncated.get(Calendar.MONTH));
        assertEquals(15, truncated.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, truncated.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, truncated.get(Calendar.MINUTE));
        assertEquals(0, truncated.get(Calendar.SECOND));
        assertEquals(0, truncated.get(Calendar.MILLISECOND));
    }

    // Test round with null Date argument throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testRound_nullDate_throwsException() {
        DateUtils.round((Date) null, Calendar.HOUR_OF_DAY);
    }

    // Test round with null Calendar argument throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testRound_nullCalendar_throwsException() {
        DateUtils.round((Calendar) null, Calendar.HOUR_OF_DAY);
    }

    // Test truncate with null Calendar argument throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testTruncate_nullCalendar_throwsException() {
        DateUtils.truncate((Calendar) null, Calendar.DAY_OF_MONTH);
    }

    // Test iterator with null Calendar focus throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testIterator_nullCalendarFocus_throwsException() {
        DateUtils.iterator((Calendar) null, DateUtils.RANGE_WEEK_SUNDAY);
    }

    // Test isSameDay with both null arguments returns true
    @Test
    public void testIsSameDay_bothNull_returnsTrue() {
        assertTrue(DateUtils.isSameDay((Calendar) null, (Calendar) null));
    }

    // Test isSameInstant with both null arguments returns true
    @Test
    public void testIsSameInstant_bothNull_returnsTrue() {
        assertTrue(DateUtils.isSameInstant((Date) null, (Date) null));
    }

    // Test isSameLocalTime with both null arguments returns true
    @Test
    public void testIsSameLocalTime_bothNull_returnsTrue() {
        assertTrue(DateUtils.isSameLocalTime((Calendar) null, (Calendar) null));
    }
}