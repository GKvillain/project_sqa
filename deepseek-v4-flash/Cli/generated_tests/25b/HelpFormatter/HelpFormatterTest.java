package org.apache.commons.cli;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

public class HelpFormatterTest {
    private HelpFormatter formatter = new HelpFormatter();

    // Test all getters return default values
    @Test
    public void testGetters_defaultValues_returnsCorrectDefaults() {
        assertEquals(HelpFormatter.DEFAULT_WIDTH, formatter.getWidth());
        assertEquals(HelpFormatter.DEFAULT_LEFT_PAD, formatter.getLeftPadding());
        assertEquals(HelpFormatter.DEFAULT_DESC_PAD, formatter.getDescPadding());
        assertEquals(HelpFormatter.DEFAULT_SYNTAX_PREFIX, formatter.getSyntaxPrefix());
        assertEquals(HelpFormatter.DEFAULT_OPT_PREFIX, formatter.getOptPrefix());
        assertEquals(HelpFormatter.DEFAULT_LONG_OPT_PREFIX, formatter.getLongOptPrefix());
        assertEquals(HelpFormatter.DEFAULT_ARG_NAME, formatter.getArgName());
        assertNotNull(formatter.getNewLine());
    }

    // Test setters update values and getters return them
    @Test
    public void testSetters_updatedValues_returnsUpdatedValues() {
        formatter.setWidth(80);
        formatter.setLeftPadding(2);
        formatter.setDescPadding(4);
        formatter.setSyntaxPrefix("Usage:");
        formatter.setNewLine("\n");
        formatter.setOptPrefix("/");
        formatter.setLongOptPrefix("//");
        formatter.setArgName("file");

        assertEquals(80, formatter.getWidth());
        assertEquals(2, formatter.getLeftPadding());
        assertEquals(4, formatter.getDescPadding());
        assertEquals("Usage:", formatter.getSyntaxPrefix());
        assertEquals("\n", formatter.getNewLine());
        assertEquals("/", formatter.getOptPrefix());
        assertEquals("//", formatter.getLongOptPrefix());
        assertEquals("file", formatter.getArgName());
    }

