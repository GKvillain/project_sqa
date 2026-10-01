package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

public class OptionsTest {

    // Tests addOption with short name only, hasArg=false
    @Test
    public void testAddOption_shortNameNoArg_returnsOptions() {
        Options options = new Options();
        options.addOption("a", "description");
        assertTrue(options.hasOption("a"));
        assertFalse(options.hasOption("-a"));
        assertFalse(options.hasOption("--a"));
        Option opt = options.getOption("a");
        assertNotNull(opt);
        assertEquals("a", opt.getOpt());
        assertFalse(opt.hasArg());
        assertEquals("description", opt.getDescription());
        assertEquals(1, options.getOptions().size());
        assertTrue(options.getOptions().contains(opt));
    }

    // Tests addOption with short name and hasArg=true
    @Test
    public void testAddOption_shortNameWithArg_returnsOptions() {
        Options options = new Options();
        options.addOption("b", true, "description");
        assertTrue(options.hasOption("b"));
        Option opt = options.getOption("b");
        assertNotNull(opt);
        assertTrue(opt.hasArg());
        assertEquals("b", opt.getOpt());
        assertNull(opt.getLongOpt());
        assertEquals(1, options.getOptions().size());
    }

    // Tests addOption with short and long name, hasArg=false
    @Test
    public void testAddOption_shortAndLongNameNoArg_returnsOptions() {
        Options options = new Options();
        options.addOption("c", "long-c", false, "description");
        assertTrue(options.hasOption("c"));
        assertTrue(options.hasOption("long-c"));
        assertTrue(options.hasShortOption("c"));
        assertTrue(options.hasLongOption("long-c"));
        Option opt = options.getOption("c");
        assertNotNull(opt);
        assertEquals("c", opt.getOpt());
        assertEquals("long-c", opt.getLongOpt());
        assertFalse(opt.hasArg());
        assertEquals(1, options.getOptions().size());
    }

    // Tests addOption with short and long name, hasArg=true
    @Test
    public void testAddOption_shortAndLongNameWithArg_returnsOptions() {
        Options options = new Options();
        options.addOption("d", "long-d", true, "description");
        assertTrue(options.hasOption("d"));
        assertTrue(options.hasOption("--long-d"));
        Option opt = options.getOption("d");
        assertNotNull(opt);
        assertTrue(opt.hasArg());
        assertEquals("long-d", opt.getLongOpt());
        assertEquals(1, options.getOptions().size());
    }

    // Tests addOption with Option instance, required option
    @Test
    public void testAddOption_optionInstanceRequired_addedToRequiredList() {
        Options options = new Options();
        Option opt = new Option("e", "long-e", true, "description");
        opt.setRequired(true);
        options.addOption(opt);
        assertTrue(options.hasOption("e"));
        assertEquals(1, options.getRequiredOptions().size());
        assertEquals("e", options.getRequiredOptions().get(0));
    }

    // Tests addOption with Option instance, non-required option not added to required list
    @Test
    public void testAddOption_optionInstanceNotRequired_notInRequiredList() {
        Options options = new Options();
        Option opt = new Option("f", "long-f", false, "description");
        options.addOption(opt);
        assertTrue(options.hasOption("f"));
        assertEquals(0, options.getRequiredOptions().size());
    }

    // Tests addOption with null description
    @Test
    public void testAddOption_nullDescription_returnsOptions() {
        Options options = new Options();
        options.addOption("g", null);
        assertTrue(options.hasOption("g"));
        Option opt = options.getOption("g");
        assertNotNull(opt);
        assertNull(opt.getDescription());
    }

    // Tests getOption with short name
    @Test
    public void testGetOption_shortName_returnsOption() {
        Options options = new Options();
        options.addOption("h", "long-h", false, "description");
        Option opt = options.getOption("h");
        assertNotNull(opt);
        assertEquals("h", opt.getOpt());
    }

    // Tests getOption with long name
    @Test
    public void testGetOption_longName_returnsOption() {
        Options options = new Options();
        options.addOption("i", "long-i", false, "description");
        Option opt = options.getOption("long-i");
        assertNotNull(opt);
        assertEquals("i", opt.getOpt());
    }

    // Tests getOption with leading hyphens stripped
    @Test
    public void testGetOption_withLeadingHyphens_returnsOption() {
        Options options = new Options();
        options.addOption("j", "long-j", false, "description");
        Option opt = options.getOption("-j");
        assertNotNull(opt);
        assertEquals("j", opt.getOpt());
        opt = options.getOption("--long-j");
        assertNotNull(opt);
        assertEquals("j", opt.getOpt());
    }

    // Tests getOption with non-existing name returns null
    @Test
    public void testGetOption_nonExistingName_returnsNull() {
        Options options = new Options();
        options.addOption("k", "long-k", false, "description");
        assertNull(options.getOption("nonexistent"));
    }

