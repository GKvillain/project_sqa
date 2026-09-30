package org.mockito.internal.creation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockSettings;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.listeners.InvocationListener;
import org.mockito.listeners.MethodInvocationReport;
import org.mockito.stubbing.Answer;
import org.mockito.invocation.InvocationOnMock;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MockSettingsImplTest {

    private MockSettingsImpl mockSettings;

    @Before
    public void setUp() {
        mockSettings = new MockSettingsImpl();
    }

    // Tests default state of serializable flag
    @Test
    public void testIsSerializable_defaultState_returnsFalse() {
        assertFalse(mockSettings.isSerializable());
        assertNull(mockSettings.getExtraInterfaces());
    }

    // Tests enabling serialization setting
    @Test
    public void testSerializable_enableSerializable_isSerializableReturnsTrue() {
        mockSettings.serializable();

        assertTrue(mockSettings.isSerializable());
        assertNotNull(mockSettings.getExtraInterfaces());
        assertEquals(1, mockSettings.getExtraInterfaces().length);
        assertEquals(Serializable.class, mockSettings.getExtraInterfaces()[0]);
    }

    // Tests setting valid extra interfaces
    @Test
    public void testExtraInterfaces_validInterfaces_storesInterfacesAndNotSerializable() {
        mockSettings.extraInterfaces(List.class, Set.class);

        Class<?>[] interfaces = mockSettings.getExtraInterfaces();
        assertNotNull(interfaces);
        assertEquals(2, interfaces.length);
        assertEquals(List.class, interfaces[0]);
        assertEquals(Set.class, interfaces[1]);
        assertFalse(mockSettings.isSerializable());
    }

    // Tests extra interfaces including Serializable interface
    @Test
    public void testExtraInterfaces_includingSerializable_isSerializableReturnsTrue() {
        mockSettings.extraInterfaces(List.class, Serializable.class);

        assertTrue(mockSettings.isSerializable());
    }

    // Tests exception path when extraInterfaces receives null array
    @Test(expected = MockitoException.class)
    public void testExtraInterfaces_nullArray_throwsException() {
        mockSettings.extraInterfaces((Class<?>[]) null);
    }

    // Tests exception path when extraInterfaces receives empty array
    @Test(expected = MockitoException.class)
    public void testExtraInterfaces_emptyArray_throwsException() {
        mockSettings.extraInterfaces(new Class<?>[0]);
    }

    // Tests exception path when extraInterfaces contains a null element
    @Test(expected = MockitoException.class)
    public void testExtraInterfaces_nullElementInArray_throwsException() {
        mockSettings.extraInterfaces(List.class, null);
    }

    // Tests exception path when extraInterfaces contains a class that is not an interface
    @Test(expected = MockitoException.class)
    public void testExtraInterfaces_nonInterfaceClass_throwsException() {
        mockSettings.extraInterfaces(List.class, String.class);
    }

    // Tests setting and getting spied instance
    @Test
    public void testSpiedInstance_setInstance_returnsSameInstance() {
        Object spied = new Object();
        mockSettings.spiedInstance(spied);

        assertSame(spied, mockSettings.getSpiedInstance());
    }

    // Tests setting and getting default answer
    @Test
    public void testDefaultAnswer_setAnswer_returnsSameAnswer() {
        Answer<Object> customAnswer = new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                return "test";
            }
        };

        mockSettings.defaultAnswer(customAnswer);

        assertSame(customAnswer, mockSettings.getDefaultAnswer());
    }

    // Tests mock naming with a custom name
    @Test
    public void testInitiateMockName_withCustomName_createsCustomMockName() {
        mockSettings.name("myCustomMock");
        mockSettings.initiateMockName(List.class);

        assertNotNull(mockSettings.getMockName());
        assertEquals("myCustomMock", mockSettings.getMockName().toString());
    }

    // Tests mock naming with default name derived from class to mock
    @Test
    public void testInitiateMockName_withoutCustomName_createsDefaultMockName() {
        mockSettings.initiateMockName(List.class);

        assertNotNull(mockSettings.getMockName());
        assertTrue(mockSettings.getMockName().toString().contains("list"));
    }

    // Tests defaultAnswer exception when null answer is passed
    @Test(expected = MockitoException.class)
    public void testDefaultAnswer_nullAnswer_throwsException() {
        mockSettings.defaultAnswer(null);
    }

    // Tests stubOnly setting and getter
    @Test
    public void testStubOnly_defaultAndEnabled() {
        assertFalse(mockSettings.isStubOnly());
        MockSettings result = mockSettings.stubOnly();
        assertSame(mockSettings, result);
        assertTrue(mockSettings.isStubOnly());
    }

    // Tests verboseLogging method adds listener and returns instance
    @Test
    public void testVerboseLogging_enablesListener() {
        MockSettings result = mockSettings.verboseLogging();
        assertSame(mockSettings, result);
        assertTrue(mockSettings.hasInvocationListeners());
        assertEquals(1, mockSettings.getInvocationListeners().size());
    }

    // Tests invocationListeners with valid listener
    @Test
    public void testInvocationListeners_validListener_addsToList() {
        InvocationListener listener = new InvocationListener() {
            public void reportInvocation(MethodInvocationReport methodInvocationReport) {
            }
        };

        MockSettings result = mockSettings.invocationListeners(listener);
        assertSame(mockSettings, result);
        assertTrue(mockSettings.hasInvocationListeners());
        assertEquals(1, mockSettings.getInvocationListeners().size());
        assertSame(listener, mockSettings.getInvocationListeners().get(0));
    }

    // Tests invocationListeners exception when null array is passed
    @Test(expected = MockitoException.class)
    public void testInvocationListeners_nullArray_throwsException() {
        mockSettings.invocationListeners((InvocationListener[]) null);
    }

    // Tests invocationListeners exception when empty array is passed
    @Test(expected = MockitoException.class)
    public void testInvocationListeners_emptyArray_throwsException() {
        mockSettings.invocationListeners(new InvocationListener[0]);
    }

    // Tests invocationListeners exception when array contains null element
    @Test(expected = MockitoException.class)
    public void testInvocationListeners_nullElement_throwsException() {
        mockSettings.invocationListeners(new InvocationListener[]{null});
    }

    // Tests fluent chaining returns same MockSettings instance
    @Test
    public void testFluentChaining() {
        assertSame(mockSettings, mockSettings.name("mock"));
        assertSame(mockSettings, mockSettings.serializable());
        assertSame(mockSettings, mockSettings.spiedInstance(new Object()));
        assertSame(mockSettings, mockSettings.extraInterfaces(List.class));
    }
}