package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class PosixParserTest {
    private PosixParser parser;
    private Options opts;

    @Before
    public void setUp() {
        parser = new PosixParser();
        opts = new Options();
    }

    private void addOption(String opt, boolean hasArg) {
        opts.addOption(opt, hasArg, "desc");
    }

    private void addLongOption(String longOpt, boolean hasArg) {
        opts.addOption(new Option("x", longOpt, hasArg, "desc"));
    }

    // Tests basic short option parsing
    @Test
    public void testFlatten_basicShortOption_returnsFlatArray() {
        addOption("a", false);
        assertArrayEquals(new String[]{"-a", "foo"}, parser.flatten(opts, new String[]{"-a", "foo"}, false));
    }

    // Tests basic long option parsing
    @Test
    public void testFlatten_basicLongOption_returnsFlatArray() {
        addLongOption("foo", false);
        assertArrayEquals(new String[]{"--foo", "bar"}, parser.flatten(opts, new String[]{"--foo", "bar"}, false));
    }

    // Tests long option with equals argument
    @Test
    public void testFlatten_longOptionWithEqualsArg_returnsOptionAndValue() {
        addLongOption("foo", true);
        assertArrayEquals(new String[]{"--foo", "bar"}, parser.flatten(opts, new String[]{"--foo=bar"}, false));
    }

    // Tests single hyphen token
    @Test
    public void testFlatten_singleHyphen_returnsHyphen() {
        assertArrayEquals(new String[]{"-"}, parser.flatten(opts, new String[]{"-"}, false));
    }

    // Tests '--' token does not eat the rest of the arguments (defect: '--' not handled specially)
    @Test
    public void testFlatten_doubleHyphenToken_doesNotEatRest() {
        addOption("a", false);
        assertArrayEquals(new String[]{"--", "-a"}, parser.flatten(opts, new String[]{"--", "-a"}, false));
    }

    // Tests unknown long option without stop does not eat the rest (defect: unconditional processNonOptionToken)
    @Test
    public void testFlatten_unknownLongOption_noStop_doesNotEatRest() {
        addOption("a", false);
        assertArrayEquals(new String[]{"--unknown", "-a"}, parser.flatten(opts, new String[]{"--unknown", "-a"}, false));
    }

    // Tests unknown long option with stop eats the rest
    @Test
    public void testFlatten_unknownLongOption_stopAtNonOption_returnsProcessedNonOption() {
        addOption("a", false);
        assertArrayEquals(new String[]{"--", "--unknown", "-a"}, parser.flatten(opts, new String[]{"--unknown", "-a"}, true));
    }

    // Tests unknown long option with equals without stop does not eat the rest (defect)
    @Test
    public void testFlatten_unknownLongOptionWithEquals_noStop_doesNotEatRest() {
        addOption("a", false);
        assertArrayEquals(new String[]{"--unknown=value", "-a"}, parser.flatten(opts, new String[]{"--unknown=value", "-a"}, false));
    }

    // Tests unknown long option with equals with stop eats the rest
    @Test
    public void testFlatten_unknownLongOptionWithEquals_stopAtNonOption_returnsProcessedNonOption() {
        addOption("a", false);
        assertArrayEquals(new String[]{"--", "--unknown=value", "-a"}, parser.flatten(opts, new String[]{"--unknown=value", "-a"}, true));
    }

    // Tests burst token with unknown option without stop adds just the character (defect: adds whole token)
    @Test
    public void testFlatten_burstTokenUnknownOption_noStop_returnsBurstChar() {
        addOption("a", false);
        assertArrayEquals(new String[]{"-a", "-b"}, parser.flatten(opts, new String[]{"-abc"}, false));
    }

    // Tests burst token with unknown option with stop processes non-option
    @Test
    public void testFlatten_burstTokenUnknownOption_stopAtNonOption_returnsProcessedNonOption() {
        addOption("a", false);
        assertArrayEquals(new String[]{"-a", "--", "bc"}, parser.flatten(opts, new String[]{"-abc"}, true));
    }

    // Tests burst token with partially known options adds until unknown (defect)
    @Test
    public void testFlatten_burstTokenPartialKnownUnknown_noStop_returnsBurstUntilUnknown() {
        addOption("a", false);
        addOption("c", false);
        assertArrayEquals(new String[]{"-a", "-b"}, parser.flatten(opts, new String[]{"-abc"}, false));
    }

    // Tests burst token with multiple valid options
    @Test
    public void testFlatten_burstTokenMultipleOptions_returnsAllOptions() {
        addOption("a", false);
        addOption("b", false);
        addOption("c", false);
        assertArrayEquals(new String[]{"-a", "-b", "-c"}, parser.flatten(opts, new String[]{"-abc"}, false));
    }

    // Tests burst token with an option that takes an argument
    @Test
    public void testFlatten_burstTokenOptionWithArg_returnsOptionAndValue() {
        addOption("b", true);
        assertArrayEquals(new String[]{"-b", "az"}, parser.flatten(opts, new String[]{"-baz"}, false));
    }

    // Tests burst token where option has argument but no value left
    @Test
    public void testFlatten_burstTokenOptionHasArgNoValue_returnsOptionOnly() {
        addOption("a", true);
        assertArrayEquals(new String[]{"-a"}, parser.flatten(opts, new String[]{"-a"}, false));
    }

    // Tests unknown short option without stop returns the token
    @Test
    public void testFlatten_unknownShortOption_noStop_returnsToken() {
        assertArrayEquals(new String[]{"-b"}, parser.flatten(opts, new String[]{"-b"}, false));
    }

    // Tests unknown short option with stop eats the rest
    @Test
    public void testFlatten_unknownShortOption_stopAtNonOption_returnsTokenAndEatRest() {
        addOption("a", false);
        assertArrayEquals(new String[]{"-b", "-a"}, parser.flatten(opts, new String[]{"-b", "-a"}, true));
    }

    // Tests stopAtNonOption stops at the first non-option argument
    @Test
    public void testFlatten_stopAtNonOption_returnsArgsAfterFirstNonOption() {
        addOption("a", false);
        assertArrayEquals(new String[]{"-a", "--", "foo", "-b"}, parser.flatten(opts, new String[]{"-a", "foo", "-b"}, true));
    }

    // Tests a non-option token without stop
    @Test
    public void testFlatten_nonOption_returnsToken() {
        assertArrayEquals(new String[]{"foo"}, parser.flatten(opts, new String[]{"foo"}, false));
    }

    // Tests empty arguments array
    @Test
    public void testFlatten_emptyArguments_returnsEmptyArray() {
        assertArrayEquals(new String[0], parser.flatten(opts, new String[0], false));
    }

    // ========== New tests for uncovered coverage ==========

    // Tests short option with argument provided as separate token
    @Test
    public void testFlatten_shortOptionWithArg_returnsOptionAndValue() {
        addOption("a", true);
        assertArrayEquals(new String[]{"-a", "value"}, parser.flatten(opts, new String[]{"-a", "value"}, false));
    }

    // Tests long option with argument provided as separate token (not using equals)
    @Test
    public void testFlatten_longOptionWithArg_returnsOptionAndValue() {
        addLongOption("foo", true);
        assertArrayEquals(new String[]{"--foo", "value"}, parser.flatten(opts, new String[]{"--foo", "value"}, false));
    }

    // Tests double hyphen token with stopAtNonOption enabled
    @Test
    public void testFlatten_doubleHyphenTokenWithStop_returnsProcessedTokens() {
        addOption("a", false);
        assertArrayEquals(new String[]{"--", "-a"}, parser.flatten(opts, new String[]{"--", "-a"}, true));
    }

    // Tests burst token with multiple known options where the last option has an argument
    // and there is a remaining character that becomes the argument, and then a following
    // token is available (but not consumed because the argument is already provided).
    // Example: -abc where a and b have no arg, c has arg, token "abcd" -> c takes "d" as arg.
    @Test
    public void testFlatten_burstTokenMultipleKnownLastWithArgAndRemaining_returnsOptionsAndArg() {
        addOption("a", false);
        addOption("b", false);
        addOption("c", true);
        assertArrayEquals(new String[]{"-a", "-b", "-c", "d"}, parser.flatten(opts, new String[]{"-abcd"}, false));
    }

    // Tests burst token with multiple known options where the last option has an argument
    // but the token ends (no remaining chars), then the next token should be consumed as argument.
    @Test
    public void testFlatten_burstTokenMultipleKnownLastWithArgNoRemaining_nextTokenAsArg() {
        addOption("a", false);
        addOption("b", false);
        addOption("c", true);
        assertArrayEquals(new String[]{"-a", "-b", "-c", "value"}, parser.flatten(opts, new String[]{"-abc", "value"}, false));
    }

    // Tests short option with argument consuming the next token even if it looks like an option
    @Test
    public void testFlatten_shortOptionWithArgConsumesNextOptionAsArgument() {
        addOption("a", true);
        addOption("b", false);
        // a has arg, so "-b" should be consumed as the argument of -a
        assertArrayEquals(new String[]{"-a", "-b"}, parser.flatten(opts, new String[]{"-a", "-b"}, false));
    }
}