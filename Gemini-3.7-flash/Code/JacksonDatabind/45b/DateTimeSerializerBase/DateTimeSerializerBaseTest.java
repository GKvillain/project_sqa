package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;

public class DateTimeSerializerBaseTest {

    private static class DummyDateTimeSerializer extends DateTimeSerializerBase<Date> {
        private static final long serialVersionUID = 1L;

        public DummyDateTimeSerializer() {
            super(Date.class, null, null);
        }

        public DummyDateTimeSerializer(Boolean useTimestamp, DateFormat customFormat) {
            super(Date.class, useTimestamp, customFormat);
        }

        @Override
        public DateTimeSerializerBase<Date> withFormat(Boolean timestamp, DateFormat customFormat) {
            return new DummyDateTimeSerializer(timestamp, customFormat);
        }

        @Override
        protected long _timestamp(Date value) {
            return (value == null) ? 0L : value.getTime();
        }

        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (_asTimestamp(serializers)) {
                gen.writeNumber(_timestamp(value));
            } else if (_customFormat != null) {
                synchronized (_customFormat) {
                    gen.writeString(_customFormat.format(value));
                }
            } else {
                serializers.defaultSerializeDateValue(value, gen);
            }
        }

        public Boolean getUseTimestamp() {
            return _useTimestamp;
        }

        public DateFormat getCustomFormat() {
            return _customFormat;
        }

