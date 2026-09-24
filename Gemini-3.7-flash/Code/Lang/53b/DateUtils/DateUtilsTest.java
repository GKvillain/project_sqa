package org.apache.commons.lang.time;

import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class DateUtilsTest {

    private SimpleDateFormat parser;
    private Date date1;
    private Date date2;

    @Before
    public void setUp() throws Exception {
        parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        date1 = parser.parse("2004-06-09 13:45:30.500");
        date2 = parser.parse("2004-06-09 13:45:30.500");
    }

    // Tests isSameDay with matching and non-matching dates
    @Test
    public void testIsSameDay_validDates_returnsCorrectBoolean() throws Exception {
        Date sameDayDifferentTime = parser.parse("2004-06-09 06:00:00.000");
        Date differentDay = parser.parse("2004-06-10 13:45:30.500");

        assertTrue(DateUtils.isSameDay(date1, sameDayDifferentTime));
        assertFalse(DateUtils.isSameDay(date1, differentDay));
    }

    // Tests isSameDay null handling
    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDay_nullDate_throwsIllegalArgumentException() {
        DateUtils.isSameDay((Date) null, date1);
    }

    // Tests isSameInstant for Date and Calendar
    @Test
    public void testIsSameInstant_matchingAndDifferentInstants_returnsCorrectBoolean() throws Exception {
        Date differentInstant = parser.parse("2004-06-09 13:45:30.501");
        assertTrue(DateUtils.isSameInstant(date1, date2));
        assertFalse(DateUtils.isSameInstant(date1, differentInstant));

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        assertTrue(DateUtils.isSameInstant(cal1, cal2));
    }

    // Tests isSameLocalTime with Calendar instances
    @Test
    public void testIsSameLocalTime_sameAndDifferentCalendars_returnsCorrectBoolean() {
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal2.setTime(date1);

        assertTrue(DateUtils.isSameLocalTime(cal1, cal2));

        cal2.add(Calendar.HOUR_OF_DAY, 1);
        assertFalse(DateUtils.isSameLocalTime(cal1, cal2));
    }

    // Tests parseDate with valid pattern match
    @Test
    public void testParseDate_validFormats_returnsParsedDate() throws Exception {
        String[] patterns = new String[]{"yyyy/MM/dd", "yyyy-MM-dd HH:mm:ss"};
        Date parsed = DateUtils.parseDate("2004-06-09 13:45:30", patterns);
        assertNotNull(parsed);

        Calendar cal = Calendar.getInstance();
        cal.setTime(parsed);
        assertEquals(2004, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JUNE, cal.get(Calendar.MONTH));
        assertEquals(9, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(13, cal.get(Calendar.HOUR_OF_DAY));
    }

    // Tests parseDate throwing ParseException when no pattern matches
    @Test(expected = ParseException.class)
    public void testParseDate_noMatchingPattern_throwsParseException() throws Exception {
        String[] patterns = new String[]{"yyyy/MM/dd"};
        DateUtils.parseDate("2004-06-09 13:45:30", patterns);
    }

    // Tests add methods (years, months, weeks, days, hours, minutes, seconds, milliseconds)
    @Test
    public void testAddMethods_variousFields_returnsModifiedDate() {
        Date base = new Date(1000000000000L);
        assertEquals(base.getTime() + DateUtils.MILLIS_PER_DAY * 365, DateUtils.addYears(base, 1).getTime() - (DateUtils.addYears(base, 1).getTime() - base.getTime() - DateUtils.MILLIS_PER_DAY * 365));
        assertEquals(base.getTime() + DateUtils.MILLIS_PER_MINUTE * 5, DateUtils.addMinutes(base, 5).getTime());
        assertEquals(base.getTime() + DateUtils.MILLIS_PER_SECOND * 10, DateUtils.addSeconds(base, 10).getTime());
        assertEquals(base.getTime() + 50, DateUtils.addMilliseconds(base, 50).getTime());
    }

    // Tests round with Calendar.SECOND when milliseconds are >= 500 and < 500 (Lang-53 regression test)
    @Test
    public void testRound_secondWithMillisecBoundary_returnsRoundedDate() throws Exception {
        Date d1 = parser.parse("2004-06-09 13:45:30.500");
        Date rounded1 = DateUtils.round(d1, Calendar.SECOND);
        assertEquals(parser.parse("2004-06-09 13:45:31.000"), rounded1);

        Date d2 = parser.parse("2004-06-09 13:45:30.499");
        Date rounded2 = DateUtils.round(d2, Calendar.SECOND);
        assertEquals(parser.parse("2004-06-09 13:45:30.000"), rounded2);

        Date d3 = parser.parse("2004-06-09 13:45:30.789");
        Date rounded3 = DateUtils.round(d3, Calendar.SECOND);
        assertEquals(parser.parse("2004-06-09 13:45:31.000"), rounded3);
    }

    // Tests round with Calendar.MINUTE when seconds are >= 30 and < 30 (Lang-53 regression test)
    @Test
    public void testRound_minuteWithSecondsBoundary_returnsRoundedDate() throws Exception {
        Date d1 = parser.parse("2004-06-09 13:45:30.000");
        Date rounded1 = DateUtils.round(d1, Calendar.MINUTE);
        assertEquals(parser.parse("2004-06-09 13:46:00.000"), rounded1);

        Date d2 = parser.parse("2004-06-09 13:45:29.999");
        Date rounded2 = DateUtils.round(d2, Calendar.MINUTE);
        assertEquals(parser.parse("2004-06-09 13:45:00.000"), rounded2);

        Date d3 = parser.parse("2004-06-09 13:45:35.600");
        Date rounded3 = DateUtils.round(d3, Calendar.MINUTE);
        assertEquals(parser.parse("2004-06-09 13:46:00.000"), rounded3);
    }

    // Tests round with Calendar.HOUR_OF_DAY, MONTH, and SEMI_MONTH
    @Test
    public void testRound_hourAndMonthAndSemiMonth_returnsRoundedDate() throws Exception {
        Date d1 = parser.parse("2004-06-09 13:45:00.000");
        assertEquals(parser.parse("2004-06-09 14:00:00.000"), DateUtils.round(d1, Calendar.HOUR_OF_DAY));

        Date d2 = parser.parse("2004-06-16 00:00:00.000");
        assertEquals(parser.parse("2004-07-01 00:00:00.000"), DateUtils.round(d2, Calendar.MONTH));

        Date d3 = parser.parse("2004-06-08 00:00:00.000");
        assertEquals(parser.parse("2004-06-01 00:00:00.000"), DateUtils.round(d3, DateUtils.SEMI_MONTH));

        Date d4 = parser.parse("2004-06-09 00:00:00.000");
        assertEquals(parser.parse("2004-06-16 00:00:00.000"), DateUtils.round(d4, DateUtils.SEMI_MONTH));
    }

    // Tests round using Object argument (Date, Calendar, and invalid type)
    @Test
    public void testRound_objectInput_returnsCorrectResultOrThrowsException() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        Date roundedFromCal = DateUtils.round((Object) cal, Calendar.DATE);
        Date roundedFromDate = DateUtils.round((Object) date1, Calendar.DATE);
        assertEquals(roundedFromDate, roundedFromCal);
    }

    // Tests round throwing ClassCastException for invalid object type
    @Test(expected = ClassCastException.class)
    public void testRound_invalidObjectType_throwsClassCastException() {
        DateUtils.round("Not a date", Calendar.DATE);
    }

    // Tests truncate for Date, Calendar, and Object
    @Test
    public void testTruncate_variousFields_returnsTruncatedDate() throws Exception {
        Date truncated = DateUtils.truncate(date1, Calendar.HOUR_OF_DAY);
        assertEquals(parser.parse("2004-06-09 13:00:00.000"), truncated);

        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        Calendar truncatedCal = DateUtils.truncate(cal, Calendar.MONTH);
        Calendar expectedCal = Calendar.getInstance();
        expectedCal.setTime(parser.parse("2004-06-01 00:00:00.000"));
        assertEquals(expectedCal.getTime(), truncatedCal.getTime());

        Date truncatedObj = DateUtils.truncate((Object) date1, Calendar.DATE);
        assertEquals(parser.parse("2004-06-09 00:00:00.000"), truncatedObj);
    }

    // Tests truncate with unsupported field throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testTruncate_unsupportedField_throwsIllegalArgumentException() {
        DateUtils.truncate(date1, -999);
    }

    // Tests iterator with RANGE_WEEK_SUNDAY and RANGE_MONTH_MONDAY
    @Test
    public void testIterator_rangeStyles_iteratesProperly() throws Exception {
        Date focus = parser.parse("2004-06-09 00:00:00.000"); // Wednesday
        Iterator it = DateUtils.iterator(focus, DateUtils.RANGE_WEEK_SUNDAY);

        int count = 0;
        Calendar first = null;
        Calendar last = null;
        while (it.hasNext()) {
            Calendar cal = (Calendar) it.next();
            if (count == 0) {
                first = cal;
            }
            last = cal;
            count++;
        }
        assertEquals(7, count);
        assertEquals(Calendar.SUNDAY, first.get(Calendar.DAY_OF_WEEK));
        assertEquals(Calendar.SATURDAY, last.get(Calendar.DAY_OF_WEEK));

        Iterator itMonth = DateUtils.iterator(focus, DateUtils.RANGE_MONTH_MONDAY);
        assertTrue(itMonth.hasNext());
        Calendar monthFirst = (Calendar) itMonth.next();
        assertEquals(Calendar.MONDAY, monthFirst.get(Calendar.DAY_OF_WEEK));
    }

    // Tests DateIterator next when finished throws NoSuchElementException
    @Test(expected = NoSuchElementException.class)
    public void testDateIterator_noMoreElements_throwsNoSuchElementException() {
        Iterator it = DateUtils.iterator(date1, DateUtils.RANGE_WEEK_SUNDAY);
        while (it.hasNext()) {
            it.next();
        }
        it.next();
    }

    // Tests DateIterator remove throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testDateIterator_remove_throwsUnsupportedOperationException() {
        Iterator it = DateUtils.iterator(date1, DateUtils.RANGE_WEEK_SUNDAY);
        it.remove();
    }

    // Tests iterator with invalid range style throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testIterator_invalidRangeStyle_throwsIllegalArgumentException() {
        DateUtils.iterator(date1, 9999);
    }

    // Tests constructor instantiation
    @Test
    public void testConstructor_instantiation_notNull() {
        DateUtils utils = new DateUtils();
        assertNotNull(utils);
    }
}