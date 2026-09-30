package org.apache.commons.cli;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class ParserTest {

    private Parser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new Parser() {
            @Override
            protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
                // simple flatten that passes through arguments for testing
                if (arguments == null) {
                    return new String[0];
                }
                return arguments;
            }
        };
        options = new Options();
    }

    // Tests parse with null arguments
    @Test
    public void testParse_nullArguments_returnsEmptyCommandLine() throws ParseException {
        CommandLine cmd = parser.parse(options, (String[]) null);
        assertNotNull(cmd);
        assertEquals(0, cmd.getArgs().length);
    }

    // Tests parse with empty arguments array
    @Test
    public void testParse_emptyArguments_returnsEmptyCommandLine() throws ParseException {
        CommandLine cmd = parser.parse(options, new String[]{});
        assertNotNull(cmd);
        assertEquals(0, cmd.getArgs().length);
    }

    // Tests parse with a simple option argument
    @Test
    public void testParse_simpleOption_returnsCommandLineWithOption() throws ParseException {
        options.addOption("v", "verbose", false, "verbose mode");
        CommandLine cmd = parser.parse(options, new String[]{"-v"});
        assertTrue(cmd.hasOption("v"));
    }

    // Tests parse with an option that has an argument
    @Test
    public void testParse_optionWithArgument_returnsCommandLineWithOptionValue() throws ParseException {
        options.addOption("o", "output", true, "output file");
        CommandLine cmd = parser.parse(options, new String[]{"-o", "file.txt"});
        assertTrue(cmd.hasOption("o"));
        assertEquals("file.txt", cmd.getOptionValue("o"));
    }

    // Tests parse with double dash token stops option processing
    @Test
    public void testParse_doubleDash_stopsOptionProcessing() throws ParseException {
        options.addOption("v", false, "verbose");
        CommandLine cmd = parser.parse(options, new String[]{"-v", "--", "-unknown"});
        assertTrue(cmd.hasOption("v"));
        // remaining args after -- should be treated as arguments
        String[] remainingArgs = cmd.getArgs();
        assertEquals(1, remainingArgs.length);
        assertEquals("-unknown", remainingArgs[0]);
    }

    // Tests parse with single dash treated as argument
    @Test
    public void testParse_singleDash_addedAsArg() throws ParseException {
        CommandLine cmd = parser.parse(options, new String[]{"-"});
        assertEquals(1, cmd.getArgs().length);
        assertEquals("-", cmd.getArgs()[0]);
    }

    // Tests parse with stopAtNonOption flag
    @Test
    public void testParse_stopAtNonOption_stopsAtNonOption() throws ParseException {
        options.addOption("v", false, "verbose");
        CommandLine cmd = parser.parse(options, new String[]{"-v", "nonOption", "-unknown"}, true);
        assertTrue(cmd.hasOption("v"));
        String[] args = cmd.getArgs();
        assertEquals(2, args.length);
        assertEquals("nonOption", args[0]);
        assertEquals("-unknown", args[1]);
    }

    // Tests parse with stopAtNonOption = false continues parsing after non-option
    @Test
    public void testParse_stopAtNonOptionFalse_continuesParsing() throws ParseException {
        options.addOption("v", false, "verbose");
        options.addOption("x", false, "extra");
        CommandLine cmd = parser.parse(options, new String[]{"-v", "nonOption", "-x"}, false);
        assertTrue(cmd.hasOption("v"));
        // -x becomes an unrecognized option
        assertFalse(cmd.hasOption("x"));
        String[] args = cmd.getArgs();
        assertEquals(1, args.length);
        assertEquals("nonOption", args[0]);
    }

    // Tests parse with unrecognized option throws exception
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unrecognizedOption_throwsException() throws ParseException {
        parser.parse(options, new String[]{"-unknown"});
    }

    // Tests parse with required option not provided throws MissingOptionException
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOption_throwsException() throws ParseException {
        options.addOption(OptionBuilder.withLongOpt("required").isRequired().create('r'));
        parser.parse(options, new String[]{});
    }

    // Tests parse with missing argument for option throws MissingArgumentException
    @Test(expected = MissingArgumentException.class)
    public void testParse_missingOptionArgument_throwsException() throws ParseException {
        options.addOption("o", true, "output");
        parser.parse(options, new String[]{"-o"});
    }

    // Tests processProperties adds option from properties
    @Test
    public void testParse_propertiesOptionAdded_valueSet() throws ParseException {
        options.addOption("o", true, "output");
        Properties props = new Properties();
        props.setProperty("o", "out.txt");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("o"));
        assertEquals("out.txt", cmd.getOptionValue("o"));
    }

    // Tests processProperties ignores properties for options without args with non-yes/true/1 values
    @Test
    public void testParse_propertiesBooleanOptionNoFalseValue_optionNotAdded() throws ParseException {
        options.addOption("v", false, "verbose");
        Properties props = new Properties();
        props.setProperty("v", "no");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertFalse(cmd.hasOption("v"));
    }

    // Tests processProperties adds boolean option with "yes" value
    @Test
    public void testParse_propertiesBooleanOptionYesValue_optionAdded() throws ParseException {
        options.addOption("v", false, "verbose");
        Properties props = new Properties();
        props.setProperty("v", "yes");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("v"));
    }

    // Tests parse with option group making one required
    @Test
    public void testParse_optionGroupRequired_oneSelected() throws ParseException {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(OptionBuilder.withLongOpt("opt1").create('a'));
        group.addOption(OptionBuilder.withLongOpt("opt2").create('b'));
        options.addOptionGroup(group);
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
    }

    // Tests parse with option that has optional argument
    @Test
    public void testParse_optionOptionalArg_noArgProvided() throws ParseException {
        Option opt = OptionBuilder.withLongOpt("opt").hasOptionalArg().create('o');
        options.addOption(opt);
        CommandLine cmd = parser.parse(options, new String[]{"-o"});
        assertTrue(cmd.hasOption("o"));
    }

    // Tests parse with token that starts with dash but is not an option and stopAtNonOption is false
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unrecognizedOptionWithoutStop_throwsException() throws ParseException {
        parser.parse(options, new String[]{"-unknown"}, false);
    }

    // Tests parse with single dash and stopAtNonOption true
    @Test
    public void testParse_singleDashStopAtNonOption_addsArg() throws ParseException {
        CommandLine cmd = parser.parse(options, new String[]{"-"}, true);
        assertEquals(1, cmd.getArgs().length);
        assertEquals("-", cmd.getArgs()[0]);
    }

    // === New test cases for uncovered coverage ===

    // Test long option (--verbose)
    @Test
    public void testParse_longOption() throws ParseException {
        options.addOption("v", "verbose", false, "verbose mode");
        CommandLine cmd = parser.parse(options, new String[]{"--verbose"});
        assertTrue(cmd.hasOption("v"));
    }

    // Test long option with argument (--output file.txt)
    @Test
    public void testParse_longOptionWithArgument() throws ParseException {
        options.addOption("o", "output", true, "output file");
        CommandLine cmd = parser.parse(options, new String[]{"--output", "file.txt"});
        assertTrue(cmd.hasOption("o"));
        assertEquals("file.txt", cmd.getOptionValue("o"));
    }

    // Test option group required but no option selected
    @Test(expected = MissingOptionException.class)
    public void testParse_optionGroupRequiredNotSelected() throws ParseException {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(OptionBuilder.create('a'));
        group.addOption(OptionBuilder.create('b'));
        options.addOptionGroup(group);
        parser.parse(options, new String[]{});
    }

    // Test option group with multiple selected options throws AlreadySelectedException
    @Test(expected = AlreadySelectedException.class)
    public void testParse_optionGroupMultipleSelected() throws ParseException {
        OptionGroup group = new OptionGroup();
        group.setRequired(false);
        group.addOption(OptionBuilder.create('a'));
        group.addOption(OptionBuilder.create('b'));
        options.addOptionGroup(group);
        parser.parse(options, new String[]{"-a", "-b"});
    }

    // Test properties with "true" value for boolean option
    @Test
    public void testParse_propertiesBooleanTrueValue() throws ParseException {
        options.addOption("v", false, "verbose");
        Properties props = new Properties();
        props.setProperty("v", "true");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("v"));
    }

    // Test properties with "1" value for boolean option
    @Test
    public void testParse_propertiesBooleanOneValue() throws ParseException {
        options.addOption("v", false, "verbose");
        Properties props = new Properties();
        props.setProperty("v", "1");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("v"));
    }

    // Test properties with undefined option key (should be ignored)
    @Test
    public void testParse_propertiesUndefinedOption() throws ParseException {
        options.addOption("v", false, "verbose");
        Properties props = new Properties();
        props.setProperty("unknown", "value");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertFalse(cmd.hasOption("unknown"));
    }

    // Test properties do not override command line arguments
    @Test
    public void testParse_propertiesNotOverrideCommandLine() throws ParseException {
        options.addOption("o", true, "output");
        Properties props = new Properties();
        props.setProperty("o", "propsValue");
        CommandLine cmd = parser.parse(options, new String[]{"-o", "cliValue"}, props);
        assertEquals("cliValue", cmd.getOptionValue("o"));
    }

    // Test properties with long option key (e.g., "output" instead of "o")
    @Test
    public void testParse_propertiesWithLongOptKey() throws ParseException {
        options.addOption("o", "output", true, "output file");
        Properties props = new Properties();
        props.setProperty("output", "file.txt");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("o"));
        assertEquals("file.txt", cmd.getOptionValue("o"));
    }

    // Test parse with properties and stopAtNonOption=true
    @Test
    public void testParse_propertiesWithStopAtNonOption() throws ParseException {
        options.addOption("v", false, "verbose");
        Properties props = new Properties();
        props.setProperty("v", "true");
        CommandLine cmd = parser.parse(options, new String[]{"nonOption"}, props, true);
        assertTrue(cmd.hasOption("v"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("nonOption", cmd.getArgs()[0]);
    }
}