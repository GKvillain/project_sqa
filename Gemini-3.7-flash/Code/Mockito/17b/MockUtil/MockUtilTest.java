package org.mockito.internal.util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.internal.MockHandlerInterface;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.stubbing.Answer;
import org.mockito.invocation.InvocationOnMock;

import java.io.Serializable;
import java.util.List;

import static org.junit.Assert.*;

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
        assertFalse(mockUtil.isMock("A regular string"));
        assertFalse(mockUtil.isMock(new Object()));
    }

    // Tests isMock with valid mock instance
    @Test
    public void testIsMock_validMock_returnsTrue() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List<?> mockList = mockUtil.createMock(List.class, settings);

        assertTrue(mockUtil.isMock(mockList));
    }

    // Tests getMockHandler with null input
    @Test(expected = NotAMockException.class)
    public void testGetMockHandler_nullInput_throwsNotAMockException() {
        mockUtil.getMockHandler(null);
    }

    // Tests getMockHandler with non-mock object
    @Test(expected = NotAMockException.class)
    public void testGetMockHandler_nonMockObject_throwsNotAMockException() {
        mockUtil.getMockHandler("Not a mock");
    }

    // Tests getMockHandler with valid mock
    @Test
    public void testGetMockHandler_validMock_returnsHandler() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List<?> mockList = mockUtil.createMock(List.class, settings);

        MockHandlerInterface<?> handler = mockUtil.getMockHandler(mockList);

        assertNotNull(handler);
        assertNotNull(handler.getMockSettings());
    }

    // Tests getMockName with valid mock
    @Test
    public void testGetMockName_validMock_returnsMockName() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List<?> mockList = mockUtil.createMock(List.class, settings);

        MockName mockName = mockUtil.getMockName(mockList);

        assertNotNull(mockName);
        assertTrue(mockName.toString().contains("list"));
    }

    // Tests createMock with a class type
    @Test
    public void testCreateMock_classType_createsMockSuccessfully() {
        SampleClass mock = mockUtil.createMock(SampleClass.class, new MockSettingsImpl());

        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
    }

    // Tests createMock with extra interfaces
    @Test
    public void testCreateMock_extraInterfaces_implementsInterfaces() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(Serializable.class);

        List<?> mock = mockUtil.createMock(List.class, settings);

        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
        assertTrue(mock instanceof Serializable);
    }

    // Tests createMock with spied instance
    @Test
    public void testCreateMock_withSpiedInstance_createsSpy() {
        SampleClass toSpy = new SampleClass();
        toSpy.setValue("test-value");

        MockSettingsImpl settings = new MockSettingsImpl();
        settings.spiedInstance(toSpy);

        SampleClass spy = mockUtil.createMock(SampleClass.class, settings);

        assertNotNull(spy);
        assertTrue(mockUtil.isMock(spy));
        assertEquals("test-value", spy.getValue());
    }

    // Tests resetMock with valid mock
    @Test
    public void testResetMock_validMock_resetsHandler() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List<?> mockList = mockUtil.createMock(List.class, settings);

        MockHandlerInterface<?> oldHandler = mockUtil.getMockHandler(mockList);
        mockUtil.resetMock(mockList);
        MockHandlerInterface<?> newHandler = mockUtil.getMockHandler(mockList);

        assertNotNull(newHandler);
        assertNotSame(oldHandler, newHandler);
    }

    // Tests custom CreationValidator constructor
    @Test
    public void testConstructor_customCreationValidator_initializedCorrectly() {
        CreationValidator validator = new CreationValidator();
        MockUtil customMockUtil = new MockUtil(validator);

        MockSettingsImpl settings = new MockSettingsImpl();
        List<?> mock = customMockUtil.createMock(List.class, settings);

        assertNotNull(mock);
        assertTrue(customMockUtil.isMock(mock));
    }

    // Tests createMock with serializable setting enabled
    @Test
    public void testCreateMock_serializable_implementsSerializable() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.serializable();

        List<?> mock = mockUtil.createMock(List.class, settings);

        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
        assertTrue(mock instanceof Serializable);
    }

    // Tests createMock with serializable and extra interfaces
    @Test
    public void testCreateMock_serializableAndExtraInterfaces_implementsAll() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.serializable();
        settings.extraInterfaces(Cloneable.class);

        List<?> mock = mockUtil.createMock(List.class, settings);

        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
        assertTrue(mock instanceof Serializable);
        assertTrue(mock instanceof Cloneable);
    }

    // Tests createMock with custom name
    @Test
    public void testCreateMock_customMockName_returnsCustomName() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.name("myCustomMock");

        List<?> mock = mockUtil.createMock(List.class, settings);

        assertEquals("myCustomMock", mockUtil.getMockName(mock).toString());
    }

    // Tests createMock with custom default answer
    @Test
    public void testCreateMock_withDefaultAnswer_usesConfiguredAnswer() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                return "custom_default";
            }
        });

        SampleClass mock = mockUtil.createMock(SampleClass.class, settings);

        assertEquals("custom_default", mock.getValue());
    }

    public static class SampleClass {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}