package org.joda.time;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DateTimeZoneTest {

    private DateTimeZone originalDefault;

    @Before
    public void setUp() {
        originalDefault = DateTimeZone.getDefault();
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(originalDefault);
    }

    // Tests getting default zone and setting a new valid default zone
    @Test
    public void testGetAndSetDefault_validZone_changesDefault() {
        assertNotNull(DateTimeZone.getDefault());
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        DateTimeZone.setDefault(zone);
        assertEquals(zone, DateTimeZone.getDefault());
    }

    // Tests setting null default zone throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetDefault_nullZone_throwsException() {
        DateTimeZone.setDefault(null);
    }

    // Tests forID with null, UTC, and valid IDs
    @Test
    public void testForID_validIDs_returnsCorrespondingZone() {
        assertEquals(DateTimeZone.getDefault(), DateTimeZone.forID(null));
        assertEquals(DateTimeZone.UTC, DateTimeZone.forID("UTC"));
        assertEquals("America/Los_Angeles", DateTimeZone.forID("America/Los_Angeles").getID());
        assertEquals("Europe/London", DateTimeZone.forID("Europe/London").getID());
    }

    // Tests forID with valid offset string representations
    @Test
    public void testForID_offsetStrings_returnsFixedOffsetZone() {
        assertEquals(DateTimeZone.UTC, DateTimeZone.forID("+00:00"));
        assertEquals(DateTimeZone.UTC, DateTimeZone.forID("-00:00"));

        DateTimeZone zonePositive = DateTimeZone.forID("+02:00");
        assertEquals("+02:00", zonePositive.getID());
        assertEquals(2 * 3600 * 1000, zonePositive.getOffset(0L));

        DateTimeZone zoneNegative = DateTimeZone.forID("-05:30");
        assertEquals("-05:30", zoneNegative.getID());
        assertEquals(-(5 * 3600 + 30 * 60) * 1000, zoneNegative.getOffset(0L));
    }

    // Tests forID with unrecognized string throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testForID_invalidID_throwsException() {
        DateTimeZone.forID("Invalid/NonExistent_Zone");
    }

    // Tests forOffsetHours and forOffsetHoursMinutes standard behavior
    @Test
    public void testForOffsetHoursMinutes_validOffsets_returnsCorrectZone() {
        assertEquals(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));
        assertEquals(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));

        DateTimeZone zone1 = DateTimeZone.forOffsetHours(5);
        assertEquals("+05:00", zone1.getID());
        assertEquals(5 * 3600 * 1000, zone1.getOffset(0L));

        DateTimeZone zone2 = DateTimeZone.forOffsetHoursMinutes(-4, 30);
        assertEquals("-04:30", zone2.getID());
        assertEquals(-(4 * 3600 + 30 * 60) * 1000, zone2.getOffset(0L));

        DateTimeZone zoneMillis = DateTimeZone.forOffsetMillis(3600000);
        assertEquals("+01:00", zoneMillis.getID());
    }

    // Tests forOffsetHoursMinutes with invalid minute range throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_minutesOutOfRange_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(2, 60);
    }

    // Tests forOffsetHoursMinutes with negative minutes throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_negativeMinutes_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(2, -1);
    }

    // Tests forTimeZone with null, UTC, short alias conversions, and raw GMT offsets
    @Test
    public void testForTimeZone_variousInputs_returnsExpectedZone() {
        assertEquals(DateTimeZone.getDefault(), DateTimeZone.forTimeZone(null));
        assertEquals(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));
        assertEquals(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT")));

        // Tests timezone alias mappings
        assertEquals("America/Los_Angeles", DateTimeZone.forTimeZone(TimeZone.getTimeZone("PST")).getID());
        assertEquals("America/New_York", DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST")).getID());
        assertEquals("Europe/London", DateTimeZone.forTimeZone(TimeZone.getTimeZone("WET")).getID());

        // Tests custom GMT offset timezone
        TimeZone gmtZone = TimeZone.getTimeZone("GMT+03:00");
        DateTimeZone zone = DateTimeZone.forTimeZone(gmtZone);
        assertEquals("+03:00", zone.getID());
    }

    // Tests getAvailableIDs and provider getters
    @Test
    public void testGetAvailableIDsAndProviders_returnsNonNull() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertNotNull(ids);
        assertTrue(ids.contains("UTC"));
        assertTrue(ids.contains("America/New_York"));
        assertNotNull(DateTimeZone.getProvider());
        assertNotNull(DateTimeZone.getNameProvider());
    }

    // Tests getName and getShortName with locales
    @Test
    public void testGetNameAndShortName_returnsFormattedOrLocalizedName() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertNotNull(zone.getName(0L));
        assertNotNull(zone.getName(0L, Locale.UK));
        assertNotNull(zone.getShortName(0L));
        assertNotNull(zone.getShortName(0L, Locale.UK));

        DateTimeZone fixedZone = DateTimeZone.forOffsetHours(3);
        assertEquals("+03:00", fixedZone.getName(0L));
        assertEquals("+03:00", fixedZone.getShortName(0L));
    }

    // Tests getOffset, getStandardOffset, and isStandardOffset
    @Test
    public void testGetOffsetAndStandardOffset_returnsExpectedValues() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // Winter instant (Standard time: EST = -5 hours)
        long winterInstant = 0L; // 1970-01-01
        assertEquals(-5 * 3600 * 1000, zone.getOffset(winterInstant));
        assertEquals(-5 * 3600 * 1000, zone.getStandardOffset(winterInstant));
        assertTrue(zone.isStandardOffset(winterInstant));

        // Using ReadableInstant
        assertEquals(-5 * 3600 * 1000, zone.getOffset(new Instant(winterInstant)));
        assertEquals(zone.getOffset(DateTimeUtils.currentTimeMillis()), zone.getOffset((ReadableInstant) null));
    }

    // Tests conversion between UTC and local instants
    @Test
    public void testConvertUTCToLocal_andLocalToUTC_roundTrip() {
        DateTimeZone zone = DateTimeZone.forID("America/Chicago");
        long instantUTC = 10000000000L;
        long instantLocal = zone.convertUTCToLocal(instantUTC);
        long convertedBackUTC = zone.convertLocalToUTC(instantLocal, false);
        assertEquals(instantUTC, convertedBackUTC);

        long withOriginal = zone.convertLocalToUTC(instantLocal, false, instantUTC);
        assertEquals(instantUTC, withOriginal);
    }

    // Tests convertLocalToUTC in DST gap with strict=true throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConvertLocalToUTC_strictDuringGap_throwsException() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // Spring forward transition: 2007-03-11 02:30:00 does not exist
        // 2007-03-11 02:00:00 local is gap start
        long gapLocalMillis = new DateTime(2007, 3, 11, 2, 30, 0, 0, DateTimeZone.UTC).getMillis();
        zone.convertLocalToUTC(gapLocalMillis, true);
    }

    // Tests getMillisKeepLocal across different zones
    @Test
    public void testGetMillisKeepLocal_differentZones_preservesLocalTime() {
        DateTimeZone zoneNY = DateTimeZone.forID("America/New_York");
        DateTimeZone zoneLA = DateTimeZone.forID("America/Los_Angeles");
        long instant = 10000000000L;
        long converted = zoneNY.getMillisKeepLocal(zoneLA, instant);
        long expectedLocal = zoneNY.convertUTCToLocal(instant);
        assertEquals(expectedLocal, zoneLA.convertUTCToLocal(converted));
        assertEquals(instant, zoneNY.getMillisKeepLocal(zoneNY, instant));
    }

    // Tests isLocalDateTimeGap check
    @Test
    public void testIsLocalDateTimeGap_gapTime_returnsTrue() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        LocalDateTime gapTime = new LocalDateTime(2007, 3, 11, 2, 30, 0, 0);
        assertTrue(zone.isLocalDateTimeGap(gapTime));

        LocalDateTime nonGapTime = new LocalDateTime(2007, 1, 1, 12, 0, 0, 0);
        assertFalse(zone.isLocalDateTimeGap(nonGapTime));
        assertFalse(DateTimeZone.UTC.isLocalDateTimeGap(gapTime));
    }

    // Tests adjustOffset during overlap
    @Test
    public void testAdjustOffset_overlap_adjustsCorrectly() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // Fall back overlap: 2007-11-04 01:30:00 occurs twice
        long overlapLocal = new DateTime(2007, 11, 4, 1, 30, 0, 0, DateTimeZone.UTC).getMillis();
        long earlier = zone.convertLocalToUTC(overlapLocal, false, overlapLocal - 3600000);
        long later = zone.convertLocalToUTC(overlapLocal, false, overlapLocal + 3600000);

        assertEquals(earlier, zone.adjustOffset(later, false));
        assertEquals(later, zone.adjustOffset(earlier, true));
        assertEquals(0L, DateTimeZone.UTC.adjustOffset(0L, false));
    }

    // Tests transitions on dynamic vs fixed zones
    @Test
    public void testTransitions_fixedAndDynamicZones() {
        assertTrue(DateTimeZone.UTC.isFixed());
        assertEquals(0L, DateTimeZone.UTC.nextTransition(0L));
        assertEquals(0L, DateTimeZone.UTC.previousTransition(0L));

        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertFalse(zone.isFixed());
        assertTrue(zone.nextTransition(0L) > 0L);
        assertTrue(zone.previousTransition(0L) < 0L);
    }

    // Tests equals, hashCode, toString, and toTimeZone
    @Test
    public void testEqualsHashCodeToStringAndToTimeZone() {
        DateTimeZone zone1 = DateTimeZone.forID("Europe/Paris");
        DateTimeZone zone2 = DateTimeZone.forID("Europe/Paris");
        DateTimeZone zone3 = DateTimeZone.forID("Europe/London");

        assertEquals(zone1, zone2);
        assertNotEquals(zone1, zone3);
        assertEquals(zone1.hashCode(), zone2.hashCode());
        assertEquals("Europe/Paris", zone1.toString());
        assertEquals("Europe/Paris", zone1.toTimeZone().getID());
    }

    // Tests serialization and deserialization via Stub
    @Test
    public void testSerialization_roundTrip_preservesZone() throws Exception {
        DateTimeZone zone = DateTimeZone.forID("America/Chicago");
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
}