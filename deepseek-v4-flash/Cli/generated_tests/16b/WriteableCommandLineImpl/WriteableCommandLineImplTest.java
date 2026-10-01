package org.apache.commons.cli2.commandline;

import static org.junit.Assert.*;

import java.util.*;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.option.PropertyOption;
import org.junit.Test;

public class WriteableCommandLineImplTest {

    // -------- Helper stubs for Option and Argument ----------
    private static class SimpleOption implements Option {
        private final String preferredName;
        private final Set<String> triggers;
        private final Set<String> prefixes;

        SimpleOption(String preferredName, Set<String> triggers, Set<String> prefixes) {
            this.preferredName = preferredName;
            this.triggers = triggers;
            this.prefixes = prefixes;
        }

        SimpleOption(String preferredName, Set<String> triggers) {
            this(preferredName, triggers, Collections.<String>emptySet());
        }

        @Override
        public String getPreferredName() {
            return preferredName;
        }

        @Override
        public Set getTriggers() {
            return triggers;
        }

        @Override
        public Set getPrefixes() {
            return prefixes;
        }

        // Unused methods - throw by default
        @Override public boolean canProcess(WriteableCommandLine cmdLine, String arg) { throw new UnsupportedOperationException(); }
        @Override public void process(WriteableCommandLine cmdLine, List args) throws Exception { throw new UnsupportedOperationException(); }
        @Override public void process(WriteableCommandLine cmdLine, String arg) throws Exception { throw new UnsupportedOperationException(); }
        @Override public void validate(WriteableCommandLine cmdLine) throws Exception { throw new UnsupportedOperationException(); }
        @Override public void defaults(WriteableCommandLine cmdLine) throws Exception { throw new UnsupportedOperationException(); }
        @Override public boolean isRequired() { throw new UnsupportedOperationException(); }
        @Override public int getId() { throw new UnsupportedOperationException(); }
        @Override public Set<String> getAliases() { throw new UnsupportedOperationException(); }
        @Override public Option getParent() { throw new UnsupportedOperationException(); }
        @Override public List getChildren() { throw new UnsupportedOperationException(); }
        @Override public List getDefaults() { throw new UnsupportedOperationException(); }
        @Override public boolean isCommand() { throw new UnsupportedOperationException(); }
        @Override public boolean isSwitch() { throw new UnsupportedOperationException(); }
        @Override public int getMinimum() { throw new UnsupportedOperationException(); }
        @Override public int getMaximum() { throw new UnsupportedOperationException(); }
        @Override public String getDescription() { throw new UnsupportedOperationException(); }
        @Override public List getArgses() { throw new UnsupportedOperationException(); }
        @Override public int getArgumentCount() { throw new UnsupportedOperationException(); }
        @Override public void setArgumentCount(int argumentCount) { throw new UnsupportedOperationException(); }
        @Override public void addOption(Option option) { throw new UnsupportedOperationException(); }
        @Override public List getTriggers() { return triggers; }
    }

    private static class SimpleArgument extends SimpleOption implements Argument {
        SimpleArgument(String preferredName, Set<String> triggers) {
            super(preferredName, triggers);
        }

        // Argument interface methods - not needed but must be present
        @Override public List getDefaultValues(WriteableCommandLine cmdLine) { throw new UnsupportedOperationException(); }
        @Override public Object getInitialValue(WriteableCommandLine cmdLine) { throw new UnsupportedOperationException(); }
        @Override public void processValues(WriteableCommandLine cmdLine, Option option, List values) throws Exception { throw new UnsupportedOperationException(); }
        @Override public void validateOption(WriteableCommandLine cmdLine, Option option, List values) throws Exception { throw new UnsupportedOperationException(); }
        @Override public boolean isList() { throw new UnsupportedOperationException(); }
        @Override public Set getDefaultPrefixes() { throw new UnsupportedOperationException(); }
        @Override public void setDefaultPrefixes(Set prefixes) { throw new UnsupportedOperationException(); }
        @Override public int getMaximum() { throw new UnsupportedOperationException(); }
        @Override public void setMaximum(int maximum) { throw new UnsupportedOperationException(); }
        @Override public int getMinimum() { throw new UnsupportedOperationException(); }
        @Override public void setMinimum(int minimum) { throw new UnsupportedOperationException(); }
        @Override public Set getPrefixes() { return Collections.emptySet(); }
        @Override public void setPrefixes(Set prefixes) { throw new UnsupportedOperationException(); }
        @Override public Map getPrefixesMap() { throw new UnsupportedOperationException(); }
        @Override public void setPrefixesMap(Map map) { throw new UnsupportedOperationException(); }
    }

    // ---------- Tests ----------

