package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GnuParserTest {

    private GnuParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new GnuParser();
        options = new Options();
        options.addOption("a", "all", false, "toggle all");
        options.addOption("b", "buffer", true, "set buffer size");
        options.addOption("c", false, "toggle c");
        options.addOption("D", "property", true, "set property");
        options.addOption(new Option("f", "foo", true, "set foo"));
    }

    // Tests flattening simple single-character options
    @Test
    public void testFlatten_simpleShortOption_returnsToken() {
        String[] args = new String[]{"-a", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-c"}, result);
    }

    // Tests flattening full long options
    @Test
    public void testFlatten_longOption_returnsToken() {
        String[] args = new String[]{"--all", "--foo", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--all", "--foo", "bar"}, result);
    }

    // Tests flattening double dash separator which triggers eatTheRest
    @Test
    public void testFlatten_doubleHyphen_eatsRemainingArguments() {
        String[] args = new String[]{"-a", "--", "-c", "extra"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "--", "-c", "extra"}, result);
    }

    // Tests flattening single dash token
    @Test
    public void testFlatten_singleHyphen_returnsHyphenToken() {
        String[] args = new String[]{"-", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-", "-a"}, result);
    }

    // Tests flattening short property option format like -Dproperty=value
    @Test
    public void testFlatten_propertyOptionFormat_splitsOptionAndValue() {
        String[] args = new String[]{"-Dkey=value"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-D", "key=value"}, result);
    }

    // Tests flattening long option with equal sign (--foo=bar)
    @Test
    public void testFlatten_longOptionWithEqualSign_handlesOptionAndValue() {
        String[] args = new String[]{"--foo=bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--foo", "bar"}, result);
    }

    // Tests flattening short option with equal sign (-f=bar)
    @Test
    public void testFlatten_shortOptionWithEqualSign_handlesOptionAndValue() {
        String[] args = new String[]{"-f=bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-f", "bar"}, result);
    }

    // Tests non-option arguments when stopAtNonOption is false
    @Test
    public void testFlatten_nonOptionArgumentsWithoutStop_preservesAllTokens() {
        String[] args = new String[]{"arg1", "-a", "arg2"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"arg1", "-a", "arg2"}, result);
    }

    // Tests non-option arguments when stopAtNonOption is true
    @Test
    public void testFlatten_nonOptionArgumentsWithStop_eatsRemainingArguments() {
        String[] args = new String[]{"arg1", "-a", "arg2"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"arg1", "-a", "arg2"}, result);
    }

    // Tests unrecognized option starting with hyphen when stopAtNonOption is false
    @Test
    public void testFlatten_unrecognizedOptionNoStop_returnsToken() {
        String[] args = new String[]{"-z", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-z", "-a"}, result);
    }

    // Tests unrecognized option starting with hyphen when stopAtNonOption is true
    @Test
    public void testFlatten_unrecognizedOptionWithStop_eatsRemainingArguments() {
        String[] args = new String[]{"-z", "-a", "extra"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "-a", "extra"}, result);
    }

    // Tests empty arguments array
    @Test
    public void testFlatten_emptyArguments_returnsEmptyArray() {
        String[] args = new String[]{};
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    // Tests end-to-end parse execution for long option with equal sign
    @Test
    public void testParse_longOptionWithEqualSign_parsesSuccessfully() throws Exception {
        String[] args = new String[]{"--foo=bar"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("foo"));
        assertEquals("bar", cmd.getOptionValue("foo"));
    }

    // Tests end-to-end parse execution for short option with equal sign
    @Test
    public void testParse_shortOptionWithEqualSign_parsesSuccessfully() throws Exception {
        String[] args = new String[]{"-f=bar"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("f"));
        assertEquals("bar", cmd.getOptionValue("f"));
    }

    // Tests end-to-end parse execution with multiple mixed options
    @Test
    public void testParse_mixedOptions_parsesSuccessfully() throws Exception {
        String[] args = new String[]{"-a", "-b", "1024", "-Dkey=value", "file.txt"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        assertEquals("1024", cmd.getOptionValue("b"));
        assertEquals("value", cmd.getOptionValue("D"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("file.txt", cmd.getArgs()[0]);
    }

    // Tests flattening attached short option value without equal sign (e.g., -b1024)
    @Test
    public void testFlatten_attachedShortOptionWithoutEqualSign_splitsOptionAndValue() {
        String[] args = new String[]{"-b1024"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "1024"}, result);
    }

    // Tests flattening unrecognized option with equal sign when stopAtNonOption is false
    @Test
    public void testFlatten_unrecognizedOptionWithEqualSignNoStop_returnsToken() {
        String[] args = new String[]{"--unknown=val", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--unknown=val", "-a"}, result);
    }

    // Tests flattening unrecognized option with equal sign when stopAtNonOption is true
    @Test
    public void testFlatten_unrecognizedOptionWithEqualSignWithStop_eatsRemainingArguments() {
        String[] args = new String[]{"--unknown=val", "-a", "extra"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--unknown=val", "-a", "extra"}, result);
    }

    // Tests end-to-end parse execution for attached short option without equal sign
    @Test
    public void testParse_attachedShortOptionWithoutEqualSign_parsesSuccessfully() throws Exception {
        String[] args = new String[]{"-b1024"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("b"));
        assertEquals("1024", cmd.getOptionValue("b"));
    }
}