package org.apache.commons.cli2.commandline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.WriteableCommandLine;
import org.apache.commons.cli2.option.PropertyOption;
import org.junit.Before;
import org.junit.Test;

public class WriteableCommandLineImplTest {

    private static class SimpleOption implements Option {
        private final String preferredName;
        private final Set<String> triggers;
        private final Option parent;
        private final Set<String> prefixes;

        public SimpleOption(String preferredName, Set<String> triggers, Option parent, Set<String> prefixes) {
            this.preferredName = preferredName;
            this.triggers = triggers;
            this.parent = parent;
            this.prefixes = prefixes;
        }

        @Override
        public boolean canProcess(WriteableCommandLine wcl, String arg) {
            return triggers.contains(arg);
        }

        @Override
        public Set<String> getPrefixes() {
            return prefixes;
        }

        @Override
        public String getPreferredName() {
            return preferredName;
        }

        @Override
        public Option getParent() {
            return parent;
        }

        @Override
        public Set<String> getTriggers() {
            return triggers;
        }

        @Override
        public void process(WriteableCommandLine wcl, ListIterator arguments) {
            // no-op
        }

        @Override
        public void defaults(WriteableCommandLine wcl) {
            // no-op
        }

        @Override
        public void validate(WriteableCommandLine wcl) {
            // no-op
        }

        @Override
        public boolean isRequired() {
            return false;
        }

        @Override
        public String toString() {
            return preferredName;
        }
    }

    private WriteableCommandLineImpl commandLine;
    private Option rootOption;
    private Option option1;
    private Option option2;
    private Option propertyOption;

    @Before
    public void setUp() {
        Set<String> prefixes = new HashSet<String>();
        prefixes.add("--");
        rootOption = new SimpleOption("--root", Collections.singleton("--root"), null, prefixes);
        option1 = new SimpleOption("--foo", new HashSet<String>(Arrays.asList("--foo", "-f")), null, prefixes);
        option2 = new SimpleOption("--bar", Collections.singleton("--bar"), rootOption, prefixes);
        propertyOption = new PropertyOption();
        List<String> args = new ArrayList<String>();
        args.add("arg1");
        args.add("arg2");
        commandLine = new WriteableCommandLineImpl(rootOption, args);
    }

    // Tests constructor initial state
    @Test
    public void testConstructor_initialState_returnsDefaults() {
        assertNotNull(commandLine);
        assertEquals(2, commandLine.getNormalised().size());
        assertTrue(commandLine.getNormalised().contains("arg1"));
        assertTrue(commandLine.getOptions().isEmpty());
        assertTrue(commandLine.getOptionTriggers().isEmpty());
    }

    // Tests addOption adds option and makes it visible
    @Test
    public void testAddOption_addsOption_returnsTrueForHasOption() {
        commandLine.addOption(option1);
        assertTrue(commandLine.hasOption(option1));
        assertTrue(commandLine.getOptions().contains(option1));
        assertEquals(option1, commandLine.getOption("--foo"));
        assertEquals(option1, commandLine.getOption("-f"));
    }

    // Tests addOption adds parent options automatically
    @Test
    public void testAddOption_parentsAdded_parentAlsoAdded() {
        commandLine.addOption(option2);
        assertTrue(commandLine.hasOption(option2));
        assertTrue(commandLine.hasOption(rootOption));
        assertTrue(commandLine.getOptions().contains(rootOption));
    }

    // Tests addOption adds triggers to optionTriggers
    @Test
    public void testAddOption_triggersAddedToOptionTriggers() {
        commandLine.addOption(option1);
        Set triggers = commandLine.getOptionTriggers();
        assertTrue(triggers.contains("--foo"));
        assertTrue(triggers.contains("-f"));
    }

    // Tests addValue adds value for a non-Argument option (should not add the option)
    @Test
    public void testAddValue_nonArgumentOption_doesNotAddOption() {
        commandLine.addValue(option1, "val");
        assertFalse(commandLine.hasOption(option1));
    }

