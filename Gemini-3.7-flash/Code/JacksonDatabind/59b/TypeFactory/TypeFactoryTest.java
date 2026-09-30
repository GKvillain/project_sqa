package com.fasterxml.jackson.databind.type;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.util.LRUMap;

public class TypeFactoryTest {

    private TypeFactory _tf;

    enum TestEnum { A, B }

    static class CustomMap<K, V> extends HashMap<K, V> {
        private static final long serialVersionUID = 1L;
    }

    static class CustomList<E> extends ArrayList<E> {
        private static final long serialVersionUID = 1L;
    }

    static class NonGenericClass { }

    static class GenericHolder<T> {
        public T value;
        public List<T> listValue;
        public T[] arrayValue;
    }

    static class StringHolder extends GenericHolder<String> {
    }

    static class CustomCollectionLike<E> {
        public E element;
    }

    static class CustomMapLike<K, V> {
        public K key;
        public V value;
    }

    @Before
    public void setUp() {
        _tf = TypeFactory.defaultInstance();
    }

    // Tests well-known core primitive and simple types
    @Test
    public void testConstructType_primitiveAndWellKnownTypes_returnsCachedInstances() {
        assertSame(TypeFactory.CORE_TYPE_INT, _tf.constructType(Integer.TYPE));
        assertSame(TypeFactory.CORE_TYPE_BOOL, _tf.constructType(Boolean.TYPE));
        assertSame(TypeFactory.CORE_TYPE_LONG, _tf.constructType(Long.TYPE));
        assertSame(TypeFactory.CORE_TYPE_STRING, _tf.constructType(String.class));
        assertSame(TypeFactory.CORE_TYPE_OBJECT, _tf.constructType(Object.class));
        assertSame(TypeFactory.CORE_TYPE_OBJECT, TypeFactory.unknownType());
    }

    // Tests rawClass helper method
    @Test
    public void testRawClass_variousInputs_returnsExpectedRawClass() {
        assertEquals(String.class, TypeFactory.rawClass(String.class));
        JavaType listType = _tf.constructCollectionType(List.class, String.class);
        assertEquals(List.class, TypeFactory.rawClass(listType));
    }

    // Tests constructing ArrayType from Class and JavaType
    @Test
    public void testConstructArrayType_validElementTypes_returnsArrayType() {
        ArrayType fromClass = _tf.constructArrayType(String.class);
        assertEquals(String[].class, fromClass.getRawClass());
        assertEquals(String.class, fromClass.getContentType().getRawClass());

        ArrayType fromJavaType = _tf.constructArrayType(_tf.constructType(Integer.class));
        assertEquals(Integer[].class, fromJavaType.getRawClass());
        assertEquals(Integer.class, fromJavaType.getContentType().getRawClass());
    }

    // Tests constructing CollectionType and raw collection
    @Test
    public void testConstructCollectionType_variousInputs_returnsCollectionType() {
        CollectionType listType = _tf.constructCollectionType(ArrayList.class, String.class);
        assertEquals(ArrayList.class, listType.getRawClass());
        assertEquals(String.class, listType.getContentType().getRawClass());
        assertTrue(listType.isCollectionLikeType());

        CollectionType rawList = _tf.constructRawCollectionType(List.class);
        assertEquals(List.class, rawList.getRawClass());
        assertEquals(Object.class, rawList.getContentType().getRawClass());
    }

    // Tests constructing MapType and raw map
    @Test
    public void testConstructMapType_variousInputs_returnsMapType() {
        MapType mapType = _tf.constructMapType(HashMap.class, String.class, Integer.class);
        assertEquals(HashMap.class, mapType.getRawClass());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(Integer.class, mapType.getContentType().getRawClass());
        assertTrue(mapType.isMapLikeType());

        MapType propType = _tf.constructMapType(Properties.class, Object.class, Object.class);
        assertEquals(String.class, propType.getKeyType().getRawClass());
        assertEquals(String.class, propType.getContentType().getRawClass());

        MapType rawMap = _tf.constructRawMapType(Map.class);
        assertEquals(Map.class, rawMap.getRawClass());
        assertEquals(Object.class, rawMap.getKeyType().getRawClass());
        assertEquals(Object.class, rawMap.getContentType().getRawClass());
    }

