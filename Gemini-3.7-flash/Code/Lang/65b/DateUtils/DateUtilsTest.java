package org.apache.commons.lang.time;

import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link DateUtils}.
 */
public class DateUtilsTest {

    private DateFormat dateFormat;
    private Date date1;
    private Date date2;
    private Calendar cal1;
    private Calendar cal2;

    @Before
    public void setUp() throws Exception {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        date1 = dateFormat.parse("2004-02-15 15:30:45.500");
        date2 = dateFormat.parse("2004-02-15 18:45:10.123");

        cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        cal2 = Calendar.getInstance();
        cal2.setTime(date2);
    }

    // Tests constructor
    @Test
    public void testConstructor_default_instantiatesSuccessfully() {
        assertNotNull(new DateUtils());
    }

    // Tests isSameDay with matching and non-matching Dates
    @Test
    public void testIsSameDay_validDates_returnsCorrectBoolean() throws Exception {
        assertTrue(DateUtils.isSameDay(date1, date2));
        Date date3 = dateFormat.parse("2004-02-16 15:30:45.500");
        assertFalse(DateUtils.isSameDay(date1, date3));
    }

    // Tests isSameDay with null Date
    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDay_nullDate_throwsException() {
        DateUtils.isSameDay((Date) null, date2);
    }

    // Tests isSameDay with Calendars
    @Test
    public void testIsSameDay_validCalendars_returnsCorrectBoolean() {
        assertTrue(DateUtils.isSameDay(cal1, cal2));
        Calendar cal3 = (Calendar) cal1.clone();
        cal3.add(Calendar.DAY_OF_MONTH, 1);
        assertFalse(DateUtils.isSameDay(cal1, cal3));
    }

    // Tests isSameDay with null Calendar
    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDay_nullCalendar_throwsException() {
        DateUtils.isSameDay((Calendar) null, cal2);
    }

    // Tests isSameInstant with Dates and Calendars
    @Test
    public void testIsSameInstant_validInputs_returnsCorrectBoolean() {
        assertFalse(DateUtils.isSameInstant(date1, date2));
        assertTrue(DateUtils.isSameInstant(date1, new Date(date1.getTime())));

        assertFalse(DateUtils.isSameInstant(cal1, cal2));
        assertTrue(DateUtils.isSameInstant(cal1, (Calendar) cal1.clone()));
    }

    // Tests isSameInstant null handling
    @Test(expected = IllegalArgumentException.class)
    public void testIsSameInstant_nullDate_throwsException() {
        DateUtils.isSameInstant((Date) null, date2);
    }

    // Tests isSameLocalTime with Calendars
    @Test
    public void testIsSameLocalTime_sameAndDifferentCalendars_returnsCorrectBoolean() {
        Calendar calSame = (Calendar) cal1.clone();
        assertTrue(DateUtils.isSameLocalTime(cal1, calSame));
        assertFalse(DateUtils.isSameLocalTime(cal1, cal2));
    }

    // Tests isSameLocalTime with null Calendar
    @Test(expected = IllegalArgumentException.class)
    public void testIsSameLocalTime_nullCalendar_throwsException() {
        DateUtils.isSameLocalTime(cal1, null);
    }

    // Tests parseDate with valid and multiple patterns
    @Test
    public void testParseDate_validPatterns_parsesCorrectly() throws Exception {
        String[] patterns = new String[]{"yyyy/MM/dd", "yyyy-MM-dd HH:mm:ss"};
        Date parsed = DateUtils.parseDate("2004-02-15 15:30:45", patterns);
        assertNotNull(parsed);

        Date parsed2 = DateUtils.parseDate("2004/02/15", patterns);
        assertNotNull(parsed2);
    }

    // Tests parseDate unmatched patterns throwing ParseException
    @Test(expected = ParseException.class)
    public void testParseDate_unmatchedPattern_throwsParseException() throws Exception {
        String[] patterns = new String[]{"yyyy/MM/dd"};
        DateUtils.parseDate("2004-02-15", patterns);
    }

    // Tests parseDate with null input
    @Test(expected = IllegalArgumentException.class)
    public void testParseDate_nullInput_throwsIllegalArgumentException() throws Exception {
        DateUtils.parseDate(null, new String[]{"yyyy-MM-dd"});
    }

