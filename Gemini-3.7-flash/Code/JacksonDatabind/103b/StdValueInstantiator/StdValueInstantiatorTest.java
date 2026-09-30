package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class StdValueInstantiatorTest {

    // Tests initialization using JavaType
    @Test
    public void testConstructor_withJavaType_initializesProperly() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        StdValueInstantiator inst = new StdValueInstantiator((DeserializationConfig) null, type);

        assertEquals(String.class, inst.getValueClass());
        assertEquals(type.toString(), inst.getValueTypeDesc());
        assertFalse(inst.canInstantiate());
        assertFalse(inst.canCreateUsingDefault());
        assertFalse(inst.canCreateUsingDelegate());
        assertFalse(inst.canCreateUsingArrayDelegate());
        assertFalse(inst.canCreateFromObjectWith());
        assertFalse(inst.canCreateFromString());
        assertFalse(inst.canCreateFromInt());
        assertFalse(inst.canCreateFromLong());
        assertFalse(inst.canCreateFromDouble());
        assertFalse(inst.canCreateFromBoolean());
    }

    // Tests initialization using null JavaType
    @Test
    public void testConstructor_withNullJavaType_initializesDefaults() {
        StdValueInstantiator inst = new StdValueInstantiator((DeserializationConfig) null, (JavaType) null);

        assertEquals(Object.class, inst.getValueClass());
        assertEquals("UNKNOWN TYPE", inst.getValueTypeDesc());
    }

    // Tests deprecated constructor with Class
    @Test
    public void testConstructor_withClass_initializesProperly() {
        StdValueInstantiator inst = new StdValueInstantiator((DeserializationConfig) null, Integer.class);

        assertEquals(Integer.class, inst.getValueClass());
        assertEquals("java.lang.Integer", inst.getValueTypeDesc());
    }

    // Tests deprecated constructor with null Class
    @Test
    public void testConstructor_withNullClass_initializesDefaults() {
        StdValueInstantiator inst = new StdValueInstantiator((DeserializationConfig) null, (Class<?>) null);

        assertEquals(Object.class, inst.getValueClass());
        assertEquals("UNKNOWN", inst.getValueTypeDesc());
    }

    // Tests copy constructor and configure methods
    @Test
    public void testCopyConstructor_copiesConfiguredCreators() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        StdValueInstantiator src = new StdValueInstantiator((DeserializationConfig) null, type);
        src.configureFromStringCreator(null);
        src.configureFromIntCreator(null);
        src.configureFromLongCreator(null);
        src.configureFromDoubleCreator(null);
        src.configureFromBooleanCreator(null);
        src.configureIncompleteParameter(null);

        StdValueInstantiator copy = new StdValueInstantiator(src);
        assertEquals(src.getValueClass(), copy.getValueClass());
        assertEquals(src.getValueTypeDesc(), copy.getValueTypeDesc());
        assertNull(copy.getIncompleteParameter());
        assertNull(copy.getDefaultCreator());
        assertNull(copy.getDelegateCreator());
        assertNull(copy.getArrayDelegateCreator());
        assertNull(copy.getWithArgsCreator());
    }

    // Tests getters for delegate types and constructor arguments
    @Test
    public void testGetDelegateTypesAndArguments_returnsConfiguredValues() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        JavaType delegateType = TypeFactory.defaultInstance().constructType(Object.class);
        StdValueInstantiator inst = new StdValueInstantiator((DeserializationConfig) null, type);

        inst.configureFromObjectSettings(null, null, delegateType, null, null, null);
        assertEquals(delegateType, inst.getDelegateType(null));
        assertTrue(inst.canCreateUsingDelegate());
        assertTrue(inst.canInstantiate());

        inst.configureFromArraySettings(null, delegateType, null);
        assertEquals(delegateType, inst.getArrayDelegateType(null));
        assertTrue(inst.canCreateUsingArrayDelegate());
        assertNull(inst.getFromObjectArguments(null));
    }

    // Tests createUsingDefault without default creator throws exception
    @Test(expected = JsonMappingException.class)
    public void testCreateUsingDefault_nullCreator_throwsException() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator((DeserializationConfig) null, String.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        inst.createUsingDefault(ctxt);
    }

    // Tests createFromObjectWith without args creator throws exception
    @Test(expected = JsonMappingException.class)
    public void testCreateFromObjectWith_nullCreator_throwsException() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator((DeserializationConfig) null, String.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        inst.createFromObjectWith(ctxt, new Object[] { "test" });
    }

    // Tests createUsingDelegate when no delegate creator configured throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testCreateUsingDelegate_nullCreator_throwsIllegalStateException() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator((DeserializationConfig) null, String.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        inst.createUsingDelegate(ctxt, "value");
    }

    // Tests createUsingArrayDelegate fallback to classic delegate when array delegate creator is null
    @Test(expected = IllegalStateException.class)
    public void testCreateUsingArrayDelegate_nullArrayCreator_fallsBackOrThrows() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator((DeserializationConfig) null, String.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        inst.createUsingArrayDelegate(ctxt, new Object[0]);
    }

    // Tests createFromString fallback when string creator is null
    @Test(expected = JsonMappingException.class)
    public void testCreateFromString_nullCreator_throwsException() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator((DeserializationConfig) null, Integer.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        inst.createFromString(ctxt, "123");
    }

    // Tests createFromInt fallback when no int or long creator configured
    @Test(expected = JsonMappingException.class)
    public void testCreateFromInt_nullCreator_throwsException() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator((DeserializationConfig) null, String.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        inst.createFromInt(ctxt, 42);
    }

    // Tests createFromLong fallback when long creator is null
    @Test(expected = JsonMappingException.class)
    public void testCreateFromLong_nullCreator_throwsException() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator((DeserializationConfig) null, String.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        inst.createFromLong(ctxt, 42L);
    }

    // Tests createFromDouble fallback when double creator is null
    @Test(expected = JsonMappingException.class)
    public void testCreateFromDouble_nullCreator_throwsException() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator((DeserializationConfig) null, String.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        inst.createFromDouble(ctxt, 3.14);
    }

    // Tests createFromBoolean fallback when boolean creator is null
    @Test(expected = JsonMappingException.class)
    public void testCreateFromBoolean_nullCreator_throwsException() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator((DeserializationConfig) null, String.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        inst.createFromBoolean(ctxt, true);
    }

    // Tests wrapException unwraps until JsonMappingException is found
    @Test
    public void testWrapException_withNestedJsonMappingException_returnsNested() {
        StdValueInstantiator inst = new StdValueInstantiator((DeserializationConfig) null, String.class);
        JsonMappingException jme = new JsonMappingException(null, "Mapping error");
        RuntimeException wrapper = new RuntimeException("Outer wrapper", jme);

        JsonMappingException result = inst.wrapException(wrapper);
        assertSame(jme, result);
    }

    // Tests wrapException creates new JsonMappingException when not found in cause chain
    @Test
    public void testWrapException_withoutJsonMappingException_createsNew() {
        StdValueInstantiator inst = new StdValueInstantiator((DeserializationConfig) null, String.class);
        IllegalArgumentException root = new IllegalArgumentException("Root cause message");

        JsonMappingException result = inst.wrapException(root);
        assertNotNull(result);
        assertTrue(result.getMessage().contains("Root cause message"));
        assertSame(root, result.getCause());
    }

    // Tests rewrapCtorProblem unwrapping InvocationTargetException and ExceptionInInitializerError
    @Test
    public void testRewrapCtorProblem_withInvocationTargetException_unwrapsCause() {
        StdValueInstantiator inst = new StdValueInstantiator((DeserializationConfig) null, String.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        IllegalArgumentException targetEx = new IllegalArgumentException("Target issue");
        InvocationTargetException ite = new InvocationTargetException(targetEx);

        JsonMappingException result = inst.rewrapCtorProblem(ctxt, ite);
        assertNotNull(result);
        assertSame(targetEx, result.getCause());
    }

    // Tests unwrapAndWrapException unwraps JsonMappingException from cause chain
    @Test
    public void testUnwrapAndWrapException_withNestedJsonMappingException_returnsNested() {
        StdValueInstantiator inst = new StdValueInstantiator((DeserializationConfig) null, String.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonMappingException jme = new JsonMappingException(null, "Nested mapping error");
        Exception outer = new Exception("Outer", jme);

        JsonMappingException result = inst.unwrapAndWrapException(ctxt, outer);
        assertSame(jme, result);
    }
}