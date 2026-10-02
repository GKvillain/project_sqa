package org.apache.commons.cli2.commandline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class WriteableCommandLineImplTest {

    private Option option;
    private Argument argument;
    private Group rootOption;
    private List argumentsList;
    private WriteableCommandLineImpl commandLine;

    @Before
    public void setUp() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();

        option = obuilder
            .withShortName("o")
            .withLongName("opt")
            .withDescription("test option")
            .create();

        argument = abuilder
            .withName("target")
            .create();

        rootOption = gbuilder
            .withOption(option)
            .withOption(argument)
            .create();

        argumentsList = new ArrayList();
        argumentsList.add("-o");
        argumentsList.add("targetValue");

        commandLine = new WriteableCommandLineImpl(rootOption, argumentsList);
    }

    // Tests adding an option and retrieving triggers and options
    @Test
    public void testAddOption_validOption_registersOptionAndTriggers() {
        commandLine.addOption(option);

        assertTrue(commandLine.hasOption(option));
        assertEquals(option, commandLine.getOption("-o"));
        assertEquals(option, commandLine.getOption("opt"));

        final List options = commandLine.getOptions();
        assertEquals(1, options.size());
        assertTrue(options.contains(option));

        final Set triggers = commandLine.getOptionTriggers();
        assertTrue(triggers.contains("-o"));
        assertTrue(triggers.contains("opt"));
    }

    // Tests getOption with non-existent trigger
    @Test
    public void testGetOption_unknownTrigger_returnsNull() {
        assertNull(commandLine.getOption("unknown"));
    }

    // Tests hasOption for absent option
    @Test
    public void testHasOption_absentOption_returnsFalse() {
        assertFalse(commandLine.hasOption(option));
    }

    // Tests adding values to a non-argument option
    @Test
    public void testAddValue_nonArgumentOption_addsValueWithoutAddingOption() {
        commandLine.addValue(option, "value1");
        commandLine.addValue(option, "value2");

        final List values = commandLine.getValues(option, null);
        assertEquals(2, values.size());
        assertEquals("value1", values.get(0));
        assertEquals("value2", values.get(1));
        assertFalse(commandLine.hasOption(option));
    }

    // Tests adding value to an argument option which automatically adds the option
    @Test
    public void testAddValue_argumentOption_addsOptionAndValue() {
        commandLine.addValue(argument, "file.txt");

        assertTrue(commandLine.hasOption(argument));
        final List values = commandLine.getValues(argument, null);
        assertEquals(1, sizeOf(values));
        assertEquals("file.txt", values.get(0));
    }

    private int sizeOf(List list) {
        return list == null ? 0 : list.size();
    }

    // Tests fallback hierarchy of getValues: method defaults then option defaults then empty list
    @Test
    public void testGetValues_fallbacks_returnsCorrectDefaultHierarchy() {
        final List methodDefaults = Arrays.asList(new Object[]{"methodDefault"});
        final List optionDefaults = Arrays.asList(new Object[]{"optionDefault"});

        // 1. Returns method defaults when no values exist
        List values = commandLine.getValues(option, methodDefaults);
        assertEquals(methodDefaults, values);

        // 2. Returns option defaults when no values and empty/null method defaults
        commandLine.setDefaultValues(option, optionDefaults);
        values = commandLine.getValues(option, Collections.EMPTY_LIST);
        assertEquals(optionDefaults, values);

        // 3. Returns empty list when no values, no method defaults, and no option defaults
        commandLine.setDefaultValues(option, null);
        values = commandLine.getValues(option, null);
        assertEquals(Collections.EMPTY_LIST, values);
    }

    // Tests setting default values to null removes the default
    @Test
    public void testSetDefaultValues_nullInput_removesDefaultValues() {
        final List optionDefaults = Arrays.asList(new Object[]{"optionDefault"});
        commandLine.setDefaultValues(option, optionDefaults);
        commandLine.setDefaultValues(option, null);

        final List values = commandLine.getValues(option, null);
        assertEquals(Collections.EMPTY_LIST, values);
    }

    // Tests adding a switch value and checking option presence
    @Test
    public void testAddSwitch_trueAndFalseValues_storesSwitchValue() {
        commandLine.addSwitch(option, true);

        assertTrue(commandLine.hasOption(option));
        assertEquals(Boolean.TRUE, commandLine.getSwitch(option, null));
    }

    // Tests exception path when adding a switch twice for the same option
    @Test(expected = IllegalStateException.class)
    public void testAddSwitch_alreadyPresent_throwsIllegalStateException() {
        commandLine.addSwitch(option, true);
        commandLine.addSwitch(option, false);
    }

    // Tests fallback hierarchy of getSwitch: method default then option default then null
    @Test
    public void testGetSwitch_fallbacks_returnsCorrectDefaultHierarchy() {
        // 1. Method default when switch is not set
        assertEquals(Boolean.TRUE, commandLine.getSwitch(option, Boolean.TRUE));

        // 2. Option default when switch is not set and method default is null
        commandLine.setDefaultSwitch(option, Boolean.FALSE);
        assertEquals(Boolean.FALSE, commandLine.getSwitch(option, null));

        // 3. Null when no switch and no defaults exist
        commandLine.setDefaultSwitch(option, null);
        assertNull(commandLine.getSwitch(option, null));
    }

    // Tests setting default switch to null removes the default
    @Test
    public void testSetDefaultSwitch_nullInput_removesDefaultSwitch() {
        commandLine.setDefaultSwitch(option, Boolean.TRUE);
        commandLine.setDefaultSwitch(option, null);

        assertNull(commandLine.getSwitch(option, null));
    }

    // Tests adding and retrieving properties
    @Test
    public void testProperties_addAndGet_returnsCorrectPropertyValues() {
        commandLine.addProperty("property1", "value1");

        assertEquals("value1", commandLine.getProperty("property1", "default"));
        assertEquals("default", commandLine.getProperty("unknownProperty", "default"));

        final Set propertyNames = commandLine.getProperties();
        assertEquals(1, propertyNames.size());
        assertTrue(propertyNames.contains("property1"));
    }

    // Tests prefix matching for looksLikeOption
    @Test
    public void testLooksLikeOption_validAndInvalidPrefixes_returnsExpectedBoolean() {
        assertTrue(commandLine.looksLikeOption("-o"));
        assertTrue(commandLine.looksLikeOption("--opt"));
        assertFalse(commandLine.looksLikeOption("noPrefix"));
    }

    // Tests toString formatting with spaces and without spaces
    @Test
    public void testToString_argumentsWithAndWithoutSpaces_quotesCorrectly() {
        final List args = Arrays.asList(new Object[]{"-o", "single", "spaced argument"});
        final WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(rootOption, args);

        assertEquals("-o single \"spaced argument\"", cmd.toString());
    }

    // Tests toString with empty argument list
    @Test
    public void testToString_emptyArguments_returnsEmptyString() {
        final WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(rootOption, Collections.EMPTY_LIST);
        assertEquals("", cmd.toString());
    }

    // Tests retrieving normalized arguments list
    @Test
    public void testGetNormalised_returnsOriginalArguments() {
        assertEquals(argumentsList, commandLine.getNormalised());
    }

    // --- New Tests ---

    @Test
    public void testHasOption_byTrigger_returnsExpectedBoolean() {
        assertFalse(commandLine.hasOption("-o"));
        assertFalse(commandLine.hasOption("opt"));
        assertFalse(commandLine.hasOption("unknown"));

        commandLine.addOption(option);

        assertTrue(commandLine.hasOption("-o"));
        assertTrue(commandLine.hasOption("opt"));
        assertFalse(commandLine.hasOption("unknown"));
    }

    @Test
    public void testGetValue_byOptionAndTrigger_returnsFirstValueOrDefault() {
        assertNull(commandLine.getValue(option));
        assertEquals("defaultVal", commandLine.getValue(option, "defaultVal"));
        assertNull(commandLine.getValue("-o"));
        assertEquals("defaultVal", commandLine.getValue("-o", "defaultVal"));

        commandLine.addOption(option);
        commandLine.addValue(option, "val1");
        commandLine.addValue(option, "val2");

        assertEquals("val1", commandLine.getValue(option));
        assertEquals("val1", commandLine.getValue(option, "defaultVal"));
        assertEquals("val1", commandLine.getValue("-o"));
        assertEquals("val1", commandLine.getValue("-o", "defaultVal"));
        assertEquals("val1", commandLine.getValue("opt"));
        assertEquals("val1", commandLine.getValue("opt", "defaultVal"));

        assertNull(commandLine.getValue("unknown"));
        assertEquals("fallback", commandLine.getValue("unknown", "fallback"));
    }

    @Test
    public void testGetValues_byTrigger_returnsValuesList() {
        commandLine.addOption(option);
        commandLine.addValue(option, "v1");
        commandLine.addValue(option, "v2");

        List byShortTrigger = commandLine.getValues("-o");
        assertEquals(2, byShortTrigger.size());
        assertEquals("v1", byShortTrigger.get(0));
        assertEquals("v2", byShortTrigger.get(1));

        List byLongTrigger = commandLine.getValues("opt");
        assertEquals(2, byLongTrigger.size());

        List byOption = commandLine.getValues(option);
        assertEquals(2, byOption.size());

        List unknownValues = commandLine.getValues("unknown");
        assertEquals(Collections.EMPTY_LIST, unknownValues);

        List unknownWithDefault = commandLine.getValues("unknown", Arrays.asList(new Object[]{"def"}));
        assertEquals(1, unknownWithDefault.size());
        assertEquals("def", unknownWithDefault.get(0));
    }

    @Test
    public void testGetUndefaultedValues_returnsOnlyExplicitlyAddedValues() {
        commandLine.setDefaultValues(option, Arrays.asList(new Object[]{"defaultVal"}));
        List undefaulted = commandLine.getUndefaultedValues(option);
        assertEquals(Collections.EMPTY_LIST, undefaulted);

        commandLine.addValue(option, "actualVal");
        undefaulted = commandLine.getUndefaultedValues(option);
        assertEquals(1, undefaulted.size());
        assertEquals("actualVal", undefaulted.get(0));

        List undefaultedNull = commandLine.getUndefaultedValues(null);
        assertEquals(Collections.EMPTY_LIST, undefaultedNull);
    }

    @Test
    public void testGetSwitch_byTriggerAndOptionOverloads() {
        assertNull(commandLine.getSwitch(option));
        assertNull(commandLine.getSwitch("-o"));
        assertEquals(Boolean.FALSE, commandLine.getSwitch("-o", Boolean.FALSE));
        assertNull(commandLine.getSwitch("unknown"));
        assertEquals(Boolean.TRUE, commandLine.getSwitch("unknown", Boolean.TRUE));

        commandLine.addOption(option);
        commandLine.addSwitch(option, true);

        assertEquals(Boolean.TRUE, commandLine.getSwitch(option));
        assertEquals(Boolean.TRUE, commandLine.getSwitch("-o"));
        assertEquals(Boolean.TRUE, commandLine.getSwitch("opt"));
        assertEquals(Boolean.TRUE, commandLine.getSwitch("-o", Boolean.FALSE));
    }

    @Test
    public void testAddProperty_withOption_storesAndRetrievesProperty() {
        commandLine.addProperty(option, "key1", "val1");

        assertEquals("val1", commandLine.getProperty(option, "key1"));
        assertEquals("val1", commandLine.getProperty(option, "key1", "default"));
        assertNull(commandLine.getProperty(option, "unknownKey"));
        assertEquals("default", commandLine.getProperty(option, "unknownKey", "default"));

        Set props = commandLine.getProperties(option);
        assertNotNull(props);
        assertEquals(1, props.size());
        assertTrue(props.contains("key1"));

        Set unknownProps = commandLine.getProperties((Option) null);
        assertEquals(Collections.EMPTY_SET, unknownProps);

        assertNull(commandLine.getProperty((Option) null, "key1"));
        assertEquals("default", commandLine.getProperty((Option) null, "key1", "default"));
    }

    @Test
    public void testGetProperty_singleArgOverload() {
        commandLine.addProperty("propA", "valueA");

        assertEquals("valueA", commandLine.getProperty("propA"));
        assertNull(commandLine.getProperty("nonExistent"));
    }
}