package org.apache.commons.cli;

import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ParserTest {

    private Parser parser;
    private Options options;

    private static class DummyParser extends Parser {
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
            return arguments != null ? arguments : new String[0];
        }
    }

    @Before
    public void setUp() {
        parser = new DummyParser();
        options = new Options();
    }

    // Tests simple option parsing
    @Test
    public void testParse_simpleOption_parsedSuccessfully() throws Exception {
        Option optA = new Option("a", "alpha", false, "Option A");
        options.addOption(optA);

        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertNotNull(cmd);
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("alpha"));
    }

    // Tests option with required argument
    @Test
    public void testParse_optionWithArgument_argumentExtracted() throws Exception {
        Option optB = new Option("b", true, "Option B with arg");
        options.addOption(optB);

        CommandLine cmd = parser.parse(options, new String[]{"-b", "foo"});
        assertNotNull(cmd);
        assertTrue(cmd.hasOption("b"));
        assertEquals("foo", cmd.getOptionValue("b"));
    }

    // Tests missing argument exception for option requiring argument
    @Test(expected = MissingArgumentException.class)
    public void testParse_missingArgument_throwsException() throws Exception {
        Option optB = new Option("b", true, "Option B with arg");
        options.addOption(optB);

        parser.parse(options, new String[]{"-b"});
    }

    // Tests optional argument when missing argument
    @Test
    public void testParse_optionalArgumentMissing_parsedSuccessfully() throws Exception {
        Option optC = new Option("c", true, "Option C with optional arg");
        optC.setOptionalArg(true);
        options.addOption(optC);

        CommandLine cmd = parser.parse(options, new String[]{"-c"});
        assertNotNull(cmd);
        assertTrue(cmd.hasOption("c"));
        assertNull(cmd.getOptionValue("c"));
    }

    // Tests unrecognized option exception
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unrecognizedOption_throwsException() throws Exception {
        options.addOption(new Option("a", false, "Option A"));
        parser.parse(options, new String[]{"-z"});
    }

    // Tests missing required option exception
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOption_throwsException() throws Exception {
        Option optRequired = new Option("r", true, "Required Option");
        optRequired.setRequired(true);
        options.addOption(optRequired);

        parser.parse(options, new String[]{});
    }

    // Tests multiple missing required options exception message formatting
    @Test
    public void testParse_multipleMissingRequiredOptions_throwsExceptionWithMessage() {
        Option opt1 = new Option("a", "alpha", false, "Required option A");
        opt1.setRequired(true);
        Option opt2 = new Option("b", "beta", false, "Required option B");
        opt2.setRequired(true);
        options.addOption(opt1);
        options.addOption(opt2);

        try {
            parser.parse(options, new String[]{});
            fail("Expected MissingOptionException");
        } catch (MissingOptionException e) {
            assertTrue(e.getMessage().contains("Missing required options:"));
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    // Tests required OptionGroup satisfied
    @Test
    public void testParse_requiredOptionGroup_parsedSuccessfully() throws Exception {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        Option optA = new Option("a", false, "Option A");
        Option optB = new Option("b", false, "Option B");
        group.addOption(optA);
        group.addOption(optB);
        options.addOptionGroup(group);

        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertNotNull(cmd);
        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));
    }

    // Tests required OptionGroup unsatisfied throws exception
    @Test(expected = MissingOptionException.class)
    public void testParse_missingRequiredOptionGroup_throwsException() throws Exception {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", false, "Option A"));
        options.addOptionGroup(group);

        parser.parse(options, new String[]{});
    }

    // Tests double-dash token stops option processing and eats remaining tokens as args
    @Test
    public void testParse_doubleDash_eatsRemainingTokensAsArgs() throws Exception {
        options.addOption(new Option("a", false, "Option A"));
        options.addOption(new Option("b", false, "Option B"));

        CommandLine cmd = parser.parse(options, new String[]{"-a", "--", "-b", "arg1"});
        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-b", cmd.getArgs()[0]);
        assertEquals("arg1", cmd.getArgs()[1]);
    }

    // Tests single dash token handling without stopAtNonOption
    @Test
    public void testParse_singleDash_treatedAsArg() throws Exception {
        CommandLine cmd = parser.parse(options, new String[]{"-", "extra"});
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-", cmd.getArgs()[0]);
        assertEquals("extra", cmd.getArgs()[1]);
    }

    // Tests single dash token handling with stopAtNonOption
    @Test
    public void testParse_singleDashWithStopAtNonOption_stopsProcessing() throws Exception {
        options.addOption(new Option("a", false, "Option A"));
        CommandLine cmd = parser.parse(options, new String[]{"-", "-a"}, true);
        assertFalse(cmd.hasOption("a"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("-a", cmd.getArgs()[0]);
    }

    // Tests unknown option with stopAtNonOption true eats remaining tokens
    @Test
    public void testParse_unknownOptionWithStopAtNonOption_stopsProcessing() throws Exception {
        options.addOption(new Option("a", false, "Option A"));
        CommandLine cmd = parser.parse(options, new String[]{"-z", "-a"}, true);
        assertFalse(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-z", cmd.getArgs()[0]);
        assertEquals("-a", cmd.getArgs()[1]);
    }

    // Tests normal argument with stopAtNonOption true
    @Test
    public void testParse_nonOptionWithStopAtNonOption_stopsProcessing() throws Exception {
        options.addOption(new Option("a", false, "Option A"));
        CommandLine cmd = parser.parse(options, new String[]{"foo", "-a"}, true);
        assertFalse(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("foo", cmd.getArgs()[0]);
        assertEquals("-a", cmd.getArgs()[1]);
    }

    // Tests properties processing for options with arguments
    @Test
    public void testParse_propertiesWithArgOption_setsOptionValue() throws Exception {
        Option optD = new Option("d", true, "Option D");
        options.addOption(optD);

        Properties props = new Properties();
        props.setProperty("d", "propertyValue");

        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("d"));
        assertEquals("propertyValue", cmd.getOptionValue("d"));
    }

    // Tests properties processing for boolean flag options
    @Test
    public void testParse_propertiesWithBooleanOption_setsOptionFlag() throws Exception {
        Option optE = new Option("e", false, "Option E");
        options.addOption(optE);

        Properties props = new Properties();
        props.setProperty("e", "true");

        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("e"));
    }

    // Tests null arguments array handled gracefully
    @Test
    public void testParse_nullArguments_parsedAsEmpty() throws Exception {
        CommandLine cmd = parser.parse(options, null);
        assertNotNull(cmd);
        assertEquals(0, cmd.getArgs().length);
    }

    // Tests AlreadySelectedException when multiple options from the same OptionGroup are supplied
    @Test(expected = AlreadySelectedException.class)
    public void testParse_optionGroupAlreadySelected_throwsAlreadySelectedException() throws Exception {
        OptionGroup group = new OptionGroup();
        Option optA = new Option("a", false, "Option A");
        Option optB = new Option("b", false, "Option B");
        group.addOption(optA);
        group.addOption(optB);
        options.addOptionGroup(group);

        parser.parse(options, new String[]{"-a", "-b"});
    }

    // Tests option with multiple arguments (fixed number)
    @Test
    public void testParse_multipleArgumentsForOption() throws Exception {
        Option optM = new Option("m", true, "Multi arg");
        optM.setArgs(2);
        options.addOption(optM);

        CommandLine cmd = parser.parse(options, new String[]{"-m", "val1", "val2"});
        assertTrue(cmd.hasOption("m"));
        String[] values = cmd.getOptionValues("m");
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals("val1", values[0]);
        assertEquals("val2", values[1]);
    }

    // Tests unlimited arguments for option stopped by next option token
    @Test
    public void testParse_unlimitedArgumentsForOption_stopsAtNextOption() throws Exception {
        Option optU = new Option("u", true, "Unlimited args");
        optU.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(optU);
        options.addOption(new Option("k", false, "Key flag"));

        CommandLine cmd = parser.parse(options, new String[]{"-u", "v1", "v2", "-k"});
        assertTrue(cmd.hasOption("u"));
        assertTrue(cmd.hasOption("k"));
        assertEquals(2, cmd.getOptionValues("u").length);
        assertEquals("v1", cmd.getOptionValues("u")[0]);
        assertEquals("v2", cmd.getOptionValues("u")[1]);
    }

    // Tests value separator handling when processing arguments
    @Test
    public void testParse_valueSeparatorHandling() throws Exception {
        Option optD = new Option("D", true, "Property definition");
        optD.setValueSeparator('=');
        optD.setArgs(2);
        options.addOption(optD);

        CommandLine cmd = parser.parse(options, new String[]{"-D", "key=value"});
        assertTrue(cmd.hasOption("D"));
        String[] values = cmd.getOptionValues("D");
        assertEquals(2, values.length);
        assertEquals("key", values[0]);
        assertEquals("value", values[1]);
    }

    // Tests argument enclosed in double quotes has quotes stripped
    @Test
    public void testParse_quotedArgumentStripped() throws Exception {
        Option optQ = new Option("q", true, "Quoted arg");
        options.addOption(optQ);

        CommandLine cmd = parser.parse(options, new String[]{"-q", "\"quotedValue\""});
        assertTrue(cmd.hasOption("q"));
        assertEquals("quotedValue", cmd.getOptionValue("q"));
    }

    // Tests argument enclosed in single quotes has quotes stripped
    @Test
    public void testParse_singleQuotedArgumentStripped() throws Exception {
        Option optQ = new Option("q", true, "Single quoted arg");
        options.addOption(optQ);

        CommandLine cmd = parser.parse(options, new String[]{"-q", "'quotedValue'"});
        assertTrue(cmd.hasOption("q"));
        assertEquals("quotedValue", cmd.getOptionValue("q"));
    }

    // Tests properties processing ignores options not defined in Options
    @Test
    public void testParse_propertiesNonExistentOption_ignored() throws Exception {
        Properties props = new Properties();
        props.setProperty("unknown", "someValue");

        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertFalse(cmd.hasOption("unknown"));
    }

    // Tests properties processing with various boolean representations
    @Test
    public void testParse_propertiesBooleanOption_variousTruthValues() throws Exception {
        Option optYes = new Option("y", false, "Yes option");
        Option optOne = new Option("o", false, "One option");
        Option optFalse = new Option("f", false, "False option");
        options.addOption(optYes);
        options.addOption(optOne);
        options.addOption(optFalse);

        Properties props = new Properties();
        props.setProperty("y", "yes");
        props.setProperty("o", "1");
        props.setProperty("f", "false");

        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("y"));
        assertTrue(cmd.hasOption("o"));
        assertFalse(cmd.hasOption("f"));
    }

    // Tests properties processing respects already selected OptionGroup from args
    @Test
    public void testParse_propertiesOptionGroupConflict_ignored() throws Exception {
        OptionGroup group = new OptionGroup();
        Option optA = new Option("a", false, "Option A");
        Option optB = new Option("b", false, "Option B");
        group.addOption(optA);
        group.addOption(optB);
        options.addOptionGroup(group);

        Properties props = new Properties();
        props.setProperty("b", "true");

        CommandLine cmd = parser.parse(options, new String[]{"-a"}, props);
        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));
    }

    // Tests parse overload with null properties
    @Test
    public void testParse_nullProperties_processedWithoutError() throws Exception {
        Option optA = new Option("a", false, "Option A");
        options.addOption(optA);

        CommandLine cmd = parser.parse(options, new String[]{"-a"}, (Properties) null);
        assertTrue(cmd.hasOption("a"));
    }

    // Tests parse overload with all 4 arguments (options, args, properties, stopAtNonOption)
    @Test
    public void testParse_fullSignatureOverload() throws Exception {
        Option optA = new Option("a", false, "Option A");
        Option optB = new Option("b", true, "Option B");
        options.addOption(optA);
        options.addOption(optB);

        Properties props = new Properties();
        props.setProperty("b", "fromProps");

        CommandLine cmd = parser.parse(options, new String[]{"-a", "extra"}, props, true);
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
        assertEquals("fromProps", cmd.getOptionValue("b"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("extra", cmd.getArgs()[0]);
    }
}