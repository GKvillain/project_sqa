package org.joda.time;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.CopticChronology;
import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import static org.junit.Assert.*;

public class LocalDateTest {

    // Tests fromCalendarFields with BC era (Defects4J Time-12 defect check)
    @Test
    public void testFromCalendarFields_bcEra_returnsCorrectLocalDate() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.clear();
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.YEAR, 1);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);
        
        LocalDate expected = new LocalDate(0, 1, 1);
        assertEquals(expected, LocalDate.fromCalendarFields(cal));
    }

    // Tests fromDateFields with BC era (Defects4J Time-12 defect check)
    @Test
    public void testFromDateFields_bcEra_returnsCorrectLocalDate() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.clear();
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.YEAR, 1);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);
        Date date = cal.getTime();

        LocalDate expected = new LocalDate(0, 1, 1);
        assertEquals(expected, LocalDate.fromDateFields(date));
    }

    // Tests normal case for fromCalendarFields
    @Test
    public void testFromCalendarFields_validCalendar_returnsCorrectLocalDate() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2020, Calendar.FEBRUARY, 29);
        
        LocalDate date = LocalDate.fromCalendarFields(cal);
        assertEquals(2020, date.getYear());
        assertEquals(2, date.getMonthOfYear());
        assertEquals(29, date.getDayOfMonth());
    }

    // Tests null calendar throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFromCalendarFields_nullCalendar_throwsException() {
        LocalDate.fromCalendarFields(null);
    }

    // Tests normal case for fromDateFields
    @Test
    public void testFromDateFields_validDate_returnsCorrectLocalDate() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2021, Calendar.DECEMBER, 25);
        Date date = cal.getTime();

        LocalDate localDate = LocalDate.fromDateFields(date);
        assertEquals(2021, localDate.getYear());
        assertEquals(12, localDate.getMonthOfYear());
        assertEquals(25, localDate.getDayOfMonth());
    }

    // Tests null date throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFromDateFields_nullDate_throwsException() {
        LocalDate.fromDateFields(null);
    }

    // Tests ISO string parsing
    @Test
    public void testParse_validIsoString_returnsParsedDate() {
        LocalDate date = LocalDate.parse("2023-11-15");
        assertEquals(2023, date.getYear());
        assertEquals(11, date.getMonthOfYear());
        assertEquals(15, date.getDayOfMonth());
    }

    // Tests size and getValue method with valid indices
    @Test
    public void testGetValue_validIndices_returnsFieldValues() {
        LocalDate date = new LocalDate(2022, 7, 20);
        assertEquals(3, date.size());
        assertEquals(2022, date.getValue(0));
        assertEquals(7, date.getValue(1));
        assertEquals(20, date.getValue(2));
    }

    // Tests getValue method with invalid index
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValue_invalidIndex_throwsException() {
        LocalDate date = new LocalDate(2022, 7, 20);
        date.getValue(3);
    }

    // Tests get and isSupported with supported and unsupported field types
    @Test
    public void testGet_supportedAndUnsupportedFieldTypes_returnsValueOrThrows() {
        LocalDate date = new LocalDate(2022, 5, 10);
        assertTrue(date.isSupported(DateTimeFieldType.dayOfMonth()));
        assertEquals(10, date.get(DateTimeFieldType.dayOfMonth()));
        assertFalse(date.isSupported(DateTimeFieldType.minuteOfDay()));

        try {
            date.get(DateTimeFieldType.minuteOfDay());
            fail("Expected IllegalArgumentException for unsupported field");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // Tests equals, hashCode, and compareTo branches
    @Test
    public void testEqualsAndCompareTo_variousScenarios_returnsExpectedResults() {
        LocalDate date1 = new LocalDate(2021, 5, 1);
        LocalDate date2 = new LocalDate(2021, 5, 1);
        LocalDate date3 = new LocalDate(2021, 5, 2);

        assertTrue(date1.equals(date1));
        assertTrue(date1.equals(date2));
        assertFalse(date1.equals(date3));
        assertFalse(date1.equals(null));
        assertFalse(date1.equals("2021-05-01"));

        assertEquals(date1.hashCode(), date2.hashCode());
        assertEquals(0, date1.compareTo(date2));
        assertTrue(date1.compareTo(date3) < 0);
        assertTrue(date3.compareTo(date1) > 0);
    }

    // Tests addition and subtraction of years, months, weeks, days
    @Test
    public void testPlusAndMinus_dateFields_modifiesDateCorrectly() {
        LocalDate date = new LocalDate(2020, 2, 29); // Leap year

        assertEquals(new LocalDate(2021, 2, 28), date.plusYears(1));
        assertEquals(new LocalDate(2019, 2, 28), date.minusYears(1));
        assertEquals(new LocalDate(2020, 3, 29), date.plusMonths(1));
        assertEquals(new LocalDate(2020, 1, 29), date.minusMonths(1));
        assertEquals(new LocalDate(2020, 3, 7), date.plusWeeks(1));
        assertEquals(new LocalDate(2020, 2, 22), date.minusWeeks(1));
        assertEquals(new LocalDate(2020, 3, 1), date.plusDays(1));
        assertEquals(new LocalDate(2020, 2, 28), date.minusDays(1));

        // Zero change returns same instance
        assertSame(date, date.plusYears(0));
        assertSame(date, date.minusYears(0));
    }

    // Tests withField and withFields functionality
    @Test
    public void testWithField_validFieldValues_returnsUpdatedInstance() {
        LocalDate date = new LocalDate(2021, 6, 15);
        LocalDate updated = date.withYear(2022).withMonthOfYear(8).withDayOfMonth(20);
        
        assertEquals(2022, updated.getYear());
        assertEquals(8, updated.getMonthOfYear());
        assertEquals(20, updated.getDayOfMonth());

        assertSame(date, date.withYear(2021));
        assertSame(date, date.withFields(null));
    }

    // Tests conversion to LocalDateTime
    @Test
    public void testToLocalDateTime_validLocalTime_combinesCorrectly() {
        LocalDate date = new LocalDate(2021, 4, 12);
        LocalTime time = new LocalTime(14, 30, 15);
        LocalDateTime ldt = date.toLocalDateTime(time);

        assertEquals(2021, ldt.getYear());
        assertEquals(4, ldt.getMonthOfYear());
        assertEquals(12, ldt.getDayOfMonth());
        assertEquals(14, ldt.getHourOfDay());
        assertEquals(30, ldt.getMinuteOfHour());
    }

    // Tests conversion to LocalDateTime with null input
    @Test(expected = IllegalArgumentException.class)
    public void testToLocalDateTime_nullLocalTime_throwsException() {
        LocalDate date = new LocalDate(2021, 4, 12);
        date.toLocalDateTime(null);
    }

    // Tests toDateTimeAtStartOfDay and toInterval
    @Test
    public void testToDateTimeAtStartOfDayAndToInterval_validDate_returnsCorrectValues() {
        LocalDate date = new LocalDate(2021, 5, 20);
        DateTime startOfDay = date.toDateTimeAtStartOfDay(DateTimeZone.UTC);
        
        assertEquals(2021, startOfDay.getYear());
        assertEquals(5, startOfDay.getMonthOfYear());
        assertEquals(20, startOfDay.getDayOfMonth());
        assertEquals(0, startOfDay.getHourOfDay());
        assertEquals(0, startOfDay.getMinuteOfHour());

        Interval interval = date.toInterval(DateTimeZone.UTC);
        assertEquals(startOfDay, interval.getStart());
        assertEquals(date.plusDays(1).toDateTimeAtStartOfDay(DateTimeZone.UTC), interval.getEnd());
    }

    // Tests toDate method
    @Test
    public void testToDate_standardDate_returnsMatchingUtilDate() {
        LocalDate date = new LocalDate(2021, 10, 5);
        Date utilDate = date.toDate();
        LocalDate roundTripped = LocalDate.fromDateFields(utilDate);

        assertEquals(date, roundTripped);
    }

    // Tests Property inner class operations
    @Test
    public void testProperty_dayOfMonth_supportsPropertyCalculations() {
        LocalDate date = new LocalDate(2020, 2, 10);
        LocalDate.Property prop = date.dayOfMonth();

        assertEquals(10, prop.get());
        assertEquals(29, prop.getMaximumValue());
        assertEquals(1, prop.getMinimumValue());
        assertEquals(new LocalDate(2020, 2, 29), prop.withMaximumValue());
        assertEquals(new LocalDate(2020, 2, 1), prop.withMinimumValue());
        assertEquals(new LocalDate(2020, 2, 15), prop.setCopy(15));
        assertEquals(new LocalDate(2020, 2, 12), prop.addToCopy(2));
    }

    // Tests toString formatting methods
    @Test
    public void testToString_variousPatterns_formatsAsExpected() {
        LocalDate date = new LocalDate(2021, 9, 8);
        assertEquals("2021-09-08", date.toString());
        assertEquals("08/09/2021", date.toString("dd/MM/yyyy"));
        assertEquals("2021-09-08", date.toString(null));
        assertEquals("08-Sep-2021", date.toString("dd-MMM-yyyy", Locale.ENGLISH));
    }

    // ==========================================
    // Additional Test Cases for Full Coverage
    // ==========================================

    @Test
    public void testConstructors_allVariants() {
        LocalDate nowDefault = new LocalDate();
        assertNotNull(nowDefault);

        LocalDate nowZone = new LocalDate(DateTimeZone.UTC);
        assertNotNull(nowZone);

        LocalDate nowNullZone = new LocalDate((DateTimeZone) null);
        assertNotNull(nowNullZone);

        LocalDate nowChrono = new LocalDate(ISOChronology.getInstanceUTC());
        assertNotNull(nowChrono);

        LocalDate nowNullChrono = new LocalDate((Chronology) null);
        assertNotNull(nowNullChrono);

        long instant = 1600000000000L;
        LocalDate fromInstant = new LocalDate(instant);
        assertEquals(fromInstant, new LocalDate(instant, (Chronology) null));
        assertEquals(fromInstant, new LocalDate(instant, (DateTimeZone) null));
        assertNotNull(new LocalDate(instant, ISOChronology.getInstanceUTC()));
        assertNotNull(new LocalDate(instant, DateTimeZone.UTC));

        LocalDate fromObj = new LocalDate("2020-05-15");
        assertEquals(2020, fromObj.getYear());
        assertEquals(5, fromObj.getMonthOfYear());
        assertEquals(15, fromObj.getDayOfMonth());

        LocalDate fromNullObj = new LocalDate((Object) null);
        assertNotNull(fromNullObj);

        LocalDate fromObjChrono = new LocalDate("2020-05-15", ISOChronology.getInstanceUTC());
        assertEquals(fromObj, fromObjChrono);

        LocalDate fromObjNullChrono = new LocalDate("2020-05-15", (Chronology) null);
        assertEquals(fromObj, fromObjNullChrono);

        LocalDate fromNullObjChrono = new LocalDate((Object) null, ISOChronology.getInstanceUTC());
        assertNotNull(fromNullObjChrono);

        LocalDate fromObjZone = new LocalDate("2020-05-15", DateTimeZone.UTC);
        assertEquals(fromObj, fromObjZone);

        LocalDate fromObjNullZone = new LocalDate("2020-05-15", (DateTimeZone) null);
        assertEquals(fromObj, fromObjNullZone);

        LocalDate fromNullObjZone = new LocalDate((Object) null, DateTimeZone.UTC);
        assertNotNull(fromNullObjZone);

        LocalDate ymdChrono = new LocalDate(2020, 5, 15, GregorianChronology.getInstanceUTC());
        assertEquals(GregorianChronology.getInstanceUTC(), ymdChrono.getChronology());

        LocalDate ymdNullChrono = new LocalDate(2020, 5, 15, null);
        assertEquals(ISOChronology.getInstanceUTC(), ymdNullChrono.getChronology());
    }

    @Test
    public void testStaticFactoryNowAndParse() {
        assertNotNull(LocalDate.now());
        assertNotNull(LocalDate.now(DateTimeZone.UTC));
        try {
            LocalDate.now((DateTimeZone) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        assertNotNull(LocalDate.now(ISOChronology.getInstanceUTC()));
        try {
            LocalDate.now((Chronology) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
        LocalDate parsed = LocalDate.parse("15/05/2020", formatter);
        assertEquals(new LocalDate(2020, 5, 15), parsed);
    }

    @Test
    public void testGetField_andFieldTypes() {
        LocalDate date = new LocalDate(2020, 5, 15);
        assertEquals(DateTimeFieldType.year(), date.getFieldType(0));
        assertEquals(DateTimeFieldType.monthOfYear(), date.getFieldType(1));
        assertEquals(DateTimeFieldType.dayOfMonth(), date.getFieldType(2));

        try {
            date.getFieldType(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            date.getFieldType(3);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        assertNotNull(date.getField(0));
        assertNotNull(date.getField(1));
        assertNotNull(date.getField(2));

        try {
            date.getField(3);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        assertTrue(date.isSupported(DurationFieldType.days()));
        assertTrue(date.isSupported(DurationFieldType.months()));
        assertTrue(date.isSupported(DurationFieldType.years()));
        assertFalse(date.isSupported(DurationFieldType.hours()));
        assertFalse(date.isSupported((DurationFieldType) null));
        assertFalse(date.isSupported((DateTimeFieldType) null));
    }

    @Test
    public void testGetters_allFields() {
        LocalDate date = new LocalDate(2020, 5, 15);
        assertEquals(2020, date.getYear());
        assertEquals(5, date.getMonthOfYear());
        assertEquals(15, date.getDayOfMonth());
        assertEquals(136, date.getDayOfYear());
        assertEquals(5, date.getDayOfWeek()); // Friday
        assertEquals(20, date.getWeekOfWeekyear());
        assertEquals(2020, date.getWeekyear());
        assertEquals(1, date.getEra());
        assertEquals(21, date.getCenturyOfEra());
        assertEquals(2020, date.getYearOfEra());
        assertEquals(20, date.getYearOfCentury());

        assertEquals(0, date.get(DateTimeFieldType.millisOfSecond()));
    }

    @Test
    public void testWithChronologyRetainFields() {
        LocalDate date = new LocalDate(2020, 5, 15, ISOChronology.getInstanceUTC());
        assertSame(date, date.withChronologyRetainFields(null));
        assertSame(date, date.withChronologyRetainFields(ISOChronology.getInstanceUTC()));

        LocalDate coptic = date.withChronologyRetainFields(CopticChronology.getInstanceUTC());
        assertEquals(CopticChronology.getInstanceUTC(), coptic.getChronology());
        assertEquals(2020, coptic.getYear());
        assertEquals(5, coptic.getMonthOfYear());
        assertEquals(15, coptic.getDayOfMonth());
    }

    @Test
    public void testWithField_andFieldAdded() {
        LocalDate date = new LocalDate(2020, 5, 15);

        try {
            date.withField(null, 1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            date.withField(DateTimeFieldType.hourOfDay(), 1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        LocalDate updated = date.withField(DateTimeFieldType.monthOfYear(), 6);
        assertEquals(6, updated.getMonthOfYear());
        assertSame(date, date.withField(DateTimeFieldType.monthOfYear(), 5));

        assertSame(date, date.withFieldAdded(DurationFieldType.days(), 0));
        assertSame(date, date.withFieldAdded(null, 1));
        try {
            date.withFieldAdded(DurationFieldType.hours(), 1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        LocalDate plusDays = date.withFieldAdded(DurationFieldType.days(), 5);
        assertEquals(20, plusDays.getDayOfMonth());

        assertSame(date, date.withPeriodAdded(null, 1));
        assertSame(date, date.withPeriodAdded(Period.days(1), 0));
        LocalDate plusPeriod = date.withPeriodAdded(Period.days(2), 3);
        assertEquals(21, plusPeriod.getDayOfMonth());

        assertEquals(new LocalDate(2020, 5, 18), date.plus(Period.days(3)));
        assertSame(date, date.plus((ReadablePeriod) null));
        assertEquals(new LocalDate(2020, 5, 12), date.minus(Period.days(3)));
        assertSame(date, date.minus((ReadablePeriod) null));

        assertSame(date, date.plusMonths(0));
        assertSame(date, date.minusMonths(0));
        assertSame(date, date.plusWeeks(0));
        assertSame(date, date.minusWeeks(0));
        assertSame(date, date.plusDays(0));
        assertSame(date, date.minusDays(0));

        assertSame(date, date.withMonthOfYear(5));
        assertSame(date, date.withDayOfMonth(15));
        assertEquals(new LocalDate(2020, 5, 100).getDayOfYear(), date.withDayOfYear(100).getDayOfYear());
        assertSame(date, date.withDayOfYear(136));
        assertEquals(1, date.withDayOfWeek(1).getDayOfWeek());
        assertSame(date, date.withDayOfWeek(5));
        assertEquals(10, date.withWeekOfWeekyear(10).getWeekOfWeekyear());
        assertSame(date, date.withWeekOfWeekyear(20));
        assertEquals(2021, date.withWeekyear(2021).getWeekyear());
        assertSame(date, date.withWeekyear(2020));
        assertEquals(1, date.withEra(1).getEra());
        assertSame(date, date.withEra(1));
        assertEquals(20, date.withCenturyOfEra(20).getCenturyOfEra());
        assertSame(date, date.withCenturyOfEra(21));
        assertEquals(2021, date.withYearOfEra(2021).getYearOfEra());
        assertSame(date, date.withYearOfEra(2020));
        assertEquals(30, date.withYearOfCentury(30).getYearOfCentury());
        assertSame(date, date.withYearOfCentury(20));
    }

    @Test
    public void testToDateTimeConversions() {
        LocalDate date = new LocalDate(2020, 5, 15);

        DateTime startOfDay = date.toDateTimeAtStartOfDay();
        assertEquals(date.toDateTimeAtStartOfDay(null), startOfDay);

        DateTime atMidnight = date.toDateTimeAtMidnight();
        assertEquals(date.toDateTimeAtMidnight(DateTimeZone.UTC), new DateTime(2020, 5, 15, 0, 0, 0, 0, DateTimeZone.UTC));
        assertEquals(date.toDateTimeAtMidnight(null), atMidnight);

        DateTime atCurrentTime = date.toDateTimeAtCurrentTime();
        assertEquals(2020, atCurrentTime.getYear());
        assertEquals(5, atCurrentTime.getMonthOfYear());
        assertEquals(15, atCurrentTime.getDayOfMonth());
        assertNotNull(date.toDateTimeAtCurrentTime(DateTimeZone.UTC));
        assertNotNull(date.toDateTimeAtCurrentTime(null));

        DateMidnight dateMidnight = date.toDateMidnight();
        assertEquals(date.toDateMidnight(DateTimeZone.UTC), new DateMidnight(2020, 5, 15, DateTimeZone.UTC));
        assertEquals(date.toDateMidnight(null), dateMidnight);

        LocalTime time = new LocalTime(10, 30);
        DateTime combined = date.toDateTime(time);
        assertEquals(10, combined.getHourOfDay());
        assertEquals(30, combined.getMinuteOfHour());
        assertEquals(date.toDateTime(null, DateTimeZone.UTC), date.toDateTimeAtCurrentTime(DateTimeZone.UTC));
        assertEquals(new DateTime(2020, 5, 15, 10, 30, 0, 0, DateTimeZone.UTC), date.toDateTime(time, DateTimeZone.UTC));

        Interval intervalDefault = date.toInterval();
        assertNotNull(intervalDefault);
        Interval intervalNull = date.toInterval(null);
        assertEquals(intervalDefault, intervalNull);
    }

    @Test
    public void testProperties_allFieldsAndMethods() {
        LocalDate date = new LocalDate(2020, 5, 15);

        assertNotNull(date.era());
        assertNotNull(date.centuryOfEra());
        assertNotNull(date.yearOfEra());
        assertNotNull(date.yearOfCentury());
        assertNotNull(date.weekyear());
        assertNotNull(date.monthOfYear());
        assertNotNull(date.weekOfWeekyear());
        assertNotNull(date.dayOfYear());
        assertNotNull(date.dayOfMonth());
        assertNotNull(date.dayOfWeek());
        assertNotNull(date.year());

        LocalDate.Property prop = date.monthOfYear();
        assertEquals(date, prop.getLocalDate());
        assertEquals(date.getChronology().monthOfYear(), prop.getField());
        assertEquals("May", prop.getAsText(Locale.ENGLISH));
        assertEquals("May", prop.getAsText());
        assertEquals("May", prop.getAsShortText(Locale.ENGLISH));
        assertEquals("May", prop.getAsShortText());

        LocalDate modified = prop.setCopy("June", Locale.ENGLISH);
        assertEquals(6, modified.getMonthOfYear());
        LocalDate modifiedDefault = prop.setCopy("June");
        assertEquals(6, modifiedDefault.getMonthOfYear());

        LocalDate addWrap = prop.addWrapFieldToCopy(10);
        assertEquals(3, addWrap.getMonthOfYear());

        LocalDate roundFloor = prop.roundFloorCopy();
        assertEquals(new LocalDate(2020, 5, 1), roundFloor);

        LocalDate roundCeiling = prop.roundCeilingCopy();
        assertEquals(new LocalDate(2020, 6, 1), roundCeiling);

        LocalDate roundHalfFloor = prop.roundHalfFloorCopy();
        assertNotNull(roundHalfFloor);

        LocalDate roundHalfCeiling = prop.roundHalfCeilingCopy();
        assertNotNull(roundHalfCeiling);

        LocalDate roundHalfEven = prop.roundHalfEvenCopy();
        assertNotNull(roundHalfEven);
    }

    @Test
    public void testSerialization() throws Exception {
        LocalDate date = new LocalDate(2020, 5, 15, ISOChronology.getInstanceUTC());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(date);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        LocalDate deserialized = (LocalDate) ois.readObject();
        ois.close();

        assertEquals(date, deserialized);
        assertEquals(date.getChronology(), deserialized.getChronology());
    }

    @Test
    public void testCompareTo_withPartialAndChronology() {
        LocalDate date1 = new LocalDate(2020, 5, 15);
        LocalDate date2 = new LocalDate(2020, 5, 15, BuddhistChronology.getInstanceUTC());
        assertEquals(0, date1.compareTo(date1));

        YearMonthDay ymd = new YearMonthDay(2020, 5, 15);
        assertEquals(0, date1.compareTo(ymd));

        YearMonthDay ymdAfter = new YearMonthDay(2020, 5, 16);
        assertTrue(date1.compareTo(ymdAfter) < 0);
    }
}