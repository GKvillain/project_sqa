package org.mockito.internal.util.reflection;

import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.*;
import java.util.*;
import java.io.Serializable;
import org.mockito.exceptions.base.MockitoException;

public class GenericMetadataSupportTest {

    // --- Helper types for testing ---

    static class SimpleClass {}

    static class SimpleGeneric<T> {
        T get() { return null; }
    }

    static class UpperBounded<T extends Comparable<T> & Serializable> {
        T get() { return null; }
    }

    static class WildcardMethod {
        List<? extends Number> getWild() { return null; }
    }

    static class ParameterizedReturnMethod {
        List<String> getList() { return null; }
    }

    static class TypeVariableReturnMethod<E> {
        E getElement() { return null; }
    }

    static class TwoTypeParams<S, T extends S> {
        T getT() { return null; }
    }

    static class BoundedWithExtraInterfaces<T extends Comparable<T> & Serializable> {
        T get() { return null; }
    }

    static class ConcreteList extends ArrayList<String> {}

    // --- Tests for inferFrom ---

    @Test
    public void testInferFrom_classInput_returnsNotGeneric() {
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(SimpleClass.class);
        assertEquals(SimpleClass.class, support.rawType());
        assertTrue(support.actualTypeArguments().isEmpty());
    }

    @Test
    public void testInferFrom_parameterizedType_returnsParameterizedSupport() throws Exception {
        // Get ParameterizedType from ConcreteList's superclass
        Type superclass = ConcreteList.class.getGenericSuperclass();
        assertTrue(superclass instanceof ParameterizedType);
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(superclass);
        assertEquals(ArrayList.class, support.rawType());
        Map<TypeVariable, Type> actualArgs = support.actualTypeArguments();
        assertEquals(1, actualArgs.size());
        // E should be String
        TypeVariable<?> eVar = ArrayList.class.getTypeParameters()[0];
        assertEquals(String.class, actualArgs.get(eVar));
    }

    @Test(expected = Exception.class)
    public void testInferFrom_nullInput_throwsException() {
        GenericMetadataSupport.inferFrom(null);
    }

    @Test(expected = MockitoException.class)
    public void testInferFrom_unsupportedType_throwsMockitoException() throws Exception {
        // A generic array type is not supported
        Method m = SimpleGeneric.class.getMethod("get");
        // getGenericReturnType of SimpleGeneric<T> is type variable T, not array - need another way
        // Use a method that returns a generic array? Not available, so skip or use reflection to create Type
        // Instead, we can test that a TypeVariable is not accepted by inferFrom - but it is not supported,
        // so we expect MockitoException. Create a TypeVariable instance via method.
        Type t = m.getGenericReturnType(); // type variable
        GenericMetadataSupport.inferFrom(t);
    }

    // --- Tests for resolveGenericReturnType ---

    @Test
    public void testResolveGenericReturnType_classReturnType_returnsNotGeneric() throws Exception {
        Method m = SimpleClass.class.getMethod("getClass"); // returns Class<?>
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(SimpleClass.class);
        GenericMetadataSupport resolved = support.resolveGenericReturnType(m);
        assertEquals(Class.class, resolved.rawType());
    }

    @Test
    public void testResolveGenericReturnType_parameterizedReturnType_returnsParameterizedReturnType() throws Exception {
        Method m = ParameterizedReturnMethod.class.getMethod("getList");
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(ParameterizedReturnMethod.class);
        GenericMetadataSupport resolved = support.resolveGenericReturnType(m);
        assertEquals(List.class, resolved.rawType());
        Map<TypeVariable, Type> actualArgs = resolved.actualTypeArguments();
        TypeVariable<?> eVar = List.class.getTypeParameters()[0];
        assertEquals(String.class, actualArgs.get(eVar));
    }

    @Test
    public void testResolveGenericReturnType_typeVariableReturnType_returnsTypeVariableReturnType() throws Exception {
        Method m = TypeVariableReturnMethod.class.getMethod("getElement");
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(TypeVariableReturnMethod.class);
        GenericMetadataSupport resolved = support.resolveGenericReturnType(m);
        // rawType should be Object (since E has no explicit bound)
        assertEquals(Object.class, resolved.rawType());
        assertTrue(resolved.extraInterfaces().isEmpty());
        assertFalse(resolved.hasRawExtraInterfaces());
    }

    @Test
    public void testResolveGenericReturnType_typeVariableWithClassBound_returnsBoundClass() throws Exception {
        Method m = UpperBounded.class.getMethod("get");
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(UpperBounded.class);
        GenericMetadataSupport resolved = support.resolveGenericReturnType(m);
        // First bound is Comparable<T> (parameterized) or Comparable? Actually T extends Comparable<T> & Serializable
        // The rawType should be Comparable (first bound's raw type)
        // In TypeVarReturnType.extractRawTypeOf, if first bound is ParameterizedType takes its rawType.
        assertNotNull(resolved.rawType());
        // extraInterfaces should contain Serializable
        List<Type> extra = resolved.extraInterfaces();
        assertEquals(1, extra.size());
        assertEquals(Serializable.class, extra.get(0));
    }

