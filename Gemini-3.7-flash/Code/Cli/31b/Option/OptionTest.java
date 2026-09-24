package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class OptionTest
{
    // Tests basic constructor initialization and getters
    @Test
    public void testConstructor_shortAndDescription_initializesProperly()
    {
        Option option = new Option("a", "description-a");
        assertEquals("a", option.getOpt());
        assertEquals("description-a", option.getDescription());
        assertNull(option.getLongOpt());
        assertFalse(option.hasArg());
        assertFalse(option.hasArgs());
        assertFalse(option.hasLongOpt());
        assertEquals(Option.UNINITIALIZED, option.getArgs());
    }

    // Tests constructor with hasArg flag
    @Test
    public void testConstructor_withHasArg_setsNumberOfArgsToOne()
    {
        Option option = new Option("b", true, "description-b");
        assertEquals("b", option.getOpt());
        assertTrue(option.hasArg());
        assertEquals(1, option.getArgs());
    }

    // Tests constructor with full parameters including longOpt
    @Test
    public void testConstructor_fullParameters_setsAllFields()
    {
        Option option = new Option("c", "long-c", true, "description-c");
        assertEquals("c", option.getOpt());
        assertEquals("long-c", option.getLongOpt());
        assertTrue(option.hasLongOpt());
        assertTrue(option.hasArg());
        assertEquals("description-c", option.getDescription());
    }

    // Tests getKey and getId when short opt is null vs present
    @Test
    public void testGetKeyAndId_optAndLongOpt_returnsKeyAndFirstChar()
    {
        Option option = new Option("o", "opt-long", false, "desc");
        assertEquals("o", option.getKey());
        assertEquals((int) 'o', option.getId());

        Option longOnly = new Option(null, "long-only", false, "desc");
        assertEquals("long-only", longOnly.getKey());
    }

    // Tests type, required, and description mutators and accessors
    @Test
    public void testSettersAndGetters_typeRequiredDescription_returnsUpdatedValues()
    {
        Option option = new Option("d", "desc");
        option.setType(String.class);
        assertEquals(String.class, option.getType());

        option.setRequired(true);
        assertTrue(option.isRequired());

        option.setDescription("new-desc");
        assertEquals("new-desc", option.getDescription());

        option.setLongOpt("long-d");
        assertEquals("long-d", option.getLongOpt());
        assertTrue(option.hasLongOpt());
    }

    // Tests argName methods with default, non-empty, empty and null values
    @Test
    public void testHasArgName_variousValues_returnsExpectedFlag()
    {
        Option option = new Option("a", "desc");
        assertEquals("arg", option.getArgName());
        assertTrue(option.hasArgName());

        option.setArgName("");
        assertFalse(option.hasArgName());

        option.setArgName(null);
        assertFalse(option.hasArgName());

        option.setArgName("custom");
        assertTrue(option.hasArgName());
        assertEquals("custom", option.getArgName());
    }

    // Tests hasArg and hasArgs under different numberOfArgs settings
    @Test
    public void testHasArgAndHasArgs_unlimitedAndMultiple_returnsCorrectFlags()
    {
        Option option = new Option("m", "desc");
        option.setArgs(Option.UNLIMITED_VALUES);
        assertTrue(option.hasArg());
        assertTrue(option.hasArgs());

        option.setArgs(2);
        assertTrue(option.hasArg());
        assertTrue(option.hasArgs());

        option.setArgs(1);
        assertTrue(option.hasArg());
        assertFalse(option.hasArgs());

        option.setArgs(0);
        assertFalse(option.hasArg());
        assertFalse(option.hasArgs());
    }

    // Tests optional argument flag
    @Test
    public void testOptionalArg_setterAndGetter_returnsCorrectState()
    {
        Option option = new Option("opt", "desc");
        assertFalse(option.hasOptionalArg());

        option.setOptionalArg(true);
        assertTrue(option.hasOptionalArg());
    }

    // Tests value separator settings and processing
    @Test
    public void testValueSeparator_setAndGet_handlesSeparatorFlag()
    {
        Option option = new Option("s", true, "desc");
        assertFalse(option.hasValueSeparator());
        assertEquals((char) 0, option.getValueSeparator());

        option.setValueSeparator('=');
        assertTrue(option.hasValueSeparator());
        assertEquals('=', option.getValueSeparator());
    }

    // Tests exception when adding value to uninitialized option
    @Test(expected = RuntimeException.class)
    public void testAddValueForProcessing_uninitializedOption_throwsException()
    {
        Option option = new Option("u", "desc");
        option.addValueForProcessing("value");
    }

    // Tests adding value when capacity is full
    @Test(expected = RuntimeException.class)
    public void testAddValueForProcessing_exceedsCapacity_throwsException()
    {
        Option option = new Option("f", true, "desc");
        option.addValueForProcessing("first");
        option.addValueForProcessing("second");
    }

    // Tests value parsing with value separator
    @Test
    public void testAddValueForProcessing_withValueSeparator_splitsValuesCorrectly()
    {
        Option option = new Option("p", "property", true, "properties");
        option.setArgs(3);
        option.setValueSeparator('=');
        option.addValueForProcessing("key1=val1=extra");

        assertEquals(3, option.getValuesList().size());
        assertEquals("key1", option.getValue(0));
        assertEquals("val1", option.getValue(1));
        assertEquals("extra", option.getValue(2));
    }

    // Tests getValue, getValue with index, default value, and getValues
    @Test
    public void testGetValues_variousScenarios_returnsExpectedValues()
    {
        Option option = new Option("v", true, "desc");
        assertNull(option.getValue());
        assertNull(option.getValue(0));
        assertEquals("default", option.getValue("default"));
        assertNull(option.getValues());

        option.addValueForProcessing("v1");
        assertEquals("v1", option.getValue());
        assertEquals("v1", option.getValue(0));
        assertEquals("v1", option.getValue("default"));

        String[] values = option.getValues();
        assertNotNull(values);
        assertEquals(1, values.length);
        assertEquals("v1", values[0]);

        List list = option.getValuesList();
        assertNotNull(list);
        assertEquals(1, list.size());
    }

    // Tests clearValues method
    @Test
    public void testClearValues_existingValues_clearsList()
    {
        Option option = new Option("c", true, "desc");
        option.addValueForProcessing("val");
        assertEquals(1, option.getValuesList().size());

        option.clearValues();
        assertNull(option.getValue());
        assertEquals(0, option.getValuesList().size());
    }

    // Tests acceptsArg condition branches
    @Test
    public void testAcceptsArg_variousConditions_returnsCorrectBoolean()
    {
        Option option = new Option("a", "desc");
        assertFalse(option.acceptsArg());

        option.setArgs(1);
        assertTrue(option.acceptsArg());

        option.addValueForProcessing("val");
        assertFalse(option.acceptsArg());

        option.setOptionalArg(true);
        assertFalse(option.acceptsArg());
    }

    // Tests requiresArg condition branches
    @Test
    public void testRequiresArg_optionalAndUnlimited_returnsExpected()
    {
        Option option = new Option("r", true, "desc");
        assertTrue(option.requiresArg());

        option.setOptionalArg(true);
        assertFalse(option.requiresArg());

        option.setOptionalArg(false);
        option.setArgs(Option.UNLIMITED_VALUES);
        assertTrue(option.requiresArg());

        option.addValueForProcessing("item");
        assertFalse(option.requiresArg());
    }

    // Tests equals and hashCode contract
    @Test
    public void testEqualsAndHashCode_sameAndDifferentObjects_behavesCorrectly()
    {
        Option opt1 = new Option("e", "long-e", false, "desc");
        Option opt2 = new Option("e", "long-e", false, "desc");
        Option opt3 = new Option("diff", "long-e", false, "desc");
        Option opt4 = new Option("e", "diff-long", false, "desc");

        assertTrue(opt1.equals(opt1));
        assertTrue(opt1.equals(opt2));
        assertEquals(opt1.hashCode(), opt2.hashCode());

        assertFalse(opt1.equals(null));
        assertFalse(opt1.equals("some-string"));
        assertFalse(opt1.equals(opt3));
        assertFalse(opt1.equals(opt4));

        Option nullOpt1 = new Option(null, "long-same", false, "desc");
        Option nullOpt2 = new Option(null, "long-same", false, "desc");
        assertTrue(nullOpt1.equals(nullOpt2));
        assertEquals(nullOpt1.hashCode(), nullOpt2.hashCode());
        assertFalse(nullOpt1.equals(opt1));
    }

    // Tests clone creates independent values list
    @Test
    public void testClone_independentCopy_doesNotMutateOriginal()
    {
        Option option = new Option("c", true, "desc");
        option.addValueForProcessing("initial");

        Option cloned = (Option) option.clone();
        assertNotSame(option, cloned);
        assertEquals(option, cloned);
        assertEquals(option.getValue(), cloned.getValue());

        cloned.clearValues();
        assertEquals(0, cloned.getValuesList().size());
        assertEquals(1, option.getValuesList().size());
    }

    // Tests deprecated addValue throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testAddValue_callingDeprecatedMethod_throwsException()
    {
        Option option = new Option("t", "desc");
        option.addValue("value");
    }

    // Tests toString format with different configurations
    @Test
    public void testToString_variousOptions_returnsExpectedString()
    {
        Option option = new Option("a", "long-a", true, "desc");
        option.setType(String.class);
        String str = option.toString();
        assertTrue(str.startsWith("[ option: a long-a"));
        assertTrue(str.contains("[ARG]"));
        assertTrue(str.contains("desc"));
        assertTrue(str.contains(String.class.toString()));

        Option multiArgOption = new Option("m", "desc");
        multiArgOption.setArgs(2);
        assertTrue(multiArgOption.toString().contains("[ARG...]"));
    }
}