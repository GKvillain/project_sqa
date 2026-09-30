package org.apache.commons.cli2.commandline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.WriteableCommandLine;
import org.apache.commons.cli2.option.PropertyOption;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit 4 test class for WriteableCommandLineImpl.
 * Targets Defects4J bug 15b in Commons CLI2.
 */
public class WriteableCommandLineImplTest {

    private WriteableCommandLineImpl cmd;
    private final List<String> args = new ArrayList<String>(Arrays.asList("arg1", "arg2"));
    private final Option rootOption = new PropertyOption();

    @Before
    public void setUp() {
        cmd = new WriteableCommandLineImpl(rootOption, args);
    }

    // --- helper stubs ---
    private static class TestOption implements Option {
        private final String preferredName;
        private final Set<String> triggers;
        private final Set<String> prefixes;

        TestOption(String preferredName, Set<String> triggers, Set<String> prefixes) {
            this.preferredName = preferredName;
            this.triggers = triggers;
            this.prefixes = prefixes;
        }

        @Override
        public Set getPrefixes() { return prefixes; }
        @Override
        public Set getTriggers() { return triggers; }
        @Override
        public String getPreferredName() { return preferredName; }
        @Override
        public boolean canProcess(WriteableCommandLine commandLine, String arg) { throw new UnsupportedOperationException(); }
        @Override
        public void process(WriteableCommandLine commandLine, List arguments) { throw new UnsupportedOperationException(); }
        @Override
        public void defaults(WriteableCommandLine commandLine) { throw new UnsupportedOperationException(); }
        @Override
        public String getDescription() { throw new UnsupportedOperationException(); }
        @Override
        public boolean isRequired() { throw new UnsupportedOperationException(); }
    }

    private static class TestArgument implements Argument {
        private final String preferredName;
        private final Set<String> triggers;
        private final Set<String> prefixes;

        TestArgument(String preferredName, Set<String> triggers, Set<String> prefixes) {
            this.preferredName = preferredName;
            this.triggers = triggers;
            this.prefixes = prefixes;
        }

        @Override public Set getPrefixes() { return prefixes; }
        @Override public Set getTriggers() { return triggers; }
        @Override public String getPreferredName() { return preferredName; }
        @Override public boolean canProcess(WriteableCommandLine commandLine, String arg) { throw new UnsupportedOperationException(); }
        @Override public void process(WriteableCommandLine commandLine, List arguments) { throw new UnsupportedOperationException(); }
        @Override public void defaults(WriteableCommandLine commandLine) { throw new UnsupportedOperationException(); }
        @Override public String getDescription() { throw new UnsupportedOperationException(); }
        @Override public boolean isRequired() { throw new UnsupportedOperationException(); }
        @Override public List getDefaultValues() { return Collections.emptyList(); }
        @Override public String getInitialSeparator() { return "="; }
        @Override public String getSubsequentSeparator() { return "="; }
        @Override public boolean handleSplit(WriteableCommandLine commandLine) { return false; }
    }

    // --- tests ---

    @Test
    public void testConstructor_storesNormalisedArguments_returnsSameList() {
        List<String> normalised = cmd.getNormalised();
        assertEquals(args.size(), normalised.size());
        assertEquals(args.get(0), normalised.get(0));
        assertEquals(args.get(1), normalised.get(1));
    }

    @Test
    public void testConstructor_storesRootPrefixes_looksLikeOptionWorks() {
        // PropertyOption prefixes? likely empty set, but we test behavior
        Set prefixes = rootOption.getPrefixes();
        if (prefixes.isEmpty()) {
            assertFalse(cmd.looksLikeOption("--foo"));
        } else {
            String prefix = (String) prefixes.iterator().next();
            assertTrue(cmd.looksLikeOption(prefix + "foo"));
        }
    }

    @Test
    public void testAddOption_addedOption_hasOptionTrue() {
        Option opt = new TestOption("foo", new HashSet<String>(Arrays.asList("foo", "f")), new HashSet<String>());
        cmd.addOption(opt);
        assertTrue(cmd.hasOption(opt));
    }

