package org.apache.commons.cli2.option;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.HelpLine;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.WriteableCommandLine;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.WriteableCommandLineImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GroupImplTest {

    private DefaultOption optionHelp;
    private DefaultOption optionVersion;
    private DefaultOption optionFile;
    private Argument argumentArg;

    @Before
    public void setUp() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final ArgumentBuilder abuilder = new ArgumentBuilder();

        optionHelp = obuilder
            .withShortName("h")
            .withLongName("help")
            .withDescription("Displays help info")
            .create();

        optionVersion = obuilder
            .withShortName("v")
            .withLongName("version")
            .withDescription("Displays version info")
            .create();

        optionFile = obuilder
            .withShortName("f")
            .withLongName("file")
            .withDescription("Specify file")
            .create();

        argumentArg = abuilder
            .withName("target")
            .withMinimum(1)
            .withMaximum(1)
            .create();
    }

    // Tests construction and splitting of options vs anonymous arguments
    @Test
    public void testConstructor_withOptionsAndArguments_correctlySegregated() {
        final List options = new ArrayList();
        options.add(optionHelp);
        options.add(argumentArg);
        options.add(optionVersion);

        final GroupImpl group = new GroupImpl(options, "testGroup", "A test group", 0, 2);

        assertEquals("testGroup", group.getPreferredName());
        assertEquals("A test group", group.getDescription());
        assertEquals(0, group.getMinimum());
        assertEquals(2, group.getMaximum());
        assertFalse(group.isRequired());

        assertEquals(2, group.getOptions().size());
        assertTrue(group.getOptions().contains(optionHelp));
        assertTrue(group.getOptions().contains(optionVersion));

        assertEquals(1, group.getAnonymous().size());
        assertTrue(group.getAnonymous().contains(argumentArg));
    }

    // Tests getPrefixes and getTriggers retrieval
    @Test
    public void testGetPrefixesAndTriggers_returnsExpectedSet() {
        final List options = new ArrayList();
        options.add(optionHelp);
        options.add(optionVersion);

        final GroupImpl group = new GroupImpl(options, "group", "desc", 1, 1);

        assertTrue(group.isRequired());
        final Set prefixes = group.getPrefixes();
        assertTrue(prefixes.contains("-"));
        assertTrue(prefixes.contains("--"));

        final Set triggers = group.getTriggers();
        assertTrue(triggers.contains("-h"));
        assertTrue(triggers.contains("--help"));
        assertTrue(triggers.contains("-v"));
        assertTrue(triggers.contains("--version"));
    }

    // Tests findOption method for matching triggers and non-matching trigger
    @Test
    public void testFindOption_matchesExistingAndMissingTriggers() {
        final List options = new ArrayList();
        options.add(optionHelp);
        options.add(optionFile);

        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 2);

        assertEquals(optionHelp, group.findOption("-h"));
        assertEquals(optionHelp, group.findOption("--help"));
        assertEquals(optionFile, group.findOption("-f"));
        assertNull(group.findOption("-unknown"));
    }

    // Tests canProcess with null argument
    @Test
    public void testCanProcess_nullArgument_returnsFalse() {
        final List options = new ArrayList();
        options.add(optionHelp);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        assertFalse(group.canProcess(commandLine, (String) null));
    }

    // Tests canProcess matching an exact option trigger
    @Test
    public void testCanProcess_matchingTrigger_returnsTrue() {
        final List options = new ArrayList();
        options.add(optionHelp);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        assertTrue(group.canProcess(commandLine, "--help"));
        assertTrue(group.canProcess(commandLine, "-h"));
    }

    // Tests canProcess with anonymous argument
    @Test
    public void testCanProcess_anonymousArgument_returnsTrueWhenNonOption() {
        final List options = new ArrayList();
        options.add(argumentArg);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        assertTrue(group.canProcess(commandLine, "someValue"));
    }

    // Tests canProcess when looksLikeOption is true but option does not match
    @Test
    public void testCanProcess_looksLikeOptionButNotPresent_returnsFalse() {
        final List options = new ArrayList();
        options.add(optionHelp);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        assertFalse(group.canProcess(commandLine, "--nonExistent"));
    }

    // Tests process with valid option token
    @Test
    public void testProcess_validOption_processedSuccessfully() throws OptionException {
        final List options = new ArrayList();
        options.add(optionHelp);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);

        final List args = new ArrayList();
        args.add("--help");
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        final ListIterator iterator = args.listIterator();

        group.process(commandLine, iterator);
        assertTrue(commandLine.hasOption("--help"));
        assertFalse(iterator.hasNext());
    }

    // Tests process with unknown option that looks like option
    @Test
    public void testProcess_unknownOption_stopsProcessing() throws OptionException {
        final List options = new ArrayList();
        options.add(optionHelp);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);

        final List args = new ArrayList();
        args.add("--unknown");
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        final ListIterator iterator = args.listIterator();

        group.process(commandLine, iterator);
        assertFalse(commandLine.hasOption("--help"));
        assertEquals("--unknown", iterator.next());
    }

    // Tests process with anonymous argument
    @Test
    public void testProcess_anonymousArgument_processesArgument() throws OptionException {
        final List options = new ArrayList();
        options.add(argumentArg);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);

        final List args = new ArrayList();
        args.add("file.txt");
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        final ListIterator iterator = args.listIterator();

        group.process(commandLine, iterator);
        assertTrue(commandLine.hasOption(argumentArg));
        assertEquals("file.txt", commandLine.getValue(argumentArg));
    }

    // Tests validate when minimum options requirement is not met
    @Test(expected = OptionException.class)
    public void testValidate_fewerThanMinimumOptions_throwsOptionException() throws OptionException {
        final List options = new ArrayList();
        options.add(optionHelp);
        options.add(optionVersion);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 1, 2);

        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        group.validate(commandLine);
    }

    // Tests validate when maximum options limit is exceeded
    @Test(expected = OptionException.class)
    public void testValidate_moreThanMaximumOptions_throwsOptionException() throws OptionException {
        final List options = new ArrayList();
        options.add(optionHelp);
        options.add(optionVersion);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);

        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        commandLine.addOption(optionHelp);
        commandLine.addOption(optionVersion);

        group.validate(commandLine);
    }

    // Tests validate when valid number of options are supplied
    @Test
    public void testValidate_validOptionCount_passesValidation() throws OptionException {
        final List options = new ArrayList();
        options.add(optionHelp);
        options.add(optionVersion);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 1, 2);

        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        commandLine.addOption(optionHelp);

        group.validate(commandLine);
    }

    // Tests validate with nested required options
    @Test(expected = OptionException.class)
    public void testValidate_nestedRequiredOptionMissing_throwsOptionException() throws OptionException {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final DefaultOption reqOption = obuilder
            .withShortName("r")
            .withLongName("required")
            .withRequired(true)
            .create();

        final List options = new ArrayList();
        options.add(reqOption);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);

        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        group.validate(commandLine);
    }

    // Tests validate with nested child group
    @Test
    public void testValidate_childGroupValidation_executesChildValidation() throws OptionException {
        final List childOptions = new ArrayList();
        childOptions.add(optionHelp);
        final GroupImpl childGroup = new GroupImpl(childOptions, "childGroup", "desc", 0, 1);

        final List parentOptions = new ArrayList();
        parentOptions.add(childGroup);
        final GroupImpl parentGroup = new GroupImpl(parentOptions, "parentGroup", "desc", 0, 1);

        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(parentGroup, new ArrayList());
        parentGroup.validate(commandLine);
    }

    // Tests appendUsage with different display settings
    @Test
    public void testAppendUsage_optionalAndNamedAndExpanded_appendsCorrectString() {
        final List options = new ArrayList();
        options.add(optionHelp);
        options.add(optionVersion);
        final GroupImpl group = new GroupImpl(options, "myGroup", "desc", 0, 2);

        final StringBuffer buffer = new StringBuffer();
        final Set displaySettings = new HashSet();
        displaySettings.add(DisplaySetting.DISPLAY_OPTIONAL);
        displaySettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        displaySettings.add(DisplaySetting.DISPLAY_GROUP_NAME);

        group.appendUsage(buffer, displaySettings, null);
        final String usage = buffer.toString();

        assertTrue(usage.startsWith("["));
        assertTrue(usage.contains("myGroup"));
        assertTrue(usage.contains("-h"));
        assertTrue(usage.contains("-v"));
        assertTrue(usage.endsWith("]"));
    }

    // Tests appendUsage with DISPLAY_GROUP_OUTER setting
    @Test
    public void testAppendUsage_outerSetting_appendsOuterBrackets() {
        final List options = new ArrayList();
        options.add(optionHelp);
        final GroupImpl group = new GroupImpl(options, "myGroup", "desc", 0, 1);

        final StringBuffer buffer = new StringBuffer();
        final Set displaySettings = new HashSet();
        displaySettings.add(DisplaySetting.DISPLAY_OPTIONAL);
        displaySettings.add(DisplaySetting.DISPLAY_GROUP_NAME);
        displaySettings.add(DisplaySetting.DISPLAY_GROUP_OUTER);

        group.appendUsage(buffer, displaySettings, null, ",");
        final String usage = buffer.toString();

        assertTrue(usage.startsWith("["));
        assertTrue(usage.endsWith("]"));
        assertTrue(usage.contains("myGroup"));
    }

    // Tests helpLines generation with display settings
    @Test
    public void testHelpLines_withExpandedAndNameSettings_returnsExpectedHelpLines() {
        final List options = new ArrayList();
        options.add(optionHelp);
        options.add(argumentArg);
        final GroupImpl group = new GroupImpl(options, "group", "Group Description", 0, 1);

        final Set displaySettings = new HashSet();
        displaySettings.add(DisplaySetting.DISPLAY_GROUP_NAME);
        displaySettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        displaySettings.add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);

        final List lines = group.helpLines(0, displaySettings, null);
        assertNotNull(lines);
        assertFalse(lines.isEmpty());

        boolean foundGroupName = false;
        for (final Object lineObj : lines) {
            final HelpLine line = (HelpLine) lineObj;
            if (line.getOption().equals(group)) {
                foundGroupName = true;
                assertEquals(0, line.getIndent());
            }
        }
        assertTrue(foundGroupName);
    }

    // Tests defaults populates default values to options and arguments
    @Test
    public void testDefaults_appliesDefaultsToChildrenAndAnonymous() {
        final List options = new ArrayList();
        options.add(optionHelp);
        options.add(argumentArg);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);

        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        group.defaults(commandLine);
        assertFalse(commandLine.hasOption(optionHelp));
    }
}