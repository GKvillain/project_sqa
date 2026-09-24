package org.apache.commons.lang3.reflect;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link TypeUtils}.
 */
public class TypeUtilsTest<T extends Number> {

    public List<String> stringList;
    public List<Object> objectList;
    public List<?> wildcardList;
    public List<? extends Number> extendsNumberList;
    public List<? extends Integer> extendsIntegerList;
    public List<? super Integer> superIntegerList;
    public List<? super Number> superNumberList;
    public List<T> genericList;
    public T typeVar;
    public T[] genericArray;
    public List<String>[] stringListArray;
    public Comparable<T> comparableT;

    private Type getType(String fieldName) throws NoSuchFieldException {
        return TypeUtilsTest.class.getField(fieldName).getGenericType();
    }

    // Tests public constructor instantiation
    @Test
    public void testConstructor_instantiation_notNull() {
        TypeUtils utils = new TypeUtils();
        assertNotNull(utils);
    }

    // Tests assignability between Class and Class
    @Test
    public void testIsAssignable_classes_returnsExpected() {
        assertTrue(TypeUtils.isAssignable(String.class, Object.class));
        assertTrue(TypeUtils.isAssignable(Integer.class, Number.class));
        assertFalse(TypeUtils.isAssignable(Number.class, Integer.class));
        assertTrue(TypeUtils.isAssignable(String.class, String.class));
        assertTrue(TypeUtils.isAssignable(int.class, Integer.class));
        assertFalse(TypeUtils.isAssignable(String.class, Integer.class));
    }

    // Tests null inputs for isAssignable
    @Test
    public void testIsAssignable_nullInputs_handlesGracefully() {
        assertTrue(TypeUtils.isAssignable(null, Object.class));
        assertFalse(TypeUtils.isAssignable(null, int.class));
        assertFalse(TypeUtils.isAssignable(String.class, (Type) null));
        assertTrue(TypeUtils.isAssignable(null, (Type) null));
    }

    // Tests assignability of parameterized types with identical type arguments
    @Test
    public void testIsAssignable_parameterizedTypeExactMatch_returnsTrue() throws Exception {
        Type strListType = getType("stringList");
        assertTrue(TypeUtils.isAssignable(strListType, strListType));
        assertTrue(TypeUtils.isAssignable(strListType, Collection.class));
        assertFalse(TypeUtils.isAssignable(Collection.class, strListType));
    }

    // Tests assignability of parameterized types with wildcard arguments
    @Test
    public void testIsAssignable_parameterizedTypeWildcards_returnsExpected() throws Exception {
        Type strListType = getType("stringList");
        Type wildcardListType = getType("wildcardList");
        Type objListType = getType("objectList");
        Type extNumListType = getType("extendsNumberList");
        Type extIntListType = getType("extendsIntegerList");
        Type superIntListType = getType("superIntegerList");
        Type superNumListType = getType("superNumberList");

        assertTrue(TypeUtils.isAssignable(strListType, wildcardListType));
        assertFalse(TypeUtils.isAssignable(wildcardListType, strListType));
        assertFalse(TypeUtils.isAssignable(strListType, objListType));
        assertTrue(TypeUtils.isAssignable(extIntListType, extNumListType));
        assertFalse(TypeUtils.isAssignable(extNumListType, extIntListType));
        assertTrue(TypeUtils.isAssignable(superNumListType, superIntListType));
        assertFalse(TypeUtils.isAssignable(superIntListType, superNumListType));
    }

    // Tests assignability of generic array types
    @Test
    public void testIsAssignable_genericArrayType_returnsExpected() throws Exception {
        Type genArrayType = getType("genericArray");
        Type strListArrayType = getType("stringListArray");

        assertTrue(TypeUtils.isAssignable(genArrayType, Object.class));
        assertTrue(TypeUtils.isAssignable(genArrayType, Object[].class));
        assertTrue(TypeUtils.isAssignable(genArrayType, genArrayType));
        assertTrue(TypeUtils.isAssignable(String[].class, Object[].class));
        assertTrue(TypeUtils.isAssignable(strListArrayType, Object[].class));
        assertFalse(TypeUtils.isAssignable(Object[].class, genArrayType));
        assertFalse(TypeUtils.isAssignable(String.class, genArrayType));
    }

    // Tests assignability of type variables
    @Test
    public void testIsAssignable_typeVariable_returnsExpected() throws Exception {
        Type typeVarType = getType("typeVar");
        assertTrue(TypeUtils.isAssignable(typeVarType, Number.class));
        assertTrue(TypeUtils.isAssignable(typeVarType, Object.class));
        assertFalse(TypeUtils.isAssignable(typeVarType, String.class));
        assertTrue(TypeUtils.isAssignable(typeVarType, typeVarType));
        assertFalse(TypeUtils.isAssignable(Number.class, typeVarType));
    }

    // Tests getTypeArguments for ParameterizedType
    @Test
    public void testGetTypeArguments_parameterizedType_returnsCorrectMapping() throws Exception {
        ParameterizedType strListType = (ParameterizedType) getType("stringList");
        Map<TypeVariable<?>, Type> typeArgs = TypeUtils.getTypeArguments(strListType);
        assertNotNull(typeArgs);
        assertEquals(1, typeArgs.size());
        TypeVariable<?> listParam = List.class.getTypeParameters()[0];
        assertEquals(String.class, typeArgs.get(listParam));
    }

    // Tests getTypeArguments across class hierarchy
    @Test
    public void testGetTypeArguments_subclassHierarchy_returnsResolvedArguments() {
        Map<TypeVariable<?>, Type> typeArgs = TypeUtils.getTypeArguments(Properties.class, Map.class);
        assertNotNull(typeArgs);
        assertEquals(2, typeArgs.size());
        TypeVariable<?>[] mapParams = Map.class.getTypeParameters();
        assertEquals(Object.class, typeArgs.get(mapParams[0]));
        assertEquals(Object.class, typeArgs.get(mapParams[1]));
    }

