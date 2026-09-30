package com.fasterxml.jackson.databind.type;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;

import static org.junit.Assert.*;

public class TypeFactoryTest {

    private TypeFactory _typeFactory;

    // Helper classes for generic type testing
    static class GenericBase<T> {
        public T value;
    }

    static class GenericSub<T> extends GenericBase<T> {
    }

    static class StringSub extends GenericBase<String> {
    }

    static interface CustomInterface<T> {
    }

    static class CustomImpl<T> implements CustomInterface<T> {
    }

    static class MultiParam<K, V> {
    }

    static class MultiParamSub<K, V> extends MultiParam<K, V> {
    }

    @Before
    public void setUp() {
        _typeFactory = TypeFactory.defaultInstance();
        _typeFactory.clearCache();
    }

    // Tests simple primitive and well-known types construction
    @Test
    public void testConstructType_wellKnownTypes_returnsCachedInstances() {
        JavaType intType = _typeFactory.constructType(Integer.TYPE);
        assertEquals(Integer.TYPE, intType.getRawClass());
        assertTrue(intType.isPrimitive());

        JavaType boolType = _typeFactory.constructType(Boolean.TYPE);
        assertEquals(Boolean.TYPE, boolType.getRawClass());
        assertTrue(boolType.isPrimitive());

        JavaType longType = _typeFactory.constructType(Long.TYPE);
        assertEquals(Long.TYPE, longType.getRawClass());
        assertTrue(longType.isPrimitive());

        JavaType stringType = _typeFactory.constructType(String.class);
        assertEquals(String.class, stringType.getRawClass());

        JavaType objectType = _typeFactory.constructType(Object.class);
        assertEquals(Object.class, objectType.getRawClass());

        JavaType enumType = _typeFactory.constructType(Enum.class);
        assertEquals(Enum.class, enumType.getRawClass());

        JavaType compType = _typeFactory.constructType(Comparable.class);
        assertEquals(Comparable.class, compType.getRawClass());

        JavaType classType = _typeFactory.constructType(Class.class);
        assertEquals(Class.class, classType.getRawClass());
    }

    // Tests unknown type and rawClass static helpers
    @Test
    public void testUnknownTypeAndRawClass() {
        JavaType unknown = TypeFactory.unknownType();
        assertNotNull(unknown);
        assertEquals(Object.class, unknown.getRawClass());

        Class<?> raw = TypeFactory.rawClass(String.class);
        assertEquals(String.class, raw);

        JavaType listType = _typeFactory.constructCollectionType(List.class, String.class);
        Class<?> rawFromType = TypeFactory.rawClass(listType);
        assertEquals(List.class, rawFromType);
    }

    // Tests findClass method with primitive names and fully qualified names
    @Test
    public void testFindClass_validNames_returnsClass() throws Exception {
        assertEquals(Integer.TYPE, _typeFactory.findClass("int"));
        assertEquals(Long.TYPE, _typeFactory.findClass("long"));
        assertEquals(Float.TYPE, _typeFactory.findClass("float"));
        assertEquals(Double.TYPE, _typeFactory.findClass("double"));
        assertEquals(Boolean.TYPE, _typeFactory.findClass("boolean"));
        assertEquals(Byte.TYPE, _typeFactory.findClass("byte"));
        assertEquals(Character.TYPE, _typeFactory.findClass("char"));
        assertEquals(Short.TYPE, _typeFactory.findClass("short"));
        assertEquals(Void.TYPE, _typeFactory.findClass("void"));

        assertEquals(String.class, _typeFactory.findClass("java.lang.String"));
        assertEquals(ArrayList.class, _typeFactory.findClass("java.util.ArrayList"));
    }

    // Tests findClass method throwing ClassNotFoundException
    @Test(expected = ClassNotFoundException.class)
    public void testFindClass_invalidName_throwsClassNotFound() throws Exception {
        _typeFactory.findClass("com.invalid.NonExistingClassName");
    }

