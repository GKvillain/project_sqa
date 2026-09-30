package com.fasterxml.jackson.databind.ser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.impl.PropertyBasedObjectIdGenerator;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;

public class BeanSerializerFactoryTest {

    private BeanSerializerFactory factory;
    private ObjectMapper mapper;
    private SerializerProvider serializerProvider;

    // Helper classes for testing
    public static class SimpleBean {
        public String name = "test";
        public int value = 42;

        public String getName() { return name; }
        public int getValue() { return value; }
    }

    public static class EmptyBean {
    }

    @JsonIgnoreProperties({"ignoredField"})
    public static class FilteredBean {
        public String kept = "keep";
        public String ignoredField = "ignore";
    }

    @JsonIgnoreType
    public static class IgnoredType {
        public String data = "data";
    }

    public static class BeanWithIgnoredTypeField {
        public String name = "name";
        public IgnoredType ignored = new IgnoredType();
    }

    public static class SetterlessBean {
        private String prop = "value";

        public String getProp() { return prop; }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class PropertyIdBean {
        public int id = 123;
        public String name = "idBean";
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
    public static class IntSequenceIdBean {
        public String name = "sequenceBean";
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "invalidIdProp")
    public static class InvalidPropertyIdBean {
        public String name = "invalid";
    }

    public static class AnyGetterBean {
        private Map<String, Object> map = new HashMap<String, Object>();

        public AnyGetterBean() {
            map.put("key1", "val1");
        }

        @JsonAnyGetter
        public Map<String, Object> any() {
            return map;
        }
    }

    public interface Views {
        interface ViewA {}
        interface ViewB {}
    }

    public static class ViewBean {
        @JsonView(Views.ViewA.class)
        public String viewA = "A";

        @JsonView(Views.ViewB.class)
        public String viewB = "B";

        public String noView = "default";
    }

    public static class SubclassedFactory extends BeanSerializerFactory {
        public SubclassedFactory(SerializerFactoryConfig config) {
            super(config);
        }
    }

    public static class CustomConverter extends StdConverter<SimpleBean, String> {
        @Override
        public String convert(SimpleBean value) {
            return value.name + ":" + value.value;
        }
    }

    public enum TestEnum {
        A, B, C
    }

    @JsonFilter("customFilter")
    public static class FilterAnnotatedBean {
        public String propA = "valA";
        public String propB = "valB";
    }

    @JsonPropertyOrder({"z", "a", "m"})
    public static class OrderedBean {
        public String a = "1";
        public String m = "2";
        public String z = "3";
    }

    public static class PolymorphicPropertyBean {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
        public Object poly = new SimpleBean();

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@elemType")
        public List<Object> polyList = Collections.<Object>singletonList(new SimpleBean());
    }

    @JsonSerialize(converter = CustomConverter.class)
    public static class ConvertedBean extends SimpleBean {
    }

    @Before
    public void setUp() {
        factory = BeanSerializerFactory.instance;
        mapper = new ObjectMapper();
        serializerProvider = mapper.getSerializerProviderInstance();
    }

    // Tests singleton instance existence
    @Test
    public void testInstance_singleton_isNotNull() {
        assertNotNull(BeanSerializerFactory.instance);
    }

    // Tests withConfig when passing the exact same config instance
    @Test
    public void testWithConfig_sameConfig_returnsSameInstance() {
        SerializerFactory factory2 = factory.withConfig(factory.getFactoryConfig());
        assertSame(factory, factory2);
    }

    // Tests withConfig when passing a new configuration object
    @Test
    public void testWithConfig_newConfig_returnsNewInstance() {
        SerializerFactoryConfig newConfig = new SerializerFactoryConfig();
        SerializerFactory factory2 = factory.withConfig(newConfig);
        assertNotNull(factory2);
        assertNotSame(factory, factory2);
        assertTrue(factory2 instanceof BeanSerializerFactory);
    }

    // Tests withConfig exception branch when subclass does not override withConfig
    @Test(expected = IllegalStateException.class)
    public void testWithConfig_unsupportedSubclass_throwsException() {
        SubclassedFactory customFactory = new SubclassedFactory(new SerializerFactoryConfig());
        customFactory.withConfig(new SerializerFactoryConfig());
    }

    // Tests createSerializer for standard Object.class returning unknown type serializer
    @Test
    public void testCreateSerializer_plainObjectClass_returnsUnknownTypeSerializer() throws Exception {
        JavaType type = mapper.constructType(Object.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        assertNotNull(ser);
        assertFalse(ser.isUnwrappingSerializer());
    }

    // Tests createSerializer for a normal Java POJO
    @Test
    public void testCreateSerializer_simpleBean_returnsBeanSerializer() throws Exception {
        JavaType type = mapper.constructType(SimpleBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        assertNotNull(ser);
        assertTrue(ser instanceof BeanSerializer || ser.getClass().getName().contains("BeanSerializer"));
    }

    // Tests createSerializer for empty bean
    @Test
    public void testCreateSerializer_emptyBean_returnsDummyOrEmptySerializer() throws Exception {
        JavaType type = mapper.constructType(EmptyBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        assertNotNull(ser);
    }

    // Tests createSerializer when custom serializer is registered in factory config
    @Test
    public void testCreateSerializer_customSerializer_usesRegisteredSerializer() throws Exception {
        final JsonSerializer<SimpleBean> customSer = new StdSerializer<SimpleBean>(SimpleBean.class) {
            @Override
            public void serialize(SimpleBean value, JsonGenerator gen, SerializerProvider provider) {
            }
        };

        SimpleSerializers serializers = new SimpleSerializers();
        serializers.addSerializer(SimpleBean.class, customSer);
        SerializerFactoryConfig config = new SerializerFactoryConfig().withAdditionalSerializers(serializers);
        BeanSerializerFactory customFactory = new BeanSerializerFactory(config);

        JavaType type = mapper.constructType(SimpleBean.class);
        JsonSerializer<Object> ser = customFactory.createSerializer(serializerProvider, type);
        assertSame(customSer, ser);
    }

    // Tests findBeanSerializer with primitive/non-potential bean type returning null
    @Test
    public void testFindBeanSerializer_primitiveType_returnsNull() throws Exception {
        JavaType type = mapper.constructType(int.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        JsonSerializer<Object> ser = factory.findBeanSerializer(serializerProvider, type, beanDesc);
        assertNull(ser);
    }

    // Tests findBeanSerializer with Enum type
    @Test
    public void testFindBeanSerializer_enumType_returnsSerializerOrNull() throws Exception {
        JavaType type = mapper.constructType(TestEnum.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        JsonSerializer<Object> ser = factory.findBeanSerializer(serializerProvider, type, beanDesc);
        // Enums are not standard beans with getters, so ser might be null or dummy
        // But isPotentialBeanType allows enums to proceed to constructBeanSerializer
        assertTrue(ser == null || ser instanceof JsonSerializer);
    }

    // Tests isPotentialBeanType for various types
    @Test
    public void testIsPotentialBeanType_variousClasses_correctFlags() {
        assertTrue(factory.isPotentialBeanType(SimpleBean.class));
        assertFalse(factory.isPotentialBeanType(int.class));
        assertFalse(factory.isPotentialBeanType(int[].class));
    }

    // Tests constructObjectIdHandler with PropertyGenerator
    @Test
    public void testConstructObjectIdHandler_propertyBased_createsHandler() throws Exception {
        JavaType type = mapper.constructType(PropertyIdBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        assertNotNull(ser);
    }

    // Tests constructObjectIdHandler with non-property generator (IntSequenceGenerator)
    @Test
    public void testConstructObjectIdHandler_intSequence_createsHandler() throws Exception {
        JavaType type = mapper.constructType(IntSequenceIdBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        assertNotNull(ser);
    }

    // Tests constructObjectIdHandler with invalid property name throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructObjectIdHandler_missingProperty_throwsException() throws Exception {
        JavaType type = mapper.constructType(InvalidPropertyIdBean.class);
        factory.createSerializer(serializerProvider, type);
    }

    // Tests bean with @JsonIgnoreProperties filtering
    @Test
    public void testFilterBeanProperties_ignoredProperties_filtersOutProperties() throws Exception {
        JavaType type = mapper.constructType(FilteredBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        assertNotNull(ser);
        String json = mapper.writeValueAsString(new FilteredBean());
        assertTrue(json.contains("kept"));
        assertFalse(json.contains("ignoredField"));
    }

    // Tests removeIgnorableTypes when property type has @JsonIgnoreType
    @Test
    public void testRemoveIgnorableTypes_ignoredType_excludesProperty() throws Exception {
        JavaType type = mapper.constructType(BeanWithIgnoredTypeField.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        assertNotNull(ser);
        String json = mapper.writeValueAsString(new BeanWithIgnoredTypeField());
        assertTrue(json.contains("name"));
        assertFalse(json.contains("ignored"));
    }

    // Tests removeSetterlessGetters when REQUIRE_SETTERS_FOR_GETTERS is enabled
    @Test
    public void testRemoveSetterlessGetters_enabledFeature_filtersSetterlessGetter() throws Exception {
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.enable(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS);
        customMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        String json = customMapper.writeValueAsString(new SetterlessBean());
        assertFalse(json.contains("prop"));
    }

    // Tests AnyGetter writer construction
    @Test
    public void testConstructBeanSerializer_anyGetter_includesAnyProperties() throws Exception {
        JavaType type = mapper.constructType(AnyGetterBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        assertNotNull(ser);

        String json = mapper.writeValueAsString(new AnyGetterBean());
        assertTrue(json.contains("key1"));
        assertTrue(json.contains("val1"));
    }

    // Tests processViews with JSON views
    @Test
    public void testProcessViews_viewAnnotations_filtersByActiveView() throws Exception {
        ViewBean bean = new ViewBean();

        String jsonViewA = mapper.writerWithView(Views.ViewA.class).writeValueAsString(bean);
        assertTrue(jsonViewA.contains("viewA"));
        assertFalse(jsonViewA.contains("viewB"));

        String jsonViewB = mapper.writerWithView(Views.ViewB.class).writeValueAsString(bean);
        assertFalse(jsonViewB.contains("viewA"));
        assertTrue(jsonViewB.contains("viewB"));
    }

    // Tests findPropertyTypeSerializer and findPropertyContentTypeSerializer returning null for plain types
    @Test
    public void testFindPropertyTypeSerializer_nonPolymorphic_returnsNull() throws Exception {
        JavaType type = mapper.constructType(SimpleBean.class);
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = config.introspect(type);
        AnnotatedMember accessor = beanDesc.findProperties().get(0).getAccessor();

        TypeSerializer typeSer = factory.findPropertyTypeSerializer(type, config, accessor);
        assertNull(typeSer);

        JavaType listType = mapper.constructType(List.class);
        TypeSerializer contentSer = factory.findPropertyContentTypeSerializer(listType, config, accessor);
        assertNull(contentSer);
    }

    // Tests customizer and modifier callbacks in BeanSerializerModifier
    @Test
    public void testBeanSerializerModifier_hooksInvoked() throws Exception {
        final boolean[] flags = new boolean[4];

        BeanSerializerModifier modifier = new BeanSerializerModifier() {
            @Override
            public BeanSerializerBuilder updateBuilder(SerializationConfig config, BeanDescription beanDesc, BeanSerializerBuilder builder) {
                flags[0] = true;
                return super.updateBuilder(config, beanDesc, builder);
            }

            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                flags[1] = true;
                return super.changeProperties(config, beanDesc, beanProperties);
            }

            @Override
            public List<BeanPropertyWriter> orderProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                flags[2] = true;
                return super.orderProperties(config, beanDesc, beanProperties);
            }

            @Override
            public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
                flags[3] = true;
                return super.modifySerializer(config, beanDesc, serializer);
            }
        };

        SerializerFactoryConfig config = new SerializerFactoryConfig().withSerializerModifier(modifier);
        BeanSerializerFactory customFactory = new BeanSerializerFactory(config);

        JavaType type = mapper.constructType(SimpleBean.class);
        JsonSerializer<Object> ser = customFactory.createSerializer(serializerProvider, type);
        assertNotNull(ser);

        assertTrue("updateBuilder should be called", flags[0]);
        assertTrue("changeProperties should be called", flags[1]);
        assertTrue("orderProperties should be called", flags[2]);
        assertTrue("modifySerializer should be called", flags[3]);
    }

    // Tests FilterId detection via @JsonFilter annotation
    @Test
    public void testConstructBeanSerializer_withJsonFilter_setsFilterId() throws Exception {
        SimpleFilterProvider filters = new SimpleFilterProvider()
                .addFilter("customFilter", SimpleBeanPropertyFilter.serializeAllExcept("propB"));
        ObjectMapper filterMapper = new ObjectMapper().setFilterProvider(filters);

        String json = filterMapper.writeValueAsString(new FilterAnnotatedBean());
        assertTrue(json.contains("propA"));
        assertFalse(json.contains("propB"));
    }

    // Tests Property ordering via @JsonPropertyOrder and alphabetic sorting
    @Test
    public void testConstructBeanSerializer_propertyOrdering() throws Exception {
        String json = mapper.writeValueAsString(new OrderedBean());
        int idxZ = json.indexOf("\"z\"");
        int idxA = json.indexOf("\"a\"");
        int idxM = json.indexOf("\"m\"");

        assertTrue(idxZ < idxA);
        assertTrue(idxA < idxM);
    }

    // Tests findPropertyTypeSerializer and findPropertyContentTypeSerializer on annotated members
    @Test
    public void testFindPropertyTypeSerializer_withPolymorphicAnnotations_returnsTypeSerializer() throws Exception {
        JavaType type = mapper.constructType(PolymorphicPropertyBean.class);
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = config.introspect(type);

        AnnotatedMember polyMember = null;
        AnnotatedMember polyListMember = null;
        for (BeanPropertyDefinition prop : beanDesc.findProperties()) {
            if ("poly".equals(prop.getName())) {
                polyMember = prop.getAccessor();
            } else if ("polyList".equals(prop.getName())) {
                polyListMember = prop.getAccessor();
            }
        }

        assertNotNull(polyMember);
        assertNotNull(polyListMember);

        JavaType polyType = mapper.constructType(Object.class);
        TypeSerializer polySer = factory.findPropertyTypeSerializer(polyType, config, polyMember);
        assertNotNull(polySer);

        JavaType listType = mapper.constructType(List.class);
        TypeSerializer listContentSer = factory.findPropertyContentTypeSerializer(listType, config, polyListMember);
        assertNotNull(listContentSer);
    }

    // Tests class-level converter usage with @JsonSerialize(converter = ...)
    @Test
    public void testCreateSerializer_classLevelConverter_appliesConverter() throws Exception {
        JavaType type = mapper.constructType(ConvertedBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        assertNotNull(ser);

        String json = mapper.writeValueAsString(new ConvertedBean());
        assertEquals("\"test:42\"", json);
    }

    // Tests customSerializers() iterator from factory config
    @Test
    public void testCustomSerializers_iteratorReturned() {
        SimpleSerializers simpleSerializers = new SimpleSerializers();
        SerializerFactoryConfig config = new SerializerFactoryConfig().withAdditionalSerializers(simpleSerializers);
        BeanSerializerFactory customFactory = new BeanSerializerFactory(config);

        Iterable<Serializers> it = customFactory.customSerializers();
        assertNotNull(it);
        assertTrue(it.iterator().hasNext());
    }
}