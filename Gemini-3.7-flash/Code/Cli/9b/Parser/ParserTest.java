package org.apache.commons.cli;

import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ParserTest {

    private Parser parser;
    private Options options;

    private static class TestParser extends Parser {
        @Override
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
            return arguments != null ? arguments : new String[0];
        }
    }

    @Before
    public void setUp() {
        parser = new TestParser();
        options = new Options();
    }

    // Tests normal option parsing with single option without arguments
    @Test
    public void testParse_simpleOptionWithoutArg_optionParsedSuccessfully() throws Exception {
        Option optA = new Option("a", false, "option a");
        options.addOption(optA);

        CommandLine cl = parser.parse(options, new String[]{"-a"});

        assertTrue(cl.hasOption("a"));
        assertEquals(0, cl.getArgs().length);
    }

    // Tests option parsing with an argument value
    @Test
    public void testParse_optionWithArgument_storesOptionAndValue() throws Exception {
        Option optB = new Option("b", true, "option b");
        options.addOption(optB);

        CommandLine cl = parser.parse(options, new String[]{"-b", "foo"});

        assertTrue(cl.hasOption("b"));
        assertEquals("foo", cl.getOptionValue("b"));
    }

    // Tests parsing null arguments array
    @Test
    public void testParse_nullArguments_treatedAsEmptyArray() throws Exception {
        Option optA = new Option("a", false, "option a");
        options.addOption(optA);

        CommandLine cl = parser.parse(options, (String[]) null);

        assertNotNull(cl);
        assertFalse(cl.hasOption("a"));
        assertEquals(0, cl.getArgs().length);
    }

    // Tests double dash token treats all subsequent tokens as non-option arguments
    @Test
    public void testParse_doubleDash_eatsRemainingTokensAsArguments() throws Exception {
        Option optA = new Option("a", false, "option a");
        Option optB = new Option("b", false, "option b");
        options.addOption(optA);
        options.addOption(optB);

        CommandLine cl = parser.parse(options, new String[]{"-a", "--", "-b", "arg1"});

        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-b", cl.getArgs()[0]);
        assertEquals("arg1", cl.getArgs()[1]);
    }

    // Tests single dash token when stopAtNonOption is false
    @Test
    public void testParse_singleDashNoStopAtNonOption_addsDashAsArg() throws Exception {
        Option optA = new Option("a", false, "option a");
        options.addOption(optA);

        CommandLine cl = parser.parse(options, new String[]{"-", "-a"}, false);

        assertTrue(cl.hasOption("a"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("-", cl.getArgs()[0]);
    }

    // Tests single dash token when stopAtNonOption is true
    @Test
    public void testParse_singleDashWithStopAtNonOption_eatsRemainingTokens() throws Exception {
        Option optA = new Option("a", false, "option a");
        options.addOption(optA);

        CommandLine cl = parser.parse(options, new String[]{"-", "-a"}, true);

        assertFalse(cl.hasOption("a"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("-a", cl.getArgs()[0]);
    }

    // Tests stopAtNonOption flag stops parsing when an unrecognized option is encountered
    @Test
    public void testParse_unrecognizedOptionWithStopAtNonOption_eatsRemainingTokens() throws Exception {
        Option optA = new Option("a", false, "option a");
        options.addOption(optA);

        CommandLine cl = parser.parse(options, new String[]{"-z", "-a"}, true);

        assertFalse(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-z", cl.getArgs()[0]);
        assertEquals("-a", cl.getArgs()[1]);
    }

    // Tests unrecognized option throws UnrecognizedOptionException when stopAtNonOption is false
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unrecognizedOption_throwsUnrecognizedOptionException() throws Exception {
        parser.parse(options, new String[]{"-z"});
    }

    // Tests missing single required option throws MissingOptionException
    @Test(expected = MissingOptionException.class)
    public void testParse_missingSingleRequiredOption_throwsMissingOptionException() throws Exception {
        Option req = new Option("r", false, "required option");
        req.setRequired(true);
        options.addOption(req);

        parser.parse(options, new String[]{});
    }

    // Tests missing multiple required options throws MissingOptionException with all missing options in message
    @Test
    public void testParse_missingMultipleRequiredOptions_throwsExceptionWithMissingList() {
        Option req1 = new Option("r", false, "required 1");
        req1.setRequired(true);
        Option req2 = new Option("s", false, "required 2");
        req2.setRequired(true);
        options.addOption(req1);
        options.addOption(req2);

        try {
            parser.parse(options, new String[]{});
            fail("Expected MissingOptionException");
        } catch (MissingOptionException e) {
            assertTrue(e.getMessage().contains("Missing required options"));
            assertTrue(e.getMessage().contains("r"));
            assertTrue(e.getMessage().contains("s"));
        } catch (ParseException e) {
            fail("Unexpected ParseException type");
        }
    }

    // Tests missing argument for option requiring one throws MissingArgumentException
    @Test(expected = MissingArgumentException.class)
    public void testParse_missingArgumentForOption_throwsMissingArgumentException() throws Exception {
        Option optB = new Option("b", true, "option b");
        options.addOption(optB);

        parser.parse(options, new String[]{"-b"});
    }

    // Tests optional argument when none provided does not throw exception
    @Test
    public void testParse_optionalArgumentNotProvided_succeedsWithoutValue() throws Exception {
        Option optO = new Option("o", true, "optional arg");
        optO.setOptionalArg(true);
        options.addOption(optO);

        CommandLine cl = parser.parse(options, new String[]{"-o"});

        assertTrue(cl.hasOption("o"));
        assertNull(cl.getOptionValue("o"));
    }

    // Tests option in OptionGroup is properly selected and satisfied
    @Test
    public void testParse_optionInRequiredGroup_selectsOptionAndSucceeds() throws Exception {
        Option opt1 = new Option("x", false, "option x");
        Option opt2 = new Option("y", false, "option y");
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);

        CommandLine cl = parser.parse(options, new String[]{"-x"});

        assertTrue(cl.hasOption("x"));
        assertFalse(cl.hasOption("y"));
        assertEquals("x", group.getSelected());
    }

    // Tests parsing with Properties supplying option values
    @Test
    public void testParse_propertiesWithArgOption_setsOptionValueFromProperties() throws Exception {
        Option optP = new Option("p", true, "property option");
        options.addOption(optP);

        Properties props = new Properties();
        props.setProperty("p", "propValue");

        CommandLine cl = parser.parse(options, new String[]{}, props);

        assertTrue(cl.hasOption("p"));
        assertEquals("propValue", cl.getOptionValue("p"));
    }

    // Tests parsing with Properties supplying boolean flags (true, 1, yes)
    @Test
    public void testParse_propertiesWithBooleanFlag_enablesOption() throws Exception {
        Option optFlag = new Option("f", false, "flag option");
        options.addOption(optFlag);

        Properties props = new Properties();
        props.setProperty("f", "true");

        CommandLine cl = parser.parse(options, new String[]{}, props);

        assertTrue(cl.hasOption("f"));
    }

    // Tests parsing with Properties when boolean flag value is false (should not add option)
    @Test
    public void testParse_propertiesWithFalseFlag_doesNotAddOption() throws Exception {
        Option optFlag = new Option("f", false, "flag option");
        options.addOption(optFlag);

        Properties props = new Properties();
        props.setProperty("f", "no");

        CommandLine cl = parser.parse(options, new String[]{}, props);

        assertFalse(cl.hasOption("f"));
    }

    // Tests overloaded parse method with properties and stopAtNonOption
    @Test
    public void testParse_overloadedMethodWithPropertiesAndStopAtNonOption_worksCorrectly() throws Exception {
        Option optA = new Option("a", true, "option a");
        options.addOption(optA);

        Properties props = new Properties();
        props.setProperty("a", "propValue");

        CommandLine cl = parser.parse(options, new String[]{"nonOption"}, props, true);

        assertTrue(cl.hasOption("a"));
        assertEquals("propValue", cl.getOptionValue("a"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("nonOption", cl.getArgs()[0]);
    }
}