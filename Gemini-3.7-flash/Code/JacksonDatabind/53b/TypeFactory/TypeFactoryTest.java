package com.fasterxml.jackson.databind.type;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;

public class TypeFactoryTest {

    private TypeFactory _tf;

    static class CustomList<E> extends ArrayList<E> {
        private static final long serialVersionUID = 1L;
    }

    static class StringMap extends HashMap<String, String> {
        private static final long serialVersionUID = 1L;
    }

    static class GenericMap<K, V> extends HashMap<K, V> {
        private static final long serialVersionUID = 1L;
    }

    interface CustomInterface<T> {
    }

    static class CustomImpl<T> implements CustomInterface<T> {
    }

    @Before
    public void setUp() {
        _tf = TypeFactory.defaultInstance();
    }

    // Tests primitive and well-known simple type construction
    @Test
    public void testConstructType_wellKnownTypes_returnsCachedInstances() {
        assertSame(_tf.constructType(String.class), _tf.constructType(String.class));
        assertEquals(String.class, _tf.constructType(String.class).getRawClass());
        assertEquals(int.class, _tf.constructType(int.class).getRawClass());
        assertEquals(boolean.class, _tf.constructType(boolean.class).getRawClass());
        assertEquals(Object.class, _tf.constructType(Object.class).getRawClass());
        assertEquals(Comparable.class, _tf.constructType(Comparable.class).getRawClass());
    }

