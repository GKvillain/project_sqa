package org.apache.commons.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class ParserTest {

    private TestParser parser;

    @Before
    public void setUp() {
        parser = new TestParser();
    }

    // Tests parsing a valid option.
    @Test
    public void testParse_validOption_returnsCommandLine() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "enable a");

        CommandLine cl = parser.parse(options, new String[] {"-a"});

        assertTrue(cl.hasOption("a"));
        assertEquals(0, cl.getArgs().length);
    }

    // Tests null arguments are treated as an empty argument array.
    @Test
    public void testParse_nullArguments_returnsEmptyCommandLine() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "enable a");

        CommandLine cl = parser.parse(options, (String[]) null, false);

        assertNotNull(cl);
        assertFalse(cl.hasOption("a"));
    }

    // Tests option argument processing.
    @Test
    public void testParse_optionWithArg_processesValue() throws Exception {
        Options options = new Options();
        options.addOption("f", true, "file");

        CommandLine cl = parser.parse(options, new String[] {"-f", "file.txt"}, false);

        assertTrue(cl.hasOption("f"));
        assertEquals("file.txt", cl.getOptionValue("f"));
    }

    // Tests that a missing required option causes MissingOptionException.
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOption_throwsMissingOptionException() throws Exception {
        Options options = new Options();
        Option opt = new Option("a", false, "enable a");
        opt.setRequired(true);
        options.addOption(opt);

        parser.parse(options, new String[0], false);
    }

    // Tests that a required option is accepted when present.
    @Test
    public void testParse_requiredOptionPresent_succeeds() throws Exception {
        Options options = new Options();
        Option opt = new Option("a", false, "enable a");
        opt.setRequired(true);
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[] {"-a"}, false);

        assertTrue(cl.hasOption("a"));
    }

    // Tests that a required option group is satisfied when one option is selected.
    @Test
    public void testParse_requiredOptionGroupSelected_succeeds() throws Exception {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", false, "a"));
        group.addOption(new Option("b", false, "b"));
        options.addOptionGroup(group);

        CommandLine cl = parser.parse(options, new String[] {"-a"}, false);

        assertTrue(cl.hasOption("a"));
    }

    // Tests that a missing required option group causes MissingOptionException.
    @Test(expected = MissingOptionException.class)
    public void testParse_requiredOptionGroupMissing_throwsMissingOptionException() throws Exception {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", false, "a"));
        group.addOption(new Option("b", false, "b"));
        options.addOptionGroup(group);

        parser.parse(options, new String[0], false);
    }

    // Tests that an unrecognized option without stopAtNonOption throws.
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unknownOption_throwsUnrecognizedOptionException() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "enable a");

        parser.parse(options, new String[] {"-x"}, false);
    }

    // Tests that an unrecognized option is added as an argument when stopAtNonOption is true.
    @Test
    public void testParse_stopAtNonOption_unknownOptionAddedAsArg() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "enable a");

        CommandLine cl = parser.parse(options, new String[] {"-x", "-a"}, true);

        assertFalse(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-x", cl.getArgs()[0]);
        assertEquals("-a", cl.getArgs()[1]);
    }

    // Tests that a non-option stops option processing when stopAtNonOption is true.
    @Test
    public void testParse_stopAtNonOption_nonOptionStopsParsing() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "enable a");

        CommandLine cl = parser.parse(options, new String[] {"-a", "foo", "-a"}, true);

        assertTrue(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("foo", cl.getArgs()[0]);
        assertEquals("-a", cl.getArgs()[1]);
    }

    // Tests that after "--" the remaining tokens are treated as arguments.
    @Test
    public void testParse_doubleDash_stopsOptionProcessing() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "enable a");

        CommandLine cl = parser.parse(options, new String[] {"--", "-a"}, false);

        assertFalse(cl.hasOption("a"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("-a", cl.getArgs()[0]);
    }

    // Tests that a property with "true" adds a flag option.
    @Test
    public void testParse_propertiesFlagTrue_addsOption() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "enable a");

        Properties props = new Properties();
        props.setProperty("a", "true");

        CommandLine cl = parser.parse(options, new String[0], props);

        assertTrue(cl.hasOption("a"));
    }

    // Tests that a property with a non-boolean value does not add a flag option.
    @Test
    public void testParse_propertiesFlagFalse_doesNotAddOption() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "enable a");

        Properties props = new Properties();
        props.setProperty("a", "false");

        CommandLine cl = parser.parse(options, new String[0], props);

        assertFalse(cl.hasOption("a"));
    }

    // Tests that an option with an argument can be set from properties.
    @Test
    public void testParse_propertiesOptionWithArg_setsValue() throws Exception {
        Options options = new Options();
        options.addOption("f", true, "file");

        Properties props = new Properties();
        props.setProperty("f", "prop.txt");

        CommandLine cl = parser.parse(options, new String[0], props, false);

        assertTrue(cl.hasOption("f"));
        assertEquals("prop.txt", cl.getOptionValue("f"));
    }

    // Tests that an unknown property is rejected when processing properties.
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unknownProperty_throwsUnrecognizedOptionException() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "enable a");

        Properties props = new Properties();
        props.setProperty("unknown", "true");

        parser.parse(options, new String[0], props, false);
    }

    // Tests that a property does not override an option already selected from the same group.
    @Test
    public void testParse_propertiesDoNotOverrideOptionGroupSelection() throws Exception {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", false, "a"));
        group.addOption(new Option("b", false, "b"));
        options.addOptionGroup(group);

        Properties props = new Properties();
        props.setProperty("b", "true");

        CommandLine cl = parser.parse(options, new String[] {"-a"}, props, false);

        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
    }

    // Tests that an option requiring an argument throws when no argument is present.
    @Test(expected = MissingArgumentException.class)
    public void testParse_optionRequiringValueMissing_throwsMissingArgumentException() throws Exception {
        Options options = new Options();
        options.addOption("f", true, "file");

        parser.parse(options, new String[] {"-f"}, false);
    }

    // Minimal Parser implementation that lets Parser.parse operate on raw tokens.
    private static class TestParser extends Parser {
        @Override
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) throws ParseException {
            return arguments == null ? new String[0] : arguments;
        }
    }
}