package com.fasterxml.jackson.databind.type;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;

import static org.junit.Assert.*;

public class TypeFactoryTest {

    private TypeFactory _tf;

    // Helper classes for testing
    static class StringIntMap extends HashMap<String, Integer> {
        private static final long serialVersionUID = 1L;
    }

    static class CustomMapLike<K, V> {
        private K key;
        private V value;
    }

    static class CustomCollectionLike<E> {
        private E element;
    }

    static class GenericHolder<T> {
        public T value;
    }

    static class MultiGenericHolder<A, B> {
        public A first;
        public B second;
    }

    static class CustomEntry<K, V> implements Map.Entry<K, V> {
        private K k;
        private V v;
        @Override public K getKey() { return k; }
        @Override public V getValue() { return v; }
        @Override public V setValue(V value) { this.v = value; return v; }
    }

    static class WildcardHolder {
        public List<?> wildcardList;
        public List<? extends Number> upperBoundList;
        public List<? super Integer> lowerBoundList;
        public GenericHolder<String>[] genericArray;
    }

    static class SubGenericHolder extends GenericHolder<String> {
    }

    @Before
    public void setUp() {
        _tf = TypeFactory.defaultInstance();
        _tf.clearCache();
    }

    // Tests defect in constructMapLikeType with Class arguments for non-Map class
    @Test
    public void testConstructMapLikeType_withClasses_returnsMapLikeType() {
        JavaType type = _tf.constructMapLikeType(CustomMapLike.class, String.class, Integer.class);
        assertNotNull(type);
        assertTrue(type instanceof MapLikeType);
        assertEquals(CustomMapLike.class, type.getRawClass());
        assertEquals(String.class, type.getKeyType().getRawClass());
        assertEquals(Integer.class, type.getContentType().getRawClass());
    }

    // Tests constructMapLikeType with JavaType arguments
    @Test
    public void testConstructMapLikeType_withJavaTypes_returnsMapLikeType() {
        JavaType keyType = _tf.constructType(String.class);
        JavaType valType = _tf.constructType(Long.class);
        JavaType type = _tf.constructMapLikeType(CustomMapLike.class, keyType, valType);
        assertNotNull(type);
        assertTrue(type instanceof MapLikeType);
        assertEquals(CustomMapLike.class, type.getRawClass());
        assertEquals(String.class, type.getKeyType().getRawClass());
        assertEquals(Long.class, type.getContentType().getRawClass());
    }

    // Tests core primitive and simple types caching
    @Test
    public void testConstructType_coreTypes_returnsCachedInstances() {
        JavaType stringType = _tf.constructType(String.class);
        assertSame(TypeFactory.CORE_TYPE_STRING, stringType);

        JavaType boolType = _tf.constructType(Boolean.TYPE);
        assertSame(TypeFactory.CORE_TYPE_BOOL, boolType);

        JavaType intType = _tf.constructType(Integer.TYPE);
        assertSame(TypeFactory.CORE_TYPE_INT, intType);

        JavaType longType = _tf.constructType(Long.TYPE);
        assertSame(TypeFactory.CORE_TYPE_LONG, longType);
    }

    // Tests constructArrayType with Class and JavaType
    @Test
    public void testConstructArrayType_validInputs_returnsArrayType() {
        ArrayType fromClass = _tf.constructArrayType(String.class);
        assertNotNull(fromClass);
        assertTrue(fromClass.isArrayType());
        assertEquals(String.class, fromClass.getContentType().getRawClass());

        JavaType intType = _tf.constructType(Integer.class);
        ArrayType fromJavaType = _tf.constructArrayType(intType);
        assertNotNull(fromJavaType);
        assertEquals(Integer.class, fromJavaType.getContentType().getRawClass());
    }

    // Tests constructCollectionType and raw collection construction
    @Test
    public void testConstructCollectionType_validInput_returnsCollectionType() {
        CollectionType type = _tf.constructCollectionType(ArrayList.class, String.class);
        assertNotNull(type);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());

