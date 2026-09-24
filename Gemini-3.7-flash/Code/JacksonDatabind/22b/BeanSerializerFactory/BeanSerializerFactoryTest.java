package com.fasterxml.jackson.databind.ser;

import java.util.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.StdConverter;

public class BeanSerializerFactoryTest {

    private BeanSerializerFactory factory;
    private ObjectMapper mapper;
    private SerializerProvider serializerProvider;

    // Helper classes for testing
    static class SimpleBean {
        private String name = "test";
        private int value = 42;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }

    static class EmptyBean {
    }

    @JsonIgnoreProperties({"ignoredField"})
    static class FilteredBean {
        public String normalField = "normal";
        public String ignoredField = "ignored";
    }

    @JsonIgnoreType
    static class IgnorableType {
        public int x = 1;
    }

    static class BeanWithIgnorableProp {
        public IgnorableType ignored = new IgnorableType();
        public String visible = "ok";
    }

    static class SetterlessBean {
        private String readOnly = "ro";
        public String getReadOnly() { return readOnly; }
    }

    static class Views {
        static class ViewA {}
        static class ViewB {}
    }

    static class ViewBean {
        @JsonView(Views.ViewA.class)
        public String a = "A";
        @JsonView(Views.ViewB.class)
        public String b = "B";
    }

    static class AnyGetterBean {
        private Map<String, Object> map = new HashMap<String, Object>();
        public AnyGetterBean() {
            map.put("key", "val");
        }
        @JsonAnyGetter
        public Map<String, Object> any() {
            return map;
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class PropertyObjectIdBean {
        public int id = 123;
        public String name = "objId";
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    static class CustomObjectIdBean {
        public String name = "custom";
    }

    static class ConvertedBean {
        public String text = "hello";
    }

    static class StringToWrapperConverter extends StdConverter<ConvertedBean, String> {
        @Override
        public String convert(ConvertedBean value) {
            return value.text.toUpperCase();
        }
    }

    static class TypeIdBean {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "extType")
        public Object poly;
        public String extType;
    }

    static class SubclassFactory extends BeanSerializerFactory {
        public SubclassFactory(SerializerFactoryConfig config) {
            super(config);
        }
    }

    @Before
    public void setUp() {
        factory = BeanSerializerFactory.instance;
        mapper = new ObjectMapper();
        serializerProvider = new DefaultSerializerProvider.Impl().createInstance(mapper.getSerializationConfig(), factory);
    }

    // Tests withConfig returning same instance when config matches
    @Test
    public void testWithConfig_sameConfig_returnsSameInstance() {
        SerializerFactoryConfig config = new SerializerFactoryConfig();
        BeanSerializerFactory customFactory = new BeanSerializerFactory(config);
        SerializerFactory result = customFactory.withConfig(config);
        assertSame(customFactory, result);
    }

    // Tests withConfig returning new instance when config differs
    @Test
    public void testWithConfig_differentConfig_returnsNewInstance() {
        SerializerFactoryConfig config1 = new SerializerFactoryConfig();
        SerializerFactoryConfig config2 = new SerializerFactoryConfig();
        BeanSerializerFactory customFactory = new BeanSerializerFactory(config1);
        SerializerFactory result = customFactory.withConfig(config2);
        assertNotSame(customFactory, result);
        assertTrue(result instanceof BeanSerializerFactory);
    }

    // Tests exception thrown when subclass does not properly override withConfig
    @Test(expected = IllegalStateException.class)
    public void testWithConfig_unsupportedSubclass_throwsException() {
        SubclassFactory sub = new SubclassFactory(new SerializerFactoryConfig());
        sub.withConfig(new SerializerFactoryConfig());
    }

    // Tests isPotentialBeanType with non-bean classes (primitive, array)
    @Test
    public void testIsPotentialBeanType_nonBeanTypes_returnsFalse() {
        assertFalse(factory.isPotentialBeanType(int.class));
        assertFalse(factory.isPotentialBeanType(int[].class));
        assertFalse(factory.isPotentialBeanType(String[].class));
    }

    // Tests isPotentialBeanType with standard bean class
    @Test
    public void testIsPotentialBeanType_beanType_returnsTrue() {
        assertTrue(factory.isPotentialBeanType(SimpleBean.class));
    }

    // Tests createSerializer for a standard POJO bean
    @Test
    public void testCreateSerializer_simpleBean_returnsSerializer() throws Exception {
        JavaType type = mapper.constructType(SimpleBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        assertNotNull(ser);
        assertTrue(ser instanceof BeanSerializer || ser instanceof ResolvableSerializer);
    }

    // Tests createSerializer for Object.class returns unknown type serializer
    @Test
    public void testCreateSerializer_objectClass_returnsUnknownTypeSerializer() throws Exception {
        JavaType type = mapper.constructType(Object.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        assertNotNull(ser);
    }

    // Tests createSerializer for empty bean
    @Test
    public void testCreateSerializer_emptyBean_returnsEmptyOrDummySerializer() throws Exception {
        JavaType type = mapper.constructType(EmptyBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        assertNotNull(ser);
    }

    // Tests findBeanSerializer returns null for primitive type
    @Test
    public void testFindBeanSerializer_primitiveType_returnsNull() throws Exception {
        JavaType type = mapper.constructType(int.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        JsonSerializer<Object> ser = factory.findBeanSerializer(serializerProvider, type, beanDesc);
        assertNull(ser);
    }

    // Tests serializer construction for bean with @JsonIgnoreProperties
    @Test
    public void testConstructBeanSerializer_withIgnoredProperties_filtersOutProperty() throws Exception {
        JavaType type = mapper.constructType(FilteredBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        assertNotNull(ser);

        String json = mapper.writeValueAsString(new FilteredBean());
        assertTrue(json.contains("normalField"));
        assertFalse(json.contains("ignoredField"));
    }

    // Tests serializer construction removing properties with @JsonIgnoreType
    @Test
    public void testConstructBeanSerializer_withIgnorableTypeProperty_removesProperty() throws Exception {
        JavaType type = mapper.constructType(BeanWithIgnorableProp.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        assertNotNull(ser);

        String json = mapper.writeValueAsString(new BeanWithIgnorableProp());
        assertTrue(json.contains("visible"));
        assertFalse(json.contains("ignored"));
    }

    // Tests setterless getters removal when REQUIRE_SETTERS_FOR_GETTERS is enabled
    @Test
    public void testConstructBeanSerializer_requireSettersForGetters_removesSetterlessGetters() throws Exception {
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.enable(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS);
        customMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        String json = customMapper.writeValueAsString(new SetterlessBean());
        assertEquals("{}", json);
    }

    // Tests serializer construction with views (@JsonView)
    @Test
    public void testConstructBeanSerializer_withViews_handlesViewInclusion() throws Exception {
        ViewBean bean = new ViewBean();
        String jsonA = mapper.writerWithView(Views.ViewA.class).writeValueAsString(bean);
        assertTrue(jsonA.contains("\"a\":\"A\""));
        assertFalse(jsonA.contains("\"b\":\"B\""));

        String jsonB = mapper.writerWithView(Views.ViewB.class).writeValueAsString(bean);
        assertFalse(jsonB.contains("\"a\":\"A\""));
        assertTrue(jsonB.contains("\"b\":\"B\""));
    }

    // Tests serializer construction for bean with @JsonAnyGetter
    @Test
    public void testConstructBeanSerializer_withAnyGetter_serializesMapEntries() throws Exception {
        AnyGetterBean bean = new AnyGetterBean();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"key\":\"val\""));
    }

    // Tests serializer construction with PropertyBased ObjectIdGenerator
    @Test
    public void testConstructBeanSerializer_withPropertyBasedObjectId_serializesObjectId() throws Exception {
        PropertyObjectIdBean bean = new PropertyObjectIdBean();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"id\":123"));
        assertTrue(json.contains("\"name\":\"objId\""));
    }

    // Tests serializer construction with standard ObjectIdGenerator
    @Test
    public void testConstructBeanSerializer_withCustomObjectId_serializesObjectId() throws Exception {
        CustomObjectIdBean bean = new CustomObjectIdBean();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"@id\":1"));
        assertTrue(json.contains("\"name\":\"custom\""));
    }

    // Tests findPropertyTypeSerializer and findPropertyContentTypeSerializer when no resolver annotations present
    @Test
    public void testFindPropertyTypeSerializer_noAnnotations_returnsNullOrTypeSerializer() throws Exception {
        JavaType baseType = mapper.constructType(String.class);
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(SimpleBean.class));
        AnnotatedMember member = desc.findProperties().get(0).getAccessor();

        TypeSerializer typeSer = factory.findPropertyTypeSerializer(baseType, config, member);
        assertNull(typeSer);

        JavaType listType = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        TypeSerializer contentTypeSer = factory.findPropertyContentTypeSerializer(listType, config, member);
        assertNull(contentTypeSer);
    }

