package org.mockito.internal.stubbing.answers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.invocation.InvocationOnMock;

import static org.junit.Assert.*;

public class CallsRealMethodsTest {

    private CallsRealMethods callsRealMethods;

    @Before
    public void setUp() {
        callsRealMethods = new CallsRealMethods();
    }

    // Tests normal case where callRealMethod returns an object
    @Test
    public void testAnswer_invocationReturnsValue_returnsSameValue() throws Throwable {
        final Object expectedResult = "realResult";
        InvocationOnMock invocation = new DummyInvocation() {
            @Override
            public Object callRealMethod() throws Throwable {
                return expectedResult;
            }
        };

        Object result = callsRealMethods.answer(invocation);
        assertEquals(expectedResult, result);
    }

    // Tests normal case where callRealMethod returns null
    @Test
    public void testAnswer_invocationReturnsNull_returnsNull() throws Throwable {
        InvocationOnMock invocation = new DummyInvocation() {
            @Override
            public Object callRealMethod() throws Throwable {
                return null;
            }
        };

        Object result = callsRealMethods.answer(invocation);
        assertNull(result);
    }

    // Tests normal case where callRealMethod returns integer primitive wrapper
    @Test
    public void testAnswer_invocationReturnsInteger_returnsInteger() throws Throwable {
        final Integer expectedResult = 42;
        InvocationOnMock invocation = new DummyInvocation() {
            @Override
            public Object callRealMethod() throws Throwable {
                return expectedResult;
            }
        };

        Object result = callsRealMethods.answer(invocation);
        assertEquals(expectedResult, result);
    }

    // Tests exception path when callRealMethod throws a checked exception
    @Test(expected = Exception.class)
    public void testAnswer_invocationThrowsCheckedException_propagatesException() throws Throwable {
        InvocationOnMock invocation = new DummyInvocation() {
            @Override
            public Object callRealMethod() throws Throwable {
                throw new Exception("Checked exception from real method");
            }
        };

        callsRealMethods.answer(invocation);
    }

    // Tests exception path when callRealMethod throws a runtime exception
    @Test(expected = RuntimeException.class)
    public void testAnswer_invocationThrowsRuntimeException_propagatesException() throws Throwable {
        InvocationOnMock invocation = new DummyInvocation() {
            @Override
            public Object callRealMethod() throws Throwable {
                throw new RuntimeException("Runtime exception from real method");
            }
        };

        callsRealMethods.answer(invocation);
    }

    // Tests exception path when callRealMethod throws an Error
    @Test(expected = AssertionError.class)
    public void testAnswer_invocationThrowsError_propagatesError() throws Throwable {
        InvocationOnMock invocation = new DummyInvocation() {
            @Override
            public Object callRealMethod() throws Throwable {
                throw new AssertionError("Assertion error from real method");
            }
        };

        callsRealMethods.answer(invocation);
    }

    // Tests invalid null invocation input
    @Test(expected = NullPointerException.class)
    public void testAnswer_nullInvocation_throwsNullPointerException() throws Throwable {
        callsRealMethods.answer(null);
    }

    // Tests serialization and deserialization of CallsRealMethods
    @Test
    public void testSerialization_serializedAndDeserialized_behavesCorrectly() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(callsRealMethods);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof CallsRealMethods);

        CallsRealMethods deserializedAnswer = (CallsRealMethods) deserialized;
        InvocationOnMock invocation = new DummyInvocation() {
            @Override
            public Object callRealMethod() throws Throwable {
                return "deserializedOk";
            }
        };

        try {
            assertEquals("deserializedOk", deserializedAnswer.answer(invocation));
        } catch (Throwable t) {
            fail("Should not throw exception: " + t.getMessage());
        }
    }

    // Tests exception path when method is abstract
    @Test(expected = MockitoException.class)
    public void testAnswer_abstractMethod_throwsCannotCallAbstractRealMethodException() throws Throwable {
        final Method abstractMethod = DummyInterface.class.getMethod("abstractMethod");
        InvocationOnMock invocation = new DummyInvocation() {
            @Override
            public Method getMethod() {
                return abstractMethod;
            }

            @Override
            public Object callRealMethod() throws Throwable {
                return "shouldNotBeCalled";
            }
        };

        callsRealMethods.answer(invocation);
    }

    // Tests normal case with explicitly provided concrete method
    @Test
    public void testAnswer_concreteMethod_callsRealMethod() throws Throwable {
        final Method concreteMethod = DummyClass.class.getMethod("concreteMethod");
        InvocationOnMock invocation = new DummyInvocation() {
            @Override
            public Method getMethod() {
                return concreteMethod;
            }

            @Override
            public Object callRealMethod() throws Throwable {
                return "concreteResult";
            }
        };

        Object result = callsRealMethods.answer(invocation);
        assertEquals("concreteResult", result);
    }

    private interface DummyInterface {
        void abstractMethod();
    }

    private static class DummyClass {
        public void concreteMethod() {}
    }

    private static class DummyInvocation implements InvocationOnMock {
        public Object getMock() {
            return null;
        }

        public Method getMethod() {
            try {
                return DummyClass.class.getMethod("concreteMethod");
            } catch (NoSuchMethodException e) {
                return null;
            }
        }

        public Object[] getArguments() {
            return new Object[0];
        }

        public Object callRealMethod() throws Throwable {
            return null;
        }

        public <T> T getArgumentAt(int index, Class<T> clazz) {
            return null;
        }
    }
}