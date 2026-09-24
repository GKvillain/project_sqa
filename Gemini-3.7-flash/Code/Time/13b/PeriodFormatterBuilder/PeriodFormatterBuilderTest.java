package org.joda.time.format;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.MutablePeriod;
import org.joda.time.DurationFieldType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PeriodFormatterBuilderTest {

    private PeriodFormatterBuilder builder;

    @Before
    public void setUp() {
        builder = new PeriodFormatterBuilder();
    }

    // Tests building a simple years and months formatter and printing standard values
    @Test
    public void testPrint_yearsAndMonths_printsCorrectFormat() {
        PeriodFormatter formatter = builder
            .appendYears()
            .appendSuffix("Y")
            .appendMonths()
            .appendSuffix("M")
            .toFormatter();

        Period period = new Period(2, 5, 0, 0, 0, 0, 0, 0);
        assertEquals("2Y5M", formatter.print(period));
    }

    // Tests negative millis printing with secondsWithOptionalMillis (regression bug for negative zero seconds)
    @Test
    public void testPrint_negativeMillisOnly_printsNegativeZeroPoint() {
        PeriodFormatter formatter = builder
            .appendLiteral("PT")
            .appendSecondsWithOptionalMillis()
            .appendSuffix("S")
            .toFormatter();

        Period period = new Period(0, 0, 0, 0, 0, 0, 0, -500);
        assertEquals("PT-0.500S", formatter.print(period));
    }

    // Tests negative seconds and millis printing with secondsWithOptionalMillis
    @Test
    public void testPrint_negativeSecondsAndMillis_printsCorrectFormat() {
        PeriodFormatter formatter = builder
            .appendLiteral("PT")
            .appendSecondsWithOptionalMillis()
            .appendSuffix("S")
            .toFormatter();

        Period period = new Period(0, 0, 0, 0, 0, 0, -2, -500);
        assertEquals("PT-2.500S", formatter.print(period));
    }

    // Tests seconds with millis where millis are zero
    @Test
    public void testPrint_secondsWithOptionalMillisZeroMillis_omitsMillis() {
        PeriodFormatter formatter = builder
            .appendLiteral("PT")
            .appendSecondsWithOptionalMillis()
            .appendSuffix("S")
            .toFormatter();

        Period period = new Period(0, 0, 0, 0, 0, 0, 5, 0);
        assertEquals("PT5S", formatter.print(period));
    }

    // Tests secondsWithMillis always prints millis even if zero
    @Test
    public void testPrint_secondsWithMillisZeroMillis_printsMillis() {
        PeriodFormatter formatter = builder
            .appendSecondsWithMillis()
            .appendSuffix("s")
            .toFormatter();

        Period period = new Period(0, 0, 0, 0, 0, 0, 5, 0);
        assertEquals("5.000s", formatter.print(period));
    }

    // Tests printZeroAlways setting
    @Test
    public void testPrint_printZeroAlways_printsZeroFields() {
        PeriodFormatter formatter = builder
            .printZeroAlways()
            .appendHours()
            .appendSuffix("h")
            .appendMinutes()
            .appendSuffix("m")
            .toFormatter();

        Period period = new Period(0, 0, 0, 0, 0, 5, 0, 0);
        assertEquals("0h5m", formatter.print(period));
    }

    // Tests printZeroNever setting
    @Test
    public void testPrint_printZeroNever_omitsZeroFields() {
        PeriodFormatter formatter = builder
            .printZeroNever()
            .appendHours()
            .appendSuffix("h")
            .appendMinutes()
            .appendSuffix("m")
            .toFormatter();

        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("", formatter.print(period));
    }

    // Tests plural affix with singular and plural values
    @Test
    public void testPrint_pluralAffix_usesSingularAndPlural() {
        PeriodFormatter formatter = builder
            .appendDays()
            .appendSuffix(" day", " days")
            .toFormatter();

        assertEquals("1 day", formatter.print(new Period().withDays(1)));
        assertEquals("2 days", formatter.print(new Period().withDays(2)));
        assertEquals("0 days", formatter.print(new Period().withDays(0)));
    }

    // Tests prefix appending and formatting
    @Test
    public void testPrint_prefix_printsPrefixBeforeField() {
        PeriodFormatter formatter = builder
            .appendPrefix("T: ")
            .appendHours()
            .toFormatter();

        assertEquals("T: 4", formatter.print(new Period().withHours(4)));
    }

    // Tests separator with multiple elements
    @Test
    public void testPrint_separator_formatsCorrectly() {
        PeriodFormatter formatter = builder
            .appendYears()
            .appendSuffix("Y")
            .appendSeparator(", ", " and ")
            .appendMonths()
            .appendSuffix("M")
            .appendSeparator(", ", " and ")
            .appendDays()
            .appendSuffix("D")
            .toFormatter();

        assertEquals("1Y, 2M and 3D", formatter.print(new Period(1, 2, 0, 3, 0, 0, 0, 0)));
        assertEquals("1Y and 2M", formatter.print(new Period(1, 2, 0, 0, 0, 0, 0, 0)));
        assertEquals("1Y", formatter.print(new Period(1, 0, 0, 0, 0, 0, 0, 0)));
    }

    // Tests parsing standard period string
    @Test
    public void testParse_standardFormat_parsesCorrectPeriod() {
        PeriodFormatter formatter = builder
            .appendYears().appendSuffix("y")
            .appendMonths().appendSuffix("m")
            .appendDays().appendSuffix("d")
            .toFormatter();

        Period parsed = formatter.parsePeriod("5y6m7d");
        assertEquals(5, parsed.getYears());
        assertEquals(6, parsed.getMonths());
        assertEquals(7, parsed.getDays());
    }

    // Tests parsing seconds and millis
    @Test
    public void testParse_secondsWithOptionalMillis_parsesCorrectly() {
        PeriodFormatter formatter = builder
            .appendLiteral("PT")
            .appendSecondsWithOptionalMillis()
            .appendSuffix("S")
            .toFormatter();

        Period parsed = formatter.parsePeriod("PT12.345S");
        assertEquals(12, parsed.getSeconds());
        assertEquals(345, parsed.getMillis());
    }

    // Tests parsing negative seconds and millis
    @Test
    public void testParse_negativeSecondsAndMillis_parsesCorrectly() {
        PeriodFormatter formatter = builder
            .appendLiteral("PT")
            .appendSecondsWithOptionalMillis()
            .appendSuffix("S")
            .toFormatter();

        Period parsed = formatter.parsePeriod("PT-12.345S");
        assertEquals(-12, parsed.getSeconds());
        assertEquals(-345, parsed.getMillis());
    }

    // Tests minimum printed digits formatting
    @Test
    public void testPrint_minimumPrintedDigits_padsZeros() {
        PeriodFormatter formatter = builder
            .minimumPrintedDigits(2)
            .appendHours()
            .appendLiteral(":")
            .appendMinutes()
            .appendLiteral(":")
            .appendSeconds()
            .toFormatter();

        Period period = new Period(0, 0, 0, 0, 3, 4, 5, 0);
        assertEquals("03:04:05", formatter.print(period));
    }

    // Tests printTo with Writer
    @Test
    public void testPrintTo_writer_outputsExpectedText() throws IOException {
        PeriodFormatter formatter = builder
            .appendYears()
            .appendSuffix("Y")
            .toFormatter();

        StringWriter writer = new StringWriter();
        formatter.getPrinter().printTo(writer, new Period().withYears(10), Locale.ENGLISH);
        assertEquals("10Y", writer.toString());
    }

    // Tests append null formatter throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAppend_nullFormatter_throwsException() {
        builder.append((PeriodFormatter) null);
    }

    // Tests appendSuffix without a field throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testAppendSuffix_noField_throwsException() {
        builder.appendSuffix("s");
    }

    // Tests clear resets builder state
    @Test
    public void testClear_builderReset_allowsReuse() {
        builder.appendHours().appendSuffix("h");
        builder.clear();
        PeriodFormatter formatter = builder.appendMinutes().appendSuffix("m").toFormatter();

        assertEquals("15m", formatter.print(new Period().withMinutes(15)));
    }

    // Tests toPrinter and toParser methods
    @Test
    public void testToPrinterAndToParser_validBuilder_returnsNotNull() {
        builder.appendDays().appendSuffix("d");
        assertNotNull(builder.toPrinter());
        assertNotNull(builder.toParser());
    }

    @Test
    public void testAppendWeeksAndMillisAndMillis3Digit() {
        PeriodFormatter formatter = builder
            .appendWeeks().appendSuffix("w")
            .appendMillis().appendSuffix("ms")
            .appendMillis3Digit()
            .toFormatter();

        Period period = new Period(0, 0, 2, 0, 0, 0, 0, 5);
        assertEquals("2w5ms005", formatter.print(period));

        Period parsed = formatter.parsePeriod("2w5ms005");
        assertEquals(2, parsed.getWeeks());
        assertEquals(5, parsed.getMillis());
    }

    @Test
    public void testPrintZeroRarelyFirstAndLastAndIfSupported() {
        PeriodFormatter f1 = new PeriodFormatterBuilder()
            .printZeroRarelyFirst()
            .appendHours().appendSuffix("h")
            .appendMinutes().appendSuffix("m")
            .toFormatter();

        assertEquals("0h5m", f1.print(new Period(0, 0, 0, 0, 0, 5, 0, 0)));
        assertEquals("", f1.print(new Period(0, 0, 0, 0, 0, 0, 0, 0)));

        PeriodFormatter f2 = new PeriodFormatterBuilder()
            .printZeroRarelyLast()
            .appendHours().appendSuffix("h")
            .appendMinutes().appendSuffix("m")
            .toFormatter();

        assertEquals("5h0m", f2.print(new Period(0, 0, 0, 0, 5, 0, 0, 0)));
        assertEquals("", f2.print(new Period(0, 0, 0, 0, 0, 0, 0, 0)));

        PeriodFormatter f3 = new PeriodFormatterBuilder()
            .printZeroIfSupported()
            .appendHours().appendSuffix("h")
            .appendMinutes().appendSuffix("m")
            .toFormatter();

        Period periodHoursOnly = new Period(0, 5, 0, 0, PeriodType.hours());
        assertEquals("5h", f3.print(periodHoursOnly));
    }

    @Test
    public void testMaximumParsedDigitsAndRejectSignedValues() {
        PeriodFormatter formatter = builder
            .maximumParsedDigits(2)
            .rejectSignedValues(true)
            .appendHours().appendSuffix("h")
            .toFormatter();

        MutablePeriod period = new MutablePeriod();
        int res = formatter.getParser().parseInto(period, "123h", 0, Locale.ENGLISH);
        assertEquals(2, res); // parsed max 2 digits "12"
        assertEquals(12, period.getHours());

        MutablePeriod periodNegative = new MutablePeriod();
        int failRes = formatter.getParser().parseInto(periodNegative, "-12h", 0, Locale.ENGLISH);
        assertTrue(failRes < 0);
    }

    @Test
    public void testAppendPrefixPluralAndPrefixSuffixBranches() {
        PeriodFormatter formatter = builder
            .appendPrefix("approx ", "around ")
            .appendHours()
            .appendSuffix(" hour", " hours")
            .toFormatter();

        assertEquals("around 1 hour", formatter.print(new Period().withHours(1)));
        assertEquals("approx 2 hours", formatter.print(new Period().withHours(2)));

        Period p1 = formatter.parsePeriod("around 1 hour");
        assertEquals(1, p1.getHours());
        Period p2 = formatter.parsePeriod("approx 2 hours");
        assertEquals(2, p2.getHours());
    }

    @Test
    public void testAppendSeparatorIfFieldsAfterAndIfFieldsBefore() {
        PeriodFormatter fAfter = new PeriodFormatterBuilder()
            .appendHours().appendSuffix("h")
            .appendSeparatorIfFieldsAfter(":")
            .appendMinutes().appendSuffix("m")
            .toFormatter();

        assertEquals("1h:2m", fAfter.print(new Period(0, 0, 0, 0, 1, 2, 0, 0)));
        assertEquals("1h", fAfter.print(new Period(0, 0, 0, 0, 1, 0, 0, 0)));
        assertEquals("2m", fAfter.print(new Period(0, 0, 0, 0, 0, 2, 0, 0)));

        PeriodFormatter fBefore = new PeriodFormatterBuilder()
            .appendHours().appendSuffix("h")
            .appendSeparatorIfFieldsBefore(":")
            .appendMinutes().appendSuffix("m")
            .toFormatter();

        assertEquals("1h:2m", fBefore.print(new Period(0, 0, 0, 0, 1, 2, 0, 0)));
        assertEquals("1h", fBefore.print(new Period(0, 0, 0, 0, 1, 0, 0, 0)));
        assertEquals("2m", fBefore.print(new Period(0, 0, 0, 0, 0, 2, 0, 0)));
    }

    @Test
    public void testAppendFormatterAndPrinterParserCombo() {
        PeriodFormatter subFormatter = new PeriodFormatterBuilder()
            .appendMinutes().appendSuffix("m")
            .toFormatter();

        PeriodFormatter mainFormatter = builder
            .appendHours().appendSuffix("h ")
            .append(subFormatter)
            .append(subFormatter.getPrinter(), subFormatter.getParser())
            .toFormatter();

        Period period = new Period(0, 0, 0, 0, 1, 2, 0, 0);
        assertEquals("1h 2m2m", mainFormatter.print(period));
    }

    @Test
    public void testEmptyBuilderToFormatterPrinterParser() {
        PeriodFormatter emptyFormatter = builder.toFormatter();
        assertNull(emptyFormatter.getPrinter());
        assertNull(emptyFormatter.getParser());
        assertEquals("", emptyFormatter.print(new Period()));

        assertNull(builder.toPrinter());
        assertNull(builder.toParser());
    }

    @Test(expected = IllegalStateException.class)
    public void testAppendPrefixWithoutFollowingField() {
        builder.appendPrefix("pre:").toFormatter();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendSeparatorNullText() {
        builder.appendSeparator(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendLiteralNullText() {
        builder.appendLiteral(null);
    }
}