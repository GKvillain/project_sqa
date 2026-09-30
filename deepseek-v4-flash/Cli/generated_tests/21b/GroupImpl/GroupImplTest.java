package org.apache.commons.cli2.option;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.WriteableCommandLine;
import org.apache.commons.cli2.commandline.WriteableCommandLineImpl;
import org.junit.Test;

public class GroupImplTest {

    private Option foo;
    private Option bar;
    private Argument arg;
    private GroupImpl group;

    private void setUpGroup(boolean required, int min, int max) {
        foo = new DefaultOption("foo", "foo option", false, false, null, 0, 0,
                Collections.singleton("-"), Collections.singleton("foo"));
        bar = new DefaultOption("bar", "bar option", false, false, null, 0, 0,
                Collections.singleton("-"), Collections.singleton("bar"));
        arg = new ArgumentImpl("arg", "argument", 0, 1);
        List<Option> options = new ArrayList<Option>();
        options.add(foo);
        options.add(bar);
        options.add(arg);
        group = new GroupImpl(options, "group", "group desc", min, max, required);
    }

    private GroupImpl createGroupWithoutAnonymous() {
        List<Option> options = new ArrayList<Option>();
        options.add(foo);
        options.add(bar);
        return new GroupImpl(options, "group", "group desc", 1, 2, true);
    }

    private WriteableCommandLine createCommandLine() {
        return new WriteableCommandLineImpl(null, new ArrayList());
    }

    // Tests constructor separates anonymous arguments from normal options
    @Test
    public void testConstructor_removesArgumentsFromOptions() {
        setUpGroup(true, 1, 2);
        assertEquals(2, group.getOptions().size());
        assertEquals(1, group.getAnonymous().size());
        assertTrue(group.getOptions().contains(foo));
        assertTrue(group.getOptions().contains(bar));
        assertFalse(group.getOptions().contains(arg));
        assertTrue(group.getAnonymous().contains(arg));
    }

    // Tests canProcess with a known trigger
    @Test
    public void testCanProcess_knownTrigger_returnsTrue() {
        setUpGroup(true, 1, 2);
        WriteableCommandLine cl = createCommandLine();
        assertTrue(group.canProcess(cl, "foo"));
    }

    // Tests canProcess with null argument
    @Test
    public void testCanProcess_nullArg_returnsFalse() {
        setUpGroup(true, 1, 2);
        WriteableCommandLine cl = createCommandLine();
        assertFalse(group.canProcess(cl, null));
    }

    // Tests canProcess with unknown argument when anonymous arguments exist
    @Test
    public void testCanProcess_unknownArg_withAnonymous_returnsTrue() {
        setUpGroup(true, 1, 2);
        WriteableCommandLine cl = createCommandLine();
        assertTrue(group.canProcess(cl, "random"));
    }

    // Tests canProcess with unknown argument and no anonymous arguments
    @Test
    public void testCanProcess_unknownArg_noAnonymous_returnsFalse() {
        setUpGroup(true, 1, 2);
        group = createGroupWithoutAnonymous();
        WriteableCommandLine cl = createCommandLine();
        assertFalse(group.canProcess(cl, "random"));
    }

    // Tests canProcess with unknown option-looking argument and no anonymous arguments
    @Test
    public void testCanProcess_looksLikeUnknownOption_noAnonymous_returnsFalse() {
        setUpGroup(true, 1, 2);
        group = createGroupWithoutAnonymous();
        WriteableCommandLine cl = createCommandLine();
        assertFalse(group.canProcess(cl, "-x"));
    }

    // Tests process with a known option adds the option to the command line
    @Test
    public void testProcess_knownOption_addsOption() throws Exception {
        setUpGroup(true, 1, 2);
        WriteableCommandLine cl = createCommandLine();
        List<String> args = Collections.singletonList("foo");
        ListIterator<String> it = args.listIterator();
        group.process(cl, it);
        assertTrue(cl.hasOption(foo));
    }

