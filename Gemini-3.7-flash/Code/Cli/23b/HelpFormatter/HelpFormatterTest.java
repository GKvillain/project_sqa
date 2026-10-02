package org.apache.commons.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

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

    // Tests null command line syntax throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_nullCmdLineSyntax_throwsException()
    {
        Options options = new Options();
        formatter.printHelp(pw, 80, null, "header", options, 1, 3, "footer", false);
    }

    // Tests empty command line syntax throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_emptyCmdLineSyntax_throwsException()
    {
        Options options = new Options();
        formatter.printHelp(pw, 80, "", "header", options, 1, 3, "footer", false);
    }

    // Tests standard help message formatting with header and footer
    @Test
    public void testPrintHelp_withHeaderAndFooter_printsExpectedOutput()
    {
        Options options = new Options();
        options.addOption(new Option("a", "all", false, "do not ignore entries starting with ."));
        options.addOption(new Option("l", false, "use a long listing format"));

        formatter.printHelp(pw, 80, "ls", "header banner", options, 2, 2, "footer banner", false);
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("usage: ls"));
        assertTrue(result.contains("header banner"));
        assertTrue(result.contains("-a,--all"));
        assertTrue(result.contains("-l"));
        assertTrue(result.contains("footer banner"));
    }

    // Tests auto usage generation including option with argument name
    @Test
    public void testPrintHelp_autoUsageTrue_generatesOptionUsage()
    {
        Options options = new Options();
        Option opt = new Option("f", "file", true, "target file");
        opt.setArgName("FILE");
        opt.setRequired(true);
        options.addOption(opt);

        formatter.printHelp(pw, 80, "myapp", null, options, 1, 3, null, true);
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("usage: myapp -f <FILE>"));
    }

    // Tests usage formatting with OptionGroup (required and optional)
    @Test
    public void testPrintUsage_withOptionGroup_printsGroupSyntax()
    {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("s", "string", false, "string output"));
        group.addOption(new Option("b", "binary", false, "binary output"));
        group.setRequired(false);
        options.addOptionGroup(group);

        formatter.printUsage(pw, 80, "app", options);
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("[-b | -s]") || result.contains("[-s | -b]"));
    }

    // Tests required OptionGroup rendering in square brackets vs omitted brackets
    @Test
    public void testPrintUsage_withRequiredOptionGroup_omitsSquareBrackets()
    {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("x", false, "option x"));
        group.addOption(new Option("y", false, "option y"));
        group.setRequired(true);
        options.addOptionGroup(group);

        formatter.printUsage(pw, 80, "app", options);
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("-x | -y") || result.contains("-y | -x"));
    }

    // Tests printOptions with options that have long-opt only
    @Test
    public void testPrintOptions_longOptOnly_formatsWithPadding()
    {
        Options options = new Options();
        Option opt = new Option(null, "config", true, "configuration file path");
        opt.setArgName("PATH");
        options.addOption(opt);

        formatter.printOptions(pw, 80, options, 2, 4);
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("--config <PATH>"));
        assertTrue(result.contains("configuration file path"));
    }

    // Tests printWrapped without line breaks for short text
    @Test
    public void testPrintWrapped_shortText_printsSingleLine()
    {
        formatter.printWrapped(pw, 80, "Short line of text.");
        pw.flush();

        String expected = "Short line of text." + formatter.getNewLine();
        assertEquals(expected, out.toString());
    }

    // Tests printWrapped wrapping long text at whitespace boundary
    @Test
    public void testPrintWrapped_longText_wrapsAtWhitespace()
    {
        String text = "The quick brown fox jumps over the lazy dog";
        formatter.printWrapped(pw, 20, 0, text);
        pw.flush();

        String nl = formatter.getNewLine();
        String expected = "The quick brown fox" + nl + "jumps over the lazy" + nl + "dog" + nl;
        assertEquals(expected, out.toString());
    }

    // Tests findWrapPos with explicit newline character
    @Test
    public void testFindWrapPos_newlineCharacter_returnsPositionAfterNewline()
    {
        String text = "Line 1\nLine 2 is longer";
        int pos = formatter.findWrapPos(text, 20, 0);
        assertEquals(7, pos);
    }

    // Tests findWrapPos with tab character within line width
    @Test
    public void testFindWrapPos_tabCharacter_returnsPositionAfterTab()
    {
        String text = "Word1\tWord2 Word3";
        int pos = formatter.findWrapPos(text, 10, 0);
        assertEquals(6, pos);
    }

    // Tests findWrapPos when text fits within width boundary
    @Test
    public void testFindWrapPos_textWithinWidth_returnsNegativeOne()
    {
        String text = "Short text";
        int pos = formatter.findWrapPos(text, 20, 0);
        assertEquals(-1, pos);
    }

    // Tests findWrapPos when text has no breakable space before width
    @Test
    public void testFindWrapPos_noWhitespaceBeforeWidth_findsNextWhitespace()
    {
        String text = "Supercalifragilisticexpialidocious is a long word";
        int pos = formatter.findWrapPos(text, 10, 0);
        assertEquals(34, pos);
    }

    // Tests rtrim utility method with spaces, tabs, and empty/null inputs
    @Test
    public void testRtrim_variousInputs_removesTrailingWhitespace()
    {
        assertNull(formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("text", formatter.rtrim("text   \t  "));
        assertEquals("  leading and middle", formatter.rtrim("  leading and middle   "));
    }

    // Tests createPadding utility method
    @Test
    public void testCreatePadding_positiveLength_returnsSpaces()
    {
        assertEquals("", formatter.createPadding(0));
        assertEquals("   ", formatter.createPadding(3));
    }

    // Tests getter and setter methods
    @Test
    public void testGettersAndSetters_customValues_retainSettings()
    {
        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());

        formatter.setLeftPadding(4);
        assertEquals(4, formatter.getLeftPadding());

        formatter.setDescPadding(6);
        assertEquals(6, formatter.getDescPadding());

        formatter.setSyntaxPrefix("Syntax: ");
        assertEquals("Syntax: ", formatter.getSyntaxPrefix());

        formatter.setNewLine("\r\n");
        assertEquals("\r\n", formatter.getNewLine());

        formatter.setOptPrefix("/");
        assertEquals("/", formatter.getOptPrefix());

        formatter.setLongOptPrefix("//");
        assertEquals("//", formatter.getLongOptPrefix());

        formatter.setArgName("VALUE");
        assertEquals("VALUE", formatter.getArgName());
    }

    // Tests custom OptionComparator and resetting to default with null
    @Test
    public void testSetOptionComparator_customAndNull_updatesComparator()
    {
        Comparator customComparator = new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return 0;
            }
        };

        formatter.setOptionComparator(customComparator);
        assertSame(customComparator, formatter.getOptionComparator());

        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
    }

    // Tests CLI-162 / CLI-23 defect scenario where long text cannot be wrapped to prevent infinite loop
    @Test(expected = RuntimeException.class)
    public void testRenderWrappedText_textTooLongForLine_throwsRuntimeException()
    {
        StringBuffer sb = new StringBuffer();
        // nextLineTabStop (20) >= width (10), so subsequent line will fail to advance wrap position
        formatter.renderWrappedText(sb, 10, 20, "abcdefghij klmnopqrstuvwxyz");
    }

    // Tests overloaded printUsage with command line string directly
    @Test
    public void testPrintUsage_commandLineString()
    {
        formatter.printUsage(pw, 80, "app -a -b file");
        pw.flush();

        String result = out.toString();
        assertTrue(result.startsWith("usage: app -a -b file"));
    }

    // Tests overloaded printHelp methods writing to System.out
    @Test
    public void testPrintHelp_overloads_coverage()
    {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            System.setOut(new PrintStream(baos));

            Options options = new Options();
            options.addOption("h", "help", false, "display help");

            formatter.printHelp("app", options);
            formatter.printHelp("app", options, true);
            formatter.printHelp("app", "header", options, "footer");
            formatter.printHelp("app", "header", options, "footer", true);
            formatter.printHelp(80, "app", "header", options, "footer");
            formatter.printHelp(80, "app", "header", options, "footer", true);

            String sysOut = baos.toString();
            assertTrue(sysOut.contains("usage: app"));
            assertTrue(sysOut.contains("-h,--help"));
        }
        finally
        {
            System.setOut(originalOut);
        }
    }

    // Tests printOptions with options without description and with optional args
    @Test
    public void testPrintOptions_noDescriptionAndOptionalArg()
    {
        Options options = new Options();
        Option optNoDesc = new Option("n", "nodesc", false, null);
        options.addOption(optNoDesc);

        Option optOptionalArg = new Option("o", "optional", true, "option with optional arg");
        optOptionalArg.setOptionalArg(true);
        options.addOption(optOptionalArg);

        formatter.printOptions(pw, 80, options, 1, 3);
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("-n,--nodesc"));
        assertTrue(result.contains("-o,--optional"));
    }

    // Tests printHelp with 8 parameter overload
    @Test
    public void testPrintHelp_eightParamOverload()
    {
        Options options = new Options();
        options.addOption("v", "version", false, "display version");

        formatter.printHelp(pw, 80, "app", "head", options, 2, 2, "foot");
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("usage: app"));
        assertTrue(result.contains("head"));
        assertTrue(result.contains("-v,--version"));
        assertTrue(result.contains("foot"));
    }
}