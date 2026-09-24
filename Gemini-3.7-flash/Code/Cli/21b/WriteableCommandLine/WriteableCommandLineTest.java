package org.apache.commons.cli2;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WriteableCommandLineTest {

    private WriteableCommandLine commandLine;
    private Option testOption;

    @Before
    public void setUp() {
        commandLine = new MockWriteableCommandLine();
        testOption = new MockOption("test", "test option");
    }

    // Tests adding an option to the command line
    @Test
    public void testAddOption_validOption_optionStoredSuccessfully() {
        commandLine.addOption(testOption);
        assertTrue(commandLine.hasOption(testOption));
    }

    // Tests adding a single value to an option
    @Test
    public void testAddValue_singleValue_valueRetrieved() {
        commandLine.addOption(testOption);
        commandLine.addValue(testOption, "value1");

        assertEquals("value1", commandLine.getValue(testOption));
    }

    // Tests adding multiple values to an option
    @Test
    public void testAddValue_multipleValues_allValuesRetrieved() {
        commandLine.addOption(testOption);
        commandLine.addValue(testOption, "value1");
        commandLine.addValue(testOption, "value2");

        List values = commandLine.getValues(testOption);
        assertEquals(2, values.size());
        assertEquals("value1", values.get(0));
        assertEquals("value2", values.get(1));
    }

    // Tests getting undefaulted values when none are added
    @Test
    public void testGetUndefaultedValues_noValues_returnsEmptyList() {
        List values = commandLine.getUndefaultedValues(testOption);
        assertNotNull(values);
        assertTrue(values.isEmpty());
    }

    // Tests getting undefaulted values when explicit values are present
    @Test
    public void testGetUndefaultedValues_withExplicitValues_returnsOnlyUndefaulted() {
        commandLine.addOption(testOption);
        commandLine.addValue(testOption, "explicitValue");

        List defaultList = new ArrayList();
        defaultList.add("defaultValue");
        commandLine.setDefaultValues(testOption, defaultList);

        List undefaulted = commandLine.getUndefaultedValues(testOption);
        assertEquals(1, undefaulted.size());
        assertEquals("explicitValue", undefaulted.get(0));
    }

    // Tests setting default values for an option
    @Test
    public void testSetDefaultValues_defaultSupplied_returnsDefaultWhenNoExplicitValue() {
        List defaultList = new ArrayList();
        defaultList.add("defaultVal");
        commandLine.setDefaultValues(testOption, defaultList);

        assertEquals("defaultVal", commandLine.getValue(testOption));
    }

    // Tests setting default values with null
    @Test
    public void testSetDefaultValues_nullDefaults_handlesGracefully() {
        commandLine.setDefaultValues(testOption, null);
        assertNull(commandLine.getValue(testOption));
    }

    // Tests adding a boolean switch set to true
    @Test
    public void testAddSwitch_trueValue_returnsTrue() {
        commandLine.addSwitch(testOption, true);
        assertEquals(Boolean.TRUE, commandLine.getSwitch(testOption));
    }

    // Tests adding a boolean switch set to false
    @Test
    public void testAddSwitch_falseValue_returnsFalse() {
        commandLine.addSwitch(testOption, false);
        assertEquals(Boolean.FALSE, commandLine.getSwitch(testOption));
    }

    // Tests that adding a duplicate switch throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testAddSwitch_alreadyAdded_throwsIllegalStateException() {
        commandLine.addSwitch(testOption, true);
        commandLine.addSwitch(testOption, false);
    }

    // Tests setting default switch state
    @Test
    public void testSetDefaultSwitch_validBoolean_returnsDefaultWhenNoSwitchAdded() {
        commandLine.setDefaultSwitch(testOption, Boolean.TRUE);
        assertEquals(Boolean.TRUE, commandLine.getSwitch(testOption));
    }

    // Tests adding property with option
    @Test
    public void testAddProperty_withOption_propertyRetrieved() {
        commandLine.addProperty(testOption, "key1", "val1");
        assertEquals("val1", commandLine.getProperty(testOption, "key1"));
    }

    // Tests adding property without option (default property set)
    @Test
    public void testAddProperty_withoutOption_propertyRetrieved() {
        commandLine.addProperty("key2", "val2");
        assertEquals("val2", commandLine.getProperty("key2"));
    }

    // Tests replacing property value
    @Test
    public void testAddProperty_existingKey_replacesValue() {
        commandLine.addProperty("key", "val1");
        commandLine.addProperty("key", "val2");
        assertEquals("val2", commandLine.getProperty("key"));
    }

    // Tests looksLikeOption when argument matches option prefix
    @Test
    public void testLooksLikeOption_matchingPrefix_returnsTrue() {
        assertTrue(commandLine.looksLikeOption("--test"));
    }

    // Tests looksLikeOption when argument does not match option prefix
    @Test
    public void testLooksLikeOption_nonMatchingPrefix_returnsFalse() {
        assertFalse(commandLine.looksLikeOption("regularArgument"));
    }

    /**
     * Test mock implementation of WriteableCommandLine.
     */
    private static class MockWriteableCommandLine implements WriteableCommandLine {
        private final Set options = new HashSet();
        private final Map values = new HashMap();
        private final Map defaultValues = new HashMap();
        private final Map switches = new HashMap();
        private final Map defaultSwitches = new HashMap();
        private final Map properties = new HashMap();
        private final Map defaultProperties = new HashMap();

        public void addOption(Option option) {
            options.add(option);
        }

        public void addValue(Option option, Object value) {
            List list = (List) values.get(option);
            if (list == null) {
                list = new ArrayList();
                values.put(option, list);
            }
            list.add(value);
        }

        public List getUndefaultedValues(Option option) {
            List list = (List) values.get(option);
            return list != null ? list : Collections.EMPTY_LIST;
        }

        public void setDefaultValues(Option option, List defaults) {
            defaultValues.put(option, defaults);
        }

        public void addSwitch(Option option, boolean value) throws IllegalStateException {
            if (switches.containsKey(option)) {
                throw new IllegalStateException("Switch already present");
            }
            switches.put(option, Boolean.valueOf(value));
        }

        public void setDefaultSwitch(Option option, Boolean defaultSwitch) {
            defaultSwitches.put(option, defaultSwitch);
        }

        public void addProperty(Option option, String property, String value) {
            Map map = (Map) properties.get(option);
            if (map == null) {
                map = new HashMap();
                properties.put(option, map);
            }
            map.put(property, value);
        }

        public void addProperty(String property, String value) {
            defaultProperties.put(property, value);
        }

        public boolean looksLikeOption(String argument) {
            return argument != null && argument.startsWith("-");
        }

        public boolean hasOption(Option option) {
            return options.contains(option);
        }

        public boolean hasOption(String trigger) {
            return false;
        }

        public Option getOption(String trigger) {
            return null;
        }

        public List getValues(Option option) {
            List valList = (List) values.get(option);
            if (valList != null && !valList.isEmpty()) {
                return valList;
            }
            return (List) defaultValues.get(option);
        }

        public List getValues(Option option, List defaultValuesList) {
            List valList = getValues(option);
            return valList != null ? valList : defaultValuesList;
        }

        public List getValues(String trigger) {
            return Collections.EMPTY_LIST;
        }

        public List getValues(String trigger, List defaultValuesList) {
            return defaultValuesList;
        }

        public Object getValue(Option option) {
            List valList = getValues(option);
            return (valList != null && !valList.isEmpty()) ? valList.get(0) : null;
        }

        public Object getValue(Option option, Object defaultValue) {
            Object val = getValue(option);
            return val != null ? val : defaultValue;
        }

        public Object getValue(String trigger) {
            return null;
        }

        public Object getValue(String trigger, Object defaultValue) {
            return defaultValue;
        }

        public Boolean getSwitch(Option option) {
            Boolean sw = (Boolean) switches.get(option);
            return sw != null ? sw : (Boolean) defaultSwitches.get(option);
        }

        public Boolean getSwitch(Option option, Boolean defaultValue) {
            Boolean sw = getSwitch(option);
            return sw != null ? sw : defaultValue;
        }

        public Boolean getSwitch(String trigger) {
            return null;
        }

        public Boolean getSwitch(String trigger, Boolean defaultValue) {
            return defaultValue;
        }

        public String getProperty(Option option, String property) {
            Map map = (Map) properties.get(option);
            return map != null ? (String) map.get(property) : null;
        }

        public String getProperty(Option option, String property, String defaultValue) {
            String val = getProperty(option, property);
            return val != null ? val : defaultValue;
        }

        public String getProperty(String property) {
            return (String) defaultProperties.get(property);
        }

        public String getProperty(String property, String defaultValue) {
            String val = getProperty(property);
            return val != null ? val : defaultValue;
        }

        public Set getProperties(Option option) {
            Map map = (Map) properties.get(option);
            return map != null ? map.keySet() : Collections.EMPTY_SET;
        }

        public Set getProperties() {
            return defaultProperties.keySet();
        }

        public List getOptions() {
            return new ArrayList(options);
        }

        public Set getOptionTriggers() {
            return Collections.EMPTY_SET;
        }

        public int getOptionCount(Option option) {
            return options.contains(option) ? 1 : 0;
        }

        public int getOptionCount(String trigger) {
            return 0;
        }

        public Iterator iterator() {
            return options.iterator();
        }
    }

    /**
     * Test mock implementation of Option.
     */
    private static class MockOption implements Option {
        private final String preferredName;
        private final String description;

        MockOption(String preferredName, String description) {
            this.preferredName = preferredName;
            this.description = description;
        }

        public boolean canProcess(WriteableCommandLine commandLine, String argument) {
            return false;
        }

        public boolean canProcess(WriteableCommandLine commandLine, ListIterator arguments) {
            return false;
        }

        public void process(WriteableCommandLine commandLine, ListIterator arguments) throws OptionException {
        }

        public void validate(WriteableCommandLine commandLine) throws OptionException {
        }

        public void appendUsage(StringBuffer buffer, Set helpSettings, java.util.Comparator comp) {
        }

        public String getPreferredName() {
            return preferredName;
        }

        public String getDescription() {
            return description;
        }

        public String getHelpPage() {
            return null;
        }

        public Set getHelpSettings() {
            return Collections.EMPTY_SET;
        }

        public Set getTriggers() {
            return Collections.singleton(preferredName);
        }

        public Set getPrefixes() {
            return Collections.singleton("-");
        }

        public boolean isRequired() {
            return false;
        }

        public void checkPrefixes(Set prefixes) {
        }

        public Option findOption(String trigger) {
            return preferredName.equals(trigger) ? this : null;
        }

        public int getId() {
            return 0;
        }
    }
}