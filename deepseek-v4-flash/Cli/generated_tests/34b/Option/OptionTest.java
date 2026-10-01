package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

public class OptionTest {

    @Test
    public void testOptionConstructorValidatesNullOpt() {
        try {
            new Option(null, "description");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testOptionConstructorValidatesInvalidOpt() {
        try {
            new Option("invalid opt", "description");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testGetIdReturnsFirstCharacterOfOpt() {
        Option option = new Option("abc", "description");
        assertEquals('a', option.getId());
    }

    @Test
    public void testGetIdReturnsFirstCharacterOfLongOptWhenOptNull() {
        Option option = new Option(null, "long", false, "description");
        assertEquals('l', option.getId());
    }

    @Test
    public void testGetOptReturnsOpt() {
        Option option = new Option("a", "long", false, "description");
        assertEquals("a", option.getOpt());
    }

    @Test
    public void testGetOptReturnsNullWhenOptNull() {
        Option option = new Option(null, "long", false, "description");
        assertNull(option.getOpt());
    }

    @Test
    public void testGetLongOptReturnsLongOpt() {
        Option option = new Option("a", "long", false, "description");
        assertEquals("long", option.getLongOpt());
    }

    @Test
    public void testGetLongOptReturnsNullWhenNotSet() {
        Option option = new Option("a", "description");
        assertNull(option.getLongOpt());
    }

    @Test
    public void testHasLongOptReturnsTrueWhenSet() {
        Option option = new Option("a", "long", false, "description");
        assertTrue(option.hasLongOpt());
    }

    @Test
    public void testHasLongOptReturnsFalseWhenNotSet() {
        Option option = new Option("a", "description");
        assertFalse(option.hasLongOpt());
    }

    @Test
    public void testHasArgReturnsTrueWhenHasArgSet() {
        Option option = new Option("a", true, "description");
        assertTrue(option.hasArg());
    }

    @Test
    public void testHasArgReturnsFalseWhenNoArg() {
        Option option = new Option("a", "description");
        assertFalse(option.hasArg());
    }

    @Test
    public void testHasArgReturnsTrueForUnlimitedValues() {
        Option option = new Option("a", "description");
        option.setArgs(Option.UNLIMITED_VALUES);
        assertTrue(option.hasArg());
    }

    @Test
    public void testHasArgsReturnsTrueWhenMultipleValuesAllowed() {
        Option option = new Option("a", "description");
        option.setArgs(3);
        assertTrue(option.hasArgs());
    }

    @Test
    public void testHasArgsReturnsTrueForUnlimitedValues() {
        Option option = new Option("a", "description");
        option.setArgs(Option.UNLIMITED_VALUES);
        assertTrue(option.hasArgs());
    }

    @Test
    public void testHasArgsReturnsFalseForSingleArg() {
        Option option = new Option("a", true, "description");
        assertFalse(option.hasArgs());
    }

    @Test
    public void testAddValueForProcessingThrowsWhenUninitialized() {
        Option option = new Option("a", "description");
        try {
            option.addValueForProcessing("value");
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("NO_ARGS_ALLOWED", e.getMessage());
        }
    }

    @Test
    public void testAddValueForProcessingAddsValue() {
        Option option = new Option("a", true, "description");
        option.addValueForProcessing("value");
        assertEquals("value", option.getValue());
    }

    @Test
    public void testAddValueForProcessingWithSeparator() {
        Option option = new Option("a", true, "description");
        option.setValueSeparator('=');
        option.addValueForProcessing("key=value");
        assertEquals("key", option.getValue());
        assertEquals("value", option.getValue(1));
    }

    @Test
    public void testGetValueReturnsNullWhenNoValues() {
        Option option = new Option("a", "description");
        assertNull(option.getValue());
    }

    @Test
    public void testGetValueWithIndexReturnsNullWhenNoValues() {
        Option option = new Option("a", "description");
        assertNull(option.getValue(0));
    }

    @Test
    public void testGetValueWithDefaultReturnsDefaultWhenNoValue() {
        Option option = new Option("a", "description");
        assertEquals("default", option.getValue("default"));
    }

    @Test
    public void testGetValuesReturnsNullWhenNoValues() {
        Option option = new Option("a", "description");
        assertNull(option.getValues());
    }

    @Test
    public void testGetValuesReturnsArrayWhenValuesExist() {
        Option option = new Option("a", true, "description");
        option.addValueForProcessing("value1");
        option.addValueForProcessing("value2");
        assertArrayEquals(new String[]{"value1", "value2"}, option.getValues());
    }

    @Test
    public void testClearValuesEmptiesValues() {
        Option option = new Option("a", true, "description");
        option.addValueForProcessing("value");
        option.clearValues();
        assertNull(option.getValue());
    }

    @Test
    public void testCloneCreatesIndependentValuesList() {
        Option option = new Option("a", true, "description");
        option.addValueForProcessing("value");
        Option clone = (Option) option.clone();
        clone.addValueForProcessing("another");
        assertEquals(1, option.getValues().length);
        assertEquals(2, clone.getValues().length);
    }

    @Test
    public void testToStringContainsDescription() {
        Option option = new Option("a", "test description");
        assertTrue(option.toString().contains("test description"));
    }

    @Test
    public void testToStringContainsTypeWhenSet() {
        Option option = new Option("a", "description");
        option.setType(String.class);
        assertTrue(option.toString().contains("java.lang.String"));
    }

    @Test
    public void testEqualsReturnsTrueForSameOptAndLongOpt() {
        Option option1 = new Option("a", "long", false, "description");
        Option option2 = new Option("a", "long", false, "description");
        assertEquals(option1, option2);
    }

    @Test
    public void testEqualsReturnsFalseForDifferentOpt() {
        Option option1 = new Option("a", "description");
        Option option2 = new Option("b", "description");
        assertFalse(option1.equals(option2));
    }

    @Test
    public void testEqualsReturnsFalseForDifferentLongOpt() {
        Option option1 = new Option("a", "long1", false, "description");
        Option option2 = new Option("a", "long2", false, "description");
        assertFalse(option1.equals(option2));
    }

    @Test
    public void testHasValueSeparatorReturnsFalseByDefault() {
        Option option = new Option("a", "description");
        assertFalse(option.hasValueSeparator());
    }

    @Test
    public void testHasValueSeparatorReturnsTrueWhenSet() {
        Option option = new Option("a", "description");
        option.setValueSeparator('=');
        assertTrue(option.hasValueSeparator());
    }

    @Test
    public void testGetValueSeparatorReturnsSetValue() {
        Option option = new Option("a", "description");
        option.setValueSeparator('=');
        assertEquals('=', option.getValueSeparator());
    }

    @Test
    public void testSetArgsChangesNumberOfArgs() {
        Option option = new Option("a", "description");
        option.setArgs(5);
        assertEquals(5, option.getArgs());
    }

    @Test
    public void testRequiresArgReturnsFalseForOptionalArg() {
        Option option = new Option("a", true, "description");
        option.setOptionalArg(true);
        assertFalse(option.requiresArg());
    }

    @Test
    public void testRequiresArgReturnsTrueWhenArgRequired() {
        Option option = new Option("a", true, "description");
        assertFalse(option.requiresArg());
    }

    @Test
    public void testAcceptsArgReturnsFalseWhenNoArgConfigured() {
        Option option = new Option("a", "description");
        assertFalse(option.acceptsArg());
    }

    @Test
    public void testAddValueThrowsUnsupportedOperation() {
        Option option = new Option("a", "description");
        try {
            option.addValue("value");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testHasOptionalArgReturnsFalseByDefault() {
        Option option = new Option("a", "description");
        assertFalse(option.hasOptionalArg());
    }

    @Test
    public void testHasOptionalArgReturnsTrueWhenSet() {
        Option option = new Option("a", "description");
        option.setOptionalArg(true);
        assertTrue(option.hasOptionalArg());
    }

    @Test
    public void testGetValuesListReturnsUnmodifiableList() {
        Option option = new Option("a", "description");
        try {
            option.getValuesList().add("test");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
}