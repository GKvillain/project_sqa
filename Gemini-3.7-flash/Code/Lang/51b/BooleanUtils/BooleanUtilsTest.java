package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

public class BooleanUtilsTest {

    // Tests constructor
    @Test
    public void testConstructor_default_instantiatesSuccessfully() {
        assertNotNull(new BooleanUtils());
    }

    // Tests negate with null, true, and false
    @Test
    public void testNegate_variousInputs_returnsExpectedResult() {
        assertNull(BooleanUtils.negate(null));
        assertEquals(Boolean.FALSE, BooleanUtils.negate(Boolean.TRUE));
        assertEquals(Boolean.TRUE, BooleanUtils.negate(Boolean.FALSE));
    }

    // Tests isTrue, isNotTrue, isFalse, isNotFalse with null and boolean values
    @Test
    public void testBooleanChecks_variousInputs_returnsExpectedResult() {
        assertFalse(BooleanUtils.isTrue(null));
        assertTrue(BooleanUtils.isTrue(Boolean.TRUE));
        assertFalse(BooleanUtils.isTrue(Boolean.FALSE));

        assertTrue(BooleanUtils.isNotTrue(null));
        assertFalse(BooleanUtils.isNotTrue(Boolean.TRUE));
        assertTrue(BooleanUtils.isNotTrue(Boolean.FALSE));

        assertFalse(BooleanUtils.isFalse(null));
        assertFalse(BooleanUtils.isFalse(Boolean.TRUE));
        assertTrue(BooleanUtils.isFalse(Boolean.FALSE));

        assertTrue(BooleanUtils.isNotFalse(null));
        assertTrue(BooleanUtils.isNotFalse(Boolean.TRUE));
        assertFalse(BooleanUtils.isNotFalse(Boolean.FALSE));
    }

