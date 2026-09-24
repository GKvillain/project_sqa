package com.fasterxml.jackson.databind.type;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.lang.reflect.Type;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.util.LRUMap;

public class TypeFactoryTest {

    private TypeFactory _tf;

    @Before
    public void setUp() {
        _tf = TypeFactory.defaultInstance();
    }

    // Tests defaultInstance returns non-null singleton
    @Test
    public void testDefaultInstance_returnsSingleton() {
        TypeFactory instance = TypeFactory.defaultInstance();
        assertNotNull(instance);
        assertSame(instance, TypeFactory.defaultInstance());
    }

    // Tests constructType for well-known and primitive types
    @Test
    public void testConstructType_primitiveAndWellKnownTypes() {
        JavaType intType = _tf.constructType(int.class);
        assertEquals(int.class, intType.getRawClass());
        assertTrue(intType.isPrimitive());

        JavaType boolType = _tf.constructType(boolean.class);
        assertEquals(boolean.class, boolType.getRawClass());
        assertTrue(boolType.isPrimitive());

        JavaType strType = _tf.constructType(String.class);
        assertEquals(String.class, strType.getRawClass());

        JavaType objType = _tf.constructType(Object.class);
        assertEquals(Object.class, objType.getRawClass());
    }

    // Tests rawClass helper method
    @Test
    public void testRawClass_validType_returnsClass() {
        Class<?> raw = TypeFactory.rawClass(String.class);
        assertEquals(String.class, raw);

        JavaType listType = _tf.constructCollectionType(List.class, String.class);
        assertEquals(List.class, TypeFactory.rawClass(listType));
    }

    // Tests unknownType creates Object JavaType
    @Test
    public void testUnknownType_returnsObjectType() {
        JavaType unknown = TypeFactory.unknownType();
        assertNotNull(unknown);
        assertEquals(Object.class, unknown.getRawClass());
    }

    // Tests constructArrayType with Class and JavaType
    @Test
    public void testConstructArrayType_validInputs_constructsArrayType() {
        ArrayType arr1 = _tf.constructArrayType(String.class);
        assertNotNull(arr1);
        assertTrue(arr1.isArrayType());
        assertEquals(String.class, arr1.getContentType().getRawClass());

        JavaType intType = _tf.constructType(Integer.class);
        ArrayType arr2 = _tf.constructArrayType(intType);
        assertNotNull(arr2);
        assertTrue(arr2.isArrayType());
        assertEquals(Integer.class, arr2.getContentType().getRawClass());
    }

    // Tests constructCollectionType and constructRawCollectionType
    @Test
    public void testConstructCollectionType_validParameters_constructsCorrectCollection() {
        CollectionType listType = _tf.constructCollectionType(ArrayList.class, String.class);
        assertEquals(ArrayList.class, listType.getRawClass());
        assertEquals(String.class, listType.getContentType().getRawClass());

        CollectionType rawList = _tf.constructRawCollectionType(List.class);
        assertEquals(List.class, rawList.getRawClass());
        assertEquals(Object.class, rawList.getContentType().getRawClass());
    }

    // Tests constructCollectionLikeType
    @Test
    public void testConstructCollectionLikeType_validParameters_constructsCollectionLike() {
        CollectionLikeType colLike = _tf.constructCollectionLikeType(List.class, Integer.class);
        assertNotNull(colLike);
        assertEquals(List.class, colLike.getRawClass());
        assertEquals(Integer.class, colLike.getContentType().getRawClass());
    }

    // Tests constructMapType for standard Map and Properties
    @Test
    public void testConstructMapType_validParameters_constructsMapType() {
        MapType mapType = _tf.constructMapType(HashMap.class, String.class, Integer.class);
        assertEquals(HashMap.class, mapType.getRawClass());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(Integer.class, mapType.getContentType().getRawClass());

        MapType propType = _tf.constructMapType(Properties.class, Object.class, Object.class);
        assertEquals(Properties.class, propType.getRawClass());
        assertEquals(String.class, propType.getKeyType().getRawClass());
        assertEquals(String.class, propType.getContentType().getRawClass());

        MapType rawMap = _tf.constructRawMapType(Map.class);
        assertEquals(Map.class, rawMap.getRawClass());
        assertEquals(Object.class, rawMap.getKeyType().getRawClass());
        assertEquals(Object.class, rawMap.getContentType().getRawClass());
    }

