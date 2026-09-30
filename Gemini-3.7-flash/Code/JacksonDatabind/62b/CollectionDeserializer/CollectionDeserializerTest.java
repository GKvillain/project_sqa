package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.UnresolvedForwardReference;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsArrayTypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class CollectionDeserializerTest {

    private final ObjectMapper _mapper = new ObjectMapper();

    // Tests isCachable returns true when all child deserializers are null
    @Test
    public void testIsCachable_noChildDeserializers_returnsTrue() {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, String.class);
        CollectionDeserializer deser = new CollectionDeserializer(type, null, null, null);
        assertTrue(deser.isCachable());
    }

    // Tests isCachable returns false when value deserializer is present
    @Test
    public void testIsCachable_withValueDeserializer_returnsFalse() {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, String.class);
        JsonDeserializer<Object> valDeser = new UntypedObjectDeserializer(null, null);
        CollectionDeserializer deser = new CollectionDeserializer(type, valDeser, null, null);
        assertFalse(deser.isCachable());
    }

    // Tests getContentType returns correct JavaType of element
    @Test
    public void testGetContentType_validType_returnsElementJavaType() {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, Integer.class);
        CollectionDeserializer deser = new CollectionDeserializer(type, null, null, null);
        assertEquals(Integer.class, deser.getContentType().getRawClass());
    }

    // Tests getContentDeserializer returns configured value deserializer
    @Test
    public void testGetContentDeserializer_configuredDeserializer_returnsSameInstance() {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, String.class);
        JsonDeserializer<Object> valDeser = new UntypedObjectDeserializer(null, null);
        CollectionDeserializer deser = new CollectionDeserializer(type, valDeser, null, null);
        assertSame(valDeser, deser.getContentDeserializer());
    }

    // Tests withResolved returns same instance if arguments have not changed
    @Test
    public void testWithResolved_sameArguments_returnsSameInstance() {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, String.class);
        CollectionDeserializer deser = new CollectionDeserializer(type, null, null, null);
        CollectionDeserializer resolved = deser.withResolved(null, null, null, null);
        assertSame(deser, resolved);
    }

    // Tests withResolved returns new instance when arguments change
    @Test
    public void testWithResolved_differentArguments_returnsNewInstance() {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, String.class);
        CollectionDeserializer deser = new CollectionDeserializer(type, null, null, null);
        CollectionDeserializer resolved = deser.withResolved(null, null, null, Boolean.TRUE);
        assertNotSame(deser, resolved);
    }

    // Tests normal deserialization of JSON array to Collection
    @Test
    public void testDeserialize_normalArray_returnsPopulatedCollection() throws IOException {
        String json = "[\"apple\", \"banana\", \"cherry\"]";
        Collection<String> result = _mapper.readValue(json, new TypeReference<Collection<String>>() {});
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("apple"));
        assertTrue(result.contains("banana"));
        assertTrue(result.contains("cherry"));
    }

    // Tests deserialization of empty JSON array
    @Test
    public void testDeserialize_emptyArray_returnsEmptyCollection() throws IOException {
        String json = "[]";
        Collection<String> result = _mapper.readValue(json, new TypeReference<ArrayList<String>>() {});
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Tests deserialization with null elements in JSON array
    @Test
    public void testDeserialize_arrayWithNullValues_preservesNulls() throws IOException {
        String json = "[\"a\", null, \"b\"]";
        List<String> result = _mapper.readValue(json, new TypeReference<List<String>>() {});
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertNull(result.get(1));
        assertEquals("b", result.get(2));
    }

    // Tests deserializing single value when ACCEPT_SINGLE_VALUE_AS_ARRAY is enabled
    @Test
    public void testDeserialize_singleValueWithFeatureEnabled_wrapsInCollection() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        Collection<String> result = mapper.readValue("\"single\"", new TypeReference<Collection<String>>() {});
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains("single"));
    }

    // Tests deserializing single value when ACCEPT_SINGLE_VALUE_AS_ARRAY is disabled throws exception
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_singleValueWithFeatureDisabled_throwsException() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.readValue("\"single\"", new TypeReference<Collection<String>>() {});
    }

    // Helper wrapper class for format annotation test
    static class SingleValueWrapper {
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        public Collection<String> values;
    }

    // Tests per-property @JsonFormat ACCEPT_SINGLE_VALUE_AS_ARRAY override
    @Test
    public void testDeserialize_perPropertyAcceptSingleValue_wrapsInCollection() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "{\"values\": \"singleValue\"}";
        SingleValueWrapper wrapper = mapper.readValue(json, SingleValueWrapper.class);
        assertNotNull(wrapper.values);
        assertEquals(1, wrapper.values.size());
        assertTrue(wrapper.values.contains("singleValue"));
    }

    // Tests CollectionReferringAccumulator ordering and resolveForwardReference
    @Test
    public void testReferringAccumulator_resolveForwardReference_maintainsOrder() throws IOException {
        List<Object> result = new ArrayList<Object>();
        CollectionDeserializer.CollectionReferringAccumulator accumulator =
                new CollectionDeserializer.CollectionReferringAccumulator(String.class, result);

        accumulator.add("first");
        assertEquals(1, result.size());
        assertEquals("first", result.get(0));
    }

    // Tests CollectionReferringAccumulator throws exception on unknown ID resolution
    @Test(expected = IllegalArgumentException.class)
    public void testReferringAccumulator_unknownId_throwsException() throws IOException {
        List<Object> result = new ArrayList<Object>();
        CollectionDeserializer.CollectionReferringAccumulator accumulator =
                new CollectionDeserializer.CollectionReferringAccumulator(String.class, result);
        accumulator.resolveForwardReference("unknownId", "value");
    }

    // Tests deserialization of Set collection types
    @Test
    public void testDeserialize_setToSetCollection_returnsSetInstance() throws IOException {
        String json = "[1, 2, 2, 3]";
        Set<Integer> result = _mapper.readValue(json, new TypeReference<Set<Integer>>() {});
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(1));
        assertTrue(result.contains(2));
        assertTrue(result.contains(3));
    }

    // --- New Tests for Full Coverage ---

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Dog.class, name = "dog"),
        @JsonSubTypes.Type(value = Cat.class, name = "cat")
    })
    static abstract class Animal {
        public String name;
    }

    static class Dog extends Animal {
        public int barkVolume;
    }

    static class Cat extends Animal {
        public boolean likesLaser;
    }

    // Tests deserialization of collection containing polymorphic types (with TypeDeserializer)
    @Test
    public void testDeserialize_polymorphicElements_deserializesCorrectSubtypes() throws IOException {
        String json = "[{\"type\":\"dog\",\"name\":\"Rex\",\"barkVolume\":10},"
                + "{\"type\":\"cat\",\"name\":\"Whiskers\",\"likesLaser\":true}]";
        List<Animal> animals = _mapper.readValue(json, new TypeReference<List<Animal>>() {});
        assertNotNull(animals);
        assertEquals(2, animals.size());
        assertTrue(animals.get(0) instanceof Dog);
        assertEquals("Rex", animals.get(0).name);
        assertEquals(10, ((Dog) animals.get(0)).barkVolume);
        assertTrue(animals.get(1) instanceof Cat);
        assertEquals("Whiskers", animals.get(1).name);
        assertTrue(((Cat) animals.get(1)).likesLaser);
    }

    // Tests updating an existing collection instance via readerForUpdating
    @Test
    public void testDeserialize_updatingExistingCollection_appendsValues() throws IOException {
        List<String> existing = new ArrayList<String>();
        existing.add("existing1");
        String json = "[\"new1\", \"new2\"]";

        List<String> result = _mapper.readerForUpdating(existing).readValue(json);
        assertSame(existing, result);
        assertEquals(3, result.size());
        assertEquals("existing1", result.get(0));
        assertEquals("new1", result.get(1));
        assertEquals("new2", result.get(2));
    }

    // Tests per-property @JsonFormat ACCEPT_SINGLE_VALUE_AS_ARRAY = FALSE when globally enabled
    static class ExplicitDisableSingleValueWrapper {
        @JsonFormat(without = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        public Collection<String> values;
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserialize_perPropertyDisabledWhenGloballyEnabled_throwsException() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "{\"values\": \"singleValue\"}";
        mapper.readValue(json, ExplicitDisableSingleValueWrapper.class);
    }

    // Tests single value deserialization with null value when ACCEPT_SINGLE_VALUE_AS_ARRAY is enabled
    @Test
    public void testDeserialize_singleNullValueWithFeatureEnabled_wrapsNullInCollection() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        Collection<String> result = mapper.readValue("null", new TypeReference<Collection<String>>() {});
        assertNull(result);
    }

    // Tests forward reference resolution within CollectionReferringAccumulator
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class IdentifiedNode {
        public int id;
        public List<IdentifiedNode> neighbors = new ArrayList<IdentifiedNode>();

        public IdentifiedNode() {}
        public IdentifiedNode(int id) { this.id = id; }
    }

    @Test
    public void testDeserialize_forwardReferencesInCollection_resolvesCorrectly() throws IOException {
        String json = "{\"node1\": {\"id\": 1, \"neighbors\": [2]}, \"node2\": {\"id\": 2, \"neighbors\": [1]}}";
        Map<String, IdentifiedNode> nodes = _mapper.readValue(json, new TypeReference<Map<String, IdentifiedNode>>() {});
        assertNotNull(nodes);
        IdentifiedNode n1 = nodes.get("node1");
        IdentifiedNode n2 = nodes.get("node2");
        assertNotNull(n1);
        assertNotNull(n2);
        assertEquals(1, n1.neighbors.size());
        assertSame(n2, n1.neighbors.get(0));
        assertEquals(1, n2.neighbors.size());
        assertSame(n1, n2.neighbors.get(0));
    }

    // Tests CollectionReferringAccumulator multiple forward references handling and ordering
    @Test
    public void testReferringAccumulator_multipleForwardReferences_resolvedInOrder() throws Exception {
        List<Object> result = new ArrayList<Object>();
        CollectionDeserializer.CollectionReferringAccumulator accumulator =
                new CollectionDeserializer.CollectionReferringAccumulator(String.class, result);

        accumulator.add("item0");

        JsonParser parser = _mapper.getFactory().createParser("[]");
        parser.nextToken();

        UnresolvedForwardReference ref1 = new UnresolvedForwardReference(parser, "Unresolved 1");
        ReadableObjectId roid1 = new ReadableObjectId(new ObjectIdGenerators.IntSequenceGenerator().key(1));
        roid1.appendReferring(accumulator.handleUnresolvedReference(ref1));

        accumulator.add("item2");

        UnresolvedForwardReference ref2 = new UnresolvedForwardReference(parser, "Unresolved 2");
        ReadableObjectId roid2 = new ReadableObjectId(new ObjectIdGenerators.IntSequenceGenerator().key(2));
        roid2.appendReferring(accumulator.handleUnresolvedReference(ref2));

        accumulator.add("item4");

        // Resolve ref2 first then ref1
        accumulator.resolveForwardReference(roid2.getKey().key, "resolved2");
        accumulator.resolveForwardReference(roid1.getKey().key, "resolved1");

        assertEquals(5, result.size());
        assertEquals("item0", result.get(0));
        assertEquals("resolved1", result.get(1));
        assertEquals("item2", result.get(2));
        assertEquals("resolved2", result.get(3));
        assertEquals("item4", result.get(4));
    }

    // Tests deserializeWithType method directly
    @Test
    public void testDeserializeWithType_arrayWithTypeInfo_returnsCollection() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, String.class);
        CollectionDeserializer deser = new CollectionDeserializer(type, null, null, null);

        String json = "[\"val1\", \"val2\"]";
        JsonParser parser = _mapper.getFactory().createParser(json);
        parser.nextToken();

        DeserializationContext ctxt = _mapper.getDeserializationContext();
        TypeDeserializer typeDeser = _mapper.getDeserializationConfig().findTypeDeserializer(type);

        if (typeDeser != null) {
            Object res = deser.deserializeWithType(parser, ctxt, typeDeser);
            assertNotNull(res);
            assertTrue(res instanceof Collection);
        }
    }
}