    // Tests addValue with single value
    @Test
    public void testAddValue_singleValue_returnsListWithValue() {
        commandLine.addOption(option1);
        commandLine.addValue(option1, "value1");
        List values = commandLine.getValues(option1, null);
        assertEquals(1, values.size());
        assertEquals("value1", values.get(0));
    }

    // Tests addValue with multiple values
    @Test
    public void testAddValue_multipleValues_returnsAllValues() {
        commandLine.addOption(option1);
        commandLine.addValue(option1, "v1");
        commandLine.addValue(option1, "v2");
        List values = commandLine.getValues(option1, null);
        assertEquals(2, values.size());
        assertEquals("v1", values.get(0));
        assertEquals("v2", values.get(1));
    }

    // Tests getValues without defaults returns empty list
    @Test
    public void testGetValues_withoutDefaults_returnsEmptyList() {
        commandLine.addOption(option1);
        List values = commandLine.getValues(option1, null);
        assertTrue(values.isEmpty());
    }

    // Tests getValues with defaults stored and fewer values -> fills with defaults
    @Test
    public void testAddValue_defaultValues_fillGap_returnsValuesPlusDefaults() {
        commandLine.addOption(option1);
        List defaults = Arrays.asList("d1", "d2", "d3");
        commandLine.setDefaultValues(option1, defaults);
        commandLine.addValue(option1, "v1");
        List values = commandLine.getValues(option1, null);
        assertEquals(3, values.size());
        assertEquals("v1", values.get(0));
        assertEquals("d2", values.get(1));
        assertEquals("d3", values.get(2));
    }

    // Tests getValues with default param list when no values exist
    @Test
    public void testGetValues_withDefaultListParam_returnsParamValuesWhenNoValues() {
        commandLine.addOption(option1);
        commandLine.setDefaultValues(option1, Arrays.asList("stored"));
        List values = commandLine.getValues(option1, Arrays.asList("param"));
        assertEquals(1, values.size());
        assertEquals("param", values.get(0));
    }

    // Tests getValues with null param but stored defaults and existing values equal size
    @Test
    public void testGetValues_defaultValuesNullParam_usesStoredDefaults() {
        commandLine.addOption(option1);
        commandLine.setDefaultValues(option1, Arrays.asList("stored"));
        commandLine.addValue(option1, "v1");
        List values = commandLine.getValues(option1, null);
        assertEquals(1, values.size());
        assertEquals("v1", values.get(0));
    }

    // Tests getUndefaultedValues returns only command line values
    @Test
    public void testGetUndefaultedValues_withoutDefaults_returnsValuesOnly() {
        commandLine.addOption(option1);
        commandLine.addValue(option1, "v1");
        commandLine.setDefaultValues(option1, Arrays.asList("d1"));
        List undefaulted = commandLine.getUndefaultedValues(option1);
        assertEquals(1, undefaulted.size());
        assertEquals("v1", undefaulted.get(0));
    }

    // Tests addSwitch normal case
    @Test
    public void testAddSwitch_normal_returnsTrue() {
        commandLine.addSwitch(option1, true);
        Boolean sw = commandLine.getSwitch(option1, null);
        assertEquals(Boolean.TRUE, sw);
    }

    // Tests addSwitch duplicate throws exception
    @Test(expected = IllegalStateException.class)
    public void testAddSwitch_duplicate_throwsException() {
        commandLine.addSwitch(option1, true);
        commandLine.addSwitch(option1, false);
    }

