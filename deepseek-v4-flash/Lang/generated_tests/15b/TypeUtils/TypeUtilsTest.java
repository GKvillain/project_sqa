package org.apache.commons.lang3.reflect;

import static org.junit.Assert.*;
import java.lang.reflect.*;
import java.util.*;
import org.junit.Test;

public class TypeUtilsTest {

    // ---------- helpers to create type instances ----------
    private static ParameterizedType paramType(final Type rawType, final Type... typeArgs) {
        return new ParameterizedType() {
            @Override public Type[] getActualTypeArguments() { return typeArgs; }
            @Override public Type getRawType() { return rawType; }
            @Override public Type getOwnerType() { return null; }
            @Override public String getTypeName() { return rawType.getTypeName(); }
        };
    }

    private static GenericArrayType genericArray(final Type component) {
        return new GenericArrayType() {
            @Override public Type getGenericComponentType() { return component; }
            @Override public String getTypeName() { return component.getTypeName() + "[]"; }
        };
    }

    private static WildcardType wildcard(final Type[] upper, final Type[] lower) {
        return new WildcardType() {
            @Override public Type[] getUpperBounds() { return upper; }
            @Override public Type[] getLowerBounds() { return lower; }
            @Override public String getTypeName() { return "?"; }
        };
    }

    // ---------- test helper types ----------
    static class BoundA {}
    static interface BoundB {}
    static class MultiBound<T extends BoundA & BoundB> {}

    static class Simple<T> {}
    static class Sub<T> extends Simple<T> {}
    static interface Iface<T> {}
    static class Impl<T> implements Iface<T> {}

    // ===================== test methods =====================

    // Tests basic Class-to-Class assignability
    @Test
    public void testIsAssignable_classToClass_correctAssignment() {
        assertTrue(TypeUtils.isAssignable(Integer.class, Number.class));
        assertFalse(TypeUtils.isAssignable(Number.class, Integer.class));
        assertTrue(TypeUtils.isAssignable(String.class, Object.class));
        assertFalse(TypeUtils.isAssignable(Object.class, String.class));
    }

    // Tests ParameterizedType-to-ParameterizedType matching and non-matching
    @Test
    public void testIsAssignable_parameterizedTypeToParameterizedType_matching() {
        ParameterizedType listString = paramType(List.class, String.class);
        ParameterizedType collectionString = paramType(Collection.class, String.class);
        assertTrue(TypeUtils.isAssignable(listString, collectionString));

        ParameterizedType listInteger = paramType(List.class, Integer.class);
        assertFalse(TypeUtils.isAssignable(listString, listInteger));
    }

    // Tests TypeVariable-to-Class via bounds
    @Test
    public void testIsAssignable_typeVariableToClass_viaBounds() {
        TypeVariable<?> var = MultiBound.class.getTypeParameters()[0];
        // T extends BoundA & BoundB -> assignable to Object, BoundA, BoundB
        assertTrue(TypeUtils.isAssignable(var, Object.class));
        assertTrue(TypeUtils.isAssignable(var, BoundA.class));
        assertTrue(TypeUtils.isAssignable(var, BoundB.class));
        // not assignable to String
        assertFalse(TypeUtils.isAssignable(var, String.class));
    }

    // Tests same TypeVariable assignable to itself
    @Test
    public void testIsAssignable_typeVariableToSameTypeVariable_returnsTrue() {
        TypeVariable<?> var = Simple.class.getTypeParameters()[0];
        assertTrue(TypeUtils.isAssignable(var, var));
    }

    // Tests TypeVariable-to-TypeVariable via bounds
    @Test
    public void testIsAssignable_typeVariableToTypeVariable_viaBounds() {
        TypeVariable<?> varA = MultiBound.class.getTypeParameters()[0];
        TypeVariable<?> varB = MultiBound.class.getTypeParameters()[0]; // same bounds
        assertTrue(TypeUtils.isAssignable(varA, varB));
    }

    // Tests GenericArrayType assignability
    @Test
    public void testIsAssignable_genericArrayTypeToClass() {
        GenericArrayType strArray = genericArray(String.class);
        assertTrue(TypeUtils.isAssignable(strArray, Object.class));
        assertTrue(TypeUtils.isAssignable(strArray, String[].class));
        assertFalse(TypeUtils.isAssignable(strArray, String.class));
    }

    // Tests null and null target handling
    @Test
    public void testIsAssignable_nullAndNullTarget() {
        assertTrue(TypeUtils.isAssignable(null, Object.class));
        assertFalse(TypeUtils.isAssignable(null, int.class));
        assertFalse(TypeUtils.isAssignable(String.class, (Type) null));
    }

