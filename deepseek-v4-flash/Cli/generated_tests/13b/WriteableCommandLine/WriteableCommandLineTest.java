package org.apache.commons.cli2;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionBuilder;
import org.apache.commons.cli2.Parser;
import org.apache.commons.cli2.parser.GnuParser;
import java.util.List;
import java.util.Set;

public class WriteableCommandLineTest {
    private Option opt1;
    private Option opt2;
    private Parser parser;
    private WriteableCommandLine cmdLine;

    @Before
    public void setUp() throws Exception {
        opt1 = OptionBuilder.withLongOpt("verbose").create();
        opt2 = OptionBuilder.withLongOpt("debug").create();
        parser = new GnuParser();
        cmdLine = (WriteableCommandLine) parser.parse(new Option[]{opt1, opt2}, new String[]{});
    }

    // Tests addOption adds an option and can be used for values
    @Test
    public void testAddOption_normalOption_addsOptionSuccessfully() {
        Option opt3 = OptionBuilder.withLongOpt("custom").create();
        cmdLine.addOption(opt3);
        cmdLine.addValue(opt3, "customValue");
        List values = cmdLine.getValues(opt3);
        assertTrue("Values should contain the added value", values.contains("customValue"));
    }

    // Tests addValue adds a value to an option
    @Test
    public void testAddValue_normalOptionAndValue_addsValue() {
        cmdLine.addValue(opt1, "value1");
        List values = cmdLine.getValues(opt1);
        assertTrue("Values should contain 'value1'", values.contains("value1"));
    }

    // Tests adding multiple values to the same option
    @Test
    public void testAddValue_multipleValuesForOption_addsAll() {
        cmdLine.addValue(opt1, "v1");
        cmdLine.addValue(opt1, "v2");
        List values = cmdLine.getValues(opt1);
        assertEquals("Should have two values", 2, values.size());
        assertTrue("Should contain v1", values.contains("v1"));
        assertTrue("Should contain v2", values.contains("v2"));
    }

    // Tests addSwitch with true value
    @Test
    public void testAddSwitch_normalSwitch_addsSwitch() {
        cmdLine.addSwitch(opt1, true);
        assertTrue("Switch should be true", cmdLine.getSwitch(opt1));
    }

    // Tests addSwitch with false value
    @Test
    public void testAddSwitch_booleanValueFalse_returnsFalse() {
        cmdLine.addSwitch(opt1, false);
        assertFalse("Switch should be false", cmdLine.getSwitch(opt1));
    }