    // Tests constructArrayType from Class and JavaType
    @Test
    public void testConstructArrayType_validInputs_returnsArrayType() {
        ArrayType arrFromClass = _typeFactory.constructArrayType(String.class);
        assertNotNull(arrFromClass);
        assertTrue(arrFromClass.isArrayType());
        assertEquals(String.class, arrFromClass.getContentType().getRawClass());

        JavaType intType = _typeFactory.constructType(Integer.class);
        ArrayType arrFromType = _typeFactory.constructArrayType(intType);
        assertNotNull(arrFromType);
        assertTrue(arrFromType.isArrayType());
        assertEquals(Integer.class, arrFromType.getContentType().getRawClass());
    }

    // Tests CollectionType and CollectionLikeType construction
    @Test
    public void testConstructCollectionType_validInputs_returnsCorrectTypes() {
        CollectionType colType = _typeFactory.constructCollectionType(List.class, String.class);
        assertEquals(List.class, colType.getRawClass());
        assertEquals(String.class, colType.getContentType().getRawClass());

        CollectionType rawCol = _typeFactory.constructRawCollectionType(ArrayList.class);
        assertEquals(ArrayList.class, rawCol.getRawClass());
        assertEquals(Object.class, rawCol.getContentType().getRawClass());

        CollectionLikeType colLike = _typeFactory.constructCollectionLikeType(Iterable.class, Integer.class);
        assertEquals(Iterable.class, colLike.getRawClass());
        assertEquals(Integer.class, colLike.getContentType().getRawClass());

        CollectionLikeType rawColLike = _typeFactory.constructRawCollectionLikeType(Iterable.class);
        assertEquals(Iterable.class, rawColLike.getRawClass());
        assertEquals(Object.class, rawColLike.getContentType().getRawClass());
    }

    // Tests MapType and MapLikeType construction including Properties special handling
    @Test
    public void testConstructMapType_validInputs_returnsCorrectTypes() {
        MapType mapType = _typeFactory.constructMapType(HashMap.class, String.class, Integer.class);
        assertEquals(HashMap.class, mapType.getRawClass());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(Integer.class, mapType.getContentType().getRawClass());

        MapType propType = _typeFactory.constructMapType(Properties.class, Object.class, Object.class);
        assertEquals(Properties.class, propType.getRawClass());
        assertEquals(String.class, propType.getKeyType().getRawClass());
        assertEquals(String.class, propType.getContentType().getRawClass());

        MapType rawMap = _typeFactory.constructRawMapType(Map.class);
        assertEquals(Map.class, rawMap.getRawClass());
        assertEquals(Object.class, rawMap.getKeyType().getRawClass());
        assertEquals(Object.class, rawMap.getContentType().getRawClass());

        MapLikeType mapLike = _typeFactory.constructMapLikeType(Map.class, String.class, Long.class);
        assertEquals(Map.class, mapLike.getRawClass());
        assertEquals(String.class, mapLike.getKeyType().getRawClass());
        assertEquals(Long.class, mapLike.getContentType().getRawClass());

        MapLikeType rawMapLike = _typeFactory.constructRawMapLikeType(Map.class);
        assertEquals(Map.class, rawMapLike.getRawClass());
        assertEquals(Object.class, rawMapLike.getKeyType().getRawClass());
        assertEquals(Object.class, rawMapLike.getContentType().getRawClass());
    }

    // Tests constructReferenceType and constructSimpleType
    @Test
    public void testConstructReferenceAndSimpleType() {
        JavaType refType = _typeFactory.constructReferenceType(AtomicReference.class, _typeFactory.constructType(String.class));
        assertNotNull(refType);
        assertTrue(refType.isReferenceType());
        assertEquals(AtomicReference.class, refType.getRawClass());
        assertEquals(String.class, refType.getContentType().getRawClass());

        JavaType simpleType = _typeFactory.constructSimpleType(String.class, new JavaType[0]);
        assertEquals(String.class, simpleType.getRawClass());

        JavaType unchecked = _typeFactory.uncheckedSimpleType(Integer.class);
        assertEquals(Integer.class, unchecked.getRawClass());
    }

