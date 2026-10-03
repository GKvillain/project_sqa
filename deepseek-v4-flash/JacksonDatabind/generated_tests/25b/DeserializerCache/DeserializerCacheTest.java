package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.*;
import com.fasterxml.jackson.databind.deser.DeserializerCache;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.deser.DeserializerFactory;
import com.fasterxml.jackson.databind.deser.BasicDeserializerFactory;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.util.Converter;

import java.io.*;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class DeserializerCacheTest {

    private DeserializerCache cache;
    private DeserializationContext ctxt;
    private DeserializerFactory factory;
    private TypeFactory typeFactory;

    @Before
    public void setUp() throws Exception {
        cache = new DeserializerCache();
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        factory = new BasicDeserializerFactory();
        ctxt = DeserializationContext.createRootContext(config, factory);
        typeFactory = config.getTypeFactory();
    }

    // Helper: create a simple dummy JsonDeserializer for custom handlers
    private static class DummyDeserializer extends StdDeserializer<Object> {
        public DummyDeserializer() { super(Object.class); }
        @Override
        public Object deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) throws java.io.IOException, com.fasterxml.jackson.core.JsonProcessingException {
            return null;
        }
    }

    // Helper: abstract class for unknown deserializer test
    private abstract static class AbstractDummy {
        public abstract void doNothing();
    }

    // Helper: self-referencing bean for circular dependency test
    private static class SelfRefBean {
        public SelfRefBean next;
        public int value;
    }

    // Test 1: Initial cache count is zero
    @Test
    public void testCachedDeserializersCount_initial_returnsZero() {
        assertEquals(0, cache.cachedDeserializersCount());
    }

    // Test 2: Flush clears cache
    @Test
    public void testFlushCachedDeserializers_afterCacheClears() throws Exception {
        // Force cache of a deserializer
        JavaType strType = typeFactory.constructType(String.class);
        cache.findValueDeserializer(ctxt, factory, strType);
        assertTrue(cache.cachedDeserializersCount() > 0);
        cache.flushCachedDeserializers();
        assertEquals(0, cache.cachedDeserializersCount());
    }

    // Test 3: findValueDeserializer for simple type returns non-null
    @Test
    public void testFindValueDeserializer_simpleString_returnsDeserializer() throws Exception {
        JavaType strType = typeFactory.constructType(String.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(ctxt, factory, strType);
        assertNotNull(deser);
    }

    // Test 4: findValueDeserializer caches and returns same instance on second call
    @Test
    public void testFindValueDeserializer_cached_returnsCached() throws Exception {
        JavaType strType = typeFactory.constructType(String.class);
        JsonDeserializer<Object> first = cache.findValueDeserializer(ctxt, factory, strType);
        int countAfterFirst = cache.cachedDeserializersCount();
        JsonDeserializer<Object> second = cache.findValueDeserializer(ctxt, factory, strType);
        assertSame(first, second);
        // Cache size should not increase
        assertEquals(countAfterFirst, cache.cachedDeserializersCount());
    }

    // Test 5: findValueDeserializer with null JavaType throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFindValueDeserializer_nullJavaType_throwsIllegalArgument() throws Exception {
        cache.findValueDeserializer(ctxt, factory, null);
    }

    // Test 6: hasValueDeserializerFor for concrete type returns true
    @Test
    public void testHasValueDeserializerFor_stringType_returnsTrue() throws Exception {
        JavaType strType = typeFactory.constructType(String.class);
        assertTrue(cache.hasValueDeserializerFor(ctxt, factory, strType));
    }

    // Test 7: hasValueDeserializerFor for abstract type returns false
    @Test
    public void testHasValueDeserializerFor_abstractType_returnsFalse() throws Exception {
        JavaType abstractType = typeFactory.constructType(AbstractDummy.class);
        assertFalse(cache.hasValueDeserializerFor(ctxt, factory, abstractType));
    }

    // Test 8: findKeyDeserializer for known key type returns non-null
    @Test
    public void testFindKeyDeserializer_stringKey_returnsKeyDeserializer() throws Exception {
        JavaType stringType = typeFactory.constructType(String.class);
        KeyDeserializer kd = cache.findKeyDeserializer(ctxt, factory, stringType);
        assertNotNull(kd);
    }

    // Test 9: findKeyDeserializer for unknown key type throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testFindKeyDeserializer_unknownType_throwsJsonMapping() throws Exception {
        // We need a type that has no key deserializer; use a custom class without any registration
        JavaType customType = typeFactory.constructType(AbstractDummy.class);
        cache.findKeyDeserializer(ctxt, factory, customType);
    }

    // Test 10: Circular dependency via self-referencing bean triggers _incompleteDeserializers path
    @Test
    public void testFindValueDeserializer_circularSelfReference_resolvesCorrectly() throws Exception {
        JavaType selfRefType = typeFactory.constructType(SelfRefBean.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(ctxt, factory, selfRefType);
        assertNotNull(deser);
        // Should be cached (bean deserializer is cachable)
        assertTrue(cache.cachedDeserializersCount() > 0);
    }

    // Test 11: Deserializer for type with custom value handler should not be cached
    @Test
    public void testFindValueDeserializer_customValueHandler_notCached() throws Exception {
        // Create a MapType and assign a custom value handler (dummy deserializer) to its content type
        JavaType mapType = typeFactory.constructMapType(HashMap.class, String.class, String.class);
        JavaType contentType = mapType.getContentType();
        // Assign a custom handler
        JavaType modifiedMapType = ((MapType) mapType).withContentValueHandler(new DummyDeserializer());
        // Ensure the cache is empty
        cache.flushCachedDeserializers();
        JsonDeserializer<Object> deser = cache.findValueDeserializer(ctxt, factory, modifiedMapType);
        assertNotNull(deser);
        // Since custom value handler present, caching should be skipped
        assertEquals(0, cache.cachedDeserializersCount());
    }

    // Test 12: Deserializer for type with custom value type handler (TypeHandler) also not cached
    @Test
    public void testFindValueDeserializer_customTypeHandler_notCached() throws Exception {
        JavaType mapType = typeFactory.constructMapType(HashMap.class, String.class, String.class);
        JavaType contentType = mapType.getContentType();
        // Assign a custom type handler (dummy deserializer) – using withContentTypeHandler
        JavaType modifiedMapType = ((MapType) mapType).withContentTypeHandler(new DummyDeserializer());
        cache.flushCachedDeserializers();
        JsonDeserializer<Object> deser = cache.findValueDeserializer(ctxt, factory, modifiedMapType);
        assertNotNull(deser);
        assertEquals(0, cache.cachedDeserializersCount());
    }

    // Test 13: Abstract type leads to _handleUnknownValueDeserializer and throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testFindValueDeserializer_abstractType_throwsJsonMapping() throws Exception {
        // Note: 'hasValueDeserializerFor' would return false, but findValueDeserializer should throw
        JavaType abstractType = typeFactory.constructType(AbstractDummy.class);
        // This should call _handleUnknownValueDeserializer
        cache.findValueDeserializer(ctxt, factory, abstractType);
    }

    // Test 14: Null key deserializer type? Not applicable, but we can test that findKeyDeserializer handles null type gracefully? Actually factory.createKeyDeserializer may return null for unknown, leading to _handleUnknownKeyDeserializer. Already covered by test 9.

    // Test 15: Verify hasValueDeserializerFor returns false for null? hasValueDeserializerFor does not accept null? The source code does not show null check; it will call _findCachedDeserializer which throws IllegalArgumentException for null type. We can test that.
    @Test(expected = IllegalArgumentException.class)
    public void testHasValueDeserializerFor_nullJavaType_throwsIllegalArgument() throws Exception {
        cache.hasValueDeserializerFor(ctxt, factory, null);
    }
}