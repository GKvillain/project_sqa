package org.mockito.internal.invocation;

import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.mockito.internal.invocation.realmethod.RealMethod;
import org.mockito.internal.invocation.MockitoMethod;
import java.io.IOException;

public class InvocationTest {

    // Tests constructor initializes fields correctly (non-varargs)
    @Test
    public void testConstructor_initializesFields() {
        Object mock = new Object();
        MockitoMethod method = mock(MockitoMethod.class);
        RealMethod realMethod = mock(RealMethod.class);
        Object[] args = new Object[] {"a", "b"};
        when(method.isVarArgs()).thenReturn(false);
        Invocation inv = new Invocation(mock, method, args, 5, realMethod);
        assertSame(mock, inv.getMock());
        assertSame(method, inv.getMethod());
        assertArrayEquals(args, inv.getArguments());
        assertEquals(5, inv.getSequenceNumber().intValue());
        assertFalse(inv.isVerified());
        assertFalse(inv.isVerifiedInOrder());
    }

    // Tests varargs expansion when last argument is an array
    @Test
    public void testConstructor_varArgsExpandsArrays() {
        Object mock = new Object();
        MockitoMethod method = mock(MockitoMethod.class);
        RealMethod realMethod = mock(RealMethod.class);
        Object[] args = new Object[] {"a", new Object[] {1, 2}};
        when(method.isVarArgs()).thenReturn(true);
        Invocation inv = new Invocation(mock, method, args, 1, realMethod);
        Object[] expected = {"a", 1, 2};
        assertArrayEquals(expected, inv.getArguments());
    }

    // Tests varargs with null last argument expands to array containing null
    @Test
    public void testConstructor_varArgsNullLastArg() {
        Object mock = new Object();
        MockitoMethod method = mock(MockitoMethod.class);
        RealMethod realMethod = mock(RealMethod.class);
        Object[] args = new Object[] {"a", null};
        when(method.isVarArgs()).thenReturn(true);
        Invocation inv = new Invocation(mock, method, args, 1, realMethod);
        Object[] expected = {"a", null};
        assertArrayEquals(expected, inv.getArguments());
        assertEquals(2, inv.getArguments().length);
    }

    // Tests varargs with non-array last argument (no expansion)
    @Test
    public void testConstructor_varArgsNonArrayLastArg() {
        Object mock = new Object();
        MockitoMethod method = mock(MockitoMethod.class);
        RealMethod realMethod = mock(RealMethod.class);
        Object[] args = new Object[] {"a", "b"};
        when(method.isVarArgs()).thenReturn(true);
        Invocation inv = new Invocation(mock, method, args, 1, realMethod);
        assertArrayEquals(args, inv.getArguments());
    }

    // Tests equals returns true for same mock, method, and arguments
    @Test
    public void testEquals_sameObject_returnsTrue() {
        Object mock = new Object();
        MockitoMethod method = mock(MockitoMethod.class);
        RealMethod realMethod = mock(RealMethod.class);
        when(method.isVarArgs()).thenReturn(false);
        Invocation inv1 = new Invocation(mock, method, new Object[] {"test"}, 1, realMethod);
        Invocation inv2 = new Invocation(mock, method, new Object[] {"test"}, 2, realMethod);
        assertTrue(inv1.equals(inv2));
    }

    // Tests equals returns false for different mock
    @Test
    public void testEquals_differentMock_returnsFalse() {
        Object mock = new Object();
        Object otherMock = new Object();
        MockitoMethod method = mock(MockitoMethod.class);
        RealMethod realMethod = mock(RealMethod.class);
        when(method.isVarArgs()).thenReturn(false);
        Invocation inv1 = new Invocation(mock, method, new Object[] {"test"}, 1, realMethod);
        Invocation inv2 = new Invocation(otherMock, method, new Object[] {"test"}, 1, realMethod);
        assertFalse(inv1.equals(inv2));
    }

