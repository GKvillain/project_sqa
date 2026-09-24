package org.apache.commons.cli;

import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
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

    // Tests normal short option without argument
    @Test
    public void testParse_shortOptionWithoutArg_success() throws Exception
    {
        options.addOption("a", false, "toggle a");
        CommandLine cl = parser.parse(options, new String[]{"-a"});

        assertTrue(cl.hasOption("a"));
        assertEquals(0, cl.getArgs().length);
    }

    // Tests normal short option with separate argument
    @Test
    public void testParse_shortOptionWithSeparateArg_success() throws Exception
    {
        options.addOption("f", true, "file path");
        CommandLine cl = parser.parse(options, new String[]{"-f", "test.txt"});

        assertTrue(cl.hasOption("f"));
        assertEquals("test.txt", cl.getOptionValue("f"));
    }

    // Tests short option with equal sign value (-f=value)
    @Test
    public void testParse_shortOptionWithEqual_success() throws Exception
    {
        options.addOption("f", true, "file path");
        CommandLine cl = parser.parse(options, new String[]{"-f=myFile.txt"});

        assertTrue(cl.hasOption("f"));
        assertEquals("myFile.txt", cl.getOptionValue("f"));
    }

    // Tests normal long option with and without argument
    @Test
    public void testParse_longOptionWithSeparateAndEqualArg_success() throws Exception
    {
        options.addOption(new Option("f", "file", true, "file path"));
        options.addOption(new Option("v", "verbose", false, "verbose mode"));

        CommandLine cl1 = parser.parse(options, new String[]{"--file", "data.json", "--verbose"});
        assertTrue(cl1.hasOption("file"));
        assertTrue(cl1.hasOption("verbose"));
        assertEquals("data.json", cl1.getOptionValue("file"));

        CommandLine cl2 = parser.parse(options, new String[]{"--file=data2.json"});
        assertTrue(cl2.hasOption("file"));
        assertEquals("data2.json", cl2.getOptionValue("file"));
    }

    // Tests concatenated short options (-abc where c takes an argument)
    @Test
    public void testParse_concatenatedShortOptionsWithArg_success() throws Exception
    {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        options.addOption("c", true, "option c");

        CommandLine cl = parser.parse(options, new String[]{"-abcvalue"});

        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertTrue(cl.hasOption("c"));
        assertEquals("value", cl.getOptionValue("c"));
    }

    // Tests negative number as argument rather than an unrecognized option
    @Test
    public void testParse_negativeNumberAsArgument_success() throws Exception
    {
        options.addOption("n", true, "numeric value");
        CommandLine cl = parser.parse(options, new String[]{"-n", "-42.5"});

        assertTrue(cl.hasOption("n"));
        assertEquals("-42.5", cl.getOptionValue("n"));
    }

    // Tests double dash token stopping option parsing
    @Test
    public void testParse_doubleDashToken_skipsParsingRemaining() throws Exception
    {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");

        CommandLine cl = parser.parse(options, new String[]{"-a", "--", "-b", "extra"});

        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-b", cl.getArgs()[0]);
        assertEquals("extra", cl.getArgs()[1]);
    }

    // Tests stopAtNonOption behavior
    @Test
    public void testParse_stopAtNonOptionTrue_addsRemainingTokensToArgs() throws Exception
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

    // Tests Java-like property arguments (-Dkey=value)
    @Test
    public void testParse_javaPropertyOption_success() throws Exception
    {
        Option propertyOpt = new Option("D", true, "java property");
        propertyOpt.setArgs(2);
        propertyOpt.setValueSeparator('=');
        options.addOption(propertyOpt);

        CommandLine cl = parser.parse(options, new String[]{"-Dkey=value"});

        assertTrue(cl.hasOption("D"));
        String[] values = cl.getOptionValues("D");
        assertEquals(2, values.length);
        assertEquals("key", values[0]);
        assertEquals("value", values[1]);
    }

    // Tests long option prefix matching (-Xmx512m)
    @Test
    public void testParse_longPrefixOption_success() throws Exception
    {
        options.addOption(new Option("X", "Xmx", true, "max memory"));

        CommandLine cl = parser.parse(options, new String[]{"-Xmx512m"});

        assertTrue(cl.hasOption("Xmx"));
        assertEquals("512m", cl.getOptionValue("Xmx"));
    }

    // Tests properties processing for boolean flags and value options
    @Test
    public void testParse_withProperties_success() throws Exception
    {
        options.addOption("a", false, "flag a");
        options.addOption("b", false, "flag b");
        options.addOption("f", true, "file path");

        Properties props = new Properties();
        props.setProperty("a", "true");
        props.setProperty("b", "no");
        props.setProperty("f", "default.txt");

        CommandLine cl = parser.parse(options, new String[]{}, props);

        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertTrue(cl.hasOption("f"));
        assertEquals("default.txt", cl.getOptionValue("f"));
    }

    // Tests properties not overriding already supplied CLI arguments
    @Test
    public void testParse_cliArgumentOverridesProperty_success() throws Exception
    {
        options.addOption("f", true, "file path");

        Properties props = new Properties();
        props.setProperty("f", "propValue.txt");

        CommandLine cl = parser.parse(options, new String[]{"-f", "cliValue.txt"}, props);

        assertTrue(cl.hasOption("f"));
        assertEquals("cliValue.txt", cl.getOptionValue("f"));
    }

    // Tests unrecognized option exception when stopAtNonOption is false
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unrecognizedOption_throwsException() throws Exception
    {
        options.addOption("a", false, "option a");
        parser.parse(options, new String[]{"-z"});
    }

    // Tests ambiguous long option prefix exception
    @Test(expected = AmbiguousOptionException.class)
    public void testParse_ambiguousOption_throwsException() throws Exception
    {
        options.addOption(new Option(null, "verbose", false, "verbose mode"));
        options.addOption(new Option(null, "version", false, "version info"));

        parser.parse(options, new String[]{"--ver"});
    }

    // Tests missing required argument exception
    @Test(expected = MissingArgumentException.class)
    public void testParse_missingRequiredArgument_throwsException() throws Exception
    {
        options.addOption("f", true, "file path");
        parser.parse(options, new String[]{"-f"});
    }

    // Tests missing required option exception
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOption_throwsException() throws Exception
    {
        Option req = new Option("r", false, "required option");
        req.setRequired(true);
        options.addOption(req);

        parser.parse(options, new String[]{});
    }

    // Tests OptionGroup selecting multiple options exception
    @Test(expected = AlreadySelectedException.class)
    public void testParse_optionGroupMultipleSelected_throwsException() throws Exception
    {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", false, "option a"));
        group.addOption(new Option("b", false, "option b"));
        options.addOptionGroup(group);

        parser.parse(options, new String[]{"-a", "-b"});
    }

    // Tests null arguments array handling
    @Test
    public void testParse_nullArguments_returnsEmptyCommandLine() throws Exception
    {
        CommandLine cl = parser.parse(options, null);

        assertNotNull(cl);
        assertEquals(0, cl.getOptions().length);
        assertEquals(0, cl.getArgs().length);
    }

    // Tests single hyphen token handled as a non-option argument
    @Test
    public void testParse_singleHyphenToken_treatedAsArg() throws Exception
    {
        options.addOption("a", false, "option a");
        CommandLine cl = parser.parse(options, new String[]{"-a", "-", "file.txt"});

        assertTrue(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-", cl.getArgs()[0]);
        assertEquals("file.txt", cl.getArgs()[1]);
    }

    // Tests optional argument when provided vs omitted
    @Test
    public void testParse_optionalArgument_success() throws Exception
    {
        Option opt = new Option("f", "file", true, "optional file");
        opt.setOptionalArg(true);
        options.addOption(opt);

        CommandLine clWithArg = parser.parse(options, new String[]{"-f", "custom.txt"});
        assertTrue(clWithArg.hasOption("f"));
        assertEquals("custom.txt", clWithArg.getOptionValue("f"));

        CommandLine clWithoutArg = parser.parse(options, new String[]{"-f"});
        assertTrue(clWithoutArg.hasOption("f"));
        assertNull(clWithoutArg.getOptionValue("f"));
    }

    // Tests unlimited multiple arguments option
    @Test
    public void testParse_multipleArgumentsUnlimited_success() throws Exception
    {
        Option opt = new Option("m", "multi", true, "multiple values");
        opt.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(opt);
        options.addOption("a", false, "flag a");

        CommandLine cl = parser.parse(options, new String[]{"-m", "val1", "val2", "val3", "-a"});
        assertTrue(cl.hasOption("m"));
        assertTrue(cl.hasOption("a"));
        String[] values = cl.getOptionValues("m");
        assertEquals(3, values.length);
        assertEquals("val1", values[0]);
        assertEquals("val2", values[1]);
        assertEquals("val3", values[2]);
    }

    // Tests missing required OptionGroup exception
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOptionGroup_throwsException() throws Exception
    {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", false, "option a"));
        group.addOption(new Option("b", false, "option b"));
        options.addOptionGroup(group);

        parser.parse(options, new String[]{});
    }

    // Tests unrecognized option when stopAtNonOption is true does not throw exception
    @Test
    public void testParse_unrecognizedOptionWithStopAtNonOption_success() throws Exception
    {
        options.addOption("a", false, "option a");
        CommandLine cl = parser.parse(options, new String[]{"-a", "-unrecognized", "extra"}, true);

        assertTrue(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-unrecognized", cl.getArgs()[0]);
        assertEquals("extra", cl.getArgs()[1]);
    }

    // Tests short option with direct attached value (-fvalue)
    @Test
    public void testParse_shortOptionWithDirectAttachedValue_success() throws Exception
    {
        options.addOption("f", true, "file path");
        CommandLine cl = parser.parse(options, new String[]{"-fmyFile.txt"});

        assertTrue(cl.hasOption("f"));
        assertEquals("myFile.txt", cl.getOptionValue("f"));
    }

    // Tests parse with all parameters (options, arguments, properties, stopAtNonOption)
    @Test
    public void testParse_allParameters_success() throws Exception
    {
        options.addOption("a", false, "flag a");
        options.addOption("p", true, "prop opt");
        Properties props = new Properties();
        props.setProperty("p", "propVal");

        CommandLine cl = parser.parse(options, new String[]{"-a", "arg1", "-p", "cliVal"}, props, true);

        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("p"));
        assertEquals(3, cl.getArgs().length);
        assertEquals("arg1", cl.getArgs()[0]);
        assertEquals("-p", cl.getArgs()[1]);
        assertEquals("cliVal", cl.getArgs()[2]);
    }

    // Tests properties selecting option for an OptionGroup
    @Test
    public void testParse_propertiesWithOptionGroup_success() throws Exception
    {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", false, "option a"));
        group.addOption(new Option("b", false, "option b"));
        options.addOptionGroup(group);

        Properties props = new Properties();
        props.setProperty("a", "true");

        CommandLine cl = parser.parse(options, new String[]{}, props);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
    }
}