package com.fasterxml.jackson.databind.type;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;

public class TypeFactoryTest {

    private TypeFactory _typeFactory;

    @Before
    public void setUp() {
        _typeFactory = TypeFactory.defaultInstance();
        _typeFactory.clearCache();
    }

    // Tests defect in 19b: Properties class type resolution to Map<String, String>
    @Test
    public void testConstructType_propertiesClass_returnsStringKeyAndValueMapType() {
        JavaType type = _typeFactory.constructType(Properties.class);
        assertTrue(type.isMapLikeType());
        MapType mapType = (MapType) type;
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(String.class, mapType.getContentType().getRawClass());
    }

    // Tests caching and pre-defined core types
    @Test
    public void testConstructType_coreTypes_returnsCoreInstances() {
        JavaType stringType = _typeFactory.constructType(String.class);
        assertSame(TypeFactory.CORE_TYPE_STRING, stringType);

        JavaType boolType = _typeFactory.constructType(Boolean.TYPE);
        assertSame(TypeFactory.CORE_TYPE_BOOL, boolType);

        JavaType intType = _typeFactory.constructType(Integer.TYPE);
        assertSame(TypeFactory.CORE_TYPE_INT, intType);

        JavaType longType = _typeFactory.constructType(Long.TYPE);
        assertSame(TypeFactory.CORE_TYPE_LONG, longType);
    }

    // Tests array construction from Class and JavaType
    @Test
    public void testConstructArrayType_validClass_returnsArrayType() {
        ArrayType arrayType = _typeFactory.constructArrayType(String.class);
        assertNotNull(arrayType);
        assertTrue(arrayType.isArrayType());
        assertEquals(String[].class, arrayType.getRawClass());
        assertEquals(String.class, arrayType.getContentType().getRawClass());

        ArrayType nestedArray = _typeFactory.constructArrayType(arrayType);
        assertEquals(String[][].class, nestedArray.getRawClass());
    }

    // Tests collection construction with Class and JavaType
    @Test
    public void testConstructCollectionType_validElementClass_returnsCollectionType() {
        CollectionType listType = _typeFactory.constructCollectionType(ArrayList.class, String.class);
        assertNotNull(listType);
        assertTrue(listType.isCollectionLikeType());
        assertEquals(ArrayList.class, listType.getRawClass());
        assertEquals(String.class, listType.getContentType().getRawClass());
    }

    // Tests map construction with Class and JavaType
    @Test
    public void testConstructMapType_validKeyAndValueClasses_returnsMapType() {
        MapType mapType = _typeFactory.constructMapType(HashMap.class, String.class, Integer.class);
        assertNotNull(mapType);
        assertTrue(mapType.isMapLikeType());
        assertEquals(HashMap.class, mapType.getRawClass());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(Integer.class, mapType.getContentType().getRawClass());
    }

