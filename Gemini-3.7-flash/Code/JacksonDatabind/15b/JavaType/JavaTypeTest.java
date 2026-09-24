package com.fasterxml.jackson.databind;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.type.TypeFactory;

public class JavaTypeTest {

    private final TypeFactory _tf = TypeFactory.defaultInstance();

    // Tests getRawClass and hasRawClass on simple type
    @Test
    public void testGetRawClass_simpleClass_returnsExpectedClass() {
        JavaType type = _tf.constructType(String.class);
        assertEquals(String.class, type.getRawClass());
        assertTrue(type.hasRawClass(String.class));
        assertFalse(type.hasRawClass(Integer.class));
    }

    // Tests isConcrete, isAbstract, isInterface, and isFinal on concrete final class
    @Test
    public void testTypeModifiers_finalClass_returnsCorrectFlags() {
        JavaType type = _tf.constructType(String.class);
        assertTrue(type.isConcrete());
        assertFalse(type.isAbstract());
        assertFalse(type.isInterface());
        assertTrue(type.isFinal());
        assertFalse(type.isPrimitive());
        assertFalse(type.isEnumType());
    }

    // Tests modifiers for interface type
    @Test
    public void testTypeModifiers_interface_returnsCorrectFlags() {
        JavaType type = _tf.constructType(CharSequence.class);
        assertFalse(type.isConcrete());
        assertTrue(type.isAbstract());
        assertTrue(type.isInterface());
        assertFalse(type.isFinal());
    }

    // Tests modifiers for abstract class
    @Test
    public void testTypeModifiers_abstractClass_returnsCorrectFlags() {
        JavaType type = _tf.constructType(Number.class);
        assertFalse(type.isConcrete());
        assertTrue(type.isAbstract());
        assertFalse(type.isInterface());
    }

    // Tests primitive types being concrete and primitive
    @Test
    public void testTypeModifiers_primitiveType_returnsConcreteAndPrimitive() {
        JavaType type = _tf.constructType(int.class);
        assertTrue(type.isConcrete());
        assertTrue(type.isPrimitive());
        assertFalse(type.isInterface());
    }

    // Tests isThrowable for Throwable subtypes and non-throwable types
    @Test
    public void testIsThrowable_throwableAndNonThrowable_returnsExpected() {
        JavaType exType = _tf.constructType(Exception.class);
        JavaType strType = _tf.constructType(String.class);
        assertTrue(exType.isThrowable());
        assertFalse(strType.isThrowable());
    }

    // Tests isEnumType for Enum types
    @Test
    public void testIsEnumType_enumClass_returnsTrue() {
        JavaType enumType = _tf.constructType(Thread.State.class);
        assertTrue(enumType.isEnumType());
    }

    // Tests narrowBy when passing the same class returns the same instance
    @Test
    public void testNarrowBy_sameClass_returnsSameInstance() {
        JavaType type = _tf.constructType(Number.class);
        JavaType narrowed = type.narrowBy(Number.class);
        assertSame(type, narrowed);
    }

    // Tests narrowBy with valid subclass narrows correctly
    @Test
    public void testNarrowBy_subclass_returnsNarrowedType() {
        JavaType type = _tf.constructType(Number.class);
        JavaType narrowed = type.narrowBy(Integer.class);
        assertEquals(Integer.class, narrowed.getRawClass());
    }

    // Tests narrowBy with incompatible class throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testNarrowBy_incompatibleClass_throwsIllegalArgumentException() {
        JavaType type = _tf.constructType(Number.class);
        type.narrowBy(String.class);
    }

    // Tests forcedNarrowBy when passing the same class returns the same instance
    @Test
    public void testForcedNarrowBy_sameClass_returnsSameInstance() {
        JavaType type = _tf.constructType(Number.class);
        JavaType narrowed = type.forcedNarrowBy(Number.class);
        assertSame(type, narrowed);
    }

    // Tests forcedNarrowBy with subclass narrows correctly
    @Test
    public void testForcedNarrowBy_subclass_returnsNarrowedType() {
        JavaType type = _tf.constructType(Number.class);
        JavaType narrowed = type.forcedNarrowBy(Integer.class);
        assertEquals(Integer.class, narrowed.getRawClass());
    }

    // Tests widenBy when passing the same class returns the same instance
    @Test
    public void testWidenBy_sameClass_returnsSameInstance() {
        JavaType type = _tf.constructType(Integer.class);
        JavaType widened = type.widenBy(Integer.class);
        assertSame(type, widened);
    }

    // Tests widenBy with valid superclass widens correctly
    @Test
    public void testWidenBy_superClass_returnsWidenedType() {
        JavaType type = _tf.constructType(Integer.class);
        JavaType widened = type.widenBy(Number.class);
        assertEquals(Number.class, widened.getRawClass());
    }

    // Tests widenBy with incompatible class throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWidenBy_incompatibleClass_throwsIllegalArgumentException() {
        JavaType type = _tf.constructType(Integer.class);
        type.widenBy(String.class);
    }

    // Tests handler preservation during narrowBy
    @Test
    public void testNarrowBy_withHandlers_preservesHandlers() {
        JavaType type = _tf.constructType(Number.class);
        Object vHandler = "valueHandler";
        Object tHandler = "typeHandler";
        type = type.withValueHandler(vHandler).withTypeHandler(tHandler);

        JavaType narrowed = type.narrowBy(Integer.class);
        assertEquals(vHandler, narrowed.getValueHandler());
        assertEquals(tHandler, narrowed.getTypeHandler());
    }

    // Tests default implementations of type parameter methods for simple types
    @Test
    public void testContainedTypes_simpleType_returnsDefaults() {
        JavaType type = _tf.constructType(String.class);
        assertFalse(type.hasGenericTypes());
        assertEquals(0, type.containedTypeCount());
        assertNull(type.containedType(0));
        assertNull(type.containedTypeName(0));
        assertNull(type.getKeyType());
        assertNull(type.getContentType());
        assertFalse(type.isArrayType());
        assertFalse(type.isCollectionLikeType());
        assertFalse(type.isMapLikeType());
    }

    // Tests containedTypeOrUnknown fallback when contained type is null
    @Test
    public void testContainedTypeOrUnknown_noContainedType_returnsUnknownType() {
        JavaType type = _tf.constructType(String.class);
        JavaType unknown = type.containedTypeOrUnknown(0);
        assertNotNull(unknown);
        assertEquals(Object.class, unknown.getRawClass());
    }

    // Tests containedTypeOrUnknown returns actual type when present
    @Test
    public void testContainedTypeOrUnknown_withContainedType_returnsContainedType() {
        JavaType type = _tf.constructParametricType(List.class, String.class);
        JavaType contained = type.containedTypeOrUnknown(0);
        assertNotNull(contained);
        assertEquals(String.class, contained.getRawClass());
    }

    // Tests signature generation methods
    @Test
    public void testSignatures_simpleType_returnsNonEmptySignatures() {
        JavaType type = _tf.constructType(String.class);
        String genericSig = type.getGenericSignature();
        String erasedSig = type.getErasedSignature();
        assertNotNull(genericSig);
        assertNotNull(erasedSig);
        assertTrue(genericSig.contains("java/lang/String"));
        assertTrue(erasedSig.contains("java/lang/String"));
    }

    // Tests hashCode consistency
    @Test
    public void testHashCode_sameType_returnsEqualHashCode() {
        JavaType type1 = _tf.constructType(String.class);
        JavaType type2 = _tf.constructType(String.class);
        assertEquals(type1.hashCode(), type2.hashCode());
    }
}