package org.apache.commons.cli;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class OptionBuilderTest {

    @Before
    public void setUp() {
        // Reset the OptionBuilder state by creating a dummy option
        OptionBuilder.withLongOpt("reset").create();
    }

    // Tests create() without longopt throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreate_withoutLongopt_throwsIllegalArgumentException() {
        OptionBuilder.create();
    }

    // Tests create() with longopt returns option with correct long option
    @Test
    public void testCreate_withLongopt_createsOptionWithLongopt() {
        Option option = OptionBuilder.withLongOpt("foo").create();
        assertEquals("foo", option.getLongOpt());
    }

    // Tests create(char) returns option with correct opt
    @Test
    public void testCreate_withOptChar_createsOptionWithOpt() {
        Option option = OptionBuilder.create('a');
        assertEquals("a", option.getOpt());
    }

    // Tests hasArg() followed by create sets one argument
    @Test
    public void testCreate_hasArg_createsOptionWithArg() {
        Option option = OptionBuilder.hasArg().create('a');
        assertTrue(option.hasArg());
        assertEquals(1, option.getArgs());
    }

    // Tests hasArg(true) sets number of arguments to 1
    @Test
    public void testCreate_hasArgTrue_createsOptionWithArg() {
        Option option = OptionBuilder.hasArg(true).create('a');
        assertTrue(option.hasArg());
        assertEquals(1, option.getArgs());
    }

    // Tests hasArg(false) resets number of arguments to UNINITIALIZED
    @Test
    public void testHasArgFalse_createsOptionWithoutArg() {
        Option option = OptionBuilder.hasArg(false).create('a');
        assertFalse(option.hasArg());
        assertEquals(Option.UNINITIALIZED, option.getArgs());
    }

    // Tests hasOptionalArg() sets optional flag and argument count to 1
    @Test
    public void testCreate_hasOptionalArg_createsOptionWithOptionalArg() {
        Option option = OptionBuilder.hasOptionalArg().create('a');
        assertTrue(option.hasOptionalArg());
        assertEquals(1, option.getArgs());
    }

    // Tests hasOptionalArgs() sets optional flag and unlimited arguments
    @Test
    public void testCreate_hasOptionalArgs_createsOptionWithUnlimitedArgs() {
        Option option = OptionBuilder.hasOptionalArgs().create('a');
        assertTrue(option.hasOptionalArg());
        assertEquals(Option.UNLIMITED_VALUES, option.getArgs());
    }

    // Tests hasOptionalArgs(int) sets optional flag and specified number
    @Test
    public void testCreate_hasOptionalArgsInt_createsOptionWithSpecificOptionalArgs() {
        Option option = OptionBuilder.hasOptionalArgs(2).create('a');
        assertTrue(option.hasOptionalArg());
        assertEquals(2, option.getArgs());
    }

    // Tests hasArgs() sets unlimited arguments
    @Test
    public void testCreate_hasArgs_createsOptionWithUnlimitedArgs() {
        Option option = OptionBuilder.hasArgs().create('a');
        assertEquals(Option.UNLIMITED_VALUES, option.getArgs());
    }

    // Tests hasArgs(int) sets specified number of arguments
    @Test
    public void testCreate_hasArgsInt_createsOptionWithSpecificArgs() {
        Option option = OptionBuilder.hasArgs(3).create('a');
        assertEquals(3, option.getArgs());
    }

    // Tests isRequired() sets required flag
    @Test
    public void testCreate_isRequired_createsRequiredOption() {
        Option option = OptionBuilder.isRequired().create('a');
        assertTrue(option.isRequired());
    }

    // Tests isRequired(true) sets required flag
    @Test
    public void testCreate_isRequiredTrue_createsRequiredOption() {
        Option option = OptionBuilder.isRequired(true).create('a');
        assertTrue(option.isRequired());
    }

    // Tests isRequired(false) sets required to false
    @Test
    public void testCreate_isRequiredFalse_createsNotRequiredOption() {
        Option option = OptionBuilder.isRequired(false).create('a');
        assertFalse(option.isRequired());
    }

    // Tests withType() sets the type
    @Test
    public void testCreate_withType_createsOptionWithType() {
        Option option = OptionBuilder.withType(Integer.class).create('a');
        assertEquals(Integer.class, option.getType());
    }

    // Tests withValueSeparator(char) sets the value separator
    @Test
    public void testCreate_withValueSeparator_createsOptionWithSeparator() {
        Option option = OptionBuilder.withValueSeparator(':').create('a');
        assertEquals(':', option.getValueSeparator());
    }

    // Tests withValueSeparator() sets separator to '='
    @Test
    public void testCreate_withValueSeparatorDefault_createsOptionWithEqualsSep() {
        Option option = OptionBuilder.withValueSeparator().create('a');
        assertEquals('=', option.getValueSeparator());
    }

    // Tests withArgName() sets the argument name
    @Test
    public void testCreate_withArgName_createsOptionWithArgName() {
        Option option = OptionBuilder.withArgName("myarg").create('a');
        assertEquals("myarg", option.getArgName());
    }

    // Tests withDescription() sets the description
    @Test
    public void testCreate_withDescription_createsOptionWithDescription() {
        Option option = OptionBuilder.withDescription("desc").create('a');
        assertEquals("desc", option.getDescription());
    }

    // Tests chained calls set multiple properties correctly
    @Test
    public void testCreate_chainedCalls_createsOptionWithMultipleProperties() {
        Option option = OptionBuilder.withLongOpt("long")
                                     .hasArg()
                                     .isRequired()
                                     .withDescription("desc")
                                     .create('a');
        assertEquals("long", option.getLongOpt());
        assertTrue(option.hasArg());
        assertTrue(option.isRequired());
        assertEquals("desc", option.getDescription());
    }

    // Tests that after creating an option, the state is reset and create() without longopt throws
    @Test
    public void testCreate_stateResetAfterCreate_causesNextCreateWithoutLongoptToThrow() {
        OptionBuilder.withLongOpt("first").create();
        try {
            OptionBuilder.create();
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // Tests create(String) with null opt throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreate_nullOpt_throwsIllegalArgumentException() {
        OptionBuilder.create((String) null);
    }
}