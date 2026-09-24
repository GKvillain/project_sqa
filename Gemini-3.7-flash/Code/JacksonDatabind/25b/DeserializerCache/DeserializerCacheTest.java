package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.StdConverter;

public class DeserializerCacheTest {

    private DeserializerCache _cache;
    private DeserializationContext _context;
    private DeserializerFactory _factory;
    private TypeFactory _typeFactory;

    static enum TestEnum {
        ALPHA, BETA;
    }

    static class SimpleBean {
        public int x;
        public String name;
    }

    static abstract class AbstractBase {
        public int id;
    }

    static class Unkeyable {
        private final int value;
        public Unkeyable(int v, boolean flag) { this.value = v; }
        public int getValue() { return value; }
    }

    static class ConvertedBean {
        public int number;
    }

    static class StringToConvertedBeanConverter extends StdConverter<String, ConvertedBean> {
        @Override
        public ConvertedBean convert(String value) {
            ConvertedBean bean = new ConvertedBean();
            bean.number = Integer.parseInt(value);
            return bean;
        }
    }

    static class RecursiveBean {
        public RecursiveBean next;
        public String value;
    }

    @JsonDeserialize(converter = StringToConvertedBeanConverter.class)
    static class ConvertedByAnnotationBean {
        public int number;
    }

