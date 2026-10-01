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

    private DateTimeZone defaultZone;

    @Before
    public void setUp() {
        defaultZone = DateTimeZone.getDefault();
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(defaultZone);
    }

    // Tests adjustOffset during DST overlap with earlier and later flags (Defects4J Time-17 target)
    @Test
    public void testAdjustOffset_dstOverlap_adjustsCorrectly() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        // Europe/Paris DST end transition in 2008 was at 2008-10-26T03:00:00+02:00 -> 02:00:00+01:00
        // 2008-10-26T00:00:00Z is 02:00:00+02:00 (overlap hour 1) = 1224979200000L
        // 2008-10-26T01:00:00Z is 02:00:00+01:00 (overlap hour 2) = 1224982800000L
        long overlapEarly = 1224979200000L;
        long overlapLate = 1224982800000L;

        assertEquals(overlapEarly, zone.adjustOffset(overlapEarly, false));
        assertEquals(overlapLate, zone.adjustOffset(overlapEarly, true));
        assertEquals(overlapEarly, zone.adjustOffset(overlapLate, false));
        assertEquals(overlapLate, zone.adjustOffset(overlapLate, true));
    }

    // Tests adjustOffset when there is no transition (normal non-overlap time)
    @Test
    public void testAdjustOffset_nonOverlap_returnsSameInstant() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        long normalInstant = 1200000000000L; // Non-transition time
        assertEquals(normalInstant, zone.adjustOffset(normalInstant, false));
        assertEquals(normalInstant, zone.adjustOffset(normalInstant, true));
    }

    // Tests getDefault and setDefault with valid and invalid inputs
    @Test
    public void testGetAndSetDefault_validAndNull_behavesCorrectly() {
        assertNotNull(DateTimeZone.getDefault());

        DateTimeZone zoneUTC = DateTimeZone.UTC;
        DateTimeZone.setDefault(zoneUTC);
        assertEquals(zoneUTC, DateTimeZone.getDefault());

        try {
            DateTimeZone.setDefault(null);
            fail("Expected IllegalArgumentException for null default zone");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    // Tests forID with null, UTC, fixed offset strings, and valid zone IDs
    @Test
    public void testForID_validAndOffsetFormats_returnsExpectedZone() {
        assertEquals(DateTimeZone.getDefault(), DateTimeZone.forID(null));
        assertEquals(DateTimeZone.UTC, DateTimeZone.forID("UTC"));
        assertEquals(DateTimeZone.UTC, DateTimeZone.forID("+00:00"));

        DateTimeZone plusOne = DateTimeZone.forID("+01:00");
        assertEquals("+01:00", plusOne.getID());
        assertEquals(3600000, plusOne.getOffset(0L));

        DateTimeZone minusFive = DateTimeZone.forID("-05:00");
        assertEquals("-05:00", minusFive.getID());
        assertEquals(-18000000, minusFive.getOffset(0L));

        DateTimeZone london = DateTimeZone.forID("Europe/London");
        assertEquals("Europe/London", london.getID());
    }

    // Tests forID with unrecognized ID format throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testForID_unrecognisedId_throwsException() {
        DateTimeZone.forID("Invalid/Zone_Name");
    }

    // Tests forOffsetHours and forOffsetHoursMinutes factory methods
    @Test
    public void testForOffsetHoursMinutes_variousInputs_returnsMatchingZone() {
        assertEquals(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));
        assertEquals(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));

        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(5, 30);
        assertEquals("+05:30", zone.getID());
        assertEquals(19800000, zone.getOffset(0L));

        DateTimeZone negZone = DateTimeZone.forOffsetHoursMinutes(-8, 30);
        assertEquals("-08:30", negZone.getID());
        assertEquals(-30600000, negZone.getOffset(0L));
    }

    // Tests forOffsetHoursMinutes with invalid minute range
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_invalidMinutes_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(2, 60);
    }

    // Tests forOffsetHoursMinutes with negative minute value
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_negativeMinutes_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(2, -1);
    }

    // Tests forOffsetMillis factory method
    @Test
    public void testForOffsetMillis_validMillis_returnsMatchingZone() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(7200000);
        assertEquals("+02:00", zone.getID());
        assertEquals(7200000, zone.getOffset(0L));

        DateTimeZone zoneWithSecs = DateTimeZone.forOffsetMillis(3661000);
        assertEquals("+01:01:01", zoneWithSecs.getID());

        DateTimeZone zoneWithMillis = DateTimeZone.forOffsetMillis(3661001);
        assertEquals("+01:01:01.001", zoneWithMillis.getID());
    }

    // Tests forTimeZone with JDK TimeZone instances
    @Test
    public void testForTimeZone_jdkTimeZone_returnsMatchingDateTimeZone() {
        assertEquals(DateTimeZone.getDefault(), DateTimeZone.forTimeZone(null));
        assertEquals(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));
        assertEquals(DateTimeZone.forID("America/New_York"), DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST")));
        assertEquals(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT")));
    }

    // Tests getAvailableIDs returns non-empty set containing UTC
    @Test
    public void testGetAvailableIDs_containsUTC() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertNotNull(ids);
        assertTrue(ids.size() > 0);
        assertTrue(ids.contains("UTC"));
        assertTrue(ids.contains("Europe/London"));
    }

    // Tests getName, getShortName, getOffset, isStandardOffset methods
    @Test
    public void testNamesAndOffsets_londonZone_returnsExpectedValues() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long winterInstant = 0L; // 1970-01-01 (GMT, +00:00)
        long summerInstant = 10000000000L; // In summer (+01:00)

        assertEquals(0, zone.getOffset(winterInstant));
        assertEquals(0, zone.getStandardOffset(winterInstant));
        assertTrue(zone.isStandardOffset(winterInstant));

        assertEquals(3600000, zone.getOffset(summerInstant));
        assertEquals(0, zone.getStandardOffset(summerInstant));
        assertFalse(zone.isStandardOffset(summerInstant));

        assertNotNull(zone.getName(winterInstant, Locale.ENGLISH));
        assertNotNull(zone.getShortName(winterInstant, Locale.ENGLISH));
        assertNotNull(zone.getName(winterInstant));
        assertNotNull(zone.getShortName(winterInstant));
    }

    // Tests getOffset with null and non-null ReadableInstant
    @Test
    public void testGetOffset_readableInstant_returnsCorrectOffset() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(3);
        assertEquals(10800000, zone.getOffset((ReadableInstant) null));
        assertEquals(10800000, zone.getOffset(new Instant(0L)));
    }

    // Tests getOffsetFromLocal calculation for normal and DST boundary cases
    @Test
    public void testGetOffsetFromLocal_normalAndBoundary_returnsCorrectOffset() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertEquals(0, zone.getOffsetFromLocal(0L));

        DateTimeZone fixed = DateTimeZone.forOffsetHours(2);
        assertEquals(7200000, fixed.getOffsetFromLocal(0L));
    }

    // Tests convertUTCToLocal and convertLocalToUTC basic conversions
    @Test
    public void testConvertUTCToLocalAndBack_standardValues_correctConversion() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        long utc = 10000000L;
        long local = zone.convertUTCToLocal(utc);
        assertEquals(utc + zone.getOffset(utc), local);

        long backToUtc = zone.convertLocalToUTC(local, false);
        assertEquals(utc, backToUtc);

        long withOriginal = zone.convertLocalToUTC(local, false, utc);
        assertEquals(utc, withOriginal);
    }

    // Tests convertUTCToLocal with long overflow throws ArithmeticException
    @Test(expected = ArithmeticException.class)
    public void testConvertUTCToLocal_overflow_throwsException() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(1);
        zone.convertUTCToLocal(Long.MAX_VALUE);
    }

    // Tests convertLocalToUTC in DST gap with strict=true throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConvertLocalToUTC_strictInGap_throwsException() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        // Spring forward 2008 gap: 2008-03-30 01:00 to 02:00
        // 2008-03-30 01:30 local time does not exist
        long gapLocal = 1206840600000L;
        zone.convertLocalToUTC(gapLocal, true);
    }

    // Tests isLocalDateTimeGap for gap and non-gap instances
    @Test
    public void testIsLocalDateTimeGap_gapAndNonGap_returnsExpected() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        LocalDateTime gapTime = new LocalDateTime(2008, 3, 30, 1, 30, 0, 0);
        assertTrue(zone.isLocalDateTimeGap(gapTime));

        LocalDateTime normalTime = new LocalDateTime(2008, 1, 1, 12, 0, 0, 0);
        assertFalse(zone.isLocalDateTimeGap(normalTime));

        assertFalse(DateTimeZone.UTC.isLocalDateTimeGap(gapTime));
    }

    // Tests getMillisKeepLocal across two different zones
    @Test
    public void testGetMillisKeepLocal_differentZones_preservesLocalTime() {
        DateTimeZone zoneLondon = DateTimeZone.forID("Europe/London");
        DateTimeZone zoneParis = DateTimeZone.forID("Europe/Paris");

        long instant = 0L;
        long converted = zoneLondon.getMillisKeepLocal(zoneParis, instant);
        assertEquals(zoneLondon.convertUTCToLocal(instant), zoneParis.convertUTCToLocal(converted));

        assertEquals(instant, zoneLondon.getMillisKeepLocal(zoneLondon, instant));
        assertEquals(zoneLondon.getMillisKeepLocal(DateTimeZone.getDefault(), instant),
                     zoneLondon.getMillisKeepLocal(null, instant));
    }

    // Tests isFixed, nextTransition, and previousTransition
    @Test
    public void testTransitions_fixedAndVariableZones() {
        assertTrue(DateTimeZone.UTC.isFixed());
        assertEquals(0L, DateTimeZone.UTC.nextTransition(0L));
        assertEquals(0L, DateTimeZone.UTC.previousTransition(0L));

        DateTimeZone london = DateTimeZone.forID("Europe/London");
        assertFalse(london.isFixed());
        long next = london.nextTransition(0L);
        assertTrue(next > 0L);
        long prev = london.previousTransition(next);
        assertTrue(prev <= 0L);
    }

    // Tests equals, hashCode, toString, and toTimeZone
    @Test
    public void testBasicObjectMethods_equalsHashCodeToString() {
        DateTimeZone zone1 = DateTimeZone.forID("Europe/London");
        DateTimeZone zone2 = DateTimeZone.forID("Europe/London");
        DateTimeZone zoneUTC = DateTimeZone.UTC;

        assertTrue(zone1.equals(zone2));
        assertFalse(zone1.equals(zoneUTC));
        assertFalse(zone1.equals("NotADateTimeZone"));
        assertEquals(zone1.hashCode(), zone2.hashCode());
        assertEquals("Europe/London", zone1.toString());

        TimeZone jdkZone = zone1.toTimeZone();
        assertEquals("Europe/London", jdkZone.getID());
    }

    // Tests serialization and deserialization via writeReplace / Stub
    @Test
    public void testSerialization_roundTrip_returnsSameInstance() throws Exception {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(zone);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTimeZone result = (DateTimeZone) ois.readObject();
        ois.close();

        assertSame(zone, result);
    }
}