        public boolean publicAsTimestamp(SerializerProvider serializers) {
            return _asTimestamp(serializers);
        }
    }

    static class DatePatternBean {
        @JsonFormat(pattern = "yyyy/MM/dd", timezone = "UTC")
        public Date date;

        public DatePatternBean(Date date) {
            this.date = date;
        }
    }

    static class DateShapeNumberBean {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public Date date;

        public DateShapeNumberBean(Date date) {
            this.date = date;
        }
    }

    static class DateShapeStringBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        public Date date;

        public DateShapeStringBean(Date date) {
            this.date = date;
        }
    }

    static class DateTimeZoneBean {
        @JsonFormat(timezone = "GMT+2")
        public Date date;

        public DateTimeZoneBean(Date date) {
            this.date = date;
        }
    }

    static class DateLocaleBean {
        @JsonFormat(locale = "fr_FR")
        public Date date;

        public DateLocaleBean(Date date) {
            this.date = date;
        }
    }

    // Tests defect fix: @JsonFormat with pattern but default Shape.ANY should format as string
    @Test
    public void testContextual_patternWithoutExplicitShape_serializesWithPattern() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Date date = new Date(0L);
        String json = mapper.writeValueAsString(new DatePatternBean(date));
        assertEquals("{\"date\":\"1970/01/01\"}", json);
    }

    // Tests @JsonFormat with shape = NUMBER
    @Test
    public void testContextual_shapeNumber_serializesAsTimestamp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Date date = new Date(123456789L);
        String json = mapper.writeValueAsString(new DateShapeNumberBean(date));
        assertEquals("{\"date\":123456789}", json);
    }

    // Tests @JsonFormat with shape = STRING without custom pattern (uses ISO8601)
    @Test
    public void testContextual_shapeStringWithoutPattern_serializesAsISO8601() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Date date = new Date(0L);
        String json = mapper.writeValueAsString(new DateShapeStringBean(date));
        assertEquals("{\"date\":\"1970-01-01T00:00:00.000+0000\"}", json);
    }

    // Tests @JsonFormat with timezone only
    @Test
    public void testContextual_timezoneOnly_formatsAsString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Date date = new Date(0L);
        String json = mapper.writeValueAsString(new DateTimeZoneBean(date));
        assertTrue(json.contains("1970-01-01"));
    }

    // Tests @JsonFormat with locale only
    @Test
    public void testContextual_localeOnly_formatsAsString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Date date = new Date(0L);
        String json = mapper.writeValueAsString(new DateLocaleBean(date));
        assertNotNull(json);
        assertTrue(json.contains("date"));
    }

    // Tests createContextual when property is null returns same serializer
    @Test
    public void testCreateContextual_nullProperty_returnsSelf() throws Exception {
        DummyDateTimeSerializer serializer = new DummyDateTimeSerializer();
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        JsonSerializer<?> result = serializer.createContextual(provider, null);
        assertSame(serializer, result);
    }

    // Tests isEmpty(T) with null date
    @Test
    public void testIsEmpty_nullValue_returnsTrue() {
        DummyDateTimeSerializer serializer = new DummyDateTimeSerializer();
        assertTrue(serializer.isEmpty(null));
    }

    // Tests isEmpty(T) with zero timestamp
    @Test
    public void testIsEmpty_zeroTimestamp_returnsTrue() {
        DummyDateTimeSerializer serializer = new DummyDateTimeSerializer();
        assertTrue(serializer.isEmpty(new Date(0L)));
    }

    // Tests isEmpty(T) with non-zero timestamp
    @Test
    public void testIsEmpty_nonZeroTimestamp_returnsFalse() {
        DummyDateTimeSerializer serializer = new DummyDateTimeSerializer();
        assertFalse(serializer.isEmpty(new Date(1000L)));
    }

    // Tests isEmpty(SerializerProvider, T)
    @Test
    public void testIsEmptyWithProvider_cases() {
        DummyDateTimeSerializer serializer = new DummyDateTimeSerializer();
        assertTrue(serializer.isEmpty(null, null));
        assertTrue(serializer.isEmpty(null, new Date(0L)));
        assertFalse(serializer.isEmpty(null, new Date(5000L)));
    }

    // Tests _asTimestamp with explicit Boolean.TRUE
    @Test
    public void testAsTimestamp_explicitTrue_returnsTrue() {
        DummyDateTimeSerializer serializer = new DummyDateTimeSerializer(Boolean.TRUE, null);
        assertTrue(serializer.publicAsTimestamp(null));
    }

    // Tests _asTimestamp with explicit Boolean.FALSE
    @Test
    public void testAsTimestamp_explicitFalse_returnsFalse() {
        DummyDateTimeSerializer serializer = new DummyDateTimeSerializer(Boolean.FALSE, null);
        assertFalse(serializer.publicAsTimestamp(null));
    }

    // Tests _asTimestamp with custom format returns false
    @Test
    public void testAsTimestamp_withCustomFormat_returnsFalse() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        DummyDateTimeSerializer serializer = new DummyDateTimeSerializer(null, df);
        assertFalse(serializer.publicAsTimestamp(null));
    }

    // Tests _asTimestamp with null provider throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testAsTimestamp_nullProviderAndNoFormat_throwsIllegalArgumentException() {
        DummyDateTimeSerializer serializer = new DummyDateTimeSerializer(null, null);
        serializer.publicAsTimestamp(null);
    }

    // Tests _asTimestamp delegates to SerializerProvider WRITE_DATES_AS_TIMESTAMPS feature
    @Test
    public void testAsTimestamp_providerFeatureFlag() throws Exception {
        DummyDateTimeSerializer serializer = new DummyDateTimeSerializer(null, null);

        ObjectMapper mapperEnabled = new ObjectMapper();
        mapperEnabled.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SerializerProvider providerEnabled = mapperEnabled.getSerializerProviderInstance();
        assertTrue(serializer.publicAsTimestamp(providerEnabled));

        ObjectMapper mapperDisabled = new ObjectMapper();
        mapperDisabled.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SerializerProvider providerDisabled = mapperDisabled.getSerializerProviderInstance();
        assertFalse(serializer.publicAsTimestamp(providerDisabled));
    }

    // Tests getSchema returns number schema when serialized as timestamp
    @Test
    public void testGetSchema_asTimestamp_returnsNumberSchema() throws Exception {
        DummyDateTimeSerializer serializer = new DummyDateTimeSerializer(Boolean.TRUE, null);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        JsonNode schema = serializer.getSchema(provider, Date.class);
        assertNotNull(schema);
        assertEquals("number", schema.get("type").asText());
    }

    // Tests getSchema returns string schema when serialized as formatted string
    @Test
    public void testGetSchema_asString_returnsStringSchema() throws Exception {
        DummyDateTimeSerializer serializer = new DummyDateTimeSerializer(Boolean.FALSE, null);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        JsonNode schema = serializer.getSchema(provider, Date.class);
        assertNotNull(schema);
        assertEquals("string", schema.get("type").asText());
    }

    // Tests acceptJsonFormatVisitor for number branch and string branch
    @Test
    public void testAcceptJsonFormatVisitor_timestampAndString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(Date.class);

        DummyDateTimeSerializer timestampSerializer = new DummyDateTimeSerializer(Boolean.TRUE, null);
        JsonFormatVisitorWrapper.Base visitor1 = new JsonFormatVisitorWrapper.Base(mapper.getSerializerProviderInstance());
        timestampSerializer.acceptJsonFormatVisitor(visitor1, type);

        DummyDateTimeSerializer stringSerializer = new DummyDateTimeSerializer(Boolean.FALSE, null);
        JsonFormatVisitorWrapper.Base visitor2 = new JsonFormatVisitorWrapper.Base(mapper.getSerializerProviderInstance());
        stringSerializer.acceptJsonFormatVisitor(visitor2, type);
    }
}