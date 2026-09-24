package com.fasterxml.jackson.databind.ser;

import java.util.*;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;

public class BeanSerializerFactoryTest {

    private BeanSerializerFactory factory;
    private ObjectMapper mapper;
    private SerializerProvider serializerProvider;

    // Helper classes for testing
    static class SimpleBean {
        private String name;
        private int age;

        public SimpleBean() {}
        public SimpleBean(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    static class SetterlessBean {
        private final List<String> values = new ArrayList<String>();
        public List<String> getValues() { return values; }
    }

    static class ViewA {}
    static class ViewB {}

    static class ViewBean {
        @JsonView(ViewA.class)
        public String viewAProperty = "A";

        @JsonView(ViewB.class)
        public String viewBProperty = "B";

        public String defaultProperty = "all";
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class PropertyIdBean {
        public int id = 123;
        public String name = "test";
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
    static class SequenceIdBean {
        public String name = "seq";
    }

    static class PolymorphicContainerBean {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
        public Object item;

        public PolymorphicContainerBean(Object item) {
            this.item = item;
        }
    }

    static class ListContainerBean {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
        public List<Object> items;
    }

    static class CustomConverter extends StdConverter<SimpleBean, String> {
        @Override
        public String convert(SimpleBean value) {
            return value.getName() + ":" + value.getAge();
        }
    }

    static class ObjectConverter extends StdConverter<SimpleBean, Object> {
        @Override
        public Object convert(SimpleBean value) {
            return value.getName();
        }
    }

    @com.fasterxml.jackson.databind.annotation.JsonSerialize(converter = CustomConverter.class)
    static class ConvertedBean {
        public String name = "converted";
        public int age = 30;
    }

    @com.fasterxml.jackson.databind.annotation.JsonSerialize(converter = ObjectConverter.class)
    static class ConvertedToObjectBean {
        public String name = "toObject";
        public int age = 40;
    }

    static class SubtypeOfFactory extends BeanSerializerFactory {
        public SubtypeOfFactory(SerializerFactoryConfig config) {
            super(config);
        }
    }

    @Before
    public void setUp() {
        factory = BeanSerializerFactory.instance;
        mapper = new ObjectMapper();
        serializerProvider = new DefaultSerializerProvider.Impl().createInstance(mapper.getSerializationConfig(), factory);
    }

    // Tests singleton instance is not null
    @Test
    public void testInstance_notNull() {
        assertNotNull(BeanSerializerFactory.instance);
    }

    // Tests withConfig when given the same config returns the same instance
    @Test
    public void testWithConfig_sameConfig_returnsThis() {
        SerializerFactoryConfig config = factory.getFactoryConfig();
        SerializerFactory sameFactory = factory.withConfig(config);
        assertSame(factory, sameFactory);
    }

    // Tests withConfig when given a new config returns a new instance
    @Test
    public void testWithConfig_differentConfig_returnsNewInstance() {
        SerializerFactoryConfig newConfig = new SerializerFactoryConfig();
        SerializerFactory newFactory = factory.withConfig(newConfig);
        assertNotNull(newFactory);
        assertNotSame(factory, newFactory);
    }

    // Tests withConfig throws IllegalStateException when subclass does not override withConfig
    @Test(expected = IllegalStateException.class)
    public void testWithConfig_invalidSubclass_throwsIllegalStateException() {
        SubtypeOfFactory subFactory = new SubtypeOfFactory(new SerializerFactoryConfig());
        subFactory.withConfig(new SerializerFactoryConfig());
    }

    // Tests createSerializer for standard bean
    @Test
    public void testCreateSerializer_standardBean_returnsBeanSerializer() throws Exception {
        JavaType type = mapper.constructType(SimpleBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        assertNotNull(ser);
        assertTrue(ser instanceof BeanSerializer || ser instanceof ResolvableSerializer);
    }

    // Tests createSerializer for java.lang.Object returns unknown type serializer
    @Test
    public void testCreateSerializer_plainObject_returnsSerializer() throws Exception {
        JavaType type = mapper.constructType(Object.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        assertNotNull(ser);
    }

    // Tests createSerializer for primitive/non-bean type
    @Test
    public void testCreateSerializer_primitiveType_returnsStandardSerializer() throws Exception {
        JavaType type = mapper.constructType(int.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        assertNotNull(ser);
        assertFalse(ser instanceof BeanSerializer);
    }

    // Tests createSerializer for bean with converter
    @Test
    public void testCreateSerializer_withConverter_returnsDelegatingSerializer() throws Exception {
        JavaType type = mapper.constructType(ConvertedBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        assertNotNull(ser);
        assertTrue(ser instanceof StdDelegatingSerializer);
    }

    // Tests createSerializer for bean with converter targeting Object.class (Issue #731 regression)
    @Test
    public void testCreateSerializer_withConverterReturningObject_returnsDelegatingSerializer() throws Exception {
        JavaType type = mapper.constructType(ConvertedToObjectBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        assertNotNull(ser);
        assertTrue(ser instanceof StdDelegatingSerializer);
    }

    // Tests findBeanSerializer returns null for non-bean primitive class
    @Test
    public void testFindBeanSerializer_primitive_returnsNull() throws Exception {
        JavaType type = mapper.constructType(int.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        JsonSerializer<Object> ser = factory.findBeanSerializer(serializerProvider, type, beanDesc);
        assertNull(ser);
    }

    // Tests findBeanSerializer returns serializer for regular bean
    @Test
    public void testFindBeanSerializer_regularBean_returnsSerializer() throws Exception {
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        JsonSerializer<Object> ser = factory.findBeanSerializer(serializerProvider, type, beanDesc);
        assertNotNull(ser);
    }

    // Tests isPotentialBeanType method with bean class and primitive
    @Test
    public void testIsPotentialBeanType_beanAndPrimitive() {
        assertTrue(factory.isPotentialBeanType(SimpleBean.class));
        assertFalse(factory.isPotentialBeanType(int.class));
        assertFalse(factory.isPotentialBeanType(int[].class));
    }

    // Tests constructObjectIdHandler with PropertyGenerator
    @Test
    public void testConstructObjectIdHandler_propertyGenerator_returnsObjectIdWriter() throws Exception {
        JavaType type = mapper.constructType(PropertyIdBean.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        BeanSerializerBuilder builder = factory.constructBeanSerializerBuilder(beanDesc);
        builder.setConfig(mapper.getSerializationConfig());
        List<BeanPropertyWriter> props = factory.findBeanProperties(serializerProvider, beanDesc, builder);

        ObjectIdWriter writer = factory.constructObjectIdHandler(serializerProvider, beanDesc, props);
        assertNotNull(writer);
        assertEquals("id", props.get(0).getName());
    }

    // Tests constructObjectIdHandler with IntSequenceGenerator
    @Test
    public void testConstructObjectIdHandler_sequenceGenerator_returnsObjectIdWriter() throws Exception {
        JavaType type = mapper.constructType(SequenceIdBean.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        BeanSerializerBuilder builder = factory.constructBeanSerializerBuilder(beanDesc);
        builder.setConfig(mapper.getSerializationConfig());
        List<BeanPropertyWriter> props = factory.findBeanProperties(serializerProvider, beanDesc, builder);

        ObjectIdWriter writer = factory.constructObjectIdHandler(serializerProvider, beanDesc, props);
        assertNotNull(writer);
    }

    // Tests findPropertyTypeSerializer for polymorphic property
    @Test
    public void testFindPropertyTypeSerializer_polymorphicProperty_returnsTypeSerializer() throws Exception {
        JavaType type = mapper.constructType(PolymorphicContainerBean.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        List<BeanPropertyDefinition> props = beanDesc.findProperties();
        AnnotatedMember member = null;
        for (BeanPropertyDefinition prop : props) {
            if ("item".equals(prop.getName())) {
                member = prop.getAccessor();
                break;
            }
        }
        assertNotNull(member);
        JavaType propType = member.getType(beanDesc.bindingsForBeanType());
        assertNotNull(factory.findPropertyTypeSerializer(propType, mapper.getSerializationConfig(), member));
    }

    // Tests findPropertyContentTypeSerializer for container property
    @Test
    public void testFindPropertyContentTypeSerializer_containerProperty_returnsTypeSerializer() throws Exception {
        JavaType type = mapper.constructType(ListContainerBean.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        List<BeanPropertyDefinition> props = beanDesc.findProperties();
        AnnotatedMember member = null;
        for (BeanPropertyDefinition prop : props) {
            if ("items".equals(prop.getName())) {
                member = prop.getAccessor();
                break;
            }
        }
        assertNotNull(member);
        JavaType propType = member.getType(beanDesc.bindingsForBeanType());
        assertNotNull(factory.findPropertyContentTypeSerializer(propType, mapper.getSerializationConfig(), member));
    }

    // Tests processViews with view annotations
    @Test
    public void testProcessViews_beanWithViews_setsFilteredProperties() throws Exception {
        JavaType type = mapper.constructType(ViewBean.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        BeanSerializerBuilder builder = factory.constructBeanSerializerBuilder(beanDesc);
        builder.setConfig(mapper.getSerializationConfig());

        List<BeanPropertyWriter> props = factory.findBeanProperties(serializerProvider, beanDesc, builder);
        builder.setProperties(props);
        factory.processViews(mapper.getSerializationConfig(), builder);

        assertNotNull(builder.getFilteredProperties());
        assertEquals(props.size(), builder.getFilteredProperties().length);
    }

    // Tests removeSetterlessGetters with REQUIRE_SETTERS_FOR_GETTERS enabled
    @Test
    public void testRemoveSetterlessGetters_featureEnabled_removesGetter() throws Exception {
        ObjectMapper requireSetterMapper = new ObjectMapper();
        requireSetterMapper.enable(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS);
        JavaType type = requireSetterMapper.constructType(SetterlessBean.class);
        BeanDescription beanDesc = requireSetterMapper.getSerializationConfig().introspect(type);
        List<BeanPropertyDefinition> props = new ArrayList<BeanPropertyDefinition>(beanDesc.findProperties());

        assertFalse(props.isEmpty());
        factory.removeSetterlessGetters(requireSetterMapper.getSerializationConfig(), beanDesc, props);
        assertTrue(props.isEmpty());
    }

    // Tests customSerializers returns empty when default configuration
    @Test
    public void testCustomSerializers_defaultConfig_returnsEmptyIterable() {
        Iterable<Serializers> serializers = factory.customSerializers();
        assertNotNull(serializers);
        assertFalse(serializers.iterator().hasNext());
    }
}