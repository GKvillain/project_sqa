package org.joda.time.chrono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

import org.joda.time.Chronology;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeZone;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.Instant;
import org.joda.time.YearMonthDay;
import org.junit.Test;

import static org.junit.Assert.*;

public class GJChronologyTest {

    // Tests Julian leap day before cutover (Defects4J Time-18 bug test)
    @Test
    public void testGetDateTimeMillis_julianLeapYearBeforeCutover_succeeds() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long millis = chrono.getDateTimeMillis(1500, 2, 29, 0);
        assertEquals(1500, chrono.year().get(millis));
        assertEquals(2, chrono.monthOfYear().get(millis));
        assertEquals(29, chrono.dayOfMonth().get(millis));
    }

    // Tests Julian leap day with time fields before cutover
    @Test
    public void testGetDateTimeMillis_julianLeapYearWithTime_succeeds() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long millis = chrono.getDateTimeMillis(1500, 2, 29, 12, 30, 40, 500);
        assertEquals(1500, chrono.year().get(millis));
        assertEquals(2, chrono.monthOfYear().get(millis));
        assertEquals(29, chrono.dayOfMonth().get(millis));
        assertEquals(12, chrono.hourOfDay().get(millis));
        assertEquals(30, chrono.minuteOfHour().get(millis));
        assertEquals(40, chrono.secondOfMinute().get(millis));
        assertEquals(500, chrono.millisOfSecond().get(millis));
    }

    // Tests date falling into the illegal cutover gap (1582-10-05 to 1582-10-14)
    @Test(expected = IllegalArgumentException.class)
    public void testGetDateTimeMillis_dateInCutoverGap_throwsException() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        chrono.getDateTimeMillis(1582, 10, 5, 0);
    }

    // Tests date falling into the illegal cutover gap with time components
    @Test(expected = IllegalArgumentException.class)
    public void testGetDateTimeMillis_dateInCutoverGapWithTime_throwsException() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        chrono.getDateTimeMillis(1582, 10, 10, 12, 0, 0, 0);
    }

    // Tests factory methods with default and custom parameters
    @Test
    public void testFactory_getInstanceVariants_returnsValidInstances() {
        GJChronology utc = GJChronology.getInstanceUTC();
        assertNotNull(utc);
        assertEquals(DateTimeZone.UTC, utc.getZone());
        assertEquals(GJChronology.DEFAULT_CUTOVER, utc.getGregorianCutover());
        assertEquals(4, utc.getMinimumDaysInFirstWeek());

        GJChronology def = GJChronology.getInstance();
        assertNotNull(def);
        assertEquals(DateTimeZone.getDefault(), def.getZone());

        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        GJChronology parisChrono = GJChronology.getInstance(paris);
        assertEquals(paris, parisChrono.getZone());

        Instant customCutover = new Instant(0L);
        GJChronology custom = GJChronology.getInstance(paris, customCutover, 3);
        assertEquals(customCutover, custom.getGregorianCutover());
        assertEquals(3, custom.getMinimumDaysInFirstWeek());

        GJChronology customFromMillis = GJChronology.getInstance(paris, 0L, 3);
        assertSame(custom, customFromMillis);

        GJChronology nullCutover = GJChronology.getInstance(paris, (org.joda.time.ReadableInstant) null);
        assertEquals(GJChronology.DEFAULT_CUTOVER, nullCutover.getGregorianCutover());
    }

    // Tests zone conversion methods withUTC and withZone
    @Test
    public void testWithZone_and_withUTC_returnsCorrectInstances() {
        GJChronology utc = GJChronology.getInstanceUTC();
        assertSame(utc, utc.withUTC());
        assertSame(utc, utc.withZone(DateTimeZone.UTC));

        DateTimeZone tokyo = DateTimeZone.forID("Asia/Tokyo");
        Chronology tokyoChrono = utc.withZone(tokyo);
        assertEquals(tokyo, tokyoChrono.getZone());

        Chronology defaultZoneChrono = utc.withZone(null);
        assertEquals(DateTimeZone.getDefault(), defaultZoneChrono.getZone());
    }

    // Tests equals, hashCode, and toString formatting
    @Test
    public void testEqualsHashCodeAndToString_variousConfigs_behavesCorrectly() {
        GJChronology utc1 = GJChronology.getInstanceUTC();
        GJChronology utc2 = GJChronology.getInstance(DateTimeZone.UTC, GJChronology.DEFAULT_CUTOVER, 4);
        GJChronology tokyo = GJChronology.getInstance(DateTimeZone.forID("Asia/Tokyo"));

        assertEquals(utc1, utc2);
        assertEquals(utc1.hashCode(), utc2.hashCode());
        assertFalse(utc1.equals(tokyo));
        assertFalse(utc1.equals(null));
        assertFalse(utc1.equals("NotAChronology"));

        String utcStr = utc1.toString();
        assertTrue(utcStr.contains("GJChronology[UTC]"));

        Instant customCutover = new Instant(0L);
        GJChronology custom = GJChronology.getInstance(DateTimeZone.UTC, customCutover, 2);
        String customStr = custom.toString();
        assertTrue(customStr.contains("cutover="));
        assertTrue(customStr.contains("mdfw=2"));
    }

    // Tests dayOfMonth field behavior across cutover boundary
    @Test
    public void testDayOfMonth_getAndSetAcrossCutover_handledAccurately() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        DateTimeField dayField = chrono.dayOfMonth();

        // 1582-10-04 (Julian) followed by 1582-10-15 (Gregorian)
        long oct4 = chrono.getDateTimeMillis(1582, 10, 4, 0);
        long oct15 = chrono.getDateTimeMillis(1582, 10, 15, 0);

        assertEquals(4, dayField.get(oct4));
        assertEquals(15, dayField.get(oct15));

        long oct16 = dayField.add(oct15, 1);
        assertEquals(16, dayField.get(oct16));

        // Setting Julian day to transition forward into Gregorian
        long setFromOct4 = dayField.set(oct4, 15);
        assertEquals(15, dayField.get(setFromOct4));
        assertEquals(1582, chrono.year().get(setFromOct4));
        assertEquals(10, chrono.monthOfYear().get(setFromOct4));
    }

    // Tests monthOfYear addition and differences across cutover
    @Test
    public void testMonthOfYear_addAndDifferenceAcrossCutover_calculatesCorrectly() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        DateTimeField monthField = chrono.monthOfYear();

        long instant = chrono.getDateTimeMillis(1582, 1, 1, 0);
        long plus12Months = monthField.add(instant, 12);
        assertEquals(1583, chrono.year().get(plus12Months));
        assertEquals(1, chrono.monthOfYear().get(plus12Months));

        assertEquals(12, monthField.getDifference(plus12Months, instant));
        assertEquals(-12, monthField.getDifference(instant, plus12Months));
        assertEquals(12L, monthField.getDifferenceAsLong(plus12Months, instant));
    }

    // Tests year field add, get, and text representations
    @Test
    public void testYearField_textAndMathAcrossCutover_returnsValidResults() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        DateTimeField yearField = chrono.year();

        long instant = chrono.getDateTimeMillis(1580, 5, 10, 0);
        assertEquals("1580", yearField.getAsText(instant, Locale.ENGLISH));
        assertEquals("1580", yearField.getAsShortText(instant, Locale.ENGLISH));
        assertFalse(yearField.isLenient());

        long plus10Years = yearField.add(instant, 10L);
        assertEquals(1590, yearField.get(plus10Years));
        assertEquals(10, yearField.getDifference(plus10Years, instant));
        assertEquals(-10L, yearField.getDifferenceAsLong(instant, plus10Years));

        assertEquals(yearField.getMinimumValue(), yearField.getMinimumValue(instant));
        assertEquals(yearField.getMaximumValue(), yearField.getMaximumValue(instant));
    }

    // Tests partial addition on CutoverField
    @Test
    public void testCutoverField_addPartial_preservesDateMath() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        YearMonthDay ymd = new YearMonthDay(2004, 2, 29, chrono);
        YearMonthDay nextYear = ymd.plusYears(1);

        assertEquals(2005, nextYear.getYear());
        assertEquals(2, nextYear.getMonthOfYear());
        assertEquals(28, nextYear.getDayOfMonth());

        YearMonthDay sameYear = ymd.plusYears(0);
        assertEquals(ymd, sameYear);
    }

    // Tests leap status and leap amounts in Julian vs Gregorian periods
    @Test
    public void testLeapFields_julianAndGregorianEras_evaluatesCorrectly() {
        GJChronology chrono = GJChronology.getInstanceUTC();

        // 1500 is leap in Julian, but not in Gregorian
        long julian1500 = chrono.getDateTimeMillis(1500, 2, 28, 0);
        assertTrue(chrono.year().isLeap(julian1500));
        assertEquals(1, chrono.year().getLeapAmount(julian1500));
        assertNotNull(chrono.year().getLeapDurationField());

        // 1700 is not leap in Gregorian
        long gregorian1700 = chrono.getDateTimeMillis(1700, 2, 28, 0);
        assertFalse(chrono.year().isLeap(gregorian1700));
        assertEquals(0, chrono.year().getLeapAmount(gregorian1700));

        // 2000 is leap in Gregorian
        long gregorian2000 = chrono.getDateTimeMillis(2000, 2, 28, 0);
        assertTrue(chrono.year().isLeap(gregorian2000));
    }

    // Tests cutover rounding methods floor and ceiling
    @Test
    public void testRounding_floorAndCeiling_roundsProperly() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long instant = chrono.getDateTimeMillis(1582, 10, 15, 12, 30, 0, 0);

        long floorDay = chrono.dayOfMonth().roundFloor(instant);
        assertEquals(chrono.getDateTimeMillis(1582, 10, 15, 0), floorDay);

        long ceilingDay = chrono.dayOfMonth().roundCeiling(instant);
        assertEquals(chrono.getDateTimeMillis(1582, 10, 16, 0), ceilingDay);

        long beforeCutover = chrono.getDateTimeMillis(1582, 10, 4, 12, 0, 0, 0);
        long floorBefore = chrono.dayOfMonth().roundFloor(beforeCutover);
        assertEquals(chrono.getDateTimeMillis(1582, 10, 4, 0), floorBefore);
    }

    // Tests serialization and deserialization singleton resolution
    @Test
    public void testSerialization_readResolve_restoresEquivalentInstance() throws Exception {
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.forOffsetHours(2));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(chrono);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        GJChronology result = (GJChronology) ois.readObject();
        ois.close();

        assertSame(chrono, result);
    }

    // Tests non-midnight cutover instant configuring sub-day cutover fields
    @Test
    public void testNonMidnightCutover_subDayFieldsConfigured_constructsSuccessfully() {
        // Cutover at 06:00:00 UTC
        Instant cutover = new Instant(-12219292800000L + 6 * 3600 * 1000L);
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC, cutover, 4);

        assertNotNull(chrono.millisOfDay());
        assertNotNull(chrono.hourOfDay());
        assertNotNull(chrono.minuteOfHour());
        assertNotNull(chrono.secondOfMinute());
        assertEquals(cutover, chrono.getGregorianCutover());
    }
}