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
    public void testIsLenient_returnsFalse() {
        assertFalse(field.isLenient());
    }

    // Tests get method extracts correct month
    @Test
    public void testGet_validInstant_returnsCorrectMonth() {
        long instant = chronology.getYearMonthDayMillis(2020, 5, 15);
        assertEquals(5, field.get(instant));
    }

    // Tests add method with zero months
    @Test
    public void testAdd_zeroMonths_returnsSameInstant() {
        long instant = chronology.getYearMonthDayMillis(2020, 5, 15);
        assertEquals(instant, field.add(instant, 0));
    }

    // Tests add method with positive months crossing year boundary
    @Test
    public void testAdd_positiveMonths_returnsCorrectInstant() {
        long instant = chronology.getYearMonthDayMillis(2020, 10, 15);
        long result = field.add(instant, 5);
        long expected = chronology.getYearMonthDayMillis(2021, 3, 15);
        assertEquals(expected, result);
    }

    // Tests add method with negative months
    @Test
    public void testAdd_negativeMonths_returnsCorrectInstant() {
        long instant = chronology.getYearMonthDayMillis(2021, 3, 15);
        long result = field.add(instant, -5);
        long expected = chronology.getYearMonthDayMillis(2020, 10, 15);
        assertEquals(expected, result);
    }

    // Tests add method negative months boundary where remMonthToUse == 0 and monthToUse == 1
    @Test
    public void testAdd_negativeMonthsBoundary_returnsCorrectInstant() {
        long instant = chronology.getYearMonthDayMillis(2020, 1, 15);
        long result = field.add(instant, -12);
        long expected = chronology.getYearMonthDayMillis(2019, 1, 15);
        assertEquals(expected, result);
    }

    // Tests add method day of month coercion to nearest sane value
    @Test
    public void testAdd_dayOfMonthCoercion_clampsToMaxDayOfMonth() {
        long instant = chronology.getYearMonthDayMillis(2020, 1, 31);
        long resultLeap = field.add(instant, 1);
        long expectedLeap = chronology.getYearMonthDayMillis(2020, 2, 29);
        assertEquals(expectedLeap, resultLeap);

        long nonLeapInstant = chronology.getYearMonthDayMillis(2021, 1, 31);
        long resultNonLeap = field.add(nonLeapInstant, 1);
        long expectedNonLeap = chronology.getYearMonthDayMillis(2021, 2, 28);
        assertEquals(expectedNonLeap, resultNonLeap);
    }

    // Tests add with long months delegate and large values
    @Test
    public void testAdd_longMonths_returnsCorrectInstant() {
        long instant = chronology.getYearMonthDayMillis(2020, 1, 15);
        long result = field.add(instant, 12L);
        long expected = chronology.getYearMonthDayMillis(2021, 1, 15);
        assertEquals(expected, result);

        long largeAdd = field.add(instant, 1200L);
        long expectedLarge = chronology.getYearMonthDayMillis(2120, 1, 15);
        assertEquals(expectedLarge, largeAdd);
    }

    // Tests add with long months exceeding maximum year throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testAdd_longMonthsOutOfRange_throwsIllegalArgumentException() {
        long instant = chronology.getYearMonthDayMillis(2020, 1, 15);
        field.add(instant, 1000000000000L);
    }

    // Tests add on contiguous ReadablePartial with zero value
    @Test
    public void testAdd_readablePartialZeroValue_returnsSameValues() {
        YearMonth ym = new YearMonth(2020, 5, chronology);
        int[] values = new int[] {2020, 5};
        int[] result = field.add(ym, 1, values, 0);
        assertArrayEquals(values, result);
    }

    // Tests add on contiguous ReadablePartial like MonthDay
    @Test
    public void testAdd_readablePartialMonthDay_adjustsMonthAndDay() {
        MonthDay md = new MonthDay(2, 29, chronology);
        int[] values = new int[] {2, 29};
        int[] result = field.add(md, 0, values, 1);
        assertEquals(3, result[0]);
        assertEquals(29, result[1]);
    }

    // Tests add on non-contiguous ReadablePartial delegates to super
    @Test
    public void testAdd_nonContiguousPartial_delegatesToSuper() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.minuteOfHour()
        };
        int[] values = new int[] {5, 30};
        Partial partial = new Partial(types, values, chronology);
        int[] result = field.add(partial, 0, values, 2);
        assertEquals(7, result[0]);
        assertEquals(30, result[1]);
    }

    // Tests addWrapField within year
    @Test
    public void testAddWrapField_wrapsWithinBounds() {
        long instant = chronology.getYearMonthDayMillis(2020, 11, 15);
        long result = field.addWrapField(instant, 3);
        long expected = chronology.getYearMonthDayMillis(2020, 2, 15);
        assertEquals(expected, result);
    }

    // Tests getDifferenceAsLong with minuend less than subtrahend
    @Test
    public void testGetDifferenceAsLong_minuendLessThanSubtrahend_returnsNegativeDifference() {
        long start = chronology.getYearMonthDayMillis(2020, 1, 15);
        long end = chronology.getYearMonthDayMillis(2020, 6, 15);
        assertEquals(-5L, field.getDifferenceAsLong(start, end));
    }

    // Tests getDifferenceAsLong with end of month adjustment
    @Test
    public void testGetDifferenceAsLong_endOfMonthAdjustment_returnsCorrectDifference() {
        long febEnd = chronology.getYearMonthDayMillis(2020, 2, 29);
        long janEnd = chronology.getYearMonthDayMillis(2020, 1, 31);
        assertEquals(1L, field.getDifferenceAsLong(febEnd, janEnd));

        long jan15 = chronology.getYearMonthDayMillis(2020, 1, 15);
        long jan14 = chronology.getYearMonthDayMillis(2020, 1, 14);
        assertEquals(0L, field.getDifferenceAsLong(jan15, jan14));
    }

    // Tests set with valid month and DOM coercion
    @Test
    public void testSet_validMonthWithDayCoercion_returnsCorrectInstant() {
        long instant = chronology.getYearMonthDayMillis(2020, 1, 31);
        long result = field.set(instant, 2);
        long expected = chronology.getYearMonthDayMillis(2020, 2, 29);
        assertEquals(expected, result);
    }

    // Tests set with invalid month value throws exception
    @Test(expected = IllegalFieldValueException.class)
    public void testSet_invalidMonthValue_throwsException() {
        long instant = chronology.getYearMonthDayMillis(2020, 1, 15);
        field.set(instant, 13);
    }

    // Tests isLeap and getLeapAmount
    @Test
    public void testIsLeapAndGetLeapAmount_leapMonthInLeapYear_returnsTrueAndOne() {
        long leapInstant = chronology.getYearMonthDayMillis(2020, 2, 15);
        assertTrue(field.isLeap(leapInstant));
        assertEquals(1, field.getLeapAmount(leapInstant));

        long nonLeapInstant = chronology.getYearMonthDayMillis(2021, 2, 15);
        assertFalse(field.isLeap(nonLeapInstant));
        assertEquals(0, field.getLeapAmount(nonLeapInstant));

        long otherMonthInstant = chronology.getYearMonthDayMillis(2020, 3, 15);
        assertFalse(field.isLeap(otherMonthInstant));
    }

    // Tests duration field getters and bounds
    @Test
    public void testFieldPropertiesAndBounds() {
        assertEquals(DurationFieldType.years(), field.getRangeDurationField().getType());
        assertEquals(DurationFieldType.days(), field.getLeapDurationField().getType());
        assertEquals(DateTimeConstants.JANUARY, field.getMinimumValue());
        assertEquals(12, field.getMaximumValue());
    }

    // Tests roundFloor and remainder methods
    @Test
    public void testRoundFloorAndRemainder() {
        long instant = chronology.getYearMonthDayMillis(2020, 5, 15) + 12345L;
        long floor = field.roundFloor(instant);
        long expectedFloor = chronology.getYearMonthDayMillis(2020, 5, 1);
        assertEquals(expectedFloor, floor);
        assertEquals(instant - expectedFloor, field.remainder(instant));
    }
}