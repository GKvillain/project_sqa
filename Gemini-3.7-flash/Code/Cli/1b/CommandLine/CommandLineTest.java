package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

public class CommandLineTest {

    private CommandLine cmd;

    @Before
    public void setUp() {
        cmd = new CommandLine();
    }

    // Tests hasOption with short option string when option is present
    @Test
    public void testHasOption_shortOptStringPresent_returnsTrue() {
        Option opt = new Option("a", "alpha", false, "Option a");
        cmd.addOption(opt);

        assertTrue(cmd.hasOption("a"));
    }

    // Tests hasOption with char when option is present
    @Test
    public void testHasOption_charOptPresent_returnsTrue() {
        Option opt = new Option("a", "alpha", false, "Option a");
        cmd.addOption(opt);

        assertTrue(cmd.hasOption('a'));
    }

    // Tests hasOption when option is not present
    @Test
    public void testHasOption_optNotPresent_returnsFalse() {
        assertFalse(cmd.hasOption("x"));
        assertFalse(cmd.hasOption('x'));
    }

    // Tests getOptionValue with short option string
    @Test
    public void testGetOptionValue_shortOptString_returnsValue() {
        Option opt = new Option("a", "alpha", true, "Option a");
        opt.addValue("value1");
        cmd.addOption(opt);

        assertEquals("value1", cmd.getOptionValue("a"));
    }

    // Tests getOptionValue with char option
    @Test
    public void testGetOptionValue_charOpt_returnsValue() {
        Option opt = new Option("b", "beta", true, "Option b");
        opt.addValue("valueB");
        cmd.addOption(opt);

        assertEquals("valueB", cmd.getOptionValue('b'));
    }

    // Tests getOptionValue returning null when option is not present or has no value
    @Test
    public void testGetOptionValue_notPresent_returnsNull() {
        assertNull(cmd.getOptionValue("nonexistent"));
        assertNull(cmd.getOptionValue('z'));
    }

    // Tests getOptionValue with default value when option is present vs not present
    @Test
    public void testGetOptionValue_withDefaultValue_returnsCorrectValue() {
        Option opt = new Option("c", "gamma", true, "Option c");
        opt.addValue("custom");
        cmd.addOption(opt);

        assertEquals("custom", cmd.getOptionValue("c", "default"));
        assertEquals("default", cmd.getOptionValue("notset", "default"));
        assertEquals("custom", cmd.getOptionValue('c', "default"));
        assertEquals("default", cmd.getOptionValue('z', "default"));
    }

    // Tests getOptionValues with multiple values on an option
    @Test
    public void testGetOptionValues_multipleValues_returnsAllValues() {
        Option opt = new Option("m", "multi", true, "Option m");
        opt.addValue("val1");
        opt.addValue("val2");
        cmd.addOption(opt);

        String[] values = cmd.getOptionValues("m");
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals("val1", values[0]);
        assertEquals("val2", values[1]);

        String[] charValues = cmd.getOptionValues('m');
        assertNotNull(charValues);
        assertEquals(2, charValues.length);
        assertEquals("val1", charValues[0]);
        assertEquals("val2", charValues[1]);
    }

    // Tests getOptionValues with long option and leading hyphens
    @Test
    public void testGetOptionValues_longOptAndLeadingHyphens_returnsValues() {
        Option opt = new Option("f", "file", true, "File option");
        opt.addValue("output.txt");
        cmd.addOption(opt);

        String[] valuesByLongOpt = cmd.getOptionValues("file");
        assertNotNull(valuesByLongOpt);
        assertEquals(1, valuesByLongOpt.length);
        assertEquals("output.txt", valuesByLongOpt[0]);

        String[] valuesWithHyphen = cmd.getOptionValues("-f");
        assertNotNull(valuesWithHyphen);
        assertEquals("output.txt", valuesWithHyphen[0]);

        String[] valuesWithDoubleHyphen = cmd.getOptionValues("--file");
        assertNotNull(valuesWithDoubleHyphen);
        assertEquals("output.txt", valuesWithDoubleHyphen[0]);
    }

    // Tests getOptionValues returning null when option does not exist
    @Test
    public void testGetOptionValues_notPresent_returnsNull() {
        assertNull(cmd.getOptionValues("unknown"));
        assertNull(cmd.getOptionValues('u'));
    }

    // Tests getOptionObject when option is not present
    @Test
    public void testGetOptionObject_notPresent_returnsNull() {
        assertNull(cmd.getOptionObject("absent"));
        assertNull(cmd.getOptionObject('x'));
    }

    // Tests getOptionObject with String type
    @Test
    public void testGetOptionObject_stringType_returnsParsedObject() {
        Option opt = new Option("s", "stringOpt", true, "String option");
        opt.addValue("hello");
        opt.setType(PatternOptionBuilder.STRING_VALUE);
        cmd.addOption(opt);

        Object resultStr = cmd.getOptionObject("s");
        assertEquals("hello", resultStr);

        Object resultChar = cmd.getOptionObject('s');
        assertEquals("hello", resultChar);
    }

    // Tests getOptionObject when option has no value
    @Test
    public void testGetOptionObject_noValue_returnsNull() {
        Option opt = new Option("n", "noValue", false, "No value option");
        cmd.addOption(opt);

        assertNull(cmd.getOptionObject("n"));
    }

