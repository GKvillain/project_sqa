package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class PosixParserTest {

    private PosixParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new PosixParser();
        options = new Options();
    }

    // Tests flattening simple short option without arguments
    @Test
    public void testFlatten_singleShortOption_returnsOption() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a"}, result);
    }

    // Tests flattening long option with equals sign
    @Test
    public void testFlatten_longOptionWithEquals_splitsIntoTwoTokens() {
        options.addOption("foo", true, "foo option");
        String[] args = new String[]{"--foo=bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--foo", "bar"}, result);
    }

    // Tests flattening long option without equals sign
    @Test
    public void testFlatten_longOptionWithoutEquals_keepsToken() {
        options.addOption("foo", false, "foo option");
        String[] args = new String[]{"--foo"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--foo"}, result);
    }

    // Tests processing single hyphen token
    @Test
    public void testFlatten_singleHyphen_keepsToken() {
        String[] args = new String[]{"-"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-"}, result);
    }

    // Tests double hyphen token
    @Test
    public void testFlatten_doubleHyphen_keepsToken() {
        String[] args = new String[]{"--"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--"}, result);
    }

    // Tests unknown single short option when stopAtNonOption is true
    @Test
    public void testFlatten_unknownShortOptionStopAtNonOptionTrue_eatsRemaining() {
        String[] args = new String[]{"-z", "arg1", "arg2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"arg1", "arg2"}, result);
    }

    // Tests unrecognized multi-character option with exact match in Options
    @Test
    public void testFlatten_multiCharOptionDefined_keepsToken() {
        options.addOption("abc", false, "abc option");
        String[] args = new String[]{"-abc"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-abc"}, result);
    }

    // Tests bursting multiple combined single-character options
    @Test
    public void testFlatten_burstCombinedOptions_splitsTokens() {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        options.addOption("c", false, "option c");
        String[] args = new String[]{"-abc"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-b", "-c"}, result);
    }

    // Tests bursting option with attached argument value
    @Test
    public void testFlatten_burstOptionWithAttachedArg_splitsOptionAndArg() {
        options.addOption("a", false, "option a");
        options.addOption("b", true, "option b with arg");
        String[] args = new String[]{"-abvalue"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-b", "value"}, result);
    }

    // Tests bursting unknown option with stopAtNonOption false
    @Test
    public void testFlatten_burstUnknownOptionStopFalse_keepsToken() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-az"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-az"}, result);
    }

    // Tests bursting unknown option with stopAtNonOption true (Defects4J CLI-17 regression test)
    @Test
    public void testFlatten_burstUnknownOptionStopTrue_processesAndStops() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-az", "extra"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "z", "extra"}, result);
    }

    // Tests non-option token with stopAtNonOption false
    @Test
    public void testFlatten_nonOptionStopFalse_addsToken() {
        String[] args = new String[]{"nonOption1", "nonOption2"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"nonOption1", "nonOption2"}, result);
    }

    // Tests non-option token with stopAtNonOption true when no option was previously active
    @Test
    public void testFlatten_nonOptionStopTrueWithoutCurrentOption_addsSpecialTokenAndEatsRest() {
        String[] args = new String[]{"file1", "file2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "file1", "file2"}, result);
    }

    // Tests non-option token following an option expecting an argument
    @Test
    public void testFlatten_optionExpectingArgFollowedByValue_consumesArg() {
        options.addOption("f", true, "file option");
        String[] args = new String[]{"-f", "filename.txt"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-f", "filename.txt"}, result);
    }

    // Tests multiple calls to flatten to verify init() resets state properly
    @Test
    public void testFlatten_multipleCalls_resetsStateCorrectly() {
        options.addOption("a", false, "option a");
        String[] args1 = new String[]{"-a", "file1"};
        String[] result1 = parser.flatten(options, args1, true);
        assertArrayEquals(new String[]{"-a", "--", "file1"}, result1);

        String[] args2 = new String[]{"-a"};
        String[] result2 = parser.flatten(options, args2, false);
        assertArrayEquals(new String[]{"-a"}, result2);
    }

    // Tests empty argument array
    @Test
    public void testFlatten_emptyArguments_returnsEmptyArray() {
        String[] args = new String[]{};
        String[] result = parser.flatten(options, args, false);
        assertEquals(0, result.length);
    }

    // Tests double hyphen followed by arguments eating the rest of the tokens
    @Test
    public void testFlatten_doubleHyphenWithTrailingArgs_gobbelsRemaining() {
        String[] args = new String[]{"--", "arg1", "arg2"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--", "arg1", "arg2"}, result);
    }

    // Tests unknown long option with stopAtNonOption true eating the rest of the arguments
    @Test
    public void testFlatten_unknownLongOptionStopAtNonOptionTrue_eatsRemaining() {
        String[] args = new String[]{"--unknown", "arg1", "arg2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--unknown", "arg1", "arg2"}, result);
    }

    // Tests unknown long option with stopAtNonOption false
    @Test
    public void testFlatten_unknownLongOptionStopAtNonOptionFalse_keepsTokens() {
        String[] args = new String[]{"--unknown", "arg1"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--unknown", "arg1"}, result);
    }

    // Tests option expecting argument followed by a hyphen-prefixed value
    @Test
    public void testFlatten_optionExpectingArgFollowedByHyphenValue_consumesHyphenAsArg() {
        options.addOption("f", true, "file option");
        String[] args = new String[]{"-f", "-value"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-f", "-value"}, result);
    }

    // Tests bursting combined options where the last option expects an argument in the next token
    @Test
    public void testFlatten_burstCombinedOptionLastRequiresArg_consumesNextToken() {
        options.addOption("a", false, "option a");
        options.addOption("b", true, "option b expecting arg");
        String[] args = new String[]{"-ab", "value"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "-b", "value"}, result);
    }
}