    // Tests constructing ReferenceType
    @Test
    public void testConstructReferenceType_validInput_returnsReferenceType() {
        JavaType refType = _tf.constructReferenceType(AtomicReference.class, _tf.constructType(String.class));
        assertTrue(refType.isReferenceType());
        assertEquals(AtomicReference.class, refType.getRawClass());
        assertEquals(String.class, refType.getContentType().getRawClass());
    }

    // Tests constructing parametric type using Class and JavaType arguments
    @Test
    public void testConstructParametricType_validArguments_returnsParametricType() {
        JavaType type = _tf.constructParametricType(Map.class, String.class, Integer.class);
        assertEquals(Map.class, type.getRawClass());
        assertEquals(String.class, type.getKeyType().getRawClass());
        assertEquals(Integer.class, type.getContentType().getRawClass());

        JavaType holderType = _tf.constructParametricType(GenericHolder.class, _tf.constructType(String.class));
        assertEquals(GenericHolder.class, holderType.getRawClass());
        assertEquals(String.class, holderType.containedType(0).getRawClass());
    }

    // Tests specializing Map and Collection types to standard implementations
    @Test
    public void testConstructSpecializedType_wellKnownSubclasses_returnsSpecializedType() {
        JavaType baseMap = _tf.constructMapType(Map.class, String.class, Integer.class);
        JavaType specHashMap = _tf.constructSpecializedType(baseMap, HashMap.class);
        assertEquals(HashMap.class, specHashMap.getRawClass());
        assertEquals(String.class, specHashMap.getKeyType().getRawClass());
        assertEquals(Integer.class, specHashMap.getContentType().getRawClass());

        JavaType specTreeMap = _tf.constructSpecializedType(baseMap, TreeMap.class);
        assertEquals(TreeMap.class, specTreeMap.getRawClass());

        JavaType baseList = _tf.constructCollectionType(List.class, String.class);
        JavaType specArrayList = _tf.constructSpecializedType(baseList, ArrayList.class);
        assertEquals(ArrayList.class, specArrayList.getRawClass());
        assertEquals(String.class, specArrayList.getContentType().getRawClass());

        JavaType specLinkedList = _tf.constructSpecializedType(baseList, LinkedList.class);
        assertEquals(LinkedList.class, specLinkedList.getRawClass());

        JavaType baseSet = _tf.constructCollectionType(Set.class, String.class);
        JavaType specHashSet = _tf.constructSpecializedType(baseSet, HashSet.class);
        assertEquals(HashSet.class, specHashSet.getRawClass());
        JavaType specTreeSet = _tf.constructSpecializedType(baseSet, TreeSet.class);
        assertEquals(TreeSet.class, specTreeSet.getRawClass());
    }

    // Tests specializing custom generic and non-generic subclasses
    @Test
    public void testConstructSpecializedType_customSubclasses_returnsSpecializedType() {
        JavaType baseMap = _tf.constructMapType(Map.class, String.class, Integer.class);
        JavaType specCustomMap = _tf.constructSpecializedType(baseMap, CustomMap.class);
        assertEquals(CustomMap.class, specCustomMap.getRawClass());
        assertEquals(String.class, specCustomMap.getKeyType().getRawClass());
        assertEquals(Integer.class, specCustomMap.getContentType().getRawClass());

        JavaType objType = _tf.constructType(Object.class);
        JavaType specNonGen = _tf.constructSpecializedType(objType, NonGenericClass.class);
        assertEquals(NonGenericClass.class, specNonGen.getRawClass());

        JavaType sameType = _tf.constructSpecializedType(baseMap, Map.class);
        assertSame(baseMap, sameType);
    }

    // Tests constructSpecializedType with EnumSet shortcut
    @Test
    public void testConstructSpecializedType_enumSet_returnsBaseType() {
        JavaType enumSetType = _tf.constructCollectionType(EnumSet.class, TestEnum.class);
        JavaType spec = _tf.constructSpecializedType(enumSetType, EnumSet.class);
        assertSame(enumSetType, spec);
    }

