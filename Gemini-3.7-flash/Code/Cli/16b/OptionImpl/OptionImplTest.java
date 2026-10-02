package org.apache.commons.cli2.option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.WriteableCommandLine;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class OptionImplTest {

    private static class TestOption extends OptionImpl {
        private String preferredName;
        private String description;
        private Set prefixes;
        private Set triggers;
        private boolean canProcessArgumentResult = true;

        public TestOption(int id, boolean required) {
            super(id, required);
            this.prefixes = Collections.EMPTY_SET;
            this.triggers = Collections.EMPTY_SET;
        }

        public TestOption(int id, boolean required, String preferredName, String description,
                          Set prefixes, Set triggers) {
            super(id, required);
            this.preferredName = preferredName;
            this.description = description;
            this.prefixes = (prefixes != null) ? prefixes : Collections.EMPTY_SET;
            this.triggers = (triggers != null) ? triggers : Collections.EMPTY_SET;
        }

        public void setCanProcessArgumentResult(boolean result) {
            this.canProcessArgumentResult = result;
        }

        public boolean canProcess(WriteableCommandLine commandLine, String argument) {
            return canProcessArgumentResult;
        }

        public void process(WriteableCommandLine commandLine, ListIterator arguments) {
        }

        public void validate(WriteableCommandLine commandLine) {
        }

        public void appendUsage(StringBuffer buffer, Set helpSettings, Comparator comp) {
            if (preferredName != null) {
                buffer.append(preferredName);
            }
        }

        public String getPreferredName() {
            return preferredName;
        }

        public String getDescription() {
            return description;
        }

        public Set getPrefixes() {
            return prefixes;
        }

        public Set getTriggers() {
            return triggers;
        }

        public List helpLines(int depth, Set helpSettings, Comparator comp) {
            return Collections.EMPTY_LIST;
        }

        public void triggerCheckPrefixes(Set prefixesToCheck) {
            checkPrefixes(prefixesToCheck);
        }
    }

    private static class DefaultConstructorOption extends OptionImpl {
        public DefaultConstructorOption() {
            super();
        }

        public boolean canProcess(WriteableCommandLine commandLine, String argument) {
            return false;
        }

        public void process(WriteableCommandLine commandLine, ListIterator arguments) {
        }

        public void validate(WriteableCommandLine commandLine) {
        }

        public void appendUsage(StringBuffer buffer, Set helpSettings, Comparator comp) {
        }

        public String getPreferredName() {
            return null;
        }

        public String getDescription() {
            return null;
        }

        public Set getPrefixes() {
            return Collections.EMPTY_SET;
        }

        public Set getTriggers() {
            return Collections.EMPTY_SET;
        }

        public List helpLines(int depth, Set helpSettings, Comparator comp) {
            return Collections.EMPTY_LIST;
        }
    }

    // Tests constructor initialization and basic getters
    @Test
    public void testConstructor_setsIdAndRequired() {
        TestOption option = new TestOption(42, true);
        assertEquals(42, option.getId());
        assertTrue(option.isRequired());

        TestOption optionalOption = new TestOption(0, false);
        assertEquals(0, optionalOption.getId());
        assertFalse(optionalOption.isRequired());
    }

    // Tests default constructor initialization
    @Test
    public void testDefaultConstructor() {
        DefaultConstructorOption option = new DefaultConstructorOption();
        assertEquals(0, option.getId());
        assertFalse(option.isRequired());
    }

    // Tests canProcess with empty argument list returns false
    @Test
    public void testCanProcess_emptyArguments_returnsFalse() {
        TestOption option = new TestOption(1, false);
        List args = new ArrayList();
        ListIterator iterator = args.listIterator();

        assertFalse(option.canProcess((WriteableCommandLine) null, iterator));
    }

    // Tests canProcess with arguments delegates and resets iterator position
    @Test
    public void testCanProcess_withArguments_delegatesAndResetsCursor() {
        TestOption option = new TestOption(1, false);
        List args = new ArrayList();
        args.add("--test");
        ListIterator iterator = args.listIterator();

        option.setCanProcessArgumentResult(true);
        assertTrue(option.canProcess((WriteableCommandLine) null, iterator));
        assertTrue(iterator.hasNext());
        assertEquals("--test", iterator.next());

        iterator.previous();
        option.setCanProcessArgumentResult(false);
        assertFalse(option.canProcess((WriteableCommandLine) null, iterator));
        assertEquals(0, iterator.nextIndex());
    }

    // Tests toString returns string populated by appendUsage
    @Test
    public void testToString_appendsUsageProperly() {
        Set prefixes = new HashSet();
        prefixes.add("--");
        TestOption option = new TestOption(1, false, "--option", "desc", prefixes, Collections.EMPTY_SET);

        assertEquals("--option", option.toString());
    }

    // Tests findOption when trigger is present in triggers set
    @Test
    public void testFindOption_matchingTrigger_returnsThis() {
        Set triggers = new HashSet();
        triggers.add("-a");
        triggers.add("--all");
        TestOption option = new TestOption(1, false, "--all", "desc", Collections.EMPTY_SET, triggers);

        Option found = option.findOption("-a");
        assertNotNull(found);
        assertSame(option, found);
    }

    // Tests findOption when trigger is not in triggers set
    @Test
    public void testFindOption_nonMatchingTrigger_returnsNull() {
        Set triggers = new HashSet();
        triggers.add("-a");
        TestOption option = new TestOption(1, false, "-a", "desc", Collections.EMPTY_SET, triggers);

        assertNull(option.findOption("-b"));
    }

    // Tests defaults method does not throw exception
    @Test
    public void testDefaults_noOpExecution() {
        TestOption option = new TestOption(1, false);
        option.defaults(null);
    }

    // Tests equals with same object reference
    @Test
    public void testEquals_sameInstance_returnsTrue() {
        TestOption option = new TestOption(1, true);
        assertTrue(option.equals(option));
    }

    // Tests equals with null and different object type
    @Test
    public void testEquals_nullOrDifferentType_returnsFalse() {
        TestOption option = new TestOption(1, true);
        assertFalse(option.equals(null));
        assertFalse(option.equals("not an OptionImpl"));
    }

    // Tests equals with matching fields
    @Test
    public void testEquals_matchingFields_returnsTrue() {
        Set prefixes = new HashSet();
        prefixes.add("-");
        Set triggers = new HashSet();
        triggers.add("-f");

        TestOption opt1 = new TestOption(10, true, "-f", "force", prefixes, triggers);
        TestOption opt2 = new TestOption(10, false, "-f", "force", prefixes, triggers);

        assertTrue(opt1.equals(opt2));
        assertTrue(opt2.equals(opt1));
        assertEquals(opt1.hashCode(), opt2.hashCode());
    }

    // Tests equals when id differs
    @Test
    public void testEquals_differentId_returnsFalse() {
        TestOption opt1 = new TestOption(1, true);
        TestOption opt2 = new TestOption(2, true);

        assertFalse(opt1.equals(opt2));
    }

    // Tests equals when preferredName differs
    @Test
    public void testEquals_differentPreferredName_returnsFalse() {
        TestOption opt1 = new TestOption(1, true, "name1", "desc", Collections.EMPTY_SET, Collections.EMPTY_SET);
        TestOption opt2 = new TestOption(1, true, "name2", "desc", Collections.EMPTY_SET, Collections.EMPTY_SET);
        TestOption optNull = new TestOption(1, true, null, "desc", Collections.EMPTY_SET, Collections.EMPTY_SET);

        assertFalse(opt1.equals(opt2));
        assertFalse(opt1.equals(optNull));
        assertFalse(optNull.equals(opt1));
    }

    // Tests equals when description differs
    @Test
    public void testEquals_differentDescription_returnsFalse() {
        TestOption opt1 = new TestOption(1, true, "name", "desc1", Collections.EMPTY_SET, Collections.EMPTY_SET);
        TestOption opt2 = new TestOption(1, true, "name", "desc2", Collections.EMPTY_SET, Collections.EMPTY_SET);
        TestOption optNull = new TestOption(1, true, "name", null, Collections.EMPTY_SET, Collections.EMPTY_SET);

        assertFalse(opt1.equals(opt2));
        assertFalse(opt1.equals(optNull));
        assertFalse(optNull.equals(opt1));
    }

    // Tests equals when prefixes differ
    @Test
    public void testEquals_differentPrefixes_returnsFalse() {
        Set pref1 = new HashSet();
        pref1.add("-");
        Set pref2 = new HashSet();
        pref2.add("--");

        TestOption opt1 = new TestOption(1, true, "name", "desc", pref1, Collections.EMPTY_SET);
        TestOption opt2 = new TestOption(1, true, "name", "desc", pref2, Collections.EMPTY_SET);

        assertFalse(opt1.equals(opt2));
    }

    // Tests equals when triggers differ
    @Test
    public void testEquals_differentTriggers_returnsFalse() {
        Set trig1 = new HashSet();
        trig1.add("-a");
        Set trig2 = new HashSet();
        trig2.add("-b");

        TestOption opt1 = new TestOption(1, true, "name", "desc", Collections.EMPTY_SET, trig1);
        TestOption opt2 = new TestOption(1, true, "name", "desc", Collections.EMPTY_SET, trig2);

        assertFalse(opt1.equals(opt2));
    }

    // Tests hashCode calculation when preferredName and description are null
    @Test
    public void testHashCode_nullPreferredNameAndDescription() {
        TestOption opt1 = new TestOption(5, false, null, null, Collections.EMPTY_SET, Collections.EMPTY_SET);
        TestOption opt2 = new TestOption(5, true, null, null, Collections.EMPTY_SET, Collections.EMPTY_SET);

        assertEquals(opt1.hashCode(), opt2.hashCode());
    }

    // Tests compareTo when compared with another Option instance
    @Test
    public void testCompareTo_withOption() {
        TestOption opt1 = new TestOption(1, false, "alpha", "desc", Collections.EMPTY_SET, Collections.EMPTY_SET);
        TestOption opt2 = new TestOption(2, false, "beta", "desc", Collections.EMPTY_SET, Collections.EMPTY_SET);

        assertTrue(opt1.compareTo(opt2) < 0);
        assertTrue(opt2.compareTo(opt1) > 0);
        assertEquals(0, opt1.compareTo(opt1));
    }

    // Tests compareTo when compared with a non-Option instance
    @Test
    public void testCompareTo_withNonOption() {
        TestOption opt = new TestOption(1, false, "alpha", "desc", Collections.EMPTY_SET, Collections.EMPTY_SET);
        String other = "otherObject";
        int expected = opt.getClass().getName().compareTo(other.getClass().getName());

        assertEquals(expected, opt.compareTo(other));
    }

    // Tests checkPrefixes with empty prefixes set does nothing
    @Test
    public void testCheckPrefixes_emptyPrefixes_noValidationPerformed() {
        TestOption option = new TestOption(1, false, "invalid", "desc", Collections.EMPTY_SET, Collections.EMPTY_SET);
        option.triggerCheckPrefixes(Collections.EMPTY_SET);
    }

    // Tests checkPrefixes when all names and triggers start with a valid prefix
    @Test
    public void testCheckPrefixes_validPrefixes_success() {
        Set prefixes = new HashSet();
        prefixes.add("-");
        prefixes.add("--");

        Set triggers = new HashSet();
        triggers.add("-v");
        triggers.add("--verbose");

        TestOption option = new TestOption(1, false, "--verbose", "desc", prefixes, triggers);
        option.triggerCheckPrefixes(prefixes);
    }

    // Tests checkPrefixes when preferredName is null and triggers are valid
    @Test
    public void testCheckPrefixes_nullPreferredName_validTriggers() {
        Set prefixes = new HashSet();
        prefixes.add("-");

        Set triggers = new HashSet();
        triggers.add("-v");

        TestOption option = new TestOption(1, false, null, "desc", prefixes, triggers);
        option.triggerCheckPrefixes(prefixes);
    }

    // Tests checkPrefixes when preferredName is null and triggers contain invalid prefix
    @Test(expected = IllegalArgumentException.class)
    public void testCheckPrefixes_nullPreferredName_invalidTriggers() {
        Set prefixes = new HashSet();
        prefixes.add("-");

        Set triggers = new HashSet();
        triggers.add("v");

        TestOption option = new TestOption(1, false, null, "desc", prefixes, triggers);
        option.triggerCheckPrefixes(prefixes);
    }

    // Tests checkPrefixes throws IllegalArgumentException when preferredName lacks valid prefix
    @Test(expected = IllegalArgumentException.class)
    public void testCheckPrefixes_invalidPreferredName_throwsException() {
        Set prefixes = new HashSet();
        prefixes.add("-");

        Set triggers = new HashSet();
        triggers.add("-v");

        TestOption option = new TestOption(1, false, "verbose", "desc", prefixes, triggers);
        option.triggerCheckPrefixes(prefixes);
    }

    // Tests checkPrefixes throws IllegalArgumentException when trigger lacks valid prefix
    @Test(expected = IllegalArgumentException.class)
    public void testCheckPrefixes_invalidTrigger_throwsException() {
        Set prefixes = new HashSet();
        prefixes.add("-");

        Set triggers = new HashSet();
        triggers.add("-v");
        triggers.add("invalidTrigger");

        TestOption option = new TestOption(1, false, "-v", "desc", prefixes, triggers);
        option.triggerCheckPrefixes(prefixes);
    }
}