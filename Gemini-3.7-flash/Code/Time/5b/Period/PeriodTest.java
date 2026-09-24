package org.joda.time;

import org.junit.Test;

import static org.junit.Assert.*;

public class PeriodTest {

    // Tests defect Time-5: normalising months to months-only PeriodType
    @Test
    public void testNormalizedStandard_monthsPeriodType_normalizesCorrectly() {
        Period test = Period.months(56);
        Period normalized = test.normalizedStandard(PeriodType.months());
        assertEquals(56, normalized.getMonths());
        assertEquals(0, normalized.getYears());
    }

    // Tests defect Time-5: normalising years and months with years-only PeriodType
    @Test
    public void testNormalizedStandard_yearsPeriodType_normalizesCorrectly() {
        Period test = Period.years(5).withMonths(24);
        Period normalized = test.normalizedStandard(PeriodType.years());
        assertEquals(7, normalized.getYears());
        assertEquals(0, normalized.getMonths());
    }

    // Tests standard normalization with year and month rollover
    @Test
    public void testNormalizedStandard_standardTypeWithRollover_returnsNormalized() {
        Period test = new Period(1, 15, 0, 0, 0, 0, 0, 0);
        Period result = test.normalizedStandard();
        assertEquals(2, result.getYears());
        assertEquals(3, result.getMonths());
    }

    // Tests static factory methods for single fields
    @Test
    public void testFactoryMethods_validValues_returnCorrectFields() {
        assertEquals(3, Period.years(3).getYears());
        assertEquals(4, Period.months(4).getMonths());
        assertEquals(5, Period.weeks(5).getWeeks());
        assertEquals(6, Period.days(6).getDays());
        assertEquals(7, Period.hours(7).getHours());
        assertEquals(8, Period.minutes(8).getMinutes());
        assertEquals(9, Period.seconds(9).getSeconds());
        assertEquals(10, Period.millis(10).getMillis());
    }

    // Tests parsing standard ISO 8601 string
    @Test
    public void testParse_standardString_returnsPeriod() {
        Period parsed = Period.parse("P1Y2M3W4DT5H6M7.008S");
        assertEquals(1, parsed.getYears());
        assertEquals(2, parsed.getMonths());
        assertEquals(3, parsed.getWeeks());
        assertEquals(4, parsed.getDays());
        assertEquals(5, parsed.getHours());
        assertEquals(6, parsed.getMinutes());
        assertEquals(7, parsed.getSeconds());
        assertEquals(8, parsed.getMillis());
    }

    // Tests fieldDifference between two Partial instances
    @Test
    public void testFieldDifference_validPartials_returnsDifference() {
        LocalDate start = new LocalDate(2005, 6, 9);
        LocalDate end = new LocalDate(2007, 4, 12);
        Period diff = Period.fieldDifference(start, end);
        assertEquals(2, diff.getYears());
        assertEquals(-2, diff.getMonths());
        assertEquals(3, diff.getDays());
    }

    // Tests fieldDifference exception when start is null
    @Test(expected = IllegalArgumentException.class)
    public void testFieldDifference_nullStart_throwsException() {
        LocalDate end = new LocalDate(2007, 4, 12);
        Period.fieldDifference(null, end);
    }

    // Tests fieldDifference exception when partial types mismatch
    @Test(expected = IllegalArgumentException.class)
    public void testFieldDifference_mismatchedPartials_throwsException() {
        LocalDate start = new LocalDate(2005, 6, 9);
        LocalTime end = new LocalTime(12, 0);
        Period.fieldDifference(start, end);
    }

    // Tests withXxx methods immutability and updated values
    @Test
    public void testWithMethods_validValues_returnsUpdatedPeriod() {
        Period base = new Period();
        Period updated = base.withYears(1)
                             .withMonths(2)
                             .withWeeks(3)
                             .withDays(4)
                             .withHours(5)
                             .withMinutes(6)
                             .withSeconds(7)
                             .withMillis(8);

        assertEquals(0, base.getYears());
        assertEquals(1, updated.getYears());
        assertEquals(2, updated.getMonths());
        assertEquals(3, updated.getWeeks());
        assertEquals(4, updated.getDays());
        assertEquals(5, updated.getHours());
        assertEquals(6, updated.getMinutes());
        assertEquals(7, updated.getSeconds());
        assertEquals(8, updated.getMillis());
    }

    // Tests plus and minus with another ReadablePeriod
    @Test
    public void testPlusAndMinus_readablePeriod_returnsExpectedResult() {
        Period p1 = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period p2 = new Period(1, 1, 1, 1, 1, 1, 1, 1);

        Period sum = p1.plus(p2);
        assertEquals(2, sum.getYears());
        assertEquals(3, sum.getMonths());
        assertEquals(4, sum.getWeeks());
        assertEquals(5, sum.getDays());
        assertEquals(6, sum.getHours());
        assertEquals(7, sum.getMinutes());
        assertEquals(8, sum.getSeconds());
        assertEquals(9, sum.getMillis());

        Period diff = p1.minus(p2);
        assertEquals(0, diff.getYears());
        assertEquals(1, diff.getMonths());
        assertEquals(2, diff.getWeeks());
        assertEquals(3, diff.getDays());
        assertEquals(4, diff.getHours());
        assertEquals(5, diff.getMinutes());
        assertEquals(6, diff.getSeconds());
        assertEquals(7, diff.getMillis());

        assertSame(p1, p1.plus((ReadablePeriod) null));
        assertSame(p1, p1.minus((ReadablePeriod) null));
    }

