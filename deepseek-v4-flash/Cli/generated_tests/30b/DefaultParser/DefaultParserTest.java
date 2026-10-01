package org.apache.commons.cli;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

public class DefaultParserTest {
    private Options options;
    private DefaultParser parser;
    private CommandLine cmd;

    @Before
    public void setUp() throws Exception {
        parser = new DefaultParser();
        options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        options.addOption("b", "beta", true, "beta option with arg");
        options.addOption("c", false, "c option");
        Option dOpt = OptionBuilder.withLongOpt("d").hasArg().create('d');
        options.addOption(dOpt);
        Option eOpt = OptionBuilder.withLongOpt("e").hasArgs().withValueSeparator(',').create('e');
        options.addOption(eOpt);
    }

    @After
    public void tearDown() throws Exception {
        cmd = null;
        parser = null;
        options = null;
    }

    // Tests normal case: parse short option without argument
    @Test
    public void testParse_shortOptionWithoutArg_returnsCommandLine() throws Exception {
        cmd = parser.parse(options, new String[]{"-a"});
        assertTrue("Option 'a' should be present", cmd.hasOption("a"));
        assertEquals("Should have no args", 0, cmd.getArgs().length);
    }

    // Tests normal case: parse short option with argument
    @Test
    public void testParse_shortOptionWithArg_returnsCommandLine() throws Exception {
        cmd = parser.parse(options, new String[]{"-b", "value"});
        assertTrue("Option 'b' should be present", cmd.hasOption("b"));
        assertEquals("Option 'b' value should be 'value'", "value", cmd.getOptionValue("b"));
    }

    // Tests normal case: parse long option without argument
    @Test
    public void testParse_longOptionWithoutArg_returnsCommandLine() throws Exception {
        cmd = parser.parse(options, new String[]{"--alpha"});
        assertTrue("Option 'alpha' should be present", cmd.hasOption("alpha"));
    }

    // Tests normal case: parse long option with argument using equals
    @Test
    public void testParse_longOptionWithArgUsingEquals_returnsCommandLine() throws Exception {
        cmd = parser.parse(options, new String[]{"--beta=value"});
        assertTrue("Option 'beta' should be present", cmd.hasOption("beta"));
        assertEquals("Option 'beta' value should be 'value'", "value", cmd.getOptionValue("beta"));
    }

    // Tests boundary case: parse empty arguments
    @Test
    public void testParse_emptyArguments_returnsEmptyCommandLine() throws Exception {
        cmd = parser.parse(options, new String[]{});
        assertFalse("No option should be present", cmd.hasOption("a"));
        assertEquals("Should have no args", 0, cmd.getArgs().length);
    }

    // Tests boundary case: parse null arguments
    @Test
    public void testParse_nullArguments_returnsEmptyCommandLine() throws Exception {
        cmd = parser.parse(options, null);
        assertFalse("No option should be present", cmd.hasOption("a"));
        assertEquals("Should have no args", 0, cmd.getArgs().length);
    }

    // Tests boundary case: parse single dash token
    @Test
    public void testParse_singleDashToken_addedAsArg() throws Exception {
        cmd = parser.parse(options, new String[]{"-"});
        assertFalse("No option should be present", cmd.hasOption("a"));
        assertEquals("Should have one arg", 1, cmd.getArgs().length);
        assertEquals("Arg should be '-'", "-", cmd.getArgs()[0]);
    }

    // Tests edge case: parse unrecognized option throws exception
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unrecognizedShortOption_throwsException() throws Exception {
        parser.parse(options, new String[]{"-z"});
    }

    // Tests edge case: parse token starting with double dash but not an option
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unrecognizedLongOption_throwsException() throws Exception {
        parser.parse(options, new String[]{"--unknown"});
    }

    // Tests exception path: option requires argument but not provided
    @Test(expected = MissingArgumentException.class)
    public void testParse_missingRequiredArgument_throwsException() throws Exception {
        parser.parse(options, new String[]{"-b"});
    }

    // Tests branch: stopAtNonOption stops parsing for unknown option
    @Test
    public void testParse_stopAtNonOption_unknownOptionAddedAsArg() throws Exception {
        cmd = parser.parse(options, new String[]{"-a", "-z", "-c"}, true);
        assertTrue("Option 'a' should be present", cmd.hasOption("a"));
        assertFalse("Option 'c' should not be present", cmd.hasOption("c"));
        assertEquals("Should have one arg", 1, cmd.getArgs().length);
        assertEquals("Arg should be '-z'", "-z", cmd.getArgs()[0]);
    }

