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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

    // Tests getting default time zone and setting default time zone
    @Test
    public void testGetAndSetDefault_validZone_changesDefault() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        DateTimeZone.setDefault(zone);
        assertEquals(zone, DateTimeZone.getDefault());
    }

    // Tests setting default time zone to null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetDefault_nullZone_throwsException() {
        DateTimeZone.setDefault(null);
    }

    // Tests forID with null returning default zone
    @Test
    public void testForID_nullId_returnsDefault() {
        assertEquals(DateTimeZone.getDefault(), DateTimeZone.forID(null));
    }

    // Tests forID with UTC returning singleton UTC instance
    @Test
    public void testForID_utcId_returnsUTC() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("UTC"));
    }

    // Tests forID with fixed offset strings
    @Test
    public void testForID_offsetString_returnsFixedOffsetZone() {
        DateTimeZone zonePlus = DateTimeZone.forID("+02:00");
        assertEquals("+02:00", zonePlus.getID());
        assertEquals(2 * 3600000, zonePlus.getOffset(0L));

        DateTimeZone zoneMinus = DateTimeZone.forID("-05:00");
        assertEquals("-05:00", zoneMinus.getID());
        assertEquals(-5 * 3600000, zoneMinus.getOffset(0L));

        DateTimeZone zoneZero = DateTimeZone.forID("+00:00");
        assertSame(DateTimeZone.UTC, zoneZero);
    }

    // Tests forID with invalid ID throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testForID_invalidId_throwsException() {
        DateTimeZone.forID("Invalid/Unknown_Zone_ID");
    }

    // Tests forOffsetHours with valid positive, negative and zero hours
    @Test
    public void testForOffsetHours_validHours_returnsCorrectZone() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));

        DateTimeZone zone3 = DateTimeZone.forOffsetHours(3);
        assertEquals("+03:00", zone3.getID());
        assertEquals(3 * 3600000, zone3.getOffset(0L));

        DateTimeZone zoneMinus8 = DateTimeZone.forOffsetHours(-8);
        assertEquals("-08:00", zoneMinus8.getID());
        assertEquals(-8 * 3600000, zoneMinus8.getOffset(0L));
    }

    // Tests forOffsetHours with out of range values
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHours_outOfRangePositive_throwsException() {
        DateTimeZone.forOffsetHours(24);
    }

    // Tests forOffsetHours with out of range negative values
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHours_outOfRangeNegative_throwsException() {
        DateTimeZone.forOffsetHours(-24);
    }

    // Tests forOffsetHoursMinutes with standard inputs
    @Test
    public void testForOffsetHoursMinutes_validInputs_returnsCorrectZone() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));

        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(5, 30);
        assertEquals("+05:30", zone.getID());
        assertEquals(5 * 3600000 + 30 * 60000, zone.getOffset(0L));

        DateTimeZone negativeZone = DateTimeZone.forOffsetHoursMinutes(-2, 30);
        assertEquals("-02:30", negativeZone.getID());
        assertEquals(-(2 * 3600000 + 30 * 60000), negativeZone.getOffset(0L));

        DateTimeZone zeroHourZone = DateTimeZone.forOffsetHoursMinutes(0, 45);
        assertEquals("+00:45", zeroHourZone.getID());
        assertEquals(45 * 60000, zeroHourZone.getOffset(0L));
    }

    // Tests forOffsetHoursMinutes with invalid minute range
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_minutesTooHigh_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(1, 60);
    }

    // Tests forOffsetHoursMinutes with negative minutes
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_negativeMinutes_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(1, -1);
    }

    // Tests forOffsetHoursMinutes with out of range hours
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_hoursOutOfRange_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(24, 0);
    }

    // Tests forOffsetMillis
    @Test
    public void testForOffsetMillis_validMillis_returnsZone() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000);
        assertEquals("+01:00", zone.getID());
        assertEquals(3600000, zone.getOffset(0L));
        assertTrue(zone.isFixed());

        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));
    }

    // Tests forTimeZone with various JDK TimeZone mappings
    @Test
    public void testForTimeZone_variousInputs_returnsMappedDateTimeZone() {
        assertEquals(DateTimeZone.getDefault(), DateTimeZone.forTimeZone(null));
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT")));

        DateTimeZone pstZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("PST"));
        assertEquals("America/Los_Angeles", pstZone.getID());

        DateTimeZone gmtOffset = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+02:00"));
        assertEquals("+02:00", gmtOffset.getID());

        DateTimeZone gmtZero = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT-00:00"));
        assertSame(DateTimeZone.UTC, gmtZero);
    }

    // Tests getAvailableIDs
    @Test
    public void testGetAvailableIDs_returnsNonEmptySet() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertNotNull(ids);
        assertTrue(ids.contains("UTC"));
        assertTrue(ids.contains("America/New_York"));
    }

    // Tests names and short names formatting
    @Test
    public void testGetNameAndShortName_utcAndFixedZone_returnsCorrectNames() {
        assertEquals("UTC", DateTimeZone.UTC.getName(0L, Locale.ENGLISH));
        assertEquals("UTC", DateTimeZone.UTC.getShortName(0L, Locale.ENGLISH));

        DateTimeZone fixed = DateTimeZone.forOffsetHours(2);
        assertEquals("+02:00", fixed.getName(0L, Locale.ENGLISH));
        assertEquals("+02:00", fixed.getShortName(0L, Locale.ENGLISH));
    }

    // Tests convertUTCToLocal and convertLocalToUTC
    @Test
    public void testConvertUTCToLocalAndBack_fixedZone_correctConversion() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(3);
        long utcMillis = 10000000L;
        long localMillis = zone.convertUTCToLocal(utcMillis);
        assertEquals(utcMillis + 3 * 3600000, localMillis);

        long convertedBack = zone.convertLocalToUTC(localMillis, false);
        assertEquals(utcMillis, convertedBack);

        long convertedBackWithOriginal = zone.convertLocalToUTC(localMillis, false, utcMillis);
        assertEquals(utcMillis, convertedBackWithOriginal);
    }

    // Tests getMillisKeepLocal across different zones
    @Test
    public void testGetMillisKeepLocal_betweenZones_maintainsLocalClock() {
        DateTimeZone zone1 = DateTimeZone.forOffsetHours(1);
        DateTimeZone zone2 = DateTimeZone.forOffsetHours(3);
        long instant = 10000000L;
        long result = zone1.getMillisKeepLocal(zone2, instant);
        assertEquals(instant - 2 * 3600000, result);
        assertEquals(instant, zone1.getMillisKeepLocal(zone1, instant));
    }

    // Tests isStandardOffset
    @Test
    public void testIsStandardOffset_fixedAndDSTZone_returnsExpected() {
        assertTrue(DateTimeZone.UTC.isStandardOffset(0L));
        DateTimeZone fixedZone = DateTimeZone.forOffsetHours(5);
        assertTrue(fixedZone.isStandardOffset(0L));
    }

    // Tests adjustOffset during overlap
    @Test
    public void testAdjustOffset_fixedZone_returnsSameInstant() {
        DateTimeZone fixedZone = DateTimeZone.forOffsetHours(2);
        long instant = 10000000L;
        assertEquals(instant, fixedZone.adjustOffset(instant, true));
        assertEquals(instant, fixedZone.adjustOffset(instant, false));
    }

    // Tests equals, hashCode, toString, and toTimeZone
    @Test
    public void testBasicMethods_equalityAndToString_consistentBehavior() {
        DateTimeZone zone1 = DateTimeZone.forID("+02:00");
        DateTimeZone zone2 = DateTimeZone.forOffsetHours(2);
        assertEquals(zone1, zone2);
        assertEquals(zone1.hashCode(), zone2.hashCode());
        assertEquals("+02:00", zone1.toString());
        assertEquals("+02:00", zone1.getID());

        assertFalse(zone1.equals("+02:00"));
        assertFalse(zone1.equals(null));

        TimeZone tz = zone1.toTimeZone();
        assertNotNull(tz);
    }

    // Tests serialization and deserialization via Stub
    @Test
    public void testSerialization_roundTrip_preservesZone() throws Exception {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(zone);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTimeZone deserialized = (DateTimeZone) ois.readObject();
        ois.close();

        assertSame(zone, deserialized);
    }

    // Additional tests for uncovered methods and branches

    @Test
    public void testForID_variousPrefixesAndAliases() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("UT"));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("GMT"));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("Etc/UTC"));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("Etc/GMT"));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("-00:00"));

        DateTimeZone zoneSec = DateTimeZone.forID("+01:23:45");
        assertEquals("+01:23:45", zoneSec.getID());
        assertEquals((1 * 3600 + 23 * 60 + 45) * 1000, zoneSec.getOffset(0L));

        DateTimeZone zoneMinusSec = DateTimeZone.forID("-01:23:45.678");
        assertEquals("-01:23:45.678", zoneMinusSec.getID());
        assertEquals(-(1 * 3600000 + 23 * 60000 + 45000 + 678), zoneMinusSec.getOffset(0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_hoursTooNegative() {
        DateTimeZone.forOffsetHoursMinutes(-24, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_negativeHoursMinutesTooHigh() {
        DateTimeZone.forOffsetHoursMinutes(-1, 60);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_negativeHoursNegativeMinutes() {
        DateTimeZone.forOffsetHoursMinutes(-1, -1);
    }

    @Test
    public void testForOffsetMillis_negativeAndBoundaryValues() {
        DateTimeZone zoneNegative = DateTimeZone.forOffsetMillis(-3600000);
        assertEquals("-01:00", zoneNegative.getID());
        assertEquals(-3600000, zoneNegative.getOffset(0L));

        DateTimeZone zoneMax = DateTimeZone.forOffsetMillis(86399999);
        assertEquals("+23:59:59.999", zoneMax.getID());

        DateTimeZone zoneMin = DateTimeZone.forOffsetMillis(-86399999);
        assertEquals("-23:59:59.999", zoneMin.getID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetMillis_tooLargePositive() {
        DateTimeZone.forOffsetMillis(86400000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetMillis_tooLargeNegative() {
        DateTimeZone.forOffsetMillis(-86400000);
    }

    @Test
    public void testForTimeZone_additionalMappings() {
        DateTimeZone zoneMinus = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT-08:00"));
        assertEquals("-08:00", zoneMinus.getID());

        DateTimeZone zoneCustom = DateTimeZone.forTimeZone(TimeZone.getTimeZone("CustomZoneThatDoesNotExist"));
        assertNotNull(zoneCustom);
    }

    @Test
    public void testGetAndSetNameProvider() {
        NameProvider original = DateTimeZone.getNameProvider();
        assertNotNull(original);

        NameProvider custom = new DefaultNameProvider();
        DateTimeZone.setNameProvider(custom);
        assertSame(custom, DateTimeZone.getNameProvider());

        try {
            DateTimeZone.setNameProvider(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testGetAndSetProvider() {
        Provider original = DateTimeZone.getProvider();
        assertNotNull(original);

        Provider custom = new UTCProvider();
        DateTimeZone.setProvider(custom);
        assertSame(custom, DateTimeZone.getProvider());

        try {
            DateTimeZone.setProvider(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testGetNameAndShortName_defaultLocale() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        assertEquals("+02:00", zone.getName(0L));
        assertEquals("+02:00", zone.getShortName(0L));
    }

    @Test
    public void testGetOffset_ReadableInstant() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(4);
        Instant instant = new Instant(1000L);
        assertEquals(4 * 3600000, zone.getOffset(instant));
        assertEquals(zone.getOffset(0L), zone.getOffset(null));
    }

    @Test
    public void testGetOffsetFromLocal_andTransitions() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // 2007-11-04T01:30:00 (during fall-back overlap)
        long localOverlap = new DateTime(2007, 11, 4, 1, 30, 0, 0, DateTimeZone.UTC).getMillis();
        int offset = zone.getOffsetFromLocal(localOverlap);
        assertTrue(offset == -4 * 3600000 || offset == -5 * 3600000);

        long instant = 0L;
        long next = zone.nextTransition(instant);
        assertTrue(next > instant);
        long prev = zone.previousTransition(next);
        assertEquals(instant <= prev, true);
    }

    @Test
    public void testTransitions_fixedZone() {
        DateTimeZone fixed = DateTimeZone.forOffsetHours(2);
        long instant = 1000L;
        assertEquals(instant, fixed.nextTransition(instant));
        assertEquals(instant, fixed.previousTransition(instant));
    }

    @Test
    public void testIsStandardOffset_variableZone() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // Summer instant (EDT, standard is EST)
        long summerInstant = new DateTime(2007, 7, 1, 12, 0, 0, 0, zone).getMillis();
        assertFalse(zone.isStandardOffset(summerInstant));

        // Winter instant (EST, standard is EST)
        long winterInstant = new DateTime(2007, 1, 1, 12, 0, 0, 0, zone).getMillis();
        assertTrue(zone.isStandardOffset(winterInstant));
    }

    @Test
    public void testIsLocalDateTimeGap() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // 2007-03-11 02:30:00 is in the spring forward gap
        LocalDateTime gapLocal = new LocalDateTime(2007, 3, 11, 2, 30, 0, 0);
        assertTrue(zone.isLocalDateTimeGap(gapLocal));

        // 2007-03-11 01:30:00 is not in gap
        LocalDateTime nonGapLocal = new LocalDateTime(2007, 3, 11, 1, 30, 0, 0);
        assertFalse(zone.isLocalDateTimeGap(nonGapLocal));

        DateTimeZone fixed = DateTimeZone.forOffsetHours(2);
        assertFalse(fixed.isLocalDateTimeGap(gapLocal));
    }

    @Test
    public void testAdjustOffset_dstTransitions() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // 2007-11-04 01:30:00 EDT / EST overlap
        DateTime dtEarlier = new DateTime(2007, 11, 4, 1, 30, 0, 0, zone).minusHours(1); // earlier offset EDT
        long instantEarlier = dtEarlier.getMillis() + 3600000L;
        long adjustedLater = zone.adjustOffset(instantEarlier, false);
        long adjustedEarlier = zone.adjustOffset(instantEarlier, true);
        assertTrue(adjustedEarlier <= adjustedLater);
    }

    @Test
    public void testConvertLocalToUTC_strictGapHandling() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // 2007-03-11 02:30:00 is in the DST spring-forward gap
        long localGap = new DateTime(2007, 3, 11, 2, 30, 0, 0, DateTimeZone.UTC).getMillis();
        try {
            zone.convertLocalToUTC(localGap, true);
            fail("Expected IllegalInstantException");
        } catch (IllegalInstantException e) {
            // expected
        }

        // Non-strict should resolve without throwing
        long nonStrictUtc = zone.convertLocalToUTC(localGap, false);
        assertTrue(nonStrictUtc > 0);
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

    @Test
    public void testGetMillisKeepLocal_nullZoneUsesDefault() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        long instant = 5000L;
        long expected = zone.getMillisKeepLocal(DateTimeZone.getDefault(), instant);
        assertEquals(expected, zone.getMillisKeepLocal(null, instant));
    }
}