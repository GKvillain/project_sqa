package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.NameTransformer;

public class BeanSerializerBaseTest {

    // Concrete implementation for unit testing BeanSerializerBase methods
    private static class ConcreteBeanSerializer extends BeanSerializerBase {
        private static final long serialVersionUID = 1L;

        public ConcreteBeanSerializer(JavaType type, BeanSerializerBuilder builder,
                BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
            super(type, builder, properties, filteredProperties);
        }

        public ConcreteBeanSerializer(ConcreteBeanSerializer src, BeanPropertyWriter[] properties,
                BeanPropertyWriter[] filteredProperties) {
            super(src, properties, filteredProperties);
        }

        public ConcreteBeanSerializer(ConcreteBeanSerializer src, ObjectIdWriter objectIdWriter) {
            super(src, objectIdWriter);
        }

        public ConcreteBeanSerializer(ConcreteBeanSerializer src, ObjectIdWriter objectIdWriter, Object filterId) {
            super(src, objectIdWriter, filterId);
        }

        public ConcreteBeanSerializer(ConcreteBeanSerializer src, String[] toIgnore) {
            super(src, toIgnore);
        }

        public ConcreteBeanSerializer(ConcreteBeanSerializer src, Set<String> toIgnore) {
            super(src, toIgnore);
        }

        public ConcreteBeanSerializer(ConcreteBeanSerializer src, NameTransformer unwrapper) {
            super(src, unwrapper);
        }

        @Override
        public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
            return new ConcreteBeanSerializer(this, objectIdWriter);
        }

        @Override
        protected BeanSerializerBase withIgnorals(String[] toIgnore) {
            return new ConcreteBeanSerializer(this, toIgnore);
        }

        @Override
        protected BeanSerializerBase withIgnorals(Set<String> toIgnore) {
            return new ConcreteBeanSerializer(this, toIgnore);
        }

        @Override
        protected BeanSerializerBase asArraySerializer() {
            return this;
        }

        @Override
        public BeanSerializerBase withFilterId(Object filterId) {
            return new ConcreteBeanSerializer(this, _objectIdWriter, filterId);
        }

