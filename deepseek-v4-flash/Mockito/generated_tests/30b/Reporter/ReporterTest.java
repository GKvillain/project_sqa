package org.mockito.exceptions;

import org.junit.Test;
import static org.junit.Assert.*;

import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import org.mockito.exceptions.misusing.MissingMethodInvocationException;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.exceptions.misusing.NullInsteadOfMockException;
import org.mockito.exceptions.misusing.UnfinishedStubbingException;
import org.mockito.exceptions.misusing.UnfinishedVerificationException;
import org.mockito.exceptions.verification.ArgumentsAreDifferent;
import org.mockito.internal.debugging.Location;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReporterTest {

    private final Reporter reporter = new Reporter();

    // Tests checked exception with a throwable
    @Test(expected = MockitoException.class)
    public void testCheckedExceptionInvalid_throwable_throwsMockitoException() {
        reporter.checkedExceptionInvalid(new RuntimeException());
    }

    // Tests cannot stub with null throwable
    @Test(expected = MockitoException.class)
    public void testCannotStubWithNullThrowable_void_throwsMockitoException() {
        reporter.cannotStubWithNullThrowable();
    }

    // Tests missing method invocation
    @Test(expected = MissingMethodInvocationException.class)
    public void testMissingMethodInvocation_void_throwsMissingMethodInvocationException() {
        reporter.missingMethodInvocation();
    }

    // Tests unfinished stubbing with location
    @Test(expected = UnfinishedStubbingException.class)
    public void testUnfinishedStubbing_location_throwsUnfinishedStubbingException() {
        reporter.unfinishedStubbing(new Location());
    }

    // Tests unfinished verification with location
    @Test(expected = UnfinishedVerificationException.class)
    public void testUnfinishedVerificationException_location_throwsUnfinishedVerificationException() {
        reporter.unfinishedVerificationException(new Location());
    }

    // Tests not a mock passed to verify
    @Test(expected = NotAMockException.class)
    public void testNotAMockPassedToVerify_class_throwsNotAMockException() {
        reporter.notAMockPassedToVerify(String.class);
    }

    // Tests null passed to verify
    @Test(expected = NullInsteadOfMockException.class)
    public void testNullPassedToVerify_void_throwsNullInsteadOfMockException() {
        reporter.nullPassedToVerify();
    }

    // Tests not a mock passed to when()
    @Test(expected = NotAMockException.class)
    public void testNotAMockPassedToWhenMethod_void_throwsNotAMockException() {
        reporter.notAMockPassedToWhenMethod();
    }

    // Tests null passed to when()
    @Test(expected = NullInsteadOfMockException.class)
    public void testNullPassedToWhenMethod_void_throwsNullInsteadOfMockException() {
        reporter.nullPassedToWhenMethod();
    }

    // Tests mocks have to be passed to verifyNoMoreInteractions
    @Test(expected = MockitoException.class)
    public void testMocksHaveToBePassedToVerifyNoMoreInteractions_void_throwsMockitoException() {
        reporter.mocksHaveToBePassedToVerifyNoMoreInteractions();
    }

    // Tests not a mock passed to verifyNoMoreInteractions
    @Test(expected = NotAMockException.class)
    public void testNotAMockPassedToVerifyNoMoreInteractions_void_throwsNotAMockException() {
        reporter.notAMockPassedToVerifyNoMoreInteractions();
    }

    // Tests null passed to verifyNoMoreInteractions
    @Test(expected = NullInsteadOfMockException.class)
    public void testNullPassedToVerifyNoMoreInteractions_void_throwsNullInsteadOfMockException() {
        reporter.nullPassedToVerifyNoMoreInteractions();
    }

    // Tests not a mock passed when creating InOrder
    @Test(expected = NotAMockException.class)
    public void testNotAMockPassedWhenCreatingInOrder_void_throwsNotAMockException() {
        reporter.notAMockPassedWhenCreatingInOrder();
    }

    // Tests null passed when creating InOrder
    @Test(expected = NullInsteadOfMockException.class)
    public void testNullPassedWhenCreatingInOrder_void_throwsNullInsteadOfMockException() {
        reporter.nullPassedWhenCreatingInOrder();
    }

    // Tests mocks have to be passed when creating InOrder
    @Test(expected = MockitoException.class)
    public void testMocksHaveToBePassedWhenCreatingInOrder_void_throwsMockitoException() {
        reporter.mocksHaveToBePassedWhenCreatingInOrder();
    }

    // Tests InOrder requires familiar mock
    @Test(expected = MockitoException.class)
    public void testInOrderRequiresFamiliarMock_void_throwsMockitoException() {
        reporter.inOrderRequiresFamiliarMock();
    }

    // Tests invalid use of matchers with specific counts
    @Test(expected = InvalidUseOfMatchersException.class)
    public void testInvalidUseOfMatchers_counts_throwsInvalidUseOfMatchersException() {
        reporter.invalidUseOfMatchers(2, 3);
    }

    // Tests arguments are different - verifies exception type and message
    @Test
    public void testArgumentsAreDifferent_values_throwsArgumentsAreDifferent() {
        try {
            reporter.argumentsAreDifferent("wanted", "actual", new Location());
            fail("Expected ArgumentsAreDifferent exception");
        } catch (ArgumentsAreDifferent e) {
            assertTrue("Exception message should contain 'Argument(s) are different!'",
                    e.getMessage().contains("Argument(s) are different!"));
        }
    }

    // Tests cannot mock final class
    @Test(expected = MockitoException.class)
    public void testCannotMockFinalClass_class_throwsMockitoException() {
        reporter.cannotMockFinalClass(String.class);
    }

    // Tests cannot stub void method with a return value
    @Test(expected = MockitoException.class)
    public void testCannotStubVoidMethodWithAReturnValue_methodName_throwsMockitoException() {
        reporter.cannotStubVoidMethodWithAReturnValue("someMethod");
    }

    // Dynamically covers the remaining uncovered Reporter methods without duplicating the methods above.
    @Test
    public void testUncoveredReporterMethods() throws Exception {
        Set<String> coveredSignatures = new HashSet<String>(Arrays.asList(
                "checkedExceptionInvalid(java.lang.Throwable)",
                "cannotStubWithNullThrowable()",
                "missingMethodInvocation()",
                "unfinishedStubbing(org.mockito.internal.debugging.Location)",
                "unfinishedVerificationException(org.mockito.internal.debugging.Location)",
                "notAMockPassedToVerify(java.lang.Class)",
                "nullPassedToVerify()",
                "notAMockPassedToWhenMethod()",
                "nullPassedToWhenMethod()",
                "mocksHaveToBePassedToVerifyNoMoreInteractions()",
                "notAMockPassedToVerifyNoMoreInteractions()",
                "nullPassedToVerifyNoMoreInteractions()",
                "notAMockPassedWhenCreatingInOrder()",
                "nullPassedWhenCreatingInOrder()",
                "mocksHaveToBePassedWhenCreatingInOrder()",
                "inOrderRequiresFamiliarMock()",
                "invalidUseOfMatchers(int,int)",
                "argumentsAreDifferent(java.lang.String,java.lang.String,org.mockito.internal.debugging.Location)",
                "cannotMockFinalClass(java.lang.Class)",
                "cannotStubVoidMethodWithAReturnValue(java.lang.String)"
        ));

        for (Method method : Reporter.class.getDeclaredMethods()) {
            if (Modifier.isPrivate(method.getModifiers()) || method.isSynthetic()) {
                continue;
            }
            if (coveredSignatures.contains(signatureOf(method))) {
                continue;
            }

            method.setAccessible(true);

            // First call with defaults/empty collections.
            invokeAndExpectReporterException(method, defaultArgs(method));

            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                // Exercise collection-handling branches: empty vs non-empty.
                if (Collection.class.isAssignableFrom(parameterTypes[i]) || Iterable.class.isAssignableFrom(parameterTypes[i])) {
                    Object[] args = defaultArgs(method);
                    args[i] = nonEmptyCollectionFor(method, i);
                    invokeAndExpectReporterException(method, args);
                }

                // Exercise Class-typed arguments with primitive/final/plain/anonymous classes.
                if (parameterTypes[i] == Class.class) {
                    for (Class<?> clazz : new Class<?>[]{int.class, Object.class, String.class, anonymousClass()}) {
                        Object[] args = defaultArgs(method);
                        args[i] = clazz;
                        invokeAndExpectReporterException(method, args);
                    }
                }

                // Exercise primitive/int/bool/long branches with non-default values.
                if (parameterTypes[i] == int.class) {
                    Object[] args = defaultArgs(method);
                    args[i] = 1;
                    invokeAndExpectReporterException(method, args);
                }
                if (parameterTypes[i] == long.class) {
                    Object[] args = defaultArgs(method);
                    args[i] = 1L;
                    invokeAndExpectReporterException(method, args);
                }
                if (parameterTypes[i] == boolean.class) {
                    Object[] args = defaultArgs(method);
                    args[i] = true;
                    invokeAndExpectReporterException(method, args);
                }
                if (parameterTypes[i] == String.class) {
                    Object[] args = defaultArgs(method);
                    args[i] = "different";
                    invokeAndExpectReporterException(method, args);
                }
            }
        }
    }

    private void invokeAndExpectReporterException(Method method, Object[] args) throws Exception {
        try {
            method.invoke(reporter, args);
            fail("Expected Reporter method to throw: " + method.getName());
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            assertTrue("Unexpected exception from " + method.getName() + ": " + cause,
                    cause instanceof MockitoException);
        }
    }

    private Object[] defaultArgs(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Object[] args = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            args[i] = argumentFor(parameterTypes[i], genericParameterTypes.length > i ? genericParameterTypes[i] : null);
        }
        return args;
    }

    private Object argumentFor(Class<?> type, Type genericType) {
        if (!type.isPrimitive()) {
            if (type == String.class) return "value";
            if (type == Object.class) return new Object();
            if (type == Class.class) return String.class;
            if (type == Location.class) return new Location();
            if (Throwable.class.isAssignableFrom(type)) {
                if (type == Throwable.class || type == Exception.class) return new RuntimeException();
                try {
                    return type.newInstance();
                } catch (Exception e) {
                    return new RuntimeException();
                }
            }
            if (List.class.isAssignableFrom(type)) return Collections.emptyList();
            if (Set.class.isAssignableFrom(type)) return Collections.emptySet();
            if (Collection.class.isAssignableFrom(type)) return Collections.emptyList();
            if (Iterable.class.isAssignableFrom(type)) return Collections.emptyList();
            if (Map.class.isAssignableFrom(type)) return Collections.emptyMap();
            if (type == Integer.class) return 0;
            if (type == Long.class) return 0L;
            if (type == Boolean.class) return false;
            if (type == Character.class) return '\0';
            if (type == Byte.class) return (byte) 0;
            if (type == Short.class) return (short) 0;
            if (type == Float.class) return 0f;
            if (type == Double.class) return 0d;
            if (type == StackTraceElement.class) return new StackTraceElement("Class", "method", "file", 1);
            if (type.isArray()) return Array.newInstance(type.getComponentType(), 0);
            return Mockito.mock(type);
        }

        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == char.class) return '\0';
        if (type == float.class) return 0f;
        if (type == double.class) return 0d;
        return null;
    }

    private Object nonEmptyCollectionFor(Method method, int index) {
        Class<?> parameterType = method.getParameterTypes()[index];
        Type genericParameterType = method.getGenericParameterTypes()[index];
        Class<?> elementType = Object.class;

        if (genericParameterType instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) genericParameterType;
            Type[] actualTypeArguments = parameterized.getActualTypeArguments();
            if (actualTypeArguments.length > 0) {
                elementType = rawClass(actualTypeArguments[0]);
            }
        }

        Object element = argumentFor(elementType, null);
        if (element == null) {
            element = Mockito.mock(elementType);
        }

        if (List.class.isAssignableFrom(parameterType)) {
            return Collections.singletonList(element);
        }
        if (Set.class.isAssignableFrom(parameterType)) {
            return Collections.singleton(element);
        }
        return Collections.singletonList(element);
    }

    private Class<?> rawClass(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            return rawClass(((ParameterizedType) type).getRawType());
        }
        if (type instanceof WildcardType) {
            Type[] upperBounds = ((WildcardType) type).getUpperBounds();
            if (upperBounds.length > 0) {
                return rawClass(upperBounds[0]);
            }
        }
        if (type instanceof GenericArrayType) {
            return rawClass(((GenericArrayType) type).getGenericComponentType());
        }
        if (type instanceof TypeVariable<?>) {
            Type[] bounds = ((TypeVariable<?>) type).getBounds();
            if (bounds.length > 0) {
                return rawClass(bounds[0]);
            }
        }
        return Object.class;
    }

    private String signatureOf(Method method) {
        StringBuilder sb = new StringBuilder(method.getName()).append("(");
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(parameterTypes[i].getName());
        }
        return sb.append(")").toString();
    }

    private static Class<?> anonymousClass() {
        return new Object() { }.getClass();
    }
}