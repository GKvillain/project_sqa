package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.ListIterator;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link Parser}.
 */
public class ParserTest
{
    private Parser parser;
    private Options options;

    /**
     * Concrete implementation of Parser for testing purposes.
     */
    private static class TestParser extends Parser
    {
        @Override
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption)
        {
            return arguments != null ? arguments : new String[0];
        }
    }

    @Before
    public void setUp()
    {
        parser = new TestParser();
        options = new Options();
    }

    // Tests normal parsing with a single simple flag option
    @Test
    public void testParse_simpleOption_setsOptionOnCommandLine() throws Exception
    {
        Option optA = new Option("a", "enable-a", false, "option a");
        options.addOption(optA);

        String[] args = new String[] { "-a" };
        CommandLine cmd = parser.parse(options, args);

        assertNotNull(cmd);
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("enable-a"));
    }

    // Tests parsing option with an argument value
    @Test
    public void testParse_optionWithArgument_returnsArgumentValue() throws Exception
    {
        Option optB = new Option("b", true, "option b with value");
        options.addOption(optB);

        String[] args = new String[] { "-b", "foo" };
        CommandLine cmd = parser.parse(options, args);

        assertTrue(cmd.hasOption("b"));
        assertEquals("foo", cmd.getOptionValue("b"));
    }

    // Tests null arguments array handling
    @Test
    public void testParse_nullArguments_returnsEmptyCommandLine() throws Exception
    {
        CommandLine cmd = parser.parse(options, (String[]) null);

        assertNotNull(cmd);
        assertEquals(0, cmd.getArgs().length);
    }

    // Tests unrecognized option throws exception when stopAtNonOption is false
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unrecognizedOption_throwsUnrecognizedOptionException() throws Exception
    {
        String[] args = new String[] { "-unknown" };
        parser.parse(options, args, false);
    }

    // Tests unrecognized option stops parsing when stopAtNonOption is true
    @Test
    public void testParse_stopAtNonOptionWithUnrecognizedOption_addsToArgs() throws Exception
    {
        Option optA = new Option("a", false, "option a");
        options.addOption(optA);

        String[] args = new String[] { "-a", "-unknown", "extra" };
        CommandLine cmd = parser.parse(options, args, true);

        assertTrue(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-unknown", cmd.getArgs()[0]);
        assertEquals("extra", cmd.getArgs()[1]);
    }

    // Tests double dash token stops option parsing and adds remaining arguments
    @Test
    public void testParse_doubleDash_eatsRemainingTokens() throws Exception
    {
        Option optA = new Option("a", false, "option a");
        options.addOption(optA);

        String[] args = new String[] { "-a", "--", "-b", "value" };
        CommandLine cmd = parser.parse(options, args);

        assertTrue(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-b", cmd.getArgs()[0]);
        assertEquals("value", cmd.getArgs()[1]);
    }

    // Tests single dash token with stopAtNonOption true
    @Test
    public void testParse_singleDashWithStopAtNonOption_eatsRemainingTokens() throws Exception
    {
        String[] args = new String[] { "-", "extra" };
        CommandLine cmd = parser.parse(options, args, true);

        assertEquals(1, cmd.getArgs().length);
        assertEquals("extra", cmd.getArgs()[0]);
    }

    // Tests single dash token with stopAtNonOption false
    @Test
    public void testParse_singleDashWithoutStopAtNonOption_addsDashToArgs() throws Exception
    {
        String[] args = new String[] { "-" };
        CommandLine cmd = parser.parse(options, args, false);

        assertEquals(1, cmd.getArgs().length);
        assertEquals("-", cmd.getArgs()[0]);
    }

    // Tests non-option argument stops option parsing when stopAtNonOption is true
    @Test
    public void testParse_nonOptionArgWithStopAtNonOption_eatsRemainingTokens() throws Exception
    {
        Option optA = new Option("a", false, "option a");
        options.addOption(optA);

        String[] args = new String[] { "arg1", "-a" };
        CommandLine cmd = parser.parse(options, args, true);

        assertFalse(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("arg1", cmd.getArgs()[0]);
        assertEquals("-a", cmd.getArgs()[1]);
    }

    // Tests exception path when a required option is missing
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOption_throwsMissingOptionException() throws Exception
    {
        Option requiredOpt = new Option("r", false, "required option");
        requiredOpt.setRequired(true);
        options.addOption(requiredOpt);

        parser.parse(options, new String[0]);
    }

    // Tests required OptionGroup satisfied by one option
    @Test
    public void testParse_requiredOptionGroup_satisfiedOptionProcessed() throws Exception
    {
        Option opt1 = new Option("x", false, "option x");
        Option opt2 = new Option("y", false, "option y");
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);

        String[] args = new String[] { "-x" };
        CommandLine cmd = parser.parse(options, args);

        assertTrue(cmd.hasOption("x"));
        assertFalse(cmd.hasOption("y"));
    }

    // Tests exception path when option is missing required argument
    @Test(expected = MissingArgumentException.class)
    public void testParse_missingRequiredArgument_throwsMissingArgumentException() throws Exception
    {
        Option optB = new Option("b", true, "option b with argument");
        options.addOption(optB);

        String[] args = new String[] { "-b" };
        parser.parse(options, args);
    }

    // Tests optional argument option without value provided
    @Test
    public void testParse_optionalArgumentNotProvided_success() throws Exception
    {
        Option optO = new Option("o", true, "optional argument option");
        optO.setOptionalArg(true);
        options.addOption(optO);

        String[] args = new String[] { "-o" };
        CommandLine cmd = parser.parse(options, args);

        assertTrue(cmd.hasOption("o"));
        assertNull(cmd.getOptionValue("o"));
    }

    // Tests stripping leading and trailing quotes from argument values
    @Test
    public void testParse_quotedArgument_stripsQuotes() throws Exception
    {
        Option optV = new Option("v", true, "value option");
        options.addOption(optV);

        String[] args = new String[] { "-v", "\"quoted-string\"" };
        CommandLine cmd = parser.parse(options, args);

        assertEquals("quoted-string", cmd.getOptionValue("v"));
    }

    // Tests properties processing for option with argument
    @Test
    public void testParse_propertiesWithArgOption_setsPropertyValue() throws Exception
    {
        Option optP = new Option("p", true, "property option");
        options.addOption(optP);

        Properties props = new Properties();
        props.setProperty("p", "propValue");

        CommandLine cmd = parser.parse(options, new String[0], props);

        assertTrue(cmd.hasOption("p"));
        assertEquals("propValue", cmd.getOptionValue("p"));
    }

    // Tests properties processing for flag options with various truthy/falsy values
    @Test
    public void testParse_propertiesBooleanValues_evaluatesCorrectly() throws Exception
    {
        Option optTrue = new Option("t", false, "true flag");
        Option optYes = new Option("y", false, "yes flag");
        Option optOne = new Option("one", false, "1 flag");
        Option optNo = new Option("n", false, "no flag");

        options.addOption(optTrue);
        options.addOption(optYes);
        options.addOption(optOne);
        options.addOption(optNo);

        Properties props = new Properties();
        props.setProperty("t", "true");
        props.setProperty("y", "yes");
        props.setProperty("one", "1");
        props.setProperty("n", "no");

        CommandLine cmd = parser.parse(options, new String[0], props);

        assertTrue(cmd.hasOption("t"));
        assertTrue(cmd.hasOption("y"));
        assertTrue(cmd.hasOption("one"));
        assertFalse(cmd.hasOption("n"));
    }

    // Tests command line arguments take precedence over properties
    @Test
    public void testParse_propertiesOptionAlreadyPresentInArgs_argsTakePrecedence() throws Exception
    {
        Option optV = new Option("v", true, "value optical");
        options.addOption(optV);

        Properties props = new Properties();
        props.setProperty("v", "propValue");

        String[] args = new String[] { "-v", "argValue" };
        CommandLine cmd = parser.parse(options, args, props);

        assertTrue(cmd.hasOption("v"));
        assertEquals("argValue", cmd.getOptionValue("v"));
    }

    // Tests properties handling when an OptionGroup option has already been selected via arguments (CLI-30)
    @Test
    public void testParse_propertiesOptionGroupAlreadySelected_ignoresPropertyOption() throws Exception
    {
        Option optA = new Option("a", false, "option a");
        Option optB = new Option("b", false, "option b");
        OptionGroup group = new OptionGroup();
        group.addOption(optA);
        group.addOption(optB);
        options.addOptionGroup(group);

        Properties props = new Properties();
        props.setProperty("b", "true");

        String[] args = new String[] { "-a" };
        CommandLine cmd = parser.parse(options, args, props);

        assertTrue(cmd.hasOption("a"));
        assertEquals("a", group.getSelected());
    }

    // Tests re-parsing with same options clears previous values (CLI-71)
    @Test
    public void testParse_reuseOptions_clearsPreviousValues() throws Exception
    {
        Option optV = new Option("v", true, "value option");
        options.addOption(optV);

        String[] firstArgs = new String[] { "-v", "first" };
        CommandLine cmd1 = parser.parse(options, firstArgs);
        assertEquals("first", cmd1.getOptionValue("v"));

        String[] secondArgs = new String[] { "-v", "second" };
        CommandLine cmd2 = parser.parse(options, secondArgs);
        assertEquals("second", cmd2.getOptionValue("second") != null ? cmd2.getOptionValue("second") : cmd2.getOptionValue("v"));
    }

    // Tests multiple arguments option processing
    @Test
    public void testParse_multipleArgumentsOption_addsAllValues() throws Exception
    {
        Option optM = new Option("m", true, "multiple values option");
        optM.setArgs(2);
        options.addOption(optM);

        String[] args = new String[] { "-m", "val1", "val2" };
        CommandLine cmd = parser.parse(options, args);

        assertTrue(cmd.hasOption("m"));
        String[] values = cmd.getOptionValues("m");
        assertEquals(2, values.length);
        assertEquals("val1", values[0]);
        assertEquals("val2", values[1]);
    }

    // Tests full parse overload with properties and stopAtNonOption boolean
    @Test
    public void testParse_fullSignatureWithPropertiesAndStopAtNonOption() throws Exception
    {
        Option optA = new Option("a", false, "flag a");
        Option optB = new Option("b", true, "value b");
        options.addOption(optA);
        options.addOption(optB);

        Properties props = new Properties();
        props.setProperty("b", "fromProps");

        String[] args = new String[] { "-a", "extra" };
        CommandLine cmd = parser.parse(options, args, props, true);

        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
        assertEquals("fromProps", cmd.getOptionValue("b"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("extra", cmd.getArgs()[0]);
    }
}