    // Tests constructParametricType and constructParametrizedType
    @Test
    public void testConstructParametricType_variousForms() {
        JavaType paramType1 = _typeFactory.constructParametricType(GenericBase.class, String.class);
        assertEquals(GenericBase.class, paramType1.getRawClass());
        assertEquals(1, paramType1.containedTypeCount());
        assertEquals(String.class, paramType1.containedType(0).getRawClass());

        JavaType paramType2 = _typeFactory.constructParametrizedType(GenericBase.class, GenericBase.class, _typeFactory.constructType(Integer.class));
        assertEquals(GenericBase.class, paramType2.getRawClass());
        assertEquals(Integer.class, paramType2.containedType(0).getRawClass());

        JavaType multiParam = _typeFactory.constructParametricType(MultiParam.class, String.class, Long.class);
        assertEquals(MultiParam.class, multiParam.getRawClass());
        assertEquals(2, multiParam.containedTypeCount());
        assertEquals(String.class, multiParam.containedType(0).getRawClass());
        assertEquals(Long.class, multiParam.containedType(1).getRawClass());
    }

    // Tests constructType with TypeReference
    @Test
    public void testConstructType_typeReference_resolvesCorrectly() {
        JavaType type = _typeFactory.constructType(new TypeReference<List<String>>() {});
        assertTrue(type.isCollectionLikeType());
        assertEquals(List.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    // Tests constructFromCanonical with simple and parameterized types
    @Test
    public void testConstructFromCanonical_validStrings_returnsJavaType() {
        JavaType t1 = _typeFactory.constructFromCanonical("java.lang.String");
        assertEquals(String.class, t1.getRawClass());

        JavaType t2 = _typeFactory.constructFromCanonical("java.util.ArrayList<java.lang.Integer>");
        assertEquals(ArrayList.class, t2.getRawClass());
        assertEquals(Integer.class, t2.getContentType().getRawClass());
    }

    // Tests constructSpecializedType with same class optimization and untyped base
    @Test
    public void testConstructSpecializedType_sameClassAndObjectBase() {
        JavaType baseType = _typeFactory.constructType(Number.class);
        assertSame(baseType, _typeFactory.constructSpecializedType(baseType, Number.class));

        JavaType objBase = _typeFactory.constructType(Object.class);
        JavaType specialized = _typeFactory.constructSpecializedType(objBase, String.class);
        assertEquals(String.class, specialized.getRawClass());
    }

    // Tests constructSpecializedType with non-subtype throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructSpecializedType_invalidSubtype_throwsException() {
        JavaType baseType = _typeFactory.constructType(List.class);
        _typeFactory.constructSpecializedType(baseType, Set.class);
    }

    // Tests constructSpecializedType with container shortcuts
    @Test
    public void testConstructSpecializedType_containerShortcuts() {
        JavaType mapBase = _typeFactory.constructMapType(Map.class, String.class, Integer.class);
        JavaType hashSub = _typeFactory.constructSpecializedType(mapBase, HashMap.class);
        assertEquals(HashMap.class, hashSub.getRawClass());
        assertEquals(String.class, hashSub.getKeyType().getRawClass());
        assertEquals(Integer.class, hashSub.getContentType().getRawClass());

        JavaType listBase = _typeFactory.constructCollectionType(List.class, String.class);
        JavaType arrayListSub = _typeFactory.constructSpecializedType(listBase, ArrayList.class);
        assertEquals(ArrayList.class, arrayListSub.getRawClass());
        assertEquals(String.class, arrayListSub.getContentType().getRawClass());

        JavaType enumSetBase = _typeFactory.constructCollectionType(EnumSet.class, _typeFactory.constructType(Thread.State.class));
        JavaType enumSetSub = _typeFactory.constructSpecializedType(enumSetBase, EnumSet.class);
        assertSame(enumSetBase, enumSetSub);
    }

    // Tests constructSpecializedType with generic types and non-generic subclasses
    @Test
    public void testConstructSpecializedType_genericSubclasses() {
        JavaType baseType = _typeFactory.constructParametricType(GenericBase.class, String.class);
        JavaType subType = _typeFactory.constructSpecializedType(baseType, StringSub.class);
        assertEquals(StringSub.class, subType.getRawClass());

        JavaType genericSub = _typeFactory.constructSpecializedType(baseType, GenericSub.class);
        assertEquals(GenericSub.class, genericSub.getRawClass());
        assertEquals(1, genericSub.containedTypeCount());
        assertEquals(String.class, genericSub.containedType(0).getRawClass());

        JavaType interfaceBase = _typeFactory.constructParametricType(CustomInterface.class, Integer.class);
        JavaType implSub = _typeFactory.constructSpecializedType(interfaceBase, CustomImpl.class);
        assertEquals(CustomImpl.class, implSub.getRawClass());
        assertEquals(Integer.class, implSub.containedType(0).getRawClass());

        JavaType multiBase = _typeFactory.constructParametricType(MultiParam.class, String.class, Long.class);
        JavaType multiSub = _typeFactory.constructSpecializedType(multiBase, MultiParamSub.class);
        assertEquals(MultiParamSub.class, multiSub.getRawClass());
        assertEquals(2, multiSub.containedTypeCount());
        assertEquals(String.class, multiSub.containedType(0).getRawClass());
        assertEquals(Long.class, multiSub.containedType(1).getRawClass());
    }

    // Tests constructGeneralizedType
    @Test
    public void testConstructGeneralizedType_validInputs_returnsGeneralizedType() {
        JavaType subType = _typeFactory.constructParametricType(ArrayList.class, String.class);
        JavaType genType = _typeFactory.constructGeneralizedType(subType, List.class);
        assertEquals(List.class, genType.getRawClass());
        assertEquals(String.class, genType.getContentType().getRawClass());

        assertSame(subType, _typeFactory.constructGeneralizedType(subType, ArrayList.class));
    }

    // Tests constructGeneralizedType throwing IllegalArgumentException for invalid superclass
    @Test(expected = IllegalArgumentException.class)
    public void testConstructGeneralizedType_invalidSuperclass_throwsException() {
        JavaType subType = _typeFactory.constructType(String.class);
        _typeFactory.constructGeneralizedType(subType, List.class);
    }

    // Tests findTypeParameters
    @Test
    public void testFindTypeParameters_mapAndCustomTypes() {
        JavaType mapType = _typeFactory.constructMapType(HashMap.class, String.class, Integer.class);
        JavaType[] params = _typeFactory.findTypeParameters(mapType, Map.class);
        assertNotNull(params);
        assertEquals(2, params.length);
        assertEquals(String.class, params[0].getRawClass());
        assertEquals(Integer.class, params[1].getRawClass());

        JavaType[] notFound = _typeFactory.findTypeParameters(mapType, Set.class);
        assertEquals(0, notFound.length);
    }

    // Tests moreSpecificType comparison method
    @Test
    public void testMoreSpecificType_variousInputs() {
        JavaType stringType = _typeFactory.constructType(String.class);
        JavaType objectType = _typeFactory.constructType(Object.class);

        assertSame(stringType, _typeFactory.moreSpecificType(stringType, objectType));
        assertSame(stringType, _typeFactory.moreSpecificType(objectType, stringType));
        assertSame(stringType, _typeFactory.moreSpecificType(stringType, stringType));

        assertSame(stringType, _typeFactory.moreSpecificType(stringType, null));
        assertSame(objectType, _typeFactory.moreSpecificType(null, objectType));
    }

    // Tests withModifier and withClassLoader configuration methods
    @Test
    public void testConfiguration_withModifierAndClassLoader() {
        ClassLoader cl = getClass().getClassLoader();
        TypeFactory factoryWithCl = _typeFactory.withClassLoader(cl);
        assertNotNull(factoryWithCl);
        assertEquals(cl, factoryWithCl.getClassLoader());

        TypeFactory factoryWithNullMod = _typeFactory.withModifier(null);
        assertNotNull(factoryWithNullMod);

        TypeModifier dummyMod = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, java.lang.reflect.Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                return type;
            }
        };

        TypeFactory factoryWithMod = _typeFactory.withModifier(dummyMod);
        assertNotNull(factoryWithMod);
        JavaType modifiedType = factoryWithMod.constructType(String.class);
        assertEquals(String.class, modifiedType.getRawClass());
    }
}