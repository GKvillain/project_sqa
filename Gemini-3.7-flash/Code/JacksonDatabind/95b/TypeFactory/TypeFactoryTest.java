package com.fasterxml.jackson.databind.type;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;

public class TypeFactoryTest {

    private TypeFactory tf;

    @Before
    public void setUp() {
        tf = TypeFactory.defaultInstance();
    }

    // Tests well-known primitive and core types
    @Test
    public void testConstructType_wellKnownTypes_returnsCachedInstances() {
        JavaType boolType = tf.constructType(Boolean.TYPE);
        assertEquals(Boolean.TYPE, boolType.getRawClass());
        assertTrue(boolType.isPrimitive());

        JavaType intType = tf.constructType(Integer.TYPE);
        assertEquals(Integer.TYPE, intType.getRawClass());

        JavaType longType = tf.constructType(Long.TYPE);
        assertEquals(Long.TYPE, longType.getRawClass());

        JavaType strType = tf.constructType(String.class);
        assertEquals(String.class, strType.getRawClass());

        JavaType objType = tf.constructType(Object.class);
        assertEquals(Object.class, objType.getRawClass());
    }

    // Tests unknownType and rawClass static helpers
    @Test
    public void testStaticHelpers_unknownAndRawClass_returnsExpected() {
        JavaType unknown = TypeFactory.unknownType();
        assertNotNull(unknown);
        assertEquals(Object.class, unknown.getRawClass());

        Class<?> raw = TypeFactory.rawClass(String.class);
        assertEquals(String.class, raw);

        Class<?> rawFromJavaType = TypeFactory.rawClass(unknown);
        assertEquals(Object.class, rawFromJavaType);
    }

    // Tests construction of ArrayType from Class and JavaType
    @Test
    public void testConstructArrayType_validInputs_returnsArrayType() {
        ArrayType arrFromClass = tf.constructArrayType(String.class);
        assertNotNull(arrFromClass);
        assertTrue(arrFromClass.isArrayType());
        assertEquals(String[].class, arrFromClass.getRawClass());
        assertEquals(String.class, arrFromClass.getContentType().getRawClass());

        JavaType intType = tf.constructType(Integer.class);
        ArrayType arrFromJavaType = tf.constructArrayType(intType);
        assertTrue(arrFromJavaType.isArrayType());
        assertEquals(Integer.class, arrFromJavaType.getContentType().getRawClass());
    }

    // Tests construction of CollectionType and CollectionLikeType
    @Test
    public void testConstructCollectionType_validInputs_returnsCollectionType() {
        CollectionType listType = tf.constructCollectionType(ArrayList.class, String.class);
        assertNotNull(listType);
        assertTrue(listType.isCollectionLikeType());
        assertEquals(ArrayList.class, listType.getRawClass());
        assertEquals(String.class, listType.getContentType().getRawClass());

        CollectionLikeType colLike = tf.constructCollectionLikeType(List.class, Integer.class);
        assertNotNull(colLike);
        assertEquals(Integer.class, colLike.getContentType().getRawClass());

        CollectionType rawList = tf.constructRawCollectionType(ArrayList.class);
        assertNotNull(rawList);
        assertEquals(Object.class, rawList.getContentType().getRawClass());
    }

    // Tests construction of MapType and MapLikeType including Properties special handling
    @Test
    public void testConstructMapType_validInputs_returnsMapType() {
        MapType mapType = tf.constructMapType(HashMap.class, String.class, Integer.class);
        assertNotNull(mapType);
        assertTrue(mapType.isMapLikeType());
        assertEquals(HashMap.class, mapType.getRawClass());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(Integer.class, mapType.getContentType().getRawClass());

        MapType propType = tf.constructMapType(Properties.class, Object.class, Object.class);
        assertEquals(String.class, propType.getKeyType().getRawClass());
        assertEquals(String.class, propType.getContentType().getRawClass());

        MapType rawMap = tf.constructRawMapType(HashMap.class);
        assertEquals(Object.class, rawMap.getKeyType().getRawClass());
        assertEquals(Object.class, rawMap.getContentType().getRawClass());

        MapLikeType mapLike = tf.constructMapLikeType(Map.class, String.class, Long.class);
        assertNotNull(mapLike);
        assertEquals(String.class, mapLike.getKeyType().getRawClass());
        assertEquals(Long.class, mapLike.getContentType().getRawClass());
    }