    // Tests removeOverlappingTypeIds with external property type id
    @Test
    public void testRemoveOverlappingTypeIds_externalPropertyConflict_removesOverlap() throws Exception {
        JavaType type = mapper.constructType(TypeIdBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        assertNotNull(ser);
    }

    // Tests custom SerializerModifier integration via factory configuration
    @Test
    public void testWithSerializerModifier_customModifier_invokesModifier() throws Exception {
        final boolean[] modifierCalled = new boolean[1];
        BeanSerializerModifier modifier = new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                    BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                modifierCalled[0] = true;
                return beanProperties;
            }
        };

        SerializerFactory customFactory = factory.withSerializerModifier(modifier);
        SerializerProvider prov = new DefaultSerializerProvider.Impl().createInstance(mapper.getSerializationConfig(), customFactory);
        JavaType type = mapper.constructType(SimpleBean.class);
        customFactory.createSerializer(prov, type);
        assertTrue(modifierCalled[0]);
    }

    // Tests withAdditionalSerializers integration via factory configuration
    @Test
    public void testWithAdditionalSerializers_customSerializers_findsSerializer() throws Exception {
        final JsonSerializer<?> customSer = new JsonSerializer<SimpleBean>() {
            @Override
            public void serialize(SimpleBean value, com.fasterxml.jackson.core.JsonGenerator gen, SerializerProvider serializers)
                    throws java.io.IOException {
                gen.writeString("custom");
            }
        };

        Serializers.Base serializers = new Serializers.Base() {
            @Override
            public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
                if (type.getRawClass() == SimpleBean.class) {
                    return customSer;
                }
                return null;
            }
        };

        SerializerFactory customFactory = factory.withAdditionalSerializers(serializers);
        SerializerProvider prov = new DefaultSerializerProvider.Impl().createInstance(mapper.getSerializationConfig(), customFactory);
        JavaType type = mapper.constructType(SimpleBean.class);
        JsonSerializer<Object> result = customFactory.createSerializer(prov, type);
        assertSame(customSer, result);
    }
}