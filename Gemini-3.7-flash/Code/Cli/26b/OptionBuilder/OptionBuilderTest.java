package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

public class OptionBuilderTest {

    // Tests create with char opt and full properties
    @Test
    public void testCreate_charWithAllProperties_returnsConfiguredOption() {
        Option option = OptionBuilder.withLongOpt("simple-option")
                                     .withDescription("this is a simple option")
                                     .withArgName("dim")
                                     .isRequired()
                                     .hasArgs(2)
                                     .withType(Float.class)
                                     .withValueSeparator(':')
                                     .create('s');

        assertEquals("s", option.getOpt());
        assertEquals("simple-option", option.getLongOpt());
        assertEquals("this is a simple option", option.getDescription());
        assertEquals("dim", option.getArgName());
        assertTrue(option.isRequired());
        assertEquals(2, option.getArgs());
        assertEquals(Float.class, option.getType());
        assertEquals(':', option.getValueSeparator());
        assertTrue(option.hasValueSeparator());
    }

    // Tests create with String opt
    @Test
    public void testCreate_stringOpt_returnsConfiguredOption() {
        Option option = OptionBuilder.withLongOpt("opt-string")
                                     .withDescription("desc")
                                     .create("opt");

        assertEquals("opt", option.getOpt());
        assertEquals("opt-string", option.getLongOpt());
        assertEquals("desc", option.getDescription());
    }

    // Tests create with no opt using longopt
    @Test
    public void testCreate_noOptArg_returnsOptionWithLongOptOnly() {
        Option option = OptionBuilder.withLongOpt("long-only")
                                     .withDescription("desc only long")
                                     .create();

        assertNull(option.getOpt());
        assertEquals("long-only", option.getLongOpt());
        assertEquals("desc only long", option.getDescription());
    }

    // Tests create without longOpt throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreate_noLongOptSet_throwsIllegalArgumentException() {
        OptionBuilder.withDescription("desc").create();
    }

    // Tests state reset after successful create
    @Test
    public void testReset_afterSuccessfulCreate_clearsState() {
        OptionBuilder.withLongOpt("first")
                     .withDescription("first desc")
                     .withArgName("arg1")
                     .isRequired()
                     .hasArg()
                     .withType(Integer.class)
                     .withValueSeparator('=')
                     .create('a');

        Option second = OptionBuilder.create('b');

        assertEquals("b", second.getOpt());
        assertNull(second.getLongOpt());
        assertNull(second.getDescription());
        assertFalse(second.isRequired());
        assertEquals(Option.UNINITIALIZED, second.getArgs());
        assertNull(second.getType());
        assertEquals((char) 0, second.getValueSeparator());
    }

    // Tests state reset after exception thrown during create
    @Test
    public void testReset_afterFailedCreate_clearsStateForNextBuild() {
        try {
            OptionBuilder.withLongOpt("bad-opt")
                         .withDescription("bad desc")
                         .create("invalid?char");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        Option nextOption = OptionBuilder.create('c');
        assertEquals("c", nextOption.getOpt());
        assertNull(nextOption.getLongOpt());
        assertNull(nextOption.getDescription());
    }

    // Tests hasArg() default parameterless method
    @Test
    public void testHasArg_noParams_setsSingleArg() {
        Option option = OptionBuilder.hasArg().create('h');

        assertEquals(1, option.getArgs());
        assertTrue(option.hasArg());
    }

    // Tests hasArg(boolean) with true
    @Test
    public void testHasArg_booleanTrue_setsSingleArg() {
        Option option = OptionBuilder.hasArg(true).create('t');

        assertEquals(1, option.getArgs());
        assertTrue(option.hasArg());
    }

    // Tests hasArg(boolean) with false
    @Test
    public void testHasArg_booleanFalse_setsUninitializedArgs() {
        Option option = OptionBuilder.hasArg(false).create('f');

        assertEquals(Option.UNINITIALIZED, option.getArgs());
        assertFalse(option.hasArg());
    }

    // Tests hasArgs() unlimited arguments
    @Test
    public void testHasArgs_noParams_setsUnlimitedArgs() {
        Option option = OptionBuilder.hasArgs().create('m');

        assertEquals(Option.UNLIMITED_VALUES, option.getArgs());
        assertTrue(option.hasArgs());
    }

    // Tests hasArgs(int) with specific count
    @Test
    public void testHasArgs_specificCount_setsArgsCount() {
        Option option = OptionBuilder.hasArgs(3).create('c');

        assertEquals(3, option.getArgs());
        assertTrue(option.hasArgs());
    }

    // Tests hasOptionalArg() single optional argument
    @Test
    public void testHasOptionalArg_noParams_setsSingleOptionalArg() {
        Option option = OptionBuilder.hasOptionalArg().create('o');

        assertEquals(1, option.getArgs());
        assertTrue(option.hasOptionalArg());
    }

    // Tests hasOptionalArgs() unlimited optional arguments
    @Test
    public void testHasOptionalArgs_noParams_setsUnlimitedOptionalArgs() {
        Option option = OptionBuilder.hasOptionalArgs().create('u');

        assertEquals(Option.UNLIMITED_VALUES, option.getArgs());
        assertTrue(option.hasOptionalArg());
    }

    // Tests hasOptionalArgs(int) specific number of optional arguments
    @Test
    public void testHasOptionalArgs_specificCount_setsOptionalArgsCount() {
        Option option = OptionBuilder.hasOptionalArgs(4).create('k');

        assertEquals(4, option.getArgs());
        assertTrue(option.hasOptionalArg());
    }

    // Tests isRequired(boolean) with true
    @Test
    public void testIsRequired_booleanTrue_setsRequiredTrue() {
        Option option = OptionBuilder.isRequired(true).create('r');

        assertTrue(option.isRequired());
    }

    // Tests isRequired(boolean) with false
    @Test
    public void testIsRequired_booleanFalse_setsRequiredFalse() {
        Option option = OptionBuilder.isRequired(false).create('n');

        assertFalse(option.isRequired());
    }

    // Tests withValueSeparator() default character '='
    @Test
    public void testWithValueSeparator_default_setsEqualsSeparator() {
        Option option = OptionBuilder.withValueSeparator().create('d');

        assertEquals('=', option.getValueSeparator());
        assertTrue(option.hasValueSeparator());
    }
}