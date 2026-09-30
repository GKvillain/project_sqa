package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

public class NullifyingDeserializerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonFactory jsonFactory = new JsonFactory();

    // Tests singleton instance and constructor
    @Test
    public void testInstance_notNull() {
        assertNotNull(NullifyingDeserializer.instance);
        NullifyingDeserializer deser = new NullifyingDeserializer();
        assertNotNull(deser);
    }

    // Tests deserialize with scalar int token
    @Test
    public void testDeserialize_scalarInt_returnsNull() throws IOException {
        JsonParser p = jsonFactory.createParser("123");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = NullifyingDeserializer.instance.deserialize(p, ctxt);
        assertNull(result);
        p.close();
    }

    // Tests deserialize with scalar string token
    @Test
    public void testDeserialize_scalarString_returnsNull() throws IOException {
        JsonParser p = jsonFactory.createParser("\"hello\"");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = NullifyingDeserializer.instance.deserialize(p, ctxt);
        assertNull(result);
        p.close();
    }

    // Tests deserialize with array token
    @Test
    public void testDeserialize_array_returnsNull() throws IOException {
        JsonParser p = jsonFactory.createParser("[1, 2, 3]");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = NullifyingDeserializer.instance.deserialize(p, ctxt);
        assertNull(result);
        p.close();
    }

    // Tests deserialize with start object token
    @Test
    public void testDeserialize_object_returnsNull() throws IOException {
        JsonParser p = jsonFactory.createParser("{\"key\":\"value\"}");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = NullifyingDeserializer.instance.deserialize(p, ctxt);
        assertNull(result);
        p.close();
    }

    // Tests deserialize when parser is pointing to field name
    @Test
    public void testDeserialize_fieldNameToken_skipsAndReturnsNull() throws IOException {
        JsonParser p = jsonFactory.createParser("{\"a\": 1, \"b\": 2}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = NullifyingDeserializer.instance.deserialize(p, ctxt);
        assertNull(result);
        p.close();
    }

    // Tests deserializeWithType for START_OBJECT branch
    @Test
    public void testDeserializeWithType_startObject_delegatesToTypeDeserializer() throws IOException {
        JsonParser p = jsonFactory.createParser("{\"type\":\"custom\",\"data\":123}");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        final boolean[] called = new boolean[1];
        TypeDeserializer typeDeser = new TypeDeserializer() {
            @Override
            public TypeDeserializer forProperty(com.fasterxml.jackson.databind.BeanProperty prop) { return this; }
            @Override
            public com.fasterxml.jackson.annotation.JsonTypeInfo.As getTypeInclusion() { return null; }
            @Override
            public String getPropertyName() { return null; }
            @Override
            public com.fasterxml.jackson.databind.jsontype.TypeIdResolver getTypeIdResolver() { return null; }
            @Override
            public Class<?> getDefaultImpl() { return null; }
            @Override
            public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) {
                called[0] = true;
                return "typedResult";
            }
        };

        Object result = NullifyingDeserializer.instance.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(called[0]);
        assertEquals("typedResult", result);
        p.close();
    }

    // Tests deserializeWithType for START_ARRAY branch
    @Test
    public void testDeserializeWithType_startArray_delegatesToTypeDeserializer() throws IOException {
        JsonParser p = jsonFactory.createParser("[1, 2]");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        final boolean[] called = new boolean[1];
        TypeDeserializer typeDeser = new TypeDeserializer() {
            @Override
            public TypeDeserializer forProperty(com.fasterxml.jackson.databind.BeanProperty prop) { return this; }
            @Override
            public com.fasterxml.jackson.annotation.JsonTypeInfo.As getTypeInclusion() { return null; }
            @Override
            public String getPropertyName() { return null; }
            @Override
            public com.fasterxml.jackson.databind.jsontype.TypeIdResolver getTypeIdResolver() { return null; }
            @Override
            public Class<?> getDefaultImpl() { return null; }
            @Override
            public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) {
                called[0] = true;
                return "arrayResult";
            }
        };

        Object result = NullifyingDeserializer.instance.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(called[0]);
        assertEquals("arrayResult", result);
        p.close();
    }

    // Tests deserializeWithType for FIELD_NAME branch
    @Test
    public void testDeserializeWithType_fieldName_delegatesToTypeDeserializer() throws IOException {
        JsonParser p = jsonFactory.createParser("{\"field\": \"value\"}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        DeserializationContext ctxt = mapper.getDeserializationContext();

        final boolean[] called = new boolean[1];
        TypeDeserializer typeDeser = new TypeDeserializer() {
            @Override
            public TypeDeserializer forProperty(com.fasterxml.jackson.databind.BeanProperty prop) { return this; }
            @Override
            public com.fasterxml.jackson.annotation.JsonTypeInfo.As getTypeInclusion() { return null; }
            @Override
            public String getPropertyName() { return null; }
            @Override
            public com.fasterxml.jackson.databind.jsontype.TypeIdResolver getTypeIdResolver() { return null; }
            @Override
            public Class<?> getDefaultImpl() { return null; }
            @Override
            public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) {
                called[0] = true;
                return "fieldResult";
            }
        };

        Object result = NullifyingDeserializer.instance.deserializeWithType(p, ctxt, typeDeser);
        assertTrue(called[0]);
        assertEquals("fieldResult", result);
        p.close();
    }

    // Tests deserializeWithType for default switch branch (scalar int)
    @Test
    public void testDeserializeWithType_scalarInt_returnsNull() throws IOException {
        JsonParser p = jsonFactory.createParser("123");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        TypeDeserializer typeDeser = new TypeDeserializer() {
            @Override
            public TypeDeserializer forProperty(com.fasterxml.jackson.databind.BeanProperty prop) { return this; }
            @Override
            public com.fasterxml.jackson.annotation.JsonTypeInfo.As getTypeInclusion() { return null; }
            @Override
            public String getPropertyName() { return null; }
            @Override
            public com.fasterxml.jackson.databind.jsontype.TypeIdResolver getTypeIdResolver() { return null; }
            @Override
            public Class<?> getDefaultImpl() { return null; }
            @Override
            public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) {
                fail("deserializeTypedFromAny should not be called for scalar");
                return null;
            }
        };

        Object result = NullifyingDeserializer.instance.deserializeWithType(p, ctxt, typeDeser);
        assertNull(result);
        p.close();
    }

    // Tests deserializeWithType for default switch branch (scalar boolean)
    @Test
    public void testDeserializeWithType_scalarBoolean_returnsNull() throws IOException {
        JsonParser p = jsonFactory.createParser("true");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        TypeDeserializer typeDeser = new TypeDeserializer() {
            @Override
            public TypeDeserializer forProperty(com.fasterxml.jackson.databind.BeanProperty prop) { return this; }
            @Override
            public com.fasterxml.jackson.annotation.JsonTypeInfo.As getTypeInclusion() { return null; }
            @Override
            public String getPropertyName() { return null; }
            @Override
            public com.fasterxml.jackson.databind.jsontype.TypeIdResolver getTypeIdResolver() { return null; }
            @Override
            public Class<?> getDefaultImpl() { return null; }
            @Override
            public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) {
                fail("deserializeTypedFromAny should not be called for boolean");
                return null;
            }
        };

        Object result = NullifyingDeserializer.instance.deserializeWithType(p, ctxt, typeDeser);
        assertNull(result);
        p.close();
    }
}