    // Tests toBoolean and toBooleanObject conversions for primitives and objects
    @Test
    public void testToBooleanAndToBooleanObject_primitivesAndObjects_returnsExpectedResult() {
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

    // Tests int/Integer to boolean/Boolean conversions
    @Test
    public void testToBoolean_intAndInteger_returnsExpectedResult() {
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
        assertTrue(BooleanUtils.toBoolean((Integer) null, null, Integer.valueOf(0)));
        assertFalse(BooleanUtils.toBoolean((Integer) null, Integer.valueOf(1), null));

        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(1, 1, 0, -1));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(0, 1, 0, -1));
        assertNull(BooleanUtils.toBooleanObject(-1, 1, 0, -1));

        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(-1)));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(-1)));
        assertNull(BooleanUtils.toBooleanObject(Integer.valueOf(-1), Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(-1)));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject((Integer) null, null, Integer.valueOf(0), Integer.valueOf(-1)));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject((Integer) null, Integer.valueOf(1), null, Integer.valueOf(-1)));
        assertNull(BooleanUtils.toBooleanObject((Integer) null, Integer.valueOf(1), Integer.valueOf(0), null));
    }

    // Tests int/Integer to boolean with no matching value throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testToBoolean_intNoMatch_throwsException() {
        BooleanUtils.toBoolean(2, 1, 0);
    }

    // Tests Integer to boolean with no matching value throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testToBoolean_integerNoMatch_throwsException() {
        BooleanUtils.toBoolean(Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(0));
    }

    // Tests int to BooleanObject with no matching value throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testToBooleanObject_intNoMatch_throwsException() {
        BooleanUtils.toBooleanObject(2, 1, 0, -1);
    }

    // Tests Integer to BooleanObject with no matching value throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testToBooleanObject_integerNoMatch_throwsException() {
        BooleanUtils.toBooleanObject(Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(-1));
    }

    // Tests boolean/Boolean to int/Integer conversions
    @Test
    public void testToIntegerAndToIntegerObject_variousInputs_returnsExpectedResult() {
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
        assertEquals(30, BooleanUtils.toInteger((Boolean) null, 10, 20, 30));

        assertEquals(Integer.valueOf(10), BooleanUtils.toIntegerObject(true, Integer.valueOf(10), Integer.valueOf(20)));
        assertEquals(Integer.valueOf(20), BooleanUtils.toIntegerObject(false, Integer.valueOf(10), Integer.valueOf(20)));

        assertEquals(Integer.valueOf(10), BooleanUtils.toIntegerObject(Boolean.TRUE, Integer.valueOf(10), Integer.valueOf(20), Integer.valueOf(30)));
        assertEquals(Integer.valueOf(20), BooleanUtils.toIntegerObject(Boolean.FALSE, Integer.valueOf(10), Integer.valueOf(20), Integer.valueOf(30)));
        assertEquals(Integer.valueOf(30), BooleanUtils.toIntegerObject((Boolean) null, Integer.valueOf(10), Integer.valueOf(20), Integer.valueOf(30)));
    }

    // Tests String to BooleanObject standard values
    @Test
    public void testToBooleanObject_stringStandardValues_returnsExpectedResult() {
        assertNull(BooleanUtils.toBooleanObject((String) null));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("true"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("TRUE"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("false"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("FALSE"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("on"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("ON"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("off"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("OFF"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("yes"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("YES"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("no"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("NO"));
        assertNull(BooleanUtils.toBooleanObject("other"));
    }

    // Tests String to BooleanObject custom values
    @Test
    public void testToBooleanObject_stringCustomValues_returnsExpectedResult() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("Y", "Y", "N", "U"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("N", "Y", "N", "U"));
        assertNull(BooleanUtils.toBooleanObject("U", "Y", "N", "U"));

        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(null, null, "N", "U"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(null, "Y", null, "U"));
        assertNull(BooleanUtils.toBooleanObject(null, "Y", "N", null));
    }

    // Tests String to BooleanObject with unmatched value throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testToBooleanObject_stringNoMatch_throwsException() {
        BooleanUtils.toBooleanObject("X", "Y", "N", "U");
    }

    // Tests String to boolean for valid true, false, and non-boolean strings (including defect Lang-51)
    @Test
    public void testToBoolean_stringVariations_returnsExpectedResult() {
        assertFalse(BooleanUtils.toBoolean((String) null));
        assertTrue(BooleanUtils.toBoolean("true"));
        assertTrue(BooleanUtils.toBoolean("TRUE"));
        assertTrue(BooleanUtils.toBoolean("True"));
        assertTrue(BooleanUtils.toBoolean("tRue"));
        assertTrue(BooleanUtils.toBoolean("on"));
        assertTrue(BooleanUtils.toBoolean("ON"));
        assertTrue(BooleanUtils.toBoolean("On"));
        assertTrue(BooleanUtils.toBoolean("yes"));
        assertTrue(BooleanUtils.toBoolean("YES"));
        assertTrue(BooleanUtils.toBoolean("Yes"));
        assertTrue(BooleanUtils.toBoolean("yEs"));

        assertFalse(BooleanUtils.toBoolean(""));
        assertFalse(BooleanUtils.toBoolean("a"));
        assertFalse(BooleanUtils.toBoolean("no"));
        assertFalse(BooleanUtils.toBoolean("off"));
        assertFalse(BooleanUtils.toBoolean("false"));
        assertFalse(BooleanUtils.toBoolean("other"));

        // Tests 3-char strings that are not "yes" (Lang-51 defect: fall-through without break)
        assertFalse(BooleanUtils.toBoolean("tru"));
        assertFalse(BooleanUtils.toBoolean("abc"));
        assertFalse(BooleanUtils.toBoolean("foo"));

        // Tests 4-char strings that are not "true"
        assertFalse(BooleanUtils.toBoolean("tree"));
        assertFalse(BooleanUtils.toBoolean("abcd"));
    }

    // Tests String to boolean custom values
    @Test
    public void testToBoolean_stringCustomValues_returnsExpectedResult() {
        assertTrue(BooleanUtils.toBoolean("Y", "Y", "N"));
        assertFalse(BooleanUtils.toBoolean("N", "Y", "N"));
        assertTrue(BooleanUtils.toBoolean(null, null, "N"));
        assertFalse(BooleanUtils.toBoolean(null, "Y", null));
    }

    // Tests String to boolean with unmatched value throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testToBoolean_stringCustomValuesNoMatch_throwsException() {
        BooleanUtils.toBoolean("X", "Y", "N");
    }

    // Tests Boolean/boolean to String conversions
    @Test
    public void testToStringMethods_variousInputs_returnsExpectedResult() {
        assertEquals("true", BooleanUtils.toStringTrueFalse(Boolean.TRUE));
        assertEquals("false", BooleanUtils.toStringTrueFalse(Boolean.FALSE));
        assertNull(BooleanUtils.toStringTrueFalse((Boolean) null));

        assertEquals("on", BooleanUtils.toStringOnOff(Boolean.TRUE));
        assertEquals("off", BooleanUtils.toStringOnOff(Boolean.FALSE));
        assertNull(BooleanUtils.toStringOnOff((Boolean) null));

        assertEquals("yes", BooleanUtils.toStringYesNo(Boolean.TRUE));
        assertEquals("no", BooleanUtils.toStringYesNo(Boolean.FALSE));
        assertNull(BooleanUtils.toStringYesNo((Boolean) null));

        assertEquals("Y", BooleanUtils.toString(Boolean.TRUE, "Y", "N", "U"));
        assertEquals("N", BooleanUtils.toString(Boolean.FALSE, "Y", "N", "U"));
        assertEquals("U", BooleanUtils.toString((Boolean) null, "Y", "N", "U"));

        assertEquals("true", BooleanUtils.toStringTrueFalse(true));
        assertEquals("false", BooleanUtils.toStringTrueFalse(false));

        assertEquals("on", BooleanUtils.toStringOnOff(true));
        assertEquals("off", BooleanUtils.toStringOnOff(false));

        assertEquals("yes", BooleanUtils.toStringYesNo(true));
        assertEquals("no", BooleanUtils.toStringYesNo(false));

        assertEquals("Y", BooleanUtils.toString(true, "Y", "N"));
        assertEquals("N", BooleanUtils.toString(false, "Y", "N"));
    }

    // Tests boolean primitive array XOR
    @Test
    public void testXor_primitiveArray_returnsExpectedResult() {
        assertTrue(BooleanUtils.xor(new boolean[]{true}));
        assertFalse(BooleanUtils.xor(new boolean[]{false}));
        assertTrue(BooleanUtils.xor(new boolean[]{true, false}));
        assertTrue(BooleanUtils.xor(new boolean[]{false, true}));
        assertFalse(BooleanUtils.xor(new boolean[]{false, false}));
        assertFalse(BooleanUtils.xor(new boolean[]{true, true}));
        assertTrue(BooleanUtils.xor(new boolean[]{false, false, true}));
        assertFalse(BooleanUtils.xor(new boolean[]{true, false, true}));
    }

    // Tests boolean primitive array XOR with null array throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testXor_primitiveArrayNull_throwsException() {
        BooleanUtils.xor((boolean[]) null);
    }

    // Tests boolean primitive array XOR with empty array throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testXor_primitiveArrayEmpty_throwsException() {
        BooleanUtils.xor(new boolean[]{});
    }

    // Tests Boolean object array XOR
    @Test
    public void testXor_objectArray_returnsExpectedResult() {
        assertEquals(Boolean.TRUE, BooleanUtils.xor(new Boolean[]{Boolean.TRUE}));
        assertEquals(Boolean.FALSE, BooleanUtils.xor(new Boolean[]{Boolean.FALSE}));
        assertEquals(Boolean.TRUE, BooleanUtils.xor(new Boolean[]{Boolean.TRUE, Boolean.FALSE}));
        assertEquals(Boolean.FALSE, BooleanUtils.xor(new Boolean[]{Boolean.TRUE, Boolean.TRUE}));
        assertEquals(Boolean.FALSE, BooleanUtils.xor(new Boolean[]{Boolean.FALSE, Boolean.FALSE}));
    }

    // Tests Boolean object array XOR with null array throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testXor_objectArrayNull_throwsException() {
        BooleanUtils.xor((Boolean[]) null);
    }

    // Tests Boolean object array XOR with empty array throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testXor_objectArrayEmpty_throwsException() {
        BooleanUtils.xor(new Boolean[]{});
    }

    // Tests Boolean object array XOR with null element throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testXor_objectArrayContainsNull_throwsException() {
        BooleanUtils.xor(new Boolean[]{Boolean.TRUE, null});
    }
}