    // Tests branch: double dash skipParsing flag
    @Test
    public void testParse_doubleDash_remainingTokensAddedAsArgs() throws Exception {
        cmd = parser.parse(options, new String[]{"-a", "--", "-b", "value"});
        assertTrue("Option 'a' should be present", cmd.hasOption("a"));
        assertFalse("Option 'b' should not be present", cmd.hasOption("b"));
        assertEquals("Should have two args", 2, cmd.getArgs().length);
        assertEquals("First arg should be '-b'", "-b", cmd.getArgs()[0]);
        assertEquals("Second arg should be 'value'", "value", cmd.getArgs()[1]);
    }

    // Tests branch: concatenated short options without argument
    @Test
    public void testParse_concatenatedShortOptions_presentsOptions() throws Exception {
        options.addOption("x", false, "x option");
        options.addOption("y", false, "y option");
        cmd = parser.parse(options, new String[]{"-xy"});
        assertTrue("Option 'x' should be present", cmd.hasOption("x"));
        assertTrue("Option 'y' should be present", cmd.hasOption("y"));
    }

    // Tests branch: long option with ambiguous match
    @Test(expected = AmbiguousOptionException.class)
    public void testParse_ambiguousLongOption_throwsException() throws Exception {
        options.addOption("foo", false, "foo option");
        options.addOption("foobar", false, "foobar option");
        parser.parse(options, new String[]{"--foo"});
    }

    // Tests branch: option with multiple values
    @Test
    public void testParse_optionWithMultipleValues_hasAllValues() throws Exception {
        cmd = parser.parse(options, new String[]{"-e", "x,y,z"});
        assertTrue("Option 'e' should be present", cmd.hasOption("e"));
        String[] values = cmd.getOptionValues("e");
        assertEquals("Should have 3 values", 3, values.length);
        assertEquals("First value should be 'x'", "x", values[0]);
        assertEquals("Second value should be 'y'", "y", values[1]);
        assertEquals("Third value should be 'z'", "z", values[2]);
    }

    // Tests branch: negative number argument
    @Test
    public void testParse_negativeNumber_argumentParsedCorrectly() throws Exception {
        cmd = parser.parse(options, new String[]{"-b", "-1"});
        assertTrue("Option 'b' should be present", cmd.hasOption("b"));
        assertEquals("Option 'b' value should be '-1'", "-1", cmd.getOptionValue("b"));
    }

    // Tests branch: option group selection
    @Test
    public void testParse_optionGroup_selectedOptionIsSet() throws Exception {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("1", true, "first option");
        Option opt2 = new Option("2", true, "second option");
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);
        cmd = parser.parse(options, new String[]{"-1", "value"});
        assertTrue("Option '1' should be present", cmd.hasOption("1"));
        assertFalse("Option '2' should not be present", cmd.hasOption("2"));
    }

    // Tests branch: required option not present
    @Test(expected = MissingOptionException.class)
    public void testParse_requiredOptionMissing_throwsException() throws Exception {
        Option reqOpt = new Option("r", "required", true, "required option");
        reqOpt.setRequired(true);
        options.addOption(reqOpt);
        parser.parse(options, new String[]{"-a"});
    }

    // Tests branch: properties handleOption with value
    @Test
    public void testParse_propertiesOptionWithValue_addsOption() throws Exception {
        Properties props = new Properties();
        props.setProperty("alpha", "true");
        cmd = parser.parse(options, new String[]{}, props, false);
        assertTrue("Option 'alpha' should be present", cmd.hasOption("alpha"));
    }

    // Tests branch: properties handleOption with no value for boolean option
    @Test
    public void testParse_propertiesOptionFalseValue_doesNotAddOption() throws Exception {
        Properties props = new Properties();
        props.setProperty("alpha", "false");
        cmd = parser.parse(options, new String[]{}, props, false);
        assertFalse("Option 'alpha' should not be present", cmd.hasOption("alpha"));
    }

    // Tests branch: long option prefix matching (-Xmx512m style)
    @Test
    public void testParse_longPrefixOption_prefixParsedCorrectly() throws Exception {
        options.addOption(OptionBuilder.withLongOpt("Xmx").hasArg().create());
        cmd = parser.parse(options, new String[]{"-Xmx512m"});
        assertTrue("Option 'Xmx' should be present", cmd.hasOption("Xmx"));
        assertEquals("Option 'Xmx' value should be '512m'", "512m", cmd.getOptionValue("Xmx"));
    }
}