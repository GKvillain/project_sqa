package org.mockito.internal.util;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.MockHandlerInterface;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.internal.util.reflection.LenientCopyTool;

import static org.junit.Assert.*;

public class MockUtilTest {

    private MockUtil mockUtil;

    @org.junit.Before
    public void setUp() {
        mockUtil = new MockUtil();
    }

    // Tests basic mock creation with default settings
    @Test
    public void testCreateMock_defaultSettings_returnsMock() {
        MockSettingsImpl settings = (MockSettingsImpl) Mockito.withSettings();
        java.util.List mock = mockUtil.createMock(java.util.List.class, settings);
        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
        assertTrue(mock instanceof java.util.List);
    }

    // Tests mock creation with extra interfaces
    @Test
    public void testCreateMock_withExtraInterfaces_returnsMock() {
        MockSettingsImpl settings = (MockSettingsImpl) Mockito.withSettings()
                .extraInterfaces(Runnable.class);
        java.util.List mock = mockUtil.createMock(java.util.List.class, settings);
        assertNotNull(mock);
        assertTrue(mock instanceof java.util.List);
        assertTrue(mock instanceof Runnable);
        assertTrue(mockUtil.isMock(mock));
    }

    // Tests mock creation with spied instance
    @Test
    public void testCreateMock_withSpiedInstance_returnsMockWithCopiedState() {
        java.util.ArrayList spyTarget = new java.util.ArrayList();
        spyTarget.add("test");
        MockSettingsImpl settings = (MockSettingsImpl) Mockito.withSettings()
                .spiedInstance(spyTarget);
        java.util.List mock = mockUtil.createMock(java.util.List.class, settings);
        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
        assertEquals(1, mock.size());
        assertEquals("test", mock.get(0));
    }

    // Tests mock creation with Serializable setting
    @Test
    public void testCreateMock_serializableSetting_returnsMock() {
        MockSettingsImpl settings = (MockSettingsImpl) Mockito.withSettings()
                .serializable();
        java.util.List mock = mockUtil.createMock(java.util.List.class, settings);
        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
        assertTrue(mock instanceof java.io.Serializable);
    }

    // Tests mock creation with Serializable and extra interfaces
    @Test
    public void testCreateMock_serializableWithExtraInterfaces_returnsMock() {
        MockSettingsImpl settings = (MockSettingsImpl) Mockito.withSettings()
                .serializable()
                .extraInterfaces(Runnable.class);
        java.util.List mock = mockUtil.createMock(java.util.List.class, settings);
        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
        assertTrue(mock instanceof java.io.Serializable);
        assertTrue(mock instanceof Runnable);
    }

    // Tests resetMock behavior
    @Test
    public void testResetMock_mockInstance_resetsState() {
        java.util.List mock = mockUtil.createMock(java.util.List.class,
                (MockSettingsImpl) Mockito.withSettings());
        Mockito.when(mock.size()).thenReturn(5);
        assertEquals(5, mock.size());
        mockUtil.resetMock(mock);
        assertEquals(0, mock.size());
        assertTrue(mockUtil.isMock(mock));
    }

    // Tests getMockHandler for a mock instance
    @Test
    public void testGetMockHandler_mockInstance_returnsHandler() {
        java.util.List mock = mockUtil.createMock(java.util.List.class,
                (MockSettingsImpl) Mockito.withSettings());
        MockHandlerInterface handler = mockUtil.getMockHandler(mock);
        assertNotNull(handler);
    }

    // Tests getMockHandler with null input
    @Test(expected = NotAMockException.class)
    public void testGetMockHandler_nullInput_throwsNotAMockException() {
        mockUtil.getMockHandler(null);
    }

    // Tests getMockHandler with non-mock object
    @Test(expected = NotAMockException.class)
    public void testGetMockHandler_nonMockObject_throwsNotAMockException() {
        Object nonMock = new Object();
        mockUtil.getMockHandler(nonMock);
    }

