package org.apache.commons.cli2.option;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.WriteableCommandLine;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for OptionImpl.
 */
public class OptionImplTest {

    private static class ConcreteOption extends OptionImpl {
        private final String preferredName;
        private final String description;
        private final Set prefixes;
        private final Set triggers;

        public ConcreteOption(final int id, final boolean required) {
            this(id, required, null, null, Collections.EMPTY_SET, Collections.EMPTY_SET);
        }

        public ConcreteOption(final int id,
                              final boolean required,
                              final String preferredName,
                              final String description,
                              final Set prefixes,
                              final Set triggers) {
            super(id, required);
            this.preferredName = preferredName;
            this.description = description;
            this.prefixes = prefixes != null ? prefixes : Collections.EMPTY_SET;
            this.triggers = triggers != null ? triggers : Collections.EMPTY_SET;
        }

        public boolean canProcess(final WriteableCommandLine commandLine, final String argument) {
            return triggers.contains(argument);
        }

        public void process(final WriteableCommandLine commandLine, final ListIterator arguments) {
        }

        public void validate(final WriteableCommandLine commandLine) {
        }

        public void appendUsage(final StringBuffer buffer, final Set helpSettings, final Comparator comp) {
            if (preferredName != null) {
                buffer.append(preferredName);
            }
        }

        public List helpLines(final int depth, final Set helpSettings, final Comparator comp) {
            return Collections.EMPTY_LIST;
        }

        public String getPreferredName() {
            return preferredName;
        }

        public String getDescription() {
            return description;
        }

        public Set getPrefixes() {
            return prefixes;
        }

        public Set getTriggers() {
            return triggers;
        }

        public void testCheckPrefixes(final Set prefixes) {
            checkPrefixes(prefixes);
        }
    }

    // Tests getId returns the constructor supplied id
    @Test
    public void testGetId_validId_returnsCorrectId() {
        final ConcreteOption option = new ConcreteOption(42, false);
        assertEquals(42, option.getId());
    }

    // Tests isRequired returns the constructor supplied required flag
    @Test
    public void testIsRequired_trueValue_returnsTrue() {
        final ConcreteOption option = new ConcreteOption(1, true);
        assertTrue(option.isRequired());
    }

    // Tests isRequired returns false when initialized with false
    @Test
    public void testIsRequired_falseValue_returnsFalse() {
        final ConcreteOption option = new ConcreteOption(1, false);
        assertFalse(option.isRequired());
    }

    // Tests canProcess with an iterator containing matching argument
    @Test
    public void testCanProcess_iteratorWithMatchingArgument_returnsTrue() {
        final Set triggers = new HashSet(Arrays.asList("--opt"));
        final ConcreteOption option = new ConcreteOption(1, false, "--opt", "desc", Collections.EMPTY_SET, triggers);

        final List args = new ArrayList();
        args.add("--opt");
        args.add("extra");
        final ListIterator iterator = args.listIterator();

        assertTrue(option.canProcess(null, iterator));
        assertEquals(0, iterator.nextIndex());
    }

    // Tests canProcess with an empty iterator
    @Test
    public void testCanProcess_emptyIterator_returnsFalse() {
        final ConcreteOption option = new ConcreteOption(1, false);
        final List args = new ArrayList();
        final ListIterator iterator = args.listIterator();

        assertFalse(option.canProcess(null, iterator));
    }

    // Tests toString generates output via appendUsage
    @Test
    public void testToString_validOption_returnsUsageString() {
        final ConcreteOption option = new ConcreteOption(1, false, "--test", "description", null, null);
        assertEquals("--test", option.toString());
    }

    // Tests equals with the same instance
    @Test
    public void testEquals_sameInstance_returnsTrue() {
        final ConcreteOption option = new ConcreteOption(1, false);
        assertTrue(option.equals(option));
    }

    // Tests equals with null and non-OptionImpl object
    @Test
    public void testEquals_nullAndOtherType_returnsFalse() {
        final ConcreteOption option = new ConcreteOption(1, false);
        assertFalse(option.equals(null));
        assertFalse(option.equals("not an option"));
    }

    // Tests equals with identical attributes
    @Test
    public void testEquals_identicalAttributes_returnsTrue() {
        final Set prefixes = new HashSet(Arrays.asList("-", "--"));
        final Set triggers = new HashSet(Arrays.asList("-o", "--opt"));
        final ConcreteOption opt1 = new ConcreteOption(1, true, "--opt", "desc", prefixes, triggers);
        final ConcreteOption opt2 = new ConcreteOption(1, true, "--opt", "desc", prefixes, triggers);

        assertTrue(opt1.equals(opt2));
        assertEquals(opt1.hashCode(), opt2.hashCode());
    }

    // Tests equals when id is different
    @Test
    public void testEquals_differentId_returnsFalse() {
        final ConcreteOption opt1 = new ConcreteOption(1, false, "name", "desc", null, null);
        final ConcreteOption opt2 = new ConcreteOption(2, false, "name", "desc", null, null);

        assertFalse(opt1.equals(opt2));
    }

