package org.mockito.internal.util.reflection;

import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.*;
import java.util.*;
import java.io.Serializable;
import org.mockito.exceptions.base.MockitoException;

public class GenericMetadataSupportTest {

    // --- Helper types for constructing generic metadata ---

    static interface SimpleGeneric<T> {
        T get();
    }

    static interface WithBound<T extends Number> {
        T get();
    }

    static interface WithMultipleBounds<T extends Number & Serializable> {
        T get();
    }

    static interface WithGenericMethod {
        <T extends Number> T get();
        <T> T[] getArray();
        List<? super Number> getListLower();
        List<? extends Number> getListUpper();
    }

    static interface MyMap extends Map<String, Integer> {}

    static class MyStringList extends ArrayList<String> {}

    static interface WithInterfaceBound<T extends Serializable & Comparable<T>> {
        T get();
    }

    // --- Test cases ---

    // Tests inferFrom with a plain Class
    @Test
    public void testInferFrom_class_returnsFromClassGenericMetadataSupport() {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(String.class);
        assertNotNull(metadata);
        assertEquals(String.class, metadata.rawType());
    }

    // Tests inferFrom with a ParameterizedType
    @Test
    public void testInferFrom_parameterizedType_returnsFromParameterizedTypeGenericMetadataSupport() {
        ParameterizedType paramType = (ParameterizedType) MyStringList.class.getGenericSuperclass();
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(paramType);
        assertNotNull(metadata);
        assertEquals(ArrayList.class, metadata.rawType());
    }

    // Tests inferFrom with null input (Checks.checkNotNull throws RuntimeException)
    @Test(expected = RuntimeException.class)
    public void testInferFrom_nullInput_throwsException() {
        GenericMetadataSupport.inferFrom(null);
    }

    // Tests inferFrom with unsupported Type (should throw MockitoException)
    @Test(expected = MockitoException.class)
    public void testInferFrom_unsupportedType_throwsMockitoException() {
        Type unsupported = new Type() {};
        GenericMetadataSupport.inferFrom(unsupported);
    }

