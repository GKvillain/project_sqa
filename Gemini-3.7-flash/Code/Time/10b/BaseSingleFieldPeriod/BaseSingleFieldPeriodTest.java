package org.joda.time.base;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationFieldType;
import org.joda.time.Hours;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.MonthDay;
import org.joda.time.Months;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.ReadablePeriod;
import org.joda.time.Weeks;
import org.joda.time.YearMonth;
import org.joda.time.chrono.ISOChronology;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BaseSingleFieldPeriodTest {

    private static class SingleTestPeriod extends BaseSingleFieldPeriod {
        private static final long serialVersionUID = 1L;

        SingleTestPeriod(int period) {
            super(period);
        }

        public void setValuePublic(int value) {
            super.setValue(value);
        }

        @Override
        public DurationFieldType getFieldType() {
            return DurationFieldType.days();
        }

        @Override
        public PeriodType getPeriodType() {
            return PeriodType.days();
        }
    }

    private static class OtherTestPeriod extends BaseSingleFieldPeriod {
        private static final long serialVersionUID = 1L;

        OtherTestPeriod(int period) {
            super(period);
        }

        @Override
        public DurationFieldType getFieldType() {
            return DurationFieldType.hours();
        }

        @Override
        public PeriodType getPeriodType() {
            return PeriodType.hours();
        }
    }

    // Tests between with valid instants
    @Test
    public void testBetween_validInstants_calculatesDifference() {
        ReadableInstant start = new Instant(0L);
        ReadableInstant end = new Instant(2L * DateTimeConstants.MILLIS_PER_DAY);
        int result = BaseSingleFieldPeriod.between(start, end, DurationFieldType.days());
        assertEquals(2, result);
    }

    // Tests between with null start instant
    @Test(expected = IllegalArgumentException.class)
    public void testBetween_nullStartInstant_throwsException() {
        BaseSingleFieldPeriod.between((ReadableInstant) null, new Instant(0L), DurationFieldType.days());
    }

    // Tests between with null end instant
    @Test(expected = IllegalArgumentException.class)
    public void testBetween_nullEndInstant_throwsException() {
        BaseSingleFieldPeriod.between(new Instant(0L), (ReadableInstant) null, DurationFieldType.days());
    }

    // Tests between with null duration field type for instants
    @Test(expected = IllegalArgumentException.class)
    public void testBetween_nullDurationFieldType_throwsException() {
        BaseSingleFieldPeriod.between(new Instant(0L), new Instant(1000L), (DurationFieldType) null);
    }

    // Tests between with valid partials
    @Test
    public void testBetween_validPartials_calculatesDifference() {
        LocalDate start = new LocalDate(2020, 1, 1);
        LocalDate end = new LocalDate(2020, 1, 15);
        int result = BaseSingleFieldPeriod.between(start, end, new SingleTestPeriod(0));
        assertEquals(14, result);
    }

    // Tests between with leap day MonthDay partials (defect detection for setting 1972 base year)
    @Test
    public void testBetween_monthDayLeapYear_calculatesDifference() {
        MonthDay start = new MonthDay(2, 29);
        MonthDay end = new MonthDay(3, 1);
        int result = BaseSingleFieldPeriod.between(start, end, new SingleTestPeriod(0));
        assertEquals(1, result);
    }

    // Tests between with null start partial
    @Test(expected = IllegalArgumentException.class)
    public void testBetween_nullStartPartial_throwsException() {
        BaseSingleFieldPeriod.between((ReadablePartial) null, new LocalDate(2020, 1, 1), new SingleTestPeriod(0));
    }

    // Tests between with null end partial
    @Test(expected = IllegalArgumentException.class)
    public void testBetween_nullEndPartial_throwsException() {
        BaseSingleFieldPeriod.between(new LocalDate(2020, 1, 1), (ReadablePartial) null, new SingleTestPeriod(0));
    }

    // Tests between with null zeroInstance partial
    @Test(expected = IllegalArgumentException.class)
    public void testBetween_nullZeroInstance_throwsException() {
        BaseSingleFieldPeriod.between(new LocalDate(2020, 1, 1), new LocalDate(2020, 1, 15), (ReadablePeriod) null);
    }

    // Tests between with partials of different size
    @Test(expected = IllegalArgumentException.class)
    public void testBetween_differentSizePartials_throwsException() {
        LocalDate start = new LocalDate(2020, 1, 1);
        YearMonth end = new YearMonth(2020, 1);
        BaseSingleFieldPeriod.between(start, end, new SingleTestPeriod(0));
    }

    // Tests between with partials of different field types
    @Test(expected = IllegalArgumentException.class)
    public void testBetween_differentFieldTypesPartials_throwsException() {
        YearMonth start = new YearMonth(2020, 1);
        MonthDay end = new MonthDay(1, 1);
        BaseSingleFieldPeriod.between(start, end, new SingleTestPeriod(0));
    }

    // Tests standardPeriodIn with null period
    @Test
    public void testStandardPeriodIn_nullPeriod_returnsZero() {
        int result = BaseSingleFieldPeriod.standardPeriodIn(null, DateTimeConstants.MILLIS_PER_DAY);
        assertEquals(0, result);
    }

    // Tests standardPeriodIn with precise period
    @Test
    public void testStandardPeriodIn_precisePeriod_calculatesCorrectUnits() {
        Period period = Period.hours(48);
        int result = BaseSingleFieldPeriod.standardPeriodIn(period, DateTimeConstants.MILLIS_PER_DAY);
        assertEquals(2, result);
    }

    // Tests standardPeriodIn with imprecise period
    @Test(expected = IllegalArgumentException.class)
    public void testStandardPeriodIn_imprecisePeriod_throwsException() {
        Period period = Period.months(1);
        BaseSingleFieldPeriod.standardPeriodIn(period, DateTimeConstants.MILLIS_PER_DAY);
    }

    // Tests size method
    @Test
    public void testSize_always_returnsOne() {
        SingleTestPeriod period = new SingleTestPeriod(5);
        assertEquals(1, period.size());
    }

    // Tests getValue and setValue
    @Test
    public void testGetAndSetValue_validIndex_returnsCorrectValue() {
        SingleTestPeriod period = new SingleTestPeriod(10);
        assertEquals(10, period.getValue());
        assertEquals(10, period.getValue(0));

        period.setValuePublic(25);
        assertEquals(25, period.getValue());
        assertEquals(25, period.getValue(0));
    }

    // Tests getValue with invalid index
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValue_invalidIndex_throwsException() {
        SingleTestPeriod period = new SingleTestPeriod(5);
        period.getValue(1);
    }

    // Tests getFieldType at valid and invalid indices
    @Test
    public void testGetFieldType_validIndex_returnsFieldType() {
        SingleTestPeriod period = new SingleTestPeriod(5);
        assertEquals(DurationFieldType.days(), period.getFieldType(0));
    }

    // Tests getFieldType with invalid index
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetFieldType_invalidIndex_throwsException() {
        SingleTestPeriod period = new SingleTestPeriod(5);
        period.getFieldType(1);
    }

    // Tests get for matching and non-matching duration field types
    @Test
    public void testGet_fieldTypeQuery_returnsValueOrZero() {
        SingleTestPeriod period = new SingleTestPeriod(7);
        assertEquals(7, period.get(DurationFieldType.days()));
        assertEquals(0, period.get(DurationFieldType.hours()));
        assertEquals(0, period.get(null));
    }

    // Tests isSupported for matching, non-matching, and null field types
    @Test
    public void testIsSupported_variousFieldTypes_returnsCorrectBoolean() {
        SingleTestPeriod period = new SingleTestPeriod(3);
        assertTrue(period.isSupported(DurationFieldType.days()));
        assertFalse(period.isSupported(DurationFieldType.hours()));
        assertFalse(period.isSupported(null));
    }

    // Tests toPeriod and toMutablePeriod conversions
    @Test
    public void testToPeriodAndToMutablePeriod_validPeriod_convertsCorrectly() {
        SingleTestPeriod period = new SingleTestPeriod(4);
        assertEquals(Period.days(4), period.toPeriod());
        assertEquals(Period.days(4).toMutablePeriod(), period.toMutablePeriod());
    }

    // Tests equals and hashCode
    @Test
    public void testEqualsAndHashCode_variousObjects_returnsCorrectResults() {
        SingleTestPeriod p1 = new SingleTestPeriod(5);
        SingleTestPeriod p2 = new SingleTestPeriod(5);
        SingleTestPeriod p3 = new SingleTestPeriod(6);
        OtherTestPeriod otherType = new OtherTestPeriod(5);

        assertTrue(p1.equals(p1));
        assertTrue(p1.equals(p2));
        assertEquals(p1.hashCode(), p2.hashCode());

        assertFalse(p1.equals(p3));
        assertFalse(p1.equals(otherType));
        assertFalse(p1.equals(null));
        assertFalse(p1.equals("non-period"));
    }

    // Tests compareTo with equal, greater, and lesser periods
    @Test
    public void testCompareTo_sameType_comparesCorrectly() {
        SingleTestPeriod p1 = new SingleTestPeriod(5);
        SingleTestPeriod p2 = new SingleTestPeriod(5);
        SingleTestPeriod p3 = new SingleTestPeriod(10);
        SingleTestPeriod p4 = new SingleTestPeriod(2);

        assertEquals(0, p1.compareTo(p2));
        assertTrue(p1.compareTo(p3) < 0);
        assertTrue(p1.compareTo(p4) > 0);
    }

    // Tests compareTo with different period class
    @Test(expected = ClassCastException.class)
    public void testCompareTo_differentClass_throwsException() {
        SingleTestPeriod p1 = new SingleTestPeriod(5);
        OtherTestPeriod p2 = new OtherTestPeriod(5);
        p1.compareTo(p2);
    }

    // Tests compareTo with null argument
    @Test(expected = NullPointerException.class)
    public void testCompareTo_nullArgument_throwsException() {
        SingleTestPeriod p1 = new SingleTestPeriod(5);
        p1.compareTo(null);
    }
}