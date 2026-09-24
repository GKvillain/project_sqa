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
    private TimeZone originalJvmDefault;

    @Before
    public void setUp() {
        originalDefault = DateTimeZone.getDefault();
        originalJvmDefault = TimeZone.getDefault();
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(originalDefault);
        TimeZone.setDefault(originalJvmDefault);
    }

    // Tests null input to forID returns default zone
    @Test
    public void testForID_nullId_returnsDefault() {
        assertEquals(DateTimeZone.getDefault(), DateTimeZone.forID(null));
    }

    // Tests UTC id to forID returns UTC instance
    @Test
    public void testForID_utcId_returnsUTC() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        assertEquals(DateTimeZone.UTC, zone);
        assertEquals("UTC", zone.getID());
    }

    // Tests valid long timezone id
    @Test
    public void testForID_validLongId_returnsZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertNotNull(zone);
        assertEquals("Europe/London", zone.getID());
    }

    // Tests offset string formats in forID
    @Test
    public void testForID_offsetString_returnsFixedOffsetZone() {
        DateTimeZone zone1 = DateTimeZone.forID("+02:00");
        assertEquals("+02:00", zone1.getID());
        assertEquals(2 * 3600 * 1000, zone1.getOffset(0L));

        DateTimeZone zone2 = DateTimeZone.forID("-08:00");
        assertEquals("-08:00", zone2.getID());
        assertEquals(-8 * 3600 * 1000, zone2.getOffset(0L));

        DateTimeZone zone3 = DateTimeZone.forID("+00:00");
        assertEquals(DateTimeZone.UTC, zone3);
    }

    // Tests invalid timezone id in forID throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testForID_invalidId_throwsException() {
        DateTimeZone.forID("Invalid/NonExistentZoneId");
    }

    // Tests forOffsetHours with zero returning UTC
    @Test
    public void testForOffsetHours_zero_returnsUTC() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(0);
        assertEquals(DateTimeZone.UTC, zone);
    }

    // Tests forOffsetHoursMinutes with positive offset
    @Test
    public void testForOffsetHoursMinutes_positiveHoursAndMinutes_returnsCorrectZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(2, 30);
        assertEquals("+02:30", zone.getID());
        assertEquals(2 * 3600 * 1000 + 30 * 60 * 1000, zone.getOffset(0L));
        assertTrue(zone.isFixed());
    }

    // Tests forOffsetHoursMinutes with negative offset
    @Test
    public void testForOffsetHoursMinutes_negativeHoursAndMinutes_returnsCorrectZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-2, 30);
        assertEquals("-02:30", zone.getID());
        assertEquals(-(2 * 3600 * 1000 + 30 * 60 * 1000), zone.getOffset(0L));
    }

    // Tests invalid minute boundaries in forOffsetHoursMinutes
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_negativeMinutes_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(1, -1);
    }

    // Tests invalid minute upper boundary in forOffsetHoursMinutes
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_minutesOutOfRange_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(1, 60);
    }

    // Tests forOffsetMillis creating fixed offset zone
    @Test
    public void testForOffsetMillis_validMillis_returnsZone() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000);
        assertEquals("+01:00", zone.getID());
        assertEquals(3600000, zone.getOffset(0L));
        assertEquals(zone, DateTimeZone.forOffsetMillis(3600000));
    }

    // Tests forTimeZone with null returning default
    @Test
    public void testForTimeZone_null_returnsDefault() {
        assertEquals(DateTimeZone.getDefault(), DateTimeZone.forTimeZone(null));
    }

    // Tests forTimeZone with UTC returning UTC instance
    @Test
    public void testForTimeZone_utcTimeZone_returnsUTC() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        assertEquals(DateTimeZone.UTC, DateTimeZone.forTimeZone(tz));
    }

    // Tests forTimeZone converting 3-letter alias mappings
    @Test
    public void testForTimeZone_oldAliases_convertsCorrectly() {
        DateTimeZone zonePst = DateTimeZone.forTimeZone(TimeZone.getTimeZone("PST"));
        assertEquals("America/Los_Angeles", zonePst.getID());

        DateTimeZone zoneEst = DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST"));
        assertEquals("America/New_York", zoneEst.getID());

        DateTimeZone zoneWet = DateTimeZone.forTimeZone(TimeZone.getTimeZone("WET"));
        assertEquals("Europe/London", zoneWet.getID());
    }

    // Tests forTimeZone with custom GMT offset
    @Test
    public void testForTimeZone_gmtOffset_returnsCorrectZone() {
        TimeZone tz = TimeZone.getTimeZone("GMT+05:30");
        DateTimeZone zone = DateTimeZone.forTimeZone(tz);
        assertEquals("+05:30", zone.getID());
        assertEquals(5 * 3600 * 1000 + 30 * 60 * 1000, zone.getOffset(0L));
    }

    // Tests getDefault and setDefault methods
    @Test
    public void testGetDefault_setDefault_worksCorrectly() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        DateTimeZone.setDefault(zone);
        assertEquals(zone, DateTimeZone.getDefault());
    }

    // Tests setDefault with null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetDefault_null_throwsException() {
        DateTimeZone.setDefault(null);
    }

    // Tests getAvailableIDs is not empty and contains UTC
    @Test
    public void testGetAvailableIDs_containsUTC() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertNotNull(ids);
        assertTrue(ids.contains("UTC"));
    }

    // Tests convertUTCToLocal and convertLocalToUTC in standard conditions
    @Test
    public void testConvertUTCToLocal_andLocalToUTC_roundTrip() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        long utc = 1000000000000L;
        long local = zone.convertUTCToLocal(utc);
        long convertedUtc = zone.convertLocalToUTC(local, false);
        assertEquals(utc, convertedUtc);
    }

    // Tests getMillisKeepLocal across different zones
    @Test
    public void testGetMillisKeepLocal_differentZones_returnsCorrectMillis() {
        DateTimeZone zoneUtc = DateTimeZone.UTC;
        DateTimeZone zonePlus2 = DateTimeZone.forOffsetHours(2);
        long utcMillis = 0L;
        long result = zoneUtc.getMillisKeepLocal(zonePlus2, utcMillis);
        assertEquals(-2 * 3600 * 1000, result);
    }

    // Tests serialization round-trip resolving to expected instance
    @Test
    public void testSerialization_roundTrip_resolvesCorrectly() throws Exception {
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
    }

    // Tests name retrieval methods with default and specific locale
    @Test
    public void testGetName_andShortName_returnsNonEmptyString() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long instant = 0L;
        assertNotNull(zone.getName(instant));
        assertNotNull(zone.getName(instant, Locale.ENGLISH));
        assertNotNull(zone.getShortName(instant));
        assertNotNull(zone.getShortName(instant, Locale.ENGLISH));
        assertTrue(zone.isStandardOffset(instant));
    }
}