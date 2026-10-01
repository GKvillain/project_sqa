package org.apache.commons.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

import org.junit.Before;
import org.junit.Test;

public class HelpFormatterTest {

    private HelpFormatter formatter;
    private Options options;

    @Before
    public void setUp() {
        formatter = new HelpFormatter();
        options = new Options();
    }

    // Tests default field values
    @Test
    public void testDefaults_initialState_hasDefaultValues() {
        assertEquals(HelpFormatter.DEFAULT_WIDTH, formatter.getWidth());
        assertEquals(HelpFormatter.DEFAULT_LEFT_PAD, formatter.getLeftPadding());
        assertEquals(HelpFormatter.DEFAULT_DESC_PAD, formatter.getDescPadding());
        assertEquals(HelpFormatter.DEFAULT_SYNTAX_PREFIX, formatter.getSyntaxPrefix());
        assertEquals(HelpFormatter.DEFAULT_OPT_PREFIX, formatter.getOptPrefix());
        assertEquals(HelpFormatter.DEFAULT_LONG_OPT_PREFIX, formatter.getLongOptPrefix());
        assertEquals(HelpFormatter.DEFAULT_ARG_NAME, formatter.getArgName());
        assertEquals(System.getProperty("line.separator"), formatter.getNewLine());
        assertNotNull(formatter.getOptionComparator());
    }

    // Tests all setters update their corresponding fields
    @Test
    public void testSetters_validValues_updateFieldsAndComparator() {
        formatter.setWidth(80);
        formatter.setLeftPadding(2);
        formatter.setDescPadding(4);
        formatter.setSyntaxPrefix("usage: ");
        formatter.setNewLine("\n");
        formatter.setOptPrefix("-");
        formatter.setLongOptPrefix("--");
        formatter.setArgName("file");

        assertEquals(80, formatter.getWidth());
        assertEquals(2, formatter.getLeftPadding());
        assertEquals(4, formatter.getDescPadding());
        assertEquals("usage: ", formatter.getSyntaxPrefix());
        assertEquals("\n", formatter.getNewLine());
        assertEquals("-", formatter.getOptPrefix());
        assertEquals("--", formatter.getLongOptPrefix());
        assertEquals("file", formatter.getArgName());

        Comparator custom = new Comparator() {
            public int compare(Object o1, Object o2) {
                return 0;
            }
        };
        formatter.setOptionComparator(custom);
        assertSame(custom, formatter.getOptionComparator());

        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
    }

    // Tests exception path for null command line syntax
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_nullCmdLineSyntax_throwsIllegalArgumentException() {
        formatter.printHelp(new PrintWriter(new StringWriter()), 80, (String) null,
                (String) null, new Options(), 1, 3, (String) null, false);
    }

    // Tests exception path for empty command line syntax
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_emptyCmdLineSyntax_throwsIllegalArgumentException() {
        formatter.printHelp(new PrintWriter(new StringWriter()), 80, "",
                (String) null, new Options(), 1, 3, (String) null, false);
    }

    // Tests printing normal help with header, options and footer
    @Test
    public void testPrintHelp_validOptionsAndHeaderFooter_writesHelp() {
        options.addOption("a", "alpha", true, "A option");
        options.addOption("b", "beta", false, "B option");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", "header", options, 1, 3, "footer", false);
        pw.flush();

        String out = sw.toString();
        assertTrue(out.contains("usage: app"));
        assertTrue(out.contains("header"));
        assertTrue(out.contains("-a"));
        assertTrue(out.contains("-b"));
        assertTrue(out.contains("footer"));
    }

    // Tests auto-generated usage with an OptionGroup
    @Test
    public void testPrintHelp_autoUsageWithOptionGroup_writesGroupInUsage() {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", false, "A option"));
        group.addOption(new Option("b", false, "B option"));
        options.addOptionGroup(group);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", (String) null, options, 1, 3, (String) null, true);
        pw.flush();

        String out = sw.toString();
        assertTrue(out.contains("usage: app"));
        assertTrue(out.contains("[-a | -b]"));
    }

    // Tests printUsage without options
    @Test
    public void testPrintUsage_noOptions_writesUsage() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app");
        pw.flush();

