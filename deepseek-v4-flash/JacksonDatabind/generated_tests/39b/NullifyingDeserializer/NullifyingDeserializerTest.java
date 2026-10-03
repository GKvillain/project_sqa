package com.fasterxml.jackson.databind.deser.std;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;

public class NullifyingDeserializerTest {

    private final NullifyingDeserializer deserializer = NullifyingDeserializer.instance;

    // Tests deserializeWithType for ID_START_ARRAY token
    @Test
    public void testDeserializeWithType_startArrayToken_returnsDeserializedFromAny() throws IOException {
        JsonParser p = createParserWithToken(JsonToken.START_ARRAY);
        DeserializationContext ctxt = createContext();
        TypeDeserializer typeDeser = createTypeDeserializer();
        Object result = deserializer.deserializeWithType(p, ctxt, typeDeser);
        assertNotNull("Should call deserializeTypedFromAny for START_ARRAY", result);
    }

    // Tests deserializeWithType for ID_START_OBJECT token
    @Test
    public void testDeserializeWithType_startObjectToken_returnsDeserializedFromAny() throws IOException {
        JsonParser p = createParserWithToken(JsonToken.START_OBJECT);
        DeserializationContext ctxt = createContext();
        TypeDeserializer typeDeser = createTypeDeserializer();
        Object result = deserializer.deserializeWithType(p, ctxt, typeDeser);
        assertNotNull("Should call deserializeTypedFromAny for START_OBJECT", result);
    }

    // Tests deserializeWithType for ID_FIELD_NAME token
    @Test
    public void testDeserializeWithType_fieldNameToken_returnsDeserializedFromAny() throws IOException {
        JsonParser p = createParserWithToken(JsonToken.FIELD_NAME);
        DeserializationContext ctxt = createContext();
        TypeDeserializer typeDeser = createTypeDeserializer();
        Object result = deserializer.deserializeWithType(p, ctxt, typeDeser);
        assertNotNull("Should call deserializeTypedFromAny for FIELD_NAME", result);
    }

    // Tests deserializeWithType for default token (e.g. VALUE_STRING) which returns null
    @Test
    public void testDeserializeWithType_defaultToken_returnsNull() throws IOException {
        JsonParser p = createParserWithToken(JsonToken.VALUE_STRING);
        DeserializationContext ctxt = createContext();
        TypeDeserializer typeDeser = createTypeDeserializer();
        Object result = deserializer.deserializeWithType(p, ctxt, typeDeser);
        assertNull("Should return null for non-structural tokens", result);
    }

    // Tests deserializeWithType for VALUE_NUMBER_INT token
    @Test
    public void testDeserializeWithType_valueNumberIntToken_returnsNull() throws IOException {
        JsonParser p = createParserWithToken(JsonToken.VALUE_NUMBER_INT);
        DeserializationContext ctxt = createContext();
        TypeDeserializer typeDeser = createTypeDeserializer();
        Object result = deserializer.deserializeWithType(p, ctxt, typeDeser);
        assertNull("Should return null for VALUE_NUMBER_INT", result);
    }

    // Tests deserializeWithType for VALUE_TRUE token
    @Test
    public void testDeserializeWithType_valueTrueToken_returnsNull() throws IOException {
        JsonParser p = createParserWithToken(JsonToken.VALUE_TRUE);
        DeserializationContext ctxt = createContext();
        TypeDeserializer typeDeser = createTypeDeserializer();
        Object result = deserializer.deserializeWithType(p, ctxt, typeDeser);
        assertNull("Should return null for VALUE_TRUE", result);
    }

    // Tests deserializeWithType for VALUE_NULL token
    @Test
    public void testDeserializeWithType_valueNullToken_returnsNull() throws IOException {
        JsonParser p = createParserWithToken(JsonToken.VALUE_NULL);
        DeserializationContext ctxt = createContext();
        TypeDeserializer typeDeser = createTypeDeserializer();
        Object result = deserializer.deserializeWithType(p, ctxt, typeDeser);
        assertNull("Should return null for VALUE_NULL", result);
    }

    // Tests deserialize normal case - calls skipChildren and returns null
    @Test
    public void testDeserialize_anyToken_returnsNull() throws IOException {
        JsonParser p = createParserWithToken(JsonToken.START_ARRAY);
        DeserializationContext ctxt = createContext();
        Object result = deserializer.deserialize(p, ctxt);
        assertNull("deserialize should always return null", result);
    }

    // Tests deserialize with FIELD_NAME token
    @Test
    public void testDeserialize_fieldNameToken_returnsNull() throws IOException {
        JsonParser p = createParserWithToken(JsonToken.FIELD_NAME);
        DeserializationContext ctxt = createContext();
        Object result = deserializer.deserialize(p, ctxt);
        assertNull("deserialize should always return null even for FIELD_NAME", result);
    }

    // Tests deserialize with VALUE_STRING token
    @Test
    public void testDeserialize_valueStringToken_returnsNull() throws IOException {
        JsonParser p = createParserWithToken(JsonToken.VALUE_STRING);
        DeserializationContext ctxt = createContext();
        Object result = deserializer.deserialize(p, ctxt);
        assertNull("deserialize should always return null", result);
    }

