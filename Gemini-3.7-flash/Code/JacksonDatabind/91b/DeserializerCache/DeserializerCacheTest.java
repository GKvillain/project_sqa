package com.fasterxml.jackson.databind.deser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.StdConverter;

public class DeserializerCacheTest {

    private ObjectMapper _mapper;
    private DeserializationContext _context;
    private DeserializerFactory _factory;
    private DeserializerCache _cache;
    private TypeFactory _typeFactory;

    static enum TestEnum { A, B, C }

    static class SimpleBean {
        public int x;
        public String y;
    }

    static class CustomKey {
        public String id;
        public CustomKey(String id) { this.id = id; }
    }

    static class CustomKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) {
            return new CustomKey(key);
        }
    }

    static class CustomStringDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) {
            return "custom";
        }
    }

    static class MapWithCustomKeyWrapper {
        @JsonDeserialize(keyUsing = CustomKeyDeserializer.class)
        public Map<CustomKey, String> map;
    }

    static class MapWithCustomContentWrapper {
        @JsonDeserialize(contentUsing = CustomStringDeserializer.class)
        public Map<String, String> map;
    }

    static class ListWithCustomContentWrapper {
        @JsonDeserialize(contentUsing = CustomStringDeserializer.class)
        public List<String> list;
    }

    static class ConvertedBean {
        public int value;

        @JsonCreator
        public ConvertedBean(@JsonProperty("value") int v) {
            this.value = v;
        }
    }

    static class StringToConvertedBeanConverter extends StdConverter<String, ConvertedBean> {
        @Override
        public ConvertedBean convert(String value) {
            return new ConvertedBean(Integer.parseInt(value));
        }
    }

    @JsonDeserialize(converter = StringToConvertedBeanConverter.class)
    static class BeanWithConverterTarget {
        public ConvertedBean target;
    }

    static class RecursiveBean {
        public RecursiveBean next;
        public int value;
    }

    static class CustomNonCachableDeserializer extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) {
            return null;
        }

        @Override
        public boolean isCachable() {
            return false;
        }
    }

    @JsonDeserialize(using = CustomNonCachableDeserializer.class)
    static class NonCachableAnnotatedBean {
        public int x;
    }

    @JsonDeserialize(keyUsing = CustomKeyDeserializer.class)
    static class AnnotatedKeyBean {
        public String id;
    }

    static class ListWithContentConverterWrapper {
        @JsonDeserialize(contentConverter = StringToConvertedBeanConverter.class)
        public List<ConvertedBean> items;
    }

    static class BeanWithAsAnnotation {
        @JsonDeserialize(as = ArrayList.class, contentAs = SimpleBean.class)
        public List<SimpleBean> items;
    }

    public interface NonDeserializableInterface {
        void doSomething();
    }

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _context = _mapper.getDeserializationContext();
        _factory = BeanDeserializerFactory.instance;
        _cache = new DeserializerCache();
        _typeFactory = _mapper.getTypeFactory();
    }

    // Tests initial state and cache count
    @Test
    public void testCachedDeserializersCount_initial_returnsZero() {
        assertEquals(0, _cache.cachedDeserializersCount());
    }

    // Tests caching behavior and flushCachedDeserializers
    @Test
    public void testFlushCachedDeserializers_clearsCache() throws Exception {
        JavaType type = _typeFactory.constructType(SimpleBean.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
        assertTrue(_cache.cachedDeserializersCount() > 0);

        _cache.flushCachedDeserializers();
        assertEquals(0, _cache.cachedDeserializersCount());
    }

    // Tests finding deserializer for a simple POJO bean
    @Test
    public void testFindValueDeserializer_simpleBean_returnsDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(SimpleBean.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
        assertTrue(deser.isCachable());

        // Cache hit test
        JsonDeserializer<Object> deser2 = _cache.findValueDeserializer(_context, _factory, type);
        assertSame(deser, deser2);
    }

    // Tests finding deserializer for Enum types
    @Test
    public void testFindValueDeserializer_enumType_returnsDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(TestEnum.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
        assertTrue(deser.isCachable());
    }

    // Tests finding deserializer for Container types (List, Map, Array)
    @Test
    public void testFindValueDeserializer_containerTypes_returnsDeserializers() throws Exception {
        JavaType listType = _typeFactory.constructCollectionType(ArrayList.class, SimpleBean.class);
        JsonDeserializer<Object> listDeser = _cache.findValueDeserializer(_context, _factory, listType);
        assertNotNull(listDeser);

        JavaType mapType = _typeFactory.constructMapType(HashMap.class, String.class, SimpleBean.class);
        JsonDeserializer<Object> mapDeser = _cache.findValueDeserializer(_context, _factory, mapType);
        assertNotNull(mapDeser);

        JavaType arrayType = _typeFactory.constructArrayType(SimpleBean.class);
        JsonDeserializer<Object> arrayDeser = _cache.findValueDeserializer(_context, _factory, arrayType);
        assertNotNull(arrayDeser);
    }

    // Tests finding deserializer for JsonNode tree type
    @Test
    public void testFindValueDeserializer_jsonNode_returnsTreeDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(JsonNode.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests finding key deserializer for map keys
    @Test
    public void testFindKeyDeserializer_stringAndEnum_returnsKeyDeserializer() throws Exception {
        JavaType stringType = _typeFactory.constructType(String.class);
        KeyDeserializer kd1 = _cache.findKeyDeserializer(_context, _factory, stringType);
        assertNotNull(kd1);

        JavaType enumType = _typeFactory.constructType(TestEnum.class);
        KeyDeserializer kd2 = _cache.findKeyDeserializer(_context, _factory, enumType);
        assertNotNull(kd2);
    }

    // Tests hasValueDeserializerFor check
    @Test
    public void testHasValueDeserializerFor_supportedType_returnsTrue() throws Exception {
        JavaType type = _typeFactory.constructType(SimpleBean.class);
        assertTrue(_cache.hasValueDeserializerFor(_context, _factory, type));
    }

    // Tests null type passed to cache lookup throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFindCachedDeserializer_nullType_throwsException() {
        _cache._findCachedDeserializer(null);
    }

    // Tests JDK serialization writeReplace clears incomplete deserializers
    @Test
    public void testWriteReplace_clearsIncompleteDeserializersAndSerializes() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes);
        out.writeObject(_cache);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        Object deserialized = in.readObject();
        in.close();

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof DeserializerCache);
    }

    // Tests handling unknown key deserializer
    @Test(expected = JsonMappingException.class)
    public void testHandleUnknownKeyDeserializer_throwsJsonMappingException() throws Exception {
        JavaType invalidType = _typeFactory.constructType(Object.class);
        // Using a custom factory or direct invocation to verify unknown key handler
        _cache._handleUnknownKeyDeserializer(_context, invalidType);
    }

    // Tests handling unknown value deserializer
    @Test(expected = JsonMappingException.class)
    public void testHandleUnknownValueDeserializer_throwsJsonMappingException() throws Exception {
        JavaType abstractType = _typeFactory.constructType(CharSequence.class);
        _cache._handleUnknownValueDeserializer(_context, abstractType);
    }

    // Tests type with custom key deserializer handler (Defects4J Bug 91 regression check)
    @Test
    public void testCustomKeyHandlerOnMap_notCachedAsStandard() throws Exception {
        JavaType mapType = _typeFactory.constructMapType(Map.class, CustomKey.class, String.class);
        KeyDeserializer customKd = new CustomKeyDeserializer();
        JavaType mapWithCustomKey = ((MapType) mapType).withKeyValueHandler(customKd);

        // A map type with custom key handler should not be cached in the standard cache lookup
        JsonDeserializer<Object> cached = _cache._findCachedDeserializer(mapWithCustomKey);
        assertNull(cached);
    }

    // Tests type with custom content deserializer handler
    @Test
    public void testCustomContentHandlerOnList_notCachedAsStandard() throws Exception {
        JavaType listType = _typeFactory.constructCollectionType(List.class, String.class);
        JavaType listWithCustomContent = listType.withContentValueHandler(new CustomStringDeserializer());

        JsonDeserializer<Object> cached = _cache._findCachedDeserializer(listWithCustomContent);
        assertNull(cached);
    }

    // Tests deserializer construction with annotated converter
    @Test
    public void testFindValueDeserializer_withConverter_constructsDelegatingDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(BeanWithConverterTarget.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests modifyTypeByAnnotation with MapWithCustomKeyWrapper and MapWithCustomContentWrapper
    @Test
    public void testObjectMapper_withCustomMapAnnotations_deserializesCorrectly() throws Exception {
        String jsonKey = "{\"map\":{\"k1\":\"v1\"}}";
        MapWithCustomKeyWrapper resultKey = _mapper.readValue(jsonKey, MapWithCustomKeyWrapper.class);
        assertNotNull(resultKey);
        assertNotNull(resultKey.map);
        assertEquals(1, resultKey.map.size());

        String jsonContent = "{\"map\":{\"k1\":\"v1\"}}";
        MapWithCustomContentWrapper resultContent = _mapper.readValue(jsonContent, MapWithCustomContentWrapper.class);
        assertNotNull(resultContent);
        assertNotNull(resultContent.map);
        assertEquals("custom", resultContent.map.get("k1"));
    }

    // Tests recursive/cyclic POJO bean deserializer resolution in DeserializerCache
    @Test
    public void testFindValueDeserializer_recursiveBean_resolvesIncompleteDeserializers() throws Exception {
        JavaType type = _typeFactory.constructType(RecursiveBean.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
        assertTrue(deser.isCachable());
        assertSame(deser, _cache.findValueDeserializer(_context, _factory, type));
    }

    // Tests non-cachable custom deserializer is not stored in cached deserializers map
    @Test
    public void testFindValueDeserializer_nonCachableDeserializer_notCached() throws Exception {
        JavaType type = _typeFactory.constructType(NonCachableAnnotatedBean.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
        assertFalse(deser.isCachable());
        assertNull(_cache._findCachedDeserializer(type));
    }

    // Tests primitive types and Object.class root type deserializer lookups
    @Test
    public void testFindValueDeserializer_primitiveAndUntyped_returnsDeserializers() throws Exception {
        JavaType intType = _typeFactory.constructType(int.class);
        JsonDeserializer<Object> intDeser = _cache.findValueDeserializer(_context, _factory, intType);
        assertNotNull(intDeser);

        JavaType boolType = _typeFactory.constructType(boolean.class);
        JsonDeserializer<Object> boolDeser = _cache.findValueDeserializer(_context, _factory, boolType);
        assertNotNull(boolDeser);

        JavaType objType = _typeFactory.constructType(Object.class);
        JsonDeserializer<Object> objDeser = _cache.findValueDeserializer(_context, _factory, objType);
        assertNotNull(objDeser);
    }

    // Tests hasValueDeserializerFor with unsupported/interface type returning false
    @Test
    public void testHasValueDeserializerFor_nonDeserializableType_returnsFalse() {
        JavaType type = _typeFactory.constructType(NonDeserializableInterface.class);
        assertFalse(_cache.hasValueDeserializerFor(_context, _factory, type));
    }

    // Tests finding key deserializer configured via class-level annotation
    @Test
    public void testFindKeyDeserializer_withAnnotatedKeyClass() throws Exception {
        JavaType keyType = _typeFactory.constructType(AnnotatedKeyBean.class);
        KeyDeserializer kd = _cache.findKeyDeserializer(_context, _factory, keyType);
        assertNotNull(kd);
        assertTrue(kd instanceof CustomKeyDeserializer);
    }

    // Tests container deserializer with content converter annotation
    @Test
    public void testFindValueDeserializer_withContentConverter() throws Exception {
        String json = "{\"items\":[\"123\",\"456\"]}";
        ListWithContentConverterWrapper result = _mapper.readValue(json, ListWithContentConverterWrapper.class);
        assertNotNull(result);
        assertNotNull(result.items);
        assertEquals(2, result.items.size());
        assertEquals(123, result.items.get(0).value);
        assertEquals(456, result.items.get(1).value);
    }

    // Tests modifyTypeByAnnotation with as and contentAs annotations
    @Test
    public void testModifyTypeByAnnotation_withAsAndContentAs() throws Exception {
        String json = "{\"items\":[{\"x\":1,\"y\":\"test\"}]}";
        BeanWithAsAnnotation result = _mapper.readValue(json, BeanWithAsAnnotation.class);
        assertNotNull(result);
        assertTrue(result.items instanceof ArrayList);
        assertEquals(1, result.items.size());
        assertEquals(1, result.items.get(0).x);
        assertEquals("test", result.items.get(0).y);
    }
}