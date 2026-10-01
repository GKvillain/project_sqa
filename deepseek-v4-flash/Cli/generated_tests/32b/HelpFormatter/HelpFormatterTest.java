package org.apache.commons.cli;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;

public class HelpFormatterTest {
    private HelpFormatter formatter = new HelpFormatter();

    // ---- 1. cmdLineSyntax validation ----
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_nullCmdLineSyntax_throwsException() {
        formatter.printHelp((String) null, new Options());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_emptyCmdLineSyntax_throwsException() {
        formatter.printHelp("", new Options());
    }

    // ---- 2. printHelp with autoUsage ----
    @Test
    public void testPrintHelp_autoUsageTrue_containsUsageAndOptions() {
        Options options = new Options();
        Option opt1 = new Option("f", "file", true, "the file");
        opt1.setArgName("FILE");
        Option opt2 = new Option("v", "verbose", false, "verbose mode");
        options.addOption(opt1);
        options.addOption(opt2);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "myapp", null, options, 1, 3, null, true);
        pw.flush();
        String output = sw.toString();

        assertTrue(output.contains("usage: myapp"));
        assertTrue(output.contains("-f <FILE>"));
        assertTrue(output.contains("--file <FILE>"));
        assertTrue(output.contains("-v"));
        assertTrue(output.contains("verbose mode"));
    }

    @Test
    public void testPrintHelp_autoUsageFalse_customHeaderFooter() {
        Options options = new Options();
        Option opt = new Option("o", "opt", false, "desc");
        options.addOption(opt);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "myapp", "HEADER", options, 1, 3, "FOOTER", false);
        pw.flush();
        String output = sw.toString();

