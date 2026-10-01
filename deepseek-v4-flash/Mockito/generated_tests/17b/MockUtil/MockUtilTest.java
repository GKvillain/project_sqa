package org.mockito.internal.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.internal.MockHandlerInterface;
import org.mockito.internal.creation.MockSettingsImpl;

public class MockUtilTest {

    private MockUtil mockUtil = new MockUtil();

    // Tests normal creation of a mock for a simple interface
    @Test
    public void testCreateMock_simpleClass_returnsMockInstance() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List mock = mockUtil.createMock(List.class, settings);
        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
        assertTrue(mock instanceof List);
    }

    // Tests creation with extra interfaces, covering the non-null extra interfaces branch
    @Test
    public void testCreateMock_withExtraInterfaces_mockImplementsInterfaces() {
        MockSettingsImpl settings = (MockSettingsImpl) withSettings()
                .extraInterfaces(Cloneable.class);
        ArrayList mock = mockUtil.createMock(ArrayList.class, settings);
        assertNotNull(mock);
        assertTrue(mock instanceof Cloneable);
        assertTrue(mockUtil.isMock(mock));
    }

    // Tests getMockHandler with null input – expects NotAMockException
    @Test(expected = NotAMockException.class)
    public void testGetMockHandler_nullInput_throwsNotAMockException() {
        mockUtil.getMockHandler(null);
    }

    // Tests getMockHandler with a non-mock object – expects NotAMockException
    @Test(expected = NotAMockException.class)
    public void testGetMockHandler_nonMock_throwsNotAMockException() {
        mockUtil.getMockHandler("not a mock");
    }

    // Tests getMockHandler with a valid mock – returns a handler
    @SuppressWarnings("unchecked")
    @Test
    public void testGetMockHandler_validMock_returnsHandler() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List mock = mockUtil.createMock(List.class, settings);
        MockHandlerInterface<List> handler = mockUtil.getMockHandler(mock);
        assertNotNull(handler);
    }

    // Tests isMock with null – returns false
    @Test
    public void testIsMock_null_returnsFalse() {
        assertFalse(mockUtil.isMock(null));
    }

    // Tests isMock with a non-mock object – returns false
    @Test
    public void testIsMock_nonMock_returnsFalse() {
        assertFalse(mockUtil.isMock("hello"));
    }

    // Tests isMock with a valid mock – returns true
    @Test
    public void testIsMock_validMock_returnsTrue() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List mock = mockUtil.createMock(List.class, settings);
        assertTrue(mockUtil.isMock(mock));
    }

    // Tests resetMock with null input – expects NotAMockException
    @Test(expected = NotAMockException.class)
    public void testResetMock_null_throwsNotAMockException() {
        mockUtil.resetMock(null);
    }

    // Tests resetMock with a non-mock object – expects NotAMockException
    @Test(expected = NotAMockException.class)
    public void testResetMock_nonMock_throwsNotAMockException() {
        mockUtil.resetMock("not a mock");
    }

    // Tests resetMock with a valid mock – mock remains a mock after reset
    @Test
    public void testResetMock_validMock_succeeds() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List mock = mockUtil.createMock(List.class, settings);
        mockUtil.resetMock(mock);
        assertTrue(mockUtil.isMock(mock));
    }

    // Tests that resetMock preserves the mock name (defect detection)
    @Test
    public void testResetMock_mockNamePreserved_afterReset() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List mock = mockUtil.createMock(List.class, settings);
        MockName nameBefore = mockUtil.getMockName(mock);
        mockUtil.resetMock(mock);
        MockName nameAfter = mockUtil.getMockName(mock);
        assertEquals(nameBefore.toString(), nameAfter.toString());
    }

    // Tests getMockName on a valid mock – returns a non-null name containing the class simple name
    @Test
    public void testGetMockName_validMock_returnsName() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List mock = mockUtil.createMock(List.class, settings);
        MockName name = mockUtil.getMockName(mock);
        assertNotNull(name);
        assertTrue(name.toString().toLowerCase().contains("list"));
    }
}