    static class CustomDummyDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }
    }

    @JsonDeserialize(using = CustomDummyDeserializer.class)
    static class AnnotatedWithCustomDeser {
        public String val;
    }

    @Before
    public void setUp() {
        _cache = new DeserializerCache();
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        _factory = BeanDeserializerFactory.instance;
        _context = new DefaultDeserializationContext.Impl(_factory).createInstance(config, null, null);
        _typeFactory = TypeFactory.defaultInstance();
    }

    // Tests initial cache count is zero
    @Test
    public void testCachedDeserializersCount_initially_returnsZero() {
        assertEquals(0, _cache.cachedDeserializersCount());
    }

    // Tests flushing cached deserializers clears cache entries
    @Test
    public void testFlushCachedDeserializers_hasEntries_clearsCache() throws JsonMappingException {
        JavaType type = _typeFactory.constructType(SimpleBean.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
        assertTrue(_cache.cachedDeserializersCount() > 0);

        _cache.flushCachedDeserializers();
        assertEquals(0, _cache.cachedDeserializersCount());
    }

    // Tests null JavaType in cache lookup throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFindCachedDeserializer_nullType_throwsIllegalArgumentException() {
        _cache._findCachedDeserializer(null);
    }

    // Tests finding deserializer for a simple POJO class and verifies caching
    @Test
    public void testFindValueDeserializer_simplePojo_successAndCached() throws JsonMappingException {
        JavaType type = _typeFactory.constructType(SimpleBean.class);
        JsonDeserializer<Object> deser1 = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser1);
        assertEquals(1, _cache.cachedDeserializersCount());

        JsonDeserializer<Object> deser2 = _cache.findValueDeserializer(_context, _factory, type);
        assertSame(deser1, deser2);
    }

    // Tests finding deserializer for an Enum type
    @Test
    public void testFindValueDeserializer_enumType_returnsDeserializer() throws JsonMappingException {
        JavaType type = _typeFactory.constructType(TestEnum.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
        assertTrue(_cache.hasValueDeserializerFor(_context, _factory, type));
    }

    // Tests finding deserializer for an Array type
    @Test
    public void testFindValueDeserializer_arrayType_returnsDeserializer() throws JsonMappingException {
        JavaType type = _typeFactory.constructArrayType(SimpleBean.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests finding deserializer for a Collection (List) type
    @Test
    public void testFindValueDeserializer_collectionType_returnsDeserializer() throws JsonMappingException {
        JavaType type = _typeFactory.constructCollectionType(List.class, SimpleBean.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests finding deserializer for a Map type
    @Test
    public void testFindValueDeserializer_mapType_returnsDeserializer() throws JsonMappingException {
        JavaType type = _typeFactory.constructMapType(Map.class, String.class, SimpleBean.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests finding deserializer for a JsonNode tree model
    @Test
    public void testFindValueDeserializer_treeType_returnsDeserializer() throws JsonMappingException {
        JavaType type = _typeFactory.constructType(JsonNode.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests hasValueDeserializerFor returns true for existing deserializer
    @Test
    public void testHasValueDeserializerFor_validType_returnsTrue() throws JsonMappingException {
        JavaType type = _typeFactory.constructType(SimpleBean.class);
        boolean hasDeser = _cache.hasValueDeserializerFor(_context, _factory, type);
        assertTrue(hasDeser);
    }

    // Tests key deserializer lookup for standard String key
    @Test
    public void testFindKeyDeserializer_stringKey_returnsKeyDeserializer() throws JsonMappingException {
        JavaType type = _typeFactory.constructType(String.class);
        KeyDeserializer kd = _cache.findKeyDeserializer(_context, _factory, type);
        assertNotNull(kd);
    }

    // Tests key deserializer lookup for unkeyable type throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testFindKeyDeserializer_unknownKeyType_throwsJsonMappingException() throws JsonMappingException {
        JavaType type = _typeFactory.constructType(Unkeyable.class);
        _cache.findKeyDeserializer(_context, _factory, type);
    }

    // Tests writeReplace clears incomplete deserializers
    @Test
    public void testWriteReplace_invoked_clearsIncompleteDeserializers() {
        Object replaced = _cache.writeReplace();
        assertSame(_cache, replaced);
        assertEquals(0, _cache._incompleteDeserializers.size());
    }

    // Tests exception message for unknown abstract value type
    @Test
    public void testHandleUnknownValueDeserializer_abstractType_throwsJsonMappingException() {
        JavaType type = _typeFactory.constructType(AbstractBase.class);
        try {
            _cache._handleUnknownValueDeserializer(type);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("abstract"));
        }
    }

    // Tests exception message for unknown concrete value type
    @Test
    public void testHandleUnknownValueDeserializer_concreteType_throwsJsonMappingException() {
        JavaType type = _typeFactory.constructType(SimpleBean.class);
        try {
            _cache._handleUnknownValueDeserializer(type);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Can not find a Value deserializer"));
        }
    }

    // Tests exception message for unknown key deserializer
    @Test
    public void testHandleUnknownKeyDeserializer_unknownType_throwsJsonMappingException() {
        JavaType type = _typeFactory.constructType(Unkeyable.class);
        try {
            _cache._handleUnknownKeyDeserializer(type);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Can not find a (Map) Key deserializer"));
        }
    }

    // Tests types with custom value handlers bypass caching
    @Test
    public void testFindCachedDeserializer_withCustomContentValueHandler_returnsNull() {
        JavaType baseType = _typeFactory.constructCollectionType(ArrayList.class, String.class);
        JavaType customType = baseType.withContentValueHandler("dummyHandler");
        JsonDeserializer<Object> deser = _cache._findCachedDeserializer(customType);
        assertNull(deser);
    }

    // Tests finding deserializer for recursive types handles incomplete deserializer resolution
    @Test
    public void testFindValueDeserializer_recursiveType_success() throws JsonMappingException {
        JavaType type = _typeFactory.constructType(RecursiveBean.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
        assertTrue(_cache.cachedDeserializersCount() > 0);
    }

    // Tests finding deserializer for a class configured with @JsonDeserialize(converter=...)
    @Test
    public void testFindValueDeserializer_annotatedConverter_returnsConvertingDeserializer() throws JsonMappingException {
        JavaType type = _typeFactory.constructType(ConvertedByAnnotationBean.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests finding deserializer for a class configured with @JsonDeserialize(using=...)
    @Test
    public void testFindValueDeserializer_customDeserializerAnnotation_returnsCustomDeser() throws JsonMappingException {
        JavaType type = _typeFactory.constructType(AnnotatedWithCustomDeser.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests finding deserializer for a ReferenceType (e.g. AtomicReference)
    @Test
    public void testFindValueDeserializer_referenceType_returnsDeserializer() throws JsonMappingException {
        JavaType type = _typeFactory.constructReferenceType(AtomicReference.class, _typeFactory.constructType(SimpleBean.class));
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests hasValueDeserializerFor returns false when the type cannot be deserialized
    @Test
    public void testHasValueDeserializerFor_abstractTypeWithoutDeser_returnsFalse() {
        JavaType type = _typeFactory.constructType(AbstractBase.class);
        boolean hasDeser = _cache.hasValueDeserializerFor(_context, _factory, type);
        assertFalse(hasDeser);
    }

    // Tests findCachedDeserializer returns null when type has valueHandler or typeHandler
    @Test
    public void testFindCachedDeserializer_withValueOrTypeHandler_returnsNull() {
        JavaType baseType = _typeFactory.constructType(SimpleBean.class);
        JavaType withValHandler = baseType.withValueHandler("handler");
        assertNull(_cache._findCachedDeserializer(withValHandler));

        JavaType withTypeHandler = baseType.withTypeHandler("typeHandler");
        assertNull(_cache._findCachedDeserializer(withTypeHandler));
    }

    // Tests findCachedDeserializer returns null when type has contentTypeHandler
    @Test
    public void testFindCachedDeserializer_withContentTypeHandler_returnsNull() {
        JavaType baseType = _typeFactory.constructCollectionType(ArrayList.class, SimpleBean.class);
        JavaType withContentTypeHandler = baseType.withContentTypeHandler("contentTypeHandler");
        assertNull(_cache._findCachedDeserializer(withContentTypeHandler));
    }
}