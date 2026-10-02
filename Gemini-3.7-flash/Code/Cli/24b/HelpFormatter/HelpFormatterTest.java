package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

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

    // Tests default property values and their getters/setters
    @Test
    public void testGettersAndSetters_customValues_returnSetValues()
    {
        assertEquals(HelpFormatter.DEFAULT_WIDTH, formatter.getWidth());
        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());

        assertEquals(HelpFormatter.DEFAULT_LEFT_PAD, formatter.getLeftPadding());
        formatter.setLeftPadding(4);
        assertEquals(4, formatter.getLeftPadding());

        assertEquals(HelpFormatter.DEFAULT_DESC_PAD, formatter.getDescPadding());
        formatter.setDescPadding(5);
        assertEquals(5, formatter.getDescPadding());

        assertEquals(HelpFormatter.DEFAULT_SYNTAX_PREFIX, formatter.getSyntaxPrefix());
        formatter.setSyntaxPrefix("Syntax: ");
        assertEquals("Syntax: ", formatter.getSyntaxPrefix());

        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());

        assertEquals(HelpFormatter.DEFAULT_OPT_PREFIX, formatter.getOptPrefix());
        formatter.setOptPrefix("/");
        assertEquals("/", formatter.getOptPrefix());

        assertEquals(HelpFormatter.DEFAULT_LONG_OPT_PREFIX, formatter.getLongOptPrefix());
        formatter.setLongOptPrefix("//");
        assertEquals("//", formatter.getLongOptPrefix());

        assertEquals(HelpFormatter.DEFAULT_ARG_NAME, formatter.getArgName());
        formatter.setArgName("value");
        assertEquals("value", formatter.getArgName());

        Comparator customComp = new Comparator() {
            public int compare(Object o1, Object o2) {
                return 0;
            }
        };
        formatter.setOptionComparator(customComp);
        assertSame(customComp, formatter.getOptionComparator());

        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
    }

    // Tests printHelp with null or empty syntax throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_nullCmdLineSyntax_throwsIllegalArgumentException()
    {
        formatter.printHelp(pw, 80, null, "header", new Options(), 1, 3, "footer", true);
    }

    // Tests printHelp with empty syntax throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_emptyCmdLineSyntax_throwsIllegalArgumentException()
    {
        formatter.printHelp(pw, 80, "", "header", new Options(), 1, 3, "footer", true);
    }

    // Tests printHelp normal execution with header, footer, and autoUsage
    @Test
    public void testPrintHelp_withHeaderAndFooter_outputsCorrectHelp()
    {
        Options options = new Options();
        options.addOption("h", "help", false, "display help");
        options.addOption(OptionBuilder.hasArg().withLongOpt("file").withDescription("input file").create('f'));

        formatter.setNewLine("\n");
        formatter.printHelp(pw, 80, "myapp", "Header banner", options, 2, 2, "Footer banner", true);
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("usage: myapp"));
        assertTrue(result.contains("Header banner"));
        assertTrue(result.contains("-h,--help"));
        assertTrue(result.contains("display help"));
        assertTrue(result.contains("-f,--file <arg>"));
        assertTrue(result.contains("input file"));
        assertTrue(result.contains("Footer banner"));
    }

    // Tests printHelp shorthand convenience overloads writing to System.out
    @Test
    public void testPrintHelp_systemOutOverloads_executeWithoutException()
    {
        Options options = new Options();
        options.addOption("v", "version", false, "print version");

        formatter.printHelp("app", options);
        formatter.printHelp("app", options, true);
        formatter.printHelp("app", "header", options, "footer");
        formatter.printHelp("app", "header", options, "footer", true);
        formatter.printHelp(80, "app", "header", options, "footer");
        formatter.printHelp(80, "app", "header", options, "footer", true);
    }

    // Tests printUsage with simple command line syntax string
    @Test
    public void testPrintUsage_simpleSyntax_printsFormattedUsage()
    {
        formatter.setNewLine("\n");
        formatter.printUsage(pw, 80, "myapp -a -b -c");
        pw.flush();

        assertEquals("usage: myapp -a -b -c\n", out.toString());
    }

    // Tests printUsage with Options including OptionGroup and required options
    @Test
    public void testPrintUsage_withOptionGroup_printsGroupSyntax()
    {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha"));
        group.addOption(new Option("b", "beta"));
        group.setRequired(false);
        options.addOptionGroup(group);

        Option reqOpt = new Option("r", "required-opt");
        reqOpt.setRequired(true);
        options.addOption(reqOpt);

        formatter.setNewLine("\n");
        formatter.printUsage(pw, 80, "myapp", options);
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("usage: myapp"));
        assertTrue(result.contains("[-a | -b]"));
        assertTrue(result.contains("-r"));
    }

    // Tests printUsage with required OptionGroup
    @Test
    public void testPrintUsage_requiredOptionGroup_printsWithoutSquareBrackets()
    {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("x", "exclusive1"));
        group.addOption(new Option("y", "exclusive2"));
        group.setRequired(true);
        options.addOptionGroup(group);

        formatter.setNewLine("\n");
        formatter.printUsage(pw, 80, "myapp", options);
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("-x | -y"));
        assertFalse(result.contains("[-x | -y]"));
    }

    // Tests renderOptions with long-opt-only options and options without descriptions
    @Test
    public void testRenderOptions_longOptOnlyAndNoDesc_rendersCorrectly()
    {
        Options options = new Options();
        Option longOnly = OptionBuilder.withLongOpt("long-only").hasArg().withArgName("NAME").create();
        options.addOption(longOnly);
        options.addOption(new Option("n", null));

        formatter.setNewLine("\n");
        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 1, 3);

        String rendered = sb.toString();
        assertTrue(rendered.contains("--long-only <NAME>"));
        assertTrue(rendered.contains("-n"));
    }

    // Tests renderWrappedText when text wraps across multiple lines with nextLineTabStop
    @Test
    public void testRenderWrappedText_multilineText_wrapsAndIndents()
    {
        formatter.setNewLine("\n");
        StringBuffer sb = new StringBuffer();
        String text = "This is a long description text that should be wrapped onto multiple lines properly.";
        formatter.renderWrappedText(sb, 30, 4, text);

        String[] lines = sb.toString().split("\n");
        assertTrue(lines.length > 1);
        for (int i = 1; i < lines.length; i++)
        {
            assertTrue(lines[i].startsWith("    "));
        }
    }

    // Tests renderWrappedText with narrow width where nextLineTabStop >= width throwing exception
    @Test(expected = IllegalStateException.class)
    public void testRenderWrappedText_tabStopGreaterOrEqualToWidth_throwsIllegalStateException()
    {
        formatter.setNewLine("\n");
        StringBuffer sb = new StringBuffer();
        String text = "Short text";
        formatter.renderWrappedText(sb, 10, 10, text);
    }

    // Tests findWrapPos with explicit newlines and tabs
    @Test
    public void testFindWrapPos_newlineAndTab_returnsCorrectWrapPosition()
    {
        String textWithNewline = "Line one\nLine two";
        int posNewline = formatter.findWrapPos(textWithNewline, 20, 0);
        assertEquals(9, posNewline);

        String textWithTab = "Line\tone Line two";
        int posTab = formatter.findWrapPos(textWithTab, 20, 0);
        assertEquals(5, posTab);

        String shortText = "Short";
        int posShort = formatter.findWrapPos(shortText, 20, 0);
        assertEquals(-1, posShort);
    }

    // Tests findWrapPos when wrapping position is within space boundary
    @Test
    public void testFindWrapPos_spaceBoundary_returnsSpacePosition()
    {
        String text = "The quick brown fox jumps over the lazy dog";
        int pos = formatter.findWrapPos(text, 15, 0);
        assertEquals(9, pos);
    }

    // Tests createPadding method for edge cases
    @Test
    public void testCreatePadding_positiveAndZeroLength_returnsCorrectSpaces()
    {
        assertEquals("", formatter.createPadding(0));
        assertEquals("   ", formatter.createPadding(3));
    }

    // Tests rtrim with whitespace, null, and empty string
    @Test
    public void testRtrim_variousInputs_trimsTrailingWhitespaceOnly()
    {
        assertNull(formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("  abc", formatter.rtrim("  abc   \t \n "));
        assertEquals("abc", formatter.rtrim("abc"));
    }

    // Tests printWrapped convenience methods
    @Test
    public void testPrintWrapped_withPrintWriter_outputsWrappedLines()
    {
        formatter.setNewLine("\n");
        formatter.printWrapped(pw, 80, "First line of text");
        formatter.printWrapped(pw, 80, 5, "Second line of text with tab stop");
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("First line of text"));
        assertTrue(result.contains("Second line of text with tab stop"));
    }

    // Tests printHelp overload without autoUsage boolean
    @Test
    public void testPrintHelp_withoutAutoUsage_executesCorrectly()
    {
        Options options = new Options();
        options.addOption("d", "debug", false, "turn on debugging");

        formatter.setNewLine("\n");
        formatter.printHelp(pw, 80, "myapp", "Header", options, 1, 3, "Footer");
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("usage: myapp"));
        assertTrue(result.contains("Header"));
        assertTrue(result.contains("-d,--debug"));
        assertTrue(result.contains("Footer"));
    }

    // Tests printWrapped overloads printing to System.out
    @Test
    public void testPrintWrapped_systemOutOverloads_executeWithoutException()
    {
        formatter.printWrapped("Sample text for default width");
        formatter.printWrapped(80, "Sample text for 80 width");
        formatter.printWrapped(80, 4, "Sample text for 80 width with tab stop");
    }

    // Tests renderWrappedText when single word is longer than specified width
    @Test
    public void testRenderWrappedText_wordLongerThanWidth_breaksWordAtWidth()
    {
        formatter.setNewLine("\n");
        StringBuffer sb = new StringBuffer();
        String veryLongWord = "Supercalifragilisticexpialidocious";
        formatter.renderWrappedText(sb, 10, 0, veryLongWord);

        String[] lines = sb.toString().split("\n");
        assertTrue(lines.length > 1);
        assertEquals("Supercalif", lines[0]);
    }

    // Tests findWrapPos with carriage return and wrap pos reaching end of text
    @Test
    public void testFindWrapPos_carriageReturnAndEndOfText_returnsExpectedPositions()
    {
        String textWithCR = "Line1\rLine2";
        int posCR = formatter.findWrapPos(textWithCR, 10, 0);
        assertEquals(6, posCR);

        String textExact = "ExactFit";
        int posExact = formatter.findWrapPos(textExact, 8, 0);
        assertEquals(-1, posExact);

        String noSpaceText = "123456789012345";
        int posNoSpace = formatter.findWrapPos(noSpaceText, 8, 0);
        assertEquals(-1, posNoSpace);
    }

    // Tests printUsage with optional arguments, long-only options, and multiple args
    @Test
    public void testPrintUsage_variousOptionArgConfigurations_formatsCorrectly()
    {
        Options options = new Options();

        Option optWithOptionalArg = OptionBuilder.withLongOpt("opt-arg")
                .hasOptionalArg()
                .withArgName("OPTIONAL")
                .create('o');
        options.addOption(optWithOptionalArg);

        Option longOnlyReq = OptionBuilder.withLongOpt("config")
                .hasArg()
                .withArgName("FILE")
                .isRequired()
                .create();
        options.addOption(longOnlyReq);

        formatter.setNewLine("\n");
        formatter.printUsage(pw, 120, "app", options);
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("[-o [<OPTIONAL>]]"));
        assertTrue(result.contains("--config <FILE>"));
    }

    // Tests default OptionComparator sorting behavior
    @Test
    public void testOptionComparator_defaultSorting_sortsByKeyCaseInsensitively()
    {
        Options options = new Options();
        options.addOption("z", "zebra", false, "z option");
        options.addOption("a", "apple", false, "a option");
        options.addOption("M", "monkey", false, "m option");

        formatter.setNewLine("\n");
        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 2, 2);

        String rendered = sb.toString();
        int posA = rendered.indexOf("-a");
        int posM = rendered.indexOf("-M");
        int posZ = rendered.indexOf("-z");

        assertTrue(posA < posM);
        assertTrue(posM < posZ);
    }
}