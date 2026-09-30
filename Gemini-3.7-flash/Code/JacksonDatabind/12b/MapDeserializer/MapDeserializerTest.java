package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MapDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    static class CustomStringDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.getText() + "_custom";
        }
    }

    static class CustomKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
            return "key_" + key;
        }
    }

    static class MapWrapperWithCustomKey {
        @JsonDeserialize(keyUsing = CustomKeyDeserializer.class)
        public Map<String, String> map;
    }

    static class MapWrapperWithCustomValue {
        @JsonDeserialize(contentUsing = CustomStringDeserializer.class)
        public Map<String, String> map;
    }

    static class MapWrapperWithIgnored {
        @JsonIgnoreProperties({ "ignore1", "ignore2" })
        public Map<String, String> map;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    static class PolymorphicValue {
        public String name;
        public PolymorphicValue() {}
        public PolymorphicValue(String name) { this.name = name; }
    }

    static class SubPolymorphicValue extends PolymorphicValue {
        public int count;
        public SubPolymorphicValue() {}
        public SubPolymorphicValue(String name, int count) {
            super(name);
            this.count = count;
        }
    }

    enum TestEnum {
        ALPHA, BETA
    }

    // Tests isCachable when valueDeserializer / keyDeserializer are standard and no ignorable properties
    @Test
    public void testIsCachable_standardMap_returnsTrue() {
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class);
        JsonDeserializer<?> deser = mapper.getDeserializationContext().findRootValueDeserializer(mapType);
        assertTrue(deser.isCachable());
    }

    // Tests isCachable when custom value/key deserializers or ignorable properties exist (Defects4J Bug 12b target)
    @Test
    public void testIsCachable_withCustomContentDeser_returnsFalse() throws Exception {
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class);
        ValueInstantiator vi = new StdValueInstantiator(mapper.getDeserializationConfig(), mapType);
        JsonDeserializer<Object> valDeser = new CustomStringDeserializer();
        MapDeserializer mapDeser = new MapDeserializer(mapType, vi, null, valDeser, null);

        // Under defect 12b, having custom valueDeser or keyDeser should make isCachable false
        // MapDeserializer.isCachable() check
        assertFalse(mapDeser.isCachable());
    }

    // Tests isCachable when valueTypeDeserializer is present
    @Test
    public void testIsCachable_withValueTypeDeserializer_returnsFalse() {
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class);
        ValueInstantiator vi = new StdValueInstantiator(mapper.getDeserializationConfig(), mapType);
        TypeDeserializer typeDeser = mapper.getDeserializationConfig().findTypeDeserializer(TypeFactory.defaultInstance().constructType(Object.class));
        MapDeserializer mapDeser = new MapDeserializer(mapType, vi, null, null, typeDeser);
        
        assertFalse(mapDeser.isCachable());
    }

    // Tests isCachable when ignorable properties are present
    @Test
    public void testIsCachable_withIgnorableProperties_returnsFalse() {
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class);
        ValueInstantiator vi = new StdValueInstantiator(mapper.getDeserializationConfig(), mapType);
        MapDeserializer mapDeser = new MapDeserializer(mapType, vi, null, null, null);
        mapDeser.setIgnorableProperties(new String[] { "ignoredKey" });
        
        assertFalse(mapDeser.isCachable());
    }

    // Tests standard String key and String value deserialization
    @Test
    public void testDeserialize_standardStringMap_success() throws Exception {
        String json = "{\"a\":\"1\",\"b\":\"2\"}";
        Map<String, String> result = mapper.readValue(json, new TypeReference<Map<String, String>>() {});
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("1", result.get("a"));
        assertEquals("2", result.get("b"));
    }

    // Tests non-standard key deserialization (e.g. Integer key)
    @Test
    public void testDeserialize_integerKeyMap_success() throws Exception {
        String json = "{\"1\":\"one\",\"2\":\"two\"}";
        Map<Integer, String> result = mapper.readValue(json, new TypeReference<Map<Integer, String>>() {});
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("one", result.get(1));
        assertEquals("two", result.get(2));
    }

    // Tests deserialization with custom KeyDeserializer
    @Test
    public void testDeserialize_customKeyDeserializer_success() throws Exception {
        String json = "{\"map\":{\"a\":\"valA\",\"b\":\"valB\"}}";
        MapWrapperWithCustomKey wrapper = mapper.readValue(json, MapWrapperWithCustomKey.class);
        
        assertNotNull(wrapper.map);
        assertEquals("valA", wrapper.map.get("key_a"));
        assertEquals("valB", wrapper.map.get("key_b"));
    }

    // Tests deserialization with custom content (value) deserializer
    @Test
    public void testDeserialize_customValueDeserializer_success() throws Exception {
        String json = "{\"map\":{\"a\":\"valA\",\"b\":\"valB\"}}";
        MapWrapperWithCustomValue wrapper = mapper.readValue(json, MapWrapperWithCustomValue.class);
        
        assertNotNull(wrapper.map);
        assertEquals("valA_custom", wrapper.map.get("a"));
        assertEquals("valB_custom", wrapper.map.get("b"));
    }

    // Tests deserialization of empty JSON Object
    @Test
    public void testDeserialize_emptyMap_returnsEmptyMap() throws Exception {
        String json = "{}";
        Map<String, Object> result = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Tests deserialization with null value in map
    @Test
    public void testDeserialize_nullValueInMap_containsNullEntry() throws Exception {
        String json = "{\"a\":null}";
        Map<String, String> result = mapper.readValue(json, new TypeReference<Map<String, String>>() {});
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("a"));
        assertNull(result.get("a"));
    }

    // Tests updating an existing map instance
    @Test
    public void testDeserialize_intoExistingMap_updatesCorrectly() throws Exception {
        String json = "{\"b\":\"2\",\"c\":\"3\"}";
        Map<String, String> existing = new HashMap<String, String>();
        existing.put("a", "1");
        
        Map<String, String> result = mapper.readerForUpdating(existing).readValue(json);
        
        assertSame(existing, result);
        assertEquals(3, result.size());
        assertEquals("1", result.get("a"));
        assertEquals("2", result.get("b"));
        assertEquals("3", result.get("c"));
    }

    // Tests deserialization from invalid token (array instead of object)
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_invalidTokenArray_throwsException() throws Exception {
        String json = "[1, 2, 3]";
        mapper.readValue(json, new TypeReference<Map<String, String>>() {});
    }

    // Tests getContentType and getContentDeserializer accessors
    @Test
    public void testAccessors_getContentTypeAndDeserializer_returnsConfiguredValues() {
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Integer.class);
        ValueInstantiator vi = new StdValueInstantiator(mapper.getDeserializationConfig(), mapType);
        MapDeserializer mapDeser = new MapDeserializer(mapType, vi, null, null, null);

        assertEquals(TypeFactory.defaultInstance().constructType(Integer.class), mapDeser.getContentType());
        assertNull(mapDeser.getContentDeserializer());
        assertEquals(HashMap.class, mapDeser.getMapClass());
        assertEquals(mapType, mapDeser.getValueType());
    }

    // Tests setIgnorableProperties with null and non-empty array
    @Test
    public void testSetIgnorableProperties_nullAndValues_handlesCorrectly() {
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class);
        ValueInstantiator vi = new StdValueInstantiator(mapper.getDeserializationConfig(), mapType);
        MapDeserializer mapDeser = new MapDeserializer(mapType, vi, null, null, null);

        mapDeser.setIgnorableProperties(new String[] { "ignoreMe" });
        assertFalse(mapDeser.isCachable());

        mapDeser.setIgnorableProperties(null);
        assertTrue(mapDeser.isCachable());

        mapDeser.setIgnorableProperties(new String[0]);
        assertTrue(mapDeser.isCachable());
    }

    // Tests withResolved fluent method preserving same instance when settings unchanged
    @Test
    public void testWithResolved_sameAttributes_returnsSameInstance() {
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class);
        ValueInstantiator vi = new StdValueInstantiator(mapper.getDeserializationConfig(), mapType);
        MapDeserializer mapDeser = new MapDeserializer(mapType, vi, null, null, null);

        MapDeserializer same = mapDeser.withResolved(null, null, null, null);
        assertSame(mapDeser, same);
    }

    // Tests withResolved returning a new instance when attributes are modified
    @Test
    public void testWithResolved_differentAttributes_returnsNewInstance() {
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class);
        ValueInstantiator vi = new StdValueInstantiator(mapper.getDeserializationConfig(), mapType);
        MapDeserializer mapDeser = new MapDeserializer(mapType, vi, null, null, null);

        HashSet<String> ignorable = new HashSet<String>(Arrays.asList("x"));
        MapDeserializer modified = mapDeser.withResolved(null, null, null, ignorable);

        assertNotSame(mapDeser, modified);
        assertFalse(modified.isCachable());
    }

    // Tests deserialization ignoring specified properties via @JsonIgnoreProperties
    @Test
    public void testDeserialize_withIgnoredProperties_skipsIgnoredEntries() throws Exception {
        String json = "{\"map\":{\"keep\":\"val1\",\"ignore1\":\"skip1\",\"ignore2\":\"skip2\"}}";
        MapWrapperWithIgnored wrapper = mapper.readValue(json, MapWrapperWithIgnored.class);

        assertNotNull(wrapper.map);
        assertEquals(1, wrapper.map.size());
        assertEquals("val1", wrapper.map.get("keep"));
        assertFalse(wrapper.map.containsKey("ignore1"));
        assertFalse(wrapper.map.containsKey("ignore2"));
    }

    // Tests EnumMap deserialization with Enum keys
    @Test
    public void testDeserialize_enumMap_success() throws Exception {
        String json = "{\"ALPHA\":\"valA\",\"BETA\":\"valB\"}";
        EnumMap<TestEnum, String> result = mapper.readValue(json, new TypeReference<EnumMap<TestEnum, String>>() {});

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("valA", result.get(TestEnum.ALPHA));
        assertEquals("valB", result.get(TestEnum.BETA));
    }

    // Tests deserialization of Map containing polymorphic values with TypeDeserializer
    @Test
    public void testDeserialize_polymorphicValues_success() throws Exception {
        String json = "{\"item\":{\"@class\":\"" + SubPolymorphicValue.class.getName() + "\",\"name\":\"polyTest\",\"count\":42}}";
        Map<String, PolymorphicValue> result = mapper.readValue(json, new TypeReference<Map<String, PolymorphicValue>>() {});

        assertNotNull(result);
        assertTrue(result.get("item") instanceof SubPolymorphicValue);
        SubPolymorphicValue sub = (SubPolymorphicValue) result.get("item");
        assertEquals("polyTest", sub.name);
        assertEquals(42, sub.count);
    }

    // Tests getValueInstantiator accessor
    @Test
    public void testGetValueInstantiator_returnsConfiguredInstantiator() {
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class);
        ValueInstantiator vi = new StdValueInstantiator(mapper.getDeserializationConfig(), mapType);
        MapDeserializer mapDeser = new MapDeserializer(mapType, vi, null, null, null);

        assertSame(vi, mapDeser.getValueInstantiator());
    }
}