        assertEquals("usage: app" + formatter.getNewLine(), sw.toString());
    }

    // Tests printUsage with an option and an argument name
    @Test
    public void testPrintUsage_withOptionAndArgName_writesBracketedOption() {
        Option opt = new Option("a", "alpha", true, "A option");
        opt.setArgName("arg");
        options.addOption(opt);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();

        String out = sw.toString();
        assertTrue(out.startsWith("usage: app"));
        assertTrue(out.contains("[-a <arg>]"));
    }

    // Tests that options are rendered in alphabetical order
    @Test
    public void testPrintOptions_multipleOptions_sortedByKey() {
        options.addOption("b", "beta", false, "B option");
        options.addOption("a", "alpha", false, "A option");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printOptions(pw, 80, options, 1, 3);
        pw.flush();

        String out = sw.toString();
        assertTrue(out.indexOf("-a") != -1);
        assertTrue(out.indexOf("-b") != -1);
        assertTrue(out.indexOf("-a") < out.indexOf("-b"));
    }

    // Tests the branch where an option has no short option
    @Test
    public void testPrintOptions_longOnlyOption_writesLongOption() {
        options.addOption(new Option((String) null, "long", false, "Long option"));

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printOptions(pw, 80, options, 1, 3);
        pw.flush();

        assertTrue(sw.toString().contains("--long"));
    }

    // Tests short text that does not need wrapping
    @Test
    public void testPrintWrapped_shortText_writesText() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printWrapped(pw, 80, "  hello  ");
        pw.flush();

        assertEquals("  hello" + formatter.getNewLine(), sw.toString());
    }

    // Tests wrapping long text at whitespace
    @Test
    public void testPrintWrapped_longTextWithSpaces_wrapsText() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printWrapped(pw, 10, "one two three four");
        pw.flush();

        String out = sw.toString();
        assertTrue(out.contains("one two" + formatter.getNewLine()));
        assertTrue(out.contains("three four"));
    }

    // Tests the defect: long unbroken text must not cause StringIndexOutOfBoundsException
    @Test
    public void testPrintWrapped_longUnbrokenText_doesNotThrowStringIndexOutOfBoundsException() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printWrapped(pw, 10, "aaaaaaaaaaaaaaaaaaaaaaaa");
        pw.flush();

        assertTrue(sw.toString().contains("aaaaaaaaaaaaaaaaaaaaaaaa"));
    }

    // Tests findWrapPos when the text is shorter than the width
    @Test
    public void testFindWrapPos_shortText_returnsNegativeOne() {
        assertEquals(-1, formatter.findWrapPos("abc", 10, 0));
    }

    // Tests findWrapPos with newline and tab within the width
    @Test
    public void testFindWrapPos_newlineAndTabWithinWidth_returnsAfterCharacter() {
        assertEquals(3, formatter.findWrapPos("ab\ncd", 10, 0));
        assertEquals(4, formatter.findWrapPos("abc\tdef", 10, 0));
    }

    // Tests findWrapPos when a whitespace occurs before the wrap position
    @Test
    public void testFindWrapPos_whitespaceBeforeWidth_returnsWhitespacePosition() {
        assertEquals(5, formatter.findWrapPos("hello world foo", 10, 0));
    }

    // Tests exception path when there is no room for the indent
    @Test(expected = IllegalStateException.class)
    public void testRenderWrappedText_indentNoRoom_throwsIllegalStateException() {
        formatter.renderWrappedText(new StringBuffer(), 5, 5, "long text that wraps");
    }

    // Tests rtrim removing trailing whitespace
    @Test
    public void testRtrim_trailingWhitespace_removesWhitespace() {
        assertEquals("abc", formatter.rtrim("abc   "));
        assertNull(formatter.rtrim(null));
        assertEquals("", formatter.rtrim("   "));
    }

    // Tests createPadding for zero and positive lengths
    @Test
    public void testCreatePadding_zeroAndPositive_returnsSpaces() {
        assertEquals("", formatter.createPadding(0));
        assertEquals("   ", formatter.createPadding(3));
    }

    // ==================== New tests for uncovered areas ====================

    @Test
    public void testPrintHelp_autoUsageFalseWithOptionGroup_doesNotIncludeGroupInUsage() {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", false, "A option"));
        group.addOption(new Option("b", false, "B option"));
        options.addOptionGroup(group);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", "header", options, 1, 3, "footer", false);
        pw.flush();

        String out = sw.toString();
        assertTrue(out.contains("usage: app"));
        assertTrue(out.contains("header"));
        assertTrue(out.contains("footer"));
        assertFalse(out.contains("[-a | -b]"));
    }

    @Test
    public void testPrintUsage_requiredOption_showsWithoutBrackets() {
        Option opt = new Option("a", "alpha", true, "A option");
        opt.setRequired(true);
        opt.setArgName("arg");
        options.addOption(opt);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();

        String out = sw.toString();
        assertTrue(out.startsWith("usage: app"));
        assertTrue(out.contains("-a <arg>")); // no brackets
        assertFalse(out.contains("[-a <arg>]"));
    }

    @Test
    public void testPrintUsage_requiredOptionGroup_showsGroupWithoutBrackets() {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", false, "A option"));
        group.addOption(new Option("b", false, "B option"));
        options.addOptionGroup(group);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();

        String out = sw.toString();
        assertTrue(out.contains("(-a | -b)")); // required group without brackets
        assertFalse(out.contains("[-a | -b]"));
    }

    @Test
    public void testFindWrapPos_newlineAtPositionWithinWidth_returnsNewlineIndex() {
        assertEquals(3, formatter.findWrapPos("ab\ncd", 3, 0));
        assertEquals(3, formatter.findWrapPos("ab\ncd", 2, 0));
        assertEquals(-1, formatter.findWrapPos("ab\ncd", 1, 0));
    }

    @Test
    public void testRenderWrappedText_textExactlyWidthAndIndent_wrapsWithoutExtraLines() {
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 10, 2, "12345678");
        String result = sb.toString();
        String expected = "  12345678"; // indent 2 spaces + text
        assertEquals(expected, result);
    }

    @Test
    public void testRtrim_withLeadingAndTrailingSpaces_removesOnlyTrailing() {
        assertEquals("   abc", formatter.rtrim("   abc   "));
    }
}