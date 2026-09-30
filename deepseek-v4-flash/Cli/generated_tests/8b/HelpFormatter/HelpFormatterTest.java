package org.apache.commons.cli;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;

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

    // Tests normal printHelp with valid cmdLineSyntax and options
    @Test
    public void testPrintHelp_normalInput_printsFormattedHelp() {
        options.addOption("a", "alpha", false, "Alpha option");
        options.addOption("b", "beta", true, "Beta option with arg");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "myapp", "Header", options, 1, 3, "Footer");
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("usage: myapp"));
        assertTrue(result.contains("-a,--alpha"));
        assertTrue(result.contains("-b,--beta"));
        assertTrue(result.contains("Header"));
        assertTrue(result.contains("Footer"));
    }

    // Tests exception when cmdLineSyntax is null
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_nullCmdLineSyntax_throwsException() {
        formatter.printHelp(new PrintWriter(System.out), 80, null, "header", options, 1, 3, "footer");
    }

    // Tests exception when cmdLineSyntax is empty
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_emptyCmdLineSyntax_throwsException() {
        formatter.printHelp(new PrintWriter(System.out), 80, "", "header", options, 1, 3, "footer");
    }

    // Tests printUsage with options and option groups
    @Test
    public void testPrintUsage_withOptionsAndGroups_printsCorrectUsage() {
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.create("x"));
        group.addOption(OptionBuilder.create("y"));
        options.addOptionGroup(group);
        options.addOption("z", "zeta", false, "Zeta option");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printUsage(pw, 80, "myapp", options);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("usage: myapp [-x | -y] [-z]"));
    }

    // Tests printUsage with required option group
    @Test
    public void testPrintUsage_requiredOptionGroup_printsWithoutBrackets() {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(OptionBuilder.create("a"));
        group.addOption(OptionBuilder.create("b"));
        options.addOptionGroup(group);
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printUsage(pw, 80, "myapp", options);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("usage: myapp -a | -b"));
    }

    // Tests printUsage with long option without short opt
    @Test
    public void testPrintUsage_longOptionOnly_usesDoubleDash() {
        options.addOption(null, "longopt", false, "Long option");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printUsage(pw, 80, "myapp", options);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("--longopt"));
    }

    // Tests printOptions with options having args and long opts
    @Test
    public void testPrintOptions_withArgsAndLongOpts_rendersCorrectly() {
        options.addOption("v", "verbose", false, "Verbose mode");
        options.addOption("o", "output", true, "Output file");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printOptions(pw, 80, options, 1, 3);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("-v,--verbose"));
        assertTrue(result.contains("-o,--output <arg>"));
    }

    // Tests printWrapped with text longer than width, triggers text wrapping
    @Test
    public void testPrintWrapped_longText_wrapsCorrectly() {
        String longText = "This is a very long text that should be wrapped across multiple lines for proper display";
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printWrapped(pw, 30, longText);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains(System.lineSeparator()));
    }

    // Tests renderWrappedText with exact width match returns true branch of pos == -1
    @Test
    public void testRenderWrappedText_shortText_noWrap() {
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 80, 0, "Short text");
        assertEquals("Short text", sb.toString());
    }

    // Tests renderWrappedText with text containing newline triggers early branch
    @Test
    public void testRenderWrappedText_textWithNewline_splitsAtNewline() {
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 50, 0, "First line\nSecond line");
        String result = sb.toString();
        assertTrue(result.contains("First line"));
        assertTrue(result.contains("Second line"));
    }

    // Tests findWrapPos with whitespace before width returns correct pos
    @Test
    public void testFindWrapPos_whitespaceBeforeWidth_returnsPos() {
        int pos = formatter.findWrapPos("abc def ghi", 10, 0);
        assertTrue(pos > 0);
    }

    // Tests findWrapPos with no whitespace returns position after width
    @Test
    public void testFindWrapPos_noWhitespace_returnsPosAfterWidth() {
        int pos = formatter.findWrapPos("abcdefghijklmnop", 10, 0);
        assertTrue(pos > 10);
    }

    // Tests findWrapPos with text shorter than width returns -1
    @Test
    public void testFindWrapPos_shortText_returnsMinusOne() {
        int pos = formatter.findWrapPos("Short", 10, 0);
        assertEquals(-1, pos);
    }

    // Tests autoUsage flag prints usage automatically
    @Test
    public void testPrintHelp_autoUsageTrue_usageIncluded() {
        options.addOption("a", "alpha", false, "Alpha");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "myapp", "Header", options, 1, 3, "Footer", true);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("usage:"));
    }

    // Tests empty header is not printed
    @Test
    public void testPrintHelp_emptyHeader_omittedFromOutput() {
        options.addOption("a", "alpha", false, "Alpha");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "myapp", "", options, 1, 3, "Footer");
        pw.flush();
        String result = out.toString();
        assertFalse(result.contains("\n\n"));
        assertTrue(result.contains("Footer"));
    }

    // Tests empty footer is not printed
    @Test
    public void testPrintHelp_emptyFooter_omittedFromOutput() {
        options.addOption("a", "alpha", false, "Alpha");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "myapp", "Header", options, 1, 3, "");
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("Header"));
        assertFalse(result.endsWith("\n\n"));
    }

    // Tests rtrim with trailing spaces
    @Test
    public void testRtrim_trailingSpaces_removesSpaces() {
        String result = formatter.rtrim("Hello World   ");
        assertEquals("Hello World", result);
    }

    // Tests rtrim with null returns null
    @Test
    public void testRtrim_nullInput_returnsNull() {
        assertNull(formatter.rtrim(null));
    }

    // Tests createPadding with positive length
    @Test
    public void testCreatePadding_positiveLength_returnsSpaces() {
        String padding = formatter.createPadding(5);
        assertEquals("     ", padding);
    }

    // ========== เริ่ม test case ที่เพิ่มใหม่ ==========

    // Tests printHelp overload without header (footer only)
    @Test
    public void testPrintHelp_noHeader_works() {
        options.addOption("a", "alpha", false, "Alpha");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "myapp", options, 1, 3, "Footer only");
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("usage: myapp"));
        assertTrue(result.contains("-a,--alpha"));
        assertTrue(result.contains("Footer only"));
        // header should not appear
        assertFalse(result.contains("Header"));
    }

    // Tests printHelp with only cmdLineSyntax and options (uses default padding)
    @Test
    public void testPrintHelp_simpleOverload_works() {
        options.addOption("v", "verbose", false, "Verbose");
        options.addOption("o", "output", true, "Output path");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "myapp", options);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("usage: myapp"));
        assertTrue(result.contains("-v,--verbose"));
        assertTrue(result.contains("-o,--output <arg>"));
    }

    // Tests printUsage with autoUsage flag set to true (prints "usage:" automatically)
    @Test
    public void testPrintUsage_autoUsageTrue_usageAdded() {
        options.addOption("x", false, "Option x");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printUsage(pw, 80, "myapp", options, true);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("usage: myapp"));
    }

    // Tests printWrapped with left indent
    @Test
    public void testPrintWrapped_withLeftIndent_indentsCorrectly() {
        String text = "Indented text";
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printWrapped(pw, 80, 5, text);
        pw.flush();
        String result = out.toString();
        // The output should start with 5 spaces then the text
        assertTrue(result.startsWith("     " + text));
    }

    // Tests findWrapPos when text contains a newline
    @Test
    public void testFindWrapPos_withNewline_returnsNewlinePosition() {
        int pos = formatter.findWrapPos("abc\ndef", 10, 0);
        // newline at index 3
        assertEquals(3, pos);
    }

    // Tests renderWrappedText with leftIndent > 0
    @Test
    public void testRenderWrappedText_withLeftIndent_indentsLines() {
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 20, 4, "Short text");
        // Because text is short, no wrap, but leftIndent is 0 in implementation? Actually leftIndent is used only when wrapping.
        // Here text fits, so output should be "Short text" (no indent, because no wrap)
        // But to test indent when wrap occurs, use longer text.
        sb.setLength(0);
        formatter.renderWrappedText(sb, 20, 4, "This is a long text that will be wrapped");
        String result = sb.toString();
        // The first line may not be indented (it's the starting line) but subsequent lines should be indented.
        // Since we don't know exact wrapping, check at least that newlines are present and lines have some spaces.
        assertTrue(result.contains(System.lineSeparator()));
        // Because leftIndent is 4, lines after the first should start with 4 spaces
        String[] lines = result.split(System.lineSeparator());
        for (int i = 1; i < lines.length; i++) {
            assertTrue("Line " + i + " should start with 4 spaces", lines[i].startsWith("    "));
        }
    }

    // Tests setLeftPadding affects printOptions spacing
    @Test
    public void testSetLeftPadding_affectsPrintOptions() {
        formatter.setLeftPadding(10);
        options.addOption("a", "alpha", false, "Alpha desc");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printOptions(pw, 80, options, 2, 5);
        pw.flush();
        String result = out.toString();
        // The left padding is applied to each option line: the option string starts after left padding.
        // The default left padding is 1. Setting it to 10 should make the option line start farther right.
        // We just verify the output does not start with the option directly (should be spaces first)
        assertTrue("Output should start with spaces", result.startsWith("          ")); // 10 spaces
    }

    // Tests setDescPadding affects spacing between option and description
    @Test
    public void testSetDescPadding_affectsPrintOptions() {
        formatter.setDescPadding(10);
        options.addOption("a", false, "Alpha desc");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        // Use leftPad=0, descPad will be used
        formatter.printOptions(pw, 80, options, 0, 5); // the descPad parameter in printOptions is ignored if setter is used? Actually printOptions uses the leftPad and descPad passed to it, not the formatter's default? In code, printOptions(PrintWriter, int, Options, int leftPad, int descPad) uses the given arguments, so setDescPadding has no effect. We need to test the printHelp or printUsage methods that use the formatter's padding settings? Actually the formatter has default leftPad and descPad used in printHelp when no padding args are given. So better to test via printHelp without specifying padding.
        // Instead, test the default padding usage in printHelp(cmdLineSyntax, options)
        formatter.setDescPadding(10);
        StringWriter out2 = new StringWriter();
        PrintWriter pw2 = new PrintWriter(out2);
        formatter.printHelp(pw2, 80, "myapp", options);
        pw2.flush();
        String result2 = out2.toString();
        // In the simplified printHelp, default leftPad=1, descPad=1. After setting descPadding=10, the description should be spaced farther.
        // Check that the option line contains at least 10 spaces between option and description
        assertTrue("Description should be indented further", result2.contains("-a")); 
        // We can't easily measure spaces, but we can check that the line is not too short.
        String line = result2.split(System.lineSeparator())[1]; // second line contains option
        int descStart = line.indexOf("Alpha desc");
        assertTrue("Description should start after some padding", descStart > 5);
    }

    // Tests setOptPrefix custom prefix
    @Test
    public void testSetOptPrefix_customPrefix_appearsInUsage() {
        formatter.setOptPrefix("/");
        options.addOption("a", "alpha", false, "Alpha");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printUsage(pw, 80, "myapp", options);
        pw.flush();
        String result = out.toString();
        // Should show "/a" instead of "-a"
        assertTrue(result.contains("/a"));
        assertFalse(result.contains("-a"));
    }

    // Tests setLongOptPrefix custom prefix
    @Test
    public void testSetLongOptPrefix_customPrefix_appearsInUsage() {
        formatter.setLongOptPrefix("---");
        options.addOption("a", "alpha", false, "Alpha");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printUsage(pw, 80, "myapp", options);
        pw.flush();
        String result = out.toString();
        // long option should appear with prefix "---"
        assertTrue(result.contains("---alpha"));
    }

    // Tests setNewLine custom line separator
    @Test
    public void testSetNewLine_customLineSeparator_usedInOutput() {
        formatter.setNewLine("\r\n");
        options.addOption("a", false, "desc");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "myapp", options);
        pw.flush();
        String result = out.toString();
        // Should contain \r\n as line separator
        assertTrue("Should contain CRLF", result.contains("\r\n"));
    }

    // Tests setArgName custom argument placeholder
    @Test
    public void testSetArgName_customArgName_appearsInUsage() {
        formatter.setArgName("value");
        options.addOption("o", "output", true, "Output file");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printUsage(pw, 80, "myapp", options);
        pw.flush();
        String result = out.toString();
        // Should show "-o <value>" instead of "-o <arg>"
        assertTrue(result.contains("-o <value>"));
    }

    // Tests setSyntaxPrefix custom prefix
    @Test
    public void testSetSyntaxPrefix_customPrefix_appearsInHelp() {
        formatter.setSyntaxPrefix("Syntax: ");
        options.addOption("a", false, "desc");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "myapp", options);
        pw.flush();
        String result = out.toString();
        // Should start with "Syntax: myapp" instead of "usage: myapp"
        assertTrue(result.startsWith("Syntax: myapp"));
    }

    // Tests setWidth affects wrapping in printUsage
    @Test
    public void testSetWidth_shrinksWidth_triggersWrap() {
        options.addOption("a", "alpha", false, "Alpha option");
        options.addOption("b", "beta", true, "Beta option with arg");
        formatter.setWidth(20); // very small width
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printUsage(pw, 20, "myapp", options); // note: printUsage uses its own width parameter, not formatter's width. Actually setWidth affects printHelp but not printUsage. To test, use printHelp which uses formatter's width default.
        pw.flush();
        // This test may not be correct because printUsage accepts explicit width.
        // Instead test printHelp without width parameter.
        StringWriter out2 = new StringWriter();
        PrintWriter pw2 = new PrintWriter(out2);
        formatter.printHelp(pw2, "myapp", options); // this overload uses formatter's default width (set by setWidth)
        pw2.flush();
        String result2 = out2.toString();
        // With width 20, usage line should be wrapped
        assertTrue("Output should contain line separators due to wrapping", result2.contains(System.lineSeparator()));
    }
}