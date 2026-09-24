package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for OptionGroup.
 */
public class OptionGroupTest
{
    private OptionGroup group;
    private Option optionA;
    private Option optionB;
    private Option optionLongOnly;

    @Before
    public void setUp()
    {
        group = new OptionGroup();
        optionA = new Option("a", "first option");
        optionB = new Option("b", "second option");
        optionLongOnly = new Option(null, "foo", false, "long option only");
    }

    // Tests default state of a new OptionGroup
    @Test
    public void testDefaultState()
    {
        assertNull(group.getSelected());
        assertFalse(group.isRequired());
        assertTrue(group.getNames().isEmpty());
        assertTrue(group.getOptions().isEmpty());
    }

    // Tests adding options and retrieving names and options collection
    @Test
    public void testAddOption_storesAndRetrievesOptionsAndNames()
    {
        group.addOption(optionA);
        group.addOption(optionB);

        Collection names = group.getNames();
        assertEquals(2, names.size());
        assertTrue(names.contains("a"));
        assertTrue(names.contains("b"));

        Collection options = group.getOptions();
        assertEquals(2, options.size());
        assertTrue(options.contains(optionA));
        assertTrue(options.contains(optionB));
    }

    // Tests setSelected with a valid short option
    @Test
    public void testSetSelected_shortOption_setsSelected() throws Exception
    {
        group.setSelected(optionA);
        assertEquals("a", group.getSelected());
    }

    // Tests setSelected with null resets selected to null
    @Test
    public void testSetSelected_null_resetsSelected() throws Exception
    {
        group.setSelected(optionA);
        assertEquals("a", group.getSelected());

        group.setSelected(null);
        assertNull(group.getSelected());
    }

    // Tests setSelected when re-selecting the same option does not throw exception
    @Test
    public void testSetSelected_sameOptionReselected_noException() throws Exception
    {
        group.setSelected(optionA);
        group.setSelected(optionA);
        assertEquals("a", group.getSelected());
    }

    // Tests setSelected throws AlreadySelectedException when selecting another option
    @Test(expected = AlreadySelectedException.class)
    public void testSetSelected_differentOption_throwsAlreadySelectedException() throws Exception
    {
        group.setSelected(optionA);
        group.setSelected(optionB);
    }

    // Tests setSelected with an option having only a long option name (Defects4J defect check)
    @Test
    public void testSetSelected_longOptionOnly_setsSelectedCorrectly() throws Exception
    {
        group.addOption(optionLongOnly);
        group.setSelected(optionLongOnly);
        assertEquals("foo", group.getSelected());
    }

    // Tests setSelected throws AlreadySelectedException after long-only option is selected
    @Test(expected = AlreadySelectedException.class)
    public void testSetSelected_longOptionOnlyThenDifferentOption_throwsException() throws Exception
    {
        group.addOption(optionLongOnly);
        group.addOption(optionA);

        group.setSelected(optionLongOnly);
        group.setSelected(optionA);
    }

    // Tests setRequired and isRequired
    @Test
    public void testSetRequired_updatesRequiredFlag()
    {
        group.setRequired(true);
        assertTrue(group.isRequired());

        group.setRequired(false);
        assertFalse(group.isRequired());
    }

    // Tests toString with empty option group
    @Test
    public void testToString_emptyGroup_returnsBrackets()
    {
        assertEquals("[]", group.toString());
    }

    // Tests toString with short option
    @Test
    public void testToString_withShortOption()
    {
        group.addOption(optionA);
        assertEquals("[-a first option]", group.toString());
    }

    // Tests toString with long-only option
    @Test
    public void testToString_withLongOnlyOption()
    {
        group.addOption(optionLongOnly);
        assertEquals("[--foo long option only]", group.toString());
    }

    // Tests toString with multiple options contains all representations
    @Test
    public void testToString_multipleOptions()
    {
        group.addOption(optionA);
        group.addOption(optionLongOnly);

        String result = group.toString();
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
        assertTrue(result.contains("-a first option"));
        assertTrue(result.contains("--foo long option only"));
        assertTrue(result.contains(", "));
    }
}