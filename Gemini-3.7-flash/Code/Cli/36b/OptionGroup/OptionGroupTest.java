package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OptionGroupTest
{
    private OptionGroup group;
    private Option optionA;
    private Option optionB;

    @Before
    public void setUp()
    {
        group = new OptionGroup();
        optionA = new Option("a", "alpha", false, "option A description");
        optionB = new Option("b", "beta", false, "option B description");
    }

    // Tests adding an option and retrieving names and options collections
    @Test
    public void testAddOption_validOption_addsToGroup()
    {
        group.addOption(optionA);

        Collection<String> names = group.getNames();
        Collection<Option> options = group.getOptions();

        assertEquals(1, names.size());
        assertTrue(names.contains("a"));
        assertEquals(1, options.size());
        assertTrue(options.contains(optionA));
    }

    // Tests initial state of default OptionGroup
    @Test
    public void testInitialState_defaultValues_returnsDefaults()
    {
        assertNull(group.getSelected());
        assertFalse(group.isRequired());
        assertTrue(group.getNames().isEmpty());
        assertTrue(group.getOptions().isEmpty());
    }

    // Tests selecting an option when no option is currently selected
    @Test
    public void testSetSelected_initialSelection_setsSelectedSuccessfully() throws AlreadySelectedException
    {
        group.addOption(optionA);
        group.setSelected(optionA);

        assertEquals("a", group.getSelected());
    }

    // Tests selecting an option by long option key
    @Test
    public void testSetSelected_longOptOnlyOption_setsSelectedSuccessfully() throws AlreadySelectedException
    {
        Option longOnly = new Option(null, "longOpt", false, "long only desc");
        group.addOption(longOnly);
        group.setSelected(longOnly);

        assertEquals("longOpt", group.getSelected());
    }

    // Tests reselecting the same option does not throw exception
    @Test
    public void testSetSelected_sameOptionReselected_noExceptionThrown() throws AlreadySelectedException
    {
        group.addOption(optionA);
        group.setSelected(optionA);
        group.setSelected(optionA);

        assertEquals("a", group.getSelected());
    }

    // Tests selecting a different option when one is already selected throws AlreadySelectedException
    @Test(expected = AlreadySelectedException.class)
    public void testSetSelected_differentOptionSelected_throwsAlreadySelectedException() throws AlreadySelectedException
    {
        group.addOption(optionA);
        group.addOption(optionB);

        group.setSelected(optionA);
        group.setSelected(optionB);
    }

    // Tests passing null resets the selected option
    @Test
    public void testSetSelected_nullInput_resetsSelected() throws AlreadySelectedException
    {
        group.addOption(optionA);
        group.setSelected(optionA);
        assertEquals("a", group.getSelected());

        group.setSelected(null);
        assertNull(group.getSelected());
    }

    // Tests selecting a new option after resetting to null
    @Test
    public void testSetSelected_resetThenSelectAnother_setsNewSelected() throws AlreadySelectedException
    {
        group.addOption(optionA);
        group.addOption(optionB);

        group.setSelected(optionA);
        assertEquals("a", group.getSelected());

        group.setSelected(null);
        assertNull(group.getSelected());

        group.setSelected(optionB);
        assertEquals("b", group.getSelected());
    }

    // Tests setting and checking the required property
    @Test
    public void testSetRequired_trueAndFalse_updatesRequiredFlag()
    {
        group.setRequired(true);
        assertTrue(group.isRequired());

        group.setRequired(false);
        assertFalse(group.isRequired());
    }

    // Tests toString on an empty OptionGroup
    @Test
    public void testToString_emptyGroup_returnsEmptyBrackets()
    {
        assertEquals("[]", group.toString());
    }

    // Tests toString with short option having description
    @Test
    public void testToString_singleOptionWithDescription_formatsCorrectly()
    {
        Option opt = new Option("f", "file description");
        group.addOption(opt);

        assertEquals("[-f file description]", group.toString());
    }

    // Tests toString with short option having null description
    @Test
    public void testToString_singleOptionWithoutDescription_formatsCorrectly()
    {
        Option opt = new Option("f", null);
        group.addOption(opt);

        assertEquals("[-f]", group.toString());
    }

    // Tests toString with long-only option
    @Test
    public void testToString_longOptOnlyOption_formatsWithDoubleDash()
    {
        Option opt = new Option(null, "file", false, "file description");
        group.addOption(opt);

        assertEquals("[--file file description]", group.toString());
    }

    // Tests toString with multiple options contains all representations
    @Test
    public void testToString_multipleOptions_containsAllOptions()
    {
        Option opt1 = new Option("a", "first");
        Option opt2 = new Option("b", "second");
        group.addOption(opt1);
        group.addOption(opt2);

        String result = group.toString();
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
        assertTrue(result.contains("-a first"));
        assertTrue(result.contains("-b second"));
        assertTrue(result.contains(", "));
    }

    // Tests toString with long-only option without description
    @Test
    public void testToString_longOptOnlyWithoutDescription_formatsCorrectly()
    {
        Option opt = new Option(null, "file", false, null);
        group.addOption(opt);

        assertEquals("[--file]", group.toString());
    }

    // Tests addOption returns the OptionGroup instance for method chaining
    @Test
    public void testAddOption_returnsSelfForChaining()
    {
        OptionGroup returned = group.addOption(optionA);
        assertSame(group, returned);
    }

    // Tests reselecting a different Option instance with the same key
    @Test
    public void testSetSelected_differentOptionInstanceWithSameKey_noExceptionThrown() throws AlreadySelectedException
    {
        Option opt1 = new Option("a", "first instance");
        Option opt2 = new Option("a", "second instance");
        group.addOption(opt1);
        group.setSelected(opt1);
        group.setSelected(opt2);

        assertEquals("a", group.getSelected());
    }

    // Tests reselecting a different long-only Option instance with the same long opt key
    @Test
    public void testSetSelected_differentLongOptionInstanceWithSameKey_noExceptionThrown() throws AlreadySelectedException
    {
        Option opt1 = new Option(null, "foo", false, "desc1");
        Option opt2 = new Option(null, "foo", false, "desc2");
        group.addOption(opt1);
        group.setSelected(opt1);
        group.setSelected(opt2);

        assertEquals("foo", group.getSelected());
    }

    // Tests AlreadySelectedException contains references to group and option
    @Test
    public void testSetSelected_conflict_exceptionContainsGroupAndOption()
    {
        group.addOption(optionA);
        group.addOption(optionB);

        try
        {
            group.setSelected(optionA);
            group.setSelected(optionB);
            fail("Expected AlreadySelectedException");
        }
        catch (AlreadySelectedException e)
        {
            assertSame(group, e.getOptionGroup());
            assertSame(optionB, e.getOption());
        }
    }
}