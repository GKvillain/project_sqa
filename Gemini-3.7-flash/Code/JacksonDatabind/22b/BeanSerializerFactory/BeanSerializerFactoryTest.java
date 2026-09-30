package com.fasterxml.jackson.databind.ser;

import java.util.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class BeanSerializerFactoryTest {

    private BeanSerializerFactory factory;
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        factory = BeanSerializerFactory.instance;
        mapper = new ObjectMapper();
    }

    private DefaultSerializerProvider createSerializerProvider() {
        DefaultSerializerProvider.Impl prov = new DefaultSerializerProvider.Impl();
        return prov.createInstance(mapper.getSerializationConfig(), factory);
    }

    // Helper classes for testing
    static class SimpleBean {
        public String name = "test";
        public int value = 42;
    }

    static class EmptyBean {
    }

    @JsonIgnoreProperties({ "ignored" })
    static class IgnoredPropBean {
        public String keep = "kept";
        public String ignored = "dropped";
    }

    static class ViewA {}
    static class ViewB {}

    static class ViewBean {
        @JsonView(ViewA.class)
        public String viewAProp = "a";

        @JsonView(ViewB.class)
        public String viewBProp = "b";

        public String defaultProp = "default";
    }

    static class AnyGetterBean {
        private final Map<String, Object> map = new HashMap<String, Object>();

        public AnyGetterBean() {
            map.put("key1", "val1");
        }

        @JsonAnyGetter
        public Map<String, Object> getAny() {
            return map;
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class PropertyIdBean {
        public int id = 123;
        public String name = "idTest";
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    static class IntSequenceIdBean {
        public String name = "seqTest";
    }

    @JsonSerialize(converter = TestConverter.class)
    static class ConvertedBean {
        public String value;
        public ConvertedBean(String v) { this.value = v; }
    }

    static class TestConverter extends StdConverter<ConvertedBean, String> {
        @Override
        public String convert(ConvertedBean value) {
            return value.value == null ? null : value.value.toUpperCase();
        }
    }

    static class SetterlessBean {
        private String hidden = "hidden";
        public String getHidden() { return hidden; }
    }

    static class PolymorphicContainerBean {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
        public List<Object> items = new ArrayList<Object>();
    }

    static class SubtypeFactory extends BeanSerializerFactory {
        public SubtypeFactory(SerializerFactoryConfig config) {
            super(config);
        }
    }

    @JsonIgnoreType
    static class IgnoredType {
        public String value = "ignoreMe";
    }

    static class BeanWithIgnoredTypeProp {
        public String name = "valid";
        public IgnoredType ignoredProp = new IgnoredType();
    }

    @JsonPropertyOrder({ "b", "a", "c" })
    static class OrderedBean {
        public String a = "1";
        public String b = "2";
        public String c = "3";
    }

    @JsonFilter("testFilter")
    static class FilteredBean {
        public String prop1 = "val1";
        public String prop2 = "val2";
    }

    static class CustomModifier extends BeanSerializerModifier {
        private boolean modifyCalled = false;

        @Override
        public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
            modifyCalled = true;
            return serializer;
        }
    }

    // Tests withConfig with identical config returns this
    @Test
    public void testWithConfig_sameConfig_returnsSameInstance() {
        SerializerFactoryConfig config = new SerializerFactoryConfig();
        BeanSerializerFactory customFactory = new BeanSerializerFactory(config);
        assertSame(customFactory, customFactory.withConfig(config));
    }

    // Tests withConfig with new config returns new factory instance
    @Test
    public void testWithConfig_newConfig_returnsNewInstance() {
        SerializerFactoryConfig config1 = new SerializerFactoryConfig();
        SerializerFactoryConfig config2 = new SerializerFactoryConfig();
        BeanSerializerFactory factory1 = new BeanSerializerFactory(config1);
        SerializerFactory factory2 = factory1.withConfig(config2);

        assertNotNull(factory2);
        assertNotSame(factory1, factory2);
        assertTrue(factory2 instanceof BeanSerializerFactory);
    }

    // Tests withConfig throws IllegalStateException when subclass does not properly override it
    @Test(expected = IllegalStateException.class)
    public void testWithConfig_subclassWithoutOverride_throwsException() {
        SubtypeFactory subtype = new SubtypeFactory(new SerializerFactoryConfig());
        subtype.withConfig(new SerializerFactoryConfig());
    }

    // Tests createSerializer for standard Java POJO
    @Test
    public void testCreateSerializer_standardBean_returnsJsonSerializer() throws Exception {
        SerializerProvider prov = createSerializerProvider();
        JavaType type = mapper.constructType(SimpleBean.class);

        JsonSerializer<Object> ser = factory.createSerializer(prov, type);
        assertNotNull(ser);
        assertTrue(ser instanceof BeanSerializer || ser.getClass().getName().contains("BeanSerializer"));
    }

    // Tests createSerializer for plain Object.class
    @Test
    public void testCreateSerializer_plainObject_returnsUnknownOrNullSerializer() throws Exception {
        SerializerProvider prov = createSerializerProvider();
        JavaType type = mapper.constructType(Object.class);

        JsonSerializer<Object> ser = factory.createSerializer(prov, type);
        assertNotNull(ser);
    }

    // Tests serialization with @JsonSerialize converter
    @Test
    public void testCreateSerializer_withConverter_serializesThroughConverter() throws Exception {
        ConvertedBean bean = new ConvertedBean("hello");
        String json = mapper.writeValueAsString(bean);
        assertEquals("\"HELLO\"", json);
    }

    // Tests findBeanSerializer returns null for primitive/int type
    @Test
    public void testFindBeanSerializer_primitiveType_returnsNull() throws Exception {
        SerializerProvider prov = createSerializerProvider();
        JavaType type = mapper.constructType(int.class);
        BeanDescription desc = mapper.getSerializationConfig().introspect(type);

        JsonSerializer<Object> ser = factory.findBeanSerializer(prov, type, desc);
        assertNull(ser);
    }

    // Tests isPotentialBeanType for enums and primitives
    @Test
    public void testIsPotentialBeanType_enumsAndPrimitives_handledCorrectly() {
        assertTrue(factory.isPotentialBeanType(SimpleBean.class));
        assertFalse(factory.isPotentialBeanType(int.class));
        assertFalse(factory.isPotentialBeanType(String[].class));
    }

    // Tests serialization with @JsonIgnoreProperties filter
    @Test
    public void testFilterBeanProperties_ignoredProperties_removesIgnored() throws Exception {
        IgnoredPropBean bean = new IgnoredPropBean();
        String json = mapper.writeValueAsString(bean);

        assertTrue(json.contains("\"keep\":\"kept\""));
        assertFalse(json.contains("ignored"));
    }

    // Tests view processing with default view inclusion
    @Test
    public void testProcessViews_withDefaultViewInclusion_includesDefault() throws Exception {
        ViewBean bean = new ViewBean();
        String json = mapper.writerWithView(ViewA.class).writeValueAsString(bean);

        assertTrue(json.contains("\"viewAProp\":\"a\""));
        assertFalse(json.contains("viewBProp"));
        assertTrue(json.contains("\"defaultProp\":\"default\""));
    }

    // Tests view processing without default view inclusion
    @Test
    public void testProcessViews_withoutDefaultViewInclusion_excludesDefault() throws Exception {
        ObjectMapper viewMapper = new ObjectMapper();
        viewMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);

        ViewBean bean = new ViewBean();
        String json = viewMapper.writerWithView(ViewA.class).writeValueAsString(bean);

        assertTrue(json.contains("\"viewAProp\":\"a\""));
        assertFalse(json.contains("viewBProp"));
        assertFalse(json.contains("defaultProp"));
    }

    // Tests @JsonAnyGetter property serialization
    @Test
    public void testConstructBeanSerializer_withAnyGetter_serializesMapEntries() throws Exception {
        AnyGetterBean bean = new AnyGetterBean();
        String json = mapper.writeValueAsString(bean);

        assertTrue(json.contains("\"key1\":\"val1\""));
    }

    // Tests PropertyGenerator object ID serialization
    @Test
    public void testConstructObjectIdHandler_propertyGenerator_serializesId() throws Exception {
        PropertyIdBean bean = new PropertyIdBean();
        String json = mapper.writeValueAsString(bean);

        assertTrue(json.contains("\"id\":123"));
        assertTrue(json.contains("\"name\":\"idTest\""));
    }

    // Tests IntSequenceGenerator object ID serialization
    @Test
    public void testConstructObjectIdHandler_sequenceGenerator_serializesObjectId() throws Exception {
        IntSequenceIdBean bean = new IntSequenceIdBean();
        String json = mapper.writeValueAsString(bean);

        assertTrue(json.contains("\"@id\":1"));
        assertTrue(json.contains("\"name\":\"seqTest\""));
    }

    // Tests setterless getter removal when REQUIRE_SETTERS_FOR_GETTERS is enabled
    @Test
    public void testRemoveSetterlessGetters_enabled_suppressesGetter() throws Exception {
        ObjectMapper reqMapper = new ObjectMapper();
        reqMapper.enable(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS);

        SetterlessBean bean = new SetterlessBean();
        String json = reqMapper.writeValueAsString(bean);

        assertEquals("{}", json);
    }

    // Tests setterless getter preserved when REQUIRE_SETTERS_FOR_GETTERS is disabled
    @Test
    public void testRemoveSetterlessGetters_disabled_includesGetter() throws Exception {
        SetterlessBean bean = new SetterlessBean();
        String json = mapper.writeValueAsString(bean);

        assertTrue(json.contains("\"hidden\":\"hidden\""));
    }

    // Tests findPropertyContentTypeSerializer for polymorphic container property
    @Test
    public void testFindPropertyContentTypeSerializer_polymorphicContainer_serializesProperly() throws Exception {
        PolymorphicContainerBean bean = new PolymorphicContainerBean();
        bean.items.add("item1");
        String json = mapper.writeValueAsString(bean);

        assertNotNull(json);
        assertTrue(json.contains("items"));
    }

    // Tests empty bean serialization handling
    @Test
    public void testCreateSerializer_emptyBean_handlesEmptyOrDisabledFailure() throws Exception {
        ObjectMapper emptyMapper = new ObjectMapper();
        emptyMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        EmptyBean bean = new EmptyBean();
        String json = emptyMapper.writeValueAsString(bean);

        assertEquals("{}", json);
    }

    // Tests removal of properties whose type is annotated with @JsonIgnoreType
    @Test
    public void testRemoveIgnorableTypes_annotatedType_excludesProperty() throws Exception {
        BeanWithIgnoredTypeProp bean = new BeanWithIgnoredTypeProp();
        String json = mapper.writeValueAsString(bean);

        assertTrue(json.contains("\"name\":\"valid\""));
        assertFalse(json.contains("ignoredProp"));
        assertFalse(json.contains("ignoreMe"));
    }

    // Tests serializer modifier registration and execution
    @Test
    public void testWithSerializerModifier_modifierInvoked() throws Exception {
        CustomModifier modifier = new CustomModifier();
        SerializerFactoryConfig config = new SerializerFactoryConfig().withSerializerModifier(modifier);
        BeanSerializerFactory customFactory = new BeanSerializerFactory(config);

        ObjectMapper customMapper = new ObjectMapper();
        customMapper.setSerializerFactory(customFactory);

        SimpleBean bean = new SimpleBean();
        String json = customMapper.writeValueAsString(bean);

        assertTrue(modifier.modifyCalled);
        assertTrue(json.contains("\"name\":\"test\""));
    }

    // Tests constructBeanSerializerBuilder creates builder for bean description
    @Test
    public void testConstructBeanSerializerBuilder_createsBuilder() {
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = mapper.getSerializationConfig().introspect(type);
        BeanSerializerBuilder builder = factory.constructBeanSerializerBuilder(desc);

        assertNotNull(builder);
        assertEquals(desc, builder.getBeanDescription());
    }

    // Tests constructPropertyBuilder creates property builder
    @Test
    public void testConstructPropertyBuilder_createsPropertyBuilder() {
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = mapper.getSerializationConfig().introspect(type);
        PropertyBuilder propBuilder = factory.constructPropertyBuilder(mapper.getSerializationConfig(), desc);

        assertNotNull(propBuilder);
    }

    // Tests property ordering configured via @JsonPropertyOrder
    @Test
    public void testFindBeanProperties_propertyOrderApplied() throws Exception {
        OrderedBean bean = new OrderedBean();
        String json = mapper.writeValueAsString(bean);

        int posB = json.indexOf("\"b\"");
        int posA = json.indexOf("\"a\"");
        int posC = json.indexOf("\"c\"");

        assertTrue(posB < posA);
        assertTrue(posA < posC);
    }

    // Tests filterId attached properly with @JsonFilter
    @Test
    public void testConstructBeanSerializer_withFilterId_appliesFilter() throws Exception {
        SimpleFilterProvider filterProvider = new SimpleFilterProvider()
                .addFilter("testFilter", SimpleBeanPropertyFilter.filterOutAllExcept("prop1"));

        ObjectMapper filterMapper = new ObjectMapper();
        filterMapper.setFilterProvider(filterProvider);

        FilteredBean bean = new FilteredBean();
        String json = filterMapper.writeValueAsString(bean);

        assertTrue(json.contains("\"prop1\":\"val1\""));
        assertFalse(json.contains("prop2"));
    }

    // Tests customSerializers iterable returns configured serializers
    @Test
    public void testCustomSerializers_returnsConfiguredSerializers() {
        Serializers customSers = new Serializers.Base();
        SerializerFactoryConfig config = new SerializerFactoryConfig().withAdditionalSerializers(customSers);
        BeanSerializerFactory customFactory = new BeanSerializerFactory(config);

        Iterable<Serializers> it = customFactory.customSerializers();
        assertNotNull(it);
        boolean found = false;
        for (Serializers s : it) {
            if (s == customSers) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }
}