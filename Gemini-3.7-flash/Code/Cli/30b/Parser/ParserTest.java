package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class ParserTest
{
    private Parser parser;
    private Options options;

    private static class DummyParser extends Parser
    {
        @Override
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption)
        {
            return arguments;
        }
    }

    @Before
    public void setUp()
    {
        parser = new DummyParser();
        options = new Options();
    }

    // Tests normal parsing of options and non-option arguments
    @Test
    public void testParse_simpleOptionsAndArgs_success() throws Exception
    {
        options.addOption("a", "all", false, "toggle all");
        options.addOption("f", "file", true, "output file");

        String[] args = new String[]{"-a", "-f", "test.txt", "extra1", "extra2"};
        CommandLine cl = parser.parse(options, args);

        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("f"));
        assertEquals("test.txt", cl.getOptionValue("f"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("extra1", cl.getArgs()[0]);
        assertEquals("extra2", cl.getArgs()[1]);
    }

    // Tests null arguments array handled as empty array
    @Test
    public void testParse_nullArguments_treatedAsEmpty() throws Exception
    {
        CommandLine cl = parser.parse(options, null);
        assertNotNull(cl);
        assertEquals(0, cl.getArgs().length);
        assertEquals(0, cl.getOptions().length);
    }

    // Tests unrecognized option without stopAtNonOption throws UnrecognizedOptionException
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unrecognizedOption_throwsUnrecognizedOptionException() throws Exception
    {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-b"};
        parser.parse(options, args, false);
    }

    // Tests stopAtNonOption stops at unrecognized option and adds remaining to args
    @Test
    public void testParse_stopAtNonOptionUnrecognized_addsRemainingToArgs() throws Exception
    {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-a", "-b", "extra"};
        CommandLine cl = parser.parse(options, args, true);

        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-b", cl.getArgs()[0]);
        assertEquals("extra", cl.getArgs()[1]);
    }

    // Tests stopAtNonOption stops at non-option argument
    @Test
    public void testParse_stopAtNonOptionArgument_stopsParsing() throws Exception
    {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        String[] args = new String[]{"arg1", "-a", "-b"};
        CommandLine cl = parser.parse(options, args, true);

        assertFalse(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(3, cl.getArgs().length);
        assertEquals("arg1", cl.getArgs()[0]);
        assertEquals("-a", cl.getArgs()[1]);
        assertEquals("-b", cl.getArgs()[2]);
    }

    // Tests double dash stops parsing options and eats remaining tokens
    @Test
    public void testParse_doubleDash_stopsParsing() throws Exception
    {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"--", "-a", "arg1"};
        CommandLine cl = parser.parse(options, args);

        assertFalse(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-a", cl.getArgs()[0]);
        assertEquals("arg1", cl.getArgs()[1]);
    }

    // Tests single dash handling without stopAtNonOption
    @Test
    public void testParse_singleDashWithoutStopAtNonOption_addsAsArg() throws Exception
    {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-", "-a"};
        CommandLine cl = parser.parse(options, args, false);

        assertTrue(cl.hasOption("a"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("-", cl.getArgs()[0]);
    }

    // Tests single dash handling with stopAtNonOption
    @Test
    public void testParse_singleDashWithStopAtNonOption_stopsParsing() throws Exception
    {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-", "-a"};
        CommandLine cl = parser.parse(options, args, true);

        assertFalse(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-", cl.getArgs()[0]);
        assertEquals("-a", cl.getArgs()[1]);
    }

    // Tests missing required option throws MissingOptionException
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOption_throwsMissingOptionException() throws Exception
    {
        Option req = new Option("r", "req", false, "required option");
        req.setRequired(true);
        options.addOption(req);

        parser.parse(options, new String[]{});
    }

    // Tests required OptionGroup satisfied
    @Test
    public void testParse_requiredOptionGroup_satisfied() throws Exception
    {
        Option opt1 = new Option("a", "optA", false, "option A");
        Option opt2 = new Option("b", "optB", false, "option B");
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);

        CommandLine cl = parser.parse(options, new String[]{"-a"});
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
    }

    // Tests missing required OptionGroup throws MissingOptionException
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOptionGroup_throwsMissingOptionException() throws Exception
    {
        Option opt1 = new Option("a", "optA", false, "option A");
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(opt1);
        options.addOptionGroup(group);

        parser.parse(options, new String[]{});
    }

    // Tests missing argument for option throws MissingArgumentException
    @Test(expected = MissingArgumentException.class)
    public void testParse_missingArgument_throwsMissingArgumentException() throws Exception
    {
        options.addOption("f", true, "file option");
        parser.parse(options, new String[]{"-f"});
    }

    // Tests optional argument when not provided
    @Test
    public void testParse_optionalArgumentNotProvided_success() throws Exception
    {
        Option opt = new Option("o", true, "optional arg option");
        opt.setOptionalArg(true);
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[]{"-o"});
        assertTrue(cl.hasOption("o"));
        assertNull(cl.getOptionValue("o"));
    }

    // Tests quoted argument stripping
    @Test
    public void testParse_quotedArgument_stripsQuotes() throws Exception
    {
        options.addOption("f", true, "file");
        CommandLine cl = parser.parse(options, new String[]{"-f", "\"quoted value\""});

        assertTrue(cl.hasOption("f"));
        assertEquals("quoted value", cl.getOptionValue("f"));
    }

    // Tests option argument followed by another option
    @Test
    public void testParse_optionFollowedByOption_doesNotConsumeOptionAsValue() throws Exception
    {
        options.addOption("a", false, "flag");
        options.addOption("b", true, "takes value");
        options.addOption("c", false, "flag");

        CommandLine cl = parser.parse(options, new String[]{"-a", "-b", "val", "-c"});
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("val", cl.getOptionValue("b"));
        assertTrue(cl.hasOption("c"));
    }

    // Tests processProperties with option values
    @Test
    public void testParse_propertiesWithArg_setsValue() throws Exception
    {
        options.addOption("f", "file", true, "file option");

        Properties props = new Properties();
        props.setProperty("f", "config.xml");

        CommandLine cl = parser.parse(options, new String[]{}, props);
        assertTrue(cl.hasOption("f"));
        assertEquals("config.xml", cl.getOptionValue("f"));
    }

    // Tests processProperties with boolean flags (true, yes, 1 vs false)
    @Test
    public void testParse_propertiesBooleanFlags_setsCorrectly() throws Exception
    {
        options.addOption("a", false, "flag A");
        options.addOption("b", false, "flag B");
        options.addOption("c", false, "flag C");
        options.addOption("d", false, "flag D");

        Properties props = new Properties();
        props.setProperty("a", "true");
        props.setProperty("b", "yes");
        props.setProperty("c", "1");
        props.setProperty("d", "false");

        CommandLine cl = parser.parse(options, new String[]{}, props);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertTrue(cl.hasOption("c"));
        assertFalse(cl.hasOption("d"));
    }

    // Tests processProperties does not overwrite command line options
    @Test
    public void testParse_propertiesDoNotOverrideCommandLine() throws Exception
    {
        options.addOption("f", true, "file option");

        Properties props = new Properties();
        props.setProperty("f", "propValue");

        CommandLine cl = parser.parse(options, new String[]{"-f", "cliValue"}, props);
        assertTrue(cl.hasOption("f"));
        assertEquals("cliValue", cl.getOptionValue("f"));
    }

    // Tests processProperties with OptionGroup already selected on command line
    @Test
    public void testParse_propertiesOptionGroupAlreadySelected_ignoredOrHandled() throws Exception
    {
        Option optA = new Option("a", "optA", false, "option A");
        Option optB = new Option("b", "optB", false, "option B");
        OptionGroup group = new OptionGroup();
        group.addOption(optA);
        group.addOption(optB);
        options.addOptionGroup(group);

        Properties props = new Properties();
        props.setProperty("b", "true");

        CommandLine cl = parser.parse(options, new String[]{"-a"}, props);
        assertTrue(cl.hasOption("a"));
    }
}