package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class PosixParserTest {
    private Options options;
    private PosixParser parser;

    @Before
    public void setUp() {
        options = new Options();
        options.addOption(new Option("a", false, "desc"));
        options.addOption(new Option("b", false, "desc"));
        options.addOption(new Option("c", true, "desc"));
        Option d = new Option("d", false, "desc");
        d.setArgs(2);
        options.addOption(d);
        parser = new PosixParser();
    }

    private String[] flatten(boolean stopAtNonOption, String... args) {
        return parser.flatten(options, args, stopAtNonOption);
    }

    @Test
    public void testFlatten_longOptionNoEquals_addsToken() {
        String[] result = flatten(false, "--foo");
        assertArrayEquals(new String[]{"--foo"}, result);
    }

    @Test
    public void testFlatten_longOptionWithEquals_splitsToken() {
        String[] result = flatten(false, "--foo=bar");
        assertArrayEquals(new String[]{"--foo", "bar"}, result);
    }

    @Test
    public void testFlatten_singleHyphen_addsToken() {
        String[] result = flatten(false, "-");
        assertArrayEquals(new String[]{"-"}, result);
    }

    @Test
    public void testFlatten_shortOptionExists_addsToken() {
        String[] result = flatten(false, "-a");
        assertArrayEquals(new String[]{"-a"}, result);
    }

    @Test
    public void testFlatten_shortOptionNotExistsStopFalse_ignoresToken() {
        String[] result = flatten(false, "-z");
        assertArrayEquals(new String[]{}, result);
    }

    @Test
    public void testFlatten_shortOptionNotExistsStopTrue_setsEatTheRestAndGobbles() {
        String[] result = flatten(true, "-z", "arg1", "arg2");
        assertArrayEquals(new String[]{"arg1", "arg2"}, result);
    }

    @Test
    public void testFlatten_nonOptionStopFalse_addsToken() {
        String[] result = flatten(false, "foo");
        assertArrayEquals(new String[]{"foo"}, result);
    }

    @Test
    public void testFlatten_nonOptionStopTrue_callsProcess() {
        String[] result = flatten(true, "foo");
        assertArrayEquals(new String[]{"--", "foo"}, result);
    }

    @Test
    public void testFlatten_burstAllKnown_addsTokens() {
        String[] result = flatten(false, "-ab");
        assertArrayEquals(new String[]{"-a", "-b"}, result);
    }

    @Test
    public void testFlatten_burstOptionHasArgAndRemaining_addsOptionAndArgument() {
        String[] result = flatten(false, "-cxy");
        assertArrayEquals(new String[]{"-c", "xy"}, result);
    }

    @Test
    public void testFlatten_burstOptionHasArgNoRemaining_addsOptionAndArgument() {
        String[] result = flatten(false, "-cx");
        assertArrayEquals(new String[]{"-c", "x"}, result);
    }

    @Test
    public void testFlatten_burstUnknownStopFalse_addsTokensPerChar() {
        String[] result = flatten(false, "-ax");
        assertArrayEquals(new String[]{"-a", "-x"}, result);
    }

    @Test
    public void testFlatten_burstUnknownStopTrue_processesOnce() {
        // correct behavior should stop after processing the first unknown portion
        String[] result = flatten(true, "-xy");
        assertArrayEquals(new String[]{"--", "xy"}, result);
    }

    @Test
    public void testFlatten_optionSingleArgStopTrue_callsProcessAndEatTheRest() {
        String[] result = flatten(true, "-c", "arg1", "arg2");
        assertArrayEquals(new String[]{"-c", "arg1", "--", "arg2"}, result);
    }

    @Test
    public void testFlatten_optionMultipleArgsStopTrue_keepsAllArgs() {
        // This test aims to detect defect: multiple arguments should all be added as arguments,
        // not reset after the first argument.
        String[] result = flatten(true, "-d", "arg1", "arg2");
        assertArrayEquals(new String[]{"-d", "arg1", "arg2"}, result);
    }

    @Test
    public void testFlatten_doubleHyphen_addsToken() {
        String[] result = flatten(false, "--");
        assertArrayEquals(new String[]{"--"}, result);
    }

    // ================ New test cases to cover uncovered parts ================

    @Test
    public void testFlatten_optionSingleArgStopFalse() {
        // short option with required argument, not burst, stopAtNonOption = false
        String[] result = flatten(false, "-c", "arg1");
        assertArrayEquals(new String[]{"-c", "arg1"}, result);
    }

    @Test
    public void testFlatten_doubleHyphenWithArgs() {
        // "--" followed by other arguments should treat everything after as non-options
        String[] result = flatten(false, "--", "-a");
        assertArrayEquals(new String[]{"--", "-a"}, result);
    }

    @Test
    public void testFlatten_burstKnownUnknownStopTrue() {
        // mixed known and unknown short options with stopAtNonOption = true
        // -a is known, -x is unknown -> should stop after processing -a and then add "--" and "x"
        String[] result = flatten(true, "-ax");
        assertArrayEquals(new String[]{"-a", "--", "x"}, result);
    }

    @Test
    public void testFlatten_longOptionUnknownStopTrue() {
        // long option (unknown) with stopAtNonOption = true -> should be added as is
        String[] result = flatten(true, "--unknown");
        assertArrayEquals(new String[]{"--unknown"}, result);
    }

    @Test
    public void testFlatten_longOptionWithEmptyValue() {
        // long option with equals and empty value
        String[] result = flatten(false, "--foo=");
        assertArrayEquals(new String[]{"--foo", ""}, result);
    }

    @Test
    public void testFlatten_optionMultipleArgsStopFalse() {
        // option with multiple arguments required, stopAtNonOption = false
        String[] result = flatten(false, "-d", "arg1", "arg2");
        assertArrayEquals(new String[]{"-d", "arg1", "arg2"}, result);
    }
}