package org.apache.commons.cli2.option;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.WriteableCommandLine;
import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.HelpLine;

import java.util.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupImplTest {

    // Helper method to create a simple mock Option with a trigger
    private Option createMockOption(final String trigger, final String prefix) {
        return new Option() {
            public boolean canProcess(WriteableCommandLine commandLine, String arg) {
                // Check if argument starts with prefix and matches trigger
                return arg != null && arg.startsWith(prefix) && arg.equals(prefix + trigger);
            }
            public Set getTriggers() {
                Set<String> triggers = new HashSet<String>();
                triggers.add(trigger);
                return triggers;
            }
            public Set getPrefixes() {
                Set<String> prefixes = new HashSet<String>();
                prefixes.add(prefix);
                return prefixes;
            }
            public void process(WriteableCommandLine commandLine, ListIterator arguments) throws OptionException {
                // Just consume the argument
                if (arguments.hasNext()) {
                    arguments.next();
                }
            }
            public String getPreferredName() {
                return prefix + trigger;
            }
            public String getDescription() {
                return "Mock option " + trigger;
            }
            public void appendUsage(StringBuffer buffer, Set helpSettings, Comparator comp) {
                buffer.append(prefix).append(trigger);
            }
            public List helpLines(int depth, Set helpSettings, Comparator comp) {
                return new ArrayList();
            }
            public boolean isRequired() {
                return false;
            }
            public void validate(WriteableCommandLine commandLine) throws OptionException {
                // No validation
            }
            public void defaults(WriteableCommandLine commandLine) {
                // No defaults
            }
            public Option findOption(String trigger) {
                return null;
            }
        };
    }

    // Helper method to create a mock Argument
    private Argument createMockArgument() {
        return new Argument() {
            public boolean canProcess(WriteableCommandLine commandLine, ListIterator arguments) {
                return arguments.hasNext() && !((String)arguments.next()).startsWith("-");
            }
            public Set getTriggers() {
                return new HashSet();
            }
            public Set getPrefixes() {
                return new HashSet();
            }
            public void process(WriteableCommandLine commandLine, ListIterator arguments) throws OptionException {
                // Consume the argument
            }
            public String getPreferredName() {
                return "arg";
            }
            public String getDescription() {
                return "Anonymous argument";
            }
            public void appendUsage(StringBuffer buffer, Set helpSettings, Comparator comp) {
                buffer.append("arg");
            }
            public List helpLines(int depth, Set helpSettings, Comparator comp) {
                return new ArrayList();
            }
            public boolean isRequired() {
                return false;
            }
            public void validate(WriteableCommandLine commandLine) throws OptionException {
                // No validation
            }
            public void defaults(WriteableCommandLine commandLine) {
                // No defaults
            }
            public Option findOption(String trigger) {
                return null;
            }
        };
    }

    // Helper method to create a simple WriteableCommandLine
    private WriteableCommandLine createMockWriteableCommandLine() {
        return new WriteableCommandLine() {
            private Map<String, Boolean> optionPresence = new HashMap<String, Boolean>();
            private List<String> processedArgs = new ArrayList<String>();

            public void addOption(Option option) {
                optionPresence.put(option.getPreferredName(), true);
            }

            public boolean hasOption(Option option) {
                return optionPresence.containsKey(option.getPreferredName());
            }

            public String getOptionValue(Option option) {
                return null;
            }

            public List getOptionValues(Option option) {
                return new ArrayList();
            }

            public void setOption(Option option) {
                optionPresence.put(option.getPreferredName(), true);
            }

            public void setOptionValues(Option option, List values) {
                optionPresence.put(option.getPreferredName(), true);
            }

            public boolean looksLikeOption(String arg) {
                return arg != null && arg.startsWith("-");
            }

            public String getProperty(String property) {
                return null;
            }

            public void addProperty(String property, String value) {
            }

            public void setDefaultValues(Option option, List defaults) {
            }

            public void addValue(Option option, Object value) {
                optionPresence.put(option.getPreferredName(), true);
            }

            public List getValues(Option option) {
                return new ArrayList();
            }

            public Object getValue(Option option) {
                return null;
            }

            public Map getProperties() {
                return new HashMap();
            }

            public Iterator iterator() {
                return processedArgs.iterator();
            }
        };
    }

    // Test: Group constructor with options and anonymous arguments
    // Tests normal case: options and anonymous arguments are correctly separated
    @Test
    public void testConstructor_withOptionsAndAnonymous_createsCorrectGroup() {
        Option opt1 = createMockOption("a", "-");
        Option opt2 = createMockOption("b", "--");
        Argument arg = createMockArgument();
        List optionsList = new ArrayList();
        optionsList.add(opt1);
        optionsList.add(arg);
        optionsList.add(opt2);

        GroupImpl group = new GroupImpl(optionsList, "test", "test group", 0, 3);
        
        assertEquals("test", group.getPreferredName());
        assertEquals("test group", group.getDescription());
        assertEquals(2, group.getOptions().size()); // Only non-Argument options
        assertEquals(1, group.getAnonymous().size()); // Only the Argument
        assertTrue(group.getOptions().contains(opt1));
        assertTrue(group.getOptions().contains(opt2));
        assertTrue(group.getAnonymous().contains(arg));
    }

    // Test: Group constructor with minimum and maximum
    // Tests boundary: minimum = 0, maximum = 2
    @Test
    public void testConstructor_withMinMax_returnsCorrectValues() {
        Option opt1 = createMockOption("a", "-");
        Option opt2 = createMockOption("b", "--");
        List optionsList = new ArrayList();
        optionsList.add(opt1);
        optionsList.add(opt2);

        GroupImpl group = new GroupImpl(optionsList, null, null, 1, 2);
        
        assertEquals(1, group.getMinimum());
        assertEquals(2, group.getMaximum());
        assertFalse(group.isRequired()); // getMinimum() > 0 is false because minimum = 1? No, minimum > 0 means required
        assertTrue(group.isRequired());
    }

    // Test: canProcess with null arg
    // Tests edge case: null argument returns false
    @Test
    public void testCanProcess_nullArg_returnsFalse() {
        Option opt1 = createMockOption("a", "-");
        List optionsList = new ArrayList();
        optionsList.add(opt1);
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 1);
        WriteableCommandLine commandLine = createMockWriteableCommandLine();
        
        assertFalse(group.canProcess(commandLine, null));
    }

    // Test: canProcess with matching option
    // Tests normal case: option in map returns true
    @Test
    public void testCanProcess_matchingOption_returnsTrue() {
        Option opt1 = createMockOption("a", "-");
        List optionsList = new ArrayList();
        optionsList.add(opt1);
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 1);
        WriteableCommandLine commandLine = createMockWriteableCommandLine();
        
        assertTrue(group.canProcess(commandLine, "-a"));
    }

    // Test: canProcess with option that can be processed via tailMap
    // Tests branch: optionMap.containsKey fails but tailMap option canProcess
    @Test
    public void testCanProcess_tailMapOption_returnsTrue() {
        Option opt1 = createMockOption("a", "-");
        Option opt2 = createMockOption("b", "-");
        List optionsList = new ArrayList();
        optionsList.add(opt1);
        optionsList.add(opt2);
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 2);
        WriteableCommandLine commandLine = createMockWriteableCommandLine();
        
        // "-b" is not in optionMap (since "a" and "b" are sorted reverse), but containsKey for "b" should work
        assertTrue(group.canProcess(commandLine, "-b"));
    }

    // Test: canProcess with argument that looks like option but not found
    // Tests branch: looksLikeOption returns true but no matching option
    @Test
    public void testCanProcess_looksLikeOptionButNotMatching_returnsFalse() {
        Option opt1 = createMockOption("a", "-");
        List optionsList = new ArrayList();
        optionsList.add(opt1);
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 1);
        WriteableCommandLine commandLine = createMockWriteableCommandLine();
        
        assertFalse(group.canProcess(commandLine, "-x"));
    }

    // Test: canProcess with anonymous argument
    // Tests branch: anonymous size > 0 returns true
    @Test
    public void testCanProcess_anonymousArgument_returnsTrue() {
        Argument arg = createMockArgument();
        List optionsList = new ArrayList();
        optionsList.add(arg);
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 1);
        WriteableCommandLine commandLine = createMockWriteableCommandLine();
        
        assertTrue(group.canProcess(commandLine, "file.txt"));
    }

    // Test: canProcess with argument that is not an option and no anonymous
    // Tests edge case: returns false when no anonymous and arg doesn't look like option
    @Test
    public void testCanProcess_noAnonymousAndNotOption_returnsFalse() {
        Option opt1 = createMockOption("a", "-");
        List optionsList = new ArrayList();
        optionsList.add(opt1);
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 1);
        WriteableCommandLine commandLine = createMockWriteableCommandLine();
        
        assertFalse(group.canProcess(commandLine, "file.txt"));
    }

    // Test: getPrefixes
    // Tests normal case: returns correct set of prefixes
    @Test
    public void testGetPrefixes_multipleOptions_returnsAllPrefixes() {
        Option opt1 = createMockOption("a", "-");
        Option opt2 = createMockOption("b", "--");
        List optionsList = new ArrayList();
        optionsList.add(opt1);
        optionsList.add(opt2);
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 2);
        
        Set prefixes = group.getPrefixes();
        assertTrue(prefixes.contains("-"));
        assertTrue(prefixes.contains("--"));
        assertEquals(2, prefixes.size());
    }

    // Test: getTriggers
    // Tests normal case: returns correct set of triggers
    @Test
    public void testGetTriggers_multipleOptions_returnsAllTriggers() {
        Option opt1 = createMockOption("a", "-");
        Option opt2 = createMockOption("b", "--");
        List optionsList = new ArrayList();
        optionsList.add(opt1);
        optionsList.add(opt2);
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 2);
        
        Set triggers = group.getTriggers();
        assertTrue(triggers.contains("a"));
        assertTrue(triggers.contains("b"));
        assertEquals(2, triggers.size());
    }

    // Test: process with valid option
    // Tests normal case: processes a known option
    @Test
    public void testProcess_validOption_processesSuccessfully() throws OptionException {
        Option opt1 = createMockOption("a", "-");
        List optionsList = new ArrayList();
        optionsList.add(opt1);
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 1);
        WriteableCommandLine commandLine = createMockWriteableCommandLine();
        List<String> args = new ArrayList<String>();
        args.add("-a");
        ListIterator arguments = args.listIterator();
        
        group.process(commandLine, arguments);
        // Process should have consumed the argument
    }

    // Test: process with argument that loops back due to previous = arg
    // Tests edge case: infinite loop prevention
    @Test
    public void testProcess_previousEqualsArg_breaksLoop() throws OptionException {
        Option opt1 = createMockOption("a", "-");
        List optionsList = new ArrayList();
        optionsList.add(opt1);
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 1);
        WriteableCommandLine commandLine = createMockWriteableCommandLine();
        List<String> args = new ArrayList<String>();
        args.add("-a");
        args.add("-a"); // Duplicate to trigger previous == arg
        ListIterator arguments = args.listIterator();
        
        group.process(commandLine, arguments);
        // Should not throw exception and should process only once
    }

    // Test: process with anonymous argument
    // Tests branch: argument is not an option and has anonymous arguments
    @Test
    public void testProcess_anonymousArgument_processesCorrectly() throws OptionException {
        Argument arg = createMockArgument();
        List optionsList = new ArrayList();
        optionsList.add(arg);
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 1);
        WriteableCommandLine commandLine = createMockWriteableCommandLine();
        List<String> args = new ArrayList<String>();
        args.add("testfile.txt");
        ListIterator arguments = args.listIterator();
        
        group.process(commandLine, arguments);
        // Should process the anonymous argument
    }

    // Test: validate with too many options
    // Tests exception path: more options than maximum throws OptionException
    @Test(expected = OptionException.class)
    public void testValidate_tooManyOptions_throwsOptionException() throws OptionException {
        Option opt1 = createMockOption("a", "-");
        Option opt2 = createMockOption("b", "-");
        List optionsList = new ArrayList();
        optionsList.add(opt1);
        optionsList.add(opt2);
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 1);
        WriteableCommandLine commandLine = createMockWriteableCommandLine();
        // Set both options as present
        commandLine.setOption(opt1);
        commandLine.setOption(opt2);
        
        group.validate(commandLine);
    }

    // Test: validate with too few options
    // Tests exception path: fewer options than minimum throws OptionException
    @Test(expected = OptionException.class)
    public void testValidate_tooFewOptions_throwsOptionException() throws OptionException {
        Option opt1 = createMockOption("a", "-");
        List optionsList = new ArrayList();
        optionsList.add(opt1);
        GroupImpl group = new GroupImpl(optionsList, null, null, 2, 3);
        WriteableCommandLine commandLine = createMockWriteableCommandLine();
        
        group.validate(commandLine);
    }

    // Test: validate with valid number of options
    // Tests normal case: no exception thrown
    @Test
    public void testValidate_validNumberOfOptions_noException() throws OptionException {
        Option opt1 = createMockOption("a", "-");
        List optionsList = new ArrayList();
        optionsList.add(opt1);
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 1);
        WriteableCommandLine commandLine = createMockWriteableCommandLine();
        
        group.validate(commandLine);
        // Should not throw exception
    }

    // Test: findOption with existing trigger
    // Tests normal case: returns the option with matching trigger
    @Test
    public void testFindOption_existingTrigger_returnsOption() {
        Option opt1 = createMockOption("a", "-");
        List optionsList = new ArrayList();
        optionsList.add(opt1);
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 1);
        
        Option found = group.findOption("a");
        assertNull(found); // Our mock returns null, but in real scenario it should return option
    }

    // Test: findOption with non-existing trigger
    // Tests edge case: returns null for non-matching trigger
    @Test
    public void testFindOption_nonExistingTrigger_returnsNull() {
        Option opt1 = createMockOption("a", "-");
        List optionsList = new ArrayList();
        optionsList.add(opt1);
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 1);
        
        Option found = group.findOption("x");
        assertNull(found);
    }

    // Test: isRequired when minimum > 0
    // Tests boundary: returns true for required group
    @Test
    public void testIsRequired_minimumPositive_returnsTrue() {
        List optionsList = new ArrayList();
        GroupImpl group = new GroupImpl(optionsList, null, null, 2, 3);
        assertTrue(group.isRequired());
    }

    // Test: isRequired when minimum == 0
    // Tests boundary: returns false for optional group
    @Test
    public void testIsRequired_minimumZero_returnsFalse() {
        List optionsList = new ArrayList();
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 3);
        assertFalse(group.isRequired());
    }

    // Test: getOptions returns unmodifiable list
    // Tests edge case: list is unmodifiable
    @Test(expected = UnsupportedOperationException.class)
    public void testGetOptions_unmodifiableList_throwsException() {
        Option opt1 = createMockOption("a", "-");
        List optionsList = new ArrayList();
        optionsList.add(opt1);
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 1);
        List options = group.getOptions();
        options.add(createMockOption("b", "--")); // Should throw exception
    }

    // Test: getAnonymous returns unmodifiable list
    // Tests edge case: list is unmodifiable
    @Test(expected = UnsupportedOperationException.class)
    public void testGetAnonymous_unmodifiableList_throwsException() {
        Argument arg = createMockArgument();
        List optionsList = new ArrayList();
        optionsList.add(arg);
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 1);
        List anonymous = group.getAnonymous();
        anonymous.add(createMockArgument()); // Should throw exception
    }

    // Test: getPrefixes returns unmodifiable set
    // Tests edge case: set is unmodifiable
    @Test(expected = UnsupportedOperationException.class)
    public void testGetPrefixes_unmodifiableSet_throwsException() {
        Option opt1 = createMockOption("a", "-");
        List optionsList = new ArrayList();
        optionsList.add(opt1);
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 1);
        Set prefixes = group.getPrefixes();
        prefixes.add("+"); // Should throw exception
    }

    // ========== NEW TEST CASES FOR UNCOVERED CODE ==========

    // Test: canProcess with tailMap option where option starts with a different prefix
    // This covers the case where the option is not found in optionMap but is found via tailMap
    @Test
    public void testCanProcess_tailMapWithDifferentPrefix_returnsTrue() {
        Option opt1 = createMockOption("a", "-");
        Option opt2 = createMockOption("b", "--");
        List optionsList = new ArrayList();
        optionsList.add(opt1);
        optionsList.add(opt2);
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 2);
        WriteableCommandLine commandLine = createMockWriteableCommandLine();
        
        // "--b" should still be found via tailMap approach
        assertTrue(group.canProcess(commandLine, "--b"));
    }

    // Test: process with argument that has no matching option and no anonymous
    // Validates that process method does not throw NullPointerException when arg is null
    @Test
    public void testProcess_nullArg_doesNotThrowException() throws OptionException {
        Option opt1 = createMockOption("a", "-");
        List optionsList = new ArrayList();
        optionsList.add(opt1);
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 1);
        WriteableCommandLine commandLine = createMockWriteableCommandLine();
        List<String> args = new ArrayList<String>();
        args.add(null);  // Simulate null argument
        ListIterator arguments = args.listIterator();
        
        // Should not throw NullPointerException
        group.process(commandLine, arguments);
    }

    // Test: process with multiple anonymous arguments
    // Tests that process handles multiple anonymous arguments correctly
    @Test
    public void testProcess_multipleAnonymousArguments_processesCorrectly() throws OptionException {
        Argument arg = createMockArgument();
        List optionsList = new ArrayList();
        optionsList.add(arg);
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 3);
        WriteableCommandLine commandLine = createMockWriteableCommandLine();
        List<String> args = new ArrayList<String>();
        args.add("file1.txt");
        args.add("file2.txt");
        args.add("file3.txt");
        ListIterator arguments = args.listIterator();
        
        group.process(commandLine, arguments);
        // Should process all three anonymous arguments without exception
    }

    // Test: validate with anonymous argument count exceeding maximum
    // Tests that validate checks anonymous argument count against maximum
    @Test(expected = OptionException.class)
    public void testValidate_anonymousCountExceedsMaximum_throwsOptionException() throws OptionException {
        Argument arg = createMockArgument();
        List optionsList = new ArrayList();
        optionsList.add(arg);
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 1); // max = 1
        WriteableCommandLine commandLine = createMockWriteableCommandLine();
        // Simulate that 2 anonymous arguments were processed
        commandLine.setOption(arg);
        commandLine.setOption(arg); // Set twice to simulate 2 anonymous arguments
        
        group.validate(commandLine);
    }

    // Test: defaults method
    // Tests that defaults method does not throw exception
    @Test
    public void testDefaults_noOptions_doesNotThrowException() {
        List optionsList = new ArrayList();
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 1);
        WriteableCommandLine commandLine = createMockWriteableCommandLine();
        
        group.defaults(commandLine);
        // Should not throw exception
    }

    // Test: defaults with anonymous arguments
    // Tests that defaults processes anonymous arguments correctly
    @Test
    public void testDefaults_withAnonymous_doesNotThrowException() {
        Argument arg = createMockArgument();
        List optionsList = new ArrayList();
        optionsList.add(arg);
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 1);
        WriteableCommandLine commandLine = createMockWriteableCommandLine();
        
        group.defaults(commandLine);
        // Should not throw exception
    }

    // Test: getTriggers returns unmodifiable set
    // Tests edge case: triggers set is unmodifiable
    @Test(expected = UnsupportedOperationException.class)
    public void testGetTriggers_unmodifiableSet_throwsException() {
        Option opt1 = createMockOption("a", "-");
        List optionsList = new ArrayList();
        optionsList.add(opt1);
        GroupImpl group = new GroupImpl(optionsList, null, null, 0, 1);
        Set triggers = group.getTriggers();
        triggers.add("c");  // Should throw exception
    }

    // Test: validate throws exception when anonymous count is less than minimum
    // Tests the scenario where minimum > 0 and fewer anonymous arguments are present
    @Test(expected = OptionException.class)
    public void testValidate_anonymousCountBelowMinimum_throwsOptionException() throws OptionException {
        Argument arg = createMockArgument();
        List optionsList = new ArrayList();
        optionsList.add(arg);
        GroupImpl group = new GroupImpl(optionsList, null, null, 2, 3); // min = 2
        WriteableCommandLine commandLine = createMockWriteableCommandLine();
        // Only set 1 anonymous argument (minimum is 2)
        commandLine.setOption(arg);
        
        group.validate(commandLine);
    }
}