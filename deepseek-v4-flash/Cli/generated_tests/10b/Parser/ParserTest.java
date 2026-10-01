package org.apache.commons.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for the abstract Parser class.
 * Tests are designed to cover methods and branches of Parser,
 * including parse method overloads, processArgs, processOption,
 * processProperties, and checkRequiredOptions.
 *
 * Uses a concrete test subclass of Parser to instantiate the abstract class.
 */
public class ParserTest {

    private Parser parser;
    private Options options;

    @Before
    public void setUp() {
        // Create a concrete Parser for testing
        parser = new Parser() {
            @Override
            protected String[] flatten(Options opts, String[] arguments,
                                       boolean stopAtNonOption) {
                // Simple flatten implementation that returns arguments as-is
                return arguments;
            }
        };
        options = new Options();
    }

    // Tests parse with no arguments returns empty CommandLine
    @Test
    public void testParse_emptyArguments_returnsEmptyCommandLine() throws Exception {
        CommandLine cmd = parser.parse(options, new String[]{});
        assertTrue(cmd.getArgs().length == 0);
        assertTrue(cmd.getOptions().length == 0);
    }

    // Tests parse with null arguments returns empty CommandLine
    @Test
    public void testParse_nullArguments_returnsEmptyCommandLine() throws Exception {
        CommandLine cmd = parser.parse(options, null);
        assertTrue(cmd.getArgs().length == 0);
        assertTrue(cmd.getOptions().length == 0);
    }

    // Tests parse with a simple option and no argument
    @Test
    public void testParse_simpleOption_recognizesOption() throws Exception {
        Option opt = new Option("a", "desc");
        options.addOption(opt);
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
        assertNull(cmd.getOptionValue("a"));
    }

    // Tests parse with option requiring argument
    @Test
    public void testParse_optionWithArgument_returnsValue() throws Exception {
        Option opt = new Option("b", true, "desc");
        options.addOption(opt);
        CommandLine cmd = parser.parse(options, new String[]{"-b", "value"});
        assertTrue(cmd.hasOption("b"));
        assertEquals("value", cmd.getOptionValue("b"));
    }

    // Tests parse with unrecognized option throws exception
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unrecognizedOption_throwsException() throws Exception {
        parser.parse(options, new String[]{"-x"});
    }

