package org.apache.commons.cli;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class OptionGroupTest {
    private OptionGroup group;
    private Option optionA;
    private Option optionB;
    private Option optionLongOnly;

    @Before
    public void setUp() {
        group = new OptionGroup();
        optionA = new Option("a", "description for a");
        optionB = new Option("b", "description for b");
        optionLongOnly = new Option(null, "long-only", false, "long option only");
    }

    // Tests addOption and getOptions consistency
    @Test
    public void testAddOption_getOptionsContainsAddedOption() {
        group.addOption(optionA);
        assertTrue(group.getOptions().contains(optionA));
        assertEquals(1, group.getOptions().size());
    }

    // Tests addOption and getNames consistency
    @Test
    public void testAddOption_getNamesContainsKey() {
        group.addOption(optionA);
        assertTrue(group.getNames().contains("a"));
    }

    // Tests getNames on empty group
    @Test
    public void testGetNames_emptyGroup() {
        assertTrue(group.getNames().isEmpty());
    }

    // Tests getOptions on empty group
    @Test
    public void testGetOptions_emptyGroup() {
        assertTrue(group.getOptions().isEmpty());
    }

    // Tests setSelected with null on empty group
    @Test
    public void testSetSelected_nullOnEmptyGroup_noException() {
        group.setSelected(null);
        assertNull(group.getSelected());
    }

    // Tests selecting the first option in the group
    @Test
    public void testSetSelected_firstOption_setsSelected() {
        group.addOption(optionA);
        group.setSelected(optionA);
        assertEquals("a", group.getSelected());
    }

    // Tests reselecting the same option instance (should not throw)
    @Test
    public void testSetSelected_sameOptionMultipleTimes_noException() {
        group.addOption(optionA);
        group.setSelected(optionA);
        group.setSelected(optionA);
        assertEquals("a", group.getSelected());
    }

    // Tests that selecting a different option throws AlreadySelectedException
    @Test(expected = AlreadySelectedException.class)
    public void testSetSelected_differentOption_throwsAlreadySelectedException() {
        group.addOption(optionA);
        group.addOption(optionB);
        try {
            group.setSelected(optionA);
        } catch (AlreadySelectedException e) {
            fail("Should not throw on first selection");
        }
        group.setSelected(optionB);
    }

    // Tests resetting selection via setSelected(null)
    @Test
    public void testSetSelected_nullAfterSelection_resetsSelection() {
        group.addOption(optionA);
        try {
            group.setSelected(optionA);
        } catch (AlreadySelectedException e) {
            fail("Should not throw on first selection");
        }
        group.setSelected(null);
        assertNull(group.getSelected());
    }

    // Tests that after reset a different option can be selected
    @Test
    public void testSetSelected_reselectAfterReset_allowed() {
        group.addOption(optionA);
        group.addOption(optionB);
        try {
            group.setSelected(optionA);
        } catch (AlreadySelectedException e) {
            fail("Should not throw on first selection");
        }
        group.setSelected(null);
        try {
            group.setSelected(optionB);
        } catch (AlreadySelectedException e) {
            fail("Should not throw after reset");
        }
        assertEquals("b", group.getSelected());
    }

    // Tests setSelected with an option not added to the group (current behavior)
    @Test
    public void testSetSelected_optionNotInGroup_setsSelected() {
        group.setSelected(optionA);
        assertEquals("a", group.getSelected());
    }

    // Tests that selecting a different option not in the group throws AlreadySelectedException
    @Test(expected = AlreadySelectedException.class)
    public void testSetSelected_optionNotInGroup_afterInGroupOption() {
        group.addOption(optionA);
        try {
            group.setSelected(optionA);
        } catch (AlreadySelectedException e) {
            fail("Should not throw on first selection");
        }
        group.setSelected(optionB);
    }

    // Tests default isRequired value
    @Test
    public void testIsRequired_defaultFalse() {
        assertFalse(group.isRequired());
    }

    // Tests setRequired(true)
    @Test
    public void testSetRequired_true() {
        group.setRequired(true);
        assertTrue(group.isRequired());
    }

    // Tests toString on empty group
    @Test
    public void testToString_emptyGroup() {
        assertEquals("[]", group.toString());
    }

    // Tests toString with option having short opt and description
    @Test
    public void testToString_optionWithOptAndDescription() {
        group.addOption(optionA);
        String result = group.toString();
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
        assertTrue(result.contains("-a"));
        assertTrue(result.contains("description for a"));
    }

    // Tests toString with option having only long opt (no short opt)
    @Test
    public void testToString_optionWithLongOptNoOpt() {
        group.addOption(optionLongOnly);
        String result = group.toString();
        assertTrue(result.contains("--long-only"));
        assertTrue(result.contains("long option only"));
    }

    // Tests toString with option that has no description
    @Test
    public void testToString_optionWithoutDescription() {
        Option optNoDesc = new Option("c", (String) null);
        group.addOption(optNoDesc);
        assertEquals("[-c]", group.toString());
    }

    // Tests toString with multiple options in the group
    @Test
    public void testToString_multipleOptions() {
        group.addOption(optionA);
        group.addOption(optionLongOnly);
        String result = group.toString();
        assertTrue(result.contains("-a") && result.contains("--long-only"));
        assertTrue(result.contains(", "));
    }

    // NEW TESTS - ครอบคลุมส่วนที่ยังขาด

    // Test: setSelected throws AlreadySelectedException with proper message
    @Test
    public void testSetSelected_throwsExceptionWithCorrectMessage() {
        group.addOption(optionA);
        group.addOption(optionB);
        try {
            group.setSelected(optionA);
            group.setSelected(optionB);
            fail("Expected AlreadySelectedException");
        } catch (AlreadySelectedException e) {
            assertNotNull("Exception message should not be null", e.getMessage());
            assertTrue("Exception message should contain option info", 
                e.getMessage().contains("b") || e.getMessage().contains("a"));
        }
    }

    // Test: setSelected allows selecting same option after resetting with null
    @Test
    public void testSetSelected_reselectSameOptionAfterNull() {
        group.addOption(optionA);
        try {
            group.setSelected(optionA);
        } catch (AlreadySelectedException e) {
            fail("Should not throw on first selection");
        }
        group.setSelected(null);
        try {
            group.setSelected(optionA);
        } catch (AlreadySelectedException e) {
            fail("Should allow reselecting same option after null");
        }
        assertEquals("a", group.getSelected());
    }

    // Test: chain of selections with multiple resets
    @Test
    public void testSetSelected_multipleSelectionResetCycles() {
        group.addOption(optionA);
        group.addOption(optionB);
        
        for (int i = 0; i < 3; i++) {
            try {
                group.setSelected(optionA);
                assertEquals("a", group.getSelected());
                group.setSelected(null);
                assertNull("Should be null after reset", group.getSelected());
                group.setSelected(optionB);
                assertEquals("b", group.getSelected());
                group.setSelected(null);
            } catch (AlreadySelectedException e) {
                fail("Exception thrown at cycle " + i + ": " + e.getMessage());
            }
        }
    }

    // Test: setSelected with option that has only long option
    @Test
    public void testSetSelected_longOnlyOption() {
        group.addOption(optionLongOnly);
        try {
            group.setSelected(optionLongOnly);
        } catch (AlreadySelectedException e) {
            fail("Should not throw on first selection of long option");
        }
        assertEquals("long-only", group.getSelected());
    }

    // Test: setSelected throws AlreadySelectedException when changing from long to short option
    @Test(expected = AlreadySelectedException.class)
    public void testSetSelected_switchFromLongToShortOption() {
        group.addOption(optionLongOnly);
        group.addOption(optionA);
        try {
            group.setSelected(optionLongOnly);
        } catch (AlreadySelectedException e) {
            fail("Should not throw on first selection");
        }
        group.setSelected(optionA);
    }

    // Test: setSelected with null multiple times
    @Test
    public void testSetSelected_nullMultipleTimes() {
        group.setSelected(null);
        group.setSelected(null);
        assertNull("Should remain null after multiple null selections", group.getSelected());
    }
}