    // Tests isInstance with various cases
    @Test
    public void testIsInstance_typicalCases() {
        assertTrue(TypeUtils.isInstance("hello", String.class));
        assertFalse(TypeUtils.isInstance("hello", Integer.class));
        assertTrue(TypeUtils.isInstance(null, String.class));
        assertFalse(TypeUtils.isInstance(null, int.class));
    }

    // Tests getTypeArguments from ParameterizedType
    @Test
    public void testGetTypeArguments_parameterizedType_returnsMap() {
        ParameterizedType listString = paramType(List.class, String.class);
        Map<TypeVariable<?>, Type> result = TypeUtils.getTypeArguments(listString);
        assertEquals(String.class, result.get(List.class.getTypeParameters()[0]));
        assertEquals(1, result.size());
    }

    // Tests getTypeArguments from Type to Class via inheritance
    @Test
    public void testGetTypeArguments_typeToClass_inheritance() {
        ParameterizedType implString = paramType(Impl.class, String.class);
        Map<TypeVariable<?>, Type> result = TypeUtils.getTypeArguments(implString, Iface.class);
        assertNotNull(result);
        assertEquals(String.class, result.get(Iface.class.getTypeParameters()[0]));
    }

    // Tests getTypeArguments returns null for non-assignable types
    @Test
    public void testGetTypeArguments_typeToClass_notAssignable_returnsNull() {
        assertNull(TypeUtils.getTypeArguments(String.class, List.class));
    }

    // Tests getRawType for TypeVariable and GenericArrayType
    @Test
    public void testGetRawType_typeVariableAndGenericArray() {
        ParameterizedType listString = paramType(List.class, String.class);
        TypeVariable<?> eVar = List.class.getTypeParameters()[0];
        assertEquals(String.class, TypeUtils.getRawType(eVar, listString));

        GenericArrayType strArray = genericArray(String.class);
        assertEquals(String[].class, TypeUtils.getRawType(strArray, null));
    }

    // Tests normalizeUpperBounds removes redundant types
    @Test
    public void testNormalizeUpperBounds_redundantBounds_removed() {
        Type[] bounds = new Type[] { List.class, Collection.class, Iterable.class };
        Type[] normalized = TypeUtils.normalizeUpperBounds(bounds);
        // only the most specific (List) should remain
        assertEquals(1, normalized.length);
        assertEquals(List.class, normalized[0]);
    }

    // Tests isArrayType
    @Test
    public void testIsArrayType_variousInputs() {
        assertTrue(TypeUtils.isArrayType(String[].class));
        assertTrue(TypeUtils.isArrayType(genericArray(String.class)));
        assertFalse(TypeUtils.isArrayType(String.class));
    }

    // Tests getArrayComponentType
    @Test
    public void testGetArrayComponentType_variousInputs() {
        assertEquals(String.class, TypeUtils.getArrayComponentType(String[].class));
        assertEquals(String.class, TypeUtils.getArrayComponentType(genericArray(String.class)));
        assertNull(TypeUtils.getArrayComponentType(String.class));
    }

    // Tests determineTypeArguments for class implementing parameterized interface
    @Test
    public void testDetermineTypeArguments_classImplementingParameterizedInterface() {
        ParameterizedType superIface = paramType(Iface.class, String.class);
        Map<TypeVariable<?>, Type> result = TypeUtils.determineTypeArguments(Impl.class, superIface);
        assertNotNull(result);
        assertEquals(String.class, result.get(Impl.class.getTypeParameters()[0]));
    }

    // Tests typesSatisfyVariables with valid assignments
    @Test
    public void testTypesSatisfyVariables_validAssignments_returnsTrue() {
        TypeVariable<?> tVar = Simple.class.getTypeParameters()[0];
        Map<TypeVariable<?>, Type> assignments = new HashMap<TypeVariable<?>, Type>();
        assignments.put(tVar, String.class);
        assertTrue(TypeUtils.typesSatisfyVariables(assignments));
    }

    // Tests WildcardType-to-WildcardType assignment
    @Test
    public void testIsAssignable_wildcardToWildcard() {
        WildcardType wcObject = wildcard(new Type[]{Object.class}, new Type[]{});
        WildcardType wcString = wildcard(new Type[]{String.class}, new Type[]{});
        // ? extends String is assignable to ? extends Object
        assertTrue(TypeUtils.isAssignable(wcString, wcObject));
        // reverse is false
        assertFalse(TypeUtils.isAssignable(wcObject, wcString));
    }

