package com.fasterxml.jackson.databind.deser;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.StdConverter;

public class DeserializerCacheTest {

    private DeserializerCache _cache;
    private ObjectMapper _mapper;
    private DeserializationContext _context;
    private DeserializerFactory _factory;
    private TypeFactory _typeFactory;

    static enum TestEnum { A, B, C }

    static class SimpleBean {
        public int id;
        public String name;
    }

    static abstract class AbstractType {
        public int x;
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    static class MapAsObject extends HashMap<String, String> {
        public String extra;
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    static class ListAsObject extends ArrayList<String> {
        public String extra;
    }

    static class CustomStringDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) {
            return "custom";
        }
    }

    static class CustomKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) {
            return "key:" + key;
        }
    }

    static class BeanWithCustomContent {
        @JsonDeserialize(contentUsing = CustomStringDeserializer.class)
        public List<String> items;
    }

    static class BeanWithCustomKeyDeser {
        @JsonDeserialize(keyUsing = CustomKeyDeserializer.class)
        public Map<String, String> map;
    }

    static class StringToIntConverter extends StdConverter<String, Integer> {
        @Override
        public Integer convert(String value) {
            return Integer.parseInt(value);
        }
    }

    static class BeanWithConverter {
        @JsonDeserialize(converter = StringToIntConverter.class)
        public Integer count;
    }

    static class RecursiveBean {
        public RecursiveBean next;
    }

    @JsonDeserialize(using = CustomClassDeserializer.class)
    static class ClassWithCustomDeser {
        public int value;
    }

    static class CustomClassDeserializer extends JsonDeserializer<ClassWithCustomDeser> {
        @Override
        public ClassWithCustomDeser deserialize(JsonParser p, DeserializationContext ctxt) {
            ClassWithCustomDeser obj = new ClassWithCustomDeser();
            obj.value = 42;
            return obj;
        }
    }

    @JsonDeserialize(as = ConcreteForAbstract.class)
    static abstract class AbstractWithAsAnnotation {
        public int x;
    }

    static class ConcreteForAbstract extends AbstractWithAsAnnotation {
        public int y;
    }

    @JsonDeserialize(keyUsing = CustomKeyDeserializer.class)
    static class ClassWithKeyDeserializerAnnotation {
    }

    @Before
    public void setUp() {
        _cache = new DeserializerCache();
        _mapper = new ObjectMapper();
        _context = _mapper.getDeserializationContext();
        _factory = new BeanDeserializerFactory(new com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig());
        _typeFactory = _mapper.getTypeFactory();
    }

    // Tests initial state and cache count
    @Test
    public void testCachedDeserializersCount_initialState_returnsZero() {
        assertEquals(0, _cache.cachedDeserializersCount());
    }

    // Tests flushCachedDeserializers clearing cache
    @Test
    public void testFlushCachedDeserializers_afterCaching_resetsCountToZero() throws Exception {
        JavaType type = _typeFactory.constructType(SimpleBean.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
        assertTrue(_cache.cachedDeserializersCount() > 0);

        _cache.flushCachedDeserializers();
        assertEquals(0, _cache.cachedDeserializersCount());
    }

    // Tests findValueDeserializer for basic POJO and verify caching
    @Test
    public void testFindValueDeserializer_simpleBean_returnsDeserializerAndCaches() throws Exception {
        JavaType type = _typeFactory.constructType(SimpleBean.class);
        JsonDeserializer<Object> deser1 = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser1);

        JsonDeserializer<Object> deser2 = _cache.findValueDeserializer(_context, _factory, type);
        assertSame(deser1, deser2);
        assertTrue(_cache.cachedDeserializersCount() >= 1);
    }

    // Tests findValueDeserializer for Enum type
    @Test
    public void testFindValueDeserializer_enumType_returnsEnumDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(TestEnum.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
        assertTrue(deser.isCachable());
    }

    // Tests findValueDeserializer for Array type
    @Test
    public void testFindValueDeserializer_arrayType_returnsArrayDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(String[].class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests findValueDeserializer for Collection type
    @Test
    public void testFindValueDeserializer_collectionType_returnsCollectionDeserializer() throws Exception {
        JavaType type = _typeFactory.constructCollectionType(List.class, String.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests findValueDeserializer for Map type
    @Test
    public void testFindValueDeserializer_mapType_returnsMapDeserializer() throws Exception {
        JavaType type = _typeFactory.constructMapType(Map.class, String.class, Integer.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests findValueDeserializer for ReferenceType (e.g. AtomicReference)
    @Test
    public void testFindValueDeserializer_referenceType_returnsReferenceDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(AtomicReference.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests findValueDeserializer for Tree / JsonNode type
    @Test
    public void testFindValueDeserializer_treeType_returnsTreeDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(JsonNode.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests hasValueDeserializerFor returning true for constructible type
    @Test
    public void testHasValueDeserializerFor_validType_returnsTrue() throws Exception {
        JavaType type = _typeFactory.constructType(SimpleBean.class);
        assertTrue(_cache.hasValueDeserializerFor(_context, _factory, type));
    }

    // Tests findKeyDeserializer for standard key type (String)
    @Test
    public void testFindKeyDeserializer_stringType_returnsKeyDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(String.class);
        KeyDeserializer kd = _cache.findKeyDeserializer(_context, _factory, type);
        assertNotNull(kd);
    }

    // Tests _findCachedDeserializer with null argument throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFindCachedDeserializer_nullType_throwsIllegalArgumentException() {
        _cache._findCachedDeserializer(null);
    }

    // Tests writeReplace behavior for serialization support
    @Test
    public void testWriteReplace_onCache_returnsSameInstance() {
        Object replaced = _cache.writeReplace();
        assertSame(_cache, replaced);
    }

    // Tests handling of Map with Shape.OBJECT annotation
    @Test
    public void testFindValueDeserializer_mapAsObject_handledAsBean() throws Exception {
        JavaType type = _typeFactory.constructType(MapAsObject.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests handling of List with Shape.OBJECT annotation
    @Test
    public void testFindValueDeserializer_listAsObject_handledAsBean() throws Exception {
        JavaType type = _typeFactory.constructType(ListAsObject.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests findValueDeserializer on abstract type without mappings throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testFindValueDeserializer_abstractType_throwsJsonMappingException() throws Exception {
        JavaType type = _typeFactory.constructType(AbstractType.class);
        _cache.findValueDeserializer(_context, _factory, type);
    }

    // Tests type with custom value handler is not cached
    @Test
    public void testFindCachedDeserializer_typeWithCustomHandler_returnsNull() {
        JavaType contentType = _typeFactory.constructType(String.class).withValueHandler(new CustomStringDeserializer());
        JavaType listType = _typeFactory.constructCollectionType(List.class, contentType);
        assertNull(_cache._findCachedDeserializer(listType));
    }

    // Tests findValueDeserializer on Bean with custom content deserializer annotation
    @Test
    public void testFindValueDeserializer_beanWithCustomContent_returnsValidDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(BeanWithCustomContent.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests findValueDeserializer on Bean with custom key deserializer annotation
    @Test
    public void testFindValueDeserializer_beanWithCustomKeyDeser_returnsValidDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(BeanWithCustomKeyDeser.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests findValueDeserializer on Bean with Converter
    @Test
    public void testFindValueDeserializer_beanWithConverter_returnsValidDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(BeanWithConverter.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests hasValueDeserializerFor returning false for unresolvable abstract type
    @Test
    public void testHasValueDeserializerFor_unresolvableAbstractType_returnsFalse() throws Exception {
        JavaType type = _typeFactory.constructType(AbstractType.class);
        assertFalse(_cache.hasValueDeserializerFor(_context, _factory, type));
    }

    // Tests findValueDeserializer for self-referential / recursive bean
    @Test
    public void testFindValueDeserializer_recursiveBean_returnsDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(RecursiveBean.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests findValueDeserializer on class annotated with @JsonDeserialize(using = ...)
    @Test
    public void testFindValueDeserializer_classWithCustomDeserAnnotation_returnsCustomDeser() throws Exception {
        JavaType type = _typeFactory.constructType(ClassWithCustomDeser.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
        assertTrue(deser instanceof CustomClassDeserializer);
    }

    // Tests findValueDeserializer on abstract class annotated with @JsonDeserialize(as = ...)
    @Test
    public void testFindValueDeserializer_abstractWithAsAnnotation_returnsConcreteDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(AbstractWithAsAnnotation.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests findKeyDeserializer on class annotated with @JsonDeserialize(keyUsing = ...)
    @Test
    public void testFindKeyDeserializer_classWithKeyDeserializerAnnotation_returnsCustomKeyDeser() throws Exception {
        JavaType type = _typeFactory.constructType(ClassWithKeyDeserializerAnnotation.class);
        KeyDeserializer kd = _cache.findKeyDeserializer(_context, _factory, type);
        assertNotNull(kd);
        assertTrue(kd instanceof CustomKeyDeserializer);
    }

    // Tests findKeyDeserializer on unhandled abstract type throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testFindKeyDeserializer_unhandledAbstractType_throwsJsonMappingException() throws Exception {
        JavaType type = _typeFactory.constructType(AbstractType.class);
        _cache.findKeyDeserializer(_context, _factory, type);
    }

    // Tests resolving abstract type via AbstractTypeResolver in DeserializerFactory
    @Test
    public void testFindValueDeserializer_abstractTypeResolvedViaFactory_returnsDeserializer() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(AbstractType.class, SimpleBean.class);
        DeserializerFactory factoryWithResolver = _factory.withAbstractTypeResolver(resolver);

        JavaType type = _typeFactory.constructType(AbstractType.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, factoryWithResolver, type);
        assertNotNull(deser);
    }
}