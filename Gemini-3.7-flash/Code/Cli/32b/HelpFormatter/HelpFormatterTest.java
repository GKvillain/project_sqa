package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    // Tests findWrapPos when word extends to the end of string without whitespace (Defects4J Bug 32)
    @Test
    public void testFindWrapPos_wordExtendsToEnd_returnsNegativeOrValidPos()
    {
        String text = "abcdefghijkl";
        int pos = formatter.findWrapPos(text, 5, 0);
        assertEquals(-1, pos);
    }

    // Tests findWrapPos when text contains newline within wrap width
    @Test
    public void testFindWrapPos_containsNewline_returnsNewlinePos()
    {
        String text = "hello\nworld";
        int pos = formatter.findWrapPos(text, 10, 0);
        assertEquals(6, pos);
    }

    // Tests findWrapPos when text contains tab within wrap width
    @Test
    public void testFindWrapPos_containsTab_returnsTabPos()
    {
        String text = "hello\tworld";
        int pos = formatter.findWrapPos(text, 10, 0);
        assertEquals(6, pos);
    }

    // Tests findWrapPos when text length is within width
    @Test
    public void testFindWrapPos_textShorterThanWidth_returnsMinusOne()
    {
        String text = "short";
        int pos = formatter.findWrapPos(text, 10, 0);
        assertEquals(-1, pos);
    }

    // Tests findWrapPos when text contains space before wrap width
    @Test
    public void testFindWrapPos_spaceBeforeWidth_returnsSpacePos()
    {
        String text = "hello world test";
        int pos = formatter.findWrapPos(text, 8, 0);
        assertEquals(5, pos);
    }

    // Tests renderWrappedText with long uninterrupted text that exceeds width (Defects4J Bug 32)
    @Test
    public void testRenderWrappedText_longWordExceedingWidth_rendersWithoutException()
    {
        StringBuffer sb = new StringBuffer();
        String text = "thisisareallylongwordwithnospaces";
        formatter.renderWrappedText(sb, 10, 0, text);
        assertEquals(text, sb.toString());
    }

    // Tests renderWrappedText with multiple wrapped lines and tab stop
    @Test
    public void testRenderWrappedText_multiLineWithTabStop_wrapsAndPadsCorrectly()
    {
        StringBuffer sb = new StringBuffer();
        String text = "line one with some words line two with more words";
        formatter.renderWrappedText(sb, 20, 5, text);
        String expected = "line one with some" + formatter.getNewLine()
                + "     words line two" + formatter.getNewLine()
                + "     with more words";
        assertEquals(expected, sb.toString());
    }

    // Tests rtrim with trailing spaces, empty string, and null
    @Test
    public void testRtrim_variousInputs_trimmedCorrectly()
    {
        assertEquals("  abc", formatter.rtrim("  abc   "));
        assertEquals("", formatter.rtrim("   "));
        assertEquals("", formatter.rtrim(""));
        assertEquals(null, formatter.rtrim(null));
    }

    // Tests createPadding creates correct number of spaces
    @Test
    public void testCreatePadding_positiveLength_returnsSpaces()
    {
        assertEquals("   ", formatter.createPadding(3));
        assertEquals("", formatter.createPadding(0));
    }

    // Tests printHelp with null cmdLineSyntax throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_nullSyntax_throwsException()
    {
        formatter.printHelp(printWriter, 80, null, "header", new Options(), 1, 3, "footer", false);
    }

    // Tests printHelp with empty cmdLineSyntax throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_emptySyntax_throwsException()
    {
        formatter.printHelp(printWriter, 80, "", "header", new Options(), 1, 3, "footer", false);
    }

    // Tests printHelp with full options, header, and footer
    @Test
    public void testPrintHelp_validOptionsAndHeaders_printsExpectedOutput()
    {
        Options options = new Options();
        options.addOption("a", "all", false, "do not ignore entries starting with .");
        options.addOption("s", "size", true, "print the allocated size of each file");

        formatter.printHelp(printWriter, 80, "app", "Header", options, 2, 2, "Footer", true);
        printWriter.flush();

        String result = stringWriter.toString();
        assertTrue(result.contains("usage: app"));
        assertTrue(result.contains("Header"));
        assertTrue(result.contains("-a,--all"));
        assertTrue(result.contains("-s,--size <arg>"));
        assertTrue(result.contains("Footer"));
    }

    // Tests printUsage with OptionGroup containing required and non-required groups
    @Test
    public void testPrintUsage_optionGroup_rendersGroupSyntax()
    {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("f", "file", false, "file option"));
        group.addOption(new Option("d", "dir", false, "directory option"));
        group.setRequired(false);
        options.addOptionGroup(group);

        formatter.printUsage(printWriter, 80, "myApp", options);
        printWriter.flush();

        String result = stringWriter.toString().trim();
        assertTrue(result.contains("myApp [-d | -f]") || result.contains("myApp [-f | -d]"));
    }

    // Tests printUsage with simple command line syntax string
    @Test
    public void testPrintUsage_simpleSyntaxString_printsUsage()
    {
        formatter.printUsage(printWriter, 80, "myApp -a -b <file>");
        printWriter.flush();
        assertEquals("usage: myApp -a -b <file>" + formatter.getNewLine(), stringWriter.toString());
    }

    // Tests renderOptions with long option only and argument name
    @Test
    public void testRenderOptions_longOptOnlyWithArg_rendersCorrectly()
    {
        Options options = new Options();
        Option opt = new Option(null, "config", true, "configuration file");
        opt.setArgName("FILE");
        options.addOption(opt);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 1, 3);
        String rendered = sb.toString();

        assertTrue(rendered.contains("--config <FILE>"));
        assertTrue(rendered.contains("configuration file"));
    }

    // Tests getters and setters for configuration attributes
    @Test
    public void testGettersAndSetters_customValues_retainUpdatedValues()
    {
        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());

        formatter.setLeftPadding(4);
        assertEquals(4, formatter.getLeftPadding());

        formatter.setDescPadding(5);
        assertEquals(5, formatter.getDescPadding());

        formatter.setSyntaxPrefix("Syntax: ");
        assertEquals("Syntax: ", formatter.getSyntaxPrefix());

        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());

        formatter.setOptPrefix("+");
        assertEquals("+", formatter.getOptPrefix());

        formatter.setLongOptPrefix("++");
        assertEquals("++", formatter.getLongOptPrefix());

        formatter.setArgName("VALUE");
        assertEquals("VALUE", formatter.getArgName());

        Comparator customComparator = Collections.reverseOrder();
        formatter.setOptionComparator(customComparator);
        assertEquals(customComparator, formatter.getOptionComparator());

        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
    }

    // Tests printWrapped method overloads
    @Test
    public void testPrintWrapped_withAndWithoutNextLineTabStop()
    {
        formatter.printWrapped(printWriter, 20, "first line of text second line of text");
        printWriter.flush();
        assertTrue(stringWriter.toString().contains("first line of text"));

        stringWriter.getBuffer().setLength(0);
        formatter.printWrapped(printWriter, 20, 4, "first line of text second line of text");
        printWriter.flush();
        assertTrue(stringWriter.toString().contains("    "));
    }

    // Tests printHelp overloaded convenience methods with autoUsage set to false
    @Test
    public void testPrintHelp_noAutoUsage_printsWithoutUsagePrefix()
    {
        Options options = new Options();
        options.addOption("v", "verbose", false, "enable verbose output");

        formatter.printHelp(printWriter, 80, "app", "Header", options, 1, 3, "Footer", false);
        printWriter.flush();

        String result = stringWriter.toString();
        assertTrue(result.startsWith("app"));
        assertTrue(result.contains("-v,--verbose"));
    }

    // Tests printUsage with required Option and required OptionGroup
    @Test
    public void testPrintUsage_requiredOptionAndRequiredGroup()
    {
        Options options = new Options();
        Option reqOpt = new Option("r", "required", false, "required option");
        reqOpt.setRequired(true);
        options.addOption(reqOpt);

        OptionGroup reqGroup = new OptionGroup();
        reqGroup.addOption(new Option("x", "exclusive1", false, "opt 1"));
        reqGroup.addOption(new Option("y", "exclusive2", false, "opt 2"));
        reqGroup.setRequired(true);
        options.addOptionGroup(reqGroup);

        formatter.printUsage(printWriter, 80, "myApp", options);
        printWriter.flush();

        String result = stringWriter.toString().trim();
        assertTrue(result.contains("-r"));
        assertTrue(result.contains("-x") && result.contains("-y"));
    }

    // Tests renderWrappedText with embedded newlines
    @Test
    public void testRenderWrappedText_embeddedNewlines_rendersCleanly()
    {
        StringBuffer sb = new StringBuffer();
        String text = "Line 1\nLine 2\nLine 3";
        formatter.renderWrappedText(sb, 80, 0, text);
        String expected = "Line 1" + formatter.getNewLine() + "Line 2" + formatter.getNewLine() + "Line 3";
        assertEquals(expected, sb.toString());
    }

    // Tests renderOptions with option having optional arg and multiple args
    @Test
    public void testRenderOptions_optionalArgAndMultipleArgs()
    {
        Options options = new Options();
        Option opt = new Option("o", "optional", true, "optional argument description");
        opt.setOptionalArg(true);
        options.addOption(opt);

        Option multiArg = new Option("m", "multi", true, "multi args description");
        multiArg.setArgs(2);
        options.addOption(multiArg);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 1, 3);
        String rendered = sb.toString();

        assertTrue(rendered.contains("-o,--optional"));
        assertTrue(rendered.contains("-m,--multi"));
    }
}