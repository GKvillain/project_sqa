package com.fasterxml.jackson.databind.deser;

import java.io.Serializable;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class BeanDeserializerFactoryTest {

    private ObjectMapper mapper;
    private BeanDeserializerFactory factory;
    private DeserializationContext ctxt;

    // Helper classes for testing
    static class SimpleBean {
        public int x;
        public String name;
    }

    @JsonIgnoreProperties({"ignoredField"})
    static class IgnoredPropsBean {
        public int a;
        public int ignoredField;
    }

    static class SetterlessBean {
        private List<String> list = new ArrayList<String>();
        private Map<String, String> map = new HashMap<String, String>();

        public List<String> getList() { return list; }
        public Map<String, String> getMap() { return map; }
    }

    static class AnySetterBean {
        public Map<String, Object> extra = new HashMap<String, Object>();

        @com.fasterxml.jackson.annotation.JsonAnySetter
        public void setExtra(String key, Object value) {
            extra.put(key, value);
        }
    }

    static class CreatorBean {
        public final int a;
        public final String b;

        @JsonCreator
        public CreatorBean(@JsonProperty("a") int a, @JsonProperty("b") String b) {
            this.a = a;
            this.b = b;
        }
    }

    @com.fasterxml.jackson.annotation.JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class ObjectIdBean {
        public int id;
        public String value;
    }

    @com.fasterxml.jackson.annotation.JsonDeserialize(builder = SimpleBuilder.class)
    static class ValueClassWithBuilder {
        final int value;

        ValueClassWithBuilder(int v) {
            this.value = v;
        }
    }

    @JsonPOJOBuilder(buildMethodName = "create", withPrefix = "with")
    static class SimpleBuilder {
        public int value;

        public SimpleBuilder withValue(int v) {
            this.value = v;
            return this;
        }

        public ValueClassWithBuilder create() {
            return new ValueClassWithBuilder(value);
        }
    }

    static class SubFactory extends BeanDeserializerFactory {
        public SubFactory(DeserializerFactoryConfig config) {
            super(config);
        }
    }

    public interface AbstractTypeInterface {
        public int getValue();
    }

    public static class AbstractTypeInterfaceImpl implements AbstractTypeInterface {
        private int value;
        public AbstractTypeInterfaceImpl() {}
        public AbstractTypeInterfaceImpl(int v) { this.value = v; }
        public void setValue(int v) { this.value = v; }
        public int getValue() { return value; }
    }

    static class CustomException extends Throwable {
        public CustomException(String msg) {
            super(msg);
        }
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        factory = BeanDeserializerFactory.instance;
        ctxt = mapper.getDeserializationContext();
    }

    // Tests singleton instance creation and withConfig with same config
    @Test
    public void testWithConfig_sameConfig_returnsSameInstance() {
        DeserializerFactoryConfig config = factory.getFactoryConfig();
        DeserializerFactory newFactory = factory.withConfig(config);
        assertSame(factory, newFactory);
    }

    // Tests withConfig with new config returns new instance
    @Test
    public void testWithConfig_differentConfig_returnsNewInstance() {
        DeserializerFactoryConfig config = new DeserializerFactoryConfig();
        DeserializerFactory newFactory = factory.withConfig(config);
        assertNotSame(factory, newFactory);
        assertEquals(BeanDeserializerFactory.class, newFactory.getClass());
    }

    // Tests withConfig when called on subclass without overriding throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testWithConfig_subclassWithoutOverride_throwsException() {
        SubFactory subFactory = new SubFactory(new DeserializerFactoryConfig());
        subFactory.withConfig(new DeserializerFactoryConfig());
    }

    // Tests deserializing standard bean
    @Test
    public void testCreateBeanDeserializer_standardBean_success() throws Exception {
        String json = "{\"x\": 10, \"name\": \"test\"}";
        SimpleBean result = mapper.readValue(json, SimpleBean.class);
        assertNotNull(result);
        assertEquals(10, result.x);
        assertEquals("test", result.name);
    }

    // Tests illegal types security check for JdbcRowSetImpl
    @Test(expected = JsonMappingException.class)
    public void testCheckIllegalTypes_jdbcRowSet_throwsException() throws Exception {
        Class<?> cls = Class.forName("com.sun.rowset.JdbcRowSetImpl");
        JavaType type = mapper.constructType(cls);
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription beanDesc = config.introspect(type);
        factory.checkIllegalTypes(ctxt, type, beanDesc);
    }

    // Tests illegal types security check passes for safe type
    @Test
    public void testCheckIllegalTypes_safeType_noException() throws Exception {
        JavaType type = mapper.constructType(SimpleBean.class);
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription beanDesc = config.introspect(type);
        factory.checkIllegalTypes(ctxt, type, beanDesc);
    }

    // Tests Throwable deserialization builder
    @Test
    public void testBuildThrowableDeserializer_customException_success() throws Exception {
        String json = "{\"message\": \"test error\"}";
        CustomException result = mapper.readValue(json, CustomException.class);
        assertNotNull(result);
        assertEquals("test error", result.getMessage());
    }

    // Tests abstract type materialization via AbstractTypeResolver
    @Test
    public void testMaterializeAbstractType_withResolver_resolvesConcrete() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(AbstractTypeInterface.class, AbstractTypeInterfaceImpl.class);
        SimpleModule module = new SimpleModule();
        module.setAbstractTypes(resolver);
        ObjectMapper absMapper = new ObjectMapper();
        absMapper.registerModule(module);

        String json = "{\"value\": 42}";
        AbstractTypeInterface result = absMapper.readValue(json, AbstractTypeInterface.class);
        assertNotNull(result);
        assertTrue(result instanceof AbstractTypeInterfaceImpl);
        assertEquals(42, result.getValue());
    }

    // Tests Builder-based deserializer
    @Test
    public void testCreateBuilderBasedDeserializer_success() throws Exception {
        String json = "{\"value\": 99}";
        ValueClassWithBuilder result = mapper.readValue(json, ValueClassWithBuilder.class);
        assertNotNull(result);
        assertEquals(99, result.value);
    }

    // Tests Creator properties deserialization
    @Test
    public void testAddBeanProps_creatorProperties_success() throws Exception {
        String json = "{\"a\": 5, \"b\": \"hello\"}";
        CreatorBean result = mapper.readValue(json, CreatorBean.class);
        assertNotNull(result);
        assertEquals(5, result.a);
        assertEquals("hello", result.b);
    }

    // Tests Setterless properties (Collections/Maps with only getter)
    @Test
    public void testAddBeanProps_setterlessProperties_success() throws Exception {
        String json = "{\"list\": [\"item1\", \"item2\"], \"map\": {\"k\": \"v\"}}";
        SetterlessBean result = mapper.readValue(json, SetterlessBean.class);
        assertNotNull(result);
        assertEquals(2, result.getList().size());
        assertEquals("item1", result.getList().get(0));
        assertEquals("v", result.getMap().get("k"));
    }

    // Tests AnySetter handling
    @Test
    public void testConstructAnySetter_success() throws Exception {
        String json = "{\"customKey\": \"customValue\"}";
        AnySetterBean result = mapper.readValue(json, AnySetterBean.class);
        assertNotNull(result);
        assertEquals("customValue", result.extra.get("customKey"));
    }

    // Tests Ignored properties filter
    @Test
    public void testFilterBeanProps_ignoredProperties_skipped() throws Exception {
        String json = "{\"a\": 1, \"ignoredField\": 2}";
        IgnoredPropsBean result = mapper.readValue(json, IgnoredPropsBean.class);
        assertNotNull(result);
        assertEquals(1, result.a);
        assertEquals(0, result.ignoredField);
    }

    // Tests Object Id property handling
    @Test
    public void testAddObjectIdReader_propertyBased_success() throws Exception {
        String json = "{\"id\": 123, \"value\": \"abc\"}";
        ObjectIdBean result = mapper.readValue(json, ObjectIdBean.class);
        assertNotNull(result);
        assertEquals(123, result.id);
        assertEquals("abc", result.value);
    }

    // Tests non-bean primitive type check
    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_primitive_throwsException() {
        factory.isPotentialBeanType(int.class);
    }

    // Tests non-bean array type check
    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_array_throwsException() {
        factory.isPotentialBeanType(int[].class);
    }
}