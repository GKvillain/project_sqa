package com.fasterxml.jackson.databind.jsontype.impl;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class TypeDeserializerBaseTest {

    private ObjectMapper _mapper;
    private DeserializationContext _context;
    private TypeFactory _typeFactory;

    private static class DummyTypeDeserializer extends TypeDeserializerBase {
        private static final long serialVersionUID = 1L;

        public DummyTypeDeserializer(JavaType baseType, TypeIdResolver idRes,
                String typePropertyName, boolean typeIdVisible, JavaType defaultImpl) {
            super(baseType, idRes, typePropertyName, typeIdVisible, defaultImpl);
        }

        public DummyTypeDeserializer(DummyTypeDeserializer src, BeanProperty prop) {
            super(src, prop);
        }

        @Override
        public TypeDeserializer forProperty(BeanProperty prop) {
            return new DummyTypeDeserializer(this, prop);
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

        public Object deserializeWithNativeTypeId(JsonParser jp, DeserializationContext ctxt, Object typeId) throws IOException {
            return _deserializeWithNativeTypeId(jp, ctxt, typeId);
        }

        public Object deserializeWithNativeTypeId(JsonParser jp, DeserializationContext ctxt) throws IOException {
            return _deserializeWithNativeTypeId(jp, ctxt);
        }

        public JavaType handleUnknownTypeId(DeserializationContext ctxt, String typeId) throws IOException {
            return _handleUnknownTypeId(ctxt, typeId);
        }

        public JavaType handleMissingTypeId(DeserializationContext ctxt, String extraDesc) throws IOException {
            return _handleMissingTypeId(ctxt, extraDesc);
        }
    }

    private static class DummyTypeIdResolver implements TypeIdResolver {
        private final JavaType _baseType;
        private final String _knownId;
        private final JavaType _knownType;

        public DummyTypeIdResolver(JavaType baseType, String knownId, JavaType knownType) {
            _baseType = baseType;
            _knownId = knownId;
            _knownType = knownType;
        }

        @Override
        public void init(JavaType baseType) { }

        @Override
        public String idFromValue(Object value) { return null; }

        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) { return null; }

        @Override
        public String idFromBaseType() { return null; }

        @Override
        public JavaType typeFromId(DeserializationContext context, String id) {
            if (_knownId != null && _knownId.equals(id)) {
                return _knownType;
            }
            return null;
        }

        @Override
        public String getDescForKnownTypeIds() {
            return (_knownId != null) ? "[" + _knownId + "]" : null;
        }

        @Override
        public JsonTypeInfo.Id getMechanism() {
            return JsonTypeInfo.Id.CUSTOM;
        }
    }

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _typeFactory = _mapper.getTypeFactory();
        _context = _mapper.getDeserializationContext();
        if (_context == null || _context.getConfig() == null) {
            DeserializationConfig config = _mapper.getDeserializationConfig();
            _context = _mapper.createDeserializationContext(null, config);
        }
    }

    // Tests accessor methods: baseTypeName, getPropertyName, getTypeIdResolver, getDefaultImpl, baseType
    @Test
    public void testAccessors_validInputs_returnExpectedValues() {
        JavaType baseType = _typeFactory.constructType(Number.class);
        JavaType defaultImpl = _typeFactory.constructType(Integer.class);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver(baseType, "int", defaultImpl);

        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, resolver, "typeProp", true, defaultImpl);

        assertEquals(Number.class.getName(), deser.baseTypeName());
        assertEquals("typeProp", deser.getPropertyName());
        assertSame(resolver, deser.getTypeIdResolver());
        assertEquals(Integer.class, deser.getDefaultImpl());
        assertSame(baseType, deser.baseType());
        assertEquals(JsonTypeInfo.As.PROPERTY, deser.getTypeInclusion());
    }

    // Tests null typePropertyName defaults to empty string via ClassUtil.nonNullString
    @Test
    public void testConstructor_nullPropertyName_setsEmptyString() {
        JavaType baseType = _typeFactory.constructType(Object.class);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver(baseType, null, null);

        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, resolver, null, false, null);

        assertEquals("", deser.getPropertyName());
        assertNull(deser.getDefaultImpl());
    }

    // Tests toString format containing class name, base-type and id-resolver
    @Test
    public void testToString_validObject_containsBaseTypeAndResolver() {
        JavaType baseType = _typeFactory.constructType(String.class);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver(baseType, null, null);

        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, resolver, "type", false, null);
        String result = deser.toString();

        assertTrue(result.contains("DummyTypeDeserializer"));
        assertTrue(result.contains("base-type:"));
        assertTrue(result.contains("id-resolver:"));
    }

    // Tests copy constructor and forProperty behavior
    @Test
    public void testForProperty_validProperty_createsCopy() {
        JavaType baseType = _typeFactory.constructType(Object.class);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver(baseType, null, null);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, resolver, "type", false, null);

        TypeDeserializer copy = deser.forProperty(null);

        assertNotNull(copy);
        assertNotSame(deser, copy);
        assertEquals(deser.getPropertyName(), copy.getPropertyName());
    }

    // Tests _findDefaultImplDeserializer with null defaultImpl when FAIL_ON_INVALID_SUBTYPE is disabled
    @Test
    public void testFindDefaultImplDeserializer_nullDefaultImplAndFailOnInvalidSubtypeDisabled_returnsNullifyingDeser() throws Exception {
        JavaType baseType = _typeFactory.constructType(Object.class);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver(baseType, null, null);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, resolver, "type", false, null);

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        if (ctxt.getConfig() == null) {
            ctxt = mapper.createDeserializationContext(null, mapper.getDeserializationConfig());
        }

        JsonDeserializer<Object> result = deser.findDefaultImplDeserializer(ctxt);
        assertSame(NullifyingDeserializer.instance, result);
    }

    // Tests _findDefaultImplDeserializer with null defaultImpl when FAIL_ON_INVALID_SUBTYPE is enabled
    @Test
    public void testFindDefaultImplDeserializer_nullDefaultImplAndFailOnInvalidSubtypeEnabled_returnsNull() throws Exception {
        JavaType baseType = _typeFactory.constructType(Object.class);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver(baseType, null, null);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, resolver, "type", false, null);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        if (ctxt.getConfig() == null) {
            ctxt = mapper.createDeserializationContext(null, mapper.getDeserializationConfig());
        }

        JsonDeserializer<Object> result = deser.findDefaultImplDeserializer(ctxt);
        assertNull(result);
    }

    // Tests _findDefaultImplDeserializer with bogus defaultImpl class (Void.class)
    @Test
    public void testFindDefaultImplDeserializer_bogusDefaultImpl_returnsNullifyingDeser() throws Exception {
        JavaType baseType = _typeFactory.constructType(Object.class);
        JavaType defaultImpl = _typeFactory.constructType(Void.class);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver(baseType, null, null);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, resolver, "type", false, defaultImpl);

        JsonDeserializer<Object> result = deser.findDefaultImplDeserializer(_context);
        assertSame(NullifyingDeserializer.instance, result);
    }

    // Tests _findDefaultImplDeserializer caching
    @Test
    public void testFindDefaultImplDeserializer_validDefaultImpl_returnsAndCachesDeserializer() throws Exception {
        JavaType baseType = _typeFactory.constructType(Number.class);
        JavaType defaultImpl = _typeFactory.constructType(Integer.class);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver(baseType, null, null);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, resolver, "type", false, defaultImpl);

        JsonDeserializer<Object> deser1 = deser.findDefaultImplDeserializer(_context);
        JsonDeserializer<Object> deser2 = deser.findDefaultImplDeserializer(_context);

        assertNotNull(deser1);
        assertSame(deser1, deser2);
    }

    // Tests _findDeserializer resolving known type id and caching it
    @Test
    public void testFindDeserializer_knownTypeId_resolvesAndCaches() throws Exception {
        JavaType baseType = _typeFactory.constructType(Number.class);
        JavaType subType = _typeFactory.constructType(Integer.class);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver(baseType, "int", subType);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, resolver, "type", false, null);

        JsonDeserializer<Object> deser1 = deser.findDeserializer(_context, "int");
        JsonDeserializer<Object> deser2 = deser.findDeserializer(_context, "int");

        assertNotNull(deser1);
        assertSame(deser1, deser2);
    }

    // Tests _findDeserializer fallback to defaultImpl when type id is unknown
    @Test
    public void testFindDeserializer_unknownTypeIdWithDefaultImpl_returnsDefaultImplDeserializer() throws Exception {
        JavaType baseType = _typeFactory.constructType(Number.class);
        JavaType defaultImpl = _typeFactory.constructType(Long.class);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver(baseType, "int", null);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, resolver, "type", false, defaultImpl);

        JsonDeserializer<Object> result = deser.findDeserializer(_context, "unknown");

        assertNotNull(result);
        assertEquals(Long.class, deser.getDefaultImpl());
    }

    // Tests _findDeserializer when type id is unknown and handleUnknownTypeId returns null
    @Test
    public void testFindDeserializer_unknownTypeIdAndNoDefaultImpl_returnsNull() throws Exception {
        JavaType baseType = _typeFactory.constructType(Number.class);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver(baseType, null, null);

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        if (ctxt.getConfig() == null) {
            ctxt = mapper.createDeserializationContext(null, mapper.getDeserializationConfig());
        }

        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, resolver, "type", false, null);

        JsonDeserializer<Object> result = deser.findDeserializer(ctxt, "unknown_type");
        // With FAIL_ON_INVALID_SUBTYPE disabled, default impl returns NullifyingDeserializer
        assertSame(NullifyingDeserializer.instance, result);
    }

    // Tests _deserializeWithNativeTypeId when native typeId is null and default impl fails
    @Test
    public void testDeserializeWithNativeTypeId_nullTypeIdNoDefaultImpl_reportsInputMismatch() throws Exception {
        JavaType baseType = _typeFactory.constructType(Number.class);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver(baseType, null, null);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        if (ctxt.getConfig() == null) {
            ctxt = mapper.createDeserializationContext(null, mapper.getDeserializationConfig());
        }

        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, resolver, "type", false, null);
        JsonParser parser = mapper.getFactory().createParser("123");

        try {
            deser.deserializeWithNativeTypeId(parser, ctxt, null);
            fail("Expected exception when no native type id found");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("No (native) type id found"));
        } finally {
            parser.close();
        }
    }

    // Tests _deserializeWithNativeTypeId with valid String typeId
    @Test
    public void testDeserializeWithNativeTypeId_validTypeId_deserializesValue() throws Exception {
        JavaType baseType = _typeFactory.constructType(Number.class);
        JavaType subType = _typeFactory.constructType(Integer.class);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver(baseType, "int", subType);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, resolver, "type", false, null);

        JsonParser parser = _mapper.getFactory().createParser("42");
        parser.nextToken();

        Object result = deser.deserializeWithNativeTypeId(parser, _context, "int");
        assertEquals(Integer.valueOf(42), result);
        parser.close();
    }

    // Tests _handleUnknownTypeId format when known type ids are available
    @Test
    public void testHandleUnknownTypeId_withKnownIds_reportsKnownIdsInException() throws Exception {
        JavaType baseType = _typeFactory.constructType(Number.class);
        JavaType subType = _typeFactory.constructType(Integer.class);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver(baseType, "int", subType);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, resolver, "type", false, null);

        try {
            deser.handleUnknownTypeId(_context, "unknownId");
            fail("Expected handleUnknownTypeId to fail by default");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("known type ids = [int]"));
        }
    }

    // Tests _handleUnknownTypeId format when known type ids are not statically known
    @Test
    public void testHandleUnknownTypeId_withoutKnownIds_reportsNotStaticallyKnown() throws Exception {
        JavaType baseType = _typeFactory.constructType(Number.class);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver(baseType, null, null);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, resolver, "type", false, null);

        try {
            deser.handleUnknownTypeId(_context, "unknownId");
            fail("Expected handleUnknownTypeId to fail by default");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("type ids are not statically known"));
        }
    }

    // Tests _handleMissingTypeId propagates to context
    @Test
    public void testHandleMissingTypeId_callsContext() throws Exception {
        JavaType baseType = _typeFactory.constructType(Number.class);
        DummyTypeIdResolver resolver = new DummyTypeIdResolver(baseType, null, null);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, resolver, "type", false, null);

        try {
            deser.handleMissingTypeId(_context, "missing type info");
            fail("Expected handleMissingTypeId to throw exception");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("missing type info") || e.getMessage().contains("missing"));
        }
    }
}