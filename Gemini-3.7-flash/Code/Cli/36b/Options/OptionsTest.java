package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OptionsTest
{
    private Options options;

    @Before
    public void setUp()
    {
        options = new Options();
    }

    // Tests adding option with short name and description only
    @Test
    public void testAddOption_shortOptAndDescription_addsOptionSuccessfully()
    {
        options.addOption("a", "description a");

        assertTrue(options.hasOption("a"));
        assertTrue(options.hasShortOption("a"));
        assertFalse(options.hasLongOption("a"));
        assertEquals("description a", options.getOption("a").getDescription());
    }

    // Tests adding option with short name, hasArg flag, and description
    @Test
    public void testAddOption_shortOptWithArg_addsOptionWithArg()
    {
        options.addOption("b", true, "description b");

        assertTrue(options.hasOption("b"));
        assertTrue(options.getOption("b").hasArg());
    }

    // Tests adding option with both short and long names
    @Test
    public void testAddOption_shortAndLongOpt_addsOptionToBothMaps()
    {
        options.addOption("c", "config", false, "description c");

        assertTrue(options.hasOption("c"));
        assertTrue(options.hasOption("config"));
        assertTrue(options.hasShortOption("c"));
        assertTrue(options.hasLongOption("config"));
        assertFalse(options.hasLongOption("c"));
        assertFalse(options.hasShortOption("config"));
        assertEquals(options.getOption("c"), options.getOption("config"));
    }

    // Tests adding an Option instance that is required
    @Test
    public void testAddOption_requiredOption_addsToRequiredOptionsList()
    {
        Option opt = new Option("r", "req", false, "required option");
        opt.setRequired(true);
        options.addOption(opt);

        List<?> requiredOpts = options.getRequiredOptions();
        assertEquals(1, requiredOpts.size());
        assertTrue(requiredOpts.contains("r"));

        // Re-adding the same required option should update without duplicate
        options.addOption(opt);
        assertEquals(1, options.getRequiredOptions().size());
    }

    // Tests retrieving option with leading hyphens
    @Test
    public void testGetOption_withLeadingHyphens_returnsCorrectOption()
    {
        options.addOption("d", "debug", false, "debug option");

        Option shortOpt = options.getOption("-d");
        Option longOpt = options.getOption("--debug");

        assertNotNull(shortOpt);
        assertNotNull(longOpt);
        assertEquals(shortOpt, longOpt);
    }

    // Tests retrieving non-existent option returns null
    @Test
    public void testGetOption_nonExistentOption_returnsNull()
    {
        assertNull(options.getOption("unknown"));
        assertNull(options.getOption("--unknown"));
    }

    // Tests matching options with exact match
    @Test
    public void testGetMatchingOptions_exactMatch_returnsSingletonList()
    {
        options.addOption("v", "version", false, "version");
        options.addOption("ver", "verbose", false, "verbose");

        List<String> matches = options.getMatchingOptions("version");
        assertEquals(1, matches.size());
        assertEquals("version", matches.get(0));
    }

    // Tests matching options with partial prefix match
    @Test
    public void testGetMatchingOptions_prefixMatch_returnsAllMatchingLongOptions()
    {
        options.addOption(null, "foo", false, "foo opt");
        options.addOption(null, "foobar", false, "foobar opt");
        options.addOption(null, "bar", false, "bar opt");

        List<String> matches = options.getMatchingOptions("--fo");
        assertEquals(2, matches.size());
        assertTrue(matches.contains("foo"));
        assertTrue(matches.contains("foobar"));
        assertFalse(matches.contains("bar"));
    }

    // Tests matching options when no option matches
    @Test
    public void testGetMatchingOptions_noMatch_returnsEmptyList()
    {
        options.addOption("f", "file", true, "file opt");

        List<String> matches = options.getMatchingOptions("dir");
        assertNotNull(matches);
        assertTrue(matches.isEmpty());
    }

    // Tests hasOption, hasLongOption, and hasShortOption methods with hyphens
    @Test
    public void testHasOption_withHyphenatedNames_returnsTrue()
    {
        options.addOption("s", "silent", false, "silent mode");

        assertTrue(options.hasOption("-s"));
        assertTrue(options.hasOption("--silent"));
        assertTrue(options.hasShortOption("-s"));
        assertTrue(options.hasLongOption("--silent"));
        assertFalse(options.hasShortOption("--silent"));
        assertFalse(options.hasLongOption("-s"));
    }

    // Tests adding an OptionGroup and verifying group associations
    @Test
    public void testAddOptionGroup_validOptionGroup_associatesGroupCorrectly()
    {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("f", "file", false, "file option");
        Option opt2 = new Option("d", "dir", false, "directory option");
        group.addOption(opt1);
        group.addOption(opt2);

        options.addOptionGroup(group);

        Collection<OptionGroup> groups = options.getOptionGroups();
        assertEquals(1, groups.size());
        assertTrue(groups.contains(group));
        assertEquals(group, options.getOptionGroup(opt1));
        assertEquals(group, options.getOptionGroup(opt2));
        assertFalse(opt1.isRequired());
        assertFalse(opt2.isRequired());
    }

    // Tests adding a required OptionGroup
    @Test
    public void testAddOptionGroup_requiredGroup_addsGroupToRequiredOptions()
    {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        Option opt = new Option("x", "exclusive", false, "exclusive opt");
        group.addOption(opt);

        options.addOptionGroup(group);

        List<?> requiredOpts = options.getRequiredOptions();
        assertEquals(1, requiredOpts.size());
        assertTrue(requiredOpts.contains(group));
    }

    // Tests getOptionGroup for option not belonging to any group
    @Test
    public void testGetOptionGroup_optionNotInGroup_returnsNull()
    {
        Option opt = new Option("n", "none", false, "no group");
        options.addOption(opt);

        assertNull(options.getOptionGroup(opt));
    }

    // Tests getOptions returns read-only collection containing all added options
    @Test
    public void testGetOptions_afterAddingOptions_returnsUnmodifiableCollection()
    {
        options.addOption("a", "optA");
        options.addOption("b", "optB");

        Collection<Option> allOptions = options.getOptions();
        assertEquals(2, allOptions.size());
    }

    // Tests modifying getOptions collection throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testGetOptions_attemptModification_throwsException()
    {
        options.addOption("a", "optA");
        Collection<Option> allOptions = options.getOptions();
        allOptions.clear();
    }

    // Tests modifying getRequiredOptions list throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testGetRequiredOptions_attemptModification_throwsException()
    {
        Option opt = new Option("r", "req", false, "desc");
        opt.setRequired(true);
        options.addOption(opt);

        List required = options.getRequiredOptions();
        required.clear();
    }

    // Tests toString method returns non-empty formatted debug string
    @Test
    public void testToString_withOptions_returnsFormattedString()
    {
        options.addOption("h", "help", false, "display help");

        String result = options.toString();
        assertNotNull(result);
        assertTrue(result.contains("[ Options: [ short "));
        assertTrue(result.contains("help"));
    }
}