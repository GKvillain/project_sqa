package org.mockito.internal.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class PrimitivesTest {

    // Tests primitiveValueOrNullFor with double primitive type (Bug 26 defect detection)
    @Test
    public void testPrimitiveValueOrNullFor_doubleType_returnsDoubleZero() {
        Double result = Primitives.primitiveValueOrNullFor(double.class);
        assertEquals(Double.valueOf(0D), result);
    }

    // Tests primitiveValueOrNullFor with int primitive type
    @Test
    public void testPrimitiveValueOrNullFor_intType_returnsIntegerZero() {
        Integer result = Primitives.primitiveValueOrNullFor(int.class);
        assertEquals(Integer.valueOf(0), result);
    }

    // Tests primitiveValueOrNullFor with boolean primitive type
    @Test
    public void testPrimitiveValueOrNullFor_booleanType_returnsFalse() {
        Boolean result = Primitives.primitiveValueOrNullFor(boolean.class);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests primitiveValueOrNullFor with char primitive type
    @Test
    public void testPrimitiveValueOrNullFor_charType_returnsNullChar() {
        Character result = Primitives.primitiveValueOrNullFor(char.class);
        assertEquals(Character.valueOf('\u0000'), result);
    }

    // Tests primitiveValueOrNullFor with byte primitive type
    @Test
    public void testPrimitiveValueOrNullFor_byteType_returnsByteZero() {
        Byte result = Primitives.primitiveValueOrNullFor(byte.class);
        assertEquals(Byte.valueOf((byte) 0), result);
    }

    // Tests primitiveValueOrNullFor with short primitive type
    @Test
    public void testPrimitiveValueOrNullFor_shortType_returnsShortZero() {
        Short result = Primitives.primitiveValueOrNullFor(short.class);
        assertEquals(Short.valueOf((short) 0), result);
    }

    // Tests primitiveValueOrNullFor with long primitive type
    @Test
    public void testPrimitiveValueOrNullFor_longType_returnsLongZero() {
        Long result = Primitives.primitiveValueOrNullFor(long.class);
        assertEquals(Long.valueOf(0L), result);
    }

    // Tests primitiveValueOrNullFor with float primitive type
    @Test
    public void testPrimitiveValueOrNullFor_floatType_returnsFloatZero() {
        Float result = Primitives.primitiveValueOrNullFor(float.class);
        assertEquals(Float.valueOf(0F), result);
    }

    // Tests primitiveValueOrNullFor with non-primitive type returns null
    @Test
    public void testPrimitiveValueOrNullFor_nonPrimitiveType_returnsNull() {
        String result = Primitives.primitiveValueOrNullFor(String.class);
        assertNull(result);
    }

    // Tests primitiveTypeOf with primitive input (true branch of isPrimitive)
    @Test
    public void testPrimitiveTypeOf_primitiveType_returnsSameType() {
        Class<Integer> result = Primitives.primitiveTypeOf(int.class);
        assertEquals(int.class, result);
    }

    // Tests primitiveTypeOf with wrapper input (false branch of isPrimitive)
    @Test
    public void testPrimitiveTypeOf_wrapperType_returnsPrimitiveType() {
        Class<Integer> result = Primitives.primitiveTypeOf(Integer.class);
        assertEquals(int.class, result);

        Class<Double> doubleResult = Primitives.primitiveTypeOf(Double.class);
        assertEquals(double.class, doubleResult);

        Class<Boolean> boolResult = Primitives.primitiveTypeOf(Boolean.class);
        assertEquals(boolean.class, boolResult);
    }

    // Tests primitiveTypeOf with non-wrapper reference type returns null
    @Test
    public void testPrimitiveTypeOf_nonWrapperType_returnsNull() {
        Class<String> result = Primitives.primitiveTypeOf(String.class);
        assertNull(result);
    }

    // Tests isPrimitiveWrapper with wrapper type returns true
    @Test
    public void testIsPrimitiveWrapper_wrapperType_returnsTrue() {
        assertTrue(Primitives.isPrimitiveWrapper(Integer.class));
        assertTrue(Primitives.isPrimitiveWrapper(Double.class));
        assertTrue(Primitives.isPrimitiveWrapper(Boolean.class));
        assertTrue(Primitives.isPrimitiveWrapper(Byte.class));
        assertTrue(Primitives.isPrimitiveWrapper(Character.class));
        assertTrue(Primitives.isPrimitiveWrapper(Short.class));
        assertTrue(Primitives.isPrimitiveWrapper(Long.class));
        assertTrue(Primitives.isPrimitiveWrapper(Float.class));
    }

    // Tests isPrimitiveWrapper with primitive type returns false
    @Test
    public void testIsPrimitiveWrapper_primitiveType_returnsFalse() {
        assertFalse(Primitives.isPrimitiveWrapper(int.class));
        assertFalse(Primitives.isPrimitiveWrapper(double.class));
        assertFalse(Primitives.isPrimitiveWrapper(boolean.class));
    }

    // Tests isPrimitiveWrapper with non-wrapper reference type returns false
    @Test
    public void testIsPrimitiveWrapper_nonWrapperType_returnsFalse() {
        assertFalse(Primitives.isPrimitiveWrapper(String.class));
        assertFalse(Primitives.isPrimitiveWrapper(Object.class));
    }

    // Tests primitiveWrapperOf with wrapper types returns default values
    @Test
    public void testPrimitiveWrapperOf_wrapperType_returnsDefaultValue() {
        assertEquals(Integer.valueOf(0), Primitives.primitiveWrapperOf(Integer.class));
        assertEquals(Double.valueOf(0D), Primitives.primitiveWrapperOf(Double.class));
        assertEquals(Boolean.FALSE, Primitives.primitiveWrapperOf(Boolean.class));
        assertEquals(Character.valueOf('\u0000'), Primitives.primitiveWrapperOf(Character.class));
        assertEquals(Byte.valueOf((byte) 0), Primitives.primitiveWrapperOf(Byte.class));
        assertEquals(Short.valueOf((short) 0), Primitives.primitiveWrapperOf(Short.class));
        assertEquals(Long.valueOf(0L), Primitives.primitiveWrapperOf(Long.class));
        assertEquals(Float.valueOf(0F), Primitives.primitiveWrapperOf(Float.class));
    }

    // Tests primitiveWrapperOf with non-wrapper reference type returns null
    @Test
    public void testPrimitiveWrapperOf_nonWrapperType_returnsNull() {
        assertNull(Primitives.primitiveWrapperOf(String.class));
    }
}