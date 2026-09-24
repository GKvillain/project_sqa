package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

public class OptionBuilderTest
{
    // Tests create with short option char and complete property chain
    @Test
    public void testCreate_charWithAllProperties_returnsConfiguredOption()
    {
        Option option = OptionBuilder.withLongOpt("simple")
                                     .withDescription("simple option")
                                     .isRequired()
                                     .hasArg()
                                     .withArgName("arg")
                                     .withType(String.class)
                                     .withValueSeparator('=')
                                     .create('s');

        assertEquals("s", option.getOpt());
        assertEquals("simple", option.getLongOpt());
        assertEquals("simple option", option.getDescription());
        assertTrue(option.isRequired());
        assertEquals(1, option.getArgs());
        assertEquals("arg", option.getArgName());
        assertEquals(String.class, option.getType());
        assertEquals('=', option.getValueSeparator());
        assertTrue(option.hasValueSeparator());
    }

    // Tests create with String option identifier
    @Test
    public void testCreate_stringOpt_returnsConfiguredOption()
    {
        Option option = OptionBuilder.withLongOpt("opt-string")
                                     .withDescription("desc")
                                     .create("opt");

        assertEquals("opt", option.getOpt());
        assertEquals("opt-string", option.getLongOpt());
        assertEquals("desc", option.getDescription());
    }

    // Tests create with long option only
    @Test
    public void testCreate_noShortOptWithLongOpt_returnsOptionWithNullShortOpt()
    {
        Option option = OptionBuilder.withLongOpt("only-long")
                                     .withDescription("only long opt")
                                     .create();

        assertNull(option.getOpt());
        assertEquals("only-long", option.getLongOpt());
        assertEquals("only long opt", option.getDescription());
    }

    // Tests create without specifying longopt throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreate_noLongOptSpecified_throwsIllegalArgumentException()
    {
        OptionBuilder.withDescription("missing longopt").create();
    }

    // Tests reset after create ensures subsequent builder starts with clean state
    @Test
    public void testReset_afterCreate_resetsAllProperties()
    {
        OptionBuilder.withLongOpt("first")
                     .withDescription("first desc")
                     .isRequired()
                     .hasArg()
                     .withArgName("arg1")
                     .withType(Integer.class)
                     .withValueSeparator(':')
                     .create('f');

        Option second = OptionBuilder.create('s');

        assertEquals("s", second.getOpt());
        assertNull(second.getLongOpt());
        assertNull(second.getDescription());
        assertFalse(second.isRequired());
        assertEquals(Option.UNINITIALIZED, second.getArgs());
        assertNull(second.getArgName());
        assertNull(second.getType());
        assertEquals((char) 0, second.getValueSeparator());
        assertFalse(second.hasOptionalArg());
    }

    // Tests hasArg with boolean true branch
    @Test
    public void testHasArg_trueBoolean_setsArgsToOne()
    {
        Option option = OptionBuilder.hasArg(true).create('a');

        assertEquals(1, option.getArgs());
        assertTrue(option.hasArg());
    }

    // Tests hasArg with boolean false branch
    @Test
    public void testHasArg_falseBoolean_setsArgsToUninitialized()
    {
        Option option = OptionBuilder.hasArg(false).create('a');

        assertEquals(Option.UNINITIALIZED, option.getArgs());
        assertFalse(option.hasArg());
    }

    // Tests hasArgs with unlimited values
    @Test
    public void testHasArgs_noArg_setsUnlimitedValues()
    {
        Option option = OptionBuilder.hasArgs().create('m');

        assertEquals(Option.UNLIMITED_VALUES, option.getArgs());
        assertTrue(option.hasArgs());
    }

    // Tests hasArgs with specific number of values
    @Test
    public void testHasArgs_specificNumber_setsGivenArgCount()
    {
        Option option = OptionBuilder.hasArgs(3).create('m');

        assertEquals(3, option.getArgs());
        assertTrue(option.hasArgs());
    }

    // Tests hasOptionalArg sets optional flag and 1 argument
    @Test
    public void testHasOptionalArg_default_setsOptionalAndOneArg()
    {
        Option option = OptionBuilder.hasOptionalArg().create('o');

        assertTrue(option.hasOptionalArg());
        assertEquals(1, option.getArgs());
    }

    // Tests hasOptionalArgs with unlimited values
    @Test
    public void testHasOptionalArgs_noArg_setsOptionalAndUnlimitedValues()
    {
        Option option = OptionBuilder.hasOptionalArgs().create('o');

        assertTrue(option.hasOptionalArg());
        assertEquals(Option.UNLIMITED_VALUES, option.getArgs());
    }

    // Tests hasOptionalArgs with specific number of values
    @Test
    public void testHasOptionalArgs_specificNumber_setsOptionalAndCount()
    {
        Option option = OptionBuilder.hasOptionalArgs(2).create('o');

        assertTrue(option.hasOptionalArg());
        assertEquals(2, option.getArgs());
    }

    // Tests default withValueSeparator sets '='
    @Test
    public void testWithValueSeparator_default_setsEqualsChar()
    {
        Option option = OptionBuilder.withValueSeparator().create('v');

        assertEquals('=', option.getValueSeparator());
        assertTrue(option.hasValueSeparator());
    }

    // Tests custom withValueSeparator character
    @Test
    public void testWithValueSeparator_customChar_setsSpecifiedChar()
    {
        Option option = OptionBuilder.withValueSeparator(':').create('v');

        assertEquals(':', option.getValueSeparator());
        assertTrue(option.hasValueSeparator());
    }

    // Tests isRequired with boolean true and false
    @Test
    public void testIsRequired_booleanParam_setsRequiredFlag()
    {
        Option optTrue = OptionBuilder.isRequired(true).create('t');
        assertTrue(optTrue.isRequired());

        Option optFalse = OptionBuilder.isRequired(false).create('f');
        assertFalse(optFalse.isRequired());
    }

    // Tests withType sets the expected Class object
    @Test
    public void testWithType_classType_setsTypeProperty()
    {
        Option option = OptionBuilder.withType(Double.class).create('d');

        assertEquals(Double.class, option.getType());
    }

    // Tests create with invalid option name throws IllegalArgumentException and resets builder
    @Test
    public void testCreate_invalidOptChar_throwsIllegalArgumentExceptionAndResets()
    {
        try
        {
            OptionBuilder.withLongOpt("invalid").create('?');
            fail("Expected IllegalArgumentException for invalid option character");
        }
        catch (IllegalArgumentException e)
        {
            // Expected exception; verify state was reset in finally block
            Option option = OptionBuilder.create("valid");
            assertNull(option.getLongOpt());
        }
    }

    // Tests create with invalid String option name throws IllegalArgumentException and resets builder
    @Test
    public void testCreate_invalidOptString_throwsIllegalArgumentExceptionAndResets()
    {
        try
        {
            OptionBuilder.withLongOpt("invalid").create("invalid opt!");
            fail("Expected IllegalArgumentException for invalid option string");
        }
        catch (IllegalArgumentException e)
        {
            // Expected exception; verify state was reset in finally block
            Option option = OptionBuilder.create("valid");
            assertNull(option.getLongOpt());
        }
    }
}