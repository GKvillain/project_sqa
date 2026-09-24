package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.InternString;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import com.fasterxml.jackson.databind.exc.IgnoredPropertyException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.util.NameTransformer;

public class BeanDeserializerBaseTest {

    static class SimpleBean {
        public int id;
        public String name;

        public SimpleBean() {}

        public SimpleBean(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    static class CreatorBean {
        final int x;
        final int y;

        @JsonCreator
        public CreatorBean(@JsonProperty("x") int x, @JsonProperty("y") int y) {
            this.x = x;
            this.y = y;
        }
    }

    @JsonIgnoreProperties({"ignoredField"})
    static class IgnoredPropBean {
        public int a;
        public String ignoredField;
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    static class ArrayShapeBean {
        public int a;
        public String b;
    }

    static class ParentRef {
        public int id;
        @JsonManagedReference
        public ChildRef child;
    }

    static class ChildRef {
        public String value;
        @JsonBackReference
        public ParentRef parent;
    }

    static class OuterBean {
        public InnerBean inner;

        public class InnerBean {
            public int val;

            public InnerBean(int val) {
                this.val = val;
            }
        }
    }

    static class UnwrappedWrapper {
        public String name;
        @JsonUnwrapped
        public UnwrappedChild child;
    }

    static class UnwrappedChild {
        public int age;
        public String city;
    }

    static class CustomDeserializerBase extends BeanDeserializerBase {
        private static final long serialVersionUID = 1L;

        public CustomDeserializerBase(BeanDeserializerBase src) {
            super(src);
        }

        @Override
        public JsonDeserializer<Object> unwrappingDeserializer(NameTransformer unwrapper) {
            return this;
        }

        @Override
        public BeanDeserializerBase withObjectIdReader(com.fasterxml.jackson.databind.deser.impl.ObjectIdReader oir) {
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
    }

    // Tests accessor methods on standard BeanDeserializerBase instance
    @Test
    public void testAccessors_standardBean_returnsExpectedMetadata() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        JsonDeserializer<?> deser = mapper.findRootValueDeserializer(type);

        assertTrue(deser instanceof BeanDeserializerBase);
        BeanDeserializerBase beanDeser = (BeanDeserializerBase) deser;

        assertEquals(SimpleBean.class, beanDeser.handledType());
        assertEquals(SimpleBean.class, beanDeser.getBeanClass());
        assertEquals(type, beanDeser.getValueType());
        assertTrue(beanDeser.isCachable());
        assertFalse(beanDeser.hasViews());
        assertNull(beanDeser.getObjectIdReader());

        assertEquals(2, beanDeser.getPropertyCount());
        assertTrue(beanDeser.hasProperty("id"));
        assertTrue(beanDeser.hasProperty("name"));
        assertFalse(beanDeser.hasProperty("unknown"));

        assertNotNull(beanDeser.findProperty("id"));
        assertNotNull(beanDeser.findProperty(new PropertyName("id")));
        assertNotNull(beanDeser.findProperty(0));
        assertNull(beanDeser.findProperty("nonExistent"));

        Collection<Object> knownProps = beanDeser.getKnownPropertyNames();
        assertEquals(2, knownProps.size());
        assertTrue(knownProps.contains("id"));
        assertTrue(knownProps.contains("name"));

        Iterator<SettableBeanProperty> it = beanDeser.properties();
        int count = 0;
        while (it.hasNext()) {
            assertNotNull(it.next());
            count++;
        }
        assertEquals(2, count);
    }

    // Tests creatorProperties iteration when creator is present
    @Test
    public void testCreatorProperties_withCreatorBean_returnsCreatorProps() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(CreatorBean.class);
        BeanDeserializerBase beanDeser = (BeanDeserializerBase) mapper.findRootValueDeserializer(type);

        Iterator<SettableBeanProperty> creatorIt = beanDeser.creatorProperties();
        List<String> creatorNames = new ArrayList<String>();
        while (creatorIt.hasNext()) {
            creatorNames.add(creatorIt.next().getName());
        }
        assertEquals(2, creatorNames.size());
        assertTrue(creatorNames.contains("x"));
        assertTrue(creatorNames.contains("y"));

        assertNotNull(beanDeser.findProperty("x"));
        assertNotNull(beanDeser.findProperty("y"));
    }

    // Tests creatorProperties on bean without property-based creator
    @Test
    public void testCreatorProperties_withoutCreator_returnsEmptyIterator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDeserializerBase beanDeser = (BeanDeserializerBase) mapper.findRootValueDeserializer(type);

        Iterator<SettableBeanProperty> creatorIt = beanDeser.creatorProperties();
        assertFalse(creatorIt.hasNext());
    }

    // Tests findBackReference returns null when no back refs exist
    @Test
    public void testFindBackReference_noBackRefs_returnsNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDeserializerBase beanDeser = (BeanDeserializerBase) mapper.findRootValueDeserializer(type);

        assertNull(beanDeser.findBackReference("dummyRef"));
    }

    // Tests managed and back reference resolution
    @Test
    public void testManagedAndBackReference_validStructure_resolvesAndDeserializes() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":10,\"child\":{\"value\":\"abc\"}}";
        ParentRef parent = mapper.readValue(json, ParentRef.class);

