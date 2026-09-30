package org.mockito.internal.util.reflection;

import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;

import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class GenericMetadataSupportTest {

    interface SampleInterface {
        String returnsString();
    }

    interface SingleGeneric<T> {
        T getValue();
    }

    interface StringGeneric extends SingleGeneric<String> {
    }

    interface ListGeneric<T> {
        List<T> getList();
    }

    interface IntegerListGeneric extends ListGeneric<Integer> {
    }

    interface SelfReferenceGeneric<T extends SelfReferenceGeneric<T>> {
        T self();
    }

    interface SubSelfReferenceGeneric extends SelfReferenceGeneric<SubSelfReferenceGeneric> {
    }

    interface BoundedGenerics<T extends Number & Comparable<T>> {
        T getBounded();
    }

    interface MultiBoundedGenerics<T extends Number & Comparable<T> & Serializable> {
        T getMultiBounded();
    }

    interface MethodTypeVariableGeneric {
        <S extends CharSequence> S process(S input);
        <U> U unboundedMethod(U input);
    }

    interface WildcardGeneric {
        List<? extends Number> getUpperWildcard();
        List<? super Integer> getLowerWildcard();
    }

    interface TwoTypeParamsGeneric<K, V> {
        Map<K, V> getMap();
    }

    interface StringIntegerMapGeneric extends TwoTypeParamsGeneric<String, Integer> {
    }

    abstract static class BaseGenericClass<T> {
        public abstract T getBaseValue();
    }

    abstract static class MiddleGenericClass<A, B> extends BaseGenericClass<B> implements TwoTypeParamsGeneric<A, B> {
    }

    static class ConcreteGenericClass extends MiddleGenericClass<String, Long> {
        @Override
        public Long getBaseValue() {
            return 1L;
        }

        @Override
        public Map<String, Long> getMap() {
            return null;
        }
    }

    interface DeepNestedGeneric<T> {
        List<Set<T>> getNested();
    }

    interface StringDeepNestedGeneric extends DeepNestedGeneric<String> {
    }

    // Tests null input throws MockitoException
    @Test(expected = MockitoException.class)
    public void testInferFrom_nullType_throwsMockitoException() {
        GenericMetadataSupport.inferFrom(null);
    }

    // Tests unsupported Type implementation throws MockitoException
    @Test(expected = MockitoException.class)
    public void testInferFrom_unsupportedType_throwsMockitoException() {
        Type unsupportedType = new Type() {
            @Override
            public String toString() {
                return "CustomUnsupportedType";
            }
        };
        GenericMetadataSupport.inferFrom(unsupportedType);
    }

    // Tests inferFrom with standard non-generic Class
    @Test
    public void testInferFrom_nonGenericClass_returnsCorrectMetadata() {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(String.class);

        assertEquals(String.class, metadata.rawType());
        assertTrue(metadata.actualTypeArguments().isEmpty());
        assertFalse(metadata.hasRawExtraInterfaces());
        assertEquals(0, metadata.rawExtraInterfaces().length);
        assertTrue(metadata.extraInterfaces().isEmpty());
    }

    // Tests inferFrom with ParameterizedType
    @Test
    public void testInferFrom_parameterizedType_returnsCorrectMetadata() throws NoSuchMethodException {
        Method method = StringIntegerMapGeneric.class.getMethod("getMap");
        Type genericReturnType = method.getGenericReturnType();

        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(genericReturnType);

        assertEquals(Map.class, metadata.rawType());
    }

    // Tests resolving non-generic method return type
    @Test
    public void testResolveGenericReturnType_nonGenericMethod_returnsClassMetadata() throws NoSuchMethodException {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(SampleInterface.class);
        Method method = SampleInterface.class.getMethod("returnsString");

        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(String.class, returnMetadata.rawType());
        assertTrue(returnMetadata.actualTypeArguments().isEmpty());
    }

    // Tests resolving TypeVariable return type when resolved via subclass interface
    @Test
    public void testResolveGenericReturnType_typeVariableResolvedInSubclass_returnsTargetClass() throws NoSuchMethodException {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(StringGeneric.class);
        Method method = SingleGeneric.class.getMethod("getValue");

        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(String.class, returnMetadata.rawType());
    }

    // Tests resolving ParameterizedType return type when type parameters are resolved
    @Test
    public void testResolveGenericReturnType_parameterizedReturnType_resolvesTypeVariables() throws NoSuchMethodException {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(IntegerListGeneric.class);
        Method method = ListGeneric.class.getMethod("getList");

        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(List.class, returnMetadata.rawType());
    }

    // Tests self-referencing generic type (Detects Defects4J Mockito-8 StackOverflow defect)
    @Test
    public void testInferFrom_selfReferencingGeneric_doesNotCauseStackOverflow() throws NoSuchMethodException {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(SelfReferenceGeneric.class);

        Map<TypeVariable, Type> actualArgs = metadata.actualTypeArguments();
        assertNotNull(actualArgs);
        assertEquals(1, actualArgs.size());

        Method method = SelfReferenceGeneric.class.getMethod("self");
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(SelfReferenceGeneric.class, returnMetadata.rawType());
    }

    // Tests resolving return type on sub-interface of self-referencing generic
    @Test
    public void testResolveGenericReturnType_subclassOfSelfReferencingGeneric_resolvesCorrectly() throws NoSuchMethodException {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(SubSelfReferenceGeneric.class);
        Method method = SelfReferenceGeneric.class.getMethod("self");

        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(SubSelfReferenceGeneric.class, returnMetadata.rawType());
    }

    // Tests bounded TypeVariable with class and interface bounds
    @Test
    public void testResolveGenericReturnType_boundedTypeVariable_extractsRawTypeAndExtraInterfaces() throws NoSuchMethodException {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(BoundedGenerics.class);
        Method method = BoundedGenerics.class.getMethod("getBounded");

        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(Number.class, returnMetadata.rawType());
        assertTrue(returnMetadata.hasRawExtraInterfaces());
        Class<?>[] extraInterfaces = returnMetadata.rawExtraInterfaces();
        assertEquals(1, extraInterfaces.length);
        assertEquals(Comparable.class, extraInterfaces[0]);
    }

    // Tests method-level generic type parameter resolution
    @Test
    public void testResolveGenericReturnType_methodWithTypeVariable_resolvesBound() throws NoSuchMethodException {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(MethodTypeVariableGeneric.class);
        Method method = MethodTypeVariableGeneric.class.getMethod("process", CharSequence.class);

        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(CharSequence.class, returnMetadata.rawType());
    }

    // Tests resolution with wildcard upper and lower bounds
    @Test
    public void testResolveGenericReturnType_wildcardBounds_resolvesRawType() throws NoSuchMethodException {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(WildcardGeneric.class);

        Method upperMethod = WildcardGeneric.class.getMethod("getUpperWildcard");
        GenericMetadataSupport upperMetadata = metadata.resolveGenericReturnType(upperMethod);
        assertEquals(List.class, upperMetadata.rawType());

        Method lowerMethod = WildcardGeneric.class.getMethod("getLowerWildcard");
        GenericMetadataSupport lowerMetadata = metadata.resolveGenericReturnType(lowerMethod);
        assertEquals(List.class, lowerMetadata.rawType());
    }

    // Tests TypeVarBoundedType behavior including equals, hashCode, and toString
    @Test
    public void testTypeVarBoundedType_boundsAndEquality() {
        TypeVariable<?>[] typeParams = SingleGeneric.class.getTypeParameters();
        TypeVariable<?> typeVar1 = typeParams[0];

        GenericMetadataSupport.TypeVarBoundedType boundedType1 = new GenericMetadataSupport.TypeVarBoundedType(typeVar1);
        GenericMetadataSupport.TypeVarBoundedType boundedType2 = new GenericMetadataSupport.TypeVarBoundedType(typeVar1);

        assertEquals(Object.class, boundedType1.firstBound());
        assertEquals(0, boundedType1.interfaceBounds().length);
        assertEquals(typeVar1, boundedType1.typeVariable());
        assertEquals(boundedType1, boundedType2);
        assertEquals(boundedType1.hashCode(), boundedType2.hashCode());
        assertNotNull(boundedType1.toString());
        assertFalse(boundedType1.equals(null));
        assertFalse(boundedType1.equals("different-type"));
    }

    // Tests WildCardBoundedType behavior including bounds and toString
    @Test
    public void testWildCardBoundedType_boundsAndProperties() throws NoSuchMethodException {
        Method method = WildcardGeneric.class.getMethod("getUpperWildcard");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();
        WildcardType wildcardType = (WildcardType) returnType.getActualTypeArguments()[0];

        GenericMetadataSupport.WildCardBoundedType boundedType = new GenericMetadataSupport.WildCardBoundedType(wildcardType);

        assertEquals(Number.class, boundedType.firstBound());
        assertEquals(0, boundedType.interfaceBounds().length);
        assertEquals(wildcardType, boundedType.wildCard());
        assertNotNull(boundedType.toString());
        assertEquals(wildcardType.hashCode(), boundedType.hashCode());
    }

    // New tests to cover additional branches and edge cases

    @Test
    public void testInferFrom_typeVariableDirectly_returnsMetadata() {
        TypeVariable<?> typeVar = SingleGeneric.class.getTypeParameters()[0];
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(typeVar);

        assertEquals(Object.class, metadata.rawType());
        assertFalse(metadata.hasRawExtraInterfaces());
        assertEquals(0, metadata.rawExtraInterfaces().length);
    }

    @Test
    public void testInferFrom_typeVarBoundedTypeDirectly_returnsMetadata() {
        TypeVariable<?> typeVar = BoundedGenerics.class.getTypeParameters()[0];
        GenericMetadataSupport.TypeVarBoundedType boundedType = new GenericMetadataSupport.TypeVarBoundedType(typeVar);

        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(boundedType);

        assertEquals(Number.class, metadata.rawType());
        assertTrue(metadata.hasRawExtraInterfaces());
        assertEquals(1, metadata.rawExtraInterfaces().length);
        assertEquals(Comparable.class, metadata.rawExtraInterfaces()[0]);
    }

    @Test
    public void testInferFrom_wildCardBoundedTypeDirectly_returnsMetadata() throws NoSuchMethodException {
        Method method = WildcardGeneric.class.getMethod("getUpperWildcard");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();
        WildcardType wildcardType = (WildcardType) returnType.getActualTypeArguments()[0];

        GenericMetadataSupport.WildCardBoundedType boundedType = new GenericMetadataSupport.WildCardBoundedType(wildcardType);
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(boundedType);

        assertEquals(Number.class, metadata.rawType());
    }

    @Test
    public void testWildCardBoundedType_equalityAndHashCode() throws NoSuchMethodException {
        Method upperMethod = WildcardGeneric.class.getMethod("getUpperWildcard");
        ParameterizedType upperType = (ParameterizedType) upperMethod.getGenericReturnType();
        WildcardType wildcardUpper = (WildcardType) upperType.getActualTypeArguments()[0];

        Method lowerMethod = WildcardGeneric.class.getMethod("getLowerWildcard");
        ParameterizedType lowerType = (ParameterizedType) lowerMethod.getGenericReturnType();
        WildcardType wildcardLower = (WildcardType) lowerType.getActualTypeArguments()[0];

        GenericMetadataSupport.WildCardBoundedType boundedUpper1 = new GenericMetadataSupport.WildCardBoundedType(wildcardUpper);
        GenericMetadataSupport.WildCardBoundedType boundedUpper2 = new GenericMetadataSupport.WildCardBoundedType(wildcardUpper);
        GenericMetadataSupport.WildCardBoundedType boundedLower = new GenericMetadataSupport.WildCardBoundedType(wildcardLower);

        assertEquals(boundedUpper1, boundedUpper2);
        assertEquals(boundedUpper1.hashCode(), boundedUpper2.hashCode());
        assertFalse(boundedUpper1.equals(boundedLower));
        assertFalse(boundedUpper1.equals(null));
        assertFalse(boundedUpper1.equals("otherObject"));
        assertEquals(Object.class, boundedLower.firstBound());
    }

    @Test
    public void testTypeVarBoundedType_multipleInterfaceBounds() {
        TypeVariable<?> typeVar = MultiBoundedGenerics.class.getTypeParameters()[0];
        GenericMetadataSupport.TypeVarBoundedType boundedType = new GenericMetadataSupport.TypeVarBoundedType(typeVar);

        assertEquals(Number.class, boundedType.firstBound());
        Type[] interfaceBounds = boundedType.interfaceBounds();
        assertEquals(2, interfaceBounds.length);

        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(MultiBoundedGenerics.class);
        try {
            Method method = MultiBoundedGenerics.class.getMethod("getMultiBounded");
            GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);
            assertEquals(Number.class, returnMetadata.rawType());
            assertTrue(returnMetadata.hasRawExtraInterfaces());
            assertEquals(2, returnMetadata.rawExtraInterfaces().length);
            assertEquals(2, returnMetadata.extraInterfaces().size());
        } catch (NoSuchMethodException e) {
            fail("Method not found: " + e.getMessage());
        }
    }

    @Test
    public void testResolveGenericReturnType_classHierarchyInheritance() throws NoSuchMethodException {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(ConcreteGenericClass.class);

        Method baseMethod = BaseGenericClass.class.getMethod("getBaseValue");
        GenericMetadataSupport baseReturnMetadata = metadata.resolveGenericReturnType(baseMethod);
        assertEquals(Long.class, baseReturnMetadata.rawType());

        Method mapMethod = TwoTypeParamsGeneric.class.getMethod("getMap");
        GenericMetadataSupport mapReturnMetadata = metadata.resolveGenericReturnType(mapMethod);
        assertEquals(Map.class, mapReturnMetadata.rawType());
    }

    @Test
    public void testResolveGenericReturnType_unboundedMethodTypeVariable() throws NoSuchMethodException {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(MethodTypeVariableGeneric.class);
        Method method = MethodTypeVariableGeneric.class.getMethod("unboundedMethod", Object.class);

        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);
        assertEquals(Object.class, returnMetadata.rawType());
    }

    @Test
    public void testResolveGenericReturnType_deeplyNestedGenerics() throws NoSuchMethodException {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(StringDeepNestedGeneric.class);
        Method method = DeepNestedGeneric.class.getMethod("getNested");

        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);
        assertEquals(List.class, returnMetadata.rawType());
    }
}