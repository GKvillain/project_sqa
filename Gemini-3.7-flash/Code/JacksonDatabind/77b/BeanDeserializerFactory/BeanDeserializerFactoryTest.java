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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.BasicClassIntrospector;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class BeanDeserializerFactoryTest {

    private BeanDeserializerFactory factory;
    private ObjectMapper mapper;

    // Helper classes for test cases

    public static class SimpleBean {
        public String name;
        public int age;

        public SimpleBean() {}

        public SimpleBean(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    public static class CustomException extends Exception {
        private static final long serialVersionUID = 1L;
        public int extraInfo;

        public CustomException() { super(); }
        public CustomException(String msg) { super(msg); }
        public void setExtraInfo(int val) { this.extraInfo = val; }
    }

    @JsonDeserialize(builder = ValueBuilder.class)
    public static class ValueWithBuilder {
        private final int x;
        private final String y;

        ValueWithBuilder(int x, String y) {
            this.x = x;
            this.y = y;
        }

        public int getX() { return x; }
        public String getY() { return y; }
    }

    @JsonPOJOBuilder(buildMethodName = "create", withPrefix = "with")
    public static class ValueBuilder {
        private int x;
        private String y;

        public ValueBuilder withX(int x) {
            this.x = x;
            return this;
        }

        public ValueBuilder withY(String y) {
            this.y = y;
            return this;
        }

        public ValueWithBuilder create() {
            return new ValueWithBuilder(x, y);
        }
    }

    public interface AbstractModel {
        String getValue();
    }

    public static class ConcreteModel implements AbstractModel {
        private String value;
        public ConcreteModel() {}
        public ConcreteModel(String value) { this.value = value; }
        @Override
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    @JsonIgnoreProperties({"ignoredField"})
    public static class IgnoredPropsBean {
        public String normalField;
        public String ignoredField;
        @JsonIgnore
        public String explicitIgnored;

        public IgnoredPropsBean() {}
    }

    public static class AnySetterBean {
        public String name;
        private final Map<String, Object> extra = new HashMap<String, Object>();

        public AnySetterBean() {}

        @JsonAnySetter
        public void handleUnknown(String key, Object value) {
            extra.put(key, value);
        }

        public Map<String, Object> getExtra() { return extra; }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class IdBean {
        public int id;
        public String name;
        public IdBean next;

        public IdBean() {}
        public IdBean(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class Parent {
        public String name;
        @JsonManagedReference
        public Child child;
    }

    public static class Child {
        public String name;
        @JsonBackReference
        public Parent parent;
    }

    public static class SetterlessCollectionBean {
        private final List<String> items = new ArrayList<String>();
        public List<String> getItems() { return items; }
    }

    public static class InjectedBean {
        @JacksonInject("injectValue")
        public String injected;
        public String normal;
    }

    public static class CreatorBean {
        public final String a;
        public final int b;

        @JsonCreator
        public CreatorBean(@JsonProperty("a") String a, @JsonProperty("b") int b) {
            this.a = a;
            this.b = b;
        }
    }

    public static class SubclassedFactory extends BeanDeserializerFactory {
        private static final long serialVersionUID = 1L;

        public SubclassedFactory(DeserializerFactoryConfig config) {
            super(config);
        }
    }

    @Before
    public void setUp() {
        factory = BeanDeserializerFactory.instance;
        mapper = new ObjectMapper();
    }

    // Tests withConfig when passing the same config instance returns this
    @Test
    public void testWithConfig_sameConfig_returnsSameInstance() {
        DeserializerFactoryConfig config = factory.getFactoryConfig();
        DeserializerFactory result = factory.withConfig(config);
        assertSame(factory, result);
    }

    // Tests withConfig when passing a new config instance returns a new BeanDeserializerFactory
    @Test
    public void testWithConfig_newConfig_returnsNewInstance() {
        DeserializerFactoryConfig newConfig = new DeserializerFactoryConfig();
        DeserializerFactory result = factory.withConfig(newConfig);
        assertNotNull(result);
        assertNotSame(factory, result);
        assertEquals(BeanDeserializerFactory.class, result.getClass());
    }

    // Tests withConfig throws IllegalStateException when subclass does not override withConfig
    @Test(expected = IllegalStateException.class)
    public void testWithConfig_unsupportedSubtype_throwsIllegalStateException() {
        SubclassedFactory customFactory = new SubclassedFactory(new DeserializerFactoryConfig());
        customFactory.withConfig(new DeserializerFactoryConfig());
    }

    // Tests isPotentialBeanType with normal class returns true
    @Test
    public void testIsPotentialBeanType_normalClass_returnsTrue() {
        assertTrue(factory.isPotentialBeanType(SimpleBean.class));
    }

    // Tests isPotentialBeanType with primitive throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_primitiveType_throwsIllegalArgumentException() {
        factory.isPotentialBeanType(int.class);
    }

    // Tests isPotentialBeanType with array throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_arrayType_throwsIllegalArgumentException() {
        factory.isPotentialBeanType(int[].class);
    }

    // Tests deserialization of a normal Bean
    @Test
    public void testCreateBeanDeserializer_normalBean_deserializesCorrectly() throws IOException {
        String json = "{\"name\":\"Alice\",\"age\":30}";
        SimpleBean result = mapper.readValue(json, SimpleBean.class);
        assertNotNull(result);
        assertEquals("Alice", result.getName());
        assertEquals(30, result.getAge());
    }

    // Tests Throwable deserialization via buildThrowableDeserializer
    @Test
    public void testCreateBeanDeserializer_throwableType_deserializesCorrectly() throws IOException {
        String json = "{\"message\":\"Something went wrong\",\"extraInfo\":42,\"cause\":{\"message\":\"root cause\"}}";
        CustomException result = mapper.readValue(json, CustomException.class);
        assertNotNull(result);
        assertEquals("Something went wrong", result.getMessage());
        assertEquals(42, result.extraInfo);
        assertNotNull(result.getCause());
        assertEquals("root cause", result.getCause().getMessage());
    }

    // Tests builder-based deserialization via createBuilderBasedDeserializer
    @Test
    public void testCreateBuilderBasedDeserializer_withCustomBuilder_deserializesCorrectly() throws IOException {
        String json = "{\"x\":100,\"y\":\"hello\"}";
        ValueWithBuilder result = mapper.readValue(json, ValueWithBuilder.class);
        assertNotNull(result);
        assertEquals(100, result.getX());
        assertEquals("hello", result.getY());
    }

    // Tests abstract type materialization via AbstractTypeResolver
    @Test
    public void testCreateBeanDeserializer_abstractTypeWithResolver_deserializesConcreteInstance() throws IOException {
        SimpleModule module = new SimpleModule();
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(AbstractModel.class, ConcreteModel.class);
        module.setAbstractTypes(resolver);

        ObjectMapper customMapper = new ObjectMapper();
        customMapper.registerModule(module);

        String json = "{\"value\":\"testAbstract\"}";
        AbstractModel result = customMapper.readValue(json, AbstractModel.class);
        assertNotNull(result);
        assertTrue(result instanceof ConcreteModel);
        assertEquals("testAbstract", result.getValue());
    }

    // Tests BeanDeserializerModifier modifying builder and properties
    @Test
    public void testBuildBeanDeserializer_withDeserializerModifier_appliesModifier() throws IOException {
        final boolean[] modifierCalled = new boolean[2];
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier() {
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
        });

        ObjectMapper customMapper = new ObjectMapper();
        customMapper.registerModule(module);

        SimpleBean result = customMapper.readValue("{\"name\":\"Bob\",\"age\":25}", SimpleBean.class);
        assertNotNull(result);
        assertEquals("Bob", result.getName());
        assertTrue(modifierCalled[0]);
        assertTrue(modifierCalled[1]);
    }

    // Tests handling of ignored properties (@JsonIgnore, @JsonIgnoreProperties)
    @Test
    public void testAddBeanProps_withIgnoredProperties_ignoresSpecifiedFields() throws IOException {
        String json = "{\"normalField\":\"val\",\"ignoredField\":\"skip\",\"explicitIgnored\":\"skip2\"}";
        IgnoredPropsBean result = mapper.readValue(json, IgnoredPropsBean.class);
        assertNotNull(result);
        assertEquals("val", result.normalField);
        assertNull(result.ignoredField);
        assertNull(result.explicitIgnored);
    }

    // Tests any setter handling via @JsonAnySetter
    @Test
    public void testConstructAnySetter_withAnySetterAnnotation_storesUnknownProperties() throws IOException {
        String json = "{\"name\":\"Test\",\"dynamic1\":\"val1\",\"dynamic2\":123}";
        AnySetterBean result = mapper.readValue(json, AnySetterBean.class);
        assertNotNull(result);
        assertEquals("Test", result.name);
        assertEquals("val1", result.getExtra().get("dynamic1"));
        assertEquals(123, result.getExtra().get("dynamic2"));
    }

    // Tests Object Id resolution and PropertyGenerator
    @Test
    public void testAddObjectIdReader_withPropertyGenerator_resolvesObjectIds() throws IOException {
        String json = "{\"id\":1,\"name\":\"first\",\"next\":1}";
        IdBean result = mapper.readValue(json, IdBean.class);
        assertNotNull(result);
        assertEquals(1, result.id);
        assertEquals("first", result.name);
        assertSame(result, result.next);
    }

    // Tests managed and back reference properties
    @Test
    public void testAddReferenceProperties_managedAndBackReferences_linksObjects() throws IOException {
        String json = "{\"name\":\"ParentName\",\"child\":{\"name\":\"ChildName\"}}";
        Parent parent = mapper.readValue(json, Parent.class);
        assertNotNull(parent);
        assertEquals("ParentName", parent.name);
        assertNotNull(parent.child);
        assertEquals("ChildName", parent.child.name);
        assertSame(parent, parent.child.parent);
    }

    // Tests setterless collection property
    @Test
    public void testConstructSetterlessProperty_collectionPropertyWithoutSetter_populatesCollection() throws IOException {
        String json = "{\"items\":[\"a\",\"b\",\"c\"]}";
        SetterlessCollectionBean result = mapper.readValue(json, SetterlessCollectionBean.class);
        assertNotNull(result);
        assertEquals(Arrays.asList("a", "b", "c"), result.getItems());
    }

    // Tests creator properties with @JsonCreator
    @Test
    public void testAddBeanProps_withCreatorProperties_instantiatesViaCreator() throws IOException {
        String json = "{\"a\":\"foo\",\"b\":99}";
        CreatorBean result = mapper.readValue(json, CreatorBean.class);
        assertNotNull(result);
        assertEquals("foo", result.a);
        assertEquals(99, result.b);
    }

    // Tests injectable values with @JacksonInject
    @Test
    public void testAddInjectables_withJacksonInject_injectsConfiguredValue() throws IOException {
        InjectableValues.Std injectables = new InjectableValues.Std();
        injectables.addValue("injectValue", "InjectedString");

        ObjectMapper customMapper = new ObjectMapper();
        customMapper.setInjectableValues(injectables);

        String json = "{\"normal\":\"normalValue\"}";
        InjectedBean result = customMapper.readValue(json, InjectedBean.class);
        assertNotNull(result);
        assertEquals("normalValue", result.normal);
        assertEquals("InjectedString", result.injected);
    }
}