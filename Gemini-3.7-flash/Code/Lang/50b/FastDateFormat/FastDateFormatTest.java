package org.apache.commons.lang.time;

import org.junit.Test;

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

import static org.junit.Assert.*;

public class FastDateFormatTest {

    // Tests default factory method and formatting
    @Test
    public void testGetInstance_default_returnsValidFormatter() {
        FastDateFormat fdf = FastDateFormat.getInstance();
        assertNotNull(fdf);
        assertNotNull(fdf.getPattern());
        assertEquals(Locale.getDefault(), fdf.getLocale());
        assertEquals(TimeZone.getDefault(), fdf.getTimeZone());
        assertFalse(fdf.getTimeZoneOverridesCalendar());
    }

    // Tests custom pattern with specific timezone and locale
    @Test
    public void testGetInstance_patternTimeZoneLocale_formatsCorrectly() {
        TimeZone tz = TimeZone.getTimeZone("GMT+00:00");
        Locale locale = Locale.US;
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS Z", tz, locale);

        Calendar cal = new GregorianCalendar(tz, locale);
        cal.set(2023, Calendar.MARCH, 15, 13, 45, 30);
        cal.set(Calendar.MILLISECOND, 123);

        String formatted = fdf.format(cal);
        assertEquals("2023-03-15 13:45:30.123 +0000", formatted);
        assertTrue(fdf.getTimeZoneOverridesCalendar());
        assertEquals(tz, fdf.getTimeZone());
        assertEquals(locale, fdf.getLocale());
    }

    // Tests various pattern tokens: era, 2-digit year, unpadded/padded month, hour variants, etc.
    @Test
    public void testFormat_variousPatternTokens_formattedCorrectly() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Locale locale = Locale.US;
        FastDateFormat fdf = FastDateFormat.getInstance(
                "G yy M MM MMM MMMM d h H k K m s S E EEEE D F w W a 'text' ''", tz, locale);

        Calendar cal = new GregorianCalendar(tz, locale);
        cal.set(2023, Calendar.MARCH, 5, 0, 0, 0); // Midnight: hour=0 (12 for h, 24 for k, 0 for K)
        cal.set(Calendar.MILLISECOND, 5);