    // Test constructor and looksLikeOption
    @Test
    public void testConstructor_setsPrefixesAndNormalised() {
        Set<String> prefixes = new HashSet<String>();
        prefixes.add("--");
        Option root = new SimpleOption("root", Collections.singleton("--root"), prefixes);
        List<String> args = Arrays.asList("--arg1", "value1");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, args);
        assertTrue(cmd.looksLikeOption("--arg1"));
        assertFalse(cmd.looksLikeOption("-arg1"));
        assertEquals(args, cmd.getNormalised());
    }

    // Test addOption and hasOption
    @Test
    public void testAddOption_optionAdded_hasOptionTrue() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        Option opt = new SimpleOption("--foo", Collections.singleton("--foo"));
        assertFalse(cmd.hasOption(opt));
        cmd.addOption(opt);
        assertTrue(cmd.hasOption(opt));
    }

    // Test getOption after add
    @Test
    public void testAddOption_triggerMapped_getOptionReturnsOption() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        Option opt = new SimpleOption("--bar", new HashSet<String>(Arrays.asList("--bar", "-b")));
        cmd.addOption(opt);
        assertSame(opt, cmd.getOption("--bar"));
        assertSame(opt, cmd.getOption("-b"));
        assertNull(cmd.getOption("--nonexistent"));
    }

    // Test addValue for an Argument (option instanceof Argument)
    @Test
    public void testAddValue_argumentOption_addsOptionAndValue() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        Argument arg = new SimpleArgument("arg1", new HashSet<String>(Arrays.asList("arg1")));
        cmd.addValue(arg, "value1");
        assertTrue(cmd.hasOption(arg));
        List values = cmd.getValues(arg, null);
        assertEquals(Arrays.asList("value1"), values);
    }

    // Test addValue for non-Argument option (no automatic addOption)
    @Test
    public void testAddValue_nonArgumentOption_noOptionAdded() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        Option opt = new SimpleOption("--opt", Collections.singleton("--opt"));
        cmd.addValue(opt, "val");
        // hasOption should be false because addOption not called
        assertFalse(cmd.hasOption(opt));
        List values = cmd.getValues(opt, null);
        assertEquals(Arrays.asList("val"), values);
    }

    // Test addSwitch normal and duplicate exception
    @Test
    public void testAddSwitch_normalSwitch_switchSet() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        Option opt = new SimpleOption("--verbose", Collections.singleton("--verbose"));
        cmd.addSwitch(opt, true);
        assertTrue(cmd.getSwitch(opt, false));
    }

    @Test(expected = IllegalStateException.class)
    public void testAddSwitch_duplicateSwitch_throwsIllegalStateException() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        Option opt = new SimpleOption("--quiet", Collections.singleton("--quiet"));
        cmd.addSwitch(opt, true);
        cmd.addSwitch(opt, false); // should throw
    }

    // Test getValues with various default scenarios
    @Test
    public void testGetValues_noValuesNoDefaults_returnsEmptyList() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        Option opt = new SimpleOption("--x", Collections.singleton("--x"));
        List result = cmd.getValues(opt, null);
        assertEquals(Collections.emptyList(), result);
    }

    @Test
    public void testGetValues_emptyValuesWithDefaults_returnsDefaults() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        Option opt = new SimpleOption("--y", Collections.singleton("--y"));
        List<String> defaults = Arrays.asList("def1", "def2");
        // set default values via setDefaultValues
        cmd.setDefaultValues(opt, defaults);
        // add no value
        List result = cmd.getValues(opt, null);
        assertEquals(defaults, result);
    }

    @Test
    public void testGetValues_valuesShorterThanDefaults_appendsExtraDefaults() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        Option opt = new SimpleOption("--z", Collections.singleton("--z"));
        List<String> defaults = Arrays.asList("def1", "def2", "def3");
        cmd.setDefaultValues(opt, defaults);
        cmd.addValue(opt, "real1");
        List result = cmd.getValues(opt, null);
        // expected: [real1, def2, def3] because only one value given, defaults have three, so append def2, def3
        List<String> expected = new ArrayList<String>();
        expected.add("real1");
        expected.add("def2");
        expected.add("def3");
        assertEquals(expected, result);
    }

    @Test
    public void testGetValues_valuesLongerThanDefaults_returnsValuesOnly() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        Option opt = new SimpleOption("--a", Collections.singleton("--a"));
        List<String> defaults = Arrays.asList("def1");
        cmd.setDefaultValues(opt, defaults);
        cmd.addValue(opt, "r1");
        cmd.addValue(opt, "r2");
        List result = cmd.getValues(opt, null);
        assertEquals(Arrays.asList("r1", "r2"), result);
    }

    // Test getUndefaultedValues
    @Test
    public void testGetUndefaultedValues_noValues_returnsEmptyList() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        Option opt = new SimpleOption("--b", Collections.singleton("--b"));
        assertEquals(Collections.emptyList(), cmd.getUndefaultedValues(opt));
    }

    @Test
    public void testGetUndefaultedValues_withValues_returnsThoseValues() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        Option opt = new SimpleOption("--c", Collections.singleton("--c"));
        cmd.addValue(opt, "val1");
        cmd.addValue(opt, "val2");
        assertEquals(Arrays.asList("val1", "val2"), cmd.getUndefaultedValues(opt));
    }

    // Test getSwitch with various fallback paths
    @Test
    public void testGetSwitch_switchPresent_returnsSwitch() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        Option opt = new SimpleOption("--d", Collections.singleton("--d"));
        cmd.addSwitch(opt, false);
        assertEquals(Boolean.FALSE, cmd.getSwitch(opt, Boolean.TRUE));
    }

    @Test
    public void testGetSwitch_switchAbsentDefaultProvided_returnsDefault() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        Option opt = new SimpleOption("--e", Collections.singleton("--e"));
        assertEquals(Boolean.TRUE, cmd.getSwitch(opt, Boolean.TRUE));
    }

    @Test
    public void testGetSwitch_switchAbsentNoDefault_returnsDefaultSwitch() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        Option opt = new SimpleOption("--f", Collections.singleton("--f"));
        cmd.setDefaultSwitch(opt, Boolean.FALSE);
        assertEquals(Boolean.FALSE, cmd.getSwitch(opt, null));
    }

    @Test
    public void testGetSwitch_allNull_returnsNull() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        Option opt = new SimpleOption("--g", Collections.singleton("--g"));
        assertNull(cmd.getSwitch(opt, null));
    }

    // Test addProperty / getProperty (PropertyOption used implicitly)
    @Test
    public void testAddProperty_simpleProperty_getsValue() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        cmd.addProperty("key", "value");
        assertEquals("value", cmd.getProperty("key"));
    }

    @Test
    public void testGetProperty_withDefaultValue_returnsDefaultWhenMissing() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        assertEquals("default", cmd.getProperty(new PropertyOption(), "nonexistent", "default"));
    }

    @Test
    public void testGetProperty_optionSpecific_returnsProperty() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        PropertyOption propOpt = new PropertyOption();
        cmd.addProperty(propOpt, "user", "alice");
        assertEquals("alice", cmd.getProperty(propOpt, "user", "bob"));
    }

    // Test getProperties returns empty set when no properties added
    @Test
    public void testGetProperties_noProperties_returnsEmptySet() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        assertEquals(Collections.emptySet(), cmd.getProperties(new PropertyOption()));
    }

    @Test
    public void testGetProperties_withProperties_returnsUnmodifiableKeySet() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        PropertyOption propOpt = new PropertyOption();
        cmd.addProperty(propOpt, "k1", "v1");
        cmd.addProperty(propOpt, "k2", "v2");
        Set keys = cmd.getProperties(propOpt);
        assertEquals(new HashSet<String>(Arrays.asList("k1", "k2")), keys);
        // verify unmodifiable
        try {
            keys.add("k3");
            fail("Should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // Test toString builds string from normalised args
    @Test
    public void testToString_concatenatesArgsWithSpaces() {
        Set<String> prefixes = new HashSet<String>();
        prefixes.add("-");
        Option root = new SimpleOption("root", Collections.singleton("root"), prefixes);
        List<String> args = Arrays.asList("--foo", "bar", "baz");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, args);
        assertEquals("--foo bar baz", cmd.toString());
    }

    @Test
    public void testToString_argsWithSpaces_quoted() {
        Set<String> prefixes = Collections.emptySet();
        Option root = new SimpleOption("root", Collections.singleton("root"), prefixes);
        List<String> args = Arrays.asList("--foo", "bar with spaces");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, args);
        assertEquals("--foo \"bar with spaces\"", cmd.toString());
    }

    // Test getOptions returns unmodifiable list
    @Test
    public void testGetOptions_afterAddOptions_returnsUnmodifiableList() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        Option opt = new SimpleOption("--h", Collections.singleton("--h"));
        cmd.addOption(opt);
        List opts = cmd.getOptions();
        assertEquals(1, opts.size());
        assertTrue(opts.contains(opt));
        try {
            opts.add(new SimpleOption("--i", Collections.singleton("--i")));
            fail("Should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // Test getOptionTriggers
    @Test
    public void testGetOptionTriggers_includesAllTriggers() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        Option opt = new SimpleOption("--j", new HashSet<String>(Arrays.asList("--j", "-j")));
        cmd.addOption(opt);
        Set triggers = cmd.getOptionTriggers();
        assertTrue(triggers.contains("--j"));
        assertTrue(triggers.contains("-j"));
        assertEquals(2, triggers.size());
    }

    // Test setDefaultValues and setDefaultSwitch (implicitly tested in other tests, but explicit coverage)
    @Test
    public void testSetDefaultValues_nullRemoves() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        Option opt = new SimpleOption("--k", Collections.singleton("--k"));
        cmd.setDefaultValues(opt, Arrays.asList("a", "b"));
        cmd.setDefaultValues(opt, null);
        List result = cmd.getValues(opt, Collections.<Object>emptyList());
        // no defaults, no values -> empty list
        assertEquals(Collections.emptyList(), result);
    }

    @Test
    public void testSetDefaultSwitch_nullRemoves() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        Option opt = new SimpleOption("--l", Collections.singleton("--l"));
        cmd.setDefaultSwitch(opt, Boolean.TRUE);
        cmd.setDefaultSwitch(opt, null);
        assertNull(cmd.getSwitch(opt, null));
    }

    // ========== NEW TEST CASES FOR MISSING COVERAGE ==========

    // Test addOption with duplicate trigger - should overwrite with last option
    @Test
    public void testAddOption_duplicateTrigger_overwrites() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        Option opt1 = new SimpleOption("--first", Collections.singleton("--dup"));
        Option opt2 = new SimpleOption("--second", Collections.singleton("--dup"));
        cmd.addOption(opt1);
        cmd.addOption(opt2);
        // getOption should return the last added option
        assertSame(opt2, cmd.getOption("--dup"));
        // hasOption should be true for both? Actually hasOption checks identity?
        // The implementation stores all options in a list, but mapping uses the latest
        assertTrue(cmd.hasOption(opt1)); // opt1 still in the list because addOption adds to list
        assertTrue(cmd.hasOption(opt2));
    }

    // Test getOption with null trigger - should return null
    @Test
    public void testGetOption_nullTrigger_returnsNull() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        Option opt = new SimpleOption("--x", Collections.singleton("--x"));
        cmd.addOption(opt);
        assertNull(cmd.getOption(null));
    }

    // Test hasOption with null option - should return false
    @Test
    public void testHasOption_nullOption_returnsFalse() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        assertFalse(cmd.hasOption(null));
    }

    // Test getValues with null option - should throw NullPointerException
    @Test(expected = NullPointerException.class)
    public void testGetValues_nullOption_throwsNullPointer() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        cmd.getValues(null, null);
    }

    // Test getUndefaultedValues with null option - should throw NullPointerException
    @Test(expected = NullPointerException.class)
    public void testGetUndefaultedValues_nullOption_throwsNullPointer() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        cmd.getUndefaultedValues(null);
    }

    // Test getSwitch with null option - should throw NullPointerException
    @Test(expected = NullPointerException.class)
    public void testGetSwitch_nullOption_throwsNullPointer() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        cmd.getSwitch(null, Boolean.TRUE);
    }

    // Test getProperties with non-PropertyOption - should return empty set
    @Test
    public void testGetProperties_nonPropertyOption_returnsEmptySet() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        Option nonProp = new SimpleOption("--np", Collections.singleton("--np"));
        cmd.addProperty("key", "value"); // adds property under default PropertyOption
        Set result = cmd.getProperties(nonProp);
        assertTrue(result.isEmpty());
    }

    // Test getOptionTriggers when no options added
    @Test
    public void testGetOptionTriggers_empty_returnsEmptySet() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        Set triggers = cmd.getOptionTriggers();
        assertTrue(triggers.isEmpty());
    }

    // Test looksLikeOption with empty prefixes (root has no prefixes)
    @Test
    public void testLooksLikeOption_emptyPrefixes_returnsFalse() {
        // createEmptyCommandLine already uses root with empty prefixes
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        assertFalse(cmd.looksLikeOption("--anything"));
        assertFalse(cmd.looksLikeOption("-x"));
        assertFalse(cmd.looksLikeOption("plain"));
    }

    // Test toString with empty args
    @Test
    public void testToString_emptyArgs_returnsEmptyString() {
        Set<String> prefixes = new HashSet<String>();
        prefixes.add("-");
        Option root = new SimpleOption("root", Collections.singleton("root"), prefixes);
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        assertEquals("", cmd.toString());
    }

    // Test addProperty overwrites existing key
    @Test
    public void testAddProperty_overwriteKey_updatesValue() {
        WriteableCommandLineImpl cmd = createEmptyCommandLine();
        cmd.addProperty("key", "old");
        cmd.addProperty("key", "new");
        assertEquals("new", cmd.getProperty("key"));
    }

    // Helper to create a simple command line with root that has empty prefixes
    private WriteableCommandLineImpl createEmptyCommandLine() {
        Set<String> prefixes = Collections.emptySet();
        Option root = new SimpleOption("root", Collections.singleton("root"), prefixes);
        return new WriteableCommandLineImpl(root, new ArrayList<String>());
    }
}