    // Tests that duplicate addSwitch throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testAddSwitch_duplicateSwitch_throwsIllegalStateException() {
        cmdLine.addSwitch(opt1, true);
        cmdLine.addSwitch(opt1, true);
    }

    // Tests setDefaultSwitch sets the default switch value
    @Test
    public void testSetDefaultSwitch_switchNotSet_setsDefault() {
        cmdLine.setDefaultSwitch(opt1, true);
        assertTrue("Default switch should be true", cmdLine.getSwitch(opt1));
    }

    // Tests addProperty adds a property value
    @Test
    public void testAddProperty_normalProperty_addsProperty() {
        cmdLine.addProperty("key1", "value1");
        assertEquals("Property value mismatch", "value1", cmdLine.getProperty("key1"));
    }

    // Tests addProperty replaces existing property value
    @Test
    public void testAddProperty_replaceProperty_replacesValue() {
        cmdLine.addProperty("key", "old");
        cmdLine.addProperty("key", "new");
        assertEquals("Property should be replaced", "new", cmdLine.getProperty("key"));
    }

    // Tests looksLikeOption with short dash prefix
    @Test
    public void testLooksLikeOption_argumentStartingWithDash_returnsTrue() {
        assertTrue("Should recognize -v as option", cmdLine.looksLikeOption("-v"));
    }

    // Tests looksLikeOption with double dash prefix
    @Test
    public void testLooksLikeOption_argumentStartingWithDoubleDash_returnsTrue() {
        assertTrue("Should recognize --verbose as option", cmdLine.looksLikeOption("--verbose"));
    }

    // Tests looksLikeOption with normal string argument
    @Test
    public void testLooksLikeOption_normalArgument_returnsFalse() {
        assertFalse("Should not recognize plain string as option", cmdLine.looksLikeOption("normal"));
    }

    // Tests looksLikeOption with empty string
    @Test
    public void testLooksLikeOption_emptyString_returnsFalse() {
        assertFalse("Empty string should not be an option", cmdLine.looksLikeOption(""));
    }

    // ========== NEW TESTS FOR UNCOVERED AREAS ==========

    // Tests hasOption after adding an option
    @Test
    public void testHasOption_optionAdded_returnsTrue() {
        Option custom = OptionBuilder.withLongOpt("custom").create();
        cmdLine.addOption(custom);
        assertTrue("Should have the added option", cmdLine.hasOption(custom));
    }

    // Tests hasOption for an option not added
    @Test
    public void testHasOption_optionNotAdded_returnsFalse() {
        Option extra = OptionBuilder.withLongOpt("extra").create();
        assertFalse("Should not have the option", cmdLine.hasOption(extra));
    }

    // Tests getOptions returns all added options
    @Test
    public void testGetOptions_afterAddingOptions_containsThem() {
        Option custom = OptionBuilder.withLongOpt("custom").create();
        cmdLine.addOption(custom);
        Set options = cmdLine.getOptions();
        assertTrue("Options set should contain opt1", options.contains(opt1));
        assertTrue("Options set should contain opt2", options.contains(opt2));
        assertTrue("Options set should contain custom", options.contains(custom));
        assertEquals("Should have exactly three options", 3, options.size());
    }

    // Tests getValues with no values added returns empty (or null? assume empty)
    @Test
    public void testGetValues_noValues_returnsEmptyList() {
        List values = cmdLine.getValues(opt1);
        assertNotNull("Should not be null", values);
        assertTrue("Values should be empty", values.isEmpty());
    }

    // Tests getSwitch default value (without setting) returns false
    @Test
    public void testGetSwitch_defaultValue_returnsFalse() {
        assertFalse("Default switch should be false", cmdLine.getSwitch(opt1));
    }

    // Tests looksLikeOption with null argument returns false
    @Test
    public void testLooksLikeOption_nullArgument_returnsFalse() {
        assertFalse("Null should not be an option", cmdLine.looksLikeOption(null));
    }

    // Tests addProperty with null key (expected NullPointerException)
    @Test(expected = NullPointerException.class)
    public void testAddProperty_nullKey_throwsNullPointerException() {
        cmdLine.addProperty(null, "value");
    }

    // Tests addProperty with null value (allowed by some implementations, but assume throws? or just stores)
    // We'll test that null value is stored as null
    @Test
    public void testAddProperty_nullValue_storesNull() {
        cmdLine.addProperty("nullKey", null);
        assertNull("Property value should be null", cmdLine.getProperty("nullKey"));
    }

    // Tests addSwitch with null option (expected NullPointerException)
    @Test(expected = NullPointerException.class)
    public void testAddSwitch_nullOption_throwsNullPointerException() {
        cmdLine.addSwitch(null, true);
    }

    // Tests addOption with null (expected NullPointerException)
    @Test(expected = NullPointerException.class)
    public void testAddOption_nullOption_throwsNullPointerException() {
        cmdLine.addOption(null);
    }

    // Tests addValue with null option (expected NullPointerException)
    @Test(expected = NullPointerException.class)
    public void testAddValue_nullOption_throwsNullPointerException() {
        cmdLine.addValue(null, "value");
    }

    // Tests setDefaultSwitch with null option (expected NullPointerException)
    @Test(expected = NullPointerException.class)
    public void testSetDefaultSwitch_nullOption_throwsNullPointerException() {
        cmdLine.setDefaultSwitch(null, false);
    }
}