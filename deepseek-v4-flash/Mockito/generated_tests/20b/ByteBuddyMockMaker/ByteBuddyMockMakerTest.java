package org.mockito.internal.creation.bytebuddy;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;
import org.mockito.internal.InternalMockHandler;
import org.mockito.invocation.MockHandler;
import org.mockito.internal.creation.bytebuddy.MockMethodInterceptor;
import org.mockito.internal.creation.bytebuddy.MockMethodInterceptor.MockAccess;
import org.mockito.mock.MockCreationSettings;
import org.mockito.mock.SerializableMode;
import org.mockito.exceptions.base.MockitoException;

import java.io.Serializable;
import java.util.Collections;

public class ByteBuddyMockMakerTest {

    // Tests createMock with valid settings and a proper internal handler
    @Test
    public void testCreateMock_normalSettings_returnsMockInstance() {
        MockCreationSettings<Runnable> settings = mock(MockCreationSettings.class);
        MockHandler handler = mock(InternalMockHandler.class);
        when(settings.getTypeToMock()).thenReturn(Runnable.class);
        when(settings.getExtraInterfaces()).thenReturn(Collections.emptySet());
        when(settings.getSerializableMode()).thenReturn(SerializableMode.NONE);

        ByteBuddyMockMaker mockMaker = new ByteBuddyMockMaker();
        Runnable mock = mockMaker.createMock(settings, handler);

        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
    }

    // Tests that SerializableMode.ACROSS_CLASSLOADERS throws immediately
    @Test(expected = MockitoException.class)
    public void testCreateMock_serializableAcrossClassLoaders_throwsMockitoException() {
        MockCreationSettings<Runnable> settings = mock(MockCreationSettings.class);
        MockHandler handler = mock(MockHandler.class);
        when(settings.getSerializableMode()).thenReturn(SerializableMode.ACROSS_CLASSLOADERS);

        // typeToMock and extraInterfaces are not accessed in this branch,
        // but we set them to avoid potential NullPointerException if code changes
        when(settings.getTypeToMock()).thenReturn(Runnable.class);
        when(settings.getExtraInterfaces()).thenReturn(Collections.emptySet());

        new ByteBuddyMockMaker().createMock(settings, handler);
    }

    // Tests that a handler which is not an InternalMockHandler causes a MockitoException
    @Test(expected = MockitoException.class)
    public void testCreateMock_nonInternalMockHandler_throwsMockitoException() {
        MockCreationSettings<Runnable> settings = mock(MockCreationSettings.class);
        MockHandler handler = mock(MockHandler.class); // NOT InternalMockHandler
        when(settings.getTypeToMock()).thenReturn(Runnable.class);
        when(settings.getExtraInterfaces()).thenReturn(Collections.emptySet());
        when(settings.getSerializableMode()).thenReturn(SerializableMode.NONE);

        new ByteBuddyMockMaker().createMock(settings, handler);
    }

    // Tests createMock with additional extra interfaces
    @Test
    public void testCreateMock_withExtraInterfaces_createsMock() {
        MockCreationSettings<Runnable> settings = mock(MockCreationSettings.class);
        MockHandler handler = mock(InternalMockHandler.class);
        when(settings.getTypeToMock()).thenReturn(Runnable.class);
        when(settings.getExtraInterfaces()).thenReturn(Collections.singleton(Serializable.class));
        when(settings.getSerializableMode()).thenReturn(SerializableMode.NONE);

        ByteBuddyMockMaker mockMaker = new ByteBuddyMockMaker();
        Runnable mock = mockMaker.createMock(settings, handler);

        assertNotNull(mock);
        assertTrue(mock instanceof Runnable);
        assertTrue(mock instanceof Serializable);
    }

    // Tests getHandler when the mock implements MockAccess
    @Test
    public void testGetHandler_mockIsMockAccess_returnsHandler() {
        ByteBuddyMockMaker mockMaker = new ByteBuddyMockMaker();
        MockAccess mockAccess = mock(MockAccess.class);
        MockMethodInterceptor interceptor = mock(MockMethodInterceptor.class);
        MockHandler mockHandler = mock(MockHandler.class);
        when(mockAccess.getMockitoInterceptor()).thenReturn(interceptor);
        when(interceptor.getMockHandler()).thenReturn(mockHandler);

        MockHandler result = mockMaker.getHandler(mockAccess);

        assertSame(mockHandler, result);
    }

    // Tests getHandler when the mock does not implement MockAccess
    @Test
    public void testGetHandler_mockIsNotMockAccess_returnsNull() {
        ByteBuddyMockMaker mockMaker = new ByteBuddyMockMaker();
        Object nonMock = new Object();

        MockHandler result = mockMaker.getHandler(nonMock);

        assertNull(result);
    }

    // Tests resetMock sets a new interceptor on the mock
    @Test
    public void testResetMock_setsNewInterceptor() {
        ByteBuddyMockMaker mockMaker = new ByteBuddyMockMaker();
        MockAccess mockAccess = mock(MockAccess.class);
        MockCreationSettings settings = mock(MockCreationSettings.class);
        MockHandler newHandler = mock(InternalMockHandler.class);

        mockMaker.resetMock(mockAccess, newHandler, settings);

        verify(mockAccess).setMockitoInterceptor(any(MockMethodInterceptor.class));
    }
}