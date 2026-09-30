package org.mockito.internal.util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.internal.MockHandlerInterface;
import org.mockito.internal.creation.MockSettingsImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class MockUtilTest {

    private MockUtil mockUtil;

    @Before
    public void setUp() {
        mockUtil = new MockUtil();
    }

    // Tests default constructor initialization
    @Test
    public void testConstructor_default_createsInstance() {
        MockUtil util = new MockUtil();
        assertNotNull(util);
    }

    // Tests constructor with custom MockCreationValidator
    @Test
    public void testConstructor_withValidator_createsInstance() {
        MockCreationValidator validator = new MockCreationValidator();
        MockUtil util = new MockUtil(validator);
        assertNotNull(util);
    }

    // Tests isMock returns false for null input
    @Test
    public void testIsMock_nullInput_returnsFalse() {
        assertFalse(mockUtil.isMock(null));
    }

    // Tests isMock returns false for a standard non-mock object
    @Test
    public void testIsMock_regularObject_returnsFalse() {
        assertFalse(mockUtil.isMock(new Object()));
        assertFalse(mockUtil.isMock("regular_string"));
    }

    // Tests isMock returns true for a created mock
    @Test
    public void testIsMock_mockObject_returnsTrue() {
        List<?> mock = mockUtil.createMock(List.class, new MockSettingsImpl());
        assertTrue(mockUtil.isMock(mock));
    }

    // Tests getMockHandler throws NotAMockException for null input
    @Test(expected = NotAMockException.class)
    public void testGetMockHandler_nullInput_throwsNotAMockException() {
        mockUtil.getMockHandler(null);
    }

    // Tests getMockHandler throws NotAMockException for non-mock object
    @Test(expected = NotAMockException.class)
    public void testGetMockHandler_nonMockObject_throwsNotAMockException() {
        mockUtil.getMockHandler("not_a_mock");
    }

    // Tests getMockHandler returns handler for a valid mock
    @Test
    public void testGetMockHandler_validMock_returnsMockHandler() {
        List<?> mock = mockUtil.createMock(List.class, new MockSettingsImpl());
        MockHandlerInterface<?> handler = mockUtil.getMockHandler(mock);
        assertNotNull(handler);
    }

    // Tests getMockName returns MockName for a valid mock
    @Test
    public void testGetMockName_validMock_returnsMockName() {
        List<?> mock = mockUtil.createMock(List.class, new MockSettingsImpl());
        MockName mockName = mockUtil.getMockName(mock);
        assertNotNull(mockName);
        assertNotNull(mockName.toString());
    }

    // Tests createMock for standard class type
    @Test
    public void testCreateMock_standardClass_returnsMockInstance() {
        List<?> mock = mockUtil.createMock(List.class, new MockSettingsImpl());
        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
        assertTrue(mock instanceof List);
    }

    // Tests createMock with extra interfaces
    @Test
    public void testCreateMock_withExtraInterfaces_implementsExtraInterface() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(Comparable.class);
        List<?> mock = mockUtil.createMock(List.class, settings);

        assertNotNull(mock);
        assertTrue(mock instanceof List);
        assertTrue(mock instanceof Comparable);
    }

    // Tests createMock with serializable setting
    @Test
    public void testCreateMock_serializable_implementsSerializable() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.serializable();
        List<?> mock = mockUtil.createMock(List.class, settings);

        assertNotNull(mock);
        assertTrue(mock instanceof Serializable);
    }

    // Tests createMock with serializable and extra interfaces
    @Test
    public void testCreateMock_serializableAndExtraInterfaces_implementsBoth() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(Comparable.class);
        settings.serializable();
        List<?> mock = mockUtil.createMock(List.class, settings);

        assertNotNull(mock);
        assertTrue(mock instanceof Comparable);
        assertTrue(mock instanceof Serializable);
    }

    // Tests createMock with spied instance
    @Test
    public void testCreateMock_spiedInstance_copiesState() {
        ArrayList<String> original = new ArrayList<String>();
        original.add("test_element");

        MockSettingsImpl settings = new MockSettingsImpl();
        settings.spiedInstance(original);

        ArrayList<?> mock = mockUtil.createMock(ArrayList.class, settings);
        assertNotNull(mock);
        assertEquals(1, mock.size());
    }

    // Tests resetMock creates a new handler for the mock
    @Test
    public void testResetMock_validMock_updatesHandler() {
        List<?> mock = mockUtil.createMock(List.class, new MockSettingsImpl());
        MockHandlerInterface<?> handlerBefore = mockUtil.getMockHandler(mock);

        mockUtil.resetMock(mock);

        MockHandlerInterface<?> handlerAfter = mockUtil.getMockHandler(mock);
        assertNotNull(handlerAfter);
        assertNotSame(handlerBefore, handlerAfter);
    }

    // Tests resetMock throws NotAMockException when called on a non-mock
    @Test(expected = NotAMockException.class)
    public void testResetMock_nonMockObject_throwsNotAMockException() {
        mockUtil.resetMock("not_a_mock");
    }
}