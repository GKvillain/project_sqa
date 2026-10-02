package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.ListIterator;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ParserTest {

    private Parser parser;
    private Options options;

    private static class DummyParser extends Parser {
        @Override
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
            return arguments != null ? arguments : new String[0];
        }
    }

    @Before
    public void setUp() {
        parser = new DummyParser();
        options = new Options();
    }

    // Tests parsing with null arguments array
    @Test
    public void testParse_nullArguments_returnsEmptyCommandLine() throws Exception {
        CommandLine cmd = parser.parse(options, null);
        assertNotNull(cmd);
        assertEquals(0, cmd.getArgs().length);
    }

    // Tests normal option parsing with argument value
    @Test
    public void testParse_simpleOptionWithArg_success() throws Exception {
        Option opt = new Option("a", true, "option a");
        options.addOption(opt);

        CommandLine cmd = parser.parse(options, new String[]{"-a", "value1"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("value1", cmd.getOptionValue("a"));
    }

    // Tests option without argument value
    @Test
    public void testParse_flagOption_success() throws Exception {
        Option opt = new Option("f", false, "flag option");
        options.addOption(opt);

        CommandLine cmd = parser.parse(options, new String[]{"-f"});
        assertTrue(cmd.hasOption("f"));
    }

    // Tests unrecognized option throws UnrecognizedOptionException
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unrecognizedOption_throwsException() throws Exception {
        parser.parse(options, new String[]{"-unknown"});
    }

    // Tests missing required option throws MissingOptionException
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOption_throwsException() throws Exception {
        Option opt = new Option("r", false, "required option");
        opt.setRequired(true);
        options.addOption(opt);

        parser.parse(options, new String[]{});
    }

    // Tests present required option succeeds
    @Test
    public void testParse_presentRequiredOption_success() throws Exception {
        Option opt = new Option("r", false, "required option");
        opt.setRequired(true);
        options.addOption(opt);

        CommandLine cmd = parser.parse(options, new String[]{"-r"});
        assertTrue(cmd.hasOption("r"));
    }

    // Tests option requiring an argument but none provided throws MissingArgumentException
    @Test(expected = MissingArgumentException.class)
    public void testParse_missingRequiredArgument_throwsException() throws Exception {
        Option opt = new Option("a", true, "arg option");
        options.addOption(opt);

        parser.parse(options, new String[]{"-a"});
    }

    // Tests option with optional argument when argument is omitted
    @Test
    public void testParse_optionalArgumentMissing_success() throws Exception {
        Option opt = new Option("o", true, "optional arg");
        opt.setOptionalArg(true);
        options.addOption(opt);

        CommandLine cmd = parser.parse(options, new String[]{"-o"});
        assertTrue(cmd.hasOption("o"));
        assertNull(cmd.getOptionValue("o"));
    }

    // Tests double dash "--" token to stop processing options and eat the rest as args
    @Test
    public void testParse_doubleDash_eatsRemainingTokens() throws Exception {
        Option opt = new Option("a", false, "option a");
        options.addOption(opt);

        CommandLine cmd = parser.parse(options, new String[]{"-a", "--", "-b", "arg1"});
        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-b", cmd.getArgs()[0]);
        assertEquals("arg1", cmd.getArgs()[1]);
    }

    // Tests single dash "-" token when stopAtNonOption is false
    @Test
    public void testParse_singleDash_addedAsArgument() throws Exception {
        CommandLine cmd = parser.parse(options, new String[]{"-", "foo"}, false);
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-", cmd.getArgs()[0]);
        assertEquals("foo", cmd.getArgs()[1]);
    }

    // Tests single dash "-" token when stopAtNonOption is true
    @Test
    public void testParse_singleDashWithStopAtNonOption_eatsTheRest() throws Exception {
        CommandLine cmd = parser.parse(options, new String[]{"-", "foo"}, true);
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-", cmd.getArgs()[0]);
        assertEquals("foo", cmd.getArgs()[1]);
    }

    // Tests non-option token with stopAtNonOption true stops option parsing
    @Test
    public void testParse_stopAtNonOption_stopsAtFirstNonOption() throws Exception {
        Option opt = new Option("a", false, "option a");
        options.addOption(opt);

        CommandLine cmd = parser.parse(options, new String[]{"nonOption", "-a"}, true);
        assertFalse(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("nonOption", cmd.getArgs()[0]);
        assertEquals("-a", cmd.getArgs()[1]);
    }

    // Tests unknown option with stopAtNonOption true treated as non-option and eats rest
    @Test
    public void testParse_unknownOptionStopAtNonOption_treatedAsArg() throws Exception {
        CommandLine cmd = parser.parse(options, new String[]{"-unknown", "foo"}, true);
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-unknown", cmd.getArgs()[0]);
        assertEquals("foo", cmd.getArgs()[1]);
    }

    // Tests processing properties for options with arguments and flag options
    @Test
    public void testParse_withProperties_setsOptionsAndValues() throws Exception {
        Option optA = new Option("a", true, "option a");
        Option optB = new Option("b", false, "flag b");
        Option optC = new Option("c", false, "flag c");
        options.addOption(optA);
        options.addOption(optB);
        options.addOption(optC);

        Properties props = new Properties();
        props.setProperty("a", "propValue");
        props.setProperty("b", "true");
        props.setProperty("c", "no");

        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("a"));
        assertEquals("propValue", cmd.getOptionValue("a"));
        assertTrue(cmd.hasOption("b"));
        assertFalse(cmd.hasOption("c"));
    }

    // Tests OptionGroup integration when required group is satisfied
    @Test
    public void testParse_requiredOptionGroup_success() throws Exception {
        Option opt1 = new Option("x", false, "option x");
        Option opt2 = new Option("y", false, "option y");
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);

        CommandLine cmd = parser.parse(options, new String[]{"-x"});
        assertTrue(cmd.hasOption("x"));
        assertEquals("x", group.getSelected());
    }

    // Tests MissingOptionException when required OptionGroup is omitted
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOptionGroup_throwsException() throws Exception {
        Option opt1 = new Option("x", false, "option x");
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(opt1);
        options.addOptionGroup(group);

        parser.parse(options, new String[]{});
    }

    // Tests stripping of quotes from option argument values
    @Test
    public void testParse_quotedArgument_stripsQuotes() throws Exception {
        Option opt = new Option("a", true, "option a");
        options.addOption(opt);

        CommandLine cmd = parser.parse(options, new String[]{"-a", "\"quoted value\""});
        assertTrue(cmd.hasOption("a"));
        assertEquals("quoted value", cmd.getOptionValue("a"));
    }

    // Tests that previous option values are cleared upon reuse (CLI-71)
    @Test
    public void testParse_reuseOptions_clearsPreviousValues() throws Exception {
        Option opt = new Option("a", true, "option a");
        options.addOption(opt);

        CommandLine cmd1 = parser.parse(options, new String[]{"-a", "first"});
        assertEquals("first", cmd1.getOptionValue("a"));

        CommandLine cmd2 = parser.parse(options, new String[]{"-a", "second"});
        assertEquals("second", cmd2.getOptionValue("a"));
        assertEquals(1, cmd2.getOptionValues("a").length);
    }

    // Tests processArgs with multiple arguments
    @Test
    public void testProcessArgs_multipleArgs() throws Exception {
        Option opt = new Option("m", true, "multiple args");
        opt.setArgs(2);
        options.addOption(opt);

        CommandLine cmd = parser.parse(options, new String[]{"-m", "val1", "val2"});
        assertTrue(cmd.hasOption("m"));
        assertEquals("val1", cmd.getOptionValues("m")[0]);
        assertEquals("val2", cmd.getOptionValues("m")[1]);
    }

    // Tests processArgs stopping when next token is another option
    @Test
    public void testProcessArgs_stopsAtNextOption() throws Exception {
        Option opt1 = new Option("m", true, "multiple args");
        opt1.setArgs(2);
        Option opt2 = new Option("b", false, "flag b");
        options.addOption(opt1);
        options.addOption(opt2);

        CommandLine cmd = parser.parse(options, new String[]{"-m", "val1", "-b"});
        assertTrue(cmd.hasOption("m"));
        assertEquals(1, cmd.getOptionValues("m").length);
        assertEquals("val1", cmd.getOptionValue("m"));
        assertTrue(cmd.hasOption("b"));
    }

    // Tests processArgs with single quoted argument
    @Test
    public void testParse_singleQuoteStripped() throws Exception {
        Option opt = new Option("s", true, "single quote");
        options.addOption(opt);

        CommandLine cmd = parser.parse(options, new String[]{"-s", "'single quoted'"});
        assertTrue(cmd.hasOption("s"));
        assertEquals("single quoted", cmd.getOptionValue("s"));
    }

    // Tests processOption throws AlreadySelectedException when selecting two options from same OptionGroup
    @Test(expected = AlreadySelectedException.class)
    public void testParse_alreadySelectedException_throwsException() throws Exception {
        Option opt1 = new Option("x", false, "option x");
        Option opt2 = new Option("y", false, "option y");
        OptionGroup group = new OptionGroup();
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);

        parser.parse(options, new String[]{"-x", "-y"});
    }

    // Tests properties with truthy values "yes", "1", "on"
    @Test
    public void testParse_propertiesWithVariousTruthyValues() throws Exception {
        Option optYes = new Option("y", false, "yes option");
        Option optOne = new Option("o", false, "one option");
        Option optOn = new Option("n", false, "on option");
        options.addOption(optYes);
        options.addOption(optOne);
        options.addOption(optOn);

        Properties props = new Properties();
        props.setProperty("y", "yes");
        props.setProperty("o", "1");
        props.setProperty("n", "on");

        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("y"));
        assertTrue(cmd.hasOption("o"));
        assertTrue(cmd.hasOption("n"));
    }

    // Tests property value is ignored if option already specified on command line
    @Test
    public void testParse_propertyIgnoredIfOptionAlreadyPresent() throws Exception {
        Option opt = new Option("a", true, "option a");
        options.addOption(opt);

        Properties props = new Properties();
        props.setProperty("a", "fromProps");

        CommandLine cmd = parser.parse(options, new String[]{"-a", "fromCmd"}, props);
        assertTrue(cmd.hasOption("a"));
        assertEquals("fromCmd", cmd.getOptionValue("a"));
    }

    // Tests getters and setters for options and required options
    @Test
    public void testGettersAndSetters() {
        parser.setOptions(options);
        assertEquals(options, parser.getOptions());
        assertNotNull(parser.getRequiredOptions());
    }

    // Tests parsing with null properties does not fail
    @Test
    public void testParse_nullProperties_success() throws Exception {
        Option opt = new Option("a", false, "option a");
        options.addOption(opt);

        CommandLine cmd = parser.parse(options, new String[]{"-a"}, (Properties) null);
        assertTrue(cmd.hasOption("a"));
    }
}