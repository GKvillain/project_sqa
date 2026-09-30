package org.joda.time.format;

import java.util.Locale;

import org.joda.time.Chronology;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationField;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.ISOChronology;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class DateTimeParserBucketTest {

    private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");
    private static final DateTimeZone LONDON = DateTimeZone.forID("Europe/London");
    private static final DateTimeZone TOKYO = DateTimeZone.forID("Asia/Tokyo");
    private static final DateTimeZone NEW_YORK = DateTimeZone.forID("America/New_York");

    private Chronology chrono;
    private Locale locale;

    @Before
    public void setUp() {
        chrono = ISOChronology.getInstance(DateTimeZone.UTC);
        locale = Locale.UK;
    }

    // Tests constructor with default parameters and getters
    @Test
    public void testConstructor_defaultParameters_initializedProperly() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, 2010, 2000);
        assertEquals(chrono, bucket.getChronology());
        assertEquals(locale, bucket.getLocale());
        assertEquals(Integer.valueOf(2010), bucket.getPivotYear());
        assertNull(bucket.getZone());
        assertEquals(0, bucket.getOffset());
    }

    // Tests deprecated 3-arg constructor
    @Test
    public void testConstructor_deprecatedThreeArgs_defaultsSet() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(1000L, chrono, null);
        assertNotNull(bucket.getLocale());
        assertNull(bucket.getPivotYear());
        assertEquals(chrono, bucket.getChronology());
    }

    // Tests deprecated 4-arg constructor with pivot year
    @Test
    public void testConstructor_deprecatedFourArgs_pivotYearSet() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(1000L, chrono, locale, Integer.valueOf(1950));
        assertEquals(Integer.valueOf(1950), bucket.getPivotYear());
    }

    // Tests setPivotYear and getPivotYear
    @Test
    public void testSetPivotYear_customValue_returnsUpdatedValue() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2000);
        assertNull(bucket.getPivotYear());
        bucket.setPivotYear(Integer.valueOf(2020));
        assertEquals(Integer.valueOf(2020), bucket.getPivotYear());
        bucket.setPivotYear(null);
        assertNull(bucket.getPivotYear());
    }

    // Tests setZone with UTC and non-UTC zones
    @Test
    public void testSetZone_zoneSettings_updatesZoneAndResetsOffset() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2000);
        bucket.setOffset(3600000);
        assertEquals(3600000, bucket.getOffset());

        bucket.setZone(PARIS);
        assertEquals(PARIS, bucket.getZone());
        assertEquals(0, bucket.getOffset());

        bucket.setZone(DateTimeZone.UTC);
        assertNull(bucket.getZone());
        assertEquals(0, bucket.getOffset());
    }

    // Tests setOffset updates offset and clears zone
    @Test
    public void testSetOffset_validOffset_clearsZone() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2000);
        bucket.setZone(PARIS);
        assertEquals(PARIS, bucket.getZone());

        bucket.setOffset(7200000);
        assertEquals(7200000, bucket.getOffset());
        assertNull(bucket.getZone());
    }

    // Tests saveField by DateTimeField and computeMillis
    @Test
    public void testSaveField_fieldAndValue_computesCorrectMillis() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2000);
        bucket.saveField(DateTimeFieldType.year().getField(chrono), 2005);
        bucket.saveField(DateTimeFieldType.monthOfYear().getField(chrono), 6);
        bucket.saveField(DateTimeFieldType.dayOfMonth().getField(chrono), 15);

        long millis = bucket.computeMillis(true);
        // 2005-06-15T00:00:00.000Z
        long expected = ISOChronology.getInstanceUTC().getDateTimeMillis(2005, 6, 15, 0);
        assertEquals(expected, millis);
    }

    // Tests saveField by DateTimeFieldType with text and locale
    @Test
    public void testSaveField_fieldTypeAndText_computesCorrectMillis() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, Locale.ENGLISH, null, 2000);
        bucket.saveField(DateTimeFieldType.year(), 2012);
        bucket.saveField(DateTimeFieldType.monthOfYear(), "March", Locale.ENGLISH);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 10);

        long millis = bucket.computeMillis(true);
        long expected = ISOChronology.getInstanceUTC().getDateTimeMillis(2012, 3, 10, 0);
        assertEquals(expected, millis);
    }

    // Tests computeMillis with default year injection when first field is month or day
    @Test
    public void testComputeMillis_monthAndDayOnly_usesDefaultYear() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2004);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 2);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 29); // Leap day in 2004

        long millis = bucket.computeMillis(true);
        long expected = ISOChronology.getInstanceUTC().getDateTimeMillis(2004, 2, 29, 0);
        assertEquals(expected, millis);
    }

    // Tests computeMillis with weekyear and week-of-weekyear ordering
    @Test
    public void testComputeMillis_weekyearAndWeekOfWeekyear_computesCorrectMillis() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2011);
        bucket.saveField(DateTimeFieldType.weekyear(), 2011);
        bucket.saveField(DateTimeFieldType.weekOfWeekyear(), 1);
        bucket.saveField(DateTimeFieldType.dayOfWeek(), 1);

        long millis = bucket.computeMillis(true, "2011-W01-1");
        long expected = ISOChronology.getInstanceUTC().getDateTimeMillis(2011, 1, 3, 0);
        assertEquals(expected, millis);
    }

    // Tests computeMillis with non-UTC zone applying offset transition
    @Test
    public void testComputeMillis_withTimeZone_computesLocalToZone() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2000);
        bucket.setZone(TOKYO); // +09:00
        bucket.saveField(DateTimeFieldType.year(), 2020);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 1);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 1);

        long millis = bucket.computeMillis(true);
        long expected = ISOChronology.getInstance(TOKYO).getDateTimeMillis(2020, 1, 1, 0);
        assertEquals(expected, millis);
    }

    // Tests computeMillis with explicit offset
    @Test
    public void testComputeMillis_withOffset_subtractsOffset() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2000);
        bucket.setOffset(3600000); // +1 hour
        bucket.saveField(DateTimeFieldType.year(), 2020);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 1);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 1);

        long millis = bucket.computeMillis(true);
        long localMillis = ISOChronology.getInstanceUTC().getDateTimeMillis(2020, 1, 1, 0);
        assertEquals(localMillis - 3600000, millis);
    }

    // Tests illegal field value throws IllegalFieldValueException with parsed text
    @Test
    public void testComputeMillis_invalidFieldValue_throwsExceptionWithText() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2000);
        bucket.saveField(DateTimeFieldType.year(), 2021);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 13); // Invalid month

        try {
            bucket.computeMillis(false, "2021-13");
            fail("Expected IllegalFieldValueException");
        } catch (IllegalFieldValueException e) {
            assertTrue(e.getMessage().contains("Cannot parse \"2021-13\""));
        }
    }

    // Tests saveState and restoreState restoring fields and zone
    @Test
    public void testSaveAndRestoreState_modifiedState_restoresOriginal() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2000);
        bucket.saveField(DateTimeFieldType.year(), 2000);
        bucket.setZone(LONDON);

        Object state = bucket.saveState();

        bucket.saveField(DateTimeFieldType.monthOfYear(), 5);
        bucket.setZone(PARIS);
        assertEquals(PARIS, bucket.getZone());

        boolean restored = bucket.restoreState(state);
        assertTrue(restored);
        assertEquals(LONDON, bucket.getZone());

        // Further saving and computing should reflect restored fields only
        bucket.saveField(DateTimeFieldType.monthOfYear(), 12);
        long millis = bucket.computeMillis(true);
        long expected = ISOChronology.getInstance(LONDON).getDateTimeMillis(2000, 12, 1, 0);
        assertEquals(expected, millis);
    }

    // Tests restoreState with invalid state object returns false
    @Test
    public void testRestoreState_invalidObject_returnsFalse() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2000);
        assertFalse(bucket.restoreState("InvalidStateObject"));
        assertFalse(bucket.restoreState(null));
    }

    // Tests array expansion when saving more than initial capacity (8 fields)
    @Test
    public void testSaveField_moreThanInitialCapacity_expandsArrayAndSorts() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2000);
        bucket.saveField(DateTimeFieldType.year(), 2020);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 10);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 25);
        bucket.saveField(DateTimeFieldType.hourOfDay(), 14);
        bucket.saveField(DateTimeFieldType.minuteOfHour(), 30);
        bucket.saveField(DateTimeFieldType.secondOfMinute(), 45);
        bucket.saveField(DateTimeFieldType.millisOfSecond(), 500);
        bucket.saveField(DateTimeFieldType.era(), 1);
        bucket.saveField(DateTimeFieldType.centuryOfEra(), 20);
        bucket.saveField(DateTimeFieldType.yearOfCentury(), 20);
        bucket.saveField(DateTimeFieldType.minuteOfDay(), 14 * 60 + 30);

        long millis = bucket.computeMillis(false);
        assertTrue(millis > 0);
    }

    // Tests restoreState from a foreign bucket instance returns false
    @Test
    public void testRestoreState_foreignBucketState_returnsFalse() {
        DateTimeParserBucket bucket1 = new DateTimeParserBucket(0L, chrono, locale, null, 2000);
        DateTimeParserBucket bucket2 = new DateTimeParserBucket(0L, chrono, locale, null, 2000);

        Object state1 = bucket1.saveState();
        assertFalse(bucket2.restoreState(state1));
    }

    // Tests compareReverse logic with null and unsupported duration fields
    @Test
    public void testCompareReverse_durationFieldComparisons() {
        Chronology c = ISOChronology.getInstanceUTC();
        assertEquals(0, DateTimeParserBucket.compareReverse(null, null));
        assertEquals(1, DateTimeParserBucket.compareReverse(c.years(), null));
        assertEquals(-1, DateTimeParserBucket.compareReverse(null, c.years()));
        assertTrue(DateTimeParserBucket.compareReverse(c.years(), c.months()) < 0);
        assertTrue(DateTimeParserBucket.compareReverse(c.days(), c.hours()) < 0);
    }

    // Tests getOffsetInteger and setOffset with Integer argument
    @Test
    public void testOffsetInteger_getAndSetIntegerOffset() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2000);
        assertNull(bucket.getOffsetInteger());

        bucket.setOffset(Integer.valueOf(3600000));
        assertEquals(Integer.valueOf(3600000), bucket.getOffsetInteger());
        assertEquals(3600000, bucket.getOffset());

        bucket.setOffset((Integer) null);
        assertNull(bucket.getOffsetInteger());
        assertEquals(0, bucket.getOffset());
    }

    // Tests saveState and restoreState restoring Integer offset
    @Test
    public void testSaveAndRestoreState_withOffset_restoresOriginalOffset() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2000);
        bucket.setOffset(Integer.valueOf(1800000));
        Object state = bucket.saveState();

        bucket.setOffset(Integer.valueOf(3600000));
        assertEquals(Integer.valueOf(3600000), bucket.getOffsetInteger());

        assertTrue(bucket.restoreState(state));
        assertEquals(Integer.valueOf(1800000), bucket.getOffsetInteger());
    }

    // Tests computeMillis no-arg method
    @Test
    public void testComputeMillis_noArg_computesAndPreservesFields() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2000);
        bucket.saveField(DateTimeFieldType.year(), 2022);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 8);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 15);

        long millis1 = bucket.computeMillis();
        long expected = ISOChronology.getInstanceUTC().getDateTimeMillis(2022, 8, 15, 0);
        assertEquals(expected, millis1);

        // Fields should not be reset after computeMillis()
        long millis2 = bucket.computeMillis();
        assertEquals(expected, millis2);
    }

    // Tests computeMillis with CharSequence (StringBuilder) and exception message
    @Test
    public void testComputeMillis_withCharSequence_invalidFieldIncludesCharSequenceInMessage() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2000);
        bucket.saveField(DateTimeFieldType.year(), 2020);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 32); // Invalid day

        StringBuilder text = new StringBuilder("2020-01-32");
        try {
            bucket.computeMillis(false, (CharSequence) text);
            fail("Expected IllegalFieldValueException");
        } catch (IllegalFieldValueException e) {
            assertTrue(e.getMessage().contains("Cannot parse \"2020-01-32\""));
        }
    }

    // Tests computeMillis with null text on invalid field
    @Test
    public void testComputeMillis_nullTextInvalidField_throwsExceptionWithoutTextPrefix() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2000);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 15); // Invalid month

        try {
            bucket.computeMillis(true, (CharSequence) null);
            fail("Expected IllegalFieldValueException");
        } catch (IllegalFieldValueException e) {
            assertFalse(e.getMessage().contains("Cannot parse"));
        }
    }

    // Tests parseMillis with DateTimeParser valid parsing
    @Test
    public void testParseMillis_validParser_computesExpectedMillis() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2000);
        DateTimeParser parser = DateTimeFormat.forPattern("yyyy-MM-dd").getParser();

        long result = bucket.parseMillis(parser, "2023-04-20");
        long expected = ISOChronology.getInstanceUTC().getDateTimeMillis(2023, 4, 20, 0);
        assertEquals(expected, result);
    }

    // Tests parseMillis with invalid input throws IllegalArgumentException
    @Test
    public void testParseMillis_invalidText_throwsIllegalArgumentException() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2000);
        DateTimeParser parser = DateTimeFormat.forPattern("yyyy-MM-dd").getParser();

        try {
            bucket.parseMillis(parser, "invalid-date-format");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("invalid-date-format"));
        }
    }

    // Tests parseMillis with incomplete input throws IllegalArgumentException
    @Test
    public void testParseMillis_incompleteText_throwsIllegalArgumentException() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2000);
        DateTimeParser parser = DateTimeFormat.forPattern("yyyy-MM-dd").getParser();

        try {
            bucket.parseMillis(parser, "2023-04-20extra");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("2023-04-20extra"));
        }
    }

    // Tests compareReverse with equal duration fields
    @Test
    public void testCompareReverse_sameOrEqualDurationFields_returnsZero() {
        DurationField days1 = ISOChronology.getInstanceUTC().days();
        DurationField days2 = ISOChronology.getInstance(TOKYO).days();
        assertEquals(0, DateTimeParserBucket.compareReverse(days1, days2));
        assertEquals(0, DateTimeParserBucket.compareReverse(days1, days1));
    }

    // Tests computeMillis with daylight saving time transition in New York
    @Test
    public void testComputeMillis_withDstTransition_computesCorrectZoneOffset() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2000);
        bucket.setZone(NEW_YORK);
        // Summer time in NY (EDT = UTC-4)
        bucket.saveField(DateTimeFieldType.year(), 2021);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 7);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 4);
        bucket.saveField(DateTimeFieldType.hourOfDay(), 12);

        long millis = bucket.computeMillis(true);
        long expected = ISOChronology.getInstance(NEW_YORK).getDateTimeMillis(2021, 7, 4, 12, 0, 0, 0);
        assertEquals(expected, millis);
    }

    // Tests saveField by DateTimeFieldType with text using bucket default locale
    @Test
    public void testSaveField_fieldTypeAndTextWithBucketLocale() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, Locale.FRENCH, null, 2000);
        bucket.saveField(DateTimeFieldType.year(), 2018);
        bucket.saveField(DateTimeFieldType.monthOfYear(), "août", null);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 1);

        long millis = bucket.computeMillis(true);
        long expected = ISOChronology.getInstanceUTC().getDateTimeMillis(2018, 8, 1, 0);
        assertEquals(expected, millis);
    }
}