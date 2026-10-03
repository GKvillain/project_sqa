package com.fasterxml.jackson.databind.deser.std;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;

public class StdValueInstantiatorTest {

    // Helper to create a basic config and type for testing
    private DeserializationConfig createConfig() {
        // Minimal setup; in real Defects4J context ObjectMapper provides this
        return new ObjectMapper().getDeserializationConfig();
    }

    private JavaType createType(Class<?> clazz) {
        return TypeFactory.defaultInstance().constructType(clazz);
    }

    // Helper: returns a simple non-null AnnotatedWithParams stub for testing
    private AnnotatedWithParams createDummyCreator() {
        // We create an anonymous subclass; since it's abstract, use a minimal one
        // In Defects4J we may use TestClass defined in test sources; but here we just use
        // a simple concrete inner subclass of AnnotatedWithParams.
        return new AnnotatedWithParams(null, null, null) {
            private static final long serialVersionUID = 1L;
            @Override
            public Object call() throws Exception {
                return new Object();
            }
            @Override
            public Object call(Object[] args) throws Exception {
                return new Object();
            }
            @Override
            public Object call1(Object arg) throws Exception {
                return new Object();
            }
            @Override
            public Class<?> getDeclaringClass() {
                return Object.class;
            }
            @Override
            public AnnotatedParameter getParameter(int index) {
                return null;
            }
            @Override
            public int getParameterCount() {
                return 0;
            }
        };
    }

    // Helper to create a creator that returns a fixed value
    private AnnotatedWithParams createReturningCreator(final Object value) {
        return new AnnotatedWithParams(null, null, null) {
            private static final long serialVersionUID = 1L;
            @Override
            public Object call() throws Exception {
                return value;
            }
            @Override
            public Object call(Object[] args) throws Exception {
                return value;
            }
            @Override
            public Object call1(Object arg) throws Exception {
                return value;
            }
            @Override
            public Class<?> getDeclaringClass() {
                return Object.class;
            }
            @Override
            public AnnotatedParameter getParameter(int index) {
                return null;
            }
            @Override
            public int getParameterCount() {
                return 0;
            }
        };
    }

    // ------------------------------------------------
    // Constructor Tests
    // ------------------------------------------------