    // Tests constructType with TypeReference
    @Test
    public void testConstructType_typeReference_returnsParameterizedType() {
        JavaType type = _tf.constructType(new TypeReference<List<String>>() {});
        assertTrue(type.isCollectionLikeType());
        assertEquals(List.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    // Tests array type construction from Class and JavaType
    @Test
    public void testConstructArrayType_validInputs_returnsArrayType() {
        ArrayType fromClass = _tf.constructArrayType(String.class);
        assertEquals(String[].class, fromClass.getRawClass());
        assertEquals(String.class, fromClass.getContentType().getRawClass());

        ArrayType fromJavaType = _tf.constructArrayType(fromClass.getContentType());
        assertEquals(String[].class, fromJavaType.getRawClass());
    }

    // Tests collection type shortcuts and raw variants
    @Test
    public void testConstructCollectionType_shortcutsAndRaw_returnsCollectionType() {
        CollectionType colType = _tf.constructCollectionType(ArrayList.class, String.class);
        assertEquals(ArrayList.class, colType.getRawClass());
        assertEquals(String.class, colType.getContentType().getRawClass());

        CollectionType rawCol = _tf.constructRawCollectionType(HashSet.class);
        assertEquals(HashSet.class, rawCol.getRawClass());
        assertEquals(Object.class, rawCol.getContentType().getRawClass());
    }

    // Tests map type shortcuts and Properties special handling
    @Test
    public void testConstructMapType_propertiesAndStandardMaps_returnsMapType() {
        MapType props = _tf.constructMapType(Properties.class, Object.class, Object.class);
        assertEquals(Properties.class, props.getRawClass());
        assertEquals(String.class, props.getKeyType().getRawClass());
        assertEquals(String.class, props.getContentType().getRawClass());

        MapType mapType = _tf.constructMapType(HashMap.class, String.class, Integer.class);
        assertEquals(HashMap.class, mapType.getRawClass());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(Integer.class, mapType.getContentType().getRawClass());

        MapType rawMap = _tf.constructRawMapType(TreeMap.class);
        assertEquals(TreeMap.class, rawMap.getRawClass());
        assertEquals(Object.class, rawMap.getKeyType().getRawClass());
    }

    // Tests parametric type construction with multiple parameters
    @Test
    public void testConstructParametricType_multiParams_returnsParametricType() {
        JavaType type = _tf.constructParametricType(Map.class, String.class, Long.class);
        assertTrue(type.isMapLikeType());
        assertEquals(String.class, type.getKeyType().getRawClass());
        assertEquals(Long.class, type.getContentType().getRawClass());
    }

    // Tests specialization on same raw class
    @Test
    public void testConstructSpecializedType_sameClass_returnsSameInstance() {
        JavaType baseType = _tf.constructType(String.class);
        JavaType specialized = _tf.constructSpecializedType(baseType, String.class);
        assertSame(baseType, specialized);
    }

    // Tests specialization from Object base type
    @Test
    public void testConstructSpecializedType_objectBase_returnsSubtype() {
        JavaType baseType = _tf.constructType(Object.class);
        JavaType specialized = _tf.constructSpecializedType(baseType, String.class);
        assertEquals(String.class, specialized.getRawClass());
    }

    // Tests specialization throwing exception when not subtype
    @Test(expected = IllegalArgumentException.class)
    public void testConstructSpecializedType_notSubtype_throwsException() {
        JavaType baseType = _tf.constructType(List.class);
        _tf.constructSpecializedType(baseType, Map.class);
    }

    // Tests specialization of container type with generic parameters
    @Test
    public void testConstructSpecializedType_containerWithGenerics_preservesParameters() {
        JavaType baseList = _tf.constructCollectionType(List.class, String.class);
        JavaType specialized = _tf.constructSpecializedType(baseList, ArrayList.class);
        assertEquals(ArrayList.class, specialized.getRawClass());
        assertEquals(String.class, specialized.getContentType().getRawClass());

        JavaType baseMap = _tf.constructMapType(Map.class, String.class, Integer.class);
        JavaType specializedMap = _tf.constructSpecializedType(baseMap, HashMap.class);
        assertEquals(HashMap.class, specializedMap.getRawClass());
        assertEquals(String.class, specializedMap.getKeyType().getRawClass());
        assertEquals(Integer.class, specializedMap.getContentType().getRawClass());
    }

    // Tests specialization of non-container generic type
    @Test
    public void testConstructSpecializedType_genericInterface_refinesCorrectly() {
        JavaType baseType = _tf.constructParametricType(CustomInterface.class, String.class);
        JavaType specialized = _tf.constructSpecializedType(baseType, CustomImpl.class);
        assertEquals(CustomImpl.class, specialized.getRawClass());
        assertEquals(1, specialized.containedTypeCount());
        assertEquals(String.class, specialized.containedType(0).getRawClass());
    }

    // Tests generalization to valid super class
    @Test
    public void testConstructGeneralizedType_validSuperClass_returnsSuperType() {
        JavaType stringList = _tf.constructCollectionType(ArrayList.class, String.class);
        JavaType genList = _tf.constructGeneralizedType(stringList, List.class);
        assertEquals(List.class, genList.getRawClass());
        assertEquals(String.class, genList.getContentType().getRawClass());

        JavaType same = _tf.constructGeneralizedType(stringList, ArrayList.class);
        assertSame(stringList, same);
    }

    // Tests generalization exception when class is not super-type
    @Test(expected = IllegalArgumentException.class)
    public void testConstructGeneralizedType_notSuperType_throwsException() {
        JavaType stringList = _tf.constructCollectionType(ArrayList.class, String.class);
        _tf.constructGeneralizedType(stringList, Set.class);
    }

    // Tests constructFromCanonical with simple and nested types
    @Test
    public void testConstructFromCanonical_validStrings_returnsParsedTypes() {
        JavaType simple = _tf.constructFromCanonical("java.lang.String");
        assertEquals(String.class, simple.getRawClass());

        JavaType nested = _tf.constructFromCanonical("java.util.List<java.lang.Integer>");
        assertEquals(List.class, nested.getRawClass());
        assertEquals(Integer.class, nested.getContentType().getRawClass());
    }

    // Tests findTypeParameters for interface bindings
    @Test
    public void testFindTypeParameters_validHierarchy_returnsResolvedParameters() {
        JavaType stringMapType = _tf.constructType(StringMap.class);
        JavaType[] params = _tf.findTypeParameters(stringMapType, Map.class);
        assertNotNull(params);
        assertEquals(2, params.length);
        assertEquals(String.class, params[0].getRawClass());
        assertEquals(String.class, params[1].getRawClass());

        JavaType[] noParams = _tf.findTypeParameters(stringMapType, List.class);
        assertEquals(0, noParams.length);
    }

    // Tests moreSpecificType comparison logic
    @Test
    public void testMoreSpecificType_variousInputs_returnsExpectedType() {
        JavaType listType = _tf.constructType(List.class);
        JavaType arrayListType = _tf.constructType(ArrayList.class);

        assertSame(arrayListType, _tf.moreSpecificType(listType, arrayListType));
        assertSame(arrayListType, _tf.moreSpecificType(arrayListType, listType));
        assertSame(listType, _tf.moreSpecificType(listType, null));
        assertSame(listType, _tf.moreSpecificType(null, listType));
        assertSame(listType, _tf.moreSpecificType(listType, listType));
    }

    // Tests findClass with primitives and standard classes
    @Test
    public void testFindClass_primitivesAndObjects_resolvesClasses() throws Exception {
        assertEquals(int.class, _tf.findClass("int"));
        assertEquals(boolean.class, _tf.findClass("boolean"));
        assertEquals(double.class, _tf.findClass("double"));
        assertEquals(void.class, _tf.findClass("void"));
        assertEquals(String.class, _tf.findClass("java.lang.String"));
    }

    // Tests findClass when class does not exist
    @Test(expected = ClassNotFoundException.class)
    public void testFindClass_nonExistingClass_throwsException() throws Exception {
        _tf.findClass("com.fasterxml.jackson.non.existing.ClassName");
    }

    // Tests reference type construction
    @Test
    public void testConstructReferenceType_validInput_returnsReferenceType() {
        JavaType strType = _tf.constructType(String.class);
        JavaType refType = _tf.constructReferenceType(AtomicReference.class, strType);
        assertTrue(refType.isReferenceType());
        assertEquals(AtomicReference.class, refType.getRawClass());
        assertEquals(String.class, refType.getContentType().getRawClass());
    }

    // Tests cache clear and static rawClass / unknownType helpers
    @Test
    public void testHelpersAndCache_operationsSucceed() {
        _tf.clearCache();
        JavaType unknown = TypeFactory.unknownType();
        assertEquals(Object.class, unknown.getRawClass());

        Class<?> raw = TypeFactory.rawClass(String.class);
        assertEquals(String.class, raw);

        TypeFactory customLoaderTf = _tf.withClassLoader(getClass().getClassLoader());
        assertNotNull(customLoaderTf.getClassLoader());
        assertNull(_tf.withModifier(null)._modifiers);
    }
}