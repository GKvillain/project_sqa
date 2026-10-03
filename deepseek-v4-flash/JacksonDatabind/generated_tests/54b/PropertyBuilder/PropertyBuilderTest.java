package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class PropertyBuilderTest {

    private ObjectMapper mapper;
    private SerializationConfig config;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        config = mapper.getSerializationConfig();
    }

    private PropertyBuilder createBuilder(Class<?> clazz) {
        BeanDescription beanDesc = config.introspect(mapper.constructType(clazz));
        return new PropertyBuilder(config, beanDesc);
    }

    // Test fixture beans
    private static class DefaultBean {
        public int value = 42;
    }

    private static class NoDefaultConstructorBean {
        public int value;
        public NoDefaultConstructorBean(int v) { value = v; }
    }

    private static class StaticTypingBean {
        @JsonSerialize(typing = JsonSerialize.Typing.STATIC)
        public Object value;
    }

    private static class DynamicTypingBean {
        @JsonSerialize(typing = JsonSerialize.Typing.DYNAMIC)
        public Object value;
    }

    private static class RefinedValidBean {
        @JsonSerialize(as = Object.class)
        public String value;
    }

    private static class RefinedInvalidBean {
        @JsonSerialize(as = String.class)
        public Integer value;
    }

    private static class BadGetterBean {
        private int value;
        public BadGetterBean() {}
        public int getValue() { throw new RuntimeException("boom"); }
    }

    private static class NonEmptyContainerBean {
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public java.util.List<String> items = new java.util.ArrayList<String>();
    }

    private static class NonNullInclusionBean {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String name = "test";
    }

    // Tests for getDefaultValue()
    @Test
    public void testGetDefaultValue_primitiveInt_returnsZero() {
        PropertyBuilder builder = createBuilder(DefaultBean.class);
        assertEquals(Integer.valueOf(0), builder.getDefaultValue(mapper.constructType(int.class)));
    }

    @Test
    public void testGetDefaultValue_wrapperInteger_returnsZero() {
        PropertyBuilder builder = createBuilder(DefaultBean.class);
        assertEquals(Integer.valueOf(0), builder.getDefaultValue(mapper.constructType(Integer.class)));
    }

    @Test
    public void testGetDefaultValue_primitiveBoolean_returnsFalse() {
        PropertyBuilder builder = createBuilder(DefaultBean.class);
        assertEquals(Boolean.FALSE, builder.getDefaultValue(mapper.constructType(boolean.class)));
    }

    @Test
    public void testGetDefaultValue_string_returnsEmptyString() {
        PropertyBuilder builder = createBuilder(DefaultBean.class);
        assertEquals("", builder.getDefaultValue(mapper.constructType(String.class)));
    }

    @Test
    public void testGetDefaultValue_containerType_returnsNonEmpty() {
        PropertyBuilder builder = createBuilder(DefaultBean.class);
        assertEquals(JsonInclude.Include.NON_EMPTY, builder.getDefaultValue(mapper.constructType(java.util.List.class)));
    }

    @Test
    public void testGetDefaultValue_referenceType_returnsNonEmpty() {
        PropertyBuilder builder = createBuilder(DefaultBean.class);
        assertEquals(JsonInclude.Include.NON_EMPTY, builder.getDefaultValue(mapper.constructType(java.util.Optional.class)));
    }

    @Test
    public void testGetDefaultValue_nonContainer_returnsNull() {
        PropertyBuilder builder = createBuilder(DefaultBean.class);
        assertNull(builder.getDefaultValue(mapper.constructType(Object.class)));
    }

    @Test
    public void testGetDefaultValue_primitiveByte_returnsZero() {
        PropertyBuilder builder = createBuilder(DefaultBean.class);
        assertEquals(Byte.valueOf((byte)0), builder.getDefaultValue(mapper.constructType(byte.class)));
    }

    @Test
    public void testGetDefaultValue_primitiveShort_returnsZero() {
        PropertyBuilder builder = createBuilder(DefaultBean.class);
        assertEquals(Short.valueOf((short)0), builder.getDefaultValue(mapper.constructType(short.class)));
    }

    @Test
    public void testGetDefaultValue_primitiveLong_returnsZero() {
        PropertyBuilder builder = createBuilder(DefaultBean.class);
        assertEquals(Long.valueOf(0L), builder.getDefaultValue(mapper.constructType(long.class)));
    }

    @Test
    public void testGetDefaultValue_primitiveFloat_returnsZero() {
        PropertyBuilder builder = createBuilder(DefaultBean.class);
        assertEquals(Float.valueOf(0.0f), builder.getDefaultValue(mapper.constructType(float.class)));
    }

    @Test
    public void testGetDefaultValue_primitiveDouble_returnsZero() {
        PropertyBuilder builder = createBuilder(DefaultBean.class);
        assertEquals(Double.valueOf(0.0d), builder.getDefaultValue(mapper.constructType(double.class)));
    }

    @Test
    public void testGetDefaultValue_primitiveChar_returnsZeroChar() {
        PropertyBuilder builder = createBuilder(DefaultBean.class);
        assertEquals(Character.valueOf('\0'), builder.getDefaultValue(mapper.constructType(char.class)));
    }

    @Test
    public void testGetDefaultValue_wrapperByte_returnsZero() {
        PropertyBuilder builder = createBuilder(DefaultBean.class);
        assertEquals(Byte.valueOf((byte)0), builder.getDefaultValue(mapper.constructType(Byte.class)));
    }

    @Test
    public void testGetDefaultValue_wrapperShort_returnsZero() {
        PropertyBuilder builder = createBuilder(DefaultBean.class);
        assertEquals(Short.valueOf((short)0), builder.getDefaultValue(mapper.constructType(Short.class)));
    }

    @Test
    public void testGetDefaultValue_wrapperLong_returnsZero() {
        PropertyBuilder builder = createBuilder(DefaultBean.class);
        assertEquals(Long.valueOf(0L), builder.getDefaultValue(mapper.constructType(Long.class)));
    }

    @Test
    public void testGetDefaultValue_wrapperFloat_returnsZero() {
        PropertyBuilder builder = createBuilder(DefaultBean.class);
        assertEquals(Float.valueOf(0.0f), builder.getDefaultValue(mapper.constructType(Float.class)));
    }

    @Test
    public void testGetDefaultValue_wrapperDouble_returnsZero() {
        PropertyBuilder builder = createBuilder(DefaultBean.class);
        assertEquals(Double.valueOf(0.0d), builder.getDefaultValue(mapper.constructType(Double.class)));
    }

    @Test
    public void testGetDefaultValue_wrapperChar_returnsZeroChar() {
        PropertyBuilder builder = createBuilder(DefaultBean.class);
        assertEquals(Character.valueOf('\0'), builder.getDefaultValue(mapper.constructType(Character.class)));
    }

    // Tests for getDefaultBean()
    @Test
    public void testGetDefaultBean_defaultConstructor_returnsInstance() {
        PropertyBuilder builder = createBuilder(DefaultBean.class);
        Object def = builder.getDefaultBean();
        assertNotNull(def);
        assertTrue(def instanceof DefaultBean);
    }

    @Test
    public void testGetDefaultBean_noDefaultConstructor_returnsNull() {
        PropertyBuilder builder = createBuilder(NoDefaultConstructorBean.class);
        assertNull(builder.getDefaultBean());
    }

    // Tests for getPropertyDefaultValue()
    @Test
    public void testGetPropertyDefaultValue_defaultBeanExists_returnsMemberValue() {
        PropertyBuilder builder = createBuilder(DefaultBean.class);
        BeanDescription beanDesc = config.introspect(mapper.constructType(DefaultBean.class));
        BeanPropertyDefinition propDef = beanDesc.findProperties().get(0);
        AnnotatedMember am = propDef.getPrimaryMember();
        Object result = builder.getPropertyDefaultValue("value", am, mapper.constructType(int.class));
        assertEquals(Integer.valueOf(42), result);
    }

    @Test
    public void testGetPropertyDefaultValue_noDefaultBean_returnsDefaultValue() {
        PropertyBuilder builder = createBuilder(NoDefaultConstructorBean.class);
        BeanDescription beanDesc = config.introspect(mapper.constructType(NoDefaultConstructorBean.class));
        BeanPropertyDefinition propDef = beanDesc.findProperties().get(0);
        AnnotatedMember am = propDef.getPrimaryMember();
        Object result = builder.getPropertyDefaultValue("value", am, mapper.constructType(int.class));
        assertEquals(Integer.valueOf(0), result);
    }

    @Test(expected = RuntimeException.class)
    public void testGetPropertyDefaultValue_memberThrows_throwsRuntimeException() {
        PropertyBuilder builder = createBuilder(BadGetterBean.class);
        BeanDescription beanDesc = config.introspect(mapper.constructType(BadGetterBean.class));
        BeanPropertyDefinition propDef = beanDesc.findProperties().get(0);
        AnnotatedMember am = propDef.getPrimaryMember();
        builder.getPropertyDefaultValue("value", am, mapper.constructType(int.class));
    }

    // Tests for findSerializationType()
    @Test
    public void testFindSerializationType_noAnnotation_useStaticTypingFalse_returnsNull() {
        PropertyBuilder builder = createBuilder(DefaultBean.class);
        BeanDescription beanDesc = config.introspect(mapper.constructType(DefaultBean.class));
        BeanPropertyDefinition propDef = beanDesc.findProperties().get(0);
        AnnotatedMember am = propDef.getPrimaryMember();
        assertNull(builder.findSerializationType(am, false, mapper.constructType(int.class)));
    }

    @Test
    public void testFindSerializationType_noAnnotation_useStaticTypingTrue_returnsStaticTyping() {
        PropertyBuilder builder = createBuilder(DefaultBean.class);
        BeanDescription beanDesc = config.introspect(mapper.constructType(DefaultBean.class));
        BeanPropertyDefinition propDef = beanDesc.findProperties().get(0);
        AnnotatedMember am = propDef.getPrimaryMember();
        JavaType result = builder.findSerializationType(am, true, mapper.constructType(int.class));
        assertNotNull(result);
        assertTrue(result.isStatic());
        assertEquals(int.class, result.getRawClass());
    }

    @Test
    public void testFindSerializationType_staticTypingAnnotated_returnsStaticTyping() {
        PropertyBuilder builder = createBuilder(StaticTypingBean.class);
        BeanDescription beanDesc = config.introspect(mapper.constructType(StaticTypingBean.class));
        BeanPropertyDefinition propDef = beanDesc.findProperties().get(0);
        AnnotatedMember am = propDef.getPrimaryMember();
        JavaType result = builder.findSerializationType(am, false, mapper.constructType(Object.class));
        assertNotNull(result);
        assertTrue(result.isStatic());
    }

    @Test
    public void testFindSerializationType_dynamicTypingAnnotated_returnsNonStatic() {
        PropertyBuilder builder = createBuilder(DynamicTypingBean.class);
        BeanDescription beanDesc = config.introspect(mapper.constructType(DynamicTypingBean.class));
        BeanPropertyDefinition propDef = beanDesc.findProperties().get(0);
        AnnotatedMember am = propDef.getPrimaryMember();
        JavaType result = builder.findSerializationType(am, false, mapper.constructType(Object.class));
        assertNotNull(result);
        assertFalse(result.isStatic());
    }

    @Test
    public void testFindSerializationType_refinedTypeValid_returnsRefinedStaticTyping() {
        PropertyBuilder builder = createBuilder(RefinedValidBean.class);
        BeanDescription beanDesc = config.introspect(mapper.constructType(RefinedValidBean.class));
        BeanPropertyDefinition propDef = beanDesc.findProperties().get(0);
        AnnotatedMember am = propDef.getPrimaryMember();
        JavaType result = builder.findSerializationType(am, false, mapper.constructType(String.class));
        assertNotNull(result);
        assertTrue(result.isStatic());
        assertEquals(Object.class, result.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindSerializationType_refinedTypeInvalid_throwsIllegalArgumentException() {
        PropertyBuilder builder = createBuilder(RefinedInvalidBean.class);
        BeanDescription beanDesc = config.introspect(mapper.constructType(RefinedInvalidBean.class));
        BeanPropertyDefinition propDef = beanDesc.findProperties().get(0);
        AnnotatedMember am = propDef.getPrimaryMember();
        builder.findSerializationType(am, false, mapper.constructType(Integer.class));
    }

    // Tests for buildWriter()
    @Test
    public void testBuildWriter_alwaysInclusion_createsWriter() throws Exception {
        PropertyBuilder builder = createBuilder(DefaultBean.class);
        BeanDescription beanDesc = config.introspect(mapper.constructType(DefaultBean.class));
        BeanPropertyDefinition propDef = beanDesc.findProperties().get(0);
        AnnotatedMember am = propDef.getPrimaryMember();
        JavaType declaredType = mapper.constructType(int.class);
        SerializerProvider prov = mapper.getSerializerProvider();
        JsonSerializer<?> ser = prov.findValueSerializer(declaredType);
        BeanPropertyWriter bpw = builder.buildWriter(prov, propDef, declaredType, ser, null, null, am, false);
        assertNotNull(bpw);
        assertEquals("value", bpw.getName());
    }

    @Test
    public void testBuildWriter_nonEmptyContainer_createsWriter() throws Exception {
        PropertyBuilder builder = createBuilder(NonEmptyContainerBean.class);
        BeanDescription beanDesc = config.introspect(mapper.constructType(NonEmptyContainerBean.class));
        BeanPropertyDefinition propDef = beanDesc.findProperties().get(0);
        AnnotatedMember am = propDef.getPrimaryMember();
        JavaType declaredType = mapper.constructType(java.util.List.class);
        SerializerProvider prov = mapper.getSerializerProvider();
        JsonSerializer<?> ser = prov.findValueSerializer(declaredType);
        BeanPropertyWriter bpw = builder.buildWriter(prov, propDef, declaredType, ser, null, null, am, false);
        assertNotNull(bpw);
        assertEquals("items", bpw.getName());
    }

    @Test
    public void testBuildWriter_nonNullInclusion_createsWriter() throws Exception {
        PropertyBuilder builder = createBuilder(NonNullInclusionBean.class);
        BeanDescription beanDesc = config.introspect(mapper.constructType(NonNullInclusionBean.class));
        BeanPropertyDefinition propDef = beanDesc.findProperties().get(0);
        AnnotatedMember am = propDef.getPrimaryMember();
        JavaType declaredType = mapper.constructType(String.class);
        SerializerProvider prov = mapper.getSerializerProvider();
        JsonSerializer<?> ser = prov.findValueSerializer(declaredType);
        BeanPropertyWriter bpw = builder.buildWriter(prov, propDef, declaredType, ser, null, null, am, false);
        assertNotNull(bpw);
        assertEquals("name", bpw.getName());
    }
}