    // Tests equals when preferredName is different
    @Test
    public void testEquals_differentPreferredName_returnsFalse() {
        final ConcreteOption opt1 = new ConcreteOption(1, false, "name1", "desc", null, null);
        final ConcreteOption opt2 = new ConcreteOption(1, false, "name2", "desc", null, null);

        assertFalse(opt1.equals(opt2));
    }

    // Tests equals when description is different
    @Test
    public void testEquals_differentDescription_returnsFalse() {
        final ConcreteOption opt1 = new ConcreteOption(1, false, "name", "desc1", null, null);
        final ConcreteOption opt2 = new ConcreteOption(1, false, "name", "desc2", null, null);

        assertFalse(opt1.equals(opt2));
    }

    // Tests equals when prefixes are different
    @Test
    public void testEquals_differentPrefixes_returnsFalse() {
        final Set prefixes1 = new HashSet(Arrays.asList("-"));
        final Set prefixes2 = new HashSet(Arrays.asList("--"));
        final ConcreteOption opt1 = new ConcreteOption(1, false, "name", "desc", prefixes1, null);
        final ConcreteOption opt2 = new ConcreteOption(1, false, "name", "desc", prefixes2, null);

        assertFalse(opt1.equals(opt2));
    }

    // Tests equals when triggers are different
    @Test
    public void testEquals_differentTriggers_returnsFalse() {
        final Set triggers1 = new HashSet(Arrays.asList("-a"));
        final Set triggers2 = new HashSet(Arrays.asList("-b"));
        final ConcreteOption opt1 = new ConcreteOption(1, false, "name", "desc", null, triggers1);
        final ConcreteOption opt2 = new ConcreteOption(1, false, "name", "desc", null, triggers2);

        assertFalse(opt1.equals(opt2));
    }

    // Tests hashCode calculation with null preferredName and description
    @Test
    public void testHashCode_nullPreferredNameAndDescription_calculatesHashCode() {
        final ConcreteOption opt = new ConcreteOption(1, false, null, null, null, null);
        final int hash = opt.hashCode();
        assertTrue(hash != 0);
    }

    // Tests findOption with matching trigger returns this instance
    @Test
    public void testFindOption_matchingTrigger_returnsThis() {
        final Set triggers = new HashSet(Arrays.asList("-f", "--file"));
        final ConcreteOption option = new ConcreteOption(1, false, "--file", "desc", null, triggers);

        final Option result = option.findOption("-f");
        assertSame(option, result);
    }

    // Tests findOption with non-matching trigger returns null
    @Test
    public void testFindOption_nonMatchingTrigger_returnsNull() {
        final Set triggers = new HashSet(Arrays.asList("-f", "--file"));
        final ConcreteOption option = new ConcreteOption(1, false, "--file", "desc", null, triggers);

        final Option result = option.findOption("-x");
        assertNull(result);
    }

    // Tests defaults does nothing and does not throw exception
    @Test
    public void testDefaults_normalInvocation_doesNotThrow() {
        final ConcreteOption option = new ConcreteOption(1, false);
        option.defaults(null);
    }

    // Tests checkPrefixes with empty prefixes set does nothing
    @Test
    public void testCheckPrefixes_emptyPrefixes_noExceptionThrown() {
        final ConcreteOption option = new ConcreteOption(1, false, "opt", "desc", null, null);
        option.testCheckPrefixes(Collections.EMPTY_SET);
    }

    // Tests checkPrefixes when all triggers and preferred name match valid prefix
    @Test
    public void testCheckPrefixes_validPrefixes_noExceptionThrown() {
        final Set prefixes = new HashSet(Arrays.asList("-", "--"));
        final Set triggers = new HashSet(Arrays.asList("-o", "--option"));
        final ConcreteOption option = new ConcreteOption(1, false, "--option", "desc", prefixes, triggers);

        option.testCheckPrefixes(prefixes);
    }

    // Tests checkPrefixes when preferred name does not start with valid prefix throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCheckPrefixes_invalidPreferredName_throwsIllegalArgumentException() {
        final Set prefixes = new HashSet(Arrays.asList("-", "--"));
        final Set triggers = new HashSet(Arrays.asList("-o", "--option"));
        final ConcreteOption option = new ConcreteOption(1, false, "optionWithoutPrefix", "desc", prefixes, triggers);

        option.testCheckPrefixes(prefixes);
    }

    // Tests checkPrefixes when a trigger does not start with valid prefix throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCheckPrefixes_invalidTrigger_throwsIllegalArgumentException() {
        final Set prefixes = new HashSet(Arrays.asList("-", "--"));
        final Set triggers = new HashSet(Arrays.asList("-o", "invalidTrigger"));
        final ConcreteOption option = new ConcreteOption(1, false, "--option", "desc", prefixes, triggers);

        option.testCheckPrefixes(prefixes);
    }
}