package com.fasterxml.jackson.databind.jsontype.impl;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class TypeDeserializerBaseTest {

    private ObjectMapper mapper;
    private JavaType baseJavaType;

    // Concrete implementation of TypeDeserializerBase for testing
    static class TestableTypeDeserializerBase extends TypeDeserializerBase {
        private static final long serialVersionUID = 1L;

        public TestableTypeDeserializerBase(JavaType baseType, TypeIdResolver idRes,
                String typePropertyName, boolean typeIdVisible, JavaType defaultImpl) {
            super(baseType, idRes, typePropertyName, typeIdVisible, defaultImpl);
        }

        public TestableTypeDeserializerBase(TestableTypeDeserializerBase src, BeanProperty property) {
            super(src, property);
        }

        @Override
        public TypeDeserializer forProperty(BeanProperty prop) {
            return new TestableTypeDeserializerBase(this, prop);
        }

        @Override
        public JsonTypeInfo.As getTypeInclusion() {
            return JsonTypeInfo.As.PROPERTY;
        }

        public JsonDeserializer<Object> findDeserializer(DeserializationContext ctxt, String typeId) throws IOException {
            return _findDeserializer(ctxt, typeId);
        }

        public JsonDeserializer<Object> findDefaultImplDeserializer(DeserializationContext ctxt) throws IOException {
            return _findDefaultImplDeserializer(ctxt);
        }

        public Object deserializeWithNativeTypeId(JsonParser jp, DeserializationContext ctxt, Object typeId) throws IOException {
            return _deserializeWithNativeTypeId(jp, ctxt, typeId);
        }

        public JavaType handleUnknownTypeId(DeserializationContext ctxt, String typeId) throws IOException {
            return _handleUnknownTypeId(ctxt, typeId);
        }

        public JavaType handleMissingTypeId(DeserializationContext ctxt, String extraDesc) throws IOException {
            return _handleMissingTypeId(ctxt, extraDesc);
        }
    }

    // Dummy TypeIdResolver for testing
    static class StubTypeIdResolver implements TypeIdResolver {
        private JavaType baseType;
        private String knownTypeDesc = "test-types";

        public StubTypeIdResolver(JavaType baseType) {
            this.baseType = baseType;
        }

        public void setDescForKnownTypeIds(String desc) {
            this.knownTypeDesc = desc;
        }

        @Override
        public void init(JavaType baseType) {
            this.baseType = baseType;
        }

        @Override
        public String idFromValue(Object value) {
            return value == null ? null : value.getClass().getName();
        }

        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) {
            return suggestedType.getName();
        }

        @Override
        public String idFromBaseType() {
            return baseType.getRawClass().getName();
        }

        @Override
        public JavaType typeFromId(DeserializationContext context, String id) throws IOException {
            if ("known".equals(id)) {
                return baseType;
            }
            if ("string".equals(id) || "123".equals(id)) {
                return TypeFactory.defaultInstance().constructType(String.class);
            }
            if ("bogus_void".equals(id)) {
                return TypeFactory.defaultInstance().constructType(Void.class);
            }
            return null;
        }

        @Override
        public String getDescForKnownTypeIds() {
            return knownTypeDesc;
        }

        @Override
        public JsonTypeInfo.Id getMechanism() {
            return JsonTypeInfo.Id.CUSTOM;
        }
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        baseJavaType = TypeFactory.defaultInstance().constructType(Number.class);
    }

    private DeserializationContext createDeserializationContext(ObjectMapper mapper, JsonParser parser) {
        return ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), parser, mapper.getInjectableValues());
    }

    // Tests basic accessors
    @Test
    public void testAccessors_initializedProperties_returnsCorrectValues() {
        StubTypeIdResolver resolver = new StubTypeIdResolver(baseJavaType);
        JavaType defaultImpl = TypeFactory.defaultInstance().constructType(Integer.class);
        TestableTypeDeserializerBase deser = new TestableTypeDeserializerBase(
                baseJavaType, resolver, "@type", true, defaultImpl);

        assertEquals("java.lang.Number", deser.baseTypeName());
        assertEquals("@type", deser.getPropertyName());
        assertSame(resolver, deser.getTypeIdResolver());
        assertEquals(Integer.class, deser.getDefaultImpl());
        assertSame(baseJavaType, deser.baseType());
        assertEquals(JsonTypeInfo.As.PROPERTY, deser.getTypeInclusion());
        assertTrue(deser.toString().contains("base-type:"));
    }

    // Tests hasDefaultImpl behavior
    @Test
    public void testHasDefaultImpl_configuredOrNot_returnsExpectedBoolean() {
        StubTypeIdResolver resolver = new StubTypeIdResolver(baseJavaType);
        JavaType defaultImpl = TypeFactory.defaultInstance().constructType(Integer.class);

        TestableTypeDeserializerBase deserWithDefault = new TestableTypeDeserializerBase(
                baseJavaType, resolver, "@type", true, defaultImpl);
        assertTrue(deserWithDefault.hasDefaultImpl());

        TestableTypeDeserializerBase deserWithoutDefault = new TestableTypeDeserializerBase(
                baseJavaType, resolver, "@type", true, null);
        assertFalse(deserWithoutDefault.hasDefaultImpl());
        assertNull(deserWithoutDefault.getDefaultImpl());
    }

    // Tests copy constructor and forProperty behavior
    @Test
    public void testForProperty_validProperty_createsCopy() {
        StubTypeIdResolver resolver = new StubTypeIdResolver(baseJavaType);
        TestableTypeDeserializerBase deser = new TestableTypeDeserializerBase(
                baseJavaType, resolver, "@type", false, null);

        BeanProperty.Std prop = new BeanProperty.Std(
                PropertyName.construct("testProp"), baseJavaType, null, null, PropertyMetadata.STD_OPTIONAL);

        TypeDeserializer copy = deser.forProperty(prop);
        assertNotNull(copy);
        assertNotSame(deser, copy);
        assertEquals("@type", copy.getPropertyName());
    }

    // Tests _findDefaultImplDeserializer with null defaultImpl and FAIL_ON_INVALID_SUBTYPE disabled
    @Test
    public void testFindDefaultImplDeserializer_nullDefaultImpl_featureDisabled_returnsNullifyingDeserializer() throws Exception {
        StubTypeIdResolver resolver = new StubTypeIdResolver(baseJavaType);
        TestableTypeDeserializerBase deser = new TestableTypeDeserializerBase(
                baseJavaType, resolver, "type", false, null);

        mapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        JsonParser parser = new JsonFactory().createParser("{}");
        DeserializationContext ctxt = createDeserializationContext(mapper, parser);

        JsonDeserializer<Object> result = deser.findDefaultImplDeserializer(ctxt);
        assertSame(NullifyingDeserializer.instance, result);
    }

    // Tests _findDefaultImplDeserializer with null defaultImpl and FAIL_ON_INVALID_SUBTYPE enabled
    @Test
    public void testFindDefaultImplDeserializer_nullDefaultImpl_featureEnabled_returnsNull() throws Exception {
        StubTypeIdResolver resolver = new StubTypeIdResolver(baseJavaType);
        TestableTypeDeserializerBase deser = new TestableTypeDeserializerBase(
                baseJavaType, resolver, "type", false, null);

        mapper.enable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        JsonParser parser = new JsonFactory().createParser("{}");
        DeserializationContext ctxt = createDeserializationContext(mapper, parser);

        JsonDeserializer<Object> result = deser.findDefaultImplDeserializer(ctxt);
        assertNull(result);
    }

    // Tests _findDefaultImplDeserializer with bogus/Void class as defaultImpl
    @Test
    public void testFindDefaultImplDeserializer_bogusClassDefaultImpl_returnsNullifyingDeserializer() throws Exception {
        JavaType voidType = TypeFactory.defaultInstance().constructType(Void.class);
        StubTypeIdResolver resolver = new StubTypeIdResolver(baseJavaType);
        TestableTypeDeserializerBase deser = new TestableTypeDeserializerBase(
                baseJavaType, resolver, "type", false, voidType);

        JsonParser parser = new JsonFactory().createParser("{}");
        DeserializationContext ctxt = createDeserializationContext(mapper, parser);

        JsonDeserializer<Object> result = deser.findDefaultImplDeserializer(ctxt);
        assertSame(NullifyingDeserializer.instance, result);
    }

    // Tests _findDefaultImplDeserializer with valid concrete defaultImpl
    @Test
    public void testFindDefaultImplDeserializer_validDefaultImpl_returnsDeserializer() throws Exception {
        JavaType defaultImpl = TypeFactory.defaultInstance().constructType(String.class);
        StubTypeIdResolver resolver = new StubTypeIdResolver(baseJavaType);
        TestableTypeDeserializerBase deser = new TestableTypeDeserializerBase(
                baseJavaType, resolver, "type", false, defaultImpl);

        JsonParser parser = new JsonFactory().createParser("\"test\"");
        DeserializationContext ctxt = createDeserializationContext(mapper, parser);

        JsonDeserializer<Object> result1 = deser.findDefaultImplDeserializer(ctxt);
        assertNotNull(result1);

        // Cached path
        JsonDeserializer<Object> result2 = deser.findDefaultImplDeserializer(ctxt);
        assertSame(result1, result2);
    }

    // Tests _findDeserializer with known typeId resolved and cached
    @Test
    public void testFindDeserializer_knownTypeId_resolvesAndCaches() throws Exception {
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        StubTypeIdResolver resolver = new StubTypeIdResolver(strType);
        TestableTypeDeserializerBase deser = new TestableTypeDeserializerBase(
                strType, resolver, "type", false, null);

        JsonParser parser = new JsonFactory().createParser("\"hello\"");
        DeserializationContext ctxt = createDeserializationContext(mapper, parser);

        JsonDeserializer<Object> d1 = deser.findDeserializer(ctxt, "string");
        assertNotNull(d1);

        // Retrieve from cache
        JsonDeserializer<Object> d2 = deser.findDeserializer(ctxt, "string");
        assertSame(d1, d2);
    }

    // Tests _findDeserializer when typeId is unknown and falls back to defaultImpl
    @Test
    public void testFindDeserializer_unknownTypeIdWithDefaultImpl_returnsDefaultDeserializer() throws Exception {
        JavaType defaultImpl = TypeFactory.defaultInstance().constructType(String.class);
        StubTypeIdResolver resolver = new StubTypeIdResolver(baseJavaType);
        TestableTypeDeserializerBase deser = new TestableTypeDeserializerBase(
                baseJavaType, resolver, "type", false, defaultImpl);

        JsonParser parser = new JsonFactory().createParser("\"test\"");
        DeserializationContext ctxt = createDeserializationContext(mapper, parser);

        JsonDeserializer<Object> d = deser.findDeserializer(ctxt, "unknown_type_id");
        assertNotNull(d);
    }

    // Tests _findDeserializer when typeId is unknown and throws mapping exception
    @Test(expected = JsonMappingException.class)
    public void testFindDeserializer_unknownTypeIdNoDefault_throwsException() throws Exception {
        StubTypeIdResolver resolver = new StubTypeIdResolver(baseJavaType);
        TestableTypeDeserializerBase deser = new TestableTypeDeserializerBase(
                baseJavaType, resolver, "type", false, null);

        mapper.enable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        JsonParser parser = new JsonFactory().createParser("{}");
        DeserializationContext ctxt = createDeserializationContext(mapper, parser);

        deser.findDeserializer(ctxt, "invalid_type");
    }

    // Tests _handleUnknownTypeId with null known type descriptions
    @Test(expected = JsonMappingException.class)
    public void testHandleUnknownTypeId_nullKnownDesc_throwsException() throws Exception {
        StubTypeIdResolver resolver = new StubTypeIdResolver(baseJavaType);
        resolver.setDescForKnownTypeIds(null);
        TestableTypeDeserializerBase deser = new TestableTypeDeserializerBase(
                baseJavaType, resolver, "type", false, null);

        JsonParser parser = new JsonFactory().createParser("{}");
        DeserializationContext ctxt = createDeserializationContext(mapper, parser);

        deser.handleUnknownTypeId(ctxt, "some_id");
    }

    // Tests _handleMissingTypeId throws exception
    @Test(expected = JsonMappingException.class)
    public void testHandleMissingTypeId_throwsException() throws Exception {
        StubTypeIdResolver resolver = new StubTypeIdResolver(baseJavaType);
        TestableTypeDeserializerBase deser = new TestableTypeDeserializerBase(
                baseJavaType, resolver, "type", false, null);

        JsonParser parser = new JsonFactory().createParser("{}");
        DeserializationContext ctxt = createDeserializationContext(mapper, parser);

        deser.handleMissingTypeId(ctxt, "missing type info");
    }

    // Tests _deserializeWithNativeTypeId with valid type id
    @Test
    public void testDeserializeWithNativeTypeId_withValidTypeId_deserializesCorrectly() throws Exception {
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        StubTypeIdResolver resolver = new StubTypeIdResolver(strType);
        TestableTypeDeserializerBase deser = new TestableTypeDeserializerBase(
                strType, resolver, "type", false, null);

        JsonParser parser = new JsonFactory().createParser("\"hello native\"");
        parser.nextToken();
        DeserializationContext ctxt = createDeserializationContext(mapper, parser);

        Object result = deser.deserializeWithNativeTypeId(parser, ctxt, "string");
        assertEquals("hello native", result);
    }

    // Tests _deserializeWithNativeTypeId with non-String typeId
    @Test
    public void testDeserializeWithNativeTypeId_nonStringTypeId_deserializesCorrectly() throws Exception {
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        StubTypeIdResolver resolver = new StubTypeIdResolver(strType);
        TestableTypeDeserializerBase deser = new TestableTypeDeserializerBase(
                strType, resolver, "type", false, null);

        JsonParser parser = new JsonFactory().createParser("\"hello 123\"");
        parser.nextToken();
        DeserializationContext ctxt = createDeserializationContext(mapper, parser);

        Object result = deser.deserializeWithNativeTypeId(parser, ctxt, Integer.valueOf(123));
        assertEquals("hello 123", result);
    }

    // Tests _deserializeWithNativeTypeId with null typeId and defaultImpl available
    @Test
    public void testDeserializeWithNativeTypeId_nullTypeIdWithDefaultImpl_deserializesCorrectly() throws Exception {
        JavaType defaultImpl = TypeFactory.defaultInstance().constructType(String.class);
        StubTypeIdResolver resolver = new StubTypeIdResolver(baseJavaType);
        TestableTypeDeserializerBase deser = new TestableTypeDeserializerBase(
                baseJavaType, resolver, "type", false, defaultImpl);

        JsonParser parser = new JsonFactory().createParser("\"default value\"");
        parser.nextToken();
        DeserializationContext ctxt = createDeserializationContext(mapper, parser);

        Object result = deser.deserializeWithNativeTypeId(parser, ctxt, null);
        assertEquals("default value", result);
    }

    // Tests _deserializeWithNativeTypeId with null typeId and no defaultImpl throws exception
    @Test(expected = JsonMappingException.class)
    public void testDeserializeWithNativeTypeId_nullTypeIdNoDefaultImpl_throwsException() throws Exception {
        StubTypeIdResolver resolver = new StubTypeIdResolver(baseJavaType);
        TestableTypeDeserializerBase deser = new TestableTypeDeserializerBase(
                baseJavaType, resolver, "type", false, null);

        mapper.enable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        JsonParser parser = new JsonFactory().createParser("{}");
        parser.nextToken();
        DeserializationContext ctxt = createDeserializationContext(mapper, parser);

        deser.deserializeWithNativeTypeId(parser, ctxt, null);
    }
}