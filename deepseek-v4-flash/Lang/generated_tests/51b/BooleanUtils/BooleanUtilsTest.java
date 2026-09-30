package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test class for BooleanUtils (Defects4J bug 51b).
 * Focuses on detecting the bug in toBooleanObject with null parameters,
 * and achieving good branch/line coverage.
 */
public class BooleanUtilsTest {

    // --- negate ---
    @Test
    public void testNegate_nullInput_returnsNull() {
        assertNull(BooleanUtils.negate(null));
    }

    @Test
    public void testNegate_trueInput_returnsFalse() {
        assertEquals(Boolean.FALSE, BooleanUtils.negate(Boolean.TRUE));
    }

    @Test
    public void testNegate_falseInput_returnsTrue() {
        assertEquals(Boolean.TRUE, BooleanUtils.negate(Boolean.FALSE));
    }

    // --- isTrue / isNotTrue / isFalse / isNotFalse ---
    @Test
    public void testIsTrue_nullInput_returnsFalse() {
        assertFalse(BooleanUtils.isTrue(null));
    }

    @Test
    public void testIsTrue_trueInput_returnsTrue() {
        assertTrue(BooleanUtils.isTrue(Boolean.TRUE));
    }

    @Test
    public void testIsTrue_falseInput_returnsFalse() {
        assertFalse(BooleanUtils.isTrue(Boolean.FALSE));
    }

    @Test
    public void testIsNotTrue_nullInput_returnsTrue() {
        assertTrue(BooleanUtils.isNotTrue(null));
    }

    @Test
    public void testIsFalse_nullInput_returnsFalse() {
        assertFalse(BooleanUtils.isFalse(null));
    }

    @Test
    public void testIsFalse_falseInput_returnsTrue() {
        assertTrue(BooleanUtils.isFalse(Boolean.FALSE));
    }

