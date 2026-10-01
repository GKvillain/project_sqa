package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.StdDateFormat;

public class DateTimeSerializerBaseTest {

    private ObjectMapper mapper;

    static class DummyDateTimeSerializer extends DateTimeSerializerBase<Date> {
        public DummyDateTimeSerializer() {
            this(null, null);
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

        public boolean checkAsTimestamp(SerializerProvider serializers) {
            return _asTimestamp(serializers);
        }

        public Boolean getUseTimestamp() {
            return _useTimestamp;
        }

        public DateFormat getCustomFormat() {
            return _customFormat;
        }
    }

    static class WrapperNumberBean {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public Date date;

        public WrapperNumberBean(Date date) {
            this.date = date;
        }
    }

    static class WrapperPatternBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd", timezone = "UTC")
        public Date date;

        public WrapperPatternBean(Date date) {
            this.date = date;
        }
    }

    static class WrapperLocaleBean {
        @JsonFormat(locale = "fr_FR", timezone = "GMT+2")
        public Date date;

        public WrapperLocaleBean(Date date) {
            this.date = date;
        }
    }

    static class WrapperTimeZoneOnlyBean {
        @JsonFormat(timezone = "PST")
        public Date date;

        public WrapperTimeZoneOnlyBean(Date date) {
            this.date = date;
        }
    }

    static class WrapperLocaleOnlyBean {
        @JsonFormat(locale = "de_DE")
        public Date date;

        public WrapperLocaleOnlyBean(Date date) {
            this.date = date;
        }
    }

    static class WrapperStringNoPatternBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public Date date;

