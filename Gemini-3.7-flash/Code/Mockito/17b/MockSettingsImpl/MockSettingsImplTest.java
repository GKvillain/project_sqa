package org.mockito.internal.creation;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.listeners.InvocationListener;
import org.mockito.listeners.MethodInvocationReport;
import org.mockito.stubbing.Answer;
import org.mockito.invocation.InvocationOnMock;

public class MockSettingsImplTest {

    private MockSettingsImpl mockSettings;

    @Before
    public void setUp() {
        mockSettings = new MockSettingsImpl();
    }

    // Tests default state of isSerializable
    @Test
    public void testIsSerializable_defaultState_returnsFalse() {
        assertFalse(mockSettings.isSerializable());
        assertNull(mockSettings.getExtraInterfaces());
    }

    // Tests serializable method enables serialization and sets extra interface
    @Test
    public void testSerializable_whenCalled_enablesSerializable() {
        mockSettings.serializable();

        assertTrue(mockSettings.isSerializable());
        assertNotNull(mockSettings.getExtraInterfaces());
        assertEquals(1, mockSettings.getExtraInterfaces().length);
        assertEquals(Serializable.class, mockSettings.getExtraInterfaces()[0]);
    }

    // Tests setting valid extra interfaces
    @Test
    public void testExtraInterfaces_validInterfaces_storesInterfaces() {
        mockSettings.extraInterfaces(List.class, Set.class);

        Class<?>[] interfaces = mockSettings.getExtraInterfaces();
        assertNotNull(interfaces);
        assertEquals(2, interfaces.length);
        assertEquals(List.class, interfaces[0]);
        assertEquals(Set.class, interfaces[1]);
        assertFalse(mockSettings.isSerializable());
    }

    // Tests extraInterfaces including Serializable returns true for isSerializable
    @Test
    public void testIsSerializable_extraInterfacesContainsSerializable_returnsTrue() {
        mockSettings.extraInterfaces(List.class, Serializable.class);

        assertTrue(mockSettings.isSerializable());
    }

    // Tests extraInterfaces with null array throws exception
    @Test(expected = MockitoException.class)
    public void testExtraInterfaces_nullArray_throwsException() {
        mockSettings.extraInterfaces((Class<?>[]) null);
    }

    // Tests extraInterfaces with empty array throws exception
    @Test(expected = MockitoException.class)
    public void testExtraInterfaces_emptyArray_throwsException() {
        mockSettings.extraInterfaces(new Class<?>[0]);
    }

    // Tests extraInterfaces containing null element throws exception
    @Test(expected = MockitoException.class)
    public void testExtraInterfaces_nullElement_throwsException() {
        mockSettings.extraInterfaces(List.class, null);
    }

    // Tests extraInterfaces with non-interface class throws exception
    @Test(expected = MockitoException.class)
    public void testExtraInterfaces_nonInterfaceClass_throwsException() {
        mockSettings.extraInterfaces(String.class);
    }

    // Tests setting and getting spied instance
    @Test
    public void testSpiedInstance_validObject_storesInstance() {
        Object spy = new Object();
        mockSettings.spiedInstance(spy);

        assertSame(spy, mockSettings.getSpiedInstance());
    }

    // Tests default answer configuration
    @Test
    public void testDefaultAnswer_validAnswer_storesAnswer() {
        Answer<Object> customAnswer = new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                return "custom";
            }
        };
        mockSettings.defaultAnswer(customAnswer);

        assertSame(customAnswer, mockSettings.getDefaultAnswer());
    }

    // Tests initiateMockName with custom name
    @Test
    public void testInitiateMockName_withCustomName_createsMockName() {
        mockSettings.name("myCustomMock");
        mockSettings.initiateMockName(List.class);

        assertNotNull(mockSettings.getMockName());
        assertEquals("myCustomMock", mockSettings.getMockName().toString());
    }

    // Tests initiateMockName with default/null name
    @Test
    public void testInitiateMockName_withoutCustomName_createsDefaultMockName() {
        mockSettings.initiateMockName(List.class);

        assertNotNull(mockSettings.getMockName());
        assertTrue(mockSettings.getMockName().toString().contains("list"));
    }

    // Tests default stubOnly state is false
    @Test
    public void testIsStubOnly_defaultState_returnsFalse() {
        assertFalse(mockSettings.isStubOnly());
    }

    // Tests stubOnly enables stub-only mode
    @Test
    public void testStubOnly_whenCalled_enablesStubOnly() {
        mockSettings.stubOnly();

        assertTrue(mockSettings.isStubOnly());
    }

    // Tests default answer with null throws exception
    @Test(expected = MockitoException.class)
    public void testDefaultAnswer_nullAnswer_throwsException() {
        mockSettings.defaultAnswer(null);
    }

    // Tests verbose logging adds an invocation listener
    @Test
    public void testVerboseLogging_addsInvocationListener() {
        assertFalse(mockSettings.hasInvocationListeners());

        mockSettings.verboseLogging();

        assertTrue(mockSettings.hasInvocationListeners());
        assertNotNull(mockSettings.getInvocationListeners());
        assertEquals(1, mockSettings.getInvocationListeners().size());
    }

    // Tests configuring valid invocation listeners
    @Test
    public void testInvocationListeners_validListeners_storesListeners() {
        InvocationListener listener = new InvocationListener() {
            public void reportInvocation(MethodInvocationReport methodInvocationReport) {
            }
        };

        mockSettings.invocationListeners(listener);

        assertTrue(mockSettings.hasInvocationListeners());
        assertEquals(1, mockSettings.getInvocationListeners().size());
        assertTrue(mockSettings.getInvocationListeners().contains(listener));
    }

    // Tests invocationListeners with null array throws exception
    @Test(expected = MockitoException.class)
    public void testInvocationListeners_nullArray_throwsException() {
        mockSettings.invocationListeners((InvocationListener[]) null);
    }

    // Tests invocationListeners with empty array throws exception
    @Test(expected = MockitoException.class)
    public void testInvocationListeners_emptyArray_throwsException() {
        mockSettings.invocationListeners(new InvocationListener[0]);
    }

    // Tests invocationListeners containing null element throws exception
    @Test(expected = MockitoException.class)
    public void testInvocationListeners_nullElement_throwsException() {
        InvocationListener listener = new InvocationListener() {
            public void reportInvocation(MethodInvocationReport methodInvocationReport) {
            }
        };

        mockSettings.invocationListeners(listener, null);
    }

    // Tests extraInterfaces ignores duplicate interfaces
    @Test
    public void testExtraInterfaces_duplicateInterfaces_storesDistinctOnly() {
        mockSettings.extraInterfaces(List.class, List.class);

        Class<?>[] interfaces = mockSettings.getExtraInterfaces();
        assertNotNull(interfaces);
        assertEquals(1, interfaces.length);
        assertEquals(List.class, interfaces[0]);
    }
}