    // Tests getMatchingOptions with exact match returns single option
    @Test
    public void testGetMatchingOptions_exactMatch_returnsSingletonList() {
        Options options = new Options();
        options.addOption("l", "long-l", false, "description");
        assertEquals(1, options.getMatchingOptions("long-l").size());
        assertEquals("long-l", options.getMatchingOptions("long-l").get(0));
    }

    // Tests getMatchingOptions with prefix match returns matching options
    @Test
    public void testGetMatchingOptions_prefixMatch_returnsMatchingOptions() {
        Options options = new Options();
        options.addOption("m", "long-m1", false, "description");
        options.addOption("n", "long-m2", false, "description");
        assertEquals(2, options.getMatchingOptions("long-m").size());
        assertTrue(options.getMatchingOptions("long-m").contains("long-m1"));
        assertTrue(options.getMatchingOptions("long-m").contains("long-m2"));
    }

    // Tests getMatchingOptions with no match returns empty list
    @Test
    public void testGetMatchingOptions_noMatch_returnsEmptyList() {
        Options options = new Options();
        options.addOption("o", "long-o", false, "description");
        assertEquals(0, options.getMatchingOptions("nonexistent").size());
    }

    // Tests getMatchingOptions with leading hyphens stripped
    @Test
    public void testGetMatchingOptions_withLeadingHyphens_returnsMatchingOptions() {
        Options options = new Options();
        options.addOption("p", "long-p", false, "description");
        assertEquals(1, options.getMatchingOptions("-long-p").size());
        assertEquals("long-p", options.getMatchingOptions("--long-p").get(0));
    }

    // Tests getOptionGroup with option in group
    @Test
    public void testGetOptionGroup_optionInGroup_returnsGroup() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("q", "long-q", false, "description");
        group.addOption(opt1);
        options.addOptionGroup(group);
        assertNotNull(options.getOptionGroup(opt1));
        assertEquals(group, options.getOptionGroup(opt1));
    }

    // Tests getOptionGroup with option not in group returns null
    @Test
    public void testGetOptionGroup_optionNotInGroup_returnsNull() {
        Options options = new Options();
        Option opt = new Option("r", "long-r", false, "description");
        options.addOption(opt);
        assertNull(options.getOptionGroup(opt));
    }

    // Tests addOptionGroup with required group adds group to required list
    @Test
    public void testAddOptionGroup_requiredGroup_addedToRequiredList() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        Option opt = new Option("s", "long-s", false, "description");
        group.addOption(opt);
        options.addOptionGroup(group);
        assertEquals(1, options.getRequiredOptions().size());
        assertEquals(group, options.getRequiredOptions().get(0));
    }

    // Tests addOptionGroup with required group sets all options not required
    @Test
    public void testAddOptionGroup_groupOptions_setNotRequired() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        Option opt = new Option("t", "long-t", false, "description");
        opt.setRequired(true);
        group.addOption(opt);
        options.addOptionGroup(group);
        assertFalse(opt.isRequired());
    }

    // Tests getOptions returns unmodifiable collection
    @Test
    public void testGetOptions_returnsUnmodifiableCollection() {
        Options options = new Options();
        options.addOption("u", "long-u", false, "description");
        try {
            options.getOptions().add(new Option("v", "long-v", false, "description"));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // Tests getRequiredOptions returns unmodifiable list
    @Test
    public void testGetRequiredOptions_returnsUnmodifiableList() {
        Options options = new Options();
        options.addOption("w", "long-w", false, "description");
        try {
            options.getRequiredOptions().add("x");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // ===== New tests for uncovered parts (AlreadySelectedException and OptionGroup.setSelected) =====

    // Tests that setSelected(null) does not throw exception when no option is selected
    @Test
    public void testOptionGroupSetSelectedNullNoSelection() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("x", "desc");
        group.addOption(opt);
        // Should not throw AlreadySelectedException
        group.setSelected(null);
        assertNull(group.getSelected());
    }

    // Tests that setSelected(null) does not throw exception when an option is already selected
    // (since null clears the selection without conflict)
    @Test
    public void testOptionGroupSetSelectedNullWithSelection() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("y", "desc1");
        Option opt2 = new Option("z", "desc2");
        group.addOption(opt1);
        group.addOption(opt2);
        group.setSelected(opt1); // select opt1
        // Now setSelected(null) should clear selection without throwing
        group.setSelected(null);
        assertNull(group.getSelected());
    }

    // Tests that setSelected with a different option throws AlreadySelectedException
    @Test(expected = AlreadySelectedException.class)
    public void testOptionGroupSetSelectedDifferentOptionThrows() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("aa", "desc1");
        Option opt2 = new Option("bb", "desc2");
        group.addOption(opt1);
        group.addOption(opt2);
        group.setSelected(opt1);
        // Attempting to select a different option should throw
        group.setSelected(opt2);
    }

    // Tests that setSelected with the same option does not throw
    @Test
    public void testOptionGroupSetSelectedSameOptionNoThrow() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("cc", "desc");
        group.addOption(opt1);
        group.setSelected(opt1);
        // Selecting the same option again should be fine
        group.setSelected(opt1);
        assertEquals(opt1, group.getSelected());
    }
}