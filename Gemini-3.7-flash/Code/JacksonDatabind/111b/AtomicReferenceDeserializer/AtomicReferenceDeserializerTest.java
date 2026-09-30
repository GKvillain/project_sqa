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
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class AtomicReferenceDeserializerTest {

    private AtomicReferenceDeserializer deserializer;
    private JavaType javaType;

    @Before
    public void setUp() {
        javaType = TypeFactory.defaultInstance().constructType(AtomicReference.class);
        deserializer = new AtomicReferenceDeserializer(javaType, null, null, null);
    }

    // Tests referenceValue with valid non-null contents
    @Test
    public void testReferenceValue_validObject_returnsAtomicReference() {
        String content = "testString";
        AtomicReference<Object> ref = deserializer.referenceValue(content);
        assertNotNull(ref);
        assertEquals(content, ref.get());
    }

    // Tests referenceValue with null content
    @Test
    public void testReferenceValue_nullObject_returnsAtomicReferenceWithNull() {
        AtomicReference<Object> ref = deserializer.referenceValue(null);
        assertNotNull(ref);
        assertNull(ref.get());
    }

    // Tests getReferenced with valid content inside AtomicReference
    @Test
    public void testGetReferenced_validReference_returnsContainedObject() {
        Integer content = 12345;
        AtomicReference<Object> ref = new AtomicReference<Object>(content);
        Object result = deserializer.getReferenced(ref);
        assertEquals(content, result);
    }

    // Tests getReferenced when AtomicReference contains null
    @Test
    public void testGetReferenced_nullContainingReference_returnsNull() {
        AtomicReference<Object> ref = new AtomicReference<Object>(null);
        Object result = deserializer.getReferenced(ref);
        assertNull(result);
    }

    // Tests updateReference updating the content of the reference and returning the same reference
    @Test
    public void testUpdateReference_validObject_updatesAndReturnsSameInstance() {
        AtomicReference<Object> ref = new AtomicReference<Object>("initial");
        String updatedContent = "updated";
        AtomicReference<Object> result = deserializer.updateReference(ref, updatedContent);

        assertSame(ref, result);
        assertEquals(updatedContent, result.get());
    }

    // Tests updateReference with null content
    @Test
    public void testUpdateReference_nullContent_updatesToNullAndReturnsSameInstance() {
        AtomicReference<Object> ref = new AtomicReference<Object>("initial");
        AtomicReference<Object> result = deserializer.updateReference(ref, null);

        assertSame(ref, result);
        assertNull(result.get());
    }

    // Tests supportsUpdate method returns Boolean.TRUE
    @Test
    public void testSupportsUpdate_nullConfig_returnsTrue() {
        Boolean result = deserializer.supportsUpdate((DeserializationConfig) null);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests getNullValue with null context
    @Test
    public void testGetNullValue_nullContext_returnsEmptyAtomicReference() throws Exception {
        AtomicReference<Object> nullValue = deserializer.getNullValue((DeserializationContext) null);
        assertNotNull(nullValue);
        assertNull(nullValue.get());
    }

    // Tests getNullValue with actual DeserializationContext
    @Test
    public void testGetNullValue_withContext_returnsEmptyAtomicReference() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        AtomicReference<Object> nullValue = deserializer.getNullValue(ctxt);
        assertNotNull(nullValue);
        assertNull(nullValue.get());
    }

    // Tests getEmptyValue with null context
    @Test
    public void testGetEmptyValue_nullContext_returnsEmptyAtomicReference() {
        Object emptyValue = deserializer.getEmptyValue((DeserializationContext) null);
        assertNotNull(emptyValue);
        assertTrue(emptyValue instanceof AtomicReference<?>);
        assertNull(((AtomicReference<?>) emptyValue).get());
    }

    // Tests getEmptyValue with actual DeserializationContext
    @Test
    public void testGetEmptyValue_withContext_returnsEmptyAtomicReference() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object emptyValue = deserializer.getEmptyValue(ctxt);
        assertNotNull(emptyValue);
        assertTrue(emptyValue instanceof AtomicReference<?>);
        assertNull(((AtomicReference<?>) emptyValue).get());
    }

    // Tests withResolved creating a new instance
    @Test
    public void testWithResolved_nullParams_returnsNewInstance() {
        AtomicReferenceDeserializer newDeser = deserializer.withResolved(null, null);
        assertNotNull(newDeser);
        assertNotSame(deserializer, newDeser);
    }

    // Tests withResolved preserving type and behavior
    @Test
    public void testWithResolved_customDeser_createsInstanceWithUpdatedBehavior() {
        JsonDeserializer<?> mockDeser = new AtomicReferenceDeserializer(javaType, null, null, null);
        AtomicReferenceDeserializer newDeser = deserializer.withResolved(null, mockDeser);
        assertNotNull(newDeser);
        
        AtomicReference<Object> ref = newDeser.referenceValue("test");
        assertNotNull(ref);
        assertEquals("test", ref.get());
    }

    // Tests constructor with all null parameters
    @Test
    public void testConstructor_allNullParams_initializesProperly() {
        AtomicReferenceDeserializer deser = new AtomicReferenceDeserializer(null, null, null, null);
        assertNotNull(deser);
        assertNull(deser.getValueType());
        
        AtomicReference<Object> ref = deser.referenceValue(100);
        assertEquals(100, ref.get());
    }

    // Tests getAbsentValue with null context
    @Test
    public void testGetAbsentValue_nullContext_returnsNull() {
        Object absentValue = deserializer.getAbsentValue(null);
        assertNull(absentValue);
    }

    // Tests getAbsentValue with DeserializationContext
    @Test
    public void testGetAbsentValue_withContext_returnsNull() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object absentValue = deserializer.getAbsentValue(ctxt);
        assertNull(absentValue);
    }

    // Tests end-to-end deserialization from JSON string to AtomicReference
    @Test
    public void testEndToEndDeserialization_stringContent() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AtomicReference<String> result = mapper.readValue(
            "\"Hello World\"",
            new TypeReference<AtomicReference<String>>() {}
        );
        assertNotNull(result);
        assertEquals("Hello World", result.get());
    }

    // Tests end-to-end deserialization from JSON null to AtomicReference
    @Test
    public void testEndToEndDeserialization_nullContent() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AtomicReference<String> result = mapper.readValue(
            "null",
            new TypeReference<AtomicReference<String>>() {}
        );
        assertNotNull(result);
        assertNull(result.get());
    }

    // Tests end-to-end deserialization updating existing AtomicReference instance
    @Test
    public void testEndToEndDeserialization_updatingExistingInstance() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AtomicReference<String> ref = new AtomicReference<String>("initialValue");
        AtomicReference<String> result = mapper.readerForUpdating(ref).readValue("\"updatedValue\"");

        assertSame(ref, result);
        assertEquals("updatedValue", result.get());
    }
}