    // Tests parse with property for option
    @Test
    public void testParse_properties_populatesOption() throws Exception {
        Option opt = new Option("c", false, "desc");
        options.addOption(opt);
        Properties props = new Properties();
        props.setProperty("c", "yes");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("c"));
    }

    // Tests parse with property but non-true value does not set option
    @Test
    public void testParse_propertiesNonTrueValue_doesNotSetOption() throws Exception {
        Option opt = new Option("d", false, "desc");
        options.addOption(opt);
        Properties props = new Properties();
        props.setProperty("d", "no");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertFalse(cmd.hasOption("d"));
    }

    // Tests parse with properties and missing required option throws exception
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOption_throwsException() throws Exception {
        Option opt = new Option("e", true, "desc");
        opt.setRequired(true);
        options.addOption(opt);
        parser.parse(options, new String[]{});
    }

    // Tests required option provided by properties
    @Test
    public void testParse_requiredOptionFromProperties_noException() throws Exception {
        Option opt = new Option("f", true, "desc");
        opt.setRequired(true);
        options.addOption(opt);
        Properties props = new Properties();
        props.setProperty("f", "value");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("f"));
        assertEquals("value", cmd.getOptionValue("f"));
    }

    // Tests parse with stopAtNonOption true on non-option argument
    @Test
    public void testParse_stopAtNonOption_addsArg() throws Exception {
        CommandLine cmd = parser.parse(options, new String[]{"arg"}, true);
        assertEquals(1, cmd.getArgs().length);
        assertEquals("arg", cmd.getArgs()[0]);
    }

    // Tests parse with stopAtNonOption false on non-option argument
    @Test
    public void testParse_nonOption_addsArg() throws Exception {
        CommandLine cmd = parser.parse(options, new String[]{"arg"}, false);
        assertEquals(1, cmd.getArgs().length);
        assertEquals("arg", cmd.getArgs()[0]);
    }

    // Tests parse with double dash stops processing
    @Test
    public void testParse_doubleDash_stopsProcessing() throws Exception {
        Option opt = new Option("g", false, "desc");
        options.addOption(opt);
        CommandLine cmd = parser.parse(options, new String[]{"--", "-g", "arg"});
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-g", cmd.getArgs()[0]);
        assertEquals("arg", cmd.getArgs()[1]);
        assertFalse(cmd.hasOption("g"));
    }

    // Tests parse with single dash without stopAtNonOption
    @Test
    public void testParse_singleDash_addsArg() throws Exception {
        CommandLine cmd = parser.parse(options, new String[]{"-"}, false);
        assertEquals(1, cmd.getArgs().length);
        assertEquals("-", cmd.getArgs()[0]);
    }

    // Tests parse with single dash and stopAtNonOption true
    @Test
    public void testParse_singleDash_stopAtNonOption_addsRest() throws Exception {
        CommandLine cmd = parser.parse(options, new String[]{"-", "arg1", "arg2"}, true);
        assertEquals(3, cmd.getArgs().length);
    }

    // Tests processArgs with missing required argument throws exception
    @Test(expected = MissingArgumentException.class)
    public void testProcessArgs_missingRequiredArgument_throwsException() throws Exception {
        Option opt = new Option("h", true, "desc");
        options.addOption(opt);
        java.util.List<String> list = new java.util.ArrayList<String>();
        list.add("-i"); // next token is another option, so no argument
        java.util.ListIterator<String> iter = list.listIterator();
        parser.processArgs(opt, iter);
    }

    // Tests processOption with option group
    @Test
    public void testProcessOption_optionGroup_selectsGroup() throws Exception {
        Option opt1 = new Option("i", false, "desc");
        Option opt2 = new Option("j", false, "desc");
        options.addOption(opt1);
        options.addOption(opt2);
        OptionGroup group = new OptionGroup();
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);
        parser.setOptions(options);
        parser.processOption("-i", java.util.Collections.emptyList().listIterator());
        assertTrue(group.getSelected().equals("i"));
    }

    // Tests checkRequiredOptions with no required options does nothing
    @Test
    public void testCheckRequiredOptions_noRequiredOptions_noException() throws Exception {
        parser.setOptions(options);
        parser.checkRequiredOptions();
    }

    // Tests checkRequiredOptions with required options not present throws exception
    @Test(expected = MissingOptionException.class)
    public void testCheckRequiredOptions_missingRequired_throwsException() throws Exception {
        Option req = new Option("k", false, "desc");
        req.setRequired(true);
        options.addOption(req);
        parser.setOptions(options);
        parser.checkRequiredOptions();
    }

    // Tests processProperties with null properties does nothing
    @Test
    public void testProcessProperties_nullProperties_noException() throws Exception {
        parser.setOptions(options);
        parser.processProperties(null);
    }

    // Tests getRequiredOptions returns list
    @Test
    public void testGetRequiredOptions_emptyOptions_returnsEmptyList() {
        parser.setOptions(options);
        List req = parser.getRequiredOptions();
        assertTrue(req.isEmpty());
    }

    // Tests parse with options, properties and stopAtNonOption true
    @Test
    public void testParse_optionsPropertiesStopAtNonOption_returnsCommandLine() throws Exception {
        Option opt = new Option("l", true, "desc");
        options.addOption(opt);
        Properties props = new Properties();
        props.setProperty("m", "yes");
        CommandLine cmd = parser.parse(options, new String[]{"-l", "value"}, props, true);
        assertTrue(cmd.hasOption("l"));
        assertEquals("value", cmd.getOptionValue("l"));
    }

    // Tests parse with properties containing option not in options
    @Test
    public void testParse_propertiesUnknownOption_noException() throws Exception {
        Properties props = new Properties();
        props.setProperty("n", "yes");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertFalse(cmd.hasOption("n"));
    }

    // Tests processOption with required option removes from required list
    @Test
    public void testProcessOption_requiredOption_removeFromRequired() throws Exception {
        Option req = new Option("o", false, "desc");
        req.setRequired(true);
        options.addOption(req);
        parser.setOptions(options);
        java.util.ListIterator<String> iter = java.util.Collections.emptyList().listIterator();
        parser.processOption("-o", iter);
        assertTrue(parser.getRequiredOptions().isEmpty());
    }

    // Tests processArgs with optional argument and no value
    @Test
    public void testProcessArgs_optionalArgument_noValue_returns() throws Exception {
        Option opt = new Option("p", false, "desc");
        opt.setOptionalArg(true);
        options.addOption(opt);
        java.util.List<String> list = new java.util.ArrayList<String>();
        list.add("-q");
        java.util.ListIterator<String> iter = list.listIterator();
        parser.processArgs(opt, iter);
        assertNull(opt.getValues());
    }

    // ============== New test cases to cover missing parts ==============

    // Tests processArgs with required argument and value present
    @Test
    public void testProcessArgs_requiredArgumentWithValue_consumesAndSetsValue() throws Exception {
        Option opt = new Option("r", true, "desc");
        java.util.List<String> list = new java.util.ArrayList<String>();
        list.add("value");
        java.util.ListIterator<String> iter = list.listIterator();
        parser.processArgs(opt, iter);
        assertNotNull(opt.getValues());
        assertEquals("value", opt.getValue());
        assertFalse(iter.hasNext());
    }

    // Tests processArgs with optional argument and value present (consumes value)
    @Test
    public void testProcessArgs_optionalArgumentWithValue_consumesValue() throws Exception {
        Option opt = new Option("o", false, "desc");
        opt.setOptionalArg(true);
        java.util.List<String> list = new java.util.ArrayList<String>();
        list.add("somevalue");
        java.util.ListIterator<String> iter = list.listIterator();
        parser.processArgs(opt, iter);
        assertNotNull(opt.getValues());
        assertEquals("somevalue", opt.getValue());
        assertFalse(iter.hasNext());
    }

    // Tests processOption with unknown option throws unrecognized option exception
    @Test(expected = UnrecognizedOptionException.class)
    public void testProcessOption_unknownOption_throwsUnrecognizedOptionException() throws Exception {
        parser.setOptions(options);
        java.util.ListIterator<String> iter = java.util.Collections.emptyList().listIterator();
        parser.processOption("-x", iter);
    }

    // Tests processOption for an option with argument processes the argument
    @Test
    public void testProcessOption_optionWithArgument_processesArg() throws Exception {
        Option opt = new Option("a", true, "desc");
        options.addOption(opt);
        parser.setOptions(options);
        java.util.List<String> list = new java.util.ArrayList<String>();
        list.add("argValue");
        java.util.ListIterator<String> iter = list.listIterator();
        parser.processOption("-a", iter);
        assertNotNull(opt.getValues());
        assertEquals("argValue", opt.getValue());
        assertFalse(iter.hasNext());
    }

    // Tests processOption with a long option
    @Test
    public void testProcessOption_longOption_recognizesLongOption() throws Exception {
        Option opt = new Option("l", true, "desc");
        opt.setLongOpt("long");
        options.addOption(opt);
        parser.setOptions(options);
        java.util.List<String> list = new java.util.ArrayList<String>();
        list.add("value");
        java.util.ListIterator<String> iter = list.listIterator();
        parser.processOption("--long", iter);
        assertNotNull(opt.getValues());
        assertEquals("value", opt.getValue());
    }

    // Tests processProperties with option that has argument sets value from property
    @Test
    public void testProcessProperties_optionWithArgument_setsValueFromProperty() throws Exception {
        Option opt = new Option("p", true, "desc");
        options.addOption(opt);
        parser.setOptions(options);
        Properties props = new Properties();
        props.setProperty("p", "propValue");
        parser.processProperties(props);
        assertNotNull(opt.getValue());
        assertEquals("propValue", opt.getValue());
    }

    // Tests checkRequiredOptions with multiple required options all present (no exception)
    @Test
    public void testCheckRequiredOptions_multipleRequiredAllPresent_noException() throws Exception {
        Option opt1 = new Option("a", false, "desc");
        opt1.setRequired(true);
        Option opt2 = new Option("b", false, "desc");
        opt2.setRequired(true);
        options.addOption(opt1);
        options.addOption(opt2);
        parser.setOptions(options);
        // Process both to remove from required list
        parser.processOption("-a", java.util.Collections.emptyList().listIterator());
        parser.processOption("-b", java.util.Collections.emptyList().listIterator());
        parser.checkRequiredOptions();
    }

    // Tests checkRequiredOptions with multiple required options, one missing throws exception
    @Test(expected = MissingOptionException.class)
    public void testCheckRequiredOptions_multipleRequiredOneMissing_throwsException() throws Exception {
        Option opt1 = new Option("a", false, "desc");
        opt1.setRequired(true);
        Option opt2 = new Option("b", false, "desc");
        opt2.setRequired(true);
        options.addOption(opt1);
        options.addOption(opt2);
        parser.setOptions(options);
        // Process only one
        parser.processOption("-a", java.util.Collections.emptyList().listIterator());
        parser.checkRequiredOptions();
    }

    // Tests getRequiredOptions non-empty
    @Test
    public void testGetRequiredOptions_nonEmpty_returnsRequiredList() throws Exception {
        Option opt = new Option("r", false, "desc");
        opt.setRequired(true);
        options.addOption(opt);
        parser.setOptions(options);
        List req = parser.getRequiredOptions();
        assertEquals(1, req.size());
        assertTrue(req.contains(opt));
    }

    // Tests parse with long option
    @Test
    public void testParse_longOption_recognizesOption() throws Exception {
        Option opt = new Option("l", false, "desc");
        opt.setLongOpt("long");
        options.addOption(opt);
        CommandLine cmd = parser.parse(options, new String[]{"--long"});
        assertTrue(cmd.hasOption("l"));
    }

    // Tests parse with long option requiring argument
    @Test
    public void testParse_longOptionWithArgument_returnsValue() throws Exception {
        Option opt = new Option("l", true, "desc");
        opt.setLongOpt("long");
        options.addOption(opt);
        CommandLine cmd = parser.parse(options, new String[]{"--long", "value"});
        assertTrue(cmd.hasOption("l"));
        assertEquals("value", cmd.getOptionValue("l"));
    }

    // Tests parse with unrecognized long option throws exception
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unrecognizedLongOption_throwsException() throws Exception {
        parser.parse(options, new String[]{"--unknown"});
    }

    // Tests parse with missing required argument for an option
    @Test(expected = MissingArgumentException.class)
    public void testParse_missingOptionArgument_throwsException() throws Exception {
        Option opt = new Option("m", true, "desc");
        options.addOption(opt);
        parser.parse(options, new String[]{"-m"});
    }

    // Tests parse with optional argument and value consumed
    @Test
    public void testParse_optionalArgumentWithValue_consumesValue() throws Exception {
        Option opt = new Option("n", false, "desc");
        opt.setOptionalArg(true);
        options.addOption(opt);
        CommandLine cmd = parser.parse(options, new String[]{"-n", "value"});
        assertTrue(cmd.hasOption("n"));
        assertEquals("value", cmd.getOptionValue("n"));
    }

    // Tests isOption method for short option
    @Test
    public void testIsOption_shortOption_returnsTrue() {
        assertTrue(parser.isOption("-a"));
    }

    // Tests isOption method for long option
    @Test
    public void testIsOption_longOption_returnsTrue() {
        assertTrue(parser.isOption("--long"));
    }

    // Tests isOption method for dash only returns false
    @Test
    public void testIsOption_dashOnly_returnsFalse() {
        assertFalse(parser.isOption("-"));
    }

    // Tests isOption method for non-option string returns false
    @Test
    public void testIsOption_nonOption_returnsFalse() {
        assertFalse(parser.isOption("arg"));
    }
}