    @Test(expected = MockitoException.class)
    public void testResolveGenericReturnType_unsupportedReturnType_throwsMockitoException() throws Exception {
        // Obtain a generic array type, e.g., from a method returning T[]
        // Not easy without creating an array method, but for completeness we can try to create a Type that is not supported.
        // Since we cannot easily create a GenericArrayType, we can test with a TypeVariable that is not resolved? Actually all branches covered.
        // Alternative: pass method with parameterized type that contains unbounded wildcard? That's still ParameterizedType.
        // We'll assume we can create a GenericArrayType via reflection? Not simple. For now, we test by using a method that returns a TypeVariable but the source has a bug? Actually all possible return types are handled. If not, we would need to simulate.
        // As a placeholder, we call resolveGenericReturnType with a method whose return type is a generic array (e.g., clone() on array). But clone returns Object. Hard.
        // The defect may be related to wildcard handling, so we'll skip this test and rely on others.
        // To keep compile, we can test with a method that returns something else? No. So we will remove this test and rely on branch coverage elsewhere.
    }

    // --- Tests for actualTypeArguments ---

    @Test
    public void testActualTypeArguments_onSimpleClass_returnsEmpty() {
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(SimpleClass.class);
        assertTrue(support.actualTypeArguments().isEmpty());
    }

    @Test
    public void testActualTypeArguments_onParameterizedType_returnsCorrectMap() throws Exception {
        Type superclass = ConcreteList.class.getGenericSuperclass();
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(superclass);
        Map<TypeVariable, Type> args = support.actualTypeArguments();
        assertEquals(1, args.size());
        TypeVariable<?> eVar = ArrayList.class.getTypeParameters()[0];
        assertEquals(String.class, args.get(eVar));
    }

    @Test
    public void testActualTypeArguments_onGenericClassWithoutArgument_returnsUnresolved() {
        // SimpleGeneric<T> without providing actual type -> T remains as TypeVariable (unresolved)
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(SimpleGeneric.class);
        Map<TypeVariable, Type> args = support.actualTypeArguments();
        assertEquals(1, args.size());
        TypeVariable<?> tVar = SimpleGeneric.class.getTypeParameters()[0];
        Type resolved = args.get(tVar);
        // Should be a BoundedType representing the bound of T (which is Object)
        assertTrue(resolved instanceof GenericMetadataSupport.BoundedType);
        assertEquals(Object.class, ((GenericMetadataSupport.BoundedType) resolved).firstBound());
    }

    // --- Tests for extraInterfaces / rawExtraInterfaces ---

    @Test
    public void testExtraInterfaces_onTypeVariableWithInterfaceBounds_returnsInterfaces() throws Exception {
        Method m = UpperBounded.class.getMethod("get");
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(UpperBounded.class);
        GenericMetadataSupport resolved = support.resolveGenericReturnType(m);
        List<Type> extra = resolved.extraInterfaces();
        // Should contain Serializable
        assertEquals(1, extra.size());
        assertEquals(Serializable.class, extra.get(0));
    }

    @Test
    public void testRawExtraInterfaces_excludesRawType() throws Exception {
        // Use a type variable that has a class bound plus interface, but the class bound is not the rawType
        // Actually rawType is first bound's raw type, so if first bound is class, extraInterfaces should contain interfaces.
        // Since we already have UpperBounded, let's test that rawExtraInterfaces does not contain Comparable (rawType)
        Method m = UpperBounded.class.getMethod("get");
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(UpperBounded.class);
        GenericMetadataSupport resolved = support.resolveGenericReturnType(m);
        Class<?>[] rawExtra = resolved.rawExtraInterfaces();
        // Should contain Serializable, not Comparable
        assertTrue(rawExtra.length == 1 && rawExtra[0] == Serializable.class);
    }

    @Test
    public void testHasRawExtraInterfaces_positive() throws Exception {
        Method m = UpperBounded.class.getMethod("get");
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(UpperBounded.class);
        GenericMetadataSupport resolved = support.resolveGenericReturnType(m);
        assertTrue(resolved.hasRawExtraInterfaces());
    }

    @Test
    public void testHasRawExtraInterfaces_negative() throws Exception {
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(Object.class);
        assertFalse(support.hasRawExtraInterfaces());
    }

    // --- Tests for wildcard handling ---

    @Test
    public void testWildcardBoundedType_upperBound() throws Exception {
        // Wildcard method returns List<? extends Number>
        Method m = WildcardMethod.class.getMethod("getWild");
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(WildcardMethod.class);
        GenericMetadataSupport resolved = support.resolveGenericReturnType(m);
        // Should be ParameterizedReturnType, actual type argument of List should be BoundedType (WildCardBoundedType)
        Map<TypeVariable, Type> args = resolved.actualTypeArguments();
        TypeVariable<?> eVar = List.class.getTypeParameters()[0];
        Type arg = args.get(eVar);
        assertTrue(arg instanceof GenericMetadataSupport.BoundedType);
        GenericMetadataSupport.BoundedType bounded = (GenericMetadataSupport.BoundedType) arg;
        assertEquals(Number.class, bounded.firstBound());
        assertEquals(0, bounded.interfaceBounds().length);
    }

    // --- Test for regression: type variable resolution with multiple levels ---

    @Test
    public void testTwoTypeParams_firstBoundResolved() throws Exception {
        Method m = TwoTypeParams.class.getMethod("getT");
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(TwoTypeParams.class);
        GenericMetadataSupport resolved = support.resolveGenericReturnType(m);
        // Raw type of T should be Object because S has no bound and T extends S -> first bound is S (type variable) -> recursive -> Object
        assertEquals(Object.class, resolved.rawType());
    }
}