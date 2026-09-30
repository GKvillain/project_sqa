package org.mockito.internal.stubbing.answers;

import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.invocation.InvocationOnMock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class CallsRealMethodsTest {

    // Tests normal case where real method returns a valid string object
    @Test
    public void testAnswer_validReturnValue_returnsExpectedObject() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        InvocationOnMock invocation = new DummyInvocation("expectedResult", null);

        Object result = answer.answer(invocation);

        assertEquals("expectedResult", result);
    }

    // Tests normal case where real method returns null
    @Test
    public void testAnswer_nullReturnValue_returnsNull() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        InvocationOnMock invocation = new DummyInvocation(null, null);

        Object result = answer.answer(invocation);

        assertNull(result);
    }

    // Tests normal case where real method returns primitive wrapper / number
    @Test
    public void testAnswer_numericReturnValue_returnsCorrectNumber() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        InvocationOnMock invocation = new DummyInvocation(42, null);

        Object result = answer.answer(invocation);

        assertEquals(42, result);
    }

    // Tests edge case where real method returns boolean value true
    @Test
    public void testAnswer_booleanReturnValue_returnsTrue() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        InvocationOnMock invocation = new DummyInvocation(Boolean.TRUE, null);

        Object result = answer.answer(invocation);

        assertEquals(Boolean.TRUE, result);
    }

    // Tests exception path where real method throws a RuntimeException
    @Test(expected = RuntimeException.class)
    public void testAnswer_realMethodThrowsRuntimeException_propagatesRuntimeException() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        InvocationOnMock invocation = new DummyInvocation(null, new RuntimeException("runtime error"));

        answer.answer(invocation);
    }

    // Tests exception path where real method throws a checked Exception
    @Test(expected = Exception.class)
    public void testAnswer_realMethodThrowsCheckedException_propagatesException() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        InvocationOnMock invocation = new DummyInvocation(null, new Exception("checked exception"));

        answer.answer(invocation);
    }

    // Tests exception path where real method throws an Error
    @Test(expected = Error.class)
    public void testAnswer_realMethodThrowsError_propagatesError() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        InvocationOnMock invocation = new DummyInvocation(null, new Error("fatal error"));

        answer.answer(invocation);
    }

    // Tests edge case where invocation parameter is null
    @Test(expected = NullPointerException.class)
    public void testAnswer_nullInvocation_throwsNullPointerException() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        answer.answer(null);
    }

    // Tests serialization and deserialization of CallsRealMethods instance
    @Test
    public void testSerialization_deserializedObject_answersCorrectly() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(answer);
        oos.flush();
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        CallsRealMethods deserializedAnswer = (CallsRealMethods) ois.readObject();
        ois.close();

        assertNotNull(deserializedAnswer);
        InvocationOnMock invocation = new DummyInvocation("deserializedResult", null);
        assertEquals("deserializedResult", deserializedAnswer.answer(invocation));
    }

    // Tests validateFor when invocation method is concrete (non-abstract)
    @Test
    public void testValidateFor_concreteMethod_doesNotThrow() throws Exception {
        CallsRealMethods answer = new CallsRealMethods();
        Method concreteMethod = SampleClass.class.getMethod("concreteMethod");
        InvocationOnMock invocation = new DummyInvocation(new SampleClass(), concreteMethod, null, null);

        answer.validateFor(invocation);
    }

    // Tests validateFor when invocation method is abstract
    @Test(expected = MockitoException.class)
    public void testValidateFor_abstractMethod_throwsMockitoException() throws Exception {
        CallsRealMethods answer = new CallsRealMethods();
        Method abstractMethod = SampleInterface.class.getMethod("abstractMethod");
        InvocationOnMock invocation = new DummyInvocation(null, abstractMethod, null, null);

        answer.validateFor(invocation);
    }

    private interface SampleInterface {
        void abstractMethod();
    }

    private static class SampleClass {
        public void concreteMethod() {}
    }

    private static class DummyInvocation implements InvocationOnMock {
        private final Object mock;
        private final Method method;
        private final Object returnValue;
        private final Throwable throwableToThrow;

        public DummyInvocation(Object returnValue, Throwable throwableToThrow) {
            this(null, null, returnValue, throwableToThrow);
        }

        public DummyInvocation(Object mock, Method method, Object returnValue, Throwable throwableToThrow) {
            this.mock = mock;
            this.method = method;
            this.returnValue = returnValue;
            this.throwableToThrow = throwableToThrow;
        }

        public Object getMock() {
            return mock;
        }

        public Method getMethod() {
            return method;
        }

        public Object[] getArguments() {
            return new Object[0];
        }

        public Object callRealMethod() throws Throwable {
            if (throwableToThrow != null) {
                throw throwableToThrow;
            }
            return returnValue;
        }

        @SuppressWarnings("unchecked")
        public <T> T getArgument(int index) {
            return (T) getArguments()[index];
        }

        public <T> T getArgument(int index, Class<T> clazz) {
            return clazz.cast(getArgument(index));
        }
    }
}