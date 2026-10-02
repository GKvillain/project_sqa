package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OptionGroupTest
{
    private OptionGroup group;

    @Before
    public void setUp()
    {
        group = new OptionGroup();
    }

    // Tests default initial state of OptionGroup
    @Test
    public void testInitialState_defaultConstructor_returnsEmptyAndNotRequired()
    {
        assertTrue(group.getNames().isEmpty());
        assertTrue(group.getOptions().isEmpty());
        assertNull(group.getSelected());
        assertFalse(group.isRequired());
    }

    // Tests adding an option and retrieving names and options
    @Test
    public void testAddOption_validOption_storesOptionCorrectly()
    {
        Option opt = new Option("a", "first option");
        OptionGroup returnedGroup = group.addOption(opt);

        assertEquals(group, returnedGroup);
        Collection<String> names = group.getNames();
        Collection<Option> options = group.getOptions();

        assertEquals(1, names.size());
        assertTrue(names.contains("a"));
        assertEquals(1, options.size());
        assertTrue(options.contains(opt));
    }

    // Tests setting and getting required flag
    @Test
    public void testSetRequired_booleanValues_updatesRequiredField()
    {
        group.setRequired(true);
        assertTrue(group.isRequired());

        group.setRequired(false);
        assertFalse(group.isRequired());
    }

    // Tests selecting an option when none was selected
    @Test
    public void testSetSelected_noPriorSelection_setsSelectedOption() throws AlreadySelectedException
    {
        Option optA = new Option("a", "option A");
        group.setSelected(optA);

        assertEquals("a", group.getSelected());
    }

    // Tests re-selecting the exact same option
    @Test
    public void testSetSelected_sameOptionReselected_succeedsWithoutException() throws AlreadySelectedException
    {
        Option optA = new Option("a", "option A");
        group.setSelected(optA);
        group.setSelected(optA);

        assertEquals("a", group.getSelected());
    }

    // Tests resetting selection by passing null
    @Test
    public void testSetSelected_nullInput_resetsSelectedToNull() throws AlreadySelectedException
    {
        Option optA = new Option("a", "option A");
        group.setSelected(optA);
        assertEquals("a", group.getSelected());

        group.setSelected(null);
        assertNull(group.getSelected());
    }

    // Tests selecting a different option throws AlreadySelectedException
    @Test(expected = AlreadySelectedException.class)
    public void testSetSelected_differentOption_throwsAlreadySelectedException() throws AlreadySelectedException
    {
        Option optA = new Option("a", "option A");
        Option optB = new Option("b", "option B");

        group.setSelected(optA);
        group.setSelected(optB);
    }

    // Tests setting selected option that has only a longOpt
    @Test
    public void testSetSelected_longOptOnlyOption_setsSelectedKey() throws AlreadySelectedException
    {
        Option optLong = new Option(null, "foo", false, "long option");
        group.setSelected(optLong);

        assertEquals("foo", group.getSelected());
    }

    // Tests toString with no options
    @Test
    public void testToString_emptyGroup_returnsEmptyBrackets()
    {
        assertEquals("[]", group.toString());
    }

    // Tests toString with a single short option and description
    @Test
    public void testToString_singleShortOptionWithDescription_returnsFormattedString()
    {
        Option optA = new Option("a", "first option");
        group.addOption(optA);

        assertEquals("[-a first option]", group.toString());
    }

    // Tests toString with a single short option without description
    @Test
    public void testToString_singleShortOptionNoDescription_returnsFormattedString()
    {
        Option optA = new Option("a", null);
        group.addOption(optA);

        assertEquals("[-a]", group.toString());
    }

    // Tests toString with a long option only and description
    @Test
    public void testToString_longOptionOnlyWithDescription_returnsFormattedString()
    {
        Option optFoo = new Option(null, "foo", false, "foo description");
        group.addOption(optFoo);

        assertEquals("[--foo foo description]", group.toString());
    }

    // Tests toString with a long option only without description
    @Test
    public void testToString_longOptionOnlyNoDescription_returnsFormattedString()
    {
        Option optFoo = new Option(null, "foo", false, null);
        group.addOption(optFoo);

        assertEquals("[--foo]", group.toString());
    }

    // Tests toString with multiple options separated by comma
    @Test
    public void testToString_multipleOptions_containsBothOptionsSeparatedByComma()
    {
        Option optA = new Option("a", "desc A");
        Option optB = new Option("b", "desc B");
        group.addOption(optA);
        group.addOption(optB);

        String result = group.toString();
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
        assertTrue(result.contains("-a desc A"));
        assertTrue(result.contains("-b desc B"));
        assertTrue(result.contains(", "));
    }

    // Tests adding an option with longOpt only and retrieving names and options
    @Test
    public void testAddOption_longOptOnlyOption_storesOptionUsingLongOptKey()
    {
        Option optLong = new Option(null, "bar", false, "bar description");
        group.addOption(optLong);

        Collection<String> names = group.getNames();
        Collection<Option> options = group.getOptions();

        assertEquals(1, names.size());
        assertTrue(names.contains("bar"));
        assertEquals(1, options.size());
        assertTrue(options.contains(optLong));
    }

    // Tests re-selecting a distinct Option instance that has the same opt key
    @Test
    public void testSetSelected_differentOptionInstanceSameKey_succeedsWithoutException() throws AlreadySelectedException
    {
        Option opt1 = new Option("a", "first instance");
        Option opt2 = new Option("a", "second instance");

        group.setSelected(opt1);
        group.setSelected(opt2);

        assertEquals("a", group.getSelected());
    }

    // Tests re-selecting a distinct Option instance that has the same longOpt key
    @Test
    public void testSetSelected_differentOptionInstanceSameLongOptKey_succeedsWithoutException() throws AlreadySelectedException
    {
        Option opt1 = new Option(null, "foo", false, "first instance");
        Option opt2 = new Option(null, "foo", false, "second instance");

        group.setSelected(opt1);
        group.setSelected(opt2);

        assertEquals("foo", group.getSelected());
    }

    // Tests AlreadySelectedException contains references to the OptionGroup and conflicting Option
    @Test
    public void testSetSelected_alreadySelectedException_containsCorrectGroupAndOption()
    {
        Option optA = new Option("a", "option A");
        Option optB = new Option("b", "option B");

        try
        {
            group.setSelected(optA);
            group.setSelected(optB);
            fail("Expected AlreadySelectedException to be thrown");
        }
        catch (AlreadySelectedException e)
        {
            assertEquals(group, e.getOptionGroup());
            assertEquals(optB, e.getOption());
        }
    }
}