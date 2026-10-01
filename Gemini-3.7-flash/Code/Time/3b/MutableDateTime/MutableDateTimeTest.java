package org.joda.time;

import java.util.Locale;
import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.ISOChronology;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MutableDateTimeTest {

    private static final DateTimeZone LONDON = DateTimeZone.forID("Europe/London");
    private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");

    private MutableDateTime dt;

    @Before
    public void setUp() {
        dt = new MutableDateTime(2011, 10, 30, 2, 30, 0, 0, LONDON);
    }

    // Tests zero addition using add(DurationFieldType, int)
    @Test
    public void testAdd_DurationFieldTypeZero_noChange() {
        MutableDateTime mdt = new MutableDateTime(2011, 10, 30, 2, 30, 0, 0, LONDON);
        long expectedMillis = mdt.getMillis();
        mdt.add(DurationFieldType.years(), 0);
        assertEquals(expectedMillis, mdt.getMillis());
        mdt.add(DurationFieldType.months(), 0);
        assertEquals(expectedMillis, mdt.getMillis());
        mdt.add(DurationFieldType.days(), 0);
        assertEquals(expectedMillis, mdt.getMillis());
    }

    // Tests null DurationFieldType throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAdd_nullDurationFieldType_throwsException() {
        dt.add((DurationFieldType) null, 1);
    }

    // Tests zero addition for specific addXxx methods
    @Test
    public void testAddMethods_zeroAmount_noChange() {
        long expectedMillis = dt.getMillis();
        dt.addYears(0);
        assertEquals(expectedMillis, dt.getMillis());
        dt.addMonths(0);
        assertEquals(expectedMillis, dt.getMillis());
        dt.addWeeks(0);
        assertEquals(expectedMillis, dt.getMillis());
        dt.addDays(0);
        assertEquals(expectedMillis, dt.getMillis());
        dt.addHours(0);
        assertEquals(expectedMillis, dt.getMillis());
        dt.addMinutes(0);
        assertEquals(expectedMillis, dt.getMillis());
        dt.addSeconds(0);
        assertEquals(expectedMillis, dt.getMillis());
        dt.addMillis(0);
        assertEquals(expectedMillis, dt.getMillis());
    }

    // Tests non-zero additions for various date/time fields
    @Test
    public void testAddMethods_positiveValues_addsCorrectly() {
        MutableDateTime mdt = new MutableDateTime(2000, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        mdt.addYears(1);
        assertEquals(2001, mdt.getYear());
        mdt.addMonths(2);
        assertEquals(3, mdt.getMonthOfYear());
        mdt.addDays(3);
        assertEquals(4, mdt.getDayOfMonth());
        mdt.addHours(4);
        assertEquals(4, mdt.getHourOfDay());
        mdt.addMinutes(5);
        assertEquals(5, mdt.getMinuteOfHour());
        mdt.addSeconds(6);
        assertEquals(6, mdt.getSecondOfMinute());
        mdt.addMillis(7);
        assertEquals(7, mdt.getMillisOfSecond());
    }

    // Tests static now() and parse() methods
    @Test
    public void testNowAndParse_validInputs_success() {
        assertNotNull(MutableDateTime.now());
        assertNotNull(MutableDateTime.now(LONDON));
        assertNotNull(MutableDateTime.now(ISOChronology.getInstanceUTC()));

        MutableDateTime parsed = MutableDateTime.parse("2020-05-15T10:30:00.000Z");
        assertEquals(2020, parsed.getYear());
        assertEquals(5, parsed.getMonthOfYear());
        assertEquals(15, parsed.getDayOfMonth());
    }

    // Tests static now(DateTimeZone) with null zone
    @Test(expected = NullPointerException.class)
    public void testNow_nullZone_throwsException() {
        MutableDateTime.now((DateTimeZone) null);
    }

    // Tests static now(Chronology) with null chronology
    @Test(expected = NullPointerException.class)
    public void testNow_nullChronology_throwsException() {
        MutableDateTime.now((Chronology) null);
    }

    // Tests constructors with different parameters
    @Test
    public void testConstructors_variousInputs_initializedCorrectly() {
        MutableDateTime mdt1 = new MutableDateTime(1000L);
        assertEquals(1000L, mdt1.getMillis());

        MutableDateTime mdt2 = new MutableDateTime(1000L, PARIS);
        assertEquals(1000L, mdt2.getMillis());
        assertEquals(PARIS, mdt2.getZone());

        MutableDateTime mdt3 = new MutableDateTime(1000L, GJChronology.getInstanceUTC());
        assertEquals(GJChronology.getInstanceUTC(), mdt3.getChronology());

        MutableDateTime mdt4 = new MutableDateTime("2010-06-01T12:00:00.000Z", DateTimeZone.UTC);
        assertEquals(2010, mdt4.getYear());
        assertEquals(DateTimeZone.UTC, mdt4.getZone());
    }

    // Tests rounding modes
    @Test
    public void testRounding_variousModes_roundsProperly() {
        MutableDateTime mdt = new MutableDateTime(2000, 1, 1, 10, 20, 30, 500, DateTimeZone.UTC);
        DateTimeField minuteField = mdt.getChronology().minuteOfHour();

        mdt.setRounding(minuteField, MutableDateTime.ROUND_FLOOR);
        assertEquals(MutableDateTime.ROUND_FLOOR, mdt.getRoundingMode());
        assertEquals(minuteField, mdt.getRoundingField());
        assertEquals(0, mdt.getSecondOfMinute());
        assertEquals(0, mdt.getMillisOfSecond());

        mdt.setMillis(new DateTime(2000, 1, 1, 10, 20, 30, 0, DateTimeZone.UTC).getMillis());
        mdt.setRounding(minuteField, MutableDateTime.ROUND_CEILING);
        assertEquals(21, mdt.getMinuteOfHour());

        mdt.setRounding(null);
        assertEquals(MutableDateTime.ROUND_NONE, mdt.getRoundingMode());
        assertNull(mdt.getRoundingField());
    }

    // Tests invalid rounding mode throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetRounding_invalidMode_throwsException() {
        dt.setRounding(dt.getChronology().minuteOfHour(), 999);
    }

    // Tests setZone and setZoneRetainFields
    @Test
    public void testSetZoneAndRetainFields_validZone_correctMillis() {
        MutableDateTime mdt = new MutableDateTime(2000, 1, 1, 12, 0, 0, 0, DateTimeZone.UTC);
        long originalMillis = mdt.getMillis();
        mdt.setZone(PARIS);
        assertEquals(originalMillis, mdt.getMillis());
        assertEquals(PARIS, mdt.getZone());

        mdt.setZoneRetainFields(DateTimeZone.UTC);
        assertEquals(12, mdt.getHourOfDay());
        assertEquals(DateTimeZone.UTC, mdt.getZone());
    }

    // Tests setDate, setTime, and setDateTime methods
    @Test
    public void testSetDateAndSetTime_variousOverloads_updatedProperly() {
        MutableDateTime mdt = new MutableDateTime(2000, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);

        mdt.setDate(2005, 5, 20);
        assertEquals(2005, mdt.getYear());
        assertEquals(5, mdt.getMonthOfYear());
        assertEquals(20, mdt.getDayOfMonth());

        mdt.setTime(14, 30, 45, 250);
        assertEquals(14, mdt.getHourOfDay());
        assertEquals(30, mdt.getMinuteOfHour());
        assertEquals(45, mdt.getSecondOfMinute());
        assertEquals(250, mdt.getMillisOfSecond());

        mdt.setDateTime(2010, 8, 12, 9, 15, 20, 100);
        assertEquals(2010, mdt.getYear());
        assertEquals(8, mdt.getMonthOfYear());
        assertEquals(12, mdt.getDayOfMonth());
        assertEquals(9, mdt.getHourOfDay());
    }

    // Tests setter methods for individual fields
    @Test
    public void testIndividualFieldSets_validValues_updatedCorrectly() {
        MutableDateTime mdt = new MutableDateTime(2000, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);

        mdt.setYear(2008);
        assertEquals(2008, mdt.getYear());
        mdt.setWeekyear(2009);
        assertEquals(2009, mdt.getWeekyear());
        mdt.setMonthOfYear(6);
        assertEquals(6, mdt.getMonthOfYear());
        mdt.setWeekOfWeekyear(10);
        assertEquals(10, mdt.getWeekOfWeekyear());
        mdt.setDayOfYear(100);
        assertEquals(100, mdt.getDayOfYear());
        mdt.setDayOfMonth(15);
        assertEquals(15, mdt.getDayOfMonth());
        mdt.setDayOfWeek(3);
        assertEquals(3, mdt.getDayOfWeek());
        mdt.setHourOfDay(18);
        assertEquals(18, mdt.getHourOfDay());
        mdt.setMinuteOfDay(500);
        assertEquals(500, mdt.getMinuteOfDay());
        mdt.setMinuteOfHour(25);
        assertEquals(25, mdt.getMinuteOfHour());
        mdt.setSecondOfDay(3600);
        assertEquals(3600, mdt.getSecondOfDay());
        mdt.setSecondOfMinute(40);
        assertEquals(40, mdt.getSecondOfMinute());
        mdt.setMillisOfDay(12345);
        assertEquals(12345, mdt.getMillisOfDay());
        mdt.setMillisOfSecond(999);
        assertEquals(999, mdt.getMillisOfSecond());
    }

    // Tests add(ReadableDuration) and add(ReadablePeriod)
    @Test
    public void testAddReadableDurationAndPeriod_validInputs_success() {
        MutableDateTime mdt = new MutableDateTime(2000, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);

        mdt.add((ReadableDuration) null);
        assertEquals(2000, mdt.getYear());

        mdt.add(new Duration(1000L), 2);
        assertEquals(2000L, mdt.getMillis() - new DateTime(2000, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis());

        mdt.add((ReadablePeriod) null);
        mdt.add(Period.days(2), 3);
        assertEquals(7, mdt.getDayOfMonth());
    }

    // Tests property access and operations
    @Test
    public void testPropertyOperations_validManipulations_correctResults() {
        MutableDateTime mdt = new MutableDateTime(2000, 1, 1, 10, 20, 30, 40, DateTimeZone.UTC);
        MutableDateTime.Property yearProp = mdt.year();

        assertEquals(2000, yearProp.get());
        assertEquals(mdt, yearProp.getMutableDateTime());
        assertEquals(mdt.getChronology().year(), yearProp.getField());

        yearProp.set(2005);
        assertEquals(2005, mdt.getYear());

        yearProp.add(2);
        assertEquals(2007, mdt.getYear());

        yearProp.addWrapField(1);
        assertEquals(2008, mdt.getYear());

        mdt.monthOfYear().set("March", Locale.ENGLISH);
        assertEquals(3, mdt.getMonthOfYear());

        mdt.secondOfMinute().roundFloor();
        assertEquals(0, mdt.getMillisOfSecond());
    }

    // Tests null property field type throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testProperty_nullFieldType_throwsException() {
        dt.property(null);
    }

    // Tests clone, copy, and toString
    @Test
    public void testCloneCopyAndToString_validInstance_consistentState() {
        MutableDateTime copy = dt.copy();
        assertEquals(dt.getMillis(), copy.getMillis());
        assertEquals(dt.getChronology(), copy.getChronology());
        assertNotSame(dt, copy);

        Object clone = dt.clone();
        assertTrue(clone instanceof MutableDateTime);
        assertEquals(dt, clone);
        assertNotSame(dt, clone);

        assertNotNull(dt.toString());
    }
}