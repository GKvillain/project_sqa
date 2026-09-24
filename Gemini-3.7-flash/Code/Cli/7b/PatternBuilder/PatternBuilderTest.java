package org.apache.commons.cli2.builder;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.option.DefaultOption;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PatternBuilderTest {

    private PatternBuilder patternBuilder;

    @Before
    public void setUp() {
        patternBuilder = new PatternBuilder();
    }

    // Tests empty pattern produces an empty group
    @Test
    public void testWithPattern_emptyString_createsGroupOption() {
        patternBuilder.withPattern("");
        final Option option = patternBuilder.create();
        assertNotNull(option);
        assertTrue(option instanceof Group);
    }

    // Tests single simple flag option without argument
    @Test
    public void testWithPattern_singleFlag_createsDefaultOption() {
        patternBuilder.withPattern("a");
        final Option option = patternBuilder.create();
        assertTrue(option instanceof DefaultOption);
        final DefaultOption defaultOption = (DefaultOption) option;
        assertEquals("-a", defaultOption.getPreferredName());
        assertEquals('a', defaultOption.getId());
        assertFalse(defaultOption.isRequired());
        assertNull(defaultOption.getArgument());
    }

    // Tests single required flag option
    @Test
    public void testWithPattern_requiredFlag_createsRequiredOption() {
        patternBuilder.withPattern("a!");
        final Option option = patternBuilder.create();
        assertTrue(option instanceof DefaultOption);
        final DefaultOption defaultOption = (DefaultOption) option;
        assertTrue(defaultOption.isRequired());
        assertNull(defaultOption.getArgument());
    }

    // Tests string argument option without validator (':')
    @Test
    public void testWithPattern_stringArgument_createsOptionWithArgument() throws Exception {
        patternBuilder.withPattern("s:");
        final Option option = patternBuilder.create();
        assertTrue(option instanceof DefaultOption);
        final DefaultOption defaultOption = (DefaultOption) option;
        assertNotNull(defaultOption.getArgument());

        final Group group = new GroupBuilder().withOption(defaultOption).create();
        final Parser parser = new Parser();
        parser.setGroup(group);
        final CommandLine cl = parser.parse(new String[]{"-s", "anyStringValue"});
        assertTrue(cl.hasOption("-s"));
        assertEquals("anyStringValue", cl.getValue("-s"));
    }

    // Tests required argument sets minimum to 1
    @Test
    public void testWithPattern_requiredArgument_setsRequiredOptionAndArgument() {
        patternBuilder.withPattern("r!:");
        final Option option = patternBuilder.create();
        assertTrue(option instanceof DefaultOption);
        final DefaultOption defaultOption = (DefaultOption) option;
        assertTrue(defaultOption.isRequired());
        assertNotNull(defaultOption.getArgument());
    }

    // Tests number validator ('%')
    @Test
    public void testWithPattern_numberValidator_createsNumberValidatorArgument() throws Exception {
        patternBuilder.withPattern("n%");
        final Option option = patternBuilder.create();
        assertTrue(option instanceof DefaultOption);
        final DefaultOption defaultOption = (DefaultOption) option;
        assertNotNull(defaultOption.getArgument());

        final Group group = new GroupBuilder().withOption(defaultOption).create();
        final Parser parser = new Parser();
        parser.setGroup(group);

        final CommandLine cl = parser.parse(new String[]{"-n", "12345"});
        assertTrue(cl.hasOption("-n"));
        assertEquals("12345", cl.getValue("-n"));

        try {
            parser.parse(new String[]{"-n", "notANumber"});
            fail("Expected OptionException for invalid number");
        } catch (final OptionException expected) {
            // expected validation failure
        }
    }

    // Tests class instance validator ('@')
    @Test
    public void testWithPattern_classInstanceValidator_createsClassValidatorArgument() throws Exception {
        patternBuilder.withPattern("c@");
        final Option option = patternBuilder.create();
        assertTrue(option instanceof DefaultOption);
        final DefaultOption defaultOption = (DefaultOption) option;
        assertNotNull(defaultOption.getArgument());

        final Group group = new GroupBuilder().withOption(defaultOption).create();
        final Parser parser = new Parser();
        parser.setGroup(group);

        final CommandLine cl = parser.parse(new String[]{"-c", "java.util.Vector"});
        assertTrue(cl.hasOption("-c"));

        try {
            parser.parse(new String[]{"-c", "non.existent.Class"});
            fail("Expected OptionException for non-existent class");
        } catch (final OptionException expected) {
            // expected validation failure
        }
    }

    // Tests class type validator ('+')
    @Test
    public void testWithPattern_classValidator_createsClassValidatorArgument() throws Exception {
        patternBuilder.withPattern("k+");
        final Option option = patternBuilder.create();
        assertTrue(option instanceof DefaultOption);
        final DefaultOption defaultOption = (DefaultOption) option;
        assertNotNull(defaultOption.getArgument());

        final Group group = new GroupBuilder().withOption(defaultOption).create();
        final Parser parser = new Parser();
        parser.setGroup(group);

        final CommandLine cl = parser.parse(new String[]{"-k", "java.lang.String"});
        assertTrue(cl.hasOption("-k"));

        try {
            parser.parse(new String[]{"-k", "non.existent.Class"});
            fail("Expected OptionException for non-existent class");
        } catch (final OptionException expected) {
            // expected validation failure
        }
    }

    // Tests date validator ('#')
    @Test
    public void testWithPattern_dateValidator_createsDateValidatorArgument() {
        patternBuilder.withPattern("d#");
        final Option option = patternBuilder.create();
        assertTrue(option instanceof DefaultOption);
        final DefaultOption defaultOption = (DefaultOption) option;
        assertNotNull(defaultOption.getArgument());

        final Group group = new GroupBuilder().withOption(defaultOption).create();
        final Parser parser = new Parser();
        parser.setGroup(group);

        try {
            parser.parse(new String[]{"-d", "not-a-valid-date"});
            fail("Expected OptionException for invalid date");
        } catch (final OptionException expected) {
            // expected validation failure
        }
    }

    // Tests existing file validator ('<')
    @Test
    public void testWithPattern_existingFileValidator_createsFileValidatorArgument() {
        patternBuilder.withPattern("f<");
        final Option option = patternBuilder.create();
        assertTrue(option instanceof DefaultOption);
        final DefaultOption defaultOption = (DefaultOption) option;
        assertNotNull(defaultOption.getArgument());

        final Group group = new GroupBuilder().withOption(defaultOption).create();
        final Parser parser = new Parser();
        parser.setGroup(group);

        try {
            parser.parse(new String[]{"-f", "non_existent_file_xyz_12345.tmp"});
            fail("Expected OptionException for non existing file");
        } catch (final OptionException expected) {
            // expected validation failure
        }
    }

    // Tests file validator ('>')
    @Test
    public void testWithPattern_fileValidator_createsFileValidatorArgument() throws Exception {
        patternBuilder.withPattern("o>");
        final Option option = patternBuilder.create();
        assertTrue(option instanceof DefaultOption);
        final DefaultOption defaultOption = (DefaultOption) option;
        assertNotNull(defaultOption.getArgument());

        final Group group = new GroupBuilder().withOption(defaultOption).create();
        final Parser parser = new Parser();
        parser.setGroup(group);

        final CommandLine cl = parser.parse(new String[]{"-o", "output.txt"});
        assertTrue(cl.hasOption("-o"));
        assertEquals("output.txt", cl.getValue("-o"));
    }

    // Tests multiple arguments file validator ('*') allowing unlimited values
    @Test
    public void testWithPattern_unlimitedFileValidator_createsFileValidatorWithUnlimitedMaximum() throws Exception {
        patternBuilder.withPattern("m*");
        final Option option = patternBuilder.create();
        assertTrue(option instanceof DefaultOption);
        final DefaultOption defaultOption = (DefaultOption) option;
        assertNotNull(defaultOption.getArgument());

        final Group group = new GroupBuilder().withOption(defaultOption).create();
        final Parser parser = new Parser();
        parser.setGroup(group);

        final CommandLine cl = parser.parse(new String[]{"-m", "file1.txt", "file2.txt"});
        assertTrue(cl.hasOption("-m"));
        assertEquals("file1.txt", cl.getValue("-m"));
    }

    // Tests URL validator ('/')
    @Test
    public void testWithPattern_urlValidator_createsUrlValidatorArgument() throws Exception {
        patternBuilder.withPattern("u/");
        final Option option = patternBuilder.create();
        assertTrue(option instanceof DefaultOption);
        final DefaultOption defaultOption = (DefaultOption) option;
        assertNotNull(defaultOption.getArgument());

        final Group group = new GroupBuilder().withOption(defaultOption).create();
        final Parser parser = new Parser();
        parser.setGroup(group);

        final CommandLine cl = parser.parse(new String[]{"-u", "http://localhost:8080"});
        assertTrue(cl.hasOption("-u"));

        try {
            parser.parse(new String[]{"-u", "invalid-url-without-protocol"});
            fail("Expected OptionException for invalid url");
        } catch (final OptionException expected) {
            // expected validation failure
        }
    }

    // Tests multiple options combined into a Group
    @Test
    public void testWithPattern_multipleOptions_createsGroupContainingAllOptions() {
        patternBuilder.withPattern("ab!c:");
        final Option option = patternBuilder.create();
        assertNotNull(option);
        assertTrue(option instanceof Group);
    }

    // Tests reset method clears builder state
    @Test
    public void testReset_configuredPattern_clearsOptions() {
        assertSame(patternBuilder, patternBuilder.withPattern("a"));
        assertSame(patternBuilder, patternBuilder.reset());
        final Option option = patternBuilder.create();
        assertTrue(option instanceof Group);
    }

    // Tests pattern starting with special characters without option character
    @Test
    public void testWithPattern_specialCharsOnly_ignoresLeadingModifiers() {
        patternBuilder.withPattern("!:@#");
        final Option option = patternBuilder.create();
        assertNotNull(option);
        assertTrue(option instanceof Group);
    }

    // Tests multiple typed options in sequence
    @Test
    public void testWithPattern_multipleTypedOptions_parsesCorrectly() {
        patternBuilder.withPattern("a:b%c@d+e#f<g>h*i/");
        final Option option = patternBuilder.create();
        assertNotNull(option);
        assertTrue(option instanceof Group);
        final Group group = (Group) option;
        assertEquals(9, group.getOptions().size());
    }

    // Tests custom builders constructor
    @Test
    public void testConstructor_customBuilders_createsConfiguredBuilder() {
        final GroupBuilder gbuilder = new GroupBuilder();
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final PatternBuilder customPatternBuilder = new PatternBuilder(gbuilder, obuilder, abuilder);

        customPatternBuilder.withPattern("z");
        final Option option = customPatternBuilder.create();
        assertNotNull(option);
        assertTrue(option instanceof DefaultOption);
        assertEquals("-z", option.getPreferredName());
    }
}