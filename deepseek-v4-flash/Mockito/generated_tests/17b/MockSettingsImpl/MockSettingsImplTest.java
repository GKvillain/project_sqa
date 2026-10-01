package org.mockito.internal.creation;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.Arrays;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class MockSettingsImplTest {

    // Helper constants for test data
    private static final Class<?> INTERFACE_CLASS = Runnable.class;
    private static final Class<?> OTHER_INTERFACE_CLASS = Comparable.class;
    private static final Class<?> NON_INTERFACE_CLASS = String.class;

    // --- Extra interfaces validation: invalid inputs expecting exception ---

    @Test(expected = RuntimeException.class)
    public void testExtraInterfaces_nullArray_throwsException() {
        new MockSettingsImpl().extraInterfaces((Class<?>[]) null);
    }

    @Test(expected = RuntimeException.class)
    public void testExtraInterfaces_emptyArray_throwsException() {
        new MockSettingsImpl().extraInterfaces();
    }

    @Test(expected = RuntimeException.class)
    public void testExtraInterfaces_nullElement_throwsException() {
        new MockSettingsImpl().extraInterfaces(new Class<?>[] { null });
    }

    @Test(expected = RuntimeException.class)
    public void testExtraInterfaces_nonInterfaceClass_throwsException() {
        new MockSettingsImpl().extraInterfaces(NON_INTERFACE_CLASS);
    }

    // --- Extra interfaces: valid inputs ---

    @Test
    public void testExtraInterfaces_validInterface_setsExtraInterfaces() {
        MockSettingsImpl settings = new MockSettingsImpl();
        // Initially null
        assertNull("extraInterfaces should be null before setting", settings.getExtraInterfaces());
        // Set and verify chaining
        assertSame(settings, settings.extraInterfaces(INTERFACE_CLASS));
        // Verify stored value
        Class<?>[] result = settings.getExtraInterfaces();
        assertNotNull(result);
        assertEquals(1, result.length);
        assertSame(INTERFACE_CLASS, result[0]);
    }

    @Test
    public void testExtraInterfaces_multipleValidInterfaces_setsAll() {
        MockSettingsImpl settings = new MockSettingsImpl();
        Class<?>[] expected = new Class<?>[] { INTERFACE_CLASS, OTHER_INTERFACE_CLASS };
        assertSame(settings, settings.extraInterfaces(expected));
        Class<?>[] result = settings.getExtraInterfaces();
        assertNotNull(result);
        assertArrayEquals(expected, result);
    }

    // --- Extra interfaces: additional edge cases ---

    @Test
    public void testExtraInterfaces_multipleIncludingSerializable_setsAllAndSerializable() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(INTERFACE_CLASS, Serializable.class);
        assertTrue("isSerializable should be true when Serializable is included",
                settings.isSerializable());
        Class<?>[] extra = settings.getExtraInterfaces();
        assertNotNull(extra);
        assertEquals(2, extra.length);
        assertTrue(Arrays.asList(extra).contains(INTERFACE_CLASS));
        assertTrue(Arrays.asList(extra).contains(Serializable.class));
    }

    @Test
    public void testExtraInterfaces_calledTwice_overwritesPrevious() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(INTERFACE_CLASS);
        settings.extraInterfaces(OTHER_INTERFACE_CLASS);
        Class<?>[] extra = settings.getExtraInterfaces();
        assertEquals(1, extra.length);
        assertSame(OTHER_INTERFACE_CLASS, extra[0]);
    }

    @Test
    public void testExtraInterfaces_overwriteRemovesSerializableFlag() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(Serializable.class);
        assertTrue(settings.isSerializable());
        settings.extraInterfaces(INTERFACE_CLASS);
        assertFalse("isSerializable should be false after overwriting without Serializable",
                settings.isSerializable());
        Class<?>[] extra = settings.getExtraInterfaces();
        assertEquals(1, extra.length);
        assertSame(INTERFACE_CLASS, extra[0]);
    }

    // --- Serializable method and related behavior ---

    @Test
    public void testSerializable_setsSerializableInterface() {
        MockSettingsImpl settings = new MockSettingsImpl();
        assertFalse("Initially isSerializable should be false", settings.isSerializable());
        assertSame(settings, settings.serializable());
        assertTrue("After serializable(), isSerializable should be true", settings.isSerializable());
        Class<?>[] extra = settings.getExtraInterfaces();
        assertNotNull(extra);
        assertTrue("extraInterfaces should contain Serializable",
                   Arrays.asList(extra).contains(Serializable.class));
    }

    @Test
    public void testExtraInterfaces_serializableDirectly_setsSerializable() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(Serializable.class);
        assertTrue(settings.isSerializable());
    }

    @Test
    public void testIsSerializable_extraInterfacesNull_returnsFalse() {
        MockSettingsImpl settings = new MockSettingsImpl();
        assertFalse(settings.isSerializable());
    }

    @Test
    public void testIsSerializable_extraInterfacesWithoutSerializable_returnsFalse() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(INTERFACE_CLASS);
        assertFalse(settings.isSerializable());
    }

    @Test
    public void testSerializable_calledTwice_doesNotChangeState() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.serializable();
        settings.serializable();
        assertTrue(settings.isSerializable());
        Class<?>[] extra = settings.getExtraInterfaces();
        // Should still contain Serializable exactly once
        assertEquals(1, extra.length);
        assertSame(Serializable.class, extra[0]);
    }

    // --- Name and MockName ---

    @Test
    public void testName_setsNameAndInitiateMockName_createsMockName() {
        MockSettingsImpl settings = new MockSettingsImpl();
        // Initially mockName is null
        assertNull("MockName should be null before initiateMockName", settings.getMockName());
        String testName = "myMock";
        // Set name, verify chaining
        assertSame(settings, settings.name(testName));
        settings.initiateMockName(String.class);
        // After initiation, mockName must not be null
        assertNotNull("MockName must be set after initiateMockName", settings.getMockName());
    }

    @Test
    public void testInitiateMockName_withoutName_createsMockName() {
        MockSettingsImpl settings = new MockSettingsImpl();
        assertNull("MockName should be null initially", settings.getMockName());
        settings.initiateMockName(Object.class);
        assertNotNull(settings.getMockName());
    }

    @Test(expected = RuntimeException.class)
    public void testName_withNull_throwsException() {
        new MockSettingsImpl().name(null);
    }

    @Test
    public void testName_withEmptyString_works() {
        MockSettingsImpl settings = new MockSettingsImpl();
        assertSame(settings, settings.name(""));
        settings.initiateMockName(String.class);
        assertNotNull(settings.getMockName());
    }

    @Test
    public void testInitiateMockName_calledTwice_doesNotOverride() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.name("first");
        settings.initiateMockName(String.class);
        String firstName = settings.getMockName().toString();
        settings.initiateMockName(Integer.class);
        // The mock name should remain the one set originally
        assertEquals(firstName, settings.getMockName().toString());
    }

    // --- Spied instance ---

    @Test
    public void testSpiedInstance_setsAndReturns() {
        MockSettingsImpl settings = new MockSettingsImpl();
        assertNull("spiedInstance should be null initially", settings.getSpiedInstance());
        Object obj = new Object();
        assertSame(settings, settings.spiedInstance(obj));
        assertSame(obj, settings.getSpiedInstance());
    }

    @Test
    public void testSpiedInstance_withNull_setsNull() {
        MockSettingsImpl settings = new MockSettingsImpl();
        assertSame(settings, settings.spiedInstance(null));
        assertNull(settings.getSpiedInstance());
    }

    // --- Default answer ---

    @Test
    public void testDefaultAnswer_setsAndReturns() {
        MockSettingsImpl settings = new MockSettingsImpl();
        assertNull("defaultAnswer should be null initially", settings.getDefaultAnswer());
        assertSame(settings, settings.defaultAnswer(null));
        assertNull(settings.getDefaultAnswer());
    }

    @Test
    public void testDefaultAnswer_withMockAnswer_setsAndReturns() {
        MockSettingsImpl settings = new MockSettingsImpl();
        Answer<?> answer = new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                return null;
            }
        };
        assertSame(settings, settings.defaultAnswer(answer));
        assertSame(answer, settings.getDefaultAnswer());
    }
}