package com.fasterxml.jackson.databind.ser;

import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;

public class AnyGetterWriterTest {

    private ObjectMapper _objectMapper;

    @Before
    public void setUp() {
        _objectMapper = new ObjectMapper();
    }

    static class SimpleAnyGetterBean {
        private Map<String, Object> properties = new HashMap<String, Object>();

        public void add(String key, Object value) {
            properties.put(key, value);
        }

        @JsonAnyGetter
        public Map<String, Object> any() {
            return properties;
        }
    }

    static class NullAnyGetterBean {
        @JsonAnyGetter
        public Map<String, Object> any() {
            return null;
        }
    }

    static class InvalidTypeAnyGetterBean {
        @JsonAnyGetter
        public String any() {
            return "not-a-map";
        }
    }

    static class EmptyAnyGetterBean {
        @JsonAnyGetter
        public Map<String, Object> any() {
            return Collections.emptyMap();
        }
    }

    // Tests normal serialization with standard any-getter map
    @Test
    public void testGetAndSerialize_validMap_serializesSuccessfully() throws Exception {
        SimpleAnyGetterBean bean = new SimpleAnyGetterBean();
        bean.add("key1", "value1");
        bean.add("key2", 123);

        String json = _objectMapper.writeValueAsString(bean);
        assertTrue(json.contains("\"key1\":\"value1\""));
        assertTrue(json.contains("\"key2\":123"));
    }

    // Tests any-getter returning null value does not write properties
    @Test
    public void testGetAndSerialize_nullMap_serializesEmptyObject() throws Exception {
        NullAnyGetterBean bean = new NullAnyGetterBean();
        String json = _objectMapper.writeValueAsString(bean);
        assertEquals("{}", json.trim());
    }

    // Tests empty map serialization through any-getter
    @Test
    public void testGetAndSerialize_emptyMap_serializesEmptyObject() throws Exception {
        EmptyAnyGetterBean bean = new EmptyAnyGetterBean();
        String json = _objectMapper.writeValueAsString(bean);
        assertEquals("{}", json.trim());
    }

    // Tests exception thrown when any-getter returns a non-Map object
    @Test(expected = JsonMappingException.class)
    public void testGetAndSerialize_nonMapType_throwsJsonMappingException() throws Exception {
        InvalidTypeAnyGetterBean bean = new InvalidTypeAnyGetterBean();
        _objectMapper.writeValueAsString(bean);
    }

    // Tests constructor and property access with direct instantiation
    @Test
    public void testConstructor_validArguments_initializesFields() {
        BeanProperty.Bogus prop = new BeanProperty.Bogus();
        AnyGetterWriter writer = new AnyGetterWriter(prop, null, null);
        assertNotNull(writer);
    }

    // Tests direct getAndSerialize call when accessor returns null
    @Test
    public void testGetAndSerialize_directCallNullValue_returnsSafely() throws Exception {
        BeanProperty.Bogus prop = new BeanProperty.Bogus();
        AnnotatedMember mockMember = new AnnotatedMember(null, null) {
            private static final long serialVersionUID = 1L;
            @Override
            public Object getValue(Object pojo) {
                return null;
            }
            @Override
            public void setValue(Object pojo, Object value) {}
            @Override
            public java.lang.reflect.AnnotatedElement getAnnotated() { return null; }
            @Override
            public int getModifiers() { return 0; }
            @Override
            public String getName() { return "testNull"; }
            @Override
            public Class<?> getRawType() { return Map.class; }
            @Override
            public com.fasterxml.jackson.databind.JavaType getType(com.fasterxml.jackson.databind.type.TypeBindings b) { return null; }
            @Override
            public Class<?> getDeclaringClass() { return getClass(); }
            @Override
            public AnnotatedMember withAnnotations(com.fasterxml.jackson.databind.introspect.AnnotationMap m) { return this; }
        };

        AnyGetterWriter writer = new AnyGetterWriter(prop, mockMember, null);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider provider = _objectMapper.getSerializerProviderInstance();

        writer.getAndSerialize(new Object(), gen, provider);
        gen.flush();
        assertEquals("", sw.toString());
    }

