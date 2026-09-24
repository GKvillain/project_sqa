package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class StringArrayDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // Tests normal deserialization of a String array with multiple elements
    @Test
    public void testDeserialize_standardArray_returnsStringArray() throws Exception {
        String json = "[\"abc\", \"def\", \"ghi\"]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertArrayEquals(new String[] { "abc", "def", "ghi" }, result);
    }

    // Tests deserialization of an empty array
    @Test
    public void testDeserialize_emptyArray_returnsEmptyStringArray() throws Exception {
        String json = "[]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    // Tests deserialization containing null value using default deserializer (Defects4J bug 3b)
    @Test
    public void testDeserialize_arrayWithNull_returnsArrayWithNullElement() throws Exception {
        String json = "[\"abc\", null, \"def\"]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("abc", result[0]);
        assertNull(result[1]);
        assertEquals("def", result[2]);
    }

    // Tests deserialization containing only null values
    @Test
    public void testDeserialize_arrayWithOnlyNulls_returnsArrayWithNulls() throws Exception {
        String json = "[null, null]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertNull(result[0]);
        assertNull(result[1]);
    }

    // Tests conversion of non-string tokens (number, boolean) to strings in default deserializer
    @Test
    public void testDeserialize_arrayWithNonStringTokens_convertsToStringArray() throws Exception {
        String json = "[123, true, 45.6]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertArrayEquals(new String[] { "123", "true", "45.6" }, result);
    }

    // Tests single value as array when ACCEPT_SINGLE_VALUE_AS_ARRAY feature is enabled
    @Test
    public void testDeserialize_singleValueWhenFeatureEnabled_returnsSingleElementArray() throws Exception {
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "\"single\"";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertArrayEquals(new String[] { "single" }, result);
    }

    // Tests single null value as array when ACCEPT_SINGLE_VALUE_AS_ARRAY is enabled
    @Test
    public void testDeserialize_singleNullWhenFeatureEnabled_returnsArrayWithNull() throws Exception {
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "null";
        String[] result = mapper.readValue(json, String[].class);
        assertNull(result);
    }

    // Tests single non-string token as array when ACCEPT_SINGLE_VALUE_AS_ARRAY is enabled
    @Test
    public void testDeserialize_singleNumberWhenFeatureEnabled_returnsConvertedStringArray() throws Exception {
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "12345";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertArrayEquals(new String[] { "12345" }, result);
    }

    // Tests empty string becoming null when ACCEPT_EMPTY_STRING_AS_NULL_OBJECT is enabled
    @Test
    public void testDeserialize_emptyStringWhenAcceptEmptyStringEnabled_returnsNull() throws Exception {
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        String json = "\"\"";
        String[] result = mapper.readValue(json, String[].class);
        assertNull(result);
    }

    // Tests exception path when non-array is encountered and ACCEPT_SINGLE_VALUE_AS_ARRAY is disabled
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_nonArrayWhenFeatureDisabled_throwsJsonMappingException() throws Exception {
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "12345";
        mapper.readValue(json, String[].class);
    }

    // Tests custom element deserializer via contextualization
    @Test
    public void testDeserialize_customElementDeserializer_usesCustomDeserializer() throws Exception {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new JsonDeserializer<String>() {
            @Override
            public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                return jp.getText().toUpperCase();
            }
        });
        mapper.registerModule(module);

        String json = "[\"abc\", \"def\"]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertArrayEquals(new String[] { "ABC", "DEF" }, result);
    }

    // Tests custom element deserializer with null value in array
    @Test
    public void testDeserialize_customElementDeserializerWithNull_returnsNullElement() throws Exception {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new JsonDeserializer<String>() {
            @Override
            public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                return jp.getText().toUpperCase();
            }
        });
        mapper.registerModule(module);

        String json = "[\"abc\", null, \"def\"]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("ABC", result[0]);
        assertNull(result[1]);
        assertEquals("DEF", result[2]);
    }

    // Tests buffer expansion with large array
    @Test
    public void testDeserialize_largeArray_expandsBufferCorrectly() throws Exception {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 1500; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"item").append(i).append("\"");
        }
        sb.append("]");

        String[] result = mapper.readValue(sb.toString(), String[].class);
        assertNotNull(result);
        assertEquals(1500, result.length);
        assertEquals("item0", result[0]);
        assertEquals("item1499", result[1499]);
    }
}