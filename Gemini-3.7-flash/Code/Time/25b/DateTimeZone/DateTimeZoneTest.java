package org.joda.time;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.joda.time.tz.NameProvider;
import org.joda.time.tz.Provider;
import org.joda.time.tz.UTCProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class DateTimeZoneTest {

    private DateTimeZone originalDefaultZone;
    private Locale originalLocale;

    @Before
    public void setUp() {
        originalDefaultZone = DateTimeZone.getDefault();
        originalLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(originalDefaultZone);
        Locale.setDefault(originalLocale);
    }

    // Tests getting UTC zone directly and via forID
    @Test
    public void testForID_utcString_returnsUTC() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        assertSame(DateTimeZone.UTC, zone);
        assertEquals("UTC", zone.getID());
        assertEquals(0, zone.getOffset(0L));
        assertTrue(zone.isFixed());
    }

    // Tests forID with null returning the default DateTimeZone
    @Test
    public void testForID_nullInput_returnsDefaultZone() {
        DateTimeZone zone = DateTimeZone.forID(null);
        assertEquals(DateTimeZone.getDefault(), zone);
    }

    // Tests forID with positive and negative fixed offset strings
    @Test
    public void testForID_validOffsetStrings_returnsFixedOffsetZone() {
        DateTimeZone zonePlus = DateTimeZone.forID("+02:00");
        assertEquals("+02:00", zonePlus.getID());
        assertEquals(2 * 3600 * 1000, zonePlus.getOffset(0L));

        DateTimeZone zoneMinus = DateTimeZone.forID("-05:30");
        assertEquals("-05:30", zoneMinus.getID());
        assertEquals(-(5 * 3600 * 1000 + 30 * 60 * 1000), zoneMinus.getOffset(0L));

        DateTimeZone zoneZero = DateTimeZone.forID("+00:00");
        assertSame(DateTimeZone.UTC, zoneZero);
    }

    // Tests forID throwing IllegalArgumentException on unrecognized zone
    @Test(expected = IllegalArgumentException.class)
    public void testForID_unrecognisedId_throwsIllegalArgumentException() {
        DateTimeZone.forID("Invalid/Zone_Name");
    }

    // Tests forOffsetHours and forOffsetHoursMinutes normal cases
    @Test
    public void testForOffsetHoursMinutes_validOffsets_returnsCorrectZone() {
        DateTimeZone zone1 = DateTimeZone.forOffsetHours(3);
        assertEquals("+03:00", zone1.getID());
        assertEquals(3 * 3600 * 1000, zone1.getOffset(0L));

        DateTimeZone zone2 = DateTimeZone.forOffsetHoursMinutes(-4, 45);
        assertEquals("-04:45", zone2.getID());
        assertEquals(-(4 * 3600 * 1000 + 45 * 60 * 1000), zone2.getOffset(0L));

        DateTimeZone zoneZero = DateTimeZone.forOffsetHoursMinutes(0, 0);
        assertSame(DateTimeZone.UTC, zoneZero);
    }

    // Tests forOffsetHoursMinutes throwing exception on invalid minutes
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_minutesOutOfRange_throwsIllegalArgumentException() {
        DateTimeZone.forOffsetHoursMinutes(5, 60);
    }

    // Tests forOffsetMillis boundary and general values
    @Test
    public void testForOffsetMillis_variousValues_returnsCorrectZone() {
        DateTimeZone zoneMillis = DateTimeZone.forOffsetMillis(3600000);
        assertEquals("+01:00", zoneMillis.getID());
        assertEquals(3600000, zoneMillis.getOffset(0L));

        DateTimeZone zoneZero = DateTimeZone.forOffsetMillis(0);
        assertSame(DateTimeZone.UTC, zoneZero);
    }

    // Tests forTimeZone with conversion and fallback
    @Test
    public void testForTimeZone_standardTimeZones_convertsCorrectly() {
        DateTimeZone zoneNull = DateTimeZone.forTimeZone(null);
        assertEquals(DateTimeZone.getDefault(), zoneNull);

        DateTimeZone zoneUTC = DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC"));
        assertSame(DateTimeZone.UTC, zoneUTC);

        DateTimeZone zoneEST = DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST"));
        assertEquals("America/New_York", zoneEST.getID());

        DateTimeZone zoneCustom = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+04:00"));
        assertEquals("+04:00", zoneCustom.getID());
    }

    // Tests setDefault and validation
    @Test
    public void testSetDefault_validAndNull_updatesAndThrows() {
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        DateTimeZone.setDefault(paris);
        assertEquals(paris, DateTimeZone.getDefault());

        try {
            DateTimeZone.setDefault(null);
            org.junit.Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    // Tests getOffsetFromLocal across DST overlap and gap conditions
    @Test
    public void testGetOffsetFromLocal_dstTransitions() {
        DateTimeZone zoneParis = DateTimeZone.forID("Europe/Paris");
        // Winter standard time (UTC+1)
        long winterInstant = new DateTime(2011, 1, 15, 12, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertEquals(3600000, zoneParis.getOffsetFromLocal(winterInstant));

        // Summer daylight savings time (UTC+2)
        long summerInstant = new DateTime(2011, 7, 15, 12, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertEquals(7200000, zoneParis.getOffsetFromLocal(summerInstant));

        // Overlap in Autumn: Europe/Moscow transition in 2007 (Oct 28, 2007 02:00..03:00 repeated)
        DateTimeZone zoneMoscow = DateTimeZone.forID("Europe/Moscow");
        long overlapLocal = new DateTime(2007, 10, 28, 2, 30, 0, 0, DateTimeZone.UTC).getMillis();
        assertEquals(4 * 3600 * 1000, zoneMoscow.getOffsetFromLocal(overlapLocal));

        // Gap in Spring: Europe/London gap (March 25, 2007 01:00 -> 02:00)
        DateTimeZone zoneLondon = DateTimeZone.forID("Europe/London");
        long gapLocal = new DateTime(2007, 3, 25, 1, 30, 0, 0, DateTimeZone.UTC).getMillis();
        assertEquals(3600000, zoneLondon.getOffsetFromLocal(gapLocal));
    }

    // Tests convertUTCToLocal and convertLocalToUTC
    @Test
    public void testConvertUTCToLocal_andConvertLocalToUTC() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        long utcMillis = 10000000L;
        long localMillis = zone.convertUTCToLocal(utcMillis);
        assertEquals(utcMillis + 2 * 3600 * 1000, localMillis);

        long convertedBack = zone.convertLocalToUTC(localMillis, false);
        assertEquals(utcMillis, convertedBack);

        long convertedBackStrict = zone.convertLocalToUTC(localMillis, true);
        assertEquals(utcMillis, convertedBackStrict);

        long convertedWithOriginal = zone.convertLocalToUTC(localMillis, false, utcMillis);
        assertEquals(utcMillis, convertedWithOriginal);
    }

    // Tests convertLocalToUTC throwing IllegalArgumentException during DST gap in strict mode
    @Test(expected = IllegalArgumentException.class)
    public void testConvertLocalToUTC_strictInGap_throwsIllegalArgumentException() {
        DateTimeZone zoneLondon = DateTimeZone.forID("Europe/London");
        // 2007-03-25 01:30:00 does not exist in Europe/London
        long gapLocal = new DateTime(2007, 3, 25, 1, 30, 0, 0, DateTimeZone.UTC).getMillis();
        zoneLondon.convertLocalToUTC(gapLocal, true);
    }

    // Tests isLocalDateTimeGap
    @Test
    public void testIsLocalDateTimeGap_gapAndNonGap() {
        DateTimeZone zoneLondon = DateTimeZone.forID("Europe/London");
        LocalDateTime inGap = new LocalDateTime(2007, 3, 25, 1, 30);
        assertTrue(zoneLondon.isLocalDateTimeGap(inGap));

        LocalDateTime notInGap = new LocalDateTime(2007, 3, 25, 0, 30);
        assertFalse(zoneLondon.isLocalDateTimeGap(notInGap));

        DateTimeZone fixedZone = DateTimeZone.forOffsetHours(1);
        assertFalse(fixedZone.isLocalDateTimeGap(inGap));
    }

    // Tests getMillisKeepLocal
    @Test
    public void testGetMillisKeepLocal_differentZones() {
        DateTimeZone zoneLondon = DateTimeZone.forID("Europe/London");
        DateTimeZone zoneParis = DateTimeZone.forID("Europe/Paris");

        // 12:00 in London to same wall time in Paris
        long instantLondon = new DateTime(2011, 1, 15, 12, 0, DateTimeZone.UTC).getMillis();
        long instantParis = zoneLondon.getMillisKeepLocal(zoneParis, instantLondon);
        assertEquals(instantLondon - 3600000L, instantParis);

        long sameZoneInstant = zoneLondon.getMillisKeepLocal(zoneLondon, instantLondon);
        assertEquals(instantLondon, sameZoneInstant);
    }

    // Tests name retrieval methods
    @Test
    public void testGetName_andGetShortName() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long winterInstant = new DateTime(2011, 1, 15, 12, 0, DateTimeZone.UTC).getMillis();

        String shortName = zone.getShortName(winterInstant, Locale.ENGLISH);
        assertNotNull(shortName);

        String longName = zone.getName(winterInstant, Locale.ENGLISH);
        assertNotNull(longName);

        assertNotNull(zone.getShortName(winterInstant));
        assertNotNull(zone.getName(winterInstant));
    }

    // Tests standard offset check
    @Test
    public void testIsStandardOffset() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long winterInstant = new DateTime(2011, 1, 15, 12, 0, DateTimeZone.UTC).getMillis();
        long summerInstant = new DateTime(2011, 7, 15, 12, 0, DateTimeZone.UTC).getMillis();

        assertTrue(zone.isStandardOffset(winterInstant));
        assertFalse(zone.isStandardOffset(summerInstant));
    }

    // Tests available IDs set and providers
    @Test
    public void testGetAvailableIDs_andProviderGetters() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertNotNull(ids);
        assertTrue(ids.contains("UTC"));
        assertTrue(ids.contains("Europe/London"));

        Provider provider = DateTimeZone.getProvider();
        assertNotNull(provider);

        NameProvider nameProvider = DateTimeZone.getNameProvider();
        assertNotNull(nameProvider);
    }

    // Tests serialization and deserialization replacing with singleton/cached instance
    @Test
    public void testSerialization_roundTrip_returnsCorrectInstance() throws Exception {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(zone);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTimeZone deserialized = (DateTimeZone) ois.readObject();
        ois.close();

        assertEquals(zone, deserialized);
        assertSame(zone, deserialized);
    }

    // Tests equals, hashCode, and toString
    @Test
    public void testEquals_hashCode_toString() {
        DateTimeZone zone1 = DateTimeZone.forOffsetHours(3);
        DateTimeZone zone2 = DateTimeZone.forOffsetHoursMinutes(3, 0);
        DateTimeZone zone3 = DateTimeZone.forOffsetHours(4);

        assertEquals(zone1, zone2);
        assertFalse(zone1.equals(zone3));
        assertFalse(zone1.equals("Not a DateTimeZone"));
        assertEquals(zone1.hashCode(), zone2.hashCode());
        assertEquals("+03:00", zone1.toString());
    }
}