package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class for {@link Options} targeting Defects4J bug 35b.
 * Focuses on behavior of options collection, matching, required options, and option groups.
 */
public class OptionsTest {

    // Test addOption with short name only and verify it's added
    @Test
    public void testAddOption_shortNameOnly_optionAdded() {
        Options opts = new Options();
        opts.addOption("a", "description");
        
        assertTrue("Should have short option 'a'", opts.hasOption("a"));
        assertTrue("Should have short option with leading hyphen", opts.hasOption("-a"));
        assertTrue("Should have short option with double hyphens", opts.hasOption("--a"));
    }

    // Test addOption with short and long name, verify both accessible
    @Test
    public void testAddOption_shortAndLongNames_bothAccessible() {
        Options opts = new Options();
        opts.addOption("b", "long-b", true, "desc");
        
        assertTrue("Should have short option", opts.hasShortOption("b"));
        assertTrue("Should have long option", opts.hasLongOption("long-b"));
        assertTrue("Should have either", opts.hasOption("b"));
        assertTrue("Should have either with hyphens", opts.hasOption("-long-b"));
    }

    // Test addOption with null long name - should still work with short
    @Test
    public void testAddOption_nullLongName_shortOptionOnly() {
        Options opts = new Options();
        opts.addOption("c", null, false, "desc");
        
        assertTrue("Short option should exist", opts.hasShortOption("c"));
        assertFalse("Long option should not exist", opts.hasLongOption("c"));
    }

    // Test addOption with null short name - should use long as key but short absent
    @Test
    public void testAddOption_nullShortName_usesLongAsKey() {
        Options opts = new Options();
        opts.addOption(null, "long-d", false, "desc");
        
        assertTrue("Long option should exist", opts.hasLongOption("long-d"));
        assertFalse("Short option should not exist", opts.hasShortOption("d"));
    }

    // Test addOption with null both names - should not be added (key null)
    @Test
    public void testAddOption_nullBothNames_notAdded() {
        Options opts = new Options();
        opts.addOption(null, null, false, "desc");
        
        assertFalse("Options map should not contain null key", opts.hasOption(null));
        assertEquals("getOptions should be empty after adding null option", 0, opts.getOptions().size());
    }

    // Test getOption with short and long names
    @Test
    public void testGetOption_validName_returnsOption() {
        Options opts = new Options();
        opts.addOption("e", "long-e", true, "desc");
        
        Option shortOpt = opts.getOption("e");
        assertNotNull("Short option should be returned", shortOpt);
        assertEquals("Short option key should be 'e'", "e", shortOpt.getKey());
        
        Option longOpt = opts.getOption("long-e");
        assertNotNull("Long option should be returned", longOpt);
        assertEquals("Long option key should be 'e'", "e", longOpt.getKey());
    }

    // Test getOption with hyphens stripped
    @Test
    public void testGetOption_withHyphens_stripsThem() {
        Options opts = new Options();
        opts.addOption("f", "long-f", false, "desc");
        
        assertNotNull("Option with single hyphen", opts.getOption("-f"));
        assertNotNull("Option with double hyphens", opts.getOption("--long-f"));
    }

    // Test getOption for non-existent option returns null
    @Test
    public void testGetOption_nonExistent_returnsNull() {
        Options opts = new Options();
        opts.addOption("g", "long-g", false, "desc");
        
        assertNull("Non-existent short option should return null", opts.getOption("z"));
        assertNull("Non-existent long option should return null", opts.getOption("long-z"));
    }

    // Test getMatchingOptions with perfect match
    @Test
    public void testGetMatchingOptions_perfectMatch_returnsSingle() {
        Options opts = new Options();
        opts.addOption("h", "long-h", false, "desc");
        opts.addOption("i", "long-i", false, "desc");
        
        assertEquals("Perfect match should return the long opt", 
                     java.util.Arrays.asList("long-h"), 
                     opts.getMatchingOptions("long-h"));
    }

    // Test getMatchingOptions with partial match - no guarantee of perfect matching logic here, but verify prefix matches
    @Test
    public void testGetMatchingOptions_partialMatch_returnsAllMatching() {
        Options opts = new Options();
        opts.addOption("j", "long-jack", false, "desc");
        opts.addOption("k", "long-king", false, "desc");
        opts.addOption("l", "other", false, "desc");
        
        java.util.List<String> matches = opts.getMatchingOptions("long-");
        assertTrue("Should match both long-jack and long-king", matches.contains("long-jack"));
        assertTrue("Should match long-king", matches.contains("long-king"));
        assertEquals("Should have 2 matches", 2, matches.size());
    }

    // Test getMatchingOptions with no match returns empty list
    @Test
    public void testGetMatchingOptions_noMatch_returnsEmptyList() {
        Options opts = new Options();
        opts.addOption("m", "long-m", false, "desc");
        
        assertEquals("No matches should return empty list", 
                     java.util.Collections.emptyList(), 
                     opts.getMatchingOptions("nonexistent"));
    }

    // Test getMatchingOptions with hyphen stripped
    @Test
    public void testGetMatchingOptions_withHyphens_stripsThem() {
        Options opts = new Options();
        opts.addOption("n", "long-nice", false, "desc");
        
        assertEquals("Hyphens should be stripped before matching", 
                     java.util.Arrays.asList("long-nice"), 
                     opts.getMatchingOptions("--long-n"));
    }

