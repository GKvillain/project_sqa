package com.fasterxml.jackson.databind.jsontype.impl;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class TypeDeserializerBaseTest {

    private ObjectMapper mapper;
    private TypeFactory typeFactory;
    private JavaType baseType;
    private TypeIdResolver idResolver;

    static class DummyTypeDeserializer extends TypeDeserializerBase {
        public DummyTypeDeserializer(JavaType baseType, TypeIdResolver idRes,
                String typePropertyName, boolean typeIdVisible, Class<?> defaultImpl) {
            super(baseType, idRes, typePropertyName, typeIdVisible, defaultImpl);
        }

        public DummyTypeDeserializer(JavaType baseType, TypeIdResolver idRes,
                String typePropertyName, boolean typeIdVisible, JavaType defaultImpl) {
            super(baseType, idRes, typePropertyName, typeIdVisible, defaultImpl);
        }

        public DummyTypeDeserializer(DummyTypeDeserializer src, BeanProperty property) {
            super(src, property);
        }

        @Override
        public TypeDeserializer forProperty(BeanProperty prop) {
            return new DummyTypeDeserializer(this, prop);
        }

        @Override
        public JsonTypeInfo.As getTypeInclusion() {
            return JsonTypeInfo.As.PROPERTY;
        }
    }

    static class CustomTypeIdResolver extends TypeIdResolverBase {
        private final String desc;

        public CustomTypeIdResolver(JavaType baseType, TypeFactory typeFactory, String desc) {
            super(baseType, typeFactory);
            this.desc = desc;
        }

        @Override
        public String idFromValue(Object value) {
            return value.getClass().getName();
        }

        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) {
            return suggestedType.getName();
        }

        @Override
        public JsonTypeInfo.Id getMechanism() {
            return JsonTypeInfo.Id.CUSTOM;
        }

        @Override
        public JavaType typeFromId(String id) {
            return null;
        }

        @Override
        public JavaType typeFromId(DatabindContext context, String id) {
            return null;
        }

        @Override
        public String getDescForKnownTypeIds() {
            return desc;
        }
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        typeFactory = mapper.getTypeFactory();
        baseType = typeFactory.constructType(CharSequence.class);
        idResolver = new ClassNameIdResolver(baseType, typeFactory);
    }

    private DeserializationContext createDeserializationContext(JsonParser parser) {
        DefaultDeserializationContext defCtxt = (DefaultDeserializationContext) mapper.getDeserializationContext();
        return defCtxt.createInstance(mapper.getDeserializationConfig(), parser, mapper.getInjectableValues());
    }

    // Tests normal constructor and getter accessors
    @Test
    public void testConstructorAndAccessors_validInputs_returnsExpectedValues() {
        DummyTypeDeserializer deser = new DummyTypeDeserializer(
                baseType, idResolver, "@type", true, String.class);

        assertEquals("java.lang.CharSequence", deser.baseTypeName());
        assertEquals("@type", deser.getPropertyName());
        assertSame(idResolver, deser.getTypeIdResolver());
        assertEquals(String.class, deser.getDefaultImpl());
        assertTrue(deser.hasDefaultImpl());
        assertEquals(JsonTypeInfo.As.PROPERTY, deser.getTypeInclusion());

        String str = deser.toString();
        assertNotNull(str);
        assertTrue(str.contains("base-type"));
        assertTrue(str.contains("id-resolver"));
    }

    // Tests constructor with JavaType defaultImpl
    @Test
    public void testConstructor_withJavaTypeDefaultImpl() {
        JavaType defaultType = typeFactory.constructType(String.class);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(
                baseType, idResolver, "@type", true, defaultType);

        assertEquals(String.class, deser.getDefaultImpl());
        assertTrue(deser.hasDefaultImpl());
    }

    // Tests null defaultImpl
    @Test
    public void testConstructor_nullDefaultImpl_getDefaultImplReturnsNull() {
        DummyTypeDeserializer deser = new DummyTypeDeserializer(
                baseType, idResolver, "@type", false, (Class<?>) null);

        assertNull(deser.getDefaultImpl());
        assertFalse(deser.hasDefaultImpl());
    }

    // Tests forProperty copy constructor
    @Test
    public void testForProperty_validProperty_preservesProperties() {
        DummyTypeDeserializer deser = new DummyTypeDeserializer(
                baseType, idResolver, "@type", true, String.class);

        BeanProperty.Bogus prop = new BeanProperty.Bogus();
        TypeDeserializer copy = deser.forProperty(prop);

        assertNotNull(copy);
        assertTrue(copy instanceof DummyTypeDeserializer);
        assertEquals(deser.baseTypeName(), ((DummyTypeDeserializer) copy).baseTypeName());
        assertEquals(deser.getPropertyName(), copy.getPropertyName());
        assertEquals(deser.getDefaultImpl(), copy.getDefaultImpl());
    }

    // Tests finding default impl deserializer when defaultImpl is null and FAIL_ON_INVALID_SUBTYPE is disabled
    @Test
    public void testFindDefaultImplDeserializer_nullDefaultImplFailDisabled_returnsNullifyingDeserializer() throws Exception {
        mapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        JsonParser parser = mapper.getFactory().createParser("{}");
        DeserializationContext ctxt = createDeserializationContext(parser);

        DummyTypeDeserializer deser = new DummyTypeDeserializer(
                baseType, idResolver, "@type", false, (Class<?>) null);

        JsonDeserializer<Object> defaultDeser = deser._findDefaultImplDeserializer(ctxt);
        assertSame(NullifyingDeserializer.instance, defaultDeser);
    }

    // Tests finding default impl deserializer when defaultImpl is null and FAIL_ON_INVALID_SUBTYPE is enabled
    @Test
    public void testFindDefaultImplDeserializer_nullDefaultImplFailEnabled_returnsNull() throws Exception {
        mapper.enable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        JsonParser parser = mapper.getFactory().createParser("{}");
        DeserializationContext ctxt = createDeserializationContext(parser);

        DummyTypeDeserializer deser = new DummyTypeDeserializer(
                baseType, idResolver, "@type", false, (Class<?>) null);

        JsonDeserializer<Object> defaultDeser = deser._findDefaultImplDeserializer(ctxt);
        assertNull(defaultDeser);
    }

    // Tests finding default impl deserializer when defaultImpl is a bogus class (e.g. Void.class)
    @Test
    public void testFindDefaultImplDeserializer_bogusDefaultImpl_returnsNullifyingDeserializer() throws Exception {
        JavaType objType = typeFactory.constructType(Object.class);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(
                objType, idResolver, "@type", false, Void.class);

        JsonParser parser = mapper.getFactory().createParser("{}");
        DeserializationContext ctxt = createDeserializationContext(parser);

        JsonDeserializer<Object> defaultDeser = deser._findDefaultImplDeserializer(ctxt);
        assertSame(NullifyingDeserializer.instance, defaultDeser);
    }

    // Tests finding default impl deserializer with a valid default class
    @Test
    public void testFindDefaultImplDeserializer_validDefaultImpl_returnsContextualDeserializer() throws Exception {
        DummyTypeDeserializer deser = new DummyTypeDeserializer(
                baseType, idResolver, "@type", false, String.class);

        JsonParser parser = mapper.getFactory().createParser("{}");
        DeserializationContext ctxt = createDeserializationContext(parser);

        JsonDeserializer<Object> defaultDeser = deser._findDefaultImplDeserializer(ctxt);
        assertNotNull(defaultDeser);
        // Verify caching returns same instance
        assertSame(defaultDeser, deser._findDefaultImplDeserializer(ctxt));
    }

    // Tests finding deserializer with a resolvable type id
    @Test
    public void testFindDeserializer_validTypeId_resolvesAndCaches() throws Exception {
        DummyTypeDeserializer deser = new DummyTypeDeserializer(
                baseType, idResolver, "@type", false, (Class<?>) null);

        JsonParser parser = mapper.getFactory().createParser("\"test\"");
        DeserializationContext ctxt = createDeserializationContext(parser);

        JsonDeserializer<Object> resolvedDeser = deser._findDeserializer(ctxt, String.class.getName());
        assertNotNull(resolvedDeser);
        // Call again to verify cache lookup branch
        JsonDeserializer<Object> cachedDeser = deser._findDeserializer(ctxt, String.class.getName());
        assertSame(resolvedDeser, cachedDeser);
    }

    // Tests finding deserializer for unknown type id when defaultImpl is present
    @Test
    public void testFindDeserializer_unknownTypeIdWithDefaultImpl_returnsDefaultImplDeser() throws Exception {
        DummyTypeDeserializer deser = new DummyTypeDeserializer(
                baseType, idResolver, "@type", false, String.class);

        JsonParser parser = mapper.getFactory().createParser("\"test\"");
        DeserializationContext ctxt = createDeserializationContext(parser);

        JsonDeserializer<Object> resolved = deser._findDeserializer(ctxt, "non.existent.TypeId");
        assertNotNull(resolved);
    }

    // Tests finding deserializer for unknown type id when defaultImpl is absent throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testFindDeserializer_unknownTypeIdWithoutDefaultImpl_throwsJsonMappingException() throws Exception {
        DummyTypeDeserializer deser = new DummyTypeDeserializer(
                baseType, idResolver, "@type", false, (Class<?>) null);

        JsonParser parser = mapper.getFactory().createParser("\"test\"");
        DeserializationContext ctxt = createDeserializationContext(parser);

        deser._findDeserializer(ctxt, "invalid.TypeId");
    }

    // Tests deserializing with native type id (2-arg method)
    @Test
    public void testDeserializeWithNativeTypeId_twoArgMethod() throws Exception {
        DummyTypeDeserializer deser = new DummyTypeDeserializer(
                baseType, idResolver, "@type", false, String.class);

        JsonParser parser = mapper.getFactory().createParser("\"hello\"");
        parser.nextToken();
        DeserializationContext ctxt = createDeserializationContext(parser);

        Object result = deser._deserializeWithNativeTypeId(parser, ctxt);
        assertEquals("hello", result);
    }

    // Tests deserializing with native type id when typeId is null and defaultImpl exists
    @Test
    public void testDeserializeWithNativeTypeId_nullTypeIdWithDefaultImpl_deserializes() throws Exception {
        DummyTypeDeserializer deser = new DummyTypeDeserializer(
                baseType, idResolver, "@type", false, String.class);

        JsonParser parser = mapper.getFactory().createParser("\"hello\"");
        parser.nextToken();
        DeserializationContext ctxt = createDeserializationContext(parser);

        Object result = deser._deserializeWithNativeTypeId(parser, ctxt, null);
        assertEquals("hello", result);
    }

    // Tests deserializing with native type id when typeId is null and no defaultImpl throws exception
    @Test(expected = JsonMappingException.class)
    public void testDeserializeWithNativeTypeId_nullTypeIdNoDefaultImpl_throwsMappingException() throws Exception {
        DummyTypeDeserializer deser = new DummyTypeDeserializer(
                baseType, idResolver, "@type", false, (Class<?>) null);

        JsonParser parser = mapper.getFactory().createParser("\"hello\"");
        parser.nextToken();
        DeserializationContext ctxt = createDeserializationContext(parser);

        deser._deserializeWithNativeTypeId(parser, ctxt, null);
    }

    // Tests deserializing with native type id when typeId is valid
    @Test
    public void testDeserializeWithNativeTypeId_validTypeId_deserializes() throws Exception {
        DummyTypeDeserializer deser = new DummyTypeDeserializer(
                baseType, idResolver, "@type", false, (Class<?>) null);

        JsonParser parser = mapper.getFactory().createParser("\"world\"");
        parser.nextToken();
        DeserializationContext ctxt = createDeserializationContext(parser);

        Object result = deser._deserializeWithNativeTypeId(parser, ctxt, String.class.getName());
        assertEquals("world", result);
    }

    // Tests _handleMissingTypeId
    @Test(expected = JsonMappingException.class)
    public void testHandleMissingTypeId_throwsMappingException() throws Exception {
        DummyTypeDeserializer deser = new DummyTypeDeserializer(
                baseType, idResolver, "@type", false, (Class<?>) null);

        JsonParser parser = mapper.getFactory().createParser("{}");
        DeserializationContext ctxt = createDeserializationContext(parser);

        deser._handleMissingTypeId(ctxt, "missing type id details");
    }

    // Tests _handleUnknownTypeId with TypeIdResolverBase providing extra description
    @Test
    public void testHandleUnknownTypeId_resolverWithDesc_includesDescInException() throws Exception {
        CustomTypeIdResolver customResolver = new CustomTypeIdResolver(baseType, typeFactory, "id1, id2");
        DummyTypeDeserializer deser = new DummyTypeDeserializer(
                baseType, customResolver, "@type", false, (Class<?>) null);

        JsonParser parser = mapper.getFactory().createParser("{}");
        DeserializationContext ctxt = createDeserializationContext(parser);

        try {
            deser._handleUnknownTypeId(ctxt, "unknownId", customResolver, baseType);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("known type ids = id1, id2"));
        }
    }

    // Tests _handleUnknownTypeId with TypeIdResolverBase returning null description
    @Test
    public void testHandleUnknownTypeId_resolverWithNullDesc_includesStaticKnownMessage() throws Exception {
        CustomTypeIdResolver customResolver = new CustomTypeIdResolver(baseType, typeFactory, null);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(
                baseType, customResolver, "@type", false, (Class<?>) null);

        JsonParser parser = mapper.getFactory().createParser("{}");
        DeserializationContext ctxt = createDeserializationContext(parser);

        try {
            deser._handleUnknownTypeId(ctxt, "unknownId", customResolver, baseType);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("known type ids are not statically known"));
        }
    }

    // Tests _handleUnknownTypeId with a TypeIdResolver that is not TypeIdResolverBase
    @Test
    public void testHandleUnknownTypeId_nonTypeIdResolverBase_throwsJsonMappingException() throws Exception {
        TypeIdResolver mockResolver = new TypeIdResolver() {
            @Override
            public void init(JavaType baseType) {}
            @Override
            public String idFromValue(Object value) { return null; }
            @Override
            public String idFromValueAndType(Object value, Class<?> suggestedType) { return null; }
            @Override
            public String idFromBaseType() { return null; }
            @Override
            public JavaType typeFromId(String id) { return null; }
            @Override
            public JavaType typeFromId(DatabindContext context, String id) throws IOException { return null; }
            @Override
            public String getDescForKnownTypeIds() { return null; }
            @Override
            public JsonTypeInfo.Id getMechanism() { return JsonTypeInfo.Id.CUSTOM; }
        };

        DummyTypeDeserializer deser = new DummyTypeDeserializer(
                baseType, mockResolver, "@type", false, (Class<?>) null);

        JsonParser parser = mapper.getFactory().createParser("{}");
        DeserializationContext ctxt = createDeserializationContext(parser);

        try {
            deser._handleUnknownTypeId(ctxt, "unknownId", mockResolver, baseType);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertNotNull(e.getMessage());
        }
    }
}