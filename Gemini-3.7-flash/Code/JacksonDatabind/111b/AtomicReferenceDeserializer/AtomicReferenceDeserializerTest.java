package com.fasterxml.jackson.databind.deser.std;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.AccessPattern;

public class AtomicReferenceDeserializerTest {

    private AtomicReferenceDeserializer deserializer;
    private JavaType fullType;

    static class Bean {
        public AtomicReference<String> ref;
    }

    @Before
    public void setUp() {
        fullType = TypeFactory.defaultInstance().constructType(new TypeReference<AtomicReference<String>>() {});
        deserializer = new AtomicReferenceDeserializer(fullType, null, null, null);
    }

    // Tests referenceValue with a non-null content
    @Test
    public void testReferenceValue_nonNullContent_returnsAtomicReferenceContainingValue() {
        String content = "testValue";
        AtomicReference<Object> ref = deserializer.referenceValue(content);

        assertNotNull(ref);
        assertEquals(content, ref.get());
    }

    // Tests referenceValue with null content
    @Test
    public void testReferenceValue_nullContent_returnsAtomicReferenceContainingNull() {
        AtomicReference<Object> ref = deserializer.referenceValue(null);

        assertNotNull(ref);
        assertNull(ref.get());
    }

    // Tests getReferenced with a reference containing a non-null value
    @Test
    public void testGetReferenced_nonNullValue_returnsContainedValue() {
        AtomicReference<Object> ref = new AtomicReference<Object>("innerContent");
        Object result = deserializer.getReferenced(ref);

        assertEquals("innerContent", result);
    }

    // Tests getReferenced with a reference containing null
    @Test
    public void testGetReferenced_nullValue_returnsNull() {
        AtomicReference<Object> ref = new AtomicReference<Object>(null);
        Object result = deserializer.getReferenced(ref);

        assertNull(result);
    }

    // Tests updateReference with a non-null new content
    @Test
    public void testUpdateReference_nonNullContent_updatesAndReturnsSameReference() {
        AtomicReference<Object> ref = new AtomicReference<Object>("oldValue");
        AtomicReference<Object> result = deserializer.updateReference(ref, "newValue");

        assertSame(ref, result);
        assertEquals("newValue", result.get());
    }

    // Tests updateReference with null new content
    @Test
    public void testUpdateReference_nullContent_updatesReferenceToNull() {
        AtomicReference<Object> ref = new AtomicReference<Object>("oldValue");
        AtomicReference<Object> result = deserializer.updateReference(ref, null);

        assertSame(ref, result);
        assertNull(result.get());
    }

    // Tests supportsUpdate with null config
    @Test
    public void testSupportsUpdate_nullConfig_returnsTrue() {
        Boolean supported = deserializer.supportsUpdate((DeserializationConfig) null);

        assertEquals(Boolean.TRUE, supported);
    }

    // Tests supportsUpdate with real DeserializationConfig
    @Test
    public void testSupportsUpdate_withConfig_returnsTrue() {
        ObjectMapper mapper = new ObjectMapper();
        Boolean supported = deserializer.supportsUpdate(mapper.getDeserializationConfig());

        assertEquals(Boolean.TRUE, supported);
    }

    // Tests getNullValue returns an empty AtomicReference
    @Test
    public void testGetNullValue_returnsAtomicReference() throws Exception {
        AtomicReference<Object> nullValue = deserializer.getNullValue((DeserializationContext) null);

        assertNotNull(nullValue);
        assertNull(nullValue.get());
    }

    // Tests getEmptyValue returns an empty AtomicReference
    @Test
    public void testGetEmptyValue_returnsAtomicReference() throws Exception {
        Object emptyValue = deserializer.getEmptyValue((DeserializationContext) null);

        assertNotNull(emptyValue);
        assertTrue(emptyValue instanceof AtomicReference);
        assertNull(((AtomicReference<?>) emptyValue).get());
    }

    // Tests withResolved creates a new deserializer instance
    @Test
    public void testWithResolved_validParameters_returnsNewInstance() {
        AtomicReferenceDeserializer resolved = deserializer.withResolved(null, null);

        assertNotNull(resolved);
        assertNotSame(deserializer, resolved);
    }

    // Tests full deserialization with normal string content
    @Test
    public void testDeserialize_normalStringContent_deserializesCorrectly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AtomicReference<String> result = mapper.readValue("\"hello world\"", new TypeReference<AtomicReference<String>>() {});

        assertNotNull(result);
        assertEquals("hello world", result.get());
    }

    // Tests full deserialization with null JSON token
    @Test
    public void testDeserialize_nullJsonToken_deserializesToAtomicReference() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AtomicReference<String> result = mapper.readValue("null", new TypeReference<AtomicReference<String>>() {});

        assertNull(result);
    }

    // Tests full deserialization with null value in JSON string format
    @Test
    public void testDeserialize_wrappedNullString_deserializesCorrectly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AtomicReference<String> result = mapper.readValue("null", new TypeReference<AtomicReference<String>>() {});

        assertNull(result);
    }

    // Tests getNullAccessPattern returns an AccessPattern instance
    @Test
    public void testGetNullAccessPattern() {
        AccessPattern pattern = deserializer.getNullAccessPattern();

        assertNotNull(pattern);
    }

    // Tests getEmptyAccessPattern returns an AccessPattern instance
    @Test
    public void testGetEmptyAccessPattern() {
        AccessPattern pattern = deserializer.getEmptyAccessPattern();

        assertNotNull(pattern);
    }

    // Tests getNullValue and getEmptyValue when resolved with a value deserializer
    @Test
    public void testGetNullAndEmptyValue_withValueDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonDeserializer<?> strDeser = mapper.deserializationConfig().findRootValueDeserializer(
                TypeFactory.defaultInstance().constructType(String.class));

        AtomicReferenceDeserializer resolved = deserializer.withResolved(null, strDeser);

        AtomicReference<Object> nullVal = resolved.getNullValue(ctxt);
        assertNotNull(nullVal);
        assertNull(nullVal.get());

        Object emptyVal = resolved.getEmptyValue(ctxt);
        assertNotNull(emptyVal);
        assertTrue(emptyVal instanceof AtomicReference);
        assertNull(((AtomicReference<?>) emptyVal).get());
    }

    // Tests deserialization inside a POJO bean
    @Test
    public void testDeserialize_inBean() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Bean bean = mapper.readValue("{\"ref\":\"abc\"}", Bean.class);

        assertNotNull(bean);
        assertNotNull(bean.ref);
        assertEquals("abc", bean.ref.get());
    }

    // Tests deserialization inside a POJO bean with null value
    @Test
    public void testDeserialize_inBean_nullValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Bean bean = mapper.readValue("{\"ref\":null}", Bean.class);

        assertNotNull(bean);
        assertNull(bean.ref);
    }

    // Tests deserialization via readerForUpdating
    @Test
    public void testDeserialize_readerForUpdating() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AtomicReference<String> ref = new AtomicReference<String>("initial");
        AtomicReference<String> result = mapper.readerForUpdating(ref).readValue("\"updated\"");

        assertSame(ref, result);
        assertEquals("updated", result.get());
    }
}