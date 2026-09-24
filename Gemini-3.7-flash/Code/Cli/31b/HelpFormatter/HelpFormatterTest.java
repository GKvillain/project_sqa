package org.apache.commons.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class HelpFormatterTest
{
    private HelpFormatter formatter;
    private StringWriter out;
    private PrintWriter pw;

    @Before
    public void setUp()
    {
        formatter = new HelpFormatter();
        out = new StringWriter();
        pw = new PrintWriter(out);
    }

    // Tests getter and setter methods for default properties
    @Test
    public void testGettersAndSetters_customValues_returnsUpdatedValues()
    {
        formatter.setWidth(80);
        assertEquals(80, formatter.getWidth());

        formatter.setLeftPadding(2);
        assertEquals(2, formatter.getLeftPadding());

        formatter.setDescPadding(4);
        assertEquals(4, formatter.getDescPadding());

        formatter.setSyntaxPrefix("Syntax: ");
        assertEquals("Syntax: ", formatter.getSyntaxPrefix());

        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());

        formatter.setOptPrefix("--");
        assertEquals("--", formatter.getOptPrefix());

        formatter.setLongOptPrefix("---");
        assertEquals("---", formatter.getLongOptPrefix());

        formatter.setLongOptSeparator("=");
        assertEquals("=", formatter.getLongOptSeparator());

        formatter.setArgName("argument");
        assertEquals("argument", formatter.getArgName());
    }

    // Tests setting comparator to null and non-null
    @Test
    public void testSetOptionComparator_nullAndCustomComparator_updatesCorrectly()
    {
        Comparator customComparator = new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return ((Option) o1).getKey().compareTo(((Option) o2).getKey());
            }
        };

        formatter.setOptionComparator(customComparator);
        assertSame(customComparator, formatter.getOptionComparator());

        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
        assertNotSame(customComparator, formatter.getOptionComparator());
    }

    // Tests exception when cmdLineSyntax is null
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_nullCmdLineSyntax_throwsIllegalArgumentException()
    {
        Options options = new Options();
        formatter.printHelp(pw, 80, null, "header", options, 1, 3, "footer", false);
    }

    // Tests exception when cmdLineSyntax is empty
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_emptyCmdLineSyntax_throwsIllegalArgumentException()
    {
        Options options = new Options();
        formatter.printHelp(pw, 80, "", "header", options, 1, 3, "footer", false);
    }

    // Tests printHelp with header, footer, and simple options
    @Test
    public void testPrintHelp_validHeaderFooterAndOptions_outputsFormattedHelp()
    {
        Options options = new Options();
        options.addOption("h", "help", false, "print this message");
        options.addOption("v", "version", false, "print version info");

        formatter.printHelp(pw, 80, "myapp", "Header banner", options, 1, 3, "Footer banner", false);
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("usage: myapp"));
        assertTrue(result.contains("Header banner"));
        assertTrue(result.contains("-h,--help"));
        assertTrue(result.contains("-v,--version"));
        assertTrue(result.contains("Footer banner"));
    }

    // Tests autoUsage in printHelp
    @Test
    public void testPrintHelp_autoUsageTrue_generatesUsageStatement()
    {
        Options options = new Options();
        Option optA = new Option("a", "all", false, "do all");
        optA.setRequired(true);
        options.addOption(optA);

        formatter.printHelp(pw, 80, "myapp", null, options, 1, 3, null, true);
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("usage: myapp -a"));
    }

    // Tests printUsage with simple options and arguments
    @Test
    public void testPrintUsage_optionsWithArgs_formatsCorrectly()
    {
        Options options = new Options();
        Option fileOpt = new Option("f", "file", true, "input file");
        fileOpt.setArgName("FILENAME");
        options.addOption(fileOpt);

        formatter.printUsage(pw, 80, "app", options);
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("usage: app [-f <FILENAME>]"));
    }

    // Tests printUsage with OptionGroup (required and optional)
    @Test
    public void testPrintUsage_optionGroup_formatsGroupSyntax()
    {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "option a"));
        group.addOption(new Option("b", "option b"));
        group.setRequired(false);
        options.addOptionGroup(group);

        formatter.printUsage(pw, 80, "myapp", options);
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("[-a | -b]"));
    }

    // Tests printUsage with required OptionGroup
    @Test
    public void testPrintUsage_requiredOptionGroup_formatsWithoutOuterSquareBrackets()
    {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "option a"));
        group.addOption(new Option("b", "option b"));
        group.setRequired(true);
        options.addOptionGroup(group);

        formatter.printUsage(pw, 80, "myapp", options);
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("-a | -b"));
        assertFalse(result.contains("[-a | -b]"));
    }

    // Tests renderOptions with long-only option and empty argName
    @Test
    public void testRenderOptions_longOptOnlyAndEmptyArgName_rendersProperPadding()
    {
        Options options = new Options();
        Option opt = new Option(null, "longonly", true, "long option only");
        opt.setArgName("");
        options.addOption(opt);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 1, 3);

        String result = sb.toString();
        assertTrue(result.contains("--longonly"));
        assertTrue(result.contains("long option only"));
    }

    // Tests renderWrappedText wrapping text across multiple lines
    @Test
    public void testRenderWrappedText_textLongerThanWidth_wrapsWithPadding()
    {
        StringBuffer sb = new StringBuffer();
        String longText = "This is a long description text that should be wrapped across multiple lines properly.";
        formatter.renderWrappedText(sb, 30, 4, longText);

        String result = sb.toString();
        String[] lines = result.split(formatter.getNewLine());
        assertTrue(lines.length > 1);
        for (int i = 1; i < lines.length; i++)
        {
            assertTrue(lines[i].startsWith("    "));
        }
    }

    // Tests renderWrappedText when nextLineTabStop is greater than or equal to width
    @Test
    public void testRenderWrappedText_tabStopGreaterThanWidth_resetsTabStopToOne()
    {
        StringBuffer sb = new StringBuffer();
        String text = "Short first line that wraps because width is very small indeed";
        formatter.renderWrappedText(sb, 10, 15, text);

        String result = sb.toString();
        String[] lines = result.split(formatter.getNewLine());
        assertTrue(lines.length > 1);
    }

    // Tests findWrapPos with newline and tab characters before wrap width
    @Test
    public void testFindWrapPos_newlineAndTab_returnsPositionPlusOne()
    {
        String textNewline = "hello\nworld this is long";
        int posNewline = formatter.findWrapPos(textNewline, 20, 0);
        assertEquals(6, posNewline);

        String textTab = "hello\tworld this is long";
        int posTab = formatter.findWrapPos(textTab, 20, 0);
        assertEquals(6, posTab);
    }

    // Tests findWrapPos when remaining text fits within width
    @Test
    public void testFindWrapPos_textFitsWithinWidth_returnsMinusOne()
    {
        String text = "short text";
        int pos = formatter.findWrapPos(text, 50, 0);
        assertEquals(-1, pos);
    }

    // Tests findWrapPos with long word exceeding width (defect regression check)
    @Test
    public void testFindWrapPos_longWordWithoutSpace_handlesBoundaryWithoutOutOfBounds()
    {
        String longWord = "abcdefghijklmnopqrstuvwxyz";
        int pos = formatter.findWrapPos(longWord, 10, 0);
        assertEquals(-1, pos);
    }

    // Tests createPadding method
    @Test
    public void testCreatePadding_positiveLength_returnsExactSpaces()
    {
        assertEquals("", formatter.createPadding(0));
        assertEquals(" ", formatter.createPadding(1));
        assertEquals("   ", formatter.createPadding(3));
    }

    // Tests rtrim method with null, empty, trailing whitespace, and no whitespace
    @Test
    public void testRtrim_variousStrings_trimsTrailingWhitespaceOnly()
    {
        assertNull(formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("  abc", formatter.rtrim("  abc  \t\n"));
        assertEquals("abc", formatter.rtrim("abc"));
    }

    // Tests printUsage single string overload
    @Test
    public void testPrintUsage_singleStringSyntax_printsFormattedSyntax()
    {
        formatter.printUsage(pw, 80, "myapp [options] <file>");
        pw.flush();

        String result = out.toString();
        assertTrue(result.startsWith("usage: myapp [options] <file>"));
    }

    // Tests printHelp overloads that print to System.out and delegate methods
    @Test
    public void testPrintHelp_convenienceOverloads_executesWithoutError()
    {
        Options options = new Options();
        options.addOption("h", "help", false, "display help");

        formatter.printHelp("app", options);
        formatter.printHelp("app", options, true);
        formatter.printHelp("app", "header", options, "footer");
        formatter.printHelp("app", "header", options, "footer", true);
        formatter.printHelp(80, "app", "header", options, "footer");
        formatter.printHelp(80, "app", "header", options, "footer", true);

        formatter.printHelp(pw, 80, "app", "header", options, 2, 4, "footer");
        pw.flush();
        assertTrue(out.toString().contains("usage: app"));
    }

    // Tests printWrapped overloads
    @Test
    public void testPrintWrapped_standardAndWithTabStop_printsFormattedText()
    {
        formatter.printWrapped(pw, 80, "Simple wrapped text");
        formatter.printWrapped(pw, 40, 4, "Wrapped text with custom tab stop that will be indented on new lines if it wraps");
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("Simple wrapped text"));
        assertTrue(result.contains("Wrapped text with custom tab stop"));
    }

    // Tests findWrapPos with carriage return (\r) and space wrapping
    @Test
    public void testFindWrapPos_carriageReturnAndSpaces_findsCorrectPos()
    {
        String textCr = "hello\rworld this is long text";
        int posCr = formatter.findWrapPos(textCr, 20, 0);
        assertEquals(6, posCr);

        String textSpace = "hello world this is a long text that should wrap on space";
        int posSpace = formatter.findWrapPos(textSpace, 15, 0);
        assertEquals(11, posSpace);

        // Test startPos past the end of string
        int posEnd = formatter.findWrapPos("short", 10, 10);
        assertEquals(-1, posEnd);
    }

    // Tests renderOptions with optional arguments and null descriptions
    @Test
    public void testRenderOptions_optionalArgsAndNullDescription_formatsCorrectly()
    {
        Options options = new Options();

        Option opt1 = new Option("o", "optional", true, null);
        opt1.setOptionalArg(true);
        opt1.setArgName("val");
        options.addOption(opt1);

        Option opt2 = new Option("s", false, "short option only with description");
        options.addOption(opt2);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 2, 2);

        String result = sb.toString();
        assertTrue(result.contains("-o,--optional[=val]"));
        assertTrue(result.contains("-s"));
        assertTrue(result.contains("short option only with description"));
    }

    // Tests appendOption in usage with optional argument and custom argName
    @Test
    public void testPrintUsage_optionalArgsInUsage_formatsBracketedValue()
    {
        Options options = new Options();
        Option opt = new Option("f", "file", true, "input file");
        opt.setOptionalArg(true);
        opt.setArgName("FILENAME");
        options.addOption(opt);

        formatter.printUsage(pw, 80, "app", options);
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("[-f [<FILENAME>]]"));
    }

    // Tests default OptionComparator ordering
    @Test
    public void testOptionComparator_defaultComparator_comparesOptionsByKeyCaseInsensitive()
    {
        Comparator comp = formatter.getOptionComparator();
        assertNotNull(comp);

        Option optA = new Option("a", "alpha", false, "desc a");
        Option optB = new Option("B", "bravo", false, "desc b");

        assertTrue(comp.compare(optA, optB) < 0);
        assertTrue(comp.compare(optB, optA) > 0);
        assertEquals(0, comp.compare(optA, optA));
    }
}