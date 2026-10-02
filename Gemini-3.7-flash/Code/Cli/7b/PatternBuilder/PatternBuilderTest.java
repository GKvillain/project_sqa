package org.apache.commons.cli2.builder;

import org.apache.commons.cli2.Option;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PatternBuilderTest {

    private PatternBuilder builder;

    @Before
    public void setUp() {
        builder = new PatternBuilder();
    }

    // Tests custom constructor initialization
    @Test
    public void testConstructor_customBuilders_initializesProperly() {
        GroupBuilder gb = new GroupBuilder();
        DefaultOptionBuilder ob = new DefaultOptionBuilder();
        ArgumentBuilder ab = new ArgumentBuilder();
        PatternBuilder customBuilder = new PatternBuilder(gb, ob, ab);
        customBuilder.withPattern("a");
        Option option = customBuilder.create();
        assertNotNull(option);
        assertEquals("-a", option.getPreferredName());
    }

    // Tests single simple flag option without argument
    @Test
    public void testWithPattern_simpleFlag_createsSingleOption() {
        builder.withPattern("a");
        Option option = builder.create();
        assertNotNull(option);
        assertEquals("-a", option.getPreferredName());
        assertFalse(option.isRequired());
    }

    // Tests required flag option
    @Test
    public void testWithPattern_requiredFlag_createsRequiredOption() {
        builder.withPattern("a!");
        Option option = builder.create();
        assertNotNull(option);
        assertEquals("-a", option.getPreferredName());
        assertTrue(option.isRequired());
    }

    // Tests string argument option (':')
    @Test
    public void testWithPattern_stringArgument_createsOptionWithArgument() {
        builder.withPattern("a:");
        Option option = builder.create();
        assertNotNull(option);
        assertEquals("-a", option.getPreferredName());
        assertFalse(option.isRequired());
    }

    // Tests class instance argument option ('@')
    @Test
    public void testWithPattern_classInstanceArgument_createsOption() {
        builder.withPattern("c@");
        Option option = builder.create();
        assertNotNull(option);
        assertEquals("-c", option.getPreferredName());
    }

    // Tests class argument option ('+')
    @Test
    public void testWithPattern_classArgument_createsOption() {
        builder.withPattern("c+");
        Option option = builder.create();
        assertNotNull(option);
        assertEquals("-c", option.getPreferredName());
    }

    // Tests number argument option ('%')
    @Test
    public void testWithPattern_numberArgument_createsOption() {
        builder.withPattern("n%");
        Option option = builder.create();
        assertNotNull(option);
        assertEquals("-n", option.getPreferredName());
    }

    // Tests date argument option ('#')
    @Test
    public void testWithPattern_dateArgument_createsOption() {
        builder.withPattern("d#");
        Option option = builder.create();
        assertNotNull(option);
        assertEquals("-d", option.getPreferredName());
    }

    // Tests existing file argument option ('<')
    @Test
    public void testWithPattern_existingFileArgument_createsOption() {
        builder.withPattern("f<");
        Option option = builder.create();
        assertNotNull(option);
        assertEquals("-f", option.getPreferredName());
    }

    // Tests file argument option ('>')
    @Test
    public void testWithPattern_fileArgument_createsOption() {
        builder.withPattern("f>");
        Option option = builder.create();
        assertNotNull(option);
        assertEquals("-f", option.getPreferredName());
    }

    // Tests multiple files argument option ('*')
    @Test
    public void testWithPattern_multipleFilesArgument_createsOption() {
        builder.withPattern("m*");
        Option option = builder.create();
        assertNotNull(option);
        assertEquals("-m", option.getPreferredName());
    }

    // Tests URL argument option ('/')
    @Test
    public void testWithPattern_urlArgument_createsOption() {
        builder.withPattern("u/");
        Option option = builder.create();
        assertNotNull(option);
        assertEquals("-u", option.getPreferredName());
    }

    // Tests required argument option
    @Test
    public void testWithPattern_requiredArgument_createsRequiredOptionWithArgument() {
        builder.withPattern("r!:");
        Option option = builder.create();
        assertNotNull(option);
        assertEquals("-r", option.getPreferredName());
        assertTrue(option.isRequired());
    }

    // Tests multiple options in single pattern leading to group option
    @Test
    public void testWithPattern_multipleOptions_createsGroup() {
        builder.withPattern("ab:c!");
        Option group = builder.create();
        assertNotNull(group);
        assertTrue(group.getTriggers().contains("-a"));
        assertTrue(group.getTriggers().contains("-b"));
        assertTrue(group.getTriggers().contains("-c"));
    }

    // Tests empty pattern string
    @Test
    public void testWithPattern_emptyPattern_createsEmptyGroup() {
        builder.withPattern("");
        Option option = builder.create();
        assertNotNull(option);
    }

    // Tests reset method clearing state
    @Test
    public void testReset_clearsBuilderState() {
        builder.withPattern("a");
        builder.reset();
        Option option = builder.create();
        assertNotNull(option);
        assertFalse(option.getTriggers().contains("-a"));
    }

    // Tests chaining multiple withPattern calls before create
    @Test
    public void testWithPattern_chainedCalls_accumulatesOptions() {
        builder.withPattern("a").withPattern("b:");
        Option group = builder.create();
        assertNotNull(group);
        assertTrue(group.getTriggers().contains("-a"));
        assertTrue(group.getTriggers().contains("-b"));
    }
}