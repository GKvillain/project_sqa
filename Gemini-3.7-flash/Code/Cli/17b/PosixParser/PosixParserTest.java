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

    // Tests flattening long option with value assigned using '='
    @Test
    public void testFlatten_longOptionWithEquals_splitsIntoKeyAndValue() {
        options.addOption(OptionBuilder.withLongOpt("foo").hasArg().create('f'));
        String[] args = new String[]{"--foo=bar"};

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[]{"--foo", "bar"}, result);
    }

    // Tests flattening plain double hyphen token '--'
    @Test
    public void testFlatten_doubleHyphenToken_retainedAsToken() {
        String[] args = new String[]{"--", "arg1", "arg2"};

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[]{"--", "arg1", "arg2"}, result);
    }

    // Tests flattening single hyphen token '-'
    @Test
    public void testFlatten_singleHyphenToken_retainedAsToken() {
        String[] args = new String[]{"-"};

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[]{"-"}, result);
    }

    // Tests flattening valid single character option
    @Test
    public void testFlatten_validSingleOption_addsToken() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-a"};

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[]{"-a"}, result);
    }

    // Tests flattening unrecognized two-character option when stopAtNonOption is true
    @Test
    public void testFlatten_unrecognizedTwoCharOptionStopAtNonOptionTrue_stopsProcessing() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-z", "extra1", "extra2"};

        String[] result = parser.flatten(options, args, true);

        assertArrayEquals(new String[]{"extra1", "extra2"}, result);
    }

    // Tests flattening unrecognized two-character option when stopAtNonOption is false
    @Test
    public void testFlatten_unrecognizedTwoCharOptionStopAtNonOptionFalse_ignoresToken() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-z", "-a"};

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[]{"-a"}, result);
    }

    // Tests flattening multi-character option defined directly in Options
    @Test
    public void testFlatten_multiCharOptionDefinedDirectly_addedWithoutBursting() {
        options.addOption("foo", false, "option foo");
        String[] args = new String[]{"-foo"};

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[]{"-foo"}, result);
    }

    // Tests bursting multiple single-character boolean options combined into one token
    @Test
    public void testFlatten_burstMultipleBooleanOptions_separatedIntoIndividualOptions() {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        options.addOption("c", false, "option c");
        String[] args = new String[]{"-abc"};

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[]{"-a", "-b", "-c"}, result);
    }

    // Tests bursting an option with an argument value attached directly
    @Test
    public void testFlatten_burstOptionWithArgAttached_splitsOptionAndArgument() {
        options.addOption(OptionBuilder.hasArg().create('a'));
        String[] args = new String[]{"-afoo"};

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[]{"-a", "foo"}, result);
    }

    // Tests bursting token encountering non-option character with stopAtNonOption true (Cli-17 defect test)
    @Test
    public void testFlatten_burstOptionWithNonOptionStopAtNonOptionTrue_stopsAndAppendsRemaining() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-ab", "c"};

        String[] result = parser.flatten(options, args, true);

        assertArrayEquals(new String[]{"-a", "--", "b", "c"}, result);
    }

    // Tests bursting token encountering non-option character with stopAtNonOption false
    @Test
    public void testFlatten_burstOptionWithNonOptionStopAtNonOptionFalse_keepsTokenIntact() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-xyz"};

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[]{"-xyz"}, result);
    }

    // Tests flattening non-option tokens when stopAtNonOption is false
    @Test
    public void testFlatten_nonOptionTokensStopAtNonOptionFalse_addsAllTokens() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"foo", "-a", "bar"};

        String[] result = parser.flatten(options, args, false);

        assertArrayEquals(new String[]{"foo", "-a", "bar"}, result);
    }

    // Tests flattening non-option token fulfilling argument of preceding option with stopAtNonOption true
    @Test
    public void testFlatten_nonOptionTokenConsumesPrecedingOptionArg_addsArgument() {
        options.addOption(OptionBuilder.hasArg().create('a'));
        String[] args = new String[]{"-a", "val", "remaining"};

        String[] result = parser.flatten(options, args, true);

        assertArrayEquals(new String[]{"-a", "val", "--", "remaining"}, result);
    }

    // Tests flattening non-option token with no preceding option when stopAtNonOption is true
    @Test
    public void testFlatten_nonOptionTokenNoPrecedingOptionStopAtNonOptionTrue_addsSeparatorAndRest() {
        String[] args = new String[]{"nonOption1", "nonOption2"};

        String[] result = parser.flatten(options, args, true);

        assertArrayEquals(new String[]{"--", "nonOption1", "nonOption2"}, result);
    }

    // Tests full parse workflow through CommandLineParser interface
    @Test
    public void testParse_standardOptionsAndArgs_returnsParsedCommandLine() throws ParseException {
        options.addOption("a", false, "option a");
        options.addOption(OptionBuilder.hasArg().create('b'));
        String[] args = new String[]{"-a", "-b", "value", "extra"};

        CommandLine cl = parser.parse(options, args);

        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("value", cl.getOptionValue("b"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("extra", cl.getArgs()[0]);
    }
}