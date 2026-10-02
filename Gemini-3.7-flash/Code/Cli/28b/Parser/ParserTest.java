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
            return arguments != null ? arguments : new String[0];
        }
    }

    @Before
    public void setUp()
    {
        parser = new DummyParser();
        options = new Options();
    }

    // Tests parsing simple option without arguments
    @Test
    public void testParse_simpleOption_success() throws Exception
    {
        options.addOption("a", "all", false, "toggle all");
        String[] args = new String[]{"-a"};

        CommandLine cl = parser.parse(options, args);

        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("all"));
    }

    // Tests parsing option with single argument
    @Test
    public void testParse_optionWithArg_success() throws Exception
    {
        options.addOption("f", "file", true, "specify file");
        String[] args = new String[]{"-f", "test.txt"};

        CommandLine cl = parser.parse(options, args);

        assertTrue(cl.hasOption("f"));
        assertEquals("test.txt", cl.getOptionValue("f"));
    }

    // Tests missing required argument triggers MissingArgumentException
    @Test(expected = MissingArgumentException.class)
    public void testParse_missingArgument_throwsException() throws Exception
    {
        options.addOption("f", "file", true, "specify file");
        String[] args = new String[]{"-f"};

        parser.parse(options, args);
    }

    // Tests optional argument when value is omitted
    @Test
    public void testParse_optionalArgumentOmitted_success() throws Exception
    {
        Option opt = new Option("f", "file", true, "specify file");
        opt.setOptionalArg(true);
        options.addOption(opt);
        String[] args = new String[]{"-f"};

        CommandLine cl = parser.parse(options, args);

        assertTrue(cl.hasOption("f"));
        assertNull(cl.getOptionValue("f"));
    }

    // Tests missing required option triggers MissingOptionException
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOption_throwsException() throws Exception
    {
        Option opt = new Option("r", "require", false, "required option");
        opt.setRequired(true);
        options.addOption(opt);
        String[] args = new String[]{};

        parser.parse(options, args);
    }

    // Tests required option satisfied
    @Test
    public void testParse_requiredOptionProvided_success() throws Exception
    {
        Option opt = new Option("r", "require", false, "required option");
        opt.setRequired(true);
        options.addOption(opt);
        String[] args = new String[]{"-r"};

        CommandLine cl = parser.parse(options, args);

        assertTrue(cl.hasOption("r"));
    }

    // Tests unrecognized option triggers UnrecognizedOptionException
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unrecognizedOption_throwsException() throws Exception
    {
        options.addOption("a", false, "alpha");
        String[] args = new String[]{"-b"};

        parser.parse(options, args);
    }

    // Tests unrecognized option with stopAtNonOption enabled stops parsing
    @Test
    public void testParse_unrecognizedOptionWithStopAtNonOption_stopsParsing() throws Exception
    {
        options.addOption("a", false, "alpha");
        String[] args = new String[]{"-a", "-b", "extra"};

        CommandLine cl = parser.parse(options, args, true);

        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-b", cl.getArgs()[0]);
        assertEquals("extra", cl.getArgs()[1]);
    }

    // Tests double dash stops parsing and collects remaining arguments
    @Test
    public void testParse_doubleDash_eatsRemainingArguments() throws Exception
    {
        options.addOption("a", false, "alpha");
        options.addOption("b", false, "beta");
        String[] args = new String[]{"-a", "--", "-b", "arg1"};

        CommandLine cl = parser.parse(options, args);

        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-b", cl.getArgs()[0]);
        assertEquals("arg1", cl.getArgs()[1]);
    }

    // Tests single dash as argument
    @Test
    public void testParse_singleDash_handledCorrectly() throws Exception
    {
        options.addOption("a", false, "alpha");
        String[] args = new String[]{"-a", "-"};

        CommandLine cl = parser.parse(options, args);

        assertTrue(cl.hasOption("a"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("-", cl.getArgs()[0]);
    }

    // Tests single dash with stopAtNonOption stops parsing
    @Test
    public void testParse_singleDashWithStopAtNonOption_stopsParsing() throws Exception
    {
        options.addOption("a", false, "alpha");
        String[] args = new String[]{"-", "-a"};

        CommandLine cl = parser.parse(options, args, true);

        assertFalse(cl.hasOption("a"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("-a", cl.getArgs()[0]);
    }

    // Tests null arguments array handles gracefully
    @Test
    public void testParse_nullArguments_returnsEmptyCommandLine() throws Exception
    {
        CommandLine cl = parser.parse(options, (String[]) null);

        assertNotNull(cl);
        assertEquals(0, cl.getArgs().length);
    }

    // Tests OptionGroup handling and required OptionGroup
    @Test
    public void testParse_optionGroupRequired_success() throws Exception
    {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "alpha", false, "option A"));
        group.addOption(new Option("b", "beta", false, "option B"));
        options.addOptionGroup(group);

        String[] args = new String[]{"-b"};
        CommandLine cl = parser.parse(options, args);

        assertTrue(cl.hasOption("b"));
        assertFalse(cl.hasOption("a"));
        assertEquals("b", group.getSelected());
    }

    // Tests missing required OptionGroup triggers MissingOptionException
    @Test(expected = MissingOptionException.class)
    public void testParse_missingOptionGroup_throwsException() throws Exception
    {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "alpha", false, "option A"));
        group.addOption(new Option("b", "beta", false, "option B"));
        options.addOptionGroup(group);

        String[] args = new String[]{};
        parser.parse(options, args);
    }

    // Tests property values applied to options with arguments
    @Test
    public void testProcessProperties_optionWithArg_setsValue() throws Exception
    {
        options.addOption("f", "file", true, "specify file");
        Properties props = new Properties();
        props.setProperty("f", "config.xml");

        CommandLine cl = parser.parse(options, new String[]{}, props);

        assertTrue(cl.hasOption("f"));
        assertEquals("config.xml", cl.getOptionValue("f"));
    }

    // Tests property boolean flag processing and continues loop for subsequent properties
    @Test
    public void testProcessProperties_multiplePropertiesWithFalseAndTrue_processesAll() throws Exception
    {
        options.addOption("a", false, "toggle a");
        options.addOption("b", false, "toggle b");
        options.addOption("c", false, "toggle c");

        Properties props = new Properties();
        props.setProperty("a", "false");
        props.setProperty("b", "true");
        props.setProperty("c", "yes");

        CommandLine cl = parser.parse(options, new String[]{}, props);

        assertFalse(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertTrue(cl.hasOption("c"));
    }

    // Tests null properties does not throw exception
    @Test
    public void testProcessProperties_nullProperties_noException() throws Exception
    {
        options.addOption("a", false, "toggle a");
        CommandLine cl = parser.parse(options, new String[]{"-a"}, null);

        assertTrue(cl.hasOption("a"));
    }

    // Tests option with multiple arguments processed successfully
    @Test
    public void testParse_multipleArgs_success() throws Exception
    {
        Option opt = new Option("m", "multi", true, "multiple args");
        opt.setArgs(2);
        options.addOption(opt);
        String[] args = new String[]{"-m", "val1", "val2"};

        CommandLine cl = parser.parse(options, args);

        assertTrue(cl.hasOption("m"));
        assertArrayEquals(new String[]{"val1", "val2"}, cl.getOptionValues("m"));
    }

    // Tests option with multiple arguments stops consuming when next option is encountered
    @Test
    public void testParse_multipleArgsStoppedByNextOption() throws Exception
    {
        Option opt = new Option("m", "multi", true, "multiple args");
        opt.setArgs(2);
        options.addOption(opt);
        options.addOption("a", "all", false, "toggle all");
        String[] args = new String[]{"-m", "val1", "-a"};

        CommandLine cl = parser.parse(options, args);

        assertTrue(cl.hasOption("m"));
        assertArrayEquals(new String[]{"val1"}, cl.getOptionValues("m"));
        assertTrue(cl.hasOption("a"));
    }

    // Tests optional argument provided with value
    @Test
    public void testParse_optionalArgumentProvided_success() throws Exception
    {
        Option opt = new Option("f", "file", true, "specify file");
        opt.setOptionalArg(true);
        options.addOption(opt);
        String[] args = new String[]{"-f", "value.txt"};

        CommandLine cl = parser.parse(options, args);

        assertTrue(cl.hasOption("f"));
        assertEquals("value.txt", cl.getOptionValue("f"));
    }

    // Tests optional argument not consuming following option as its value
    @Test
    public void testParse_optionalArgumentFollowedByOption_doesNotConsumeOption() throws Exception
    {
        Option opt = new Option("f", "file", true, "specify file");
        opt.setOptionalArg(true);
        options.addOption(opt);
        options.addOption("a", "all", false, "toggle all");
        String[] args = new String[]{"-f", "-a"};

        CommandLine cl = parser.parse(options, args);

        assertTrue(cl.hasOption("f"));
        assertNull(cl.getOptionValue("f"));
        assertTrue(cl.hasOption("a"));
    }

    // Tests selecting multiple options from same OptionGroup triggers AlreadySelectedException
    @Test(expected = AlreadySelectedException.class)
    public void testParse_optionGroupMultipleOptions_throwsAlreadySelectedException() throws Exception
    {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha", false, "option A"));
        group.addOption(new Option("b", "beta", false, "option B"));
        options.addOptionGroup(group);

        String[] args = new String[]{"-a", "-b"};
        parser.parse(options, args);
    }

    // Tests non-option arguments interspersed when stopAtNonOption is false
    @Test
    public void testParse_nonOptionArgsWithStopAtNonOptionFalse() throws Exception
    {
        options.addOption("a", "all", false, "toggle all");
        String[] args = new String[]{"arg1", "-a", "arg2"};

        CommandLine cl = parser.parse(options, args, false);

        assertTrue(cl.hasOption("a"));
        assertArrayEquals(new String[]{"arg1", "arg2"}, cl.getArgs());
    }

    // Tests property for unknown option key is ignored
    @Test
    public void testProcessProperties_unknownProperty_ignored() throws Exception
    {
        Properties props = new Properties();
        props.setProperty("unknown", "someValue");

        CommandLine cl = parser.parse(options, new String[]{}, props);

        assertFalse(cl.hasOption("unknown"));
    }

    // Tests property value ignored when option was already provided via command line args
    @Test
    public void testProcessProperties_optionAlreadySetOnCommandLine_propertyIgnored() throws Exception
    {
        options.addOption("f", "file", true, "specify file");
        Properties props = new Properties();
        props.setProperty("f", "from-props.txt");

        CommandLine cl = parser.parse(options, new String[]{"-f", "from-cli.txt"}, props);

        assertTrue(cl.hasOption("f"));
        assertEquals("from-cli.txt", cl.getOptionValue("f"));
    }

    // Tests property with value "1" sets boolean flag
    @Test
    public void testProcessProperties_numericOneFlag_processed() throws Exception
    {
        options.addOption("b", false, "boolean flag");
        Properties props = new Properties();
        props.setProperty("b", "1");

        CommandLine cl = parser.parse(options, new String[]{}, props);

        assertTrue(cl.hasOption("b"));
    }

    // Tests required option requirement satisfied via property
    @Test
    public void testProcessProperties_requiredOptionSatisfiedByProperty() throws Exception
    {
        Option opt = new Option("r", "require", true, "required option");
        opt.setRequired(true);
        options.addOption(opt);

        Properties props = new Properties();
        props.setProperty("r", "req-val");

        CommandLine cl = parser.parse(options, new String[]{}, props);

        assertTrue(cl.hasOption("r"));
        assertEquals("req-val", cl.getOptionValue("r"));
    }

    // Tests required OptionGroup requirement satisfied via property
    @Test
    public void testProcessProperties_requiredOptionGroupSatisfiedByProperty() throws Exception
    {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "alpha", true, "option A"));
        group.addOption(new Option("b", "beta", true, "option B"));
        options.addOptionGroup(group);

        Properties props = new Properties();
        props.setProperty("a", "valA");

        CommandLine cl = parser.parse(options, new String[]{}, props);

        assertTrue(cl.hasOption("a"));
        assertEquals("valA", cl.getOptionValue("a"));
        assertEquals("a", group.getSelected());
    }
}