    // Tests type-to-WildcardType assignment
    @Test
    public void testIsAssignable_typeToWildcardType() {
        WildcardType wcNumber = wildcard(new Type[]{Number.class}, new Type[]{});
        assertTrue(TypeUtils.isAssignable(Integer.class, wcNumber));
        assertFalse(TypeUtils.isAssignable(String.class, wcNumber));
    }

    // Tests WildcardType-to-Class (should be false)
    @Test
    public void testIsAssignable_wildcardToClass_returnsFalse() {
        WildcardType wc = wildcard(new Type[]{Object.class}, new Type[]{});
        assertFalse(TypeUtils.isAssignable(wc, String.class));
    }

    // Tests ParameterizedType-to-ParameterizedType with type variable arguments
    @Test
    public void testIsAssignable_parameterizedTypeWithVariableToParameterizedType() {
        TypeVariable<?> tVar = Simple.class.getTypeParameters()[0];
        ParameterizedType simpleT = paramType(Simple.class, tVar);
        ParameterizedType simpleString = paramType(Simple.class, String.class);
        // Simple<T> is assignable to Simple<String>? No, because T is a type variable and String is not a supertype of T.
        // Actually T has implicit bound Object, so Simple<T> is not assignable to Simple<String> because T could be Integer.
        assertFalse(TypeUtils.isAssignable(simpleT, simpleString));
        // But Simple<T> is assignable to Simple<Object>? T's bound is Object, so yes.
        ParameterizedType simpleObject = paramType(Simple.class, Object.class);
        assertTrue(TypeUtils.isAssignable(simpleT, simpleObject));
    }

    // ===================== NEW TEST CASES =====================

    // Tests ParameterizedType-to-Class (raw type) - e.g., List<String> to List.class
    @Test
    public void testIsAssignable_parameterizedTypeToRawClass() {
        ParameterizedType listString = paramType(List.class, String.class);
        // parameterized type is assignable to its raw class
        assertTrue(TypeUtils.isAssignable(listString, List.class));
        // but raw class is not assignable to parameterized type (unless unchecked)
        assertFalse(TypeUtils.isAssignable(List.class, listString));
    }

    // Tests GenericArrayType-to-GenericArrayType with different components
    @Test
    public void testIsAssignable_genericArrayToGenericArray() {
        GenericArrayType strArray = genericArray(String.class);
        GenericArrayType objArray = genericArray(Object.class);
        // String[] is assignable to Object[]
        assertTrue(TypeUtils.isAssignable(strArray, objArray));
        // Object[] is not assignable to String[]
        assertFalse(TypeUtils.isAssignable(objArray, strArray));
    }

    // Tests GenericArrayType-to-ParameterizedType (should be false)
    @Test
    public void testIsAssignable_genericArrayToParameterizedType() {
        GenericArrayType strArray = genericArray(String.class);
        ParameterizedType listString = paramType(List.class, String.class);
        assertFalse(TypeUtils.isAssignable(strArray, listString));
    }

    // Tests WildcardType with lower bounds assignment
    @Test
    public void testIsAssignable_wildcardWithLowerBound() {
        // ? super String is assignable to ? super Object? No, because String is narrower.
        WildcardType wcSuperString = wildcard(new Type[]{Object.class}, new Type[]{String.class});
        WildcardType wcSuperObject = wildcard(new Type[]{Object.class}, new Type[]{Object.class});
        // ? super String is assignable to ? super Object (since String extends Object => lower bound wider)
        // Actually ? super String means any type that is a supertype of String, including Object.
        // ? super Object means any type that is a supertype of Object, which is only Object itself.
        // So ? super String is more specific? The assignability rules: ? super T is assignable to ? super S if S is a supertype of T.
        assertTrue(TypeUtils.isAssignable(wcSuperString, wcSuperObject));
        // Reverse should be false
        assertFalse(TypeUtils.isAssignable(wcSuperObject, wcSuperString));
    }

    // Tests TypeVariable-to-WildcardType assignment
    @Test
    public void testIsAssignable_typeVariableToWildcardType() {
        TypeVariable<?> tVar = Simple.class.getTypeParameters()[0]; // bound Object
        WildcardType wcObject = wildcard(new Type[]{Object.class}, new Type[]{});
        assertTrue(TypeUtils.isAssignable(tVar, wcObject));
        // TypeVariable with narrower bound to wildcard with narrower bound
        TypeVariable<?> multiVar = MultiBound.class.getTypeParameters()[0]; // extends BoundA & BoundB
        WildcardType wcBoundA = wildcard(new Type[]{BoundA.class}, new Type[]{});
        assertTrue(TypeUtils.isAssignable(multiVar, wcBoundA));
        // But not to wildcard with more specific bound (e.g., String)
        WildcardType wcString = wildcard(new Type[]{String.class}, new Type[]{});
        assertFalse(TypeUtils.isAssignable(multiVar, wcString));
    }

