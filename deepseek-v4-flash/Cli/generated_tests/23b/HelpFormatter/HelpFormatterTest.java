package org.apache.commons.cli;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.PrintWriter;
import java.io.StringWriter;

public class HelpFormatterTest {

    private HelpFormatter formatter = new HelpFormatter();
    private String lineSeparator = System.getProperty("line.separator");

    // === Existing tests (from original) ===

    // Tests basic usage statement without options
    @Test
    public void testPrintUsage_noOptions_printsSyntax() {
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printUsage(pw, 80, "myapp");
        pw.flush();
        String result = out.toString().trim();
        assertEquals("usage: myapp", result);
    }

    // Tests usage with a simple option
    @Test
    public void testPrintUsage_withOption_printsOption() {
        Options opts = new Options();
        opts.addOption("v", "verbose", false, "verbose mode");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printUsage(pw, 80, "myapp", opts);
        pw.flush();
        String result = out.toString().trim();
        assertTrue("Expected usage containing [-v]", result.contains("[-v]"));
    }

    // Tests usage with required option group (no brackets)
    @Test
    public void testPrintUsage_optionGroupRequired_printsGroupNoBrackets() {
        Options opts = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "alpha", false, "alpha"));
        group.addOption(new Option("b", "beta", false, "beta"));
        opts.addOptionGroup(group);
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printUsage(pw, 80, "myapp", opts);
        pw.flush();
        String result = out.toString().trim();
        assertTrue(result.contains("-a | -b"));
        assertFalse("Group should not be wrapped in brackets", result.startsWith("["));
    }

    // Tests usage with optional option group (brackets)
    @Test
    public void testPrintUsage_optionGroupOptional_printsGroupWithBrackets() {
        Options opts = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(false);
        group.addOption(new Option("x", false, "x"));
        group.addOption(new Option("y", false, "y"));
        opts.addOptionGroup(group);
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printUsage(pw, 80, "myapp", opts);
        pw.flush();
        String result = out.toString().trim();
        assertTrue(result.contains("[-x | -y]"));
    }

    // Tests usage with option having argument and arg name
    @Test
    public void testPrintUsage_optionWithArg_showsArgName() {
        Options opts = new Options();
        Option opt = new Option("o", "output", true, "output file");
        opt.setArgName("file");
        opts.addOption(opt);
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printUsage(pw, 80, "myapp", opts);
        pw.flush();
        String result = out.toString().trim();
        assertTrue(result.contains("-o <file>"));
    }

    // Tests usage with long-only option (no short opt)
    @Test
    public void testPrintUsage_longOnlyOption_usesDoubleDash() {
        Options opts = new Options();
        Option opt = new Option(null, "longopt", false, "a long option");
        opts.addOption(opt);
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printUsage(pw, 80, "myapp", opts);
        pw.flush();
        String result = out.toString().trim();
        assertTrue(result.contains("--longopt"));
    }

    // Tests printHelp with autoUsage true - includes usage with options
    @Test
    public void testPrintHelp_autoUsageTrue_includesUsageLine() {
        Options opts = new Options();
        opts.addOption("h", false, "help");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "app", null, opts, 1, 3, null, true);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("usage: app"));
        assertTrue(result.contains("-h"));
    }

    // Tests printHelp with autoUsage false - usage line without options
    @Test
    public void testPrintHelp_autoUsageFalse_usageNoOptions() {
        Options opts = new Options();
        opts.addOption("h", false, "help");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "app", null, opts, 1, 3, null, false);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("usage: app"));
        assertFalse("Options should not appear in usage line", result.contains("-h"));
    }

    // Tests printHelp with header and footer
    @Test
    public void testPrintHelp_withHeaderAndFooter_printsBoth() {
        Options opts = new Options();
        opts.addOption("t", false, "test");
        String header = "This is header";
        String footer = "This is footer";
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "app", header, opts, 1, 3, footer, false);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains(header));
        assertTrue(result.contains(footer));
    }

    // Tests printOptions rendering (short and long opt)
    @Test
    public void testPrintOptions_rendersOptionsCorrectly() {
        Options opts = new Options();
        Option opt = new Option("f", "file", true, "input file");
        opt.setArgName("path");
        opts.addOption(opt);
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printOptions(pw, 80, opts, 1, 3);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("-f,--file <path>"));
        assertTrue(result.contains("input file"));
    }

    // Tests renderWrappedText with text shorter than width (no wrap)
    @Test
    public void testRenderWrappedText_shortText_noWrap() {
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 80, 0, "short text");
        String result = sb.toString();
        assertEquals("short text", result.trim());
    }

    // Tests renderWrappedText with text longer than width - wraps at space
    @Test
    public void testRenderWrappedText_longText_wrapsAtSpace() {
        StringBuffer sb = new StringBuffer();
        String text = "This is a long text that should be wrapped because it exceeds the width";
        formatter.renderWrappedText(sb, 25, 0, text);
        String result = sb.toString();
        String[] lines = result.split(lineSeparator);
        for (String line : lines) {
            assertTrue("Line too long: " + line, line.length() <= 25);
        }
    }

    // Tests renderWrappedText with nextLineTabStop - second line indented
    @Test
    public void testRenderWrappedText_withNextLineTabStop_indentsSecondLine() {
        StringBuffer sb = new StringBuffer();
        String text = "A long line that needs wrapping and indentation";
        formatter.renderWrappedText(sb, 20, 5, text);
        String result = sb.toString();
        String[] lines = result.split(lineSeparator);
        assertTrue(lines.length >= 2);
        for (int i = 1; i < lines.length; i++) {
            assertEquals("Padding missing or wrong", 5, lines[i].length() - lines[i].trim().length());
        }
    }

    // Tests findWrapPos when text fits within width - returns -1
    @Test
    public void testFindWrapPos_textFits_returnsMinusOne() {
        int pos = formatter.findWrapPos("hello", 10, 0);
        assertEquals(-1, pos);
    }

    // Tests findWrapPos with newline character within width
    @Test
    public void testFindWrapPos_newlineWithinWidth_returnsPosPlusOne() {
        int pos = formatter.findWrapPos("hello\nworld", 10, 0);
        assertEquals(6, pos);
    }

    // Tests findWrapPos wraps at last space before width
    @Test
    public void testFindWrapPos_spaceBeforeWidth_returnsSpacePosition() {
        int pos = formatter.findWrapPos("one two three four", 10, 0);
        assertEquals(3, pos);
    }

    // Tests findWrapPos when no whitespace exists - triggers defect (IndexOutOfBoundsException)
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testFindWrapPos_noWhitespace_throwsIndexOutOfBounds() {
        String text = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        formatter.findWrapPos(text, 30, 0);
    }

    // Tests printHelp with null cmdLineSyntax - throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_nullCmdLineSyntax_throwsIllegalArgument() {
        formatter.printHelp((String) null, new Options());
    }

    // Tests printHelp with empty cmdLineSyntax - throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_emptyCmdLineSyntax_throwsIllegalArgument() {
        formatter.printHelp("", new Options());
    }

    // Tests rtrim with null input returns null
    @Test
    public void testRtrim_nullInput_returnsNull() {
        assertNull(formatter.rtrim(null));
    }

    // Tests rtrim removes trailing spaces
    @Test
    public void testRtrim_trailingSpaces_removed() {
        assertEquals("hello", formatter.rtrim("hello   "));
        assertEquals("no spaces", formatter.rtrim("no spaces"));
        assertEquals("", formatter.rtrim("   "));
    }

    // ============ New test cases to cover previously uncovered parts ============

    // Tests printUsage with both long and short options present
    @Test
    public void testPrintUsage_longAndShortOptions_printsBoth() {
        Options opts = new Options();
        opts.addOption("v", "verbose", false, "verbose");
        opts.addOption("o", "output", true, "output file");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printUsage(pw, 80, "myapp", opts);
        pw.flush();
        String result = out.toString().trim();
        assertTrue(result.contains("[-v]"));
        assertTrue(result.contains("[-o <arg>]"));
    }

    // Tests printHelp without header and footer, only usage and options
    @Test
    public void testPrintHelp_withoutHeaderAndFooter_printsUsageAndOptions() {
        Options opts = new Options();
        opts.addOption("d", false, "debug");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "app", null, opts, 1, 3, null, false);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("usage: app"));
        assertTrue(result.contains("-d"));
        assertFalse(result.contains("null"));
    }

    // Tests printHelp with autoUsage true when options is empty
    @Test
    public void testPrintHelp_autoUsageTrue_emptyOptions_printsUsageOnly() {
        Options opts = new Options();
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "app", null, opts, 1, 3, null, true);
        pw.flush();
        String result = out.toString().trim();
        assertTrue(result.contains("usage: app"));
        // no options to show
        assertFalse(result.contains("--"));
    }

    // Tests printOptions with multiple options (some long-only)
    @Test
    public void testPrintOptions_multipleOptions_rendersCorrectly() {
        Options opts = new Options();
        opts.addOption("a", null, false, "alpha");
        opts.addOption(null, "beta", false, "beta");
        Option gamma = new Option("g", "gamma", true, "gamma");
        gamma.setArgName("file");
        opts.addOption(gamma);
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printOptions(pw, 80, opts, 2, 5);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("-a"));
        assertTrue(result.contains("--beta"));
        assertTrue(result.contains("-g,--gamma <file>"));
    }

    // Tests findWrapPos when startPos is non-zero (wrapping in middle of text)
    @Test
    public void testFindWrapPos_startPosNonZero_returnsCorrectPosition() {
        // text: "abc def ghi jkl" with width 10 and startPos 4 (starting at 'def')
        // the substring from 4 is "def ghi jkl", width 10, should wrap at space after "def" (position 7)
        int pos = formatter.findWrapPos("abc def ghi jkl", 10, 4);
        // Expected: startPos=4, substring "def ghi jkl", first space at index 3 of substring -> global index 7
        assertEquals(7, pos);
    }

    // Tests renderWrappedText with embedded newline characters
    @Test
    public void testRenderWrappedText_withNewline_breaksAtNewline() {
        StringBuffer sb = new StringBuffer();
        String text = "first line\nsecond line";
        formatter.renderWrappedText(sb, 50, 0, text);
        String result = sb.toString();
        assertTrue(result.contains("first line"));
        assertTrue(result.contains("second line"));
    }

    // Tests rtrim with tabs and mixed trailing whitespace
    @Test
    public void testRtrim_tabsAndSpaces_removed() {
        assertEquals("hello", formatter.rtrim("hello \t  "));
        assertEquals("hello\tworld", formatter.rtrim("hello\tworld  "));
        assertEquals("", formatter.rtrim("\t  \t"));
    }

    // Tests createPadding returns correct number of spaces
    @Test
    public void testCreatePadding_returnsCorrectSpaces() {
        assertEquals("", formatter.createPadding(0));
        assertEquals(" ", formatter.createPadding(1));
        assertEquals("     ", formatter.createPadding(5));
        // Test with negative (should return empty string or handle gracefully)
        assertEquals("", formatter.createPadding(-1));
    }

    // Tests getNewLine returns the system line separator
    @Test
    public void testGetNewLine_returnsSystemLineSeparator() {
        String nl = formatter.getNewLine();
        assertEquals(System.getProperty("line.separator"), nl);
    }

    // Tests setWidth and getWidth work correctly
    @Test
    public void testSetWidthAndGetWidth_workCorrectly() {
        formatter.setWidth(120);
        assertEquals(120, formatter.getWidth());
        formatter.setWidth(40);
        assertEquals(40, formatter.getWidth());
    }

    // Tests setLeftPadding and getLeftPadding
    @Test
    public void testSetLeftPaddingAndGetLeftPadding() {
        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());
    }

    // Tests setDescPadding and getDescPadding
    @Test
    public void testSetDescPaddingAndGetDescPadding() {
        formatter.setDescPadding(3);
        assertEquals(3, formatter.getDescPadding());
    }

    // Tests printHelp with all parameters (non-null header/footer, autoUsage)
    @Test
    public void testPrintHelp_fullParams_autoUsageTrue() {
        Options opts = new Options();
        opts.addOption("v", false, "verbose");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "app", "header", opts, 2, 4, "footer", true);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("usage: app [-v]"));
        assertTrue(result.contains("header"));
        assertTrue(result.contains("footer"));
        assertTrue(result.contains("-v"));
    }

    // Tests printUsage when option group has more than two options
    @Test
    public void testPrintUsage_optionGroupThreeOptions_printsAll() {
        Options opts = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(false);
        group.addOption(new Option("a", false, "a"));
        group.addOption(new Option("b", false, "b"));
        group.addOption(new Option("c", false, "c"));
        opts.addOptionGroup(group);
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printUsage(pw, 80, "myapp", opts);
        pw.flush();
        String result = out.toString().trim();
        assertTrue(result.contains("[-a | -b | -c]"));
    }

    // Tests renderWrappedText when text exactly fits width (no wrap)
    @Test
    public void testRenderWrappedText_exactFit_noWrap() {
        StringBuffer sb = new StringBuffer();
        String text = "1234567890";
        formatter.renderWrappedText(sb, 10, 0, text);
        String result = sb.toString();
        assertEquals("1234567890", result.trim());
        // verify no newline added
        assertFalse(result.contains(lineSeparator));
    }

    // Tests findWrapPos when there is no space but text fits width
    @Test
    public void testFindWrapPos_noSpaceExactlyFits_returnsMinusOne() {
        int pos = formatter.findWrapPos("abcdefghij", 10, 0);
        assertEquals(-1, pos);
    }
}