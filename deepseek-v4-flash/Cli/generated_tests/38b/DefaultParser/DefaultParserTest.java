package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Properties;

public class DefaultParserTest {

    private Options createBasicOptions() {
        Options options = new Options();
        options.addOption("a", "all", false, "list all");
        options.addOption("o", "output", true, "output file");
        options.addOption("v", null, false, "verbose");
        options.addOption("D", null, true, "define property");
        return options;
    }

    // Test simple short option without argument
    @Test
    public void testParse_simpleShortOption_returnsOptionAdded() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = createBasicOptions();
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
        assertNull(cmd.getOptionValue("a"));
        assertEquals(0, cmd.getArgs().length);
    }

    // Test simple long option
    @Test
    public void testParse_simpleLongOption_returnsOptionAdded() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = createBasicOptions();
        CommandLine cmd = parser.parse(options, new String[]{"--all"});
        assertTrue(cmd.hasOption("all"));
        assertEquals(0, cmd.getArgs().length);
    }

    // Test long option with equal sign value
    @Test
    public void testParse_longOptionWithEqualSign_returnsValue() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = createBasicOptions();
        CommandLine cmd = parser.parse(options, new String[]{"--output=file.txt"});
        assertTrue(cmd.hasOption("output"));
        assertEquals("file.txt", cmd.getOptionValue("output"));
    }

    // Test short option with argument (space separated)
    @Test
    public void testParse_shortOptionWithArg_returnsArgValue() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = createBasicOptions();
        CommandLine cmd = parser.parse(options, new String[]{"-o", "out.txt"});
        assertTrue(cmd.hasOption("o"));
        assertEquals("out.txt", cmd.getOptionValue("o"));
    }

    // Test short option with concatenated argument (-oabc)
    @Test
    public void testParse_shortOptionArgConcatenated_returnsArgValue() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = createBasicOptions();
        CommandLine cmd = parser.parse(options, new String[]{"-oabc"});
        assertTrue(cmd.hasOption("o"));
        assertEquals("abc", cmd.getOptionValue("o"));
        assertEquals(0, cmd.getArgs().length);
    }

    // Test short option missing required argument
    @Test(expected = MissingArgumentException.class)
    public void testParse_shortOptionMissingArg_throwsMissingArgumentException() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = createBasicOptions();
        parser.parse(options, new String[]{"-o"});
    }

    // Test unrecognized option throws exception
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unrecognizedOption_throwsUnrecognizedOptionException() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = createBasicOptions();
        parser.parse(options, new String[]{"-x"});
    }

    // Test unrecognized option with stopAtNonOption -> adds to args
    @Test
    public void testParse_unrecognizedOption_stopAtNonOption_addsToArgs() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = createBasicOptions();
        CommandLine cmd = parser.parse(options, new String[]{"-x", "arg1", "-a"}, true);
        assertEquals(3, cmd.getArgs().length);
        assertEquals("-x", cmd.getArgs()[0]);
        assertEquals("arg1", cmd.getArgs()[1]);
        assertEquals("-a", cmd.getArgs()[2]);
        assertFalse(cmd.hasOption("a"));
    }

    // Test double dash stops parsing
    @Test
    public void testParse_doubleDash_stopsParsing() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = createBasicOptions();
        CommandLine cmd = parser.parse(options, new String[]{"-a", "--", "-o", "out.txt"});
        assertTrue(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-o", cmd.getArgs()[0]);
        assertEquals("out.txt", cmd.getArgs()[1]);
        assertFalse(cmd.hasOption("o"));
    }

    // Test concatenated short options (-av)
    @Test
    public void testParse_concatenatedShortOptions_parsedCorrectly() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = createBasicOptions();
        CommandLine cmd = parser.parse(options, new String[]{"-av"});
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("v"));
        assertEquals(0, cmd.getArgs().length);
    }

    // Test concatenated short options with invalid char
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_concatenatedOptionsInvalidChar_throwsException() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = createBasicOptions();
        parser.parse(options, new String[]{"-az"});
    }

    // Test Java property with equal sign (-Dkey=value)
    @Test
    public void testParse_javaPropertyWithEqual_parsedCorrectly() throws ParseException {
        Option d = new Option("D", "define property");
        d.setArgs(2);
        Options options = new Options();
        options.addOption(d);
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[]{"-Dkey=value"});
        assertTrue(cmd.hasOption("D"));
        String[] values = cmd.getOptionValues("D");
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals("key", values[0]);
        assertEquals("value", values[1]);
    }

    // Test Java property without equal sign (-Dflag)
    @Test
    public void testParse_javaPropertyWithoutEqual_parsedCorrectly() throws ParseException {
        Option d = new Option("D", "define property");
        d.setArgs(2);
        Options options = new Options();
        options.addOption(d);
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[]{"-Dflag"});
        assertTrue(cmd.hasOption("D"));
        String[] values = cmd.getOptionValues("D");
        assertNotNull(values);
        assertEquals(1, values.length);
        assertEquals("flag", values[0]);
    }

    // Test long prefix option (-Xmx512m)
    @Test
    public void testParse_longPrefixOption_parsedCorrectly() throws ParseException {
        Options options = new Options();
        Option xmx = new Option(null, "Xmx", true, "memory");
        options.addOption(xmx);
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[]{"-Xmx512m"});
        assertTrue(cmd.hasOption("Xmx"));
        assertEquals("512m", cmd.getOptionValue("Xmx"));
    }

    // Test ambiguous long option prefix
    @Test(expected = AmbiguousOptionException.class)
    public void testParse_ambiguousOption_throwsAmbiguousOptionException() throws ParseException {
        Options options = new Options();
        options.addOption(new Option(null, "add", false, "add"));
        options.addOption(new Option(null, "all", false, "all"));
        DefaultParser parser = new DefaultParser();
        parser.parse(options, new String[]{"--a"});
    }

    // Test required option missing
    @Test(expected = MissingOptionException.class)
    public void testParse_requiredOptionMissing_throwsMissingOptionException() throws ParseException {
        Options options = new Options();
        Option required = new Option("r", "required", true, "required");
        required.setRequired(true);
        options.addOption(required);
        DefaultParser parser = new DefaultParser();
        parser.parse(options, new String[]{});
    }

    // Test required option group missing
    @Test(expected = MissingOptionException.class)
    public void testParse_requiredOptionGroupMissing_throwsMissingOptionException() throws ParseException {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "a", false, "a"));
        group.addOption(new Option("b", "b", false, "b"));
        options.addOptionGroup(group);
        DefaultParser parser = new DefaultParser();
        parser.parse(options, new String[]{});
    }

    // Test option group conflict
    @Test(expected = AlreadySelectedException.class)
    public void testParse_optionGroupConflict_throwsAlreadySelectedException() throws ParseException {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "a", false, "a"));
        group.addOption(new Option("b", "b", false, "b"));
        options.addOptionGroup(group);
        DefaultParser parser = new DefaultParser();
        parser.parse(options, new String[]{"-a", "-b"});
    }

    // Test handleProperties adds default options (no arg, value "true")
    @Test
    public void testParse_handleProperties_optionNoArgValueYes_addsOption() throws ParseException {
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose");
        Properties prop = new Properties();
        prop.setProperty("v", "true");
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[]{}, prop);
        assertTrue(cmd.hasOption("v"));
    }

    // Test handleProperties adds value for option with arg
    @Test
    public void testParse_handleProperties_optionWithArg_addsValue() throws ParseException {
        Options options = new Options();
        Option o = new Option("o", "output", true, "output");
        options.addOption(o);
        Properties prop = new Properties();
        prop.setProperty("o", "propfile.txt");
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[]{}, prop);
        assertTrue(cmd.hasOption("o"));
        assertEquals("propfile.txt", cmd.getOptionValue("o"));
    }

    // Test plain argument added to args
    @Test
    public void testParse_plainArgument_addedToArgs() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = createBasicOptions();
        CommandLine cmd = parser.parse(options, new String[]{"-a", "plainarg"});
        assertTrue(cmd.hasOption("a"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("plainarg", cmd.getArgs()[0]);
    }

    // Test negative number is not recognized as option (should throw)
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_negativeNumber_throwsUnrecognizedOptionException() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = createBasicOptions();
        parser.parse(options, new String[]{"-1"});
    }
}