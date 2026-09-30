package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ReturnsDeepStubsTest {

    private ReturnsDeepStubs returnsDeepStubs;

    @Before
    public void setUp() {
        returnsDeepStubs = new ReturnsDeepStubs();
    }

    // Tests non-mockable primitive return type returns default primitive value
    @Test
    public void testAnswer_primitiveReturnType_returnsDefaultValue() {
        SampleService mock = mock(SampleService.class, returnsDeepStubs);
        int result = mock.getIntValue();
        assertEquals(0, result);
    }

    // Tests non-mockable String/final type returns empty string
    @Test
    public void testAnswer_finalReturnType_returnsEmptyValue() {
        SampleService mock = mock(SampleService.class, returnsDeepStubs);
        String result = mock.getStringValue();
        assertEquals("", result);
    }

    // Tests basic 1-level deep stub returns a mock object
    @Test
    public void testAnswer_singleLevelDeepStub_returnsMock() {
        SampleService mock = mock(SampleService.class, returnsDeepStubs);
        FirstLevel firstLevel = mock.getFirstLevel();
        assertNotNull(firstLevel);
    }

    // Tests consecutive invocations return the same deep stubbed mock instance
    @Test
    public void testAnswer_consecutiveCalls_returnsSameMockInstance() {
        SampleService mock = mock(SampleService.class, returnsDeepStubs);
        FirstLevel first = mock.getFirstLevel();
        FirstLevel second = mock.getFirstLevel();
        assertSame(first, second);
    }

    // Tests multi-level deep stubbing chain
    @Test
    public void testAnswer_multiLevelDeepStub_returnsNestedMock() {
        SampleService mock = mock(SampleService.class, returnsDeepStubs);
        SecondLevel secondLevel = mock.getFirstLevel().getSecondLevel();
        assertNotNull(secondLevel);
    }

    // Tests overriding deep stub with explicit when-thenReturn stubbing
    @Test
    public void testAnswer_explicitStubbing_returnsConfiguredValue() {
        SampleService mock = mock(SampleService.class, returnsDeepStubs);
        when(mock.getFirstLevel().getSecondLevel().getName()).thenReturn("custom-name");

        assertEquals("custom-name", mock.getFirstLevel().getSecondLevel().getName());
    }

    // Tests generic return type resolution for parameterized interfaces
    @Test
    public void testAnswer_genericReturnType_resolvesTypeCorrectly() {
        GenericContainer<FirstLevel> mock = mock(GenericContainer.class, returnsDeepStubs);
        FirstLevel item = mock.getItem();
        assertNotNull(item);
        SecondLevel second = item.getSecondLevel();
        assertNotNull(second);
    }

    // Tests deep stubbing on generic nested interface with multiple type bounds
    @Test
    public void testAnswer_multipleGenericBounds_createsMockWithExtraInterfaces() {
        GenericsNest<?> mock = mock(GenericsNest.class, returnsDeepStubs);
        assertNotNull(mock.entrySet());
        assertNotNull(mock.entrySet().iterator());
        assertNotNull(mock.entrySet().iterator().next());
        assertNotNull(mock.entrySet().iterator().next().getValue());
    }

    // Tests serialization behavior of mock created with ReturnsDeepStubs
    @Test
    public void testAnswer_serializationOfDeepStubMock_deserializesSuccessfully() throws Exception {
        SerializableService mock = mock(SerializableService.class, withSettings().serializable().defaultAnswer(returnsDeepStubs));
        FirstLevel firstLevel = mock.getFirstLevel();
        assertNotNull(firstLevel);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(mock);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        SerializableService deserialized = (SerializableService) ois.readObject();
        assertNotNull(deserialized);
    }

    // Tests deep stubbing when returned class has no default no-arg constructor (Defects4J Bug 10 regression)
    @Test
    public void testAnswer_classWithoutNoArgConstructor_createsDeepStubSuccessfully() {
        ContainerOfNonSerializable mock = mock(ContainerOfNonSerializable.class, returnsDeepStubs);
        ClassWithoutNoArgConstructor deepStub = mock.getCustomClass();
        assertNotNull(deepStub);
    }

    // Tests direct answer invocation on custom mock
    @Test
    public void testAnswer_directInvocationOnMock_returnsExpectedAnswer() throws Throwable {
        SampleService mock = mock(SampleService.class, returnsDeepStubs);
        InvocationOnMock invocation = createInvocation(mock, "getFirstLevel");
        Object result = returnsDeepStubs.answer(invocation);
        assertNotNull(result);
        assertTrue(result instanceof FirstLevel);
    }

    // Tests serialization of ReturnsDeepStubs answer itself
    @Test
    public void testAnswer_serializationOfReturnsDeepStubsInstance() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(returnsDeepStubs);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        Object deserialized = ois.readObject();
        assertNotNull(deserialized);
        assertTrue(deserialized instanceof ReturnsDeepStubs);
    }

    // Tests different arguments yield different deep-stubbed instances
    @Test
    public void testAnswer_differentArguments_yieldDifferentInstances() {
        SampleServiceWithArgs mock = mock(SampleServiceWithArgs.class, returnsDeepStubs);
        FirstLevel first = mock.getFirstLevelWithArg("arg1");
        FirstLevel second = mock.getFirstLevelWithArg("arg2");
        assertNotNull(first);
        assertNotNull(second);
        assertNotSame(first, second);

        FirstLevel firstAgain = mock.getFirstLevelWithArg("arg1");
        assertSame(first, firstAgain);
    }

    // Tests Mockito.RETURNS_DEEP_STUBS constant usage
    @Test
    public void testAnswer_usingMockitoConstant() {
        SampleService mock = mock(SampleService.class, Mockito.RETURNS_DEEP_STUBS);
        assertNotNull(mock.getFirstLevel().getSecondLevel());
    }

    // Tests deep stubbing on generic method returning generic list
    @Test
    public void testAnswer_genericListReturnType_returnsEmptyListByDefault() {
        GenericContainer<FirstLevel> mock = mock(GenericContainer.class, returnsDeepStubs);
        List<FirstLevel> list = mock.getList();
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    // Tests deep stubbing with an abstract class return type
    @Test
    public void testAnswer_abstractClassReturnType_returnsMock() {
        AbstractClassContainer mock = mock(AbstractClassContainer.class, returnsDeepStubs);
        AbstractService result = mock.getAbstractService();
        assertNotNull(result);
        assertNotNull(result.getFirstLevel());
    }

    private InvocationOnMock createInvocation(final Object mock, String methodName) throws NoSuchMethodException {
        final java.lang.reflect.Method method = mock.getClass().getMethod(methodName);
        return new InvocationOnMock() {
            public Object getMock() {
                return mock;
            }

            public java.lang.reflect.Method getMethod() {
                return method;
            }

            public Object[] getArguments() {
                return new Object[0];
            }

            public Object callRealMethod() throws Throwable {
                return null;
            }
        };
    }

    // Domain test interfaces and classes
    interface SampleService {
        FirstLevel getFirstLevel();
        String getStringValue();
        int getIntValue();
    }

    interface SampleServiceWithArgs {
        FirstLevel getFirstLevelWithArg(String arg);
    }

    interface FirstLevel {
        SecondLevel getSecondLevel();
        String getName();
    }

    interface SecondLevel {
        String getName();
    }

    interface SerializableService extends Serializable {
        FirstLevel getFirstLevel();
    }

    interface GenericContainer<T> {
        T getItem();
        List<T> getList();
    }

    interface GenericsNest<K extends Comparable<K> & Cloneable> extends Map<K, Set<Number>> {
    }

    static class ClassWithoutNoArgConstructor {
        private final String value;

        public ClassWithoutNoArgConstructor(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    interface ContainerOfNonSerializable {
        ClassWithoutNoArgConstructor getCustomClass();
    }

    static abstract class AbstractService {
        public abstract FirstLevel getFirstLevel();
    }

    interface AbstractClassContainer {
        AbstractService getAbstractService();
    }
}