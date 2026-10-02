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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class WriteableCommandLineImplTest {

    private Option rootOption;
    private List args;
    private WriteableCommandLineImpl commandLine;

    private Option createOption(final String preferredName, final Set triggers, final Set prefixes) {
        return (Option) Proxy.newProxyInstance(
            Option.class.getClassLoader(),
            new Class[] { Option.class },
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] methodArgs) throws Throwable {
                    String name = method.getName();
                    if ("getPreferredName".equals(name)) {
                        return preferredName;
                    }
                    if ("getTriggers".equals(name)) {
                        return triggers != null ? triggers : (preferredName != null ? Collections.singleton(preferredName) : Collections.EMPTY_SET);
                    }
                    if ("getPrefixes".equals(name)) {
                        return prefixes;
                    }
                    if ("equals".equals(name)) {
                        return Boolean.valueOf(proxy == methodArgs[0]);
                    }
                    if ("hashCode".equals(name)) {
                        return Integer.valueOf(System.identityHashCode(proxy));
                    }
                    if ("toString".equals(name)) {
                        return "Option[" + preferredName + "]";
                    }
                    return null;
                }
            }
        );
    }

    private Argument createArgument(final String preferredName, final Set triggers, final Set prefixes) {
        return (Argument) Proxy.newProxyInstance(
            Argument.class.getClassLoader(),
            new Class[] { Argument.class },
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] methodArgs) throws Throwable {
                    String name = method.getName();
                    if ("getPreferredName".equals(name)) {
                        return preferredName;
                    }
                    if ("getTriggers".equals(name)) {
                        return triggers != null ? triggers : (preferredName != null ? Collections.singleton(preferredName) : Collections.EMPTY_SET);
                    }
                    if ("getPrefixes".equals(name)) {
                        return prefixes;
                    }
                    if ("equals".equals(name)) {
                        return Boolean.valueOf(proxy == methodArgs[0]);
                    }
                    if ("hashCode".equals(name)) {
                        return Integer.valueOf(System.identityHashCode(proxy));
                    }
                    if ("toString".equals(name)) {
                        return "Argument[" + preferredName + "]";
                    }
                    return null;
                }
            }
        );
    }

    @Before
    public void setUp() {
        Set prefixes = new HashSet(Arrays.asList(new String[] { "-", "--" }));
        rootOption = createOption("root", Collections.singleton("root"), prefixes);
        args = new ArrayList(Arrays.asList(new String[] { "-a", "val1", "--b", "val 2" }));
        commandLine = new WriteableCommandLineImpl(rootOption, args);
    }

    // Tests constructor initialization and normalised arguments
    @Test
    public void testConstructor_validArguments_initializesCorrectly() {
        assertEquals(args, commandLine.getNormalised());
        assertTrue(commandLine.looksLikeOption("-a"));
        assertTrue(commandLine.looksLikeOption("--b"));
        assertFalse(commandLine.looksLikeOption("val1"));
    }

    // Tests adding an option and retrieving it via triggers and preferred name
    @Test
    public void testAddOption_registersPreferredNameAndTriggers() {
        Set triggers = new HashSet(Arrays.asList(new String[] { "-o", "--opt" }));
        Option opt = createOption("-o", triggers, Collections.singleton("-"));

        commandLine.addOption(opt);

        assertTrue(commandLine.hasOption(opt));
        assertEquals(opt, commandLine.getOption("-o"));
        assertEquals(opt, commandLine.getOption("--opt"));
        assertTrue(commandLine.getOptions().contains(opt));
        assertTrue(commandLine.getOptionTriggers().contains("-o"));
        assertTrue(commandLine.getOptionTriggers().contains("--opt"));
    }

    // Tests adding value for an Argument option which also adds the option
    @Test
    public void testAddValue_argumentOption_addsOptionAndValues() {
        Argument arg = createArgument("arg", Collections.singleton("arg"), Collections.singleton("-"));

        commandLine.addValue(arg, "first");
        commandLine.addValue(arg, "second");

        assertTrue(commandLine.hasOption(arg));
        List expected = Arrays.asList(new Object[] { "first", "second" });
        assertEquals(expected, commandLine.getValues(arg, null));
        assertEquals(expected, commandLine.getUndefaultedValues(arg));
    }

    // Tests adding value for standard non-Argument option
    @Test
    public void testAddValue_nonArgumentOption_addsValueWithoutImplicitAddOption() {
        Option opt = createOption("-v", Collections.singleton("-v"), Collections.singleton("-"));

        commandLine.addValue(opt, "val");

        assertFalse(commandLine.hasOption(opt));
        assertEquals(Collections.singletonList("val"), commandLine.getUndefaultedValues(opt));
    }

    // Tests adding switch for the first time
    @Test
    public void testAddSwitch_firstTime_storesSwitchValue() {
        Option opt = createOption("-s", Collections.singleton("-s"), Collections.singleton("-"));

        commandLine.addSwitch(opt, true);

        assertTrue(commandLine.hasOption(opt));
        assertEquals(Boolean.TRUE, commandLine.getSwitch(opt, null));
    }

    // Tests adding switch second time throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testAddSwitch_alreadySet_throwsIllegalStateException() {
        Option opt = createOption("-s", Collections.singleton("-s"), Collections.singleton("-"));

        commandLine.addSwitch(opt, true);
        commandLine.addSwitch(opt, false);
    }

    // Tests hasOption returns false when option is absent
    @Test
    public void testHasOption_absentOption_returnsFalse() {
        Option opt = createOption("-x", Collections.singleton("-x"), Collections.singleton("-"));
        assertFalse(commandLine.hasOption(opt));
    }

    // Tests getOption returns null for unknown trigger
    @Test
    public void testGetOption_unknownTrigger_returnsNull() {
        assertNull(commandLine.getOption("-unknown"));
    }

    // Tests getValues fallback hierarchy: values -> passed default -> configured default -> empty list
    @Test
    public void testGetValues_fallbackOrder_returnsExpected() {
        Option opt1 = createOption("-o1", Collections.singleton("-o1"), Collections.singleton("-"));
        Option opt2 = createOption("-o2", Collections.singleton("-o2"), Collections.singleton("-"));
        Option opt3 = createOption("-o3", Collections.singleton("-o3"), Collections.singleton("-"));

        // Case 1: has values
        commandLine.addValue(opt1, "val1");
        assertEquals(Collections.singletonList("val1"), commandLine.getValues(opt1, Collections.singletonList("defPassed")));

        // Case 2: no values, has passed default
        assertEquals(Collections.singletonList("defPassed"), commandLine.getValues(opt2, Collections.singletonList("defPassed")));

        // Case 3: no values, no passed default, has configured default
        commandLine.setDefaultValues(opt2, Collections.singletonList("defConfigured"));
        assertEquals(Collections.singletonList("defConfigured"), commandLine.getValues(opt2, null));
        assertEquals(Collections.singletonList("defConfigured"), commandLine.getValues(opt2, Collections.EMPTY_LIST));

        // Case 4: no values, no passed default, no configured default
        assertEquals(Collections.EMPTY_LIST, commandLine.getValues(opt3, null));
    }

    // Tests getUndefaultedValues returns empty list when no value was added
    @Test
    public void testGetUndefaultedValues_noValues_returnsEmptyList() {
        Option opt = createOption("-o", Collections.singleton("-o"), Collections.singleton("-"));
        assertEquals(Collections.EMPTY_LIST, commandLine.getUndefaultedValues(opt));
    }

    // Tests getSwitch fallback: switch value -> passed default -> configured default -> null
    @Test
    public void testGetSwitch_fallbackHierarchy() {
        Option opt1 = createOption("-s1", Collections.singleton("-s1"), Collections.singleton("-"));
        Option opt2 = createOption("-s2", Collections.singleton("-s2"), Collections.singleton("-"));
        Option opt3 = createOption("-s3", Collections.singleton("-s3"), Collections.singleton("-"));

        commandLine.addSwitch(opt1, false);
        assertEquals(Boolean.FALSE, commandLine.getSwitch(opt1, Boolean.TRUE));

        assertEquals(Boolean.TRUE, commandLine.getSwitch(opt2, Boolean.TRUE));

        commandLine.setDefaultSwitch(opt2, Boolean.FALSE);
        assertEquals(Boolean.FALSE, commandLine.getSwitch(opt2, null));

        assertNull(commandLine.getSwitch(opt3, null));
    }

    // Tests setDefaultValues and setDefaultSwitch with null to remove defaults
    @Test
    public void testSetDefaultValuesAndSwitch_nullRemovesConfiguredDefaults() {
        Option opt = createOption("-o", Collections.singleton("-o"), Collections.singleton("-"));

        commandLine.setDefaultValues(opt, Collections.singletonList("default"));
        assertEquals(Collections.singletonList("default"), commandLine.getValues(opt, null));

        commandLine.setDefaultValues(opt, null);
        assertEquals(Collections.EMPTY_LIST, commandLine.getValues(opt, null));

        commandLine.setDefaultSwitch(opt, Boolean.TRUE);
        assertEquals(Boolean.TRUE, commandLine.getSwitch(opt, null));

        commandLine.setDefaultSwitch(opt, null);
        assertNull(commandLine.getSwitch(opt, null));
    }

    // Tests PropertyOption support with addProperty and getProperty methods
    @Test
    public void testProperties_addAndGet_returnsCorrectValues() {
        commandLine.addProperty("prop1", "value1");
        assertEquals("value1", commandLine.getProperty("prop1"));
        assertTrue(commandLine.getProperties().contains("prop1"));

        Option customOpt = new PropertyOption();
        commandLine.addProperty(customOpt, "customProp", "customVal");
        assertEquals("customVal", commandLine.getProperty(customOpt, "customProp", "defaultVal"));
        assertTrue(commandLine.getProperties(customOpt).contains("customProp"));

        // Query missing property with default
        assertEquals("defaultVal", commandLine.getProperty(customOpt, "missingProp", "defaultVal"));

        // Query properties for option without any properties
        Option emptyOpt = createOption("-empty", Collections.singleton("-empty"), Collections.singleton("-"));
        assertEquals(Collections.EMPTY_SET, commandLine.getProperties(emptyOpt));
        assertEquals("fallback", commandLine.getProperty(emptyOpt, "anyProp", "fallback"));
    }

    // Tests looksLikeOption with false condition
    @Test
    public void testLooksLikeOption_prefixMismatch_returnsFalse() {
        assertFalse(commandLine.looksLikeOption("plainArgument"));
    }

    // Tests toString formatting with single and multi-word arguments
    @Test
    public void testToString_formatsArgumentsProperly() {
        String output = commandLine.toString();
        assertEquals("-a val1 --b \"val 2\"", output);
    }

    // Tests toString with empty argument list
    @Test
    public void testToString_emptyArguments_returnsEmptyString() {
        WriteableCommandLineImpl emptyCmdLine = new WriteableCommandLineImpl(rootOption, Collections.EMPTY_LIST);
        assertEquals("", emptyCmdLine.toString());
    }

    // Tests hasOption by trigger string
    @Test
    public void testHasOption_byTriggerString() {
        Option opt = createOption("-t", Collections.singleton("-t"), Collections.singleton("-"));
        commandLine.addOption(opt);

        assertTrue(commandLine.hasOption("-t"));
        assertFalse(commandLine.hasOption("-absent"));
    }

    // Tests constructor when root option prefixes are null
    @Test
    public void testConstructor_nullPrefixesOnRootOption_handlesGracefully() {
        Option noPrefixRoot = createOption("root", Collections.singleton("root"), null);
        WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(noPrefixRoot, Collections.singletonList("-a"));

        assertFalse(cmdLine.looksLikeOption("-a"));
    }

    // Tests looksLikeOption with null trigger
    @Test
    public void testLooksLikeOption_nullTrigger_returnsFalse() {
        assertFalse(commandLine.looksLikeOption(null));
    }

    // Tests addOption with null preferredName and empty triggers
    @Test
    public void testAddOption_nullPreferredNameAndEmptyTriggers() {
        Option opt = createOption(null, Collections.EMPTY_SET, Collections.singleton("-"));
        commandLine.addOption(opt);

        assertTrue(commandLine.hasOption(opt));
        assertTrue(commandLine.getOptions().contains(opt));
    }
}