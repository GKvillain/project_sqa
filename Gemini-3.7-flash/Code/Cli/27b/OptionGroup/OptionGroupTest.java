package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OptionGroupTest
{
    private OptionGroup group;
    private Option optA;
    private Option optB;
    private Option optLongOnly;

    @Before
    public void setUp()
    {
        group = new OptionGroup();
        optA = new Option("a", "option A description");
        optB = new Option("b", "option B description");
        optLongOnly = new Option(null, "long-only", false, "long only description");
    }

    // Tests adding options and verifying names and collection content
    @Test
    public void testAddOption_validOptions_containsAllOptionsAndNames()
    {
        group.addOption(optA);
        group.addOption(optB);

        Collection names = group.getNames();
        Collection options = group.getOptions();

        assertEquals(2, names.size());
        assertTrue(names.contains("a"));
        assertTrue(names.contains("b"));

        assertEquals(2, options.size());
        assertTrue(options.contains(optA));
        assertTrue(options.contains(optB));
    }

    // Tests required flag default value and setter/getter
    @Test
    public void testSetRequired_booleanValues_updatesRequiredState()
    {
        assertFalse(group.isRequired());

        group.setRequired(true);
        assertTrue(group.isRequired());

        group.setRequired(false);
        assertFalse(group.isRequired());
    }

    // Tests setting selected option successfully
    @Test
    public void testSetSelected_validOption_selectedIsUpdated() throws AlreadySelectedException
    {
        group.setSelected(optA);
        assertEquals("a", group.getSelected());
    }

    // Tests re-selecting the same option does not throw exception
    @Test
    public void testSetSelected_sameOptionReselected_noExceptionThrown() throws AlreadySelectedException
    {
        group.setSelected(optA);
        group.setSelected(optA);
        assertEquals("a", group.getSelected());
    }

    // Tests selecting a different option when one is already selected throws AlreadySelectedException
    @Test(expected = AlreadySelectedException.class)
    public void testSetSelected_differentOption_throwsAlreadySelectedException() throws AlreadySelectedException
    {
        group.setSelected(optA);
        group.setSelected(optB);
    }

    // Tests AlreadySelectedException contains correct OptionGroup and Option references
    @Test
    public void testSetSelected_differentOption_exceptionContainsCorrectDetails()
    {
        try
        {
            group.setSelected(optA);
            group.setSelected(optB);
            fail("Expected AlreadySelectedException was not thrown");
        }
        catch (AlreadySelectedException ex)
        {
            assertEquals(group, ex.getOptionGroup());
            assertEquals(optB, ex.getOption());
        }
    }

    // Tests setting selected to null resets the selected option
    @Test
    public void testSetSelected_nullOption_resetsSelected() throws AlreadySelectedException
    {
        group.setSelected(optA);
        assertEquals("a", group.getSelected());

        group.setSelected(null);
        assertNull(group.getSelected());

        // After reset, selecting another option should succeed
        group.setSelected(optB);
        assertEquals("b", group.getSelected());
    }

    // Tests selecting an option that only has a long option name (Defects4J Cli-27)
    @Test
    public void testSetSelected_longOptionOnly_selectedIsSetToKey() throws AlreadySelectedException
    {
        group.setSelected(optLongOnly);
        assertEquals("long-only", group.getSelected());
    }

    // Tests re-selecting the same long-only option does not throw exception
    @Test
    public void testSetSelected_sameLongOptionOnlyReselected_noExceptionThrown() throws AlreadySelectedException
    {
        group.setSelected(optLongOnly);
        group.setSelected(optLongOnly);
        assertEquals("long-only", group.getSelected());
    }

    // Tests toString with empty OptionGroup
    @Test
    public void testToString_emptyGroup_returnsEmptyBrackets()
    {
        assertEquals("[]", group.toString());
    }

    // Tests toString with short options
    @Test
    public void testToString_shortOption_returnsFormattedString()
    {
        group.addOption(optA);
        assertEquals("[-a option A description]", group.toString());
    }

    // Tests toString with long-only option
    @Test
    public void testToString_longOptionOnly_returnsFormattedString()
    {
        group.addOption(optLongOnly);
        assertEquals("[--long-only long only description]", group.toString());
    }

    // Tests toString with multiple options contains all representations
    @Test
    public void testToString_multipleOptions_containsAllOptionRepresentations()
    {
        group.addOption(optA);
        group.addOption(optLongOnly);

        String result = group.toString();
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
        assertTrue(result.contains("-a option A description"));
        assertTrue(result.contains("--long-only long only description"));
        assertTrue(result.contains(", "));
    }

    // Tests setSelected with null when initially null does not change state
    @Test
    public void testSetSelected_nullWhenInitiallyNull_remainsNull() throws AlreadySelectedException
    {
        assertNull(group.getSelected());
        group.setSelected(null);
        assertNull(group.getSelected());
    }

    // Tests toString with short option that has no description
    @Test
    public void testToString_shortOptionNoDescription_returnsFormattedStringWithoutDescription()
    {
        Option optNoDesc = new Option("c", null);
        group.addOption(optNoDesc);
        assertEquals("[-c]", group.toString());
    }

    // Tests toString with long-only option that has no description
    @Test
    public void testToString_longOptionOnlyNoDescription_returnsFormattedStringWithoutDescription()
    {
        Option optLongNoDesc = new Option(null, "flag", false, null);
        group.addOption(optLongNoDesc);
        assertEquals("[--flag]", group.toString());
    }

    // Tests toString with an option having both short and long option names
    @Test
    public void testToString_shortAndLongOption_usesShortOptionName()
    {
        Option optBoth = new Option("d", "dual", false, "dual description");
        group.addOption(optBoth);
        assertEquals("[-d dual description]", group.toString());
    }
}