    // Test hasOption with exact match
    @Test
    public void testHasOption_existingOption_returnsTrue() {
        Options opts = new Options();
        opts.addOption("o", "long-o", false, "desc");
        
        assertTrue("Short option should exist", opts.hasOption("o"));
        assertTrue("Long option should exist", opts.hasOption("long-o"));
    }

    // Test hasOption with non-existing option returns false
    @Test
    public void testHasOption_nonExistent_returnsFalse() {
        Options opts = new Options();
        opts.addOption("p", "long-p", false, "desc");
        
        assertFalse("Non-existent option should return false", opts.hasOption("zzz"));
    }

    // Test hasLongOption and hasShortOption
    @Test
    public void testHasLongOption_hasShortOption_respectively() {
        Options opts = new Options();
        opts.addOption("q", "long-q", true, "desc");
        
        assertTrue("Long option exists", opts.hasLongOption("long-q"));
        assertFalse("Long option not short", opts.hasLongOption("q"));
        assertFalse("Short option not long", opts.hasShortOption("long-q"));
        assertTrue("Short option exists", opts.hasShortOption("q"));
    }

    // Test required options list when adding required option
    @Test
    public void testAddOption_requiredOption_addedToRequiredList() {
        Options opts = new Options();
        Option req = new Option("r", "long-r", false, "desc");
        req.setRequired(true);
        opts.addOption(req);
        
        java.util.List<?> required = opts.getRequiredOptions();
        assertEquals("Required list should have size 1", 1, required.size());
        assertEquals("Required list should contain the key", "r", required.get(0));
    }

    // Test required options list when adding non-required then required (bug 35b related: duplicate key handling)
    @Test
    public void testAddOption_duplicateRequiredReplacesOld() {
        Options opts = new Options();
        Option opt1 = new Option("s", "long-s", false, "desc");
        opt1.setRequired(true);
        opts.addOption(opt1);
        
        Option opt2 = new Option("s", "long-s2", false, "desc2");
        opt2.setRequired(true);
        opts.addOption(opt2);
        
        java.util.List<?> required = opts.getRequiredOptions();
        assertEquals("Should have only one entry for duplicate key", 1, required.size());
        assertEquals("Should have the key 's'", "s", required.get(0));
    }

    // Test addOptionGroup when group not required
    @Test
    public void testAddOptionGroup_notRequired_optionsAdded() {
        Options opts = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("t", "long-t", false, "desc"));
        group.addOption(new Option("u", "long-u", false, "desc"));
        
        opts.addOptionGroup(group);
        
        assertTrue("Option t should exist", opts.hasOption("t"));
        assertTrue("Option u should exist", opts.hasOption("u"));
        assertEquals("getOptions should have size 2", 2, opts.getOptions().size());
        // OptionGroup not required so required list should be empty
        assertEquals("Required list should be empty", 0, opts.getRequiredOptions().size());
    }

    // Test addOptionGroup when group is required - adds group to required list
    @Test
    public void testAddOptionGroup_required_groupAddedToRequired() {
        Options opts = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("v", "long-v", false, "desc"));
        group.setRequired(true);
        
        opts.addOptionGroup(group);
        
        java.util.List<?> required = opts.getRequiredOptions();
        assertEquals("Required list should have the group", 1, required.size());
        assertTrue("Required list should contain the group object", required.get(0) instanceof OptionGroup);
    }

    // Test getOptionGroup returns correct group
    @Test
    public void testGetOptionGroup_existingOption_returnsGroup() {
        Options opts = new Options();
        OptionGroup group = new OptionGroup();
        Option opt = new Option("w", "long-w", false, "desc");
        group.addOption(opt);
        opts.addOptionGroup(group);
        
        OptionGroup result = opts.getOptionGroup(opt);
        assertNotNull("Should return group for option", result);
        assertSame("Should return the same group object", group, result);
    }

    // Test getOptionGroup for option not in group returns null
    @Test
    public void testGetOptionGroup_nonGroupedOption_returnsNull() {
        Options opts = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("x", "long-x", false, "desc"));
        opts.addOptionGroup(group);
        
        Option notInGroup = new Option("y", "long-y", false, "desc");
        assertNull("Option not in group should return null", opts.getOptionGroup(notInGroup));
    }

    // Test getOptions returns read-only collection and includes added options
    @Test
    public void testGetOptions_returnsAllAddedOptions() {
        Options opts = new Options();
        opts.addOption("a", "long-a", false, "desc");
        opts.addOption("b", "long-b", true, "desc2");
        
        java.util.Collection<Option> options = opts.getOptions();
        assertEquals("Should have 2 options", 2, options.size());
    }

    // Test toString includes short and long maps (basic check)
    @Test
    public void testToString_containsShortAndLongMaps() {
        Options opts = new Options();
        opts.addOption("z", "long-z", false, "desc");
        
        String str = opts.toString();
        assertTrue("toString should contain short map output", str.contains("short"));
        assertTrue("toString should contain long map output", str.contains("long"));
    }
}