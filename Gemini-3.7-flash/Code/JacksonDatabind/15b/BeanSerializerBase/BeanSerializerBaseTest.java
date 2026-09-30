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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.fasterxml.jackson.databind.util.StdConverter;

public class BeanSerializerBaseTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // Helper classes for testing various BeanSerializerBase features

    static class SimpleBean {
        public String name;
        public int value;

        public SimpleBean(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder({ "name", "value" })
    static class ArrayShapeBean {
        public String name;
        public int value;

        public ArrayShapeBean(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    @JsonFilter("testFilter")
    static class FilteredBean {
        public String keep;
        public String omit;

        public FilteredBean(String keep, String omit) {
            this.keep = keep;
            this.omit = omit;
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class IdentifiedBean {
        public int id;
        public String name;
        public IdentifiedBean next;

        public IdentifiedBean(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    static class IntIdBean {
        public String data;
        public IntIdBean child;

        public IntIdBean(String data) {
            this.data = data;
        }
    }

    static class AnyGetterBean {
        public String id;
        private Map<String, Object> extra = new HashMap<String, Object>();

        public AnyGetterBean(String id) {
            this.id = id;
        }

        public void add(String key, Object value) {
            extra.put(key, value);
        }

        @JsonAnyGetter
        public Map<String, Object> getExtra() {
            return extra;
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    static class PolymorphicBean {
        public String val;

        public PolymorphicBean(String val) {
            this.val = val;
        }
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public enum ObjectFormatEnum {
        A("alpha", 1),
        B("beta", 2);

        public final String label;
        public final int code;

        ObjectFormatEnum(String label, int code) {
            this.label = label;
            this.code = code;
        }
    }

    static class EnumWrapper {
        public ObjectFormatEnum regular = ObjectFormatEnum.A;

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public ObjectFormatEnum asString = ObjectFormatEnum.B;

        @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
        public ObjectFormatEnum asNumber = ObjectFormatEnum.A;
    }

    static class ConvertedBean {
        @com.fasterxml.jackson.databind.annotation.JsonSerialize(converter = StringUpperConverter.class)
        public String text;

        public ConvertedBean(String text) {
            this.text = text;
        }
    }

    static class StringUpperConverter extends StdConverter<String, String> {
        @Override
        public String convert(String value) {
            return (value == null) ? null : value.toUpperCase();
        }
    }

    static class ContainerBean {
        public List<String> items = Arrays.asList("x", "y");
    }

    static class IgnoreOverrideWrapper {
        @JsonIgnoreProperties({ "value" })
        public SimpleBean bean = new SimpleBean("test", 123);
    }

    interface ViewA {}
    interface ViewB {}

    static class ViewBean {
        @JsonView(ViewA.class)
        public String propA = "A";

        @JsonView(ViewB.class)
        public String propB = "B";
    }

    static class UnwrappedParent {
        public String parentProp = "p";

        @JsonUnwrapped(prefix = "child_")
        public SimpleBean child = new SimpleBean("nested", 99);
    }

    static class FailingGetterBean {
        public String getGood() {
            return "ok";
        }

        public String getFailing() {
            throw new IllegalStateException("Simulated getter failure");
        }
    }

    static class TestBeanSerializer extends BeanSerializerBase {
        public TestBeanSerializer(JavaType type, BeanSerializerBuilder builder,
                BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
            super(type, builder, properties, filteredProperties);
        }

        public TestBeanSerializer(BeanSerializerBase src) {
            super(src);
        }

        public TestBeanSerializer(BeanSerializerBase src, ObjectIdWriter objectIdWriter) {
            super(src, objectIdWriter);
        }

        public TestBeanSerializer(BeanSerializerBase src, ObjectIdWriter objectIdWriter, Object filterId) {
            super(src, objectIdWriter, filterId);
        }

        public TestBeanSerializer(BeanSerializerBase src, String[] toIgnore) {
            super(src, toIgnore);
        }

        public TestBeanSerializer(BeanSerializerBase src, NameTransformer unwrapper) {
            super(src, unwrapper);
        }

        @Override
        public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
            return new TestBeanSerializer(this, objectIdWriter);
        }

        @Override
        public BeanSerializerBase withIgnorals(String[] toIgnore) {
            return new TestBeanSerializer(this, toIgnore);
        }

        @Override
        public BeanSerializerBase asArraySerializer() {
            return this;
        }

        @Override
        public BeanSerializerBase withFilterId(Object filterId) {
            return new TestBeanSerializer(this, _objectIdWriter, filterId);
        }

        @Override
        public void serialize(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStartObject();
            serializeFields(bean, jgen, provider);
            jgen.writeEndObject();
        }
    }

    // Tests standard POJO serialization through BeanSerializerBase
    @Test
    public void testSerialize_standardBean_producesExpectedJson() throws Exception {
        SimpleBean bean = new SimpleBean("Bob", 42);
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"name\":\"Bob\""));
        assertTrue(json.contains("\"value\":42"));
    }

    // Tests serialization shape modification to array
    @Test
    public void testSerialize_arrayShape_producesJsonArray() throws Exception {
        ArrayShapeBean bean = new ArrayShapeBean("Alice", 100);
        String json = mapper.writeValueAsString(bean);
        assertEquals("[\"Alice\",100]", json);
    }

    // Tests filtering functionality using JsonFilter
    @Test
    public void testSerialize_withPropertyFilter_omitsFilteredProperties() throws Exception {
        ObjectMapper filterMapper = new ObjectMapper();
        FilterProvider filters = new SimpleFilterProvider().addFilter(
                "testFilter", SimpleBeanPropertyFilter.filterOutAllExcept("keep"));
        filterMapper.setFilters(filters);

        FilteredBean bean = new FilteredBean("keepMe", "omitMe");
        String json = filterMapper.writeValueAsString(bean);
        assertTrue(json.contains("\"keep\":\"keepMe\""));
        assertFalse(json.contains("omitMe"));
    }

    // Tests property-based ObjectId handling
    @Test
    public void testSerialize_propertyBasedObjectId_handlesCycles() throws Exception {
        IdentifiedBean b1 = new IdentifiedBean(1, "first");
        IdentifiedBean b2 = new IdentifiedBean(2, "second");
        b1.next = b2;
        b2.next = b1;

        String json = mapper.writeValueAsString(b1);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"first\""));
        assertTrue(json.contains("\"next\":{\"id\":2,\"name\":\"second\",\"next\":1}"));
    }

    // Tests generator-based ObjectId serialization
    @Test
    public void testSerialize_generatorObjectId_writesIds() throws Exception {
        IntIdBean parent = new IntIdBean("parent");
        IntIdBean child = new IntIdBean("child");
        parent.child = child;
        child.child = parent;

        String json = mapper.writeValueAsString(parent);
        assertTrue(json.contains("\"@id\":1"));
        assertTrue(json.contains("\"@id\":2"));
    }

    // Tests any-getter handling
    @Test
    public void testSerialize_anyGetter_includesDynamicProperties() throws Exception {
        AnyGetterBean bean = new AnyGetterBean("123");
        bean.add("attr1", "val1");
        bean.add("attr2", 99);

        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"id\":\"123\""));
        assertTrue(json.contains("\"attr1\":\"val1\""));
        assertTrue(json.contains("\"attr2\":99"));
    }

    // Tests polymorphic serialization with TypeSerializer
    @Test
    public void testSerializeWithType_polymorphicBean_includesType() throws Exception {
        PolymorphicBean bean = new PolymorphicBean("data");
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"type\":\"BeanSerializerBaseTest$PolymorphicBean\"")
                || json.contains("\"type\":"));
        assertTrue(json.contains("\"val\":\"data\""));
    }

    // Tests enum serialization shape reverting from OBJECT to STRING / NUMBER
    @Test
    public void testCreateContextual_enumShapeOverride_revertsToStringAndNumber() throws Exception {
        EnumWrapper wrapper = new EnumWrapper();
        String json = mapper.writeValueAsString(wrapper);

        // regular enum should serialize as object
        assertTrue(json.contains("\"regular\":{\"label\":\"alpha\",\"code\":1}"));
        // overridden with STRING should serialize as string
        assertTrue(json.contains("\"asString\":\"B\""));
        // overridden with NUMBER_INT should serialize as ordinal
        assertTrue(json.contains("\"asNumber\":0"));
    }

    // Tests custom converter resolving
    @Test
    public void testResolve_customConverter_appliesConversion() throws Exception {
        ConvertedBean bean = new ConvertedBean("lowercase");
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"text\":\"LOWERCASE\"}", json);
    }

    // Tests contextual property ignore override
    @Test
    public void testCreateContextual_propertyIgnorals_ignoresSpecifiedProperty() throws Exception {
        IgnoreOverrideWrapper wrapper = new IgnoreOverrideWrapper();
        String json = mapper.writeValueAsString(wrapper);
        assertTrue(json.contains("\"name\":\"test\""));
        assertFalse(json.contains("\"value\":"));
    }

    // Tests usesObjectId method
    @Test
    public void testUsesObjectId_withAndWithoutObjectIdWriter() {
        JavaType type = mapper.constructType(SimpleBean.class);
        TestBeanSerializer serWithout = new TestBeanSerializer(type, null,
                new BeanPropertyWriter[0], null);
        assertFalse(serWithout.usesObjectId());
    }

    // Tests withIgnorals constructor filtering
    @Test
    public void testConstructor_withIgnorals_filtersProps() throws Exception {
        JavaType type = mapper.constructType(SimpleBean.class);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        
        JsonSerializer<Object> baseSer = provider.findValueSerializer(SimpleBean.class, null);
        assertTrue(baseSer instanceof BeanSerializerBase);
        BeanSerializerBase bsb = (BeanSerializerBase) baseSer;

        TestBeanSerializer custom = new TestBeanSerializer(bsb);
        BeanSerializerBase ignored = custom.withIgnorals(new String[] { "value" });

        StringWriter sw = new StringWriter();
        JsonGenerator g = mapper.getFactory().createGenerator(sw);
        ignored.serialize(new SimpleBean("John", 50), g, provider);
        g.close();

        String result = sw.toString();
        assertTrue(result.contains("\"name\":\"John\""));
        assertFalse(result.contains("50"));
    }

    // Tests schema generation via getSchema
    @SuppressWarnings("deprecation")
    @Test
    public void testGetSchema_producesObjectSchemaWithProperties() throws Exception {
        JavaType type = mapper.constructType(SimpleBean.class);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonSerializer<Object> ser = provider.findValueSerializer(SimpleBean.class, null);

        assertTrue(ser instanceof BeanSerializerBase);
        BeanSerializerBase bsb = (BeanSerializerBase) ser;

        JsonNode schemaNode = bsb.getSchema(provider, type.getRawClass());
        assertNotNull(schemaNode);
        assertEquals("object", schemaNode.get("type").asText());
        assertNotNull(schemaNode.get("properties"));
        assertTrue(schemaNode.get("properties").has("name"));
        assertTrue(schemaNode.get("properties").has("value"));
    }

    // Tests JSON format visitor acceptance
    @Test
    public void testAcceptJsonFormatVisitor_visitsProperties() throws Exception {
        final List<String> visitedProps = new ArrayList<String>();
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonObjectFormatVisitor expectObjectFormat(JavaType type) {
                return new JsonObjectFormatVisitor.Base() {
                    @Override
                    public void property(BeanProperty prop) {
                        visitedProps.add(prop.getName());
                    }
                };
            }
        };

        mapper.acceptJsonFormatVisitor(SimpleBean.class, visitor);
        assertTrue(visitedProps.contains("name"));
        assertTrue(visitedProps.contains("value"));
    }

    // Tests visitor with null object visitor
    @Test
    public void testAcceptJsonFormatVisitor_nullVisitor_returnsWithoutException() throws Exception {
        JavaType type = mapper.constructType(SimpleBean.class);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonSerializer<Object> ser = provider.findValueSerializer(SimpleBean.class, null);

        assertTrue(ser instanceof BeanSerializerBase);
        BeanSerializerBase bsb = (BeanSerializerBase) ser;

        // null visitor
        bsb.acceptJsonFormatVisitor(null, type);

        // visitor returning null object visitor
        JsonFormatVisitorWrapper emptyWrapper = new JsonFormatVisitorWrapper.Base();
        bsb.acceptJsonFormatVisitor(emptyWrapper, type);
    }

    // Tests properties iterator on BeanSerializerBase
    @Test
    public void testProperties_iterator_returnsAllProperties() throws Exception {
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonSerializer<Object> ser = provider.findValueSerializer(SimpleBean.class, null);
        assertTrue(ser instanceof BeanSerializerBase);
        BeanSerializerBase bsb = (BeanSerializerBase) ser;

        Iterator<PropertyWriter> it = bsb.properties();
        assertNotNull(it);
        List<String> names = new ArrayList<String>();
        while (it.hasNext()) {
            names.add(it.next().getName());
        }
        assertEquals(2, names.size());
        assertTrue(names.contains("name"));
        assertTrue(names.contains("value"));
    }

    // Tests view filtering with @JsonView
    @Test
    public void testSerialize_withJsonView_filtersAppropriately() throws Exception {
        ViewBean bean = new ViewBean();

        String jsonA = mapper.writerWithView(ViewA.class).writeValueAsString(bean);
        assertTrue(jsonA.contains("\"propA\":\"A\""));
        assertFalse(jsonA.contains("propB"));

        String jsonB = mapper.writerWithView(ViewB.class).writeValueAsString(bean);
        assertFalse(jsonB.contains("propA"));
        assertTrue(jsonB.contains("\"propB\":\"B\""));
    }

    // Tests unwrapped serialization with @JsonUnwrapped
    @Test
    public void testSerialize_unwrappedProperty_flattensFields() throws Exception {
        UnwrappedParent bean = new UnwrappedParent();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"parentProp\":\"p\""));
        assertTrue(json.contains("\"child_name\":\"nested\""));
        assertTrue(json.contains("\"child_value\":99"));
    }

    // Tests exception handling and wrapAndThrow behavior during field serialization
    @Test
    public void testSerialize_getterThrowsException_wrapsInJsonMappingException() {
        FailingGetterBean bean = new FailingGetterBean();
        try {
            mapper.writeValueAsString(bean);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Simulated getter failure")
                    || (e.getCause() != null && e.getCause().getMessage().contains("Simulated getter failure")));
        } catch (IOException e) {
            fail("Expected JsonMappingException but got IOException: " + e);
        }
    }

    // Tests withFilterId functionality directly on TestBeanSerializer
    @Test
    public void testWithFilterId_createsSerializerWithCustomFilterId() throws Exception {
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonSerializer<Object> ser = provider.findValueSerializer(SimpleBean.class, null);
        assertTrue(ser instanceof BeanSerializerBase);
        BeanSerializerBase bsb = (BeanSerializerBase) ser;

        TestBeanSerializer testSer = new TestBeanSerializer(bsb);
        BeanSerializerBase withFilter = testSer.withFilterId("customFilter");
        assertNotNull(withFilter);

        // Test withFilterId with null (clearing filterId)
        BeanSerializerBase clearedFilter = withFilter.withFilterId(null);
        assertNotNull(clearedFilter);
    }

    // Tests acceptJsonFormatVisitor when Array format is configured
    @Test
    public void testAcceptJsonFormatVisitor_arrayShape_visitsArray() throws Exception {
        final boolean[] arrayVisited = new boolean[1];
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor expectArrayFormat(JavaType type) {
                arrayVisited[0] = true;
                return new com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor.Base();
            }
        };

        mapper.acceptJsonFormatVisitor(ArrayShapeBean.class, visitor);
        assertTrue(arrayVisited[0]);
    }

    // Tests getSchema when filter is missing but expected
    @SuppressWarnings("deprecation")
    @Test
    public void testGetSchema_filteredBeanWithoutFilterProvider_returnsBaseSchema() throws Exception {
        JavaType type = mapper.constructType(FilteredBean.class);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonSerializer<Object> ser = provider.findValueSerializer(FilteredBean.class, null);

        assertTrue(ser instanceof BeanSerializerBase);
        BeanSerializerBase bsb = (BeanSerializerBase) ser;

        JsonNode schema = bsb.getSchema(provider, type.getRawClass());
        assertNotNull(schema);
        assertEquals("object", schema.get("type").asText());
    }
}