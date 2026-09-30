package org.mockito.internal.creation;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class DelegatingMethodTest {

    private Method stringLengthMethod;
    private Method stringFormatMethod;
    private Method listSizeMethod;
    private Method classForNameMethod;
    private DelegatingMethod delegatingMethod;

    @Before
    public void setUp() throws Exception {
        stringLengthMethod = String.class.getMethod("length");
        stringFormatMethod = String.class.getMethod("format", String.class, Object[].class);
        listSizeMethod = List.class.getMethod("size");
        classForNameMethod = Class.class.getMethod("forName", String.class);
        delegatingMethod = new DelegatingMethod(stringLengthMethod);
    }

    // Tests equals with the same DelegatingMethod instance
    @Test
    public void testEquals_sameInstance_returnsTrue() {
        assertTrue(delegatingMethod.equals(delegatingMethod));
    }

    // Tests equals with another DelegatingMethod wrapping the same underlying method (Defect 11b)
    @Test
    public void testEquals_twoDelegatingMethodsWithSameUnderlyingMethod_returnsTrue() {
        DelegatingMethod other = new DelegatingMethod(stringLengthMethod);
        assertTrue(delegatingMethod.equals(other));
    }

    // Tests equals with another DelegatingMethod wrapping a different underlying method
    @Test
    public void testEquals_twoDelegatingMethodsWithDifferentUnderlyingMethod_returnsFalse() {
        DelegatingMethod other = new DelegatingMethod(listSizeMethod);
        assertFalse(delegatingMethod.equals(other));
    }

    // Tests equals with the underlying Method object directly
    @Test
    public void testEquals_underlyingMethodInstance_returnsTrue() {
        assertTrue(delegatingMethod.equals(stringLengthMethod));
    }

    // Tests equals with a different Method object
    @Test
    public void testEquals_differentMethodInstance_returnsFalse() {
        assertFalse(delegatingMethod.equals(listSizeMethod));
    }

    // Tests equals with null input
    @Test
    public void testEquals_nullInput_returnsFalse() {
        assertFalse(delegatingMethod.equals(null));
    }

    // Tests equals with an object of an unrelated type
    @Test
    public void testEquals_unrelatedType_returnsFalse() {
        assertFalse(delegatingMethod.equals("someString"));
    }

    // Tests hashCode consistency and contract
    @Test
    public void testHashCode_validInstance_returnsConsistentHashCode() {
        assertEquals(1, delegatingMethod.hashCode());
    }

    // Tests getName delegating to underlying method
    @Test
    public void testGetName_standardMethod_returnsMethodName() {
        assertEquals("length", delegatingMethod.getName());
    }

    // Tests getJavaMethod returns the underlying Method object
    @Test
    public void testGetJavaMethod_standardMethod_returnsExactMethod() {
        assertSame(stringLengthMethod, delegatingMethod.getJavaMethod());
    }

    // Tests getReturnType returns expected class
    @Test
    public void testGetReturnType_returnsCorrectType() {
        assertEquals(int.class, delegatingMethod.getReturnType());
    }

    // Tests getParameterTypes returns expected parameter array
    @Test
    public void testGetParameterTypes_emptyParams_returnsEmptyArray() {
        assertArrayEquals(new Class<?>[0], delegatingMethod.getParameterTypes());
    }

    // Tests getParameterTypes for method with arguments
    @Test
    public void testGetParameterTypes_withParams_returnsCorrectArray() {
        DelegatingMethod formatDelegating = new DelegatingMethod(stringFormatMethod);
        assertArrayEquals(new Class<?>[]{String.class, Object[].class}, formatDelegating.getParameterTypes());
    }

    // Tests getExceptionTypes returns expected exceptions
    @Test
    public void testGetExceptionTypes_declaredExceptions_returnsExceptionTypes() {
        DelegatingMethod forNameDelegating = new DelegatingMethod(classForNameMethod);
        assertArrayEquals(new Class<?>[]{ClassNotFoundException.class}, forNameDelegating.getExceptionTypes());
    }

    // Tests getExceptionTypes for method without declared exceptions returns empty array
    @Test
    public void testGetExceptionTypes_noDeclaredExceptions_returnsEmptyArray() {
        assertArrayEquals(new Class<?>[0], delegatingMethod.getExceptionTypes());
    }

    // Tests isVarArgs for varargs method returns true
    @Test
    public void testIsVarArgs_varArgsMethod_returnsTrue() {
        DelegatingMethod formatDelegating = new DelegatingMethod(stringFormatMethod);
        assertTrue(formatDelegating.isVarArgs());
    }

    // Tests isVarArgs for non-varargs method returns false
    @Test
    public void testIsVarArgs_nonVarArgsMethod_returnsFalse() {
        assertFalse(delegatingMethod.isVarArgs());
    }

    // Tests isAbstract for abstract interface method returns true
    @Test
    public void testIsAbstract_abstractMethod_returnsTrue() {
        DelegatingMethod abstractDelegating = new DelegatingMethod(listSizeMethod);
        assertTrue(abstractDelegating.isAbstract());
    }

    // Tests isAbstract for concrete method returns false
    @Test
    public void testIsAbstract_concreteMethod_returnsFalse() {
        assertFalse(delegatingMethod.isAbstract());
    }

    // Tests isAbstract for static method returns false
    @Test
    public void testIsAbstract_staticMethod_returnsFalse() {
        DelegatingMethod forNameDelegating = new DelegatingMethod(classForNameMethod);
        assertFalse(forNameDelegating.isAbstract());
    }

    // Tests getReturnType for void return type
    @Test
    public void testGetReturnType_voidMethod_returnsVoidType() throws Exception {
        Method waitMethod = Object.class.getMethod("wait");
        DelegatingMethod waitDelegating = new DelegatingMethod(waitMethod);
        assertEquals(void.class, waitDelegating.getReturnType());
    }
}