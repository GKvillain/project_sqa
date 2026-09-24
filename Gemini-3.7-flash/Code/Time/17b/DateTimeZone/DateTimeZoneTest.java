package org.joda.time;

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

    // Tests adjusting offset during DST overlap to choose later offset
    @Test
    public void testAdjustOffset_dstOverlapLater_returnsLaterInstant() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        // 2011-10-30T02:00:00 local time is ambiguous (00:00:00 UTC CEST +02:00 or 01:00:00 UTC CET +01:00)
        long instantEarlier = 1319932800000L; // 2011-10-30T00:00:00Z (+02:00 = 02:00)
        long instantLater = 1319936400000L;   // 2011-10-30T01:00:00Z (+01:00 = 02:00)

        assertEquals(instantLater, zone.adjustOffset(instantEarlier, true));
        assertEquals(instantLater, zone.adjustOffset(instantLater, true));
    }

    // Tests adjusting offset during DST overlap to choose earlier offset
    @Test
    public void testAdjustOffset_dstOverlapEarlier_returnsEarlierInstant() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        long instantEarlier = 1319932800000L; // 2011-10-30T00:00:00Z (+02:00 = 02:00)
        long instantLater = 1319936400000L;   // 2011-10-30T01:00:00Z (+01:00 = 02:00)

        assertEquals(instantEarlier, zone.adjustOffset(instantLater, false));
        assertEquals(instantEarlier, zone.adjustOffset(instantEarlier, false));
    }

    // Tests adjusting offset when not in an overlap returns identical instant
    @Test
    public void testAdjustOffset_nonOverlapInstant_returnsSameInstant() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        long regularInstant = 1319900000000L;
        assertEquals(regularInstant, zone.adjustOffset(regularInstant, true));
        assertEquals(regularInstant, zone.adjustOffset(regularInstant, false));
    }

    // Tests forID with null returns default zone
    @Test
    public void testForID_nullInput_returnsDefaultZone() {
        DateTimeZone zone = DateTimeZone.forID(null);
        assertEquals(DateTimeZone.getDefault(), zone);
    }

    // Tests forID with UTC returns UTC instance
    @Test
    public void testForID_utcId_returnsUTC() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        assertSame(DateTimeZone.UTC, zone);
        assertEquals("UTC", zone.getID());
    }

    // Tests forID with offset strings
    @Test
    public void testForID_offsetString_returnsFixedZone() {
        DateTimeZone zonePlus = DateTimeZone.forID("+02:00");
        assertEquals("+02:00", zonePlus.getID());
        assertEquals(2 * 3600 * 1000, zonePlus.getOffset(0L));

        DateTimeZone zoneMinus = DateTimeZone.forID("-05:00");
        assertEquals("-05:00", zoneMinus.getID());
        assertEquals(-5 * 3600 * 1000, zoneMinus.getOffset(0L));

        DateTimeZone zoneZero = DateTimeZone.forID("+00:00");
        assertSame(DateTimeZone.UTC, zoneZero);
    }

    // Tests forID with invalid format throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testForID_invalidId_throwsException() {
        DateTimeZone.forID("InvalidZoneID");
    }

    // Tests forOffsetHours and forOffsetHoursMinutes valid inputs
    @Test
    public void testForOffsetHoursMinutes_validValues_returnsFixedZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(5, 30);
        assertEquals("+05:30", zone.getID());
        assertEquals((5 * 60 + 30) * 60 * 1000, zone.getOffset(0L));

        DateTimeZone negativeZone = DateTimeZone.forOffsetHoursMinutes(-4, 45);
        assertEquals("-04:45", negativeZone.getID());
        assertEquals(-(4 * 60 + 45) * 60 * 1000, negativeZone.getOffset(0L));

        DateTimeZone utcZone = DateTimeZone.forOffsetHoursMinutes(0, 0);
        assertSame(DateTimeZone.UTC, utcZone);
    }

    // Tests forOffsetHoursMinutes with negative minute throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_negativeMinutes_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(2, -15);
    }

    // Tests forOffsetHoursMinutes with minute >= 60 throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_minutesOutOfRange_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(2, 60);
    }

    // Tests forOffsetMillis zero returns UTC and non-zero returns fixed offset zone
    @Test
    public void testForOffsetMillis_variousOffsets_returnsCorrectZone() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));

        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000);
        assertEquals("+01:00", zone.getID());
        assertEquals(3600000, zone.getOffset(0L));
    }

    // Tests forTimeZone with various inputs
    @Test
    public void testForTimeZone_validInputs_returnsExpectedZone() {
        assertNotNull(DateTimeZone.forTimeZone(null));
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));

        DateTimeZone converted = DateTimeZone.forTimeZone(TimeZone.getTimeZone("PST"));
        assertEquals("America/Los_Angeles", converted.getID());

        DateTimeZone gmtZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+03:00"));
        assertEquals("+03:00", gmtZone.getID());
    }

    // Tests convertUTCToLocal and convertLocalToUTC standard roundtrip
    @Test
    public void testConvertUTCToLocal_and_ConvertLocalToUTC_roundTrip() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long utcMillis = 1000000000000L;
        long localMillis = zone.convertUTCToLocal(utcMillis);
        long resultUTC = zone.convertLocalToUTC(localMillis, false);
        assertEquals(utcMillis, resultUTC);
    }

    // Tests convertLocalToUTC in DST gap with strict=true throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConvertLocalToUTC_inDstGapStrict_throwsException() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // 2007-03-11 02:30:00 EST gap -> does not exist locally
        // 2007-03-11 02:00 EST is transition to 03:00 EDT
        // 1173598200000L is 2007-03-11T02:30:00.000 (local millis representation)
        long gapLocalMillis = new DateTime(2007, 3, 11, 2, 30, DateTimeZone.UTC).getMillis();
        zone.convertLocalToUTC(gapLocalMillis, true);
    }

    // Tests isLocalDateTimeGap method
    @Test
    public void testIsLocalDateTimeGap_gapTime_returnsTrue() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        LocalDateTime gapTime = new LocalDateTime(2007, 3, 11, 2, 30, 0, 0);
        assertTrue(zone.isLocalDateTimeGap(gapTime));

        LocalDateTime regularTime = new LocalDateTime(2007, 3, 11, 4, 30, 0, 0);
        assertFalse(zone.isLocalDateTimeGap(regularTime));
    }

    // Tests isStandardOffset for winter vs summer
    @Test
    public void testIsStandardOffset_summerAndWinter_returnsExpected() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long winterInstant = new DateTime(2011, 1, 1, 0, 0, DateTimeZone.UTC).getMillis();
        long summerInstant = new DateTime(2011, 7, 1, 0, 0, DateTimeZone.UTC).getMillis();

        assertTrue(zone.isStandardOffset(winterInstant));
        assertFalse(zone.isStandardOffset(summerInstant));
    }

    // Tests getMillisKeepLocal across different zones
    @Test
    public void testGetMillisKeepLocal_differentZones_preservesLocalTime() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        DateTimeZone tokyo = DateTimeZone.forID("Asia/Tokyo");

        long instantInLondon = new DateTime(2011, 1, 1, 12, 0, london).getMillis();
        long instantInTokyo = london.getMillisKeepLocal(tokyo, instantInLondon);

        DateTime dtLondon = new DateTime(instantInLondon, london);
        DateTime dtTokyo = new DateTime(instantInTokyo, tokyo);

        assertEquals(dtLondon.getHourOfDay(), dtTokyo.getHourOfDay());
        assertEquals(dtLondon.getMinuteOfHour(), dtTokyo.getMinuteOfHour());
    }

    // Tests setDefault with null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetDefault_nullZone_throwsException() {
        DateTimeZone.setDefault(null);
    }

    // Tests basic getters: getAvailableIDs, getName, getShortName, equals, hashCode, toString
    @Test
    public void testBasicMethods_validZone_returnsExpectedValues() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertNotNull(ids);
        assertTrue(ids.contains("UTC"));

        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        assertEquals("Europe/Paris", zone.toString());
        assertEquals("Europe/Paris", zone.getID());
        assertNotNull(zone.getName(0L, Locale.ENGLISH));
        assertNotNull(zone.getShortName(0L, Locale.ENGLISH));

        assertEquals(zone, DateTimeZone.forID("Europe/Paris"));
        assertNotEquals(zone, DateTimeZone.UTC);
        assertEquals(zone.hashCode(), DateTimeZone.forID("Europe/Paris").hashCode());
    }
}