    // Tests constructMapLikeType and constructRawMapLikeType
    @Test
    public void testConstructMapLikeType_validParameters_constructsMapLike() {
        MapLikeType mapLike = _tf.constructMapLikeType(Map.class, String.class, Long.class);
        assertNotNull(mapLike);
        assertEquals(Map.class, mapLike.getRawClass());
        assertEquals(String.class, mapLike.getKeyType().getRawClass());
        assertEquals(Long.class, mapLike.getContentType().getRawClass());

        MapLikeType rawMapLike = _tf.constructRawMapLikeType(Map.class);
        assertNotNull(rawMapLike);
        assertEquals(Map.class, rawMapLike.getRawClass());
    }

    // Tests constructReferenceType for AtomicReference
    @Test
    public void testConstructReferenceType_validParameters_constructsReferenceType() {
        JavaType refType = _tf.constructReferenceType(AtomicReference.class, _tf.constructType(String.class));
        assertTrue(refType.isReferenceType());
        assertEquals(AtomicReference.class, refType.getRawClass());
        assertEquals(String.class, refType.getContentType().getRawClass());
    }

    // Tests constructParametricType with Class and JavaType arguments
    @Test
    public void testConstructParametricType_nestedGenerics_constructsCorrectType() {
        JavaType inner = _tf.constructParametricType(List.class, String.class);
        JavaType outer = _tf.constructParametricType(Map.class, _tf.constructType(Integer.class), inner);
        assertTrue(outer.isMapLikeType());
        assertEquals(Map.class, outer.getRawClass());
        assertEquals(Integer.class, outer.getKeyType().getRawClass());
        assertEquals(List.class, outer.getContentType().getRawClass());
        assertEquals(String.class, outer.getContentType().getContentType().getRawClass());
    }

