package org.apache.commons.lang3.time;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class FastDateFormatTest {

    private TimeZone originalDefault;
    private Locale originalLocale;

    @Before
    public void setUp() {
        originalDefault = TimeZone.getDefault();
        originalLocale = Locale.getDefault();
    }

    @After
    public void tearDown() {
        TimeZone.setDefault(originalDefault);
        Locale.setDefault(originalLocale);
    }

    // Tests that getDateInstance uses current default time zone
    @Test
    public void testGetDateInstance_timeZoneChange_reflectsNewDefault() {
        TimeZone tz1 = TimeZone.getTimeZone("GMT+5");
        TimeZone tz2 = TimeZone.getTimeZone("GMT-3");
        TimeZone.setDefault(tz1);
        FastDateFormat fdf1 = FastDateFormat.getDateInstance(FastDateFormat.SHORT);
        TimeZone.setDefault(tz2);
        FastDateFormat fdf2 = FastDateFormat.getDateInstance(FastDateFormat.SHORT);
        assertEquals("Should use new default time zone", tz2, fdf2.getTimeZone());
    }

    // Tests that getTimeInstance uses current default time zone
    @Test
    public void testGetTimeInstance_timeZoneChange_reflectsNewDefault() {
        TimeZone tz1 = TimeZone.getTimeZone("GMT+5");
        TimeZone tz2 = TimeZone.getTimeZone("GMT-3");
        TimeZone.setDefault(tz1);
        FastDateFormat fdf1 = FastDateFormat.getTimeInstance(FastDateFormat.SHORT);
        TimeZone.setDefault(tz2);
        FastDateFormat fdf2 = FastDateFormat.getTimeInstance(FastDateFormat.SHORT);
        assertEquals("Should use new default time zone", tz2, fdf2.getTimeZone());
    }

    // Tests that getDateTimeInstance uses current default time zone
    @Test
    public void testGetDateTimeInstance_timeZoneChange_reflectsNewDefault() {
        TimeZone tz1 = TimeZone.getTimeZone("GMT+5");
        TimeZone tz2 = TimeZone.getTimeZone("GMT-3");
        TimeZone.setDefault(tz1);
        FastDateFormat fdf1 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT);
        TimeZone.setDefault(tz2);
        FastDateFormat fdf2 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT);
        assertEquals("Should use new default time zone", tz2, fdf2.getTimeZone());
    }

    // Tests default getInstance returns non-null
    @Test
    public void testGetInstance_default_returnsNonNull() {
        assertNotNull(FastDateFormat.getInstance());
    }

    // Tests getInstance with pattern returns correct pattern
    @Test
    public void testGetInstance_withPattern_returnsCorrectPattern() {
        String pattern = "yyyy-MM-dd";
        FastDateFormat fdf = FastDateFormat.getInstance(pattern);
        assertEquals(pattern, fdf.getPattern());
    }

    // Tests getInstance with null pattern throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testGetInstance_nullPattern_throwsException() {
        FastDateFormat.getInstance((String) null);
    }

    // Tests format Date produces correct string
    @Test
    public void testFormat_date_returnsFormattedString() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy.MM.dd");
        Calendar cal = new GregorianCalendar(2021, Calendar.JANUARY, 15);
        String formatted = fdf.format(cal.getTime());
        assertEquals("2021.01.15", formatted);
    }

    // Tests format Calendar with forced time zone
    @Test
    public void testFormat_calendar_withForcedTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("GMT+2");
        FastDateFormat fdf = FastDateFormat.getInstance("HH:mm", tz);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.set(2020, Calendar.JANUARY, 1, 10, 0, 0);
        String formatted = fdf.format(cal);
        assertEquals("12:00", formatted);
    }

    // Tests format long millis
    @Test
    public void testFormat_long_millis() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy");
        long millis = new GregorianCalendar(2019, Calendar.JANUARY, 1).getTimeInMillis();
        assertEquals("2019", fdf.format(millis));
    }

    // Tests pattern 'ZZ' (ISO 8601)
    @Test
    public void testFormat_patternZZ_returnsISO8601() {
        TimeZone tz = TimeZone.getTimeZone("GMT+5:30");
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ssZZ", tz);
        Calendar cal = new GregorianCalendar(tz);
        cal.set(2021, Calendar.JUNE, 1, 12, 0, 0);
        String result = fdf.format(cal.getTime());
        assertTrue("Should contain +05:30", result.contains("+05:30"));
    }

    // Tests pattern 'Z' (RFC 822)
    @Test
    public void testFormat_patternZ_returnsRFC822() {
        TimeZone tz = TimeZone.getTimeZone("GMT+5:30");
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ssZ", tz);
        Calendar cal = new GregorianCalendar(tz);
        cal.set(2021, Calendar.JUNE, 1, 12, 0, 0);
        String result = fdf.format(cal.getTime());
        assertTrue("Should contain +0530", result.contains("+0530"));
    }

    // Tests format Calendar without forced time zone uses calendar's time zone
    @Test
    public void testFormat_calendar_withoutTimeZoneForced() {
        TimeZone calTz = TimeZone.getTimeZone("GMT-4");
        FastDateFormat fdf = FastDateFormat.getInstance("HH:mm");
        Calendar cal = new GregorianCalendar(calTz);
        cal.set(2021, Calendar.JANUARY, 1, 8, 0, 0);
        String formatted = fdf.format(cal);
        assertEquals("08:00", formatted);
    }

    // Tests getTimeZoneOverridesCalendar returns true when time zone forced
    @Test
    public void testGetTimeZoneOverridesCalendar_returnsTrueWhenForced() {
        FastDateFormat fdf = FastDateFormat.getInstance("HH:mm", TimeZone.getTimeZone("GMT"));
        assertTrue(fdf.getTimeZoneOverridesCalendar());
    }

    // Tests getTimeZoneOverridesCalendar returns false when time zone not forced
    @Test
    public void testGetTimeZoneOverridesCalendar_returnsFalseWhenNotForced() {
        FastDateFormat fdf = FastDateFormat.getInstance("HH:mm");
        assertFalse(fdf.getTimeZoneOverridesCalendar());
    }

    // Tests equals with same instance
    @Test
    public void testEquals_sameInstance_returnsTrue() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("yyyyMMdd");
        FastDateFormat fdf2 = FastDateFormat.getInstance("yyyyMMdd");
        assertEquals(fdf1, fdf2);
    }

    // Tests equals with different patterns returns false
    @Test
    public void testEquals_differentPattern_returnsFalse() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("yyyy");
        FastDateFormat fdf2 = FastDateFormat.getInstance("MM");
        assertFalse(fdf1.equals(fdf2));
    }

    // Tests hashCode consistency
    @Test
    public void testHashCode_consistent() {
        FastDateFormat fdf = FastDateFormat.getInstance("dd/MM/yyyy");
        int h1 = fdf.hashCode();
        int h2 = fdf.hashCode();
        assertEquals(h1, h2);
    }

    // Tests getMaxLengthEstimate returns positive
    @Test
    public void testGetMaxLengthEstimate_positive() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
        assertTrue(fdf.getMaxLengthEstimate() > 0);
    }

    // Tests parseObject returns null
    @Test
    public void testParseObject_returnsNull() {
        FastDateFormat fdf = FastDateFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        assertNull(fdf.parseObject("", pos));
        assertEquals(0, pos.getIndex());
        assertEquals(0, pos.getErrorIndex());
    }

    // Tests format with year 0 (1 BC)
    @Test
    public void testFormat_yearZero() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy");
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.YEAR, 0);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        String result = fdf.format(cal.getTime());
        assertEquals("0000", result);
    }

    // Tests hour boundary (midnight in 24-hour format)
    @Test
    public void testFormat_hourBoundary() {
        FastDateFormat fdf = FastDateFormat.getInstance("H:mm");
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        assertEquals("0:00", fdf.format(cal.getTime()));
    }

    // Tests twelve-hour field with midnight converts to 12 AM
    @Test
    public void testFormat_twelveHourField() {
        FastDateFormat fdf = FastDateFormat.getInstance("h:mm a");
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.set(2020, Calendar.JANUARY, 1, 0, 30, 0);
        assertEquals("12:30 AM", fdf.format(cal.getTime()));
    }

    // ===== New tests for uncovered coverage =====

    @Test
    public void testGetInstance_withLocale_returnsLocale() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy", Locale.FRANCE);
        assertEquals(Locale.FRANCE, fdf.getLocale());
    }

    @Test
    public void testGetInstance_withTimeZoneAndLocale_returnsFields() {
        TimeZone tz = TimeZone.getTimeZone("GMT+7");
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy", tz, Locale.US);
        assertEquals(tz, fdf.getTimeZone());
        assertEquals(Locale.US, fdf.getLocale());
    }

    @Test
    public void testGetTimeZone_returnsDefaultWhenNotForced() {
        TimeZone tz = TimeZone.getTimeZone("GMT+2");
        TimeZone.setDefault(tz);
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy");
        assertEquals(tz, fdf.getTimeZone());
    }

    @Test
    public void testGetDateInstance_FULL_withTimeZoneAndLocale_returnsFormattedValue() {
        FastDateFormat fdf = FastDateFormat.getDateInstance(FastDateFormat.FULL, TimeZone.getTimeZone("GMT"), Locale.US);
        assertNotNull(fdf);
        assertNotNull(fdf.getPattern());
        assertNotNull(fdf.format(new Date(0)));
    }

    @Test
    public void testGetTimeInstance_LONG_withTimeZoneAndLocale_returnsFormattedValue() {
        FastDateFormat fdf = FastDateFormat.getTimeInstance(FastDateFormat.LONG, TimeZone.getTimeZone("GMT"), Locale.US);
        String result = fdf.format(new GregorianCalendar(TimeZone.getTimeZone("GMT")).getTime());
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetDateTimeInstance_MEDIUM_SHORT_withTimeZoneAndLocale_returnsFormattedValue() {
        FastDateFormat fdf = FastDateFormat.getDateTimeInstance(FastDateFormat.MEDIUM, FastDateFormat.SHORT,
                TimeZone.getTimeZone("GMT"), Locale.US);
        String result = fdf.format(new GregorianCalendar(TimeZone.getTimeZone("GMT")).getTime());
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testFormat_unpaddedMonthAndDay() {
        FastDateFormat fdf = FastDateFormat.getInstance("M/d/yyyy", TimeZone.getTimeZone("GMT"));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.set(2021, Calendar.JUNE, 5);
        assertEquals("6/5/2021", fdf.format(cal.getTime()));
    }

    @Test
    public void testFormat_twoDigitYear() {
        FastDateFormat fdf = FastDateFormat.getInstance("yy/MM/dd", TimeZone.getTimeZone("GMT"));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.set(2021, Calendar.JUNE, 5);
        assertEquals("21/06/05", fdf.format(cal.getTime()));
    }

    @Test
    public void testFormat_era() {
        FastDateFormat fdf = FastDateFormat.getInstance("G yyyy", TimeZone.getTimeZone("GMT"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.set(2021, Calendar.JUNE, 1);
        assertEquals("AD 2021", fdf.format(cal.getTime()));
    }

    @Test
    public void testFormat_dayOfWeekShort() {
        FastDateFormat fdf = FastDateFormat.getInstance("E", TimeZone.getTimeZone("GMT"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.set(2021, Calendar.JUNE, 1);
        assertEquals("Tue", fdf.format(cal.getTime()));
    }

    @Test
    public void testFormat_timeZoneShortName() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        FastDateFormat fdf = FastDateFormat.getInstance("z", tz, Locale.US);
        Calendar cal = new GregorianCalendar(tz, Locale.US);
        cal.set(2021, Calendar.JANUARY, 1, 12, 0, 0);
        assertEquals("EST", fdf.format(cal.getTime()));
    }

    @Test
    public void testFormat_timeZoneLongName() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        FastDateFormat fdf = FastDateFormat.getInstance("zzzz", tz, Locale.US);
        Calendar cal = new GregorianCalendar(tz, Locale.US);
        cal.set(2021, Calendar.JANUARY, 1, 12, 0, 0);
        assertEquals("Eastern Standard Time", fdf.format(cal.getTime()));
    }

    @Test
    public void testFormat_secondsAndMillis() {
        FastDateFormat fdf = FastDateFormat.getInstance("HH:mm:ss.SSS", TimeZone.getTimeZone("GMT"));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.set(2021, Calendar.JUNE, 1, 12, 34, 56);
        cal.set(Calendar.MILLISECOND, 789);
        assertEquals("12:34:56.789", fdf.format(cal.getTime()));
    }

    @Test
    public void testFormat_quotedLiteral() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy 'at' HH:mm", TimeZone.getTimeZone("GMT"));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.set(2021, Calendar.JUNE, 1, 9, 30, 0);
        assertEquals("2021 at 09:30", fdf.format(cal.getTime()));
    }

    @Test
    public void testFormat_quotedEscapedQuoteAndSingleCharLiteral() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy 'a' HH 'o''clock'", TimeZone.getTimeZone("GMT"));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.set(2021, Calendar.JUNE, 1, 9, 0, 0);
        assertEquals("2021 a 09 o'clock", fdf.format(cal.getTime()));
    }

    @Test
    public void testFormat_patternZ_negativeOffset() {
        TimeZone tz = TimeZone.getTimeZone("GMT-06:00");
        FastDateFormat fdf = FastDateFormat.getInstance("yyyyMMdd'T'HHmmssZ", tz);
        Calendar cal = new GregorianCalendar(tz);
        cal.set(2021, Calendar.JUNE, 1, 12, 0, 0);
        String result = fdf.format(cal.getTime());
        assertTrue(result.endsWith("-0600"));
    }

    @Test
    public void testFormat_patternZZ_negativeOffset() {
        TimeZone tz = TimeZone.getTimeZone("GMT-06:00");
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ssZZ", tz);
        Calendar cal = new GregorianCalendar(tz);
        cal.set(2021, Calendar.JUNE, 1, 12, 0, 0);
        String result = fdf.format(cal.getTime());
        assertTrue(result.endsWith("-06:00"));
    }

    @Test
    public void testFormat_timeZoneZeroOffset() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fdf = FastDateFormat.getInstance("Z ZZ", tz);
        Calendar cal = new GregorianCalendar(tz);
        cal.set(2021, Calendar.JUNE, 1, 12, 0, 0);
        String result = fdf.format(cal.getTime());
        assertTrue(result.contains("+0000"));
        assertTrue(result.contains("+00:00"));
    }

    @Test
    public void testFormat_dayOfYearAndWeekFields() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fdf = FastDateFormat.getInstance("D F w W k K", tz, Locale.US);
        Calendar cal = new GregorianCalendar(tz, Locale.US);
        cal.set(2021, Calendar.JUNE, 1, 0, 0, 0);
        String result = fdf.format(cal.getTime());
        assertNotNull(result);
        assertFalse(result.trim().isEmpty());
    }

    @Test
    public void testEquals_differentTimeZone_returnsFalse() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("HH:mm", TimeZone.getTimeZone("GMT"));
        FastDateFormat fdf2 = FastDateFormat.getInstance("HH:mm", TimeZone.getTimeZone("GMT+1"));
        assertFalse(fdf1.equals(fdf2));
    }

    @Test
    public void testEquals_differentLocale_returnsFalse() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("HH:mm", Locale.US);
        FastDateFormat fdf2 = FastDateFormat.getInstance("HH:mm", Locale.FRANCE);
        assertFalse(fdf1.equals(fdf2));
    }

    @Test
    public void testEquals_null_returnsFalse() {
        FastDateFormat fdf = FastDateFormat.getInstance("HH:mm");
        assertFalse(fdf.equals(null));
    }

    @Test
    public void testHashCode_equalObjects_haveSameHashCode() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("yyyyMMdd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat fdf2 = FastDateFormat.getInstance("yyyyMMdd", TimeZone.getTimeZone("GMT"), Locale.US);
        assertEquals(fdf1.hashCode(), fdf2.hashCode());
    }

    @Test
    public void testToString_containsPattern() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyyMMdd", TimeZone.getTimeZone("GMT"), Locale.US);
        assertTrue(fdf.toString().contains("yyyyMMdd"));
    }
}