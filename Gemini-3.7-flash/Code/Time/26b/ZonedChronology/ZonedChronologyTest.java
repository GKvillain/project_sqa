package org.joda.time.chrono;

import java.util.Locale;
import org.joda.time.Chronology;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.Partial;
import org.joda.time.field.UnsupportedDurationField;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ZonedChronologyTest {

    private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");
    private static final DateTimeZone LONDON = DateTimeZone.forID("Europe/London");
    private static final DateTimeZone NEW_YORK = DateTimeZone.forID("America/New_York");
    private static final DateTimeZone UTC = DateTimeZone.UTC;

    private DateTimeZone originalDefaultZone;

    @Before
    public void setUp() {
        originalDefaultZone = DateTimeZone.getDefault();
        DateTimeZone.setDefault(LONDON);
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(originalDefaultZone);
    }

    // Tests getInstance with null base chronology throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetInstance_nullBase_throwsIllegalArgumentException() {
        ZonedChronology.getInstance(null, PARIS);
    }

    // Tests getInstance with null zone throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetInstance_nullZone_throwsIllegalArgumentException() {
        ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), null);
    }

    // Tests getInstance with valid parameters
    @Test
    public void testGetInstance_validChronologyAndZone_returnsInstance() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        assertNotNull(chrono);
        assertEquals(PARIS, chrono.getZone());
        assertEquals(ISOChronology.getInstanceUTC(), chrono.withUTC());
    }

    // Tests withZone returns same instance for current zone, base for UTC, new instance for other zone
    @Test
    public void testWithZone_variousZones_returnsExpectedChronology() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        assertSame(chrono, chrono.withZone(PARIS));
        assertSame(ISOChronology.getInstanceUTC(), chrono.withZone(UTC));

        Chronology nyChrono = chrono.withZone(NEW_YORK);
        assertTrue(nyChrono instanceof ZonedChronology);
        assertEquals(NEW_YORK, nyChrono.getZone());

        Chronology defaultChrono = chrono.withZone(null);
        assertEquals(LONDON, defaultChrono.getZone());
    }

    // Tests getDateTimeMillis with 4 arguments (year, month, day, millisOfDay)
    @Test
    public void testGetDateTimeMillis_fourArgs_returnsCorrectMillis() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        // 2007-06-01 is in CEST (UTC+2)
        long millis = chrono.getDateTimeMillis(2007, 6, 1, 3600000);
        long expected = ISOChronology.getInstanceUTC().getDateTimeMillis(2007, 6, 1, 1, 0, 0, 0) - 2 * DateTimeConstants.MILLIS_PER_HOUR;
        assertEquals(expected, millis);
    }

    // Tests getDateTimeMillis with 7 arguments (year, month, day, hour, min, sec, millis)
    @Test
    public void testGetDateTimeMillis_sevenArgs_returnsCorrectMillis() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        // 2007-01-01 is in CET (UTC+1)
        long millis = chrono.getDateTimeMillis(2007, 1, 1, 12, 30, 45, 500);
        long expectedUtc = ISOChronology.getInstanceUTC().getDateTimeMillis(2007, 1, 1, 11, 30, 45, 500);
        assertEquals(expectedUtc, millis);
    }

    // Tests getDateTimeMillis with instant and time fields
    @Test
    public void testGetDateTimeMillis_instantAndTimeArgs_returnsCorrectMillis() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        long baseInstant = ISOChronology.getInstanceUTC().getDateTimeMillis(2007, 1, 1, 0, 0, 0, 0);
        long result = chrono.getDateTimeMillis(baseInstant, 14, 0, 0, 0);
        long expected = ISOChronology.getInstanceUTC().getDateTimeMillis(2007, 1, 1, 13, 0, 0, 0);
        assertEquals(expected, result);
    }

    // Tests getDateTimeMillis during spring forward DST gap throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetDateTimeMillis_gapTransition_throwsIllegalArgumentException() {
        // America/New_York gap: 2007-03-11 02:00:00 to 03:00:00 does not exist
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), NEW_YORK);
        chrono.getDateTimeMillis(2007, 3, 11, 2, 30, 0, 0);
    }

    // Tests equals, hashCode and toString methods
    @Test
    public void testEqualsAndHashCodeAndToString() {
        ZonedChronology chrono1 = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        ZonedChronology chrono2 = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        ZonedChronology chrono3 = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), LONDON);

        assertEquals(chrono1, chrono1);
        assertEquals(chrono1, chrono2);
        assertNotEquals(chrono1, chrono3);
        assertNotEquals(chrono1, null);
        assertNotEquals(chrono1, "StringObject");

        assertEquals(chrono1.hashCode(), chrono2.hashCode());
        assertEquals("ZonedChronology[ISOChronology[UTC], Europe/Paris]", chrono1.toString());
    }

    // Tests useTimeArithmetic helper method
    @Test
    public void testUseTimeArithmetic() {
        Chronology iso = ISOChronology.getInstanceUTC();
        assertTrue(ZonedChronology.useTimeArithmetic(iso.hours()));
        assertTrue(ZonedChronology.useTimeArithmetic(iso.minutes()));
        assertTrue(ZonedChronology.useTimeArithmetic(iso.seconds()));
        assertTrue(ZonedChronology.useTimeArithmetic(iso.millis()));
        assertFalse(ZonedChronology.useTimeArithmetic(iso.days()));
        assertFalse(ZonedChronology.useTimeArithmetic(iso.months()));
        assertFalse(ZonedChronology.useTimeArithmetic(iso.years()));
        assertFalse(ZonedChronology.useTimeArithmetic((DurationField) null));
    }

    // Tests ZonedDateTimeField get, getAsText, getAsShortText
    @Test
    public void testZonedDateTimeField_getAndTextMethods() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        DateTimeField hourField = chrono.hourOfDay();
        long instant = ISOChronology.getInstanceUTC().getDateTimeMillis(2007, 1, 1, 10, 0, 0, 0); // 11:00 in Paris

        assertEquals(11, hourField.get(instant));
        assertEquals("11", hourField.getAsText(instant, Locale.ENGLISH));
        assertEquals("11", hourField.getAsShortText(instant, Locale.ENGLISH));
        assertEquals("5", hourField.getAsText(5, Locale.ENGLISH));
        assertEquals("5", hourField.getAsShortText(5, Locale.ENGLISH));
        assertFalse(hourField.isLenient());
    }

    // Tests ZonedDateTimeField add and addWrapField for time fields
    @Test
    public void testZonedDateTimeField_addTimeField() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        DateTimeField hourField = chrono.hourOfDay();
        long instant = ISOChronology.getInstanceUTC().getDateTimeMillis(2007, 1, 1, 10, 0, 0, 0); // 11:00 Paris

        long added = hourField.add(instant, 3);
        assertEquals(14, hourField.get(added));

        long addedLong = hourField.add(instant, 3L);
        assertEquals(14, hourField.get(addedLong));

        long wrapped = hourField.addWrapField(instant, 15);
        assertEquals(2, hourField.get(wrapped));
    }

    // Tests ZonedDateTimeField add and addWrapField for date fields
    @Test
    public void testZonedDateTimeField_addDateField() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        DateTimeField dayField = chrono.dayOfMonth();
        long instant = ISOChronology.getInstanceUTC().getDateTimeMillis(2007, 1, 1, 10, 0, 0, 0);

        long added = dayField.add(instant, 5);
        assertEquals(6, dayField.get(added));

        long addedLong = dayField.add(instant, 10L);
        assertEquals(11, dayField.get(addedLong));

        long wrapped = dayField.addWrapField(instant, 31);
        assertEquals(1, dayField.get(wrapped));
    }

    // Tests ZonedDateTimeField set method with valid int and text
    @Test
    public void testZonedDateTimeField_setValidValues() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        DateTimeField hourField = chrono.hourOfDay();
        long instant = ISOChronology.getInstanceUTC().getDateTimeMillis(2007, 1, 1, 10, 0, 0, 0);

        long result = hourField.set(instant, 20);
        assertEquals(20, hourField.get(result));

        long resultText = hourField.set(instant, "22", Locale.ENGLISH);
        assertEquals(22, hourField.get(resultText));
    }

    // Tests ZonedDateTimeField set during DST gap throws IllegalFieldValueException
    @Test(expected = IllegalFieldValueException.class)
    public void testZonedDateTimeField_setInDstGap_throwsIllegalFieldValueException() {
        // America/New_York gap: 2007-03-11 02:00:00 to 03:00:00 does not exist
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), NEW_YORK);
        long instant = ISOChronology.getInstanceUTC().getDateTimeMillis(2007, 3, 11, 0, 30, 0, 0);
        chrono.hourOfDay().set(instant, 2);
    }

    // Tests ZonedDateTimeField roundFloor, roundCeiling and remainder for time and date fields
    @Test
    public void testZonedDateTimeField_roundingAndRemainder() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        DateTimeField hourField = chrono.hourOfDay();
        long instant = ISOChronology.getInstanceUTC().getDateTimeMillis(2007, 1, 1, 10, 45, 30, 500);

        long floor = hourField.roundFloor(instant);
        assertEquals(11, hourField.get(floor));
        assertEquals(0, chrono.minuteOfHour().get(floor));

        long ceiling = hourField.roundCeiling(instant);
        assertEquals(12, hourField.get(ceiling));
        assertEquals(0, chrono.minuteOfHour().get(ceiling));

        long remainder = hourField.remainder(instant);
        assertEquals(45 * 60 * 1000 + 30 * 1000 + 500, remainder);

        DateTimeField dayField = chrono.dayOfMonth();
        long dayFloor = dayField.roundFloor(instant);
        assertEquals(1, dayField.get(dayFloor));
        assertEquals(0, hourField.get(dayFloor));

        long dayCeiling = dayField.roundCeiling(instant);
        assertEquals(2, dayField.get(dayCeiling));
        assertEquals(0, hourField.get(dayCeiling));
    }

    // Tests ZonedDateTimeField differences, leap methods, and min/max boundaries
    @Test
    public void testZonedDateTimeField_differencesLeapAndBoundaries() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        DateTimeField monthField = chrono.monthOfYear();
        long instant1 = ISOChronology.getInstanceUTC().getDateTimeMillis(2007, 1, 1, 0, 0, 0, 0);
        long instant2 = ISOChronology.getInstanceUTC().getDateTimeMillis(2007, 5, 1, 0, 0, 0, 0);

        assertEquals(4, monthField.getDifference(instant2, instant1));
        assertEquals(4L, monthField.getDifferenceAsLong(instant2, instant1));

        DateTimeField leapMonth = chrono.monthOfYear();
        assertFalse(leapMonth.isLeap(instant1));
        assertEquals(0, leapMonth.getLeapAmount(instant1));
        assertNull(leapMonth.getLeapDurationField());

        assertEquals(1, monthField.getMinimumValue());
        assertEquals(1, monthField.getMinimumValue(instant1));
        assertEquals(12, monthField.getMaximumValue());
        assertEquals(12, monthField.getMaximumValue(instant1));
        assertTrue(monthField.getMaximumTextLength(Locale.ENGLISH) > 0);
        assertTrue(monthField.getMaximumShortTextLength(Locale.ENGLISH) > 0);
        assertNotNull(monthField.getDurationField());
        assertNotNull(monthField.getRangeDurationField());
    }

    // Tests ZonedDurationField methods (isPrecise, getUnitMillis, getValue, getMillis, add, getDifference)
    @Test
    public void testZonedDurationField_operations() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        DurationField hourDuration = chrono.hours();
        DurationField dayDuration = chrono.days();

        assertTrue(hourDuration.isPrecise());
        assertFalse(dayDuration.isPrecise());
        assertEquals(DateTimeConstants.MILLIS_PER_HOUR, hourDuration.getUnitMillis());

        long instant = ISOChronology.getInstanceUTC().getDateTimeMillis(2007, 1, 1, 10, 0, 0, 0);
        long duration = 3 * DateTimeConstants.MILLIS_PER_HOUR;

        assertEquals(3, hourDuration.getValue(duration, instant));
        assertEquals(3L, hourDuration.getValueAsLong(duration, instant));
        assertEquals(duration, hourDuration.getMillis(3, instant));
        assertEquals(duration, hourDuration.getMillis(3L, instant));

        long added = hourDuration.add(instant, 2);
        assertEquals(instant + 2 * DateTimeConstants.MILLIS_PER_HOUR, added);

        long addedLong = hourDuration.add(instant, 2L);
        assertEquals(instant + 2 * DateTimeConstants.MILLIS_PER_HOUR, addedLong);

        assertEquals(2, hourDuration.getDifference(added, instant));
        assertEquals(2L, hourDuration.getDifferenceAsLong(added, instant));

        long dayAdded = dayDuration.add(instant, 1);
        assertEquals(1, dayDuration.getDifference(dayAdded, instant));
        assertEquals(1L, dayDuration.getDifferenceAsLong(dayAdded, instant));
    }

    // Tests getInstance with base already being ZonedChronology or UTC zone
    @Test
    public void testGetInstance_withZonedBaseAndUTCZone() {
        ZonedChronology chronoParis = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        ZonedChronology chronoNewYork = ZonedChronology.getInstance(chronoParis, NEW_YORK);
        assertEquals(NEW_YORK, chronoNewYork.getZone());
        assertSame(chronoParis, ZonedChronology.getInstance(chronoParis, PARIS));

        Chronology utcChrono = ZonedChronology.getInstance(chronoParis, UTC);
        assertEquals(ISOChronology.getInstanceUTC(), utcChrono);
    }

    // Tests getDateTimeMillis 4-args during DST gap throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetDateTimeMillis_fourArgs_gapTransition_throwsIllegalArgumentException() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), NEW_YORK);
        int millisOfDay = (2 * 60 + 30) * 60 * 1000;
        chrono.getDateTimeMillis(2007, 3, 11, millisOfDay);
    }

    // Tests getDateTimeMillis instant with time args during DST gap throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetDateTimeMillis_instantAndTimeArgs_gapTransition_throwsIllegalArgumentException() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), NEW_YORK);
        long instant = ISOChronology.getInstanceUTC().getDateTimeMillis(2007, 3, 11, 0, 0, 0, 0);
        chrono.getDateTimeMillis(instant, 2, 30, 0, 0);
    }

    // Tests ZonedDateTimeField half rounding methods for time and date fields
    @Test
    public void testZonedDateTimeField_roundHalfMethods() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        DateTimeField hourField = chrono.hourOfDay();
        DateTimeField dayField = chrono.dayOfMonth();

        // 11:30:00 Paris
        long instant30 = ISOChronology.getInstanceUTC().getDateTimeMillis(2007, 1, 1, 10, 30, 0, 0);
        long floorHour = hourField.roundFloor(instant30);
        long ceilHour = hourField.roundCeiling(instant30);

        assertEquals(floorHour, hourField.roundHalfFloor(instant30));
        assertEquals(ceilHour, hourField.roundHalfCeiling(instant30));
        assertEquals(floorHour, hourField.roundHalfEven(instant30));

        // 11:30:00 Paris on dayOfMonth
        assertEquals(dayField.roundFloor(instant30), dayField.roundHalfFloor(instant30));
        assertEquals(dayField.roundFloor(instant30), dayField.roundHalfCeiling(instant30));
        assertEquals(dayField.roundFloor(instant30), dayField.roundHalfEven(instant30));
    }

    // Tests ZonedDateTimeField leap year support
    @Test
    public void testZonedDateTimeField_leapYearSupport() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        DateTimeField yearField = chrono.year();
        long leapInstant = ISOChronology.getInstanceUTC().getDateTimeMillis(2004, 2, 29, 0, 0, 0, 0);

        assertTrue(yearField.isLeap(leapInstant));
        assertEquals(1, yearField.getLeapAmount(leapInstant));
        assertEquals(chrono.days(), yearField.getLeapDurationField());
    }

    // Tests ZonedDateTimeField getMinimumValue and getMaximumValue with ReadablePartial
    @Test
    public void testZonedDateTimeField_minMaxWithPartial() {
        ZonedChronology chrono = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        DateTimeField dayField = chrono.dayOfMonth();
        Partial partial = new Partial(DateTimeFieldType.dayOfMonth(), 15);
        int[] values = new int[]{15};

        assertEquals(1, dayField.getMinimumValue(partial));
        assertEquals(1, dayField.getMinimumValue(partial, values));
        assertEquals(31, dayField.getMaximumValue(partial));
        assertEquals(31, dayField.getMaximumValue(partial, values));
    }

    // Tests ZonedDateTimeField equals and hashCode
    @Test
    public void testZonedDateTimeField_equalsAndHashCode() {
        ZonedChronology chronoParis = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        ZonedChronology chronoLondon = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), LONDON);

        DateTimeField hourParis1 = chronoParis.hourOfDay();
        DateTimeField hourParis2 = chronoParis.hourOfDay();
        DateTimeField hourLondon = chronoLondon.hourOfDay();
        DateTimeField minuteParis = chronoParis.minuteOfHour();

        assertEquals(hourParis1, hourParis1);
        assertEquals(hourParis1, hourParis2);
        assertEquals(hourParis1.hashCode(), hourParis2.hashCode());
        assertNotEquals(hourParis1, hourLondon);
        assertNotEquals(hourParis1, minuteParis);
        assertNotEquals(hourParis1, null);
        assertNotEquals(hourParis1, "StringObject");
    }

    // Tests ZonedDurationField equals and hashCode
    @Test
    public void testZonedDurationField_equalsAndHashCode() {
        ZonedChronology chronoParis = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), PARIS);
        ZonedChronology chronoLondon = ZonedChronology.getInstance(ISOChronology.getInstanceUTC(), LONDON);

        DurationField hoursParis1 = chronoParis.hours();
        DurationField hoursParis2 = chronoParis.hours();
        DurationField hoursLondon = chronoLondon.hours();
        DurationField daysParis = chronoParis.days();

        assertEquals(hoursParis1, hoursParis1);
        assertEquals(hoursParis1, hoursParis2);
        assertEquals(hoursParis1.hashCode(), hoursParis2.hashCode());
        assertNotEquals(hoursParis1, hoursLondon);
        assertNotEquals(hoursParis1, daysParis);
        assertNotEquals(hoursParis1, null);
        assertNotEquals(hoursParis1, "StringObject");
    }

    // Tests ZonedChronology assembly with different chronologies such as BuddhistChronology
    @Test
    public void testZonedChronology_withBuddhistChronology() {
        BuddhistChronology buddhistUtc = BuddhistChronology.getInstanceUTC();
        ZonedChronology chrono = ZonedChronology.getInstance(buddhistUtc, PARIS);

        assertNotNull(chrono.eras());
        assertNotNull(chrono.centuries());
        assertNotNull(chrono.halfdays());
        assertNotNull(chrono.era());
        assertNotNull(chrono.centuryOfEra());
        assertNotNull(chrono.yearOfEra());
        assertNotNull(chrono.yearOfCentury());
        assertNotNull(chrono.halfdayOfDay());
        assertNotNull(chrono.dayOfWeek());
    }
}