    // Tests isMock with null input
    @Test
    public void testIsMock_nullInput_returnsFalse() {
        assertFalse(mockUtil.isMock(null));
    }

    // Tests isMock with mock instance
    @Test
    public void testIsMock_mockInstance_returnsTrue() {
        java.util.List mock = mockUtil.createMock(java.util.List.class,
                (MockSettingsImpl) Mockito.withSettings());
        assertTrue(mockUtil.isMock(mock));
    }

    // Tests isMock with non-mock object
    @Test
    public void testIsMock_nonMockObject_returnsFalse() {
        Object nonMock = new Object();
        assertFalse(mockUtil.isMock(nonMock));
    }

    // Tests getMockName with mock instance
    @Test
    public void testGetMockName_mockInstance_returnsName() {
        java.util.List mock = mockUtil.createMock(java.util.List.class,
                (MockSettingsImpl) Mockito.withSettings().name("myMock"));
        MockName mockName = mockUtil.getMockName(mock);
        assertNotNull(mockName);
        assertEquals("myMock", mockName.toString());
    }

    // Tests getMockName with default mock name
    @Test
    public void testGetMockName_defaultSettings_returnsGeneratedName() {
        java.util.List mock = mockUtil.createMock(java.util.List.class,
                (MockSettingsImpl) Mockito.withSettings());
        MockName mockName = mockUtil.getMockName(mock);
        assertNotNull(mockName);
        assertFalse(mockName.toString().isEmpty());
    }

    // Tests createMock with interface class
    @Test
    public void testCreateMock_interfaceClass_returnsMock() {
        Runnable mock = mockUtil.createMock(Runnable.class,
                (MockSettingsImpl) Mockito.withSettings());
        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
    }

    // Tests mock creation with validateType failure
    @Test(expected = RuntimeException.class)
    public void testCreateMock_invalidType_throwsException() {
        MockCreationValidator validator = new MockCreationValidator() {
            @Override
            public void validateType(Class classToMock) {
                throw new RuntimeException("Invalid type");
            }
        };
        MockUtil util = new MockUtil(validator);
        util.createMock(java.util.List.class, (MockSettingsImpl) Mockito.withSettings());
    }

    // Tests mock creation with validateExtraInterfaces failure
    @Test(expected = RuntimeException.class)
    public void testCreateMock_invalidExtraInterfaces_throwsException() {
        MockCreationValidator validator = new MockCreationValidator() {
            @Override
            public void validateExtraInterfaces(Class classToMock, Class... extraInterfaces) {
                throw new RuntimeException("Invalid extra interfaces");
            }
        };
        MockUtil util = new MockUtil(validator);
        util.createMock(java.util.List.class,
                (MockSettingsImpl) Mockito.withSettings().extraInterfaces(Runnable.class));
    }

    // Tests createMock with serializable and complex interface
    @Test
    public void testCreateMock_serializableWithMultipleInterfaces_returnsMock() {
        MockSettingsImpl settings = (MockSettingsImpl) Mockito.withSettings()
                .serializable()
                .extraInterfaces(Runnable.class, java.io.Closeable.class);
        java.util.List mock = mockUtil.createMock(java.util.List.class, settings);
        assertNotNull(mock);
        assertTrue(mock instanceof java.io.Serializable);
        assertTrue(mock instanceof Runnable);
        assertTrue(mock instanceof java.io.Closeable);
        assertTrue(mockUtil.isMock(mock));
    }

    // Tests resetMock on interface mock
    @Test
    public void testResetMock_interfaceMock_resetsState() {
        java.util.List mock = mockUtil.createMock(java.util.List.class,
                (MockSettingsImpl) Mockito.withSettings());
        Mockito.when(mock.size()).thenReturn(10);
        assertEquals(10, mock.size());
        mockUtil.resetMock(mock);
        assertEquals(0, mock.size());
        assertTrue(mockUtil.isMock(mock));
    }
}