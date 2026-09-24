package org.joda.time.format;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DateTimeFormatterBuilderTest {

    private DateTimeFormatterBuilder builder;

    @Before
    public void setUp() {
        builder = new DateTimeFormatterBuilder();
    }

    // Tests building empty formatter throws exception
    @Test(expected = UnsupportedOperationException.class)
    public void testToFormatter_emptyBuilder_throwsException() {
        assertFalse(builder.canBuildFormatter());
        assertFalse(builder.canBuildPrinter());
        assertFalse(builder.canBuildParser());
        builder.toFormatter();
    }

    // Tests appending null formatter throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAppend_nullFormatter_throwsException() {
        builder.append((DateTimeFormatter) null);
    }

    // Tests appendLiteral with char and string and clearing builder
    @Test
    public void testAppendLiteral_validCharacters_formatsAndParsesCorrectly() {
        builder.appendLiteral('T')
               .appendLiteral("---")
               .appendLiteral(""); // empty literal branch

        assertTrue(builder.canBuildFormatter());
        assertTrue(builder.canBuildPrinter());
        assertTrue(builder.canBuildParser());

        DateTimeFormatter formatter = builder.toFormatter();
        assertNotNull(formatter.getPrinter());
        assertNotNull(formatter.getParser());

        builder.clear();
        assertFalse(builder.canBuildFormatter());
    }

    // Tests appendLiteral null string throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAppendLiteral_nullString_throwsException() {
        builder.appendLiteral((String) null);
    }

    // Tests appendDecimal and appendSignedDecimal boundary and invalid parameters
    @Test(expected = IllegalArgumentException.class)
    public void testAppendDecimal_invalidDigits_throwsException() {
        builder.appendDecimal(DateTimeFieldType.year(), -1, 4);
    }

    // Tests null fieldType in appendDecimal throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testAppendDecimal_nullFieldType_throwsException() {
        builder.appendDecimal(null, 1, 2);
    }

    // Tests fixed decimal formatting and parsing
    @Test
    public void testAppendFixedDecimal_validInput_parsesAndPrintsCorrectly() {
        DateTimeFormatter formatter = builder.appendFixedDecimal(DateTimeFieldType.year(), 4)
                                             .appendLiteral('-')
                                             .appendFixedDecimal(DateTimeFieldType.monthOfYear(), 2)
                                             .toFormatter();

        DateTime dt = formatter.parseDateTime("2023-05");
        assertEquals(2023, dt.getYear());
        assertEquals(5, dt.getMonthOfYear());

        String printed = formatter.print(new LocalDateTime(2023, 5, 1, 0, 0));
        assertEquals("2023-05", printed);
    }

    // Tests fixed decimal with invalid digit count
    @Test(expected = IllegalArgumentException.class)
    public void testAppendFixedDecimal_invalidNumDigits_throwsException() {
        builder.appendFixedDecimal(DateTimeFieldType.year(), 0);
    }

    // Tests appendTwoDigitYear with pivot and lenient parsing
    @Test
    public void testAppendTwoDigitYear_pivotAndLenientParsing_parsesExpectedYears() {
        DateTimeFormatter formatter = builder.appendTwoDigitYear(2000, true)
                                             .toFormatter();

        assertEquals(1975, formatter.parseDateTime("75").getYear());
        assertEquals(2025, formatter.parseDateTime("25").getYear());
        assertEquals(1920, formatter.parseDateTime("1920").getYear());
        assertEquals(2050, formatter.parseDateTime("+2050").getYear());
    }

    // Tests appendTwoDigitWeekyear formatting and parsing
    @Test
    public void testAppendTwoDigitWeekyear_validInput_parsesCorrectly() {
        DateTimeFormatter formatter = builder.appendTwoDigitWeekyear(2000)
                                             .toFormatter();

        assertEquals(2005, formatter.parseDateTime("05").getWeekyear());
    }

    // Tests text field formatting and parsing with locale
    @Test
    public void testAppendText_monthAndEra_formatsAndParsesCorrectly() {
        DateTimeFormatter formatter = builder.appendMonthOfYearText()
                                             .appendLiteral(' ')
                                             .appendYear(4, 4)
                                             .appendLiteral(' ')
                                             .appendEraText()
                                             .toFormatter()
                                             .withLocale(Locale.ENGLISH)
                                             .withZoneUTC();

        DateTime dt = formatter.parseDateTime("January 2021 AD");
        assertEquals(1, dt.getMonthOfYear());
        assertEquals(2021, dt.getYear());

        String printed = formatter.print(dt);
        assertEquals("January 2021 AD", printed);
    }

    // Tests appendFraction formatting and parsing
    @Test
    public void testAppendFractionOfSecond_validInput_printsAndParsesProperly() {
        DateTimeFormatter formatter = builder.appendSecondOfMinute(2)
                                             .appendLiteral('.')
                                             .appendFractionOfSecond(1, 3)
                                             .toFormatter()
                                             .withZoneUTC();

        DateTime dt = formatter.parseDateTime("15.500");
        assertEquals(15, dt.getSecondOfMinute());
        assertEquals(500, dt.getMillisOfSecond());

        DateTime dt2 = formatter.parseDateTime("15.05");
        assertEquals(15, dt2.getSecondOfMinute());
        assertEquals(50, dt2.getMillisOfSecond());
    }

    // Tests appendTimeZoneOffset with zero offset text and separators
    @Test
    public void testAppendTimeZoneOffset_zeroOffsetAndSeparators_formatsAndParses() {
        DateTimeFormatter formatter = builder.appendHourOfDay(2)
                                             .appendLiteral(':')
                                             .appendMinuteOfHour(2)
                                             .appendTimeZoneOffset("Z", true, 2, 2)
                                             .toFormatter();

        DateTime dtUtc = formatter.parseDateTime("12:30Z");
        assertEquals(DateTimeZone.UTC, dtUtc.getZone());

        DateTime dtOffset = formatter.parseDateTime("12:30+02:00");
        assertEquals(DateTimeZone.forOffsetHours(2), dtOffset.getZone());

        String printedUtc = formatter.print(new DateTime(2020, 1, 1, 12, 30, DateTimeZone.UTC));
        assertEquals("12:30Z", printedUtc);

        String printedOffset = formatter.print(new DateTime(2020, 1, 1, 12, 30, DateTimeZone.forOffsetHours(2)));
        assertEquals("12:30+02:00", printedOffset);
    }

    // Tests appendTimeZoneName with lookup map
    @Test
    public void testAppendTimeZoneName_withLookupMap_parsesCorrectZone() {
        Map<String, DateTimeZone> lookup = new LinkedHashMap<String, DateTimeZone>();
        lookup.put("GMT", DateTimeZone.UTC);
        lookup.put("EST", DateTimeZone.forOffsetHours(-5));

        DateTimeFormatter formatter = builder.appendHourOfDay(2)
                                             .appendLiteral(' ')
                                             .appendTimeZoneName(lookup)
                                             .toFormatter();

        DateTime dt = formatter.parseDateTime("15 EST");
        assertEquals(DateTimeZone.forOffsetHours(-5), dt.getZone());
    }

    // Tests appendTimeZoneId matching longer prefix over shorter prefix (Defects4J Time-20)
    @Test
    public void testAppendTimeZoneId_overlappingPrefixes_parsesLongestMatchingId() {
        DateTimeFormatter formatter = builder.appendTimeZoneId()
                                             .toFormatter();

        DateTime dt = formatter.parseDateTime("America/Dawson_Creek");
        assertEquals(DateTimeZone.forID("America/Dawson_Creek"), dt.getZone());

        DateTime dtDawson = formatter.parseDateTime("America/Dawson");
        assertEquals(DateTimeZone.forID("America/Dawson"), dtDawson.getZone());
    }

    // Tests appendOptional and MatchingParser fallback behaviour
    @Test
    public void testAppendOptional_matchingParser_parsesBothPresentAndAbsentOptional() {
        DateTimeParser optionalParser = new DateTimeFormatterBuilder()
                .appendLiteral('-')
                .appendMonthOfYear(2)
                .toParser();

        DateTimeFormatter formatter = builder.appendYear(4, 4)
                                             .appendOptional(optionalParser)
                                             .toFormatter()
                                             .withZoneUTC();

        DateTime dtWithMonth = formatter.parseDateTime("2022-08");
        assertEquals(2022, dtWithMonth.getYear());
        assertEquals(8, dtWithMonth.getMonthOfYear());

        DateTime dtWithoutMonth = formatter.parseDateTime("2022");
        assertEquals(2022, dtWithoutMonth.getYear());
    }

    // Tests append with array of parsers
    @Test
    public void testAppend_multipleParsers_matchesFirstSuccessful() {
        DateTimeParser p1 = new DateTimeFormatterBuilder().appendLiteral('A').toParser();
        DateTimeParser p2 = new DateTimeFormatterBuilder().appendLiteral('B').toParser();

        DateTimeFormatter formatter = builder.append(null, new DateTimeParser[]{p1, p2})
                                             .appendYear(4, 4)
                                             .toFormatter()
                                             .withZoneUTC();

        DateTime dtA = formatter.parseDateTime("A2020");
        assertEquals(2020, dtA.getYear());

        DateTime dtB = formatter.parseDateTime("B2021");
        assertEquals(2021, dtB.getYear());
    }

    // Tests appendPattern integration from DateTimeFormat
    @Test
    public void testAppendPattern_standardPattern_formatsAndParsesCorrectly() {
        DateTimeFormatter formatter = builder.appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                                             .toFormatter()
                                             .withZoneUTC();

        DateTime dt = formatter.parseDateTime("2023-11-20T14:45:30");
        assertEquals(2023, dt.getYear());
        assertEquals(11, dt.getMonthOfYear());
        assertEquals(20, dt.getDayOfMonth());
        assertEquals(14, dt.getHourOfDay());
        assertEquals(45, dt.getMinuteOfHour());
        assertEquals(30, dt.getSecondOfMinute());
    }

    // Tests Composite printTo with Writer
    @Test
    public void testComposite_printToWriter_writesExpectedCharacters() throws IOException {
        DateTimeFormatter formatter = builder.appendYear(4, 4)
                                             .appendLiteral('/')
                                             .appendMonthOfYear(2)
                                             .toFormatter();

        CharArrayWriter writer = new CharArrayWriter();
        formatter.printTo(writer, new LocalDateTime(2025, 12, 1, 0, 0));
        assertEquals("2025/12", writer.toString());
    }
}