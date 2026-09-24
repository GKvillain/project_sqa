package org.apache.commons.cli2.commandline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class WriteableCommandLineImplTest {

    private Option rootOption;
    private Option optA;
    private Option optB;
    private Argument arg;
    private List argsList;
    private WriteableCommandLineImpl commandLine;

    @Before
    public void setUp() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();

        optA = obuilder.withShortName("a").withLongName("alpha").create();
        optB = obuilder.withShortName("b").withLongName("beta").create();
        arg = abuilder.withName("target").create();

        rootOption = gbuilder.withOption(optA).withOption(optB).create();
        argsList = new ArrayList();
        commandLine = new WriteableCommandLineImpl(rootOption, argsList);
    }

    // Tests adding an option and verifying option lookup and presence
    @Test
    public void testAddOption_validOption_optionIsPresentAndSearchable() {
        commandLine.addOption(optA);

        assertTrue(commandLine.hasOption(optA));
        assertFalse(commandLine.hasOption(optB));
        assertEquals(optA, commandLine.getOption("-a"));
        assertEquals(optA, commandLine.getOption("--alpha"));
        assertEquals(1, commandLine.getOptions().size());
        assertTrue(commandLine.getOptions().contains(optA));
        assertTrue(commandLine.getOptionTriggers().contains("-a"));
        assertTrue(commandLine.getOptionTriggers().contains("--alpha"));
    }

    // Tests adding values to a standard option
    @Test
    public void testAddValue_standardOption_storesValuesCorrectly() {
        commandLine.addValue(optA, "val1");
        commandLine.addValue(optA, "val2");

        List values = commandLine.getValues(optA, null);
        assertNotNull(values);
        assertEquals(2, values.size());
        assertEquals("val1", values.get(0));
        assertEquals("val2", values.get(1));
    }

    // Tests adding value to an Argument type which automatically adds the option
    @Test
    public void testAddValue_argumentInstance_automaticallyAddsOption() {
        commandLine.addValue(arg, "file.txt");

        assertTrue(commandLine.hasOption(arg));
        List values = commandLine.getValues(arg, null);
        assertEquals(1, values.size());
        assertEquals("file.txt", values.get(0));
    }

    // Tests getValues fallback when no values are added directly
    @Test
    public void testGetValues_noValuesPresent_usesMethodDefaults() {
        List defaults = Arrays.asList(new Object[]{"default1", "default2"});
        List values = commandLine.getValues(optA, defaults);

        assertEquals(defaults, values);
    }

    // Tests getValues fallback to instance default values
    @Test
    public void testGetValues_noValuesAndNoMethodDefaults_usesConfiguredDefaultValues() {
        List defaults = Arrays.asList(new Object[]{"cfgDefault"});
        commandLine.setDefaultValues(optA, defaults);

        List values = commandLine.getValues(optA, null);
        assertEquals(defaults, values);
    }

    // Tests getValues returning empty list when no values or defaults exist
    @Test
    public void testGetValues_noValuesOrDefaults_returnsEmptyList() {
        List values = commandLine.getValues(optA, null);
        assertNotNull(values);
        assertTrue(values.isEmpty());
        assertEquals(Collections.EMPTY_LIST, values);
    }

    // Tests removing default values by passing null to setDefaultValues
    @Test
    public void testSetDefaultValues_nullDefaults_removesConfiguredDefaults() {
        List defaults = Arrays.asList(new Object[]{"cfgDefault"});
        commandLine.setDefaultValues(optA, defaults);
        commandLine.setDefaultValues(optA, null);

        List values = commandLine.getValues(optA, null);
        assertTrue(values.isEmpty());
    }

    // Tests adding a switch and querying its boolean value
    @Test
    public void testAddSwitch_validSwitch_returnsCorrectBoolean() {
        commandLine.addSwitch(optA, true);

        assertTrue(commandLine.hasOption(optA));
        assertEquals(Boolean.TRUE, commandLine.getSwitch(optA, null));
    }

    // Tests adding a duplicate switch throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testAddSwitch_alreadySet_throwsIllegalStateException() {
        commandLine.addSwitch(optA, true);
        commandLine.addSwitch(optA, false);
    }

    // Tests getSwitch fallback to method defaultValue
    @Test
    public void testGetSwitch_noSwitchPresent_usesMethodDefault() {
        Boolean result = commandLine.getSwitch(optA, Boolean.FALSE);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests getSwitch fallback to instance defaultSwitch
    @Test
    public void testGetSwitch_noSwitchAndNoMethodDefault_usesConfiguredDefaultSwitch() {
        commandLine.setDefaultSwitch(optA, Boolean.TRUE);
        Boolean result = commandLine.getSwitch(optA, null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests getSwitch when no switch or default is configured
    @Test
    public void testGetSwitch_noValuesOrDefaults_returnsNull() {
        Boolean result = commandLine.getSwitch(optA, null);
        assertNull(result);
    }

    // Tests removing default switch by passing null
    @Test
    public void testSetDefaultSwitch_nullValue_removesConfiguredDefaultSwitch() {
        commandLine.setDefaultSwitch(optA, Boolean.TRUE);
        commandLine.setDefaultSwitch(optA, null);

        assertNull(commandLine.getSwitch(optA, null));
    }

    // Tests adding and retrieving properties
    @Test
    public void testProperties_addAndGet_returnsCorrectPropertyValues() {
        commandLine.addProperty("key1", "value1");

        assertEquals("value1", commandLine.getProperty("key1", "defaultVal"));
        assertEquals("defaultVal", commandLine.getProperty("unknownKey", "defaultVal"));

        Set properties = commandLine.getProperties();
        assertEquals(1, properties.size());
        assertTrue(properties.contains("key1"));
    }

    // Tests looksLikeOption with valid prefix
    @Test
    public void testLooksLikeOption_matchingPrefix_returnsTrue() {
        assertTrue(commandLine.looksLikeOption("-a"));
        assertTrue(commandLine.looksLikeOption("--alpha"));
    }

    // Tests looksLikeOption with non-matching prefix
    @Test
    public void testLooksLikeOption_nonMatchingPrefix_returnsFalse() {
        assertFalse(commandLine.looksLikeOption("plainArgument"));
    }

    // Tests toString formatting with single and multi-word arguments
    @Test
    public void testToString_argumentsWithAndWithoutSpaces_formatsCorrectly() {
        argsList.add("-a");
        argsList.add("hello world");
        argsList.add("single");

        String result = commandLine.toString();
        assertEquals("-a \"hello world\" single", result);
    }

    // Tests getNormalised returns the backing list
    @Test
    public void testGetNormalised_populatedArguments_returnsUnmodifiableList() {
        argsList.add("-a");
        argsList.add("value");

        List normalised = commandLine.getNormalised();
        assertEquals(2, normalised.size());
        assertEquals("-a", normalised.get(0));
        assertEquals("value", normalised.get(1));
    }

    // Tests getUndefaultedValues returning empty list when no explicit values were added
    @Test
    public void testGetUndefaultedValues_noExplicitValues_returnsEmptyListEvenWithDefaults() {
        commandLine.setDefaultValues(optA, Arrays.asList(new Object[]{"defaultVal"}));

        List undefaulted = commandLine.getUndefaultedValues(optA);
        assertNotNull(undefaulted);
        assertTrue(undefaulted.isEmpty());
    }

    // Tests getUndefaultedValues returning unmodifiable list of explicitly added values
    @Test
    public void testGetUndefaultedValues_withExplicitValues_returnsExplicitValuesOnly() {
        commandLine.setDefaultValues(optA, Arrays.asList(new Object[]{"defaultVal"}));
        commandLine.addValue(optA, "explicitVal1");
        commandLine.addValue(optA, "explicitVal2");

        List undefaulted = commandLine.getUndefaultedValues(optA);
        assertEquals(2, undefaulted.size());
        assertEquals("explicitVal1", undefaulted.get(0));
        assertEquals("explicitVal2", undefaulted.get(1));
    }

    // Tests getUndefaultedValues returning unmodifiable list that prevents mutation
    @Test(expected = UnsupportedOperationException.class)
    public void testGetUndefaultedValues_returnedListIsUnmodifiable() {
        commandLine.addValue(optA, "explicitVal");
        List undefaulted = commandLine.getUndefaultedValues(optA);
        undefaulted.add("illegal");
    }
}