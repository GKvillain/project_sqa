package com.fasterxml.jackson.databind.jsontype.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class TypeDeserializerBaseTest {

    private ObjectMapper _mapper;
    private TypeFactory _typeFactory;

    // Concrete test implementation of TypeDeserializerBase
    private static class TestableTypeDeserializer extends TypeDeserializerBase {
        private static final long serialVersionUID = 1L;

        public TestableTypeDeserializer(JavaType baseType, TypeIdResolver idRes,
                String typePropertyName, boolean typeIdVisible, JavaType defaultImpl) {
            super(baseType, idRes, typePropertyName, typeIdVisible, defaultImpl);
        }

        public TestableTypeDeserializer(TestableTypeDeserializer src, BeanProperty prop) {
            super(src, prop);
        }

        @Override
        public TypeDeserializer forProperty(BeanProperty prop) {
            return new TestableTypeDeserializer(this, prop);
        }

        @Override
        public JsonTypeInfo.As getTypeInclusion() {
            return JsonTypeInfo.As.PROPERTY;
        }

        @Override
        public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }

        @Override
        public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }

        @Override
        public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }

        @Override
        public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }

        public JsonDeserializer<Object> findDeserializer(DeserializationContext ctxt, String typeId) throws IOException {
            return _findDeserializer(ctxt, typeId);
        }

        public JsonDeserializer<Object> findDefaultImplDeserializer(DeserializationContext ctxt) throws IOException {
            return _findDefaultImplDeserializer(ctxt);
        }

        public Object deserializeWithNativeTypeId(JsonParser jp, DeserializationContext ctxt) throws IOException {
            return _deserializeWithNativeTypeId(jp, ctxt);
        }

        public Object deserializeWithNativeTypeId(JsonParser jp, DeserializationContext ctxt, Object typeId) throws IOException {
            return _deserializeWithNativeTypeId(jp, ctxt, typeId);
        }

        public JavaType handleUnknownTypeId(DeserializationContext ctxt, String typeId,
                TypeIdResolver idResolver, JavaType baseType) throws IOException {
            return _handleUnknownTypeId(ctxt, typeId, idResolver, baseType);
        }
    }

    private static class DummyTypeIdResolver extends TypeIdResolverBase {
        private final JavaType _resolvedType;

        public DummyTypeIdResolver(JavaType resolvedType, TypeFactory typeFactory) {
            super(resolvedType, typeFactory);
            this._resolvedType = resolvedType;
        }

        @Override
        public String idFromValue(Object value) {
            return "dummy";
        }

        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) {
            return "dummy";
        }

        @Override
        public JsonTypeInfo.Id getMechanism() {
            return JsonTypeInfo.Id.CUSTOM;
        }

        @Override
        public JavaType typeFromId(String id) {
            return _resolvedType;
        }

        @Override
        public JavaType typeFromId(com.fasterxml.jackson.databind.DatabindContext context, String id) {
            if ("known".equals(id)) {
                return _resolvedType;
            }
            return null;
        }

        @Override
        public String getDescForKnownTypeIds() {
            return "known";
        }
    }

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _typeFactory = _mapper.getTypeFactory();
    }

    private DeserializationContext createDeserializationContext() {
        return _mapper.getDeserializationContext();
    }

    // Tests normal property name and base type accessors
    @Test
    public void testAccessors_validInputs_returnsCorrectValues() {
        JavaType baseType = _typeFactory.constructType(Number.class);
        JavaType defaultImpl = _typeFactory.constructType(Integer.class);
        TypeIdResolver idRes = new ClassNameIdResolver(baseType, _typeFactory);

        TestableTypeDeserializer deser = new TestableTypeDeserializer(baseType, idRes, "@type", true, defaultImpl);

        assertEquals("@type", deser.getPropertyName());
        assertEquals(Number.class.getName(), deser.baseTypeName());
        assertEquals(Integer.class, deser.getDefaultImpl());
        assertSame(idRes, deser.getTypeIdResolver());
        assertEquals(JsonTypeInfo.As.PROPERTY, deser.getTypeInclusion());
    }

    // Tests constructor when typePropertyName is null (should default to empty string)
    @Test
    public void testConstructor_nullTypePropertyName_defaultsToEmptyString() {
        JavaType baseType = _typeFactory.constructType(Object.class);
        TypeIdResolver idRes = new ClassNameIdResolver(baseType, _typeFactory);

        TestableTypeDeserializer deser = new TestableTypeDeserializer(baseType, idRes, null, false, null);

        assertEquals("", deser.getPropertyName());
        assertNull(deser.getDefaultImpl());
    }

    // Tests toString format contains class name and base type
    @Test
    public void testToString_validObject_containsBaseTypeAndResolver() {
        JavaType baseType = _typeFactory.constructType(String.class);
        TypeIdResolver idRes = new ClassNameIdResolver(baseType, _typeFactory);
        TestableTypeDeserializer deser = new TestableTypeDeserializer(baseType, idRes, "type", false, null);

        String desc = deser.toString();
        assertTrue(desc.contains(TestableTypeDeserializer.class.getName()));
        assertTrue(desc.contains("base-type:"));
        assertTrue(desc.contains("id-resolver:"));
    }

    // Tests copy constructor via forProperty
    @Test
    public void testForProperty_validProperty_createsCopy() {
        JavaType baseType = _typeFactory.constructType(Object.class);
        TypeIdResolver idRes = new ClassNameIdResolver(baseType, _typeFactory);
        TestableTypeDeserializer deser = new TestableTypeDeserializer(baseType, idRes, "type", false, null);

        TypeDeserializer copy = deser.forProperty(null);
        assertNotNull(copy);
        assertEquals("type", copy.getPropertyName());
    }

    // Tests _findDefaultImplDeserializer when defaultImpl is null and FAIL_ON_INVALID_SUBTYPE is disabled
    @Test
    public void testFindDefaultImplDeserializer_nullDefaultImplFeatureDisabled_returnsNullifyingDeserializer() throws Exception {
        JavaType baseType = _typeFactory.constructType(Object.class);
        TypeIdResolver idRes = new ClassNameIdResolver(baseType, _typeFactory);
        TestableTypeDeserializer deser = new TestableTypeDeserializer(baseType, idRes, "type", false, null);

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonDeserializer<Object> defaultDeser = deser.findDefaultImplDeserializer(ctxt);
        assertSame(NullifyingDeserializer.instance, defaultDeser);
    }

    // Tests _findDefaultImplDeserializer when defaultImpl is null and FAIL_ON_INVALID_SUBTYPE is enabled
    @Test
    public void testFindDefaultImplDeserializer_nullDefaultImplFeatureEnabled_returnsNull() throws Exception {
        JavaType baseType = _typeFactory.constructType(Object.class);
        TypeIdResolver idRes = new ClassNameIdResolver(baseType, _typeFactory);
        TestableTypeDeserializer deser = new TestableTypeDeserializer(baseType, idRes, "type", false, null);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonDeserializer<Object> defaultDeser = deser.findDefaultImplDeserializer(ctxt);
        assertNull(defaultDeser);
    }

    // Tests _findDefaultImplDeserializer with bogus class (e.g. Void.class)
    @Test
    public void testFindDefaultImplDeserializer_bogusClassDefaultImpl_returnsNullifyingDeserializer() throws Exception {
        JavaType baseType = _typeFactory.constructType(Object.class);
        JavaType defaultImpl = _typeFactory.constructType(Void.class);
        TypeIdResolver idRes = new ClassNameIdResolver(baseType, _typeFactory);
        TestableTypeDeserializer deser = new TestableTypeDeserializer(baseType, idRes, "type", false, defaultImpl);

        DeserializationContext ctxt = createDeserializationContext();
        JsonDeserializer<Object> defaultDeser = deser.findDefaultImplDeserializer(ctxt);
        assertSame(NullifyingDeserializer.instance, defaultDeser);
    }

    // Tests _findDefaultImplDeserializer caching
    @Test
    public void testFindDefaultImplDeserializer_validDefaultImpl_returnsCachedDeserializer() throws Exception {
        JavaType baseType = _typeFactory.constructType(Object.class);
        JavaType defaultImpl = _typeFactory.constructType(String.class);
        TypeIdResolver idRes = new ClassNameIdResolver(baseType, _typeFactory);
        TestableTypeDeserializer deser = new TestableTypeDeserializer(baseType, idRes, "type", false, defaultImpl);

        DeserializationContext ctxt = createDeserializationContext();
        JsonDeserializer<Object> deser1 = deser.findDefaultImplDeserializer(ctxt);
        assertNotNull(deser1);
        JsonDeserializer<Object> deser2 = deser.findDefaultImplDeserializer(ctxt);
        assertSame(deser1, deser2);
    }

    // Tests _findDeserializer resolution with known type ID and caching
    @Test
    public void testFindDeserializer_knownTypeId_cachesAndReturnsDeserializer() throws Exception {
        JavaType baseType = _typeFactory.constructType(Object.class);
        JavaType resolvedType = _typeFactory.constructType(String.class);
        TypeIdResolver idRes = new DummyTypeIdResolver(resolvedType, _typeFactory);
        TestableTypeDeserializer deser = new TestableTypeDeserializer(baseType, idRes, "type", false, null);

        DeserializationContext ctxt = createDeserializationContext();
        JsonDeserializer<Object> d1 = deser.findDeserializer(ctxt, "known");
        assertNotNull(d1);
        JsonDeserializer<Object> d2 = deser.findDeserializer(ctxt, "known");
        assertSame(d1, d2);
    }

    // Tests _findDeserializer fallback to default implementation when type id is not resolved
    @Test
    public void testFindDeserializer_unknownTypeIdFallbackToDefault_returnsDefaultDeserializer() throws Exception {
        JavaType baseType = _typeFactory.constructType(Object.class);
        JavaType defaultImpl = _typeFactory.constructType(String.class);
        TypeIdResolver idRes = new DummyTypeIdResolver(null, _typeFactory);
        TestableTypeDeserializer deser = new TestableTypeDeserializer(baseType, idRes, "type", false, defaultImpl);

        DeserializationContext ctxt = createDeserializationContext();
        JsonDeserializer<Object> d = deser.findDeserializer(ctxt, "unknown");
        assertNotNull(d);
    }

    // Tests _findDeserializer specialization of generic/collection base type (related to bug 51)
    @Test
    public void testFindDeserializer_specializedGenericType_resolvesCorrectDeserializer() throws Exception {
        JavaType baseType = _typeFactory.constructCollectionType(List.class, Object.class);
        JavaType subType = _typeFactory.constructCollectionType(ArrayList.class, Object.class);
        TypeIdResolver idRes = new DummyTypeIdResolver(subType, _typeFactory);
        TestableTypeDeserializer deser = new TestableTypeDeserializer(baseType, idRes, "type", false, null);

        DeserializationContext ctxt = createDeserializationContext();
        JsonDeserializer<Object> d = deser.findDeserializer(ctxt, "known");
        assertNotNull(d);
    }

    // Tests _findDeserializer when type ID is completely unknown and no default impl exists
    @Test(expected = JsonMappingException.class)
    public void testFindDeserializer_unknownTypeIdNoDefault_throwsJsonMappingException() throws Exception {
        JavaType baseType = _typeFactory.constructType(Object.class);
        TypeIdResolver idRes = new DummyTypeIdResolver(null, _typeFactory);
        TestableTypeDeserializer deser = new TestableTypeDeserializer(baseType, idRes, "type", false, null);

        DeserializationContext ctxt = createDeserializationContext();
        deser.findDeserializer(ctxt, "completely_unknown");
    }

    // Tests _deserializeWithNativeTypeId with valid native type id
    @Test
    public void testDeserializeWithNativeTypeId_withStringTypeId_deserializesValue() throws Exception {
        JavaType baseType = _typeFactory.constructType(String.class);
        TypeIdResolver idRes = new DummyTypeIdResolver(baseType, _typeFactory);
        TestableTypeDeserializer deser = new TestableTypeDeserializer(baseType, idRes, "type", false, null);

        DeserializationContext ctxt = createDeserializationContext();
        JsonParser p = _mapper.getFactory().createParser("\"test-value\"");
        p.nextToken();

        Object result = deser.deserializeWithNativeTypeId(p, ctxt, "known");
        assertEquals("test-value", result);
        p.close();
    }

    // Tests _deserializeWithNativeTypeId when typeId is null and no default implementation exists
    @Test(expected = JsonMappingException.class)
    public void testDeserializeWithNativeTypeId_nullTypeIdNoDefault_throwsException() throws Exception {
        JavaType baseType = _typeFactory.constructType(Object.class);
        TypeIdResolver idRes = new DummyTypeIdResolver(null, _typeFactory);
        TestableTypeDeserializer deser = new TestableTypeDeserializer(baseType, idRes, "type", false, null);

        DeserializationContext ctxt = createDeserializationContext();
        JsonParser p = _mapper.getFactory().createParser("\"test\"");
        p.nextToken();

        try {
            deser.deserializeWithNativeTypeId(p, ctxt, null);
        } finally {
            p.close();
        }
    }

    // Tests _deserializeWithNativeTypeId when typeId is null but default implementation is available
    @Test
    public void testDeserializeWithNativeTypeId_nullTypeIdWithDefault_usesDefaultDeserializer() throws Exception {
        JavaType baseType = _typeFactory.constructType(Object.class);
        JavaType defaultImpl = _typeFactory.constructType(String.class);
        TypeIdResolver idRes = new DummyTypeIdResolver(null, _typeFactory);
        TestableTypeDeserializer deser = new TestableTypeDeserializer(baseType, idRes, "type", false, defaultImpl);

        DeserializationContext ctxt = createDeserializationContext();
        JsonParser p = _mapper.getFactory().createParser("\"hello\"");
        p.nextToken();

        Object result = deser.deserializeWithNativeTypeId(p, ctxt, null);
        assertEquals("hello", result);
        p.close();
    }

    // Tests _handleUnknownTypeId with TypeIdResolverBase extra description formatting
    @Test(expected = JsonMappingException.class)
    public void testHandleUnknownTypeId_withTypeIdResolverBase_formatsExtraDescription() throws Exception {
        JavaType baseType = _typeFactory.constructType(Object.class);
        DummyTypeIdResolver idRes = new DummyTypeIdResolver(null, _typeFactory);
        TestableTypeDeserializer deser = new TestableTypeDeserializer(baseType, idRes, "type", false, null);

        DeserializationContext ctxt = createDeserializationContext();
        deser.handleUnknownTypeId(ctxt, "invalidId", idRes, baseType);
    }
}