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

public class GroupImplTest {

    private DefaultOption optionHelp;
    private DefaultOption optionVersion;
    private Argument argumentFile;
    private GroupImpl group;

    @Before
    public void setUp() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final ArgumentBuilder abuilder = new ArgumentBuilder();

        optionHelp = obuilder
            .withShortName("h")
            .withLongName("help")
            .withDescription("print help message")
            .create();

        optionVersion = obuilder
            .withShortName("v")
            .withLongName("version")
            .withDescription("print version info")
            .create();

        argumentFile = abuilder
            .withName("file")
            .withMinimum(0)
            .withMaximum(1)
            .create();

        final List options = new ArrayList();
        options.add(optionHelp);
        options.add(optionVersion);
        options.add(argumentFile);

        group = new GroupImpl(options, "options", "Available options", 0, 2);
    }

    // Tests getter methods and initial state
    @Test
    public void testGetters_validGroup_returnsConfiguredValues() {
        assertEquals("options", group.getPreferredName());
        assertEquals("Available options", group.getDescription());
        assertEquals(0, group.getMinimum());
        assertEquals(2, group.getMaximum());
        assertFalse(group.isRequired());

        assertEquals(2, group.getOptions().size());
        assertTrue(group.getOptions().contains(optionHelp));
        assertTrue(group.getOptions().contains(optionVersion));

        assertEquals(1, group.getAnonymous().size());
        assertTrue(group.getAnonymous().contains(argumentFile));

        assertTrue(group.getTriggers().contains("--help"));
        assertTrue(group.getTriggers().contains("-h"));
        assertTrue(group.getTriggers().contains("--version"));
        assertTrue(group.getTriggers().contains("-v"));
        assertTrue(group.getPrefixes().contains("-"));
        assertTrue(group.getPrefixes().contains("--"));
    }

    // Tests isRequired when minimum is greater than zero
    @Test
    public void testIsRequired_minimumGreaterThanZero_returnsTrue() {
        final List options = new ArrayList();
        options.add(optionHelp);
        final GroupImpl requiredGroup = new GroupImpl(options, "req", "Required group", 1, 1);

        assertTrue(requiredGroup.isRequired());
        assertEquals(1, requiredGroup.getMinimum());
    }

    // Tests findOption with existing and non-existing triggers
    @Test
    public void testFindOption_existingAndNonExistingTriggers_returnsExpectedResult() {
        assertEquals(optionHelp, group.findOption("-h"));
        assertEquals(optionHelp, group.findOption("--help"));
        assertEquals(optionVersion, group.findOption("-v"));
        assertNull(group.findOption("-unknown"));
    }

    // Tests canProcess with null argument
    @Test
    public void testCanProcess_nullArgument_returnsFalse() {
        final WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(group, new ArrayList());
        assertFalse(group.canProcess(cmdLine, (String) null));
    }

    // Tests canProcess with matching trigger
    @Test
    public void testCanProcess_validTrigger_returnsTrue() {
        final WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(group, new ArrayList());
        assertTrue(group.canProcess(cmdLine, "--help"));
        assertTrue(group.canProcess(cmdLine, "-v"));
    }

    // Tests canProcess with anonymous argument present
    @Test
    public void testCanProcess_nonOptionArgumentWithAnonymous_returnsTrue() {
        final WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(group, new ArrayList());
        assertTrue(group.canProcess(cmdLine, "filename.txt"));
    }

    // Tests canProcess when no anonymous argument is available
    @Test
    public void testCanProcess_nonOptionArgumentWithoutAnonymous_returnsFalse() {
        final List options = new ArrayList();
        options.add(optionHelp);
        final GroupImpl groupNoAnon = new GroupImpl(options, "noAnon", "desc", 0, 1);
        final WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(groupNoAnon, new ArrayList());

        assertFalse(groupNoAnon.canProcess(cmdLine, "filename.txt"));
    }

    // Tests canProcess with unknown option-like argument
    @Test
    public void testCanProcess_unknownOptionLikeArgument_returnsFalse() {
        final WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(group, new ArrayList());
        assertFalse(group.canProcess(cmdLine, "--unknown"));
    }

    // Tests canProcess with ListIterator
    @Test
    public void testCanProcess_listIterator_processesNextElementWithoutConsuming() {
        final WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(group, new ArrayList());
        final List args = new ArrayList();
        args.add("--help");

        final ListIterator iterator = args.listIterator();
        assertTrue(group.canProcess(cmdLine, iterator));
        assertTrue(iterator.hasNext());
        assertEquals("--help", iterator.next());

        assertFalse(group.canProcess(cmdLine, iterator));
    }

    // Tests process method with valid option tokens
    @Test
    public void testProcess_validOptionTokens_processesSuccessfully() throws OptionException {
        final List args = new ArrayList();
        args.add("--help");
        args.add("--version");

        final WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(group, new ArrayList());
        final ListIterator iterator = args.listIterator();

        group.process(cmdLine, iterator);

        assertTrue(cmdLine.hasOption(optionHelp));
        assertTrue(cmdLine.hasOption(optionVersion));
        assertFalse(iterator.hasNext());
    }

    // Tests process method with anonymous arguments
    @Test
    public void testProcess_anonymousArguments_processesValues() throws OptionException {
        final List args = new ArrayList();
        args.add("file1.txt");

        final WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(group, new ArrayList());
        final ListIterator iterator = args.listIterator();

        group.process(cmdLine, iterator);

        assertTrue(cmdLine.hasOption(argumentFile));
        assertEquals("file1.txt", cmdLine.getValue(argumentFile));
    }

    // Tests process method stops when encountering unhandled option token
    @Test
    public void testProcess_unrecognizedOption_abortsProcessing() throws OptionException {
        final List args = new ArrayList();
        args.add("--unknown");

        final WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(group, new ArrayList());
        final ListIterator iterator = args.listIterator();

        group.process(cmdLine, iterator);

        assertTrue(iterator.hasNext());
        assertEquals("--unknown", iterator.next());
    }

    // Tests validate successfully when criteria are met
    @Test
    public void testValidate_validCommandLine_noExceptionThrown() throws OptionException {
        final WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(group, new ArrayList());
        cmdLine.addOption(optionHelp);

        group.validate(cmdLine);
    }

    // Tests validate throws OptionException when minimum required options not met
    @Test(expected = OptionException.class)
    public void testValidate_fewerThanMinimumOptions_throwsOptionException() throws OptionException {
        final List options = new ArrayList();
        options.add(optionHelp);
        options.add(optionVersion);
        final GroupImpl requiredGroup = new GroupImpl(options, "req", "desc", 1, 2);

        final WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(requiredGroup, new ArrayList());
        requiredGroup.validate(cmdLine);
    }

    // Tests validate throws OptionException when options exceed maximum
    @Test(expected = OptionException.class)
    public void testValidate_moreThanMaximumOptions_throwsOptionException() throws OptionException {
        final List options = new ArrayList();
        options.add(optionHelp);
        options.add(optionVersion);
        final GroupImpl maxGroup = new GroupImpl(options, "max", "desc", 0, 1);

        final WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(maxGroup, new ArrayList());
        cmdLine.addOption(optionHelp);
        cmdLine.addOption(optionVersion);

        maxGroup.validate(cmdLine);
    }

    // Tests validate with optional anonymous argument not supplied (Defects4J Cli-14 regression check)
    @Test
    public void testValidate_optionalAnonymousArgumentNotSupplied_validatesSuccessfully() throws OptionException {
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final Argument optionalArg = abuilder
            .withName("target")
            .withMinimum(0)
            .withMaximum(1)
            .create();

        final List options = new ArrayList();
        options.add(optionHelp);
        options.add(optionalArg);

        final GroupImpl testGroup = new GroupImpl(options, "grp", "desc", 0, 1);
        final WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(testGroup, new ArrayList());

        testGroup.validate(cmdLine);
    }

    // Tests appendUsage with expanded and optional settings
    @Test
    public void testAppendUsage_optionalAndExpandedSettings_buildsUsageString() {
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_OPTIONAL);
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        settings.add(DisplaySetting.DISPLAY_GROUP_NAME);

        group.appendUsage(buffer, settings, null);

        final String usage = buffer.toString();
        assertTrue(usage.startsWith("["));
        assertTrue(usage.contains("options"));
        assertTrue(usage.contains("-h"));
        assertTrue(usage.contains("-v"));
        assertTrue(usage.endsWith("]"));
    }

    // Tests appendUsage without expanded display displays only the group name
    @Test
    public void testAppendUsage_withoutExpanded_displaysNameOnly() {
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_GROUP_NAME);

        group.appendUsage(buffer, settings, null);

        assertEquals("options", buffer.toString().trim());
    }

    // Tests appendUsage with custom separator and comparator
    @Test
    public void testAppendUsage_withComparatorAndCustomSeparator_formatsCorrectly() {
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);

        final Comparator reverseComp = Collections.reverseOrder();
        group.appendUsage(buffer, settings, reverseComp, " AND ");

        final String usage = buffer.toString();
        assertTrue(usage.contains(" AND "));
    }

    // Tests helpLines generation with display settings
    @Test
    public void testHelpLines_withGroupNameAndExpanded_returnsHelpLines() {
        final Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_GROUP_NAME);
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        settings.add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);

        final List lines = group.helpLines(0, settings, null);

        assertNotNull(lines);
        assertFalse(lines.isEmpty());
        assertTrue(lines.size() >= 3);

        final HelpLine firstLine = (HelpLine) lines.get(0);
        assertEquals(0, firstLine.getIndent());
        assertEquals(group, firstLine.getOption());
    }

    // Tests defaults populates command line defaults
    @Test
    public void testDefaults_commandLine_invokesDefaultsOnChildren() {
        final WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(group, new ArrayList());
        group.defaults(cmdLine);
    }
}