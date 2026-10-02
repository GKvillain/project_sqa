package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PosixParserTest {

    private PosixParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new PosixParser();
        options = new Options();
    }

    // Tests flattening a single long option
    @Test
    public void testFlatten_longOption_returnsFlattenedToken() {
        options.addOption("f", "foo", false, "foo option");
        String[] args = new String[]{"--foo"};
        String[] result = parser.flatten(options, args, false);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("--foo", result[0]);
    }

    // Tests flattening a long option with an equals sign separating the value
    @Test
    public void testFlatten_longOptionWithEquals_splitsIntoTwoTokens() {
        options.addOption(OptionBuilder.withLongOpt("foo").hasArg().create('f'));
        String[] args = new String[]{"--foo=bar"};
        String[] result = parser.flatten(options, args, false);

        assertEquals(2, result.length);
        assertEquals("--foo", result[0]);
        assertEquals("bar", result[1]);
    }

    // Tests single hyphen argument
    @Test
    public void testFlatten_singleHyphen_retainsSingleHyphenToken() {
        String[] args = new String[]{"-"};
        String[] result = parser.flatten(options, args, false);

        assertEquals(1, result.length);
        assertEquals("-", result[0]);
    }

    // Tests double hyphen alone without option name
    @Test
    public void testFlatten_doubleHyphen_retainsDoubleHyphen() {
        String[] args = new String[]{"--", "arg1"};
        String[] result = parser.flatten(options, args, false);

        assertEquals(2, result.length);
        assertEquals("--", result[0]);
        assertEquals("arg1", result[1]);
    }

    // Tests short option without argument
    @Test
    public void testFlatten_shortOptionWithoutArg_returnsOptionToken() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-a"};
        String[] result = parser.flatten(options, args, false);

        assertEquals(1, result.length);
        assertEquals("-a", result[0]);
    }

    // Tests short option with an argument value following it
    @Test
    public void testFlatten_shortOptionWithArg_returnsOptionAndArgTokens() {
        options.addOption("a", true, "option a with arg");
        String[] args = new String[]{"-a", "val"};
        String[] result = parser.flatten(options, args, true);

        assertEquals(2, result.length);
        assertEquals("-a", result[0]);
        assertEquals("val", result[1]);
    }

    // Tests bursting of clustered short options without arguments
    @Test
    public void testFlatten_burstMultipleShortOptions_splitsIntoIndividualTokens() {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        options.addOption("c", false, "option c");
        String[] args = new String[]{"-abc"};
        String[] result = parser.flatten(options, args, false);

        assertEquals(3, result.length);
        assertEquals("-a", result[0]);
        assertEquals("-b", result[1]);
        assertEquals("-c", result[2]);
    }

    // Tests bursting when an option with argument is followed by value in the same token
    @Test
    public void testFlatten_burstOptionWithAttachedValue_splitsOptionAndValue() {
        options.addOption("a", false, "option a");
        options.addOption("b", true, "option b with arg");
        String[] args = new String[]{"-abvalue"};
        String[] result = parser.flatten(options, args, false);

        assertEquals(3, result.length);
        assertEquals("-a", result[0]);
        assertEquals("-b", result[1]);
        assertEquals("value", result[2]);
    }

    // Tests stopAtNonOption when encountering an unrecognized short option token
    @Test
    public void testFlatten_unrecognizedShortOptionWithStopAtNonOption_stopsAndGobblesRemaining() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-z", "arg1", "arg2"};
        String[] result = parser.flatten(options, args, true);

        // When stopAtNonOption is true, invalid 2-char token triggers eatTheRest
        assertEquals(2, result.length);
        assertEquals("arg1", result[0]);
        assertEquals("arg2", result[1]);
    }

    // Tests burstToken when encountering an unrecognized character with stopAtNonOption true
    @Test
    public void testBurstToken_unrecognizedCharWithStopAtNonOption_insertsDoubleHyphenAndStops() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-az", "arg1"};
        String[] result = parser.flatten(options, args, true);

        assertEquals(4, result.length);
        assertEquals("-a", result[0]);
        assertEquals("--", result[1]);
        assertEquals("z", result[2]);
        assertEquals("arg1", result[3]);
    }

    // Tests burstToken when encountering an unrecognized character with stopAtNonOption false
    @Test
    public void testBurstToken_unrecognizedCharWithoutStopAtNonOption_addsFullToken() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-az"};
        String[] result = parser.flatten(options, args, false);

        assertEquals(2, result.length);
        assertEquals("-a", result[0]);
        assertEquals("-az", result[1]);
    }

    // Tests non-option token when stopAtNonOption is false
    @Test
    public void testFlatten_nonOptionWithoutStopAtNonOption_addsTokenDirectly() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"nonOption", "-a"};
        String[] result = parser.flatten(options, args, false);

        assertEquals(2, result.length);
        assertEquals("nonOption", result[0]);
        assertEquals("-a", result[1]);
    }

    // Tests non-option token when stopAtNonOption is true
    @Test
    public void testFlatten_nonOptionWithStopAtNonOption_addsDoubleHyphenAndRemainingTokens() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"nonOption", "-a", "extra"};
        String[] result = parser.flatten(options, args, true);

        assertEquals(4, result.length);
        assertEquals("--", result[0]);
        assertEquals("nonOption", result[1]);
        assertEquals("-a", result[2]);
        assertEquals("extra", result[3]);
    }

    // Tests multi-character option that matches an option name directly without bursting
    @Test
    public void testFlatten_multiCharOptionDirectMatch_retainsToken() {
        options.addOption("foo", false, "multi-char short option");
        String[] args = new String[]{"-foo"};
        String[] result = parser.flatten(options, args, false);

        assertEquals(1, result.length);
        assertEquals("-foo", result[0]);
    }

    // Tests parsing complete command line through parse method
    @Test
    public void testParse_standardCommandLine_parsesOptionsAndArgumentsCorrectly() throws ParseException {
        options.addOption("a", false, "option a");
        options.addOption("b", true, "option b");
        String[] args = new String[]{"-a", "-b", "foo", "extra1", "extra2"};

        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("foo", cl.getOptionValue("b"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("extra1", cl.getArgs()[0]);
        assertEquals("extra2", cl.getArgs()[1]);
    }

    // Tests parsing with stopAtNonOption enabled through parse method
    @Test
    public void testParse_stopAtNonOption_stopsOptionProcessingAtFirstNonOption() throws ParseException {
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

    // Tests multiple consecutive calls to ensure internal state is reset properly
    @Test
    public void testFlatten_consecutiveRuns_resetsInternalStateCorrectly() {
        options.addOption("a", true, "option a");
        String[] args1 = new String[]{"-a", "val1"};
        String[] result1 = parser.flatten(options, args1, false);

        assertEquals(2, result1.length);
        assertEquals("-a", result1[0]);
        assertEquals("val1", result1[1]);

        String[] args2 = new String[]{"-a", "val2"};
        String[] result2 = parser.flatten(options, args2, false);

        assertEquals(2, result2.length);
        assertEquals("-a", result2[0]);
        assertEquals("val2", result2[1]);
    }

    // Tests flattening empty arguments array
    @Test
    public void testFlatten_emptyArguments_returnsEmptyArray() {
        String[] args = new String[]{};
        String[] result = parser.flatten(options, args, false);

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    // Tests flattening multi-character unrecognized token with stopAtNonOption = true
    @Test
    public void testFlatten_multiCharUnrecognizedTokenWithStopAtNonOption_addsDoubleHyphenAndGobbles() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-unknown", "remaining1", "remaining2"};
        String[] result = parser.flatten(options, args, true);

        assertEquals(4, result.length);
        assertEquals("--", result[0]);
        assertEquals("-unknown", result[1]);
        assertEquals("remaining1", result[2]);
        assertEquals("remaining2", result[3]);
    }

    // Tests flattening when current option requires an argument and accepts an option-like value
    @Test
    public void testFlatten_optionExpectingArgFollowedByOptionLikeValue_assignsValueToOption() {
        options.addOption("a", true, "option a expecting arg");
        String[] args = new String[]{"-a", "-multiValue", "rest"};
        String[] result = parser.flatten(options, args, true);

        assertEquals(4, result.length);
        assertEquals("-a", result[0]);
        assertEquals("-multiValue", result[1]);
        assertEquals("--", result[2]);
        assertEquals("rest", result[3]);
    }

    // Tests bursting clustered options where an option requiring an argument is at the end of the cluster
    @Test
    public void testFlatten_burstClusterWithArgOptionAtEnd_assignsFollowingTokenAsArg() {
        options.addOption("a", false, "option a");
        options.addOption("b", true, "option b with arg");
        String[] args = new String[]{"-ab", "bValue"};
        String[] result = parser.flatten(options, args, false);

        assertEquals(3, result.length);
        assertEquals("-a", result[0]);
        assertEquals("-b", result[1]);
        assertEquals("bValue", result[2]);
    }

    // Tests option configured with multiple arguments under PosixParser
    @Test
    public void testFlatten_optionWithMultipleArgs_processesAllValues() {
        Option multiArgOption = OptionBuilder.hasArgs(2).create('m');
        options.addOption(multiArgOption);
        String[] args = new String[]{"-m", "val1", "val2"};
        String[] result = parser.flatten(options, args, true);

        assertEquals(3, result.length);
        assertEquals("-m", result[0]);
        assertEquals("val1", result[1]);
        assertEquals("val2", result[2]);
    }

    // Tests parsing with OptionGroup
    @Test
    public void testParse_withOptionGroup_setsSelectedOption() throws ParseException {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "option A"));
        group.addOption(new Option("b", "option B"));
        options.addOptionGroup(group);

        CommandLine cl = parser.parse(options, new String[]{"-b"});
        assertTrue(cl.hasOption("b"));
        assertFalse(cl.hasOption("a"));
    }

    // Tests unrecognized option throwing UnrecognizedOptionException during parse
    @Test(expected = UnrecognizedOptionException.class)
    public void testParse_unrecognizedOption_throwsUnrecognizedOptionException() throws ParseException {
        options.addOption("a", false, "option a");
        parser.parse(options, new String[]{"-z"});
    }
}