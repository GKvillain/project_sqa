package org.joda.time.base;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeUtils;
import org.joda.time.Days;
import org.joda.time.DurationFieldType;
import org.joda.time.Hours;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.MonthDay;
import org.joda.time.Months;
import org.joda.time.MutablePeriod;
import org.joda.time.Partial;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.ReadablePeriod;
import org.joda.time.YearMonth;
import org.joda.time.chrono.ISOChronology;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BaseSingleFieldPeriodTest {

    private static class TestSingleFieldPeriod extends BaseSingleFieldPeriod {
        private static final long serialVersionUID = 1L;

        TestSingleFieldPeriod(int period) {
            super(period);
        }

        public static int between(ReadableInstant start, ReadableInstant end, DurationFieldType field) {
            return BaseSingleFieldPeriod.between(start, end, field);
        }

        public static int between(ReadablePartial start, ReadablePartial end, ReadablePeriod zeroInstance) {
            return BaseSingleFieldPeriod.between(start, end, zeroInstance);
        }

        public static int standardPeriodIn(ReadablePeriod period, long millisPerUnit) {
            return BaseSingleFieldPeriod.standardPeriodIn(period, millisPerUnit);
        }

        @Override
        public DurationFieldType getFieldType() {
            return DurationFieldType.days();
        }

        @Override
        public PeriodType getPeriodType() {
            return PeriodType.days();
        }

        @Override
        public void setValue(int value) {
            super.setValue(value);
        }
    }

    private static class OtherSingleFieldPeriod extends BaseSingleFieldPeriod {
        private static final long serialVersionUID = 1L;

        OtherSingleFieldPeriod(int period) {
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

    // Tests between with valid ReadableInstant objects
    @Test
    public void testBetween_validInstants_returnsDifference() {
        DateTime start = new DateTime(2020, 1, 1, 0, 0, ISOChronology.getInstanceUTC());
        DateTime end = new DateTime(2020, 1, 5, 0, 0, ISOChronology.getInstanceUTC());
        int result = TestSingleFieldPeriod.between(start, end, DurationFieldType.days());
        assertEquals(4, result);
    }

    // Tests between with null start instant throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testBetween_nullStartInstant_throwsException() {
        DateTime end = new DateTime(2020, 1, 5, 0, 0, ISOChronology.getInstanceUTC());
        TestSingleFieldPeriod.between(null, end, DurationFieldType.days());
    }

    // Tests between with null end instant throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testBetween_nullEndInstant_throwsException() {
        DateTime start = new DateTime(2020, 1, 1, 0, 0, ISOChronology.getInstanceUTC());
        TestSingleFieldPeriod.between(start, null, DurationFieldType.days());
    }

    // Tests between with valid ReadablePartial objects
    @Test
    public void testBetween_validPartials_returnsDifference() {
        LocalDate start = new LocalDate(2020, 1, 1);
        LocalDate end = new LocalDate(2020, 1, 10);
        int result = TestSingleFieldPeriod.between(start, end, Days.ZERO);
        assertEquals(9, result);
    }

    // Tests between with MonthDay partials having no year field
    @Test
    public void testBetween_monthDayPartials_returnsDifference() {
        MonthDay start = new MonthDay(2, 1);
        MonthDay end = new MonthDay(3, 1);
        int result = TestSingleFieldPeriod.between(start, end, Months.ZERO);
        assertEquals(1, result);
    }

    // Tests between with null start partial throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testBetween_nullStartPartial_throwsException() {
        LocalDate end = new LocalDate(2020, 1, 10);
        TestSingleFieldPeriod.between(null, end, Days.ZERO);
    }

    // Tests between with null end partial throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testBetween_nullEndPartial_throwsException() {
        LocalDate start = new LocalDate(2020, 1, 1);
        TestSingleFieldPeriod.between(start, null, Days.ZERO);
    }

    // Tests between with mismatched partial sizes throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testBetween_differentSizePartials_throwsException() {
        LocalDate start = new LocalDate(2020, 1, 1);
        YearMonth end = new YearMonth(2020, 1);
        TestSingleFieldPeriod.between(start, end, Days.ZERO);
    }

    // Tests between with mismatched partial field types throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testBetween_differentFieldTypesPartials_throwsException() {
        YearMonth start = new YearMonth(2020, 1);
        MonthDay end = new MonthDay(1, 1);
        TestSingleFieldPeriod.between(start, end, Days.ZERO);
    }

    // Tests between with non-contiguous partials throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testBetween_nonContiguousPartials_throwsException() {
        Partial start = new Partial(new DateTimeFieldType[]{DateTimeFieldType.year(), DateTimeFieldType.dayOfMonth()}, new int[]{2020, 1});
        Partial end = new Partial(new DateTimeFieldType[]{DateTimeFieldType.year(), DateTimeFieldType.dayOfMonth()}, new int[]{2020, 2});
        TestSingleFieldPeriod.between(start, end, Days.ZERO);
    }

    // Tests standardPeriodIn with null period returning zero
    @Test
    public void testStandardPeriodIn_nullPeriod_returnsZero() {
        int result = TestSingleFieldPeriod.standardPeriodIn(null, 1000L);
        assertEquals(0, result);
    }

    // Tests standardPeriodIn with valid standard duration period
    @Test
    public void testStandardPeriodIn_validPeriod_calculatesUnits() {
        Period period = Period.hours(2).withMinutes(30);
        int result = TestSingleFieldPeriod.standardPeriodIn(period, 60000L);
        assertEquals(150, result);
    }

    // Tests standardPeriodIn with imprecise period throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testStandardPeriodIn_imprecisePeriod_throwsException() {
        Period period = Period.months(1);
        TestSingleFieldPeriod.standardPeriodIn(period, 1000L);
    }

    // Tests size, getFieldType and getValue indexed methods
    @Test
    public void testFieldAccess_indexZero_returnsValues() {
        TestSingleFieldPeriod period = new TestSingleFieldPeriod(5);
        assertEquals(1, period.size());
        assertEquals(DurationFieldType.days(), period.getFieldType(0));
        assertEquals(5, period.getValue(0));
        assertEquals(5, period.getValue());
    }

    // Tests getFieldType with invalid index throwing exception
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetFieldType_invalidIndex_throwsException() {
        TestSingleFieldPeriod period = new TestSingleFieldPeriod(5);
        period.getFieldType(1);
    }

    // Tests getValue with invalid index throwing exception
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValue_invalidIndex_throwsException() {
        TestSingleFieldPeriod period = new TestSingleFieldPeriod(5);
        period.getValue(-1);
    }

    // Tests get by DurationFieldType and isSupported
    @Test
    public void testGetAndIsSupported_variousFieldTypes_returnsCorrectStatus() {
        TestSingleFieldPeriod period = new TestSingleFieldPeriod(7);
        assertEquals(7, period.get(DurationFieldType.days()));
        assertEquals(0, period.get(DurationFieldType.hours()));
        assertEquals(0, period.get(null));

        assertTrue(period.isSupported(DurationFieldType.days()));
        assertFalse(period.isSupported(DurationFieldType.hours()));
        assertFalse(period.isSupported(null));
    }

    // Tests setValue modifies the underlying period value
    @Test
    public void testSetValue_updatesPeriodValue() {
        TestSingleFieldPeriod period = new TestSingleFieldPeriod(3);
        period.setValue(8);
        assertEquals(8, period.getValue());
    }

    // Tests conversions to immutable Period and MutablePeriod
    @Test
    public void testToPeriodAndToMutablePeriod_convertsCorrectly() {
        TestSingleFieldPeriod period = new TestSingleFieldPeriod(12);
        Period p = period.toPeriod();
        assertEquals(12, p.getDays());

        MutablePeriod mp = period.toMutablePeriod();
        assertEquals(12, mp.getDays());
    }

    // Tests equals and hashCode contract
    @Test
    public void testEqualsAndHashCode_variousObjects_satisfiesContract() {
        TestSingleFieldPeriod period1 = new TestSingleFieldPeriod(5);
        TestSingleFieldPeriod period2 = new TestSingleFieldPeriod(5);
        TestSingleFieldPeriod period3 = new TestSingleFieldPeriod(10);
        OtherSingleFieldPeriod otherPeriod = new OtherSingleFieldPeriod(5);

        assertTrue(period1.equals(period1));
        assertTrue(period1.equals(period2));
        assertEquals(period1.hashCode(), period2.hashCode());

        assertFalse(period1.equals(period3));
        assertFalse(period1.equals(otherPeriod));
        assertFalse(period1.equals(null));
        assertFalse(period1.equals("non-period-object"));
    }

    // Tests compareTo with equal, greater, and lesser values
    @Test
    public void testCompareTo_sameClassDifferentValues_returnsExpectedOrder() {
        TestSingleFieldPeriod period5 = new TestSingleFieldPeriod(5);
        TestSingleFieldPeriod period10 = new TestSingleFieldPeriod(10);
        TestSingleFieldPeriod period5Copy = new TestSingleFieldPeriod(5);

        assertEquals(0, period5.compareTo(period5Copy));
        assertTrue(period5.compareTo(period10) < 0);
        assertTrue(period10.compareTo(period5) > 0);
    }

    // Tests compareTo with different class throwing ClassCastException
    @Test(expected = ClassCastException.class)
    public void testCompareTo_differentClass_throwsClassCastException() {
        TestSingleFieldPeriod days = new TestSingleFieldPeriod(5);
        OtherSingleFieldPeriod hours = new OtherSingleFieldPeriod(5);
        days.compareTo(hours);
    }

    // Tests compareTo with null throwing NullPointerException
    @Test(expected = NullPointerException.class)
    public void testCompareTo_nullArgument_throwsNullPointerException() {
        TestSingleFieldPeriod period = new TestSingleFieldPeriod(5);
        period.compareTo(null);
    }
}