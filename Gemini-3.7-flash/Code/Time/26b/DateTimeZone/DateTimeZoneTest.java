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
    private Locale originalLocale;

    @Before
    public void setUp() {
        originalDefault = DateTimeZone.getDefault();
        originalLocale = Locale.getDefault();
        Locale.setDefault(Locale.UK);
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(originalDefault);
        Locale.setDefault(originalLocale);
    }

    // Tests forID with standard long IDs
    @Test
    public void testForID_validLongId_returnsZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertNotNull(zone);
        assertEquals("Europe/London", zone.getID());
    }

    // Tests forID with null input returning default zone
    @Test
    public void testForID_nullId_returnsDefaultZone() {
        DateTimeZone zone = DateTimeZone.forID(null);
        assertEquals(DateTimeZone.getDefault(), zone);
    }

    // Tests forID with UTC
    @Test
    public void testForID_utcId_returnsUTC() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        assertSame(DateTimeZone.UTC, zone);
        assertEquals("UTC", zone.getID());
        assertEquals(0, zone.getOffset(0L));
        assertTrue(zone.isFixed());
    }

    // Tests forID with valid offset strings (+hh:mm and -hh:mm)
    @Test
    public void testForID_offsetString_returnsFixedOffsetZone() {
        DateTimeZone zonePlus = DateTimeZone.forID("+02:00");
        assertEquals("+02:00", zonePlus.getID());
        assertEquals(2 * 3600 * 1000, zonePlus.getOffset(0L));
        assertTrue(zonePlus.isFixed());

        DateTimeZone zoneMinus = DateTimeZone.forID("-08:30");
        assertEquals("-08:30", zoneMinus.getID());
        assertEquals(-(8 * 3600 * 1000 + 30 * 60 * 1000), zoneMinus.getOffset(0L));

        DateTimeZone zoneZero = DateTimeZone.forID("+00:00");
        assertSame(DateTimeZone.UTC, zoneZero);
    }

    // Tests forID with unrecognized string throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testForID_invalidId_throwsIllegalArgumentException() {
        DateTimeZone.forID("Invalid/NonExistentZone");
    }

    // Tests forOffsetHours with normal and zero values
    @Test
    public void testForOffsetHours_validValues_returnsZone() {
        DateTimeZone zoneZero = DateTimeZone.forOffsetHours(0);
        assertSame(DateTimeZone.UTC, zoneZero);

        DateTimeZone zoneFive = DateTimeZone.forOffsetHours(5);
        assertEquals("+05:00", zoneFive.getID());
        assertEquals(5 * 3600 * 1000, zoneFive.getOffset(0L));

        DateTimeZone zoneNegFive = DateTimeZone.forOffsetHours(-5);
        assertEquals("-05:00", zoneNegFive.getID());
        assertEquals(-5 * 3600 * 1000, zoneNegFive.getOffset(0L));
    }

    // Tests forOffsetHoursMinutes valid combinations and boundary limits
    @Test
    public void testForOffsetHoursMinutes_validCombinations_returnsZone() {
        DateTimeZone zonePos = DateTimeZone.forOffsetHoursMinutes(5, 30);
        assertEquals("+05:30", zonePos.getID());
        assertEquals(5 * 3600 * 1000 + 30 * 60 * 1000, zonePos.getOffset(0L));

        DateTimeZone zoneNeg = DateTimeZone.forOffsetHoursMinutes(-5, 30);
        assertEquals("-05:30", zoneNeg.getID());
        assertEquals(-(5 * 3600 * 1000 + 30 * 60 * 1000), zoneNeg.getOffset(0L));
    }

    // Tests forOffsetHoursMinutes with negative minute throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_negativeMinutes_throwsIllegalArgumentException() {
        DateTimeZone.forOffsetHoursMinutes(2, -1);
    }

    // Tests forOffsetHoursMinutes with minutes > 59 throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_minutesTooLarge_throwsIllegalArgumentException() {
        DateTimeZone.forOffsetHoursMinutes(2, 60);
    }

    // Tests forOffsetMillis factory method
    @Test
    public void testForOffsetMillis_validMillis_returnsZone() {
        DateTimeZone zoneZero = DateTimeZone.forOffsetMillis(0);
        assertSame(DateTimeZone.UTC, zoneZero);

        DateTimeZone zoneMillis = DateTimeZone.forOffsetMillis(3600000);
        assertEquals("+01:00", zoneMillis.getID());
        assertEquals(3600000, zoneMillis.getOffset(0L));
    }

    // Tests forTimeZone with JDK TimeZone mapping
    @Test
    public void testForTimeZone_validAndConvertedId_returnsZone() {
        DateTimeZone zoneNull = DateTimeZone.forTimeZone(null);
        assertEquals(DateTimeZone.getDefault(), zoneNull);

        DateTimeZone zoneUtc = DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC"));
        assertSame(DateTimeZone.UTC, zoneUtc);

        DateTimeZone zoneEst = DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST"));
        assertEquals("America/New_York", zoneEst.getID());
    }

    // Tests setDefault and validation
    @Test
    public void testSetDefault_validZone_updatesDefault() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        DateTimeZone.setDefault(london);
        assertEquals(london, DateTimeZone.getDefault());
    }

    // Tests setDefault null argument
    @Test(expected = IllegalArgumentException.class)
    public void testSetDefault_nullZone_throwsIllegalArgumentException() {
        DateTimeZone.setDefault(null);
    }

    // Tests convertUTCToLocal calculation and arithmetic overflow
    @Test
    public void testConvertUTCToLocal_standardAndOverflow() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        long local = zone.convertUTCToLocal(1000L);
        assertEquals(1000L + 2 * 3600 * 1000L, local);

        try {
            zone.convertUTCToLocal(Long.MAX_VALUE);
            fail("Expected ArithmeticException on overflow");
        } catch (ArithmeticException ex) {
            // Expected
        }
    }

    // Tests convertLocalToUTC in normal case and gap with strict=false
    @Test
    public void testConvertLocalToUTC_normalAndNonStrict() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(3);
        long utc = zone.convertLocalToUTC(10000000L, false);
        assertEquals(10000000L - 3 * 3600 * 1000L, utc);
    }

    // Tests convertLocalToUTC in DST gap with strict=true throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConvertLocalToUTC_strictInDstGap_throwsIllegalArgumentException() {
        // Paris DST gap on 2007-03-25: 02:00 -> 03:00 (spring forward)
        // 2007-03-25T02:30:00.000 in Paris does not exist locally
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        long gapLocalMillis = new DateTime(2007, 3, 25, 2, 30, 0, 0, DateTimeZone.UTC).getMillis();
        paris.convertLocalToUTC(gapLocalMillis, true);
    }

    // Tests getMillisKeepLocal across two different zones
    @Test
    public void testGetMillisKeepLocal_betweenZones_maintainsLocalClock() {
        DateTimeZone zoneLondon = DateTimeZone.forID("Europe/London");
        DateTimeZone zoneParis = DateTimeZone.forID("Europe/Paris");

        long instantUTC = new DateTime(2010, 1, 1, 12, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long resultUTC = zoneLondon.getMillisKeepLocal(zoneParis, instantUTC);

        // London at 2010-01-01T12:00Z is 12:00 local (UTC+0).
        // 12:00 local in Paris (UTC+1) corresponds to 11:00Z.
        assertEquals(instantUTC - 3600000L, resultUTC);
    }

    // Tests getOffset with ReadableInstant (both non-null and null)
    @Test
    public void testGetOffset_readableInstant_returnsCorrectOffset() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        DateTime winterInstant = new DateTime(2010, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        DateTime summerInstant = new DateTime(2010, 7, 1, 0, 0, 0, 0, DateTimeZone.UTC);

        assertEquals(0, zone.getOffset(winterInstant));
        assertEquals(3600000, zone.getOffset(summerInstant));

        int offsetNow = zone.getOffset((ReadableInstant) null);
        assertEquals(zone.getOffset(DateTimeUtils.currentTimeMillis()), offsetNow);
    }

    // Tests isStandardOffset for winter (standard) vs summer (DST)
    @Test
    public void testIsStandardOffset_dstAndNonDst_returnsExpected() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long winter = new DateTime(2010, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long summer = new DateTime(2010, 7, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();

        assertTrue(zone.isStandardOffset(winter));
        assertFalse(zone.isStandardOffset(summer));
    }

    // Tests isLocalDateTimeGap during daylight savings transition
    @Test
    public void testIsLocalDateTimeGap_gapAndNonGap_returnsCorrectResult() {
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        LocalDateTime gapTime = new LocalDateTime(2007, 3, 25, 2, 30, 0, 0);
        LocalDateTime normalTime = new LocalDateTime(2007, 3, 25, 4, 30, 0, 0);

        assertTrue(paris.isLocalDateTimeGap(gapTime));
        assertFalse(paris.isLocalDateTimeGap(normalTime));
        assertFalse(DateTimeZone.UTC.isLocalDateTimeGap(gapTime));
    }

    // Tests available IDs, names, hashCode, equals, toString, and serialization
    @Test
    public void testBasicMethodsAndSerialization_expectedResults() throws Exception {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertNotNull(ids);
        assertTrue(ids.contains("UTC"));
        assertTrue(ids.contains("Europe/London"));

        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertEquals("Europe/London", zone.toString());
        assertEquals("Europe/London", zone.getID());
        assertEquals(57 + "Europe/London".hashCode(), zone.hashCode());
        assertTrue(zone.equals(DateTimeZone.forID("Europe/London")));
        assertFalse(zone.equals(DateTimeZone.UTC));
        assertFalse(zone.equals(null));

        assertNotNull(zone.getName(0L));
        assertNotNull(zone.getShortName(0L));
        assertEquals("Europe/London", zone.toTimeZone().getID());

        // Test serialization round-trip
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