package org.mockito.internal.invocation;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.invocation.realmethod.RealMethod;
import org.mockito.internal.reporting.PrintSettings;

import static org.junit.Assert.*;

public class InvocationTest {

    private Object mock;
    private MockitoMethod mockMethod;
    private RealMethod realMethod;

    @Before
    public void setUp() {
        mock = new Object();
        mockMethod = createMockitoMethod("simpleMethod", String.class, new Class<?>[]{IOException.class}, false);
        realMethod = createRealMethod("realResult");
    }

    private MockitoMethod createMockitoMethod(final String name, final Class<?> returnType, final Class<?>[] exceptionTypes, final boolean isVarArgs) {
        return (MockitoMethod) Proxy.newProxyInstance(
                MockitoMethod.class.getClassLoader(),
                new Class<?>[]{MockitoMethod.class},
                new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String methodName = method.getName();
                        if ("getName".equals(methodName)) {
                            return name;
                        }
                        if ("getReturnType".equals(methodName)) {
                            return returnType;
                        }
                        if ("getExceptionTypes".equals(methodName)) {
                            return exceptionTypes != null ? exceptionTypes : new Class<?>[0];
                        }
                        if ("getParameterTypes".equals(methodName)) {
                            return new Class<?>[0];
                        }
                        if ("isVarArgs".equals(methodName)) {
                            return isVarArgs;
                        }
                        if ("equals".equals(methodName)) {
                            return proxy == args[0];
                        }
                        if ("hashCode".equals(methodName)) {
                            return System.identityHashCode(proxy);
                        }
                        if ("toString".equals(methodName)) {
                            return "MockitoMethod$" + name;
                        }
                        return null;
                    }
                }
        );
    }

    private RealMethod createRealMethod(final Object returnValue) {
        return new RealMethod() {
            private static final long serialVersionUID = 1L;

            public Object invoke(Object target, Object[] arguments) throws Throwable {
                return returnValue;
            }
        };
    }

    // Tests basic constructor initialization and getter accessors
    @Test
    public void testGetters_validInvocation_returnsCorrectProperties() {
        Object[] args = new Object[]{"arg1", 123};
        Invocation invocation = new Invocation(mock, mockMethod, args, 1, realMethod);

        assertSame(mock, invocation.getMock());
        assertSame(mockMethod, invocation.getMethod());
        assertEquals("simpleMethod", invocation.getMethodName());
        assertEquals(1, invocation.getSequenceNumber().intValue());
        assertEquals(2, invocation.getArgumentsCount());
        assertArrayEquals(args, invocation.getArguments());
        assertArrayEquals(args, invocation.getRawArguments());
        assertNotNull(invocation.getLocation());
        assertFalse(invocation.isVerified());
        assertFalse(invocation.isVerifiedInOrder());
    }

    // Tests varargs expansion when arguments contain an array at the end
    @Test
    public void testExpandVarArgs_varArgsWithArray_expandsCorrectly() {
        MockitoMethod varArgMethod = createMockitoMethod("varArgMethod", Void.TYPE, null, true);
        Object[] rawArgs = new Object[]{"prefix", new String[]{"a", "b"}};
        Invocation invocation = new Invocation(mock, varArgMethod, rawArgs, 1, realMethod);

        Object[] expandedArgs = invocation.getArguments();
        assertEquals(3, expandedArgs.length);
        assertEquals("prefix", expandedArgs[0]);
        assertEquals("a", expandedArgs[1]);
        assertEquals("b", expandedArgs[2]);
        assertArrayEquals(rawArgs, invocation.getRawArguments());
    }

    // Tests varargs expansion when null is explicitly passed as varargs
    @Test
    public void testExpandVarArgs_varArgsWithNull_expandsWithNullElement() {
        MockitoMethod varArgMethod = createMockitoMethod("varArgMethod", Void.TYPE, null, true);
        Object[] rawArgs = new Object[]{"prefix", null};
        Invocation invocation = new Invocation(mock, varArgMethod, rawArgs, 1, realMethod);

        Object[] expandedArgs = invocation.getArguments();
        assertEquals(2, expandedArgs.length);
        assertEquals("prefix", expandedArgs[0]);
        assertNull(expandedArgs[1]);
    }

    // Tests expandVarArgs when isVarArgs is false
    @Test
    public void testExpandVarArgs_nonVarArgs_returnsSameArguments() {
        Object[] rawArgs = new Object[]{"a", "b"};
        Invocation invocation = new Invocation(mock, mockMethod, rawArgs, 1, realMethod);

        assertArrayEquals(rawArgs, invocation.getArguments());
    }

    // Tests expandVarArgs with null args array for non-varargs
    @Test
    public void testExpandVarArgs_nullArgsNonVarArgs_returnsEmptyArray() {
        Invocation invocation = new Invocation(mock, mockMethod, null, 1, realMethod);

        assertNotNull(invocation.getArguments());
        assertEquals(0, invocation.getArguments().length);
    }

    // Tests equality between identical invocations
    @Test
    public void testEquals_sameInvocationDetails_returnsTrue() {
        Invocation inv1 = new Invocation(mock, mockMethod, new Object[]{"test"}, 1, realMethod);
        Invocation inv2 = new Invocation(mock, mockMethod, new Object[]{"test"}, 2, realMethod);

        assertTrue(inv1.equals(inv2));
        assertTrue(inv2.equals(inv1));
    }

    // Tests equality with null, different class, different mock, or different arguments
    @Test
    public void testEquals_differentDetails_returnsFalse() {
        Invocation inv1 = new Invocation(mock, mockMethod, new Object[]{"test"}, 1, realMethod);
        Invocation invDifferentMock = new Invocation(new Object(), mockMethod, new Object[]{"test"}, 1, realMethod);
        Invocation invDifferentArgs = new Invocation(mock, mockMethod, new Object[]{"other"}, 1, realMethod);

        assertFalse(inv1.equals(null));
        assertFalse(inv1.equals("aString"));
        assertFalse(inv1.equals(invDifferentMock));
        assertFalse(inv1.equals(invDifferentArgs));
    }

    // Tests that hashCode throws an exception as specified by implementation
    @Test(expected = RuntimeException.class)
    public void testHashCode_always_throwsRuntimeException() {
        Invocation invocation = new Invocation(mock, mockMethod, new Object[0], 1, realMethod);
        invocation.hashCode();
    }

    // Tests toString generation with arguments
    @Test
    public void testToString_standardInvocation_returnsFormattedString() {
        Invocation invocation = new Invocation(mock, mockMethod, new Object[]{"abc", 123}, 1, realMethod);
        String str = invocation.toString();

        assertNotNull(str);
        assertTrue(str.contains("simpleMethod"));
        assertTrue(str.contains("abc"));
        assertTrue(str.contains("123"));
    }

    // Tests toString with multiline setting
    @Test
    public void testToString_withPrintSettings_returnsString() {
        Invocation invocation = new Invocation(mock, mockMethod, new Object[]{"arg1", "arg2"}, 1, realMethod);
        PrintSettings settings = new PrintSettings();
        settings.setMultiline(true);

        String str = invocation.toString(settings);
        assertNotNull(str);
        assertTrue(str.contains("simpleMethod"));
    }

    // Tests argumentsToMatchers conversion with array and non-array arguments
    @Test
    public void testArgumentsToMatchers_arrayAndNonArrayArgs_createsCorrectMatchers() {
        Invocation invocation = new Invocation(mock, mockMethod, new Object[]{"text", new int[]{1, 2}}, 1, realMethod);
        List<Matcher> matchers = invocation.argumentsToMatchers();

        assertEquals(2, matchers.size());
    }

    // Tests isToString helper method
    @Test
    public void testIsToString_toStringMethod_returnsTrue() {
        MockitoMethod toStringMethod = createMockitoMethod("toString", String.class, null, false);
        Invocation invocation = new Invocation(mock, toStringMethod, new Object[0], 1, realMethod);

        assertTrue(Invocation.isToString(invocation));
    }

    // Tests isToString helper method with non-toString method
    @Test
    public void testIsToString_nonToStringMethod_returnsFalse() {
        Invocation invocation = new Invocation(mock, mockMethod, new Object[0], 1, realMethod);

        assertFalse(Invocation.isToString(invocation));
    }

    // Tests exception validation when thrown exception is declared
    @Test
    public void testIsValidException_declaredException_returnsTrue() {
        Invocation invocation = new Invocation(mock, mockMethod, new Object[0], 1, realMethod);

        assertTrue(invocation.isValidException(new IOException("error")));
    }

    // Tests exception validation when thrown exception is not declared
    @Test
    public void testIsValidException_undeclaredCheckedException_returnsFalse() {
        Invocation invocation = new Invocation(mock, mockMethod, new Object[0], 1, realMethod);

        assertFalse(invocation.isValidException(new Exception("undeclared")));
    }

    // Tests return type validation for reference types
    @Test
    public void testIsValidReturnType_compatibleAndIncompatibleReferenceTypes() {
        Invocation invocation = new Invocation(mock, mockMethod, new Object[0], 1, realMethod);

        assertTrue(invocation.isValidReturnType(String.class));
        assertFalse(invocation.isValidReturnType(Integer.class));
    }

    // Tests return type validation for primitive types
    @Test
    public void testIsValidReturnType_primitiveTypes() {
        MockitoMethod intMethod = createMockitoMethod("getInt", Integer.TYPE, null, false);
        Invocation invocation = new Invocation(mock, intMethod, new Object[0], 1, realMethod);

        assertTrue(invocation.returnsPrimitive());
        assertTrue(invocation.isValidReturnType(Integer.class));
        assertFalse(invocation.isValidReturnType(String.class));
    }

    // Tests isVoid and printMethodReturnType
    @Test
    public void testIsVoid_andPrintMethodReturnType() {
        MockitoMethod voidMethod = createMockitoMethod("doNothing", Void.TYPE, null, false);
        Invocation voidInvocation = new Invocation(mock, voidMethod, new Object[0], 1, realMethod);

        assertTrue(voidInvocation.isVoid());
        assertEquals("void", voidInvocation.printMethodReturnType());

        Invocation nonVoidInvocation = new Invocation(mock, mockMethod, new Object[0], 1, realMethod);
        assertFalse(nonVoidInvocation.isVoid());
        assertEquals("String", nonVoidInvocation.printMethodReturnType());
    }

    // Tests verification state transitions
    @Test
    public void testMarkVerified_andMarkVerifiedInOrder() {
        Invocation invocation = new Invocation(mock, mockMethod, new Object[0], 1, realMethod);

        assertFalse(invocation.isVerified());
        assertFalse(invocation.isVerifiedInOrder());

        invocation.markVerified();
        assertTrue(invocation.isVerified());
        assertFalse(invocation.isVerifiedInOrder());

        invocation.markVerifiedInOrder();
        assertTrue(invocation.isVerified());
        assertTrue(invocation.isVerifiedInOrder());
    }

    // Tests callRealMethod invocation
    @Test
    public void testCallRealMethod_delegatesToRealMethod() throws Throwable {
        Invocation invocation = new Invocation(mock, mockMethod, new Object[]{"test"}, 1, realMethod);
        Object result = invocation.callRealMethod();

        assertEquals("realResult", result);
    }
}