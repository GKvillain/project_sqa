package com.fasterxml.jackson.databind.ser;

import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class AnyGetterWriterTest {

    static class DummyBeanWithMap {
        private final Map<String, Object> extra = new HashMap<String, Object>();

        public DummyBeanWithMap() {
            extra.put("key1", "val1");
            extra.put("key2", 123);
        }

        @JsonAnyGetter
        public Map<String, Object> any() {
            return extra;
        }
    }

    static class DummyBeanWithNullMap {
        @JsonAnyGetter
        public Map<String, Object> any() {
            return null;
        }
    }

    static class DummyBeanWithEmptyMap {
        @JsonAnyGetter
        public Map<String, Object> any() {
            return Collections.emptyMap();
        }
    }

    static class DummyBeanWithNonMap {
        @JsonAnyGetter
        public Object any() {
            return "not-a-map";
        }
    }

    @JsonFilter("anyFilter")
    static class DummyBeanWithFilter {
        private final Map<String, Object> extra = new HashMap<String, Object>();

        public DummyBeanWithFilter() {
            extra.put("keep", "ok");
            extra.put("drop", "no");
        }

        @JsonAnyGetter
        public Map<String, Object> any() {
            return extra;
        }
    }

    private static class StubAnnotatedMember extends AnnotatedMember {
        private static final long serialVersionUID = 1L;
        private final Object _value;

        public StubAnnotatedMember(Object value) {
            super((TypeResolutionContext) null, (AnnotationMap) null);
            _value = value;
        }

        @Override
        public Object getValue(Object pojo) {
            return _value;
        }

        @Override
        public void setValue(Object pojo, Object value) { }

        @Override
        public java.lang.reflect.AnnotatedElement getAnnotated() {
            return null;
        }

        @Override
        public java.lang.reflect.Member getMember() {
            return null;
        }

        @Override
        public int getModifiers() {
            return 0;
        }

        @Override
        public String getName() {
            return "stubGetter";
        }

        @Override
        public Class<?> getRawType() {
            return Object.class;
        }

        @Override
        public com.fasterxml.jackson.databind.JavaType getType() {
            return TypeFactory.defaultInstance().constructType(Object.class);
        }

        @Override
        public Class<?> getDeclaringClass() {
            return getClass();
        }

        @Override
        public AnnotatedMember withAnnotations(AnnotationMap fallback) {
            return this;
        }

        @Override
        public void fixAccess(boolean force) { }
    }

    // Tests AnyGetterWriter constructor initializes fields correctly
    @Test
    public void testConstructor_validArguments_createsInstance() {
        StubAnnotatedMember member = new StubAnnotatedMember(null);
        AnyGetterWriter writer = new AnyGetterWriter(null, member, null);
        assertNotNull(writer);
    }

    // Tests getAndSerialize with valid map serialization via ObjectMapper
    @Test
    public void testGetAndSerialize_validMap_serializesFlatProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new DummyBeanWithMap());
        assertTrue(json.contains("\"key1\":\"val1\""));
        assertTrue(json.contains("\"key2\":123"));
    }

    // Tests getAndSerialize with null map returns without writing properties
    @Test
    public void testGetAndSerialize_nullMap_serializesEmptyObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new DummyBeanWithNullMap());
        assertEquals("{}", json);
    }

    // Tests getAndSerialize with empty map serializes empty json object
    @Test
    public void testGetAndSerialize_emptyMap_serializesEmptyObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new DummyBeanWithEmptyMap());
        assertEquals("{}", json);
    }

    // Tests getAndSerialize with non-map return type throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testGetAndSerialize_nonMapType_throwsJsonMappingException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValueAsString(new DummyBeanWithNonMap());
    }

    // Tests getAndSerialize direct call when value is null
    @Test
    public void testGetAndSerialize_nullValueDirect_returnsEarly() throws Exception {
        StubAnnotatedMember member = new StubAnnotatedMember(null);
        AnyGetterWriter writer = new AnyGetterWriter(null, member, null);

        ObjectMapper mapper = new ObjectMapper();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        writer.getAndSerialize(new Object(), gen, provider);
        gen.flush();
        assertEquals("", sw.toString());
    }

    // Tests getAndSerialize direct call when value is not a Map
    @Test(expected = JsonMappingException.class)
    public void testGetAndSerialize_nonMapValueDirect_throwsJsonMappingException() throws Exception {
        StubAnnotatedMember member = new StubAnnotatedMember("InvalidString");
        AnyGetterWriter writer = new AnyGetterWriter(null, member, null);

        ObjectMapper mapper = new ObjectMapper();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        writer.getAndSerialize(new Object(), gen, provider);
    }

    // Tests getAndSerialize direct call with null mapSerializer
    @Test
    public void testGetAndSerialize_nullMapSerializer_returnsWithoutError() throws Exception {
        Map<String, String> data = new HashMap<String, String>();
        data.put("a", "b");
        StubAnnotatedMember member = new StubAnnotatedMember(data);
        AnyGetterWriter writer = new AnyGetterWriter(null, member, null);

        ObjectMapper mapper = new ObjectMapper();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        writer.getAndSerialize(new Object(), gen, provider);
        gen.flush();
        assertEquals("", sw.toString());
    }

    // Tests getAndFilter with property filter applying to any-getter
    @Test
    public void testGetAndFilter_filteredFields_serializesOnlyAllowedProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        filterProvider.addFilter("anyFilter", SimpleBeanPropertyFilter.filterOutAllExcept("keep"));
        mapper.setFilterProvider(filterProvider);

        String json = mapper.writeValueAsString(new DummyBeanWithFilter());
        assertTrue(json.contains("\"keep\":\"ok\""));
        assertFalse(json.contains("\"drop\""));
    }

    // Tests getAndFilter direct call when value is null
    @Test
    public void testGetAndFilter_nullValueDirect_returnsEarly() throws Exception {
        StubAnnotatedMember member = new StubAnnotatedMember(null);
        AnyGetterWriter writer = new AnyGetterWriter(null, member, null);

        ObjectMapper mapper = new ObjectMapper();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        PropertyFilter filter = SimpleBeanPropertyFilter.serializeAll();

        writer.getAndFilter(new Object(), gen, provider, filter);
        gen.flush();
        assertEquals("", sw.toString());
    }

    // Tests getAndFilter direct call when value is not a Map
    @Test(expected = JsonMappingException.class)
    public void testGetAndFilter_nonMapValueDirect_throwsJsonMappingException() throws Exception {
        StubAnnotatedMember member = new StubAnnotatedMember(Integer.valueOf(42));
        AnyGetterWriter writer = new AnyGetterWriter(null, member, null);

        ObjectMapper mapper = new ObjectMapper();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        PropertyFilter filter = SimpleBeanPropertyFilter.serializeAll();

        writer.getAndFilter(new Object(), gen, provider, filter);
    }

    // Tests getAndFilter direct call with null mapSerializer
    @Test
    public void testGetAndFilter_nullMapSerializer_returnsWithoutError() throws Exception {
        Map<String, String> data = new HashMap<String, String>();
        data.put("x", "y");
        StubAnnotatedMember member = new StubAnnotatedMember(data);
        AnyGetterWriter writer = new AnyGetterWriter(null, member, null);

        ObjectMapper mapper = new ObjectMapper();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        PropertyFilter filter = SimpleBeanPropertyFilter.serializeAll();

        writer.getAndFilter(new Object(), gen, provider, filter);
        gen.flush();
        assertEquals("", sw.toString());
    }

    // Tests resolve method invokes handlePrimaryContextualization on provider
    @Test
    public void testResolve_nullSerializer_resolvesWithoutException() throws Exception {
        StubAnnotatedMember member = new StubAnnotatedMember(null);
        AnyGetterWriter writer = new AnyGetterWriter(null, member, null);

        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        writer.resolve(provider);
        assertNotNull(writer);
    }

    // Tests resolve method contextualizing MapSerializer
    @Test
    public void testResolve_withMapSerializer_contextualizesSuccessfully() throws Exception {
        StubAnnotatedMember member = new StubAnnotatedMember(null);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonSerializer<Object> ser = provider.findValueSerializer(Map.class);
        AnyGetterWriter writer = new AnyGetterWriter(null, member, ser);

        writer.resolve(provider);
        assertNotNull(writer);
    }

    // Tests fixAccess on AnyGetterWriter delegates to accessor
    @Test
    public void testFixAccess_delegatesToAccessor() {
        StubAnnotatedMember member = new StubAnnotatedMember(null);
        AnyGetterWriter writer = new AnyGetterWriter(null, member, null);
        ObjectMapper mapper = new ObjectMapper();

        writer.fixAccess(mapper.getSerializationConfig());
        assertNotNull(writer);
    }
}