    // Tests plusXxx and minusXxx field methods with zero and non-zero
    @Test
    public void testPlusMinusFields_zeroAndValues_returnsExpectedResult() {
        Period p = Period.hours(5);
        assertSame(p, p.plusHours(0));
        assertEquals(8, p.plusHours(3).getHours());
        assertEquals(2, p.minusHours(3).getHours());
        assertEquals(2, p.plusMinutes(2).getMinutes());
        assertEquals(-2, p.minusMinutes(2).getMinutes());
    }

    // Tests multipliedBy and negated
    @Test
    public void testMultipliedByAndNegated_variousScalars_returnsExpected() {
        Period p = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        assertSame(Period.ZERO, Period.ZERO.multipliedBy(5));
        assertSame(p, p.multipliedBy(1));

        Period doubled = p.multipliedBy(2);
        assertEquals(2, doubled.getYears());
        assertEquals(4, doubled.getMonths());
        assertEquals(6, doubled.getWeeks());
        assertEquals(8, doubled.getDays());
        assertEquals(10, doubled.getHours());
        assertEquals(12, doubled.getMinutes());
        assertEquals(14, doubled.getSeconds());
        assertEquals(16, doubled.getMillis());

        Period negated = p.negated();
        assertEquals(-1, negated.getYears());
        assertEquals(-2, negated.getMonths());
        assertEquals(-3, negated.getWeeks());
        assertEquals(-4, negated.getDays());
    }

    // Tests conversions to standard units (Weeks, Days, Hours, Minutes, Seconds, Duration)
    @Test
    public void testToStandardUnits_validTimeAndDayPeriod_convertsCorrectly() {
        Period p = new Period(0, 0, 1, 2, 3, 4, 5, 6);
        // 1 week + 2 days = 9 days = 216 hours + 3 hours = 219 hours
        assertEquals(1, p.toStandardWeeks().getWeeks());
        assertEquals(9, p.toStandardDays().getDays());
        assertEquals(219, p.toStandardHours().getHours());
        assertEquals(219 * 60 + 4, p.toStandardMinutes().getMinutes());
        assertEquals((219 * 60 + 4) * 60 + 5, p.toStandardSeconds().getSeconds());

        long expectedMillis = (((((1L * 7 + 2) * 24 + 3) * 60 + 4) * 60 + 5) * 1000) + 6;
        assertEquals(expectedMillis, p.toStandardDuration().getMillis());
    }

    // Tests toStandard conversion throwing exception when years are present
    @Test(expected = UnsupportedOperationException.class)
    public void testToStandardDuration_withYears_throwsException() {
        Period.years(1).toStandardDuration();
    }

    // Tests toStandard conversion throwing exception when months are present
    @Test(expected = UnsupportedOperationException.class)
    public void testToStandardSeconds_withMonths_throwsException() {
        Period.months(1).toStandardSeconds();
    }

    // Tests withPeriodType method
    @Test
    public void testWithPeriodType_sameAndDifferentType_returnsExpected() {
        Period p = new Period(0, 0, 0, 0, 1, 30, 0, 0);
        assertSame(p, p.withPeriodType(PeriodType.standard()));

        Period timeOnly = p.withPeriodType(PeriodType.time());
        assertEquals(PeriodType.time(), timeOnly.getPeriodType());
        assertEquals(1, timeOnly.getHours());
        assertEquals(30, timeOnly.getMinutes());
    }

    // Tests withField and withFieldAdded
    @Test
    public void testWithFieldAndWithFieldAdded_validFields_modifiesCorrectly() {
        Period p = Period.hours(2);
        Period p2 = p.withField(DurationFieldType.minutes(), 45);
        assertEquals(2, p2.getHours());
        assertEquals(45, p2.getMinutes());

        assertSame(p2, p2.withFieldAdded(DurationFieldType.minutes(), 0));
        Period p3 = p2.withFieldAdded(DurationFieldType.minutes(), 15);
        assertEquals(60, p3.getMinutes());
    }

    // Tests withField exception with null field
    @Test(expected = IllegalArgumentException.class)
    public void testWithField_nullField_throwsException() {
        Period.hours(1).withField(null, 5);
    }

    // Tests withFields merging another period
    @Test
    public void testWithFields_readablePeriod_mergesValues() {
        Period p1 = Period.hours(2).withMinutes(10);
        Period p2 = Period.minutes(30).withSeconds(15);
        Period result = p1.withFields(p2);

        assertEquals(2, result.getHours());
        assertEquals(30, result.getMinutes());
        assertEquals(15, result.getSeconds());
        assertSame(p1, p1.withFields(null));
    }
}