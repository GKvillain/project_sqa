package org.apache.commons.cli2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.DefaultSwitchBuilder;
import org.apache.commons.cli2.commandline.CommandLineImpl;
import org.apache.commons.cli2.commandline.WriteableCommandLineImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit 4 test class for WriteableCommandLine interface using the real
 * implementation WriteableCommandLineImpl.
 */
public class WriteableCommandLineTest {

    private WriteableCommandLine wc;
    private Option option;
    private Option swOption;

    @Before
    public void setUp() {
        // Create base command line and the writable wrapper
        CommandLine base = new CommandLineImpl();
        wc = new WriteableCommandLineImpl(base);

        // Build an ordinary option and a switch option
        option = new DefaultOptionBuilder().withLongName("testopt").create();
        swOption = new DefaultSwitchBuilder().withLongName("testsw").create();
    }

    // Tests that addOption makes the option visible via hasOption
    @Test
    public void testAddOption_validOption_optionAdded() {
        wc.addOption(option);
        assertTrue(wc.hasOption(option));
    }

    // Tests adding a single value to an option
    @Test
    public void testAddValue_normalValue_valueStored() {
        wc.addOption(option);
        wc.addValue(option, "value1");
        List<?> values = wc.getValues(option);
        assertEquals(1, values.size());
        assertEquals("value1", values.get(0));
    }

    // Tests that multiple values are accumulated
    @Test
    public void testAddValue_multipleValues_valuesAccumulated() {
        wc.addOption(option);
        wc.addValue(option, "first");
        wc.addValue(option, "second");
        List<?> values = wc.getValues(option);
        assertEquals(2, values.size());
        assertEquals("first", values.get(0));
        assertEquals("second", values.get(1));
    }

    // Tests addSwitch for the first time (switch becomes true)
    @Test
    public void testAddSwitch_firstSwitch_switchSet() {
        wc.addOption(swOption);
        wc.addSwitch(swOption, true);
        assertEquals(Boolean.TRUE, wc.getSwitch(swOption));
    }

    // Tests that adding the same switch again throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testAddSwitch_duplicateSwitch_throwsIllegalStateException() {
        wc.addOption(swOption);
        wc.addSwitch(swOption, true);
        wc.addSwitch(swOption, true);
    }

    // Tests that adding a different boolean value to an already added switch
    // also throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testAddSwitch_duplicateSwitchDifferentValue_throwsIllegalStateException() {
        wc.addOption(swOption);
        wc.addSwitch(swOption, true);
        wc.addSwitch(swOption, false);
    }

    // Tests that getUndefaultedValues is empty when no defaults and no values
    @Test
    public void testGetUndefaultedValues_noDefaultsAndNoValues_emptyList() {
        wc.addOption(option);
        List<?> undef = wc.getUndefaultedValues(option);
        assertTrue(undef.isEmpty());
    }

    // Tests that after setting defaults, getUndefaultedValues is empty (unless
    // explicit values are added)
    @Test
    public void testSetDefaultValues_defaultValues_undefaultedValuesEmpty() {
        wc.addOption(option);
        wc.setDefaultValues(option, Arrays.asList("d1", "d2"));
        List<?> undef = wc.getUndefaultedValues(option);
        assertTrue(undef.isEmpty());
    }

    // Tests that custom added values appear in getUndefaultedValues even when
    // defaults are set
    @Test
    public void testSetDefaultValues_customAfterDefault_customReturned() {
        wc.addOption(option);
        wc.setDefaultValues(option, Arrays.asList("d1", "d2"));
        wc.addValue(option, "custom");
        List<?> undef = wc.getUndefaultedValues(option);
        assertEquals(1, undef.size());
        assertEquals("custom", undef.get(0));
    }

    // Tests that setDefaultSwitch defines the initial switch value
    @Test
    public void testSetDefaultSwitch_defaultSwitchSet() {
        wc.addOption(swOption);
        wc.setDefaultSwitch(swOption, true);
        assertEquals(Boolean.TRUE, wc.getSwitch(swOption));
    }

    // Tests that an explicit addSwitch overrides the default switch
    @Test
    public void testSetDefaultSwitch_addSwitchOverridesDefault() {
        wc.addOption(swOption);
        wc.setDefaultSwitch(swOption, true);
        wc.addSwitch(swOption, false);
        assertEquals(Boolean.FALSE, wc.getSwitch(swOption));
    }

    // Tests addProperty without an option stores a global property
    @Test
    public void testAddProperty_defaultProperty_propertyStored() {
        wc.addProperty("mykey", "myvalue");
        assertEquals("myvalue", wc.getProperty("mykey"));
    }

    // Tests that adding a property with the same key replaces the old value
    @Test
    public void testAddProperty_replaceExistingProperty() {
        wc.addProperty("key", "first");
        wc.addProperty("key", "second");
        assertEquals("second", wc.getProperty("key"));
    }

    // Tests that an argument starting with '-' looks like an option
    @Test
    public void testLooksLikeOption_hyphenPrefix_returnsTrue() {
        assertTrue(wc.looksLikeOption("-verbose"));
    }

    // Tests that an argument without '-' does not look like an option
    @Test
    public void testLooksLikeOption_noHyphen_returnsFalse() {
        assertFalse(wc.looksLikeOption("verbose"));
    }

    // Tests that an empty string does not look like an option
    @Test
    public void testLooksLikeOption_emptyString_returnsFalse() {
        assertFalse(wc.looksLikeOption(""));
    }

    // Tests that null does not look like an option
    @Test
    public void testLooksLikeOption_nullArgument_returnsFalse() {
        assertFalse(wc.looksLikeOption(null));
    }

    // ==================== New tests covering missing areas ====================

    @Test
    public void testHasOption_withStringTrigger() {
        wc.addOption(option);
        assertTrue(wc.hasOption("testopt"));
    }

    @Test
    public void testHasOption_withStringTrigger_unknown() {
        assertFalse(wc.hasOption("unknown"));
    }

    @Test
    public void testGetOption_found() {
        wc.addOption(option);
        assertEquals(option, wc.getOption("testopt"));
    }

    @Test
    public void testGetOption_notFound() {
        assertNull(wc.getOption("unknown"));
    }

    @Test
    public void testGetOptions_containsAddedOptions() {
        wc.addOption(option);
        wc.addOption(swOption);
        List<?> options = wc.getOptions();
        assertTrue(options.contains(option));
        assertTrue(options.contains(swOption));
    }

    @Test
    public void testGetProperties_emptyInitially() {
        assertTrue(wc.getProperties().isEmpty());
    }

    @Test
    public void testGetProperties_afterAddProperty() {
        wc.addProperty("key", "value");
        assertEquals("value", wc.getProperties().get("key"));
    }
}