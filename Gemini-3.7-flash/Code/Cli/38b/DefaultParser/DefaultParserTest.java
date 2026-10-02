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

    // Tests simple short option parsing
    @Test
    public void testParse_simpleShortOption_parsedSuccessfully() throws ParseException
    {
        options.addOption("a", false, "simple flag");
        String[] args = new String[]{"-a"};

        CommandLine cmd = parser.parse(options, args);

        assertTrue(cmd.hasOption("a"));
    }

    // Tests simple long option parsing without equal sign
    @Test
    public void testParse_simpleLongOption_parsedSuccessfully() throws ParseException
    {
        options.addOption(new Option("a", "all", false, "all flag"));
        String[] args = new String[]{"--all"};

        CommandLine cmd = parser.parse(options, args);

        assertTrue(cmd.hasOption("all"));
        assertTrue(cmd.hasOption("a"));
    }

    // Tests short option with separate argument token
    @Test
    public void testParse_shortOptionWithArg_parsedSuccessfully() throws ParseException
    {
        options.addOption("f", true, "file path");
        String[] args = new String[]{"-f", "test.txt"};

        CommandLine cmd = parser.parse(options, args);

        assertTrue(cmd.hasOption("f"));
        assertEquals("test.txt", cmd.getOptionValue("f"));
    }

    // Tests long option with equal sign and argument value
    @Test
    public void testParse_longOptionWithEqual_parsedSuccessfully() throws ParseException
    {
        options.addOption(new Option("f", "file", true, "file path"));
        String[] args = new String[]{"--file=output.log"};

        CommandLine cmd = parser.parse(options, args);

        assertTrue(cmd.hasOption("file"));
        assertEquals("output.log", cmd.getOptionValue("file"));
    }

    // Tests short option with equal sign and argument value
    @Test
    public void testParse_shortOptionWithEqual_parsedSuccessfully() throws ParseException
    {
        options.addOption("s", true, "setting");
        String[] args = new String[]{"-s=value"};

        CommandLine cmd = parser.parse(options, args);

        assertTrue(cmd.hasOption("s"));
        assertEquals("value", cmd.getOptionValue("s"));
    }

    // Tests missing required argument exception path
    @Test(expected = MissingArgumentException.class)
    public void testParse_missingRequiredArgument_throwsException() throws ParseException
    {
        options.addOption("f", true, "file path");
        String[] args = new String[]{"-f"};

        parser.parse(options, args);
    }

    // Tests missing required option exception path
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOption_throwsException() throws ParseException
    {
        Option reqOpt = new Option("r", "required", false, "required option");
        reqOpt.setRequired(true);
        options.addOption(reqOpt);

        parser.parse(options, new String[]{});
    }

    // Tests unrecognized option exception path
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unrecognizedOption_throwsException() throws ParseException
    {
        options.addOption("a", false, "flag");
        String[] args = new String[]{"-u"};

        parser.parse(options, args);
    }

    // Tests stopAtNonOption flag stops parsing on unknown option
    @Test
    public void testParse_stopAtNonOption_addsRemainingArgs() throws ParseException
    {
        options.addOption("a", false, "flag");
        String[] args = new String[]{"-a", "unknown", "--other", "arg2"};

        CommandLine cmd = parser.parse(options, args, true);

        assertTrue(cmd.hasOption("a"));
        assertEquals(3, cmd.getArgs().length);
        assertEquals("unknown", cmd.getArgs()[0]);
        assertEquals("--other", cmd.getArgs()[1]);
        assertEquals("arg2", cmd.getArgs()[2]);
    }

    // Tests double dash token triggers skipping of subsequent options
    @Test
    public void testParse_doubleDash_skipsParsing() throws ParseException
    {
        options.addOption("a", false, "flag");
        options.addOption("b", false, "flag");
        String[] args = new String[]{"-a", "--", "-b"};

        CommandLine cmd = parser.parse(options, args);

        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("-b", cmd.getArgs()[0]);
    }

    // Tests negative numbers are recognized as arguments rather than options
    @Test
    public void testParse_negativeNumberAsArgument_accepted() throws ParseException
    {
        options.addOption("n", true, "number");
        String[] args = new String[]{"-n", "-42.5"};

        CommandLine cmd = parser.parse(options, args);

        assertTrue(cmd.hasOption("n"));
        assertEquals("-42.5", cmd.getOptionValue("n"));
    }

    // Tests properties configuration populates missing options
    @Test
    public void testParse_propertiesWithArgOption_setsValue() throws ParseException
    {
        options.addOption("k", true, "key");
        Properties props = new Properties();
        props.setProperty("k", "propertyValue");

        CommandLine cmd = parser.parse(options, new String[]{}, props);

        assertTrue(cmd.hasOption("k"));
        assertEquals("propertyValue", cmd.getOptionValue("k"));
    }

    // Tests properties configuration for boolean flag
    @Test
    public void testParse_propertiesWithFlagOption_setsFlag() throws ParseException
    {
        options.addOption("v", false, "verbose");
        Properties props = new Properties();
        props.setProperty("v", "true");

        CommandLine cmd = parser.parse(options, new String[]{}, props);

        assertTrue(cmd.hasOption("v"));
    }

    // Tests unrecognized property throws UnrecognizedOptionException
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_propertiesUnrecognized_throwsException() throws ParseException
    {
        Properties props = new Properties();
        props.setProperty("unknownProp", "value");

        parser.parse(options, new String[]{}, props);
    }

    // Tests ambiguous partial long option throws AmbiguousOptionException
    @Test(expected = AmbiguousOptionException.class)
    public void testParse_ambiguousOption_throwsException() throws ParseException
    {
        options.addOption(new Option("a", "action-add", false, "add"));
        options.addOption(new Option("b", "action-all", false, "all"));
        String[] args = new String[]{"--action"};

        parser.parse(options, args);
    }

    // Tests OptionGroup selection and mutual exclusion
    @Test
    public void testParse_optionGroup_setsSelected() throws ParseException
    {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("x", "xml", false, "xml format"));
        group.addOption(new Option("j", "json", false, "json format"));
        options.addOptionGroup(group);

        CommandLine cmd = parser.parse(options, new String[]{"-x"});

        assertTrue(cmd.hasOption("x"));
        assertFalse(cmd.hasOption("j"));
        assertEquals("x", group.getSelected());
    }

    // Tests Java-like property style option (-Dkey=value)
    @Test
    public void testParse_javaPropertyOption_handlesKeyAndValue() throws ParseException
    {
        Option propOption = new Option("D", true, "java property");
        propOption.setArgs(2);
        propOption.setValueSeparator('=');
        options.addOption(propOption);

        CommandLine cmd = parser.parse(options, new String[]{"-Dkey=value"});

        assertTrue(cmd.hasOption("D"));
        assertEquals("key", cmd.getOptionValues("D")[0]);
        assertEquals("value", cmd.getOptionValues("D")[1]);
    }

    // Tests concatenated short options (-abc)
    @Test
    public void testParse_concatenatedShortOptions_parsedSuccessfully() throws ParseException
    {
        options.addOption("a", false, "a flag");
        options.addOption("b", false, "b flag");
        options.addOption("c", false, "c flag");
        String[] args = new String[]{"-abc"};

        CommandLine cmd = parser.parse(options, args);

        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
        assertTrue(cmd.hasOption("c"));
    }

    // Tests concatenated short options with trailing argument value
    @Test
    public void testParse_concatenatedShortOptionsWithArg_parsedSuccessfully() throws ParseException
    {
        options.addOption("a", false, "a flag");
        options.addOption("f", true, "f option");
        String[] args = new String[]{"-afmyFile.txt"};

        CommandLine cmd = parser.parse(options, args);

        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("f"));
        assertEquals("myFile.txt", cmd.getOptionValue("f"));
    }

    // Tests short option requiring argument when followed by another short option token
    @Test(expected = MissingArgumentException.class)
    public void testParse_shortOptionFollowedByConcatenatedShortOption_throwsMissingArg() throws ParseException
    {
        options.addOption("t", true, "target option requiring arg");
        options.addOption("a", false, "a flag");
        options.addOption("b", false, "b flag");
        String[] args = new String[]{"-t", "-ab"};

        parser.parse(options, args);
    }
}