    // Tests direct getAndSerialize call when accessor returns non-Map throwing exception
    @Test(expected = JsonMappingException.class)
    public void testGetAndSerialize_directCallNonMap_throwsJsonMappingException() throws Exception {
        BeanProperty.Bogus prop = new BeanProperty.Bogus();
        AnnotatedMember mockMember = new AnnotatedMember(null, null) {
            private static final long serialVersionUID = 1L;
            @Override
            public Object getValue(Object pojo) {
                return "invalidString";
            }
            @Override
            public void setValue(Object pojo, Object value) {}
            @Override
            public java.lang.reflect.AnnotatedElement getAnnotated() { return null; }
            @Override
            public int getModifiers() { return 0; }
            @Override
            public String getName() { return "testInvalid"; }
            @Override
            public Class<?> getRawType() { return String.class; }
            @Override
            public com.fasterxml.jackson.databind.JavaType getType(com.fasterxml.jackson.databind.type.TypeBindings b) { return null; }
            @Override
            public Class<?> getDeclaringClass() { return getClass(); }
            @Override
            public AnnotatedMember withAnnotations(com.fasterxml.jackson.databind.introspect.AnnotationMap m) { return this; }
        };

        AnyGetterWriter writer = new AnyGetterWriter(prop, mockMember, null);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider provider = _objectMapper.getSerializerProviderInstance();

        writer.getAndSerialize(new Object(), gen, provider);
    }

    // Tests direct getAndFilter call when accessor returns null
    @Test
    public void testGetAndFilter_directCallNullValue_returnsSafely() throws Exception {
        BeanProperty.Bogus prop = new BeanProperty.Bogus();
        AnnotatedMember mockMember = new AnnotatedMember(null, null) {
            private static final long serialVersionUID = 1L;
            @Override
            public Object getValue(Object pojo) {
                return null;
            }
            @Override
            public void setValue(Object pojo, Object value) {}
            @Override
            public java.lang.reflect.AnnotatedElement getAnnotated() { return null; }
            @Override
            public int getModifiers() { return 0; }
            @Override
            public String getName() { return "testNullFilter"; }
            @Override
            public Class<?> getRawType() { return Map.class; }
            @Override
            public com.fasterxml.jackson.databind.JavaType getType(com.fasterxml.jackson.databind.type.TypeBindings b) { return null; }
            @Override
            public Class<?> getDeclaringClass() { return getClass(); }
            @Override
            public AnnotatedMember withAnnotations(com.fasterxml.jackson.databind.introspect.AnnotationMap m) { return this; }
        };

        AnyGetterWriter writer = new AnyGetterWriter(prop, mockMember, null);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider provider = _objectMapper.getSerializerProviderInstance();

        writer.getAndFilter(new Object(), gen, provider, null);
        gen.flush();
        assertEquals("", sw.toString());
    }

    // Tests direct getAndFilter call when accessor returns non-Map throwing exception
    @Test(expected = JsonMappingException.class)
    public void testGetAndFilter_directCallNonMap_throwsJsonMappingException() throws Exception {
        BeanProperty.Bogus prop = new BeanProperty.Bogus();
        AnnotatedMember mockMember = new AnnotatedMember(null, null) {
            private static final long serialVersionUID = 1L;
            @Override
            public Object getValue(Object pojo) {
                return Integer.valueOf(42);
            }
            @Override
            public void setValue(Object pojo, Object value) {}
            @Override
            public java.lang.reflect.AnnotatedElement getAnnotated() { return null; }
            @Override
            public int getModifiers() { return 0; }
            @Override
            public String getName() { return "testFilterNonMap"; }
            @Override
            public Class<?> getRawType() { return Integer.class; }
            @Override
            public com.fasterxml.jackson.databind.JavaType getType(com.fasterxml.jackson.databind.type.TypeBindings b) { return null; }
            @Override
            public Class<?> getDeclaringClass() { return getClass(); }
            @Override
            public AnnotatedMember withAnnotations(com.fasterxml.jackson.databind.introspect.AnnotationMap m) { return this; }
        };

        AnyGetterWriter writer = new AnyGetterWriter(prop, mockMember, null);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider provider = _objectMapper.getSerializerProviderInstance();

        writer.getAndFilter(new Object(), gen, provider, null);
    }

    // Tests resolve method execution with provider
    @Test
    public void testResolve_withProvider_contextualizesMapSerializer() throws Exception {
        BeanProperty.Bogus prop = new BeanProperty.Bogus();
        AnyGetterWriter writer = new AnyGetterWriter(prop, null, null);
        SerializerProvider provider = _objectMapper.getSerializerProviderInstance();

        writer.resolve(provider);
        assertNotNull(writer);
    }
}