    // Tests constructType using TypeReference
    @Test
    public void testConstructType_typeReference_returnsResolvedGenericType() {
        TypeReference<List<String>> ref = new TypeReference<List<String>>() {};
        JavaType type = _typeFactory.constructType(ref);
        assertTrue(type instanceof CollectionType);
        assertEquals(List.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    // Tests constructParametrizedType with valid parameters
    @Test
    public void testConstructParametrizedType_listSubclass_returnsParametrizedType() {
        JavaType stringType = _typeFactory.constructType(String.class);
        JavaType paramType = _typeFactory.constructParametrizedType(ArrayList.class, List.class, stringType);
        assertTrue(paramType.isCollectionLikeType());
        assertEquals(ArrayList.class, paramType.getRawClass());
        assertEquals(String.class, paramType.getContentType().getRawClass());
    }

    // Tests constructSpecializedType for narrowing base types
    @Test
    public void testConstructSpecializedType_subclassOfMap_returnsSpecializedType() {
        JavaType baseMap = _typeFactory.constructMapType(Map.class, String.class, Object.class);
        JavaType specialized = _typeFactory.constructSpecializedType(baseMap, HashMap.class);
        assertEquals(HashMap.class, specialized.getRawClass());
        assertEquals(String.class, specialized.getKeyType().getRawClass());
    }

    // Tests constructSpecializedType exception path on incompatible subtype
    @Test(expected = IllegalArgumentException.class)
    public void testConstructSpecializedType_incompatibleSubclass_throwsIllegalArgumentException() {
        JavaType stringType = _typeFactory.constructType(String.class);
        _typeFactory.constructSpecializedType(stringType, ArrayList.class);
    }

    // Tests canonical string parsing
    @Test
    public void testConstructFromCanonical_validCanonicalName_returnsCorrespondingType() {
        JavaType type = _typeFactory.constructFromCanonical("java.util.List<java.lang.String>");
        assertNotNull(type);
        assertEquals(List.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    // Tests constructFromCanonical exception on invalid format
    @Test(expected = IllegalArgumentException.class)
    public void testConstructFromCanonical_malformedString_throwsIllegalArgumentException() {
        _typeFactory.constructFromCanonical("java.util.List<unknown_class_name>");
    }

    // Tests findTypeParameters with generic inheritance chain
    @Test
    public void testFindTypeParameters_genericSubclass_findsTypeParameters() {
        JavaType[] params = _typeFactory.findTypeParameters(ArrayList.class, List.class);
        assertNotNull(params);
        assertEquals(1, params.length);
        assertEquals(Object.class, params[0].getRawClass());
    }

    // Tests findTypeParameters exception when class is not a subtype
    @Test(expected = IllegalArgumentException.class)
    public void testFindTypeParameters_notSubtype_throwsIllegalArgumentException() {
        _typeFactory.findTypeParameters(String.class, List.class);
    }

    // Tests moreSpecificType comparison logic
    @Test
    public void testMoreSpecificType_subAndSuperTypes_returnsMoreSpecific() {
        JavaType objectType = _typeFactory.constructType(Object.class);
        JavaType stringType = _typeFactory.constructType(String.class);

        assertSame(stringType, _typeFactory.moreSpecificType(objectType, stringType));
        assertSame(stringType, _typeFactory.moreSpecificType(stringType, objectType));
        assertSame(stringType, _typeFactory.moreSpecificType(stringType, null));
        assertSame(stringType, _typeFactory.moreSpecificType(null, stringType));
    }

    // Tests raw container construction helpers
    @Test
    public void testConstructRawContainers_validContainerClasses_returnsUnknownParameterizedContainers() {
        CollectionType rawList = _typeFactory.constructRawCollectionType(ArrayList.class);
        assertEquals(ArrayList.class, rawList.getRawClass());
        assertEquals(Object.class, rawList.getContentType().getRawClass());

        MapType rawMap = _typeFactory.constructRawMapType(HashMap.class);
        assertEquals(HashMap.class, rawMap.getRawClass());
        assertEquals(Object.class, rawMap.getKeyType().getRawClass());
        assertEquals(Object.class, rawMap.getContentType().getRawClass());
    }

    // Tests ReferenceType construction (e.g. AtomicReference)
    @Test
    public void testConstructType_atomicReference_returnsReferenceType() {
        JavaType type = _typeFactory.constructType(new TypeReference<AtomicReference<String>>() {});
        assertTrue(type.isReferenceType());
        assertEquals(AtomicReference.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    // Tests TypeModifier registration and clearCache functionality
    @Test
    public void testWithModifier_customModifier_modifiesConstructedType() {
        TypeModifier mod = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                if (type.getRawClass() == Integer.class) {
                    return typeFactory.constructType(Long.class);
                }
                return type;
            }
        };

        TypeFactory customFactory = _typeFactory.withModifier(mod);
        JavaType modifiedType = customFactory.constructType(Integer.class);
        assertEquals(Long.class, modifiedType.getRawClass());

        customFactory.clearCache();
    }

    // Additional coverage tests

    @Test
    public void testConstructParametricType_withClassesAndJavaTypes() {
        JavaType mapType = _typeFactory.constructParametricType(Map.class, String.class, Integer.class);
        assertTrue(mapType.isMapLikeType());
        assertEquals(Map.class, mapType.getRawClass());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(Integer.class, mapType.getContentType().getRawClass());

        JavaType stringType = _typeFactory.constructType(String.class);
        JavaType listType = _typeFactory.constructParametricType(List.class, new JavaType[] { stringType });
        assertTrue(listType.isCollectionLikeType());
        assertEquals(List.class, listType.getRawClass());
        assertEquals(String.class, listType.getContentType().getRawClass());
    }

    @Test
    public void testConstructCollectionLikeType_andMapLikeType() {
        CollectionLikeType colLike = _typeFactory.constructCollectionLikeType(ArrayList.class, String.class);
        assertNotNull(colLike);
        assertEquals(ArrayList.class, colLike.getRawClass());
        assertEquals(String.class, colLike.getContentType().getRawClass());

        JavaType strType = _typeFactory.constructType(String.class);
        JavaType intType = _typeFactory.constructType(Integer.class);
        MapLikeType mapLike = _typeFactory.constructMapLikeType(HashMap.class, strType, intType);
        assertNotNull(mapLike);
        assertEquals(HashMap.class, mapLike.getRawClass());
        assertEquals(String.class, mapLike.getKeyType().getRawClass());
        assertEquals(Integer.class, mapLike.getContentType().getRawClass());
    }

    @Test
    public void testConstructRawCollectionLikeType_andRawMapLikeType() {
        CollectionLikeType rawColLike = _typeFactory.constructRawCollectionLikeType(ArrayList.class);
        assertNotNull(rawColLike);
        assertEquals(ArrayList.class, rawColLike.getRawClass());
        assertEquals(Object.class, rawColLike.getContentType().getRawClass());

        MapLikeType rawMapLike = _typeFactory.constructRawMapLikeType(HashMap.class);
        assertNotNull(rawMapLike);
        assertEquals(HashMap.class, rawMapLike.getRawClass());
        assertEquals(Object.class, rawMapLike.getKeyType().getRawClass());
        assertEquals(Object.class, rawMapLike.getContentType().getRawClass());
    }

    @Test
    public void testConstructGeneralizedType_validSuperClass() {
        JavaType arrayListType = _typeFactory.constructCollectionType(ArrayList.class, String.class);
        JavaType generalized = _typeFactory.constructGeneralizedType(arrayListType, List.class);
        assertEquals(List.class, generalized.getRawClass());
        assertEquals(String.class, generalized.getContentType().getRawClass());

        JavaType same = _typeFactory.constructGeneralizedType(arrayListType, ArrayList.class);
        assertSame(arrayListType, same);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructGeneralizedType_incompatibleSuperClass_throwsIllegalArgumentException() {
        JavaType stringType = _typeFactory.constructType(String.class);
        _typeFactory.constructGeneralizedType(stringType, List.class);
    }

    @Test
    public void testUnknownType() {
        JavaType unknown = TypeFactory.unknownType();
        assertNotNull(unknown);
        assertEquals(Object.class, unknown.getRawClass());
    }

    @Test
    public void testConstructType_withContextClass_andContextType() {
        JavaType listType = _typeFactory.constructType(List.class, String.class);
        assertNotNull(listType);
        assertEquals(List.class, listType.getRawClass());

        JavaType contextType = _typeFactory.constructType(String.class);
        JavaType mapType = _typeFactory.constructType(Map.class, contextType);
        assertNotNull(mapType);
        assertEquals(Map.class, mapType.getRawClass());
    }

    @Test
    public void testConstructFromCanonical_nestedMapAndGenerics() {
        JavaType type = _typeFactory.constructFromCanonical("java.util.Map<java.lang.String,java.util.List<java.lang.Integer>>");
        assertNotNull(type);
        assertTrue(type.isMapLikeType());
        assertEquals(String.class, type.getKeyType().getRawClass());
        JavaType valueType = type.getContentType();
        assertTrue(valueType.isCollectionLikeType());
        assertEquals(List.class, valueType.getRawClass());
        assertEquals(Integer.class, valueType.getContentType().getRawClass());
    }

    @Test
    public void testWithClassLoader_andFindClass() throws ClassNotFoundException {
        ClassLoader cl = getClass().getClassLoader();
        TypeFactory tf = _typeFactory.withClassLoader(cl);
        assertNotNull(tf);
        Class<?> foundClass = tf.findClass("java.lang.String");
        assertEquals(String.class, foundClass);
    }

    @Test(expected = ClassNotFoundException.class)
    public void testFindClass_notFound_throwsClassNotFound() throws ClassNotFoundException {
        _typeFactory.findClass("com.non.existent.Class12345");
    }

    @Test
    public void testUncheckedSimpleType() {
        JavaType simple = TypeFactory.uncheckedSimpleType(String.class);
        assertNotNull(simple);
        assertEquals(String.class, simple.getRawClass());
    }

    @Test
    public void testConstructSimpleType_withParameters() {
        JavaType[] params = new JavaType[] { _typeFactory.constructType(String.class) };
        JavaType simpleType = _typeFactory.constructSimpleType(ArrayList.class, params);
        assertNotNull(simpleType);
        assertEquals(ArrayList.class, simpleType.getRawClass());
    }
}