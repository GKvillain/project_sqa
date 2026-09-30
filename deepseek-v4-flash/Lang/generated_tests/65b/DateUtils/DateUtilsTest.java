package org.apache.commons.lang.time;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.Calendar;
import java.util.Date;
import java.text.ParseException;

public class DateUtilsTest {

    // Helper to create a Calendar instance in UTC time zone
    private Calendar createCalendar(int year, int month, int date, int hour, int min, int sec, int millis) {
        Calendar cal = Calendar.getInstance(DateUtils.UTC_TIME_ZONE);
        cal.set(year, month, date, hour, min, sec);
        cal.set(Calendar.MILLISECOND, millis);
        return cal;
    }

    // Helper to create a Date
    private Date createDate(int year, int month, int date, int hour, int min, int sec, int millis) {
        return createCalendar(year, month, date, hour, min, sec, millis).getTime();
    }

    // Test truncate to YEAR
    @Test
    public void testTruncateYear_ValidDate_TruncatedToYear() {
        Date input = createDate(2002, Calendar.MARCH, 28, 13, 45, 1, 231);
        Date result = DateUtils.truncate(input, Calendar.YEAR);
        Calendar expected = createCalendar(2002, Calendar.JANUARY, 1, 0, 0, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test truncate to MONTH
    @Test
    public void testTruncateMonth_ValidDate_TruncatedToMonth() {
        Date input = createDate(2002, Calendar.MARCH, 28, 13, 45, 1, 231);
        Date result = DateUtils.truncate(input, Calendar.MONTH);
        Calendar expected = createCalendar(2002, Calendar.MARCH, 1, 0, 0, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test truncate to DATE
    @Test
    public void testTruncateDate_ValidDate_TruncatedToDay() {
        Date input = createDate(2002, Calendar.MARCH, 28, 13, 45, 1, 231);
        Date result = DateUtils.truncate(input, Calendar.DATE);
        Calendar expected = createCalendar(2002, Calendar.MARCH, 28, 0, 0, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test truncate to HOUR_OF_DAY
    @Test
    public void testTruncateHour_ValidDate_TruncatedToHour() {
        Date input = createDate(2002, Calendar.MARCH, 28, 13, 45, 1, 231);
        Date result = DateUtils.truncate(input, Calendar.HOUR_OF_DAY);
        Calendar expected = createCalendar(2002, Calendar.MARCH, 28, 13, 0, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test truncate to MINUTE
    @Test
    public void testTruncateMinute_ValidDate_TruncatedToMinute() {
        Date input = createDate(2002, Calendar.MARCH, 28, 13, 45, 1, 231);
        Date result = DateUtils.truncate(input, Calendar.MINUTE);
        Calendar expected = createCalendar(2002, Calendar.MARCH, 28, 13, 45, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test truncate to SECOND
    @Test
    public void testTruncateSecond_ValidDate_TruncatedToSecond() {
        Date input = createDate(2002, Calendar.MARCH, 28, 13, 45, 1, 231);
        Date result = DateUtils.truncate(input, Calendar.SECOND);
        Calendar expected = createCalendar(2002, Calendar.MARCH, 28, 13, 45, 1, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test round to HOUR_OF_DAY: minute < 30 -> round down
    @Test
    public void testRoundHour_MinuteLessThan30_RoundsDown() {
        Date input = createDate(2002, Calendar.MARCH, 28, 13, 29, 59, 999);
        Date result = DateUtils.round(input, Calendar.HOUR_OF_DAY);
        Calendar expected = createCalendar(2002, Calendar.MARCH, 28, 13, 0, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test round to HOUR_OF_DAY: minute >= 30 -> round up
    @Test
    public void testRoundHour_MinuteEqualTo30_RoundsUp() {
        Date input = createDate(2002, Calendar.MARCH, 28, 13, 30, 0, 0);
        Date result = DateUtils.round(input, Calendar.HOUR_OF_DAY);
        Calendar expected = createCalendar(2002, Calendar.MARCH, 28, 14, 0, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test round to MINUTE: second < 30 -> round down
    @Test
    public void testRoundMinute_SecondLessThan30_RoundsDown() {
        Date input = createDate(2002, Calendar.MARCH, 28, 13, 45, 29, 999);
        Date result = DateUtils.round(input, Calendar.MINUTE);
        Calendar expected = createCalendar(2002, Calendar.MARCH, 28, 13, 45, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test round to MINUTE: second >= 30 -> round up
    @Test
    public void testRoundMinute_SecondEqualTo30_RoundsUp() {
        Date input = createDate(2002, Calendar.MARCH, 28, 13, 45, 30, 0);
        Date result = DateUtils.round(input, Calendar.MINUTE);
        Calendar expected = createCalendar(2002, Calendar.MARCH, 28, 13, 46, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test round to SECOND: millisecond < 500 -> round down
    @Test
    public void testRoundSecond_MillisLessThan500_RoundsDown() {
        Date input = createDate(2002, Calendar.MARCH, 28, 13, 45, 1, 499);
        Date result = DateUtils.round(input, Calendar.SECOND);
        Calendar expected = createCalendar(2002, Calendar.MARCH, 28, 13, 45, 1, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test round to SECOND: millisecond >= 500 -> round up
    @Test
    public void testRoundSecond_MillisEqualTo500_RoundsUp() {
        Date input = createDate(2002, Calendar.MARCH, 28, 13, 45, 1, 500);
        Date result = DateUtils.round(input, Calendar.SECOND);
        Calendar expected = createCalendar(2002, Calendar.MARCH, 28, 13, 45, 2, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test round to DATE: time before noon (hour < 12) -> round down
    @Test
    public void testRoundDate_TimeBeforeNoon_RoundsDown() {
        Date input = createDate(2002, Calendar.MARCH, 28, 6, 30, 0, 0);
        Date result = DateUtils.round(input, Calendar.DATE);
        Calendar expected = createCalendar(2002, Calendar.MARCH, 28, 0, 0, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test round to DATE: time at noon (hour = 12) -> round up to next day
    @Test
    public void testRoundDate_TimeAtNoon_RoundsUp() {
        Date input = createDate(2002, Calendar.MARCH, 28, 12, 0, 0, 0);
        Date result = DateUtils.round(input, Calendar.DATE);
        Calendar expected = createCalendar(2002, Calendar.MARCH, 29, 0, 0, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test isSameDay with Date
    @Test
    public void testIsSameDay_SameDay_ReturnsTrue() {
        Date d1 = createDate(2002, Calendar.MARCH, 28, 10, 0, 0, 0);
        Date d2 = createDate(2002, Calendar.MARCH, 28, 23, 59, 59, 999);
        assertTrue(DateUtils.isSameDay(d1, d2));
    }

    @Test
    public void testIsSameDay_DifferentDay_ReturnsFalse() {
        Date d1 = createDate(2002, Calendar.MARCH, 28, 0, 0, 0, 0);
        Date d2 = createDate(2002, Calendar.MARCH, 29, 0, 0, 0, 0);
        assertFalse(DateUtils.isSameDay(d1, d2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDay_NullDate_ThrowsIllegalArgumentException() {
        DateUtils.isSameDay((Date) null, new Date());
    }

    // Test parseDate with valid pattern
    @Test
    public void testParseDate_ValidPattern_ReturnsDate() throws ParseException {
        String dateStr = "2002-03-28";
        String[] patterns = {"yyyy-MM-dd"};
        Date result = DateUtils.parseDate(dateStr, patterns);
        Calendar expected = createCalendar(2002, Calendar.MARCH, 28, 0, 0, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseDate_NullDate_ThrowsIllegalArgumentException() throws ParseException {
        DateUtils.parseDate(null, new String[]{"yyyy-MM-dd"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseDate_NullPatterns_ThrowsIllegalArgumentException() throws ParseException {
        DateUtils.parseDate("2002-03-28", null);
    }

    // Test addDays with zero
    @Test
    public void testAddDays_Zero_ReturnsSameDate() {
        Date date = createDate(2002, Calendar.MARCH, 28, 0, 0, 0, 0);
        Date result = DateUtils.addDays(date, 0);
        assertEquals(date, result);
    }

    // Test addDays with positive amount
    @Test
    public void testAddDays_Positive_AddsCorrectDays() {
        Date date = createDate(2002, Calendar.MARCH, 28, 0, 0, 0, 0);
        Date result = DateUtils.addDays(date, 5);
        Calendar expected = createCalendar(2002, Calendar.APRIL, 2, 0, 0, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test addMonths crossing year boundary
    @Test
    public void testAddMonths_CrossYear_AddsCorrectMonths() {
        Date date = createDate(2002, Calendar.NOVEMBER, 30, 0, 0, 0, 0);
        Date result = DateUtils.addMonths(date, 3);
        Calendar expected = createCalendar(2003, Calendar.FEBRUARY, 28, 0, 0, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test addYears
    @Test
    public void testAddYears_ValidAmount_AddsCorrectYears() {
        Date date = createDate(2002, Calendar.MARCH, 28, 0, 0, 0, 0);
        Date result = DateUtils.addYears(date, 10);
        Calendar expected = createCalendar(2012, Calendar.MARCH, 28, 0, 0, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // ============== START OF NEW TESTS ==============

    // Test round to MONTH: day < 15 -> round down
    @Test
    public void testRoundMonth_DayLessThan15_RoundsDown() {
        Date input = createDate(2002, Calendar.MARCH, 14, 13, 45, 1, 231);
        Date result = DateUtils.round(input, Calendar.MONTH);
        Calendar expected = createCalendar(2002, Calendar.MARCH, 1, 0, 0, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test round to MONTH: day >= 15 -> round up
    @Test
    public void testRoundMonth_DayEqualTo15_RoundsUp() {
        Date input = createDate(2002, Calendar.MARCH, 15, 0, 0, 0, 0);
        Date result = DateUtils.round(input, Calendar.MONTH);
        Calendar expected = createCalendar(2002, Calendar.APRIL, 1, 0, 0, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test round to YEAR: month < July (month < 7) -> round down
    @Test
    public void testRoundYear_MonthLessThanJuly_RoundsDown() {
        Date input = createDate(2002, Calendar.JUNE, 28, 13, 45, 1, 231);
        Date result = DateUtils.round(input, Calendar.YEAR);
        Calendar expected = createCalendar(2002, Calendar.JANUARY, 1, 0, 0, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test round to YEAR: month >= July (month = 7) -> round up
    @Test
    public void testRoundYear_MonthEqualToJuly_RoundsUp() {
        Date input = createDate(2002, Calendar.JULY, 1, 0, 0, 0, 0);
        Date result = DateUtils.round(input, Calendar.YEAR);
        Calendar expected = createCalendar(2003, Calendar.JANUARY, 1, 0, 0, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test ceiling to YEAR
    @Test
    public void testCeilYear_ValidDate_CeilToNextYear() {
        Date input = createDate(2002, Calendar.MARCH, 28, 13, 45, 1, 231);
        Date result = DateUtils.ceiling(input, Calendar.YEAR);
        Calendar expected = createCalendar(2003, Calendar.JANUARY, 1, 0, 0, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test ceiling to MONTH
    @Test
    public void testCeilMonth_ValidDate_CeilToNextMonth() {
        Date input = createDate(2002, Calendar.MARCH, 28, 13, 45, 1, 231);
        Date result = DateUtils.ceiling(input, Calendar.MONTH);
        Calendar expected = createCalendar(2002, Calendar.APRIL, 1, 0, 0, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test ceiling to DATE (day)
    @Test
    public void testCeilDate_ValidDate_CeilToNextDay() {
        Date input = createDate(2002, Calendar.MARCH, 28, 13, 45, 1, 231);
        Date result = DateUtils.ceiling(input, Calendar.DATE);
        Calendar expected = createCalendar(2002, Calendar.MARCH, 29, 0, 0, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test ceiling to HOUR_OF_DAY
    @Test
    public void testCeilHour_ValidDate_CeilToNextHour() {
        Date input = createDate(2002, Calendar.MARCH, 28, 13, 45, 1, 231);
        Date result = DateUtils.ceiling(input, Calendar.HOUR_OF_DAY);
        Calendar expected = createCalendar(2002, Calendar.MARCH, 28, 14, 0, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test addHours
    @Test
    public void testAddHours_Positive_AddsCorrectHours() {
        Date date = createDate(2002, Calendar.MARCH, 28, 10, 0, 0, 0);
        Date result = DateUtils.addHours(date, 5);
        Calendar expected = createCalendar(2002, Calendar.MARCH, 28, 15, 0, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test addMinutes
    @Test
    public void testAddMinutes_Positive_AddsCorrectMinutes() {
        Date date = createDate(2002, Calendar.MARCH, 28, 10, 30, 0, 0);
        Date result = DateUtils.addMinutes(date, 15);
        Calendar expected = createCalendar(2002, Calendar.MARCH, 28, 10, 45, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test addSeconds
    @Test
    public void testAddSeconds_Positive_AddsCorrectSeconds() {
        Date date = createDate(2002, Calendar.MARCH, 28, 10, 30, 0, 0);
        Date result = DateUtils.addSeconds(date, 45);
        Calendar expected = createCalendar(2002, Calendar.MARCH, 28, 10, 30, 45, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test addMilliseconds
    @Test
    public void testAddMilliseconds_Positive_AddsCorrectMillis() {
        Date date = createDate(2002, Calendar.MARCH, 28, 10, 30, 0, 0);
        Date result = DateUtils.addMilliseconds(date, 500);
        Calendar expected = createCalendar(2002, Calendar.MARCH, 28, 10, 30, 0, 500);
        assertEquals(expected.getTime(), result);
    }

    // Test addWeeks
    @Test
    public void testAddWeeks_Positive_AddsCorrectWeeks() {
        Date date = createDate(2002, Calendar.MARCH, 28, 0, 0, 0, 0);
        Date result = DateUtils.addWeeks(date, 2);
        Calendar expected = createCalendar(2002, Calendar.APRIL, 11, 0, 0, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    // Test isSameInstant with Date
    @Test
    public void testIsSameInstant_SameInstant_ReturnsTrue() {
        Date d1 = createDate(2002, Calendar.MARCH, 28, 10, 0, 0, 0);
        Date d2 = createDate(2002, Calendar.MARCH, 28, 10, 0, 0, 0);
        assertTrue(DateUtils.isSameInstant(d1, d2));
    }

    @Test
    public void testIsSameInstant_DifferentInstant_ReturnsFalse() {
        Date d1 = createDate(2002, Calendar.MARCH, 28, 10, 0, 0, 0);
        Date d2 = createDate(2002, Calendar.MARCH, 28, 11, 0, 0, 0);
        assertFalse(DateUtils.isSameInstant(d1, d2));
    }

    // Test isSameDay with Calendar
    @Test
    public void testIsSameDayCalendar_SameDay_ReturnsTrue() {
        Calendar cal1 = createCalendar(2002, Calendar.MARCH, 28, 10, 0, 0, 0);
        Calendar cal2 = createCalendar(2002, Calendar.MARCH, 28, 23, 59, 59, 999);
        assertTrue(DateUtils.isSameDay(cal1, cal2));
    }

    @Test
    public void testIsSameDayCalendar_DifferentDay_ReturnsFalse() {
        Calendar cal1 = createCalendar(2002, Calendar.MARCH, 28, 0, 0, 0, 0);
        Calendar cal2 = createCalendar(2002, Calendar.MARCH, 29, 0, 0, 0, 0);
        assertFalse(DateUtils.isSameDay(cal1, cal2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDayCalendar_NullFirst_ThrowsIllegalArgumentException() {
        DateUtils.isSameDay((Calendar) null, Calendar.getInstance());
    }

    // Test parseDateStrictly
    @Test
    public void testParseDateStrictly_ValidPattern_ReturnsDate() throws ParseException {
        String dateStr = "2002-03-28";
        String[] patterns = {"yyyy-MM-dd"};
        Date result = DateUtils.parseDateStrictly(dateStr, patterns);
        Calendar expected = createCalendar(2002, Calendar.MARCH, 28, 0, 0, 0, 0);
        assertEquals(expected.getTime(), result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseDateStrictly_NullDate_ThrowsIllegalArgumentException() throws ParseException {
        DateUtils.parseDateStrictly(null, new String[]{"yyyy-MM-dd"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseDateStrictly_NullPatterns_ThrowsIllegalArgumentException() throws ParseException {
        DateUtils.parseDateStrictly("2002-03-28", null);
    }

    // Test truncate with Calendar
    @Test
    public void testTruncateCalendar_Year_TruncatedToYear() {
        Calendar input = createCalendar(2002, Calendar.MARCH, 28, 13, 45, 1, 231);
        Calendar result = DateUtils.truncate(input, Calendar.YEAR);
        Calendar expected = createCalendar(2002, Calendar.JANUARY, 1, 0, 0, 0, 0);
        assertEquals(expected, result);
    }

    // Test ceiling with Calendar
    @Test
    public void testCeilCalendar_Year_CeilToNextYear() {
        Calendar input = createCalendar(2002, Calendar.MARCH, 28, 13, 45, 1, 231);
        Calendar result = DateUtils.ceiling(input, Calendar.YEAR);
        Calendar expected = createCalendar(2003, Calendar.JANUARY, 1, 0, 0, 0, 0);
        assertEquals(expected, result);
    }

    // Test round with null (should throw exception)
    @Test(expected = IllegalArgumentException.class)
    public void testRound_NullDate_ThrowsIllegalArgumentException() {
        DateUtils.round((Date) null, Calendar.YEAR);
    }

    // Test truncate with null
    @Test(expected = IllegalArgumentException.class)
    public void testTruncate_NullDate_ThrowsIllegalArgumentException() {
        DateUtils.truncate((Date) null, Calendar.YEAR);
    }

    // Test ceiling with null
    @Test(expected = IllegalArgumentException.class)
    public void testCeil_NullDate_ThrowsIllegalArgumentException() {
        DateUtils.ceiling((Date) null, Calendar.YEAR);
    }

    // Test addDays with null
    @Test(expected = IllegalArgumentException.class)
    public void testAddDays_NullDate_ThrowsIllegalArgumentException() {
        DateUtils.addDays(null, 1);
    }

}