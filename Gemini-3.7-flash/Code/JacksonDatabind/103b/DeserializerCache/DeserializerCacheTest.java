package com.fasterxml.jackson.databind.deser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.StdConverter;

public class DeserializerCacheTest {

    private DeserializerCache _cache;
    private ObjectMapper _mapper;
    private DeserializationContext _context;
    private DeserializerFactory _factory;
    private TypeFactory _typeFactory;

    // Helper classes for testing various deserializer creation paths
    enum TestEnum { A, B }

    static class SimpleBean {
        public int x;
        public String y;
    }

    static class RecursiveBean {
        public RecursiveBean next;
        public String value;
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    static class MapAsObject extends HashMap<String, String> {
        private static final long serialVersionUID = 1L;
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    static class CollectionAsObject extends ArrayList<String> {
        private static final long serialVersionUID = 1L;
    }

    @JsonDeserialize(builder = SimpleBuilder.class)
    static class ValueWithBuilder {
        final int value;
        ValueWithBuilder(int v) { this.value = v; }
    }

    @JsonPOJOBuilder(withPrefix = "with")
    static class SimpleBuilder {
        private int value;
        public SimpleBuilder withValue(int v) { this.value = v; return this; }
        public ValueWithBuilder build() { return new ValueWithBuilder(value); }
    }

    static class CustomConverter extends StdConverter<String, SimpleBean> {
        @Override
        public SimpleBean convert(String value) {
            SimpleBean bean = new SimpleBean();
            bean.y = value;
            return bean;
        }
    }

    @JsonDeserialize(converter = CustomConverter.class)
    static class ConvertedBean {
        public int id;
    }

    @JsonDeserialize(using = CustomClassDeser.class)
    static class ClassWithCustomDeser { }

    static class CustomClassDeser extends JsonDeserializer<ClassWithCustomDeser> {
        @Override
        public ClassWithCustomDeser deserialize(JsonParser p, DeserializationContext ctxt) {
            return new ClassWithCustomDeser();
        }
    }

    @JsonDeserialize(keyUsing = CustomKeyDeser.class)
    static class ClassWithCustomKeyDeser { }

    static class CustomKeyDeser extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) {
            return new ClassWithCustomKeyDeser();
        }
    }

    interface UnregisteredInterface { }

    @Before
    public void setUp() {
        _cache = new DeserializerCache();
        _mapper = new ObjectMapper();
        _context = _mapper.getDeserializationContext();
        _factory = _mapper.getDeserializationContext().getFactory();
        _typeFactory = _mapper.getTypeFactory();
    }

    // Tests initial empty cache state and flushing
    @Test
    public void testFlushCachedDeserializers_initiallyZero_flushesCorrectly() {
        assertEquals(0, _cache.cachedDeserializersCount());
        _cache.flushCachedDeserializers();
        assertEquals(0, _cache.cachedDeserializersCount());
    }

