package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

public class OptionBuilderTest
{
    // Tests creating an option with all builder properties configured using char opt
    @Test
    public void testCompleteOption_validProperties_createsConfiguredOption()
    {
        Option opt = OptionBuilder.withLongOpt("simple-option")
                                  .withDescription("this is a simple option")
                                  .hasArg()
                                  .isRequired()
                                  .hasArgs()
                                  .hasOptionalArg()
                                  .withType(Float.class)
                                  .withValueSeparator(':')
                                  .withArgName("argName")
                                  .create('a');

        assertEquals("a", opt.getOpt());
        assertEquals("simple-option", opt.getLongOpt());
        assertEquals("this is a simple option", opt.getDescription());
        assertTrue(opt.isRequired());
        assertTrue(opt.hasOptionalArg());
        assertEquals(1, opt.getArgs());
        assertEquals(Float.class, opt.getType());
        assertEquals(':', opt.getValueSeparator());
        assertEquals("argName", opt.getArgName());
    }

    // Tests create without longopt throws exception and resets state
    @Test(expected = IllegalArgumentException.class)
    public void testCreate_noLongOpt_throwsIllegalArgumentException()
    {
        OptionBuilder.withDescription("description only").create();
    }

    // Tests create using longopt only without short opt
    @Test
    public void testCreate_withLongOpt_createsOption()
    {
        Option opt = OptionBuilder.withLongOpt("only-long").create();
        assertNull(opt.getOpt());
        assertEquals("only-long", opt.getLongOpt());
    }

    // Tests create with String opt
    @Test
    public void testCreateString_validString_createsOption()
    {
        Option opt = OptionBuilder.withDescription("desc").create("optString");
        assertEquals("optString", opt.getOpt());
        assertEquals("desc", opt.getDescription());
    }

    // Tests hasArg with and without boolean argument
    @Test
    public void testHasArg_booleanVariants_setsArgsCorrectly()
    {
        Option optTrue = OptionBuilder.hasArg(true).create('t');
        assertEquals(1, optTrue.getArgs());

        Option optFalse = OptionBuilder.hasArg(false).create('f');
        assertEquals(Option.UNINITIALIZED, optFalse.getArgs());

        Option optDefault = OptionBuilder.hasArg().create('d');
        assertEquals(1, optDefault.getArgs());
    }

    // Tests isRequired with and without boolean argument
    @Test
    public void testIsRequired_booleanVariants_setsRequiredCorrectly()
    {
        Option optTrue = OptionBuilder.isRequired(true).create('t');
        assertTrue(optTrue.isRequired());

        Option optFalse = OptionBuilder.isRequired(false).create('f');
        assertFalse(optFalse.isRequired());

        Option optDefault = OptionBuilder.isRequired().create('d');
        assertTrue(optDefault.isRequired());
    }

    // Tests withValueSeparator default '=' and custom char
    @Test
    public void testWithValueSeparator_defaultAndCustomChar_setsSeparator()
    {
        Option optDefault = OptionBuilder.withValueSeparator().create('d');
        assertEquals('=', optDefault.getValueSeparator());

        Option optCustom = OptionBuilder.withValueSeparator(';').create('c');
        assertEquals(';', optCustom.getValueSeparator());
    }

    // Tests hasArgs with unlimited values and with specific count
    @Test
    public void testHasArgs_unlimitedAndCount_setsArgsCorrectly()
    {
        Option optUnlimited = OptionBuilder.hasArgs().create('u');
        assertEquals(Option.UNLIMITED_VALUES, optUnlimited.getArgs());

        Option optCount = OptionBuilder.hasArgs(5).create('c');
        assertEquals(5, optCount.getArgs());
    }

    // Tests hasOptionalArgs with unlimited and specific count
    @Test
    public void testHasOptionalArgs_unlimitedAndCount_setsOptionalArgsCorrectly()
    {
        Option optUnlimited = OptionBuilder.hasOptionalArgs().create('u');
        assertTrue(optUnlimited.hasOptionalArg());
        assertEquals(Option.UNLIMITED_VALUES, optUnlimited.getArgs());

        Option optCount = OptionBuilder.hasOptionalArgs(3).create('c');
        assertTrue(optCount.hasOptionalArg());
        assertEquals(3, optCount.getArgs());
    }

    // Tests withType method
    @Test
    public void testWithType_customType_setsType()
    {
        Option opt = OptionBuilder.withType(Integer.class).create('i');
        assertEquals(Integer.class, opt.getType());
    }

    // Tests withDescription method
    @Test
    public void testWithDescription_customDescription_setsDescription()
    {
        Option opt = OptionBuilder.withDescription("my description").create('d');
        assertEquals("my description", opt.getDescription());
    }

    // Tests withArgName method
    @Test
    public void testWithArgName_customArgName_setsArgName()
    {
        Option opt = OptionBuilder.withArgName("customName").create('a');
        assertEquals("customName", opt.getArgName());
    }

    // Tests state reset after option creation
    @Test
    public void testReset_afterOptionCreation_defaultsRestored()
    {
        OptionBuilder.withLongOpt("first")
                     .withDescription("first desc")
                     .isRequired()
                     .hasArg()
                     .withType(String.class)
                     .withValueSeparator('=')
                     .withArgName("firstArg")
                     .create('f');

        Option second = OptionBuilder.create('s');
        assertNull(second.getLongOpt());
        assertNull(second.getDescription());
        assertFalse(second.isRequired());
        assertEquals(Option.UNINITIALIZED, second.getArgs());
        assertNull(second.getType());
        assertEquals((char) 0, second.getValueSeparator());
        assertEquals("arg", second.getArgName());
        assertFalse(second.hasOptionalArg());
    }

    // Tests standalone hasOptionalArg method
    @Test
    public void testHasOptionalArg_standalone_setsOptionalArgAndSingleArg()
    {
        Option opt = OptionBuilder.hasOptionalArg().create('o');
        assertTrue(opt.hasOptionalArg());
        assertEquals(1, opt.getArgs());
    }

    // Tests withType accepting Object
    @Test
    public void testWithType_objectArgument_setsType()
    {
        Object typeObj = new Object();
        Option opt = OptionBuilder.withType(typeObj).create('o');
        assertEquals(typeObj, opt.getType());
    }

    // Tests state reset when create() fails due to missing longOpt
    @Test
    public void testReset_afterCreateException_defaultsRestored()
    {
        try
        {
            OptionBuilder.withDescription("desc to discard").create();
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // Expected exception
        }

        Option nextOpt = OptionBuilder.create('n');
        assertNull(nextOpt.getDescription());
    }
}