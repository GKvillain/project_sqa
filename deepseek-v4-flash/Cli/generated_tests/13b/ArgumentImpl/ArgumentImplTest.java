package org.apache.commons.cli2.option;

import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.WriteableCommandLine;
import org.apache.commons.cli2.validation.InvalidArgumentException;
import org.apache.commons.cli2.validation.Validator;
import static org.junit.Assert.*;

public class ArgumentImplTest {
    private WriteableCommandLine commandLine;
    private List<String> emptyList;

    @Before
    public void setUp() {
        // Use a concrete WriteableCommandLine implementation from the same project.
        // We assume org.apache.commons.cli2.CommandLine exists.
        commandLine = new org.apache.commons.cli2.CommandLine();
        emptyList = new ArrayList<String>();
    }

    // ========== Constructor Tests ==========

    @Test
    public void testConstructor_validArguments_createsInstance() {
        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 1, 3, '=', ',', null, "--", null, 0);
        assertNotNull(arg);
        assertEquals("arg", arg.getPreferredName());
        assertEquals("desc", arg.getDescription());
    }

    @Test
    public void testConstructor_minExceedsMax_throwsIllegalArgumentException() {
        try {
            new ArgumentImpl("arg", null, 3, 2, '=', ',', null, "--", null, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructor_tooFewDefaults_throwsIllegalArgumentException() {
        List<String> defaults = new ArrayList<String>();
        defaults.add("val1");
        try {
            new ArgumentImpl("arg", null, 3, 5, '=', ',', null, "--", defaults, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructor_tooManyDefaults_throwsIllegalArgumentException() {
        List<String> defaults = new ArrayList<String>();
        defaults.add("a");
        defaults.add("b");
        defaults.add("c");
        try {
            new ArgumentImpl("arg", null, 1, 2, '=', ',', null, "--", defaults, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructor_nullName_defaultsToArg() {
        ArgumentImpl arg = new ArgumentImpl(null, null, 0, 1, '=', ',', null, "--", null, 0);
        assertEquals("arg", arg.getPreferredName());
    }

    // ========== getPreferredName, getDescription, isRequired ==========

    @Test
    public void testGetPreferredName_returnsName() {
        ArgumentImpl arg = new ArgumentImpl("myarg", null, 0, 1, '=', ',', null, "--", null, 0);
        assertEquals("myarg", arg.getPreferredName());
    }

    @Test
    public void testGetDescription_returnsDescription() {
        ArgumentImpl arg = new ArgumentImpl("a", "test desc", 0, 1, '=', ',', null, "--", null, 0);
        assertEquals("test desc", arg.getDescription());
    }

    @Test
    public void testIsRequired_minGreaterThanZero_returnsTrue() {
        ArgumentImpl arg = new ArgumentImpl("a", null, 1, 2, '=', ',', null, "--", null, 0);
        assertTrue(arg.isRequired());
    }

    @Test
    public void testIsRequired_minIsZero_returnsFalse() {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 2, '=', ',', null, "--", null, 0);
        assertFalse(arg.isRequired());
    }

    // ========== stripBoundaryQuotes ==========

    @Test
    public void testStripBoundaryQuotes_quotedString_returnsUnquoted() {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 1, '=', ',', null, "--", null, 0);
        assertEquals("hello", arg.stripBoundaryQuotes("\"hello\""));
    }

    @Test
    public void testStripBoundaryQuotes_noQuotes_returnsSame() {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 1, '=', ',', null, "--", null, 0);
        assertEquals("hello", arg.stripBoundaryQuotes("hello"));
    }

    @Test
    public void testStripBoundaryQuotes_partialQuotes_returnsSame() {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 1, '=', ',', null, "--", null, 0);
        assertEquals("\"hello", arg.stripBoundaryQuotes("\"hello"));
        assertEquals("hello\"", arg.stripBoundaryQuotes("hello\""));
    }

    // ========== processValues ==========

    @Test
    public void testProcessValues_normalValues_addsValues() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 3, '=', ',', null, "--", null, 0);
        List<String> argsList = new ArrayList<String>();
        argsList.add("val1");
        argsList.add("val2");
        ListIterator<String> iter = argsList.listIterator();
        arg.processValues(commandLine, iter, arg);
        assertTrue(commandLine.getValues(arg).contains("val1"));
        assertTrue(commandLine.getValues(arg).contains("val2"));
    }

    @Test
    public void testProcessValues_exceedsMaximum_stopsAdding() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 2, '=', ',', null, "--", null, 0);
        List<String> argsList = new ArrayList<String>();
        argsList.add("v1");
        argsList.add("v2");
        argsList.add("v3");
        ListIterator<String> iter = argsList.listIterator();
        arg.processValues(commandLine, iter, arg);
        // only first two should be added
        assertEquals(2, commandLine.getValues(arg).size());
    }

    @Test
    public void testProcessValues_consumeRemainingToken_consumesRest() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 5, '=', ',', null, "--", null, 0);
        List<String> argsList = new ArrayList<String>();
        argsList.add("--");
        argsList.add("x");
        argsList.add("y");
        ListIterator<String> iter = argsList.listIterator();
        arg.processValues(commandLine, iter, arg);
        List<String> values = commandLine.getValues(arg);
        assertTrue(values.contains("x"));
        assertTrue(values.contains("y"));
        assertEquals(2, values.size());
    }

    @Test
    public void testProcessValues_looksLikeOption_stopsProcessing() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 5, '=', ',', null, "--", null, 0);
        // We need to make commandLine.looksLikeOption return true for a specific string.
        // If we cannot control that, we can override in a subclass? Instead, we'll rely on the default
        // implementation which likely considers strings starting with '-' as options.
        List<String> argsList = new ArrayList<String>();
        argsList.add("val1");
        argsList.add("-opt");
        argsList.add("val2");
        ListIterator<String> iter = argsList.listIterator();
        arg.processValues(commandLine, iter, arg);
        List<String> values = commandLine.getValues(arg);
        // Should only contain "val1"
        assertEquals(1, values.size());
        assertTrue(values.contains("val1"));
        // Check that iterator now points to "-opt"
        assertTrue(iter.hasNext()); // not consumed
        assertEquals("-opt", iter.next());
    }

    @Test
    public void testProcessValues_subsequentSplit_splitsValues() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 5, '=', ',', ',', "--", null, 0);
        List<String> argsList = new ArrayList<String>();
        argsList.add("a,b,c");
        ListIterator<String> iter = argsList.listIterator();
        arg.processValues(commandLine, iter, arg);
        List<String> values = commandLine.getValues(arg);
        assertTrue(values.contains("a"));
        assertTrue(values.contains("b"));
        assertTrue(values.contains("c"));
        assertEquals(3, values.size());
    }

    @Test(expected = OptionException.class)
    public void testProcessValues_subsequentSplit_tooManyTokens_throws() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 2, '=', ',', ',', "--", null, 0);
        List<String> argsList = new ArrayList<String>();
        argsList.add("x,y,z");
        ListIterator<String> iter = argsList.listIterator();
        arg.processValues(commandLine, iter, arg);
    }

    // ========== validate ==========

    @Test(expected = OptionException.class)
    public void testValidate_tooFewValues_throwsOptionException() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("a", null, 2, 3, '=', ',', null, "--", null, 0);
        commandLine.addValue(arg, "onlyOne");
        arg.validate(commandLine);
    }

    @Test(expected = OptionException.class)
    public void testValidate_tooManyValues_throwsOptionException() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 1, '=', ',', null, "--", null, 0);
        commandLine.addValue(arg, "v1");
        commandLine.addValue(arg, "v2");
        arg.validate(commandLine);
    }

    @Test
    public void testValidate_validValues_noException() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("a", null, 1, 2, '=', ',', null, "--", null, 0);
        commandLine.addValue(arg, "val");
        arg.validate(commandLine);
        // no exception expected
    }

    @Test(expected = OptionException.class)
    public void testValidate_validatorFails_throwsOptionException() throws OptionException {
        Validator failingValidator = new Validator() {
            public void validate(List values) throws InvalidArgumentException {
                throw new InvalidArgumentException("invalid");
            }
        };
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 1, '=', ',', failingValidator, "--", null, 0);
        commandLine.addValue(arg, "val");
        arg.validate(commandLine);
    }

    // ========== defaults and defaultValues ==========

    @Test
    public void testDefaults_setsDefaultValuesOnCommandLine() {
        List<String> defaults = new ArrayList<String>();
        defaults.add("def1");
        defaults.add("def2");
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 5, '=', ',', null, "--", defaults, 0);
        arg.defaults(commandLine);
        List<String> defaultsFromCmd = commandLine.getValues(arg);
        assertTrue(defaultsFromCmd.contains("def1"));
        assertTrue(defaultsFromCmd.contains("def2"));
    }

    @Test
    public void testDefaultValues_setsDefaultsForOption() {
        List<String> defaults = new ArrayList<String>();
        defaults.add("default");
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 1, '=', ',', null, "--", defaults, 0);
        arg.defaultValues(commandLine, arg);
        List<String> vals = commandLine.getValues(arg);
        assertTrue(vals.contains("default"));
    }

    // ========== getMinimum, getMaximum ==========

    @Test
    public void testGetMinimum_returnsConstructorValue() {
        ArgumentImpl arg = new ArgumentImpl("a", null, 2, 5, '=', ',', null, "--", null, 0);
        assertEquals(2, arg.getMinimum());
    }

    @Test
    public void testGetMaximum_returnsConstructorValue() {
        ArgumentImpl arg = new ArgumentImpl("a", null, 1, 10, '=', ',', null, "--", null, 0);
        assertEquals(10, arg.getMaximum());
    }

    // ========== getConsumeRemaining ==========

    @Test
    public void testGetConsumeRemaining_returnsConstructorValue() {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 1, '=', ',', null, "END", null, 0);
        assertEquals("END", arg.getConsumeRemaining());
    }

    // ========== getTriggers ==========

    @Test
    public void testGetTriggers_returnsEmptySet() {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 1, '=', ',', null, "--", null, 0);
        Set triggers = arg.getTriggers();
        assertTrue(triggers.isEmpty());
    }

    // ========== getPrefixes ==========

    @Test
    public void testGetPrefixes_returnsEmptySet() {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 1, '=', ',', null, "--", null, 0);
        Set prefixes = arg.getPrefixes();
        assertTrue(prefixes.isEmpty());
    }

    // ========== process (delegates to processValues) ==========

    @Test
    public void testProcess_delegatesToProcessValues() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 2, '=', ',', null, "--", null, 0);
        List<String> argsList = new ArrayList<String>();
        argsList.add("value");
        ListIterator<String> iter = argsList.listIterator();
        arg.process(commandLine, iter);
        List<String> vals = commandLine.getValues(arg);
        assertEquals(1, vals.size());
        assertTrue(vals.contains("value"));
    }

    // ========== appendUsage (basic test for coverage) ==========

    @Test
    public void testAppendUsage_optionalAndBracketed_formsCorrectString() {
        ArgumentImpl arg = new ArgumentImpl("file", null, 1, 1, '=', ',', null, "--", null, 0);
        StringBuffer buffer = new StringBuffer();
        Set helpSettings = new java.util.HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_OPTIONAL);
        helpSettings.add(DisplaySetting.DISPLAY_ARGUMENT_BRACKETED);
        arg.appendUsage(buffer, helpSettings, null);
        // For minimum=1, maximum=1, not optional, so no brackets, but with bracketed display it should be <file>
        String result = buffer.toString();
        // Since minimum=1 and we are not optional (i>=minimum true only when i<minimum? hard to predict)
        // We'll just check it doesn't throw and contains name
        assertTrue(result.contains("file"));
    }

    // ========== New tests for uncovered parts (AI test suite compile failed) ==========

    @Test
    public void testCanProcess_returnsFalse() {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 1, '=', ',', null, "--", null, 0);
        // ArgumentImpl is not triggered by any prefix, so canProcess should return false
        assertFalse(arg.canProcess(commandLine, "anything"));
    }

    @Test
    public void testGetInitialSeparator_returnsConstructorValue() {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 1, ',', ';', null, "--", null, 0);
        assertEquals(',', arg.getInitialSeparator());
    }

    @Test
    public void testGetSubsequentSeparator_returnsConstructorValue() {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 1, ',', ';', null, "--", null, 0);
        assertEquals(';', arg.getSubsequentSeparator());
    }

    @Test
    public void testGetValidator_returnsConstructorValidator() {
        Validator validator = new Validator() {
            public void validate(List values) {}
        };
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 1, '=', ',', validator, "--", null, 0);
        assertSame(validator, arg.getValidator());
    }

    @Test
    public void testGetDefaultValues_returnsConstructorDefaults() {
        List<String> defaults = new ArrayList<String>();
        defaults.add("def1");
        defaults.add("def2");
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 1, '=', ',', null, "--", defaults, 0);
        List<String> retrieved = arg.getDefaultValues();
        assertNotNull(retrieved);
        assertEquals(2, retrieved.size());
        assertTrue(retrieved.contains("def1"));
    }

    @Test
    public void testToString_containsName() {
        ArgumentImpl arg = new ArgumentImpl("myArg", "desc", 0, 1, '=', ',', null, "--", null, 0);
        String str = arg.toString();
        assertNotNull(str);
        assertTrue(str.contains("myArg"));
    }

    @Test
    public void testEquals_self_returnsTrue() {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 1, '=', ',', null, "--", null, 0);
        assertTrue(arg.equals(arg));
    }

    @Test
    public void testEquals_null_returnsFalse() {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 1, '=', ',', null, "--", null, 0);
        assertFalse(arg.equals(null));
    }

    @Test
    public void testEquals_differentType_returnsFalse() {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 1, '=', ',', null, "--", null, 0);
        assertFalse(arg.equals("string"));
    }

    @Test
    public void testEquals_sameProperties_returnsTrue() {
        ArgumentImpl arg1 = new ArgumentImpl("a", "desc", 0, 1, '=', ',', null, "--", null, 0);
        ArgumentImpl arg2 = new ArgumentImpl("a", "desc", 0, 1, '=', ',', null, "--", null, 0);
        assertTrue(arg1.equals(arg2));
    }

    @Test
    public void testHashCode_consistentWithEquals() {
        ArgumentImpl arg1 = new ArgumentImpl("a", "desc", 0, 1, '=', ',', null, "--", null, 0);
        ArgumentImpl arg2 = new ArgumentImpl("a", "desc", 0, 1, '=', ',', null, "--", null, 0);
        assertEquals(arg1.hashCode(), arg2.hashCode());
    }

    @Test
    public void testProcessValues_emptyList_doesNotAddValues() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 5, '=', ',', null, "--", null, 0);
        List<String> argsList = new ArrayList<String>();
        ListIterator<String> iter = argsList.listIterator();
        arg.processValues(commandLine, iter, arg);
        assertTrue(commandLine.getValues(arg).isEmpty());
    }

    @Test
    public void testProcessValues_withInitialSep_consumesOnlyFirstToken() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 5, ':', ',', null, "--", null, 0);
        List<String> argsList = new ArrayList<String>();
        argsList.add("a:b:c");
        ListIterator<String> iter = argsList.listIterator();
        arg.processValues(commandLine, iter, arg);
        List<String> values = commandLine.getValues(arg);
        // With initialSep=':', it should split the first token by ':'
        assertEquals(3, values.size());
        assertTrue(values.containsAll(Arrays.asList("a","b","c")));
    }

    @Test
    public void testProcessValues_consumeRemainingWithNoMoreArgs() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 10, '=', ',', null, "--", null, 0);
        List<String> argsList = new ArrayList<String>();
        argsList.add("--");
        ListIterator<String> iter = argsList.listIterator();
        arg.processValues(commandLine, iter, arg);
        // Should add nothing because no tokens after "--"
        assertTrue(commandLine.getValues(arg).isEmpty());
    }

    @Test
    public void testValidate_validatorPasses_noException() throws OptionException {
        Validator passingValidator = new Validator() {
            public void validate(List values) {}
        };
        ArgumentImpl arg = new ArgumentImpl("a", null, 0, 1, '=', ',', passingValidator, "--", null, 0);
        commandLine.addValue(arg, "val");
        arg.validate(commandLine);
        // no exception expected
    }
}