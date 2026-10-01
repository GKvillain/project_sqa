package org.apache.commons.cli;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class HelpFormatterTest {
    private HelpFormatter formatter;

    @Before
    public void setUp() {
        formatter = new HelpFormatter();
    }

    // Tests null cmdLineSyntax throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_nullCmdLineSyntax_throwsIllegalArgumentException() {
        formatter.printHelp((String) null, new Options());
    }

    // Tests empty cmdLineSyntax throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_emptyCmdLineSyntax_throwsIllegalArgumentException() {
        formatter.printHelp("", new Options());
    }

    // Tests simple usage output is correct
    @Test
    public void testPrintHelp_simpleUsage_outputCorrect() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 74, "myapp", null, new Options(), 1, 3, null, false);
        pw.flush();
        String output = sw.toString();
        String newLine = formatter.getNewLine();
        String expected = "usage: myapp" + newLine;
        assertEquals("Simple usage should match", expected, output);
    }

    // Tests printHelp with autoUsage shows options
    @Test
    public void testPrintHelp_withAutoUsage_includesOptions() {
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose mode");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 74, "app", null, options, 1, 3, null, true);
        pw.flush();
        String output = sw.toString();
        assertTrue("Output should contain -v option", output.contains("-v"));
    }

    // Tests printHelp with header starting with newline does not produce extra blank line
    @Test
    public void testPrintHelp_headerStartingWithNewline_noExtraBlankLine() {
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose mode");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 74, "app", "\nHeader", options, 1, 3, null, false);
        pw.flush();
        String output = sw.toString();
        String newLine = formatter.getNewLine();
        String expectedStart = "usage: app" + newLine + "Header";
        assertTrue("Header should appear immediately after usage line", output.startsWith(expectedStart));
    }

    // Tests printWrapped with text starting with tab does not throw and contains text
    @Test
    public void testPrintWrapped_textStartingWithTab_handled() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printWrapped(pw, 74, "\tHello");
        pw.flush();
        String output = sw.toString();
        assertFalse("Output should not be empty", output.isEmpty());
        assertTrue("Output should contain Hello", output.contains("Hello"));
    }

    // Tests printWrapped with text longer than width wraps lines
    @Test
    public void testPrintWrapped_longText_wrapsCorrectly() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String longText = "This is a long text that should be wrapped to multiple lines because it exceeds the set width.";
        formatter.printWrapped(pw, 30, longText);
        pw.flush();
        String output = sw.toString();
        assertTrue("Wrapped output should contain newline", output.contains(formatter.getNewLine()));
    }

    // Tests printWrapped when nextLineTabStop >= width uses minimum value
    @Test
    public void testPrintWrapped_nextLineTabStopEqualsWidth_noException() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printWrapped(pw, 10, 10, "some text");
        pw.flush();
        String output = sw.toString();
        assertNotNull(output);
    }

    // Tests findWrapPos returns -1 when text fits within width
    @Test
    public void testFindWrapPos_textShorterThanWidth_returnsMinusOne() {
        int pos = formatter.findWrapPos("short", 74, 0);
        assertEquals(-1, pos);
    }

    // Tests findWrapPos returns index+1 when newline found
    @Test
    public void testFindWrapPos_textWithNewline_returnsPosPlusOne() {
        int pos = formatter.findWrapPos("12345\n67890", 10, 0);
        assertEquals(6, pos);
    }

    // Tests findWrapPos returns position of last whitespace within width
    @Test
    public void testFindWrapPos_textWithWhitespace_returnsCorrectPos() {
        int pos = formatter.findWrapPos("abc def ghi", 5, 0);
        assertEquals(3, pos);
    }

    // Tests renderOptions with long option and arg name renders correctly
    @Test
    public void testRenderOptions_longOptionWithArgName_rendersCorrectly() {
        Options options = new Options();
        Option opt = new Option("o", "output", true, "output file");
        opt.setArgName("file");
        options.addOption(opt);
        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 74, options, 1, 3);
        String rendered = sb.toString();
        assertTrue("Rendered options should contain --output <file>", rendered.contains("--output <file>"));
    }

    // Tests printUsage with OptionGroup shows brackets
    @Test
    public void testPrintUsage_withOptionGroup_showsBrackets() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "option-a", false, "desc a"));
        group.addOption(new Option("b", null, false, "desc b"));
        options.addOptionGroup(group);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 74, "app", options);
        pw.flush();
        String output = sw.toString();
        assertTrue("Usage output should contain group in brackets", output.contains("[-a | -b]"));
    }

    // Tests printOptions outputs option descriptions
    @Test
    public void testPrintOptions_withOptions_outputContainsOptionDescriptions() {
        Options options = new Options();
        options.addOption("v", "verbose", false, "be verbose");
        options.addOption("o", "output", true, "output file");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printOptions(pw, 74, options, 2, 4);
        pw.flush();
        String output = sw.toString();
        assertTrue("Output should contain description of verbose", output.contains("be verbose"));
        assertTrue("Output should contain description of output file", output.contains("output file"));
    }

    // Tests printUsage with required option does not enclose with brackets
    @Test
    public void testPrintUsage_requiredOption_noBrackets() {
        Options options = new Options();
        Option opt = new Option("v", null, false, "desc");
        opt.setRequired(true);
        options.addOption(opt);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 74, "app", options);
        pw.flush();
        String output = sw.toString();
        assertTrue("Usage output should contain -v", output.contains("-v"));
        assertFalse("Required option should not be in brackets", output.contains("[-v]"));
    }

    // Tests printHelp with null header and footer does not throw
    @Test
    public void testPrintHelp_nullHeaderAndFooter_noException() {
        Options options = new Options();
        options.addOption("a", "all", false, "all items");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 74, "app", null, options, 1, 3, null, false);
        pw.flush();
        String output = sw.toString();
        assertTrue("Output should contain usage line", output.contains("usage: app"));
    }

    // Tests printHelp with empty header and footer does not throw
    @Test
    public void testPrintHelp_emptyHeaderAndFooter_noException() {
        Options options = new Options();
        options.addOption("a", "all", false, "all items");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 74, "app", "", options, 1, 3, "", false);
        pw.flush();
        String output = sw.toString();
        assertTrue("Output should contain usage line", output.contains("usage: app"));
    }

    // Tests printUsage without options produces only syntax prefix and app name
    @Test
    public void testPrintUsage_simpleUsage_correct() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 74, "myapp");
        pw.flush();
        String output = sw.toString();
        String newLine = formatter.getNewLine();
        String expected = "usage: myapp" + newLine;
        assertEquals(expected, output);
    }
}