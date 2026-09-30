package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Properties;

/**
 * JUnit 4 test class for org.apache.commons.cli.Parser.
 * Tests the concrete methods of Parser using the GnuParser implementation.
 */
public class ParserTest {

    // Helper: create an Options object with a required option and an option group
    private Options createOptionsWithRequired() {
        Options options = new Options();
        Option reqOpt = OptionBuilder.isRequired().create("r");
        options.addOption(reqOpt);
        return options;
    }

    private Options createOptionsWithGroup() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(OptionBuilder.create("a"));
        group.addOption(OptionBuilder.create("b"));
        options.addOptionGroup(group);
        return options;
    }

    // Helper: create an option that takes an argument
    private Options createOptionsWithArg() {
        Options options = new Options();
        options.addOption(OptionBuilder.hasArg().create("o"));
        return options;
    }

    // Helper: create boolean flags (no args)
    private Options createBooleanFlags() {
        Options options = new Options();
        options.addOption(OptionBuilder.create("f"));
        options.addOption(OptionBuilder.create("g"));
        return options;
    }

    // Tests null arguments converted to empty array
    @Test
    public void testParse_nullArguments_returnsEmptyCommandLine() throws ParseException {
        Parser parser = new GnuParser();
        Options options = new Options();
        CommandLine cmd = parser.parse(options, null);
        assertNotNull(cmd);
        assertEquals(0, cmd.getArgs().length);
    }

    // Tests double-dash stops parsing and adds remaining as args
    @Test
    public void testParse_doubleDash_eatsRest() throws ParseException {
        Parser parser = new GnuParser();
        Options options = new Options();
        String[] args = {"--", "arg1", "arg2"};
        CommandLine cmd = parser.parse(options, args, false);
        assertEquals(2, cmd.getArgs().length);
        assertEquals("arg1", cmd.getArgs()[0]);
        assertEquals("arg2", cmd.getArgs()[1]);
    }

    // Tests single dash with stopAtNonOption=true is added as arg
    @Test
    public void testParse_singleDash_stopAtNonOptionTrue_addsArg() throws ParseException {
        Parser parser = new GnuParser();
        Options options = new Options();
        String[] args = {"-"};
        CommandLine cmd = parser.parse(options, args, true);
        assertEquals(1, cmd.getArgs().length);
        assertEquals("-", cmd.getArgs()[0]);
    }

    // Tests single dash with stopAtNonOption=false is added as arg
    @Test
    public void testParse_singleDash_stopAtNonOptionFalse_addsArg() throws ParseException {
        Parser parser = new GnuParser();
        Options options = new Options();
        String[] args = {"-"};
        CommandLine cmd = parser.parse(options, args, false);
        assertEquals(1, cmd.getArgs().length);
        assertEquals("-", cmd.getArgs()[0]);
    }

    // Tests unknown option with stopAtNonOption=false throws exception
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unknownOption_stopAtNonOptionFalse_throwsException() throws ParseException {
        Parser parser = new GnuParser();
        Options options = new Options();
        String[] args = {"-x"};
        parser.parse(options, args, false);
    }

    // Tests unknown option with stopAtNonOption=true is added as arg
    @Test
    public void testParse_unknownOption_stopAtNonOptionTrue_addsArg() throws ParseException {
        Parser parser = new GnuParser();
        Options options = new Options();
        String[] args = {"-x", "y"};
        CommandLine cmd = parser.parse(options, args, true);
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-x", cmd.getArgs()[0]);
        assertEquals("y", cmd.getArgs()[1]);
    }

    // Tests option with argument
    @Test
    public void testParse_optionWithArg_returnsCommandLineWithOptionAndValue() throws ParseException {
        Parser parser = new GnuParser();
        Options options = createOptionsWithArg();
        String[] args = {"-o", "val"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("o"));
        assertEquals("val", cmd.getOptionValue("o"));
    }

    // Tests option with argument but missing argument throws MissingArgumentException
    @Test(expected = MissingArgumentException.class)
    public void testParse_optionWithArgMissing_throwsMissingArgumentException() throws ParseException {
        Parser parser = new GnuParser();
        Options options = createOptionsWithArg();
        String[] args = {"-o"};
        parser.parse(options, args);
    }

    // Tests required option present succeeds
    @Test
    public void testParse_requiredOptionPresent_success() throws ParseException {
        Parser parser = new GnuParser();
        Options options = createOptionsWithRequired();
        String[] args = {"-r"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("r"));
    }

    // Tests required option missing throws MissingOptionException
    @Test(expected = MissingOptionException.class)
    public void testParse_requiredOptionMissing_throwsMissingOptionException() throws ParseException {
        Parser parser = new GnuParser();
        Options options = createOptionsWithRequired();
        String[] args = {};
        parser.parse(options, args);
    }

    // Tests required option group with one selected succeeds
    @Test
    public void testParse_requiredOptionGroup_oneSelected_success() throws ParseException {
        Parser parser = new GnuParser();
        Options options = createOptionsWithGroup();
        String[] args = {"-a"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
    }

    // Tests required option group with no selected throws MissingOptionException
    @Test(expected = MissingOptionException.class)
    public void testParse_requiredOptionGroup_noSelected_throwsMissingOptionException() throws ParseException {
        Parser parser = new GnuParser();
        Options options = createOptionsWithGroup();
        String[] args = {};
        parser.parse(options, args);
    }

    // Tests processProperties with null properties
    @Test
    public void testProcessProperties_nullProperties_noEffect() throws ParseException {
        Parser parser = new GnuParser();
        Options options = createBooleanFlags();
        CommandLine cmd = parser.parse(options, new String[]{}, (Properties) null);
        assertFalse(cmd.hasOption("f"));
        assertFalse(cmd.hasOption("g"));
    }

    // Tests processProperties with single property set to "true" adds option
    @Test
    public void testProcessProperties_singlePropertyTrue_addsOption() throws ParseException {
        Parser parser = new GnuParser();
        Options options = createBooleanFlags();
        Properties props = new Properties();
        props.setProperty("f", "true");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("f"));
    }

    // Tests processProperties with single property set to "no" does not add option
    @Test
    public void testProcessProperties_singlePropertyFalse_doesNotAddOption() throws ParseException {
        Parser parser = new GnuParser();
        Options options = createBooleanFlags();
        Properties props = new Properties();
        props.setProperty("f", "no");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertFalse(cmd.hasOption("f"));
    }

    // Tests processProperties with multiple properties: first false, second true.
    // This test detects the break bug (should not break, should continue).
    @Test
    public void testProcessProperties_multipleProperties_breakBug_detection() throws ParseException {
        Parser parser = new GnuParser();
        Options options = createBooleanFlags(); // options: -f, -g
        Properties props = new Properties();
        props.setProperty("f", "no");   // should not add -f
        props.setProperty("g", "yes");  // should add -g
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        // With the bug (break), -g is not added.
        // Correct behavior: -g is added.
        assertFalse("Option -f should not be present because property value is 'no'", cmd.hasOption("f"));
        assertTrue("Option -g should be present because property value is 'yes'", cmd.hasOption("g"));
    }

    // === New test cases for uncovered parts ===

    // Tests processProperties with option that takes an argument (string property value)
    @Test
    public void testProcessProperties_optionWithArg_setsValue() throws ParseException {
        Parser parser = new GnuParser();
        Options options = createOptionsWithArg();
        Properties props = new Properties();
        props.setProperty("o", "myvalue");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("o"));
        assertEquals("myvalue", cmd.getOptionValue("o"));
    }

    // Tests processProperties where property option is not defined in Options (should be ignored)
    @Test
    public void testProcessProperties_unknownPropertyOption_ignored() throws ParseException {
        Parser parser = new GnuParser();
        Options options = createBooleanFlags();
        Properties props = new Properties();
        props.setProperty("z", "true");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertFalse(cmd.hasOption("z"));
    }

    // Tests processProperties with option that has an argument, but property value is empty string
    @Test
    public void testProcessProperties_optionWithArg_emptyPropertyValue_setsEmptyValue() throws ParseException {
        Parser parser = new GnuParser();
        Options options = createOptionsWithArg();
        Properties props = new Properties();
        props.setProperty("o", "");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("o"));
        assertEquals("", cmd.getOptionValue("o"));
    }

    // Tests processProperties with an option that takes an argument, but property is "true" (no argument needed for boolean)
    // This should be treated as setting the option with value "true"
    @Test
    public void testProcessProperties_optionWithArg_propertyTrue_setsValueTrue() throws ParseException {
        Parser parser = new GnuParser();
        Options options = createOptionsWithArg();
        Properties props = new Properties();
        props.setProperty("o", "true");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("o"));
        assertEquals("true", cmd.getOptionValue("o"));
    }

    // Tests processProperties with an option that does not take argument, property set to "no" (should be ignored)
    @Test
    public void testProcessProperties_optionNoArg_propertyNo_ignored() throws ParseException {
        Parser parser = new GnuParser();
        Options options = createBooleanFlags();
        Properties props = new Properties();
        props.setProperty("f", "no");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertFalse(cmd.hasOption("f"));
    }

    // Tests processProperties with an option that does not take argument, property set to "yes" (should be added)
    @Test
    public void testProcessProperties_optionNoArg_propertyYes_addsOption() throws ParseException {
        Parser parser = new GnuParser();
        Options options = createBooleanFlags();
        Properties props = new Properties();
        props.setProperty("f", "yes");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("f"));
    }
}