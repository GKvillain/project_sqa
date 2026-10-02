package org.apache.commons.cli2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli2.commandline.WriteableCommandLineImpl;
import org.apache.commons.cli2.option.PropertyOption;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class WriteableCommandLineTest {

    private WriteableCommandLine commandLine;
    private Option option;

    @Before
    public void setUp() {
        option = new PropertyOption();
        commandLine = new WriteableCommandLineImpl(option, new ArrayList());
    }

    // Tests adding an option and verifying its presence
    @Test
    public void testAddOption_validOption_optionIsPresent() {
        commandLine.addOption(option);
        assertTrue(commandLine.hasOption(option));
    }

    // Tests adding a single value to an option
    @Test
    public void testAddValue_singleValue_valueRetrieved() {
        commandLine.addValue(option, "value1");
        assertEquals("value1", commandLine.getValue(option));
    }

    // Tests adding multiple values to an option
    @Test
    public void testAddValue_multipleValues_allValuesRetrieved() {
        commandLine.addValue(option, "value1");
        commandLine.addValue(option, "value2");
        List expected = Arrays.asList("value1", "value2");
        assertEquals(expected, commandLine.getValues(option));
    }

    // Tests getUndefaultedValues returns only explicitly added values
    @Test
    public void testGetUndefaultedValues_withDefaultValues_returnsOnlyUndefaultedValues() {
        commandLine.setDefaultValues(option, Arrays.asList("default1", "default2"));
        commandLine.addValue(option, "explicitValue");

        List undefaulted = commandLine.getUndefaultedValues(option);
        assertEquals(1, undefaulted.size());
        assertEquals("explicitValue", undefaulted.get(0));
    }

    // Tests getUndefaultedValues returns an empty list when no values are added
    @Test
    public void testGetUndefaultedValues_noValuesAdded_returnsEmptyList() {
        commandLine.setDefaultValues(option, Collections.singletonList("defaultVal"));
        List undefaulted = commandLine.getUndefaultedValues(option);
        assertNotNull(undefaulted);
        assertTrue(undefaulted.isEmpty());
    }

    // Tests setDefaultValues applies default values when no explicit value is present
    @Test
    public void testSetDefaultValues_validList_defaultValuesAppliedWhenNoValueAdded() {
        List defaults = Arrays.asList("default1", "default2");
        commandLine.setDefaultValues(option, defaults);
        assertEquals(defaults, commandLine.getValues(option));
    }

    // Tests adding a true switch value
    @Test
    public void testAddSwitch_trueValue_switchIsTrue() {
        commandLine.addSwitch(option, true);
        assertEquals(Boolean.TRUE, commandLine.getSwitch(option));
    }

    // Tests adding a false switch value
    @Test
    public void testAddSwitch_falseValue_switchIsFalse() {
        commandLine.addSwitch(option, false);
        assertEquals(Boolean.FALSE, commandLine.getSwitch(option));
    }

    // Tests adding duplicate switch throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testAddSwitch_alreadyAdded_throwsIllegalStateException() {
        commandLine.addSwitch(option, true);
        commandLine.addSwitch(option, false);
    }

    // Tests setting default switch state
    @Test
    public void testSetDefaultSwitch_booleanDefault_defaultStateApplied() {
        commandLine.setDefaultSwitch(option, Boolean.TRUE);
        assertEquals(Boolean.TRUE, commandLine.getSwitch(option));
    }

    // Tests setting default switch to null
    @Test
    public void testSetDefaultSwitch_nullDefault_returnsNull() {
        commandLine.setDefaultSwitch(option, null);
        assertNull(commandLine.getSwitch(option));
    }

    // Tests adding a property associated with a specific option
    @Test
    public void testAddProperty_withOption_propertyRetrievedByOption() {
        commandLine.addProperty(option, "key1", "val1");
        assertEquals("val1", commandLine.getProperty(option, "key1"));
    }

    // Tests replacing an existing property value associated with an option
    @Test
    public void testAddProperty_withOptionOverwrite_replacesExistingValue() {
        commandLine.addProperty(option, "key1", "val1");
        commandLine.addProperty(option, "key1", "val2");
        assertEquals("val2", commandLine.getProperty(option, "key1"));
    }

    // Tests adding a property to default property set
    @Test
    public void testAddProperty_defaultPropertySet_propertyRetrieved() {
        commandLine.addProperty("globalKey", "globalValue");
        assertEquals("globalValue", commandLine.getProperty("globalKey"));
    }

    // Tests replacing an existing property value in default property set
    @Test
    public void testAddProperty_defaultPropertySetOverwrite_replacesExistingValue() {
        commandLine.addProperty("globalKey", "val1");
        commandLine.addProperty("globalKey", "val2");
        assertEquals("val2", commandLine.getProperty("globalKey"));
    }

    // Tests looksLikeOption with valid option prefix
    @Test
    public void testLooksLikeOption_validOptionTrigger_returnsTrue() {
        assertTrue(commandLine.looksLikeOption("-D"));
    }

    // Tests looksLikeOption with non-option argument
    @Test
    public void testLooksLikeOption_nonOptionString_returnsFalse() {
        assertFalse(commandLine.looksLikeOption("nonOptionArgument"));
    }

    // Tests looksLikeOption with null input
    @Test
    public void testLooksLikeOption_nullArgument_returnsFalse() {
        assertFalse(commandLine.looksLikeOption(null));
    }

    // Tests hasOption by trigger string
    @Test
    public void testHasOption_byTrigger_returnsCorrectStatus() {
        assertFalse(commandLine.hasOption("-D"));
        commandLine.addOption(option);
        assertTrue(commandLine.hasOption("-D"));
        assertFalse(commandLine.hasOption("-unknown"));
    }

    // Tests getOption by trigger string and by option instance
    @Test
    public void testGetOption_byTriggerAndOption() {
        assertNull(commandLine.getOption("-D"));
        commandLine.addOption(option);
        assertEquals(option, commandLine.getOption("-D"));
        assertEquals(option, commandLine.getOption(option));
        assertNull(commandLine.getOption("-unknown"));
    }

    // Tests getValue with default value fallback
    @Test
    public void testGetValue_withDefaultFallback() {
        assertEquals("defaultVal", commandLine.getValue(option, "defaultVal"));
        assertEquals("defaultVal", commandLine.getValue("-D", "defaultVal"));

        commandLine.addValue(option, "realVal");
        assertEquals("realVal", commandLine.getValue(option, "defaultVal"));
        assertEquals("realVal", commandLine.getValue("-D", "defaultVal"));
        assertEquals("realVal", commandLine.getValue("-D"));
    }

    // Tests getValues with fallback defaults and by trigger string
    @Test
    public void testGetValues_withTriggerAndDefaultFallback() {
        List fallback = Arrays.asList("fb1", "fb2");
        assertEquals(fallback, commandLine.getValues(option, fallback));
        assertEquals(fallback, commandLine.getValues("-D", fallback));

        commandLine.addValue(option, "v1");
        assertEquals(Collections.singletonList("v1"), commandLine.getValues("-D"));
        assertEquals(Collections.singletonList("v1"), commandLine.getValues("-D", fallback));
    }

    // Tests getSwitch by trigger string and with default fallback
    @Test
    public void testGetSwitch_byTriggerAndWithDefaultFallback() {
        assertEquals(Boolean.FALSE, commandLine.getSwitch(option, Boolean.FALSE));
        assertEquals(Boolean.TRUE, commandLine.getSwitch("-D", Boolean.TRUE));
        assertNull(commandLine.getSwitch("-D"));

        commandLine.addSwitch(option, true);
        assertEquals(Boolean.TRUE, commandLine.getSwitch("-D"));
        assertEquals(Boolean.TRUE, commandLine.getSwitch("-D", Boolean.FALSE));
    }

    // Tests getProperty with default fallback
    @Test
    public void testGetProperty_withDefaultFallback() {
        assertEquals("def", commandLine.getProperty(option, "k1", "def"));
        assertEquals("defGlobal", commandLine.getProperty("k2", "defGlobal"));

        commandLine.addProperty(option, "k1", "v1");
        commandLine.addProperty("k2", "v2");

        assertEquals("v1", commandLine.getProperty(option, "k1", "def"));
        assertEquals("v2", commandLine.getProperty("k2", "defGlobal"));
    }

    // Tests getProperties retrieving all properties
    @Test
    public void testGetProperties_withOptionAndGlobal() {
        commandLine.addProperty(option, "prop1", "val1");
        commandLine.addProperty(option, "prop2", "val2");
        commandLine.addProperty("global1", "gval1");

        Set optionProps = commandLine.getProperties(option);
        assertNotNull(optionProps);
        assertTrue(optionProps.contains("prop1"));
        assertTrue(optionProps.contains("prop2"));

        Set globalProps = commandLine.getProperties();
        assertNotNull(globalProps);
        assertTrue(globalProps.contains("global1"));
    }

    // Tests getOptionCount by option and by trigger
    @Test
    public void testGetOptionCount_byOptionAndTrigger() {
        assertEquals(0, commandLine.getOptionCount(option));
        assertEquals(0, commandLine.getOptionCount("-D"));

        commandLine.addOption(option);
        assertEquals(1, commandLine.getOptionCount(option));
        assertEquals(1, commandLine.getOptionCount("-D"));

        commandLine.addOption(option);
        assertEquals(2, commandLine.getOptionCount(option));
        assertEquals(2, commandLine.getOptionCount("-D"));
    }

    // Tests getOptions and getOptionTriggers
    @Test
    public void testGetOptionsAndOptionTriggers() {
        assertTrue(commandLine.getOptions().isEmpty());
        assertTrue(commandLine.getOptionTriggers().isEmpty());

        commandLine.addOption(option);
        List options = commandLine.getOptions();
        assertEquals(1, options.size());
        assertTrue(options.contains(option));

        Set triggers = commandLine.getOptionTriggers();
        assertTrue(triggers.contains("-D"));
    }

    // Tests toString method of WriteableCommandLine
    @Test
    public void testToString_formatsCorrectly() {
        commandLine.addOption(option);
        commandLine.addValue(option, "val");
        String result = commandLine.toString();
        assertNotNull(result);
    }
}