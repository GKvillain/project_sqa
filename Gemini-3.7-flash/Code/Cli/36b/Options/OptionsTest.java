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

    // Tests adding simple short option with description
    @Test
    public void testAddOption_shortOnlyWithDescription_optionAddedSuccessfully()
    {
        options.addOption("a", "description a");
        assertTrue(options.hasOption("a"));
        assertTrue(options.hasShortOption("a"));
        assertFalse(options.hasLongOption("a"));
        assertEquals("description a", options.getOption("a").getDescription());
    }

    // Tests adding short option with boolean hasArg flag
    @Test
    public void testAddOption_shortWithHasArg_optionHasArgConfigured()
    {
        options.addOption("b", true, "description b");
        Option opt = options.getOption("b");
        assertNotNull(opt);
        assertTrue(opt.hasArg());
        assertEquals("description b", opt.getDescription());
    }

    // Tests adding option with short and long name
    @Test
    public void testAddOption_shortAndLongOpt_bothNamesAccessible()
    {
        options.addOption("c", "config", true, "description c");
        assertTrue(options.hasOption("c"));
        assertTrue(options.hasOption("config"));
        assertTrue(options.hasOption("--config"));
        assertTrue(options.hasOption("-c"));
        assertTrue(options.hasShortOption("c"));
        assertTrue(options.hasLongOption("config"));
        assertEquals(options.getOption("c"), options.getOption("config"));
    }

    // Tests adding an option with only long name
    @Test
    public void testAddOption_longOptOnly_retrievableByLongName()
    {
        Option longOnly = new Option(null, "verbose", false, "verbose mode");
        options.addOption(longOnly);
        assertTrue(options.hasOption("verbose"));
        assertTrue(options.hasOption("--verbose"));
        assertTrue(options.hasLongOption("verbose"));
        assertFalse(options.hasShortOption("verbose"));
        assertEquals(longOnly, options.getOption("verbose"));
        assertEquals(longOnly, options.getOption("--verbose"));
    }

    // Tests getOption with non-existent option
    @Test
    public void testGetOption_nonExistent_returnsNull()
    {
        options.addOption("a", "opt a");
        assertNull(options.getOption("nonexistent"));
        assertNull(options.getOption("--unknown"));
        assertFalse(options.hasOption("nonexistent"));
        assertFalse(options.hasLongOption("nonexistent"));
        assertFalse(options.hasShortOption("nonexistent"));
    }

    // Tests adding required option and retrieving required options list
    @Test
    public void testGetRequiredOptions_withRequiredOption_returnsRequiredKeys()
    {
        Option reqOpt = new Option("r", "require", true, "required option");
        reqOpt.setRequired(true);
        options.addOption(reqOpt);

        List required = options.getRequiredOptions();
        assertEquals(1, required.size());
        assertTrue(required.contains("r"));

        // Add again to test duplicate handling branch
        options.addOption(reqOpt);
        assertEquals(1, options.getRequiredOptions().size());
    }

    // Tests addOptionGroup with required and non-required group
    @Test
    public void testAddOptionGroup_requiredAndNonRequiredGroup_correctlyAssociated()
    {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("f", "file", false, "file opt");
        Option opt2 = new Option("d", "dir", false, "dir opt");
        opt1.setRequired(true); // Should be reset to false inside addOptionGroup
        group.addOption(opt1);
        group.addOption(opt2);
        group.setRequired(true);

        options.addOptionGroup(group);

        assertFalse(opt1.isRequired());
        assertEquals(group, options.getOptionGroup(opt1));
        assertEquals(group, options.getOptionGroup(opt2));
        assertTrue(options.getRequiredOptions().contains(group));

        Collection<OptionGroup> groups = options.getOptionGroups();
        assertEquals(1, groups.size());
        assertTrue(groups.contains(group));
    }

    // Tests getOptionGroup for option that has only long name
    @Test
    public void testGetOptionGroup_longOptOnlyInGroup_returnsOptionGroup()
    {
        OptionGroup group = new OptionGroup();
        Option longOnly = new Option(null, "foo", false, "foo opt");
        group.addOption(longOnly);
        options.addOptionGroup(group);

        assertEquals(group, options.getOptionGroup(longOnly));
        assertTrue(options.hasOption("foo"));
        assertTrue(options.hasLongOption("foo"));
    }

    // Tests getOptionGroup for option not in any group
    @Test
    public void testGetOptionGroup_optionNotInGroup_returnsNull()
    {
        Option standalone = new Option("s", "standalone", false, "standalone");
        options.addOption(standalone);
        assertNull(options.getOptionGroup(standalone));
    }

    // Tests getMatchingOptions with exact match
    @Test
    public void testGetMatchingOptions_exactMatch_returnsSingleOption()
    {
        options.addOption("v", "version", false, "display version");
        options.addOption(null, "verbose", false, "verbose output");

        List<String> matches = options.getMatchingOptions("version");
        assertEquals(1, matches.size());
        assertEquals("version", matches.get(0));

        List<String> strippedMatches = options.getMatchingOptions("--version");
        assertEquals(1, strippedMatches.size());
        assertEquals("version", strippedMatches.get(0));
    }

    // Tests getMatchingOptions with partial prefix match
    @Test
    public void testGetMatchingOptions_partialMatch_returnsAllMatchingLongOpts()
    {
        options.addOption("v", "version", false, "display version");
        options.addOption(null, "verbose", false, "verbose output");
        options.addOption("h", "help", false, "display help");

        List<String> matches = options.getMatchingOptions("ver");
        assertEquals(2, matches.size());
        assertTrue(matches.contains("version"));
        assertTrue(matches.contains("verbose"));
    }

    // Tests getMatchingOptions with no match
    @Test
    public void testGetMatchingOptions_noMatch_returnsEmptyList()
    {
        options.addOption("v", "version", false, "display version");
        List<String> matches = options.getMatchingOptions("xyz");
        assertTrue(matches.isEmpty());
    }

    // Tests getOptions and helpOptions
    @Test
    public void testGetOptions_returnsAllShortAndLongOnlyOptions()
    {
        Option optA = new Option("a", "alpha", false, "option A");
        Option optB = new Option("b", false, "option B");
        Option optC = new Option(null, "charlie", false, "option C");

        options.addOption(optA);
        options.addOption(optB);
        options.addOption(optC);

        Collection<Option> allOpts = options.getOptions();
        assertEquals(3, allOpts.size());
        assertTrue(allOpts.contains(optA));
        assertTrue(allOpts.contains(optB));
        assertTrue(allOpts.contains(optC));

        List<Option> helpOpts = options.helpOptions();
        assertEquals(3, helpOpts.size());
    }

    // Tests getOptions returns unmodifiable collection
    @Test(expected = UnsupportedOperationException.class)
    public void testGetOptions_modifyReturnedCollection_throwsException()
    {
        options.addOption("a", "description a");
        Collection<Option> opts = options.getOptions();
        opts.clear();
    }

    // Tests getRequiredOptions returns unmodifiable list
    @Test(expected = UnsupportedOperationException.class)
    public void testGetRequiredOptions_modifyReturnedList_throwsException()
    {
        Option opt = new Option("r", true, "req");
        opt.setRequired(true);
        options.addOption(opt);
        options.getRequiredOptions().clear();
    }

    // Tests toString method contains short and long option representations
    @Test
    public void testToString_formatsCorrectly()
    {
        options.addOption("a", "alpha", false, "description a");
        String result = options.toString();
        assertNotNull(result);
        assertTrue(result.contains("[ Options: [ short "));
        assertTrue(result.contains("alpha"));
    }
}