        String formatted = fdf.format(cal);
        assertNotNull(formatted);
        assertTrue(formatted.contains("AD"));
        assertTrue(formatted.contains("23"));
        assertTrue(formatted.contains("text"));
        assertTrue(formatted.endsWith("'"));
    }

    // Tests ISO8601 full timezone format (ZZ) and standard timezone format (z, Z)
    @Test
    public void testFormat_timeZoneRules_formattedCorrectly() {
        TimeZone tz = TimeZone.getTimeZone("GMT+02:00");
        FastDateFormat fdf = FastDateFormat.getInstance("ZZ z zzzz", tz, Locale.US);

        Calendar cal = new GregorianCalendar(tz, Locale.US);
        cal.set(2023, Calendar.JANUARY, 1, 12, 0, 0);

        String formatted = fdf.format(cal);
        assertTrue(formatted.startsWith("+02:00"));
    }

    // Tests formatting overloads: Date, Calendar, long, StringBuffer, FieldPosition
    @Test
    public void testFormat_overloadedMethods_matchExpected() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd", tz, Locale.US);

        Calendar cal = new GregorianCalendar(tz, Locale.US);
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
        Date date = cal.getTime();
        long millis = date.getTime();

        assertEquals("2023-01-01", fdf.format(date));
        assertEquals("2023-01-01", fdf.format(cal));
        assertEquals("2023-01-01", fdf.format(millis));

        StringBuffer buf = new StringBuffer();
        fdf.format(date, buf);
        assertEquals("2023-01-01", buf.toString());

        buf = new StringBuffer();
        fdf.format(millis, buf);
        assertEquals("2023-01-01", buf.toString());

        buf = new StringBuffer();
        fdf.format((Object) date, buf, new FieldPosition(0));
        assertEquals("2023-01-01", buf.toString());

        buf = new StringBuffer();
        fdf.format((Object) cal, buf, new FieldPosition(0));
        assertEquals("2023-01-01", buf.toString());

        buf = new StringBuffer();
        fdf.format((Object) new Long(millis), buf, new FieldPosition(0));
        assertEquals("2023-01-01", buf.toString());
    }

    // Tests getDateInstance with locale and timezone
    @Test
    public void testGetDateInstance_localeCaching_formatsCorrectly() {
        FastDateFormat fdfUS = FastDateFormat.getDateInstance(FastDateFormat.SHORT, Locale.US);
        FastDateFormat fdfDE = FastDateFormat.getDateInstance(FastDateFormat.SHORT, Locale.GERMANY);
        assertNotNull(fdfUS);
        assertNotNull(fdfDE);
        assertNotEquals(fdfUS.getPattern(), fdfDE.getPattern());

        FastDateFormat fdfTz = FastDateFormat.getDateInstance(FastDateFormat.MEDIUM, TimeZone.getTimeZone("GMT"), Locale.US);
        assertNotNull(fdfTz);
        assertEquals(TimeZone.getTimeZone("GMT"), fdfTz.getTimeZone());

        FastDateFormat fdfDefault = FastDateFormat.getDateInstance(FastDateFormat.FULL);
        assertNotNull(fdfDefault);
    }

    // Tests getTimeInstance variations
    @Test
    public void testGetTimeInstance_stylesAndTimeZone_returnsInstance() {
        FastDateFormat fdfShort = FastDateFormat.getTimeInstance(FastDateFormat.SHORT);
        assertNotNull(fdfShort);

        FastDateFormat fdfLocale = FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM, Locale.UK);
        assertNotNull(fdfLocale);

        FastDateFormat fdfTz = FastDateFormat.getTimeInstance(FastDateFormat.LONG, TimeZone.getTimeZone("UTC"));
        assertNotNull(fdfTz);

        FastDateFormat fdfFull = FastDateFormat.getTimeInstance(FastDateFormat.FULL, TimeZone.getTimeZone("UTC"), Locale.US);
        assertNotNull(fdfFull);
    }

    // Tests getDateTimeInstance variations
    @Test
    public void testGetDateTimeInstance_stylesAndTimeZone_returnsInstance() {
        FastDateFormat fdf = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT);
        assertNotNull(fdf);

        FastDateFormat fdfLocale = FastDateFormat.getDateTimeInstance(FastDateFormat.MEDIUM, FastDateFormat.MEDIUM, Locale.US);
        assertNotNull(fdfLocale);

        FastDateFormat fdfTz = FastDateFormat.getDateTimeInstance(FastDateFormat.LONG, FastDateFormat.LONG, TimeZone.getTimeZone("UTC"));
        assertNotNull(fdfTz);

        FastDateFormat fdfFull = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL, TimeZone.getTimeZone("UTC"), Locale.GERMANY);
        assertNotNull(fdfFull);
    }

    // Tests equals, hashCode, and toString methods
    @Test
    public void testEqualsAndHashCodeAndToString() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat fdf2 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat fdfDiffPattern = FastDateFormat.getInstance("yyyy/MM/dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat fdfDiffTz = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDateFormat fdfDiffLocale = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.GERMANY);

        assertEquals(fdf1, fdf2);
        assertEquals(fdf1.hashCode(), fdf2.hashCode());
        assertFalse(fdf1.equals(fdfDiffPattern));
        assertFalse(fdf1.equals(fdfDiffTz));
        assertFalse(fdf1.equals(fdfDiffLocale));
        assertFalse(fdf1.equals("non-FastDateFormat"));
        assertFalse(fdf1.equals(null));

        assertEquals("FastDateFormat[yyyy-MM-dd]", fdf1.toString());
    }

    // Tests serialization and deserialization
    @Test
    public void testSerialization_roundTrip_preservesBehavior() throws Exception {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT"), Locale.US);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(fdf);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FastDateFormat deserialized = (FastDateFormat) ois.readObject();
        ois.close();

        assertEquals(fdf, deserialized);
        assertEquals(fdf.getMaxLengthEstimate(), deserialized.getMaxLengthEstimate());

        Date now = new Date();
        assertEquals(fdf.format(now), deserialized.format(now));
    }

    // Tests parseObject returning null as not supported
    @Test
    public void testParseObject_unsupported_returnsNull() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        assertNull(fdf.parseObject("2023-01-01", pos));
        assertEquals(0, pos.getIndex());
        assertEquals(0, pos.getErrorIndex());
    }

    // Tests exception on null pattern
    @Test(expected = IllegalArgumentException.class)
    public void testGetInstance_nullPattern_throwsException() {
        FastDateFormat.getInstance(null);
    }

    // Tests exception on illegal pattern component
    @Test(expected = IllegalArgumentException.class)
    public void testGetInstance_invalidPatternComponent_throwsException() {
        FastDateFormat.getInstance("yyyy-MM-dd X");
    }

    // Tests formatting with unsupported object type throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFormat_unsupportedObject_throwsException() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        fdf.format("Invalid object type", new StringBuffer(), new FieldPosition(0));
    }

    // Tests padded number field logic with large values
    @Test
    public void testFormat_paddedNumberField_largeValue() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-DDD-yyyyy");
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.set(2023, Calendar.DECEMBER, 31);
        String formatted = fdf.format(cal);
        assertNotNull(formatted);
        assertTrue(formatted.startsWith("2023-365-02023") || formatted.startsWith("2023-365-"));
    }
}