    // Tests equals returns false for different method
    @Test
    public void testEquals_differentMethod_returnsFalse() {
        Object mock = new Object();
        MockitoMethod method1 = mock(MockitoMethod.class);
        MockitoMethod method2 = mock(MockitoMethod.class);
        RealMethod realMethod = mock(RealMethod.class);
        when(method1.isVarArgs()).thenReturn(false);
        when(method2.isVarArgs()).thenReturn(false);
        Invocation inv1 = new Invocation(mock, method1, new Object[] {"test"}, 1, realMethod);
        Invocation inv2 = new Invocation(mock, method2, new Object[] {"test"}, 1, realMethod);
        assertFalse(inv1.equals(inv2));
    }

    // Tests equals returns false for different arguments
    @Test
    public void testEquals_differentArguments_returnsFalse() {
        Object mock = new Object();
        MockitoMethod method = mock(MockitoMethod.class);
        RealMethod realMethod = mock(RealMethod.class);
        when(method.isVarArgs()).thenReturn(false);
        Invocation inv1 = new Invocation(mock, method, new Object[] {"a"}, 1, realMethod);
        Invocation inv2 = new Invocation(mock, method, new Object[] {"b"}, 1, realMethod);
        assertFalse(inv1.equals(inv2));
    }

    // Tests equals returns false for null object
    @Test
    public void testEquals_nullObject_returnsFalse() {
        Object mock = new Object();
        MockitoMethod method = mock(MockitoMethod.class);
        RealMethod realMethod = mock(RealMethod.class);
        when(method.isVarArgs()).thenReturn(false);
        Invocation inv = new Invocation(mock, method, new Object[] {"test"}, 1, realMethod);
        assertFalse(inv.equals(null));
    }

    // Tests equals returns false for object of different class
    @Test
    public void testEquals_wrongClass_returnsFalse() {
        Object mock = new Object();
        MockitoMethod method = mock(MockitoMethod.class);
        RealMethod realMethod = mock(RealMethod.class);
        when(method.isVarArgs()).thenReturn(false);
        Invocation inv = new Invocation(mock, method, new Object[] {"test"}, 1, realMethod);
        assertFalse(inv.equals("string"));
    }

    // Tests equals with varargs expansions: different raw args but same expanded arguments
    @Test
    public void testEquals_varArgsExpandedEqual() {
        Object mock = new Object();
        MockitoMethod method = mock(MockitoMethod.class);
        RealMethod realMethod = mock(RealMethod.class);
        when(method.isVarArgs()).thenReturn(true);
        Invocation inv1 = new Invocation(mock, method, new Object[] {"a", new Object[] {1, 2}}, 1, realMethod);
        Invocation inv2 = new Invocation(mock, method, new Object[] {"a", 1, 2}, 2, realMethod);
        assertTrue(inv1.equals(inv2));
    }

    // Tests markVerified and markVerifiedInOrder set flags correctly
    @Test
    public void testMarkVerified_setsFlags() {
        Object mock = new Object();
        MockitoMethod method = mock(MockitoMethod.class);
        RealMethod realMethod = mock(RealMethod.class);
        when(method.isVarArgs()).thenReturn(false);
        Invocation inv = new Invocation(mock, method, new Object[] {"test"}, 1, realMethod);
        assertFalse(inv.isVerified());
        assertFalse(inv.isVerifiedInOrder());
        inv.markVerified();
        assertTrue(inv.isVerified());
        assertFalse(inv.isVerifiedInOrder());
        inv.markVerifiedInOrder();
        assertTrue(inv.isVerifiedInOrder());
    }

    // Tests isValidException returns true when exception type is declared
    @Test
    public void testIsValidException_matchingException_returnsTrue() {
        Object mock = new Object();
        MockitoMethod method = mock(MockitoMethod.class);
        RealMethod realMethod = mock(RealMethod.class);
        when(method.isVarArgs()).thenReturn(false);
        when(method.getExceptionTypes()).thenReturn(new Class<?>[] {RuntimeException.class, IOException.class});
        Invocation inv = new Invocation(mock, method, new Object[] {}, 1, realMethod);
        assertTrue(inv.isValidException(new RuntimeException()));
        assertTrue(inv.isValidException(new IOException()));
    }

