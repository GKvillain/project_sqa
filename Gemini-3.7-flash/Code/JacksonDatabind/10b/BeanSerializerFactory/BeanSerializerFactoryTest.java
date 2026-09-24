package com.fasterxml.jackson.databind.ser;

import java.io.IOException;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class BeanSerializerFactoryTest {

    private BeanSerializerFactory factory;
    private ObjectMapper mapper;
    private DefaultSerializerProvider provider;

    // View markers for testing
    interface Views {
        interface ViewA {}
        interface ViewB {}
    }

    // Test POJOs
    static class SimpleBean {
        public String name = "test";
        public int value = 42;

        public String getName() { return name; }
        public int getValue() { return value; }
    }

    static class EmptyBean {
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class PropertyIdBean {
        public int id = 123;
        public String name = "idBean";
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
    static class SequenceIdBean {
        public String name = "seqBean";
    }

    static class AnyGetterBean {
        private Map<String, Object> extra = new HashMap<String, Object>();

        public AnyGetterBean() {
            extra.put("extraKey", "extraVal");
        }

        @JsonAnyGetter
        public Map<String, Object> getExtra() {
            return extra;
        }
    }

    @JsonIgnoreProperties({"ignoredField"})
    static class FilteredPropertiesBean {
        public String visibleField = "visible";
        public String ignoredField = "ignored";
    }

    @JsonIgnoreType
    static class IgnoredType {
        public String secret = "hidden";
    }

    static class BeanWithIgnoredType {
        public String normal = "normal";
        public IgnoredType ignored = new IgnoredType();
    }

    static class SetterlessGetterBean {
        private String field = "getterOnly";

        public String getField() {
            return field;
        }
    }

    static class ViewBean {
        @JsonView(Views.ViewA.class)
        public String viewA = "A";

        @JsonView(Views.ViewB.class)
        public String viewB = "B";

        public String general = "General";
    }

    @JsonFilter("customFilter")
    static class FilteredBean {
        public String propA = "valA";
        public String propB = "valB";
    }

    @JsonPropertyOrder(alphabetic = true)
    static class AlphabeticOrderBean {
        public String z = "z";
        public String a = "a";
        public String m = "m";
    }

    static class PolymorphicPropertyBean {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
        public Object data = "someData";
    }

    static class CustomSubtypeFactory extends BeanSerializerFactory {
        public CustomSubtypeFactory(SerializerFactoryConfig config) {
            super(config);
        }
    }

    @Before
    public void setUp() {
        factory = BeanSerializerFactory.instance;
        mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        provider = ((DefaultSerializerProvider) mapper.getSerializerProvider()).createInstance(config, factory);
    }

    // Tests singleton instance and withConfig behavior with identical config
    @Test
    public void testWithConfig_sameConfig_returnsSameInstance() {
        SerializerFactoryConfig config = new SerializerFactoryConfig();
        BeanSerializerFactory customFactory = new BeanSerializerFactory(config);
        SerializerFactory result = customFactory.withConfig(config);
        assertSame(customFactory, result);
    }

    // Tests withConfig when creating a new instance with a new config
    @Test
    public void testWithConfig_newConfig_returnsNewInstance() {
        SerializerFactoryConfig config1 = new SerializerFactoryConfig();
        SerializerFactoryConfig config2 = new SerializerFactoryConfig();
        BeanSerializerFactory customFactory = new BeanSerializerFactory(config1);
        SerializerFactory result = customFactory.withConfig(config2);
        assertNotSame(customFactory, result);
        assertTrue(result instanceof BeanSerializerFactory);
    }

    // Tests withConfig when invoked on a subclass without overriding withConfig throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testWithConfig_unsupportedSubtype_throwsIllegalStateException() {
        SerializerFactoryConfig config1 = new SerializerFactoryConfig();
        SerializerFactoryConfig config2 = new SerializerFactoryConfig();
        CustomSubtypeFactory customFactory = new CustomSubtypeFactory(config1);
        customFactory.withConfig(config2);
    }

    // Tests standard POJO serialization creation
    @Test
    public void testCreateSerializer_simpleBean_returnsNonNullSerializer() throws JsonMappingException {
        JavaType type = mapper.constructType(SimpleBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(provider, type);
        assertNotNull(ser);
    }

    // Tests serialization creation for Object.class returns unknown type serializer
    @Test
    public void testCreateSerializer_objectClass_returnsUnknownSerializer() throws JsonMappingException {
        JavaType type = mapper.constructType(Object.class);
        JsonSerializer<Object> ser = factory.createSerializer(provider, type);
        assertNotNull(ser);
    }

    // Tests finding bean serializer for non-bean primitive types returns null
    @Test
    public void testFindBeanSerializer_primitiveType_returnsNull() throws JsonMappingException {
        JavaType type = mapper.constructType(int.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        JsonSerializer<Object> ser = factory.findBeanSerializer(provider, type, beanDesc);
        assertNull(ser);
    }

    // Tests finding bean serializer for empty bean without properties returns null or dummy
    @Test
    public void testFindBeanSerializer_emptyBean_returnsSerializerOrNull() throws JsonMappingException {
        JavaType type = mapper.constructType(EmptyBean.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        JsonSerializer<Object> ser = factory.findBeanSerializer(provider, type, beanDesc);
        assertNull(ser);
    }

    // Tests isPotentialBeanType method with standard classes and primitive/array types
    @Test
    public void testIsPotentialBeanType_variousClasses_returnsExpectedBoolean() {
        assertTrue(factory.isPotentialBeanType(SimpleBean.class));
        assertFalse(factory.isPotentialBeanType(int.class));
        assertFalse(factory.isPotentialBeanType(int[].class));
    }

    // Tests handling of @JsonIdentityInfo with PropertyGenerator
    @Test
    public void testConstructBeanSerializer_propertyBasedObjectId_constructsSerializer() throws IOException {
        PropertyIdBean bean = new PropertyIdBean();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"id\":123"));
        assertTrue(json.contains("\"name\":\"idBean\""));
    }

    // Tests handling of @JsonIdentityInfo with standard generator
    @Test
    public void testConstructBeanSerializer_sequenceObjectId_constructsSerializer() throws IOException {
        SequenceIdBean bean = new SequenceIdBean();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"seqBean\""));
    }

    // Tests serialization of bean containing @JsonAnyGetter
    @Test
    public void testConstructBeanSerializer_anyGetter_serializesAnyProperties() throws IOException {
        AnyGetterBean bean = new AnyGetterBean();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"extraKey\":\"extraVal\""));
    }

    // Tests filterBeanProperties suppresses ignored properties
    @Test
    public void testFilterBeanProperties_ignoredProperties_suppressesIgnoredField() throws IOException {
        FilteredPropertiesBean bean = new FilteredPropertiesBean();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("visibleField"));
        assertFalse(json.contains("ignoredField"));
    }

    // Tests removeIgnorableTypes eliminates properties marked with @JsonIgnoreType
    @Test
    public void testRemoveIgnorableTypes_ignoredType_suppressesTypeField() throws IOException {
        BeanWithIgnoredType bean = new BeanWithIgnoredType();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("normal"));
        assertFalse(json.contains("ignored"));
    }

    // Tests processViews with DEFAULT_VIEW_INCLUSION enabled
    @Test
    public void testProcessViews_defaultViewInclusionEnabled_includesUnannotated() throws IOException {
        ViewBean bean = new ViewBean();
        String json = mapper.writerWithView(Views.ViewA.class).writeValueAsString(bean);
        assertTrue(json.contains("viewA"));
        assertFalse(json.contains("viewB"));
        assertTrue(json.contains("general"));
    }

    // Tests processViews with DEFAULT_VIEW_INCLUSION disabled
    @Test
    public void testProcessViews_defaultViewInclusionDisabled_excludesUnannotated() throws IOException {
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        ViewBean bean = new ViewBean();
        String json = customMapper.writerWithView(Views.ViewA.class).writeValueAsString(bean);
        assertTrue(json.contains("viewA"));
        assertFalse(json.contains("viewB"));
        assertFalse(json.contains("general"));
    }

    // Tests removeSetterlessGetters when REQUIRE_SETTERS_FOR_GETTERS is enabled
    @Test
    public void testRemoveSetterlessGetters_requireSettersEnabled_removesGetterOnlyProperty() throws IOException {
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.enable(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS);
        customMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        SetterlessGetterBean bean = new SetterlessGetterBean();
        String json = customMapper.writeValueAsString(bean);
        assertEquals("{}", json);
    }

    // Tests custom serializer modifiers via SerializerFactoryConfig
    @Test
    public void testSerializerModifier_customModifier_invokedDuringConstruction() throws IOException {
        final boolean[] modifierCalled = new boolean[1];
        BeanSerializerModifier modifier = new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                    BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                modifierCalled[0] = true;
                return beanProperties;
            }
        };

        SerializerFactoryConfig config = new SerializerFactoryConfig().withSerializerModifier(modifier);
        BeanSerializerFactory customFactory = new BeanSerializerFactory(config);
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.setSerializerFactory(customFactory);

        customMapper.writeValueAsString(new SimpleBean());
        assertTrue(modifierCalled[0]);
    }

    // Tests @JsonFilter on bean class
    @Test
    public void testConstructBeanSerializer_withJsonFilter() throws IOException {
        FilteredBean bean = new FilteredBean();
        SimpleFilterProvider filters = new SimpleFilterProvider().addFilter(
                "customFilter", SimpleBeanPropertyFilter.filterOutAllExcept("propA"));
        String json = mapper.writer(filters).writeValueAsString(bean);
        assertTrue(json.contains("propA"));
        assertFalse(json.contains("propB"));
    }

    // Tests sorting of bean properties with alphabetic order
    @Test
    public void testConstructBeanSerializer_alphabeticPropertyOrder() throws IOException {
        AlphabeticOrderBean bean = new AlphabeticOrderBean();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"a\":\"a\",\"m\":\"m\",\"z\":\"z\"}", json);
    }

    // Tests property with polymorphic TypeSerializer
    @Test
    public void testConstructBeanSerializer_polymorphicProperty() throws IOException {
        PolymorphicPropertyBean bean = new PolymorphicPropertyBean();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"@class\":\"java.lang.String\""));
        assertTrue(json.contains("\"data\":\"someData\""));
    }

    // Tests modifier updateBuilder callback
    @Test
    public void testSerializerModifier_updateBuilder() throws IOException {
        final boolean[] updateBuilderCalled = new boolean[1];
        BeanSerializerModifier modifier = new BeanSerializerModifier() {
            @Override
            public BeanSerializerBuilder updateBuilder(SerializationConfig config,
                    BeanDescription beanDesc, BeanSerializerBuilder builder) {
                updateBuilderCalled[0] = true;
                return builder;
            }
        };

        SerializerFactoryConfig config = new SerializerFactoryConfig().withSerializerModifier(modifier);
        BeanSerializerFactory customFactory = new BeanSerializerFactory(config);
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.setSerializerFactory(customFactory);

        customMapper.writeValueAsString(new SimpleBean());
        assertTrue(updateBuilderCalled[0]);
    }
}