package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class PropertyBuilderTest {

    private ObjectMapper _mapper;

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
    }

    // Helper classes for testing various scenarios

    static class SimpleBean {
        public String strVal = "default";
        public int intVal = 42;
        public Integer integerObj = Integer.valueOf(0);
        public boolean boolVal = true;
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    static class NonDefaultBean {
        public String strVal = "abc";
        public int intVal = 10;
        public int[] arrayVal = new int[] { 1, 2 };
        public List<String> listVal = new ArrayList<String>();
    }

    static class NoDefaultConstructorBean {
        public String value;

        public NoDefaultConstructorBean(String v) {
            this.value = v;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class NonNullBean {
        public String name = null;
        public String nonNull = "test";
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    static class NonEmptyBean {
        public String emptyStr = "";
        public List<String> emptyList = Collections.emptyList();
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    static class NonAbsentBean {
        public String str = null;
    }

    static class ContainerBean {
        public List<String> items = new ArrayList<String>();
    }

    static class ArrayComparatorBean {
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public int[] numbers = new int[] { 1, 2, 3 };
    }

    static class PropertyOverrideBean {
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public int count = 0;

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public String text = "";

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public List<String> list = Collections.emptyList();
    }

    @JsonPropertyOrder({ "id", "name" })
    static class BaseTypeBean {
        public Object id = 1;
        public CharSequence name = "test";
    }

    // Helper method to create PropertyBuilder
    private PropertyBuilder createPropertyBuilder(Class<?> cls) {
        SerializationConfig config = _mapper.getSerializationConfig();
        JavaType type = _mapper.getTypeFactory().constructType(cls);
        BeanDescription beanDesc = config.introspect(type);
        return new PropertyBuilder(config, beanDesc);
    }

    // Tests getClassAnnotations returns non-null annotations for bean
    @Test
    public void testGetClassAnnotations_validClass_returnsAnnotations() {
        PropertyBuilder builder = createPropertyBuilder(SimpleBean.class);
        assertNotNull(builder.getClassAnnotations());
    }

    // Tests getDefaultBean returns an instantiated default instance
    @Test
    public void testGetDefaultBean_classWithDefaultConstructor_instantiatesBean() {
        PropertyBuilder builder = createPropertyBuilder(SimpleBean.class);
        Object defaultBean = builder.getDefaultBean();
        assertNotNull(defaultBean);
        assertTrue(defaultBean instanceof SimpleBean);
    }

    // Tests getDefaultBean handles classes without default constructor gracefully
    @Test
    public void testGetDefaultBean_noDefaultConstructor_returnsNull() {
        PropertyBuilder builder = createPropertyBuilder(NoDefaultConstructorBean.class);
        Object defaultBean = builder.getDefaultBean();
        assertNull(defaultBean);
    }

    // Tests getDefaultValue for primitive types returns standard primitive defaults
    @Test
    public void testGetDefaultValue_primitiveTypes_returnsPrimitiveDefaults() {
        PropertyBuilder builder = createPropertyBuilder(SimpleBean.class);
        TypeFactory tf = _mapper.getTypeFactory();

        Object intDef = builder.getDefaultValue(tf.constructType(int.class));
        assertEquals(Integer.valueOf(0), intDef);

        Object boolDef = builder.getDefaultValue(tf.constructType(boolean.class));
        assertEquals(Boolean.FALSE, boolDef);

        Object doubleDef = builder.getDefaultValue(tf.constructType(double.class));
        assertEquals(Double.valueOf(0.0), doubleDef);
    }

    // Tests getDefaultValue for primitive wrapper types returns wrapper defaults
    @Test
    public void testGetDefaultValue_wrapperTypes_returnsWrapperDefaults() {
        PropertyBuilder builder = createPropertyBuilder(SimpleBean.class);
        TypeFactory tf = _mapper.getTypeFactory();

        Object integerDef = builder.getDefaultValue(tf.constructType(Integer.class));
        assertEquals(Integer.valueOf(0), integerDef);

        Object booleanDef = builder.getDefaultValue(tf.constructType(Boolean.class));
        assertEquals(Boolean.FALSE, booleanDef);
    }

    // Tests getDefaultValue for String type returns empty string
    @Test
    public void testGetDefaultValue_stringType_returnsEmptyString() {
        PropertyBuilder builder = createPropertyBuilder(SimpleBean.class);
        JavaType stringType = _mapper.getTypeFactory().constructType(String.class);
        Object defaultVal = builder.getDefaultValue(stringType);
        assertEquals("", defaultVal);
    }

    // Tests getDefaultValue for Container types returns Include.NON_EMPTY marker
    @Test
    public void testGetDefaultValue_containerType_returnsIncludeNonEmpty() {
        PropertyBuilder builder = createPropertyBuilder(SimpleBean.class);
        JavaType listType = _mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        Object defaultVal = builder.getDefaultValue(listType);
        assertEquals(JsonInclude.Include.NON_EMPTY, defaultVal);
    }

    // Tests getDefaultValue for generic Object types returns null
    @Test
    public void testGetDefaultValue_objectType_returnsNull() {
        PropertyBuilder builder = createPropertyBuilder(SimpleBean.class);
        JavaType objType = _mapper.getTypeFactory().constructType(Object.class);
        Object defaultVal = builder.getDefaultValue(objType);
        assertNull(defaultVal);
    }

    // Tests serialization behavior when class has NON_DEFAULT inclusion
    @Test
    public void testSerialization_classWithNonDefault_suppressesDefaultValues() throws Exception {
        NonDefaultBean bean = new NonDefaultBean();
        String json = _mapper.writeValueAsString(bean);
        assertEquals("{}", json);

        bean.strVal = "modified";
        json = _mapper.writeValueAsString(bean);
        assertEquals("{\"strVal\":\"modified\"}", json);
    }

    // Tests serialization behavior when properties have NON_DEFAULT inclusion override
    @Test
    public void testSerialization_propertyWithNonDefault_suppressesStaticDefaults() throws Exception {
        PropertyOverrideBean bean = new PropertyOverrideBean();
        bean.count = 0;
        bean.text = "";
        bean.list = Collections.emptyList();

        String json = _mapper.writeValueAsString(bean);
        assertEquals("{}", json);

        bean.count = 5;
        json = _mapper.writeValueAsString(bean);
        assertEquals("{\"count\":5}", json);
    }

    // Tests serialization behavior when class has NON_NULL inclusion
    @Test
    public void testSerialization_classWithNonNull_suppressesNullValues() throws Exception {
        NonNullBean bean = new NonNullBean();
        String json = _mapper.writeValueAsString(bean);
        assertEquals("{\"nonNull\":\"test\"}", json);
    }

    // Tests serialization behavior when class has NON_EMPTY inclusion
    @Test
    public void testSerialization_classWithNonEmpty_suppressesEmptyValues() throws Exception {
        NonEmptyBean bean = new NonEmptyBean();
        String json = _mapper.writeValueAsString(bean);
        assertEquals("{}", json);
    }

    // Tests serialization behavior when WRITE_EMPTY_JSON_ARRAYS feature is disabled
    @Test
    public void testSerialization_disableWriteEmptyJsonArrays_suppressesEmptyContainers() throws Exception {
        _mapper.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
        ContainerBean bean = new ContainerBean();
        String json = _mapper.writeValueAsString(bean);
        assertEquals("{}", json);
    }

    // Tests serialization of array with NON_DEFAULT inclusion using array comparator
    @Test
    public void testSerialization_arrayNonDefault_usesArrayComparator() throws Exception {
        ArrayComparatorBean bean = new ArrayComparatorBean();
        String json = _mapper.writeValueAsString(bean);
        // Default array is suppressed
        assertEquals("{}", json);

        bean.numbers = new int[] { 1, 2, 4 };
        json = _mapper.writeValueAsString(bean);
        assertEquals("{\"numbers\":[1,2,4]}", json);
    }

    // Tests _throwWrapped correctly unwraps cause and throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testThrowWrapped_checkedException_throwsIllegalArgumentException() {
        PropertyBuilder builder = createPropertyBuilder(SimpleBean.class);
        Exception cause = new Exception("Root cause");
        builder._throwWrapped(cause, "testProp", new SimpleBean());
    }

    // Tests _throwWrapped rethrows RuntimeException directly
    @Test(expected = IllegalStateException.class)
    public void testThrowWrapped_runtimeException_rethrowsOriginalException() {
        PropertyBuilder builder = createPropertyBuilder(SimpleBean.class);
        RuntimeException runtimeException = new IllegalStateException("Test runtime exception");
        builder._throwWrapped(runtimeException, "testProp", new SimpleBean());
    }
}