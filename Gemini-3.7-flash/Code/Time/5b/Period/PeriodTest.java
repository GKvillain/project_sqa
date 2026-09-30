package org.joda.time;

import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.ISOPeriodFormat;
import org.junit.Test;

import static org.junit.Assert.*;

public class PeriodTest {

    // Tests normalizedStandard calculation with standard years and months
    @Test
    public void testNormalizedStandard_yearsAndMonths_normalizesCorrectly() {
        Period period = new Period(1, 15, 0, 0, 0, 0, 0, 0);
        Period normalized = period.normalizedStandard();
        assertEquals(2, normalized.getYears());
        assertEquals(3, normalized.getMonths());
    }

    // Tests normalizedStandard with custom PeriodType when years and months are present
    @Test
    public void testNormalizedStandard_customPeriodType_normalizesCorrectly() {
        Period period = new Period(0, 0, 2, 3, 4, 5, 6, 7);
        Period normalized = period.normalizedStandard(PeriodType.dayTime());
        assertEquals(0, normalized.getWeeks());
        assertEquals(17, normalized.getDays());
        assertEquals(4, normalized.getHours());
        assertEquals(5, normalized.getMinutes());
        assertEquals(6, normalized.getSeconds());
        assertEquals(7, normalized.getMillis());
    }

    // Tests normalizedStandard with custom PeriodType unsupported field exception
    @Test(expected = UnsupportedOperationException.class)
    public void testNormalizedStandard_unsupportedYearsMonths_throwsException() {
        Period period = new Period(1, 2, 0, 0, 0, 0, 0, 0);
        period.normalizedStandard(PeriodType.time());
    }

    // Tests factory methods for single field period creation
    @Test
    public void testFactoryMethods_singleFieldValues_correctlyAssigned() {
        assertEquals(5, Period.years(5).getYears());
        assertEquals(4, Period.months(4).getMonths());
        assertEquals(3, Period.weeks(3).getWeeks());
        assertEquals(2, Period.days(2).getDays());
        assertEquals(10, Period.hours(10).getHours());
        assertEquals(20, Period.minutes(20).getMinutes());
        assertEquals(30, Period.seconds(30).getSeconds());
        assertEquals(400, Period.millis(400).getMillis());
    }

    // Tests fieldDifference with matching LocalDate instances
    @Test
    public void testFieldDifference_validLocalDate_calculatesDifference() {
        LocalDate start = new LocalDate(2020, 1, 15);
        LocalDate end = new LocalDate(2022, 5, 20);
        Period diff = Period.fieldDifference(start, end);
        assertEquals(2, diff.getYears());
        assertEquals(4, diff.getMonths());
        assertEquals(5, diff.getDays());
    }

    // Tests fieldDifference with null start argument
    @Test(expected = IllegalArgumentException.class)
    public void testFieldDifference_nullStart_throwsException() {
        LocalDate end = new LocalDate(2022, 5, 20);
        Period.fieldDifference(null, end);
    }

    // Tests fieldDifference with partials having mismatched sizes
    @Test(expected = IllegalArgumentException.class)
    public void testFieldDifference_mismatchedPartials_throwsException() {
        LocalDate start = new LocalDate(2020, 1, 15);
        LocalTime end = new LocalTime(12, 30);
        Period.fieldDifference(start, end);
    }

    // Tests withPeriodType when the same PeriodType is requested
    @Test
    public void testWithPeriodType_samePeriodType_returnsSameInstance() {
        Period period = Period.days(5);
        assertSame(period, period.withPeriodType(PeriodType.standard()));
    }

    // Tests withFields copying non-null period fields
    @Test
    public void testWithFields_validPeriod_updatesFields() {
        Period base = new Period(1, 2, 0, 0, 5, 0, 0, 0);
        Period override = new Period(0, 0, 3, 4, 0, 0, 0, 0);
        Period result = base.withFields(override);
        assertEquals(1, result.getYears());
        assertEquals(2, result.getMonths());
        assertEquals(3, result.getWeeks());
        assertEquals(4, result.getDays());
        assertEquals(5, result.getHours());
    }

