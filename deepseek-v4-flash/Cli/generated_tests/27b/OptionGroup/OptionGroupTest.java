package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class OptionGroupTest {

    private OptionGroup group;

    @Before
    public void setUp() {
        group = new OptionGroup();
    }

    // Tests adding an option to the group
    @Test
    public void testAddOption_addsToGroup() {
        Option optA = new Option("a", "alpha", false, "Alpha option");
        OptionGroup result = group.addOption(optA);
        assertSame(group, result);
        assertTrue(group.getNames().contains("a"));
        assertTrue(group.getOptions().contains(optA));
    }

    // Tests empty group collections
    @Test
    public void testGetNamesAndOptions_emptyGroup_returnsEmptyCollections() {
        assertTrue(group.getNames().isEmpty());
        assertTrue(group.getOptions().isEmpty());
    }

    // Tests initialized selected state
    @Test
    public void testGetSelected_noSelection_returnsNull() {
        assertNull(group.getSelected());
    }

    // Tests selecting first option sets selected
    @Test
    public void testSetSelected_firstOption_setsSelected() {
        Option optA = new Option("a", "alpha", false, "Alpha option");
        group.addOption(optA);
        group.setSelected(optA);
        assertEquals("a", group.getSelected());
    }

    // Tests selecting same option again is allowed
    @Test
    public void testSetSelected_sameOptionAgain_keepsSelected() {
        Option optA = new Option("a", "alpha", false, "Alpha option");
        group.addOption(optA);
        group.setSelected(optA);
        group.setSelected(optA);
        assertEquals("a", group.getSelected());
    }

    // Tests selecting a different option throws exception
    @Test(expected = AlreadySelectedException.class)
    public void testSetSelected_differentOption_throwsAlreadySelectedException() {
        Option optA = new Option("a", "alpha", false, "Alpha option");
        Option optB = new Option("b", "beta", false, "Beta option");
        group.addOption(optA);
        group.addOption(optB);
        group.setSelected(optA);
        group.setSelected(optB);
    }

    // Tests passing null resets selected
    @Test
    public void testSetSelected_null_resetsSelected() {
        Option optA = new Option("a", "alpha", false, "Alpha option");
        group.addOption(optA);
        group.setSelected(optA);
        group.setSelected(null);
        assertNull(group.getSelected());
    }

    // Tests passing null with no previous selection leaves selected null
    @Test
    public void testSetSelected_null_noSelection_leavesNull() {
        group.setSelected(null);
        assertNull(group.getSelected());
    }

    // Tests long-only option selection uses option key (regression for CLI-27)
    @Test
    public void testSetSelected_longOnlyOption_setsKeyAsSelected() {
        Option optL = OptionBuilder.withLongOpt("long").withDescription("Long option").create();
        group.addOption(optL);
        group.setSelected(optL);
        assertEquals("long", group.getSelected());
    }

    // Tests selecting a second option after a long-only option throws (regression)
    @Test(expected = AlreadySelectedException.class)
    public void testSetSelected_longOnlyThenOther_throwsAlreadySelectedException() {
        Option optL = OptionBuilder.withLongOpt("long").withDescription("Long option").create();
        Option optB = new Option("b", "beta", false, "Beta option");
        group.addOption(optL);
        group.addOption(optB);
        group.setSelected(optL);
        group.setSelected(optB);
    }

    // Tests selecting two different long-only options throws (regression)
    @Test(expected = AlreadySelectedException.class)
    public void testSetSelected_twoLongOnlyOptions_throwsAlreadySelectedException() {
        Option optL1 = OptionBuilder.withLongOpt("one").withDescription("One").create();
        Option optL2 = OptionBuilder.withLongOpt("two").withDescription("Two").create();
        group.addOption(optL1);
        group.addOption(optL2);
        group.setSelected(optL1);
        group.setSelected(optL2);
    }

    // Tests default required flag
    @Test
    public void testIsRequired_default_returnsFalse() {
        assertFalse(group.isRequired());
    }

    // Tests setting required to true
    @Test
    public void testSetRequired_true_isRequiredReturnsTrue() {
        group.setRequired(true);
        assertTrue(group.isRequired());
    }

    // Tests setting required to false
    @Test
    public void testSetRequired_false_isRequiredReturnsFalse() {
        group.setRequired(true);
        group.setRequired(false);
        assertFalse(group.isRequired());
    }

    // Tests toString on empty group
    @Test
    public void testToString_emptyGroup_returnsEmptyBrackets() {
        assertEquals("[]", group.toString());
    }

    // Tests toString with short option
    @Test
    public void testToString_shortOption_returnsFormattedString() {
        Option optA = new Option("a", "alpha", false, "Alpha option");
        group.addOption(optA);
        assertEquals("[-a Alpha option]", group.toString());
    }

    // Tests toString with long-only option
    @Test
    public void testToString_longOnlyOption_returnsFormattedString() {
        Option optL = OptionBuilder.withLongOpt("long").withDescription("Long option").create();
        group.addOption(optL);
        assertEquals("[--long Long option]", group.toString());
    }

    // Tests toString with multiple options
    @Test
    public void testToString_multipleOptions_returnsFormattedString() {
        Option optA = new Option("a", "alpha", false, "Alpha option");
        Option optB = new Option("b", "beta", false, "Beta option");
        group.addOption(optA);
        group.addOption(optB);
        String s = group.toString();
        assertTrue(s.startsWith("["));
        assertTrue(s.endsWith("]"));
        assertTrue(s.contains("-a Alpha option"));
        assertTrue(s.contains("-b Beta option"));
        assertTrue(s.contains(", "));
        // Check one of the two possible orderings
        assertTrue(s.equals("[-a Alpha option, -b Beta option]") ||
                   s.equals("[-b Beta option, -a Alpha option]"));
    }

    // Tests getNames after adding multiple options
    @Test
    public void testGetNames_multipleOptions_containsAllKeys() {
        Option optA = new Option("a", "alpha", false, "Alpha option");
        Option optB = new Option("b", "beta", false, "Beta option");
        group.addOption(optA);
        group.addOption(optB);
        assertEquals(2, group.getNames().size());
        assertTrue(group.getNames().contains("a"));
        assertTrue(group.getNames().contains("b"));
    }

    // ========== New tests to cover missing coverage ==========

    // Tests that adding an option with a duplicate key overwrites the previous one
    @Test
    public void testAddOption_duplicateKey_overwritesOption() {
        Option optA1 = new Option("a", "alpha1", false, "First alpha");
        Option optA2 = new Option("a", "alpha2", false, "Second alpha");
        group.addOption(optA1);
        group.addOption(optA2);
        assertEquals(1, group.getNames().size());
        assertEquals(1, group.getOptions().size());
        assertTrue(group.getOptions().contains(optA2));
        assertFalse(group.getOptions().contains(optA1));
    }

    // Tests that getNames() returns an unmodifiable collection
    @Test(expected = UnsupportedOperationException.class)
    public void testGetNames_returnsUnmodifiableCollection() {
        group.addOption(new Option("a", "alpha", false, "Alpha option"));
        group.getNames().add("b");
    }

    // Tests that getOptions() returns an unmodifiable collection
    @Test(expected = UnsupportedOperationException.class)
    public void testGetOptions_returnsUnmodifiableCollection() {
        group.addOption(new Option("a", "alpha", false, "Alpha option"));
        group.getOptions().add(new Option("b", "beta", false, "Beta option"));
    }

    // Tests toString with a null description
    @Test
    public void testToString_nullDescription_formatsCorrectly() {
        Option opt = new Option("a", null, false, null);
        group.addOption(opt);
        String s = group.toString();
        assertTrue(s.startsWith("["));
        assertTrue(s.endsWith("]"));
        assertTrue(s.contains("-a"));
        // Ensure no "null" text appears
        assertFalse(s.contains("null"));
    }

    // Tests that getNames() includes the long option key when no short option exists
    @Test
    public void testGetNames_includesLongOptionKey() {
        Option optL = OptionBuilder.withLongOpt("long").withDescription("Long option").create();
        group.addOption(optL);
        assertTrue(group.getNames().contains("long"));
    }
}