    // Tests finding deserializer for simple POJO and caching behavior
    @Test
    public void testFindValueDeserializer_simpleBean_cachesDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(SimpleBean.class);
        JsonDeserializer<Object> deser1 = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser1);
        assertEquals(1, _cache.cachedDeserializersCount());

        JsonDeserializer<Object> deser2 = _cache.findValueDeserializer(_context, _factory, type);
        assertSame(deser1, deser2);
        assertEquals(1, _cache.cachedDeserializersCount());

        _cache.flushCachedDeserializers();
        assertEquals(0, _cache.cachedDeserializersCount());
    }

    // Tests hasValueDeserializerFor returns true for valid POJO
    @Test
    public void testHasValueDeserializerFor_validType_returnsTrue() throws Exception {
        JavaType type = _typeFactory.constructType(SimpleBean.class);
        assertTrue(_cache.hasValueDeserializerFor(_context, _factory, type));
    }

    // Tests null type passed to cache lookup throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFindCachedDeserializer_nullType_throwsException() {
        _cache._findCachedDeserializer(null);
    }

    // Tests Enum deserializer creation branch
    @Test
    public void testFindValueDeserializer_enumType_findsEnumDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(TestEnum.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests Array deserializer creation branch
    @Test
    public void testFindValueDeserializer_arrayType_findsArrayDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(SimpleBean[].class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests Map deserializer creation branch
    @Test
    public void testFindValueDeserializer_mapType_findsMapDeserializer() throws Exception {
        JavaType type = _typeFactory.constructMapType(Map.class, String.class, SimpleBean.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests Collection deserializer creation branch
    @Test
    public void testFindValueDeserializer_collectionType_findsCollectionDeserializer() throws Exception {
        JavaType type = _typeFactory.constructCollectionType(List.class, SimpleBean.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests Map-as-POJO shape override branch
    @Test
    public void testFindValueDeserializer_mapAsObject_createsBeanDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(MapAsObject.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests Collection-as-POJO shape override branch
    @Test
    public void testFindValueDeserializer_collectionAsObject_createsBeanDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(CollectionAsObject.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests ReferenceType (e.g., AtomicReference) creation branch
    @Test
    public void testFindValueDeserializer_referenceType_findsReferenceDeserializer() throws Exception {
        JavaType type = _typeFactory.constructReferenceType(AtomicReference.class, _typeFactory.constructType(String.class));
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests JsonNode / Tree deserializer creation branch
    @Test
    public void testFindValueDeserializer_jsonNodeType_findsTreeDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(JsonNode.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests Builder-based deserializer creation branch
    @Test
    public void testFindValueDeserializer_builderAnnotated_createsBuilderDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(ValueWithBuilder.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests Converter-based deserializer creation branch
    @Test
    public void testFindValueDeserializer_converterAnnotated_createsDelegatingDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(ConvertedBean.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests findKeyDeserializer for standard key type
    @Test
    public void testFindKeyDeserializer_stringKey_findsKeyDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(String.class);
        KeyDeserializer kd = _cache.findKeyDeserializer(_context, _factory, type);
        assertNotNull(kd);
    }

    // Tests unknown abstract type throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testFindValueDeserializer_unknownAbstractType_throwsJsonMappingException() throws Exception {
        JavaType type = _typeFactory.constructType(UnregisteredInterface.class);
        _cache.findValueDeserializer(_context, _factory, type);
    }

    // Tests JDK serialization writeReplace clears incomplete deserializers
    @Test
    public void testWriteReplace_serializesAndDeserializesSuccessfully() throws Exception {
        JavaType type = _typeFactory.constructType(SimpleBean.class);
        _cache.findValueDeserializer(_context, _factory, type);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(_cache);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        DeserializerCache deserialized = (DeserializerCache) ois.readObject();
        assertNotNull(deserialized);
    }

    // Additional Tests

    @Test
    public void testFindValueDeserializer_recursiveType_resolvesIncompleteDeserializers() throws Exception {
        JavaType type = _typeFactory.constructType(RecursiveBean.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    @Test
    public void testHasValueDeserializerFor_unregisteredInterface_returnsFalse() {
        JavaType type = _typeFactory.constructType(UnregisteredInterface.class);
        assertFalse(_cache.hasValueDeserializerFor(_context, _factory, type));
    }

    @Test
    public void testFindValueDeserializer_annotatedCustomDeser_findsCustomDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(ClassWithCustomDeser.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
        assertTrue(deser instanceof CustomClassDeser);
    }

    @Test
    public void testFindKeyDeserializer_annotatedCustomKeyDeser_findsCustomKeyDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(ClassWithCustomKeyDeser.class);
        KeyDeserializer kd = _cache.findKeyDeserializer(_context, _factory, type);
        assertNotNull(kd);
        assertTrue(kd instanceof CustomKeyDeser);
    }

    @Test
    public void testFindKeyDeserializer_enumKey_findsEnumKeyDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(TestEnum.class);
        KeyDeserializer kd = _cache.findKeyDeserializer(_context, _factory, type);
        assertNotNull(kd);
    }

    @Test(expected = JsonMappingException.class)
    public void testFindKeyDeserializer_unknownAbstractType_throwsJsonMappingException() throws Exception {
        JavaType type = _typeFactory.constructType(UnregisteredInterface.class);
        _cache.findKeyDeserializer(_context, _factory, type);
    }

    @Test
    public void testFindValueDeserializer_mapLikeType_findsDeserializer() throws Exception {
        MapLikeType type = _typeFactory.constructMapLikeType(Map.class, String.class, String.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    @Test
    public void testFindValueDeserializer_collectionLikeType_findsDeserializer() throws Exception {
        CollectionLikeType type = _typeFactory.constructCollectionLikeType(List.class, String.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }
}