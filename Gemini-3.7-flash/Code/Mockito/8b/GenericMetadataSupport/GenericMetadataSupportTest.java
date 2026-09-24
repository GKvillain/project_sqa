package org.mockito.internal.util.reflection;

import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GenericMetadataSupportTest {

    interface BaseInterface<T> {
        T get();
    }

    interface UpperBoundedInterface<E extends Comparable<E> & Cloneable> {
        E getUpper();
    }

    interface GenericsNest<K extends Comparable<K> & Cloneable> extends Map<K, Set<Number>> {
        Set<Number> remove(Object key);
        List<? super Integer> returning_wildcard_with_class_lower_bound();
        List<? super K> returning_wildcard_with_typeVar_lower_bound();
        List<? extends K> returning_wildcard_with_typeVar_upper_bound();
        K returningK();
        <O extends K> List<O> paramType_with_type_params();
        <S, T extends S> T two_type_params();
        <O extends K> O typeVar_with_type_params();
        Number returningNonGeneric();
        K[] returningGenericArray();
    }

    interface SubInterface extends BaseInterface<String> {
        String nonGenericMethod();
        List<Integer> parameterizedMethod();
    }

    static class GenericClass<T, U extends Number> {
        public T getT() { return null; }
        public U getU() { return null; }
        public <V> V getGeneric(V val) { return val; }
    }

    static class ConcreteClass extends GenericClass<String, Integer> {
    }

    interface DeepInterfaceA<X> {
        X getA();
    }

    interface DeepInterfaceB<Y> extends DeepInterfaceA<List<Y>> {
        Y getB();
    }

    interface DeepInterfaceC extends DeepInterfaceB<Double> {
    }

    // Tests inferFrom with Class type representing a non-generic class
    @Test
    public void testInferFrom_classWithoutGenerics_returnsCorrectRawType() {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(String.class);
        assertEquals(String.class, metadata.rawType());
        assertTrue(metadata.actualTypeArguments().isEmpty());
        assertFalse(metadata.hasRawExtraInterfaces());
        assertEquals(0, metadata.rawExtraInterfaces().length);
        assertTrue(metadata.extraInterfaces().isEmpty());
    }

    // Tests inferFrom with ParameterizedType and resolves its type arguments
    @Test
    public void testInferFrom_parameterizedType_returnsCorrectRawAndActualTypeArguments() throws Exception {
        Method method = SubInterface.class.getMethod("parameterizedMethod");
        Type returnType = method.getGenericReturnType();
        assertTrue(returnType instanceof ParameterizedType);

        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(returnType);
        assertEquals(List.class, metadata.rawType());
        Map<TypeVariable, Type> args = metadata.actualTypeArguments();
        assertEquals(1, args.size());
        assertEquals(Integer.class, args.values().iterator().next());
    }

    // Tests inferFrom with null input throws exception
    @Test(expected = RuntimeException.class)
    public void testInferFrom_nullType_throwsException() {
        GenericMetadataSupport.inferFrom(null);
    }

    // Tests inferFrom with unsupported Type throws MockitoException
    @Test(expected = MockitoException.class)
    public void testInferFrom_unsupportedType_throwsMockitoException() throws Exception {
        Method method = GenericsNest.class.getMethod("returning_wildcard_with_class_lower_bound");
        ParameterizedType listType = (ParameterizedType) method.getGenericReturnType();
        Type wildcardType = listType.getActualTypeArguments()[0];
        GenericMetadataSupport.inferFrom(wildcardType);
    }

    // Tests resolveGenericReturnType on non-generic method
    @Test
    public void testResolveGenericReturnType_nonGenericMethod_returnsNotGenericReturnTypeSupport() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(SubInterface.class);
        Method method = SubInterface.class.getMethod("nonGenericMethod");
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(String.class, returnMetadata.rawType());
        assertFalse(returnMetadata.hasRawExtraInterfaces());
    }

    // Tests resolveGenericReturnType on inherited generic method resolved via class hierarchy
    @Test
    public void testResolveGenericReturnType_inheritedGenericMethod_resolvesActualType() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(SubInterface.class);
        Method method = BaseInterface.class.getMethod("get");
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(String.class, returnMetadata.rawType());
    }

    // Tests resolveGenericReturnType on parameterized return type
    @Test
    public void testResolveGenericReturnType_parameterizedReturnType_resolvesRawType() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(SubInterface.class);
        Method method = SubInterface.class.getMethod("parameterizedMethod");
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(List.class, returnMetadata.rawType());
    }

    // Tests resolveGenericReturnType on generic class with subclass type resolution
    @Test
    public void testResolveGenericReturnType_subclassWithTypeArguments_resolvesTypeVariables() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(ConcreteClass.class);
        Method getT = GenericClass.class.getMethod("getT");
        Method getU = GenericClass.class.getMethod("getU");

        GenericMetadataSupport returnT = metadata.resolveGenericReturnType(getT);
        GenericMetadataSupport returnU = metadata.resolveGenericReturnType(getU);

        assertEquals(String.class, returnT.rawType());
        assertEquals(Integer.class, returnU.rawType());
    }

    // Tests resolveGenericReturnType with method level type parameter
    @Test
    public void testResolveGenericReturnType_methodLevelTypeParameter_resolvesRawType() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(GenericClass.class);
        Method method = GenericClass.class.getMethod("getGeneric", Object.class);
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(Object.class, returnMetadata.rawType());
    }

    // Tests upper bounded type variable with extra interface bounds
    @Test
    public void testResolveGenericReturnType_typeVariableWithMultipleBounds_resolvesExtraInterfaces() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(UpperBoundedInterface.class);
        Method method = UpperBoundedInterface.class.getMethod("getUpper");
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(Comparable.class, returnMetadata.rawType());
        assertTrue(returnMetadata.hasRawExtraInterfaces());
        assertArrayEquals(new Class<?>[]{Cloneable.class}, returnMetadata.rawExtraInterfaces());
        assertEquals(1, returnMetadata.extraInterfaces().size());
    }

    // Tests self-referential generic type definitions from GenericsNest interface
    @Test
    public void testResolveGenericReturnType_nestedGenerics_resolvesCorrectly() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(GenericsNest.class);

        Method returningK = GenericsNest.class.getMethod("returningK");
        GenericMetadataSupport returnK = metadata.resolveGenericReturnType(returningK);
        assertEquals(Comparable.class, returnK.rawType());
        assertTrue(returnK.hasRawExtraInterfaces());
        assertArrayEquals(new Class<?>[]{Cloneable.class}, returnK.rawExtraInterfaces());

        Method returningNonGeneric = GenericsNest.class.getMethod("returningNonGeneric");
        GenericMetadataSupport returnNonGeneric = metadata.resolveGenericReturnType(returningNonGeneric);
        assertEquals(Number.class, returnNonGeneric.rawType());

        Method paramTypeWithParams = GenericsNest.class.getMethod("paramType_with_type_params");
        GenericMetadataSupport returnParamType = metadata.resolveGenericReturnType(paramTypeWithParams);
        assertEquals(List.class, returnParamType.rawType());

        Method removeMethod = GenericsNest.class.getMethod("remove", Object.class);
        GenericMetadataSupport returnRemove = metadata.resolveGenericReturnType(removeMethod);
        assertEquals(Set.class, returnRemove.rawType());
    }

    // Tests type variable with wildcard upper and lower bounds
    @Test
    public void testResolveGenericReturnType_wildcardBounds_resolvesRawType() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(GenericsNest.class);

        Method lowerBoundMethod = GenericsNest.class.getMethod("returning_wildcard_with_class_lower_bound");
        GenericMetadataSupport lowerBoundMeta = metadata.resolveGenericReturnType(lowerBoundMethod);
        assertEquals(List.class, lowerBoundMeta.rawType());

        Method typeVarLowerBound = GenericsNest.class.getMethod("returning_wildcard_with_typeVar_lower_bound");
        GenericMetadataSupport typeVarLowerMeta = metadata.resolveGenericReturnType(typeVarLowerBound);
        assertEquals(List.class, typeVarLowerMeta.rawType());

        Method typeVarUpperBound = GenericsNest.class.getMethod("returning_wildcard_with_typeVar_upper_bound");
        GenericMetadataSupport typeVarUpperMeta = metadata.resolveGenericReturnType(typeVarUpperBound);
        assertEquals(List.class, typeVarUpperMeta.rawType());
    }

    // Tests TypeVarBoundedType equals, hashCode, and toString methods
    @Test
    public void testTypeVarBoundedType_equalsAndHashCodeAndToString() {
        TypeVariable<Class<GenericClass>>[] typeParams = GenericClass.class.getTypeParameters();
        GenericMetadataSupport.TypeVarBoundedType boundedType1 = new GenericMetadataSupport.TypeVarBoundedType(typeParams[0]);
        GenericMetadataSupport.TypeVarBoundedType boundedType2 = new GenericMetadataSupport.TypeVarBoundedType(typeParams[0]);
        GenericMetadataSupport.TypeVarBoundedType boundedType3 = new GenericMetadataSupport.TypeVarBoundedType(typeParams[1]);

        assertEquals(boundedType1, boundedType1);
        assertEquals(boundedType1, boundedType2);
        assertFalse(boundedType1.equals(boundedType3));
        assertFalse(boundedType1.equals(null));
        assertFalse(boundedType1.equals("different_type"));

        assertEquals(boundedType1.hashCode(), boundedType2.hashCode());
        assertEquals(typeParams[0], boundedType1.typeVariable());
        assertEquals(Object.class, boundedType1.firstBound());
        assertEquals(0, boundedType1.interfaceBounds().length);
        assertNotNull(boundedType1.toString());
    }

    // Tests WildCardBoundedType equals, hashCode, toString, and interface bounds
    @Test
    public void testWildCardBoundedType_propertiesAndMethods() throws Exception {
        Method methodLower = GenericsNest.class.getMethod("returning_wildcard_with_class_lower_bound");
        ParameterizedType paramTypeLower = (ParameterizedType) methodLower.getGenericReturnType();
        WildcardType wildcardLower = (WildcardType) paramTypeLower.getActualTypeArguments()[0];

        GenericMetadataSupport.WildCardBoundedType wildcardBounded = new GenericMetadataSupport.WildCardBoundedType(wildcardLower);
        assertEquals(Integer.class, wildcardBounded.firstBound());
        assertEquals(0, wildcardBounded.interfaceBounds().length);
        assertEquals(wildcardLower, wildcardBounded.wildCard());
        assertEquals(wildcardLower.hashCode(), wildcardBounded.hashCode());
        assertEquals(wildcardBounded, wildcardBounded);
        assertFalse(wildcardBounded.equals(null));
        assertFalse(wildcardBounded.equals("other"));
        assertNotNull(wildcardBounded.toString());

        Method methodUpper = GenericsNest.class.getMethod("returning_wildcard_with_typeVar_upper_bound");
        ParameterizedType paramTypeUpper = (ParameterizedType) methodUpper.getGenericReturnType();
        WildcardType wildcardUpper = (WildcardType) paramTypeUpper.getActualTypeArguments()[0];
        GenericMetadataSupport.WildCardBoundedType wildcardBoundedUpper = new GenericMetadataSupport.WildCardBoundedType(wildcardUpper);
        assertTrue(wildcardBoundedUpper.firstBound() instanceof TypeVariable);
    }

    // Tests resolveGenericReturnType on method with chained type parameters (<S, T extends S>)
    @Test
    public void testResolveGenericReturnType_chainedTypeParameters() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(GenericsNest.class);
        Method method = GenericsNest.class.getMethod("two_type_params");
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(Object.class, returnMetadata.rawType());
    }

    // Tests resolveGenericReturnType on method with type param bounded by class type variable (<O extends K>)
    @Test
    public void testResolveGenericReturnType_typeParamBoundedByClassTypeVar() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(GenericsNest.class);
        Method method = GenericsNest.class.getMethod("typeVar_with_type_params");
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(Comparable.class, returnMetadata.rawType());
        assertTrue(returnMetadata.hasRawExtraInterfaces());
        assertArrayEquals(new Class<?>[]{Cloneable.class}, returnMetadata.rawExtraInterfaces());
    }

    // Tests inferFrom on a TypeVariable directly
    @Test
    public void testInferFrom_typeVariable() {
        TypeVariable<Class<UpperBoundedInterface>> typeVar = UpperBoundedInterface.class.getTypeParameters()[0];
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(typeVar);

        assertEquals(Comparable.class, metadata.rawType());
        assertTrue(metadata.hasRawExtraInterfaces());
        assertArrayEquals(new Class<?>[]{Cloneable.class}, metadata.rawExtraInterfaces());
    }

    // Tests deep interface generic inheritance hierarchy
    @Test
    public void testResolveGenericReturnType_deepGenericHierarchy() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(DeepInterfaceC.class);

        Method getB = DeepInterfaceB.class.getMethod("getB");
        GenericMetadataSupport returnB = metadata.resolveGenericReturnType(getB);
        assertEquals(Double.class, returnB.rawType());

        Method getA = DeepInterfaceA.class.getMethod("getA");
        GenericMetadataSupport returnA = metadata.resolveGenericReturnType(getA);
        assertEquals(List.class, returnA.rawType());
    }

    // Tests WildCardBoundedType equals with matching WildCardBoundedType
    @Test
    public void testWildCardBoundedType_equalsAnotherWildCardBoundedType() throws Exception {
        Method method1 = GenericsNest.class.getMethod("returning_wildcard_with_class_lower_bound");
        WildcardType wildcard1 = (WildcardType) ((ParameterizedType) method1.getGenericReturnType()).getActualTypeArguments()[0];

        Method method2 = GenericsNest.class.getMethod("returning_wildcard_with_class_lower_bound");
        WildcardType wildcard2 = (WildcardType) ((ParameterizedType) method2.getGenericReturnType()).getActualTypeArguments()[0];

        GenericMetadataSupport.WildCardBoundedType bounded1 = new GenericMetadataSupport.WildCardBoundedType(wildcard1);
        GenericMetadataSupport.WildCardBoundedType bounded2 = new GenericMetadataSupport.WildCardBoundedType(wildcard2);

        assertEquals(bounded1, bounded2);
        assertEquals(bounded1.hashCode(), bounded2.hashCode());
    }

    // Tests generic array return type resolution
    @Test
    public void testResolveGenericReturnType_genericArrayReturnType() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(GenericsNest.class);
        Method method = GenericsNest.class.getMethod("returningGenericArray");
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(Comparable[].class, returnMetadata.rawType());
    }
}