package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.impl.NullsConstantProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class StringCollectionDeserializerTest {

    private final ObjectMapper MAPPER = new ObjectMapper();

    static class CustomStringDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.getText() + "-custom";
        }
    }

    static class SingleStringCollectionWrapper {
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        public Collection<String> values;
    }

    static class SkipNullCollectionWrapper {
        @JsonSetter(contentNulls = Nulls.SKIP)
        public List<String> values;
    }

    static class DelegatingCollection {
        private final Collection<String> values;

        @JsonCreator
        public DelegatingCollection(Collection<String> values) {
            this.values = values;
        }

        public Collection<String> getValues() {
            return values;
        }
    }

    // Tests normal deserialization of a standard JSON string array
    @Test
    public void testDeserialize_standardArray_returnsCollection() throws Exception {
        List<String> result = MAPPER.readValue("[\"apple\", \"banana\", \"cherry\"]",
                new TypeReference<List<String>>() {});

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("apple", result.get(0));
        assertEquals("banana", result.get(1));
        assertEquals("cherry", result.get(2));
    }

    // Tests deserialization of an empty JSON array
    @Test
    public void testDeserialize_emptyArray_returnsEmptyCollection() throws Exception {
        Collection<String> result = MAPPER.readValue("[]",
                new TypeReference<Collection<String>>() {});

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Tests deserialization of array containing null elements
    @Test
    public void testDeserialize_arrayWithNull_preservesNull() throws Exception {
        List<String> result = MAPPER.readValue("[\"a\", null, \"b\"]",
                new TypeReference<List<String>>() {});

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertNull(result.get(1));
        assertEquals("b", result.get(2));
    }

    // Tests skip nulls configuration in collection deserialization
    @Test
    public void testDeserialize_skipNulls_omitsNullValues() throws Exception {
        SkipNullCollectionWrapper wrapper = MAPPER.readValue("{\"values\":[\"a\", null, \"b\"]}",
                SkipNullCollectionWrapper.class);

        assertNotNull(wrapper.values);
        assertEquals(2, wrapper.values.size());
        assertEquals("a", wrapper.values.get(0));
        assertEquals("b", wrapper.values.get(1));
    }

    // Tests deserialization with custom element value deserializer
    @Test
    public void testDeserialize_customValueDeserializer_usesCustomLogic() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new CustomStringDeserializer());
        mapper.registerModule(module);

        List<String> result = mapper.readValue("[\"first\", \"second\"]",
                new TypeReference<List<String>>() {});

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("first-custom", result.get(0));
        assertEquals("second-custom", result.get(1));
    }

    // Tests single value unwrapping when enabled via JsonFormat annotation
    @Test
    public void testDeserialize_singleValueWithAnnotation_unwrapsSuccessfully() throws Exception {
        SingleStringCollectionWrapper wrapper = MAPPER.readValue("{\"values\":\"singleItem\"}",
                SingleStringCollectionWrapper.class);

        assertNotNull(wrapper.values);
        assertEquals(1, wrapper.values.size());
        assertEquals("singleItem", wrapper.values.iterator().next());
    }

    // Tests single value unwrapping with global ACCEPT_SINGLE_VALUE_AS_ARRAY feature enabled
    @Test
    public void testDeserialize_singleValueFeatureEnabled_returnsCollection() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        List<String> result = mapper.readValue("\"singleElement\"",
                new TypeReference<List<String>>() {});

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("singleElement", result.get(0));
    }

    // Tests single value unwrapping for null value when feature is enabled
    @Test
    public void testDeserialize_singleNullFeatureEnabled_returnsCollectionWithNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        List<String> result = mapper.readValue("null",
                new TypeReference<List<String>>() {});

        assertNull(result);
    }

    // Tests exception path when non-array token is encountered and unwrapping is disabled
    @Test(expected = MismatchedInputException.class)
    public void testDeserialize_singleValueFeatureDisabled_throwsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        mapper.readValue("12345", new TypeReference<List<String>>() {});
    }

    // Tests deserialization through delegating creator
    @Test
    public void testDeserialize_delegatingCreator_createsInstance() throws Exception {
        DelegatingCollection result = MAPPER.readValue("[\"x\", \"y\"]", DelegatingCollection.class);

        assertNotNull(result);
        assertNotNull(result.getValues());
        assertEquals(2, result.getValues().size());
        assertTrue(result.getValues().contains("x"));
        assertTrue(result.getValues().contains("y"));
    }

    // Tests isCachable method when standard vs custom deserializers are used
    @Test
    public void testIsCachable_standardAndCustomDeserializer_returnsExpectedBoolean() {
        JavaType type = MAPPER.getTypeFactory().constructCollectionType(List.class, String.class);
        StringCollectionDeserializer standardDeser = new StringCollectionDeserializer(type, null, null);

        assertTrue(standardDeser.isCachable());

        StringCollectionDeserializer customDeser = standardDeser.withResolved(
                null, new CustomStringDeserializer(), null, Boolean.TRUE);

        assertFalse(customDeser.isCachable());
    }

    // Tests withResolved method returns same instance if arguments match existing fields
    @Test
    public void testWithResolved_sameArguments_returnsSameInstance() {
        JavaType type = MAPPER.getTypeFactory().constructCollectionType(List.class, String.class);
        StringCollectionDeserializer deser = new StringCollectionDeserializer(type, null, null);

        StringCollectionDeserializer resolved = deser.withResolved(null, null, null, null);

        assertSame(deser, resolved);
    }

    // Tests withResolved method returns new instance when configuration changes
    @Test
    public void testWithResolved_differentArguments_returnsNewInstance() {
        JavaType type = MAPPER.getTypeFactory().constructCollectionType(List.class, String.class);
        StringCollectionDeserializer deser = new StringCollectionDeserializer(type, null, null);
        NullValueProvider nuller = NullsConstantProvider.nuller();

        StringCollectionDeserializer resolved = deser.withResolved(null, null, nuller, Boolean.TRUE);

        assertNotSame(deser, resolved);
    }

    // Tests getContentDeserializer and getValueInstantiator getters
    @Test
    public void testGetContentDeserializerAndValueInstantiator_uninitialized_returnsNullOrSetValues() {
        JavaType type = MAPPER.getTypeFactory().constructCollectionType(Set.class, String.class);
        CustomStringDeserializer customDeser = new CustomStringDeserializer();
        StringCollectionDeserializer deser = new StringCollectionDeserializer(type, customDeser, null);

        assertSame(customDeser, deser.getContentDeserializer());
        assertNull(deser.getValueInstantiator());
    }

    // Tests contextual creation with custom content deserializer and unwrapSingle
    @Test
    public void testCreateContextual_withCustomDeserializer_resolvesContextualDeserializer() throws Exception {
        DeserializationContext ctxt = MAPPER.getDeserializationContext();
        JavaType type = MAPPER.getTypeFactory().constructCollectionType(List.class, String.class);
        StringCollectionDeserializer deser = new StringCollectionDeserializer(type, new CustomStringDeserializer(), null);

        JsonDeserializer<?> contextual = deser.createContextual(ctxt, null);

        assertNotNull(contextual);
        assertTrue(contextual instanceof StringCollectionDeserializer);
    }
}