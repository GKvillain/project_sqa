package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class ParserTest {

    private static class TestParser extends Parser {
        @Override
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
            return arguments != null ? arguments : new String[0];
        }
    }

    private Parser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new TestParser();
        options = new Options();
    }

    // Tests parsing with null arguments array
    @Test
    public void testParse_nullArguments_returnsEmptyCommandLine() throws Exception {
        CommandLine cl = parser.parse(options, null);
        assertNotNull(cl);
        assertEquals(0, cl.getArgs().length);
    }

    // Tests normal parsing with valid options and argument values
    @Test
    public void testParse_validOptionsAndValues_parsedSuccessfully() throws Exception {
        options.addOption("a", false, "option a");
        options.addOption("b", true, "option b with arg");

        CommandLine cl = parser.parse(options, new String[]{"-a", "-b", "val", "arg1", "arg2"});

        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("val", cl.getOptionValue("b"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("arg1", cl.getArgs()[0]);
        assertEquals("arg2", cl.getArgs()[1]);
    }

    // Tests double-dash token stops option processing and remaining tokens are treated as args
    @Test
    public void testParse_doubleDash_eatsRemainingTokensAsArgs() throws Exception {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");

        CommandLine cl = parser.parse(options, new String[]{"-a", "--", "-b", "foo"});

        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-b", cl.getArgs()[0]);
        assertEquals("foo", cl.getArgs()[1]);
    }

    // Tests single-dash handling with stopAtNonOption false
    @Test
    public void testParse_singleDashNoStop_addsDashAsArg() throws Exception {
        CommandLine cl = parser.parse(options, new String[]{"-", "foo"}, false);

        assertEquals(2, cl.getArgs().length);
        assertEquals("-", cl.getArgs()[0]);
        assertEquals("foo", cl.getArgs()[1]);
    }

    // Tests single-dash handling with stopAtNonOption true
    @Test
    public void testParse_singleDashStopAtNonOption_eatsRemainingTokens() throws Exception {
        CommandLine cl = parser.parse(options, new String[]{"-", "foo"}, true);

        assertEquals(2, cl.getArgs().length);
        assertEquals("-", cl.getArgs()[0]);
        assertEquals("foo", cl.getArgs()[1]);
    }

    // Tests stopAtNonOption when encountering an unknown option
    @Test
    public void testParse_stopAtNonOptionUnknownOption_eatsRemainingTokensAsArgs() throws Exception {
        options.addOption("a", false, "option a");

        CommandLine cl = parser.parse(options, new String[]{"-a", "-unknown", "extra"}, true);

        assertTrue(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-unknown", cl.getArgs()[0]);
        assertEquals("extra", cl.getArgs()[1]);
    }

    // Tests stopAtNonOption when encountering a regular non-option argument
    @Test
    public void testParse_stopAtNonOptionNonOptionArg_eatsRemainingTokens() throws Exception {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");

        CommandLine cl = parser.parse(options, new String[]{"-a", "nonOption", "-b"}, true);

        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("nonOption", cl.getArgs()[0]);
        assertEquals("-b", cl.getArgs()[1]);
    }

    // Tests exception thrown on unrecognized option
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unrecognizedOption_throwsUnrecognizedOptionException() throws Exception {
        options.addOption("a", false, "option a");

        parser.parse(options, new String[]{"-unknown"});
    }

    // Tests exception thrown on missing required argument for an option
    @Test(expected = MissingArgumentException.class)
    public void testParse_missingArgument_throwsMissingArgumentException() throws Exception {
        options.addOption("b", true, "option b requires arg");

        parser.parse(options, new String[]{"-b"});
    }

    // Tests optional argument when value is omitted
    @Test
    public void testParse_optionalArgumentOmitted_doesNotThrowException() throws Exception {
        Option opt = new Option("b", true, "option b with optional arg");
        opt.setOptionalArg(true);
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[]{"-b"});

        assertTrue(cl.hasOption("b"));
        assertNull(cl.getOptionValue("b"));
    }

    // Tests exception thrown on missing single required option
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOption_throwsMissingOptionException() throws Exception {
        Option opt = new Option("r", false, "required option");
        opt.setRequired(true);
        options.addOption(opt);

        parser.parse(options, new String[]{});
    }

    // Tests exception thrown on missing multiple required options
    @Test(expected = MissingOptionException.class)
    public void testParse_multipleMissingRequiredOptions_throwsMissingOptionException() throws Exception {
        Option opt1 = new Option("r1", false, "required 1");
        opt1.setRequired(true);
        Option opt2 = new Option("r2", false, "required 2");
        opt2.setRequired(true);
        options.addOption(opt1);
        options.addOption(opt2);

        parser.parse(options, new String[]{});
    }

    // Tests reuse of options with required option across multiple parse calls (CLI-10)
    @Test(expected = MissingOptionException.class)
    public void testParse_reuseOptionsRequiredOptionMissingOnSecondCall_throwsMissingOptionException() throws Exception {
        Option opt = new Option("r", true, "required option");
        opt.setRequired(true);
        options.addOption(opt);

        // First parse satisfies required option
        CommandLine cl = parser.parse(options, new String[]{"-r", "val"});
        assertTrue(cl.hasOption("r"));

        // Second parse on same Options instance omits required option
        parser.parse(options, new String[]{});
    }

    // Tests OptionGroup marked as required is satisfied
    @Test
    public void testParse_requiredOptionGroup_satisfied() throws Exception {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        Option optA = new Option("a", false, "option a");
        Option optB = new Option("b", false, "option b");
        group.addOption(optA);
        group.addOption(optB);
        options.addOptionGroup(group);

        CommandLine cl = parser.parse(options, new String[]{"-a"});

        assertTrue(cl.hasOption("a"));
        assertEquals("a", group.getSelected());
    }

    // Tests properties populate option with argument
    @Test
    public void testParse_propertiesWithArgOption_setsOptionValue() throws Exception {
        options.addOption("p", true, "property option");
        Properties props = new Properties();
        props.setProperty("p", "propVal");

        CommandLine cl = parser.parse(options, new String[]{}, props);

        assertTrue(cl.hasOption("p"));
        assertEquals("propVal", cl.getOptionValue("p"));
    }

    // Tests properties populate boolean option with yes/true/1
    @Test
    public void testParse_propertiesWithBooleanOption_enablesOption() throws Exception {
        options.addOption("b", false, "boolean option");
        Properties props = new Properties();
        props.setProperty("b", "true");

        CommandLine cl = parser.parse(options, new String[]{}, props);

        assertTrue(cl.hasOption("b"));
    }

    // Tests properties with non-truthy value do not enable boolean option
    @Test
    public void testParse_propertiesWithFalseValue_ignoresBooleanOption() throws Exception {
        options.addOption("b", false, "boolean option");
        Properties props = new Properties();
        props.setProperty("b", "no");

        CommandLine cl = parser.parse(options, new String[]{}, props);

        assertFalse(cl.hasOption("b"));
    }

    // Tests selecting multiple options from the same OptionGroup throws AlreadySelectedException
    @Test(expected = AlreadySelectedException.class)
    public void testParse_multipleOptionsFromSameGroup_throwsAlreadySelectedException() throws Exception {
        OptionGroup group = new OptionGroup();
        Option optA = new Option("a", false, "option a");
        Option optB = new Option("b", false, "option b");
        group.addOption(optA);
        group.addOption(optB);
        options.addOptionGroup(group);

        parser.parse(options, new String[]{"-a", "-b"});
    }

    // Tests missing required OptionGroup throws MissingOptionException
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOptionGroup_throwsMissingOptionException() throws Exception {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", false, "option a"));
        group.addOption(new Option("b", false, "option b"));
        options.addOptionGroup(group);

        parser.parse(options, new String[]{});
    }

    // Tests multiple arguments option stops consuming values when the next token is an existing option
    @Test
    public void testParse_multipleArgsOptionFollowedByOption_stopsConsumingValues() throws Exception {
        Option multiOpt = new Option("m", true, "multi arg option");
        multiOpt.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(multiOpt);
        options.addOption("b", false, "option b");

        CommandLine cl = parser.parse(options, new String[]{"-m", "val1", "val2", "-b", "extra"});

        assertTrue(cl.hasOption("m"));
        assertArrayEquals(new String[]{"val1", "val2"}, cl.getOptionValues("m"));
        assertTrue(cl.hasOption("b"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("extra", cl.getArgs()[0]);
    }

    // Tests option arguments with leading and trailing quotes are stripped
    @Test
    public void testParse_quotedArgument_stripsQuotes() throws Exception {
        options.addOption("p", true, "property option");

        CommandLine cl = parser.parse(options, new String[]{"-p", "\"quoted-value\""});

        assertTrue(cl.hasOption("p"));
        assertEquals("quoted-value", cl.getOptionValue("p"));
    }

    // Tests properties with 'yes' and '1' activate boolean options
    @Test
    public void testParse_propertiesWithYesAndOne_enablesBooleanOptions() throws Exception {
        options.addOption("x", false, "option x");
        options.addOption("y", false, "option y");
        Properties props = new Properties();
        props.setProperty("x", "yes");
        props.setProperty("y", "1");

        CommandLine cl = parser.parse(options, new String[]{}, props);

        assertTrue(cl.hasOption("x"));
        assertTrue(cl.hasOption("y"));
    }

    // Tests properties containing unrecognized options are ignored without error
    @Test
    public void testParse_propertiesWithUnknownOption_ignored() throws Exception {
        options.addOption("a", false, "option a");
        Properties props = new Properties();
        props.setProperty("unknownProp", "value");

        CommandLine cl = parser.parse(options, new String[]{"-a"}, props);

        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("unknownProp"));
    }

    // Tests command-line arguments take precedence over properties
    @Test
    public void testParse_commandLineTakesPrecedenceOverProperties() throws Exception {
        options.addOption("p", true, "property option");
        Properties props = new Properties();
        props.setProperty("p", "fromProperties");

        CommandLine cl = parser.parse(options, new String[]{"-p", "fromCommandLine"}, props);

        assertTrue(cl.hasOption("p"));
        assertEquals("fromCommandLine", cl.getOptionValue("p"));
    }

    // Tests properties satisfy required boolean option
    @Test
    public void testParse_propertiesSatisfiesRequiredOption() throws Exception {
        Option opt = new Option("r", false, "required boolean");
        opt.setRequired(true);
        options.addOption(opt);

        Properties props = new Properties();
        props.setProperty("r", "true");

        CommandLine cl = parser.parse(options, new String[]{}, props);

        assertTrue(cl.hasOption("r"));
    }

    // Tests properties satisfy required OptionGroup for boolean and arg options
    @Test
    public void testParse_propertiesSatisfiesRequiredOptionGroup() throws Exception {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        Option optA = new Option("a", false, "option a");
        Option optB = new Option("b", true, "option b");
        group.addOption(optA);
        group.addOption(optB);
        options.addOptionGroup(group);

        Properties props = new Properties();
        props.setProperty("b", "groupVal");

        CommandLine cl = parser.parse(options, new String[]{}, props);

        assertTrue(cl.hasOption("b"));
        assertEquals("groupVal", cl.getOptionValue("b"));
        assertEquals("b", group.getSelected());
    }

    // Tests properties satisfy required OptionGroup with boolean option
    @Test
    public void testParse_propertiesSatisfiesRequiredOptionGroupWithBoolean() throws Exception {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        Option optA = new Option("a", false, "option a");
        Option optB = new Option("b", false, "option b");
        group.addOption(optA);
        group.addOption(optB);
        options.addOptionGroup(group);

        Properties props = new Properties();
        props.setProperty("a", "yes");

        CommandLine cl = parser.parse(options, new String[]{}, props);

        assertTrue(cl.hasOption("a"));
        assertEquals("a", group.getSelected());
    }
}