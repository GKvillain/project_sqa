package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PosixParserTest {

    private PosixParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new PosixParser();
        options = new Options();
        options.addOption("a", false, "option a");
        options.addOption("b", true, "option b");
        options.addOption("c", false, "option c");
        options.addOption("d", true, "option d");
        options.addOption("foo", false, "option foo");
        options.addOption("-bar", false, "option -bar");
    }

    // Tests empty argument array
    @Test
    public void testFlatten_emptyArgs_returnsEmptyArray() {
        String[] args = new String[0];
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[0], result);
    }

    // Tests single hyphen token
    @Test
    public void testFlatten_singleHyphen_retainsHyphen() {
        String[] args = new String[]{"-"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-"}, result);
    }

    // Tests double hyphen token without equals
    @Test
    public void testFlatten_doubleHyphenWithoutEquals_retainsToken() {
        String[] args = new String[]{"--foo"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--foo"}, result);
    }

    // Tests double hyphen token with equals splitting
    @Test
    public void testFlatten_doubleHyphenWithEquals_splitsIntoTwoTokens() {
        String[] args = new String[]{"--foo=bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--foo", "bar"}, result);
    }

    // Tests valid short option (length 2)
    @Test
    public void testFlatten_validShortOption_addsOptionToken() {
        String[] args = new String[]{"-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a"}, result);
    }

    // Tests invalid short option when stopAtNonOption is false
    @Test
    public void testFlatten_invalidShortOptionStopAtNonOptionFalse_ignoresOption() {
        String[] args = new String[]{"-z"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[0], result);
    }

    // Tests invalid short option when stopAtNonOption is true
    @Test
    public void testFlatten_invalidShortOptionStopAtNonOptionTrue_eatsRemainingTokens() {
        String[] args = new String[]{"-z", "arg1", "arg2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "arg1", "arg2"}, result);
    }

    // Tests bursting of concatenated short options
    @Test
    public void testFlatten_burstMultipleShortOptions_splitsIntoIndividualTokens() {
        String[] args = new String[]{"-ac"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-c"}, result);
    }

    // Tests bursting option with attached argument
    @Test
    public void testFlatten_burstOptionWithAttachedArgument_splitsOptionAndArgument() {
        String[] args = new String[]{"-bvalue"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value"}, result);
    }

    // Tests bursting multiple options where last option has attached argument
    @Test
    public void testFlatten_burstMultipleOptionsWithAttachedArgument_splitsAllTokens() {
        String[] args = new String[]{"-abvalue"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-b", "value"}, result);
    }

    // Tests bursting unrecognized option when stopAtNonOption is false
    @Test
    public void testFlatten_burstUnrecognizedOptionStopAtNonOptionFalse_addsRawToken() {
        String[] args = new String[]{"-xyz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-xyz"}, result);
    }

    // Tests bursting unrecognized option when stopAtNonOption is true
    @Test
    public void testFlatten_burstUnrecognizedOptionStopAtNonOptionTrue_processesRemainder() {
        String[] args = new String[]{"-xyz", "extra"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "xyz", "extra"}, result);
    }

    // Tests token matching option with hyphen directly in Options
    @Test
    public void testFlatten_matchingOptionWithHyphenPrefix_retainsToken() {
        String[] args = new String[]{"--bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--bar"}, result);
    }

    // Tests non-option token when stopAtNonOption is false
    @Test
    public void testFlatten_nonOptionStopAtNonOptionFalse_addsToken() {
        String[] args = new String[]{"nonOption1", "nonOption2"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"nonOption1", "nonOption2"}, result);
    }

    // Tests non-option token when stopAtNonOption is true without current option
    @Test
    public void testFlatten_nonOptionStopAtNonOptionTrueWithoutCurrentOption_addsDoubleHyphenAndEatsRest() {
        String[] args = new String[]{"file1", "file2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "file1", "file2"}, result);
    }

    // Tests non-option token when stopAtNonOption is true following an option with argument
    @Test
    public void testFlatten_nonOptionStopAtNonOptionTrueWithOptionExpectingArg_consumesArg() {
        String[] args = new String[]{"-b", "bValue", "extra"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "bValue", "--", "extra"}, result);
    }

    // Tests resetting state across multiple flatten calls
    @Test
    public void testFlatten_multipleInvocations_resetsState() {
        String[] args1 = new String[]{"-a", "-b", "val"};
        String[] result1 = parser.flatten(options, args1, false);
        assertArrayEquals(new String[]{"-a", "-b", "val"}, result1);

        String[] args2 = new String[]{"-c"};
        String[] result2 = parser.flatten(options, args2, false);
        assertArrayEquals(new String[]{"-c"}, result2);
    }

    // Tests full parsing flow via parse method
    @Test
    public void testParse_standardCommandLine_parsesSuccessfully() throws Exception {
        String[] args = new String[]{"-a", "-b", "value", "arg1", "arg2"};
        CommandLine cl = parser.parse(options, args);
        assertNotNull(cl);
        assertTrue(cl.hasOption("a"));
        assertEquals("value", cl.getOptionValue("b"));
        assertEquals(2, cl.getArgs().length);
    }
}