package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.concurrent.Callable;
import org.mockito.Mockito;

public class ReturnsSmartNullsTest {

    @Test
    public void testAnswer_objectReturnType_returnsSmartNull() {
        // Test that when delegate returns null and return type is mockable, a smart null is returned
        List<Object> mock = Mockito.mock(List.class, new ReturnsSmartNulls());
        Object result = mock.get(0);
        assertNotNull(result);
    }

    @Test
    public void testAnswer_primitiveReturnType_returnsDefaultValue() {
        // Test that for primitive int, delegate returns default (0) -> not null
        List<Object> mock = Mockito.mock(List.class, new ReturnsSmartNulls());
        int size = mock.size();
        assertEquals(0, size);
    }

    @Test
    public void testAnswer_booleanReturnType_returnsDefaultValue() {
        // Test that for boolean return, delegate returns false
        List<Object> mock = Mockito.mock(List.class, new ReturnsSmartNulls());
        boolean added = mock.add("item");
        assertFalse(added);
    }

    @Test
    public void testAnswer_stringReturnType_returnsEmptyString() {
        // Test that for String return, delegate returns empty String
        List<Object> mock = Mockito.mock(List.class, new ReturnsSmartNulls());
        String str = mock.toString();
        assertEquals("", str);
    }

    @Test
    public void testAnswer_voidReturnType_returnsVoid() {
        // Test that void method is handled without exception
        Runnable mock = Mockito.mock(Runnable.class, new ReturnsSmartNulls());
        mock.run();
    }

    @Test
    public void testSmartNull_toString_returnsFormattedMessage() {
        // Test that toString on smart null returns the expected message
        List<Object> mock = Mockito.mock(List.class, new ReturnsSmartNulls());
        Object proxy = mock.get(0);
        String result = proxy.toString();
        assertEquals("SmartNull returned by unstubbed get() method on mock", result);
    }

    @Test(expected = RuntimeException.class)
    public void testSmartNull_nonToStringMethod_throwsException() {
        // Test that invoking a non-toString method on smart null throws RuntimeException (SmartNullPointerException)
        List<Object> mock = Mockito.mock(List.class, new ReturnsSmartNulls());
        Object proxy = mock.get(0);
        proxy.hashCode();
    }

    @Test(expected = RuntimeException.class)
    public void testSmartNull_equalsMethod_throwsException() {
        // Test that equals method on smart null also throws
        List<Object> mock = Mockito.mock(List.class, new ReturnsSmartNulls());
        Object proxy = mock.get(0);
        proxy.equals(new Object());
    }

    @Test
    public void testSmartNull_fromMethodNoArgs_toString() throws Exception {
        // Test smart null from method with no arguments (Callable.call())
        Callable<Object> mock = Mockito.mock(Callable.class, new ReturnsSmartNulls());
        Object proxy = mock.call();
        String str = proxy.toString();
        assertTrue(str.contains("call()"));
    }

    @Test
    public void testSmartNull_toString_containsMethodName() {
        // Verify the formatted message contains the unstubbed method name
        List<Object> mock = Mockito.mock(List.class, new ReturnsSmartNulls());
        Object proxy = mock.get(0);
        String str = proxy.toString();
        assertTrue(str.contains("get()"));
    }
}