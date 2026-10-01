package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonschema.SchemaAware;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
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

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // Tests 1-arg constructor and accessors
    @Test
    public void testConstructor_singleArg_initializesCorrectly() {
        Converter<String, Integer> converter = new StdConverter<String, Integer>() {
            @Override
            public Integer convert(String value) {
                return value == null ? 0 : value.length();
            }
        };

        StdDelegatingSerializer serializer = new StdDelegatingSerializer(converter);

        assertSame(converter, serializer.getConverter());
        assertNull(serializer.getDelegatee());
    }

    // Tests 2-arg constructor with target class
    @Test
    public void testConstructor_withClass_initializesCorrectly() {
        Converter<String, Integer> converter = new StdConverter<String, Integer>() {
            @Override
            public Integer convert(String value) {
                return value.length();
            }
        };

        StdDelegatingSerializer serializer = new StdDelegatingSerializer(String.class, converter);

        assertEquals(String.class, serializer.handledType());
        assertSame(converter, serializer.getConverter());
        assertNull(serializer.getDelegatee());
    }

    // Tests withDelegate creating new instance for base class
    @Test
    public void testWithDelegate_baseClass_returnsNewInstance() {
        Converter<Object, Object> converter = new StdConverter<Object, Object>() {
            @Override
            public Object convert(Object value) {
                return value;
            }
        };
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        JsonSerializer<Object> delSer = mapper.getSerializerProviderInstance().findValueSerializer(String.class);

        StdDelegatingSerializer serializer = new StdDelegatingSerializer(converter);
        StdDelegatingSerializer result = serializer.withDelegate(converter, type, delSer);

        assertNotNull(result);
        assertNotSame(serializer, result);
        assertSame(delSer, result.getDelegatee());
    }

    // Tests withDelegate throws IllegalStateException when subclass does not override it
    @Test(expected = IllegalStateException.class)
    public void testWithDelegate_subclassWithoutOverride_throwsIllegalStateException() {
        Converter<Object, Object> converter = new StdConverter<Object, Object>() {
            @Override
            public Object convert(Object value) {
                return value;
            }
        };

        StdDelegatingSerializer customSubclass = new StdDelegatingSerializer(converter) {
        };

        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        customSubclass.withDelegate(converter, type, null);
    }

    // Tests createContextual locates delegate serializer when not present
    @Test
    public void testCreateContextual_locatesDelegateSerializer() throws JsonMappingException {
        Converter<String, Integer> converter = new StdConverter<String, Integer>() {
            @Override
            public Integer convert(String value) {
                return Integer.parseInt(value);
            }
        };

        StdDelegatingSerializer serializer = new StdDelegatingSerializer(String.class, converter);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        JsonSerializer<?> contextual = serializer.createContextual(provider, null);

        assertNotNull(contextual);
        assertTrue(contextual instanceof StdDelegatingSerializer);
        assertNotNull(((StdDelegatingSerializer) contextual).getDelegatee());
    }

    // Tests createContextual returns this when delegate is unchanged
    @Test
    public void testCreateContextual_alreadyResolved_returnsSameInstance() throws JsonMappingException {
        JavaType type = TypeFactory.defaultInstance().constructType(Integer.class);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonSerializer<Object> delSer = provider.findValueSerializer(Integer.class);

        Converter<Object, Object> converter = new StdConverter<Object, Object>() {
            @Override
            public Object convert(Object value) {
                return value;
            }
        };

        StdDelegatingSerializer serializer = new StdDelegatingSerializer(converter, type, delSer);
        JsonSerializer<?> contextual = serializer.createContextual(provider, null);

        assertSame(serializer, contextual);
    }

    // Tests resolve forwards to delegate serializer when it implements ResolvableSerializer
    @Test
    public void testResolve_resolvesResolvableDelegate() throws JsonMappingException {
        final boolean[] resolved = new boolean[]{false};
        class ResolvableMockSerializer extends JsonSerializer<Object> implements ResolvableSerializer {
            @Override
            public void resolve(SerializerProvider provider) {
                resolved[0] = true;
            }
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) {}
        }

        ResolvableMockSerializer mockDel = new ResolvableMockSerializer();
        Converter<Object, Object> converter = new StdConverter<Object, Object>() {
            @Override
            public Object convert(Object value) {
                return value;
            }
        };

        StdDelegatingSerializer serializer = new StdDelegatingSerializer(converter, null, mockDel);
        serializer.resolve(mapper.getSerializerProviderInstance());

        assertTrue(resolved[0]);
    }

    // Tests resolve with null delegate does not throw exception
    @Test
    public void testResolve_nullDelegate_doesNotThrow() throws JsonMappingException {
        Converter<Object, Object> converter = new StdConverter<Object, Object>() {
            @Override
            public Object convert(Object value) {
                return value;
            }
        };

        StdDelegatingSerializer serializer = new StdDelegatingSerializer(converter);
        serializer.resolve(mapper.getSerializerProviderInstance());
    }

    // Tests serialization when converted value is valid non-null
    @Test
    public void testSerialize_validValue_delegatesCorrectly() throws IOException {
        Converter<Integer, String> converter = new StdConverter<Integer, String>() {
            @Override
            public String convert(Integer value) {
                return "VAL:" + value;
            }
        };

        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonSerializer<Object> delSer = provider.findValueSerializer(String.class);

        @SuppressWarnings("unchecked")
        Converter<Object, ?> objConverter = (Converter<Object, ?>) (Converter<?, ?>) converter;
        StdDelegatingSerializer serializer = new StdDelegatingSerializer(objConverter, type, delSer);

        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);

        serializer.serialize(123, gen, provider);
        gen.flush();

        assertEquals("\"VAL:123\"", sw.toString());
    }

    // Tests serialization when converted value is null
    @Test
    public void testSerialize_convertedNull_serializesNull() throws IOException {
        Converter<Integer, String> converter = new StdConverter<Integer, String>() {
            @Override
            public String convert(Integer value) {
                return null;
            }
        };

        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonSerializer<Object> delSer = provider.findValueSerializer(String.class);

        @SuppressWarnings("unchecked")
        Converter<Object, ?> objConverter = (Converter<Object, ?>) (Converter<?, ?>) converter;
        StdDelegatingSerializer serializer = new StdDelegatingSerializer(objConverter, type, delSer);

        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);

        serializer.serialize(123, gen, provider);
        gen.flush();

        assertEquals("null", sw.toString());
    }

    // Tests isEmpty deprecated and 2-arg methods
    @Test
    public void testIsEmpty_delegatesToUnderlyingSerializer() {
        Converter<String, String> converter = new StdConverter<String, String>() {
            @Override
            public String convert(String value) {
                return value;
            }
        };

        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonSerializer<Object> delSer = new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) {}

            @Override
            @Deprecated
            public boolean isEmpty(Object value) {
                return value == null || "".equals(value);
            }

            @Override
            public boolean isEmpty(SerializerProvider prov, Object value) {
                return value == null || "".equals(value);
            }
        };

        @SuppressWarnings("unchecked")
        Converter<Object, ?> objConverter = (Converter<Object, ?>) (Converter<?, ?>) converter;
        StdDelegatingSerializer serializer = new StdDelegatingSerializer(objConverter, type, delSer);

        assertTrue(serializer.isEmpty(""));
        assertFalse(serializer.isEmpty("abc"));
        assertTrue(serializer.isEmpty(provider, ""));
        assertFalse(serializer.isEmpty(provider, "abc"));
    }

    // Tests getSchema with SchemaAware delegate
    @Test
    public void testGetSchema_withSchemaAwareDelegate_returnsSchema() throws JsonMappingException {
        Converter<String, String> converter = new StdConverter<String, String>() {
            @Override
            public String convert(String value) {
                return value;
            }
        };

        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonSerializer<Object> delSer = provider.findValueSerializer(String.class);

        @SuppressWarnings("unchecked")
        Converter<Object, ?> objConverter = (Converter<Object, ?>) (Converter<?, ?>) converter;
        StdDelegatingSerializer serializer = new StdDelegatingSerializer(objConverter,
                TypeFactory.defaultInstance().constructType(String.class), delSer);

        JsonNode schema1 = serializer.getSchema(provider, String.class);
        assertNotNull(schema1);

        JsonNode schema2 = serializer.getSchema(provider, String.class, true);
        assertNotNull(schema2);
    }

    // Tests getSchema when delegate is not SchemaAware
    @Test
    public void testGetSchema_nonSchemaAwareDelegate_returnsDefaultSchema() throws JsonMappingException {
        Converter<String, String> converter = new StdConverter<String, String>() {
            @Override
            public String convert(String value) {
                return value;
            }
        };

        JsonSerializer<Object> nonSchemaAwareSer = new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) {}
        };

        @SuppressWarnings("unchecked")
        Converter<Object, ?> objConverter = (Converter<Object, ?>) (Converter<?, ?>) converter;
        StdDelegatingSerializer serializer = new StdDelegatingSerializer(objConverter,
                TypeFactory.defaultInstance().constructType(String.class), nonSchemaAwareSer);

        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonNode schema = serializer.getSchema(provider, String.class);
        assertNotNull(schema);
        assertEquals("string", schema.get("type").asText());
    }

    // Tests acceptJsonFormatVisitor delegates to delegate serializer
    @Test
    public void testAcceptJsonFormatVisitor_delegatesToDelegateSerializer() throws JsonMappingException {
        final boolean[] visited = new boolean[]{false};
        JsonSerializer<Object> mockSer = new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) {}

            @Override
            public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) {
                visited[0] = true;
            }
        };

        Converter<Object, Object> converter = new StdConverter<Object, Object>() {
            @Override
            public Object convert(Object value) {
                return value;
            }
        };

        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        StdDelegatingSerializer serializer = new StdDelegatingSerializer(converter, type, mockSer);

        serializer.acceptJsonFormatVisitor(null, type);
        assertTrue(visited[0]);
    }
}