        public WrapperStringNoPatternBean(Date date) {
            this.date = date;
        }
    }

    static class WrapperAnyShapeBean {
        @JsonFormat(shape = JsonFormat.Shape.ANY)
        public Date date;

        public WrapperAnyShapeBean(Date date) {
            this.date = date;
        }
    }

    static class WrapperDefaultBean {
        public Date date;

        public WrapperDefaultBean(Date date) {
            this.date = date;
        }
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // Tests isEmpty with single argument on null value
    @Test
    public void testIsEmpty_nullValue_returnsTrue() {
        DummyDateTimeSerializer ser = new DummyDateTimeSerializer();
        assertTrue(ser.isEmpty(null));
    }

    // Tests isEmpty with single argument on zero timestamp (timestamp 0 qualifies as empty)
    @Test
    public void testIsEmpty_zeroTimestamp_returnsTrue() {
        DummyDateTimeSerializer ser = new DummyDateTimeSerializer();
        assertTrue(ser.isEmpty(new Date(0L)));
    }

    // Tests isEmpty with single argument on non-zero timestamp
    @Test
    public void testIsEmpty_nonZeroTimestamp_returnsFalse() {
        DummyDateTimeSerializer ser = new DummyDateTimeSerializer();
        assertFalse(ser.isEmpty(new Date(123456789L)));
    }

    // Tests isEmpty with SerializerProvider parameter on null value
    @Test
    public void testIsEmptyWithProvider_nullValue_returnsTrue() {
        DummyDateTimeSerializer ser = new DummyDateTimeSerializer();
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        assertTrue(ser.isEmpty(provider, null));
    }

    // Tests isEmpty with SerializerProvider parameter on non-zero timestamp
    @Test
    public void testIsEmptyWithProvider_nonZeroTimestamp_returnsFalse() {
        DummyDateTimeSerializer ser = new DummyDateTimeSerializer();
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        assertFalse(ser.isEmpty(provider, new Date(5000L)));
    }

    // Tests _asTimestamp when _useTimestamp is explicitly set to true
    @Test
    public void testAsTimestamp_explicitUseTimestampTrue_returnsTrue() {
        DummyDateTimeSerializer ser = new DummyDateTimeSerializer(Boolean.TRUE, null);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        assertTrue(ser.checkAsTimestamp(provider));
    }

    // Tests _asTimestamp when _useTimestamp is explicitly set to false
    @Test
    public void testAsTimestamp_explicitUseTimestampFalse_returnsFalse() {
        DummyDateTimeSerializer ser = new DummyDateTimeSerializer(Boolean.FALSE, null);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        assertFalse(ser.checkAsTimestamp(provider));
    }

    // Tests _asTimestamp when customFormat is provided and useTimestamp is null
    @Test
    public void testAsTimestamp_customFormatProvided_returnsFalse() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        DummyDateTimeSerializer ser = new DummyDateTimeSerializer(null, df);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        assertFalse(ser.checkAsTimestamp(provider));
    }

    // Tests _asTimestamp relying on provider feature WRITE_DATES_AS_TIMESTAMPS enabled
    @Test
    public void testAsTimestamp_featureEnabled_returnsTrue() {
        DummyDateTimeSerializer ser = new DummyDateTimeSerializer(null, null);
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        assertTrue(ser.checkAsTimestamp(provider));
    }

    // Tests _asTimestamp relying on provider feature WRITE_DATES_AS_TIMESTAMPS disabled
    @Test
    public void testAsTimestamp_featureDisabled_returnsFalse() {
        DummyDateTimeSerializer ser = new DummyDateTimeSerializer(null, null);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        assertFalse(ser.checkAsTimestamp(provider));
    }

    // Tests _asTimestamp throws IllegalArgumentException when provider is null and no timestamp flag
    @Test(expected = IllegalArgumentException.class)
    public void testAsTimestamp_nullProvider_throwsIllegalArgumentException() {
        DummyDateTimeSerializer ser = new DummyDateTimeSerializer(null, null);
        ser.checkAsTimestamp(null);
    }

    // Tests getSchema when serialized as timestamp
    @Test
    public void testGetSchema_asTimestamp_returnsNumberSchema() throws Exception {
        DummyDateTimeSerializer ser = new DummyDateTimeSerializer(Boolean.TRUE, null);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonNode schemaNode = ser.getSchema(provider, (Type) Date.class);
        assertNotNull(schemaNode);
        assertEquals("number", schemaNode.get("type").asText());
    }

    // Tests getSchema when serialized as formatted string
    @Test
    public void testGetSchema_asString_returnsStringSchema() throws Exception {
        DummyDateTimeSerializer ser = new DummyDateTimeSerializer(Boolean.FALSE, null);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonNode schemaNode = ser.getSchema(provider, (Type) Date.class);
        assertNotNull(schemaNode);
        assertEquals("string", schemaNode.get("type").asText());
    }

    // Tests acceptJsonFormatVisitor format visitor invocation for timestamp format
    @Test
    public void testAcceptJsonFormatVisitor_asTimestamp_invokesIntegerVisitor() throws Exception {
        DummyDateTimeSerializer ser = new DummyDateTimeSerializer(Boolean.TRUE, null);
        final boolean[] visitedInteger = new boolean[1];
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(mapper.getSerializerProviderInstance()) {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                visitedInteger[0] = true;
                return super.expectIntegerFormat(type);
            }
        };
        JavaType javaType = TypeFactory.defaultInstance().constructType(Date.class);
        ser.acceptJsonFormatVisitor(visitor, javaType);
        assertTrue(visitedInteger[0]);
    }

    // Tests acceptJsonFormatVisitor format visitor invocation for string format
    @Test
    public void testAcceptJsonFormatVisitor_asString_invokesStringVisitor() throws Exception {
        DummyDateTimeSerializer ser = new DummyDateTimeSerializer(Boolean.FALSE, null);
        final boolean[] visitedString = new boolean[1];
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(mapper.getSerializerProviderInstance()) {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                visitedString[0] = true;
                return new JsonStringFormatVisitor.Base() {
                    @Override
                    public void format(JsonValueFormat format) {
                        assertEquals(JsonValueFormat.DATE_TIME, format);
                    }
                };
            }
        };
        JavaType javaType = TypeFactory.defaultInstance().constructType(Date.class);
        ser.acceptJsonFormatVisitor(visitor, javaType);
        assertTrue(visitedString[0]);
    }

    // Tests createContextual with null property returns same instance
    @Test
    public void testCreateContextual_nullProperty_returnsSelf() throws Exception {
        DummyDateTimeSerializer ser = new DummyDateTimeSerializer();
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        JsonSerializer<?> contextual = ser.createContextual(provider, null);
        assertSame(ser, contextual);
    }

    // Tests createContextual on property without @JsonFormat override returns same instance
    @Test
    public void testCreateContextual_noFormatAnnotation_returnsSelf() throws Exception {
        DummyDateTimeSerializer ser = new DummyDateTimeSerializer();
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        BeanProperty prop = new BeanProperty.Bogus();
        JsonSerializer<?> contextual = ser.createContextual(provider, prop);
        assertSame(ser, contextual);
    }

    // Tests createContextual with Shape.NUMBER sets useTimestamp to true
    @Test
    public void testCreateContextual_numericShape_returnsTimestampSerializer() throws Exception {
        Date date = new Date(1000000000000L);
        String json = mapper.writeValueAsString(new WrapperNumberBean(date));
        assertEquals("{\"date\":1000000000000}", json);
    }

    // Tests createContextual with custom pattern and timezone
    @Test
    public void testCreateContextual_patternAndTimeZone_serializesWithFormat() throws Exception {
        Date date = new Date(0L);
        String json = mapper.writeValueAsString(new WrapperPatternBean(date));
        assertEquals("{\"date\":\"1970/01/01\"}", json);
    }

    // Tests createContextual with custom locale
    @Test
    public void testCreateContextual_localeConfigured_serializesFormattedString() throws Exception {
        Date date = new Date(0L);
        String json = mapper.writeValueAsString(new WrapperLocaleBean(date));
        assertNotNull(json);
        assertTrue(json.contains("1970"));
    }

    // Tests createContextual with timezone only (no pattern)
    @Test
    public void testCreateContextual_timezoneOnly_serializesFormattedString() throws Exception {
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Date date = new Date(0L);
        String json = mapper.writeValueAsString(new WrapperTimeZoneOnlyBean(date));
        assertNotNull(json);
        assertTrue(json.contains("1969") || json.contains("1970"));
    }

    // Tests createContextual with locale only (no pattern)
    @Test
    public void testCreateContextual_localeOnly_serializesFormattedString() throws Exception {
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Date date = new Date(0L);
        String json = mapper.writeValueAsString(new WrapperLocaleOnlyBean(date));
        assertNotNull(json);
        assertTrue(json.contains("1970"));
    }

    // Tests serialization with custom DateFormat on ObjectMapper and Shape.STRING on property
    @Test
    public void testCreateContextual_mapperCustomDateFormatWithShapeString_serializesCorrectly() throws Exception {
        SimpleDateFormat customDf = new SimpleDateFormat("yyyy.MM.dd", Locale.US);
        customDf.setTimeZone(TimeZone.getTimeZone("UTC"));
        mapper.setDateFormat(customDf);

        Date date = new Date(0L);
        String json = mapper.writeValueAsString(new WrapperStringNoPatternBean(date));
        assertNotNull(json);
        assertTrue(json.contains("1970.01.01"));
    }

    // Tests createContextual with StdDateFormat on ObjectMapper and Shape.STRING on property
    @Test
    public void testCreateContextual_stdDateFormatWithShapeString_serializesCorrectly() throws Exception {
        mapper.setDateFormat(StdDateFormat.instance);
        Date date = new Date(0L);
        String json = mapper.writeValueAsString(new WrapperStringNoPatternBean(date));
        assertNotNull(json);
        assertTrue(json.contains("1970-01-01"));
    }

    // Tests serialization when Shape is ANY without pattern, timezone, or locale
    @Test
    public void testCreateContextual_anyShapeDefault_serializesDefault() throws Exception {
        Date date = new Date(1000L);
        String json = mapper.writeValueAsString(new WrapperAnyShapeBean(date));
        assertNotNull(json);
    }
}