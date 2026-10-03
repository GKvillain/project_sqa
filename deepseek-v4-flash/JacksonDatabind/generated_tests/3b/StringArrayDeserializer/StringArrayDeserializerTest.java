package com.fasterxml.jackson.databind.deser.std;

import static org.junit.Assert.*;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;

import org.junit.Test;

public class StringArrayDeserializerTest {

    // Helper to create ObjectMapper with given features
    private ObjectMapper mapperWithFeatures(DeserializationFeature... features) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, false);
        for (DeserializationFeature f : features) {
            mapper.enable(f);
        }
        return mapper;
    }

    // Tests normal array deserialization
    @Test
    public void testDeserialize_normalArray_returnsArray() throws Exception {
        ObjectMapper mapper = mapperWithFeatures();
        String[] result = mapper.readValue("[\"a\",\"b\"]", String[].class);
        assertArrayEquals(new String[]{"a", "b"}, result);
    }

    // Tests empty array
    @Test
    public void testDeserialize_emptyArray_returnsEmptyArray() throws Exception {
        ObjectMapper mapper = mapperWithFeatures();
        String[] result = mapper.readValue("[]", String[].class);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    // Tests array with null element – bug path: NPE when _elementDeserializer is null
    @Test
    public void testDeserialize_nullElementInArray_returnsArrayWithNull() throws Exception {
        ObjectMapper mapper = mapperWithFeatures();
        // Buggy version throws NullPointerException, correct version returns [null]
        String[] result = mapper.readValue("[null]", String[].class);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertNull(result[0]);
    }

    // Tests array with mixed null and strings
    @Test
    public void testDeserialize_arrayWithNullAndString_returnsArray() throws Exception {
        ObjectMapper mapper = mapperWithFeatures();
        String[] result = mapper.readValue("[\"x\",null,\"y\"]", String[].class);
        assertArrayEquals(new String[]{"x", null, "y"}, result);
    }

    // Tests single value as array when feature enabled
    @Test
    public void testDeserialize_singleValueAsArrayEnabled_returnsArray() throws Exception {
        ObjectMapper mapper = mapperWithFeatures(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String[] result = mapper.readValue("\"hello\"", String[].class);
        assertArrayEquals(new String[]{"hello"}, result);
    }

    // Tests single null value as array when feature enabled
    @Test
    public void testDeserialize_singleNullAsArrayEnabled_returnsArrayWithNull() throws Exception {
        ObjectMapper mapper = mapperWithFeatures(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String[] result = mapper.readValue("null", String[].class);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertNull(result[0]);
    }

    // Tests non-array without ACCEPT_SINGLE_VALUE_AS_ARRAY -> exception
    @Test(expected = com.fasterxml.jackson.databind.exc.MismatchedInputException.class)
    public void testDeserialize_singleValueAsArrayDisabled_throwsException() throws Exception {
        ObjectMapper mapper = mapperWithFeatures();
        mapper.readValue("\"hello\"", String[].class);
    }

    // Tests empty string with ACCEPT_EMPTY_STRING_AS_NULL_OBJECT enabled and single value as array disabled
    // Bug path: returns null instead of array with null element
    @Test
    public void testDeserialize_emptyStringWithAcceptEmptyStringAsNullObject_returnsArrayWithNull() throws Exception {
        ObjectMapper mapper = mapperWithFeatures(
                DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        // Buggy version returns null; correct version returns [null]
        String[] result = mapper.readValue("\"\"", String[].class);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertNull(result[0]);
    }

    // Tests empty string without ACCEPT_EMPTY_STRING_AS_NULL_OBJECT and with single value as array disabled -> exception
    @Test(expected = com.fasterxml.jackson.databind.exc.MismatchedInputException.class)
    public void testDeserialize_emptyStringWithoutAcceptEmptyString_throwsException() throws Exception {
        ObjectMapper mapper = mapperWithFeatures();
        mapper.readValue("\"\"", String[].class);
    }

    // Tests empty string with both ACCEPT_SINGLE_VALUE_AS_ARRAY and ACCEPT_EMPTY_STRING_AS_NULL_OBJECT enabled
    // Should return array containing empty string (not null)
    @Test
    public void testDeserialize_emptyStringWithBothFeatures_returnsArrayWithEmptyString() throws Exception {
        ObjectMapper mapper = mapperWithFeatures(
                DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
                DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        String[] result = mapper.readValue("\"\"", String[].class);
        assertArrayEquals(new String[]{""}, result);
    }

    // Tests array containing empty string element
    @Test
    public void testDeserialize_arrayWithEmptyString_returnsArrayWithEmptyString() throws Exception {
        ObjectMapper mapper = mapperWithFeatures();
        String[] result = mapper.readValue("[\"\"]", String[].class);
        assertArrayEquals(new String[]{""}, result);
    }

    // Tests array with multiple nulls
    @Test
    public void testDeserialize_arrayWithMultipleNulls_returnsArray() throws Exception {
        ObjectMapper mapper = mapperWithFeatures();
        String[] result = mapper.readValue("[null,null]", String[].class);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertNull(result[0]);
        assertNull(result[1]);
    }

    // Tests custom element deserializer path (_deserializeCustom)
    @Test
    public void testDeserialize_customElementDeserializer_usesCustomDeserializer() throws Exception {
        // Custom deserializer that converts string to uppercase
        JsonDeserializer<String> customDeser = new JsonDeserializer<String>() {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return p.getText().toUpperCase();
            }
            @Override
            public String getNullValue() {
                return null;
            }
        };
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, customDeser);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        // Result should have uppercase values
        String[] result = mapper.readValue("[\"hello\",null,\"world\"]", String[].class);
        assertArrayEquals(new String[]{"HELLO", null, "WORLD"}, result);
    }

    // Tests that handleNonArray throws exception when single value as array is disabled and no empty string handling
    @Test(expected = com.fasterxml.jackson.databind.exc.MismatchedInputException.class)
    public void testHandleNonArray_nonArrayWithoutFeatures_throwsException() throws Exception {
        ObjectMapper mapper = mapperWithFeatures();
        // Token is a string, not array
        mapper.readValue("\"test\"", String[].class);
    }
}