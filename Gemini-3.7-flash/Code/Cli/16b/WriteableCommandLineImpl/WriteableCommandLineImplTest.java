package org.apache.commons.cli2.commandline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.option.PropertyOption;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class WriteableCommandLineImplTest {

    private PropertyOption rootOption;
    private List arguments;
    private WriteableCommandLineImpl commandLine;

    @Before
    public void setUp() {
        rootOption = new PropertyOption();
        arguments = new ArrayList();
        arguments.add("-Dkey=value");
        arguments.add("arg with spaces");
        arguments.add("simpleArg");
        commandLine = new WriteableCommandLineImpl(rootOption, arguments);
    }

    // Tests adding option and verifying presence and triggers
    @Test
    public void testAddOption_validOption_registersOptionAndTriggers() {
        PropertyOption option = new PropertyOption();
        commandLine.addOption(option);

        assertTrue(commandLine.hasOption(option));
        assertTrue(commandLine.getOptions().contains(option));
        assertEquals(option, commandLine.getOption(option.getPreferredName()));
        assertTrue(commandLine.getOptionTriggers().contains(option.getPreferredName()));
    }

    // Tests hasOption for absent option
    @Test
    public void testHasOption_absentOption_returnsFalse() {
        PropertyOption option = new PropertyOption();
        assertFalse(commandLine.hasOption(option));
        assertNull(commandLine.getOption("unknown"));
    }

    // Tests addValue and getValues for normal Option
    @Test
    public void testAddValue_normalOption_storesAndRetrievesValues() {
        PropertyOption option = new PropertyOption();
        commandLine.addValue(option, "val1");
        commandLine.addValue(option, "val2");

        List expected = Arrays.asList(new Object[]{"val1", "val2"});
        assertEquals(expected, commandLine.getValues(option, null));
        assertEquals(expected, commandLine.getUndefaultedValues(option));
    }

    // Tests addValue for Argument instance triggers addOption
    @Test
    public void testAddValue_argumentOption_automaticallyAddsOption() {
        Argument argument = new FakeArgument();
        commandLine.addValue(argument, "argVal");

        assertTrue(commandLine.hasOption(argument));
        assertEquals(Collections.singletonList("argVal"), commandLine.getValues(argument, null));
    }

    // Tests getValues when no values are added returns empty list or default values
    @Test
    public void testGetValues_noValuesPresent_returnsDefaultsOrEmptyList() {
        PropertyOption option = new PropertyOption();
        assertEquals(Collections.EMPTY_LIST, commandLine.getValues(option, null));
        assertEquals(Collections.EMPTY_LIST, commandLine.getUndefaultedValues(option));

        List defaults = Arrays.asList(new Object[]{"default1", "default2"});
        assertEquals(defaults, commandLine.getValues(option, defaults));

        commandLine.setDefaultValues(option, defaults);
        assertEquals(defaults, commandLine.getValues(option, null));

        commandLine.setDefaultValues(option, null);
        assertEquals(Collections.EMPTY_LIST, commandLine.getValues(option, null));
    }

    // Tests getValues when default values list has more items than specified values
    @Test
    public void testGetValues_moreDefaultsThanValues_augmentsValueList() {
        PropertyOption option = new PropertyOption();
        commandLine.addValue(option, "val1");

        List defaults = Arrays.asList(new Object[]{"def1", "def2", "def3"});
        List result = commandLine.getValues(option, defaults);

        List expected = Arrays.asList(new Object[]{"val1", "def2", "def3"});
        assertEquals(expected, result);
    }

    // Tests addSwitch and getSwitch normal behavior
    @Test
    public void testAddSwitch_validSwitch_storesAndRetrievesBoolean() {
        PropertyOption option = new PropertyOption();
        commandLine.addSwitch(option, true);

        assertTrue(commandLine.hasOption(option));
        assertEquals(Boolean.TRUE, commandLine.getSwitch(option, null));
    }

    // Tests addSwitch duplicate switch throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testAddSwitch_duplicateSwitch_throwsIllegalStateException() {
        PropertyOption option = new PropertyOption();
        commandLine.addSwitch(option, true);
        commandLine.addSwitch(option, false);
    }

    // Tests getSwitch fallbacks to method default and option default switch
    @Test
    public void testGetSwitch_absentSwitch_fallsBackToDefaults() {
        PropertyOption option = new PropertyOption();
        assertNull(commandLine.getSwitch(option, null));
        assertEquals(Boolean.TRUE, commandLine.getSwitch(option, Boolean.TRUE));

        commandLine.setDefaultSwitch(option, Boolean.FALSE);
        assertEquals(Boolean.FALSE, commandLine.getSwitch(option, null));

        commandLine.setDefaultSwitch(option, null);
        assertNull(commandLine.getSwitch(option, null));
    }

    // Tests addProperty and getProperty with default PropertyOption
    @Test
    public void testProperty_defaultPropertyOption_storesAndRetrievesProperty() {
        commandLine.addProperty("foo", "bar");

        assertEquals("bar", commandLine.getProperty("foo"));
        assertTrue(commandLine.getProperties().contains("foo"));
    }

    // Tests addProperty and getProperty with specific Option and fallback defaultValue
    @Test
    public void testProperty_customOption_storesAndRetrievesProperty() {
        PropertyOption option = new PropertyOption();
        assertNull(commandLine.getProperty(option, "key", null));
        assertEquals("defaultVal", commandLine.getProperty(option, "key", "defaultVal"));
        assertEquals(Collections.EMPTY_SET, commandLine.getProperties(option));

        commandLine.addProperty(option, "key", "value");
        assertEquals("value", commandLine.getProperty(option, "key", "defaultVal"));
        assertEquals(new HashSet(Collections.singletonList("key")), commandLine.getProperties(option));
    }

    // Tests looksLikeOption matching prefixes
    @Test
    public void testLooksLikeOption_matchingPrefix_returnsTrue() {
        assertTrue(commandLine.looksLikeOption("-Dproperty=value"));
        assertFalse(commandLine.looksLikeOption("nonOption"));
    }

    // Tests toString formatting with spaces and multiple arguments
    @Test
    public void testToString_multipleArguments_formatsWithQuotesForSpaces() {
        String expected = "-Dkey=value \"arg with spaces\" simpleArg";
        assertEquals(expected, commandLine.toString());
    }

    // Tests toString with empty argument list
    @Test
    public void testToString_emptyArguments_returnsEmptyString() {
        WriteableCommandLineImpl emptyCmd = new WriteableCommandLineImpl(rootOption, Collections.EMPTY_LIST);
        assertEquals("", emptyCmd.toString());
    }

    // Tests getNormalised returns correct arguments
    @Test
    public void testGetNormalised_returnsNormalisedArguments() {
        assertEquals(arguments, commandLine.getNormalised());
    }

    // Fake Argument implementation for testing Argument-specific branch in addValue
    private static class FakeArgument implements Argument {
        public String getPreferredName() {
            return "fakeArgument";
        }

        public String getDescription() {
            return "fake description";
        }

        public Set getPrefixes() {
            return Collections.EMPTY_SET;
        }

        public Set getTriggers() {
            return Collections.EMPTY_SET;
        }

        public void validate(org.apache.commons.cli2.WriteableCommandLine commandLine) {
        }

        public void appendUsage(StringBuffer buffer, Set helpSettings, java.util.Comparator comp) {
        }

        public boolean canProcess(org.apache.commons.cli2.WriteableCommandLine commandLine, String arg) {
            return true;
        }

        public void process(org.apache.commons.cli2.WriteableCommandLine commandLine, java.util.ListIterator args) {
        }

        public boolean isRequired() {
            return false;
        }

        public List helpLines(int depth, Set helpSettings, java.util.Comparator comp) {
            return Collections.EMPTY_LIST;
        }

        public boolean canProcess(org.apache.commons.cli2.WriteableCommandLine commandLine, java.util.ListIterator args) {
            return true;
        }

        public Option findOption(String trigger) {
            return null;
        }

        public void defaultValues(org.apache.commons.cli2.WriteableCommandLine commandLine) {
        }

        public void defaultValues(org.apache.commons.cli2.WriteableCommandLine commandLine, Option option) {
        }

        public void validate(org.apache.commons.cli2.WriteableCommandLine commandLine, Option option) {
        }

        public char getInitialSeparator() {
            return '\0';
        }

        public int getMaximum() {
            return 1;
        }

        public int getMinimum() {
            return 0;
        }

        public List defaultValues(Option option) {
            return Collections.EMPTY_LIST;
        }

        public boolean isStrip() {
            return false;
        }
    }
}