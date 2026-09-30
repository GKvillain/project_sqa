package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class PropertyBuilderTest {

    private ObjectMapper _mapper;

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
    }

    // Helper classes for testing serialization and inclusion behaviors
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    static class NonDefaultBean {
        public int primitiveInt = 0;
        public int modifiedInt = 5;
        public String defaultString = "default";
        public String modifiedString = "other";
        public List<String> defaultList = new ArrayList<String>();
        public int[] defaultArray = new int[]{1, 2};
        public int[] modifiedArray = new int[]{1, 2};

        public NonDefaultBean() {
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    static class NonEmptyBean {
        public String emptyStr = "";
        public String nonEmptyStr = "abc";
        public List<String> emptyList = Collections.emptyList();
        public List<String> nonEmptyList = Collections.singletonList("item");
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    static class NonAbsentBean {
        public AtomicReference<String> nullRef = null;
        public AtomicReference<String> emptyRef = new AtomicReference<String>(null);
        public AtomicReference<String> fullRef = new AtomicReference<String>("content");
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class NonNullBean {
        public String nullValue = null;
        public String nonNullValue = "present";
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    static class AlwaysBean {
        public String nullValue = null;
        public List<String> emptyList = new ArrayList<String>();
    }

    static class NoDefaultConstructorBean {
        public String text;

        public NoDefaultConstructorBean(String text) {
            this.text = text;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    static class PropertySpecificOverrideBean {
        @JsonInclude(JsonInclude.Include.ALWAYS)
        public int intAlways = 0;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public String strNonEmpty = "";

        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        public AtomicReference<String> refNonAbsent = new AtomicReference<String>(null);
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    static class ExceptionGetterBean {
        public String getFailingProp() {
            throw new IllegalStateException("Failed to calculate getter");
        }
    }

    @JsonPropertyOrder({"primitiveInt", "modifiedInt"})
    static class DefaultValueDirectTestClass {
        public int primitiveInt;
        public Integer wrapperInt;
        public String stringValue;
        public List<String> listValue;
        public AtomicReference<String> refValue;
    }

    // Tests PropertyBuilder initialization and getClassAnnotations
    @Test
    public void testGetClassAnnotations_validBeanDesc_returnsAnnotations() {
        SerializationConfig config = _mapper.getSerializationConfig();
        JavaType javaType = _mapper.constructType(NonDefaultBean.class);
        BeanDescription beanDesc = config.introspect(javaType);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);

        assertNotNull(builder.getClassAnnotations());
    }

    // Tests getDefaultValue method on primitive, wrapper, string, container, and reference types
    @Test
    public void testGetDefaultValue_differentTypes_returnsExpectedDefaults() {
        SerializationConfig config = _mapper.getSerializationConfig();
        JavaType javaType = _mapper.constructType(DefaultValueDirectTestClass.class);
        BeanDescription beanDesc = config.introspect(javaType);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);

        JavaType intType = _mapper.constructType(int.class);
        JavaType wrapperIntType = _mapper.constructType(Integer.class);
        JavaType strType = _mapper.constructType(String.class);
        JavaType listType = _mapper.constructType(List.class);
        JavaType refType = _mapper.constructType(AtomicReference.class);
        JavaType objType = _mapper.constructType(Object.class);

        assertEquals(Integer.valueOf(0), builder.getDefaultValue(intType));
        assertEquals(Integer.valueOf(0), builder.getDefaultValue(wrapperIntType));
        assertEquals("", builder.getDefaultValue(strType));
        assertEquals(JsonInclude.Include.NON_EMPTY, builder.getDefaultValue(listType));
        assertEquals(JsonInclude.Include.NON_EMPTY, builder.getDefaultValue(refType));
        assertNull(builder.getDefaultValue(objType));
    }

    // Tests getDefaultBean when bean has default constructor
    @Test
    public void testGetDefaultBean_classWithDefaultConstructor_returnsInstance() {
        SerializationConfig config = _mapper.getSerializationConfig();
        JavaType javaType = _mapper.constructType(NonDefaultBean.class);
        BeanDescription beanDesc = config.introspect(javaType);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);

        Object defaultBean = builder.getDefaultBean();
        assertNotNull(defaultBean);
        assertTrue(defaultBean instanceof NonDefaultBean);
        // Repeated call should cache and return same instance
        assertSame(defaultBean, builder.getDefaultBean());
    }

    // Tests getDefaultBean when bean lacks default constructor
    @Test
    public void testGetDefaultBean_classWithoutDefaultConstructor_returnsNull() {
        SerializationConfig config = _mapper.getSerializationConfig();
        JavaType javaType = _mapper.constructType(NoDefaultConstructorBean.class);
        BeanDescription beanDesc = config.introspect(javaType);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);

        Object defaultBean = builder.getDefaultBean();
        assertNull(defaultBean);
        // Repeated call should continue to return null from marker
        assertNull(builder.getDefaultBean());
    }

    // Tests NON_DEFAULT serialization filtering behavior on properties matching default values
    @Test
    public void testBuildWriter_nonDefaultInclusion_filtersDefaultProperties() throws Exception {
        NonDefaultBean bean = new NonDefaultBean();
        String json = _mapper.writeValueAsString(bean);

        assertFalse(json.contains("primitiveInt"));
        assertFalse(json.contains("defaultString"));
        assertFalse(json.contains("defaultList"));
        assertFalse(json.contains("defaultArray"));
    }

    // Tests NON_DEFAULT serialization retaining properties modified from default values
    @Test
    public void testBuildWriter_nonDefaultInclusion_serializesModifiedProperties() throws Exception {
        NonDefaultBean bean = new NonDefaultBean();
        bean.modifiedInt = 42;
        bean.modifiedString = "changed";
        bean.modifiedArray = new int[]{3, 4};
        String json = _mapper.writeValueAsString(bean);

        assertTrue(json.contains("\"modifiedInt\":42"));
        assertTrue(json.contains("\"modifiedString\":\"changed\""));
        assertTrue(json.contains("\"modifiedArray\":[3,4]"));
    }

    // Tests NON_EMPTY inclusion branch
    @Test
    public void testBuildWriter_nonEmptyInclusion_suppressesEmptyValues() throws Exception {
        NonEmptyBean bean = new NonEmptyBean();
        String json = _mapper.writeValueAsString(bean);

        assertFalse(json.contains("emptyStr"));
        assertFalse(json.contains("emptyList"));
        assertTrue(json.contains("\"nonEmptyStr\":\"abc\""));
        assertTrue(json.contains("\"nonEmptyList\":[\"item\"]"));
    }

    // Tests NON_ABSENT inclusion branch with reference types
    @Test
    public void testBuildWriter_nonAbsentInclusion_suppressesAbsentAndEmptyReference() throws Exception {
        NonAbsentBean bean = new NonAbsentBean();
        String json = _mapper.writeValueAsString(bean);

        assertFalse(json.contains("nullRef"));
        assertFalse(json.contains("emptyRef"));
        assertTrue(json.contains("\"fullRef\":\"content\""));
    }

    // Tests NON_NULL inclusion branch
    @Test
    public void testBuildWriter_nonNullInclusion_suppressesNullProperties() throws Exception {
        NonNullBean bean = new NonNullBean();
        String json = _mapper.writeValueAsString(bean);

        assertFalse(json.contains("nullValue"));
        assertTrue(json.contains("\"nonNullValue\":\"present\""));
    }

    // Tests ALWAYS inclusion branch and WRITE_EMPTY_JSON_ARRAYS feature
    @Test
    public void testBuildWriter_alwaysInclusion_writesNullsAndEmptyArrays() throws Exception {
        AlwaysBean bean = new AlwaysBean();
        String json = _mapper.writeValueAsString(bean);

        assertTrue(json.contains("\"nullValue\":null"));
        assertTrue(json.contains("\"emptyList\":[]"));
    }

    // Tests ALWAYS inclusion branch when WRITE_EMPTY_JSON_ARRAYS is disabled
    @Test
    public void testBuildWriter_disabledEmptyJsonArrays_suppressesEmptyArrays() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
        AlwaysBean bean = new AlwaysBean();
        String json = mapper.writeValueAsString(bean);

        assertTrue(json.contains("\"nullValue\":null"));
        assertFalse(json.contains("emptyList"));
    }

    // Tests per-property inclusion overrides over class-level NON_DEFAULT
    @Test
    public void testBuildWriter_propertyOverrideInclusion_respectsOverrides() throws Exception {
        PropertySpecificOverrideBean bean = new PropertySpecificOverrideBean();
        String json = _mapper.writeValueAsString(bean);

        assertTrue(json.contains("\"intAlways\":0"));
        assertFalse(json.contains("strNonEmpty"));
        assertFalse(json.contains("refNonAbsent"));
    }

    // Tests _throwWrapped exception wrapping
    @Test(expected = RuntimeException.class)
    public void testThrowWrapped_runtimeException_rethrowsDirectly() {
        SerializationConfig config = _mapper.getSerializationConfig();
        JavaType javaType = _mapper.constructType(NonDefaultBean.class);
        BeanDescription beanDesc = config.introspect(javaType);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);

        builder._throwWrapped(new IllegalStateException("Direct error"), "prop", new Object());
    }

    // Tests _throwWrapped exception wrapping for checked exceptions
    @Test(expected = IllegalArgumentException.class)
    public void testThrowWrapped_checkedException_throwsIllegalArgumentException() {
        SerializationConfig config = _mapper.getSerializationConfig();
        JavaType javaType = _mapper.constructType(NonDefaultBean.class);
        BeanDescription beanDesc = config.introspect(javaType);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);

        builder._throwWrapped(new Exception("Checked exception"), "prop", new Object());
    }
}