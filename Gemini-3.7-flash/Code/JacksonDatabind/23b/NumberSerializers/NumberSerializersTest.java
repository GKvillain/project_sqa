package com.fasterxml.jackson.databind.ser.std;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNumberFormatVisitor;

public class NumberSerializersTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonFactory jsonFactory = new JsonFactory();

    static class FormattedNumbers {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int intVal = 123;

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public long longVal = 456L;

        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public double doubleVal = 7.89;

        public short shortVal = 12;
    }

    // Tests constructor instantiation
    @Test
    public void testConstructor_instantiation_success() {
        NumberSerializers serializers = new NumberSerializers();
        assertNotNull(serializers);
    }

    // Tests addAll populates map with standard numeric serializers
    @Test
    public void testAddAll_validMap_populatesAllPrimitiveAndWrapperTypes() {
        Map<String, JsonSerializer<?>> map = new HashMap<String, JsonSerializer<?>>();
        NumberSerializers.addAll(map);

        assertTrue(map.containsKey(Integer.class.getName()));
        assertTrue(map.containsKey(Integer.TYPE.getName()));
        assertTrue(map.containsKey(Long.class.getName()));
        assertTrue(map.containsKey(Long.TYPE.getName()));
        assertTrue(map.containsKey(Byte.class.getName()));
        assertTrue(map.containsKey(Byte.TYPE.getName()));
        assertTrue(map.containsKey(Short.class.getName()));
        assertTrue(map.containsKey(Short.TYPE.getName()));
        assertTrue(map.containsKey(Float.class.getName()));
        assertTrue(map.containsKey(Float.TYPE.getName()));
        assertTrue(map.containsKey(Double.class.getName()));
        assertTrue(map.containsKey(Double.TYPE.getName()));

        assertTrue(map.get(Integer.class.getName()) instanceof NumberSerializers.IntegerSerializer);
        assertTrue(map.get(Long.class.getName()) instanceof NumberSerializers.LongSerializer);
        assertTrue(map.get(Byte.class.getName()) instanceof NumberSerializers.IntLikeSerializer);
        assertTrue(map.get(Short.class.getName()) instanceof NumberSerializers.ShortSerializer);
        assertTrue(map.get(Float.class.getName()) instanceof NumberSerializers.FloatSerializer);
        assertTrue(map.get(Double.class.getName()) instanceof NumberSerializers.DoubleSerializer);
    }

    // Tests ShortSerializer serialization and schema
    @Test
    public void testShortSerializer_serializeAndSchema_returnsCorrectOutput() throws Exception {
        NumberSerializers.ShortSerializer serializer = new NumberSerializers.ShortSerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        serializer.serialize((short) 42, gen, provider);
        gen.flush();
        assertEquals("42", sw.toString());

        JsonNode schemaNode = serializer.getSchema(provider, Short.class);
        assertNotNull(schemaNode);
        assertEquals("number", schemaNode.get("type").asText());
    }

    // Tests IntegerSerializer serialization, serializeWithType, and schema
    @Test
    public void testIntegerSerializer_serializeAndSchema_returnsCorrectOutput() throws Exception {
        NumberSerializers.IntegerSerializer serializer = new NumberSerializers.IntegerSerializer(Integer.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        serializer.serialize(100, gen, provider);
        gen.flush();
        assertEquals("100", sw.toString());

        sw = new StringWriter();
        gen = jsonFactory.createGenerator(sw);
        serializer.serializeWithType(200, gen, provider, null);
        gen.flush();
        assertEquals("200", sw.toString());

        JsonNode schemaNode = serializer.getSchema(provider, Integer.class);
        assertNotNull(schemaNode);
        assertEquals("integer", schemaNode.get("type").asText());
    }

    // Tests IntLikeSerializer serialization and schema
    @Test
    public void testIntLikeSerializer_serializeAndSchema_returnsCorrectOutput() throws Exception {
        NumberSerializers.IntLikeSerializer serializer = new NumberSerializers.IntLikeSerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        serializer.serialize((byte) 8, gen, provider);
        gen.flush();
        assertEquals("8", sw.toString());

        JsonNode schemaNode = serializer.getSchema(provider, Byte.class);
        assertNotNull(schemaNode);
        assertEquals("integer", schemaNode.get("type").asText());
    }

    // Tests LongSerializer serialization and schema
    @Test
    public void testLongSerializer_serializeAndSchema_returnsCorrectOutput() throws Exception {
        NumberSerializers.LongSerializer serializer = new NumberSerializers.LongSerializer(Long.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        serializer.serialize(9876543210L, gen, provider);
        gen.flush();
        assertEquals("9876543210", sw.toString());

        JsonNode schemaNode = serializer.getSchema(provider, Long.class);
        assertNotNull(schemaNode);
        assertEquals("number", schemaNode.get("type").asText());
    }

    // Tests FloatSerializer serialization and schema
    @Test
    public void testFloatSerializer_serializeAndSchema_returnsCorrectOutput() throws Exception {
        NumberSerializers.FloatSerializer serializer = new NumberSerializers.FloatSerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        serializer.serialize(1.25f, gen, provider);
        gen.flush();
        assertEquals("1.25", sw.toString());

        JsonNode schemaNode = serializer.getSchema(provider, Float.class);
        assertNotNull(schemaNode);
        assertEquals("number", schemaNode.get("type").asText());
    }

    // Tests DoubleSerializer serialization, serializeWithType, and schema
    @Test
    public void testDoubleSerializer_serializeAndSchema_returnsCorrectOutput() throws Exception {
        NumberSerializers.DoubleSerializer serializer = new NumberSerializers.DoubleSerializer(Double.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        serializer.serialize(3.1415, gen, provider);
        gen.flush();
        assertEquals("3.1415", sw.toString());

        sw = new StringWriter();
        gen = jsonFactory.createGenerator(sw);
        serializer.serializeWithType(2.71828, gen, provider, null);
        gen.flush();
        assertEquals("2.71828", sw.toString());

        JsonNode schemaNode = serializer.getSchema(provider, Double.class);
        assertNotNull(schemaNode);
        assertEquals("number", schemaNode.get("type").asText());
    }

    // Tests createContextual when property is null
    @Test
    public void testCreateContextual_nullProperty_returnsSelf() throws Exception {
        NumberSerializers.IntegerSerializer serializer = new NumberSerializers.IntegerSerializer(Integer.class);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonSerializer<?> contextual = serializer.createContextual(provider, null);
        assertSame(serializer, contextual);
    }

    // Tests createContextual with JsonFormat shape STRING and default shape
    @Test
    public void testCreateContextual_withStringShape_returnsToStringSerializer() throws Exception {
        FormattedNumbers pojo = new FormattedNumbers();
        String json = mapper.writeValueAsString(pojo);

        assertTrue(json.contains("\"intVal\":\"123\""));
        assertTrue(json.contains("\"longVal\":\"456\""));
        assertTrue(json.contains("\"doubleVal\":7.89"));
        assertTrue(json.contains("\"shortVal\":12"));
    }

    // Tests acceptJsonFormatVisitor for integer types
    @Test
    public void testAcceptJsonFormatVisitor_integerType_callsExpectIntegerFormat() throws Exception {
        NumberSerializers.IntegerSerializer serializer = new NumberSerializers.IntegerSerializer(Integer.class);
        final boolean[] called = new boolean[2];

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                called[0] = true;
                return new JsonIntegerFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        called[1] = (type == JsonParser.NumberType.INT);
                    }
                };
            }
        };

        serializer.acceptJsonFormatVisitor(visitor, mapper.constructType(Integer.class));
        assertTrue(called[0]);
        assertTrue(called[1]);
    }

    // Tests acceptJsonFormatVisitor for floating point types
    @Test
    public void testAcceptJsonFormatVisitor_floatType_callsExpectNumberFormat() throws Exception {
        NumberSerializers.DoubleSerializer serializer = new NumberSerializers.DoubleSerializer(Double.class);
        final boolean[] called = new boolean[2];

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
                called[0] = true;
                return new JsonNumberFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        called[1] = (type == JsonParser.NumberType.DOUBLE);
                    }
                };
            }
        };

        serializer.acceptJsonFormatVisitor(visitor, mapper.constructType(Double.class));
        assertTrue(called[0]);
        assertTrue(called[1]);
    }

    // Tests acceptJsonFormatVisitor when visitor returns null sub-visitor
    @Test
    public void testAcceptJsonFormatVisitor_nullSubVisitor_handlesGracefully() throws Exception {
        NumberSerializers.IntegerSerializer intSerializer = new NumberSerializers.IntegerSerializer(Integer.class);
        NumberSerializers.DoubleSerializer doubleSerializer = new NumberSerializers.DoubleSerializer(Double.class);

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                return null;
            }

            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
                return null;
            }
        };

        intSerializer.acceptJsonFormatVisitor(visitor, mapper.constructType(Integer.class));
        doubleSerializer.acceptJsonFormatVisitor(visitor, mapper.constructType(Double.class));
    }

    // Tests acceptJsonFormatVisitor for LongSerializer
    @Test
    public void testAcceptJsonFormatVisitor_longSerializer_callsExpectIntegerFormatWithLongType() throws Exception {
        NumberSerializers.LongSerializer serializer = new NumberSerializers.LongSerializer(Long.class);
        final boolean[] called = new boolean[2];

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                called[0] = true;
                return new JsonIntegerFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        called[1] = (type == JsonParser.NumberType.LONG);
                    }
                };
            }
        };

        serializer.acceptJsonFormatVisitor(visitor, mapper.constructType(Long.class));
        assertTrue(called[0]);
        assertTrue(called[1]);
    }

    // Tests acceptJsonFormatVisitor for FloatSerializer
    @Test
    public void testAcceptJsonFormatVisitor_floatSerializer_callsExpectNumberFormatWithFloatType() throws Exception {
        NumberSerializers.FloatSerializer serializer = new NumberSerializers.FloatSerializer();
        final boolean[] called = new boolean[2];

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
                called[0] = true;
                return new JsonNumberFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        called[1] = (type == JsonParser.NumberType.FLOAT);
                    }
                };
            }
        };

        serializer.acceptJsonFormatVisitor(visitor, mapper.constructType(Float.class));
        assertTrue(called[0]);
        assertTrue(called[1]);
    }
}