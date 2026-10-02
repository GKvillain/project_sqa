package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DefaultParserTest {

    private DefaultParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new DefaultParser();
        options = new Options();
    }

    // Tests parsing simple short options without arguments
    @Test
    public void testParse_simpleShortOptions_success() throws Exception {
        options.addOption("a", "alpha", false, "Option a");
        options.addOption("b", "beta", false, "Option b");

        CommandLine cl = parser.parse(options, new String[]{"-a", "-b"});

        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertFalse(cl.hasOption("c"));
    }

    // Tests parsing long options with argument using '=' and separated space
    @Test
    public void testParse_longOptionsWithArgs_success() throws Exception {
        Option optFile = OptionBuilder.hasArg().withLongOpt("file").create('f');
        Option optOut = OptionBuilder.hasArg().withLongOpt("output").create('o');
        options.addOption(optFile);
        options.addOption(optOut);

        CommandLine cl = parser.parse(options, new String[]{"--file=test.txt", "--output", "out.txt"});

        assertTrue(cl.hasOption("file"));
        assertEquals("test.txt", cl.getOptionValue("file"));
        assertTrue(cl.hasOption("output"));
        assertEquals("out.txt", cl.getOptionValue("output"));
    }

    // Tests parsing concatenated short options (-abc)
    @Test
    public void testParse_concatenatedShortOptions_success() throws Exception {
        options.addOption("a", false, "Option A");
        options.addOption("b", false, "Option B");
        options.addOption("c", true, "Option C");

        CommandLine cl = parser.parse(options, new String[]{"-abcvalue"});

        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertTrue(cl.hasOption("c"));
        assertEquals("value", cl.getOptionValue("c"));
    }

    // Tests parsing negative number argument
    @Test
    public void testParse_negativeNumberArgument_success() throws Exception {
        Option numOpt = OptionBuilder.hasArg().create('n');
        options.addOption(numOpt);

        CommandLine cl = parser.parse(options, new String[]{"-n", "-42.5"});

        assertTrue(cl.hasOption("n"));
        assertEquals("-42.5", cl.getOptionValue("n"));
    }

    // Tests Java-like property options (-Dkey=value)
    @Test
    public void testParse_javaPropertyOption_success() throws Exception {
        Option propOpt = OptionBuilder.hasArgs(2).withValueSeparator('=').create('D');
        options.addOption(propOpt);

        CommandLine cl = parser.parse(options, new String[]{"-Dkey=value"});

        assertTrue(cl.hasOption("D"));
        String[] values = cl.getOptionValues("D");
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals("key", values[0]);
        assertEquals("value", values[1]);
    }

    // Tests long option prefix (-Xmx512m)
    @Test
    public void testParse_longOptionPrefix_success() throws Exception {
        Option xmx = OptionBuilder.hasArg().withLongOpt("Xmx").create();
        options.addOption(xmx);

        CommandLine cl = parser.parse(options, new String[]{"-Xmx512m"});

        assertTrue(cl.hasOption("Xmx"));
        assertEquals("512m", cl.getOptionValue("Xmx"));
    }

    // Tests parsing double-dash terminator (--)
    @Test
    public void testParse_doubleDash_stopsParsingOptions() throws Exception {
        options.addOption("a", false, "Option A");
        options.addOption("b", false, "Option B");

        CommandLine cl = parser.parse(options, new String[]{"-a", "--", "-b", "extra"});

        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getArgList().size());
        assertEquals("-b", cl.getArgList().get(0));
        assertEquals("extra", cl.getArgList().get(1));
    }

    // Tests stopAtNonOption behavior when encountering unknown token
    @Test
    public void testParse_stopAtNonOption_leavesRemainingAsArgs() throws Exception {
        options.addOption("a", false, "Option A");
        options.addOption("b", false, "Option B");

        CommandLine cl = parser.parse(options, new String[]{"-a", "nonOption", "-b"}, true);

        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getArgList().size());
        assertEquals("nonOption", cl.getArgList().get(0));
        assertEquals("-b", cl.getArgList().get(1));
    }

    // Tests exception on unrecognized option when stopAtNonOption is false
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unrecognizedOption_throwsException() throws Exception {
        options.addOption("a", false, "Option A");

        parser.parse(options, new String[]{"-z"});
    }

    // Tests exception on missing required option
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOption_throwsException() throws Exception {
        Option reqOpt = OptionBuilder.isRequired().create('r');
        options.addOption(reqOpt);

        parser.parse(options, new String[]{});
    }

    // Tests exception on missing required argument
    @Test(expected = MissingArgumentException.class)
    public void testParse_missingArgument_throwsException() throws Exception {
        Option opt = OptionBuilder.hasArg().isRequired().create('a');
        options.addOption(opt);

        parser.parse(options, new String[]{"-a"});
    }

    // Tests exception when option matching is ambiguous
    @Test(expected = AmbiguousOptionException.class)
    public void testParse_ambiguousLongOption_throwsException() throws Exception {
        options.addOption(OptionBuilder.withLongOpt("test-one").create('1'));
        options.addOption(OptionBuilder.withLongOpt("test-two").create('2'));

        parser.parse(options, new String[]{"--test"});
    }

    // Tests OptionGroup behavior ensuring selected option is recorded
    @Test
    public void testParse_optionGroup_success() throws Exception {
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.create('a'));
        group.addOption(OptionBuilder.create('b'));
        options.addOptionGroup(group);

        CommandLine cl = parser.parse(options, new String[]{"-b"});

        assertFalse(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("b", group.getSelected());
    }

    // Tests exception when multiple options in same OptionGroup are specified
    @Test(expected = AlreadySelectedException.class)
    public void testParse_optionGroupMultipleSelected_throwsException() throws Exception {
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.create('a'));
        group.addOption(OptionBuilder.create('b'));
        options.addOptionGroup(group);

        parser.parse(options, new String[]{"-a", "-b"});
    }

    // Tests default options populated via Properties
    @Test
    public void testParse_propertiesSupport_success() throws Exception {
        Option optWithArg = OptionBuilder.hasArg().create("param");
        Option optFlag = OptionBuilder.create("flag");
        options.addOption(optWithArg);
        options.addOption(optFlag);

        Properties props = new Properties();
        props.setProperty("param", "defaultVal");
        props.setProperty("flag", "true");

        CommandLine cl = parser.parse(options, new String[]{}, props);

        assertTrue(cl.hasOption("param"));
        assertEquals("defaultVal", cl.getOptionValue("param"));
        assertTrue(cl.hasOption("flag"));
    }

    // Tests properties when option is in OptionGroup and another is already selected
    @Test
    public void testParse_propertiesWithSelectedOptionGroup_ignoresGroupProperty() throws Exception {
        Option optA = OptionBuilder.create('a');
        Option optB = OptionBuilder.create('b');
        OptionGroup group = new OptionGroup();
        group.addOption(optA);
        group.addOption(optB);
        options.addOptionGroup(group);

        Properties props = new Properties();
        props.setProperty("b", "true");

        CommandLine cl = parser.parse(options, new String[]{"-a"}, props);

        assertTrue(cl.hasOption("a"));
    }

    // Tests null arguments array handling
    @Test
    public void testParse_nullArguments_success() throws Exception {
        options.addOption("a", false, "Option A");

        CommandLine cl = parser.parse(options, (String[]) null);

        assertNotNull(cl);
        assertFalse(cl.hasOption("a"));
    }

    // Tests short option with equal sign value (-s=val)
    @Test
    public void testParse_shortOptionWithEqual_success() throws Exception {
        Option opt = OptionBuilder.hasArg().create('s');
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[]{"-s=myvalue"});

        assertTrue(cl.hasOption("s"));
        assertEquals("myvalue", cl.getOptionValue("s"));
    }

    // Tests parsing single dash ("-") as an argument
    @Test
    public void testParse_singleHyphen_handledAsArg() throws Exception {
        options.addOption("a", false, "Option A");

        CommandLine cl = parser.parse(options, new String[]{"-a", "-"});

        assertTrue(cl.hasOption("a"));
        assertEquals(1, cl.getArgList().size());
        assertEquals("-", cl.getArgList().get(0));
    }

    // Tests exact match when prefix matches multiple options but one is exact
    @Test
    public void testParse_exactMatchWithPrefixCollisions_success() throws Exception {
        options.addOption(OptionBuilder.withLongOpt("foo").create('f'));
        options.addOption(OptionBuilder.withLongOpt("foobar").create('b'));

        CommandLine cl = parser.parse(options, new String[]{"--foo"});

        assertTrue(cl.hasOption("foo"));
        assertFalse(cl.hasOption("foobar"));
    }

    // Tests optional argument provided vs omitted
    @Test
    public void testParse_optionalArgument_success() throws Exception {
        Option opt = OptionBuilder.hasOptionalArg().withLongOpt("opt").create('o');
        options.addOption(opt);

        CommandLine clWithout = parser.parse(options, new String[]{"--opt"});
        assertTrue(clWithout.hasOption("opt"));
        assertNull(clWithout.getOptionValue("opt"));

        CommandLine clWith = parser.parse(options, new String[]{"--opt=custom"});
        assertTrue(clWith.hasOption("opt"));
        assertEquals("custom", clWith.getOptionValue("opt"));
    }

    // Tests multiple arguments option (unlimited or fixed)
    @Test
    public void testParse_multipleArgumentsOption_success() throws Exception {
        Option multiOpt = OptionBuilder.hasArgs(3).create('m');
        options.addOption(multiOpt);

        CommandLine cl = parser.parse(options, new String[]{"-m", "val1", "val2", "val3"});

        assertTrue(cl.hasOption("m"));
        String[] values = cl.getOptionValues("m");
        assertEquals(3, values.length);
        assertEquals("val1", values[0]);
        assertEquals("val2", values[1]);
        assertEquals("val3", values[2]);
    }

    // Tests missing required OptionGroup
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOptionGroup_throwsException() throws Exception {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(OptionBuilder.create('a'));
        group.addOption(OptionBuilder.create('b'));
        options.addOptionGroup(group);

        parser.parse(options, new String[]{});
    }

    // Tests properties with false/no/0 values for flag option
    @Test
    public void testParse_propertiesWithFalseFlag_doesNotSetOption() throws Exception {
        options.addOption("f", "flag", false, "Flag option");

        Properties props = new Properties();
        props.setProperty("flag", "false");

        CommandLine cl = parser.parse(options, new String[]{}, props);

        assertFalse(cl.hasOption("flag"));
    }

    // Tests stopAtNonOption with concatenated unrecognized short option token
    @Test
    public void testParse_concatenatedUnknownShortOptionWithStopAtNonOption_stopsParsing() throws Exception {
        options.addOption("a", false, "Option A");

        CommandLine cl = parser.parse(options, new String[]{"-azb"}, true);

        assertTrue(cl.hasOption("a"));
        assertEquals(1, cl.getArgList().size());
        assertEquals("zb", cl.getArgList().get(0));
    }

    // Tests single dash long option (e.g. -verbose where verbose is long opt only)
    @Test
    public void testParse_singleDashLongOption_success() throws Exception {
        Option verbose = OptionBuilder.withLongOpt("verbose").create();
        options.addOption(verbose);

        CommandLine cl = parser.parse(options, new String[]{"-verbose"});

        assertTrue(cl.hasOption("verbose"));
    }
}