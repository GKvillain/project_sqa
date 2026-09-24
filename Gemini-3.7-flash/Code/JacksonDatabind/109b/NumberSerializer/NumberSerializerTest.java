package com.fasterxml.jackson.databind.ser.std;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

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
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class NumberSerializerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonFactory jsonFactory = new JsonFactory();

    static class CustomNumber extends Number {
        private final String value;

        public CustomNumber(String value) {
            this.value = value;
        }

        @Override
        public int intValue() { return Integer.parseInt(value); }
        @Override
        public long longValue() { return Long.parseLong(value); }
        @Override
        public float floatValue() { return Float.parseFloat(value); }
        @Override
        public double doubleValue() { return Double.parseDouble(value); }
        @Override
        public String toString() { return value; }
    }

    static class FormattedStringNumberWrapper {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public BigDecimal bigDecimalValue;

        public FormattedStringNumberWrapper(BigDecimal value) {
            this.bigDecimalValue = value;
        }
    }

    static class FormattedNumberWrapper {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public BigDecimal bigDecimalValue;

        public FormattedNumberWrapper(BigDecimal value) {
            this.bigDecimalValue = value;
        }
    }

    // Tests serialize with BigDecimal value
    @Test
    public void testSerialize_bigDecimalValue_writesBigDecimal() throws Exception {
        NumberSerializer serializer = NumberSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator g = jsonFactory.createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        serializer.serialize(new BigDecimal("123.456"), g, prov);
        g.close();

        assertEquals("123.456", sw.toString());
    }

    // Tests serialize with BigInteger value
    @Test
    public void testSerialize_bigIntegerValue_writesBigInteger() throws Exception {
        NumberSerializer serializer = new NumberSerializer(BigInteger.class);
        StringWriter sw = new StringWriter();
        JsonGenerator g = jsonFactory.createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        serializer.serialize(new BigInteger("9876543210987654321"), g, prov);
        g.close();

        assertEquals("9876543210987654321", sw.toString());
    }

    // Tests serialize with Long value
    @Test
    public void testSerialize_longValue_writesLong() throws Exception {
        NumberSerializer serializer = NumberSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator g = jsonFactory.createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        serializer.serialize(Long.valueOf(123456789L), g, prov);
        g.close();

        assertEquals("123456789", sw.toString());
    }

    // Tests serialize with Double value
    @Test
    public void testSerialize_doubleValue_writesDouble() throws Exception {
        NumberSerializer serializer = NumberSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator g = jsonFactory.createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        serializer.serialize(Double.valueOf(3.1415), g, prov);
        g.close();

        assertEquals("3.1415", sw.toString());
    }

    // Tests serialize with Float value
    @Test
    public void testSerialize_floatValue_writesFloat() throws Exception {
        NumberSerializer serializer = NumberSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator g = jsonFactory.createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        serializer.serialize(Float.valueOf(1.25f), g, prov);
        g.close();

        assertEquals("1.25", sw.toString());
    }

    // Tests serialize with Integer, Short, Byte values
    @Test
    public void testSerialize_integerValues_writesInt() throws Exception {
        NumberSerializer serializer = NumberSerializer.instance;
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        StringWriter sw1 = new StringWriter();
        JsonGenerator g1 = jsonFactory.createGenerator(sw1);
        serializer.serialize(Integer.valueOf(42), g1, prov);
        g1.close();
        assertEquals("42", sw1.toString());

        StringWriter sw2 = new StringWriter();
        JsonGenerator g2 = jsonFactory.createGenerator(sw2);
        serializer.serialize(Short.valueOf((short) 10), g2, prov);
        g2.close();
        assertEquals("10", sw2.toString());

        StringWriter sw3 = new StringWriter();
        JsonGenerator g3 = jsonFactory.createGenerator(sw3);
        serializer.serialize(Byte.valueOf((byte) 5), g3, prov);
        g3.close();
        assertEquals("5", sw3.toString());
    }

    // Tests serialize fallback branch for custom Number implementation
    @Test
    public void testSerialize_customNumber_writesStringRepresentation() throws Exception {
        NumberSerializer serializer = NumberSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator g = jsonFactory.createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        serializer.serialize(new CustomNumber("999999"), g, prov);
        g.close();

        assertEquals("999999", sw.toString());
    }

    // Tests getSchema for integer number serializer
    @Test
    public void testGetSchema_bigIntegerType_returnsIntegerSchema() throws Exception {
        NumberSerializer serializer = new NumberSerializer(BigInteger.class);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        JsonNode schema = serializer.getSchema(prov, BigInteger.class);
        assertNotNull(schema);
        assertEquals("integer", schema.get("type").asText());
    }

    // Tests getSchema for standard number serializer
    @Test
    public void testGetSchema_numberType_returnsNumberSchema() throws Exception {
        NumberSerializer serializer = NumberSerializer.instance;
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        JsonNode schema = serializer.getSchema(prov, Number.class);
        assertNotNull(schema);
        assertEquals("number", schema.get("type").asText());
    }

    // Tests createContextual when property is null
    @Test
    public void testCreateContextual_nullProperty_returnsSelf() throws Exception {
        NumberSerializer serializer = NumberSerializer.instance;
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        JsonSerializer<?> result = serializer.createContextual(prov, null);
        assertSame(serializer, result);
    }

    // Tests createContextual with String shape override
    @Test
    public void testCreateContextual_stringShape_returnsToStringSerializer() throws Exception {
        NumberSerializer serializer = new NumberSerializer(BigDecimal.class);
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        BeanProperty prop = new BeanProperty.Bogus();

        String json = mapper.writeValueAsString(new FormattedStringNumberWrapper(new BigDecimal("123.45")));
        assertEquals("{\"bigDecimalValue\":\"123.45\"}", json);
    }

    // Tests createContextual with Number shape override
    @Test
    public void testCreateContextual_numberShape_returnsSelf() throws Exception {
        String json = mapper.writeValueAsString(new FormattedNumberWrapper(new BigDecimal("123.45")));
        assertEquals("{\"bigDecimalValue\":123.45}", json);
    }

    // Tests acceptJsonFormatVisitor for BigInteger, BigDecimal, and Number types
    @Test
    public void testAcceptJsonFormatVisitor_allTypes_acceptsVisitorWithoutError() throws Exception {
        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base();

        NumberSerializer intSerializer = new NumberSerializer(BigInteger.class);
        JavaType bigIntType = TypeFactory.defaultInstance().constructType(BigInteger.class);
        intSerializer.acceptJsonFormatVisitor(visitor, bigIntType);

        NumberSerializer decSerializer = new NumberSerializer(BigDecimal.class);
        JavaType bigDecType = TypeFactory.defaultInstance().constructType(BigDecimal.class);
        decSerializer.acceptJsonFormatVisitor(visitor, bigDecType);

        NumberSerializer numSerializer = NumberSerializer.instance;
        JavaType numType = TypeFactory.defaultInstance().constructType(Number.class);
        numSerializer.acceptJsonFormatVisitor(visitor, numType);
    }
}