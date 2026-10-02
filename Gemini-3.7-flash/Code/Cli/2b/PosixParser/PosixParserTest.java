package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PosixParserTest {

    private PosixParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new PosixParser();
        options = new Options();
    }

    // Tests flattening when arguments array is empty
    @Test
    public void testFlatten_emptyArgs_returnsEmptyArray() {
        String[] args = new String[]{};
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    // Tests handling of double hyphen token without equals sign
    @Test
    public void testFlatten_doubleHyphenWithoutEquals_preservesToken() {
        options.addOption("test", false, "test option");
        String[] args = new String[]{"--test"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--test"}, result);
    }

    // Tests splitting of double hyphen token containing an equals sign
    @Test
    public void testFlatten_doubleHyphenWithEquals_splitsKeyAndValue() {
        options.addOption("foo", true, "foo option");
        String[] args = new String[]{"--foo=bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--foo", "bar"}, result);
    }

    // Tests single hyphen argument preservation
    @Test
    public void testFlatten_singleHyphen_addsSingleHyphenDirectly() {
        String[] args = new String[]{"-"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-"}, result);
    }

    // Tests valid 2-character single option flag
    @Test
    public void testFlatten_validTwoCharOption_addsOptionToken() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a"}, result);
    }

    // Tests invalid 2-character option with stopAtNonOption set to false
    @Test
    public void testFlatten_invalidTwoCharOptionNoStop_ignoresToken() {
        String[] args = new String[]{"-x"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{}, result);
    }

    // Tests invalid 2-character option with stopAtNonOption set to true
    @Test
    public void testFlatten_invalidTwoCharOptionStopAtNonOption_eatsRemainingTokens() {
        String[] args = new String[]{"-x", "extra1", "extra2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"extra1", "extra2"}, result);
    }

    // Tests multi-character option defined directly in Options
    @Test
    public void testFlatten_definedLongOptionStartingWithSingleHyphen_addsTokenDirectly() {
        options.addOption("foo", false, "option foo");
        String[] args = new String[]{"-foo"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-foo"}, result);
    }

    // Tests bursting multiple valid single-character options
    @Test
    public void testFlatten_burstValidFlags_splitsIntoSeparateOptions() {
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        options.addOption("c", false, "option c");
        String[] args = new String[]{"-abc"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-b", "-c"}, result);
    }

    // Tests bursting option with attached argument value
    @Test
    public void testFlatten_burstOptionWithArgument_extractsArgumentAndStopsBursting() {
        options.addOption("a", false, "option a");
        options.addOption("b", true, "option b with argument");
        String[] args = new String[]{"-abValue"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-b", "Value"}, result);
    }

    // Tests bursting unrecognized character when stopAtNonOption is false
    @Test
    public void testFlatten_burstUnrecognizedCharacterNoStop_addsPrefixedToken() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-az"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-z"}, result);
    }

    // Tests bursting unrecognized character when stopAtNonOption is true
    @Test
    public void testFlatten_burstUnrecognizedCharacterStopAtNonOption_stopsAndProcessesRemaining() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-az", "remainder"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "z", "remainder"}, result);
    }

    // Tests argument consumption for preceding option token
    @Test
    public void testFlatten_nonOptionValueForPreviousOption_consumesAsArgument() {
        options.addOption("d", true, "option d with argument");
        String[] args = new String[]{"-d", "file.txt"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-d", "file.txt"}, result);
    }

    // Tests non-option token with stopAtNonOption set to true
    @Test
    public void testFlatten_nonOptionTokenStopAtNonOptionTrue_insertsDoubleHyphenAndEatsRest() {
        String[] args = new String[]{"cmd", "arg1", "arg2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "cmd", "arg1", "arg2"}, result);
    }

    // Tests non-option token with stopAtNonOption set to false
    @Test
    public void testFlatten_nonOptionTokenStopAtNonOptionFalse_addsTokenDirectly() {
        String[] args = new String[]{"file1.txt", "file2.txt"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"file1.txt", "file2.txt"}, result);
    }

    // Tests state reset on reuse of the PosixParser instance
    @Test
    public void testFlatten_parserReuse_resetsInternalStateCorrectly() {
        options.addOption("a", false, "option a");
        String[] firstArgs = new String[]{"-x", "leftover"};
        parser.flatten(options, firstArgs, true);

        String[] secondArgs = new String[]{"-a"};
        String[] secondResult = parser.flatten(options, secondArgs, false);
        assertArrayEquals(new String[]{"-a"}, secondResult);
    }
}