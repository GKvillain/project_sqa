package org.apache.commons.cli2.option;

import java.io.PrintWriter;
import java.util.*;
import org.apache.commons.cli2.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class OptionImplTest {

    // Concrete subclass to test abstract OptionImpl
    private static class TestOption extends OptionImpl {
        private final String preferredName;
        private final String description;
        private final Set<String> prefixes;
        private final Set<String> triggers;
        private boolean canProcessResult = true;

        TestOption(int id, boolean required,
                   String preferredName, String description,
                   Set<String> prefixes, Set<String> triggers) {
            super(id, required);
            this.preferredName = preferredName;
            this.description = description;
            this.prefixes = prefixes;
            this.triggers = triggers;
        }

        @Override
        public String getPreferredName() { return preferredName; }

        @Override
        public String getDescription() { return description; }

        @Override
        public Set<String> getPrefixes() { return prefixes; }

        @Override
        public Set<String> getTriggers() { return triggers; }

        @Override
        public boolean canProcess(WriteableCommandLine commandLine, String argument) {
            return canProcessResult;
        }

        @Override
        public void appendUsage(StringBuffer buffer, Set displaySettings, PrintWriter writer) {
            buffer.append("usage");
        }

        @Override
        public void process(WriteableCommandLine commandLine, ListIterator<String> args) {
            // No-op for testing
        }

        public void setCanProcessResult(boolean val) { this.canProcessResult = val; }
    }

    // Dummy WriteableCommandLine (only required for passing to methods)
    private WriteableCommandLine createDummyCommandLine() {
        return new WriteableCommandLine() {
            public void addOption(Option option) { throw new UnsupportedOperationException(); }
            public void addValue(Option option, Object value) { throw new UnsupportedOperationException(); }
            public void setDefaultValues(Option option, List defaults) { throw new UnsupportedOperationException(); }
            public boolean looksLikeOption(String argument) { throw new UnsupportedOperationException(); }
            public String[] getValues(Option option, List defaultValues) { throw new UnsupportedOperationException(); }
            public List getValues(Option option, List defaultValues, boolean allowNull) { throw new UnsupportedOperationException(); }
            public boolean hasOption(Option option) { throw new UnsupportedOperationException(); }
            public Option getOption(String trigger) { throw new UnsupportedOperationException(); }
            public List getOptions() { throw new UnsupportedOperationException(); }
            public List getOptionValues(Option option) { throw new UnsupportedOperationException(); }
            public Object getOptionValue(Option option) { throw new UnsupportedOperationException(); }
            public String getProperty(String property) { throw new UnsupportedOperationException(); }
            public void addProperty(String property, Object value) { throw new UnsupportedOperationException(); }
            public void addProperty(Option option, String property, Object value) { throw new UnsupportedOperationException(); }
            public Boolean getUndefaulted(Option option) { throw new UnsupportedOperationException(); }
        };
    }

    // Constructor tests
    @Test
    public void testConstructor_positiveIdAndRequired_returnsCorrectIdAndRequired() {
        final TestOption option = new TestOption(5, true, "a", "desc",
                Collections.singleton("--"), Collections.singleton("--a"));
        assertEquals(5, option.getId());
        assertTrue(option.isRequired());
    }

    @Test
    public void testConstructor_negativeIdAndNotRequired_returnsNegativeIdAndFalse() {
        final TestOption option = new TestOption(-1, false, "b", null,
                Collections.EMPTY_SET, Collections.EMPTY_SET);
        assertEquals(-1, option.getId());
        assertFalse(option.isRequired());
    }

    @Test
    public void testConstructor_zeroId_returnsZero() {
        final TestOption option = new TestOption(0, true, "", "",
                new HashSet<String>(), new HashSet<String>());
        assertEquals(0, option.getId());
        assertTrue(option.isRequired());
    }

    // canProcess tests
    @Test
    public void testCanProcess_iteratorHasNext_returnsSubclassResult() {
        final TestOption option = new TestOption(1, false, "opt", "desc",
                Collections.singleton("-"), Collections.singleton("-opt"));
        option.setCanProcessResult(true);
        WriteableCommandLine cmdLine = createDummyCommandLine();
        List<String> args = new ArrayList<String>();
        args.add("arg");
        ListIterator<String> it = args.listIterator();
        assertTrue(option.canProcess(cmdLine, it));
    }

    @Test
    public void testCanProcess_iteratorNoNext_returnsFalse() {
        final TestOption option = new TestOption(2, false, "opt2", "desc2",
                Collections.EMPTY_SET, Collections.EMPTY_SET);
        WriteableCommandLine cmdLine = createDummyCommandLine();
        List<String> args = new ArrayList<String>();
        ListIterator<String> it = args.listIterator();
        assertFalse(option.canProcess(cmdLine, it));
    }

    // toString
    @Test
    public void testToString_anyOption_returnsNonNull() {
        final TestOption option = new TestOption(3, false, "test", "testdesc",
                new HashSet<String>(), new HashSet<String>());
        assertNotNull(option.toString());
        assertFalse(option.toString().isEmpty());
    }

    // equals tests
    @Test
    public void testEquals_sameObject_returnsTrue() {
        final TestOption option = new TestOption(10, false, "same", "desc",
                Collections.singleton("--"), Collections.singleton("--same"));
        assertTrue(option.equals(option));
    }

    @Test
    public void testEquals_differentId_returnsFalse() {
        final TestOption option1 = new TestOption(1, false, "name", "desc",
                Collections.singleton("--"), Collections.singleton("--name"));
        final TestOption option2 = new TestOption(2, false, "name", "desc",
                Collections.singleton("--"), Collections.singleton("--name"));
        assertFalse(option1.equals(option2));
    }

    @Test
    public void testEquals_differentPreferredName_returnsFalse() {
        final TestOption option1 = new TestOption(1, false, "name1", "desc",
                Collections.singleton("--"), Collections.singleton("--name"));
        final TestOption option2 = new TestOption(1, false, "name2", "desc",
                Collections.singleton("--"), Collections.singleton("--name"));
        assertFalse(option1.equals(option2));
    }

    @Test
    public void testEquals_differentDescription_returnsFalse() {
        final TestOption option1 = new TestOption(1, false, "name", "desc1",
                Collections.singleton("--"), Collections.singleton("--name"));
        final TestOption option2 = new TestOption(1, false, "name", "desc2",
                Collections.singleton("--"), Collections.singleton("--name"));
        assertFalse(option1.equals(option2));
    }

    @Test
    public void testEquals_nullPreferredNameVsNonNull_returnsFalse() {
        final TestOption option1 = new TestOption(1, false, null, "desc",
                Collections.singleton("--"), Collections.singleton("--name"));
        final TestOption option2 = new TestOption(1, false, "name", "desc",
                Collections.singleton("--"), Collections.singleton("--name"));
        assertFalse(option1.equals(option2));
    }

    @Test
    public void testEquals_bothNullPreferredName_returnsTrue() {
        final TestOption option1 = new TestOption(1, false, null, "desc",
                Collections.singleton("--"), Collections.singleton("--name"));
        final TestOption option2 = new TestOption(1, false, null, "desc",
                Collections.singleton("--"), Collections.singleton("--name"));
        assertTrue(option1.equals(option2));
    }

    @Test
    public void testEquals_nonOptionObject_returnsFalse() {
        final TestOption option = new TestOption(1, false, "n", "d",
                new HashSet<String>(), new HashSet<String>());
        assertFalse(option.equals("string"));
    }

    @Test
    public void testEquals_nullObject_returnsFalse() {
        final TestOption option = new TestOption(1, false, "n", "d",
                new HashSet<String>(), new HashSet<String>());
        assertFalse(option.equals(null));
    }

    @Test
    public void testEquals_differentPrefixes_returnsFalse() {
        final TestOption option1 = new TestOption(1, false, "name", "desc",
                new HashSet<String>(Arrays.asList("--")), new HashSet<String>(Arrays.asList("--name")));
        final TestOption option2 = new TestOption(1, false, "name", "desc",
                new HashSet<String>(Arrays.asList("-")), new HashSet<String>(Arrays.asList("--name")));
        assertFalse(option1.equals(option2));
    }

    // hashCode tests
    @Test
    public void testHashCode_equalObjects_sameHashCode() {
        final TestOption option1 = new TestOption(5, false, "pref", "desc",
                new HashSet<String>(Arrays.asList("-")), new HashSet<String>(Arrays.asList("-pref")));
        final TestOption option2 = new TestOption(5, false, "pref", "desc",
                new HashSet<String>(Arrays.asList("-")), new HashSet<String>(Arrays.asList("-pref")));
        assertEquals(option1.hashCode(), option2.hashCode());
    }

    @Test
    public void testHashCode_differentId_differentHashCode() {
        final TestOption option1 = new TestOption(5, false, "pref", "desc",
                new HashSet<String>(Arrays.asList("-")), new HashSet<String>(Arrays.asList("-pref")));
        final TestOption option2 = new TestOption(6, false, "pref", "desc",
                new HashSet<String>(Arrays.asList("-")), new HashSet<String>(Arrays.asList("-pref")));
        assertFalse(option1.hashCode() == option2.hashCode());
    }

    // findOption tests
    @Test
    public void testFindOption_triggerInTriggers_returnsThis() {
        final Set<String> triggers = new HashSet<String>(Arrays.asList("-a", "-b"));
        final TestOption option = new TestOption(1, false, "a", "desc",
                new HashSet<String>(), triggers);
        assertSame(option, option.findOption("-a"));
    }

    @Test
    public void testFindOption_triggerNotInTriggers_returnsNull() {
        final Set<String> triggers = new HashSet<String>(Arrays.asList("-a", "-b"));
        final TestOption option = new TestOption(1, false, "a", "desc",
                new HashSet<String>(), triggers);
        assertNull(option.findOption("-c"));
    }

    // checkPrefixes tests
    @Test
    public void testCheckPrefixes_emptyPrefixes_doesNotThrow() {
        final Set<String> prefixes = new HashSet<String>();
        final Set<String> triggers = new HashSet<String>(Arrays.asList("--foo"));
        final TestOption option = new TestOption(1, false, "--foo", null,
                prefixes, triggers);
        option.checkPrefixes(prefixes); // should return immediately
    }

    @Test
    public void testCheckPrefixes_validPrefix_doesNotThrow() {
        final Set<String> prefixes = new HashSet<String>(Arrays.asList("--", "-"));
        final Set<String> triggers = new HashSet<String>(Arrays.asList("--foo", "-bar"));
        final TestOption option = new TestOption(1, false, "pref", "desc",
                prefixes, triggers);
        option.checkPrefixes(prefixes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckPrefixes_invalidPreferredName_throwsIllegalArgumentException() {
        final Set<String> prefixes = new HashSet<String>(Arrays.asList("--", "-"));
        final Set<String> triggers = new HashSet<String>(Arrays.asList("validTrigger"));
        final TestOption option = new TestOption(1, false, "invalidPref", "desc",
                prefixes, triggers);
        option.checkPrefixes(prefixes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckPrefixes_invalidTrigger_throwsIllegalArgumentException() {
        final Set<String> prefixes = new HashSet<String>(Arrays.asList("--", "-"));
        final Set<String> triggers = new HashSet<String>(Arrays.asList("invalidTrigger"));
        final TestOption option = new TestOption(1, false, "--validPref", null,
                prefixes, triggers);
        option.checkPrefixes(prefixes);
    }

    // defaults method (void, no side effects)
    @Test
    public void testDefaults_noException() {
        final TestOption option = new TestOption(1, false, "opt", "desc",
                new HashSet<String>(), new HashSet<String>());
        WriteableCommandLine cmdLine = createDummyCommandLine();
        option.defaults(cmdLine);
    }

    // ========== New test cases for uncovered parts ==========

    // getter tests
    @Test
    public void testGetPreferredName_returnsPreferredName() {
        final TestOption option = new TestOption(1, false, "myName", "desc",
                Collections.singleton("--"), Collections.singleton("--myName"));
        assertEquals("myName", option.getPreferredName());
    }

    @Test
    public void testGetDescription_returnsDescription() {
        final TestOption option = new TestOption(1, false, "name", "myDescription",
                Collections.singleton("--"), Collections.singleton("--name"));
        assertEquals("myDescription", option.getDescription());
    }

    @Test
    public void testGetPrefixes_returnsPrefixes() {
        final Set<String> prefixes = new HashSet<String>(Arrays.asList("--", "-"));
        final TestOption option = new TestOption(1, false, "name", "desc",
                prefixes, Collections.singleton("--name"));
        assertEquals(prefixes, option.getPrefixes());
    }

    @Test
    public void testGetTriggers_returnsTriggers() {
        final Set<String> triggers = new HashSet<String>(Arrays.asList("-a", "-b"));
        final TestOption option = new TestOption(1, false, "name", "desc",
                Collections.singleton("-"), triggers);
        assertEquals(triggers, option.getTriggers());
    }

    // canProcess(String) overload test
    @Test
    public void testCanProcess_string_returnsSubclassResult() {
        final TestOption option = new TestOption(1, false, "opt", "desc",
                Collections.singleton("-"), Collections.singleton("-opt"));
        option.setCanProcessResult(true);
        WriteableCommandLine cmdLine = createDummyCommandLine();
        assertTrue(option.canProcess(cmdLine, "anyArgument"));
    }

    // process method test
    @Test
    public void testProcess_noException() {
        final TestOption option = new TestOption(1, false, "opt", "desc",
                Collections.singleton("-"), Collections.singleton("-opt"));
        WriteableCommandLine cmdLine = createDummyCommandLine();
        List<String> args = new ArrayList<String>();
        ListIterator<String> it = args.listIterator();
        option.process(cmdLine, it); // should not throw
    }

    // appendUsage test
    @Test
    public void testAppendUsage_setsBuffer() {
        final TestOption option = new TestOption(1, false, "name", "desc",
                Collections.singleton("--"), Collections.singleton("--name"));
        StringBuffer sb = new StringBuffer();
        option.appendUsage(sb, null, null);
        assertEquals("usage", sb.toString());
    }
}