    // Tests deserialize with VALUE_NUMBER_FLOAT token
    @Test
    public void testDeserialize_valueNumberFloatToken_returnsNull() throws IOException {
        JsonParser p = createParserWithToken(JsonToken.VALUE_NUMBER_FLOAT);
        DeserializationContext ctxt = createContext();
        Object result = deserializer.deserialize(p, ctxt);
        assertNull("deserialize should always return null", result);
    }

    // Tests deserialize with END_ARRAY token
    @Test
    public void testDeserialize_endArrayToken_returnsNull() throws IOException {
        JsonParser p = createParserWithToken(JsonToken.END_ARRAY);
        DeserializationContext ctxt = createContext();
        Object result = deserializer.deserialize(p, ctxt);
        assertNull("deserialize should always return null", result);
    }

    // Tests deserialize with END_OBJECT token
    @Test
    public void testDeserialize_endObjectToken_returnsNull() throws IOException {
        JsonParser p = createParserWithToken(JsonToken.END_OBJECT);
        DeserializationContext ctxt = createContext();
        Object result = deserializer.deserialize(p, ctxt);
        assertNull("deserialize should always return null", result);
    }

    // Helper method to create a mock JsonParser that returns a given token
    private JsonParser createParserWithToken(JsonToken token) {
        return new JsonParser() {
            @Override
            public JsonToken getCurrentToken() {
                return token;
            }

            @Override
            public int getCurrentTokenId() {
                // Convert JsonToken to token id; for simplicity we use switch
                // In real implementation, token.getId() would be used
                switch (token) {
                    case START_ARRAY: return JsonTokenId.ID_START_ARRAY;
                    case START_OBJECT: return JsonTokenId.ID_START_OBJECT;
                    case FIELD_NAME: return JsonTokenId.ID_FIELD_NAME;
                    case VALUE_STRING: return JsonTokenId.ID_STRING;
                    case VALUE_NUMBER_INT: return JsonTokenId.ID_NUMBER_INT;
                    case VALUE_NUMBER_FLOAT: return JsonTokenId.ID_NUMBER_FLOAT;
                    case VALUE_TRUE: return JsonTokenId.ID_TRUE;
                    case VALUE_FALSE: return JsonTokenId.ID_FALSE;
                    case VALUE_NULL: return JsonTokenId.ID_NULL;
                    case END_ARRAY: return JsonTokenId.ID_END_ARRAY;
                    case END_OBJECT: return JsonTokenId.ID_END_OBJECT;
                    default: return -1;
                }
            }

            @Override
            public int getCurrentTokenId() {
                // Override to match our implementation
                switch (token) {
                    case START_ARRAY: return JsonTokenId.ID_START_ARRAY;
                    case START_OBJECT: return JsonTokenId.ID_START_OBJECT;
                    case FIELD_NAME: return JsonTokenId.ID_FIELD_NAME;
                    case VALUE_STRING: return JsonTokenId.ID_STRING;
                    case VALUE_NUMBER_INT: return JsonTokenId.ID_NUMBER_INT;
                    case VALUE_NUMBER_FLOAT: return JsonTokenId.ID_NUMBER_FLOAT;
                    case VALUE_TRUE: return JsonTokenId.ID_TRUE;
                    case VALUE_FALSE: return JsonTokenId.ID_FALSE;
                    case VALUE_NULL: return JsonTokenId.ID_NULL;
                    case END_ARRAY: return JsonTokenId.ID_END_ARRAY;
                    case END_OBJECT: return JsonTokenId.ID_END_OBJECT;
                    default: return -1;
                }
            }

            @Override
            public JsonToken nextToken() throws IOException {
                return null;
            }

            @Override
            public JsonParser skipChildren() throws IOException {
                // no-op mock
                return this;
            }

            @Override
            public JsonToken getCurrentToken() {
                return token;
            }

            @Override
            public boolean isClosed() {
                return false;
            }

            @Override
            public void clearCurrentToken() {
            }

            @Override
            public JsonLocation getTokenLocation() {
                return null;
            }

            @Override
            public JsonLocation getCurrentLocation() {
                return null;
            }

            @Override
            public int releaseBuffered(JsonGenerator g) throws IOException {
                return 0;
            }

            @Override
            public ObjectCodec getCodec() {
                return null;
            }

            @Override
            public void setCodec(ObjectCodec c) {
            }

            @Override
            public void close() throws IOException {
            }
        };
    }

    // Helper method to create a mock DeserializationContext
    private DeserializationContext createContext() {
        return new DeserializationContext(null, null, null) {
            // minimal implementation; not used in these tests
        };
    }

    // Helper method to create a mock TypeDeserializer that returns a non-null object
    private TypeDeserializer createTypeDeserializer() {
        return new TypeDeserializer() {
            @Override
            public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) throws IOException {
                return new Object();
            }

            @Override
            public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) throws IOException {
                return null;
            }

            @Override
            public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) throws IOException {
                return null;
            }

            @Override
            public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) throws IOException {
                return null;
            }
        };
    }
}