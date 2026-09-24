package com.fasterxml.jackson.databind.ser.std;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNumberFormatVisitor;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class NumberSerializerTest {

    private final JsonFactory _jsonFactory = new JsonFactory();
    private final ObjectMapper _mapper = new ObjectMapper();

    // Tests BigDecimal serialization
    @Test
    public void testSerialize_bigDecimalValue_writesExactDecimal() throws Exception {
        NumberSerializer serializer = new NumberSerializer(BigDecimal.class);
        StringWriter writer = new StringWriter();
        JsonGenerator generator = _jsonFactory.createGenerator(writer);

        serializer.serialize(new BigDecimal("123.456"), generator, _mapper.getSerializerProvider());
        generator.close();

        assertEquals("123.456", writer.toString());
    }

    // Tests BigInteger serialization
    @Test
    public void testSerialize_bigIntegerValue_writesExactInteger() throws Exception {
        NumberSerializer serializer = new NumberSerializer(BigInteger.class);
        StringWriter writer = new StringWriter();
        JsonGenerator generator = _jsonFactory.createGenerator(writer);

        serializer.serialize(new BigInteger("98765432109876543210"), generator, _mapper.getSerializerProvider());
        generator.close();

        assertEquals("98765432109876543210", writer.toString());
    }

    // Tests Integer serialization branch
    @Test
    public void testSerialize_integerValue_writesInt() throws Exception {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = _jsonFactory.createGenerator(writer);

        NumberSerializer.instance.serialize(Integer.valueOf(42), generator, _mapper.getSerializerProvider());
        generator.close();

        assertEquals("42", writer.toString());
    }

    // Tests Long serialization branch
    @Test
    public void testSerialize_longValue_writesLong() throws Exception {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = _jsonFactory.createGenerator(writer);

        NumberSerializer.instance.serialize(Long.valueOf(1234567890123L), generator, _mapper.getSerializerProvider());
        generator.close();

        assertEquals("1234567890123", writer.toString());
    }

    // Tests Double serialization branch
    @Test
    public void testSerialize_doubleValue_writesDouble() throws Exception {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = _jsonFactory.createGenerator(writer);

        NumberSerializer.instance.serialize(Double.valueOf(12.5), generator, _mapper.getSerializerProvider());
        generator.close();

        assertEquals("12.5", writer.toString());
    }

    // Tests Float serialization branch
    @Test
    public void testSerialize_floatValue_writesFloat() throws Exception {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = _jsonFactory.createGenerator(writer);

        NumberSerializer.instance.serialize(Float.valueOf(2.5f), generator, _mapper.getSerializerProvider());
        generator.close();

        assertEquals("2.5", writer.toString());
    }

    // Tests Byte serialization branch
    @Test
    public void testSerialize_byteValue_writesByteAsInt() throws Exception {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = _jsonFactory.createGenerator(writer);

        NumberSerializer.instance.serialize(Byte.valueOf((byte) 8), generator, _mapper.getSerializerProvider());
        generator.close();

        assertEquals("8", writer.toString());
    }

    // Tests Short serialization branch
    @Test
    public void testSerialize_shortValue_writesShortAsInt() throws Exception {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = _jsonFactory.createGenerator(writer);

        NumberSerializer.instance.serialize(Short.valueOf((short) 16), generator, _mapper.getSerializerProvider());
        generator.close();

        assertEquals("16", writer.toString());
    }

    // Tests custom Number type fallback serialization branch
    @Test
    public void testSerialize_customNumberValue_writesToStringFallback() throws Exception {
        Number customNumber = new Number() {
            private static final long serialVersionUID = 1L;
            @Override public int intValue() { return 100; }
            @Override public long longValue() { return 100L; }
            @Override public float floatValue() { return 100.0f; }
            @Override public double doubleValue() { return 100.0; }
            @Override public String toString() { return "100.000"; }
        };

        StringWriter writer = new StringWriter();
        JsonGenerator generator = _jsonFactory.createGenerator(writer);

        NumberSerializer.instance.serialize(customNumber, generator, _mapper.getSerializerProvider());
        generator.close();

        assertEquals("100.000", writer.toString());
    }

    // Tests getSchema for BigInteger serializer returning integer type schema
    @Test
    public void testGetSchema_bigIntegerType_returnsIntegerSchema() {
        NumberSerializer serializer = new NumberSerializer(BigInteger.class);
        JsonNode schemaNode = serializer.getSchema(_mapper.getSerializerProvider(), null);

        assertNotNull(schemaNode);
        assertEquals("integer", schemaNode.get("type").asText());
    }

    // Tests getSchema for non-BigInteger serializer returning number type schema
    @Test
    public void testGetSchema_numberType_returnsNumberSchema() {
        JsonNode schemaNode = NumberSerializer.instance.getSchema(_mapper.getSerializerProvider(), null);

        assertNotNull(schemaNode);
        assertEquals("number", schemaNode.get("type").asText());
    }

    // Tests acceptJsonFormatVisitor for BigInteger serializer
    @Test
    public void testAcceptJsonFormatVisitor_bigInteger_visitsIntegerFormatWithBigIntegerType() throws Exception {
        NumberSerializer serializer = new NumberSerializer(BigInteger.class);
        final JsonParser.NumberType[] capturedType = new JsonParser.NumberType[1];

        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                return new JsonIntegerFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        capturedType[0] = type;
                    }
                };
            }
        };

        JavaType typeHint = TypeFactory.defaultInstance().constructType(BigInteger.class);
        serializer.acceptJsonFormatVisitor(visitor, typeHint);

        assertEquals(JsonParser.NumberType.BIG_INTEGER, capturedType[0]);
    }

    // Tests acceptJsonFormatVisitor for BigDecimal serializer to ensure BIG_DECIMAL is reported
    @Test
    public void testAcceptJsonFormatVisitor_bigDecimal_visitsFloatFormatWithBigDecimalType() throws Exception {
        NumberSerializer serializer = new NumberSerializer(BigDecimal.class);
        final JsonParser.NumberType[] capturedType = new JsonParser.NumberType[1];

        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
                return new JsonNumberFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        capturedType[0] = type;
                    }
                };
            }
        };

        JavaType typeHint = TypeFactory.defaultInstance().constructType(BigDecimal.class);
        serializer.acceptJsonFormatVisitor(visitor, typeHint);

        assertEquals(JsonParser.NumberType.BIG_DECIMAL, capturedType[0]);
    }

    // Tests acceptJsonFormatVisitor for generic Number serializer
    @Test
    public void testAcceptJsonFormatVisitor_genericNumber_expectsNumberFormat() throws Exception {
        final boolean[] called = new boolean[1];

        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
                called[0] = true;
                return null;
            }
        };

        JavaType typeHint = TypeFactory.defaultInstance().constructType(Number.class);
        NumberSerializer.instance.acceptJsonFormatVisitor(visitor, typeHint);

        assertTrue(called[0]);
    }
}