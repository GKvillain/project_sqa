package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class NullifyingDeserializerTest {

    private NullifyingDeserializer deser;
    private JsonFactory jsonFactory;

    @Before
    public void setUp() {
        deser = NullifyingDeserializer.instance;
        jsonFactory = new JsonFactory();
    }

    // Tests singleton instance availability
    @Test
    public void testInstance_singletonField_isNotNull() {
        assertNotNull(NullifyingDeserializer.instance);
    }

    // Tests default constructor
    @Test
    public void testConstructor_newInstance_isNotNull() {
        NullifyingDeserializer instance = new NullifyingDeserializer();
        assertNotNull(instance);
        assertEquals(Object.class, instance.handledType());
    }

    // Tests deserialization of scalar string value
    @Test
    public void testDeserialize_scalarString_returnsNull() throws IOException {
        JsonParser p = jsonFactory.createParser("\"hello world\"");
        p.nextToken();
        Object result = deser.deserialize(p, null);
        assertNull(result);
        p.close();
    }

    // Tests deserialization of scalar integer value
    @Test
    public void testDeserialize_scalarNumber_returnsNull() throws IOException {
        JsonParser p = jsonFactory.createParser("12345");
        p.nextToken();
        Object result = deser.deserialize(p, null);
        assertNull(result);
        p.close();
    }

    // Tests deserialization of scalar boolean value
    @Test
    public void testDeserialize_scalarBoolean_returnsNull() throws IOException {
        JsonParser p = jsonFactory.createParser("true");
        p.nextToken();
        Object result = deser.deserialize(p, null);
        assertNull(result);
        p.close();
    }

    // Tests deserialization and skipping of JSON array
    @Test
    public void testDeserialize_arrayInput_skipsChildrenAndReturnsNull() throws IOException {
        JsonParser p = jsonFactory.createParser("[1, 2, [3, 4], {\"a\": 5}]");
        p.nextToken();
        Object result = deser.deserialize(p, null);
        assertNull(result);
        assertEquals(JsonToken.END_ARRAY, p.getCurrentToken());
        p.close();
    }

    // Tests deserialization and skipping of JSON object
    @Test
    public void testDeserialize_objectInput_skipsChildrenAndReturnsNull() throws IOException {
        JsonParser p = jsonFactory.createParser("{\"k1\": \"v1\", \"k2\": [1, 2], \"k3\": {\"nested\": true}}");
        p.nextToken();
        Object result = deser.deserialize(p, null);
        assertNull(result);
        assertEquals(JsonToken.END_OBJECT, p.getCurrentToken());
        p.close();
    }

    // Tests deserialization when parser points directly to FIELD_NAME token
    @Test
    public void testDeserialize_fieldNameToken_skipsChildrenAndReturnsNull() throws IOException {
        JsonParser p = jsonFactory.createParser("{\"field1\": \"val1\", \"field2\": 100}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME "field1"
        assertEquals(JsonToken.FIELD_NAME, p.getCurrentToken());
        Object result = deser.deserialize(p, null);
        assertNull(result);
        p.close();
    }

    // Tests deserializeWithType when current token is START_OBJECT
    @Test
    public void testDeserializeWithType_startObject_delegatesToTypeDeserializer() throws IOException {
        JsonParser p = jsonFactory.createParser("{\"a\": 1}");
        p.nextToken();
        TypeDeserializer typeDeser = createStubTypeDeserializer("fromAnyResult");
        Object result = deser.deserializeWithType(p, null, typeDeser);
        assertEquals("fromAnyResult", result);
        p.close();
    }

    // Tests deserializeWithType when current token is START_ARRAY
    @Test
    public void testDeserializeWithType_startArray_delegatesToTypeDeserializer() throws IOException {
        JsonParser p = jsonFactory.createParser("[1, 2, 3]");
        p.nextToken();
        TypeDeserializer typeDeser = createStubTypeDeserializer("fromAnyArrayResult");
        Object result = deser.deserializeWithType(p, null, typeDeser);
        assertEquals("fromAnyArrayResult", result);
        p.close();
    }

    // Tests deserializeWithType when current token is FIELD_NAME
    @Test
    public void testDeserializeWithType_fieldName_delegatesToTypeDeserializer() throws IOException {
        JsonParser p = jsonFactory.createParser("{\"name\": \"value\"}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        TypeDeserializer typeDeser = createStubTypeDeserializer("fromAnyFieldResult");
        Object result = deser.deserializeWithType(p, null, typeDeser);
        assertEquals("fromAnyFieldResult", result);
        p.close();
    }

    // Tests deserializeWithType default branch on scalar string token
    @Test
    public void testDeserializeWithType_scalarStringToken_returnsNull() throws IOException {
        JsonParser p = jsonFactory.createParser("\"test-string\"");
        p.nextToken();
        TypeDeserializer typeDeser = createStubTypeDeserializer("unexpected");
        Object result = deser.deserializeWithType(p, null, typeDeser);
        assertNull(result);
        p.close();
    }

    // Tests deserializeWithType default branch on scalar int token
    @Test
    public void testDeserializeWithType_scalarNumberToken_returnsNull() throws IOException {
        JsonParser p = jsonFactory.createParser("999");
        p.nextToken();
        TypeDeserializer typeDeser = createStubTypeDeserializer("unexpected");
        Object result = deser.deserializeWithType(p, null, typeDeser);
        assertNull(result);
        p.close();
    }

    // Tests deserializeWithType default branch on scalar boolean token
    @Test
    public void testDeserializeWithType_scalarBooleanToken_returnsNull() throws IOException {
        JsonParser p = jsonFactory.createParser("false");
        p.nextToken();
        TypeDeserializer typeDeser = createStubTypeDeserializer("unexpected");
        Object result = deser.deserializeWithType(p, null, typeDeser);
        assertNull(result);
        p.close();
    }

    // Tests deserializeWithType default branch on null token
    @Test
    public void testDeserializeWithType_nullToken_returnsNull() throws IOException {
        JsonParser p = jsonFactory.createParser("null");
        p.nextToken();
        TypeDeserializer typeDeser = createStubTypeDeserializer("unexpected");
        Object result = deser.deserializeWithType(p, null, typeDeser);
        assertNull(result);
        p.close();
    }

    private TypeDeserializer createStubTypeDeserializer(final Object anyReturnValue) {
        return new TypeDeserializer() {
            @Override
            public TypeDeserializer forProperty(BeanProperty prop) {
                return this;
            }

            @Override
            public JsonTypeInfo.As getTypeInclusion() {
                return JsonTypeInfo.As.WRAPPER_OBJECT;
            }

            @Override
            public String getPropertyName() {
                return null;
            }

            @Override
            public TypeIdResolver getTypeIdResolver() {
                return null;
            }

            @Override
            public Class<?> getDefaultImpl() {
                return null;
            }

            @Override
            public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) {
                return null;
            }

            @Override
            public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) {
                return null;
            }

            @Override
            public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) {
                return null;
            }

            @Override
            public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) {
                return anyReturnValue;
            }
        };
    }
}