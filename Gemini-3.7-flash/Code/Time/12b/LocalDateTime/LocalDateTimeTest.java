package org.joda.time;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class LocalDateTimeTest {

    private DateTimeZone originalZone;
    private TimeZone originalJvmZone;

    @Before
    public void setUp() {
        originalZone = DateTimeZone.getDefault();
        originalJvmZone = TimeZone.getDefault();
        DateTimeZone.setDefault(DateTimeZone.UTC);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(originalZone);
        TimeZone.setDefault(originalJvmZone);
    }

    // Tests fromCalendarFields with normal AD date
    @Test
    public void testFromCalendarFields_adDate_returnsCorrectLocalDateTime() {
        Calendar cal = new GregorianCalendar(2010, Calendar.FEBRUARY, 15, 12, 30, 45);
        cal.set(Calendar.MILLISECOND, 500);

        LocalDateTime ldt = LocalDateTime.fromCalendarFields(cal);

        assertEquals(2010, ldt.getYear());
        assertEquals(2, ldt.getMonthOfYear());
        assertEquals(15, ldt.getDayOfMonth());
        assertEquals(12, ldt.getHourOfDay());
        assertEquals(30, ldt.getMinuteOfHour());
        assertEquals(45, ldt.getSecondOfMinute());
        assertEquals(500, ldt.getMillisOfSecond());
    }

    // Tests fromCalendarFields with BC date (Defects4J Time-12 bug trigger)
    @Test
    public void testFromCalendarFields_bcDate_returnsCorrectLocalDateTime() {
        Calendar cal = new GregorianCalendar(2010, Calendar.FEBRUARY, 15, 12, 30, 45);
        cal.set(Calendar.MILLISECOND, 500);
        cal.set(Calendar.ERA, GregorianCalendar.BC);

        LocalDateTime ldt = LocalDateTime.fromCalendarFields(cal);

        assertEquals(-2009, ldt.getYear());
        assertEquals(2, ldt.getMonthOfYear());
        assertEquals(15, ldt.getDayOfMonth());
        assertEquals(12, ldt.getHourOfDay());
        assertEquals(30, ldt.getMinuteOfHour());
        assertEquals(45, ldt.getSecondOfMinute());
        assertEquals(500, ldt.getMillisOfSecond());
    }

    // Tests fromCalendarFields with null input throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testFromCalendarFields_nullCalendar_throwsIllegalArgumentException() {
        LocalDateTime.fromCalendarFields(null);
    }

    // Tests fromDateFields with normal date
    @Test
    public void testFromDateFields_adDate_returnsCorrectLocalDateTime() {
        Calendar cal = new GregorianCalendar(2012, Calendar.AUGUST, 20, 14, 15, 30);
        cal.set(Calendar.MILLISECOND, 123);
        Date date = cal.getTime();

        LocalDateTime ldt = LocalDateTime.fromDateFields(date);

        assertEquals(2012, ldt.getYear());
        assertEquals(8, ldt.getMonthOfYear());
        assertEquals(20, ldt.getDayOfMonth());
        assertEquals(14, ldt.getHourOfDay());
        assertEquals(15, ldt.getMinuteOfHour());
        assertEquals(30, ldt.getSecondOfMinute());
        assertEquals(123, ldt.getMillisOfSecond());
    }

    // Tests fromDateFields with BC date (Defects4J Time-12 bug trigger)
    @Test
    public void testFromDateFields_bcDate_returnsCorrectLocalDateTime() {
        Calendar cal = new GregorianCalendar(2010, Calendar.FEBRUARY, 15, 12, 30, 45);
        cal.set(Calendar.MILLISECOND, 500);
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        Date date = cal.getTime();

        LocalDateTime ldt = LocalDateTime.fromDateFields(date);

        assertEquals(-2009, ldt.getYear());
        assertEquals(2, ldt.getMonthOfYear());
        assertEquals(15, ldt.getDayOfMonth());
        assertEquals(12, ldt.getHourOfDay());
        assertEquals(30, ldt.getMinuteOfHour());
        assertEquals(45, ldt.getSecondOfMinute());
        assertEquals(500, ldt.getMillisOfSecond());
    }

    // Tests fromDateFields with null input throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testFromDateFields_nullDate_throwsIllegalArgumentException() {
        LocalDateTime.fromDateFields(null);
    }

    // Tests now() factory methods and null validations
    @Test(expected = NullPointerException.class)
    public void testNow_nullDateTimeZone_throwsNullPointerException() {
        LocalDateTime.now((DateTimeZone) null);
    }

    // Tests now(Chronology) with null parameter
    @Test(expected = NullPointerException.class)
    public void testNow_nullChronology_throwsNullPointerException() {
        LocalDateTime.now((Chronology) null);
    }

    // Tests parse(String) with standard ISO format
    @Test
    public void testParse_isoString_parsesSuccessfully() {
        LocalDateTime ldt = LocalDateTime.parse("2021-11-25T15:45:30.123");

        assertEquals(2021, ldt.getYear());
        assertEquals(11, ldt.getMonthOfYear());
        assertEquals(25, ldt.getDayOfMonth());
        assertEquals(15, ldt.getHourOfDay());
        assertEquals(45, ldt.getMinuteOfHour());
        assertEquals(30, ldt.getSecondOfMinute());
        assertEquals(123, ldt.getMillisOfSecond());
    }

    // Tests constructors with various field values
    @Test
    public void testConstructor_fieldValues_createsInstanceCorrectly() {
        LocalDateTime ldt = new LocalDateTime(2023, 5, 10, 8, 30, 15, 200);

        assertEquals(2023, ldt.getYear());
        assertEquals(5, ldt.getMonthOfYear());
        assertEquals(10, ldt.getDayOfMonth());
        assertEquals(8, ldt.getHourOfDay());
        assertEquals(30, ldt.getMinuteOfHour());
        assertEquals(15, ldt.getSecondOfMinute());
        assertEquals(200, ldt.getMillisOfSecond());
        assertEquals(4, ldt.size());
    }

    // Tests size and getValue boundary/indexing
    @Test
    public void testGetValue_validAndInvalidIndex() {
        LocalDateTime ldt = new LocalDateTime(2023, 5, 10, 8, 30, 15, 200);

        assertEquals(2023, ldt.getValue(0));
        assertEquals(5, ldt.getValue(1));
        assertEquals(10, ldt.getValue(2));
        assertEquals(8 * 3600000 + 30 * 60000 + 15 * 1000 + 200, ldt.getValue(3));
    }

    // Tests getValue out of bounds throws IndexOutOfBoundsException
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValue_indexOutOfBounds_throwsIndexOutOfBoundsException() {
        LocalDateTime ldt = new LocalDateTime(2023, 5, 10, 8, 30);
        ldt.getValue(4);
    }

    // Tests get(DateTimeFieldType) and isSupported
    @Test
    public void testGetAndIsSupported_validAndNullFields() {
        LocalDateTime ldt = new LocalDateTime(2023, 5, 10, 8, 30, 15, 200);

        assertEquals(2023, ldt.get(DateTimeFieldType.year()));
        assertEquals(5, ldt.get(DateTimeFieldType.monthOfYear()));
        assertEquals(10, ldt.get(DateTimeFieldType.dayOfMonth()));
        assertTrue(ldt.isSupported(DateTimeFieldType.hourOfDay()));
        assertFalse(ldt.isSupported((DateTimeFieldType) null));
        assertTrue(ldt.isSupported(DurationFieldType.days()));
        assertFalse(ldt.isSupported((DurationFieldType) null));
    }

    // Tests get with null DateTimeFieldType throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testGet_nullFieldType_throwsIllegalArgumentException() {
        LocalDateTime ldt = new LocalDateTime(2023, 5, 10, 8, 30);
        ldt.get(null);
    }

    // Tests equals and compareTo
    @Test
    public void testEqualsAndCompareTo_sameAndDifferentInstants() {
        LocalDateTime ldt1 = new LocalDateTime(2023, 5, 10, 8, 30, 0, 0);
        LocalDateTime ldt2 = new LocalDateTime(2023, 5, 10, 8, 30, 0, 0);
        LocalDateTime ldt3 = new LocalDateTime(2023, 5, 10, 8, 30, 0, 1);

        assertTrue(ldt1.equals(ldt1));
        assertTrue(ldt1.equals(ldt2));
        assertFalse(ldt1.equals(ldt3));
        assertFalse(ldt1.equals("NotALocalDateTime"));

        assertEquals(0, ldt1.compareTo(ldt2));
        assertTrue(ldt1.compareTo(ldt3) < 0);
        assertTrue(ldt3.compareTo(ldt1) > 0);
    }

    // Tests plus and minus methods for date and time fields
    @Test
    public void testPlusMinusFields_validOperations() {
        LocalDateTime ldt = new LocalDateTime(2023, 5, 10, 8, 30, 15, 200);

        assertSame(ldt, ldt.plusYears(0));
        assertEquals(2025, ldt.plusYears(2).getYear());
        assertEquals(2021, ldt.minusYears(2).getYear());

        assertSame(ldt, ldt.plusMonths(0));
        assertEquals(7, ldt.plusMonths(2).getMonthOfYear());
        assertEquals(3, ldt.minusMonths(2).getMonthOfYear());

        assertSame(ldt, ldt.plusWeeks(0));
        assertEquals(24, ldt.plusWeeks(2).getDayOfMonth());
        assertEquals(26, ldt.minusWeeks(2).getDayOfMonth());

        assertSame(ldt, ldt.plusDays(0));
        assertEquals(15, ldt.plusDays(5).getDayOfMonth());
        assertEquals(5, ldt.minusDays(5).getDayOfMonth());

        assertSame(ldt, ldt.plusHours(0));
        assertEquals(11, ldt.plusHours(3).getHourOfDay());
        assertEquals(5, ldt.minusHours(3).getHourOfDay());

        assertSame(ldt, ldt.plusMinutes(0));
        assertEquals(40, ldt.plusMinutes(10).getMinuteOfHour());
        assertEquals(20, ldt.minusMinutes(10).getMinuteOfHour());

        assertSame(ldt, ldt.plusSeconds(0));
        assertEquals(35, ldt.plusSeconds(20).getSecondOfMinute());
        assertEquals(5, ldt.minusSeconds(10).getSecondOfMinute());

        assertSame(ldt, ldt.plusMillis(0));
        assertEquals(500, ldt.plusMillis(300).getMillisOfSecond());
        assertEquals(100, ldt.minusMillis(100).getMillisOfSecond());
    }

    // Tests withDate and withTime
    @Test
    public void testWithDateAndWithTime_updatesValues() {
        LocalDateTime ldt = new LocalDateTime(2023, 5, 10, 8, 30, 15, 200);

        LocalDateTime updatedDate = ldt.withDate(2020, 1, 1);
        assertEquals(2020, updatedDate.getYear());
        assertEquals(1, updatedDate.getMonthOfYear());
        assertEquals(1, updatedDate.getDayOfMonth());
        assertEquals(8, updatedDate.getHourOfDay());

        LocalDateTime updatedTime = ldt.withTime(12, 0, 0, 0);
        assertEquals(2023, updatedTime.getYear());
        assertEquals(12, updatedTime.getHourOfDay());
        assertEquals(0, updatedTime.getMinuteOfHour());
        assertEquals(0, updatedTime.getSecondOfMinute());
        assertEquals(0, updatedTime.getMillisOfSecond());
    }

    // Tests withField and withFieldAdded
    @Test
    public void testWithFieldAndWithFieldAdded() {
        LocalDateTime ldt = new LocalDateTime(2023, 5, 10, 8, 30);

        LocalDateTime modified = ldt.withField(DateTimeFieldType.monthOfYear(), 12);
        assertEquals(12, modified.getMonthOfYear());

        LocalDateTime added = ldt.withFieldAdded(DurationFieldType.days(), 3);
        assertEquals(13, added.getDayOfMonth());

        assertSame(ldt, ldt.withFieldAdded(DurationFieldType.days(), 0));
    }

    // Tests conversions to LocalDate, LocalTime, DateTime, and Date
    @Test
    public void testConversions_returnsExpectedObjects() {
        LocalDateTime ldt = new LocalDateTime(2023, 5, 10, 8, 30, 15, 200);

        LocalDate localDate = ldt.toLocalDate();
        assertEquals(2023, localDate.getYear());
        assertEquals(5, localDate.getMonthOfYear());
        assertEquals(10, localDate.getDayOfMonth());

        LocalTime localTime = ldt.toLocalTime();
        assertEquals(8, localTime.getHourOfDay());
        assertEquals(30, localTime.getMinuteOfHour());
        assertEquals(15, localTime.getSecondOfMinute());
        assertEquals(200, localTime.getMillisOfSecond());

        DateTime dt = ldt.toDateTime(DateTimeZone.UTC);
        assertEquals(ldt.getYear(), dt.getYear());
        assertEquals(ldt.getMillisOfDay(), dt.getMillisOfDay());

        Date d = ldt.toDate();
        assertNotNull(d);
        LocalDateTime roundTrip = LocalDateTime.fromDateFields(d);
        assertEquals(ldt, roundTrip);
    }

    // Tests toString formats
    @Test
    public void testToString_formattedOutput() {
        LocalDateTime ldt = new LocalDateTime(2023, 5, 10, 8, 30, 15, 200);

        assertEquals("2023-05-10T08:30:15.200", ldt.toString());
        assertEquals("2023/05/10 08:30", ldt.toString("yyyy/MM/dd HH:mm"));
        assertEquals("2023-05-10T08:30:15.200", ldt.toString((String) null));
        assertEquals("2023-05-10T08:30:15.200", ldt.toString(null, Locale.ENGLISH));
    }

    // Tests Property class methods
    @Test
    public void testProperty_operationsAndRounding() {
        LocalDateTime ldt = new LocalDateTime(2023, 5, 10, 8, 30, 15, 200);

        LocalDateTime.Property prop = ldt.dayOfMonth();
        assertEquals(10, prop.get());
        assertEquals(ldt, prop.getLocalDateTime());

        LocalDateTime maxDay = prop.withMaximumValue();
        assertEquals(31, maxDay.getDayOfMonth());

        LocalDateTime minDay = prop.withMinimumValue();
        assertEquals(1, minDay.getDayOfMonth());

        LocalDateTime addedCopy = prop.addToCopy(5);
        assertEquals(15, addedCopy.getDayOfMonth());

        LocalDateTime setCopy = prop.setCopy(20);
        assertEquals(20, setCopy.getDayOfMonth());

        LocalDateTime floorHour = ldt.hourOfDay().roundFloorCopy();
        assertEquals(0, floorHour.getMinuteOfHour());
        assertEquals(0, floorHour.getSecondOfMinute());
        assertEquals(0, floorHour.getMillisOfSecond());
    }
}