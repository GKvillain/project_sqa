package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    // Tests parsing simple short option
    @Test
    public void testParse_simpleShortOption_parsedSuccessfully() throws Exception
    {
        options.addOption("a", "enable-a", false, "turn on a");
        CommandLine cmd = parser.parse(options, new String[]{"-a"});

        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("enable-a"));
        assertEquals(0, cmd.getArgs().length);
    }

    // Tests parsing long option without equals sign
    @Test
    public void testParse_longOptionWithoutEquals_parsedSuccessfully() throws Exception
    {
        options.addOption("b", "beta", true, "beta option");
        CommandLine cmd = parser.parse(options, new String[]{"--beta", "value1"});

        assertTrue(cmd.hasOption("beta"));
        assertEquals("value1", cmd.getOptionValue("beta"));
    }

    // Tests parsing long option with equals sign
    @Test
    public void testParse_longOptionWithEquals_parsedSuccessfully() throws Exception
    {
        options.addOption(new Option("c", "config", true, "configuration file"));
        CommandLine cmd = parser.parse(options, new String[]{"--config=app.properties"});

        assertTrue(cmd.hasOption("c"));
        assertEquals("app.properties", cmd.getOptionValue("config"));
    }

    // Tests parsing short option with equals sign
    @Test
    public void testParse_shortOptionWithEquals_parsedSuccessfully() throws Exception
    {
        options.addOption(new Option("f", true, "file"));
        CommandLine cmd = parser.parse(options, new String[]{"-f=test.txt"});

        assertTrue(cmd.hasOption("f"));
        assertEquals("test.txt", cmd.getOptionValue("f"));
    }

    // Tests negative number as argument to an option
    @Test
    public void testParse_negativeNumberArgument_parsedAsValue() throws Exception
    {
        options.addOption(new Option("n", "num", true, "numeric value"));
        CommandLine cmd = parser.parse(options, new String[]{"-n", "-42"});

        assertTrue(cmd.hasOption("n"));
        assertEquals("-42", cmd.getOptionValue("n"));
    }

    // Tests double hyphen '--' stops parsing options and treats subsequent tokens as arguments
    @Test
    public void testParse_doubleHyphen_skipsParsingRemainingTokens() throws Exception
    {
        options.addOption("a", false, "option a");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "--", "-b", "--other"});

        assertTrue(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-b", cmd.getArgs()[0]);
        assertEquals("--other", cmd.getArgs()[1]);
    }

    // Tests stopAtNonOption flag stops parsing when unrecognized token is encountered
    @Test
    public void testParse_stopAtNonOption_stopsAtFirstNonOption() throws Exception
    {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "nonOption", "-b"}, true);

        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("nonOption", cmd.getArgs()[0]);
        assertEquals("-b", cmd.getArgs()[1]);
    }

    // Tests concatenated short options
    @Test
    public void testParse_concatenatedShortOptions_allParsed() throws Exception
    {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        options.addOption("c", true, "option c with arg");
        CommandLine cmd = parser.parse(options, new String[]{"-abcvalue"});

        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
        assertTrue(cmd.hasOption("c"));
        assertEquals("value", cmd.getOptionValue("c"));
    }

    // Tests Java-like property arguments (-Dkey=value)
    @Test
    public void testParse_javaPropertyOption_parsedKeyAndValue() throws Exception
    {
        Option propOption = new Option("D", true, "define property");
        propOption.setArgs(2);
        propOption.setValueSeparator('=');
        options.addOption(propOption);

        CommandLine cmd = parser.parse(options, new String[]{"-Dkey=value"});

        assertTrue(cmd.hasOption("D"));
        String[] values = cmd.getOptionValues("D");
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals("key", values[0]);
        assertEquals("value", values[1]);
    }

    // Tests missing argument triggers MissingArgumentException
    @Test(expected = MissingArgumentException.class)
    public void testParse_missingArgument_throwsException() throws Exception
    {
        options.addOption(new Option("r", "req", true, "requires argument"));
        parser.parse(options, new String[]{"-r"});
    }

    // Tests required option missing triggers MissingOptionException
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOption_throwsException() throws Exception
    {
        Option reqOpt = new Option("r", "required", false, "required option");
        reqOpt.setRequired(true);
        options.addOption(reqOpt);

        parser.parse(options, new String[]{});
    }

    // Tests unrecognized option triggers UnrecognizedOptionException
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unrecognizedOption_throwsException() throws Exception
    {
        options.addOption("a", false, "option a");
        parser.parse(options, new String[]{"--unknown"});
    }

    // Tests ambiguous partial long option triggers AmbiguousOptionException
    @Test(expected = AmbiguousOptionException.class)
    public void testParse_ambiguousOption_throwsException() throws Exception
    {
        options.addOption(new Option(null, "verbose", false, "verbose output"));
        options.addOption(new Option(null, "version", false, "display version"));

        parser.parse(options, new String[]{"--ver"});
    }

    // Tests OptionGroup selection
    @Test
    public void testParse_optionGroup_selectsOnlyOneOption() throws Exception
    {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("f", "file", false, "file source"));
        group.addOption(new Option("d", "dir", false, "dir source"));
        options.addOptionGroup(group);

        CommandLine cmd = parser.parse(options, new String[]{"-f"});

        assertTrue(cmd.hasOption("f"));
        assertFalse(cmd.hasOption("d"));
        assertEquals("f", group.getSelected());
    }

    // Tests OptionGroup selecting multiple mutually exclusive options throws AlreadySelectedException
    @Test(expected = AlreadySelectedException.class)
    public void testParse_optionGroupMultipleSelected_throwsException() throws Exception
    {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("f", "file", false, "file source"));
        group.addOption(new Option("d", "dir", false, "dir source"));
        options.addOptionGroup(group);

        parser.parse(options, new String[]{"-f", "-d"});
    }

    // Tests loading default option values from Properties
    @Test
    public void testParse_propertiesSupport_populatesValues() throws Exception
    {
        options.addOption(new Option("p", "port", true, "port number"));
        options.addOption(new Option("v", "verbose", false, "verbose mode"));

        Properties props = new Properties();
        props.setProperty("port", "8080");
        props.setProperty("verbose", "true");

        CommandLine cmd = parser.parse(options, new String[]{}, props);

        assertTrue(cmd.hasOption("port"));
        assertEquals("8080", cmd.getOptionValue("port"));
        assertTrue(cmd.hasOption("verbose"));
    }

    // Tests unknown option in Properties triggers UnrecognizedOptionException
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unknownProperty_throwsException() throws Exception
    {
        Properties props = new Properties();
        props.setProperty("invalidOption", "value");

        parser.parse(options, new String[]{}, props);
    }

    // Tests short option whose argument begins with a hyphen and matches another short option prefix
    @Test
    public void testParse_shortOptionWithHyphenValue_parsedAsArgument() throws Exception
    {
        options.addOption(new Option("t", true, "target"));
        options.addOption(new Option("l", false, "list"));

        CommandLine cmd = parser.parse(options, new String[]{"-t", "-last"});

        assertTrue(cmd.hasOption("t"));
        assertEquals("-last", cmd.getOptionValue("t"));
        assertFalse(cmd.hasOption("l"));
    }

    // Tests null arguments array handles gracefully
    @Test
    public void testParse_nullArguments_returnsEmptyCommandLine() throws Exception
    {
        CommandLine cmd = parser.parse(options, null);
        assertNotNull(cmd);
        assertEquals(0, cmd.getOptions().length);
        assertEquals(0, cmd.getArgs().length);
    }
}