    // Tests ReferenceType construction
    @Test
    public void testConstructReferenceType_validInput_returnsReferenceType() {
        JavaType refType = tf.constructReferenceType(AtomicReference.class, tf.constructType(String.class));
        assertNotNull(refType);
        assertTrue(refType.isReferenceType());
        assertEquals(AtomicReference.class, refType.getRawClass());
        assertEquals(String.class, refType.getContentType().getRawClass());
    }

    // Tests constructParametricType and constructParametrizedType
    @Test
    public void testConstructParametricType_nestedGenerics_resolvesProperly() {
        JavaType setType = tf.constructParametricType(HashSet.class, Integer.class);
        JavaType listType = tf.constructParametricType(ArrayList.class, setType);

        assertNotNull(listType);
        assertEquals(ArrayList.class, listType.getRawClass());
        JavaType elemType = listType.getContentType();
        assertEquals(HashSet.class, elemType.getRawClass());
        assertEquals(Integer.class, elemType.getContentType().getRawClass());

        JavaType parameterized = tf.constructParametrizedType(ArrayList.class, List.class, String.class);
        assertEquals(ArrayList.class, parameterized.getRawClass());
        assertEquals(String.class, parameterized.getContentType().getRawClass());
    }

    // Tests constructType with TypeReference
    @Test
    public void testConstructType_typeReference_resolvesGenericType() {
        JavaType type = tf.constructType(new TypeReference<List<String>>() {});
        assertNotNull(type);
        assertTrue(type.isCollectionLikeType());
        assertEquals(List.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    // Tests canonical string representation parsing
    @Test
    public void testConstructFromCanonical_validCanonicalName_returnsJavaType() {
        JavaType parsed = tf.constructFromCanonical("java.util.List<java.lang.String>");
        assertNotNull(parsed);
        assertTrue(parsed.isCollectionLikeType());
        assertEquals(List.class, parsed.getRawClass());
        assertEquals(String.class, parsed.getContentType().getRawClass());
    }

    // Tests constructFromCanonical with invalid format throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructFromCanonical_malformedString_throwsException() {
        tf.constructFromCanonical("java.util.List<invalid.nonexistent.Class");
    }

    // Tests specialization of a parameterized base type
    @Test
    public void testConstructSpecializedType_validSubclass_preservesGenerics() {
        JavaType baseType = tf.constructCollectionType(List.class, String.class);
        JavaType specialized = tf.constructSpecializedType(baseType, ArrayList.class);

        assertEquals(ArrayList.class, specialized.getRawClass());
        assertEquals(String.class, specialized.getContentType().getRawClass());

        // Same class optimization
        JavaType same = tf.constructSpecializedType(specialized, ArrayList.class);
        assertSame(specialized, same);
    }

    // Tests specialization on invalid hierarchy throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructSpecializedType_notSubtype_throwsException() {
        JavaType stringType = tf.constructType(String.class);
        tf.constructSpecializedType(stringType, Integer.class);
    }

    // Tests generalization of a type to super-class/interface
    @Test
    public void testConstructGeneralizedType_validSuperClass_returnsSuperType() {
        JavaType subType = tf.constructParametricType(ArrayList.class, String.class);
        JavaType generalized = tf.constructGeneralizedType(subType, List.class);

        assertEquals(List.class, generalized.getRawClass());
        assertEquals(String.class, generalized.getContentType().getRawClass());

        // Same class optimization
        JavaType same = tf.constructGeneralizedType(subType, ArrayList.class);
        assertSame(subType, same);
    }

