package com.fasterxml.jackson.databind.ser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class PropertyBuilderTest {

    // Test helper classes for serialization scenarios

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    static class NonDefaultBean {
        public String str = "default";
        public int num = 10;
        public List<String> list = Collections.emptyList();
        public int[] array = new int[] { 1, 2 };

        public NonDefaultBean() {}
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    static class NonEmptyBean {
        public String emptyStr = "";
        public String nonEmptyStr = "abc";
        public List<String> emptyList = new ArrayList<String>();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class NonNullBean {
        public String nullStr = null;
        public String nonNullStr = "value";
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    static class NonAbsentBean {
        public String nullVal = null;
        public String nonNullVal = "present";
    }

    static class PropertyOverrideBean {
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public String str = "default";

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public int num = 0;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public String emptyStr = "";

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String nullStr = null;

        @JsonInclude(JsonInclude.Include.ALWAYS)
        public String alwaysNull = null;
    }

    static class StaticTypingBean {
        @JsonSerialize(typing = JsonSerialize.Typing.STATIC)
        public CharSequence seq = "staticText";
    }

    static class NoDefaultConstructorBean {
        public String value;

        public NoDefaultConstructorBean(String v) {
            this.value = v;
        }
    }

    static class DefaultValuesBean {
        public boolean boolVal = false;
        public byte byteVal = 0;
        public short shortVal = 0;
        public char charVal = '\0';
        public int intVal = 0;
        public long longVal = 0L;
        public float floatVal = 0.0f;
        public double doubleVal = 0.0d;
        public String strVal = "";
        public List<String> listVal = null;
    }

    static class SubPropertyBuilder extends PropertyBuilder {
        public SubPropertyBuilder(SerializationConfig config, BeanDescription beanDesc) {
            super(config, beanDesc);
        }

        public Object callGetDefaultBean() {
            return getDefaultBean();
        }

        public Object callGetDefaultValue(JavaType type) {
            return getDefaultValue(type);
        }

        public Object callGetPropertyDefaultValue(String name, AnnotatedMember member, JavaType type) {
            return getPropertyDefaultValue(name, member, type);
        }

        public JavaType callFindSerializationType(AnnotatedMember a, boolean useStaticTyping, JavaType declaredType)
            throws JsonMappingException
        {
            return findSerializationType(a, useStaticTyping, declaredType);
        }

        public Object callThrowWrapped(Exception e, String propName, Object defaultBean) {
            return _throwWrapped(e, propName, defaultBean);
        }
    }

    // Tests construction and metadata retrieval
    @Test
    public void testGetClassAnnotations_validBean_returnsNonNull() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        JavaType type = mapper.constructType(NonDefaultBean.class);
        BeanDescription beanDesc = config.introspect(type);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);

        assertNotNull(builder.getClassAnnotations());
    }

    // Tests serialization with class-level NON_DEFAULT inclusion
    @Test
    public void testBuildWriter_classLevelNonDefault_suppressesDefaultValues() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        NonDefaultBean bean = new NonDefaultBean();

        String json = mapper.writeValueAsString(bean);
        assertEquals("{}", json);

        bean.str = "custom";
        bean.num = 20;
        json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"str\":\"custom\""));
        assertTrue(json.contains("\"num\":20"));
    }

    // Tests array comparison suppression in class-level NON_DEFAULT
    @Test
    public void testBuildWriter_classLevelNonDefaultArray_suppressesMatchingArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        NonDefaultBean bean = new NonDefaultBean();
        bean.str = "changed";
        bean.array = new int[] { 1, 2 }; // Matches default array content

        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"str\":\"changed\""));
        assertFalse(json.contains("\"array\""));

        bean.array = new int[] { 1, 3 }; // Modified array content
        json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"array\":[1,3]"));
    }

    // Tests property-level NON_DEFAULT inclusion override
    @Test
    public void testBuildWriter_propertyLevelNonDefault_suppressesDefaultPrimitiveAndString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PropertyOverrideBean bean = new PropertyOverrideBean();

        String json = mapper.writeValueAsString(bean);
        // Default String for property-level NON_DEFAULT is empty string "", so "default" is serialized
        assertTrue(json.contains("\"str\":\"default\""));
        // Default int is 0, so num=0 is suppressed
        assertFalse(json.contains("\"num\""));

        bean.num = 42;
        json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"num\":42"));
    }

    // Tests property-level NON_EMPTY inclusion override
    @Test
    public void testBuildWriter_propertyLevelNonEmpty_suppressesEmpty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PropertyOverrideBean bean = new PropertyOverrideBean();
        bean.emptyStr = "";

        String json = mapper.writeValueAsString(bean);
        assertFalse(json.contains("\"emptyStr\""));

        bean.emptyStr = "filled";
        json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"emptyStr\":\"filled\""));
    }

    // Tests property-level NON_NULL and ALWAYS inclusion
    @Test
    public void testBuildWriter_propertyLevelNonNullAndAlways_respectsInclusion() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PropertyOverrideBean bean = new PropertyOverrideBean();

        String json = mapper.writeValueAsString(bean);
        assertFalse(json.contains("\"nullStr\""));
        assertTrue(json.contains("\"alwaysNull\":null"));
    }

    // Tests class-level NON_EMPTY inclusion
    @Test
    public void testBuildWriter_classLevelNonEmpty_suppressesEmptyFields() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        NonEmptyBean bean = new NonEmptyBean();

        String json = mapper.writeValueAsString(bean);
        assertFalse(json.contains("\"emptyStr\""));
        assertFalse(json.contains("\"emptyList\""));
        assertTrue(json.contains("\"nonEmptyStr\":\"abc\""));
    }

    // Tests class-level NON_NULL inclusion
    @Test
    public void testBuildWriter_classLevelNonNull_suppressesNullFields() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        NonNullBean bean = new NonNullBean();

        String json = mapper.writeValueAsString(bean);
        assertFalse(json.contains("\"nullStr\""));
        assertTrue(json.contains("\"nonNullStr\":\"value\""));
    }

    // Tests class-level NON_ABSENT inclusion
    @Test
    public void testBuildWriter_classLevelNonAbsent_suppressesNulls() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        NonAbsentBean bean = new NonAbsentBean();

        String json = mapper.writeValueAsString(bean);
        assertFalse(json.contains("\"nullVal\""));
        assertTrue(json.contains("\"nonNullVal\":\"present\""));
    }

    // Tests static typing configuration on property
    @Test
    public void testBuildWriter_staticTyping_serializesCorrectly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        StaticTypingBean bean = new StaticTypingBean();

        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"seq\":\"staticText\""));
    }

    // Tests getDefaultBean when target class has no default constructor
    @Test
    public void testGetDefaultBean_noDefaultConstructor_returnsNull() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        JavaType type = mapper.constructType(NoDefaultConstructorBean.class);
        BeanDescription beanDesc = config.introspect(type);
        SubPropertyBuilder builder = new SubPropertyBuilder(config, beanDesc);

        Object defaultBean = builder.callGetDefaultBean();
        assertNull(defaultBean);
    }

    // Tests getDefaultValue helper method for primitives and reference/container types
    @Test
    public void testGetDefaultValue_variousTypes_returnsExpectedDefaults() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = config.introspect(mapper.constructType(DefaultValuesBean.class));
        SubPropertyBuilder builder = new SubPropertyBuilder(config, beanDesc);

        TypeFactory tf = mapper.getTypeFactory();

        assertEquals(false, builder.callGetDefaultValue(tf.constructType(boolean.class)));
        assertEquals((byte) 0, builder.callGetDefaultValue(tf.constructType(byte.class)));
        assertEquals((short) 0, builder.callGetDefaultValue(tf.constructType(short.class)));
        assertEquals('\0', builder.callGetDefaultValue(tf.constructType(char.class)));
        assertEquals(0, builder.callGetDefaultValue(tf.constructType(int.class)));
        assertEquals(0L, builder.callGetDefaultValue(tf.constructType(long.class)));
        assertEquals(0.0f, builder.callGetDefaultValue(tf.constructType(float.class)));
        assertEquals(0.0d, builder.callGetDefaultValue(tf.constructType(double.class)));
        assertEquals("", builder.callGetDefaultValue(tf.constructType(String.class)));
        assertEquals(JsonInclude.Include.NON_EMPTY, builder.callGetDefaultValue(tf.constructType(List.class)));
        assertNull(builder.callGetDefaultValue(tf.constructType(Object.class)));
    }

    // Tests getPropertyDefaultValue fallback when bean has no default constructor
    @Test
    public void testGetPropertyDefaultValue_noDefaultBean_fallsBackToTypeDefault() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = config.introspect(mapper.constructType(NoDefaultConstructorBean.class));
        SubPropertyBuilder builder = new SubPropertyBuilder(config, beanDesc);

        JavaType stringType = mapper.constructType(String.class);
        Object defVal = builder.callGetPropertyDefaultValue("value", null, stringType);
        assertEquals("", defVal);
    }

    // Tests exception wrapping in _throwWrapped
    @Test(expected = IllegalArgumentException.class)
    public void testThrowWrapped_checkedException_throwsIllegalArgumentException() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = config.introspect(mapper.constructType(DefaultValuesBean.class));
        SubPropertyBuilder builder = new SubPropertyBuilder(config, beanDesc);

        Exception checkedEx = new Exception("test checked exception");
        builder.callThrowWrapped(checkedEx, "testProp", new DefaultValuesBean());
    }

    // Tests runtime exception propagation in _throwWrapped
    @Test(expected = IllegalStateException.class)
    public void testThrowWrapped_runtimeException_rethrowsSameRuntimeException() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = config.introspect(mapper.constructType(DefaultValuesBean.class));
        SubPropertyBuilder builder = new SubPropertyBuilder(config, beanDesc);

        IllegalStateException runtimeEx = new IllegalStateException("test runtime exception");
        builder.callThrowWrapped(runtimeEx, "testProp", new DefaultValuesBean());
    }

    // Tests error propagation in _throwWrapped
    @Test(expected = AssertionError.class)
    public void testThrowWrapped_error_rethrowsError() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = config.introspect(mapper.constructType(DefaultValuesBean.class));
        SubPropertyBuilder builder = new SubPropertyBuilder(config, beanDesc);

        AssertionError error = new AssertionError("test assertion error");
        Exception wrapper = new Exception(error);
        builder.callThrowWrapped(wrapper, "testProp", new DefaultValuesBean());
    }
}