package org.joda.time;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
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

    // Tests null id defaults to default zone
    @Test
    public void testForID_nullId_returnsDefault() {
        assertEquals(DateTimeZone.getDefault(), DateTimeZone.forID(null));
    }

    // Tests UTC zone lookup
    @Test
    public void testForID_utcId_returnsUTC() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("UTC"));
    }

    // Tests parsing positive and negative fixed offset string
    @Test
    public void testForID_offsetString_returnsFixedOffsetZone() {
        DateTimeZone zonePlus = DateTimeZone.forID("+02:00");
        assertEquals("+02:00", zonePlus.getID());
        assertEquals(2 * DateTimeConstants.MILLIS_PER_HOUR, zonePlus.getOffset(0L));

        DateTimeZone zoneMinus = DateTimeZone.forID("-05:30");
        assertEquals("-05:30", zoneMinus.getID());
        assertEquals(-(5 * DateTimeConstants.MILLIS_PER_HOUR + 30 * DateTimeConstants.MILLIS_PER_MINUTE), zoneMinus.getOffset(0L));

        DateTimeZone zoneZero = DateTimeZone.forID("+00:00");
        assertSame(DateTimeZone.UTC, zoneZero);
    }

    // Tests unknown zone ID throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testForID_invalidId_throwsException() {
        DateTimeZone.forID("Invalid/Zone_Name");
    }

    // Tests forOffsetHours and forOffsetHoursMinutes factory methods
    @Test
    public void testForOffsetHoursMinutes_validValues_returnsCorrectZone() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));

        DateTimeZone zone1 = DateTimeZone.forOffsetHours(3);
        assertEquals("+03:00", zone1.getID());
        assertEquals(3 * DateTimeConstants.MILLIS_PER_HOUR, zone1.getOffset(0L));

        DateTimeZone zone2 = DateTimeZone.forOffsetHoursMinutes(-4, 30);
        assertEquals("-04:30", zone2.getID());
        assertEquals(-(4 * DateTimeConstants.MILLIS_PER_HOUR + 30 * DateTimeConstants.MILLIS_PER_MINUTE), zone2.getOffset(0L));
    }

    // Tests forOffsetHoursMinutes with invalid minute range
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_invalidMinutes_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(2, 60);
    }

    // Tests forOffsetHoursMinutes with negative minute
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_negativeMinutes_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(2, -1);
    }

    // Tests forOffsetHoursMinutes when offset overflows integer
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_hoursTooLarge_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(Integer.MAX_VALUE, 0);
    }

    // Tests forTimeZone conversion from java.util.TimeZone
    @Test
    public void testForTimeZone_validTimeZone_returnsEquivalentZone() {
        assertEquals(DateTimeZone.getDefault(), DateTimeZone.forTimeZone(null));
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));

        DateTimeZone zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+02:00"));
        assertEquals("+02:00", zone.getID());

        DateTimeZone zoneConverted = DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST"));
        assertEquals("America/New_York", zoneConverted.getID());
    }

    // Tests getting and setting default DateTimeZone
    @Test
    public void testGetAndSetDefault_validZone_updatesDefault() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        DateTimeZone.setDefault(zone);
        assertEquals(zone, DateTimeZone.getDefault());
    }

    // Tests setting null default throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetDefault_nullZone_throwsException() {
        DateTimeZone.setDefault(null);
    }

    // Tests getAvailableIDs and provider getters
    @Test
    public void testGetAvailableIDsAndProvider_notNull() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertNotNull(ids);
        assertTrue(ids.contains("UTC"));
        assertNotNull(DateTimeZone.getProvider());
        assertNotNull(DateTimeZone.getNameProvider());
    }

    // Tests convertUTCToLocal and convertLocalToUTC standard conversion
    @Test
    public void testConvertUTCToLocalAndLocalToUTC_roundTrip() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        long utcMillis = 10000000L;
        long localMillis = zone.convertUTCToLocal(utcMillis);
        assertEquals(utcMillis + 2 * DateTimeConstants.MILLIS_PER_HOUR, localMillis);
        assertEquals(utcMillis, zone.convertLocalToUTC(localMillis, false));
        assertEquals(utcMillis, zone.convertLocalToUTC(localMillis, true));
    }

    // Tests convertLocalToUTC during strict daylight savings transition gap
    @Test(expected = IllegalArgumentException.class)
    public void testConvertLocalToUTC_strictGapTransition_throwsException() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // 2007-03-11 02:30:00 is in the DST gap in America/New_York
        DateTime dt = new DateTime(2007, 3, 11, 3, 30, 0, 0, DateTimeZone.UTC);
        long localMillis = dt.getMillis() - DateTimeConstants.MILLIS_PER_HOUR;
        zone.convertLocalToUTC(localMillis, true);
    }

    // Tests convertUTCToLocal arithmetic overflow
    @Test(expected = ArithmeticException.class)
    public void testConvertUTCToLocal_overflow_throwsArithmeticException() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        zone.convertUTCToLocal(Long.MAX_VALUE);
    }

    // Tests getMillisKeepLocal across different zones
    @Test
    public void testGetMillisKeepLocal_differentZones_preservesLocalTime() {
        DateTimeZone zoneLondon = DateTimeZone.forID("Europe/London");
        DateTimeZone zoneParis = DateTimeZone.forID("Europe/Paris");

        long instant = 0L;
        long sameInstant = zoneLondon.getMillisKeepLocal(zoneLondon, instant);
        assertEquals(instant, sameInstant);

        long keepLocal = zoneLondon.getMillisKeepLocal(zoneParis, instant);
        long localLondon = zoneLondon.convertUTCToLocal(instant);
        long localParis = zoneParis.convertUTCToLocal(keepLocal);
        assertEquals(localLondon, localParis);
    }

    // Tests getName, getShortName, and getNameKey
    @Test
    public void testGetNameAndShortName_returnsValidStrings() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertNotNull(zone.getName(0L));
        assertNotNull(zone.getName(0L, Locale.UK));
        assertNotNull(zone.getShortName(0L));
        assertNotNull(zone.getShortName(0L, Locale.UK));
        assertNotNull(zone.getNameKey(0L));
    }

    // Tests getOffset with ReadableInstant and isStandardOffset
    @Test
    public void testGetOffsetAndIsStandardOffset_validValues() {
        DateTimeZone zone = DateTimeZone.UTC;
        assertEquals(0, zone.getOffset((ReadableInstant) null));
        assertEquals(0, zone.getOffset(new Instant(0L)));
        assertTrue(zone.isStandardOffset(0L));
        assertTrue(zone.isFixed());
    }

    // Tests isLocalDateTimeGap for fixed and non-fixed zones
    @Test
    public void testIsLocalDateTimeGap_fixedAndVariableZones() {
        DateTimeZone fixedZone = DateTimeZone.forOffsetHours(1);
        LocalDateTime ldt = new LocalDateTime(2007, 3, 11, 2, 30);
        assertFalse(fixedZone.isLocalDateTimeGap(ldt));

        DateTimeZone variableZone = DateTimeZone.forID("America/New_York");
        assertTrue(variableZone.isLocalDateTimeGap(ldt));
    }

    // Tests equals, hashCode, toString and serialization roundtrip
    @Test
    public void testEqualsHashCodeToStringAndSerialization() throws Exception {
        DateTimeZone zone1 = DateTimeZone.forID("America/New_York");
        DateTimeZone zone2 = DateTimeZone.forID("America/New_York");
        DateTimeZone zone3 = DateTimeZone.forOffsetHours(5);

        assertEquals(zone1, zone2);
        assertFalse(zone1.equals(zone3));
        assertFalse(zone1.equals(null));
        assertFalse(zone1.equals("NotAZone"));
        assertEquals(zone1.hashCode(), zone2.hashCode());
        assertEquals("America/New_York", zone1.toString());
        assertEquals("America/New_York", zone1.toTimeZone().getID());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(zone1);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTimeZone deserialized = (DateTimeZone) ois.readObject();
        ois.close();

        assertSame(zone1, deserialized);
    }

    // --- New tests for remaining uncovered paths ---

    @Test
    public void testForID_aliasesAndSpecialFormats() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("UT"));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("GMT"));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("Z"));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("+00"));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("-00:00"));

        DateTimeZone zoneHoursOnly = DateTimeZone.forID("+05");
        assertEquals("+05:00", zoneHoursOnly.getID());
        assertEquals(5 * DateTimeConstants.MILLIS_PER_HOUR, zoneHoursOnly.getOffset(0L));

        DateTimeZone zoneMinusHoursOnly = DateTimeZone.forID("-08");
        assertEquals("-08:00", zoneMinusHoursOnly.getID());
        assertEquals(-8 * DateTimeConstants.MILLIS_PER_HOUR, zoneMinusHoursOnly.getOffset(0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_invalidPrefix_throwsException() {
        DateTimeZone.forID("+invalid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_invalidOffsetFormat_throwsException() {
        DateTimeZone.forID("+25:00");
    }

    @Test
    public void testForOffsetMillis_validAndBoundaries() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));

        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000);
        assertEquals("+01:00", zone.getID());
        assertEquals(3600000, zone.getOffset(0L));

        DateTimeZone zoneMin = DateTimeZone.forOffsetMillis(-86399999);
        assertEquals(-86399999, zoneMin.getOffset(0L));

        DateTimeZone zoneMax = DateTimeZone.forOffsetMillis(86399999);
        assertEquals(86399999, zoneMax.getOffset(0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetMillis_tooLarge_throwsException() {
        DateTimeZone.forOffsetMillis(86400000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetMillis_tooSmall_throwsException() {
        DateTimeZone.forOffsetMillis(-86400000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHours_tooLarge_throwsException() {
        DateTimeZone.forOffsetHours(24);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHours_tooSmall_throwsException() {
        DateTimeZone.forOffsetHours(-24);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_outOfRangeHours_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(24, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_outOfRangeNegativeHours_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(-24, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_positiveHoursNegativeMinutes_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(5, -30);
    }

    @Test
    public void testForOffsetHoursMinutes_negativeZeroHours() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(0, 30);
        assertEquals("+00:30", zone.getID());
        assertEquals(30 * DateTimeConstants.MILLIS_PER_MINUTE, zone.getOffset(0L));
    }

    @Test
    public void testForTimeZone_variousIds() {
        DateTimeZone zoneGmtMinus = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT-08:00"));
        assertEquals("-08:00", zoneGmtMinus.getID());

        DateTimeZone zoneGmtOnly = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT"));
        assertSame(DateTimeZone.UTC, zoneGmtOnly);
    }

    @Test
    public void testSetAndGetProvider() {
        Provider customProvider = new UTCProvider();
        DateTimeZone.setProvider(customProvider);
        assertSame(customProvider, DateTimeZone.getProvider());

        DateTimeZone.setProvider(null);
        assertNotNull(DateTimeZone.getProvider());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProvider_emptyZoneIds_throwsException() {
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) {
                return null;
            }
            public Set<String> getAvailableIDs() {
                return Collections.emptySet();
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProvider_missingUTC_throwsException() {
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) {
                return null;
            }
            public Set<String> getAvailableIDs() {
                return Collections.singleton("America/New_York");
            }
        });
    }

    @Test
    public void testSetAndGetNameProvider() {
        NameProvider customNameProvider = new DefaultNameProvider();
        DateTimeZone.setNameProvider(customNameProvider);
        assertSame(customNameProvider, DateTimeZone.getNameProvider());

        DateTimeZone.setNameProvider(null);
        assertNotNull(DateTimeZone.getNameProvider());
    }

    @Test
    public void testTransitions_and_adjustOffset() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // 2007-03-11 02:00 EST -> EDT transition is at 2007-03-11 07:00:00 UTC = 1173596400000L
        long transition = 1173596400000L;

        long next = zone.nextTransition(transition - 10000L);
        assertEquals(transition, next);

        long prev = zone.previousTransition(transition + 10000L);
        assertEquals(transition, prev);

        assertEquals(0L, DateTimeZone.UTC.nextTransition(0L));
        assertEquals(0L, DateTimeZone.UTC.previousTransition(0L));

        // Adjust offset during DST overlap in Autumn (2007-11-04 01:30:00 EDT / EST)
        // Overlap local 01:30 is 1194154200000L (EDT, UTC-4 -> 05:30 UTC) or 1194157800000L (EST, UTC-5 -> 06:30 UTC)
        long instantBefore = 1194154200000L;
        long adjustedLater = zone.adjustOffset(instantBefore, false);
        long adjustedEarlier = zone.adjustOffset(adjustedLater, true);
        assertEquals(instantBefore, adjustedEarlier);
    }

    @Test
    public void testConvertLocalToUTC_threeArgs() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        long originalUTC = 1194154200000L;
        long localMillis = zone.convertUTCToLocal(originalUTC);
        long result = zone.convertLocalToUTC(localMillis, false, originalUTC);
        assertEquals(originalUTC, result);
    }

    @Test(expected = ArithmeticException.class)
    public void testConvertLocalToUTC_overflow_throwsArithmeticException() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(-2);
        zone.convertLocalToUTC(Long.MAX_VALUE, false);
    }

    @Test(expected = ArithmeticException.class)
    public void testConvertLocalToUTC_underflow_throwsArithmeticException() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        zone.convertLocalToUTC(Long.MIN_VALUE, false);
    }

    @Test
    public void testGetMillisKeepLocal_nullZone_usesDefault() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(3);
        long result = zone.getMillisKeepLocal(null, 1000L);
        long expected = zone.getMillisKeepLocal(DateTimeZone.getDefault(), 1000L);
        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsLocalDateTimeGap_null_throwsException() {
        DateTimeZone.UTC.isLocalDateTimeGap(null);
    }

    @Test
    public void testFixedOffsetZone_methods() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(5, 30);
        assertEquals(5 * 3600000 + 30 * 60000, zone.getStandardOffset(0L));
        assertEquals(0L, zone.nextTransition(1000L));
        assertEquals(1000L, zone.previousTransition(1000L));
    }
}