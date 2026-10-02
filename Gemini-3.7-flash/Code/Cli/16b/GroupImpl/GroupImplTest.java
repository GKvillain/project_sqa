package org.apache.commons.cli2.option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.WriteableCommandLine;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.WriteableCommandLineImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GroupImplTest {

    private DefaultOption optionA;
    private DefaultOption optionB;
    private Argument argument1;

    @Before
    public void setUp() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final ArgumentBuilder abuilder = new ArgumentBuilder();

        optionA = obuilder.withShortName("a").withLongName("optA").withDescription("Option A").create();
        optionB = obuilder.withShortName("b").withLongName("optB").withDescription("Option B").create();
        argument1 = abuilder.withName("target").withMinimum(1).withMaximum(1).create();
    }

    // Tests constructor separation of options and anonymous arguments
    @Test
    public void testConstructor_separatesOptionsAndAnonymousArguments() {
        final List options = new ArrayList();
        options.add(optionA);
        options.add(argument1);

        final GroupImpl group = new GroupImpl(options, "testGroup", "Test Group Description", 0, 1);

        assertEquals("testGroup", group.getPreferredName());
        assertEquals("Test Group Description", group.getDescription());
        assertEquals(0, group.getMinimum());
        assertEquals(1, group.getMaximum());
        assertFalse(group.isRequired());

        assertEquals(1, group.getOptions().size());
        assertTrue(group.getOptions().contains(optionA));
        assertEquals(1, group.getAnonymous().size());
        assertTrue(group.getAnonymous().contains(argument1));
    }

    // Tests isRequired when minimum is greater than 0
    @Test
    public void testIsRequired_minimumGreaterThanZero_returnsTrue() {
        final List options = new ArrayList();
        options.add(optionA);

        final GroupImpl group = new GroupImpl(options, "requiredGroup", "Required", 1, 2);
        assertTrue(group.isRequired());
    }

    // Tests canProcess with null argument returns false
    @Test
    public void testCanProcess_nullArg_returnsFalse() {
        final List options = new ArrayList();
        options.add(optionA);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        assertFalse(group.canProcess(commandLine, (String) null));
    }

    // Tests canProcess with matching trigger returns true
    @Test
    public void testCanProcess_matchingTrigger_returnsTrue() {
        final List options = new ArrayList();
        options.add(optionA);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        assertTrue(group.canProcess(commandLine, "-a"));
        assertTrue(group.canProcess(commandLine, "--optA"));
    }

    // Tests canProcess with anonymous argument present returns true for non-option token
    @Test
    public void testCanProcess_anonymousArgument_returnsTrueForNonOption() {
        final List options = new ArrayList();
        options.add(argument1);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        assertTrue(group.canProcess(commandLine, "someValue"));
    }

    // Tests canProcess with no anonymous argument returns false for non-option token
    @Test
    public void testCanProcess_noAnonymousArgument_returnsFalseForNonOption() {
        final List options = new ArrayList();
        options.add(optionA);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        assertFalse(group.canProcess(commandLine, "someValue"));
    }

    // Tests getPrefixes and getTriggers
    @Test
    public void testGetPrefixesAndTriggers_returnsExpectedSets() {
        final List options = new ArrayList();
        options.add(optionA);
        options.add(optionB);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 2);

        final Set triggers = group.getTriggers();
        assertTrue(triggers.contains("-a"));
        assertTrue(triggers.contains("--optA"));
        assertTrue(triggers.contains("-b"));
        assertTrue(triggers.contains("--optB"));

        final Set prefixes = group.getPrefixes();
        assertTrue(prefixes.contains("-"));
        assertTrue(prefixes.contains("--"));
    }

    // Tests findOption finds existing option trigger and returns null for unknown
    @Test
    public void testFindOption_existingAndUnknownTriggers() {
        final List options = new ArrayList();
        options.add(optionA);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);

        assertEquals(optionA, group.findOption("-a"));
        assertEquals(optionA, group.findOption("--optA"));
        assertNull(group.findOption("-unknown"));
    }

    // Tests process parses matching options into command line
    @Test
    public void testProcess_validOption_processedCorrectly() throws OptionException {
        final List options = new ArrayList();
        options.add(optionA);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);

        final List args = new ArrayList();
        args.add("-a");
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        final ListIterator iterator = args.listIterator();

        group.process(commandLine, iterator);

        assertTrue(commandLine.hasOption("-a"));
        assertFalse(iterator.hasNext());
    }

    // Tests process stops when encountering an unknown option
    @Test
    public void testProcess_unknownOption_stopsAndRollsBack() throws OptionException {
        final List options = new ArrayList();
        options.add(optionA);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);

        final List args = new ArrayList();
        args.add("-unknown");
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        final ListIterator iterator = args.listIterator();

        group.process(commandLine, iterator);

        assertFalse(commandLine.hasOption("-a"));
        assertEquals("-unknown", iterator.next());
    }

    // Tests process with anonymous arguments processes values
    @Test
    public void testProcess_anonymousArgument_processesValues() throws OptionException {
        final List options = new ArrayList();
        options.add(argument1);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);

        final List args = new ArrayList();
        args.add("file.txt");
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        final ListIterator iterator = args.listIterator();

        group.process(commandLine, iterator);

        assertTrue(commandLine.hasOption("target"));
        assertEquals("file.txt", commandLine.getValue("target"));
    }

    // Tests validate passes when constraints are met
    @Test
    public void testValidate_validOptionCount_passes() throws OptionException {
        final List options = new ArrayList();
        options.add(optionA);
        options.add(optionB);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 1, 2);

        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        commandLine.addOption(optionA);

        group.validate(commandLine);
    }

    // Tests validate throws exception when options count is below minimum
    @Test(expected = OptionException.class)
    public void testValidate_tooFewOptions_throwsException() throws OptionException {
        final List options = new ArrayList();
        options.add(optionA);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 1, 1);

        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        group.validate(commandLine);
    }

    // Tests validate throws exception when options count exceeds maximum
    @Test(expected = OptionException.class)
    public void testValidate_tooManyOptions_throwsException() throws OptionException {
        final List options = new ArrayList();
        options.add(optionA);
        options.add(optionB);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);

        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        commandLine.addOption(optionA);
        commandLine.addOption(optionB);

        group.validate(commandLine);
    }

    // Tests appendUsage with default settings
    @Test
    public void testAppendUsage_defaultSettings_appendsCorrectly() {
        final List options = new ArrayList();
        options.add(optionA);
        options.add(optionB);
        final GroupImpl group = new GroupImpl(options, "groupName", "Group Description", 0, 1);

        final StringBuffer buffer = new StringBuffer();
        final Set helpSettings = new HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_NAME);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        helpSettings.add(DisplaySetting.DISPLAY_OPTIONAL);

        group.appendUsage(buffer, helpSettings, null);

        final String usage = buffer.toString();
        assertTrue(usage.startsWith("["));
        assertTrue(usage.endsWith("]"));
        assertTrue(usage.contains("groupName"));
        assertTrue(usage.contains("-a"));
        assertTrue(usage.contains("-b"));
    }

    // Tests appendUsage with comparator sorting
    @Test
    public void testAppendUsage_withComparator_sortsOptions() {
        final List options = new ArrayList();
        options.add(optionB);
        options.add(optionA);
        final GroupImpl group = new GroupImpl(options, null, "Group Description", 1, 2);

        final StringBuffer buffer = new StringBuffer();
        final Set helpSettings = new HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);

        final Comparator comp = new Comparator() {
            public int compare(final Object o1, final Object o2) {
                final Option opt1 = (Option) o1;
                final Option opt2 = (Option) o2;
                return opt1.getPreferredName().compareTo(opt2.getPreferredName());
            }
        };

        group.appendUsage(buffer, helpSettings, comp, ", ");

        final String usage = buffer.toString();
        assertTrue(usage.indexOf("-a") < usage.indexOf("-b"));
    }

    // Tests helpLines generation with display settings
    @Test
    public void testHelpLines_withSettings_returnsHelpLines() {
        final List options = new ArrayList();
        options.add(optionA);
        options.add(argument1);
        final GroupImpl group = new GroupImpl(options, "groupName", "Group Description", 0, 1);

        final Set helpSettings = new HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_NAME);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);

        final List lines = group.helpLines(0, helpSettings, null);

        assertNotNull(lines);
        assertFalse(lines.isEmpty());
    }

    // Tests defaults propagation to child options and anonymous arguments
    @Test
    public void testDefaults_propagatesToChildren() {
        final List options = new ArrayList();
        options.add(optionA);
        options.add(argument1);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);

        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        group.defaults(commandLine);

        assertNotNull(commandLine);
    }

    // Tests canProcess using ListIterator overload
    @Test
    public void testCanProcess_listIterator() {
        final List options = new ArrayList();
        options.add(optionA);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        final List args = new ArrayList();
        args.add("-a");
        final ListIterator it = args.listIterator();

        assertTrue(group.canProcess(commandLine, it));
        // Verify iterator position wasn't modified after canProcess
        assertTrue(it.hasNext());
        assertEquals("-a", it.next());

        final List emptyArgs = new ArrayList();
        assertFalse(group.canProcess(commandLine, emptyArgs.listIterator()));
    }

    // Tests canProcess with nested group
    @Test
    public void testCanProcess_nestedGroup() {
        final List childOptions = new ArrayList();
        childOptions.add(optionA);
        final GroupImpl childGroup = new GroupImpl(childOptions, "childGroup", "childDesc", 0, 1);

        final List parentOptions = new ArrayList();
        parentOptions.add(childGroup);
        final GroupImpl parentGroup = new GroupImpl(parentOptions, "parentGroup", "parentDesc", 0, 1);
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(parentGroup, new ArrayList());

        assertTrue(parentGroup.canProcess(commandLine, "-a"));
        assertFalse(parentGroup.canProcess(commandLine, "-b"));
    }

    // Tests findOption in nested group hierarchy
    @Test
    public void testFindOption_nestedGroup() {
        final List childOptions = new ArrayList();
        childOptions.add(optionA);
        final GroupImpl childGroup = new GroupImpl(childOptions, "childGroup", "childDesc", 0, 1);

        final List parentOptions = new ArrayList();
        parentOptions.add(childGroup);
        final GroupImpl parentGroup = new GroupImpl(parentOptions, "parentGroup", "parentDesc", 0, 1);

        assertEquals(optionA, parentGroup.findOption("-a"));
    }

    // Tests isSelected method
    @Test
    public void testIsSelected() {
        final List options = new ArrayList();
        options.add(optionA);
        options.add(argument1);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 2);

        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        assertFalse(group.isSelected(commandLine));

        commandLine.addOption(optionA);
        assertTrue(group.isSelected(commandLine));
    }

    // Tests toString method calls appendUsage
    @Test
    public void testToString_returnsNonEmptyString() {
        final List options = new ArrayList();
        options.add(optionA);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);

        final String result = group.toString();
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    // Tests checkPrefixes method
    @Test
    public void testCheckPrefixes() {
        final List options = new ArrayList();
        options.add(optionA);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);

        final Set validPrefixes = new HashSet();
        validPrefixes.add("-");
        validPrefixes.add("--");
        group.checkPrefixes(validPrefixes);
    }

    // Tests checkPrefixes with invalid prefix throws OptionException
    @Test(expected = OptionException.class)
    public void testCheckPrefixes_invalidPrefix_throwsException() {
        final List options = new ArrayList();
        options.add(optionA);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);

        final Set invalidPrefixes = new HashSet();
        invalidPrefixes.add("/");
        group.checkPrefixes(invalidPrefixes);
    }

    // Tests appendUsage with DISPLAY_GROUP_OUTER
    @Test
    public void testAppendUsage_withOuterSetting() {
        final List options = new ArrayList();
        options.add(optionA);
        options.add(optionB);
        final GroupImpl group = new GroupImpl(options, "myGroup", "desc", 1, 1);

        final StringBuffer buffer = new StringBuffer();
        final Set helpSettings = new HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_OUTER);

        group.appendUsage(buffer, helpSettings, null, " | ");
        final String usage = buffer.toString();
        assertTrue(usage.startsWith("(") && usage.endsWith(")"));
    }

    // Tests appendUsage with empty options
    @Test
    public void testAppendUsage_emptyOptions() {
        final GroupImpl group = new GroupImpl(Collections.EMPTY_LIST, "emptyGroup", "desc", 0, 0);
        final StringBuffer buffer = new StringBuffer();
        final Set helpSettings = new HashSet();

        group.appendUsage(buffer, helpSettings, null);
        assertEquals("", buffer.toString());
    }

    // Tests validate passes for valid nested group and child option
    @Test
    public void testValidate_nestedGroupValidation() throws OptionException {
        final List childOptions = new ArrayList();
        childOptions.add(optionA);
        final GroupImpl childGroup = new GroupImpl(childOptions, "childGroup", "childDesc", 1, 1);

        final List parentOptions = new ArrayList();
        parentOptions.add(childGroup);
        final GroupImpl parentGroup = new GroupImpl(parentOptions, "parentGroup", "parentDesc", 1, 1);

        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(parentGroup, new ArrayList());
        commandLine.addOption(optionA);

        parentGroup.validate(commandLine);
    }

    // Tests validate throws when child in nested group fails validation
    @Test(expected = OptionException.class)
    public void testValidate_nestedGroupValidationFails() throws OptionException {
        final List childOptions = new ArrayList();
        childOptions.add(optionA);
        final GroupImpl childGroup = new GroupImpl(childOptions, "childGroup", "childDesc", 1, 1);

        final List parentOptions = new ArrayList();
        parentOptions.add(childGroup);
        final GroupImpl parentGroup = new GroupImpl(parentOptions, "parentGroup", "parentDesc", 0, 1);

        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(parentGroup, new ArrayList());
        // Child group is selected but optionA is missing
        commandLine.addOption(childGroup);

        parentGroup.validate(commandLine);
    }

    // Tests helpLines when DISPLAY_GROUP_NAME is absent
    @Test
    public void testHelpLines_withoutGroupName() {
        final List options = new ArrayList();
        options.add(optionA);
        options.add(optionB);
        final GroupImpl group = new GroupImpl(options, "groupName", "Group Description", 0, 1);

        final Set helpSettings = new HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);

        final List lines = group.helpLines(0, helpSettings, null);
        assertNotNull(lines);
        assertFalse(lines.isEmpty());
    }
}