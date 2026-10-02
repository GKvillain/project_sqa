package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
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

    // Tests adding an option with short name and description only
    @Test
    public void testAddOption_shortOnly_addedSuccessfully()
    {
        options.addOption("a", "description a");
        assertTrue(options.hasOption("a"));
        assertTrue(options.hasShortOption("a"));
        assertFalse(options.hasLongOption("a"));
        assertNotNull(options.getOption("a"));
    }

    // Tests adding an option with short name, hasArg flag, and description
    @Test
    public void testAddOption_shortAndHasArg_addedSuccessfully()
    {
        options.addOption("b", true, "description b");
        assertTrue(options.hasOption("b"));
        assertTrue(options.hasShortOption("b"));
        assertEquals("description b", options.getOption("b").getDescription());
        assertTrue(options.getOption("b").hasArg());
    }

    // Tests adding an option with short and long name, hasArg, and description
    @Test
    public void testAddOption_shortAndLong_addedSuccessfully()
    {
        options.addOption("c", "count", true, "description count");
        assertTrue(options.hasOption("c"));
        assertTrue(options.hasOption("count"));
        assertTrue(options.hasShortOption("c"));
        assertTrue(options.hasLongOption("count"));
        assertEquals(options.getOption("c"), options.getOption("count"));
    }

    // Tests adding Option instance with longOpt and required flag
    @Test
    public void testAddOption_requiredOption_storedInRequiredOptions()
    {
        Option requiredOpt = new Option("r", "required", false, "required option");
        requiredOpt.setRequired(true);
        options.addOption(requiredOpt);

        assertTrue(options.getRequiredOptions().contains("r"));
        assertEquals(1, options.getRequiredOptions().size());

        // Re-adding the same required option should not duplicate key in requiredOpts
        options.addOption(requiredOpt);
        assertEquals(1, options.getRequiredOptions().size());
    }

    // Tests getOption and hasOption with leading hyphens stripped
    @Test
    public void testGetOption_withHyphens_returnsOption()
    {
        options.addOption("d", "debug", false, "debug option");
        assertNotNull(options.getOption("-d"));
        assertNotNull(options.getOption("--debug"));
        assertTrue(options.hasOption("-d"));
        assertTrue(options.hasOption("--debug"));
        assertTrue(options.hasShortOption("-d"));
        assertTrue(options.hasLongOption("--debug"));
    }

    // Tests getOption with non-existent option returns null
    @Test
    public void testGetOption_nonExistent_returnsNull()
    {
        assertNull(options.getOption("nonexistent"));
        assertFalse(options.hasOption("nonexistent"));
        assertFalse(options.hasShortOption("nonexistent"));
        assertFalse(options.hasLongOption("nonexistent"));
    }

    // Tests getMatchingOptions when an exact match exists among prefixes (CLI-253 / bug 35b)
    @Test
    public void testGetMatchingOptions_exactMatch_returnsOnlyExactMatch()
    {
        options.addOption(new Option("d", "date", false, "date option"));
        options.addOption(new Option("dt", "datetime", false, "datetime option"));

        List<String> matches = options.getMatchingOptions("date");
        assertNotNull(matches);
        assertEquals(1, matches.size());
        assertEquals("date", matches.get(0));
    }

    // Tests getMatchingOptions partial matching when no exact match exists
    @Test
    public void testGetMatchingOptions_partialMatch_returnsAllMatching()
    {
        options.addOption(new Option("e", "export", false, "export"));
        options.addOption(new Option("ex", "export-all", false, "export all"));

        List<String> matches = options.getMatchingOptions("exp");
        assertEquals(2, matches.size());
        assertTrue(matches.contains("export"));
        assertTrue(matches.contains("export-all"));
    }

    // Tests getMatchingOptions when no option matches
    @Test
    public void testGetMatchingOptions_noMatch_returnsEmptyList()
    {
        options.addOption(new Option("f", "file", false, "file option"));
        List<String> matches = options.getMatchingOptions("foo");
        assertTrue(matches.isEmpty());
    }

    // Tests adding an OptionGroup and verifying group members and requirements
    @Test
    public void testAddOptionGroup_requiredGroup_addsGroupAndOptions()
    {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("h", "help", false, "help");
        Option opt2 = new Option("v", "version", false, "version");
        group.addOption(opt1);
        group.addOption(opt2);
        group.setRequired(true);

        options.addOptionGroup(group);

        assertTrue(options.hasOption("h"));
        assertTrue(options.hasOption("v"));
        assertEquals(group, options.getOptionGroup(opt1));
        assertEquals(group, options.getOptionGroup(opt2));
        assertTrue(options.getRequiredOptions().contains(group));
        assertEquals(1, options.getOptionGroups().size());
        assertFalse(opt1.isRequired());
        assertFalse(opt2.isRequired());
    }

    // Tests getOptionGroup for an option not in any group
    @Test
    public void testGetOptionGroup_notInGroup_returnsNull()
    {
        Option opt = new Option("k", "key", false, "key");
        options.addOption(opt);
        assertNull(options.getOptionGroup(opt));
    }

    // Tests getOptions and helpOptions return all added options
    @Test
    public void testGetOptions_returnsAllOptions()
    {
        options.addOption("o1", "opt1");
        options.addOption("o2", "opt2");

        Collection<Option> allOptions = options.getOptions();
        assertEquals(2, allOptions.size());

        List<Option> helpOpts = options.helpOptions();
        assertEquals(2, helpOpts.size());
    }

    // Tests toString format contains short and long options
    @Test
    public void testToString_notNull_containsInformation()
    {
        options.addOption("s", "silent", false, "silent mode");
        String str = options.toString();
        assertNotNull(str);
        assertTrue(str.contains("silent"));
        assertTrue(str.contains("short"));
        assertTrue(str.contains("long"));
    }
}