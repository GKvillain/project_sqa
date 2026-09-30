package org.apache.commons.lang.time;

import org.junit.Before;
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

    private Date date1;
    private Date date2;
    private Calendar cal1;
    private Calendar cal2;

    @Before
    public void setUp() {
        cal1 = Calendar.getInstance();
        cal1.set(2004, Calendar.JULY, 4, 12, 30, 40);
        cal1.set(Calendar.MILLISECOND, 500);
        date1 = cal1.getTime();

        cal2 = Calendar.getInstance();
        cal2.set(2004, Calendar.JULY, 4, 16, 20, 10);
        cal2.set(Calendar.MILLISECOND, 100);
        date2 = cal2.getTime();
    }

    // Tests constructor instantiation
    @Test
    public void testConstructor_instanceCreation_succeeds() {
        assertNotNull(new DateUtils());
    }

    // Tests isSameDay with valid dates on the same day and different days
    @Test
    public void testIsSameDay_validDates_returnsCorrectBoolean() {
        assertTrue(DateUtils.isSameDay(date1, date2));
        assertTrue(DateUtils.isSameDay(cal1, cal2));

        Calendar calDifferentDay = Calendar.getInstance();
        calDifferentDay.set(2004, Calendar.JULY, 5, 12, 30, 40);
        assertFalse(DateUtils.isSameDay(cal1, calDifferentDay));
        assertFalse(DateUtils.isSameDay(date1, calDifferentDay.getTime()));
    }

    // Tests isSameDay with null input throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDay_nullDate_throwsException() {
        DateUtils.isSameDay((Date) null, date2);
    }

    // Tests isSameInstant for Date and Calendar
    @Test
    public void testIsSameInstant_validInputs_returnsCorrectBoolean() {
        Date sameInstantDate = new Date(date1.getTime());
        assertTrue(DateUtils.isSameInstant(date1, sameInstantDate));
        assertFalse(DateUtils.isSameInstant(date1, date2));

        Calendar sameInstantCal = Calendar.getInstance();
        sameInstantCal.setTime(date1);
        assertTrue(DateUtils.isSameInstant(cal1, sameInstantCal));
        assertFalse(DateUtils.isSameInstant(cal1, cal2));
    }

    // Tests isSameInstant with null input throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testIsSameInstant_nullCalendar_throwsException() {
        DateUtils.isSameInstant((Calendar) null, cal2);
    }

    // Tests isSameLocalTime with matching and non-matching local times
    @Test
    public void testIsSameLocalTime_validCalendars_returnsCorrectBoolean() {
        Calendar calSameLocal = (Calendar) cal1.clone();
        assertTrue(DateUtils.isSameLocalTime(cal1, calSameLocal));
        assertFalse(DateUtils.isSameLocalTime(cal1, cal2));
    }

    // Tests isSameLocalTime with null input throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testIsSameLocalTime_nullCalendar_throwsException() {
        DateUtils.isSameLocalTime(cal1, null);
    }

    // Tests parseDate with valid matching formats
    @Test
    public void testParseDate_validPatterns_parsesCorrectly() throws ParseException {
        String[] patterns = new String[]{"yyyy/MM/dd", "yyyy-MM-dd HH:mm:ss"};
        Date parsed = DateUtils.parseDate("2004-07-04 12:30:40", patterns);
        assertNotNull(parsed);

        Calendar parsedCal = Calendar.getInstance();
        parsedCal.setTime(parsed);
        assertEquals(2004, parsedCal.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, parsedCal.get(Calendar.MONTH));
        assertEquals(4, parsedCal.get(Calendar.DAY_OF_MONTH));
        assertEquals(12, parsedCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, parsedCal.get(Calendar.MINUTE));
        assertEquals(40, parsedCal.get(Calendar.SECOND));
    }

    // Tests parseDate with non-matching pattern throwing ParseException
    @Test(expected = ParseException.class)
    public void testParseDate_unmatchedPattern_throwsParseException() throws ParseException {
        String[] patterns = new String[]{"yyyy/MM/dd"};
        DateUtils.parseDate("2004-07-04", patterns);
    }

    // Tests parseDate with null input throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testParseDate_nullInput_throwsException() throws ParseException {
        DateUtils.parseDate(null, new String[]{"yyyy/MM/dd"});
    }

    // Tests adding various units of time to Date
    @Test
    public void testAddMethods_validInputs_returnsAdjustedDate() {
        Date afterYears = DateUtils.addYears(date1, 1);
        Calendar checkCal = Calendar.getInstance();
        checkCal.setTime(afterYears);
        assertEquals(2005, checkCal.get(Calendar.YEAR));

        Date afterMonths = DateUtils.addMonths(date1, 1);
        checkCal.setTime(afterMonths);
        assertEquals(Calendar.AUGUST, checkCal.get(Calendar.MONTH));

        Date afterWeeks = DateUtils.addWeeks(date1, 1);
        checkCal.setTime(afterWeeks);
        assertEquals(11, checkCal.get(Calendar.DAY_OF_MONTH));

        Date afterDays = DateUtils.addDays(date1, 2);
        checkCal.setTime(afterDays);
        assertEquals(6, checkCal.get(Calendar.DAY_OF_MONTH));

        Date afterHours = DateUtils.addHours(date1, 2);
        checkCal.setTime(afterHours);
        assertEquals(14, checkCal.get(Calendar.HOUR_OF_DAY));

        Date afterMinutes = DateUtils.addMinutes(date1, 10);
        checkCal.setTime(afterMinutes);
        assertEquals(40, checkCal.get(Calendar.MINUTE));

        Date afterSeconds = DateUtils.addSeconds(date1, 10);
        checkCal.setTime(afterSeconds);
        assertEquals(50, checkCal.get(Calendar.SECOND));

        Date afterMillis = DateUtils.addMilliseconds(date1, 100);
        checkCal.setTime(afterMillis);
        assertEquals(600, checkCal.get(Calendar.MILLISECOND));
    }

    // Tests add method with null input throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testAdd_nullDate_throwsException() {
        DateUtils.add(null, Calendar.YEAR, 1);
    }

    // Tests round with Calendar and Date on various fields
    @Test
    public void testRound_variousFields_roundsCorrectly() {
        // Rounding minutes up from 40s
        Date roundedMinute = DateUtils.round(date1, Calendar.MINUTE);
        Calendar resCal = Calendar.getInstance();
        resCal.setTime(roundedMinute);
        assertEquals(31, resCal.get(Calendar.MINUTE));
        assertEquals(0, resCal.get(Calendar.SECOND));

        // Rounding hour up from 30m 40s
        Calendar roundedHourCal = DateUtils.round(cal1, Calendar.HOUR_OF_DAY);
        assertEquals(13, roundedHourCal.get(Calendar.HOUR_OF_DAY));

        // Rounding SEMI_MONTH
        Calendar semiCal = Calendar.getInstance();
        semiCal.set(2004, Calendar.JULY, 10);
        Date roundedSemi = DateUtils.round((Object) semiCal, DateUtils.SEMI_MONTH);
        resCal.setTime(roundedSemi);
        assertEquals(1, resCal.get(Calendar.DAY_OF_MONTH));
    }

    // Tests round with invalid object type throwing ClassCastException
    @Test(expected = ClassCastException.class)
    public void testRound_invalidType_throwsClassCastException() {
        DateUtils.round("invalid-date-object", Calendar.DATE);
    }

    // Tests truncate with Calendar and Date on various fields
    @Test
    public void testTruncate_variousFields_truncatesCorrectly() {
        Date truncatedHour = DateUtils.truncate(date1, Calendar.HOUR_OF_DAY);
        Calendar resCal = Calendar.getInstance();
        resCal.setTime(truncatedHour);
        assertEquals(12, resCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, resCal.get(Calendar.MINUTE));
        assertEquals(0, resCal.get(Calendar.SECOND));
        assertEquals(0, resCal.get(Calendar.MILLISECOND));

        Calendar truncatedDayCal = DateUtils.truncate(cal1, Calendar.DATE);
        assertEquals(0, truncatedDayCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, truncatedDayCal.get(Calendar.MINUTE));
        assertEquals(0, truncatedDayCal.get(Calendar.SECOND));
        assertEquals(0, truncatedDayCal.get(Calendar.MILLISECOND));

        // Truncate Date with SEMI_MONTH
        Calendar semiCal = Calendar.getInstance();
        semiCal.set(2004, Calendar.JULY, 20);
        Date truncatedSemi = DateUtils.truncate(semiCal.getTime(), DateUtils.SEMI_MONTH);
        resCal.setTime(truncatedSemi);
        assertEquals(16, resCal.get(Calendar.DAY_OF_MONTH));
    }

    // Tests truncate with unsupported field throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testTruncate_unsupportedField_throwsException() {
        DateUtils.truncate(date1, -999);
    }

    // Tests iterator across different range styles
    @Test
    public void testIterator_rangeStyles_iteratesProperRange() {
        Iterator itMonth = DateUtils.iterator(cal1, DateUtils.RANGE_MONTH_SUNDAY);
        assertNotNull(itMonth);
        assertTrue(itMonth.hasNext());
        Calendar firstDay = (Calendar) itMonth.next();
        assertEquals(Calendar.SUNDAY, firstDay.get(Calendar.DAY_OF_WEEK));

        Iterator itWeek = DateUtils.iterator((Object) date1, DateUtils.RANGE_WEEK_MONDAY);
        assertTrue(itWeek.hasNext());
        Calendar firstWeekDay = (Calendar) itWeek.next();
        assertEquals(Calendar.MONDAY, firstWeekDay.get(Calendar.DAY_OF_WEEK));

        Iterator itCenter = DateUtils.iterator(cal1, DateUtils.RANGE_WEEK_CENTER);
        assertTrue(itCenter.hasNext());

        Iterator itRelative = DateUtils.iterator(cal1, DateUtils.RANGE_WEEK_RELATIVE);
        assertTrue(itRelative.hasNext());
    }

    // Tests DateIterator next when finished throwing NoSuchElementException
    @Test(expected = NoSuchElementException.class)
    public void testDateIterator_exhaustedIterator_throwsNoSuchElementException() {
        Iterator it = DateUtils.iterator(cal1, DateUtils.RANGE_WEEK_SUNDAY);
        while (it.hasNext()) {
            it.next();
        }
        it.next();
    }

    // Tests DateIterator remove throwing UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testDateIterator_remove_throwsUnsupportedOperationException() {
        Iterator it = DateUtils.iterator(date1, DateUtils.RANGE_WEEK_SUNDAY);
        it.remove();
    }

    // Tests iterator with invalid rangeStyle throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testIterator_invalidRangeStyle_throwsException() {
        DateUtils.iterator(cal1, 9999);
    }
}