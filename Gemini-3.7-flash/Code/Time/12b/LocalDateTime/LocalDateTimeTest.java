package org.joda.time;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import static org.junit.Assert.*;

public class LocalDateTimeTest {

    // Tests fromCalendarFields with BC era (Defects4J Time-12 regression)
    @Test
    public void testFromCalendarFields_beforeCommonEra_constructsCorrectYear() {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.clear();
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.YEAR, 5); // 5 BC -> year -4 in ISO
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 3);
        cal.set(Calendar.HOUR_OF_DAY, 4);
        cal.set(Calendar.MINUTE, 5);
        cal.set(Calendar.SECOND, 6);
        cal.set(Calendar.MILLISECOND, 7);

        LocalDateTime ldt = LocalDateTime.fromCalendarFields(cal);
        assertEquals(-4, ldt.getYear());
        assertEquals(2, ldt.getMonthOfYear());
        assertEquals(3, ldt.getDayOfMonth());
        assertEquals(4, ldt.getHourOfDay());
        assertEquals(5, ldt.getMinuteOfHour());
        assertEquals(6, ldt.getSecondOfMinute());
        assertEquals(7, ldt.getMillisOfSecond());
    }

    // Tests fromDateFields with BC era date (Defects4J Time-12 regression)
    @Test
    public void testFromDateFields_beforeCommonEra_constructsCorrectYear() {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.clear();
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.YEAR, 5); // 5 BC -> year -4
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 3);
        cal.set(Calendar.HOUR_OF_DAY, 4);
        cal.set(Calendar.MINUTE, 5);
        cal.set(Calendar.SECOND, 6);
        cal.set(Calendar.MILLISECOND, 7);

        Date date = cal.getTime();
        LocalDateTime ldt = LocalDateTime.fromDateFields(date);
        assertEquals(-4, ldt.getYear());
        assertEquals(2, ldt.getMonthOfYear());
        assertEquals(3, ldt.getDayOfMonth());
    }

    // Tests fromCalendarFields with normal AD calendar
    @Test
    public void testFromCalendarFields_normalAD_constructsCorrectly() {
        GregorianCalendar cal = new GregorianCalendar(2023, Calendar.MARCH, 15, 10, 20, 30);
        cal.set(Calendar.MILLISECOND, 123);

        LocalDateTime ldt = LocalDateTime.fromCalendarFields(cal);
        assertEquals(2023, ldt.getYear());
        assertEquals(3, ldt.getMonthOfYear());
        assertEquals(15, ldt.getDayOfMonth());
        assertEquals(10, ldt.getHourOfDay());
        assertEquals(20, ldt.getMinuteOfHour());
        assertEquals(30, ldt.getSecondOfMinute());
        assertEquals(123, ldt.getMillisOfSecond());
    }

    // Tests fromCalendarFields with null input throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testFromCalendarFields_nullInput_throwsIllegalArgumentException() {
        LocalDateTime.fromCalendarFields(null);
    }

    // Tests fromDateFields with null input throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testFromDateFields_nullInput_throwsIllegalArgumentException() {
        LocalDateTime.fromDateFields(null);
    }

    // Tests now() factory methods
    @Test
    public void testNow_withZoneAndChronology_returnsNonNull() {
        LocalDateTime now1 = LocalDateTime.now();
        assertNotNull(now1);

        LocalDateTime now2 = LocalDateTime.now(DateTimeZone.UTC);
        assertNotNull(now2);

        LocalDateTime now3 = LocalDateTime.now(ISOChronology.getInstanceUTC());
        assertNotNull(now3);
    }

    // Tests now(DateTimeZone) with null zone
    @Test(expected = NullPointerException.class)
    public void testNow_nullZone_throwsNullPointerException() {
        LocalDateTime.now((DateTimeZone) null);
    }

    // Tests parse methods
    @Test
    public void testParse_stringAndFormatter_returnsCorrectLocalDateTime() {
        LocalDateTime parsed = LocalDateTime.parse("2021-12-25T14:30:00.500");
        assertEquals(2021, parsed.getYear());
        assertEquals(12, parsed.getMonthOfYear());
        assertEquals(25, parsed.getDayOfMonth());
        assertEquals(14, parsed.getHourOfDay());
        assertEquals(30, parsed.getMinuteOfHour());
        assertEquals(0, parsed.getSecondOfMinute());
        assertEquals(500, parsed.getMillisOfSecond());

        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm");
        LocalDateTime parsedCustom = LocalDateTime.parse("25/12/2021 14:30", formatter);
        assertEquals(2021, parsedCustom.getYear());
        assertEquals(12, parsedCustom.getMonthOfYear());
        assertEquals(25, parsedCustom.getDayOfMonth());
        assertEquals(14, parsedCustom.getHourOfDay());
        assertEquals(30, parsedCustom.getMinuteOfHour());
    }

    // Tests constructors with various parameter counts
    @Test
    public void testConstructors_variousSignatures_initializeCorrectly() {
        LocalDateTime dt4 = new LocalDateTime(2020, 5, 10, 8, 30);
        assertEquals(2020, dt4.getYear());
        assertEquals(5, dt4.getMonthOfYear());
        assertEquals(10, dt4.getDayOfMonth());
        assertEquals(8, dt4.getHourOfDay());
        assertEquals(30, dt4.getMinuteOfHour());
        assertEquals(0, dt4.getSecondOfMinute());
        assertEquals(0, dt4.getMillisOfSecond());

        LocalDateTime dt6 = new LocalDateTime(2020, 5, 10, 8, 30, 45);
        assertEquals(45, dt6.getSecondOfMinute());

        LocalDateTime dt7 = new LocalDateTime(2020, 5, 10, 8, 30, 45, 250);
        assertEquals(250, dt7.getMillisOfSecond());

        LocalDateTime dtChrono = new LocalDateTime(2020, 5, 10, 8, 30, 45, 250, BuddhistChronology.getInstanceUTC());
        assertEquals(BuddhistChronology.getInstanceUTC(), dtChrono.getChronology());
    }

    // Tests size and getValue / getField indices
    @Test
    public void testSizeAndGetValue_allIndices_returnsExpectedValues() {
        LocalDateTime dt = new LocalDateTime(2022, 7, 19, 15, 45, 30, 100);
        assertEquals(4, dt.size());
        assertEquals(2022, dt.getValue(0));
        assertEquals(7, dt.getValue(1));
        assertEquals(19, dt.getValue(2));
        assertEquals(15 * 3600000 + 45 * 60000 + 30 * 1000 + 100, dt.getValue(3));
    }

    // Tests getValue with invalid index throws exception
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValue_invalidIndex_throwsIndexOutOfBoundsException() {
        LocalDateTime dt = new LocalDateTime(2022, 7, 19, 15, 45);
        dt.getValue(4);
    }

    // Tests isSupported and get with DateTimeFieldType
    @Test
    public void testGetAndIsSupported_fieldTypes_returnsExpectedResults() {
        LocalDateTime dt = new LocalDateTime(2022, 7, 19, 15, 45, 30, 100);
        assertTrue(dt.isSupported(DateTimeFieldType.year()));
        assertTrue(dt.isSupported(DateTimeFieldType.hourOfDay()));
        assertTrue(dt.isSupported(DurationFieldType.days()));
        assertFalse(dt.isSupported((DateTimeFieldType) null));
        assertFalse(dt.isSupported((DurationFieldType) null));

        assertEquals(2022, dt.get(DateTimeFieldType.year()));
        assertEquals(7, dt.get(DateTimeFieldType.monthOfYear()));
        assertEquals(19, dt.get(DateTimeFieldType.dayOfMonth()));
        assertEquals(15, dt.get(DateTimeFieldType.hourOfDay()));
        assertEquals(45, dt.get(DateTimeFieldType.minuteOfHour()));
        assertEquals(30, dt.get(DateTimeFieldType.secondOfMinute()));
        assertEquals(100, dt.get(DateTimeFieldType.millisOfSecond()));
        assertEquals(1, dt.getEra());
        assertEquals(20, dt.getCenturyOfEra());
        assertEquals(22, dt.getYearOfCentury());
        assertEquals(2022, dt.getYearOfEra());
    }

    // Tests plus and minus methods
    @Test
    public void testPlusMinus_variousFields_calculatesCorrectly() {
        LocalDateTime dt = new LocalDateTime(2020, 1, 15, 12, 0, 0, 0);

        assertEquals(new LocalDateTime(2022, 1, 15, 12, 0, 0, 0), dt.plusYears(2));
        assertEquals(new LocalDateTime(2018, 1, 15, 12, 0, 0, 0), dt.minusYears(2));
        assertEquals(dt, dt.plusYears(0));
        assertEquals(dt, dt.minusYears(0));

        assertEquals(new LocalDateTime(2020, 4, 15, 12, 0, 0, 0), dt.plusMonths(3));
        assertEquals(new LocalDateTime(2019, 10, 15, 12, 0, 0, 0), dt.minusMonths(3));
        assertEquals(dt, dt.plusMonths(0));
        assertEquals(dt, dt.minusMonths(0));

        assertEquals(new LocalDateTime(2020, 1, 29, 12, 0, 0, 0), dt.plusWeeks(2));
        assertEquals(new LocalDateTime(2020, 1, 1, 12, 0, 0, 0), dt.minusWeeks(2));
        assertEquals(dt, dt.plusWeeks(0));
        assertEquals(dt, dt.minusWeeks(0));

        assertEquals(new LocalDateTime(2020, 1, 20, 12, 0, 0, 0), dt.plusDays(5));
        assertEquals(new LocalDateTime(2020, 1, 10, 12, 0, 0, 0), dt.minusDays(5));
        assertEquals(dt, dt.plusDays(0));
        assertEquals(dt, dt.minusDays(0));

        assertEquals(new LocalDateTime(2020, 1, 15, 15, 0, 0, 0), dt.plusHours(3));
        assertEquals(new LocalDateTime(2020, 1, 15, 9, 0, 0, 0), dt.minusHours(3));

        assertEquals(new LocalDateTime(2020, 1, 15, 12, 45, 0, 0), dt.plusMinutes(45));
        assertEquals(new LocalDateTime(2020, 1, 15, 11, 15, 0, 0), dt.minusMinutes(45));

        assertEquals(new LocalDateTime(2020, 1, 15, 12, 0, 30, 0), dt.plusSeconds(30));
        assertEquals(new LocalDateTime(2020, 1, 15, 11, 59, 30, 0), dt.minusSeconds(30));

        assertEquals(new LocalDateTime(2020, 1, 15, 12, 0, 0, 500), dt.plusMillis(500));
        assertEquals(new LocalDateTime(2020, 1, 15, 11, 59, 59, 500), dt.minusMillis(500));
    }

    // Tests withDate and withTime methods
    @Test
    public void testWithDateAndWithTime_validInputs_returnsModifiedCopies() {
        LocalDateTime dt = new LocalDateTime(2020, 1, 15, 12, 30, 45, 500);

        LocalDateTime newDate = dt.withDate(2025, 6, 20);
        assertEquals(2025, newDate.getYear());
        assertEquals(6, newDate.getMonthOfYear());
        assertEquals(20, newDate.getDayOfMonth());
        assertEquals(12, newDate.getHourOfDay());

        LocalDateTime newTime = dt.withTime(8, 15, 20, 100);
        assertEquals(2020, newTime.getYear());
        assertEquals(8, newTime.getHourOfDay());
        assertEquals(15, newTime.getMinuteOfHour());
        assertEquals(20, newTime.getSecondOfMinute());
        assertEquals(100, newTime.getMillisOfSecond());
    }

    // Tests withField and withFields
    @Test
    public void testWithFieldAndWithFields_validInputs_returnsUpdatedInstances() {
        LocalDateTime dt = new LocalDateTime(2020, 1, 15, 12, 30, 45, 500);

        LocalDateTime updatedYear = dt.withField(DateTimeFieldType.year(), 2024);
        assertEquals(2024, updatedYear.getYear());

        LocalDateTime updatedWithAdded = dt.withFieldAdded(DurationFieldType.years(), 3);
        assertEquals(2023, updatedWithAdded.getYear());

        assertSame(dt, dt.withFieldAdded(DurationFieldType.years(), 0));
        assertSame(dt, dt.withFields(null));
    }

    // Tests conversions to DateTime, LocalDate, LocalTime, Date
    @Test
    public void testConversions_toDateTime_toLocalDate_toLocalTime_toDate() {
        LocalDateTime dt = new LocalDateTime(2020, 1, 15, 12, 30, 45, 500);

        DateTime dateTimeUtc = dt.toDateTime(DateTimeZone.UTC);
        assertEquals(2020, dateTimeUtc.getYear());
        assertEquals(DateTimeZone.UTC, dateTimeUtc.getZone());

        LocalDate localDate = dt.toLocalDate();
        assertEquals(2020, localDate.getYear());
        assertEquals(1, localDate.getMonthOfYear());
        assertEquals(15, localDate.getDayOfMonth());

        LocalTime localTime = dt.toLocalTime();
        assertEquals(12, localTime.getHourOfDay());
        assertEquals(30, localTime.getMinuteOfHour());
        assertEquals(45, localTime.getSecondOfMinute());
        assertEquals(500, localTime.getMillisOfSecond());

        Date jdkDate = dt.toDate();
        assertNotNull(jdkDate);
        LocalDateTime roundTrip = LocalDateTime.fromDateFields(jdkDate);
        assertEquals(dt.getYear(), roundTrip.getYear());
        assertEquals(dt.getMonthOfYear(), roundTrip.getMonthOfYear());
        assertEquals(dt.getDayOfMonth(), roundTrip.getDayOfMonth());
        assertEquals(dt.getHourOfDay(), roundTrip.getHourOfDay());
        assertEquals(dt.getMinuteOfHour(), roundTrip.getMinuteOfHour());
        assertEquals(dt.getSecondOfMinute(), roundTrip.getSecondOfMinute());
        assertEquals(dt.getMillisOfSecond(), roundTrip.getMillisOfSecond());
    }

    // Tests equals and compareTo
    @Test
    public void testEqualsAndCompareTo_variousObjects_returnsExpectedComparison() {
        LocalDateTime dt1 = new LocalDateTime(2020, 1, 15, 12, 0);
        LocalDateTime dt2 = new LocalDateTime(2020, 1, 15, 12, 0);
        LocalDateTime dt3 = new LocalDateTime(2020, 1, 15, 13, 0);

        assertTrue(dt1.equals(dt1));
        assertTrue(dt1.equals(dt2));
        assertFalse(dt1.equals(dt3));
        assertFalse(dt1.equals("not a localdatetime"));
        assertFalse(dt1.equals(null));

        assertEquals(0, dt1.compareTo(dt2));
        assertTrue(dt1.compareTo(dt3) < 0);
        assertTrue(dt3.compareTo(dt1) > 0);
    }

    // Tests toString with pattern and locale
    @Test
    public void testToString_patternAndLocale_formatsProperly() {
        LocalDateTime dt = new LocalDateTime(2020, 1, 15, 12, 30, 45, 500);
        assertEquals("2020-01-15T12:30:45.500", dt.toString());
        assertEquals("15/01/2020 12:30", dt.toString("dd/MM/yyyy HH:mm"));
        assertEquals("15/01/2020", dt.toString("dd/MM/yyyy", Locale.US));
        assertEquals(dt.toString(), dt.toString(null));
        assertEquals(dt.toString(), dt.toString(null, Locale.US));
    }

    // Tests property operations
    @Test
    public void testProperty_operations_modifyAndQueryCorrectly() {
        LocalDateTime dt = new LocalDateTime(2020, 1, 15, 12, 30, 45, 500);
        LocalDateTime.Property prop = dt.monthOfYear();

        assertEquals(1, prop.get());
        assertEquals("January", prop.getAsText(Locale.ENGLISH));
        assertEquals("Jan", prop.getAsShortText(Locale.ENGLISH));
        assertEquals(1, prop.getMinimumValue());
        assertEquals(12, prop.getMaximumValue());

        LocalDateTime modified = prop.setCopy(6);
        assertEquals(6, modified.getMonthOfYear());

        LocalDateTime added = prop.addToCopy(2);
        assertEquals(3, added.getMonthOfYear());

        LocalDateTime maxDay = dt.dayOfMonth().withMaximumValue();
        assertEquals(31, maxDay.getDayOfMonth()); // Jan has 31 days

        LocalDateTime minDay = dt.dayOfMonth().withMinimumValue();
        assertEquals(1, minDay.getDayOfMonth());

        LocalDateTime rounded = dt.hourOfDay().roundFloorCopy();
        assertEquals(0, rounded.getMinuteOfHour());
        assertEquals(0, rounded.getSecondOfMinute());
        assertEquals(0, rounded.getMillisOfSecond());
    }

    // Tests individual field modifier methods (withYear, withMonthOfYear, etc.)
    @Test
    public void testWithIndividualFields_validValues_returnsUpdatedInstances() {
        LocalDateTime dt = new LocalDateTime(2020, 1, 15, 12, 30, 45, 500);

        assertEquals(2021, dt.withYear(2021).getYear());
        assertEquals(10, dt.withMonthOfYear(10).getMonthOfYear());
        assertEquals(25, dt.withDayOfMonth(25).getDayOfMonth());
        assertEquals(18, dt.withHourOfDay(18).getHourOfDay());
        assertEquals(40, dt.withMinuteOfHour(40).getMinuteOfHour());
        assertEquals(50, dt.withSecondOfMinute(50).getSecondOfMinute());
        assertEquals(750, dt.withMillisOfSecond(750).getMillisOfSecond());
        assertEquals(5000, dt.withMillisOfDay(5000).getMillisOfDay());
        assertEquals(200, dt.withDayOfYear(200).getDayOfYear());
        assertEquals(3, dt.withDayOfWeek(3).getDayOfWeek());
        assertEquals(2021, dt.withWeekyear(2021).getWeekyear());
        assertEquals(10, dt.withWeekOfWeekyear(10).getWeekOfWeekyear());
    }
}