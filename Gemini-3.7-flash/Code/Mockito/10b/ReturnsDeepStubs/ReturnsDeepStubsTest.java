package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.GenericMetadataSupport;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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

    // Tests non-mockable primitive return type returns default value (0)
    @Test
    public void testAnswer_primitiveIntReturnType_returnsZero() {
        Person person = mock(Person.class, returnsDeepStubs);
        assertEquals(0, person.getAge());
    }

    // Tests non-mockable primitive boolean return type returns false
    @Test
    public void testAnswer_primitiveBooleanReturnType_returnsFalse() {
        Person person = mock(Person.class, returnsDeepStubs);
        assertFalse(person.isActive());
    }

    // Tests non-mockable final String return type returns empty/null default value
    @Test
    public void testAnswer_finalStringReturnType_returnsNull() {
        Person person = mock(Person.class, returnsDeepStubs);
        assertNull(person.getName());
    }

    // Tests mockable interface return type returns non-null deep stub mock
    @Test
    public void testAnswer_mockableInterfaceReturnType_returnsDeepMock() {
        Person person = mock(Person.class, returnsDeepStubs);
        Address address = person.getAddress();
        assertNotNull(address);
    }

    // Tests repeated invocation on same method returns the exact same mock instance
    @Test
    public void testAnswer_repeatedInvocation_returnsSameMockInstance() {
        Person person = mock(Person.class, returnsDeepStubs);
        Address address1 = person.getAddress();
        Address address2 = person.getAddress();
        assertSame(address1, address2);
    }

    // Tests nested chained invocations create deep stubs across levels
    @Test
    public void testAnswer_nestedChainedInvocations_returnsDeepStubsAcrossLevels() {
        Person person = mock(Person.class, returnsDeepStubs);
        City city = person.getAddress().getCity();
        assertNotNull(city);
        assertNull(city.getName());
    }

    // Tests manual stubbing overrides default deep stub answer
    @Test
    public void testAnswer_manuallyStubbedInvocation_returnsStubbedValue() {
        Person person = mock(Person.class, returnsDeepStubs);
        City customCity = mock(City.class);
        when(customCity.getName()).thenReturn("Bangkok");
        when(person.getAddress().getCity()).thenReturn(customCity);

        assertEquals("Bangkok", person.getAddress().getCity().getName());
    }

    // Tests deep stubbing with generic interface return type resolves metadata
    @Test
    public void testAnswer_genericInterfaceReturnType_resolvesTypeCorrectly() {
        Container<Person> container = mock(Container.class, returnsDeepStubs);
        Person person = container.getItem();
        assertNotNull(person);
        assertEquals(0, person.getAge());
    }

    // Tests complex nested generic type returns mockable deep stub
    @Test
    public void testAnswer_complexGenericsNest_returnsDeepMock() {
        GenericsNest<?> nest = mock(GenericsNest.class, returnsDeepStubs);
        assertNotNull(nest.entrySet());
        assertNotNull(nest.entrySet().iterator());
    }

    // Tests collection return type returns empty collection from delegate
    @Test
    public void testAnswer_collectionReturnType_returnsEmptyList() {
        Person person = mock(Person.class, returnsDeepStubs);
        List<String> tags = person.getTags();
        assertNotNull(tags);
        assertTrue(tags.isEmpty());
    }

    // Tests actualParameterizedType extracts metadata from mock
    @Test
    public void testActualParameterizedType_validMock_returnsGenericMetadataSupport() {
        Person person = mock(Person.class, returnsDeepStubs);
        GenericMetadataSupport metadata = returnsDeepStubs.actualParameterizedType(person);
        assertNotNull(metadata);
        assertEquals(Person.class, metadata.rawType());
    }

    // Tests serialization round-trip of serializable mock with deep stubs
    @Test
    public void testSerialization_serializableMockWithDeepStubs_deserializesCorrectly() throws Exception {
        SerializablePerson person = mock(SerializablePerson.class, withSettings().serializable().defaultAnswer(returnsDeepStubs));
        when(person.getAge()).thenReturn(25);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(person);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        SerializablePerson deserialized = (SerializablePerson) ois.readObject();
        ois.close();

        assertEquals(25, deserialized.getAge());
    }

    // Tests deep stubbing on class without serializable interface when parent is not serializable
    @Test
    public void testAnswer_nonSerializableClassReturn_returnsDeepStub() {
        ServiceWithNonSerializableClass service = mock(ServiceWithNonSerializableClass.class, returnsDeepStubs);
        NonSerializableItem item = service.getItem();
        assertNotNull(item);
    }

    // Tests deep stubbing propagation of serializable setting to child mocks
    @Test
    public void testSerialization_chainedDeepStubsSerializationRoundTrip() throws Exception {
        SerializablePerson person = mock(SerializablePerson.class, withSettings().serializable().defaultAnswer(returnsDeepStubs));
        Address address = person.getAddress();
        assertNotNull(address);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(person);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        SerializablePerson deserializedPerson = (SerializablePerson) ois.readObject();
        ois.close();

        assertNotNull(deserializedPerson.getAddress());
    }

    // Tests deep stubbing when serializable mock returns non-serializable class
    @Test
    public void testSerialization_serializableMockReturningNonSerializableType() {
        SerializableServiceWithNonSerializableReturn service = mock(
                SerializableServiceWithNonSerializableReturn.class,
                withSettings().serializable().defaultAnswer(returnsDeepStubs)
        );
        NonSerializableItem item = service.getItem();
        assertNotNull(item);
    }

    // Tests deep stubbing with generic method type parameter
    @Test
    public void testAnswer_genericMethodTypeParameter_returnsDeepMock() {
        GenericMethodContainer container = mock(GenericMethodContainer.class, returnsDeepStubs);
        Address address = container.find(Address.class);
        assertNotNull(address);
        assertNotNull(address.getCity());
    }

    // Tests deep stubbing with custom delegate answer
    @Test
    public void testAnswer_withCustomDelegateAnswer_delegatesProperlyForNonMockable() {
        Answer<Object> customDelegate = new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                if (invocation.getMethod().getReturnType().equals(String.class)) {
                    return "custom-default";
                }
                return new ReturnsEmptyValues().answer(invocation);
            }
        };

        ReturnsDeepStubs customDeepStubs = new ReturnsDeepStubs(customDelegate);
        Person person = mock(Person.class, customDeepStubs);

        assertEquals("custom-default", person.getName());
        assertNotNull(person.getAddress());
    }

    // Helper Interfaces and Classes
    interface Person {
        Address getAddress();
        int getAge();
        String getName();
        boolean isActive();
        List<String> getTags();
    }

    interface SerializablePerson extends Serializable {
        Address getAddress();
        int getAge();
    }

    interface Address {
        City getCity();
        String getStreet();
    }

    interface City {
        String getName();
    }

    interface Container<T> {
        T getItem();
    }

    interface GenericMethodContainer {
        <T> T find(Class<T> type);
    }

    interface SerializableServiceWithNonSerializableReturn extends Serializable {
        NonSerializableItem getItem();
    }

    interface GenericsNest<K extends Comparable<K> & Cloneable> extends Map<K, Set<Number>> {}

    static class NonSerializableItem {
        private final String value;
        public NonSerializableItem(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
    }

    interface ServiceWithNonSerializableClass {
        NonSerializableItem getItem();
    }
}