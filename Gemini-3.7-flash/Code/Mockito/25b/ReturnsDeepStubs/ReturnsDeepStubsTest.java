package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.GenericMetadataSupport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ReturnsDeepStubsTest {

    private ReturnsDeepStubs returnsDeepStubs;

    interface SampleService {
        SampleRepository getRepository();
        String getString();
        int getPrimitiveInt();
        boolean getPrimitiveBoolean();
        void doVoid();
    }

    interface SampleRepository {
        SampleEntity getEntity();
        int count();
    }

    interface SampleEntity {
        String getName();
    }

    interface GenericsNest<K extends Comparable<K> & Cloneable> extends Map<K, Set<Number>> {}

    interface GenericContainer<T> {
        T getValue();
        List<T> getList();
    }

    interface StringContainer extends GenericContainer<String> {}

    @Before
    public void setUp() {
        returnsDeepStubs = new ReturnsDeepStubs();
    }

    // Tests deep stubbing on mockable return type
    @Test
    public void testAnswer_mockableType_returnsDeepMock() {
        SampleService service = Mockito.mock(SampleService.class, returnsDeepStubs);
        SampleRepository repository = service.getRepository();

        assertNotNull(repository);
        assertNotNull(repository.getEntity());
    }

    // Tests unmockable primitive int return type returns default value 0
    @Test
    public void testAnswer_primitiveInt_returnsZero() {
        SampleService service = Mockito.mock(SampleService.class, returnsDeepStubs);

        assertEquals(0, service.getPrimitiveInt());
    }

    // Tests unmockable primitive boolean return type returns default value false
    @Test
    public void testAnswer_primitiveBoolean_returnsFalse() {
        SampleService service = Mockito.mock(SampleService.class, returnsDeepStubs);

        assertFalse(service.getPrimitiveBoolean());
    }

    // Tests unmockable String return type returns empty string from default answers
    @Test
    public void testAnswer_stringType_returnsEmptyString() {
        SampleService service = Mockito.mock(SampleService.class, returnsDeepStubs);

        assertEquals("", service.getString());
    }

    // Tests void method invocation returns null
    @Test
    public void testAnswer_voidMethod_returnsNull() {
        SampleService service = Mockito.mock(SampleService.class, returnsDeepStubs);
        service.doVoid();
    }

    // Tests multiple invocations of the same method return the same deep mock instance
    @Test
    public void testAnswer_consecutiveCalls_returnsSameMockInstance() {
        SampleService service = Mockito.mock(SampleService.class, returnsDeepStubs);

        SampleRepository firstCall = service.getRepository();
        SampleRepository secondCall = service.getRepository();

        assertSame(firstCall, secondCall);
    }

    // Tests chained deep stubbing returns previously recorded intermediate mocks
    @Test
    public void testAnswer_nestedChainedCalls_retainsHierarchy() {
        SampleService service = Mockito.mock(SampleService.class, returnsDeepStubs);

        SampleRepository repo = service.getRepository();
        SampleEntity entity1 = repo.getEntity();
        SampleEntity entity2 = service.getRepository().getEntity();

        assertSame(entity1, entity2);
    }

    // Tests explicit stubbing overrides deep stub answer
    @Test
    public void testAnswer_explicitStubbing_returnsStubbedValue() {
        SampleService service = Mockito.mock(SampleService.class, returnsDeepStubs);
        Mockito.when(service.getRepository().count()).thenReturn(42);

        assertEquals(42, service.getRepository().count());
    }

    // Tests deep stubbing with generic interface and bounds
    @Test
    public void testAnswer_genericNest_resolvesNestedGenerics() {
        GenericsNest<?> mock = Mockito.mock(GenericsNest.class, returnsDeepStubs);

        Set<?> entrySet = mock.entrySet();
        assertNotNull(entrySet);

        Iterator<?> iterator = mock.entrySet().iterator();
        assertNotNull(iterator);

        Map.Entry<?, ?> entry = (Map.Entry<?, ?>) mock.entrySet().iterator().next();
        assertNotNull(entry);

        Set<Number> value = (Set<Number>) mock.entrySet().iterator().next().getValue();
        assertNotNull(value);
    }

    // Tests deep stubbing on generic interface with parameterized type
    @Test
    public void testAnswer_parameterizedGeneric_returnsMockForNestedMethod() {
        StringContainer container = Mockito.mock(StringContainer.class, returnsDeepStubs);

        List<String> list = container.getList();
        assertNotNull(list);
        assertNotNull(list.iterator());
    }

    // Tests actualParameterizedType returns metadata from mock settings
    @Test
    public void testActualParameterizedType_validMock_returnsGenericMetadata() {
        SampleService service = Mockito.mock(SampleService.class, returnsDeepStubs);
        GenericMetadataSupport metadata = returnsDeepStubs.actualParameterizedType(service);

        assertNotNull(metadata);
        assertEquals(SampleService.class, metadata.rawType());
    }

    // Tests serialization and deserialization of ReturnsDeepStubs
    @Test
    public void testSerialization_returnsDeepStubs_isSerializable() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(returnsDeepStubs);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        Object deserialized = ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof ReturnsDeepStubs);
    }
}