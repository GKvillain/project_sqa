package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsonschema.SchemaAware;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.NameTransformer;

public class BeanSerializerBaseTest {

    // --- Helper POJOs for testing ---

    static class Views {
        interface ViewA {}
        interface ViewB {}
    }

    @JsonPropertyOrder({ "id", "name" })
    static class SimpleBean {
        public int id;
        public String name;

        public SimpleBean(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @JsonPropertyOrder({ "id", "secret" })
    static class ViewBean {
        @JsonView(Views.ViewA.class)
        public int id = 1;

        @JsonView(Views.ViewB.class)
        public String secret = "hidden";
    }

    @JsonFilter("customFilter")
    static class FilteredBean {
        public String keep = "keep";
        public String drop = "drop";
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder({ "x", "y" })
    static class ArrayShapeBean {
        public int x;
        public int y;

        public ArrayShapeBean(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonPropertyOrder({ "id", "name", "next" })
    static class SelfRefBean {
        public int id;
        public String name;
        public SelfRefBean next;

        public SelfRefBean(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    static class IdBean {
        public String name;

        public IdBean(String name) {
            this.name = name;
        }
    }

    static class ContainerOfIdBean {
        @JsonIdentityReference(alwaysAsId = true)
        public IdBean item;

        public ContainerOfIdBean(IdBean item) {
            this.item = item;
        }
    }

    @JsonIgnoreProperties({ "ignoredProp" })
    static class IgnoralBean {
        public String keepProp = "yes";
        public String ignoredProp = "no";
    }

    static class AnyGetterBean {
        public int id = 100;
        private Map<String, Object> extra = new HashMap<String, Object>();

        public void add(String k, Object v) {
            extra.put(k, v);
        }

        @JsonAnyGetter
        public Map<String, Object> any() {
            return extra;
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonTypeName("poly")
    static class PolyBean {
        public String value = "hello";
    }

    static class UnwrappedParent {
        public int id = 1;
        @JsonUnwrapped(prefix = "child_")
        public SimpleBean child = new SimpleBean(2, "sub");
    }

    // Concrete implementation of BeanSerializerBase for direct testing
    static class DummyBeanSerializer extends BeanSerializerBase {
        public DummyBeanSerializer(JavaType type, BeanPropertyWriter[] props) {
            super(type, (BeanSerializerBuilder) null, props, null);
        }

        public DummyBeanSerializer(DummyBeanSerializer src, ObjectIdWriter oiw) {
            super(src, oiw);
        }

        public DummyBeanSerializer(DummyBeanSerializer src, String[] toIgnore) {
            super(src, toIgnore);
        }

        public DummyBeanSerializer(DummyBeanSerializer src, Object filterId) {
            super(src, src._objectIdWriter, filterId);
        }

        @Override
        public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
            return new DummyBeanSerializer(this, objectIdWriter);
        }

        @Override
        protected BeanSerializerBase withIgnorals(String[] toIgnore) {
            return new DummyBeanSerializer(this, toIgnore);
        }

        @Override
        protected BeanSerializerBase asArraySerializer() {
            return this;
        }

        @Override
        public BeanSerializerBase withFilterId(Object filterId) {
            return new DummyBeanSerializer(this, filterId);
        }

        @Override
        public void serialize(Object bean, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            serializeFields(bean, gen, provider);
            gen.writeEndObject();
        }
    }

    // Tests normal property serialization of a basic POJO
    @Test
    public void testSerialize_simpleBean_producesExpectedJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean bean = new SimpleBean(42, "test");
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"id\":42,\"name\":\"test\"}", json);
    }

    // Tests array shape transformation via @JsonFormat(shape = Shape.ARRAY)
    @Test
    public void testSerialize_arrayShape_producesJsonArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ArrayShapeBean bean = new ArrayShapeBean(10, 20);
        String json = mapper.writeValueAsString(bean);
        assertEquals("[10,20]", json);
    }

    // Tests property filtering using @JsonFilter
    @Test
    public void testSerialize_withFilter_excludesFilteredProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleFilterProvider filters = new SimpleFilterProvider();
        filters.addFilter("customFilter", SimpleBeanPropertyFilter.filterOutAllExcept("keep"));
        mapper.setFilterProvider(filters);

        FilteredBean bean = new FilteredBean();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"keep\":\"keep\"}", json);
    }

    // Tests active view filtering with @JsonView
    @Test
    public void testSerialize_withActiveView_includesOnlyViewProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ViewBean bean = new ViewBean();
        String json = mapper.writerWithView(Views.ViewA.class).writeValueAsString(bean);
        assertEquals("{\"id\":1}", json);
    }

    // Tests cyclic reference handling with property-based Object Id
    @Test
    public void testSerialize_propertyBasedObjectId_resolvesCyclicReference() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SelfRefBean b1 = new SelfRefBean(1, "first");
        SelfRefBean b2 = new SelfRefBean(2, "second");
        b1.next = b2;
        b2.next = b1;

        String json = mapper.writeValueAsString(b1);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"next\":{\"id\":2,\"name\":\"second\",\"next\":1}"));
    }

    // Tests @JsonIdentityReference(alwaysAsId=true) on referencing property
    @Test
    public void testSerialize_alwaysAsId_outputsOnlyId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        IdBean idBean = new IdBean("item1");
        ContainerOfIdBean container = new ContainerOfIdBean(idBean);

        String json = mapper.writeValueAsString(container);
        assertEquals("{\"item\":1}", json);
    }

    // Tests @JsonIgnoreProperties excluding specified fields
    @Test
    public void testSerialize_withIgnorals_ignoresExcludedFields() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        IgnoralBean bean = new IgnoralBean();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"keepProp\":\"yes\"}", json);
    }

    // Tests @JsonAnyGetter serialization
    @Test
    public void testSerialize_anyGetter_serializesDynamicFields() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnyGetterBean bean = new AnyGetterBean();
        bean.add("dynKey", "dynVal");

        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"id\":100"));
        assertTrue(json.contains("\"dynKey\":\"dynVal\""));
    }

    // Tests polymorphic type info serialization with type prefix and suffix
    @Test
    public void testSerialize_polyBeanWithTypeInfo_includesTypeProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PolyBean bean = new PolyBean();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"type\":\"poly\""));
        assertTrue(json.contains("\"value\":\"hello\""));
    }

    // Tests JSON unwrapped serialization via NameTransformer
    @Test
    public void testSerialize_unwrappedChild_prefixesPropertyNames() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        UnwrappedParent parent = new UnwrappedParent();
        String json = mapper.writeValueAsString(parent);
        assertEquals("{\"id\":1,\"child_id\":2,\"child_name\":\"sub\"}", json);
    }

    // Tests SchemaAware getSchema method
    @Test
    public void testGetSchema_simpleBean_returnsObjectSchema() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JavaType type = mapper.constructType(SimpleBean.class);
        JsonSerializer<Object> ser = provider.findValueSerializer(type, null);

        assertTrue(ser instanceof SchemaAware);
        JsonNode schemaNode = ((SchemaAware) ser).getSchema(provider, null);
        assertNotNull(schemaNode);
        assertEquals("object", schemaNode.get("type").asText());
        assertNotNull(schemaNode.get("properties"));
        assertTrue(schemaNode.get("properties").has("id"));
        assertTrue(schemaNode.get("properties").has("name"));
    }

    // Tests format visitor acceptJsonFormatVisitor method
    @Test
    public void testAcceptJsonFormatVisitor_normalVisitor_visitsObjectProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SimpleBean.class);
        final List<String> visitedProps = new ArrayList<String>();

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(mapper.getSerializerProviderInstance()) {
            @Override
            public JsonObjectFormatVisitor expectObjectFormat(JavaType visitedType) {
                return new JsonObjectFormatVisitor.Base(getProvider()) {
                    @Override
                    public void property(BeanProperty prop) {
                        visitedProps.add(prop.getName());
                    }
                };
            }
        };

        mapper.acceptJsonFormatVisitor(SimpleBean.class, visitor);
        assertTrue(visitedProps.contains("id"));
        assertTrue(visitedProps.contains("name"));
    }

    // Tests acceptJsonFormatVisitor with null visitor gracefully returning
    @Test
    public void testAcceptJsonFormatVisitor_nullVisitor_doesNotThrow() throws Exception {
        DummyBeanSerializer ser = new DummyBeanSerializer(
                TypeFactory.defaultInstance().constructType(SimpleBean.class),
                BeanSerializerBase.NO_PROPS
        );
        ser.acceptJsonFormatVisitor(null, null);
    }

    // Tests direct helper methods of BeanSerializerBase: properties() and usesObjectId()
    @Test
    public void testPropertiesAndUsesObjectId_withoutObjectIdWriter_returnsExpectedValues() {
        DummyBeanSerializer ser = new DummyBeanSerializer(
                TypeFactory.defaultInstance().constructType(SimpleBean.class),
                BeanSerializerBase.NO_PROPS
        );
        assertFalse(ser.usesObjectId());
        Iterator<PropertyWriter> props = ser.properties();
        assertNotNull(props);
        assertFalse(props.hasNext());
    }

    // Tests mutant factories withFilterId and withIgnorals
    @Test
    public void testMutantFactories_withFilterIdAndIgnorals_createsNewInstances() {
        DummyBeanSerializer ser = new DummyBeanSerializer(
                TypeFactory.defaultInstance().constructType(SimpleBean.class),
                BeanSerializerBase.NO_PROPS
        );

        BeanSerializerBase withFilter = ser.withFilterId("filter123");
        assertNotNull(withFilter);

        BeanSerializerBase withIgn = ser.withIgnorals(new String[] { "id" });
        assertNotNull(withIgn);
    }

    // Tests exception handling in serializeFields unwrapping or throwing JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testSerialize_throwingGetter_throwsJsonMappingException() throws Exception {
        class FaultyBean {
            public String getFailing() {
                throw new RuntimeException("Simulated error");
            }
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValueAsString(new FaultyBean());
    }
}