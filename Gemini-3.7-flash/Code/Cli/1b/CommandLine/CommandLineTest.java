package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link CommandLine}.
 */
public class CommandLineTest {

    private CommandLine cmd;

    @Before
    public void setUp() {
        cmd = new CommandLine();
    }

    // Tests querying an existing option by short string name
    @Test
    public void testHasOption_existingShortOpt_returnsTrue() {
        Option opt = new Option("a", "alpha", false, "Alpha option");
        cmd.addOption(opt);

        assertTrue(cmd.hasOption("a"));
    }

    // Tests querying a non-existing option by string name
    @Test
    public void testHasOption_nonExistingOpt_returnsFalse() {
        assertFalse(cmd.hasOption("nonexisting"));
    }

    // Tests querying an existing option by character
    @Test
    public void testHasOption_charExistingOpt_returnsTrue() {
        Option opt = new Option("c", "charlie", false, "Charlie option");
        cmd.addOption(opt);

        assertTrue(cmd.hasOption('c'));
    }

    // Tests querying a non-existing option by character
    @Test
    public void testHasOption_charNonExistingOpt_returnsFalse() {
        assertFalse(cmd.hasOption('z'));
    }

    // Tests retrieving single value for existing option
    @Test
    public void testGetOptionValue_existingOptWithValue_returnsValue() {
        Option opt = new Option("f", "file", true, "File option");
        opt.addValue("test.txt");
        cmd.addOption(opt);

        assertEquals("test.txt", cmd.getOptionValue("f"));
    }

    // Tests retrieving single value when option exists but has no values
    @Test
    public void testGetOptionValue_existingOptWithoutValue_returnsNull() {
        Option opt = new Option("n", "none", false, "No arg option");
        cmd.addOption(opt);

        assertNull(cmd.getOptionValue("n"));
    }

    // Tests retrieving value for non-existing option
    @Test
    public void testGetOptionValue_nonExistingOpt_returnsNull() {
        assertNull(cmd.getOptionValue("unknown"));
    }

    // Tests retrieving single value by character
    @Test
    public void testGetOptionValue_charExistingOpt_returnsValue() {
        Option opt = new Option("v", true, "Version");
        opt.addValue("1.0.0");
        cmd.addOption(opt);

        assertEquals("1.0.0", cmd.getOptionValue('v'));
    }

    // Tests retrieving single value by character for non-existing option
    @Test
    public void testGetOptionValue_charNonExistingOpt_returnsNull() {
        assertNull(cmd.getOptionValue('x'));
    }

    // Tests retrieving option value with default fallback (String overload)
    @Test
    public void testGetOptionValue_withDefaultValueString_returnsCorrectValue() {
        Option opt = new Option("p", true, "Port");
        opt.addValue("8080");
        cmd.addOption(opt);

        assertEquals("8080", cmd.getOptionValue("p", "9090"));
        assertEquals("defaultPort", cmd.getOptionValue("missing", "defaultPort"));
    }

    // Tests retrieving option value with default fallback (char overload)
    @Test
    public void testGetOptionValue_withDefaultValueChar_returnsCorrectValue() {
        Option opt = new Option("h", true, "Host");
        opt.addValue("localhost");
        cmd.addOption(opt);

        assertEquals("localhost", cmd.getOptionValue('h', "127.0.0.1"));
        assertEquals("defaultHost", cmd.getOptionValue('m', "defaultHost"));
    }

    // Tests retrieving multiple values for an option
    @Test
    public void testGetOptionValues_multipleValues_returnsAllValues() {
        Option opt = new Option("m", "multi", true, "Multiple values");
        opt.addValue("val1");
        opt.addValue("val2");
        opt.addValue("val3");
        cmd.addOption(opt);

        String[] expected = new String[]{"val1", "val2", "val3"};
        assertArrayEquals(expected, cmd.getOptionValues("m"));
    }

    // Tests retrieving values with hyphens and long option name
    @Test
    public void testGetOptionValues_withHyphensAndLongOpt_returnsValues() {
        Option opt = new Option("s", "server", true, "Server option");
        opt.addValue("srv1");
        cmd.addOption(opt);

        assertArrayEquals(new String[]{"srv1"}, cmd.getOptionValues("-s"));
        assertArrayEquals(new String[]{"srv1"}, cmd.getOptionValues("--server"));
        assertArrayEquals(new String[]{"srv1"}, cmd.getOptionValues("server"));
    }

    // Tests retrieving values by character
    @Test
    public void testGetOptionValues_charExistingOpt_returnsValues() {
        Option opt = new Option("k", true, "Key");
        opt.addValue("secret");
        cmd.addOption(opt);

        assertArrayEquals(new String[]{"secret"}, cmd.getOptionValues('k'));
    }

    // Tests retrieving values for non-existing option
    @Test
    public void testGetOptionValues_nonExistingOpt_returnsNull() {
        assertNull(cmd.getOptionValues("nonexistent"));
        assertNull(cmd.getOptionValues('z'));
    }

    // Tests getOptionObject with valid type and value
    @Test
    public void testGetOptionObject_validType_returnsConvertedObject() {
        Option opt = new Option("n", true, "Number");
        opt.addValue("123");
        opt.setType(PatternOptionBuilder.NUMBER_VALUE);
        cmd.addOption(opt);

        Object obj = cmd.getOptionObject("n");
        assertNotNull(obj);
        assertEquals(new Long(123), obj);
    }

