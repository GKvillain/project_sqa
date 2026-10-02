package org.apache.commons.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class HelpFormatterTest {

    private HelpFormatter formatter;
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private final String EOL = System.getProperty("line.separator");

    @Before
    public void setUp() {
        formatter = new HelpFormatter();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    // Tests getter and setter default values and modifications
    @Test
    public void testGettersAndSetters_customValues_returnsConfiguredValues() {
        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());

        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());

        formatter.setDescPadding(8);
        assertEquals(8, formatter.getDescPadding());

        formatter.setSyntaxPrefix("Syntax: ");
        assertEquals("Syntax: ", formatter.getSyntaxPrefix());

        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());

        formatter.setOptPrefix("+");
        assertEquals("+", formatter.getOptPrefix());

        formatter.setLongOptPrefix("++");
        assertEquals("++", formatter.getLongOptPrefix());

        formatter.setLongOptSeparator("=");
        assertEquals("=", formatter.getLongOptSeparator());

        formatter.setArgName("parameter");
        assertEquals("parameter", formatter.getArgName());
    }

    // Tests comparator getter and setter, including null handling
    @Test
    public void testSetOptionComparator_nullComparator_resetsToDefaultComparator() {
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

    // Tests exception path when cmdLineSyntax is null
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_nullCmdLineSyntax_throwsIllegalArgumentException() {
        Options options = new Options();
        formatter.printHelp(printWriter, 80, null, "header", options, 1, 3, "footer");
    }

    // Tests exception path when cmdLineSyntax is empty string
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_emptyCmdLineSyntax_throwsIllegalArgumentException() {
        Options options = new Options();
        formatter.printHelp(printWriter, 80, "", "header", options, 1, 3, "footer");
    }

    // Tests printHelp with full header, footer, options and custom padding
    @Test
    public void testPrintHelp_withHeaderAndFooter_printsCompleteHelpText() {
        Options options = new Options();
        options.addOption("a", "all", false, "do not ignore entries starting with .");
        options.addOption("s", "size", true, "print size of each file");

        formatter.printHelp(printWriter, 80, "myapp", "Header banner", options, 2, 4, "Footer banner", true);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("usage: myapp [-a] [-s <size>]") || output.contains("usage: myapp [-a] [-s <arg>]"));
        assertTrue(output.contains("Header banner"));
        assertTrue(output.contains("-a,--all"));
        assertTrue(output.contains("-s,--size"));
        assertTrue(output.contains("Footer banner"));
    }

    // Tests printUsage with required and optional options
    @Test
    public void testPrintUsage_requiredAndOptionalOptions_formatsCorrectSyntax() {
        Options options = new Options();
        Option requiredOpt = new Option("r", "req", false, "required option");
        requiredOpt.setRequired(true);
        options.addOption(requiredOpt);
        options.addOption(new Option("o", "opt", false, "optional option"));

        formatter.printUsage(printWriter, 80, "testApp", options);
        printWriter.flush();

        String expected = "usage: testApp -r [-o]" + EOL;
        assertEquals(expected, stringWriter.toString());
    }

    // Tests printUsage with default argument name when option has argument
    @Test
    public void testPrintUsage_optionWithArgAndDefaultArgName_includesArgNameInUsage() {
        Options options = new Options();
        Option opt = new Option("f", "file", true, "specify target file");
        options.addOption(opt);

        formatter.printUsage(printWriter, 80, "app", options);
        printWriter.flush();

        String expected = "usage: app [-f <arg>]" + EOL;
        assertEquals(expected, stringWriter.toString());
    }

    // Tests printUsage with OptionGroup (required and non-required)
    @Test
    public void testPrintUsage_optionGroup_formatsMutuallyExclusiveOptions() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "Option A"));
        group.addOption(new Option("b", "Option B"));
        group.setRequired(false);
        options.addOptionGroup(group);

        formatter.printUsage(printWriter, 80, "groupApp", options);
        printWriter.flush();

        String expected = "usage: groupApp [-a | -b]" + EOL;
        assertEquals(expected, stringWriter.toString());
    }

    // Tests printUsage with simple command line syntax string
    @Test
    public void testPrintUsage_simpleSyntaxString_printsExpectedUsageLine() {
        formatter.printUsage(printWriter, 80, "simpleApp <input> <output>");
        printWriter.flush();

        String expected = "usage: simpleApp <input> <output>" + EOL;
        assertEquals(expected, stringWriter.toString());
    }

    // Tests printOptions with options having long options only and empty arg names
    @Test
    public void testPrintOptions_longOptOnlyAndEmptyArgName_rendersCorrectly() {
        Options options = new Options();
        Option longOnly = new Option(null, "verbose", false, "verbose output");
        Option withEmptyArg = new Option("e", "empty", true, "empty argument name");
        withEmptyArg.setArgName("");
        options.addOption(longOnly);
        options.addOption(withEmptyArg);

        formatter.printOptions(printWriter, 80, options, 1, 3);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("--verbose"));
        assertTrue(output.contains("-e,--empty"));
    }

    // Tests wrapping when text contains newlines and tabs
    @Test
    public void testFindWrapPos_newlineAndTabCharacters_wrapsAtSpecialCharacter() {
        String textWithNewline = "first line\nsecond line";
        int posNewline = formatter.findWrapPos(textWithNewline, 20, 0);
        assertEquals(11, posNewline);

        String textWithTab = "col1\tcol2";
        int posTab = formatter.findWrapPos(textWithTab, 20, 0);
        assertEquals(5, posTab);

        String shortText = "short";
        int posEnd = formatter.findWrapPos(shortText, 20, 0);
        assertEquals(-1, posEnd);
    }

    // Tests wrapping behavior when line wraps past maxWidth without whitespace before width
    @Test
    public void testFindWrapPos_noWhitespaceBeforeWidth_findsNextAvailableWhitespace() {
        String longWord = "verylongwordthatiswiderthanwidth following text";
        int pos = formatter.findWrapPos(longWord, 10, 0);
        assertEquals(32, pos);
    }

    // Tests printWrapped with nextLineTabStop equal to or exceeding width
    @Test
    public void testPrintWrapped_tabStopGreaterThanWidth_wrapsWithoutInfiniteLoop() {
        String text = "This is a long description text that needs wrapping across lines.";
        formatter.printWrapped(printWriter, 20, 25, text);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains(EOL));
    }

    // Tests rtrim helper method with null, empty, trailing whitespace, and normal string
    @Test
    public void testRtrim_variousInputs_trimsTrailingWhitespaceOnly() {
        assertNull(formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("  abc", formatter.rtrim("  abc   \t \n"));
        assertEquals("abc", formatter.rtrim("abc"));
    }

    // Tests createPadding utility method
    @Test
    public void testCreatePadding_positiveLength_returnsStringOfSpaces() {
        assertEquals("   ", formatter.createPadding(3));
        assertEquals("", formatter.createPadding(0));
    }

    // Tests convenience printHelp overloads that write to standard output
    @Test
    public void testPrintHelp_stdoutOverloads_executesWithoutException() {
        Options options = new Options();
        options.addOption("h", "help", false, "display help");

        formatter.printHelp("app", options);
        formatter.printHelp("app", options, true);
        formatter.printHelp("app", "header", options, "footer");
        formatter.printHelp("app", "header", options, "footer", true);
        formatter.printHelp(80, "app", "header", options, "footer");
        formatter.printHelp(80, "app", "header", options, "footer", true);
    }

    // Tests printHelp 8-parameter overload without autoUsage (defaults autoUsage=false)
    @Test
    public void testPrintHelp_eightArgOverload_executesWithAutoUsageFalse() {
        Options options = new Options();
        options.addOption("x", "execute", false, "execute command");

        formatter.printHelp(printWriter, 80, "app", "Header", options, 2, 4, "Footer");
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.startsWith("usage: app" + EOL));
        assertTrue(output.contains("Header"));
        assertTrue(output.contains("-x,--execute"));
        assertTrue(output.contains("Footer"));
    }

    // Tests printHelp with null header and null footer
    @Test
    public void testPrintHelp_nullHeaderAndFooter_omitsHeaderAndFooter() {
        Options options = new Options();
        options.addOption("k", false, "keep files");

        formatter.printHelp(printWriter, 80, "cleanApp", null, options, 1, 3, null, true);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("usage: cleanApp [-k]"));
        assertTrue(output.contains("-k"));
    }

    // Tests printUsage with required OptionGroup
    @Test
    public void testPrintUsage_requiredOptionGroup_enclosesInParentheses() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("x", "Option X"));
        group.addOption(new Option("y", "Option Y"));
        group.setRequired(true);
        options.addOptionGroup(group);

        formatter.printUsage(printWriter, 80, "reqGroupApp", options);
        printWriter.flush();

        String expected = "usage: reqGroupApp (-x | -y)" + EOL;
        assertEquals(expected, stringWriter.toString());
    }

    // Tests printUsage with an option that only has a long option and argument
    @Test
    public void testPrintUsage_longOptOnlyWithArg_rendersWithLongOptPrefixAndSeparator() {
        Options options = new Options();
        Option longOnlyWithArg = new Option(null, "config", true, "configuration file");
        options.addOption(longOnlyWithArg);

        formatter.printUsage(printWriter, 80, "configApp", options);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("usage: configApp [--config <arg>]"));
    }

    // Tests printWrapped 3-parameter overload
    @Test
    public void testPrintWrapped_threeArgsOverload_wrapsTextCorrectly() {
        String longText = "This is a simple text that needs to be wrapped across lines.";
        formatter.printWrapped(printWriter, 20, longText);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains(EOL));
    }

    // Tests renderOptions and renderWrappedText directly using StringBuffer
    @Test
    public void testRenderOptionsAndRenderWrappedText_stringBufferInputs_appendsFormattedText() {
        Options options = new Options();
        options.addOption("b", "beta", false, "beta feature");
        options.addOption("a", "alpha", false, "alpha feature");

        StringBuffer sbOptions = new StringBuffer();
        formatter.renderOptions(sbOptions, 80, options, 2, 4);
        String optionsStr = sbOptions.toString();
        assertTrue(optionsStr.indexOf("-a,--alpha") < optionsStr.indexOf("-b,--beta"));

        StringBuffer sbWrapped = new StringBuffer("Prefix: ");
        formatter.renderWrappedText(sbWrapped, 30, 4, "Line one that wraps into line two with indentation");
        assertTrue(sbWrapped.toString().contains(EOL));
    }

    // Tests findWrapPos with carriage return and startPos offset
    @Test
    public void testFindWrapPos_carriageReturnAndOffset_findsCorrectWrapPosition() {
        String textWithCr = "line1\rline2";
        int posCr = formatter.findWrapPos(textWithCr, 20, 0);
        assertEquals(6, posCr);

        String multiLine = "first word second word third word";
        int posOffset = formatter.findWrapPos(multiLine, 12, 11);
        assertEquals(22, posOffset);
    }
}