    // Tests process with an anonymous argument adds its value
    @Test
    public void testProcess_anonymousArgument_addsValue() throws Exception {
        setUpGroup(true, 1, 2);
        WriteableCommandLine cl = createCommandLine();
        List<String> args = Collections.singletonList("value");
        ListIterator<String> it = args.listIterator();
        group.process(cl, it);
        assertTrue(cl.getValues(arg).contains("value"));
    }

    // Tests process with unknown option-like argument and no anonymous arguments does not throw
    @Test
    public void testProcess_unknownArg_noAnonymous_doesNotThrow() throws Exception {
        setUpGroup(true, 1, 2);
        group = createGroupWithoutAnonymous();
        WriteableCommandLine cl = createCommandLine();
        List<String> args = Collections.singletonList("-x");
        ListIterator<String> it = args.listIterator();
        group.process(cl, it);
    }

    // Tests validate throws when more than maximum options are present
    @Test(expected = OptionException.class)
    public void testValidate_tooManyOptions_throws() throws Exception {
        setUpGroup(true, 1, 1);
        WriteableCommandLine cl = createCommandLine();
        cl.addOption(foo);
        cl.addOption(bar);
        group.validate(cl);
    }

    // Tests validate throws when fewer than minimum options are present
    @Test(expected = OptionException.class)
    public void testValidate_tooFewOptions_throws() throws Exception {
        setUpGroup(true, 2, 3);
        WriteableCommandLine cl = createCommandLine();
        cl.addOption(foo);
        group.validate(cl);
    }

    // Tests validate succeeds when option count is within range
    @Test
    public void testValidate_validCount_noException() throws Exception {
        setUpGroup(true, 1, 2);
        WriteableCommandLine cl = createCommandLine();
        cl.addOption(foo);
        group.validate(cl);
    }

    // Tests isRequired returns true when required and minimum > 0
    @Test
    public void testIsRequired_requiredAndMin_returnsTrue() {
        setUpGroup(true, 1, 2);
        assertTrue(group.isRequired());
    }

    // Tests isRequired returns false when not required
    @Test
    public void testIsRequired_notRequired_returnsFalse() {
        setUpGroup(false, 1, 2);
        assertFalse(group.isRequired());
    }

    // Tests findOption finds an existing option
    @Test
    public void testFindOption_found_returnsOption() {
        setUpGroup(true, 1, 2);
        assertSame(foo, group.findOption("foo"));
    }

    // Tests findOption returns null when option is not found
    @Test
    public void testFindOption_notFound_returnsNull() {
        setUpGroup(true, 1, 2);
        assertNull(group.findOption("baz"));
    }

    // Tests appendUsage starts with '[' for an optional group
    @Test
    public void testAppendUsage_optionalGroup_startsWithBracket() {
        setUpGroup(false, 1, 2);
        Set<DisplaySetting> helpSettings = new HashSet<DisplaySetting>();
        helpSettings.add(DisplaySetting.DISPLAY_OPTIONAL);
        StringBuffer buffer = new StringBuffer();
        group.appendUsage(buffer, helpSettings, null);
        assertTrue(buffer.toString().startsWith("["));
    }

    // ===== New test cases for uncovered areas =====

    // Test constructor with min=0 and max=-1 (unlimited)
    @Test
    public void testConstructor_minZeroMaxUnlimited() {
        List<Option> options = new ArrayList<Option>();
        options.add(foo);
        options.add(bar);
        GroupImpl groupUnlimited = new GroupImpl(options, "group", "group desc", 0, -1, false);
        assertFalse(groupUnlimited.isRequired());
        assertEquals(2, groupUnlimited.getOptions().size());
        assertEquals(0, groupUnlimited.getAnonymous().size());
    }

    // Test process with multiple anonymous values (when allowed)
    @Test
    public void testProcess_multipleAnonymousValues() throws Exception {
        Argument multiArg = new ArgumentImpl("multiarg", "multi argument", 0, 2);
        List<Option> options = new ArrayList<Option>();
        options.add(multiArg);
        GroupImpl groupMulti = new GroupImpl(options, "group", "group desc", 0, 2, false);
        WriteableCommandLine cl = createCommandLine();
        List<String> args = Arrays.asList("value1", "value2");
        ListIterator<String> it = args.listIterator();
        groupMulti.process(cl, it);
        assertTrue(cl.getValues(multiArg).contains("value1"));
        assertTrue(cl.getValues(multiArg).contains("value2"));
        assertEquals(2, cl.getValues(multiArg).size());
    }