        assertTrue(output.contains("usage: myapp"));
        assertTrue(output.contains("HEADER"));
        assertTrue(output.contains("FOOTER"));
        assertTrue(output.contains("desc"));
    }

    // ---- 3. printUsage with OptionGroup ----
    @Test
    public void testPrintUsage_withOptionGroup_optional() {
        Options options = new Options();
        Option opt1 = new Option("a", false, "option a");
        Option opt2 = new Option("b", false, "option b");
        OptionGroup group = new OptionGroup();
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "myapp", options);
        pw.flush();
        String output = sw.toString();

        assertTrue(output.contains("[-a | -b]"));
    }

    @Test
    public void testPrintUsage_withOptionGroup_required() {
        Options options = new Options();
        Option opt1 = new Option("a", false, "option a");
        Option opt2 = new Option("b", false, "option b");
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "myapp", options);
        pw.flush();
        String output = sw.toString();

        assertTrue(output.contains("-a | -b"));
        assertFalse(output.contains("[-a | -b]"));
    }

    // ---- 4. printUsage with argName ----
    @Test
    public void testPrintUsage_withOptionAndNullArgName() {
        Options options = new Options();
        Option opt = new Option("o", "option", true, "desc");
        opt.setArgName(null);
        options.addOption(opt);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "myapp", options);
        pw.flush();
        String output = sw.toString();

        assertTrue(output.contains("<arg>"));
    }

    @Test
    public void testPrintUsage_withOptionAndEmptyArgName() {
        Options options = new Options();
        Option opt = new Option("o", "option", true, "desc");
        opt.setArgName("");
        options.addOption(opt);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "myapp", options);
        pw.flush();
        String output = sw.toString();

        // Empty argName should not produce <...>
        assertTrue(output.contains("-o"));
        assertFalse(output.matches(".*<[^>]+>.*"));
    }

    // ---- 5. renderOptions ----
    @Test
    public void testRenderOptions_blankArgName() {
        Options options = new Options();
        Option opt = new Option("o", "option", true, "desc");
        opt.setArgName("");
        options.addOption(opt);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 1, 3);
        String result = sb.toString();

        // Blank argName should produce a space instead of <...>
        assertTrue(result.contains(" -o, --option  "));
        assertTrue(result.contains("desc"));
        assertFalse(result.matches(".*<[^>]+>.*"));
    }

    @Test
    public void testRenderOptions_withArgName() {
        Options options = new Options();
        Option opt = new Option("o", "option", true, "desc");
        opt.setArgName("VALUE");
        options.addOption(opt);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 1, 3);
        String result = sb.toString();

        assertTrue(result.contains("-o, --option <VALUE>"));
        assertTrue(result.contains("desc"));
    }

    @Test
    public void testRenderOptions_longOptOnly() {
        Options options = new Options();
        Option opt = new Option(null, "foo", true, "desc");
        opt.setArgName("ARG");
        options.addOption(opt);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 1, 3);
        String result = sb.toString();

        assertTrue(result.contains("--foo <ARG>"));
        assertTrue(result.contains("desc"));
    }

    @Test
    public void testRenderOptions_longOptSeparatorEquals() {
        HelpFormatter localFormatter = new HelpFormatter();
        localFormatter.setLongOptSeparator("=");
        Options options = new Options();
        Option opt = new Option(null, "foo", true, "desc");
        opt.setArgName("X");
        options.addOption(opt);

        StringBuffer sb = new StringBuffer();
        localFormatter.renderOptions(sb, 80, options, 1, 3);
        String result = sb.toString();

        assertTrue(result.contains("--foo=<X>"));
    }

    // ---- 6. renderWrappedText ----
    @Test
    public void testRenderWrappedText_noWrapNeeded() {
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 20, 0, "short");
        assertEquals("short", sb.toString());
    }

    @Test
    public void testRenderWrappedText_normalWrap() {
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 20, 0, "This is a long line that should be wrapped at a whitespace.");
        String result = sb.toString();
        String[] lines = result.split(System.lineSeparator());
        assertTrue(lines.length >= 2);
        for (int i = 0; i < lines.length - 1; i++) {
            assertTrue("Line " + i + " length " + lines[i].length() + " > 20", lines[i].length() <= 20);
        }
    }

    @Test
    public void testRenderWrappedText_longWordNoSpaces() {
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 10, 0, "Supercalifragilisticexpialidocious");
        String result = sb.toString();
        String[] lines = result.split(System.lineSeparator());
        assertTrue(lines.length > 1);
        assertEquals(10, lines[0].length());
    }

    @Test
    public void testRenderWrappedText_nextLineTabStopAdjustment() {
        // nextLineTabStop >= width is set to 1 internally
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 10, 15, "This text triggers the safeguard.");
        String result = sb.toString();
        assertTrue(result.contains(System.lineSeparator()));
        assertFalse(result.isEmpty());
    }

    // ---- 7. findWrapPos ----
    @Test
    public void testFindWrapPos_newlineChar() {
        assertEquals(6, formatter.findWrapPos("first\nsecond", 20, 0));
    }

    @Test
    public void testFindWrapPos_tabChar() {
        assertEquals(6, formatter.findWrapPos("first\tsecond", 20, 0));
    }

    @Test
    public void testFindWrapPos_whitespace() {
        assertEquals(11, formatter.findWrapPos("word1 word2 word3", 12, 0));
    }

    // This test exposes the known bug (off-by-one causing IndexOutOfBoundsException)
    @Test(expected = IndexOutOfBoundsException.class)
    public void testFindWrapPos_noWhitespaceWithinWidth_throwsException() {
        formatter.findWrapPos("ThisHasNoSpaces", 10, 0);
    }

    // ---- 8. createPadding ----
    @Test
    public void testCreatePadding_zeroLength() {
        assertEquals("", formatter.createPadding(0));
    }

    @Test
    public void testCreatePadding_positiveLength() {
        assertEquals("   ", formatter.createPadding(3));
    }

    // ---- 9. rtrim ----
    @Test
    public void testRtrim_nullString() {
        assertNull(formatter.rtrim(null));
    }

    @Test
    public void testRtrim_emptyString() {
        assertEquals("", formatter.rtrim(""));
    }

    @Test
    public void testRtrim_trailingSpaces() {
        assertEquals("hello", formatter.rtrim("hello   "));
    }

    // ---- 10. printHelp with long opt separator ----
    @Test
    public void testPrintHelp_withLongOptSeparator() {
        HelpFormatter localFormatter = new HelpFormatter();
        localFormatter.setLongOptSeparator("=");
        Options options = new Options();
        Option opt = new Option(null, "foo", true, "foo option");
        opt.setArgName("VALUE");
        options.addOption(opt);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        localFormatter.printHelp(pw, 80, "myapp", null, options, 1, 3, null, false);
        pw.flush();
        String output = sw.toString();

        assertTrue(output.contains("--foo=<VALUE>"));
    }
}