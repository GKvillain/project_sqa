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
import org.apache.commons.cli2.option.PropertyOption;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class WriteableCommandLineImplTest {

    private Option rootOption;
    private List args;
    private WriteableCommandLineImpl commandLine;
    private Option optA;
    private Option optB;
    private Argument argOpt;

    @Before
    public void setUp() {
        DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();
        ArgumentBuilder aBuilder = new ArgumentBuilder();

        rootOption = new PropertyOption();
        args = new ArrayList();
        args.add("-a");
        args.add("value with space");
        args.add("plainValue");

        commandLine = new WriteableCommandLineImpl(rootOption, args);

        optA = oBuilder.withShortName("a").withLongName("optA").create();
        optB = oBuilder.withShortName("b").withLongName("optB").create();
        argOpt = aBuilder.withName("arg").create();
    }

    // Tests adding an option and verifying presence and retrieval via trigger
    @Test
    public void testAddOption_andHasOption_returnsTrue() {
        assertFalse(commandLine.hasOption(optA));
        commandLine.addOption(optA);

        assertTrue(commandLine.hasOption(optA));
        assertFalse(commandLine.hasOption(optB));
        assertEquals(optA, commandLine.getOption("-a"));
        assertEquals(optA, commandLine.getOption("--optA"));
        assertEquals(1, commandLine.getOptions().size());
        assertTrue(commandLine.getOptionTriggers().contains("-a"));
    }

    // Tests addValue for Argument instance which also adds the option
    @Test
    public void testAddValue_argumentOption_automaticallyAddsOption() {
        commandLine.addValue(argOpt, "val1");

        assertTrue(commandLine.hasOption(argOpt));
        List values = commandLine.getValues(argOpt, null);
        assertEquals(1, values.size());
        assertEquals("val1", values.get(0));
    }

    // Tests addValue for standard Option does not automatically add the option
    @Test
    public void testAddValue_regularOption_doesNotAddOptionAutomatically() {
        commandLine.addValue(optA, "valA");

        assertFalse(commandLine.hasOption(optA));
        List values = commandLine.getValues(optA, null);
        assertEquals(1, values.size());
        assertEquals("valA", values.get(0));
    }

    // Tests getValues when no values are added returns empty list
    @Test
    public void testGetValues_noValuesAndNoDefaults_returnsEmptyList() {
        List values = commandLine.getValues(optA, null);
        assertTrue(values.isEmpty());
    }

    // Tests getValues when method default values are provided
    @Test
    public void testGetValues_withMethodDefaults_returnsDefaults() {
        List defaults = Arrays.asList("def1", "def2");
        List values = commandLine.getValues(optA, defaults);

        assertEquals(2, values.size());
        assertEquals("def1", values.get(0));
        assertEquals("def2", values.get(1));
    }

    // Tests getValues when commandLine default values are configured
    @Test
    public void testGetValues_withConfiguredDefaults_returnsDefaults() {
        List defaults = Arrays.asList("cDef1", "cDef2");
        commandLine.setDefaultValues(optA, defaults);

        List values = commandLine.getValues(optA, null);
        assertEquals(2, values.size());
        assertEquals("cDef1", values.get(0));
        assertEquals("cDef2", values.get(1));
    }

    // Tests getValues augmenting existing values when defaults have more elements
    @Test
    public void testGetValues_moreDefaultsThanValues_augmentsValuesList() {
        commandLine.addValue(optA, "val1");
        List defaults = Arrays.asList("def1", "def2", "def3");

        List values = commandLine.getValues(optA, defaults);
        assertEquals(3, values.size());
        assertEquals("val1", values.get(0));
        assertEquals("def2", values.get(1));
        assertEquals("def3", values.get(2));
    }

    // Tests getUndefaultedValues returns only actual command line values
    @Test
    public void testGetUndefaultedValues_withDefaultsPresent_returnsOnlyActualValues() {
        commandLine.setDefaultValues(optA, Arrays.asList("def1"));
        assertEquals(0, commandLine.getUndefaultedValues(optA).size());

        commandLine.addValue(optA, "actual1");
        List undefaulted = commandLine.getUndefaultedValues(optA);
        assertEquals(1, undefaulted.size());
        assertEquals("actual1", undefaulted.get(0));
    }

    // Tests adding switch and getting switch value
    @Test
    public void testAddSwitch_andGetSwitch_returnsSwitchValue() {
        commandLine.addSwitch(optA, true);

        assertTrue(commandLine.hasOption(optA));
        assertTrue(commandLine.getSwitch(optA, Boolean.FALSE).booleanValue());
    }

    // Tests switch fallback to method default and configured default
    @Test
    public void testGetSwitch_fallbacksToMethodDefaultAndConfiguredDefault() {
        assertNull(commandLine.getSwitch(optA, null));
        assertEquals(Boolean.FALSE, commandLine.getSwitch(optA, Boolean.FALSE));

        commandLine.setDefaultSwitch(optA, Boolean.TRUE);
        assertEquals(Boolean.TRUE, commandLine.getSwitch(optA, null));
    }

    // Tests exception path when switch is added twice
    @Test(expected = IllegalStateException.class)
    public void testAddSwitch_alreadySet_throwsIllegalStateException() {
        commandLine.addSwitch(optA, true);
        commandLine.addSwitch(optA, false);
    }

    // Tests property addition and retrieval using PropertyOption and explicit Option
    @Test
    public void testAddProperty_andGetProperty_returnsCorrectValues() {
        commandLine.addProperty("propKey", "propVal");
        assertEquals("propVal", commandLine.getProperty("propKey"));
        assertEquals("defaultVal", commandLine.getProperty(new PropertyOption(), "unknownKey", "defaultVal"));

        commandLine.addProperty(optA, "customProp", "customVal");
        assertEquals("customVal", commandLine.getProperty(optA, "customProp", null));
        assertNull(commandLine.getProperty(optB, "customProp", null));

        Set props = commandLine.getProperties();
        assertEquals(1, props.size());
        assertTrue(props.contains("propKey"));

        Set optAProps = commandLine.getProperties(optA);
        assertEquals(1, optAProps.size());
        assertTrue(optAProps.contains("customProp"));

        Set emptyProps = commandLine.getProperties(optB);
        assertTrue(emptyProps.isEmpty());
    }

    // Tests looksLikeOption with matching and non-matching triggers
    @Test
    public void testLooksLikeOption_matchingAndNonMatchingPrefix_returnsExpectedBoolean() {
        assertTrue(commandLine.looksLikeOption("-Dproperty=value"));
        assertFalse(commandLine.looksLikeOption("plainArgument"));
    }

    // Tests toString formatting with argument quoting when spaces are present
    @Test
    public void testToString_normalisedArgsWithAndWithoutSpaces_formatsCorrectly() {
        String expected = "-a \"value with space\" plainValue";
        assertEquals(expected, commandLine.toString());
    }

    // Tests removing default values and default switch by passing null
    @Test
    public void testSetDefaultValuesAndSwitch_nullInput_removesDefaults() {
        commandLine.setDefaultValues(optA, Arrays.asList("def"));
        commandLine.setDefaultValues(optA, null);
        assertTrue(commandLine.getValues(optA, null).isEmpty());

        commandLine.setDefaultSwitch(optA, Boolean.TRUE);
        commandLine.setDefaultSwitch(optA, null);
        assertNull(commandLine.getSwitch(optA, null));
    }

    // Tests getNormalised returns unmodifiable list matching input
    @Test
    public void testGetNormalised_returnsExpectedArgumentsList() {
        List normalised = commandLine.getNormalised();
        assertEquals(args.size(), normalised.size());
        assertEquals("-a", normalised.get(0));
    }

    // Tests hasOption by trigger string
    @Test
    public void testHasOption_byTriggerString() {
        assertFalse(commandLine.hasOption("-a"));
        assertFalse(commandLine.hasOption("--optA"));

        commandLine.addOption(optA);
        assertTrue(commandLine.hasOption("-a"));
        assertTrue(commandLine.hasOption("--optA"));
        assertFalse(commandLine.hasOption("-b"));
        assertFalse(commandLine.hasOption("--nonExistent"));
    }

    // Tests getOption when trigger is not found
    @Test
    public void testGetOption_unknownTrigger_returnsNull() {
        assertNull(commandLine.getOption("-unknown"));
        assertNull(commandLine.getOption("--unknown"));
    }

    // Tests getValue by Option and by trigger string
    @Test
    public void testGetValue_byOptionAndByTrigger() {
        assertNull(commandLine.getValue(optA));
        assertEquals("defaultVal", commandLine.getValue(optA, "defaultVal"));
        assertNull(commandLine.getValue("-a"));
        assertEquals("defaultVal", commandLine.getValue("-a", "defaultVal"));

        commandLine.addOption(optA);
        commandLine.addValue(optA, "val1");
        commandLine.addValue(optA, "val2");

        assertEquals("val1", commandLine.getValue(optA));
        assertEquals("val1", commandLine.getValue(optA, "defaultVal"));
        assertEquals("val1", commandLine.getValue("-a"));
        assertEquals("val1", commandLine.getValue("-a", "defaultVal"));
        assertEquals("val1", commandLine.getValue("--optA"));
    }

    // Tests getValues by trigger string
    @Test
    public void testGetValues_byTriggerString() {
        commandLine.addOption(optA);
        commandLine.addValue(optA, "val1");
        commandLine.addValue(optA, "val2");

        List valuesByShort = commandLine.getValues("-a");
        assertEquals(2, valuesByShort.size());
        assertEquals("val1", valuesByShort.get(0));
        assertEquals("val2", valuesByShort.get(1));

        List valuesByLong = commandLine.getValues("--optA");
        assertEquals(2, valuesByLong.size());

        List unknownValues = commandLine.getValues("-unknown");
        assertTrue(unknownValues.isEmpty());

        List fallbackValues = commandLine.getValues("-unknown", Arrays.asList("fallback"));
        assertEquals(1, fallbackValues.size());
        assertEquals("fallback", fallbackValues.get(0));
    }

    // Tests getSwitch with false value and by trigger string
    @Test
    public void testGetSwitch_falseValueAndByTrigger() {
        commandLine.addOption(optA);
        commandLine.addSwitch(optA, false);

        assertEquals(Boolean.FALSE, commandLine.getSwitch(optA));
        assertEquals(Boolean.FALSE, commandLine.getSwitch("-a"));
        assertEquals(Boolean.FALSE, commandLine.getSwitch("--optA"));
        assertNull(commandLine.getSwitch("-unknown"));
        assertEquals(Boolean.TRUE, commandLine.getSwitch("-unknown", Boolean.TRUE));
    }

    // Tests getProperty by name with default value overload
    @Test
    public void testGetProperty_withDefaultValue() {
        assertEquals("defaultVal", commandLine.getProperty("nonExistentKey", "defaultVal"));
        commandLine.addProperty("myKey", "myVal");
        assertEquals("myVal", commandLine.getProperty("myKey", "defaultVal"));
    }

    // Tests getValues when more actual values exist than defaults
    @Test
    public void testGetValues_moreValuesThanDefaults_returnsAllValues() {
        commandLine.addValue(optA, "val1");
        commandLine.addValue(optA, "val2");
        commandLine.addValue(optA, "val3");

        List defaults = Arrays.asList("def1");
        List values = commandLine.getValues(optA, defaults);

        assertEquals(3, values.size());
        assertEquals("val1", values.get(0));
        assertEquals("val2", values.get(1));
        assertEquals("val3", values.get(2));
    }

    // Tests looksLikeOption when rootOption has no prefix or is null
    @Test
    public void testLooksLikeOption_nullRootOption() {
        WriteableCommandLineImpl clNullRoot = new WriteableCommandLineImpl(null, new ArrayList());
        assertFalse(clNullRoot.looksLikeOption("-a"));
    }

    // Tests addProperty with null option uses rootOption
    @Test
    public void testAddProperty_nullOptionUsesRootOption() {
        commandLine.addProperty(null, "rootProp", "rootVal");
        assertEquals("rootVal", commandLine.getProperty("rootProp"));
    }
}