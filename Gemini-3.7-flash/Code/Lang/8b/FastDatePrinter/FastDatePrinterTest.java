package org.apache.commons.lang3.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.FieldPosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

public class FastDatePrinterTest {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final TimeZone GMT_PLUS_2 = TimeZone.getTimeZone("GMT+02:00");
    private static final TimeZone NEW_YORK = TimeZone.getTimeZone("America/New_York");
    private static final Locale US = Locale.US;

    private Calendar baseCalendar;
    private Date baseDate;

    @Before
    public void setUp() {
        baseCalendar = new GregorianCalendar(UTC, US);
        baseCalendar.clear();
        baseCalendar.set(2023, Calendar.OCTOBER, 27, 14, 30, 45);
        baseCalendar.set(Calendar.MILLISECOND, 123);
        baseDate = baseCalendar.getTime();
    }

    // Tests standard formatting with date, month, and year tokens
    @Test
    public void testFormat_dateTokens_returnsFormattedString() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", UTC, US);
        assertEquals("2023-10-27", printer.format(baseDate));
        assertEquals("2023-10-27", printer.format(baseCalendar));
        assertEquals("2023-10-27", printer.format(baseDate.getTime()));
    }

    // Tests two-digit year, single-digit month and day
    @Test
    public void testFormat_twoDigitYearAndUnpaddedFields_returnsFormattedString() {
        FastDatePrinter printer = new FastDatePrinter("yy/M/d", UTC, US);
        assertEquals("23/10/27", printer.format(baseDate));
    }

    // Tests month text variations (short and full names)
    @Test
    public void testFormat_monthTextVariations_returnsFormattedString() {
        FastDatePrinter shortMonthPrinter = new FastDatePrinter("MMM", UTC, US);
        assertEquals("Oct", shortMonthPrinter.format(baseDate));

        FastDatePrinter longMonthPrinter = new FastDatePrinter("MMMM", UTC, US);
        assertEquals("October", longMonthPrinter.format(baseDate));
    }

    // Tests time tokens: 12-hour, 24-hour, minutes, seconds, milliseconds, and AM/PM
    @Test
    public void testFormat_timeTokens_returnsFormattedString() {
        FastDatePrinter printer = new FastDatePrinter("hh:mm:ss.SSS a (H, k, K)", UTC, US);
        assertEquals("02:30:45.123 PM (14, 14, 2)", printer.format(baseDate));
    }

    // Tests boundary conditions for hour fields (midnight and noon)
    @Test
    public void testFormat_midnightAndNoonHours_formatsCorrectly() {
        Calendar cal = new GregorianCalendar(UTC, US);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0); // Midnight

        FastDatePrinter printer = new FastDatePrinter("HH:hh:kk:KK", UTC, US);
        assertEquals("00:12:24:00", printer.format(cal));

        cal.set(Calendar.HOUR_OF_DAY, 12); // Noon
        assertEquals("12:12:12:00", printer.format(cal));
    }

    // Tests day-of-week, day-of-year, week-of-year, and era tokens
    @Test
    public void testFormat_miscellaneousTokens_returnsFormattedString() {
        FastDatePrinter printer = new FastDatePrinter("G E EEEE D F w W", UTC, US);
        String formatted = printer.format(baseCalendar);
        assertTrue(formatted.startsWith("AD Fri Friday"));
    }

    // Tests timezone name formatting (short and long)
    @Test
    public void testFormat_timeZoneNameTokens_returnsFormattedString() {
        FastDatePrinter shortTzPrinter = new FastDatePrinter("z", UTC, US);
        assertEquals("UTC", shortTzPrinter.format(baseCalendar));

        FastDatePrinter longTzPrinter = new FastDatePrinter("zzzz", UTC, US);
        assertEquals("Coordinated Universal Time", longTzPrinter.format(baseCalendar));
    }

    // Tests RFC822 and ISO8601 timezone offset formatting
    @Test
    public void testFormat_timeZoneOffsetTokens_returnsFormattedString() {
        FastDatePrinter rfc822Printer = new FastDatePrinter("Z", GMT_PLUS_2, US);
        assertEquals("+0200", rfc822Printer.format(baseDate));

        FastDatePrinter iso8601Printer = new FastDatePrinter("ZZ", GMT_PLUS_2, US);
        assertEquals("+02:00", iso8601Printer.format(baseDate));
    }

    // Tests negative timezone offsets formatting
    @Test
    public void testFormat_negativeTimeZoneOffset_returnsNegativeOffset() {
        FastDatePrinter rfc822Printer = new FastDatePrinter("Z", NEW_YORK, US);
        Calendar cal = new GregorianCalendar(NEW_YORK, US);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1, 12, 0, 0); // Standard time EST (-0500)
        assertEquals("-0500", rfc822Printer.format(cal));

        FastDatePrinter iso8601Printer = new FastDatePrinter("ZZ", NEW_YORK, US);
        assertEquals("-05:00", iso8601Printer.format(cal));
    }

    // Tests literal strings and single quote escaping
    @Test
    public void testFormat_quotedLiterals_preservesLiteralsAndQuotes() {
        FastDatePrinter printer = new FastDatePrinter("'Date: 'yyyy-MM-dd' ''T'''", UTC, US);
        assertEquals("Date: 2023-10-27 'T'", printer.format(baseDate));
    }

    // Tests number padding rule with large padding
    @Test
    public void testFormat_paddedNumberField_padsWithLeadingZeros() {
        FastDatePrinter printer = new FastDatePrinter("yyyyy-ddddd", UTC, US);
        assertEquals("02023-00027", printer.format(baseDate));
    }

    // Tests format(Object, StringBuffer, FieldPosition) with Date, Calendar, and Long
    @Test
    public void testFormat_formatObjectTypes_appendsToBuffer() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", UTC, US);
        StringBuffer buf = new StringBuffer();
        
        printer.format((Object) baseDate, buf, new FieldPosition(0));
        assertEquals("2023-10-27", buf.toString());

        buf.setLength(0);
        printer.format((Object) baseCalendar, buf, new FieldPosition(0));
        assertEquals("2023-10-27", buf.toString());

        buf.setLength(0);
        printer.format((Object) Long.valueOf(baseDate.getTime()), buf, new FieldPosition(0));
        assertEquals("2023-10-27", buf.toString());
    }

    // Tests format(Object) with unsupported type throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFormat_unsupportedObjectType_throwsIllegalArgumentException() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", UTC, US);
        printer.format("2023-10-27", new StringBuffer(), new FieldPosition(0));
    }

    // Tests format(Object) with null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFormat_nullObject_throwsIllegalArgumentException() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", UTC, US);
        printer.format((Object) null, new StringBuffer(), new FieldPosition(0));
    }

    // Tests invalid pattern token throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParsePattern_illegalPatternCharacter_throwsIllegalArgumentException() {
        new FastDatePrinter("yyyy-MM-dd X", UTC, US);
    }

    // Tests getters: getPattern, getTimeZone, getLocale, and getMaxLengthEstimate
    @Test
    public void testAccessors_returnConfiguredValues() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", UTC, US);
        assertEquals("yyyy-MM-dd", printer.getPattern());
        assertEquals(UTC, printer.getTimeZone());
        assertEquals(US, printer.getLocale());
        assertTrue(printer.getMaxLengthEstimate() > 0);
    }

    // Tests equals, hashCode, and toString contracts
    @Test
    public void testEqualsAndHashCodeAndToString() {
        FastDatePrinter printer1 = new FastDatePrinter("yyyy-MM-dd", UTC, US);
        FastDatePrinter printer2 = new FastDatePrinter("yyyy-MM-dd", UTC, US);
        FastDatePrinter printerDiffPattern = new FastDatePrinter("yyyy/MM/dd", UTC, US);
        FastDatePrinter printerDiffTz = new FastDatePrinter("yyyy-MM-dd", NEW_YORK, US);
        FastDatePrinter printerDiffLocale = new FastDatePrinter("yyyy-MM-dd", UTC, Locale.GERMANY);

        assertTrue(printer1.equals(printer1));
        assertTrue(printer1.equals(printer2));
        assertEquals(printer1.hashCode(), printer2.hashCode());

        assertFalse(printer1.equals(null));
        assertFalse(printer1.equals("different type"));
        assertFalse(printer1.equals(printerDiffPattern));
        assertFalse(printer1.equals(printerDiffTz));
        assertFalse(printer1.equals(printerDiffLocale));

        String toString = printer1.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("yyyy-MM-dd"));
        assertTrue(toString.contains("UTC"));
    }

    // Tests serialization and deserialization retains formatting behavior
    @Test
    public void testSerialization_roundTrip_producesIdenticalFormatting() throws Exception {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd HH:mm:ss Z", UTC, US);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(printer);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FastDatePrinter deserialized = (FastDatePrinter) ois.readObject();
        ois.close();

        assertEquals(printer, deserialized);
        assertEquals(printer.format(baseDate), deserialized.format(baseDate));
    }
}