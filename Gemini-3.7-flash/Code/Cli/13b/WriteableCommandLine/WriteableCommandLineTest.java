package org.apache.commons.cli2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.builder.PropertyOptionBuilder;
import org.apache.commons.cli2.commandline.WriteableCommandLineImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class WriteableCommandLineTest {

    private Option helpOption;
    private Option verboseOption;
    private Option propertyOption;
    private Option rootOption;
    private WriteableCommandLine commandLine;

    @Before
    public void setUp() {
        final DefaultOptionBuilder optionBuilder = new DefaultOptionBuilder();
        final PropertyOptionBuilder propertyBuilder = new PropertyOptionBuilder();
        final GroupBuilder groupBuilder = new GroupBuilder();

        helpOption = optionBuilder.withShortName("h").withLongName("help").create();
        verboseOption = optionBuilder.withShortName("v").withLongName("verbose").create();
        propertyOption = propertyBuilder.create();
        rootOption = groupBuilder.withOption(helpOption).withOption(verboseOption).withOption(propertyOption).create();

        commandLine = new WriteableCommandLineImpl(rootOption, new ArrayList());
    }

    // Tests adding an option and verifying hasOption returns true
    @Test
    public void testAddOption_validOption_hasOptionReturnsTrue() {
        commandLine.addOption(helpOption);
        assertTrue(commandLine.hasOption(helpOption));
        assertTrue(commandLine.hasOption("-h"));
        assertTrue(commandLine.hasOption("--help"));
    }

    // Tests unadded option returns false
    @Test
    public void testHasOption_unaddedOption_returnsFalse() {
        assertFalse(commandLine.hasOption(helpOption));
        assertFalse(commandLine.hasOption("-h"));
    }

    // Tests adding a single value to an option
    @Test
    public void testAddValue_singleValue_getValueReturnsValue() {
        commandLine.addValue(helpOption, "value1");
        assertEquals("value1", commandLine.getValue(helpOption));
        assertEquals(Collections.singletonList("value1"), commandLine.getValues(helpOption));
    }

    // Tests adding multiple values to an option
    @Test
    public void testAddValue_multipleValues_getValuesReturnsAllValues() {
        commandLine.addValue(helpOption, "val1");
        commandLine.addValue(helpOption, "val2");
        final List expected = Arrays.asList(new Object[]{"val1", "val2"});
        assertEquals(expected, commandLine.getValues(helpOption));
    }

    // Tests default values when no explicit value is added
    @Test
    public void testSetDefaultValues_noValuesAdded_returnsDefaultValues() {
        final List defaults = Arrays.asList(new Object[]{"defaultVal1", "defaultVal2"});
        commandLine.setDefaultValues(helpOption, defaults);
        assertEquals(defaults, commandLine.getValues(helpOption));
        assertEquals("defaultVal1", commandLine.getValue(helpOption));
    }

    // Tests explicit value overrides default values
    @Test
    public void testSetDefaultValues_explicitValueAdded_returnsExplicitValue() {
        final List defaults = Collections.singletonList("defaultVal");
        commandLine.setDefaultValues(helpOption, defaults);
        commandLine.addValue(helpOption, "explicitVal");
        assertEquals(Collections.singletonList("explicitVal"), commandLine.getValues(helpOption));
        assertEquals("explicitVal", commandLine.getValue(helpOption));
    }

    // Tests adding a boolean switch as true
    @Test
    public void testAddSwitch_trueValue_getSwitchReturnsTrue() {
        commandLine.addSwitch(helpOption, true);
        assertEquals(Boolean.TRUE, commandLine.getSwitch(helpOption));
        assertTrue(commandLine.hasOption(helpOption));
    }

    // Tests adding a boolean switch as false
    @Test
    public void testAddSwitch_falseValue_getSwitchReturnsFalse() {
        commandLine.addSwitch(helpOption, false);
        assertEquals(Boolean.FALSE, commandLine.getSwitch(helpOption));
        assertTrue(commandLine.hasOption(helpOption));
    }

    // Tests duplicate switch addition throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testAddSwitch_duplicateSwitch_throwsIllegalStateException() {
        commandLine.addSwitch(helpOption, true);
        commandLine.addSwitch(helpOption, false);
    }

    // Tests default switch when no switch is explicitly added
    @Test
    public void testSetDefaultSwitch_noSwitchAdded_returnsDefaultSwitch() {
        commandLine.setDefaultSwitch(helpOption, Boolean.TRUE);
        assertEquals(Boolean.TRUE, commandLine.getSwitch(helpOption));
    }

    // Tests explicit switch overrides default switch
    @Test
    public void testSetDefaultSwitch_explicitSwitchAdded_returnsExplicitSwitch() {
        commandLine.setDefaultSwitch(helpOption, Boolean.FALSE);
        commandLine.addSwitch(helpOption, true);
        assertEquals(Boolean.TRUE, commandLine.getSwitch(helpOption));
    }

    // Tests adding a property and retrieving its value
    @Test
    public void testAddProperty_singleProperty_getPropertyReturnsValue() {
        commandLine.addProperty("key1", "value1");
        assertEquals("value1", commandLine.getProperty("key1"));
    }

    // Tests adding a property with same key overwrites existing value
    @Test
    public void testAddProperty_duplicateKey_overwritesExistingValue() {
        commandLine.addProperty("key1", "initial");
        commandLine.addProperty("key1", "updated");
        assertEquals("updated", commandLine.getProperty("key1"));
    }

    // Tests retrieving non-existent property returns null
    @Test
    public void testGetProperty_nonExistentProperty_returnsNull() {
        assertNull(commandLine.getProperty("nonExistent"));
    }

    // Tests looksLikeOption with valid option prefix triggers
    @Test
    public void testLooksLikeOption_validPrefix_returnsTrue() {
        assertTrue(commandLine.looksLikeOption("-h"));
        assertTrue(commandLine.looksLikeOption("--help"));
    }

    // Tests looksLikeOption with non-option string
    @Test
    public void testLooksLikeOption_nonPrefixString_returnsFalse() {
        assertFalse(commandLine.looksLikeOption("argumentValue"));
    }

    // Tests looksLikeOption with empty string
    @Test
    public void testLooksLikeOption_emptyString_returnsFalse() {
        assertFalse(commandLine.looksLikeOption(""));
    }

    // Tests looksLikeOption with null input
    @Test
    public void testLooksLikeOption_nullInput_returnsFalse() {
        assertFalse(commandLine.looksLikeOption(null));
    }

    @Test
    public void testGetOption_byTrigger_returnsOption() {
        assertEquals(helpOption, commandLine.getOption("-h"));
        assertEquals(helpOption, commandLine.getOption("--help"));
        assertEquals(verboseOption, commandLine.getOption("-v"));
        assertNull(commandLine.getOption("-unknown"));
    }

    @Test
    public void testGetOptions_returnsAllAddedOptions() {
        commandLine.addOption(helpOption);
        commandLine.addOption(verboseOption);
        final List options = commandLine.getOptions();
        assertTrue(options.contains(helpOption));
        assertTrue(options.contains(verboseOption));
        assertEquals(2, options.size());
    }

    @Test
    public void testGetOptionTriggers_returnsTriggersForAddedOptions() {
        commandLine.addOption(helpOption);
        final Set triggers = commandLine.getOptionTriggers();
        assertTrue(triggers.contains("-h") || triggers.contains("--help"));
    }

    @Test
    public void testGetValue_byTrigger_returnsValue() {
        commandLine.addValue(helpOption, "helpVal");
        assertEquals("helpVal", commandLine.getValue("-h"));
        assertEquals("helpVal", commandLine.getValue("--help"));
        assertNull(commandLine.getValue("-v"));
    }

    @Test
    public void testGetValue_withDefaultObject_returnsValueOrDefault() {
        assertEquals("default", commandLine.getValue(helpOption, "default"));
        assertEquals("default", commandLine.getValue("-h", "default"));

        commandLine.addValue(helpOption, "actual");
        assertEquals("actual", commandLine.getValue(helpOption, "default"));
        assertEquals("actual", commandLine.getValue("-h", "default"));
    }

    @Test
    public void testGetValues_byTrigger_returnsValues() {
        commandLine.addValue(helpOption, "val1");
        commandLine.addValue(helpOption, "val2");
        final List expected = Arrays.asList(new Object[]{"val1", "val2"});
        assertEquals(expected, commandLine.getValues("-h"));
        assertEquals(expected, commandLine.getValues("--help"));
        assertTrue(commandLine.getValues("-v").isEmpty());
    }

    @Test
    public void testGetValues_withDefaultList_returnsValuesOrDefault() {
        final List defaultList = Arrays.asList(new Object[]{"d1", "d2"});
        assertEquals(defaultList, commandLine.getValues(helpOption, defaultList));
        assertEquals(defaultList, commandLine.getValues("-h", defaultList));

        commandLine.addValue(helpOption, "actual");
        final List expected = Collections.singletonList("actual");
        assertEquals(expected, commandLine.getValues(helpOption, defaultList));
        assertEquals(expected, commandLine.getValues("-h", defaultList));
    }

    @Test
    public void testGetUndefaultedValues_returnsOnlyExplicitValues() {
        commandLine.setDefaultValues(helpOption, Arrays.asList(new Object[]{"defaultVal"}));
        assertTrue(commandLine.getUndefaultedValues(helpOption).isEmpty());

        commandLine.addValue(helpOption, "explicitVal");
        assertEquals(Collections.singletonList("explicitVal"), commandLine.getUndefaultedValues(helpOption));
    }

    @Test
    public void testGetSwitch_byTrigger_returnsSwitchValue() {
        commandLine.addSwitch(helpOption, true);
        assertEquals(Boolean.TRUE, commandLine.getSwitch("-h"));
        assertEquals(Boolean.TRUE, commandLine.getSwitch("--help"));
        assertNull(commandLine.getSwitch("-v"));
    }

    @Test
    public void testGetSwitch_withDefault_returnsSwitchOrDefault() {
        assertEquals(Boolean.TRUE, commandLine.getSwitch(helpOption, Boolean.TRUE));
        assertEquals(Boolean.TRUE, commandLine.getSwitch("-h", Boolean.TRUE));

        commandLine.addSwitch(helpOption, false);
        assertEquals(Boolean.FALSE, commandLine.getSwitch(helpOption, Boolean.TRUE));
        assertEquals(Boolean.FALSE, commandLine.getSwitch("-h", Boolean.TRUE));
    }

    @Test
    public void testAddProperty_withOption_retrievesPropertyCorrectly() {
        commandLine.addProperty(propertyOption, "propKey", "propValue");
        assertEquals("propValue", commandLine.getProperty(propertyOption, "propKey"));
        assertEquals("propValue", commandLine.getProperty("propKey"));

        final Set names = commandLine.getPropertyNames(propertyOption);
        assertTrue(names.contains("propKey"));
    }

    @Test
    public void testGetProperty_withDefaultValue_returnsValueOrDefault() {
        assertEquals("defaultVal", commandLine.getProperty("missingKey", "defaultVal"));
        assertEquals("defaultVal", commandLine.getProperty(propertyOption, "missingKey", "defaultVal"));

        commandLine.addProperty(propertyOption, "existingKey", "actualVal");
        assertEquals("actualVal", commandLine.getProperty("existingKey", "defaultVal"));
        assertEquals("actualVal", commandLine.getProperty(propertyOption, "existingKey", "defaultVal"));
    }

    @Test
    public void testGetPropertyNames_returnsAllPropertyNames() {
        commandLine.addProperty("k1", "v1");
        commandLine.addProperty("k2", "v2");
        final Set names = commandLine.getPropertyNames();
        assertTrue(names.contains("k1"));
        assertTrue(names.contains("k2"));
    }

    @Test
    public void testToString_returnsNonNullStringRepresentation() {
        commandLine.addOption(helpOption);
        commandLine.addValue(helpOption, "arg");
        final String str = commandLine.toString();
        assertNotNull(str);
        assertTrue(str.length() > 0);
    }
}