    // Tests getSwitch with default value when switch not set
    @Test
    public void testGetSwitch_defaultValue_returnsDefault() {
        Boolean result = commandLine.getSwitch(option1, Boolean.TRUE);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests getSwitch with stored default switch
    @Test
    public void testGetSwitch_defaultSwitch_returnsStoredDefault() {
        commandLine.setDefaultSwitch(option1, Boolean.FALSE);
        Boolean result = commandLine.getSwitch(option1, null);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests getSwitch with existing switch overrides default value
    @Test
    public void testGetSwitch_existingSwitch_overridesDefault() {
        commandLine.addSwitch(option1, true);
        Boolean result = commandLine.getSwitch(option1, false);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests looksLikeOption with matching prefix
    @Test
    public void testLooksLikeOption_prefixMatch_returnsTrue() {
        assertTrue(commandLine.looksLikeOption("--foo"));
    }

    // Tests looksLikeOption with non-matching prefix
    @Test
    public void testLooksLikeOption_noMatch_returnsFalse() {
        assertFalse(commandLine.looksLikeOption("foo"));
    }

    // Tests getProperty(String)
    @Test
    public void testGetProperty_string_returnsProperty() {
        commandLine.addProperty("key", "value");
        assertEquals("value", commandLine.getProperty("key"));
    }

    // Tests getProperty with option and property exists
    @Test
    public void testGetProperty_option_returnsProperty() {
        commandLine.addProperty(propertyOption, "key", "value");
        assertEquals("value", commandLine.getProperty(propertyOption, "key", "default"));
    }

    // Tests getProperty with option returns default for missing property
    @Test
    public void testGetProperty_option_defaultValue_returnsDefault() {
        assertEquals("default", commandLine.getProperty(propertyOption, "nonexistent", "default"));
    }

    // Tests getProperties returns set of keys
    @Test
    public void testGetProperties_option_returnsSet() {
        commandLine.addProperty(propertyOption, "key1", "val1");
        commandLine.addProperty(propertyOption, "key2", "val2");
        Set props = commandLine.getProperties(propertyOption);
        assertEquals(2, props.size());
        assertTrue(props.contains("key1"));
        assertTrue(props.contains("key2"));
    }

    // Tests toString with space in argument
    @Test
    public void testToString_withSpaces_quotesValues() {
        List argsWithSpace = new ArrayList();
        argsWithSpace.add("normal");
        argsWithSpace.add("with space");
        Set<String> prefixes = new HashSet<String>();
        prefixes.add("--");
        Option root = new SimpleOption("--root", Collections.singleton("--root"), null, prefixes);
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, argsWithSpace);
        String result = cmd.toString();
        assertEquals("normal \"with space\"", result);
    }

    // Tests setDefaultValues with null removes defaults
    @Test
    public void testSetDefaultValues_remove_removesDefaults() {
        commandLine.setDefaultValues(option1, Arrays.asList("d1"));
        commandLine.setDefaultValues(option1, null);
        List values = commandLine.getValues(option1, null);
        assertTrue(values.isEmpty());
    }

    // Tests setDefaultSwitch with null removes default
    @Test
    public void testSetDefaultSwitch_remove_removesDefault() {
        commandLine.setDefaultSwitch(option1, Boolean.TRUE);
        commandLine.setDefaultSwitch(option1, null);
        Boolean sw = commandLine.getSwitch(option1, null);
        assertNull(sw);
    }

    // ===== New test cases to improve coverage =====

    // Tests hasOption(String trigger)
    @Test
    public void testHasOption_string_trigger() {
        commandLine.addOption(option1);
        assertTrue(commandLine.hasOption("--foo"));
        assertTrue(commandLine.hasOption("-f"));
        assertFalse(commandLine.hasOption("--nonexistent"));
    }

    // Tests getOption returns null for missing trigger
    @Test
    public void testGetOption_notExists() {
        assertNull(commandLine.getOption("--nonexistent"));
    }

    // Tests addProperty duplicate overwrites
    @Test
    public void testAddProperty_duplicate_overwrites() {
        commandLine.addProperty("key", "first");
        commandLine.addProperty("key", "second");
        assertEquals("second", commandLine.getProperty("key"));
    }

    // Tests getProperties (global) returns all global property keys
    @Test
    public void testGetProperties_global() {
        commandLine.addProperty("prop1", "val1");
        commandLine.addProperty("prop2", "val2");
        Set props = commandLine.getProperties();
        assertEquals(2, props.size());
        assertTrue(props.contains("prop1"));
        assertTrue(props.contains("prop2"));
    }
}