package org.joda.time;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.ISOChronology;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LocalDateTest {

    // Tests fromCalendarFields with BC era (Defects4J Time-12 defect detection)
    @Test
    public void testFromCalendarFields_BCEra_returnsNegativeYear() {
        Calendar cal = new GregorianCalendar();
        cal.clear();
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.YEAR, 5);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 10);

        LocalDate result = LocalDate.fromCalendarFields(cal);
        assertEquals(-4, result.getYear());
        assertEquals(2, result.getMonthOfYear());
        assertEquals(10, result.getDayOfMonth());
    }

    // Tests fromDateFields with BC year (Defects4J Time-12 defect detection)
    @Test
    public void testFromDateFields_BCDate_returnsNegativeYear() {
        Calendar cal = new GregorianCalendar();
        cal.clear();
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.YEAR, 5);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 10);
        Date date = cal.getTime();

        LocalDate result = LocalDate.fromDateFields(date);
        assertEquals(-4, result.getYear());
        assertEquals(2, result.getMonthOfYear());
        assertEquals(10, result.getDayOfMonth());
    }

    // Tests fromCalendarFields with AD date
    @Test
    public void testFromCalendarFields_ADDate_returnsCorrectLocalDate() {
        Calendar cal = new GregorianCalendar(2023, Calendar.MARCH, 15);
        LocalDate result = LocalDate.fromCalendarFields(cal);
        assertEquals(2023, result.getYear());
        assertEquals(3, result.getMonthOfYear());
        assertEquals(15, result.getDayOfMonth());
    }

    // Tests fromCalendarFields null argument exception
    @Test(expected = IllegalArgumentException.class)
    public void testFromCalendarFields_nullInput_throwsException() {
        LocalDate.fromCalendarFields(null);
    }

    // Tests fromDateFields null argument exception
    @Test(expected = IllegalArgumentException.class)
    public void testFromDateFields_nullInput_throwsException() {
        LocalDate.fromDateFields(null);
    }

    // Tests static factory now() and now(DateTimeZone)
    @Test
    public void testNow_variousParameters_returnsValidLocalDate() {
        LocalDate nowDefault = LocalDate.now();
        assertNotNull(nowDefault);

        LocalDate nowZone = LocalDate.now(DateTimeZone.UTC);
        assertNotNull(nowZone);

        LocalDate nowChrono = LocalDate.now(ISOChronology.getInstanceUTC());
        assertNotNull(nowChrono);
    }

    // Tests parse methods with default and custom format
    @Test
    public void testParse_validString_returnsParsedLocalDate() {
        LocalDate parsed = LocalDate.parse("2020-05-20");
        assertEquals(2020, parsed.getYear());
        assertEquals(5, parsed.getMonthOfYear());
        assertEquals(20, parsed.getDayOfMonth());
    }

    // Tests constructors with different parameters
    @Test
    public void testConstructors_validInputs_initializeFieldsCorrectly() {
        LocalDate date1 = new LocalDate(2021, 12, 25);
        assertEquals(2021, date1.getYear());
        assertEquals(12, date1.getMonthOfYear());
        assertEquals(25, date1.getDayOfMonth());
        assertEquals(ISOChronology.getInstanceUTC(), date1.getChronology());

        LocalDate date2 = new LocalDate(2021, 12, 25, GregorianChronology.getInstanceUTC());
        assertEquals(GregorianChronology.getInstanceUTC(), date2.getChronology());

        LocalDate date3 = new LocalDate("2021-12-25");
        assertEquals(date1, date3);

        LocalDate date4 = new LocalDate(date1.getLocalMillis(), (Chronology) null);
        assertEquals(date1.getLocalMillis(), date4.getLocalMillis());
    }

    // Tests size, getField, and getValue indexed access
    @Test
    public void testIndexedAccess_sizeAndValues_returnsExpected() {
        LocalDate date = new LocalDate(2022, 6, 18);
        assertEquals(3, date.size());
        assertEquals(2022, date.getValue(0));
        assertEquals(6, date.getValue(1));
        assertEquals(18, date.getValue(2));
    }

    // Tests get and isSupported for supported and unsupported field types
    @Test
    public void testGetAndIsSupported_variousFields_returnsCorrectValues() {
        LocalDate date = new LocalDate(2022, 6, 18);

        assertTrue(date.isSupported(DateTimeFieldType.year()));
        assertTrue(date.isSupported(DateTimeFieldType.monthOfYear()));
        assertTrue(date.isSupported(DateTimeFieldType.dayOfMonth()));
        assertTrue(date.isSupported(DateTimeFieldType.dayOfWeek()));
        assertFalse(date.isSupported(DateTimeFieldType.hourOfDay()));

        assertTrue(date.isSupported(DurationFieldType.days()));
        assertTrue(date.isSupported(DurationFieldType.months()));
        assertFalse(date.isSupported(DurationFieldType.hours()));

        assertEquals(2022, date.get(DateTimeFieldType.year()));
        assertEquals(6, date.get(DateTimeFieldType.monthOfYear()));
        assertEquals(18, date.get(DateTimeFieldType.dayOfMonth()));
        assertEquals(2022, date.getYear());
        assertEquals(6, date.getMonthOfYear());
        assertEquals(18, date.getDayOfMonth());
        assertEquals(6, date.getDayOfWeek());
        assertEquals(169, date.getDayOfYear());
        assertEquals(1, date.getEra());
        assertEquals(20, date.getCenturyOfEra());
        assertEquals(22, date.getYearOfCentury());
    }

    // Tests exception when get unsupported field
    @Test(expected = IllegalArgumentException.class)
    public void testGet_unsupportedField_throwsException() {
        LocalDate date = new LocalDate(2022, 6, 18);
        date.get(DateTimeFieldType.hourOfDay());
    }

    // Tests date arithmetic with plus and minus methods
    @Test
    public void testArithmetic_plusAndMinusMethods_returnsCorrectDates() {
        LocalDate base = new LocalDate(2020, 2, 28);

        assertEquals(new LocalDate(2021, 2, 28), base.plusYears(1));
        assertEquals(new LocalDate(2020, 2, 28), base.plusYears(0));
        assertEquals(new LocalDate(2019, 2, 28), base.minusYears(1));

        assertEquals(new LocalDate(2020, 3, 28), base.plusMonths(1));
        assertEquals(new LocalDate(2020, 1, 28), base.minusMonths(1));

        assertEquals(new LocalDate(2020, 3, 6), base.plusWeeks(1));
        assertEquals(new LocalDate(2020, 2, 21), base.minusWeeks(1));

        assertEquals(new LocalDate(2020, 2, 29), base.plusDays(1));
        assertEquals(new LocalDate(2020, 2, 27), base.minusDays(1));

        assertEquals(new LocalDate(2020, 3, 29), base.plus(Period.months(1).withDays(1)));
        assertEquals(new LocalDate(2020, 1, 27), base.minus(Period.months(1).withDays(1)));
    }

    // Tests withField, withFieldAdded, withFields modifications
    @Test
    public void testWithMethods_fieldModifications_returnsUpdatedLocalDate() {
        LocalDate date = new LocalDate(2021, 5, 10);

        assertEquals(new LocalDate(2025, 5, 10), date.withYear(2025));
        assertEquals(new LocalDate(2021, 11, 10), date.withMonthOfYear(11));
        assertEquals(new LocalDate(2021, 5, 25), date.withDayOfMonth(25));
        assertEquals(new LocalDate(2021, 5, 10), date.withFields(null));
        assertEquals(new LocalDate(2021, 5, 10), date.withFieldAdded(DurationFieldType.years(), 0));
        assertEquals(new LocalDate(2023, 5, 10), date.withFieldAdded(DurationFieldType.years(), 2));
    }

    // Tests conversion to DateTime, LocalDateTime, Interval, and Date
    @Test
    public void testConversions_toOtherTypes_returnsExpectedObjects() {
        LocalDate date = new LocalDate(2021, 7, 15);

        DateTime startOfDay = date.toDateTimeAtStartOfDay(DateTimeZone.UTC);
        assertEquals(new DateTime(2021, 7, 15, 0, 0, 0, 0, DateTimeZone.UTC), startOfDay);

        DateTime atMidnight = date.toDateTimeAtMidnight(DateTimeZone.UTC);
        assertEquals(new DateTime(2021, 7, 15, 0, 0, 0, 0, DateTimeZone.UTC), atMidnight);

        LocalDateTime ldt = date.toLocalDateTime(new LocalTime(14, 30, 0));
        assertEquals(new LocalDateTime(2021, 7, 15, 14, 30, 0), ldt);

        Interval interval = date.toInterval(DateTimeZone.UTC);
        assertEquals(new DateTime(2021, 7, 15, 0, 0, 0, 0, DateTimeZone.UTC), interval.getStart());
        assertEquals(new DateTime(2021, 7, 16, 0, 0, 0, 0, DateTimeZone.UTC), interval.getEnd());

        Date d = date.toDate();
        assertNotNull(d);
    }

    // Tests toLocalDateTime with mismatched chronology throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testToLocalDateTime_mismatchedChronology_throwsException() {
        LocalDate date = new LocalDate(2021, 7, 15, ISOChronology.getInstanceUTC());
        LocalTime time = new LocalTime(10, 0, 0, 0, BuddhistChronology.getInstanceUTC());
        date.toLocalDateTime(time);
    }

    // Tests equals, hashCode, and compareTo methods
    @Test
    public void testEqualsAndHashCodeAndCompareTo_comparison_behavesCorrectly() {
        LocalDate date1 = new LocalDate(2021, 4, 1);
        LocalDate date2 = new LocalDate(2021, 4, 1);
        LocalDate date3 = new LocalDate(2021, 4, 2);
        LocalDate date4 = new LocalDate(2020, 4, 1);

        assertTrue(date1.equals(date1));
        assertTrue(date1.equals(date2));
        assertFalse(date1.equals(date3));
        assertFalse(date1.equals("not a date"));
        assertFalse(date1.equals(null));

        assertEquals(date1.hashCode(), date2.hashCode());

        assertEquals(0, date1.compareTo(date2));
        assertTrue(date1.compareTo(date3) < 0);
        assertTrue(date1.compareTo(date4) > 0);
    }

    // Tests toString with and without pattern
    @Test
    public void testToString_formatsCorrectly() {
        LocalDate date = new LocalDate(2021, 9, 5);
        assertEquals("2021-09-05", date.toString());
        assertEquals("05/09/2021", date.toString("dd/MM/yyyy"));
        assertEquals("2021-09-05", date.toString(null, Locale.ENGLISH));
    }

    // Tests Property inner class manipulation and queries
    @Test
    public void testProperty_methods_workCorrectly() {
        LocalDate date = new LocalDate(2020, 2, 15);
        LocalDate.Property monthProp = date.monthOfYear();

        assertNotNull(monthProp.getField());
        assertEquals(date, monthProp.getLocalDate());
        assertEquals(2, monthProp.get());
        assertEquals("February", monthProp.getAsText(Locale.ENGLISH));
        assertEquals("Feb", monthProp.getAsShortText(Locale.ENGLISH));

        LocalDate plusTwoMonths = monthProp.addToCopy(2);
        assertEquals(4, plusTwoMonths.getMonthOfYear());

        LocalDate wrapped = monthProp.addWrapFieldToCopy(11);
        assertEquals(1, wrapped.getMonthOfYear());

        LocalDate setMonth = monthProp.setCopy(8);
        assertEquals(8, setMonth.getMonthOfYear());

        LocalDate maxDay = date.dayOfMonth().withMaximumValue();
        assertEquals(29, maxDay.getDayOfMonth());

        LocalDate minDay = date.dayOfMonth().withMinimumValue();
        assertEquals(1, minDay.getDayOfMonth());

        LocalDate roundedFloor = date.monthOfYear().roundFloorCopy();
        assertEquals(new LocalDate(2020, 2, 1), roundedFloor);
    }
}