    @Test
    public void testAddOption_getOptionByTrigger_returnsOption() {
        Option opt = new TestOption("bar", new HashSet<String>(Arrays.asList("bar", "b")), new HashSet<String>());
        cmd.addOption(opt);
        assertSame(opt, cmd.getOption("bar"));
        assertSame(opt, cmd.getOption("b"));
    }

    @Test
    public void testAddValue_nonArgumentOption_noOptionAdded() {
        Option opt = new TestOption("val", new HashSet<String>(Arrays.asList("val")), new HashSet<String>());
        cmd.addValue(opt, "value1");
        assertFalse(cmd.hasOption(opt));
        // value stored
        List values = cmd.getValues(opt, null);
        assertEquals(1, values.size());
        assertEquals("value1", values.get(0));
    }

    @Test
    public void testAddValue_argumentOption_optionAddedAndValueStored() {
        Option arg = new TestArgument("arg", new HashSet<String>(Arrays.asList("arg")), new HashSet<String>());
        cmd.addValue(arg, "argValue");
        assertTrue(cmd.hasOption(arg));
        List values = cmd.getValues(arg, null);
        assertEquals(1, values.size());
        assertEquals("argValue", values.get(0));
    }

    @Test
    public void testGetValues_valuesExist_returnsValues() {
        Option opt = new TestOption("opt", new HashSet<String>(Arrays.asList("opt")), new HashSet<String>());
        cmd.addValue(opt, "a");
        cmd.addValue(opt, "b");
        List result = cmd.getValues(opt, Collections.singletonList("default"));
        assertEquals(2, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
    }

    @Test
    public void testGetValues_noValuesDefaultParamNotNull_returnsDefaultParam() {
        Option opt = new TestOption("opt", new HashSet<String>(Arrays.asList("opt")), new HashSet<String>());
        List defaultParam = Arrays.asList("d1", "d2");
        List result = cmd.getValues(opt, defaultParam);
        assertEquals(defaultParam, result);
    }

    @Test
    public void testGetValues_noValuesDefaultParamNullStoredDefaultNotNull_returnsStoredDefault() {
        Option opt = new TestOption("opt", new HashSet<String>(Arrays.asList("opt")), new HashSet<String>());
        List stored = Arrays.asList("s1", "s2");
        cmd.setDefaultValues(opt, stored);
        List result = cmd.getValues(opt, null);
        assertEquals(stored, result);
    }

    @Test
    public void testGetValues_allNull_returnsEmptyList() {
        Option opt = new TestOption("opt", new HashSet<String>(Arrays.asList("opt")), new HashSet<String>());
        List result = cmd.getValues(opt, null);
        assertEquals(Collections.EMPTY_LIST, result);
    }

    @Test
    public void testGetValues_emptyValuesDefaultParamNotNull_returnsDefaultParam() {
        Option opt = new TestOption("opt", new HashSet<String>(Arrays.asList("opt")), new HashSet<String>());
        // values map has no entry for opt -> getValues will use defaultParam
        List defaultParam = Arrays.asList("d1");
        List result = cmd.getValues(opt, defaultParam);
        assertEquals(defaultParam, result);
    }

    @Test
    public void testAddSwitch_addSwitchTrue_getSwitchReturnsTrue() {
        Option opt = new TestOption("sw", new HashSet<String>(Arrays.asList("sw")), new HashSet<String>());
        cmd.addSwitch(opt, true);
        assertEquals(Boolean.TRUE, cmd.getSwitch(opt, null));
    }

    @Test(expected = IllegalStateException.class)
    public void testAddSwitch_duplicate_throwsIllegalStateException() {
        Option opt = new TestOption("sw2", new HashSet<String>(Arrays.asList("sw2")), new HashSet<String>());
        cmd.addSwitch(opt, true);
        cmd.addSwitch(opt, false);
    }

    @Test
    public void testGetSwitch_noSwitchDefaultParamNotNull_returnsDefaultParam() {
        Option opt = new TestOption("sw3", new HashSet<String>(Arrays.asList("sw3")), new HashSet<String>());
        Boolean result = cmd.getSwitch(opt, Boolean.TRUE);
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testGetSwitch_noSwitchDefaultParamNullStoredDefaultNotNull_returnsStoredDefault() {
        Option opt = new TestOption("sw4", new HashSet<String>(Arrays.asList("sw4")), new HashSet<String>());
        cmd.setDefaultSwitch(opt, Boolean.FALSE);
        Boolean result = cmd.getSwitch(opt, null);
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testGetSwitch_noSwitchAllDefaultsNull_returnsNull() {
        Option opt = new TestOption("sw5", new HashSet<String>(Arrays.asList("sw5")), new HashSet<String>());
        Boolean result = cmd.getSwitch(opt, null);
        assertNull(result);
    }

    @Test
    public void testSetDefaultValues_thenGetValues_returnsStoredDefaults() {
        Option opt = new TestOption("dv", new HashSet<String>(Arrays.asList("dv")), new HashSet<String>());
        List defaults = Arrays.asList("x", "y");
        cmd.setDefaultValues(opt, defaults);
        List result = cmd.getValues(opt, null);
        assertEquals(defaults, result);
    }

    @Test
    public void testSetDefaultSwitch_thenGetSwitch_returnsStoredDefault() {
        Option opt = new TestOption("ds", new HashSet<String>(Arrays.asList("ds")), new HashSet<String>());
        cmd.setDefaultSwitch(opt, Boolean.TRUE);
        assertEquals(Boolean.TRUE, cmd.getSwitch(opt, null));
    }

    @Test
    public void testAddProperty_thenGetProperty_returnsProperty() {
        PropertyOption propOpt = new PropertyOption();
        cmd.addProperty(propOpt, "key1", "value1");
        assertEquals("value1", cmd.getProperty(propOpt, "key1", "default"));
    }

    @Test
    public void testGetProperty_noProperty_returnsDefaultValue() {
        PropertyOption propOpt = new PropertyOption();
        assertEquals("default", cmd.getProperty(propOpt, "noKey", "default"));
    }

    @Test
    public void testGetProperty_noKeyNoDefault_returnsNull() {
        PropertyOption propOpt = new PropertyOption();
        assertNull(cmd.getProperty(propOpt, "noKey")); // overload without default
    }

    @Test
    public void testGetProperties_empty_returnsEmptySet() {
        PropertyOption propOpt = new PropertyOption();
        Set keys = cmd.getProperties(propOpt);
        assertEquals(Collections.EMPTY_SET, keys);
    }

    @Test
    public void testGetProperties_nonEmpty_returnsKeySet() {
        PropertyOption propOpt = new PropertyOption();
        cmd.addProperty(propOpt, "k1", "v1");
        cmd.addProperty(propOpt, "k2", "v2");
        Set keys = cmd.getProperties(propOpt);
        assertEquals(2, keys.size());
        assertTrue(keys.contains("k1"));
        assertTrue(keys.contains("k2"));
    }

    @Test
    public void testToString_argumentsWithSpace_quotesThem() {
        List<String> spacedArgs = new ArrayList<String>(Arrays.asList("a b", "c"));
        WriteableCommandLineImpl cmd2 = new WriteableCommandLineImpl(rootOption, spacedArgs);
        assertEquals("\"a b\" c", cmd2.toString());
    }

    @Test
    public void testLooksLikeOption_matchingPrefix_returnsTrue() {
        // Use rootOption that gives prefix "-"
        Option rootWithPrefix = new TestOption("r", new HashSet<String>(), new HashSet<String>(Arrays.asList("-")));
        WriteableCommandLineImpl cmd2 = new WriteableCommandLineImpl(rootWithPrefix, args);
        assertTrue(cmd2.looksLikeOption("-v"));
    }

    @Test
    public void testLooksLikeOption_nonMatchingPrefix_returnsFalse() {
        Option rootWithPrefix = new TestOption("r", new HashSet<String>(), new HashSet<String>(Arrays.asList("--")));
        WriteableCommandLineImpl cmd2 = new WriteableCommandLineImpl(rootWithPrefix, args);
        assertFalse(cmd2.looksLikeOption("-v"));
    }

    @Test
    public void testGetUndefaultedValues_valuesExist_returnsValues() {
        Option opt = new TestOption("u1", new HashSet<String>(Arrays.asList("u1")), new HashSet<String>());
        cmd.addValue(opt, "only");
        List result = cmd.getUndefaultedValues(opt);
        assertEquals(1, result.size());
        assertEquals("only", result.get(0));
    }

    @Test
    public void testGetUndefaultedValues_noValues_returnsEmptyList() {
        Option opt = new TestOption("u2", new HashSet<String>(Arrays.asList("u2")), new HashSet<String>());
        List result = cmd.getUndefaultedValues(opt);
        assertEquals(Collections.EMPTY_LIST, result);
    }

    @Test
    public void testGetUndefaultedValues_withDefaultValuesNoActual_returnsEmptyList() {
        Option opt = new TestOption("u3", new HashSet<String>(Arrays.asList("u3")), new HashSet<String>());
        cmd.setDefaultValues(opt, Arrays.asList("default1", "default2"));
        // no addValue, so undefaulted values should be empty
        List result = cmd.getUndefaultedValues(opt);
        assertEquals(Collections.EMPTY_LIST, result);
    }

    @Test
    public void testGetUndefaultedValues_withDefaultValuesAndActual_returnsActualValues() {
        Option opt = new TestOption("u4", new HashSet<String>(Arrays.asList("u4")), new HashSet<String>());
        cmd.setDefaultValues(opt, Arrays.asList("default1"));
        cmd.addValue(opt, "actual");
        List result = cmd.getUndefaultedValues(opt);
        assertEquals(1, result.size());
        assertEquals("actual", result.get(0));
    }

    @Test
    public void testGetOptions_afterAddOption_containsOption() {
        Option opt = new TestOption("list", new HashSet<String>(Arrays.asList("list")), new HashSet<String>());
        cmd.addOption(opt);
        List opts = cmd.getOptions();
        assertTrue(opts.contains(opt));
    }

    @Test
    public void testGetOptions_empty_returnsEmptyList() {
        List opts = cmd.getOptions();
        assertTrue(opts.isEmpty());
    }

    @Test
    public void testGetOptionTriggers_afterAddOption_containsTrigger() {
        Option opt = new TestOption("trig", new HashSet<String>(Arrays.asList("trig", "t")), new HashSet<String>());
        cmd.addOption(opt);
        Set triggers = cmd.getOptionTriggers();
        assertTrue(triggers.contains("trig"));
        assertTrue(triggers.contains("t"));
    }

    @Test
    public void testGetOptionTriggers_empty_returnsEmptySet() {
        Set triggers = cmd.getOptionTriggers();
        assertTrue(triggers.isEmpty());
    }

    @Test
    public void testGetOption_notFound_returnsNull() {
        Option opt = new TestOption("notfound", new HashSet<String>(Arrays.asList("nf")), new HashSet<String>());
        cmd.addOption(opt);
        // query with a trigger that does not exist
        assertNull(cmd.getOption("nonexistent"));
    }

    @Test
    public void testHasOption_notAdded_returnsFalse() {
        Option opt = new TestOption("notadded", new HashSet<String>(Arrays.asList("na")), new HashSet<String>());
        assertFalse(cmd.hasOption(opt));
    }

    @Test
    public void testGetValues_withDefaultValuesAndActualValues_returnsActualValues() {
        Option opt = new TestOption("act", new HashSet<String>(Arrays.asList("act")), new HashSet<String>());
        cmd.setDefaultValues(opt, Arrays.asList("default1", "default2"));
        cmd.addValue(opt, "actual1");
        cmd.addValue(opt, "actual2");
        List result = cmd.getValues(opt, Collections.singletonList("shouldNotBeUsed"));
        assertEquals(2, result.size());
        assertEquals("actual1", result.get(0));
        assertEquals("actual2", result.get(1));
    }
}