package org.apache.commons.lang.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class FastDateFormatTest {

    // Tests default getInstance caching and pattern retrieval
    @Test
    public void testGetInstance_default_returnsValidInstance() {
        FastDateFormat fdf1 = FastDateFormat.getInstance();
        FastDateFormat fdf2 = FastDateFormat.getInstance();
        assertNotNull(fdf1);
        assertSame(fdf1, fdf2);
        assertNotNull(fdf1.getPattern());
        assertEquals(Locale.getDefault(), fdf1.getLocale());
        assertEquals(TimeZone.getDefault(), fdf1.getTimeZone());
        assertFalse(fdf1.getTimeZoneOverridesCalendar());
    }

    // Tests getInstance with custom pattern, timezone, and locale
    @Test
    public void testGetInstance_patternTimeZoneLocale_returnsConfiguredInstance() {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Locale locale = Locale.UK;

        FastDateFormat fdf = FastDateFormat.getInstance(pattern, tz, locale);
        assertNotNull(fdf);
        assertEquals(pattern, fdf.getPattern());
        assertEquals(tz, fdf.getTimeZone());
        assertEquals(locale, fdf.getLocale());
        assertTrue(fdf.getTimeZoneOverridesCalendar());
    }

    // Tests exception when pattern is null
    @Test(expected = IllegalArgumentException.class)
    public void testGetInstance_nullPattern_throwsIllegalArgumentException() {
        FastDateFormat.getInstance(null);
    }

    // Tests exception when pattern contains illegal characters
    @Test(expected = IllegalArgumentException.class)
    public void testGetInstance_illegalPattern_throwsIllegalArgumentException() {
        FastDateFormat.getInstance("yyyy-MM-dd QQ");
    }

    // Tests defect Lang-50: locale caching in getDateInstance when default locale changes
    @Test
    public void testGetDateInstance_changeDefaultLocale_returnsCorrectLocaleInstance() {
        Locale originalDefault = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            FastDateFormat format1 = FastDateFormat.getDateInstance(FastDateFormat.FULL, Locale.GERMANY);
            FastDateFormat format2 = FastDateFormat.getDateInstance(FastDateFormat.FULL);
            Locale.setDefault(Locale.GERMANY);
            FastDateFormat format3 = FastDateFormat.getDateInstance(FastDateFormat.FULL);

            assertSame(Locale.GERMANY, format1.getLocale());
            assertSame(Locale.US, format2.getLocale());
            assertSame(Locale.GERMANY, format3.getLocale());
            assertNotSame(format1, format2);
            assertNotSame(format2, format3);
        } finally {
            Locale.setDefault(originalDefault);
        }
    }

    // Tests defect Lang-50: locale caching in getTimeInstance when default locale changes
    @Test
    public void testGetTimeInstance_changeDefaultLocale_returnsCorrectLocaleInstance() {
        Locale originalDefault = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            FastDateFormat format1 = FastDateFormat.getTimeInstance(FastDateFormat.FULL, Locale.GERMANY);
            FastDateFormat format2 = FastDateFormat.getTimeInstance(FastDateFormat.FULL);
            Locale.setDefault(Locale.GERMANY);
            FastDateFormat format3 = FastDateFormat.getTimeInstance(FastDateFormat.FULL);

            assertSame(Locale.GERMANY, format1.getLocale());
            assertSame(Locale.US, format2.getLocale());
            assertSame(Locale.GERMANY, format3.getLocale());
            assertNotSame(format1, format2);
            assertNotSame(format2, format3);
        } finally {
            Locale.setDefault(originalDefault);
        }
    }

    // Tests defect Lang-50: locale caching in getDateTimeInstance when default locale changes
    @Test
    public void testGetDateTimeInstance_changeDefaultLocale_returnsCorrectLocaleInstance() {
        Locale originalDefault = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            FastDateFormat format1 = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL, Locale.GERMANY);
            FastDateFormat format2 = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL);
            Locale.setDefault(Locale.GERMANY);
            FastDateFormat format3 = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL);

            assertSame(Locale.GERMANY, format1.getLocale());
            assertSame(Locale.US, format2.getLocale());
            assertSame(Locale.GERMANY, format3.getLocale());
            assertNotSame(format1, format2);
            assertNotSame(format2, format3);
        } finally {
            Locale.setDefault(originalDefault);
        }
    }

    // Tests formatting with various standard tokens and literal handling
    @Test
    public void testFormat_comprehensivePattern_formatsCorrectly() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Locale locale = Locale.US;
        String pattern = "G yyyy-yy-y MMMM-MMM-MM-M dd-d hh-h HH-H mm-m ss-s SSS-S EEEE-E D-F-w-W-a '''' 'literal'";
        FastDateFormat fdf = FastDateFormat.getInstance(pattern, tz, locale);

        Calendar cal = new GregorianCalendar(tz, locale);
        cal.clear();
        cal.set(2004, Calendar.JANUARY, 5, 8, 9, 7);
        cal.set(Calendar.MILLISECOND, 6);

        String expected = "AD 2004-04-2004 January-Jan-01-1 05-5 08-8 08-8 09-9 07-7 006-6 Monday-Mon 5-1-2-2-AM ' literal";
        assertEquals(expected, fdf.format(cal));
        assertEquals(expected, fdf.format(cal.getTime()));
        assertEquals(expected, fdf.format(cal.getTimeInMillis()));
    }

    // Tests special hour conversions for 12-hour and 24-hour clocks at midnight
    @Test
    public void testFormat_midnightHourConversions_formatsCorrectly() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar cal = new GregorianCalendar(tz, Locale.US);
        cal.clear();
        cal.set(2023, Calendar.MARCH, 15, 0, 30, 0);

        FastDateFormat fdf12 = FastDateFormat.getInstance("hh-h-K-KK", tz, Locale.US);
        assertEquals("12-12-0-00", fdf12.format(cal));

        FastDateFormat fdf24 = FastDateFormat.getInstance("HH-H-k-kk", tz, Locale.US);
        assertEquals("00-0-24-24", fdf24.format(cal));
    }

    // Tests timezone name and numeric timezone format rules
    @Test
    public void testFormat_timeZoneRules_formatsExpectedOutputs() {
        TimeZone tz = TimeZone.getTimeZone("GMT+02:00");
        Calendar cal = new GregorianCalendar(tz, Locale.US);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1, 12, 0, 0);

        FastDateFormat fdf = FastDateFormat.getInstance("z zzzz Z ZZ", tz, Locale.US);
        String formatted = fdf.format(cal);
        assertTrue(formatted.contains("+0200"));
        assertTrue(formatted.contains("+02:00"));

        TimeZone tzNegative = TimeZone.getTimeZone("GMT-05:00");
        FastDateFormat fdfNeg = FastDateFormat.getInstance("Z ZZ", tzNegative, Locale.US);
        Calendar calNeg = new GregorianCalendar(tzNegative, Locale.US);
        calNeg.clear();
        calNeg.set(2023, Calendar.JANUARY, 1, 12, 0, 0);
        assertEquals("-0500 -05:00", fdfNeg.format(calNeg));
    }

    // Tests format method accepting Object and StringBuffer
    @Test
    public void testFormat_objectTypesAndStringBuffer_appendsCorrectly() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.FEBRUARY, 10);
        Date date = cal.getTime();
        Long millis = new Long(date.getTime());

        StringBuffer buf1 = new StringBuffer("Date: ");
        StringBuffer buf2 = new StringBuffer("Cal: ");
        StringBuffer buf3 = new StringBuffer("Long: ");

        fdf.format(date, buf1, new FieldPosition(0));
        fdf.format(cal, buf2, new FieldPosition(0));
        fdf.format(millis, buf3, new FieldPosition(0));

        assertEquals("Date: 2023-02-10", buf1.toString());
        assertEquals("Cal: 2023-02-10", buf2.toString());
        assertEquals("Long: 2023-02-10", buf3.toString());
    }

    // Tests format with invalid object type
    @Test(expected = IllegalArgumentException.class)
    public void testFormat_invalidObjectType_throwsIllegalArgumentException() {
        FastDateFormat fdf = FastDateFormat.getInstance();
        fdf.format("2023-01-01", new StringBuffer(), new FieldPosition(0));
    }

    // Tests parseObject unsupported behavior
    @Test
    public void testParseObject_always_returnsNullAndResetsPosition() {
        FastDateFormat fdf = FastDateFormat.getInstance();
        ParsePosition pos = new ParsePosition(5);
        Object result = fdf.parseObject("2023-01-01", pos);
        assertNull(result);
        assertEquals(0, pos.getIndex());
        assertEquals(0, pos.getErrorIndex());
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_sameAndDifferentParameters_correctBehavior() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat fdf2 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat fdf3 = FastDateFormat.getInstance("yyyy/MM/dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat fdf4 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);

        assertEquals(fdf1, fdf2);
        assertEquals(fdf1.hashCode(), fdf2.hashCode());
        assertFalse(fdf1.equals(fdf3));
        assertFalse(fdf1.equals(fdf4));
        assertFalse(fdf1.equals("Not a FastDateFormat"));
        assertFalse(fdf1.equals(null));
    }

    // Tests toString format
    @Test
    public void testToString_validPattern_returnsFormattedString() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        assertEquals("FastDateFormat[yyyy-MM-dd]", fdf.toString());
    }

    // Tests serialization and deserialization
    @Test
    public void testSerialization_roundTrip_instanceFunctionsCorrectly() throws Exception {
        FastDateFormat original = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT"), Locale.US);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.flush();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        FastDateFormat deserialized = (FastDateFormat) ois.readObject();

        assertEquals(original, deserialized);
        assertEquals(original.getMaxLengthEstimate(), deserialized.getMaxLengthEstimate());

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
        assertEquals("2023-01-01 00:00:00", deserialized.format(cal));
    }
}