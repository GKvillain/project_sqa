package org.jfree.data.time;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class WeekTest {

    private static final TimeZone ZONE_UTC = TimeZone.getTimeZone("UTC");
    private static final Locale LOCALE_US = Locale.US;
    private static final Locale LOCALE_UK = Locale.UK;

    // ----- Constructor Week(int, int) -----

    @Test
    public void testWeekIntInt_validInput_createsCorrectWeek() {
        Week w = new Week(1, 2000);
        assertEquals(1, w.getWeek());
        assertEquals(2000, w.getYearValue());
    }

    // Defect: condition uses && instead of ||, so no exception thrown for invalid week
    @Test(expected = IllegalArgumentException.class)
    public void testWeekIntInt_weekBelow1_throwsIllegalArgumentException() {
        new Week(0, 2000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWeekIntInt_weekAbove53_throwsIllegalArgumentException() {
        new Week(54, 2000);
    }

    // ----- Constructor Week(int, Year) -----

    @Test
    public void testWeekIntYear_validInput_createsCorrectWeek() {
        Week w = new Week(1, new Year(2000));
        assertEquals(1, w.getWeek());
        assertEquals(2000, w.getYearValue());
    }

    // ----- Constructor Week(Date, TimeZone, Locale) -----

    // Boundary: 31 Dec 2001 in US locale -> week 1 of 2002
    @Test
    public void testWeekDate_dec31_2001_usLocale_returnsWeek1Of2002() {
        Calendar cal = Calendar.getInstance(ZONE_UTC, LOCALE_US);
        cal.set(2001, Calendar.DECEMBER, 31, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Week w = new Week(cal.getTime(), ZONE_UTC, LOCALE_US);
        assertEquals(1, w.getWeek());
        assertEquals(2002, w.getYearValue());
    }

    // Boundary: 1 Jan 2002 in US locale -> week 1 of 2002
    @Test
    public void testWeekDate_jan1_2002_usLocale_returnsWeek1Of2002() {
        Calendar cal = Calendar.getInstance(ZONE_UTC, LOCALE_US);
        cal.set(2002, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Week w = new Week(cal.getTime(), ZONE_UTC, LOCALE_US);
        assertEquals(1, w.getWeek());
        assertEquals(2002, w.getYearValue());
    }

    // Boundary: 1 Jan 2000 in UK locale (ISO) -> week 52 of 1999
    @Test
    public void testWeekDate_jan1_2000_ukLocale_returnsWeek52Of1999() {
        Calendar cal = Calendar.getInstance(ZONE_UTC, LOCALE_UK);
        cal.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Week w = new Week(cal.getTime(), ZONE_UTC, LOCALE_UK);
        assertEquals(52, w.getWeek());
        assertEquals(1999, w.getYearValue());
    }

    // Boundary: 31 Dec 2001 in UK locale -> week 1 of 2002
    @Test
    public void testWeekDate_dec31_2001_ukLocale_returnsWeek1Of2002() {
        Calendar cal = Calendar.getInstance(ZONE_UTC, LOCALE_UK);
        cal.set(2001, Calendar.DECEMBER, 31, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Week w = new Week(cal.getTime(), ZONE_UTC, LOCALE_UK);
        assertEquals(1, w.getWeek());
        assertEquals(2002, w.getYearValue());
    }

    // Null arguments
    @Test(expected = IllegalArgumentException.class)
    public void testWeekDate_nullTime_throwsIllegalArgumentException() {
        new Week(null, ZONE_UTC, LOCALE_US);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWeekDate_nullZone_throwsIllegalArgumentException() {
        new Week(new Date(), null, LOCALE_US);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWeekDate_nullLocale_throwsIllegalArgumentException() {
        new Week(new Date(), ZONE_UTC, null);
    }

    // ----- Method previous() -----

    @Test
    public void testPrevious_week1_2000_returnsLastWeekOf1999() {
        Week w = new Week(1, 2000);
        Week prev = (Week) w.previous();
        assertNotNull(prev);
        assertEquals(1999, prev.getYearValue());
        assertTrue(prev.getWeek() == 52 || prev.getWeek() == 53);
    }

    @Test
    public void testPrevious_week1_1900_returnsNull() {
        Week w = new Week(1, 1900);
        assertNull(w.previous());
    }

    // ----- Method next() -----

    @Test
    public void testNext_week52_2000_returnsNextWeekInSameYear() {
        Week w = new Week(52, 2000);
        Week next = (Week) w.next();
        assertNotNull(next);
        assertEquals(2000, next.getYearValue());
        assertTrue(next.getWeek() == 53 || next.getWeek() == 52);
    }

    @Test
    public void testNext_week53_2000_returnsWeek1Of2001() {
        Week w = new Week(53, 2000);
        Week next = (Week) w.next();
        assertNotNull(next);
        assertEquals(1, next.getWeek());
        assertEquals(2001, next.getYearValue());
    }

    @Test
    public void testNext_week53_9999_returnsNull() {
        Week w = new Week(53, 9999);
        assertNull(w.next());
    }

    // ----- Method getSerialIndex() -----

    @Test
    public void testGetSerialIndex_returnsCorrectValue() {
        Week w = new Week(1, 2000);
        assertEquals(2000 * 53L + 1, w.getSerialIndex());
    }

    // ----- Method equals() -----

    @Test
    public void testEquals_sameWeek_returnsTrue() {
        Week w1 = new Week(1, 2000);
        Week w2 = new Week(1, 2000);
        assertTrue(w1.equals(w2));
    }

    @Test
    public void testEquals_differentWeek_returnsFalse() {
        Week w1 = new Week(1, 2000);
        Week w2 = new Week(2, 2000);
        assertFalse(w1.equals(w2));
    }

    @Test
    public void testEquals_null_returnsFalse() {
        Week w = new Week(1, 2000);
        assertFalse(w.equals(null));
    }

    @Test
    public void testEquals_otherType_returnsFalse() {
        Week w = new Week(1, 2000);
        assertFalse(w.equals(""));
    }

    // ----- Method hashCode() -----

    @Test
    public void testHashCode_equalObjects_haveSameHashCode() {
        Week w1 = new Week(1, 2000);
        Week w2 = new Week(1, 2000);
        assertEquals(w1.hashCode(), w2.hashCode());
    }

    // ----- Method compareTo() -----

    @Test
    public void testCompareTo_sameWeek_returnsZero() {
        Week w1 = new Week(1, 2000);
        Week w2 = new Week(1, 2000);
        assertEquals(0, w1.compareTo(w2));
    }

    @Test
    public void testCompareTo_earlierWeek_returnsNegative() {
        Week w1 = new Week(1, 2000);
        Week w2 = new Week(2, 2000);
        assertTrue(w1.compareTo(w2) < 0);
    }

    @Test
    public void testCompareTo_laterWeek_returnsPositive() {
        Week w1 = new Week(2, 2000);
        Week w2 = new Week(1, 2000);
        assertTrue(w1.compareTo(w2) > 0);
    }

    // ----- Method toString() -----

    @Test
    public void testToString_returnsFormattedString() {
        Week w = new Week(1, 2000);
        assertEquals("Week 1, 2000", w.toString());
    }

    // ----- Method parseWeek() -----

    @Test
    public void testParseWeek_validInput_returnsWeek() {
        Week w = Week.parseWeek("2001-W01");
        assertNotNull(w);
        assertEquals(1, w.getWeek());
        assertEquals(2001, w.getYearValue());
    }

    @Test
    public void testParseWeek_alternateFormat_returnsWeek() {
        Week w = Week.parseWeek("W01-2001");
        assertNotNull(w);
        assertEquals(1, w.getWeek());
        assertEquals(2001, w.getYearValue());
    }

    @Test
    public void testParseWeek_nullInput_returnsNull() {
        assertNull(Week.parseWeek(null));
    }

    @Test(expected = TimePeriodFormatException.class)
    public void testParseWeek_invalidString_throwsTimePeriodFormatException() {
        Week.parseWeek("invalid");
    }

    // ----- Method getFirstMillisecond() / getLastMillisecond() (basic sanity) -----

    @Test
    public void testGetFirstMillisecond_lessThanLastMillisecond() {
        Week w = new Week(1, 2000);
        assertTrue(w.getFirstMillisecond() < w.getLastMillisecond());
    }
}