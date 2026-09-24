package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link BooleanUtils}.
 */
public class BooleanUtilsTest {

    // Tests constructor
    @Test
    public void testConstructor_default_createsInstance() {
        assertNotNull(new BooleanUtils());
    }

    // Tests negate with valid and null inputs
    @Test
    public void testNegate_variousInputs_returnsNegatedValue() {
        assertNull(BooleanUtils.negate(null));
        assertEquals(Boolean.FALSE, BooleanUtils.negate(Boolean.TRUE));
        assertEquals(Boolean.TRUE, BooleanUtils.negate(Boolean.FALSE));
    }

    // Tests isTrue, isNotTrue, isFalse, isNotFalse
    @Test
    public void testBooleanChecks_variousBooleans_returnsCorrectFlags() {
        assertTrue(BooleanUtils.isTrue(Boolean.TRUE));
        assertFalse(BooleanUtils.isTrue(Boolean.FALSE));
        assertFalse(BooleanUtils.isTrue(null));

        assertFalse(BooleanUtils.isNotTrue(Boolean.TRUE));
        assertTrue(BooleanUtils.isNotTrue(Boolean.FALSE));
        assertTrue(BooleanUtils.isNotTrue(null));

        assertFalse(BooleanUtils.isFalse(Boolean.TRUE));
        assertTrue(BooleanUtils.isFalse(Boolean.FALSE));
        assertFalse(BooleanUtils.isFalse(null));

        assertTrue(BooleanUtils.isNotFalse(Boolean.TRUE));
        assertFalse(BooleanUtils.isNotFalse(Boolean.FALSE));
        assertTrue(BooleanUtils.isNotFalse(null));
    }

