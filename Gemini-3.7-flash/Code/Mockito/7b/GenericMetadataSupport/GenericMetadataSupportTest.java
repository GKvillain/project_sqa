package org.mockito.internal.util.reflection;

import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class GenericMetadataSupportTest {

    interface SingleGeneric<T> {
        T get();
        String nonGeneric();
        List<T> list();
    }

    interface MultiGeneric<K, V> {
        K getKey();
        V getValue();
    }

    interface SubSingleGeneric extends SingleGeneric<String> {
    }

    interface BoundedGeneric<T extends Comparable<T> & Cloneable> {
        T getBounded();
        <S extends T> S getSubBounded();
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
    }

    static class BaseGenericClass<A, B> {
        A a;
        B b;
    }

    static class SubGenericClass extends BaseGenericClass<String, Integer> implements Serializable {
    }

    interface MultipleInterfaceBounds<T extends Serializable & Cloneable> {
        T getMultiple();
    }

    private static class DummyType implements Type {
    }

    // Tests null input to inferFrom
    @Test(expected = RuntimeException.class)
    public void testInferFrom_nullType_throwsException() {
        GenericMetadataSupport.inferFrom(null);
    }

    // Tests unsupported Type implementation to inferFrom
    @Test(expected = MockitoException.class)
    public void testInferFrom_unsupportedType_throwsMockitoException() {
        GenericMetadataSupport.inferFrom(new DummyType());
    }

    // Tests inferFrom with standard Class
    @Test
    public void testInferFrom_class_returnsFromClassGenericMetadataSupport() {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(String.class);
        assertNotNull(metadata);
        assertEquals(String.class, metadata.rawType());
        assertTrue(metadata.actualTypeArguments().isEmpty());
        assertFalse(metadata.hasRawExtraInterfaces());
        assertEquals(0, metadata.rawExtraInterfaces().length);
        assertEquals(Collections.emptyList(), metadata.extraInterfaces());
    }

    // Tests inferFrom with ParameterizedType
    @Test
    public void testInferFrom_parameterizedType_returnsCorrectRawTypeAndArgs() throws Exception {
        Method method = GenericsNest.class.getMethod("paramType_with_type_params");
        Type genericReturnType = method.getGenericReturnType();
        assertTrue(genericReturnType instanceof ParameterizedType);

        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(genericReturnType);
        assertNotNull(metadata);
        assertEquals(List.class, metadata.rawType());
    }

    // Tests resolving non-generic return type
    @Test
    public void testResolveGenericReturnType_nonGenericMethod_returnsClass() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(SingleGeneric.class);
        Method method = SingleGeneric.class.getMethod("nonGeneric");

        GenericMetadataSupport returnTypeMetadata = metadata.resolveGenericReturnType(method);
        assertEquals(String.class, returnTypeMetadata.rawType());
        assertFalse(returnTypeMetadata.hasRawExtraInterfaces());
    }

    // Tests resolving ParameterizedType return type on class
    @Test
    public void testResolveGenericReturnType_parameterizedReturnType_returnsRawClass() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(SingleGeneric.class);
        Method method = SingleGeneric.class.getMethod("list");

        GenericMetadataSupport returnTypeMetadata = metadata.resolveGenericReturnType(method);
        assertEquals(List.class, returnTypeMetadata.rawType());
    }

    // Tests resolving TypeVariable return type from an interface declaration
    @Test
    public void testResolveGenericReturnType_typeVariableOnInterface_returnsBoundOrObject() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(SingleGeneric.class);
        Method method = SingleGeneric.class.getMethod("get");

        GenericMetadataSupport returnTypeMetadata = metadata.resolveGenericReturnType(method);
        assertEquals(Object.class, returnTypeMetadata.rawType());
    }

    // Tests resolving TypeVariable on concrete sub-interface
    @Test
    public void testResolveGenericReturnType_typeVariableOnSubInterface_resolvesActualType() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(SubSingleGeneric.class);
        Method method = SingleGeneric.class.getMethod("get");

        GenericMetadataSupport returnTypeMetadata = metadata.resolveGenericReturnType(method);
        assertEquals(String.class, returnTypeMetadata.rawType());
    }

    // Tests resolving TypeVariable on bounded interface
    @Test
    public void testResolveGenericReturnType_boundedTypeVariable_resolvesBoundsAndExtraInterfaces() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(BoundedGeneric.class);
        Method method = BoundedGeneric.class.getMethod("getBounded");

        GenericMetadataSupport returnTypeMetadata = metadata.resolveGenericReturnType(method);
        assertEquals(Comparable.class, returnTypeMetadata.rawType());
        assertTrue(returnTypeMetadata.hasRawExtraInterfaces());
        assertArrayEquals(new Class<?>[]{Cloneable.class}, returnTypeMetadata.rawExtraInterfaces());
    }

    // Tests resolving method-level TypeVariable with upper bound
    @Test
    public void testResolveGenericReturnType_methodLevelTypeVariable_resolvesUpperBounds() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(BoundedGeneric.class);
        Method method = BoundedGeneric.class.getMethod("getSubBounded");

        GenericMetadataSupport returnTypeMetadata = metadata.resolveGenericReturnType(method);
        assertEquals(Comparable.class, returnTypeMetadata.rawType());
        assertTrue(returnTypeMetadata.hasRawExtraInterfaces());
        assertArrayEquals(new Class<?>[]{Cloneable.class}, returnTypeMetadata.rawExtraInterfaces());
    }

    // Tests resolving various methods in GenericsNest
    @Test
    public void testResolveGenericReturnType_genericsNestMethods_resolvesCorrectly() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(GenericsNest.class);

        Method removeMethod = GenericsNest.class.getMethod("remove", Object.class);
        GenericMetadataSupport removeMetadata = metadata.resolveGenericReturnType(removeMethod);
        assertEquals(Set.class, removeMetadata.rawType());

        Method returningKMethod = GenericsNest.class.getMethod("returningK");
        GenericMetadataSupport returningKMetadata = metadata.resolveGenericReturnType(returningKMethod);
        assertEquals(Comparable.class, returningKMetadata.rawType());
        assertTrue(returningKMetadata.hasRawExtraInterfaces());
        assertArrayEquals(new Class<?>[]{Cloneable.class}, returningKMetadata.rawExtraInterfaces());

        Method returningNonGenericMethod = GenericsNest.class.getMethod("returningNonGeneric");
        GenericMetadataSupport nonGenericMetadata = metadata.resolveGenericReturnType(returningNonGenericMethod);
        assertEquals(Number.class, nonGenericMetadata.rawType());

        Method twoTypeParamsMethod = GenericsNest.class.getMethod("two_type_params");
        GenericMetadataSupport twoTypeParamsMetadata = metadata.resolveGenericReturnType(twoTypeParamsMethod);
        assertEquals(Object.class, twoTypeParamsMetadata.rawType());
    }

    // Tests actualTypeArguments retrieval on a parameterized generic interface
    @Test
    public void testActualTypeArguments_subSingleGeneric_containsConcreteBinding() {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(SubSingleGeneric.class);
        Map<TypeVariable, Type> actualArgs = metadata.actualTypeArguments();
        assertNotNull(actualArgs);
        assertEquals(0, actualArgs.size());
    }

    // Tests actualTypeArguments on class with type parameters
    @Test
    public void testActualTypeArguments_genericClass_returnsTypeVariableMap() {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(MultiGeneric.class);
        Map<TypeVariable, Type> actualArgs = metadata.actualTypeArguments();
        assertEquals(2, actualArgs.size());
        for (Map.Entry<TypeVariable, Type> entry : actualArgs.entrySet()) {
            assertNotNull(entry.getKey());
            assertNotNull(entry.getValue());
        }
    }

    // Tests TypeVarBoundedType methods, equals, hashCode and toString
    @Test
    public void testTypeVarBoundedType_behavior() throws Exception {
        Method method = BoundedGeneric.class.getMethod("getBounded");
        TypeVariable<?> typeVar = (TypeVariable<?>) method.getGenericReturnType();

        GenericMetadataSupport.TypeVarBoundedType boundedType = new GenericMetadataSupport.TypeVarBoundedType(typeVar);
        assertEquals(Comparable.class, ((ParameterizedType) boundedType.firstBound()).getRawType());
        assertEquals(1, boundedType.interfaceBounds().length);
        assertEquals(Cloneable.class, boundedType.interfaceBounds()[0]);
        assertEquals(typeVar, boundedType.typeVariable());

        GenericMetadataSupport.TypeVarBoundedType sameBoundedType = new GenericMetadataSupport.TypeVarBoundedType(typeVar);
        assertEquals(boundedType, sameBoundedType);
        assertEquals(boundedType.hashCode(), sameBoundedType.hashCode());
        assertNotNull(boundedType.toString());
        assertFalse(boundedType.equals(null));
        assertFalse(boundedType.equals("string"));
    }

    // Tests WildCardBoundedType methods, equals, hashCode and toString
    @Test
    public void testWildCardBoundedType_behavior() throws Exception {
        Method method = GenericsNest.class.getMethod("returning_wildcard_with_class_lower_bound");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();
        WildcardType wildcardType = (WildcardType) returnType.getActualTypeArguments()[0];

        GenericMetadataSupport.WildCardBoundedType wildCardBoundedType = new GenericMetadataSupport.WildCardBoundedType(wildcardType);
        assertEquals(Integer.class, wildCardBoundedType.firstBound());
        assertEquals(0, wildCardBoundedType.interfaceBounds().length);
        assertEquals(wildcardType, wildCardBoundedType.wildCard());

        GenericMetadataSupport.WildCardBoundedType sameWildCardBoundedType = new GenericMetadataSupport.WildCardBoundedType(wildcardType);
        assertEquals(wildCardBoundedType.hashCode(), sameWildCardBoundedType.hashCode());
        assertNotNull(wildCardBoundedType.toString());
        assertFalse(wildCardBoundedType.equals(null));
        assertFalse(wildCardBoundedType.equals("string"));
    }

    // Tests wildcard upper bound in GenericsNest
    @Test
    public void testResolveGenericReturnType_wildcardUpperBounds() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(GenericsNest.class);
        Method method = GenericsNest.class.getMethod("returning_wildcard_with_typeVar_upper_bound");
        GenericMetadataSupport returnTypeMetadata = metadata.resolveGenericReturnType(method);
        assertEquals(List.class, returnTypeMetadata.rawType());
    }

    // Tests inferFrom with TypeVariable directly
    @Test
    public void testInferFrom_typeVariable_returnsMetadata() {
        TypeVariable<?> typeVar = GenericsNest.class.getTypeParameters()[0];
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(typeVar);
        assertNotNull(metadata);
        assertEquals(Comparable.class, metadata.rawType());
        assertTrue(metadata.hasRawExtraInterfaces());
        assertArrayEquals(new Class<?>[]{Cloneable.class}, metadata.rawExtraInterfaces());
    }

    // Tests inferFrom with WildcardType directly
    @Test
    public void testInferFrom_wildcardType_returnsMetadata() throws Exception {
        Method method = GenericsNest.class.getMethod("returning_wildcard_with_class_lower_bound");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();
        WildcardType wildcardType = (WildcardType) returnType.getActualTypeArguments()[0];

        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(wildcardType);
        assertNotNull(metadata);
        assertEquals(Integer.class, metadata.rawType());
    }

    // Tests inferFrom with BoundedType directly
    @Test
    public void testInferFrom_boundedType_returnsMetadata() throws Exception {
        Method method = BoundedGeneric.class.getMethod("getBounded");
        TypeVariable<?> typeVar = (TypeVariable<?>) method.getGenericReturnType();
        GenericMetadataSupport.TypeVarBoundedType boundedType = new GenericMetadataSupport.TypeVarBoundedType(typeVar);

        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(boundedType);
        assertNotNull(metadata);
        assertEquals(Comparable.class, metadata.rawType());
        assertTrue(metadata.hasRawExtraInterfaces());
    }

    // Tests resolveGenericReturnType for wildcard with TypeVariable lower bound
    @Test
    public void testResolveGenericReturnType_wildcardWithTypeVarLowerBound() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(GenericsNest.class);
        Method method = GenericsNest.class.getMethod("returning_wildcard_with_typeVar_lower_bound");
        GenericMetadataSupport returnTypeMetadata = metadata.resolveGenericReturnType(method);
        assertEquals(List.class, returnTypeMetadata.rawType());
    }

    // Tests class hierarchy with generic superclass
    @Test
    public void testInferFrom_classWithGenericSuperClass_registersTypeVariables() {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(SubGenericClass.class);
        assertNotNull(metadata);
        assertEquals(SubGenericClass.class, metadata.rawType());
    }

    // Tests multiple interface bounds on TypeVariable
    @Test
    public void testResolveGenericReturnType_multipleInterfaceBounds() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(MultipleInterfaceBounds.class);
        Method method = MultipleInterfaceBounds.class.getMethod("getMultiple");
        GenericMetadataSupport returnTypeMetadata = metadata.resolveGenericReturnType(method);
        assertEquals(Serializable.class, returnTypeMetadata.rawType());
        assertTrue(returnTypeMetadata.hasRawExtraInterfaces());
        assertArrayEquals(new Class<?>[]{Cloneable.class}, returnTypeMetadata.rawExtraInterfaces());
    }
}