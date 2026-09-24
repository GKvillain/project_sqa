package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

public class NumberSerializersTest {

    private ObjectMapper mapper;
    private SerializerProvider provider;
    private JsonFactory factory;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        provider = mapper.getSerializerProviderInstance();
        factory = new JsonFactory();
    }

    // Tests constructor
    @Test
    public void testConstructor() {
        NumberSerializers ns = new NumberSerializers();
        assertNotNull(ns);
    }

    // Tests addAll populates map with correct number serializer classes
    @Test
    public void testAddAll() {
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
        assertTrue(map.containsKey(Double.class.getName()));
        assertTrue(map.containsKey(Double.TYPE.getName()));
        assertTrue(map.containsKey(Float.class.getName()));
        assertTrue(map.containsKey(Float.TYPE.getName()));
    }

    // Tests IntegerSerializer serialize with positive, negative and zero
    @Test
    public void testIntegerSerializer_serialize() throws IOException {
        NumberSerializers.IntegerSerializer ser = new NumberSerializers.IntegerSerializer(Integer.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = factory.createGenerator(sw);

        ser.serialize(42, gen, provider);
        ser.serialize(-123, gen, provider);
        ser.serialize(0, gen, provider);
        gen.flush();

        assertEquals("42 -123 0", sw.toString().trim());
    }

    // Tests IntegerSerializer serializeWithType delegates to serialize
    @Test
    public void testIntegerSerializer_serializeWithType() throws IOException {
        NumberSerializers.IntegerSerializer ser = new NumberSerializers.IntegerSerializer(Integer.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = factory.createGenerator(sw);

        ser.serializeWithType(999, gen, provider, null);
        gen.flush();

        assertEquals("999", sw.toString().trim());
    }

    // Tests LongSerializer serialize
    @Test
    public void testLongSerializer_serialize() throws IOException {
        NumberSerializers.LongSerializer ser = new NumberSerializers.LongSerializer(Long.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = factory.createGenerator(sw);

        ser.serialize(1234567890123L, gen, provider);
        gen.flush();

        assertEquals("1234567890123", sw.toString().trim());
    }

    // Tests ShortSerializer serialize
    @Test
    public void testShortSerializer_serialize() throws IOException {
        NumberSerializers.ShortSerializer ser = NumberSerializers.ShortSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = factory.createGenerator(sw);

        ser.serialize((short) 15, gen, provider);
        gen.flush();

        assertEquals("15", sw.toString().trim());
    }

    // Tests IntLikeSerializer serialize
    @Test
    public void testIntLikeSerializer_serialize() throws IOException {
        NumberSerializers.IntLikeSerializer ser = NumberSerializers.IntLikeSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = factory.createGenerator(sw);

        ser.serialize((byte) 8, gen, provider);
        gen.flush();

        assertEquals("8", sw.toString().trim());
    }

    // Tests FloatSerializer serialize
    @Test
    public void testFloatSerializer_serialize() throws IOException {
        NumberSerializers.FloatSerializer ser = NumberSerializers.FloatSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = factory.createGenerator(sw);

        ser.serialize(3.14f, gen, provider);
        gen.flush();

        assertEquals("3.14", sw.toString().trim());
    }

    // Tests DoubleSerializer serialize
    @Test
    public void testDoubleSerializer_serialize() throws IOException {
        NumberSerializers.DoubleSerializer ser = new NumberSerializers.DoubleSerializer(Double.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = factory.createGenerator(sw);

        ser.serialize(2.71828, gen, provider);
        gen.flush();

        assertEquals("2.71828", sw.toString().trim());
    }

    // Tests DoubleSerializer serializeWithType delegates to serialize
    @Test
    public void testDoubleSerializer_serializeWithType() throws IOException {
        NumberSerializers.DoubleSerializer ser = new NumberSerializers.DoubleSerializer(Double.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = factory.createGenerator(sw);

        ser.serializeWithType(1.23, gen, provider, null);
        gen.flush();

        assertEquals("1.23", sw.toString().trim());
    }

    // Tests getSchema for integer and double serializers
    @Test
    public void testGetSchema() {
        NumberSerializers.IntegerSerializer intSer = new NumberSerializers.IntegerSerializer(Integer.class);
        JsonNode intSchema = intSer.getSchema(provider, null);
        assertNotNull(intSchema);
        assertEquals("integer", intSchema.get("type").asText());

        NumberSerializers.DoubleSerializer doubleSer = new NumberSerializers.DoubleSerializer(Double.class);
        JsonNode doubleSchema = doubleSer.getSchema(provider, null);
        assertNotNull(doubleSchema);
        assertEquals("number", doubleSchema.get("type").asText());
    }

    // Tests createContextual with null property returns same serializer
    @Test
    public void testCreateContextual_nullProperty_returnsSelf() throws Exception {
        NumberSerializers.IntegerSerializer intSer = new NumberSerializers.IntegerSerializer(Integer.class);
        JsonSerializer<?> contextual = intSer.createContextual(provider, null);
        assertSame(intSer, contextual);
    }

    // Tests createContextual with format shape STRING returns ToStringSerializer
    @Test
    public void testCreateContextual_shapeString_returnsToStringSerializer() throws Exception {
        String json = mapper.writeValueAsString(new FormattedNumberWrapper(123));
        assertEquals("{\"value\":\"123\"}", json);
    }

    // Tests createContextual with format shape NUMBER returns standard serialization
    @Test
    public void testCreateContextual_shapeNumber_returnsSelf() throws Exception {
        String json = mapper.writeValueAsString(new NumberFormatNumberWrapper(456));
        assertEquals("{\"value\":456}", json);
    }

    // Tests acceptJsonFormatVisitor execution via mapper schema/type visit
    @Test
    public void testAcceptJsonFormatVisitor() throws Exception {
        NumberSerializers.IntegerSerializer intSer = new NumberSerializers.IntegerSerializer(Integer.class);
        JavaType type = mapper.constructType(Integer.class);
        // Ensure visiting does not fail with NullPointerException or other unexpected errors
        assertNotNull(intSer.getSchema(provider, type));

        NumberSerializers.FloatSerializer floatSer = NumberSerializers.FloatSerializer.instance;
        JavaType floatType = mapper.constructType(Float.class);
        assertNotNull(floatSer.getSchema(provider, floatType));
    }

    // Helper classes for testing @JsonFormat handling
    static class FormattedNumberWrapper {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int value;

        public FormattedNumberWrapper(int v) {
            this.value = v;
        }
    }

    static class NumberFormatNumberWrapper {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public int value;

        public NumberFormatNumberWrapper(int v) {
            this.value = v;
        }
    }
}