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

    // Tests flattening with empty arguments array
    @Test
    public void testFlatten_emptyArguments_returnsEmptyArray() {
        String[] args = new String[] {};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] {}, result);
    }

    // Tests standard single hyphen token
    @Test
    public void testFlatten_singleHyphen_retainsHyphenToken() {
        String[] args = new String[] { "-" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "-" }, result);
    }

    // Tests double hyphen token without options
    @Test
    public void testFlatten_doubleHyphen_retainsDoubleHyphen() {
        String[] args = new String[] { "--" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "--" }, result);
    }

    // Tests long option token with value separated by equal sign
    @Test
    public void testFlatten_longOptionWithEqualSign_splitsKeyAndValue() {
        options.addOption("foo", "foo-opt", true, "desc");
        String[] args = new String[] { "--foo=bar" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "--foo", "bar" }, result);
    }

    // Tests long option token without value
    @Test
    public void testFlatten_longOptionWithoutEqualSign_retainsOption() {
        options.addOption("foo", "foo-opt", false, "desc");
        String[] args = new String[] { "--foo" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "--foo" }, result);
    }

    // Tests single character valid option token
    @Test
    public void testFlatten_validSingleCharOption_addsOptionToTokens() {
        options.addOption("a", false, "Option a");
        String[] args = new String[] { "-a" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "-a" }, result);
    }

    // Tests single character option with argument when stopAtNonOption is true
    @Test
    public void testFlatten_optionWithArgWhenStopAtNonOption_addsArgToTokens() {
        options.addOption("a", true, "Option a");
        String[] args = new String[] { "-a", "val", "extra" };
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] { "-a", "val", "--", "extra" }, result);
    }

    // Tests non-option argument with stopAtNonOption set to false
    @Test
    public void testFlatten_nonOptionArgumentStopAtNonOptionFalse_retainsArgument() {
        String[] args = new String[] { "foo", "bar" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "foo", "bar" }, result);
    }

    // Tests non-option argument with stopAtNonOption set to true
    @Test
    public void testFlatten_nonOptionArgumentStopAtNonOptionTrue_addsDoubleHyphenAndGobbles() {
        String[] args = new String[] { "foo", "bar" };
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] { "--", "foo", "bar" }, result);
    }

    // Tests bursting of concatenated single-character options
    @Test
    public void testFlatten_burstMultipleSingleCharOptions_splitsIntoSeparateTokens() {
        options.addOption("a", false, "Option a");
        options.addOption("b", false, "Option b");
        options.addOption("c", false, "Option c");
        String[] args = new String[] { "-abc" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "-a", "-b", "-c" }, result);
    }

    // Tests bursting of concatenated options where last option takes argument
    @Test
    public void testFlatten_burstOptionWithInlineArgument_splitsOptionAndArgument() {
        options.addOption("a", false, "Option a");
        options.addOption("f", true, "Option f with arg");
        String[] args = new String[] { "-afbar" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "-a", "-f", "bar" }, result);
    }

    // Tests bursting with invalid option when stopAtNonOption is false
    @Test
    public void testFlatten_burstUnrecognizedOptionStopAtNonOptionFalse_splitsCharsWithHyphens() {
        options.addOption("a", false, "Option a");
        String[] args = new String[] { "-ax" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "-a", "-x" }, result);
    }

    // Tests bursting with invalid option when stopAtNonOption is true
    @Test
    public void testFlatten_burstUnrecognizedOptionStopAtNonOptionTrue_addsRemainingAsNonOption() {
        options.addOption("a", false, "Option a");
        String[] args = new String[] { "-axyz", "extra" };
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] { "-a", "--", "xyz", "extra" }, result);
    }

    // Tests option starting with hyphen that is defined as long option with single hyphen
    @Test
    public void testFlatten_singleHyphenLongOption_retainsOption() {
        options.addOption("foo", false, "Option foo");
        String[] args = new String[] { "-foo" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "-foo" }, result);
    }

    // Tests unrecognized 2-char option token with stopAtNonOption set to true (Defects4J Cli-2 regression)
    @Test
    public void testFlatten_unrecognizedTwoCharOptionStopAtNonOptionTrue_preservesTokenAndStops() {
        options.addOption("a", false, "Option a");
        String[] args = new String[] { "-u", "arg1", "arg2" };
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] { "--", "-u", "arg1", "arg2" }, result);
    }

    // Tests full parse integration through Parser base class
    @Test
    public void testParse_multipleOptionsAndArgs_parsedSuccessfully() throws Exception {
        options.addOption("a", false, "flag a");
        options.addOption("b", true, "option b with arg");
        String[] args = new String[] { "-a", "-b", "bValue", "extra1", "extra2" };
        
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("bValue", cl.getOptionValue("b"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("extra1", cl.getArgs()[0]);
        assertEquals("extra2", cl.getArgs()[1]);
    }

    // Tests parser reusability ensuring init resets previous parse state
    @Test
    public void testParse_reusedParserInstance_resetsStateCorrectly() throws Exception {
        options.addOption("a", false, "flag a");
        
        CommandLine cl1 = parser.parse(options, new String[] { "-a" });
        assertTrue(cl1.hasOption("a"));

        CommandLine cl2 = parser.parse(options, new String[] { "argOnly" });
        assertFalse(cl2.hasOption("a"));
        assertEquals(1, cl2.getArgs().length);
        assertEquals("argOnly", cl2.getArgs()[0]);
    }
}