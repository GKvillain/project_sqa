package org.joda.time.format;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.MutableDateTime;
import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.ISOChronology;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DateTimeFormatterTest {

    private DateTimeZone originalZone;
    private Locale originalLocale;

    @Before
    public void setUp() {
        originalZone = DateTimeZone.getDefault();
        originalLocale = Locale.getDefault();
        DateTimeZone.setDefault(DateTimeZone.UTC);
        Locale.setDefault(Locale.UK);
    }

    @org.junit.After
    public void tearDown() {
        DateTimeZone.setDefault(originalZone);
        Locale.setDefault(originalLocale);
    }

    // Tests defect 16b: parseInto should preserve the year of the input instant when parsing month/day
    @Test
    public void testParseInto_monthOnly_preservesInstantYear() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("M").withZoneUTC();
        MutableDateTime mdt = new MutableDateTime(2004, 12, 25, 10, 20, 30, 40, DateTimeZone.UTC);

        int result = formatter.parseInto(mdt, "5", 0);

        assertEquals(1, result);
        assertEquals(2004, mdt.getYear());
        assertEquals(5, mdt.getMonthOfYear());
        assertEquals(25, mdt.getDayOfMonth());
    }

    // Tests defect 16b: parseInto month and day retains year for leap year instant
    @Test
    public void testParseInto_leapYearFeb29_preservesLeapYear() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MM dd").withZoneUTC();
        MutableDateTime mdt = new MutableDateTime(2004, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);

        int result = formatter.parseInto(mdt, "02 29", 0);

        assertEquals(5, result);
        assertEquals(2004, mdt.getYear());
        assertEquals(2, mdt.getMonthOfYear());
        assertEquals(29, mdt.getDayOfMonth());
    }

    // Tests withLocale and getLocale
    @Test
    public void testWithLocale_differentAndSameLocale_returnsExpected() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MMMM dd yyyy");
        assertNull(formatter.getLocale());

        DateTimeFormatter frenchFormatter = formatter.withLocale(Locale.FRENCH);
        assertEquals(Locale.FRENCH, frenchFormatter.getLocale());
        assertSame(frenchFormatter, frenchFormatter.withLocale(Locale.FRENCH));

        DateTime dt = new DateTime(2020, 5, 1, 12, 0, DateTimeZone.UTC);
        assertEquals("mai 01 2020", frenchFormatter.print(dt));
    }

    // Tests withZone and withZoneUTC
    @Test
    public void testWithZone_andWithZoneUTC_appliesZoneCorrectly() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
        assertNull(formatter.getZone());

        DateTimeZone zonePlus2 = DateTimeZone.forOffsetHours(2);
        DateTimeFormatter zoneFormatter = formatter.withZone(zonePlus2);
        assertEquals(zonePlus2, zoneFormatter.getZone());
        assertSame(zoneFormatter, zoneFormatter.withZone(zonePlus2));

        DateTimeFormatter utcFormatter = zoneFormatter.withZoneUTC();
        assertEquals(DateTimeZone.UTC, utcFormatter.getZone());

        DateTime dt = new DateTime(2020, 1, 1, 10, 0, DateTimeZone.UTC);
        assertEquals("2020-01-01 12:00", zoneFormatter.print(dt));
        assertEquals("2020-01-01 10:00", utcFormatter.print(dt));
    }

    // Tests withChronology, getChronology, and deprecated getChronolgy
    @Test
    public void testWithChronology_setsAndRetrievesChronology() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        assertNull(formatter.getChronology());
        assertNull(formatter.getChronolgy());

        Chronology buddhist = BuddhistChronology.getInstanceUTC();
        DateTimeFormatter buddhistFormatter = formatter.withChronology(buddhist);

        assertSame(buddhist, buddhistFormatter.getChronology());
        assertSame(buddhist, buddhistFormatter.getChronolgy());
        assertSame(buddhistFormatter, buddhistFormatter.withChronology(buddhist));

        DateTime dt = new DateTime(2000, 1, 1, 0, 0, ISOChronology.getInstanceUTC());
        assertEquals("2543-01-01", buddhistFormatter.print(dt));
    }

    // Tests withOffsetParsed and isOffsetParsed
    @Test
    public void testWithOffsetParsed_parsesZoneOffset() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm Z");
        assertFalse(formatter.isOffsetParsed());

        DateTimeFormatter offsetFormatter = formatter.withOffsetParsed();
        assertTrue(offsetFormatter.isOffsetParsed());
        assertSame(offsetFormatter, offsetFormatter.withOffsetParsed());

        DateTime parsed = offsetFormatter.parseDateTime("2020-01-01 12:00 +0200");
        assertEquals(DateTimeZone.forOffsetHours(2), parsed.getZone());
        assertEquals(12, parsed.getHourOfDay());
    }

    // Tests withPivotYear and getPivotYear
    @Test
    public void testWithPivotYear_twoDigitYearParsing() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yy-MM-dd");
        assertNull(formatter.getPivotYear());

        DateTimeFormatter pivotFormatter = formatter.withPivotYear(1950);
        assertEquals(Integer.valueOf(1950), pivotFormatter.getPivotYear());
        assertSame(pivotFormatter, pivotFormatter.withPivotYear(Integer.valueOf(1950)));

        DateTime parsed50 = pivotFormatter.parseDateTime("50-01-01");
        assertEquals(1950, parsed50.getYear());

        DateTime parsed49 = pivotFormatter.parseDateTime("49-01-01");
        assertEquals(2049, parsed49.getYear());
    }

    // Tests withDefaultYear and getDefaultYear
    @Test
    public void testWithDefaultYear_setsYearWhenNoYearParsed() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MM-dd");
        assertEquals(2000, formatter.getDefaultYear());

        DateTimeFormatter customDefault = formatter.withDefaultYear(1996);
        assertEquals(1996, customDefault.getDefaultYear());

        DateTime parsed = customDefault.parseDateTime("02-29");
        assertEquals(1996, parsed.getYear());
        assertEquals(2, parsed.getMonthOfYear());
        assertEquals(29, parsed.getDayOfMonth());
    }

    // Tests printTo with StringBuffer, Writer, and Appendable for ReadableInstant and millis
    @Test
    public void testPrintTo_variousTargets_formatsCorrectly() throws IOException {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZoneUTC();
        DateTime dt = new DateTime(2021, 6, 15, 14, 30, 45, DateTimeZone.UTC);

        StringBuffer sbInstant = new StringBuffer();
        formatter.printTo(sbInstant, dt);
        assertEquals("2021-06-15 14:30:45", sbInstant.toString());

        StringWriter writerInstant = new StringWriter();
        formatter.printTo(writerInstant, dt);
        assertEquals("2021-06-15 14:30:45", writerInstant.toString());

        StringBuilder appendableInstant = new StringBuilder();
        formatter.printTo((Appendable) appendableInstant, dt);
        assertEquals("2021-06-15 14:30:45", appendableInstant.toString());

        StringBuffer sbMillis = new StringBuffer();
        formatter.printTo(sbMillis, dt.getMillis());
        assertEquals("2021-06-15 14:30:45", sbMillis.toString());

        StringWriter writerMillis = new StringWriter();
        formatter.printTo(writerMillis, dt.getMillis());
        assertEquals("2021-06-15 14:30:45", writerMillis.toString());

        StringBuilder appendableMillis = new StringBuilder();
        formatter.printTo((Appendable) appendableMillis, dt.getMillis());
        assertEquals("2021-06-15 14:30:45", appendableMillis.toString());
    }

    // Tests print and printTo for ReadablePartial
    @Test
    public void testPrint_readablePartial_formatsCorrectly() throws IOException {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate date = new LocalDate(2020, 8, 20);

        assertEquals("2020-08-20", formatter.print(date));

        StringBuffer sb = new StringBuffer();
        formatter.printTo(sb, date);
        assertEquals("2020-08-20", sb.toString());

        CharArrayWriter writer = new CharArrayWriter();
        formatter.printTo(writer, date);
        assertEquals("2020-08-20", writer.toString());

        StringBuilder appendable = new StringBuilder();
        formatter.printTo((Appendable) appendable, date);
        assertEquals("2020-08-20", appendable.toString());
    }

    // Tests printTo with null partial throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintTo_nullPartial_throwsException() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        formatter.printTo(new StringBuffer(), (org.joda.time.ReadablePartial) null);
    }

    // Tests parseLocalDate, parseLocalTime, parseLocalDateTime
    @Test
    public void testParseLocalTypes_success() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

        LocalDate localDate = formatter.parseLocalDate("2022-03-10 15:45:30");
        assertEquals(new LocalDate(2022, 3, 10), localDate);

        LocalTime localTime = formatter.parseLocalTime("2022-03-10 15:45:30");
        assertEquals(new LocalTime(15, 45, 30), localTime);

        LocalDateTime localDateTime = formatter.parseLocalDateTime("2022-03-10 15:45:30");
        assertEquals(new LocalDateTime(2022, 3, 10, 15, 45, 30), localDateTime);
    }

    // Tests parseMillis and parseMutableDateTime
    @Test
    public void testParseMillis_andParseMutableDateTime() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss").withZoneUTC();
        String text = "2021-11-05T08:30:00";

        DateTime dt = new DateTime(2021, 11, 5, 8, 30, 0, DateTimeZone.UTC);
        long millis = formatter.parseMillis(text);
        assertEquals(dt.getMillis(), millis);

        MutableDateTime mdt = formatter.parseMutableDateTime(text);
        assertEquals(dt.getMillis(), mdt.getMillis());
        assertEquals(DateTimeZone.UTC, mdt.getZone());
    }

    // Tests parseInto with null instant throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseInto_nullInstant_throwsException() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        formatter.parseInto(null, "2020-01-01", 0);
    }

    // Tests parseDateTime with invalid format throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseDateTime_invalidText_throwsException() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        formatter.parseDateTime("invalid-date");
    }

    // Tests formatter without printer throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testPrint_noPrinter_throwsException() {
        DateTimeFormatter formatter = new DateTimeFormatter(null, DateTimeFormat.forPattern("yyyy").getParser());
        assertFalse(formatter.isPrinter());
        assertTrue(formatter.isParser());
        assertNull(formatter.getPrinter());
        assertNotNull(formatter.getParser());
        formatter.print(new DateTime());
    }

    // Tests formatter without parser throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testParse_noParser_throwsException() {
        DateTimeFormatter formatter = new DateTimeFormatter(DateTimeFormat.forPattern("yyyy").getPrinter(), null);
        assertTrue(formatter.isPrinter());
        assertFalse(formatter.isParser());
        assertNotNull(formatter.getPrinter());
        assertNull(formatter.getParser());
        formatter.parseDateTime("2020");
    }
}