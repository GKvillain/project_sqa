package org.joda.time.format;

import java.util.Locale;

import org.joda.time.Chronology;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.ISOChronology;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DateTimeParserBucketTest {

    private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");
    private static final DateTimeZone LONDON = DateTimeZone.forID("Europe/London");
    private static final DateTimeZone TOKYO = DateTimeZone.forID("Asia/Tokyo");

    private Chronology chrono;
    private Locale locale;

    @Before
    public void setUp() {
        chrono = ISOChronology.getInstance(DateTimeZone.UTC);
        locale = Locale.ENGLISH;
    }

    // Tests constructors and getters
    @Test
    public void testConstructor_withDefaults_initializesCorrectly() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale);
        assertEquals(chrono.withUTC(), bucket.getChronology());
        assertEquals(locale, bucket.getLocale());
        assertNull(bucket.getZone());
        assertEquals(0, bucket.getOffset());
        assertNull(bucket.getPivotYear());
    }

    // Tests constructor with pivot year and default year
    @Test
    public void testConstructor_withPivotAndDefaultYear_initializesCorrectly() {
        Integer pivot = Integer.valueOf(2050);
        DateTimeParserBucket bucket = new DateTimeParserBucket(1000L, GJChronology.getInstance(TOKYO), Locale.FRANCE, pivot, 1999);
        assertEquals(GJChronology.getInstanceUTC(), bucket.getChronology());
        assertEquals(Locale.FRANCE, bucket.getLocale());
        assertEquals(TOKYO, bucket.getZone());
        assertEquals(pivot, bucket.getPivotYear());
    }

    // Tests setZone and setOffset overriding
    @Test
    public void testSetZoneAndSetOffset_overridesEachOther() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale);
        
        bucket.setZone(PARIS);
        assertEquals(PARIS, bucket.getZone());
        assertEquals(0, bucket.getOffset());

        bucket.setZone(DateTimeZone.UTC);
        assertNull(bucket.getZone());

        bucket.setOffset(3600000);
        assertEquals(3600000, bucket.getOffset());
        assertEquals(Integer.valueOf(3600000), bucket.getOffsetInteger());
        assertNull(bucket.getZone());

        bucket.setOffset((Integer) null);
        assertNull(bucket.getOffsetInteger());
        assertEquals(0, bucket.getOffset());

        bucket.setPivotYear(Integer.valueOf(2020));
        assertEquals(Integer.valueOf(2020), bucket.getPivotYear());
        bucket.setPivotYear(null);
        assertNull(bucket.getPivotYear());
    }

    // Tests normal computeMillis with year, month, day
    @Test
    public void testComputeMillis_yearMonthDay_returnsCorrectMillis() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2000);
        bucket.saveField(DateTimeFieldType.year(), 2004);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 6);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 9);

        // 2004-06-09T00:00:00.000Z
        long expected = ISOChronology.getInstanceUTC().getDateTimeMillis(2004, 6, 9, 0);
        assertEquals(expected, bucket.computeMillis());
    }

    // Tests computeMillis defaulting year when only month and day are provided
    @Test
    public void testComputeMillis_monthAndDayWithoutYear_usesDefaultYear() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2005);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 10);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 25);

        // 2005-10-25T00:00:00.000Z
        long expected = ISOChronology.getInstanceUTC().getDateTimeMillis(2005, 10, 25, 0);
        assertEquals(expected, bucket.computeMillis());
    }

    // Tests computeMillis with weekyear and weekOfWeekyear (regression check for week-based parsing)
    @Test
    public void testComputeMillis_weekyearAndWeekOfWeek_returnsCorrectMillis() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2010);
        bucket.saveField(DateTimeFieldType.weekyear(), 2011);
        bucket.saveField(DateTimeFieldType.weekOfWeekyear(), 1);
        bucket.saveField(DateTimeFieldType.dayOfWeek(), 1);

        long expected = ISOChronology.getInstanceUTC().getDateTimeMillis(2011, 1, 3, 0);
        assertEquals(expected, bucket.computeMillis(true));
    }

    // Tests computeMillis with text field and locale
    @Test
    public void testComputeMillis_textFieldWithLocale_parsesCorrectly() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, Locale.ENGLISH, null, 2000);
        bucket.saveField(DateTimeFieldType.year(), 2008);
        bucket.saveField(DateTimeFieldType.monthOfYear(), "December", Locale.ENGLISH);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 15);

        long expected = ISOChronology.getInstanceUTC().getDateTimeMillis(2008, 12, 15, 0);
        assertEquals(expected, bucket.computeMillis(false));
    }

    // Tests computeMillis with zone and offset calculations
    @Test
    public void testComputeMillis_withZone_appliesZoneOffset() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale);
        bucket.setZone(LONDON);
        bucket.saveField(DateTimeFieldType.year(), 2007);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 7);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 1);

        // 2007-07-01 in London is BST (UTC+1)
        long localMillis = ISOChronology.getInstanceUTC().getDateTimeMillis(2007, 7, 1, 0);
        long expected = localMillis - 3600000L;
        assertEquals(expected, bucket.computeMillis());
    }

    // Tests computeMillis with explicit offset
    @Test
    public void testComputeMillis_withOffset_subtractsOffset() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale);
        bucket.setOffset(7200000); // UTC+2
        bucket.saveField(DateTimeFieldType.year(), 2000);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 1);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 1);

        long localMillis = ISOChronology.getInstanceUTC().getDateTimeMillis(2000, 1, 1, 0);
        assertEquals(localMillis - 7200000L, bucket.computeMillis());
    }

    // Tests computeMillis when more than 10 fields are saved to trigger Arrays.sort branch
    @Test
    public void testComputeMillis_moreThanTenFields_sortsCorrectly() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale);
        bucket.saveField(DateTimeFieldType.year(), 2012);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 5);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 10);
        bucket.saveField(DateTimeFieldType.hourOfDay(), 14);
        bucket.saveField(DateTimeFieldType.minuteOfHour(), 30);
        bucket.saveField(DateTimeFieldType.secondOfMinute(), 45);
        bucket.saveField(DateTimeFieldType.millisOfSecond(), 500);
        bucket.saveField(DateTimeFieldType.centuryOfEra(), 20);
        bucket.saveField(DateTimeFieldType.era(), 1);
        bucket.saveField(DateTimeFieldType.dayOfWeek(), 4);
        bucket.saveField(DateTimeFieldType.minuteOfDay(), 14 * 60 + 30);

        long expected = ISOChronology.getInstanceUTC().getDateTimeMillis(2012, 5, 10, 14, 30, 45, 500);
        assertEquals(expected, bucket.computeMillis());
    }

    // Tests saveState and restoreState with rollback of saved fields
    @Test
    public void testSaveAndRestoreState_revertsSavedFieldsAndProperties() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale);
        bucket.saveField(DateTimeFieldType.year(), 1995);
        bucket.setOffset(1000);

        Object state = bucket.saveState();

        bucket.saveField(DateTimeFieldType.monthOfYear(), 12);
        bucket.setOffset(5000);
        bucket.setZone(PARIS);

        assertTrue(bucket.restoreState(state));
        assertEquals(1000, bucket.getOffset());
        assertNull(bucket.getZone());

        // Should compute without monthOfYear set
        long expected = ISOChronology.getInstanceUTC().getDateTimeMillis(1995, 1, 1, 0) - 1000L;
        assertEquals(expected, bucket.computeMillis());
    }

    // Tests restoreState with foreign/invalid state object
    @Test
    public void testRestoreState_invalidObject_returnsFalse() {
        DateTimeParserBucket bucket1 = new DateTimeParserBucket(0L, chrono, locale);
        DateTimeParserBucket bucket2 = new DateTimeParserBucket(0L, chrono, locale);

        assertFalse(bucket1.restoreState("invalid_state"));
        assertFalse(bucket1.restoreState(null));

        Object state2 = bucket2.saveState();
        assertFalse(bucket1.restoreState(state2));
    }

    // Tests IllegalFieldValueException handling with text prepending
    @Test
    public void testComputeMillis_invalidFieldValueWithText_prependsParseTextToException() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 13);
        try {
            bucket.computeMillis(false, "2020-13-01");
            fail("Expected IllegalFieldValueException");
        } catch (IllegalFieldValueException e) {
            assertTrue(e.getMessage().contains("Cannot parse \"2020-13-01\""));
        }
    }

    // Tests IllegalFieldValueException without text parameter
    @Test(expected = IllegalFieldValueException.class)
    public void testComputeMillis_invalidFieldValueWithoutText_throwsException() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 32);
        bucket.computeMillis();
    }

    // Tests DST gap / offset transition exception
    @Test(expected = IllegalArgumentException.class)
    public void testComputeMillis_gapInZoneTransition_throwsIllegalArgumentException() {
        DateTimeZone zoneWithGap = DateTimeZone.forID("America/New_York");
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, ISOChronology.getInstance(zoneWithGap), locale);
        bucket.setZone(zoneWithGap);
        // 2007-03-11 02:30:00 did not exist in EST/EDT due to spring forward
        bucket.saveField(DateTimeFieldType.year(), 2007);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 3);
        bucket.saveField(DateTimeFieldType.dayOfMonth(), 11);
        bucket.saveField(DateTimeFieldType.hourOfDay(), 2);
        bucket.saveField(DateTimeFieldType.minuteOfHour(), 30);

        bucket.computeMillis(false, "2007-03-11 02:30");
    }

    // Tests saveField with raw DateTimeField instance
    @Test
    public void testSaveField_withDateTimeFieldInstance_setsCorrectValue() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, BuddhistChronology.getInstanceUTC(), locale);
        bucket.saveField(BuddhistChronology.getInstanceUTC().year(), 2543);

        // Buddhist year 2543 = Gregorian year 2000
        long expected = ISOChronology.getInstanceUTC().getDateTimeMillis(2000, 1, 1, 0);
        assertEquals(expected, bucket.computeMillis());
    }

    // Tests saveField with raw DateTimeField instance and text value
    @Test
    public void testSaveField_withDateTimeFieldInstanceAndText_setsCorrectValue() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2000);
        bucket.saveField(ISOChronology.getInstanceUTC().monthOfYear(), "March", Locale.ENGLISH);

        long expected = ISOChronology.getInstanceUTC().getDateTimeMillis(2000, 3, 1, 0);
        assertEquals(expected, bucket.computeMillis());
    }

    // Tests computeMillis idempotency
    @Test
    public void testComputeMillis_calledMultipleTimes_isIdempotent() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0L, chrono, locale, null, 2000);
        bucket.saveField(DateTimeFieldType.year(), 2002);
        bucket.saveField(DateTimeFieldType.monthOfYear(), 4);

        long firstResult = bucket.computeMillis();
        long secondResult = bucket.computeMillis();
        assertEquals(firstResult, secondResult);
    }
}