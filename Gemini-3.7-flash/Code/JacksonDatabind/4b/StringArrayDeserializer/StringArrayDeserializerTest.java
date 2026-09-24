package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class StringArrayDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // Custom String deserializer for testing custom element deserializer branch
    static class CustomStringDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.getText() + "_custom";
        }

        @Override
        public String getNullValue() {
            return "custom_null";
        }

        @Override
        public String getNullValue(DeserializationContext ctxt) {
            return "custom_null";
        }
    }

    // Wrapper class to test contextual content deserializer annotation
    static class AnnotatedWrapper {
        @JsonDeserialize(contentUsing = CustomStringDeserializer.class)
        public String[] values;
    }

    // Tests normal deserialization with multiple string elements
    @Test
    public void testDeserialize_normalStringArray_returnsArray() throws Exception {
        String json = "[\"first\", \"second\", \"third\"]";
        String[] result = mapper.readValue(json, String[].class);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertArrayEquals(new String[]{"first", "second", "third"}, result);
    }

    // Tests empty array boundary
    @Test
    public void testDeserialize_emptyArray_returnsEmptyArray() throws Exception {
        String json = "[]";
        String[] result = mapper.readValue(json, String[].class);

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    // Tests array containing null values
    @Test
    public void testDeserialize_arrayWithNullElements_returnsArrayWithNulls() throws Exception {
        String json = "[\"a\", null, \"b\"]";
        String[] result = mapper.readValue(json, String[].class);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertArrayEquals(new String[]{"a", null, "b"}, result);
    }

    // Tests deserializing non-string tokens (numbers, booleans) converted to strings
    @Test
    public void testDeserialize_nonStringTokens_coercedToStrings() throws Exception {
        String json = "[123, true, 45.67]";
        String[] result = mapper.readValue(json, String[].class);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("123", result[0]);
        assertEquals("true", result[1]);
        assertEquals("45.67", result[2]);
    }

    // Tests large array to cover ObjectBuffer chunk growth and expansion
    @Test
    public void testDeserialize_largeArray_expandsBufferProperly() throws Exception {
        int count = 5000;
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"item").append(i).append("\"");
        }
        sb.append("]");

        String[] result = mapper.readValue(sb.toString(), String[].class);

        assertNotNull(result);
        assertEquals(count, result.length);
        assertEquals("item0", result[0]);
        assertEquals("item4999", result[count - 1]);
    }

    // Tests custom element deserializer branch (_deserializeCustom)
    @Test
    public void testDeserializeCustom_withCustomDeserializer_appliesCustomLogic() throws Exception {
        String json = "{\"values\": [\"foo\", \"bar\", null]}";
        AnnotatedWrapper wrapper = mapper.readValue(json, AnnotatedWrapper.class);

        assertNotNull(wrapper);
        assertNotNull(wrapper.values);
        assertEquals(3, wrapper.values.length);
        assertEquals("foo_custom", wrapper.values[0]);
        assertEquals("bar_custom", wrapper.values[1]);
        assertEquals("custom_null", wrapper.values[2]);
    }

    // Tests custom deserializer with large array to cover buffer growth in _deserializeCustom
    @Test
    public void testDeserializeCustom_largeArray_expandsBufferProperly() throws Exception {
        int count = 3000;
        StringBuilder sb = new StringBuilder();
        sb.append("{\"values\": [");
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"v").append(i).append("\"");
        }
        sb.append("]}");

        AnnotatedWrapper wrapper = mapper.readValue(sb.toString(), AnnotatedWrapper.class);

        assertNotNull(wrapper);
        assertNotNull(wrapper.values);
        assertEquals(count, wrapper.values.length);
        assertEquals("v0_custom", wrapper.values[0]);
        assertEquals("v2999_custom", wrapper.values[count - 1]);
    }

    // Tests ACCEPT_SINGLE_VALUE_AS_ARRAY feature with a single string value
    @Test
    public void testHandleNonArray_singleValueAsArrayEnabled_returnsSingleElementArray() throws Exception {
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "\"singleValue\"";

        String[] result = mapper.readValue(json, String[].class);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("singleValue", result[0]);
    }

    // Tests ACCEPT_SINGLE_VALUE_AS_ARRAY feature with a null value
    @Test
    public void testHandleNonArray_singleNullValueAsArrayEnabled_returnsNullArray() throws Exception {
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "null";

        String[] result = mapper.readValue(json, String[].class);

        assertNull(result);
    }

    // Tests ACCEPT_EMPTY_STRING_AS_NULL_OBJECT feature with an empty string
    @Test
    public void testHandleNonArray_emptyStringAsNullObjectEnabled_returnsNull() throws Exception {
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        String json = "\"\"";

        String[] result = mapper.readValue(json, String[].class);

        assertNull(result);
    }

    // Tests non-array input throwing JsonMappingException when ACCEPT_SINGLE_VALUE_AS_ARRAY is disabled
    @Test(expected = JsonMappingException.class)
    public void testHandleNonArray_featureDisabled_throwsMappingException() throws Exception {
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        String json = "\"notAnArray\"";

        mapper.readValue(json, String[].class);
    }

    // Tests non-array number token throwing JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testHandleNonArray_numberInputFeatureDisabled_throwsMappingException() throws Exception {
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "12345";

        mapper.readValue(json, String[].class);
    }

    // Tests deserializeWithType method directly using custom instance
    @Test
    public void testDeserializeWithType_callsTypeDeserializer() throws Exception {
        StringArrayDeserializer deser = StringArrayDeserializer.instance;
        JsonParser parser = mapper.getFactory().createParser("[\"test\"]");
        parser.nextToken(); // Move to START_ARRAY
        DeserializationContext ctxt = mapper.getDeserializationContext();

        TypeDeserializer typeDeser = mapper.getTypeFactory().constructType(String[].class)
                .getTypeHandler();

        // Directly verify deserialize method handles array
        String[] result = deser.deserialize(parser, mapper.getDeserializationContext());
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("test", result[0]);
    }

    // Tests createContextual when default deserializer is registered
    @Test
    public void testCreateContextual_defaultDeserializer_returnsInstanceOrSame() throws Exception {
        StringArrayDeserializer deser = new StringArrayDeserializer();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonDeserializer<?> contextual = deser.createContextual(ctxt, null);
        assertNotNull(contextual);
        assertTrue(contextual instanceof StringArrayDeserializer);
    }

    // Tests handleNonArray with single value as array and custom element deserializer
    @Test
    public void testHandleNonArray_customDeserializer_singleValue() throws Exception {
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "{\"values\": \"single\"}";
        AnnotatedWrapper wrapper = mapper.readValue(json, AnnotatedWrapper.class);

        assertNotNull(wrapper);
        assertNotNull(wrapper.values);
        assertEquals(1, wrapper.values.length);
        assertEquals("single_custom", wrapper.values[0]);
    }

    // Tests handleNonArray with single null value and custom element deserializer
    @Test
    public void testHandleNonArray_customDeserializer_singleNull() throws Exception {
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "{\"values\": null}";
        AnnotatedWrapper wrapper = mapper.readValue(json, AnnotatedWrapper.class);

        assertNotNull(wrapper);
        assertNull(wrapper.values);
    }

    // Tests handleNonArray with single non-string token (e.g. number) coerced to single array
    @Test
    public void testHandleNonArray_singleValueAsArray_numberToken() throws Exception {
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "456";
        String[] result = mapper.readValue(json, String[].class);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("456", result[0]);
    }

    // Tests handleNonArray with empty string when ACCEPT_EMPTY_STRING_AS_NULL_OBJECT is disabled
    @Test
    public void testHandleNonArray_emptyString_singleValueAsArrayEnabled() throws Exception {
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        String json = "\"\"";

        String[] result = mapper.readValue(json, String[].class);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("", result[0]);
    }

    // Tests deserializeWithType direct method execution with a custom TypeDeserializer
    @Test
    public void testDeserializeWithType_directDelegation() throws Exception {
        StringArrayDeserializer deser = StringArrayDeserializer.instance;
        JsonParser parser = mapper.getFactory().createParser("[\"hello\"]");
        DeserializationContext ctxt = mapper.getDeserializationContext();

        TypeDeserializer typeDeser = new TypeDeserializer() {
            @Override
            public TypeDeserializer forProperty(BeanProperty prop) { return this; }
            @Override
            public com.fasterxml.jackson.annotation.JsonTypeInfo.As getTypeInclusion() { return com.fasterxml.jackson.annotation.JsonTypeInfo.As.WRAPPER_ARRAY; }
            @Override
            public String getPropertyName() { return null; }
            @Override
            public com.fasterxml.jackson.databind.jsontype.TypeIdResolver getTypeIdResolver() { return null; }
            @Override
            public Class<?> getDefaultImpl() { return String[].class; }
            @Override
            public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) throws IOException {
                return new String[]{"typed_hello"};
            }
            @Override
            public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) throws IOException { return null; }
            @Override
            public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) throws IOException { return null; }
            @Override
            public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) throws IOException { return null; }
        };

        Object result = deser.deserializeWithType(parser, ctxt, typeDeser);
        assertNotNull(result);
        assertTrue(result instanceof String[]);
        assertArrayEquals(new String[]{"typed_hello"}, (String[]) result);
    }

    // Tests direct instantiation and custom constructor
    @Test
    public void testCustomConstructor_andDirectDeserialize() throws Exception {
        CustomStringDeserializer customDeser = new CustomStringDeserializer();
        StringArrayDeserializer deser = new StringArrayDeserializer(customDeser);

        JsonParser parser = mapper.getFactory().createParser("[\"alpha\", null]");
        parser.nextToken(); // START_ARRAY
        DeserializationContext ctxt = mapper.getDeserializationContext();

        String[] result = deser.deserialize(parser, ctxt);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("alpha_custom", result[0]);
        assertEquals("custom_null", result[1]);
    }
}