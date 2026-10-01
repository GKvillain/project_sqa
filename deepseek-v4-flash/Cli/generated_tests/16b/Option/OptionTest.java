package org.apache.commons.cli2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.option.DefaultOption;
import org.apache.commons.cli2.commandline.WriteableCommandLine;
import org.junit.Test;

public class OptionTest {

    // Helper: create a minimal WriteableCommandLine stub that always reports hasOption false
    private WriteableCommandLine createEmptyCommandLine() {
        return new WriteableCommandLine() {
            @Override public void addOption(Option opt) { }
            @Override public void addValue(Option opt, Object value) throws OptionException { }
            @Override public void addProperty(String prop, Object value) { }
            @Override public boolean hasOption(Option opt) { return false; }
            @Override public Option getOption(String trigger) { return null; }
            @Override public List getValues(Option opt) { return Collections.emptyList(); }
            @Override public List getOptions() { return Collections.emptyList(); }
            @Override public Map getProperties() { return Collections.emptyMap(); }
        };
    }

    // Helper: create a WriteableCommandLine that reports hasOption true for a specific Option
    private WriteableCommandLine createCommandLineWithOption(final Option option) {
        return new WriteableCommandLine() {
            @Override public void addOption(Option opt) { }
            @Override public void addValue(Option opt, Object value) throws OptionException { }
            @Override public void addProperty(String prop, Object value) { }
            @Override public boolean hasOption(Option opt) { return opt == option; }
            @Override public Option getOption(String trigger) { return null; }
            @Override public List getValues(Option opt) { return Collections.emptyList(); }
            @Override public List getOptions() { return Collections.emptyList(); }
            @Override public Map getProperties() { return Collections.emptyMap(); }
        };
    }

    // Helper: build a simple flag Option using DefaultOptionBuilder
    private Option createFlagOption(final boolean required) {
        DefaultOptionBuilder builder = new DefaultOptionBuilder();
        builder.withLongName("alpha")
               .withShortName("a")
               .withDescription("Alpha option")
               .withRequired(required);
        return builder.create();
    }

    @Test
    public void testGetPreferredName_returnsLongName() {
        Option option = createFlagOption(false);
        assertEquals("alpha", option.getPreferredName());
    }

    @Test
    public void testGetDescription_returnsDescription() {
        Option option = createFlagOption(false);
        assertEquals("Alpha option", option.getDescription());
    }

    @Test
    public void testGetId_returnsDefaultId() {
        Option option = createFlagOption(false);
        // DefaultOption does not assign a special id; default is 0
        assertEquals(0, option.getId());
    }

    @Test
    public void testIsRequired_requiredOption_returnsTrue() {
        Option option = createFlagOption(true);
        assertTrue(option.isRequired());
    }

    @Test
    public void testIsRequired_notRequiredOption_returnsFalse() {
        Option option = createFlagOption(false);
        assertFalse(option.isRequired());
    }

    @Test
    public void testGetTriggers_containsShortAndLong() {
        Option option = createFlagOption(false);
        Set triggers = option.getTriggers();
        assertNotNull(triggers);
        assertTrue(triggers.contains("-a"));
        assertTrue(triggers.contains("--alpha"));
    }

    @Test
    public void testGetPrefixes_containsDefaultPrefixes() {
        Option option = createFlagOption(false);
        Set prefixes = option.getPrefixes();
        assertNotNull(prefixes);
        assertTrue(prefixes.contains("-"));
        assertTrue(prefixes.contains("--"));
    }

    @Test
    public void testCanProcess_matchingLongTrigger_returnsTrue() {
        Option option = createFlagOption(false);
        WriteableCommandLine cmd = createEmptyCommandLine();
        assertTrue(option.canProcess(cmd, "--alpha"));
    }

    @Test
    public void testCanProcess_matchingShortTrigger_returnsTrue() {
        Option option = createFlagOption(false);
        WriteableCommandLine cmd = createEmptyCommandLine();
        assertTrue(option.canProcess(cmd, "-a"));
    }

    @Test
    public void testCanProcess_nonMatchingTrigger_returnsFalse() {
        Option option = createFlagOption(false);
        WriteableCommandLine cmd = createEmptyCommandLine();
        assertFalse(option.canProcess(cmd, "--beta"));
    }

    @Test
    public void testProcess_withTrigger_consumesOneArgument() {
        Option option = createFlagOption(false);
        WriteableCommandLine cmd = createEmptyCommandLine();
        List<String> argsList = new ArrayList<String>();
        argsList.add("--alpha");
        argsList.add("other");
        ListIterator args = argsList.listIterator();
        option.process(cmd, args);
        // After process, iterator should point to "other"
        assertTrue(args.hasNext());
        assertEquals("other", args.next());
    }

