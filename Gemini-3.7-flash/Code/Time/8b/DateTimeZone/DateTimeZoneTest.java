package org.joda.time;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.joda.time.tz.DefaultNameProvider;
import org.joda.time.tz.NameProvider;
import org.joda.time.tz.Provider;
import org.joda.time.tz.UTCProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DateTimeZoneTest {

    private DateTimeZone originalDefault;
    private Provider originalProvider;
    private NameProvider originalNameProvider;

    @Before
    public void setUp() {
        originalDefault = DateTimeZone.getDefault();
        originalProvider = DateTimeZone.getProvider();
        originalNameProvider = DateTimeZone.getNameProvider();
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(originalDefault);
        DateTimeZone.setProvider(originalProvider);
        DateTimeZone.setNameProvider(originalNameProvider);
    }

    // Tests getting time zone by null ID returns default zone
    @Test
    public void testForID_nullId_returnsDefault() {
        DateTimeZone zone = DateTimeZone.forID(null);
        assertEquals(DateTimeZone.getDefault(), zone);
    }

    // Tests getting UTC zone by ID
    @Test
    public void testForID_utcId_returnsUTC() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        assertSame(DateTimeZone.UTC, zone);
        assertEquals("UTC", zone.getID());
    }

    // Tests getting zone with positive offset ID string
    @Test
    public void testForID_validOffsetString_returnsFixedOffsetZone() {
        DateTimeZone zone = DateTimeZone.forID("+02:00");
        assertEquals("+02:00", zone.getID());
        assertEquals(2 * 3600 * 1000, zone.getOffset(0L));
    }

    // Tests getting zone with negative offset ID string
    @Test
    public void testForID_negativeOffsetString_returnsFixedOffsetZone() {
        DateTimeZone zone = DateTimeZone.forID("-05:00");
        assertEquals("-05:00", zone.getID());
        assertEquals(-5 * 3600 * 1000, zone.getOffset(0L));
    }

    // Tests invalid ID string throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testForID_invalidId_throwsException() {
        DateTimeZone.forID("Invalid/Unknown_Zone_ID");
    }

    // Tests forOffsetHours with valid positive and negative hours
    @Test
    public void testForOffsetHours_validHours_returnsCorrectZone() {
        DateTimeZone zonePlus5 = DateTimeZone.forOffsetHours(5);
        assertEquals("+05:00", zonePlus5.getID());
        assertEquals(5 * 3600 * 1000, zonePlus5.getOffset(0L));

        DateTimeZone zoneMinus8 = DateTimeZone.forOffsetHours(-8);
        assertEquals("-08:00", zoneMinus8.getID());
        assertEquals(-8 * 3600 * 1000, zoneMinus8.getOffset(0L));
    }

    // Tests forOffsetHoursMinutes with negative minute offset when hour is zero (Defects4J Time-8)
    @Test
    public void testForOffsetHoursMinutes_zeroHoursNegativeMinutes_returnsCorrectZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(0, -15);
        assertEquals("-00:15", zone.getID());
        assertEquals(-15 * 60 * 1000, zone.getOffset(0L));
    }

    // Tests forOffsetHoursMinutes with negative hour and negative minute offset (Defects4J Time-8)
    @Test
    public void testForOffsetHoursMinutes_negativeHoursNegativeMinutes_returnsCorrectZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-2, -15);
        assertEquals("-02:15", zone.getID());
        assertEquals(-(2 * 60 + 15) * 60 * 1000, zone.getOffset(0L));
    }

    // Tests forOffsetHoursMinutes with positive hour and positive minute offset
    @Test
    public void testForOffsetHoursMinutes_positiveHoursMinutes_returnsCorrectZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(2, 30);
        assertEquals("+02:30", zone.getID());
        assertEquals((2 * 60 + 30) * 60 * 1000, zone.getOffset(0L));
    }

    // Tests forOffsetHoursMinutes with 0, 0 returns UTC
    @Test
    public void testForOffsetHoursMinutes_zeroHoursZeroMinutes_returnsUTC() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(0, 0);
        assertSame(DateTimeZone.UTC, zone);
    }

    // Tests forOffsetHoursMinutes with out of range hours throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_hoursOutOfRange_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(24, 0);
    }

    // Tests forOffsetHoursMinutes with out of range minutes throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_minutesOutOfRange_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(0, 60);
    }

    // Tests forOffsetMillis with valid values and limits
    @Test
    public void testForOffsetMillis_validMillis_returnsZone() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000);
        assertEquals("+01:00", zone.getID());
        assertEquals(3600000, zone.getOffset(0L));

        DateTimeZone utc = DateTimeZone.forOffsetMillis(0);
        assertSame(DateTimeZone.UTC, utc);
    }

    // Tests forOffsetMillis exceeding maximum allowed offset throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetMillis_outOfRangeMillis_throwsException() {
        DateTimeZone.forOffsetMillis(86400 * 1000);
    }

    // Tests forTimeZone with null, UTC, and custom IDs
    @Test
    public void testForTimeZone_variousInputs_returnsExpectedZone() {
        assertEquals(DateTimeZone.getDefault(), DateTimeZone.forTimeZone(null));
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));

        DateTimeZone pstZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("PST"));
        assertEquals("America/Los_Angeles", pstZone.getID());

        DateTimeZone gmtOffsetZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+03:00"));
        assertEquals("+03:00", gmtOffsetZone.getID());
    }

    // Tests setDefault with valid zone and null
    @Test
    public void testSetDefault_validAndNull() {
        DateTimeZone original = DateTimeZone.getDefault();
        DateTimeZone utc = DateTimeZone.UTC;
        DateTimeZone.setDefault(utc);
        assertEquals(utc, DateTimeZone.getDefault());
        DateTimeZone.setDefault(original);

        try {
            DateTimeZone.setDefault(null);
            fail("Expected IllegalArgumentException on null default zone");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    // Tests convertUTCToLocal and convertLocalToUTC conversions
    @Test
    public void testConvertUTCToLocal_and_convertLocalToUTC() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(3);
        long utcMillis = 10000000L;
        long localMillis = zone.convertUTCToLocal(utcMillis);
        assertEquals(utcMillis + 3 * 3600 * 1000L, localMillis);

        long backToUtc = zone.convertLocalToUTC(localMillis, false);
        assertEquals(utcMillis, backToUtc);
    }

    // Tests getMillisKeepLocal across different zones
    @Test
    public void testGetMillisKeepLocal_differentZones_preservesLocalWallTime() {
        DateTimeZone zonePlus2 = DateTimeZone.forOffsetHours(2);
        DateTimeZone zonePlus5 = DateTimeZone.forOffsetHours(5);

        long instantInPlus2 = 10000000L;
        long instantInPlus5 = zonePlus2.getMillisKeepLocal(zonePlus5, instantInPlus2);
        assertEquals(instantInPlus2 - 3 * 3600 * 1000L, instantInPlus5);
        assertEquals(instantInPlus2, zonePlus2.getMillisKeepLocal(zonePlus2, instantInPlus2));
    }

    // Tests basic properties: name, short name, standard offset, fixed, toString, equals, hashCode
    @Test
    public void testBasicProperties_fixedZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(1, 30);
        assertTrue(zone.isFixed());
        assertEquals("+01:30", zone.toString());
        assertEquals("+01:30", zone.getName(0L, Locale.ENGLISH));
        assertEquals("+01:30", zone.getShortName(0L, Locale.ENGLISH));
        assertTrue(zone.isStandardOffset(0L));
        assertEquals(zone.getOffset(0L), zone.getStandardOffset(0L));

        DateTimeZone sameZone = DateTimeZone.forOffsetHoursMinutes(1, 30);
        assertEquals(zone, sameZone);
        assertEquals(zone.hashCode(), sameZone.hashCode());
        assertFalse(zone.equals("+01:30"));
    }

    // Tests available IDs, Provider, and NameProvider accessors
    @Test
    public void testGetAvailableIDs_and_Providers() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertNotNull(ids);
        assertTrue(ids.contains("UTC"));
        assertNotNull(DateTimeZone.getProvider());
        assertNotNull(DateTimeZone.getNameProvider());
    }

    // Tests serialization and deserialization of DateTimeZone
    @Test
    public void testSerialization() throws Exception {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(zone);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTimeZone result = (DateTimeZone) ois.readObject();
        ois.close();

        assertEquals(zone, result);
        assertSame(zone, result);
    }

    // Additional tests to complete code coverage

    @Test
    public void testForID_zAndGmtEquivalents() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("UT"));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("Z"));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("+00:00"));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("-00:00"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_positiveHourNegativeMinute_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(2, -15);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_negativeHourPositiveMinute_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(-2, 15);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_minuteLessThanNegative59_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(0, -60);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHours_outOfRangeNegative_throwsException() {
        DateTimeZone.forOffsetHours(-24);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetMillis_minOutOfRange_throwsException() {
        DateTimeZone.forOffsetMillis(-86400 * 1000);
    }

    @Test
    public void testToTimeZone() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        TimeZone tz = zone.toTimeZone();
        assertEquals("America/New_York", tz.getID());

        DateTimeZone fixedZone = DateTimeZone.forOffsetHours(3);
        TimeZone fixedTz = fixedZone.toTimeZone();
        assertEquals("GMT+03:00", fixedTz.getID());
    }

    @Test
    public void testGetOffset_ReadableInstant() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        Instant instant = new Instant(0L);
        assertEquals(zone.getOffset(0L), zone.getOffset(instant));
        assertEquals(DateTimeZone.getDefault().getOffset(0L), DateTimeZone.getDefault().getOffset((ReadableInstant) null));
    }

    @Test
    public void testGetNameAndShortName_noLocale() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        assertNotNull(zone.getName(0L));
        assertNotNull(zone.getShortName(0L));
        assertNotNull(zone.getNameKey(0L));
    }

    @Test
    public void testTransitions_and_isStandardOffset() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        assertFalse(zone.isFixed());

        long instant = 0L;
        long next = zone.nextTransition(instant);
        assertTrue(next > instant);
        long prev = zone.previousTransition(next);
        assertTrue(prev <= next);

        DateTimeZone fixedZone = DateTimeZone.forOffsetHours(2);
        assertEquals(instant, fixedZone.nextTransition(instant));
        assertEquals(instant, fixedZone.previousTransition(instant));

        // Summer vs Winter offset check for standard offset
        // 2007-06-01 UTC = 1180656000000L (EDT, non-standard offset for America/New_York)
        // 2007-01-01 UTC = 1167609600000L (EST, standard offset for America/New_York)
        assertFalse(zone.isStandardOffset(1180656000000L));
        assertTrue(zone.isStandardOffset(1167609600000L));
    }

    @Test
    public void testConvertLocalToUTC_strictAndGaps() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // Spring forward transition: 2007-03-11 02:00 -> 03:00 (gap from 2:00 to 2:59:59)
        // 2007-03-11 02:30 local time in America/New_York is 1173598200000L (gap)
        long localGapInstant = new DateTime(2007, 3, 11, 2, 30, 0, 0, DateTimeZone.UTC).getMillis();

        try {
            zone.convertLocalToUTC(localGapInstant, true);
            fail("Expected IllegalInstantException or IllegalArgumentException for gap when strict");
        } catch (IllegalArgumentException expected) {
            // expected
        }

        long utcLenient = zone.convertLocalToUTC(localGapInstant, false);
        assertTrue(utcLenient > 0);

        // Overlap: Fall back transition: 2007-11-04 01:30 (occurs twice)
        long localOverlapInstant = new DateTime(2007, 11, 4, 1, 30, 0, 0, DateTimeZone.UTC).getMillis();
        long utcStrict = zone.convertLocalToUTC(localOverlapInstant, true);
        assertTrue(utcStrict > 0);

        long utcWithOriginal = zone.convertLocalToUTC(localOverlapInstant, false, localOverlapInstant - 4 * 3600 * 1000L);
        assertTrue(utcWithOriginal > 0);
    }

    @Test
    public void testIsLocalDateTimeGap() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        LocalDateTime gapDateTime = new LocalDateTime(2007, 3, 11, 2, 30, 0, 0);
        assertTrue(zone.isLocalDateTimeGap(gapDateTime));

        LocalDateTime nonGapDateTime = new LocalDateTime(2007, 3, 11, 4, 30, 0, 0);
        assertFalse(zone.isLocalDateTimeGap(nonGapDateTime));
    }

    @Test
    public void testAdjustOffset() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // Overlap period: 2007-11-04 01:30 EDT / EST
        long localOverlapInstant = new DateTime(2007, 11, 4, 1, 30, 0, 0, zone).getMillis();

        long adjustedEarlier = zone.adjustOffset(localOverlapInstant, false);
        long adjustedLater = zone.adjustOffset(localOverlapInstant, true);
        assertTrue(adjustedEarlier <= adjustedLater);

        DateTimeZone fixed = DateTimeZone.forOffsetHours(1);
        assertEquals(1000L, fixed.adjustOffset(1000L, false));
    }

    @Test
    public void testGetOffsetFromLocal() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        long winterLocal = new DateTime(2007, 1, 1, 12, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertEquals(-5 * 3600 * 1000, zone.getOffsetFromLocal(winterLocal));

        long summerLocal = new DateTime(2007, 7, 1, 12, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertEquals(-4 * 3600 * 1000, zone.getOffsetFromLocal(summerLocal));
    }

    @Test
    public void testSetProviderAndNameProvider() {
        Provider customProvider = new UTCProvider();
        DateTimeZone.setProvider(customProvider);
        assertSame(customProvider, DateTimeZone.getProvider());

        try {
            DateTimeZone.setProvider(null);
            fail("Expected IllegalArgumentException when provider is null");
        } catch (IllegalArgumentException expected) {
            // expected
        }

        NameProvider customNameProvider = new DefaultNameProvider();
        DateTimeZone.setNameProvider(customNameProvider);
        assertSame(customNameProvider, DateTimeZone.getNameProvider());

        try {
            DateTimeZone.setNameProvider(null);
            fail("Expected IllegalArgumentException when name provider is null");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    @Test(expected = ArithmeticException.class)
    public void testConvertUTCToLocal_overflow() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(5);
        zone.convertUTCToLocal(Long.MAX_VALUE);
    }

    @Test(expected = ArithmeticException.class)
    public void testConvertLocalToUTC_underflow() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(5);
        zone.convertLocalToUTC(Long.MIN_VALUE, false);
    }
}