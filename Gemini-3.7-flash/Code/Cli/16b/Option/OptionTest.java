package org.apache.commons.cli2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import junit.framework.TestCase;

public class OptionTest extends TestCase {

    private TestOption option;

    private static class TestOption implements Option {
        private final int id;
        private final String preferredName;
        private final String description;
        private final boolean required;
        private final Set triggers = new HashSet();
        private final Set prefixes = new HashSet();
        private boolean processCalled = false;
        private boolean defaultsCalled = false;
        private boolean validateCalled = false;
        private boolean shouldThrowOnValidate = false;
        private boolean shouldThrowOnProcess = false;

        public TestOption(int id, String preferredName, String description, boolean required) {
            this.id = id;
            this.preferredName = preferredName;
            this.description = description;
            this.required = required;
            if (preferredName != null) {
                this.triggers.add(preferredName);
            }
        }

        public void process(WriteableCommandLine commandLine, ListIterator args) throws OptionException {
            this.processCalled = true;
            if (shouldThrowOnProcess) {
                throw new OptionException(this, "Process failed");
            }
            if (args != null && args.hasNext()) {
                args.next();
            }
        }

        public void defaults(WriteableCommandLine commandLine) {
            this.defaultsCalled = true;
        }

        public boolean canProcess(WriteableCommandLine commandLine, String argument) {
            return argument != null && triggers.contains(argument);
        }

        public boolean canProcess(WriteableCommandLine commandLine, ListIterator arguments) {
            if (arguments == null || !arguments.hasNext()) {
                return false;
            }
            String next = (String) arguments.next();
            arguments.previous();
            return canProcess(commandLine, next);
        }

        public Set getTriggers() {
            return Collections.unmodifiableSet(triggers);
        }

        public Set getPrefixes() {
            return Collections.unmodifiableSet(prefixes);
        }

        public void validate(WriteableCommandLine commandLine) throws OptionException {
            this.validateCalled = true;
            if (shouldThrowOnValidate) {
                throw new OptionException(this, "Validation failed");
            }
        }

        public List helpLines(int depth, Set helpSettings, Comparator comp) {
            return Collections.emptyList();
        }

        public void appendUsage(StringBuffer buffer, Set helpSettings, Comparator comp) {
            if (buffer != null && preferredName != null) {
                buffer.append(preferredName);
            }
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
    }

    protected void setUp() throws Exception {
        super.setUp();
        option = new TestOption(1, "--test", "Test option description", true);
    }

    // Tests getId returns correct identifier
    public void testGetId_positiveId_returnsAssignedId() {
        assertEquals(1, option.getId());
    }

    // Tests getPreferredName returns correct name
    public void testGetPreferredName_standardOption_returnsPreferredName() {
        assertEquals("--test", option.getPreferredName());
    }

    // Tests getDescription returns assigned description string
    public void testGetDescription_standardDescription_returnsDescription() {
        assertEquals("Test option description", option.getDescription());
    }

    // Tests isRequired when configured to true
    public void testIsRequired_requiredOption_returnsTrue() {
        assertTrue(option.isRequired());
    }

    // Tests isRequired when configured to false
    public void testIsRequired_optionalOption_returnsFalse() {
        TestOption optionalOption = new TestOption(2, "--optional", "Optional description", false);
        assertFalse(optionalOption.isRequired());
    }

    // Tests getTriggers contains expected trigger strings
    public void testGetTriggers_standardOption_containsPreferredName() {
        Set triggers = option.getTriggers();
        assertNotNull(triggers);
        assertTrue(triggers.contains("--test"));
        assertEquals(1, triggers.size());
    }

    // Tests getPrefixes returns empty set when no prefixes are added
    public void testGetPrefixes_noPrefixes_returnsEmptySet() {
        Set prefixes = option.getPrefixes();
        assertNotNull(prefixes);
        assertTrue(prefixes.isEmpty());
    }

    // Tests findOption with matching trigger string
    public void testFindOption_matchingTrigger_returnsOptionInstance() {
        Option found = option.findOption("--test");
        assertNotNull(found);
        assertEquals(option, found);
    }

    // Tests findOption with non-matching trigger string
    public void testFindOption_nonMatchingTrigger_returnsNull() {
        Option found = option.findOption("--unknown");
        assertNull(found);
    }

