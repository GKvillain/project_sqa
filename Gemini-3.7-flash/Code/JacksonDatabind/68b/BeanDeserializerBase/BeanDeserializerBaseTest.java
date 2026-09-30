package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.exc.IgnoredPropertyException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.util.NameTransformer;

public class BeanDeserializerBaseTest {

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
        public String name;
        public String ignoredField;
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    static class ArrayFormatBean {
        public String a;
        public int b;
    }

    static class StringCtorBean {
        String value;
        public StringCtorBean(String v) {
            this.value = v;
        }
    }

    static class DoubleCtorBean {
        double value;
        public DoubleCtorBean(double v) {
            this.value = v;
        }
    }

    static class BooleanCtorBean {
        boolean value;
        public BooleanCtorBean(boolean v) {
            this.value = v;
        }
    }

    static class IntCtorBean {
        int value;
        public IntCtorBean(int v) {
            this.value = v;
        }
    }

    static class LongCtorBean {
        long value;
        public LongCtorBean(long v) {
            this.value = v;
        }
    }

    static class DummyDeserializer extends BeanDeserializerBase {
        private static final long serialVersionUID = 1L;

        protected DummyDeserializer(BeanDeserializerBase src) {
            super(src);
        }

        @Override
        public JsonDeserializer<Object> unwrappingDeserializer(NameTransformer unwrapper) {
            return this;
        }

        @Override
        public BeanDeserializerBase withObjectIdReader(ObjectIdReader oir) {
            return this;
        }

        @Override
        public BeanDeserializerBase withIgnorableProperties(Set<String> ignorableProps) {
            return this;
        }

        @Override
        protected BeanDeserializerBase asArrayDeserializer() {
            return this;
        }

        @Override
        public Object deserializeFromObject(JsonParser p, DeserializationContext ctxt) {
            return null;
        }

        @Override
        protected Object _deserializeUsingPropertyBased(JsonParser p, DeserializationContext ctxt) {
            return null;
        }
    }

    private final ObjectMapper mapper = new ObjectMapper();

