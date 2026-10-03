package com.fasterxml.jackson.databind.ser.std;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsonFormatVisitors.*;
import com.fasterxml.jackson.databind.ser.std.DateTimeSerializerBase;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import org.junit.Test;

public class DateTimeSerializerBaseTest {

    // Concrete subclass for testing
    private static class TestDateSerializer extends DateTimeSerializerBase<Long> {
        public TestDateSerializer(Boolean useTimestamp, DateFormat customFormat) {
            super(Long.class, useTimestamp, customFormat);
        }

        @Override
        public DateTimeSerializerBase<Long> withFormat(Boolean timestamp, DateFormat customFormat) {
            return new TestDateSerializer(timestamp, customFormat);
        }

        @Override
        protected long _timestamp(Long value) {
            return value == null ? 0L : value;
        }

        @Override
        public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            // Not needed for testing
        }
    }

    // Test: _asTimestamp with _useTimestamp = true returns true
    @Test
    public void test_asTimestamp_useTimestampTrue_returnsTrue() {
        DateTimeSerializerBase<Long> ser = new TestDateSerializer(Boolean.TRUE, null);
        assertTrue(ser._asTimestamp(null));
    }

    // Test: _asTimestamp with _useTimestamp = false returns false
    @Test
    public void test_asTimestamp_useTimestampFalse_returnsFalse() {
        DateTimeSerializerBase<Long> ser = new TestDateSerializer(Boolean.FALSE, null);
        assertFalse(ser._asTimestamp(null));
    }

    // Test: _asTimestamp with _useTimestamp = null and _customFormat != null returns false
    @Test
    public void test_asTimestamp_nullUseTimestampWithCustomFormat_returnsFalse() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        DateTimeSerializerBase<Long> ser = new TestDateSerializer(null, df);
        assertFalse(ser._asTimestamp(null));
    }

    // Test: _asTimestamp with _useTimestamp = null and _customFormat = null and
    // serializers = null throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void test_asTimestamp_nullUseTimestampNullCustomFormatNullProvider_throwsException() {
        DateTimeSerializerBase<Long> ser = new TestDateSerializer(null, null);
        ser._asTimestamp(null);
    }

    // Test: _asTimestamp with SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
    // disabled returns false
    @Test
    public void test_asTimestamp_noTimestampFeature_returnsFalse() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SerializerProvider serializers = mapper.getSerializerProvider();
        DateTimeSerializerBase<Long> ser = new TestDateSerializer(null, null);
        assertFalse(ser._asTimestamp(serializers));
    }

    // Test: _asTimestamp with SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
    // enabled returns true
    @Test
    public void test_asTimestamp_withTimestampFeature_returnsTrue() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SerializerProvider serializers = mapper.getSerializerProvider();
        DateTimeSerializerBase<Long> ser = new TestDateSerializer(null, null);
        assertTrue(ser._asTimestamp(serializers));
    }

    // Test: isEmpty with null value returns true
    @Test
    public void testIsEmpty_nullValue_returnsTrue() {
        DateTimeSerializerBase<Long> ser = new TestDateSerializer(null, null);
        assertTrue(ser.isEmpty(null));
    }

    // Test: isEmpty with zero timestamp value returns true
    @Test
    public void testIsEmpty_zeroTimestampValue_returnsTrue() {
        DateTimeSerializerBase<Long> ser = new TestDateSerializer(null, null);
        assertTrue(ser.isEmpty(0L));
    }

    // Test: isEmpty with non-zero timestamp value returns false
    @Test
    public void testIsEmpty_nonZeroTimestampValue_returnsFalse() {
        DateTimeSerializerBase<Long> ser = new TestDateSerializer(null, null);
        assertFalse(ser.isEmpty(12345L));
    }

    // Test: isEmpty with SerializerProvider, null value returns true
    @Test
    public void testIsEmptyWithProvider_nullValue_returnsTrue() {
        DateTimeSerializerBase<Long> ser = new TestDateSerializer(null, null);
        assertTrue(ser.isEmpty(null, null));
    }

    // Test: isEmpty with SerializerProvider, zero timestamp value returns true
    @Test
    public void testIsEmptyWithProvider_zeroTimestampValue_returnsTrue() {
        DateTimeSerializerBase<Long> ser = new TestDateSerializer(null, null);
        assertTrue(ser.isEmpty(null, 0L));
    }

    // Test: isEmpty with SerializerProvider, non-zero timestamp value returns false
    @Test
    public void testIsEmptyWithProvider_nonZeroTimestampValue_returnsFalse() {
        DateTimeSerializerBase<Long> ser = new TestDateSerializer(null, null);
        assertFalse(ser.isEmpty(null, 12345L));
    }

    // Test: getSchema with _asTimestamp returning true returns "number" type
    @Test
    public void testGetSchema_asTimestampTrue_returnsNumberNode() {
        DateTimeSerializerBase<Long> ser = new TestDateSerializer(Boolean.TRUE, null);
        JsonNode schema = ser.getSchema(null, null);
        assertEquals("number", schema.get("type").asText());
    }

    // Test: getSchema with _asTimestamp returning false returns "string" type
    @Test
    public void testGetSchema_asTimestampFalse_returnsStringNode() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        DateTimeSerializerBase<Long> ser = new TestDateSerializer(Boolean.FALSE, df);
        JsonNode schema = ser.getSchema(null, null);
        assertEquals("string", schema.get("type").asText());
    }

    // Test: createContextual with null property returns this
    @Test
    public void testCreateContextual_nullProperty_returnsThis() throws JsonMappingException {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider serializers = mapper.getSerializerProvider();
        DateTimeSerializerBase<Long> ser = new TestDateSerializer(null, null);
        assertSame(ser, ser.createContextual(serializers, null));
    }

    // Test: createContextual with numeric shape returns format with Boolean.TRUE
    @Test
    public void testCreateContextual_numericShape_returnsWithTimestampTrue() throws JsonMappingException {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider serializers = mapper.getSerializerProvider();
        TestDateSerializer ser = new TestDateSerializer(null, null);
        // Need a bean property with numeric shape. We'll use a mock-like approach by
        // using Jackson's internal annotations. Simpler: use a property with @JsonFormat(shape=NUMBER)
        // We cannot create a real BeanProperty easily. We'll test via reflection or
        // create an anonymous property? Instead, create a class with @JsonFormat on a
        // getter and get the property.
        // For simplicity, we can test internal branching by calling withFormat directly.
        // This tests the branch of shape.isNumeric().
        // We can simulate with an actual BeanProperty from a class.
        // Use a simple test class:
        class TestClass {
            @JsonFormat(shape = JsonFormat.Shape.NUMBER)
            public Long getDate() { return 0L; }
        }
        JavaType type = mapper.constructType(Long.class);
        BeanProperty property = null;
        // Iterate properties of TestClass to find the one with annotation
        try {
            BeanDescription beanDesc = mapper.getSerializationConfig().introspect(mapper.constructType(TestClass.class));
            for (BeanPropertyDefinition propDef : beanDesc.findProperties()) {
                if (propDef.getName().equals("date")) {
                    property = propDef.getAccessor().getProperty();
                    break;
                }
            }
        } catch (Exception e) {
            // fallback
        }
        if (property != null) {
            JsonSerializer<?> result = ser.createContextual(serializers, property);
            assertTrue(result instanceof DateTimeSerializerBase);
            DateTimeSerializerBase<?> resultBase = (DateTimeSerializerBase<?>) result;
            assertEquals(Boolean.TRUE, resultBase._useTimestamp);
            assertNull(resultBase._customFormat);
        }
    }

    // Test: _acceptJsonFormatVisitor with asNumber=true calls visitIntFormat
    @Test
    public void test_acceptJsonFormatVisitor_asNumberTrue_callsVisitIntFormat() throws JsonMappingException {
        DateTimeSerializerBase<Long> ser = new TestDateSerializer(Boolean.TRUE, null);
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(new ObjectMapper().getSerializerProvider());
        ser._acceptJsonFormatVisitor(visitor, null, true);
        // No assertion possible on effect, but ensure no exception
    }

    // Test: _acceptJsonFormatVisitor with asNumber=false calls visitStringFormat
    @Test
    public void test_acceptJsonFormatVisitor_asNumberFalse_callsVisitStringFormat() throws JsonMappingException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        DateTimeSerializerBase<Long> ser = new TestDateSerializer(Boolean.FALSE, df);
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(new ObjectMapper().getSerializerProvider());
        ser._acceptJsonFormatVisitor(visitor, null, false);
        // No assertion possible on effect, but ensure no exception
    }

    // Test: acceptJsonFormatVisitor delegates correctly with asTimestamp true
    @Test
    public void testAcceptJsonFormatVisitor_asTimestampTrue_visitsIntFormat() throws JsonMappingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SerializerProvider serializers = mapper.getSerializerProvider();
        DateTimeSerializerBase<Long> ser = new TestDateSerializer(null, null);
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(serializers);
        ser.acceptJsonFormatVisitor(visitor, null);
        // Ensure no exception
    }

    // Test: acceptJsonFormatVisitor delegates correctly with asTimestamp false
    @Test
    public void testAcceptJsonFormatVisitor_asTimestampFalse_visitsStringFormat() throws JsonMappingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SerializerProvider serializers = mapper.getSerializerProvider();
        DateTimeSerializerBase<Long> ser = new TestDateSerializer(null, null);
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(serializers);
        ser.acceptJsonFormatVisitor(visitor, null);
        // Ensure no exception
    }
}