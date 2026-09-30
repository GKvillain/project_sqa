package com.fasterxml.jackson.databind;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.type.TypeFactory;

public class JavaTypeTest {

    private TypeFactory _typeFactory;

    @Before
    public void setUp() {
        _typeFactory = TypeFactory.defaultInstance();
    }

    // Tests getRawClass returns underlying Class
    @Test
    public void testGetRawClass_validType_returnsCorrectClass() {
        JavaType type = _typeFactory.constructType(String.class);
        assertEquals(String.class, type.getRawClass());
    }

    // Tests hasRawClass matching and non-matching conditions
    @Test
    public void testHasRawClass_sameAndDifferentClass_returnsExpectedBoolean() {
        JavaType type = _typeFactory.constructType(String.class);
        assertTrue(type.hasRawClass(String.class));
        assertFalse(type.hasRawClass(Object.class));
    }

    // Tests primitive type properties: isPrimitive, isConcrete, isAbstract
    @Test
    public void testIsPrimitiveAndIsConcrete_primitiveType_returnsTrue() {
        JavaType type = _typeFactory.constructType(int.class);
        assertTrue(type.isPrimitive());
        assertTrue(type.isConcrete());
        assertFalse(type.isAbstract());
        assertFalse(type.isInterface());
    }

    // Tests interface type properties: isInterface, isAbstract, isConcrete
    @Test
    public void testIsInterfaceAndIsAbstract_interfaceType_returnsTrue() {
        JavaType type = _typeFactory.constructType(List.class);
        assertTrue(type.isInterface());
        assertTrue(type.isAbstract());
        assertFalse(type.isConcrete());
    }

    // Tests concrete final class properties
    @Test
    public void testIsFinalAndIsConcrete_finalClass_returnsTrue() {
        JavaType type = _typeFactory.constructType(String.class);
        assertTrue(type.isFinal());
        assertTrue(type.isConcrete());
        assertFalse(type.isAbstract());
    }

    // Tests isEnumType returns true for enum class
    @Test
    public void testIsEnumType_enumClass_returnsTrue() {
        JavaType type = _typeFactory.constructType(TimeUnit.class);
        assertTrue(type.isEnumType());
        assertFalse(type.isPrimitive());
    }

    // Tests isThrowable returns true for Throwable subclass
    @Test
    public void testIsThrowable_throwableClass_returnsTrue() {
        JavaType type = _typeFactory.constructType(IllegalArgumentException.class);
        assertTrue(type.isThrowable());
        assertFalse(type.isEnumType());
    }

    // Tests isThrowable returns false for non-throwable class
    @Test
    public void testIsThrowable_nonThrowableClass_returnsFalse() {
        JavaType type = _typeFactory.constructType(String.class);
        assertFalse(type.isThrowable());
    }

    // Tests narrowBy when raw class is identical returns same instance
    @Test
    public void testNarrowBy_sameClass_returnsSameInstance() {
        JavaType type = _typeFactory.constructType(CharSequence.class);
        assertSame(type, type.narrowBy(CharSequence.class));
    }

    // Tests narrowBy with valid subclass returns narrowed type and preserves handlers
    @Test
    public void testNarrowBy_validSubclass_returnsNarrowedTypeWithHandlers() {
        JavaType baseType = _typeFactory.constructType(CharSequence.class)
                .withValueHandler("valHandler")
                .withTypeHandler("typeHandler");
        JavaType narrowed = baseType.narrowBy(String.class);
        assertEquals(String.class, narrowed.getRawClass());
        assertEquals("valHandler", narrowed.getValueHandler());
        assertEquals("typeHandler", narrowed.getTypeHandler());
    }

