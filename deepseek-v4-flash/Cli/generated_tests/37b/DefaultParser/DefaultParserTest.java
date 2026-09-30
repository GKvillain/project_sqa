package org.apache.commons.cli;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

public class DefaultParserTest {
    private Options options;
    private DefaultParser parser;
    private CommandLine cmd;

    @Before
    public void setUp() {
        options = new Options();
        parser = new DefaultParser();
    }

    @After
    public void tearDown() {
        options = null;
        parser = null;
        cmd = null;
    }

    @Test
    public void testParseBasicOptionAndValue() throws ParseException {
        options.addOption("a", "alpha", true, "Alpha option");
        String[] args = {"-a", "value"};
        cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }

    @Test
    public void testParseLongOptionWithEquals() throws ParseException {
        options.addOption("a", "alpha", true, "Alpha option");
        String[] args = {"--alpha=value"};
        cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }

    @Test
    public void testParseShortOptionNoArgument() throws ParseException {
        options.addOption("b", "beta", false, "Beta option");
        String[] args = {"-b"};
        cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("b"));
    }

    @Test
    public void testParseConcatenatedShortOptions() throws ParseException {
        options.addOption("a", false, "Alpha option");
        options.addOption("b", false, "Beta option");
        String[] args = {"-ab"};
        cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
    }

    @Test
    public void testParseShortOptionWithValueNoEquals() throws ParseException {
        options.addOption("a", "alpha", true, "Alpha option");
        String[] args = {"-avalue"};
        cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }

    @Test
    public void testParseShortOptionWithValueAndEquals() throws ParseException {
        options.addOption("a", "alpha", true, "Alpha option");
        String[] args = {"-a=value"};
        cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }

    @Test
    public void testParseLongPrefixOption() throws ParseException {
        options.addOption("X", "xarg", true, "X argument");
        String[] args = {"-Xmx512m"};
        cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("X"));
        assertEquals("mx512m", cmd.getOptionValue("X"));
    }

    @Test
    public void testParseJavaProperty() throws ParseException {
        options.addOption("D", "property", true, "Java property");
        String[] args = {"-Dkey=value"};
        cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("D"));
        assertEquals("key=value", cmd.getOptionValue("D"));
    }

    @Test
    public void testParseAllArgumentsAfterDoubleDash() throws ParseException {
        options.addOption("a", "alpha", false, "Alpha option");
        String[] args = {"--", "-notAnOption", "value"};
        cmd = parser.parse(options, args);
        assertEquals(2, cmd.getArgList().size());
        assertEquals("-notAnOption", cmd.getArgs()[0]);
        assertEquals("value", cmd.getArgs()[1]);
    }

    @Test
    public void testParseUnknownOptionThrowsException() throws ParseException {
        options.addOption("a", "alpha", false, "Alpha option");
        String[] args = {"-unknown"};
        try {
            parser.parse(options, args);
            fail("Expected UnrecognizedOptionException");
        } catch (UnrecognizedOptionException e) {
            assertEquals("-unknown", e.getOption());
        }
    }

    @Test
    public void testParseAmbiguousLongOptionThrowsException() throws ParseException {
        options.addOption("a", "alpha", false, "Alpha option");
        options.addOption("b", "alpine", false, "Alpine option");
        String[] args = {"--alp"};
        try {
            parser.parse(options, args);
            fail("Expected AmbiguousOptionException");
        } catch (AmbiguousOptionException e) {
            assertEquals("--alp", e.getOption());
        }
    }

    @Test
    public void testParseMissingRequiredOptionThrowsException() throws ParseException {
        options.addOption(Option.builder("r").required().build());
        String[] args = {};
        try {
            parser.parse(options, args);
            fail("Expected MissingOptionException");
        } catch (MissingOptionException e) {
            assertTrue(e.getMissingOptions().contains("r"));
        }
    }

    @Test
    public void testParseMissingArgumentForOptionThrowsException() throws ParseException {
        options.addOption("a", "alpha", true, "Alpha option");
        String[] args = {"-a"};
        try {
            parser.parse(options, args);
            fail("Expected MissingArgumentException");
        } catch (MissingArgumentException e) {
            assertEquals("a", e.getOption().getOpt());
        }
    }

    @Test
    public void testParseStopAtNonOption() throws ParseException {
        options.addOption("a", "alpha", false, "Alpha option");
        String[] args = {"-a", "nonOption", "-b"};
        cmd = parser.parse(options, args, true);
        assertTrue(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgList().size());
        assertEquals("nonOption", cmd.getArgs()[0]);
        assertEquals("-b", cmd.getArgs()[1]);
    }

    @Test
    public void testParseOptionGroupSelectsOne() throws ParseException {
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder("a").build());
        group.addOption(Option.builder("b").build());
        options.addOptionGroup(group);
        String[] args = {"-a"};
        cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));
    }

    @Test
    public void testParsePropertiesHandling() throws ParseException {
        options.addOption("a", "alpha", false, "Alpha option");
        java.util.Properties props = new java.util.Properties();
        props.setProperty("a", "true");
        cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("a"));
    }

    @Test
    public void testParseNegativeNumberAsValue() throws ParseException {
        options.addOption("n", "number", true, "Number option");
        String[] args = {"-n", "-5"};
        cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("n"));
        assertEquals("-5", cmd.getOptionValue("n"));
    }

    @Test
    public void testParsePropertyWithValueWhenOptionAlreadyHasValue() throws ParseException {
        options.addOption("a", "alpha", true, "Alpha option");
        java.util.Properties props = new java.util.Properties();
        props.setProperty("a", "fromProps");
        cmd = parser.parse(options, new String[]{"-a", "fromArgs"}, props);
        assertEquals("fromArgs", cmd.getOptionValue("a"));
    }

    @Test
    public void testParsePropertyWithYesValueForFlag() throws ParseException {
        options.addOption("a", "alpha", false, "Alpha option");
        java.util.Properties props = new java.util.Properties();
        props.setProperty("a", "yes");
        cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("a"));
    }

    @Test
    public void testParsePropertyWithNoValueForFlag() throws ParseException {
        options.addOption("a", "alpha", false, "Alpha option");
        java.util.Properties props = new java.util.Properties();
        props.setProperty("a", "no");
        cmd = parser.parse(options, new String[]{}, props);
        assertFalse(cmd.hasOption("a"));
    }

    @Test
    public void testParseUnknownPropertyThrowsException() throws ParseException {
        options.addOption("a", "alpha", false, "Alpha option");
        java.util.Properties props = new java.util.Properties();
        props.setProperty("unknown", "true");
        try {
            parser.parse(options, new String[]{}, props);
            fail("Expected UnrecognizedOptionException");
        } catch (UnrecognizedOptionException e) {
            assertEquals("unknown", e.getOption());
        }
    }
}