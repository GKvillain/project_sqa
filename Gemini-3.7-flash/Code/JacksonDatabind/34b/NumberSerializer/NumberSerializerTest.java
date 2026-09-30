package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNumberFormatVisitor;
import com.fasterxml.jackson.databind.type.TypeFactory;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class NumberSerializerTest {

    private JsonFactory jsonFactory;
    private NumberSerializer standardSerializer;
    private NumberSerializer bigIntegerSerializer;
    private NumberSerializer bigDecimalSerializer;

    @Before
    public void setUp() {
        jsonFactory = new JsonFactory();
        standardSerializer = NumberSerializer.instance;
        bigIntegerSerializer = new NumberSerializer(BigInteger.class);
        bigDecimalSerializer = new NumberSerializer(BigDecimal.class);
    }

    private String serializeNumber(NumberSerializer serializer, Number value) throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator generator = jsonFactory.createGenerator(sw);
        serializer.serialize(value, generator, null);
        generator.close();
        return sw.toString();
    }

    // Tests serialize method with BigDecimal input
    @Test
    public void testSerialize_bigDecimalValue_writesExactDecimal() throws IOException {
        BigDecimal value = new BigDecimal("12345.67890");
        String result = serializeNumber(standardSerializer, value);
        assertEquals("12345.67890", result);
    }

    // Tests serialize method with BigInteger input
    @Test
    public void testSerialize_bigIntegerValue_writesExactInteger() throws IOException {
        BigInteger value = new BigInteger("123456789012345678901234567890");
        String result = serializeNumber(bigIntegerSerializer, value);
        assertEquals("123456789012345678901234567890", result);
    }

    // Tests serialize method with Integer input
    @Test
    public void testSerialize_integerValue_writesInt() throws IOException {
        String result = serializeNumber(standardSerializer, Integer.valueOf(42));
        assertEquals("42", result);
    }

    // Tests serialize method with Long input
    @Test
    public void testSerialize_longValue_writesLong() throws IOException {
        String result = serializeNumber(standardSerializer, Long.valueOf(9876543210L));
        assertEquals("9876543210", result);
    }

    // Tests serialize method with Double input
    @Test
    public void testSerialize_doubleValue_writesDouble() throws IOException {
        String result = serializeNumber(standardSerializer, Double.valueOf(3.14159));
        assertEquals("3.14159", result);
    }

    // Tests serialize method with Float input
    @Test
    public void testSerialize_floatValue_writesFloat() throws IOException {
        String result = serializeNumber(standardSerializer, Float.valueOf(1.25f));
        assertEquals("1.25", result);
    }

    // Tests serialize method with Byte input
    @Test
    public void testSerialize_byteValue_writesByteAsInt() throws IOException {
        String result = serializeNumber(standardSerializer, Byte.valueOf((byte) 8));
        assertEquals("8", result);
    }

    // Tests serialize method with Short input
    @Test
    public void testSerialize_shortValue_writesShortAsInt() throws IOException {
        String result = serializeNumber(standardSerializer, Short.valueOf((short) 1024));
        assertEquals("1024", result);
    }

    // Tests serialize method with custom/fallback Number subclass
    @Test
    public void testSerialize_customNumberSubclass_writesStringRepresentation() throws IOException {
        Number customNumber = new Number() {
            private static final long serialVersionUID = 1L;
            @Override public int intValue() { return 0; }
            @Override public long longValue() { return 0L; }
            @Override public float floatValue() { return 0.0f; }
            @Override public double doubleValue() { return 0.0d; }
            @Override public String toString() { return "999999999999999999999999.999"; }
        };
        String result = serializeNumber(standardSerializer, customNumber);
        assertEquals("999999999999999999999999.999", result);
    }

    // Tests getSchema for BigInteger serializer returning integer type schema
    @Test
    public void testGetSchema_bigIntegerType_returnsIntegerSchema() {
        JsonNode schema = bigIntegerSerializer.getSchema(null, null);
        assertNotNull(schema);
        assertEquals("integer", schema.get("type").asText());
    }

    // Tests getSchema for general Number serializer returning number type schema
    @Test
    public void testGetSchema_generalNumberType_returnsNumberSchema() {
        JsonNode schema = standardSerializer.getSchema(null, null);
        assertNotNull(schema);
        assertEquals("number", schema.get("type").asText());
    }

    // Tests acceptJsonFormatVisitor for BigInteger serializer
    @Test
    public void testAcceptJsonFormatVisitor_bigInteger_visitsIntFormatWithBigIntegerType() throws Exception {
        final JsonParser.NumberType[] recordedType = new JsonParser.NumberType[1];
        final boolean[] intVisited = new boolean[1];

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                intVisited[0] = true;
                return new JsonIntegerFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        recordedType[0] = type;
                    }
                };
            }
        };

        JavaType javaType = TypeFactory.defaultInstance().constructType(BigInteger.class);
        bigIntegerSerializer.acceptJsonFormatVisitor(visitor, javaType);

        assertTrue(intVisited[0]);
        assertEquals(JsonParser.NumberType.BIG_INTEGER, recordedType[0]);
    }

    // Tests acceptJsonFormatVisitor for BigDecimal serializer detecting Defects4J 34b
    @Test
    public void testAcceptJsonFormatVisitor_bigDecimal_visitsFloatFormatWithBigDecimalType() throws Exception {
        final JsonParser.NumberType[] recordedType = new JsonParser.NumberType[1];
        final boolean[] floatVisited = new boolean[1];

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
                floatVisited[0] = true;
                return new JsonNumberFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        recordedType[0] = type;
                    }
                };
            }
        };

        JavaType javaType = TypeFactory.defaultInstance().constructType(BigDecimal.class);
        bigDecimalSerializer.acceptJsonFormatVisitor(visitor, javaType);

        assertTrue(floatVisited[0]);
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, recordedType[0]);
    }

    // Tests acceptJsonFormatVisitor fallback for generic Number serializer
    @Test
    public void testAcceptJsonFormatVisitor_genericNumber_callsExpectNumberFormat() throws Exception {
        final boolean[] numberVisited = new boolean[1];

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
                numberVisited[0] = true;
                return new JsonNumberFormatVisitor.Base();
            }
        };

        JavaType javaType = TypeFactory.defaultInstance().constructType(Number.class);
        standardSerializer.acceptJsonFormatVisitor(visitor, javaType);

        assertTrue(numberVisited[0]);
    }
}