    // Tests narrowBy with invalid non-subclass throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testNarrowBy_invalidSubclass_throwsIllegalArgumentException() {
        JavaType type = _typeFactory.constructType(String.class);
        type.narrowBy(Integer.class);
    }

    // Tests forcedNarrowBy when raw class is identical returns same instance
    @Test
    public void testForcedNarrowBy_sameClass_returnsSameInstance() {
        JavaType type = _typeFactory.constructType(String.class);
        assertSame(type, type.forcedNarrowBy(String.class));
    }

    // Tests forcedNarrowBy with subclass returns narrowed type and preserves handlers
    @Test
    public void testForcedNarrowBy_differentClass_returnsNarrowedTypeWithHandlers() {
        JavaType baseType = _typeFactory.constructType(Object.class)
                .withValueHandler("valHandler")
                .withTypeHandler("typeHandler");
        JavaType forced = baseType.forcedNarrowBy(String.class);
        assertEquals(String.class, forced.getRawClass());
        assertEquals("valHandler", forced.getValueHandler());
        assertEquals("typeHandler", forced.getTypeHandler());
    }

    // Tests widenBy when raw class is identical returns same instance
    @Test
    public void testWidenBy_sameClass_returnsSameInstance() {
        JavaType type = _typeFactory.constructType(String.class);
        assertSame(type, type.widenBy(String.class));
    }

    // Tests widenBy with valid superclass returns widened type
    @Test
    public void testWidenBy_validSuperclass_returnsWidenedType() {
        JavaType type = _typeFactory.constructType( String.class);
        JavaType widened = type.widenBy(CharSequence.class);
        assertEquals(CharSequence.class, widened.getRawClass());
    }

    // Tests widenBy with invalid non-superclass throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWidenBy_invalidSuperclass_throwsIllegalArgumentException() {
        JavaType type = _typeFactory.constructType(String.class);
        type.widenBy(Integer.class);
    }

    // Tests container and collection-like / map-like query methods
    @Test
    public void testIsContainerTypeAndCollectionMapLike_variousTypes_returnsExpected() {
        JavaType stringType = _typeFactory.constructType(String.class);
        assertFalse(stringType.isContainerType());
        assertFalse(stringType.isCollectionLikeType());
        assertFalse(stringType.isMapLikeType());
        assertFalse(stringType.isArrayType());

        JavaType listType = _typeFactory.constructType(List.class);
        assertTrue(listType.isContainerType());
        assertTrue(listType.isCollectionLikeType());
        assertFalse(listType.isMapLikeType());

        JavaType mapType = _typeFactory.constructType(Map.class);
        assertTrue(mapType.isContainerType());
        assertFalse(mapType.isCollectionLikeType());
        assertTrue(mapType.isMapLikeType());
    }

    // Tests containedTypeOrUnknown fallback when contained type index is invalid
    @Test
    public void testContainedTypeOrUnknown_simpleNonContainerType_returnsUnknownType() {
        JavaType stringType = _typeFactory.constructType(String.class);
        assertEquals(0, stringType.containedTypeCount());
        assertNull(stringType.containedType(0));
        assertNull(stringType.containedTypeName(0));
        assertNull(stringType.getKeyType());
        assertNull(stringType.getContentType());
        assertFalse(stringType.hasGenericTypes());

        JavaType unknown = stringType.containedTypeOrUnknown(0);
        assertNotNull(unknown);
        assertEquals(Object.class, unknown.getRawClass());
    }

    // Tests signatures generation without generic info
    @Test
    public void testGetGenericAndErasedSignature_simpleClass_returnsExpectedSignatures() {
        JavaType stringType = _typeFactory.constructType(String.class);
        assertEquals("Ljava/lang/String;", stringType.getGenericSignature());
        assertEquals("Ljava/lang/String;", stringType.getErasedSignature());
    }

    // Tests useStaticType before and after withStaticTyping
    @Test
    public void testWithStaticTyping_invoked_enablesStaticTyping() {
        JavaType normal = _typeFactory.constructType(String.class);
        assertFalse(normal.useStaticType());
        JavaType staticTyped = normal.withStaticTyping();
        assertTrue(staticTyped.useStaticType());
    }

    // Tests hashCode and equals consistency
    @Test
    public void testEqualsAndHashCode_sameAndDifferentTypes_consistentBehavior() {
        JavaType type1 = _typeFactory.constructType(String.class);
        JavaType type2 = _typeFactory.constructType(String.class);
        JavaType type3 = _typeFactory.constructType(Integer.class);

        assertEquals(type1, type2);
        assertEquals(type1.hashCode(), type2.hashCode());
        assertFalse(type1.equals(type3));
        assertFalse(type1.equals(null));
        assertFalse(type1.equals("a string"));
    }

    // Tests isTypeOrSubTypeOf and isTypeOrSuperTypeOf
    @Test
    public void testIsTypeOrSubTypeOfAndSuperTypeOf() {
        JavaType stringType = _typeFactory.constructType(String.class);
        assertTrue(stringType.isTypeOrSubTypeOf(Object.class));
        assertTrue(stringType.isTypeOrSubTypeOf(CharSequence.class));
        assertTrue(stringType.isTypeOrSubTypeOf(String.class));
        assertFalse(stringType.isTypeOrSubTypeOf(Integer.class));

        assertTrue(stringType.isTypeOrSuperTypeOf(String.class));
        assertFalse(stringType.isTypeOrSuperTypeOf(CharSequence.class));
        assertFalse(stringType.isTypeOrSuperTypeOf(Object.class));

        JavaType objectType = _typeFactory.constructType(Object.class);
        assertTrue(objectType.isTypeOrSuperTypeOf(String.class));
    }

    // Tests ArrayType methods (isArrayType, getContentType)
    @Test
    public void testArrayType_propertiesAndContentType() {
        JavaType arrayType = _typeFactory.constructArrayType(String.class);
        assertTrue(arrayType.isArrayType());
        assertTrue(arrayType.isContainerType());
        assertNotNull(arrayType.getContentType());
        assertEquals(String.class, arrayType.getContentType().getRawClass());
    }

    // Tests parameterized Collection and Map types (contained types, key type, content type)
    @Test
    public void testParameterizedTypes_containedTypesAndKeyContentType() {
        JavaType listType = _typeFactory.constructCollectionType(ArrayList.class, String.class);
        assertTrue(listType.hasGenericTypes());
        assertEquals(1, listType.containedTypeCount());
        assertEquals(String.class, listType.containedType(0).getRawClass());
        assertEquals(String.class, listType.getContentType().getRawClass());
        assertEquals(String.class, listType.containedTypeOrUnknown(0).getRawClass());
        assertEquals("E", listType.containedTypeName(0));

        JavaType mapType = _typeFactory.constructMapType(Map.class, String.class, Integer.class);
        assertTrue(mapType.hasGenericTypes());
        assertEquals(2, mapType.containedTypeCount());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(Integer.class, mapType.getContentType().getRawClass());
        assertEquals(String.class, mapType.containedType(0).getRawClass());
        assertEquals(Integer.class, mapType.containedType(1).getRawClass());
    }

    // Tests handlers (hasHandlers, hasValueHandler, getContentValueHandler, getContentTypeHandler)
    @Test
    public void testHandlersAndContentHandlers() {
        JavaType type = _typeFactory.constructCollectionType(ArrayList.class, String.class);
        assertFalse(type.hasHandlers());
        assertFalse(type.hasValueHandler());
        assertNull(type.getValueHandler());
        assertNull(type.getTypeHandler());
        assertNull(type.getContentValueHandler());
        assertNull(type.getContentTypeHandler());

        JavaType withVal = type.withValueHandler("vh");
        assertTrue(withVal.hasValueHandler());
        assertTrue(withVal.hasHandlers());
        assertEquals("vh", withVal.getValueHandler());

        JavaType withTypeH = type.withTypeHandler("th");
        assertTrue(withTypeH.hasHandlers());
        assertEquals("th", withTypeH.getTypeHandler());

        JavaType withContentVal = type.withContentValueHandler("cvh");
        assertTrue(withContentVal.hasHandlers());
        assertEquals("cvh", withContentVal.getContentValueHandler());

        JavaType withContentH = type.withContentTypeHandler("cth");
        assertTrue(withContentH.hasHandlers());
        assertEquals("cth", withContentH.getContentTypeHandler());
    }

    // Tests findSuperType and hierarchy traversal
    @Test
    public void testFindSuperType() {
        JavaType arrayListType = _typeFactory.constructCollectionType(ArrayList.class, String.class);
        JavaType listSuper = arrayListType.findSuperType(List.class);
        assertNotNull(listSuper);
        assertEquals(List.class, listSuper.getRawClass());
        assertEquals(String.class, listSuper.getContentType().getRawClass());

        JavaType nonExistentSuper = arrayListType.findSuperType(Map.class);
        assertNull(nonExistentSuper);

        JavaType sameSuper = arrayListType.findSuperType(ArrayList.class);
        assertSame(arrayListType, sameSuper);
    }

    // Tests getSuperClass and getInterfaces
    @Test
    public void testGetSuperClassAndInterfaces() {
        JavaType arrayListType = _typeFactory.constructType(ArrayList.class);
        JavaType superClass = arrayListType.getSuperClass();
        assertNotNull(superClass);

        List<JavaType> interfaces = arrayListType.getInterfaces();
        assertNotNull(interfaces);
        assertFalse(interfaces.isEmpty());

        JavaType objectType = _typeFactory.constructType(Object.class);
        assertNull(objectType.getSuperClass());
        assertEquals(0, objectType.getInterfaces().size());
    }

    // Tests canonical description and toCanonical
    @Test
    public void testToCanonical() {
        JavaType listType = _typeFactory.constructCollectionType(ArrayList.class, String.class);
        String canonical = listType.toCanonical();
        assertNotNull(canonical);
        assertTrue(canonical.contains("java.util.ArrayList<java.lang.String>"));
    }

    // Tests StringBuilder signature overloads
    @Test
    public void testGetSignaturesWithStringBuilder() {
        JavaType stringType = _typeFactory.constructType(String.class);
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        assertSame(sb1, stringType.getGenericSignature(sb1));
        assertSame(sb2, stringType.getErasedSignature(sb2));
        assertEquals("Ljava/lang/String;", sb1.toString());
        assertEquals("Ljava/lang/String;", sb2.toString());
    }

    // Tests toString
    @Test
    public void testToString() {
        JavaType stringType = _typeFactory.constructType(String.class);
        String str = stringType.toString();
        assertNotNull(str);
        assertTrue(str.contains("java.lang.String"));
    }
}