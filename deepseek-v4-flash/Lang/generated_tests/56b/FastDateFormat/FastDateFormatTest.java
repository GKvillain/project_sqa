package org.apache.commons.lang.time;

import static org.junit.Assert.*;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

public class FastDateFormatTest {

    // ----- existing tests -----
    @Test
    public void testGetInstance_default_returnsNonNull() {
        FastDateFormat fmt = FastDateFormat.getInstance();
        assertNotNull(fmt);
        assertNotNull(fmt.getPattern());
    }

    @Test
    public void testFormatDate_withStandardPattern_matchesSimpleDateFormat() {
        Date date = new Date(0L);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("UTC"));
        assertEquals(sdf.format(date), fdf.format(date));
    }

    @Test
    public void testFormatCalendar_withForcedTimeZone_usesForcedZone() {
        TimeZone tz = TimeZone.getTimeZone("GMT+02:00");
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", tz);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+05:00"));
        cal.set(2020, 0, 1, 12, 0, 0);
        assertEquals("2020-01-01 09:00:00", fdf.format(cal));
    }

    @Test
    public void testPattern_Z_withNonIntegerHourOffset_correctOutput() {
        TimeZone tz = TimeZone.getTimeZone("GMT+05:30");
        FastDateFormat fdf = FastDateFormat.getInstance("Z", tz);
        Calendar cal = new GregorianCalendar(tz);
        cal.set(2020, 0, 1, 0, 0, 0);
        assertEquals("+0530", fdf.format(cal));
    }

    @Test
    public void testPattern_ZZ_withNonIntegerHourOffset_correctOutput() {
        TimeZone tz = TimeZone.getTimeZone("GMT+05:30");
        FastDateFormat fdf = FastDateFormat.getInstance("ZZ", tz);
        Calendar cal = new GregorianCalendar(tz);
        cal.set(2020, 0, 1, 0, 0, 0);
        assertEquals("+05:30", fdf.format(cal));
    }

    @Test
    public void testPattern_Z_withNegativeOffset_correctOutput() {
        TimeZone tz = TimeZone.getTimeZone("GMT-05:30");
        FastDateFormat fdf = FastDateFormat.getInstance("Z", tz);
        Calendar cal = new GregorianCalendar(tz);
        cal.set(2020, 0, 1, 0, 0, 0);
        assertEquals("-0530", fdf.format(cal));
    }

    @Test
    public void testPattern_ZZ_withNegativeOffset_correctOutput() {
        TimeZone tz = TimeZone.getTimeZone("GMT-05:30");
        FastDateFormat fdf = FastDateFormat.getInstance("ZZ", tz);
        Calendar cal = new GregorianCalendar(tz);
        cal.set(2020, 0, 1, 0, 0, 0);
        assertEquals("-05:30", fdf.format(cal));
    }

    @Test
    public void testFormatDate_withDST_usesDSTOffset() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        FastDateFormat fdf = FastDateFormat.getInstance("Z", tz);
        Calendar cal = new GregorianCalendar(tz);
        cal.set(2020, 6, 1, 12, 0, 0); // July, DST
        assertEquals("-0400", fdf.format(cal));
    }

    @Test
    public void testPattern_z_shortTimeZoneName() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        FastDateFormat fdf = FastDateFormat.getInstance("z", tz);
        Calendar cal = new GregorianCalendar(tz);
        cal.set(2020, 0, 1, 12, 0, 0); // standard time
        String expected = tz.getDisplayName(false, TimeZone.SHORT, Locale.getDefault());
        assertEquals(expected, fdf.format(cal));
    }

    @Test
    public void testPattern_k_withZeroHour_returns24() {
        FastDateFormat fdf = FastDateFormat.getInstance("k", TimeZone.getTimeZone("UTC"));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, 0, 1, 0, 0, 0);
        assertEquals("24", fdf.format(cal));
    }

    @Test
    public void testPattern_h_withZeroHour_returns12() {
        FastDateFormat fdf = FastDateFormat.getInstance("h", TimeZone.getTimeZone("UTC"));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, 0, 1, 0, 0, 0);
        assertEquals("12", fdf.format(cal));
    }

    @Test
    public void testPattern_yy_twoDigitYear_returnsLastTwoDigits() {
        FastDateFormat fdf = FastDateFormat.getInstance("yy", TimeZone.getTimeZone("UTC"));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2005, 0, 1);
        assertEquals("05", fdf.format(cal));
        cal.set(1999, 0, 1);
        assertEquals("99", fdf.format(cal));
    }

    @Test
    public void testPattern_literalText_includesLiteral() {
        FastDateFormat fdf = FastDateFormat.getInstance("'Today is 'dd MMM");
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, 0, 15);
        String result = fdf.format(cal);
        assertTrue(result.startsWith("Today is "));
    }

    @Test
    public void testPattern_MMM_shortMonthName() {
        FastDateFormat fdf = FastDateFormat.getInstance("MMM", Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, 0, 1);
        assertEquals("Jan", fdf.format(cal));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPattern_throwsIllegalArgumentException() {
        FastDateFormat.getInstance(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPattern_throwsIllegalArgumentException() {
        FastDateFormat.getInstance("X");
    }

    @Test
    public void testEquals_samePattern_returnsTrue() {
        FastDateFormat f1 = FastDateFormat.getInstance("yyyyMMdd");
        FastDateFormat f2 = FastDateFormat.getInstance("yyyyMMdd");
        assertEquals(f1, f2);
    }

    @Test
    public void testGetTimeZoneOverridesCalendar_forced() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyyMMdd", TimeZone.getDefault());
        assertTrue(fdf.getTimeZoneOverridesCalendar());
    }

    @Test
    public void testGetMaxLengthEstimate_returnsPositive() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyyMMdd");
        assertTrue(fdf.getMaxLengthEstimate() > 0);
    }

    @Test
    public void testParseObject_returnsNull() {
        FastDateFormat fdf = FastDateFormat.getInstance();
        assertNull(fdf.parseObject("", new ParsePosition(0)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatObject_withInvalidType_throwsIllegalArgumentException() {
        FastDateFormat fdf = FastDateFormat.getInstance();
        fdf.format("not a date", new StringBuffer(), null);
    }

    @Test
    public void testPattern_a_amPmMarker() {
        FastDateFormat fdf = FastDateFormat.getInstance("a", Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, 0, 1, 0, 0, 0);
        assertEquals("AM", fdf.format(cal));
        cal.set(2020, 0, 1, 12, 0, 0);
        assertEquals("PM", fdf.format(cal));
    }

    // ===== new tests to increase coverage =====

    @Test
    public void testFormatDate_yyyy_fourDigitYear() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy", TimeZone.getTimeZone("UTC"));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(1970, 0, 1);
        assertEquals("1970", fdf.format(cal));
        cal.set(2020, 0, 1);
        assertEquals("2020", fdf.format(cal));
    }

    @Test
    public void testFormatDate_MM_twoDigitMonth() {
        FastDateFormat fdf = FastDateFormat.getInstance("MM", TimeZone.getTimeZone("UTC"));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1);
        assertEquals("01", fdf.format(cal));
        cal.set(2020, Calendar.OCTOBER, 1);
        assertEquals("10", fdf.format(cal));
    }

    @Test
    public void testFormatDate_M_oneDigitMonth() {
        FastDateFormat fdf = FastDateFormat.getInstance("M", TimeZone.getTimeZone("UTC"));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1);
        assertEquals("1", fdf.format(cal));
        cal.set(2020, Calendar.OCTOBER, 1);
        assertEquals("10", fdf.format(cal));
    }

    @Test
    public void testFormatDate_dd_twoDigitDay() {
        FastDateFormat fdf = FastDateFormat.getInstance("dd", TimeZone.getTimeZone("UTC"));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 5);
        assertEquals("05", fdf.format(cal));
        cal.set(2020, Calendar.JANUARY, 15);
        assertEquals("15", fdf.format(cal));
    }

    @Test
    public void testFormatDate_d_oneDigitDay() {
        FastDateFormat fdf = FastDateFormat.getInstance("d", TimeZone.getTimeZone("UTC"));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 5);
        assertEquals("5", fdf.format(cal));
        cal.set(2020, Calendar.JANUARY, 15);
        assertEquals("15", fdf.format(cal));
    }

    @Test
    public void testFormatDate_D_dayOfYear() {
        FastDateFormat fdf = FastDateFormat.getInstance("D", TimeZone.getTimeZone("UTC"));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1);
        assertEquals("1", fdf.format(cal));
        cal.set(2020, Calendar.DECEMBER, 31);
        assertEquals("366", fdf.format(cal));  // 2020 is leap year
    }

    @Test
    public void testFormatDate_MMMM_fullMonthName() {
        FastDateFormat fdf = FastDateFormat.getInstance("MMMM", Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1);
        assertEquals("January", fdf.format(cal));
        cal.set(2020, Calendar.FEBRUARY, 1);
        assertEquals("February", fdf.format(cal));
    }

    @Test
    public void testFormatDate_EEE_shortDayName() {
        FastDateFormat fdf = FastDateFormat.getInstance("EEE", Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1); // Wednesday
        assertEquals("Wed", fdf.format(cal));
    }

    @Test
    public void testFormatDate_EEEE_fullDayName() {
        FastDateFormat fdf = FastDateFormat.getInstance("EEEE", Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1); // Wednesday
        assertEquals("Wednesday", fdf.format(cal));
    }

    @Test
    public void testFormatDate_S_millis() {
        FastDateFormat fdf = FastDateFormat.getInstance("S", TimeZone.getTimeZone("UTC"));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 123);
        assertEquals("123", fdf.format(cal));
        cal.set(Calendar.MILLISECOND, 9);
        assertEquals("9", fdf.format(cal));
    }

    @Test
    public void testFormatDate_escapedQuote() {
        FastDateFormat fdf = FastDateFormat.getInstance("'o''clock' a", Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        assertEquals("o'clock AM", fdf.format(cal));
    }

    @Test
    public void testFormatCalendar_withoutForcedZone_usesCalendarZone() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+05:00"));
        cal.set(2020, Calendar.JANUARY, 1, 12, 0, 0);
        assertEquals("2020-01-01 12:00:00", fdf.format(cal));
    }

    @Test
    public void testGetInstance_withLocale_returnsCorrectLocale() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyyMMdd", Locale.US);
        assertNotNull(fdf);
        assertEquals(Locale.US, fdf.getLocale());
    }

    @Test
    public void testGetInstance_withTimeZoneAndLocale() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDateFormat fdf = FastDateFormat.getInstance("yyyyMMdd", tz, Locale.US);
        assertNotNull(fdf);
        assertEquals(tz, fdf.getTimeZone());
        assertEquals(Locale.US, fdf.getLocale());
    }

    @Test
    public void testEquals_differentPattern_returnsFalse() {
        FastDateFormat f1 = FastDateFormat.getInstance("yyyyMMdd");
        FastDateFormat f2 = FastDateFormat.getInstance("yyyyMMddHHmmss");
        assertFalse(f1.equals(f2));
    }

    @Test
    public void testEquals_null_returnsFalse() {
        FastDateFormat f1 = FastDateFormat.getInstance("yyyyMMdd");
        assertFalse(f1.equals(null));
    }

    @Test
    public void testHashCode_equalObjects_equalHash() {
        FastDateFormat f1 = FastDateFormat.getInstance("yyyyMMdd");
        FastDateFormat f2 = FastDateFormat.getInstance("yyyyMMdd");
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    public void testToString_returnsPattern() {
        String pattern = "yyyyMMdd";
        FastDateFormat fdf = FastDateFormat.getInstance(pattern);
        assertEquals(pattern, fdf.toString());
    }

    @Test
    public void testFormatObject_Date_success() {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        FastDateFormat fdf = FastDateFormat.getInstance("yyyyMMdd", utc);
        Date date = new Date(0L); // 1970-01-01 UTC
        StringBuffer buffer = new StringBuffer();
        fdf.format(date, buffer, null);
        assertEquals("19700101", buffer.toString());
    }

    @Test
    public void testFormatObject_Calendar_success() {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        FastDateFormat fdf = FastDateFormat.getInstance("yyyyMMdd", utc);
        Calendar cal = new GregorianCalendar(utc);
        cal.set(2020, Calendar.JANUARY, 1);
        StringBuffer buffer = new StringBuffer();
        fdf.format(cal, buffer, null);
        assertEquals("20200101", buffer.toString());
    }

    @Test
    public void testGetTimeZoneOverridesCalendar_notForced() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyyMMdd");
        assertFalse(fdf.getTimeZoneOverridesCalendar());
    }

    @Test
    public void testGetTimeZone_returnsNullWhenNotSet() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyyMMdd");
        assertNull(fdf.getTimeZone());
    }

    @Test
    public void testFormatDate_H_hour0_23() {
        FastDateFormat fdf = FastDateFormat.getInstance("H", TimeZone.getTimeZone("UTC"));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        assertEquals("0", fdf.format(cal));
        cal.set(2020, Calendar.JANUARY, 1, 13, 0, 0);
        assertEquals("13", fdf.format(cal));
    }

    @Test
    public void testFormatDate_h_hour1_12() {
        FastDateFormat fdf = FastDateFormat.getInstance("h", TimeZone.getTimeZone("UTC"));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        assertEquals("12", fdf.format(cal));
        cal.set(2020, Calendar.JANUARY, 1, 13, 0, 0);
        assertEquals("1", fdf.format(cal));
    }

    @Test
    public void testFormatDate_K_hour0_11() {
        FastDateFormat fdf = FastDateFormat.getInstance("K", TimeZone.getTimeZone("UTC"));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        assertEquals("0", fdf.format(cal));
        cal.set(2020, Calendar.JANUARY, 1, 13, 0, 0);
        assertEquals("1", fdf.format(cal));
    }

    @Test
    public void testFormatDate_mm_minute() {
        FastDateFormat fdf = FastDateFormat.getInstance("mm", TimeZone.getTimeZone("UTC"));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1, 12, 5, 0);
        assertEquals("05", fdf.format(cal));
        cal.set(2020, Calendar.JANUARY, 1, 12, 45, 0);
        assertEquals("45", fdf.format(cal));
    }

    @Test
    public void testFormatDate_ss_second() {
        FastDateFormat fdf = FastDateFormat.getInstance("ss", TimeZone.getTimeZone("UTC"));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1, 12, 0, 5);
        assertEquals("05", fdf.format(cal));
        cal.set(2020, Calendar.JANUARY, 1, 12, 0, 30);
        assertEquals("30", fdf.format(cal));
    }

    @Test
    public void testFormatDate_Z_integerOffset() {
        // Positive integer offset
        TimeZone tzPlus2 = TimeZone.getTimeZone("GMT+02:00");
        FastDateFormat fdfZ = FastDateFormat.getInstance("Z", tzPlus2);
        Calendar cal = new GregorianCalendar(tzPlus2);
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        assertEquals("+0200", fdfZ.format(cal));

        // Negative integer offset
        TimeZone tzMinus2 = TimeZone.getTimeZone("GMT-02:00");
        FastDateFormat fdfZMinus = FastDateFormat.getInstance("Z", tzMinus2);
        Calendar calMinus = new GregorianCalendar(tzMinus2);
        calMinus.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        assertEquals("-0200", fdfZMinus.format(cal));
    }

    @Test
    public void testFormatDate_ZZ_integerOffset() {
        // Positive integer offset
        TimeZone tzPlus2 = TimeZone.getTimeZone("GMT+02:00");
        FastDateFormat fdfZZ = FastDateFormat.getInstance("ZZ", tzPlus2);
        Calendar cal = new GregorianCalendar(tzPlus2);
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        assertEquals("+02:00", fdfZZ.format(cal));

        // Negative integer offset
        TimeZone tzMinus2 = TimeZone.getTimeZone("GMT-02:00");
        FastDateFormat fdfZZMinus = FastDateFormat.getInstance("ZZ", tzMinus2);
        Calendar calMinus = new GregorianCalendar(tzMinus2);
        calMinus.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        assertEquals("-02:00", fdfZZMinus.format(cal));
    }

    @Test
    public void testFormatCalendar_winterDST() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        FastDateFormat fdf = FastDateFormat.getInstance("Z", tz);
        Calendar cal = new GregorianCalendar(tz);
        cal.set(2020, Calendar.JANUARY, 1, 12, 0, 0); // January, standard time -0500
        assertEquals("-0500", fdf.format(cal));
    }
}