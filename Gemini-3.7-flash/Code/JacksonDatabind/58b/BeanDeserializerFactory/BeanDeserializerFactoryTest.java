package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.std.ThrowableDeserializer;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class BeanDeserializerFactoryTest {

    private BeanDeserializerFactory factory;
    private ObjectMapper mapper;
    private DeserializationContext ctxt;

    // Helper classes for testing
    static class SimpleBean {
        public String name;
        public int age;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    static class SetterlessBean {
        private List<String> items = new ArrayList<String>();
        public List<String> getItems() { return items; }
    }

    static class CustomException extends Throwable {
        private static final long serialVersionUID = 1L;
        public CustomException(String msg) { super(msg); }
    }

    interface AbstractService { }

    static class ConcreteService implements AbstractService { }

    @JsonDeserialize(builder = ValueClassBuilder.class)
    static class ValueClass {
        final int value;
        ValueClass(int v) { this.value = v; }
    }

    @JsonPOJOBuilder(buildMethodName = "create", withPrefix = "set")
    static class ValueClassBuilder {
        private int val;
        public ValueClassBuilder setVal(int v) { this.val = v; return this; }
        public ValueClass create() { return new ValueClass(val); }
    }

    static class SubFactory extends BeanDeserializerFactory {
        public SubFactory(DeserializerFactoryConfig config) {
            super(config);
        }
    }

    static class CustomModifier extends BeanDeserializerModifier { }

    @Before
    public void setUp() {
        factory = BeanDeserializerFactory.instance;
        mapper = new ObjectMapper();
        ctxt = mapper.getDeserializationContext();
    }

    // Tests withConfig with same configuration instance returns this
    @Test
    public void testWithConfig_sameConfig_returnsSameInstance() {
        DeserializerFactoryConfig config = factory.getFactoryConfig();
        DeserializerFactory result = factory.withConfig(config);
        assertSame(factory, result);
    }

    // Tests withConfig with different configuration returns new factory
    @Test
    public void testWithConfig_differentConfig_returnsNewInstance() {
        DeserializerFactoryConfig newConfig = new DeserializerFactoryConfig();
        DeserializerFactory result = factory.withConfig(newConfig);
        assertNotSame(factory, result);
        assertTrue(result instanceof BeanDeserializerFactory);
    }

    // Tests withConfig when called on subclass throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testWithConfig_subclassWithoutOverride_throwsIllegalStateException() {
        SubFactory subFactory = new SubFactory(new DeserializerFactoryConfig());
        subFactory.withConfig(new DeserializerFactoryConfig());
    }

    // Tests createBeanDeserializer with standard POJO bean
    @Test
    public void testCreateBeanDeserializer_standardPojo_returnsBeanDeserializer() throws JsonMappingException {
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = factory.createBeanDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);
        assertTrue(deser instanceof BeanDeserializer);
    }

    // Tests buildThrowableDeserializer creates ThrowableDeserializer
    @Test
    public void testBuildThrowableDeserializer_throwableClass_returnsThrowableDeserializer() throws JsonMappingException {
        JavaType type = mapper.constructType(CustomException.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = factory.buildThrowableDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);
        assertTrue(deser instanceof ThrowableDeserializer);
    }

    // Tests Jackson #877 / Defects4J 58b: Deserializing Exception with security/access configuration
    @Test
    public void testCreateBeanDeserializer_throwableSubclass_deserializesCorrectly() throws IOException {
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.enable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
        customMapper.enable(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS);
        
        CustomException ex = customMapper.readValue("{\"message\":\"test error\"}", CustomException.class);
        assertNotNull(ex);
        assertEquals("test error", ex.getMessage());
    }

    // Tests createBeanDeserializer for abstract type with registered resolver
    @Test
    public void testCreateBeanDeserializer_abstractTypeWithResolver_returnsDeserializer() throws JsonMappingException {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(AbstractService.class, ConcreteService.class);

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withAbstractTypeResolver(resolver);
        BeanDeserializerFactory customFactory = new BeanDeserializerFactory(config);

        JavaType type = mapper.constructType(AbstractService.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = customFactory.createBeanDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);
        assertTrue(deser instanceof BeanDeserializer);
    }

    // Tests isPotentialBeanType with non-bean primitive type
    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_primitiveType_throwsException() {
        factory.isPotentialBeanType(int.class);
    }

    // Tests isPotentialBeanType with valid bean type
    @Test
    public void testIsPotentialBeanType_validBean_returnsTrue() {
        boolean result = factory.isPotentialBeanType(SimpleBean.class);
        assertTrue(result);
    }

    // Tests createBuilderBasedDeserializer creates valid builder deserializer
    @Test
    public void testCreateBuilderBasedDeserializer_customPOJOBuilder_returnsDeserializer() throws Exception {
        JavaType type = mapper.constructType(ValueClass.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = factory.createBuilderBasedDeserializer(
                ctxt, type, beanDesc, ValueClassBuilder.class);
        assertNotNull(deser);
        assertTrue(deser instanceof BuilderBasedDeserializer);
    }

    // Tests setterless property handling on BeanDeserializer creation
    @Test
    public void testCreateBeanDeserializer_setterlessProperty_constructsSuccessfully() throws JsonMappingException {
        JavaType type = mapper.constructType(SetterlessBean.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = factory.createBeanDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);
        assertTrue(deser instanceof BeanDeserializer);
    }

    // Tests findStdDeserializer with DeserializerModifier applied
    @Test
    public void testFindStdDeserializer_withModifier_appliesModifier() throws JsonMappingException {
        final boolean[] modified = new boolean[1];
        BeanDeserializerModifier mod = new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                                                         BeanDescription beanDesc,
                                                         JsonDeserializer<?> deserializer) {
                modified[0] = true;
                return deserializer;
            }
        };

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withDeserializerModifier(mod);
        BeanDeserializerFactory customFactory = new BeanDeserializerFactory(config);

        JavaType type = mapper.constructType(String.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<?> deser = customFactory.findStdDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);
        assertTrue(modified[0]);
    }
}