    // Tests that deprecated constructor sets proper valueTypeDesc for non-null class
    @Test
    public void testConstructorDeprecatedNonNullClass_setsValueTypeDesc() {
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), String.class);
        assertNotNull(inst.getValueTypeDesc());
        assertTrue(inst.getValueTypeDesc().contains("java.lang.String"));
        assertEquals(String.class, inst.getValueClass());
    }

    // Tests deprecated constructor with null class
    @Test
    public void testConstructorDeprecatedNullClass_usesFallback() {
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), (Class<?>) null);
        assertEquals("UNKNOWN TYPE", inst.getValueTypeDesc());
        assertEquals(Object.class, inst.getValueClass());
    }

    // Tests JavaType constructor with null type
    @Test
    public void testConstructorJavaTypeNullType_usesFallback() {
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), (JavaType) null);
        assertEquals("UNKNOWN TYPE", inst.getValueTypeDesc());
        assertEquals(Object.class, inst.getValueClass());
    }

    // Tests JavaType constructor with valid type
    @Test
    public void testConstructorJavaTypeNonNullType_setsCorrectTypes() {
        JavaType type = createType(Integer.class);
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), type);
        assertEquals(type.toString(), inst.getValueTypeDesc());
        assertEquals(Integer.class, inst.getValueClass());
    }

    // Tests copy constructor copies all fields
    @Test
    public void testCopyConstructor_copiesAllFields() {
        StdValueInstantiator original = new StdValueInstantiator(createConfig(), String.class);
        original.configureFromStringCreator(createDummyCreator());
        original.configureFromIntCreator(createDummyCreator());

        StdValueInstantiator copy = new StdValueInstantiator(original);

        assertEquals(original.getValueTypeDesc(), copy.getValueTypeDesc());
        assertEquals(original.getValueClass(), copy.getValueClass());
        assertTrue(copy.canCreateFromString());
        assertTrue(copy.canCreateFromInt());
    }

    // ------------------------------------------------
    // Tests for configureFromObjectSettings and metadata
    // ------------------------------------------------

    // Tests that after configureFromObjectSettings, canCreateUsingDefault returns true
    @Test
    public void testConfigureFromObjectSettings_withDefaultCreator_canCreateDefault() {
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), String.class);
        assertFalse(inst.canCreateUsingDefault());
        inst.configureFromObjectSettings(createDummyCreator(), null, null, null, null, null);
        assertTrue(inst.canCreateUsingDefault());
    }

    // Tests that after configureFromObjectSettings with delegate, canCreateUsingDelegate returns true
    @Test
    public void testConfigureFromObjectSettings_withDelegateType_canCreateDelegate() {
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), String.class);
        assertFalse(inst.canCreateUsingDelegate());
        JavaType delegateType = createType(Object.class);
        inst.configureFromObjectSettings(null, createDummyCreator(), delegateType, null, null, null);
        assertTrue(inst.canCreateUsingDelegate());
    }

    // Tests that after configureFromObjectSettings with withArgsCreator, canCreateFromObjectWith returns true
    @Test
    public void testConfigureFromObjectSettings_withWithArgsCreator_canCreateFromObjectWith() {
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), String.class);
        assertFalse(inst.canCreateFromObjectWith());
        inst.configureFromObjectSettings(null, null, null, null, createDummyCreator(), new SettableBeanProperty[0]);
        assertTrue(inst.canCreateFromObjectWith());
    }

    // ------------------------------------------------
    // Tests for configureFromArraySettings
    // ------------------------------------------------

    // Tests that after configureFromArraySettings, canCreateUsingArrayDelegate returns true
    @Test
    public void testConfigureFromArraySettings_withArrayDelegateType_canCreateArrayDelegate() {
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), String.class);
        assertFalse(inst.canCreateUsingArrayDelegate());
        JavaType arrayDelegateType = createType(Object[].class);
        inst.configureFromArraySettings(createDummyCreator(), arrayDelegateType, null);
        assertTrue(inst.canCreateUsingArrayDelegate());
    }

    // ------------------------------------------------
    // Tests for scalar creator configuration and metadata
    // ------------------------------------------------

    @Test
    public void testConfigureFromStringCreator_setsCanCreateFromString() {
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), String.class);
        assertFalse(inst.canCreateFromString());
        inst.configureFromStringCreator(createDummyCreator());
        assertTrue(inst.canCreateFromString());
    }

    @Test
    public void testConfigureFromIntCreator_setsCanCreateFromInt() {
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), String.class);
        assertFalse(inst.canCreateFromInt());
        inst.configureFromIntCreator(createDummyCreator());
        assertTrue(inst.canCreateFromInt());
    }

    @Test
    public void testConfigureFromLongCreator_setsCanCreateFromLong() {
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), String.class);
        assertFalse(inst.canCreateFromLong());
        inst.configureFromLongCreator(createDummyCreator());
        assertTrue(inst.canCreateFromLong());
    }

    @Test
    public void testConfigureFromDoubleCreator_setsCanCreateFromDouble() {
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), String.class);
        assertFalse(inst.canCreateFromDouble());
        inst.configureFromDoubleCreator(createDummyCreator());
        assertTrue(inst.canCreateFromDouble());
    }

    @Test
    public void testConfigureFromBooleanCreator_setsCanCreateFromBoolean() {
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), String.class);
        assertFalse(inst.canCreateFromBoolean());
        inst.configureFromBooleanCreator(createDummyCreator());
        assertTrue(inst.canCreateFromBoolean());
    }

    // ------------------------------------------------
    // Tests for getDelegateType and getArrayDelegateType
    // ------------------------------------------------

    @Test
    public void testGetDelegateType_returnsConfiguredType() {
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), String.class);
        assertNull(inst.getDelegateType(createConfig()));
        JavaType delegateType = createType(Integer.class);
        inst.configureFromObjectSettings(null, createDummyCreator(), delegateType, null, null, null);
        assertEquals(delegateType, inst.getDelegateType(createConfig()));
    }

    @Test
    public void testGetArrayDelegateType_returnsConfiguredType() {
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), String.class);
        assertNull(inst.getArrayDelegateType(createConfig()));
        JavaType arrayDelegateType = createType(Integer[].class);
        inst.configureFromArraySettings(createDummyCreator(), arrayDelegateType, null);
        assertEquals(arrayDelegateType, inst.getArrayDelegateType(createConfig()));
    }

    // ------------------------------------------------
    // Tests for getFromObjectArguments
    // ------------------------------------------------

    @Test
    public void testGetFromObjectArguments_returnsConfiguredArgs() {
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), String.class);
        assertNull(inst.getFromObjectArguments(createConfig()));
        SettableBeanProperty[] args = new SettableBeanProperty[0];
        inst.configureFromObjectSettings(null, null, null, null, createDummyCreator(), args);
        assertSame(args, inst.getFromObjectArguments(createConfig()));
    }

    // ------------------------------------------------
    // Tests for getIncompleteParameter and configureIncompleteParameter
    // ------------------------------------------------

    @Test
    public void testGetIncompleteParameter_returnsConfiguredParam() {
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), String.class);
        assertNull(inst.getIncompleteParameter());
        // We cannot easily create AnnotatedParameter without complex setup; we just test null
        // For coverage, we can at least ensure no exception
        inst.configureIncompleteParameter(null);
        assertNull(inst.getIncompleteParameter());
    }

    // ------------------------------------------------
    // Tests for exception handling helper (rewrapCtorProblem)
    // ------------------------------------------------
    // rewrapCtorProblem is protected; we test indirectly via createUsingDefault with exception-throwing creator

    @Test(expected = JsonMappingException.class)
    public void testCreateUsingDefault_whenCreatorThrowsException_wrapsInJsonMappingException() throws IOException {
        // Create a creator that throws InvocationTargetException
        AnnotatedWithParams throwingCreator = new AnnotatedWithParams(null, null, null) {
            private static final long serialVersionUID = 1L;
            @Override
            public Object call() throws Exception {
                throw new InvocationTargetException(new IllegalArgumentException("test"), "test");
            }
            @Override
            public Object call(Object[] args) throws Exception {
                return null;
            }
            @Override
            public Object call1(Object arg) throws Exception {
                return null;
            }
            @Override
            public Class<?> getDeclaringClass() {
                return Object.class;
            }
            @Override
            public AnnotatedParameter getParameter(int index) {
                return null;
            }
            @Override
            public int getParameterCount() {
                return 0;
            }
        };

        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), String.class);
        inst.configureFromObjectSettings(throwingCreator, null, null, null, null, null);
        // This should throw JsonMappingException via handleInstantiationProblem
        // Since handleInstantiationProblem is implemented by DeserializationContext, we need real ctxt
        // Using ObjectMapper readValue to trigger: but easier: we just verify that exception path is taken.
        // For this test we use a real context from ObjectMapper
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        // Reset context for safe usage
        inst.createUsingDefault(ctxt);
    }

    // ------------------------------------------------
    // Edge cases: null configuration
    // ------------------------------------------------

    // Tests that getValueTypeDesc does not return null
    @Test
    public void testGetValueTypeDesc_neverNull() {
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), (Class<?>) null);
        assertNotNull(inst.getValueTypeDesc());
    }

    // Tests that getValueClass never returns null
    @Test
    public void testGetValueClass_neverNull() {
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), (Class<?>) null);
        assertNotNull(inst.getValueClass());
    }

    // ================================================
    // New tests covering creation methods
    // ================================================

    // Test createUsingDefault with a successful default creator
    @Test
    public void testCreateUsingDefault_successful() throws IOException {
        Object expected = new Object();
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), Object.class);
        inst.configureFromObjectSettings(createReturningCreator(expected), null, null, null, null, null);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        assertSame(expected, inst.createUsingDefault(ctxt));
    }

    // Test createUsingDefault when no creator configured throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testCreateUsingDefault_noDefaultCreator_throwsJsonMappingException() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), String.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        inst.createUsingDefault(ctxt);
    }

    // Test createUsingDelegate with a successful delegate creator
    @Test
    public void testCreateUsingDelegate_successful() throws IOException {
        Object delegate = new Object();
        Object expected = new Object();
        AnnotatedWithParams delegateCreator = createReturningCreator(expected);
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), Object.class);
        inst.configureFromObjectSettings(null, delegateCreator, createType(Object.class), null, null, null);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        assertSame(expected, inst.createUsingDelegate(ctxt, delegate));
    }

    // Test createUsingArrayDelegate with a successful array delegate creator
    @Test
    public void testCreateUsingArrayDelegate_successful() throws IOException {
        Object[] arrayDelegate = new Object[0];
        Object expected = new Object();
        AnnotatedWithParams arrayDelegateCreator = createReturningCreator(expected);
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), Object.class);
        inst.configureFromArraySettings(arrayDelegateCreator, createType(Object[].class), null);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        assertSame(expected, inst.createUsingArrayDelegate(ctxt, arrayDelegate));
    }

    // Test createFromString with a successful string creator
    @Test
    public void testCreateFromString_successful() throws IOException {
        Object expected = new Object();
        AnnotatedWithParams stringCreator = createReturningCreator(expected);
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), Object.class);
        inst.configureFromStringCreator(stringCreator);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        assertSame(expected, inst.createFromString(ctxt, "test"));
    }

    // Test createFromInt with a successful int creator
    @Test
    public void testCreateFromInt_successful() throws IOException {
        Object expected = new Object();
        AnnotatedWithParams intCreator = createReturningCreator(expected);
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), Object.class);
        inst.configureFromIntCreator(intCreator);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        assertSame(expected, inst.createFromInt(ctxt, 42));
    }

    // Test createFromLong with a successful long creator
    @Test
    public void testCreateFromLong_successful() throws IOException {
        Object expected = new Object();
        AnnotatedWithParams longCreator = createReturningCreator(expected);
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), Object.class);
        inst.configureFromLongCreator(longCreator);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        assertSame(expected, inst.createFromLong(ctxt, 42L));
    }

    // Test createFromDouble with a successful double creator
    @Test
    public void testCreateFromDouble_successful() throws IOException {
        Object expected = new Object();
        AnnotatedWithParams doubleCreator = createReturningCreator(expected);
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), Object.class);
        inst.configureFromDoubleCreator(doubleCreator);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        assertSame(expected, inst.createFromDouble(ctxt, 3.14));
    }

    // Test createFromBoolean with a successful boolean creator
    @Test
    public void testCreateFromBoolean_successful() throws IOException {
        Object expected = new Object();
        AnnotatedWithParams booleanCreator = createReturningCreator(expected);
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), Object.class);
        inst.configureFromBooleanCreator(booleanCreator);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        assertSame(expected, inst.createFromBoolean(ctxt, true));
    }

    // Test createFromObjectWith with a successful creator and arguments
    @Test
    public void testCreateFromObjectWith_successful() throws IOException {
        Object expected = new Object();
        AnnotatedWithParams withArgsCreator = createReturningCreator(expected);
        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), Object.class);
        inst.configureFromObjectSettings(null, null, null, null, withArgsCreator, new SettableBeanProperty[0]);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object[] args = new Object[0];
        assertSame(expected, inst.createFromObjectWith(ctxt, args));
    }

    // Test createFromString with a creator that throws an exception gets wrapped
    @Test(expected = JsonMappingException.class)
    public void testCreateFromString_whenCreatorThrowsException_wrapsInJsonMappingException() throws IOException {
        AnnotatedWithParams throwingStringCreator = new AnnotatedWithParams(null, null, null) {
            private static final long serialVersionUID = 1L;
            @Override
            public Object call() throws Exception {
                throw new IllegalStateException("test");
            }
            @Override
            public Object call(Object[] args) throws Exception {
                throw new IllegalStateException("test");
            }
            @Override
            public Object call1(Object arg) throws Exception {
                throw new IllegalStateException("test");
            }
            @Override
            public Class<?> getDeclaringClass() {
                return Object.class;
            }
            @Override
            public AnnotatedParameter getParameter(int index) {
                return null;
            }
            @Override
            public int getParameterCount() {
                return 0;
            }
        };

        StdValueInstantiator inst = new StdValueInstantiator(createConfig(), Object.class);
        inst.configureFromStringCreator(throwingStringCreator);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        inst.createFromString(ctxt, "test");
    }
}