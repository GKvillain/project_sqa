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

    // Tests empty arguments array
    @Test
    public void testFlatten_emptyArguments_returnsEmptyArray() {
        String[] args = new String[0];
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[0], result);
    }

    // Tests single hyphen argument
    @Test
    public void testFlatten_singleHyphen_preservedAsToken() {
        String[] args = new String[] { "-" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "-" }, result);
    }

    // Tests long option without value
    @Test
    public void testFlatten_longOptionWithoutEquals_returnsLongOption() {
        options.addOption("f", "foo", false, "foo option");
        String[] args = new String[] { "--foo" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "--foo" }, result);
    }

    // Tests long option with equals sign split into option and value
    @Test
    public void testFlatten_longOptionWithEquals_splitsTokenAndValue() {
        options.addOption("f", "foo", true, "foo option");
        String[] args = new String[] { "--foo=bar" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "--foo", "bar" }, result);
    }

    // Tests double hyphen stops option processing
    @Test
    public void testFlatten_doubleHyphen_preservedInTokens() {
        String[] args = new String[] { "--", "-a", "arg" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "--", "-a", "arg" }, result);
    }

    // Tests valid single-character option
    @Test
    public void testFlatten_validShortOption_setsCurrentOption() {
        options.addOption("a", false, "option a");
        String[] args = new String[] { "-a" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "-a" }, result);
    }

    // Tests invalid short option with stopAtNonOption false
    @Test
    public void testFlatten_invalidShortOptionNoStopAtNonOption_addsToken() {
        String[] args = new String[] { "-z" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "-z" }, result);
    }

    // Tests invalid short option with stopAtNonOption true triggers gobble
    @Test
    public void testFlatten_invalidShortOptionStopAtNonOption_eatsTheRest() {
        String[] args = new String[] { "-z", "remaining1", "remaining2" };
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] { "-z", "remaining1", "remaining2" }, result);
    }

    // Tests option token recognized directly in options
    @Test
    public void testFlatten_recognizedMultiCharOption_addedDirectly() {
        options.addOption("foo", false, "multi-char option");
        String[] args = new String[] { "-foo" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "-foo" }, result);
    }

    // Tests bursting multiple bundled single-char options
    @Test
    public void testFlatten_burstMultipleShortOptions_burstsIntoSeparateTokens() {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        options.addOption("c", false, "option c");
        String[] args = new String[] { "-abc" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "-a", "-b", "-c" }, result);
    }

    // Tests bursting short option that accepts an argument attached to it
    @Test
    public void testFlatten_burstOptionWithAttachedArgument_separatesArg() {
        options.addOption("a", false, "option a");
        options.addOption("b", true, "option b with arg");
        String[] args = new String[] { "-abfoo" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "-a", "-b", "foo" }, result);
    }

    // Tests bursting with unknown option and stopAtNonOption false
    @Test
    public void testFlatten_burstUnknownOptionNoStopAtNonOption_addsRemainingAsSingleToken() {
        options.addOption("a", false, "option a");
        String[] args = new String[] { "-az" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "-a", "-az" }, result);
    }

    // Tests bursting with unknown option and stopAtNonOption true
    @Test
    public void testFlatten_burstUnknownOptionStopAtNonOption_addsDoubleHyphenAndEatsRest() {
        options.addOption("a", false, "option a");
        String[] args = new String[] { "-az", "extra" };
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] { "-a", "--", "z", "extra" }, result);
    }

    // Tests non-option argument following an option that requires an argument
    @Test
    public void testFlatten_optionExpectingArgFollowedByValue_passesArg() {
        options.addOption("a", true, "option a with arg");
        String[] args = new String[] { "-a", "val", "nonOption" };
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] { "-a", "val", "--", "nonOption" }, result);
    }

    // Tests non-option argument when stopAtNonOption is false
    @Test
    public void testFlatten_nonOptionNoStopAtNonOption_addsToken() {
        String[] args = new String[] { "nonOption1", "nonOption2" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "nonOption1", "nonOption2" }, result);
    }

    // Tests non-option argument when stopAtNonOption is true
    @Test
    public void testFlatten_nonOptionStopAtNonOption_insertsDoubleHyphenAndEatsRest() {
        String[] args = new String[] { "nonOption1", "nonOption2" };
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] { "--", "nonOption1", "nonOption2" }, result);
    }

    // Tests parser reusability and state reset in init()
    @Test
    public void testFlatten_consecutiveCalls_resetsInternalState() {
        options.addOption("a", true, "option a with arg");
        String[] args1 = new String[] { "-a", "foo" };
        String[] result1 = parser.flatten(options, args1, false);
        assertArrayEquals(new String[] { "-a", "foo" }, result1);

        String[] args2 = new String[] { "standalone" };
        String[] result2 = parser.flatten(options, args2, false);
        assertArrayEquals(new String[] { "standalone" }, result2);
    }

    // Tests unrecognized long option with equals sign and stopAtNonOption true
    @Test
    public void testFlatten_unrecognizedLongOptionWithEqualsStopAtNonOption_eatsTheRest() {
        String[] args = new String[] { "--unknown=value", "extra" };
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] { "--unknown=value", "extra" }, result);
    }

    // Tests unrecognized long option with equals sign and stopAtNonOption false
    @Test
    public void testFlatten_unrecognizedLongOptionWithEqualsNoStopAtNonOption_addsToken() {
        String[] args = new String[] { "--unknown=value" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "--unknown=value" }, result);
    }

    // Tests unrecognized long option without equals sign and stopAtNonOption true
    @Test
    public void testFlatten_unrecognizedLongOptionWithoutEqualsStopAtNonOption_eatsTheRest() {
        String[] args = new String[] { "--unknown", "extra1", "extra2" };
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] { "--unknown", "extra1", "extra2" }, result);
    }

    // Tests unrecognized long option without equals sign and stopAtNonOption false
    @Test
    public void testFlatten_unrecognizedLongOptionWithoutEqualsNoStopAtNonOption_addsToken() {
        String[] args = new String[] { "--unknown" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "--unknown" }, result);
    }

    // Tests bursting where last character in bundle is an option requiring an argument
    @Test
    public void testFlatten_burstLastOptionRequiresArgument_keepsOptionWaitingForNextToken() {
        options.addOption("a", false, "option a");
        options.addOption("b", true, "option b with arg");
        String[] args = new String[] { "-ab", "val", "nonOption" };
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] { "-a", "-b", "val", "--", "nonOption" }, result);
    }

    // Tests bursting when first character is unknown with stopAtNonOption true
    @Test
    public void testFlatten_burstFirstCharacterUnknownStopAtNonOption_addsDoubleHyphenAndEatsRest() {
        String[] args = new String[] { "-xyz", "extra" };
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] { "--", "-xyz", "extra" }, result);
    }

    // Tests bursting when first character is unknown with stopAtNonOption false
    @Test
    public void testFlatten_burstFirstCharacterUnknownNoStopAtNonOption_addsWholeToken() {
        String[] args = new String[] { "-xyz" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "-xyz" }, result);
    }
}