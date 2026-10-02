package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HelpFormatterTest {

    private HelpFormatter formatter;
    private String eol;

    @Before
    public void setUp() {
        formatter = new HelpFormatter();
        eol = formatter.getNewLine();
    }

    // Tests default property getters and setters
    @Test
    public void testGettersAndSetters_modifyProperties_valuesUpdated() {
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

        formatter.setArgName("value");
        assertEquals("value", formatter.getArgName());
    }

    // Tests setting comparator to null resets to default comparator
    @Test
    public void testSetOptionComparator_nullComparator_resetsToDefault() {
        Comparator defaultComp = formatter.getOptionComparator();
        assertNotNull(defaultComp);

        Comparator customComp = new Comparator() {
            public int compare(Object o1, Object o2) {
                return 0;
            }
        };

        formatter.setOptionComparator(customComp);
        assertSame(customComp, formatter.getOptionComparator());

        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
    }

    // Tests printHelp with null or empty command line syntax throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_nullCmdLineSyntax_throwsException() {
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, null, "header", new Options(), 1, 3, "footer");
    }

    // Tests printHelp with empty command line syntax throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_emptyCmdLineSyntax_throwsException() {
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "", "header", new Options(), 1, 3, "footer");
    }

    // Tests printHelp with header, footer, and basic options without autoUsage
    @Test
    public void testPrintHelp_withHeaderFooter_printsFormattedOutput() {
        Options options = new Options();
        options.addOption("a", "all", false, "do all things");

        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);

        formatter.printHelp(pw, 80, "myapp", "Header banner", options, 2, 4, "Footer banner", false);
        pw.flush();

        String expected = "usage: myapp" + eol
                + "Header banner" + eol
                + "  -a,--all    do all things" + eol
                + "Footer banner" + eol;

        assertEquals(expected, out.toString());
    }

    // Tests printHelp with autoUsage enabled
    @Test
    public void testPrintHelp_autoUsageTrue_printsAutoUsageStatement() {
        Options options = new Options();
        options.addOption("a", false, "option a");
        options.addOption("b", true, "option b with arg");

        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);

        formatter.printHelp(pw, 80, "myapp", null, options, 1, 3, null, true);
        pw.flush();

        String expected = "usage: myapp [-a] [-b <arg>]" + eol
                + " -a    option a" + eol
                + " -b <arg>   option b with arg" + eol;

        assertEquals(expected, out.toString());
    }

    // Tests printUsage with simple command syntax
    @Test
    public void testPrintUsage_simpleSyntax_printsExpectedPrefix() {
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);

        formatter.printUsage(pw, 80, "appname --param value");
        pw.flush();

        assertEquals("usage: appname --param value" + eol, out.toString());
    }

    // Tests printUsage with OptionGroup containing required and non-required groups
    @Test
    public void testPrintUsage_optionGroup_rendersGroupSyntax() {
        OptionGroup requiredGroup = new OptionGroup();
        requiredGroup.setRequired(true);
        requiredGroup.addOption(new Option("a", "option A"));
        requiredGroup.addOption(new Option("b", "option B"));

        OptionGroup optionalGroup = new OptionGroup();
        optionalGroup.setRequired(false);
        optionalGroup.addOption(new Option("x", "option X"));
        optionalGroup.addOption(new Option("y", "option Y"));

        Options options = new Options();
        options.addOptionGroup(requiredGroup);
        options.addOptionGroup(optionalGroup);

        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);

        formatter.printUsage(pw, 80, "groupApp", options);
        pw.flush();

        String result = out.toString().trim();
        assertTrue(result.contains("-a | -b"));
        assertTrue(result.contains("[-x | -y]"));
    }

    // Tests printOptions with options having long-only option and custom arg names
    @Test
    public void testPrintOptions_longOnlyAndCustomArgName_formatsProperly() {
        Options options = new Options();
        Option longOnly = new Option(null, "config", true, "configuration file");
        longOnly.setArgName("FILE");
        options.addOption(longOnly);

        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);

        formatter.printOptions(pw, 80, options, 1, 3);
        pw.flush();

        String expected = "    --config <FILE>   configuration file" + eol;
        assertEquals(expected, out.toString());
    }

    // Tests printOptions with option with argument but no arg name
    @Test
    public void testPrintOptions_optionWithArgNoArgName_rendersCorrectly() {
        Options options = new Options();
        Option opt = new Option("p", "port", true, "port number");
        opt.setArgName(null);
        options.addOption(opt);

        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);

        formatter.printOptions(pw, 80, options, 1, 2);
        pw.flush();

        String expected = " -p,--port    port number" + eol;
        assertEquals(expected, out.toString());
    }

    // Tests renderWrappedText with long text exceeding width (Defects4J Cli-25 regression test)
    @Test
    public void testRenderWrappedText_longOptionDescriptionWrapping_wrapsWithoutInfiniteLoop() {
        Options options = new Options();
        options.addOption("c", true, "description with a very long argument description that needs wrapping across lines");

        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);

        formatter.printHelp(pw, 20, "app", null, options, 2, 2, null, false);
        pw.flush();

        String output = out.toString();
        assertTrue("Output should contain wrapped description", output.contains("description"));
        assertTrue("Output should contain multiple lines", output.split(eol).length > 2);
    }

    // Tests renderWrappedText when nextLineTabStop is greater than or equal to width
    @Test
    public void testRenderWrappedText_tabStopGreaterOrEqualToWidth_adjustsTabStop() {
        StringBuffer sb = new StringBuffer();
        String text = "a short text that should be wrapped properly";
        formatter.renderWrappedText(sb, 10, 15, text);

        assertTrue(sb.length() > 0);
        String[] lines = sb.toString().split(eol);
        assertTrue(lines.length > 1);
    }

    // Tests renderWrappedText when text fits completely within width
    @Test
    public void testRenderWrappedText_textFitsWidth_noWrap() {
        StringBuffer sb = new StringBuffer();
        String text = "short text";
        formatter.renderWrappedText(sb, 50, 0, text);

        assertEquals("short text", sb.toString());
    }

    // Tests findWrapPos with newlines and tabs
    @Test
    public void testFindWrapPos_withNewlineAndTab_returnsCorrectPosition() {
        String textWithNewline = "first line\nsecond line";
        int posNewline = formatter.findWrapPos(textWithNewline, 20, 0);
        assertEquals(11, posNewline);

        String textWithTab = "tabbed\tcontent";
        int posTab = formatter.findWrapPos(textWithTab, 20, 0);
        assertEquals(7, posTab);

        String textNoWrap = "short";
        int posNoWrap = formatter.findWrapPos(textNoWrap, 20, 0);
        assertEquals(-1, posNoWrap);
    }

    // Tests findWrapPos when word is longer than width
    @Test
    public void testFindWrapPos_wordLongerThanWidth_returnsPositionAfterWidth() {
        String longWord = "verylongwordwithoutspaces next";
        int pos = formatter.findWrapPos(longWord, 10, 0);
        assertEquals(25, pos);
    }

    // Tests rtrim utility method with spaces, empty, and null strings
    @Test
    public void testRtrim_variousStrings_trimsTrailingWhitespaceOnly() {
        assertEquals("", formatter.rtrim("   "));
        assertEquals("  abc", formatter.rtrim("  abc  \t\n"));
        assertEquals("text", formatter.rtrim("text"));
        assertEquals("", formatter.rtrim(""));
        assertEquals(null, formatter.rtrim(null));
    }

    // Tests createPadding method
    @Test
    public void testCreatePadding_positiveLength_returnsSpaces() {
        assertEquals("", formatter.createPadding(0));
        assertEquals("   ", formatter.createPadding(3));
    }

    // Tests printHelp convenience overloads without throwing exceptions
    @Test
    public void testPrintHelp_convenienceOverloads_executeSuccessfully() {
        Options options = new Options();
        options.addOption("h", "help", false, "display help");

        // Test overloads that print to System.out or use defaults
        formatter.printHelp("app", options);
        formatter.printHelp("app", options, true);
        formatter.printHelp("app", "header", options, "footer");
        formatter.printHelp("app", "header", options, "footer", true);
        formatter.printHelp(60, "app", "header", options, "footer");
        formatter.printHelp(60, "app", "header", options, "footer", true);
    }

    // Tests 8-parameter printHelp overload defaulting autoUsage to false
    @Test
    public void testPrintHelp_eightParamOverload_printsExpected() {
        Options options = new Options();
        options.addOption("a", "all", false, "do all");

        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);

        formatter.printHelp(pw, 80, "myapp", "Header", options, 2, 4, "Footer");
        pw.flush();

        String expected = "usage: myapp" + eol
                + "Header" + eol
                + "  -a,--all    do all" + eol
                + "Footer" + eol;

        assertEquals(expected, out.toString());
    }

    // Tests printUsage with optional arguments, unlimited arguments, and required options
    @Test
    public void testPrintUsage_withOptionalArgAndMultipleArgs_rendersCorrectSyntax() {
        Options options = new Options();
        Option optOptional = new Option("f", "file", true, "input file");
        optOptional.setOptionalArg(true);
        options.addOption(optOptional);

        Option optMultiple = new Option("m", "many", true, "many args");
        optMultiple.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(optMultiple);

        Option reqOpt = new Option("r", "req", false, "required opt");
        reqOpt.setRequired(true);
        options.addOption(reqOpt);

        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);

        formatter.printUsage(pw, 80, "app", options);
        pw.flush();

        String res = out.toString();
        assertTrue(res.contains("-r"));
        assertTrue(res.contains("[-f [<file>]]") || res.contains("[-f [<arg>]]"));
        assertTrue(res.contains("[-m <many>...]") || res.contains("[-m <arg>...]"));
    }

    // Tests findWrapPos with carriage return (\r\n) and single long token without any whitespace
    @Test
    public void testFindWrapPos_withCarriageReturnAndNoSpaces() {
        String textWithCR = "first\r\nsecond";
        int posCR = formatter.findWrapPos(textWithCR, 20, 0);
        assertEquals(6, posCR);

        String singleLongWord = "abcdefghijklmnopqrstuvwxyz";
        int posNoSpace = formatter.findWrapPos(singleLongWord, 10, 0);
        assertEquals(-1, posNoSpace);
    }

    // Tests renderWrappedText with embedded newlines across multiple lines
    @Test
    public void testRenderWrappedText_withEmbeddedNewlines() {
        StringBuffer sb = new StringBuffer();
        String text = "Line 1\nLine 2\r\nLine 3";
        formatter.renderWrappedText(sb, 80, 0, text);

        String expected = "Line 1" + eol + "Line 2" + eol + "Line 3";
        assertEquals(expected, sb.toString());
    }

    // Tests printOptions with options having null or empty descriptions
    @Test
    public void testPrintOptions_nullAndEmptyDescription() {
        Options options = new Options();
        options.addOption("n", "no-desc", false, null);
        options.addOption("e", "empty-desc", false, "");

        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);

        formatter.printOptions(pw, 80, options, 2, 2);
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("-n,--no-desc"));
        assertTrue(result.contains("-e,--empty-desc"));
    }

    // Tests printHelp with OptionGroup rendering options correctly
    @Test
    public void testPrintHelp_withOptionGroup_printsHelpCorrectly() {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("s", "start", false, "start service"));
        group.addOption(new Option("t", "stop", false, "stop service"));

        Options options = new Options();
        options.addOptionGroup(group);

        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);

        formatter.printHelp(pw, 80, "service", null, options, 1, 3, null, true);
        pw.flush();

        String result = out.toString();
        assertTrue(result.contains("usage: service [-s | -t]"));
        assertTrue(result.contains("-s,--start"));
        assertTrue(result.contains("-t,--stop"));
    }

    // Tests custom comparator for ordering options in printOptions
    @Test
    public void testPrintHelp_customComparator_ordersOptions() {
        Options options = new Options();
        options.addOption("z", "last", false, "desc z");
        options.addOption("a", "first", false, "desc a");

        formatter.setOptionComparator(new Comparator() {
            public int compare(Object o1, Object o2) {
                Option opt1 = (Option) o1;
                Option opt2 = (Option) o2;
                return opt2.getKey().compareToIgnoreCase(opt1.getKey());
            }
        });

        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);

        formatter.printOptions(pw, 80, options, 1, 2);
        pw.flush();

        String res = out.toString();
        int posZ = res.indexOf("-z");
        int posA = res.indexOf("-a");
        assertTrue(posZ < posA);
    }
}