    // Tests isValidException returns false when exception type is not declared
    @Test
    public void testIsValidException_nonMatchingException_returnsFalse() {
        Object mock = new Object();
        MockitoMethod method = mock(MockitoMethod.class);
        RealMethod realMethod = mock(RealMethod.class);
        when(method.isVarArgs()).thenReturn(false);
        when(method.getExceptionTypes()).thenReturn(new Class<?>[] {IOException.class});
        Invocation inv = new Invocation(mock, method, new Object[] {}, 1, realMethod);
        assertFalse(inv.isValidException(new RuntimeException()));
    }

    // Tests isValidReturnType returns true for primitive matching wrapper
    @Test
    public void testIsValidReturnType_primitiveMatch_returnsTrue() {
        Object mock = new Object();
        MockitoMethod method = mock(MockitoMethod.class);
        RealMethod realMethod = mock(RealMethod.class);
        when(method.isVarArgs()).thenReturn(false);
        doReturn(int.class).when(method).getReturnType();
        Invocation inv = new Invocation(mock, method, new Object[] {}, 1, realMethod);
        assertTrue(inv.isValidReturnType(Integer.class));
    }

    // Tests isValidReturnType returns false for primitive mismatch
    @Test
    public void testIsValidReturnType_primitiveMismatch_returnsFalse() {
        Object mock = new Object();
        MockitoMethod method = mock(MockitoMethod.class);
        RealMethod realMethod = mock(RealMethod.class);
        when(method.isVarArgs()).thenReturn(false);
        doReturn(int.class).when(method).getReturnType();
        Invocation inv = new Invocation(mock, method, new Object[] {}, 1, realMethod);
        assertFalse(inv.isValidReturnType(Long.class));
    }

    // Tests isValidReturnType returns true for assignable types when return type is not primitive
    @Test
    public void testIsValidReturnType_assignableMatch_returnsTrue() {
        Object mock = new Object();
        MockitoMethod method = mock(MockitoMethod.class);
        RealMethod realMethod = mock(RealMethod.class);
        when(method.isVarArgs()).thenReturn(false);
        doReturn(Object.class).when(method).getReturnType();
        Invocation inv = new Invocation(mock, method, new Object[] {}, 1, realMethod);
        assertTrue(inv.isValidReturnType(String.class));
        assertTrue(inv.isValidReturnType(Integer.class));
    }

    // Tests isVoid returns true for void return type
    @Test
    public void testIsVoid_true() {
        Object mock = new Object();
        MockitoMethod method = mock(MockitoMethod.class);
        RealMethod realMethod = mock(RealMethod.class);
        when(method.isVarArgs()).thenReturn(false);
        doReturn(void.class).when(method).getReturnType();
        Invocation inv = new Invocation(mock, method, new Object[] {}, 1, realMethod);
        assertTrue(inv.isVoid());
    }

    // Tests isVoid returns false for non-void return type
    @Test
    public void testIsVoid_false() {
        Object mock = new Object();
        MockitoMethod method = mock(MockitoMethod.class);
        RealMethod realMethod = mock(RealMethod.class);
        when(method.isVarArgs()).thenReturn(false);
        doReturn(String.class).when(method).getReturnType();
        Invocation inv = new Invocation(mock, method, new Object[] {}, 1, realMethod);
        assertFalse(inv.isVoid());
    }

    // Tests callRealMethod invokes real method on mock
    @Test
    public void testCallRealMethod_invokesRealMethod() throws Throwable {
        Object mock = new Object();
        MockitoMethod method = mock(MockitoMethod.class);
        RealMethod realMethod = mock(RealMethod.class);
        when(method.isVarArgs()).thenReturn(false);
        Object[] args = new Object[] {};
        when(realMethod.invoke(mock, args)).thenReturn("result");
        Invocation inv = new Invocation(mock, method, args, 1, realMethod);
        assertEquals("result", inv.callRealMethod());
        verify(realMethod).invoke(mock, args);
    }
}