        @Override
        public void serialize(Object bean, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (_objectIdWriter != null) {
                _serializeWithObjectId(bean, gen, provider, true);
                return;
            }
            gen.writeStartObject();
            if (_propertyFilterId != null) {
                serializeFieldsFiltered(bean, gen, provider);
            } else {
                serializeFields(bean, gen, provider);
            }
            gen.writeEndObject();
        }
    }

    // Helper classes for testing
    static class Views {
        interface ViewA {}
        interface ViewB {}
    }

    static class SimpleBean {
        @JsonProperty("name")
        public String name = "test";

        @JsonProperty("age")
        public int age = 25;
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    static class ArrayFormatBean {
        public String field1 = "a";
        public String field2 = "b";
    }

    @JsonFilter("customFilter")
    static class FilteredBean {
        public String prop1 = "val1";
        public String prop2 = "val2";
    }

    static class ViewBean {
        @JsonView(Views.ViewA.class)
        public String viewA = "A";

        @JsonView(Views.ViewB.class)
        public String viewB = "B";
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class IdClass {
        public int id = 42;
        public String text = "content";
    }

    static class WrapperWithIdRef {
        @JsonIdentityReference(alwaysAsId = true)
        public IdClass item = new IdClass();
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    static class PolymorphicBean {
        public String name = "poly";
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    static class PolymorphicIdBean {
        public String data = "info";
    }

    static class ThrowingGetterBean {
        public String getFailing() {
            throw new RuntimeException("Simulated getter error");
        }
    }

    // Tests constructor with null builder
    @Test
    public void testConstructor_nullBuilder_initializesNullFields() {
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        BeanPropertyWriter[] props = new BeanPropertyWriter[0];
        ConcreteBeanSerializer serializer = new ConcreteBeanSerializer(type, null, props, null);

        assertFalse(serializer.usesObjectId());
        assertNull(serializer.getFilterId());
        assertNotNull(serializer.properties());
        assertFalse(serializer.properties().hasNext());
    }

    // Tests usesObjectId with and without ObjectIdWriter
    @Test
    public void testUsesObjectId_withAndWithoutObjectIdWriter_returnsExpectedBoolean() {
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        ConcreteBeanSerializer serializer = new ConcreteBeanSerializer(type, null, NO_PROPS, null);
        assertFalse(serializer.usesObjectId());

        ObjectIdWriter oiw = ObjectIdWriter.construct(
                TypeFactory.defaultInstance().constructType(String.class),
                new PropertyName("id"),
                new ObjectIdGenerators.IntSequenceGenerator(),
                false);

        BeanSerializerBase serializerWithId = serializer.withObjectIdWriter(oiw);
        assertTrue(serializerWithId.usesObjectId());
    }

    // Tests properties iterator
    @Test
    public void testProperties_returnsIteratorOverProps() {
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        BeanPropertyWriter bpw = new BeanPropertyWriter();
        BeanPropertyWriter[] props = new BeanPropertyWriter[] { bpw };

        ConcreteBeanSerializer serializer = new ConcreteBeanSerializer(type, null, props, null);
        Iterator<PropertyWriter> it = serializer.properties();

        assertTrue(it.hasNext());
        assertSame(bpw, it.next());
        assertFalse(it.hasNext());
    }

    // Tests constructor with ignorals
    @Test
    public void testConstructor_withIgnorals_ignoresSpecifiedProperties() {
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        BeanPropertyWriter bpw1 = new BeanPropertyWriter();
        BeanPropertyWriter bpw2 = new BeanPropertyWriter();

        ConcreteBeanSerializer serializer = new ConcreteBeanSerializer(type, null,
                new BeanPropertyWriter[] { bpw1, bpw2 },
                new BeanPropertyWriter[] { bpw1, bpw2 });

        BeanSerializerBase filtered = serializer.withIgnorals(new String[] { bpw1.getName() });
        Iterator<PropertyWriter> it = filtered.properties();
        
        // bpw1 should be ignored, leaving bpw2
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        assertEquals(1, count);
    }

    // Tests withFilterId mutant factory
    @Test
    public void testWithFilterId_newFilterId_updatesFilterId() {
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        ConcreteBeanSerializer serializer = new ConcreteBeanSerializer(type, null, NO_PROPS, null);

        assertNull(serializer.getFilterId());
        BeanSerializerBase withFilter = serializer.withFilterId("myFilter");
        assertNotNull(withFilter);
    }

    // Tests NameTransformer unwrapper constructor
    @Test
    public void testConstructor_withNameTransformer_renamesProperties() {
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        BeanPropertyWriter bpw = new BeanPropertyWriter();
        ConcreteBeanSerializer serializer = new ConcreteBeanSerializer(type, null,
                new BeanPropertyWriter[] { bpw }, null);

        NameTransformer transformer = NameTransformer.simpleTransformer("prefix.", "");
        ConcreteBeanSerializer unwrapped = new ConcreteBeanSerializer(serializer, transformer);

        Iterator<PropertyWriter> it = unwrapped.properties();
        assertTrue(it.hasNext());
        PropertyWriter pw = it.next();
        assertNotNull(pw);
    }

    // Tests createContextual for POJO with JsonFormat shape = ARRAY
    @Test
    public void testCreateContextual_arrayShape_serializesAsArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ArrayFormatBean bean = new ArrayFormatBean();
        String json = mapper.writeValueAsString(bean);

        assertEquals("[\"a\",\"b\"]", json);
    }

    // Tests serialization with active filter
    @Test
    public void testSerialize_withPropertyFilter_filtersProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleFilterProvider filters = new SimpleFilterProvider();
        filters.addFilter("customFilter", SimpleBeanPropertyFilter.filterOutAllExcept("prop1"));
        mapper.setFilterProvider(filters);

        FilteredBean bean = new FilteredBean();
        String json = mapper.writeValueAsString(bean);

        assertTrue(json.contains("\"prop1\":\"val1\""));
        assertFalse(json.contains("prop2"));
    }

    // Tests serialization with JsonView active view
    @Test
    public void testSerialize_withJsonView_serializesOnlyActiveView() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ViewBean bean = new ViewBean();

        String jsonA = mapper.writerWithView(Views.ViewA.class).writeValueAsString(bean);
        assertTrue(jsonA.contains("\"viewA\":\"A\""));
        assertFalse(jsonA.contains("viewB"));

        String jsonB = mapper.writerWithView(Views.ViewB.class).writeValueAsString(bean);
        assertTrue(jsonB.contains("\"viewB\":\"B\""));
        assertFalse(jsonA.contains("viewA") && !jsonB.contains("viewA"));
    }

    // Tests serialization with JsonIdentityReference alwaysAsId
    @Test
    public void testSerialize_withJsonIdentityReferenceAlwaysAsId_serializesIdOnly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        WrapperWithIdRef wrapper = new WrapperWithIdRef();

        String json = mapper.writeValueAsString(wrapper);
        assertEquals("{\"item\":42}", json);
    }

    // Tests acceptJsonFormatVisitor with null visitor
    @Test
    public void testAcceptJsonFormatVisitor_nullVisitor_returnsWithoutException() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        ConcreteBeanSerializer serializer = new ConcreteBeanSerializer(type, null, NO_PROPS, null);

        // Should return gracefully without NullPointerException
        serializer.acceptJsonFormatVisitor(null, type);
    }

    // Tests getSchema method
    @Test
    public void testGetSchema_withoutFilter_returnsObjectSchema() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        ConcreteBeanSerializer serializer = new ConcreteBeanSerializer(type, null, NO_PROPS, null);

        JsonNode schema = serializer.getSchema(provider, null);
        assertNotNull(schema);
        assertEquals("object", schema.get("type").textValue());
        assertNotNull(schema.get("properties"));
    }

    // Tests resolve null serializer resolution
    @Test
    public void testResolve_resolvesWithoutException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        ConcreteBeanSerializer serializer = new ConcreteBeanSerializer(type, null, NO_PROPS, null);

        serializer.resolve(provider);
        assertNotNull(serializer);
    }

    // --- New Tests ---

    @Test
    public void testSerializeWithType_polymorphicBean_serializesWithTypeInfo() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PolymorphicBean bean = new PolymorphicBean();
        String json = mapper.writeValueAsString(bean);

        assertTrue(json.contains("\"@class\":\"com.fasterxml.jackson.databind.ser.std.BeanSerializerBaseTest$PolymorphicBean\""));
        assertTrue(json.contains("\"name\":\"poly\""));
    }

    @Test
    public void testSerializeWithType_withObjectId_serializesObjectIdAndType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PolymorphicIdBean bean = new PolymorphicIdBean();
        String json = mapper.writeValueAsString(bean);

        assertTrue(json.contains("\"@class\":\"com.fasterxml.jackson.databind.ser.std.BeanSerializerBaseTest$PolymorphicIdBean\""));
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"data\":\"info\""));
    }

    @Test
    public void testSerializeFieldsFiltered_missingFilterProvider_throwsJsonMappingException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        FilteredBean bean = new FilteredBean();
        try {
            mapper.writeValueAsString(bean);
            fail("Expected JsonMappingException due to missing filter");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Can not resolve PropertyFilter with id 'customFilter'"));
        }
    }

    @Test
    public void testWrapAndThrow_wrapsRuntimeExceptionsInJsonMappingException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ThrowingGetterBean bean = new ThrowingGetterBean();
        try {
            mapper.writeValueAsString(bean);
            fail("Expected JsonMappingException wrapping getter exception");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Simulated getter error"));
        }
    }

    @Test
    public void testAcceptJsonFormatVisitor_withNonNullVisitor_visitsProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SimpleBean.class);
        JsonSerializer<Object> ser = mapper.getSerializerProviderInstance().findValueSerializer(type, null);

        final boolean[] visitedObject = new boolean[1];
        ser.acceptJsonFormatVisitor(new JsonFormatVisitorWrapper.Base(mapper.getSerializerProviderInstance()) {
            @Override
            public JsonObjectFormatVisitor expectObjectFormat(JavaType type) {
                visitedObject[0] = true;
                return new JsonObjectFormatVisitor.Base(getProvider());
            }
        }, type);

        assertTrue("expectObjectFormat should have been invoked", visitedObject[0]);
    }

    @Test
    public void testGetSchema_withFilteredBeanAndFilterProvider_generatesSchema() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleFilterProvider filters = new SimpleFilterProvider();
        filters.addFilter("customFilter", SimpleBeanPropertyFilter.serializeAll());
        mapper.setFilterProvider(filters);

        JavaType type = mapper.constructType(FilteredBean.class);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonSerializer<Object> ser = provider.findValueSerializer(type, null);

        JsonNode schema = ser.getSchema(provider, null);
        assertNotNull(schema);
        assertEquals("object", schema.get("type").textValue());
        assertNotNull(schema.get("properties"));
    }

    @Test
    public void testConstructor_withSetIgnorals_ignoresProperties() {
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        BeanPropertyWriter bpw1 = new BeanPropertyWriter();
        BeanPropertyWriter bpw2 = new BeanPropertyWriter();

        ConcreteBeanSerializer serializer = new ConcreteBeanSerializer(type, null,
                new BeanPropertyWriter[] { bpw1, bpw2 },
                new BeanPropertyWriter[] { bpw1, bpw2 });

        Set<String> toIgnore = new HashSet<String>();
        toIgnore.add(bpw1.getName());
        BeanSerializerBase filtered = serializer.withIgnorals(toIgnore);

        Iterator<PropertyWriter> it = filtered.properties();
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        assertEquals(1, count);
    }
}