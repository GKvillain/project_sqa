package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.IgnoredPropertyException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.junit.Assert.*;

public class BeanDeserializerBaseTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonFactory factory = new JsonFactory();

    interface ViewA {}

    static class SimpleBean {
        public String name;
        public int age;

        public SimpleBean() {}

        public SimpleBean(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    static class ViewBean {
        @JsonView(ViewA.class)
        public String propA;
        public String propB;
    }

    static class CreatorBean {
        final String name;
        final int age;

        @JsonCreator
        public CreatorBean(@JsonProperty("name") String name, @JsonProperty("age") int age) {
            this.name = name;
            this.age = age;
        }
    }

    static class StringDelegateBean {
        final String value;

        @JsonCreator
        public StringDelegateBean(String value) {
            this.value = value;
        }
    }

    static class NumberDelegateBean {
        final long num;

        @JsonCreator
        public NumberDelegateBean(long num) {
            this.num = num;
        }
    }

    static class BooleanDelegateBean {
        final boolean flag;

        @JsonCreator
        public BooleanDelegateBean(boolean flag) {
            this.flag = flag;
        }
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    static class ArrayShapeBean {
        public String propA;
        public String propB;

        public ArrayShapeBean() {}
    }

    static class IgnoredBean {
        public String propA;

        @JsonIgnoreProperties({"ignoredField"})
        public SimpleBean nested;
    }

    private BeanDeserializerBase getBeanDeserializer(Class<?> cls) throws Exception {
        JavaType type = mapper.constructType(cls);
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), factory.createParser("{}"), null);
        JsonDeserializer<?> deser = ctxt.findRootValueDeserializer(type);
        if (deser instanceof BeanDeserializerBase) {
            return (BeanDeserializerBase) deser;
        }
        BeanDeserializerFactory deserFactory = BeanDeserializerFactory.instance;
        JsonDeserializer<?> created = deserFactory.createBeanDeserializer(ctxt, type, mapper.getDeserializationConfig().introspect(type));
        if (created instanceof ResolvableDeserializer) {
            ((ResolvableDeserializer) created).resolve(ctxt);
        }
        return (BeanDeserializerBase) created;
    }

    // Tests handledType returns correct bean raw class
    @SuppressWarnings("deprecation")
    @Test
    public void testHandledType_standardBean_returnsCorrectClass() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        assertEquals(SimpleBean.class, deser.handledType());
        assertEquals(SimpleBean.class, deser.getBeanClass());
    }

    // Tests isCachable returns true for standard bean deserializer
    @Test
    public void testIsCachable_standardBean_returnsTrue() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        assertTrue(deser.isCachable());
    }

    // Tests property lookup and existence check
    @Test
    public void testFindProperty_existingAndNonExisting_returnsPropertyOrNull() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        assertNotNull(deser.findProperty("name"));
        assertNotNull(deser.findProperty("age"));
        assertNotNull(deser.findProperty(PropertyName.construct("name")));
        assertNull(deser.findProperty("unknownField"));
        assertNull(deser.findProperty(PropertyName.construct("unknownField")));
        assertTrue(deser.hasProperty("name"));
        assertFalse(deser.hasProperty("unknownField"));
        assertEquals(2, deser.getPropertyCount());
    }

    // Tests findProperty by index
    @Test
    public void testFindProperty_byIndex_returnsPropertyOrNull() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        SettableBeanProperty prop0 = deser.findProperty(0);
        assertNotNull(prop0);
        SettableBeanProperty invalidProp = deser.findProperty(999);
        assertNull(invalidProp);
    }

    // Tests getKnownPropertyNames collects all registered property names
    @Test
    public void testGetKnownPropertyNames_standardBean_returnsAllPropertyNames() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        Collection<Object> names = deser.getKnownPropertyNames();
        assertTrue(names.contains("name"));
        assertTrue(names.contains("age"));
        assertEquals(2, names.size());
    }

    // Tests properties iterator
    @Test
    public void testProperties_standardBean_iteratesAllProperties() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        Iterator<SettableBeanProperty> it = deser.properties();
        int count = 0;
        while (it.hasNext()) {
            assertNotNull(it.next());
            count++;
        }
        assertEquals(2, count);
    }

    // Tests creatorProperties on property-based creator bean
    @Test
    public void testCreatorProperties_creatorBean_returnsCreatorProperties() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(CreatorBean.class);
        Iterator<SettableBeanProperty> it = deser.creatorProperties();
        int count = 0;
        while (it.hasNext()) {
            assertNotNull(it.next());
            count++;
        }
        assertEquals(2, count);
    }

    // Tests findBackReference returns null when no back reference is configured
    @Test
    public void testFindBackReference_noBackRefs_returnsNull() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        assertNull(deser.findBackReference("nonExistentBackRef"));
    }

    // Tests deserializing from String using delegating creator
    @Test
    public void testDeserializeFromString_delegatingStringCreator_deserializesCorrectly() throws Exception {
        String json = "\"testValue\"";
        StringDelegateBean result = mapper.readValue(json, StringDelegateBean.class);
        assertNotNull(result);
        assertEquals("testValue", result.value);
    }

    // Tests deserializing from Number using delegating creator
    @Test
    public void testDeserializeFromNumber_delegatingNumberCreator_deserializesCorrectly() throws Exception {
        String json = "12345";
        NumberDelegateBean result = mapper.readValue(json, NumberDelegateBean.class);
        assertNotNull(result);
        assertEquals(12345L, result.num);
    }

    // Tests deserializing from Boolean using delegating creator
    @Test
    public void testDeserializeFromBoolean_delegatingBooleanCreator_deserializesCorrectly() throws Exception {
        String json = "true";
        BooleanDelegateBean result = mapper.readValue(json, BooleanDelegateBean.class);
        assertNotNull(result);
        assertTrue(result.flag);
    }

    // Tests array unwrapping when UNWRAP_SINGLE_VALUE_ARRAYS is enabled
    @Test
    public void testDeserializeFromArray_unwrapSingleValueArrays_deserializesCorrectly() throws Exception {
        ObjectMapper unwrapMapper = new ObjectMapper();
        unwrapMapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        String json = "[{\"name\":\"Bob\",\"age\":25}]";
        SimpleBean bean = unwrapMapper.readValue(json, SimpleBean.class);
        assertNotNull(bean);
        assertEquals("Bob", bean.name);
        assertEquals(25, bean.age);
    }

    // Tests deserializing empty array as null object
    @Test
    public void testDeserializeFromArray_acceptEmptyArrayAsNullObject_returnsNull() throws Exception {
        ObjectMapper emptyArrayMapper = new ObjectMapper();
        emptyArrayMapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        String json = "[]";
        SimpleBean bean = emptyArrayMapper.readValue(json, SimpleBean.class);
        assertNull(bean);
    }

    // Tests handling unknown properties with fail on unknown enabled
    @Test(expected = UnrecognizedPropertyException.class)
    public void testHandleUnknownProperty_failOnUnknown_throwsUnrecognizedPropertyException() throws Exception {
        String json = "{\"name\":\"Bob\",\"age\":25,\"unknown\":123}";
        mapper.readValue(json, SimpleBean.class);
    }

    // Tests handling unknown properties when FAIL_ON_UNKNOWN_PROPERTIES is disabled
    @Test
    public void testHandleUnknownProperty_failOnUnknownDisabled_ignoresProperty() throws Exception {
        ObjectMapper lenientMapper = new ObjectMapper();
        lenientMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        String json = "{\"name\":\"Bob\",\"age\":25,\"unknown\":123}";
        SimpleBean bean = lenientMapper.readValue(json, SimpleBean.class);
        assertNotNull(bean);
        assertEquals("Bob", bean.name);
        assertEquals(25, bean.age);
    }

    // Tests handleIgnoredProperty throws exception when FAIL_ON_IGNORED_PROPERTIES is enabled
    @Test(expected = IgnoredPropertyException.class)
    public void testHandleIgnoredProperty_failOnIgnoredEnabled_throwsIgnoredPropertyException() throws Exception {
        ObjectMapper failIgnoredMapper = new ObjectMapper();
        failIgnoredMapper.enable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        String json = "{\"propA\":\"val\",\"nested\":{\"name\":\"A\",\"age\":1,\"ignoredField\":\"ignoreMe\"}}";
        failIgnoredMapper.readValue(json, IgnoredBean.class);
    }

    // Tests wrapAndThrow wraps checked exception into JsonMappingException
    @Test
    public void testWrapAndThrow_checkedException_wrapsInJsonMappingException() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), factory.createParser("{}"), null);
        try {
            deser.wrapAndThrow(new IllegalArgumentException("test failure"), new SimpleBean(), "name", ctxt);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("test failure"));
            assertEquals(1, e.getPath().size());
            assertEquals("name", e.getPath().get(0).getFieldName());
        }
    }

    // Tests wrapAndThrow with index adds correct reference index
    @SuppressWarnings("deprecation")
    @Test
    public void testWrapAndThrow_withIndex_wrapsWithPathIndex() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), factory.createParser("{}"), null);
        try {
            deser.wrapAndThrow(new RuntimeException("index error"), new SimpleBean(), 3, ctxt);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("index error"));
            assertEquals(1, e.getPath().size());
            assertEquals(3, e.getPath().get(0).getIndex());
        }
    }

    // Tests Shape.ARRAY format creates array-based deserializer
    @Test
    public void testCreateContextual_arrayShape_deserializesFromArray() throws Exception {
        String json = "[\"first\",\"second\"]";
        ArrayShapeBean bean = mapper.readValue(json, ArrayShapeBean.class);
        assertNotNull(bean);
        assertEquals("first", bean.propA);
        assertEquals("second", bean.propB);
    }

    // Tests withBeanProperties throws UnsupportedOperationException by default
    @Test(expected = UnsupportedOperationException.class)
    public void testWithBeanProperties_defaultImpl_throwsUnsupportedOperationException() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        deser.withBeanProperties(deser._beanProperties);
    }

    // Tests ValueInstantiator and ObjectIdReader accessors
    @Test
    public void testAccessors_valueInstantiatorAndViews() throws Exception {
        BeanDeserializerBase simpleDeser = getBeanDeserializer(SimpleBean.class);
        assertNotNull(simpleDeser.getValueInstantiator());
        assertNull(simpleDeser.getObjectIdReader());
        assertFalse(simpleDeser.hasViews());

        BeanDeserializerBase viewDeser = getBeanDeserializer(ViewBean.class);
        assertTrue(viewDeser.hasViews());
    }

    // Tests replaceProperty replaces an existing property
    @Test
    public void testReplaceProperty_replacesSettableProperty() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        SettableBeanProperty prop = deser.findProperty("name");
        assertNotNull(prop);
        SettableBeanProperty renamed = prop.withSimpleName("renamedName");
        deser.replaceProperty(prop, renamed);
        assertNotNull(deser.findProperty("renamedName"));
        assertNull(deser.findProperty("name"));
    }
}