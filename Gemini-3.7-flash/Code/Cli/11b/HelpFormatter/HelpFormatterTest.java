package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

import static org.junit.Assert.*;

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

    // Tests getters and setters for default properties
    @Test
    public void testGettersAndSetters_defaultProperties_updateCorrectly() {
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

        formatter.setArgName("argument");
        assertEquals("argument", formatter.getArgName());
    }

    // Tests setting comparator to null restores default comparator
    @Test
    public void testSetOptionComparator_nullValue_restoresDefaultComparator() {
        Comparator customComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                return 0;
            }
        };

        formatter.setOptionComparator(customComparator);
        assertSame(customComparator, formatter.getOptionComparator());

        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
        assertNotSame(customComparator, formatter.getOptionComparator());
    }

    // Tests printHelp with null cmdLineSyntax throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_nullCmdLineSyntax_throwsException() {
        Options options = new Options();
        formatter.printHelp(printWriter, 80, null, "header", options, 1, 3, "footer", false);
    }

    // Tests printHelp with empty cmdLineSyntax throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_emptyCmdLineSyntax_throwsException() {
        Options options = new Options();
        formatter.printHelp(printWriter, 80, "", "header", options, 1, 3, "footer", false);
    }

    // Tests printHelp with autoUsage false, headers, options, and footers
    @Test
    public void testPrintHelp_manualUsageWithHeaderAndFooter_outputsExpectedText() {
        Options options = new Options();
        options.addOption("a", "all", false, "do not ignore entries starting with .");
        options.addOption("l", false, "use a long listing format");

        formatter.printHelp(printWriter, 80, "ls [OPTION]... [FILE]...", "Header text", options, 2, 4, "Footer text", false);
        printWriter.flush();

        String result = stringWriter.toString();
        assertTrue(result.contains("usage: ls [OPTION]... [FILE]..."));
        assertTrue(result.contains("Header text"));
        assertTrue(result.contains("-a,--all"));
        assertTrue(result.contains("do not ignore entries starting with ."));
        assertTrue(result.contains("-l"));
        assertTrue(result.contains("use a long listing format"));
        assertTrue(result.contains("Footer text"));
    }

    // Tests printUsage with autoUsage true and single optional option
    @Test
    public void testPrintUsage_singleOptionalOption_rendersSquareBrackets() {
        Options options = new Options();
        options.addOption("f", "file", false, "target file");

        formatter.printUsage(printWriter, 80, "myapp", options);
        printWriter.flush();

        String result = stringWriter.toString().trim();
        assertEquals("usage: myapp [-f]", result);
    }

    // Tests printUsage with required option and option with argument name
    @Test
    public void testPrintUsage_requiredOptionWithArgName_rendersWithoutBracketsAndWithArg() {
        Options options = new Options();
        Option option = new Option("c", "config", true, "config file");
        option.setRequired(true);
        option.setArgName("path");
        options.addOption(option);

        formatter.printUsage(printWriter, 80, "myapp", options);
        printWriter.flush();

        String result = stringWriter.toString().trim();
        assertEquals("usage: myapp -c <path>", result);
    }

    // Tests printUsage with empty argName (Defects4J CLI-11 regression test)
    @Test
    public void testPrintUsage_optionWithEmptyArgName_doesNotRenderEmptyBrackets() {
        Options options = new Options();
        Option option = new Option("f", true, "specify file");
        option.setArgName("");
        options.addOption(option);

        formatter.printUsage(printWriter, 80, "myapp", options);
        printWriter.flush();

        String result = stringWriter.toString().trim();
        assertEquals("usage: myapp [-f]", result);
    }

    // Tests printUsage with long-only option
    @Test
    public void testPrintUsage_longOnlyOption_rendersDoubleDashPrefix() {
        Options options = new Options();
        Option option = new Option(null, "verbose", false, "verbose output");
        options.addOption(option);

        formatter.printUsage(printWriter, 80, "myapp", options);
        printWriter.flush();

        String result = stringWriter.toString().trim();
        assertEquals("usage: myapp [--verbose]", result);
    }

    // Tests printUsage with OptionGroup (both optional and required groups)
    @Test
    public void testPrintUsage_optionGroups_rendersCorrectGroupingSyntax() {
        Options options = new Options();

        OptionGroup optionalGroup = new OptionGroup();
        optionalGroup.addOption(new Option("a", "alpha option"));
        optionalGroup.addOption(new Option("b", "beta option"));
        optionalGroup.setRequired(false);
        options.addOptionGroup(optionalGroup);

        OptionGroup requiredGroup = new OptionGroup();
        requiredGroup.addOption(new Option("x", "x option"));
        requiredGroup.addOption(new Option("y", "y option"));
        requiredGroup.setRequired(true);
        options.addOptionGroup(requiredGroup);

        formatter.printUsage(printWriter, 80, "myapp", options);
        printWriter.flush();

        String result = stringWriter.toString().trim();
        assertTrue(result.contains("[-a | -b]"));
        assertTrue(result.contains("-x | -y"));
        assertFalse(result.contains("[-x | -y]"));
    }

    // Tests renderOptions with long options only and arguments without argName
    @Test
    public void testRenderOptions_longOptionWithArgWithoutArgName_padsProperly() {
        Options options = new Options();
        Option option = new Option(null, "config", true, "configuration path");
        option.setArgName(null);
        options.addOption(option);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 1, 3);

        String result = sb.toString();
        assertTrue(result.contains("--config"));
        assertTrue(result.contains("configuration path"));
    }

    // Tests findWrapPos with explicit newline character within width
    @Test
    public void testFindWrapPos_newlineWithinWidth_returnsPosAfterNewline() {
        String text = "Line 1\nLine 2";
        int pos = formatter.findWrapPos(text, 10, 0);
        assertEquals(7, pos);
    }

    // Tests findWrapPos with tab character within width
    @Test
    public void testFindWrapPos_tabWithinWidth_returnsPosAfterTab() {
        String text = "Col1\tCol2";
        int pos = formatter.findWrapPos(text, 10, 0);
        assertEquals(5, pos);
    }

    // Tests findWrapPos when text fits within width
    @Test
    public void testFindWrapPos_textFitsWithinWidth_returnsNegativeOne() {
        String text = "short text";
        int pos = formatter.findWrapPos(text, 20, 0);
        assertEquals(-1, pos);
    }

    // Tests findWrapPos when wrapping on whitespace before width limit
    @Test
    public void testFindWrapPos_wrapOnWhitespace_returnsSpacePos() {
        String text = "The quick brown fox jumps";
        int pos = formatter.findWrapPos(text, 12, 0);
        assertEquals(9, pos);
    }

    // Tests findWrapPos when no whitespace before width limit but exists after width limit
    @Test
    public void testFindWrapPos_noWhitespaceBeforeWidth_findsWhitespaceAfterWidth() {
        String text = "Supercalifragilisticexpialidocious word";
        int pos = formatter.findWrapPos(text, 10, 0);
        assertEquals(34, pos);
    }

    // Tests rtrim with various inputs
    @Test
    public void testRtrim_variousStrings_trimsTrailingWhitespaceOnly() {
        assertNull(formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("   hello", formatter.rtrim("   hello   \t\n"));
        assertEquals("hello", formatter.rtrim("hello"));
    }

    // Tests createPadding method
    @Test
    public void testCreatePadding_zeroAndPositiveLength_returnsCorrectSpaces() {
        assertEquals("", formatter.createPadding(0));
        assertEquals("   ", formatter.createPadding(3));
    }

    // Tests convenience printHelp overloads using standard output
    @Test
    public void testPrintHelp_overloads_executesWithoutException() {
        Options options = new Options();
        options.addOption("h", "help", false, "display help");

        formatter.printHelp("app", options);
        formatter.printHelp("app", options, true);
        formatter.printHelp("app", "header", options, "footer");
        formatter.printHelp("app", "header", options, "footer", true);
        formatter.printHelp(60, "app", "header", options, "footer");
        formatter.printHelp(60, "app", "header", options, "footer", true);
        formatter.printHelp(printWriter, 60, "app", "header", options, 1, 3, "footer");
    }

    // Tests printWrapped formatting
    @Test
    public void testPrintWrapped_withPaddingAndTabs_wrapsLinesCorrectly() {
        formatter.printWrapped(printWriter, 20, 4, "This is a long sentence that should wrap across lines.");
        printWriter.flush();

        String result = stringWriter.toString();
        String[] lines = result.split(System.getProperty("line.separator"));
        assertTrue(lines.length > 1);
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].length() > 0) {
                assertTrue(lines[i].startsWith("    "));
            }
        }
    }
}