package org.joda.time.format;

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
import org.joda.time.ReadablePartial;
import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.ISOChronology;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DateTimeFormatterTest {

    private DateTimeZone originalZone;
    private DateTimeFormatter formatter;
    private DateTimeFormatter printerOnly;
    private DateTimeFormatter parserOnly;

    @Before
    public void setUp() {
        originalZone = DateTimeZone.getDefault();
        DateTimeZone.setDefault(DateTimeZone.UTC);
        formatter = ISODateTimeFormat.dateTime();
        printerOnly = new DateTimeFormatter(formatter.getPrinter(), null);
        parserOnly = new DateTimeFormatter(null, formatter.getParser());
    }

    @org.junit.After
    public void tearDown() {
        DateTimeZone.setDefault(originalZone);
    }

    // Tests getters for printer and parser capabilities
    @Test
    public void testIsPrinterAndIsParser_normalState_returnsCorrectBooleans() {
        assertTrue(formatter.isPrinter());
        assertTrue(formatter.isParser());
        assertNotNull(formatter.getPrinter());
        assertNotNull(formatter.getParser());

        assertTrue(printerOnly.isPrinter());
        assertFalse(printerOnly.isParser());
        assertNotNull(printerOnly.getPrinter());
        assertNull(printerOnly.getParser());

        assertFalse(parserOnly.isPrinter());
        assertTrue(parserOnly.isParser());
        assertNull(parserOnly.getPrinter());
        assertNotNull(parserOnly.getParser());
    }

    // Tests withLocale modifier and identity when locale is unchanged
    @Test
    public void testWithLocale_validAndSameLocale_returnsExpectedInstances() {
        assertNull(formatter.getLocale());
        DateTimeFormatter frenchFormatter = formatter.withLocale(Locale.FRENCH);
        assertEquals(Locale.FRENCH, frenchFormatter.getLocale());
        assertSame(frenchFormatter, frenchFormatter.withLocale(Locale.FRENCH));
        DateTimeFormatter defaultLocaleFormatter = frenchFormatter.withLocale(null);
        assertNull(defaultLocaleFormatter.getLocale());
    }

    // Tests withZone and withZoneUTC modifiers and identity behavior
    @Test
    public void testWithZone_variousZones_returnsExpectedInstances() {
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        DateTimeFormatter parisFormatter = formatter.withZone(paris);
        assertEquals(paris, parisFormatter.getZone());
        assertSame(parisFormatter, parisFormatter.withZone(paris));

        DateTimeFormatter utcFormatter = parisFormatter.withZoneUTC();
        assertEquals(DateTimeZone.UTC, utcFormatter.getZone());
    }

    // Tests withChronology modifier and deprecated getChronolgy method
    @Test
    public void testWithChronology_validChronology_returnsExpectedInstance() {
        Chronology buddhist = BuddhistChronology.getInstanceUTC();
        DateTimeFormatter buddhistFormatter = formatter.withChronology(buddhist);
        assertEquals(buddhist, buddhistFormatter.getChronology());
        assertEquals(buddhist, buddhistFormatter.getChronolgy());
        assertSame(buddhistFormatter, buddhistFormatter.withChronology(buddhist));
    }

    // Tests withOffsetParsed flag behavior
    @Test
    public void testWithOffsetParsed_normalUsage_returnsExpectedState() {
        assertFalse(formatter.isOffsetParsed());
        DateTimeFormatter offsetParsedFormatter = formatter.withOffsetParsed();
        assertTrue(offsetParsedFormatter.isOffsetParsed());
        assertSame(offsetParsedFormatter, offsetParsedFormatter.withOffsetParsed());

        DateTimeFormatter zoneOverrideFormatter = offsetParsedFormatter.withZone(DateTimeZone.UTC);
        assertFalse(zoneOverrideFormatter.isOffsetParsed());
    }

    // Tests withPivotYear modifier and identity checks
    @Test
    public void testWithPivotYear_integerAndPrimitive_returnsExpectedInstances() {
        assertNull(formatter.getPivotYear());
        DateTimeFormatter pivotFormatter = formatter.withPivotYear(2050);
        assertEquals(Integer.valueOf(2050), pivotFormatter.getPivotYear());
        assertSame(pivotFormatter, pivotFormatter.withPivotYear(2050));
        assertSame(pivotFormatter, pivotFormatter.withPivotYear(Integer.valueOf(2050)));
    }

    // Tests withDefaultYear modifier
    @Test
    public void testWithDefaultYear_customYear_returnsExpectedDefaultYear() {
        assertEquals(2000, formatter.getDefaultYear());
        DateTimeFormatter customYearFormatter = formatter.withDefaultYear(1996);
        assertEquals(1996, customYearFormatter.getDefaultYear());
    }

    // Tests printing ReadableInstant across print, printTo(StringBuffer), and printTo(Writer)
    @Test
    public void testPrint_readableInstant_formatsCorrectly() throws IOException {
        DateTime dt = new DateTime(2020, 5, 12, 10, 30, 0, 0, DateTimeZone.UTC);
        String expected = "2020-05-12T10:30:00.000Z";

        assertEquals(expected, formatter.print(dt));

        StringBuffer buf = new StringBuffer();
        formatter.printTo(buf, dt);
        assertEquals(expected, buf.toString());

        StringWriter writer = new StringWriter();
        formatter.printTo(writer, dt);
        assertEquals(expected, writer.toString());

        StringBuilder builder = new StringBuilder();
        formatter.printTo((Appendable) builder, dt);
        assertEquals(expected, builder.toString());
    }

    // Tests printing millis instant across print, printTo(StringBuffer), and printTo(Writer)
    @Test
    public void testPrint_millisInstant_formatsCorrectly() throws IOException {
        DateTime dt = new DateTime(2020, 5, 12, 10, 30, 0, 0, DateTimeZone.UTC);
        long millis = dt.getMillis();
        String expected = "2020-05-12T10:30:00.000Z";

        assertEquals(expected, formatter.print(millis));

        StringBuffer buf = new StringBuffer();
        formatter.printTo(buf, millis);
        assertEquals(expected, buf.toString());

        StringWriter writer = new StringWriter();
        formatter.printTo(writer, millis);
        assertEquals(expected, writer.toString());

        StringBuilder builder = new StringBuilder();
        formatter.printTo((Appendable) builder, millis);
        assertEquals(expected, builder.toString());
    }

    // Tests printing ReadablePartial across print, printTo(StringBuffer), and printTo(Writer)
    @Test
    public void testPrint_readablePartial_formatsCorrectly() throws IOException {
        DateTimeFormatter dateFormatter = ISODateTimeFormat.date();
        LocalDate date = new LocalDate(2020, 5, 12);
        String expected = "2020-05-12";

        assertEquals(expected, dateFormatter.print(date));

        StringBuffer buf = new StringBuffer();
        dateFormatter.printTo(buf, date);
        assertEquals(expected, buf.toString());

        StringWriter writer = new StringWriter();
        dateFormatter.printTo(writer, date);
        assertEquals(expected, writer.toString());

        StringBuilder builder = new StringBuilder();
        dateFormatter.printTo((Appendable) builder, date);
        assertEquals(expected, builder.toString());
    }

    // Tests null ReadablePartial throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintTo_nullPartial_throwsIllegalArgumentException() {
        formatter.printTo(new StringBuffer(), (ReadablePartial) null);
    }

    // Tests printing unsupported operation exception when printer is null
    @Test(expected = UnsupportedOperationException.class)
    public void testPrint_unsupportedPrinter_throwsUnsupportedOperationException() {
        parserOnly.print(0L);
    }

    // Tests parseDateTime, parseMutableDateTime, and parseMillis
    @Test
    public void testParse_dateString_parsedCorrectly() {
        String text = "2020-05-12T10:30:00.000Z";
        DateTime expectedDt = new DateTime(2020, 5, 12, 10, 30, 0, 0, DateTimeZone.UTC);

        DateTime parsedDt = formatter.parseDateTime(text);
        assertEquals(expectedDt, parsedDt);

        MutableDateTime parsedMdt = formatter.parseMutableDateTime(text);
        assertEquals(expectedDt.getMillis(), parsedMdt.getMillis());

        long parsedMillis = formatter.parseMillis(text);
        assertEquals(expectedDt.getMillis(), parsedMillis);
    }

    // Tests parseLocalDate, parseLocalTime, and parseLocalDateTime
    @Test
    public void testParseLocal_localDateAndDateTime_parsedCorrectly() {
        String dateText = "2020-05-12";
        LocalDate date = ISODateTimeFormat.date().parseLocalDate(dateText);
        assertEquals(new LocalDate(2020, 5, 12), date);

        String timeText = "10:30:00";
        LocalTime time = ISODateTimeFormat.hourMinuteSecond().parseLocalTime(timeText);
        assertEquals(new LocalTime(10, 30, 0), time);

        String dateTimeText = "2020-05-12T10:30:00";
        LocalDateTime dateTime = ISODateTimeFormat.dateHourMinuteSecond().parseLocalDateTime(dateTimeText);
        assertEquals(new LocalDateTime(2020, 5, 12, 10, 30, 0), dateTime);
    }

    // Tests parseInto with normal parse and position advance
    @Test
    public void testParseInto_validText_modifiesInstantAndAdvancesPosition() {
        MutableDateTime mdt = new MutableDateTime(0L, DateTimeZone.UTC);
        String text = "2020-05-12T10:30:00.000Z extra";
        int newPos = formatter.parseInto(mdt, text, 0);

        assertEquals(24, newPos);
        DateTime expected = new DateTime(2020, 5, 12, 10, 30, 0, 0, DateTimeZone.UTC);
        assertEquals(expected.getMillis(), mdt.getMillis());
    }

    // Tests parseInto with leap year day/month parsing
    @Test
    public void testParseInto_monthDayOnLeapYearInstant_correctlySetsDate() {
        DateTimeFormatter dayMonthFormatter = DateTimeFormat.forPattern("MM-dd");
        MutableDateTime mdt = new MutableDateTime(2004, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        int newPos = dayMonthFormatter.parseInto(mdt, "02-29", 0);

        assertEquals(5, newPos);
        assertEquals(2, mdt.getMonthOfYear());
        assertEquals(29, mdt.getDayOfMonth());
        assertEquals(2004, mdt.getYear());
    }

    // Tests parseInto with null instant argument
    @Test(expected = IllegalArgumentException.class)
    public void testParseInto_nullInstant_throwsIllegalArgumentException() {
        formatter.parseInto(null, "2020-05-12T10:30:00.000Z", 0);
    }

    // Tests parse invalid format throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParseDateTime_invalidFormat_throwsIllegalArgumentException() {
        formatter.parseDateTime("invalid-date-string");
    }

    // Tests parsing unsupported operation exception when parser is null
    @Test(expected = UnsupportedOperationException.class)
    public void testParse_unsupportedParser_throwsUnsupportedOperationException() {
        printerOnly.parseDateTime("2020-05-12T10:30:00.000Z");
    }
}