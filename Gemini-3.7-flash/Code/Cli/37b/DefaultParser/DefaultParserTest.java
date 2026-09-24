package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class DefaultParserTest
{
    private DefaultParser parser;
    private Options options;

    @Before
    public void setUp()
    {
        parser = new DefaultParser();
        options = new Options();
    }

    // Tests parsing simple short option without argument
    @Test
    public void testParse_simpleShortOption_success() throws Exception
    {
        options.addOption("a", false, "option a");
        CommandLine cl = parser.parse(options, new String[]{"-a"});

        assertTrue(cl.hasOption("a"));
        assertEquals(0, cl.getArgs().length);
    }

    // Tests parsing short option with separate argument
    @Test
    public void testParse_shortOptionWithArg_success() throws Exception
    {
        options.addOption("f", true, "file option");
        CommandLine cl = parser.parse(options, new String[]{"-f", "test.txt"});

        assertTrue(cl.hasOption("f"));
        assertEquals("test.txt", cl.getOptionValue("f"));
    }

    // Tests parsing short option with argument attached via equal sign
    @Test
    public void testParse_shortOptionWithEqual_success() throws Exception
    {
        options.addOption("f", true, "file option");
        CommandLine cl = parser.parse(options, new String[]{"-f=test.txt"});

        assertTrue(cl.hasOption("f"));
        assertEquals("test.txt", cl.getOptionValue("f"));
    }

    // Tests parsing long option without equal sign
    @Test
    public void testParse_longOptionWithoutEqual_success() throws Exception
    {
        options.addOption(new Option("f", "file", true, "file option"));
        CommandLine cl = parser.parse(options, new String[]{"--file", "output.txt"});

        assertTrue(cl.hasOption("file"));
        assertEquals("output.txt", cl.getOptionValue("file"));
    }

    // Tests parsing long option with equal sign
    @Test
    public void testParse_longOptionWithEqual_success() throws Exception
    {
        options.addOption(new Option("f", "file", true, "file option"));
        CommandLine cl = parser.parse(options, new String[]{"--file=output.txt"});

        assertTrue(cl.hasOption("file"));
        assertEquals("output.txt", cl.getOptionValue("file"));
    }

    // Tests double hyphen separator stopping option parsing
    @Test
    public void testParse_doubleHyphen_skipsParsingRemainingTokens() throws Exception
    {
        options.addOption("a", false, "option a");
        CommandLine cl = parser.parse(options, new String[]{"-a", "--", "-b", "arg1"});

        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-b", cl.getArgs()[0]);
        assertEquals("arg1", cl.getArgs()[1]);
    }

    // Tests stopAtNonOption flag stops parsing when encountering non-option token
    @Test
    public void testParse_stopAtNonOption_preservesRemainingArgs() throws Exception
    {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        CommandLine cl = parser.parse(options, new String[]{"-a", "nonOption", "-b"}, true);

        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("nonOption", cl.getArgs()[0]);
        assertEquals("-b", cl.getArgs()[1]);
    }

    // Tests unrecognized option throws exception when stopAtNonOption is false
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unrecognizedOption_throwsException() throws Exception
    {
        parser.parse(options, new String[]{"-z"});
    }

    // Tests missing required option throws exception
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOption_throwsException() throws Exception
    {
        Option opt = new Option("r", "required", false, "required option");
        opt.setRequired(true);
        options.addOption(opt);

        parser.parse(options, new String[]{});
    }

    // Tests missing required option argument throws exception
    @Test(expected = MissingArgumentException.class)
    public void testParse_missingRequiredArg_throwsException() throws Exception
    {
        options.addOption("f", true, "file option");
        parser.parse(options, new String[]{"-f"});
    }

    // Tests ambiguous partial long option throws exception
    @Test(expected = AmbiguousOptionException.class)
    public void testParse_ambiguousOption_throwsException() throws Exception
    {
        options.addOption(new Option(null, "verbose", false, "verbose"));
        options.addOption(new Option(null, "version", false, "version"));

        parser.parse(options, new String[]{"--ver"});
    }

    // Tests parsing concatenated short options
    @Test
    public void testParse_concatenatedShortOptions_success() throws Exception
    {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        options.addOption("c", false, "option c");

        CommandLine cl = parser.parse(options, new String[]{"-abc"});

        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertTrue(cl.hasOption("c"));
    }

    // Tests concatenated short options ending with an option requiring argument
    @Test
    public void testParse_concatenatedShortOptionWithArg_success() throws Exception
    {
        options.addOption("a", false, "option a");
        options.addOption("f", true, "file option");

        CommandLine cl = parser.parse(options, new String[]{"-afvalue"});

        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("f"));
        assertEquals("value", cl.getOptionValue("f"));
    }

    // Tests negative number as argument value
    @Test
    public void testParse_negativeNumberArgument_success() throws Exception
    {
        options.addOption("n", true, "number option");
        CommandLine cl = parser.parse(options, new String[]{"-n", "-42"});

        assertTrue(cl.hasOption("n"));
        assertEquals("-42", cl.getOptionValue("n"));
    }

    // Tests Java-like property style options (-Dkey=value)
    @Test
    public void testParse_javaPropertyOption_success() throws Exception
    {
        Option property = new Option("D", true, "defines property");
        property.setArgs(2);
        property.setValueSeparator('=');
        options.addOption(property);

        CommandLine cl = parser.parse(options, new String[]{"-Dkey=value"});

        assertTrue(cl.hasOption("D"));
        String[] values = cl.getOptionValues("D");
        assertEquals(2, values.length);
        assertEquals("key", values[0]);
        assertEquals("value", values[1]);
    }

    // Tests default values from Properties object
    @Test
    public void testParse_propertiesSupport_success() throws Exception
    {
        options.addOption("f", true, "file option");
        options.addOption("v", false, "verbose option");

        Properties props = new Properties();
        props.setProperty("f", "default.txt");
        props.setProperty("v", "true");

        CommandLine cl = parser.parse(options, new String[]{}, props);

        assertTrue(cl.hasOption("f"));
        assertEquals("default.txt", cl.getOptionValue("f"));
        assertTrue(cl.hasOption("v"));
    }

    // Tests properties with undefined option throws exception
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_undefinedPropertyOption_throwsException() throws Exception
    {
        Properties props = new Properties();
        props.setProperty("unknown", "value");

        parser.parse(options, new String[]{}, props);
    }

    // Tests OptionGroup handling
    @Test
    public void testParse_optionGroup_success() throws Exception
    {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha"));
        group.addOption(new Option("b", "beta"));
        options.addOptionGroup(group);

        CommandLine cl = parser.parse(options, new String[]{"-a"});

        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals("a", group.getSelected());
    }

    // Tests OptionGroup selecting second option throws AlreadySelectedException
    @Test(expected = AlreadySelectedException.class)
    public void testParse_optionGroupMultipleSelected_throwsException() throws Exception
    {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha"));
        group.addOption(new Option("b", "beta"));
        options.addOptionGroup(group);

        parser.parse(options, new String[]{"-a", "-b"});
    }

    // Tests null arguments array handling
    @Test
    public void testParse_nullArguments_returnsEmptyCommandLine() throws Exception
    {
        CommandLine cl = parser.parse(options, null);
        assertNotNull(cl);
        assertEquals(0, cl.getArgs().length);
    }

    // Tests short option detection does not treat arbitrary string starting with option char as short option
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_shortOptionWithSecondCharNotOption_throwsException() throws Exception
    {
        options.addOption("t", false, "option t");
        parser.parse(options, new String[]{"-t1"});
    }
}