        assertNotNull(parent);
        assertEquals(10, parent.id);
        assertNotNull(parent.child);
        assertEquals("abc", parent.child.value);
        assertSame(parent, parent.child.parent);
    }

    // Tests Shape.ARRAY deserialization
    @Test
    public void testShapeArray_validInput_deserializesCorrectly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "[42, \"foo\"]";
        ArrayShapeBean bean = mapper.readValue(json, ArrayShapeBean.class);

        assertNotNull(bean);
        assertEquals(42, bean.a);
        assertEquals("foo", bean.b);
    }

    // Tests unwrapped property deserialization
    @Test
    public void testUnwrappedProperties_validInput_deserializesFields() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"name\":\"John\",\"age\":30,\"city\":\"NYC\"}";
        UnwrappedWrapper result = mapper.readValue(json, UnwrappedWrapper.class);

        assertNotNull(result);
        assertEquals("John", result.name);
        assertNotNull(result.child);
        assertEquals(30, result.child.age);
        assertEquals("NYC", result.child.city);
    }

    // Tests unknown property with default config throws exception
    @Test(expected = UnrecognizedPropertyException.class)
    public void testHandleUnknownProperty_defaultConfig_throwsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":1,\"unknownProp\":\"value\"}";
        mapper.readValue(json, SimpleBean.class);
    }

    // Tests unknown property ignored when FAIL_ON_UNKNOWN_PROPERTIES is false
    @Test
    public void testHandleUnknownProperty_featureDisabled_skipsUnknown() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        String json = "{\"id\":1,\"unknownProp\":\"value\",\"name\":\"test\"}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);

        assertNotNull(bean);
        assertEquals(1, bean.id);
        assertEquals("test", bean.name);
    }

    // Tests ignored property with FAIL_ON_IGNORED_PROPERTIES enabled
    @Test(expected = IgnoredPropertyException.class)
    public void testHandleIgnoredProperty_failOnIgnoredEnabled_throwsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        String json = "{\"a\":1,\"ignoredField\":\"skip\"}";
        mapper.readValue(json, IgnoredPropBean.class);
    }

    // Tests ignored property skipped with default config
    @Test
    public void testHandleIgnoredProperty_defaultConfig_skipsProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"a\":5,\"ignoredField\":\"skip\"}";
        IgnoredPropBean bean = mapper.readValue(json, IgnoredPropBean.class);

        assertNotNull(bean);
        assertEquals(5, bean.a);
        assertNull(bean.ignoredField);
    }

    // Tests UNWRAP_SINGLE_VALUE_ARRAYS feature on BeanDeserializerBase
    @Test
    public void testDeserializeFromArray_unwrapSingleValueArrayEnabled_returnsBean() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        String json = "[{\"id\":7,\"name\":\"unwrapped\"}]";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);

        assertNotNull(bean);
        assertEquals(7, bean.id);
        assertEquals("unwrapped", bean.name);
    }

    // Tests ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT feature on BeanDeserializerBase
    @Test
    public void testDeserializeFromArray_emptyArrayAsNull_returnsNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        String json = "[]";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);

        assertNull(bean);
    }

    // Tests withBeanProperties default implementation throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testWithBeanProperties_defaultMethod_throwsUnsupportedOperationException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDeserializerBase beanDeser = (BeanDeserializerBase) mapper.findRootValueDeserializer(type);

        CustomDeserializerBase custom = new CustomDeserializerBase(beanDeser);
        custom.withBeanProperties(BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false));
    }

    // Tests wrapAndThrow unwraps InvocationTargetException and wraps into JsonMappingException
    @Test
    public void testWrapAndThrow_invocationTargetException_wrapsCorrectly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDeserializerBase beanDeser = (BeanDeserializerBase) mapper.findRootValueDeserializer(type);

        IllegalStateException cause = new IllegalStateException("Inner failure");
        InvocationTargetException ite = new InvocationTargetException(cause);

        try {
            beanDeser.wrapAndThrow(ite, new SimpleBean(), "testField", mapper.getDeserializationContext());
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertSame(cause, e.getCause());
            assertTrue(e.getMessage().contains("Inner failure"));
            assertTrue(e.getPathReference().contains("testField"));
        }
    }

    // Tests wrapAndThrow with an Error rethrows the error directly
    @Test(expected = OutOfMemoryError.class)
    public void testWrapAndThrow_errorThrown_rethrowsErrorDirectly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDeserializerBase beanDeser = (BeanDeserializerBase) mapper.findRootValueDeserializer(type);

        beanDeser.wrapAndThrow(new OutOfMemoryError("OOM test"), new SimpleBean(), "field", mapper.getDeserializationContext());
    }

    // Tests replaceProperty functionality on BeanDeserializerBase
    @Test
    public void testReplaceProperty_validReplacement_updatesPropertyMap() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDeserializerBase beanDeser = (BeanDeserializerBase) mapper.findRootValueDeserializer(type);

        SettableBeanProperty origProp = beanDeser.findProperty("id");
        assertNotNull(origProp);

        SettableBeanProperty renamedProp = origProp.withSimpleName("id");
        beanDeser.replaceProperty(origProp, renamedProp);

        SettableBeanProperty currentProp = beanDeser.findProperty("id");
        assertSame(renamedProp, currentProp);
    }
}