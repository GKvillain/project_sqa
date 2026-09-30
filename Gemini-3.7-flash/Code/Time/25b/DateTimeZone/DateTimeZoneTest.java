package org.joda.time;

import org.joda.time.tz.DefaultNameProvider;
import org.joda.time.tz.NameProvider;
import org.joda.time.tz.Provider;
import org.joda.time.tz.UTCProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class DateTimeZoneTest {

    private DateTimeZone originalDefault;
    private TimeZone originalJvmDefault;
    private Provider originalProvider;
    private NameProvider originalNameProvider;

    @Before
    public void setUp() {
        originalDefault = DateTimeZone.getDefault();
        originalJvmDefault = TimeZone.getDefault();
        originalProvider = DateTimeZone.getProvider();
        originalNameProvider = DateTimeZone.getNameProvider();
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(originalDefault);
        TimeZone.setDefault(originalJvmDefault);
        DateTimeZone.setProvider(originalProvider);
        DateTimeZone.setNameProvider(originalNameProvider);
    }

    // Tests getting zone with null ID returning default
    @Test
    public void testForID_nullId_returnsDefaultZone() {
        DateTimeZone zone = DateTimeZone.forID(null);
        assertEquals(originalDefault, zone);
    }

    // Tests getting UTC zone by ID
    @Test
    public void testForID_utcId_returnsUTC() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        assertSame(DateTimeZone.UTC, zone);
        assertEquals(0, zone.getOffset(0L));
    }

    // Tests parsing positive fixed offset ID
    @Test
    public void testForID_positiveOffset_returnsFixedOffsetZone() {
        DateTimeZone zone = DateTimeZone.forID("+02:00");
        assertEquals("+02:00", zone.getID());
        assertEquals(2 * 3600000, zone.getOffset(0L));
        assertTrue(zone.isFixed());
    }

    // Tests parsing negative fixed offset ID
    @Test
    public void testForID_negativeOffset_returnsFixedOffsetZone() {
        DateTimeZone zone = DateTimeZone.forID("-05:30");
        assertEquals("-05:30", zone.getID());
        assertEquals(-(5 * 3600000 + 30 * 60000), zone.getOffset(0L));
        assertTrue(zone.isFixed());
    }

    // Tests offset string resulting in zero offset returns UTC
    @Test
    public void testForID_zeroOffsetString_returnsUTC() {
        DateTimeZone zone = DateTimeZone.forID("+00:00");
        assertSame(DateTimeZone.UTC, zone);
    }

    // Tests invalid zone ID throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testForID_invalidId_throwsException() {
        DateTimeZone.forID("Invalid/NonExistentZoneId_12345");
    }

    // Tests forOffsetHours with valid positive value
    @Test
    public void testForOffsetHours_positiveHours_returnsCorrectZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(8);
        assertEquals("+08:00", zone.getID());
        assertEquals(8 * 3600000, zone.getOffset(0L));
    }

    // Tests forOffsetHoursMinutes with positive hours and minutes
    @Test
    public void testForOffsetHoursMinutes_positiveHoursAndMinutes_returnsCorrectZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(5, 45);
        assertEquals("+05:45", zone.getID());
        assertEquals(5 * 3600000 + 45 * 60000, zone.getOffset(0L));
    }

    // Tests forOffsetHoursMinutes with negative hours and positive minutes
    @Test
    public void testForOffsetHoursMinutes_negativeHoursAndPositiveMinutes_returnsCorrectZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-2, 30);
        assertEquals("-02:30", zone.getID());
        assertEquals(-(2 * 3600000 + 30 * 60000), zone.getOffset(0L));
    }

    // Tests forOffsetHoursMinutes with zeroes returns UTC
    @Test
    public void testForOffsetHoursMinutes_zeroHoursAndMinutes_returnsUTC() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(0, 0);
        assertSame(DateTimeZone.UTC, zone);
    }

    // Tests forOffsetHoursMinutes with invalid minutes throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_minutesOutOfRange_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(2, 60);
    }

    // Tests forOffsetMillis with valid milliseconds
    @Test
    public void testForOffsetMillis_positiveMillis_returnsCorrectZone() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000);
        assertEquals("+01:00", zone.getID());
        assertEquals(3600000, zone.getOffset(0L));
    }

    // Tests forTimeZone with null input returns default
    @Test
    public void testForTimeZone_nullTimeZone_returnsDefault() {
        DateTimeZone zone = DateTimeZone.forTimeZone(null);
        assertEquals(originalDefault, zone);
    }

    // Tests forTimeZone with custom JDK TimeZone
    @Test
    public void testForTimeZone_validJdkTimeZone_returnsMappedZone() {
        TimeZone jdkZone = TimeZone.getTimeZone("America/New_York");
        DateTimeZone zone = DateTimeZone.forTimeZone(jdkZone);
        assertEquals("America/New_York", zone.getID());
    }

    // Tests forTimeZone with old 3-letter alias conversion
    @Test
    public void testForTimeZone_convertedAlias_returnsCanonicalZone() {
        TimeZone jdkZone = TimeZone.getTimeZone("EST");
        DateTimeZone zone = DateTimeZone.forTimeZone(jdkZone);
        assertEquals("America/New_York", zone.getID());
    }

    // Tests setDefault and getDefault
    @Test
    public void testSetDefault_validZone_updatesDefaultZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(3);
        DateTimeZone.setDefault(zone);
        assertEquals(zone, DateTimeZone.getDefault());
    }

    // Tests setDefault with null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSetDefault_nullZone_throwsException() {
        DateTimeZone.setDefault(null);
    }

    // Tests available IDs not empty and contains UTC
    @Test
    public void testGetAvailableIDs_notEmpty_containsUTC() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertNotNull(ids);
        assertTrue(ids.contains("UTC"));
    }

    // Tests convertUTCToLocal calculation
    @Test
    public void testConvertUTCToLocal_fixedZone_returnsAdjustedMillis() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        long utcMillis = 10000L;
        long localMillis = zone.convertUTCToLocal(utcMillis);
        assertEquals(utcMillis + 2 * 3600000L, localMillis);
    }

    // Tests convertLocalToUTC calculation
    @Test
    public void testConvertLocalToUTC_fixedZone_returnsAdjustedMillis() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        long localMillis = 10000L + 2 * 3600000L;
        long utcMillis = zone.convertLocalToUTC(localMillis, false);
        assertEquals(10000L, utcMillis);
    }

    // Tests getOffsetFromLocal near DST transition
    @Test
    public void testGetOffsetFromLocal_nearDstBoundary_calculatesCorrectOffset() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // Winter: standard offset is -5 hours (-18000000 ms)
        long winterLocal = 0L; // 1970-01-01 local
        assertEquals(-18000000, zone.getOffsetFromLocal(winterLocal));
    }

    // Tests getMillisKeepLocal across different zones
    @Test
    public void testGetMillisKeepLocal_differentZones_preservesLocalTime() {
        DateTimeZone zone1 = DateTimeZone.forOffsetHours(1);
        DateTimeZone zone2 = DateTimeZone.forOffsetHours(4);
        long instant = 0L;
        long result = zone1.getMillisKeepLocal(zone2, instant);
        assertEquals(instant - 3 * 3600000L, result);
    }

    // Tests getOffset with ReadableInstant input
    @Test
    public void testGetOffset_readableInstant_returnsCorrectOffset() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(3);
        Instant instant = new Instant(0L);
        assertEquals(3 * 3600000, zone.getOffset(instant));
        assertEquals(zone.getOffset(DateTimeUtils.currentTimeMillis()), zone.getOffset((ReadableInstant) null));
    }

    // Tests getName and getShortName with locale
    @Test
    public void testGetNameAndShortName_fixedOffset_returnsFormattedName() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        assertNotNull(zone.getName(0L, Locale.ENGLISH));
        assertNotNull(zone.getShortName(0L, Locale.ENGLISH));
        assertNotNull(zone.getName(0L));
        assertNotNull(zone.getShortName(0L));
    }

    // Tests toTimeZone conversion and equals/hashCode/toString
    @Test
    public void testToTimeZone_equalsAndHashCode_validOutput() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        TimeZone tz = zone.toTimeZone();
        assertEquals("UTC", tz.getID());
        assertEquals("UTC", zone.toString());
        assertTrue(zone.equals(DateTimeZone.UTC));
        assertEquals(DateTimeZone.UTC.hashCode(), zone.hashCode());
        assertFalse(zone.equals(DateTimeZone.forOffsetHours(1)));
    }

    // --- Additional Coverage Tests ---

    @Test
    public void testForID_aliasesAndSpecialStrings() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("UT"));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("GMT"));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("Z"));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("-00:00"));

        DateTimeZone zonePlus = DateTimeZone.forID("+01");
        assertEquals("+01:00", zonePlus.getID());

        DateTimeZone zoneMinus = DateTimeZone.forID("-01");
        assertEquals("-01:00", zoneMinus.getID());

        DateTimeZone zoneSeconds = DateTimeZone.forID("+01:02:03");
        assertEquals("+01:02:03", zoneSeconds.getID());
        assertEquals((1 * 3600 + 2 * 60 + 3) * 1000, zoneSeconds.getOffset(0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_invalidFormat_throwsException() {
        DateTimeZone.forID("+25:00");
    }

    @Test
    public void testForOffsetHours_negativeAndBoundaryValues() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(-12);
        assertEquals("-12:00", zone.getID());
        assertEquals(-12 * 3600000, zone.getOffset(0L));

        DateTimeZone zoneZero = DateTimeZone.forOffsetHours(0);
        assertSame(DateTimeZone.UTC, zoneZero);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHours_tooHigh_throwsException() {
        DateTimeZone.forOffsetHours(24);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHours_tooLow_throwsException() {
        DateTimeZone.forOffsetHours(-24);
    }

    @Test
    public void testForOffsetHoursMinutes_minutesNegativeBranch() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(0, 45);
        assertEquals("+00:45", zone.getID());

        DateTimeZone zoneNegZeroHour = DateTimeZone.forOffsetHoursMinutes(0, -45);
        assertEquals("-00:45", zoneNegZeroHour.getID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_negativeMinutesWithPositiveHours_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(2, -15);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_minutesTooLow_throwsException() {
        DateTimeZone.forOffsetHoursMinutes(-2, -60);
    }

    @Test
    public void testForOffsetMillis_boundariesAndZero() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));
        DateTimeZone zone = DateTimeZone.forOffsetMillis(-3600000);
        assertEquals("-01:00", zone.getID());

        DateTimeZone millisZone = DateTimeZone.forOffsetMillis(1234);
        assertEquals("+00:00:01.234", millisZone.getID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetMillis_tooHigh_throwsException() {
        DateTimeZone.forOffsetMillis(86400000); // 24 hours in millis
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetMillis_tooLow_throwsException() {
        DateTimeZone.forOffsetMillis(-86400000);
    }

    @Test
    public void testForTimeZone_unrecognizedCustomTimeZone() {
        TimeZone customTz = new SimpleTimeZone(3600000, "NonStandardId_XYZ");
        DateTimeZone zone = DateTimeZone.forTimeZone(customTz);
        assertEquals("+01:00", zone.getID());

        TimeZone customUtcTz = new SimpleTimeZone(0, "NonStandardUtc_XYZ");
        DateTimeZone utcZone = DateTimeZone.forTimeZone(customUtcTz);
        assertSame(DateTimeZone.UTC, utcZone);
    }

    @Test
    public void testGetMillisKeepLocal_nullZone_usesDefault() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        long instant = 50000L;
        long expected = zone.getMillisKeepLocal(DateTimeZone.getDefault(), instant);
        long actual = zone.getMillisKeepLocal(null, instant);
        assertEquals(expected, actual);
    }

    @Test
    public void testConvertUTCToLocal_overflow_throwsArithmeticException() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        try {
            zone.convertUTCToLocal(Long.MAX_VALUE);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException ex) {
            // expected
        }
    }

    @Test
    public void testConvertLocalToUTC_strictAndNonStrict_gapAndOverlap() {
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        // Spring forward transition in Paris: 2023-03-26 02:00:00 -> 03:00:00 (+01:00 to +02:00)
        // Gap is between 02:00 and 02:59:59 local time
        long gapLocal = new DateTime(2023, 3, 26, 2, 30, 0, 0, DateTimeZone.UTC).getMillis();

        try {
            paris.convertLocalToUTC(gapLocal, true);
            fail("Expected IllegalInstantException on gap with strict=true");
        } catch (IllegalInstantException ex) {
            // expected
        }

        long nonStrictUtc = paris.convertLocalToUTC(gapLocal, false);
        assertTrue(nonStrictUtc > 0);

        // Fall back transition in Paris: 2023-10-29 03:00:00 -> 02:00:00
        long overlapLocal = new DateTime(2023, 10, 29, 2, 30, 0, 0, DateTimeZone.UTC).getMillis();
        long overlapUtc = paris.convertLocalToUTC(overlapLocal, true);
        assertTrue(overlapUtc > 0);

        // 3-argument convertLocalToUTC
        long convertedWithOriginal = paris.convertLocalToUTC(overlapLocal, false, overlapUtc);
        assertTrue(convertedWithOriginal > 0);
    }

    @Test
    public void testConvertLocalToUTC_overflow_throwsArithmeticException() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(-2);
        try {
            zone.convertLocalToUTC(Long.MAX_VALUE, false);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException ex) {
            // expected
        }
    }

    @Test
    public void testIsStandardOffset_andTransitions() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        long winterInstant = new DateTime(2023, 1, 1, 0, 0, DateTimeZone.UTC).getMillis();
        long summerInstant = new DateTime(2023, 7, 1, 0, 0, DateTimeZone.UTC).getMillis();

        assertTrue(zone.isStandardOffset(winterInstant));
        assertFalse(zone.isStandardOffset(summerInstant));

        long next = zone.nextTransition(winterInstant);
        assertTrue(next > winterInstant);

        long prev = zone.previousTransition(summerInstant);
        assertTrue(prev < summerInstant);

        DateTimeZone fixed = DateTimeZone.forOffsetHours(2);
        assertEquals(winterInstant, fixed.nextTransition(winterInstant));
        assertEquals(winterInstant, fixed.previousTransition(winterInstant));
        assertTrue(fixed.isStandardOffset(winterInstant));
        assertEquals(2 * 3600000, fixed.getStandardOffset(winterInstant));
    }

    @Test
    public void testIsLocalDateTimeGap_andAdjustOffset() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        LocalDateTime gapTime = new LocalDateTime(2023, 3, 26, 2, 30, 0, 0);
        assertTrue(zone.isLocalDateTimeGap(gapTime));

        LocalDateTime nonGapTime = new LocalDateTime(2023, 3, 26, 4, 30, 0, 0);
        assertFalse(zone.isLocalDateTimeGap(nonGapTime));

        // Adjust offset during DST overlap
        long overlapWinterLocal = new DateTime(2023, 10, 29, 2, 30, 0, 0, DateTimeZone.UTC).getMillis();
        long earlier = zone.adjustOffset(overlapWinterLocal, false);
        long later = zone.adjustOffset(overlapWinterLocal, true);
        assertTrue(earlier <= later);

        // adjustOffset on fixed zone
        DateTimeZone fixed = DateTimeZone.forOffsetHours(1);
        assertEquals(1000L, fixed.adjustOffset(1000L, true));
    }

    @Test
    public void testProviderAndNameProviderAccessors() {
        Provider customProvider = new UTCProvider();
        DateTimeZone.setProvider(customProvider);
        assertSame(customProvider, DateTimeZone.getProvider());

        NameProvider customNameProvider = new DefaultNameProvider();
        DateTimeZone.setNameProvider(customNameProvider);
        assertSame(customNameProvider, DateTimeZone.getNameProvider());

        DateTimeZone.setNameProvider(null);
        assertNotNull(DateTimeZone.getNameProvider());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProvider_emptyIds_throwsException() {
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) {
                return null;
            }
            public Set<String> getAvailableIDs() {
                return java.util.Collections.emptySet();
            }
        });
    }

    @Test
    public void testSerialization() throws Exception {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
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