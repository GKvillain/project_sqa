package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class OptionTest
{
    // Tests two-argument constructor and default values
    @Test
    public void testConstructor_twoArgs_setsDefaultProperties()
    {
        Option option = new Option("a", "description of a");
        assertEquals("a", option.getOpt());
        assertEquals("description of a", option.getDescription());
        assertNull(option.getLongOpt());
        assertFalse(option.hasArg());
        assertFalse(option.hasArgs());
        assertFalse(option.hasLongOpt());
        assertEquals(Option.UNINITIALIZED, option.getArgs());
    }

    // Tests three-argument constructor with hasArg set to true
    @Test
    public void testConstructor_threeArgs_setsHasArgCorrectly()
    {
        Option option = new Option("b", true, "option with arg");
        assertEquals("b", option.getOpt());
        assertTrue(option.hasArg());
        assertEquals(1, option.getArgs());
    }

    // Tests four-argument constructor with long option
    @Test
    public void testConstructor_fourArgs_setsAllFieldsCorrectly()
    {
        Option option = new Option("c", "config", true, "configuration file");
        assertEquals("c", option.getOpt());
        assertEquals("config", option.getLongOpt());
        assertTrue(option.hasLongOpt());
        assertTrue(option.hasArg());
        assertEquals("configuration file", option.getDescription());
    }

    // Tests invalid option character throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_invalidOpt_throwsIllegalArgumentException()
    {
        new Option("?", "illegal char option");
    }

    // Tests getId for single character option
    @Test
    public void testGetId_singleCharOpt_returnsAsciiValue()
    {
        Option option = new Option("x", "test id");
        assertEquals('x', option.getId());
    }

    // Tests getKey returning opt or longOpt when opt is null
    @Test
    public void testGetKey_optAndLongOpt_returnsExpectedKey()
    {
        Option option = new Option("o", "longO", false, "desc");
        assertEquals("o", option.getKey());

        Option longOnly = new Option(null, "onlyLong", false, "desc");
        assertEquals("onlyLong", longOnly.getKey());
    }

    // Tests setting and getting type
    @Test
    public void testType_setAndGet_returnsAssignedType()
    {
        Option option = new Option("f", "file option");
        assertNull(option.getType());
        option.setType(String.class);
        assertEquals(String.class, option.getType());
    }

    // Tests optional argument flag
    @Test
    public void testOptionalArg_setAndGet_returnsExpectedState()
    {
        Option option = new Option("opt", "optional arg opt");
        assertFalse(option.hasOptionalArg());
        option.setOptionalArg(true);
        assertTrue(option.hasOptionalArg());
    }

    // Tests argName getters and setters
    @Test
    public void testArgName_setAndGet_validatesPresence()
    {
        Option option = new Option("p", true, "port option");
        assertFalse(option.hasArgName());
        option.setArgName("PORT");
        assertEquals("PORT", option.getArgName());
        assertTrue(option.hasArgName());
        option.setArgName("");
        assertFalse(option.hasArgName());
    }

    // Tests required flag
    @Test
    public void testRequired_setAndGet_returnsExpectedState()
    {
        Option option = new Option("r", "required option");
        assertFalse(option.isRequired());
        option.setRequired(true);
        assertTrue(option.isRequired());
    }

    // Tests setting unlimited values and hasArgs behavior
    @Test
    public void testHasArgs_variousArgCounts_evaluatesCorrectly()
    {
        Option option = new Option("m", "multiple args");
        option.setArgs(Option.UNLIMITED_VALUES);
        assertTrue(option.hasArgs());
        assertTrue(option.hasArg());

        option.setArgs(2);
        assertTrue(option.hasArgs());

        option.setArgs(1);
        assertFalse(option.hasArgs());
        assertTrue(option.hasArg());
    }

    // Tests adding value when no args are allowed
    @Test(expected = RuntimeException.class)
    public void testAddValueForProcessing_uninitializedArgs_throwsRuntimeException()
    {
        Option option = new Option("n", "no args");
        option.addValueForProcessing("val");
    }

    // Tests adding value exceeding maximum allowed arguments
    @Test(expected = RuntimeException.class)
    public void testAddValueForProcessing_exceedsCapacity_throwsRuntimeException()
    {
        Option option = new Option("s", true, "single arg");
        option.addValueForProcessing("first");
        option.addValueForProcessing("second");
    }

    // Tests value separators and splitting of tokens
    @Test
    public void testProcessValue_withValueSeparator_splitsValues()
    {
        Option option = new Option("D", "property option");
        option.setArgs(2);
        option.setValueSeparator('=');
        assertTrue(option.hasValueSeparator());
        assertEquals('=', option.getValueSeparator());

        option.addValueForProcessing("key=value");
        assertEquals("key", option.getValue(0));
        assertEquals("value", option.getValue(1));
    }

    // Tests getValue, getValues, getValuesList and default values
    @Test
    public void testGetValues_noValuesAndPopulated_returnsCorrectValues()
    {
        Option option = new Option("v", true, "values option");
        assertNull(option.getValue());
        assertNull(option.getValues());
        assertEquals("default", option.getValue("default"));

        option.addValueForProcessing("val1");
        assertEquals("val1", option.getValue());
        assertEquals("val1", option.getValue(0));
        assertEquals("val1", option.getValue("default"));

        String[] values = option.getValues();
        assertNotNull(values);
        assertEquals(1, values.length);
        assertEquals("val1", values[0]);

        List list = option.getValuesList();
        assertNotNull(list);
        assertEquals(1, list.size());
    }

    // Tests acceptsArg and requiresArg logic across configurations
    @Test
    public void testRequiresArg_variousConditions_evaluatesProperly()
    {
        Option option = new Option("a", true, "desc");
        assertTrue(option.requiresArg());
        assertTrue(option.acceptsArg());

        option.addValueForProcessing("val");
        assertFalse(option.requiresArg());
        assertFalse(option.acceptsArg());

        Option optionalOpt = new Option("b", true, "desc");
        optionalOpt.setOptionalArg(true);
        assertFalse(optionalOpt.requiresArg());
        assertTrue(optionalOpt.acceptsArg());

        Option unlimitedOpt = new Option("c", "desc");
        unlimitedOpt.setArgs(Option.UNLIMITED_VALUES);
        assertTrue(unlimitedOpt.requiresArg());
        unlimitedOpt.addValueForProcessing("v1");
        assertFalse(unlimitedOpt.requiresArg());
        assertTrue(unlimitedOpt.acceptsArg());
    }

    // Tests equals and hashCode methods
    @Test
    public void testEqualsAndHashCode_sameAndDifferentOptions_behavesCorrectly()
    {
        Option opt1 = new Option("a", "longA", false, "desc1");
        Option opt2 = new Option("a", "longA", false, "desc2");
        Option opt3 = new Option("b", "longB", false, "desc1");

        assertTrue(opt1.equals(opt1));
        assertTrue(opt1.equals(opt2));
        assertEquals(opt1.hashCode(), opt2.hashCode());

        assertFalse(opt1.equals(null));
        assertFalse(opt1.equals("different class"));
        assertFalse(opt1.equals(opt3));
    }

    // Tests clone method and independent values list
    @Test
    public void testClone_clonesObject_createsIndependentValues()
    {
        Option original = new Option("c", true, "clone test");
        original.addValueForProcessing("origVal");

        Option cloned = (Option) original.clone();
        assertEquals(original, cloned);
        assertEquals(original.getValue(), cloned.getValue());

        cloned.clearValues();
        assertNull(cloned.getValue());
        assertEquals("origVal", original.getValue());
    }

    // Tests deprecated addValue method throwing UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testAddValue_callingDeprecated_throwsUnsupportedOperationException()
    {
        Option option = new Option("d", "deprecated addValue");
        option.addValue("test");
    }

    // Tests toString method formatting
    @Test
    public void testToString_variousConfigurations_formatsExpectedString()
    {
        Option option = new Option("t", "test", true, "test option");
        option.setType(Integer.class);
        String str = option.toString();
        assertTrue(str.contains("[ option: t test"));
        assertTrue(str.contains("[ARG]"));
        assertTrue(str.contains("test option"));
        assertTrue(str.contains("class java.lang.Integer"));
    }

    // Tests getId when opt is null and longOpt is present
    @Test
    public void testGetId_nullOptWithLongOpt_returnsFirstCharOfLongOpt()
    {
        Option option = new Option(null, "longOption", false, "desc");
        assertEquals('l', option.getId());
    }

    // Tests setDescription and setLongOpt mutations
    @Test
    public void testSetDescriptionAndSetLongOpt_modifiesFieldsCorrectly()
    {
        Option option = new Option("k", "initial desc");
        option.setDescription("updated desc");
        assertEquals("updated desc", option.getDescription());

        assertNull(option.getLongOpt());
        option.setLongOpt("updatedLong");
        assertEquals("updatedLong", option.getLongOpt());
        assertTrue(option.hasLongOpt());
    }

    // Tests getValue with invalid and out of bounds indices
    @Test
    public void testGetValue_outOfBoundsIndex_returnsNull()
    {
        Option option = new Option("g", true, "desc");
        assertNull(option.getValue(0));
        assertNull(option.getValue(-1));
        assertNull(option.getValue(5));

        option.addValueForProcessing("val");
        assertNull(option.getValue(-1));
        assertNull(option.getValue(1));
        assertNull(option.getValue(10));
    }

    // Tests argName set to null
    @Test
    public void testSetArgName_null_clearsArgName()
    {
        Option option = new Option("a", true, "desc");
        option.setArgName("ARG");
        assertTrue(option.hasArgName());
        option.setArgName(null);
        assertNull(option.getArgName());
        assertFalse(option.hasArgName());
    }

    // Tests equals and hashCode with null opt and longOpt permutations
    @Test
    public void testEqualsAndHashCode_nullOptAndLongOptBranches()
    {
        Option optNull1 = new Option(null, "longOnly", false, "desc");
        Option optNull2 = new Option(null, "longOnly", false, "desc");
        Option optNull3 = new Option(null, "otherLong", false, "desc");
        Option optWithOpt = new Option("a", "longOnly", false, "desc");
        Option optNoLong1 = new Option("a", null, false, "desc");
        Option optNoLong2 = new Option("a", null, false, "desc");

        assertTrue(optNull1.equals(optNull2));
        assertEquals(optNull1.hashCode(), optNull2.hashCode());
        assertFalse(optNull1.equals(optNull3));
        assertFalse(optNull1.equals(optWithOpt));
        assertFalse(optWithOpt.equals(optNull1));

        assertTrue(optNoLong1.equals(optNoLong2));
        assertEquals(optNoLong1.hashCode(), optNoLong2.hashCode());
        assertFalse(optNoLong1.equals(optWithOpt));
        assertFalse(optWithOpt.equals(optNoLong1));
    }

    // Tests addValueForProcessing with value separator when separator is not found
    @Test
    public void testAddValueForProcessing_withSeparatorNotPresent_addsSingleValue()
    {
        Option option = new Option("D", true, "desc");
        option.setValueSeparator('=');
        option.addValueForProcessing("novaluesep");
        assertEquals("novaluesep", option.getValue());
    }

    // Tests toString without long option, without type, and without args
    @Test
    public void testToString_minimalOption_formatsCorrectly()
    {
        Option option = new Option("s", "simple desc");
        String str = option.toString();
        assertTrue(str.contains("[ option: s"));
        assertFalse(str.contains("[ARG]"));
        assertTrue(str.contains("simple desc"));
    }
}