    // Tests addArg, getArgs, and getArgList
    @Test
    public void testArgs_addAndRetrieve_returnsExpectedArguments() {
        cmd.addArg("arg1");
        cmd.addArg("arg2");

        String[] argsArray = cmd.getArgs();
        assertNotNull(argsArray);
        assertEquals(2, argsArray.length);
        assertEquals("arg1", argsArray[0]);
        assertEquals("arg2", argsArray[1]);

        List argList = cmd.getArgList();
        assertNotNull(argList);
        assertEquals(2, argList.size());
        assertEquals("arg1", argList.get(0));
        assertEquals("arg2", argList.get(1));
    }

    // Tests getOptions and iterator methods
    @Test
    public void testGetOptionsAndIterator_multipleOptions_returnsOptions() {
        Option opt1 = new Option("a", "alpha", false, "Option A");
        Option opt2 = new Option("b", "beta", false, "Option B");

        cmd.addOption(opt1);
        cmd.addOption(opt2);

        Option[] optionsArray = cmd.getOptions();
        assertNotNull(optionsArray);
        assertEquals(2, optionsArray.length);

        Iterator it = cmd.iterator();
        assertNotNull(it);
        int count = 0;
        while (it.hasNext()) {
            assertNotNull(it.next());
            count++;
        }
        assertEquals(2, count);
    }

    // Tests addOption with Option having only longOpt (no short opt)
    @Test
    public void testAddOption_longOptOnly_storesAndRetrievesCorrectly() {
        Option opt = new Option(null, "onlyLong", true, "Only long option");
        opt.addValue("valLong");
        cmd.addOption(opt);

        assertTrue(cmd.hasOption("onlyLong"));
        assertEquals("valLong", cmd.getOptionValue("onlyLong"));
        assertNotNull(cmd.getOptionValues("onlyLong"));
    }

    // Tests getOptionProperties when option is not present
    @Test
    public void testGetOptionProperties_notPresent_returnsEmptyProperties() {
        Properties props = cmd.getOptionProperties("D");
        assertNotNull(props);
        assertTrue(props.isEmpty());
    }

    // Tests getOptionProperties with key-value pairs
    @Test
    public void testGetOptionProperties_propertyOption_returnsProperties() {
        Option opt = new Option("D", "property", true, "Property option");
        opt.setArgs(2);
        opt.setValueSeparator('=');
        opt.addValue("key1");
        opt.addValue("value1");
        cmd.addOption(opt);

        Option opt2 = new Option("D", "property", true, "Property option");
        opt2.setArgs(2);
        opt2.setValueSeparator('=');
        opt2.addValue("key2");
        opt2.addValue("value2");
        cmd.addOption(opt2);

        Properties props = cmd.getOptionProperties("D");
        assertNotNull(props);
        assertEquals(2, props.size());
        assertEquals("value1", props.getProperty("key1"));
        assertEquals("value2", props.getProperty("key2"));
    }

    // Tests getOptionProperties with single value (unary property)
    @Test
    public void testGetOptionProperties_singleValue_returnsTrueValue() {
        Option opt = new Option("D", "property", true, "Property option");
        opt.addValue("flagKey");
        cmd.addOption(opt);

        Properties props = cmd.getOptionProperties("D");
        assertNotNull(props);
        assertEquals(1, props.size());
        assertEquals("true", props.getProperty("flagKey"));
    }

    // Tests getOptionProperties with multiple values on same option instance
    @Test
    public void testGetOptionProperties_multiplePairsInSingleOption_returnsProperties() {
        Option opt = new Option("D", "property", true, "Property option");
        opt.setArgs(Option.UNLIMITED_VALUES);
        opt.addValue("k1");
        opt.addValue("v1");
        opt.addValue("k2");
        opt.addValue("v2");
        cmd.addOption(opt);

        Properties props = cmd.getOptionProperties("D");
        assertNotNull(props);
        assertEquals(2, props.size());
        assertEquals("v1", props.getProperty("k1"));
        assertEquals("v2", props.getProperty("k2"));
    }

    // Tests getOptionObject with non-string types such as Number and File
    @Test
    public void testGetOptionObject_numberAndFileType_returnsParsedObjects() {
        Option numOpt = new Option("n", "num", true, "Number opt");
        numOpt.addValue("123");
        numOpt.setType(PatternOptionBuilder.NUMBER_VALUE);
        cmd.addOption(numOpt);

        Object numResult = cmd.getOptionObject("n");
        assertNotNull(numResult);
        assertTrue(numResult instanceof Number);
        assertEquals(123L, ((Number) numResult).longValue());

        Option fileOpt = new Option("f", "file", true, "File opt");
        fileOpt.addValue("test.txt");
        fileOpt.setType(PatternOptionBuilder.FILE_VALUE);
        cmd.addOption(fileOpt);

        Object fileResult = cmd.getOptionObject("f");
        assertNotNull(fileResult);
        assertTrue(fileResult instanceof File);
        assertEquals("test.txt", ((File) fileResult).getName());
    }

    // Tests getOptionObject with invalid value throws ParseException internally and returns null
    @Test
    public void testGetOptionObject_invalidValue_returnsNull() {
        Option opt = new Option("c", "class", true, "Class opt");
        opt.addValue("non.existent.ClassName12345");
        opt.setType(PatternOptionBuilder.CLASS_VALUE);
        cmd.addOption(opt);

        assertNull(cmd.getOptionObject("c"));
    }
}