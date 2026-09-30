package org.mockito.internal.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class PrimitivesTest {

    // Tests primitiveTypeOf with primitive input returns the same primitive class
    @Test
    public void testPrimitiveTypeOf_primitiveClass_returnsSameClass() {
        assertSame(boolean.class, Primitives.primitiveTypeOf(boolean.class));
        assertSame(int.class, Primitives.primitiveTypeOf(int.class));
        assertSame(double.class, Primitives.primitiveTypeOf(double.class));
    }

    // Tests primitiveTypeOf with wrapper input returns matching primitive class
    @Test
    public void testPrimitiveTypeOf_wrapperClass_returnsPrimitiveClass() {
        assertSame(Boolean.TYPE, Primitives.primitiveTypeOf(Boolean.class));
        assertSame(Character.TYPE, Primitives.primitiveTypeOf(Character.class));
        assertSame(Byte.TYPE, Primitives.primitiveTypeOf(Byte.class));
        assertSame(Short.TYPE, Primitives.primitiveTypeOf(Short.class));
        assertSame(Integer.TYPE, Primitives.primitiveTypeOf(Integer.class));
        assertSame(Long.TYPE, Primitives.primitiveTypeOf(Long.class));
        assertSame(Float.TYPE, Primitives.primitiveTypeOf(Float.class));
        assertSame(Double.TYPE, Primitives.primitiveTypeOf(Double.class));
    }

    // Tests primitiveTypeOf with non-wrapper/non-primitive class returns null
    @Test
    public void testPrimitiveTypeOf_nonPrimitiveClass_returnsNull() {
        assertNull(Primitives.primitiveTypeOf(String.class));
    }

    // Tests primitiveTypeOf with null input throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testPrimitiveTypeOf_nullInput_throwsNullPointerException() {
        Primitives.<Object>primitiveTypeOf(null);
    }

    // Tests isPrimitiveWrapper with all primitive wrapper classes returns true
    @Test
    public void testIsPrimitiveWrapper_wrapperClass_returnsTrue() {
        assertTrue(Primitives.isPrimitiveWrapper(Boolean.class));
        assertTrue(Primitives.isPrimitiveWrapper(Character.class));
        assertTrue(Primitives.isPrimitiveWrapper(Byte.class));
        assertTrue(Primitives.isPrimitiveWrapper(Short.class));
        assertTrue(Primitives.isPrimitiveWrapper(Integer.class));
        assertTrue(Primitives.isPrimitiveWrapper(Long.class));
        assertTrue(Primitives.isPrimitiveWrapper(Float.class));
        assertTrue(Primitives.isPrimitiveWrapper(Double.class));
    }

    // Tests isPrimitiveWrapper with primitive classes returns false
    @Test
    public void testIsPrimitiveWrapper_primitiveClass_returnsFalse() {
        assertFalse(Primitives.isPrimitiveWrapper(boolean.class));
        assertFalse(Primitives.isPrimitiveWrapper(int.class));
        assertFalse(Primitives.isPrimitiveWrapper(double.class));
    }

    // Tests isPrimitiveWrapper with non-wrapper class and null returns false
    @Test
    public void testIsPrimitiveWrapper_nonWrapperClass_returnsFalse() {
        assertFalse(Primitives.isPrimitiveWrapper(String.class));
        assertFalse(Primitives.isPrimitiveWrapper(null));
    }

    // Tests primitiveWrapperOf with all wrapper classes returns default wrapper values
    @Test
    public void testPrimitiveWrapperOf_wrapperClass_returnsDefaultValue() {
        assertEquals(Boolean.FALSE, Primitives.primitiveWrapperOf(Boolean.class));
        assertEquals(Character.valueOf('\u0000'), Primitives.primitiveWrapperOf(Character.class));
        assertEquals(Byte.valueOf((byte) 0), Primitives.primitiveWrapperOf(Byte.class));
        assertEquals(Short.valueOf((short) 0), Primitives.primitiveWrapperOf(Short.class));
        assertEquals(Integer.valueOf(0), Primitives.primitiveWrapperOf(Integer.class));
        assertEquals(Long.valueOf(0L), Primitives.primitiveWrapperOf(Long.class));
        assertEquals(Float.valueOf(0.0F), Primitives.primitiveWrapperOf(Float.class));
        assertEquals(Double.valueOf(0.0D), Primitives.primitiveWrapperOf(Double.class));
    }

    // Tests primitiveWrapperOf with non-wrapper class returns null
    @Test
    public void testPrimitiveWrapperOf_nonWrapperClass_returnsNull() {
        assertNull(Primitives.primitiveWrapperOf(String.class));
    }

    // Tests primitiveValueOrNullFor with non-double primitives returns default primitive values
    @Test
    public void testPrimitiveValueOrNullFor_booleanAndNumericPrimitives_returnsDefaultValue() {
        assertEquals(Boolean.FALSE, Primitives.primitiveValueOrNullFor(boolean.class));
        assertEquals(Character.valueOf('\u0000'), Primitives.primitiveValueOrNullFor(char.class));
        assertEquals(Byte.valueOf((byte) 0), Primitives.primitiveValueOrNullFor(byte.class));
        assertEquals(Short.valueOf((short) 0), Primitives.primitiveValueOrNullFor(short.class));
        assertEquals(Integer.valueOf(0), Primitives.primitiveValueOrNullFor(int.class));
        assertEquals(Long.valueOf(0L), Primitives.primitiveValueOrNullFor(long.class));
        assertEquals(Float.valueOf(0.0F), Primitives.primitiveValueOrNullFor(float.class));
    }

    // Tests primitiveValueOrNullFor(double.class) returns Double zero, not Integer zero
    @Test
    public void testPrimitiveValueOrNullFor_doublePrimitive_returnsZeroDouble() {
        Object result = Primitives.primitiveValueOrNullFor(double.class);
        assertEquals(Double.valueOf(0.0D), result);
    }

    // Tests primitiveValueOrNullFor with non-primitive class returns null
    @Test
    public void testPrimitiveValueOrNullFor_nonPrimitiveClass_returnsNull() {
        assertNull(Primitives.primitiveValueOrNullFor(String.class));
    }

    // Tests primitiveValueOrNullFor with null input returns null
    @Test
    public void testPrimitiveValueOrNullFor_nullInput_returnsNull() {
        assertNull(Primitives.<Object>primitiveValueOrNullFor(null));
    }
}