    // Tests resolveGenericReturnType when return type is a Class (non-generic)
    @Test
    public void testResolveGenericReturnType_class_returnsNotGenericReturnTypeSupport() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(Object.class);
        Method toString = Object.class.getMethod("toString");
        GenericMetadataSupport returnTypeSupport = metadata.resolveGenericReturnType(toString);
        assertNotNull(returnTypeSupport);
        assertEquals(String.class, returnTypeSupport.rawType());
    }

    // Tests resolveGenericReturnType when return type is a ParameterizedType
    @Test
    public void testResolveGenericReturnType_parameterizedType_returnsParameterizedReturnType() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(MyMap.class);
        Method entrySet = Map.class.getMethod("entrySet");
        GenericMetadataSupport returnTypeSupport = metadata.resolveGenericReturnType(entrySet);
        assertNotNull(returnTypeSupport);
        assertEquals(Set.class, returnTypeSupport.rawType());
    }

    // Tests resolveGenericReturnType when return type is a TypeVariable
    @Test
    public void testResolveGenericReturnType_typeVariable_returnsTypeVariableReturnType() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(WithGenericMethod.class);
        Method getMethod = WithGenericMethod.class.getMethod("get");
        GenericMetadataSupport returnTypeSupport = metadata.resolveGenericReturnType(getMethod);
        assertNotNull(returnTypeSupport);
        assertEquals(Number.class, returnTypeSupport.rawType());
    }

    // Tests resolveGenericReturnType with unsupported type (GenericArrayType) -> throws MockitoException
    @Test(expected = MockitoException.class)
    public void testResolveGenericReturnType_unsupportedType_throwsMockitoException() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(WithGenericMethod.class);
        Method getArrayMethod = WithGenericMethod.class.getMethod("getArray");
        metadata.resolveGenericReturnType(getArrayMethod);
    }

    // Tests actualTypeArguments for a non-generic class (no type parameters)
    @Test
    public void testActualTypeArguments_simpleClass_returnsEmpty() {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(String.class);
        Map<TypeVariable, Type> actualArgs = metadata.actualTypeArguments();
        assertTrue(actualArgs.isEmpty());
    }

    // Tests actualTypeArguments for a generic interface whose type variables are resolved
    @Test
    public void testActualTypeArguments_genericInterface_returnsResolvedTypes() {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(MyMap.class);
        Map<TypeVariable, Type> actualArgs = metadata.actualTypeArguments();
        assertEquals(2, actualArgs.size());
        for (Map.Entry<TypeVariable, Type> entry : actualArgs.entrySet()) {
            String varName = entry.getKey().getName();
            if ("K".equals(varName)) {
                assertEquals(String.class, entry.getValue());
            } else if ("V".equals(varName)) {
                assertEquals(Integer.class, entry.getValue());
            } else {
                fail("Unexpected type variable: " + varName);
            }
        }
    }

    // Tests TypeVarBoundedType.firstBound returns the first bound of a type variable
    @Test
    public void testTypeVarBoundedType_firstBound_returnsFirstBound() throws Exception {
        Method getMethod = WithMultipleBounds.class.getMethod("get");
        TypeVariable<?> tv = (TypeVariable<?>) getMethod.getGenericReturnType();
        GenericMetadataSupport.TypeVarBoundedType boundedType = new GenericMetadataSupport.TypeVarBoundedType(tv);
        assertEquals(Number.class, boundedType.firstBound());
    }

    // Tests TypeVarBoundedType.interfaceBounds returns the additional interface bounds
    @Test
    public void testTypeVarBoundedType_interfaceBounds_returnsAdditionalInterfaces() throws Exception {
        Method getMethod = WithMultipleBounds.class.getMethod("get");
        TypeVariable<?> tv = (TypeVariable<?>) getMethod.getGenericReturnType();
        GenericMetadataSupport.TypeVarBoundedType boundedType = new GenericMetadataSupport.TypeVarBoundedType(tv);
        Type[] interfaces = boundedType.interfaceBounds();
        assertEquals(1, interfaces.length);
        assertEquals(Serializable.class, interfaces[0]);
    }

    // Tests WildCardBoundedType.firstBound with a lower-bounded wildcard
    @Test
    public void testWildCardBoundedType_firstBound_withLowerBound_returnsLowerBound() throws Exception {
        Method getMethod = WithGenericMethod.class.getMethod("getListLower");
        Type returnType = getMethod.getGenericReturnType();
        ParameterizedType pType = (ParameterizedType) returnType;
        WildcardType wildcard = (WildcardType) pType.getActualTypeArguments()[0];
        GenericMetadataSupport.WildCardBoundedType boundedType = new GenericMetadataSupport.WildCardBoundedType(wildcard);
        assertEquals(Number.class, boundedType.firstBound());
    }

    // Tests WildCardBoundedType.firstBound with an upper-bounded wildcard
    @Test
    public void testWildCardBoundedType_firstBound_withUpperBound_returnsUpperBound() throws Exception {
        Method getMethod = WithGenericMethod.class.getMethod("getListUpper");
        Type returnType = getMethod.getGenericReturnType();
        ParameterizedType pType = (ParameterizedType) returnType;
        WildcardType wildcard = (WildcardType) pType.getActualTypeArguments()[0];
        GenericMetadataSupport.WildCardBoundedType boundedType = new GenericMetadataSupport.WildCardBoundedType(wildcard);
        assertEquals(Number.class, boundedType.firstBound());
    }

    // Tests WildCardBoundedType.interfaceBounds is always empty
    @Test
    public void testWildCardBoundedType_interfaceBounds_empty() throws Exception {
        Method getMethod = WithGenericMethod.class.getMethod("getListLower");
        Type returnType = getMethod.getGenericReturnType();
        ParameterizedType pType = (ParameterizedType) returnType;
        WildcardType wildcard = (WildcardType) pType.getActualTypeArguments()[0];
        GenericMetadataSupport.WildCardBoundedType boundedType = new GenericMetadataSupport.WildCardBoundedType(wildcard);
        assertEquals(0, boundedType.interfaceBounds().length);
    }

    // Tests extraInterfaces on a TypeVariableReturnType that has extra interfaces
    @Test
    public void testExtraInterfaces_typeVariableReturnType_returnsExtraInterfaces() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(WithInterfaceBound.class);
        Method getMethod = WithInterfaceBound.class.getMethod("get");
        GenericMetadataSupport returnTypeSupport = metadata.resolveGenericReturnType(getMethod);
        List<Type> extra = returnTypeSupport.extraInterfaces();
        assertEquals(1, extra.size());
        Type extraType = extra.get(0);
        assertTrue(extraType instanceof ParameterizedType);
        assertEquals(Comparable.class, ((ParameterizedType) extraType).getRawType());
    }

    // Tests rawExtraInterfaces and hasRawExtraInterfaces when extra interfaces exist
    @Test
    public void testRawExtraInterfaces_typeVariableReturnType_returnsRawClasses() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(WithInterfaceBound.class);
        Method getMethod = WithInterfaceBound.class.getMethod("get");
        GenericMetadataSupport returnTypeSupport = metadata.resolveGenericReturnType(getMethod);
        Class<?>[] rawExtras = returnTypeSupport.rawExtraInterfaces();
        assertEquals(1, rawExtras.length);
        assertEquals(Comparable.class, rawExtras[0]);
        assertTrue(returnTypeSupport.hasRawExtraInterfaces());
    }

    // Tests rawExtraInterfaces and hasRawExtraInterfaces when there are no extra interfaces
    @Test
    public void testRawExtraInterfaces_noExtra_empty() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(WithBound.class);
        Method getMethod = WithBound.class.getMethod("get");
        GenericMetadataSupport returnTypeSupport = metadata.resolveGenericReturnType(getMethod);
        Class<?>[] rawExtras = returnTypeSupport.rawExtraInterfaces();
        assertEquals(0, rawExtras.length);
        assertFalse(returnTypeSupport.hasRawExtraInterfaces());
    }

    // Tests equals and hashCode for TypeVarBoundedType
    @Test
    public void testTypeVarBoundedType_equalsAndHashCode() throws Exception {
        Method getMethod = WithMultipleBounds.class.getMethod("get");
        TypeVariable<?> tv1 = (TypeVariable<?>) getMethod.getGenericReturnType();
        TypeVariable<?> tv2 = (TypeVariable<?>) getMethod.getGenericReturnType();
        GenericMetadataSupport.TypeVarBoundedType b1 = new GenericMetadataSupport.TypeVarBoundedType(tv1);
        GenericMetadataSupport.TypeVarBoundedType b2 = new GenericMetadataSupport.TypeVarBoundedType(tv2);
        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());
    }

    // Tests equals and hashCode for WildCardBoundedType
    @Test
    public void testWildCardBoundedType_equalsAndHashCode() throws Exception {
        Method getMethod = WithGenericMethod.class.getMethod("getListLower");
        Type returnType = getMethod.getGenericReturnType();
        ParameterizedType pType = (ParameterizedType) returnType;
        WildcardType wildcard1 = (WildcardType) pType.getActualTypeArguments()[0];
        WildcardType wildcard2 = (WildcardType) pType.getActualTypeArguments()[0];
        GenericMetadataSupport.WildCardBoundedType wb1 = new GenericMetadataSupport.WildCardBoundedType(wildcard1);
        GenericMetadataSupport.WildCardBoundedType wb2 = new GenericMetadataSupport.WildCardBoundedType(wildcard2);
        assertEquals(wb1, wb2);
        assertEquals(wb1.hashCode(), wb2.hashCode());
    }
}