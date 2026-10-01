package org.apache.commons.cli;

import static org.junit.Assert.*;

import org.junit.Test;

public class OptionBuilderTest {

    // Test create with valid short option string
    @Test
    public void testCreate_validOpt_returnsOption() {
        Option option = OptionBuilder.create("a");
        assertEquals("a", option.getOpt());
        assertFalse(option.hasLongOpt());
        assertFalse(option.isRequired());
        assertFalse(option.hasOptionalArg());
        assertEquals(Option.UNINITIALIZED, option.getArgs());
    }

    // Test create with char option
    @Test
    public void testCreate_validChar_returnsOption() {
        Option option = OptionBuilder.create('b');
        assertEquals("b", option.getOpt());
        assertFalse(option.hasLongOpt());
    }

    // Test create with long option set
    @Test
    public void testCreate_withLongOpt_returnsOption() {
        Option option = OptionBuilder.withLongOpt("verbose").create("v");
        assertEquals("v", option.getOpt());
        assertEquals("verbose", option.getLongOpt());
    }

    // Test create with description
    @Test
    public void testCreate_withDescription_returnsOption() {
        Option option = OptionBuilder.withDescription("help").create("h");
        assertEquals("h", option.getOpt());
        assertEquals("help", option.getDescription());
    }

    // Test create with argName
    @Test
    public void testCreate_withArgName_returnsOption() {
        Option option = OptionBuilder.withArgName("file").create("f");
        assertEquals("f", option.getOpt());
        assertEquals("file", option.getArgName());
    }

    // Test create with required set to true
    @Test
    public void testCreate_isRequired_returnsRequiredOption() {
        Option option = OptionBuilder.isRequired().create("r");
        assertTrue(option.isRequired());
    }

    // Test create with hasArg set
    @Test
    public void testCreate_hasArg_returnsOptionWithOneArg() {
        Option option = OptionBuilder.hasArg().create("a");
        assertEquals(1, option.getArgs());
    }

    // Test create with hasArg boolean true
    @Test
    public void testCreate_hasArgTrue_returnsOptionWithOneArg() {
        Option option = OptionBuilder.hasArg(true).create("a");
        assertEquals(1, option.getArgs());
    }

    // Test create with hasArg boolean false
    @Test
    public void testCreate_hasArgFalse_returnsOptionWithUninitializedArgs() {
        Option option = OptionBuilder.hasArg(false).create("a");
        assertEquals(Option.UNINITIALIZED, option.getArgs());
    }

    // Test create with hasArgs unlimited
    @Test
    public void testCreate_hasArgs_returnsOptionWithUnlimitedArgs() {
        Option option = OptionBuilder.hasArgs().create("a");
        assertEquals(Option.UNLIMITED_VALUES, option.getArgs());
    }

    // Test create with hasArgs specific number
    @Test
    public void testCreate_hasArgsNum_returnsOptionWithNumArgs() {
        Option option = OptionBuilder.hasArgs(3).create("a");
        assertEquals(3, option.getArgs());
    }

    // Test create with hasOptionalArg
    @Test
    public void testCreate_hasOptionalArg_returnsOptionalArgOption() {
        Option option = OptionBuilder.hasOptionalArg().create("a");
        assertTrue(option.hasOptionalArg());
        assertEquals(1, option.getArgs());
    }

    // Test create with hasOptionalArgs unlimited
    @Test
    public void testCreate_hasOptionalArgs_returnsOptionalArgsOption() {
        Option option = OptionBuilder.hasOptionalArgs().create("a");
        assertTrue(option.hasOptionalArg());
        assertEquals(Option.UNLIMITED_VALUES, option.getArgs());
    }

    // Test create with hasOptionalArgs specific number
    @Test
    public void testCreate_hasOptionalArgsNum_returnsOptionalArgsOption() {
        Option option = OptionBuilder.hasOptionalArgs(5).create("a");
        assertTrue(option.hasOptionalArg());
        assertEquals(5, option.getArgs());
    }

    // Test create with withValueSeparator char
    @Test
    public void testCreate_withValueSeparatorChar_returnsOptionWithSeparator() {
        Option option = OptionBuilder.withValueSeparator(':').create("d");
        assertEquals(':', option.getValueSeparator());
    }

    // Test create with withValueSeparator default
    @Test
    public void testCreate_withValueSeparatorDefault_returnsOptionWithEqualsSeparator() {
        Option option = OptionBuilder.withValueSeparator().create("d");
        assertEquals('=', option.getValueSeparator());
    }

    // Test create with type set
    @Test
    public void testCreate_withType_returnsOptionWithType() {
        Option option = OptionBuilder.withType(Integer.class).create("i");
        assertEquals(Integer.class, option.getType());
    }

    // Test create() without long option throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testCreate_noLongopt_throwsException() {
        // Ensure no long opt is set
        OptionBuilder.withDescription("test");
        OptionBuilder.create();
    }

    // Test create() with long option
    @Test
    public void testCreate_withLongOptOnly_returnsOption() {
        Option option = OptionBuilder.withLongOpt("longonly").create();
        assertNull(option.getOpt());
        assertEquals("longonly", option.getLongOpt());
    }
}