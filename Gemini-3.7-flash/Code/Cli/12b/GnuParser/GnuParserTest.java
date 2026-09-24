package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GnuParserTest {

    private GnuParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new GnuParser();
        options = new Options();
    }

    // Tests empty argument list
    @Test
    public void testFlatten_emptyArguments_returnsEmptyArray() {
        String[] args = new String[0];
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    // Tests simple single-hyphen option recognized by options
    @Test
    public void testFlatten_singleHyphenOption_returnsSameToken() {
        options.addOption("a", "all", false, "toggle all");
        String[] args = new String[]{"-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a"}, result);
    }

    // Tests simple long option recognized by options
    @Test
    public void testFlatten_longOption_returnsSameToken() {
        options.addOption("a", "all", false, "toggle all");
        String[] args = new String[]{"--all"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--all"}, result);
    }

    // Tests solitary single hyphen token
    @Test
    public void testFlatten_singleHyphenToken_returnsSingleHyphen() {
        String[] args = new String[]{"-"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-"}, result);
    }

    // Tests double hyphen token which triggers eating the rest of arguments
    @Test
    public void testFlatten_doubleHyphenToken_eatsRemainingArguments() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"--", "-a", "arg1", "arg2"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--", "-a", "arg1", "arg2"}, result);
    }

    // Tests property-style option like -Dkey=value where first char after hyphen is an option
    @Test
    public void testFlatten_propertyStyleOption_splitsOptionAndValue() {
        options.addOption("D", "define", true, "define property");
        String[] args = new String[]{"-Dkey=value"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-D", "key=value"}, result);
    }

    // Tests long option with equal sign (--foo=bar)
    @Test
    public void testFlatten_longOptionWithEqualsSign_splitsOptionAndValue() {
        options.addOption("f", "foo", true, "foo option");
        String[] args = new String[]{"--foo=bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--foo", "bar"}, result);
    }

    // Tests short option with equal sign (-f=bar)
    @Test
    public void testFlatten_shortOptionWithEqualsSign_splitsOptionAndValue() {
        options.addOption("f", "foo", true, "foo option");
        String[] args = new String[]{"-f=bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-f", "bar"}, result);
    }

    // Tests unrecognized option when stopAtNonOption is true
    @Test
    public void testFlatten_unrecognizedOptionStopAtNonOptionTrue_eatsRest() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-z", "-a", "extra"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "-a", "extra"}, result);
    }

    // Tests unrecognized option when stopAtNonOption is false
    @Test
    public void testFlatten_unrecognizedOptionStopAtNonOptionFalse_continuesProcessing() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-z", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-z", "-a"}, result);
    }

    // Tests non-option argument with stopAtNonOption false
    @Test
    public void testFlatten_nonOptionArgumentStopAtNonOptionFalse_keepsTokenAndContinues() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"nonOption", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"nonOption", "-a"}, result);
    }

    // Tests non-option argument with stopAtNonOption true
    @Test
    public void testFlatten_nonOptionArgumentStopAtNonOptionTrue_eatsRemainingArguments() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"nonOption", "-a", "rest"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"nonOption", "-a", "rest"}, result);
    }

    // Tests multiple valid options and arguments mixed together
    @Test
    public void testFlatten_mixedOptionsAndArguments_returnsFlattenedTokens() {
        options.addOption("a", false, "option a");
        options.addOption("b", "batch", true, "batch mode");
        options.addOption("c", false, "option c");

        String[] args = new String[]{"-a", "--batch", "file.txt", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "--batch", "file.txt", "-c"}, result);
    }

    // Tests parse method via Parser superclass integration
    @Test
    public void testParse_validOptions_createsCommandLine() throws Exception {
        options.addOption("a", false, "option a");
        options.addOption("b", true, "option b");

        String[] args = new String[]{"-a", "-b", "val", "nonOpt"};
        CommandLine cl = parser.parse(options, args);

        assertNotNull(cl);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("val", cl.getOptionValue("b"));
        assertEquals(1, cl.getArgList().size());
        assertEquals("nonOpt", cl.getArgList().get(0));
    }

    // Tests property-style option without equal sign (e.g. -Dvalue where option is "D")
    @Test
    public void testFlatten_propertyStyleOptionWithoutEquals_splitsOptionAndValue() {
        options.addOption("D", true, "define property");
        String[] args = new String[]{"-Dmyproperty"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-D", "myproperty"}, result);
    }

    // Tests unrecognized option with equal sign when stopAtNonOption is true
    @Test
    public void testFlatten_unrecognizedOptionWithEqualsStopAtNonOptionTrue_eatsRest() {
        options.addOption("a", false, "option a");
        String[] args = new String[]{"--unknown=val", "-a", "extra"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--unknown=val", "-a", "extra"}, result);
    }
}