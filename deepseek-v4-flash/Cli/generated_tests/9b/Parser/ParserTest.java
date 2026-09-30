package org.apache.commons.cli;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.Properties;

public class ParserTest {

    private Parser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new Parser() {
            @Override
            protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
                return arguments;
            }
        };
        options = new Options();
    }

    @Test
    public void testParse_SingleShortOption_ReturnsCommandLine() throws ParseException {
        options.addOption(new Option("a", false, "desc"));
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue("Option 'a' should be present", cmd.hasOption("a"));
        assertEquals("CommandLine should have no args", 0, cmd.getArgs().length);
    }

    @Test
    public void testParse_OptionWithArgument_ReturnsCommandLine() throws ParseException {
        options.addOption(new Option("b", true, "desc"));
        CommandLine cmd = parser.parse(options, new String[]{"-b", "value"});
        assertTrue("Option 'b' should be present", cmd.hasOption("b"));
        assertEquals("Option 'b' value should be 'value'", "value", cmd.getOptionValue("b"));
    }

    @Test
    public void testParse_LongOption_ReturnsCommandLine() throws ParseException {
        options.addOption(new Option("l", "long", false, "desc"));
        CommandLine cmd = parser.parse(options, new String[]{"--long"});
        assertTrue("Option 'long' should be present", cmd.hasOption("long"));
    }

    @Test
    public void testParse_NullArguments_ReturnsEmptyCommandLine() throws ParseException {
        CommandLine cmd = parser.parse(options, null);
        assertEquals(0, cmd.getArgs().length);
    }

    @Test
    public void testParse_EmptyArguments_ReturnsEmptyCommandLine() throws ParseException {
        CommandLine cmd = parser.parse(options, new String[0]);
        assertEquals(0, cmd.getArgs().length);
    }

    @Test
    public void testParse_StopAtNonOption_AddsNonOptionAsArgs() throws ParseException {
        options.addOption(new Option("a", false, "desc"));
        options.addOption(new Option("b", false, "desc"));
        CommandLine cmd = parser.parse(options, new String[]{"-a", "foo", "-b"}, true);
        assertTrue("Option 'a' should be present", cmd.hasOption("a"));
        assertFalse("Option 'b' should not be present", cmd.hasOption("b"));
        String[] args = cmd.getArgs();
        assertEquals("Should have two args", 2, args.length);
        assertEquals("First arg should be 'foo'", "foo", args[0]);
        assertEquals("Second arg should be '-b'", "-b", args[1]);
    }

    @Test
    public void testParse_DoubleDash_StopsParsingAndAddsArgs() throws ParseException {
        options.addOption(new Option("a", false, "desc"));
        CommandLine cmd = parser.parse(options, new String[]{"--", "-a", "foo"}, false);
        assertFalse("Option 'a' should not be present", cmd.hasOption("a"));
        String[] args = cmd.getArgs();
        assertEquals("Should have two args", 2, args.length);
        assertEquals("-a", args[0]);
        assertEquals("foo", args[1]);
    }

    @Test
    public void testParse_SingleDash_WithStopFalse_AddsArg() throws ParseException {
        CommandLine cmd = parser.parse(options, new String[]{"-", "foo"}, false);
        String[] args = cmd.getArgs();
        assertEquals("Should have two args", 2, args.length);
        assertEquals("-", args[0]);
        assertEquals("foo", args[1]);
    }

    @Test
    public void testParse_SingleDash_WithStopTrue_EatsRest() throws ParseException {
        CommandLine cmd = parser.parse(options, new String[]{"-", "foo", "-a"}, true);
        String[] args = cmd.getArgs();
        assertEquals("Should have three args", 3, args.length);
        assertEquals("-", args[0]);
        assertEquals("foo", args[1]);
        assertEquals("-a", args[2]);
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_UnrecognizedOption_ThrowsUnrecognizedOptionException() throws ParseException {
        parser.parse(options, new String[]{"-x"});
    }

    @Test(expected = MissingOptionException.class)
    public void testParse_MissingRequiredOption_ThrowsMissingOptionException() throws ParseException {
        Option optReq = new Option("r", false, "desc");
        optReq.setRequired(true);
        options.addOption(optReq);
        parser.parse(options, new String[]{});
    }

    @Test(expected = MissingArgumentException.class)
    public void testParse_OptionWithMissingArg_ThrowsMissingArgumentException() throws ParseException {
        options.addOption(new Option("b", true, "desc"));
        parser.parse(options, new String[]{"-b"});
    }

    @Test(expected = MissingArgumentException.class)
    public void testParse_OptionWithArgFollowedByOption_ThrowsMissingArgumentException() throws ParseException {
        options.addOption(new Option("a", false, "desc"));
        options.addOption(new Option("b", true, "desc"));
        parser.parse(options, new String[]{"-b", "-a"});
    }

    @Test
    public void testParse_OptionGroupRequired_RemovesGroupFromRequired() throws ParseException {
        Option optA = new Option("a", false, "desc");
        Option optB = new Option("b", false, "desc");
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(optA);
        group.addOption(optB);
        options.addOptionGroup(group);
        options.addOption(optA);
        options.addOption(optB);
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue("Option 'a' should be present", cmd.hasOption("a"));
        assertTrue("Required options list should be empty", parser.getRequiredOptions().isEmpty());
    }

    @Test
    public void testParse_RequiredOptionProvided_NoException() throws ParseException {
        Option optReq = new Option("r", false, "desc");
        optReq.setRequired(true);
        options.addOption(optReq);
        CommandLine cmd = parser.parse(options, new String[]{"-r"});
        assertTrue("Option 'r' should be present", cmd.hasOption("r"));
        assertTrue("Required options list should be empty", parser.getRequiredOptions().isEmpty());
    }

    @Test
    public void testParse_PropertiesWithYes_AddsOption() throws ParseException {
        options.addOption(new Option("a", false, "desc"));
        Properties props = new Properties();
        props.setProperty("a", "yes");
        CommandLine cmd = parser.parse(options, new String[]{}, props, false);
        assertTrue("Option 'a' should be present", cmd.hasOption("a"));
    }

    @Test
    public void testParse_PropertiesWithTrue_AddsOption() throws ParseException {
        options.addOption(new Option("a", false, "desc"));
        Properties props = new Properties();
        props.setProperty("a", "TRUE");
        CommandLine cmd = parser.parse(options, new String[]{}, props, false);
        assertTrue("Option 'a' should be present", cmd.hasOption("a"));
    }

    @Test
    public void testParse_PropertiesWithNo_DoesNotAddOption() throws ParseException {
        options.addOption(new Option("a", false, "desc"));
        Properties props = new Properties();
        props.setProperty("a", "no");
        CommandLine cmd = parser.parse(options, new String[]{}, props, false);
        assertFalse("Option 'a' should not be present", cmd.hasOption("a"));
    }

    @Test
    public void testParse_PropertiesWithArg_SetsValue() throws ParseException {
        options.addOption(new Option("b", true, "desc"));
        Properties props = new Properties();
        props.setProperty("b", "propval");
        CommandLine cmd = parser.parse(options, new String[]{}, props, false);
        assertTrue("Option 'b' should be present", cmd.hasOption("b"));
        assertEquals("propval", cmd.getOptionValue("b"));
    }

    @Test
    public void testParse_PropertiesWithArg_DoesNotOverrideCommandLineValue() throws ParseException {
        options.addOption(new Option("b", true, "desc"));
        Properties props = new Properties();
        props.setProperty("b", "propval");
        CommandLine cmd = parser.parse(options, new String[]{"-b", "clival"}, props, false);
        assertEquals("Command line value should take precedence", "clival", cmd.getOptionValue("b"));
    }

    @Test
    public void testParse_PropertiesNull_DoesNothing() throws ParseException {
        CommandLine cmd = parser.parse(options, new String[]{"arg"}, (Properties) null, false);
        assertEquals("Args should contain 'arg'", 1, cmd.getArgs().length);
        assertEquals("arg", cmd.getArgs()[0]);
    }

    // ====== Test cases for uncovered parts ======

    @Test
    public void testParse_Properties_WithEmptyString_DoesNotAddOption() throws ParseException {
        options.addOption(new Option("a", false, "desc"));
        Properties props = new Properties();
        props.setProperty("a", "");
        CommandLine cmd = parser.parse(options, new String[]{}, props, false);
        assertFalse("Option 'a' should not be present for empty property value", cmd.hasOption("a"));
    }

    @Test
    public void testParse_Properties_WithNonBooleanValueForFlag_DoesNotAddOption() throws ParseException {
        options.addOption(new Option("a", false, "desc"));
        Properties props = new Properties();
        props.setProperty("a", "someValue");
        CommandLine cmd = parser.parse(options, new String[]{}, props, false);
        assertFalse("Option 'a' should not be present for non-boolean value", cmd.hasOption("a"));
    }

    @Test
    public void testParse_Properties_OptionNotInOptions_DoesNotThrow() throws ParseException {
        Properties props = new Properties();
        props.setProperty("unknownOpt", "true");
        CommandLine cmd = parser.parse(options, new String[]{}, props, false);
        assertFalse("Unknown property option should not be added", cmd.hasOption("unknownOpt"));
    }

    @Test
    public void testParse_Properties_FlagOptionFromProperty_WithStopAtNonOption() throws ParseException {
        options.addOption(new Option("a", false, "desc"));
        Properties props = new Properties();
        props.setProperty("a", "yes");
        CommandLine cmd = parser.parse(options, new String[]{"-b"}, props, true);
        // Property should add option 'a', and '-b' should be added as non-option arg
        assertTrue("Option 'a' should be present from property", cmd.hasOption("a"));
        String[] args = cmd.getArgs();
        assertEquals("Should have 1 arg", 1, args.length);
        assertEquals("-b", args[0]);
    }

    @Test
    public void testParse_Properties_WithStopAtNonOptionFalse_PropertyStillWorks() throws ParseException {
        options.addOption(new Option("a", false, "desc"));
        Properties props = new Properties();
        props.setProperty("a", "true");
        CommandLine cmd = parser.parse(options, new String[]{"foo", "bar"}, props, false);
        assertTrue("Option 'a' should be present from property", cmd.hasOption("a"));
        String[] args = cmd.getArgs();
        assertEquals("Should have 2 args", 2, args.length);
        assertEquals("foo", args[0]);
        assertEquals("bar", args[1]);
    }

    @Test
    public void testParse_Properties_ArgOptionWithDifferentPropertyValue() throws ParseException {
        options.addOption(new Option("c", true, "desc"));
        Properties props = new Properties();
        props.setProperty("c", "propValue");
        CommandLine cmd = parser.parse(options, new String[]{}, props, false);
        assertTrue("Option 'c' should be present", cmd.hasOption("c"));
        assertEquals("propValue", cmd.getOptionValue("c"));
    }

    @Test
    public void testParse_Properties_NoPropertiesProvided_DoesNothingWithStopAtNonOption() throws ParseException {
        options.addOption(new Option("a", false, "desc"));
        CommandLine cmd = parser.parse(options, new String[]{"arg1", "arg2"}, (Properties) null, true);
        assertEquals("Should have 2 args", 2, cmd.getArgs().length);
        assertEquals("arg1", cmd.getArgs()[0]);
        assertEquals("arg2", cmd.getArgs()[1]);
    }

    @Test
    public void testParse_Properties_OptionAlreadySetByCommandLine_PropertyValueIgnored() throws ParseException {
        options.addOption(new Option("a", false, "desc"));
        Properties props = new Properties();
        props.setProperty("a", "no");
        CommandLine cmd = parser.parse(options, new String[]{"-a"}, props, false);
        assertTrue("Option 'a' should be present from command line", cmd.hasOption("a"));
    }

    @Test
    public void testParse_Properties_CommandLineArgOptionWithPropertyValue_CommandLineTakesPrecedence() throws ParseException {
        options.addOption(new Option("d", true, "desc"));
        Properties props = new Properties();
        props.setProperty("d", "propVal");
        CommandLine cmd = parser.parse(options, new String[]{"-d", "cliVal"}, props, false);
        assertEquals("cliVal", cmd.getOptionValue("d"));
    }

    @Test
    public void testParse_Properties_BooleanPropertyWithUpperCaseTrue() throws ParseException {
        options.addOption(new Option("e", false, "desc"));
        Properties props = new Properties();
        props.setProperty("e", "TRUE");
        CommandLine cmd = parser.parse(options, new String[]{}, props, false);
        assertTrue("Option 'e' should be present for TRUE", cmd.hasOption("e"));
    }

    @Test
    public void testParse_Properties_BooleanPropertyWithUpperCaseYes() throws ParseException {
        options.addOption(new Option("f", false, "desc"));
        Properties props = new Properties();
        props.setProperty("f", "YES");
        CommandLine cmd = parser.parse(options, new String[]{}, props, false);
        assertTrue("Option 'f' should be present for YES", cmd.hasOption("f"));
    }

    @Test
    public void testParse_NullArgumentsWithProperties_WorksCorrectly() throws ParseException {
        options.addOption(new Option("a", false, "desc"));
        Properties props = new Properties();
        props.setProperty("a", "true");
        CommandLine cmd = parser.parse(options, null, props, false);
        assertTrue("Option 'a' should be present from property", cmd.hasOption("a"));
        assertEquals(0, cmd.getArgs().length);
    }
}