    // Test process with known option followed by anonymous argument
    @Test
    public void testProcess_knownOptionThenAnonymousValue() throws Exception {
        setUpGroup(true, 1, 2);
        WriteableCommandLine cl = createCommandLine();
        List<String> args = Arrays.asList("foo", "anonvalue");
        ListIterator<String> it = args.listIterator();
        group.process(cl, it);
        assertTrue(cl.hasOption(foo));
        assertTrue(cl.getValues(arg).contains("anonvalue"));
    }

    // Test validate with optional group and no options should not throw
    @Test
    public void testValidate_notRequiredNoOptions_doesNotThrow() throws Exception {
        List<Option> options = new ArrayList<Option>();
        GroupImpl emptyGroup = new GroupImpl(options, "empty", "empty", 0, 0, false);
        WriteableCommandLine cl = createCommandLine();
        emptyGroup.validate(cl); // should not throw
    }

    // Test appendUsage for a required group does not start with bracket
    @Test
    public void testAppendUsage_requiredGroup_doesNotStartWithBracket() {
        setUpGroup(true, 1, 2);
        Set<DisplaySetting> helpSettings = new HashSet<DisplaySetting>();
        helpSettings.add(DisplaySetting.DISPLAY_OPTIONAL);
        StringBuffer buffer = new StringBuffer();
        group.appendUsage(buffer, helpSettings, null);
        assertFalse(buffer.toString().startsWith("["));
    }

    // Test process with unknown plain string (not option-like) and no anonymous
    @Test
    public void testProcess_unknownPlainString_noAnonymous() throws Exception {
        setUpGroup(true, 1, 2);
        group = createGroupWithoutAnonymous();
        WriteableCommandLine cl = createCommandLine();
        List<String> args = Collections.singletonList("plainstring");
        ListIterator<String> it = args.listIterator();
        group.process(cl, it);
        assertFalse(cl.hasOption(foo));
        assertFalse(cl.hasOption(bar));
    }

    // Test process with a subgroup containing an option
    @Test
    public void testProcess_subGroupOption() throws Exception {
        Option subFoo = new DefaultOption("subfoo", "subfoo option", false, false, null, 0, 0,
                Collections.singleton("-"), Collections.singleton("subfoo"));
        List<Option> subOptions = new ArrayList<Option>();
        subOptions.add(subFoo);
        GroupImpl subGroup = new GroupImpl(subOptions, "sub", "sub group", 0, 1, false);
        List<Option> parentOptions = new ArrayList<Option>();
        parentOptions.add(subGroup);
        GroupImpl parentGroup = new GroupImpl(parentOptions, "parent", "parent group", 0, 1, true);
        WriteableCommandLine cl = createCommandLine();
        List<String> args = Collections.singletonList("subfoo");
        ListIterator<String> it = args.listIterator();
        parentGroup.process(cl, it);
        assertTrue(cl.hasOption(subFoo));
    }

    // Test canProcess with a trigger inside a subgroup
    @Test
    public void testCanProcess_subGroupTrigger() {
        Option subFoo = new DefaultOption("subfoo", "subfoo option", false, false, null, 0, 0,
                Collections.singleton("-"), Collections.singleton("subfoo"));
        List<Option> subOptions = new ArrayList<Option>();
        subOptions.add(subFoo);
        GroupImpl subGroup = new GroupImpl(subOptions, "sub", "sub group", 0, 1, false);
        List<Option> parentOptions = new ArrayList<Option>();
        parentOptions.add(subGroup);
        GroupImpl parentGroup = new GroupImpl(parentOptions, "parent", "parent group", 0, 1, true);
        WriteableCommandLine cl = createCommandLine();
        assertTrue(parentGroup.canProcess(cl, "subfoo"));
    }
}