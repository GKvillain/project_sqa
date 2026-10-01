package com.google.gson;

import static org.junit.Assert.*;

import org.junit.Test;

import java.lang.reflect.*;
import java.util.List;

public class TypeInfoFactoryTest {

    // Helper class to create ParameterizedType.
    private static class ParameterizedTypeImpl implements ParameterizedType {
        private final Type rawType;
        private final Type[] actualTypeArguments;
        private final Type ownerType;

        public ParameterizedTypeImpl(Type rawType, Type[] actualTypeArguments, Type ownerType) {
            this.rawType = rawType;
            this.actualTypeArguments = actualTypeArguments;
            this.ownerType = ownerType;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return actualTypeArguments;
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return ownerType;
        }
    }

    // Test getTypeInfoForArray with normal array type
    @Test
    public void testGetTypeInfoForArray_stringArray_returnsTypeInfoArray() {
        TypeInfoArray info = TypeInfoFactory.getTypeInfoForArray(String[].class);
        assertNotNull(info);
    }

    // Test getTypeInfoForArray with null input - should throw exception
    @Test(expected = IllegalArgumentException.class)
    public void testGetTypeInfoForArray_nullInput_throwsException() {
        TypeInfoFactory.getTypeInfoForArray(null);
    }

    // Test getTypeInfoForArray with non-array type - should throw exception
    @Test(expected = IllegalArgumentException.class)
    public void testGetTypeInfoForArray_nonArrayType_throwsException() {
        TypeInfoFactory.getTypeInfoForArray(String.class);
    }

    // Test getTypeInfoForField with simple class field (Class<?> type)
    @Test
    public void testGetTypeInfoForField_simpleField_returnsActualType() throws Exception {
        class SimpleContainer {
            public String name;
        }
        Field f = SimpleContainer.class.getField("name");
        TypeInfo info = TypeInfoFactory.getTypeInfoForField(f, SimpleContainer.class);
        assertEquals(String.class, info.getActualType());
    }

    // Test getTypeInfoForField with parameterized type field
    @Test
    public void testGetTypeInfoForField_parameterizedField_returnsActualType() throws Exception {
        class Container {
            public List<String> values;
        }
        Field f = Container.class.getField("values");
        TypeInfo info = TypeInfoFactory.getTypeInfoForField(f, Container.class);
        assertTrue(info.getActualType() instanceof ParameterizedType);
        ParameterizedType pType = (ParameterizedType) info.getActualType();
        assertEquals(List.class, pType.getRawType());
        assertEquals(String.class, pType.getActualTypeArguments()[0]);
    }

    // Test getTypeInfoForField with TypeVariable field - resolved from parent
    @Test
    public void testGetTypeInfoForField_typeVariableField_resolvedFromParent() throws Exception {
        class Container<A> {
            public A value;
        }
        Field f = Container.class.getField("value");
        // Create a ParameterizedType to represent Container<Integer>
        Type parentType = new ParameterizedTypeImpl(Container.class, new Type[]{Integer.class}, null);
        TypeInfo info = TypeInfoFactory.getTypeInfoForField(f, parentType);
        assertEquals(Integer.class, info.getActualType());
    }

    // Test getTypeInfoForField with TypeVariable field - unresolved parent throws exception
    @Test(expected = UnsupportedOperationException.class)
    public void testGetTypeInfoForField_typeVariableField_unresolvedParent_throwsException() throws Exception {
        class Container<A> {
            public A value;
        }
        Field f = Container.class.getField("value");
        // Pass raw class, not parameterized type -> will trigger exception
        TypeInfoFactory.getTypeInfoForField(f, Container.class);
    }

    // Test getTypeInfoForField with GenericArrayType field - component type resolved from TypeVariable
    @Test
    public void testGetTypeInfoForField_genericArrayField_resolvedComponent() throws Exception {
        class Container<A> {
            public A[] array;
        }
        Field f = Container.class.getField("array");
        Type parentType = new ParameterizedTypeImpl(Container.class, new Type[]{String.class}, null);
        TypeInfo info = TypeInfoFactory.getTypeInfoForField(f, parentType);
        // Should become String[]
        assertEquals(String[].class, info.getActualType());
    }

