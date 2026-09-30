package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

public class MethodPropertyTest {

    @Retention(RetentionPolicy.RUNTIME)
    @interface CustomAnno {
        String value() default "";
    }

    static class TestBean {
        private String text;
        private String returnText;

        @CustomAnno("custom")
        public void setText(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public TestBean setReturnText(String returnText) {
            this.returnText = returnText;
            return this;
        }

        public String getReturnText() {
            return returnText;
        }

        public void setFail(String val) {
            throw new IllegalArgumentException("Simulated setter error: " + val);
        }

        public String getFail() {
            return "";
        }
    }

    private ObjectMapper _mapper;
    private DeserializationContext _context;

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _context = _mapper.getDeserializationContext();
    }

    private MethodProperty _createMethodProperty(String propName, Class<?> beanClass) throws Exception {
        JavaType beanType = _mapper.constructType(beanClass);
        BeanDescription beanDesc = _mapper.getDeserializationConfig().introspect(beanType);
        BeanPropertyDefinition targetDef = null;
        for (BeanPropertyDefinition prop : beanDesc.findProperties()) {
            if (propName.equals(prop.getName())) {
                targetDef = prop;
                break;
            }
        }
        assertNotNull("Property definition not found for: " + propName, targetDef);
        AnnotatedMethod setter = targetDef.getSetter();
        JavaType type = setter.getParameterType(0);
        MethodProperty prop = new MethodProperty(targetDef, type, null, null, setter);
        JsonDeserializer<Object> deser = _context.findRootValueDeserializer(type);
        return (MethodProperty) prop.withValueDeserializer(deser);
    }

    // Tests constructor initialization and basic getter members
    @Test
    public void testConstructorAndGetters_validInputs_returnsCorrectMembers() throws Exception {
        MethodProperty prop = _createMethodProperty("text", TestBean.class);
        assertNotNull(prop.getMember());
        assertEquals("setText", prop.getMember().getName());
        CustomAnno anno = prop.getAnnotation(CustomAnno.class);
        assertNotNull(anno);
        assertEquals("custom", anno.value());
    }

    // Tests withName method creating a copy with a new PropertyName
    @Test
    public void testWithName_differentName_returnsUpdatedProperty() throws Exception {
        MethodProperty prop = _createMethodProperty("text", TestBean.class);
        PropertyName newName = new PropertyName("newText");
        SettableBeanProperty renamed = prop.withName(newName);

        assertNotSame(prop, renamed);
        assertEquals("newText", renamed.getName());
    }

    // Tests withValueDeserializer identity check and replacement
    @Test
    public void testWithValueDeserializer_sameAndDifferentDeser_handlesCorrectly() throws Exception {
        MethodProperty prop = _createMethodProperty("text", TestBean.class);
        JsonDeserializer<?> currentDeser = prop.getValueDeserializer();

        // Same deserializer should return this
        SettableBeanProperty same = prop.withValueDeserializer(currentDeser);
        assertSame(prop, same);

        // Different deserializer returns new instance
        JsonDeserializer<?> dummyDeser = _context.findRootValueDeserializer(_mapper.constructType(Integer.class));
        SettableBeanProperty modified = prop.withValueDeserializer(dummyDeser);
        assertNotSame(prop, modified);
        assertSame(dummyDeser, modified.getValueDeserializer());
    }

    // Tests withNullProvider creating copy with given NullValueProvider
    @Test
    public void testWithNullProvider_customProvider_returnsUpdatedProperty() throws Exception {
        MethodProperty prop = _createMethodProperty("text", TestBean.class);
        NullValueProvider nva = NullsConstantProvider.skipper();
        SettableBeanProperty modified = prop.withNullProvider(nva);

        assertNotSame(prop, modified);
        assertSame(nva, modified.getNullValueProvider());
    }

    // Tests fixAccess without errors
    @Test
    public void testFixAccess_validConfig_executesSuccessfully() throws Exception {
        MethodProperty prop = _createMethodProperty("text", TestBean.class);
        prop.fixAccess(_mapper.getDeserializationConfig());
        assertNotNull(prop.getMember());
    }

    // Tests direct set method
    @Test
    public void testSet_validValue_setsPropertyOnInstance() throws Exception {
        MethodProperty prop = _createMethodProperty("text", TestBean.class);
        TestBean bean = new TestBean();
        prop.set(bean, "hello world");
        assertEquals("hello world", bean.getText());
    }

    // Tests direct set method when setter throws exception
    @Test(expected = JsonMappingException.class)
    public void testSet_throwingSetter_throwsJsonMappingException() throws Exception {
        MethodProperty prop = _createMethodProperty("fail", TestBean.class);
        TestBean bean = new TestBean();
        prop.set(bean, "failure test");
    }

