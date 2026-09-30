package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class DeserializerCacheTest {

    private DeserializerCache _cache;
    private DeserializationContext _context;
    private DeserializerFactory _factory;
    private TypeFactory _typeFactory;

    static class SimpleBean implements Serializable {
        private static final long serialVersionUID = 1L;
        public String name;
        public int age;
    }

    enum TestEnum {
        ALPHA, BETA
    }

    static abstract class AbstractBean {
        public int id;
    }

    static class NonKeyClass {
        public int x;
    }

    static class RecursiveBean {
        public RecursiveBean next;
        public String value;
    }

    @JsonDeserialize(using = CustomBeanDeserializer.class)
    static class CustomAnnotatedBean {
        public String text;
    }

    public static class CustomBeanDeserializer extends JsonDeserializer<CustomAnnotatedBean> {
        @Override
        public CustomAnnotatedBean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            CustomAnnotatedBean bean = new CustomAnnotatedBean();
            bean.text = p.getText();
            return bean;
        }
    }

    @JsonDeserialize(keyUsing = CustomKeyDeser.class)
    static class CustomKeyBean {
        public String keyId;
    }

    public static class CustomKeyDeser extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) {
            CustomKeyBean bean = new CustomKeyBean();
            bean.keyId = key;
            return bean;
        }
    }

    @Before
    public void setUp() {
        _cache = new DeserializerCache();
        ObjectMapper mapper = new ObjectMapper();
        _context = mapper.getDeserializationContext();
        _factory = BeanDeserializerFactory.instance;
        _typeFactory = mapper.getTypeFactory();
    }

    // Tests initial cache count is zero
    @Test
    public void testCachedDeserializersCount_initialState_returnsZero() {
        assertEquals(0, _cache.cachedDeserializersCount());
    }

    // Tests flush removes cached deserializers
    @Test
    public void testFlushCachedDeserializers_withCachedEntries_clearsCache() throws Exception {
        JavaType type = _typeFactory.constructType(SimpleBean.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
        assertTrue(_cache.cachedDeserializersCount() > 0);

        _cache.flushCachedDeserializers();
        assertEquals(0, _cache.cachedDeserializersCount());
    }

    // Tests null JavaType in _findCachedDeserializer throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testFindCachedDeserializer_nullType_throwsException() {
        _cache._findCachedDeserializer(null);
    }

    // Tests finding value deserializer for simple bean type
    @Test
    public void testFindValueDeserializer_simpleBean_returnsDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(SimpleBean.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
        assertTrue(_cache.cachedDeserializersCount() >= 1);

        // Fetching again should return cached instance
        JsonDeserializer<Object> deser2 = _cache.findValueDeserializer(_context, _factory, type);
        assertSame(deser, deser2);
    }

    // Tests finding value deserializer for enum type
    @Test
    public void testFindValueDeserializer_enumType_returnsDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(TestEnum.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
        assertTrue(_cache.hasValueDeserializerFor(_context, _factory, type));
    }

    // Tests finding value deserializer for array type
    @Test
    public void testFindValueDeserializer_arrayType_returnsDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(String[].class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests finding value deserializer for map type
    @Test
    public void testFindValueDeserializer_mapType_returnsDeserializer() throws Exception {
        JavaType type = _typeFactory.constructMapType(HashMap.class, String.class, Integer.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests finding value deserializer for collection type
    @Test
    public void testFindValueDeserializer_collectionType_returnsDeserializer() throws Exception {
        JavaType type = _typeFactory.constructCollectionType(ArrayList.class, String.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests finding value deserializer for JsonNode tree type
    @Test
    public void testFindValueDeserializer_jsonNodeType_returnsDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(JsonNode.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
    }

    // Tests hasValueDeserializerFor returns true for resolvable type
    @Test
    public void testHasValueDeserializerFor_validType_returnsTrue() throws Exception {
        JavaType type = _typeFactory.constructType(String.class);
        boolean hasDeser = _cache.hasValueDeserializerFor(_context, _factory, type);
        assertTrue(hasDeser);
    }

    // Tests findKeyDeserializer for standard key type
    @Test
    public void testFindKeyDeserializer_stringType_returnsKeyDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(String.class);
        KeyDeserializer kd = _cache.findKeyDeserializer(_context, _factory, type);
        assertNotNull(kd);
    }

    // Tests findKeyDeserializer for unsupported key type throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testFindKeyDeserializer_unsupportedType_throwsException() throws Exception {
        JavaType type = _typeFactory.constructType(NonKeyClass.class);
        _cache.findKeyDeserializer(_context, _factory, type);
    }

    // Tests error handling for unknown abstract value deserializer
    @Test
    public void testHandleUnknownValueDeserializer_abstractType_throwsJsonMappingException() {
        JavaType type = _typeFactory.constructType(AbstractBean.class);
        try {
            _cache._handleUnknownValueDeserializer(_context, type);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("abstract") || e.getMessage().contains("AbstractBean"));
        }
    }

    // Tests error handling for unknown concrete value deserializer
    @Test
    public void testHandleUnknownValueDeserializer_concreteType_throwsJsonMappingException() {
        JavaType type = _typeFactory.constructType(SimpleBean.class);
        try {
            _cache._handleUnknownValueDeserializer(_context, type);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Value deserializer") || e.getMessage().contains("SimpleBean"));
        }
    }

    // Tests error handling for unknown key deserializer
    @Test
    public void testHandleUnknownKeyDeserializer_type_throwsJsonMappingException() {
        JavaType type = _typeFactory.constructType(SimpleBean.class);
        try {
            _cache._handleUnknownKeyDeserializer(_context, type);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Key deserializer") || e.getMessage().contains("SimpleBean"));
        }
    }

    // Tests writeReplace lifecycle method clears incomplete deserializers
    @Test
    public void testWriteReplace_invoked_returnsSameInstance() {
        Object replaced = _cache.writeReplace();
        assertSame(_cache, replaced);
    }

    // Tests finding value deserializer for recursive bean type
    @Test
    public void testFindValueDeserializer_recursiveType_resolvesSuccessfully() throws Exception {
        JavaType type = _typeFactory.constructType(RecursiveBean.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
        assertTrue(_cache.cachedDeserializersCount() > 0);
    }

    // Tests finding value deserializer for primitive types
    @Test
    public void testFindValueDeserializer_primitiveTypes_returnsDeserializer() throws Exception {
        JavaType intType = _typeFactory.constructType(int.class);
        JsonDeserializer<Object> intDeser = _cache.findValueDeserializer(_context, _factory, intType);
        assertNotNull(intDeser);

        JavaType boolType = _typeFactory.constructType(boolean.class);
        JsonDeserializer<Object> boolDeser = _cache.findValueDeserializer(_context, _factory, boolType);
        assertNotNull(boolDeser);
    }

    // Tests finding value deserializer with custom @JsonDeserialize annotation
    @Test
    public void testFindValueDeserializer_customAnnotatedBean_returnsCustomDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(CustomAnnotatedBean.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        assertNotNull(deser);
        assertTrue(deser instanceof CustomBeanDeserializer);
    }

    // Tests finding key deserializer with custom @JsonDeserialize(keyUsing = ...)
    @Test
    public void testFindKeyDeserializer_customAnnotatedKeyBean_returnsCustomKeyDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(CustomKeyBean.class);
        KeyDeserializer kd = _cache.findKeyDeserializer(_context, _factory, type);
        assertNotNull(kd);
        assertTrue(kd instanceof CustomKeyDeser);
    }

    // Tests finding key deserializer for enum type
    @Test
    public void testFindKeyDeserializer_enumType_returnsKeyDeserializer() throws Exception {
        JavaType type = _typeFactory.constructType(TestEnum.class);
        KeyDeserializer kd = _cache.findKeyDeserializer(_context, _factory, type);
        assertNotNull(kd);
    }

    // Tests finding value deserializer for uninstantiable abstract type throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testFindValueDeserializer_abstractTypeWithoutAnnotation_throwsException() throws Exception {
        JavaType type = _typeFactory.constructType(AbstractBean.class);
        _cache.findValueDeserializer(_context, _factory, type);
    }

    // Tests hasValueDeserializerFor returns false for unhandled abstract type
    @Test
    public void testHasValueDeserializerFor_unresolvableAbstractType_returnsFalse() {
        JavaType type = _typeFactory.constructType(AbstractBean.class);
        assertFalse(_cache.hasValueDeserializerFor(_context, _factory, type));
    }
}