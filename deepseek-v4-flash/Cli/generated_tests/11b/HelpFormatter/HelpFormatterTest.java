package org.apache.commons.cli;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

public class HelpFormatterTest {

    // Tests printHelp with short cmdLineSyntax and no header/footer
    @Test
    public void testPrintHelp_shortCmdLineSyntax_noHeaderFooter() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "myapp", null, options, 2, 2, null);
        pw.flush();
        String result = sw.toString();
        assertTrue(result.startsWith("usage: myapp"));
        assertFalse(result.contains("null"));
    }

    // Tests printUsage with options containing required option
    @Test
    public void testPrintUsage_options_requiredOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption(OptionBuilder.isRequired().create("v"));
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "myapp", options);
        pw.flush();
        String result = sw.toString();
        assertTrue(result.contains("-v"));
    }

    // Tests printUsage with options containing optional option
    @Test
    public void testPrintUsage_options_optionalOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("o", false, "optional option");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "myapp", options);
        pw.flush();
        String result = sw.toString();
        assertTrue(result.contains("[-o]"));
    }

    // Tests printUsage with option group (optional)
    @Test
    public void testPrintUsage_optionGroup_optional() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.create("a"));
        group.addOption(OptionBuilder.create("b"));
        options.addOptionGroup(group);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "myapp", options);
        pw.flush();
        String result = sw.toString();
        assertTrue(result.contains("[-a") || result.contains("[-b"));
    }

    // Tests printUsage with option group (required)
    @Test
    public void testPrintUsage_optionGroup_required() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(OptionBuilder.create("a"));
        group.addOption(OptionBuilder.create("b"));
        options.addOptionGroup(group);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "myapp", options);
        pw.flush();
        String result = sw.toString();
        assertTrue(result.contains(" -a | -b"));
    }

    // Tests printUsage with long option
    @Test
    public void testPrintUsage_longOption() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption(null, "long", false, "long option");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "myapp", options);
        pw.flush();
        String result = sw.toString();
        assertTrue(result.contains("--long"));
    }

    // Tests printUsage with option that has arg and argName
    @Test
    public void testPrintUsage_optionWithArgAndArgName() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "file", true, "file option");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "myapp", options);
        pw.flush();
        String result = sw.toString();
        assertTrue(result.contains(" -f <arg>"));
    }

    // Tests renderOptions with multiple options to check prefix formatting
    @Test
    public void testRenderOptions_multipleOptions_correctPrefix() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "aaa", false, "description for a");
        options.addOption("b", null, false, "description for b");
        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 1, 3);
        String result = sb.toString();
        assertTrue(result.contains("-a,--aaa"));
    }

    // Tests renderWrappedText with text that needs wrapping
    @Test
    public void testRenderWrappedText_wrapNeeded_returnsWrappedText() {
        HelpFormatter formatter = new HelpFormatter();
        StringBuffer sb = new StringBuffer();
        String text = "This is a long text that should be wrapped at some point";
        formatter.renderWrappedText(sb, 20, 5, text);
        String result = sb.toString();
        assertTrue(result.contains("\n"));
    }

    // Tests renderWrappedText with short text, no wrapping needed
    @Test
    public void testRenderWrappedText_noWrap_returnsSameText() {
        HelpFormatter formatter = new HelpFormatter();
        StringBuffer sb = new StringBuffer();
        String text = "short";
        formatter.renderWrappedText(sb, 20, 5, text);
        String result = sb.toString();
        assertEquals("short", result.trim());
    }

    // Tests findWrapPos with newline character within width
    @Test
    public void testFindWrapPos_newlineWithinWidth_returnsPositionAfterNewline() {
        HelpFormatter formatter = new HelpFormatter();
        int pos = formatter.findWrapPos("hello\nworld", 20, 0);
        assertEquals(6, pos);
    }

    // Tests findWrapPos with tab character within width
    @Test
    public void testFindWrapPos_tabWithinWidth_returnsPositionAfterTab() {
        HelpFormatter formatter = new HelpFormatter();
        int pos = formatter.findWrapPos("hello\tworld", 20, 0);
        assertEquals(6, pos);
    }

    // Tests findWrapPos when text fits within width, returns -1
    @Test
    public void testFindWrapPos_textFitsWidth_returnsMinusOne() {
        HelpFormatter formatter = new HelpFormatter();
        int pos = formatter.findWrapPos("short", 20, 0);
        assertEquals(-1, pos);
    }

    // Tests findWrapPos when wrap position found at whitespace
    @Test
    public void testFindWrapPos_wrapAtWhitespace_returnsPosition() {
        HelpFormatter formatter = new HelpFormatter();
        int pos = formatter.findWrapPos("hello world", 8, 0);
        assertEquals(5, pos);
    }

    // Tests findWrapPos when no whitespace found and text is longer than width
    @Test
    public void testFindWrapPos_noWhitespace_returnsPositionBeyondWidth() {
        HelpFormatter formatter = new HelpFormatter();
        int pos = formatter.findWrapPos("helloworld", 5, 0);
        assertTrue(pos > 0);
    }

    // Tests rtrim with trailing spaces
    @Test
    public void testRtrim_withTrailingSpaces_returnsTrimmedString() {
        HelpFormatter formatter = new HelpFormatter();
        String result = formatter.rtrim("hello   ");
        assertEquals("hello", result);
    }

    // Tests rtrim with null input
    @Test
    public void testRtrim_nullInput_returnsNull() {
        HelpFormatter formatter = new HelpFormatter();
        String result = formatter.rtrim(null);
        assertNull(result);
    }

    // Tests rtrim with empty string
    @Test
    public void testRtrim_emptyString_returnsEmptyString() {
        HelpFormatter formatter = new HelpFormatter();
        String result = formatter.rtrim("");
        assertEquals("", result);
    }

    // Tests setOptionComparator with null resets to default comparator
    @Test
    public void testSetOptionComparator_null_setsDefaultComparator() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
        assertTrue(formatter.getOptionComparator() instanceof HelpFormatter.OptionComparator);
    }

    // Tests printHelp with autoUsage true
    @Test
    public void testPrintHelp_autoUsageTrue_containsUsage() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("v", false, "verbose");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "myapp", null, options, 2, 2, null, true);
        pw.flush();
        String result = sw.toString();
        assertTrue(result.contains("usage: myapp"));
    }

    // Tests printHelp with header and footer
    @Test
    public void testPrintHelp_withHeaderFooter_containsBoth() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "myapp", "Header", options, 2, 2, "Footer");
        pw.flush();
        String result = sw.toString();
        assertTrue(result.contains("Header"));
        assertTrue(result.contains("Footer"));
    }

    // Tests printHelp with null cmdLineSyntax throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_nullCmdLineSyntax_throwsException() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, null, null, options, 2, 2, null);
    }

    // Tests printHelp with empty cmdLineSyntax throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_emptyCmdLineSyntax_throwsException() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "", null, options, 2, 2, null);
    }

    // Tests printUsage with simple cmdLineSyntax
    @Test
    public void testPrintUsage_simpleCmdLineSyntax() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "myapp");
        pw.flush();
        String result = sw.toString();
        assertTrue(result.startsWith("usage: myapp"));
    }

    // Tests createPadding with positive length
    @Test
    public void testCreatePadding_positiveLength_returnsSpaces() {
        HelpFormatter formatter = new HelpFormatter();
        String padding = formatter.createPadding(5);
        assertEquals("     ", padding);
    }

    // Tests createPadding with zero length
    @Test
    public void testCreatePadding_zeroLength_returnsEmptyString() {
        HelpFormatter formatter = new HelpFormatter();
        String padding = formatter.createPadding(0);
        assertEquals("", padding);
    }

    // ==================== New tests for uncovered areas ====================

    // Tests setOptionComparator with a custom comparator (reverse order)
    @Test
    public void testSetOptionComparator_withCustomComparator_affectsOrder() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        // set comparator to reverse order
        formatter.setOptionComparator(new Comparator<Option>() {
            @Override
            public int compare(Option o1, Option o2) {
                return o2.getOpt().compareTo(o1.getOpt());
            }
        });
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "myapp", options);
        pw.flush();
        String result = sw.toString();
        // default order would be "-a -b", reversed is "-b -a"
        assertTrue("Expected -b before -a", result.indexOf("-b") < result.indexOf("-a"));
    }

    // Tests getOptionComparator returns non-null by default
    @Test
    public void testGetOptionComparator_defaultNotNull() {
        HelpFormatter formatter = new HelpFormatter();
        assertNotNull("Default comparator should not be null", formatter.getOptionComparator());
    }

    // Tests setWidth and getWidth
    @Test
    public void testSetGetWidth() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());
    }

    // Tests setLeftPadding and getLeftPadding
    @Test
    public void testSetGetLeftPadding() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());
    }

    // Tests setLongOptSeparator and getLongOptSeparator
    @Test
    public void testSetGetLongOptSeparator() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setLongOptSeparator(" = ");
        assertEquals(" = ", formatter.getLongOptSeparator());
    }

    // Tests renderWrappedText with null text throws exception
    @Test(expected = NullPointerException.class)
    public void testRenderWrappedText_nullText_throwsException() {
        HelpFormatter formatter = new HelpFormatter();
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 20, 5, null);
    }

    // Tests renderWrappedText with text containing newline preserves newline
    @Test
    public void testRenderWrappedText_textWithNewline_preservesNewline() {
        HelpFormatter formatter = new HelpFormatter();
        StringBuffer sb = new StringBuffer();
        String text = "line1\nline2";
        formatter.renderWrappedText(sb, 80, 0, text);
        String result = sb.toString();
        assertTrue("Should contain newline", result.contains("\n"));
        // first line should be "line1"
        assertTrue(result.startsWith("line1"));
    }

    // Tests findWrapPos with non-zero startPos
    @Test
    public void testFindWrapPos_startPosNonZero_returnsCorrectPosition() {
        HelpFormatter formatter = new HelpFormatter();
        // "hello world" from position 3: look for wrap within width 8 starting at 3
        // characters after index 3: "o world"
        // width 8 means we can go up to index 3+8-1 = 10, but string length is 11.
        // whitespace at 5 (relative to start=3? actual index 5) 
        // findWrapPos("hello world", 8, 3) should return 5 (the space after "hello")
        int pos = formatter.findWrapPos("hello world", 8, 3);
        assertEquals(5, pos);
    }

    // Tests printUsage with null options does not throw
    @Test
    public void testPrintUsage_nullOptions_noException() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        // should not throw
        formatter.printUsage(pw, 80, "myapp", null);
        pw.flush();
        String result = sw.toString();
        assertTrue("Should still produce usage", result.startsWith("usage: myapp"));
    }

    // Tests printHelp with null options throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_nullOptions_throwsException() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "myapp", null, null, 2, 2, null);
    }
}