    // Tests toBooleanObject and toBoolean with boolean / Boolean inputs
    @Test
    public void testToBooleanAndObject_primitiveAndObject_returnsExpected() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(true));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(false));

        assertTrue(BooleanUtils.toBoolean(Boolean.TRUE));
        assertFalse(BooleanUtils.toBoolean(Boolean.FALSE));
        assertFalse(BooleanUtils.toBoolean((Boolean) null));

        assertTrue(BooleanUtils.toBooleanDefaultIfNull(Boolean.TRUE, false));
        assertFalse(BooleanUtils.toBooleanDefaultIfNull(Boolean.FALSE, true));
        assertTrue(BooleanUtils.toBooleanDefaultIfNull(null, true));
        assertFalse(BooleanUtils.toBooleanDefaultIfNull(null, false));
    }

    // Tests toBoolean / toBooleanObject with int / Integer values
    @Test
    public void testToBoolean_intAndInteger_returnsExpected() {
        assertFalse(BooleanUtils.toBoolean(0));
        assertTrue(BooleanUtils.toBoolean(1));
        assertTrue(BooleanUtils.toBoolean(-1));

        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(0));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(1));

        assertNull(BooleanUtils.toBooleanObject((Integer) null));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(Integer.valueOf(0)));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(Integer.valueOf(1)));

        assertTrue(BooleanUtils.toBoolean(1, 1, 0));
        assertFalse(BooleanUtils.toBoolean(0, 1, 0));

        assertTrue(BooleanUtils.toBoolean(Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(0)));
        assertFalse(BooleanUtils.toBoolean(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(0)));
        assertTrue(BooleanUtils.toBoolean(null, null, Integer.valueOf(0)));
        assertFalse(BooleanUtils.toBoolean(null, Integer.valueOf(1), null));

        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(1, 1, 0, -1));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(0, 1, 0, -1));
        assertNull(BooleanUtils.toBooleanObject(-1, 1, 0, -1));

        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(-1)));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(-1)));
        assertNull(BooleanUtils.toBooleanObject(Integer.valueOf(-1), Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(-1)));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(null, null, Integer.valueOf(0), Integer.valueOf(-1)));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(null, Integer.valueOf(1), null, Integer.valueOf(-1)));
        assertNull(BooleanUtils.toBooleanObject(null, Integer.valueOf(1), Integer.valueOf(0), null));
    }

    // Tests toBoolean int/Integer matching with no match throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testToBoolean_intNoMatch_throwsException() {
        BooleanUtils.toBoolean(2, 1, 0);
    }

    // Tests toBoolean Integer matching with no match throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testToBoolean_integerNoMatch_throwsException() {
        BooleanUtils.toBoolean(Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(0));
    }

    // Tests toBooleanObject int matching with no match throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testToBooleanObject_intNoMatch_throwsException() {
        BooleanUtils.toBooleanObject(2, 1, 0, -1);
    }

    // Tests toBooleanObject Integer matching with no match throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testToBooleanObject_integerNoMatch_throwsException() {
        BooleanUtils.toBooleanObject(Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(-1));
    }

    // Tests toInteger methods
    @Test
    public void testToInteger_variousInputs_returnsExpected() {
        assertEquals(1, BooleanUtils.toInteger(true));
        assertEquals(0, BooleanUtils.toInteger(false));

        assertEquals(Integer.valueOf(1), BooleanUtils.toIntegerObject(true));
        assertEquals(Integer.valueOf(0), BooleanUtils.toIntegerObject(false));

        assertNull(BooleanUtils.toIntegerObject((Boolean) null));
        assertEquals(Integer.valueOf(1), BooleanUtils.toIntegerObject(Boolean.TRUE));
        assertEquals(Integer.valueOf(0), BooleanUtils.toIntegerObject(Boolean.FALSE));

        assertEquals(10, BooleanUtils.toInteger(true, 10, 20));
        assertEquals(20, BooleanUtils.toInteger(false, 10, 20));

        assertEquals(10, BooleanUtils.toInteger(Boolean.TRUE, 10, 20, 30));
        assertEquals(20, BooleanUtils.toInteger(Boolean.FALSE, 10, 20, 30));
        assertEquals(30, BooleanUtils.toInteger(null, 10, 20, 30));

        assertEquals(Integer.valueOf(10), BooleanUtils.toIntegerObject(true, Integer.valueOf(10), Integer.valueOf(20)));
        assertEquals(Integer.valueOf(20), BooleanUtils.toIntegerObject(false, Integer.valueOf(10), Integer.valueOf(20)));

        assertEquals(Integer.valueOf(10), BooleanUtils.toIntegerObject(Boolean.TRUE, Integer.valueOf(10), Integer.valueOf(20), Integer.valueOf(30)));
        assertEquals(Integer.valueOf(20), BooleanUtils.toIntegerObject(Boolean.FALSE, Integer.valueOf(10), Integer.valueOf(20), Integer.valueOf(30)));
        assertEquals(Integer.valueOf(30), BooleanUtils.toIntegerObject(null, Integer.valueOf(10), Integer.valueOf(20), Integer.valueOf(30)));
    }

    // Tests toBooleanObject(String) standard strings
    @Test
    public void testToBooleanObject_stringInput_returnsBooleanObject() {
        assertNull(BooleanUtils.toBooleanObject((String) null));
        assertNull(BooleanUtils.toBooleanObject(""));
        assertNull(BooleanUtils.toBooleanObject("unknown"));

        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("true"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("TRUE"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("on"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("ON"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("yes"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("YES"));

        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("false"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("FALSE"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("off"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("OFF"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("no"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("NO"));
    }

    // Tests toBooleanObject(String, String, String, String)
    @Test
    public void testToBooleanObject_stringMatches_returnsExpected() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("Y", "Y", "N", "U"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("N", "Y", "N", "U"));
        assertNull(BooleanUtils.toBooleanObject("U", "Y", "N", "U"));

        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(null, null, "N", "U"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(null, "Y", null, "U"));
        assertNull(BooleanUtils.toBooleanObject(null, "Y", "N", null));
    }

    // Tests toBooleanObject(String, String, String, String) no match throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testToBooleanObject_stringNoMatch_throwsException() {
        BooleanUtils.toBooleanObject("X", "Y", "N", "U");
    }

    // Tests toBoolean(String) for various lengths, cases, and non-matching prefixes (Regression for defect 51b)
    @Test
    public void testToBoolean_stringOptimized_returnsExpected() {
        assertFalse(BooleanUtils.toBoolean((String) null));
        assertFalse(BooleanUtils.toBoolean(""));
        assertFalse(BooleanUtils.toBoolean("a"));
        assertFalse(BooleanUtils.toBoolean("invalid"));

        // Length 2
        assertTrue(BooleanUtils.toBoolean("on"));
        assertTrue(BooleanUtils.toBoolean("ON"));
        assertTrue(BooleanUtils.toBoolean("On"));
        assertTrue(BooleanUtils.toBoolean("oN"));
        assertFalse(BooleanUtils.toBoolean("no"));
        assertFalse(BooleanUtils.toBoolean("ox"));

        // Length 3
        assertTrue(BooleanUtils.toBoolean("yes"));
        assertTrue(BooleanUtils.toBoolean("YES"));
        assertTrue(BooleanUtils.toBoolean("YEs"));
        assertTrue(BooleanUtils.toBoolean("yeS"));
        assertFalse(BooleanUtils.toBoolean("off"));
        assertFalse(BooleanUtils.toBoolean("yep"));
        assertFalse(BooleanUtils.toBoolean("tru")); // Length 3 starting with 't' should return false without exception
        assertFalse(BooleanUtils.toBoolean("abc"));

        // Length 4
        assertTrue(BooleanUtils.toBoolean("true"));
        assertTrue(BooleanUtils.toBoolean("TRUE"));
        assertTrue(BooleanUtils.toBoolean("tRue"));
        assertTrue(BooleanUtils.toBoolean("True"));
        assertFalse(BooleanUtils.toBoolean("false"));
        assertFalse(BooleanUtils.toBoolean("tree"));
        assertFalse(BooleanUtils.toBoolean("txyz"));
    }

    // Tests toBoolean(String, String, String)
    @Test
    public void testToBoolean_stringMatches_returnsExpected() {
        assertTrue(BooleanUtils.toBoolean("Y", "Y", "N"));
        assertFalse(BooleanUtils.toBoolean("N", "Y", "N"));
        assertTrue(BooleanUtils.toBoolean(null, null, "N"));
        assertFalse(BooleanUtils.toBoolean(null, "Y", null));
    }

    // Tests toBoolean(String, String, String) with no match throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testToBoolean_stringMatchingNoMatch_throwsException() {
        BooleanUtils.toBoolean("X", "Y", "N");
    }

    // Tests toString methods
    @Test
    public void testToString_variousInputs_returnsExpected() {
        assertEquals("true", BooleanUtils.toStringTrueFalse(Boolean.TRUE));
        assertEquals("false", BooleanUtils.toStringTrueFalse(Boolean.FALSE));
        assertNull(BooleanUtils.toStringTrueFalse((Boolean) null));
        assertEquals("true", BooleanUtils.toStringTrueFalse(true));
        assertEquals("false", BooleanUtils.toStringTrueFalse(false));

        assertEquals("on", BooleanUtils.toStringOnOff(Boolean.TRUE));
        assertEquals("off", BooleanUtils.toStringOnOff(Boolean.FALSE));
        assertNull(BooleanUtils.toStringOnOff((Boolean) null));
        assertEquals("on", BooleanUtils.toStringOnOff(true));
        assertEquals("off", BooleanUtils.toStringOnOff(false));

        assertEquals("yes", BooleanUtils.toStringYesNo(Boolean.TRUE));
        assertEquals("no", BooleanUtils.toStringYesNo(Boolean.FALSE));
        assertNull(BooleanUtils.toStringYesNo((Boolean) null));
        assertEquals("yes", BooleanUtils.toStringYesNo(true));
        assertEquals("no", BooleanUtils.toStringYesNo(false));

        assertEquals("Y", BooleanUtils.toString(Boolean.TRUE, "Y", "N", "U"));
        assertEquals("N", BooleanUtils.toString(Boolean.FALSE, "Y", "N", "U"));
        assertEquals("U", BooleanUtils.toString((Boolean) null, "Y", "N", "U"));
        assertEquals("Y", BooleanUtils.toString(true, "Y", "N"));
        assertEquals("N", BooleanUtils.toString(false, "Y", "N"));
    }

    // Tests xor(boolean[])
    @Test
    public void testXor_primitiveArray_returnsExpected() {
        assertTrue(BooleanUtils.xor(new boolean[] { true }));
        assertFalse(BooleanUtils.xor(new boolean[] { false }));
        assertTrue(BooleanUtils.xor(new boolean[] { true, false }));
        assertTrue(BooleanUtils.xor(new boolean[] { false, true }));
        assertFalse(BooleanUtils.xor(new boolean[] { true, true }));
        assertFalse(BooleanUtils.xor(new boolean[] { false, false }));
        assertTrue(BooleanUtils.xor(new boolean[] { false, true, false }));
        assertFalse(BooleanUtils.xor(new boolean[] { true, false, true }));
    }

    // Tests xor(boolean[]) null input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testXor_primitiveArrayNull_throwsException() {
        BooleanUtils.xor((boolean[]) null);
    }

    // Tests xor(boolean[]) empty array throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testXor_primitiveArrayEmpty_throwsException() {
        BooleanUtils.xor(new boolean[] {});
    }

    // Tests xor(Boolean[])
    @Test
    public void testXor_objectArray_returnsExpected() {
        assertEquals(Boolean.TRUE, BooleanUtils.xor(new Boolean[] { Boolean.TRUE, Boolean.FALSE }));
        assertEquals(Boolean.FALSE, BooleanUtils.xor(new Boolean[] { Boolean.TRUE, Boolean.TRUE }));
        assertEquals(Boolean.FALSE, BooleanUtils.xor(new Boolean[] { Boolean.FALSE, Boolean.FALSE }));
    }

    // Tests xor(Boolean[]) null input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testXor_objectArrayNull_throwsException() {
        BooleanUtils.xor((Boolean[]) null);
    }

    // Tests xor(Boolean[]) empty array throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testXor_objectArrayEmpty_throwsException() {
        BooleanUtils.xor(new Boolean[] {});
    }

    // Tests xor(Boolean[]) containing null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testXor_objectArrayWithNull_throwsException() {
        BooleanUtils.xor(new Boolean[] { Boolean.TRUE, null });
    }
}