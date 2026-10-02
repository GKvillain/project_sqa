package org.apache.commons.cli2.commandline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.WriteableCommandLine;
import org.apache.commons.cli2.option.PropertyOption;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
        commandLine = new WriteableCommandLineImpl(rootOption, arguments);
    }

    // Tests adding an option and checking triggers, preferred name, and containment
    @Test
    public void testAddOption_simpleOption_storedAndRetrievable() {
        DummyOption opt = new DummyOption("opt", Collections.singleton("-o"), null);
        commandLine.addOption(opt);

        assertTrue(commandLine.hasOption(opt));
        assertEquals(opt, commandLine.getOption("opt"));
        assertEquals(opt, commandLine.getOption("-o"));
        assertEquals(1, commandLine.getOptions().size());
        assertTrue(commandLine.getOptionTriggers().contains("opt"));
        assertTrue(commandLine.getOptionTriggers().contains("-o"));
    }

    // Tests adding an option with parent hierarchy to verify parent options are added
    @Test
    public void testAddOption_withParent_addsParentOptions() {
        DummyOption parent = new DummyOption("parent", Collections.singleton("--parent"), null);
        DummyOption child = new DummyOption("child", Collections.singleton("--child"), parent);

        commandLine.addOption(child);

        assertTrue(commandLine.hasOption(child));
        assertTrue(commandLine.hasOption(parent));
        assertEquals(2, commandLine.getOptions().size());
    }

    // Tests adding and retrieving values without defaults
    @Test
    public void testAddValue_andGetUndefaultedValues_returnsAddedValues() {
        DummyOption opt = new DummyOption("opt", Collections.singleton("-o"), null);
        commandLine.addValue(opt, "val1");
        commandLine.addValue(opt, "val2");

        List values = commandLine.getUndefaultedValues(opt);
        assertEquals(2, values.size());
        assertEquals("val1", values.get(0));
        assertEquals("val2", values.get(1));
    }

    // Tests getUndefaultedValues returns empty list for unknown option
    @Test
    public void testGetUndefaultedValues_unknownOption_returnsEmptyList() {
        DummyOption opt = new DummyOption("opt", Collections.singleton("-o"), null);
        List values = commandLine.getUndefaultedValues(opt);
        assertNotNull(values);
        assertTrue(values.isEmpty());
    }

    // Tests addValue automatically adds the option when option is an Argument instance
    @Test
    public void testAddValue_argumentOption_automaticallyAddsOption() {
        DummyArgument arg = new DummyArgument("arg", Collections.singleton("arg"), null);
        commandLine.addValue(arg, "argValue");

        assertTrue(commandLine.hasOption(arg));
        assertEquals(1, commandLine.getValues(arg, null).size());
        assertEquals("argValue", commandLine.getValues(arg, null).get(0));
    }

    // Tests getValues with command line default values fallback
    @Test
    public void testGetValues_noValuesPresent_usesDefaultValues() {
        DummyOption opt = new DummyOption("opt", Collections.singleton("-o"), null);
        List defaults = Arrays.asList(new Object[]{"def1", "def2"});
        commandLine.setDefaultValues(opt, defaults);

        List values = commandLine.getValues(opt, null);
        assertEquals(defaults, values);
    }

    // Tests getValues augmenting existing values when defaults size exceeds existing values
    @Test
    public void testGetValues_defaultsExceedValues_augmentsList() {
        DummyOption opt = new DummyOption("opt", Collections.singleton("-o"), null);
        commandLine.addValue(opt, "val1");

        List passedDefaults = Arrays.asList(new Object[]{"def1", "def2", "def3"});
        List values = commandLine.getValues(opt, passedDefaults);

        assertEquals(3, values.size());
        assertEquals("val1", values.get(0));
        assertEquals("def2", values.get(1));
        assertEquals("def3", values.get(2));
    }

    // Tests getValues returns empty list when no values and no defaults exist
    @Test
    public void testGetValues_noValuesAndNoDefaults_returnsEmptyList() {
        DummyOption opt = new DummyOption("opt", Collections.singleton("-o"), null);
        List values = commandLine.getValues(opt, null);
        assertNotNull(values);
        assertTrue(values.isEmpty());
    }

    // Tests removing default values by passing null to setDefaultValues
    @Test
    public void testSetDefaultValues_nullRemovesDefaults() {
        DummyOption opt = new DummyOption("opt", Collections.singleton("-o"), null);
        commandLine.setDefaultValues(opt, Collections.singletonList("def"));
        commandLine.setDefaultValues(opt, null);

        List values = commandLine.getValues(opt, null);
        assertTrue(values.isEmpty());
    }

    // Tests addSwitch and getSwitch normal flow
    @Test
    public void testAddSwitch_andGetSwitch_returnsCorrectBoolean() {
        DummyOption opt = new DummyOption("opt", Collections.singleton("-s"), null);
        commandLine.addSwitch(opt, true);

        assertTrue(commandLine.hasOption(opt));
        assertEquals(Boolean.TRUE, commandLine.getSwitch(opt, null));
    }

    // Tests addSwitch throws IllegalStateException when switch is already added
    @Test(expected = IllegalStateException.class)
    public void testAddSwitch_duplicateSwitch_throwsIllegalStateException() {
        DummyOption opt = new DummyOption("opt", Collections.singleton("-s"), null);
        commandLine.addSwitch(opt, true);
        commandLine.addSwitch(opt, false);
    }

    // Tests getSwitch falling back to parameter default and configured default
    @Test
    public void testGetSwitch_fallbacks_returnsExpectedValues() {
        DummyOption opt = new DummyOption("opt", Collections.singleton("-s"), null);

        assertEquals(Boolean.TRUE, commandLine.getSwitch(opt, Boolean.TRUE));

        commandLine.setDefaultSwitch(opt, Boolean.FALSE);
        assertEquals(Boolean.FALSE, commandLine.getSwitch(opt, null));

        commandLine.setDefaultSwitch(opt, null);
        assertNull(commandLine.getSwitch(opt, null));
    }

    // Tests adding and retrieving properties using default PropertyOption
    @Test
    public void testProperty_defaultOption_storedAndRetrieved() {
        commandLine.addProperty("key1", "val1");
        assertEquals("val1", commandLine.getProperty("key1"));
        assertEquals(1, commandLine.getProperties().size());
        assertTrue(commandLine.getProperties().contains("key1"));
    }

    // Tests adding and retrieving properties using specific Option and default values
    @Test
    public void testProperty_specificOption_storedAndRetrieved() {
        DummyOption opt = new DummyOption("opt", Collections.singleton("-p"), null);
        commandLine.addProperty(opt, "k1", "v1");

        assertEquals("v1", commandLine.getProperty(opt, "k1", "fallback"));
        assertEquals("fallback", commandLine.getProperty(opt, "k2", "fallback"));
        assertEquals(1, commandLine.getProperties(opt).size());
    }

    // Tests getProperties returns empty set when no property exists for option
    @Test
    public void testGetProperties_unknownOption_returnsEmptySet() {
        DummyOption opt = new DummyOption("opt", Collections.singleton("-p"), null);
        Set props = commandLine.getProperties(opt);
        assertNotNull(props);
        assertTrue(props.isEmpty());
        assertNull(commandLine.getProperty(opt, "k", null));
    }

    // Tests looksLikeOption with matching prefix and non-matching prefix
    @Test
    public void testLooksLikeOption_matchingAndNonMatchingPrefix() {
        Set prefixes = new HashSet();
        prefixes.add("-");
        prefixes.add("--");
        DummyOption root = new DummyOption("root", prefixes, prefixes, null);
        WriteableCommandLineImpl cl = new WriteableCommandLineImpl(root, new ArrayList());

        assertTrue(cl.looksLikeOption("-opt"));
        assertTrue(cl.looksLikeOption("--option"));
        assertFalse(cl.looksLikeOption("value"));
    }

    // Tests toString formatting with single arguments and arguments containing spaces
    @Test
    public void testToString_argumentsWithAndWithoutSpaces() {
        List args = new ArrayList();
        args.add("-a");
        args.add("hello world");
        args.add("test");

        WriteableCommandLineImpl cl = new WriteableCommandLineImpl(rootOption, args);
        assertEquals("-a \"hello world\" test", cl.toString());
        assertEquals(3, cl.getNormalised().size());
    }

    // Dummy Option implementation for testing
    private static class DummyOption implements Option {
        private final String preferredName;
        private final Set triggers;
        private final Set prefixes;
        private final Option parent;

        DummyOption(String preferredName, Set triggers, Option parent) {
            this(preferredName, triggers, Collections.EMPTY_SET, parent);
        }

        DummyOption(String preferredName, Set triggers, Set prefixes, Option parent) {
            this.preferredName = preferredName;
            this.triggers = triggers != null ? triggers : Collections.EMPTY_SET;
            this.prefixes = prefixes != null ? prefixes : Collections.EMPTY_SET;
            this.parent = parent;
        }

        public boolean canProcess(WriteableCommandLine commandLine, String arg) { return false; }
        public boolean canProcess(WriteableCommandLine commandLine, ListIterator args) { return false; }
        public void process(WriteableCommandLine commandLine, ListIterator args) {}
        public void validate(WriteableCommandLine commandLine) {}
        public void appendUsage(StringBuffer buffer, Set helpSettings, Comparator comp) {}
        public String getDescription() { return preferredName; }
        public Option getParent() { return parent; }
        public void setParent(Option parent) {}
        public String getPreferredName() { return preferredName; }
        public Set getPrefixes() { return prefixes; }
        public Set getTriggers() { return triggers; }
        public int getId() { return 0; }
        public boolean isRequired() { return false; }
        public Option findOption(String trigger) { return triggers.contains(trigger) ? this : null; }
        public void defaults(WriteableCommandLine commandLine) {}
        public boolean checkPrefixes(Set prefixes) { return true; }
    }

    // Dummy Argument implementation extending DummyOption and implementing Argument
    private static class DummyArgument extends DummyOption implements Argument {
        DummyArgument(String preferredName, Set triggers, Option parent) {
            super(preferredName, triggers, parent);
        }

        public String getInitialSeparator() { return null; }
        public char getInitialSeparatorChar() { return '\0'; }
        public int getMaximum() { return Integer.MAX_VALUE; }
        public int getMinimum() { return 0; }
        public Set getSubDelimiters() { return Collections.EMPTY_SET; }
        public boolean isInitialSeparator(char ch) { return false; }
        public boolean isSubDelimiter(char ch) { return false; }
        public void processValues(WriteableCommandLine commandLine, ListIterator args, Option option) {}
        public String stripInitialSeparator(String token) { return token; }
    }
}