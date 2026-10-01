package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test class for PosixParser.
 */
public class PosixParserTest {

    // Tests normal case: simple long option "--foo"
    @Test
    public void testFlatten_longOptionWithoutEqual_signleLongOption() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("f", "foo", false, "desc");
        String[] result = parser.flatten(options, new String[]{"--foo"}, false);
        assertArrayEquals(new String[]{"--foo"}, result);
    }

    // Tests normal case: long option with value using "="
    @Test
    public void testFlatten_longOptionWithEqual_keyValuePair() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("f", "foo", true, "desc");
        String[] result = parser.flatten(options, new String[]{"--foo=bar"}, false);
        assertArrayEquals(new String[]{"--foo", "bar"}, result);
    }

    // Tests boundary: single hyphen "-" token
    @Test
    public void testFlatten_singleHyphen_tokenAdded() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        String[] result = parser.flatten(options, new String[]{"-"}, false);
        assertArrayEquals(new String[]{"-"}, result);
    }

    // Tests normal case: short option with two characters "-a"
    @Test
    public void testFlatten_shortOptionValid_optionTokenAdded() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        String[] result = parser.flatten(options, new String[]{"-a"}, false);
        assertArrayEquals(new String[]{"-a"}, result);
    }

    // Tests edge case: short option not valid with stopAtNonOption true
    @Test
    public void testFlatten_shortOptionInvalidStopAtNonOption_remainingTokensConsumed() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        String[] result = parser.flatten(options, new String[]{"-x", "other"}, true);
        assertArrayEquals(new String[]{"--", "-x", "other"}, result);
    }

    // Tests edge case: short option not valid with stopAtNonOption false
    @Test
    public void testFlatten_shortOptionInvalidStopAtNonOptionFalse_optionAdded() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        String[] result = parser.flatten(options, new String[]{"-x"}, false);
        assertArrayEquals(new String[]{"-x"}, result);
    }

    // Tests normal case: long option (more than 2 chars) that is valid without bursting
    @Test
    public void testFlatten_longerTokenValidOption_tokenAdded() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("abc", true, "desc");
        String[] result = parser.flatten(options, new String[]{"-abc"}, false);
        assertArrayEquals(new String[]{"-abc"}, result);
    }

    // Tests bursting: "-abc" where "a","b","c" are options
    @Test
    public void testFlatten_burstTokenAllOptions_eachBurst() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        options.addOption("b", false, "desc");
        options.addOption("c", false, "desc");
        String[] result = parser.flatten(options, new String[]{"-abc"}, false);
        assertArrayEquals(new String[]{"-a", "-b", "-c"}, result);
    }

    // Tests bursting with argument: "-a" as option with arg, remaining "bc" as value
    @Test
    public void testFlatten_burstOptionWithArg_remainingIsArg() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", true, "desc");
        options.addOption("b", false, "desc");
        String[] result = parser.flatten(options, new String[]{"-abc"}, false);
        assertArrayEquals(new String[]{"-a", "bc"}, result);
    }

    // Tests bursting with stopAtNonOption: token starts with non-option
    @Test
    public void testFlatten_burstNonOptionStopAtNonOption_remainingAndRestGobbled() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        String[] result = parser.flatten(options, new String[]{"-ab", "other"}, true);
        assertArrayEquals(new String[]{"-a", "--", "b", "other"}, result);
    }

    // Tests non-option token with stopAtNonOption true
    @Test
    public void testFlatten_nonOptionStopAtNonOption_remainingTokensGobbled() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        String[] result = parser.flatten(options, new String[]{"some", "value"}, true);
        assertArrayEquals(new String[]{"--", "some", "value"}, result);
    }

    // Tests non-option token with stopAtNonOption false
    @Test
    public void testFlatten_nonOptionStopAtNonOptionFalse_addedDirectly() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        String[] result = parser.flatten(options, new String[]{"some", "value"}, false);
        assertArrayEquals(new String[]{"some", "value"}, result);
    }

    // Tests option with argument following (currentOption set via processOptionToken)
    @Test
    public void testFlatten_optionWithArgFollowingToken_argConsumed() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", true, "desc");
        String[] result = parser.flatten(options, new String[]{"-a", "value"}, false);
        assertArrayEquals(new String[]{"-a", "value"}, result);
    }

    // Tests the "gobble" mechanism when eatTheRest becomes true from process()
    @Test
    public void testFlatten_nonOptionAfterStopAtNonOption_remainingGobbled() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        // first token is not an option, stopAtNonOption is true -> triggers process -> eatTheRest true
        String[] result = parser.flatten(options, new String[]{"x", "y", "z"}, true);
        assertArrayEquals(new String[]{"--", "x", "y", "z"}, result);
    }

    // Tests the "--" token (double hyphen) is added as is
    @Test
    public void testFlatten_doubleHyphen_tokenAddedDirectly() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        String[] result = parser.flatten(options, new String[]{"--", "foo"}, false);
        assertArrayEquals(new String[]{"--", "foo"}, result);
    }

    // Tests edge: empty arguments array
    @Test
    public void testFlatten_emptyArguments_returnsEmptyArray() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        String[] result = parser.flatten(options, new String[]{}, false);
        assertArrayEquals(new String[]{}, result);
    }

    // Tests edge: null argument in array (should be handled as string)
    @Test(expected = NullPointerException.class)
    public void testFlatten_nullToken_throwsNullPointer() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        parser.flatten(options, new String[]{null}, false);
    }

    // ====================== NEW TEST CASES FOR UNCOVERED AREAS ======================

    // Tests bursting with non-option and stopAtNonOption false: "-ab" where a is bool, b not option
    @Test
    public void testFlatten_burstNonOptionStopAtNonOptionFalse() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        // 'b' is not a defined option, stopAtNonOption false -> continue adding remaining as token
        String[] result = parser.flatten(options, new String[]{"-ab"}, false);
        assertArrayEquals(new String[]{"-a", "b"}, result);
    }

    // Tests long option with '=' but the option does not take an argument
    @Test
    public void testFlatten_longOptionWithEqualButOptionNoArg() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("f", "foo", false, "desc"); // no argument
        // flatten should still split into "--foo" and "bar"
        String[] result = parser.flatten(options, new String[]{"--foo=bar"}, false);
        assertArrayEquals(new String[]{"--foo", "bar"}, result);
    }

    // Tests option with argument and the next token is an option (e.g., -a -b)
    @Test
    public void testFlatten_optionWithArgAndNextTokenIsOption() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", true, "desc"); // takes argument
        options.addOption("b", false, "desc");
        // "-b" should be treated as argument for "-a"
        String[] result = parser.flatten(options, new String[]{"-a", "-b"}, false);
        assertArrayEquals(new String[]{"-a", "-b"}, result);
    }

    // Tests double hyphen token with stopAtNonOption true
    @Test
    public void testFlatten_doubleHyphenStopAtNonOptionTrue() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        // "--" causes eatTheRest = true, remaining tokens added as-is
        String[] result = parser.flatten(options, new String[]{"--", "foo"}, true);
        assertArrayEquals(new String[]{"--", "foo"}, result);
    }

    // Tests single hyphen token with stopAtNonOption true
    @Test
    public void testFlatten_singleHyphenStopAtNonOptionTrue() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        String[] result = parser.flatten(options, new String[]{"-", "foo"}, true);
        // "-" added, then "foo" is a non-option -> stopAtNonOption triggers "--" and then gobble?
        // Expected: "-", "--", "foo"
        assertArrayEquals(new String[]{"-", "--", "foo"}, result);
    }

    // Tests empty string token (should be added as-is)
    @Test
    public void testFlatten_emptyStringToken() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        String[] result = parser.flatten(options, new String[]{""}, false);
        assertArrayEquals(new String[]{""}, result);
    }
}