    // Tests findOption with null trigger string
    public void testFindOption_nullTrigger_returnsNull() {
        Option found = option.findOption(null);
        assertNull(found);
    }

    // Tests canProcess with matching string argument
    public void testCanProcess_matchingString_returnsTrue() {
        assertTrue(option.canProcess(null, "--test"));
    }

    // Tests canProcess with non-matching string argument
    public void testCanProcess_nonMatchingString_returnsFalse() {
        assertFalse(option.canProcess(null, "--other"));
    }

    // Tests canProcess with null string argument
    public void testCanProcess_nullString_returnsFalse() {
        assertFalse(option.canProcess(null, (String) null));
    }

    // Tests canProcess with ListIterator matching argument
    public void testCanProcess_matchingListIterator_returnsTrueAndPreservesState() {
        List args = new ArrayList();
        args.add("--test");
        args.add("value");
        ListIterator it = args.listIterator();

        assertTrue(option.canProcess(null, it));
        assertEquals(0, it.nextIndex());
    }

    // Tests canProcess with ListIterator non-matching argument
    public void testCanProcess_nonMatchingListIterator_returnsFalse() {
        List args = new ArrayList();
        args.add("--mismatch");
        ListIterator it = args.listIterator();

        assertFalse(option.canProcess(null, it));
        assertEquals(0, it.nextIndex());
    }

    // Tests canProcess with empty ListIterator
    public void testCanProcess_emptyListIterator_returnsFalse() {
        List args = new ArrayList();
        ListIterator it = args.listIterator();

        assertFalse(option.canProcess(null, it));
    }

    // Tests canProcess with null ListIterator
    public void testCanProcess_nullListIterator_returnsFalse() {
        assertFalse(option.canProcess(null, (ListIterator) null));
    }

    // Tests process consumes argument successfully
    public void testProcess_validArguments_consumesArgument() throws OptionException {
        List args = new ArrayList();
        args.add("--test");
        ListIterator it = args.listIterator();

        option.process(null, it);
        assertTrue(option.processCalled);
        assertEquals(1, it.nextIndex());
    }

    // Tests process throws OptionException on error
    public void testProcess_errorCondition_throwsOptionException() {
        option.shouldThrowOnProcess = true;
        try {
            option.process(null, null);
            fail("Expected OptionException to be thrown");
        } catch (OptionException e) {
            assertEquals(option, e.getOption());
        }
    }

    // Tests defaults invocation
    public void testDefaults_invocation_executesDefaults() {
        option.defaults(null);
        assertTrue(option.defaultsCalled);
    }

    // Tests validate passes on normal state
    public void testValidate_validCommandLine_success() throws OptionException {
        option.validate(null);
        assertTrue(option.validateCalled);
    }

    // Tests validate throws OptionException when invalid
    public void testValidate_invalidCommandLine_throwsOptionException() {
        option.shouldThrowOnValidate = true;
        try {
            option.validate(null);
            fail("Expected OptionException to be thrown");
        } catch (OptionException e) {
            assertEquals(option, e.getOption());
        }
    }

    // Tests appendUsage appends preferred name to buffer
    public void testAppendUsage_validBuffer_appendsName() {
        StringBuffer sb = new StringBuffer();
        option.appendUsage(sb, Collections.EMPTY_SET, null);
        assertEquals("--test", sb.toString());
    }

    // Tests appendUsage with null buffer does nothing
    public void testAppendUsage_nullBuffer_doesNothing() {
        option.appendUsage(null, Collections.EMPTY_SET, null);
    }

    // Tests helpLines returns list
    public void testHelpLines_defaultSettings_returnsList() {
        List lines = option.helpLines(0, Collections.EMPTY_SET, null);
        assertNotNull(lines);
        assertTrue(lines.isEmpty());
    }

    // Tests TestOption with null preferredName
    public void testOption_nullPreferredName_triggersEmpty() {
        TestOption nullNameOption = new TestOption(3, null, null, false);
        assertNull(nullNameOption.getPreferredName());
        assertNull(nullNameOption.getDescription());
        assertTrue(nullNameOption.getTriggers().isEmpty());

        StringBuffer sb = new StringBuffer();
        nullNameOption.appendUsage(sb, Collections.EMPTY_SET, null);
        assertEquals(0, sb.length());
    }
}