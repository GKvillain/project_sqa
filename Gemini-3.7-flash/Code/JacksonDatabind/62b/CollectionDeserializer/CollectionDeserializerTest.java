package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.util.*;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.UnresolvedForwardReference;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class CollectionDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    static class SingleElementWrapper {
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        public List<String> values;
    }

    static class StrictArrayWrapper {
        @JsonFormat(without = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        public List<String> values;
    }

    static class UnmodifiableSetBean {
        public Set<String> values;

        public void setValues(Set<String> v) {
            this.values = Collections.unmodifiableSet(v);
        }
    }

    static class UnmodifiableListBean {
        public List<String> values;

        public void setValues(List<String> v) {
            this.values = Collections.unmodifiableList(v);
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    static class Animal {
        public String name;
    }

    static class Dog extends Animal {
        public boolean barks;
    }

    // Tests normal array deserialization to ArrayList
    @Test
    public void testDeserialize_normalJsonArray_returnsPopulatedList() throws Exception {
        String json = "[\"item1\", \"item2\", \"item3\"]";
        List<String> result = mapper.readValue(json, new TypeReference<List<String>>() {});
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("item1", result.get(0));
        assertEquals("item2", result.get(1));
        assertEquals("item3", result.get(2));
    }

    // Tests empty JSON array deserialization
    @Test
    public void testDeserialize_emptyJsonArray_returnsEmptyList() throws Exception {
        String json = "[]";
        List<String> result = mapper.readValue(json, new TypeReference<List<String>>() {});
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Tests deserialization preserving null values inside array
    @Test
    public void testDeserialize_arrayWithNullElements_containsNull() throws Exception {
        String json = "[\"a\", null, \"b\"]";
        List<String> result = mapper.readValue(json, new TypeReference<List<String>>() {});
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertNull(result.get(1));
        assertEquals("b", result.get(2));
    }

    // Tests deserialization into HashSet
    @Test
    public void testDeserialize_jsonArrayToSet_returnsPopulatedSet() throws Exception {
        String json = "[\"a\", \"b\", \"a\"]";
        Set<String> result = mapper.readValue(json, new TypeReference<HashSet<String>>() {});
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("a"));
        assertTrue(result.contains("b"));
    }

    // Tests deserialization into TreeSet with natural ordering
    @Test
    public void testDeserialize_jsonArrayToTreeSet_returnsSortedSet() throws Exception {
        String json = "[\"c\", \"a\", \"b\"]";
        TreeSet<String> result = mapper.readValue(json, new TypeReference<TreeSet<String>>() {});
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("a", result.first());
        assertEquals("c", result.last());
    }

    // Tests single value unwrapping when ACCEPT_SINGLE_VALUE_AS_ARRAY is globally enabled
    @Test
    public void testDeserialize_singleValueGlobalFeature_returnsSingleElementList() throws Exception {
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "\"singleItem\"";
        List<String> result = mapper.readValue(json, new TypeReference<List<String>>() {});
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("singleItem", result.get(0));
    }

    // Tests single value without ACCEPT_SINGLE_VALUE_AS_ARRAY throws exception
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_singleValueWithoutFeature_throwsJsonMappingException() throws Exception {
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "\"singleItem\"";
        mapper.readValue(json, new TypeReference<List<String>>() {});
    }

    // Tests single value unwrapping per-property via @JsonFormat annotation
    @Test
    public void testDeserialize_singleValuePropertyAnnotation_success() throws Exception {
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "{\"values\": \"annotatedSingle\"}";
        SingleElementWrapper result = mapper.readValue(json, SingleElementWrapper.class);
        assertNotNull(result);
        assertNotNull(result.values);
        assertEquals(1, result.values.size());
        assertEquals("annotatedSingle", result.values.get(0));
    }

    // Tests explicit disabling of single value unwrapping per-property via @JsonFormat annotation
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_strictPropertyAnnotationWithSingleValue_throwsException() throws Exception {
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "{\"values\": \"strictValue\"}";
        mapper.readValue(json, StrictArrayWrapper.class);
    }

    // Tests deserialization of unmodifiable collection types (Defects4J 62 regression test)
    @Test
    public void testDeserialize_unmodifiableSet_returnsPopulatedSet() throws Exception {
        Set<String> original = new HashSet<String>(Arrays.asList("x", "y"));
        Set<String> unmodifiable = Collections.unmodifiableSet(original);
        String json = mapper.writeValueAsString(unmodifiable);
        
        Set<String> deserialized = mapper.readValue(json, new TypeReference<Set<String>>() {});
        assertNotNull(deserialized);
        assertEquals(2, deserialized.size());
        assertTrue(deserialized.contains("x"));
        assertTrue(deserialized.contains("y"));
    }

    // Tests deserialization into bean with unmodifiable collection
    @Test
    public void testDeserialize_unmodifiableSetInBean_success() throws Exception {
        String json = "{\"values\": [\"val1\", \"val2\"]}";
        UnmodifiableSetBean bean = mapper.readValue(json, UnmodifiableSetBean.class);
        assertNotNull(bean);
        assertNotNull(bean.values);
        assertEquals(2, bean.values.size());
        assertTrue(bean.values.contains("val1"));
    }

    // Tests deserialization with polymorphic typed elements
    @Test
    public void testDeserialize_polymorphicElements_returnsSubclassInstances() throws Exception {
        Dog dog = new Dog();
        dog.name = "Rex";
        dog.barks = true;
        List<Animal> animals = Collections.<Animal>singletonList(dog);
        String json = mapper.writeValueAsString(animals);

        List<Animal> result = mapper.readValue(json, new TypeReference<List<Animal>>() {});
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof Dog);
        Dog deserializedDog = (Dog) result.get(0);
        assertEquals("Rex", deserializedDog.name);
        assertTrue(deserializedDog.barks);
    }

    // Tests isCachable returns true when no deserializer or type deserializer is present
    @Test
    public void testIsCachable_noDeserializers_returnsTrue() {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, Object.class);
        CollectionDeserializer deser = new CollectionDeserializer(type, null, null, null);
        assertTrue(deser.isCachable());
    }

    // Tests isCachable returns false when value deserializer is present
    @Test
    public void testIsCachable_withValueDeserializer_returnsFalse() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, String.class);
        JsonDeserializer<Object> valDeser = mapper.getDeserializationContext().findRootValueDeserializer(
                TypeFactory.defaultInstance().constructType(String.class));
        CollectionDeserializer deser = new CollectionDeserializer(type, valDeser, null, null);
        assertFalse(deser.isCachable());
    }

    // Tests getContentType and getContentDeserializer getters
    @Test
    public void testGetContentTypeAndContentDeserializer_returnsExpectedValues() throws Exception {
        JavaType stringType = TypeFactory.defaultInstance().constructType(String.class);
        JavaType collType = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, stringType);
        JsonDeserializer<Object> valDeser = mapper.getDeserializationContext().findRootValueDeserializer(stringType);

        CollectionDeserializer deser = new CollectionDeserializer(collType, valDeser, null, null);
        assertEquals(stringType, deser.getContentType());
        assertEquals(valDeser, deser.getContentDeserializer());
    }

    // Tests CollectionReferringAccumulator forward reference ordering resolution
    @Test
    public void testReferringAccumulator_resolveForwardReference_preservesOrder() throws IOException {
        List<Object> resultList = new ArrayList<Object>();
        CollectionDeserializer.CollectionReferringAccumulator accumulator =
                new CollectionDeserializer.CollectionReferringAccumulator(String.class, resultList);

        // Simulate reading: "first", unresolved(id=1), "second", "third"
        accumulator.add("first");

        ReadableObjectId roid = new ReadableObjectId(new ReadableObjectId.Referring(null, Object.class) {
            @Override
            public void handleResolvedForwardReference(Object id, Object value) {}
        });
        UnresolvedForwardReference ref = new UnresolvedForwardReference(null, "Unresolved ref", null, roid);
        accumulator.handleUnresolvedReference(ref);

        accumulator.add("second");
        accumulator.add("third");

        // Resolve reference id=roid.getKey() with resolved value "resolvedRef"
        accumulator.resolveForwardReference(roid.getKey(), "resolvedRef");

        assertEquals(4, resultList.size());
        assertEquals("first", resultList.get(0));
        assertEquals("resolvedRef", resultList.get(1));
        assertEquals("second", resultList.get(2));
        assertEquals("third", resultList.get(3));
    }

    // Tests CollectionReferringAccumulator exception on unresolved forward reference id
    @Test(expected = IllegalArgumentException.class)
    public void testReferringAccumulator_unseenId_throwsIllegalArgumentException() throws IOException {
        List<Object> resultList = new ArrayList<Object>();
        CollectionDeserializer.CollectionReferringAccumulator accumulator =
                new CollectionDeserializer.CollectionReferringAccumulator(String.class, resultList);

        accumulator.resolveForwardReference("unknownId", "value");
    }

    // Tests exception wrapping when DeserializationFeature.WRAP_EXCEPTIONS is disabled
    @Test(expected = RuntimeException.class)
    public void testDeserialize_wrapExceptionsDisabled_rethrowsRuntimeException() throws Exception {
        mapper.disable(DeserializationFeature.WRAP_EXCEPTIONS);
        String json = "[\"invalid_int\"]";
        mapper.readValue(json, new TypeReference<List<Integer>>() {});
    }

    // Tests exception wrapping when DeserializationFeature.WRAP_EXCEPTIONS is enabled
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_wrapExceptionsEnabled_throwsJsonMappingException() throws Exception {
        mapper.enable(DeserializationFeature.WRAP_EXCEPTIONS);
        String json = "[\"invalid_int\"]";
        mapper.readValue(json, new TypeReference<List<Integer>>() {});
    }
}