package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.AccessPattern;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class StringArrayDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    static class CustomStringDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return "custom:" + p.getText();
        }

        @Override
        public String getNullValue(DeserializationContext ctxt) {
            return "custom:null";
        }

        public String getNullValue() {
            return "custom:null";
        }
    }

    static class CustomArrayBean {
        @JsonDeserialize(contentUsing = CustomStringDeserializer.class)
        public String[] values;
    }

    static class SingleValueBean {
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        public String[] values;
    }

    static class CustomSingleValueBean {
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        @JsonDeserialize(contentUsing = CustomStringDeserializer.class)
        public String[] values;
    }

    // Tests deserialization of normal non-empty String array
    @Test
    public void testDeserialize_normalArray_returnsStringArray() throws Exception {
        String json = "[\"abc\", \"def\", \"ghi\"]";
        String[] result = mapper.readValue(json, String[].class);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("abc", result[0]);
        assertEquals("def", result[1]);
        assertEquals("ghi", result[2]);
    }

    // Tests deserialization of empty JSON array
    @Test
    public void testDeserialize_emptyArray_returnsEmptyArray() throws Exception {
        String json = "[]";
        String[] result = mapper.readValue(json, String[].class);

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    // Tests deserialization of array containing null elements
    @Test
    public void testDeserialize_arrayWithNull_returnsArrayWithNullElement() throws Exception {
        String json = "[\"first\", null, \"third\"]";
        String[] result = mapper.readValue(json, String[].class);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("first", result[0]);
        assertNull(result[1]);
        assertEquals("third", result[2]);
    }

    // Tests deserialization of non-string scalar tokens converting to strings
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

    // Tests deserialization when array exceeds buffer chunk size triggering buffer expansion
    @Test
    public void testDeserialize_largeArray_handlesBufferGrowth() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        int count = 5000;
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"item").append(i).append("\"");
        }
        sb.append("]");

        String[] result = mapper.readValue(sb.toString(), String[].class);

        assertNotNull(result);
        assertEquals(count, result.length);
        assertEquals("item0", result[0]);
        assertEquals("item4999", result[4999]);
    }

    // Tests ACCEPT_SINGLE_VALUE_AS_ARRAY feature with single string value
    @Test
    public void testDeserialize_acceptSingleValueAsArray_returnsSingleElementArray() throws Exception {
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "\"singleValue\"";
        String[] result = mapper.readValue(json, String[].class);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("singleValue", result[0]);
    }

    // Tests ACCEPT_SINGLE_VALUE_AS_ARRAY feature with single null value
    @Test
    public void testDeserialize_acceptSingleValueAsArrayWithNull_returnsArrayWithNull() throws Exception {
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "null";
        String[] result = mapper.readValue(json, String[].class);

        assertNull(result);
    }

    // Tests ACCEPT_SINGLE_VALUE_AS_ARRAY feature with non-string scalar value
    @Test
    public void testDeserialize_acceptSingleValueAsArrayNonString_returnsCoercedArray() throws Exception {
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "12345";
        String[] result = mapper.readValue(json, String[].class);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("12345", result[0]);
    }

    // Tests exception path when non-array input received and ACCEPT_SINGLE_VALUE_AS_ARRAY is disabled
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_nonArrayWithoutSingleValueFeature_throwsJsonMappingException() throws Exception {
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "12345";
        mapper.readValue(json, String[].class);
    }

    // Tests ACCEPT_EMPTY_STRING_AS_NULL_OBJECT feature returning null when input is empty string
    @Test
    public void testDeserialize_acceptEmptyStringAsNullObject_returnsNull() throws Exception {
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        String json = "\"\"";
        String[] result = mapper.readValue(json, String[].class);

        assertNull(result);
    }

    // Tests empty string input throwing exception when ACCEPT_EMPTY_STRING_AS_NULL_OBJECT is disabled
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_emptyStringWithoutFeature_throwsJsonMappingException() throws Exception {
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        String json = "\"\"";
        mapper.readValue(json, String[].class);
    }

    // Tests custom element deserializer via annotation triggering _deserializeCustom
    @Test
    public void testDeserialize_customElementDeserializer_usesCustomDeserializer() throws Exception {
        String json = "{\"values\": [\"a\", \"b\"]}";
        CustomArrayBean bean = mapper.readValue(json, CustomArrayBean.class);

        assertNotNull(bean);
        assertNotNull(bean.values);
        assertEquals(2, bean.values.length);
        assertEquals("custom:a", bean.values[0]);
        assertEquals("custom:b", bean.values[1]);
    }

    // Tests custom element deserializer handling null element value
    @Test
    public void testDeserialize_customElementDeserializerWithNull_usesCustomNullValue() throws Exception {
        String json = "{\"values\": [\"a\", null]}";
        CustomArrayBean bean = mapper.readValue(json, CustomArrayBean.class);

        assertNotNull(bean);
        assertNotNull(bean.values);
        assertEquals(2, bean.values.length);
        assertEquals("custom:a", bean.values[0]);
        assertEquals("custom:null", bean.values[1]);
    }

    // Tests custom element deserializer handling large array chunk expansion
    @Test
    public void testDeserialize_customElementDeserializerLargeArray_handlesBufferGrowth() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"values\": [");
        int count = 1000;
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"val").append(i).append("\"");
        }
        sb.append("]}");

        CustomArrayBean bean = mapper.readValue(sb.toString(), CustomArrayBean.class);

        assertNotNull(bean);
        assertEquals(count, bean.values.length);
        assertEquals("custom:val0", bean.values[0]);
        assertEquals("custom:val999", bean.values[999]);
    }

    // Tests createContextual returning instance when default deserializer is resolved
    @Test
    public void testCreateContextual_defaultDeserializer_returnsInstance() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        StringArrayDeserializer deser = StringArrayDeserializer.instance;
        JsonDeserializer<?> contextual = deser.createContextual(ctxt, null);

        assertNotNull(contextual);
        assertTrue(contextual instanceof StringArrayDeserializer);
    }

    // Tests deserializeWithType delegated call
    @Test
    public void testDeserializeWithType_polymorphicArray_deserializesCorrectly() throws Exception {
        ObjectMapper polyMapper = new ObjectMapper();
        polyMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
        String json = polyMapper.writeValueAsString(new String[]{"item1", "item2"});

        Object result = polyMapper.readValue(json, Object.class);
        assertNotNull(result);
        assertTrue(result instanceof String[]);
        String[] arrayResult = (String[]) result;
        assertEquals(2, arrayResult.length);
        assertEquals("item1", arrayResult[0]);
        assertEquals("item2", arrayResult[1]);
    }

    // Tests JsonFormat annotation for ACCEPT_SINGLE_VALUE_AS_ARRAY per property
    @Test
    public void testDeserialize_formatAcceptSingleValueAsArray_returnsSingleElementArray() throws Exception {
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "{\"values\": \"single\"}";
        SingleValueBean bean = mapper.readValue(json, SingleValueBean.class);

        assertNotNull(bean);
        assertNotNull(bean.values);
        assertEquals(1, bean.values.length);
        assertEquals("single", bean.values[0]);
    }

    // Tests custom element deserializer with ACCEPT_SINGLE_VALUE_AS_ARRAY enabled
    @Test
    public void testDeserialize_customDeserializerSingleValue_returnsSingleElementArray() throws Exception {
        String json = "{\"values\": \"customVal\"}";
        CustomSingleValueBean bean = mapper.readValue(json, CustomSingleValueBean.class);

        assertNotNull(bean);
        assertNotNull(bean.values);
        assertEquals(1, bean.values.length);
        assertEquals("custom:customVal", bean.values[0]);
    }

    // Tests custom element deserializer with single null value when unwrapping is enabled
    @Test
    public void testDeserialize_customDeserializerSingleNullValue_usesCustomNullValue() throws Exception {
        String json = "{\"values\": null}";
        CustomSingleValueBean bean = mapper.readValue(json, CustomSingleValueBean.class);

        assertNotNull(bean);
        assertNull(bean.values);
    }

    // Tests custom element deserializer handling empty string with ACCEPT_EMPTY_STRING_AS_NULL_OBJECT
    @Test
    public void testDeserialize_customDeserializerEmptyStringAsNullObject_returnsNull() throws Exception {
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        String json = "{\"values\": \"\"}";
        CustomArrayBean bean = mapper.readValue(json, CustomArrayBean.class);

        assertNotNull(bean);
        assertNull(bean.values);
    }

    // Tests custom element deserializer throwing exception when non-array received and single value disabled
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_customDeserializerNonArrayDisabled_throwsJsonMappingException() throws Exception {
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "{\"values\": 12345}";
        mapper.readValue(json, CustomArrayBean.class);
    }

    // Tests getEmptyAccessPattern and getEmptyValue methods
    @Test
    public void testGetEmptyValueAndAccessPattern() throws Exception {
        StringArrayDeserializer deser = StringArrayDeserializer.instance;
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Object emptyVal = deser.getEmptyValue(ctxt);
        assertNotNull(emptyVal);
        assertTrue(emptyVal instanceof String[]);
        assertEquals(0, ((String[]) emptyVal).length);

        AccessPattern pattern = deser.getEmptyAccessPattern();
        assertEquals(AccessPattern.CONSTANT, pattern);
    }
}