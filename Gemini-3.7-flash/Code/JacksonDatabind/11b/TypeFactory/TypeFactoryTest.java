package com.fasterxml.jackson.databind.type;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TypeFactoryTest {

    private TypeFactory _tf;

    static class GenericHolder<T extends Number> {
        public T numberField;
        public List<T> listField;
        public T[] arrayField;
        public List<? extends CharSequence> wildcardField;
        public List<? super Integer> lowerBoundField;
    }

    static class StringHolder extends GenericHolder<Integer> {
    }

    static class CustomMap<K, V> extends HashMap<K, V> {
        private static final long serialVersionUID = 1L;
    }

    static class StringIntMap extends CustomMap<String, Integer> {
        private static final long serialVersionUID = 1L;
    }

    static class CustomCollectionLike<E> {
    }

    static class CustomMapLike<K, V> {
    }

    @Before
    public void setUp() {
        _tf = TypeFactory.defaultInstance();
        _tf.clearCache();
    }

    // Tests constructType for primitive and basic core types
    @Test
    public void testConstructType_coreTypes_returnsCachedInstances() {
        JavaType stringType = _tf.constructType(String.class);
        assertEquals(String.class, stringType.getRawClass());
        assertSame(TypeFactory.CORE_TYPE_STRING, stringType);

        JavaType intType = _tf.constructType(Integer.TYPE);
        assertEquals(Integer.TYPE, intType.getRawClass());
        assertSame(TypeFactory.CORE_TYPE_INT, intType);

        JavaType boolType = _tf.constructType(Boolean.TYPE);
        assertEquals(Boolean.TYPE, boolType.getRawClass());
        assertSame(TypeFactory.CORE_TYPE_BOOL, boolType);

        JavaType longType = _tf.constructType(Long.TYPE);
        assertEquals(Long.TYPE, longType.getRawClass());
        assertSame(TypeFactory.CORE_TYPE_LONG, longType);
    }

    // Tests constructType for generic array type reflection
    @Test
    public void testConstructType_genericArrayType_constructsArrayType() throws Exception {
        Field field = GenericHolder.class.getField("arrayField");
        Type genericType = field.getGenericType();
        assertTrue(genericType instanceof GenericArrayType);

        JavaType javaType = _tf.constructType(genericType);
        assertTrue(javaType.isArrayType());
        assertEquals(ArrayType.class, javaType.getClass());
    }

    // Tests constructType with TypeVariable and no context
    @Test
    public void testConstructType_typeVariableWithoutContext_resolvesType() throws Exception {
        Field field = GenericHolder.class.getField("numberField");
        Type genericType = field.getGenericType();
        assertTrue(genericType instanceof TypeVariable<?>);

        JavaType javaType = _tf.constructType(genericType);
        assertNotNull(javaType);
    }

    // Tests constructType with WildcardType
    @Test
    public void testConstructType_wildcardType_resolvesUpperBounds() throws Exception {
        Field field = GenericHolder.class.getField("wildcardField");
        ParameterizedType pt = (ParameterizedType) field.getGenericType();
        Type wildcardType = pt.getActualTypeArguments()[0];
        assertTrue(wildcardType instanceof WildcardType);

        JavaType javaType = _tf.constructType(wildcardType);
        assertEquals(CharSequence.class, javaType.getRawClass());
    }

    // Tests constructType with TypeReference
    @Test
    public void testConstructType_typeReference_constructsParameterizedType() {
        TypeReference<Map<String, List<Integer>>> typeRef = new TypeReference<Map<String, List<Integer>>>() {};
        JavaType type = _tf.constructType(typeRef);

        assertTrue(type.isMapLikeType());
        assertEquals(Map.class, type.getRawClass());
        assertEquals(String.class, type.getKeyType().getRawClass());
        assertEquals(List.class, type.getContentType().getRawClass());
        assertEquals(Integer.class, type.getContentType().getContentType().getRawClass());
    }

    // Tests constructCollectionType and constructMapType with Class parameters
    @Test
    public void testConstructCollectionAndMapType_validClasses_createsCorrectTypes() {
        CollectionType listType = _tf.constructCollectionType(ArrayList.class, String.class);
        assertEquals(ArrayList.class, listType.getRawClass());
        assertEquals(String.class, listType.getContentType().getRawClass());

        MapType mapType = _tf.constructMapType(HashMap.class, String.class, Integer.class);
        assertEquals(HashMap.class, mapType.getRawClass());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(Integer.class, mapType.getContentType().getRawClass());
    }

    // Tests constructSpecializedType for Map subclassing
    @Test
    public void testConstructSpecializedType_mapSubclass_narrowsCorrectly() {
        JavaType baseMapType = _tf.constructMapType(Map.class, String.class, Integer.class);
        JavaType specialized = _tf.constructSpecializedType(baseMapType, HashMap.class);

        assertEquals(HashMap.class, specialized.getRawClass());
        assertEquals(String.class, specialized.getKeyType().getRawClass());
        assertEquals(Integer.class, specialized.getContentType().getRawClass());
    }

    // Tests constructSpecializedType with incompatible subclass throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructSpecializedType_incompatibleSubclass_throwsException() {
        JavaType stringType = _tf.constructType(String.class);
        _tf.constructSpecializedType(stringType, ArrayList.class);
    }

    // Tests findTypeParameters for generic subclass inheritance chain
    @Test
    public void testFindTypeParameters_subclassHierarchy_resolvesTypeArguments() {
        JavaType[] params = _tf.findTypeParameters(StringIntMap.class, Map.class);
        assertNotNull(params);
        assertEquals(2, params.length);
        assertEquals(String.class, params[0].getRawClass());
        assertEquals(Integer.class, params[1].getRawClass());
    }

    // Tests findTypeParameters with not a subtype throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testFindTypeParameters_notSubtype_throwsException() {
        _tf.findTypeParameters(String.class, List.class);
    }

    // Tests constructFromCanonical with valid and invalid input
    @Test
    public void testConstructFromCanonical_validCanonicalName_returnsJavaType() {
        String canonical = "java.util.HashMap<java.lang.String,java.lang.Integer>";
        JavaType type = _tf.constructFromCanonical(canonical);

        assertEquals(HashMap.class, type.getRawClass());
        assertTrue(type.isMapLikeType());
        assertEquals(String.class, type.getKeyType().getRawClass());
        assertEquals(Integer.class, type.getContentType().getRawClass());
    }

    // Tests constructFromCanonical with invalid canonical string
    @Test(expected = IllegalArgumentException.class)
    public void testConstructFromCanonical_invalidString_throwsException() {
        _tf.constructFromCanonical("unknown.package.NonExistentClass");
    }

    // Tests rawClass static helper
    @Test
    public void testRawClass_variousTypes_returnsCorrectClass() {
        assertEquals(String.class, TypeFactory.rawClass(String.class));
        TypeReference<List<String>> ref = new TypeReference<List<String>>() {};
        assertEquals(List.class, TypeFactory.rawClass(ref.getType()));
    }

    // Tests moreSpecificType comparison logic
    @Test
    public void testMoreSpecificType_subclassAndSuperclass_returnsSubclass() {
        JavaType listType = _tf.constructType(List.class);
        JavaType arrayListType = _tf.constructType(ArrayList.class);

        assertSame(arrayListType, _tf.moreSpecificType(listType, arrayListType));
        assertSame(arrayListType, _tf.moreSpecificType(arrayListType, listType));
        assertSame(listType, _tf.moreSpecificType(listType, null));
        assertSame(listType, _tf.moreSpecificType(null, listType));
    }

    // Tests withModifier functionality
    @Test
    public void testWithModifier_customModifier_modifiesConstructedType() {
        TypeModifier modifier = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                if (type.getRawClass() == Number.class) {
                    return typeFactory.constructType(Double.class);
                }
                return type;
            }
        };

        TypeFactory customFactory = _tf.withModifier(modifier);
        assertNotSame(_tf, customFactory);

        JavaType modified = customFactory.constructType(Number.class);
        assertEquals(Double.class, modified.getRawClass());

        // Null modifier returns copy without NPE
        TypeFactory nullModFactory = customFactory.withModifier(null);
        assertNotNull(nullModFactory);
    }

    // Tests constructParametrizedType with array and simple class
    @Test
    public void testConstructParametrizedType_arrayAndSimple_returnsParametricTypes() {
        JavaType arrayType = _tf.constructParametrizedType(String[].class, String[].class, String.class);
        assertTrue(arrayType.isArrayType());
        assertEquals(String.class, arrayType.getContentType().getRawClass());

        JavaType customHolder = _tf.constructParametrizedType(GenericHolder.class, GenericHolder.class, Integer.class);
        assertEquals(GenericHolder.class, customHolder.getRawClass());
        assertEquals(Integer.class, customHolder.containedType(0).getRawClass());
    }

    // Tests raw factory methods when type parameters are unknown
    @Test
    public void testConstructRawTypes_unknownGenerics_createsRawInstances() {
        CollectionType rawColl = _tf.constructRawCollectionType(List.class);
        assertEquals(List.class, rawColl.getRawClass());
        assertEquals(Object.class, rawColl.getContentType().getRawClass());

        MapType rawMap = _tf.constructRawMapType(Map.class);
        assertEquals(Map.class, rawMap.getRawClass());
        assertEquals(Object.class, rawMap.getKeyType().getRawClass());
        assertEquals(Object.class, rawMap.getContentType().getRawClass());
    }

    // --- Added Tests for Uncovered Paths ---

    @Test
    public void testConstructArrayType_byClassAndJavaType() {
        ArrayType arrayTypeFromClass = _tf.constructArrayType(String.class);
        assertEquals(String[].class, arrayTypeFromClass.getRawClass());
        assertEquals(String.class, arrayTypeFromClass.getContentType().getRawClass());

        JavaType intType = _tf.constructType(Integer.class);
        ArrayType arrayTypeFromJavaType = _tf.constructArrayType(intType);
        assertEquals(Integer[].class, arrayTypeFromJavaType.getRawClass());
        assertEquals(Integer.class, arrayTypeFromJavaType.getContentType().getRawClass());
    }

    @Test
    public void testConstructCollectionLikeAndMapLikeTypes() {
        JavaType elemType = _tf.constructType(Double.class);
        CollectionLikeType collLikeFromJavaType = _tf.constructCollectionLikeType(CustomCollectionLike.class, elemType);
        assertEquals(CustomCollectionLike.class, collLikeFromJavaType.getRawClass());
        assertEquals(Double.class, collLikeFromJavaType.getContentType().getRawClass());

        CollectionLikeType collLikeFromClass = _tf.constructCollectionLikeType(CustomCollectionLike.class, String.class);
        assertEquals(CustomCollectionLike.class, collLikeFromClass.getRawClass());
        assertEquals(String.class, collLikeFromClass.getContentType().getRawClass());

        JavaType keyType = _tf.constructType(String.class);
        JavaType valType = _tf.constructType(Long.class);
        MapLikeType mapLikeFromJavaType = _tf.constructMapLikeType(CustomMapLike.class, keyType, valType);
        assertEquals(CustomMapLike.class, mapLikeFromJavaType.getRawClass());
        assertEquals(String.class, mapLikeFromJavaType.getKeyType().getRawClass());
        assertEquals(Long.class, mapLikeFromJavaType.getContentType().getRawClass());

        MapLikeType mapLikeFromClass = _tf.constructMapLikeType(CustomMapLike.class, Integer.class, Boolean.class);
        assertEquals(CustomMapLike.class, mapLikeFromClass.getRawClass());
        assertEquals(Integer.class, mapLikeFromClass.getKeyType().getRawClass());
        assertEquals(Boolean.class, mapLikeFromClass.getContentType().getRawClass());
    }

    @Test
    public void testConstructRawCollectionLikeAndRawMapLikeType() {
        CollectionLikeType rawCollLike = _tf.constructRawCollectionLikeType(CustomCollectionLike.class);
        assertEquals(CustomCollectionLike.class, rawCollLike.getRawClass());
        assertEquals(Object.class, rawCollLike.getContentType().getRawClass());

        MapLikeType rawMapLike = _tf.constructRawMapLikeType(CustomMapLike.class);
        assertEquals(CustomMapLike.class, rawMapLike.getRawClass());
        assertEquals(Object.class, rawMapLike.getKeyType().getRawClass());
        assertEquals(Object.class, rawMapLike.getContentType().getRawClass());
    }

    @Test
    public void testConstructParametricType_variousOverloads() {
        JavaType listType = _tf.constructParametricType(List.class, String.class);
        assertTrue(listType.isCollectionLikeType());
        assertEquals(String.class, listType.getContentType().getRawClass());

        JavaType mapType = _tf.constructParametricType(Map.class, _tf.constructType(String.class), _tf.constructType(Integer.class));
        assertTrue(mapType.isMapLikeType());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(Integer.class, mapType.getContentType().getRawClass());
    }

    @Test
    public void testConstructGeneralizedType() {
        JavaType subType = _tf.constructCollectionType(ArrayList.class, String.class);
        JavaType generalized = _tf.constructGeneralizedType(subType, List.class);
        assertEquals(List.class, generalized.getRawClass());
        assertEquals(String.class, generalized.getContentType().getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructGeneralizedType_notSuperclass_throwsException() {
        JavaType stringType = _tf.constructType(String.class);
        _tf.constructGeneralizedType(stringType, ArrayList.class);
    }

    @Test
    public void testConstructSpecializedType_collectionAndArray() {
        JavaType baseColl = _tf.constructCollectionType(List.class, String.class);
        JavaType specializedColl = _tf.constructSpecializedType(baseColl, ArrayList.class);
        assertEquals(ArrayList.class, specializedColl.getRawClass());
        assertEquals(String.class, specializedColl.getContentType().getRawClass());

        JavaType baseArray = _tf.constructArrayType(Object.class);
        JavaType specializedArray = _tf.constructSpecializedType(baseArray, String[].class);
        assertEquals(String[].class, specializedArray.getRawClass());
    }

    @Test
    public void testConstructType_withContextClassAndJavaType() throws Exception {
        Field field = GenericHolder.class.getField("numberField");
        Type genericType = field.getGenericType();

        JavaType resolvedWithClass = _tf.constructType(genericType, StringHolder.class);
        assertEquals(Integer.class, resolvedWithClass.getRawClass());

        JavaType contextType = _tf.constructType(StringHolder.class);
        JavaType resolvedWithJavaType = _tf.constructType(genericType, contextType);
        assertEquals(Integer.class, resolvedWithJavaType.getRawClass());
    }

    @Test
    public void testConstructType_wildcardType_lowerBound() throws Exception {
        Field field = GenericHolder.class.getField("lowerBoundField");
        ParameterizedType pt = (ParameterizedType) field.getGenericType();
        Type wildcardType = pt.getActualTypeArguments()[0];
        assertTrue(wildcardType instanceof WildcardType);

        JavaType javaType = _tf.constructType(wildcardType);
        assertEquals(Integer.class, javaType.getRawClass());
    }

    @Test
    public void testFindTypeParameters_withJavaType() {
        JavaType subType = _tf.constructType(StringIntMap.class);
        JavaType[] params = _tf.findTypeParameters(subType, Map.class);
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

    @Test
    public void testWithClassLoader() {
        ClassLoader cl = getClass().getClassLoader();
        TypeFactory customFactory = _tf.withClassLoader(cl);
        assertNotNull(customFactory);
        JavaType type = customFactory.constructType(String.class);
        assertEquals(String.class, type.getRawClass());
    }

    @Test
    public void testMoreSpecificType_bothNullOrEqual() {
        assertNull(_tf.moreSpecificType(null, null));
        JavaType stringType = _tf.constructType(String.class);
        assertSame(stringType, _tf.moreSpecificType(stringType, stringType));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructFromCanonical_malformedMissingClosingBracket() {
        _tf.constructFromCanonical("java.util.List<java.lang.String");
    }
}