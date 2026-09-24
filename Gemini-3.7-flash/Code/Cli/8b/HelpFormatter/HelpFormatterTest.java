package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for {@link HelpFormatter}.
 */
public class HelpFormatterTest {

    private HelpFormatter formatter;
    private StringWriter out;
    private PrintWriter pw;
    private String defaultEOL;

    @Before
    public void setUp() {
        formatter = new HelpFormatter();
        out = new StringWriter();
        pw = new PrintWriter(out);
        defaultEOL = formatter.getNewLine();
    }

    // Tests getters and setters for configuration properties
    @Test
    public void testGettersAndSetters_customValues_returnsUpdatedValues() {
        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());

        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());

        formatter.setDescPadding(7);
        assertEquals(7, formatter.getDescPadding());

        formatter.setSyntaxPrefix("Syntax: ");
        assertEquals("Syntax: ", formatter.getSyntaxPrefix());

        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());

        formatter.setOptPrefix("+");
        assertEquals("+", formatter.getOptPrefix());

        formatter.setLongOptPrefix("++");
        assertEquals("++", formatter.getLongOptPrefix());

        formatter.setArgName("parameter");
        assertEquals("parameter", formatter.getArgName());
    }

    // Tests printHelp with null or empty command line syntax throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_nullCmdLineSyntax_throwsIllegalArgumentException() {
        formatter.printHelp(pw, 80, null, "header", new Options(), 1, 3, "footer");
    }

    // Tests printHelp with empty command line syntax throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_emptyCmdLineSyntax_throwsIllegalArgumentException() {
        formatter.printHelp(pw, 80, "", "header", new Options(), 1, 3, "footer");
    }

    // Tests printHelp with basic options, header, and footer without autoUsage
    @Test
    public void testPrintHelp_standardOptions_rendersFormattedHelp() {
        Options options = new Options();
        options.addOption(new Option("a", "enable-a", false, "Option A description"));
        options.addOption(new Option("b", true, "Option B with value"));

        formatter.printHelp(pw, 80, "myapp <arg>", "Header text", options, 2, 4, "Footer text", false);
        pw.flush();

        String expected = "usage: myapp <arg>" + defaultEOL
                + "Header text" + defaultEOL
                + "  -a,--enable-a        Option A description" + defaultEOL
                + "  -b <arg>             Option B with value" + defaultEOL
                + "Footer text" + defaultEOL;

        assertEquals(expected, out.toString());
    }

    // Tests printHelp with autoUsage enabled
    @Test
    public void testPrintHelp_autoUsageEnabled_rendersGeneratedUsage() {
        Options options = new Options();
        Option optA = new Option("a", "alpha", false, "Alpha option");
        Option optB = new Option("b", "beta", true, "Beta option");
        optB.setRequired(true);

        options.addOption(optA);
        options.addOption(optB);

        formatter.printHelp(pw, 80, "myapp", null, options, 1, 3, null, true);
        pw.flush();

        String expected = "usage: myapp [-a] -b <arg>" + defaultEOL
                + " -a,--alpha   Alpha option" + defaultEOL
                + " -b,--beta <arg>   Beta option" + defaultEOL;

        assertEquals(expected, out.toString());
    }

    // Tests printUsage with OptionGroup containing required and non-required groups
    @Test
    public void testPrintUsage_optionGroup_rendersMutualExclusionCorrectly() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("s", "start", false, "start engine"));
        group.addOption(new Option("t", "stop", false, "stop engine"));
        group.setRequired(true);

        options.addOptionGroup(group);

        formatter.printUsage(pw, 80, "app", options);
        pw.flush();

        String expected = "usage: app -s | -t" + defaultEOL;
        assertEquals(expected, out.toString());
    }

    // Tests printUsage with optional OptionGroup
    @Test
    public void testPrintUsage_optionalOptionGroup_rendersBrackets() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("x", false, "option x"));
        group.addOption(new Option("y", false, "option y"));
        group.setRequired(false);

        options.addOptionGroup(group);

        formatter.printUsage(pw, 80, "app", options);
        pw.flush();

        String expected = "usage: app [-x | -y]" + defaultEOL;
        assertEquals(expected, out.toString());
    }

    // Tests printUsage with option having custom argument name and long opt only
    @Test
    public void testPrintUsage_longOptOnlyWithArgName_rendersCorrectly() {
        Options options = new Options();
        Option longOnly = new Option(null, "config", true, "configuration file");
        longOnly.setArgName("FILE");
        options.addOption(longOnly);

        formatter.printUsage(pw, 80, "app", options);
        pw.flush();

        String expected = "usage: app [--config <FILE>]" + defaultEOL;
        assertEquals(expected, out.toString());
    }

    // Tests renderOptions with option having only long opt and null description
    @Test
    public void testRenderOptions_longOptOnlyAndNullDescription_rendersCorrectly() {
        Options options = new Options();
        Option longOnly = new Option(null, "verbose", false, null);
        options.addOption(longOnly);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 1, 3);

        String expected = "    --verbose   ";
        assertEquals(expected, sb.toString());
    }

    // Tests renderOptions with argument having no argName
    @Test
    public void testRenderOptions_argWithoutArgName_rendersSpace() {
        Options options = new Options();
        Option opt = new Option("o", "output", true, "output path");
        opt.setArgName(null);
        options.addOption(opt);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 1, 3);

        String expected = " -o,--output    output path";
        assertEquals(expected, sb.toString());
    }

    // Tests renderWrappedText wrapping text across multiple lines with nextLineTabStop
    @Test
    public void testRenderWrappedText_textLongerThanWidth_wrapsWithTabStopPadding() {
        StringBuffer sb = new StringBuffer();
        String text = "This is a long description text that needs to be wrapped across multiple lines.";
        int width = 25;
        int nextLineTabStop = 4;

        formatter.renderWrappedText(sb, width, nextLineTabStop, text);

        String expected = "This is a long" + defaultEOL
                + "    description text that" + defaultEOL
                + "    needs to be wrapped" + defaultEOL
                + "    across multiple" + defaultEOL
                + "    lines.";
        assertEquals(expected, sb.toString());
    }

    // Tests renderWrappedText with text containing explicit newline character
    @Test
    public void testRenderWrappedText_textWithEmbeddedNewlines_preservesLines() {
        StringBuffer sb = new StringBuffer();
        String text = "Line 1\nLine 2\nLine 3";

        formatter.renderWrappedText(sb, 80, 0, text);

        String expected = "Line 1" + defaultEOL
                + "Line 2" + defaultEOL
                + "Line 3";
        assertEquals(expected, sb.toString());
    }

    // Tests renderWrappedText when text fits within single line
    @Test
    public void testRenderWrappedText_shortText_doesNotWrap() {
        StringBuffer sb = new StringBuffer();
        String text = "Short text";

        formatter.renderWrappedText(sb, 80, 4, text);

        assertEquals("Short text", sb.toString());
    }

    // Tests findWrapPos with explicit newline within line width
    @Test
    public void testFindWrapPos_newlineWithinWidth_returnsNewlinePosition() {
        String text = "First line\nSecond line";
        int pos = formatter.findWrapPos(text, 20, 0);
        assertEquals(11, pos);
    }

    // Tests findWrapPos with tab character within line width
    @Test
    public void testFindWrapPos_tabWithinWidth_returnsTabPosition() {
        String text = "First\tSecond";
        int pos = formatter.findWrapPos(text, 20, 0);
        assertEquals(6, pos);
    }

    // Tests findWrapPos when startPos plus width exceeds text length
    @Test
    public void testFindWrapPos_startPosPlusWidthExceedsLength_returnsMinusOne() {
        String text = "Hello world";
        int pos = formatter.findWrapPos(text, 50, 0);
        assertEquals(-1, pos);
    }

    // Tests findWrapPos when word is longer than column width (wrap after width)
    @Test
    public void testFindWrapPos_wordLongerThanWidth_findsNextSpaceAfterWidth() {
        String text = "Supercalifragilisticexpialidocious text";
        int pos = formatter.findWrapPos(text, 10, 0);
        assertEquals(34, pos);
    }

    // Tests createPadding with zero and positive lengths
    @Test
    public void testCreatePadding_variousLengths_returnsExpectedSpaces() {
        assertEquals("", formatter.createPadding(0));
        assertEquals(" ", formatter.createPadding(1));
        assertEquals("   ", formatter.createPadding(3));
    }

    // Tests rtrim with null, empty, trimmed, and padded strings
    @Test
    public void testRtrim_variousInputs_removesOnlyTrailingWhitespace() {
        assertNull(formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("  abc", formatter.rtrim("  abc  \t\n"));
        assertEquals("hello", formatter.rtrim("hello"));
    }

    // Tests printWrapped convenience methods writing to PrintWriter
    @Test
    public void testPrintWrapped_withPrintWriter_outputsExpectedContent() {
        formatter.printWrapped(pw, 20, "This is a test message for printWrapped.");
        pw.flush();

        String expected = "This is a test" + defaultEOL
                + "message for" + defaultEOL
                + "printWrapped." + defaultEOL;
        assertEquals(expected, out.toString());
    }

    // Tests printUsage with simple cmdLineSyntax
    @Test
    public void testPrintUsage_simpleSyntax_formatsWithPrefix() {
        formatter.printUsage(pw, 80, "myapp -f <file>");
        pw.flush();

        String expected = "usage: myapp -f <file>" + defaultEOL;
        assertEquals(expected, out.toString());
    }

    // Tests comparator getter and setter and custom comparator ordering
    @Test
    public void testOptionComparator_customComparator_sortsOptionsAccordingly() {
        Comparator<Option> comparator = formatter.getOptionComparator();
        assertTrue(comparator != null);

        Options options = new Options();
        options.addOption(new Option("z", "last", false, "Z option"));
        options.addOption(new Option("a", "first", false, "A option"));

        // Custom comparator: reverse alphabetical by opt
        formatter.setOptionComparator(new Comparator<Option>() {
            @Override
            public int compare(Option o1, Option o2) {
                return o2.getKey().compareToIgnoreCase(o1.getKey());
            }
        });

        formatter.printHelp(pw, 80, "myapp", null, options, 1, 3, null, false);
        pw.flush();

        String expected = "usage: myapp" + defaultEOL
                + " -z,--last    Z option" + defaultEOL
                + " -a,--first   A option" + defaultEOL;
        assertEquals(expected, out.toString());
    }

    // Tests setOptionComparator with null preserving insertion order
    @Test
    public void testOptionComparator_nullComparator_preservesInsertionOrder() {
        formatter.setOptionComparator(null);
        assertNull(formatter.getOptionComparator());

        Options options = new Options();
        options.addOption(new Option("c", false, "third"));
        options.addOption(new Option("a", false, "first"));
        options.addOption(new Option("b", false, "second"));

        formatter.printHelp(pw, 80, "myapp", null, options, 1, 3, null, false);
        pw.flush();

        String expected = "usage: myapp" + defaultEOL
                + " -c   third" + defaultEOL
                + " -a   first" + defaultEOL
                + " -b   second" + defaultEOL;
        assertEquals(expected, out.toString());
    }

    // Tests findWrapPos with carriage return '\r' and '\r\n'
    @Test
    public void testFindWrapPos_carriageReturn_returnsCorrectPosition() {
        String text = "First line\rSecond line";
        int pos = formatter.findWrapPos(text, 20, 0);
        assertEquals(11, pos);

        String textCRLF = "First line\r\nSecond line";
        int posCRLF = formatter.findWrapPos(textCRLF, 20, 0);
        assertEquals(11, posCRLF);
    }

    // Tests findWrapPos when single unbroken word exceeds string length
    @Test
    public void testFindWrapPos_singleUnbrokenWordExceedsWidth_returnsMinusOne() {
        String text = "UnbrokenWordWithoutAnySpaces";
        int pos = formatter.findWrapPos(text, 10, 0);
        assertEquals(-1, pos);
    }

    // Tests renderWrappedText when nextLineTabStop is greater than or equal to width
    @Test
    public void testRenderWrappedText_tabStopGreaterOrEqualToWidth_wrapsGracefully() {
        StringBuffer sb = new StringBuffer();
        String text = "Line that is long enough to wrap";
        formatter.renderWrappedText(sb, 10, 10, text);

        String expected = "Line that" + defaultEOL
                + " is long" + defaultEOL
                + " enough to" + defaultEOL
                + " wrap";
        assertEquals(expected, sb.toString());
    }

    // Tests renderOptions with wrapped description when description exceeds line width
    @Test
    public void testRenderOptions_longDescription_wrapsWithCorrectPadding() {
        Options options = new Options();
        options.addOption(new Option("h", "help", false, "This is a very long description that definitely exceeds the given line width."));

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 35, options, 2, 2);

        String expected = "  -h,--help  This is a very" + defaultEOL
                + "             long description" + defaultEOL
                + "             that definitely" + defaultEOL
                + "             exceeds the given" + defaultEOL
                + "             line width.";
        assertEquals(expected, sb.toString());
    }

    // Tests renderOptions with option having optional arg
    @Test
    public void testRenderOptions_optionalArg_rendersCorrectArgSyntax() {
        Options options = new Options();
        Option opt = new Option("f", "file", true, "file path");
        opt.setOptionalArg(true);
        options.addOption(opt);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 1, 3);

        String expected = " -f,--file [<arg>]   file path";
        assertEquals(expected, sb.toString());
    }

    // Tests printUsage with options wrapping across multiple lines
    @Test
    public void testPrintUsage_longUsageText_wrapsAcrossLines() {
        Options options = new Options();
        options.addOption(new Option("a", "alpha-long-option-name", true, "desc a"));
        options.addOption(new Option("b", "beta-long-option-name", true, "desc b"));
        options.addOption(new Option("c", "gamma-long-option-name", true, "desc c"));

        formatter.printUsage(pw, 40, "myapp", options);
        pw.flush();

        String expected = "usage: myapp [-a <arg>] [-b <arg>]" + defaultEOL
                + "       [-c <arg>]" + defaultEOL;
        assertEquals(expected, out.toString());
    }

    // Tests printWrapped with custom nextLineTabStop
    @Test
    public void testPrintWrapped_withTabStop_outputsWrappedWithIndent() {
        formatter.printWrapped(pw, 20, 4, "This is a long message to test printWrapped with tab stop.");
        pw.flush();

        String expected = "This is a long" + defaultEOL
                + "    message to test" + defaultEOL
                + "    printWrapped" + defaultEOL
                + "    with tab stop." + defaultEOL;
        assertEquals(expected, out.toString());
    }

    // Tests overloaded printHelp methods to ensure full API coverage
    @Test
    public void testPrintHelp_allOverloadedSignatures_executeSuccessfully() {
        Options options = new Options();
        options.addOption(new Option("v", "version", false, "show version"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(baos));

            formatter.printHelp("cmd", options);
            formatter.printHelp("cmd", options, true);
            formatter.printHelp("cmd", "header", options, "footer");
            formatter.printHelp("cmd", "header", options, "footer", true);
            formatter.printHelp(60, "cmd", "header", options, "footer");
            formatter.printHelp(60, "cmd", "header", options, "footer", true);
        } finally {
            System.setOut(originalOut);
        }

        assertTrue(baos.toString().contains("usage: cmd"));
    }

    // Tests printHelp with 8-argument overload taking PrintWriter
    @Test
    public void testPrintHelp_eightArgOverload_rendersFormattedHelp() {
        Options options = new Options();
        options.addOption(new Option("h", "help", false, "display help"));

        formatter.printHelp(pw, 80, "myapp", "Header", options, 2, 2, "Footer");
        pw.flush();

        String expected = "usage: myapp" + defaultEOL
                + "Header" + defaultEOL
                + "  -h,--help  display help" + defaultEOL
                + "Footer" + defaultEOL;
        assertEquals(expected, out.toString());
    }
}