    // Tests withField setting a specific DurationFieldType
    @Test
    public void testWithField_validField_updatesValue() {
        Period period = Period.hours(2);
        Period updated = period.withField(DurationFieldType.minutes(), 45);
        assertEquals(2, updated.getHours());
        assertEquals(45, updated.getMinutes());
    }

    // Tests withField with null DurationFieldType throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWithField_nullField_throwsException() {
        Period period = Period.hours(2);
        period.withField(null, 10);
    }

    // Tests withFieldAdded with zero value returning same instance
    @Test
    public void testWithFieldAdded_zeroValue_returnsSameInstance() {
        Period period = Period.hours(2);
        assertSame(period, period.withFieldAdded(DurationFieldType.hours(), 0));
    }

    // Tests arithmetic plus and minus methods
    @Test
    public void testPlusAndMinus_validPeriods_correctCalculation() {
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
    }

    // Tests plus with null returning same instance
    @Test
    public void testPlus_nullPeriod_returnsSameInstance() {
        Period period = Period.days(3);
        assertSame(period, period.plus(null));
    }

    // Tests minus with null returning same instance
    @Test
    public void testMinus_nullPeriod_returnsSameInstance() {
        Period period = Period.days(3);
        assertSame(period, period.minus(null));
    }

    // Tests multipliedBy and negated operations
    @Test
    public void testMultipliedByAndNegated_variousScalars_correctValues() {
        Period period = new Period(1, -2, 3, -4, 5, -6, 7, -8);

        assertSame(Period.ZERO, Period.ZERO.multipliedBy(5));
        assertSame(period, period.multipliedBy(1));

        Period multiplied = period.multipliedBy(2);
        assertEquals(2, multiplied.getYears());
        assertEquals(-4, multiplied.getMonths());
        assertEquals(6, multiplied.getWeeks());
        assertEquals(-8, multiplied.getDays());

        Period negated = period.negated();
        assertEquals(-1, negated.getYears());
        assertEquals(2, negated.getMonths());
        assertEquals(-3, negated.getWeeks());
        assertEquals(4, negated.getDays());
    }

    // Tests toStandardDays conversion
    @Test
    public void testToStandardDays_standardTimePeriod_convertsCorrectly() {
        Period period = new Period(0, 0, 2, 3, 48, 0, 0, 0);
        Days days = period.toStandardDays();
        assertEquals(19, days.getDays());
    }

    // Tests toStandardSeconds with unsupported years field throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testToStandardSeconds_withYears_throwsException() {
        Period period = Period.years(1).withSeconds(30);
        period.toStandardSeconds();
    }

    // Tests parse method using ISO format
    @Test
    public void testParse_standardIsoString_parsesCorrectly() {
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

    // Tests constructors with different parameters
    @Test
    public void testConstructors_variousParameters() {
        Period p0 = new Period();
        assertEquals(0, p0.getYears());

        Period p1 = new Period(1, 2, 3, 4);
        assertEquals(1, p1.getHours());
        assertEquals(2, p1.getMinutes());
        assertEquals(3, p1.getSeconds());
        assertEquals(4, p1.getMillis());

        Period p2 = new Period(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        assertEquals(1, p2.getYears());
        assertEquals(8, p2.getMillis());

        Period p3 = new Period(1000L);
        assertEquals(1, p3.getSeconds());

        Period p4 = new Period(1000L, PeriodType.standard());
        assertEquals(1, p4.getSeconds());

        Period p5 = new Period(1000L, ISOChronology.getInstanceUTC());
        assertEquals(1, p5.getSeconds());

        Period p6 = new Period(1000L, PeriodType.standard(), ISOChronology.getInstanceUTC());
        assertEquals(1, p6.getSeconds());

        Period p7 = new Period(0L, 2000L);
        assertEquals(2, p7.getSeconds());

        Period p8 = new Period(0L, 2000L, PeriodType.standard());
        assertEquals(2, p8.getSeconds());

        Period p9 = new Period(0L, 2000L, ISOChronology.getInstanceUTC());
        assertEquals(2, p9.getSeconds());

        Period p10 = new Period(0L, 2000L, PeriodType.standard(), ISOChronology.getInstanceUTC());
        assertEquals(2, p10.getSeconds());

        Instant start = new Instant(0L);
        Instant end = new Instant(3000L);
        Duration dur = new Duration(3000L);

        Period p11 = new Period(start, end);
        assertEquals(3, p11.getSeconds());

        Period p12 = new Period(start, end, PeriodType.standard());
        assertEquals(3, p12.getSeconds());

        Period p13 = new Period(start, dur);
        assertEquals(3, p13.getSeconds());

        Period p14 = new Period(start, dur, PeriodType.standard());
        assertEquals(3, p14.getSeconds());

        Period p15 = new Period(dur, end);
        assertEquals(3, p15.getSeconds());

        Period p16 = new Period(dur, end, PeriodType.standard());
        assertEquals(3, p16.getSeconds());

        Period p17 = new Period("PT5S");
        assertEquals(5, p17.getSeconds());

        Period p18 = new Period("PT5S", PeriodType.standard());
        assertEquals(5, p18.getSeconds());

        Period p19 = new Period("PT5S", ISOChronology.getInstanceUTC());
        assertEquals(5, p19.getSeconds());

        Period p20 = new Period("PT5S", PeriodType.standard(), ISOChronology.getInstanceUTC());
        assertEquals(5, p20.getSeconds());

        LocalDate ld1 = new LocalDate(2020, 1, 1);
        LocalDate ld2 = new LocalDate(2020, 1, 5);
        Period p21 = new Period(ld1, ld2);
        assertEquals(4, p21.getDays());

        Period p22 = new Period(ld1, ld2, PeriodType.days());
        assertEquals(4, p22.getDays());
    }

    // Tests individual field setters and getters
    @Test
    public void testWithIndividualFields_createsUpdatedPeriod() {
        Period p = new Period();
        assertEquals(1, p.withYears(1).getYears());
        assertEquals(2, p.withMonths(2).getMonths());
        assertEquals(3, p.withWeeks(3).getWeeks());
        assertEquals(4, p.withDays(4).getDays());
        assertEquals(5, p.withHours(5).getHours());
        assertEquals(6, p.withMinutes(6).getMinutes());
        assertEquals(7, p.withSeconds(7).getSeconds());
        assertEquals(8, p.withMillis(8).getMillis());

        assertSame(p, p.withYears(0));
        assertSame(p, p.withMonths(0));
        assertSame(p, p.withWeeks(0));
        assertSame(p, p.withDays(0));
        assertSame(p, p.withHours(0));
        assertSame(p, p.withMinutes(0));
        assertSame(p, p.withSeconds(0));
        assertSame(p, p.withMillis(0));
    }

    // Tests plus individual fields
    @Test
    public void testPlusIndividualFields_addsCorrectly() {
        Period p = new Period();
        assertEquals(1, p.plusYears(1).getYears());
        assertEquals(2, p.plusMonths(2).getMonths());
        assertEquals(3, p.plusWeeks(3).getWeeks());
        assertEquals(4, p.plusDays(4).getDays());
        assertEquals(5, p.plusHours(5).getHours());
        assertEquals(6, p.plusMinutes(6).getMinutes());
        assertEquals(7, p.plusSeconds(7).getSeconds());
        assertEquals(8, p.plusMillis(8).getMillis());

        assertSame(p, p.plusYears(0));
        assertSame(p, p.plusMonths(0));
        assertSame(p, p.plusWeeks(0));
        assertSame(p, p.plusDays(0));
        assertSame(p, p.plusHours(0));
        assertSame(p, p.plusMinutes(0));
        assertSame(p, p.plusSeconds(0));
        assertSame(p, p.plusMillis(0));
    }

    // Tests minus individual fields
    @Test
    public void testMinusIndividualFields_subtractsCorrectly() {
        Period p = new Period();
        assertEquals(-1, p.minusYears(1).getYears());
        assertEquals(-2, p.minusMonths(2).getMonths());
        assertEquals(-3, p.minusWeeks(3).getWeeks());
        assertEquals(-4, p.minusDays(4).getDays());
        assertEquals(-5, p.minusHours(5).getHours());
        assertEquals(-6, p.minusMinutes(6).getMinutes());
        assertEquals(-7, p.minusSeconds(7).getSeconds());
        assertEquals(-8, p.minusMillis(8).getMillis());

        assertSame(p, p.minusYears(0));
        assertSame(p, p.minusMonths(0));
        assertSame(p, p.minusWeeks(0));
        assertSame(p, p.minusDays(0));
        assertSame(p, p.minusHours(0));
        assertSame(p, p.minusMinutes(0));
        assertSame(p, p.minusSeconds(0));
        assertSame(p, p.minusMillis(0));
    }

    // Tests standard duration conversions
    @Test
    public void testToStandardConversions_validPeriod() {
        Period p = new Period(0, 0, 1, 2, 3, 4, 5, 6);
        assertEquals(1, p.toStandardWeeks().getWeeks());
        assertEquals(9, p.toStandardDays().getDays());
        assertEquals(219, p.toStandardHours().getHours());
        assertEquals(13144, p.toStandardMinutes().getMinutes());
        assertEquals(788645, p.toStandardSeconds().getSeconds());
        assertEquals(788645006L, p.toStandardDuration().getMillis());
    }

    // Tests toStandardWeeks exception on unsupported months
    @Test(expected = UnsupportedOperationException.class)
    public void testToStandardWeeks_withMonths_throwsException() {
        Period.months(1).toStandardWeeks();
    }

    // Tests toStandardHours exception on unsupported months
    @Test(expected = UnsupportedOperationException.class)
    public void testToStandardHours_withMonths_throwsException() {
        Period.months(1).toStandardHours();
    }

    // Tests toStandardMinutes exception on unsupported months
    @Test(expected = UnsupportedOperationException.class)
    public void testToStandardMinutes_withMonths_throwsException() {
        Period.months(1).toStandardMinutes();
    }

    // Tests toStandardDuration exception on unsupported months
    @Test(expected = UnsupportedOperationException.class)
    public void testToStandardDuration_withMonths_throwsException() {
        Period.months(1).toStandardDuration();
    }

    // Tests toPeriod returns same instance
    @Test
    public void testToPeriod_returnsSame() {
        Period p = Period.hours(5);
        assertSame(p, p.toPeriod());
    }

    // Tests withFields null returns same instance
    @Test
    public void testWithFields_nullPeriod_returnsSameInstance() {
        Period p = Period.hours(5);
        assertSame(p, p.withFields(null));
    }

    // Tests withPeriodType converting type
    @Test
    public void testWithPeriodType_differentType_converts() {
        Period p = new Period(0, 0, 1, 2, 0, 0, 0, 0);
        Period converted = p.withPeriodType(PeriodType.days());
        assertEquals(9, converted.getDays());
    }

    // Tests parse with custom formatter
    @Test
    public void testParse_withFormatter() {
        PeriodFormatter formatter = ISOPeriodFormat.standard();
        Period p = Period.parse("P2Y3M", formatter);
        assertEquals(2, p.getYears());
        assertEquals(3, p.getMonths());
    }

    // Tests fieldDifference with null end argument
    @Test(expected = IllegalArgumentException.class)
    public void testFieldDifference_nullEnd_throwsException() {
        LocalDate start = new LocalDate(2022, 5, 20);
        Period.fieldDifference(start, null);
    }

    // Tests fieldDifference with different field types
    @Test(expected = IllegalArgumentException.class)
    public void testFieldDifference_differentFieldTypes_throwsException() {
        YearMonth ym = new YearMonth(2020, 1);
        MonthDay md = new MonthDay(1, 15);
        Period.fieldDifference(ym, md);
    }

    // Tests withFieldAdded with non-zero value
    @Test
    public void testWithFieldAdded_nonZeroValue_addsCorrectly() {
        Period period = Period.hours(2);
        Period updated = period.withFieldAdded(DurationFieldType.hours(), 3);
        assertEquals(5, updated.getHours());
    }

    // Tests withFieldAdded with null field throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testWithFieldAdded_nullField_throwsException() {
        Period period = Period.hours(2);
        period.withFieldAdded(null, 3);
    }
}