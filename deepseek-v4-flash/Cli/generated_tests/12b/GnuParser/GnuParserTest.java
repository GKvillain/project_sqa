package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test class for GnuParser, targeting the flatten method.
 * Designed to detect the Defects4J bug #12b (long options with equal sign).
 */
public class GnuParserTest {

    private Options createOptions() {
        Options options = new Options();
        // short option without argument (just exists)
        options.addOption("a", "alpha", false, "alpha option");
        // long option without argument
        options.addOption("b", "beta", false, "beta option");
        // short option with argument (e.g., -D)
        options.addOption("D", "debug", true, "debug option");
        // long option with argument (e.g., --verbose)
        options.addOption("v", "verbose", true, "verbose option");
        // additional option for other tests
        options.addOption("x", null, false, "x option");
        return options;
    }

    // ---------- Normal cases ----------

    // Tests normal short option without argument
    @Test
    public void testFlatten_shortOption_returnsSingleToken() {
        GnuParser parser = new GnuParser();
        Options options = createOptions();
        String[] args = {"-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a"}, result);
    }

    // Tests normal long option without argument
    @Test
    public void testFlatten_longOption_returnsSingleToken() {
        GnuParser parser = new GnuParser();
        Options options = createOptions();
        String[] args = {"--beta"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--beta"}, result);
    }

    // Tests -Dproperty=value: known short option -D, should split into two tokens
    @Test
    public void testFlatten_shortOptionWithEquals_returnsTwoTokens() {
        GnuParser parser = new GnuParser();
        Options options = createOptions();
        String[] args = {"-Dproperty=value"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-D", "property=value"}, result);
    }

    // Tests --verbose=3: known long option --verbose, should split into two tokens (current bug)
    @Test
    public void testFlatten_longOptionWithEquals_returnsTwoTokens() {
        GnuParser parser = new GnuParser();
        Options options = createOptions();
        String[] args = {"--verbose=3"};
        String[] result = parser.flatten(options, args, false);
        // Expected: ["--verbose", "3"] but current bug produces ["--verbose=3"]
        assertArrayEquals(new String[]{"--verbose", "3"}, result);
    }

    // Tests multiple options with arguments
    @Test
    public void testFlatten_multipleOptions_returnsCorrectOrder() {
        GnuParser parser = new GnuParser();
        Options options = createOptions();
        String[] args = {"-a", "--beta", "-Dfoo=bar", "arg1"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "--beta", "-D", "foo=bar", "arg1"}, result);
    }

    // ---------- Boundary and special symbols ----------

    // Tests double dash (--), should stop processing further tokens when stopAtNonOption is true
    @Test
    public void testFlatten_doubleDash_stopsFlattening() {
        GnuParser parser = new GnuParser();
        Options options = createOptions();
        String[] args = {"--", "-a", "--beta"};
        String[] result = parser.flatten(options, args, false);
        // "--" is added, then subsequent tokens are added as they are
        assertArrayEquals(new String[]{"--", "-a", "--beta"}, result);
    }

    // Tests single dash (-)
    @Test
    public void testFlatten_singleDash_returnsDashToken() {
        GnuParser parser = new GnuParser();
        Options options = createOptions();
        String[] args = {"-"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-"}, result);
    }

    // Tests token that is not an option (no leading dash)
    @Test
    public void testFlatten_nonOptionToken_returnsToken() {
        GnuParser parser = new GnuParser();
        Options options = createOptions();
        String[] args = {"somefile.txt"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"somefile.txt"}, result);
    }

    // Tests stopAtNonOption behaviour: an unknown short option stops processing
    @Test
    public void testFlatten_stopAtNonOption_unknownOptionStops() {
        GnuParser parser = new GnuParser();
        Options options = createOptions();
        String[] args = {"-x", "--beta"};  // -x is known, but we'll test an unknown: -z
        // Actually -x is known, so use unknown -z
        args = new String[]{"-z", "--beta"};
        String[] result = parser.flatten(options, args, true);
        // -z is unknown, so stopAtNonOption=true => eatTheRest => adds -z and then all remaining
        assertArrayEquals(new String[]{"-z", "--beta"}, result);
    }

    // Tests stopAtNonOption=false: unknown option does not stop
    @Test
    public void testFlatten_noStopAtNonOption_unknownOptionContinues() {
        GnuParser parser = new GnuParser();
        Options options = createOptions();
        String[] args = {"-z", "--beta"};
        String[] result = parser.flatten(options, args, false);
        // -z unknown, stopAtNonOption=false => eatTheRest=false, add -z, then continue parsing --beta
        assertArrayEquals(new String[]{"-z", "--beta"}, result);
    }

    // ---------- Edge cases ----------

    // Tests empty arguments array
    @Test
    public void testFlatten_emptyArray_returnsEmptyArray() {
        GnuParser parser = new GnuParser();
        Options options = createOptions();
        String[] args = {};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[0], result);
    }

    // Tests argument with leading dash but unknown character (e.g., "-=")
    @Test
    public void testFlatten_dashUnknown_returnsFullToken() {
        GnuParser parser = new GnuParser();
        Options options = createOptions();
        String[] args = {"-="};
        // starts with "-", not "-" exactly, not "--", opt = "=", hasOption? no,
        // arg.substring(0,2) = "-=", hasOption? no, else branch: tokens.add("-=")
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-="}, result);
    }

    // Tests long option with = and value that also exists as an option name (e.g., --foo=bar where --foo is not registered but --foo=bar is not a typical case)
    // Already covered in longOptionWithEquals test.

    // Tests argument being "--" followed by more tokens and stopAtNonOption
    @Test
    public void testFlatten_doubleDashWithStop_stopsAfter() {
        GnuParser parser = new GnuParser();
        Options options = createOptions();
        String[] args = {"--", "arg1", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "arg1", "-a"}, result);
    }

    // ========== New tests for uncovered coverage ==========
    
    // Tests long option with equals sign and no value (--verbose=) - edge case
    @Test
    public void testFlatten_longOptionWithEqualsNoValue_returnsTokens() {
        GnuParser parser = new GnuParser();
        Options options = createOptions();
        String[] args = {"--verbose="};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--verbose", ""}, result);
    }

    // Tests long option with equals sign and numeric value
    @Test
    public void testFlatten_longOptionWithEqualsNumericValue_returnsTokens() {
        GnuParser parser = new GnuParser();
        Options options = createOptions();
        String[] args = {"--verbose=123"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--verbose", "123"}, result);
    }
}