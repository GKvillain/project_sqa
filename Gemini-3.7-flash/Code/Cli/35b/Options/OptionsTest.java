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

    // Tests adding an option with short name and description
    @Test
    public void testAddOption_shortOptAndDescription_addsOptionCorrectly()
    {
        options.addOption("a", "description a");

        assertTrue(options.hasOption("a"));
        assertTrue(options.hasShortOption("a"));
        assertFalse(options.hasLongOption("a"));
        assertNotNull(options.getOption("a"));
        assertEquals("description a", options.getOption("a").getDescription());
    }

    // Tests adding an option with short name, hasArg, and description
    @Test
    public void testAddOption_shortOptWithArgFlag_addsOptionWithArg()
    {
        options.addOption("b", true, "option with arg");

        assertTrue(options.hasOption("b"));
        Option opt = options.getOption("b");
        assertNotNull(opt);
        assertTrue(opt.hasArg());
    }

    // Tests adding an option with short name, long name, hasArg, and description
    @Test
    public void testAddOption_shortAndLongOpt_addsBothKeys()
    {
        options.addOption("c", "config", false, "configuration file");

        assertTrue(options.hasOption("c"));
        assertTrue(options.hasOption("config"));
        assertTrue(options.hasOption("--config"));
        assertTrue(options.hasOption("-c"));
        assertTrue(options.hasLongOption("config"));
        assertTrue(options.hasShortOption("c"));
        assertEquals(options.getOption("c"), options.getOption("config"));
    }

    // Tests adding a required Option object and checking required options list
    @Test
    public void testAddOption_requiredOption_updatesRequiredOptionsList()
    {
        Option reqOpt = new Option("r", "required-opt", false, "required option");
        reqOpt.setRequired(true);

        options.addOption(reqOpt);

        List<?> required = options.getRequiredOptions();
        assertEquals(1, required.size());
        assertTrue(required.contains("r"));

        // Add the same option again to verify deduplication in required list
        options.addOption(reqOpt);
        assertEquals(1, options.getRequiredOptions().size());
    }

    // Tests adding an OptionGroup and verifying group association and required state
    @Test
    public void testAddOptionGroup_requiredGroup_updatesRequiredAndMembers()
    {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);

        Option opt1 = new Option("x", "exclusive 1");
        opt1.setRequired(true);
        Option opt2 = new Option("y", "exclusive 2");

        group.addOption(opt1);
        group.addOption(opt2);

        options.addOptionGroup(group);

        assertFalse(opt1.isRequired());
        assertEquals(group, options.getOptionGroup(opt1));
        assertEquals(group, options.getOptionGroup(opt2));
        assertTrue(options.getRequiredOptions().contains(group));
        assertEquals(1, options.getOptionGroups().size());
        assertTrue(options.getOptionGroups().contains(group));
    }

    // Tests getOption with hyphens stripped and non-existent option
    @Test
    public void testGetOption_variousInputs_returnsExpectedOptionOrNull()
    {
        Option opt = new Option("h", "help", false, "print help");
        options.addOption(opt);

        assertEquals(opt, options.getOption("-h"));
        assertEquals(opt, options.getOption("--help"));
        assertEquals(opt, options.getOption("help"));
        assertEquals(opt, options.getOption("h"));
        assertNull(options.getOption("unknown"));
        assertNull(options.getOption("--unknown"));
    }

    // Tests hasOption, hasLongOption, and hasShortOption methods
    @Test
    public void testHasOptionMethods_existingAndNonExisting_returnsCorrectFlags()
    {
        options.addOption("s", "silent", false, "silent mode");

        assertTrue(options.hasOption("s"));
        assertTrue(options.hasOption("silent"));
        assertTrue(options.hasLongOption("silent"));
        assertTrue(options.hasLongOption("--silent"));
        assertFalse(options.hasLongOption("s"));
        assertTrue(options.hasShortOption("s"));
        assertTrue(options.hasShortOption("-s"));
        assertFalse(options.hasShortOption("silent"));

        assertFalse(options.hasOption("nonexistent"));
        assertFalse(options.hasLongOption("nonexistent"));
        assertFalse(options.hasShortOption("nonexistent"));
    }

    // Tests getMatchingOptions with exact match vs partial prefix match
    @Test
    public void testGetMatchingOptions_exactMatch_returnsExactMatchOnly()
    {
        options.addOption(new Option(null, "date", false, "Date option"));
        options.addOption(new Option(null, "date-format", false, "Date format option"));

        List<String> matches = options.getMatchingOptions("date");
        assertNotNull(matches);
        assertEquals(1, matches.size());
        assertEquals("date", matches.get(0));
    }

    // Tests getMatchingOptions with partial prefix matching multiple long options
    @Test
    public void testGetMatchingOptions_partialPrefix_returnsAllMatchingLongOpts()
    {
        options.addOption(new Option(null, "foo", false, "Option foo"));
        options.addOption(new Option(null, "foobar", false, "Option foobar"));
        options.addOption(new Option(null, "baz", false, "Option baz"));

        List<String> matches = options.getMatchingOptions("--fo");
        assertEquals(2, matches.size());
        assertTrue(matches.contains("foo"));
        assertTrue(matches.contains("foobar"));

        List<String> noMatches = options.getMatchingOptions("bar");
        assertTrue(noMatches.isEmpty());
    }

    // Tests getOptions and helpOptions returning unmodifiable collection
    @Test
    public void testGetOptions_returnsAllOptions()
    {
        Option o1 = new Option("1", "one", false, "first");
        Option o2 = new Option("2", "two", false, "second");

        options.addOption(o1);
        options.addOption(o2);

        Collection<Option> allOptions = options.getOptions();
        assertEquals(2, allOptions.size());
        assertTrue(allOptions.contains(o1));
        assertTrue(allOptions.contains(o2));
        assertEquals(2, options.helpOptions().size());
    }

    // Tests getOptionGroup with non-group option returning null
    @Test
    public void testGetOptionGroup_optionNotInGroup_returnsNull()
    {
        Option opt = new Option("o", "other", false, "other option");
        options.addOption(opt);

        assertNull(options.getOptionGroup(opt));
    }

    // Tests toString method contains short and long options
    @Test
    public void testToString_returnsValidStringRepresentation()
    {
        options.addOption("v", "verbose", false, "verbose output");
        String str = options.toString();

        assertNotNull(str);
        assertTrue(str.contains("verbose"));
        assertTrue(str.contains("v"));
    }
}