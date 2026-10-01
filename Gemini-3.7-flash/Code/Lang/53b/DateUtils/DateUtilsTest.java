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

public class DateUtilsTest {

    private DateFormat dateFormat;
    private Calendar cal;
    private Date date1;
    private Date date2;

    @Before
    public void setUp() throws Exception {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(2004, Calendar.JULY, 4, 13, 45, 30);
        cal.set(Calendar.MILLISECOND, 500);

        date1 = dateFormat.parse("2004-07-04 13:45:30.500");
        date2 = dateFormat.parse("2004-07-04 13:45:30.000");
    }

    // Tests constructor
    @Test
    public void testConstructor_default_instantiates() {
        assertNotNull(new DateUtils());
    }

    // Tests isSameDay with Date and Calendar objects
    @Test
    public void testIsSameDay_validDates_returnsCorrectBoolean() throws ParseException {
        Date d1 = dateFormat.parse("2004-07-04 06:00:00.000");
        Date d2 = dateFormat.parse("2004-07-04 18:00:00.000");
        Date d3 = dateFormat.parse("2004-07-05 06:00:00.000");

        assertTrue(DateUtils.isSameDay(d1, d2));
        assertFalse(DateUtils.isSameDay(d1, d3));

        Calendar c1 = Calendar.getInstance();
        Calendar c2 = (Calendar) c1.clone();
        assertTrue(DateUtils.isSameDay(c1, c2));
    }

    // Tests isSameDay with null inputs
    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDay_nullDate_throwsException() {
        DateUtils.isSameDay((Date) null, new Date());
    }

