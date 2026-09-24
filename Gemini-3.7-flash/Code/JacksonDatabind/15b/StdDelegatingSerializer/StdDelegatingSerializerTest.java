package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonschema.SchemaAware;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.ResolvableSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;

import static org.junit.Assert.*;

public class StdDelegatingSerializerTest {

    private ObjectMapper mapper;
    private DefaultSerializerProvider serializerProvider;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        serializerProvider = (DefaultSerializerProvider) mapper.getSerializerProviderInstance();
    }

    private static class StringToIntegerConverter extends StdConverter<String, Integer> {
        @Override
        public Integer convert(String value) {
            return value == null ? null : Integer.valueOf(value);
        }
    }

    private static class NullReturningConverter extends StdConverter<String, Object> {
        @Override
        public Object convert(String value) {
            return null;
        }
    }

    private static class DummySubclassSerializer extends StdDelegatingSerializer {
        public DummySubclassSerializer(Converter<?, ?> converter) {
            super(converter);
        }
    }

    private static class MockResolvableSchemaAwareSerializer extends JsonSerializer<Object>
            implements ResolvableSerializer, SchemaAware {
        boolean resolved = false;
        boolean visited = false;

        @Override
        public void resolve(SerializerProvider provider) {
            resolved = true;
        }

        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString("mock:" + value);
        }

        @Override
        public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider serializers,
                                      TypeSerializer typeSer) throws IOException {
            gen.writeString("mockWithType:" + value);
        }

        @Override
        public boolean isEmpty(SerializerProvider provider, Object value) {
            return value == null || "empty".equals(value);
        }

        @Override
        @SuppressWarnings("deprecation")
        public boolean isEmpty(Object value) {
            return value == null || "empty".equals(value);
        }

        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
            return provider.getNodeFactory().textNode("mockSchema");
        }

        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint, boolean isOptional) {
            return provider.getNodeFactory().textNode("mockSchemaOptional:" + isOptional);
        }

        @Override
        public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) {
            visited = true;
        }
    }

    // Tests constructor with converter only
    @Test
    public void testConstructor_withConverter_initializesCorrectly() {
        Converter<String, Integer> conv = new StringToIntegerConverter();
        StdDelegatingSerializer ser = new StdDelegatingSerializer(conv);

        assertSame(conv, ser.getConverter());
        assertNull(ser.getDelegatee());
    }

    // Tests constructor with class and converter
    @Test
    public void testConstructor_withClassAndConverter_initializesCorrectly() {
        Converter<String, Integer> conv = new StringToIntegerConverter();
        StdDelegatingSerializer ser = new StdDelegatingSerializer(String.class, conv);

        assertSame(conv, ser.getConverter());
        assertNull(ser.getDelegatee());
        assertEquals(String.class, ser.handledType());
    }

    // Tests constructor with all parameters
    @Test
    public void testConstructor_withAllParams_initializesCorrectly() {
        Converter<Object, Object> conv = new StdConverter<Object, Object>() {
            @Override
            public Object convert(Object value) {
                return value;
            }
        };
        JavaType type = TypeFactory.defaultInstance().constructType(Integer.class);
        JsonSerializer<?> dummySerializer = new MockResolvableSchemaAwareSerializer();

        StdDelegatingSerializer ser = new StdDelegatingSerializer(conv, type, dummySerializer);

        assertSame(conv, ser.getConverter());
        assertSame(dummySerializer, ser.getDelegatee());
        assertEquals(Integer.class, ser.handledType());
    }

    // Tests withDelegate method on base class returns new instance
    @Test
    public void testWithDelegate_sameClass_returnsNewInstance() {
        Converter<Object, Object> conv = new StdConverter<Object, Object>() {
            @Override
            public Object convert(Object value) {
                return value;
            }
        };
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        JsonSerializer<?> delegateSer = new MockResolvableSchemaAwareSerializer();

        StdDelegatingSerializer ser = new StdDelegatingSerializer(conv);
        StdDelegatingSerializer result = ser.withDelegate(conv, type, delegateSer);

        assertNotNull(result);
        assertNotSame(ser, result);
        assertSame(delegateSer, result.getDelegatee());
    }

    // Tests withDelegate method throws IllegalStateException on subclass if not overridden
    @Test(expected = IllegalStateException.class)
    public void testWithDelegate_subclassWithoutOverride_throwsIllegalStateException() {
        Converter<String, Integer> conv = new StringToIntegerConverter();
        DummySubclassSerializer subclassSer = new DummySubclassSerializer(conv);
        subclassSer.withDelegate(null, null, null);
    }

    // Tests convertValue delegates to the underlying converter
    @Test
    public void testConvertValue_validValue_returnsConvertedResult() {
        Converter<String, Integer> conv = new StringToIntegerConverter();
        StdDelegatingSerializer ser = new StdDelegatingSerializer(conv);

        Object result = ser.convertValue("123");
        assertEquals(Integer.valueOf(123), result);
    }

    // Tests createContextual resolves serializer when delegate serializer is null
    @Test
    public void testCreateContextual_unresolvedDelegate_resolvesDelegateSerializer() throws JsonMappingException {
        Converter<String, Integer> conv = new StringToIntegerConverter();
        StdDelegatingSerializer ser = new StdDelegatingSerializer(conv);

        JsonSerializer<?> contextual = ser.createContextual(serializerProvider, null);

        assertNotNull(contextual);
        assertTrue(contextual instanceof StdDelegatingSerializer);
        StdDelegatingSerializer delegating = (StdDelegatingSerializer) contextual;
        assertNotNull(delegating.getDelegatee());
    }

    // Tests createContextual returns this when delegate serializer is already resolved and unchanged
    @Test
    public void testCreateContextual_alreadyResolvedSameSerializer_returnsThis() throws JsonMappingException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        JsonSerializer<Object> nonContextualSer = new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString(String.valueOf(value));
            }
        };
        Converter<Object, Object> conv = new StdConverter<Object, Object>() {
            @Override
            public Object convert(Object value) {
                return value;
            }
        };

        StdDelegatingSerializer ser = new StdDelegatingSerializer(conv, type, nonContextualSer);
        JsonSerializer<?> contextual = ser.createContextual(serializerProvider, null);

        assertSame(ser, contextual);
    }

    // Tests resolve method when delegate serializer implements ResolvableSerializer
    @Test
    public void testResolve_resolvableDelegate_callsResolveOnDelegate() throws JsonMappingException {
        MockResolvableSchemaAwareSerializer mockSer = new MockResolvableSchemaAwareSerializer();
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        Converter<Object, Object> conv = new StdConverter<Object, Object>() {
            @Override
            public Object convert(Object value) {
                return value;
            }
        };

        StdDelegatingSerializer ser = new StdDelegatingSerializer(conv, type, mockSer);
        ser.resolve(serializerProvider);

        assertTrue(mockSer.resolved);
    }

    // Tests resolve method when delegate serializer is null or not resolvable
    @Test
    public void testResolve_nonResolvableOrNullDelegate_doesNotThrow() throws JsonMappingException {
        Converter<String, Integer> conv = new StringToIntegerConverter();
        StdDelegatingSerializer ser = new StdDelegatingSerializer(conv);
        ser.resolve(serializerProvider); // delegate is null, should be no-op
    }

    // Tests serialize method when converted value is null
    @Test
    public void testSerialize_convertedValueIsNull_writesNull() throws IOException {
        Converter<String, Object> conv = new NullReturningConverter();
        JavaType type = TypeFactory.defaultInstance().constructType(Object.class);
        MockResolvableSchemaAwareSerializer mockSer = new MockResolvableSchemaAwareSerializer();
        StdDelegatingSerializer ser = new StdDelegatingSerializer(conv, type, mockSer);

        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);

        ser.serialize("someValue", gen, serializerProvider);
        gen.flush();

        assertEquals("null", sw.toString());
    }

    // Tests serialize method when converted value is non-null
    @Test
    public void testSerialize_convertedValueIsNonNull_delegatesToSerializer() throws IOException {
        Converter<String, Integer> conv = new StringToIntegerConverter();
        JavaType type = TypeFactory.defaultInstance().constructType(Integer.class);
        MockResolvableSchemaAwareSerializer mockSer = new MockResolvableSchemaAwareSerializer();
        StdDelegatingSerializer ser = new StdDelegatingSerializer((Converter) conv, type, mockSer);

        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);

        ser.serialize("456", gen, serializerProvider);
        gen.flush();

        assertEquals("\"mock:456\"", sw.toString());
    }

    // Tests serializeWithType method delegates to delegateSerializer
    @Test
    public void testSerializeWithType_delegatesToDelegateSerializer() throws IOException {
        Converter<String, Integer> conv = new StringToIntegerConverter();
        JavaType type = TypeFactory.defaultInstance().constructType(Integer.class);
        MockResolvableSchemaAwareSerializer mockSer = new MockResolvableSchemaAwareSerializer();
        StdDelegatingSerializer ser = new StdDelegatingSerializer((Converter) conv, type, mockSer);

        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);

        ser.serializeWithType("789", gen, serializerProvider, null);
        gen.flush();

        assertEquals("\"mockWithType:789\"", sw.toString());
    }

    // Tests isEmpty with SerializerProvider
    @Test
    public void testIsEmpty_withProvider_evaluatesConvertedValue() {
        Converter<String, String> conv = new StdConverter<String, String>() {
            @Override
            public String convert(String value) {
                return value;
            }
        };
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        MockResolvableSchemaAwareSerializer mockSer = new MockResolvableSchemaAwareSerializer();
        StdDelegatingSerializer ser = new StdDelegatingSerializer(conv, type, mockSer);

        assertTrue(ser.isEmpty(serializerProvider, "empty"));
        assertFalse(ser.isEmpty(serializerProvider, "notEmpty"));
    }

    // Tests deprecated isEmpty without SerializerProvider
    @Test
    @SuppressWarnings("deprecation")
    public void testIsEmpty_withoutProvider_evaluatesConvertedValue() {
        Converter<String, String> conv = new StdConverter<String, String>() {
            @Override
            public String convert(String value) {
                return value;
            }
        };
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        MockResolvableSchemaAwareSerializer mockSer = new MockResolvableSchemaAwareSerializer();
        StdDelegatingSerializer ser = new StdDelegatingSerializer(conv, type, mockSer);

        assertTrue(ser.isEmpty("empty"));
        assertFalse(ser.isEmpty("notEmpty"));
    }

    // Tests getSchema when delegate is SchemaAware
    @Test
    public void testGetSchema_schemaAwareDelegate_returnsDelegateSchema() throws JsonMappingException {
        Converter<String, String> conv = new StdConverter<String, String>() {
            @Override
            public String convert(String value) {
                return value;
            }
        };
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        MockResolvableSchemaAwareSerializer mockSer = new MockResolvableSchemaAwareSerializer();
        StdDelegatingSerializer ser = new StdDelegatingSerializer(conv, type, mockSer);

        JsonNode schema = ser.getSchema(serializerProvider, String.class);
        assertNotNull(schema);
        assertEquals("mockSchema", schema.textValue());

        JsonNode schemaOptional = ser.getSchema(serializerProvider, String.class, true);
        assertNotNull(schemaOptional);
        assertEquals("mockSchemaOptional:true", schemaOptional.textValue());
    }

    // Tests getSchema fallback when delegate is not SchemaAware
    @Test
    public void testGetSchema_nonSchemaAwareDelegate_returnsDefaultSchema() throws JsonMappingException {
        Converter<String, String> conv = new StdConverter<String, String>() {
            @Override
            public String convert(String value) {
                return value;
            }
        };
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        JsonSerializer<Object> simpleSer = new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) {
            }
        };
        StdDelegatingSerializer ser = new StdDelegatingSerializer(conv, type, simpleSer);

        JsonNode schema = ser.getSchema(serializerProvider, String.class);
        assertNotNull(schema);
        assertEquals("string", schema.get("type").textValue());

        JsonNode schemaOptional = ser.getSchema(serializerProvider, String.class, true);
        assertNotNull(schemaOptional);
        assertEquals("string", schemaOptional.get("type").textValue());
    }

    // Tests acceptJsonFormatVisitor delegates to delegateSerializer
    @Test
    public void testAcceptJsonFormatVisitor_delegatesToUnderlyingSerializer() throws JsonMappingException {
        Converter<String, String> conv = new StdConverter<String, String>() {
            @Override
            public String convert(String value) {
                return value;
            }
        };
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        MockResolvableSchemaAwareSerializer mockSer = new MockResolvableSchemaAwareSerializer();
        StdDelegatingSerializer ser = new StdDelegatingSerializer(conv, type, mockSer);

        ser.acceptJsonFormatVisitor(null, type);
        assertTrue(mockSer.visited);
    }

    // Tests full serialization integration with ObjectMapper
    @Test
    public void testIntegration_withObjectMapper() throws Exception {
        StdDelegatingSerializer ser = new StdDelegatingSerializer(String.class, new StringToIntegerConverter());
        com.fasterxml.jackson.databind.module.SimpleModule module =
                new com.fasterxml.jackson.databind.module.SimpleModule();
        module.addSerializer(String.class, ser);
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.registerModule(module);

        String json = customMapper.writeValueAsString("12345");
        assertEquals("12345", json);
    }
}