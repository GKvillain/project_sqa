package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PosixParserTest
{
    private PosixParser parser;
    private Options options;

    @Before
    public void setUp()
    {
        parser = new PosixParser();
        options = new Options();
    }

    // Tests flattening with empty arguments array
    @Test
    public void testFlatten_emptyArguments_returnsEmptyArray()
    {
        String[] args = new String[0];
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[0], result);
    }

    // Tests single hyphen token preserved
    @Test
    public void testFlatten_singleHyphen_preservedAsToken()
    {
        String[] args = new String[] { "-" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "-" }, result);
    }

    // Tests double hyphen token preserved
    @Test
    public void testFlatten_doubleHyphen_handledCorrectly()
    {
        options.addOption("a", "alpha", false, "alpha option");
        String[] args = new String[] { "--", "-a" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "--", "-a" }, result);
    }

    // Tests valid long option without argument
    @Test
    public void testFlatten_validLongOption_returnsFlattenedLongOption()
    {
        options.addOption("f", "foo", false, "foo option");
        String[] args = new String[] { "--foo" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "--foo" }, result);
    }

    // Tests valid long option with '=' value separator
    @Test
    public void testFlatten_validLongOptionWithValue_splitsOptionAndValue()
    {
        options.addOption("f", "foo", true, "foo option with arg");
        String[] args = new String[] { "--foo=bar" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "--foo", "bar" }, result);
    }

    // Tests unrecognized long option triggers non-option processing when stopAtNonOption is true
    @Test
    public void testFlatten_unknownLongOptionStopAtNonOption_addsRemainingTokens()
    {
        options.addOption("a", false, "a option");
        String[] args = new String[] { "--unknown=value", "-a", "extra" };
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] { "--", "--unknown=value", "-a", "extra" }, result);
    }

    // Tests recognized single-character option token
    @Test
    public void testFlatten_validSingleCharOption_returnsOptionToken()
    {
        options.addOption("a", false, "a option");
        options.addOption("b", false, "b option");
        String[] args = new String[] { "-a", "-b" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "-a", "-b" }, result);
    }

    // Tests unknown single-character option when stopAtNonOption is false
    @Test
    public void testFlatten_unknownSingleCharOptionNoStop_returnsTokenDirectly()
    {
        String[] args = new String[] { "-z" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "-z" }, result);
    }

    // Tests unknown single-character option when stopAtNonOption is true triggers eating remaining tokens
    @Test
    public void testFlatten_unknownSingleCharOptionStopAtNonOption_eatsTheRest()
    {
        options.addOption("a", false, "a option");
        String[] args = new String[] { "-z", "-a", "param" };
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] { "-z", "-a", "param" }, result);
    }

    // Tests bursting multiple concatenated flags without argument
    @Test
    public void testFlatten_burstMultipleFlags_burstsIntoIndividualOptions()
    {
        options.addOption("a", false, "a option");
        options.addOption("b", false, "b option");
        options.addOption("c", false, "c option");
        String[] args = new String[] { "-abc" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "-a", "-b", "-c" }, result);
    }

    // Tests bursting option that takes argument followed by value
    @Test
    public void testFlatten_burstOptionWithArgValue_splitsOptionAndArgumentValue()
    {
        options.addOption("s", "size", true, "size option");
        String[] args = new String[] { "-s100" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "-s", "100" }, result);
    }

    // Tests bursting option with non-option character and stopAtNonOption is true
    @Test
    public void testFlatten_burstTokenUnknownCharStopAtNonOption_addsDoubleHyphenAndEatsRest()
    {
        options.addOption("a", false, "a option");
        String[] args = new String[] { "-az", "extra" };
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] { "-a", "--", "z", "extra" }, result);
    }

    // Tests bursting option with non-option character when stopAtNonOption is false
    @Test
    public void testFlatten_burstTokenUnknownCharNoStop_addsFullToken()
    {
        options.addOption("a", false, "a option");
        String[] args = new String[] { "-az" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "-a", "-az" }, result);
    }

    // Tests non-option token when stopAtNonOption is false
    @Test
    public void testFlatten_nonOptionTokenNoStop_addsTokenDirectly()
    {
        options.addOption("a", false, "a option");
        String[] args = new String[] { "nonOption1", "-a", "nonOption2" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "nonOption1", "-a", "nonOption2" }, result);
    }

    // Tests non-option token when stopAtNonOption is true stops further processing
    @Test
    public void testFlatten_nonOptionTokenStopAtNonOption_prependsDoubleHyphenAndEatsRest()
    {
        options.addOption("a", false, "a option");
        String[] args = new String[] { "nonOption", "-a", "other" };
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] { "--", "nonOption", "-a", "other" }, result);
    }

    // Tests end-to-end parse method using PosixParser
    @Test
    public void testParse_standardOptionsAndArgs_correctCommandLineGenerated() throws Exception
    {
        options.addOption("a", "all", false, "do not ignore entries starting with .");
        options.addOption("l", false, "use a long listing format");
        options.addOption("f", "file", true, "target file");

        String[] args = new String[] { "-al", "--file", "output.txt", "file1.txt", "file2.txt" };
        CommandLine cl = parser.parse(options, args);

        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("l"));
        assertTrue(cl.hasOption("f"));
        assertEquals("output.txt", cl.getOptionValue("f"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("file1.txt", cl.getArgs()[0]);
        assertEquals("file2.txt", cl.getArgs()[1]);
    }

    // Tests full parse with stopAtNonOption enabled
    @Test
    public void testParse_stopAtNonOptionEnabled_stopsOptionParsingAtNonOption() throws Exception
    {
        options.addOption("a", false, "alpha option");
        options.addOption("b", false, "beta option");

        String[] args = new String[] { "-a", "nonOptionTarget", "-b" };
        CommandLine cl = parser.parse(options, args, true);

        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("nonOptionTarget", cl.getArgs()[0]);
        assertEquals("-b", cl.getArgs()[1]);
    }

    // Tests unknown long option without '=' value separator when stopAtNonOption is false
    @Test
    public void testFlatten_unknownLongOptionNoStop_returnsTokenDirectly()
    {
        String[] args = new String[] { "--unknown", "--unknown=value" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "--unknown", "--unknown=value" }, result);
    }

    // Tests unknown long option without '=' value separator when stopAtNonOption is true
    @Test
    public void testFlatten_unknownLongOptionNoValueStopAtNonOption_addsRemainingTokens()
    {
        options.addOption("a", false, "a option");
        String[] args = new String[] { "--unknown", "-a", "extra" };
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] { "--unknown", "-a", "extra" }, result);
    }

    // Tests burst when an option with argument is the last character in the burst token
    @Test
    public void testFlatten_burstOptionWithArgAtEnd_splitsOptionFlagsAndPreservesNextArg()
    {
        options.addOption("a", false, "a option");
        options.addOption("s", "size", true, "size option");
        String[] args = new String[] { "-as", "100" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "-a", "-s", "100" }, result);
    }

    // Tests burst when the first character after hyphen is unknown and stopAtNonOption is true
    @Test
    public void testFlatten_burstTokenUnknownFirstCharStopAtNonOption_addsDoubleHyphenAndEatsRest()
    {
        String[] args = new String[] { "-xyz", "param" };
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] { "--", "xyz", "param" }, result);
    }

    // Tests burst when the first character after hyphen is unknown and stopAtNonOption is false
    @Test
    public void testFlatten_burstTokenUnknownFirstCharNoStop_addsFullToken()
    {
        String[] args = new String[] { "-xyz" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "-xyz" }, result);
    }

    // Tests recognized option taking argument followed by non-option token when stopAtNonOption is true
    @Test
    public void testFlatten_optionWithArgFollowedByArgument_doesNotTriggerStopAtNonOption()
    {
        options.addOption("f", "file", true, "target file");
        String[] args = new String[] { "-f", "output.txt", "nonOption" };
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[] { "-f", "output.txt", "nonOption" }, result);
    }

    // Tests multi-character option defined in options matching token directly
    @Test
    public void testFlatten_multiCharOptionToken_processedDirectly()
    {
        options.addOption("-D", true, "property option");
        String[] args = new String[] { "-D", "key=value" };
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[] { "-D", "key=value" }, result);
    }
}