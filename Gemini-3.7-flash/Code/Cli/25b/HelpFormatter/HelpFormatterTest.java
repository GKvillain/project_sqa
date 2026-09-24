package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

    // Tests getters and setters for all configurable properties
    @Test
    public void testGettersAndSetters_customValues_returnsConfiguredValues()
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

        formatter.setOptPrefix("/");
        assertEquals("/", formatter.getOptPrefix());

        formatter.setLongOptPrefix("//");
        assertEquals("//", formatter.getLongOptPrefix());

        formatter.setArgName("value");
        assertEquals("value", formatter.getArgName());

        Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                return 0;
            }
        };
        formatter.setOptionComparator(comp);
        assertEquals(comp, formatter.getOptionComparator());

        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
    }

    // Tests printHelp with null or empty cmdLineSyntax throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_nullCmdLineSyntax_throwsIllegalArgumentException()
    {
        Options options = new Options();
        formatter.printHelp(pw, 80, null, "header", options, 1, 3, "footer", false);
    }

    // Tests printHelp with empty cmdLineSyntax throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_emptyCmdLineSyntax_throwsIllegalArgumentException()
    {
        Options options = new Options();
        formatter.printHelp(pw, 80, "", "header", options, 1, 3, "footer", false);
    }

    // Tests printHelp with full options, header, footer, and autoUsage enabled
    @Test
    public void testPrintHelp_validOptionsWithHeaderAndFooter_outputsFormattedHelp()
    {
        Options options = new Options();
        options.addOption(new Option("a", "alpha", false, "Description for alpha"));
        options.addOption(new Option("b", "beta", true, "Description for beta"));

        formatter.printHelp(pw, 80, "myapp", "Header banner", options, 2, 4, "Footer banner", true);
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("usage: myapp"));
        assertTrue(result.contains("Header banner"));
        assertTrue(result.contains("-a,--alpha"));
        assertTrue(result.contains("-b,--beta <arg>"));
        assertTrue(result.contains("Description for alpha"));
        assertTrue(result.contains("Description for beta"));
        assertTrue(result.contains("Footer banner"));
    }

    // Tests printHelp without autoUsage and with empty header/footer
    @Test
    public void testPrintHelp_noAutoUsageAndEmptyHeaderFooter_printsOnlyUsageAndOptions()
    {
        Options options = new Options();
        options.addOption(new Option("h", "help", false, "display help"));

        formatter.printHelp(pw, 80, "myapp -h", "   ", options, 1, 3, "", false);
        pw.flush();

        String result = out.toString();
        assertTrue(result.startsWith("usage: myapp -h"));
        assertTrue(result.contains("-h,--help"));
        assertTrue(result.contains("display help"));
    }

    // Tests printUsage with OptionGroup containing required and non-required groups
    @Test
    public void testPrintUsage_withOptionGroup_formatsGroupCorrectly()
    {
        OptionGroup requiredGroup = new OptionGroup();
        requiredGroup.setRequired(true);
        requiredGroup.addOption(new Option("x", "Option X"));
        requiredGroup.addOption(new Option("y", "Option Y"));

        OptionGroup optionalGroup = new OptionGroup();
        optionalGroup.setRequired(false);
        optionalGroup.addOption(new Option("m", "Option M"));
        optionalGroup.addOption(new Option("n", "Option N"));

        Options options = new Options();
        options.addOptionGroup(requiredGroup);
        options.addOptionGroup(optionalGroup);

        formatter.printUsage(pw, 80, "myapp", options);
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("-m | -n"));
        assertTrue(result.contains("-x | -y"));
        assertTrue(result.contains("[-m | -n]"));
    }

    // Tests printUsage with single Option having custom arg name and required status
    @Test
    public void testPrintUsage_requiredOptionWithArgName_formatsProperly()
    {
        Option opt = new Option("f", "file", true, "target file");
        opt.setRequired(true);
        opt.setArgName("FILE");

        Option optLongOnly = new Option(null, "verbose", false, "verbose output");
        optLongOnly.setRequired(false);

        Options options = new Options();
        options.addOption(opt);
        options.addOption(optLongOnly);

        formatter.printUsage(pw, 80, "app", options);
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("-f <FILE>"));
        assertTrue(result.contains("[--verbose]"));
    }

    // Tests renderOptions with long-only option and option without description
    @Test
    public void testRenderOptions_longOptOnlyAndNoDescription_rendersProperly()
    {
        Options options = new Options();
        Option longOnly = new Option(null, "config", true, null);
        options.addOption(longOnly);

        formatter.printOptions(pw, 80, options, 2, 2);
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("--config"));
    }

    // Tests renderOptions with option having arg without argName
    @Test
    public void testRenderOptions_optionWithArgButEmptyArgName_rendersProperly()
    {
        Options options = new Options();
        Option opt = new Option("c", "count", true, "number of items");
        opt.setArgName(null);
        options.addOption(opt);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 1, 3);

        String result = sb.toString();
        assertTrue(result.contains("-c,--count"));
        assertTrue(result.contains("number of items"));
    }

    // Tests findWrapPos when text contains newline character before width
    @Test
    public void testFindWrapPos_newlineWithinWidth_returnsNewlinePosition()
    {
        String text = "Line one\nLine two";
        int pos = formatter.findWrapPos(text, 20, 0);
        assertEquals(9, pos);
    }

    // Tests findWrapPos when text contains tab character before width
    @Test
    public void testFindWrapPos_tabWithinWidth_returnsTabPosition()
    {
        String text = "Tab\there";
        int pos = formatter.findWrapPos(text, 10, 0);
        assertEquals(4, pos);
    }

    // Tests findWrapPos when startPos + width >= text length
    @Test
    public void testFindWrapPos_textShorterThanWidth_returnsNegativeOne()
    {
        String text = "Short text";
        int pos = formatter.findWrapPos(text, 50, 0);
        assertEquals(-1, pos);
    }

    // Tests findWrapPos with normal space wrapping before width boundary
    @Test
    public void testFindWrapPos_normalWrapping_returnsLastSpaceBeforeWidth()
    {
        String text = "The quick brown fox jumps over the lazy dog";
        int pos = formatter.findWrapPos(text, 15, 0);
        assertEquals(13, pos); // "The quick " -> position of space before 'f'
    }

    // Tests findWrapPos when word exceeds line width without whitespace before width
    @Test
    public void testFindWrapPos_noWhitespaceBeforeWidth_findsFirstWhitespaceAfterWidth()
    {
        String text = "supercalifragilisticexpialidocious is a long word";
        int pos = formatter.findWrapPos(text, 10, 0);
        assertEquals(34, pos); // wraps after the long word
    }

    // Tests renderWrappedText when nextLineTabStop >= width
    @Test
    public void testRenderWrappedText_tabStopGreaterThanWidth_adjustsTabStopAndRenders()
    {
        StringBuffer sb = new StringBuffer();
        String text = "This is a long line of description text that will be wrapped across multiple lines.";
        formatter.renderWrappedText(sb, 20, 25, text);

        String result = sb.toString();
        assertNotNull(result);
        assertTrue(result.length() > 0);
        assertTrue(result.contains(formatter.getNewLine()));
    }

    // Tests renderWrappedText with long multi-line text (Cli-25 regression test for wrapped line padding)
    @Test
    public void testRenderWrappedText_longWrappedDescription_doesNotInfiniteLoopOrCorrupt()
    {
        StringBuffer sb = new StringBuffer();
        String text = "first line has some initial text then some very long description wordthatcannotbewrappedneatly and keeps going further";
        formatter.renderWrappedText(sb, 30, 8, text);

        String result = sb.toString();
        assertNotNull(result);
        assertTrue(result.contains("first line"));
        assertTrue(result.contains("wordthatcannotbewrappedneatly"));
    }

    // Tests rtrim with null, empty, trailing whitespace, and no trailing whitespace
    @Test
    public void testRtrim_variousInputs_trimmedExpected()
    {
        assertNull(formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("  abc", formatter.rtrim("  abc   "));
        assertEquals("abc", formatter.rtrim("abc"));
        assertEquals("", formatter.rtrim("   \t\n\r"));
    }

    // Tests createPadding for various lengths
    @Test
    public void testCreatePadding_validLength_returnsExpectedSpaces()
    {
        assertEquals("", formatter.createPadding(0));
        assertEquals(" ", formatter.createPadding(1));
        assertEquals("   ", formatter.createPadding(3));
    }

    // Tests printHelp convenience overloads without throwing exceptions
    @Test
    public void testPrintHelp_convenienceOverloads_executeSuccessfully()
    {
        Options options = new Options();
        options.addOption("o", "option", false, "desc");

        formatter.printHelp("app", options);
        formatter.printHelp("app", options, true);
        formatter.printHelp("app", "header", options, "footer");
        formatter.printHelp("app", "header", options, "footer", true);
        formatter.printHelp(60, "app", "header", options, "footer");
        formatter.printHelp(60, "app", "header", options, "footer", true);
        formatter.printHelp(pw, 60, "app", "header", options, 2, 4, "footer");
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("-o,--option"));
    }

    // Tests OptionComparator sorting options alphabetically ignoring case
    @Test
    public void testOptionComparator_mixedCaseKeys_sortsCaseInsensitive()
    {
        Comparator comp = formatter.getOptionComparator();
        Option o1 = new Option("a", "alpha", false, "");
        Option o2 = new Option("B", "beta", false, "");
        Option o3 = new Option("c", "charlie", false, "");

        assertTrue(comp.compare(o1, o2) < 0);
        assertTrue(comp.compare(o2, o3) < 0);
        assertTrue(comp.compare(o1, o1) == 0);
    }
}