    // Tests generalization with non-super class throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructGeneralizedType_invalidSuperClass_throwsException() {
        JavaType listType = tf.constructType(ArrayList.class);
        tf.constructGeneralizedType(listType, Set.class);
    }

    // Tests findTypeParameters for interface implementations
    @Test
    public void testFindTypeParameters_mapType_returnsKeyAndValueTypes() {
        JavaType mapType = tf.constructMapType(HashMap.class, String.class, Integer.class);
        JavaType[] params = tf.findTypeParameters(mapType, Map.class);

        assertNotNull(params);
        assertEquals(2, params.length);
        assertEquals(String.class, params[0].getRawClass());
        assertEquals(Integer.class, params[1].getRawClass());

        JavaType[] noParams = tf.findTypeParameters(mapType, Collection.class);
        assertEquals(0, noParams.length);
    }

    // Tests moreSpecificType comparison
    @Test
    public void testMoreSpecificType_variousPairs_returnsExpected() {
        JavaType listType = tf.constructType(List.class);
        JavaType arrayListType = tf.constructType(ArrayList.class);
        JavaType stringType = tf.constructType(String.class);

        // Null checks
        assertSame(listType, tf.moreSpecificType(listType, null));
        assertSame(listType, tf.moreSpecificType(null, listType));

        // Subtype preference
        assertSame(arrayListType, tf.moreSpecificType(listType, arrayListType));
        assertSame(arrayListType, tf.moreSpecificType(arrayListType, listType));

        // Unrelated types returns first
        assertSame(stringType, tf.moreSpecificType(stringType, listType));
    }

    // Tests findClass with primitive and class names
    @Test
    public void testFindClass_primitivesAndObjects_resolvesCorrectly() throws ClassNotFoundException {
        assertEquals(Integer.TYPE, tf.findClass("int"));
        assertEquals(Long.TYPE, tf.findClass("long"));
        assertEquals(Boolean.TYPE, tf.findClass("boolean"));
        assertEquals(String.class, tf.findClass("java.lang.String"));
    }

    // Tests findClass for nonexistent class throws ClassNotFoundException
    @Test(expected = ClassNotFoundException.class)
    public void testFindClass_nonExistent_throwsClassNotFoundException() throws ClassNotFoundException {
        tf.findClass("com.invalid.NonExistentClass12345");
    }

    // Tests TypeModifier registration and application
    @Test
    public void testWithModifier_customModifier_appliesModification() {
        TypeModifier mod = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                if (type.getRawClass() == String.class) {
                    return typeFactory.constructType(CharSequence.class);
                }
                return type;
            }
        };

        TypeFactory customTf = tf.withModifier(mod);
        JavaType modified = customTf.constructType(String.class);
        assertEquals(CharSequence.class, modified.getRawClass());

        // withModifier(null) clears modifiers
        TypeFactory resetTf = customTf.withModifier(null);
        JavaType unModified = resetTf.constructType(String.class);
        assertEquals(String.class, unModified.getRawClass());
    }

    // Tests withClassLoader and clearCache methods
    @Test
    public void testConfigurationAndCache_mutators_produceValidInstances() {
        ClassLoader cl = getClass().getClassLoader();
        TypeFactory customLoaderTf = tf.withClassLoader(cl);
        assertEquals(cl, customLoaderTf.getClassLoader());

        TypeFactory cachedTf = tf.withCache(null);
        assertNotNull(cachedTf);
        cachedTf.clearCache();
    }

    // --- Newly added tests to improve coverage ---

    // Helper generic holders for reflection-based type construction tests
    private static class GenericHolder<T> {
        public T genericField;
        public List<?> wildcardList;
        public List<? extends Number> boundedWildcardList;
        public List<? super Integer> superBoundedWildcardList;
        public T[] genericArray;
    }

    private static class StringHolder extends GenericHolder<String> {}

    private enum TestEnum { A, B }

    @Test
    public void testFindClass_allPrimitiveTypes() throws ClassNotFoundException {
        assertEquals(Byte.TYPE, tf.findClass("byte"));
        assertEquals(Character.TYPE, tf.findClass("char"));
        assertEquals(Short.TYPE, tf.findClass("short"));
        assertEquals(Float.TYPE, tf.findClass("float"));
        assertEquals(Double.TYPE, tf.findClass("double"));
        assertEquals(Void.TYPE, tf.findClass("void"));
    }

    @Test
    public void testConstructType_allPrimitiveTypes() {
        assertEquals(Byte.TYPE, tf.constructType(Byte.TYPE).getRawClass());
        assertEquals(Character.TYPE, tf.constructType(Character.TYPE).getRawClass());
        assertEquals(Short.TYPE, tf.constructType(Short.TYPE).getRawClass());
        assertEquals(Float.TYPE, tf.constructType(Float.TYPE).getRawClass());
        assertEquals(Double.TYPE, tf.constructType(Double.TYPE).getRawClass());
        assertEquals(Void.TYPE, tf.constructType(Void.TYPE).getRawClass());
    }

    @Test
    public void testConstructType_withContextClassAndJavaType() {
        JavaType contextType = tf.constructType(StringHolder.class);
        Field field;
        try {
            field = GenericHolder.class.getField("genericField");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        JavaType resolvedWithClass = tf.constructType(field.getGenericType(), StringHolder.class);
        assertEquals(String.class, resolvedWithClass.getRawClass());

        JavaType resolvedWithJavaType = tf.constructType(field.getGenericType(), contextType);
        assertEquals(String.class, resolvedWithJavaType.getRawClass());

        TypeBindings bindings = TypeBindings.create(GenericHolder.class, tf.constructType(String.class));
        JavaType resolvedWithBindings = tf.constructType(field.getGenericType(), bindings);
        assertEquals(String.class, resolvedWithBindings.getRawClass());
    }

    @Test
    public void testConstructType_wildcardsAndGenericArrays() throws Exception {
        Field wildcardField = GenericHolder.class.getField("wildcardList");
        JavaType wildcardType = tf.constructType(wildcardField.getGenericType());
        assertEquals(List.class, wildcardType.getRawClass());
        assertEquals(Object.class, wildcardType.getContentType().getRawClass());

        Field boundedField = GenericHolder.class.getField("boundedWildcardList");
        JavaType boundedType = tf.constructType(boundedField.getGenericType());
        assertEquals(List.class, boundedType.getRawClass());
        assertEquals(Number.class, boundedType.getContentType().getRawClass());

        Field superBoundedField = GenericHolder.class.getField("superBoundedWildcardList");
        JavaType superBoundedType = tf.constructType(superBoundedField.getGenericType());
        assertEquals(List.class, superBoundedType.getRawClass());
        assertEquals(Object.class, superBoundedType.getContentType().getRawClass());

        Field genericArrayField = GenericHolder.class.getField("genericArray");
        JavaType genericArrayType = tf.constructType(genericArrayField.getGenericType(), StringHolder.class);
        assertTrue(genericArrayType.isArrayType());
        assertEquals(String.class, genericArrayType.getContentType().getRawClass());
    }

    @Test
    public void testUncheckedSimpleType() {
        JavaType type = TypeFactory.uncheckedSimpleType(String.class);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
    }

    @Test
    public void testConstructSimpleType() {
        JavaType strType = tf.constructType(String.class);
        JavaType simple1 = tf.constructSimpleType(ArrayList.class, new JavaType[] { strType });
        assertEquals(ArrayList.class, simple1.getRawClass());

        JavaType simple2 = tf.constructSimpleType(ArrayList.class, List.class, new JavaType[] { strType });
        assertEquals(ArrayList.class, simple2.getRawClass());
    }

    @Test
    public void testConstructFromCanonical_mapAndArray() {
        JavaType mapType = tf.constructFromCanonical("java.util.Map<java.lang.String,java.lang.Integer>");
        assertTrue(mapType.isMapLikeType());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(Integer.class, mapType.getContentType().getRawClass());

        JavaType arrayType = tf.constructFromCanonical("java.lang.String[]");
        assertTrue(arrayType.isArrayType());
        assertEquals(String.class, arrayType.getContentType().getRawClass());
    }

    @Test
    public void testFindTypeParameters_fromClass() {
        JavaType[] params = tf.findTypeParameters(HashMap.class, Map.class);
        assertNotNull(params);
        assertEquals(2, params.length);

        TypeBindings bindings = TypeBindings.create(HashMap.class, tf.constructType(String.class), tf.constructType(Integer.class));
        JavaType[] resolvedParams = tf.findTypeParameters(HashMap.class, Map.class, bindings);
        assertNotNull(resolvedParams);
        assertEquals(2, resolvedParams.length);
        assertEquals(String.class, resolvedParams[0].getRawClass());
        assertEquals(Integer.class, resolvedParams[1].getRawClass());
    }

    @Test
    public void testSpecialCollections_EnumSetAndEnumMap() {
        CollectionType enumSetType = tf.constructCollectionType(EnumSet.class, TestEnum.class);
        assertNotNull(enumSetType);
        assertEquals(EnumSet.class, enumSetType.getRawClass());
        assertEquals(TestEnum.class, enumSetType.getContentType().getRawClass());

        MapType enumMapType = tf.constructMapType(EnumMap.class, TestEnum.class, String.class);
        assertNotNull(enumMapType);
        assertEquals(EnumMap.class, enumMapType.getRawClass());
        assertEquals(TestEnum.class, enumMapType.getKeyType().getRawClass());
        assertEquals(String.class, enumMapType.getContentType().getRawClass());
    }

    @Test
    public void testConstructParametricType_withJavaTypeArray() {
        JavaType strType = tf.constructType(String.class);
        JavaType intType = tf.constructType(Integer.class);
        JavaType mapType = tf.constructParametricType(HashMap.class, strType, intType);
        assertTrue(mapType.isMapLikeType());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(Integer.class, mapType.getContentType().getRawClass());
    }

    @Test
    public void testConstructParametrizedType_withJavaTypes() {
        JavaType strType = tf.constructType(String.class);
        JavaType listType = tf.constructParametrizedType(ArrayList.class, List.class, new JavaType[] { strType });
        assertTrue(listType.isCollectionLikeType());
        assertEquals(String.class, listType.getContentType().getRawClass());
    }
}