package com.fasterxml.jackson.databind.deser.std;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ValueInstantiator;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

@RunWith(MockitoJUnitRunner.class)
public class AtomicReferenceDeserializerTest {

    @Mock
    private JavaType fullType;
    @Mock
    private ValueInstantiator valueInstantiator;
    @Mock
    private TypeDeserializer typeDeserializer;
    @Mock
    private JsonDeserializer<Object> valueDeserializer;
    @Mock
    private DeserializationContext ctxt;
    @Mock
    private DeserializationConfig config;
    @Mock
    private JsonParser jsonParser;

    private AtomicReferenceDeserializer deserializer;

    @Before
    public void setUp() {
        deserializer = new AtomicReferenceDeserializer(fullType, valueInstantiator,
                typeDeserializer, valueDeserializer);
    }

    // ===== Existing tests =====

    // Test constructor works
    @Test
    public void testConstructor_createsInstance() {
        assertNotNull(deserializer);
    }

    // Test getNullValue returns null (correct behavior after fix)
    @Test
    public void testGetNullValue_returnsNull() throws JsonMappingException {
        assertNull(deserializer.getNullValue(ctxt));
    }

    // Test getEmptyValue returns a non-null AtomicReference
    @Test
    public void testGetEmptyValue_returnsNonNullAtomicReference() {
        assertNotNull(deserializer.getEmptyValue(ctxt));
    }

    // Test referenceValue with non-null contents
    @Test
    public void testReferenceValue_withNonNullContents_returnsAtomicReferenceWithContent() {
        String content = "test";
        AtomicReference<Object> ref = deserializer.referenceValue(content);
        assertNotNull(ref);
        assertEquals(content, ref.get());
    }

    // Test referenceValue with null contents
    @Test
    public void testReferenceValue_withNullContents_returnsAtomicReferenceWithNull() {
        AtomicReference<Object> ref = deserializer.referenceValue(null);
        assertNotNull(ref);
        assertNull(ref.get());
    }

    // Test getReferenced with valid reference
    @Test
    public void testGetReferenced_withValidReference_returnsContent() {
        String content = "test";
        AtomicReference<Object> ref = new AtomicReference<>(content);
        Object result = deserializer.getReferenced(ref);
        assertEquals(content, result);
    }

    // Test getReferenced with null reference throws NPE
    @Test(expected = NullPointerException.class)
    public void testGetReferenced_withNullReference_throwsNullPointerException() {
        deserializer.getReferenced(null);
    }

    // Test updateReference updates and returns the same reference
    @Test
    public void testUpdateReference_updatesAndReturnsReference() {
        AtomicReference<Object> ref = new AtomicReference<>("old");
        Object newContent = "new";
        AtomicReference<Object> result = deserializer.updateReference(ref, newContent);
        assertSame(ref, result);
        assertEquals(newContent, ref.get());
    }

    // Test updateReference with null reference throws NPE
    @Test(expected = NullPointerException.class)
    public void testUpdateReference_withNullReference_throwsNullPointerException() {
        deserializer.updateReference(null, "content");
    }

    // Test supportsUpdate returns TRUE
    @Test
    public void testSupportsUpdate_returnsTrue() {
        assertTrue(deserializer.supportsUpdate(config));
    }

    // Test withResolved returns a new deserializer instance
    @Test
    public void testWithResolved_returnsNewInstance() {
        AtomicReferenceDeserializer resolved = deserializer.withResolved(typeDeserializer, valueDeserializer);
        assertNotNull(resolved);
        assertNotSame(deserializer, resolved);
    }

    // ===== New tests for uncovered coverage =====

    // Test getEmptyValue returns an empty AtomicReference (value == null)
    @Test
    public void testGetEmptyValue_returnsEmptyAtomicReference() {
        AtomicReference<Object> emptyRef = deserializer.getEmptyValue(ctxt);
        assertNotNull(emptyRef);
        assertNull("Empty AtomicReference must have null content", emptyRef.get());
    }

    // Test deserialize without typeDeserializer – uses valueDeserializer
    @Test
    public void testDeserialize_withoutTypeDeserializer_usesValueDeserializer() throws Exception {
        Object content = "deserializedContent";
        when(valueDeserializer.deserialize(jsonParser, ctxt)).thenReturn(content);

        AtomicReference<Object> result = deserializer.deserialize(jsonParser, ctxt);
        assertNotNull(result);
        assertEquals(content, result.get());
    }

    // Test deserialize with typeDeserializer – uses typeDeserializer.deserializeWithType
    @Test
    public void testDeserializeWithType_usesTypeDeserializer() throws Exception {
        Object content = "typedContent";
        when(typeDeserializer.deserializeWithType(jsonParser, ctxt, valueDeserializer)).thenReturn(content);

        AtomicReference<Object> result = (AtomicReference<Object>) deserializer.deserializeWithType(jsonParser, ctxt, typeDeserializer);
        assertNotNull(result);
        assertEquals(content, result.get());
    }

    // Test withResolved preserves new instances with different typeDeserializer
    @Test
    public void testWithResolved_differentTypeDeserializer() {
        TypeDeserializer newTypeDeser = mock(TypeDeserializer.class);
        JsonDeserializer<Object> newValueDeser = mock(JsonDeserializer.class);
        AtomicReferenceDeserializer resolved = deserializer.withResolved(newTypeDeser, newValueDeser);
        assertNotNull(resolved);
        assertNotSame(deserializer, resolved);
        // Verify that the resolved instance uses the new deserializers (if there were getters)
        // Here we just confirm it doesn't throw and returns different instance
    }

    // Test constructor with null valueInstantiator does not cause NPE
    @Test
    public void testConstructor_withNullValueInstantiator() {
        AtomicReferenceDeserializer noInstantiator = new AtomicReferenceDeserializer(
                fullType, null, typeDeserializer, valueDeserializer);
        assertNotNull(noInstantiator);
        assertNull("getNullValue should still work", noInstantiator.getNullValue(ctxt));
    }

    // Test getNullValue with null context (should still return null)
    @Test
    public void testGetNullValue_withNullContext() throws JsonMappingException {
        assertNull(deserializer.getNullValue(null));
    }
}