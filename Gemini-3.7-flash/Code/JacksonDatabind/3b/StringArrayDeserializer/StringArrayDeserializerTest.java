package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class StringArrayDeserializerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // Tests deserializing standard string array containing null value (catches bug 3b)
    @Test
    public void testDeserialize_arrayContainingNull_returnsArrayWithNull() throws Exception {
        String json = "[\"abc\", null, \"def\"]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("abc", result[0]);
        assertNull(result[1]);
        assertEquals("def", result[2]);
    }

    // Tests normal case of deserializing string array
    @Test
    public void testDeserialize_validStringArray_returnsStringArray() throws Exception {
        String json = "[\"a\", \"b\", \"c\"]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertArrayEquals(new String[]{"a", "b", "c"}, result);
    }

    // Tests boundary case of empty array
    @Test
    public void testDeserialize_emptyArray_returnsEmptyArray() throws Exception {
        String json = "[]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    // Tests parsing non-string values inside array to strings
    @Test
    public void testDeserialize_nonStringTokensInArray_convertsToStrings() throws Exception {
        String json = "[123, true, 45.6]";
        String[] result = mapper.readValue(json, String[].class);
        assertNotNull(result);
        assertArrayEquals(new String[]{"123", "true", "45.6"}, result);
    }

    // Tests large array exceeding single buffer chunk
    @Test
    public void testDeserialize_largeArray_expandsBufferAndReturnsAll() throws Exception {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 5000; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"item").append(i).append("\"");
        }
        sb.append("]");

        String[] result = mapper.readValue(sb.toString(), String[].class);
        assertNotNull(result);
        assertEquals(5000, result.length);
        assertEquals("item0", result[0]);
        assertEquals("item4999", result[4999]);
    }

    // Tests single value as array when ACCEPT_SINGLE_VALUE_AS_ARRAY is enabled
    @Test
    public void testDeserialize_singleValueAsArrayEnabled_returnsSingleElementArray() throws Exception {
        ObjectMapper localMapper = new ObjectMapper();
        localMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        String json = "\"single\"";
        String[] result = localMapper.readValue(json, String[].class);
        assertNotNull(result);
        assertArrayEquals(new String[]{"single"}, result);
    }

    // Tests single null value as array when ACCEPT_SINGLE_VALUE_AS_ARRAY is enabled
    @Test
    public void testDeserialize_singleNullAsArrayEnabled_returnsSingleNullElementArray() throws Exception {
        ObjectMapper localMapper = new ObjectMapper();
        localMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        String json = "null";
        String[] result = localMapper.readValue(json, String[].class);
        assertNull(result);
    }

    // Tests non-array value when ACCEPT_SINGLE_VALUE_AS_ARRAY is disabled (throws JsonMappingException)
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_singleValueAsArrayDisabled_throwsJsonMappingException() throws Exception {
        String json = "\"notAnArray\"";
        mapper.readValue(json, String[].class);
    }

    // Tests empty string as null object when ACCEPT_EMPTY_STRING_AS_NULL_OBJECT is enabled
    @Test
    public void testDeserialize_emptyStringAsNullObjectEnabled_returnsNull() throws Exception {
        ObjectMapper localMapper = new ObjectMapper();
        localMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        String json = "\"\"";
        String[] result = localMapper.readValue(json, String[].class);
        assertNull(result);
    }

    // Tests empty string when ACCEPT_EMPTY_STRING_AS_NULL_OBJECT is disabled (throws JsonMappingException)
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_emptyStringAsNullObjectDisabled_throwsJsonMappingException() throws Exception {
        ObjectMapper localMapper = new ObjectMapper();
        localMapper.disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        String json = "\"\"";
        localMapper.readValue(json, String[].class);
    }

    // Tests deserialization with custom string element deserializer
    @Test
    public void testDeserialize_customElementDeserializer_usesCustomDeserializer() throws Exception {
        ObjectMapper localMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new JsonDeserializer<String>() {
            @Override
            public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
                return jp.getText().toUpperCase();
            }
        });
        localMapper.registerModule(module);

        String json = "[\"abc\", null, \"xyz\"]";
        String[] result = localMapper.readValue(json, String[].class);
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("ABC", result[0]);
        assertNull(result[1]);
        assertEquals("XYZ", result[2]);
    }

    // Tests polymorphic deserialization with type information
    @Test
    public void testDeserializeWithType_typedArray_returnsCorrectArray() throws Exception {
        ObjectMapper localMapper = new ObjectMapper();
        localMapper.enableDefaultTyping();

        String[] original = new String[]{"first", "second"};
        String json = localMapper.writeValueAsString(original);
        String[] result = localMapper.readValue(json, String[].class);

        assertNotNull(result);
        assertArrayEquals(original, result);
    }

    // Tests createContextual returns instance itself when default deserializer is present
    @Test
    public void testCreateContextual_defaultDeserializer_returnsSameOrEquivalentInstance() throws Exception {
        StringArrayDeserializer deser = StringArrayDeserializer.instance;
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonDeserializer<?> contextualDeser = deser.createContextual(ctxt, null);
        assertNotNull(contextualDeser);
        assertTrue(contextualDeser instanceof StringArrayDeserializer);
    }
}