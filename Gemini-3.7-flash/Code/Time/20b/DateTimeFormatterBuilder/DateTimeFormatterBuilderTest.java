package org.joda.time.format;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DateTimeFormatterBuilderTest {

    private DateTimeFormatterBuilder builder;

    @Before
    public void setUp() {
        builder = new DateTimeFormatterBuilder();
    }

    // Tests defect Time-20: TimeZoneId parsing should match longest valid ID (e.g. America/Dawson_Creek over America/Dawson)
    @Test
    public void testAppendTimeZoneId_parsingLongestPrefixMatch_setsCorrectZone() {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendYear(4, 4)
            .appendLiteral(' ')
            .appendTimeZoneId()
            .toFormatter();

        DateTime dt = formatter.parseDateTime("2007 America/Dawson_Creek");
        assertEquals(DateTimeZone.forID("America/Dawson_Creek"), dt.getZone());
    }

    // Tests basic literal printing and parsing
    @Test
    public void testAppendLiteral_charAndString_formatsAndParsesCorrectly() {
        DateTimeFormatter formatter = builder
            .appendLiteral("Year:")
            .appendLiteral(' ')
            .appendYear(4, 4)
            .toFormatter();

        DateTime dt = formatter.parseDateTime("Year: 2021");
        assertEquals(2021, dt.getYear());

        String printed = formatter.print(dt);
        assertEquals("Year: 2021", printed);
    }

    // Tests empty string literal appending returns same builder without modification
    @Test
    public void testAppendLiteral_emptyString_builderUnchanged() {
        DateTimeFormatter formatter = builder
            .appendLiteral("")
            .appendYear(4, 4)
            .toFormatter();

        DateTime dt = formatter.parseDateTime("2020");
        assertEquals(2020, dt.getYear());
    }

    // Tests exception on null literal
    @Test(expected = IllegalArgumentException.class)
    public void testAppendLiteral_nullString_throwsIllegalArgumentException() {
        builder.appendLiteral((String) null);
    }

    // Tests fixed and signed decimal formatting/parsing
    @Test
    public void testAppendFixedDecimalAndSignedDecimal_validInputs_parsesValues() {
        DateTimeFormatter formatter = builder
            .appendSignedDecimal(DateTimeFieldType.year(), 4, 4)
            .appendLiteral('-')
            .appendFixedDecimal(DateTimeFieldType.monthOfYear(), 2)
            .appendLiteral('-')
            .appendFixedDecimal(DateTimeFieldType.dayOfMonth(), 2)
            .toFormatter();

        DateTime dt = formatter.parseDateTime("2023-05-19");
        assertEquals(2023, dt.getYear());
        assertEquals(5, dt.getMonthOfYear());
        assertEquals(19, dt.getDayOfMonth());
    }

    // Tests invalid arguments for fixed decimal methods
    @Test(expected = IllegalArgumentException.class)
    public void testAppendFixedDecimal_zeroDigits_throwsIllegalArgumentException() {
        builder.appendFixedDecimal(DateTimeFieldType.year(), 0);
    }

    // Tests two-digit year parsing with pivot year
    @Test
    public void testAppendTwoDigitYear_pivotCentury_parsesCorrectly() {
        DateTimeFormatter formatter = builder
            .appendTwoDigitYear(2000, false)
            .toFormatter();

        DateTime dt1 = formatter.parseDateTime("99");
        assertEquals(1999, dt1.getYear());

        DateTime dt2 = formatter.parseDateTime("05");
        assertEquals(2005, dt2.getYear());
    }

    // Tests text printing and parsing (long and short)
    @Test
    public void testAppendTextAndShortText_monthAndEra_formatsAndParses() {
        DateTimeFormatter formatter = builder
            .appendMonthOfYearText()
            .appendLiteral(' ')
            .appendYear(4, 4)
            .appendLiteral(' ')
            .appendEraText()
            .toFormatter()
            .withLocale(Locale.ENGLISH);

        DateTime dt = formatter.parseDateTime("December 2015 AD");
        assertEquals(12, dt.getMonthOfYear());
        assertEquals(2015, dt.getYear());
    }

    // Tests fraction of second printing and parsing
    @Test
    public void testAppendFractionOfSecond_validFraction_printsAndParses() {
        DateTimeFormatter formatter = builder
            .appendSecondOfMinute(2)
            .appendLiteral('.')
            .appendFractionOfSecond(1, 3)
            .toFormatter();

        DateTime dt = formatter.parseDateTime("15.450");
        assertEquals(15, dt.getSecondOfMinute());
        assertEquals(450, dt.getMillisOfSecond());
    }

    // Tests time zone offset printing and parsing with separators and zero offset text
    @Test
    public void testAppendTimeZoneOffset_customZeroOffset_printsAndParses() {
        DateTimeFormatter formatter = builder
            .appendHourOfDay(2)
            .appendLiteral(':')
            .appendMinuteOfHour(2)
            .appendTimeZoneOffset("Z", true, 2, 2)
            .toFormatter();

        DateTime dt1 = formatter.parseDateTime("12:00Z");
        assertEquals(0, dt1.getZone().getOffset(0L));

        DateTime dt2 = formatter.parseDateTime("12:00+02:00");
        assertEquals(2 * 3600 * 1000, dt2.getZone().getOffset(0L));
    }

    // Tests time zone name with custom lookup map
    @Test
    public void testAppendTimeZoneName_withLookupMap_parsesZone() {
        Map<String, DateTimeZone> lookup = new HashMap<String, DateTimeZone>();
        lookup.put("GMT", DateTimeZone.UTC);
        lookup.put("EST", DateTimeZone.forOffsetHours(-5));

        DateTimeFormatter formatter = builder
            .appendHourOfDay(2)
            .appendLiteral(' ')
            .appendTimeZoneName(lookup)
            .toFormatter();

        DateTime dt = formatter.parseDateTime("10 EST");
        assertEquals(DateTimeZone.forOffsetHours(-5), dt.getZone());
    }

    // Tests append optional parser behavior
    @Test
    public void testAppendOptional_matchingAndNonMatching_parsesProperly() {
        DateTimeParser optionalParser = new DateTimeFormatterBuilder()
            .appendLiteral('-')
            .appendMonthOfYear(2)
            .toParser();

        DateTimeFormatter formatter = builder
            .appendYear(4, 4)
            .appendOptional(optionalParser)
            .toFormatter();

        DateTime dt1 = formatter.parseDateTime("2020-08");
        assertEquals(2020, dt1.getYear());
        assertEquals(8, dt1.getMonthOfYear());

        DateTime dt2 = formatter.parseDateTime("2020");
        assertEquals(2020, dt2.getYear());
    }

    // Tests appending pattern via appendPattern
    @Test
    public void testAppendPattern_standardPattern_formatsAndParses() {
        DateTimeFormatter formatter = builder
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .toFormatter();

        DateTime dt = formatter.parseDateTime("2022-11-20 14:30:45");
        assertEquals(2022, dt.getYear());
        assertEquals(11, dt.getMonthOfYear());
        assertEquals(20, dt.getDayOfMonth());
        assertEquals(14, dt.getHourOfDay());
        assertEquals(30, dt.getMinuteOfHour());
        assertEquals(45, dt.getSecondOfMinute());
    }

    // Tests builder state inquiry and clear functionality
    @Test
    public void testBuilderState_canBuildAndClear_updatesState() {
        assertFalse(builder.canBuildFormatter());
        assertFalse(builder.canBuildPrinter());
        assertFalse(builder.canBuildParser());

        builder.appendYear(4, 4);
        assertTrue(builder.canBuildFormatter());
        assertTrue(builder.canBuildPrinter());
        assertTrue(builder.canBuildParser());

        builder.clear();
        assertFalse(builder.canBuildFormatter());
    }

    // Tests exception when toFormatter is called on empty builder
    @Test(expected = UnsupportedOperationException.class)
    public void testToFormatter_emptyBuilder_throwsUnsupportedOperationException() {
        builder.toFormatter();
    }

    // Tests append with null formatter argument
    @Test(expected = IllegalArgumentException.class)
    public void testAppend_nullFormatter_throwsIllegalArgumentException() {
        builder.append((DateTimeFormatter) null);
    }

    // Tests printer writing to Writer interface
    @Test
    public void testPrintToWriter_compositePrinter_writesExpectedOutput() throws IOException {
        DateTimeFormatter formatter = builder
            .appendYear(4, 4)
            .appendLiteral('/')
            .appendMonthOfYear(2)
            .toFormatter();

        DateTime dt = new DateTime(2021, 6, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        StringWriter writer = new StringWriter();
        formatter.getPrinter().printTo(writer, dt.getMillis(), null, 0, DateTimeZone.UTC, Locale.US);
        assertEquals("2021/06", writer.toString());
    }
}