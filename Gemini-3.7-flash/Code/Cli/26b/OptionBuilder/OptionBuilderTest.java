package org.apache.commons.cli;

import org.junit.Test;
import java.lang.reflect.Constructor;
import static org.junit.Assert.*;

public class OptionBuilderTest
{
    // Tests creating an option with all properties set via char create method
    @Test
    public void testCreate_charWithAllProperties_createsConfiguredOption()
    {
        Option opt = OptionBuilder.withLongOpt("test-long")
                                  .withDescription("testing description")
                                  .isRequired()
                                  .hasArg()
                                  .withArgName("myArg")
                                  .withType(String.class)
                                  .withValueSeparator(':')
                                  .create('t');

        assertEquals("t", opt.getOpt());
        assertEquals("test-long", opt.getLongOpt());
        assertEquals("testing description", opt.getDescription());
        assertTrue(opt.isRequired());
        assertEquals(1, opt.getArgs());
        assertEquals("myArg", opt.getArgName());
        assertEquals(String.class, opt.getType());
        assertEquals(':', opt.getValueSeparator());
    }

    // Tests creating an option using a String name
    @Test
    public void testCreate_stringOpt_createsConfiguredOption()
    {
        Option opt = OptionBuilder.withLongOpt("opt-long")
                                  .withDescription("desc")
                                  .isRequired(true)
                                  .create("opt");

        assertEquals("opt", opt.getOpt());
        assertEquals("opt-long", opt.getLongOpt());
        assertEquals("desc", opt.getDescription());
        assertTrue(opt.isRequired());
    }

    // Tests creating a long-only option using create() without argument
    @Test
    public void testCreate_noOptWithLongOpt_createsLongOnlyOption()
    {
        Option opt = OptionBuilder.withLongOpt("only-long")
                                  .withDescription("desc")
                                  .create();

        assertNull(opt.getOpt());
        assertEquals("only-long", opt.getLongOpt());
        assertEquals("desc", opt.getDescription());
    }

    // Tests exception path when create() is called without longopt set
    @Test(expected = IllegalArgumentException.class)
    public void testCreate_noLongOptSpecified_throwsIllegalArgumentException()
    {
        OptionBuilder.withDescription("desc").create();
    }

    // Tests that builder resets internal state after create() call
    @Test
    public void testCreate_subsequentCreate_resetsState()
    {
        OptionBuilder.withLongOpt("first")
                     .withDescription("first desc")
                     .isRequired()
                     .hasArg()
                     .withArgName("arg1")
                     .withType(Integer.class)
                     .withValueSeparator(',')
                     .create('a');

        Option opt2 = OptionBuilder.create('b');

        assertEquals("b", opt2.getOpt());
        assertNull(opt2.getLongOpt());
        assertNull(opt2.getDescription());
        assertFalse(opt2.isRequired());
        assertEquals(Option.UNINITIALIZED, opt2.getArgs());
        assertEquals("arg", opt2.getArgName());
        assertNull(opt2.getType());
        assertEquals((char) 0, opt2.getValueSeparator());
        assertFalse(opt2.hasOptionalArg());
    }

    // Tests hasArg with boolean true
    @Test
    public void testHasArg_booleanTrue_setsSingleArg()
    {
        Option opt = OptionBuilder.hasArg(true).create('a');

        assertEquals(1, opt.getArgs());
    }

    // Tests hasArg with boolean false
    @Test
    public void testHasArg_booleanFalse_setsUninitializedArgs()
    {
        Option opt = OptionBuilder.hasArg(false).create('a');

        assertEquals(Option.UNINITIALIZED, opt.getArgs());
    }

    // Tests hasArgs with no parameter sets unlimited args
    @Test
    public void testHasArgs_noParam_setsUnlimitedArgs()
    {
        Option opt = OptionBuilder.hasArgs().create('a');

        assertEquals(Option.UNLIMITED_VALUES, opt.getArgs());
    }

    // Tests hasArgs with explicit count
    @Test
    public void testHasArgs_withCount_setsSpecifiedArgs()
    {
        Option opt = OptionBuilder.hasArgs(3).create('a');

        assertEquals(3, opt.getArgs());
    }

    // Tests hasOptionalArg sets single optional argument
    @Test
    public void testHasOptionalArg_noParam_setsSingleOptionalArg()
    {
        Option opt = OptionBuilder.hasOptionalArg().create('a');

        assertEquals(1, opt.getArgs());
        assertTrue(opt.hasOptionalArg());
    }

    // Tests hasOptionalArgs with no parameter sets unlimited optional args
    @Test
    public void testHasOptionalArgs_noParam_setsUnlimitedOptionalArgs()
    {
        Option opt = OptionBuilder.hasOptionalArgs().create('a');

        assertEquals(Option.UNLIMITED_VALUES, opt.getArgs());
        assertTrue(opt.hasOptionalArg());
    }

    // Tests hasOptionalArgs with explicit count
    @Test
    public void testHasOptionalArgs_withCount_setsSpecifiedOptionalArgs()
    {
        Option opt = OptionBuilder.hasOptionalArgs(2).create('a');

        assertEquals(2, opt.getArgs());
        assertTrue(opt.hasOptionalArg());
    }

    // Tests default withValueSeparator sets '='
    @Test
    public void testWithValueSeparator_default_setsEqualsSeparator()
    {
        Option opt = OptionBuilder.withValueSeparator().create('a');

        assertEquals('=', opt.getValueSeparator());
    }

    // Tests withValueSeparator with custom char
    @Test
    public void testWithValueSeparator_customChar_setsCustomSeparator()
    {
        Option opt = OptionBuilder.withValueSeparator(';').create('a');

        assertEquals(';', opt.getValueSeparator());
    }

    // Tests isRequired with boolean false
    @Test
    public void testIsRequired_booleanFalse_setsNotRequired()
    {
        Option opt = OptionBuilder.isRequired(false).create('a');

        assertFalse(opt.isRequired());
    }

    // Tests withType using an Object instance
    @Test
    public void testWithType_objectType_setsObject()
    {
        Object typeObj = new Object();
        Option opt = OptionBuilder.withType(typeObj).create('a');

        assertEquals(typeObj, opt.getType());
    }

    // Tests private constructor invocation for code coverage
    @Test
    public void testPrivateConstructor() throws Exception
    {
        Constructor<OptionBuilder> constructor = OptionBuilder.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        OptionBuilder instance = constructor.newInstance();
        assertNotNull(instance);
    }
}