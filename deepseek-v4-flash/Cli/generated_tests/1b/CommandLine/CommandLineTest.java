package org.apache.commons.cli;

import static org.junit.Assert.*;
import java.util.Iterator;
import org.junit.Test;

public class CommandLineTest {

    private CommandLine createCommandLineWithOption(String shortOpt, String longOpt, String... values) {
        CommandLine cmd = new CommandLine();
        Option opt = new Option(shortOpt, longOpt, true, "desc");
        for (String val : values) {
            opt.addValue(val);
        }
        cmd.addOption(opt);
        return cmd;
    }

    // Tests hasOption returns false when option not added
    @Test
    public void testHasOption_NotAdded_ReturnsFalse() {
        CommandLine cmd = new CommandLine();
        assertFalse(cmd.hasOption("x"));
    }

    // Tests hasOption returns true for added option (both String and char)
    @Test
    public void testHasOption_Added_ReturnsTrue() {
        CommandLine cmd = createCommandLineWithOption("a", null, "value");
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption('a'));
    }

    // Tests hasOption with null key (HashMap accepts null)
    @Test
    public void testHasOption_NullOpt_ReturnsFalse() {
        CommandLine cmd = new CommandLine();
        assertFalse(cmd.hasOption((String) null));
    }

    // Tests getOptionValue returns null for absent option
    @Test
    public void testGetOptionValue_NotPresent_ReturnsNull() {
        CommandLine cmd = new CommandLine();
        assertNull(cmd.getOptionValue("b"));
    }

    // Tests getOptionValue returns correct value for present option (String)
    @Test
    public void testGetOptionValue_Present_ReturnsValue() {
        CommandLine cmd = createCommandLineWithOption("a", null, "testValue");
        assertEquals("testValue", cmd.getOptionValue("a"));
    }

    // Tests getOptionValue with char argument
    @Test
    public void testGetOptionValue_CharOption_ReturnsValue() {
        CommandLine cmd = createCommandLineWithOption("a", null, "charVal");
        assertEquals("charVal", cmd.getOptionValue('a'));
    }

    // Tests getOptionValue with default returns default when option absent
    @Test
    public void testGetOptionValue_Default_ReturnsDefaultWhenNotPresent() {
        CommandLine cmd = new CommandLine();
        assertEquals("default", cmd.getOptionValue("nonexist", "default"));
    }

    // Tests getOptionValue with default returns actual value when option present
    @Test
    public void testGetOptionValue_Default_ReturnsValueWhenPresent() {
        CommandLine cmd = createCommandLineWithOption("a", null, "actual");
        assertEquals("actual", cmd.getOptionValue("a", "default"));
    }

    // Tests getOptionValues returns null for absent option
    @Test
    public void testGetOptionValues_NotPresent_ReturnsNull() {
        CommandLine cmd = new CommandLine();
        assertNull(cmd.getOptionValues("b"));
    }

    // Tests getOptionValues returns array for present option (single value)
    @Test
    public void testGetOptionValues_Present_ReturnsValues() {
        CommandLine cmd = createCommandLineWithOption("a", null, "val1");
        String[] values = cmd.getOptionValues("a");
        assertNotNull(values);
        assertEquals(1, values.length);
        assertEquals("val1", values[0]);
    }

    // Tests getOptionValues returns all values when multiple values added
    @Test
    public void testGetOptionValues_MultipleValues_ReturnsAll() {
        CommandLine cmd = new CommandLine();
        Option opt = new Option("a", null, true, "desc");
        opt.addValue("v1");
        opt.addValue("v2");
        cmd.addOption(opt);
        String[] values = cmd.getOptionValues("a");
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals("v1", values[0]);
        assertEquals("v2", values[1]);
    }

    // Tests getOptionObject returns null for absent option
    @Test
    public void testGetOptionObject_NotPresent_ReturnsNull() {
        CommandLine cmd = new CommandLine();
        assertNull(cmd.getOptionObject("nonexisting"));
    }

    // Tests getOptionObject returns created object for present option
    @Test
    public void testGetOptionObject_Present_ReturnsObject() {
        CommandLine cmd = createCommandLineWithOption("a", null, "objVal");
        Object obj = cmd.getOptionObject("a");
        assertNotNull(obj);
        assertTrue(obj instanceof String);
        assertEquals("objVal", obj);
    }

    // Tests addArg then getArgs returns array of added arguments
    @Test
    public void testAddArg_ThenGetArgs_ReturnsArray() {
        CommandLine cmd = new CommandLine();
        cmd.addArg("arg1");
        cmd.addArg("arg2");
        assertArrayEquals(new String[]{"arg1", "arg2"}, cmd.getArgs());
    }

    // Tests addOption with both short and long can be retrieved by long name
    @Test
    public void testAddOption_ShortAndLong_GetValuesByLong_ReturnsValue() {
        CommandLine cmd = createCommandLineWithOption("s", "longopt", "shortLongVal");
        String[] values = cmd.getOptionValues("longopt");
        assertNotNull(values);
        assertEquals(1, values.length);
        assertEquals("shortLongVal", values[0]);
    }

    // Tests addOption with only long option can be retrieved by long name
    @Test
    public void testAddOption_OnlyLong_GetValuesByLong_ReturnsValue() {
        CommandLine cmd = new CommandLine();
        Option opt = new Option(null, "onlylong", true, "desc");
        opt.addValue("longOnlyVal");
        cmd.addOption(opt);
        String[] values = cmd.getOptionValues("onlylong");
        assertNotNull(values);
        assertEquals(1, values.length);
        assertEquals("longOnlyVal", values[0]);
    }

    // Tests getOptions returns array of all options
    @Test
    public void testGetOptions_ReturnsArray() {
        CommandLine cmd = createCommandLineWithOption("a", null, "v");
        Option[] opts = cmd.getOptions();
        assertEquals(1, opts.length);
        assertEquals("a", opts[0].getOpt());
    }

    // Tests iterator returns all options
    @Test
    public void testIterator_ReturnsAllOptions() {
        CommandLine cmd = createCommandLineWithOption("a", null, "v");
        Iterator iter = cmd.iterator();
        assertTrue(iter.hasNext());
        Option opt = (Option) iter.next();
        assertEquals("a", opt.getOpt());
        assertFalse(iter.hasNext());
    }

    // Tests getOptionValues strips leading hyphen for short option
    @Test
    public void testGetOptionValues_StripHyphens_ShortWithDash_ReturnsValue() {
        CommandLine cmd = createCommandLineWithOption("a", null, "hyphenVal");
        String[] values = cmd.getOptionValues("-a");
        assertNotNull(values);
        assertEquals("hyphenVal", values[0]);
    }

    // Tests getOptionValues strips leading double hyphen for long option
    @Test
    public void testGetOptionValues_StripHyphens_LongWithDoubleDash_ReturnsValue() {
        CommandLine cmd = createCommandLineWithOption("s", "longopt2", "doubleDashVal");
        String[] values = cmd.getOptionValues("--longopt2");
        assertNotNull(values);
        assertEquals("doubleDashVal", values[0]);
    }

    // Tests getOptionValues with null throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testGetOptionValues_NullOpt_ThrowsNullPointerException() {
        CommandLine cmd = new CommandLine();
        cmd.getOptionValues((String) null);
    }

    // ========== New tests for uncovered methods ==========

    // Tests getOptionValue with char option and default returns default when not present
    @Test
    public void testGetOptionValue_CharOptionDefault_ReturnsDefaultWhenNotPresent() {
        CommandLine cmd = new CommandLine();
        assertEquals("default", cmd.getOptionValue('z', "default"));
    }

    // Tests getOptionValue with char option and default returns actual value when present
    @Test
    public void testGetOptionValue_CharOptionDefault_ReturnsValueWhenPresent() {
        CommandLine cmd = createCommandLineWithOption("b", null, "actualChar");
        assertEquals("actualChar", cmd.getOptionValue('b', "default"));
    }

    // Tests getOptionValues with char option returns values for single value
    @Test
    public void testGetOptionValues_CharOption_ReturnsValues() {
        CommandLine cmd = createCommandLineWithOption("c", null, "valC");
        String[] values = cmd.getOptionValues('c');
        assertNotNull(values);
        assertEquals(1, values.length);
        assertEquals("valC", values[0]);
    }

    // Tests getOptionValues with char option returns multiple values
    @Test
    public void testGetOptionValues_CharOption_MultipleValues_ReturnsAll() {
        CommandLine cmd = new CommandLine();
        Option opt = new Option("d", null, true, "desc");
        opt.addValue("v1");
        opt.addValue("v2");
        cmd.addOption(opt);
        String[] values = cmd.getOptionValues('d');
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals("v1", values[0]);
        assertEquals("v2", values[1]);
    }

    // Tests getOptionObject with char option returns object
    @Test
    public void testGetOptionObject_CharOption_ReturnsObject() {
        CommandLine cmd = createCommandLineWithOption("e", null, "objChar");
        Object obj = cmd.getOptionObject('e');
        assertNotNull(obj);
        assertTrue(obj instanceof String);
        assertEquals("objChar", obj);
    }
}