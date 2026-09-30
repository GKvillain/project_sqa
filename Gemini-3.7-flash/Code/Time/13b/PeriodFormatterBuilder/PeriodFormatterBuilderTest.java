package org.joda.time.format;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.MutablePeriod;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PeriodFormatterBuilderTest {

    private PeriodFormatterBuilder builder;

    @Before
    public void setUp() {
        builder = new PeriodFormatterBuilder();
    }

    // Tests formatting negative millis less than 1 second with optional millis (Defects4J Time-13 defect)
    @Test
    public void testPrint_negativeMillisLessThanOneSecond_printsMinusZeroPointMillis() {
        PeriodFormatter formatter = builder
                .appendSecondsWithOptionalMillis()
                .appendSuffix("s")
                .toFormatter();

        Period period = new Period(0, 0, 0, -500);
        assertEquals("-0.5s", formatter.print(period));

        Period period2 = new Period(0, 0, 0, -8);
        assertEquals("-0.008s", formatter.print(period2));
    }

    // Tests formatting negative seconds and millis with appendSecondsWithMillis
    @Test
    public void testPrint_negativeSecondsAndMillis_printsNegative() {
        PeriodFormatter formatter = builder
                .appendSecondsWithMillis()
                .appendSuffix("s")
                .toFormatter();

        Period period = new Period(0, 0, -2, -500);
        assertEquals("-2.500s", formatter.print(period));
    }

    // Tests standard printing of fields with suffixes
    @Test
    public void testPrint_standardFieldsWithSuffixes_printsFormattedString() {
        PeriodFormatter formatter = builder
                .appendYears().appendSuffix(" year", " years")
                .appendSeparator(", ")
                .appendMonths().appendSuffix(" month", " months")
                .appendSeparator(", ")
                .appendDays().appendSuffix(" day", " days")
                .toFormatter();

        Period p1 = new Period(1, 2, 0, 3, 0, 0, 0, 0);
        assertEquals("1 year, 2 months, 3 days", formatter.print(p1));

        Period p2 = new Period(2, 1, 0, 1, 0, 0, 0, 0);
        assertEquals("2 years, 1 month, 1 day", formatter.print(p2));
    }

    // Tests printZeroAlways behavior
    @Test
    public void testPrint_printZeroAlways_printsZeroValues() {
        PeriodFormatter formatter = builder
                .printZeroAlways()
                .appendHours()
                .appendLiteral(":")
                .appendMinutes()
                .appendLiteral(":")
                .appendSeconds()
                .toFormatter();

        Period period = new Period(0, 0, 0, 0);
        assertEquals("0:0:0", formatter.print(period));
    }

    // Tests printZeroRarelyFirst behavior
    @Test
    public void testPrint_printZeroRarelyFirst_printsZeroOnFirstFieldOnly() {
        PeriodFormatter formatter = builder
                .printZeroRarelyFirst()
                .appendHours().appendSuffix("h")
                .appendMinutes().appendSuffix("m")
                .appendSeconds().appendSuffix("s")
                .toFormatter();

        Period period = new Period(0, 0, 0, 0);
        assertEquals("0h", formatter.print(period));
    }

    // Tests printZeroRarelyLast behavior (default)
    @Test
    public void testPrint_printZeroRarelyLast_printsZeroOnLastFieldOnly() {
        PeriodFormatter formatter = builder
                .printZeroRarelyLast()
                .appendHours().appendSuffix("h")
                .appendMinutes().appendSuffix("m")
                .appendSeconds().appendSuffix("s")
                .toFormatter();

        Period period = new Period(0, 0, 0, 0);
        assertEquals("0s", formatter.print(period));
    }

    // Tests printZeroNever behavior
    @Test
    public void testPrint_printZeroNever_printsEmptyStringWhenZero() {
        PeriodFormatter formatter = builder
                .printZeroNever()
                .appendHours().appendSuffix("h")
                .appendMinutes().appendSuffix("m")
                .toFormatter();

        Period period = new Period(0, 0, 0, 0);
        assertEquals("", formatter.print(period));
    }

    // Tests minimumPrintedDigits formatting
    @Test
    public void testPrint_minimumPrintedDigits_padsWithZeros() {
        PeriodFormatter formatter = builder
                .minimumPrintedDigits(2)
                .appendHours()
                .appendLiteral(":")
                .appendMinutes()
                .toFormatter();

        Period period = new Period(5, 7, 0, 0);
        assertEquals("05:07", formatter.print(period));
    }

    // Tests printing to Writer and calculatePrintedLength
    @Test
    public void testPrintToWriter_validPeriod_writesSuccessfully() throws IOException {
        PeriodFormatter formatter = builder
                .appendHours().appendSuffix("h")
                .appendMinutes().appendSuffix("m")
                .toFormatter();

        Period period = new Period(1, 30, 0, 0);
        StringWriter writer = new StringWriter();
        formatter.getPrinter().printTo(writer, period, Locale.getDefault());
        assertEquals("1h30m", writer.toString());

        int length = formatter.getPrinter().calculatePrintedLength(period, Locale.getDefault());
        assertEquals("1h30m".length(), length);
    }

    // Tests appendPrefix and composite prefix/affix
    @Test
    public void testPrintAndParse_withPrefixAndSuffix_success() {
        PeriodFormatter formatter = builder
                .appendPrefix("P-", "P+")
                .appendDays()
                .appendSuffix("D")
                .toFormatter();

        Period period = new Period(0, 0, 0, 5, 0, 0, 0, 0);
        assertEquals("P+5D", formatter.print(period));

        MutablePeriod parsed = new MutablePeriod();
        int pos = formatter.getParser().parseInto(parsed, "P+5D", 0, Locale.getDefault());
        assertEquals(4, pos);
        assertEquals(5, parsed.getDays());
    }

    // Tests appendSeparator variations (final text and variants)
    @Test
    public void testPrint_appendSeparatorWithFinalText_printsCorrectSeparators() {
        PeriodFormatter formatter = builder
                .appendYears().appendSuffix("y")
                .appendSeparator(", ", " and ")
                .appendMonths().appendSuffix("m")
                .appendSeparator(", ", " and ")
                .appendDays().appendSuffix("d")
                .toFormatter();

        Period threeFields = new Period(1, 2, 0, 3, 0, 0, 0, 0);
        assertEquals("1y, 2m and 3d", formatter.print(threeFields));

        Period twoFields = new Period(1, 0, 0, 3, 0, 0, 0, 0);
        assertEquals("1y and 3d", formatter.print(twoFields));

        Period oneField = new Period(1, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("1y", formatter.print(oneField));
    }

    // Tests parsing with integer fields, signed values, and separators
    @Test
    public void testParse_standardText_parsesIntoPeriod() {
        PeriodFormatter formatter = builder
                .appendYears().appendSuffix("Y")
                .appendMonths().appendSuffix("M")
                .appendWeeks().appendSuffix("W")
                .appendDays().appendSuffix("D")
                .toFormatter();

        MutablePeriod period = new MutablePeriod();
        int pos = formatter.getParser().parseInto(period, "1Y2M3W4D", 0, Locale.getDefault());

        assertEquals(8, pos);
        assertEquals(1, period.getYears());
        assertEquals(2, period.getMonths());
        assertEquals(3, period.getWeeks());
        assertEquals(4, period.getDays());
    }

    // Tests decimal seconds and millis
    @Test
    public void testParse_secondsWithFractionalMillis_parsesSecondsAndMillis() {
        PeriodFormatter formatter = builder
                .appendSecondsWithOptionalMillis()
                .appendSuffix("s")
                .toFormatter();

        MutablePeriod period = new MutablePeriod();
        int pos = formatter.getParser().parseInto(period, "12.345s", 0, Locale.getDefault());

        assertEquals(7, pos);
        assertEquals(12, period.getSeconds());
        assertEquals(345, period.getMillis());
    }

    // Tests parsing signed negative values
    @Test
    public void testParse_negativeValues_parsesNegative() {
        PeriodFormatter formatter = builder
                .appendHours().appendSuffix("h")
                .appendMinutes().appendSuffix("m")
                .toFormatter();

        MutablePeriod period = new MutablePeriod();
        int pos = formatter.getParser().parseInto(period, "-5h-30m", 0, Locale.getDefault());

        assertEquals(7, pos);
        assertEquals(-5, period.getHours());
        assertEquals(-30, period.getMinutes());
    }

    // Tests rejectSignedValues setting during parsing
    @Test
    public void testParse_rejectSignedValues_failsOnSign() {
        PeriodFormatter formatter = builder
                .rejectSignedValues(true)
                .appendHours().appendSuffix("h")
                .toFormatter();

        MutablePeriod period = new MutablePeriod();
        int pos = formatter.getParser().parseInto(period, "-5h", 0, Locale.getDefault());

        assertTrue(pos < 0);
    }

    // Tests appendMillis and appendMillis3Digit
    @Test
    public void testPrint_appendMillis3Digit_prints3Digits() {
        PeriodFormatter formatter = builder
                .appendMillis3Digit()
                .appendSuffix("ms")
                .toFormatter();

        Period period = new Period(0, 0, 0, 5);
        assertEquals("005ms", formatter.print(period));
    }

    // Tests append(PeriodFormatter) and clear()
    @Test
    public void testAppendFormatterAndClear_resetsBuilder() {
        PeriodFormatter subFormatter = new PeriodFormatterBuilder()
                .appendHours().appendSuffix("h")
                .toFormatter();

        builder.append(subFormatter).appendMinutes().appendSuffix("m");
        PeriodFormatter combined = builder.toFormatter();
        assertEquals("1h30m", combined.print(new Period(1, 30, 0, 0)));

        builder.clear();
        builder.appendDays().appendSuffix("d");
        PeriodFormatter clearedFormatter = builder.toFormatter();
        assertEquals("5d", clearedFormatter.print(new Period(0, 0, 0, 5, 0, 0, 0, 0)));
    }

    // Tests exception path when appending null formatter
    @Test(expected = IllegalArgumentException.class)
    public void testAppend_nullFormatter_throwsIllegalArgumentException() {
        builder.append((PeriodFormatter) null);
    }

    // Tests exception path when appending null literal
    @Test(expected = IllegalArgumentException.class)
    public void testAppendLiteral_nullText_throwsIllegalArgumentException() {
        builder.appendLiteral(null);
    }

    // Tests exception path when suffix has no preceding field
    @Test(expected = IllegalStateException.class)
    public void testAppendSuffix_noField_throwsIllegalStateException() {
        builder.appendSuffix("suffix");
    }

    // Tests exception path when builder creates neither printer nor parser
    @Test(expected = IllegalStateException.class)
    public void testToFormatter_emptyBuilder_throwsIllegalStateException() {
        builder.toFormatter();
    }

    // Tests maximumParsedDigits configuration
    @Test
    public void testParse_maximumParsedDigits_limitsDigitCount() {
        PeriodFormatter formatter = builder
                .maximumParsedDigits(2)
                .appendHours()
                .appendSuffix("h")
                .toFormatter();

        MutablePeriod period = new MutablePeriod();
        int pos = formatter.getParser().parseInto(period, "123h", 0, Locale.getDefault());
        assertEquals(2, pos);
        assertEquals(12, period.getHours());
    }

    // Tests appendMillis standard
    @Test
    public void testPrintAndParse_appendMillis() {
        PeriodFormatter formatter = builder
                .appendMillis()
                .appendSuffix("ms")
                .toFormatter();

        Period period = new Period(0, 0, 0, 42);
        assertEquals("42ms", formatter.print(period));

        MutablePeriod parsed = new MutablePeriod();
        int pos = formatter.getParser().parseInto(parsed, "42ms", 0, Locale.getDefault());
        assertEquals(4, pos);
        assertEquals(42, parsed.getMillis());
    }

    // Tests appendSeparatorIfFieldsAfter
    @Test
    public void testPrint_appendSeparatorIfFieldsAfter() {
        PeriodFormatter formatter = builder
                .appendYears().appendSuffix("y")
                .appendSeparatorIfFieldsAfter(", ")
                .appendMonths().appendSuffix("m")
                .toFormatter();

        Period periodWithBoth = new Period(1, 2, 0, 0, 0, 0, 0, 0);
        assertEquals("1y, 2m", formatter.print(periodWithBoth));

        Period periodWithYearsOnly = new Period(1, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("1y", formatter.print(periodWithYearsOnly));
    }

    // Tests appendSeparatorIfFieldsBefore
    @Test
    public void testPrint_appendSeparatorIfFieldsBefore() {
        PeriodFormatter formatter = builder
                .appendYears().appendSuffix("y")
                .appendSeparatorIfFieldsBefore(", ")
                .appendMonths().appendSuffix("m")
                .toFormatter();

        Period periodWithBoth = new Period(1, 2, 0, 0, 0, 0, 0, 0);
        assertEquals("1y, 2m", formatter.print(periodWithBoth));

        Period periodWithMonthsOnly = new Period(0, 2, 0, 0, 0, 0, 0, 0);
        assertEquals("2m", formatter.print(periodWithMonthsOnly));
    }

    // Tests printZeroIfSupported setting
    @Test
    public void testPrint_printZeroIfSupported() {
        PeriodFormatter formatter = builder
                .printZeroIfSupported()
                .appendHours().appendSuffix("h")
                .appendMinutes().appendSuffix("m")
                .toFormatter();

        Period period = new Period(0, 0, 0, 0, PeriodType.hours());
        assertEquals("0h", formatter.print(period));
    }

    // Tests toPrinter and toParser
    @Test
    public void testToPrinterAndToParser() {
        builder.appendHours().appendSuffix("h");
        PeriodPrinter printer = builder.toPrinter();
        PeriodParser parser = builder.toParser();

        assertNotNull(printer);
        assertNotNull(parser);

        StringBuffer buf = new StringBuffer();
        printer.printTo(buf, new Period(2, 0, 0, 0), Locale.getDefault());
        assertEquals("2h", buf.toString());

        MutablePeriod parsed = new MutablePeriod();
        int pos = parser.parseInto(parsed, "3h", 0, Locale.getDefault());
        assertEquals(2, pos);
        assertEquals(3, parsed.getHours());
    }

    // Tests append(PeriodPrinter, PeriodParser)
    @Test
    public void testAppend_printerAndParser() {
        PeriodFormatter subFormatter = new PeriodFormatterBuilder()
                .appendHours().appendSuffix("h")
                .toFormatter();

        builder.append(subFormatter.getPrinter(), subFormatter.getParser())
                .appendMinutes().appendSuffix("m");

        PeriodFormatter formatter = builder.toFormatter();
        assertEquals("1h30m", formatter.print(new Period(1, 30, 0, 0)));
    }

    // Tests appendPrefix simple string
    @Test
    public void testAppendPrefix_simple() {
        PeriodFormatter formatter = builder
                .appendPrefix("PT")
                .appendHours()
                .appendSuffix("H")
                .toFormatter();

        assertEquals("PT2H", formatter.print(new Period(2, 0, 0, 0)));

        MutablePeriod parsed = new MutablePeriod();
        int pos = formatter.getParser().parseInto(parsed, "PT2H", 0, Locale.getDefault());
        assertEquals(4, pos);
        assertEquals(2, parsed.getHours());
    }
}