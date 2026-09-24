package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class MapDeserializerTest {

    static class CustomStringDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt)
                throws IOException {
            return p.getText() + "_custom";
        }
    }

    static class CustomKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
            return key + "_key";
        }
    }

    static class MapWrapperWithCustomDeser {
        @JsonDeserialize(contentUsing = CustomStringDeserializer.class)
        public Map<String, String> map;
    }

    static class MapWrapperWithCustomKeyDeser {
        @JsonDeserialize(keyUsing = CustomKeyDeserializer.class)
        public Map<String, String> map;
    }

    static class MapWrapperWithIgnored {
        @JsonIgnoreProperties({"ignoredKey"})
        public Map<String, String> map;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
    static class PolymorphicValue {
        public String name;
    }

    static class NonDefaultMap extends HashMap<String, String> {
        public NonDefaultMap(String dummy) {
            super();
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_OBJECT)
    static class PolymorphicMapWrapper {
        public Map<String, Object> data;
    }

    static class CreatorMap extends HashMap<String, Object> {
        private final String extra;

        @JsonCreator
        public CreatorMap(@JsonProperty("extra") String extra) {
            super();
            this.extra = extra;
        }

        public String getExtra() {
            return extra;
        }
    }

    static class ThrowingMap extends HashMap<String, Object> {
        @Override
        public Object put(String key, Object value) {
            if ("fail".equals(key)) {
                throw new IllegalStateException("Simulated put failure");
            }
            return super.put(key, value);
        }
    }

    // Tests isCachable returns false when valueTypeDeserializer is present
    @Test
    public void testIsCachable_withValueTypeDeserializer_returnsFalse() {
        JavaType type = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class);
        ValueInstantiator vi = new StdValueInstantiator(null, type);
        com.fasterxml.jackson.databind.jsontype.TypeDeserializer typeDeser =
                new com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeDeserializer(
                        type.getContentType(), null, "@class", false, null);

        MapDeserializer deser = new MapDeserializer(type, vi, null, null, typeDeser);
        assertFalse(deser.isCachable());
    }

    // Tests isCachable returns false when ignorable properties are set
    @Test
    public void testIsCachable_withIgnorableProperties_returnsFalse() {
        JavaType type = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class);
        ValueInstantiator vi = new StdValueInstantiator(null, type);
        MapDeserializer deser = new MapDeserializer(type, vi, null, null, null);
        deser.setIgnorableProperties(new String[]{"ignoreMe"});
        assertFalse(deser.isCachable());
    }

    // Tests isCachable when valueDeserializer or keyDeserializer is provided
    @Test
    public void testIsCachable_withCustomValueOrKeyDeser_returnsFalse() {
        JavaType type = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class);
        ValueInstantiator vi = new StdValueInstantiator(null, type);
        JsonDeserializer<Object> valDeser = new UntypedObjectDeserializer();
        KeyDeserializer keyDeser = new CustomKeyDeserializer();

        MapDeserializer deserVal = new MapDeserializer(type, vi, null, valDeser, null);
        assertFalse(deserVal.isCachable());

        MapDeserializer deserKey = new MapDeserializer(type, vi, keyDeser, null, null);
        assertFalse(deserKey.isCachable());
    }

    // Tests standard deserialization for simple String to String Map
    @Test
    public void testDeserialize_standardStringMap_success() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"a\":\"1\",\"b\":\"2\"}";

        Map<String, String> result = mapper.readValue(json, new TypeReference<Map<String, String>>() {});
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("1", result.get("a"));
        assertEquals("2", result.get("b"));
    }

    // Tests deserialization with custom value deserializer
    @Test
    public void testDeserialize_customContentDeserializer_appliedCorrectly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"map\":{\"k1\":\"v1\"}}";

        MapWrapperWithCustomDeser result = mapper.readValue(json, MapWrapperWithCustomDeser.class);
        assertNotNull(result.map);
        assertEquals("v1_custom", result.map.get("k1"));
    }

    // Tests deserialization with custom key deserializer
    @Test
    public void testDeserialize_customKeyDeserializer_appliedCorrectly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"map\":{\"k1\":\"v1\"}}";

        MapWrapperWithCustomKeyDeser result = mapper.readValue(json, MapWrapperWithCustomKeyDeser.class);
        assertNotNull(result.map);
        assertEquals("v1", result.map.get("k1_key"));
    }

    // Tests deserialization ignoring specified properties
    @Test
    public void testDeserialize_ignorableProperties_skipsIgnoredKey() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"map\":{\"ignoredKey\":\"skip\",\"keptKey\":\"keep\"}}";

        MapWrapperWithIgnored result = mapper.readValue(json, MapWrapperWithIgnored.class);
        assertNotNull(result.map);
        assertFalse(result.map.containsKey("ignoredKey"));
        assertEquals("keep", result.map.get("keptKey"));
    }

    // Tests updating an existing Map instance via updateValue
    @Test
    public void testDeserialize_updatingExistingMap_mergesValues() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> target = new LinkedHashMap<String, Object>();
        target.put("initial", "val0");

        String json = "{\"newKey\":\"val1\"}";
        Map<String, Object> result = mapper.readerForUpdating(target).readValue(json);

        assertSame(target, result);
        assertEquals(2, result.size());
        assertEquals("val0", result.get("initial"));
        assertEquals("val1", result.get("newKey"));
    }

    // Tests deserializing null value in Map
    @Test
    public void testDeserialize_nullValueInMap_storesNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"key\":null}";

        Map<String, Object> result = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        assertNotNull(result);
        assertTrue(result.containsKey("key"));
        assertNull(result.get("key"));
    }

    // Tests deserialization of non-String keys (e.g. Integer key)
    @Test
    public void testDeserialize_nonStringKey_parsedCorrectly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"123\":\"value\"}";

        Map<Integer, String> result = mapper.readValue(json, new TypeReference<Map<Integer, String>>() {});
        assertNotNull(result);
        assertEquals("value", result.get(123));
    }

    // Tests deserialization with polymorphic typed value
    @Test
    public void testDeserialize_polymorphicValue_typeDeserializerUsed() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"item\":{\"@class\":\"" + PolymorphicValue.class.getName() + "\",\"name\":\"poly\"}}";

        Map<String, PolymorphicValue> result = mapper.readValue(
                json, new TypeReference<Map<String, PolymorphicValue>>() {});
        assertNotNull(result);
        PolymorphicValue pv = result.get("item");
        assertNotNull(pv);
        assertEquals("poly", pv.name);
    }

    // Tests deserialization failing on invalid JSON token (e.g. numeric array instead of object)
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_invalidJsonToken_throwsMappingException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "[1, 2, 3]";
        mapper.readValue(json, new TypeReference<Map<String, String>>() {});
    }

    // Tests deserialization with missing default constructor
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_noDefaultConstructor_throwsInstantiationException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"a\":\"b\"}";
        mapper.readValue(json, NonDefaultMap.class);
    }

    // Tests getContentType and getContentDeserializer accessors
    @Test
    public void testAccessors_getContentTypeAndDeserializer() {
        JavaType type = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Integer.class);
        ValueInstantiator vi = new StdValueInstantiator(null, type);
        JsonDeserializer<Object> valDeser = new UntypedObjectDeserializer();

        MapDeserializer deser = new MapDeserializer(type, vi, null, valDeser, null);
        assertEquals(Integer.class, deser.getContentType().getRawClass());
        assertSame(valDeser, deser.getContentDeserializer());
        assertEquals(HashMap.class, deser.getMapClass());
        assertEquals(type, deser.getValueType());
        assertSame(vi, deser.getValueInstantiator());
    }

    // Tests withResolved fluent factory method
    @Test
    public void testWithResolved_returnsNewOrSameInstance() {
        JavaType type = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class);
        ValueInstantiator vi = new StdValueInstantiator(null, type);
        MapDeserializer deser = new MapDeserializer(type, vi, null, null, null);

        MapDeserializer same = deser.withResolved(null, null, null, null);
        assertSame(deser, same);

        HashSet<String> ignorable = new HashSet<String>(Arrays.asList("x"));
        MapDeserializer different = deser.withResolved(null, null, null, ignorable);
        assertNotSame(deser, different);
    }

    // Tests deserialization of empty map {}
    @Test
    public void testDeserialize_emptyMap_returnsEmptyMap() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> result = mapper.readValue("{}", new TypeReference<Map<String, String>>() {});
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Tests deserialization with empty string when ACCEPT_EMPTY_STRING_AS_NULL_OBJECT is enabled
    @Test
    public void testDeserialize_emptyString_asNullObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        Map<String, String> result = mapper.readValue("\"\"", new TypeReference<Map<String, String>>() {});
        assertNull(result);
    }

    // Tests deserialization with empty string when ACCEPT_EMPTY_STRING_AS_NULL_OBJECT is disabled
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_emptyString_throwsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mapper.readValue("\"\"", new TypeReference<Map<String, String>>() {});
    }

    // Tests deserialization when parser is positioned directly on FIELD_NAME (not START_OBJECT)
    @Test
    public void testDeserialize_startingAtFieldNameToken() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"k1\":\"v1\",\"k2\":\"v2\"}";
        JsonParser p = mapper.getFactory().createParser(json);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());

        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType mapType = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class);
        JsonDeserializer<Object> deser = mapper.getDeserializationConfig().findRootValueDeserializer(mapType);

        @SuppressWarnings("unchecked")
        Map<String, String> result = (Map<String, String>) deser.deserialize(p, ctxt);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("v1", result.get("k1"));
        assertEquals("v2", result.get("k2"));
        p.close();
    }

    // Tests deserialization with creator-based instantiation
    @Test
    public void testDeserialize_creatorBasedMap_instantiatedProperly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"extra\":\"hello\",\"k1\":\"v1\"}";

        CreatorMap result = mapper.readValue(json, CreatorMap.class);
        assertNotNull(result);
        assertEquals("hello", result.getExtra());
        assertEquals("v1", result.get("k1"));
    }

    // Tests wrapAndThrow handling when Map.put() throws an exception
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_throwingMap_wrapsAndRethrows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"ok\":\"val\",\"fail\":\"boom\"}";
        mapper.readValue(json, ThrowingMap.class);
    }

    // Tests polymorphic deserialization with type wrapper
    @Test
    public void testDeserialize_polymorphicMapWrapper_deserializesWithType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"data\":{\"key\":\"value\"}}";

        PolymorphicMapWrapper result = mapper.readValue(json, PolymorphicMapWrapper.class);
        assertNotNull(result);
        assertNotNull(result.data);
        assertEquals("value", result.data.get("key"));
    }

    // Tests setIgnorableProperties with Set directly
    @Test
    public void testSetIgnorableProperties_withSet() {
        JavaType type = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class);
        ValueInstantiator vi = new StdValueInstantiator(null, type);
        MapDeserializer deser = new MapDeserializer(type, vi, null, null, null);

        HashSet<String> set = new HashSet<String>(Arrays.asList("a", "b"));
        deser.setIgnorableProperties(set);
        assertFalse(deser.isCachable());
    }

    // Tests deserialize into existing Map when input is empty
    @Test
    public void testDeserialize_updatingExistingMap_withEmptyJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> target = new LinkedHashMap<String, Object>();
        target.put("existing", "123");

        Map<String, Object> result = mapper.readerForUpdating(target).readValue("{}");
        assertSame(target, result);
        assertEquals(1, result.size());
        assertEquals("123", result.get("existing"));
    }
}