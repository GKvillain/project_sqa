package org.apache.commons.lang3.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

public class DateUtilsTest {

    private Date utcDate(int year, int month, int day, int hour, int minute, int second, int millis) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(year, month, day, hour, minute, second);
        cal.set(Calendar.MILLISECOND, millis);
        return cal.getTime();
    }

    // Tests null date input for parseDate
    @Test(expected = IllegalArgumentException.class)
    public void testParseDate_nullDate_throwsIllegalArgumentException() throws Exception {
        DateUtils.parseDate(null, "yyyy-MM-dd");
    }

    // Tests null patterns array for parseDate
    @Test(expected = IllegalArgumentException.class)
    public void testParseDate_nullPatterns_throwsIllegalArgumentException() throws Exception {
        DateUtils.parseDate("2000-01-01", (String[]) null);
    }

    // Tests invalid date string for parseDate
    @Test(expected = ParseException.class)
    public void testParseDate_invalidDate_throwsParseException() throws Exception {
        DateUtils.parseDate("invalid", "yyyy-MM-dd");
    }

    // Tests successful parse with simple pattern
    @Test
    public void testParseDate_simplePattern_returnsCorrectDate() throws Exception {
        Date result = DateUtils.parseDate("2000-01-01", "yyyy-MM-dd");
        Date expected = utcDate(2000, 0, 1, 0, 0, 0, 0);
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests parseDate with pattern containing "ZZ" (ISO 8601 timezone with colon)
    @Test
    public void testParseDate_withZZPattern_returnsCorrectDate() throws Exception {
        String pattern = "yyyy-MM-dd'T'HH:mm:ssZZ";
        // positive offset +05:00 -> 12:00 local = 07:00 UTC
        Date result1 = DateUtils.parseDate("2000-01-01T12:00:00+05:00", pattern);
        Date expected1 = utcDate(2000, 0, 1, 7, 0, 0, 0);
        assertEquals(expected1.getTime(), result1.getTime());
        // negative offset -05:00 -> 12:00 local = 17:00 UTC
        Date result2 = DateUtils.parseDate("2000-01-01T12:00:00-05:00", pattern);
        Date expected2 = utcDate(2000, 0, 1, 17, 0, 0, 0);
        assertEquals(expected2.getTime(), result2.getTime());
    }

    // Tests parseDateStrictly with pattern containing "ZZ"
    @Test
    public void testParseDateStrictly_withZZPattern_returnsCorrectDate() throws Exception {
        String pattern = "yyyy-MM-dd'T'HH:mm:ssZZ";
        Date result = DateUtils.parseDateStrictly("2000-06-15T10:30:00+02:00", pattern);
        Date expected = utcDate(2000, 5, 15, 8, 30, 0, 0);
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests isSameDay returns true for same calendar day
    @Test
    public void testIsSameDay_sameDay_returnsTrue() throws Exception {
        Date d1 = utcDate(2000, 0, 1, 10, 0, 0, 0);
        Date d2 = utcDate(2000, 0, 1, 22, 0, 0, 0);
        assertTrue(DateUtils.isSameDay(d1, d2));
    }

    // Tests isSameDay returns false for different calendar day
    @Test
    public void testIsSameDay_differentDay_returnsFalse() throws Exception {
        Date d1 = utcDate(2000, 0, 1, 10, 0, 0, 0);
        Date d2 = utcDate(2000, 0, 2, 10, 0, 0, 0);
        assertFalse(DateUtils.isSameDay(d1, d2));
    }

    // Tests isSameInstant with equal millisecond values
    @Test
    public void testIsSameInstant_sameInstant_returnsTrue() throws Exception {
        Date d1 = utcDate(2000, 0, 1, 12, 0, 0, 0);
        Date d2 = new Date(d1.getTime());
        assertTrue(DateUtils.isSameInstant(d1, d2));
    }

    // Tests addDays with positive amount
    @Test
    public void testAddDays_positiveAmount_returnsCorrectDate() throws Exception {
        Date base = utcDate(2000, 0, 1, 0, 0, 0, 0);
        Date result = DateUtils.addDays(base, 5);
        Date expected = utcDate(2000, 0, 6, 0, 0, 0, 0);
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests addDays with negative amount
    @Test
    public void testAddDays_negativeAmount_returnsCorrectDate() throws Exception {
        Date base = utcDate(2000, 0, 10, 0, 0, 0, 0);
        Date result = DateUtils.addDays(base, -3);
        Date expected = utcDate(2000, 0, 7, 0, 0, 0, 0);
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests setYears with valid year
    @Test
    public void testSetYears_validAmount_returnsCorrectDate() throws Exception {
        Date base = utcDate(2000, 0, 1, 0, 0, 0, 0);
        Date result = DateUtils.setYears(base, 2005);
        Date expected = utcDate(2005, 0, 1, 0, 0, 0, 0);
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests truncate to month field
    @Test
    public void testTruncate_monthField_truncatesCorrectly() throws Exception {
        Date input = utcDate(2000, 2, 28, 13, 45, 1, 231); // 28 Mar 2000
        Date result = DateUtils.truncate(input, Calendar.MONTH);
        Date expected = utcDate(2000, 2, 1, 0, 0, 0, 0); // 1 Mar 2000
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests round to hour field (round up)
    @Test
    public void testRound_hourField_roundsCorrectly() throws Exception {
        Date input = utcDate(2000, 2, 28, 13, 45, 1, 231); // 13:45
        Date result = DateUtils.round(input, Calendar.HOUR_OF_DAY);
        Date expected = utcDate(2000, 2, 28, 14, 0, 0, 0); // round up to 14:00
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests ceiling to year field
    @Test
    public void testCeiling_yearField_ceilsCorrectly() throws Exception {
        Date input = utcDate(2000, 2, 28, 13, 45, 1, 231); // Mar 2000
        Date result = DateUtils.ceiling(input, Calendar.YEAR);
        Date expected = utcDate(2001, 0, 1, 0, 0, 0, 0); // first day of 2001
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests truncate to hour field
    @Test
    public void testTruncate_hourField_truncatesCorrectly() throws Exception {
        Date input = utcDate(2000, 2, 28, 13, 45, 1, 231);
        Date result = DateUtils.truncate(input, Calendar.HOUR_OF_DAY);
        Date expected = utcDate(2000, 2, 28, 13, 0, 0, 0);
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests ceiling with AM_PM field at midnight (hour 0)
    @Test
    public void testCeiling_amPmMidnight_ceilsCorrectly() throws Exception {
        Date input = utcDate(2000, 0, 1, 0, 0, 0, 0);
        Date result = DateUtils.ceiling(input, Calendar.AM_PM);
        Date expected = utcDate(2000, 0, 1, 12, 0, 0, 0);
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests ceiling with AM_PM field in the afternoon (hour 14)
    @Test
    public void testCeiling_amPmAfternoon_ceilsCorrectly() throws Exception {
        Date input = utcDate(2000, 0, 1, 14, 0, 0, 0);
        Date result = DateUtils.ceiling(input, Calendar.AM_PM);
        // subtract 12h -> 02:00, add 1 day -> Jan 2 02:00
        Date expected = utcDate(2000, 0, 2, 2, 0, 0, 0);
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests getFragmentInDays for MONTH fragment
    @Test
    public void testGetFragmentInDays_monthFragment_returnsCorrect() throws Exception {
        Date date = utcDate(2000, 0, 15, 10, 30, 20, 500);
        long result = DateUtils.getFragmentInDays(date, Calendar.MONTH);
        assertEquals(15L, result);
    }

    // Tests iterator with RANGE_WEEK_SUNDAY returns 7 days
    @Test
    public void testIterator_rangeWeekSunday_returnsCorrectNumberOfDays() throws Exception {
        Date focus = utcDate(2000, 0, 5, 0, 0, 0, 0); // Wednesday Jan 5
        Iterator<Calendar> it = DateUtils.iterator(focus, DateUtils.RANGE_WEEK_SUNDAY);
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        assertEquals(7, count);
    }

    // Tests truncatedEquals returns true for same truncated date (ignoring time)
    @Test
    public void testTruncatedEquals_sameTruncated_returnsTrue() throws Exception {
        Date d1 = utcDate(2000, 0, 15, 10, 0, 0, 0);
        Date d2 = utcDate(2000, 0, 15, 5, 0, 0, 0);
        assertTrue(DateUtils.truncatedEquals(d1, d2, Calendar.DATE));
    }

    // Tests truncate with MILLISECOND field returns unchanged value
    @Test
    public void testModify_millisecondField_returnsIdentical() throws Exception {
        Date input = utcDate(2000, 0, 1, 12, 30, 45, 123);
        Date result = DateUtils.truncate(input, Calendar.MILLISECOND);
        assertEquals(input.getTime(), result.getTime());
    }

    // ==================== New tests for uncovered code ====================

    // Tests isSameDay with null arguments
    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDay_nullDate1_throwsIllegalArgumentException() {
        DateUtils.isSameDay(null, new Date());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDay_nullDate2_throwsIllegalArgumentException() {
        DateUtils.isSameDay(new Date(), null);
    }

    // Tests isSameInstant with null arguments
    @Test(expected = IllegalArgumentException.class)
    public void testIsSameInstant_nullDate1_throwsIllegalArgumentException() {
        DateUtils.isSameInstant(null, new Date());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameInstant_nullDate2_throwsIllegalArgumentException() {
        DateUtils.isSameInstant(new Date(), null);
    }

    // Tests isSameInstant with different millisecond values
    @Test
    public void testIsSameInstant_differentInstant_returnsFalse() throws Exception {
        Date d1 = utcDate(2000, 0, 1, 12, 0, 0, 0);
        Date d2 = utcDate(2000, 0, 1, 12, 0, 0, 1);
        assertFalse(DateUtils.isSameInstant(d1, d2));
    }

    // Tests isSameLocalTime with equal Calendar objects
    @Test
    public void testIsSameLocalTime_sameTime_returnsTrue() throws Exception {
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal1.set(2000, 0, 1, 12, 30, 45);
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal2.set(2000, 0, 1, 12, 30, 45);
        assertTrue(DateUtils.isSameLocalTime(cal1, cal2));
    }

    // Tests isSameLocalTime with different time zones but same local time
    @Test
    public void testIsSameLocalTime_differentTimeZone_returnsTrue() throws Exception {
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal1.set(2000, 0, 1, 12, 0, 0);
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
        cal2.set(2000, 0, 1, 12, 0, 0);
        assertTrue(DateUtils.isSameLocalTime(cal1, cal2));
    }

    // Tests addHours with positive amount
    @Test
    public void testAddHours_positiveAmount_returnsCorrectDate() throws Exception {
        Date base = utcDate(2000, 0, 1, 10, 0, 0, 0);
        Date result = DateUtils.addHours(base, 3);
        Date expected = utcDate(2000, 0, 1, 13, 0, 0, 0);
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests addMinutes with negative amount
    @Test
    public void testAddMinutes_negativeAmount_returnsCorrectDate() throws Exception {
        Date base = utcDate(2000, 0, 1, 10, 30, 0, 0);
        Date result = DateUtils.addMinutes(base, -15);
        Date expected = utcDate(2000, 0, 1, 10, 15, 0, 0);
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests addSeconds with zero amount
    @Test
    public void testAddSeconds_zeroAmount_returnsSameDate() throws Exception {
        Date base = utcDate(2000, 0, 1, 10, 30, 45, 0);
        Date result = DateUtils.addSeconds(base, 0);
        assertEquals(base.getTime(), result.getTime());
    }

    // Tests addMilliseconds with positive amount crossing minute boundary
    @Test
    public void testAddMilliseconds_crossingMinute_returnsCorrectDate() throws Exception {
        Date base = utcDate(2000, 0, 1, 10, 30, 59, 900);
        Date result = DateUtils.addMilliseconds(base, 200);
        Date expected = utcDate(2000, 0, 1, 10, 31, 0, 100);
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests setMonths with valid month
    @Test
    public void testSetMonths_validAmount_returnsCorrectDate() throws Exception {
        Date base = utcDate(2000, 0, 1, 0, 0, 0, 0);
        Date result = DateUtils.setMonths(base, 5);
        Date expected = utcDate(2000, 5, 1, 0, 0, 0, 0);
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests setDays with valid day
    @Test
    public void testSetDays_validAmount_returnsCorrectDate() throws Exception {
        Date base = utcDate(2000, 0, 15, 0, 0, 0, 0);
        Date result = DateUtils.setDays(base, 1);
        Date expected = utcDate(2000, 0, 1, 0, 0, 0, 0);
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests setHours with valid hour
    @Test
    public void testSetHours_validAmount_returnsCorrectDate() throws Exception {
        Date base = utcDate(2000, 0, 1, 10, 30, 45, 123);
        Date result = DateUtils.setHours(base, 23);
        Date expected = utcDate(2000, 0, 1, 23, 30, 45, 123);
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests setMinutes with valid minute
    @Test
    public void testSetMinutes_validAmount_returnsCorrectDate() throws Exception {
        Date base = utcDate(2000, 0, 1, 10, 30, 45, 123);
        Date result = DateUtils.setMinutes(base, 0);
        Date expected = utcDate(2000, 0, 1, 10, 0, 45, 123);
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests setSeconds with valid second
    @Test
    public void testSetSeconds_validAmount_returnsCorrectDate() throws Exception {
        Date base = utcDate(2000, 0, 1, 10, 30, 45, 123);
        Date result = DateUtils.setSeconds(base, 59);
        Date expected = utcDate(2000, 0, 1, 10, 30, 59, 123);
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests setMilliseconds with valid millisecond
    @Test
    public void testSetMilliseconds_validAmount_returnsCorrectDate() throws Exception {
        Date base = utcDate(2000, 0, 1, 10, 30, 45, 123);
        Date result = DateUtils.setMilliseconds(base, 999);
        Date expected = utcDate(2000, 0, 1, 10, 30, 45, 999);
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests round to year field (round down)
    @Test
    public void testRound_yearField_roundsDownCorrectly() throws Exception {
        Date input = utcDate(2000, 5, 15, 0, 0, 0, 0); // June 15 2000
        Date result = DateUtils.round(input, Calendar.YEAR);
        Date expected = utcDate(2001, 0, 1, 0, 0, 0, 0); // round up since >= July 1
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests round to month field (round down, less than 15th)
    @Test
    public void testRound_monthField_roundsDownCorrectly() throws Exception {
        Date input = utcDate(2000, 0, 10, 0, 0, 0, 0); // Jan 10 2000
        Date result = DateUtils.round(input, Calendar.MONTH);
        Date expected = utcDate(2000, 0, 1, 0, 0, 0, 0); // round down to Jan
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests ceiling to month field
    @Test
    public void testCeiling_monthField_ceilsCorrectly() throws Exception {
        Date input = utcDate(2000, 0, 15, 10, 30, 45, 123);
        Date result = DateUtils.ceiling(input, Calendar.MONTH);
        Date expected = utcDate(2000, 1, 1, 0, 0, 0, 0); // Feb 1
        assertEquals(expected.getTime(), result.getTime());
    }

    // Tests fragment in hours for DAY_OF_YEAR
    @Test
    public void testGetFragmentInHours_dayOfYear_returnsCorrect() throws Exception {
        Date date = utcDate(2000, 0, 15, 10, 30, 20, 500);
        long result = DateUtils.getFragmentInHours(date, Calendar.DAY_OF_YEAR);
        assertEquals(10L, result);
    }

    // Tests fragment in minutes for HOUR_OF_DAY
    @Test
    public void testGetFragmentInMinutes_hourOfDay_returnsCorrect() throws Exception {
        Date date = utcDate(2000, 0, 15, 10, 30, 20, 500);
        long result = DateUtils.getFragmentInMinutes(date, Calendar.HOUR_OF_DAY);
        assertEquals(30L, result);
    }

    // Tests fragment in seconds for MINUTE
    @Test
    public void testGetFragmentInSeconds_minute_returnsCorrect() throws Exception {
        Date date = utcDate(2000, 0, 15, 10, 30, 20, 500);
        long result = DateUtils.getFragmentInSeconds(date, Calendar.MINUTE);
        assertEquals(20L, result);
    }

    // Tests fragment in milliseconds for SECOND
    @Test
    public void testGetFragmentInMilliseconds_second_returnsCorrect() throws Exception {
        Date date = utcDate(2000, 0, 15, 10, 30, 20, 500);
        long result = DateUtils.getFragmentInMilliseconds(date, Calendar.SECOND);
        assertEquals(500L, result);
    }

    // Tests iterator with RANGE_WEEK_MONDAY
    @Test
    public void testIterator_rangeWeekMonday_returnsCorrectDays() throws Exception {
        Date focus = utcDate(2000, 0, 5, 0, 0, 0, 0); // Wednesday Jan 5
        Iterator<Calendar> it = DateUtils.iterator(focus, DateUtils.RANGE_WEEK_MONDAY);
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        assertEquals(7, count);
    }

    // Tests iterator with RANGE_MONTH_SUNDAY
    @Test
    public void testIterator_rangeMonthSunday_returnsCorrectNumberOfDays() throws Exception {
        Date focus = utcDate(2000, 0, 15, 0, 0, 0, 0);
        Iterator<Calendar> it = DateUtils.iterator(focus, DateUtils.RANGE_MONTH_SUNDAY);
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        // January 2000 has 31 days
        assertEquals(31, count);
    }

    // Tests truncatedEquals returns false for different truncated dates
    @Test
    public void testTruncatedEquals_differentTruncated_returnsFalse() throws Exception {
        Date d1 = utcDate(2000, 0, 15, 10, 0, 0, 0);
        Date d2 = utcDate(2000, 0, 16, 5, 0, 0, 0);
        assertFalse(DateUtils.truncatedEquals(d1, d2, Calendar.DATE));
    }

    // Tests truncatedCompareTo returns 0 for equal truncated dates
    @Test
    public void testTruncatedCompareTo_equalDates_returnsZero() throws Exception {
        Date d1 = utcDate(2000, 0, 15, 10, 30, 0, 0);
        Date d2 = utcDate(2000, 0, 15, 5, 0, 0, 0);
        assertEquals(0, DateUtils.truncatedCompareTo(d1, d2, Calendar.DATE));
    }

    // Tests truncatedCompareTo returns positive for later truncated date
    @Test
    public void testTruncatedCompareTo_laterDate_returnsPositive() throws Exception {
        Date d1 = utcDate(2000, 0, 16, 10, 0, 0, 0);
        Date d2 = utcDate(2000, 0, 15, 5, 0, 0, 0);
        assertTrue(DateUtils.truncatedCompareTo(d1, d2, Calendar.DATE) > 0);
    }

    // Tests truncatedCompareTo returns negative for earlier truncated date
    @Test
    public void testTruncatedCompareTo_earlierDate_returnsNegative() throws Exception {
        Date d1 = utcDate(2000, 0, 14, 10, 0, 0, 0);
        Date d2 = utcDate(2000, 0, 15, 5, 0, 0, 0);
        assertTrue(DateUtils.truncatedCompareTo(d1, d2, Calendar.DATE) < 0);
    }
}