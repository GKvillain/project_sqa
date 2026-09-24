package org.apache.commons.cli2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class OptionTest {

    private TestOption option;

    @Before
    public void setUp() {
        option = new TestOption(1, "--test", "Test Option description", true);
    }

    // Tests getId returns configured id
    @Test
    public void testGetId_validId_returnsCorrectId() {
        assertEquals(1, option.getId());
    }

    // Tests getPreferredName returns configured preferred name
    @Test
    public void testGetPreferredName_validName_returnsCorrectName() {
        assertEquals("--test", option.getPreferredName());
    }

    // Tests getDescription returns configured description
    @Test
    public void testGetDescription_validDescription_returnsCorrectDescription() {
        assertEquals("Test Option description", option.getDescription());
    }

    // Tests isRequired returns configured required status
    @Test
    public void testIsRequired_trueValue_returnsTrue() {
        assertTrue(option.isRequired());
    }

    // Tests isRequired for optional option
    @Test
    public void testIsRequired_falseValue_returnsFalse() {
        TestOption optionalOpt = new TestOption(2, "-o", "Optional", false);
        assertFalse(optionalOpt.isRequired());
    }

    // Tests getTriggers contains expected triggers
    @Test
    public void testGetTriggers_standardOption_returnsNonEmptySet() {
        Set triggers = option.getTriggers();
        assertNotNull(triggers);
        assertTrue(triggers.contains("--test"));
        assertTrue(triggers.contains("-t"));
    }

    // Tests getPrefixes returns configured prefixes
    @Test
    public void testGetPrefixes_standardOption_returnsPrefixSet() {
        Set prefixes = option.getPrefixes();
        assertNotNull(prefixes);
        assertTrue(prefixes.contains("-"));
        assertTrue(prefixes.contains("--"));
    }

    // Tests canProcess with matching string argument
    @Test
    public void testCanProcess_matchingString_returnsTrue() {
        assertTrue(option.canProcess(null, "--test"));
        assertTrue(option.canProcess(null, "-t"));
    }

    // Tests canProcess with non-matching string argument
    @Test
    public void testCanProcess_nonMatchingString_returnsFalse() {
        assertFalse(option.canProcess(null, "--other"));
        assertFalse(option.canProcess(null, "-x"));
    }

    // Tests canProcess with null string argument
    @Test
    public void testCanProcess_nullString_returnsFalse() {
        assertFalse(option.canProcess(null, (String) null));
    }

    // Tests canProcess with ListIterator matching argument
    @Test
    public void testCanProcess_matchingIterator_returnsTrueAndPreservesIndex() {
        List args = new ArrayList();
        args.add("--test");
        args.add("value");
        ListIterator it = args.listIterator();

        int initialIndex = it.nextIndex();
        boolean result = option.canProcess(null, it);

        assertTrue(result);
        assertEquals(initialIndex, it.nextIndex());
    }

    // Tests canProcess with ListIterator non-matching argument
    @Test
    public void testCanProcess_nonMatchingIterator_returnsFalseAndPreservesIndex() {
        List args = new ArrayList();
        args.add("--unknown");
        ListIterator it = args.listIterator();

        int initialIndex = it.nextIndex();
        boolean result = option.canProcess(null, it);

        assertFalse(result);
        assertEquals(initialIndex, it.nextIndex());
    }

    // Tests findOption with matching trigger
    @Test
    public void testFindOption_matchingTrigger_returnsSelf() {
        Option found = option.findOption("--test");
        assertNotNull(found);
        assertEquals(option, found);
    }

    // Tests findOption with non-matching trigger
    @Test
    public void testFindOption_nonMatchingTrigger_returnsNull() {
        Option found = option.findOption("--unknown");
        assertNull(found);
    }

    // Tests findOption with null trigger
    @Test
    public void testFindOption_nullTrigger_returnsNull() {
        Option found = option.findOption(null);
        assertNull(found);
    }

    // Tests appendUsage writes preferred name
    @Test
    public void testAppendUsage_validBuffer_appendsOptionName() {
        StringBuffer buffer = new StringBuffer();
        option.appendUsage(buffer, Collections.EMPTY_SET, null);
        assertEquals("--test", buffer.toString());
    }

    // Tests helpLines returns list of help entries
    @Test
    public void testHelpLines_defaultSettings_returnsHelpLineList() {
        List lines = option.helpLines(0, Collections.EMPTY_SET, null);
        assertNotNull(lines);
        assertEquals(1, lines.size());
    }

    // Tests process method consumes iterator element
    @Test
    public void testProcess_validArguments_advancesIterator() throws OptionException {
        List args = new ArrayList();
        args.add("--test");
        args.add("extra");
        ListIterator it = args.listIterator();

        option.process(null, it);
        assertEquals(1, it.nextIndex());
        assertEquals("extra", it.next());
    }

    // Tests process method with invalid argument throws OptionException
    @Test(expected = OptionException.class)
    public void testProcess_invalidArguments_throwsOptionException() throws OptionException {
        List args = new ArrayList();
        args.add("--invalid");
        ListIterator it = args.listIterator();

        option.process(null, it);
    }

    // Tests checkPrefixes with matching prefixes returns true
    @Test
    public void testCheckPrefixes_matchingPrefixes_returnsTrue() {
        Set prefixes = new HashSet();
        prefixes.add("-");
        assertTrue(option.checkPrefixes(prefixes));
    }

    // Tests checkPrefixes with non-matching prefixes returns false
    @Test
    public void testCheckPrefixes_nonMatchingPrefixes_returnsFalse() {
        Set prefixes = new HashSet();
        prefixes.add("/");
        prefixes.add("+");
        assertFalse(option.checkPrefixes(prefixes));
    }

    // Tests checkPrefixes with null or empty set returns false
    @Test
    public void testCheckPrefixes_emptyOrNullPrefixes_returnsFalse() {
        assertFalse(option.checkPrefixes(null));
        assertFalse(option.checkPrefixes(Collections.EMPTY_SET));
    }

    // Tests canProcess with null or empty ListIterator returns false
    @Test
    public void testCanProcess_nullOrEmptyIterator_returnsFalse() {
        assertFalse(option.canProcess(null, (ListIterator) null));
        List emptyList = new ArrayList();
        assertFalse(option.canProcess(null, emptyList.listIterator()));
    }

    // Tests defaults and validate methods do not throw unexpected exceptions
    @Test
    public void testDefaultsAndValidate_executesNormally() throws OptionException {
        option.defaults(null);
        option.validate(null);
    }

    // Tests process method with empty iterator throws OptionException
    @Test(expected = OptionException.class)
    public void testProcess_emptyIterator_throwsOptionException() throws OptionException {
        List emptyList = new ArrayList();
        option.process(null, emptyList.listIterator());
    }

    // Test implementation of Option interface for contract testing
    private static class TestOption implements Option {
        private final int id;
        private final String preferredName;
        private final String description;
        private final boolean required;
        private final Set triggers = new HashSet();
        private final Set prefixes = new HashSet();

        public TestOption(int id, String preferredName, String description, boolean required) {
            this.id = id;
            this.preferredName = preferredName;
            this.description = description;
            this.required = required;
            this.triggers.add(preferredName);
            this.triggers.add("-t");
            this.prefixes.add("-");
            this.prefixes.add("--");
        }

        public void process(WriteableCommandLine commandLine, ListIterator args) throws OptionException {
            if (!args.hasNext()) {
                throw new OptionException(this, "No arguments to process");
            }
            String next = (String) args.next();
            if (!triggers.contains(next)) {
                args.previous();
                throw new OptionException(this, "Unexpected argument: " + next);
            }
        }

        public void defaults(WriteableCommandLine commandLine) {
        }

        public boolean canProcess(WriteableCommandLine commandLine, String argument) {
            return argument != null && triggers.contains(argument);
        }

        public boolean canProcess(WriteableCommandLine commandLine, ListIterator arguments) {
            if (arguments != null && arguments.hasNext()) {
                String next = (String) arguments.next();
                arguments.previous();
                return canProcess(commandLine, next);
            }
            return false;
        }

        public Set getTriggers() {
            return triggers;
        }

        public Set getPrefixes() {
            return prefixes;
        }

        public void validate(WriteableCommandLine commandLine) throws OptionException {
        }

        public List helpLines(int depth, Set helpSettings, Comparator comp) {
            List list = new ArrayList();
            list.add(preferredName + " : " + description);
            return list;
        }

        public void appendUsage(StringBuffer buffer, Set helpSettings, Comparator comp) {
            buffer.append(preferredName);
        }

        public String getPreferredName() {
            return preferredName;
        }

        public String getDescription() {
            return description;
        }

        public int getId() {
            return id;
        }

        public Option findOption(String trigger) {
            if (trigger != null && triggers.contains(trigger)) {
                return this;
            }
            return null;
        }

        public boolean isRequired() {
            return required;
        }

        public boolean checkPrefixes(Set prefixes) {
            if (prefixes == null) {
                return false;
            }
            for (Iterator i = triggers.iterator(); i.hasNext();) {
                String trigger = (String) i.next();
                for (Iterator j = prefixes.iterator(); j.hasNext();) {
                    String prefix = (String) j.next();
                    if (trigger.startsWith(prefix)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}