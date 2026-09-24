package com.fasterxml.jackson.databind.deser;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BeanDeserializerFactoryTest {

    private BeanDeserializerFactory factory;
    private ObjectMapper mapper;
    private DeserializationContext context;

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
        private String extraInfo;

        public CustomException(String msg) { super(msg); }
        public String getExtraInfo() { return extraInfo; }
        public void setExtraInfo(String extraInfo) { this.extraInfo = extraInfo; }
    }

    abstract static class AbstractBean {
        public String id;
    }

    static class ConcreteBean extends AbstractBean {
        public String value;
    }

    static class AnySetterBean {
        private Map<String, Object> other = new HashMap<String, Object>();

        public void setOther(String key, Object value) {
            other.put(key, value);
        }
    }

    static class AnySetterFieldBean {
        public Map<String, Object> other = new HashMap<String, Object>();
    }

    @JsonIgnoreType
    static class IgnoredType {
        public String value;
    }

    static class InjectableBean {
        @JacksonInject("idInject")
        public String injectedId;
        public String regularProp;
    }

    static class ParentNode {
        public String name;
        @JsonManagedReference
        public ChildNode child;
    }

    static class ChildNode {
        public String name;
        @JsonBackReference
        public ParentNode parent;
    }

    class NonStaticInnerClass {
        public String value;
    }

    @JsonDeserialize(builder = SimpleBuilder.class)
    static class ValueWithBuilder {
        final int x;
        final String y;

        ValueWithBuilder(int x, String y) {
            this.x = x;
            this.y = y;
        }
    }

    @JsonPOJOBuilder(withPrefix = "with")
    static class SimpleBuilder {
        private int x;
        private String y;

        public SimpleBuilder withX(int x) {
            this.x = x;
            return this;
        }

        public SimpleBuilder withY(String y) {
            this.y = y;
            return this;
        }

        public ValueWithBuilder build() {
            return new ValueWithBuilder(x, y);
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class IdentifiedBean {
        public int id;
        public String name;
    }

    @JsonIgnoreProperties({ "ignoredProp" })
    static class IgnoredPropsBean {
        public String keptProp;
        public String ignoredProp;
    }

    @Before
    public void setUp() {
        factory = BeanDeserializerFactory.instance;
        mapper = new ObjectMapper();
        context = mapper.getDeserializationContext();
    }

    // Tests withConfig returning the same instance when config is identical
    @Test
    public void testWithConfig_sameConfig_returnsSameInstance() {
        DeserializerFactoryConfig config = factory.getFactoryConfig();
        DeserializerFactory newFactory = factory.withConfig(config);
        assertSame(factory, newFactory);
    }

    // Tests withConfig returning a new instance when config changes
    @Test
    public void testWithConfig_differentConfig_returnsNewInstance() {
        DeserializerFactoryConfig newConfig = new DeserializerFactoryConfig();
        DeserializerFactory newFactory = factory.withConfig(newConfig);
        assertNotSame(factory, newFactory);
        assertTrue(newFactory instanceof BeanDeserializerFactory);
    }

    // Tests isPotentialBeanType for a normal bean class
    @Test
    public void testIsPotentialBeanType_validBeanClass_returnsTrue() {
        assertTrue(factory.isPotentialBeanType(SimpleBean.class));
    }

    // Tests isPotentialBeanType rejecting primitive types
    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_primitiveType_throwsException() {
        factory.isPotentialBeanType(int.class);
    }

    // Tests isPotentialBeanType rejecting array types
    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_arrayType_throwsException() {
        factory.isPotentialBeanType(int[].class);
    }

    // Tests createBeanDeserializer for a standard bean class
    @Test
    public void testCreateBeanDeserializer_regularBean_returnsDeserializer() throws Exception {
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) context)
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);

        JsonDeserializer<Object> deser = factory.createBeanDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);
        assertTrue(deser instanceof BeanDeserializer);
    }

    // Tests createBeanDeserializer for Throwable subtypes
    @Test
    public void testCreateBeanDeserializer_throwableSubclass_returnsThrowableDeserializer() throws Exception {
        JavaType type = mapper.constructType(CustomException.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) context)
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);

        JsonDeserializer<Object> deser = factory.createBeanDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);
        assertTrue(deser instanceof com.fasterxml.jackson.databind.deser.std.ThrowableDeserializer);
    }

    // Tests createBeanDeserializer for an abstract class without materialization
    @Test
    public void testCreateBeanDeserializer_abstractClass_returnsAbstractDeserializer() throws Exception {
        JavaType type = mapper.constructType(AbstractBean.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) context)
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);

        JsonDeserializer<Object> deser = factory.createBeanDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);
        assertTrue(deser instanceof AbstractDeserializer);
    }

    // Tests createBeanDeserializer with abstract type materializer resolver
    @Test
    public void testCreateBeanDeserializer_withAbstractTypeResolver_resolvesToConcrete() throws Exception {
        DeserializerFactoryConfig config = new DeserializerFactoryConfig()
                .withAbstractTypeResolver(new com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver()
                        .addMapping(AbstractBean.class, ConcreteBean.class));
        BeanDeserializerFactory customFactory = new BeanDeserializerFactory(config);

        JavaType type = mapper.constructType(AbstractBean.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) context)
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);

        JsonDeserializer<Object> deser = customFactory.createBeanDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);
        assertTrue(deser instanceof BeanDeserializer);
    }

    // Tests createBuilderBasedDeserializer builds a builder-based deserializer
    @Test
    public void testCreateBuilderBasedDeserializer_validBuilder_returnsBuilderDeserializer() throws Exception {
        JavaType valueType = mapper.constructType(ValueWithBuilder.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(valueType);
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) context)
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);

        JsonDeserializer<Object> deser = factory.createBuilderBasedDeserializer(ctxt, valueType, beanDesc, SimpleBuilder.class);
        assertNotNull(deser);
        assertTrue(deser instanceof BuilderBasedDeserializer);
    }

    // Tests addObjectIdReader via deserializer construction for property generator identity
    @Test
    public void testCreateBeanDeserializer_propertyBasedObjectId_constructsReader() throws Exception {
        JavaType type = mapper.constructType(IdentifiedBean.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) context)
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);

        JsonDeserializer<Object> deser = factory.createBeanDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);
        assertTrue(deser instanceof BeanDeserializer);
        BeanDeserializer bdeser = (BeanDeserializer) deser;
        assertNotNull(bdeser.getObjectIdReader());
        assertEquals("id", bdeser.getObjectIdReader().propertyName.getSimpleName());
    }

    // Tests isIgnorableType caching and result for primitives and String
    @Test
    public void testIsIgnorableType_primitiveAndString_returnsFalse() {
        Map<Class<?>, Boolean> cache = new HashMap<Class<?>, Boolean>();
        DeserializationConfig config = mapper.getDeserializationConfig();

        boolean stringIgnored = factory.isIgnorableType(config, null, String.class, cache);
        assertFalse(stringIgnored);
        assertEquals(Boolean.FALSE, cache.get(String.class));

        boolean intIgnored = factory.isIgnorableType(config, null, int.class, cache);
        assertFalse(intIgnored);
        assertEquals(Boolean.FALSE, cache.get(int.class));
    }

    // Tests constructAnySetter using an AnnotatedMethod
    @Test
    public void testConstructAnySetter_methodMutator_createsSettableAnyProperty() throws Exception {
        JavaType type = mapper.constructType(AnySetterBean.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) context)
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);

        Method method = AnySetterBean.class.getMethod("setOther", String.class, Object.class);
        AnnotatedMethod annotatedMethod = new AnnotatedMethod(new TypeResolutionContext.Basic(mapper.getTypeFactory(), TypeFactory.unknownType().getBindings()), method, null, null);

        SettableAnyProperty anyProp = factory.constructAnySetter(ctxt, beanDesc, annotatedMethod);
        assertNotNull(anyProp);
        assertEquals("setOther", anyProp.getProperty().getName());
    }

    // Tests deserializer modifier hook in BeanDeserializerFactory
    @Test
    public void testCreateBeanDeserializer_withDeserializerModifier_invokesModifier() throws Exception {
        final boolean[] modifierCalled = new boolean[1];
        BeanDeserializerModifier modifier = new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                    BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                modifierCalled[0] = true;
                return deserializer;
            }
        };

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withDeserializerModifier(modifier);
        BeanDeserializerFactory customFactory = new BeanDeserializerFactory(config);

        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) context)
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);

        JsonDeserializer<Object> deser = customFactory.createBeanDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);
        assertTrue(modifierCalled[0]);
    }

    // Tests isPotentialBeanType rejecting non-static inner classes
    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_nonStaticInnerClass_throwsException() {
        factory.isPotentialBeanType(NonStaticInnerClass.class);
    }

    // Tests isIgnorableType with @JsonIgnoreType annotation
    @Test
    public void testIsIgnorableType_annotatedClass_returnsTrue() {
        Map<Class<?>, Boolean> cache = new HashMap<Class<?>, Boolean>();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription beanDesc = config.introspectClassAnnotations(IgnoredType.class);

        boolean ignored = factory.isIgnorableType(config, beanDesc, IgnoredType.class, cache);
        assertTrue(ignored);
        assertEquals(Boolean.TRUE, cache.get(IgnoredType.class));
    }

    // Tests constructAnySetter using an AnnotatedField
    @Test
    public void testConstructAnySetter_fieldMutator_createsSettableAnyProperty() throws Exception {
        JavaType type = mapper.constructType(AnySetterFieldBean.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) context)
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);

        Field field = AnySetterFieldBean.class.getField("other");
        AnnotatedField annotatedField = new AnnotatedField(new TypeResolutionContext.Basic(mapper.getTypeFactory(), TypeFactory.unknownType().getBindings()), field, null);

        SettableAnyProperty anyProp = factory.constructAnySetter(ctxt, beanDesc, annotatedField);
        assertNotNull(anyProp);
        assertEquals("other", anyProp.getProperty().getName());
    }

    // Tests createBeanDeserializer with injectables and back-references
    @Test
    public void testCreateBeanDeserializer_withInjectables_handlesInjectedProperties() throws Exception {
        JavaType type = mapper.constructType(InjectableBean.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) context)
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);

        JsonDeserializer<Object> deser = factory.createBeanDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);
        assertTrue(deser instanceof BeanDeserializer);
    }

    // Tests createBeanDeserializer with back reference resolution
    @Test
    public void testCreateBeanDeserializer_withBackReference_bindsBackReference() throws Exception {
        JavaType type = mapper.constructType(ChildNode.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) context)
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);

        JsonDeserializer<Object> deser = factory.createBeanDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);
        assertTrue(deser instanceof BeanDeserializer);
    }

    // Tests createBeanDeserializer handling of ignored properties configured on class
    @Test
    public void testCreateBeanDeserializer_withIgnoredProperties_filtersProperties() throws Exception {
        JavaType type = mapper.constructType(IgnoredPropsBean.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) context)
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);

        JsonDeserializer<Object> deser = factory.createBeanDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);
        assertTrue(deser instanceof BeanDeserializer);
        BeanDeserializer bdeser = (BeanDeserializer) deser;
        assertNull(bdeser.findProperty("ignoredProp"));
        assertNotNull(bdeser.findProperty("keptProp"));
    }
}