package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class ParserTest {

    private Parser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new Parser() {
            protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
                return arguments != null ? arguments : new String[0];
            }
        };
        options = new Options();
    }

    // Tests normal parsing with a single boolean option
    @Test
    public void testParse_simpleOption_parsedSuccessfully() throws Exception {
        options.addOption("a", "alpha", false, "Alpha option");
        CommandLine cmd = parser.parse(options, new String[]{"-a"});

        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("alpha"));
    }

    // Tests parsing with null arguments array
    @Test
    public void testParse_nullArguments_returnsEmptyCommandLine() throws Exception {
        options.addOption("a", false, "Alpha option");
        CommandLine cmd = parser.parse(options, (String[]) null);

        assertNotNull(cmd);
        assertFalse(cmd.hasOption("a"));
        assertEquals(0, cmd.getArgs().length);
    }

    // Tests parsing option with argument value
    @Test
    public void testParse_optionWithArgument_returnsOptionValue() throws Exception {
        Option optB = OptionBuilder.hasArg().create('b');
        options.addOption(optB);

        CommandLine cmd = parser.parse(options, new String[]{"-b", "valueB"});
        assertTrue(cmd.hasOption("b"));
        assertEquals("valueB", cmd.getOptionValue("b"));
    }

    // Tests exception path when required argument is missing
    @Test(expected = MissingArgumentException.class)
    public void testParse_missingOptionArgument_throwsException() throws Exception {
        Option optB = OptionBuilder.hasArg().create('b');
        options.addOption(optB);

        parser.parse(options, new String[]{"-b"});
    }

    // Tests exception path for unrecognized option
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unrecognizedOption_throwsException() throws Exception {
        options.addOption("a", false, "Alpha option");
        parser.parse(options, new String[]{"-unknown"});
    }

    // Tests exception path when required option is not supplied
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOption_throwsException() throws Exception {
        Option optR = OptionBuilder.isRequired().create('r');
        options.addOption(optR);

        parser.parse(options, new String[0]);
    }

    // Tests single dash token handling when stopAtNonOption is false
    @Test
    public void testParse_singleDash_addedAsArgument() throws Exception {
        CommandLine cmd = parser.parse(options, new String[]{"-"}, false);

        assertEquals(1, cmd.getArgs().length);
        assertEquals("-", cmd.getArgs()[0]);
    }

    // Tests single dash token handling when stopAtNonOption is true
    @Test
    public void testParse_singleDashWithStopAtNonOption_eatsTheRest() throws Exception {
        CommandLine cmd = parser.parse(options, new String[]{"-", "extra1", "extra2"}, true);

        assertEquals(3, cmd.getArgs().length);
        assertEquals("-", cmd.getArgs()[0]);
        assertEquals("extra1", cmd.getArgs()[1]);
        assertEquals("extra2", cmd.getArgs()[2]);
    }

    // Tests double dash token behavior eating subsequent tokens
    @Test
    public void testParse_doubleDash_eatsRemainingTokensAsArgs() throws Exception {
        options.addOption("a", false, "Alpha option");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "--", "-b", "extra"});

        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-b", cmd.getArgs()[0]);
        assertEquals("extra", cmd.getArgs()[1]);
    }

    // Tests stopAtNonOption encountering unrecognized option
    @Test
    public void testParse_stopAtNonOptionWithUnrecognizedOption_stopsParsing() throws Exception {
        options.addOption("a", false, "Alpha option");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "-nonOption", "arg1"}, true);

        assertTrue(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-nonOption", cmd.getArgs()[0]);
        assertEquals("arg1", cmd.getArgs()[1]);
    }

    // Tests stopAtNonOption encountering normal argument
    @Test
    public void testParse_stopAtNonOptionWithNormalArg_stopsParsing() throws Exception {
        options.addOption("a", false, "Alpha option");
        CommandLine cmd = parser.parse(options, new String[]{"plainArg", "-a"}, true);

        assertFalse(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("plainArg", cmd.getArgs()[0]);
        assertEquals("-a", cmd.getArgs()[1]);
    }

    // Tests required OptionGroup selection and resolution
    @Test
    public void testParse_requiredOptionGroup_satisfied() throws Exception {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("f", "file", false, "file option"));
        group.addOption(new Option("d", "dir", false, "directory option"));
        options.addOptionGroup(group);

        CommandLine cmd = parser.parse(options, new String[]{"-f"});
        assertTrue(cmd.hasOption("f"));
        assertEquals("f", group.getSelected());
    }

    // Tests optional argument handling where next token is another option
    @Test
    public void testParse_optionalArgumentNotProvided_succeeds() throws Exception {
        Option opt = OptionBuilder.hasOptionalArg().create('o');
        options.addOption(opt);
        options.addOption("b", false, "Beta option");

        CommandLine cmd = parser.parse(options, new String[]{"-o", "-b"});
        assertTrue(cmd.hasOption("o"));
        assertTrue(cmd.hasOption("b"));
        assertNull(cmd.getOptionValue("o"));
    }

    // Tests properties processing for both boolean and argument options
    @Test
    public void testParse_withProperties_populatesCommandLine() throws Exception {
        options.addOption("a", false, "Alpha option");
        Option optB = OptionBuilder.hasArg().create('b');
        options.addOption(optB);

        Properties props = new Properties();
        props.setProperty("a", "true");
        props.setProperty("b", "propValue");

        CommandLine cmd = parser.parse(options, new String[0], props);

        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
        assertEquals("propValue", cmd.getOptionValue("b"));
    }

    // Tests properties loop when encountering non-true flag value followed by valid property
    @Test
    public void testProcessProperties_nonTrueFlagDoesNotPreventSubsequentProperties() throws Exception {
        options.addOption("a", false, "Alpha flag");
        options.addOption("b", false, "Beta flag");
        Option optC = OptionBuilder.hasArg().create('c');
        options.addOption(optC);

        Properties props = new Properties();
        props.setProperty("a", "no");
        props.setProperty("b", "yes");
        props.setProperty("c", "valueC");

        CommandLine cmd = parser.parse(options, new String[0], props);

        assertFalse(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
        assertTrue(cmd.hasOption("c"));
        assertEquals("valueC", cmd.getOptionValue("c"));
    }

    // Tests processProperties with null properties object
    @Test
    public void testProcessProperties_nullProperties_noException() throws Exception {
        options.addOption("a", false, "Alpha option");
        CommandLine cmd = parser.parse(options, new String[]{"-a"}, null);

        assertNotNull(cmd);
        assertTrue(cmd.hasOption("a"));
    }

    // Tests OptionGroup collision throws AlreadySelectedException
    @Test(expected = AlreadySelectedException.class)
    public void testParse_optionGroupMultipleSelected_throwsException() throws Exception {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("f", "file", false, "file option"));
        group.addOption(new Option("d", "dir", false, "directory option"));
        options.addOptionGroup(group);

        parser.parse(options, new String[]{"-f", "-d"});
    }

    // Tests missing required OptionGroup throws MissingOptionException
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOptionGroup_throwsException() throws Exception {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("f", "file", false, "file option"));
        group.addOption(new Option("d", "dir", false, "directory option"));
        options.addOptionGroup(group);

        parser.parse(options, new String[0]);
    }

    // Tests optional argument when argument is provided
    @Test
    public void testParse_optionalArgumentProvided_returnsValue() throws Exception {
        Option opt = OptionBuilder.hasOptionalArg().create('o');
        options.addOption(opt);

        CommandLine cmd = parser.parse(options, new String[]{"-o", "optionalValue"});
        assertTrue(cmd.hasOption("o"));
        assertEquals("optionalValue", cmd.getOptionValue("o"));
    }

    // Tests multiple arguments for an option
    @Test
    public void testParse_multipleArguments_returnsAllValues() throws Exception {
        Option optM = OptionBuilder.hasArgs(2).create('m');
        options.addOption(optM);

        CommandLine cmd = parser.parse(options, new String[]{"-m", "val1", "val2"});
        assertTrue(cmd.hasOption("m"));
        assertArrayEquals(new String[]{"val1", "val2"}, cmd.getOptionValues("m"));
    }

    // Tests value separator processing
    @Test
    public void testParse_valueSeparator_splitsValues() throws Exception {
        Option optD = OptionBuilder.hasArgs().withValueSeparator('=').create('D');
        options.addOption(optD);

        CommandLine cmd = parser.parse(options, new String[]{"-D", "key=value"});
        assertTrue(cmd.hasOption("D"));
        assertArrayEquals(new String[]{"key", "value"}, cmd.getOptionValues("D"));
    }

    // Tests properties processing ignores options not defined in Options
    @Test
    public void testProcessProperties_unknownProperty_ignored() throws Exception {
        options.addOption("a", false, "Alpha option");

        Properties props = new Properties();
        props.setProperty("unknownProp", "someValue");

        CommandLine cmd = parser.parse(options, new String[0], props);
        assertNotNull(cmd);
        assertFalse(cmd.hasOption("unknownProp"));
    }

    // Tests properties processing does not overwrite option already supplied via command line
    @Test
    public void testProcessProperties_optionAlreadySetByCommandLine_propertyNotUsed() throws Exception {
        Option optB = OptionBuilder.hasArg().create('b');
        options.addOption(optB);

        Properties props = new Properties();
        props.setProperty("b", "propValue");

        CommandLine cmd = parser.parse(options, new String[]{"-b", "cliValue"}, props);
        assertEquals("cliValue", cmd.getOptionValue("b"));
    }

    // Tests properties selecting an option in OptionGroup
    @Test
    public void testProcessProperties_selectsOptionGroup() throws Exception {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("f", "file", false, "file option"));
        group.addOption(new Option("d", "dir", false, "directory option"));
        options.addOptionGroup(group);

        Properties props = new Properties();
        props.setProperty("f", "true");

        CommandLine cmd = parser.parse(options, new String[0], props);
        assertTrue(cmd.hasOption("f"));
        assertEquals("f", group.getSelected());
    }

    // Tests getters and setters for options and required options
    @Test
    public void testGettersAndSetters() {
        parser.setOptions(options);
        assertSame(options, parser.getOptions());
        assertNotNull(parser.getRequiredOptions());
    }
}