    // Tests getTypeArguments with unassignable classes returns null
    @Test
    public void testGetTypeArguments_unassignable_returnsNull() {
        assertNull(TypeUtils.getTypeArguments(String.class, List.class));
    }

    // Tests determineTypeArguments for complex inheritance hierarchies
    @Test
    public void testDetermineTypeArguments_hierarchyMapping_returnsTypeArgs() throws Exception {
        ParameterizedType superType = (ParameterizedType) getType("stringList");
        Map<TypeVariable<?>, Type> determined = TypeUtils.determineTypeArguments(ArrayList.class, superType);
        assertNotNull(determined);
        TypeVariable<?> arrayListParam = ArrayList.class.getTypeParameters()[0];
        assertEquals(String.class, determined.get(arrayListParam));
    }

    // Tests determineTypeArguments compatibility check returning null
    @Test
    public void testDetermineTypeArguments_incompatible_returnsNull() throws Exception {
        ParameterizedType superType = (ParameterizedType) getType("stringList");
        assertNull(TypeUtils.determineTypeArguments(Set.class, superType));
    }

    // Tests isInstance method for various types and values
    @Test
    public void testIsInstance_variousInputs_returnsExpected() throws Exception {
        assertTrue(TypeUtils.isInstance("test", String.class));
        assertTrue(TypeUtils.isInstance("test", Object.class));
        assertFalse(TypeUtils.isInstance("test", Number.class));
        assertFalse(TypeUtils.isInstance("test", null));
        assertTrue(TypeUtils.isInstance(null, Object.class));
        assertFalse(TypeUtils.isInstance(null, int.class));

        Type strListType = getType("stringList");
        List<String> list = new ArrayList<String>();
        assertTrue(TypeUtils.isInstance(list, strListType));
    }

    // Tests normalizeUpperBounds with subtype reductions
    @Test
    public void testNormalizeUpperBounds_redundantBounds_removesSuperTypes() {
        Type[] bounds = new Type[] { Collection.class, List.class };
        Type[] normalized = TypeUtils.normalizeUpperBounds(bounds);
        assertEquals(1, normalized.length);
        assertEquals(List.class, normalized[0]);

        Type[] singleBound = new Type[] { Object.class };
        assertSame(singleBound, TypeUtils.normalizeUpperBounds(singleBound));
    }

    // Tests getImplicitBounds for TypeVariable and WildcardType
    @Test
    public void testGetImplicitBounds_typeVariableAndWildcard_returnsBounds() throws Exception {
        TypeVariable<?> typeVarType = (TypeVariable<?>) getType("typeVar");
        Type[] bounds = TypeUtils.getImplicitBounds(typeVarType);
        assertEquals(1, bounds.length);
        assertEquals(Number.class, bounds[0]);

        ParameterizedType extNumList = (ParameterizedType) getType("extendsNumberList");
        WildcardType wildcard = (WildcardType) extNumList.getActualTypeArguments()[0];
        Type[] upperBounds = TypeUtils.getImplicitUpperBounds(wildcard);
        assertEquals(1, upperBounds.length);
        assertEquals(Number.class, upperBounds[0]);

        Type[] lowerBounds = TypeUtils.getImplicitLowerBounds(wildcard);
        assertEquals(1, lowerBounds.length);
        assertNull(lowerBounds[0]);
    }

    // Tests isArrayType and getArrayComponentType utility methods
    @Test
    public void testIsArrayType_and_getArrayComponentType_returnsCorrectResults() throws Exception {
        assertTrue(TypeUtils.isArrayType(String[].class));
        assertFalse(TypeUtils.isArrayType(String.class));
        assertTrue(TypeUtils.isArrayType(getType("genericArray")));

        assertEquals(String.class, TypeUtils.getArrayComponentType(String[].class));
        assertNull(TypeUtils.getArrayComponentType(String.class));
        Type genArrayType = getType("genericArray");
        assertEquals(getType("typeVar"), TypeUtils.getArrayComponentType(genArrayType));
    }

    // Tests getRawType resolution
    @Test
    public void testGetRawType_variousTypes_resolvesClass() throws Exception {
        assertEquals(String.class, TypeUtils.getRawType(String.class, null));
        ParameterizedType strListType = (ParameterizedType) getType("stringList");
        assertEquals(List.class, TypeUtils.getRawType(strListType, null));

        Type typeVarType = getType("typeVar");
        assertNull(TypeUtils.getRawType(typeVarType, null));
        assertEquals(TypeUtilsTest.class, TypeUtils.getRawType(typeVarType, TypeUtilsTest.class));

        Type genArrayType = getType("genericArray");
        Class<?> rawArrayClass = TypeUtils.getRawType(genArrayType, TypeUtilsTest.class);
        assertNotNull(rawArrayClass);
        assertTrue(rawArrayClass.isArray());
    }

    // Tests typesSatisfyVariables method
    @Test
    public void testTypesSatisfyVariables_validAndInvalid_returnsExpected() throws Exception {
        TypeVariable<?> typeVarType = (TypeVariable<?>) getType("typeVar");
        Map<TypeVariable<?>, Type> validMap = new HashMap<TypeVariable<?>, Type>();
        validMap.put(typeVarType, Integer.class);
        assertTrue(TypeUtils.typesSatisfyVariables(validMap));

        Map<TypeVariable<?>, Type> invalidMap = new HashMap<TypeVariable<?>, Type>();
        invalidMap.put(typeVarType, String.class);
        assertFalse(TypeUtils.typesSatisfyVariables(invalidMap));
    }
}