    // --- toBooleanObject(boolean) ---
    @Test
    public void testToBooleanObject_boolean_true() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(true));
    }

    @Test
    public void testToBooleanObject_boolean_false() {
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(false));
    }

    // --- toBoolean(Boolean) ---
    @Test
    public void testToBoolean_Boolean_null_returnsFalse() {
        assertFalse(BooleanUtils.toBoolean((Boolean) null));
    }

    @Test
    public void testToBoolean_Boolean_true_returnsTrue() {
        assertTrue(BooleanUtils.toBoolean(Boolean.TRUE));
    }

    // --- toBooleanDefaultIfNull ---
    @Test
    public void testToBooleanDefaultIfNull_nullInput_returnsDefault() {
        assertTrue(BooleanUtils.toBooleanDefaultIfNull(null, true));
        assertFalse(BooleanUtils.toBooleanDefaultIfNull(null, false));
    }

    @Test
    public void testToBooleanDefaultIfNull_notNull_returnsActual() {
        assertTrue(BooleanUtils.toBooleanDefaultIfNull(Boolean.TRUE, false));
        assertFalse(BooleanUtils.toBooleanDefaultIfNull(Boolean.FALSE, true));
    }

    // --- toBoolean(int) ---
    @Test
    public void testToBoolean_int_zero_returnsFalse() {
        assertFalse(BooleanUtils.toBoolean(0));
    }

    @Test
    public void testToBoolean_int_nonZero_returnsTrue() {
        assertTrue(BooleanUtils.toBoolean(1));
        assertTrue(BooleanUtils.toBoolean(42));
    }

    // --- toBooleanObject(int) ---
    @Test
    public void testToBooleanObject_int_zero_returnsFalse() {
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(0));
    }

    @Test
    public void testToBooleanObject_int_nonZero_returnsTrue() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(1));
    }

    // --- toBooleanObject(Integer) ---
    @Test
    public void testToBooleanObject_Integer_null_returnsNull() {
        assertNull(BooleanUtils.toBooleanObject((Integer) null));
    }

    @Test
    public void testToBooleanObject_Integer_zero_returnsFalse() {
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(Integer.valueOf(0)));
    }

    @Test
    public void testToBooleanObject_Integer_nonZero_returnsTrue() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(Integer.valueOf(5)));
    }

    // --- toBoolean(int, int, int) ---
    @Test
    public void testToBoolean_int_trueFalseMatch() {
        assertTrue(BooleanUtils.toBoolean(1, 1, 0));
        assertFalse(BooleanUtils.toBoolean(0, 1, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToBoolean_int_noMatch_throwsException() {
        BooleanUtils.toBoolean(2, 1, 0);
    }

    // --- toBoolean(Integer, Integer, Integer) ---
    @Test
    public void testToBoolean_Integer_nullTrueMatch_returnsTrue() {
        assertTrue(BooleanUtils.toBoolean(null, null, Integer.valueOf(0)));
    }

    @Test
    public void testToBoolean_Integer_nullFalseMatch_returnsFalse() {
        assertFalse(BooleanUtils.toBoolean(null, Integer.valueOf(1), null));
    }

    @Test
    public void testToBoolean_Integer_valueMatch() {
        assertTrue(BooleanUtils.toBoolean(Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(0)));
        assertFalse(BooleanUtils.toBoolean(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(0)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToBoolean_Integer_noMatch_throwsException() {
        BooleanUtils.toBoolean(Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(0));
    }

    // --- toBooleanObject(int, int, int, int) ---
    @Test
    public void testToBooleanObject_int_trueFalseNullMatch() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(1, 1, 0, 2));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(0, 1, 0, 2));
        assertNull(BooleanUtils.toBooleanObject(2, 1, 0, 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToBooleanObject_int_noMatch_throwsException() {
        BooleanUtils.toBooleanObject(3, 1, 0, 2);
    }

    // --- toBooleanObject(Integer, Integer, Integer, Integer) ---
    // These tests expose the Defects4J bug 51b.
    @Test
    public void testToBooleanObject_Integer_allNull_shouldReturnNull() {
        // When value == null and trueValue, falseValue, nullValue are all null,
        // the expected result is null (because null is matched to nullValue).
        // Bug: the code returns Boolean.TRUE due to incorrect order of checks.
        assertNull("Bug: toBooleanObject(null, null, null, null) should return null, but returns TRUE",
            BooleanUtils.toBooleanObject(null, null, null, null));
    }

    @Test
    public void testToBooleanObject_Integer_nullAndOnlyNullValueNull_shouldReturnNull() {
        // value==null, trueValue=1, falseValue=0, nullValue=null
        // Expected: null (because nullValue==null matches value==null)
        assertNull("Bug: should return null when nullValue==null and value==null",
            BooleanUtils.toBooleanObject(null, Integer.valueOf(1), Integer.valueOf(0), null));
    }

    @Test
    public void testToBooleanObject_Integer_nullWithTrueValueNull_returnsTrue() {
        // value==null, trueValue==null, falseValue!=null, nullValue!=null
        // According to current logic, it returns Boolean.TRUE. Is that correct?
        // The doc says null will be converted to null if nullValue matches.
        // Here nullValue is not null (e.g. 2), so null doesn't match nullValue.
        // But trueValue is null, so null == null? The logic returns TRUE.
        // This test documents the current behaviour, but it may be a bug.
        assertEquals(Boolean.TRUE,
            BooleanUtils.toBooleanObject(null, null, Integer.valueOf(0), Integer.valueOf(2)));
    }

    @Test
    public void testToBooleanObject_Integer_valueMatchCorrect() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(2)));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(2)));
        assertNull(BooleanUtils.toBooleanObject(Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToBooleanObject_Integer_noMatch_throwsException() {
        BooleanUtils.toBooleanObject(Integer.valueOf(3), Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(2));
    }

    // --- toBoolean(String) ---
    @Test
    public void testToBoolean_String_null_returnsFalse() {
        assertFalse(BooleanUtils.toBoolean((String) null));
    }

    @Test
    public void testToBoolean_String_exactTrue_returnsTrue() {
        assertTrue(BooleanUtils.toBoolean("true"));
    }

    @Test
    public void testToBoolean_String_TRUE_returnsTrue() {
        assertTrue(BooleanUtils.toBoolean("TRUE"));
    }

    @Test
    public void testToBoolean_String_mixedCaseTrue_returnsTrue() {
        assertTrue(BooleanUtils.toBoolean("tRuE"));
        assertTrue(BooleanUtils.toBoolean("True"));
    }

    @Test
    public void testToBoolean_String_on_returnsTrue() {
        assertTrue(BooleanUtils.toBoolean("on"));
        assertTrue(BooleanUtils.toBoolean("ON"));
        assertTrue(BooleanUtils.toBoolean("On"));
    }

    @Test
    public void testToBoolean_String_yes_returnsTrue() {
        assertTrue(BooleanUtils.toBoolean("yes"));
        assertTrue(BooleanUtils.toBoolean("YES"));
        assertTrue(BooleanUtils.toBoolean("Yes"));
    }

    @Test
    public void testToBoolean_String_false_returnsFalse() {
        assertFalse(BooleanUtils.toBoolean("false"));
    }

    @Test
    public void testToBoolean_String_off_returnsFalse() {
        assertFalse(BooleanUtils.toBoolean("off"));
    }

    @Test
    public void testToBoolean_String_no_returnsFalse() {
        assertFalse(BooleanUtils.toBoolean("no"));
    }

    @Test
    public void testToBoolean_String_nonMatch_returnsFalse() {
        assertFalse(BooleanUtils.toBoolean("blue"));
        assertFalse(BooleanUtils.toBoolean("x"));
        assertFalse(BooleanUtils.toBoolean("yes ")); // trailing space
    }

    // --- toBooleanObject(String) ---
    @Test
    public void testToBooleanObject_String_null_returnsNull() {
        assertNull(BooleanUtils.toBooleanObject(null));
    }

    @Test
    public void testToBooleanObject_String_true_returnsTrue() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("true"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("TRUE"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("tRuE"));
    }

    @Test
    public void testToBooleanObject_String_false_returnsFalse() {
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("false"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("FALSE"));
    }

    @Test
    public void testToBooleanObject_String_onOff_returnsCorrect() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("on"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("off"));
    }

    @Test
    public void testToBooleanObject_String_yesNo_returnsCorrect() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("yes"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("no"));
    }

    @Test
    public void testToBooleanObject_String_nonMatch_returnsNull() {
        assertNull(BooleanUtils.toBooleanObject("maybe"));
    }

    // --- toBooleanObject(String, String, String, String) ---
    // Exposes same bug pattern as Integer version
    @Test
    public void testToBooleanObject_String_allNull_shouldReturnNull() {
        // Bug: when str==null and all three strings are null, code returns TRUE instead of null
        assertNull("Bug: toBooleanObject(null, null, null, null) should return null",
            BooleanUtils.toBooleanObject(null, null, null, null));
    }

    @Test
    public void testToBooleanObject_String_nullWithOnlyNullStringNull_shouldReturnNull() {
        assertNull("Bug: should return null when nullString==null and str==null",
            BooleanUtils.toBooleanObject(null, "true", "false", null));
    }

    @Test
    public void testToBooleanObject_String_matchCorrect() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("yes", "yes", "no", "maybe"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("no", "yes", "no", "maybe"));
        assertNull(BooleanUtils.toBooleanObject("maybe", "yes", "no", "maybe"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToBooleanObject_String_noMatch_throwsException() {
        BooleanUtils.toBooleanObject("other", "yes", "no", "maybe");
    }

    // --- toString methods ---
    @Test
    public void testToString_boolean_returnsCorrect() {
        assertEquals("true", BooleanUtils.toString(true, "true", "false"));
        assertEquals("false", BooleanUtils.toString(false, "true", "false"));
    }

    @Test
    public void testToString_Boolean_null_returnsNullString() {
        assertNull(BooleanUtils.toString(null, "T", "F", "N"));
        assertEquals("N", BooleanUtils.toString(null, "T", "F", "N"));
    }

    @Test
    public void testToStringTrueFalse_boolean() {
        assertEquals("true", BooleanUtils.toStringTrueFalse(true));
        assertEquals("false", BooleanUtils.toStringTrueFalse(false));
    }

    @Test
    public void testToStringOnOff_Boolean() {
        assertEquals("on", BooleanUtils.toStringOnOff(Boolean.TRUE));
        assertEquals("off", BooleanUtils.toStringOnOff(Boolean.FALSE));
        assertNull(BooleanUtils.toStringOnOff(null));
    }

    @Test
    public void testToStringYesNo_boolean() {
        assertEquals("yes", BooleanUtils.toStringYesNo(true));
        assertEquals("no", BooleanUtils.toStringYesNo(false));
    }

    // --- xor (boolean[]) ---
    @Test(expected = IllegalArgumentException.class)
    public void testXor_boolean_nullArray_throwsException() {
        BooleanUtils.xor((boolean[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testXor_boolean_emptyArray_throwsException() {
        BooleanUtils.xor(new boolean[0]);
    }

    @Test
    public void testXor_boolean_singleTrue_returnsTrue() {
        assertTrue(BooleanUtils.xor(new boolean[]{true}));
        assertFalse(BooleanUtils.xor(new boolean[]{false}));
    }

    @Test
    public void testXor_boolean_twoTrue_returnsFalse() {
        assertFalse(BooleanUtils.xor(new boolean[]{true, true}));
    }

    @Test
    public void testXor_boolean_mixed_returnsCorrect() {
        assertTrue(BooleanUtils.xor(new boolean[]{true, false}));
        assertFalse(BooleanUtils.xor(new boolean[]{false, false}));
        assertFalse(BooleanUtils.xor(new boolean[]{true, false, true}));
    }

    // --- xor (Boolean[]) ---
    @Test(expected = IllegalArgumentException.class)
    public void testXor_Boolean_nullArray_throwsException() {
        BooleanUtils.xor((Boolean[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testXor_Boolean_emptyArray_throwsException() {
        BooleanUtils.xor(new Boolean[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testXor_Boolean_arrayWithNull_throwsException() {
        BooleanUtils.xor(new Boolean[]{Boolean.TRUE, null});
    }

    @Test
    public void testXor_Boolean_correctResult() {
        assertEquals(Boolean.TRUE, BooleanUtils.xor(new Boolean[]{Boolean.TRUE, Boolean.FALSE}));
        assertEquals(Boolean.FALSE, BooleanUtils.xor(new Boolean[]{Boolean.TRUE, Boolean.TRUE}));
    }
}