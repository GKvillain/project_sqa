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

    interface UpperBoundedInterface<E extends Number & Serializable> {
        E getBounded();
    }

    static class StringGenericsNest implements GenericsNest<StringComparableCloneable> {
        public Set<Number> remove(Object key) { return null; }
        public List<? super Integer> returning_wildcard_with_class_lower_bound() { return null; }
        public List<? super StringComparableCloneable> returning_wildcard_with_typeVar_lower_bound() { return null; }
        public List<? extends StringComparableCloneable> returning_wildcard_with_typeVar_upper_bound() { return null; }
        public StringComparableCloneable returningK() { return null; }
        public <O extends StringComparableCloneable> List<O> paramType_with_type_params() { return null; }
        public <S, T extends S> T two_type_params() { return null; }
        public <O extends StringComparableCloneable> O typeVar_with_type_params() { return null; }
        public Number returningNonGeneric() { return null; }
        public void clear() {}
        public boolean containsKey(Object key) { return false; }
        public boolean containsValue(Object value) { return false; }
        public Set<java.util.Map.Entry<StringComparableCloneable, Set<Number>>> entrySet() { return null; }
        public boolean equals(Object o) { return false; }
        public Set<Number> get(Object key) { return null; }
        public int hashCode() { return 0; }
        public boolean isEmpty() { return false; }
        public Set<StringComparableCloneable> keySet() { return null; }
        public Set<Number> put(StringComparableCloneable key, Set<Number> value) { return null; }
        public void putAll(Map<? extends StringComparableCloneable, ? extends Set<Number>> m) {}
        public int size() { return 0; }
        public java.util.Collection<Set<Number>> values() { return null; }
    }

    static abstract class StringComparableCloneable implements Comparable<StringComparableCloneable>, Cloneable {
    }

    // Tests inferFrom with Class
    @Test
    public void testInferFrom_class_returnsClassMetadata() {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(StringGenericsNest.class);
        assertEquals(StringGenericsNest.class, metadata.rawType());
    }

    // Tests inferFrom with ParameterizedType
    @Test
    public void testInferFrom_parameterizedType_returnsParameterizedMetadata() throws NoSuchMethodException {
        Method method = GenericsNest.class.getMethod("paramType_with_type_params");
        Type genericReturnType = method.getGenericReturnType();
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(genericReturnType);
        assertEquals(List.class, metadata.rawType());
    }

    // Tests inferFrom with null input throws MockitoException
    @Test(expected = MockitoException.class)
    public void testInferFrom_nullType_throwsMockitoException() {
        GenericMetadataSupport.inferFrom(null);
    }

    // Tests inferFrom with unsupported Type throws MockitoException
    @Test(expected = MockitoException.class)
    public void testInferFrom_unsupportedType_throwsMockitoException() {
        Type customType = new Type() {
            @Override
            public String toString() {
                return "CustomType";
            }
        };
        GenericMetadataSupport.inferFrom(customType);
    }

    // Tests resolving non-generic return type
    @Test
    public void testResolveGenericReturnType_nonGenericMethod_returnsClassType() throws NoSuchMethodException {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(GenericsNest.class);
        Method method = GenericsNest.class.getMethod("returningNonGeneric");
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(Number.class, returnMetadata.rawType());
        assertFalse(returnMetadata.hasRawExtraInterfaces());
        assertEquals(0, returnMetadata.rawExtraInterfaces().length);
        assertEquals(Collections.emptyList(), returnMetadata.extraInterfaces());
    }

    // Tests resolving ParameterizedType return type
    @Test
    public void testResolveGenericReturnType_parameterizedReturnType_returnsRawClass() throws NoSuchMethodException {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(GenericsNest.class);
        Method method = GenericsNest.class.getMethod("remove", Object.class);
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(Set.class, returnMetadata.rawType());
    }

    // Tests resolving TypeVariable return type
    @Test
    public void testResolveGenericReturnType_typeVariableReturnType_returnsBoundRawClass() throws NoSuchMethodException {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(GenericsNest.class);
        Method method = GenericsNest.class.getMethod("returningK");
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(Comparable.class, returnMetadata.rawType());
        assertTrue(returnMetadata.hasRawExtraInterfaces());
        assertArrayEquals(new Class<?>[]{Cloneable.class}, returnMetadata.rawExtraInterfaces());
    }

    // Tests resolving method with TypeVariable bounds on class hierarchy
    @Test
    public void testResolveGenericReturnType_implementedGenerics_resolvesActualType() throws NoSuchMethodException {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(StringGenericsNest.class);
        Method method = GenericsNest.class.getMethod("returningK");
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(StringComparableCloneable.class, returnMetadata.rawType());
    }

    // Tests actualTypeArguments on generic class
    @Test
    public void testActualTypeArguments_genericInterface_returnsResolvedTypeMap() {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(GenericsNest.class);
        Map<TypeVariable, Type> typeArguments = metadata.actualTypeArguments();

        assertNotNull(typeArguments);
        assertEquals(1, typeArguments.size());
    }

    // Tests TypeVarBoundedType methods
    @Test
    public void testTypeVarBoundedType_boundsAndEquality() throws NoSuchMethodException {
        TypeVariable<?>[] typeParameters = UpperBoundedInterface.class.getTypeParameters();
        TypeVariable<?> typeVar = typeParameters[0];

        GenericMetadataSupport.TypeVarBoundedType boundedType = new GenericMetadataSupport.TypeVarBoundedType(typeVar);

        assertEquals(Number.class, boundedType.firstBound());
        assertArrayEquals(new Type[]{Serializable.class}, boundedType.interfaceBounds());
        assertEquals(typeVar, boundedType.typeVariable());

        GenericMetadataSupport.TypeVarBoundedType sameBoundedType = new GenericMetadataSupport.TypeVarBoundedType(typeVar);
        assertEquals(boundedType, sameBoundedType);
        assertEquals(boundedType.hashCode(), sameBoundedType.hashCode());
        assertNotNull(boundedType.toString());

        assertFalse(boundedType.equals(null));
        assertFalse(boundedType.equals("NotATypeVarBoundedType"));
    }

    // Tests WildCardBoundedType methods with upper bound
    @Test
    public void testWildCardBoundedType_upperBound() throws NoSuchMethodException {
        Method method = GenericsNest.class.getMethod("returning_wildcard_with_class_lower_bound");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();
        WildcardType wildcardType = (WildcardType) returnType.getActualTypeArguments()[0];

        GenericMetadataSupport.WildCardBoundedType boundedType = new GenericMetadataSupport.WildCardBoundedType(wildcardType);

        assertEquals(Integer.class, boundedType.firstBound());
        assertEquals(0, boundedType.interfaceBounds().length);
        assertEquals(wildcardType, boundedType.wildCard());
        assertNotNull(boundedType.toString());
        assertEquals(wildcardType.hashCode(), boundedType.hashCode());
    }

    // Tests method with type parameters defined on the method itself
    @Test
    public void testResolveGenericReturnType_methodWithTypeParameters_resolvesProperly() throws NoSuchMethodException {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(GenericsNest.class);
        Method method = GenericsNest.class.getMethod("two_type_params");
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(Object.class, returnMetadata.rawType());
    }

    // Tests method returning TypeVariable with nested type params
    @Test
    public void testResolveGenericReturnType_typeVarWithTypeParams_resolvesToBound() throws NoSuchMethodException {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(GenericsNest.class);
        Method method = GenericsNest.class.getMethod("typeVar_with_type_params");
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(Comparable.class, returnMetadata.rawType());
    }
}