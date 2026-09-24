package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PosixParserTest {

    private PosixParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new PosixParser();
        options = new Options();
    }

    // Tests flattening single character options
    @Test
    public void testFlatten_singleCharOption_returnsOption() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a"}, result);
    }

    // Tests flattening single hyphen token
    @Test
    public void testFlatten_singleHyphen_returnsHyphen() {
        String[] args = new String[]{"-"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-"}, result);
    }

    // Tests flattening long option with equals sign
    @Test
    public void testFlatten_longOptionWithEquals_splitsOptionAndValue() {
        options.addOption("foo", true, "foo option");
        String[] args = new String[]{"--foo=bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--foo", "bar"}, result);
    }

    // Tests flattening long option without equals sign
    @Test
    public void testFlatten_longOptionWithoutEquals_returnsLongOption() {
        options.addOption("foo", false, "foo option");
        String[] args = new String[]{"--foo"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--foo"}, result);
    }

    // Tests flattening unrecognized long option without stopAtNonOption
    @Test
    public void testFlatten_unrecognizedLongOptionNoStop_returnsNonOptionToken() {
        String[] args = new String[]{"--unknown", "arg1"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--", "--unknown", "arg1"}, result);
    }

    // Tests bursting multiple flag options combined in a single token
    @Test
    public void testFlatten_burstMultipleFlags_burstsIntoSeparateTokens() {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        options.addOption("c", false, "option c");
        String[] args = new String[]{"-abc"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-b", "-c"}, result);
    }

    // Tests bursting an option with an argument attached
    @Test
    public void testFlatten_burstOptionWithAttachedArg_burstsOptionAndArg() {
        Option optF = new Option("f", true, "file option");
        options.addOption(optF);
        String[] args = new String[]{"-ffilename.txt"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-f", "filename.txt"}, result);
    }

    // Tests bursting flags followed by an option with argument
    @Test
    public void testFlatten_burstFlagsFollowedByOptionWithArg_burstsProperly() {
        options.addOption("v", false, "verbose");
        options.addOption("f", true, "file");
        String[] args = new String[]{"-vffilename.txt"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-v", "-f", "filename.txt"}, result);
    }

    // Tests bursting unrecognized token when stopAtNonOption is false
    @Test
    public void testFlatten_burstUnrecognizedNoStop_returnsEntireToken() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-azx"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-azx"}, result);
    }

    // Tests bursting unrecognized token when stopAtNonOption is true
    @Test
    public void testFlatten_burstUnrecognizedStopAtNonOption_eatsRest() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-azx", "extra"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "zx", "extra"}, result);
    }

    // Tests stopAtNonOption on non-option token
    @Test
    public void testFlatten_nonOptionTokenStopAtNonOption_eatsRest() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"nonOption", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "nonOption", "-a"}, result);
    }

    // Tests non-option token when stopAtNonOption is false
    @Test
    public void testFlatten_nonOptionTokenNoStop_preservesToken() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"nonOption", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"nonOption", "-a"}, result);
    }

    // Tests single character unrecognized option when stopAtNonOption is true
    @Test
    public void testFlatten_unrecognizedSingleCharOptionStopAtNonOption_eatsRest() {
        String[] args = new String[]{"-u", "remaining"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-u", "remaining"}, result);
    }

    // Tests double hyphen token stopping processing
    @Test
    public void testFlatten_doubleHyphenToken_preservesDoubleHyphen() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"--", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--", "--", "-a"}, result);
    }

    // Tests end-to-end parse method using PosixParser
    @Test
    public void testParse_standardOptions_parsesSuccessfully() throws Exception {
        options.addOption("a", false, "option a");
        options.addOption("b", true, "option b");
        String[] args = new String[]{"-a", "-b", "value", "extra"};
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("value", cl.getOptionValue("b"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("extra", cl.getArgs()[0]);
    }

    // Tests end-to-end parse method with stopAtNonOption
    @Test
    public void testParse_stopAtNonOption_stopsParsingOptions() throws Exception {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        String[] args = new String[]{"-a", "nonOption", "-b"};
        CommandLine cl = parser.parse(options, args, true);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("nonOption", cl.getArgs()[0]);
        assertEquals("-b", cl.getArgs()[1]);
    }
}