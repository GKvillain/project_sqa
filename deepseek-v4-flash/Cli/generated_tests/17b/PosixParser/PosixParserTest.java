package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;

public class PosixParserTest {

    private PosixParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new PosixParser();
        options = new Options();
        // short options without argument
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        // short option with one argument
        options.addOption("c", true, "option c with one arg");
        // short option with two arguments
        Option optD = new Option("d", true, "option d with multiple args");
        optD.setArgs(2);
        options.addOption(optD);
        // long options
        options.addOption("long", false, "long no arg");
        options.addOption("longwitharg", true, "long with arg");
    }

    // Long option with '=' contains argument value
    @Test
    public void testFlatten_longOptionWithEquals_returnsTokenAndValue() {
        String[] result = parser.flatten(options, new String[]{"--longwitharg=value"}, false);
        String[] expected = {"--longwitharg", "value"};
        assertArrayEquals(expected, result);
    }

    // Long option without '='
    @Test
    public void testFlatten_longOptionWithoutEquals_returnsToken() {
        String[] result = parser.flatten(options, new String[]{"--long"}, false);
        String[] expected = {"--long"};
        assertArrayEquals(expected, result);
    }

    // Short option without argument
    @Test
    public void testFlatten_shortOptionNoArg_returnsOptionToken() {
        String[] result = parser.flatten(options, new String[]{"-a"}, false);
        String[] expected = {"-a"};
        assertArrayEquals(expected, result);
    }

    // Short option with argument (separate token)
    @Test
    public void testFlatten_shortOptionWithArg_returnsOptionAndArg() {
        String[] result = parser.flatten(options, new String[]{"-c", "val"}, false);
        String[] expected = {"-c", "val"};
        assertArrayEquals(expected, result);
    }

    // Short option with multiple arguments (setArgs(2))
    // The method should keep currentOption active until enough tokens are consumed.
    @Test
    public void testFlatten_shortOptionWithMultipleArgs_returnsOptionAndArgs() {
        String[] result = parser.flatten(options, new String[]{"-d", "val1", "val2"}, false);
        String[] expected = {"-d", "val1", "val2"};
        assertArrayEquals(expected, result);
    }

    // Burst token: all characters are valid options without argument
    @Test
    public void testFlatten_burstAllValidOptions_returnsIndividualOptions() {
        String[] result = parser.flatten(options, new String[]{"-ab"}, false);
        String[] expected = {"-a", "-b"};
        assertArrayEquals(expected, result);
    }

    // Burst token: option with argument combined with following characters
    @Test
    public void testFlatten_burstOptionWithArgAndRemaining_returnsOptionAndRemaining() {
        // 'c' has one argument, token "-cxyz" should yield "-c" and "xyz"
        String[] result = parser.flatten(options, new String[]{"-cxyz"}, false);
        String[] expected = {"-c", "xyz"};
        assertArrayEquals(expected, result);
    }

    // Burst token: first non‑option character when stopAtNonOption==true
    // should process the whole remaining substring as one token (bug: no break after process)
    @Test
    public void testFlatten_burstNonOptionStopAtNonOptionTrue_doesNotDuplicateRemaining() {
        // options: a,b valid; c,d are not options
        // token "-abcd" with stopAtNonOption=true
        // Expected: process entire remaining "cd" once, not twice
        String[] result = parser.flatten(options, new String[]{"-abcd"}, true);
        // a and b valid -> "-a", "-b", then "cd" non-option -> process("cd") -> "--" + "cd"
        String[] expected = {"-a", "-b", "--", "cd"};
        assertArrayEquals(expected, result);
    }

    // Burst token: first non‑option character when stopAtNonOption==false
    // should add the whole original token and break
    @Test
    public void testFlatten_burstNonOptionStopAtNonOptionFalse_addsFullToken() {
        String[] result = parser.flatten(options, new String[]{"-abcdef"}, false);
        String[] expected = {"-abcdef"};
        assertArrayEquals(expected, result);
    }

    // Single hyphen token
    @Test
    public void testFlatten_singleHyphen_returnsHyphen() {
        String[] result = parser.flatten(options, new String[]{"-"}, false);
        String[] expected = {"-"};
        assertArrayEquals(expected, result);
    }

    // Double hyphen token (end of options marker)
    @Test
    public void testFlatten_doubleHyphen_returnsDoubleHyphen() {
        String[] result = parser.flatten(options, new String[]{"--"}, false);
        String[] expected = {"--"};
        assertArrayEquals(expected, result);
    }

    // Double hyphen followed by non‑option argument
    @Test
    public void testFlatten_doubleHyphenThenNonOption_remainsAsIs() {
        String[] result = parser.flatten(options, new String[]{"--", "value"}, false);
        String[] expected = {"--", "value"};
        assertArrayEquals(expected, result);
    }

    // Non‑option token at start with stopAtNonOption==true
    // should produce "--" + the token, and then all remaining tokens are added
    @Test
    public void testFlatten_nonOptionAtStartStopAtNonOptionTrue_addsDoubleDashAndRemaining() {
        String[] result = parser.flatten(options, new String[]{"foo", "-a"}, true);
        // process("foo") sets eatTheRest=true, adds "--" and "foo"
        // then gobble adds "-a"
        String[] expected = {"--", "foo", "-a"};
        assertArrayEquals(expected, result);
    }

    // Non‑option token with stopAtNonOption==false added without change
    @Test
    public void testFlatten_nonOptionOnlyStopAtNonOptionFalse_addsDirectly() {
        String[] result = parser.flatten(options, new String[]{"foo"}, false);
        String[] expected = {"foo"};
        assertArrayEquals(expected, result);
    }

    // Empty arguments array
    @Test
    public void testFlatten_emptyArguments_returnsEmptyArray() {
        String[] result = parser.flatten(options, new String[0], false);
        String[] expected = {};
        assertArrayEquals(expected, result);
    }

    // Burst token where a valid option with argument exhausts all remaining characters
    @Test
    public void testFlatten_burstOptionWithArgNoRemaining_returnsOptionOnly() {
        // option 'c' hasArg, token exactly "-c"
        String[] result = parser.flatten(options, new String[]{"-c"}, false);
        String[] expected = {"-c"};
        assertArrayEquals(expected, result);
    }

    // Burst token: option with argument and the argument takes the rest (no extra chars to burst)
    @Test
    public void testFlatten_burstOptionWithArgAndOnlyOneChar_returnsOptionAndArg() {
        String[] result = parser.flatten(options, new String[]{"-cx"}, false);
        String[] expected = {"-c", "x"};
        assertArrayEquals(expected, result);
    }

    // (Optional) Test null arguments – expecting NullPointerException
    @Test(expected = NullPointerException.class)
    public void testFlatten_nullArguments_throwsNullPointerException() {
        parser.flatten(options, null, false);
    }

    // === New tests for uncovered sections ===

    // Burst token where an option with an argument appears at the very end.
    // This exercises the branch when opt.hasArg() is true and no characters remain.
    @Test
    public void testFlatten_burstWithOptionWithArgAtEnd_returnsEmptyArg() {
        String[] result = parser.flatten(options, new String[]{"-abc"}, false);
        String[] expected = {"-a", "-b", "-c", ""};
        assertArrayEquals(expected, result);
    }

    // Long option with argument supplied as a separate token (not using '=')
    @Test
    public void testFlatten_longOptionWithSeparateArg() {
        String[] result = parser.flatten(options, new String[]{"--longwitharg", "value"}, false);
        String[] expected = {"--longwitharg", "value"};
        assertArrayEquals(expected, result);
    }

    // Double hyphen followed by multiple tokens: all remaining tokens should be kept verbatim
    @Test
    public void testFlatten_doubleHyphenWithMultipleTokens() {
        String[] result = parser.flatten(options, new String[]{"--", "-a", "foo"}, false);
        String[] expected = {"--", "-a", "foo"};
        assertArrayEquals(expected, result);
    }

    // Single hyphen followed by a non-option token
    @Test
    public void testFlatten_singleHyphenAndToken() {
        String[] result = parser.flatten(options, new String[]{"-", "foo"}, false);
        String[] expected = {"-", "foo"};
        assertArrayEquals(expected, result);
    }
}