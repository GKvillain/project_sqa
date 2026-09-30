package org.joda.time.base;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.DurationFieldType;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Partial;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.ReadableDuration;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.ReadablePeriod;
import org.joda.time.YearMonth;
import org.joda.time.chrono.ISOChronology;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class BasePeriodTest {

    private static class StubBasePeriod extends BasePeriod {
        private static final long serialVersionUID = 1L;

        StubBasePeriod(int years, int months, int weeks, int days,
                       int hours, int minutes, int seconds, int millis, PeriodType type) {
            super(years, months, weeks, days, hours, minutes, seconds, millis, type);
        }

        StubBasePeriod(long startInstant, long endInstant, PeriodType type, Chronology chrono) {
            super(startInstant, endInstant, type, chrono);
        }

        StubBasePeriod(ReadableInstant startInstant, ReadableInstant endInstant, PeriodType type) {
            super(startInstant, endInstant, type);
        }

        StubBasePeriod(ReadablePartial start, ReadablePartial end, PeriodType type) {
            super(start, end, type);
        }

        StubBasePeriod(ReadableInstant startInstant, ReadableDuration duration, PeriodType type) {
            super(startInstant, duration, type);
        }

        StubBasePeriod(ReadableDuration duration, ReadableInstant endInstant, PeriodType type) {
            super(duration, endInstant, type);
        }

        StubBasePeriod(long duration) {
            super(duration);
        }

        StubBasePeriod(long duration, PeriodType type, Chronology chrono) {
            super(duration, type, chrono);
        }

        StubBasePeriod(Object period, PeriodType type, Chronology chrono) {
            super(period, type, chrono);
        }

        StubBasePeriod(int[] values, PeriodType type) {
            super(values, type);
        }

        public void publicSetField(DurationFieldType field, int value) {
            setField(field, value);
        }

        public void publicAddField(DurationFieldType field, int value) {
            addField(field, value);
        }

        public void publicMergePeriod(ReadablePeriod period) {
            mergePeriod(period);
        }

        public void publicAddPeriod(ReadablePeriod period) {
            addPeriod(period);
        }

        public void publicSetPeriod(ReadablePeriod period) {
            setPeriod(period);
        }

        public void publicSetPeriod(int years, int months, int weeks, int days,
                                    int hours, int minutes, int seconds, int millis) {
            setPeriod(years, months, weeks, days, hours, minutes, seconds, millis);
        }

        public void publicSetValue(int index, int value) {
            setValue(index, value);
        }

        public void publicSetValues(int[] values) {
            setValues(values);
        }
    }

    // Tests constructor with long duration for 0 milliseconds
    @Test
    public void testConstructor_longDurationZero_allZeroValues() {
        StubBasePeriod period = new StubBasePeriod(0L);
        assertEquals(PeriodType.standard(), period.getPeriodType());
        assertEquals(8, period.size());
        assertEquals(0, period.getValue(0)); // years
        assertEquals(0, period.getValue(1)); // months
        assertEquals(0, period.getValue(2)); // weeks
        assertEquals(0, period.getValue(3)); // days
        assertEquals(0, period.getValue(4)); // hours
        assertEquals(0, period.getValue(5)); // minutes
        assertEquals(0, period.getValue(6)); // seconds
        assertEquals(0, period.getValue(7)); // millis
    }

    // Tests 8-integer constructor with valid values
    @Test
    public void testConstructor_eightInts_setsAllFields() {
        StubBasePeriod period = new StubBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        assertEquals(1, period.getValue(0));
        assertEquals(2, period.getValue(1));
        assertEquals(3, period.getValue(2));
        assertEquals(4, period.getValue(3));
        assertEquals(5, period.getValue(4));
        assertEquals(6, period.getValue(5));
        assertEquals(7, period.getValue(6));
        assertEquals(8, period.getValue(7));
    }

    // Tests 8-integer constructor with unsupported field non-zero throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_eightIntsUnsupportedFieldNonZero_throwsException() {
        new StubBasePeriod(1, 0, 0, 0, 0, 0, 0, 0, PeriodType.time());
    }

    // Tests ReadableInstant start and end constructor with both nulls
    @Test
    public void testConstructor_instantsNullBoth_initializesZeros() {
        StubBasePeriod period = new StubBasePeriod((ReadableInstant) null, (ReadableInstant) null, PeriodType.standard());
        assertEquals(8, period.size());
        for (int i = 0; i < period.size(); i++) {
            assertEquals(0, period.getValue(i));
        }
    }

    // Tests ReadableInstant start and end constructor with non-null instants
    @Test
    public void testConstructor_instantsNonNull_calculatesInterval() {
        Instant start = new Instant(1000L);
        Instant end = new Instant(61000L);
        StubBasePeriod period = new StubBasePeriod(start, end, PeriodType.standard());
        assertEquals(1, period.getValue(5)); // 1 minute
    }

    // Tests ReadablePartial constructor with BaseLocal instances (LocalDate)
    @Test
    public void testConstructor_partialsBaseLocal_calculatesPeriod() {
        LocalDate start = new LocalDate(2020, 1, 1);
        LocalDate end = new LocalDate(2021, 3, 5);
        StubBasePeriod period = new StubBasePeriod(start, end, PeriodType.yearMonthDay());
        assertEquals(1, period.getValue(0)); // 1 year
        assertEquals(2, period.getValue(1)); // 2 months
        assertEquals(4, period.getValue(2)); // 4 days
    }

    // Tests ReadablePartial constructor when start is null
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_partialsNullStart_throwsException() {
        LocalDate end = new LocalDate(2021, 1, 1);
        new StubBasePeriod((ReadablePartial) null, end, PeriodType.standard());
    }

    // Tests ReadablePartial constructor when partials have different field types
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_partialsDifferentFieldTypes_throwsException() {
        LocalDate start = new LocalDate(2020, 1, 1);
        LocalTime end = new LocalTime(12, 0);
        new StubBasePeriod(start, end, PeriodType.standard());
    }

    // Tests constructor with ReadableInstant and ReadableDuration
    @Test
    public void testConstructor_startInstantAndDuration_calculatesPeriod() {
        Instant start = new Instant(0L);
        Duration duration = new Duration(3600000L); // 1 hour
        StubBasePeriod period = new StubBasePeriod(start, duration, PeriodType.time());
        assertEquals(1, period.getValue(0)); // 1 hour
    }

    // Tests constructor with ReadableDuration and ReadableInstant
    @Test
    public void testConstructor_durationAndEndInstant_calculatesPeriod() {
        Instant end = new Instant(3600000L);
        Duration duration = new Duration(3600000L);
        StubBasePeriod period = new StubBasePeriod(duration, end, PeriodType.time());
        assertEquals(1, period.getValue(0)); // 1 hour
    }

    // Tests constructor converting another period object
    @Test
    public void testConstructor_objectPeriod_convertsCorrectly() {
        Period source = Period.hours(5);
        StubBasePeriod period = new StubBasePeriod(source, null, ISOChronology.getInstanceUTC());
        assertEquals(5, period.getValue(4)); // hours field in standard PeriodType
    }

    // Tests toDurationFrom and toDurationTo calculations
    @Test
    public void testToDurationFromAndTo_validInstant_returnsCorrectDuration() {
        StubBasePeriod period = new StubBasePeriod(0, 0, 0, 0, 2, 0, 0, 0, PeriodType.standard());
        DateTime start = new DateTime(2020, 1, 1, 0, 0, DateTimeZone.UTC);
        Duration durationFrom = period.toDurationFrom(start);
        assertEquals(2 * 3600 * 1000L, durationFrom.getMillis());

        Duration durationTo = period.toDurationTo(start);
        assertEquals(2 * 3600 * 1000L, durationTo.getMillis());
    }

    // Tests setField and addField operations
    @Test
    public void testSetFieldAndAddField_validFields_updatesValues() {
        StubBasePeriod period = new StubBasePeriod(new int[8], PeriodType.standard());
        period.publicSetField(DurationFieldType.days(), 5);
        assertEquals(5, period.getValue(3));

        period.publicAddField(DurationFieldType.days(), 3);
        assertEquals(8, period.getValue(3));
    }

    // Tests setField on unsupported field with non-zero value throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetField_unsupportedFieldNonZero_throwsException() {
        StubBasePeriod period = new StubBasePeriod(new int[4], PeriodType.time());
        period.publicSetField(DurationFieldType.years(), 1);
    }

    // Tests addPeriod and mergePeriod methods
    @Test
    public void testAddPeriodAndMergePeriod_validPeriod_updatesValues() {
        StubBasePeriod period = new StubBasePeriod(1, 1, 0, 0, 0, 0, 0, 0, PeriodType.standard());
        Period other = new Period(2, 3, 0, 0, 0, 0, 0, 0);

        period.publicAddPeriod(other);
        assertEquals(3, period.getValue(0)); // 1 + 2 = 3 years
        assertEquals(4, period.getValue(1)); // 1 + 3 = 4 months

        period.publicMergePeriod(Period.years(10));
        assertEquals(10, period.getValue(0));
    }

    // Tests setPeriod and setValues
    @Test
    public void testSetPeriod_nullPeriod_resetsAllValuesToZero() {
        StubBasePeriod period = new StubBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        period.publicSetPeriod((ReadablePeriod) null);
        for (int i = 0; i < period.size(); i++) {
            assertEquals(0, period.getValue(i));
        }
    }

    // Tests setValue and getFieldType bounds
    @Test
    public void testGetFieldTypeAndSetValue_validIndex_returnsExpected() {
        StubBasePeriod period = new StubBasePeriod(new int[8], PeriodType.standard());
        assertEquals(DurationFieldType.years(), period.getFieldType(0));
        period.publicSetValue(0, 42);
        assertEquals(42, period.getValue(0));
    }

    // Tests ReadablePartial constructor when end is null
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_partialsNullEnd_throwsException() {
        LocalDate start = new LocalDate(2020, 1, 1);
        new StubBasePeriod(start, (ReadablePartial) null, PeriodType.standard());
    }

    // Tests ReadablePartial constructor when partials have different sizes
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_partialsDifferentSizes_throwsException() {
        LocalDate start = new LocalDate(2020, 1, 1);
        YearMonth end = new YearMonth(2020, 1);
        new StubBasePeriod(start, end, PeriodType.standard());
    }

    // Tests ReadablePartial constructor with non-BaseLocal Partial
    @Test
    public void testConstructor_partialsNonBaseLocal_calculatesPeriod() {
        Partial p1 = new Partial(org.joda.time.DateTimeFieldType.hourOfDay(), 10);
        Partial p2 = new Partial(org.joda.time.DateTimeFieldType.hourOfDay(), 12);
        StubBasePeriod period = new StubBasePeriod(p1, p2, PeriodType.time());
        assertEquals(2, period.getValue(0)); // 2 hours
    }

    // Tests ReadablePartial constructor with empty partials (zero fields)
    @Test
    public void testConstructor_partialsZeroFields_initializesZeros() {
        Partial p1 = new Partial();
        Partial p2 = new Partial();
        StubBasePeriod period = new StubBasePeriod(p1, p2, PeriodType.standard());
        assertEquals(8, period.size());
        for (int i = 0; i < period.size(); i++) {
            assertEquals(0, period.getValue(i));
        }
    }

    // Tests long start and end constructor
    @Test
    public void testConstructor_longStartEndInstant_calculatesCorrectly() {
        StubBasePeriod period = new StubBasePeriod(1000L, 5000L, PeriodType.standard(), ISOChronology.getInstanceUTC());
        assertEquals(4, period.getValue(6)); // 4 seconds
    }

    // Tests ReadableInstant constructor with start non-null and end null
    @Test
    public void testConstructor_startNonNullEndNull_calculatesInterval() {
        Instant start = new Instant(DateTimeZone.getDefault());
        StubBasePeriod period = new StubBasePeriod(start, (ReadableInstant) null, PeriodType.standard());
        assertNotNull(period);
    }

    // Tests ReadableInstant constructor with start null and end non-null
    @Test
    public void testConstructor_startNullEndNonNull_calculatesInterval() {
        Instant end = new Instant(DateTimeZone.getDefault());
        StubBasePeriod period = new StubBasePeriod((ReadableInstant) null, end, PeriodType.standard());
        assertNotNull(period);
    }

    // Tests constructor with ReadableInstant and null ReadableDuration
    @Test
    public void testConstructor_startInstantAndNullDuration_initializesZeros() {
        Instant start = new Instant(0L);
        StubBasePeriod period = new StubBasePeriod(start, (ReadableDuration) null, PeriodType.standard());
        assertEquals(0, period.getValue(0));
    }

    // Tests constructor with null ReadableDuration and ReadableInstant
    @Test
    public void testConstructor_nullDurationAndEndInstant_initializesZeros() {
        Instant end = new Instant(0L);
        StubBasePeriod period = new StubBasePeriod((ReadableDuration) null, end, PeriodType.standard());
        assertEquals(0, period.getValue(0));
    }

    // Tests constructor with null Object period
    @Test
    public void testConstructor_objectNull_initializesZeros() {
        StubBasePeriod period = new StubBasePeriod((Object) null, PeriodType.standard(), ISOChronology.getInstanceUTC());
        assertEquals(0, period.getValue(0));
    }

    // Tests constructor with string duration
    @Test
    public void testConstructor_objectString_convertsCorrectly() {
        StubBasePeriod period = new StubBasePeriod("PT1H", PeriodType.standard(), null);
        assertEquals(1, period.getValue(4)); // 1 hour
    }

    // Tests constructor with duration, PeriodType and Chronology
    @Test
    public void testConstructor_longDurationTypeAndChrono_calculatesPeriod() {
        StubBasePeriod period = new StubBasePeriod(3600000L, PeriodType.standard(), ISOChronology.getInstanceUTC());
        assertEquals(1, period.getValue(4)); // 1 hour
    }

    // Tests publicSetPeriod with 8 integer values
    @Test
    public void testSetPeriod_eightInts_setsValues() {
        StubBasePeriod period = new StubBasePeriod(new int[8], PeriodType.standard());
        period.publicSetPeriod(8, 7, 6, 5, 4, 3, 2, 1);
        assertEquals(8, period.getValue(0));
        assertEquals(7, period.getValue(1));
        assertEquals(6, period.getValue(2));
        assertEquals(5, period.getValue(3));
        assertEquals(4, period.getValue(4));
        assertEquals(3, period.getValue(5));
        assertEquals(2, period.getValue(6));
        assertEquals(1, period.getValue(7));
    }

    // Tests publicSetPeriod with 8 ints on unsupported field non-zero throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetPeriod_eightIntsUnsupportedFieldNonZero_throwsException() {
        StubBasePeriod period = new StubBasePeriod(new int[4], PeriodType.time());
        period.publicSetPeriod(1, 0, 0, 0, 0, 0, 0, 0);
    }

    // Tests setField on unsupported field with zero value does not throw
    @Test
    public void testSetField_unsupportedFieldZero_doesNotThrow() {
        StubBasePeriod period = new StubBasePeriod(new int[4], PeriodType.time());
        period.publicSetField(DurationFieldType.years(), 0);
    }

    // Tests addField with zero value does not alter values
    @Test
    public void testAddField_zeroValue_noOp() {
        StubBasePeriod period = new StubBasePeriod(new int[8], PeriodType.standard());
        period.publicAddField(DurationFieldType.years(), 0);
        assertEquals(0, period.getValue(0));
    }

    // Tests addField on unsupported field with zero value does not throw
    @Test
    public void testAddField_unsupportedFieldZero_doesNotThrow() {
        StubBasePeriod period = new StubBasePeriod(new int[4], PeriodType.time());
        period.publicAddField(DurationFieldType.years(), 0);
    }

    // Tests addField on unsupported field with non-zero value throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testAddField_unsupportedFieldNonZero_throwsException() {
        StubBasePeriod period = new StubBasePeriod(new int[4], PeriodType.time());
        period.publicAddField(DurationFieldType.years(), 1);
    }

    // Tests mergePeriod with null period is a no-op
    @Test
    public void testMergePeriod_null_noOp() {
        StubBasePeriod period = new StubBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        period.publicMergePeriod(null);
        assertEquals(1, period.getValue(0));
    }

    // Tests addPeriod with null period is a no-op
    @Test
    public void testAddPeriod_null_noOp() {
        StubBasePeriod period = new StubBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        period.publicAddPeriod(null);
        assertEquals(1, period.getValue(0));
    }

    // Tests setValues sets all period values
    @Test
    public void testSetValues_validArray_updatesValues() {
        StubBasePeriod period = new StubBasePeriod(new int[8], PeriodType.standard());
        period.publicSetValues(new int[]{8, 7, 6, 5, 4, 3, 2, 1});
        assertEquals(8, period.getValue(0));
        assertEquals(1, period.getValue(7));
    }

    // Tests toDurationFrom and toDurationTo with null instant uses current time
    @Test
    public void testToDurationFromAndTo_nullInstant_calculatesDuration() {
        StubBasePeriod period = new StubBasePeriod(0, 0, 0, 0, 1, 0, 0, 0, PeriodType.standard());
        Duration durationFrom = period.toDurationFrom(null);
        assertNotNull(durationFrom);
        Duration durationTo = period.toDurationTo(null);
        assertNotNull(durationTo);
    }
}