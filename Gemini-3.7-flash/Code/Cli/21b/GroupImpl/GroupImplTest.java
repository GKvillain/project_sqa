package org.apache.commons.cli2.option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

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

import static org.junit.Assert.*;

public class GroupImplTest {

    private DefaultOption optionA;
    private DefaultOption optionB;
    private Option anonArg;
    private GroupImpl group;

    @Before
    public void setUp() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final ArgumentBuilder abuilder = new ArgumentBuilder();

        optionA = obuilder.withShortName("a").withLongName("alpha").withDescription("Option A").create();
        optionB = obuilder.withShortName("b").withLongName("beta").withDescription("Option B").create();
        anonArg = abuilder.withName("arg").withMinimum(0).withMaximum(1).create();

        final List options = new ArrayList();
        options.add(optionA);
        options.add(optionB);
        options.add(anonArg);

        group = new GroupImpl(options, "testGroup", "Test Group Description", 1, 2, true);
    }

    // Tests properties getters
    @Test
    public void testGetProperties_normal_returnsCorrectValues() {
        assertEquals("testGroup", group.getPreferredName());
        assertEquals("Test Group Description", group.getDescription());
        assertEquals(1, group.getMinimum());
        assertEquals(2, group.getMaximum());
        assertEquals(2, group.getOptions().size());
        assertEquals(1, group.getAnonymous().size());
        assertTrue(group.getAnonymous().contains(anonArg));
    }

    // Tests getTriggers and getPrefixes
    @Test
    public void testGetTriggersAndPrefixes_normal_containsExpected() {
        final Set triggers = group.getTriggers();
        assertTrue(triggers.contains("-a"));
        assertTrue(triggers.contains("--alpha"));
        assertTrue(triggers.contains("-b"));
        assertTrue(triggers.contains("--beta"));

        final Set prefixes = group.getPrefixes();
        assertTrue(prefixes.contains("-"));
        assertTrue(prefixes.contains("--"));
    }

    // Tests canProcess with null argument
    @Test
    public void testCanProcess_nullArg_returnsFalse() {
        final WriteableCommandLineImpl commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        assertFalse(group.canProcess(commandLine, (String) null));
    }

    // Tests canProcess with existing trigger
    @Test
    public void testCanProcess_validTrigger_returnsTrue() {
        final WriteableCommandLineImpl commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        assertTrue(group.canProcess(commandLine, "-a"));
        assertTrue(group.canProcess(commandLine, "--beta"));
    }

    // Tests canProcess with anonymous argument present
    @Test
    public void testCanProcess_nonOptionWithAnonymousArg_returnsTrue() {
        final WriteableCommandLineImpl commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        assertTrue(group.canProcess(commandLine, "someValue"));
    }

    // Tests canProcess when looking like an option but not matching
    @Test
    public void testCanProcess_unknownOption_returnsFalse() {
        final WriteableCommandLineImpl commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        assertFalse(group.canProcess(commandLine, "-unknown"));
    }

    // Tests canProcess with no anonymous arguments
    @Test
    public void testCanProcess_noAnonymousArgsAndNotOption_returnsFalse() {
        final List options = new ArrayList();
        options.add(optionA);
        final GroupImpl noAnonGroup = new GroupImpl(options, "group", "desc", 0, 1, false);
        final WriteableCommandLineImpl commandLine = new WriteableCommandLineImpl(noAnonGroup, new ArrayList());

        assertFalse(noAnonGroup.canProcess(commandLine, "someValue"));
    }

    // Tests findOption with matching and non-matching triggers
    @Test
    public void testFindOption_triggers_returnsMatchingOptionOrNull() {
        assertEquals(optionA, group.findOption("-a"));
        assertEquals(optionA, group.findOption("--alpha"));
        assertEquals(optionB, group.findOption("-b"));
        assertNull(group.findOption("-unknown"));
    }

    // Tests isRequired based on minimum and parent relationship
    @Test
    public void testIsRequired_parentAndMinimumConstraints() {
        assertTrue(group.isRequired());

        final List options = new ArrayList();
        options.add(optionA);

        final GroupImpl zeroMinGroup = new GroupImpl(options, "zeroMin", "desc", 0, 1, true);
        assertFalse(zeroMinGroup.isRequired());

        final GroupImpl notRequiredGroup = new GroupImpl(options, "notReq", "desc", 1, 1, false);
        assertTrue(notRequiredGroup.isRequired());

        // When child of another group, isRequired delegates to super.isRequired()
        final GroupImpl parentGroup = new GroupImpl(Collections.singletonList(notRequiredGroup), "parent", "desc", 0, 1, false);
        assertFalse(notRequiredGroup.isRequired());
    }

    // Tests process method parsing valid options and arguments
    @Test
    public void testProcess_validArguments_success() throws OptionException {
        final List tokens = new ArrayList();
        tokens.add("-a");
        tokens.add("value1");

        final WriteableCommandLineImpl commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        final ListIterator iterator = tokens.listIterator();

        group.process(commandLine, iterator);

        assertTrue(commandLine.hasOption("-a"));
        assertTrue(commandLine.hasOption(optionA));
        assertTrue(commandLine.getValues(anonArg).contains("value1"));
        assertFalse(iterator.hasNext());
    }

    // Tests process stops when argument cannot be processed
    @Test
    public void testProcess_unrecognizedOption_stopsIterator() throws OptionException {
        final List tokens = new ArrayList();
        tokens.add("-a");
        tokens.add("-z");

        final WriteableCommandLineImpl commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        final ListIterator iterator = tokens.listIterator();

        group.process(commandLine, iterator);

        assertTrue(commandLine.hasOption("-a"));
        assertTrue(iterator.hasNext());
        assertEquals("-z", iterator.next());
    }

    // Tests validate when minimum options requirement is not met
    @Test(expected = OptionException.class)
    public void testValidate_missingOption_throwsException() throws OptionException {
        final WriteableCommandLineImpl commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        group.validate(commandLine);
    }

    // Tests validate when maximum options requirement is exceeded
    @Test(expected = OptionException.class)
    public void testValidate_unexpectedOptionExceedsMax_throwsException() throws OptionException {
        final List options = new ArrayList();
        options.add(optionA);
        options.add(optionB);

        final GroupImpl singleChoiceGroup = new GroupImpl(options, "singleChoice", "desc", 1, 1, true);
        final WriteableCommandLineImpl commandLine = new WriteableCommandLineImpl(singleChoiceGroup, new ArrayList());
        commandLine.addOption(optionA);
        commandLine.addOption(optionB);

        singleChoiceGroup.validate(commandLine);
    }

    // Tests validate with valid number of options
    @Test
    public void testValidate_validOptions_noException() throws OptionException {
        final WriteableCommandLineImpl commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        commandLine.addOption(optionA);

        group.validate(commandLine);
    }

    // Tests appendUsage with various display settings
    @Test
    public void testAppendUsage_variousSettings_formatsCorrectly() {
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_GROUP_NAME);
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        settings.add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);

        group.appendUsage(buffer, settings, null);

        final String usage = buffer.toString();
        assertTrue(usage.contains("testGroup"));
        assertTrue(usage.contains("-a"));
        assertTrue(usage.contains("-b"));
    }

    // Tests appendUsage with optional display setting
    @Test
    public void testAppendUsage_optionalSetting_includesBrackets() {
        final List options = new ArrayList();
        options.add(optionA);
        final GroupImpl optionalGroup = new GroupImpl(options, "optGroup", "desc", 0, 1, false);

        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_OPTIONAL);
        settings.add(DisplaySetting.DISPLAY_GROUP_NAME);

        optionalGroup.appendUsage(buffer, settings, null);

        final String usage = buffer.toString();
        assertTrue(usage.startsWith("["));
        assertTrue(usage.endsWith("]"));
    }

    // Tests helpLines generation
    @Test
    public void testHelpLines_expanded_returnsHelpLines() {
        final Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_GROUP_NAME);
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        settings.add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);

        final List lines = group.helpLines(0, settings, null);
        assertNotNull(lines);
        assertFalse(lines.isEmpty());

        boolean foundGroupName = false;
        for (final Object lineObj : lines) {
            final HelpLine line = (HelpLine) lineObj;
            if (line.getOption().equals(group)) {
                foundGroupName = true;
                break;
            }
        }
        assertTrue(foundGroupName);
    }

    // Tests defaults method populates defaults
    @Test
    public void testDefaults_appliesToChildren() {
        final WriteableCommandLineImpl commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        group.defaults(commandLine);
        assertNotNull(commandLine);
    }

    // Tests canProcess using ListIterator argument overload
    @Test
    public void testCanProcess_withListIterator_checksLookahead() {
        final WriteableCommandLineImpl commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        final List tokens = new ArrayList();
        tokens.add("-a");

        final ListIterator it = tokens.listIterator();
        assertTrue(group.canProcess(commandLine, it));
        assertEquals("-a", it.next());

        final List emptyTokens = new ArrayList();
        assertFalse(group.canProcess(commandLine, emptyTokens.listIterator()));
    }

    // Tests appendUsage with comparator sorting options and arguments
    @Test
    public void testAppendUsage_withComparator_sortsOptions() {
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);

        final Comparator reverseComp = new Comparator() {
            public int compare(final Object o1, final Object o2) {
                final Option opt1 = (Option) o1;
                final Option opt2 = (Option) o2;
                return opt2.getPreferredName().compareTo(opt1.getPreferredName());
            }
        };

        group.appendUsage(buffer, settings, reverseComp);
        final String usage = buffer.toString();
        final int indexB = usage.indexOf("-b");
        final int indexA = usage.indexOf("-a");
        assertTrue(indexB >= 0 && indexA >= 0 && indexB < indexA);
    }

    // Tests appendUsage exclusive group (min=1, max=1) produces pipe separator
    @Test
    public void testAppendUsage_exclusiveGroup_usesPipeSeparator() {
        final List options = new ArrayList();
        options.add(optionA);
        options.add(optionB);

        final GroupImpl exclusiveGroup = new GroupImpl(options, "exclusive", "Exclusive Group", 1, 1, true);
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);

        exclusiveGroup.appendUsage(buffer, settings, null);
        final String usage = buffer.toString();
        assertTrue(usage.contains("|"));
    }

    // Tests appendUsage with DISPLAY_GROUP_OUTER setting
    @Test
    public void testAppendUsage_withDisplayGroupOuter_includesOuterBracketsOrParentheses() {
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        settings.add(DisplaySetting.DISPLAY_GROUP_OUTER);

        group.appendUsage(buffer, settings, null);
        final String usage = buffer.toString();
        assertTrue(usage.startsWith("(") && usage.endsWith(")"));
    }

    // Tests helpLines with comparator
    @Test
    public void testHelpLines_withComparator_returnsSortedHelpLines() {
        final Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);

        final Comparator comp = new Comparator() {
            public int compare(final Object o1, final Object o2) {
                final Option opt1 = (Option) o1;
                final Option opt2 = (Option) o2;
                return opt2.getPreferredName().compareTo(opt1.getPreferredName());
            }
        };

        final List lines = group.helpLines(0, settings, comp);
        assertNotNull(lines);
        assertTrue(lines.size() >= 2);
    }

    // Tests helpLines when not expanded returns group name line only
    @Test
    public void testHelpLines_notExpanded_returnsSummaryLine() {
        final Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_GROUP_NAME);

        final List lines = group.helpLines(0, settings, null);
        assertEquals(1, lines.size());
        final HelpLine line = (HelpLine) lines.get(0);
        assertEquals(group, line.getOption());
    }

    // Tests validate when required anonymous argument is missing
    @Test(expected = OptionException.class)
    public void testValidate_requiredAnonymousArgumentMissing_throwsException() throws OptionException {
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final Option reqAnonArg = abuilder.withName("requiredArg").withMinimum(1).withMaximum(1).create();

        final List options = new ArrayList();
        options.add(reqAnonArg);

        final GroupImpl anonGroup = new GroupImpl(options, "anonGroup", "desc", 0, 1, false);
        final WriteableCommandLineImpl commandLine = new WriteableCommandLineImpl(anonGroup, new ArrayList());

        anonGroup.validate(commandLine);
    }
}