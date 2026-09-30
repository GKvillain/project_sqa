package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.exc.IgnoredPropertyException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.util.NameTransformer;

public class BeanDeserializerBaseTest {

    private final ObjectMapper MAPPER = new ObjectMapper();

    static class SimpleBean {
        public int a;
        public String b;

        public SimpleBean() {}

        public SimpleBean(int a, String b) {
            this.a = a;
            this.b = b;
        }
    }

    @JsonIgnoreProperties({"ignoredField"})
    static class IgnoredBean {
        public int a;
        public String ignoredField;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class IgnoreUnknownBean {
        public int a;
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    static class ArrayShapeBean {
        public int a;
        public String b;
    }

    static class ContainerWithIgnorals {
        @JsonIgnoreProperties({"b"})
        public SimpleBean bean;
    }

    static class CaseInsensitiveBean {
        @JsonFormat(with = {JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES})
        public SimpleBean bean;
    }

    private BeanDeserializerBase getBeanDeserializer(Class<?> cls) throws Exception {
        ObjectReader reader = MAPPER.readerFor(cls);
        reader.readValue("{}");
        DefaultDeserializationContext ctxt = (DefaultDeserializationContext) MAPPER.getDeserializationContext();
        ctxt = ctxt.createInstance(MAPPER.getDeserializationConfig(), MAPPER.getFactory().createParser("{}"), null);
        return (BeanDeserializerBase) ctxt.findRootValueDeserializer(MAPPER.constructType(cls));
    }

    // Tests handledType returns the bean's raw class
    @Test
    public void testHandledType_returnsCorrectClass() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        assertEquals(SimpleBean.class, deser.handledType());
    }

    // Tests getValueType returns correct JavaType
    @Test
    public void testGetValueType_returnsBeanJavaType() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        JavaType javaType = deser.getValueType();
        assertNotNull(javaType);
        assertEquals(SimpleBean.class, javaType.getRawClass());
    }

