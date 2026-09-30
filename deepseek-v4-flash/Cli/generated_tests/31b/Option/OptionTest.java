package org.apache.commons.cli;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.List;

public class OptionTest {

    // Tests constructor with valid short option and description
    @Test
    public void testConstructor_validOptAndDescription_createsOption() {
        Option option = new Option("a", "description");
        assertEquals("a", option.getOpt());
        assertEquals("description", option.getDescription());
        assertFalse(option.hasArg());
        assertFalse(option.hasLongOpt());
        assertNull(option.getLongOpt());
    }

    // Tests constructor with hasArg=true
    @Test
    public void testConstructor_hasArgTrue_setsArgsToOne() {
        Option option = new Option("b", true, "desc");
        assertTrue(option.hasArg());
        assertEquals(1, option.getArgs());
    }

    // Tests constructor with hasArg=false
    @Test
    public void testConstructor_hasArgFalse_argsUninitialized() {
        Option option = new Option("c", false, "desc");
        assertFalse(option.hasArg());
        assertEquals(Option.UNINITIALIZED, option.getArgs());
    }

    // Tests constructor with long option and hasArg
    @Test
    public void testConstructor_withLongOptAndHasArg_createsOption() {
        Option option = new Option("d", "long-d", true, "desc");
        assertEquals("d", option.getOpt());
        assertEquals("long-d", option.getLongOpt());
        assertTrue(option.hasArg());
        assertEquals(1, option.getArgs());
    }

    // Tests constructor with null opt (long option only)
    @Test
    public void testConstructor_nullOpt_usesLongOptAsKey() {
        // OptionValidator allows null opt for long-only options
        Option option = new Option(null, "long-only", true, "desc");
        assertNull(option.getOpt());
        assertEquals("long-only", option.getKey());
        assertEquals("long-only", option.getLongOpt());
        assertTrue(option.hasArg());
    }

    // Tests getId with single character opt
    @Test
    public void testGetId_singleCharOpt_returnsCharInt() {
        Option option = new Option("x", "desc");
        assertEquals('x', option.getId());
    }

    // Tests getId with null opt (long option) returns first char of longOpt
    @Test
    public void testGetId_nullOpt_returnsFirstCharOfLongOpt() {
        Option option = new Option(null, "test", false, "desc");
        assertEquals('t', option.getId());
    }

    // Tests hasArg with UNLIMITED_VALUES
    @Test
    public void testHasArg_unlimitedValues_returnsTrue() {
        Option option = new Option("e", "desc");
        option.setArgs(Option.UNLIMITED_VALUES);
        assertTrue(option.hasArg());
    }

    // Tests hasArgs with numberOfArgs > 1
    @Test
    public void testHasArgs_multipleArgs_returnsTrue() {
        Option option = new Option("f", true, "desc");
        option.setArgs(3);
        assertTrue(option.hasArgs());
    }

    // Tests hasArgs with UNLIMITED_VALUES
    @Test
    public void testHasArgs_unlimitedValues_returnsTrue() {
        Option option = new Option("g", "desc");
        option.setArgs(Option.UNLIMITED_VALUES);
        assertTrue(option.hasArgs());
    }

    // Tests hasArgs with numberOfArgs == 1
    @Test
    public void testHasArgs_singleArg_returnsFalse() {
        Option option = new Option("h", "desc");
        option.setArgs(1);
        assertFalse(option.hasArgs());
    }

    // Tests isRequired default false
    @Test
    public void testIsRequired_default_returnsFalse() {
        Option option = new Option("i", "desc");
        assertFalse(option.isRequired());
    }

    // Tests setRequired true
    @Test
    public void testSetRequired_true_returnsTrue() {
        Option option = new Option("j", "desc");
        option.setRequired(true);
        assertTrue(option.isRequired());
    }

    // Tests hasOptionalArg default false
    @Test
    public void testHasOptionalArg_default_returnsFalse() {
        Option option = new Option("k", "desc");
        assertFalse(option.hasOptionalArg());
    }

    // Tests setOptionalArg true
    @Test
    public void testSetOptionalArg_true_returnsTrue() {
        Option option = new Option("l", "desc");
        option.setOptionalArg(true);
        assertTrue(option.hasOptionalArg());
    }

    // Tests getValue with no values added
    @Test
    public void testGetValue_noValues_returnsNull() {
        Option option = new Option("m", "desc");
        assertNull(option.getValue());
    }

    // Tests getValue after adding a value
    @Test
    public void testGetValue_withValue_returnsValue() {
        Option option = new Option("n", true, "desc");
        option.addValueForProcessing("testVal");
        assertEquals("testVal", option.getValue());
    }

    // Tests getValue(int) with index out of bounds
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueByIndex_outOfBounds_throwsException() {
        Option option = new Option("o", true, "desc");
        option.addValueForProcessing("val1");
        option.getValue(5); // index 5 is out of bounds (size=1)
    }

    // Tests getValue with defaultValue when no value present
    @Test
    public void testGetValueWithDefault_noValue_returnsDefault() {
        Option option = new Option("p", "desc");
        assertEquals("default", option.getValue("default"));
    }

    // Tests getValues with no values
    @Test
    public void testGetValues_noValues_returnsNull() {
        Option option = new Option("q", "desc");
        assertNull(option.getValues());
    }

