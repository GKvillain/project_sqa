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

    // Tests default factory method and formatting
    @Test
    public void testGetInstance_default_formatsCorrectly() {
        FastDateFormat fdf = FastDateFormat.getInstance();
        assertNotNull(fdf);
        assertNotNull(fdf.getPattern());
        Date now = new Date();
        assertNotNull(fdf.format(now));
    }

    // Tests custom pattern factory method and formatting Date
    @Test
    public void testGetInstance_customPattern_formatsDate() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
        Calendar cal = new GregorianCalendar(2023, Calendar.MARCH, 15, 14, 30, 45);
        assertEquals("2023-03-15 14:30:45", fdf.format(cal.getTime()));
    }

    // Tests getInstance with pattern, timeZone and locale
    @Test
    public void testGetInstance_withTimeZoneAndLocale_appliesSettings() {
        TimeZone tz = TimeZone.getTimeZone("GMT+0");
        Locale locale = Locale.US;
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd z", tz, locale);
        
        Calendar cal = Calendar.getInstance(tz, locale);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
        
        assertEquals(tz, fdf.getTimeZone());
        assertEquals(locale, fdf.getLocale());
        assertTrue(fdf.getTimeZoneOverridesCalendar());
        assertEquals("2023-01-01 GMT+00:00", fdf.format(cal));
    }

    // Tests invalid pattern throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetInstance_invalidPattern_throwsException() {
        FastDateFormat.getInstance("yyyy-MM-dd X");
    }

    // Tests null pattern throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetInstance_nullPattern_throwsException() {
        FastDateFormat.getInstance(null);
    }

    // Tests getDateInstance factory methods and cache
    @Test
    public void testGetDateInstance_variousStyles_returnsFormatters() {
        FastDateFormat fdfShort = FastDateFormat.getDateInstance(FastDateFormat.SHORT, Locale.US);
        FastDateFormat fdfMedium = FastDateFormat.getDateInstance(FastDateFormat.MEDIUM, TimeZone.getTimeZone("UTC"));
        FastDateFormat fdfLong = FastDateFormat.getDateInstance(FastDateFormat.LONG);

        assertNotNull(fdfShort);
        assertNotNull(fdfMedium);
        assertNotNull(fdfLong);
        
        FastDateFormat fdfShortCached = FastDateFormat.getDateInstance(FastDateFormat.SHORT, Locale.US);
        assertSame(fdfShort, fdfShortCached);
    }

    // Tests getTimeInstance factory methods
    @Test
    public void testGetTimeInstance_validStyle_returnsFormatter() {
        FastDateFormat fdf = FastDateFormat.getTimeInstance(FastDateFormat.SHORT, Locale.US);
        assertNotNull(fdf);

        FastDateFormat fdfWithTz = FastDateFormat.getTimeInstance(FastDateFormat.SHORT, TimeZone.getTimeZone("GMT"));
        assertNotNull(fdfWithTz);

        FastDateFormat fdfAll = FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM, TimeZone.getTimeZone("GMT"), Locale.US);
        assertNotNull(fdfAll);
    }

    // Tests getDateTimeInstance factory methods
    @Test
    public void testGetDateTimeInstance_validStyles_returnsFormatter() {
        FastDateFormat fdf = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT);
        assertNotNull(fdf);

        FastDateFormat fdfLocale = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT, Locale.US);
        assertNotNull(fdfLocale);

        FastDateFormat fdfTz = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT, TimeZone.getTimeZone("GMT"));
        assertNotNull(fdfTz);

        FastDateFormat fdfAll = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL, TimeZone.getTimeZone("GMT"), Locale.US);
        assertNotNull(fdfAll);
    }

    // Tests comprehensive pattern tokens formatting
    @Test
    public void testFormat_allPatternTokens_formatsCorrectly() {
        TimeZone tz = TimeZone.getTimeZone("GMT+0");
        Locale locale = Locale.US;
        String pattern = "G yyyy yy MMMM MMM MM M d h H m s S EEEE E D F w W a k K z zzzz Z ZZ '' 'text'";
        FastDateFormat fdf = FastDateFormat.getInstance(pattern, tz, locale);

        Calendar cal = Calendar.getInstance(tz, locale);
        cal.clear();
        cal.set(2023, Calendar.MARCH, 5, 0, 15, 30);
        cal.set(Calendar.MILLISECOND, 7);

        String result = fdf.format(cal);
        assertNotNull(result);
        assertTrue(result.contains("AD"));
        assertTrue(result.contains("2023"));
        assertTrue(result.contains("23"));
        assertTrue(result.contains("March"));
        assertTrue(result.contains("Mar"));
        assertTrue(result.contains("03"));
        assertTrue(result.contains("3"));
        assertTrue(result.contains("5"));
        assertTrue(result.contains("Sunday"));
        assertTrue(result.contains("Sun"));
        assertTrue(result.contains("AM"));
        assertTrue(result.contains("+0000"));
        assertTrue(result.contains("+00:00"));
        assertTrue(result.contains("' text"));
    }

    // Tests numeric padding rules with various digits
    @Test
    public void testFormat_paddedNumberField_padsZeroes() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd SSS");
        Calendar cal = new GregorianCalendar(2023, Calendar.JANUARY, 2);
        cal.set(Calendar.MILLISECOND, 5);

        String formatted = fdf.format(cal);
        assertEquals("2023-01-02 005", formatted);

        cal.set(Calendar.MILLISECOND, 50);
        assertEquals("2023-01-02 050", fdf.format(cal));

        cal.set(Calendar.MILLISECOND, 500);
        assertEquals("2023-01-02 500", fdf.format(cal));
    }

    // Tests hour edge cases (12-hour and 24-hour midnight/noon values)
    @Test
    public void testFormat_hourEdgeCases_formatsCorrectly() {
        FastDateFormat fdf = FastDateFormat.getInstance("h H K k");
        Calendar cal = Calendar.getInstance();
        cal.clear();
        
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
        assertEquals("12 0 0 24", fdf.format(cal));

        cal.set(2023, Calendar.JANUARY, 1, 12, 0, 0);
        assertEquals("12 12 0 12", fdf.format(cal));
    }

    // Tests format overloads with StringBuffer and primitive types
    @Test
    public void testFormat_overloadsAndBuffer_appendsSuccessfully() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        Calendar cal = new GregorianCalendar(2023, Calendar.DECEMBER, 25);
        long millis = cal.getTimeInMillis();

        assertEquals("2023-12-25", fdf.format(millis));
        assertEquals("2023-12-25", fdf.format(cal));

        StringBuffer sb = new StringBuffer("Date: ");
        StringBuffer returnedSb = fdf.format(millis, sb);
        assertSame(sb, returnedSb);
        assertEquals("Date: 2023-12-25", sb.toString());

        sb = new StringBuffer();
        fdf.format(cal.getTime(), sb);
        assertEquals("2023-12-25", sb.toString());
    }

    // Tests format(Object, StringBuffer, FieldPosition) polymorphic behavior
    @Test
    public void testFormat_objectFormatMethod_supportsTypes() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy");
        Calendar cal = new GregorianCalendar(2023, Calendar.JANUARY, 1);
        FieldPosition pos = new FieldPosition(0);

        StringBuffer sb1 = fdf.format((Object) cal.getTime(), new StringBuffer(), pos);
        assertEquals("2023", sb1.toString());

        StringBuffer sb2 = fdf.format((Object) cal, new StringBuffer(), pos);
        assertEquals("2023", sb2.toString());

        StringBuffer sb3 = fdf.format((Object) new Long(cal.getTimeInMillis()), new StringBuffer(), pos);
        assertEquals("2023", sb3.toString());
    }

    // Tests format(Object) with invalid type throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFormat_invalidObjectType_throwsException() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy");
        fdf.format("2023-01-01", new StringBuffer(), new FieldPosition(0));
    }

    // Tests parseObject returns null as not supported
    @Test
    public void testParseObject_unsupported_returnsNullAndResetsPosition() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(5);
        Object result = fdf.parseObject("2023-01-01", pos);
        assertNull(result);
        assertEquals(0, pos.getIndex());
        assertEquals(0, pos.getErrorIndex());
    }

    // Tests equals, hashCode, and toString consistency
    @Test
    public void testEqualsAndHashCode_consistency() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat fdf2 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat fdf3 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);

        assertTrue(fdf1.equals(fdf2));
        assertFalse(fdf1.equals(fdf3));
        assertFalse(fdf1.equals(null));
        assertFalse(fdf1.equals("yyyy-MM-dd"));

        assertEquals(fdf1.hashCode(), fdf2.hashCode());
        assertEquals("FastDateFormat[yyyy-MM-dd]", fdf1.toString());
        assertTrue(fdf1.getMaxLengthEstimate() > 0);
    }

    // Tests serialization roundtrip and verifies rules are properly initialized
    @Test
    public void testSerialization_roundTrip_rulesPreserved() throws Exception {
        FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss z", TimeZone.getTimeZone("GMT"), Locale.US);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(format);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FastDateFormat deserialized = (FastDateFormat) ois.readObject();
        ois.close();

        assertEquals(format, deserialized);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1, 12, 0, 0);

        assertEquals("2023-01-01 12:00:00 GMT+00:00", deserialized.format(cal));
    }
}