package org.mockito.internal.util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.internal.InvocationNotifierHandler;
import org.mockito.internal.MockHandlerInterface;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.listeners.InvocationListener;
import org.mockito.listeners.MethodInvocationReport;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MockUtilTest {

    private MockUtil mockUtil;

    @Before
    public void setUp() {
        mockUtil = new MockUtil();
    }

    // Tests isMock with null input
    @Test
    public void testIsMock_nullInput_returnsFalse() {
        assertFalse(mockUtil.isMock(null));
    }

    // Tests isMock with non-mock object
    @Test
    public void testIsMock_nonMockObject_returnsFalse() {
        assertFalse(mockUtil.isMock("regularString"));
    }

    // Tests isMock with valid created mock
    @Test
    public void testIsMock_validMock_returnsTrue() {
        List<?> mockList = mockUtil.createMock(List.class, new MockSettingsImpl());
        assertTrue(mockUtil.isMock(mockList));
    }

    // Tests getMockHandler with null input throws NotAMockException
    @Test(expected = NotAMockException.class)
    public void testGetMockHandler_nullInput_throwsException() {
        mockUtil.getMockHandler(null);
    }

    // Tests getMockHandler with non-mock object throws NotAMockException
    @Test(expected = NotAMockException.class)
    public void testGetMockHandler_nonMockObject_throwsException() {
        mockUtil.getMockHandler(new Object());
    }

    // Tests getMockHandler with valid mock
    @Test
    public void testGetMockHandler_validMock_returnsMockHandler() {
        List<?> mockList = mockUtil.createMock(List.class, new MockSettingsImpl());
        MockHandlerInterface<?> handler = mockUtil.getMockHandler(mockList);
        assertNotNull(handler);
    }

    // Tests getMockName returns default mock name
    @Test
    public void testGetMockName_validMock_returnsCorrectMockName() {
        List<?> mockList = mockUtil.createMock(List.class, new MockSettingsImpl());
        MockName mockName = mockUtil.getMockName(mockList);
        assertNotNull(mockName);
        assertTrue(mockName.toString().contains("list"));
    }

    // Tests getMockName with custom name
    @Test
    public void testGetMockName_customName_returnsCustomMockName() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.name("customListName");
        List<?> mockList = mockUtil.createMock(List.class, settings);
        MockName mockName = mockUtil.getMockName(mockList);
        assertEquals("customListName", mockName.toString());
    }

    // Tests createMock with extra interfaces
    @Test
    public void testCreateMock_withExtraInterfaces_implementsInterfaces() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(Comparable.class);
        List<?> mockList = mockUtil.createMock(List.class, settings);

        assertTrue(mockList instanceof List);
        assertTrue(mockList instanceof Comparable);
    }

    // Tests createMock with serializable setting
    @Test
    public void testCreateMock_serializable_implementsSerializable() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.serializable();
        List<?> mockList = mockUtil.createMock(List.class, settings);

        assertTrue(mockList instanceof Serializable);
    }

    // Tests createMock with serializable setting and extra interfaces
    @Test
    public void testCreateMock_serializableAndExtraInterfaces_implementsBoth() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.serializable();
        settings.extraInterfaces(Comparable.class);
        List<?> mockList = mockUtil.createMock(List.class, settings);

        assertTrue(mockList instanceof Serializable);
        assertTrue(mockList instanceof Comparable);
    }

    // Tests createMock with spied instance copies state
    @Test
    public void testCreateMock_withSpiedInstance_createsSpyMock() {
        MockSettingsImpl settings = new MockSettingsImpl();
        SampleClass original = new SampleClass();
        original.value = "test-value";
        settings.spiedInstance(original);

        SampleClass spy = mockUtil.createMock(SampleClass.class, settings);
        assertTrue(mockUtil.isMock(spy));
        assertEquals("test-value", spy.value);
    }

    // Tests resetMock creates a functional mock handler
    @Test
    public void testResetMock_resetsMockCorrectly() {
        List<?> mockList = mockUtil.createMock(List.class, new MockSettingsImpl());
        MockHandlerInterface<?> oldHandler = mockUtil.getMockHandler(mockList);

        mockUtil.resetMock(mockList);

        MockHandlerInterface<?> newHandler = mockUtil.getMockHandler(mockList);
        assertNotNull(newHandler);
        assertTrue(mockUtil.isMock(mockList));
    }

    // Tests resetMock preserves invocation notifier handler structure
    @Test
    public void testResetMock_preservesInvocationNotifierHandler() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.invocationListeners(new InvocationListener() {
            public void reportInvocation(MethodInvocationReport methodInvocationReport) {
            }
        });
        List<?> mockList = mockUtil.createMock(List.class, settings);
        
        mockUtil.resetMock(mockList);
        
        MockHandlerInterface<?> handler = mockUtil.getMockHandler(mockList);
        assertTrue(handler instanceof InvocationNotifierHandler);
    }

    // Helper class for spying test
    private static class SampleClass {
        String value;
    }
}