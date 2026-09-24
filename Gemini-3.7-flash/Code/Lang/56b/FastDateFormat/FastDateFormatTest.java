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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FastDateFormatTest {

    // Tests default instance creation and formatting
    @Test
    public void testGetInstance_default_returnsNonNullInstance() {
        FastDateFormat format = FastDateFormat.getInstance();
        assertNotNull(format);
        assertNotNull(format.getPattern());
        assertEquals(Locale.getDefault(), format.getLocale());
        assertEquals(TimeZone.getDefault(), format.getTimeZone());
        assertFalse(format.getTimeZoneOverridesCalendar());
    }

    // Tests custom pattern formatting for Date
    @Test
    public void testGetInstance_customPattern_formatsDateCorrectly() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 15, 13, 45, 30);
        cal.set(Calendar.MILLISECOND, 0);

        String result = fdf.format(cal.getTime());
        assertEquals("2023-01-15 13:45:30", result);
    }

    // Tests null pattern throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetInstance_nullPattern_throwsIllegalArgumentException() {
        FastDateFormat.getInstance(null);
    }

    // Tests illegal pattern character throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetInstance_invalidPattern_throwsIllegalArgumentException() {
        FastDateFormat.getInstance("yyyy-MM-dd X");
    }

    // Tests formatting via format(Object, StringBuffer, FieldPosition)
    @Test
    public void testFormat_objectTypes_formatsAppropriately() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy/MM/dd", TimeZone.getTimeZone("GMT"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.set(2023, Calendar.MARCH, 10, 0, 0, 0);
        Date date = cal.getTime();

        StringBuffer sbDate = fdf.format((Object) date, new StringBuffer(), new FieldPosition(0));
        assertEquals("2023/03/10", sbDate.toString());

        StringBuffer sbCal = fdf.format((Object) cal, new StringBuffer(), new FieldPosition(0));
        assertEquals("2023/03/10", sbCal.toString());

        StringBuffer sbLong = fdf.format(new Long(date.getTime()), new StringBuffer(), new FieldPosition(0));
        assertEquals("2023/03/10", sbLong.toString());
    }

    // Tests formatting invalid object type throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFormat_invalidObjectType_throwsIllegalArgumentException() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy");
        fdf.format("2023", new StringBuffer(), new FieldPosition(0));
    }

    // Tests formatting null object throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFormat_nullObject_throwsIllegalArgumentException() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy");
        fdf.format(null, new StringBuffer(), new FieldPosition(0));
    }

    // Tests various pattern rules (numeric, padded, text, literal, escaped literal, hours)
    @Test
    public void testFormat_patternRulesCoverage_formatsExpectedOutput() {
        String pattern = "G yyyy yy MMMM MMM MM M d h H m s SSS E EEEE D F w W a k K '' 'quoted text'";
        FastDateFormat fdf = FastDateFormat.getInstance(pattern, TimeZone.getTimeZone("GMT"), Locale.US);

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.set(2023, Calendar.JULY, 4, 0, 5, 9);
        cal.set(Calendar.MILLISECOND, 7);

        String result = fdf.format(cal);
        assertTrue(result.contains("AD"));
        assertTrue(result.contains("2023"));
        assertTrue(result.contains("23"));
        assertTrue(result.contains("July"));
        assertTrue(result.contains("Jul"));
        assertTrue(result.contains("07"));
        assertTrue(result.contains("4"));
        assertTrue(result.contains("12")); // 12-hour at midnight
        assertTrue(result.contains("007")); // millisecond padded
        assertTrue(result.contains("Tue"));
        assertTrue(result.contains("Tuesday"));
        assertTrue(result.contains("AM"));
        assertTrue(result.contains("24")); // 24-hour 'k' at midnight
        assertTrue(result.contains("0"));  // 'K' at midnight
        assertTrue(result.contains("'"));
        assertTrue(result.contains("quoted text"));
    }

    // Tests timezone format patterns 'z', 'zzzz', 'Z', 'ZZ'
    @Test
    public void testFormat_timeZoneRules_formatsExpectedTimeZones() {
        FastDateFormat fdfShortZ = FastDateFormat.getInstance("z Z ZZ", TimeZone.getTimeZone("GMT"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);

        String formatted = fdfShortZ.format(cal);
        assertTrue(formatted.contains("GMT"));
        assertTrue(formatted.contains("+0000"));
        assertTrue(formatted.contains("+00:00"));

        FastDateFormat fdfLongZ = FastDateFormat.getInstance("zzzz", TimeZone.getTimeZone("GMT"), Locale.US);
        String formattedLong = fdfLongZ.format(cal);
        assertTrue(formattedLong.contains("Greenwich Mean Time") || formattedLong.contains("GMT"));
    }

    // Tests factory methods for standard date instances
    @Test
    public void testGetDateInstance_variousStyles_returnsInstances() {
        FastDateFormat dfFull = FastDateFormat.getDateInstance(FastDateFormat.FULL, Locale.US);
        FastDateFormat dfLong = FastDateFormat.getDateInstance(FastDateFormat.LONG, TimeZone.getTimeZone("GMT"));
        FastDateFormat dfMed = FastDateFormat.getDateInstance(FastDateFormat.MEDIUM, TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat dfShort = FastDateFormat.getDateInstance(FastDateFormat.SHORT);

        assertNotNull(dfFull);
        assertNotNull(dfLong);
        assertNotNull(dfMed);
        assertNotNull(dfShort);
    }

    // Tests factory methods for standard time instances
    @Test
    public void testGetTimeInstance_variousStyles_returnsInstances() {
        FastDateFormat tfFull = FastDateFormat.getTimeInstance(FastDateFormat.FULL, Locale.US);
        FastDateFormat tfLong = FastDateFormat.getTimeInstance(FastDateFormat.LONG, TimeZone.getTimeZone("GMT"));
        FastDateFormat tfMed = FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM, TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat tfShort = FastDateFormat.getTimeInstance(FastDateFormat.SHORT);

        assertNotNull(tfFull);
        assertNotNull(tfLong);
        assertNotNull(tfMed);
        assertNotNull(tfShort);
    }

    // Tests factory methods for standard date-time instances
    @Test
    public void testGetDateTimeInstance_variousStyles_returnsInstances() {
        FastDateFormat dtf1 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT);
        FastDateFormat dtf2 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT, Locale.US);
        FastDateFormat dtf3 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT, TimeZone.getTimeZone("GMT"));
        FastDateFormat dtf4 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT, TimeZone.getTimeZone("GMT"), Locale.US);

        assertNotNull(dtf1);
        assertNotNull(dtf2);
        assertNotNull(dtf3);
        assertNotNull(dtf4);
    }

    // Tests format using millisecond long input
    @Test
    public void testFormat_longMillis_formatsCorrectly() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy", TimeZone.getTimeZone("GMT"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);

        assertEquals("2023", fdf.format(cal.getTimeInMillis()));
        StringBuffer buf = new StringBuffer("Year: ");
        assertEquals("Year: 2023", fdf.format(cal.getTimeInMillis(), buf).toString());
    }

    // Tests parseObject method always returns null and resets position
    @Test
    public void testParseObject_alwaysReturnsNull() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(2);
        Object parsed = fdf.parseObject("2023-01-01", pos);
        assertNull(parsed);
        assertEquals(0, pos.getIndex());
        assertEquals(0, pos.getErrorIndex());
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEqualsAndHashCode_sameAndDifferentObjects_worksCorrectly() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat fdf2 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat fdfDiffPattern = FastDateFormat.getInstance("yyyy/MM/dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat fdfDiffTz = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDateFormat fdfDiffLocale = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.FRANCE);

        assertTrue(fdf1.equals(fdf1));
        assertTrue(fdf1.equals(fdf2));
        assertEquals(fdf1.hashCode(), fdf2.hashCode());

        assertFalse(fdf1.equals(null));
        assertFalse(fdf1.equals("NotAFastDateFormat"));
        assertFalse(fdf1.equals(fdfDiffPattern));
        assertFalse(fdf1.equals(fdfDiffTz));
        assertFalse(fdf1.equals(fdfDiffLocale));
    }

    // Tests accessors and toString
    @Test
    public void testToStringAndAccessors_validState_returnsExpectedValues() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        assertEquals("yyyy-MM-dd", fdf.getPattern());
        assertEquals(TimeZone.getTimeZone("GMT"), fdf.getTimeZone());
        assertEquals(Locale.US, fdf.getLocale());
        assertTrue(fdf.getMaxLengthEstimate() > 0);
        assertTrue(fdf.getTimeZoneOverridesCalendar());
        assertEquals("FastDateFormat[yyyy-MM-dd]", fdf.toString());
    }

    // Tests serialization and deserialization of FastDateFormat
    @Test
    public void testSerialization_roundTrip_canFormatAfterDeserialization() throws Exception {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT"), Locale.US);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(fdf);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FastDateFormat deserialized = (FastDateFormat) ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertEquals(fdf, deserialized);

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 15, 10, 20, 30);
        assertEquals("2023-01-15 10:20:30", deserialized.format(cal.getTime()));
    }
}