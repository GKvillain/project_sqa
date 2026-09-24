package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class StringCollectionDeserializerTest {

    private final ObjectMapper MAPPER = new ObjectMapper();

    // Helper classes for testing Jackson annotations with StringCollectionDeserializer
    static class CustomStringDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.getText().toUpperCase();
        }
    }

    static class CustomListWrapper {
        @JsonDeserialize(contentUsing = CustomStringDeserializer.class)
        public Collection<String> values;
    }

    static class SingleWrapListWrapper {
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        public List<String> list;
    }

    static class SkipNullListWrapper {
        @JsonSetter(contentNulls = Nulls.SKIP)
        public List<String> values;
    }

    static class DelegatingListWrapper {
        public final List<String> values;

        public DelegatingListWrapper(List<String> values) {
            this.values = values;
        }

        public static DelegatingListWrapper fromString(String str) {
            return new DelegatingListWrapper(Collections.singletonList(str));
        }
    }

    // Tests normal deserialization of standard JSON array to Collection/List/Set
    @Test
    public void testDeserialize_standardArray_returnsCollection() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        List<String> result = MAPPER.readValue("[\"a\", \"b\", \"c\"]", type);
        assertEquals(Arrays.asList("a", "b", "c"), result);
    }

    // Tests deserialization of empty JSON array
    @Test
    public void testDeserialize_emptyArray_returnsEmptyCollection() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        List<String> result = MAPPER.readValue("[]", type);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Tests deserialization of array containing null elements
    @Test
    public void testDeserialize_nullElementsInArray_retainsNull() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        List<String> result = MAPPER.readValue("[\"a\", null, \"b\"]", type);
        assertEquals(Arrays.asList("a", null, "b"), result);
    }

    // Tests deserialization with custom value deserializer (Defects4J 112b contextualization check)
    @Test
    public void testDeserialize_customContentDeserializer_appliesCustomLogic() throws Exception {
        CustomListWrapper wrapper = MAPPER.readValue("{\"values\": [\"abc\", \"def\"]}", CustomListWrapper.class);
        assertNotNull(wrapper.values);
        assertEquals(Arrays.asList("ABC", "DEF"), new ArrayList<String>(wrapper.values));
    }

    // Tests custom deserializer with null elements in array
    @Test
    public void testDeserialize_customContentDeserializerWithNull_appliesNullHandling() throws Exception {
        CustomListWrapper wrapper = MAPPER.readValue("{\"values\": [\"abc\", null]}", CustomListWrapper.class);
        assertNotNull(wrapper.values);
        assertEquals(Arrays.asList("ABC", null), new ArrayList<String>(wrapper.values));
    }

    // Tests ACCEPT_SINGLE_VALUE_AS_ARRAY feature enabled via ObjectMapper
    @Test
    public void testHandleNonArray_acceptSingleValueEnabled_returnsSingleElementList() throws Exception {
        ObjectMapper mapper = new ObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        List<String> result = mapper.readValue("\"single\"", type);
        assertEquals(Collections.singletonList("single"), result);
    }

    // Tests single value unwrapping via @JsonFormat annotation on property
    @Test
    public void testHandleNonArray_jsonFormatAnnotation_unwrapsSingleValue() throws Exception {
        SingleWrapListWrapper wrapper = MAPPER.readValue("{\"list\": \"singleValue\"}", SingleWrapListWrapper.class);
        assertNotNull(wrapper.list);
        assertEquals(Collections.singletonList("singleValue"), wrapper.list);
    }

    // Tests single value null when ACCEPT_SINGLE_VALUE_AS_ARRAY is enabled
    @Test
    public void testHandleNonArray_nullSingleValue_returnsListWithNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        List<String> result = mapper.readValue("null", type);
        assertNull(result);
    }

    // Tests non-array input when ACCEPT_SINGLE_VALUE_AS_ARRAY is disabled throws MismatchedInputException
    @Test(expected = MismatchedInputException.class)
    public void testHandleNonArray_singleValueDisabled_throwsException() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        MAPPER.readValue("123", type);
    }

    // Tests skipping null values using JsonSetter Nulls.SKIP
    @Test
    public void testDeserialize_skipNullValues_skipsNullInArray() throws Exception {
        SkipNullListWrapper wrapper = MAPPER.readValue("{\"values\": [\"a\", null, \"b\"]}", SkipNullListWrapper.class);
        assertNotNull(wrapper.values);
        assertEquals(Arrays.asList("a", "b"), wrapper.values);
    }

    // Tests isCachable method
    @Test
    public void testIsCachable_standardDeserializer_returnsTrue() {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        StringCollectionDeserializer deser = new StringCollectionDeserializer(type, null, null);
        assertTrue(deser.isCachable());
    }

    // Tests isCachable when custom deserializer is present
    @Test
    public void testIsCachable_withCustomDeserializer_returnsFalse() {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        StringCollectionDeserializer deser = new StringCollectionDeserializer(type, new CustomStringDeserializer(), null);
        assertFalse(deser.isCachable());
    }

    // Tests getContentDeserializer and getValueInstantiator methods
    @Test
    public void testGetters_contentDeserializerAndValueInstantiator() {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        CustomStringDeserializer customDeser = new CustomStringDeserializer();
        StringCollectionDeserializer deser = new StringCollectionDeserializer(type, customDeser, null);
        assertSame(customDeser, deser.getContentDeserializer());
        assertNull(deser.getValueInstantiator());
    }

    // Tests withResolved returns same instance when identical parameters are passed
    @Test
    public void testWithResolved_sameParameters_returnsThis() {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        StringCollectionDeserializer deser = new StringCollectionDeserializer(type, null, null);
        StringCollectionDeserializer resolved = deser.withResolved(null, null, null, null);
        assertSame(deser, resolved);
    }

    // Tests deserialization of numbers/booleans in String collection (converts to string)
    @Test
    public void testDeserialize_mixedTypesToString_convertsSuccessfully() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        List<String> result = MAPPER.readValue("[123, true, \"hello\"]", type);
        assertEquals(Arrays.asList("123", "true", "hello"), result);
    }

    // Tests deserialization into Set collection type
    @Test
    public void testDeserialize_setCollection_returnsCorrectSet() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(Set.class, String.class);
        Set<String> result = MAPPER.readValue("[\"x\", \"y\", \"x\"]", type);
        assertEquals(new HashSet<String>(Arrays.asList("x", "y")), result);
    }
}