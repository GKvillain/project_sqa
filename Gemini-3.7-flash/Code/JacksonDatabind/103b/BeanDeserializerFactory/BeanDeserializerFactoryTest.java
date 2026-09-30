package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class BeanDeserializerFactoryTest {

    private BeanDeserializerFactory factory;
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        factory = BeanDeserializerFactory.instance;
        mapper = new ObjectMapper();
    }

    // Helper dummy classes for tests
    static class SimpleBean {
        public String name;
        public int age;

        public SimpleBean() {}
        public SimpleBean(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    @JsonIgnoreProperties({"ignoredField"})
    static class IgnoredPropsBean {
        public String validField;
        public String ignoredField;
    }

    static class SetterlessBean {
        private java.util.List<String> list = new java.util.ArrayList<String>();

        public java.util.List<String> getList() {
            return list;
        }
    }

    static class CustomException extends Throwable {
        private static final long serialVersionUID = 1L;
        public CustomException() { super(); }
        public CustomException(String msg) { super(msg); }
    }

    @JsonDeserialize(builder = ValueClass.Builder.class)
    static class ValueClass {
        final int value;

        ValueClass(int v) { this.value = v; }

        @JsonPOJOBuilder(withPrefix = "set")
        static class Builder {
            int value;

            public Builder setValue(int v) {
                this.value = v;
                return this;
            }

            public ValueClass build() {
                return new ValueClass(value);
            }
        }
    }

    static class CreatorBean {
        final String name;

        @JsonCreator
        public CreatorBean(@JsonProperty("name") String name) {
            this.name = name;
        }
    }

    // Additional helper dummy classes for extended test coverage
    static class Views {
        static class Public {}
        static class Internal extends Public {}
    }

    static class ViewBean {
        @JsonView(Views.Public.class)
        public String publicField;

        @JsonView(Views.Internal.class)
        public String internalField;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class IdentifiedBean {
        public int id;
        public String name;
    }

    static class ParentBean {
        public int id;
        @JsonManagedReference
        public ChildBean child;
    }

    static class ChildBean {
        public String name;
        @JsonBackReference
        public ParentBean parent;
    }

    static class InjectedBean {
        @JacksonInject("injectedVal")
        public String injected;
        public String normal;
    }

    static class AnySetterBean {
        public String normal;
        private Map<String, Object> extra = new HashMap<String, Object>();

        @JsonAnySetter
        public void setExtra(String key, Object value) {
            extra.put(key, value);
        }

        public Map<String, Object> getExtra() {
            return extra;
        }
    }

    static class Location {
        public String city;
        public String country;
    }

    static class UnwrappedBean {
        public String name;
        @JsonUnwrapped
        public Location location;
    }

    interface MyInterface {
        String getValue();
    }

    static class MyInterfaceImpl implements MyInterface {
        private String value;
        public void setValue(String v) { this.value = v; }
        @Override
        public String getValue() { return value; }
    }

    enum TestEnum {
        A, B
    }

    // Tests withConfig with same configuration instance returns this
    @Test
    public void testWithConfig_sameConfig_returnsSameInstance() {
        DeserializerFactoryConfig config = factory.getFactoryConfig();
        DeserializerFactory newFactory = factory.withConfig(config);
        assertSame(factory, newFactory);
    }

    // Tests withConfig with new configuration instance returns a new instance
    @Test
    public void testWithConfig_newConfig_returnsNewInstance() {
        DeserializerFactoryConfig config = new DeserializerFactoryConfig();
        DeserializerFactory newFactory = factory.withConfig(config);
        assertNotSame(factory, newFactory);
        assertTrue(newFactory instanceof BeanDeserializerFactory);
    }

    // Tests creating deserializer for standard POJO class
    @Test
    public void testCreateBeanDeserializer_simplePOJO_returnsValidDeserializer() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = factory.createBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
        assertTrue(deser.isCachable());
    }

    // Tests creating deserializer for Throwable subclass
    @Test
    public void testCreateBeanDeserializer_throwableType_returnsThrowableDeserializer() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(CustomException.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = factory.createBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
    }

    // Tests creating deserializer for builder-based class
    @Test
    public void testCreateBuilderBasedDeserializer_validBuilder_returnsDeserializer() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(ValueClass.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = factory.createBuilderBasedDeserializer(ctxt, type, desc, ValueClass.Builder.class);
        assertNotNull(deser);
    }

    // Tests non-potential bean type throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_primitiveType_throwsIllegalArgumentException() {
        factory.isPotentialBeanType(int.class);
    }

    // Tests non-potential bean type for array class
    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_arrayType_throwsIllegalArgumentException() {
        factory.isPotentialBeanType(String[].class);
    }

    // Tests building deserializer with creator properties
    @Test
    public void testBuildBeanDeserializer_creatorBean_constructsDeserializer() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(CreatorBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
    }

    // Tests deserializer creation with ignored properties
    @Test
    public void testBuildBeanDeserializer_ignoredProperties_constructsDeserializer() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(IgnoredPropsBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
    }

    // Tests deserializer creation for setterless collections
    @Test
    public void testBuildBeanDeserializer_setterlessBean_constructsDeserializer() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SetterlessBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
    }

    // Tests abstract type materialization when no resolver present
    @Test
    public void testMaterializeAbstractType_noResolver_returnsNull() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(CharSequence.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JavaType result = factory.materializeAbstractType(ctxt, type, desc);
        assertNull(result);
    }

    // Tests deserializer modifier hook in DeserializerFactoryConfig
    @Test
    public void testCreateBeanDeserializer_withModifier_appliesModifier() throws Exception {
        final boolean[] modifierCalled = new boolean[1];
        BeanDeserializerModifier modifier = new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                modifierCalled[0] = true;
                return super.modifyDeserializer(config, beanDesc, deserializer);
            }
        };

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withDeserializerModifier(modifier);
        BeanDeserializerFactory customFactory = new BeanDeserializerFactory(config);

        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = customFactory.createBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
        assertTrue(modifierCalled[0]);
    }

    // Tests full end-to-end deserialization via ObjectMapper with BeanDeserializerFactory
    @Test
    public void testFullDeserialization_jsonToBean_succeeds() throws IOException {
        String json = "{\"name\":\"Alice\",\"age\":30}";
        SimpleBean result = mapper.readValue(json, SimpleBean.class);
        assertNotNull(result);
        assertEquals("Alice", result.name);
        assertEquals(30, result.age);
    }

    // --- New Tests for Full Coverage ---

    @Test
    public void testBuildBeanDeserializer_withViews() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(ViewBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"publicField\":\"pub\",\"internalField\":\"priv\"}";
        ViewBean bean = mapper.readerWithView(Views.Public.class).forType(ViewBean.class).readValue(json);
        assertEquals("pub", bean.publicField);
        assertNull(bean.internalField);
    }

    @Test
    public void testBuildBeanDeserializer_withObjectIdReader() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(IdentifiedBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"id\":123,\"name\":\"Identified\"}";
        IdentifiedBean bean = mapper.readValue(json, IdentifiedBean.class);
        assertEquals(123, bean.id);
        assertEquals("Identified", bean.name);
    }

    @Test
    public void testBuildBeanDeserializer_withBackAndManagedReferences() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(ParentBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"id\":1,\"child\":{\"name\":\"Kid\"}}";
        ParentBean parent = mapper.readValue(json, ParentBean.class);
        assertNotNull(parent);
        assertNotNull(parent.child);
        assertSame(parent, parent.child.parent);
    }

    @Test
    public void testBuildBeanDeserializer_withInjectables() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(InjectedBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        InjectableValues.Std injectables = new InjectableValues.Std();
        injectables.addValue("injectedVal", "InjectedValue");

        InjectedBean bean = mapper.reader(injectables).forType(InjectedBean.class).readValue("{\"normal\":\"NormalValue\"}");
        assertEquals("NormalValue", bean.normal);
        assertEquals("InjectedValue", bean.injected);
    }

    @Test
    public void testBuildBeanDeserializer_withAnySetter() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(AnySetterBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"normal\":\"val\",\"extra1\":\"val1\",\"extra2\":\"val2\"}";
        AnySetterBean bean = mapper.readValue(json, AnySetterBean.class);
        assertEquals("val", bean.normal);
        assertEquals("val1", bean.getExtra().get("extra1"));
        assertEquals("val2", bean.getExtra().get("extra2"));
    }

    @Test
    public void testBuildBeanDeserializer_withUnwrappedProperty() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(UnwrappedBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);

        String json = "{\"name\":\"Bob\",\"city\":\"Bangkok\",\"country\":\"Thailand\"}";
        UnwrappedBean bean = mapper.readValue(json, UnwrappedBean.class);
        assertEquals("Bob", bean.name);
        assertNotNull(bean.location);
        assertEquals("Bangkok", bean.location.city);
        assertEquals("Thailand", bean.location.country);
    }

    @Test
    public void testMaterializeAbstractType_withResolver_resolvesConcreteType() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(MyInterface.class, MyInterfaceImpl.class);

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withAbstractTypeResolver(resolver);
        BeanDeserializerFactory customFactory = new BeanDeserializerFactory(config);

        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(MyInterface.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JavaType materialized = customFactory.materializeAbstractType(ctxt, type, desc);
        assertNotNull(materialized);
        assertEquals(MyInterfaceImpl.class, materialized.getRawClass());
    }

    @Test
    public void testCreateBeanDeserializer_builderModifier_invokesUpdateBuilder() throws Exception {
        final boolean[] updateBuilderCalled = new boolean[1];
        BeanDeserializerModifier modifier = new BeanDeserializerModifier() {
            @Override
            public BeanDeserializerBuilder updateBuilder(DeserializationConfig config, BeanDescription beanDesc, BeanDeserializerBuilder builder) {
                updateBuilderCalled[0] = true;
                return super.updateBuilder(config, beanDesc, builder);
            }
        };

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withDeserializerModifier(modifier);
        BeanDeserializerFactory customFactory = new BeanDeserializerFactory(config);

        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = customFactory.createBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
        assertTrue(updateBuilderCalled[0]);
    }

    @Test
    public void testIsPotentialBeanType_validAndInvalidTypes() {
        assertNull(factory.isPotentialBeanType(SimpleBean.class));
        assertNotNull(factory.isPotentialBeanType(TestEnum.class));
        assertNotNull(factory.isPotentialBeanType(Object.class));
    }
}