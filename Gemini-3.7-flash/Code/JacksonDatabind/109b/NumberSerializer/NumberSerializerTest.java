package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNumberFormatVisitor;

public class NumberSerializerTest {

    private ObjectMapper mapper;
    private JsonFactory jsonFactory;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        jsonFactory = new JsonFactory();
    }

    // Helper POJO with @JsonFormat(shape = STRING) on BigDecimal
    static class BigDecimalAsString {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public BigDecimal value;

        public BigDecimalAsString(BigDecimal value) {
            this.value = value;
        }
    }

    // Helper POJO with @JsonFormat(shape = NUMBER) on BigDecimal
    static class BigDecimalAsNumber {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public BigDecimal value;

        public BigDecimalAsNumber(BigDecimal value) {
            this.value = value;
        }
    }

    // Helper POJO without format annotation
    static class BigDecimalDefault {
        public BigDecimal value;

        public BigDecimalDefault(BigDecimal value) {
            this.value = value;
        }
    }

    // Helper POJO with @JsonFormat(shape = STRING) on BigInteger
    static class BigIntegerAsString {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public BigInteger value;

        public BigIntegerAsString(BigInteger value) {
            this.value = value;
        }
    }

    // Helper POJO with @JsonFormat(shape = ANY) on BigDecimal
    static class BigDecimalAsAny {
        @JsonFormat(shape = JsonFormat.Shape.ANY)
        public BigDecimal value;

        public BigDecimalAsAny(BigDecimal value) {
            this.value = value;
        }
    }

    // Custom Number implementation to test fallback branch
    static class CustomNumber extends Number {
        private static final long serialVersionUID = 1L;
        private final String representation;

        public CustomNumber(String representation) {
            this.representation = representation;
        }

        @Override
        public int intValue() { return 0; }
        @Override
        public long longValue() { return 0L; }
        @Override
        public float floatValue() { return 0.0f; }
        @Override
        public double doubleValue() { return 0.0; }
        @Override
        public String toString() { return representation; }
    }

    // Tests serialization of BigDecimal value
    @Test
    public void testSerialize_bigDecimal_writesNumber() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        BigDecimal value = new BigDecimal("123.456");

        NumberSerializer.instance.serialize(value, gen, null);
        gen.flush();

        assertEquals("123.456", sw.toString());
    }

    // Tests serialization of BigInteger value
    @Test
    public void testSerialize_bigInteger_writesNumber() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        BigInteger value = new BigInteger("98765432109876543210");

        NumberSerializer.instance.serialize(value, gen, null);
        gen.flush();

        assertEquals("98765432109876543210", sw.toString());
    }

    // Tests serialization of Long value
    @Test
    public void testSerialize_long_writesNumber() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        Long value = 123456789L;

        NumberSerializer.instance.serialize(value, gen, null);
        gen.flush();

        assertEquals("123456789", sw.toString());
    }

    // Tests serialization of Double value
    @Test
    public void testSerialize_double_writesNumber() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        Double value = 3.14159;

        NumberSerializer.instance.serialize(value, gen, null);
        gen.flush();

        assertEquals("3.14159", sw.toString());
    }

    // Tests serialization of Float value
    @Test
    public void testSerialize_float_writesNumber() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        Float value = 2.5f;

        NumberSerializer.instance.serialize(value, gen, null);
        gen.flush();

        assertEquals("2.5", sw.toString());
    }

    // Tests serialization of Integer, Short, Byte values
    @Test
    public void testSerialize_integerTypes_writesIntNumber() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);

        NumberSerializer.instance.serialize(Integer.valueOf(42), gen, null);
        gen.flush();
        assertEquals("42", sw.toString());

        sw = new StringWriter();
        gen = jsonFactory.createGenerator(sw);
        NumberSerializer.instance.serialize(Short.valueOf((short) 7), gen, null);
        gen.flush();
        assertEquals("7", sw.toString());

        sw = new StringWriter();
        gen = jsonFactory.createGenerator(sw);
        NumberSerializer.instance.serialize(Byte.valueOf((byte) 3), gen, null);
        gen.flush();
        assertEquals("3", sw.toString());
    }

    // Tests serialization of custom Number subclass (fallback branch)
    @Test
    public void testSerialize_customNumber_writesRawStringRepresentation() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        CustomNumber customNum = new CustomNumber("99999.88888");

        NumberSerializer.instance.serialize(customNum, gen, null);
        gen.flush();

        assertEquals("99999.88888", sw.toString());
    }

    // Tests contextual serialization when JsonFormat.Shape is STRING
    @Test
    public void testCreateContextual_stringShape_serializesAsString() throws IOException {
        BigDecimalAsString bean = new BigDecimalAsString(new BigDecimal("123.45"));
        String json = mapper.writeValueAsString(bean);

        assertEquals("{\"value\":\"123.45\"}", json);
    }

    // Tests contextual serialization when JsonFormat.Shape is NUMBER
    @Test
    public void testCreateContextual_numberShape_serializesAsNumber() throws IOException {
        BigDecimalAsNumber bean = new BigDecimalAsNumber(new BigDecimal("123.45"));
        String json = mapper.writeValueAsString(bean);

        assertEquals("{\"value\":123.45}", json);
    }

    // Tests contextual serialization when no format is specified
    @Test
    public void testCreateContextual_nullFormat_returnsSameSerializer() throws IOException {
        BigDecimalDefault bean = new BigDecimalDefault(new BigDecimal("123.45"));
        String json = mapper.writeValueAsString(bean);

        assertEquals("{\"value\":123.45}", json);
    }

    // Tests contextual resolution directly without format annotation
    @Test
    public void testCreateContextual_nullProperty_returnsThis() throws JsonMappingException {
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        NumberSerializer serializer = new NumberSerializer(BigDecimal.class);

        JsonSerializer<?> contextual = serializer.createContextual(prov, null);

        assertSame(serializer, contextual);
    }

    // Tests getSchema for integer type (BigInteger)
    @Test
    public void testGetSchema_bigInteger_returnsIntegerSchema() {
        NumberSerializer serializer = new NumberSerializer(BigInteger.class);
        JsonNode schema = serializer.getSchema(null, null);

        assertNotNull(schema);
        assertEquals("integer", schema.get("type").asText());
    }

    // Tests getSchema for non-integer types (BigDecimal / Number)
    @Test
    public void testGetSchema_number_returnsNumberSchema() {
        NumberSerializer serializer = NumberSerializer.instance;
        JsonNode schema = serializer.getSchema(null, null);

        assertNotNull(schema);
        assertEquals("number", schema.get("type").asText());

        NumberSerializer decimalSerializer = new NumberSerializer(BigDecimal.class);
        JsonNode decimalSchema = decimalSerializer.getSchema(null, null);
        assertEquals("number", decimalSchema.get("type").asText());
    }

    // Tests format visitor for BigInteger
    @Test
    public void testAcceptJsonFormatVisitor_bigInteger_visitsIntegerFormat() throws JsonMappingException {
        final boolean[] intVisited = new boolean[1];
        final JsonParser.NumberType[] numberTypeHolder = new JsonParser.NumberType[1];

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                return new JsonIntegerFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        intVisited[0] = true;
                        numberTypeHolder[0] = type;
                    }
                };
            }
        };

        NumberSerializer serializer = new NumberSerializer(BigInteger.class);
        serializer.acceptJsonFormatVisitor(visitor, null);

        assertTrue(intVisited[0]);
        assertEquals(JsonParser.NumberType.BIG_INTEGER, numberTypeHolder[0]);
    }

    // Tests format visitor for BigDecimal
    @Test
    public void testAcceptJsonFormatVisitor_bigDecimal_visitsFloatFormat() throws JsonMappingException {
        final boolean[] floatVisited = new boolean[1];
        final JsonParser.NumberType[] numberTypeHolder = new JsonParser.NumberType[1];

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
                return new JsonNumberFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        floatVisited[0] = true;
                        numberTypeHolder[0] = type;
                    }
                };
            }
        };

        NumberSerializer serializer = new NumberSerializer(BigDecimal.class);
        serializer.acceptJsonFormatVisitor(visitor, null);

        assertTrue(floatVisited[0]);
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, numberTypeHolder[0]);
    }

    // Tests format visitor for generic Number type
    @Test
    public void testAcceptJsonFormatVisitor_genericNumber_callsExpectNumberFormat() throws JsonMappingException {
        final boolean[] numberVisited = new boolean[1];

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
                numberVisited[0] = true;
                return new JsonNumberFormatVisitor.Base();
            }
        };

        NumberSerializer.instance.acceptJsonFormatVisitor(visitor, null);

        assertTrue(numberVisited[0]);
    }

    // Tests contextual serialization when JsonFormat.Shape is STRING on BigInteger
    @Test
    public void testCreateContextual_bigIntegerStringShape_serializesAsString() throws IOException {
        BigIntegerAsString bean = new BigIntegerAsString(new BigInteger("987654321"));
        String json = mapper.writeValueAsString(bean);

        assertEquals("{\"value\":\"987654321\"}", json);
    }

    // Tests contextual serialization when JsonFormat.Shape is ANY on BigDecimal
    @Test
    public void testCreateContextual_anyShape_returnsSelf() throws IOException {
        BigDecimalAsAny bean = new BigDecimalAsAny(new BigDecimal("123.45"));
        String json = mapper.writeValueAsString(bean);

        assertEquals("{\"value\":123.45}", json);
    }

    // Tests serialization of AtomicInteger and AtomicLong (Number subclasses falling to toString branch)
    @Test
    public void testSerialize_atomicTypes_writesNumber() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        AtomicInteger atomicInt = new AtomicInteger(100);

        NumberSerializer.instance.serialize(atomicInt, gen, null);
        gen.flush();
        assertEquals("100", sw.toString());

        sw = new StringWriter();
        gen = jsonFactory.createGenerator(sw);
        AtomicLong atomicLong = new AtomicLong(200L);

        NumberSerializer.instance.serialize(atomicLong, gen, null);
        gen.flush();
        assertEquals("200", sw.toString());
    }

    // Tests acceptJsonFormatVisitor when visitor callbacks return null
    @Test
    public void testAcceptJsonFormatVisitor_visitorReturnsNull_handlesGracefully() throws JsonMappingException {
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

        NumberSerializer bigIntSerializer = new NumberSerializer(BigInteger.class);
        bigIntSerializer.acceptJsonFormatVisitor(visitor, null);

        NumberSerializer bigDecSerializer = new NumberSerializer(BigDecimal.class);
        bigDecSerializer.acceptJsonFormatVisitor(visitor, null);
    }
}