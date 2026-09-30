package org.mockito.internal.stubbing.answers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;

import static org.junit.Assert.*;

/**
 * Test class for CallsRealMethods.
 * Uses dynamic proxy to simulate InvocationOnMock without relying on Mockito.mock().
 */
public class CallsRealMethodsTest {

    private static InvocationOnMock createInvocation(final Object returnValue, final Throwable throwable) {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("callRealMethod".equals(method.getName())) {
                    if (throwable != null) {
                        throw throwable;
                    }
                    return returnValue;
                }
                // For Object methods delegate to default behavior
                if (method.getDeclaringClass() == Object.class) {
                    return method.invoke(this, args);
                }
                // For other InvocationOnMock methods return default values
                Class<?> returnType = method.getReturnType();
                if (returnType == void.class) {
                    return null;
                }
                if (returnType.isPrimitive()) {
                    if (returnType == boolean.class) return false;
                    if (returnType == int.class) return 0;
                    if (returnType == long.class) return 0L;
                    if (returnType == double.class) return 0.0d;
                    if (returnType == float.class) return 0.0f;
                    if (returnType == short.class) return (short) 0;
                    if (returnType == byte.class) return (byte) 0;
                    if (returnType == char.class) return '\u0000';
                }
                return null;
            }
        };
        return (InvocationOnMock) Proxy.newProxyInstance(
                InvocationOnMock.class.getClassLoader(),
                new Class<?>[] { InvocationOnMock.class },
                handler);
    }

    // Normal case: callRealMethod returns a value, answer returns the same value
    @Test
    public void testAnswer_normalReturn_returnsSameValue() throws Throwable {
        Object expected = "testValue";
        InvocationOnMock invocation = createInvocation(expected, null);
        CallsRealMethods answer = new CallsRealMethods();

        Object actual = answer.answer(invocation);

        assertEquals(expected, actual);
    }

    // Null case: callRealMethod returns null, answer returns null
    @Test
    public void testAnswer_nullReturn_returnsNull() throws Throwable {
        InvocationOnMock invocation = createInvocation(null, null);
        CallsRealMethods answer = new CallsRealMethods();

        Object actual = answer.answer(invocation);

        assertNull(actual);
    }

    // Runtime exception: callRealMethod throws RuntimeException, answer propagates it
    @Test(expected = RuntimeException.class)
    public void testAnswer_runtimeExceptionThrown_throwsRuntimeException() throws Throwable {
        RuntimeException ex = new RuntimeException("expected runtime");
        InvocationOnMock invocation = createInvocation(null, ex);
        CallsRealMethods answer = new CallsRealMethods();

        answer.answer(invocation);
    }

    // Checked exception: callRealMethod throws checked Exception, answer propagates it
    @Test(expected = Exception.class)
    public void testAnswer_checkedExceptionThrown_throwsException() throws Throwable {
        Exception ex = new Exception("expected checked");
        InvocationOnMock invocation = createInvocation(null, ex);
        CallsRealMethods answer = new CallsRealMethods();

        answer.answer(invocation);
    }

    // Error: callRealMethod throws Error, answer propagates it
    @Test(expected = Error.class)
    public void testAnswer_errorThrown_throwsError() throws Throwable {
        Error err = new Error("expected error");
        InvocationOnMock invocation = createInvocation(null, err);
        CallsRealMethods answer = new CallsRealMethods();

        answer.answer(invocation);
    }
}