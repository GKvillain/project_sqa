package org.joda.time.format;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

import org.joda.time.MutablePeriod;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.junit.Before;
import org.junit.Test;

public class PeriodFormatterBuilderTest {

    private PeriodFormatterBuilder builder;

    @Before
    public void setUp() {
        builder = new PeriodFormatterBuilder();
    }

    // Tests defect reproduction: separator before fields with appendSeparatorIfFieldsAfter
    @Test
    public void testAppendSeparatorIfFieldsAfter_leadingSeparator_formatsCorrectly() {
        PeriodFormatter formatter = builder
                .appendYears().appendSuffix("Y")
                .appendSeparatorIfFieldsAfter("T")
                .appendHours().appendSuffix("H")
                .toFormatter();

        Period period = new Period(0, 0, 0, 0, 4, 0, 0, 0);
        assertEquals("T4H", formatter.print(period));

        Period periodWithYears = new Period(2, 0, 0, 0, 4, 0, 0, 0);
        assertEquals("2YT4H", formatter.print(periodWithYears));

        Period periodYearsOnly = new Period(2, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("2Y", formatter.print(periodYearsOnly));

        MutablePeriod parsed = new MutablePeriod();
        int pos = formatter.getParser().parseInto(parsed, "T4H", 0, Locale.ENGLISH);
        assertEquals(3, pos);
        assertEquals(4, parsed.getHours());
        assertEquals(0, parsed.getYears());
    }

    // Tests standard build, print, and parse with multiple fields and suffixes
    @Test
    public void testAppendFields_standardValues_printsAndParses() {
        PeriodFormatter formatter = builder
                .appendYears().appendSuffix(" year", " years")
                .appendSeparator(", ")
                .appendMonths().appendSuffix(" month", " months")
                .appendSeparator(", ")
                .appendWeeks().appendSuffix(" week", " weeks")
                .appendSeparator(", ")
                .appendDays().appendSuffix(" day", " days")
                .toFormatter();

        Period p1 = new Period(1, 2, 1, 4, 0, 0, 0, 0);
        assertEquals("1 year, 2 months, 1 week, 4 days", formatter.print(p1));

        Period parsed = formatter.parsePeriod("1 year, 2 months, 1 week, 4 days");
        assertEquals(1, parsed.getYears());
        assertEquals(2, parsed.getMonths());
        assertEquals(1, parsed.getWeeks());
        assertEquals(4, parsed.getDays());
    }

    // Tests seconds with millis and optional millis
    @Test
    public void testAppendSecondsWithMillis_andOptionalMillis() {
        PeriodFormatter formatterMillis = new PeriodFormatterBuilder()
                .appendSecondsWithMillis()
                .toFormatter();

        Period p1 = new Period(0, 0, 0, 0, 0, 0, 5, 25);
        assertEquals("5.025", formatterMillis.print(p1));

        Period pZero = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0.000", formatterMillis.print(pZero));

        PeriodFormatter formatterOptional = new PeriodFormatterBuilder()
                .appendSecondsWithOptionalMillis()
                .toFormatter();

        assertEquals("5.025", formatterOptional.print(p1));
        Period pWholeSec = new Period(0, 0, 0, 0, 0, 0, 5, 0);
        assertEquals("5", formatterOptional.print(pWholeSec));

        MutablePeriod parsed = new MutablePeriod();
        formatterMillis.getParser().parseInto(parsed, "12.345", 0, Locale.ENGLISH);
        assertEquals(12, parsed.getSeconds());
        assertEquals(345, parsed.getMillis());
    }

    // Tests printZero settings (printZeroAlways, printZeroNever, printZeroIfSupported)
    @Test
    public void testPrintZeroSettings_variousModes() {
        PeriodFormatter alwaysFormatter = builder
                .printZeroAlways()
                .appendHours().appendSuffix("H")
                .appendMinutes().appendSuffix("M")
                .toFormatter();

        Period pZero = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0H0M", alwaysFormatter.print(pZero));

        PeriodFormatter neverFormatter = new PeriodFormatterBuilder()
                .printZeroNever()
                .appendHours().appendSuffix("H")
                .appendMinutes().appendSuffix("M")
                .toFormatter();

        assertEquals("", neverFormatter.print(pZero));

        PeriodFormatter ifSupportedFormatter = new PeriodFormatterBuilder()
                .printZeroIfSupported()
                .appendHours().appendSuffix("H")
                .toFormatter();

        assertEquals("0H", ifSupportedFormatter.print(new Period(0, PeriodType.hours())));
    }

    // Tests printZeroRarelyFirst and printZeroRarelyLast behavior on zero period
    @Test
    public void testPrintZeroRarely_firstAndLast() {
        PeriodFormatter rarelyFirst = new PeriodFormatterBuilder()
                .printZeroRarelyFirst()
                .appendHours().appendSuffix("H")
                .appendMinutes().appendSuffix("M")
                .toFormatter();

        Period zero = Period.ZERO;
        assertEquals("0H", rarelyFirst.print(zero));

        PeriodFormatter rarelyLast = new PeriodFormatterBuilder()
                .printZeroRarelyLast()
                .appendHours().appendSuffix("H")
                .appendMinutes().appendSuffix("M")
                .toFormatter();

        assertEquals("0M", rarelyLast.print(zero));
    }

    // Tests prefixes, composite prefixes, and composite suffixes
    @Test
    public void testPrefixAndSuffix_compositeAndPlural() {
        PeriodFormatter formatter = builder
                .appendPrefix("Time: ")
                .appendPrefix("approx. ")
                .appendHours()
                .appendSuffix(" hr", " hrs")
                .toFormatter();

        Period p1 = new Period(0, 0, 0, 0, 1, 0, 0, 0);
        assertEquals("Time: approx. 1 hr", formatter.print(p1));

        Period p2 = new Period(0, 0, 0, 0, 5, 0, 0, 0);
        assertEquals("Time: approx. 5 hrs", formatter.print(p2));

        Period parsed = formatter.parsePeriod("Time: approx. 3 hrs");
        assertEquals(3, parsed.getHours());
    }

    // Tests digit padding and maximum parsed digit controls
    @Test
    public void testDigitsFormattingAndParsingControls() {
        PeriodFormatter formatter = builder
                .minimumPrintedDigits(3)
                .maximumParsedDigits(2)
                .appendHours()
                .toFormatter();

        Period p = new Period(0, 0, 0, 0, 5, 0, 0, 0);
        assertEquals("005", formatter.print(p));

        MutablePeriod parsed = new MutablePeriod();
        int endPos = formatter.getParser().parseInto(parsed, "1234", 0, Locale.ENGLISH);
        assertEquals(2, endPos);
        assertEquals(12, parsed.getHours());
    }

    // Tests rejectSignedValues rejecting positive and negative signs
    @Test
    public void testRejectSignedValues_rejectsSigns() {
        PeriodFormatter formatter = builder
                .rejectSignedValues(true)
                .appendHours()
                .toFormatter();

        MutablePeriod parsed = new MutablePeriod();
        int posNegative = formatter.getParser().parseInto(parsed, "-5", 0, Locale.ENGLISH);
        assertTrue(posNegative < 0);

        int posPositive = formatter.getParser().parseInto(parsed, "+5", 0, Locale.ENGLISH);
        assertTrue(posPositive < 0);
    }

    // Tests multiple separators with final separator and variants
    @Test
    public void testAppendSeparator_withFinalTextAndVariants() {
        PeriodFormatter formatter = builder
                .appendDays().appendSuffix("d")
                .appendSeparator(", ", " and ", new String[] { " & " })
                .appendHours().appendSuffix("h")
                .appendSeparator(", ", " and ", new String[] { " & " })
                .appendMinutes().appendSuffix("m")
                .toFormatter();

        Period p2 = new Period(0, 0, 0, 1, 2, 0, 0, 0);
        assertEquals("1d and 2h", formatter.print(p2));

        Period p3 = new Period(0, 0, 0, 1, 2, 3, 0, 0);
        assertEquals("1d, 2h and 3m", formatter.print(p3));

        Period parsedAmpersand = formatter.parsePeriod("1d & 2h");
        assertEquals(1, parsedAmpersand.getDays());
        assertEquals(2, parsedAmpersand.getHours());
    }

    // Tests appendSeparatorIfFieldsBefore
    @Test
    public void testAppendSeparatorIfFieldsBefore() {
        PeriodFormatter formatter = builder
                .appendHours().appendSuffix("h")
                .appendSeparatorIfFieldsBefore(":")
                .appendMinutes().appendSuffix("m")
                .toFormatter();

        Period pHours = new Period(0, 0, 0, 0, 5, 0, 0, 0);
        assertEquals("5h:", formatter.print(pHours));
    }

    // Tests printing to Writer (IOException / Writer path)
    @Test
    public void testPrintToWriter() throws IOException {
        PeriodFormatter formatter = builder
                .appendLiteral("PT")
                .appendHours().appendSuffix("H")
                .appendMinutes().appendSuffix("M")
                .toFormatter();

        StringWriter writer = new StringWriter();
        formatter.getPrinter().printTo(writer, new Period(0, 0, 0, 0, 2, 30, 0, 0), Locale.ENGLISH);
        assertEquals("PT2H30M", writer.toString());
    }

    // Tests toPrinter and toParser returning null when printer or parser is missing
    @Test
    public void testToPrinterAndToParser_partialSupport() {
        PeriodPrinter dummyPrinter = builder.appendHours().toPrinter();
        builder.clear();
        builder.append(dummyPrinter, null);

        assertNotNull(builder.toPrinter());
        assertNull(builder.toParser());
        assertTrue(builder.toFormatter().isPrinter());
        assertFalse(builder.toFormatter().isParser());
    }

    // Tests clear method resetting builder state
    @Test
    public void testClear_resetsState() {
        builder.appendHours().appendSuffix("H");
        builder.clear();
        builder.appendMinutes().appendSuffix("M");
        PeriodFormatter formatter = builder.toFormatter();

        Period period = new Period(0, 0, 0, 0, 1, 2, 0, 0);
        assertEquals("2M", formatter.print(period));
    }

    // Tests exception when appending null literal
    @Test(expected = IllegalArgumentException.class)
    public void testAppendLiteral_null_throwsException() {
        builder.appendLiteral(null);
    }

    // Tests exception when appending suffix without prior field
    @Test(expected = IllegalStateException.class)
    public void testAppendSuffix_withoutField_throwsException() {
        builder.appendSuffix("suffix");
    }

    // Tests exception when appending prefix without subsequent field
    @Test(expected = IllegalStateException.class)
    public void testPrefixNotFollowedByField_throwsException() {
        builder.appendPrefix("prefix-").toFormatter();
    }

    // Tests exception on adjacent separators
    @Test(expected = IllegalStateException.class)
    public void testAdjacentSeparators_throwsException() {
        builder.appendHours()
                .appendSeparator(",")
                .appendSeparator(",")
                .appendMinutes();
    }

    // Tests exception when toFormatter has neither printer nor parser
    @Test(expected = IllegalStateException.class)
    public void testToFormatter_emptyBuilder_throwsException() {
        builder.clear();
        builder.toFormatter();
    }
}