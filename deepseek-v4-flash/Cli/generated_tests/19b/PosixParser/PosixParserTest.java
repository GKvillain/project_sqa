package org.apache.commons.cli;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PosixParserTest {

    private Options options;
    private PosixParser parser;

    @Before
    public void setUp() {
        options = new Options();
        parser = new PosixParser();
    }

    // Tests normal case: short option with argument
    @Test
    public void testFlatten_shortOptionWithArgument_returnsTokens() {
        options.addOption("a", true, "arg option");
        String[] args = {"-a", "foo"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "foo"}, result);
    }

    // Tests normal case: long option with equals sign
    @Test
    public void testFlatten_longOptionWithEquals_returnsSplitTokens() {
        options.addOption("b", "bar", true, "arg option");
        String[] args = {"--bar=value"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--bar", "value"}, result);
    }

    // Tests boundary case: single hyphen token
    @Test
    public void testFlatten_singleHyphen_returnsHyphen() {
        String[] args = {"-"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-"}, result);
    }

    // Tests normal case: burst short option without argument
    @Test
    public void testFlatten_burstShortOptions_returnsExpandedTokens() {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        String[] args = {"-ab"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-b"}, result);
    }

    // Tests boundary case: burst short option with argument at end of token
    @Test
    public void testFlatten_burstShortOptionWithArgument_returnsTokensWithArg() {
        options.addOption("a", true, "arg option");
        String[] args = {"-afoo"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "foo"}, result);
    }

    // Tests edge case: stopAtNonOption true with non-option starting with '-'
    @Test
    public void testFlatten_stopAtNonOptionWithInvalidOption_returnsRestEaten() {
        String[] args = {"-a", "foo"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "foo"}, result);
    }

    // Tests edge case: stopAtNonOption true with non-option token
    @Test
    public void testFlatten_stopAtNonOptionWithNonOption_returnsDashDashAndValue() {
        String[] args = {"foo", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "bar"}, result);
    }

    // Tests edge case: stopAtNonOption true with burst and invalid option
    @Test
    public void testFlatten_burstTokenWithStopAtNonOptionAndInvalidOption_returnsRestEaten() {
        options.addOption("a", false, "option a");
        String[] args = {"-ac", "foo"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "c", "foo"}, result);
    }

    // Tests normal case: long option without argument
    @Test
    public void testFlatten_longOptionWithoutArg_returnsToken() {
        options.addOption("c", "ccc", false, "option c");
        String[] args = {"--ccc"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--ccc"}, result);
    }

    // Tests edge case: double dash stop marker
    @Test
    public void testFlatten_doubleDash_returnsDoubleDashAlone() {
        String[] args = {"--", "foo"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--", "foo"}, result);
    }

    // Tests boundary case: long option with equals but empty value
    @Test
    public void testFlatten_longOptionWithEmptyEqualsValue_returnsTokens() {
        options.addOption("d", "ddd", true, "arg option");
        String[] args = {"--ddd="};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--ddd", ""}, result);
    }

    // Tests normal case: two character option token directly
    @Test
    public void testFlatten_twoCharOption_returnsToken() {
        options.addOption("ab", false, "two char option");
        String[] args = {"-ab"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-ab"}, result);
    }

    // Tests branch: burst token with invalid option, no stopAtNonOption
    @Test
    public void testFlatten_burstTokenInvalidOptionNoStop_returnsTokenAsIs() {
        options.addOption("a", false, "option a");
        String[] args = {"-az"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-z"}, result);
    }

    // ========== New test cases for uncovered coverage ==========

    // 1. long option with separate argument (not via '=')
    @Test
    public void testFlatten_longOptionWithArgumentSeparate() {
        options.addOption("b", "bar", true, "arg option");
        String[] args = {"--bar", "value"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--bar", "value"}, result);
    }

    // 2. long option without argument, next token is non-option
    @Test
    public void testFlatten_longOptionWithoutArgumentWithNextNonOption() {
        options.addOption("c", "ccc", false, "no arg");
        String[] args = {"--ccc", "foo"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--ccc", "foo"}, result);
    }

    // 3. invalid short option (single character, not defined), no stop
    @Test
    public void testFlatten_invalidShortOptionNoStop() {
        String[] args = {"-z"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-z"}, result);
    }

    // 4. invalid short option with stopAtNonOption true
    @Test
    public void testFlatten_invalidShortOptionWithStop() {
        String[] args = {"-z", "foo"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "--", "foo"}, result);
    }

    // 5. invalid long option (not defined) without '='
    @Test
    public void testFlatten_invalidLongOptionWithoutEquals() {
        String[] args = {"--unknown"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--unknown"}, result);
    }

    // 6. invalid long option (not defined) with '='
    @Test
    public void testFlatten_invalidLongOptionWithEquals() {
        String[] args = {"--unknown=value"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--unknown", "value"}, result);
    }

    // 7. empty args array
    @Test
    public void testFlatten_emptyArgs() {
        String[] args = {};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{}, result);
    }

    // 8. non-option token (no dash) with stopAtNonOption false
    @Test
    public void testFlatten_nonOptionNoStop() {
        String[] args = {"foo"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo"}, result);
    }

    // 9. short option with argument and stopAtNonOption true
    @Test
    public void testFlatten_shortOptionWithArgumentAndStopAtNonOption() {
        options.addOption("a", true, "arg option");
        String[] args = {"-a", "value"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "value"}, result);
    }

    // 10. burst short option with argument missing (no next token)
    @Test
    public void testFlatten_burstShortOptionWithArgumentMissing() {
        options.addOption("a", true, "arg option");
        String[] args = {"-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a"}, result);
    }

    // 11. long option with argument and stopAtNonOption true
    @Test
    public void testFlatten_longOptionWithArgumentAndStopAtNonOption() {
        options.addOption("b", "bar", true, "arg option");
        String[] args = {"--bar", "value"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--bar", "value"}, result);
    }
}