package com.fasterxml.jackson.databind.ser;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonFilter;
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
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BeanSerializerFactoryTest {

    private BeanSerializerFactory factory;
    private ObjectMapper mapper;
    private SerializerProvider serializerProvider;

    // Helper classes for testing
    public static class SimpleBean {
        private String name = "test";
        private int age = 30;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    public static class CompletelyEmptyBean {
    }

    @JsonFilter("testFilter")
    public static class EmptyAnnotatedBean {
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

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class ObjectIdBean {
        public String name = "name";
        public int id = 123;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "nonExistentId")
    public static class InvalidObjectIdBean {
        public String name = "name";
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    public static class IntSequenceIdBean {
        public String value = "val";
    }

    public static class Views {
        public interface ViewA {}
        public interface ViewB {}
    }

    public static class ViewBean {
        @JsonView(Views.ViewA.class)
        public String fieldA = "A";

        @JsonView(Views.ViewB.class)
        public String fieldB = "B";

        public String fieldDefault = "D";
    }

    @JsonIgnoreProperties({"ignoredField"})
    public static class IgnoredPropsBean {
        public String keptField = "kept";
        public String ignoredField = "ignored";
    }

    @JsonIgnoreType
    public static class IgnorableType {
        public String data = "data";
    }

    public static class ContainerOfIgnorable {
        public String title = "title";
        public IgnorableType ignorable = new IgnorableType();
    }

    public static class SetterlessBean {
        private String setterless = "val";
        private String normal = "norm";

        public String getSetterless() { return setterless; }
        public String getNormal() { return normal; }
        public void setNormal(String normal) { this.normal = normal; }
    }

    public static class PolymorphicContainer {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
        public Object polyProperty = new SimpleBean();

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "elemType")
        public List<Object> polyList = new ArrayList<Object>();
    }

    public static class NonPolymorphicContainer {
        public String plainProperty = "plain";
        public List<String> plainList = new ArrayList<String>();
    }

    public enum TestEnum {
        VAL_A, VAL_B
    }

    private static class CustomSubtypeFactory extends BeanSerializerFactory {
        public CustomSubtypeFactory(SerializerFactoryConfig config) {
            super(config);
        }
    }

    @Before
    public void setUp() {
        factory = BeanSerializerFactory.instance;
        mapper = new ObjectMapper();
        serializerProvider = mapper.getSerializerProviderInstance();
    }

    // Tests withConfig when passing identical config returns the same instance
    @Test
    public void testWithConfig_sameConfig_returnsSameInstance() {
        SerializerFactoryConfig config = factory.getFactoryConfig();
        SerializerFactory result = factory.withConfig(config);
        assertSame(factory, result);
    }

    // Tests withConfig when passing new config returns a new BeanSerializerFactory instance
    @Test
    public void testWithConfig_newConfig_returnsNewInstance() {
        SerializerFactoryConfig newConfig = new SerializerFactoryConfig();
        SerializerFactory result = factory.withConfig(newConfig);
        assertNotNull(result);
        assertNotSame(factory, result);
        assertEquals(BeanSerializerFactory.class, result.getClass());
    }

    // Tests withConfig throws IllegalStateException when invoked on an improperly overridden subtype
    @Test(expected = IllegalStateException.class)
    public void testWithConfig_customSubtypeWithoutOverride_throwsIllegalStateException() {
        CustomSubtypeFactory customFactory = new CustomSubtypeFactory(new SerializerFactoryConfig());
        customFactory.withConfig(new SerializerFactoryConfig());
    }

    // Tests createSerializer for plain Object class returns unknown serializer
    @Test
    public void testCreateSerializer_plainObjectClass_returnsUnknownSerializer() throws Exception {
        JavaType objectType = mapper.constructType(Object.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, objectType);
        assertNotNull(ser);
    }

    // Tests createSerializer for a regular Java bean constructs a BeanSerializer
    @Test
    public void testCreateSerializer_simpleBean_returnsBeanSerializer() throws Exception {
        JavaType beanType = mapper.constructType(SimpleBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, beanType);
        assertNotNull(ser);
        assertEquals(BeanSerializer.class, ser.getClass());
    }

    // Tests createSerializer for an empty bean that has class annotations returns dummy serializer
    @Test
    public void testCreateSerializer_emptyBeanWithKnownAnnotations_returnsSerializer() throws Exception {
        JavaType emptyType = mapper.constructType(EmptyAnnotatedBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, emptyType);
        assertNotNull(ser);
    }

    // Tests constructBeanSerializer handling of @JsonAnyGetter
    @Test
    public void testCreateSerializer_withAnyGetter_constructsSerializerSuccessfully() throws Exception {
        JavaType anyGetterType = mapper.constructType(AnyGetterBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, anyGetterType);
        assertNotNull(ser);
        assertEquals(BeanSerializer.class, ser.getClass());
    }

    // Tests constructObjectIdHandler with PropertyGenerator reordering id property to first position
    @Test
    public void testCreateSerializer_withPropertyBasedObjectId_reordersIdProperty() throws Exception {
        JavaType idBeanType = mapper.constructType(ObjectIdBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, idBeanType);
        assertNotNull(ser);
        assertEquals(BeanSerializer.class, ser.getClass());
    }

    // Tests constructObjectIdHandler with Non-PropertyGenerator (e.g., IntSequenceGenerator)
    @Test
    public void testCreateSerializer_withStandardObjectIdGenerator_constructsObjectIdWriter() throws Exception {
        JavaType idBeanType = mapper.constructType(IntSequenceIdBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, idBeanType);
        assertNotNull(ser);
        assertEquals(BeanSerializer.class, ser.getClass());
    }

    // Tests filterBeanProperties via @JsonIgnoreProperties
    @Test
    public void testCreateSerializer_withIgnoredProperties_filtersOutProperty() throws Exception {
        JavaType ignoredType = mapper.constructType(IgnoredPropsBean.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(ignoredType);
        JsonSerializer<Object> ser = factory.findBeanSerializer(serializerProvider, ignoredType, beanDesc);
        assertNotNull(ser);
        assertTrue(ser instanceof BeanSerializer);
    }

    // Tests removeIgnorableTypes removes property whose type has @JsonIgnoreType
    @Test
    public void testCreateSerializer_withIgnorableType_removesIgnorableProperty() throws Exception {
        JavaType containerType = mapper.constructType(ContainerOfIgnorable.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, containerType);
        assertNotNull(ser);
        assertTrue(ser instanceof BeanSerializer);
    }

    // Tests removeSetterlessGetters when REQUIRE_SETTERS_FOR_GETTERS is enabled
    @Test
    public void testCreateSerializer_withRequireSettersForGetters_removesSetterlessProperties() throws Exception {
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.enable(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS);
        SerializerProvider prov = customMapper.getSerializerProviderInstance();

        JavaType type = customMapper.constructType(SetterlessBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(prov, type);
        assertNotNull(ser);
    }

    // Tests processViews with @JsonView properties
    @Test
    public void testCreateSerializer_withJsonView_configuresFilteredProperties() throws Exception {
        JavaType viewBeanType = mapper.constructType(ViewBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, viewBeanType);
        assertNotNull(ser);
        assertTrue(ser instanceof BeanSerializer);
    }

    // Tests findBeanSerializer returns null for non-bean primitive types
    @Test
    public void testFindBeanSerializer_primitiveType_returnsNull() throws Exception {
        JavaType intType = mapper.constructType(int.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(intType);
        JsonSerializer<Object> ser = factory.findBeanSerializer(serializerProvider, intType, beanDesc);
        assertNull(ser);
    }

    // Tests findBeanSerializer allows Enum types
    @Test
    public void testFindBeanSerializer_enumType_returnsSerializer() throws Exception {
        JavaType enumType = mapper.constructType(TestEnum.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(enumType);
        JsonSerializer<Object> ser = factory.findBeanSerializer(serializerProvider, enumType, beanDesc);
        assertNotNull(ser);
    }

    // Tests findPropertyTypeSerializer for polymorphic property
    @Test
    public void testFindPropertyTypeSerializer_polymorphicProperty_returnsTypeSerializer() throws Exception {
        JavaType containerType = mapper.constructType(PolymorphicContainer.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(containerType);
        SerializationConfig config = mapper.getSerializationConfig();

        AnnotatedMember fieldMember = null;
        for (BeanPropertyDefinition prop : beanDesc.findProperties()) {
            if ("polyProperty".equals(prop.getName())) {
                fieldMember = prop.getAccessor();
                break;
            }
        }
        assertNotNull(fieldMember);
        TypeSerializer typeSer = factory.findPropertyTypeSerializer(fieldMember.getType(beanDesc.bindingsForBeanType()), config, fieldMember);
        assertNotNull(typeSer);
    }

    // Tests findPropertyContentTypeSerializer for polymorphic collection property
    @Test
    public void testFindPropertyContentTypeSerializer_polymorphicContainerProperty_returnsTypeSerializer() throws Exception {
        JavaType containerType = mapper.constructType(PolymorphicContainer.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(containerType);
        SerializationConfig config = mapper.getSerializationConfig();

        AnnotatedMember fieldMember = null;
        for (BeanPropertyDefinition prop : beanDesc.findProperties()) {
            if ("polyList".equals(prop.getName())) {
                fieldMember = prop.getAccessor();
                break;
            }
        }
        assertNotNull(fieldMember);
        JavaType listType = fieldMember.getType(beanDesc.bindingsForBeanType());
        TypeSerializer contentSer = factory.findPropertyContentTypeSerializer(listType, config, fieldMember);
        assertNotNull(contentSer);
    }

    // Tests BeanSerializerModifier hooks in constructBeanSerializer
    @Test
    public void testCreateSerializer_withSerializerModifier_appliesModifierModifications() throws Exception {
        final boolean[] modifierCalled = new boolean[3];

        BeanSerializerModifier modifier = new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                modifierCalled[0] = true;
                return beanProperties;
            }

            @Override
            public List<BeanPropertyWriter> orderProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                modifierCalled[1] = true;
                return beanProperties;
            }

            @Override
            public BeanSerializerBuilder updateBuilder(SerializationConfig config, BeanDescription beanDesc, BeanSerializerBuilder builder) {
                modifierCalled[2] = true;
                return builder;
            }
        };

        SerializerFactoryConfig config = new SerializerFactoryConfig().withSerializerModifier(modifier);
        BeanSerializerFactory modifiedFactory = (BeanSerializerFactory) factory.withConfig(config);

        JavaType beanType = mapper.constructType(SimpleBean.class);
        JsonSerializer<Object> ser = modifiedFactory.createSerializer(serializerProvider, beanType);

        assertNotNull(ser);
        assertTrue(modifierCalled[0]);
        assertTrue(modifierCalled[1]);
        assertTrue(modifierCalled[2]);
    }

    // Tests createSerializer with additional custom Serializers configured
    @Test
    public void testCreateSerializer_withCustomSerializers_usesCustomSerializer() throws Exception {
        final JsonSerializer<Object> customSer = new StdSerializer<Object>(SimpleBean.class) {
            @Override
            public void serialize(Object value, com.fasterxml.jackson.core.JsonGenerator gen, SerializerProvider provider) {}
        };

        Serializers.Base additionalSerializers = new Serializers.Base() {
            @Override
            public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
                if (type.getRawClass() == SimpleBean.class) {
                    return customSer;
                }
                return null;
            }
        };

        SerializerFactoryConfig config = new SerializerFactoryConfig().withAdditionalSerializers(additionalSerializers);
        BeanSerializerFactory customFactory = (BeanSerializerFactory) factory.withConfig(config);

        JavaType beanType = mapper.constructType(SimpleBean.class);
        JsonSerializer<Object> ser = customFactory.createSerializer(serializerProvider, beanType);
        assertSame(customSer, ser);
    }

    // Tests BeanSerializerModifier modifySerializer hook
    @Test
    public void testCreateSerializer_withModifySerializerHook_replacesSerializer() throws Exception {
        final JsonSerializer<Object> replacement = new StdSerializer<Object>(SimpleBean.class) {
            @Override
            public void serialize(Object value, com.fasterxml.jackson.core.JsonGenerator gen, SerializerProvider provider) {}
        };

        BeanSerializerModifier modifier = new BeanSerializerModifier() {
            @Override
            public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
                return replacement;
            }
        };

        SerializerFactoryConfig config = new SerializerFactoryConfig().withSerializerModifier(modifier);
        BeanSerializerFactory modifiedFactory = (BeanSerializerFactory) factory.withConfig(config);

        JavaType beanType = mapper.constructType(SimpleBean.class);
        JsonSerializer<Object> ser = modifiedFactory.createSerializer(serializerProvider, beanType);
        assertSame(replacement, ser);
    }

    // Tests constructObjectIdHandler with non-existent property throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCreateSerializer_withInvalidPropertyObjectId_throwsIllegalArgumentException() throws Exception {
        JavaType invalidIdType = mapper.constructType(InvalidObjectIdBean.class);
        factory.createSerializer(serializerProvider, invalidIdType);
    }

    // Tests findPropertyTypeSerializer returns null when property has no type info
    @Test
    public void testFindPropertyTypeSerializer_nonPolymorphicProperty_returnsNull() throws Exception {
        JavaType containerType = mapper.constructType(NonPolymorphicContainer.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(containerType);
        SerializationConfig config = mapper.getSerializationConfig();

        AnnotatedMember fieldMember = null;
        for (BeanPropertyDefinition prop : beanDesc.findProperties()) {
            if ("plainProperty".equals(prop.getName())) {
                fieldMember = prop.getAccessor();
                break;
            }
        }
        assertNotNull(fieldMember);
        TypeSerializer typeSer = factory.findPropertyTypeSerializer(fieldMember.getType(beanDesc.bindingsForBeanType()), config, fieldMember);
        assertNull(typeSer);
    }

    // Tests findPropertyContentTypeSerializer returns null when container content has no type info
    @Test
    public void testFindPropertyContentTypeSerializer_nonPolymorphicContainerProperty_returnsNull() throws Exception {
        JavaType containerType = mapper.constructType(NonPolymorphicContainer.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(containerType);
        SerializationConfig config = mapper.getSerializationConfig();

        AnnotatedMember fieldMember = null;
        for (BeanPropertyDefinition prop : beanDesc.findProperties()) {
            if ("plainList".equals(prop.getName())) {
                fieldMember = prop.getAccessor();
                break;
            }
        }
        assertNotNull(fieldMember);
        JavaType listType = fieldMember.getType(beanDesc.bindingsForBeanType());
        TypeSerializer contentSer = factory.findPropertyContentTypeSerializer(listType, config, fieldMember);
        assertNull(contentSer);
    }

    // Tests createSerializer with empty bean when FAIL_ON_EMPTY_BEANS is disabled
    @Test
    public void testCreateSerializer_emptyBeanWithFailOnEmptyBeansDisabled_returnsSerializer() throws Exception {
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        SerializerProvider prov = customMapper.getSerializerProviderInstance();

        JavaType emptyType = customMapper.constructType(CompletelyEmptyBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(prov, emptyType);
        assertNotNull(ser);
    }

    // Tests findBeanSerializer returns null for container types like List and Map
    @Test
    public void testFindBeanSerializer_containerType_returnsNull() throws Exception {
        JavaType listType = mapper.constructType(List.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(listType);
        JsonSerializer<Object> ser = factory.findBeanSerializer(serializerProvider, listType, beanDesc);
        assertNull(ser);

        JavaType mapType = mapper.constructType(Map.class);
        BeanDescription mapDesc = mapper.getSerializationConfig().introspect(mapType);
        JsonSerializer<Object> mapSer = factory.findBeanSerializer(serializerProvider, mapType, mapDesc);
        assertNull(mapSer);
    }
}