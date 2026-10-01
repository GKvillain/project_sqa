package org.joda.time.field;

import org.joda.time.Chronology;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.ISOChronology;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for org.joda.time.field.LenientDateTimeField
 */
public class LenientDateTimeFieldTest {

    private Chronology isoUtc;
    private Chronology isoParis;
    private DateTimeZone parisZone;

    @Before
    public void setUp() {
        isoUtc = ISOChronology.getInstanceUTC();
        parisZone = DateTimeZone.forID("Europe/Paris");
        isoParis = ISOChronology.getInstance(parisZone);
    }

    // Tests getInstance with null field returning null
    @Test
    public void testGetInstance_nullField_returnsNull() {
        DateTimeField result = LenientDateTimeField.getInstance(null, isoUtc);
        assertNull(result);
    }

    // Tests getInstance with already lenient field returning same instance
    @Test
    public void testGetInstance_alreadyLenientField_returnsSame() {
        DateTimeField monthField = isoUtc.monthOfYear();
        DateTimeField lenientMonth = LenientDateTimeField.getInstance(monthField, isoUtc);
        DateTimeField result = LenientDateTimeField.getInstance(lenientMonth, isoUtc);
        assertSame(lenientMonth, result);
    }

    // Tests getInstance with StrictDateTimeField unwrapping and wrapping as lenient
    @Test
    public void testGetInstance_strictDateTimeField_unwrapsAndReturnsLenient() {
        DateTimeField dayField = isoUtc.dayOfMonth();
        DateTimeField strictDay = StrictDateTimeField.getInstance(dayField);
        DateTimeField lenientDay = LenientDateTimeField.getInstance(strictDay, isoUtc);

        assertNotNull(lenientDay);
        assertTrue(lenientDay.isLenient());
        assertEquals(dayField.getType(), lenientDay.getType());
    }

    // Tests getInstance with standard non-lenient field returning new lenient instance
    @Test
    public void testGetInstance_standardField_returnsLenientInstance() {
        DateTimeField hourField = isoUtc.hourOfDay();
        assertFalse(hourField.isLenient());

        DateTimeField lenientHour = LenientDateTimeField.getInstance(hourField, isoUtc);
        assertNotNull(lenientHour);
        assertTrue(lenientHour.isLenient());
        assertEquals(DateTimeFieldType.hourOfDay(), lenientHour.getType());
    }

    // Tests isLenient returning true
    @Test
    public void testIsLenient_always_returnsTrue() {
        DateTimeField field = LenientDateTimeField.getInstance(isoUtc.dayOfMonth(), isoUtc);
        assertTrue(field.isLenient());
    }

    // Tests set with in-bounds value
    @Test
    public void testSet_inBoundsValue_setsCorrectly() {
        // 2007-02-15T12:00:00.000Z
        long instant = 1171540800000L;
        DateTimeField dayField = LenientDateTimeField.getInstance(isoUtc.dayOfMonth(), isoUtc);
        long result = dayField.set(instant, 20);

        // Expected: 2007-02-20T12:00:00.000Z
        long expected = 1171972800000L;
        assertEquals(expected, result);
    }

    // Tests set with out-of-bounds positive overflow value
    @Test
    public void testSet_outOfBoundsPositiveDay_rollsForwardToNextMonth() {
        // 2007-01-15T00:00:00.000Z
        long instant = 1168819200000L;
        DateTimeField dayField = LenientDateTimeField.getInstance(isoUtc.dayOfMonth(), isoUtc);
        // Setting day of month to 32 -> 2007-02-01T00:00:00.000Z
        long result = dayField.set(instant, 32);

        assertEquals(2, isoUtc.monthOfYear().get(result));
        assertEquals(1, isoUtc.dayOfMonth().get(result));
    }

    // Tests set with out-of-bounds zero/negative underflow value
    @Test
    public void testSet_zeroDay_rollsBackToPreviousMonth() {
        // 2007-03-10T00:00:00.000Z
        long instant = 1173484800000L;
        DateTimeField dayField = LenientDateTimeField.getInstance(isoUtc.dayOfMonth(), isoUtc);
        // Setting day of month to 0 -> 2007-02-28T00:00:00.000Z
        long result = dayField.set(instant, 0);

        assertEquals(2, isoUtc.monthOfYear().get(result));
        assertEquals(28, isoUtc.dayOfMonth().get(result));
    }

    // Tests set with negative month value rolling back multiple months and years
    @Test
    public void testSet_negativeMonth_rollsBackYears() {
        // 2007-06-15T00:00:00.000Z
        long instant = 1181865600000L;
        DateTimeField monthField = LenientDateTimeField.getInstance(isoUtc.monthOfYear(), isoUtc);
        // Current month is 6, setting to -2 (diff = -8 months -> 2006-10-15)
        long result = monthField.set(instant, -2);

        assertEquals(2006, isoUtc.year().get(result));
        assertEquals(10, isoUtc.monthOfYear().get(result));
        assertEquals(15, isoUtc.dayOfMonth().get(result));
    }

