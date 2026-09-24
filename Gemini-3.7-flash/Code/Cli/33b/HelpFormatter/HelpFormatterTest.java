package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HelpFormatterTest {

    private HelpFormatter formatter;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @Before
    public void setUp() {
        formatter = new HelpFormatter();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    // Tests getter and setter for default properties
    @Test
    public void testGettersAndSetters_normalValues_returnsExpectedValues() {
        formatter.setWidth(80);
        assertEquals(80, formatter.getWidth());

        formatter.setLeftPadding(2);
        assertEquals(2, formatter.getLeftPadding());

        formatter.setDescPadding(5);
        assertEquals(5, formatter.getDescPadding());

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

        Comparator customComparator = CollectionsComparator.INSTANCE;
        formatter.setOptionComparator(customComparator);
        assertEquals(customComparator, formatter.getOptionComparator());

        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
    }

    // Tests printHelp with null or empty command line syntax throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_nullCmdLineSyntax_throwsIllegalArgumentException() {
        Options options = new Options();
        formatter.printHelp(printWriter, 80, null, "header", options, 1, 3, "footer");
    }

    // Tests printHelp with empty string command line syntax throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_emptyCmdLineSyntax_throwsIllegalArgumentException() {
        Options options = new Options();
        formatter.printHelp(printWriter, 80, "", "header", options, 1, 3, "footer");
    }

    // Tests printHelp with autoUsage enabled, header, footer, and standard options
    @Test
    public void testPrintHelp_withHeaderFooterAndAutoUsage_formatsCorrectly() {
        Options options = new Options();
        options.addOption(new Option("a", "enable-a", false, "description of a"));
        Option optB = new Option("b", true, "description of b");
        optB.setRequired(true);
        options.addOption(optB);

        formatter.printHelp(printWriter, 80, "myapp", "Header info", options, 2, 4, "Footer info", true);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("usage: myapp"));
        assertTrue(output.contains("-a,--enable-a"));
        assertTrue(output.contains("-b <arg>"));
        assertTrue(output.contains("Header info"));
        assertTrue(output.contains("Footer info"));
    }

    // Tests printHelp overloads using System.out redirection via internal call
    @Test
    public void testPrintHelp_convenienceMethods_runsWithoutErrors() {
        Options options = new Options();
        options.addOption("c", false, "option c");

        formatter.printHelp("myapp", options);
        formatter.printHelp("myapp", options, true);
        formatter.printHelp("myapp", "header", options, "footer");
        formatter.printHelp("myapp", "header", options, "footer", true);
        formatter.printHelp(80, "myapp", "header", options, "footer");
        formatter.printHelp(80, "myapp", "header", options, "footer", true);
    }

    // Tests printUsage with simple string command syntax
    @Test
    public void testPrintUsage_stringSyntax_printsFormattedUsage() {
        formatter.printUsage(printWriter, 80, "app -a -b [files...]");
        printWriter.flush();

        String expected = "usage: app -a -b [files...]" + formatter.getNewLine();
        assertEquals(expected, stringWriter.toString());
    }

    // Tests printUsage with OptionGroup (required and optional)
    @Test
    public void testPrintUsage_withOptionGroup_printsGroupFormat() {
        Options options = new Options();
        OptionGroup requiredGroup = new OptionGroup();
        requiredGroup.setRequired(true);
        requiredGroup.addOption(new Option("f", "file", true, "file"));
        requiredGroup.addOption(new Option("s", "stream", true, "stream"));
        options.addOptionGroup(requiredGroup);

        OptionGroup optionalGroup = new OptionGroup();
        optionalGroup.setRequired(false);
        optionalGroup.addOption(new Option("x", "exclude", false, "exclude"));
        optionalGroup.addOption(new Option("i", "include", false, "include"));
        options.addOptionGroup(optionalGroup);

        formatter.printUsage(printWriter, 80, "myapp", options);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.startsWith("usage: myapp "));
        assertTrue(output.contains("-f <file>") || output.contains("-f <arg>"));
        assertTrue(output.contains(" | "));
        assertTrue(output.contains("[-i | -x]") || output.contains("[-x | -i]"));
    }

    // Tests long option without short option and custom argument name
    @Test
    public void testRenderOptions_longOptOnlyAndCustomArgName_rendersProperFormat() {
        Options options = new Options();
        Option longOnly = new Option(null, "config", true, "configuration path");
        longOnly.setArgName("PATH");
        options.addOption(longOnly);

        Option blankArgName = new Option("b", "blank", true, "blank arg name");
        blankArgName.setArgName("");
        options.addOption(blankArgName);

        formatter.setLongOptSeparator("=");
        formatter.printOptions(printWriter, 80, options, 1, 3);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("--config=<PATH>"));
        assertTrue(output.contains("-b,--blank"));
    }

    // Tests wrapping when nextLineTabStop is greater than or equal to width
    @Test
    public void testRenderWrappedText_tabStopGreaterThanWidth_resetsTabStop() {
        StringBuffer sb = new StringBuffer();
        String text = "Line that is long enough to require wrapping into a second line of text.";
        formatter.renderWrappedText(sb, 20, 25, text);

        String result = sb.toString();
        assertTrue(result.contains(formatter.getNewLine()));
    }

    // Tests findWrapPos boundary conditions (newline, tab, no whitespace, end of text)
    @Test
    public void testFindWrapPos_variousScenarios_returnsExpectedPositions() {
        String textWithNewline = "first line\nsecond line";
        assertEquals(11, formatter.findWrapPos(textWithNewline, 20, 0));

        String textWithTab = "tab\tseparated";
        assertEquals(4, formatter.findWrapPos(textWithTab, 20, 0));

        String textNoWhitespace = "supercalifragilisticexpialidocious";
        assertEquals(10, formatter.findWrapPos(textNoWhitespace, 10, 0));

        String shortText = "short";
        assertEquals(-1, formatter.findWrapPos(shortText, 10, 0));
    }

    // Tests createPadding and rtrim helper methods
    @Test
    public void testCreatePaddingAndRtrim_normalAndEdgeCases_returnsExpectedResults() {
        assertEquals("    ", formatter.createPadding(4));
        assertEquals("", formatter.createPadding(0));

        assertEquals("abc", formatter.rtrim("abc   "));
        assertEquals("abc", formatter.rtrim("abc\t\n\r"));
        assertEquals("", formatter.rtrim("   "));
        assertEquals("", formatter.rtrim(""));
        assertEquals(null, formatter.rtrim(null));
    }

    // Tests default OptionComparator sorting behavior
    @Test
    public void testOptionComparator_sortsAlphabeticallyCaseInsensitive() {
        Comparator comp = formatter.getOptionComparator();
        Option optA = new Option("a", "First");
        Option optB = new Option("B", "Second");

        assertTrue(comp.compare(optA, optB) < 0);
        assertTrue(comp.compare(optB, optA) > 0);
        assertEquals(0, comp.compare(optA, new Option("A", "First duplicate")));
    }

    // Dummy comparator for testing custom comparator setting
    private static class CollectionsComparator implements Comparator {
        static final CollectionsComparator INSTANCE = new CollectionsComparator();

        public int compare(Object o1, Object o2) {
            return 0;
        }
    }
}