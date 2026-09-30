package org.apache.commons.cli2.commandline;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.option.SourceDestArgument;
import org.apache.commons.cli2.option.ArgumentImpl;

import java.util.*;

public class WriteableCommandLineImplTest {

    // Helper method to create a simple Option for testing
    private Option createOption(String preferredName, String trigger) {
        return new Option() {
            public String getPreferredName() {
                return preferredName;
            }

            public Set getTriggers() {
                Set<String> triggers = new HashSet<String>();
                triggers.add(trigger);
                triggers.add(preferredName);
                return triggers;
            }

            public Set getPrefixes() {
                Set<String> prefixes = new HashSet<String>();
                prefixes.add("-");
                return prefixes;
            }

            // Other Option methods - not needed for these tests
            public void defaultValues(WriteableCommandLine commandLine) { }
            public void process(WriteableCommandLine commandLine, List arguments) { }
            public boolean canProcess(WriteableCommandLine commandLine, String argument) { return false; }
            public boolean isRequired() { return false; }
            public List getDescription() { return Collections.emptyList(); }
            public boolean canProcessLine(String line) { return false; }
            public boolean isSwitch() { return false; }
            public void validate(WriteableCommandLine commandLine) { }
        };
    }

    // Helper method to create an Argument for testing
    private Argument createArgument(final String preferredName, String trigger) {
        return new Argument() {
            public String getPreferredName() {
                return preferredName;
            }

            public Set getTriggers() {
                Set<String> triggers = new HashSet<String>();
                triggers.add(trigger);
                triggers.add(preferredName);
                return triggers;
            }

            public Set getPrefixes() {
                Set<String> prefixes = new HashSet<String>();
                prefixes.add("-");
                return prefixes;
            }

            public void defaultValues(WriteableCommandLine commandLine) { }
            public void process(WriteableCommandLine commandLine, List arguments) { }
            public boolean canProcess(WriteableCommandLine commandLine, String argument) { return false; }
            public boolean isRequired() { return false; }
            public List getDescription() { return Collections.emptyList(); }
            public boolean canProcessLine(String line) { return false; }
            public boolean isSwitch() { return false; }
            public void validate(WriteableCommandLine commandLine) { }
            public void process(WriteableCommandLine commandLine, List arguments, boolean stopAtNonOption) { }
            public String getInitialSeparator() { return null; }
            public int getMaximum() { return Integer.MAX_VALUE; }
            public int getMinimum() { return 0; }
            public boolean isSubstring() { return false; }
        };
    }

    // Test constructor and basic setup
    @Test
    public void testConstructor_normalInput_setsPrefixesAndNormalised() {
        Option root = createOption("root", "r");
        List args = new ArrayList();
        args.add("--file");
        args.add("test.txt");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, args);

