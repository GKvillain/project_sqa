package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

    // Tests getter and setter properties
    @Test
    public void testGettersAndSetters_defaultAndCustomValues_valuesUpdated() {
        assertEquals(HelpFormatter.DEFAULT_WIDTH, formatter.getWidth());
        assertEquals(HelpFormatter.DEFAULT_LEFT_PAD, formatter.getLeftPadding());
        assertEquals(HelpFormatter.DEFAULT_DESC_PAD, formatter.getDescPadding());
        assertEquals(HelpFormatter.DEFAULT_SYNTAX_PREFIX, formatter.getSyntaxPrefix());
        assertEquals(HelpFormatter.DEFAULT_OPT_PREFIX, formatter.getOptPrefix());
        assertEquals(HelpFormatter.DEFAULT_LONG_OPT_PREFIX, formatter.getLongOptPrefix());
        assertEquals(HelpFormatter.DEFAULT_LONG_OPT_SEPARATOR, formatter.getLongOptSeparator());
        assertEquals(HelpFormatter.DEFAULT_ARG_NAME, formatter.getArgName());
        assertEquals(System.getProperty("line.separator"), formatter.getNewLine());
        assertNotNull(formatter.getOptionComparator());

        formatter.setWidth(100);
        formatter.setLeftPadding(2);
        formatter.setDescPadding(4);
        formatter.setSyntaxPrefix("Syntax: ");
        formatter.setOptPrefix("+");
        formatter.setLongOptPrefix("++");
        formatter.setLongOptSeparator("=");
        formatter.setArgName("value");
        formatter.setNewLine("\n");
        formatter.setOptionComparator(null);

        assertEquals(100, formatter.getWidth());
        assertEquals(2, formatter.getLeftPadding());
        assertEquals(4, formatter.getDescPadding());
        assertEquals("Syntax: ", formatter.getSyntaxPrefix());
        assertEquals("+", formatter.getOptPrefix());
        assertEquals("++", formatter.getLongOptPrefix());
        assertEquals("=", formatter.getLongOptSeparator());
        assertEquals("value", formatter.getArgName());
        assertEquals("\n", formatter.getNewLine());
        assertNotNull(formatter.getOptionComparator());
    }

    // Tests custom comparator for ordering options
    @Test
    public void testSetOptionComparator_customComparator_ordersOptionsAccordingly() {
        Comparator customComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                Option opt1 = (Option) o1;
                Option opt2 = (Option) o2;
                return opt2.getKey().compareToIgnoreCase(opt1.getKey());
            }
        };

        formatter.setOptionComparator(customComparator);
        assertEquals(customComparator, formatter.getOptionComparator());

        Options options = new Options();
        options.addOption("a", "alpha", false, "desc a");
        options.addOption("b", "beta", false, "desc b");

        formatter.printUsage(printWriter, 80, "app", options);
        printWriter.flush();
        String out = stringWriter.toString().trim();
        assertTrue(out.indexOf("-b") < out.indexOf("-a"));
    }

    // Tests exception path for empty or null cmdLineSyntax in printHelp
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_nullCmdLineSyntax_throwsIllegalArgumentException() {
        Options options = new Options();
        formatter.printHelp(printWriter, 80, null, "header", options, 1, 3, "footer", false);
    }

    // Tests exception path for empty cmdLineSyntax string in printHelp
    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelp_emptyCmdLineSyntax_throwsIllegalArgumentException() {
        Options options = new Options();
        formatter.printHelp(printWriter, 80, "", "header", options, 1, 3, "footer", false);
    }

    // Tests printHelp with full options, header, and footer
    @Test
    public void testPrintHelp_fullParameters_printsFormattedHelp() {
        Options options = new Options();
        options.addOption("h", "help", false, "display help");
        options.addOption(new Option("f", "file", true, "input file"));

        formatter.printHelp(printWriter, 80, "myapp", "Header info", options, 2, 2, "Footer info", true);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("usage: myapp"));
        assertTrue(output.contains("Header info"));
        assertTrue(output.contains("-f,--file <file>"));
        assertTrue(output.contains("-h,--help"));
        assertTrue(output.contains("Footer info"));
    }

    // Tests printHelp overloaded methods delegating to standard output
    @Test
    public void testPrintHelp_overloads_executesWithoutException() {
        Options options = new Options();
        options.addOption("v", "version", false, "display version");

        formatter.printHelp("app", options);
        formatter.printHelp("app", options, true);
        formatter.printHelp("app", "header", options, "footer");
        formatter.printHelp("app", "header", options, "footer", true);
        formatter.printHelp(80, "app", "header", options, "footer");
        formatter.printHelp(80, "app", "header", options, "footer", true);
    }

    // Tests printHelp with autoUsage false
    @Test
    public void testPrintHelp_autoUsageFalse_printsSimpleUsage() {
        Options options = new Options();
        options.addOption("a", false, "option a");

        formatter.printHelp(printWriter, 80, "app [options]", null, options, 1, 3, null, false);
        printWriter.flush();

        String expected = "usage: app [options]" + EOL + " -a   option a" + EOL;
        assertEquals(expected, stringWriter.toString());
    }

    // Tests printUsage with simple command line syntax
    @Test
    public void testPrintUsage_simpleSyntax_printsPrefixAndSyntax() {
        formatter.printUsage(printWriter, 80, "myapp arg1 arg2");
        printWriter.flush();

        assertEquals("usage: myapp arg1 arg2" + EOL, stringWriter.toString());
    }

    // Tests printUsage with required Option and OptionGroup
    @Test
    public void testPrintUsage_withOptionGroupAndRequiredOption_formatsCorrectly() {
        Options options = new Options();

        Option requiredOpt = new Option("r", "req", false, "required opt");
        requiredOpt.setRequired(true);
        options.addOption(requiredOpt);

        Option optLongOnly = new Option(null, "long-only", true, "long only with arg");
        optLongOnly.setArgName("val");
        options.addOption(optLongOnly);

        OptionGroup group = new OptionGroup();
        group.addOption(new Option("x", "option x"));
        group.addOption(new Option("y", "option y"));
        group.setRequired(false);
        options.addOptionGroup(group);

        OptionGroup requiredGroup = new OptionGroup();
        requiredGroup.addOption(new Option("m", "mode m"));
        requiredGroup.addOption(new Option("n", "mode n"));
        requiredGroup.setRequired(true);
        options.addOptionGroup(requiredGroup);

        formatter.printUsage(printWriter, 80, "app", options);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("-r"));
        assertTrue(output.contains("[--long-only <val>]"));
        assertTrue(output.contains("[-x | -y]"));
        assertTrue(output.contains("-m | -n"));
    }

    // Tests option without short name in renderOptions
    @Test
    public void testRenderOptions_longOptOnly_padsProperly() {
        Options options = new Options();
        Option longOnly = new Option(null, "config", true, "config file path");
        longOnly.setArgName("");
        options.addOption(longOnly);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 1, 3);

        String result = sb.toString();
        assertTrue(result.contains("   --config "));
        assertTrue(result.contains("config file path"));
    }

    // Tests wrapped text rendering with long single line
    @Test
    public void testRenderWrappedText_longLine_wrapsProperly() {
        StringBuffer sb = new StringBuffer();
        String text = "This is a very long line that definitely needs to be wrapped across multiple lines of text output.";
        formatter.renderWrappedText(sb, 30, 0, text);

        String result = sb.toString();
        String[] lines = result.split(EOL);
        assertTrue(lines.length > 1);
        for (int i = 0; i < lines.length; i++) {
            assertTrue(lines[i].length() <= 30);
        }
    }

    // Tests wrapped text rendering when nextLineTabStop exceeds width
    @Test
    public void testRenderWrappedText_tabStopExceedsWidth_resetsTabStopToOne() {
        StringBuffer sb = new StringBuffer();
        String text = "This is a long description text that wraps with a very large tab stop offset.";
        formatter.renderWrappedText(sb, 20, 25, text);

        String result = sb.toString();
        String[] lines = result.split(EOL);
        assertTrue(lines.length > 1);
    }

    // Tests findWrapPos with explicit newline character
    @Test
    public void testFindWrapPos_withNewline_findsNewlinePos() {
        String text = "Line one\nLine two";
        int pos = formatter.findWrapPos(text, 20, 0);
        assertEquals(9, pos);
    }

    // Tests findWrapPos with tab character
    @Test
    public void testFindWrapPos_withTab_findsTabPos() {
        String text = "Field1\tField2";
        int pos = formatter.findWrapPos(text, 20, 0);
        assertEquals(7, pos);
    }

    // Tests findWrapPos when startPos plus width is beyond text length
    @Test
    public void testFindWrapPos_beyondLength_returnsNegativeOne() {
        String text = "Short";
        int pos = formatter.findWrapPos(text, 20, 0);
        assertEquals(-1, pos);
    }

    // Tests findWrapPos when no whitespace is available within width
    @Test
    public void testFindWrapPos_noWhitespace_chopsAtWidth() {
        String text = "Supercalifragilisticexpialidocious";
        int pos = formatter.findWrapPos(text, 10, 0);
        assertEquals(10, pos);
    }

    // Tests createPadding helper method
    @Test
    public void testCreatePadding_positiveLength_returnsSpaceString() {
        String padding = formatter.createPadding(5);
        assertEquals("     ", padding);
        assertEquals(5, padding.length());

        assertEquals("", formatter.createPadding(0));
    }

    // Tests rtrim trimming of trailing whitespace
    @Test
    public void testRtrim_variousInputs_trimsTrailingSpaces() {
        assertNull(formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("abc", formatter.rtrim("abc   "));
        assertEquals("  abc", formatter.rtrim("  abc  \t\n"));
        assertEquals("abc", formatter.rtrim("abc"));
    }

    // Tests printWrapped method
    @Test
    public void testPrintWrapped_withTabStop_rendersCorrectly() {
        formatter.printWrapped(printWriter, 40, 4, "First line of text that wraps nicely onto following indented lines.");
        printWriter.flush();

        String output = stringWriter.toString();
        String[] lines = output.split(EOL);
        assertTrue(lines.length >= 2);
        assertTrue(lines[1].startsWith("    "));
    }

    // Tests printOptions method
    @Test
    public void testPrintOptions_printsFormattedOptions() {
        Options options = new Options();
        options.addOption("o", "output", true, "output file");
        formatter.printOptions(printWriter, 80, options, 2, 4);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("-o,--output <arg>"));
        assertTrue(output.contains("output file"));
    }

    // Tests printHelp 8-parameter overload without autoUsage boolean
    @Test
    public void testPrintHelp_eightParamOverload_printsExpectedHelp() {
        Options options = new Options();
        options.addOption("d", "debug", false, "turn on debugging");

        formatter.printHelp(printWriter, 80, "myapp", "Start Header", options, 2, 3, "End Footer");
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("usage: myapp"));
        assertTrue(output.contains("Start Header"));
        assertTrue(output.contains("-d,--debug"));
        assertTrue(output.contains("End Footer"));
    }

    // Tests printHelp with null header and null footer
    @Test
    public void testPrintHelp_nullHeaderAndFooter_omitsHeaderAndFooter() {
        Options options = new Options();
        options.addOption("e", "exec", false, "execute");

        formatter.printHelp(printWriter, 80, "app", null, options, 1, 2, null, true);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("usage: app"));
        assertTrue(output.contains("-e,--exec"));
    }

    // Tests printWrapped 3-parameter overload
    @Test
    public void testPrintWrapped_threeParameters_printsWrappedText() {
        formatter.printWrapped(printWriter, 30, "Short wrapped text line");
        printWriter.flush();

        assertEquals("Short wrapped text line" + EOL, stringWriter.toString());
    }

    // Tests findWrapPos with carriage return character
    @Test
    public void testFindWrapPos_withCarriageReturn_findsCarriageReturnPos() {
        String text = "Line1\rLine2";
        int pos = formatter.findWrapPos(text, 20, 0);
        assertEquals(6, pos);
    }

    // Tests default OptionComparator sorting options alphabetically by key
    @Test
    public void testDefaultOptionComparator_sortsAlphabetically() {
        Options options = new Options();
        options.addOption("z", "zeta", false, "zeta desc");
        options.addOption("a", "alpha", false, "alpha desc");

        formatter.printUsage(printWriter, 80, "app", options);
        printWriter.flush();

        String out = stringWriter.toString();
        assertTrue(out.indexOf("-a") < out.indexOf("-z"));
    }

    // Tests renderOptions with optional argument and multiple arguments
    @Test
    public void testRenderOptions_optionalAndMultipleArgs_formatsCorrectly() {
        Options options = new Options();

        Option optOptional = new Option("o", "optional", true, "optional value");
        optOptional.setOptionalArg(true);
        options.addOption(optOptional);

        Option optMulti = new Option("m", "multi", true, "multiple values");
        optMulti.setArgs(2);
        options.addOption(optMulti);

        Option optUnlimited = new Option("u", "unlimited", true, "unlimited values");
        optUnlimited.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(optUnlimited);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 1, 2);

        String result = sb.toString();
        assertTrue(result.contains("[<optional>]") || result.contains("[optional]"));
        assertTrue(result.contains("-m,--multi"));
        assertTrue(result.contains("-u,--unlimited"));
    }

    // Tests renderOptions with null description
    @Test
    public void testRenderOptions_nullDescription_rendersOptionWithoutError() {
        Options options = new Options();
        options.addOption(new Option("n", "nodesc", false, null));

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 1, 2);

        String result = sb.toString();
        assertTrue(result.contains("-n,--nodesc"));
    }

    // Tests printUsage with long-only option inside OptionGroup
    @Test
    public void testPrintUsage_optionGroupWithLongOnlyOption_formatsCorrectly() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option(null, "foo", false, "foo option"));
        group.addOption(new Option("b", "bar", false, "bar option"));
        options.addOptionGroup(group);

        formatter.printUsage(printWriter, 80, "app", options);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("[--foo | -b]"));
    }

    // Tests renderWrappedText with text containing carriage return newline sequence
    @Test
    public void testRenderWrappedText_withCarriageReturnNewline_wrapsProperly() {
        StringBuffer sb = new StringBuffer();
        String text = "First line\r\nSecond line\r\nThird line";
        formatter.renderWrappedText(sb, 80, 0, text);

        String result = sb.toString();
        assertTrue(result.contains("First line"));
        assertTrue(result.contains("Second line"));
        assertTrue(result.contains("Third line"));
    }
}