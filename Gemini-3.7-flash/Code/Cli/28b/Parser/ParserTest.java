package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ParserTest {

    private static class DummyParser extends Parser {
        @Override
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
            return arguments != null ? arguments : new String[0];
        }
    }

    private Parser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new DummyParser();
        options = new Options();
    }

    // Tests normal parsing with a single option without arguments
    @Test
    public void testParse_simpleOption_parsedSuccessfully() throws Exception {
        options.addOption("a", "alpha", false, "alpha option");
        CommandLine cl = parser.parse(options, new String[]{"-a"});

        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("alpha"));
    }

    // Tests parsing with null arguments array defaulting to empty array
    @Test
    public void testParse_nullArguments_returnsEmptyCommandLine() throws Exception {
        options.addOption("a", false, "alpha option");
        CommandLine cl = parser.parse(options, (String[]) null);

        assertFalse(cl.hasOption("a"));
        assertEquals(0, cl.getArgs().length);
    }

    // Tests parsing arguments following double-dash
    @Test
    public void testParse_doubleDashToken_eatsRemainingTokensAsArgs() throws Exception {
        options.addOption("a", false, "alpha option");
        CommandLine cl = parser.parse(options, new String[]{"-a", "--", "-b", "foo"});

        assertTrue(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-b", cl.getArgs()[0]);
        assertEquals("foo", cl.getArgs()[1]);
    }

    // Tests single dash token when stopAtNonOption is false
    @Test
    public void testParse_singleDashWithoutStopAtNonOption_addsDashAsArg() throws Exception {
        CommandLine cl = parser.parse(options, new String[]{"-", "arg1"}, false);

        assertEquals(2, cl.getArgs().length);
        assertEquals("-", cl.getArgs()[0]);
        assertEquals("arg1", cl.getArgs()[1]);
    }

    // Tests single dash token when stopAtNonOption is true
    @Test
    public void testParse_singleDashWithStopAtNonOption_eatsRemainingTokens() throws Exception {
        CommandLine cl = parser.parse(options, new String[]{"-", "arg1"}, true);

        assertEquals(2, cl.getArgs().length);
        assertEquals("-", cl.getArgs()[0]);
        assertEquals("arg1", cl.getArgs()[1]);
    }

    // Tests unrecognized option when stopAtNonOption is false throws exception
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unrecognizedOptionNoStopAtNonOption_throwsException() throws Exception {
        parser.parse(options, new String[]{"-unknown"});
    }

    // Tests unrecognized option when stopAtNonOption is true adds to arguments and stops
    @Test
    public void testParse_unrecognizedOptionWithStopAtNonOption_eatsRemainingTokens() throws Exception {
        options.addOption("a", false, "alpha");
        CommandLine cl = parser.parse(options, new String[]{"-a", "-unknown", "extra"}, true);

        assertTrue(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-unknown", cl.getArgs()[0]);
        assertEquals("extra", cl.getArgs()[1]);
    }

    // Tests normal argument following option when stopAtNonOption is true
    @Test
    public void testParse_nonOptionArgWithStopAtNonOption_eatsRemainingTokens() throws Exception {
        options.addOption("a", false, "alpha");
        CommandLine cl = parser.parse(options, new String[]{"arg1", "-a"}, true);

        assertFalse(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("arg1", cl.getArgs()[0]);
        assertEquals("-a", cl.getArgs()[1]);
    }

    // Tests missing required option throws MissingOptionException
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOption_throwsException() throws Exception {
        Option opt = new Option("r", "req", false, "required option");
        opt.setRequired(true);
        options.addOption(opt);

        parser.parse(options, new String[]{});
    }

    // Tests option requiring an argument but none provided throws MissingArgumentException
    @Test(expected = MissingArgumentException.class)
    public void testParse_missingOptionArgument_throwsException() throws Exception {
        Option opt = new Option("v", true, "value option");
        options.addOption(opt);

        parser.parse(options, new String[]{"-v"});
    }

    // Tests option with optional argument provided
    @Test
    public void testParse_optionalArgumentProvided_returnsArgumentValue() throws Exception {
        Option opt = new Option("v", true, "optional value");
        opt.setOptionalArg(true);
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[]{"-v", "val"});
        assertTrue(cl.hasOption("v"));
        assertEquals("val", cl.getOptionValue("v"));
    }

    // Tests option with optional argument missing
    @Test
    public void testParse_optionalArgumentMissing_parsedWithoutError() throws Exception {
        Option opt = new Option("v", true, "optional value");
        opt.setOptionalArg(true);
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[]{"-v"});
        assertTrue(cl.hasOption("v"));
        assertEquals(null, cl.getOptionValue("v"));
    }

    // Tests OptionGroup selection and requirement
    @Test
    public void testParse_requiredOptionGroup_selectedProperly() throws Exception {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        Option opt1 = new Option("a", "alpha");
        Option opt2 = new Option("b", "beta");
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);

        CommandLine cl = parser.parse(options, new String[]{"-b"});
        assertTrue(cl.hasOption("b"));
        assertFalse(cl.hasOption("a"));
        assertEquals("b", group.getSelected());
    }

    // Tests properties processing for options with arguments
    @Test
    public void testProcessProperties_optionWithArg_setsValueFromProperty() throws Exception {
        options.addOption("prop", true, "property with arg");

        Properties props = new Properties();
        props.setProperty("prop", "propValue");

        CommandLine cl = parser.parse(options, new String[]{}, props);
        assertTrue(cl.hasOption("prop"));
        assertEquals("propValue", cl.getOptionValue("prop"));
    }

    // Tests properties processing with multiple boolean flags (Cli-28 regression check)
    @Test
    public void testProcessProperties_multiplePropertiesWithFalseFlag_processesSubsequentProperties() throws Exception {
        Option optA = new Option("a", false, "flag a");
        Option optB = new Option("b", false, "flag b");
        Option optC = new Option("c", true, "arg c");

        options.addOption(optA);
        options.addOption(optB);
        options.addOption(optC);

        Properties props = new Properties();
        props.setProperty("a", "false");
        props.setProperty("b", "true");
        props.setProperty("c", "cValue");

        CommandLine cl = parser.parse(options, new String[]{}, props);

        assertFalse("Option 'a' should not be set for 'false'", cl.hasOption("a"));
        assertTrue("Option 'b' should be set for 'true'", cl.hasOption("b"));
        assertTrue("Option 'c' should be set", cl.hasOption("c"));
        assertEquals("cValue", cl.getOptionValue("c"));
    }

    // Tests properties processing with "yes" and "1" flag values
    @Test
    public void testProcessProperties_flagWithYesAndOneValues_setsOption() throws Exception {
        options.addOption("y", false, "yes flag");
        options.addOption("o", false, "one flag");

        Properties props = new Properties();
        props.setProperty("y", "yes");
        props.setProperty("o", "1");

        CommandLine cl = parser.parse(options, new String[]{}, props);
        assertTrue(cl.hasOption("y"));
        assertTrue(cl.hasOption("o"));
    }

    // Tests stripping leading and trailing quotes from option value
    @Test
    public void testProcessArgs_quotedValue_stripsQuotes() throws Exception {
        options.addOption("v", true, "value option");
        CommandLine cl = parser.parse(options, new String[]{"-v", "\"quoted value\""});

        assertEquals("quoted value", cl.getOptionValue("v"));
    }
}