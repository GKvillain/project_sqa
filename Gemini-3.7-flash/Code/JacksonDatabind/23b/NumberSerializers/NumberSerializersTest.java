package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNumberFormatVisitor;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class NumberSerializersTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonFactory jsonFactory = new JsonFactory();
    private final SerializerProvider provider = new DefaultSerializerProvider.Impl();

    // Tests instantiation of protected constructor
    @Test
    public void testConstructor_instantiation_createsInstance() {
        NumberSerializers serializers = new NumberSerializers();
        assertNotNull(serializers);
    }

    // Tests mapping registration of all standard numeric types and primitives
    @Test
    public void testAddAll_standardNumericTypes_registersAllExpectedEntries() {
        Map<String, JsonSerializer<?>> serializers = new HashMap<String, JsonSerializer<?>>();
        NumberSerializers.addAll(serializers);

        assertEquals(12, serializers.size());
        assertTrue(serializers.containsKey(Integer.class.getName()));
        assertTrue(serializers.containsKey(Integer.TYPE.getName()));
        assertTrue(serializers.containsKey(Long.class.getName()));
        assertTrue(serializers.containsKey(Long.TYPE.getName()));
        assertTrue(serializers.containsKey(Byte.class.getName()));
        assertTrue(serializers.containsKey(Byte.TYPE.getName()));
        assertTrue(serializers.containsKey(Short.class.getName()));
        assertTrue(serializers.containsKey(Short.TYPE.getName()));
        assertTrue(serializers.containsKey(Float.class.getName()));
        assertTrue(serializers.containsKey(Float.TYPE.getName()));
        assertTrue(serializers.containsKey(Double.class.getName()));
        assertTrue(serializers.containsKey(Double.TYPE.getName()));
    }

    // Tests serialization of Short values
    @Test
    public void testShortSerializer_serialize_writesShortValue() throws IOException {
        NumberSerializers.ShortSerializer serializer = new NumberSerializers.ShortSerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        serializer.serialize((short) 123, gen, provider);
        gen.close();

        assertEquals("123", sw.toString());
    }

    // Tests serialization of Integer values
    @Test
    public void testIntegerSerializer_serialize_writesIntValue() throws IOException {
        NumberSerializers.IntegerSerializer serializer = new NumberSerializers.IntegerSerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        serializer.serialize(Integer.valueOf(456), gen, provider);
        gen.close();

        assertEquals("456", sw.toString());
    }

    // Tests serializeWithType of IntegerSerializer without type information
    @Test
    public void testIntegerSerializer_serializeWithType_writesPlainValue() throws IOException {
        NumberSerializers.IntegerSerializer serializer = new NumberSerializers.IntegerSerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        serializer.serializeWithType(Integer.valueOf(789), gen, provider, null);
        gen.close();

        assertEquals("789", sw.toString());
    }

    // Tests serialization of Number via IntLikeSerializer
    @Test
    public void testIntLikeSerializer_serialize_writesIntValue() throws IOException {
        NumberSerializers.IntLikeSerializer serializer = new NumberSerializers.IntLikeSerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        serializer.serialize((byte) 42, gen, provider);
        gen.close();

        assertEquals("42", sw.toString());
    }

    // Tests serialization of Long values
    @Test
    public void testLongSerializer_serialize_writesLongValue() throws IOException {
        NumberSerializers.LongSerializer serializer = new NumberSerializers.LongSerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        serializer.serialize(Long.valueOf(9876543210L), gen, provider);
        gen.close();

        assertEquals("9876543210", sw.toString());
    }

    // Tests serialization of Float values
    @Test
    public void testFloatSerializer_serialize_writesFloatValue() throws IOException {
        NumberSerializers.FloatSerializer serializer = new NumberSerializers.FloatSerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        serializer.serialize(Float.valueOf(1.25f), gen, provider);
        gen.close();

        assertEquals("1.25", sw.toString());
    }

    // Tests serialization of Double values
    @Test
    public void testDoubleSerializer_serialize_writesDoubleValue() throws IOException {
        NumberSerializers.DoubleSerializer serializer = new NumberSerializers.DoubleSerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        serializer.serialize(Double.valueOf(3.14159), gen, provider);
        gen.close();

        assertEquals("3.14159", sw.toString());
    }

    // Tests serializeWithType of DoubleSerializer without type information
    @Test
    public void testDoubleSerializer_serializeWithType_writesPlainValue() throws IOException {
        NumberSerializers.DoubleSerializer serializer = new NumberSerializers.DoubleSerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        serializer.serializeWithType(Double.valueOf(2.718), gen, provider, null);
        gen.close();

        assertEquals("2.718", sw.toString());
    }

    // Tests JSON schema retrieval for integer schema types
    @Test
    public void testGetSchema_integerSerializer_returnsIntegerSchema() {
        NumberSerializers.IntegerSerializer serializer = new NumberSerializers.IntegerSerializer();
        JsonNode schemaNode = serializer.getSchema(null, null);

        assertNotNull(schemaNode);
        assertEquals("integer", schemaNode.get("type").asText());
    }

    // Tests JSON schema retrieval for number schema types
    @Test
    public void testGetSchema_doubleSerializer_returnsNumberSchema() {
        NumberSerializers.DoubleSerializer serializer = new NumberSerializers.DoubleSerializer();
        JsonNode schemaNode = serializer.getSchema(null, null);

        assertNotNull(schemaNode);
        assertEquals("number", schemaNode.get("type").asText());
    }

    // Tests format visitor for integer numeric serializers
    @Test
    public void testAcceptJsonFormatVisitor_integerSerializer_invokesIntegerVisitor() throws JsonMappingException {
        NumberSerializers.IntegerSerializer serializer = new NumberSerializers.IntegerSerializer();
        final boolean[] visited = new boolean[1];
        final JsonParser.NumberType[] assignedType = new JsonParser.NumberType[1];

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                visited[0] = true;
                return new JsonIntegerFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        assignedType[0] = type;
                    }
                };
            }
        };

        serializer.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Integer.class));
        assertTrue(visited[0]);
        assertEquals(JsonParser.NumberType.INT, assignedType[0]);
    }

    // Tests format visitor for floating point numeric serializers
    @Test
    public void testAcceptJsonFormatVisitor_doubleSerializer_invokesNumberVisitor() throws JsonMappingException {
        NumberSerializers.DoubleSerializer serializer = new NumberSerializers.DoubleSerializer();
        final boolean[] visited = new boolean[1];
        final JsonParser.NumberType[] assignedType = new JsonParser.NumberType[1];

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
                visited[0] = true;
                return new JsonNumberFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        assignedType[0] = type;
                    }
                };
            }
        };

        serializer.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Double.class));
        assertTrue(visited[0]);
        assertEquals(JsonParser.NumberType.DOUBLE, assignedType[0]);
    }

    // Tests createContextual returning this when property is null
    @Test
    public void testCreateContextual_nullProperty_returnsSameInstance() throws JsonMappingException {
        NumberSerializers.IntegerSerializer serializer = new NumberSerializers.IntegerSerializer();
        JsonSerializer<?> result = serializer.createContextual(provider, null);

        assertSame(serializer, result);
    }

    static class FormattedNumberBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int value = 12345;

        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public double doubleValue = 5.5;
    }

    // Tests contextual resolution with STRING format shape delegating to ToStringSerializer
    @Test
    public void testCreateContextual_withStringFormatShape_serializesAsString() throws Exception {
        FormattedNumberBean bean = new FormattedNumberBean();
        String json = mapper.writeValueAsString(bean);

        assertTrue(json.contains("\"value\":\"12345\""));
        assertTrue(json.contains("\"doubleValue\":5.5"));
    }
}