    // Tests set with same value as current (difference is zero)
    @Test
    public void testSet_sameValue_returnsOriginalInstant() {
        // 2007-05-10T15:30:00.000Z
        long instant = 1178811000000L;
        DateTimeField hourField = LenientDateTimeField.getInstance(isoUtc.hourOfDay(), isoUtc);
        long result = hourField.set(instant, 15);

        assertEquals(instant, result);
    }

    // Tests set in non-UTC timezone with DST conversion
    @Test
    public void testSet_nonUTCTimeZone_adjustsCorrectlyAcrossDstTransition() {
        // Paris DST switch happened on 2007-03-25 (02:00 -> 03:00)
        // 2007-03-24T12:00:00 in Paris (UTC+1) = 1174734000000L
        long instant = 1174734000000L;
        DateTimeField hourField = LenientDateTimeField.getInstance(isoParis.hourOfDay(), isoParis);
        // Add 24 hours leninetly -> setting hour to 36
        long result = hourField.set(instant, 36);

        // In Paris zone, hour should be 12 on the next day
        assertEquals(12, isoParis.hourOfDay().get(result));
        assertEquals(25, isoParis.dayOfMonth().get(result));
    }

    // Tests set with hour overflow in standard timezone
    @Test
    public void testSet_hourOverflow_rollsToNextDay() {
        // 2007-01-01T20:00:00.000Z
        long instant = 1167681600000L;
        DateTimeField hourField = LenientDateTimeField.getInstance(isoUtc.hourOfDay(), isoUtc);
        // Setting hour to 26 (diff = +6 -> 2007-01-02T02:00:00.000Z)
        long result = hourField.set(instant, 26);

        assertEquals(2, isoUtc.dayOfMonth().get(result));
        assertEquals(2, isoUtc.hourOfDay().get(result));
    }

    // Tests set with large positive difference causing multiple overflows
    @Test
    public void testSet_largePositiveMinute_advancesHoursAndDays() {
        // 2007-01-01T00:00:00.000Z
        long instant = 1167609600000L;
        DateTimeField minuteField = LenientDateTimeField.getInstance(isoUtc.minuteOfHour(), isoUtc);
        // Setting minute to 1440 (24 hours later -> 2007-01-02T00:00:00.000Z)
        long result = minuteField.set(instant, 1440);

        assertEquals(2, isoUtc.dayOfMonth().get(result));
        assertEquals(0, isoUtc.hourOfDay().get(result));
        assertEquals(0, isoUtc.minuteOfHour().get(result));
    }

    // Tests subclassing LenientDateTimeField to directly verify protected constructor
    @Test
    public void testSubclassConstructor() {
        LenientDateTimeField custom = new LenientDateTimeField(isoUtc.dayOfMonth(), isoUtc) {
            private static final long serialVersionUID = 1L;
        };
        assertTrue(custom.isLenient());
        assertEquals(isoUtc.dayOfMonth().getType(), custom.getType());
    }

    // Tests set with year field
    @Test
    public void testSet_yearField() {
        // 1970-01-01T00:00:00.000Z
        long instant = 0L;
        DateTimeField yearField = LenientDateTimeField.getInstance(isoUtc.year(), isoUtc);
        long result = yearField.set(instant, 1960);

        assertEquals(1960, isoUtc.year().get(result));
        assertEquals(1, isoUtc.dayOfMonth().get(result));
    }

    // Tests set with secondOfMinute overflow
    @Test
    public void testSet_secondOfMinute_overflow() {
        // 1970-01-01T00:00:00.000Z
        long instant = 0L;
        DateTimeField secField = LenientDateTimeField.getInstance(isoUtc.secondOfMinute(), isoUtc);
        long result = secField.set(instant, 120);

        assertEquals(2, isoUtc.minuteOfHour().get(result));
        assertEquals(0, isoUtc.secondOfMinute().get(result));
    }

    // Tests set with millisOfSecond underflow
    @Test
    public void testSet_millisOfSecond_underflow() {
        // 1970-01-01T00:00:05.000Z
        long instant = 5000L;
        DateTimeField millisField = LenientDateTimeField.getInstance(isoUtc.millisOfSecond(), isoUtc);
        long result = millisField.set(instant, -500);

        assertEquals(4500L, result);
        assertEquals(4, isoUtc.secondOfMinute().get(result));
        assertEquals(500, isoUtc.millisOfSecond().get(result));
    }

    // Tests set during DST overlap/fall-back transition in non-UTC timezone
    @Test
    public void testSet_dstOverlap_adjustsCorrectly() {
        // Paris DST switch back happened on 2007-10-28 (03:00 -> 02:00)
        // 2007-10-28T01:30:00 in Paris (UTC+2) = 1193527800000L
        long instant = 1193527800000L;
        DateTimeField hourField = LenientDateTimeField.getInstance(isoParis.hourOfDay(), isoParis);
        long result = hourField.set(instant, 2);

        assertEquals(2, isoParis.hourOfDay().get(result));
        assertEquals(30, isoParis.minuteOfHour().get(result));
    }
}