    // Tests add methods (Years, Months, Weeks, Days, Hours, Minutes, Seconds, Milliseconds)
    @Test
    public void testAddMethods_validAmounts_returnsUpdatedDate() throws Exception {
        Date base = dateFormat.parse("2004-02-15 12:00:00.000");

        Date addedYears = DateUtils.addYears(base, 1);
        assertEquals(dateFormat.parse("2005-02-15 12:00:00.000"), addedYears);

        Date addedMonths = DateUtils.addMonths(base, 1);
        assertEquals(dateFormat.parse("2004-03-15 12:00:00.000"), addedMonths);

        Date addedWeeks = DateUtils.addWeeks(base, 1);
        assertEquals(dateFormat.parse("2004-02-22 12:00:00.000"), addedWeeks);

        Date addedDays = DateUtils.addDays(base, 5);
        assertEquals(dateFormat.parse("2004-02-20 12:00:00.000"), addedDays);

        Date addedHours = DateUtils.addHours(base, 2);
        assertEquals(dateFormat.parse("2004-02-15 14:00:00.000"), addedHours);

        Date addedMinutes = DateUtils.addMinutes(base, 30);
        assertEquals(dateFormat.parse("2004-02-15 12:30:00.000"), addedMinutes);

        Date addedSeconds = DateUtils.addSeconds(base, 45);
        assertEquals(dateFormat.parse("2004-02-15 12:00:45.000"), addedSeconds);

        Date addedMillis = DateUtils.addMilliseconds(base, 250);
        assertEquals(dateFormat.parse("2004-02-15 12:00:00.250"), addedMillis);
    }

    // Tests add with null date
    @Test(expected = IllegalArgumentException.class)
    public void testAdd_nullDate_throwsException() {
        DateUtils.add(null, Calendar.DATE, 1);
    }

    // Tests round Date and Calendar for HOUR and MONTH fields
    @Test
    public void testRound_variousFields_roundsCorrectly() throws Exception {
        Date d1 = dateFormat.parse("2004-02-15 13:45:01.231");
        Date roundedHour = DateUtils.round(d1, Calendar.HOUR);
        assertEquals(dateFormat.parse("2004-02-15 14:00:00.000"), roundedHour);

        Date roundedMonth = DateUtils.round(d1, Calendar.MONTH);
        assertEquals(dateFormat.parse("2004-03-01 00:00:00.000"), roundedMonth);

        Calendar cal = Calendar.getInstance();
        cal.setTime(d1);
        Calendar roundedCal = DateUtils.round(cal, Calendar.MINUTE);
        assertEquals(45, roundedCal.get(Calendar.MINUTE));

        Date roundedObj = DateUtils.round((Object) d1, Calendar.DATE);
        assertEquals(dateFormat.parse("2004-02-16 00:00:00.000"), roundedObj);

        Date roundedCalObj = DateUtils.round((Object) cal, Calendar.DATE);
        assertEquals(dateFormat.parse("2004-02-16 00:00:00.000"), roundedCalObj);
    }

    // Tests round with SEMI_MONTH field
    @Test
    public void testRound_semiMonth_roundsCorrectly() throws Exception {
        Date d1 = dateFormat.parse("2004-02-01 00:00:00.000");
        Date rounded1 = DateUtils.round(d1, DateUtils.SEMI_MONTH);
        assertEquals(dateFormat.parse("2004-02-01 00:00:00.000"), rounded1);

        Date d2 = dateFormat.parse("2004-02-09 00:00:00.000");
        Date rounded2 = DateUtils.round(d2, DateUtils.SEMI_MONTH);
        assertEquals(dateFormat.parse("2004-02-16 00:00:00.000"), rounded2);

        Date d3 = dateFormat.parse("2004-02-25 00:00:00.000");
        Date rounded3 = DateUtils.round(d3, DateUtils.SEMI_MONTH);
        assertEquals(dateFormat.parse("2004-03-01 00:00:00.000"), rounded3);
    }

    // Tests truncate for Date, Calendar and Object
    @Test
    public void testTruncate_variousFields_truncatesCorrectly() throws Exception {
        Date d = dateFormat.parse("2004-02-15 13:45:01.231");

        Date truncatedHour = DateUtils.truncate(d, Calendar.HOUR);
        assertEquals(dateFormat.parse("2004-02-15 13:00:00.000"), truncatedHour);

        Date truncatedMonth = DateUtils.truncate(d, Calendar.MONTH);
        assertEquals(dateFormat.parse("2004-02-01 00:00:00.000"), truncatedMonth);

        Date truncatedSecond = DateUtils.truncate(d, Calendar.SECOND);
        assertEquals(dateFormat.parse("2004-02-15 13:45:01.000"), truncatedSecond);

        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        Calendar truncatedCal = DateUtils.truncate(cal, Calendar.DATE);
        assertEquals(dateFormat.parse("2004-02-15 00:00:00.000"), truncatedCal.getTime());

        Date truncatedObj = DateUtils.truncate((Object) d, Calendar.YEAR);
        assertEquals(dateFormat.parse("2004-01-01 00:00:00.000"), truncatedObj);

        Date truncatedCalObj = DateUtils.truncate((Object) cal, Calendar.YEAR);
        assertEquals(dateFormat.parse("2004-01-01 00:00:00.000"), truncatedCalObj);
    }