    // Tests constructType with TypeReference
    @Test
    public void testConstructType_typeReference_resolvesGenericType() {
        JavaType type = _tf.constructType(new TypeReference<List<String>>() {});
        assertTrue(type.isCollectionLikeType());
        assertEquals(List.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    // Tests constructSpecializedType for Map and Collection shortcuts
    @Test
    public void testConstructSpecializedType_shortcutsAndIdentical_returnsSpecialized() {
        JavaType listBase = _tf.constructCollectionType(List.class, String.class);
        JavaType arrayListSub = _tf.constructSpecializedType(listBase, ArrayList.class);
        assertEquals(ArrayList.class, arrayListSub.getRawClass());
        assertEquals(String.class, arrayListSub.getContentType().getRawClass());

        JavaType sameType = _tf.constructSpecializedType(listBase, List.class);
        assertSame(listBase, sameType);

        JavaType mapBase = _tf.constructMapType(Map.class, String.class, Integer.class);
        JavaType hashMapSub = _tf.constructSpecializedType(mapBase, HashMap.class);
        assertEquals(HashMap.class, hashMapSub.getRawClass());
        assertEquals(String.class, hashMapSub.getKeyType().getRawClass());
        assertEquals(Integer.class, hashMapSub.getContentType().getRawClass());

        JavaType objBase = _tf.constructType(Object.class);
        JavaType stringSub = _tf.constructSpecializedType(objBase, String.class);
        assertEquals(String.class, stringSub.getRawClass());
    }

    // Tests constructSpecializedType throws IllegalArgumentException when not a subtype
    @Test(expected = IllegalArgumentException.class)
    public void testConstructSpecializedType_notSubtype_throwsException() {
        JavaType listType = _tf.constructCollectionType(List.class, String.class);
        _tf.constructSpecializedType(listType, Set.class);
    }

    // Tests constructGeneralizedType
    @Test
    public void testConstructGeneralizedType_validSuperClass_returnsGeneralized() {
        JavaType arrayListType = _tf.constructCollectionType(ArrayList.class, String.class);
        JavaType listType = _tf.constructGeneralizedType(arrayListType, List.class);
        assertEquals(List.class, listType.getRawClass());
        assertEquals(String.class, listType.getContentType().getRawClass());

        JavaType same = _tf.constructGeneralizedType(arrayListType, ArrayList.class);
        assertSame(arrayListType, same);
    }

    // Tests constructGeneralizedType throws IllegalArgumentException for invalid supertype
    @Test(expected = IllegalArgumentException.class)
    public void testConstructGeneralizedType_notSuperType_throwsException() {
        JavaType listType = _tf.constructCollectionType(List.class, String.class);
        _tf.constructGeneralizedType(listType, ArrayList.class);
    }

    // Tests constructFromCanonical and toCanonical round-trip
    @Test
    public void testConstructFromCanonical_validCanonicalName_constructsMatchingType() {
        String canonical = "java.util.HashMap<java.lang.String,java.lang.Integer>";
        JavaType type = _tf.constructFromCanonical(canonical);
        assertNotNull(type);
        assertTrue(type.isMapLikeType());
        assertEquals(HashMap.class, type.getRawClass());
        assertEquals(String.class, type.getKeyType().getRawClass());
        assertEquals(Integer.class, type.getContentType().getRawClass());
        assertEquals(canonical, type.toCanonical());
    }

    // Tests constructFromCanonical with invalid format throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructFromCanonical_invalidString_throwsException() {
        _tf.constructFromCanonical("java.util.List<invalid.class.Name");
    }

    // Tests findClass for primitives and fully qualified classes
    @Test
    public void testFindClass_primitivesAndStandardClasses() throws ClassNotFoundException {
        assertEquals(int.class, _tf.findClass("int"));
        assertEquals(boolean.class, _tf.findClass("boolean"));
        assertEquals(long.class, _tf.findClass("long"));
        assertEquals(float.class, _tf.findClass("float"));
        assertEquals(double.class, _tf.findClass("double"));
        assertEquals(byte.class, _tf.findClass("byte"));
        assertEquals(char.class, _tf.findClass("char"));
        assertEquals(short.class, _tf.findClass("short"));
        assertEquals(void.class, _tf.findClass("void"));

        assertEquals(String.class, _tf.findClass("java.lang.String"));
    }

    // Tests findClass with non-existent class throws ClassNotFoundException
    @Test(expected = ClassNotFoundException.class)
    public void testFindClass_unknownClass_throwsClassNotFoundException() throws ClassNotFoundException {
        _tf.findClass("com.invalid.nonexistent.ClassXYZ");
    }

    // Tests moreSpecificType comparison
    @Test
    public void testMoreSpecificType_variousHierarchies_returnsExpected() {
        JavaType listType = _tf.constructType(List.class);
        JavaType arrayListType = _tf.constructType(ArrayList.class);
        JavaType strType = _tf.constructType(String.class);

        assertSame(arrayListType, _tf.moreSpecificType(listType, arrayListType));
        assertSame(arrayListType, _tf.moreSpecificType(arrayListType, listType));
        assertSame(listType, _tf.moreSpecificType(listType, strType));
        assertSame(listType, _tf.moreSpecificType(listType, null));
        assertSame(listType, _tf.moreSpecificType(null, listType));
    }

    // Tests configuration mutants: withModifier, withClassLoader, withCache and clearCache
    @Test
    public void testConfigurationMutants_andClearCache() {
        ClassLoader cl = getClass().getClassLoader();
        TypeFactory tfWithCl = _tf.withClassLoader(cl);
        assertEquals(cl, tfWithCl.getClassLoader());

        TypeModifier mockModifier = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                return type;
            }
        };
        TypeFactory tfWithMod = _tf.withModifier(mockModifier);
        assertNotNull(tfWithMod);
        TypeFactory tfNullMod = tfWithMod.withModifier(null);
        assertNotNull(tfNullMod);

        LRUMap<Object, JavaType> cache = new LRUMap<Object, JavaType>(10, 50);
        TypeFactory tfWithCache = _tf.withCache(cache);
        assertNotNull(tfWithCache);

        tfWithCache.constructType(String.class);
        tfWithCache.clearCache();
    }
}