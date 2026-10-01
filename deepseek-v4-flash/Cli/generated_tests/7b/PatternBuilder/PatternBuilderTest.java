package org.apache.commons.cli2.builder;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.validation.ClassValidator;
import org.apache.commons.cli2.validation.DateValidator;
import org.apache.commons.cli2.validation.FileValidator;
import org.apache.commons.cli2.validation.NumberValidator;
import org.apache.commons.cli2.validation.UrlValidator;

public class PatternBuilderTest {

    private PatternBuilder builder;

    @Before
    public void setUp() {
        builder = new PatternBuilder();
    }

    @Test
    public void testDefaultConstructor_createsBuilder() {
        assertNotNull(new PatternBuilder());
    }

    @Test
    public void testConstructorWithBuilders_createsBuilder() {
        GroupBuilder gb = new GroupBuilder();
        DefaultOptionBuilder dob = new DefaultOptionBuilder();
        ArgumentBuilder ab = new ArgumentBuilder();
        PatternBuilder pb = new PatternBuilder(gb, dob, ab);
        assertNotNull(pb);
    }

    @Test
    public void testWithPattern_simpleOption_returnsOptionWithShortName() {
        builder.withPattern("D");
        Option opt = builder.create();
        assertTrue(opt.getPreferredName().endsWith("D"));
        assertFalse(opt.isRequired());
        assertNull(opt.getArgument());
    }

    @Test
    public void testWithPattern_requiredBeforeOption_optionIsRequired() {
        builder.withPattern("!D");
        Option opt = builder.create();
        assertTrue(opt.isRequired());
        assertNull(opt.getArgument());
    }

    @Test
    public void testWithPattern_requiredAfterOption_optionIsRequired() {
        builder.withPattern("D!");
        Option opt = builder.create();
        assertTrue(opt.isRequired());
        assertNull(opt.getArgument());
    }

    @Test
    public void testWithPattern_argumentTypeAt_createsClassValidator() {
        builder.withPattern("D@");
        Option opt = builder.create();
        Argument arg = opt.getArgument();
        assertNotNull(arg);
        assertTrue(arg.getValidator() instanceof ClassValidator);
    }

    @Test
    public void testWithPattern_argumentTypePlus_createsClassValidator() {
        builder.withPattern("D+");
        Option opt = builder.create();
        Argument arg = opt.getArgument();
        assertNotNull(arg);
        assertTrue(arg.getValidator() instanceof ClassValidator);
    }

    @Test
    public void testWithPattern_argumentTypePercent_createsNumberValidator() {
        builder.withPattern("D%");
        Option opt = builder.create();
        Argument arg = opt.getArgument();
        assertNotNull(arg);
        assertTrue(arg.getValidator() instanceof NumberValidator);
    }

    @Test
    public void testWithPattern_argumentTypeHash_createsDateValidator() {
        builder.withPattern("D#");
        Option opt = builder.create();
        Argument arg = opt.getArgument();
        assertNotNull(arg);
        assertTrue(arg.getValidator() instanceof DateValidator);
    }

    @Test
    public void testWithPattern_argumentTypeLessThan_createsFileValidator() {
        builder.withPattern("D<");
        Option opt = builder.create();
        Argument arg = opt.getArgument();
        assertNotNull(arg);
        assertTrue(arg.getValidator() instanceof FileValidator);
    }

    @Test
    public void testWithPattern_argumentTypeGreaterThan_createsFileValidator() {
        builder.withPattern("D>");
        Option opt = builder.create();
        Argument arg = opt.getArgument();
        assertNotNull(arg);
        assertTrue(arg.getValidator() instanceof FileValidator);
    }

    @Test
    public void testWithPattern_argumentTypeAsterisk_createsFileValidator() {
        builder.withPattern("D*");
        Option opt = builder.create();
        Argument arg = opt.getArgument();
        assertNotNull(arg);
        assertTrue(arg.getValidator() instanceof FileValidator);
    }

    @Test
    public void testWithPattern_argumentTypeSlash_createsUrlValidator() {
        builder.withPattern("D/");
        Option opt = builder.create();
        Argument arg = opt.getArgument();
        assertNotNull(arg);
        assertTrue(arg.getValidator() instanceof UrlValidator);
    }

    @Test
    public void testWithPattern_argumentTypeColon_createsNullValidator() {
        builder.withPattern("D:");
        Option opt = builder.create();
        Argument arg = opt.getArgument();
        assertNotNull(arg);
        assertNull(arg.getValidator());
    }

    @Test
    public void testWithPattern_multipleOptions_returnsGroup() {
        builder.withPattern("ABC");
        Option opt = builder.create();
        assertNotNull(opt);
        assertTrue(opt instanceof Group);
    }

    @Test
    public void testCreate_withoutPattern_doesNotThrow() {
        Option opt = builder.create();
        assertNotNull(opt);
        assertTrue(opt instanceof Group);
    }

    @Test
    public void testCreate_resetsOptions() {
        builder.withPattern("A");
        builder.create();
        builder.withPattern("B");
        Option opt = builder.create();
        assertTrue(opt.getPreferredName().endsWith("B"));
    }

    @Test
    public void testWithPattern_requiredAndArgumentType_createsOption() {
        builder.withPattern("!D+");
        Option opt = builder.create();
        assertTrue(opt.isRequired());
        assertNotNull(opt.getArgument());
        assertTrue(opt.getArgument().getValidator() instanceof ClassValidator);
    }

    @Test
    public void testWithPattern_returnsThis() {
        assertSame(builder, builder.withPattern("D"));
    }

    @Test
    public void testCreate_resetsAfterGroupCreation() {
        builder.withPattern("AB");
        builder.create();
        builder.withPattern("C");
        Option opt = builder.create();
        assertTrue(opt.getPreferredName().endsWith("C"));
    }

    @Test
    public void testWithPattern_mixedOptionsAndArgument_returnsGroup() {
        builder.withPattern("A@B");
        Option opt = builder.create();
        assertNotNull(opt);
        assertTrue(opt instanceof Group);
    }

    @Test
    public void testWithPattern_multipleCalls_accumulateOptions() {
        builder.withPattern("A");
        builder.withPattern("B");
        Option opt = builder.create();
        assertNotNull(opt);
        assertTrue(opt instanceof Group);
    }
}