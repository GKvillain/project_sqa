package org.joda.time;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.*;

public class MutableDateTimeTest {

    private static final DateTimeZone LONDON = DateTimeZone.forID("Europe/London");
    private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");
    private static final DateTimeZone UTC = DateTimeZone.UTC;

    private MutableDateTime mutableDateTime;

    @Before
    public void setUp() {
        // 2004-06-09T10:20:30.040Z
        mutableDateTime = new MutableDateTime(2004, 6, 9, 10, 20, 30, 40, UTC);
    }

    // Tests static factory methods and constructors
    @Test
    public void testFactoryAndConstructors_validInputs_createdCorrectly() {
        assertNotNull(MutableDateTime.now());
        assertNotNull(MutableDateTime.now(LONDON));
        assertNotNull(MutableDateTime.now(org.joda.time.chrono.ISOChronology.getInstanceUTC()));

        MutableDateTime dtMillis = new MutableDateTime(1000L, LONDON);
        assertEquals(1000L, dtMillis.getMillis());
        assertEquals(LONDON, dtMillis.getZone());

        MutableDateTime dtFields = new MutableDateTime(2010, 5, 20, 14, 30, 15, 500, PARIS);
        assertEquals(2010, dtFields.getYear());
        assertEquals(5, dtFields.getMonthOfYear());
        assertEquals(20, dtFields.getDayOfMonth());
        assertEquals(14, dtFields.getHourOfDay());
        assertEquals(30, dtFields.getMinuteOfHour());
        assertEquals(15, dtFields.getSecondOfMinute());
        assertEquals(500, dtFields.getMillisOfSecond());
        assertEquals(PARIS, dtFields.getZone());
    }