    // Tests getOptionObject using character overload
    @Test
    public void testGetOptionObject_charOpt_returnsConvertedObject() {
        Option opt = new Option("n", true, "Number");
        opt.addValue("456");
        opt.setType(PatternOptionBuilder.NUMBER_VALUE);
        cmd.addOption(opt);

        Object obj = cmd.getOptionObject('n');
        assertNotNull(obj);
        assertEquals(new Long(456), obj);
    }

    // Tests getOptionObject when option does not exist
    @Test
    public void testGetOptionObject_nonExistingOpt_returnsNull() {
        assertNull(cmd.getOptionObject("nonexistent"));
        assertNull(cmd.getOptionObject('u'));
    }

    // Tests getOptionObject when option has no value
    @Test
    public void testGetOptionObject_optWithoutValue_returnsNull() {
        Option opt = new Option("x", false, "Flag");
        opt.setType(PatternOptionBuilder.STRING_VALUE);
        cmd.addOption(opt);

        assertNull(cmd.getOptionObject("x"));
    }

    // Tests adding and retrieving unrecognized arguments
    @Test
    public void testAddArg_getArgsAndGetArgList_returnsArguments() {
        assertEquals(0, cmd.getArgs().length);
        assertEquals(0, cmd.getArgList().size());

        cmd.addArg("arg1");
        cmd.addArg("arg2");

        String[] args = cmd.getArgs();
        assertArrayEquals(new String[]{"arg1", "arg2"}, args);

        List argList = cmd.getArgList();
        assertEquals(2, argList.size());
        assertEquals("arg1", argList.get(0));
        assertEquals("arg2", argList.get(1));
    }

    // Tests iterator over added options
    @Test
    public void testIterator_optionsPresent_iteratesOptions() {
        Option opt1 = new Option("a", "Option A");
        Option opt2 = new Option("b", "Option B");
        cmd.addOption(opt1);
        cmd.addOption(opt2);

        Iterator it = cmd.iterator();
        assertNotNull(it);
        int count = 0;
        while (it.hasNext()) {
            assertNotNull(it.next());
            count++;
        }
        assertEquals(2, count);
    }

    // Tests getOptions returning array of processed options
    @Test
    public void testGetOptions_optionsPresent_returnsOptionsArray() {
        Option opt1 = new Option("a", "Option A");
        Option opt2 = new Option("b", "Option B");
        cmd.addOption(opt1);
        cmd.addOption(opt2);

        Option[] options = cmd.getOptions();
        assertNotNull(options);
        assertEquals(2, options.length);
    }

    // Tests adding an option with only long opt (null key branch)
    @Test
    public void testAddOption_longOptOnly_storedSuccessfully() {
        Option opt = new Option(null, "verbose", false, "Verbose output");
        cmd.addOption(opt);

        assertTrue(cmd.hasOption("verbose"));
        Option[] options = cmd.getOptions();
        assertEquals(1, options.length);
        assertEquals("verbose", options[0].getLongOpt());
    }

    // Tests getOptionProperties when option has key and value
    @Test
    public void testGetOptionProperties_twoValues_returnsKeyValueProperty() {
        Option opt = new Option("D", "property", true, "Property option");
        opt.addValue("property.name");
        opt.addValue("property.value");
        cmd.addOption(opt);

        Properties props = cmd.getOptionProperties("D");
        assertNotNull(props);
        assertEquals(1, props.size());
        assertEquals("property.value", props.getProperty("property.name"));
    }

    // Tests getOptionProperties when option has single value (defaults to "true")
    @Test
    public void testGetOptionProperties_oneValue_returnsKeyWithTrue() {
        Option opt = new Option("D", "property", true, "Property option");
        opt.addValue("flag.name");
        cmd.addOption(opt);

        Properties props = cmd.getOptionProperties("D");
        assertNotNull(props);
        assertEquals(1, props.size());
        assertEquals("true", props.getProperty("flag.name"));
    }

    // Tests getOptionProperties by long opt name
    @Test
    public void testGetOptionProperties_byLongOpt_returnsProperties() {
        Option opt = new Option("D", "define", true, "Define property");
        opt.addValue("key");
        opt.addValue("val");
        cmd.addOption(opt);

        Properties props = cmd.getOptionProperties("define");
        assertNotNull(props);
        assertEquals("val", props.getProperty("key"));
    }

    // Tests getOptionProperties on non-existing option returns empty Properties
    @Test
    public void testGetOptionProperties_nonExistingOpt_returnsEmptyProperties() {
        Properties props = cmd.getOptionProperties("unknown");
        assertNotNull(props);
        assertTrue(props.isEmpty());
    }

    // Tests getOptionObject with conversion failure returning null
    @Test
    public void testGetOptionObject_invalidValueConversion_returnsNull() {
        Option opt = new Option("n", true, "Number");
        opt.addValue("not-a-number");
        opt.setType(PatternOptionBuilder.NUMBER_VALUE);
        cmd.addOption(opt);

        assertNull(cmd.getOptionObject("n"));
    }
}