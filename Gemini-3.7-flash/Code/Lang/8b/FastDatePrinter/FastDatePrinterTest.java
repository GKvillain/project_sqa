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
    private static final TimeZone NEW_YORK = TimeZone.getTimeZone("America/New_York");
    private static final Locale US = Locale.US;

    private Calendar cal;
    private Date date;

    @Before
    public void setUp() {
        cal = new GregorianCalendar(UTC, US);
        cal.clear();
        cal.set(2023, Calendar.JULY, 4, 16, 30, 45);
        cal.set(Calendar.MILLISECOND, 123);
        date = cal.getTime();
    }

    // Tests standard date and time pattern formatting with Date and Calendar
    @Test
    public void testFormat_standardDateTime_formatsCorrectly() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd HH:mm:ss.SSS", UTC, US);
        String expected = "2023-07-04 16:30:45.123";

        assertEquals(expected, printer.format(date));
        assertEquals(expected, printer.format(cal));
        assertEquals(expected, printer.format(date.getTime()));

        StringBuffer buf = new StringBuffer("Result: ");
        printer.format(date, buf);
        assertEquals("Result: " + expected, buf.toString());
    }

    // Tests format using Object dispatch with Date, Calendar, Long, and invalid type
    @Test
    public void testFormat_objectDispatch_handlesSupportedTypes() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", UTC, US);
        FieldPosition pos = new FieldPosition(0);

        StringBuffer buf1 = new StringBuffer();
        printer.format((Object) date, buf1, pos);
        assertEquals("2023-07-04", buf1.toString());

        StringBuffer buf2 = new StringBuffer();
        printer.format((Object) cal, buf2, pos);
        assertEquals("2023-07-04", buf2.toString());

        StringBuffer buf3 = new StringBuffer();
        printer.format((Object) Long.valueOf(date.getTime()), buf3, pos);
        assertEquals("2023-07-04", buf3.toString());
    }

    // Tests format with unsupported Object type throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFormat_invalidObjectType_throwsIllegalArgumentException() {
        FastDatePrinter printer = new FastDatePrinter("yyyy", UTC, US);
        printer.format("2023", new StringBuffer(), new FieldPosition(0));
    }

    // Tests format with null Object throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFormat_nullObject_throwsIllegalArgumentException() {
        FastDatePrinter printer = new FastDatePrinter("yyyy", UTC, US);
        printer.format((Object) null, new StringBuffer(), new FieldPosition(0));
    }

    // Tests 2-digit and 4-digit year formatting
    @Test
    public void testFormat_yearPatterns_formatsCorrectly() {
        FastDatePrinter printer2Digit = new FastDatePrinter("yy", UTC, US);
        assertEquals("23", printer2Digit.format(date));

        FastDatePrinter printer1Digit = new FastDatePrinter("y", UTC, US);
        assertEquals("2023", printer1Digit.format(date));

        FastDatePrinter printer4Digit = new FastDatePrinter("yyyy", UTC, US);
        assertEquals("2023", printer4Digit.format(date));

        FastDatePrinter printer5Digit = new FastDatePrinter("yyyyy", UTC, US);
        assertEquals("02023", printer5Digit.format(date));
    }

    // Tests month pattern variants (M, MM, MMM, MMMM)
    @Test
    public void testFormat_monthPatterns_formatsCorrectly() {
        FastDatePrinter p1 = new FastDatePrinter("M", UTC, US);
        FastDatePrinter p2 = new FastDatePrinter("MM", UTC, US);
        FastDatePrinter p3 = new FastDatePrinter("MMM", UTC, US);
        FastDatePrinter p4 = new FastDatePrinter("MMMM", UTC, US);

        assertEquals("7", p1.format(date));
        assertEquals("07", p2.format(date));
        assertEquals("Jul", p3.format(date));
        assertEquals("July", p4.format(date));

        Calendar jan = new GregorianCalendar(UTC, US);
        jan.clear();
        jan.set(2023, Calendar.JANUARY, 1);
        assertEquals("1", p1.format(jan));
        assertEquals("01", p2.format(jan));
    }

    // Tests 12-hour and 24-hour hour fields including boundary values 0, 12, 24
    @Test
    public void testFormat_hourPatterns_formatsCorrectly() {
        FastDatePrinter h = new FastDatePrinter("h:K:H:k a", UTC, US);
        // 16:30 -> h=4, K=4, H=16, k=16 PM
        assertEquals("4:4:16:16 PM", h.format(date));

        Calendar midnight = new GregorianCalendar(UTC, US);
        midnight.clear();
        midnight.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
        // Midnight -> h=12, K=0, H=0, k=24 AM
        assertEquals("12:0:0:24 AM", h.format(midnight));

        Calendar noon = new GregorianCalendar(UTC, US);
        noon.clear();
        noon.set(2023, Calendar.JANUARY, 1, 12, 0, 0);
        // Noon -> h=12, K=0, H=12, k=12 PM
        assertEquals("12:0:12:12 PM", h.format(noon));
    }

    // Tests day, week, era, and day-of-week patterns
    @Test
    public void testFormat_calendarFields_formatsCorrectly() {
        FastDatePrinter printer = new FastDatePrinter("G E EEEE d D F w W", UTC, US);
        // 2023-07-04 is Tuesday, Day 4 of month, Day 185 of year, 1st Tuesday, Week 27 of year, Week 2 of month
        String formatted = printer.format(date);
        assertTrue(formatted.startsWith("AD Tue Tuesday 4 185 1 27"));
    }

    // Tests timezone numeric patterns (Z and ZZ) with positive and negative offsets
    @Test
    public void testFormat_timeZoneNumberPatterns_formatsCorrectly() {
        FastDatePrinter zNoColon = new FastDatePrinter("Z", UTC, US);
        FastDatePrinter zColon = new FastDatePrinter("ZZ", UTC, US);

        assertEquals("+0000", zNoColon.format(date));
        assertEquals("+00:00", zColon.format(date));

        FastDatePrinter nyNoColon = new FastDatePrinter("Z", NEW_YORK, US);
        FastDatePrinter nyColon = new FastDatePrinter("ZZ", NEW_YORK, US);

        // July in New York is EDT (UTC-4)
        assertEquals("-0400", nyNoColon.format(date));
        assertEquals("-04:00", nyColon.format(date));
    }

    // Tests timezone name patterns (z and zzzz) and display name caching
    @Test
    public void testFormat_timeZoneNamePatterns_formatsCorrectly() {
        FastDatePrinter shortTz = new FastDatePrinter("z", UTC, US);
        FastDatePrinter longTz = new FastDatePrinter("zzzz", UTC, US);

        assertEquals("UTC", shortTz.format(date));
        assertEquals("Coordinated Universal Time", longTz.format(date));

        FastDatePrinter nyShortTz = new FastDatePrinter("z", NEW_YORK, US);
        assertEquals("EDT", nyShortTz.format(date));

        String cachedName = FastDatePrinter.getTimeZoneDisplay(NEW_YORK, true, TimeZone.SHORT, US);
        assertEquals("EDT", cachedName);
    }

    // Tests formatting Calendar with its own timezone when printer has different timezone
    @Test
    public void testFormat_calendarWithSpecificTimeZone_appliesTimeZoneCorrectly() {
        Calendar nyCal = new GregorianCalendar(NEW_YORK, US);
        nyCal.clear();
        nyCal.set(2023, Calendar.JULY, 4, 12, 0, 0); // 12:00 EDT = 16:00 UTC

        FastDatePrinter printer = new FastDatePrinter("HH:mm Z", NEW_YORK, US);
        String formatted = printer.format(nyCal);
        assertEquals("12:00 -0400", formatted);
    }

    // Tests quoted literals and escaped single quotes in patterns
    @Test
    public void testFormat_quotedLiterals_outputsLiteralText() {
        FastDatePrinter printer = new FastDatePrinter("yyyy 'o''clock' ''", UTC, US);
        assertEquals("2023 o'clock '", printer.format(date));

        FastDatePrinter charLiteralPrinter = new FastDatePrinter("'T'HH:mm", UTC, US);
        assertEquals("T16:30", charLiteralPrinter.format(date));
    }

    // Tests invalid pattern character throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testParsePattern_illegalComponent_throwsIllegalArgumentException() {
        new FastDatePrinter("yyyy-MM-dd X", UTC, US);
    }

    // Tests getters for pattern, time zone, locale, and estimate length
    @Test
    public void testAccessors_returnCorrectValues() {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        FastDatePrinter printer = new FastDatePrinter(pattern, UTC, US);

        assertEquals(pattern, printer.getPattern());
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
        assertFalse(printer1.equals("yyyy-MM-dd"));
        assertFalse(printer1.equals(printerDiffPattern));
        assertFalse(printer1.equals(printerDiffTz));
        assertFalse(printer1.equals(printerDiffLocale));

        String str = printer1.toString();
        assertTrue(str.contains("yyyy-MM-dd"));
        assertTrue(str.contains(US.toString()));
        assertTrue(str.contains(UTC.getID()));
    }

    // Tests serialization and deserialization of FastDatePrinter
    @Test
    public void testSerialization_roundTrip_preservesBehavior() throws Exception {
        FastDatePrinter original = new FastDatePrinter("yyyy-MM-dd HH:mm:ss", UTC, US);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FastDatePrinter deserialized = (FastDatePrinter) ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertEquals(original, deserialized);
        assertEquals(original.format(date), deserialized.format(date));
    }
}