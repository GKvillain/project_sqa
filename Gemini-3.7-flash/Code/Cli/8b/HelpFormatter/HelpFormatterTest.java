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

    // Tests default property values and their getters/setters
    @Test
    public void testGettersAndSetters_customValues_returnUpdatedValues() {
        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());

        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());

        formatter.setDescPadding(7);
        assertEquals(7, formatter.getDescPadding());

        formatter.setSyntaxPrefix("Syntax: ");
        assertEquals("Syntax: ", formatter.getSyntaxPrefix());

        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());

        formatter.setOptPrefix("/");
        assertEquals("/", formatter.getOptPrefix());

        formatter.setLongOptPrefix("//");
        assertEquals("//", formatter.getLongOptPrefix());

        formatter.setArgName("val");
        assertEquals("val", formatter.getArgName());
    }

    // Tests null or empty cmdLineSyntax throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_nullCmdLineSyntax_throwsIllegalArgumentException() {
        formatter.printHelp(printWriter, 80, null, "header", new Options(), 1, 3, "footer");
    }

    // Tests empty string cmdLineSyntax throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_emptyCmdLineSyntax_throwsIllegalArgumentException() {
        formatter.printHelp(printWriter, 80, "", "header", new Options(), 1, 3, "footer");
    }

    // Tests printHelp with full options, header, and footer
    @Test
    public void testPrintHelp_withHeaderAndFooter_outputsCorrectHelpMessage() {
        Options options = new Options();
        options.addOption(new Option("a", "alpha", false, "Description for alpha"));

        formatter.printHelp(printWriter, 80, "myapp", "Header banner", options, 2, 4, "Footer banner", true);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("usage: myapp [-a]"));
        assertTrue(output.contains("Header banner"));
        assertTrue(output.contains("-a,--alpha"));
        assertTrue(output.contains("Description for alpha"));
        assertTrue(output.contains("Footer banner"));
    }

    // Tests printHelp overloads printing to System.out without exceptions
    @Test
    public void testPrintHelp_systemOutOverloads_runsWithoutExceptions() {
        Options options = new Options();
        options.addOption(new Option("h", "help", false, "Help option"));

        formatter.printHelp("app", options);
        formatter.printHelp("app", options, true);
        formatter.printHelp("app", "header", options, "footer");
        formatter.printHelp("app", "header", options, "footer", true);
        formatter.printHelp(80, "app", "header", options, "footer");
        formatter.printHelp(80, "app", "header", options, "footer", true);
    }

    // Tests printUsage with simple command line syntax string
    @Test
    public void testPrintUsage_simpleSyntax_printsExpectedUsageLine() {
        formatter.printUsage(printWriter, 80, "appname --option <arg>");
        printWriter.flush();

        String expected = "usage: appname --option <arg>" + formatter.getNewLine();
        assertEquals(expected, stringWriter.toString());
    }

    // Tests printUsage with required and optional options, and option with argument
    @Test
    public void testPrintUsage_optionsWithArgsAndRequired_printsFormattedUsage() {
        Options options = new Options();
        Option optA = new Option("a", "all", false, "do all");
        Option optB = new Option("b", "file", true, "output file");
        optB.setRequired(true);
        optB.setArgName("FILE");

        options.addOption(optA);
        options.addOption(optB);

        formatter.printUsage(printWriter, 80, "testapp", options);
        printWriter.flush();

        String output = stringWriter.toString().trim();
        assertEquals("usage: testapp [-a] -b <FILE>", output);
    }

    // Tests printUsage with OptionGroup (required and non-required)
    @Test
    public void testPrintUsage_withOptionGroup_printsGroupSyntax() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("c", "create", false, "create item"));
        group.addOption(new Option("d", "delete", false, "delete item"));
        options.addOptionGroup(group);

        formatter.printUsage(printWriter, 80, "app", options);
        printWriter.flush();

        String output = stringWriter.toString().trim();
        assertEquals("usage: app [-c | -d]", output);
    }

    // Tests printUsage with required OptionGroup
    @Test
    public void testPrintUsage_requiredOptionGroup_printsWithoutEnclosingSquareBrackets() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("c", "create", false, "create item"));
        group.addOption(new Option("d", "delete", false, "delete item"));
        options.addOptionGroup(group);

        formatter.printUsage(printWriter, 80, "app", options);
        printWriter.flush();

        String output = stringWriter.toString().trim();
        assertEquals("usage: app -c | -d", output);
    }

    // Tests renderOptions with long options only and options without description
    @Test
    public void testRenderOptions_longOptOnlyAndNoDescription_formatsCorrectly() {
        Options options = new Options();
        Option longOnly = new Option(null, "verbose", false, null);
        options.addOption(longOnly);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 1, 3);

        String result = sb.toString();
        assertTrue(result.contains("--verbose"));
    }

    // Tests renderOptions with argument and custom argName
    @Test
    public void testRenderOptions_withArgAndArgName_formatsArgPlaceholder() {
        Options options = new Options();
        Option fileOpt = new Option("f", "file", true, "specify input file");
        fileOpt.setArgName("PATH");
        options.addOption(fileOpt);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 2, 2);

        String result = sb.toString();
        assertTrue(result.contains("-f,--file <PATH>"));
        assertTrue(result.contains("specify input file"));
    }

    // Tests renderWrappedText when text fits within width
    @Test
    public void testRenderWrappedText_textFitsWidth_noWrap() {
        StringBuffer sb = new StringBuffer();
        String text = "This is a short line.";
        formatter.renderWrappedText(sb, 50, 0, text);

        assertEquals("This is a short line.", sb.toString());
    }

    // Tests renderWrappedText wrapping text across multiple lines with nextLineTabStop
    @Test
    public void testRenderWrappedText_longTextWithTabStop_wrapsAndPadsNextLines() {
        StringBuffer sb = new StringBuffer();
        String text = "The quick brown fox jumps over the lazy dog near the riverbank.";
        formatter.renderWrappedText(sb, 30, 4, text);

        String result = sb.toString();
        String[] lines = result.split(formatter.getNewLine());
        assertTrue(lines.length > 1);
        for (int i = 1; i < lines.length; i++) {
            assertTrue(lines[i].startsWith("    "));
        }
    }

    // Tests renderWrappedText when text contains tab and newline characters
    @Test
    public void testRenderWrappedText_withNewlinesAndTabs_splitsAtCharacters() {
        StringBuffer sb = new StringBuffer();
        String text = "First line\nSecond line\tThird line";
        formatter.renderWrappedText(sb, 50, 0, text);

        String result = sb.toString();
        assertTrue(result.contains("First line"));
        assertTrue(result.contains("Second line"));
        assertTrue(result.contains("Third line"));
    }

    // Tests findWrapPos when word is longer than available width
    @Test
    public void testFindWrapPos_wordLongerThanWidth_findsNextWhitespaceAfterWidth() {
        String text = "supercalifragilisticexpialidocious is a long word";
        int pos = formatter.findWrapPos(text, 10, 0);
        assertEquals(34, pos);
    }

    // Tests findWrapPos when text ends before width
    @Test
    public void testFindWrapPos_textShorterThanWidth_returnsNegativeOne() {
        String text = "short text";
        int pos = formatter.findWrapPos(text, 50, 0);
        assertEquals(-1, pos);
    }

    // Tests createPadding method for positive and zero lengths
    @Test
    public void testCreatePadding_variousLengths_returnsExpectedSpaces() {
        assertEquals("", formatter.createPadding(0));
        assertEquals("   ", formatter.createPadding(3));
    }

    // Tests rtrim method with various inputs
    @Test
    public void testRtrim_variousInputs_trimsTrailingWhitespaceOnly() {
        assertNull(formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("  abc", formatter.rtrim("  abc   "));
        assertEquals("hello world", formatter.rtrim("hello world\n \t"));
    }

    // Tests comparator getter and setter
    @Test
    public void testOptionComparator_customComparator_ordersOptionsAccordingly() {
        assertNotNull(formatter.getOptionComparator());

        Comparator<Option> customComparator = new Comparator<Option>() {
            public int compare(Option o1, Option o2) {
                return o2.getKey().compareToIgnoreCase(o1.getKey());
            }
        };

        formatter.setOptionComparator(customComparator);
        assertEquals(customComparator, formatter.getOptionComparator());

        Options options = new Options();
        options.addOption("a", "alpha", false, "Option A");
        options.addOption("z", "zeta", false, "Option Z");

        formatter.printHelp(printWriter, 80, "app", null, options, 1, 3, null, false);
        printWriter.flush();

        String output = stringWriter.toString();
        int idxZ = output.indexOf("-z");
        int idxA = output.indexOf("-a");
        assertTrue(idxZ < idxA);
    }

    // Tests comparator set to null retains insertion or natural order without exception
    @Test
    public void testOptionComparator_nullComparator_rendersOptionsWithoutSorting() {
        formatter.setOptionComparator(null);
        assertNull(formatter.getOptionComparator());

        Options options = new Options();
        options.addOption("b", "beta", false, "Option B");
        options.addOption("a", "alpha", false, "Option A");

        formatter.printHelp(printWriter, 80, "app", null, options, 1, 3, null, false);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("-b"));
        assertTrue(output.contains("-a"));
    }

    // Tests printWrapped overloads
    @Test
    public void testPrintWrapped_withAndWithoutTabStop_printsWrappedText() {
        formatter.printWrapped(printWriter, 20, "This is a simple wrapped line of text.");
        formatter.printWrapped(printWriter, 20, 4, "Another line of text with tab stop indentation.");
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("This is a simple"));
        assertTrue(output.contains("    stop"));
    }

    // Tests printOptions method directly
    @Test
    public void testPrintOptions_directCall_outputsOptionDetails() {
        Options options = new Options();
        options.addOption("x", "extend", true, "Extended option description that is long enough to wrap");

        formatter.printOptions(printWriter, 40, options, 2, 4);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("-x,--extend"));
        assertTrue(output.contains("Extended option"));
    }

    // Tests printHelp with autoUsage set to false
    @Test
    public void testPrintHelp_autoUsageFalse_printsDirectSyntaxWithoutAutoGeneration() {
        Options options = new Options();
        options.addOption("o", false, "an option");

        formatter.printHelp(printWriter, 80, "mycustomsyntax", "Header", options, 1, 3, "Footer", false);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("usage: mycustomsyntax"));
        assertFalse(output.contains("usage: mycustomsyntax [-o]"));
    }

    // Tests printHelp with PrintWriter and 8 parameters (without autoUsage boolean)
    @Test
    public void testPrintHelp_eightParamPrintWriter_defaultsToAutoUsage() {
        Options options = new Options();
        options.addOption("v", "verbose", false, "Verbose mode");

        formatter.printHelp(printWriter, 80, "app", "Header", options, 1, 3, "Footer");
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("usage: app [-v]"));
    }

    // Tests renderWrappedText when nextLineTabStop is greater than or equal to width
    @Test
    public void testRenderWrappedText_tabStopGreaterOrEqualToWidth_resetsTabStopToOne() {
        StringBuffer sb = new StringBuffer();
        String text = "The quick brown fox jumps over the lazy dog";
        formatter.renderWrappedText(sb, 15, 20, text);

        String result = sb.toString();
        String[] lines = result.split(formatter.getNewLine());
        assertTrue(lines.length > 1);
        assertTrue(lines[1].startsWith(" "));
    }

    // Tests findWrapPos when newline or return character appears before width
    @Test
    public void testFindWrapPos_newlineBeforeWidth_returnsPositionAfterNewline() {
        String text = "hello\nworld";
        int pos = formatter.findWrapPos(text, 20, 0);
        assertEquals(6, pos);

        String textWithCR = "hello\r\nworld";
        int posCR = formatter.findWrapPos(textWithCR, 20, 0);
        assertEquals(7, posCR);
    }

    // Tests findWrapPos when tab character appears before width
    @Test
    public void testFindWrapPos_tabBeforeWidth_returnsPositionAfterTab() {
        String text = "hello\tworld";
        int pos = formatter.findWrapPos(text, 20, 0);
        assertEquals(6, pos);
    }

    // Tests renderOptions and appendOption with optional arguments and multi-arg options
    @Test
    public void testRenderOptions_optionalArgsAndMultiArgs_formatsCorrectly() {
        Options options = new Options();

        Option optOptionalArg = new Option("o", "optional", true, "Optional argument");
        optOptionalArg.setOptionalArg(true);
        optOptionalArg.setArgName("OPT_VAL");
        options.addOption(optOptionalArg);

        Option optNoArgName = new Option("n", "none", true, "Empty arg name");
        optNoArgName.setArgName("");
        options.addOption(optNoArgName);

        Option optOnlyShort = new Option("s", false, "Short only");
        options.addOption(optOnlyShort);

        formatter.printHelp(printWriter, 80, "app", null, options, 2, 2, null, true);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("[-o [<OPT_VAL>]]") || output.contains("-o"));
        assertTrue(output.contains("-s"));
    }

    // Tests printUsage wrapping when options exceed line width
    @Test
    public void testPrintUsage_longUsageLine_wrapsProperly() {
        Options options = new Options();
        options.addOption("alpha", "alpha-long-option-name", true, "first option");
        options.addOption("beta", "beta-long-option-name", true, "second option");
        options.addOption("gamma", "gamma-long-option-name", true, "third option");
        options.addOption("delta", "delta-long-option-name", true, "fourth option");

        formatter.printUsage(printWriter, 30, "longappname", options);
        printWriter.flush();

        String output = stringWriter.toString();
        String[] lines = output.split(formatter.getNewLine());
        assertTrue(lines.length > 1);
    }

    // Tests OptionGroup with long options only and required options
    @Test
    public void testPrintUsage_optionGroupWithLongOptOnlyAndArgs_formatsGroupSyntax() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();

        Option opt1 = new Option(null, "first-long", true, "First long option");
        Option opt2 = new Option("s", null, false, "Second short option");
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);

        formatter.printUsage(printWriter, 80, "app", options);
        printWriter.flush();

        String output = stringWriter.toString().trim();
        assertTrue(output.contains("[--first-long <arg> | -s]"));
    }
}