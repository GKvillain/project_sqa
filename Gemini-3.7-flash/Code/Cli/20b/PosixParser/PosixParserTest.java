package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link PosixParser}.
 */
public class PosixParserTest {

    private PosixParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new PosixParser();
        options = new Options();
    }

    // Tests flattening with empty arguments
    @Test
    public void testFlatten_emptyArguments_returnsEmptyArray() throws Exception {
        String[] args = new String[0];
        String[] result = parser.flatten(options, args, false);
        assertEquals(0, result.length);
    }

    // Tests flattening with long option without value
    @Test
    public void testFlatten_longOption_returnsLongOptionToken() throws Exception {
        options.addOption("f", "foo", false, "foo option");
        String[] args = new String[]{"--foo"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--foo"}, result);
    }

    // Tests flattening with long option containing equals sign
    @Test
    public void testFlatten_longOptionWithEquals_splitsIntoTwoTokens() throws Exception {
        options.addOption("f", "foo", true, "foo option");
        String[] args = new String[]{"--foo=bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--foo", "bar"}, result);
    }

    // Tests single hyphen argument
    @Test
    public void testFlatten_singleHyphen_returnsHyphenToken() throws Exception {
        String[] args = new String[]{"-"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-"}, result);
    }

    // Tests recognized single-character option
    @Test
    public void testFlatten_validSingleCharOption_returnsOptionToken() throws Exception {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a"}, result);
    }

    // Tests unrecognized single-character option with stopAtNonOption false
    @Test
    public void testFlatten_unrecognizedSingleCharOptionNoStop_returnsOptionToken() throws Exception {
        String[] args = new String[]{"-z"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-z"}, result);
    }

    // Tests unrecognized single-character option with stopAtNonOption true
    @Test
    public void testFlatten_unrecognizedSingleCharOptionStopAtNonOption_gobblesRemainingTokens() throws Exception {
        String[] args = new String[]{"-z", "extra1", "extra2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "extra1", "extra2"}, result);
    }

    // Tests multi-character option defined in Options
    @Test
    public void testFlatten_multiCharOptionDefinedInOptions_returnsMultiCharToken() throws Exception {
        options.addOption("foo", false, "option foo");
        String[] args = new String[]{"-foo"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-foo"}, result);
    }

    // Tests bursting multiple single-character flag options
    @Test
    public void testFlatten_burstMultipleFlags_burstsIntoIndividualTokens() throws Exception {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        options.addOption("c", false, "option c");
        String[] args = new String[]{"-abc"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-b", "-c"}, result);
    }

    // Tests bursting flag options with attached argument value
    @Test
    public void testFlatten_burstFlagsWithAttachedArgument_separatesArgValue() throws Exception {
        options.addOption("a", false, "option a");
        options.addOption("b", true, "option b with arg");
        String[] args = new String[]{"-abvalue"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-b", "value"}, result);
    }

    // Tests bursting unrecognized option with stopAtNonOption false
    @Test
    public void testFlatten_burstUnrecognizedOptionNoStop_returnsOriginalToken() throws Exception {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-az"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-az"}, result);
    }

    // Tests bursting unrecognized option with stopAtNonOption true
    @Test
    public void testFlatten_burstUnrecognizedOptionStopAtNonOption_addsSpecialTokens() throws Exception {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-az", "remaining"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "z", "remaining"}, result);
    }

    // Tests non-option arguments with stopAtNonOption false
    @Test
    public void testFlatten_nonOptionNoStop_addsTokenDirectly() throws Exception {
        String[] args = new String[]{"arg1", "arg2"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"arg1", "arg2"}, result);
    }

    // Tests non-option arguments with stopAtNonOption true when no current option exists
    @Test
    public void testFlatten_nonOptionStopAtNonOptionNoCurrentOption_addsSeparatorAndGobbles() throws Exception {
        String[] args = new String[]{"arg1", "arg2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "arg1", "arg2"}, result);
    }

    // Tests non-option argument provided after option expecting an argument with stopAtNonOption true
    @Test
    public void testFlatten_nonOptionWithCurrentOptionExpectingArg_consumesArgProperly() throws Exception {
        options.addOption("a", true, "option a requiring arg");
        String[] args = new String[]{"-a", "val", "nonOption"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "val", "--", "nonOption"}, result);
    }

    // Tests full command line parse end-to-end
    @Test
    public void testParse_standardCommandLine_parsesOptionsAndArguments() throws Exception {
        options.addOption("a", false, "flag a");
        options.addOption("b", true, "option b");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "-b", "val", "extra"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("val", cmd.getOptionValue("b"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("extra", cmd.getArgs()[0]);
    }

    // Tests multiple calls to flatten on the same parser instance to verify state re-initialization
    @Test
    public void testFlatten_multipleCalls_resetsInternalStateCorrectly() throws Exception {
        options.addOption("a", false, "option a");
        String[] firstArgs = new String[]{"-a", "nonOption"};
        parser.flatten(options, firstArgs, true);

        String[] secondArgs = new String[]{"-a"};
        String[] result = parser.flatten(options, secondArgs, false);
        assertArrayEquals(new String[]{"-a"}, result);
    }
}