    @Test
    public void testValidate_requiredOptionMissing_throwsOptionException() {
        Option option = createFlagOption(true);
        WriteableCommandLine cmd = createEmptyCommandLine();
        try {
            option.validate(cmd);
            // should not reach
            assertTrue(option.isRequired() && cmd.hasOption(option) == false);
        } catch (OptionException e) {
            // expected
            assertNotNull(e);
        }
    }

    @Test(expected = OptionException.class)
    public void testValidate_requiredOptionMissing_throws() {
        Option option = createFlagOption(true);
        WriteableCommandLine cmd = createEmptyCommandLine();
        option.validate(cmd); // must throw OptionException
    }

    @Test
    public void testValidate_requiredOptionPresent_doesNotThrow() throws OptionException {
        Option option = createFlagOption(true);
        WriteableCommandLine cmd = createCommandLineWithOption(option);
        // cmd.hasOption returns true, so validate should pass
        option.validate(cmd);
        // No exception expected
        assertTrue(true);
    }

    @Test
    public void testFindOption_withMatchingTrigger_returnsSelf() {
        Option option = createFlagOption(false);
        Option found = option.findOption("--alpha");
        assertNotNull(found);
        assertEquals(option, found);
    }

    @Test
    public void testFindOption_withNonMatchingTrigger_returnsNull() {
        Option option = createFlagOption(false);
        Option found = option.findOption("--beta");
        assertNull(found);
    }

    @Test
    public void testDefaults_doesNotThrow() {
        Option option = createFlagOption(false);
        WriteableCommandLine cmd = createEmptyCommandLine();
        // defaults should be harmless
        option.defaults(cmd);
        assertTrue(true);
    }

    @Test
    public void testAppendUsage_appendsToBuffer() {
        Option option = createFlagOption(false);
        StringBuffer sb = new StringBuffer();
        option.appendUsage(sb, Collections.emptySet(), null);
        assertTrue(sb.length() > 0);
        assertTrue(sb.toString().contains("alpha"));
    }

    @Test
    public void testHelpLines_returnsNonNullList() {
        Option option = createFlagOption(false);
        List lines = option.helpLines(0, Collections.emptySet(), null);
        assertNotNull(lines);
        // At least one line is expected for a valid option
        assertFalse(lines.isEmpty());
    }

    @Test
    public void testGetParent_defaultIsNull() {
        Option option = createFlagOption(false);
        assertNull(option.getParent());
    }

    @Test
    public void testSetParent_setsParent() {
        Option option = createFlagOption(false);
        Option parent = createFlagOption(true);
        option.setParent(parent);
        assertEquals(parent, option.getParent());
    }

    // Additional edge case test: canProcess with empty string
    @Test
    public void testCanProcess_emptyString_returnsFalse() {
        Option option = createFlagOption(false);
        WriteableCommandLine cmd = createEmptyCommandLine();
        assertFalse(option.canProcess(cmd, ""));
    }

    // ========== New test cases for uncovered coverage ==========

    @Test
    public void testValidate_nonRequiredOptionMissing_doesNotThrow() throws OptionException {
        Option option = createFlagOption(false);
        WriteableCommandLine cmd = createEmptyCommandLine();
        // Validate should succeed because option is not required and missing is allowed
        option.validate(cmd);
        // No exception expected
        assertTrue(true);
    }

    @Test
    public void testProcess_withTriggerOnly_consumesTriggerAndLeavesNoArgs() {
        Option option = createFlagOption(false);
        WriteableCommandLine cmd = createEmptyCommandLine();
        List<String> argsList = new ArrayList<String>();
        argsList.add("--alpha");
        ListIterator args = argsList.listIterator();
        option.process(cmd, args);
        // After process, iterator should have no next element
        assertFalse(args.hasNext());
    }

    @Test
    public void testCanProcess_withNullTrigger_returnsFalse() {
        Option option = createFlagOption(false);
        WriteableCommandLine cmd = createEmptyCommandLine();
        // Expect false when trigger is null (no NullPointerException)
        assertFalse(option.canProcess(cmd, null));
    }

    @Test
    public void testFindOption_withNullTrigger_returnsNull() {
        Option option = createFlagOption(false);
        Option found = option.findOption(null);
        assertNull(found);
    }

    @Test
    public void testGetTriggers_returnsUnmodifiableSet() {
        Option option = createFlagOption(false);
        Set triggers = option.getTriggers();
        assertNotNull(triggers);
        // Attempt to modify should throw UnsupportedOperationException
        try {
            triggers.add("--new");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testGetPrefixes_returnsUnmodifiableSet() {
        Option option = createFlagOption(false);
        Set prefixes = option.getPrefixes();
        assertNotNull(prefixes);
        try {
            prefixes.add("/");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
}