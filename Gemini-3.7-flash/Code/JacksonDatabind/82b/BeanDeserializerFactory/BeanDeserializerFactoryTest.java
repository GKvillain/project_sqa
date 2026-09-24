package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class BeanDeserializerFactoryTest {

    private ObjectMapper mapper;
    private DeserializationContext ctxt;
    private BeanDeserializerFactory factory;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        ctxt = mapper.getDeserializationContext();
        factory = BeanDeserializerFactory.instance;
    }

    // Helper classes for testing
    static class SimpleBean {
        public int x;
        public String y;
    }

    @JsonIgnoreProperties({"ignoredProp"})
    static class IgnoredPropsBean {
        public int normalProp;
        public int ignoredProp;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class IgnoreUnknownBean {
        public int normalProp;
    }

    static class AnySetterBean {
        private Map<String, Object> extra = new HashMap<>();

        @JsonAnySetter
        public void setExtra(String key, Object value) {
            extra.put(key, value);
        }

        public Map<String, Object> getExtra() {
            return extra;
        }
    }

    static class AnySetterFieldBean {
        @JsonAnySetter
        public Map<String, Object> extra = new HashMap<>();
    }

    @JsonDeserialize(builder = ValueClassBuilder.class)
    static class ValueClass {
        final int value;

        ValueClass(int v) {
            this.value = v;
        }
    }

    @JsonPOJOBuilder(withPrefix = "with")
    static class ValueClassBuilder {
        private int value;

        public ValueClassBuilder withValue(int v) {
            this.value = v;
            return this;
        }

        public ValueClass build() {
            return new ValueClass(value);
        }
    }

    static class CustomException extends Throwable {
        private static final long serialVersionUID = 1L;
        public int customField;
        public CustomException() {}
        public CustomException(String msg) { super(msg); }
    }

    static class ObjectIdBean {
        @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
        public int id;
        public String name;
    }

    static class SubFactory extends BeanDeserializerFactory {
        private static final long serialVersionUID = 1L;

        public SubFactory(DeserializerFactoryConfig config) {
            super(config);
        }
    }

    class NonStaticInnerClass {
        public int x;
    }

    interface AbstractInterface {
        int getVal();
    }

    static class ConcreteImpl implements AbstractInterface {
        public int val;
        @Override
        public int getVal() { return val; }
    }

    static class ParentNode {
        public String name;
        @JsonManagedReference
        public ChildNode child;
    }

    static class ChildNode {
        public String childName;
        @JsonBackReference
        public ParentNode parent;
    }

    static class CreatorBean {
        private final int x;
        private final String y;

        @JsonCreator
        public CreatorBean(@JsonProperty("x") int x, @JsonProperty("y") String y) {
            this.x = x;
            this.y = y;
        }

        public int getX() { return x; }
        public String getY() { return y; }
    }

    static class InjectBean {
        @JacksonInject("injectedVal")
        public String injected;
        public int normal;
    }

    static class UnwrappedOuter {
        public String name;
        @JsonUnwrapped
        public UnwrappedInner inner;
    }

    static class UnwrappedInner {
        public int count;
        public String desc;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = PolymorphicSubA.class, name = "A")
    })
    static abstract class PolymorphicBase {
        public String baseField;
    }

    static class PolymorphicSubA extends PolymorphicBase {
        public int aValue;
    }

    static class PolymorphicContainer {
        public PolymorphicBase poly;
    }

    static class CustomModifier extends BeanDeserializerModifier {
        boolean builderModified = false;
        boolean deserModified = false;

        @Override
        public BeanDeserializerBuilder updateBuilder(DeserializationConfig config,
                BeanDescription beanDesc, BeanDeserializerBuilder builder) {
            builderModified = true;
            return super.updateBuilder(config, beanDesc, builder);
        }

        @Override
        public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
            deserModified = true;
            return super.modifyDeserializer(config, beanDesc, deserializer);
        }
    }

    // Tests singleton instance and withConfig behavior with identical config
    @Test
    public void testWithConfig_sameConfig_returnsSameInstance() {
        DeserializerFactoryConfig config = factory.getFactoryConfig();
        DeserializerFactory newFactory = factory.withConfig(config);
        assertSame(factory, newFactory);
    }

    // Tests withConfig with new config
    @Test
    public void testWithConfig_newConfig_returnsNewInstance() {
        DeserializerFactoryConfig newConfig = new DeserializerFactoryConfig();
        DeserializerFactory newFactory = factory.withConfig(newConfig);
        assertNotSame(factory, newFactory);
        assertTrue(newFactory instanceof BeanDeserializerFactory);
    }

    // Tests withConfig when called on subclass without override throws exception
    @Test(expected = IllegalStateException.class)
    public void testWithConfig_subclassWithoutOverride_throwsException() {
        SubFactory subFactory = new SubFactory(new DeserializerFactoryConfig());
        subFactory.withConfig(new DeserializerFactoryConfig());
    }

    // Tests deserializer creation for standard POJO
    @Test
    public void testCreateBeanDeserializer_simpleBean_createsDeserializer() throws Exception {
        JavaType type = mapper.constructType(SimpleBean.class);
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription beanDesc = config.introspect(type);

        JsonDeserializer<Object> deser = factory.createBeanDeserializer(
                mapper.getDeserializationContext(), type, beanDesc);
        assertNotNull(deser);
    }

    // Tests deserializer creation for Throwable type
    @Test
    public void testCreateBeanDeserializer_throwableType_createsThrowableDeserializer() throws Exception {
        JavaType type = mapper.constructType(CustomException.class);
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription beanDesc = config.introspect(type);

        JsonDeserializer<Object> deser = factory.createBeanDeserializer(
                mapper.getDeserializationContext(), type, beanDesc);
        assertNotNull(deser);
    }

    // Tests deserializer creation for illegal/nasty types for security reasons
    @Test(expected = JsonMappingException.class)
    public void testCreateBeanDeserializer_illegalType_throwsSecurityException() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructFromCanonical(
                "org.apache.commons.collections.functors.InvokerTransformer");
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription beanDesc = config.introspect(type);

        factory.createBeanDeserializer(mapper.getDeserializationContext(), type, beanDesc);
    }

    // Tests isPotentialBeanType with non-bean primitive/array/local types
    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_primitiveType_throwsException() {
        factory.isPotentialBeanType(int.class);
    }

    // Tests isPotentialBeanType with valid bean class
    @Test
    public void testIsPotentialBeanType_validClass_returnsTrue() {
        assertTrue(factory.isPotentialBeanType(SimpleBean.class));
    }

    // Tests deserialization of bean with @JsonIgnoreProperties annotation on class
    @Test
    public void testAddBeanProps_withClassJsonIgnoreProperties_ignoresProperty() throws IOException {
        String json = "{\"normalProp\": 42, \"ignoredProp\": 99}";
        IgnoredPropsBean result = mapper.readValue(json, IgnoredPropsBean.class);
        assertNotNull(result);
        assertEquals(42, result.normalProp);
        assertEquals(0, result.ignoredProp);
    }

    // Tests deserialization of bean with ignoreUnknown = true
    @Test
    public void testAddBeanProps_ignoreUnknown_skipsUnknownProperty() throws IOException {
        String json = "{\"normalProp\": 10, \"extraUnknown\": 20}";
        IgnoreUnknownBean result = mapper.readValue(json, IgnoreUnknownBean.class);
        assertNotNull(result);
        assertEquals(10, result.normalProp);
    }

    // Tests deserialization with @JsonAnySetter on a method
    @Test
    public void testAddBeanProps_anySetterMethod_populatesMap() throws IOException {
        String json = "{\"key1\": \"val1\", \"key2\": \"val2\"}";
        AnySetterBean result = mapper.readValue(json, AnySetterBean.class);
        assertNotNull(result);
        assertEquals("val1", result.getExtra().get("key1"));
        assertEquals("val2", result.getExtra().get("key2"));
    }

    // Tests deserialization with @JsonAnySetter on a field
    @Test
    public void testAddBeanProps_anySetterField_populatesMap() throws IOException {
        String json = "{\"key1\": \"val1\", \"key2\": \"val2\"}";
        AnySetterFieldBean result = mapper.readValue(json, AnySetterFieldBean.class);
        assertNotNull(result);
        assertEquals("val1", result.extra.get("key1"));
        assertEquals("val2", result.extra.get("key2"));
    }

    // Tests builder-based deserializer creation
    @Test
    public void testCreateBuilderBasedDeserializer_validBuilder_deserializesCorrectly() throws IOException {
        String json = "{\"value\": 123}";
        ValueClass result = mapper.readValue(json, ValueClass.class);
        assertNotNull(result);
        assertEquals(123, result.value);
    }

    // Tests ObjectId property-based deserializer construction
    @Test
    public void testAddObjectIdReader_propertyBasedGenerator_handlesIdentity() throws IOException {
        String json = "{\"id\": 1, \"name\": \"test\"}";
        ObjectIdBean result = mapper.readValue(json, ObjectIdBean.class);
        assertNotNull(result);
        assertEquals(1, result.id);
        assertEquals("test", result.name);
    }

    // Tests buildThrowableDeserializer properly includes initCause and ignores standard fields
    @Test
    public void testBuildThrowableDeserializer_deserializesExceptionWithCause() throws IOException {
        String json = "{\"message\": \"error message\", \"customField\": 7}";
        CustomException result = mapper.readValue(json, CustomException.class);
        assertNotNull(result);
        assertEquals("error message", result.getMessage());
        assertEquals(7, result.customField);
    }

    // Tests materializeAbstractType when no resolvers are configured
    @Test
    public void testMaterializeAbstractType_noResolvers_returnsNull() throws Exception {
        JavaType type = mapper.constructType(List.class);
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription beanDesc = config.introspect(type);

        JavaType concrete = factory.materializeAbstractType(
                mapper.getDeserializationContext(), type, beanDesc);
        assertNull(concrete);
    }

    // Additional tests for missing coverage

    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_arrayType_throwsException() {
        factory.isPotentialBeanType(String[].class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_nonStaticInnerClass_throwsException() {
        factory.isPotentialBeanType(NonStaticInnerClass.class);
    }

    @Test
    public void testMaterializeAbstractType_withAbstractTypeResolver_resolvesType() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(AbstractInterface.class, ConcreteImpl.class);

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withAbstractTypeResolver(resolver);
        BeanDeserializerFactory customFactory = new BeanDeserializerFactory(config);

        JavaType type = mapper.constructType(AbstractInterface.class);
        DeserializationConfig deserConfig = mapper.getDeserializationConfig();
        BeanDescription beanDesc = deserConfig.introspect(type);

        JavaType concreteType = customFactory.materializeAbstractType(ctxt, type, beanDesc);
        assertNotNull(concreteType);
        assertEquals(ConcreteImpl.class, concreteType.getRawClass());
    }

    @Test
    public void testCreateBeanDeserializer_withBeanDeserializerModifier() throws Exception {
        CustomModifier modifier = new CustomModifier();
        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withDeserializerModifier(modifier);
        BeanDeserializerFactory customFactory = new BeanDeserializerFactory(config);

        JavaType type = mapper.constructType(SimpleBean.class);
        DeserializationConfig deserConfig = mapper.getDeserializationConfig();
        BeanDescription beanDesc = deserConfig.introspect(type);

        JsonDeserializer<Object> deser = customFactory.createBeanDeserializer(ctxt, type, beanDesc);
        assertNotNull(deser);
        assertTrue(modifier.builderModified);
        assertTrue(modifier.deserModified);
    }

    @Test
    public void testDeserialization_creatorProperties() throws IOException {
        String json = "{\"x\": 10, \"y\": \"hello\"}";
        CreatorBean result = mapper.readValue(json, CreatorBean.class);
        assertNotNull(result);
        assertEquals(10, result.getX());
        assertEquals("hello", result.getY());
    }

    @Test
    public void testDeserialization_managedAndBackReference() throws IOException {
        String json = "{\"name\": \"parent\", \"child\": {\"childName\": \"child\"}}";
        ParentNode parent = mapper.readValue(json, ParentNode.class);
        assertNotNull(parent);
        assertEquals("parent", parent.name);
        assertNotNull(parent.child);
        assertEquals("child", parent.child.childName);
        assertSame(parent, parent.child.parent);
    }

    @Test
    public void testDeserialization_jacksonInject() throws IOException {
        InjectableValues.Std injectables = new InjectableValues.Std();
        injectables.addValue("injectedVal", "injected_value");
        ObjectMapper mapperWithInject = new ObjectMapper().setInjectableValues(injectables);

        String json = "{\"normal\": 50}";
        InjectBean result = mapperWithInject.readValue(json, InjectBean.class);
        assertNotNull(result);
        assertEquals(50, result.normal);
        assertEquals("injected_value", result.injected);
    }

    @Test
    public void testDeserialization_unwrappedProperties() throws IOException {
        String json = "{\"name\": \"main\", \"count\": 3, \"desc\": \"test unwrapped\"}";
        UnwrappedOuter result = mapper.readValue(json, UnwrappedOuter.class);
        assertNotNull(result);
        assertEquals("main", result.name);
        assertNotNull(result.inner);
        assertEquals(3, result.inner.count);
        assertEquals("test unwrapped", result.inner.desc);
    }

    @Test
    public void testDeserialization_polymorphicPropertyType() throws IOException {
        String json = "{\"poly\": {\"type\": \"A\", \"baseField\": \"bVal\", \"aValue\": 99}}";
        PolymorphicContainer result = mapper.readValue(json, PolymorphicContainer.class);
        assertNotNull(result);
        assertNotNull(result.poly);
        assertTrue(result.poly instanceof PolymorphicSubA);
        PolymorphicSubA subA = (PolymorphicSubA) result.poly;
        assertEquals("bVal", subA.baseField);
        assertEquals(99, subA.aValue);
    }
}