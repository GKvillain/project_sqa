package org.apache.commons.lang3.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import org.junit.Test;

public class DateUtilsTest {

    // Tests isSameLocalTime with same hour in AM and PM to catch 12h vs 24h defect (Lang-21)
    @Test
    public void testIsSameLocalTime_sameHourDifferentAmPm_returnsFalse() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.set(2004, Calendar.JUNE, 9, 9, 30, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        cal2.set(2004, Calendar.JUNE, 9, 21, 30, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        assertFalse(DateUtils.isSameLocalTime(cal1, cal2));
    }

    // Tests isSameLocalTime when both calendars represent identical local time
    @Test
    public void testIsSameLocalTime_identicalLocalTime_returnsTrue() {
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal1.set(2004, Calendar.JUNE, 9, 10, 20, 30);
        cal1.set(Calendar.MILLISECOND, 100);
        cal2.set(2004, Calendar.JUNE, 9, 10, 20, 30);
        cal2.set(Calendar.MILLISECOND, 100);

        assertTrue(DateUtils.isSameLocalTime(cal1, cal2));
    }

    // Tests isSameLocalTime with null calendar throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testIsSameLocalTime_nullCalendar_throwsException() {
        DateUtils.isSameLocalTime(null, Calendar.getInstance());
    }

