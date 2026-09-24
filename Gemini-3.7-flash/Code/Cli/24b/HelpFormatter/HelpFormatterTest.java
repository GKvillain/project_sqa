package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HelpFormatterTest
{
    private HelpFormatter formatter;
    private String EOL;

    @Before
    public void setUp()
    {
        formatter = new HelpFormatter();
        EOL = formatter.getNewLine();
    }

    // Tests default property values and getters/setters
    @Test
    public void testGettersAndSetters_customValues_updatedCorrectly()
    {
        assertEquals(HelpFormatter.DEFAULT_WIDTH, formatter.getWidth());
        assertEquals(HelpFormatter.DEFAULT_LEFT_PAD, formatter.getLeftPadding());
        assertEquals(HelpFormatter.DEFAULT_DESC_PAD, formatter.getDescPadding());
        assertEquals(HelpFormatter.DEFAULT_SYNTAX_PREFIX, formatter.getSyntaxPrefix());
        assertEquals(HelpFormatter.DEFAULT_OPT_PREFIX, formatter.getOptPrefix());
        assertEquals(HelpFormatter.DEFAULT_LONG_OPT_PREFIX, formatter.getLongOptPrefix());
        assertEquals(HelpFormatter.DEFAULT_ARG_NAME, formatter.getArgName());
        assertNotNull(formatter.getOptionComparator());

        formatter.setWidth(100);
        formatter.setLeftPadding(2);
        formatter.setDescPadding(5);
        formatter.setSyntaxPrefix("Usage: ");
        formatter.setNewLine("\n");
        formatter.setOptPrefix("+");
        formatter.setLongOptPrefix("++");
        formatter.setArgName("parameter");

        assertEquals(100, formatter.getWidth());
        assertEquals(2, formatter.getLeftPadding());
        assertEquals(5, formatter.getDescPadding());
        assertEquals("Usage: ", formatter.getSyntaxPrefix());
        assertEquals("\n", formatter.getNewLine());
        assertEquals("+", formatter.getOptPrefix());
        assertEquals("++", formatter.getLongOptPrefix());
        assertEquals("parameter", formatter.getArgName());
    }

    // Tests setOptionComparator with custom comparator and null resetting to default
    @Test
    public void testSetOptionComparator_nullValue_resetsToDefault()
    {
        Comparator customComp = new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return 0;
            }
        };

        formatter.setOptionComparator(customComp);
        assertEquals(customComp, formatter.getOptionComparator());

        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
    }

    // Tests createPadding method with various lengths
    @Test
    public void testCreatePadding_variousLengths_returnsExpectedSpaces()
    {
        assertEquals("", formatter.createPadding(0));
        assertEquals(" ", formatter.createPadding(1));
        assertEquals("   ", formatter.createPadding(3));
    }

    // Tests rtrim method with null, empty, trailing whitespace, and normal string
    @Test
    public void testRtrim_variousInputs_trimmedCorrectly()
    {
        assertEquals(null, formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("test", formatter.rtrim("test"));
        assertEquals("test", formatter.rtrim("test   \t\r\n"));
        assertEquals("  test", formatter.rtrim("  test   "));
    }

    // Tests findWrapPos for newline, tab, and normal space wrapping
    @Test
    public void testFindWrapPos_boundaryAndSpecialCharacters_correctWrapPositions()
    {
        // Line fits entirely within width
        assertEquals(-1, formatter.findWrapPos("short text", 20, 0));

        // Line contains newline within width
        assertEquals(5, formatter.findWrapPos("line1\nline2", 10, 0));

        // Line contains tab within width
        assertEquals(5, formatter.findWrapPos("line1\tline2", 10, 0));

        // Line wraps at last space before width
        assertEquals(5, formatter.findWrapPos("hello world again", 8, 0));

        // Line with no whitespace before width looks after width
        assertEquals(11, formatter.findWrapPos("supercalifragilistic expialidocious", 5, 0));
    }

    // Tests renderWrappedText with text that fits on a single line
    @Test
    public void testRenderWrappedText_fitsSingleLine_appendsWithoutWrap()
    {
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 80, 0, "Simple single line text.");
        assertEquals("Simple single line text.", sb.toString());
    }

    // Tests renderWrappedText throwing IllegalStateException when nextLineTabStop >= width
    @Test(expected = IllegalStateException.class)
    public void testRenderWrappedText_tabStopGreaterOrEqualWidth_throwsException()
    {
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 10, 10, "This should fail because tabStop >= width");
    }

    // Tests renderWrappedText multi-line wrapping with nextLineTabStop indentation (Defects4J Cli-24 regression)
    @Test
    public void testRenderWrappedText_multiLineWithIndent_wrappedAndIndentedCorrectly()
    {
        StringBuffer sb = new StringBuffer();
        int width = 20;
        int nextLineTabStop = 6;
        String text = "first line text that is very long and should wrap multiple times across several lines";

        formatter.renderWrappedText(sb, width, nextLineTabStop, text);

        String expected = "first line text that" + EOL +
                          "      is very long" + EOL +
                          "      and should" + EOL +
                          "      wrap multiple" + EOL +
                          "      times across" + EOL +
                          "      several lines";
        assertEquals(expected, sb.toString());
    }

    // Tests printWrapped method output to PrintWriter
    @Test
    public void testPrintWrapped_withPrintWriter_writesFormattedText()
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printWrapped(pw, 20, "This is wrapped text that exceeds width.");
        pw.flush();

        String expected = "This is wrapped text" + EOL +
                          "that exceeds width." + EOL;
        assertEquals(expected, sw.toString());
    }

    // Tests printUsage with simple command line syntax
    @Test
    public void testPrintUsage_simpleSyntax_formatsCorrectly()
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printUsage(pw, 80, "myapp -a -b <file>");
        pw.flush();

        String expected = "usage: myapp -a -b <file>" + EOL;
        assertEquals(expected, sw.toString());
    }

    // Tests printUsage with Options and OptionGroup
    @Test
    public void testPrintUsage_withOptionsAndOptionGroup_formatsCorrectly()
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Options options = new Options();
        Option optA = new Option("a", "alpha", false, "Option Alpha");
        Option optB = new Option("b", true, "Option Beta");
        optB.setArgName("val");

        OptionGroup group = new OptionGroup();
        Option optC = new Option("c", false, "Option Charlie");
        Option optD = new Option("d", false, "Option Delta");
        group.addOption(optC);
        group.addOption(optD);
        group.setRequired(false);

        options.addOption(optA);
        options.addOption(optB);
        options.addOptionGroup(group);

        formatter.printUsage(pw, 80, "app", options);
        pw.flush();

        String expected = "usage: app [-a] [-b <val>] [-c | -d]" + EOL;
        assertEquals(expected, sw.toString());
    }

    // Tests renderOptions with short, long, arguments, and required options
    @Test
    public void testRenderOptions_variousOptionTypes_renderedWithPadding()
    {
        Options options = new Options();
        Option optA = new Option("a", "alpha", false, "Alpha description");
        Option optB = new Option(null, "beta", true, "Beta description");
        optB.setArgName("num");
        Option optC = new Option("c", false, null);

        options.addOption(optA);
        options.addOption(optB);
        options.addOption(optC);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 1, 3);

        String result = sb.toString();
        assertTrue(result.contains("-a,--alpha"));
        assertTrue(result.contains("--beta <num>"));
        assertTrue(result.contains("-c"));
        assertTrue(result.contains("Alpha description"));
        assertTrue(result.contains("Beta description"));
    }

    // Tests printHelp with null or empty cmdLineSyntax throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_nullCmdLineSyntax_throwsException()
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, null, "header", new Options(), 1, 3, "footer", false);
    }

    // Tests printHelp with empty cmdLineSyntax throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_emptyCmdLineSyntax_throwsException()
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "", "header", new Options(), 1, 3, "footer", false);
    }

    // Tests printHelp full workflow including header, footer, autoUsage, and options
    @Test
    public void testPrintHelp_fullWorkflow_formatsCompleteHelpText()
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Options options = new Options();
        options.addOption("h", "help", false, "display help");

        formatter.printHelp(pw, 80, "myapp", "Header banner", options, 2, 4, "Footer banner", true);
        pw.flush();

        String output = sw.toString();
        assertTrue(output.contains("usage: myapp [-h]"));
        assertTrue(output.contains("Header banner"));
        assertTrue(output.contains("-h,--help"));
        assertTrue(output.contains("display help"));
        assertTrue(output.contains("Footer banner"));
    }

    // Tests printHelp overloaded methods delegating to System.out without exceptions
    @Test
    public void testPrintHelp_stdoutDelegates_executesSuccessfully()
    {
        Options options = new Options();
        options.addOption("v", "version", false, "show version");

        formatter.printHelp("app", options);
        formatter.printHelp("app", options, true);
        formatter.printHelp("app", "header", options, "footer");
        formatter.printHelp("app", "header", options, "footer", true);
        formatter.printHelp(80, "app", "header", options, "footer");
        formatter.printHelp(80, "app", "header", options, "footer", false);
    }
}