        CollectionType rawType = _tf.constructRawCollectionType(List.class);
        assertNotNull(rawType);
        assertEquals(Object.class, rawType.getContentType().getRawClass());
    }

    // Tests constructCollectionLikeType and raw collection-like type
    @Test
    public void testConstructCollectionLikeType_validInput_returnsCollectionLikeType() {
        CollectionLikeType type = _tf.constructCollectionLikeType(CustomCollectionLike.class, String.class);
        assertNotNull(type);
        assertEquals(CustomCollectionLike.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());

        CollectionLikeType rawType = _tf.constructRawCollectionLikeType(CustomCollectionLike.class);
        assertNotNull(rawType);
        assertEquals(Object.class, rawType.getContentType().getRawClass());
    }

    // Tests constructMapType and raw map construction
    @Test
    public void testConstructMapType_validInput_returnsMapType() {
        MapType type = _tf.constructMapType(HashMap.class, String.class, Integer.class);
        assertNotNull(type);
        assertEquals(HashMap.class, type.getRawClass());
        assertEquals(String.class, type.getKeyType().getRawClass());
        assertEquals(Integer.class, type.getContentType().getRawClass());

        MapType rawType = _tf.constructRawMapType(Map.class);
        assertNotNull(rawType);
        assertEquals(Object.class, rawType.getKeyType().getRawClass());
        assertEquals(Object.class, rawType.getContentType().getRawClass());
    }

    // Tests constructSpecializedType for sub-classing Map
    @Test
    public void testConstructSpecializedType_mapSubclass_preservesGenericInfo() {
        JavaType baseType = _tf.constructType(Map.class);
        JavaType specialized = _tf.constructSpecializedType(baseType, StringIntMap.class);
        assertEquals(StringIntMap.class, specialized.getRawClass());
        assertEquals(String.class, specialized.getKeyType().getRawClass());
        assertEquals(Integer.class, specialized.getContentType().getRawClass());
    }

    // Tests constructSpecializedType when raw class is the same
    @Test
    public void testConstructSpecializedType_sameClass_returnsSameType() {
        JavaType baseType = _tf.constructType(String.class);
        JavaType specialized = _tf.constructSpecializedType(baseType, String.class);
        assertSame(baseType, specialized);
    }

    // Tests constructSpecializedType with invalid sub-class hierarchy
    @Test(expected = IllegalArgumentException.class)
    public void testConstructSpecializedType_incompatibleSubclass_throwsException() {
        JavaType baseType = _tf.constructType(List.class);
        _tf.constructSpecializedType(baseType, Map.class);
    }

    // Tests constructFromCanonical with simple and nested types
    @Test
    public void testConstructFromCanonical_validString_returnsJavaType() {
        JavaType type = _tf.constructFromCanonical("java.util.List<java.lang.String>");
        assertNotNull(type);
        assertEquals(List.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    // Tests constructFromCanonical with invalid canonical name
    @Test(expected = IllegalArgumentException.class)
    public void testConstructFromCanonical_unknownClass_throwsException() {
        _tf.constructFromCanonical("com.nonexistent.FakeClass");
    }

    // Tests findTypeParameters for sub-types
    @Test
    public void testFindTypeParameters_mapSubclass_returnsResolvedTypeParameters() {
        JavaType[] params = _tf.findTypeParameters(StringIntMap.class, Map.class);
        assertNotNull(params);
        assertEquals(2, params.length);
        assertEquals(String.class, params[0].getRawClass());
        assertEquals(Integer.class, params[1].getRawClass());
    }

    // Tests findTypeParameters with not-a-subtype exception
    @Test(expected = IllegalArgumentException.class)
    public void testFindTypeParameters_notSubtype_throwsException() {
        _tf.findTypeParameters(String.class, List.class);
    }

    // Tests moreSpecificType comparison
    @Test
    public void testMoreSpecificType_variousCases_returnsExpectedType() {
        JavaType listType = _tf.constructType(List.class);
        JavaType arrayListType = _tf.constructType(ArrayList.class);
        JavaType stringType = _tf.constructType(String.class);

        assertSame(arrayListType, _tf.moreSpecificType(listType, arrayListType));
        assertSame(arrayListType, _tf.moreSpecificType(arrayListType, listType));
        assertSame(listType, _tf.moreSpecificType(listType, stringType));
        assertSame(listType, _tf.moreSpecificType(listType, null));
        assertSame(listType, _tf.moreSpecificType(null, listType));
    }

    // Tests constructType with TypeReference
    @Test
    public void testConstructType_typeReference_returnsResolvedType() {
        TypeReference<List<String>> ref = new TypeReference<List<String>>() {};
        JavaType type = _tf.constructType(ref);
        assertNotNull(type);
        assertEquals(List.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    // Tests constructParametrizedType with Class parameters
    @Test
    public void testConstructParametrizedType_validParameters_returnsConstructedType() {
        JavaType type = _tf.constructParametrizedType(GenericHolder.class, GenericHolder.class, String.class);
        assertNotNull(type);
        assertEquals(GenericHolder.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    // Tests constructParametrizedType parameter count mismatch exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructParametrizedType_mismatchedParams_throwsException() {
        _tf.constructParametrizedType(GenericHolder.class, GenericHolder.class, String.class, Integer.class);
    }

    // Tests static helpers: rawClass and unknownType
    @Test
    public void testRawClassAndUnknownType() {
        Class<?> raw = TypeFactory.rawClass(String.class);
        assertEquals(String.class, raw);

        JavaType unknown = TypeFactory.unknownType();
        assertNotNull(unknown);
        assertEquals(Object.class, unknown.getRawClass());
    }

    // Tests AtomicReference and Map.Entry resolution
    @Test
    public void testConstructType_specialReferentialTypes_returnsCorrectType() {
        JavaType refType = _tf.constructType(new TypeReference<AtomicReference<String>>() {});
        assertNotNull(refType);
        assertEquals(AtomicReference.class, refType.getRawClass());

        JavaType entryType = _tf.constructType(new TypeReference<CustomEntry<String, Integer>>() {});
        assertNotNull(entryType);
        assertEquals(CustomEntry.class, entryType.getRawClass());
    }

    // Tests withModifier functionality
    @Test
    public void testWithModifier_nullModifier_returnsSameConfiguredInstance() {
        TypeFactory modified = _tf.withModifier(null);
        assertNotNull(modified);
    }

    // --- New Tests for Full Coverage ---

    @Test
    public void testConstructAllPrimitives() {
        Class<?>[] primitives = new Class<?>[] {
            byte.class, short.class, char.class, float.class, double.class, void.class
        };
        for (Class<?> prim : primitives) {
            JavaType type = _tf.constructType(prim);
            assertNotNull(type);
            assertTrue(type.isPrimitive());
            assertEquals(prim, type.getRawClass());
        }
    }

    @Test
    public void testConstructGeneralizedType() {
        JavaType subType = _tf.constructType(SubGenericHolder.class);
        JavaType generalized = _tf.constructGeneralizedType(subType, GenericHolder.class);
        assertNotNull(generalized);
        assertEquals(GenericHolder.class, generalized.getRawClass());
        assertEquals(String.class, generalized.containedType(0).getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructGeneralizedType_notSuperClass_throwsException() {
        JavaType stringType = _tf.constructType(String.class);
        _tf.constructGeneralizedType(stringType, List.class);
    }

    @Test
    public void testConstructParametricType_withJavaTypes() {
        JavaType stringType = _tf.constructType(String.class);
        JavaType integerType = _tf.constructType(Integer.class);
        JavaType type = _tf.constructParametricType(MultiGenericHolder.class, stringType, integerType);
        assertNotNull(type);
        assertEquals(MultiGenericHolder.class, type.getRawClass());
        assertEquals(2, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
        assertEquals(Integer.class, type.containedType(1).getRawClass());
    }

    @Test
    public void testConstructParametricType_collectionsAndMaps() {
        JavaType listType = _tf.constructParametricType(ArrayList.class, String.class);
        assertTrue(listType.isCollectionLikeType());
        assertEquals(String.class, listType.getContentType().getRawClass());

        JavaType mapType = _tf.constructParametricType(HashMap.class, String.class, Integer.class);
        assertTrue(mapType.isMapLikeType());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(Integer.class, mapType.getContentType().getRawClass());
    }

    @Test
    public void testConstructFromCanonical_arrayAndPrimitives() {
        JavaType arrayType = _tf.constructFromCanonical("[Ljava.lang.String;");
        assertTrue(arrayType.isArrayType());
        assertEquals(String.class, arrayType.getContentType().getRawClass());

        JavaType intType = _tf.constructFromCanonical("int");
        assertEquals(int.class, intType.getRawClass());

        JavaType boolType = _tf.constructFromCanonical("boolean");
        assertEquals(boolean.class, boolType.getRawClass());
    }

    @Test
    public void testConstructFromCanonical_nestedMapAndList() {
        JavaType type = _tf.constructFromCanonical("java.util.HashMap<java.lang.String,java.util.ArrayList<java.lang.Integer>>");
        assertNotNull(type);
        assertTrue(type.isMapLikeType());
        assertEquals(HashMap.class, type.getRawClass());
        assertEquals(String.class, type.getKeyType().getRawClass());

        JavaType valueType = type.getContentType();
        assertTrue(valueType.isCollectionLikeType());
        assertEquals(ArrayList.class, valueType.getRawClass());
        assertEquals(Integer.class, valueType.getContentType().getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructFromCanonical_malformedSyntax_throwsException() {
        _tf.constructFromCanonical("java.util.List<java.lang.String");
    }

    @Test
    public void testConstructType_withContextClassAndBindings() throws Exception {
        Field field = GenericHolder.class.getField("value");
        Type genericFieldType = field.getGenericType(); // TypeVariable T

        JavaType resolved = _tf.constructType(genericFieldType, SubGenericHolder.class);
        assertNotNull(resolved);
        assertEquals(String.class, resolved.getRawClass());

        JavaType contextType = _tf.constructType(SubGenericHolder.class);
        JavaType resolvedFromContextType = _tf.constructType(genericFieldType, contextType);
        assertNotNull(resolvedFromContextType);
        assertEquals(String.class, resolvedFromContextType.getRawClass());
    }

    @Test
    public void testConstructType_wildcardsAndGenericArrays() throws Exception {
        Field wildcardField = WildcardHolder.class.getField("wildcardList");
        JavaType wcType = _tf.constructType(wildcardField.getGenericType());
        assertEquals(Object.class, wcType.getContentType().getRawClass());

        Field upperBoundField = WildcardHolder.class.getField("upperBoundList");
        JavaType ubType = _tf.constructType(upperBoundField.getGenericType());
        assertEquals(Number.class, ubType.getContentType().getRawClass());

        Field lowerBoundField = WildcardHolder.class.getField("lowerBoundList");
        JavaType lbType = _tf.constructType(lowerBoundField.getGenericType());
        assertEquals(Integer.class, lbType.getContentType().getRawClass());

        Field genArrayField = WildcardHolder.class.getField("genericArray");
        JavaType arrayType = _tf.constructType(genArrayField.getGenericType());
        assertTrue(arrayType.isArrayType());
        assertEquals(GenericHolder.class, arrayType.getContentType().getRawClass());
    }

    @Test
    public void testWithClassLoader() {
        ClassLoader cl = getClass().getClassLoader();
        TypeFactory customTf = _tf.withClassLoader(cl);
        assertNotNull(customTf);
        assertEquals(cl, customTf.getClassLoader());

        TypeFactory sameTf = customTf.withClassLoader(cl);
        assertSame(customTf, sameTf);
    }

    @Test
    public void testWithModifier_customTypeModifier() {
        TypeModifier modifier = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                if (type.hasRawClass(ArrayList.class)) {
                    return typeFactory.constructType(LinkedList.class);
                }
                return type;
            }
        };

        TypeFactory modifiedTf = _tf.withModifier(modifier);
        assertNotNull(modifiedTf);
        JavaType type = modifiedTf.constructType(ArrayList.class);
        assertEquals(LinkedList.class, type.getRawClass());
    }

    @Test
    public void testConstructReferenceType() {
        JavaType stringType = _tf.constructType(String.class);
        JavaType refType = _tf.constructReferenceType(AtomicReference.class, stringType);
        assertNotNull(refType);
        assertTrue(refType.isReferenceType());
        assertEquals(AtomicReference.class, refType.getRawClass());
        assertEquals(String.class, refType.getContentType().getRawClass());
    }

    @Test
    public void testConstructSimpleType() {
        JavaType[] paramTypes = new JavaType[] { _tf.constructType(String.class) };
        JavaType type = _tf.constructSimpleType(GenericHolder.class, paramTypes);
        assertNotNull(type);
        assertEquals(GenericHolder.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test
    public void testConstructRawMapLikeType() {
        JavaType type = _tf.constructRawMapLikeType(CustomMapLike.class);
        assertNotNull(type);
        assertTrue(type instanceof MapLikeType);
        assertEquals(CustomMapLike.class, type.getRawClass());
        assertEquals(Object.class, type.getKeyType().getRawClass());
        assertEquals(Object.class, type.getContentType().getRawClass());
    }

    @Test
    public void testFindTypeParameters_withJavaTypeContext() {
        JavaType specialized = _tf.constructType(StringIntMap.class);
        JavaType[] params = _tf.findTypeParameters(specialized, Map.class);
        assertNotNull(params);
        assertEquals(2, params.length);
        assertEquals(String.class, params[0].getRawClass());
        assertEquals(Integer.class, params[1].getRawClass());
    }

    @Test
    public void testUncheckedSimpleType() {
        JavaType type = _tf.uncheckedSimpleType(String.class);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
    }
}