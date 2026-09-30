package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.GenericMetadataSupport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class ReturnsDeepStubsTest {

    private ReturnsDeepStubs returnsDeepStubs;

    interface SampleInterface {
        SampleInterface getNext();
        String getString();
        int getInt();
        boolean getBoolean();
        List<String> getList();
    }

    interface GenericParent<T> {
        T getValue();
        List<T> getListValues();
    }

    interface StringGenericChild extends GenericParent<SampleInterface> {
    }

    interface GenericsNest<K extends Comparable<K> & Cloneable> extends Map<K, Set<Number>> {
    }

    interface NestedGenerics<T extends SampleInterface> {
        T getNested();
    }

    static class SampleClass {
        public SampleInterface getSampleInterface() {
            return null;
        }

        public final String getFinalString() {
            return "final";
        }
    }

    @Before
    public void setUp() {
        returnsDeepStubs = new ReturnsDeepStubs();
    }

    // Tests returning default value for primitive int return type
    @Test
    public void testAnswer_primitiveIntReturnType_returnsZero() {
        SampleInterface mock = Mockito.mock(SampleInterface.class, returnsDeepStubs);
        assertEquals(0, mock.getInt());
    }

    // Tests returning default value for primitive boolean return type
    @Test
    public void testAnswer_primitiveBooleanReturnType_returnsFalse() {
        SampleInterface mock = Mockito.mock(SampleInterface.class, returnsDeepStubs);
        assertFalse(mock.getBoolean());
    }

    // Tests returning empty string / non-mockable type
    @Test
    public void testAnswer_stringReturnType_returnsEmptyString() {
        SampleInterface mock = Mockito.mock(SampleInterface.class, returnsDeepStubs);
        assertEquals("", mock.getString());
    }

    // Tests returning deep mock for mockable interface return type
    @Test
    public void testAnswer_mockableInterfaceReturnType_returnsMock() {
        SampleInterface mock = Mockito.mock(SampleInterface.class, returnsDeepStubs);
        SampleInterface next = mock.getNext();
        assertNotNull(next);
        assertEquals(0, next.getInt());
    }

    // Tests that chained deep stub calls work across multiple levels
    @Test
    public void testAnswer_deepStubChain_returnsNestedMockValues() {
        SampleInterface mock = Mockito.mock(SampleInterface.class, returnsDeepStubs);
        assertEquals("", mock.getNext().getNext().getString());
    }

    // Tests returning same mock instance on repeated calls to same method
    @Test
    public void testAnswer_repeatedInvocation_returnsSameMockInstance() {
        SampleInterface mock = Mockito.mock(SampleInterface.class, returnsDeepStubs);
        SampleInterface firstCall = mock.getNext();
        SampleInterface secondCall = mock.getNext();
        assertSame(firstCall, secondCall);
    }

    // Tests returning empty collections from delegate for collection types
    @Test
    public void testAnswer_collectionReturnType_returnsEmptyCollection() {
        SampleInterface mock = Mockito.mock(SampleInterface.class, returnsDeepStubs);
        List<String> list = mock.getList();
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    // Tests generic type resolution on generic interface
    @Test
    public void testAnswer_genericInterfaceReturnType_resolvesType() {
        StringGenericChild mock = Mockito.mock(StringGenericChild.class, returnsDeepStubs);
        SampleInterface value = mock.getValue();
        assertNotNull(value);
        assertEquals("", value.getString());
    }

    // Tests nested generics navigation
    @Test
    public void testAnswer_nestedGenerics_navigatesGenericsTree() {
        GenericsNest<?> mock = Mockito.mock(GenericsNest.class, returnsDeepStubs);
        assertNotNull(mock.entrySet());
        assertNotNull(mock.values());
    }

    // Tests deep stubbing with generic type bound
    @Test
    public void testAnswer_boundedGenericType_returnsMockForType() {
        NestedGenerics<?> mock = Mockito.mock(NestedGenerics.class, returnsDeepStubs);
        SampleInterface nested = mock.getNested();
        assertNotNull(nested);
        assertEquals(0, nested.getInt());
    }

    // Tests mock creation on a concrete class
    @Test
    public void testAnswer_classReturnType_returnsMock() {
        SampleClass mock = Mockito.mock(SampleClass.class, returnsDeepStubs);
        SampleInterface sampleInterface = mock.getSampleInterface();
        assertNotNull(sampleInterface);
        assertEquals("", sampleInterface.getString());
    }

    // Tests actualParameterizedType extracts metadata from mock
    @Test
    public void testActualParameterizedType_validMock_returnsGenericMetadata() {
        SampleInterface mock = Mockito.mock(SampleInterface.class, returnsDeepStubs);
        GenericMetadataSupport metadata = returnsDeepStubs.actualParameterizedType(mock);
        assertNotNull(metadata);
        assertEquals(SampleInterface.class, metadata.rawType());
    }

    // Tests serialization of ReturnsDeepStubs
    @Test
    public void testSerialization_returnsDeepStubs_isSerializable() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(returnsDeepStubs);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertSame(ReturnsDeepStubs.class, deserialized.getClass());
    }
}