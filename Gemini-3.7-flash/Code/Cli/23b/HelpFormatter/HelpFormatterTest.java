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
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @Before
    public void setUp()
    {
        formatter = new HelpFormatter();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    // Tests getter and setter methods for default properties
    @Test
    public void testGettersAndSetters_customValues_returnsUpdatedValues()
    {
        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());

        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());

        formatter.setDescPadding(7);
        assertEquals(7, formatter.getDescPadding());

        formatter.setSyntaxPrefix("syn: ");
        assertEquals("syn: ", formatter.getSyntaxPrefix());

        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());

        formatter.setOptPrefix("+");
        assertEquals("+", formatter.getOptPrefix());

        formatter.setLongOptPrefix("++");
        assertEquals("++", formatter.getLongOptPrefix());

        formatter.setArgName("parameter");
        assertEquals("parameter", formatter.getArgName());
    }

    // Tests setting comparator to null and non-null values
    @Test
    public void testSetOptionComparator_nullAndCustomComparator_handlesCorrectly()
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
        assertNotSame(customComparator, formatter.getOptionComparator());
    }

    // Tests exception path for null cmdLineSyntax
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_nullCmdLineSyntax_throwsIllegalArgumentException()
    {
        formatter.printHelp(printWriter, 80, null, "header", new Options(), 1, 3, "footer", false);
    }

    // Tests exception path for empty cmdLineSyntax
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_emptyCmdLineSyntax_throwsIllegalArgumentException()
    {
        formatter.printHelp(printWriter, 80, "", "header", new Options(), 1, 3, "footer", false);
    }

    // Tests basic printHelp execution and output formatting
    @Test
    public void testPrintHelp_simpleOptions_outputsUsageAndOptions()
    {
        Options options = new Options();
        options.addOption("a", "all", false, "turn on all options");

        formatter.printHelp(printWriter, 80, "myapp", "Header banner", options, 2, 4, "Footer text", false);
        printWriter.flush();

        String result = stringWriter.toString();
        assertTrue(result.contains("usage: myapp"));
        assertTrue(result.contains("Header banner"));
        assertTrue(result.contains("-a,--all"));
        assertTrue(result.contains("turn on all options"));
        assertTrue(result.contains("Footer text"));
    }

    // Tests autoUsage flag formatting with simple and required options
    @Test
    public void testPrintHelp_autoUsageTrue_generatesFullUsageStatement()
    {
        Options options = new Options();
        Option requiredOpt = new Option("r", "require", true, "required option");
        requiredOpt.setRequired(true);
        requiredOpt.setArgName("VAL");
        options.addOption(requiredOpt);

        Option optOnlyLong = new Option(null, "long-only", false, "long option only");
        options.addOption(optOnlyLong);

        formatter.printHelp(printWriter, 80, "testApp", null, options, 1, 3, null, true);
        printWriter.flush();

        String result = stringWriter.toString();
        assertTrue(result.contains("usage: testApp -r <VAL> [--long-only]"));
        assertTrue(result.contains("-r,--require <VAL>"));
        assertTrue(result.contains("--long-only"));
    }

    // Tests printUsage with OptionGroup required and optional
    @Test
    public void testPrintUsage_optionGroup_rendersGroupCorrectly()
    {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "first in group"));
        group.addOption(new Option("b", "second in group"));
        options.addOptionGroup(group);

        formatter.printUsage(printWriter, 80, "appGroup", options);
        printWriter.flush();

        String result = stringWriter.toString();
        assertTrue(result.contains("[-a | -b]"));
    }

    // Tests printUsage with required OptionGroup
    @Test
    public void testPrintUsage_requiredOptionGroup_rendersWithoutOuterBrackets()
    {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("x", "option x"));
        group.addOption(new Option("y", "option y"));
        options.addOptionGroup(group);

        formatter.printUsage(printWriter, 80, "appGroupReq", options);
        printWriter.flush();

        String result = stringWriter.toString();
        assertTrue(result.contains("-x | -y"));
        assertFalse(result.contains("[-x | -y]"));
    }

    // Tests renderOptions when options have arguments without custom arg names
    @Test
    public void testRenderOptions_optionWithArgNoCustomArgName_rendersDefaultSpace()
    {
        Options options = new Options();
        Option opt = new Option("f", "file", true, "input file description");
        opt.setArgName(null);
        options.addOption(opt);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 2, 2);

        String rendered = sb.toString();
        assertTrue(rendered.contains("-f,--file "));
        assertTrue(rendered.contains("input file description"));
    }

    // Tests renderWrappedText with text that fits within line width
    @Test
    public void testRenderWrappedText_shortText_doesNotWrap()
    {
        StringBuffer sb = new StringBuffer();
        String text = "This is a short line.";
        formatter.renderWrappedText(sb, 80, 0, text);

        assertEquals("This is a short line.", sb.toString());
    }

    // Tests renderWrappedText with text requiring multiple line wrapping
    @Test
    public void testRenderWrappedText_longTextWithSpaces_wrapsAcrossLines()
    {
        StringBuffer sb = new StringBuffer();
        String text = "This is a longer line of text that is intended to exceed the column width and wrap properly.";
        formatter.renderWrappedText(sb, 30, 4, text);

        String[] lines = sb.toString().split(formatter.getNewLine());
        assertTrue(lines.length > 1);
        for (int i = 1; i < lines.length; i++)
        {
            assertTrue(lines[i].startsWith("    "));
        }
    }

    // Tests CLI-162 / Bug 23 infinite loop protection in renderWrappedText
    @Test(expected = RuntimeException.class)
    public void testRenderWrappedText_textTooLongForLine_throwsRuntimeException()
    {
        StringBuffer sb = new StringBuffer();
        String text = "word " + "longwordthatcannotfitinlineandcauseswrappingissue";
        formatter.renderWrappedText(sb, 10, 8, text);
    }

    // Tests findWrapPos when newline or tab character is within width
    @Test
    public void testFindWrapPos_withNewlineAndTab_returnsCharPosition()
    {
        String textNewline = "hello\nworld";
        int posNewline = formatter.findWrapPos(textNewline, 10, 0);
        assertEquals(6, posNewline);

        String textTab = "hello\tworld";
        int posTab = formatter.findWrapPos(textTab, 10, 0);
        assertEquals(6, posTab);
    }

    // Tests findWrapPos boundary when startPos + width >= text length
    @Test
    public void testFindWrapPos_textFitsWithinWidth_returnsMinusOne()
    {
        String text = "short";
        int pos = formatter.findWrapPos(text, 10, 0);
        assertEquals(-1, pos);
    }

    // Tests findWrapPos when word extends past width and next space is found
    @Test
    public void testFindWrapPos_noSpaceBeforeWidth_findsNextSpace()
    {
        String text = "abcdefghijklm nopqr";
        int pos = formatter.findWrapPos(text, 5, 0);
        assertEquals(13, pos);
    }

    // Tests createPadding helper method with various lengths
    @Test
    public void testCreatePadding_zeroAndPositiveLength_returnsCorrectSpaces()
    {
        assertEquals("", formatter.createPadding(0));
        assertEquals("   ", formatter.createPadding(3));
    }

    // Tests rtrim helper method for null, empty, and whitespace-padded strings
    @Test
    public void testRtrim_variousInputs_trimsTrailingWhitespaceOnly()
    {
        assertNull(formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("  abc", formatter.rtrim("  abc   "));
        assertEquals("abc", formatter.rtrim("abc\t\n\r"));
    }

    // Tests printHelp overloads that write to standard out
    @Test
    public void testPrintHelp_standardOutputOverloads_executesWithoutException()
    {
        Options options = new Options();
        options.addOption("h", "help", false, "display help");

        formatter.printHelp("app", options);
        formatter.printHelp("app", options, true);
        formatter.printHelp("app", "header", options, "footer");
        formatter.printHelp("app", "header", options, "footer", true);
        formatter.printHelp(60, "app", "header", options, "footer");
        formatter.printHelp(60, "app", "header", options, "footer", true);
    }
}