    // Tests isSameInstant
    @Test
    public void testIsSameInstant_validInputs_returnsCorrectBoolean() {
        Date d1 = new Date(1000L);
        Date d2 = new Date(1000L);
        Date d3 = new Date(2000L);

        assertTrue(DateUtils.isSameInstant(d1, d2));
        assertFalse(DateUtils.isSameInstant(d1, d3));

        Calendar c1 = Calendar.getInstance();
        c1.setTime(d1);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);
        assertTrue(DateUtils.isSameInstant(c1, c2));
    }

    // Tests isSameLocalTime
    @Test
    public void testIsSameLocalTime_sameAndDifferent_returnsCorrectBoolean() {
        Calendar c1 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        Calendar c2 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c1.setTime(date1);
        c2.setTime(date1);

        assertTrue(DateUtils.isSameLocalTime(c1, c2));

        c2.add(Calendar.SECOND, 1);
        assertFalse(DateUtils.isSameLocalTime(c1, c2));
    }

    // Tests parseDate with valid and invalid patterns
    @Test
    public void testParseDate_validAndInvalid_parsesOrThrowsException() throws ParseException {
        String[] parsers = new String[]{"yyyy/MM/dd", "yyyy-MM-dd HH:mm:ss"};
        Date parsed = DateUtils.parseDate("2004-07-04 13:45:30", parsers);
        assertNotNull(parsed);

        Date parsed2 = DateUtils.parseDate("2004/07/04", parsers);
        assertNotNull(parsed2);
    }

    // Tests parseDate with unmatched pattern
    @Test(expected = ParseException.class)
    public void testParseDate_unmatchedPattern_throwsParseException() throws ParseException {
        DateUtils.parseDate("2004.07.04", new String[]{"yyyy-MM-dd"});
    }

    // Tests add methods
    @Test
    public void testAddMethods_positiveAndNegativeValues_modifiesDateCorrectly() {
        Date base = date2;

        Date resYears = DateUtils.addYears(base, 1);
        assertEquals(base.getTime() + (365L * 24 * 3600 * 1000), resYears.getTime());

        Date resMonths = DateUtils.addMonths(base, 1);
        assertNotNull(resMonths);

        Date resWeeks = DateUtils.addWeeks(base, 1);
        assertEquals(base.getTime() + (7L * 24 * 3600 * 1000), resWeeks.getTime());

        Date resDays = DateUtils.addDays(base, 2);
        assertEquals(base.getTime() + (2L * 24 * 3600 * 1000), resDays.getTime());

        Date resHours = DateUtils.addHours(base, 3);
        assertEquals(base.getTime() + (3L * 3600 * 1000), resHours.getTime());

        Date resMinutes = DateUtils.addMinutes(base, 4);
        assertEquals(base.getTime() + (4L * 60 * 1000), resMinutes.getTime());

        Date resSeconds = DateUtils.addSeconds(base, 5);
        assertEquals(base.getTime() + (5L * 1000), resSeconds.getTime());

        Date resMillis = DateUtils.addMilliseconds(base, 600);
        assertEquals(base.getTime() + 600, resMillis.getTime());
    }

    // Tests round with minute boundary (Lang-53 regression test)
    @Test
    public void testRound_minute_roundsCorrectly() throws ParseException {
        Date d1 = dateFormat.parse("2004-07-04 13:45:30.000");
        Date expected1 = dateFormat.parse("2004-07-04 13:46:00.000");
        assertEquals(expected1, DateUtils.round(d1, Calendar.MINUTE));

        Date d2 = dateFormat.parse("2004-07-04 13:45:29.999");
        Date expected2 = dateFormat.parse("2004-07-04 13:45:00.000");
        assertEquals(expected2, DateUtils.round(d2, Calendar.MINUTE));

        Date d3 = dateFormat.parse("2004-07-04 13:45:30.500");
        Date expected3 = dateFormat.parse("2004-07-04 13:46:00.000");
        assertEquals(expected3, DateUtils.round(d3, Calendar.MINUTE));
    }

    // Tests round with second boundary (Lang-53 regression test)
    @Test
    public void testRound_second_roundsCorrectly() throws ParseException {
        Date d1 = dateFormat.parse("2004-07-04 13:45:30.500");
        Date expected1 = dateFormat.parse("2004-07-04 13:45:31.000");
        assertEquals(expected1, DateUtils.round(d1, Calendar.SECOND));

        Date d2 = dateFormat.parse("2004-07-04 13:45:30.499");
        Date expected2 = dateFormat.parse("2004-07-04 13:45:30.000");
        assertEquals(expected2, DateUtils.round(d2, Calendar.SECOND));
    }

    // Tests round with hour, day, month, year, and SEMI_MONTH
    @Test
    public void testRound_variousFields_roundsCorrectly() throws ParseException {
        Date d = dateFormat.parse("2004-07-16 13:45:30.000");

        Date roundedHour = DateUtils.round(d, Calendar.HOUR_OF_DAY);
        assertEquals(dateFormat.parse("2004-07-16 14:00:00.000"), roundedHour);

        Date roundedDay = DateUtils.round(d, Calendar.DAY_OF_MONTH);
        assertEquals(dateFormat.parse("2004-07-17 00:00:00.000"), roundedDay);

        Date roundedMonth = DateUtils.round(d, Calendar.MONTH);
        assertEquals(dateFormat.parse("2004-08-01 00:00:00.000"), roundedMonth);

        Date roundedSemiMonth = DateUtils.round(d, DateUtils.SEMI_MONTH);
        assertEquals(dateFormat.parse("2004-07-16 00:00:00.000"), roundedSemiMonth);

        Date roundedAmPm = DateUtils.round(d, Calendar.AM_PM);
        assertEquals(dateFormat.parse("2004-07-17 00:00:00.000"), roundedAmPm);

        Date roundedYear = DateUtils.round(d, Calendar.YEAR);
        assertEquals(dateFormat.parse("2005-01-01 00:00:00.000"), roundedYear);
    }

    // Tests round and truncate with Calendar and Object overloads
    @Test
    public void testRoundAndTruncate_objectAndCalendarOverloads_succeed() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.set(2004, Calendar.JULY, 4, 13, 45, 30);
        c.set(Calendar.MILLISECOND, 0);

        Calendar roundedCal = DateUtils.round(c, Calendar.HOUR_OF_DAY);
        assertEquals(14, roundedCal.get(Calendar.HOUR_OF_DAY));

        Date roundedObj = DateUtils.round((Object) c, Calendar.HOUR_OF_DAY);
        assertNotNull(roundedObj);

        Date truncatedObj = DateUtils.truncate((Object) c.getTime(), Calendar.HOUR_OF_DAY);
        assertNotNull(truncatedObj);

        Calendar truncatedCal = DateUtils.truncate(c, Calendar.HOUR_OF_DAY);
        assertEquals(13, truncatedCal.get(Calendar.HOUR_OF_DAY));
    }

    // Tests truncate with various fields
    @Test
    public void testTruncate_variousFields_truncatesCorrectly() throws ParseException {
        Date d = dateFormat.parse("2004-07-16 13:45:30.500");

        assertEquals(dateFormat.parse("2004-07-16 13:45:30.000"), DateUtils.truncate(d, Calendar.SECOND));
        assertEquals(dateFormat.parse("2004-07-16 13:45:00.000"), DateUtils.truncate(d, Calendar.MINUTE));
        assertEquals(dateFormat.parse("2004-07-16 13:00:00.000"), DateUtils.truncate(d, Calendar.HOUR_OF_DAY));
        assertEquals(dateFormat.parse("2004-07-16 00:00:00.000"), DateUtils.truncate(d, Calendar.DATE));
        assertEquals(dateFormat.parse("2004-07-01 00:00:00.000"), DateUtils.truncate(d, Calendar.MONTH));
        assertEquals(dateFormat.parse("2004-01-01 00:00:00.000"), DateUtils.truncate(d, Calendar.YEAR));
        assertEquals(dateFormat.parse("2004-07-16 13:45:30.500"), DateUtils.truncate(d, Calendar.MILLISECOND));
    }

    // Tests round with unsupported field throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testRound_unsupportedField_throwsException() {
        DateUtils.round(new Date(), -9999);
    }

    // Tests truncate with huge year throws ArithmeticException
    @Test(expected = ArithmeticException.class)
    public void testTruncate_hugeYear_throwsArithmeticException() {
        Calendar hugeCal = Calendar.getInstance();
        hugeCal.set(Calendar.YEAR, 280000001);
        DateUtils.truncate(hugeCal, Calendar.MONTH);
    }

    // Tests iterator with different range styles
    @Test
    public void testIterator_rangeStyles_iteratesCorrectly() {
        Calendar focus = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        focus.set(2004, Calendar.JULY, 4);

        int[] styles = new int[]{
                DateUtils.RANGE_MONTH_SUNDAY,
                DateUtils.RANGE_MONTH_MONDAY,
                DateUtils.RANGE_WEEK_SUNDAY,
                DateUtils.RANGE_WEEK_MONDAY,
                DateUtils.RANGE_WEEK_RELATIVE,
                DateUtils.RANGE_WEEK_CENTER
        };

        for (int style : styles) {
            Iterator it = DateUtils.iterator(focus, style);
            assertTrue(it.hasNext());
            int count = 0;
            while (it.hasNext()) {
                Object next = it.next();
                assertTrue(next instanceof Calendar);
                count++;
            }
            assertTrue(count >= 7);
        }

        Iterator dateIt = DateUtils.iterator((Object) focus.getTime(), DateUtils.RANGE_WEEK_SUNDAY);
        assertTrue(dateIt.hasNext());
    }

    // Tests iterator with invalid range style throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testIterator_invalidRangeStyle_throwsException() {
        DateUtils.iterator(Calendar.getInstance(), -1);
    }

    // Tests iterator remove throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testIterator_remove_throwsUnsupportedOperationException() {
        Iterator it = DateUtils.iterator(Calendar.getInstance(), DateUtils.RANGE_WEEK_SUNDAY);
        it.remove();
    }

    // Tests iterator next past end throws NoSuchElementException
    @Test(expected = NoSuchElementException.class)
    public void testIterator_pastEnd_throwsNoSuchElementException() {
        Iterator it = DateUtils.iterator(Calendar.getInstance(), DateUtils.RANGE_WEEK_SUNDAY);
        while (it.hasNext()) {
            it.next();
        }
        it.next();
    }
}