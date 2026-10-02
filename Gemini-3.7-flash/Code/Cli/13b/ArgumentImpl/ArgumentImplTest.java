package org.apache.commons.cli2.option;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.HelpLine;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.commandline.WriteableCommandLineImpl;
import org.apache.commons.cli2.validation.InvalidArgumentException;
import org.apache.commons.cli2.validation.Validator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ArgumentImplTest {

    private ArgumentImpl argument;

    @Before
    public void setUp() {
        argument = new ArgumentImpl("arg", "argument description", 1, 2, '=', ',', null, "--", null, 0);
    }

    // Tests default name assignment when name is null
    @Test
    public void testConstructor_nullName_defaultsToArg() {
        ArgumentImpl arg = new ArgumentImpl(null, "desc", 0, 1, '\0', '\0', null, "--", null, 0);
        assertEquals("arg", arg.getPreferredName());
    }

    // Tests minimum greater than maximum throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_minExceedsMax_throwsIllegalArgumentException() {
        new ArgumentImpl("test", "desc", 3, 2, '\0', '\0', null, "--", null, 0);
    }

    // Tests defaults list smaller than minimum throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_tooFewDefaults_throwsIllegalArgumentException() {
        List defaults = Collections.singletonList("val1");
        new ArgumentImpl("test", "desc", 2, 3, '\0', '\0', null, "--", defaults, 0);
    }

    // Tests defaults list greater than maximum throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_tooManyDefaults_throwsIllegalArgumentException() {
        List defaults = Arrays.asList(new String[]{"val1", "val2", "val3"});
        new ArgumentImpl("test", "desc", 1, 2, '\0', '\0', null, "--", defaults, 0);
    }

    // Tests getters return correctly configured values
    @Test
    public void testGetters_validConfiguration_returnsExpectedValues() {
        Validator dummyValidator = new Validator() {
            public void validate(List values) throws InvalidArgumentException {
            }
        };
        List defaults = Arrays.asList(new String[]{"def1", "def2"});
        ArgumentImpl arg = new ArgumentImpl("myArg", "myDesc", 1, 2, '=', ';', dummyValidator, "++", defaults, 10);

        assertEquals("myArg", arg.getPreferredName());
        assertEquals("myDesc", arg.getDescription());
        assertEquals(1, arg.getMinimum());
        assertEquals(2, arg.getMaximum());
        assertEquals('=', arg.getInitialSeparator());
        assertEquals(';', arg.getSubsequentSeparator());
        assertEquals("++", arg.getConsumeRemaining());
        assertEquals(defaults, arg.getDefaultValues());
        assertEquals(dummyValidator, arg.getValidator());
        assertEquals(10, arg.getId());
        assertTrue(arg.isRequired());
        assertEquals(Collections.EMPTY_SET, arg.getPrefixes());
        assertEquals(Collections.EMPTY_SET, arg.getTriggers());
    }

    // Tests canProcess returns true
    @Test
    public void testCanProcess_anyArgument_returnsTrue() {
        WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(argument, new ArrayList());
        assertTrue(argument.canProcess(cmdLine, "any"));
        assertTrue(argument.canProcess(cmdLine, (String) null));
    }

    // Tests stripBoundaryQuotes with double quotes
    @Test
    public void testStripBoundaryQuotes_quotedString_stripsQuotes() {
        assertEquals("value", argument.stripBoundaryQuotes("\"value\""));
    }

    // Tests stripBoundaryQuotes without quotes
    @Test
    public void testStripBoundaryQuotes_unquotedString_returnsSame() {
        assertEquals("value", argument.stripBoundaryQuotes("value"));
        assertEquals("\"value", argument.stripBoundaryQuotes("\"value"));
        assertEquals("value\"", argument.stripBoundaryQuotes("value\""));
    }

    // Tests stripBoundaryQuotes with empty string and edge quote strings
    @Test
    public void testStripBoundaryQuotes_emptyAndShortQuotes() {
        assertEquals("", argument.stripBoundaryQuotes(""));
        assertEquals("", argument.stripBoundaryQuotes("\"\""));
    }

    // Tests processing normal values
    @Test
    public void testProcessValues_normalValues_addsToCommandLine() throws Exception {
        List argsList = new ArrayList(Arrays.asList(new String[]{"val1", "val2"}));
        ListIterator it = argsList.listIterator();
        WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(argument, new ArrayList());

        argument.process(cmdLine, it);

        List values = cmdLine.getValues(argument);
        assertEquals(2, values.size());
        assertEquals("val1", values.get(0));
        assertEquals("val2", values.get(1));
        assertFalse(it.hasNext());
    }

    // Tests consume remaining token
    @Test
    public void testProcessValues_consumeRemaining_addsRemainingValues() throws Exception {
        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 0, 5, '\0', '\0', null, "--", null, 0);
        List argsList = new ArrayList(Arrays.asList(new String[]{"--", "-opt1", "val1"}));
        ListIterator it = argsList.listIterator();
        WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(arg, new ArrayList());

        arg.processValues(cmdLine, it, arg);

        List values = cmdLine.getValues(arg);
        assertEquals(2, values.size());
        assertEquals("-opt1", values.get(0));
        assertEquals("val1", values.get(1));
    }

    // Tests stopping value processing when option is encountered
    @Test
    public void testProcessValues_looksLikeOption_stopsProcessing() throws Exception {
        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 0, 5, '\0', '\0', null, "--", null, 0);
        List argsList = new ArrayList(Arrays.asList(new String[]{"val1", "-opt"}));
        ListIterator it = argsList.listIterator();
        WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(arg, new ArrayList());

        arg.processValues(cmdLine, it, arg);

        List values = cmdLine.getValues(arg);
        assertEquals(1, values.size());
        assertEquals("val1", values.get(0));
        assertTrue(it.hasNext());
        assertEquals("-opt", it.next());
    }

    // Tests subsequent separator splitting
    @Test
    public void testProcessValues_subsequentSeparator_splitsTokens() throws Exception {
        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 0, 3, '\0', ',', null, "--", null, 0);
        List argsList = new ArrayList(Collections.singletonList("a,b,c"));
        ListIterator it = argsList.listIterator();
        WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(arg, new ArrayList());

        arg.processValues(cmdLine, it, arg);

        List values = cmdLine.getValues(arg);
        assertEquals(3, values.size());
        assertEquals("a", values.get(0));
        assertEquals("b", values.get(1));
        assertEquals("c", values.get(2));
    }

    // Tests subsequent separator splitting with excess tokens throwing OptionException
    @Test(expected = OptionException.class)
    public void testProcessValues_subsequentSeparatorTooManyTokens_throwsOptionException() throws Exception {
        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 0, 2, '\0', ',', null, "--", null, 0);
        List argsList = new ArrayList(Collections.singletonList("a,b,c"));
        ListIterator it = argsList.listIterator();
        WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(arg, new ArrayList());

        arg.processValues(cmdLine, it, arg);
    }

    // Tests validation when fewer values than minimum are present
    @Test(expected = OptionException.class)
    public void testValidate_missingValues_throwsOptionException() throws Exception {
        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 2, 4, '\0', '\0', null, "--", null, 0);
        WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(arg, new ArrayList());
        cmdLine.addValue(arg, "one");

        arg.validate(cmdLine);
    }

    // Tests validation when validator fails
    @Test(expected = OptionException.class)
    public void testValidate_validatorFails_throwsOptionException() throws Exception {
        Validator failingValidator = new Validator() {
            public void validate(List values) throws InvalidArgumentException {
                throw new InvalidArgumentException("Invalid argument");
            }
        };
        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 1, 2, '\0', '\0', failingValidator, "--", null, 0);
        WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(arg, new ArrayList());
        cmdLine.addValue(arg, "badValue");

        arg.validate(cmdLine);
    }

    // Tests appendUsage with optional and bracketed settings
    @Test
    public void testAppendUsage_optionalAndBracketed() {
        ArgumentImpl arg = new ArgumentImpl("file", "desc", 1, 3, '\0', '\0', null, "--", null, 0);
        Set helpSettings = new HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_OPTIONAL);
        helpSettings.add(DisplaySetting.DISPLAY_ARGUMENT_BRACKETED);
        helpSettings.add(DisplaySetting.DISPLAY_ARGUMENT_NUMBERED);

        StringBuffer buffer = new StringBuffer();
        arg.appendUsage(buffer, helpSettings, null);

        assertEquals("<file1> [<file2> [<file3>]]", buffer.toString());
    }

    // Tests appendUsage with infinite maximum
    @Test
    public void testAppendUsage_infiniteMaximum() {
        ArgumentImpl arg = new ArgumentImpl("file", "desc", 0, Integer.MAX_VALUE, '\0', '\0', null, "--", null, 0);
        Set helpSettings = new HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_OPTIONAL);

        StringBuffer buffer = new StringBuffer();
        arg.appendUsage(buffer, helpSettings, null);

        assertEquals("[file [file] ...]", buffer.toString());
    }

    // Tests helpLines generation
    @Test
    public void testHelpLines_returnsHelpLineList() {
        List lines = argument.helpLines(0, Collections.EMPTY_SET, null);
        assertNotNull(lines);
        assertEquals(1, lines.size());
        HelpLine line = (HelpLine) lines.get(0);
        assertEquals(argument, line.getOption());
        assertEquals(0, line.getIndent());
    }

    // Tests defaults method populates default values on command line
    @Test
    public void testDefaults_appliesDefaultValues() {
        List defaults = Arrays.asList(new String[]{"d1", "d2"});
        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 0, 2, '\0', '\0', null, "--", defaults, 0);
        WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(arg, new ArrayList());

        arg.defaults(cmdLine);

        List values = cmdLine.getValues(arg);
        assertEquals(defaults, values);
    }

    // Tests canProcess using ListIterator overload
    @Test
    public void testCanProcess_listIterator_returnsTrueWhenHasNext() {
        List argsList = Collections.singletonList("val");
        ListIterator it = argsList.listIterator();
        WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(argument, new ArrayList());
        assertTrue(argument.canProcess(cmdLine, it));
    }

    // Tests canProcess using ListIterator overload when empty
    @Test
    public void testCanProcess_listIteratorEmpty_returnsFalse() {
        List argsList = Collections.emptyList();
        ListIterator it = argsList.listIterator();
        WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(argument, new ArrayList());
        assertFalse(argument.canProcess(cmdLine, it));
    }

    // Tests single character quote string handling in stripBoundaryQuotes
    @Test
    public void testStripBoundaryQuotes_singleQuoteChar_returnsOriginal() {
        assertEquals("\"", argument.stripBoundaryQuotes("\""));
    }

    // Tests initial separator splitting in processValues
    @Test
    public void testProcessValues_initialSeparator_splitsToken() throws Exception {
        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 0, 2, '=', '\0', null, "--", null, 0);
        List argsList = new ArrayList(Collections.singletonList("key=value"));
        ListIterator it = argsList.listIterator();
        WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(arg, new ArrayList());

        arg.processValues(cmdLine, it, arg);

        List values = cmdLine.getValues(arg);
        assertEquals(1, values.size());
        assertEquals("value", values.get(0));
    }

    // Tests initial and subsequent separator combined in processValues
    @Test
    public void testProcessValues_initialAndSubsequentSeparator_splitsTokens() throws Exception {
        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 0, 3, '=', ',', null, "--", null, 0);
        List argsList = new ArrayList(Collections.singletonList("key=v1,v2"));
        ListIterator it = argsList.listIterator();
        WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(arg, new ArrayList());

        arg.processValues(cmdLine, it, arg);

        List values = cmdLine.getValues(arg);
        assertEquals(2, values.size());
        assertEquals("v1", values.get(0));
        assertEquals("v2", values.get(1));
    }

    // Tests validate method when validator passes successfully
    @Test
    public void testValidate_validatorPasses_noExceptionThrown() throws Exception {
        Validator passingValidator = new Validator() {
            public void validate(List values) throws InvalidArgumentException {
            }
        };
        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 1, 2, '\0', '\0', passingValidator, "--", null, 0);
        WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(arg, new ArrayList());
        cmdLine.addValue(arg, "validValue");

        arg.validate(cmdLine);
    }

    // Tests validate method when minimum is 0 and no values are provided
    @Test
    public void testValidate_zeroMinimumNoValues_noExceptionThrown() throws Exception {
        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 0, 2, '\0', '\0', null, "--", null, 0);
        WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(arg, new ArrayList());

        arg.validate(cmdLine);
    }

    // Tests appendUsage with fixed bounds without optional brackets
    @Test
    public void testAppendUsage_fixedBoundsWithoutOptional() {
        ArgumentImpl arg = new ArgumentImpl("param", "desc", 2, 2, '\0', '\0', null, "--", null, 0);
        Set helpSettings = new HashSet();

        StringBuffer buffer = new StringBuffer();
        arg.appendUsage(buffer, helpSettings, null);

        assertEquals("param1 param2", buffer.toString());
    }

    // Tests defaults method when defaultValues is null
    @Test
    public void testDefaults_nullDefaultValues_doesNothing() {
        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 0, 2, '\0', '\0', null, "--", null, 0);
        WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(arg, new ArrayList());

        arg.defaults(cmdLine);

        List values = cmdLine.getValues(arg);
        assertTrue(values.isEmpty());
    }

    // Tests checkPrefixes does not throw exception
    @Test
    public void testCheckPrefixes_emptyOrNonEmpty_doesNotThrow() {
        Set prefixes = new HashSet();
        prefixes.add("-");
        prefixes.add("--");
        argument.checkPrefixes(prefixes);
    }

    // Tests isRequired returns false when minimum is 0
    @Test
    public void testIsRequired_zeroMinimum_returnsFalse() {
        ArgumentImpl arg = new ArgumentImpl("arg", "desc", 0, 2, '\0', '\0', null, "--", null, 0);
        assertFalse(arg.isRequired());
    }
}