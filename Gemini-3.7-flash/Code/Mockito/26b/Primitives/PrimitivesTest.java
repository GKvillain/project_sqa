package org.mockito.internal.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PrimitivesTest {

    // Tests primitiveTypeOf with primitive class input (true branch of isPrimitive)
    @Test
    public void testPrimitiveTypeOf_primitiveInput_returnsSameType() {
        assertEquals(int.class, Primitives.primitiveTypeOf(int.class));
        assertEquals(boolean.class, Primitives.primitiveTypeOf(boolean.class));
    }

    // Tests primitiveTypeOf with wrapper class input (false branch of isPrimitive)
    @Test
    public void testPrimitiveTypeOf_wrapperInput_returnsPrimitiveType() {
        assertEquals(int.class, Primitives.primitiveTypeOf(Integer.class));
        assertEquals(boolean.class, Primitives.primitiveTypeOf(Boolean.class));
        assertEquals(double.class, Primitives.primitiveTypeOf(Double.class));
        assertEquals(long.class, Primitives.primitiveTypeOf(Long.class));
        assertEquals(float.class, Primitives.primitiveTypeOf(Float.class));
        assertEquals(byte.class, Primitives.primitiveTypeOf(Byte.class));
        assertEquals(short.class, Primitives.primitiveTypeOf(Short.class));
        assertEquals(char.class, Primitives.primitiveTypeOf(Character.class));
    }

    // Tests primitiveTypeOf with non-primitive and non-wrapper input
    @Test
    public void testPrimitiveTypeOf_nonPrimitiveOrWrapper_returnsNull() {
        assertNull(Primitives.primitiveTypeOf(String.class));
        assertNull(Primitives.primitiveTypeOf(Object.class));
    }

    // Tests isPrimitiveWrapper with wrapper types
    @Test
    public void testIsPrimitiveWrapper_wrapperClass_returnsTrue() {
        assertTrue(Primitives.isPrimitiveWrapper(Integer.class));
        assertTrue(Primitives.isPrimitiveWrapper(Boolean.class));
        assertTrue(Primitives.isPrimitiveWrapper(Character.class));
        assertTrue(Primitives.isPrimitiveWrapper(Byte.class));
        assertTrue(Primitives.isPrimitiveWrapper(Short.class));
        assertTrue(Primitives.isPrimitiveWrapper(Long.class));
        assertTrue(Primitives.isPrimitiveWrapper(Float.class));
        assertTrue(Primitives.isPrimitiveWrapper(Double.class));
    }

    // Tests isPrimitiveWrapper with primitive types
    @Test
    public void testIsPrimitiveWrapper_primitiveClass_returnsFalse() {
        assertFalse(Primitives.isPrimitiveWrapper(int.class));
        assertFalse(Primitives.isPrimitiveWrapper(boolean.class));
        assertFalse(Primitives.isPrimitiveWrapper(double.class));
    }

    // Tests isPrimitiveWrapper with non-primitive and non-wrapper class
    @Test
    public void testIsPrimitiveWrapper_nonWrapperClass_returnsFalse() {
        assertFalse(Primitives.isPrimitiveWrapper(String.class));
        assertFalse(Primitives.isPrimitiveWrapper(Object.class));
    }

    // Tests primitiveWrapperOf with wrapper types
    @Test
    public void testPrimitiveWrapperOf_wrapperTypes_returnsDefaultValues() {
        assertEquals(false, Primitives.primitiveWrapperOf(Boolean.class));
        assertEquals(Character.valueOf('\u0000'), Primitives.primitiveWrapperOf(Character.class));
        assertEquals(Byte.valueOf((byte) 0), Primitives.primitiveWrapperOf(Byte.class));
        assertEquals(Short.valueOf((short) 0), Primitives.primitiveWrapperOf(Short.class));
        assertEquals(Integer.valueOf(0), Primitives.primitiveWrapperOf(Integer.class));
        assertEquals(Long.valueOf(0L), Primitives.primitiveWrapperOf(Long.class));
        assertEquals(Float.valueOf(0F), Primitives.primitiveWrapperOf(Float.class));
        assertEquals(Double.valueOf(0D), Primitives.primitiveWrapperOf(Double.class));
    }

    // Tests primitiveWrapperOf with non-wrapper class
    @Test
    public void testPrimitiveWrapperOf_nonWrapperClass_returnsNull() {
        assertNull(Primitives.primitiveWrapperOf(String.class));
    }

    // Tests primitiveValueOrNullFor with all primitive types including double (Defects4J bug 26b check)
    @Test
    public void testPrimitiveValueOrNullFor_primitiveTypes_returnsDefaultValues() {
        assertEquals(false, Primitives.primitiveValueOrNullFor(boolean.class));
        assertEquals(Character.valueOf('\u0000'), Primitives.primitiveValueOrNullFor(char.class));
        assertEquals(Byte.valueOf((byte) 0), Primitives.primitiveValueOrNullFor(byte.class));
        assertEquals(Short.valueOf((short) 0), Primitives.primitiveValueOrNullFor(short.class));
        assertEquals(Integer.valueOf(0), Primitives.primitiveValueOrNullFor(int.class));
        assertEquals(Long.valueOf(0L), Primitives.primitiveValueOrNullFor(long.class));
        assertEquals(Float.valueOf(0F), Primitives.primitiveValueOrNullFor(float.class));
        assertEquals(Double.valueOf(0D), Primitives.primitiveValueOrNullFor(double.class));
    }

    // Tests primitiveValueOrNullFor with non-primitive type
    @Test
    public void testPrimitiveValueOrNullFor_nonPrimitiveType_returnsNull() {
        assertNull(Primitives.primitiveValueOrNullFor(String.class));
        assertNull(Primitives.primitiveValueOrNullFor(Integer.class));
    }

    // Tests instantiation of Primitives utility class
    @Test
    public void testConstructor_instantiation_success() {
        Primitives primitives = new Primitives();
        org.junit.Assert.assertNotNull(primitives);
    }
}