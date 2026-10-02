package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class OptionTest
{
    // Tests two-argument constructor and basic property getters
    @Test
    public void testConstructor_twoArgs_initializesProperties()
    {
        Option option = new Option("a", "description");
        assertEquals("a", option.getOpt());
        assertEquals("description", option.getDescription());
        assertNull(option.getLongOpt());
        assertFalse(option.hasArg());
        assertFalse(option.hasArgs());
        assertFalse(option.hasLongOpt());
        assertEquals("arg", option.getArgName());
        assertTrue(option.hasArgName());
        assertEquals('a', option.getId());
        assertEquals("a", option.getKey());
    }

    // Tests three-argument constructor with hasArg true
    @Test
    public void testConstructor_threeArgsWithHasArg_setsSingleArg()
    {
        Option option = new Option("b", true, "desc");
        assertTrue(option.hasArg());
        assertFalse(option.hasArgs());
        assertEquals(1, option.getArgs());
    }

    // Tests four-argument constructor with longOpt and hasArg false
    @Test
    public void testConstructor_fourArgs_setsAllFields()
    {
        Option option = new Option("c", "c-long", false, "desc");
        assertEquals("c", option.getOpt());
        assertEquals("c-long", option.getLongOpt());
        assertTrue(option.hasLongOpt());
        assertFalse(option.hasArg());
        assertEquals(Option.UNINITIALIZED, option.getArgs());
    }

    // Tests invalid option characters in constructor
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_invalidOptCharacter_throwsException()
    {
        new Option("invalid?", "description");
    }

    // Tests long option only (null opt) and key resolution
    @Test
    public void testGetKey_nullOpt_returnsLongOpt()
    {
        Option option = new Option(null, "verbose", true, "verbose mode");
        assertNull(option.getOpt());
        assertEquals("verbose", option.getLongOpt());
        assertEquals("verbose", option.getKey());
        assertEquals('v', option.getId());
    }

    // Tests setters and getters for optional attributes
    @Test
    public void testSettersAndGetters_customValues_returnUpdatedValues()
    {
        Option option = new Option("x", "desc");
        
        option.setLongOpt("extended");
        assertEquals("extended", option.getLongOpt());
        assertTrue(option.hasLongOpt());

        option.setDescription("new desc");
        assertEquals("new desc", option.getDescription());

        option.setType(String.class);
        assertEquals(String.class, option.getType());

        option.setRequired(true);
        assertTrue(option.isRequired());

        option.setOptionalArg(true);
        assertTrue(option.hasOptionalArg());

        option.setArgName("val");
        assertEquals("val", option.getArgName());
        assertTrue(option.hasArgName());

        option.setArgName(null);
        assertNull(option.getArgName());
        assertFalse(option.hasArgName());

        option.setArgName("");
        assertEquals("", option.getArgName());
        assertFalse(option.hasArgName());
    }

    // Tests adding value when no args are allowed
    @Test(expected = RuntimeException.class)
    public void testAddValueForProcessing_uninitializedArgs_throwsException()
    {
        Option option = new Option("f", "flag");
        option.addValueForProcessing("unexpected");
    }

    // Tests adding value exceeding maximum allowed arguments
    @Test(expected = RuntimeException.class)
    public void testAddValueForProcessing_exceedsArgLimit_throwsException()
    {
        Option option = new Option("f", true, "flag");
        option.addValueForProcessing("v1");
        option.addValueForProcessing("v2");
    }

    // Tests adding single argument and querying value getters
    @Test
    public void testGetValue_singleValue_returnsExpected()
    {
        Option option = new Option("f", true, "flag");
        assertNull(option.getValue());
        assertNull(option.getValues());
        assertEquals("default", option.getValue("default"));

        option.addValueForProcessing("value1");

        assertEquals("value1", option.getValue());
        assertEquals("value1", option.getValue(0));
        assertEquals("value1", option.getValue("default"));
        
        String[] values = option.getValues();
        assertNotNull(values);
        assertEquals(1, values.length);
        assertEquals("value1", values[0]);

        List valuesList = option.getValuesList();
        assertEquals(1, valuesList.size());
        assertEquals("value1", valuesList.get(0));
    }

    // Tests multiple arguments and unlimited values
    @Test
    public void testAddValueForProcessing_unlimitedArgs_acceptsMultiple()
    {
        Option option = new Option("m", "multiple args");
        option.setArgs(Option.UNLIMITED_VALUES);
        assertTrue(option.hasArgs());
        assertTrue(option.hasArg());

        option.addValueForProcessing("v1");
        option.addValueForProcessing("v2");

        String[] values = option.getValues();
        assertEquals(2, values.length);
        assertEquals("v1", values[0]);
        assertEquals("v2", values[1]);
    }

    // Tests value separator parsing with limited numberOfArgs
    @Test
    public void testProcessValue_withSeparatorAndLimitedArgs_parsesCorrectly()
    {
        Option option = new Option("D", "property");
        option.setArgs(2);
        option.setValueSeparator('=');
        assertTrue(option.hasValueSeparator());
        assertEquals('=', option.getValueSeparator());

        option.addValueForProcessing("key=value=extra");
        assertEquals(2, option.getValues().length);
        assertEquals("key", option.getValue(0));
        assertEquals("value=extra", option.getValue(1));
    }

    // Tests clearValues method
    @Test
    public void testClearValues_clearsExistingValues()
    {
        Option option = new Option("f", true, "flag");
        option.addValueForProcessing("val");
        assertEquals("val", option.getValue());

        option.clearValues();
        assertNull(option.getValue());
        assertNull(option.getValues());
        assertTrue(option.getValuesList().isEmpty());
    }

    // Tests deprecated addValue method throwing exception
    @Test(expected = UnsupportedOperationException.class)
    public void testAddValue_invoked_throwsUnsupportedOperationException()
    {
        Option option = new Option("o", "option");
        option.addValue("test");
    }

    // Tests acceptsArg and requiresArg states under various configurations
    @Test
    public void testAcceptsArgAndRequiresArg_differentConfigurations_returnsExpectedBoolean()
    {
        Option optNoArg = new Option("n", "no arg");
        assertFalse(optNoArg.acceptsArg());
        assertFalse(optNoArg.requiresArg());

        Option optRequiredArg = new Option("r", true, "required arg");
        assertTrue(optRequiredArg.acceptsArg());
        assertTrue(optRequiredArg.requiresArg());
        optRequiredArg.addValueForProcessing("val");
        assertFalse(optRequiredArg.acceptsArg());
        assertFalse(optRequiredArg.requiresArg());

        Option optOptionalArg = new Option("o", true, "optional arg");
        optOptionalArg.setOptionalArg(true);
        assertTrue(optOptionalArg.acceptsArg());
        assertFalse(optOptionalArg.requiresArg());

        Option optUnlimited = new Option("u", "unlimited");
        optUnlimited.setArgs(Option.UNLIMITED_VALUES);
        assertTrue(optUnlimited.acceptsArg());
        assertTrue(optUnlimited.requiresArg());
        optUnlimited.addValueForProcessing("val1");
        assertTrue(optUnlimited.acceptsArg());
        assertFalse(optUnlimited.requiresArg());
    }

    // Tests equals and hashCode contracts
    @Test
    public void testEqualsAndHashCode_sameAndDifferentOptions_behaveCorrectly()
    {
        Option opt1 = new Option("a", "longA", false, "desc");
        Option opt2 = new Option("a", "longA", false, "desc");
        Option opt3 = new Option("a", "longB", false, "desc");
        Option opt4 = new Option("b", "longA", false, "desc");
        Option nullOpt1 = new Option(null, "longA", false, "desc");
        Option nullOpt2 = new Option(null, "longA", false, "desc");

        assertTrue(opt1.equals(opt1));
        assertTrue(opt1.equals(opt2));
        assertEquals(opt1.hashCode(), opt2.hashCode());

        assertTrue(nullOpt1.equals(nullOpt2));
        assertEquals(nullOpt1.hashCode(), nullOpt2.hashCode());

        assertFalse(opt1.equals(opt3));
        assertFalse(opt1.equals(opt4));
        assertFalse(opt1.equals(nullOpt1));
        assertFalse(nullOpt1.equals(opt1));
        assertFalse(opt1.equals(null));
        assertFalse(opt1.equals("not an Option"));
    }

    // Tests clone method produces deep copy of values list
    @Test
    public void testClone_clonedOption_independentValuesList()
    {
        Option option = new Option("c", true, "clone test");
        option.addValueForProcessing("original");

        Option cloned = (Option) option.clone();
        assertNotSame(option, cloned);
        assertEquals(option, cloned);
        assertEquals(option.getValue(), cloned.getValue());

        cloned.clearValues();
        assertEquals("original", option.getValue());
        assertNull(cloned.getValue());
    }

    // Tests toString formatting with different configurations
    @Test
    public void testToString_variousOptions_returnsExpectedFormat()
    {
        Option simple = new Option("s", "simple");
        assertEquals("[ option: s  :: simple ]", simple.toString());

        Option withLongAndArg = new Option("o", "opt", true, "with arg");
        assertEquals("[ option: o opt  [ARG] :: with arg ]", withLongAndArg.toString());

        Option withArgsAndType = new Option("m", "multi", false, "with args");
        withArgsAndType.setArgs(2);
        withArgsAndType.setType(Integer.class);
        assertEquals("[ option: m multi [ARG...] :: with args :: class java.lang.Integer ]", withArgsAndType.toString());
    }

    // Tests getValue(int) returning null when no values are present
    @Test
    public void testGetValueByIndex_emptyValues_returnsNull()
    {
        Option option = new Option("a", true, "desc");
        assertNull(option.getValue(0));
        assertNull(option.getValue(-1));
        assertNull(option.getValue(5));
    }

    // Tests getValue(int) throwing IndexOutOfBoundsException when index is invalid and values exist
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueByIndex_outOfBounds_throwsException()
    {
        Option option = new Option("a", true, "desc");
        option.addValueForProcessing("val");
        option.getValue(2);
    }

    // Tests processValue with separator when numberOfArgs is UNLIMITED_VALUES
    @Test
    public void testProcessValue_withSeparatorAndUnlimitedArgs_parsesAllTokens()
    {
        Option option = new Option("D", "property");
        option.setArgs(Option.UNLIMITED_VALUES);
        option.setValueSeparator(',');

        option.addValueForProcessing("v1,v2,v3");
        String[] values = option.getValues();
        assertNotNull(values);
        assertEquals(3, values.length);
        assertEquals("v1", values[0]);
        assertEquals("v2", values[1]);
        assertEquals("v3", values[2]);
    }

    // Tests processValue with separator when the separator is not present in the value
    @Test
    public void testProcessValue_withSeparatorNotInValue_addsEntireValue()
    {
        Option option = new Option("D", "property");
        option.setArgs(2);
        option.setValueSeparator('=');

        option.addValueForProcessing("singleToken");
        assertEquals(1, option.getValues().length);
        assertEquals("singleToken", option.getValue(0));
    }

    // Tests equals comparisons when one option has longOpt null and the other has longOpt non-null
    @Test
    public void testEqualsAndHashCode_nullAndNonNullLongOpt()
    {
        Option withLong = new Option("a", "longA", false, "desc");
        Option withoutLong = new Option("a", "desc");
        assertFalse(withLong.equals(withoutLong));
        assertFalse(withoutLong.equals(withLong));

        Option nullOptLongA = new Option(null, "longA", false, "desc");
        Option nullOptLongB = new Option(null, "longB", false, "desc");
        assertFalse(nullOptLongA.equals(nullOptLongB));
    }

    // Tests setArgs with zero arguments
    @Test
    public void testSetArgs_zeroArgs_hasNoArgs()
    {
        Option option = new Option("z", "zero args");
        option.setArgs(0);
        assertFalse(option.hasArg());
        assertFalse(option.hasArgs());
        assertEquals(0, option.getArgs());
    }

    // Tests valid special single-character options
    @Test
    public void testConstructor_validSpecialCharacters_accepted()
    {
        Option questionOpt = new Option("?", "help option");
        assertEquals("?", questionOpt.getOpt());
        assertEquals('?', questionOpt.getId());

        Option atOpt = new Option("@", "at option");
        assertEquals("@", atOpt.getOpt());
        assertEquals('@', atOpt.getId());
    }
}