package com.fasterxml.jackson.databind.deser;

import java.util.*;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.std.ThrowableDeserializer;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class BeanDeserializerFactoryTest {

    private ObjectMapper objectMapper;
    private DeserializationContext deserializationContext;
    private BeanDeserializerFactory factory;

    static class SimpleBean {
        public String name;
        public int value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    static class SubclassedFactory extends BeanDeserializerFactory {
        private static final long serialVersionUID = 1L;

        public SubclassedFactory(DeserializerFactoryConfig config) {
            super(config);
        }
    }

    static class CustomException extends Exception {
        private static final long serialVersionUID = 1L;

        public CustomException() {
            super();
        }

        public CustomException(String message) {
            super(message);
        }
    }

    static class SetterlessBean {
        private final List<String> items = new ArrayList<>();

        public List<String> getItems() {
            return items;
        }
    }

    @JsonIgnoreProperties({"ignoredField"})
    static class IgnoredPropsBean {
        public String normalField;
        public String ignoredField;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class IdentifiedBean {
        public int id;
        public String name;
    }

    @JsonDeserialize(builder = ValueClassBuilder.class)
    static class ValueClass {
        final int x;

        ValueClass(int x) {
            this.x = x;
        }
    }

    @JsonPOJOBuilder(withPrefix = "with")
    static class ValueClassBuilder {
        private int x;

        public ValueClassBuilder withX(int x) {
            this.x = x;
            return this;
        }

        public ValueClass build() {
            return new ValueClass(x);
        }
    }

    public interface InterfaceType {
        String getValue();
    }

    static class InterfaceImpl implements InterfaceType {
        public String value;

        @Override
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    static class CreatorBean {
        final String first;
        final int second;

        @JsonCreator
        public CreatorBean(@JsonProperty("first") String first, @JsonProperty("second") int second) {
            this.first = first;
            this.second = second;
        }
    }

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        deserializationContext = ((ObjectMapper) objectMapper).getDeserializationContext();
        factory = BeanDeserializerFactory.instance;
    }

    // Tests withConfig returns same instance when given identical config
    @Test
    public void testWithConfig_sameConfig_returnsSameInstance() {
        DeserializerFactoryConfig config = factory.getFactoryConfig();
        DeserializerFactory result = factory.withConfig(config);
        assertSame(factory, result);
    }

    // Tests withConfig returns new factory instance with different config
    @Test
    public void testWithConfig_newConfig_returnsNewInstance() {
        DeserializerFactoryConfig config = new DeserializerFactoryConfig();
        DeserializerFactory result = factory.withConfig(config);
        assertNotNull(result);
        assertNotSame(factory, result);
        assertEquals(BeanDeserializerFactory.class, result.getClass());
    }

    // Tests withConfig throws IllegalStateException when called on subclass that did not override withConfig
    @Test(expected = IllegalStateException.class)
    public void testWithConfig_unsupportedSubclass_throwsIllegalStateException() {
        SubclassedFactory subFactory = new SubclassedFactory(new DeserializerFactoryConfig());
        subFactory.withConfig(new DeserializerFactoryConfig());
    }

    // Tests isPotentialBeanType on normal valid POJO class
    @Test
    public void testIsPotentialBeanType_validBeanClass_returnsTrue() {
        assertTrue(factory.isPotentialBeanType(SimpleBean.class));
    }

    // Tests isPotentialBeanType throws IllegalArgumentException for primitive types
    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_primitiveType_throwsIllegalArgumentException() {
        factory.isPotentialBeanType(int.class);
    }

    // Tests isPotentialBeanType throws IllegalArgumentException for array types
    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_arrayType_throwsIllegalArgumentException() {
        factory.isPotentialBeanType(String[].class);
    }

    // Tests createBeanDeserializer builds a working BeanDeserializer for normal class
    @Test
    public void testCreateBeanDeserializer_regularBean_returnsNonNullDeserializer() throws Exception {
        JavaType type = objectMapper.constructType(SimpleBean.class);
        DeserializationConfig config = objectMapper.getDeserializationConfig();
        BeanDescription beanDesc = config.introspect(type);

        JsonDeserializer<Object> deser = factory.createBeanDeserializer(deserializationContext, type, beanDesc);
        assertNotNull(deser);
        assertTrue(deser.isCachable());
    }

    // Tests createBeanDeserializer creates ThrowableDeserializer for Throwable subtypes
    @Test
    public void testCreateBeanDeserializer_throwableType_returnsThrowableDeserializer() throws Exception {
        JavaType type = objectMapper.constructType(CustomException.class);
        DeserializationConfig config = objectMapper.getDeserializationConfig();
        BeanDescription beanDesc = config.introspect(type);

        JsonDeserializer<Object> deser = factory.createBeanDeserializer(deserializationContext, type, beanDesc);
        assertNotNull(deser);
        assertTrue(deser instanceof ThrowableDeserializer);
    }

    // Tests createBeanDeserializer with AbstractTypeResolver materialization
    @Test
    public void testCreateBeanDeserializer_abstractTypeWithResolver_materializesAndBuildsDeserializer() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(InterfaceType.class, InterfaceImpl.class);

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withAbstractTypeResolver(resolver);
        BeanDeserializerFactory customFactory = new BeanDeserializerFactory(config);

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.setAbstractTypes(resolver);
        mapper.registerModule(module);

        JavaType type = mapper.constructType(InterfaceType.class);
        DeserializationConfig deserConfig = mapper.getDeserializationConfig();
        BeanDescription beanDesc = deserConfig.introspect(type);

        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(deserConfig, mapper.getFactory().createParser("{}"), mapper.getInjectableValues());

        JsonDeserializer<Object> deser = customFactory.createBeanDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);
    }

    // Tests createBeanDeserializer with builder based deserialization
    @Test
    public void testCreateBuilderBasedDeserializer_validBuilder_buildsDeserializer() throws Exception {
        JavaType valueType = objectMapper.constructType(ValueClass.class);
        DeserializationConfig config = objectMapper.getDeserializationConfig();
        BeanDescription beanDesc = config.introspect(valueType);

        JsonDeserializer<Object> deser = factory.createBuilderBasedDeserializer(
                deserializationContext, valueType, beanDesc, ValueClassBuilder.class);
        assertNotNull(deser);
    }

    // Tests buildBeanDeserializer handles setterless collection properties
    @Test
    public void testBuildBeanDeserializer_setterlessProperties_handlesCorrectly() throws Exception {
        String json = "{\"items\":[\"a\",\"b\"]}";
        SetterlessBean result = objectMapper.readValue(json, SetterlessBean.class);
        assertNotNull(result);
        assertEquals(2, result.getItems().size());
        assertEquals("a", result.getItems().get(0));
        assertEquals("b", result.getItems().get(1));
    }

    // Tests buildBeanDeserializer correctly ignores @JsonIgnoreProperties
    @Test
    public void testBuildBeanDeserializer_ignoredProperties_ignoresConfiguredFields() throws Exception {
        String json = "{\"normalField\":\"ok\",\"ignoredField\":\"ignored\"}";
        IgnoredPropsBean result = objectMapper.readValue(json, IgnoredPropsBean.class);
        assertNotNull(result);
        assertEquals("ok", result.normalField);
        assertNull(result.ignoredField);
    }

    // Tests buildBeanDeserializer with constructor properties and creator
    @Test
    public void testBuildBeanDeserializer_creatorProperties_deserializesCorrectly() throws Exception {
        String json = "{\"first\":\"abc\",\"second\":123}";
        CreatorBean result = objectMapper.readValue(json, CreatorBean.class);
        assertNotNull(result);
        assertEquals("abc", result.first);
        assertEquals(123, result.second);
    }

    // Tests buildBeanDeserializer with ObjectIdReader
    @Test
    public void testBuildBeanDeserializer_objectIdReader_deserializesIdentity() throws Exception {
        String json = "{\"id\":10,\"name\":\"test\"}";
        IdentifiedBean result = objectMapper.readValue(json, IdentifiedBean.class);
        assertNotNull(result);
        assertEquals(10, result.id);
        assertEquals("test", result.name);
    }

    // Tests isIgnorableType with non-ignorable class returns false
    @Test
    public void testIsIgnorableType_standardClass_returnsFalse() {
        DeserializationConfig config = objectMapper.getDeserializationConfig();
        BeanDescription desc = config.introspectClassAnnotations(SimpleBean.class);
        Map<Class<?>, Boolean> cache = new HashMap<>();

        boolean ignorable = factory.isIgnorableType(config, desc, SimpleBean.class, cache);
        assertFalse(ignorable);
        assertTrue(cache.containsKey(SimpleBean.class));
        assertFalse(cache.get(SimpleBean.class));
    }

    // Tests buildThrowableDeserializer properly ignores standard throwable fields like localizedMessage
    @Test
    public void testBuildThrowableDeserializer_customException_ignoresSuppressedAndLocalizedMessage() throws Exception {
        String json = "{\"message\":\"error message\",\"localizedMessage\":\"local error\",\"cause\":null}";
        CustomException ex = objectMapper.readValue(json, CustomException.class);
        assertNotNull(ex);
        assertEquals("error message", ex.getMessage());
    }
}