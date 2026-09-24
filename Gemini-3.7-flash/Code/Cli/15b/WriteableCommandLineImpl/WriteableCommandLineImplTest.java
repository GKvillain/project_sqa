package org.apache.commons.cli2.commandline;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.option.PropertyOption;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class WriteableCommandLineImplTest {

    private Option rootOption;
    private List argumentList;
    private WriteableCommandLineImpl commandLine;

    @Before
    public void setUp() {
        rootOption = new PropertyOption();
        argumentList = new ArrayList();
        argumentList.add("-Dkey=value");
        commandLine = new WriteableCommandLineImpl(rootOption, argumentList);
    }

    private Argument createDummyArgument(final String preferredName, final Set triggers) {
        return (Argument) Proxy.newProxyInstance(
            Argument.class.getClassLoader(),
            new Class[] { Argument.class },
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) {
                    if ("getPreferredName".equals(method.getName())) {
                        return preferredName;
                    }
                    if ("getTriggers".equals(method.getName())) {
                        return triggers == null ? Collections.EMPTY_SET : triggers;
                    }
                    if ("getPrefixes".equals(method.getName())) {
                        return Collections.EMPTY_SET;
                    }
                    return null;
                }
            }
        );
    }

    // Tests constructor and getNormalised
    @Test
    public void testGetNormalised_validList_returnsUnmodifiableList() {
        List normalised = commandLine.getNormalised();
        assertEquals(argumentList, normalised);
    }

    // Tests adding an option and checking existence
    @Test
    public void testAddOption_singleOption_hasOptionReturnsTrue() {
        Option opt = new PropertyOption();
        commandLine.addOption(opt);

        assertTrue(commandLine.hasOption(opt));
        assertEquals(1, commandLine.getOptions().size());
        assertEquals(opt, commandLine.getOptions().get(0));
    }

    // Tests getOption when trigger matches preferred name or triggers
    @Test
    public void testGetOption_validTrigger_returnsOption() {
        Option opt = new PropertyOption();
        commandLine.addOption(opt);

        Option found = commandLine.getOption("-D");
        assertNotNull(found);
        assertEquals(opt, found);
    }

    // Tests getOption with non-existent trigger
    @Test
    public void testGetOption_unknownTrigger_returnsNull() {
        Option found = commandLine.getOption("--unknown");
        assertNull(found);
    }

    // Tests adding value to regular non-argument option
    @Test
    public void testAddValue_regularOption_storesValueWithoutAddingToOptions() {
        Option opt = new PropertyOption();
        commandLine.addValue(opt, "val1");
        commandLine.addValue(opt, "val2");

        List values = commandLine.getValues(opt, null);
        assertEquals(2, values.size());
        assertEquals("val1", values.get(0));
        assertEquals("val2", values.get(1));
        assertFalse(commandLine.hasOption(opt));
    }

    // Tests adding value to Argument option which auto-adds the option
    @Test
    public void testAddValue_argumentOption_automaticallyAddsOption() {
        Argument arg = createDummyArgument("testArg", Collections.singleton("testArg"));
        commandLine.addValue(arg, "argVal");

        assertTrue(commandLine.hasOption(arg));
        assertEquals(Collections.singletonList("argVal"), commandLine.getValues(arg, null));
    }

    // Tests addSwitch and getSwitch normal flow
    @Test
    public void testAddSwitch_newSwitch_storedAndRetrievedCorrectly() {
        Option opt = new PropertyOption();
        commandLine.addSwitch(opt, true);

        assertTrue(commandLine.hasOption(opt));
        assertEquals(Boolean.TRUE, commandLine.getSwitch(opt, null));
    }

    // Tests addSwitch when switch already set throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testAddSwitch_alreadySet_throwsIllegalStateException() {
        Option opt = new PropertyOption();
        commandLine.addSwitch(opt, true);
        commandLine.addSwitch(opt, false);
    }

    // Tests getSwitch falling back to method default and option default
    @Test
    public void testGetSwitch_fallbacks_returnsDefaults() {
        Option opt1 = new PropertyOption();
        Option opt2 = new PropertyOption();

        // No value, method default provided
        assertEquals(Boolean.TRUE, commandLine.getSwitch(opt1, Boolean.TRUE));

        // Option default set
        commandLine.setDefaultSwitch(opt2, Boolean.FALSE);
        assertEquals(Boolean.FALSE, commandLine.getSwitch(opt2, null));

        // Clear option default
        commandLine.setDefaultSwitch(opt2, null);
        assertNull(commandLine.getSwitch(opt2, null));
    }

    // Tests getValues hierarchy: commandline values -> method default -> option default -> empty list
    @Test
    public void testGetValues_hierarchy_resolvesInOrder() {
        Option opt = new PropertyOption();

        // 1. No values set -> empty list
        assertEquals(Collections.EMPTY_LIST, commandLine.getValues(opt, null));

        // 2. Option default values set
        List optionDefaults = Arrays.asList(new Object[] {"default1", "default2"});
        commandLine.setDefaultValues(opt, optionDefaults);
        assertEquals(optionDefaults, commandLine.getValues(opt, null));

        // 3. Method default values supplied
        List methodDefaults = Collections.singletonList("methodDefault");
        assertEquals(methodDefaults, commandLine.getValues(opt, methodDefaults));

        // 4. Commandline value added
        commandLine.addValue(opt, "actual");
        assertEquals(Collections.singletonList("actual"), commandLine.getValues(opt, methodDefaults));
    }

    // Tests setDefaultValues with null removes the default values
    @Test
    public void testSetDefaultValues_null_removesDefaultValues() {
        Option opt = new PropertyOption();
        commandLine.setDefaultValues(opt, Arrays.asList(new Object[] {"val"}));
        commandLine.setDefaultValues(opt, null);

        assertEquals(Collections.EMPTY_LIST, commandLine.getValues(opt, null));
    }

    // Tests getUndefaultedValues only returns command line values
    @Test
    public void testGetUndefaultedValues_withDefaults_returnsOnlyCliValues() {
        Option opt = new PropertyOption();
        commandLine.setDefaultValues(opt, Collections.singletonList("def"));

        assertEquals(Collections.EMPTY_LIST, commandLine.getUndefaultedValues(opt));

        commandLine.addValue(opt, "cliVal");
        assertEquals(Collections.singletonList("cliVal"), commandLine.getUndefaultedValues(opt));
    }

    // Tests default property operations without explicit option
    @Test
    public void testPropertyMethods_defaultPropertyOption_addsAndGetsProperties() {
        commandLine.addProperty("propertyKey", "propertyValue");

        assertEquals("propertyValue", commandLine.getProperty("propertyKey"));
        Set keys = commandLine.getProperties();
        assertTrue(keys.contains("propertyKey"));
    }

    // Tests property operations with explicit Option
    @Test
    public void testPropertyMethods_specificOption_addsAndGetsProperties() {
        Option opt = new PropertyOption();

        assertNull(commandLine.getProperty(opt, "missing", null));
        assertEquals("fallback", commandLine.getProperty(opt, "missing", "fallback"));
        assertEquals(Collections.EMPTY_SET, commandLine.getProperties(opt));

        commandLine.addProperty(opt, "k1", "v1");
        assertEquals("v1", commandLine.getProperty(opt, "k1", "fallback"));
        assertEquals(1, commandLine.getProperties(opt).size());
        assertTrue(commandLine.getProperties(opt).contains("k1"));
    }

    // Tests looksLikeOption matching prefixes
    @Test
    public void testLooksLikeOption_matchingAndNonMatching_returnsCorrectBoolean() {
        assertTrue(commandLine.looksLikeOption("-Dproperty=value"));
        assertFalse(commandLine.looksLikeOption("nonOption"));
    }

    // Tests toString formatting with and without space in arguments
    @Test
    public void testToString_argumentsWithAndWithoutSpaces_formatsCorrectly() {
        List args = new ArrayList();
        args.add("--opt");
        args.add("arg with spaces");
        args.add("plainArg");

        WriteableCommandLineImpl cl = new WriteableCommandLineImpl(rootOption, args);
        assertEquals("--opt \"arg with spaces\" plainArg", cl.toString());
    }

    // Tests getOptionTriggers returns all registered triggers
    @Test
    public void testGetOptionTriggers_multipleTriggers_returnsAllTriggers() {
        Set triggers = new HashSet();
        triggers.add("-t");
        triggers.add("--test");
        Argument arg = createDummyArgument("--test", triggers);

        commandLine.addOption(arg);

        Set resultTriggers = commandLine.getOptionTriggers();
        assertTrue(resultTriggers.contains("-t"));
        assertTrue(resultTriggers.contains("--test"));
    }
}