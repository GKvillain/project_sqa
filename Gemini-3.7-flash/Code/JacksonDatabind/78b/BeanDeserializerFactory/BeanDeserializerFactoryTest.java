package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.util.*;

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
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.std.ThrowableDeserializer;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class BeanDeserializerFactoryTest {

    private BeanDeserializerFactory factory;
    private ObjectMapper mapper;
    private DeserializationContext ctxt;

    @Before
    public void setUp() {
        factory = BeanDeserializerFactory.instance;
        mapper = new ObjectMapper();
        ctxt = mapper.getDeserializationContext();
    }

    // Helper classes for testing
    static class SimpleBean {
        private String name;
        private int age;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    static class CustomException extends Throwable {
        private static final long serialVersionUID = 1L;
        public CustomException(String msg) { super(msg); }
    }

    @JsonIgnoreProperties({"ignoredField"})
    static class IgnoredPropsBean {
        public String normalField;
        public String ignoredField;
    }

    interface AbstractInterface {
        String getValue();
    }

    static class ConcreteImpl implements AbstractInterface {
        private String value;
        public String getValue() { return value; }
        public void setValue(String v) { this.value = v; }
    }

    @JsonDeserialize(builder = SimpleBuilder.class)
    static class ValueClass {
        final int x;
        ValueClass(int x) { this.x = x; }
    }

    @JsonPOJOBuilder(buildMethodName = "create", withPrefix = "with")
    static class SimpleBuilder {
        private int x;
        public SimpleBuilder withX(int x) { this.x = x; return this; }
        public ValueClass create() { return new ValueClass(x); }
    }

    static class SubclassedFactory extends BeanDeserializerFactory {
        private static final long serialVersionUID = 1L;
        public SubclassedFactory(DeserializerFactoryConfig config) {
            super(config);
        }
    }

    static class AnySetterBean {
        private Map<String, Object> properties = new HashMap<String, Object>();

        @JsonAnySetter
        public void set(String name, Object value) {
            properties.put(name, value);
        }
    }

    static class CreatorBean {
        final String name;
        final int value;

        @JsonCreator
        public CreatorBean(@JsonProperty("name") String name, @JsonProperty("value") int value) {
            this.name = name;
            this.value = value;
        }
    }

    static class InjectedBean {
        @JacksonInject
        public String injectedValue;
        public String regularValue;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class IdentifiedBean {
        public int id;
        public String name;
    }

    static class ParentBean {
        public String name;
        @JsonManagedReference
        public ChildBean child;
    }

    static class ChildBean {
        public String title;
        @JsonBackReference
        public ParentBean parent;
    }

    // Tests withConfig with identical config returns the same instance
    @Test
    public void testWithConfig_sameConfig_returnsSameInstance() {
        DeserializerFactoryConfig config = factory.getFactoryConfig();
        DeserializerFactory result = factory.withConfig(config);
        assertSame(factory, result);
    }

    // Tests withConfig with new config returns new factory instance
    @Test
    public void testWithConfig_newConfig_returnsNewInstance() {
        DeserializerFactoryConfig newConfig = new DeserializerFactoryConfig();
        DeserializerFactory result = factory.withConfig(newConfig);
        assertNotNull(result);
        assertNotSame(factory, result);
        assertTrue(result instanceof BeanDeserializerFactory);
    }

    // Tests withConfig on improperly overridden subtype throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testWithConfig_subtypeWithoutOverride_throwsIllegalStateException() {
        SubclassedFactory customFactory = new SubclassedFactory(new DeserializerFactoryConfig());
        customFactory.withConfig(new DeserializerFactoryConfig());
    }

    // Tests isPotentialBeanType on regular class returns true
    @Test
    public void testIsPotentialBeanType_regularClass_returnsTrue() {
        assertTrue(factory.isPotentialBeanType(SimpleBean.class));
    }

    // Tests isPotentialBeanType on primitive type throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_primitiveType_throwsIllegalArgumentException() {
        factory.isPotentialBeanType(int.class);
    }

    // Tests isPotentialBeanType on array type throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_arrayType_throwsIllegalArgumentException() {
        factory.isPotentialBeanType(int[].class);
    }

    // Tests createBeanDeserializer for regular POJO builds a valid deserializer
    @Test
    public void testCreateBeanDeserializer_regularPOJO_returnsBeanDeserializer() throws JsonMappingException {
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = factory.createBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
        assertTrue(deser instanceof BeanDeserializer);
    }

    // Tests createBeanDeserializer for Throwable types builds ThrowableDeserializer
    @Test
    public void testCreateBeanDeserializer_throwableType_returnsThrowableDeserializer() throws JsonMappingException {
        JavaType type = mapper.constructType(CustomException.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = factory.createBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
        assertTrue(deser instanceof ThrowableDeserializer);
    }

    // Tests createBeanDeserializer with registered custom deserializer returns custom instance
    @Test
    public void testCreateBeanDeserializer_customDeserializer_returnsCustom() throws IOException {
        SimpleModule module = new SimpleModule();
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> customDeser = (JsonDeserializer<Object>) (JsonDeserializer<?>) new JsonDeserializer<SimpleBean>() {
            @Override
            public SimpleBean deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) {
                return new SimpleBean();
            }
        };
        SimpleDeserializers desers = new SimpleDeserializers();
        desers.addDeserializer(SimpleBean.class, customDeser);
        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withAdditionalDeserializers(desers);
        BeanDeserializerFactory customFactory = (BeanDeserializerFactory) factory.withConfig(config);

        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> result = customFactory.createBeanDeserializer(ctxt, type, desc);
        assertSame(customDeser, result);
    }

    // Tests createBeanDeserializer with AbstractTypeResolver resolves interface
    @Test
    public void testCreateBeanDeserializer_abstractTypeResolved_returnsConcreteDeserializer() throws JsonMappingException {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(AbstractInterface.class, ConcreteImpl.class);
        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withAbstractTypeResolver(resolver);
        BeanDeserializerFactory customFactory = (BeanDeserializerFactory) factory.withConfig(config);

        JavaType type = mapper.constructType(AbstractInterface.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = customFactory.createBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
        assertTrue(deser instanceof BeanDeserializer);
    }

    // Tests createBeanDeserializer for abstract type without resolver returns abstract deserializer
    @Test
    public void testCreateBeanDeserializer_abstractTypeWithoutResolver_returnsAbstractDeserializer() throws JsonMappingException {
        JavaType type = mapper.constructType(AbstractInterface.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = factory.createBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
        assertTrue(deser instanceof AbstractDeserializer);
    }

    // Tests createBuilderBasedDeserializer constructs deserializer with custom builder
    @Test
    public void testCreateBuilderBasedDeserializer_validBuilder_returnsBuilderBasedDeserializer() throws JsonMappingException {
        JavaType valueType = mapper.constructType(ValueClass.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(valueType);
        JsonDeserializer<Object> deser = factory.createBuilderBasedDeserializer(ctxt, valueType, beanDesc);
        assertNotNull(deser);
        assertTrue(deser instanceof BuilderBasedDeserializer);
    }

    // Tests buildBeanDeserializer handles creator properties properly
    @Test
    public void testBuildBeanDeserializer_withCreatorProperties_buildsSuccessfully() throws JsonMappingException {
        JavaType type = mapper.constructType(CreatorBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
        assertTrue(deser instanceof BeanDeserializer);
    }

    // Tests buildBeanDeserializer handles @JsonIgnoreProperties annotations
    @Test
    public void testBuildBeanDeserializer_withIgnoredProperties_buildsSuccessfully() throws JsonMappingException {
        JavaType type = mapper.constructType(IgnoredPropsBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
        assertTrue(deser instanceof BeanDeserializer);
    }

    // Tests DeserializerModifier updateBuilder and modifyDeserializer lifecycle
    @Test
    public void testBuildBeanDeserializer_withDeserializerModifier_appliesModifier() throws JsonMappingException {
        final boolean[] modifierCalled = new boolean[2];
        BeanDeserializerModifier modifier = new BeanDeserializerModifier() {
            @Override
            public BeanDeserializerBuilder updateBuilder(DeserializationConfig config,
                    BeanDescription beanDesc, BeanDeserializerBuilder builder) {
                modifierCalled[0] = true;
                return builder;
            }

            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                    BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                modifierCalled[1] = true;
                return deserializer;
            }
        };

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withDeserializerModifier(modifier);
        BeanDeserializerFactory customFactory = (BeanDeserializerFactory) factory.withConfig(config);

        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = customFactory.buildBeanDeserializer(ctxt, type, desc);

        assertNotNull(deser);
        assertTrue("updateBuilder should have been called", modifierCalled[0]);
        assertTrue("modifyDeserializer should have been called", modifierCalled[1]);
    }

    // Tests buildBeanDeserializer with AnySetter
    @Test
    public void testBuildBeanDeserializer_withAnySetter_buildsSuccessfully() throws JsonMappingException {
        JavaType type = mapper.constructType(AnySetterBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
        assertTrue(deser instanceof BeanDeserializer);
    }

    // Tests buildBeanDeserializer with JacksonInject annotations
    @Test
    public void testBuildBeanDeserializer_withInjectables_buildsSuccessfully() throws JsonMappingException {
        JavaType type = mapper.constructType(InjectedBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
        assertTrue(deser instanceof BeanDeserializer);
    }

    // Tests buildBeanDeserializer with Object Id / Identity Info
    @Test
    public void testBuildBeanDeserializer_withObjectId_buildsSuccessfully() throws JsonMappingException {
        JavaType type = mapper.constructType(IdentifiedBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
        assertTrue(deser instanceof BeanDeserializer);
    }

    // Tests buildBeanDeserializer with BackReference properties
    @Test
    public void testBuildBeanDeserializer_withBackReference_buildsSuccessfully() throws JsonMappingException {
        JavaType type = mapper.constructType(ChildBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = factory.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
        assertTrue(deser instanceof BeanDeserializer);
    }

    // Tests buildThrowableDeserializer explicitly
    @Test
    public void testBuildThrowableDeserializer_buildsSuccessfully() throws JsonMappingException {
        JavaType type = mapper.constructType(CustomException.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = factory.buildThrowableDeserializer(ctxt, type, desc);
        assertNotNull(deser);
        assertTrue(deser instanceof ThrowableDeserializer);
    }

    // Tests constructBeanDeserializerBuilder
    @Test
    public void testConstructBeanDeserializerBuilder_returnsBuilderInstance() {
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        BeanDeserializerBuilder builder = factory.constructBeanDeserializerBuilder(ctxt, desc);
        assertNotNull(builder);
    }
}