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

/**
 * JUnit 4 Unit Tests for {@link DateTimeZone}.
 */
public class DateTimeZoneTest {

    private DateTimeZone defaultZone;

    @Before
    public void setUp() throws Exception {
        defaultZone = DateTimeZone.getDefault();
    }

    @After
    public void tearDown() throws Exception {
        DateTimeZone.setDefault(defaultZone);
    }

    // Tests getOffsetFromLocal during autumn DST overlap for zero-offset standard zone (Defects4J Time-19)
    @Test
    public void testGetOffsetFromLocal_duringDSTOverlapLondon_returnsSummerOffset() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        // 2011-10-30T01:15:00.000 London is ambiguous during the 1-hour overlap (BST +01:00 -> GMT +00:00)
        // 2011-10-30T01:15:00.000 local millis relative to 1970
        DateTime dt = new DateTime(2011, 10, 30, 1, 15, 0, 0, DateTimeZone.UTC);
        int offset = zone.getOffsetFromLocal(dt.getMillis());
        assertEquals(3600000L, offset);
    }

    // Tests getOffsetFromLocal during autumn DST overlap for positive offset zone
    @Test
    public void testGetOffsetFromLocal_duringDSTOverlapParis_returnsSummerOffset() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        // 2011-10-30T02:15:00.000 Paris is ambiguous (CEST +02:00 -> CET +01:00)
        DateTime dt = new DateTime(2011, 10, 30, 2, 15, 0, 0, DateTimeZone.UTC);
        int offset = zone.getOffsetFromLocal(dt.getMillis());
        assertEquals(7200000L, offset);
    }

    // Tests forID with null returns default zone
    @Test
    public void testForID_null_returnsDefaultZone() {
        assertEquals(DateTimeZone.getDefault(), DateTimeZone.forID(null));
    }

    // Tests forID with "UTC"
    @Test
    public void testForID_UTC_returnsUTCInstance() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        assertSame(DateTimeZone.UTC, zone);
        assertEquals("UTC", zone.getID());
    }

    // Tests forID with valid offset strings
    @Test
    public void testForID_offsetStrings_returnsFixedOffsetZone() {
        DateTimeZone zonePlus = DateTimeZone.forID("+02:00");
        assertEquals("+02:00", zonePlus.getID());
        assertEquals(7200000, zonePlus.getOffset(0L));
        assertTrue(zonePlus.isFixed());

        DateTimeZone zoneMinus = DateTimeZone.forID("-05:30");
        assertEquals("-05:30", zoneMinus.getID());
        assertEquals(-19800000, zoneMinus.getOffset(0L));

        DateTimeZone zoneZero = DateTimeZone.forID("+00:00");
        assertSame(DateTimeZone.UTC, zoneZero);
    }

    // Tests forID with invalid format throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testForID_invalidID_throwsException() {
        DateTimeZone.forID("Invalid/Zone_Name_123");
    }

    // Tests forOffsetHours and forOffsetHoursMinutes
    @Test
    public void testForOffsetHoursMinutes_validValues_returnsCorrectZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(5, 30);
        assertEquals("+05:30", zone.getID());
        assertEquals(19800000, zone.getOffset(0L));

        DateTimeZone zoneNeg = DateTimeZone.forOffsetHoursMinutes(-4, 15);
        assertEquals("-04:15", zoneNeg.getID());
        assertEquals(-15300000, zoneNeg.getOffset(0L));

        DateTimeZone zoneZero = DateTimeZone.forOffsetHoursMinutes(0, 0);
        assertSame(DateTimeZone.UTC, zoneZero);

        DateTimeZone zoneHours = DateTimeZone.forOffsetHours(-8);
        assertEquals("-08:00", zoneHours.getID());
    }

    // Tests forOffsetHoursMinutes with invalid minute out of range throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_invalidMinute_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(2, 60);
    }

    // Tests forOffsetHoursMinutes with negative minute throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_negativeMinute_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(2, -1);
    }

    // Tests forOffsetMillis
    @Test
    public void testForOffsetMillis_validMillis_returnsZone() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000);
        assertEquals("+01:00", zone.getID());
        assertEquals(3600000, zone.getOffset(0L));

        DateTimeZone zoneWithMillis = DateTimeZone.forOffsetMillis(3661001);
        assertEquals("+01:01:01.001", zoneWithMillis.getID());
    }

    // Tests forTimeZone with java.util.TimeZone and aliases
    @Test
    public void testForTimeZone_variousTimeZones_returnsExpectedZone() {
        assertEquals(DateTimeZone.getDefault(), DateTimeZone.forTimeZone(null));
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));
        assertEquals("America/New_York", DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST")).getID());
        assertEquals("+03:00", DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+03:00")).getID());
    }

    // Tests setDefault with null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetDefault_null_throwsException() {
        DateTimeZone.setDefault(null);
    }

    // Tests setDefault and getDefault
    @Test
    public void testSetDefault_validZone_changesDefault() {
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        DateTimeZone.setDefault(paris);
        assertEquals(paris, DateTimeZone.getDefault());
    }

    // Tests convertUTCToLocal and convertLocalToUTC
    @Test
    public void testConvertUTCToLocal_and_convertLocalToUTC_roundTrip() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        long instantUTC = 1000000000000L;
        long instantLocal = zone.convertUTCToLocal(instantUTC);
        assertEquals(instantUTC + zone.getOffset(instantUTC), instantLocal);
        long backToUTC = zone.convertLocalToUTC(instantLocal, false);
        assertEquals(instantUTC, backToUTC);
    }

    // Tests convertLocalToUTC strict mode inside DST gap throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConvertLocalToUTC_strictInsideGap_throwsException() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // In 2011, US DST spring forward was 2011-03-13 02:00 -> 03:00
        DateTime localInGap = new DateTime(2011, 3, 13, 2, 30, 0, 0, DateTimeZone.UTC);
        zone.convertLocalToUTC(localInGap.getMillis(), true);
    }

    // Tests isLocalDateTimeGap
    @Test
    public void testIsLocalDateTimeGap_gapAndNonGap() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        LocalDateTime gapTime = new LocalDateTime(2011, 3, 13, 2, 30);
        assertTrue(zone.isLocalDateTimeGap(gapTime));

        LocalDateTime validTime = new LocalDateTime(2011, 3, 13, 1, 30);
        assertFalse(zone.isLocalDateTimeGap(validTime));

        assertFalse(DateTimeZone.UTC.isLocalDateTimeGap(gapTime));
    }

    // Tests adjustOffset during overlap
    @Test
    public void testAdjustOffset_duringOverlap() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        // 2011-10-30T02:30:00 overlap between +02:00 (earlier) and +01:00 (later)
        long instantEarlier = new DateTime(2011, 10, 30, 2, 30, 0, 0, zone).getMillis();
        long adjustedEarlier = zone.adjustOffset(instantEarlier, false);
        long adjustedLater = zone.adjustOffset(instantEarlier, true);

        assertEquals(instantEarlier, adjustedEarlier);
        assertEquals(instantEarlier + 3600000L, adjustedLater);
    }

    // Tests getMillisKeepLocal
    @Test
    public void testGetMillisKeepLocal_betweenDifferentZones() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        DateTimeZone tokyo = DateTimeZone.forID("Asia/Tokyo");
        long instant = 1000000000000L;
        long result = london.getMillisKeepLocal(tokyo, instant);
        long expectedLocal = london.convertUTCToLocal(instant);
        assertEquals(expectedLocal, tokyo.convertUTCToLocal(result));

        assertEquals(instant, london.getMillisKeepLocal(london, instant));
    }

    // Tests getName, getShortName, and isStandardOffset
    @Test
    public void testGetNameAndShortName() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long winterInstant = new DateTime(2011, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long summerInstant = new DateTime(2011, 7, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();

        assertTrue(zone.isStandardOffset(winterInstant));
        assertFalse(zone.isStandardOffset(summerInstant));

        assertNotNull(zone.getName(winterInstant, Locale.UK));
        assertNotNull(zone.getShortName(winterInstant, Locale.UK));
        assertEquals(zone.getName(winterInstant, Locale.getDefault()), zone.getName(winterInstant));
        assertEquals(zone.getShortName(winterInstant, Locale.getDefault()), zone.getShortName(winterInstant));
    }

    // Tests getAvailableIDs, toTimeZone, hashCode, toString, and Serialization
    @Test
    public void testBasicMethodsAndSerialization() throws Exception {
        DateTimeZone zone = DateTimeZone.forID("America/Chicago");

        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertTrue(ids.contains("America/Chicago"));
        assertTrue(ids.contains("UTC"));

        assertEquals("America/Chicago", zone.toString());
        assertEquals("America/Chicago", zone.toTimeZone().getID());
        assertEquals(57 + "America/Chicago".hashCode(), zone.hashCode());
        assertFalse(zone.equals(DateTimeZone.UTC));
        assertTrue(zone.equals(DateTimeZone.forID("America/Chicago")));

        // Serialization round trip
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