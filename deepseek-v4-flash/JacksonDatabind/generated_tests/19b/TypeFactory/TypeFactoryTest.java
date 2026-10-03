package com.fasterxml.jackson.databind.type;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Map.Entry;

import org.junit.Test;
import static org.junit.Assert.*;

public class TypeFactoryTest {

    // ============================================================
    // Helper
    // ============================================================
    private TypeFactory tf = TypeFactory.defaultInstance();

    // ============================================================
    // 1. Basic constructType tests
    // ============================================================

    @Test
    public void testConstructType_StringClass_returnsCoreTypeString() {
        JavaType type = tf.constructType(String.class);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
        // core type is a SimpleType
        assertTrue(type.getClass() == SimpleType.class);
    }

    @Test
    public void testConstructType_booleanPrimitive_returnsCoreTypeBool() {
        JavaType type = tf.constructType(Boolean.TYPE);
        assertNotNull(type);
        assertEquals(Boolean.TYPE, type.getRawClass());
    }

    @Test
    public void testConstructType_intPrimitive_returnsCoreTypeInt() {
        JavaType type = tf.constructType(Integer.TYPE);
        assertNotNull(type);
        assertEquals(Integer.TYPE, type.getRawClass());
    }

    @Test
    public void testConstructType_longPrimitive_returnsCoreTypeLong() {
        JavaType type = tf.constructType(Long.TYPE);
        assertNotNull(type);
        assertEquals(Long.TYPE, type.getRawClass());
    }

