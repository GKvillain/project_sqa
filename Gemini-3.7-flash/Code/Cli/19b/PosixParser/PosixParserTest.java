package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;

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
    public void testFlatten_singleHyphenOptions_flattenedCorrectly() {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        String[] args = new String[] { "-a", "-b" };

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[] { "-a", "-b" }, result);
    }

    // Tests flattening long options without value
    @Test
    public void testFlatten_longOption_flattenedCorrectly() {
        options.addOption("foo", false, "foo option");
        String[] args = new String[] { "--foo" };

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[] { "--foo" }, result);
    }

    // Tests flattening long options with '=' delimiter
    @Test
    public void testFlatten_longOptionWithEquals_splitIntoTwoTokens() {
        options.addOption("foo", true, "foo option");
        String[] args = new String[] { "--foo=bar" };

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[] { "--foo", "bar" }, result);
    }

    // Tests flattening single hyphen '-' token
    @Test
    public void testFlatten_singleHyphen_passedAsIs() {
        String[] args = new String[] { "-" };

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[] { "-" }, result);
    }

    // Tests flattening double hyphen '--' token
    @Test
    public void testFlatten_doubleHyphen_passedAsIs() {
        String[] args = new String[] { "--" };

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[] { "--" }, result);
    }

    // Tests bursting multiple clustered single-char options
    @Test
    public void testFlatten_burstTokenMultipleOptions_burstsCorrectly() {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        options.addOption("c", false, "option c");
        String[] args = new String[] { "-abc" };

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[] { "-a", "-b", "-c" }, result);
    }

    // Tests bursting option with attached argument value
    @Test
    public void testFlatten_burstTokenWithAttachedArg_separatesOptionAndArg() {
        options.addOption("f", true, "file option");
        String[] args = new String[] { "-fFilename.txt" };

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[] { "-f", "Filename.txt" }, result);
    }

    // Tests stopAtNonOption=true with regular non-option argument
    @Test
    public void testFlatten_stopAtNonOptionWithArgument_addsDoubleHyphenAndGobbles() {
        options.addOption("a", false, "option a");
        String[] args = new String[] { "-a", "nonOption1", "nonOption2" };

        String[] result = parser.flatten(options, args, true);

        assertArrayEquals(new String[] { "-a", "--", "nonOption1", "nonOption2" }, result);
    }

    // Tests stopAtNonOption=false with non-option arguments
    @Test
    public void testFlatten_stopAtNonOptionFalseWithArgument_keepsArguments() {
        options.addOption("a", false, "option a");
        String[] args = new String[] { "-a", "nonOption1", "nonOption2" };

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[] { "-a", "nonOption1", "nonOption2" }, result);
    }

    // Tests unrecognized 2-char option with stopAtNonOption=true
    @Test
    public void testFlatten_unrecognizedOptionStopAtNonOptionTrue_stopsAndGobbles() {
        options.addOption("a", false, "option a");
        String[] args = new String[] { "-a", "-z", "extra" };

        String[] result = parser.flatten(options, args, true);

        assertArrayEquals(new String[] { "-a", "-z", "extra" }, result);
    }

    // Tests unrecognized 2-char option with stopAtNonOption=false
    @Test
    public void testFlatten_unrecognizedOptionStopAtNonOptionFalse_ignored() {
        options.addOption("a", false, "option a");
        String[] args = new String[] { "-a", "-z", "extra" };

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[] { "-a", "extra" }, result);
    }

    // Tests bursting with unrecognized char when stopAtNonOption=true
    @Test
    public void testFlatten_burstUnrecognizedCharStopAtNonOptionTrue_stopsProcessing() {
        options.addOption("a", false, "option a");
        String[] args = new String[] { "-az", "extra" };

        String[] result = parser.flatten(options, args, true);

        assertArrayEquals(new String[] { "-a", "--", "z", "extra" }, result);
    }

    // Tests bursting with unrecognized char when stopAtNonOption=false
    @Test
    public void testFlatten_burstUnrecognizedCharStopAtNonOptionFalse_addsRawToken() {
        options.addOption("a", false, "option a");
        String[] args = new String[] { "-az" };

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[] { "-a", "-az" }, result);
    }

    // Tests option with argument when stopAtNonOption=true
    @Test
    public void testFlatten_optionWithArgFollowedByArgStopAtNonOptionTrue_processesCorrectly() {
        options.addOption("f", true, "file option");
        String[] args = new String[] { "-f", "myFile.txt", "extra" };

        String[] result = parser.flatten(options, args, true);

        assertArrayEquals(new String[] { "-f", "myFile.txt", "--", "extra" }, result);
    }

    // Tests long option defined with single hyphen
    @Test
    public void testFlatten_singleHyphenLongOption_recognizedDirectly() {
        options.addOption("foo", false, "foo option");
        String[] args = new String[] { "-foo" };

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[] { "-foo" }, result);
    }

    // Tests empty arguments array
    @Test
    public void testFlatten_emptyArguments_returnsEmptyArray() {
        String[] args = new String[0];

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[0], result);
    }

    // Tests unrecognized long option with stopAtNonOption=true
    @Test
    public void testFlatten_unrecognizedLongOptionStopAtNonOptionTrue_addsDoubleHyphenAndGobbles() {
        options.addOption("a", false, "option a");
        String[] args = new String[] { "-a", "--unknown", "extra" };

        String[] result = parser.flatten(options, args, true);

        assertArrayEquals(new String[] { "-a", "--", "--unknown", "extra" }, result);
    }

    // Tests unrecognized long option with stopAtNonOption=false
    @Test
    public void testFlatten_unrecognizedLongOptionStopAtNonOptionFalse_appendsToken() {
        options.addOption("a", false, "option a");
        String[] args = new String[] { "-a", "--unknown", "extra" };

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[] { "-a", "--unknown", "extra" }, result);
    }

    // Tests unrecognized long option with '=' delimiter and stopAtNonOption=true
    @Test
    public void testFlatten_unrecognizedLongOptionWithEqualsStopAtNonOptionTrue_stopsAndGobbles() {
        options.addOption("a", false, "option a");
        String[] args = new String[] { "-a", "--unknown=value", "extra" };

        String[] result = parser.flatten(options, args, true);

        assertArrayEquals(new String[] { "-a", "--", "--unknown=value", "extra" }, result);
    }

    // Tests unrecognized long option with '=' delimiter and stopAtNonOption=false
    @Test
    public void testFlatten_unrecognizedLongOptionWithEqualsStopAtNonOptionFalse_appendsToken() {
        options.addOption("a", false, "option a");
        String[] args = new String[] { "-a", "--unknown=value", "extra" };

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[] { "-a", "--unknown=value", "extra" }, result);
    }

    // Tests double hyphen '--' followed by additional arguments
    @Test
    public void testFlatten_doubleHyphenFollowedByArguments_gobblesRemainingArgs() {
        options.addOption("a", false, "option a");
        String[] args = new String[] { "-a", "--", "-b", "foo" };

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[] { "-a", "--", "-b", "foo" }, result);
    }

    // Tests multiple invocations on the same parser instance to verify state reset
    @Test
    public void testFlatten_multipleInvocations_resetsInternalStateCorrectly() {
        options.addOption("a", false, "option a");
        String[] firstArgs = new String[] { "-a", "arg1", "arg2" };
        String[] secondArgs = new String[] { "-a" };

        String[] firstResult = parser.flatten(options, firstArgs, true);
        assertArrayEquals(new String[] { "-a", "--", "arg1", "arg2" }, firstResult);

        String[] secondResult = parser.flatten(options, secondArgs, false);
        assertArrayEquals(new String[] { "-a" }, secondResult);
    }

    // Tests bursting with option taking arg at end of cluster without attached value
    @Test
    public void testFlatten_burstTokenWithArgOptionAtEnd_burstsCorrectly() {
        options.addOption("a", false, "option a");
        options.addOption("f", true, "file option");
        String[] args = new String[] { "-af", "file.txt" };

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[] { "-a", "-f", "file.txt" }, result);
    }
}