    private BeanDeserializerBase getBeanDeserializer(Class<?> cls) throws IOException {
        JavaType type = mapper.constructType(cls);
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);
        JsonDeserializer<?> deser = ctxt.findRootValueDeserializer(type);
        if (deser instanceof BeanDeserializerBase) {
            return (BeanDeserializerBase) deser;
        }
        fail("Expected BeanDeserializerBase, got: " + deser);
        return null;
    }

    // Tests metadata and accessor methods on standard BeanDeserializerBase
    @Test
    public void testHandledTypeAndAccessors_validBean_returnsCorrectMetadata() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);

        assertEquals(SimpleBean.class, deser.handledType());
        assertEquals(SimpleBean.class, deser.getBeanClass());
        assertEquals(mapper.constructType(SimpleBean.class), deser.getValueType());
        assertTrue(deser.isCachable());
        assertFalse(deser.hasViews());
        assertEquals(2, deser.getPropertyCount());
        assertNotNull(deser.getValueInstantiator());

        Collection<Object> names = deser.getKnownPropertyNames();
        assertTrue(names.contains("name"));
        assertTrue(names.contains("age"));
    }

    // Tests findProperty by name, PropertyName, and index
    @Test
    public void testFindProperty_existingAndNonExisting_returnsPropertyOrNull() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);

        assertTrue(deser.hasProperty("name"));
        assertTrue(deser.hasProperty("age"));
        assertFalse(deser.hasProperty("nonExisting"));

        assertNotNull(deser.findProperty("name"));
        assertNotNull(deser.findProperty(new PropertyName("age")));
        assertNull(deser.findProperty("nonExisting"));
        assertNull(deser.findProperty(new PropertyName("nonExisting")));

        SettableBeanProperty prop0 = deser.findProperty(0);
        SettableBeanProperty prop1 = deser.findProperty(1);
        assertNotNull(prop0);
        assertNotNull(prop1);
        assertNull(deser.findProperty(999));
    }

    // Tests creator properties and back references when empty/none
    @Test
    public void testCreatorPropertiesAndBackReference_emptyOrNone_returnsEmptyOrNull() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);

        Iterator<SettableBeanProperty> creatorProps = deser.creatorProperties();
        assertNotNull(creatorProps);
        assertFalse(creatorProps.hasNext());

        assertNull(deser.findBackReference("dummyRef"));
    }

    // Tests properties iterator
    @Test
    public void testProperties_standardBean_iteratesAllProperties() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);

        Iterator<SettableBeanProperty> it = deser.properties();
        assertNotNull(it);
        int count = 0;
        while (it.hasNext()) {
            SettableBeanProperty prop = it.next();
            assertNotNull(prop);
            count++;
        }
        assertEquals(2, count);
    }

    // Tests default implementation of withBeanProperties throwing UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testWithBeanProperties_baseImplementation_throwsUnsupportedOperationException() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        DummyDeserializer dummy = new DummyDeserializer(deser);
        dummy.withBeanProperties(null);
    }

    // Tests deserializeFromString using string-based creator
    @Test
    public void testDeserializeFromString_stringCreatorBean_constructsObject() throws Exception {
        String json = "\"hello\"";
        StringCtorBean result = mapper.readValue(json, StringCtorBean.class);
        assertNotNull(result);
        assertEquals("hello", result.value);
    }

    // Tests deserializeFromDouble using double-based creator
    @Test
    public void testDeserializeFromDouble_doubleCreatorBean_constructsObject() throws Exception {
        String json = "3.1415";
        DoubleCtorBean result = mapper.readValue(json, DoubleCtorBean.class);
        assertNotNull(result);
        assertEquals(3.1415, result.value, 0.0001);
    }

    // Tests deserializeFromBoolean using boolean-based creator
    @Test
    public void testDeserializeFromBoolean_booleanCreatorBean_constructsObject() throws Exception {
        String json = "true";
        BooleanCtorBean result = mapper.readValue(json, BooleanCtorBean.class);
        assertNotNull(result);
        assertTrue(result.value);
    }

    // Tests deserializeFromNumber using int and long creators
    @Test
    public void testDeserializeFromNumber_intAndLongCreatorBean_constructsObject() throws Exception {
        IntCtorBean intResult = mapper.readValue("42", IntCtorBean.class);
        assertNotNull(intResult);
        assertEquals(42, intResult.value);

        LongCtorBean longResult = mapper.readValue("1234567890123", LongCtorBean.class);
        assertNotNull(longResult);
        assertEquals(1234567890123L, longResult.value);
    }

    // Tests deserializeFromArray when UNWRAP_SINGLE_VALUE_ARRAYS feature is enabled
    @Test
    public void testDeserializeFromArray_unwrapSingleValueArrays_deserializesBean() throws Exception {
        ObjectMapper unwrapMapper = new ObjectMapper();
        unwrapMapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);

        String json = "[{\"name\":\"Alice\",\"age\":30}]";
        SimpleBean result = unwrapMapper.readValue(json, SimpleBean.class);
        assertNotNull(result);
        assertEquals("Alice", result.name);
        assertEquals(30, result.age);
    }

    // Tests deserializeFromArray when ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT is enabled
    @Test
    public void testDeserializeFromArray_acceptEmptyArrayAsNull_returnsNull() throws Exception {
        ObjectMapper emptyArrayMapper = new ObjectMapper();
        emptyArrayMapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);

        String json = "[]";
        SimpleBean result = emptyArrayMapper.readValue(json, SimpleBean.class);
        assertNull(result);
    }

    // Tests handleIgnoredProperty when FAIL_ON_IGNORED_PROPERTIES is enabled
    @Test(expected = IgnoredPropertyException.class)
    public void testHandleIgnoredProperty_failOnIgnoredEnabled_throwsIgnoredPropertyException() throws Exception {
        ObjectMapper failOnIgnoredMapper = new ObjectMapper();
        failOnIgnoredMapper.enable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);

        String json = "{\"name\":\"Alice\",\"ignoredField\":\"secret\"}";
        failOnIgnoredMapper.readValue(json, IgnoredPropsBean.class);
    }

    // Tests handleIgnoredProperty when FAIL_ON_IGNORED_PROPERTIES is disabled
    @Test
    public void testHandleIgnoredProperty_failOnIgnoredDisabled_skipsIgnoredProperty() throws Exception {
        String json = "{\"name\":\"Bob\",\"ignoredField\":\"secret\"}";
        IgnoredPropsBean result = mapper.readValue(json, IgnoredPropsBean.class);
        assertNotNull(result);
        assertEquals("Bob", result.name);
        assertNull(result.ignoredField);
    }

    // Tests handleUnknownProperty when FAIL_ON_UNKNOWN_PROPERTIES is enabled
    @Test(expected = UnrecognizedPropertyException.class)
    public void testHandleUnknownProperty_failOnUnknownEnabled_throwsUnrecognizedPropertyException() throws Exception {
        String json = "{\"name\":\"Bob\",\"unknownField\":\"value\"}";
        mapper.readValue(json, SimpleBean.class);
    }

    // Tests handleUnknownProperty when FAIL_ON_UNKNOWN_PROPERTIES is disabled
    @Test
    public void testHandleUnknownProperty_failOnUnknownDisabled_ignoresProperty() throws Exception {
        ObjectMapper ignoreUnknownMapper = new ObjectMapper();
        ignoreUnknownMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        String json = "{\"name\":\"Bob\",\"unknownField\":\"value\",\"age\":25}";
        SimpleBean result = ignoreUnknownMapper.readValue(json, SimpleBean.class);
        assertNotNull(result);
        assertEquals("Bob", result.name);
        assertEquals(25, result.age);
    }

    // Tests wrapAndThrow wrapping RuntimeException with path reference
    @Test
    public void testWrapAndThrow_runtimeException_wrapsInJsonMappingException() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);
        SimpleBean bean = new SimpleBean();

        try {
            deser.wrapAndThrow(new IllegalArgumentException("test error"), bean, "name", ctxt);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("test error"));
            assertEquals(1, e.getPath().size());
            assertEquals("name", e.getPath().get(0).getFieldName());
        }
    }

    // Tests wrapAndThrow passing through Error directly
    @Test(expected = OutOfMemoryError.class)
    public void testWrapAndThrow_error_rethrowsDirectly() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);
        deser.wrapAndThrow(new OutOfMemoryError("OOM"), new SimpleBean(), "age", ctxt);
    }

    // Tests createContextual adapting deserializer for Shape.ARRAY format
    @Test
    public void testCreateContextual_arrayShapeFormat_deserializesFromArray() throws Exception {
        String json = "[\"testString\", 100]";
        ArrayFormatBean result = mapper.readValue(json, ArrayFormatBean.class);
        assertNotNull(result);
        assertEquals("testString", result.a);
        assertEquals(100, result.b);
    }

    // Tests replaceProperty functionality
    @Test
    public void testReplaceProperty_existingProperty_replacesInMap() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        SettableBeanProperty orig = deser.findProperty("name");
        assertNotNull(orig);

        SettableBeanProperty replacement = orig.withSimpleName("name");
        deser.replaceProperty(orig, replacement);
        assertEquals(replacement, deser.findProperty("name"));
    }

    // Tests supportsUpdate and ObjectIdReader accessors
    @Test
    public void testSupportsUpdateAndObjectIdReader() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        assertTrue(deser.supportsUpdate(mapper.getDeserializationConfig()));
        assertNull(deser.getObjectIdReader());
    }

    // Tests getEmptyValue and getNullValue delegates
    @Test
    public void testGetEmptyAndNullValue() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);

        assertNull(deser.getNullValue(ctxt));
        Object emptyVal = deser.getEmptyValue(ctxt);
        assertNotNull(emptyVal);
        assertTrue(emptyVal instanceof SimpleBean);
    }
}