package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.MockUtil;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class ReturnsDeepStubsTest {

    // Tests answer when return type is String (final, non-mockable) - expects null default
    @Test
    public void testAnswer_nonMockableString_returnsNull() {
        List<String> mock = Mockito.mock(List.class, new ReturnsDeepStubs());
        String result = mock.toString();
        assertNull(result);
    }

    // Tests answer when return type is int (primitive, non-mockable) - expects 0
    @Test
    public void testAnswer_primitiveInt_returnsZero() {
        List<String> mock = Mockito.mock(List.class, new ReturnsDeepStubs());
        int result = mock.size();
        assertEquals(0, result);
    }

    // Tests answer when return type is boolean (primitive, non-mockable) - expects false
    @Test
    public void testAnswer_primitiveBoolean_returnsFalse() {
        List<String> mock = Mockito.mock(List.class, new ReturnsDeepStubs());
        boolean result = mock.isEmpty();
        assertFalse(result);
    }

    // Tests answer when return type is array (Object[]) - expects null
    @Test
    public void testAnswer_arrayReturnType_returnsNull() {
        List<String> mock = Mockito.mock(List.class, new ReturnsDeepStubs());
        Object[] result = mock.toArray();
        assertNull(result);
    }

    // Tests answer when return type is mockable interface (Iterator) - returns a deep stub mock
    @Test
    public void testAnswer_mockableRawType_returnsDeepStubMock() {
        List<String> mock = Mockito.mock(List.class, new ReturnsDeepStubs());
        Iterator<String> result = mock.iterator();
        assertNotNull(result);
        assertTrue(new MockUtil().isMock(result));
    }

    // Tests answer when an existing stubbing matches the invocation - returns stubbed value
    @Test
    public void testAnswer_existingStubbing_returnsStubbedValue() {
        List<String> mock = Mockito.mock(List.class, new ReturnsDeepStubs());
        Iterator<String> stubbedIterator = Mockito.mock(Iterator.class);
        Mockito.when(mock.iterator()).thenReturn(stubbedIterator);
        Iterator<String> result = mock.iterator();
        assertSame(stubbedIterator, result);
    }

    // Tests answer when stubbed invocations exist but none match the current invocation - deep stub is created
    @Test
    public void testAnswer_noMatchingStubbing_returnsDeepStub() {
        List<String> mock = Mockito.mock(List.class, new ReturnsDeepStubs());
        // stub a different method - size()
        Mockito.when(mock.size()).thenReturn(42);
        // iterator() is not stubbed -> should produce deep stub
        Iterator<String> result = mock.iterator();
        assertNotNull(result);
        assertTrue(new MockUtil().isMock(result));
    }

    // Tests answer for a chained method (list.get(0)) returning Object - deep stub for Object
    @Test
    public void testAnswer_chainedMethod_returnsDeepStub() {
        List<String> mock = Mockito.mock(List.class, new ReturnsDeepStubs());
        Object result = mock.get(0);
        assertNotNull(result);
        assertTrue(new MockUtil().isMock(result));
    }
}