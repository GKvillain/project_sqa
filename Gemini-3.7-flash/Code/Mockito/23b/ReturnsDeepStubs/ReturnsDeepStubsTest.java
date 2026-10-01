package org.mockito.internal.stubbing.defaultanswers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.GenericMetadataSupport;
import org.mockito.invocation.InvocationOnMock;

public class ReturnsDeepStubsTest {

    private ReturnsDeepStubs returnsDeepStubs;

    interface SampleContainer<T> {
        T getValue();
    }

    interface SampleService {
        NestedService getNested();
        String getString();
        int getPrimitiveInt();
        int[] getArray();
        SampleContainer<NestedService> getGenericContainer();
        <E extends CharSequence & Serializable> E getMultipleBoundGeneric();
    }

    interface NestedService {
        String getValue();
        DeepService getDeep();
    }

    interface DeepService {
        String getFinalValue();
    }

    @Before
    public void setUp() {
        returnsDeepStubs = new ReturnsDeepStubs();
    }

    // Tests non-mockable primitive return type returns empty value
    @Test
    public void testAnswer_primitiveReturnType_returnsDefaultEmptyValue() {
        SampleService mock = mock(SampleService.class, returnsDeepStubs);

        int result = mock.getPrimitiveInt();

        assertEquals(0, result);
    }

    // Tests non-mockable final class return type returns empty string
    @Test
    public void testAnswer_finalReturnType_returnsEmptyValue() {
        SampleService mock = mock(SampleService.class, returnsDeepStubs);

        String result = mock.getString();

        assertEquals("", result);
    }

    // Tests array return type returns empty array
    @Test
    public void testAnswer_arrayReturnType_returnsEmptyArray() {
        SampleService mock = mock(SampleService.class, returnsDeepStubs);

        int[] result = mock.getArray();

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    // Tests mockable interface return type returns new mock
    @Test
    public void testAnswer_mockableReturnType_returnsMockObject() {
        SampleService mock = mock(SampleService.class, returnsDeepStubs);

        NestedService nested = mock.getNested();

        assertNotNull(nested);
        assertTrue(Mockito.mockingDetails(nested).isMock());
    }

    // Tests repeated invocation on same method returns the same mock instance
    @Test
    public void testAnswer_repeatedInvocation_returnsSameMockInstance() {
        SampleService mock = mock(SampleService.class, returnsDeepStubs);

        NestedService firstCall = mock.getNested();
        NestedService secondCall = mock.getNested();

        assertSame(firstCall, secondCall);
    }

    // Tests multi-level deep stub chaining
    @Test
    public void testAnswer_multiLevelDeepStub_returnsNestedMockAndFinalValue() {
        SampleService mock = mock(SampleService.class, returnsDeepStubs);

        String value = mock.getNested().getDeep().getFinalValue();

        assertEquals("", value);
    }

    // Tests stubbing nested mock behavior explicitly
    @Test
    public void testAnswer_stubbedNestedMock_returnsConfiguredStubValue() {
        SampleService mock = mock(SampleService.class, returnsDeepStubs);

        when(mock.getNested().getValue()).thenReturn("custom_value");

        assertEquals("custom_value", mock.getNested().getValue());
    }

    // Tests generic container return type resolution
    @Test
    public void testAnswer_genericReturnType_resolvesTypeAndReturnsMock() {
        SampleService mock = mock(SampleService.class, returnsDeepStubs);

        NestedService nested = mock.getGenericContainer().getValue();

        assertNotNull(nested);
        assertTrue(Mockito.mockingDetails(nested).isMock());
    }

    // Tests generic return type with extra interfaces
    @Test
    public void testAnswer_genericWithExtraInterfaces_createsMockWithInterfaces() {
        SampleService mock = mock(SampleService.class, returnsDeepStubs);

        CharSequence result = mock.getMultipleBoundGeneric();

        assertNotNull(result);
        assertTrue(result instanceof Serializable);
        assertTrue(result instanceof CharSequence);
    }

    // Tests actualParameterizedType method returns metadata
    @Test
    public void testActualParameterizedType_validMock_returnsGenericMetadata() {
        SampleService mock = mock(SampleService.class, returnsDeepStubs);

        GenericMetadataSupport metadata = returnsDeepStubs.actualParameterizedType(mock);

        assertNotNull(metadata);
        assertEquals(SampleService.class, metadata.rawType());
    }

    // Tests serialization of deep stub mock and answer
    @Test
    public void testAnswer_serializableDeepStubMock_serializesAndDeserializesSuccessfully() throws Exception {
        SampleService mock = mock(SampleService.class, withSettings().defaultAnswer(returnsDeepStubs).serializable());

        NestedService nested = mock.getNested();
        assertNotNull(nested);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(mock);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        SampleService deserializedMock = (SampleService) ois.readObject();
        ois.close();

        assertNotNull(deserializedMock);
        assertNotNull(deserializedMock.getNested());
    }

    // Tests serialization of ReturnsDeepStubs answer itself
    @Test
    public void testReturnsDeepStubs_serialization_serializesAndDeserializesSuccessfully() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(returnsDeepStubs);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        ReturnsDeepStubs deserializedAnswer = (ReturnsDeepStubs) ois.readObject();
        ois.close();

        assertNotNull(deserializedAnswer);
    }
}