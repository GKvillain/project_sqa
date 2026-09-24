package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class DefaultParserTest {

    private DefaultParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new DefaultParser();
        options = new Options();
    }

    // Tests parsing short option without argument
    @Test
    public void testParse_simpleShortOption_parsedSuccessfully() throws Exception {
        options.addOption("a", false, "option a");
        CommandLine cl = parser.parse(options, new String[]{"-a"});
        assertTrue(cl.hasOption("a"));
    }

    // Tests parsing short option with separate argument
    @Test
    public void testParse_shortOptionWithArgument_returnsValue() throws Exception {
        options.addOption(OptionBuilder.hasArg().create('a'));
        CommandLine cl = parser.parse(options, new String[]{"-a", "foo"});
        assertTrue(cl.hasOption("a"));
        assertEquals("foo", cl.getOptionValue("a"));
    }

    // Tests parsing long option without equal sign
    @Test
    public void testParse_longOption_parsedSuccessfully() throws Exception {
        options.addOption(OptionBuilder.withLongOpt("foo").hasArg().create());
        CommandLine cl = parser.parse(options, new String[]{"--foo", "bar"});
        assertTrue(cl.hasOption("foo"));
        assertEquals("bar", cl.getOptionValue("foo"));
    }

    // Tests parsing long option with equal sign (--opt=val)
    @Test
    public void testParse_longOptionWithEqual_returnsValue() throws Exception {
        options.addOption(OptionBuilder.withLongOpt("foo").hasArg().create());
        CommandLine cl = parser.parse(options, new String[]{"--foo=bar"});
        assertTrue(cl.hasOption("foo"));
        assertEquals("bar", cl.getOptionValue("foo"));
    }

    // Tests concatenated short options with trailing argument (Defects4J Cli-38 related)
    @Test
    public void testParse_concatenatedShortOptions_handlesCorrectly() throws Exception {
        options.addOption("t1", false, "option t1");
        options.addOption(OptionBuilder.hasArg().create('b'));
        CommandLine cl = parser.parse(options, new String[]{"-t1"});
        assertTrue(cl.hasOption("t1"));
    }

    // Tests concatenated short options (-ab where both are flags)
    @Test
    public void testParse_concatenatedFlags_parsedSuccessfully() throws Exception {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        CommandLine cl = parser.parse(options, new String[]{"-ab"});
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
    }

    // Tests negative number as argument rather than an unrecognized option
    @Test
    public void testParse_negativeNumberArgument_parsedAsArg() throws Exception {
        options.addOption(OptionBuilder.hasArg().create('n'));
        CommandLine cl = parser.parse(options, new String[]{"-n", "-42.5"});
        assertTrue(cl.hasOption("n"));
        assertEquals("-42.5", cl.getOptionValue("n"));
    }

    // Tests double hyphen '--' stops further option parsing
    @Test
    public void testParse_doubleHyphenToken_skipsParsingRemainingTokens() throws Exception {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        CommandLine cl = parser.parse(options, new String[]{"-a", "--", "-b", "extra"});
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-b", cl.getArgs()[0]);
        assertEquals("extra", cl.getArgs()[1]);
    }

    // Tests unrecognized option when stopAtNonOption is false throws exception
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unrecognizedOption_throwsException() throws Exception {
        parser.parse(options, new String[]{"-unknown"});
    }

    // Tests unrecognized option when stopAtNonOption is true stops parsing
    @Test
    public void testParse_stopAtNonOption_addsRemainingToArgs() throws Exception {
        options.addOption("a", false, "option a");
        CommandLine cl = parser.parse(options, new String[]{"-a", "-unknown", "extra"}, true);
        assertTrue(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-unknown", cl.getArgs()[0]);
        assertEquals("extra", cl.getArgs()[1]);
    }

    // Tests missing required option throws exception
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOption_throwsException() throws Exception {
        Option opt = OptionBuilder.isRequired().create('r');
        options.addOption(opt);
        parser.parse(options, new String[]{});
    }

    // Tests missing required argument for an option throws exception
    @Test(expected = MissingArgumentException.class)
    public void testParse_missingRequiredArgument_throwsException() throws Exception {
        options.addOption(OptionBuilder.hasArg().isRequired().create('a'));
        parser.parse(options, new String[]{"-a"});
    }

    // Tests ambiguous partial long option throws exception
    @Test(expected = AmbiguousOptionException.class)
    public void testParse_ambiguousOption_throwsException() throws Exception {
        options.addOption(OptionBuilder.withLongOpt("option1").create());
        options.addOption(OptionBuilder.withLongOpt("option2").create());
        parser.parse(options, new String[]{"--opt"});
    }

    // Tests Java-like property option handling (-Dkey=value)
    @Test
    public void testParse_javaPropertyOption_parsedSuccessfully() throws Exception {
        Option opt = OptionBuilder.withValueSeparator('=').hasArgs(2).create('D');
        options.addOption(opt);
        CommandLine cl = parser.parse(options, new String[]{"-Dkey=value"});
        assertTrue(cl.hasOption("D"));
        assertEquals("key", cl.getOptionValues("D")[0]);
        assertEquals("value", cl.getOptionValues("D")[1]);
    }

    // Tests long prefix with attached argument (-Xmx512m)
    @Test
    public void testParse_longPrefixWithArgument_parsedSuccessfully() throws Exception {
        Option opt = OptionBuilder.withLongOpt("Xmx").hasArg().create();
        options.addOption(opt);
        CommandLine cl = parser.parse(options, new String[]{"-Xmx512m"});
        assertTrue(cl.hasOption("Xmx"));
        assertEquals("512m", cl.getOptionValue("Xmx"));
    }

    // Tests OptionGroup handling and mutual exclusivity
    @Test(expected = AlreadySelectedException.class)
    public void testParse_optionGroupMultipleSelected_throwsException() throws Exception {
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.create('a'));
        group.addOption(OptionBuilder.create('b'));
        options.addOptionGroup(group);
        parser.parse(options, new String[]{"-a", "-b"});
    }

    // Tests properties providing default values for options
    @Test
    public void testParse_withProperties_setsDefaultValues() throws Exception {
        options.addOption(OptionBuilder.hasArg().create("param"));
        options.addOption(OptionBuilder.create("flag"));

        Properties props = new Properties();
        props.setProperty("param", "defaultVal");
        props.setProperty("flag", "true");

        CommandLine cl = parser.parse(options, new String[]{}, props);
        assertTrue(cl.hasOption("param"));
        assertEquals("defaultVal", cl.getOptionValue("param"));
        assertTrue(cl.hasOption("flag"));
    }

    // Tests properties containing undefined option throws exception
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_withUndefinedProperty_throwsException() throws Exception {
        Properties props = new Properties();
        props.setProperty("undefined", "val");
        parser.parse(options, new String[]{}, props);
    }
}