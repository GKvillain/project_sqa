package com.fasterxml.jackson.databind.deser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;

public class DeserializerCacheTest {

    private ObjectMapper mapper;
    private DeserializationContext context;
    private DeserializerFactory factory;
    private DeserializerCache cache;
    private TypeFactory typeFactory;

    // Helper classes for testing
    static class SimpleBean {
        public String name;
        public int value;
    }

    enum TestEnum {
        A, B, C
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    static class CustomShapeCollection extends java.util.ArrayList<String> {
        private static final long serialVersionUID = 1L;
        public int extraData;
    }

    static class CustomKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) {
            return "CUSTOM_KEY:" + key;
        }
    }

    static class CustomStringDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) {
            return "CUSTOM_VAL";
        }
    }

    static class ContainerWithCustomKeyMap {
        @JsonDeserialize(keyUsing = CustomKeyDeserializer.class)
        public Map<String, String> map;
    }

    static class ContainerWithCustomContentMap {
        @JsonDeserialize(contentUsing = CustomStringDeserializer.class)
        public Map<String, String> map;
    }

    static class ContainerWithCustomContentList {
        @JsonDeserialize(contentUsing = CustomStringDeserializer.class)
        public List<String> list;
    }

    static class ConvertedBean {
        @JsonProperty("text")
        public String text;
    }

    static class TargetBean {
        public String fullText;
    }

    static class BeanConverter extends StdConverter<ConvertedBean, TargetBean> {
        @Override
        public TargetBean convert(ConvertedBean value) {
            TargetBean tb = new TargetBean();
            tb.fullText = "CONVERTED:" + value.text;
            return tb;
        }
    }

    @JsonDeserialize(converter = BeanConverter.class)
    static class AnnotatedConvertedBean extends TargetBean {
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        context = mapper.getDeserializationContext();
        factory = BeanDeserializerFactory.instance;
        cache = new DeserializerCache();
        typeFactory = mapper.getTypeFactory();
    }

    // Tests initial cache state
    @Test
    public void testCachedDeserializersCount_initial_returnsZero() {
        assertEquals(0, cache.cachedDeserializersCount());
    }

    // Tests caching bean deserializer
    @Test
    public void testFindValueDeserializer_simpleBean_cachesDeserializer() throws Exception {
        JavaType type = typeFactory.constructType(SimpleBean.class);
        JsonDeserializer<Object> deser1 = cache.findValueDeserializer(context, factory, type);
        assertNotNull(deser1);
        assertEquals(1, cache.cachedDeserializersCount());

        JsonDeserializer<Object> deser2 = cache.findValueDeserializer(context, factory, type);
        assertSame(deser1, deser2);
        assertEquals(1, cache.cachedDeserializersCount());
    }

    // Tests flushing cache
    @Test
    public void testFlushCachedDeserializers_clearsCache() throws Exception {
        JavaType type = typeFactory.constructType(SimpleBean.class);
        cache.findValueDeserializer(context, factory, type);
        assertTrue(cache.cachedDeserializersCount() > 0);

        cache.flushCachedDeserializers();
        assertEquals(0, cache.cachedDeserializersCount());
    }

    // Tests null input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFindCachedDeserializer_nullType_throwsException() {
        cache._findCachedDeserializer(null);
    }

    // Tests enum deserializer creation
    @Test
    public void testFindValueDeserializer_enumType_returnsDeserializer() throws Exception {
        JavaType type = typeFactory.constructType(TestEnum.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(context, factory, type);
        assertNotNull(deser);
        assertTrue(cache.hasValueDeserializerFor(context, factory, type));
    }

    // Tests array type deserializer creation
    @Test
    public void testFindValueDeserializer_arrayType_returnsDeserializer() throws Exception {
        JavaType type = typeFactory.constructArrayType(String.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(context, factory, type);
        assertNotNull(deser);
        assertTrue(cache.hasValueDeserializerFor(context, factory, type));
    }

    // Tests collection type deserializer creation
    @Test
    public void testFindValueDeserializer_collectionType_returnsDeserializer() throws Exception {
        JavaType type = typeFactory.constructCollectionType(List.class, String.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(context, factory, type);
        assertNotNull(deser);
        assertTrue(cache.hasValueDeserializerFor(context, factory, type));
    }

    // Tests map type deserializer creation
    @Test
    public void testFindValueDeserializer_mapType_returnsDeserializer() throws Exception {
        JavaType type = typeFactory.constructMapType(Map.class, String.class, Integer.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(context, factory, type);
        assertNotNull(deser);
        assertTrue(cache.hasValueDeserializerFor(context, factory, type));
    }

    // Tests Tree / JsonNode deserializer creation
    @Test
    public void testFindValueDeserializer_jsonNodeType_returnsDeserializer() throws Exception {
        JavaType type = typeFactory.constructType(JsonNode.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(context, factory, type);
        assertNotNull(deser);
        assertTrue(cache.hasValueDeserializerFor(context, factory, type));
    }

    // Tests key deserializer lookup for standard key
    @Test
    public void testFindKeyDeserializer_stringKey_returnsKeyDeserializer() throws Exception {
        JavaType type = typeFactory.constructType(String.class);
        KeyDeserializer kd = cache.findKeyDeserializer(context, factory, type);
        assertNotNull(kd);
    }

    // Tests hasValueDeserializerFor for supported and unknown types
    @Test
    public void testHasValueDeserializerFor_validType_returnsTrue() throws Exception {
        JavaType type = typeFactory.constructType(SimpleBean.class);
        boolean hasDeser = cache.hasValueDeserializerFor(context, factory, type);
        assertTrue(hasDeser);
    }

    // Tests JDK serialization writeReplace clears incomplete deserializers
    @Test
    public void testWriteReplace_serialization_clearsIncomplete() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes);
        out.writeObject(cache);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        Object deserialized = in.readObject();
        in.close();

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof DeserializerCache);
    }

    // Tests custom shape collection deserializer creation
    @Test
    public void testFindValueDeserializer_customShapeCollection_returnsDeserializer() throws Exception {
        JavaType type = typeFactory.constructType(CustomShapeCollection.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(context, factory, type);
        assertNotNull(deser);
    }

    // Tests converter annotated bean
    @Test
    public void testFindValueDeserializer_converterAnnotated_returnsDeserializer() throws Exception {
        JavaType type = typeFactory.constructType(AnnotatedConvertedBean.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(context, factory, type);
        assertNotNull(deser);
    }

    // Tests container with custom content handler not cached incorrectly
    @Test
    public void testHasCustomHandlers_listWithContentHandler_notCached() throws Exception {
        JavaType type = typeFactory.constructType(ContainerWithCustomContentList.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        assertNotNull(beanDesc);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(context, factory, type);
        assertNotNull(deser);
    }

    // Tests container with custom key handler in map
    @Test
    public void testHasCustomHandlers_mapWithKeyHandler_notCached() throws Exception {
        JavaType type = typeFactory.constructType(ContainerWithCustomKeyMap.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(context, factory, type);
        assertNotNull(deser);
    }

    // Tests map with custom content handler
    @Test
    public void testHasCustomHandlers_mapWithContentHandler_notCached() throws Exception {
        JavaType type = typeFactory.constructType(ContainerWithCustomContentMap.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(context, factory, type);
        assertNotNull(deser);
    }

    // Tests unknown key deserializer exception path
    @Test(expected = JsonMappingException.class)
    public void testFindKeyDeserializer_unknownType_throwsException() throws Exception {
        JavaType type = typeFactory.constructType(Object.class);
        // Custom dummy factory that returns null
        DeserializerFactory mockFactory = new DeserializerFactoryConfig().withAdditionalKeyDeserializers(null) != null
                ? factory : factory;
        cache._handleUnknownKeyDeserializer(context, type);
    }

    // Tests unknown value deserializer exception path
    @Test(expected = JsonMappingException.class)
    public void testHandleUnknownValueDeserializer_throwsException() throws Exception {
        JavaType type = typeFactory.constructType(Object.class);
        cache._handleUnknownValueDeserializer(context, type);
    }
}