package org.apache.commons.lang3.time;

import org.junit.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DateUtilsTest {

    // Tests defect Lang-21: isSameLocalTime with same hour in 12-hour format but different AM/PM
    @Test
    public void testIsSameLocalTime_differentAmPmSameHour_returnsFalse() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2020, Calendar.JULY, 15, 8, 30, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.set(2020, Calendar.JULY, 15, 20, 30, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        assertFalse(DateUtils.isSameLocalTime(cal1, cal2));
    }

    // Tests isSameLocalTime with identical local time and class
    @Test
    public void testIsSameLocalTime_sameLocalTime_returnsTrue() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2020, Calendar.JULY, 15, 14, 30, 0);
        cal1.set(Calendar.MILLISECOND, 500);

        Calendar cal2 = (Calendar) cal1.clone();

        assertTrue(DateUtils.isSameLocalTime(cal1, cal2));
    }

    // Tests isSameLocalTime with null input
    @Test(expected = IllegalArgumentException.class)
    public void testIsSameLocalTime_nullCalendar_throwsException() {
        DateUtils.isSameLocalTime(null, Calendar.getInstance());
    }

    // Tests isSameDay for Date and Calendar
    @Test
    public void testIsSameDay_sameAndDifferentDates_returnsExpectedResults() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2020, Calendar.MAY, 10, 10, 0, 0);
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2020, Calendar.MAY, 10, 22, 0, 0);
        Calendar cal3 = Calendar.getInstance();
        cal3.set(2020, Calendar.MAY, 11, 10, 0, 0);

        assertTrue(DateUtils.isSameDay(cal1.getTime(), cal2.getTime()));
        assertTrue(DateUtils.isSameDay(cal1, cal2));
        assertFalse(DateUtils.isSameDay(cal1.getTime(), cal3.getTime()));
        assertFalse(DateUtils.isSameDay(cal1, cal3));
    }

    // Tests isSameDay with null parameter
    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDay_nullInput_throwsException() {
        DateUtils.isSameDay((Date) null, new Date());
    }

    // Tests isSameInstant for Date and Calendar
    @Test
    public void testIsSameInstant_matchingAndNonMatchingInstants_returnsCorrectly() {
        Date date1 = new Date(1000000L);
        Date date2 = new Date(1000000L);
        Date date3 = new Date(2000000L);

        assertTrue(DateUtils.isSameInstant(date1, date2));
        assertFalse(DateUtils.isSameInstant(date1, date3));

        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("GMT+2"));
        cal2.setTime(date2);
        Calendar cal3 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal3.setTime(date3);

        assertTrue(DateUtils.isSameInstant(cal1, cal2));
        assertFalse(DateUtils.isSameInstant(cal1, cal3));
    }

    // Tests parseDate and parseDateStrictly with various patterns including ZZ
    @Test
    public void testParseDate_validPatterns_returnsParsedDate() throws ParseException {
        String[] patterns = new String[]{"yyyy-MM-dd", "yyyy/MM/dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ssZZ"};
        Date parsed = DateUtils.parseDate("2020-05-15", patterns);
        assertNotNull(parsed);

        Date parsedWithTz = DateUtils.parseDate("2020-05-15T12:00:00+02:00", patterns);
        assertNotNull(parsedWithTz);

        Date parsedStrict = DateUtils.parseDateStrictly("2020/05/15 12:30:00", patterns);
        assertNotNull(parsedStrict);
    }

    // Tests parseDateStrictly rejecting invalid lenient dates
    @Test(expected = ParseException.class)
    public void testParseDateStrictly_invalidDate_throwsException() throws ParseException {
        DateUtils.parseDateStrictly("2020-02-31", "yyyy-MM-dd");
    }

    // Tests add methods for date units
    @Test
    public void testAdd_dateFields_returnsAdjustedDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.JANUARY, 10, 10, 10, 10);
        cal.set(Calendar.MILLISECOND, 100);
        Date base = cal.getTime();

        Date addedYears = DateUtils.addYears(base, 1);
        Date addedMonths = DateUtils.addMonths(base, 2);
        Date addedWeeks = DateUtils.addWeeks(base, 1);
        Date addedDays = DateUtils.addDays(base, 5);
        Date addedHours = DateUtils.addHours(base, 3);
        Date addedMinutes = DateUtils.addMinutes(base, 20);
        Date addedSeconds = DateUtils.addSeconds(base, 15);
        Date addedMillis = DateUtils.addMilliseconds(base, 50);

        Calendar res = Calendar.getInstance();
        res.setTime(addedYears);
        assertEquals(2021, res.get(Calendar.YEAR));

        res.setTime(addedMonths);
        assertEquals(Calendar.MARCH, res.get(Calendar.MONTH));

        res.setTime(addedWeeks);
        assertEquals(17, res.get(Calendar.DAY_OF_MONTH));

        res.setTime(addedDays);
        assertEquals(15, res.get(Calendar.DAY_OF_MONTH));

        res.setTime(addedHours);
        assertEquals(13, res.get(Calendar.HOUR_OF_DAY));

        res.setTime(addedMinutes);
        assertEquals(30, res.get(Calendar.MINUTE));

        res.setTime(addedSeconds);
        assertEquals(25, res.get(Calendar.SECOND));

        res.setTime(addedMillis);
        assertEquals(150, res.get(Calendar.MILLISECOND));
    }

    // Tests set methods for date fields
    @Test
    public void testSet_dateFields_returnsModifiedDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date base = cal.getTime();

        Date setYear = DateUtils.setYears(base, 2022);
        Date setMonth = DateUtils.setMonths(base, Calendar.DECEMBER);
        Date setDay = DateUtils.setDays(base, 25);
        Date setHour = DateUtils.setHours(base, 15);
        Date setMinute = DateUtils.setMinutes(base, 45);
        Date setSecond = DateUtils.setSeconds(base, 30);
        Date setMillis = DateUtils.setMilliseconds(base, 500);

        Calendar res = Calendar.getInstance();
        res.setTime(setYear);
        assertEquals(2022, res.get(Calendar.YEAR));

        res.setTime(setMonth);
        assertEquals(Calendar.DECEMBER, res.get(Calendar.MONTH));

        res.setTime(setDay);
        assertEquals(25, res.get(Calendar.DAY_OF_MONTH));

        res.setTime(setHour);
        assertEquals(15, res.get(Calendar.HOUR_OF_DAY));

        res.setTime(setMinute);
        assertEquals(45, res.get(Calendar.MINUTE));

        res.setTime(setSecond);
        assertEquals(30, res.get(Calendar.SECOND));

        res.setTime(setMillis);
        assertEquals(500, res.get(Calendar.MILLISECOND));
    }

    // Tests toCalendar conversion
    @Test
    public void testToCalendar_validDate_returnsCalendar() {
        Date date = new Date(123456789L);
        Calendar cal = DateUtils.toCalendar(date);
        assertNotNull(cal);
        assertEquals(date.getTime(), cal.getTime().getTime());
    }

    // Tests truncate, round, and ceiling for Date, Calendar, and Object
    @Test
    public void testTruncateRoundCeiling_variousFields_correctAdjustments() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.JUNE, 15, 12, 45, 45);
        cal.set(Calendar.MILLISECOND, 800);

        Calendar truncatedCal = DateUtils.truncate(cal, Calendar.HOUR);
        assertEquals(0, truncatedCal.get(Calendar.MINUTE));
        assertEquals(0, truncatedCal.get(Calendar.SECOND));
        assertEquals(0, truncatedCal.get(Calendar.MILLISECOND));

        Date roundedDate = DateUtils.round(cal.getTime(), Calendar.HOUR);
        Calendar roundCal = Calendar.getInstance();
        roundCal.setTime(roundedDate);
        assertEquals(13, roundCal.get(Calendar.HOUR_OF_DAY));

        Date ceiledDate = DateUtils.ceiling(cal.getTime(), Calendar.MONTH);
        Calendar ceilCal = Calendar.getInstance();
        ceilCal.setTime(ceiledDate);
        assertEquals(Calendar.JULY, ceilCal.get(Calendar.MONTH));
        assertEquals(1, ceilCal.get(Calendar.DAY_OF_MONTH));

        // Test Object overload
        Object objDate = cal.getTime();
        Date objTrunc = DateUtils.truncate(objDate, Calendar.DAY_OF_MONTH);
        Date objRound = DateUtils.round(objDate, Calendar.DAY_OF_MONTH);
        Date objCeil = DateUtils.ceiling(objDate, Calendar.DAY_OF_MONTH);
        assertNotNull(objTrunc);
        assertNotNull(objRound);
        assertNotNull(objCeil);

        Object objCal = cal;
        Date calTrunc = DateUtils.truncate(objCal, Calendar.DAY_OF_MONTH);
        Date calRound = DateUtils.round(objCal, Calendar.DAY_OF_MONTH);
        Date calCeil = DateUtils.ceiling(objCal, Calendar.DAY_OF_MONTH);
        assertNotNull(calTrunc);
        assertNotNull(calRound);
        assertNotNull(calCeil);
    }

    // Tests modify special fields: SEMI_MONTH and AM_PM
    @Test
    public void testTruncateAndRound_semiMonthAndAmPm_correctResults() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.MARCH, 20, 16, 0, 0);

        Calendar semiMonthTrunc = DateUtils.truncate(cal, DateUtils.SEMI_MONTH);
        assertEquals(16, semiMonthTrunc.get(Calendar.DAY_OF_MONTH));

        cal.set(Calendar.DAY_OF_MONTH, 5);
        Calendar semiMonthTrunc2 = DateUtils.truncate(cal, DateUtils.SEMI_MONTH);
        assertEquals(1, semiMonthTrunc2.get(Calendar.DAY_OF_MONTH));

        Calendar amPmTrunc = DateUtils.truncate(cal, Calendar.AM_PM);
        assertEquals(12, amPmTrunc.get(Calendar.HOUR_OF_DAY));
    }

    // Tests DateIterator with ranges and next/hasNext/remove
    @Test
    public void testIterator_rangeWeekAndMonth_iteratesCorrectly() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.JULY, 15);

        Iterator<Calendar> itWeek = DateUtils.iterator(cal, DateUtils.RANGE_WEEK_SUNDAY);
        assertTrue(itWeek.hasNext());
        int count = 0;
        while (itWeek.hasNext()) {
            Calendar c = itWeek.next();
            assertNotNull(c);
            count++;
        }
        assertEquals(7, count);

        Iterator<Calendar> itMonth = DateUtils.iterator(cal.getTime(), DateUtils.RANGE_MONTH_MONDAY);
        assertTrue(itMonth.hasNext());

        Iterator<?> itObj = DateUtils.iterator((Object) cal, DateUtils.RANGE_WEEK_CENTER);
        assertTrue(itObj.hasNext());
    }

    // Tests DateIterator remove throwing UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testDateIterator_remove_throwsException() {
        Calendar cal = Calendar.getInstance();
        Iterator<Calendar> it = DateUtils.iterator(cal, DateUtils.RANGE_WEEK_SUNDAY);
        it.remove();
    }

    // Tests DateIterator next when exhausted throwing NoSuchElementException
    @Test(expected = NoSuchElementException.class)
    public void testDateIterator_exhaustedNext_throwsException() {
        Calendar cal = Calendar.getInstance();
        Iterator<Calendar> it = DateUtils.iterator(cal, DateUtils.RANGE_WEEK_SUNDAY);
        while (it.hasNext()) {
            it.next();
        }
        it.next();
    }

    // Tests fragment methods for Date and Calendar
    @Test
    public void testGetFragment_variousUnits_returnsExpectedValues() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.JANUARY, 1, 5, 30, 15);
        cal.set(Calendar.MILLISECOND, 250);
        Date date = cal.getTime();

        assertEquals(250L, DateUtils.getFragmentInMilliseconds(date, Calendar.SECOND));
        assertEquals(250L, DateUtils.getFragmentInMilliseconds(cal, Calendar.SECOND));
        assertEquals(15L, DateUtils.getFragmentInSeconds(date, Calendar.MINUTE));
        assertEquals(15L, DateUtils.getFragmentInSeconds(cal, Calendar.MINUTE));
        assertEquals(30L, DateUtils.getFragmentInMinutes(date, Calendar.HOUR_OF_DAY));
        assertEquals(30L, DateUtils.getFragmentInMinutes(cal, Calendar.HOUR_OF_DAY));
        assertEquals(5L, DateUtils.getFragmentInHours(date, Calendar.DAY_OF_YEAR));
        assertEquals(5L, DateUtils.getFragmentInHours(cal, Calendar.DAY_OF_YEAR));
        assertEquals(1L, DateUtils.getFragmentInDays(date, Calendar.MONTH));
        assertEquals(1L, DateUtils.getFragmentInDays(cal, Calendar.MONTH));
    }

    // Tests truncatedEquals and truncatedCompareTo for Date and Calendar
    @Test
    public void testTruncatedEqualsAndCompareTo_sameAndDifferent_returnsExpected() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2020, Calendar.APRIL, 10, 10, 15, 20);
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2020, Calendar.APRIL, 10, 10, 45, 50);

        assertTrue(DateUtils.truncatedEquals(cal1, cal2, Calendar.HOUR_OF_DAY));
        assertTrue(DateUtils.truncatedEquals(cal1.getTime(), cal2.getTime(), Calendar.HOUR_OF_DAY));
        assertEquals(0, DateUtils.truncatedCompareTo(cal1, cal2, Calendar.HOUR_OF_DAY));
        assertEquals(0, DateUtils.truncatedCompareTo(cal1.getTime(), cal2.getTime(), Calendar.HOUR_OF_DAY));

        assertFalse(DateUtils.truncatedEquals(cal1, cal2, Calendar.MINUTE));
        assertFalse(DateUtils.truncatedEquals(cal1.getTime(), cal2.getTime(), Calendar.MINUTE));
        assertTrue(DateUtils.truncatedCompareTo(cal1, cal2, Calendar.MINUTE) < 0);
        assertTrue(DateUtils.truncatedCompareTo(cal1.getTime(), cal2.getTime(), Calendar.MINUTE) < 0);
    }

    // Tests constructor instantiation
    @Test
    public void testConstructor_instantiation_createsInstance() {
        DateUtils utils = new DateUtils();
        assertNotNull(utils);
    }
}