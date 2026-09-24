package com.fasterxml.jackson.databind.type;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

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
    }

    // Tests lookup of primitive types by name
    @Test
    public void testFindClass_primitiveNames_returnsPrimitiveClasses() throws Exception {
        assertEquals(Integer.TYPE, _typeFactory.findClass("int"));
        assertEquals(Long.TYPE, _typeFactory.findClass("long"));
        assertEquals(Float.TYPE, _typeFactory.findClass("float"));
        assertEquals(Double.TYPE, _typeFactory.findClass("double"));
        assertEquals(Boolean.TYPE, _typeFactory.findClass("boolean"));
        assertEquals(Byte.TYPE, _typeFactory.findClass("byte"));
        assertEquals(Character.TYPE, _typeFactory.findClass("char"));
        assertEquals(Short.TYPE, _typeFactory.findClass("short"));
        assertEquals(Void.TYPE, _typeFactory.findClass("void"));
    }

    // Tests lookup of standard fully qualified class name
    @Test
    public void testFindClass_validClassName_returnsClass() throws Exception {
        Class<?> clazz = _typeFactory.findClass("java.lang.String");
        assertEquals(String.class, clazz);
    }

    // Tests exception path for invalid class name in findClass
    @Test(expected = ClassNotFoundException.class)
    public void testFindClass_invalidClassName_throwsClassNotFoundException() throws Exception {
        _typeFactory.findClass("com.invalid.NonExistentClass");
    }

    // Tests constructing well-known core simple types
    @Test
    public void testConstructType_wellKnownTypes_returnsCachedInstances() {
        JavaType stringType = _typeFactory.constructType(String.class);
        assertEquals(String.class, stringType.getRawClass());

        JavaType intType = _typeFactory.constructType(Integer.TYPE);
        assertEquals(Integer.TYPE, intType.getRawClass());

        JavaType objType = _typeFactory.constructType(Object.class);
        assertEquals(Object.class, objType.getRawClass());

        JavaType boolType = _typeFactory.constructType(Boolean.TYPE);
        assertEquals(Boolean.TYPE, boolType.getRawClass());

        JavaType enumType = _typeFactory.constructType(Enum.class);
        assertEquals(Enum.class, enumType.getRawClass());

        JavaType compType = _typeFactory.constructType(Comparable.class);
        assertEquals(Comparable.class, compType.getRawClass());
    }

    // Tests constructType with TypeReference
    @Test
    public void testConstructType_typeReference_constructsGenericType() {
        JavaType type = _typeFactory.constructType(new TypeReference<List<String>>() {});
        assertTrue(type.isCollectionLikeType());
        assertEquals(List.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    // Tests array type construction from Class and JavaType
    @Test
    public void testConstructArrayType_validInputs_returnsArrayType() {
        ArrayType fromClass = _typeFactory.constructArrayType(String.class);
        assertEquals(String[].class, fromClass.getRawClass());
        assertEquals(String.class, fromClass.getContentType().getRawClass());

        JavaType stringType = _typeFactory.constructType(String.class);
        ArrayType fromJavaType = _typeFactory.constructArrayType(stringType);
        assertEquals(String[].class, fromJavaType.getRawClass());
        assertEquals(stringType, fromJavaType.getContentType());
    }

    // Tests collection type construction
    @Test
    public void testConstructCollectionType_validClasses_returnsCollectionType() {
        CollectionType type = _typeFactory.constructCollectionType(ArrayList.class, String.class);
        assertTrue(type.isCollectionLikeType());
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    // Tests map type construction for Map and Properties
    @Test
    public void testConstructMapType_validClasses_returnsMapType() {
        MapType mapType = _typeFactory.constructMapType(HashMap.class, String.class, Integer.class);
        assertTrue(mapType.isMapLikeType());
        assertEquals(HashMap.class, mapType.getRawClass());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(Integer.class, mapType.getContentType().getRawClass());

        MapType propType = _typeFactory.constructMapType(Properties.class, Object.class, Object.class);
        assertEquals(Properties.class, propType.getRawClass());
        assertEquals(String.class, propType.getKeyType().getRawClass());
        assertEquals(String.class, propType.getContentType().getRawClass());
    }

    // Tests raw container type constructions
    @Test
    public void testConstructRawContainers_unknownParams_returnsRawTypes() {
        CollectionType rawColl = _typeFactory.constructRawCollectionType(ArrayList.class);
        assertEquals(Object.class, rawColl.getContentType().getRawClass());

        MapType rawMap = _typeFactory.constructRawMapType(HashMap.class);
        assertEquals(Object.class, rawMap.getKeyType().getRawClass());
        assertEquals(Object.class, rawMap.getContentType().getRawClass());
    }

    // Tests parametric type construction
    @Test
    public void testConstructParametricType_validParameters_returnsParametricType() {
        JavaType type = _typeFactory.constructParametricType(Map.class, String.class, Long.class);
        assertTrue(type.isMapLikeType());
        assertEquals(String.class, type.getKeyType().getRawClass());
        assertEquals(Long.class, type.getContentType().getRawClass());
    }

    // Tests reference type construction (AtomicReference)
    @Test
    public void testConstructReferenceType_atomicReference_returnsReferenceType() {
        JavaType stringType = _typeFactory.constructType(String.class);
        JavaType refType = _typeFactory.constructReferenceType(AtomicReference.class, stringType);
        assertTrue(refType.isReferenceType());
        assertEquals(AtomicReference.class, refType.getRawClass());
        assertEquals(stringType, refType.getContentType());
    }

    // Tests specializing base type into subtype with same and different generics
    @Test
    public void testConstructSpecializedType_validSubclasses_specializesCorrectly() {
        JavaType mapType = _typeFactory.constructMapType(Map.class, String.class, Integer.class);
        JavaType specializedMap = _typeFactory.constructSpecializedType(mapType, HashMap.class);
        assertEquals(HashMap.class, specializedMap.getRawClass());
        assertEquals(String.class, specializedMap.getKeyType().getRawClass());
        assertEquals(Integer.class, specializedMap.getContentType().getRawClass());

        JavaType objType = _typeFactory.constructType(Object.class);
        JavaType specializedObj = _typeFactory.constructSpecializedType(objType, String.class);
        assertEquals(String.class, specializedObj.getRawClass());

        JavaType sameType = _typeFactory.constructSpecializedType(mapType, Map.class);
        assertSame(mapType, sameType);
    }

    // Tests exception path in constructSpecializedType for non-subtype
    @Test(expected = IllegalArgumentException.class)
    public void testConstructSpecializedType_notSubtype_throwsIllegalArgumentException() {
        JavaType listType = _typeFactory.constructCollectionType(List.class, String.class);
        _typeFactory.constructSpecializedType(listType, Map.class);
    }

    // Tests constructGeneralizedType to navigate super class hierarchy
    @Test
    public void testConstructGeneralizedType_validSuperClass_generalizesCorrectly() {
        JavaType hashMapType = _typeFactory.constructMapType(HashMap.class, String.class, Integer.class);
        JavaType mapType = _typeFactory.constructGeneralizedType(hashMapType, Map.class);
        assertEquals(Map.class, mapType.getRawClass());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(Integer.class, mapType.getContentType().getRawClass());

        JavaType sameType = _typeFactory.constructGeneralizedType(hashMapType, HashMap.class);
        assertSame(hashMapType, sameType);
    }

    // Tests exception path in constructGeneralizedType for non-super class
    @Test(expected = IllegalArgumentException.class)
    public void testConstructGeneralizedType_notSuperClass_throwsIllegalArgumentException() {
        JavaType stringType = _typeFactory.constructType(String.class);
        _typeFactory.constructGeneralizedType(stringType, List.class);
    }

    // Tests constructFromCanonical parsing
    @Test
    public void testConstructFromCanonical_validCanonicalStrings_returnsExpectedTypes() {
        JavaType simpleType = _typeFactory.constructFromCanonical("java.lang.String");
        assertEquals(String.class, simpleType.getRawClass());

        JavaType genericType = _typeFactory.constructFromCanonical("java.util.List<java.lang.Integer>");
        assertTrue(genericType.isCollectionLikeType());
        assertEquals(List.class, genericType.getRawClass());
        assertEquals(Integer.class, genericType.getContentType().getRawClass());
    }

    // Tests moreSpecificType comparison
    @Test
    public void testMoreSpecificType_variousPairs_returnsMoreSpecific() {
        JavaType objType = _typeFactory.constructType(Object.class);
        JavaType strType = _typeFactory.constructType(String.class);

        assertEquals(strType, _typeFactory.moreSpecificType(objType, strType));
        assertEquals(strType, _typeFactory.moreSpecificType(strType, objType));
        assertEquals(strType, _typeFactory.moreSpecificType(strType, null));
        assertEquals(strType, _typeFactory.moreSpecificType(null, strType));
    }

    // Tests static helper methods
    @Test
    public void testStaticHelpers_rawClassAndUnknownType() {
        assertEquals(String.class, TypeFactory.rawClass(String.class));
        JavaType unknown = TypeFactory.unknownType();
        assertNotNull(unknown);
        assertEquals(Object.class, unknown.getRawClass());
    }

    // Tests clearing cache and configuration mutators
    @Test
    public void testConfigurationMutators_createsNewInstances() {
        _typeFactory.clearCache();

        ClassLoader cl = getClass().getClassLoader();
        TypeFactory withCl = _typeFactory.withClassLoader(cl);
        assertNotNull(withCl);
        assertEquals(cl, withCl.getClassLoader());

        TypeFactory withMod = _typeFactory.withModifier(null);
        assertNotNull(withMod);
    }
}