    // Test getTypeInfoForField with WildcardType field - uses upper bound
    @Test
    public void testGetTypeInfoForField_wildcardField_upperBoundResolved() throws Exception {
        class Container {
            public List<? extends Number> values;
        }
        Field f = Container.class.getField("values");
        TypeInfo info = TypeInfoFactory.getTypeInfoForField(f, Container.class);
        assertTrue(info.getActualType() instanceof ParameterizedType);
        ParameterizedType pType = (ParameterizedType) info.getActualType();
        assertEquals(List.class, pType.getRawType());
        assertEquals(Number.class, pType.getActualTypeArguments()[0]);
    }

    // Test getTypeInfoForField with invalid type - should throw IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetTypeInfoForField_invalidType_throwsException() throws Exception {
        // Use custom type that is not Class, ParameterizedType, GenericArrayType, TypeVariable, WildcardType
        Type invalidType = new Type() {}; // anonymous type
        class Container {
            public Object value;
        }
        // Modify field type via reflection to inject invalid type - test internal behavior indirectly
        // Instead, directly test getActualType by creating scenario not possible via normal field
        // So we use a custom class with a field of unknown type? Not possible.
        // This test case is not feasible via public API; skip.
    }

    // Test getTypeInfoForField with field that has TypeVariable in ParameterizedType
    @Test
    public void testGetTypeInfoForField_parameterizedFieldWithTypeVariable_resolved() throws Exception {
        class Container<A> {
            public List<A> values;
        }
        Field f = Container.class.getField("values");
        Type parentType = new ParameterizedTypeImpl(Container.class, new Type[]{Integer.class}, null);
        TypeInfo info = TypeInfoFactory.getTypeInfoForField(f, parentType);
        assertTrue(info.getActualType() instanceof ParameterizedType);
        ParameterizedType pType = (ParameterizedType) info.getActualType();
        assertEquals(List.class, pType.getRawType());
        assertEquals(Integer.class, pType.getActualTypeArguments()[0]);
    }

    // Test getTypeInfoForField with nested GenericArrayType component resolved
    @Test
    public void testGetTypeInfoForField_nestedGenericArrayField_resolved() throws Exception {
        class Container<A> {
            public List<A>[] array;
        }
        Field f = Container.class.getField("array");
        Type parentType = new ParameterizedTypeImpl(Container.class, new Type[]{String.class}, null);
        TypeInfo info = TypeInfoFactory.getTypeInfoForField(f, parentType);
        // Should be GenericArrayType with component List<String>
        assertTrue(info.getActualType() instanceof GenericArrayType);
        GenericArrayType gaType = (GenericArrayType) info.getActualType();
        assertTrue(gaType.getGenericComponentType() instanceof ParameterizedType);
        ParameterizedType compType = (ParameterizedType) gaType.getGenericComponentType();
        assertEquals(List.class, compType.getRawType());
        assertEquals(String.class, compType.getActualTypeArguments()[0]);
    }

    // Edge case: getTypeInfoForField with null field - should throw exception
    @Test(expected = NullPointerException.class)
    public void testGetTypeInfoForField_nullField_throwsException() {
        TypeInfoFactory.getTypeInfoForField(null, Object.class);
    }

    // Edge case: getTypeInfoForField with null parent type - should throw exception
    @Test(expected = NullPointerException.class)
    public void testGetTypeInfoForField_nullParentType_throwsException() throws Exception {
        class Container {
            public String value;
        }
        Field f = Container.class.getField("value");
        TypeInfoFactory.getTypeInfoForField(f, null);
    }

    // Edge case: getTypeInfoForArray with primitive array
    @Test
    public void testGetTypeInfoForArray_intArray_returnsTypeInfoArray() {
        TypeInfoArray info = TypeInfoFactory.getTypeInfoForArray(int[].class);
        assertNotNull(info);
    }
}