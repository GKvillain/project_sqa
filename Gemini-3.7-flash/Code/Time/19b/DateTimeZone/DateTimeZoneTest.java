package org.joda.time;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import static org.junit.Assert.*;

/**
 * Unit tests for org.joda.time.DateTimeZone.
 */
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

    // Tests forID with null returning default zone
    @Test
    public void testForID_null_returnsDefault() {
        assertEquals(DateTimeZone.getDefault(), DateTimeZone.forID(null));
    }

    // Tests forID with standard UTC string
    @Test
    public void testForID_utc_returnsUTC() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("UTC"));
    }

    // Tests forID with positive and negative fixed offset strings
    @Test
    public void testForID_offsetStrings_returnsFixedZones() {
        DateTimeZone zonePlus = DateTimeZone.forID("+02:00");
        assertEquals("+02:00", zonePlus.getID());
        assertEquals(2 * 3600 * 1000, zonePlus.getOffset(0L));

        DateTimeZone zoneMinus = DateTimeZone.forID("-05:30");
        assertEquals("-05:30", zoneMinus.getID());
        assertEquals(-(5 * 3600 + 30 * 60) * 1000, zoneMinus.getOffset(0L));

        DateTimeZone zoneZero = DateTimeZone.forID("+00:00");
        assertSame(DateTimeZone.UTC, zoneZero);
    }

    // Tests forID throwing exception for invalid zone id
    @Test(expected = IllegalArgumentException.class)
    public void testForID_invalidID_throwsException() {
        DateTimeZone.forID("Invalid/Unknown_Zone_ID");
    }

    // Tests forOffsetHours and forOffsetHoursMinutes valid conversions
    @Test
    public void testForOffsetHoursMinutes_validOffsets_returnsCorrectZone() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));

        DateTimeZone zone1 = DateTimeZone.forOffsetHours(5);
        assertEquals("+05:00", zone1.getID());
        assertEquals(5 * 3600 * 1000, zone1.getOffset(0L));

        DateTimeZone zone2 = DateTimeZone.forOffsetHoursMinutes(-4, 30);
        assertEquals("-04:30", zone2.getID());
        assertEquals(-(4 * 3600 + 30 * 60) * 1000, zone2.getOffset(0L));
    }

    // Tests forOffsetHoursMinutes when minutes are out of range
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_minutesOutOfRange_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(2, 60);
    }

    // Tests forOffsetMillis creating zone with fixed offset
    @Test
    public void testForOffsetMillis_validMillis_returnsZone() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000);
        assertEquals("+01:00", zone.getID());
        assertEquals(3600000, zone.getOffset(0L));
        assertTrue(zone.isFixed());
    }

    // Tests forTimeZone with null, UTC, and JDK legacy ID conversions
    @Test
    public void testForTimeZone_variousInputs_returnsMatchingDateTimeZone() {
        assertEquals(DateTimeZone.getDefault(), DateTimeZone.forTimeZone(null));
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));

        DateTimeZone zoneEst = DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST"));
        assertEquals("America/New_York", zoneEst.getID());

        DateTimeZone gmtOffset = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+03:00"));
        assertEquals("+03:00", gmtOffset.getID());
    }

    // Tests setDefault and getDefault behavior
    @Test
    public void testSetDefault_validZone_updatesDefault() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        DateTimeZone.setDefault(zone);
        assertEquals(zone, DateTimeZone.getDefault());
    }

    // Tests setDefault throwing exception on null argument
    @Test(expected = IllegalArgumentException.class)
    public void testSetDefault_null_throwsException() {
        DateTimeZone.setDefault(null);
    }

    // Tests getOffset with null ReadableInstant fallback to current time
    @Test
    public void testGetOffset_readableInstant_returnsCorrectOffset() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        assertEquals(2 * 3600 * 1000, zone.getOffset((ReadableInstant) null));

        Instant instant = new Instant(0L);
        assertEquals(2 * 3600 * 1000, zone.getOffset(instant));
    }

    // Tests getOffsetFromLocal during autumn DST overlap for zones with standard offset 0 (e.g. Europe/London)
    @Test
    public void testGetOffsetFromLocal_dstOverlapLondon_favoursDaylight() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        // 2011-10-30 01:30:00 BST/GMT overlap transition (01:00 to 02:00 repeats)
        // Local 01:30 occurs first at UTC 00:30 (BST, +01:00), then at UTC 01:30 (GMT, +00:00)
        long localInstant = new DateTime(2011, 10, 30, 1, 30, 0, 0, DateTimeZone.UTC).getMillis();
        int offset = zone.getOffsetFromLocal(localInstant);
        // During overlap, earlier instant (daylight savings: +1 hour = 3600000 ms) is expected
        assertEquals(3600000, offset);
    }

    // Tests convertUTCToLocal and convertLocalToUTC basic conversions
    @Test
    public void testConvertUTCToLocal_and_convertLocalToUTC_roundTrip() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        long instantUTC = 1000000000000L;
        long instantLocal = zone.convertUTCToLocal(instantUTC);
        assertEquals(instantUTC + zone.getOffset(instantUTC), instantLocal);

        long backToUTC = zone.convertLocalToUTC(instantLocal, false);
        assertEquals(instantUTC, backToUTC);
    }

    // Tests convertLocalToUTC strict mode throwing exception on DST gap
    @Test(expected = IllegalArgumentException.class)
    public void testConvertLocalToUTC_strictGap_throwsException() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // 2011-03-13 02:30:00 does not exist due to spring DST cutover (+1 hour gap)
        long gapLocalInstant = new DateTime(2011, 3, 13, 2, 30, 0, 0, DateTimeZone.UTC).getMillis();
        zone.convertLocalToUTC(gapLocalInstant, true);
    }

    // Tests isLocalDateTimeGap for gap and non-gap periods
    @Test
    public void testIsLocalDateTimeGap_detectsGapCorrectly() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        LocalDateTime gapTime = new LocalDateTime(2011, 3, 13, 2, 30, 0, 0);
        LocalDateTime normalTime = new LocalDateTime(2011, 3, 13, 1, 30, 0, 0);

        assertTrue(zone.isLocalDateTimeGap(gapTime));
        assertFalse(zone.isLocalDateTimeGap(normalTime));
    }

    // Tests adjustOffset during DST overlap
    @Test
    public void testAdjustOffset_dstOverlap_adjustsBetweenEarlierAndLater() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // 2011-11-06 01:30:00 EDT/EST overlap
        long instantEDT = new DateTime(2011, 11, 6, 1, 30, 0, 0, zone).getMillis();

        long adjustedLater = zone.adjustOffset(instantEDT, true);
        long adjustedEarlier = zone.adjustOffset(instantEDT, false);

        assertTrue(adjustedLater > adjustedEarlier);
        assertEquals(3600000L, adjustedLater - adjustedEarlier);
    }

    // Tests getMillisKeepLocal converting instant preserving wall clock time
    @Test
    public void testGetMillisKeepLocal_transfersLocalTimeToNewZone() {
        DateTimeZone zoneLondon = DateTimeZone.forID("Europe/London");
        DateTimeZone zoneTokyo = DateTimeZone.forID("Asia/Tokyo");

        long instant = 0L; // 1970-01-01T00:00:00Z
        long result = zoneLondon.getMillisKeepLocal(zoneTokyo, instant);

        assertEquals(zoneTokyo.convertUTCToLocal(result), zoneLondon.convertUTCToLocal(instant));
    }

    // Tests getName and getShortName localized methods
    @Test
    public void testGetName_and_getShortName_returnsDisplayStrings() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long winterInstant = new DateTime(2011, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();

        assertNotNull(zone.getName(winterInstant, Locale.ENGLISH));
        assertNotNull(zone.getShortName(winterInstant, Locale.ENGLISH));
        assertTrue(zone.isStandardOffset(winterInstant));
    }

    // Tests getAvailableIDs, toTimeZone, hashCode, and toString
    @Test
    public void testGeneralZoneProperties() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertNotNull(ids);
        assertTrue(ids.contains("UTC"));

        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        TimeZone tz = zone.toTimeZone();
        assertEquals("Europe/Paris", tz.getID());

        assertEquals("Europe/Paris", zone.toString());
        assertEquals(zone.hashCode(), DateTimeZone.forID("Europe/Paris").hashCode());
        assertEquals(zone, DateTimeZone.forID("Europe/Paris"));
        assertNotEquals(zone, DateTimeZone.UTC);
    }
}