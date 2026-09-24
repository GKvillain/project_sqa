package org.mockito.internal.stubbing.defaultanswers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.GenericMetadataSupport;
import org.mockito.invocation.InvocationOnMock;

public class ReturnsDeepStubsTest {

    private ReturnsDeepStubs deepStubsAnswer;

    @Before
    public void setUp() {
        deepStubsAnswer = new ReturnsDeepStubs();
    }

    // Tests non-mockable primitive return type returns empty/default value
    @Test
    public void testAnswer_primitiveReturnType_returnsDefaultPrimitive() {
        Person person = mock(Person.class, deepStubsAnswer);
        assertEquals(0, person.getAge());
    }

    // Tests non-mockable final/String return type returns default empty value
    @Test
    public void testAnswer_finalReturnType_returnsDefaultEmptyValue() {
        Person person = mock(Person.class, deepStubsAnswer);
        assertEquals("", person.getName());
    }

    // Tests mockable object return type returns a deep stub mock
    @Test
    public void testAnswer_mockableReturnType_returnsMockObject() {
        Person person = mock(Person.class, deepStubsAnswer);
        Address address = person.getAddress();
        assertNotNull(address);
    }

    // Tests chained deep stub invocations across multiple levels
    @Test
    public void testAnswer_nestedDeepStubs_returnsChainedMocks() {
        Person person = mock(Person.class, deepStubsAnswer);
        Street street = person.getAddress().getStreet();
        assertNotNull(street);
        assertEquals("", street.getName());
    }

    // Tests repeated invocation on same method returns the same mock instance
    @Test
    public void testAnswer_repeatedInvocation_returnsCachedMock() {
        Person person = mock(Person.class, deepStubsAnswer);
        Address firstCall = person.getAddress();
        Address secondCall = person.getAddress();
        assertSame(firstCall, secondCall);
    }

    // Tests explicit stubbing overrides deep stub answer
    @Test
    public void testAnswer_explicitStubbing_overridesDeepStub() {
        Person person = mock(Person.class, deepStubsAnswer);
        Address customAddress = mock(Address.class);
        when(person.getAddress()).thenReturn(customAddress);

        assertSame(customAddress, person.getAddress());
    }

    // Tests generic return type resolution
    @Test
    public void testAnswer_genericReturnType_resolvesMockableGenericType() {
        Container container = mock(Container.class, deepStubsAnswer);
        Address address = container.getElement();
        assertNotNull(address);
    }

    // Tests nested generics with type bounds and extra interfaces
    @Test
    public void testAnswer_nestedGenericsWithBounds_createsMockSuccessfully() {
        GenericsNest<?> nest = mock(GenericsNest.class, deepStubsAnswer);
        Map<?, Set<Number>> map = nest.getMap();
        assertNotNull(map);
    }

    // Tests serialization of ReturnsDeepStubs answer instance
    @Test
    public void testSerialization_returnsDeepStubsInstance_isSerializable() throws Exception {
        ReturnsDeepStubs deserializedAnswer = serializeAndDeserialize(deepStubsAnswer);
        assertNotNull(deserializedAnswer);
    }

    // Tests serialization of a mock created with serializable settings and deep stubs
    @Test
    public void testSerialization_serializableMockWithDeepStubs_canBeSerialized() throws Exception {
        Person serializablePerson = mock(Person.class, withSettings().defaultAnswer(deepStubsAnswer).serializable());
        Address address = serializablePerson.getAddress();
        assertNotNull(address);

        Person deserializedPerson = serializeAndDeserialize(serializablePerson);
        assertNotNull(deserializedPerson);
        assertNotNull(deserializedPerson.getAddress());
    }

    // Tests actualParameterizedType returns correct metadata support
    @Test
    public void testActualParameterizedType_returnsGenericMetadataSupport() {
        Person person = mock(Person.class, deepStubsAnswer);
        GenericMetadataSupport metadata = deepStubsAnswer.actualParameterizedType(person);
        assertNotNull(metadata);
        assertEquals(Person.class, metadata.rawType());
    }

    // Helper method for serialization testing
    private <T> T serializeAndDeserialize(T object) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        @SuppressWarnings("unchecked")
        T result = (T) ois.readObject();
        ois.close();
        return result;
    }

    // Interfaces used for deep stub testing
    interface Person extends Serializable {
        Address getAddress();
        String getName();
        int getAge();
    }

    interface Address extends Serializable {
        Street getStreet();
        String getCity();
    }

    interface Street extends Serializable {
        String getName();
    }

    interface Container {
        Address getElement();
    }

    interface GenericsNest<K extends Comparable<K> & Cloneable> {
        Map<K, Set<Number>> getMap();
    }
}