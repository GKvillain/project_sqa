package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class OptionTest
{
    // Tests constructor and default property values
    @Test
    public void testConstructor_standardParameters_initializesCorrectly()
    {
        Option option = new Option("a", "alpha", true, "Option description");
        assertEquals("a", option.getOpt());
        assertEquals("alpha", option.getLongOpt());
        assertEquals("Option description", option.getDescription());
        assertTrue(option.hasArg());
        assertFalse(option.hasArgs());
        assertTrue(option.hasLongOpt());
        assertEquals('a', option.getId());
        assertEquals("a", option.getKey());
    }

    // Tests constructor with short option and description only
    @Test
    public void testConstructor_shortOptAndDescription_hasNoArgAndNoLongOpt()
    {
        Option option = new Option("b", "Beta description");
        assertEquals("b", option.getOpt());
        assertNull(option.getLongOpt());
        assertEquals("Beta description", option.getDescription());
        assertFalse(option.hasArg());
        assertFalse(option.hasLongOpt());
        assertEquals(Option.UNINITIALIZED, option.getArgs());
    }

    // Tests constructor with short option, hasArg flag, and description
    @Test
    public void testConstructor_optWithHasArgFlag_setsArgCountProperly()
    {
        Option option = new Option("c", false, "Gamma description");
        assertEquals("c", option.getOpt());
        assertFalse(option.hasArg());
        assertEquals(Option.UNINITIALIZED, option.getArgs());

        Option optionWithArg = new Option("d", true, "Delta description");
        assertTrue(optionWithArg.hasArg());
        assertEquals(1, optionWithArg.getArgs());
    }

    // Tests invalid option characters in constructor
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_invalidOptionCharacter_throwsIllegalArgumentException()
    {
        new Option("invalid-opt!", "Invalid option");
    }

    // Tests getKey and getId when opt is null but longOpt exists
    @Test
    public void testGetKey_nullOpt_returnsLongOpt()
    {
        Option option = new Option(null, "verbose", false, "Verbose output");
        assertEquals("verbose", option.getKey());
        assertEquals('v', option.getId());
    }

    // Tests getType and setType
    @Test
    public void testSetType_validClass_storesAndReturnsType()
    {
        Option option = new Option("t", "Type option");
        assertNull(option.getType());
        option.setType(Integer.class);
        assertEquals(Integer.class, option.getType());
    }

    // Tests setLongOpt and hasLongOpt
    @Test
    public void testSetLongOpt_validString_updatesLongOpt()
    {
        Option option = new Option("o", "Option");
        assertFalse(option.hasLongOpt());
        option.setLongOpt("output");
        assertTrue(option.hasLongOpt());
        assertEquals("output", option.getLongOpt());
    }

    // Tests optional argument flag
    @Test
    public void testSetOptionalArg_booleanFlag_updatesOptionalArg()
    {
        Option option = new Option("opt", true, "Optional argument option");
        assertFalse(option.hasOptionalArg());
        option.setOptionalArg(true);
        assertTrue(option.hasOptionalArg());
    }

    // Tests setDescription and isRequired / setRequired
    @Test
    public void testSetRequiredAndDescription_validValues_updatesFields()
    {
        Option option = new Option("r", "Required option");
        assertFalse(option.isRequired());
        option.setRequired(true);
        assertTrue(option.isRequired());

        option.setDescription("Updated description");
        assertEquals("Updated description", option.getDescription());
    }

    // Tests setArgName and hasArgName boundaries
    @Test
    public void testArgName_nullEmptyAndValid_handlesCorrectly()
    {
        Option option = new Option("f", "File option");
        assertNull(option.getArgName());
        assertFalse(option.hasArgName());

        option.setArgName("");
        assertEquals("", option.getArgName());
        assertFalse(option.hasArgName());

        option.setArgName("FILE");
        assertEquals("FILE", option.getArgName());
        assertTrue(option.hasArgName());
    }

    // Tests hasArgs with multiple values and UNLIMITED_VALUES
    @Test
    public void testHasArgs_variousArgCounts_evaluatesCorrectly()
    {
        Option option = new Option("m", "Multi option");
        assertFalse(option.hasArgs());

        option.setArgs(2);
        assertTrue(option.hasArgs());
        assertTrue(option.hasArg());

        option.setArgs(Option.UNLIMITED_VALUES);
        assertTrue(option.hasArgs());
        assertTrue(option.hasArg());
    }

    // Tests value separator configuration
    @Test
    public void testValueSeparator_character_configuredProperly()
    {
        Option option = new Option("D", true, "Define property");
        assertFalse(option.hasValueSeparator());
        assertEquals(0, option.getValueSeparator());

        option.setValueSeparator('=');
        assertTrue(option.hasValueSeparator());
        assertEquals('=', option.getValueSeparator());
    }

    // Tests addValueForProcessing when no arguments are allowed
    @Test(expected = RuntimeException.class)
    public void testAddValueForProcessing_uninitializedArgs_throwsRuntimeException()
    {
        Option option = new Option("u", "Uninitialized option");
        option.addValueForProcessing("value");
    }

    // Tests adding more values than maximum allowed capacity
    @Test(expected = RuntimeException.class)
    public void testAddValueForProcessing_exceedingMaxArgs_throwsRuntimeException()
    {
        Option option = new Option("s", true, "Single arg option");
        option.addValueForProcessing("first");
        option.addValueForProcessing("second");
    }

    // Tests addValueForProcessing with value separator and multiple tokens
    @Test
    public void testAddValueForProcessing_withValueSeparator_splitsValuesCorrectly()
    {
        Option option = new Option("D", "key=value option");
        option.setArgs(2);
        option.setValueSeparator('=');
        option.addValueForProcessing("key=value");

        assertEquals("key", option.getValue());
        assertEquals("value", option.getValue(1));
        assertArrayEquals(new String[]{"key", "value"}, option.getValues());
    }

    // Tests value retrieval methods with default values and empty state
    @Test
    public void testGetValues_variousStates_returnsExpectedValues()
    {
        Option option = new Option("v", true, "Value option");
        assertNull(option.getValue());
        assertNull(option.getValue(0));
        assertNull(option.getValues());
        assertEquals("defaultVal", option.getValue("defaultVal"));
        assertNotNull(option.getValuesList());
        assertTrue(option.getValuesList().isEmpty());

        option.addValueForProcessing("actualVal");
        assertEquals("actualVal", option.getValue());
        assertEquals("actualVal", option.getValue("defaultVal"));
        assertEquals("actualVal", option.getValue(0));
        assertArrayEquals(new String[]{"actualVal"}, option.getValues());
        assertEquals(1, option.getValuesList().size());
    }

    // Tests acceptsArg and requiresArg logic
    @Test
    public void testAcceptsArgAndRequiresArg_differentConfigurations_evaluatesAccurately()
    {
        Option option = new Option("a", true, "Arg option");
        assertTrue(option.acceptsArg());
        assertTrue(option.requiresArg());

        option.addValueForProcessing("val");
        assertFalse(option.acceptsArg());
        assertFalse(option.requiresArg());

        Option optUnlimited = new Option("u", "Unlimited option");
        optUnlimited.setArgs(Option.UNLIMITED_VALUES);
        assertTrue(optUnlimited.acceptsArg());
        assertTrue(optUnlimited.requiresArg());
        optUnlimited.addValueForProcessing("val1");
        assertTrue(optUnlimited.acceptsArg());
        assertFalse(optUnlimited.requiresArg());

        Option optOptional = new Option("o", true, "Optional option");
        optOptional.setOptionalArg(true);
        assertTrue(optOptional.acceptsArg());
        assertFalse(optOptional.requiresArg());
    }

    // Tests equals and hashCode contracts
    @Test
    public void testEqualsAndHashCode_variousInstances_adheresToContract()
    {
        Option opt1 = new Option("a", "alpha", true, "desc");
        Option opt2 = new Option("a", "alpha", true, "desc");
        Option opt3 = new Option("b", "beta", true, "desc");
        Option opt4 = new Option("a", "different", true, "desc");

        assertTrue(opt1.equals(opt1));
        assertTrue(opt1.equals(opt2));
        assertEquals(opt1.hashCode(), opt2.hashCode());

        assertFalse(opt1.equals(null));
        assertFalse(opt1.equals("Not an Option"));
        assertFalse(opt1.equals(opt3));
        assertFalse(opt1.equals(opt4));

        Option nullOpt1 = new Option(null, "longOnly", false, "desc");
        Option nullOpt2 = new Option(null, "longOnly", false, "desc");
        assertTrue(nullOpt1.equals(nullOpt2));
        assertEquals(nullOpt1.hashCode(), nullOpt2.hashCode());
        assertFalse(nullOpt1.equals(opt1));
    }

    // Tests clone and clearValues
    @Test
    public void testCloneAndClearValues_clonesProperlyAndClearsValues()
    {
        Option option = new Option("c", true, "Clone option");
        option.addValueForProcessing("val1");

        Option cloned = (Option) option.clone();
        assertNotSame(option, cloned);
        assertEquals(option, cloned);
        assertEquals(option.getValue(), cloned.getValue());

        option.clearValues();
        assertNull(option.getValue());
        assertEquals("val1", cloned.getValue());
    }

    // Tests toString format representation
    @Test
    public void testToString_variousOptions_containsExpectedTokens()
    {
        Option single = new Option("s", "Single");
        single.setType(String.class);
        String sStr = single.toString();
        assertTrue(sStr.contains("[ option: s"));
        assertTrue(sStr.contains("java.lang.String"));

        Option withArgs = new Option("m", "multi", false, "Multi option");
        withArgs.setArgs(2);
        String mStr = withArgs.toString();
        assertTrue(mStr.contains("[ option: m multi [ARG...] :: Multi option ]"));

        Option withSingleArg = new Option("o", "one", true, "One arg option");
        String oStr = withSingleArg.toString();
        assertTrue(oStr.contains("[ option: o one  [ARG] :: One arg option ]"));
    }

    // Tests deprecated addValue method throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testAddValue_invoked_throwsUnsupportedOperationException()
    {
        Option option = new Option("o", "Option");
        option.addValue("test");
    }

    // Tests getValue(int) boundary conditions (negative index and out of bounds)
    @Test
    public void testGetValue_outOfBoundsIndex_returnsNull()
    {
        Option option = new Option("x", true, "Option");
        option.addValueForProcessing("val");
        assertNull(option.getValue(-1));
        assertNull(option.getValue(1));
        assertNull(option.getValue(10));
    }

    // Tests equals and hashCode with asymmetrical null fields
    @Test
    public void testEqualsAndHashCode_asymmetricalNullFields_handlesCorrectly()
    {
        Option optWithNullOpt = new Option(null, "longA", false, "desc");
        Option optWithNonNullOpt = new Option("a", "longA", false, "desc");
        assertFalse(optWithNullOpt.equals(optWithNonNullOpt));
        assertFalse(optWithNonNullOpt.equals(optWithNullOpt));

        Option optWithNullLongOpt = new Option("a", null, false, "desc");
        assertFalse(optWithNonNullOpt.equals(optWithNullLongOpt));
        assertFalse(optWithNullLongOpt.equals(optWithNonNullOpt));

        Option optNulls1 = new Option(null, null, false, "desc");
        Option optNulls2 = new Option(null, null, false, "desc");
        assertTrue(optNulls1.equals(optNulls2));
        assertEquals(optNulls1.hashCode(), optNulls2.hashCode());
    }

    // Tests special valid option characters such as '?' and '@'
    @Test
    public void testConstructor_specialValidOptionCharacters_accepted()
    {
        Option question = new Option("?", "Help option");
        assertEquals("?", question.getOpt());
        assertEquals('?', question.getId());

        Option at = new Option("@", "At option");
        assertEquals("@", at.getOpt());
        assertEquals('@', at.getId());
    }

    // Tests builder pattern instantiation and method configurations
    @Test
    public void testBuilder_allPropertiesConfigured_buildsCorrectOption()
    {
        Option option = Option.builder("o")
            .longOpt("opt-long")
            .desc("Builder description")
            .required()
            .required(true)
            .optionalArg(true)
            .numberOfArgs(3)
            .argName("param")
            .type(Double.class)
            .valueSeparator(':')
            .build();

        assertEquals("o", option.getOpt());
        assertEquals("opt-long", option.getLongOpt());
        assertEquals("Builder description", option.getDescription());
        assertTrue(option.isRequired());
        assertTrue(option.hasOptionalArg());
        assertEquals(3, option.getArgs());
        assertEquals("param", option.getArgName());
        assertEquals(Double.class, option.getType());
        assertEquals(':', option.getValueSeparator());
        assertTrue(option.hasValueSeparator());
    }

    // Tests builder with default hasArg, hasArgs, and no-arg builder
    @Test
    public void testBuilder_hasArgAndHasArgsVariants_buildsExpectedOptions()
    {
        Option optArg = Option.builder("a").hasArg().build();
        assertTrue(optArg.hasArg());
        assertEquals(1, optArg.getArgs());

        Option optArgs = Option.builder("b").hasArgs().build();
        assertTrue(optArgs.hasArgs());
        assertEquals(Option.UNLIMITED_VALUES, optArgs.getArgs());

        Option optNoArg = Option.builder("c").hasArg(false).build();
        assertFalse(optNoArg.hasArg());

        Option optDefaultSeparator = Option.builder("d").hasArgs().valueSeparator().build();
        assertEquals('=', optDefaultSeparator.getValueSeparator());

        Option optFromNoArgBuilder = Option.builder().opt("e").build();
        assertEquals("e", optFromNoArgBuilder.getOpt());
    }

    // Tests builder without opt or longOpt throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testBuilder_missingOptAndLongOpt_throwsIllegalArgumentException()
    {
        Option.builder().build();
    }

    // Tests getId when both opt and longOpt are null
    @Test
    public void testGetId_bothOptAndLongOptNull_returnsZero()
    {
        Option option = new Option(null, null, false, "desc");
        assertEquals(0, option.getId());
        assertNull(option.getKey());
    }

    // Tests clone creates an independent values list
    @Test
    public void testClone_independentValuesList_mutationDoesNotAffectClone()
    {
        Option original = new Option("m", true, "desc");
        original.setArgs(2);
        original.addValueForProcessing("v1");

        Option cloned = (Option) original.clone();
        original.addValueForProcessing("v2");

        assertEquals(2, original.getValuesList().size());
        assertEquals(1, cloned.getValuesList().size());
    }
}