    // Tests WildcardType-to-TypeVariable assignment (should be false in general)
    @Test
    public void testIsAssignable_wildcardToTypeVariable_returnsFalse() {
        TypeVariable<?> tVar = Simple.class.getTypeParameters()[0];
        WildcardType wcObject = wildcard(new Type[]{Object.class}, new Type[]{});
        assertFalse(TypeUtils.isAssignable(wcObject, tVar));
    }

    // Tests getTypeArguments from Class to Class (non-parameterized)
    @Test
    public void testGetTypeArguments_classToClass_noTypeParams() {
        // Without type parameters, should return empty map
        Map<TypeVariable<?>, Type> result = TypeUtils.getTypeArguments(String.class, Object.class);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Tests getRawType with null input
    @Test
    public void testGetRawType_nullReturnsNull() {
        assertNull(TypeUtils.getRawType(null, null));
    }

    // Tests normalizeUpperBounds with empty array
    @Test
    public void testNormalizeUpperBounds_emptyArray() {
        Type[] empty = new Type[0];
        Type[] normalized = TypeUtils.normalizeUpperBounds(empty);
        assertNotNull(normalized);
        assertEquals(0, normalized.length);
    }

    // Tests normalizeUpperBounds with array containing interfaces and a superclass
    @Test
    public void testNormalizeUpperBounds_withInterfacesAndClass() {
        Type[] bounds = new Type[] { Serializable.class, Comparable.class, Object.class };
        Type[] normalized = TypeUtils.normalizeUpperBounds(bounds);
        // Object is most general, so Serializable and Comparable are kept (no superset relation)
        // Actually Serializable and Comparable are unrelated, both kept; Object is redundant.
        assertEquals(2, normalized.length);
        assertTrue(Arrays.asList(normalized).contains(Serializable.class));
        assertTrue(Arrays.asList(normalized).contains(Comparable.class));
    }

    // Tests typesSatisfyVariables with invalid assignment (type not within bound)
    @Test
    public void testTypesSatisfyVariables_invalidAssignment_returnsFalse() {
        TypeVariable<?> multiVar = MultiBound.class.getTypeParameters()[0]; // extends BoundA & BoundB
        Map<TypeVariable<?>, Type> assignments = new HashMap<TypeVariable<?>, Type>();
        assignments.put(multiVar, String.class); // String does not extend both BoundA and BoundB
        assertFalse(TypeUtils.typesSatisfyVariables(assignments));
    }

    // Tests isInstance with ParameterizedType (e.g., List<String>)
    @Test
    public void testIsInstance_withParameterizedType() {
        ParameterizedType listString = paramType(List.class, String.class);
        assertTrue(TypeUtils.isInstance(new ArrayList<String>(), listString));
        assertFalse(TypeUtils.isInstance(new ArrayList<Integer>(), listString)); // raw type? Actually both are ArrayList<?>, but parameterized type checks raw type?
        // The behavior depends on implementation: isInstance typically only checks raw class.
        // At least it should not throw exception.
    }

    // Tests determineTypeArguments for class extending parameterized superclass
    @Test
    public void testDetermineTypeArguments_classExtendingParameterizedSuperclass() {
        ParameterizedType superClass = paramType(Simple.class, String.class);
        Map<TypeVariable<?>, Type> result = TypeUtils.determineTypeArguments(Sub.class, superClass);
        assertNotNull(result);
        assertEquals(String.class, result.get(Sub.class.getTypeParameters()[0]));
    }

    // Tests isAssignable with identical ParameterizedTypes
    @Test
    public void testIsAssignable_identicalParameterizedTypes() {
        ParameterizedType listString1 = paramType(List.class, String.class);
        ParameterizedType listString2 = paramType(List.class, String.class);
        assertTrue(TypeUtils.isAssignable(listString1, listString2));
    }

    // Tests isAssignable with Class-to-ParameterizedType where class implements interface (e.g., ArrayList to List<String>)
    @Test
    public void testIsAssignable_classToParameterizedType_implements() {
        // ArrayList.class is a raw type, but assignable to List<String>? Usually yes via unchecked.
        // This might be covered by existing? Not explicitly.
        ParameterizedType listString = paramType(List.class, String.class);
        assertTrue(TypeUtils.isAssignable(ArrayList.class, listString));
    }
}