    // Tests setAndReturn with void return type (returns input instance)
    @Test
    public void testSetAndReturn_voidReturnType_returnsInstance() throws Exception {
        MethodProperty prop = _createMethodProperty("text", TestBean.class);
        TestBean bean = new TestBean();
        Object result = prop.setAndReturn(bean, "value123");
        assertSame(bean, result);
        assertEquals("value123", bean.getText());
    }

    // Tests setAndReturn with non-void return type (returns setter result)
    @Test
    public void testSetAndReturn_nonVoidReturnType_returnsResult() throws Exception {
        MethodProperty prop = _createMethodProperty("returnText", TestBean.class);
        TestBean bean = new TestBean();
        Object result = prop.setAndReturn(bean, "customReturn");
        assertSame(bean, result);
        assertEquals("customReturn", bean.getReturnText());
    }

    // Tests setAndReturn exception handling
    @Test(expected = JsonMappingException.class)
    public void testSetAndReturn_throwingSetter_throwsJsonMappingException() throws Exception {
        MethodProperty prop = _createMethodProperty("fail", TestBean.class);
        TestBean bean = new TestBean();
        prop.setAndReturn(bean, "failure test");
    }

    // Tests deserializeAndSet with normal token
    @Test
    public void testDeserializeAndSet_normalValue_setsDeserializedValue() throws Exception {
        MethodProperty prop = _createMethodProperty("text", TestBean.class);
        TestBean bean = new TestBean();
        JsonParser p = _mapper.createParser("\"deserializedValue\"");
        p.nextToken();

        prop.deserializeAndSet(p, _context, bean);
        p.close();

        assertEquals("deserializedValue", bean.getText());
    }

    // Tests deserializeAndSet with VALUE_NULL token
    @Test
    public void testDeserializeAndSet_nullToken_setsNullValue() throws Exception {
        MethodProperty prop = _createMethodProperty("text", TestBean.class);
        TestBean bean = new TestBean();
        bean.setText("initial");
        JsonParser p = _mapper.createParser("null");
        p.nextToken();

        prop.deserializeAndSet(p, _context, bean);
        p.close();

        assertNull(bean.getText());
    }

    // Tests deserializeAndSet with skipNulls enabled
    @Test
    public void testDeserializeAndSet_skipNulls_doesNotModifyValue() throws Exception {
        MethodProperty prop = _createMethodProperty("text", TestBean.class);
        prop = (MethodProperty) prop.withNullProvider(NullsConstantProvider.skipper());
        TestBean bean = new TestBean();
        bean.setText("keepThis");
        JsonParser p = _mapper.createParser("null");
        p.nextToken();

        prop.deserializeAndSet(p, _context, bean);
        p.close();

        assertEquals("keepThis", bean.getText());
    }

    // Tests deserializeSetAndReturn with normal value
    @Test
    public void testDeserializeSetAndReturn_normalValue_returnsInstance() throws Exception {
        MethodProperty prop = _createMethodProperty("returnText", TestBean.class);
        TestBean bean = new TestBean();
        JsonParser p = _mapper.createParser("\"returnValue\"");
        p.nextToken();

        Object result = prop.deserializeSetAndReturn(p, _context, bean);
        p.close();

        assertSame(bean, result);
        assertEquals("returnValue", bean.getReturnText());
    }

    // Tests deserializeSetAndReturn with null value and skipNulls
    @Test
    public void testDeserializeSetAndReturn_nullValueSkipped_returnsInstanceWithoutSetting() throws Exception {
        MethodProperty prop = _createMethodProperty("returnText", TestBean.class);
        prop = (MethodProperty) prop.withNullProvider(NullsConstantProvider.skipper());
        TestBean bean = new TestBean();
        bean.setReturnText("preserve");
        JsonParser p = _mapper.createParser("null");
        p.nextToken();

        Object result = prop.deserializeSetAndReturn(p, _context, bean);
        p.close();

        assertSame(bean, result);
        assertEquals("preserve", bean.getReturnText());
    }

    // Tests readResolve for JDK serialization support
    @Test
    public void testReadResolve_validProperty_reconstructsMethodProperty() throws Exception {
        MethodProperty prop = _createMethodProperty("text", TestBean.class);
        Object resolved = prop.readResolve();

        assertNotNull(resolved);
        assertTrue(resolved instanceof MethodProperty);
        MethodProperty resolvedProp = (MethodProperty) resolved;
        assertEquals(prop.getName(), resolvedProp.getName());
    }
}