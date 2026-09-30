package com.fasterxml.jackson.databind.deser;

import java.util.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class BeanDeserializerFactoryTest {

    private BeanDeserializerFactory _factory;
    private ObjectMapper _mapper;
    private DeserializationContext _context;

    // Helper POJOs for testing
    static class SimpleBean {
        public int x;
        public String y;
    }

    static class ThrowableBean extends Exception {
        private static final long serialVersionUID = 1L;
        public int extra;
        public ThrowableBean() { super(); }
        public ThrowableBean(String msg) { super(msg); }
    }

    static abstract class AbstractBean {
        public int id;
    }

    static class ConcreteBean extends AbstractBean {
    }

    @JsonDeserialize(builder = ValueClassBuilder.class)
    static class ValueClass {
        final int value;
        ValueClass(int v) { this.value = v; }
    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    static class ValueClassBuilder {
        private int _value;
        public ValueClassBuilder withValue(int v) {
            this._value = v;
            return this;
        }
        public ValueClass build() {
            return new ValueClass(_value);
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class IdentifiedBean {
        public int id;
        public String name;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
    static class SequenceIdentifiedBean {
        public int id;
    }

    static class CreatorBean {
        final int a;
        final String b;

        @JsonCreator
        public CreatorBean(@JsonProperty("a") int a, @JsonProperty("b") String b) {
            this.a = a;
            this.b = b;
        }
    }

    static class CustomSubFactory extends BeanDeserializerFactory {
        private static final long serialVersionUID = 1L;

        public CustomSubFactory(DeserializerFactoryConfig config) {
            super(config);
        }
    }

    @Before
    public void setUp() {
        _factory = BeanDeserializerFactory.instance;
        _mapper = new ObjectMapper();
        _context = _mapper.getDeserializationContext();
    }

    // Tests singleton instance existence
    @Test
    public void testInstance_notNull() {
        assertNotNull(BeanDeserializerFactory.instance);
    }

    // Tests withConfig with same configuration returning same instance
    @Test
    public void testWithConfig_sameConfig_returnsSameInstance() {
        DeserializerFactoryConfig config = _factory.getFactoryConfig();
        DeserializerFactory result = _factory.withConfig(config);
        assertSame(_factory, result);
    }

    // Tests withConfig with new configuration returning new instance
    @Test
    public void testWithConfig_newConfig_returnsNewInstance() {
        DeserializerFactoryConfig newConfig = new DeserializerFactoryConfig();
        DeserializerFactory result = _factory.withConfig(newConfig);
        assertNotSame(_factory, result);
        assertTrue(result instanceof BeanDeserializerFactory);
    }

    // Tests withConfig when called on subclass without overriding throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testWithConfig_subclassWithoutOverride_throwsIllegalStateException() {
        CustomSubFactory subFactory = new CustomSubFactory(new DeserializerFactoryConfig());
        subFactory.withConfig(new DeserializerFactoryConfig());
    }

    // Tests checkIllegalTypes with known illegal/nasty class name
    @Test(expected = JsonMappingException.class)
    public void testCheckIllegalTypes_illegalClass_throwsJsonMappingException() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructFromCanonical("com.sun.rowset.JdbcRowSetImpl");
        BeanDescription beanDesc = _mapper.getDeserializationConfig().introspect(type);
        _factory.checkIllegalTypes(_context, type, beanDesc);
    }

    // Tests checkIllegalTypes with valid class name
    @Test
    public void testCheckIllegalTypes_validClass_doesNotThrow() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        BeanDescription beanDesc = _mapper.getDeserializationConfig().introspect(type);
        _factory.checkIllegalTypes(_context, type, beanDesc);
    }

    // Tests createBeanDeserializer for standard bean
    @Test
    public void testCreateBeanDeserializer_simpleBean_returnsDeserializer() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        BeanDescription beanDesc = _mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = _factory.createBeanDeserializer(_context, type, beanDesc);
        assertNotNull(deser);
    }

    // Tests createBeanDeserializer for Throwable types
    @Test
    public void testCreateBeanDeserializer_throwableType_returnsThrowableDeserializer() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(ThrowableBean.class);
        BeanDescription beanDesc = _mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = _factory.createBeanDeserializer(_context, type, beanDesc);
        assertNotNull(deser);
    }

    // Tests createBeanDeserializer for abstract type with materializer
    @Test
    public void testCreateBeanDeserializer_abstractTypeWithResolver_returnsDeserializer() throws Exception {
        AbstractTypeResolver resolver = new SimpleAbstractTypeResolver().addMapping(AbstractBean.class, ConcreteBean.class);
        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withAbstractTypeResolver(resolver);
        BeanDeserializerFactory customFactory = new BeanDeserializerFactory(config);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = TypeFactory.defaultInstance().constructType(AbstractBean.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = customFactory.createBeanDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);
    }

    // Tests createBuilderBasedDeserializer for builder-annotated class
    @Test
    public void testCreateBuilderBasedDeserializer_builderAnnotated_returnsDeserializer() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(ValueClass.class);
        BeanDescription beanDesc = _mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = _factory.createBuilderBasedDeserializer(
                _context, type, beanDesc, ValueClassBuilder.class);
        assertNotNull(deser);
    }

    // Tests createBeanDeserializer with Property-based ObjectId
    @Test
    public void testCreateBeanDeserializer_propertyObjectId_returnsDeserializer() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(IdentifiedBean.class);
        BeanDescription beanDesc = _mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = _factory.createBeanDeserializer(_context, type, beanDesc);
        assertNotNull(deser);
    }

    // Tests createBeanDeserializer with Non-Property ObjectId
    @Test
    public void testCreateBeanDeserializer_sequenceObjectId_returnsDeserializer() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(SequenceIdentifiedBean.class);
        BeanDescription beanDesc = _mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = _factory.createBeanDeserializer(_context, type, beanDesc);
        assertNotNull(deser);
    }

    // Tests createBeanDeserializer for Bean with creator parameters
    @Test
    public void testCreateBeanDeserializer_creatorProperties_returnsDeserializer() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(CreatorBean.class);
        BeanDescription beanDesc = _mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = _factory.createBeanDeserializer(_context, type, beanDesc);
        assertNotNull(deser);
    }

    // Tests isPotentialBeanType with valid non-primitive class
    @Test
    public void testIsPotentialBeanType_validClass_returnsTrue() {
        assertTrue(_factory.isPotentialBeanType(SimpleBean.class));
    }

    // Tests isPotentialBeanType with primitive type throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_primitiveClass_throwsException() {
        _factory.isPotentialBeanType(int.class);
    }

    // Tests isPotentialBeanType with array class throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_arrayClass_throwsException() {
        _factory.isPotentialBeanType(SimpleBean[].class);
    }

    // Tests DeserializerModifier interaction
    @Test
    public void testBuildBeanDeserializer_withModifier_invokesModifier() throws Exception {
        final boolean[] modified = new boolean[1];
        BeanDeserializerModifier modifier = new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                    BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                modified[0] = true;
                return deserializer;
            }
        };

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withDeserializerModifier(modifier);
        BeanDeserializerFactory customFactory = new BeanDeserializerFactory(config);

        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        BeanDescription beanDesc = _mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = customFactory.createBeanDeserializer(_context, type, beanDesc);

        assertNotNull(deser);
        assertTrue(modified[0]);
    }
}