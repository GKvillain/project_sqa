package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class BeanDeserializerFactoryTest {

    private ObjectMapper mapper;
    private DeserializationContext ctxt;
    private BeanDeserializerFactory factory;

    // Helper classes for testing
    static class SimpleBean {
        public String name;
        public int value;

        public SimpleBean() { }
        public SimpleBean(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    static class CustomException extends Throwable {
        private static final long serialVersionUID = 1L;
        private int code;

        public CustomException() { super(); }
        public CustomException(String msg) { super(msg); }
        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }
    }

    interface MyInterface {
        String getValue();
    }

    static class MyInterfaceImpl implements MyInterface {
        private String value;
        @Override
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    @JsonIgnoreProperties({ "ignoredProp" })
    static class IgnoredPropsBean {
        public String keptProp;
        public String ignoredProp;
    }

    static class AnySetterBean {
        private Map<String, Object> other = new HashMap<String, Object>();

        @com.fasterxml.jackson.annotation.JsonAnySetter
        public void setOther(String key, Object value) {
            other.put(key, value);
        }

        public Map<String, Object> getOther() {
            return other;
        }
    }

    @JsonDeserialize(builder = SimpleBuilderBean.Builder.class)
    static class SimpleBuilderBean {
        final String name;
        final int age;

        SimpleBuilderBean(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
        static class Builder {
            private String name;
            private int age;

            public Builder withName(String name) {
                this.name = name;
                return this;
            }

            public Builder withAge(int age) {
                this.age = age;
                return this;
            }

            public SimpleBuilderBean build() {
                return new SimpleBuilderBean(name, age);
            }
        }
    }

    static class CreatorBean {
        private final String name;
        private final int count;

        @JsonCreator
        public CreatorBean(@JsonProperty("name") String name, @JsonProperty("count") int count) {
            this.name = name;
            this.count = count;
        }

        public String getName() { return name; }
        public int getCount() { return count; }
    }

    static class SubFactory extends BeanDeserializerFactory {
        public SubFactory(DeserializerFactoryConfig config) {
            super(config);
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class IdentifiedBean {
        public int id;
        public String name;
        public IdentifiedBean next;
    }

    static class ParentBean {
        public String parentName;
        @JsonManagedReference
        public ChildBean child;
    }

    static class ChildBean {
        public String childName;
        @JsonBackReference
        public ParentBean parent;
    }

    static class Location {
        public double lat;
        public double lon;
    }

    static class PlaceBean {
        public String name;
        @JsonUnwrapped
        public Location location;
    }

    static class InjectedBean {
        public String name;
        @JacksonInject(value = "injectedValue")
        public String injected;
    }

    @JsonDeserialize(using = CustomBeanDeserializer.class)
    static class CustomAnnotatedBean {
        public String value;
    }

    static class CustomBeanDeserializer extends StdDeserializer<CustomAnnotatedBean> {
        public CustomBeanDeserializer() {
            super(CustomAnnotatedBean.class);
        }

        @Override
        public CustomAnnotatedBean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            CustomAnnotatedBean bean = new CustomAnnotatedBean();
            bean.value = "custom:" + p.getText();
            return bean;
        }
    }

    enum TestEnum {
        A, B
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        ctxt = mapper.getDeserializationContext();
        factory = BeanDeserializerFactory.instance;
    }

    // Tests singleton instance and withConfig behavior with identical config
    @Test
    public void testWithConfig_sameConfig_returnsSameInstance() {
        DeserializerFactoryConfig config = factory.getFactoryConfig();
        DeserializerFactory newFactory = factory.withConfig(config);
        assertSame(factory, newFactory);
    }

    // Tests withConfig behavior with new configuration
    @Test
    public void testWithConfig_newConfig_returnsNewInstance() {
        DeserializerFactoryConfig newConfig = new DeserializerFactoryConfig();
        DeserializerFactory newFactory = factory.withConfig(newConfig);
        assertNotNull(newFactory);
        assertNotSame(factory, newFactory);
    }

    // Tests subtype without proper withConfig override throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testWithConfig_subclassWithoutOverride_throwsException() {
        SubFactory subFactory = new SubFactory(new DeserializerFactoryConfig());
        subFactory.withConfig(new DeserializerFactoryConfig());
    }

    // Tests standard bean deserializer creation
    @Test
    public void testCreateBeanDeserializer_simpleBean_createsDeserializer() throws Exception {
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = factory.createBeanDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);
    }

    // Tests Throwable deserializer creation and cause handling
    @Test
    public void testCreateBeanDeserializer_throwableType_createsThrowableDeserializer() throws Exception {
        JavaType type = mapper.constructType(CustomException.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = factory.createBeanDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);

        CustomException ex = mapper.readValue("{\"message\":\"test error\",\"code\":404}", CustomException.class);
        assertNotNull(ex);
        assertEquals("test error", ex.getMessage());
        assertEquals(404, ex.getCode());
    }

    // Tests abstract type resolution and materialization
    @Test
    public void testCreateBeanDeserializer_abstractTypeWithResolver_resolvesAndCreatesDeserializer() throws Exception {
        SimpleModule module = new SimpleModule();
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(MyInterface.class, MyInterfaceImpl.class);
        module.setAbstractTypes(resolver);
        mapper.registerModule(module);

        MyInterface result = mapper.readValue("{\"value\":\"hello\"}", MyInterface.class);
        assertNotNull(result);
        assertTrue(result instanceof MyInterfaceImpl);
        assertEquals("hello", result.getValue());
    }

    // Tests potential bean type validation for primitive types
    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_primitiveType_throwsException() {
        factory.isPotentialBeanType(int.class);
    }

    // Tests potential bean type validation for array types
    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_arrayType_throwsException() {
        factory.isPotentialBeanType(int[].class);
    }

    // Tests potential bean type validation for valid class
    @Test
    public void testIsPotentialBeanType_validClass_returnsTrue() {
        boolean isBean = factory.isPotentialBeanType(SimpleBean.class);
        assertTrue(isBean);
    }

    // Tests builder-based deserializer creation
    @Test
    public void testCreateBuilderBasedDeserializer_builderAnnotatedBean_deserializesCorrectly() throws Exception {
        SimpleBuilderBean result = mapper.readValue("{\"name\":\"Alice\",\"age\":30}", SimpleBuilderBean.class);
        assertNotNull(result);
        assertEquals("Alice", result.name);
        assertEquals(30, result.age);
    }

    // Tests deserialization with @JsonIgnoreProperties
    @Test
    public void testCreateBeanDeserializer_ignoredProperties_ignoresSpecifiedProperty() throws Exception {
        IgnoredPropsBean result = mapper.readValue("{\"keptProp\":\"kept\",\"ignoredProp\":\"ignoreMe\"}", IgnoredPropsBean.class);
        assertNotNull(result);
        assertEquals("kept", result.keptProp);
        assertNull(result.ignoredProp);
    }

    // Tests deserialization with @JsonAnySetter
    @Test
    public void testCreateBeanDeserializer_anySetter_capturesUnknownProperties() throws Exception {
        AnySetterBean result = mapper.readValue("{\"foo\":\"bar\",\"count\":123}", AnySetterBean.class);
        assertNotNull(result);
        assertEquals("bar", result.getOther().get("foo"));
        assertEquals(123, result.getOther().get("count"));
    }

    // Tests deserialization using @JsonCreator
    @Test
    public void testCreateBeanDeserializer_creatorProperties_bindsParameters() throws Exception {
        CreatorBean result = mapper.readValue("{\"name\":\"item1\",\"count\":5}", CreatorBean.class);
        assertNotNull(result);
        assertEquals("item1", result.getName());
        assertEquals(5, result.getCount());
    }

    // Tests DeserializerModifier integration in BeanDeserializerFactory
    @Test
    public void testBeanDeserializerModifier_modifiesDeserializerBuilder() throws Exception {
        final boolean[] modified = new boolean[1];
        BeanDeserializerModifier modifier = new BeanDeserializerModifier() {
            @Override
            public BeanDeserializerBuilder updateBuilder(DeserializationConfig config,
                    BeanDescription beanDesc, BeanDeserializerBuilder builder) {
                modified[0] = true;
                return builder;
            }
        };

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withDeserializerModifier(modifier);
        BeanDeserializerFactory customFactory = (BeanDeserializerFactory) factory.withConfig(config);

        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = customFactory.createBeanDeserializer(ctxt, type, beanDesc);

        assertNotNull(deser);
        assertTrue(modified[0]);
    }

    // Tests isPotentialBeanType on non-bean types such as enums
    @Test
    public void testIsPotentialBeanType_enumType_returnsFalse() {
        assertFalse(factory.isPotentialBeanType(TestEnum.class));
    }

    // Tests creation and deserialization with ObjectId info
    @Test
    public void testCreateBeanDeserializer_withObjectId() throws Exception {
        String json = "{\"id\":1,\"name\":\"first\",\"next\":1}";
        IdentifiedBean bean = mapper.readValue(json, IdentifiedBean.class);
        assertNotNull(bean);
        assertEquals(1, bean.id);
        assertEquals("first", bean.name);
        assertSame(bean, bean.next);
    }

    // Tests creation and deserialization with managed/back references
    @Test
    public void testCreateBeanDeserializer_withBackReference() throws Exception {
        String json = "{\"parentName\":\"Dad\",\"child\":{\"childName\":\"Junior\"}}";
        ParentBean parent = mapper.readValue(json, ParentBean.class);
        assertNotNull(parent);
        assertEquals("Dad", parent.parentName);
        assertNotNull(parent.child);
        assertEquals("Junior", parent.child.childName);
        assertSame(parent, parent.child.parent);
    }

    // Tests creation and deserialization with unwrapped properties
    @Test
    public void testCreateBeanDeserializer_withUnwrappedProperty() throws Exception {
        String json = "{\"name\":\"Tower\",\"lat\":13.75,\"lon\":100.50}";
        PlaceBean place = mapper.readValue(json, PlaceBean.class);
        assertNotNull(place);
        assertEquals("Tower", place.name);
        assertNotNull(place.location);
        assertEquals(13.75, place.location.lat, 0.001);
        assertEquals(100.50, place.location.lon, 0.001);
    }

    // Tests creation and deserialization with JacksonInject
    @Test
    public void testCreateBeanDeserializer_withInjectedValues() throws Exception {
        InjectableValues.Std injectables = new InjectableValues.Std();
        injectables.addValue("injectedValue", "injected-data");
        ObjectMapper injectedMapper = mapper.copy().setInjectableValues(injectables);

        InjectedBean result = injectedMapper.readValue("{\"name\":\"test\"}", InjectedBean.class);
        assertNotNull(result);
        assertEquals("test", result.name);
        assertEquals("injected-data", result.injected);
    }

    // Tests creation with custom ValueInstantiator from DeserializerFactoryConfig
    @Test
    public void testCreateBeanDeserializer_withCustomValueInstantiator() throws Exception {
        final boolean[] instantiated = new boolean[1];
        ValueInstantiator customInstantiator = new StdValueInstantiator(
                mapper.getDeserializationConfig(), mapper.constructType(SimpleBean.class)) {
            @Override
            public boolean canCreateUsingDefault() {
                return true;
            }

            @Override
            public Object createUsingDefault(DeserializationContext ctxt) {
                instantiated[0] = true;
                SimpleBean bean = new SimpleBean();
                bean.name = "default-from-instantiator";
                return bean;
            }
        };

        ValueInstantiators vi = new ValueInstantiators.Base() {
            @Override
            public ValueInstantiator findValueInstantiator(DeserializationConfig config,
                    BeanDescription beanDesc, ValueInstantiator defaultInstantiator) {
                if (beanDesc.getBeanClass() == SimpleBean.class) {
                    return customInstantiator;
                }
                return defaultInstantiator;
            }
        };

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withValueInstantiators(vi);
        BeanDeserializerFactory customFactory = (BeanDeserializerFactory) factory.withConfig(config);

        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = customFactory.createBeanDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);

        SimpleBean bean = (SimpleBean) deser.deserialize(
                mapper.getFactory().createParser("{\"value\":42}"), ctxt);
        assertNotNull(bean);
        assertTrue(instantiated[0]);
        assertEquals("default-from-instantiator", bean.name);
        assertEquals(42, bean.value);
    }

    // Tests modifier hooks: modifyDeserializer and updateProperties
    @Test
    public void testBeanDeserializerModifier_updatePropertiesAndModifyDeserializer() throws Exception {
        final boolean[] updatedProps = new boolean[1];
        final boolean[] modifiedDeser = new boolean[1];

        BeanDeserializerModifier modifier = new BeanDeserializerModifier() {
            @Override
            public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config,
                    BeanDescription beanDesc, List<BeanPropertyDefinition> propDefs) {
                updatedProps[0] = true;
                return propDefs;
            }

            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                    BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                modifiedDeser[0] = true;
                return deserializer;
            }
        };

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withDeserializerModifier(modifier);
        BeanDeserializerFactory customFactory = (BeanDeserializerFactory) factory.withConfig(config);

        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = customFactory.createBeanDeserializer(ctxt, type, beanDesc);

        assertNotNull(deser);
        assertTrue(updatedProps[0]);
        assertTrue(modifiedDeser[0]);
    }

    // Tests custom deserializer specified via annotation is respected
    @Test
    public void testCreateBeanDeserializer_annotatedCustomDeserializer() throws Exception {
        CustomAnnotatedBean result = mapper.readValue("\"custom-input\"", CustomAnnotatedBean.class);
        assertNotNull(result);
        assertEquals("custom:custom-input", result.value);
    }
}