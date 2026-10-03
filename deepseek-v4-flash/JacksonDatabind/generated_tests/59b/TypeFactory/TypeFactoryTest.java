package com.fasterxml.jackson.databind.type;

import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;

public class TypeFactoryTest {

    private final TypeFactory factory = TypeFactory.defaultInstance();

    // ===== Test เดิม =====

    // Test constructType with String.class returns SimpleType
    @Test
    public void testConstructType_StringClass_returnsSimpleType() {
        JavaType type = factory.constructType(String.class);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
        assertTrue(type.isContainerType() == false); // SimpleType not container
    }

    // Test constructType with primitive int.class returns SimpleType
    @Test
    public void testConstructType_PrimitiveInt_returnsSimpleType() {
        JavaType type = factory.constructType(Integer.TYPE);
        assertNotNull(type);
        assertEquals(Integer.TYPE, type.getRawClass());
    }

    // Test constructType with Object.class returns core type
    @Test
    public void testConstructType_ObjectClass_returnsCoreTypeObject() {
        JavaType type = factory.constructType(Object.class);
        assertNotNull(type);
        assertEquals(Object.class, type.getRawClass());
    }

    // Test constructType with array type returns ArrayType
    @Test
    public void testConstructType_ArrayClass_returnsArrayType() {
        JavaType type = factory.constructType(String[].class);
        assertTrue(type.isArrayType());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    // Test constructType with ParameterizedType (List<String>) returns CollectionType
    @Test
    public void testConstructType_ParametricList_returnsCollectionType() {
        JavaType type = factory.constructType(new TypeReference<List<String>>() {}.getType());
        assertTrue(type.isCollectionLikeType());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    // Test constructType with ParameterizedType (Map<String,Integer>) returns MapType
    @Test
    public void testConstructType_ParametricMap_returnsMapType() {
        JavaType type = factory.constructType(new TypeReference<Map<String, Integer>>() {}.getType());
        assertTrue(type.isMapLikeType());
        assertEquals(String.class, type.getKeyType().getRawClass());
        assertEquals(Integer.class, type.getContentType().getRawClass());
    }

    // Test constructArrayType with element class
    @Test
    public void testConstructArrayType_ElementClass_returnsArrayType() {
        JavaType elementType = factory.constructType(Long.class);
        JavaType arrayType = factory.constructArrayType(elementType);
        assertTrue(arrayType.isArrayType());
        assertEquals(Long.class, arrayType.getContentType().getRawClass());
    }

    // Test constructCollectionType with ArrayList and element class
    @Test
    public void testConstructCollectionType_ArrayListString_returnsCollectionType() {
        JavaType type = factory.constructCollectionType(ArrayList.class, String.class);
        assertTrue(type instanceof CollectionType);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    // Test constructMapType with HashMap and key/value classes
    @Test
    public void testConstructMapType_HashMapStringInteger_returnsMapType() {
        JavaType type = factory.constructMapType(HashMap.class, String.class, Integer.class);
        assertTrue(type instanceof MapType);
        assertEquals(HashMap.class, type.getRawClass());
        assertEquals(String.class, type.getKeyType().getRawClass());
        assertEquals(Integer.class, type.getContentType().getRawClass());
    }

    // Test constructSpecializedType with valid subclass (List<String> to ArrayList)
    @Test
    public void testConstructSpecializedType_ListToArrayList_returnsSpecialized() {
        JavaType baseType = factory.constructType(new TypeReference<List<String>>() {}.getType());
        JavaType specialized = factory.constructSpecializedType(baseType, ArrayList.class);
        assertEquals(ArrayList.class, specialized.getRawClass());
        assertEquals(String.class, specialized.getContentType().getRawClass());
    }

    // Test constructSpecializedType with invalid subclass throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructSpecializedType_InvalidSubclass_throwsException() {
        JavaType baseType = factory.constructType(String.class);
        factory.constructSpecializedType(baseType, ArrayList.class);
    }

    // Test constructGeneralizedType with superclass (ArrayList to List)
    @Test
    public void testConstructGeneralizedType_ArrayListToList_returnsGeneralized() {
        JavaType baseType = factory.constructType(ArrayList.class);
        JavaType generalized = factory.constructGeneralizedType(baseType, List.class);
        assertEquals(List.class, generalized.getRawClass());
    }

    // Test constructFromCanonical valid string
    @Test
    public void testConstructFromCanonical_Valid_returnsType() {
        JavaType type = factory.constructFromCanonical("java.util.List<java.lang.String>");
        assertTrue(type.isCollectionLikeType());
        assertEquals(List.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    // Test constructFromCanonical invalid string throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructFromCanonical_InvalidString_throwsException() {
        factory.constructFromCanonical("invalid");
    }

    // Test findClass with valid class name
    @Test
    public void testFindClass_ValidClassName_returnsClass() throws Exception {
        Class<?> clazz = factory.findClass("java.lang.String");
        assertEquals(String.class, clazz);
    }

    // Test findClass with primitive name
    @Test
    public void testFindClass_PrimitiveName_returnsPrimitive() throws Exception {
        Class<?> clazz = factory.findClass("int");
        assertEquals(Integer.TYPE, clazz);
    }

    // Test findClass with invalid name throws ClassNotFoundException
    @Test(expected = ClassNotFoundException.class)
    public void testFindClass_InvalidName_throwsException() throws Exception {
        factory.findClass("com.nonexistent.Foo");
    }

    // Test constructParametricType with Class<?>...
    @Test
    public void testConstructParametricType_ArrayListString_returnsType() {
        JavaType type = factory.constructParametricType(ArrayList.class, String.class);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    // Test constructReferenceType for AtomicReference
    @Test
    public void testConstructReferenceType_AtomicReference_returnsReferenceType() {
        JavaType referred = factory.constructType(String.class);
        JavaType refType = factory.constructReferenceType(AtomicReference.class, referred);
        assertTrue(refType.isReferenceType());
        assertEquals(AtomicReference.class, refType.getRawClass());
        assertEquals(String.class, refType.getReferencedType().getRawClass());
    }

    // Test moreSpecificType with related types returns more specific
    @Test
    public void testMoreSpecificType_Related_returnsSpecific() {
        JavaType type1 = factory.constructType(Collection.class);
        JavaType type2 = factory.constructType(List.class);
        JavaType result = factory.moreSpecificType(type1, type2);
        assertEquals(List.class, result.getRawClass());
    }

    // Test clearCache does not throw
    @Test
    public void testClearCache_NoException() {
        factory.clearCache();
        // No assertion, just verifies no exception
    }

    // Test constructRawCollectionType returns raw type with unknown content
    @Test
    public void testConstructRawCollectionType_ArrayList_returnsRaw() {
        CollectionType raw = factory.constructRawCollectionType(ArrayList.class);
        assertEquals(ArrayList.class, raw.getRawClass());
        assertEquals(Object.class, raw.getContentType().getRawClass());
    }

    // Test constructRawMapType returns raw map with unknown key/value
    @Test
    public void testConstructRawMapType_HashMap_returnsRaw() {
        MapType raw = factory.constructRawMapType(HashMap.class);
        assertEquals(HashMap.class, raw.getRawClass());
        assertEquals(Object.class, raw.getKeyType().getRawClass());
        assertEquals(Object.class, raw.getContentType().getRawClass());
    }

    // ===== Test ใหม่ที่เพิ่มเพื่อครอบคลุมส่วนที่ขาด =====

    // Test constructType with void.class
    @Test
    public void testConstructType_VoidClass_returnsSimpleType() {
        JavaType type = factory.constructType(void.class);
        assertNotNull(type);
        assertEquals(void.class, type.getRawClass());
    }

    // Test constructType with boolean.class
    @Test
    public void testConstructType_BooleanClass_returnsSimpleType() {
        JavaType type = factory.constructType(boolean.class);
        assertNotNull(type);
        assertEquals(boolean.class, type.getRawClass());
    }

    // Test constructType with an enum class
    enum TestEnum { A, B }
    @Test
    public void testConstructType_EnumClass_returnsSimpleType() {
        JavaType type = factory.constructType(TestEnum.class);
        assertNotNull(type);
        assertEquals(TestEnum.class, type.getRawClass());
        assertTrue(type.isEnumType());
    }

    // Test constructType with primitive array (int[])
    @Test
    public void testConstructType_PrimitiveArray_returnsArrayType() {
        JavaType type = factory.constructType(int[].class);
        assertTrue(type.isArrayType());
        assertEquals(int.class, type.getContentType().getRawClass());
    }

    // Test constructType with raw HashMap (no generics) -> should produce MapType
    @Test
    public void testConstructType_RawHashMap_returnsMapType() {
        JavaType type = factory.constructType(HashMap.class);
        assertTrue(type.isMapLikeType());
        assertEquals(HashMap.class, type.getRawClass());
        // Raw map should have Object key/value
        assertEquals(Object.class, type.getKeyType().getRawClass());
        assertEquals(Object.class, type.getContentType().getRawClass());
    }

    // Test constructType with TypeReference containing wildcard (? extends Number)
    @Test
    public void testConstructType_TypeReferenceWithWildcard_returnsCollectionType() {
        JavaType type = factory.constructType(new TypeReference<List<? extends Number>>() {}.getType());
        assertTrue(type.isCollectionLikeType());
        // Content type should be some bounded type, but we can check raw class is Number
        assertEquals(Number.class, type.getContentType().getRawClass());
    }

    // Test constructFromCanonical with array type
    @Test
    public void testConstructFromCanonical_ArrayType_returnsArrayType() {
        JavaType type = factory.constructFromCanonical("[Ljava.lang.String;");
        assertTrue(type.isArrayType());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    // Test constructFromCanonical with primitive name
    @Test
    public void testConstructFromCanonical_Primitive_returnsSimpleType() {
        JavaType type = factory.constructFromCanonical("int");
        assertEquals(int.class, type.getRawClass());
    }

    // Test findClass for all primitive types
    @Test
    public void testFindClass_Void_returnsPrimitive() throws Exception {
        assertEquals(void.class, factory.findClass("void"));
    }

    @Test
    public void testFindClass_Boolean_returnsPrimitive() throws Exception {
        assertEquals(boolean.class, factory.findClass("boolean"));
    }

    @Test
    public void testFindClass_Byte_returnsPrimitive() throws Exception {
        assertEquals(byte.class, factory.findClass("byte"));
    }

    @Test
    public void testFindClass_Short_returnsPrimitive() throws Exception {
        assertEquals(short.class, factory.findClass("short"));
    }

    @Test
    public void testFindClass_Long_returnsPrimitive() throws Exception {
        assertEquals(long.class, factory.findClass("long"));
    }

    @Test
    public void testFindClass_Float_returnsPrimitive() throws Exception {
        assertEquals(float.class, factory.findClass("float"));
    }

    @Test
    public void testFindClass_Double_returnsPrimitive() throws Exception {
        assertEquals(double.class, factory.findClass("double"));
    }

    // Test constructParametricType with Map (two type parameters)
    @Test
    public void testConstructParametricType_MapStringInteger_returnsMapType() {
        JavaType type = factory.constructParametricType(HashMap.class, String.class, Integer.class);
        assertTrue(type instanceof MapType);
        assertEquals(String.class, type.getKeyType().getRawClass());
        assertEquals(Integer.class, type.getContentType().getRawClass());
    }

    // Test constructSpecializedType with array type (String[] to CharSequence[])
    @Test
    public void testConstructSpecializedType_ArrayToSuperType_returnsSpecialized() {
        JavaType arrayType = factory.constructType(String[].class);
        // Specialize to CharSequence[] (String implements CharSequence)
        JavaType specialized = factory.constructSpecializedType(arrayType, CharSequence[].class);
        assertEquals(CharSequence[].class, specialized.getRawClass());
        assertEquals(CharSequence.class, specialized.getContentType().getRawClass());
    }

    // Test constructGeneralizedType with Collection to Iterable
    @Test
    public void testConstructGeneralizedType_CollectionToIterable_returnsGeneralized() {
        JavaType baseType = factory.constructType(ArrayList.class);
        JavaType generalized = factory.constructGeneralizedType(baseType, Iterable.class);
        assertEquals(Iterable.class, generalized.getRawClass());
    }

    // Test constructType with AtomicReference (raw) -> should produce ReferenceType
    @Test
    public void testConstructType_AtomicReferenceRaw_returnsReferenceType() {
        JavaType type = factory.constructType(AtomicReference.class);
        assertTrue(type.isReferenceType());
        assertEquals(AtomicReference.class, type.getRawClass());
        // Raw reference should have Object as referenced
        assertEquals(Object.class, type.getReferencedType().getRawClass());
    }

    // Test constructType with nested generic (Map<String, List<Integer>>)
    @Test
    public void testConstructType_NestedGeneric_returnsMapType() {
        JavaType type = factory.constructType(new TypeReference<Map<String, List<Integer>>>() {}.getType());
        assertTrue(type.isMapLikeType());
        assertEquals(String.class, type.getKeyType().getRawClass());
        JavaType valueType = type.getContentType();
        assertTrue(valueType.isCollectionLikeType());
        assertEquals(List.class, valueType.getRawClass());
        assertEquals(Integer.class, valueType.getContentType().getRawClass());
    }
}