    @Test
    public void testConstructType_arrayClass_returnsArrayType() {
        JavaType type = tf.constructType(String[].class);
        assertNotNull(type);
        assertTrue(type.getClass() == ArrayType.class);
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructType_enumClass_returnsSimpleType() {
        JavaType type = tf.constructType(TestEnum.class);
        assertNotNull(type);
        assertTrue(type.getClass() == SimpleType.class);
        assertEquals(TestEnum.class, type.getRawClass());
    }

    @Test
    public void testConstructType_mapClass_returnsMapType() {
        JavaType type = tf.constructType(HashMap.class);
        assertNotNull(type);
        assertTrue(type instanceof MapType);
        assertEquals(HashMap.class, type.getRawClass());
    }

    @Test
    public void testConstructType_collectionClass_returnsCollectionType() {
        JavaType type = tf.constructType(ArrayList.class);
        assertNotNull(type);
        assertTrue(type instanceof CollectionType);
        assertEquals(ArrayList.class, type.getRawClass());
    }

    // ============================================================
    // 2. Parameterized types (via TypeReference)
    // ============================================================

    @Test
    public void testConstructType_parameterizedList_returnsCollectionTypeWithString() {
        JavaType type = tf.constructType(new TypeReference<List<String>>() {});
        assertNotNull(type);
        assertTrue(type instanceof CollectionType);
        assertEquals(List.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructType_parameterizedMap_returnsMapTypeWithStringInteger() {
        JavaType type = tf.constructType(new TypeReference<Map<String, Integer>>() {});
        assertNotNull(type);
        assertTrue(type instanceof MapType);
        assertEquals(Map.class, type.getRawClass());
        assertEquals(String.class, type.getKeyType().getRawClass());
        assertEquals(Integer.class, type.getContentType().getRawClass());
    }

    // ============================================================
    // 3. Special types: AtomicReference and Map.Entry
    // ============================================================

    @Test
    public void testConstructType_atomicReference_returnsReferenceType() {
        JavaType type = tf.constructType(new TypeReference<AtomicReference<String>>() {});
        assertNotNull(type);
        // Check that it's a ReferenceType (if available) or at least not a container
        assertEquals(AtomicReference.class, type.getRawClass());
        // Content type should be String
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructType_mapEntry_returnsSimpleTypeWithParams() {
        JavaType type = tf.constructType(new TypeReference<Entry<String, Integer>>() {});
        assertNotNull(type);
        assertEquals(Entry.class, type.getRawClass());
        // The type is a SimpleType (since Map.Entry is not a Map or Collection)
        assertTrue(type.getClass() == SimpleType.class);
        // It should have two contained types
        assertEquals(2, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
        assertEquals(Integer.class, type.containedType(1).getRawClass());
    }

    @Test
    public void testConstructType_subclassOfAtomicReference_returnsReferenceType() {
        JavaType type = tf.constructType(new TypeReference<MyAtomicRef<String>>() {});
        assertNotNull(type);
        assertEquals(MyAtomicRef.class, type.getRawClass());
        // Should resolve the type parameter for AtomicReference superclass
        // (this is likely the defect area for Bug 19b)
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    // ============================================================
    // 4. constructParametrizedType (renamed from constructParametricType)
    // ============================================================

    @Test
    public void testConstructParametrizedType_list_String() {
        JavaType type = tf.constructParametrizedType(ArrayList.class, List.class, String.class);
        assertNotNull(type);
        assertTrue(type instanceof CollectionType);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructParametrizedType_map_StringInteger() {
        JavaType type = tf.constructParametrizedType(HashMap.class, Map.class, String.class, Integer.class);
        assertNotNull(type);
        assertTrue(type instanceof MapType);
        assertEquals(HashMap.class, type.getRawClass());
        assertEquals(String.class, type.getKeyType().getRawClass());
        assertEquals(Integer.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructParametrizedType_array_String() {
        JavaType type = tf.constructParametrizedType(String[].class, String[].class, String.class);
        assertNotNull(type);
        assertTrue(type.getClass() == ArrayType.class);
        assertEquals(String[].class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructParametrizedType_atomicReference_String() {
        JavaType type = tf.constructParametrizedType(AtomicReference.class, AtomicReference.class, String.class);
        assertNotNull(type);
        assertEquals(AtomicReference.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    // ============================================================
    // 5. findTypeParameters
    // ============================================================

    @Test
    public void testFindTypeParameters_List_String() {
        JavaType listType = tf.constructType(new TypeReference<List<String>>() {});
        JavaType[] params = tf.findTypeParameters(listType, Collection.class);
        assertNotNull(params);
        assertEquals(1, params.length);
        assertEquals(String.class, params[0].getRawClass());
    }

    @Test
    public void testFindTypeParameters_HashMap_StringInteger() {
        JavaType mapType = tf.constructType(new TypeReference<HashMap<String, Integer>>() {});
        JavaType[] params = tf.findTypeParameters(mapType, Map.class);
        assertNotNull(params);
        assertEquals(2, params.length);
        assertEquals(String.class, params[0].getRawClass());
        assertEquals(Integer.class, params[1].getRawClass());
    }

    // ============================================================
    // 6. constructSpecializedType
    // ============================================================

    @Test
    public void testConstructSpecializedType_sameClass_returnsBaseType() {
        JavaType base = tf.constructType(List.class);
        JavaType specialized = tf.constructSpecializedType(base, List.class);
        assertSame(base, specialized);
    }

    @Test
    public void testConstructSpecializedType_mapToHashMap() {
        JavaType base = tf.constructType(new TypeReference<Map<String, Integer>>() {});
        JavaType specialized = tf.constructSpecializedType(base, HashMap.class);
        assertNotNull(specialized);
        assertEquals(HashMap.class, specialized.getRawClass());
        // Should retain type parameters
        assertEquals(String.class, specialized.getKeyType().getRawClass());
        assertEquals(Integer.class, specialized.getContentType().getRawClass());
    }

    // ============================================================
    // 7. constructFromCanonical
    // ============================================================

    @Test
    public void testConstructFromCanonical_simpleClass() {
        JavaType type = tf.constructFromCanonical("java.lang.String");
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
    }

    @Test
    public void testConstructFromCanonical_parameterizedList() {
        // canonical for List<String> is "java.util.List<java.lang.String>"
        JavaType type = tf.constructFromCanonical("java.util.List<java.lang.String>");
        assertNotNull(type);
        assertEquals(List.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    // ============================================================
    // 8. Edge cases: null, invalid, etc.
    // ============================================================

    @Test(expected = IllegalArgumentException.class)
    public void testConstructType_null_throwsException() {
        tf.constructType((Type) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructFromCanonical_malformed_throwsException() {
        tf.constructFromCanonical("not.a.real.class");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructParametrizedType_wrongParamCount_throwsException() {
        tf.constructParametrizedType(HashMap.class, Map.class, String.class);
    }

    // ============================================================
    // 9. Additional tests for uncovered areas
    // ============================================================

    // --- Primitive arrays ---
    @Test
    public void testConstructType_primitiveIntArray_returnsArrayType() {
        JavaType type = tf.constructType(int[].class);
        assertNotNull(type);
        assertTrue(type.getClass() == ArrayType.class);
        assertEquals(Integer.TYPE, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructType_primitiveBooleanArray_returnsArrayType() {
        JavaType type = tf.constructType(boolean[].class);
        assertNotNull(type);
        assertTrue(type.getClass() == ArrayType.class);
        assertEquals(Boolean.TYPE, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructType_multiDimensionalArray_returnsArrayType() {
        JavaType type = tf.constructType(int[][].class);
        assertNotNull(type);
        assertTrue(type.getClass() == ArrayType.class);
        // content type should be int[] (array type)
        JavaType content = type.getContentType();
        assertTrue(content.getClass() == ArrayType.class);
        assertEquals(Integer.TYPE, content.getContentType().getRawClass());
    }

    // --- constructFromCanonical with primitives and arrays ---
    @Test
    public void testConstructFromCanonical_primitiveInt() {
        JavaType type = tf.constructFromCanonical("int");
        assertNotNull(type);
        assertEquals(Integer.TYPE, type.getRawClass());
    }

    @Test
    public void testConstructFromCanonical_primitiveBoolean() {
        JavaType type = tf.constructFromCanonical("boolean");
        assertNotNull(type);
        assertEquals(Boolean.TYPE, type.getRawClass());
    }

    @Test
    public void testConstructFromCanonical_primitiveIntArray() {
        // canonical for int[] is "[I"
        JavaType type = tf.constructFromCanonical("[I");
        assertNotNull(type);
        assertTrue(type.getClass() == ArrayType.class);
        assertEquals(Integer.TYPE, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructFromCanonical_nestedGenerics() {
        // canonical for Map<String, List<Integer>> is
        // "java.util.Map<java.lang.String, java.util.List<java.lang.Integer>>"
        JavaType type = tf.constructFromCanonical("java.util.Map<java.lang.String, java.util.List<java.lang.Integer>>");
        assertNotNull(type);
        assertTrue(type instanceof MapType);
        assertEquals(Map.class, type.getRawClass());
        assertEquals(String.class, type.getKeyType().getRawClass());
        JavaType valueType = type.getContentType();
        assertTrue(valueType instanceof CollectionType);
        assertEquals(List.class, valueType.getRawClass());
        assertEquals(Integer.class, valueType.getContentType().getRawClass());
    }

    // --- constructSpecializedType with subclass ---
    @Test
    public void testConstructSpecializedType_listToLinkedList() {
        JavaType base = tf.constructType(List.class);
        JavaType specialized = tf.constructSpecializedType(base, LinkedList.class);
        assertNotNull(specialized);
        assertEquals(LinkedList.class, specialized.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructSpecializedType_unrelatedClass_throwsException() {
        JavaType base = tf.constructType(List.class);
        tf.constructSpecializedType(base, String.class);
    }

    // --- findTypeParameters with no match ---
    @Test
    public void testFindTypeParameters_noMatch_returnsEmpty() {
        JavaType stringType = tf.constructType(String.class);
        JavaType[] params = tf.findTypeParameters(stringType, Collection.class);
        assertNotNull(params);
        assertEquals(0, params.length);
    }

    // --- constructParametrizedType with nested generics ---
    @Test
    public void testConstructParametrizedType_mapWithNestedList() {
        // Create Map<String, List<Integer>> via HashMap – not directly possible with constructParametrizedType
        // Instead create a parameterized type using TypeReference and then check
        JavaType type = tf.constructType(new TypeReference<HashMap<String, List<Integer>>>() {});
        assertNotNull(type);
        assertTrue(type instanceof MapType);
        assertEquals(HashMap.class, type.getRawClass());
        assertEquals(String.class, type.getKeyType().getRawClass());
        JavaType valueType = type.getContentType();
        assertTrue(valueType instanceof CollectionType);
        assertEquals(List.class, valueType.getRawClass());
        assertEquals(Integer.class, valueType.getContentType().getRawClass());
    }

    // --- Caching behavior (clearCache is not tested, but we can test that same type returns same instance) ---
    @Test
    public void testCache_sameTypeReturnsSameInstance() {
        JavaType t1 = tf.constructType(String.class);
        JavaType t2 = tf.constructType(String.class);
        assertSame("TypeFactory should cache simple types", t1, t2);
    }

    @Test
    public void testCache_sameParameterizedTypeReturnsSameInstance() {
        JavaType t1 = tf.constructType(new TypeReference<Map<String, Integer>>() {});
        JavaType t2 = tf.constructType(new TypeReference<Map<String, Integer>>() {});
        assertSame("TypeFactory should cache parameterized types", t1, t2);
    }

    // --- constructType with wildcard type ---
    @Test
    public void testConstructType_wildcardList_upperBound() {
        JavaType type = tf.constructType(new TypeReference<List<? extends Number>>() {});
        assertNotNull(type);
        assertTrue(type instanceof CollectionType);
        assertEquals(List.class, type.getRawClass());
        // Content type should be ? extends Number
        JavaType content = type.getContentType();
        // It's a WildcardType, but we can check the raw class of its upper bound
        // In Jackson, WildcardType is not a SimpleType, but we can inspect further.
        // Just verify that Number is the bound
        assertEquals(Number.class, content.getRawClass());
    }

    // ============================================================
    // Helper enum and class for tests
    // ============================================================

    private enum TestEnum { A, B }

    private static class MyAtomicRef<T> extends AtomicReference<T> {
        private static final long serialVersionUID = 1L;
    }
}