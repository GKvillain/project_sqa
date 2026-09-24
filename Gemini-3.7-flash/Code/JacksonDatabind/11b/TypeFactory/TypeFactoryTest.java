package com.fasterxml.jackson.databind.type;

import java.lang.reflect.Type;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TypeFactoryTest {

    private TypeFactory _typeFactory;

    @Before
    public void setUp() {
        _typeFactory = TypeFactory.defaultInstance();
        _typeFactory.clearCache();
    }

    // Tests core primitive and standard types caching
    @Test
    public void testConstructType_coreTypes_returnsCachedInstances() {
        JavaType stringType = _typeFactory.constructType(String.class);
        assertEquals(String.class, stringType.getRawClass());
        assertSame(TypeFactory.CORE_TYPE_STRING, stringType);

        JavaType intType = _typeFactory.constructType(Integer.TYPE);
        assertSame(TypeFactory.CORE_TYPE_INT, intType);

        JavaType boolType = _typeFactory.constructType(Boolean.TYPE);
        assertSame(TypeFactory.CORE_TYPE_BOOL, boolType);

        JavaType longType = _typeFactory.constructType(Long.TYPE);
        assertSame(TypeFactory.CORE_TYPE_LONG, longType);
    }

    // Tests simple class resolution and caching
    @Test
    public void testConstructType_customClass_returnsCorrectJavaType() {
        JavaType type1 = _typeFactory.constructType(Double.class);
        assertNotNull(type1);
        assertEquals(Double.class, type1.getRawClass());

        JavaType type2 = _typeFactory.constructType(Double.class);
        assertSame(type1, type2);
    }

    // Tests unknownType and rawClass static helpers
    @Test
    public void testUnknownTypeAndRawClass_returnsExpectedValues() {
        JavaType unknown = TypeFactory.unknownType();
        assertNotNull(unknown);
        assertEquals(Object.class, unknown.getRawClass());

        Class<?> raw = TypeFactory.rawClass(String.class);
        assertEquals(String.class, raw);
    }

    // Tests ArrayType construction from class and JavaType
    @Test
    public void testConstructArrayType_validInputs_returnsArrayType() {
        ArrayType arrayFromClass = _typeFactory.constructArrayType(String.class);
        assertTrue(arrayFromClass.isArrayType());
        assertEquals(String[].class, arrayFromClass.getRawClass());
        assertEquals(String.class, arrayFromClass.getContentType().getRawClass());

        ArrayType arrayFromJavaType = _typeFactory.constructArrayType(_typeFactory.constructType(Integer.class));
        assertTrue(arrayFromJavaType.isArrayType());
        assertEquals(Integer[].class, arrayFromJavaType.getRawClass());
    }

    // Tests CollectionType construction
    @Test
    public void testConstructCollectionType_validInputs_returnsCollectionType() {
        CollectionType collType = _typeFactory.constructCollectionType(ArrayList.class, String.class);
        assertTrue(collType.isCollectionLikeType());
        assertEquals(ArrayList.class, collType.getRawClass());
        assertEquals(String.class, collType.getContentType().getRawClass());

        CollectionType collType2 = _typeFactory.constructCollectionType(List.class, _typeFactory.constructType(Integer.class));
        assertEquals(List.class, collType2.getRawClass());
        assertEquals(Integer.class, collType2.getContentType().getRawClass());
    }

    // Tests MapType construction
    @Test
    public void testConstructMapType_validInputs_returnsMapType() {
        MapType mapType = _typeFactory.constructMapType(HashMap.class, String.class, Integer.class);
        assertTrue(mapType.isMapLikeType());
        assertEquals(HashMap.class, mapType.getRawClass());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(Integer.class, mapType.getContentType().getRawClass());

        MapType mapType2 = _typeFactory.constructMapType(Map.class, _typeFactory.constructType(String.class), _typeFactory.constructType(Long.class));
        assertEquals(Map.class, mapType2.getRawClass());
        assertEquals(Long.class, mapType2.getContentType().getRawClass());
    }

    // Tests raw collection and raw map construction
    @Test
    public void testConstructRawCollectionAndMap_returnsTypesWithUnknownParameters() {
        CollectionType rawList = _typeFactory.constructRawCollectionType(List.class);
        assertEquals(List.class, rawList.getRawClass());
        assertEquals(Object.class, rawList.getContentType().getRawClass());

        MapType rawMap = _typeFactory.constructRawMapType(Map.class);
        assertEquals(Map.class, rawMap.getRawClass());
        assertEquals(Object.class, rawMap.getKeyType().getRawClass());
        assertEquals(Object.class, rawMap.getContentType().getRawClass());
    }

    // Tests TypeReference resolution for nested generic types
    @Test
    public void testConstructType_typeReference_resolvesNestedGenerics() {
        TypeReference<Map<String, List<Integer>>> typeRef = new TypeReference<Map<String, List<Integer>>>() {};
        JavaType javaType = _typeFactory.constructType(typeRef);

        assertTrue(javaType.isMapLikeType());
        assertEquals(Map.class, javaType.getRawClass());
        assertEquals(String.class, javaType.getKeyType().getRawClass());
        
        JavaType valueType = javaType.getContentType();
        assertTrue(valueType.isCollectionLikeType());
        assertEquals(List.class, valueType.getRawClass());
        assertEquals(Integer.class, valueType.getContentType().getRawClass());
    }

    // Tests constructParametricType / constructParametrizedType
    @Test
    public void testConstructParametrizedType_variousInputs_constructsCorrectly() {
        JavaType listType = _typeFactory.constructParametrizedType(ArrayList.class, ArrayList.class, String.class);
        assertTrue(listType.isCollectionLikeType());
        assertEquals(String.class, listType.getContentType().getRawClass());

        JavaType mapType = _typeFactory.constructParametrizedType(HashMap.class, HashMap.class, String.class, Integer.class);
        assertTrue(mapType.isMapLikeType());
        assertEquals(String.class, ((MapType) mapType).getKeyType().getRawClass());
        assertEquals(Integer.class, mapType.getContentType().getRawClass());
    }

    // Tests constructSpecializedType narrowing
    @Test
    public void testConstructSpecializedType_validSubclass_narrowsType() {
        JavaType baseType = _typeFactory.constructType(new TypeReference<Map<String, Integer>>() {});
        JavaType specialized = _typeFactory.constructSpecializedType(baseType, HashMap.class);
        
        assertEquals(HashMap.class, specialized.getRawClass());
        assertTrue(specialized.isMapLikeType());
        assertEquals(String.class, specialized.getKeyType().getRawClass());
        assertEquals(Integer.class, specialized.getContentType().getRawClass());

        // Same raw class should return identical instance
        JavaType same = _typeFactory.constructSpecializedType(specialized, HashMap.class);
        assertSame(specialized, same);
    }

    // Tests constructSpecializedType failure on incompatible subclass
    @Test(expected = IllegalArgumentException.class)
    public void testConstructSpecializedType_incompatibleSubclass_throwsException() {
        JavaType baseType = _typeFactory.constructType(List.class);
        _typeFactory.constructSpecializedType(baseType, HashMap.class);
    }

    // Tests findTypeParameters for class hierarchy
    @Test
    public void testFindTypeParameters_interfaceHierarchy_resolvesParameters() {
        JavaType[] params = _typeFactory.findTypeParameters(StringKeyMap.class, Map.class);
        assertNotNull(params);
        assertEquals(2, params.length);
        assertEquals(String.class, params[0].getRawClass());
        assertEquals(Long.class, params[1].getRawClass());
    }

    // Tests findTypeParameters invalid subtype relation
    @Test(expected = IllegalArgumentException.class)
    public void testFindTypeParameters_unrelatedClasses_throwsException() {
        _typeFactory.findTypeParameters(String.class, List.class);
    }

    // Tests constructFromCanonical parsing
    @Test
    public void testConstructFromCanonical_validCanonicalName_returnsJavaType() {
        JavaType type = _typeFactory.constructFromCanonical("java.util.List<java.lang.String>");
        assertNotNull(type);
        assertTrue(type.isCollectionLikeType());
        assertEquals(List.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    // Tests constructFromCanonical with invalid format
    @Test(expected = IllegalArgumentException.class)
    public void testConstructFromCanonical_invalidString_throwsException() {
        _typeFactory.constructFromCanonical("java.util.List<invalid class>");
    }

    // Tests moreSpecificType selection logic
    @Test
    public void testMoreSpecificType_differentCases_selectsMoreSpecific() {
        JavaType objType = _typeFactory.constructType(Object.class);
        JavaType strType = _typeFactory.constructType(String.class);

        assertSame(strType, _typeFactory.moreSpecificType(objType, strType));
        assertSame(strType, _typeFactory.moreSpecificType(strType, objType));
        assertSame(strType, _typeFactory.moreSpecificType(strType, null));
        assertSame(strType, _typeFactory.moreSpecificType(null, strType));

        JavaType listType = _typeFactory.constructType(List.class);
        assertSame(strType, _typeFactory.moreSpecificType(strType, listType));
    }

    // Tests TypeModifier registration
    @Test
    public void testWithModifier_customModifier_modifiesConstructedType() {
        TypeModifier mod = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                if (type.getRawClass() == int.class) {
                    return typeFactory.constructType(Double.class);
                }
                return type;
            }
        };

        TypeFactory customFactory = _typeFactory.withModifier(mod);
        assertNotNull(customFactory);

        JavaType modified = customFactory.constructType(int.class);
        assertEquals(Double.class, modified.getRawClass());

        // withModifier(null) returns same configuration
        TypeFactory unmodified = customFactory.withModifier(null);
        assertNotNull(unmodified);
    }

    // Tests uncheckedSimpleType direct construction
    @Test
    public void testUncheckedSimpleType_validClass_constructsSimpleType() {
        JavaType unchecked = _typeFactory.uncheckedSimpleType(ArrayList.class);
        assertNotNull(unchecked);
        assertEquals(ArrayList.class, unchecked.getRawClass());
        assertFalse(unchecked.isContainerType());
    }

    // Tests resolution of generic class with TypeVariable bounds and context
    @Test
    public void testConstructType_genericSubclassWithTypeVariable_resolvesCorrectly() {
        JavaType type = _typeFactory.constructType(new TypeReference<GenericContainer<String>>() {});
        assertEquals(GenericContainer.class, type.getRawClass());
        JavaType[] itemTypes = _typeFactory.findTypeParameters(type, GenericContainer.class);
        assertNotNull(itemTypes);
        assertEquals(1, itemTypes.length);
        assertEquals(String.class, itemTypes[0].getRawClass());
    }

    // Helper classes for generic resolution tests
    private static class StringKeyMap extends HashMap<String, Long> {
        private static final long serialVersionUID = 1L;
    }

    private static class GenericContainer<E> {
        public E element;
    }
}