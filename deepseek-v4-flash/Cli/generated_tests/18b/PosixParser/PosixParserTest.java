package org.apache.commons.cli;

import static org.junit.Assert.*;
import org.junit.Test;

public class PosixParserTest {

    @Test
    public void testFlatten_singleDash_returnsSingleHyphen() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        String[] args = {"-"};
        String[] result = p.flatten(opts, args, false);
        assertArrayEquals(new String[]{"-"}, result);
    }

    @Test
    public void testFlatten_doubleDash_returnsDoubleDash() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        String[] args = {"--"};
        String[] result = p.flatten(opts, args, false);
        assertArrayEquals(new String[]{"--"}, result);
    }

    @Test
    public void testFlatten_longOptionWithEquals_returnsSplitTokens() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        String[] args = {"--foo=bar"};
        String[] result = p.flatten(opts, args, false);
        assertArrayEquals(new String[]{"--foo", "bar"}, result);
    }

    @Test
    public void testFlatten_knownShortOption_returnsOption() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        opts.addOption(new Option("a", false, "desc"));
        String[] args = {"-a"};
        String[] result = p.flatten(opts, args, false);
        assertArrayEquals(new String[]{"-a"}, result);
    }

    @Test
    public void testFlatten_validLongOption_returnsOption() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        opts.addOption(new Option("long", false, "desc"));
        String[] args = {"-long"};
        String[] result = p.flatten(opts, args, false);
        assertArrayEquals(new String[]{"-long"}, result);
    }

    @Test
    public void testFlatten_unknownShortOption_stopTrue_returnsTokenAndStops() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        String[] args = {"-x", "foo"};
        String[] result = p.flatten(opts, args, true);
        assertArrayEquals(new String[]{"-x", "foo"}, result);
    }

    @Test
    public void testFlatten_unknownShortOptionFollowedByOptions_stopTrue_returnsAllTokens() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        opts.addOption(new Option("a", false, "desc"));
        String[] args = {"-x", "-a", "-y"};
        String[] result = p.flatten(opts, args, true);
        assertArrayEquals(new String[]{"-x", "-a", "-y"}, result);
    }

    @Test
    public void testFlatten_knownOptionFollowedByUnknownOption_stopTrue_returnsAllTokens() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        opts.addOption(new Option("a", false, "desc"));
        String[] args = {"-a", "-x"};
        String[] result = p.flatten(opts, args, true);
        assertArrayEquals(new String[]{"-a", "-x"}, result);
    }

    @Test
    public void testFlatten_unknownShortOption_stopFalse_returnsEmpty() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        String[] args = {"-x"};
        String[] result = p.flatten(opts, args, false);
        assertArrayEquals(new String[]{}, result);
    }

    @Test
    public void testFlatten_burstOptions_allKnown_returnsEachOption() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        opts.addOption(new Option("a", false, "desc"));
        opts.addOption(new Option("b", false, "desc"));
        String[] args = {"-ab"};
        String[] result = p.flatten(opts, args, false);
        assertArrayEquals(new String[]{"-a", "-b"}, result);
    }

    @Test
    public void testFlatten_burstOptionWithArg_returnsOptionAndArg() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        opts.addOption(new Option("a", true, "desc"));
        String[] args = {"-aval"};
        String[] result = p.flatten(opts, args, false);
        assertArrayEquals(new String[]{"-a", "val"}, result);
    }

    @Test
    public void testFlatten_burstOptionWithNonOption_stopTrue_returnsBurstAndPutsRest() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        opts.addOption(new Option("a", false, "desc"));
        String[] args = {"-axy", "foo"};
        String[] result = p.flatten(opts, args, true);
        assertArrayEquals(new String[]{"-a", "--", "xy", "foo"}, result);
    }

    @Test
    public void testFlatten_burstOptionWithNonOption_stopFalse_returnsWholeToken() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        opts.addOption(new Option("a", false, "desc"));
        String[] args = {"-ax"};
        String[] result = p.flatten(opts, args, false);
        assertArrayEquals(new String[]{"-a", "-ax"}, result);
    }

    @Test
    public void testFlatten_nonOptionToken_stopTrue_callsProcess() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        String[] args = {"foo"};
        String[] result = p.flatten(opts, args, true);
        assertArrayEquals(new String[]{"--", "foo"}, result);
    }

    @Test
    public void testFlatten_nonOptionToken_stopFalse_addsDirectly() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        String[] args = {"foo"};
        String[] result = p.flatten(opts, args, false);
        assertArrayEquals(new String[]{"foo"}, result);
    }

    @Test
    public void testFlatten_knownOptionWithArg_stopTrue_processHandlesArg() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        opts.addOption(new Option("a", true, "desc"));
        String[] args = {"-a", "val"};
        String[] result = p.flatten(opts, args, true);
        assertArrayEquals(new String[]{"-a", "val"}, result);
    }

    @Test
    public void testFlatten_knownOptionNoArg_stopTrue_processAddsDashDash() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        opts.addOption(new Option("a", false, "desc"));
        String[] args = {"-a", "val"};
        String[] result = p.flatten(opts, args, true);
        assertArrayEquals(new String[]{"-a", "--", "val"}, result);
    }

    @Test
    public void testFlatten_burstWithMultipleOptionsOneHasArg_returnsAllTokens() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        opts.addOption(new Option("a", false, "desc"));
        opts.addOption(new Option("b", true, "desc"));
        String[] args = {"-abval"};
        String[] result = p.flatten(opts, args, false);
        assertArrayEquals(new String[]{"-a", "-b", "val"}, result);
    }

    @Test
    public void testFlatten_emptyArgs_returnsEmptyArray() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        String[] args = {};
        String[] result = p.flatten(opts, args, false);
        assertArrayEquals(new String[]{}, result);
    }

    @Test(expected = NullPointerException.class)
    public void testFlatten_nullOptions_throwsNullPointerException() {
        PosixParser p = new PosixParser();
        String[] args = {"-a"};
        p.flatten(null, args, false);
    }

    // Newly added tests to increase coverage

    @Test
    public void testFlatten_singleDash_stopTrue_returnsSingleHyphen() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        String[] args = {"-"};
        String[] result = p.flatten(opts, args, true);
        assertArrayEquals(new String[]{"-"}, result);
    }

    @Test
    public void testFlatten_doubleDash_stopTrue_returnsDoubleDash() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        String[] args = {"--"};
        String[] result = p.flatten(opts, args, true);
        assertArrayEquals(new String[]{"--"}, result);
    }

    @Test
    public void testFlatten_longOptionWithoutEquals_returnsSingleToken() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        String[] args = {"--foo"};
        String[] result = p.flatten(opts, args, false);
        assertArrayEquals(new String[]{"--foo"}, result);
    }

    @Test
    public void testFlatten_burstOptionWithArgFirst_returnsOptionAndRest() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        opts.addOption(new Option("a", true, "desc"));
        String[] args = {"-ab"};
        String[] result = p.flatten(opts, args, false);
        assertArrayEquals(new String[]{"-a", "b"}, result);
    }

    @Test
    public void testFlatten_burstMultipleOptionsWithLastArg_returnsAllTokens() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        opts.addOption(new Option("a", false, "desc"));
        opts.addOption(new Option("b", false, "desc"));
        opts.addOption(new Option("c", true, "desc"));
        String[] args = {"-abcval"};
        String[] result = p.flatten(opts, args, false);
        assertArrayEquals(new String[]{"-a", "-b", "-c", "val"}, result);
    }

    @Test
    public void testFlatten_optionWithSeparateArg_stopFalse_returnsBothTokens() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        opts.addOption(new Option("a", true, "desc"));
        String[] args = {"-a", "val"};
        String[] result = p.flatten(opts, args, false);
        assertArrayEquals(new String[]{"-a", "val"}, result);
    }

    @Test
    public void testFlatten_mixedOptionsAndNonOptions_stopTrue_addsDashDashBeforeNonOptions() {
        PosixParser p = new PosixParser();
        Options opts = new Options();
        opts.addOption(new Option("a", false, "desc"));
        String[] args = {"-a", "foo", "bar"};
        String[] result = p.flatten(opts, args, true);
        assertArrayEquals(new String[]{"-a", "--", "foo", "bar"}, result);
    }
}