    // Tests truncate and round with SEMI_MONTH and AM_PM
    @Test
    public void testTruncate_semiMonthAndAmPm_truncatesCorrectly() throws Exception {
        Date d1 = dateFormat.parse("2004-02-20 15:30:45.500");
        Date truncatedSemiMonth = DateUtils.truncate(d1, DateUtils.SEMI_MONTH);
        assertEquals(dateFormat.parse("2004-02-16 00:00:00.000"), truncatedSemiMonth);

        Date truncatedAmPm = DateUtils.truncate(d1, Calendar.AM_PM);
        assertEquals(dateFormat.parse("2004-02-20 12:00:00.000"), truncatedAmPm);

        Date d2 = dateFormat.parse("2004-02-05 08:30:45.500");
        Date truncatedAmPm2 = DateUtils.truncate(d2, Calendar.AM_PM);
        assertEquals(dateFormat.parse("2004-02-05 00:00:00.000"), truncatedAmPm2);
    }

    // Tests round with unsupported field throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testRound_unsupportedField_throwsIllegalArgumentException() {
        DateUtils.round(date1, -999);
    }

    // Tests round with invalid object type throws ClassCastException
    @Test(expected = ClassCastException.class)
    public void testRound_invalidObjectType_throwsClassCastException() {
        DateUtils.round("Not a Date", Calendar.DATE);
    }

    // Tests truncate with invalid object type throws ClassCastException
    @Test(expected = ClassCastException.class)
    public void testTruncate_invalidObjectType_throwsClassCastException() {
        DateUtils.truncate("Not a Date", Calendar.DATE);
    }

    // Tests round with huge year throwing ArithmeticException
    @Test(expected = ArithmeticException.class)
    public void testRound_hugeYear_throwsArithmeticException() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 280000001);
        DateUtils.round(cal, Calendar.YEAR);
    }

    // Tests iterator with all supported range styles
    @Test
    public void testIterator_validRangeStyles_iteratesExpectedDays() {
        int[] styles = new int[]{
                DateUtils.RANGE_MONTH_SUNDAY,
                DateUtils.RANGE_MONTH_MONDAY,
                DateUtils.RANGE_WEEK_SUNDAY,
                DateUtils.RANGE_WEEK_MONDAY,
                DateUtils.RANGE_WEEK_RELATIVE,
                DateUtils.RANGE_WEEK_CENTER
        };

        for (int i = 0; i < styles.length; i++) {
            Iterator it = DateUtils.iterator(date1, styles[i]);
            assertNotNull(it);
            assertTrue(it.hasNext());
            int count = 0;
            while (it.hasNext()) {
                Object next = it.next();
                assertTrue(next instanceof Calendar);
                count++;
            }
            assertTrue(count >= 7);
        }

        Iterator calIt = DateUtils.iterator(cal1, DateUtils.RANGE_WEEK_SUNDAY);
        assertNotNull(calIt);
        assertTrue(calIt.hasNext());

        Iterator objIt = DateUtils.iterator((Object) date1, DateUtils.RANGE_WEEK_SUNDAY);
        assertNotNull(objIt);
        assertTrue(objIt.hasNext());
    }

    // Tests iterator with invalid range style
    @Test(expected = IllegalArgumentException.class)
    public void testIterator_invalidRangeStyle_throwsException() {
        DateUtils.iterator(date1, -1);
    }

    // Tests iterator with null input
    @Test(expected = IllegalArgumentException.class)
    public void testIterator_nullDate_throwsException() {
        DateUtils.iterator((Date) null, DateUtils.RANGE_WEEK_SUNDAY);
    }

    // Tests iterator with invalid object type
    @Test(expected = ClassCastException.class)
    public void testIterator_invalidObjectType_throwsClassCastException() {
        DateUtils.iterator("Not a date", DateUtils.RANGE_WEEK_SUNDAY);
    }

    // Tests DateIterator remove method throwing UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testDateIterator_remove_throwsUnsupportedOperationException() {
        Iterator it = DateUtils.iterator(date1, DateUtils.RANGE_WEEK_SUNDAY);
        it.remove();
    }

    // Tests DateIterator next past end throwing NoSuchElementException
    @Test(expected = NoSuchElementException.class)
    public void testDateIterator_nextPastEnd_throwsNoSuchElementException() {
        Iterator it = DateUtils.iterator(date1, DateUtils.RANGE_WEEK_SUNDAY);
        while (it.hasNext()) {
            it.next();
        }
        it.next();
    }
}