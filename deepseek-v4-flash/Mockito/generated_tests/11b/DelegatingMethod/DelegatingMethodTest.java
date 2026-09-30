package org.mockito.internal.creation;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.List;

public class DelegatingMethodTest {

    // Helper methods to get sample methods
    public void sampleMethod() {}
    public void sampleMethodWithParams(int a, String b) {}
    public void methodWithException() throws InterruptedException {}

    private Method getMethod(String name, Class<?>... paramTypes) throws NoSuchMethodException {
        return getClass().getMethod(name, paramTypes);
    }

    @Test
    public void testGetName_normalMethod_returnsCorrectName() throws Exception {
        Method method = getMethod("sampleMethod");
        DelegatingMethod dm = new DelegatingMethod(method);
        assertEquals("sampleMethod", dm.getName());
    }

    @Test
    public void testGetReturnType_voidMethod_returnsVoidType() throws Exception {
        Method method = getMethod("sampleMethod");
        DelegatingMethod dm = new DelegatingMethod(method);
        assertEquals(void.class, dm.getReturnType());
    }

    @Test
    public void testGetParameterTypes_noParams_returnsEmptyArray() throws Exception {
        Method method = getMethod("sampleMethod");
        DelegatingMethod dm = new DelegatingMethod(method);
        assertArrayEquals(new Class<?>[0], dm.getParameterTypes());
    }

    @Test
    public void testGetParameterTypes_withParams_returnsCorrectArray() throws Exception {
        Method method = getMethod("sampleMethodWithParams", int.class, String.class);
        DelegatingMethod dm = new DelegatingMethod(method);
        assertArrayEquals(new Class<?>[]{int.class, String.class}, dm.getParameterTypes());
    }

    @Test
    public void testIsVarArgs_nonVarArgsMethod_returnsFalse() throws Exception {
        Method method = getMethod("sampleMethod");
        DelegatingMethod dm = new DelegatingMethod(method);
        assertFalse(dm.isVarArgs());
    }

    @Test
    public void testIsVarArgs_varArgsMethod_returnsTrue() throws Exception {
        Method method = String.class.getMethod("format", String.class, Object[].class);
        DelegatingMethod dm = new DelegatingMethod(method);
        assertTrue(dm.isVarArgs());
    }

    @Test
    public void testIsAbstract_nonAbstractMethod_returnsFalse() throws Exception {
        Method method = getMethod("sampleMethod");
        DelegatingMethod dm = new DelegatingMethod(method);
        assertFalse(dm.isAbstract());
    }

    @Test
    public void testIsAbstract_abstractMethod_returnsTrue() throws Exception {
        Method method = List.class.getMethod("add", Object.class);
        DelegatingMethod dm = new DelegatingMethod(method);
        assertTrue(dm.isAbstract());
    }

    @Test
    public void testGetJavaMethod_returnsSameMethod() throws Exception {
        Method method = getMethod("sampleMethod");
        DelegatingMethod dm = new DelegatingMethod(method);
        assertSame(method, dm.getJavaMethod());
    }

    @Test
    public void testGetExceptionTypes_noExceptions_returnsEmptyArray() throws Exception {
        Method method = getMethod("sampleMethod");
        DelegatingMethod dm = new DelegatingMethod(method);
        assertArrayEquals(new Class<?>[0], dm.getExceptionTypes());
    }

    @Test
    public void testGetExceptionTypes_methodWithException_returnsCorrectArray() throws Exception {
        Method method = getClass().getMethod("methodWithException");
        DelegatingMethod dm = new DelegatingMethod(method);
        Class<?>[] expected = {InterruptedException.class};
        assertArrayEquals(expected, dm.getExceptionTypes());
    }

    // Tests for equals and hashCode – designed to expose the equals defect

    @Test
    public void testEquals_sameDelegatingMethod_returnsTrue() throws Exception {
        Method method = getMethod("sampleMethod");
        DelegatingMethod dm1 = new DelegatingMethod(method);
        DelegatingMethod dm2 = new DelegatingMethod(method);
        // This assertion should fail with current implementation, revealing the defect
        assertTrue("equals between DelegatingMethods with same underlying method should be true", dm1.equals(dm2));
    }

    @Test
    public void testEquals_differentDelegatingMethod_returnsFalse() throws Exception {
        Method method1 = getMethod("sampleMethod");
        Method method2 = getMethod("sampleMethodWithParams", int.class, String.class);
        DelegatingMethod dm1 = new DelegatingMethod(method1);
        DelegatingMethod dm2 = new DelegatingMethod(method2);
        assertFalse(dm1.equals(dm2));
    }

    @Test
    public void testEquals_compareWithMethodObject_equalMethod_returnsTrue() throws Exception {
        Method method = getMethod("sampleMethod");
        DelegatingMethod dm = new DelegatingMethod(method);
        assertTrue(dm.equals(method));
    }

    @Test
    public void testEquals_compareWithMethodObject_differentMethod_returnsFalse() throws Exception {
        Method method1 = getMethod("sampleMethod");
        Method method2 = getMethod("sampleMethodWithParams", int.class, String.class);
        DelegatingMethod dm = new DelegatingMethod(method1);
        assertFalse(dm.equals(method2));
    }

    @Test
    public void testEquals_compareWithNull_returnsFalse() throws Exception {
        Method method = getMethod("sampleMethod");
        DelegatingMethod dm = new DelegatingMethod(method);
        assertFalse(dm.equals(null));
    }

    @Test
    public void testHashCode_alwaysReturnsOne() throws Exception {
        Method method = getMethod("sampleMethod");
        DelegatingMethod dm = new DelegatingMethod(method);
        assertEquals(1, dm.hashCode());
    }

}