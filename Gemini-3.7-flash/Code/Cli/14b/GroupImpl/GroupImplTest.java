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
import org.apache.commons.cli2.HelpLine;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.WriteableCommandLine;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.builder.SwitchBuilder;
import org.apache.commons.cli2.commandline.WriteableCommandLineImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GroupImplTest {

    private Option helpOption;
    private Option versionOption;
    private Argument targetArg;
    private DefaultOptionBuilder obuilder;
    private ArgumentBuilder abuilder;
    private GroupBuilder gbuilder;

    @Before
    public void setUp() {
        this.obuilder = new DefaultOptionBuilder();
        this.abuilder = new ArgumentBuilder();
        this.gbuilder = new GroupBuilder();

        this.helpOption = obuilder
                .withShortName("h")
                .withLongName("help")
                .withDescription("displays the help message")
                .create();

        this.versionOption = obuilder
                .withShortName("v")
                .withLongName("version")
                .withDescription("displays the version information")
                .create();

        this.targetArg = abuilder
                .withName("target")
                .withMinimum(1)
                .withMaximum(1)
                .create();
    }

    // Tests construction and property getters
    @Test
    public void testGetters_validGroup_returnsProperties() {
        List options = new ArrayList();
        options.add(helpOption);
        options.add(versionOption);
        options.add(targetArg);

        GroupImpl group = new GroupImpl(options, "options", "Available options", 1, 2);

        assertEquals("options", group.getPreferredName());
        assertEquals("Available options", group.getDescription());
        assertEquals(1, group.getMinimum());
        assertEquals(2, group.getMaximum());
        assertTrue(group.isRequired());
        assertEquals(2, group.getOptions().size());
        assertEquals(1, group.getAnonymous().size());
    }

    // Tests isRequired when minimum is 0
    @Test
    public void testIsRequired_zeroMinimum_returnsFalse() {
        List options = new ArrayList();
        options.add(helpOption);

        GroupImpl group = new GroupImpl(options, "options", "desc", 0, 1);
        assertFalse(group.isRequired());
    }

    // Tests getPrefixes and getTriggers extraction from options
    @Test
    public void testGetPrefixesAndTriggers_standardOptions_returnsPopulatedSets() {
        List options = new ArrayList();
        options.add(helpOption);
        options.add(versionOption);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 2);

        Set triggers = group.getTriggers();
        assertTrue(triggers.contains("-h"));
        assertTrue(triggers.contains("--help"));
        assertTrue(triggers.contains("-v"));
        assertTrue(triggers.contains("--version"));

        Set prefixes = group.getPrefixes();
        assertTrue(prefixes.contains("-"));
        assertTrue(prefixes.contains("--"));
    }

    // Tests findOption by existing trigger and non-existing trigger
    @Test
    public void testFindOption_existingAndNonExistingTrigger_findsOptionOrNull() {
        List options = new ArrayList();
        options.add(helpOption);
        options.add(versionOption);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 2);

        assertSame(helpOption, group.findOption("-h"));
        assertSame(helpOption, group.findOption("--help"));
        assertSame(versionOption, group.findOption("-v"));
        assertNull(group.findOption("-unknown"));
    }

    // Tests canProcess for null, direct match, bursting, anonymous argument and unknown option
    @Test
    public void testCanProcess_variousInputs_returnsExpectedResult() {
        List options = new ArrayList();
        options.add(helpOption);
        options.add(targetArg);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        assertFalse(group.canProcess(commandLine, (String) null));
        assertTrue(group.canProcess(commandLine, "--help"));
        assertTrue(group.canProcess(commandLine, "-h"));
        assertTrue(group.canProcess(commandLine, "file.txt"));
        assertFalse(group.canProcess(commandLine, "--unknown"));
    }

    // Tests canProcess without anonymous arguments
    @Test
    public void testCanProcess_withoutAnonymousArguments_rejectsNonOption() {
        List options = new ArrayList();
        options.add(helpOption);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        assertFalse(group.canProcess(commandLine, "file.txt"));
    }

    // Tests process method with valid options and arguments
    @Test
    public void testProcess_validOptionAndArgument_processesSuccessfully() throws OptionException {
        List options = new ArrayList();
        options.add(helpOption);
        options.add(targetArg);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 2);

        List args = new ArrayList();
        args.add("--help");
        args.add("targetVal");

        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        ListIterator iterator = args.listIterator();

        group.process(commandLine, iterator);

        assertTrue(commandLine.hasOption(helpOption));
        assertTrue(commandLine.hasOption(targetArg));
        assertEquals("targetVal", commandLine.getValue(targetArg));
        assertFalse(iterator.hasNext());
    }

    // Tests process method when encountering an unexpected option-like argument
    @Test
    public void testProcess_unrecognizedOption_abortsProcessing() throws OptionException {
        List options = new ArrayList();
        options.add(helpOption);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 1);

        List args = new ArrayList();
        args.add("--unknown");

        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        ListIterator iterator = args.listIterator();

        group.process(commandLine, iterator);

        assertFalse(commandLine.hasOption(helpOption));
        assertTrue(iterator.hasNext());
        assertEquals("--unknown", iterator.next());
    }

    // Tests validate method when required minimum count is not met
    @Test(expected = OptionException.class)
    public void testValidate_missingRequiredOption_throwsOptionException() throws OptionException {
        List options = new ArrayList();
        options.add(helpOption);
        options.add(versionOption);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 1, 2);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        group.validate(commandLine);
    }

    // Tests validate method when maximum option count is exceeded
    @Test(expected = OptionException.class)
    public void testValidate_exceedsMaximumOptions_throwsOptionException() throws OptionException {
        List options = new ArrayList();
        options.add(helpOption);
        options.add(versionOption);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 1);

        List args = new ArrayList();
        args.add("-h");
        args.add("-v");

        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        ListIterator iterator = args.listIterator();
        group.process(commandLine, iterator);

        group.validate(commandLine);
    }

    // Tests validate method when constraints are satisfied
    @Test
    public void testValidate_withinBounds_passesValidation() throws OptionException {
        List options = new ArrayList();
        options.add(helpOption);
        options.add(versionOption);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 1, 2);

        List args = new ArrayList();
        args.add("-h");

        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        ListIterator iterator = args.listIterator();
        group.process(commandLine, iterator);

        group.validate(commandLine);
    }

    // Tests validate method for nested required group (Defects4J Cli-14 bug check)
    @Test
    public void testValidate_nestedRequiredGroup_validatesChildGroup() {
        List subOptions = new patrioticOptions();
        subOptions.add(helpOption);
        GroupImpl subGroup = new GroupImpl(subOptions, "sub", "sub group", 1, 1);

        List rootOptions = new ArrayList();
        rootOptions.add(subGroup);
        GroupImpl rootGroup = new GroupImpl(rootOptions, "root", "root group", 0, 1);

        WriteableCommandLine commandLine = new WriteableCommandLineImpl(rootGroup, new ArrayList());

        try {
            rootGroup.validate(commandLine);
            fail("Expected OptionException for missing required subGroup");
        } catch (OptionException e) {
            assertNotNull(e.getMessage());
        }
    }

    private List patrioticOptions() {
        return new ArrayList();
    }

    // Tests appendUsage with different display settings
    @Test
    public void testAppendUsage_withSettings_buildsExpectedUsageString() {
        List options = new ArrayList();
        options.add(helpOption);
        options.add(versionOption);

        GroupImpl group = new GroupImpl(options, "options", "desc", 0, 2);

        Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_OPTIONAL);
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        settings.add(DisplaySetting.DISPLAY_GROUP_NAME);

        StringBuffer buffer = new StringBuffer();
        group.appendUsage(buffer, settings, null, "|");

        String usage = buffer.toString();
        assertTrue(usage.startsWith("["));
        assertTrue(usage.contains("options"));
        assertTrue(usage.contains("-h"));
        assertTrue(usage.contains("-v"));
    }

    // Tests helpLines generation with group name and expanded options
    @Test
    public void testHelpLines_expandedAndNamed_returnsHelpLineList() {
        List options = new ArrayList();
        options.add(helpOption);
        options.add(versionOption);

        GroupImpl group = new GroupImpl(options, "options", "desc", 0, 2);

        Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_GROUP_NAME);
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);

        List lines = group.helpLines(0, settings, null);
        assertNotNull(lines);
        assertFalse(lines.isEmpty());
    }

    // Tests defaults method propagation to child options and arguments
    @Test
    public void testDefaults_commandLine_populatesDefaults() {
        List options = new ArrayList();
        options.add(helpOption);
        options.add(targetArg);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 2);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        group.defaults(commandLine);
        assertFalse(commandLine.hasOption(helpOption));
    }

    // Tests null list in constructor creates empty option and anonymous lists
    @Test
    public void testConstructor_nullOptionsList_initializesEmpty() {
        GroupImpl group = new GroupImpl(null, "emptyGroup", "empty desc", 0, 0);
        assertTrue(group.getOptions().isEmpty());
        assertTrue(group.getAnonymous().isEmpty());
        assertTrue(group.getTriggers().isEmpty());
        assertTrue(group.getPrefixes().isEmpty());
        assertEquals("emptyGroup", group.getPreferredName());
        assertEquals("emptyDesc", group.getDescription().replace(" ", ""));
    }

    // Tests canProcess using ListIterator overload and arguments
    @Test
    public void testCanProcess_listIterator_iteratesAndMatches() {
        List options = new ArrayList();
        options.add(helpOption);
        options.add(targetArg);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 2);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        List args = new ArrayList();
        args.add("-h");
        args.add("targetValue");

        ListIterator it = args.listIterator();
        assertTrue(group.canProcess(commandLine, it));
        assertEquals(0, it.nextIndex()); // Verifies iterator is rewound

        it.next(); // advance to targetValue
        assertTrue(group.canProcess(commandLine, it));
        assertEquals(1, it.nextIndex());
    }

    // Tests canProcess bursting with switch options
    @Test
    public void testCanProcess_switchOptionBursting_returnsTrue() {
        SwitchBuilder sbuilder = new SwitchBuilder();
        Option switchOpt = sbuilder.withShortName("a").withShortName("b").create();

        List options = new ArrayList();
        options.add(switchOpt);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        assertTrue(group.canProcess(commandLine, "+a"));
        assertTrue(group.canProcess(commandLine, "-ab"));
        assertFalse(group.canProcess(commandLine, "-xyz"));
    }

    // Tests appendUsage with DISPLAY_GROUP_OUTER, DISPLAY_GROUP_ARGUMENT, sort comparator, and unexpected combinations
    @Test
    public void testAppendUsage_outerAndArgumentSettingsAndComparator() {
        List options = new ArrayList();
        options.add(versionOption);
        options.add(helpOption);
        options.add(targetArg);

        GroupImpl group = new GroupImpl(options, "myGroup", "group desc", 1, 1);

        Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        settings.add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);
        settings.add(DisplaySetting.DISPLAY_GROUP_OUTER);

        Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Option) o1).getPreferredName().compareTo(((Option) o2).getPreferredName());
            }
        };

        StringBuffer buffer = new StringBuffer();
        group.appendUsage(buffer, settings, comp, "|");

        String usage = buffer.toString();
        assertTrue(usage.startsWith("("));
        assertTrue(usage.endsWith(")"));
        assertTrue(usage.contains("target"));
    }

    // Tests appendUsage with DISPLAY_GROUP_NAME only and no DISPLAY_GROUP_EXPANDED
    @Test
    public void testAppendUsage_groupNameOnly_notExpanded() {
        List options = new ArrayList();
        options.add(helpOption);

        GroupImpl group = new GroupImpl(options, "namedGrp", "desc", 0, 1);

        Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_GROUP_NAME);

        StringBuffer buffer = new StringBuffer();
        group.appendUsage(buffer, settings, null);

        assertEquals("[namedGrp]", buffer.toString());
    }

    // Tests appendUsage without group name fallback to preferred name
    @Test
    public void testAppendUsage_noGroupName_notExpanded() {
        List options = new ArrayList();
        options.add(helpOption);

        GroupImpl group = new GroupImpl(options, "fallbackName", "desc", 1, 1);

        Set settings = new HashSet();

        StringBuffer buffer = new StringBuffer();
        group.appendUsage(buffer, settings, null);

        assertEquals("fallbackName", buffer.toString());
    }

    // Tests helpLines with comparator and without expanded setting
    @Test
    public void testHelpLines_withComparatorAndNoExpanded() {
        List options = new ArrayList();
        options.add(versionOption);
        options.add(helpOption);

        GroupImpl group = new GroupImpl(options, "namedGroup", "group description", 0, 2);

        Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_GROUP_NAME);

        Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Option) o1).getPreferredName().compareTo(((Option) o2).getPreferredName());
            }
        };

        List lines = group.helpLines(1, settings, comp);
        assertEquals(1, lines.size());
        HelpLine line = (HelpLine) lines.get(0);
        assertEquals(1, line.getIndent());
        assertSame(group, line.getOption());
    }

    // Tests validate missing anonymous argument
    @Test(expected = OptionException.class)
    public void testValidate_missingAnonymousArgument_throwsOptionException() throws OptionException {
        List options = new ArrayList();
        options.add(targetArg);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        group.validate(commandLine);
    }

    // Tests validate missing required child option when minimum count is met but required child is not present
    @Test(expected = OptionException.class)
    public void testValidate_requiredChildOptionMissing_throwsOptionException() throws OptionException {
        Option reqOpt = obuilder
                .withShortName("r")
                .withLongName("required")
                .withRequired(true)
                .create();

        List options = new ArrayList();
        options.add(reqOpt);
        options.add(helpOption);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 2);

        List args = new ArrayList();
        args.add("-h");

        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        ListIterator iterator = args.listIterator();
        group.process(commandLine, iterator);

        group.validate(commandLine);
    }
}