    // Tests isSameDay for Date objects
    @Test
    public void testIsSameDay_sameDayDifferentTime_returnsTrue() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2004, Calendar.JUNE, 9, 10, 20, 30);
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2004, Calendar.JUNE, 9, 18, 45, 0);

        assertTrue(DateUtils.isSameDay(cal1.getTime(), cal2.getTime()));
    }

    // Tests isSameDay with null date throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDay_nullDate_throwsException() {
        DateUtils.isSameDay((Date) null, new Date());
    }

    // Tests isSameInstant for Date objects
    @Test
    public void testIsSameInstant_sameInstant_returnsTrue() {
        Date date1 = new Date(1000000L);
        Date date2 = new Date(1000000L);
        Date date3 = new Date(2000000L);

        assertTrue(DateUtils.isSameInstant(date1, date2));
        assertFalse(DateUtils.isSameInstant(date1, date3));
    }

    // Tests parseDate and parseDateStrictly with various patterns
    @Test
    public void testParseDate_validPattern_returnsParsedDate() throws ParseException {
        String[] parsers = new String[] {"yyyy-MM-dd", "yyyy/MM/dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ssZZ"};
        Date parsed1 = DateUtils.parseDate("2020-05-15", parsers);
        assertNotNull(parsed1);

        Date parsed2 = DateUtils.parseDateStrictly("2020-05-15T10:30:00+02:00", parsers);
        assertNotNull(parsed2);
    }

    // Tests parseDate throwing ParseException when no pattern matches
    @Test(expected = ParseException.class)
    public void testParseDate_unmatchedPattern_throwsParseException() throws ParseException {
        DateUtils.parseDate("2020-05-15", "yyyy/MM/dd");
    }

    // Tests add methods (years, months, weeks, days, hours, minutes, seconds, milliseconds)
    @Test
    public void testAddMethods_normalValues_returnsCorrectlyAddedDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date base = cal.getTime();

        Date addedYears = DateUtils.addYears(base, 1);
        Date addedMonths = DateUtils.addMonths(base, 2);
        Date addedWeeks = DateUtils.addWeeks(base, 1);
        Date addedDays = DateUtils.addDays(base, 5);
        Date addedHours = DateUtils.addHours(base, 3);
        Date addedMinutes = DateUtils.addMinutes(base, 15);
        Date addedSeconds = DateUtils.addSeconds(base, 30);
        Date addedMillis = DateUtils.addMilliseconds(base, 500);

        assertEquals(2021, DateUtils.toCalendar(addedYears).get(Calendar.YEAR));
        assertEquals(Calendar.MARCH, DateUtils.toCalendar(addedMonths).get(Calendar.MONTH));
        assertEquals(8, DateUtils.toCalendar(addedWeeks).get(Calendar.DAY_OF_MONTH));
        assertEquals(6, DateUtils.toCalendar(addedDays).get(Calendar.DAY_OF_MONTH));
        assertEquals(3, DateUtils.toCalendar(addedHours).get(Calendar.HOUR_OF_DAY));
        assertEquals(15, DateUtils.toCalendar(addedMinutes).get(Calendar.MINUTE));
        assertEquals(30, DateUtils.toCalendar(addedSeconds).get(Calendar.SECOND));
        assertEquals(500, DateUtils.toCalendar(addedMillis).get(Calendar.MILLISECOND));
    }

    // Tests set methods (years, months, days, hours, minutes, seconds, milliseconds)
    @Test
    public void testSetMethods_normalValues_returnsCorrectlySetDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date base = cal.getTime();

        Date setYears = DateUtils.setYears(base, 2022);
        Date setMonths = DateUtils.setMonths(base, Calendar.DECEMBER);
        Date setDays = DateUtils.setDays(base, 25);
        Date setHours = DateUtils.setHours(base, 14);
        Date setMinutes = DateUtils.setMinutes(base, 45);
        Date setSeconds = DateUtils.setSeconds(base, 50);
        Date setMillis = DateUtils.setMilliseconds(base, 750);

        assertEquals(2022, DateUtils.toCalendar(setYears).get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, DateUtils.toCalendar(setMonths).get(Calendar.MONTH));
        assertEquals(25, DateUtils.toCalendar(setDays).get(Calendar.DAY_OF_MONTH));
        assertEquals(14, DateUtils.toCalendar(setHours).get(Calendar.HOUR_OF_DAY));
        assertEquals(45, DateUtils.toCalendar(setMinutes).get(Calendar.MINUTE));
        assertEquals(50, DateUtils.toCalendar(setSeconds).get(Calendar.SECOND));
        assertEquals(750, DateUtils.toCalendar(setMillis).get(Calendar.MILLISECOND));
    }

    // Tests truncate, round, and ceiling for Date and Calendar
    @Test
    public void testTruncateRoundCeiling_variousFields_modifiesCorrectly() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.JANUARY, 15, 12, 35, 40);
        cal.set(Calendar.MILLISECOND, 600);
        Date date = cal.getTime();

        Date truncated = DateUtils.truncate(date, Calendar.HOUR_OF_DAY);
        assertEquals(0, DateUtils.toCalendar(truncated).get(Calendar.MINUTE));
        assertEquals(0, DateUtils.toCalendar(truncated).get(Calendar.SECOND));

        Date rounded = DateUtils.round(date, Calendar.HOUR_OF_DAY);
        assertEquals(13, DateUtils.toCalendar(rounded).get(Calendar.HOUR_OF_DAY));

        Date ceiled = DateUtils.ceiling(date, Calendar.HOUR_OF_DAY);
        assertEquals(13, DateUtils.toCalendar(ceiled).get(Calendar.HOUR_OF_DAY));

        Calendar calTrunc = DateUtils.truncate(cal, Calendar.DAY_OF_MONTH);
        assertEquals(0, calTrunc.get(Calendar.HOUR_OF_DAY));

        Calendar calCeil = DateUtils.ceiling(cal, Calendar.MONTH);
        assertEquals(Calendar.FEBRUARY, calCeil.get(Calendar.MONTH));
    }

    // Tests round with Object parameter and invalid type throwing ClassCastException
    @Test(expected = ClassCastException.class)
    public void testRound_invalidObjectType_throwsClassCastException() {
        DateUtils.round("invalid", Calendar.DAY_OF_MONTH);
    }

    // Tests truncatedEquals and truncatedCompareTo methods
    @Test
    public void testTruncatedEqualsAndCompareTo_sameAndDifferentFields_returnsExpected() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2020, Calendar.MAY, 10, 10, 15, 0);
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2020, Calendar.MAY, 10, 10, 45, 0);

        assertTrue(DateUtils.truncatedEquals(cal1, cal2, Calendar.HOUR_OF_DAY));
        assertFalse(DateUtils.truncatedEquals(cal1, cal2, Calendar.MINUTE));

        assertEquals(0, DateUtils.truncatedCompareTo(cal1, cal2, Calendar.HOUR_OF_DAY));
        assertTrue(DateUtils.truncatedCompareTo(cal1, cal2, Calendar.MINUTE) < 0);
        assertTrue(DateUtils.truncatedEquals(cal1.getTime(), cal2.getTime(), Calendar.DAY_OF_MONTH));
    }

    // Tests getFragmentInX methods for various fragments
    @Test
    public void testGetFragment_variousUnits_returnsCorrectCounts() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.JANUARY, 5, 6, 30, 15);
        cal.set(Calendar.MILLISECOND, 250);
        Date date = cal.getTime();

        assertEquals(250, DateUtils.getFragmentInMilliseconds(date, Calendar.SECOND));
        assertEquals(15, DateUtils.getFragmentInSeconds(date, Calendar.MINUTE));
        assertEquals(30, DateUtils.getFragmentInMinutes(date, Calendar.HOUR_OF_DAY));
        assertEquals(6, DateUtils.getFragmentInHours(date, Calendar.DAY_OF_YEAR));
        assertEquals(5, DateUtils.getFragmentInDays(date, Calendar.MONTH));

        assertEquals(250, DateUtils.getFragmentInMilliseconds(cal, Calendar.SECOND));
        assertEquals(15, DateUtils.getFragmentInSeconds(cal, Calendar.MINUTE));
        assertEquals(30, DateUtils.getFragmentInMinutes(cal, Calendar.HOUR_OF_DAY));
        assertEquals(6, DateUtils.getFragmentInHours(cal, Calendar.DAY_OF_YEAR));
        assertEquals(5, DateUtils.getFragmentInDays(cal, Calendar.MONTH));
    }

    // Tests date range iterator and NoSuchElementException / UnsupportedOperationException
    @Test
    public void testIterator_rangeWeekSunday_iteratesDaysCorrectly() {
        Calendar focus = Calendar.getInstance();
        focus.set(2020, Calendar.JUNE, 10); // Wednesday

        Iterator<Calendar> it = DateUtils.iterator(focus, DateUtils.RANGE_WEEK_SUNDAY);
        int count = 0;
        while (it.hasNext()) {
            Calendar day = it.next();
            assertNotNull(day);
            count++;
        }
        assertEquals(7, count);
    }

    // Tests iterator exception on next when exhausted and on remove call
    @Test(expected = UnsupportedOperationException.class)
    public void testIterator_removeCalled_throwsUnsupportedOperationException() {
        Calendar focus = Calendar.getInstance();
        Iterator<Calendar> it = DateUtils.iterator(focus, DateUtils.RANGE_WEEK_SUNDAY);
        it.next();
        it.remove();
    }

    // Tests iterator next past end throwing NoSuchElementException
    @Test(expected = NoSuchElementException.class)
    public void testIterator_nextExhausted_throwsNoSuchElementException() {
        Calendar focus = Calendar.getInstance();
        Iterator<Calendar> it = DateUtils.iterator(focus, DateUtils.RANGE_WEEK_SUNDAY);
        while (it.hasNext()) {
            it.next();
        }
        it.next();
    }

    // Tests constructor instantiation
    @Test
    public void testConstructor_defaultInstantiation_createsInstance() {
        DateUtils utils = new DateUtils();
        assertNotNull(utils);
    }
}