    // Tests now() with null zone throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testNow_nullZone_throwsException() {
        MutableDateTime.now((DateTimeZone) null);
    }

    // Tests now() with null chronology throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testNow_nullChronology_throwsException() {
        MutableDateTime.now((Chronology) null);
    }

    // Tests parse method with valid string
    @Test
    public void testParse_validString_parsesSuccessfully() {
        MutableDateTime parsed = MutableDateTime.parse("2004-06-09T10:20:30.040Z");
        assertEquals(mutableDateTime.getMillis(), parsed.getMillis());
    }

    // Tests add method with zero and positive amounts during daylight savings (Defects4J Time-3)
    @Test
    public void testAdd_zeroAmount_doesNotChangeInstant() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        // 2011-10-30T01:30:00.000 BST (just before DST fallback)
        MutableDateTime dt = new MutableDateTime(2011, 10, 30, 1, 30, 0, 0, zone);
        long originalMillis = dt.getMillis();

        dt.add(DurationFieldType.years(), 0);
        assertEquals(originalMillis, dt.getMillis());

        dt.add(DurationFieldType.months(), 0);
        assertEquals(originalMillis, dt.getMillis());

        dt.add(DurationFieldType.weeks(), 0);
        assertEquals(originalMillis, dt.getMillis());

        dt.add(DurationFieldType.days(), 0);
        assertEquals(originalMillis, dt.getMillis());

        dt.add(DurationFieldType.hours(), 0);
        assertEquals(originalMillis, dt.getMillis());

        dt.add(DurationFieldType.minutes(), 0);
        assertEquals(originalMillis, dt.getMillis());

        dt.add(DurationFieldType.seconds(), 0);
        assertEquals(originalMillis, dt.getMillis());

        dt.add(DurationFieldType.millis(), 0);
        assertEquals(originalMillis, dt.getMillis());
    }

    // Tests field specific add methods with zero amount
    @Test
    public void testAddSpecificFields_zeroAmount_doesNotChangeInstant() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        MutableDateTime dt = new MutableDateTime(2011, 10, 30, 1, 30, 0, 0, zone);
        long originalMillis = dt.getMillis();

        dt.addYears(0);
        assertEquals(originalMillis, dt.getMillis());

        dt.addMonths(0);
        assertEquals(originalMillis, dt.getMillis());

        dt.addWeeks(0);
        assertEquals(originalMillis, dt.getMillis());

        dt.addDays(0);
        assertEquals(originalMillis, dt.getMillis());

        dt.addHours(0);
        assertEquals(originalMillis, dt.getMillis());

        dt.addMinutes(0);
        assertEquals(originalMillis, dt.getMillis());

        dt.addSeconds(0);
        assertEquals(originalMillis, dt.getMillis());

        dt.addMillis(0);
        assertEquals(originalMillis, dt.getMillis());
    }

    // Tests add with null DurationFieldType throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAdd_nullDurationFieldType_throwsException() {
        mutableDateTime.add((DurationFieldType) null, 1);
    }

    // Tests set with null DateTimeFieldType throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSet_nullDateTimeFieldType_throwsException() {
        mutableDateTime.set((DateTimeFieldType) null, 1);
    }

    // Tests individual field setters and adders
    @Test
    public void testFieldSettersAndAdders_validValues_updatesCorrectly() {
        mutableDateTime.setYear(2005);
        assertEquals(2005, mutableDateTime.getYear());

        mutableDateTime.addYears(2);
        assertEquals(2007, mutableDateTime.getYear());

        mutableDateTime.setMonthOfYear(12);
        assertEquals(12, mutableDateTime.getMonthOfYear());

        mutableDateTime.addMonths(2);
        assertEquals(2008, mutableDateTime.getYear());
        assertEquals(2, mutableDateTime.getMonthOfYear());

        mutableDateTime.setDayOfMonth(15);
        assertEquals(15, mutableDateTime.getDayOfMonth());

        mutableDateTime.addDays(10);
        assertEquals(25, mutableDateTime.getDayOfMonth());

        mutableDateTime.setHourOfDay(18);
        assertEquals(18, mutableDateTime.getHourOfDay());

        mutableDateTime.addHours(3);
        assertEquals(21, mutableDateTime.getHourOfDay());

        mutableDateTime.setMinuteOfHour(45);
        assertEquals(45, mutableDateTime.getMinuteOfHour());

        mutableDateTime.addMinutes(15);
        assertEquals(22, mutableDateTime.getHourOfDay());
        assertEquals(0, mutableDateTime.getMinuteOfHour());

        mutableDateTime.setSecondOfMinute(50);
        assertEquals(50, mutableDateTime.getSecondOfMinute());

        mutableDateTime.addSeconds(12);
        assertEquals(1, mutableDateTime.getMinuteOfHour());
        assertEquals(2, mutableDateTime.getSecondOfMinute());

        mutableDateTime.setMillisOfSecond(300);
        assertEquals(300, mutableDateTime.getMillisOfSecond());

        mutableDateTime.addMillis(800);
        assertEquals(3, mutableDateTime.getSecondOfMinute());
        assertEquals(100, mutableDateTime.getMillisOfSecond());
    }

    // Tests setDate, setTime and setDateTime methods
    @Test
    public void testSetDateAndSetTime_variousInputs_setsExpectedFields() {
        mutableDateTime.setDate(2015, 8, 20);
        assertEquals(2015, mutableDateTime.getYear());
        assertEquals(8, mutableDateTime.getMonthOfYear());
        assertEquals(20, mutableDateTime.getDayOfMonth());
        assertEquals(10, mutableDateTime.getHourOfDay());

        mutableDateTime.setTime(14, 25, 35, 450);
        assertEquals(2015, mutableDateTime.getYear());
        assertEquals(14, mutableDateTime.getHourOfDay());
        assertEquals(25, mutableDateTime.getMinuteOfHour());
        assertEquals(35, mutableDateTime.getSecondOfMinute());
        assertEquals(450, mutableDateTime.getMillisOfSecond());

        mutableDateTime.setDateTime(2020, 1, 2, 3, 4, 5, 6);
        assertEquals(2020, mutableDateTime.getYear());
        assertEquals(1, mutableDateTime.getMonthOfYear());
        assertEquals(2, mutableDateTime.getDayOfMonth());
        assertEquals(3, mutableDateTime.getHourOfDay());
        assertEquals(4, mutableDateTime.getMinuteOfHour());
        assertEquals(5, mutableDateTime.getSecondOfMinute());
        assertEquals(6, mutableDateTime.getMillisOfSecond());
    }

    // Tests add with ReadableDuration and ReadablePeriod
    @Test
    public void testAdd_readableDurationAndPeriod_updatesMillis() {
        Duration duration = new Duration(5000L);
        mutableDateTime.add(duration);
        assertEquals(35, mutableDateTime.getSecondOfMinute());

        mutableDateTime.add(duration, 2);
        assertEquals(45, mutableDateTime.getSecondOfMinute());

        mutableDateTime.add((ReadableDuration) null);
        assertEquals(45, mutableDateTime.getSecondOfMinute());

        Period period = Period.days(1);
        mutableDateTime.add(period);
        assertEquals(10, mutableDateTime.getDayOfMonth());

        mutableDateTime.add((ReadablePeriod) null);
        assertEquals(10, mutableDateTime.getDayOfMonth());
    }

    // Tests rounding modes and behavior
    @Test
    public void testRounding_allModes_roundsCorrectly() {
        DateTimeField minuteField = mutableDateTime.getChronology().minuteOfHour();

        // Round Floor
        mutableDateTime.setRounding(minuteField, MutableDateTime.ROUND_FLOOR);
        assertEquals(minuteField, mutableDateTime.getRoundingField());
        assertEquals(MutableDateTime.ROUND_FLOOR, mutableDateTime.getRoundingMode());
        assertEquals(0, mutableDateTime.getSecondOfMinute());
        assertEquals(0, mutableDateTime.getMillisOfSecond());

        // Round Ceiling
        mutableDateTime.setMillis(1000L * 60 + 1000); // 00:01:01
        mutableDateTime.setRounding(minuteField, MutableDateTime.ROUND_CEILING);
        assertEquals(2, mutableDateTime.getMinuteOfHour());

        // Round Half Floor
        mutableDateTime.setRounding(minuteField, MutableDateTime.ROUND_HALF_FLOOR);
        assertEquals(MutableDateTime.ROUND_HALF_FLOOR, mutableDateTime.getRoundingMode());

        // Round Half Ceiling
        mutableDateTime.setRounding(minuteField, MutableDateTime.ROUND_HALF_CEILING);
        assertEquals(MutableDateTime.ROUND_HALF_CEILING, mutableDateTime.getRoundingMode());

        // Round Half Even
        mutableDateTime.setRounding(minuteField, MutableDateTime.ROUND_HALF_EVEN);
        assertEquals(MutableDateTime.ROUND_HALF_EVEN, mutableDateTime.getRoundingMode());

        // Disable rounding
        mutableDateTime.setRounding(null);
        assertNull(mutableDateTime.getRoundingField());
        assertEquals(MutableDateTime.ROUND_NONE, mutableDateTime.getRoundingMode());
    }

    // Tests invalid rounding mode throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetRounding_invalidMode_throwsException() {
        DateTimeField minuteField = mutableDateTime.getChronology().minuteOfHour();
        mutableDateTime.setRounding(minuteField, 999);
    }

    // Tests setZone and setZoneRetainFields
    @Test
    public void testZoneTransitions_setZoneAndSetZoneRetainFields() {
        long originalMillis = mutableDateTime.getMillis();
        mutableDateTime.setZone(PARIS);
        assertEquals(originalMillis, mutableDateTime.getMillis());
        assertEquals(PARIS, mutableDateTime.getZone());

        mutableDateTime.setZoneRetainFields(UTC);
        assertEquals(UTC, mutableDateTime.getZone());
        assertEquals(12, mutableDateTime.getHourOfDay()); // Paris is UTC+2 in June, retaining gives 12 UTC
    }

    // Tests Property accessor and operations
    @Test
    public void testPropertyOperations_modifyAndRound() {
        MutableDateTime.Property yearProp = mutableDateTime.year();
        assertNotNull(yearProp.getField());
        assertEquals(mutableDateTime, yearProp.getMutableDateTime());

        yearProp.add(5);
        assertEquals(2009, mutableDateTime.getYear());

        yearProp.set(2012);
        assertEquals(2012, mutableDateTime.getYear());

        yearProp.set("2015", Locale.ENGLISH);
        assertEquals(2015, mutableDateTime.getYear());

        MutableDateTime.Property minuteProp = mutableDateTime.minuteOfHour();
        minuteProp.roundFloor();
        assertEquals(0, mutableDateTime.getSecondOfMinute());
        assertEquals(0, mutableDateTime.getMillisOfSecond());

        minuteProp.roundCeiling();
        minuteProp.roundHalfFloor();
        minuteProp.roundHalfCeiling();
        minuteProp.roundHalfEven();
        assertNotNull(minuteProp.getChronology());
    }

    // Tests property method with unsupported field throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testProperty_nullFieldType_throwsException() {
        mutableDateTime.property(null);
    }

    // Tests clone, copy and toString methods
    @Test
    public void testCloneCopyAndToString_validState_producesCorrectResults() {
        MutableDateTime copy = mutableDateTime.copy();
        assertEquals(mutableDateTime, copy);
        assertNotSame(mutableDateTime, copy);

        Object cloned = mutableDateTime.clone();
        assertEquals(mutableDateTime, cloned);
        assertNotSame(mutableDateTime, cloned);

        String str = mutableDateTime.toString();
        assertTrue(str.startsWith("2004-06-09T10:20:30.040"));
    }
}