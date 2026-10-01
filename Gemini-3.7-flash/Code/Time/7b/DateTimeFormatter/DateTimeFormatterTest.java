package org.joda.time.format;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
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

    // Tests parseInto with leap year on MutableDateTime
    @Test
    public void testParseInto_leapDayWithLeapYearMutableDateTime_updatesSuccessfully() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MM dd");
        MutableDateTime mdt = new MutableDateTime(2004, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        int result = formatter.parseInto(mdt, "02 29", 0);
        assertEquals(5, result);
        assertEquals(2004, mdt.getYear());
        assertEquals(2, mdt.getMonthOfYear());
        assertEquals(29, mdt.getDayOfMonth());
    }

    // Tests parseInto with null instant throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testParseInto_nullInstant_throwsException() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        formatter.parseInto(null, "2020-01-01", 0);
    }

    // Tests parseInto invalid text returns negative position
    @Test
    public void testParseInto_invalidText_returnsNegativePosition() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        MutableDateTime mdt = new MutableDateTime(2000, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        int result = formatter.parseInto(mdt, "invalid-date", 0);
        assertTrue(result < 0);
    }

    // Tests parseInto with offset parsed and zone override
    @Test
    public void testParseInto_withOffsetParsed_updatesZone() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd Z").withOffsetParsed();
        MutableDateTime mdt = new MutableDateTime(2000, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        int result = formatter.parseInto(mdt, "2010-06-15 +0200", 0);
        assertEquals(16, result);
        assertEquals(DateTimeZone.forOffsetHours(2), mdt.getZone());
    }

    // Tests parseDateTime, parseMutableDateTime, parseMillis
    @Test
    public void testParseDateTime_validString_returnsExpectedDateTime() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime dt = formatter.parseDateTime("2021-12-25 10:30:00");
        assertEquals(2021, dt.getYear());
        assertEquals(12, dt.getMonthOfYear());
        assertEquals(25, dt.getDayOfMonth());
        assertEquals(10, dt.getHourOfDay());
        assertEquals(30, dt.getMinuteOfHour());

        MutableDateTime mdt = formatter.parseMutableDateTime("2021-12-25 10:30:00");
        assertEquals(dt.getMillis(), mdt.getMillis());

        long millis = formatter.parseMillis("2021-12-25 10:30:00");
        assertEquals(dt.getMillis(), millis);
    }

    // Tests parseLocalDate, parseLocalTime, parseLocalDateTime
    @Test
    public void testParseLocalTypes_validString_returnsCorrectValues() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        LocalDate date = formatter.parseLocalDate("2020-05-10 14:20:00");
        assertEquals(new LocalDate(2020, 5, 10), date);

        LocalTime time = formatter.parseLocalTime("2020-05-10 14:20:00");
        assertEquals(new LocalTime(14, 20, 0), time);

        LocalDateTime dateTime = formatter.parseLocalDateTime("2020-05-10 14:20:00");
        assertEquals(new LocalDateTime(2020, 5, 10, 14, 20, 0), dateTime);
    }

    // Tests parseDateTime with invalid string throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testParseDateTime_invalidString_throwsException() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        formatter.parseDateTime("invalid");
    }

    // Tests print methods with ReadableInstant
    @Test
    public void testPrint_readableInstant_returnsCorrectString() throws IOException {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm");
        DateTime dt = new DateTime(2022, 5, 4, 15, 30, DateTimeZone.UTC);

        assertEquals("2022/05/04 15:30", formatter.print(dt));

        StringBuffer buf = new StringBuffer();
        formatter.printTo(buf, dt);
        assertEquals("2022/05/04 15:30", buf.toString());

        StringWriter writer = new StringWriter();
        formatter.printTo(writer, dt);
        assertEquals("2022/05/04 15:30", writer.toString());

        StringBuilder sb = new StringBuilder();
        formatter.printTo(sb, dt);
        assertEquals("2022/05/04 15:30", sb.toString());
    }

    // Tests print methods with millis
    @Test
    public void testPrint_longMillis_returnsCorrectString() throws IOException {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC();
        long millis = new DateTime(2020, 1, 1, 0, 0, DateTimeZone.UTC).getMillis();

        assertEquals("2020-01-01", formatter.print(millis));

        StringBuffer buf = new StringBuffer();
        formatter.printTo(buf, millis);
        assertEquals("2020-01-01", buf.toString());

        StringWriter writer = new StringWriter();
        formatter.printTo(writer, millis);
        assertEquals("2020-01-01", writer.toString());

        StringBuilder sb = new StringBuilder();
        formatter.printTo(sb, millis);
        assertEquals("2020-01-01", sb.toString());
    }

    // Tests print methods with ReadablePartial
    @Test
    public void testPrint_readablePartial_returnsCorrectString() throws IOException {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate localDate = new LocalDate(2023, 7, 19);

        assertEquals("2023-07-19", formatter.print(localDate));

        StringBuffer buf = new StringBuffer();
        formatter.printTo(buf, localDate);
        assertEquals("2023-07-19", buf.toString());

        StringWriter writer = new StringWriter();
        formatter.printTo(writer, localDate);
        assertEquals("2023-07-19", writer.toString());

        StringBuilder sb = new StringBuilder();
        formatter.printTo(sb, localDate);
        assertEquals("2023-07-19", sb.toString());
    }

    // Tests printTo with null partial throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testPrintTo_nullPartial_throwsException() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        formatter.printTo(new StringBuffer(), (org.joda.time.ReadablePartial) null);
    }

    // Tests withLocale modifier and getLocale
    @Test
    public void testWithLocale_differentAndSameLocale_returnsExpected() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MMMM").withZoneUTC();
        assertNull(formatter.getLocale());

        DateTimeFormatter frenchFormatter = formatter.withLocale(Locale.FRENCH);
        assertEquals(Locale.FRENCH, frenchFormatter.getLocale());
        assertSame(frenchFormatter, frenchFormatter.withLocale(Locale.FRENCH));

        DateTime dt = new DateTime(2020, 1, 1, 0, 0, DateTimeZone.UTC);
        assertEquals("janvier", frenchFormatter.print(dt));
    }

    // Tests withZone and withZoneUTC modifiers
    @Test
    public void testWithZone_differentAndSameZone_returnsExpected() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
        assertNull(formatter.getZone());

        DateTimeZone zoneParis = DateTimeZone.forID("Europe/Paris");
        DateTimeFormatter zoneFormatter = formatter.withZone(zoneParis);
        assertEquals(zoneParis, zoneFormatter.getZone());
        assertSame(zoneFormatter, zoneFormatter.withZone(zoneParis));

        DateTimeFormatter utcFormatter = zoneFormatter.withZoneUTC();
        assertEquals(DateTimeZone.UTC, utcFormatter.getZone());
    }

    // Tests withChronology and getChronology
    @Test
    public void testWithChronology_differentAndSameChrono_returnsExpected() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        assertNull(formatter.getChronology());
        assertNull(formatter.getChronolgy());

        Chronology chrono = BuddhistChronology.getInstanceUTC();
        DateTimeFormatter chronoFormatter = formatter.withChronology(chrono);
        assertEquals(chrono, chronoFormatter.getChronology());
        assertEquals(chrono, chronoFormatter.getChronolgy());
        assertSame(chronoFormatter, chronoFormatter.withChronology(chrono));
    }

    // Tests withOffsetParsed and isOffsetParsed
    @Test
    public void testWithOffsetParsed_returnsCorrectInstance() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd Z");
        assertFalse(formatter.isOffsetParsed());

        DateTimeFormatter offsetFormatter = formatter.withOffsetParsed();
        assertTrue(offsetFormatter.isOffsetParsed());
        assertNull(offsetFormatter.getZone());
        assertSame(offsetFormatter, offsetFormatter.withOffsetParsed());

        DateTime parsed = offsetFormatter.parseDateTime("2020-01-01 +0300");
        assertEquals(DateTimeZone.forOffsetHours(3), parsed.getZone());
    }

    // Tests withPivotYear and getPivotYear
    @Test
    public void testWithPivotYear_differentAndSamePivotYear_returnsExpected() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yy-MM-dd");
        assertNull(formatter.getPivotYear());

        DateTimeFormatter pivotFormatter = formatter.withPivotYear(1950);
        assertEquals(Integer.valueOf(1950), pivotFormatter.getPivotYear());
        assertSame(pivotFormatter, pivotFormatter.withPivotYear(1950));
        assertSame(pivotFormatter, pivotFormatter.withPivotYear(Integer.valueOf(1950)));

        DateTime dt = pivotFormatter.parseDateTime("20-01-01");
        assertEquals(1920, dt.getYear());
    }

    // Tests withDefaultYear and getDefaultYear
    @Test
    public void testWithDefaultYear_returnsExpected() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MM-dd");
        assertEquals(2000, formatter.getDefaultYear());

        DateTimeFormatter customDefault = formatter.withDefaultYear(1996);
        assertEquals(1996, customDefault.getDefaultYear());

        DateTime dt = customDefault.parseDateTime("02-29");
        assertEquals(1996, dt.getYear());
        assertEquals(2, dt.getMonthOfYear());
        assertEquals(29, dt.getDayOfMonth());
    }

    // Tests isPrinter and isParser with printer-only or parser-only formatters
    @Test
    public void testIsPrinterAndIsParser_printerOrParserNull_returnsCorrectFlags() {
        DateTimePrinter printer = DateTimeFormat.forPattern("yyyy").getPrinter();
        DateTimeParser parser = DateTimeFormat.forPattern("yyyy").getParser();

        DateTimeFormatter printerOnly = new DateTimeFormatter(printer, null);
        assertTrue(printerOnly.isPrinter());
        assertFalse(printerOnly.isParser());
        assertNotNull(printerOnly.getPrinter());
        assertNull(printerOnly.getParser());

        DateTimeFormatter parserOnly = new DateTimeFormatter(null, parser);
        assertFalse(parserOnly.isPrinter());
        assertTrue(parserOnly.isParser());
        assertNull(parserOnly.getPrinter());
        assertNotNull(parserOnly.getParser());
    }

    // Tests printer unsupported throws exception
    @Test(expected = UnsupportedOperationException.class)
    public void testPrint_printerNull_throwsUnsupportedOperationException() {
        DateTimeParser parser = DateTimeFormat.forPattern("yyyy").getParser();
        DateTimeFormatter parserOnly = new DateTimeFormatter(null, parser);
        parserOnly.print(new DateTime());
    }

    // Tests parser unsupported throws exception
    @Test(expected = UnsupportedOperationException.class)
    public void testParseDateTime_parserNull_throwsUnsupportedOperationException() {
        DateTimePrinter printer = DateTimeFormat.forPattern("yyyy").getPrinter();
        DateTimeFormatter printerOnly = new DateTimeFormatter(printer, null);
        printerOnly.parseDateTime("2020");
    }
}