    // Tests getValues with values
    @Test
    public void testGetValues_withValues_returnsArray() {
        Option option = new Option("r", true, "desc");
        option.setArgs(2);
        option.addValueForProcessing("val1");
        option.addValueForProcessing("val2");
        String[] values = option.getValues();
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals("val1", values[0]);
        assertEquals("val2", values[1]);
    }

    // Tests hasValueSeparator default false
    @Test
    public void testHasValueSeparator_default_returnsFalse() {
        Option option = new Option("s", "desc");
        assertFalse(option.hasValueSeparator());
    }

    // Tests setValueSeparator and hasValueSeparator
    @Test
    public void testSetValueSeparator_validChar_returnsTrue() {
        Option option = new Option("t", "desc");
        option.setValueSeparator('=');
        assertTrue(option.hasValueSeparator());
        assertEquals('=', option.getValueSeparator());
    }

    // Tests addValueForProcessing with UNINITIALIZED args throws RuntimeException
    @Test(expected = RuntimeException.class)
    public void testAddValueForProcessing_noArgsAllowed_throwsException() {
        Option option = new Option("u", "desc");
        option.addValueForProcessing("value");
    }

    // Tests setType and getType
    @Test
    public void testSetType_stringType_returnsCorrectType() {
        Option option = new Option("v", "desc");
        option.setType(String.class);
        assertEquals(String.class, option.getType());
    }

    // Tests setArgName and getArgName
    @Test
    public void testSetArgName_validName_returnsName() {
        Option option = new Option("w", "desc");
        option.setArgName("myArg");
        assertEquals("myArg", option.getArgName());
    }

    // Tests hasArgName when argName is set
    @Test
    public void testHasArgName_argNameSet_returnsTrue() {
        Option option = new Option("x", "desc");
        option.setArgName("argname");
        assertTrue(option.hasArgName());
    }

    // Tests hasArgName with default argName (non-empty "arg")
    @Test
    public void testHasArgName_defaultArgName_returnsTrue() {
        Option option = new Option("y", "desc");
        assertTrue(option.hasArgName());
    }

    // Tests equals with same opt and longOpt
    @Test
    public void testEquals_sameOptAndLongOpt_returnsTrue() {
        Option option1 = new Option("a", "long", false, "desc1");
        Option option2 = new Option("a", "long", false, "desc2");
        assertEquals(option1, option2);
    }

    // Tests equals with different opt
    @Test
    public void testEquals_differentOpt_returnsFalse() {
        Option option1 = new Option("a", "desc");
        Option option2 = new Option("b", "desc");
        assertFalse(option1.equals(option2));
    }

    // Tests clone method
    @Test
    public void testClone_clonedOption_equalsOriginal() {
        Option option = new Option("z", "long-z", true, "desc");
        option.addValueForProcessing("val");
        Option cloned = (Option) option.clone();
        assertEquals(option, cloned);
        assertNotSame(option, cloned);
    }

    // Tests clearValues
    @Test
    public void testClearValues_afterAddingValues_valuesCleared() {
        Option option = new Option("a", true, "desc");
        option.addValueForProcessing("value1");
        option.clearValues();
        assertNull(option.getValue());
        assertTrue(option.getValuesList().isEmpty());
    }

    // Tests requiresArg with optionalArg true
    @Test
    public void testRequiresArg_optionalArg_returnsFalse() {
        Option option = new Option("b", "desc");
        option.setOptionalArg(true);
        assertFalse(option.requiresArg());
    }

    // Tests requiresArg with UNLIMITED_VALUES and no values
    @Test
    public void testRequiresArg_unlimitedValuesNoValues_returnsTrue() {
        Option option = new Option("c", "desc");
        option.setArgs(Option.UNLIMITED_VALUES);
        assertTrue(option.requiresArg());
    }

    // Tests requiresArg with UNLIMITED_VALUES and one value
    @Test
    public void testRequiresArg_unlimitedValuesWithValue_returnsFalse() {
        Option option = new Option("d", true, "desc");
        option.setArgs(Option.UNLIMITED_VALUES);
        option.addValueForProcessing("val");
        // requiresArg returns values.size() < 1, so false
        assertFalse(option.requiresArg());
    }

    // Tests acceptsArg when numberOfArgs <=0 and values.size() < numberOfArgs is false
    @Test
    public void testAcceptsArg_noArgsSet_returnsFalse() {
        Option option = new Option("e", "desc");
        // numberOfArgs = UNINITIALIZED (-1), hasArg returns false
        assertFalse(option.acceptsArg());
    }

    // Tests toString basic output
    @Test
    public void testToString_basicOption_containsOptAndDesc() {
        Option option = new Option("f", "description");
        String str = option.toString();
        assertTrue(str.contains("f"));
        assertTrue(str.contains("description"));
    }

    // Tests toString with longOpt
    @Test
    public void testToString_withLongOpt_containsLongOpt() {
        Option option = new Option("g", "long-g", false, "desc");
        String str = option.toString();
        assertTrue(str.contains("long-g"));
    }

    // Tests hashCode consistency with equals
    @Test
    public void testHashCode_consistentWithEquals() {
        Option option1 = new Option("h", "long-h", false, "desc1");
        Option option2 = new Option("h", "long-h", false, "desc2");
        assertEquals(option1.hashCode(), option2.hashCode());
    }
}