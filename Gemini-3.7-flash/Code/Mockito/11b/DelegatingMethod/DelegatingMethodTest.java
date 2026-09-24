package org.mockito.internal.creation;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class DelegatingMethodTest {

    private interface SampleInterface {
        void abstractMethod() throws Exception;
        void varArgsMethod(String... args);
        String regularMethod(int a, String b);
    }

    private static class SampleClass implements SampleInterface {
        @Override
        public void abstractMethod() throws Exception {}

        @Override
        public void varArgsMethod(String... args) {}

        @Override
        public String regularMethod(int a, String b) {
            return b;
        }
    }

    private Method regularMethod;
    private Method interfaceRegularMethod;
    private Method abstractMethod;
    private Method varArgsMethod;
    private DelegatingMethod delegatingRegularMethod;

    @Before
    public void setUp() throws Exception {
        regularMethod = SampleClass.class.getMethod("regularMethod", int.class, String.class);
        interfaceRegularMethod = SampleInterface.class.getMethod("regularMethod", int.class, String.class);
        abstractMethod = SampleInterface.class.getMethod("abstractMethod");
        varArgsMethod = SampleInterface.class.getMethod("varArgsMethod", String[].class);
        delegatingRegularMethod = new DelegatingMethod(regularMethod);
    }

    // Tests equals with another DelegatingMethod wrapping the same Method
    @Test
    public void testEquals_anotherDelegatingMethodWithSameMethod_returnsTrue() {
        DelegatingMethod other = new DelegatingMethod(regularMethod);
        assertTrue(delegatingRegularMethod.equals(other));
    }

    // Tests equals with the same DelegatingMethod instance
    @Test
    public void testEquals_sameInstance_returnsTrue() {
        assertTrue(delegatingRegularMethod.equals(delegatingRegularMethod));
    }

    // Tests equals with the underlying java.lang.reflect.Method instance
    @Test
    public void testEquals_underlyingMethodInstance_returnsTrue() {
        assertTrue(delegatingRegularMethod.equals(regularMethod));
    }

    // Tests equals with a different java.lang.reflect.Method instance directly
    @Test
    public void testEquals_differentUnderlyingMethod_returnsFalse() {
        assertFalse(delegatingRegularMethod.equals(abstractMethod));
    }

    // Tests equals with a DelegatingMethod wrapping an interface method versus class method
    @Test
    public void testEquals_interfaceVsClassMethod_returnsFalse() {
        DelegatingMethod interfaceDelegatingMethod = new DelegatingMethod(interfaceRegularMethod);
        assertFalse(delegatingRegularMethod.equals(interfaceDelegatingMethod));
    }

    // Tests equals with a DelegatingMethod wrapping a different Method
    @Test
    public void testEquals_differentMethod_returnsFalse() {
        DelegatingMethod other = new DelegatingMethod(abstractMethod);
        assertFalse(delegatingRegularMethod.equals(other));
    }

    // Tests equals with null input
    @Test
    public void testEquals_nullInput_returnsFalse() {
        assertFalse(delegatingRegularMethod.equals(null));
    }

    // Tests equals with an unrelated object type
    @Test
    public void testEquals_differentObjectType_returnsFalse() {
        assertFalse(delegatingRegularMethod.equals("someString"));
    }

    // Tests hashCode value
    @Test
    public void testHashCode_validMethod_returnsOne() {
        assertEquals(1, delegatingRegularMethod.hashCode());
    }

    // Tests getName returns the correct method name
    @Test
    public void testGetName_validMethod_returnsCorrectName() {
        assertEquals("regularMethod", delegatingRegularMethod.getName());
    }

    // Tests getJavaMethod returns the wrapped Method instance
    @Test
    public void testGetJavaMethod_validMethod_returnsWrappedMethod() {
        assertSame(regularMethod, delegatingRegularMethod.getJavaMethod());
    }

    // Tests getReturnType returns the correct return type
    @Test
    public void testGetReturnType_validMethod_returnsCorrectReturnType() {
        assertEquals(String.class, delegatingRegularMethod.getReturnType());
    }

    // Tests getParameterTypes returns the correct parameter types
    @Test
    public void testGetParameterTypes_validMethod_returnsCorrectParameterTypes() {
        Class<?>[] paramTypes = delegatingRegularMethod.getParameterTypes();
        assertArrayEquals(new Class<?>[]{int.class, String.class}, paramTypes);
    }

    // Tests getExceptionTypes returns the correct exception types
    @Test
    public void testGetExceptionTypes_methodWithExceptions_returnsExceptionTypes() {
        DelegatingMethod delegating = new DelegatingMethod(abstractMethod);
        Class<?>[] exceptionTypes = delegating.getExceptionTypes();
        assertArrayEquals(new Class<?>[]{Exception.class}, exceptionTypes);
    }

    // Tests isVarArgs on a varargs method returns true
    @Test
    public void testIsVarArgs_varArgsMethod_returnsTrue() {
        DelegatingMethod delegating = new DelegatingMethod(varArgsMethod);
        assertTrue(delegating.isVarArgs());
    }

    // Tests isVarArgs on a non-varargs method returns false
    @Test
    public void testIsVarArgs_nonVarArgsMethod_returnsFalse() {
        assertFalse(delegatingRegularMethod.isVarArgs());
    }

    // Tests isAbstract on an abstract interface method returns true
    @Test
    public void testIsAbstract_abstractMethod_returnsTrue() {
        DelegatingMethod delegating = new DelegatingMethod(abstractMethod);
        assertTrue(delegating.isAbstract());
    }

    // Tests isAbstract on a concrete class method returns false
    @Test
    public void testIsAbstract_concreteMethod_returnsFalse() {
        assertFalse(delegatingRegularMethod.isAbstract());
    }
}