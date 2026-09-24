package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class for PosixParser.
 */
public class PosixParserTest {

    private PosixParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new PosixParser();
        options = new Options();
        options.addOption("a", false, "flag a");
        options.addOption("b", true, "option b with argument");
        options.addOption("c", false, "flag c");
        options.addOption("d", "delta", true, "option delta with argument");
        options.addOption(new Option("foo", false, "multi-char option foo"));
    }

    // Tests empty argument array flattening
    @Test
    public void testFlatten_emptyArguments_returnsEmptyArray() {
        String[] args = new String[]{};
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    // Tests double hyphen with equals sign splitting into option and argument
    @Test
    public void testFlatten_doubleHyphenWithEquals_splitsKeyAndValue() {
        String[] args = new String[]{"--delta=value"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--delta", "value"}, result);
    }

    // Tests standard double hyphen option without equals
    @Test
    public void testFlatten_doubleHyphenWithoutEquals_addsToken() {
        String[] args = new String[]{"--delta", "value"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--delta", "value"}, result);
    }

    // Tests double hyphen terminator '--'
    @Test
    public void testFlatten_doubleHyphenTerminator_addsDirectly() {
        String[] args = new String[]{"--", "arg1", "arg2"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--", "arg1", "arg2"}, result);
    }

    // Tests single hyphen '-' token
    @Test
    public void testFlatten_singleHyphen_addsSingleHyphenToken() {
        String[] args = new String[]{"-"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-"}, result);
    }

    // Tests single hyphen mixed with other options
    @Test
    public void testFlatten_singleHyphenWithOtherOptions_preservesOrder() {
        String[] args = new String[]{"-a", "-", "nonOption"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-", "nonOption"}, result);
    }

    // Tests recognized 2-character short option
    @Test
    public void testFlatten_recognizedShortOption_addsToken() {
        String[] args = new String[]{"-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a"}, result);
    }

    // Tests unrecognized 2-character short option when stopAtNonOption is false
    @Test
    public void testFlatten_unrecognizedShortOptionStopAtNonOptionFalse_ignoresOption() {
        String[] args = new String[]{"-z", "arg1"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"arg1"}, result);
    }

    // Tests unrecognized 2-character short option when stopAtNonOption is true
    @Test
    public void testFlatten_unrecognizedShortOptionStopAtNonOptionTrue_eatsRemainingTokens() {
        String[] args = new String[]{"-z", "arg1", "arg2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"arg1", "arg2"}, result);
    }

    // Tests multi-character option with single hyphen that matches an option id
    @Test
    public void testFlatten_multiCharOptionMatchingDirectly_addsToken() {
        String[] args = new String[]{"-foo"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-foo"}, result);
    }

    // Tests bursting multiple flag options combined in a single token
    @Test
    public void testFlatten_burstMultipleFlags_burstsIntoSeparateTokens() {
        String[] args = new String[]{"-ac"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-c"}, result);
    }

    // Tests bursting flag and option with argument attached
    @Test
    public void testFlatten_burstFlagAndArgOption_burstsAndExtractsArg() {
        String[] args = new String[]{"-abmyval"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-b", "myval"}, result);
    }

    // Tests bursting with unrecognized character when stopAtNonOption is false
    @Test
    public void testFlatten_burstUnrecognizedStopAtNonOptionFalse_addsEntireToken() {
        String[] args = new String[]{"-azc"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-azc"}, result);
    }

    // Tests bursting with unrecognized character when stopAtNonOption is true
    @Test
    public void testFlatten_burstUnrecognizedStopAtNonOptionTrue_stopsAndProcesses() {
        String[] args = new String[]{"-azc", "rest"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "zc", "rest"}, result);
    }

    // Tests non-option token following an option that requires an argument
    @Test
    public void testFlatten_nonOptionAfterOptionWithArg_consumesArg() {
        String[] args = new String[]{"-b", "bvalue", "extra"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "bvalue", "--", "extra"}, result);
    }

    // Tests non-option token when stopAtNonOption is true and currentOption is null
    @Test
    public void testFlatten_nonOptionStopAtNonOptionTrueNoCurrentOption_addsHyphenHyphenAndEatsRest() {
        String[] args = new String[]{"nonOption1", "nonOption2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "nonOption1", "nonOption2"}, result);
    }

    // Tests non-option token when stopAtNonOption is false
    @Test
    public void testFlatten_nonOptionStopAtNonOptionFalse_addsTokensDirectly() {
        String[] args = new String[]{"nonOption1", "nonOption2"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"nonOption1", "nonOption2"}, result);
    }

    // Tests full parsing workflow with options and non-option arguments
    @Test
    public void testParse_standardCommandLine_parsesSuccessfully() throws Exception {
        String[] args = new String[]{"-a", "-b", "foo", "--delta=bar", "extraArg"};
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("foo", cl.getOptionValue("b"));
        assertTrue(cl.hasOption("delta"));
        assertEquals("bar", cl.getOptionValue("delta"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("extraArg", cl.getArgs()[0]);
    }

    // Tests full parsing with stopAtNonOption set to true
    @Test
    public void testParse_stopAtNonOption_stopsProcessingOptions() throws Exception {
        String[] args = new String[]{"-a", "nonOption", "-b", "foo"};
        CommandLine cl = parser.parse(options, args, true);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(3, cl.getArgs().length);
        assertEquals("nonOption", cl.getArgs()[0]);
        assertEquals("-b", cl.getArgs()[1]);
        assertEquals("foo", cl.getArgs()[2]);
    }
}