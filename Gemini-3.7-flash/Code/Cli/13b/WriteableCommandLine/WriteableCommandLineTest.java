package org.apache.commons.cli2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.builder.SwitchBuilder;
import org.apache.commons.cli2.commandline.WriteableCommandLineImpl;
import org.apache.commons.cli2.option.DefaultOption;
import org.apache.commons.cli2.option.Group;
import org.apache.commons.cli2.option.PropertyOption;
import org.apache.commons.cli2.option.Switch;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class WriteableCommandLineTest {

    private WriteableCommandLine commandLine;
    private DefaultOption helpOption;
    private DefaultOption fileOption;
    private Switch debugSwitch;
    private PropertyOption propertyOption;
    private Group rootGroup;

    @Before
    public void setUp() {
        helpOption = new DefaultOptionBuilder()
                .withShortName("h")
                .withLongName("help")
                .withDescription("displays help")
                .create();

        fileOption = new DefaultOptionBuilder()
                .withShortName("f")
                .withLongName("file")
                .withDescription("target file")
                .create();

        debugSwitch = new SwitchBuilder()
                .withShortName("d")
                .withLongName("debug")
                .create();

        propertyOption = new PropertyOption();

        rootGroup = new GroupBuilder()
                .withOption(helpOption)
                .withOption(fileOption)
                .withOption(debugSwitch)
                .withOption(propertyOption)
                .create();

        commandLine = new WriteableCommandLineImpl(rootGroup, new ArrayList());
    }

    // Tests normal case: adding an option and verifying it is present
    @Test
    public void testAddOption_validOption_optionIsPresent() {
        assertFalse(commandLine.hasOption(helpOption));
        commandLine.addOption(helpOption);
        assertTrue(commandLine.hasOption(helpOption));
        assertTrue(commandLine.hasOption("-h"));
        assertTrue(commandLine.hasOption("--help"));
    }

    // Tests normal case: adding a single value to an option
    @Test
    public void testAddValue_singleValue_valueIsRetrievable() {
        commandLine.addOption(fileOption);
        commandLine.addValue(fileOption, "data.txt");

        assertEquals("data.txt", commandLine.getValue(fileOption));
        assertEquals("data.txt", commandLine.getValue("-f"));
        List values = commandLine.getValues(fileOption);
        assertEquals(1, values.size());
        assertEquals("data.txt", values.get(0));
    }

    // Tests normal case: adding multiple values to an option in order
    @Test
    public void testAddValue_multipleValues_valuesAreRetrievedInOrder() {
        commandLine.addOption(fileOption);
        commandLine.addValue(fileOption, "file1.txt");
        commandLine.addValue(fileOption, "file2.txt");

        List values = commandLine.getValues(fileOption);
        assertEquals(2, values.size());
        assertEquals("file1.txt", values.get(0));
        assertEquals("file2.txt", values.get(1));
    }

    // Tests normal case: setting default values when no value was added
    @Test
    public void testSetDefaultValues_noExplicitValues_returnsDefaults() {
        List defaults = Arrays.asList("default1.txt", "default2.txt");
        commandLine.setDefaultValues(fileOption, defaults);

        List values = commandLine.getValues(fileOption);
        assertEquals(2, values.size());
        assertEquals("default1.txt", values.get(0));
        assertEquals("default2.txt", values.get(1));
    }

    // Tests branch: explicit value overrides default values
    @Test
    public void testSetDefaultValues_explicitValueAdded_overridesDefaults() {
        commandLine.setDefaultValues(fileOption, Collections.singletonList("default.txt"));
        commandLine.addOption(fileOption);
        commandLine.addValue(fileOption, "custom.txt");

        List values = commandLine.getValues(fileOption);
        assertEquals(1, values.size());
        assertEquals("custom.txt", values.get(0));
    }

    // Tests edge case: setting empty list as default values
    @Test
    public void testSetDefaultValues_emptyList_returnsEmptyList() {
        commandLine.setDefaultValues(fileOption, Collections.emptyList());
        List values = commandLine.getValues(fileOption);
        assertTrue(values.isEmpty());
    }

    // Tests normal case: adding a switch with true value
    @Test
    public void testAddSwitch_trueValue_switchIsTrue() {
        commandLine.addSwitch(debugSwitch, true);
        assertTrue(commandLine.hasOption(debugSwitch));
        assertEquals(Boolean.TRUE, commandLine.getSwitch(debugSwitch));
    }

    // Tests normal case: adding a switch with false value
    @Test
    public void testAddSwitch_falseValue_switchIsFalse() {
        commandLine.addSwitch(debugSwitch, false);
        assertTrue(commandLine.hasOption(debugSwitch));
        assertEquals(Boolean.FALSE, commandLine.getSwitch(debugSwitch));
    }

    // Tests exception path: adding duplicate switch throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testAddSwitch_duplicateSwitch_throwsIllegalStateException() {
        commandLine.addSwitch(debugSwitch, true);
        commandLine.addSwitch(debugSwitch, false);
    }

    // Tests normal case: default switch value returned when no switch added
    @Test
    public void testSetDefaultSwitch_noExplicitSwitch_returnsDefault() {
        commandLine.setDefaultSwitch(debugSwitch, Boolean.TRUE);
        assertEquals(Boolean.TRUE, commandLine.getSwitch(debugSwitch));
    }

    // Tests branch: explicit switch overrides default switch value
    @Test
    public void testSetDefaultSwitch_explicitSwitchAdded_overridesDefault() {
        commandLine.setDefaultSwitch(debugSwitch, Boolean.TRUE);
        commandLine.addSwitch(debugSwitch, false);
        assertEquals(Boolean.FALSE, commandLine.getSwitch(debugSwitch));
    }

    // Tests normal case: adding property value
    @Test
    public void testAddProperty_singleProperty_propertyIsRetrievable() {
        commandLine.addProperty("key1", "value1");
        assertEquals("value1", commandLine.getProperty("key1"));
    }

    // Tests normal case: overwriting existing property value
    @Test
    public void testAddProperty_duplicateProperty_overwritesPreviousValue() {
        commandLine.addProperty("key1", "value1");
        commandLine.addProperty("key1", "value2");
        assertEquals("value2", commandLine.getProperty("key1"));
    }

    // Tests branch true: argument looks like an option trigger
    @Test
    public void testLooksLikeOption_matchingTrigger_returnsTrue() {
        assertTrue(commandLine.looksLikeOption("-h"));
        assertTrue(commandLine.looksLikeOption("--help"));
        assertTrue(commandLine.looksLikeOption("-f"));
    }

    // Tests branch false: argument does not look like an option trigger
    @Test
    public void testLooksLikeOption_nonMatchingString_returnsFalse() {
        assertFalse(commandLine.looksLikeOption("nonOption"));
        assertFalse(commandLine.looksLikeOption(""));
    }

    // Tests edge case: null argument returns false
    @Test
    public void testLooksLikeOption_nullArgument_returnsFalse() {
        assertFalse(commandLine.looksLikeOption(null));
    }
}