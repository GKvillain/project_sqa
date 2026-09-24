package org.joda.time.base;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.Duration;
import org.joda.time.DurationFieldType;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.ReadableDuration;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.chrono.ISOChronology;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class BasePeriodTest {

    private static class MockBasePeriod extends BasePeriod {
        private static final long serialVersionUID = 1L;

        MockBasePeriod(int years, int months, int weeks, int days,
                       int hours, int minutes, int seconds, int millis,
                       PeriodType type) {
            super(years, months, weeks, days, hours, minutes, seconds, millis, type);
        }

        MockBasePeriod(long startInstant, long endInstant, PeriodType type, Chronology chrono) {
            super(startInstant, endInstant, type, chrono);
        }

        MockBasePeriod(ReadableInstant startInstant, ReadableInstant endInstant, PeriodType type) {
            super(startInstant, endInstant, type);
        }

        MockBasePeriod(ReadablePartial start, ReadablePartial end, PeriodType type) {
            super(start, end, type);
        }

        MockBasePeriod(ReadableInstant startInstant, ReadableDuration duration, PeriodType type) {
            super(startInstant, duration, type);
        }

        MockBasePeriod(ReadableDuration duration, ReadableInstant endInstant, PeriodType type) {
            super(duration, endInstant, type);
        }

        MockBasePeriod(long duration) {
            super(duration);
        }

        MockBasePeriod(long duration, PeriodType type, Chronology chrono) {
            super(duration, type, chrono);
        }

        MockBasePeriod(Object period, PeriodType type, Chronology chrono) {
            super(period, type, chrono);
        }

        MockBasePeriod(int[] values, PeriodType type) {
            super(values, type);
        }

        @Override
        public void setField(DurationFieldType field, int value) {
            super.setField(field, value);
        }

        @Override
        public void addField(DurationFieldType field, int value) {
            super.addField(field, value);
        }

        @Override
        public void mergePeriod(org.joda.time.ReadablePeriod period) {
            super.mergePeriod(period);
        }

        @Override
        public void addPeriod(org.joda.time.ReadablePeriod period) {
            super.addPeriod(period);
        }

        @Override
        public void setValue(int index, int value) {
            super.setValue(index, value);
        }

        @Override
        public void setValues(int[] values) {
            super.setValues(values);
        }
    }

    // Tests duration constructor with standard period type calculation
    @Test
    public void testConstructor_longDuration_storesValues() {
        long duration = 4 * 60 * 60 * 1000L + 5 * 60 * 1000L + 6 * 1000L + 7L;
        MockBasePeriod period = new MockBasePeriod(duration);
        assertEquals(4, period.get(DurationFieldType.hours()));
        assertEquals(5, period.get(DurationFieldType.minutes()));
        assertEquals(6, period.get(DurationFieldType.seconds()));
        assertEquals(7, period.get(DurationFieldType.millis()));
        assertEquals(0, period.get(DurationFieldType.days()));
        assertEquals(0, period.get(DurationFieldType.weeks()));
        assertEquals(0, period.get(DurationFieldType.months()));
        assertEquals(0, period.get(DurationFieldType.years()));
    }

    // Tests constructor with all 8 field values and standard type
    @Test
    public void testConstructor_allFields_returnsCorrectValues() {
        MockBasePeriod period = new MockBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        assertEquals(PeriodType.standard(), period.getPeriodType());
        assertEquals(1, period.getYears());
        assertEquals(2, period.getMonths());
        assertEquals(3, period.getWeeks());
        assertEquals(4, period.getDays());
        assertEquals(5, period.getHours());
        assertEquals(6, period.getMinutes());
        assertEquals(7, period.getSeconds());
        assertEquals(8, period.getMillis());
    }

    // Tests exception when non-zero value is supplied for unsupported field
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_unsupportedFieldNonZero_throwsException() {
        new MockBasePeriod(1, 0, 0, 0, 0, 0, 0, 0, PeriodType.time());
    }

    // Tests constructor with start and end timestamps in milliseconds
    @Test
    public void testConstructor_longInstantsAndChronology_calculatesPeriod() {
        long start = 1000000000L;
        long end = start + (2 * 3600 + 30 * 60 + 15) * 1000L;
        MockBasePeriod period = new MockBasePeriod(start, end, PeriodType.time(), ISOChronology.getInstanceUTC());
        assertEquals(2, period.getHours());
        assertEquals(30, period.getMinutes());
        assertEquals(15, period.getSeconds());
    }

    // Tests constructor with null ReadableInstant parameters
    @Test
    public void testConstructor_nullReadableInstants_initializesZeroValues() {
        MockBasePeriod period = new MockBasePeriod((ReadableInstant) null, (ReadableInstant) null, PeriodType.standard());
        assertEquals(PeriodType.standard(), period.getPeriodType());
        for (int i = 0; i < period.size(); i++) {
            assertEquals(0, period.getValue(i));
        }
    }

    // Tests constructor with valid ReadableInstant parameters
    @Test
    public void testConstructor_validReadableInstants_calculatesPeriod() {
        DateTime start = new DateTime(2020, 1, 1, 10, 0, 0, 0);
        DateTime end = new DateTime(2020, 1, 1, 12, 30, 0, 0);
        MockBasePeriod period = new MockBasePeriod(start, end, PeriodType.standard());
        assertEquals(2, period.getHours());
        assertEquals(30, period.getMinutes());
    }

    // Tests constructor with BaseLocal partials (LocalDate)
    @Test
    public void testConstructor_baseLocalPartials_calculatesPeriod() {
        LocalDate start = new LocalDate(2020, 1, 1);
        LocalDate end = new LocalDate(2021, 3, 10);
        MockBasePeriod period = new MockBasePeriod(start, end, PeriodType.yearMonthDay());
        assertEquals(1, period.getYears());
        assertEquals(2, period.getMonths());
        assertEquals(9, period.getDays());
    }

    // Tests exception when start partial is null
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullStartPartial_throwsException() {
        LocalDate end = new LocalDate(2020, 1, 1);
        new MockBasePeriod(null, end, PeriodType.standard());
    }

    // Tests exception when partials have mismatched field types
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_mismatchedPartials_throwsException() {
        LocalDate start = new LocalDate(2020, 1, 1);
        LocalTime end = new LocalTime(12, 0);
        new MockBasePeriod(start, end, PeriodType.standard());
    }

    // Tests constructor with start instant and duration
    @Test
    public void testConstructor_instantAndDuration_calculatesPeriod() {
        DateTime start = new DateTime(2020, 1, 1, 10, 0, 0, 0);
        Duration duration = new Duration(3600000L);
        MockBasePeriod period = new MockBasePeriod(start, duration, PeriodType.time());
        assertEquals(1, period.getHours());
    }

    // Tests constructor with duration and end instant
    @Test
    public void testConstructor_durationAndInstant_calculatesPeriod() {
        DateTime end = new DateTime(2020, 1, 1, 10, 0, 0, 0);
        Duration duration = new Duration(7200000L);
        MockBasePeriod period = new MockBasePeriod(duration, end, PeriodType.time());
        assertEquals(2, period.getHours());
    }

    // Tests constructor from another Period object via converter
    @Test
    public void testConstructor_objectPeriod_convertsCorrectly() {
        Period original = new Period(1, 2, 0, 4, 5, 6, 7, 8);
        MockBasePeriod period = new MockBasePeriod(original, PeriodType.standard(), null);
        assertEquals(1, period.getYears());
        assertEquals(2, period.getMonths());
        assertEquals(4, period.getDays());
    }

    // Tests constructor with int array and PeriodType
    @Test
    public void testConstructor_intValuesAndType_setsValuesDirectly() {
        int[] values = new int[]{1, 2, 3, 4, 5, 6, 7, 8};
        MockBasePeriod period = new MockBasePeriod(values, PeriodType.standard());
        assertEquals(8, period.size());
        assertEquals(1, period.getValue(0));
        assertEquals(8, period.getValue(7));
        assertEquals(DurationFieldType.years(), period.getFieldType(0));
    }

    // Tests toDurationFrom calculation relative to an instant
    @Test
    public void testToDurationFrom_validInstant_returnsCorrectDuration() {
        MockBasePeriod period = new MockBasePeriod(0, 0, 0, 1, 2, 0, 0, 0, PeriodType.standard());
        DateTime start = new DateTime(2020, 1, 1, 0, 0, 0, 0);
        Duration duration = period.toDurationFrom(start);
        assertEquals((24 + 2) * 3600 * 1000L, duration.getMillis());
    }

    // Tests toDurationTo calculation relative to an instant
    @Test
    public void testToDurationTo_validInstant_returnsCorrectDuration() {
        MockBasePeriod period = new MockBasePeriod(0, 0, 0, 0, 1, 30, 0, 0, PeriodType.standard());
        DateTime end = new DateTime(2020, 1, 1, 12, 0, 0, 0);
        Duration duration = period.toDurationTo(end);
        assertEquals((60 + 30) * 60 * 1000L, duration.getMillis());
    }

    // Tests setField and addField operations on supported and unsupported fields
    @Test
    public void testSetFieldAndAddField_validValues_updatesCorrectly() {
        MockBasePeriod period = new MockBasePeriod(0, 0, 0, 0, 1, 10, 0, 0, PeriodType.time());
        period.setField(DurationFieldType.hours(), 3);
        assertEquals(3, period.getHours());
        period.addField(DurationFieldType.hours(), 2);
        assertEquals(5, period.getHours());
    }

    // Tests exception when setField is called with unsupported field and non-zero value
    @Test(expected = IllegalArgumentException.class)
    public void testSetField_unsupportedField_throwsException() {
        MockBasePeriod period = new MockBasePeriod(0, 0, 0, 0, 1, 0, 0, 0, PeriodType.time());
        period.setField(DurationFieldType.years(), 5);
    }

    // Tests mergePeriod and addPeriod operations
    @Test
    public void testMergePeriodAndAddPeriod_validPeriods_updatesValues() {
        MockBasePeriod period = new MockBasePeriod(1, 2, 0, 0, 0, 0, 0, 0, PeriodType.standard());
        Period toMerge = new Period(0, 5, 0, 3, 0, 0, 0, 0);
        period.mergePeriod(toMerge);
        assertEquals(1, period.getYears());
        assertEquals(5, period.getMonths());
        assertEquals(3, period.getDays());

        Period toAdd = new Period(2, 1, 0, 0, 0, 0, 0, 0);
        period.addPeriod(toAdd);
        assertEquals(3, period.getYears());
        assertEquals(6, period.getMonths());
    }

    // Tests setValue and setValues directly
    @Test
    public void testSetValueAndSetValues_validValues_updatesDirectly() {
        MockBasePeriod period = new MockBasePeriod(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.standard());
        period.setValue(0, 10);
        assertEquals(10, period.getValue(0));

        int[] newValues = new int[]{1, 1, 1, 1, 1, 1, 1, 1};
        period.setValues(newValues);
        for (int i = 0; i < period.size(); i++) {
            assertEquals(1, period.getValue(i));
        }
    }
}