package org.joda.time.chrono;

import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DurationFieldType;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.MonthDay;
import org.joda.time.Partial;
import org.joda.time.YearMonth;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BasicMonthOfYearDateTimeFieldTest {

    private BasicChronology chronology;
    private BasicMonthOfYearDateTimeField field;

    @Before
    public void setUp() {
        chronology = GregorianChronology.getInstanceUTC();
        field = new BasicMonthOfYearDateTimeField(chronology, DateTimeConstants.FEBRUARY);
    }

    // Tests isLenient returns false
    @Test
    public void testIsLenient_always_returnsFalse() {
        assertFalse(field.isLenient());
    }

    // Tests get method for basic month extraction
    @Test
    public void testGet_validInstant_returnsCorrectMonth() {
        // 2020-06-15 UTC
        long instant = chronology.getYearMonthDayMillis(2020, 6, 15);
        assertEquals(6, field.get(instant));
    }

    // Tests adding zero months returns the exact same instant
    @Test
    public void testAdd_zeroMonths_returnsSameInstant() {
        long instant = chronology.getYearMonthDayMillis(2021, 5, 10) + 12345L;
        assertEquals(instant, field.add(instant, 0));
        assertEquals(instant, field.add(instant, 0L));
    }

    // Tests normal positive addition within the same year
    @Test
    public void testAdd_positiveMonthsWithinYear_returnsUpdatedInstant() {
        long instant = chronology.getYearMonthDayMillis(2021, 3, 15);
        long expected = chronology.getYearMonthDayMillis(2021, 7, 15);
        assertEquals(expected, field.add(instant, 4));
    }

    // Tests positive addition wrapping over year boundaries
    @Test
    public void testAdd_positiveMonthsCrossingYear_returnsUpdatedInstant() {
        long instant = chronology.getYearMonthDayMillis(2021, 10, 15);
        long expected = chronology.getYearMonthDayMillis(2023, 2, 15);
        assertEquals(expected, field.add(instant, 16));
    }

    // Tests negative month addition and boundary conditions
    @Test
    public void testAdd_negativeMonths_returnsUpdatedInstant() {
        long instant = chronology.getYearMonthDayMillis(2021, 5, 15);
        long expected = chronology.getYearMonthDayMillis(2020, 11, 15);
        assertEquals(expected, field.add(instant, -6));

        // Test boundary where remMonthToUse == 0 and monthToUse == 1
        long janInstant = chronology.getYearMonthDayMillis(2021, 1, 15);
        long decExpected = chronology.getYearMonthDayMillis(2020, 12, 15);
        assertEquals(decExpected, field.add(janInstant, -1));

        long prevJanExpected = chronology.getYearMonthDayMillis(2020, 1, 15);
        assertEquals(prevJanExpected, field.add(janInstant, -12));
    }

    // Tests day-of-month clamping when target month has fewer days
    @Test
    public void testAdd_dayClamping_clampsToLastDayOfMonth() {
        // 2021-03-31 + 1 month -> 2021-04-30
        long mar31 = chronology.getYearMonthDayMillis(2021, 3, 31);
        long apr30 = chronology.getYearMonthDayMillis(2021, 4, 30);
        assertEquals(apr30, field.add(mar31, 1));

        // 2020-03-31 - 1 month -> 2020-02-29 (leap year)
        long mar31Leap = chronology.getYearMonthDayMillis(2020, 3, 31);
        long feb29 = chronology.getYearMonthDayMillis(2020, 2, 29);
        assertEquals(feb29, field.add(mar31Leap, -1));

        // 2021-03-31 - 1 month -> 2021-02-28 (non-leap year)
        long feb28 = chronology.getYearMonthDayMillis(2021, 2, 28);
        assertEquals(feb28, field.add(mar31, -1));
    }

    // Tests add with long value within and beyond integer range
    @Test
    public void testAdd_longAmount_returnsCorrectInstant() {
        long instant = chronology.getYearMonthDayMillis(2021, 3, 15);
        long expected = chronology.getYearMonthDayMillis(2023, 7, 15);
        assertEquals(expected, field.add(instant, 28L));
    }

    // Tests add with long value exceeding max supported year throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testAdd_longAmountTooLarge_throwsException() {
        long instant = chronology.getYearMonthDayMillis(2021, 3, 15);
        field.add(instant, 1000000000000L);
    }

    // Tests add on ReadablePartial when valueToAdd is zero
    @Test
    public void testAddPartial_zeroValueToAdd_returnsOriginalValues() {
        YearMonth ym = new YearMonth(2021, 5, chronology);
        int[] values = new int[]{2021, 5};
        int[] result = field.add(ym, 1, values, 0);
        assertArrayEquals(values, result);
    }

    // Tests add on contiguous ReadablePartial (MonthDay with Feb 29 - Defects4J 14b trigger)
    @Test
    public void testAddPartial_contiguousMonthDayLeap_returnsExpected() {
        MonthDay md = new MonthDay(2, 29, chronology);
        int[] values = new int[]{2, 29};
        int[] result = field.add(md, 0, values, 1);
        assertEquals(3, result[0]);
        assertEquals(29, result[1]);
    }

    // Tests add on non-contiguous Partial delegates to super
    @Test
    public void testAddPartial_nonContiguousPartial_delegatesToSuper() {
        Partial partial = new Partial(DateTimeFieldType.monthOfYear(), 5);
        int[] values = new int[]{5};
        int[] result = field.add(partial, 0, values, 3);
        assertEquals(8, result[0]);
    }

    // Tests addWrapField correctly wraps around month bounds
    @Test
    public void testAddWrapField_wrapAround_returnsWrappedMonth() {
        long instant = chronology.getYearMonthDayMillis(2021, 11, 15);
        long expected = chronology.getYearMonthDayMillis(2021, 2, 15);
        assertEquals(expected, field.addWrapField(instant, 3));
    }

    // Tests difference between two instants in months
    @Test
    public void testGetDifferenceAsLong_positiveAndNegative_returnsCorrectDifference() {
        long instant1 = chronology.getYearMonthDayMillis(2021, 6, 15);
        long instant2 = chronology.getYearMonthDayMillis(2020, 2, 15);

        assertEquals(16L, field.getDifferenceAsLong(instant1, instant2));
        assertEquals(-16L, field.getDifferenceAsLong(instant2, instant1));

        // Test difference when subtrahend day is larger than minuend month end day
        long mar31 = chronology.getYearMonthDayMillis(2021, 3, 31);
        long feb28 = chronology.getYearMonthDayMillis(2021, 2, 28);
        assertEquals(1L, field.getDifferenceAsLong(mar31, feb28));
    }

    // Tests setting month with valid values and day clamping
    @Test
    public void testSet_validMonthAndDayClamping_returnsUpdatedInstant() {
        long jan31 = chronology.getYearMonthDayMillis(2021, 1, 31) + 5000L;
        long feb28 = chronology.getYearMonthDayMillis(2021, 2, 28) + 5000L;
        assertEquals(feb28, field.set(jan31, 2));

        long jul31 = chronology.getYearMonthDayMillis(2021, 7, 31);
        long jun30 = chronology.getYearMonthDayMillis(2021, 6, 30);
        assertEquals(jun30, field.set(jul31, 6));
    }

    // Tests setting invalid month throws exception
    @Test(expected = IllegalFieldValueException.class)
    public void testSet_invalidMonthBelowMin_throwsException() {
        long instant = chronology.getYearMonthDayMillis(2021, 5, 10);
        field.set(instant, 0);
    }

    // Tests setting invalid month above max throws exception
    @Test(expected = IllegalFieldValueException.class)
    public void testSet_invalidMonthAboveMax_throwsException() {
        long instant = chronology.getYearMonthDayMillis(2021, 5, 10);
        field.set(instant, 13);
    }

    // Tests leap status and leap amounts for leap vs non-leap years
    @Test
    public void testIsLeapAndLeapAmount_leapAndNonLeapYears_returnsCorrectValues() {
        long leapFeb = chronology.getYearMonthDayMillis(2020, 2, 15);
        long nonLeapFeb = chronology.getYearMonthDayMillis(2021, 2, 15);
        long leapMar = chronology.getYearMonthDayMillis(2020, 3, 15);

        assertTrue(field.isLeap(leapFeb));
        assertEquals(1, field.getLeapAmount(leapFeb));

        assertFalse(field.isLeap(nonLeapFeb));
        assertEquals(0, field.getLeapAmount(nonLeapFeb));

        assertFalse(field.isLeap(leapMar));
        assertEquals(0, field.getLeapAmount(leapMar));
    }

    // Tests duration fields, bounds, roundFloor, and remainder
    @Test
    public void testMetadataAndRoundingMethods_validInputs_returnsExpectedValues() {
        assertEquals(1, field.getMinimumValue());
        assertEquals(12, field.getMaximumValue());
        assertEquals(DurationFieldType.years(), field.getRangeDurationField().getType());
        assertEquals(DurationFieldType.days(), field.getLeapDurationField().getType());

        long instant = chronology.getYearMonthDayMillis(2021, 5, 20) + 123456L;
        long expectedFloor = chronology.getYearMonthMillis(2021, 5);
        assertEquals(expectedFloor, field.roundFloor(instant));
        assertEquals(instant - expectedFloor, field.remainder(instant));
    }
}