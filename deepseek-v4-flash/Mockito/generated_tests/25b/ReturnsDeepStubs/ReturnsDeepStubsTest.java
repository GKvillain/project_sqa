package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.internal.InternalMockHandler;
import org.mockito.internal.creation.settings.CreationSettings;
import org.mockito.internal.stubbing.InvocationContainerImpl;
import org.mockito.internal.stubbing.StubbedInvocationMatcher;
import org.mockito.internal.util.MockCreationValidator;
import org.mockito.internal.util.MockUtil;
import org.mockito.internal.util.reflection.GenericMetadataSupport;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReturnsDeepStubsTest {

    // Tests the normal flow: answer() method for a mockable return type when no previous stub matches.
    // Verifies that getMock() returns the newly created deep stub mock.
    @Test
    public void testAnswer_mockableReturnTypeNoMatch_returnsDeepStubMock() throws Throwable {
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        // Create a mock of a class with a method returning List (mockable)
        List<?> mock = org.mockito.Mockito.mock(List.class, answer);
        // Invoke a method that returns a mockable type
        Object result = mock.iterator().next();
        assertNotNull(result);
    }

    // Tests that answer() returns delegate value for non-mockable types (e.g., final class or primitive).
    // Uses the newMockCreationValidator branch: !isTypeMockable(rawType)
    @Test
    public void testAnswer_nonMockableReturnType_returnsDelegateValue() throws Throwable {
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        // String is final and not mockable in some contexts; but real test uses primitive-like.
        // Instead, use a mock with method returning void (non-mockable return type).
        // For simplicity, mock an interface with method returning int.
        TestInterface mock = org.mockito.Mockito.mock(TestInterface.class, answer);
        int result = mock.getIntValue();
        assertEquals(0, result); // default value for int
    }

    // Tests getMock: when a previous stub matches the invocation, return that stub's answer.
    @Test
    public void testGetMock_matchingStubExists_returnsStubbedAnswer() throws Throwable {
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        TestList mock = org.mockito.Mockito.mock(TestList.class, answer);
        // Prepare a stub on the mock's method (size()) to return fixed value.
        org.mockito.Mockito.when(mock.size()).thenReturn(100);
        // Invoke via deep stubs: get(0).size() -> should use stub.
        Object result = mock.get(0).size();
        assertEquals(100, result);
    }

    // Tests getMock: when no prior stub matches, record deep stub mock.
    @Test
    public void testGetMock_noMatchingStub_recordsDeepStub() throws Throwable {
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        TestMap mock = org.mockito.Mockito.mock(TestMap.class, answer);
        // Invoke method that returns a complex type
        Object result = mock.entrySet().iterator().next().getKey();
        assertNotNull(result);
    }

    // Tests recordDeepStubMock: creates mock and adds answer to container.
    @Test
    public void testRecordDeepStubMock_createsMockAndAddsAnswer() throws Throwable {
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        TestList mock = org.mockito.Mockito.mock(TestList.class, answer);
        // Trigger recordDeepStubMock for get(0)
        Object deepMock = mock.get(0);
        assertNotNull(deepMock);
        // Invoke again; should return the same mock
        Object sameMock = mock.get(0);
        assertSame(deepMock, sameMock);
    }

    // Tests actualParameterizedType: infers generic type from mock settings.
    @Test
    public void testActualParameterizedType_returnsGenericMetadata() {
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        TestList mock = org.mockito.Mockito.mock(TestList.class, answer);
        GenericMetadataSupport metadata = answer.actualParameterizedType(mock);
        assertNotNull(metadata);
    }

    // Tests that answer() throws exception for invalid method matching internal state.
    @Test(expected = ClassCastException.class)
    public void testGetMock_invalidContainerCast_throwsException() throws Throwable {
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        Object mock = org.mockito.Mockito.mock(Object.class, answer);
        mock.toString(); // This will eventually throw ClassCastException because container cast fails
    }

    // Interface for testing non-mockable return type (int)
    interface TestInterface {
        int getIntValue();
    }

    // Interface with generic list method
    interface TestList {
        TestList get(int index);
        int size();
    }

    // Interface with generic map method
    interface TestMap {
        Set<Map.Entry<String, String>> entrySet();
        TestMap getKey();
    }
}