package org.apache.commons.lang3.reflect;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TypeUtilsTest<B> {

    public interface This<K, V> {}
    public interface That<T> extends This<T, String> {}
    public interface Other<T> extends This<String, T> {}
    public interface Form<P1, P2> {}
    public static class And<K, V> implements That<Number>, This<K, V> {}
    public static class The<T> implements That<T>, Form<String, Integer> {}
    public static class Container<T> {
        public class Member {}
    }
    public static class Bounded<T extends Number & Comparable<T>> {}

    public List<String> stringList;
    public List<Comparable<String>> comparableStringList;
    public List<? extends CharSequence> extendsCharSequenceList;
    public List<? super String> superStringList;
    public List<? super Number> superNumberList;
    public List<? extends Number> extendsNumberList;
    public List<?> wildcardList;
    public ArrayList<String> stringArrayList;
    public String[] stringArray;
    public int[] intArray;
    public List<String>[] stringListArray;
    public B genericField;
    public Comparable<B>[] genericArray;
    public Container<String>.Member memberField;
    public Bounded<Integer> boundedField;

    // Tests constructor
    @Test
    public void testConstructor_defaultInstantiation_createsInstance() {
        TypeUtils utils = new TypeUtils();
        assertNotNull(utils);
    }

    // Tests isAssignable with null checks and primitive consistency
    @Test
    public void testIsAssignable_nullInputs_returnsExpected() {
        assertTrue(TypeUtils.isAssignable(null, Object.class));
        assertFalse(TypeUtils.isAssignable(null, int.class));
        assertFalse(TypeUtils.isAssignable(String.class, (Type) null));
        assertTrue(TypeUtils.isAssignable((Type) null, (Type) null));
    }

    // Tests isAssignable between simple classes
    @Test
    public void testIsAssignable_simpleClasses_returnsCorrectHierarchy() {
        assertTrue(TypeUtils.isAssignable(String.class, CharSequence.class));
        assertTrue(TypeUtils.isAssignable(Integer.class, Number.class));
        assertFalse(TypeUtils.isAssignable(Number.class, Integer.class));
    }

    // Tests isAssignable for ParameterizedType to Class
    @Test
    public void testIsAssignable_parameterizedTypeToClass_returnsTrueForRawAssignable() throws Exception {
        Type stringListType = getClass().getField("stringList").getGenericType();
        assertTrue(TypeUtils.isAssignable(stringListType, List.class));
        assertTrue(TypeUtils.isAssignable(stringListType, Collection.class));
        assertFalse(TypeUtils.isAssignable(stringListType, Set.class));
    }

    // Tests isAssignable for ParameterizedType to ParameterizedType
    @Test
    public void testIsAssignable_parameterizedTypeToParameterizedType_returnsCorrectAssignments() throws Exception {
        Type stringListType = getClass().getField("stringList").getGenericType();
        Type stringArrayListType = getClass().getField("stringArrayList").getGenericType();
        Type extendsCharSequenceListType = getClass().getField("extendsCharSequenceList").getGenericType();
        Type superStringListType = getClass().getField("superStringList").getGenericType();
        Type wildcardListType = getClass().getField("wildcardList").getGenericType();

        assertTrue(TypeUtils.isAssignable(stringArrayListType, stringListType));
        assertTrue(TypeUtils.isAssignable(stringListType, extendsCharSequenceListType));
        assertTrue(TypeUtils.isAssignable(stringListType, superStringListType));
        assertTrue(TypeUtils.isAssignable(stringListType, wildcardListType));
        assertFalse(TypeUtils.isAssignable(extendsCharSequenceListType, stringListType));
    }

    // Tests isAssignable with GenericArrayType
    @Test
    public void testIsAssignable_genericArrayType_returnsCorrectResult() throws Exception {
        Type stringListArrayType = getClass().getField("stringListArray").getGenericType();
        Type stringArrayType = getClass().getField("stringArray").getGenericType();

        assertTrue(TypeUtils.isAssignable(stringListArrayType, Object.class));
        assertTrue(TypeUtils.isAssignable(stringListArrayType, Object[].class));
        assertTrue(TypeUtils.isAssignable(stringListArrayType, stringListArrayType));
        assertFalse(TypeUtils.isAssignable(stringArrayType, stringListArrayType));
        assertFalse(TypeUtils.isAssignable(String.class, stringListArrayType));
    }

    // Tests isAssignable with WildcardType target
    @Test
    public void testIsAssignable_wildcardType_returnsExpected() throws Exception {
        Type wildcardListType = getClass().getField("wildcardList").getGenericType();
        ParameterizedType pType = (ParameterizedType) wildcardListType;
        Type wildcardType = pType.getActualTypeArguments()[0];

        assertTrue(TypeUtils.isAssignable(String.class, wildcardType));
        assertTrue(TypeUtils.isAssignable(Integer.class, wildcardType));
        assertTrue(TypeUtils.isAssignable(wildcardType, wildcardType));
        assertFalse(TypeUtils.isAssignable(wildcardType, String.class));
    }

    // Tests isAssignable with TypeVariable
    @Test
    public void testIsAssignable_typeVariable_returnsExpected() throws Exception {
        Type genericFieldType = getClass().getField("genericField").getGenericType();
        assertTrue(TypeUtils.isAssignable(genericFieldType, Object.class));
        assertTrue(TypeUtils.isAssignable(genericFieldType, genericFieldType));
        assertFalse(TypeUtils.isAssignable(String.class, genericFieldType));
    }

    // Tests isAssignable with primitive widening and mismatch
    @Test
    public void testIsAssignable_primitiveWidening_returnsTrue() {
        assertTrue(TypeUtils.isAssignable(int.class, long.class));
        assertFalse(TypeUtils.isAssignable(long.class, int.class));
    }

    // Tests isInstance method
    @Test
    public void testIsInstance_variousInputs_returnsCorrect() throws Exception {
        Type stringListType = getClass().getField("stringList").getGenericType();
        
        assertFalse(TypeUtils.isInstance(null, int.class));
        assertTrue(TypeUtils.isInstance(null, Object.class));
        assertFalse(TypeUtils.isInstance("test", null));
        assertTrue(TypeUtils.isInstance("test", String.class));
        assertTrue(TypeUtils.isInstance("test", CharSequence.class));
        assertFalse(TypeUtils.isInstance(123, String.class));
        assertTrue(TypeUtils.isInstance(new ArrayList<String>(), stringListType));
    }

    // Tests isArrayType and getArrayComponentType
    @Test
    public void testArrayTypeMethods_variousTypes_returnsComponentType() throws Exception {
        Type stringArrayType = getClass().getField("stringArray").getGenericType();
        Type stringListArrayType = getClass().getField("stringListArray").getGenericType();
        Type stringListType = getClass().getField("stringList").getGenericType();

        assertTrue(TypeUtils.isArrayType(stringArrayType));
        assertTrue(TypeUtils.isArrayType(stringListArrayType));
        assertFalse(TypeUtils.isArrayType(stringListType));

        assertEquals(String.class, TypeUtils.getArrayComponentType(stringArrayType));
        assertEquals(stringListType, TypeUtils.getArrayComponentType(stringListArrayType));
        assertNull(TypeUtils.getArrayComponentType(stringListType));
    }

    // Tests getTypeArguments for parameterized type
    @Test
    public void testGetTypeArguments_parameterizedType_returnsPopulatedMap() throws Exception {
        Type stringArrayListType = getClass().getField("stringArrayList").getGenericType();
        Map<TypeVariable<?>, Type> typeArgs = TypeUtils.getTypeArguments(stringArrayListType, Collection.class);
        
        assertNotNull(typeArgs);
        assertEquals(1, typeArgs.size());
        assertEquals(String.class, typeArgs.values().iterator().next());
    }

    // Tests getTypeArguments for incompatible class
    @Test
    public void testGetTypeArguments_incompatibleHierarchy_returnsNull() {
        Map<TypeVariable<?>, Type> typeArgs = TypeUtils.getTypeArguments(String.class, List.class);
        assertNull(typeArgs);
    }

    // Tests determineTypeArguments mapping from super parameterized type
    @Test
    public void testDetermineTypeArguments_fromSuperType_returnsMapping() throws Exception {
        Type stringListType = getClass().getField("stringList").getGenericType();
        Map<TypeVariable<?>, Type> typeArgs = TypeUtils.determineTypeArguments(ArrayList.class, (ParameterizedType) stringListType);
        
        assertNotNull(typeArgs);
        assertEquals(1, typeArgs.size());
        assertEquals(String.class, typeArgs.values().iterator().next());
    }

    // Tests determineTypeArguments with incompatible super type
    @Test
    public void testDetermineTypeArguments_incompatibleType_returnsNull() throws Exception {
        Type stringListType = getClass().getField("stringList").getGenericType();
        Map<TypeVariable<?>, Type> typeArgs = TypeUtils.determineTypeArguments(Set.class, (ParameterizedType) stringListType);
        assertNull(typeArgs);
    }

    // Tests normalizeUpperBounds and bounds resolution
    @Test
    public void testNormalizeUpperBounds_redundantBounds_stripsSuperType() {
        Type[] bounds = new Type[] { CharSequence.class, String.class, Object.class };
        Type[] normalized = TypeUtils.normalizeUpperBounds(bounds);
        
        assertEquals(1, normalized.length);
        assertEquals(String.class, normalized[0]);
    }

    // Tests getImplicitUpperBounds and getImplicitLowerBounds for WildcardType
    @Test
    public void testGetImplicitBounds_wildcardType_returnsExpected() throws Exception {
        Type extendsCharSequenceListType = getClass().getField("extendsCharSequenceList").getGenericType();
        WildcardType wildcardType = (WildcardType) ((ParameterizedType) extendsCharSequenceListType).getActualTypeArguments()[0];

        Type[] upperBounds = TypeUtils.getImplicitUpperBounds(wildcardType);
        assertEquals(1, upperBounds.length);
        assertEquals(CharSequence.class, upperBounds[0]);

        Type[] lowerBounds = TypeUtils.getImplicitLowerBounds(wildcardType);
        assertEquals(1, lowerBounds.length);
        assertNull(lowerBounds[0]);
    }

    // Tests getRawType resolution
    @Test
    public void testGetRawType_variousInputs_returnsRawClass() throws Exception {
        Type genericFieldType = getClass().getField("genericField").getGenericType();
        Type stringListType = getClass().getField("stringList").getGenericType();
        Type stringArrayType = getClass().getField("stringArray").getGenericType();

        assertEquals(String.class, TypeUtils.getRawType(String.class, null));
        assertEquals(List.class, TypeUtils.getRawType(stringListType, null));
        assertEquals(String[].class, TypeUtils.getRawType(stringArrayType, null));
        assertNull(TypeUtils.getRawType(genericFieldType, null));
        assertEquals(String.class, TypeUtils.getRawType(genericFieldType, TypeUtilsTest.class.getDeclaredField("stringList").getDeclaringClass()));
    }

    // Tests typesSatisfyVariables method
    @Test
    public void testTypesSatisfyVariables_validAndInvalidTypes_returnsExpected() throws Exception {
        TypeVariable<?>[] typeParameters = List.class.getTypeParameters();
        Map<TypeVariable<?>, Type> validMap = new HashMap<TypeVariable<?>, Type>();
        validMap.put(typeParameters[0], String.class);
        assertTrue(TypeUtils.typesSatisfyVariables(validMap));
    }

    // Additional coverage tests

    @Test
    public void testIsAssignable_wildcardCombinations() throws Exception {
        Type superStringListType = getClass().getField("superStringList").getGenericType();
        Type superNumberListType = getClass().getField("superNumberList").getGenericType();
        Type extendsNumberListType = getClass().getField("extendsNumberList").getGenericType();
        Type extendsCharSequenceListType = getClass().getField("extendsCharSequenceList").getGenericType();

        WildcardType superString = (WildcardType) ((ParameterizedType) superStringListType).getActualTypeArguments()[0];
        WildcardType superNumber = (WildcardType) ((ParameterizedType) superNumberListType).getActualTypeArguments()[0];
        WildcardType extendsNumber = (WildcardType) ((ParameterizedType) extendsNumberListType).getActualTypeArguments()[0];
        WildcardType extendsCharSequence = (WildcardType) ((ParameterizedType) extendsCharSequenceListType).getActualTypeArguments()[0];

        assertTrue(TypeUtils.isAssignable(superNumber, superString));
        assertFalse(TypeUtils.isAssignable(superString, superNumber));
        assertTrue(TypeUtils.isAssignable(Integer.class, extendsNumber));
        assertFalse(TypeUtils.isAssignable(String.class, extendsNumber));
        assertTrue(TypeUtils.isAssignable(Object.class, superString));
        assertFalse(TypeUtils.isAssignable(Integer.class, superString));
        assertFalse(TypeUtils.isAssignable(extendsNumber, superString));
        assertFalse(TypeUtils.isAssignable(superString, extendsNumber));
        assertFalse(TypeUtils.isAssignable(extendsCharSequence, extendsNumber));
    }

    @Test
    public void testIsAssignable_primitiveAndArrayTypes() throws Exception {
        Type intArrayType = getClass().getField("intArray").getGenericType();
        Type stringArrayType = getClass().getField("stringArray").getGenericType();

        assertFalse(TypeUtils.isAssignable(intArrayType, Object[].class));
        assertTrue(TypeUtils.isAssignable(intArrayType, Object.class));
        assertTrue(TypeUtils.isAssignable(intArrayType, Cloneable.class));
        assertTrue(TypeUtils.isAssignable(intArrayType, Serializable.class));
        assertFalse(TypeUtils.isAssignable(intArrayType, stringArrayType));
        assertFalse(TypeUtils.isAssignable(stringArrayType, intArrayType));
    }

    @Test
    public void testParameterizeAndParameterizeWithOwner() throws Exception {
        ParameterizedType pt = TypeUtils.parameterize(List.class, String.class);
        assertEquals(List.class, pt.getRawType());
        assertEquals(1, pt.getActualTypeArguments().length);
        assertEquals(String.class, pt.getActualTypeArguments()[0]);
        assertNull(pt.getOwnerType());

        ParameterizedType ownerType = TypeUtils.parameterize(Container.class, String.class);
        ParameterizedType memberType = TypeUtils.parameterizeWithOwner(ownerType, Container.Member.class);
        assertEquals(ownerType, memberType.getOwnerType());
        assertEquals(Container.Member.class, memberType.getRawType());

        Map<TypeVariable<?>, Type> typeArgMap = new HashMap<TypeVariable<?>, Type>();
        typeArgMap.put(List.class.getTypeParameters()[0], Integer.class);
        ParameterizedType fromMap = TypeUtils.parameterize(List.class, typeArgMap);
        assertEquals(Integer.class, fromMap.getActualTypeArguments()[0]);

        ParameterizedType fromOwnerMap = TypeUtils.parameterizeWithOwner(ownerType, Container.Member.class, Collections.<TypeVariable<?>, Type>emptyMap());
        assertEquals(Container.Member.class, fromOwnerMap.getRawType());
    }

    @Test
    public void testWildcardTypeBuilder() {
        WildcardType wt = TypeUtils.wildcardType()
                .withUpperBounds(Number.class)
                .withLowerBounds(Integer.class)
                .build();
        assertNotNull(wt);
        assertEquals(1, wt.getUpperBounds().length);
        assertEquals(Number.class, wt.getUpperBounds()[0]);
        assertEquals(1, wt.getLowerBounds().length);
        assertEquals(Integer.class, wt.getLowerBounds()[0]);
    }

    @Test
    public void testGenericArrayTypeBuilder() {
        GenericArrayType gat = TypeUtils.genericArrayType(String.class);
        assertNotNull(gat);
        assertEquals(String.class, gat.getGenericComponentType());
    }

    @Test
    public void testEqualsAndToStringAndToLongString() throws Exception {
        Type stringListType1 = getClass().getField("stringList").getGenericType();
        Type stringListType2 = getClass().getField("stringList").getGenericType();
        Type stringArrayType = getClass().getField("stringArray").getGenericType();

        assertTrue(TypeUtils.equals(stringListType1, stringListType2));
        assertFalse(TypeUtils.equals(stringListType1, stringArrayType));
        assertTrue(TypeUtils.equals((Type) null, (Type) null));
        assertFalse(TypeUtils.equals(stringListType1, null));
        assertFalse(TypeUtils.equals(null, stringListType1));

        WildcardType wt1 = TypeUtils.wildcardType().withUpperBounds(CharSequence.class).build();
        WildcardType wt2 = TypeUtils.wildcardType().withUpperBounds(CharSequence.class).build();
        assertTrue(TypeUtils.equals(wt1, wt2));

        GenericArrayType gat1 = TypeUtils.genericArrayType(stringListType1);
        GenericArrayType gat2 = TypeUtils.genericArrayType(stringListType2);
        assertTrue(TypeUtils.equals(gat1, gat2));

        assertNotNull(TypeUtils.toString(stringListType1));
        assertNotNull(TypeUtils.toLongString(stringListType1));
        assertNull(TypeUtils.toString((Type) null));
        assertNull(TypeUtils.toLongString((Type) null));
    }

    @Test
    public void testWrapTyped() throws Exception {
        Type stringListType = getClass().getField("stringList").getGenericType();
        Typed<List<String>> typed = TypeUtils.wrap(stringListType);
        assertNotNull(typed);
        assertEquals(stringListType, typed.getType());

        Typed<String> classTyped = TypeUtils.wrap(String.class);
        assertNotNull(classTyped);
        assertEquals(String.class, classTyped.getType());
    }

    @Test
    public void testContainsTypeVariables() throws Exception {
        Type genericFieldType = getClass().getField("genericField").getGenericType();
        Type stringListType = getClass().getField("stringList").getGenericType();
        Type genericArrayType = getClass().getField("genericArray").getGenericType();
        Type wildcardListType = getClass().getField("wildcardList").getGenericType();

        assertTrue(TypeUtils.containsTypeVariables(genericFieldType));
        assertTrue(TypeUtils.containsTypeVariables(genericArrayType));
        assertFalse(TypeUtils.containsTypeVariables(stringListType));
        assertFalse(TypeUtils.containsTypeVariables(String.class));
        assertFalse(TypeUtils.containsTypeVariables(wildcardListType));
        assertFalse(TypeUtils.containsTypeVariables(null));
    }

    @Test
    public void testUnrollVariables() throws Exception {
        Type genericFieldType = getClass().getField("genericField").getGenericType();
        TypeVariable<?> typeVarB = (TypeVariable<?>) genericFieldType;

        Map<TypeVariable<?>, Type> typeMap = new HashMap<TypeVariable<?>, Type>();
        typeMap.put(typeVarB, String.class);

        assertEquals(String.class, TypeUtils.unrollVariables(typeMap, genericFieldType));
        assertEquals(Integer.class, TypeUtils.unrollVariables(typeMap, Integer.class));
        assertNull(TypeUtils.unrollVariables(typeMap, null));

        Type genericArrayType = getClass().getField("genericArray").getGenericType();
        Type unrolledArray = TypeUtils.unrollVariables(typeMap, genericArrayType);
        assertNotNull(unrolledArray);
        assertTrue(unrolledArray instanceof GenericArrayType);

        Type wildcardListType = getClass().getField("wildcardList").getGenericType();
        Type unrolledWildcard = TypeUtils.unrollVariables(typeMap, wildcardListType);
        assertNotNull(unrolledWildcard);
    }

    @Test
    public void testGetImplicitBounds_typeVariable() throws Exception {
        Type genericFieldType = getClass().getField("genericField").getGenericType();
        TypeVariable<?> typeVar = (TypeVariable<?>) genericFieldType;
        Type[] bounds = TypeUtils.getImplicitBounds(typeVar);
        assertEquals(1, bounds.length);
        assertEquals(Object.class, bounds[0]);

        Type boundedFieldType = getClass().getField("boundedField").getGenericType();
        TypeVariable<?> boundedVar = Bounded.class.getTypeParameters()[0];
        Type[] boundedBounds = TypeUtils.getImplicitBounds(boundedVar);
        assertEquals(2, boundedBounds.length);
        assertEquals(Number.class, boundedBounds[0]);
    }

    @Test
    public void testTypesSatisfyVariables_invalidBounds() throws Exception {
        TypeVariable<?> boundedVar = Bounded.class.getTypeParameters()[0];
        Map<TypeVariable<?>, Type> invalidMap = new HashMap<TypeVariable<?>, Type>();
        invalidMap.put(boundedVar, Object.class);
        assertFalse(TypeUtils.typesSatisfyVariables(invalidMap));

        Map<TypeVariable<?>, Type> validMap = new HashMap<TypeVariable<?>, Type>();
        validMap.put(boundedVar, Integer.class);
        assertTrue(TypeUtils.typesSatisfyVariables(validMap));

        assertFalse(TypeUtils.typesSatisfyVariables(null));
    }

    @Test
    public void testGetTypeArguments_hierarchyScenarios() {
        ParameterizedType thatType = TypeUtils.parameterize(That.class, Number.class);
        Map<TypeVariable<?>, Type> typeArgs = TypeUtils.getTypeArguments(thatType, This.class);
        assertNotNull(typeArgs);
        TypeVariable<?>[] thisParams = This.class.getTypeParameters();
        assertEquals(Number.class, typeArgs.get(thisParams[0]));
        assertEquals(String.class, typeArgs.get(thisParams[1]));

        ParameterizedType andType = TypeUtils.parameterize(And.class, String.class, Integer.class);
        Map<TypeVariable<?>, Type> andArgs = TypeUtils.getTypeArguments(andType, This.class);
        assertNotNull(andArgs);

        ParameterizedType theType = TypeUtils.parameterize(The.class, Double.class);
        Map<TypeVariable<?>, Type> formArgs = TypeUtils.getTypeArguments(theType, Form.class);
        assertNotNull(formArgs);
        TypeVariable<?>[] formParams = Form.class.getTypeParameters();
        assertEquals(String.class, formArgs.get(formParams[0]));
        assertEquals(Integer.class, formArgs.get(formParams[1]));
    }

    @Test
    public void testNormalizeUpperBounds_emptyAndNull() {
        Type[] empty = new Type[0];
        assertEquals(0, TypeUtils.normalizeUpperBounds(empty).length);

        Type[] single = new Type[] { String.class };
        Type[] normalized = TypeUtils.normalizeUpperBounds(single);
        assertEquals(1, normalized.length);
        assertEquals(String.class, normalized[0]);
    }

    @Test
    public void testDetermineTypeArguments_nullInputs() {
        assertNull(TypeUtils.determineTypeArguments(null, (ParameterizedType) null));
    }
}