package org.joda.time;

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

    // Tests zero hours and negative minutes (Defects4J Time-8 bug detection)
    @Test
    public void testForOffsetHoursMinutes_zeroHoursNegativeMinutes_returnsCorrectZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(0, -15);
        assertEquals("-00:15", zone.getID());
        assertEquals(-15 * 60 * 1000, zone.getOffset(0L));
    }

    // Tests negative hours and negative minutes
    @Test
    public void testForOffsetHoursMinutes_negativeHoursNegativeMinutes_returnsCorrectZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-2, -15);
        assertEquals("-02:15", zone.getID());
        assertEquals(-(2 * 60 + 15) * 60 * 1000, zone.getOffset(0L));
    }

    // Tests negative hours and positive minutes
    @Test
    public void testForOffsetHoursMinutes_negativeHoursPositiveMinutes_returnsCorrectZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-2, 15);
        assertEquals("-02:15", zone.getID());
        assertEquals(-(2 * 60 + 15) * 60 * 1000, zone.getOffset(0L));
    }

    // Tests positive hours and positive minutes
    @Test
    public void testForOffsetHoursMinutes_positiveHoursPositiveMinutes_returnsCorrectZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(2, 15);
        assertEquals("+02:15", zone.getID());
        assertEquals((2 * 60 + 15) * 60 * 1000, zone.getOffset(0L));
    }

    // Tests positive hours and negative minutes (should throw IllegalArgumentException)
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_positiveHoursNegativeMinutes_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(2, -15);
    }

    // Tests zero hours and zero minutes returns UTC
    @Test
    public void testForOffsetHoursMinutes_zeroHoursZeroMinutes_returnsUTC() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(0, 0);
        assertSame(DateTimeZone.UTC, zone);
    }

    // Tests hours boundary beyond +23 throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_hoursTooHigh_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(24, 0);
    }

    // Tests hours boundary below -23 throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_hoursTooLow_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(-24, 0);
    }

    // Tests minutes boundary beyond +59 throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_minutesTooHigh_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(0, 60);
    }

    // Tests minutes boundary below -59 throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_minutesTooLow_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(0, -60);
    }

    // Tests forOffsetHours convenience method
    @Test
    public void testForOffsetHours_validPositiveHour_returnsCorrectZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(5);
        assertEquals("+05:00", zone.getID());
        assertEquals(5 * 3600 * 1000, zone.getOffset(0L));
    }

    // Tests forOffsetMillis within valid range
    @Test
    public void testForOffsetMillis_validMillis_returnsCorrectZone() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000);
        assertEquals("+01:00", zone.getID());
        assertEquals(3600000, zone.getOffset(0L));
        assertTrue(zone.isFixed());
    }

    // Tests forOffsetMillis with 0 returns UTC
    @Test
    public void testForOffsetMillis_zeroMillis_returnsUTC() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(0);
        assertSame(DateTimeZone.UTC, zone);
    }

    // Tests forOffsetMillis exceeding maximum allowed offset throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetMillis_exceedsMaxLimit_throwsException() {
        DateTimeZone.forOffsetMillis(86400 * 1000);
    }

    // Tests forID with null returns default zone
    @Test
    public void testForID_nullId_returnsDefault() {
        DateTimeZone zone = DateTimeZone.forID(null);
        assertEquals(DateTimeZone.getDefault(), zone);
    }

    // Tests forID with "UTC"
    @Test
    public void testForID_utcString_returnsUTC() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        assertSame(DateTimeZone.UTC, zone);
    }

    // Tests forID with valid formatted offset string
    @Test
    public void testForID_validOffsetString_returnsCorrectZone() {
        DateTimeZone zone = DateTimeZone.forID("-08:00");
        assertEquals("-08:00", zone.getID());
        assertEquals(-8 * 3600 * 1000, zone.getOffset(0L));
    }

    // Tests forID with unrecognized ID throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testForID_unknownId_throwsException() {
        DateTimeZone.forID("NonExistentZone/Unknown");
    }

    // Tests forTimeZone with JDK TimeZone conversion
    @Test
    public void testForTimeZone_jdkTimeZone_returnsMappedZone() {
        TimeZone jdkZone = TimeZone.getTimeZone("GMT+02:00");
        DateTimeZone zone = DateTimeZone.forTimeZone(jdkZone);
        assertEquals("+02:00", zone.getID());
    }

    // Tests forTimeZone with null returns default zone
    @Test
    public void testForTimeZone_nullZone_returnsDefault() {
        DateTimeZone zone = DateTimeZone.forTimeZone(null);
        assertEquals(DateTimeZone.getDefault(), zone);
    }

    // Tests setDefault with null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetDefault_nullZone_throwsException() {
        DateTimeZone.setDefault(null);
    }

    // Tests setDefault changes default zone
    @Test
    public void testSetDefault_validZone_setsDefault() {
        DateTimeZone newDefault = DateTimeZone.UTC;
        DateTimeZone.setDefault(newDefault);
        assertSame(DateTimeZone.UTC, DateTimeZone.getDefault());
    }

    // Tests UTC conversions and offset calculations
    @Test
    public void testConvertUTCToLocal_fixedZone_calculatesCorrectLocalMillis() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(3);
        long utcMillis = 10000000L;
        long localMillis = zone.convertUTCToLocal(utcMillis);
        assertEquals(utcMillis + (3 * 3600 * 1000), localMillis);
        assertEquals(utcMillis, zone.convertLocalToUTC(localMillis, false));
    }

    // Tests getAvailableIDs is not empty and contains UTC
    @Test
    public void testGetAvailableIDs_notEmpty_containsUTC() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertNotNull(ids);
        assertFalse(ids.isEmpty());
        assertTrue(ids.contains("UTC"));
    }
}