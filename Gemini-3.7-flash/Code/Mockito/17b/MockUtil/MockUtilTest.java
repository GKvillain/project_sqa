package org.mockito.internal.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.internal.MockHandlerInterface;
import org.mockito.internal.creation.MockSettingsImpl;

public class MockUtilTest {

    private MockUtil mockUtil;

    @Before
    public void setUp() {
        mockUtil = new MockUtil();
    }

    // Tests creating mock for standard interface
    @Test
    public void testCreateMock_interfaceClass_returnsMockInstance() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List<?> mock = mockUtil.createMock(List.class, settings);

        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
    }

    // Tests creating mock for concrete class
    @Test
    public void testCreateMock_concreteClass_returnsMockInstance() {
        MockSettingsImpl settings = new MockSettingsImpl();
        ArrayList<?> mock = mockUtil.createMock(ArrayList.class, settings);

        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
    }

    // Tests creating mock with extra interfaces
    @Test
    public void testCreateMock_withExtraInterfaces_implementsInterfaces() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(Serializable.class);
        List<?> mock = mockUtil.createMock(List.class, settings);

        assertNotNull(mock);
        assertTrue(mock instanceof Serializable);
        assertTrue(mockUtil.isMock(mock));
    }

    // Tests creating mock with spied instance copies state
    @Test
    public void testCreateMock_withSpiedInstance_createsMockSpy() {
        ArrayList<String> spied = new ArrayList<String>();
        spied.add("item");

        MockSettingsImpl settings = new MockSettingsImpl();
        settings.spiedInstance(spied);

        ArrayList<?> mock = mockUtil.createMock(ArrayList.class, settings);

        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
    }

    // Tests isMock returns true for valid mock
    @Test
    public void testIsMock_validMock_returnsTrue() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List<?> mock = mockUtil.createMock(List.class, settings);

        assertTrue(mockUtil.isMock(mock));
    }

    // Tests isMock returns false for null input
    @Test
    public void testIsMock_nullInput_returnsFalse() {
        assertFalse(mockUtil.isMock(null));
    }

    // Tests isMock returns false for non-mock standard object
    @Test
    public void testIsMock_regularObject_returnsFalse() {
        assertFalse(mockUtil.isMock("regularString"));
        assertFalse(mockUtil.isMock(new Object()));
    }

    // Tests getMockHandler returns valid handler for mock
    @Test
    public void testGetMockHandler_validMock_returnsMockHandler() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List<?> mock = mockUtil.createMock(List.class, settings);

        MockHandlerInterface<?> handler = mockUtil.getMockHandler(mock);

        assertNotNull(handler);
        assertNotNull(handler.getMockSettings());
    }

    // Tests getMockHandler throws NotAMockException on null input
    @Test(expected = NotAMockException.class)
    public void testGetMockHandler_nullInput_throwsException() {
        mockUtil.getMockHandler(null);
    }

    // Tests getMockHandler throws NotAMockException on non-mock object
    @Test(expected = NotAMockException.class)
    public void testGetMockHandler_nonMockObject_throwsException() {
        mockUtil.getMockHandler(new ArrayList<String>());
    }

    // Tests resetMock resets callback on existing mock
    @Test
    public void testResetMock_validMock_resetsSuccessfully() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List<?> mock = mockUtil.createMock(List.class, settings);

        mockUtil.resetMock(mock);

        assertTrue(mockUtil.isMock(mock));
        assertNotNull(mockUtil.getMockHandler(mock));
    }

    // Tests getMockName returns non-null MockName
    @Test
    public void testGetMockName_validMock_returnsMockName() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List<?> mock = mockUtil.createMock(List.class, settings);

        MockName mockName = mockUtil.getMockName(mock);

        assertNotNull(mockName);
        assertNotNull(mockName.toString());
    }

    // Tests custom CreationValidator constructor
    @Test
    public void testConstructor_customCreationValidator_createsMockCorrectly() {
        CreationValidator validator = new CreationValidator();
        MockUtil customMockUtil = new MockUtil(validator);

        MockSettingsImpl settings = new MockSettingsImpl();
        List<?> mock = customMockUtil.createMock(List.class, settings);

        assertNotNull(mock);
        assertTrue(customMockUtil.isMock(mock));
    }

    // Tests resetMock throws NotAMockException on null input
    @Test(expected = NotAMockException.class)
    public void testResetMock_nullInput_throwsException() {
        mockUtil.resetMock(null);
    }

    // Tests resetMock throws NotAMockException on non-mock object
    @Test(expected = NotAMockException.class)
    public void testResetMock_nonMockObject_throwsException() {
        mockUtil.resetMock(new Object());
    }

    // Tests getMockName throws NotAMockException on null input
    @Test(expected = NotAMockException.class)
    public void testGetMockName_nullInput_throwsException() {
        mockUtil.getMockName(null);
    }

    // Tests getMockName throws NotAMockException on non-mock object
    @Test(expected = NotAMockException.class)
    public void testGetMockName_nonMockObject_throwsException() {
        mockUtil.getMockName(new Object());
    }

    // Tests getMockName returns configured mock name
    @Test
    public void testGetMockName_customName_returnsConfiguredMockName() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.name("customMockName");
        List<?> mock = mockUtil.createMock(List.class, settings);

        MockName mockName = mockUtil.getMockName(mock);

        assertNotNull(mockName);
        assertEquals("customMockName", mockName.toString());
    }

    // Tests creating mock with serializable setting
    @Test
    public void testCreateMock_serializableSetting_implementsSerializable() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.serializable();
        List<?> mock = mockUtil.createMock(List.class, settings);

        assertNotNull(mock);
        assertTrue(mock instanceof Serializable);
        assertTrue(mockUtil.isMock(mock));
    }
}