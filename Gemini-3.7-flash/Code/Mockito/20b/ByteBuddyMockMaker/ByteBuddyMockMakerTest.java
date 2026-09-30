package org.mockito.internal.creation.bytebuddy;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.creation.settings.CreationSettings;
import org.mockito.internal.handler.MockHandlerFactory;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.MockHandler;
import org.mockito.mock.SerializableMode;
import org.mockito.plugins.MockMaker;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class ByteBuddyMockMakerTest {

    private ByteBuddyMockMaker mockMaker;

    public interface SampleInterface {
        void doWork();
    }

    public interface ExtraInterface {
        void extraWork();
    }

    public static class SampleClass {
        public String getMessage() {
            return "message";
        }
    }

    public static final class FinalSampleClass {
        public void execute() {
        }
    }

    public static abstract class SampleAbstractClass {
        public abstract void execute();
    }

    public static class SampleClassWithConstructor {
        private final String value;

        public SampleClassWithConstructor(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private static class CustomNonInternalMockHandler implements MockHandler {
        private static final long serialVersionUID = 1L;

        @Override
        public Object handle(Invocation invocation) throws Throwable {
            return null;
        }
    }

    @Before
    public void setUp() {
        mockMaker = new ByteBuddyMockMaker();
    }

    // Tests normal mock creation for an interface
    @Test
    public void testCreateMock_interfaceType_createsMockInstance() {
        CreationSettings<SampleInterface> settings = new CreationSettings<SampleInterface>();
        settings.setTypeToMock(SampleInterface.class);
        MockHandler handler = MockHandlerFactory.createMockHandler(settings);

        SampleInterface mock = mockMaker.createMock(settings, handler);

        assertNotNull(mock);
        assertTrue(mock instanceof SampleInterface);
        assertTrue(mock instanceof MockMethodInterceptor.MockAccess);
    }

    // Tests normal mock creation for a concrete class
    @Test
    public void testCreateMock_concreteClass_createsMockInstance() {
        CreationSettings<SampleClass> settings = new CreationSettings<SampleClass>();
        settings.setTypeToMock(SampleClass.class);
        MockHandler handler = MockHandlerFactory.createMockHandler(settings);

        SampleClass mock = mockMaker.createMock(settings, handler);

        assertNotNull(mock);
        assertTrue(mock instanceof SampleClass);
        assertTrue(mock instanceof MockMethodInterceptor.MockAccess);
    }

    // Tests mock creation for an abstract class
    @Test
    public void testCreateMock_abstractClass_createsMockInstance() {
        CreationSettings<SampleAbstractClass> settings = new CreationSettings<SampleAbstractClass>();
        settings.setTypeToMock(SampleAbstractClass.class);
        MockHandler handler = MockHandlerFactory.createMockHandler(settings);

        SampleAbstractClass mock = mockMaker.createMock(settings, handler);

        assertNotNull(mock);
        assertTrue(mock instanceof SampleAbstractClass);
    }

    // Tests mock creation for a class without a default constructor
    @Test
    public void testCreateMock_classWithConstructorParameters_createsMockInstance() {
        CreationSettings<SampleClassWithConstructor> settings = new CreationSettings<SampleClassWithConstructor>();
        settings.setTypeToMock(SampleClassWithConstructor.class);
        MockHandler handler = MockHandlerFactory.createMockHandler(settings);

        SampleClassWithConstructor mock = mockMaker.createMock(settings, handler);

        assertNotNull(mock);
        assertTrue(mock instanceof SampleClassWithConstructor);
    }

    // Tests mock creation with extra interfaces
    @Test
    public void testCreateMock_withExtraInterfaces_implementsAllInterfaces() {
        CreationSettings<SampleClass> settings = new CreationSettings<SampleClass>();
        settings.setTypeToMock(SampleClass.class);
        Set<Class<?>> extraInterfaces = new HashSet<Class<?>>();
        extraInterfaces.add(ExtraInterface.class);
        settings.setExtraInterfaces(extraInterfaces);
        MockHandler handler = MockHandlerFactory.createMockHandler(settings);

        SampleClass mock = mockMaker.createMock(settings, handler);

        assertNotNull(mock);
        assertTrue(mock instanceof SampleClass);
        assertTrue(mock instanceof ExtraInterface);
    }

    // Tests mock creation with BASIC serialization mode
    @Test
    public void testCreateMock_serializationBasic_createsMockInstance() {
        CreationSettings<SampleClass> settings = new CreationSettings<SampleClass>();
        settings.setTypeToMock(SampleClass.class);
        settings.setSerializableMode(SerializableMode.BASIC);
        MockHandler handler = MockHandlerFactory.createMockHandler(settings);

        SampleClass mock = mockMaker.createMock(settings, handler);

        assertNotNull(mock);
        assertTrue(mock instanceof SampleClass);
        assertTrue(mock instanceof Serializable);
    }

    // Tests exception path when serialization mode across classloaders is requested
    @Test(expected = MockitoException.class)
    public void testCreateMock_serializationAcrossClassloaders_throwsMockitoException() {
        CreationSettings<SampleClass> settings = new CreationSettings<SampleClass>();
        settings.setTypeToMock(SampleClass.class);
        settings.setSerializableMode(SerializableMode.ACROSS_CLASSLOADERS);
        MockHandler handler = MockHandlerFactory.createMockHandler(settings);

        mockMaker.createMock(settings, handler);
    }

    // Tests exception path when handler is not an InternalMockHandler
    @Test(expected = MockitoException.class)
    public void testCreateMock_nonInternalMockHandler_throwsMockitoException() {
        CreationSettings<SampleClass> settings = new CreationSettings<SampleClass>();
        settings.setTypeToMock(SampleClass.class);
        CustomNonInternalMockHandler handler = new CustomNonInternalMockHandler();

        mockMaker.createMock(settings, handler);
    }

    // Tests getHandler with non-mock object returns null
    @Test
    public void testGetHandler_nonMockObject_returnsNull() {
        MockHandler handler = mockMaker.getHandler("a regular string");
        assertNull(handler);
    }

    // Tests getHandler with null input returns null
    @Test
    public void testGetHandler_nullInput_returnsNull() {
        MockHandler handler = mockMaker.getHandler(null);
        assertNull(handler);
    }

    // Tests getHandler with a valid mock created by the maker
    @Test
    public void testGetHandler_validMock_returnsAssociatedHandler() {
        CreationSettings<SampleClass> settings = new CreationSettings<SampleClass>();
        settings.setTypeToMock(SampleClass.class);
        MockHandler handler = MockHandlerFactory.createMockHandler(settings);

        SampleClass mock = mockMaker.createMock(settings, handler);
        MockHandler resultHandler = mockMaker.getHandler(mock);

        assertNotNull(resultHandler);
        assertSame(handler, resultHandler);
    }

    // Tests resetting handler on a valid mock
    @Test
    public void testResetMock_validMock_replacesHandler() {
        CreationSettings<SampleClass> settings = new CreationSettings<SampleClass>();
        settings.setTypeToMock(SampleClass.class);
        MockHandler initialHandler = MockHandlerFactory.createMockHandler(settings);
        SampleClass mock = mockMaker.createMock(settings, initialHandler);

        MockHandler newHandler = MockHandlerFactory.createMockHandler(settings);
        mockMaker.resetMock(mock, newHandler, settings);

        MockHandler resultHandler = mockMaker.getHandler(mock);
        assertSame(newHandler, resultHandler);
    }

    // Tests resetting mock with a non-internal mock handler throws exception
    @Test(expected = MockitoException.class)
    public void testResetMock_nonInternalMockHandler_throwsMockitoException() {
        CreationSettings<SampleClass> settings = new CreationSettings<SampleClass>();
        settings.setTypeToMock(SampleClass.class);
        MockHandler initialHandler = MockHandlerFactory.createMockHandler(settings);
        SampleClass mock = mockMaker.createMock(settings, initialHandler);

        CustomNonInternalMockHandler nonInternalHandler = new CustomNonInternalMockHandler();
        mockMaker.resetMock(mock, nonInternalHandler, settings);
    }

    // Tests isTypeMockable for a mockable class
    @Test
    public void testIsTypeMockable_mockableClass_returnsMockable() {
        MockMaker.TypeMockability mockability = mockMaker.isTypeMockable(SampleClass.class);
        assertNotNull(mockability);
        assertTrue(mockability.mockable());
        assertEquals("", mockability.nonMockableReason());
    }

    // Tests isTypeMockable for an interface
    @Test
    public void testIsTypeMockable_interface_returnsMockable() {
        MockMaker.TypeMockability mockability = mockMaker.isTypeMockable(SampleInterface.class);
        assertNotNull(mockability);
        assertTrue(mockability.mockable());
        assertEquals("", mockability.nonMockableReason());
    }

    // Tests isTypeMockable for a primitive type
    @Test
    public void testIsTypeMockable_primitiveType_returnsNotMockable() {
        MockMaker.TypeMockability mockability = mockMaker.isTypeMockable(int.class);
        assertNotNull(mockability);
        assertFalse(mockability.mockable());
        assertNotNull(mockability.nonMockableReason());
        assertFalse(mockability.nonMockableReason().isEmpty());
    }

    // Tests isTypeMockable for a final class
    @Test
    public void testIsTypeMockable_finalClass_returnsNotMockable() {
        MockMaker.TypeMockability mockability = mockMaker.isTypeMockable(FinalSampleClass.class);
        assertNotNull(mockability);
        assertFalse(mockability.mockable());
        assertNotNull(mockability.nonMockableReason());
        assertFalse(mockability.nonMockableReason().isEmpty());
    }
}