package org.apache.commons.cli2.option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.HelpLine;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.WriteableCommandLine;
import org.apache.commons.cli2.commandline.WriteableCommandLineImpl;
import org.apache.commons.cli2.validation.InvalidArgumentException;
import org.apache.commons.cli2.validation.Validator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ArgumentImplTest {

    // Tests default name assignment when name is null
    @Test
    public void testConstructor_nullName_defaultsToArg() {
        ArgumentImpl arg = new ArgumentImpl(null, "desc", 0, 1, '=', ',', null, null, null, 1);
        assertEquals("arg", arg.getPreferredName());
        assertEquals("desc", arg.getDescription());
        assertEquals(0, arg.getMinimum());
        assertEquals(1, arg.getMaximum());
        assertEquals('=', arg.getInitialSeparator());
        assertEquals(',', arg.getSubsequentSeparator());
        assertNull(arg.getValidator());
        assertNull(arg.getConsumeRemaining());
        assertNull(arg.getDefaultValues());
        assertFalse(arg.isRequired());
    }

    // Tests exception when minimum exceeds maximum
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_minGreaterThanMax_throwsIllegalArgumentException() {
        new ArgumentImpl("test", "desc", 5, 2, '\0', '\0', null, null, null, 1);
    }

    // Tests exception when default values count is less than minimum
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_tooFewDefaults_throwsIllegalArgumentException() {
        List defaults = Collections.singletonList("default1");
        new ArgumentImpl("test", "desc", 2, 5, '\0', '\0', null, null, defaults, 1);
    }

    // Tests exception when default values count exceeds maximum
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_tooManyDefaults_throwsIllegalArgumentException() {
        List defaults = new ArrayList();
        defaults.add("def1");
        defaults.add("def2");
        defaults.add("def3");
        new ArgumentImpl("test", "desc", 1, 2, '\0', '\0', null, null, defaults, 1);
    }

    // Tests stripBoundaryQuotes method with quoted and unquoted strings
    @Test
    public void testStripBoundaryQuotes_variousInputs_stripsQuotesCorrectly() {
        ArgumentImpl arg = new ArgumentImpl("test", "desc", 0, 1, '\0', '\0', null, null, null, 1);
        assertEquals("hello", arg.stripBoundaryQuotes("\"hello\""));
        assertEquals("\"hello", arg.stripBoundaryQuotes("\"hello"));
        assertEquals("hello\"", arg.stripBoundaryQuotes("hello\""));
        assertEquals("hello", arg.stripBoundaryQuotes("hello"));
        assertEquals("", arg.stripBoundaryQuotes("\"\""));
    }

    // Tests isRequired when minimum is positive
    @Test
    public void testIsRequired_minPositive_returnsTrue() {
        ArgumentImpl arg = new ArgumentImpl("test", "desc", 1, 2, '\0', '\0', null, null, null, 1);
        assertTrue(arg.isRequired());
    }

    // Tests canProcess, getPrefixes, and getTriggers behavior
    @Test
    public void testGettersAndBasicProperties_validState_returnsExpected() {
        ArgumentImpl arg = new ArgumentImpl("argName", "desc", 1, 1, '\0', '\0', null, "--", null, 42);
        assertTrue(arg.canProcess(null, "anything"));
        assertTrue(arg.getPrefixes().isEmpty());
        assertTrue(arg.getTriggers().isEmpty());
        assertEquals("--", arg.getConsumeRemaining());
    }

    // Tests processValues consuming all remaining arguments with consumeRemaining token
    @Test
    public void testProcessValues_consumeRemainingToken_addsAllRemainingValues() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 0, 5, '\0', '\0', null, "--", null, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(arg, new ArrayList());

        List args = new ArrayList();
        args.add("--");
        args.add("-notAnOption");
        args.add("value2");

        ListIterator iterator = args.listIterator();
        arg.processValues(commandLine, iterator, arg);

        List values = commandLine.getValues((Option) arg);
        assertEquals(2, values.size());
        assertEquals("-notAnOption", values.get(0));
        assertEquals("value2", values.get(1));
    }

    // Tests processValues stopping when encountering an argument that looks like an option
    @Test
    public void testProcessValues_looksLikeOption_stopsProcessing() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 0, 5, '\0', '\0', null, "--", null, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(arg, new ArrayList());

        List args = new ArrayList();
        args.add("-opt");
        args.add("value");

        ListIterator iterator = args.listIterator();
        arg.processValues(commandLine, iterator, arg);

        List values = commandLine.getValues((Option) arg);
        assertEquals(0, values.size());
        assertTrue(iterator.hasNext());
        assertEquals("-opt", iterator.next());
    }

    // Tests processValues with subsequent separator splitting tokens
    @Test
    public void testProcessValues_subsequentSeparator_splitsTokensCorrectly() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 0, 5, '\0', ',', null, "--", null, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(arg, new ArrayList());

        List args = new ArrayList();
        args.add("val1,val2,val3");

        ListIterator iterator = args.listIterator();
        arg.processValues(commandLine, iterator, arg);

        List values = commandLine.getValues((Option) arg);
        assertEquals(3, values.size());
        assertEquals("val1", values.get(0));
        assertEquals("val2", values.get(1));
        assertEquals("val3", values.get(2));
    }

    // Tests processValues throwing exception when subsequent split exceeds maximum
    @Test(expected = OptionException.class)
    public void testProcessValues_subsequentSplitExceedsMaximum_throwsOptionException() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 0, 2, '\0', ',', null, "--", null, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(arg, new ArrayList());

        List args = new ArrayList();
        args.add("val1,val2,val3");

        ListIterator iterator = args.listIterator();
        arg.processValues(commandLine, iterator, arg);
    }

    // Tests validate when values are fewer than minimum
    @Test(expected = OptionException.class)
    public void testValidate_fewerThanMinimumValues_throwsOptionException() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 2, 3, '\0', '\0', null, null, null, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(arg, new ArrayList());

        commandLine.addValue(arg, "val1");
        arg.validate(commandLine);
    }

    // Tests validate when values exceed maximum
    @Test(expected = OptionException.class)
    public void testValidate_moreThanMaximumValues_throwsOptionException() throws OptionException {
        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 1, 2, '\0', '\0', null, null, null, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(arg, new ArrayList());

        commandLine.addValue(arg, "val1");
        commandLine.addValue(arg, "val2");
        commandLine.addValue(arg, "val3");
        arg.validate(commandLine);
    }

    // Tests validate with a custom validator throwing InvalidArgumentException
    @Test(expected = OptionException.class)
    public void testValidate_validatorThrowsInvalidArgument_throwsOptionException() throws OptionException {
        Validator failingValidator = new Validator() {
            public void validate(List values) throws InvalidArgumentException {
                throw new InvalidArgumentException("Invalid argument value");
            }
        };

        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 1, 1, '\0', '\0', failingValidator, null, null, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(arg, new ArrayList());
        commandLine.addValue(arg, "invalidVal");

        arg.validate(commandLine);
    }

    // Tests validate with a passing validator
    @Test
    public void testValidate_validInput_passesWithoutException() throws OptionException {
        Validator passingValidator = new Validator() {
            public void validate(List values) throws InvalidArgumentException {
                // validation succeeds
            }
        };

        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 1, 2, '\0', '\0', passingValidator, null, null, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(arg, new ArrayList());
        commandLine.addValue(arg, "val1");

        arg.validate(commandLine);
        assertSame(passingValidator, arg.getValidator());
    }

    // Tests appendUsage with optional, bracketed, and numbered settings
    @Test
    public void testAppendUsage_optionalBracketedNumbered_appendsCorrectly() {
        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 1, 3, '\0', '\0', null, null, null, 1);
        Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_OPTIONAL);
        settings.add(DisplaySetting.DISPLAY_ARGUMENT_BRACKETED);
        settings.add(DisplaySetting.DISPLAY_ARGUMENT_NUMBERED);

        StringBuffer buffer = new StringBuffer();
        arg.appendUsage(buffer, settings, null);

        assertEquals("<arg1> [<arg2> [<arg3>]]", buffer.toString());
    }

    // Tests appendUsage with infinite maximum arguments
    @Test
    public void testAppendUsage_infiniteMaximum_appendsEllipsis() {
        ArgumentImpl arg = new ArgumentImpl("file", "desc", 1, Integer.MAX_VALUE, '\0', '\0', null, null, null, 1);
        Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_OPTIONAL);

        StringBuffer buffer = new StringBuffer();
        arg.appendUsage(buffer, settings, null);

        assertEquals("file [file] ...]", buffer.toString());
    }

    // Tests helpLines method returns single HelpLine element
    @Test
    public void testHelpLines_singleArgument_returnsSingleHelpLine() {
        ArgumentImpl arg = new ArgumentImpl("arg", "description", 0, 1, '\0', '\0', null, null, null, 1);
        List helpLines = arg.helpLines(0, Collections.EMPTY_SET, null);
        assertNotNull(helpLines);
        assertEquals(1, helpLines.size());
        HelpLine line = (HelpLine) helpLines.get(0);
        assertEquals("arg", line.getOption().getPreferredName());
    }

    // Tests defaults and defaultValues population on CommandLine
    @Test
    public void testDefaults_withDefaultValues_populatesCommandLine() {
        List defaultValues = new ArrayList();
        defaultValues.add("defaultVal");

        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 0, 1, '\0', '\0', null, null, defaultValues, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(arg, new ArrayList());

        arg.defaults(commandLine);
        assertEquals(defaultValues, commandLine.getValues((Option) arg));
    }
}