    // Test null cmdLineSyntax throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_nullCmdLine_throwsIllegalArgumentException() {
        formatter.printHelp((String) null, new Options());
    }

    // Test empty cmdLineSyntax throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_emptyCmdLine_throwsIllegalArgumentException() {
        formatter.printHelp("", new Options());
    }

    // Test printUsage with required and optional options, includes argument names
    @Test
    public void testPrintUsage_writerWidthAppOptions_requiredAndOptionalOptions_outputsCorrectUsage() {
        Options options = new Options();
        Option a = new Option("a", "aaa", false, "desc a");
        a.setRequired(true);
        Option b = new Option("b", "bbb", true, "desc b");
        b.setArgName("arg");
        options.addOption(a);
        options.addOption(b);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 50, "myapp", options);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("usage: myapp"));
        assertTrue(output.contains("-a"));
        assertTrue(output.contains("[-b --bbb <arg>]"));
    }

    // Test optional OptionGroup is rendered inside brackets
    @Test
    public void testPrintUsage_writerWidthAppOptions_optionalGroup_outputsGroupInBrackets() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "aaa", false, "desc a"));
        group.addOption(new Option("b", "bbb", false, "desc b"));
        group.setRequired(false);
        options.addOptionGroup(group);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 50, "myapp", options);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("[-a | -b]"));
    }

    // Test printUsage with only command line syntax
    @Test
    public void testPrintUsage_writerWidthCmdLineSyntax_only_containsSyntaxPrefix() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 50, "myapp -v");
        pw.flush();
        String expected = "usage: myapp -v" + formatter.getNewLine();
        assertEquals(expected, sw.toString());
    }

    // Test printOptions includes option prefixes, long options, argument names, and descriptions
    @Test
    public void testPrintOptions_renderOptions_outputIncludesOptionsDescriptions() {
        Options options = new Options();
        Option a = new Option("a", "aaa", false, "desc a");
        Option b = new Option("b", "bbb", true, "desc b");
        b.setArgName("arg");
        Option c = new Option("c", "ccc", true, "desc c"); // hasArg true but no argName
        options.addOption(a);
        options.addOption(b);
        options.addOption(c);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printOptions(pw, 60, options, 1, 3);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("-a,--aaa"));
        assertTrue(output.contains("desc a"));
        assertTrue(output.contains("-b,--bbb <arg>"));
        assertTrue(output.contains("desc b"));
        assertTrue(output.contains("-c,--ccc"));
        assertTrue(output.contains("desc c"));
    }

    // Test printWrapped with a short text does not wrap
    @Test
    public void testPrintWrapped_shortText_noWrap() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printWrapped(pw, 50, "short text");
        pw.flush();
        String expected = "short text" + formatter.getNewLine();
        assertEquals(expected, sw.toString());
    }

    // Test printWrapped with a long text wraps within the specified width
    @Test
    public void testPrintWrapped_longText_wrapsWithinWidth() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printWrapped(pw, 30, "This is a long line that should wrap because it exceeds the width");
        pw.flush();
        String output = sw.toString();
        String[] lines = output.split(formatter.getNewLine());
        assertTrue(lines.length > 1);
        for (String line : lines) {
            assertTrue(line.length() <= 30);
        }
    }

    // Test renderWrappedText wraps and handles nextLineTabStop >= width
    @Test
    public void testRenderWrappedText_longText_wrapsWithPadding() {
        StringBuffer sb = new StringBuffer();
        String text = "This is a long line that should wrap because it exceeds the width";
        formatter.renderWrappedText(sb, 20, 5, text);
        String result = sb.toString();
        assertTrue(result.length() > 0);
        assertTrue(result.contains(formatter.getNewLine()));

        // Test the branch where nextLineTabStop >= width (prevents infinite loop)
        StringBuffer sb2 = new StringBuffer();
        formatter.renderWrappedText(sb2, 10, 10, text);
        assertTrue(sb2.length() > 0);
    }

    // Test findWrapPos returns the position before a space within the width
    @Test
    public void testFindWrapPos_spaceWithinWidth_returnsSpacePosition() {
        assertEquals(5, formatter.findWrapPos("hello world", 6, 0));
    }

    // Test findWrapPos returns -1 when no wrap position exists
    @Test
    public void testFindWrapPos_noSpaceWithinWidth_returnsMinusOne() {
        assertEquals(-1, formatter.findWrapPos("abcdefgh", 4, 0));
    }

    // Test findWrapPos handles newline and tab characters within the width
    @Test
    public void testFindWrapPos_withNewlineOrTabWithinWidth_returnsPosAfterCharacter() {
        assertEquals(4, formatter.findWrapPos("abc\ndefgh", 5, 0));
        assertEquals(3, formatter.findWrapPos("ab\tdefgh", 4, 0));
    }

    // Test createPadding returns empty string for zero and the correct spaces for positive length
    @Test
    public void testCreatePadding_variousLengths_returnsCorrectPadding() {
        assertEquals("", formatter.createPadding(0));
        assertEquals("   ", formatter.createPadding(3));
    }

    // Test rtrim handles null, empty, and trailing whitespace inputs
    @Test
    public void testRtrim_variousInputs_returnsCorrectString() {
        assertNull(formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("abc", formatter.rtrim("abc   "));
    }

    // Test printHelp with autoUsage=true includes usage, header, options, and footer
    @Test
    public void testPrintHelp_autoUsageTrue_outputsUsageHeaderOptionsFooter() {
        Options options = new Options();
        options.addOption(new Option("a", "aaa", false, "desc a"));
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 50, "myapp", "header", options, 1, 3, "footer", true);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("usage: myapp"));
        assertTrue(output.contains("header"));
        assertTrue(output.contains("-a"));
        assertTrue(output.contains("footer"));
    }

    // Test printHelp with autoUsage=false does not include options in the usage line
    @Test
    public void testPrintHelp_autoUsageFalse_usageLineDoesNotIncludeOptions() {
        Options options = new Options();
        options.addOption(new Option("a", "aaa", false, "desc a"));
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 50, "myapp", "header", options, 1, 3, "footer", false);
        pw.flush();
        String output = sw.toString();
        String firstLine = output.split(formatter.getNewLine())[0];
        assertFalse(firstLine.contains("-a"));
        assertTrue(output.contains("-a"));
        assertTrue(output.contains("header"));
        assertTrue(output.contains("footer"));
    }

    // Test setting a custom comparator changes option ordering, and null restores default
    @Test
    public void testSetOptionComparator_customComparator_usesReverseOrder() {
        Options options = new Options();
        options.addOption(new Option("b", "bbb", false, "desc b"));
        options.addOption(new Option("a", "aaa", false, "desc a"));

        formatter.setOptionComparator(new Comparator() {
            public int compare(Object o1, Object o2) {
                Option opt1 = (Option) o1;
                Option opt2 = (Option) o2;
                return opt2.getKey().compareToIgnoreCase(opt1.getKey());
            }
        });

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printOptions(pw, 50, options, 1, 3);
        pw.flush();
        String output = sw.toString();
        int idxB = output.indexOf("-b");
        int idxA = output.indexOf("-a");
        assertTrue(idxB != -1 && idxA != -1 && idxB < idxA);

        // Setting null must restore the default comparator
        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
    }

    // ========== New test cases for uncovered parts ==========

    // Test printHelp(String, Options) overload (default autoUsage = true)
    @Test
    public void testPrintHelp_stringAndOptions_overload_outputsUsageAndOptions() {
        Options options = new Options();
        options.addOption(new Option("x", "xxx", false, "desc x"));
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        // This overload exists in HelpFormatter
        formatter.printHelp(pw, HelpFormatter.DEFAULT_WIDTH, "myapp", options);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("usage: myapp"));
        assertTrue(output.contains("-x"));
    }

    // Test printHelp with header and footer but no autoUsage separation
    @Test
    public void testPrintHelp_withHeaderAndFooter_noAutoUsage() {
        Options options = new Options();
        options.addOption(new Option("o", "opt", false, "desc"));
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 60, "tool", "Header", options, 2, 4, "Footer");
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("Header"));
        assertTrue(output.contains("Footer"));
        assertTrue(output.contains("-o,--opt"));
        // With no autoUsage flag, the usage line includes options by default (autoUsage=true)
        assertTrue(output.contains("usage: tool"));
    }

    // Test printUsage with a required OptionGroup
    @Test
    public void testPrintUsage_requiredOptionGroup_outputsGroupWithoutBrackets() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "aaa", false, "desc a"));
        group.addOption(new Option("b", "bbb", false, "desc b"));
        group.setRequired(true);  // required group
        options.addOptionGroup(group);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 50, "myapp", options);
        pw.flush();
        String output = sw.toString();
        // Required group should not be wrapped in brackets
        assertTrue(output.contains("-a | -b"));
        assertFalse(output.contains("[-a | -b]"));
    }

    // Test printOptions when an option has no longOpt
    @Test
    public void testPrintOptions_longOptNull_showsOnlyShortOption() {
        Options options = new Options();
        Option opt = new Option("s", null, false, "short only desc");
        options.addOption(opt);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printOptions(pw, 50, options, 1, 3);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("-s"));
        assertFalse(output.contains("-s,--")); // no long opt
        assertTrue(output.contains("short only desc"));
    }

    // Test printWrapped with an indent (left margin)
    @Test
    public void testPrintWrapped_withIndent_respectsLeftMargin() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        // Uses overload: printWrapped(PrintWriter, int, int, String)
        formatter.printWrapped(pw, 30, 4, "This is a long line that should wrap with left indent.");
        pw.flush();
        String output = sw.toString();
        String[] lines = output.split(formatter.getNewLine());
        assertTrue(lines.length > 1);
        // First line should start with 4 spaces (the indent)
        assertTrue(lines[0].startsWith("    "));
        // Subsequent lines should also be indented (since nextLineTabStop = indent)
        for (int i = 0; i < lines.length; i++) {
            assertTrue(lines[i].startsWith("    "));
        }
    }

    // Test renderWrappedText when text length <= width (no wrapping)
    @Test
    public void testRenderWrappedText_noWrap_returnsOriginalText() {
        StringBuffer sb = new StringBuffer();
        String text = "short";
        formatter.renderWrappedText(sb, 20, 5, text);
        String result = sb.toString();
        // Should not contain newline because text fits
        assertFalse(result.contains(formatter.getNewLine()));
        assertEquals("short", result.trim());
    }

    // Test findWrapPos when startPos > 0
    @Test
    public void testFindWrapPos_startPosPositive_returnsCorrectPosition() {
        // "hello world beautiful" with width 10 and startPos=3
        // Starting from index 3: "lo world beauti"
        // First space after startPos is at index 6 (relative to original)
        assertEquals(6, formatter.findWrapPos("hello world beautiful", 10, 3));
    }

    // Test createPadding with negative value (should return empty string)
    @Test
    public void testCreatePadding_negativeLength_returnsEmptyString() {
        assertEquals("", formatter.createPadding(-1));
        assertEquals("", formatter.createPadding(-5));
    }

    // Test rtrim with leading spaces only (should not change)
    @Test
    public void testRtrim_leadingSpaces_noChange() {
        assertEquals("   abc", formatter.rtrim("   abc"));
        assertEquals("   abc   ", formatter.rtrim("   abc   ")); // still trailing
    }

    // Test printUsage with options that have no longOpt and no argName
    @Test
    public void testPrintUsage_optionsWithOnlyShortOpt() {
        Options options = new Options();
        options.addOption(new Option("x", null, false, "desc"));
        options.addOption(new Option("y", "yyy", false, "desc y"));
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 50, "app", options);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("-x"));
        assertTrue(output.contains("-y --yyy"));
    }
}