        assertNotNull(cmd);
        assertFalse(cmd.hasOption(root));
        assertEquals(args, cmd.getNormalised());
    }

    // Tests addOption - normal case: add a new option
    @Test
    public void testAddOption_singleOption_addsToList() {
        Option root = createOption("root", "r");
        Option opt = createOption("verbose", "v");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        cmd.addOption(opt);
        assertTrue(cmd.hasOption(opt));
        assertTrue(cmd.getOptions().contains(opt));
    }

    // Tests addOption - option with triggers can be retrieved by trigger
    @Test
    public void testAddOption_withTriggers_retrievableByTrigger() {
        Option root = createOption("root", "r");
        Option opt = createOption("output", "o");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        cmd.addOption(opt);
        assertSame(opt, cmd.getOption("output"));
        assertSame(opt, cmd.getOption("o"));
    }

    // Tests addValue - normal case: add a value to an option
    @Test
    public void testAddValue_singleValue_storesCorrectly() {
        Option root = createOption("root", "r");
        Option opt = createOption("file", "f");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        cmd.addValue(opt, "test.txt");
        List values = cmd.getValues(opt, null);
        assertEquals(1, values.size());
        assertEquals("test.txt", values.get(0));
    }

    // Tests addValue - multiple values for same option are stored
    @Test
    public void testAddValue_multipleValues_storesAll() {
        Option root = createOption("root", "r");
        Option opt = createOption("src", "s");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        cmd.addValue(opt, "file1.java");
        cmd.addValue(opt, "file2.java");
        List values = cmd.getValues(opt, null);
        assertEquals(2, values.size());
        assertEquals("file1.java", values.get(0));
        assertEquals("file2.java", values.get(1));
    }

    // Tests addValue - with Argument option: also adds option
    @Test
    public void testAddValue_argumentOption_addsOption() {
        Option root = createOption("root", "r");
        Argument arg = createArgument("input", "i");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        assertFalse(cmd.hasOption(arg));
        cmd.addValue(arg, "data");
        assertTrue(cmd.hasOption(arg));
    }

    // Tests addSwitch - normal case: set switch to true
    @Test
    public void testAddSwitch_singleSwitch_returnsTrue() {
        Option root = createOption("root", "r");
        Option opt = createOption("debug", "d");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        cmd.addSwitch(opt, true);
        assertEquals(Boolean.TRUE, cmd.getSwitch(opt, null));
    }

    // Tests addSwitch - set switch to false
    @Test
    public void testAddSwitch_switchOff_returnsFalse() {
        Option root = createOption("root", "r");
        Option opt = createOption("quiet", "q");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        cmd.addSwitch(opt, false);
        assertEquals(Boolean.FALSE, cmd.getSwitch(opt, null));
    }

    // Tests addSwitch - exception case: setting same switch twice throws exception
    @Test(expected = IllegalStateException.class)
    public void testAddSwitch_alreadySet_throwsIllegalStateException() {
        Option root = createOption("root", "r");
        Option opt = createOption("verbose", "v");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        cmd.addSwitch(opt, true);
        cmd.addSwitch(opt, false);
    }

    // Tests hasOption - option not added returns false
    @Test
    public void testHasOption_optionNotPresent_returnsFalse() {
        Option root = createOption("root", "r");
        Option opt = createOption("missing", "m");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        assertFalse(cmd.hasOption(opt));
    }

    // Tests getValues - no command line values, method default returns supplied default
    @Test
    public void testGetValues_noCommandLineValues_returnsMethodDefault() {
        Option root = createOption("root", "r");
        Option opt = createOption("output", "o");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        List defaults = new ArrayList();
        defaults.add("default.out");
        List values = cmd.getValues(opt, defaults);
        assertEquals(1, values.size());
        assertEquals("default.out", values.get(0));
    }

    // Tests getValues - no values at all returns empty list
    @Test
    public void testGetValues_noValuesAtAll_returnsEmptyList() {
        Option root = createOption("root", "r");
        Option opt = createOption("temp", "t");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        List values = cmd.getValues(opt, (List) null);
        assertNotNull(values);
        assertTrue(values.isEmpty());
    }

    // Tests getSwitch - no switch set, method default provided
    @Test
    public void testGetSwitch_noSwitchSet_returnsMethodDefault() {
        Option root = createOption("root", "r");
        Option opt = createOption("flag", "f");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        Boolean result = cmd.getSwitch(opt, Boolean.TRUE);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests getSwitch - no switch set and no defaults returns null
    @Test
    public void testGetSwitch_noSwitchSetAndNoDefaults_returnsNull() {
        Option root = createOption("root", "r");
        Option opt = createOption("flag", "f");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        assertNull(cmd.getSwitch(opt, null));
    }

    // Tests setDefaultValues - set defaults then getValues returns it
    @Test
    public void testSetDefaultValues_defaultsSet_returnsDefaults() {
        Option root = createOption("root", "r");
        Option opt = createOption("config", "c");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        List defaults = new ArrayList();
        defaults.add("default.cfg");
        cmd.setDefaultValues(opt, defaults);

        List values = cmd.getValues(opt, (List) null);
        assertEquals(1, values.size());
        assertEquals("default.cfg", values.get(0));
    }

    // Tests setDefaultValues - null removes default
    @Test
    public void testSetDefaultValues_nullDefaults_removesPreviousDefault() {
        Option root = createOption("root", "r");
        Option opt = createOption("config", "c");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        List defaults = new ArrayList();
        defaults.add("default.cfg");
        cmd.setDefaultValues(opt, defaults);
        cmd.setDefaultValues(opt, (List) null);

        List values = cmd.getValues(opt, (List) null);
        assertTrue(values.isEmpty());
    }

    // Tests addProperty and getProperty
    @Test
    public void testProperty_setAndGet_returnsCorrectValue() {
        Option root = createOption("root", "r");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        cmd.addProperty("user", "admin");
        assertEquals("admin", cmd.getProperty("user", "default"));
    }

    // Tests getProperty - property not set returns default
    @Test
    public void testProperty_notSet_returnsDefault() {
        Option root = createOption("root", "r");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        assertEquals("default", cmd.getProperty("missing", "default"));
    }

    // Tests getProperties - returns unmodifiable set
    @Test
    public void testGetProperties_returnsPropertyKeys() {
        Option root = createOption("root", "r");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        cmd.addProperty("key1", "val1");
        cmd.addProperty("key2", "val2");
        Set props = cmd.getProperties();
        assertEquals(2, props.size());
        assertTrue(props.contains("key1"));
        assertTrue(props.contains("key2"));
    }

    // Tests looksLikeOption - prefix matches returns true
    @Test
    public void testLooksLikeOption_startsWithPrefix_returnsTrue() {
        Option root = createOption("root", "r");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        assertTrue(cmd.looksLikeOption("-verbose"));
        assertTrue(cmd.looksLikeOption("--file"));
    }

    // Tests looksLikeOption - no prefix match returns false
    @Test
    public void testLooksLikeOption_noPrefix_returnsFalse() {
        Option root = createOption("root", "r");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        assertFalse(cmd.looksLikeOption("verbose"));
        assertFalse(cmd.looksLikeOption("file.txt"));
    }

    // Tests setDefaultSwitch and getSwitch uses it
    @Test
    public void testSetDefaultSwitch_switchSet_returnsDefault() {
        Option root = createOption("root", "r");
        Option opt = createOption("flag", "f");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        cmd.setDefaultSwitch(opt, Boolean.FALSE);
        assertEquals(Boolean.FALSE, cmd.getSwitch(opt, null));
    }

    // Tests setDefaultSwitch - null removes default
    @Test
    public void testSetDefaultSwitch_null_removesDefault() {
        Option root = createOption("root", "r");
        Option opt = createOption("flag", "f");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        cmd.setDefaultSwitch(opt, Boolean.TRUE);
        cmd.setDefaultSwitch(opt, (Boolean) null);
        assertNull(cmd.getSwitch(opt, null));
    }

    // Tests toString with arguments
    @Test
    public void testToString_argumentsWithSpaces_usesQuotes() {
        Option root = createOption("root", "r");
        List args = new ArrayList();
        args.add("--file");
        args.add("my file.txt");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, args);

        String result = cmd.toString();
        assertTrue(result.contains("--file"));
        assertTrue(result.contains("\"my file.txt\""));
    }

    // Tests toString with normal arguments
    @Test
    public void testToString_normalArguments_noQuotes() {
        Option root = createOption("root", "r");
        List args = new ArrayList();
        args.add("input.txt");
        args.add("output.txt");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, args);

        String result = cmd.toString();
        assertEquals("input.txt output.txt", result);
    }

    // ========== New tests added to improve coverage ==========

    @Test
    public void testHasOption_triggerAdded_returnsTrue() {
        Option root = createOption("root", "r");
        Option opt = createOption("verbose", "v");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        cmd.addOption(opt);
        assertTrue(cmd.hasOption("verbose"));
        assertTrue(cmd.hasOption("v"));
    }

    @Test
    public void testHasOption_triggerNotAdded_returnsFalse() {
        Option root = createOption("root", "r");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        assertFalse(cmd.hasOption("missing"));
    }

    @Test
    public void testGetOption_notFound_returnsNull() {
        Option root = createOption("root", "r");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        assertNull(cmd.getOption("nonexistent"));
    }

    @Test
    public void testGetOption_foundByAlternativeTrigger_returnsCorrectOption() {
        Option root = createOption("root", "r");
        Option opt = createOption("output", "o");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        cmd.addOption(opt);
        assertSame(opt, cmd.getOption("o"));
        assertSame(opt, cmd.getOption("output"));
    }

    @Test
    public void testAddOption_duplicateOption_doesNotCauseException() {
        Option root = createOption("root", "r");
        Option opt = createOption("verbose", "v");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        cmd.addOption(opt);
        // Adding same option again should be allowed (no exception)
        cmd.addOption(opt);
        assertTrue(cmd.hasOption(opt));
    }

    @Test(expected = NullPointerException.class)
    public void testAddValue_nullOption_throwsNullPointerException() {
        Option root = createOption("root", "r");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        cmd.addValue(null, "value");
    }

    @Test(expected = NullPointerException.class)
    public void testAddValue_nullValue_throwsNullPointerException() {
        Option root = createOption("root", "r");
        Option opt = createOption("file", "f");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        cmd.addValue(opt, null);
    }

    @Test(expected = NullPointerException.class)
    public void testAddSwitch_nullOption_throwsNullPointerException() {
        Option root = createOption("root", "r");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        cmd.addSwitch(null, true);
    }

    @Test(expected = NullPointerException.class)
    public void testAddSwitch_nullBoolean_throwsNullPointerException() {
        Option root = createOption("root", "r");
        Option opt = createOption("debug", "d");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        cmd.addSwitch(opt, null);
    }

    @Test(expected = NullPointerException.class)
    public void testAddProperty_nullKey_throwsNullPointerException() {
        Option root = createOption("root", "r");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        cmd.addProperty(null, "value");
    }

    @Test
    public void testAddProperty_nullValue_storesNull() {
        Option root = createOption("root", "r");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        cmd.addProperty("key", null);
        assertNull(cmd.getProperty("key"));
    }

    @Test(expected = NullPointerException.class)
    public void testGetProperty_nullKey_throwsNullPointerException() {
        Option root = createOption("root", "r");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        cmd.getProperty(null);
    }

    @Test
    public void testGetProperty_nullDefault_returnsNullForMissingKey() {
        Option root = createOption("root", "r");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        assertNull(cmd.getProperty("missing", null));
    }

    @Test
    public void testToString_emptyArgs_returnsEmptyString() {
        Option root = createOption("root", "r");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        assertEquals("", cmd.toString());
    }

    @Test
    public void testGetRoot_returnsConstructorRoot() {
        Option root = createOption("root", "r");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        assertSame(root, cmd.getRoot());
    }

    @Test
    public void testGetUndefaultedValues_noValues_returnsEmptyList() {
        Option root = createOption("root", "r");
        Option opt = createOption("file", "f");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        assertTrue(cmd.getUndefaultedValues(opt).isEmpty());
    }

    @Test
    public void testGetUndefaultedValues_returnsValuesWithoutDefaults() {
        Option root = createOption("root", "r");
        Option opt = createOption("file", "f");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        cmd.addValue(opt, "actual.txt");
        List defaults = new ArrayList();
        defaults.add("default.txt");
        cmd.setDefaultValues(opt, defaults);
        List undefaulted = cmd.getUndefaultedValues(opt);
        assertEquals(1, undefaulted.size());
        assertEquals("actual.txt", undefaulted.get(0));
    }
}