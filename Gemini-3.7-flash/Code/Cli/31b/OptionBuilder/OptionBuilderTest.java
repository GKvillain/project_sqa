package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

public class OptionBuilderTest
{
    // Tests create with char opt and full properties set
    @Test
    public void testCreateChar_allPropertiesSet_createsConfiguredOption()
    {
        Option option = OptionBuilder.withLongOpt("file")
                                     .withDescription("target file")
                                     .withArgName("filename")
                                     .isRequired()
                                     .hasArg()
                                     .withType(String.class)
                                     .withValueSeparator(':')
                                     .create('f');

        assertEquals("f", option.getOpt());
        assertEquals("file", option.getLongOpt());
        assertEquals("target file", option.getDescription());
        assertEquals("filename", option.getArgName());
        assertTrue(option.isRequired());
        assertEquals(1, option.getArgs());
        assertEquals(String.class, option.getType());
        assertEquals(':', option.getValueSeparator());
    }

    // Tests create with String opt
    @Test
    public void testCreateString_validStringOpt_createsConfiguredOption()
    {
        Option option = OptionBuilder.withLongOpt("directory")
                                     .withDescription("target directory")
                                     .create("dir");

        assertEquals("dir", option.getOpt());
        assertEquals("directory", option.getLongOpt());
        assertEquals("target directory", option.getDescription());
    }

    // Tests create without opt when longOpt is provided
    @Test
    public void testCreate_withLongOpt_createsOptionWithNullOpt()
    {
        Option option = OptionBuilder.withLongOpt("verbose")
                                     .withDescription("verbose mode")
                                     .create();

        assertNull(option.getOpt());
        assertEquals("verbose", option.getLongOpt());
        assertEquals("verbose mode", option.getDescription());
    }

    // Tests create without longOpt throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreate_missingLongOpt_throwsIllegalArgumentException()
    {
        OptionBuilder.withDescription("no long opt").create();
    }

    // Tests hasArg with boolean parameter true
    @Test
    public void testHasArgBoolean_true_setsNumberOfArgsToOne()
    {
        Option option = OptionBuilder.hasArg(true).create('a');

        assertEquals(1, option.getArgs());
        assertTrue(option.hasArg());
    }

    // Tests hasArg with boolean parameter false
    @Test
    public void testHasArgBoolean_false_setsNumberOfArgsToUninitialized()
    {
        Option option = OptionBuilder.hasArg(false).create('a');

        assertEquals(Option.UNINITIALIZED, option.getArgs());
        assertFalse(option.hasArg());
    }

    // Tests isRequired with boolean parameter true and false
    @Test
    public void testIsRequiredBoolean_trueAndFalse_setsRequiredCorrectly()
    {
        Option requiredOption = OptionBuilder.isRequired(true).create('r');
        assertTrue(requiredOption.isRequired());

        Option optionalOption = OptionBuilder.isRequired(false).create('o');
        assertFalse(optionalOption.isRequired());
    }

    // Tests hasArgs with unlimited arguments
    @Test
    public void testHasArgs_unlimited_setsUnlimitedValues()
    {
        Option option = OptionBuilder.hasArgs().create('m');

        assertEquals(Option.UNLIMITED_VALUES, option.getArgs());
        assertTrue(option.hasArgs());
    }

    // Tests hasArgs with specified number of arguments
    @Test
    public void testHasArgsInt_specificCount_setsExactArgCount()
    {
        Option option = OptionBuilder.hasArgs(3).create('t');

        assertEquals(3, option.getArgs());
        assertTrue(option.hasArgs());
    }

    // Tests hasOptionalArg setting single optional argument
    @Test
    public void testHasOptionalArg_default_setsSingleOptionalArg()
    {
        Option option = OptionBuilder.hasOptionalArg().create('o');

        assertEquals(1, option.getArgs());
        assertTrue(option.hasOptionalArg());
    }

    // Tests hasOptionalArgs setting unlimited optional arguments
    @Test
    public void testHasOptionalArgs_unlimited_setsUnlimitedOptionalArgs()
    {
        Option option = OptionBuilder.hasOptionalArgs().create('o');

        assertEquals(Option.UNLIMITED_VALUES, option.getArgs());
        assertTrue(option.hasOptionalArg());
    }

    // Tests hasOptionalArgs with specified argument count
    @Test
    public void testHasOptionalArgsInt_specificCount_setsExactOptionalArgs()
    {
        Option option = OptionBuilder.hasOptionalArgs(2).create('o');

        assertEquals(2, option.getArgs());
        assertTrue(option.hasOptionalArg());
    }

    // Tests default withValueSeparator sets '='
    @Test
    public void testWithValueSeparator_default_setsEqualsSeparator()
    {
        Option option = OptionBuilder.withValueSeparator().create('D');

        assertEquals('=', option.getValueSeparator());
    }

    // Tests state reset after create so subsequent options are clean
    @Test
    public void testReset_afterCreate_resetsBuilderState()
    {
        OptionBuilder.withLongOpt("first")
                     .withDescription("first desc")
                     .isRequired()
                     .hasArgs(2)
                     .withValueSeparator('=')
                     .create('f');

        Option secondOption = OptionBuilder.create('s');

        assertNull(secondOption.getLongOpt());
        assertNull(secondOption.getDescription());
        assertFalse(secondOption.isRequired());
        assertEquals(Option.UNINITIALIZED, secondOption.getArgs());
        assertEquals((char) 0, secondOption.getValueSeparator());
    }

    // Tests state reset even when create throws exception
    @Test
    public void testReset_onException_resetsBuilderState()
    {
        try
        {
            OptionBuilder.withLongOpt("invalid")
                         .withDescription("invalid option")
                         .create("invalid opt with spaces");
            fail("Expected IllegalArgumentException for invalid option name");
        }
        catch (IllegalArgumentException expected)
        {
            // Expected exception
        }

        Option nextOption = OptionBuilder.create('c');
        assertNull(nextOption.getLongOpt());
        assertNull(nextOption.getDescription());
    }
}