package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ParserTest {

    private Parser parser;
    private Options options;

    private static class DummyParser extends Parser {
        @Override
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
            if (arguments == null) {
                return new String[0];
            }
            return arguments;
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
        CommandLine cl = parser.parse(options, null);
        assertNotNull(cl);
        assertEquals(0, cl.getArgs().length);
    }

    // Tests normal option without argument
    @Test
    public void testParse_simpleOption_optionProcessed() throws Exception {
        options.addOption("a", "alpha", false, "simple flag option");
        String[] args = new String[]{"-a"};

        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("alpha"));
    }

    // Tests option with single argument
    @Test
    public void testParse_optionWithArgument_valueCaptured() throws Exception {
        Option opt = new Option("b", true, "option with value");
        options.addOption(opt);
        String[] args = new String[]{"-b", "foo"};

        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("b"));
        assertEquals("foo", cl.getOptionValue("b"));
    }

    // Tests missing argument for an option expecting one
    @Test(expected = MissingArgumentException.class)
    public void testParse_missingArgument_throwsException() throws Exception {
        Option opt = new Option("b", true, "option with value");
        options.addOption(opt);
        String[] args = new String[]{"-b"};

        parser.parse(options, args);
    }

    // Tests optional argument when value is missing
    @Test
    public void testParse_optionalArgumentMissing_noException() throws Exception {
        Option opt = new Option("b", true, "option with optional value");
        opt.setOptionalArg(true);
        options.addOption(opt);
        String[] args = new String[]{"-b"};

        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("b"));
        assertEquals(0, cl.getArgs().length);
    }

    // Tests unrecognized option throws exception
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unrecognizedOption_throwsException() throws Exception {
        String[] args = new String[]{"-unknown"};
        parser.parse(options, args);
    }

    // Tests missing required option throws exception
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOption_throwsException() throws Exception {
        Option opt = new Option("r", "req", false, "required option");
        opt.setRequired(true);
        options.addOption(opt);

        parser.parse(options, new String[]{});
    }

    // Tests required option provided successfully
    @Test
    public void testParse_requiredOptionPresent_success() throws Exception {
        Option opt = new Option("r", "req", false, "required option");
        opt.setRequired(true);
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[]{"-r"});
        assertTrue(cl.hasOption("r"));
    }

    // Tests required option reuse across multiple parses (Defects4J CLI-4 regression test)
    @Test(expected = MissingOptionException.class)
    public void testParse_reuseOptionsWithRequiredOption_throwsMissingOptionOnSecondParse() throws Exception {
        Option opt = new Option("r", "req", false, "required option");
        opt.setRequired(true);
        options.addOption(opt);

        CommandLine cl1 = parser.parse(options, new String[]{"-r"});
        assertTrue(cl1.hasOption("r"));

        parser.parse(options, new String[]{});
    }

    // Tests OptionGroup with required group
    @Test
    public void testParse_requiredOptionGroup_success() throws Exception {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        Option optA = new Option("a", "option A");
        Option optB = new Option("b", "option B");
        group.addOption(optA);
        group.addOption(optB);
        options.addOptionGroup(group);

        CommandLine cl = parser.parse(options, new String[]{"-a"});
        assertTrue(cl.hasOption("a"));
        assertEquals("a", group.getSelected());
    }

    // Tests double dash stops option parsing and eats remaining tokens
    @Test
    public void testParse_doubleDash_eatsRemainingArguments() throws Exception {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"--", "-a", "extra"};

        CommandLine cl = parser.parse(options, args);
        assertFalse(cl.hasOption("a"));
        String[] leftOver = cl.getArgs();
        assertEquals(2, leftOver.length);
        assertEquals("-a", leftOver[0]);
        assertEquals("extra", leftOver[1]);
    }

    // Tests single dash as argument
    @Test
    public void testParse_singleDash_parsedAsArgument() throws Exception {
        String[] args = new String[]{"-"};
        CommandLine cl = parser.parse(options, args, false);
        assertEquals(1, cl.getArgs().length);
        assertEquals("-", cl.getArgs()[0]);
    }

    // Tests single dash with stopAtNonOption true
    @Test
    public void testParse_singleDashStopAtNonOption_eatsRest() throws Exception {
        String[] args = new String[]{"-", "rest1", "rest2"};
        CommandLine cl = parser.parse(options, args, true);
        String[] leftOver = cl.getArgs();
        assertEquals(2, leftOver.length);
        assertEquals("rest1", leftOver[0]);
        assertEquals("rest2", leftOver[1]);
    }

    // Tests stopAtNonOption with unrecognized option
    @Test
    public void testParse_stopAtNonOptionWithUnrecognized_eatsRemaining() throws Exception {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-a", "-unrecognized", "arg1"};

        CommandLine cl = parser.parse(options, args, true);
        assertTrue(cl.hasOption("a"));
        String[] leftOver = cl.getArgs();
        assertEquals(2, leftOver.length);
        assertEquals("-unrecognized", leftOver[0]);
        assertEquals("arg1", leftOver[1]);
    }

    // Tests stopAtNonOption with plain argument
    @Test
    public void testParse_stopAtNonOptionWithPlainArgument_eatsRemaining() throws Exception {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"plain", "-a"};

        CommandLine cl = parser.parse(options, args, true);
        assertFalse(cl.hasOption("a"));
        String[] leftOver = cl.getArgs();
        assertEquals(2, leftOver.length);
        assertEquals("plain", leftOver[0]);
        assertEquals("-a", leftOver[1]);
    }

    // Tests parsing with properties for flag options
    @Test
    public void testParse_propertiesFlagOption_success() throws Exception {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        options.addOption("c", false, "option c");

        Properties props = new Properties();
        props.setProperty("a", "true");
        props.setProperty("b", "yes");
        props.setProperty("c", "1");

        CommandLine cl = parser.parse(options, new String[]{}, props);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertTrue(cl.hasOption("c"));
    }

    // Tests parsing with properties for flag options with invalid truthy value
    @Test
    public void testParse_propertiesInvalidFlagValue_ignored() throws Exception {
        options.addOption("a", false, "option a");

        Properties props = new Properties();
        props.setProperty("a", "no");

        CommandLine cl = parser.parse(options, new String[]{}, props);
        assertFalse(cl.hasOption("a"));
    }

    // Tests parsing with properties for option expecting value
    @Test
    public void testParse_propertiesWithArgOption_valueSet() throws Exception {
        Option opt = new Option("d", true, "option with arg");
        options.addOption(opt);

        Properties props = new Properties();
        props.setProperty("d", "propValue");

        CommandLine cl = parser.parse(options, new String[]{}, props);
        assertTrue(cl.hasOption("d"));
        assertEquals("propValue", cl.getOptionValue("d"));
    }

    // Tests that command line arguments take precedence over properties
    @Test
    public void testParse_propertiesNotOverridingCommandLineArg() throws Exception {
        Option opt = new Option("d", true, "option with arg");
        options.addOption(opt);

        Properties props = new Properties();
        props.setProperty("d", "propValue");

        CommandLine cl = parser.parse(options, new String[]{"-d", "cliValue"}, props);
        assertTrue(cl.hasOption("d"));
        assertEquals("cliValue", cl.getOptionValue("d"));
    }

    // Tests argument values enclosed with quotes are stripped
    @Test
    public void testParse_quotedArgument_quotesStripped() throws Exception {
        Option opt = new Option("b", true, "option with value");
        options.addOption(opt);
        String[] args = new String[]{"-b", "\"quoted-value\""};

        CommandLine cl = parser.parse(options, args);
        assertEquals("quoted-value", cl.getOptionValue("b"));
    }

    // Tests missing required OptionGroup throws MissingOptionException
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOptionGroup_throwsException() throws Exception {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "option A"));
        group.addOption(new Option("b", "option B"));
        options.addOptionGroup(group);

        parser.parse(options, new String[]{});
    }

    // Tests selecting multiple options from the same OptionGroup throws AlreadySelectedException
    @Test(expected = AlreadySelectedException.class)
    public void testParse_multipleOptionsFromSameGroup_throwsException() throws Exception {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "option A"));
        group.addOption(new Option("b", "option B"));
        options.addOptionGroup(group);

        parser.parse(options, new String[]{"-a", "-b"});
    }

    // Tests option with multiple arguments captured properly
    @Test
    public void testParse_multipleArgsOption_valuesCaptured() throws Exception {
        Option opt = new Option("m", "multi", true, "multi args");
        opt.setArgs(2);
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[]{"-m", "val1", "val2"});
        assertTrue(cl.hasOption("m"));
        String[] values = cl.getOptionValues("m");
        assertEquals(2, values.length);
        assertEquals("val1", values[0]);
        assertEquals("val2", values[1]);
    }

    // Tests option with unlimited arguments
    @Test
    public void testParse_unlimitedArgsOption_valuesCaptured() throws Exception {
        Option opt = new Option("m", "multi", true, "unlimited args");
        opt.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[]{"-m", "v1", "v2", "v3"});
        assertTrue(cl.hasOption("m"));
        String[] values = cl.getOptionValues("m");
        assertEquals(3, values.length);
        assertEquals("v1", values[0]);
        assertEquals("v2", values[1]);
        assertEquals("v3", values[2]);
    }

    // Tests option with value separator (e.g. -Dkey=value)
    @Test
    public void testParse_valueSeparator_valuesCaptured() throws Exception {
        Option opt = new Option("D", true, "property definition");
        opt.setValueSeparator('=');
        opt.setArgs(2);
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[]{"-D", "foo=bar"});
        assertTrue(cl.hasOption("D"));
        String[] values = cl.getOptionValues("D");
        assertEquals(2, values.length);
        assertEquals("foo", values[0]);
        assertEquals("bar", values[1]);
    }

    // Tests optional argument followed by another option
    @Test
    public void testParse_optionalArgFollowedByAnotherOption_handledCorrectly() throws Exception {
        Option optA = new Option("a", true, "optional arg");
        optA.setOptionalArg(true);
        Option optB = new Option("b", false, "flag");
        options.addOption(optA);
        options.addOption(optB);

        CommandLine cl = parser.parse(options, new String[]{"-a", "-b"});
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
    }

    // Tests single quotes stripped from argument
    @Test
    public void testParse_singleQuotedArgument_quotesStripped() throws Exception {
        Option opt = new Option("b", true, "option with value");
        options.addOption(opt);
        String[] args = new String[]{"-b", "'single-quoted'"};

        CommandLine cl = parser.parse(options, args);
        assertEquals("single-quoted", cl.getOptionValue("b"));
    }

    // Tests OptionGroup updated when option is provided via properties
    @Test
    public void testParse_propertiesWithOptionGroup_groupUpdated() throws Exception {
        OptionGroup group = new OptionGroup();
        Option optA = new Option("a", "option A");
        Option optB = new Option("b", "option B");
        group.addOption(optA);
        group.addOption(optB);
        options.addOptionGroup(group);

        Properties props = new Properties();
        props.setProperty("a", "true");

        CommandLine cl = parser.parse(options, new String[]{}, props);
        assertTrue(cl.hasOption("a"));
        assertEquals("a", group.getSelected());
    }

    // Tests null properties handled gracefully
    @Test
    public void testParse_nullProperties_success() throws Exception {
        options.addOption("a", false, "option a");
        CommandLine cl = parser.parse(options, new String[]{"-a"}, (Properties) null);
        assertTrue(cl.hasOption("a"));
    }

    // Tests property for unknown option is ignored
    @Test
    public void testParse_propertiesUnknownOption_ignored() throws Exception {
        Properties props = new Properties();
        props.setProperty("unknown", "value");

        CommandLine cl = parser.parse(options, new String[]{}, props);
        assertNotNull(cl);
        assertFalse(cl.hasOption("unknown"));
    }

    // Tests Parser getter and setter for options
    @Test
    public void testParse_optionsGettersAndSetters() throws Exception {
        parser.setOptions(options);
        assertEquals(options, parser.getOptions());
        assertNotNull(parser.getRequiredOptions());
    }
}