package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class HelpFormatterTest {

    private HelpFormatter formatter;
    private ByteArrayOutputStream out;
    private PrintWriter pw;

    @Before
    public void setUp() {
        formatter = new HelpFormatter();
        out = new ByteArrayOutputStream();
        pw = new PrintWriter(out);
    }

    // Tests getters and setters for configuration properties
    @Test
    public void testGettersAndSetters_customValues_returnsExpectedValues() {
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

        formatter.setLongOptSeparator("=");
        assertEquals("=", formatter.getLongOptSeparator());

        formatter.setArgName("value");
        assertEquals("value", formatter.getArgName());
    }

    // Tests option comparator setting and null handling for default comparator
    @Test
    public void testSetOptionComparator_nullValue_setsDefaultComparator() {
        Comparator customComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                return 0;
            }
        };
        formatter.setOptionComparator(customComparator);
        assertEquals(customComparator, formatter.getOptionComparator());

        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
    }

    // Tests rtrim utility method with normal, empty, and null strings
    @Test
    public void testRtrim_variousStrings_trimsTrailingWhitespaceOnly() {
        assertNull(formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("  abc", formatter.rtrim("  abc   "));
        assertEquals("abc", formatter.rtrim("abc\t \n\r"));
        assertEquals("abc", formatter.rtrim("abc"));
    }

    // Tests createPadding utility method
    @Test
    public void testCreatePadding_positiveLength_returnsSpaceString() {
        assertEquals("", formatter.createPadding(0));
        assertEquals("   ", formatter.createPadding(3));
    }

    // Tests findWrapPos when text contains newline or tab within width
    @Test
    public void testFindWrapPos_newlineAndTab_returnsCorrectBreakPosition() {
        assertEquals(5, formatter.findWrapPos("abc\ndef", 10, 0));
        assertEquals(4, formatter.findWrapPos("ab\tcde", 10, 0));
    }

    // Tests findWrapPos when startPos plus width exceeds text length
    @Test
    public void testFindWrapPos_textShorterThanWidth_returnsNegativeOne() {
        assertEquals(-1, formatter.findWrapPos("short", 10, 0));
    }

    // Tests findWrapPos when wrapping on whitespace boundary
    @Test
    public void testFindWrapPos_spaceBoundary_returnsSpacePosition() {
        assertEquals(5, formatter.findWrapPos("hello world", 8, 0));
    }

    // Tests findWrapPos with long word without whitespace (Defects4J CLI-32 defect check)
    @Test
    public void testFindWrapPos_wordLongerThanWidth_handlesWithoutException() {
        int pos = formatter.findWrapPos("abcdefghij", 5, 0);
        assertTrue(pos == -1 || pos == 5 || pos == 10);
    }

    // Tests renderWrappedText with long unbroken string (Defects4J CLI-32 defect check)
    @Test
    public void testRenderWrappedText_wordLongerThanWidth_rendersWithoutStringIndexOutOfBounds() {
        StringBuffer sb = new StringBuffer();
        formatter.setNewLine("\n");
        formatter.renderWrappedText(sb, 5, 0, "abcdefghij");
        assertTrue(sb.length() > 0);
    }

    // Tests renderWrappedText with nextLineTabStop exceeding or equaling width
    @Test
    public void testRenderWrappedText_tabStopExceedsWidth_resetsTabStopToAvoidInfiniteLoop() {
        StringBuffer sb = new StringBuffer();
        formatter.setNewLine("\n");
        formatter.renderWrappedText(sb, 10, 15, "line one is very long and must be wrapped");
        assertTrue(sb.toString().contains("\n"));
    }

    // Tests printHelp with null cmdLineSyntax expecting exception
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_nullCmdLineSyntax_throwsIllegalArgumentException() {
        Options options = new Options();
        formatter.printHelp(pw, 80, null, "header", options, 1, 3, "footer", false);
    }

    // Tests printHelp with empty cmdLineSyntax expecting exception
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_emptyCmdLineSyntax_throwsIllegalArgumentException() {
        Options options = new Options();
        formatter.printHelp(pw, 80, "", "header", options, 1, 3, "footer", false);
    }

    // Tests printHelp with header, footer, options, and autoUsage enabled
    @Test
    public void testPrintHelp_withHeaderFooterAndAutoUsage_printsFormattedHelp() {
        Options options = new Options();
        Option optA = new Option("a", "alpha", false, "Option Alpha");
        Option optB = new Option("b", true, "Option Beta");
        optB.setArgName("val");
        optB.setRequired(true);
        options.addOption(optA);
        options.addOption(optB);

        formatter.setNewLine("\n");
        formatter.printHelp(pw, 80, "myApp", "Header Text", options, 2, 4, "Footer Text", true);
        pw.flush();

        String output = out.toString();
        assertTrue(output.contains("usage: myApp"));
        assertTrue(output.contains("Header Text"));
        assertTrue(output.contains("-a, --alpha"));
        assertTrue(output.contains("-b <val>"));
        assertTrue(output.contains("Option Alpha"));
        assertTrue(output.contains("Option Beta"));
        assertTrue(output.contains("Footer Text"));
    }

    // Tests printUsage with OptionGroup containing mutually exclusive options
    @Test
    public void testPrintUsage_withOptionGroup_printsGroupSyntax() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(false);
        group.addOption(new Option("x", "Option X"));
        group.addOption(new Option("y", "Option Y"));
        options.addOptionGroup(group);

        formatter.setNewLine("\n");
        formatter.printUsage(pw, 80, "myApp", options);
        pw.flush();

        String output = out.toString();
        assertTrue(output.contains("[-x | -y]") || output.contains("[-y | -x]"));
    }

    // Tests printUsage with required OptionGroup
    @Test
    public void testPrintUsage_requiredOptionGroup_printsWithoutEnclosingSquareBrackets() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("x", "Option X"));
        group.addOption(new Option("y", "Option Y"));
        options.addOptionGroup(group);

        formatter.setNewLine("\n");
        formatter.printUsage(pw, 80, "myApp", options);
        pw.flush();

        String output = out.toString();
        assertTrue(output.contains("-x | -y") || output.contains("-y | -x"));
    }

    // Tests printOptions with long option only and blank arg name
    @Test
    public void testPrintOptions_longOptionOnlyAndEmptyArgName_formatsCorrectly() {
        Options options = new Options();
        Option optLongOnly = new Option(null, "longOnly", true, "Long only description");
        optLongOnly.setArgName("");
        options.addOption(optLongOnly);

        formatter.setNewLine("\n");
        formatter.printOptions(pw, 80, options, 1, 3);
        pw.flush();

        String output = out.toString();
        assertTrue(output.contains("--longOnly"));
        assertTrue(output.contains("Long only description"));
    }

    // Tests convenience overloaded printHelp methods for basic execution
    @Test
    public void testPrintHelp_overloadedVariants_executeSuccessfully() {
        Options options = new Options();
        options.addOption("h", "help", false, "Print help");

        formatter.printHelp("app", options);
        formatter.printHelp("app", options, true);
        formatter.printHelp("app", "header", options, "footer");
        formatter.printHelp("app", "header", options, "footer", true);
        formatter.printHelp(80, "app", "header", options, "footer");
        formatter.printHelp(80, "app", "header", options, "footer", true);
        formatter.printHelp(pw, 80, "app", "header", options, 1, 3, "footer");
        pw.flush();

        String output = out.toString();
        assertTrue(output.contains("usage: app"));
        assertTrue(output.contains("-h, --help"));
    }

    // Tests printWrapped convenience methods
    @Test
    public void testPrintWrapped_text_printsWrappedOutput() {
        formatter.printWrapped(pw, 20, "This is a simple text that will be wrapped by the formatter.");
        pw.flush();

        String output = out.toString();
        assertTrue(output.contains("This is a simple"));
    }
}