    // Tests constructSpecializedType with invalid subtype throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructSpecializedType_incompatibleSubclass_throwsIllegalArgumentException() {
        JavaType listType = _tf.constructCollectionType(List.class, String.class);
        _tf.constructSpecializedType(listType, Map.class);
    }

    // Tests generalizing type to a superclass or superinterface
    @Test
    public void testConstructGeneralizedType_validSuperClass_returnsSuperType() {
        JavaType hashMapType = _tf.constructMapType(HashMap.class, String.class, Integer.class);
        JavaType generalized = _tf.constructGeneralizedType(hashMapType, Map.class);
        assertEquals(Map.class, generalized.getRawClass());
        assertEquals(String.class, generalized.getKeyType().getRawClass());
        assertEquals(Integer.class, generalized.getContentType().getRawClass());

        JavaType sameGen = _tf.constructGeneralizedType(hashMapType, HashMap.class);
        assertSame(hashMapType, sameGen);
    }

    // Tests constructGeneralizedType with non-supertype throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructGeneralizedType_nonSuperType_throwsIllegalArgumentException() {
        JavaType listType = _tf.constructCollectionType(List.class, String.class);
        _tf.constructGeneralizedType(listType, Map.class);
    }

    // Tests finding type parameters from a subtype for an interface
    @Test
    public void testFindTypeParameters_validSubtype_returnsBoundTypes() {
        JavaType mapType = _tf.constructMapType(HashMap.class, String.class, Integer.class);
        JavaType[] params = _tf.findTypeParameters(mapType, Map.class);
        assertNotNull(params);
        assertEquals(2, params.length);
        assertEquals(String.class, params[0].getRawClass());
        assertEquals(Integer.class, params[1].getRawClass());

        JavaType[] notFound = _tf.findTypeParameters(mapType, List.class);
        assertEquals(0, notFound.length);
    }

    // Tests moreSpecificType comparison
    @Test
    public void testMoreSpecificType_hierarchyAndDisjoint_returnsCorrectType() {
        JavaType listType = _tf.constructType(List.class);
        JavaType arrayListType = _tf.constructType(ArrayList.class);
        JavaType mapType = _tf.constructType(Map.class);

        assertSame(arrayListType, _tf.moreSpecificType(listType, arrayListType));
        assertSame(arrayListType, _tf.moreSpecificType(arrayListType, listType));
        assertSame(listType, _tf.moreSpecificType(listType, mapType));
        assertSame(listType, _tf.moreSpecificType(listType, null));
        assertSame(mapType, _tf.moreSpecificType(null, mapType));
    }

    // Tests constructFromCanonical parsing
    @Test
    public void testConstructFromCanonical_validStrings_returnsMatchingJavaType() {
        JavaType type = _tf.constructFromCanonical("java.util.List<java.lang.String>");
        assertEquals(List.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());

        JavaType mapType = _tf.constructFromCanonical("java.util.HashMap<java.lang.String,java.lang.Integer>");
        assertEquals(HashMap.class, mapType.getRawClass());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(Integer.class, mapType.getContentType().getRawClass());
    }

    // Tests findClass with primitives and valid/invalid class names
    @Test
    public void testFindClass_primitiveAndClassNames_returnsExpectedClasses() throws Exception {
        assertEquals(int.class, _tf.findClass("int"));
        assertEquals(boolean.class, _tf.findClass("boolean"));
        assertEquals(long.class, _tf.findClass("long"));
        assertEquals(double.class, _tf.findClass("double"));
        assertEquals(float.class, _tf.findClass("float"));
        assertEquals(byte.class, _tf.findClass("byte"));
        assertEquals(char.class, _tf.findClass("char"));
        assertEquals(short.class, _tf.findClass("short"));
        assertEquals(void.class, _tf.findClass("void"));

        assertEquals(String.class, _tf.findClass("java.lang.String"));
    }

    // Tests findClass with non-existent class throwing ClassNotFoundException
    @Test(expected = ClassNotFoundException.class)
    public void testFindClass_nonExistentClass_throwsClassNotFoundException() throws Exception {
        _tf.findClass("com.invalid.NonExistentClass12345");
    }

    // Tests TypeFactory configuration methods withModifier, withClassLoader, withCache, and clearCache
    @Test
    public void testConfigurationMethods_mutants_returnConfiguredInstances() {
        ClassLoader cl = getClass().getClassLoader();
        TypeFactory tfWithCl = _tf.withClassLoader(cl);
        assertEquals(cl, tfWithCl.getClassLoader());

        LRUMap<Object, JavaType> customCache = new LRUMap<Object, JavaType>(10, 50);
        TypeFactory tfWithCache = _tf.withCache(customCache);
        assertNotNull(tfWithCache);

        TypeModifier dummyMod = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                return type;
            }
        };
        TypeFactory tfWithMod = _tf.withModifier(dummyMod);
        assertNotNull(tfWithMod);

        TypeFactory tfResetMod = tfWithMod.withModifier(null);
        assertNotNull(tfResetMod);

        _tf.clearCache();
    }

    // Tests constructType with TypeReference
    @Test
    public void testConstructType_typeReference_returnsResolvedType() {
        JavaType refType = _tf.constructType(new TypeReference<List<String>>() {});
        assertEquals(List.class, refType.getRawClass());
        assertEquals(String.class, refType.getContentType().getRawClass());
    }

    // Tests constructCollectionLikeType and constructRawCollectionLikeType
    @Test
    public void testConstructCollectionLikeType_customClass_returnsCollectionLikeType() {
        CollectionLikeType colLikeFromClass = _tf.constructCollectionLikeType(CustomCollectionLike.class, String.class);
        assertEquals(CustomCollectionLike.class, colLikeFromClass.getRawClass());
        assertEquals(String.class, colLikeFromClass.getContentType().getRawClass());

        CollectionLikeType colLikeFromJavaType = _tf.constructCollectionLikeType(CustomCollectionLike.class, _tf.constructType(Integer.class));
        assertEquals(CustomCollectionLike.class, colLikeFromJavaType.getRawClass());
        assertEquals(Integer.class, colLikeFromJavaType.getContentType().getRawClass());

        CollectionLikeType rawColLike = _tf.constructRawCollectionLikeType(CustomCollectionLike.class);
        assertEquals(CustomCollectionLike.class, rawColLike.getRawClass());
        assertEquals(Object.class, rawColLike.getContentType().getRawClass());
    }

    // Tests constructMapLikeType and constructRawMapLikeType
    @Test
    public void testConstructMapLikeType_customClass_returnsMapLikeType() {
        MapLikeType mapLikeFromClass = _tf.constructMapLikeType(CustomMapLike.class, String.class, Integer.class);
        assertEquals(CustomMapLike.class, mapLikeFromClass.getRawClass());
        assertEquals(String.class, mapLikeFromClass.getKeyType().getRawClass());
        assertEquals(Integer.class, mapLikeFromClass.getContentType().getRawClass());

        MapLikeType mapLikeFromJavaType = _tf.constructMapLikeType(CustomMapLike.class, _tf.constructType(String.class), _tf.constructType(Double.class));
        assertEquals(CustomMapLike.class, mapLikeFromJavaType.getRawClass());
        assertEquals(String.class, mapLikeFromJavaType.getKeyType().getRawClass());
        assertEquals(Double.class, mapLikeFromJavaType.getContentType().getRawClass());

        MapLikeType rawMapLike = _tf.constructRawMapLikeType(CustomMapLike.class);
        assertEquals(CustomMapLike.class, rawMapLike.getRawClass());
        assertEquals(Object.class, rawMapLike.getKeyType().getRawClass());
        assertEquals(Object.class, rawMapLike.getContentType().getRawClass());
    }

    // Tests constructSimpleType
    @Test
    public void testConstructSimpleType_withParameterTypes_returnsSimpleType() {
        JavaType[] paramTypes = new JavaType[] { _tf.constructType(String.class) };
        JavaType simpleType = _tf.constructSimpleType(GenericHolder.class, paramTypes);
        assertEquals(GenericHolder.class, simpleType.getRawClass());
        assertEquals(1, simpleType.containedTypeCount());
        assertEquals(String.class, simpleType.containedType(0).getRawClass());
    }

    // Tests constructType with Class context and JavaType context
    @Test
    public void testConstructType_withContext_resolvesGenericFieldTypes() throws Exception {
        Field field = GenericHolder.class.getField("value");
        Type genericFieldType = field.getGenericType();

        JavaType resolvedWithClass = _tf.constructType(genericFieldType, StringHolder.class);
        assertEquals(String.class, resolvedWithClass.getRawClass());

        JavaType holderJavaType = _tf.constructParametricType(GenericHolder.class, Integer.class);
        JavaType resolvedWithJavaType = _tf.constructType(genericFieldType, holderJavaType);
        assertEquals(Integer.class, resolvedWithJavaType.getRawClass());

        Field listField = GenericHolder.class.getField("listValue");
        JavaType resolvedList = _tf.constructType(listField.getGenericType(), holderJavaType);
        assertEquals(List.class, resolvedList.getRawClass());
        assertEquals(Integer.class, resolvedList.getContentType().getRawClass());

        Field arrayField = GenericHolder.class.getField("arrayValue");
        JavaType resolvedArray = _tf.constructType(arrayField.getGenericType(), holderJavaType);
        assertTrue(resolvedArray.isArrayType());
        assertEquals(Integer.class, resolvedArray.getContentType().getRawClass());
    }

    // Tests constructType with TypeBindings
    @Test
    public void testConstructType_withBindings_resolvesProperly() throws Exception {
        TypeBindings bindings = TypeBindings.create(GenericHolder.class, new JavaType[] { _tf.constructType(Boolean.class) });
        Field field = GenericHolder.class.getField("value");
        JavaType resolved = _tf.constructType(field.getGenericType(), bindings);
        assertEquals(Boolean.class, resolved.getRawClass());
    }

    // Tests findTypeParameters with Class input
    @Test
    public void testFindTypeParameters_classInput_returnsTypeParameters() {
        JavaType[] params = _tf.findTypeParameters(StringHolder.class, GenericHolder.class);
        assertNotNull(params);
        assertEquals(1, params.length);
        assertEquals(String.class, params[0].getRawClass());
    }

    // Tests constructFromCanonical with nested generics and invalid input
    @Test
    public void testConstructFromCanonical_nestedAndInvalid_handlesCorrectly() {
        JavaType nestedType = _tf.constructFromCanonical("java.util.Map<java.lang.String,java.util.List<java.lang.Integer>>");
        assertEquals(Map.class, nestedType.getRawClass());
        assertEquals(String.class, nestedType.getKeyType().getRawClass());
        assertEquals(List.class, nestedType.getContentType().getRawClass());
        assertEquals(Integer.class, nestedType.getContentType().getContentType().getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructFromCanonical_malformedString_throwsIllegalArgumentException() {
        _tf.constructFromCanonical("java.util.List<java.lang.String");
    }

    // Tests constructArrayType with primitive classes
    @Test
    public void testConstructArrayType_primitiveTypes_returnsPrimitiveArrayTypes() {
        ArrayType intArray = _tf.constructArrayType(int.class);
        assertEquals(int[].class, intArray.getRawClass());
        assertEquals(int.class, intArray.getContentType().getRawClass());

        ArrayType byteArray = _tf.constructArrayType(byte.class);
        assertEquals(byte[].class, byteArray.getRawClass());
        assertEquals(byte.class, byteArray.getContentType().getRawClass());
    }

    // Tests findClass for inner class notation
    @Test
    public void testFindClass_innerClassWithDot_resolvesInnerClass() throws Exception {
        Class<?> innerCls = _tf.findClass("com.fasterxml.jackson.databind.type.TypeFactoryTest.TestEnum");
        assertEquals(TestEnum.class, innerCls);
    }

    // Tests uncheckedSimpleType
    @Test
    public void testUncheckedSimpleType_returnsSimpleTypeWithoutCheck() {
        JavaType simpleType = _tf.uncheckedSimpleType(String.class);
        assertEquals(String.class, simpleType.getRawClass());
        assertFalse(simpleType.isContainerType());
    }
}