    // Tests isCachable returns true
    @Test
    public void testIsCachable_returnsTrue() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        assertTrue(deser.isCachable());
    }

    // Tests getPropertyCount returns correct number of properties
    @Test
    public void testGetPropertyCount_returnsCorrectCount() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        assertEquals(2, deser.getPropertyCount());
    }

    // Tests hasProperty returns true for existing and false for non-existing property
    @Test
    public void testHasProperty_returnsExpectedBoolean() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        assertTrue(deser.hasProperty("a"));
        assertTrue(deser.hasProperty("b"));
        assertFalse(deser.hasProperty("nonExistent"));
    }

    // Tests findProperty by String name
    @Test
    public void testFindProperty_byName_returnsPropertyOrNull() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        SettableBeanProperty propA = deser.findProperty("a");
        assertNotNull(propA);
        assertEquals("a", propA.getName());

        SettableBeanProperty propNull = deser.findProperty("unknown");
        assertNull(propNull);
    }

    // Tests findProperty by PropertyName
    @Test
    public void testFindProperty_byPropertyName_returnsProperty() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        SettableBeanProperty prop = deser.findProperty(new PropertyName("b"));
        assertNotNull(prop);
        assertEquals("b", prop.getName());
    }

    // Tests findProperty by index
    @Test
    public void testFindProperty_byIndex_returnsPropertyOrNull() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        SettableBeanProperty prop = deser.findProperty(0);
        assertNotNull(prop);

        SettableBeanProperty outOfBounds = deser.findProperty(999);
        assertNull(outOfBounds);
    }

    // Tests getKnownPropertyNames returns collection containing all property names
    @Test
    public void testGetKnownPropertyNames_returnsAllNames() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        Collection<Object> names = deser.getKnownPropertyNames();
        assertEquals(2, names.size());
        assertTrue(names.contains("a"));
        assertTrue(names.contains("b"));
    }

    // Tests properties() iterator provides all properties
    @Test
    public void testProperties_iteratesAllProperties() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        Iterator<SettableBeanProperty> it = deser.properties();
        int count = 0;
        while (it.hasNext()) {
            SettableBeanProperty prop = it.next();
            assertNotNull(prop);
            count++;
        }
        assertEquals(2, count);
    }

    // Tests default withBeanProperties implementation throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testWithBeanProperties_defaultImplementation_throwsUnsupportedOperationException() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        BeanDeserializerBase custom = new BeanDeserializerBase(deser) {
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
            public Object deserializeFromObject(JsonParser p, DeserializationContext ctxt) throws IOException {
                return null;
            }

            @Override
            protected Object _deserializeUsingPropertyBased(JsonParser p, DeserializationContext ctxt) throws IOException {
                return null;
            }
        };

        custom.withBeanProperties(BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false));
    }

    // Tests wrapAndThrow with RuntimeException wraps inside JsonMappingException
    @Test
    public void testWrapAndThrow_withFieldName_wrapsInJsonMappingException() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        DefaultDeserializationContext ctxt = (DefaultDeserializationContext) MAPPER.getDeserializationContext();
        ctxt = ctxt.createInstance(MAPPER.getDeserializationConfig(), MAPPER.getFactory().createParser("{}"), null);

        try {
            deser.wrapAndThrow(new NumberFormatException("bad input"), new SimpleBean(), "a", ctxt);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("bad input"));
            assertEquals(1, e.getPath().size());
            assertEquals("a", e.getPath().get(0).getFieldName());
        }
    }

    // Tests wrapAndThrow with index wraps inside JsonMappingException
    @Test
    @SuppressWarnings("deprecation")
    public void testWrapAndThrow_withIndex_wrapsInJsonMappingException() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        DefaultDeserializationContext ctxt = (DefaultDeserializationContext) MAPPER.getDeserializationContext();
        ctxt = ctxt.createInstance(MAPPER.getDeserializationConfig(), MAPPER.getFactory().createParser("{}"), null);

        try {
            deser.wrapAndThrow(new IllegalArgumentException("invalid index"), new SimpleBean(), 2, ctxt);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("invalid index"));
            assertEquals(1, e.getPath().size());
            assertEquals(2, e.getPath().get(0).getIndex());
        }
    }

    // Tests wrapAndThrow propagates Error without wrapping
    @Test(expected = OutOfMemoryError.class)
    public void testWrapAndThrow_errorPropagatedDirectly() throws Exception {
        BeanDeserializerBase deser = getBeanDeserializer(SimpleBean.class);
        DefaultDeserializationContext ctxt = (DefaultDeserializationContext) MAPPER.getDeserializationContext();
        ctxt = ctxt.createInstance(MAPPER.getDeserializationConfig(), MAPPER.getFactory().createParser("{}"), null);

        deser.wrapAndThrow(new OutOfMemoryError("OOM"), new SimpleBean(), "a", ctxt);
    }

    // Tests deserializing unknown property throws UnrecognizedPropertyException by default
    @Test(expected = UnrecognizedPropertyException.class)
    public void testHandleUnknownProperty_throwsException() throws Exception {
        MAPPER.readValue("{\"unknownProp\": 123}", SimpleBean.class);
    }

    // Tests deserializing ignored property succeeds and ignores value
    @Test
    public void testHandleIgnoredProperty_skipsIgnoredField() throws Exception {
        IgnoredBean bean = MAPPER.readValue("{\"a\": 5, \"ignoredField\": \"test\"}", IgnoredBean.class);
        assertNotNull(bean);
        assertEquals(5, bean.a);
        assertNull(bean.ignoredField);
    }

    // Tests deserializing ignored property throws when FAIL_ON_IGNORED_PROPERTIES is enabled
    @Test(expected = IgnoredPropertyException.class)
    public void testHandleIgnoredProperty_failsWhenFeatureEnabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        mapper.readValue("{\"a\": 5, \"ignoredField\": \"test\"}", IgnoredBean.class);
    }

    // Tests ignoreUnknown class annotation ignores unknown properties
    @Test
    public void testIgnoreAllUnknown_skipsUnknownProperties() throws Exception {
        IgnoreUnknownBean bean = MAPPER.readValue("{\"a\": 10, \"extra\": \"ignored\"}", IgnoreUnknownBean.class);
        assertNotNull(bean);
        assertEquals(10, bean.a);
    }

    // Tests deserialization with JSON Array shape
    @Test
    public void testDeserialize_asArrayShape() throws Exception {
        ArrayShapeBean bean = MAPPER.readValue("[42, \"hello\"]", ArrayShapeBean.class);
        assertNotNull(bean);
        assertEquals(42, bean.a);
        assertEquals("hello", bean.b);
    }

    // Tests contextualization with per-property @JsonIgnoreProperties override
    @Test
    public void testCreateContextual_withPerPropertyIgnorals() throws Exception {
        ContainerWithIgnorals result = MAPPER.readValue(
                "{\"bean\": {\"a\": 1, \"b\": \"ignored\"}}", ContainerWithIgnorals.class);
        assertNotNull(result);
        assertNotNull(result.bean);
        assertEquals(1, result.bean.a);
        assertNull(result.bean.b);
    }

    // Tests contextualization with ACCEPT_CASE_INSENSITIVE_PROPERTIES feature
    @Test
    public void testCreateContextual_caseInsensitiveProperties() throws Exception {
        CaseInsensitiveBean result = MAPPER.readValue(
                "{\"bean\": {\"A\": 10, \"B\": \"case\"}}", CaseInsensitiveBean.class);
        assertNotNull(result);
        assertNotNull(result.bean);
        assertEquals(10, result.bean.a);
        assertEquals("case", result.bean.b);
    }
}